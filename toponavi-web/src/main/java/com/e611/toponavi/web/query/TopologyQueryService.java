package com.e611.toponavi.web.query;

import api.TopologyData;
import compiler.CompilationResult;
import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.api.RequestFields;
import com.e611.toponavi.web.service.BuildingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TopologyQueryService {
    private static final Set<String> NODE_FIELDS = Set.of("nodeId", "mapId", "localNodeId", "displayName", "attributes");
    private static final Set<String> EDGE_FIELDS = Set.of("mapId", "fromNodeId", "toNodeId", "cost", "tags", "requiredActions", "attributes");
    private final BuildingService buildings;
    private final FilterParser filters;
    private final ObjectMapper mapper;

    public TopologyQueryService(BuildingService buildings, FilterParser filters, ObjectMapper mapper) {
        this.buildings = buildings;
        this.filters = filters;
        this.mapper = mapper;
    }

    public ObjectNode query(String buildingId, String kind, JsonNode body) {
        RequestFields.object(body, kind.equals("neighbors")
                ? Set.of("userParams", "selection", "filter", "inDetail", "limit")
                : Set.of("userParams", "selection", "filter", "inDetail"), "");
        boolean inDetail = RequestFields.bool(body, "inDetail", false);
        JsonNode selection = RequestFields.optionalObject(body, "selection");
        Set<String> selectionKeys = switch (kind) {
            case "maps" -> Set.of("mapIds");
            case "nodes" -> Set.of("mapId", "nodeIds");
            case "edges" -> Set.of("mapId", "fromNodeId", "toNodeId");
            case "neighbors" -> Set.of("mapId", "nodeId");
            case "transports" -> Set.of("types", "transportIds");
            default -> throw new IllegalArgumentException("Unknown topology query");
        };
        RequestFields.object(selection, selectionKeys, "/selection");
        Set<String> fields = switch (kind) {
            case "maps" -> Set.of("mapId", "displayName", "attributes");
            case "nodes" -> NODE_FIELDS;
            case "edges" -> EDGE_FIELDS;
            case "neighbors" -> Set.of("nodeId", "mapId", "localNodeId", "displayName", "cost", "tags", "requiredActions", "nodeAttributes", "edgeAttributes");
            default -> Set.of("transportId", "type", "displayName", "params", "stations", "attributes", "direction", "ridePermissions");
        };
        DataPredicate predicate = filters.parse(body.get("filter"), fields);
        String mapId = RequestFields.text(selection, "mapId", null);
        List<String> ids = RequestFields.strings(selection, kind.equals("maps") ? "mapIds" : kind.equals("transports") ? "transportIds" : "nodeIds");
        List<String> types = RequestFields.strings(selection, "types");
        if (types != null && !Set.of("Elevator", "Escalator", "Stairs").containsAll(types))
            throw RequestFields.invalid("/selection/types", "Unknown transport type.");
        if (ids != null && kind.equals("nodes")) ids.forEach(id -> checkNodeSelection(id, mapId));
        String from = RequestFields.text(selection, "fromNodeId", null);
        String to = RequestFields.text(selection, "toNodeId", null);
        if (from != null) { RequestFields.qualified(from, "/selection/fromNodeId"); checkNodeSelection(from, mapId); }
        if (to != null) { RequestFields.qualified(to, "/selection/toNodeId"); checkNodeSelection(to, mapId); }
        String source = RequestFields.text(selection, "nodeId", null);
        int limit = 5;
        if (kind.equals("neighbors")) {
            if (source == null) throw RequestFields.invalid("/selection/nodeId", "A source node is required.");
            checkNodeSelection(source, mapId);
            if (body.has("limit")) {
                if (!body.get("limit").isIntegralNumber() || !body.get("limit").canConvertToInt() || body.get("limit").intValue() < 1)
                    throw RequestFields.invalid("/limit", "Expected a positive integer.");
                limit = body.get("limit").intValue();
            }
        }
        CompilationResult result = buildings.compile(buildingId, RequestFields.optionalObject(body, "userParams"));
        if (mapId != null && !result.graphs().contains(mapId)) throw new ApiException(404, "MAP_NOT_FOUND", "Unknown map ID.");
        List<JsonNode> candidates = switch (kind) {
            case "maps" -> records(TopologyData.maps(result));
            case "nodes", "neighbors" -> records(TopologyData.nodes(result));
            case "edges" -> records(TopologyData.edges(result));
            default -> records(TopologyData.transports(result));
        };
        ObjectNode response = mapper.createObjectNode().put("status", "success").put("inDetail", inDetail);
        if (kind.equals("neighbors")) {
            String resolved = resolveNode(candidates, source, mapId);
            candidates = neighbors(candidates, records(TopologyData.edges(result)), resolved);
            response.put("resolvedNodeId", resolved);
        } else {
            candidates = candidates.stream().filter(record -> mapId == null || record.path("mapId").asText().equals(mapId))
                    .filter(record -> ids == null || ids.stream().anyMatch(id -> switch (kind) {
                        case "maps" -> record.path("mapId").asText().equals(id);
                        case "transports" -> record.path("transportId").asText().equals(id);
                        default -> record.path("nodeId").asText().equals(id) || record.path("localNodeId").asText().equals(id);
                    }))
                    .filter(record -> types == null || types.contains(record.path("type").asText()))
                    .filter(record -> from == null || record.path("fromNodeId").asText().equals(from))
                    .filter(record -> to == null || record.path("toNodeId").asText().equals(to)).toList();
        }
        List<JsonNode> matches = candidates.stream().filter(predicate).sorted(order(kind)).toList();
        if (kind.equals("neighbors")) {
            response.put("total", matches.size()).put("limit", limit).put("truncated", matches.size() > limit);
            matches = matches.stream().limit(limit).toList();
        }
        response.set(kind, mapper.valueToTree(matches.stream().map(record -> project(record, kind, inDetail)).toList()));
        return response;
    }

    private void checkNodeSelection(String id, String mapId) {
        if (id.contains(":")) {
            RequestFields.qualified(id, "/selection");
            if (mapId != null && !id.startsWith(mapId + "::"))
                throw new ApiException(400, "CONFLICTING_NODE_SELECTION", "Qualified node ID contradicts mapId.");
        } else if (id.isBlank() || id.chars().anyMatch(Character::isWhitespace))
            throw RequestFields.invalid("/selection", "Malformed node ID.");
    }

    public static String resolveNode(List<JsonNode> nodes, String id, String mapId) {
        String qualified = id.contains("::") ? id : mapId == null ? null : mapId + "::" + id;
        List<String> matches = nodes.stream().filter(node -> qualified == null
                ? node.path("localNodeId").asText().equals(id) : node.path("nodeId").asText().equals(qualified))
                .map(node -> node.path("nodeId").asText()).sorted().toList();
        if (matches.isEmpty()) throw new ApiException(404, "NODE_NOT_FOUND", "Unknown node ID.");
        if (matches.size() > 1) throw new ApiException(422, "AMBIGUOUS_NODE_ID", "Provide a mapId or a qualified nodeId.",
                Map.of("nodeId", id, "candidates", matches));
        return matches.get(0);
    }

    private List<JsonNode> neighbors(List<JsonNode> nodes, List<JsonNode> edges, String source) {
        Map<String, JsonNode> byId = new LinkedHashMap<>();
        nodes.forEach(node -> byId.put(node.path("nodeId").asText(), node));
        Map<String, JsonNode> cheapest = new LinkedHashMap<>();
        edges.stream().filter(edge -> edge.path("fromNodeId").asText().equals(source))
                .sorted(Comparator.comparingDouble((JsonNode edge) -> edge.path("cost").asDouble()).thenComparing(JsonNode::toString))
                .forEach(edge -> cheapest.putIfAbsent(edge.path("toNodeId").asText(), edge));
        return cheapest.entrySet().stream().map(entry -> {
            ObjectNode node = byId.get(entry.getKey()).deepCopy();
            node.set("nodeAttributes", node.remove("attributes"));
            node.set("edgeAttributes", entry.getValue().get("attributes"));
            for (String field : List.of("cost", "tags", "requiredActions")) node.set(field, entry.getValue().get(field));
            return (JsonNode) node;
        }).toList();
    }

    private List<JsonNode> records(Object values) {
        JsonNode array = mapper.valueToTree(values);
        List<JsonNode> result = new ArrayList<>();
        array.forEach(value -> result.add(canonical(value)));
        return result;
    }

    private JsonNode canonical(JsonNode value) {
        if (value.isObject()) {
            ObjectNode sorted = mapper.createObjectNode();
            List<String> keys = new ArrayList<>();
            value.fieldNames().forEachRemaining(keys::add);
            keys.stream().sorted().forEach(key -> sorted.set(key, canonical(value.get(key))));
            return sorted;
        }
        if (value.isArray()) {
            var array = mapper.createArrayNode();
            value.forEach(child -> array.add(canonical(child)));
            return array;
        }
        return value;
    }

    private Comparator<JsonNode> order(String kind) {
        return switch (kind) {
            case "neighbors" -> Comparator.comparingDouble((JsonNode node) -> node.path("cost").asDouble()).thenComparing(node -> node.path("nodeId").asText());
            case "edges" -> Comparator.comparing((JsonNode node) -> node.path("mapId").asText())
                    .thenComparing(node -> node.path("fromNodeId").asText()).thenComparing(node -> node.path("toNodeId").asText())
                    .thenComparingDouble(node -> node.path("cost").asDouble()).thenComparing(JsonNode::toString);
            default -> Comparator.comparing(node -> node.path(kind.equals("maps") ? "mapId" : kind.equals("transports") ? "transportId" : "nodeId").asText());
        };
    }

    private JsonNode project(JsonNode record, String kind, boolean detail) {
        if (detail) return record;
        ObjectNode summary = record.deepCopy();
        summary.remove(List.of("attributes", "nodeAttributes", "edgeAttributes", "direction", "ridePermissions"));
        if (kind.equals("transports")) {
            summary.path("stations").forEach(station -> ((ObjectNode) station).retain("label", "nodeId"));
        }
        return summary;
    }
}
