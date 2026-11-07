package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiErrors;
import com.e611.toponavi.web.project.InMemoryProjectStore;
import com.e611.toponavi.web.project.ProjectApplicationService;
import com.e611.toponavi.web.project.ProjectLimits;
import com.e611.toponavi.web.project.ProjectStorageValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ProjectControllerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final InMemoryProjectStore store = new InMemoryProjectStore();
    private ProjectLimits limits = new ProjectLimits(512, 4_194_304, 33_554_432, 5_000);
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        rebuild();
    }

    private void rebuild() {
        var validator = new ProjectStorageValidator(limits);
        var controller = new ProjectController(new ProjectApplicationService(store, validator), new ProjectRequestReader(limits, validator));
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiErrors()).build();
    }

    private record Response(int status, JsonNode body, String location) {
        String code() { return body.path("error").path("code").asText(); }
        String path() { return body.path("error").path("details").path("path").asText(); }
    }

    private Response perform(MockHttpServletRequestBuilder request) throws Exception {
        var response = mvc.perform(request).andReturn().getResponse();
        String text = response.getContentAsString(StandardCharsets.UTF_8);
        return new Response(response.getStatus(), text.isEmpty() ? MAPPER.nullNode() : MAPPER.readTree(text), response.getHeader("Location"));
    }

    private Response send(MockHttpServletRequestBuilder request, String json) throws Exception {
        return perform(request.contentType("application/json").content(json.getBytes(StandardCharsets.UTF_8)));
    }

    private static String create(String buildingId, String name, Map<String, String> files) throws Exception {
        ObjectNode body = MAPPER.createObjectNode().put("buildingId", buildingId).put("name", name);
        body.set("files", MAPPER.valueToTree(files));
        return MAPPER.writeValueAsString(body);
    }

    private Response createProject(String buildingId, Map<String, String> files) throws Exception {
        return send(post("/api/v1/projects"), create(buildingId, "Project " + buildingId.length(), files));
    }

    @Test
    void createsReadsReplacesAndDeletesWithExactSourcePreservation() throws Exception {
        Map<String, String> files = Map.of(
                "configuration", "building-includes {\n submap Lobby\n}\n",
                "Lobby.tmap", "\uFEFFtopo-map Lobby() {\r\n topo-node entrance \t\r\n}\r\n",
                "floors/Ünïcode 层.tmap", "// 😀 emoji, CJK 中文, trailing spaces   \n",
                "root", "");
        Response created = send(post("/api/v1/projects"), create("LobbyDemo", "Lobby demonstration", files));
        assertEquals(201, created.status(), created.body().toString());
        JsonNode summary = created.body().path("project");
        assertEquals("success", created.body().path("status").asText());
        String id = summary.path("id").asText();
        assertEquals("http://localhost/api/v1/projects/" + id, created.location());
        assertEquals("LobbyDemo", summary.path("buildingId").asText());
        assertEquals("Lobby demonstration", summary.path("name").asText());
        assertEquals(summary.path("createdAt"), summary.path("updatedAt"));
        assertFalse(summary.has("files"));

        Response detail = perform(get("/api/v1/projects/" + id));
        assertEquals(200, detail.status());
        assertEquals(MAPPER.valueToTree(files), detail.body().path("project").path("files"));
        assertEquals(detail.body(), perform(get("/api/v1/projects/by-building-id/LobbyDemo")).body());
        assertEquals("PROJECT_NOT_FOUND", perform(get("/api/v1/projects/by-building-id/lobbydemo")).code());

        // Unfinished source is a valid storage request; omitted paths are deleted.
        Response replaced = send(put("/api/v1/projects/" + id),
                "{\"name\":\"Renamed\",\"files\":{\"configuration\":\"building-includes {\\n\",\"Lobby.tmap\":\"topo-map Lobby() {\\n\"}}");
        assertEquals(200, replaced.status(), replaced.body().toString());
        assertEquals("Renamed", replaced.body().path("project").path("name").asText());
        assertEquals("LobbyDemo", replaced.body().path("project").path("buildingId").asText());
        assertEquals(summary.path("createdAt"), replaced.body().path("project").path("createdAt"));
        JsonNode stored = perform(get("/api/v1/projects/" + id)).body().path("project").path("files");
        assertEquals(List.of("Lobby.tmap", "configuration"), MAPPER.convertValue(stored, Map.class).keySet().stream().sorted().toList());

        assertEquals(200, send(put("/api/v1/projects/" + id), "{\"name\":\"Empty\",\"files\":{}}").status());
        assertTrue(perform(get("/api/v1/projects/" + id)).body().path("project").path("files").isEmpty());

        assertEquals(204, perform(delete("/api/v1/projects/" + id)).status());
        assertEquals("PROJECT_NOT_FOUND", perform(delete("/api/v1/projects/" + id)).code());
        assertEquals(404, perform(get("/api/v1/projects/" + id)).status());
        assertEquals(404, send(put("/api/v1/projects/" + id), "{\"name\":\"x\",\"files\":{}}").status());
        // Deletion frees the identifier; reuse creates a new project.
        Response reused = createProject("LobbyDemo", Map.of());
        assertEquals(201, reused.status());
        assertNotEquals(id, reused.body().path("project").path("id").asText());
    }

    @Test
    void rejectsExactAndCaseOnlyBuildingIdCollisions() throws Exception {
        assertEquals(201, createProject("swfc", Map.of()).status());
        for (String collision : List.of("swfc", "SWFC", "Swfc")) {
            Response conflict = createProject(collision, Map.of());
            assertEquals(409, conflict.status());
            assertEquals("BUILDING_ID_CONFLICT", conflict.code());
        }
        assertEquals(List.of("swfc"), store.buildingIds());
        for (String shipped : List.of("indigoBJ", "nbc4", "trent")) assertEquals(201, createProject(shipped, Map.of()).status());
    }

    @Test
    void validatesIdentityAndEnvelope() throws Exception {
        for (String id : List.of("", "-lead", ".hidden", "has space", "slash/inside", "ü", "a".repeat(256))) {
            Response invalid = createProject(id, Map.of());
            assertEquals(400, invalid.status(), id);
            assertEquals("/buildingId", invalid.path(), id);
        }
        assertEquals(201, createProject("a".repeat(255), Map.of()).status());
        assertEquals(201, createProject("A0._-z", Map.of()).status());

        record Case(String json, String pointer) {}
        for (Case c : List.of(
                new Case("[]", ""),
                new Case("\"text\"", ""),
                new Case("{", ""),
                new Case("", ""),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{}} {}", ""),
                new Case("{\"name\":\"x\",\"files\":{}}", "/buildingId"),
                new Case("{\"buildingId\":\"x\",\"files\":{}}", "/name"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\"}", "/files"),
                new Case("{\"buildingId\":null,\"name\":\"x\",\"files\":{}}", "/buildingId"),
                new Case("{\"buildingId\":\"x\",\"name\":null,\"files\":{}}", "/name"),
                new Case("{\"buildingId\":\"x\",\"name\":\" \\t \",\"files\":{}}", "/name"),
                new Case("{\"buildingId\":\"x\",\"name\":\"" + "n".repeat(256) + "\",\"files\":{}}", "/name"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":null}", "/files"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":[]}", "/files"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":null}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":1}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":{}}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{},\"extra\":true}", "/extra"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"name\":\"y\",\"files\":{}}", "/name"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"a~b/c.tmap\":\"\",\"a~b/c.tmap\":\"\"}}", "/files/a~0b~1c.tmap"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":\"\\u0000\"}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":\"\\ud800\"}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":\"\\udc00\\ud83d\"}}", "/files/root"),
                new Case("{\"buildingId\":\"x\",\"name\":\"\\ud800\",\"files\":{}}", "/name"))) {
            Response invalid = send(post("/api/v1/projects"), c.json());
            assertEquals(400, invalid.status(), c.json() + " -> " + invalid.body());
            assertEquals("INVALID_REQUEST", invalid.code(), c.json());
            assertEquals(c.pointer(), invalid.path(), c.json());
        }
        assertTrue(store.buildingIds().stream().noneMatch(id -> id.equals("x")));
        // Malformed UTF-8 bytes are rejected rather than replaced.
        byte[] malformed = "{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{\"root\":\"\u0000\"}}".getBytes(StandardCharsets.UTF_8);
        malformed[malformed.length - 4] = (byte) 0xC3;
        Response bad = perform(post("/api/v1/projects").contentType("application/json").content(malformed));
        assertEquals(400, bad.status());
        assertEquals("INVALID_REQUEST", bad.code());
    }

    @Test
    void replacementRequiresNameAndFilesAndRejectsRenames() throws Exception {
        String id = createProject("Fixed", Map.of("root", "")).body().path("project").path("id").asText();
        Response rename = send(put("/api/v1/projects/" + id), "{\"buildingId\":\"Other\",\"name\":\"x\",\"files\":{}}");
        assertEquals(400, rename.status());
        assertEquals("/buildingId", rename.path());
        assertEquals("/files", send(put("/api/v1/projects/" + id), "{\"name\":\"x\"}").path());
        assertEquals("/name", send(put("/api/v1/projects/" + id), "{\"files\":{}}").path());
        assertEquals(Map.of("root", ""), store.findByBuildingId("Fixed").orElseThrow().files());
    }

    @Test
    void validatesFilePathsWithoutNormalizingThem() throws Exception {
        for (String path : List.of("configuration", "configuration.tcfg", "root", "Lobby.tmap", "Moving.ttr", "floors/Lobby.tmap",
                "a.b.tmap", "Ünïcode 层", "x".repeat(512))) {
            Response accepted = createProject("ok" + Math.abs(path.hashCode()), Map.of(path, ""));
            assertEquals(201, accepted.status(), path + " -> " + accepted.body());
        }
        for (String path : List.of("", "/abs", "dir/", "a\\b", "a//b", "./a", "a/./b", "floor/../Lobby.tmap", "..", "C:root",
                "file:root", "http://x/root", "a\u0001b", "a\u007fb", "tab\there", "Lobby.json", "map.json", "README.md", "Lobby.TMAP",
                "Lobby.tmap.bak", "x".repeat(513), "é".repeat(257))) {
            Response rejected = createProject("bad", Map.of(path, ""));
            assertEquals(400, rejected.status(), path);
            assertEquals("/files/" + ProjectStorageValidator.escape(path), rejected.path(), path);
        }
        assertFalse(store.buildingIds().contains("bad"));
    }

    @Test
    void enforcesConfiguredSizeLimitsWith413() throws Exception {
        limits = new ProjectLimits(2, 10, 300, 5_000);
        rebuild();
        assertEquals(201, createProject("fits", Map.of("a", "12345", "b", "123é")).status());
        Response files = createProject("files", Map.of("a", "", "b", "", "c", ""));
        assertEquals(413, files.status());
        assertEquals("PROJECT_TOO_LARGE", files.code());
        assertEquals("platform.projects.max-files", files.body().path("error").path("details").path("limit").asText());
        assertEquals(2, files.body().path("error").path("details").path("maximum").asInt());
        Response bytes = createProject("bytes", Map.of("a", "12345", "b", "1234é"));
        assertEquals(413, bytes.status());
        assertEquals("platform.projects.max-source-bytes", bytes.body().path("error").path("details").path("limit").asText());
        Response body = send(post("/api/v1/projects"), create("body", "x".repeat(300), Map.of()));
        assertEquals(413, body.status());
        assertEquals("platform.projects.max-request-bytes", body.body().path("error").path("details").path("limit").asText());
        assertEquals(List.of("fits"), store.buildingIds());
    }

    @Test
    void boundsUndeclaredBodyLengthsWhileStreamingAndRejectsContentEncodings() {
        var reader = new ProjectRequestReader(new ProjectLimits(512, 4_194_304, 64, 5_000),
                new ProjectStorageValidator(new ProjectLimits(512, 4_194_304, 64, 5_000)));
        byte[] large = ("{\"buildingId\":\"x\",\"name\":\"" + "x".repeat(100) + "\",\"files\":{}}").getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest chunked = new MockHttpServletRequest("POST", "/api/v1/projects") {
            @Override public long getContentLengthLong() { return -1; }
            @Override public int getContentLength() { return -1; }
            @Override public jakarta.servlet.ServletInputStream getInputStream() {
                var bytes = new ByteArrayInputStream(large);
                return new org.springframework.mock.web.DelegatingServletInputStream(bytes);
            }
        };
        var tooLarge = assertThrows(com.e611.toponavi.web.api.ApiException.class,
                () -> reader.read(chunked, ProjectRequestReader.Kind.CREATE));
        assertEquals(413, tooLarge.status());

        var gzip = new MockHttpServletRequest("POST", "/api/v1/projects");
        gzip.addHeader("Content-Encoding", "gzip");
        gzip.setContent("{}".getBytes(StandardCharsets.UTF_8));
        assertEquals(400, assertThrows(com.e611.toponavi.web.api.ApiException.class,
                () -> reader.read(gzip, ProjectRequestReader.Kind.CREATE)).status());
    }

    @Test
    void listsSummariesInBuildingIdOrderWithStrictPagination() throws Exception {
        Response empty = perform(get("/api/v1/projects"));
        assertEquals(200, empty.status());
        assertEquals(MAPPER.readTree("{\"status\":\"success\",\"projects\":[],\"page\":0,\"size\":50,\"total\":0}"), empty.body());
        for (String id : List.of("trent", "Zeta", "alpha", "swfc")) createProject(id, Map.of("root", "x"));
        Response first = perform(get("/api/v1/projects").param("page", "0").param("size", "3"));
        assertEquals(List.of("Zeta", "alpha", "swfc"), first.body().path("projects").findValuesAsText("buildingId"));
        assertEquals(4, first.body().path("total").asInt());
        assertEquals(3, first.body().path("size").asInt());
        assertFalse(first.body().path("projects").get(0).has("files"));
        Response second = perform(get("/api/v1/projects?page=1&size=3"));
        assertEquals(List.of("trent"), second.body().path("projects").findValuesAsText("buildingId"));
        assertEquals(1, second.body().path("page").asInt());
        assertTrue(perform(get("/api/v1/projects?page=9")).body().path("projects").isEmpty());
        assertEquals(200, perform(get("/api/v1/projects?size=200")).status());
        for (String query : List.of("page=-1", "page=x", "page=1.0", "page=", "page=99999999999", "size=0", "size=201",
                "size=-5", "limit=1", "page=0&page=1", "size=1&size=1", "PAGE=1")) {
            Response invalid = perform(get("/api/v1/projects?" + query));
            assertEquals(400, invalid.status(), query);
            assertEquals("INVALID_REQUEST", invalid.code(), query);
        }
    }

    @Test
    void reportsMalformedIdsQueryStringsAndStoreOutages() throws Exception {
        for (String id : List.of("not-a-uuid", "1-1-1-1-1", "00000000000000000000000000000000", "by-building-id")) {
            Response invalid = perform(get("/api/v1/projects/" + id));
            assertEquals(400, invalid.status(), id);
            assertEquals("/id", invalid.path(), id);
        }
        String unknown = "123e4567-e89b-12d3-a456-426614174000";
        assertEquals("PROJECT_NOT_FOUND", perform(get("/api/v1/projects/" + unknown)).code());
        assertEquals("PROJECT_NOT_FOUND", perform(get("/api/v1/projects/" + unknown.toUpperCase())).code());
        assertEquals(400, perform(get("/api/v1/projects/" + unknown + "?verbose=true")).status());
        assertEquals(400, send(post("/api/v1/projects?x=1"), create("q", "q", Map.of())).status());

        store.setUnavailable(true);
        for (MockHttpServletRequestBuilder request : List.of(get("/api/v1/projects"), get("/api/v1/projects/" + unknown),
                get("/api/v1/projects/by-building-id/swfc"), delete("/api/v1/projects/" + unknown))) {
            Response unavailable = perform(request);
            assertEquals(503, unavailable.status());
            assertEquals("PROJECT_STORE_UNAVAILABLE", unavailable.code());
        }
        Response write = createProject("down", Map.of());
        assertEquals(503, write.status());
        assertEquals("PROJECT_STORE_UNAVAILABLE", write.code());
        assertFalse(write.body().toString().contains("Simulated"));
    }
}
