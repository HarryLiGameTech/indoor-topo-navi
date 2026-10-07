package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.service.BuildingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BuildingCatalogTest {
    @TempDir Path root;

    @Test
    void advertisesInstalledProjectsAndRequiresCanonicalCase() throws Exception {
        Files.createDirectories(root.resolve("indigoBJ"));
        Files.writeString(root.resolve("indigoBJ/configuration"), "building-includes {}");
        Files.createDirectories(root.resolve("empty"));
        BuildingService service = new BuildingService(root.toString(), null, new ObjectMapper());
        assertEquals(List.of("indigoBJ"), service.buildings());
        assertEquals(404, assertThrows(ApiException.class, () -> service.files("indigobj")).status());
        assertEquals(404, assertThrows(ApiException.class, () -> service.files("../indigoBJ")).status());
        assertTrue(service.files("indigoBJ").containsKey("configuration"));
    }

    @Test
    void distinguishesEmptyAndUnavailableInventories() {
        assertEquals(List.of(), new BuildingService(root.toString(), null, new ObjectMapper()).buildings());
        assertEquals(503, assertThrows(ApiException.class, () ->
                new BuildingService(root.resolve("missing").toString(), null, new ObjectMapper()).buildings()).status());
    }
}
