package com.e611.toponavi.web.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Stored projects survive restarting both the application and the database server on the same data volume. */
class PersistenceRestartIntegrationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final PostgreSQLContainer<?> POSTGRES = PostgresIntegrationSupport.newContainer();

    @AfterAll
    static void stop() {
        POSTGRES.stop();
    }

    @Test
    void projectsSurviveApplicationAndDatabaseRestarts() throws Exception {
        Map<String, String> files = Map.of(
                "configuration", "building-includes {\n submap Lobby\n}\n",
                "root", "root Building() {\n}\n",
                "Lobby", "topo-map Lobby() {\r\n let params = {area = 1}\r\n topo-node entrance  \r\n}\r\n");
        String id;
        String createdAt;
        try (RunningApplication first = RunningApplication.start(POSTGRES.getJdbcUrl())) {
            Http.Response created = first.http(PostgresIntegrationSupport.TOKEN)
                    .post("/api/v1/projects", Map.of("buildingId", "Durable", "name", "Durable 项目", "files", files));
            assertEquals(201, created.status(), created.body().toString());
            id = created.project().path("id").asText();
            createdAt = created.project().path("createdAt").asText();
        }

        String jdbcUrl = restartDatabase();

        try (RunningApplication second = RunningApplication.start(jdbcUrl)) {
            Http http = second.http(PostgresIntegrationSupport.TOKEN);
            Http.Response stored = http.get("/api/v1/projects/" + id);
            assertEquals(200, stored.status(), stored.body().toString());
            assertEquals("Durable", stored.project().path("buildingId").asText());
            assertEquals("Durable 项目", stored.project().path("name").asText());
            assertEquals(createdAt, stored.project().path("createdAt").asText());
            assertEquals(MAPPER.valueToTree(files), stored.project().path("files"));
            assertEquals(MAPPER.readTree("[\"Durable\"]"), http.get("/api/v1/buildings").body().path("buildings"));
            assertEquals(200, http.post("/api/v1/buildings/Durable/nodes/query", Map.of()).status());
        }
    }

    /** Restarts the same container (same data directory) and returns the possibly re-mapped JDBC URL. */
    private static String restartDatabase() throws Exception {
        DockerClient docker = DockerClientFactory.instance().client();
        docker.restartContainerCmd(POSTGRES.getContainerId()).exec();
        var bindings = docker.inspectContainerCmd(POSTGRES.getContainerId()).exec().getNetworkSettings().getPorts().getBindings();
        String hostPort = bindings.get(com.github.dockerjava.api.model.ExposedPort.tcp(5432))[0].getHostPortSpec();
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + hostPort + "/toponavi";
        Instant deadline = Instant.now().plus(Duration.ofSeconds(60));
        while (true) {
            try (var connection = DriverManager.getConnection(url, "toponavi", "toponavi")) {
                return url;
            } catch (SQLException notReady) {
                if (Instant.now().isAfter(deadline)) throw notReady;
                Thread.sleep(250);
            }
        }
    }
}
