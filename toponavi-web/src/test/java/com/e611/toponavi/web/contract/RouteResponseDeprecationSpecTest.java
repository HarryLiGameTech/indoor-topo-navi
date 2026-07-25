package com.e611.toponavi.web.contract;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteResponseDeprecationSpecTest {

    @Test
    void namedWaypointsIsMarkedAsToBeDeprecatedWithStructuredReplacement() {
        List<Map<String, Object>> deprecations = RouteResponseDeprecationSpec.activeDeprecations();

        assertEquals(1, deprecations.size());
        Map<String, Object> namedWaypoints = deprecations.get(0);
        assertEquals("steps[].namedWaypoints", namedWaypoints.get("field"));
        assertEquals("to-be-deprecated", namedWaypoints.get("status"));
        assertEquals("steps[].waypoints", namedWaypoints.get("replacement"));
        assertEquals("2026-07-25", namedWaypoints.get("since"));
        assertNull(namedWaypoints.get("removalVersion"));
    }
}
