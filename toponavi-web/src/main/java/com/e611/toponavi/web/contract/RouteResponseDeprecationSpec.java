package com.e611.toponavi.web.contract;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Machine-readable deprecation metadata for route response fields. */
public final class RouteResponseDeprecationSpec {

    private RouteResponseDeprecationSpec() {}

    public static List<Map<String, Object>> activeDeprecations() {
        return List.of(namedWaypoints());
    }

    private static Map<String, Object> namedWaypoints() {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("field", "steps[].namedWaypoints");
        spec.put("status", "to-be-deprecated");
        spec.put("replacement", "steps[].waypoints");
        spec.put("since", "2026-07-25");
        spec.put("removalVersion", null);
        spec.put(
                "message",
                "namedWaypoints is a legacy human-readable string list. Use waypoints for structured node metadata."
        );
        return spec;
    }
}
