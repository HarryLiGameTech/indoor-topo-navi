package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiErrors;
import com.e611.toponavi.web.auth.ApiAccessPolicy;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.query.FilterParser;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SwfcEndpointTest {
    @TempDir static Path cache;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static MockMvc mvc;
    private static BuildingService buildings;

    @BeforeAll
    static void setup() {
        buildings = new BuildingService(Path.of("../examples").toAbsolutePath().normalize().toString(), new CompilationCacheService(cache), MAPPER);
        var queries = new TopologyQueryService(buildings, new FilterParser(16, 256, 1024, 16384, 256), MAPPER);
        mvc = MockMvcBuilders.standaloneSetup(new TopoController(buildings, queries,
                new NavigationApiService(buildings, MAPPER), new AuthoringApiService(buildings, MAPPER, 512, 4194304, 2),
                new ApiAccessPolicy("swfc", false))).setControllerAdvice(new ApiErrors()).build();
    }

    private ObjectNode request() {
        ObjectNode body = MAPPER.createObjectNode();
        body.putObject("userParams").put("haveStaffCard", false).put("haveManagementCard", false)
                .put("haveRoomKey", false).put("id", 0).put("aggregatedWeight", 0);
        return body;
    }

    private JsonNode query(String operation, ObjectNode request, int status) throws Exception {
        var httpResponse = mvc.perform(post("/api/v1/buildings/swfc/" + operation).contentType("application/json")
                .content(request.toString())).andReturn().getResponse();
        String json = httpResponse.getContentAsString();
        assertEquals(status, httpResponse.getStatus(), json);
        JsonNode response = MAPPER.readTree(json);
        assertEquals(status >= 400, response.has("error"), json);
        return response;
    }

    @Test
    void queriesAllNodesAndExpandsActualStarbucksMetadata() throws Exception {
        JsonNode all = query("nodes/query", request(), 200);
        assertFalse(all.path("inDetail").asBoolean());
        assertTrue(all.path("nodes").size() > 500);
        assertFalse(all.path("nodes").get(0).has("attributes"));
        ObjectNode details = request().put("inDetail", true);
        details.putObject("selection").putArray("nodeIds").add("LowerLobby::cafe");
        JsonNode node = query("nodes/query", details, 200).path("nodes").get(0);
        assertEquals("Starbucks Coffee", node.path("displayName").asText());
        assertEquals("Starbucks Coffee", node.path("attributes").path("shop_name").asText());
        assertTrue(node.path("attributes").path("tags").toString().contains("cafe"));
        JsonNode detailedAll = query("nodes/query", request().put("inDetail", true), 200);
        assertEquals(all.path("nodes").size(), detailedAll.path("nodes").size());
    }

    @Test
    void filtersTagsAndCustomAttributesBeforeSummaryProjection() throws Exception {
        ObjectNode body = request();
        body.putObject("selection").put("mapId", "LowerLobby");
        body.set("filter", MAPPER.readTree("""
                {"and":[{"field":"attributes.tags","op":"contains","value":"cafe"},
                  {"field":"attributes.shop_name","op":"isNotNull"}]}
                """));
        JsonNode summary = query("nodes/query", body, 200).path("nodes");
        assertEquals(1, summary.size());
        assertEquals("LowerLobby::cafe", summary.get(0).path("nodeId").asText());
        assertFalse(summary.get(0).has("attributes"));
        body.put("inDetail", true);
        assertEquals(1, query("nodes/query", body, 200).path("nodes").size());
        body.putObject("selection").putArray("nodeIds");
        assertTrue(query("nodes/query", body, 200).path("nodes").isEmpty());
        body.set("filter", MAPPER.readTree("{\"or\":[{\"field\":\"nodeId\",\"op\":\"exists\"},{}]}"));
        assertEquals("INVALID_FILTER", query("nodes/query", body, 400).path("error").path("code").asText());
    }

    @Test
    void rejectsAmbiguousNeighborsAndReturnsMatchingCountsBeforeLimit() throws Exception {
        ObjectNode body = request();
        body.putObject("selection").put("nodeId", "LS1_hall");
        JsonNode ambiguous = query("nodes/neighbors", body, 422);
        assertEquals("AMBIGUOUS_NODE_ID", ambiguous.path("error").path("code").asText());
        assertTrue(ambiguous.path("error").path("details").path("candidates").size() > 1);
        body.putObject("selection").put("nodeId", "cafe");
        body.put("limit", 1).put("inDetail", true);
        JsonNode neighbors = query("nodes/neighbors", body, 200);
        assertEquals("LowerLobby::cafe", neighbors.path("resolvedNodeId").asText());
        assertEquals("LowerLobby::gate_1", neighbors.path("neighbors").get(0).path("nodeId").asText());
        assertEquals(5, neighbors.path("neighbors").get(0).path("cost").asDouble());
        assertEquals(3, neighbors.path("total").asInt());
        assertTrue(neighbors.path("truncated").asBoolean());
        assertTrue(neighbors.path("neighbors").get(0).has("edgeAttributes"));
        body.set("filter", MAPPER.readTree("{\"field\":\"nodeAttributes.tags\",\"op\":\"contains\",\"value\":\"door\"}"));
        assertEquals(1, query("nodes/neighbors", body, 200).path("total").asInt());
        body.putObject("selection").put("nodeId", "LowerLobby::cafe").put("mapId", "UpperLobby");
        assertEquals("CONFLICTING_NODE_SELECTION", query("nodes/neighbors", body, 400).path("error").path("code").asText());
    }

    @Test
    void exposesAllTransportKindsAndOnlyDeclaredParameters() throws Exception {
        JsonNode all = query("transports/query", request(), 200).path("transports");
        List<String> types = StreamSupport.stream(all.spliterator(), false).map(node -> node.path("type").asText()).distinct().sorted().toList();
        assertEquals(List.of("Elevator", "Stairs"), types);
        ObjectNode body = request();
        body.putObject("selection").putArray("transportIds").add("LS1");
        JsonNode lift = query("transports/query", body, 200).path("transports").get(0);
        assertEquals(1.5, lift.path("params").path("maxSpeed").asDouble());
        assertEquals(3, lift.path("params").size());
        assertFalse(lift.path("params").has("capacity"));
        assertEquals(7, lift.path("stations").size());
        assertFalse(lift.path("stations").get(0).has("permissions"));
        body.put("inDetail", true);
        JsonNode detailed = query("transports/query", body, 200).path("transports").get(0);
        assertEquals(lift.path("params"), detailed.path("params"));
        assertTrue(detailed.path("stations").get(0).has("permissions"));
        body.set("filter", MAPPER.readTree("{\"field\":\"params.capacity\",\"op\":\"isNotNull\"}"));
        assertTrue(query("transports/query", body, 200).path("transports").isEmpty());
        body.remove("filter");
        body.putObject("selection").putArray("transportIds").add("GroundLobbyEscalator");
        // This source file is shipped but is not included by SWFC configuration.
        assertTrue(query("transports/query", body, 200).path("transports").isEmpty());
    }

    @Test
    void parameterSpecificEdgesRetainActionsAndRouteCost() throws Exception {
        ObjectNode edges = request();
        edges.putObject("selection").put("fromNodeId", "LowerLobby::entrance_Z1_2_B").put("toNodeId", "LowerLobby::L1_2_hall_end_B");
        assertTrue(query("edges/query", edges, 200).path("edges").isEmpty());
        ((ObjectNode) edges.get("userParams")).put("haveStaffCard", true);
        JsonNode permitted = query("edges/query", edges, 200).path("edges");
        assertEquals(1, permitted.size());
        assertTrue(permitted.get(0).path("requiredActions").toString().contains("cross_turnstile"));
        for (String mode : List.of("fullyInformed", "partiallyInformed")) {
            ObjectNode route = request().put("startNode", "LowerLobby::gate_1").put("endNode", "LowerLobby::cafe").put("routingMode", mode);
            JsonNode planned = query("find-route", route, 200);
            assertEquals(5, planned.path("totalCostSeconds").asDouble());
            assertFalse(planned.has("cacheKey"));
            assertFalse(planned.has("filesLoaded"));
            assertEquals("conservative", planned.path("appliedTraversalPreference").path("riskPreference").asText());
            route.putObject("traversalPreference").putArray("banTags").add("cafe");
            assertEquals("DESTINATION_HAS_BANNED_TAG", query("find-route", route, 422).path("error").path("code").asText());
        }
        ObjectNode transportRoute = request().put("startNode", "LowerLobby::LS1_hall")
                .put("endNode", "Floor3::LS1_hall").put("routingMode", "fullyInformed");
        JsonNode planned = query("find-route", transportRoute, 200);
        assertTrue(planned.path("totalCostSeconds").asDouble() > 0);
        assertTrue(StreamSupport.stream(planned.path("steps").spliterator(), false)
                .anyMatch(step -> step.path("type").asText().equals("Transport")
                        && step.path("toGraph").asText().equals("Floor3")));
    }

    @Test
    void validationCatalogueSurvivesMissingParametersAndKeepsDeclaredFalseConstraints() throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.set("files", MAPPER.valueToTree(buildings.files("swfc")));
        JsonNode missing = MAPPER.readTree(mvc.perform(post("/api/v1/authoring/validate").contentType("application/json").content(body.toString()))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString());
        assertEquals(5, missing.path("parameters").size());
        assertEquals(4, missing.path("constraints").path("entries").size());
        assertTrue(missing.path("constraints").path("complete").asBoolean());
        body.set("userParams", request().get("userParams"));
        JsonNode validated = MAPPER.readTree(mvc.perform(post("/api/v1/authoring/validate").contentType("application/json").content(body.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(missing.path("constraints"), validated.path("constraints"));
        assertTrue(validated.path("valid").asBoolean());
    }

    @Test
    void rejectsStrictInputFailuresAndRemovesOldHandlers() throws Exception {
        assertEquals("INVALID_USER_PARAMS", query("nodes/query", MAPPER.createObjectNode(), 400).path("error").path("code").asText());
        for (String invalid : List.of("{\"inDetail\":\"true\"}", "{\"selection\":null}", "{\"surprise\":true}", "{\"filter\":null}")) {
            query("nodes/query", (ObjectNode) MAPPER.readTree(invalid), 400);
        }
        mvc.perform(get("/api/v1/quick-demo-all-available-nodes")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/test-constraints")).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/buildings/swfc/nodes/query").contentType("application/json").content("[]")).andExpect(status().isBadRequest());
        JsonNode maps = query("maps/query", request(), 200).path("maps");
        assertTrue(maps.size() >= 100);
    }
}
