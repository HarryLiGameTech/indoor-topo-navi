package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.query.FilterParser;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.AuthoringApiService;
import com.e611.toponavi.web.service.BuildingService;
import com.e611.toponavi.web.service.NavigationApiService;
import compiler.DiscoveryProjectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class TopologyContractTest {
    @TempDir Path root;
    private final ObjectMapper mapper = new ObjectMapper();
    private BuildingService buildings;
    private TopologyQueryService query;

    @BeforeEach
    void setup() throws Exception {
        Path project = Files.createDirectory(root.resolve("fixture"));
        Files.writeString(project.resolve("configuration"), "building-includes {\n submap Lower\n submap Upper\n vehicle Moving\n}");
        Files.writeString(project.resolve("root"), "root Building(allowed: Bool) {\n constraint Access { require allowed }\n}");
        Files.writeString(project.resolve("Lower"), """
                topo-map Lower() {
                  let params = {permResident = 1, description = "Lower floor"}
                  topo-node landing {tags = ["indoor"]}
                  topo-node destination {description = "Destination"}
                  atomic-path [landing -> destination] {cost = 2, tags = ["outdoor"], effort = 1}
                  atomic-path [landing -> destination] {cost = 5, tags = ["indoor"], effort = 0.5}
                  constraint Local { require false }
                }
                """);
        Files.writeString(project.resolve("Upper"), "topo-map Upper() {\n let params = {area = 1}\n topo-node landing\n}\n");
        Files.writeString(project.resolve("Moving"), """
                transport Moving is Escalator {
                  let params = {time = 18, distance = 8.5, inspection = {team = "Facilities", intervalDays = 30}, large = 9007199254740993}
                  station Bottom at Lower::landing {} requires Access
                  station Top at Upper::landing {}
                  direction Bottom -> Top
                  ride-from Top to Bottom requires Access
                }
                """);
        buildings = new BuildingService(root.toString(), new CompilationCacheService(root.resolve("cache")), mapper);
        query = new TopologyQueryService(buildings, new FilterParser(16, 256, 1024, 16384, 256), mapper);
    }

    private ObjectNode request(boolean allowed) {
        ObjectNode body = mapper.createObjectNode();
        body.putObject("userParams").put("allowed", allowed);
        return body;
    }

    @Test
    void exposesDeclaredGraphSequenceWithoutTemplatesAcrossFilteringAndCacheLoads() throws Exception {
        Path project = root.resolve("fixture");
        Files.writeString(project.resolve("configuration"), """
                building-includes {
                  submap Upper
                  submap Middle using Base
                  submap Lower
                  vehicle Moving
                }
                """);
        Files.writeString(project.resolve("Base"), "topo-map Base() {\n let params = {area = 1}\n topo-node landing\n}\n");
        var expected = mapper.readTree("[\"Upper\",\"Middle\",\"Lower\"]");
        for (boolean allowed : new boolean[]{true, false}) {
            var full = query.query("fixture", "nodes", request(allowed));
            assertEquals(expected, full.path("graphSequence"));
            assertTrue(full.path("nodes").toString().contains("Base::landing"));

            ObjectNode filtered = request(allowed).put("inDetail", true);
            filtered.putObject("selection").put("mapId", "Lower");
            filtered.set("filter", mapper.readTree("{\"field\":\"localNodeId\",\"op\":\"eq\",\"value\":\"absent\"}"));
            // This second request loads the same compiled result from disk cache.
            var empty = mapper.readTree(mapper.writeValueAsString(query.query("fixture", "nodes", filtered)));
            assertTrue(empty.path("nodes").isEmpty());
            assertEquals(expected, empty.path("graphSequence"));
        }
    }

    @Test
    void exposesNativeCustomObjectivesAndTheirScoresInBothRoutingModes() throws Exception {
        var navigation = new NavigationApiService(buildings, mapper);
        for (String mode : java.util.List.of("fullyInformed", "partiallyInformed")) {
            ObjectNode body = request(true).put("startNode", "Lower::landing").put("endNode", "Lower::destination")
                    .put("routingMode", mode);
            ObjectNode preference = body.putObject("traversalPreference").put("routePlanningPreference", "MinimizeCustomData");
            preference.putObject("customObjective").put("kind", "tagExposure").put("tag", "outdoor");
            var tagged = navigation.findRoute("fixture", body);
            assertEquals(5, tagged.path("totalCostSeconds").asDouble());
            assertEquals(0, tagged.path("customCost").asDouble());
            assertEquals(0, tagged.path("transferCount").asInt());
            assertEquals(5, tagged.path("physicalDemand").asDouble());
            assertEquals("MinimizeTransfers", tagged.path("appliedTraversalPreference").path("customObjective")
                    .path("tieBreakers").get(0).asText());
            preference.putObject("customObjective").put("kind", "edgeAttribute").put("attribute", "effort")
                    .putArray("tieBreakers").add("MinimizeTime").add("MinimizeTransfers");
            var numeric = navigation.findRoute("fixture", body);
            assertEquals(5, numeric.path("totalCostSeconds").asDouble());
            assertEquals(0.5, numeric.path("customCost").asDouble());
            assertEquals(0, numeric.path("appliedTraversalPreference").path("customObjective").path("missingValue").asDouble());
            preference.putObject("customObjective").put("kind", "edgeAttribute").put("attribute", "absent").put("missingValue", 2);
            var missing = navigation.findRoute("fixture", body);
            assertEquals(2, missing.path("totalCostSeconds").asDouble());
            assertEquals(2, missing.path("customCost").asDouble());
        }
    }

    @Test
    void returnsStructuredErrorsForInvalidCustomMapData() throws Exception {
        Path file = root.resolve("fixture/Lower");
        Files.writeString(file, Files.readString(file).replace("effort = 1", "effort = -1"));
        ObjectNode body = request(true).put("startNode", "Lower::landing").put("endNode", "Lower::destination");
        body.putObject("traversalPreference").put("routePlanningPreference", "MinimizeCustomData")
                .putObject("customObjective").put("kind", "edgeAttribute").put("attribute", "effort");
        var error = assertThrows(ApiException.class, () -> new NavigationApiService(buildings, mapper).findRoute("fixture", body));
        assertEquals(422, error.status());
        assertTrue(error.body().toString().contains("INVALID_ROUTING_DATA"));
    }

    @Test
    void preservesNestedParametersLongsDirectionAndUnavailableDeclarationsAcrossCacheLoads() throws Exception {
        ObjectNode body = request(false);
        body.set("filter", mapper.readTree("{\"field\":\"params.inspection.intervalDays\",\"op\":\"gte\",\"value\":30.0}"));
        var summary = query.query("fixture", "transports", body).path("transports");
        assertEquals(1, summary.size());
        assertEquals(9007199254740993L, summary.get(0).path("params").path("large").longValue());
        assertEquals("Facilities", summary.get(0).path("params").path("inspection").path("team").asText());
        body.put("inDetail", true);
        var detail = query.query("fixture", "transports", body).path("transports").get(0);
        assertEquals(summary.get(0).path("params"), detail.path("params"));
        assertEquals("NoAccess", detail.path("stations").get(0).path("permissions").asText());
        assertEquals("Lower::landing", detail.path("direction").get(0).path("fromNodeId").asText());
        assertFalse(detail.path("ridePermissions").get(0).path("allowed").asBoolean());
        ObjectNode route = request(false).put("startNode", "Lower::landing").put("endNode", "Upper::landing");
        ApiException error = assertThrows(ApiException.class, () -> new NavigationApiService(buildings, mapper).findRoute("fixture", route));
        assertTrue(error.body().toString().contains("NO_ROUTE_FOUND"));
        assertFalse(error.body().toString().contains("NO_ROUTE_FOR_USER_PARAMS"));
    }

    @Test
    void filtersNeighborsAfterRepresentativeSelectionAndKeepsAllParallelEdges() throws Exception {
        ObjectNode body = request(true);
        body.putObject("selection").put("nodeId", "Lower::landing");
        body.set("filter", mapper.readTree("{\"field\":\"tags\",\"op\":\"contains\",\"value\":\"indoor\"}"));
        var neighbors = query.query("fixture", "neighbors", body);
        assertTrue(neighbors.path("neighbors").isEmpty());
        assertEquals(0, neighbors.path("total").asInt());
        body.putObject("selection").put("fromNodeId", "Lower::landing");
        assertEquals(5, query.query("fixture", "edges", body).path("edges").get(0).path("cost").asInt());
        body.remove("filter");
        assertEquals(2, query.query("fixture", "edges", body).path("edges").size());
        body.putObject("selection").put("fromNodeId", "Lower::destination");
        assertTrue(query.query("fixture", "edges", body).path("edges").isEmpty());
    }

    @Test
    void reportsUnserializableCustomParametersWithoutDroppingThem() throws Exception {
        Path file = root.resolve("fixture/Moving");
        Files.writeString(file, Files.readString(file).replace("time = 18,", "time = 18, callback = (fn x: Int => x),"));
        var error = assertThrows(DiscoveryProjectionException.class, () -> query.query("fixture", "transports", request(true)));
        assertEquals("Moving", error.getOwner());
        assertEquals(".params.callback", error.getFieldPath());
    }

    @Test
    void partialCataloguePreservesScopesAndLocationsWithoutEvaluatingParameters() throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.set("files", mapper.valueToTree(buildings.files("fixture")));
        var authoring = new AuthoringApiService(buildings, mapper, 512, 4194304, 2);
        var missing = authoring.validate(body);
        assertEquals(400, missing.getStatusCode().value());
        var catalogue = mapper.valueToTree(missing.getBody()).path("constraints");
        assertEquals(2, catalogue.path("entries").size());
        assertTrue(catalogue.path("complete").asBoolean());
        assertTrue(catalogue.path("entries").get(0).path("source").path("line").asInt() > 0);
        ((ObjectNode) body.get("files")).put("Upper", "topo-map Upper() {\n constraint Recoverable { require true }\n");
        var partial = mapper.valueToTree(authoring.validate(body).getBody());
        assertFalse(partial.path("constraints").path("complete").asBoolean());
        assertTrue(partial.path("constraints").path("entries").toString().contains("Recoverable"), partial.toString());
        assertFalse(partial.path("diagnostics").isEmpty());
    }
}
