package com.e611.toponavi.web.service;

import api.NavigationRequestException;
import api.TopoNaviService;
import api.TopologyData;
import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.api.RequestFields;
import com.e611.toponavi.web.contract.RouteResponseDeprecationSpec;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class NavigationApiService {
    private final BuildingService buildings;
    private final ObjectMapper mapper;

    public NavigationApiService(BuildingService buildings, ObjectMapper mapper) {
        this.buildings = buildings;
        this.mapper = mapper;
    }

    public ObjectNode findRoute(String buildingId, JsonNode body) {
        RequestFields.object(body, Set.of("startNode", "endNode", "userParams", "traversalPreference", "routingMode"), "");
        String start = RequestFields.qualified(RequestFields.text(body, "startNode", null), "/startNode");
        String end = RequestFields.qualified(RequestFields.text(body, "endNode", null), "/endNode");
        JsonNode preference = RequestFields.optionalObject(body, "traversalPreference");
        RequestFields.object(preference, Set.of("routePlanningPreference", "banTags", "minimizeTag", "maximizeTag", "riskPreference"), "/traversalPreference");
        if (preference.has("minimizeTag") && preference.has("maximizeTag"))
            throw new ApiException(400, "CONFLICTING_TAG_PREFERENCES", "minimizeTag and maximizeTag cannot be combined.");
        if (preference.has("maximizeTag")) throw new ApiException(422, "UNSUPPORTED_TRAVERSAL_PREFERENCE", "maximizeTag remains deferred.");
        String planning = choice(preference, "routePlanningPreference", "MinimizeTime", Set.of("MinimizeTime", "MinimizeTransfers", "MinimizePhysicalDemands"));
        String risk = choice(preference, "riskPreference", "conservative", Set.of("conservative", "permissive", "aggressive"));
        String mode = choice(body, "routingMode", "partiallyInformed", Set.of("fullyInformed", "partiallyInformed"));
        String minimizeTag = RequestFields.text(preference, "minimizeTag", null);
        List<String> requestedTags = RequestFields.strings(preference, "banTags");
        List<String> banTags = requestedTags == null ? List.of() : requestedTags.stream().distinct().toList();
        var result = buildings.compile(buildingId, RequestFields.optionalObject(body, "userParams"));
        List<JsonNode> nodes = new ArrayList<>();
        mapper.<JsonNode>valueToTree(TopologyData.nodes(result)).forEach(nodes::add);
        TopologyQueryService.resolveNode(nodes, start, null);
        TopologyQueryService.resolveNode(nodes, end, null);
        try {
            var planned = TopoNaviService.findRoutePlanWithRiskPreference(result, start, end, planning,
                    banTags, mode.equals("partiallyInformed"), risk, minimizeTag);
            var plan = planned.plan();
            ObjectNode response = mapper.createObjectNode().put("status", "success")
                    .put("path", plan.prettyPrint()).put("totalCostSeconds", plan.totalCost()).put("routingMode", mode);
            response.set("steps", mapper.valueToTree(plan.toStructuredSteps()));
            response.set("deprecations", mapper.valueToTree(RouteResponseDeprecationSpec.activeDeprecations()));
            ObjectNode applied = response.putObject("appliedTraversalPreference").put("routePlanningPreference", planning)
                    .put("riskPreference", planned.appliedRiskPreference());
            applied.set("banTags", mapper.valueToTree(banTags));
            if (minimizeTag != null) applied.put("minimizeTag", minimizeTag);
            if (plan.uncertainAccess().isDefined()) {
                ObjectNode summary = response.putObject("riskSummary").put("routeNotNecessarilyAvailable", true)
                        .put("appliedRiskPreference", planned.appliedRiskPreference())
                        .put("disclaimer", "These uncertainty notes are provided by the map creator. They may be inaccurate or outdated; follow on-site rules and ask local staff if access does not work.");
                var reasons = summary.putArray("uncertaintyReasons");
                CollectionConverters.asJava(plan.uncertainAccess().get().uncertaintyReasons()).forEach(reason ->
                        reasons.addObject().put("type", reason.conditionType())
                                .put("reason", reason.reason().isDefined() ? reason.reason().get() : null));
            }
            return response;
        } catch (NavigationRequestException error) {
            throw new ApiException(error.getCode().equals("TRAVERSAL_PREFERENCE_SEARCH_LIMIT") ? 503 : 422,
                    error.getCode(), error.getMessage(), error.getDetails());
        }
    }

    private String choice(JsonNode body, String key, String fallback, Set<String> choices) {
        String value = RequestFields.text(body, key, fallback);
        if (!choices.contains(value)) throw RequestFields.invalid("/" + key, "Unsupported " + key + ".");
        return value;
    }
}
