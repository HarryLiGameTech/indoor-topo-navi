package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.AuthoringApiService;
import com.e611.toponavi.web.service.BuildingService;
import com.e611.toponavi.web.service.NavigationApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.e611.toponavi.web.auth.ApiAccessPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class TopoController {
    private final BuildingService buildings;
    private final TopologyQueryService topology;
    private final NavigationApiService navigation;
    private final AuthoringApiService authoring;
    private final ApiAccessPolicy access;

    public TopoController(BuildingService buildings, TopologyQueryService topology,
                          NavigationApiService navigation, AuthoringApiService authoring, ApiAccessPolicy access) {
        this.buildings = buildings;
        this.topology = topology;
        this.navigation = navigation;
        this.authoring = authoring;
        this.access = access;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "toponavi-web");
    }

    @GetMapping("/buildings")
    public Map<String, ?> buildings(Authentication authentication) {
        return Map.of("status", "success", "buildings", access.visibleBuildings(authentication, buildings.buildings()));
    }

    @PostMapping(value = "/buildings/{buildingId}/find-route", consumes = "application/json")
    public JsonNode findRoute(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return navigation.findRoute(buildingId, body);
    }

    @PostMapping(value = "/buildings/{buildingId}/maps/query", consumes = "application/json")
    public JsonNode maps(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return topology.query(buildingId, "maps", body);
    }

    @PostMapping(value = "/buildings/{buildingId}/nodes/query", consumes = "application/json")
    public JsonNode nodes(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return topology.query(buildingId, "nodes", body);
    }

    @PostMapping(value = "/buildings/{buildingId}/edges/query", consumes = "application/json")
    public JsonNode edges(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return topology.query(buildingId, "edges", body);
    }

    @PostMapping(value = "/buildings/{buildingId}/nodes/neighbors", consumes = "application/json")
    public JsonNode neighbors(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return topology.query(buildingId, "neighbors", body);
    }

    @PostMapping(value = "/buildings/{buildingId}/transports/query", consumes = "application/json")
    public JsonNode transports(@PathVariable String buildingId, @RequestBody JsonNode body) {
        return topology.query(buildingId, "transports", body);
    }

    @PostMapping(value = "/authoring/validate", consumes = "application/json")
    public ResponseEntity<?> validate(@RequestBody JsonNode body) {
        return authoring.validate(body);
    }
}
