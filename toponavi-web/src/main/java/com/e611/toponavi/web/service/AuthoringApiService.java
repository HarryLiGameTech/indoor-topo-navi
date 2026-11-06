package com.e611.toponavi.web.service;

import api.AuthoringService;
import api.TopoNaviService;
import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.api.RequestFields;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

@Service
public class AuthoringApiService {
    private final CompilationParameterResolver parameters;
    private final ObjectMapper mapper;
    private final int maxFiles, maxSourceBytes;
    private final Semaphore compilations;

    public AuthoringApiService(CompilationParameterResolver parameters, ObjectMapper mapper,
                              @Value("${platform.api.authoring.max-files:512}") int maxFiles,
                              @Value("${platform.api.authoring.max-source-bytes:4194304}") int maxSourceBytes,
                              @Value("${platform.api.authoring.max-concurrent-compilations:2}") int concurrency) {
        if (maxFiles < 1 || maxSourceBytes < 1 || concurrency < 1) throw new IllegalArgumentException("Authoring limits must be positive");
        this.parameters = parameters;
        this.mapper = mapper;
        this.maxFiles = maxFiles;
        this.maxSourceBytes = maxSourceBytes;
        this.compilations = new Semaphore(concurrency);
    }

    public ResponseEntity<?> validate(JsonNode body) {
        RequestFields.object(body, Set.of("files", "userParams"), "");
        JsonNode source = body.path("files");
        if (!source.isObject() || source.isEmpty()) throw RequestFields.invalid("/files", "Provide source files keyed by filename.");
        if (source.size() > maxFiles) throw RequestFields.invalid("/files", "Too many source files.");
        JsonNode userParams = RequestFields.optionalObject(body, "userParams");
        Map<String, String> files = new LinkedHashMap<>();
        long bytes = 0;
        var fields = source.fields();
        while (fields.hasNext()) {
            var file = fields.next();
            if (file.getKey().isBlank() || !file.getValue().isTextual()) throw RequestFields.invalid("/files", "Source contents must be strings.");
            bytes += file.getValue().textValue().getBytes(StandardCharsets.UTF_8).length;
            if (bytes > maxSourceBytes) throw RequestFields.invalid("/files", "Source exceeds the configured size limit.");
            files.put(file.getKey(), file.getValue().textValue());
        }
        if (!compilations.tryAcquire()) throw new ApiException(503, "AUTHORING_BUSY", "Authoring capacity is currently in use.");
        try {
            ObjectNode inspection = mapper.valueToTree(AuthoringService.inspect(files));
            if (!inspection.path("diagnostics").isEmpty()) return failure(inspection,
                    new ApiException(422, "INVALID_TOPOSCRIPT", "Source parsing failed."));
            Map<String, Object> params;
            try { params = parameters.resolve(inspection, userParams); }
            catch (ApiException error) { return failure(inspection, error); }
            try { TopoNaviService.compile(files, params); }
            catch (RuntimeException error) {
                String message = error.getMessage() == null ? "Source compilation failed." : error.getMessage();
                inspection.withArray("diagnostics").addObject().put("stage", "compile")
                        .put("message", message.substring(0, Math.min(message.length(), 2000)));
                return failure(inspection, new ApiException(422, "INVALID_TOPOSCRIPT", "Source compilation failed."));
            }
            return ResponseEntity.ok(inspection.put("valid", true).put("status", "success"));
        } finally {
            compilations.release();
        }
    }

    private ResponseEntity<?> failure(ObjectNode inspection, ApiException error) {
        inspection.put("valid", false);
        inspection.set("error", mapper.<JsonNode>valueToTree(error.body()).get("error"));
        return ResponseEntity.status(error.status()).body(inspection);
    }
}
