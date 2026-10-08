package com.e611.toponavi.web.service;

import api.NavigationRequestException;
import api.TopoNaviService;
import api.TopologyData;
import navigation.CustomRouteObjective;
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
        RequestFields.object(preference, Set.of("routePlanningPreference", "banTags", "minimizeTag", "maximizeTag", "riskPreference", "customObjective"), "/traversalPreference");
        if (preference.has("minimizeTag") && preference.has("maximizeTag"))
            throw new ApiException(400, "CONFLICTING_TAG_PREFERENCES", "minimizeTag and maximizeTag cannot be combined.");
        if (preference.has("maximizeTag")) throw new ApiException(422, "UNSUPPORTED_TRAVERSAL_PREFERENCE", "maximizeTag remains deferred.");
        String planning = choice(preference, "routePlanningPreference", "MinimizeTime", Set.of("MinimizeTime", "MinimizeTransfers", "MinimizePhysicalDemands", "MinimizeCustomData"));
        CustomRouteObjective custom = customObjective(preference, planning);
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
                    banTags, mode.equals("partiallyInformed"), risk, minimizeTag, custom);
            var plan = planned.plan();
            ObjectNode response = mapper.createObjectNode().put("status", "success")
                    .put("path", plan.prettyPrint()).put("totalCostSeconds", plan.totalCost()).put("routingMode", mode);
            response.set("steps", mapper.valueToTree(plan.toStructuredSteps()));
            response.set("deprecations", mapper.valueToTree(RouteResponseDeprecationSpec.activeDeprecations()));
            ObjectNode applied = response.putObject("appliedTraversalPreference").put("routePlanningPreference", planning)
                    .put("riskPreference", planned.appliedRiskPreference());
            applied.set("banTags", mapper.valueToTree(banTags));
            if (minimizeTag != null) applied.put("minimizeTag", minimizeTag);
            if (custom != null) {
                ObjectNode definition = preference.path("customObjective").deepCopy();
                definition.set("tieBreakers", mapper.valueToTree(CollectionConverters.asJava(custom.tieBreakers()).stream()
                        .map(Object::toString).toList()));
                if (definition.path("kind").asText().equals("edgeAttribute") && !definition.has("missingValue"))
                    definition.put("missingValue", 0);
                applied.set("customObjective", definition);
                response.put("customCost", custom.cost().total(plan));
            }
            response.put("transferCount", plan.transferCount()).put("physicalDemand", plan.physicalDemandScore());
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

    private CustomRouteObjective customObjective(JsonNode preference, String planning) {
        String path = "/traversalPreference/customObjective";
        boolean custom = planning.equals("MinimizeCustomData");
        if (custom != preference.has("customObjective"))
            throw RequestFields.invalid(path, "customObjective is required only for MinimizeCustomData.");
        if (!custom) return null;
        if (preference.has("minimizeTag"))
            throw RequestFields.invalid(path, "MinimizeCustomData cannot be combined with minimizeTag.");
        JsonNode definition = RequestFields.optionalObject(preference, "customObjective");
        String kind = RequestFields.text(definition, "kind", null);
        if (!Set.of("tagExposure", "edgeAttribute").contains(kind == null ? "" : kind))
            throw RequestFields.invalid(path + "/kind", "Use tagExposure or edgeAttribute.");
        boolean tag = kind.equals("tagExposure");
        RequestFields.object(definition, tag ? Set.of("kind", "tag", "tieBreakers")
                : Set.of("kind", "attribute", "missingValue", "tieBreakers"), path);
        String field = RequestFields.text(definition, tag ? "tag" : "attribute", null);
        if (field == null) throw RequestFields.invalid(path, "The custom tag or attribute is required.");
        double missing = 0;
        if (definition.has("missingValue")) {
            JsonNode value = definition.get("missingValue");
            if (!value.isNumber() || !Double.isFinite(value.doubleValue()) || value.doubleValue() < 0)
                throw RequestFields.invalid(path + "/missingValue", "Expected a finite, non-negative number.");
            missing = value.doubleValue();
        }
        List<String> order = RequestFields.strings(definition, "tieBreakers");
        if (order != null && (order.size() != 2 || !Set.copyOf(order).equals(Set.of("MinimizeTransfers", "MinimizeTime"))))
            throw RequestFields.invalid(path + "/tieBreakers", "Include MinimizeTransfers and MinimizeTime exactly once.");
        return TopoNaviService.customRouteObjective(kind, field, missing, order);
    }

    private String choice(JsonNode body, String key, String fallback, Set<String> choices) {
        String value = RequestFields.text(body, key, fallback);
        if (!choices.contains(value)) throw RequestFields.invalid("/" + key, "Unsupported " + key + ".");
        return value;
    }
}
