package com.e611.toponavi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.e611.toponavi.web.dto.QuickDemoNavigationRequest;
import com.e611.toponavi.web.dto.TraversalPreferenceRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopoControllerPreferenceTest {

    @Test
    void bodyPreferenceOverridesLegacyQueryParameter() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTransfers");

        assertEquals(
                "MinimizeTransfers",
                TopoController.resolveRoutePlanningPreference("MinimizeTime", body)
        );
    }

    @Test
    void queryParameterRemainsAsFallback() {
        assertEquals(
                "MinimizePhysicalDemands",
                TopoController.resolveRoutePlanningPreference("MinimizePhysicalDemands", null)
        );
    }

    @Test
    void missingPreferenceDefaultsToMinimizeTime() {
        assertEquals("MinimizeTime", TopoController.resolveRoutePlanningPreference(null, null));
    }

    @Test
    void invalidPreferenceIsRejectedInsteadOfSilentlyDefaulted() {
        QuickDemoNavigationRequest body = requestWithPreference("FastestMaybe");

        assertThrows(
                IllegalArgumentException.class,
                () -> TopoController.resolveRoutePlanningPreference(null, body)
        );
    }

    @Test
    void unsupportedRuntimePreferenceFieldsAreReported() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
        body.traversalPreference.banTags = List.of("outdoor");
        body.traversalPreference.minimizeTag = "odor_prone";
        body.traversalPreference.maximizeTag = "shop";
        body.traversalPreference.riskPreference = "conservative";

        assertEquals(
                List.of("minimizeTag", "maximizeTag"),
                TopoController.unsupportedTraversalFields(body)
        );
    }

    @Test
    void riskPreferenceDefaultsToConservativeAndAcceptsAllModes() {
        assertEquals("conservative", TopoController.resolveRiskPreference(null));

        for (String riskPreference : List.of("conservative", "permissive", "aggressive")) {
            QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
            body.traversalPreference.riskPreference = riskPreference;
            assertEquals(riskPreference, TopoController.resolveRiskPreference(body));
        }
    }

    @Test
    void invalidRiskPreferenceIsRejected() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
        body.traversalPreference.riskPreference = "reckless";

        assertThrows(IllegalArgumentException.class, () -> TopoController.resolveRiskPreference(body));
    }

    @Test
    void jsonBodyBindsTraversalPreferenceAndUserParams() throws Exception {
        String json = """
                {
                  "userParams": {"haveCard": true},
                  "traversalPreference": {
                    "routePlanningPreference": "MinimizeTime",
                    "banTags": ["outdoor"]
                  }
                }
                """;

        QuickDemoNavigationRequest body = new ObjectMapper()
                .readValue(json, QuickDemoNavigationRequest.class);

        assertEquals(true, body.userParams.get("haveCard"));
        assertEquals("MinimizeTime", body.traversalPreference.routePlanningPreference);
        assertEquals(List.of("outdoor"), body.traversalPreference.banTags);
    }

    @Test
    void banTagsAreResolvedAndDeduplicated() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
        body.traversalPreference.banTags = List.of("outdoor", "rain_exposed", "outdoor");

        assertEquals(
                List.of("outdoor", "rain_exposed"),
                TopoController.resolveBanTags(body)
        );
        assertEquals(List.of(), TopoController.unsupportedTraversalFields(body));
    }

    @Test
    void blankBanTagIsRejected() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
        body.traversalPreference.banTags = List.of("outdoor", " ");

        assertThrows(IllegalArgumentException.class, () -> TopoController.resolveBanTags(body));
    }

    @Test
    void proximityNodesKeepTheCheapestDirectEdgeAndRespectAmount() {
        List<TopoController.ProximityNodeView> candidates = List.of(
                proximityNode("Floor1::b", 8),
                proximityNode("Floor1::a", 3),
                proximityNode("Floor1::b", 2),
                proximityNode("Floor1::c", 4)
        );

        List<TopoController.ProximityNodeView> nearest =
                TopoController.nearestProximityNodes(candidates, 2);

        assertEquals(List.of("Floor1::b", "Floor1::a"),
                nearest.stream().map(TopoController.ProximityNodeView::nodeIdentifier).toList());
        assertEquals(List.of(2.0, 3.0),
                nearest.stream().map(TopoController.ProximityNodeView::costSeconds).toList());
    }

    @Test
    void proximityAmountMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TopoController.nearestProximityNodes(List.of(), 0)
        );
    }

    private QuickDemoNavigationRequest requestWithPreference(String preference) {
        QuickDemoNavigationRequest body = new QuickDemoNavigationRequest();
        body.traversalPreference = new TraversalPreferenceRequest();
        body.traversalPreference.routePlanningPreference = preference;
        return body;
    }

    private TopoController.ProximityNodeView proximityNode(String nodeIdentifier, double costSeconds) {
        String[] parts = nodeIdentifier.split("::", 2);
        return new TopoController.ProximityNodeView(
                nodeIdentifier,
                parts[0],
                parts[1],
                costSeconds,
                Map.of(),
                List.of(),
                List.of()
        );
    }
}
