package com.e611.toponavi.web.query;

import com.e611.toponavi.web.api.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FilterParser {
    private static final Set<String> CHECKS = Set.of("exists", "isNull", "isNotNull");
    private static final Set<String> NUMERIC = Set.of("lt", "lte", "gt", "gte");
    private static final Set<String> OPERATORS = Set.of("eq", "ne", "lt", "lte", "gt", "gte", "in", "contains", "exists", "isNull", "isNotNull");
    private final int maxDepth, maxPredicates, maxPathLength, maxLiteralBytes, maxMembership;

    public FilterParser(@Value("${platform.api.filter.max-depth:16}") int maxDepth,
                        @Value("${platform.api.filter.max-predicates:256}") int maxPredicates,
                        @Value("${platform.api.filter.max-path-length:1024}") int maxPathLength,
                        @Value("${platform.api.filter.max-literal-bytes:16384}") int maxLiteralBytes,
                        @Value("${platform.api.filter.max-membership:256}") int maxMembership) {
        if (Math.min(Math.min(maxDepth, maxPredicates), Math.min(maxPathLength, Math.min(maxLiteralBytes, maxMembership))) < 1)
            throw new IllegalArgumentException("Filter limits must be positive");
        this.maxDepth = maxDepth;
        this.maxPredicates = maxPredicates;
        this.maxPathLength = maxPathLength;
        this.maxLiteralBytes = maxLiteralBytes;
        this.maxMembership = maxMembership;
    }

    public DataPredicate parse(JsonNode filter, Set<String> fields) {
        if (filter == null) return new DataPredicate.All(List.of());
        return parse(filter, fields, "/filter", 1, new AtomicInteger());
    }

    private DataPredicate parse(JsonNode node, Set<String> fields, String path, int depth, AtomicInteger count) {
        if (depth > maxDepth || count.incrementAndGet() > maxPredicates) throw complex(path);
        if (!node.isObject()) throw invalid(path, "Expected a predicate object.");
        for (String compound : List.of("and", "or", "not")) {
            if (!node.has(compound)) continue;
            if (node.size() != 1) throw invalid(path, "Predicate forms cannot be mixed.");
            JsonNode children = node.get(compound);
            if (compound.equals("not")) return new DataPredicate.Not(parse(children, fields, path + "/not", depth + 1, count));
            if (!children.isArray() || children.isEmpty()) throw invalid(path + "/" + compound, "Expected a nonempty predicate list.");
            List<DataPredicate> parsed = new ArrayList<>();
            for (int index = 0; index < children.size(); index++) {
                parsed.add(parse(children.get(index), fields, path + "/" + compound + "/" + index, depth + 1, count));
            }
            return compound.equals("and") ? new DataPredicate.All(parsed) : new DataPredicate.Any(parsed);
        }
        if (!node.path("op").isTextual() || !OPERATORS.contains(node.path("op").textValue()))
            throw invalid(path + "/op", "Unknown predicate operator.");
        String operator = node.get("op").textValue();
        Set<String> keys = CHECKS.contains(operator) ? Set.of("field", "op") : Set.of("field", "op", "value");
        if (node.size() != keys.size() || !keys.stream().allMatch(node::has)) throw invalid(path, "Invalid predicate fields.");
        JsonNode field = node.get("field");
        List<String> segments = new ArrayList<>();
        if (field.isTextual()) segments.addAll(Arrays.asList(field.textValue().split("\\.", -1)));
        else if (field.isArray()) {
            for (JsonNode segment : field) {
                if (!segment.isTextual()) throw invalid(path + "/field", "Field segments must be strings.");
                segments.add(segment.textValue());
            }
        } else throw invalid(path + "/field", "Expected a field path or list of key segments.");
        if (segments.isEmpty() || segments.stream().anyMatch(String::isEmpty)) throw invalid(path + "/field", "Field segments cannot be empty.");
        if (segments.size() > maxDepth || segments.stream().mapToInt(String::length).sum() > maxPathLength) throw complex(path + "/field");
        if (!fields.contains(segments.get(0))) throw invalid(path + "/field", "Unknown field for this operation.");
        JsonNode value = node.get("value");
        if (value != null) {
            if (value.toString().getBytes(StandardCharsets.UTF_8).length > maxLiteralBytes || literalDepth(value) > maxDepth) throw complex(path + "/value");
            if (NUMERIC.contains(operator) && !value.isNumber()) throw invalid(path + "/value", "Numeric comparison requires a number.");
            if (operator.equals("in")) {
                if (!value.isArray()) throw invalid(path + "/value", "Membership requires a list.");
                if (value.size() > maxMembership) throw complex(path + "/value");
            }
        }
        return new DataPredicate.Field(segments, operator, value == null ? NullNode.getInstance() : value);
    }

    private int literalDepth(JsonNode value) {
        int depth = 0;
        for (JsonNode child : value) depth = Math.max(depth, literalDepth(child));
        return depth + 1;
    }

    private ApiException invalid(String path, String message) {
        return new ApiException(400, "INVALID_FILTER", message, Map.of("path", path));
    }

    private ApiException complex(String path) {
        return new ApiException(400, "FILTER_TOO_COMPLEX", "Filter exceeds configured structural limits.", Map.of("path", path));
    }
}
