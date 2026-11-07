package com.e611.toponavi.web.project;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * After startup, losing the database maps to the specified 503 codes at each boundary, while liveness and
 * request-supplied validation keep working without storage.
 */
class DatabaseOutageIntegrationTest {

    @Test
    void storageOutageMapsToBoundarySpecific503s() {
        PostgreSQLContainer<?> postgres = PostgresIntegrationSupport.newContainer();
        try (RunningApplication app = RunningApplication.start(postgres.getJdbcUrl(),
                "spring.datasource.hikari.connection-timeout=1000", "spring.datasource.hikari.validation-timeout=500")) {
            Http http = app.http(PostgresIntegrationSupport.TOKEN);
            Http.Response created = http.post("/api/v1/projects", Map.of("buildingId", "Outage", "name", "Outage",
                    "files", Map.of("configuration", "building-includes {\n}\n", "root", "root R() {\n}\n")));
            assertEquals(201, created.status(), created.body().toString());
            String id = created.project().path("id").asText();

            postgres.stop();

            assertOutage(http.get("/api/v1/buildings"), "BUILDING_CATALOG_UNAVAILABLE");
            assertOutage(http.post("/api/v1/buildings/Outage/nodes/query", Map.of()), "BUILDING_UNAVAILABLE");
            assertOutage(http.post("/api/v1/buildings/Outage/find-route",
                    Map.of("startNode", "A::a", "endNode", "A::b")), "BUILDING_UNAVAILABLE");
            assertOutage(http.get("/api/v1/projects"), "PROJECT_STORE_UNAVAILABLE");
            assertOutage(http.get("/api/v1/projects/" + id), "PROJECT_STORE_UNAVAILABLE");
            assertOutage(http.get("/api/v1/projects/by-building-id/Outage"), "PROJECT_STORE_UNAVAILABLE");
            assertOutage(http.post("/api/v1/projects", Map.of("buildingId", "New", "name", "New", "files", Map.of())),
                    "PROJECT_STORE_UNAVAILABLE");
            assertOutage(http.put("/api/v1/projects/" + id, Map.of("name", "x", "files", Map.of())), "PROJECT_STORE_UNAVAILABLE");
            assertOutage(http.delete("/api/v1/projects/" + id), "PROJECT_STORE_UNAVAILABLE");

            assertEquals(200, app.http(null).get("/api/v1/health").status());
            Http.Response validated = http.post("/api/v1/authoring/validate", Map.of("files", Map.of(
                    "configuration", "building-includes {\n submap Lobby\n}\n",
                    "root", "root R() {\n}\n",
                    "Lobby", "topo-map Lobby() {\n let params = {area = 1}\n topo-node entrance\n}\n")));
            assertEquals(200, validated.status(), validated.body().toString());
            assertTrue(validated.body().path("valid").asBoolean());
        } finally {
            postgres.stop();
        }
    }

    private static void assertOutage(Http.Response response, String code) {
        assertEquals(503, response.status(), response.body().toString());
        assertEquals(code, response.code());
        String body = response.body().toString().toLowerCase();
        assertFalse(body.contains("sql") || body.contains("jdbc") || body.contains("connection"), body);
    }
}
