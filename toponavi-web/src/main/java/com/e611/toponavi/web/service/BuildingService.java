package com.e611.toponavi.web.service;

import api.AuthoringService;
import api.TopoNaviService;
import compiler.CompilationResult;
import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuildingService {
    private final Path examplesRoot;
    private final CompilationCacheService cache;
    private final ObjectMapper mapper;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BuildingService.class);

    public BuildingService(@Value("${platform.examples-path:examples}") String examplesRoot,
                           CompilationCacheService cache, ObjectMapper mapper) {
        this.examplesRoot = Path.of(examplesRoot);
        this.cache = cache;
        this.mapper = mapper;
    }

    public List<String> buildings() {
        try (var children = Files.list(examplesRoot)) {
            return children.filter(Files::isDirectory)
                    .filter(path -> java.util.stream.Stream.of("configuration", "configuration.tcfg")
                            .map(path::resolve).anyMatch(file -> Files.isRegularFile(file) && Files.isReadable(file)))
                    .map(path -> path.getFileName().toString()).sorted().toList();
        } catch (IOException error) {
            throw new ApiException(503, "BUILDING_CATALOG_UNAVAILABLE", "The configured map inventory could not be read.");
        }
    }

    public Map<String, String> files(String buildingId) {
        if (!buildings().contains(buildingId)) throw new ApiException(404, "BUILDING_NOT_FOUND", "Unknown building ID.");
        Path directory = examplesRoot.resolve(buildingId);
        try (var paths = Files.walk(directory)) {
            Map<String, String> result = new LinkedHashMap<>();
            for (Path file : paths.filter(Files::isRegularFile).sorted().toList()) {
                String name = directory.relativize(file).toString();
                if (!name.contains(".") || name.endsWith(".tmap") || name.endsWith(".ttr") || name.endsWith(".tcfg"))
                    result.put(name, Files.readString(file));
            }
            return result;
        } catch (IOException error) {
            throw new ApiException(503, "BUILDING_UNAVAILABLE", "The building source could not be read.");
        }
    }

    public CompilationResult compile(String buildingId, JsonNode userParams) {
        Map<String, String> files = files(buildingId);
        JsonNode inspection = mapper.valueToTree(AuthoringService.inspect(files));
        if (!inspection.path("diagnostics").isEmpty())
            throw new ApiException(500, "INVALID_BUILDING", "The installed building has invalid or missing source files.");
        Map<String, Object> params = parameters(inspection, userParams);
        return cache.load(buildingId, files, params).map(CompilationCacheService.CachedResult::getResult).orElseGet(() -> {
            CompilationResult result;
            try { result = TopoNaviService.compile(files, params); }
            catch (RuntimeException error) {
                LOG.error("Installed building compilation failed: {}", buildingId, error);
                throw new ApiException(500, "INVALID_BUILDING", "The installed building could not be compiled.");
            }
            cache.save(buildingId, files, params, result);
            return result;
        });
    }

    public Map<String, Object> parameters(JsonNode inspection, JsonNode userParams) {
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
