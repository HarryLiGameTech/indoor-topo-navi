package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.project.InMemoryProjectStore;
import com.e611.toponavi.web.service.BuildingService;
import com.e611.toponavi.web.service.CompilationParameterResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCatalogTest {
    @TempDir Path cacheDir;
    private final InMemoryProjectStore store = new InMemoryProjectStore();
    private final ObjectMapper mapper = new ObjectMapper();

    private BuildingService service() {
        return new BuildingService(store, new CompilationCacheService(cacheDir), new CompilationParameterResolver(), mapper);
    }

    private static ApiException error(Runnable call) {
        return assertThrows(ApiException.class, call::run);
    }

    private static String code(ApiException error) {
        return ((Map<?, ?>) error.body().get("error")).get("code").toString();
    }

    @Test
    void advertisesInstalledProjectsAndRequiresCanonicalCase() {
        store.put("indigoBJ", Map.of("configuration", "building-includes {}"));
        store.put("empty", Map.of());
        BuildingService service = service();
        assertEquals(List.of("indigoBJ"), service.buildings());
        assertEquals(404, error(() -> service.files("indigobj")).status());
        assertEquals(404, error(() -> service.files("../indigoBJ")).status());
        assertTrue(service.files("indigoBJ").containsKey("configuration"));
    }

    @Test
    void distinguishesEmptyAndUnavailableInventories() {
        assertEquals(List.of(), service().buildings());
        store.setUnavailable(true);
        ApiException catalog = error(() -> service().buildings());
        assertEquals(503, catalog.status());
        assertEquals("BUILDING_CATALOG_UNAVAILABLE", code(catalog));
        ApiException building = error(() -> service().files("indigoBJ"));
        assertEquals(503, building.status());
        assertEquals("BUILDING_UNAVAILABLE", code(building));
    }

    @Test
    void eligibilityIsConfigurationPresenceNotCompilability() {
        store.put("noConfig", Map.of("root", "root R() {}", "Lobby.tmap", "topo-map Lobby() {\n topo-node a\n}\n"));
        store.put("tcfg", Map.of("configuration.tcfg", "building-includes {}"));
        store.put("emptyConfig", Map.of("configuration", ""));
        store.put("broken", Map.of("configuration", "building-includes {", "root", "root ("));
        store.put("nested", Map.of("floors/configuration", "building-includes {}"));
        BuildingService service = service();
        assertEquals(List.of("broken", "emptyConfig", "tcfg"), service.buildings());
        assertEquals("BUILDING_NOT_FOUND", code(error(() -> service.files("noConfig"))));
        assertEquals("BUILDING_NOT_FOUND", code(error(() -> service.files("nested"))));
        assertEquals("BUILDING_NOT_FOUND", code(error(() -> service.files("absent"))));
        ApiException invalid = error(() -> service.compile("broken", mapper.createObjectNode()));
        assertEquals(500, invalid.status());
        assertEquals("INVALID_BUILDING", code(invalid));
        // Java string ordering: uppercase sorts before lowercase.
        store.put("Zeta", Map.of("configuration", ""));
        store.put("alpha", Map.of("configuration", ""));
        assertEquals(List.of("Zeta", "alpha", "broken", "emptyConfig", "tcfg"), service.buildings());
    }

    @Test
    void compilesTheCurrentSnapshotAndNeverServesStaleOrDeletedSources() {
        Map<String, String> valid = Map.of(
                "configuration", "building-includes {\n submap Lobby\n}\n",
                "root", "root Building(allowed: Bool) {\n constraint Access { require allowed }\n}\n",
                "Lobby", "topo-map Lobby() {\n let params = {area = 1}\n topo-node entrance\n}\n");
        var created = store.put("cached", valid);
        BuildingService service = service();
        var params = mapper.createObjectNode().put("allowed", true);
        assertEquals(List.of("Lobby"), scala.jdk.javaapi.CollectionConverters.asJava(service.compile("cached", params).graphSequence()));

        Map<String, String> changed = new java.util.HashMap<>(valid);
        changed.put("configuration", "building-includes {\n submap Lobby\n submap Annex\n}\n");
        changed.put("Annex", "topo-map Annex() {\n let params = {area = 2}\n topo-node door\n}\n");
        store.put("cached", changed);
        assertEquals(List.of("Lobby", "Annex"), scala.jdk.javaapi.CollectionConverters.asJava(service.compile("cached", params).graphSequence()));

        // A different compilation context gets its own cache identity and still validates parameter types.
        ApiException wrongType = error(() -> service.compile("cached", mapper.createObjectNode().put("allowed", 1)));
        assertEquals("INVALID_USER_PARAMS", code(wrongType));

        Map<String, String> invalid = new java.util.HashMap<>(changed);
        invalid.put("Annex", "topo-map Annex() {\n");
        store.put("cached", invalid);
        assertEquals("INVALID_BUILDING", code(error(() -> service.compile("cached", params))));

        store.delete(created.id());
        assertEquals("BUILDING_NOT_FOUND", code(error(() -> service.compile("cached", params))));
    }
}
