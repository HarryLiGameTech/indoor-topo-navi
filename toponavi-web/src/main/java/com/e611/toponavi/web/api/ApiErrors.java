package com.e611.toponavi.web.api;

import compiler.DiscoveryProjectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.e611.toponavi.web.controller")
public class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> apiError(ApiException error) {
        return ResponseEntity.status(error.status()).body(error.body());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> malformedBody() {
        return apiError(new ApiException(400, "INVALID_REQUEST", "Provide a valid JSON request body."));
    }

    @ExceptionHandler(DiscoveryProjectionException.class)
    public ResponseEntity<?> projectionError(DiscoveryProjectionException error) {
        String path = error.getFieldPath();
        return apiError(new ApiException(500, path.startsWith(".params")
                ? "UNSERIALIZABLE_TRANSPORT_PARAM" : "UNSERIALIZABLE_METADATA",
                "Compiled metadata cannot be represented as JSON.",
                Map.of("entityId", error.getOwner(), "path", path)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception error) {
        LOG.error("API request failed", error);
        return apiError(new ApiException(500, "INTERNAL_ERROR", "The request could not be completed."));
    }
}
