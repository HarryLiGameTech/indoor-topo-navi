package com.e611.toponavi.web.controller;

import api.TopoNaviService; // The Scala Facade
import com.e611.toponavi.web.dto.NavigationRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TopoController {

    @GetMapping(value = "test-constraints")
    public ResponseEntity<?> testConstraints() {
        try {
            return ResponseEntity.ok(Map.of("constraint_names", List.of("haveCard", "inEmergency", "nowIsWeekday", "nowIsInOfficeHours")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // Accept JSON body
    @PostMapping(value = "/validate", consumes = {"application/json", "text/plain", "*/*"})
    public ResponseEntity<?> validate(@RequestBody Map<String, String> files) {
        try {
            String result = TopoNaviService.validateCode(files);
            return ResponseEntity.ok(Map.of("status", "success", "message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // Accept multipart/form-data — each form field is a filename, its value is the file content
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateMultipart(@RequestParam Map<String, MultipartFile> fileUploads,
                                               @RequestParam Map<String, String> textFields) throws IOException {
        Map<String, String> files = new HashMap<>(textFields);
        for (Map.Entry<String, MultipartFile> entry : fileUploads.entrySet()) {
            files.put(entry.getKey(), new String(entry.getValue().getBytes(), StandardCharsets.UTF_8));
        }
        try {
            String result = TopoNaviService.validateCode(files);
            return ResponseEntity.ok(Map.of("status", "success", "message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/testNavigate")
    public ResponseEntity<?> testNavigate(@RequestBody NavigationRequest request) {
        try {
            // Stateless: We compile the code sent in THIS request to find the path
            String pathOutput = TopoNaviService.findPath(
                    request.files,
                    request.startNode,
                    request.endNode
            );
            return ResponseEntity.ok(Map.of("path", pathOutput));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}