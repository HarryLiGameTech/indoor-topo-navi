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
import java.util.Collections;
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
    public ResponseEntity<?> quickDemoNavigationGet(
            @RequestParam String buildingName,
            @RequestParam String startNode,
            @RequestParam String endNode,
            @RequestParam(required = false) String routePlanningPreference,
            @RequestParam(required = false) String forceRecompile) {
        return quickDemoNavigation(buildingName, startNode, endNode, routePlanningPreference, forceRecompile, Collections.emptyMap());
    }

    @PostMapping("/quick-demo-navigation")
    public ResponseEntity<?> quickDemoNavigationPost(
            @RequestParam String buildingName,
            @RequestParam String startNode,
            @RequestParam String endNode,
            @RequestParam(required = false) String routePlanningPreference,
            @RequestParam(required = false) String forceRecompile,
            @RequestBody(required = false) Map<String, Object> body) {
        return quickDemoNavigation(buildingName, startNode, endNode, routePlanningPreference, forceRecompile,
                extractUserParams(body));
    }

    private ResponseEntity<?> quickDemoNavigation(
            String buildingName, String startNode, String endNode,
            String routePlanningPreference, String forceRecompile,
            Map<String, Object> userParams) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            if (exampleFiles.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No example files found in examples directory"));

            String cacheKey = cacheService.generateCacheHash(buildingName, exampleFiles, userParams);
            if ("true".equals(forceRecompile)) cacheService.invalidate(cacheKey);

            CompileOutcome outcome = compileWithCache(buildingName, exampleFiles, userParams);
            NavigationOutputPath plan = TopoNaviService.findRoutePlan(outcome.result(), startNode, endNode, routePlanningPreference);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", plan.prettyPrint(),
                "steps", plan.toStructuredSteps(),
                "filesLoaded", exampleFiles.size(),
                "cacheKey", cacheKey.substring(0, 8),
                "fromCache", outcome.fromCache()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(formatError(e));
        }
    }

    @GetMapping("/quick-demo-available-submaps")
    public ResponseEntity<?> getAvailableSubmapsGet(@RequestParam String buildingName) {
        return getAvailableSubmaps(buildingName, Collections.emptyMap());
    }

    @PostMapping("/quick-demo-available-submaps")
    public ResponseEntity<?> getAvailableSubmapsPost(
            @RequestParam String buildingName,
            @RequestBody(required = false) Map<String, Object> body) {
        return getAvailableSubmaps(buildingName, extractUserParams(body));
    }

    private ResponseEntity<?> getAvailableSubmaps(String buildingName, Map<String, Object> userParams) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            if (exampleFiles.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No example files found"));

            CompileOutcome outcome = compileWithCache(buildingName, exampleFiles, userParams);
            Set<String> submaps = CollectionConverters.asJava(outcome.result().graphs().keySet());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", outcome.message(),
                "availableMaps", submaps
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(formatError(e));
        }
    }

    @GetMapping("/quick-demo-available-nodes-in-map")
    public ResponseEntity<?> getAvailableNodesFromMapGet(
            @RequestParam String buildingName,
            @RequestParam String mapName) {
        return getAvailableNodesFromMap(buildingName, mapName, Collections.emptyMap());
    }

    @PostMapping("/quick-demo-available-nodes-in-map")
    public ResponseEntity<?> getAvailableNodesFromMapPost(
            @RequestParam String buildingName,
            @RequestParam String mapName,
            @RequestBody(required = false) Map<String, Object> body) {
        return getAvailableNodesFromMap(buildingName, mapName, extractUserParams(body));
    }

    private ResponseEntity<?> getAvailableNodesFromMap(String buildingName, String mapName, Map<String, Object> userParams) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            if (exampleFiles.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No example files found"));

            CompileOutcome outcome = compileWithCache(buildingName, exampleFiles, userParams);
            NavigationGraph graph = CollectionConverters.asJava(outcome.result().graphs()).get(mapName);
            if (graph == null) return ResponseEntity.badRequest().body(Map.of("error", "Map not found: " + mapName));

            Set<String> nodesInMap = CollectionConverters.asJava(graph.nodes()).stream()
                    .map(node -> node.identifier())
                    .collect(java.util.stream.Collectors.toSet());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", outcome.message(),
                    "availableFiles", nodesInMap
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(formatError(e));
        }
    }

    @GetMapping("/quick-demo-all-available-nodes")
    public ResponseEntity<?> getAllAvailableNodesGet(
            @RequestParam String buildingName,
            @RequestParam(required = false) String withNodesAttributes) {
        return getAllAvailableNodes(buildingName, withNodesAttributes, Collections.emptyMap());
    }

    @PostMapping("/quick-demo-all-available-nodes")
    public ResponseEntity<?> getAllAvailableNodesPost(
            @RequestParam String buildingName,
            @RequestParam(required = false) String withNodesAttributes,
            @RequestBody(required = false) Map<String, Object> body) {
        return getAllAvailableNodes(buildingName, withNodesAttributes, extractUserParams(body));
    }

    private ResponseEntity<?> getAllAvailableNodes(String buildingName, String withNodesAttributes, Map<String, Object> userParams) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            if (exampleFiles.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No example files found"));

            CompileOutcome outcome = compileWithCache(buildingName, exampleFiles, userParams);
            Map<String, NavigationGraph> graphs = CollectionConverters.asJava(outcome.result().graphs());

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
                    "message", outcome.message(),
                    "allNodes", nodesData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(formatError(e));
        }
    }

    @GetMapping("/quick-demo-node-info")
    public ResponseEntity<?> getNodeInfoGet(
            @RequestParam String buildingName,
            @RequestParam String nodeIdentifier) {
        return getNodeInfo(buildingName, nodeIdentifier, Collections.emptyMap());
    }

    @PostMapping("/quick-demo-node-info")
    public ResponseEntity<?> getNodeInfoPost(
            @RequestParam String buildingName,
            @RequestParam String nodeIdentifier,
            @RequestBody(required = false) Map<String, Object> body) {
        return getNodeInfo(buildingName, nodeIdentifier, extractUserParams(body));
    }

    private ResponseEntity<?> getNodeInfo(String buildingName, String nodeIdentifier, Map<String, Object> userParams) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            if (exampleFiles.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No example files found"));

            CompileOutcome outcome = compileWithCache(buildingName, exampleFiles, userParams);
            CompilationResult result = outcome.result();

            String[] parts = nodeIdentifier.split("::");
            if (parts.length != 2) return ResponseEntity.badRequest().body(Map.of("error", "Invalid node identifier format. Expected '{graph_identifier}::{node_identifier}'"));

            String graphId = parts[0];
            String nodeId = parts[1];

            NavigationGraph graph = CollectionConverters.asJava(result.graphs()).get(graphId);
            if (graph == null) return ResponseEntity.badRequest().body(Map.of("error", "Graph not found: " + graphId));

            data.TopoNode node = CollectionConverters.asJava(graph.nodes()).stream()
                    .filter(n -> n.identifier().equals(nodeId))
                    .findFirst()
                    .orElse(null);
            if (node == null) return ResponseEntity.badRequest().body(Map.of("error", "Node not found: " + nodeId));

            Map<String, Object> attrs = new HashMap<>();
            CollectionConverters.asJava(node.attributes()).forEach((attrKey, attrVal) ->
                    attrs.put(attrKey, toJavaValue(attrVal))
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", outcome.message(),
                    "nodeIdentifier", nodeIdentifier,
                    "attributes", attrs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(formatError(e));
        }
    }

    @PostMapping("/cache/invalidate")
    public ResponseEntity<?> invalidateCache(
            @RequestParam(required = false) String projectIdentifier,
            @RequestParam(required = false, defaultValue = "") String buildingName) {
        try {
            String cacheKey = projectIdentifier != null ? projectIdentifier : "all";

            if ("all".equals(projectIdentifier)) {
                cacheService.clearAll();
                return ResponseEntity.ok(Map.of("status", "success", "cacheKey", "all"));
            } else {
                Map<String, String> exampleFiles = loadExampleFiles(buildingName);
                String key = cacheService.generateCacheHash(buildingName, exampleFiles);
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
    public ResponseEntity<?> getCacheStatus(@RequestParam(required = false, defaultValue = "") String buildingName) {
        try {
            Map<String, String> exampleFiles = loadExampleFiles(buildingName);
            String cacheKey = cacheService.generateCacheHash(buildingName, exampleFiles);
            boolean cacheExists = cacheService.load(buildingName, exampleFiles).isPresent();

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

    private record CompileOutcome(CompilationResult result, String message, boolean fromCache) {}

    private CompileOutcome compileWithCache(String buildingName, Map<String, String> exampleFiles, Map<String, Object> userParams) {
        CompilationCacheService.CachedResult cached = cacheService.load(buildingName, exampleFiles, userParams).orElse(null);
        if (cached != null) {
            return new CompileOutcome(cached.getResult(), "Compilation Successful (from cache)", true);
        }
        CompilationResult result = TopoNaviService.compile(exampleFiles, userParams);
        cacheService.save(buildingName, exampleFiles, userParams, result);
        return new CompileOutcome(result, "Compilation Successful", false);
    }

    /** Extracts the "userParams" key from a raw JSON body map, or returns empty map. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractUserParams(Map<String, Object> body) {
        if (body == null) return Collections.emptyMap();
        Object up = body.get("userParams");
        if (up instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return Collections.emptyMap();
    }

    /** Formats an exception into a diagnostic map with type, message, and cause chain. */
    private Map<String, Object> formatError(Exception e) {
        List<String> causeChain = new java.util.ArrayList<>();
        Throwable t = e;
        while (t != null) {
            causeChain.add(t.getClass().getName() + ": " + t.getMessage());
            t = t.getCause();
        }
        return Map.of(
            "error", e.getMessage() != null ? e.getMessage() : "(null message)",
            "exceptionType", e.getClass().getName(),
            "causeChain", causeChain
        );
    }

    @PostMapping("/quick-demo-compile-check")
    public ResponseEntity<?> quickDemoCompileCheck(
            @RequestParam String buildingName,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> userParams = extractUserParams(body);
        Map<String, String> exampleFiles;
        try {
            exampleFiles = loadExampleFiles(buildingName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("stage", "loadFiles", "receivedUserParams", userParams, "error", formatError(e)));
        }
        if (exampleFiles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("stage", "loadFiles", "error", "No example files found for: " + buildingName));
        }
        try {
            CompilationResult result = TopoNaviService.compile(exampleFiles, userParams);
            Map<String, NavigationGraph> graphs = CollectionConverters.asJava(result.graphs());
            List<String> graphNames = new java.util.ArrayList<>(graphs.keySet());
            long totalNodes = graphs.values().stream()
                    .mapToLong(g -> CollectionConverters.asJava(g.nodes()).size())
                    .sum();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "buildingName", buildingName,
                "receivedUserParams", userParams,
                "filesLoaded", exampleFiles.keySet(),
                "graphsCompiled", graphNames,
                "totalNodeCount", totalNodes
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "stage", "compilation",
                "buildingName", buildingName,
                "receivedUserParams", userParams,
                "filesLoaded", exampleFiles.keySet(),
                "error", formatError(e)
            ));
        }
    }


    private Object toJavaValue(enums.AttributeValue av) {
        if (av instanceof enums.AttributeValue.IntValue v) return v.value();
        if (av instanceof enums.AttributeValue.StringValue v) return v.value();
        if (av instanceof enums.AttributeValue.BoolValue v) return v.value();
        if (av instanceof enums.AttributeValue.DoubleValue v) return v.value();
        return av.toString();
    }

    private Map<String, String> loadExampleFiles(String buildingName) {
        Map<String, String> files = new HashMap<>();
        java.nio.file.Path examplesPath = java.nio.file.Paths.get(examplesPathConfig, buildingName.toLowerCase());

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
