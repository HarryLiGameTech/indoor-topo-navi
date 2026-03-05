package com.e611.toponavi.web.controller;

import api.TopoNaviService; // The Scala Facade
import com.e611.toponavi.web.dto.NavigationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TopoController {

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, String> files) {
        try {
            String result = TopoNaviService.validateCode(files);
            return ResponseEntity.ok(Map.of("status", "success", "message", result));
        } catch (Exception e) {
            // Return 400 with the compiler error message
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