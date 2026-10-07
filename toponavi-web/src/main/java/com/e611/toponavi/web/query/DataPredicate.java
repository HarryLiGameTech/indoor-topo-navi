package com.e611.toponavi.web.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/** Immutable predicates over public query records. */
public sealed interface DataPredicate extends Predicate<JsonNode> {
    record All(List<DataPredicate> children) implements DataPredicate {
        public All { children = List.copyOf(children); }
        public boolean test(JsonNode record) { return children.stream().allMatch(child -> child.test(record)); }
    }

    record Any(List<DataPredicate> children) implements DataPredicate {
        public Any { children = List.copyOf(children); }
        public boolean test(JsonNode record) { return children.stream().anyMatch(child -> child.test(record)); }
    }

    record Not(DataPredicate child) implements DataPredicate {
        public boolean test(JsonNode record) { return !child.test(record); }
    }

    record Field(List<String> path, String operator, JsonNode value) implements DataPredicate {
        public Field { path = List.copyOf(path); value = value.deepCopy(); }
        public boolean test(JsonNode record) {
            JsonNode actual = record;
            for (String segment : path) actual = actual.isObject() ? actual.path(segment) : MissingNode.getInstance();
            if (actual.isMissingNode()) return false;
            return switch (operator) {
                case "exists" -> true;
                case "isNull" -> actual.isNull();
                case "isNotNull" -> !actual.isNull();
                case "eq" -> equal(actual, value);
                case "ne" -> !equal(actual, value);
                case "in" -> contains(value, actual);
                case "contains" -> contains(actual, value);
                case "lt" -> actual.isNumber() && actual.decimalValue().compareTo(value.decimalValue()) < 0;
                case "lte" -> actual.isNumber() && actual.decimalValue().compareTo(value.decimalValue()) <= 0;
                case "gt" -> actual.isNumber() && actual.decimalValue().compareTo(value.decimalValue()) > 0;
                case "gte" -> actual.isNumber() && actual.decimalValue().compareTo(value.decimalValue()) >= 0;
                default -> throw new IllegalStateException("Unvalidated predicate operator");
            };
        }
    }

    private static boolean contains(JsonNode collection, JsonNode value) {
        return collection.isArray() && IntStream.range(0, collection.size())
                .anyMatch(index -> equal(collection.get(index), value));
    }

    private static boolean equal(JsonNode left, JsonNode right) {
        if (left.isNumber() && right.isNumber()) return left.decimalValue().compareTo(right.decimalValue()) == 0;
        if (left.isArray() && right.isArray()) return left.size() == right.size()
                && IntStream.range(0, left.size()).allMatch(index -> equal(left.get(index), right.get(index)));
        if (left.isObject() && right.isObject()) {
            if (left.size() != right.size()) return false;
            var fields = left.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                if (!right.has(field.getKey()) || !equal(field.getValue(), right.get(field.getKey()))) return false;
            }
            return true;
        }
        return left.equals(right);
    }
}
