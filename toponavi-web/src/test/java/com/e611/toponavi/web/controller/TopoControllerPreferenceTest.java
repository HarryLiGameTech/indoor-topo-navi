package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.service.NavigationApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TopoControllerPreferenceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final NavigationApiService service = new NavigationApiService(null, mapper);

    @Test
    void validatesCustomObjectiveContractsBeforeCompilation() throws Exception {
        for (String preference : new String[]{
                "\"routePlanningPreference\":\"MinimizeCustomData\"",
                "\"customObjective\":{\"kind\":\"tagExposure\",\"tag\":\"outdoor\"}",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"customObjective\":null",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"customObjective\":{\"kind\":\"unknown\"}",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"customObjective\":{\"kind\":\"tagExposure\"}",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"minimizeTag\":\"outdoor\",\"customObjective\":{\"kind\":\"tagExposure\",\"tag\":\"outdoor\"}",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"customObjective\":{\"kind\":\"edgeAttribute\",\"attribute\":\"cost\",\"missingValue\":-1}",
                "\"routePlanningPreference\":\"MinimizeCustomData\",\"customObjective\":{\"kind\":\"tagExposure\",\"tag\":\"outdoor\",\"tieBreakers\":[\"MinimizeTime\",\"MinimizeTime\"]}"}) {
            var body = mapper.readTree("{\"startNode\":\"F::a\",\"endNode\":\"F::b\",\"traversalPreference\":{" + preference + "}}");
            assertEquals(400, assertThrows(ApiException.class, () -> service.findRoute("unused", body)).status(), preference);
        }
    }

    @Test
    void rejectsConflictingAndDeferredSoftPreferencesBeforeCompilation() throws Exception {
        var body = mapper.readTree("""
                {"startNode":"F::a","endNode":"F::b","traversalPreference":{"minimizeTag":"outdoor","maximizeTag":"shop"}}
                """);
        assertEquals(400, assertThrows(ApiException.class, () -> service.findRoute("unused", body)).status());
        ((com.fasterxml.jackson.databind.node.ObjectNode) body.get("traversalPreference")).remove("minimizeTag");
        var error = assertThrows(ApiException.class, () -> service.findRoute("unused", body));
        assertEquals(422, error.status());
        assertTrue(error.body().toString().contains("UNSUPPORTED_TRAVERSAL_PREFERENCE"));
    }

    @Test
    void rejectsInvalidPreferencesAndUnknownInputs() throws Exception {
        for (String preference : new String[]{"\"routePlanningPreference\":\"FastestMaybe\"", "\"riskPreference\":\"reckless\"",
                "\"banTags\":[\" \"]", "\"surprise\":true", "\"minimizeTag\":null"}) {
            var body = mapper.readTree("{\"startNode\":\"F::a\",\"endNode\":\"F::b\",\"traversalPreference\":{" + preference + "}}");
            assertEquals(400, assertThrows(ApiException.class, () -> service.findRoute("unused", body)).status());
        }
        for (String extra : new String[]{"\"isHighRise\":true", "\"forceRecompile\":true", "\"routingMode\":\"auto\""}) {
            var body = mapper.readTree("{\"startNode\":\"F::a\",\"endNode\":\"F::b\"," + extra + "}");
            assertEquals(400, assertThrows(ApiException.class, () -> service.findRoute("unused", body)).status());
        }
    }
}
