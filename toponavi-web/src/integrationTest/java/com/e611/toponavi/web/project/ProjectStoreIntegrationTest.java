package com.e611.toponavi.web.project;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.query.FilterParser;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.BuildingService;
import com.e611.toponavi.web.service.CompilationParameterResolver;
import com.e611.toponavi.web.service.NavigationApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"platform.projects.lock-timeout-ms=1000", "spring.datasource.hikari.maximum-pool-size=6"})
@Import(PostgresIntegrationSupport.IsolatedCache.class)
class ProjectStoreIntegrationTest {
    private static final PostgreSQLContainer<?> POSTGRES = PostgresIntegrationSupport.shared();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path EXAMPLES = Path.of("../examples").toAbsolutePath().normalize();

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        PostgresIntegrationSupport.register(registry, POSTGRES);
    }

    @LocalServerPort int port;
    @Autowired ProjectStore store;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectFileRepository fileRepository;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ProjectLimits limits;
    @Autowired DataSource dataSource;
    @Autowired ApplicationContext context;
    @TempDir Path localCache;

    private Http http;

    @BeforeEach
    void clean() throws SQLException {
        http = new Http(port, PostgresIntegrationSupport.TOKEN);
        try (Connection connection = PostgresIntegrationSupport.connect(POSTGRES); var statement = connection.createStatement()) {
            statement.execute("TRUNCATE toponavi_projects.projects CASCADE");
        }
    }

    private static Map<String, Object> project(String buildingId, String name, Map<String, String> files) {
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", buildingId);
        body.put("name", name);
        body.put("files", files);
        return body;
    }

    private static Map<String, Object> replacement(String name, Map<String, String> files) {
        return Map.of("name", name, "files", files);
    }

    private String create(String buildingId, Map<String, String> files) {
        Http.Response created = http.post("/api/v1/projects", project(buildingId, buildingId, files));
        assertEquals(201, created.status(), created.body().toString());
        return created.project().path("id").asText();
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = PostgresIntegrationSupport.connect(POSTGRES); var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    private String text(String sql) throws SQLException {
        try (Connection connection = PostgresIntegrationSupport.connect(POSTGRES); var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            return result.next() ? result.getString(1) : null;
        }
    }

    @Test
    void migrationOwnsOnlyTheNewSchemaAndLeavesLegacyDataUntouched() throws SQLException {
        assertEquals(1, count("SELECT count(*) FROM toponavi_projects.flyway_schema_history WHERE version = '1' AND success"));
        assertNull(text("SELECT to_regclass('public.flyway_schema_history')::text"));
        assertEquals("legacy sentinel|toponavi-map-store/legacy", text("SELECT name || '|' || repo FROM public.maps"));
        assertEquals("legacy-user", text("SELECT github_login FROM public.users"));
        assertEquals(1, count("SELECT count(*) FROM public.maps"));
        assertEquals("UTF8", text("SELECT pg_encoding_to_char(encoding) FROM pg_database WHERE datname = current_database()"));
        assertEquals("C", text("SELECT collation_name FROM information_schema.columns WHERE table_schema = 'toponavi_projects'"
                + " AND table_name = 'projects' AND column_name = 'building_id'"));
    }

    @Test
    void crudRoundTripPreservesExactSourcesAndUpdatesTimestamps() throws Exception {
        Map<String, String> files = Map.of(
                "configuration", "building-includes {\n submap Lobby\n}\n",
                "Lobby.tmap", "\uFEFFtopo-map Lobby() {\r\n  topo-node entrance  \t\r\n}\r\n",
                "floors/Ünïcode 层.tmap", "// 😀 emoji, CJK 中文, trailing spaces   \n\n",
                "root", "");
        Http.Response created = http.post("/api/v1/projects", project("LobbyDemo", "Lobby demonstration", files));
        assertEquals(201, created.status());
        String id = created.project().path("id").asText();
        assertEquals("http://127.0.0.1:" + port + "/api/v1/projects/" + id, created.raw().headers().firstValue("Location").orElseThrow());

        Http.Response detail = http.get("/api/v1/projects/" + id);
        assertEquals(MAPPER.valueToTree(files), detail.project().path("files"));
        assertEquals(detail.body(), http.get("/api/v1/projects/by-building-id/LobbyDemo").body());
        assertEquals("PROJECT_NOT_FOUND", http.get("/api/v1/projects/by-building-id/lobbydemo").code());
        assertEquals("PROJECT_NOT_FOUND", http.get("/api/v1/projects/" + UUID.randomUUID()).code());

        Instant createdAt = Instant.parse(created.project().path("createdAt").asText());
        Thread.sleep(5);
        Map<String, String> changed = new HashMap<>(files);
        changed.remove("root");
        changed.put("Lobby.tmap", "topo-map Lobby() {\n"); // Unfinished sources are valid storage.
        Http.Response replaced = http.put("/api/v1/projects/" + id, replacement("Lobby demonstration", changed));
        assertEquals(200, replaced.status(), replaced.body().toString());
        assertEquals(createdAt, Instant.parse(replaced.project().path("createdAt").asText()));
        assertTrue(Instant.parse(replaced.project().path("updatedAt").asText()).isAfter(createdAt), "updatedAt changes on file-only replacement");
        assertEquals(MAPPER.valueToTree(changed), http.get("/api/v1/projects/" + id).project().path("files"));

        create("trent", Map.of());
        create("Zeta", Map.of("root", ""));
        Http.Response page = http.get("/api/v1/projects?page=0&size=2");
        assertEquals(List.of("LobbyDemo", "Zeta"), page.body().path("projects").findValuesAsText("buildingId"));
        assertEquals(3, page.body().path("total").asInt());
        assertEquals(List.of("trent"), http.get("/api/v1/projects?page=1&size=2").body().path("projects").findValuesAsText("buildingId"));

        assertEquals(204, http.delete("/api/v1/projects/" + id).status());
        assertEquals(404, http.delete("/api/v1/projects/" + id).status());
        assertEquals(0, count("SELECT count(*) FROM toponavi_projects.project_files WHERE project_id = '" + id + "'"));
        assertEquals(0, count("SELECT count(*) FROM toponavi_projects.project_files f WHERE NOT EXISTS"
                + " (SELECT 1 FROM toponavi_projects.projects p WHERE p.id = f.project_id)"));
    }

    @Test
    void concurrentCaseOnlyCreatesAdmitExactlyOneProject() throws Exception {
        List<String> variants = List.of("Race", "race", "RACE", "rAce", "Race", "racE", "RaCe", "race");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(variants.size());
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (String id : variants) results.add(pool.submit(() -> {
                start.await();
                return http.post("/api/v1/projects", project(id, id, Map.of("root", ""))).status();
            }));
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : results) statuses.add(result.get(60, TimeUnit.SECONDS));
            assertEquals(1, statuses.stream().filter(s -> s == 201).count(), statuses.toString());
            assertEquals(variants.size() - 1, statuses.stream().filter(s -> s == 409).count(), statuses.toString());
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, count("SELECT count(*) FROM toponavi_projects.projects WHERE lower(building_id) = 'race'"));
    }

    @Test
    void failedReplacementRollsBackMetadataAndEveryFile() throws Exception {
        Map<String, String> original = Map.of("configuration", "", "root", "root R() {}");
        UUID id = UUID.fromString(create("Rollback", original));
        Map<String, String> failing = new HashMap<>();
        failing.put("fresh", "new");
        failing.put("x".repeat(600), "violates the path length constraint after the old files were deleted");
        assertThrows(IllegalStateException.class, () -> store.replace(id, "Should not persist", failing));
        ProjectSnapshot after = store.findById(id).orElseThrow();
        assertEquals("Rollback", after.summary().name());
        assertEquals(original, after.files());
    }

    @Test
    void concurrentReplacementsSerializeAndReadersNeverSeeAMixture() throws Exception {
        UUID id = UUID.fromString(create("Serial", Map.of()));
        Map<String, String> a = new HashMap<>(), b = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            a.put("a" + i, "A" + i);
            b.put("b" + i, "B" + i);
            a.put("shared" + i, "A");
            b.put("shared" + i, "B"); // Reused primary keys in both sets.
        }
        AtomicBoolean writing = new AtomicBoolean(true);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            Callable<Void> writerA = () -> { for (int i = 0; i < 25; i++) store.replace(id, "A", a); return null; };
            Callable<Void> writerB = () -> { for (int i = 0; i < 25; i++) store.replace(id, "B", b); return null; };
            Future<Integer> reader = pool.submit(() -> {
                int reads = 0;
                while (writing.get()) {
                    ProjectSnapshot snapshot = store.findById(id).orElseThrow();
                    if (snapshot.summary().name().equals("A")) assertEquals(a, snapshot.files());
                    else if (snapshot.summary().name().equals("B")) assertEquals(b, snapshot.files());
                    else assertTrue(snapshot.files().isEmpty());
                    reads++;
                }
                return reads;
            });
            Future<Void> first = pool.submit(writerA);
            Future<Void> second = pool.submit(writerB);
            first.get(120, TimeUnit.SECONDS);
            second.get(120, TimeUnit.SECONDS);
            writing.set(false);
            assertTrue(reader.get(60, TimeUnit.SECONDS) > 0);
        } finally {
            writing.set(false);
            pool.shutdownNow();
        }
        ProjectSnapshot last = store.findById(id).orElseThrow();
        assertEquals(last.summary().name().equals("A") ? a : b, last.files());
    }

    @Test
    void concurrentUpdateAndDeleteNeverRecreateOrOrphan() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 10; round++) {
                UUID id = UUID.fromString(create("Doomed" + round, Map.of("configuration", "", "old", "x")));
                CountDownLatch start = new CountDownLatch(1);
                Future<Optional<ProjectSummary>> update = pool.submit(() -> {
                    start.await();
                    return store.replace(id, "updated", Map.of("new1", "y", "new2", "z"));
                });
                Future<Boolean> deletion = pool.submit(() -> {
                    start.await();
                    return store.delete(id);
                });
                start.countDown();
                update.get(30, TimeUnit.SECONDS); // Either completed before the delete, or found nothing.
                assertTrue(deletion.get(30, TimeUnit.SECONDS));
                assertTrue(store.findById(id).isEmpty());
                assertEquals(0, count("SELECT count(*) FROM toponavi_projects.project_files WHERE project_id = '" + id + "'"));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void lockTimeoutMapsToUnavailableAndStaysTransactionLocal() throws Exception {
        String id = create("Locked", Map.of());
        try (Connection holder = PostgresIntegrationSupport.connect(POSTGRES)) {
            holder.setAutoCommit(false);
            try (var statement = holder.createStatement()) {
                statement.execute("SELECT id FROM toponavi_projects.projects WHERE id = '" + id + "' FOR UPDATE");
            }
            long started = System.nanoTime();
            Http.Response blocked = http.put("/api/v1/projects/" + id, replacement("x", Map.of()));
            assertEquals(503, blocked.status(), blocked.body().toString());
            assertEquals("PROJECT_STORE_UNAVAILABLE", blocked.code());
            assertFalse(blocked.body().toString().toLowerCase().contains("select"));
            assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 10_000);
            assertEquals("PROJECT_STORE_UNAVAILABLE", http.delete("/api/v1/projects/" + id).code());
            holder.rollback();
        }
        assertEquals(200, http.put("/api/v1/projects/" + id, replacement("x", Map.of())).status());
        // Every pooled connection still has the server default; the timeout was SET LOCAL to its transaction.
        List<Connection> pooled = new ArrayList<>();
        try {
            for (int i = 0; i < 6; i++) pooled.add(dataSource.getConnection());
            for (Connection connection : pooled) {
                try (var statement = connection.createStatement(); var result = statement.executeQuery("SHOW lock_timeout")) {
                    result.next();
                    assertEquals("0", result.getString(1));
                }
            }
        } finally {
            for (Connection connection : pooled) connection.close();
        }
    }

    @Test
    void snapshotsComeFromOneCommittedStateEvenWithAConcurrentWriter() throws Exception {
        UUID id = UUID.fromString(create("Snapshot", Map.of("configuration", "", "v1", "first")));
        CountDownLatch once = new CountDownLatch(1);
        PostgresProjectStore interleaved = new PostgresProjectStore(projectRepository, fileRepository, entityManager, transactions, limits) {
            @Override
            protected void afterMetadataRead(UUID projectId) {
                if (once.getCount() == 0) return;
                once.countDown();
                // A different thread commits a full replacement between this transaction's metadata and file reads.
                ExecutorService writer = Executors.newSingleThreadExecutor();
                try {
                    assertTrue(writer.submit(() -> store.replace(projectId, "second", Map.of("v2", "second"))).get(30, TimeUnit.SECONDS).isPresent());
                } catch (Exception error) {
                    throw new IllegalStateException(error);
                } finally {
                    writer.shutdownNow();
                }
            }
        };
        ProjectSnapshot inFlight = interleaved.findById(id).orElseThrow();
        assertEquals("Snapshot", inFlight.summary().name());
        assertEquals(Map.of("configuration", "", "v1", "first"), inFlight.files());
        ProjectSnapshot fresh = store.findById(id).orElseThrow();
        assertEquals("second", fresh.summary().name());
        assertEquals(Map.of("v2", "second"), fresh.files());

        // A request that already holds its snapshot can finish computing after the project is deleted...
        BuildingService local = new BuildingService(new SnapshotOnlyStore(inFlight), new CompilationCacheService(localCache),
                new CompilationParameterResolver(), MAPPER);
        assertEquals(inFlight.files(), local.files("Snapshot"));
        assertTrue(store.delete(id));
        assertEquals(inFlight.files(), local.files("Snapshot"));
        // ...while a request starting after the deletion commits observes its absence.
        assertEquals("BUILDING_NOT_FOUND", http.post("/api/v1/buildings/Snapshot/maps/query", Map.of()).code());
    }

    @Test
    void storedBuildingsComputeExactlyLikeTheirNativeSources() throws Exception {
        for (String building : List.of("indigoBJ", "trent")) create(building, SourceDirectories.read(EXAMPLES.resolve(building)));
        create("draft", Map.of("Lobby.tmap", "topo-map Lobby() {\n")); // No configuration: stored, but not a building.
        assertEquals(MAPPER.readTree("[\"indigoBJ\",\"trent\"]"), http.get("/api/v1/buildings").body().path("buildings"));
        assertEquals("BUILDING_NOT_FOUND", http.post("/api/v1/buildings/draft/maps/query", Map.of()).code());
        assertEquals("BUILDING_NOT_FOUND", http.post("/api/v1/buildings/indigobj/maps/query", Map.of()).code());

        InMemoryProjectStore sources = SourceDirectories.storeOf(EXAMPLES);
        BuildingService local = new BuildingService(sources, new CompilationCacheService(localCache), new CompilationParameterResolver(), MAPPER);
        TopologyQueryService topology = new TopologyQueryService(local, new FilterParser(16, 256, 1024, 16384, 256), MAPPER);
        NavigationApiService navigation = new NavigationApiService(local, MAPPER);

        ObjectNode none = MAPPER.createObjectNode();
        ObjectNode detailed = MAPPER.createObjectNode().put("inDetail", true);
        ObjectNode neighbors = MAPPER.createObjectNode().put("inDetail", true);
        neighbors.putObject("selection").put("nodeId", "Level1::north_elevator_hall");
        ObjectNode tagged = MAPPER.createObjectNode();
        tagged.set("filter", MAPPER.readTree("{\"field\":\"attributes.tags\",\"op\":\"contains\",\"value\":\"shop\"}"));
        Map<String, Supplier<JsonNode>> expectations = new java.util.LinkedHashMap<>();
        Map<String, ObjectNode> requests = new java.util.LinkedHashMap<>();
        for (String kind : List.of("maps", "nodes", "edges", "transports")) {
            requests.put(kind + "/query", detailed);
            expectations.put(kind + "/query", () -> topology.query("indigoBJ", kind, detailed));
        }
        requests.put("nodes/query#filtered", tagged);
        expectations.put("nodes/query#filtered", () -> topology.query("indigoBJ", "nodes", tagged));
        requests.put("nodes/neighbors", neighbors);
        expectations.put("nodes/neighbors", () -> topology.query("indigoBJ", "neighbors", neighbors));
        ObjectNode route = MAPPER.createObjectNode().put("startNode", "Level1::north_elevator_hall")
                .put("endNode", "Level2::south_elevator_hall").put("routingMode", "fullyInformed");
        requests.put("find-route", route);
        expectations.put("find-route", () -> navigation.findRoute("indigoBJ", route));

        for (var expectation : expectations.entrySet()) {
            String operation = expectation.getKey().split("#")[0];
            Http.Response stored = http.post("/api/v1/buildings/indigoBJ/" + operation, requests.get(expectation.getKey()));
            JsonNode expected = MAPPER.readTree(MAPPER.writeValueAsString(expectation.getValue().get()));
            assertEquals(200, stored.status(), expectation.getKey() + " " + stored.body());
            assertEquals(expected, stored.body(), expectation.getKey());
        }
        assertTrue(http.post("/api/v1/buildings/indigoBJ/find-route", route).body().path("totalCostSeconds").asDouble() > 0);
        assertFalse(http.post("/api/v1/buildings/indigoBJ/nodes/query", none).body().path("nodes").isEmpty());

        // trent: the same outcome (success or the same structured error) for the same sources and parameters.
        ObjectNode trentRequest = MAPPER.createObjectNode();
        trentRequest.putObject("userParams").put("haveStaffCard", false).put("timeOfDay", 36_000);
        Http.Response stored = http.post("/api/v1/buildings/trent/nodes/query", trentRequest);
        int expectedStatus;
        JsonNode expectedBody;
        try {
            expectedBody = MAPPER.readTree(MAPPER.writeValueAsString(topology.query("trent", "nodes", trentRequest)));
            expectedStatus = 200;
        } catch (ApiException error) {
            expectedStatus = error.status();
            expectedBody = MAPPER.valueToTree(error.body());
        }
        assertEquals(expectedStatus, stored.status());
        assertEquals(expectedBody, stored.body());
    }

    @Test
    void computationFollowsTheCurrentStoredContentAndParameters() throws Exception {
        Map<String, String> sources = new HashMap<>(Map.of(
                "configuration", "building-includes {\n submap Lobby\n}\n",
                "root", "root Building(allowed: Bool) {\n constraint Access { require allowed }\n}\n",
                "Lobby", "topo-map Lobby() {\n let params = {area = 1}\n topo-node entrance\n topo-node desk\n"
                        + " atomic-path [entrance -> desk] {cost = 3} requires Access\n}\n"));
        String id = create("Cached", sources);
        ObjectNode allowed = MAPPER.createObjectNode();
        allowed.putObject("userParams").put("allowed", true);
        ObjectNode denied = MAPPER.createObjectNode();
        denied.putObject("userParams").put("allowed", false);
        assertEquals(1, http.post("/api/v1/buildings/Cached/edges/query", allowed).body().path("edges").size());
        // A different compilation context is isolated from the cached result for the first one.
        assertEquals(0, http.post("/api/v1/buildings/Cached/edges/query", denied).body().path("edges").size());
        assertEquals(1, http.post("/api/v1/buildings/Cached/edges/query", allowed).body().path("edges").size());

        sources.put("Lobby", sources.get("Lobby").replace(" topo-node desk\n", " topo-node desk\n topo-node exit\n"));
        assertEquals(200, http.put("/api/v1/projects/" + id, replacement("Cached", sources)).status());
        assertTrue(http.post("/api/v1/buildings/Cached/nodes/query", allowed).body().path("nodes").toString().contains("Lobby::exit"));

        Map<String, String> invalid = new HashMap<>(sources);
        invalid.put("Lobby", "topo-map Lobby() {\n");
        assertEquals(200, http.put("/api/v1/projects/" + id, replacement("Cached", invalid)).status());
        Http.Response broken = http.post("/api/v1/buildings/Cached/nodes/query", allowed);
        assertEquals(500, broken.status());
        assertEquals("INVALID_BUILDING", broken.code());

        assertEquals(200, http.put("/api/v1/projects/" + id, replacement("Cached", sources)).status());
        assertEquals(200, http.post("/api/v1/buildings/Cached/nodes/query", allowed).status());
        assertEquals(204, http.delete("/api/v1/projects/" + id).status());
        assertEquals("BUILDING_NOT_FOUND", http.post("/api/v1/buildings/Cached/nodes/query", allowed).code());
        assertFalse(http.get("/api/v1/buildings").body().path("buildings").toString().contains("Cached"));
    }

    @Test
    void runsWithoutAnyGitHubIntegrationOrPlatformAccounts() {
        for (String retired : List.of("org.springframework.security.oauth2.client.registration.ClientRegistrationRepository",
                "io.jsonwebtoken.Jwts", "okhttp3.OkHttpClient", "org.bouncycastle.openssl.PEMParser", "org.h2.Driver")) {
            assertThrows(ClassNotFoundException.class, () -> Class.forName(retired), retired);
        }
        assertTrue(java.util.Arrays.stream(context.getBeanDefinitionNames())
                .noneMatch(name -> name.toLowerCase().contains("github") || name.toLowerCase().contains("jwt")));
        assertEquals(404, http.get("/auth/github").status());
        assertEquals(404, http.get("/api/me").status());
        assertEquals(404, http.get("/api/maps").status());
        assertTrue(http.get("/auth/github").raw().headers().firstValue("Location").isEmpty());
        assertEquals(200, new Http(port, null).get("/api/v1/health").status());
    }

    /** Serves one already-loaded snapshot, modeling a request that obtained its sources before a mutation. */
    private record SnapshotOnlyStore(ProjectSnapshot snapshot) implements ProjectStore {
        public List<String> eligibleBuildingIds() { return List.of(snapshot.buildingId()); }
        public ProjectPage list(int page, int size) { throw new UnsupportedOperationException(); }
        public Optional<ProjectSnapshot> findById(UUID id) { return Optional.of(snapshot); }
        public Optional<ProjectSnapshot> findByBuildingId(String buildingId) { return Optional.of(snapshot); }
        public ProjectSummary create(String buildingId, String name, Map<String, String> files) { throw new UnsupportedOperationException(); }
        public Optional<ProjectSummary> replace(UUID id, String name, Map<String, String> files) { throw new UnsupportedOperationException(); }
        public boolean delete(UUID id) { throw new UnsupportedOperationException(); }
    }
}
