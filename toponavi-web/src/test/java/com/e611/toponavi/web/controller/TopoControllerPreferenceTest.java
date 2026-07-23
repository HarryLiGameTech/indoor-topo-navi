package com.e611.toponavi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.e611.toponavi.web.dto.QuickDemoNavigationRequest;
import com.e611.toponavi.web.dto.TraversalPreferenceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

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
                List.of("banTags", "minimizeTag", "maximizeTag", "riskPreference"),
                TopoController.unsupportedTraversalFields(body)
        );
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
    void endpointRejectsUnsupportedTagPreferenceExplicitly() {
        QuickDemoNavigationRequest body = requestWithPreference("MinimizeTime");
        body.traversalPreference.banTags = List.of("outdoor");

        ResponseEntity<?> response = new TopoController().quickDemoNavigationPost(
                "swfc", "LowerLobby::gate_1", "LowerLobby::cafe", null, null, body);

        assertEquals(501, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("TRAVERSAL_PREFERENCE_NOT_IMPLEMENTED", responseBody.get("code"));
        assertEquals(List.of("banTags"), responseBody.get("unsupportedFields"));
    }

    private QuickDemoNavigationRequest requestWithPreference(String preference) {
        QuickDemoNavigationRequest body = new QuickDemoNavigationRequest();
        body.traversalPreference = new TraversalPreferenceRequest();
        body.traversalPreference.routePlanningPreference = preference;
        return body;
    }
}
