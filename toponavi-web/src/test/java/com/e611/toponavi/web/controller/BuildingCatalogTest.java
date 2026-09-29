package com.e611.toponavi.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildingCatalogTest {

    @Test
    void advertisesOnlyInstalledMapProjectsAndPreservesDirectoryCase(@TempDir Path root)
            throws IOException, ReflectiveOperationException {
        Path mall = Files.createDirectory(root.resolve("indigoBJ"));
        Path custom = Files.createDirectory(root.resolve("DemoTower"));
        Files.writeString(mall.resolve("configuration"), "building-includes {}");
        Files.writeString(custom.resolve("configuration.tcfg"), "building-includes {}");
        Files.createDirectory(root.resolve("empty"));
        Files.writeString(root.resolve("README.md"), "Maps");
        Path notes = Files.createDirectory(root.resolve("notes"));
        Files.createDirectory(notes.resolve("configuration"));
        TopoController controller = controller(root);

        assertEquals(Map.of("status", "success", "buildings", List.of("DemoTower", "indigoBJ")),
                controller.availableBuildings().getBody());
        Files.delete(custom.resolve("configuration.tcfg"));
        assertEquals(List.of("indigoBJ"), TopoController.availableBuildingNames(root));
    }

    @Test
    void emptyInventoryIsDifferentFromAnUnreadableInventory(@TempDir Path root)
            throws ReflectiveOperationException {
        assertEquals(Map.of("status", "success", "buildings", List.of()),
                controller(root).availableBuildings().getBody());
        var response = controller(root.resolve("missing")).availableBuildings();

        assertEquals(503, response.getStatusCode().value());
        assertEquals("BUILDING_CATALOG_UNAVAILABLE", ((Map<?, ?>) response.getBody()).get("code"));
    }

    private TopoController controller(Path root) throws ReflectiveOperationException {
        TopoController controller = new TopoController();
        var field = TopoController.class.getDeclaredField("examplesPathConfig");
        field.setAccessible(true);
        field.set(controller, root.toString());
        return controller;
    }
}
