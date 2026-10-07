package com.e611.toponavi.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RequestFields {
    private RequestFields() {}

    public static void object(JsonNode value, Set<String> allowed, String path) {
        if (value == null || !value.isObject()) throw invalid(path, "Expected an object.");
        value.fieldNames().forEachRemaining(key -> {
            if (!allowed.contains(key)) throw invalid(path + "/" + key, "Unknown field.");
        });
    }

    public static JsonNode optionalObject(JsonNode body, String key) {
        if (!body.has(key)) return JsonNodeFactory.instance.objectNode();
        JsonNode value = body.get(key);
        if (!value.isObject()) throw invalid("/" + key, "Expected an object.");
        return value;
    }

    public static String text(JsonNode body, String key, String fallback) {
        if (!body.has(key)) return fallback;
        JsonNode value = body.get(key);
        if (!value.isTextual() || value.textValue().isBlank()) throw invalid("/" + key, "Expected a nonempty string.");
        return value.textValue();
    }

    public static boolean bool(JsonNode body, String key, boolean fallback) {
        if (!body.has(key)) return fallback;
        if (!body.get(key).isBoolean()) throw invalid("/" + key, "Expected a boolean.");
        return body.get(key).booleanValue();
    }

    public static List<String> strings(JsonNode body, String key) {
        if (!body.has(key)) return null;
        JsonNode value = body.get(key);
        if (!value.isArray()) throw invalid("/" + key, "Expected a list of strings.");
        List<String> result = new ArrayList<>();
        value.forEach(item -> {
            if (!item.isTextual() || item.textValue().isBlank()) throw invalid("/" + key, "Expected nonempty strings.");
            result.add(item.textValue());
        });
        return List.copyOf(result);
    }

    public static String qualified(String id, String path) {
        if (id == null || !id.matches("[^:\\s]+::[^:\\s]+")) {
            throw invalid(path, "Use a qualified mapId::localNodeId.");
        }
        return id;
    }

    public static ApiException invalid(String path, String message) {
        return new ApiException(400, "INVALID_REQUEST", message, java.util.Map.of("path", path));
    }
}
