package com.e611.toponavi.web.controller;

import api.TopoNaviService; // The Scala Facade
import compiler.CompilationResult;
import data.NavigationGraph;
import data.NavigationOutputPath;
import com.e611.toponavi.web.dto.NavigationRequest;
import com.e611.toponavi.web.cache.CompilationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import scala.jdk.javaapi.CollectionConverters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class TopoController {

    @Autowired
    private CompilationCacheService cacheService;

    @Value("${platform.examples-path:examples}")
    private String examplesPathConfig;

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

    // Accept multipart/form-data — each form field is a filename, its value is file content
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
            // Stateless: We compile code sent in THIS request to find path
            String pathOutput = TopoNaviService.findPath(
                    request.files,
                    request.startNode,
                    request.endNode,
                    "MinimizeTime"
            );
            return ResponseEntity.ok(Map.of("path", pathOutput));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-building-query")
    public ResponseEntity<?> testBuildingQuery(@RequestParam(required = false) String name){
        try{
            return ResponseEntity.ok(Map.of(
                "name",        "Shanghai World Financial Center",
                "address",     "Lujiazui Shanghai",
                "height",      "492",
                "description", "A super-tall building recognized as a bottle-opener, built by Mori in 2008. It survived economic crisis in late 1990s, and eventually completed without big issues"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/quick-demo-navigation")
    public ResponseEntity<?> quickDemoNavigation(
            @RequestParam String startNode,
            @RequestParam String endNode,
            @RequestParam(required = false) String routePlanningPreference,
            @RequestParam(required = false) String forceRecompile) {
        try {
            // Load example files from examples directory
            Map<String, String> exampleFiles = loadExampleFiles();

            if (exampleFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No example files found in examples directory")
                );
            }

            // Check cache
            String cacheKey = cacheService.generateCacheHash(exampleFiles);
            CompilationCacheService.CachedResult cached = !"true".equals(forceRecompile) ?
                    cacheService.load(exampleFiles).orElse(null) : null;
            boolean fromCache = cached != null;

            // Compile (from cache or fresh)
            CompilationResult compilationResult;
            if (fromCache) {
                compilationResult = cached.getResult();
            } else {
                compilationResult = TopoNaviService.compile(exampleFiles);
                cacheService.save(exampleFiles, compilationResult);
            }

            NavigationOutputPath plan = TopoNaviService.findRoutePlan(compilationResult, startNode, endNode, routePlanningPreference);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", plan.prettyPrint(),
                "steps", plan.toStructuredSteps(),
                "filesLoaded", exampleFiles.size(),
                "cacheKey", cacheKey.substring(0, 8),
                "fromCache", fromCache
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                        Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/quick-demo-available-submaps")
    public ResponseEntity<?> getAvailableSubmaps() {
        try {
            Map<String, String> exampleFiles = loadExampleFiles();

            if (exampleFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No example files found")
                );
            }

            // Check cache; compile and save if miss
            CompilationCacheService.CachedResult cached =
                    cacheService.load(exampleFiles).orElse(null);
            String message;
            CompilationResult result;
            if (cached != null) {
                result = cached.getResult();
                message = "Compilation Successful (from cache)";
            } else {
                result = TopoNaviService.compile(exampleFiles);
                cacheService.save(exampleFiles, result);
                message = "Compilation Successful";
            }

            Set<String> submaps = CollectionConverters.asJava(result.graphs().keySet());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", message,
                "availableMaps", submaps
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                        Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/quick-demo-available-nodes")
    public ResponseEntity<?> getAvailableNodesFromMap(@RequestParam String mapName) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles();

            if (exampleFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No example files found")
                );
            }

            // Check cache; compile and save if miss
            CompilationCacheService.CachedResult cached =
                    cacheService.load(exampleFiles).orElse(null);
            String message;
            CompilationResult result;
            if (cached != null) {
                result = cached.getResult();
                message = "Compilation Successful (from cache)";
            } else {
                result = TopoNaviService.compile(exampleFiles);
                cacheService.save(exampleFiles, result);
                message = "Compilation Successful";
            }

            NavigationGraph graph = CollectionConverters.asJava(result.graphs()).get(mapName);
            if (graph == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Map not found: " + mapName));
            }
            Set<String> nodesInMap = CollectionConverters.asJava(graph.nodes())
                    .stream()
                    .map(node -> node.identifier())
                    .collect(java.util.stream.Collectors.toSet());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", message,
                    "availableFiles", nodesInMap
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/quick-demo-all-available-nodes")
    public ResponseEntity<?> getAllAvailableNodes(@RequestParam(required = false) String withNodesAttributes) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles();

            if (exampleFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No example files found")
                );
            }

            // Check cache; compile and save if miss
            CompilationCacheService.CachedResult cached =
                    cacheService.load(exampleFiles).orElse(null);
            String message;
            CompilationResult result;
            if (cached != null) {
                result = cached.getResult();
                message = "Compilation Successful (from cache)";
            } else {
                result = TopoNaviService.compile(exampleFiles);
                cacheService.save(exampleFiles, result);
                message = "Compilation Successful";
            }

            // Get all the nodes inside the result, formatted as "{graph_identifier}::{node_identifier}"
            Map<String, NavigationGraph> graphs = CollectionConverters.asJava(result.graphs());
            List<String> allNodes = graphs.entrySet().stream()
                    .flatMap(entry -> CollectionConverters.asJava(entry.getValue().nodes()).stream()
                            .map(node -> entry.getKey() + "::" + node.identifier()))
                    .toList();

            boolean includeAttributes = "true".equalsIgnoreCase(withNodesAttributes);
            Object nodesData;
            if (includeAttributes) {
                Map<String, Map<String, Object>> nodesWithAttrs = new LinkedHashMap<>();
                graphs.forEach((graphId, graph) ->
                    CollectionConverters.asJava(graph.nodes()).forEach(node -> {
                        Map<String, Object> attrs = new HashMap<>();
                        CollectionConverters.asJava(node.attributes()).forEach((attrKey, attrVal) ->
                            attrs.put(attrKey, toJavaValue(attrVal))
                        );
                        nodesWithAttrs.put(graphId + "::" + node.identifier(), attrs);
                    })
                );
                nodesData = nodesWithAttrs;
            } else {
                nodesData = allNodes;
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", message,
                    "allNodes", nodesData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/cache/invalidate")
    public ResponseEntity<?> invalidateCache(
            @RequestParam(required = false) String projectIdentifier) {
        try {
            String cacheKey = projectIdentifier != null ? projectIdentifier : "all";

            if ("all".equals(projectIdentifier)) {
                cacheService.clearAll();
                return ResponseEntity.ok(Map.of("status", "success", "cacheKey", "all"));
            } else {
                // Load files to generate proper key
                Map<String, String> exampleFiles = loadExampleFiles();
                String key = cacheService.generateCacheHash(exampleFiles);
                boolean invalidated = cacheService.invalidate(key);

                return ResponseEntity.ok(Map.of(
                    "status", invalidated ? "success" : "not_found",
                    "cacheKey", key != null ? key.substring(0, Math.min(8, key.length())) : "all"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                        Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/cache/status")
    public ResponseEntity<?> getCacheStatus() {
        try {
            Map<String, String> exampleFiles = loadExampleFiles();
            String cacheKey = cacheService.generateCacheHash(exampleFiles);
            boolean cacheExists = cacheService.load(exampleFiles).isPresent();

            return ResponseEntity.ok(Map.of(
                "cacheKey", cacheKey.substring(0, 8),
                "cacheHit", cacheExists,
                "filesLoaded", exampleFiles.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    private Object toJavaValue(enums.AttributeValue av) {
        if (av instanceof enums.AttributeValue.IntValue v) return v.value();
        if (av instanceof enums.AttributeValue.StringValue v) return v.value();
        if (av instanceof enums.AttributeValue.BoolValue v) return v.value();
        if (av instanceof enums.AttributeValue.DoubleValue v) return v.value();
        return av.toString();
    }

    private Map<String, String> loadExampleFiles() {
        Map<String, String> files = new HashMap<>();
        java.nio.file.Path examplesPath = java.nio.file.Paths.get(examplesPathConfig);

        if (!java.nio.file.Files.exists(examplesPath)) {
            System.err.println("Examples directory not found: " + examplesPath.toAbsolutePath());
            return files;
        }

        try {
            // Walk through examples directory and load all files
            java.nio.file.Files.walk(examplesPath)
                .filter(java.nio.file.Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // Create relative path as filename (e.g., "swfc/Floor3")
                        String relativePath = examplesPath.relativize(file).toString();
                        String content = java.nio.file.Files.readString(file);
                        files.put(relativePath, content);
                    } catch (IOException e) {
                        // Skip files that cannot be read
                        System.err.println("Failed to read file: " + file + " - " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("Failed to load example files: " + e.getMessage());
        }

        return files;
    }
}
