package com.e611.toponavi.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TopoControllerHealthTest {

    @Test
    void returnsStableLivenessPayload() {
        var response = new TopoController().health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of(
                "status", "ok",
                "service", "toponavi-web"
        ), response.getBody());
    }

    @Test
    void resolvesExampleDirectoryWithoutDependingOnFilesystemCaseSensitivity(@TempDir Path examplesRoot)
            throws IOException {
        Path expected = Files.createDirectory(examplesRoot.resolve("indigoBJ"));

        assertEquals(expected, TopoController.resolveExampleDirectory(examplesRoot, "indigobj"));
        assertEquals(expected, TopoController.resolveExampleDirectory(examplesRoot, "IndigoBJ"));
    }

    @Test
    void doesNotResolvePathsOutsideTheExamplesRoot(@TempDir Path examplesRoot) throws IOException {
        Files.createDirectory(examplesRoot.resolve("indigoBJ"));

        assertNull(TopoController.resolveExampleDirectory(examplesRoot, "../indigoBJ"));
    }
}
