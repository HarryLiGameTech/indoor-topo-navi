package com.e611.toponavi.web.controller;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TopoControllerHealthTest {
    @Test
    void healthDoesNotAccessStorageOrCompiler() {
        assertEquals(Map.of("status", "ok", "service", "toponavi-web"),
                new TopoController(null, null, null, null, null).health());
    }
}
