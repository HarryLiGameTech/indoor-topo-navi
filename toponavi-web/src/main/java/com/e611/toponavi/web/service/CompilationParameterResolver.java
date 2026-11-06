package com.e611.toponavi.web.service;

import com.e611.toponavi.web.api.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks request {@code userParams} against the parameters declared by an inspected source set and converts them to
 * the typed values the compiler expects. It is independent of where the sources came from, so stored-building
 * computation and request-supplied validation share exactly the same coercion and error rules.
 */
@Component
public class CompilationParameterResolver {

    public Map<String, Object> resolve(JsonNode inspection, JsonNode userParams) {
        Map<String, String> declared = new LinkedHashMap<>();
        inspection.path("parameters").forEach(param -> declared.put(param.path("name").asText(), param.path("type").asText()));
        List<String> missing = declared.keySet().stream().filter(name -> !userParams.has(name)).toList();
        Map<String, String> invalid = new LinkedHashMap<>();
        userParams.fields().forEachRemaining(entry -> {
            String type = declared.get(entry.getKey());
            JsonNode value = entry.getValue();
            boolean valid = type != null && switch (type) {
                case "Bool" -> value.isBoolean();
                case "Int" -> value.isIntegralNumber() && value.canConvertToLong();
                case "Float" -> value.isNumber() && Double.isFinite(value.doubleValue());
                case "String" -> value.isTextual();
                default -> false;
            };
            if (!valid) invalid.put(entry.getKey(), type == null ? "Unknown parameter" : "Expected " + type);
        });
        if (!missing.isEmpty() || !invalid.isEmpty()) throw new ApiException(400, "INVALID_USER_PARAMS",
                "Provide the declared compilation parameters with their required types.", Map.of("missing", missing, "invalid", invalid));
        Map<String, Object> result = new LinkedHashMap<>();
        userParams.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            Object converted = switch (declared.get(entry.getKey())) {
                case "Int" -> value.longValue();
                case "Float" -> value.doubleValue();
                case "Bool" -> value.booleanValue();
                default -> value.textValue();
            };
            result.put(entry.getKey(), converted);
        });
        return result;
    }
}
