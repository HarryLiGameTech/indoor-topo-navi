package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.auth.*;
import com.e611.toponavi.web.project.*;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TopoController.class, ProjectController.class})
@Import({SecurityConfig.class, ApiAccessPolicy.class, ApiCorsConfiguration.class,
        com.e611.toponavi.web.api.ApiWebConfiguration.class, ProjectRequestReader.class, ProjectLimits.class,
        ProjectStorageValidator.class, ProjectApplicationService.class, ApiSecurityTest.Store.class})
@TestPropertySource(properties = {"platform.api.allow-cors=true", "platform.api.cors.allowed-origins=https://client.example",
        "platform.api.anonymous-buildings=swfc", "platform.api.authoring.allow-guests=false",
        "platform.security.service-token=" + ApiSecurityTest.TOKEN})
class ApiSecurityTest {
    static final String TOKEN = "test-service-token_0123456789-abcdefABCDEF";

    @TestConfiguration
    static class Store {
        @Bean InMemoryProjectStore projectStore() { return new InMemoryProjectStore(); }
    }

    @Autowired MockMvc mvc;
    @Autowired InMemoryProjectStore store;
    @MockBean BuildingService buildings;
    @MockBean TopologyQueryService topology;
    @MockBean NavigationApiService navigation;
    @MockBean AuthoringApiService authoring;

    private static RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    private static RequestPostProcessor service() {
        return bearer(TOKEN);
    }

    @BeforeEach
    void storeOneProject() {
        store.clear();
        store.put("swfc", Map.of("configuration", ""));
    }

    @Test
    void authoringRequiresLoginAndDemoAccessIsExplicitlyScoped() throws Exception {
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/authoring/validate").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        mvc.perform(post("/api/v1/buildings/private/nodes/query").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/buildings/swfc/nodes/query").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/buildings/swfc/nodes/query?forceRecompile=true").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mvc.perform(post("/api/v1/buildings/private/nodes/query").with(service()).contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/authoring/validate").with(service()).contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        when(buildings.buildings()).thenReturn(List.of("private", "swfc"));
        mvc.perform(get("/api/v1/buildings")).andExpect(jsonPath("$.buildings.length()").value(1))
                .andExpect(jsonPath("$.buildings[0]").value("swfc"));
        mvc.perform(get("/api/v1/buildings").with(service())).andExpect(jsonPath("$.buildings.length()").value(2));
    }

    @Test
    void invalidCredentialsEstablishNoAuthenticationButPublicRoutesStillRunAnonymously() throws Exception {
        for (String token : List.of("wrong-token-0123456789-0123456789-0123", TOKEN + "x", TOKEN.substring(1), "")) {
            mvc.perform(post("/api/v1/buildings/private/nodes/query").with(bearer(token)).contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
            mvc.perform(post("/api/v1/buildings/swfc/nodes/query").with(bearer(token)).contentType("application/json").content("{}"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/projects").with(bearer(token))).andExpect(status().isUnauthorized());
        }
        // Other schemes are not service credentials.
        mvc.perform(get("/api/v1/projects").header("Authorization", "Basic " + TOKEN)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/projects").header("Authorization", TOKEN)).andExpect(status().isUnauthorized());
        // The scheme name is case-insensitive.
        mvc.perform(get("/api/v1/projects").header("Authorization", "bearer " + TOKEN)).andExpect(status().isOk());
        when(buildings.buildings()).thenReturn(List.of("private", "swfc"));
        mvc.perform(get("/api/v1/buildings").with(bearer("wrong-token-0123456789-0123456789-0123")))
                .andExpect(jsonPath("$.buildings.length()").value(1));
    }

    @Test
    void projectCrudAndSourcesAlwaysRequireTheServiceCredential() throws Exception {
        String id = store.findByBuildingId("swfc").orElseThrow().summary().id().toString();
        // swfc is anonymously computable, but its sources are still protected.
        mvc.perform(get("/api/v1/projects/by-building-id/swfc")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        mvc.perform(get("/api/v1/projects/" + id)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/projects")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/projects").contentType("application/json")
                .content("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{}}")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/projects/" + id).contentType("application/json")
                .content("{\"name\":\"x\",\"files\":{}}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/projects/" + id)).andExpect(status().isUnauthorized());
        assertEquals(List.of("swfc"), store.buildingIds());

        mvc.perform(get("/api/v1/projects/by-building-id/swfc").with(service())).andExpect(status().isOk())
                .andExpect(jsonPath("$.project.files.configuration").value(""));
        mvc.perform(post("/api/v1/projects").with(service()).contentType("application/json")
                        .content("{\"buildingId\":\"x\",\"name\":\"x\",\"files\":{}}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"));
        mvc.perform(delete("/api/v1/projects/" + id).with(service())).andExpect(status().isNoContent());
    }

    @Test
    void isStatelessWithoutLoginRedirectsAndRetiresPlatformRoutes() throws Exception {
        var response = mvc.perform(get("/api/v1/projects").with(service())).andExpect(status().isOk()).andReturn().getResponse();
        assertNull(response.getHeader("Set-Cookie"));
        var denied = mvc.perform(get("/api/v1/projects")).andExpect(status().isUnauthorized()).andReturn();
        assertNull(denied.getResponse().getHeader("Location"));
        assertNull(denied.getRequest().getSession(false));
        for (String retired : List.of("/auth/github", "/api/me", "/api/maps", "/api/maps/123", "/oauth2/authorization/github",
                "/login/oauth2/code/github", "/login")) {
            mvc.perform(get(retired).with(service())).andExpect(status().isNotFound());
            mvc.perform(get(retired)).andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/maps").with(service()).contentType("application/json").content("{}")).andExpect(status().isNotFound());
        mvc.perform(post("/logout").with(service())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/unknown").with(service())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/unknown")).andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightDoesNotAuthenticateTheActualRequest() throws Exception {
        mvc.perform(options("/api/v1/authoring/validate").header("Origin", "https://client.example")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "https://client.example"));
        mvc.perform(post("/api/v1/authoring/validate").header("Origin", "https://client.example").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(options("/api/v1/authoring/validate").header("Origin", "https://other.example")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }

    @Test
    void corsPermitsProjectMutationsButPreflightGrantsNoAccess() throws Exception {
        String id = store.findByBuildingId("swfc").orElseThrow().summary().id().toString();
        for (String method : List.of("PUT", "DELETE")) {
            mvc.perform(options("/api/v1/projects/" + id).header("Origin", "https://client.example")
                            .header("Access-Control-Request-Method", method).header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                    .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "https://client.example"));
        }
        mvc.perform(put("/api/v1/projects/" + id).header("Origin", "https://client.example").contentType("application/json")
                .content("{\"name\":\"x\",\"files\":{}}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/projects/" + id).header("Origin", "https://client.example")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/projects").with(service()).header("Origin", "https://client.example").contentType("application/json")
                        .content("{\"buildingId\":\"Lobby\",\"name\":\"Lobby\",\"files\":{}}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("Location")));
    }

    @Test
    void validatesCorsConfigurationAndGuestToggle() {
        var cors = new ApiCorsConfiguration();
        assertThrows(IllegalArgumentException.class, () -> cors.apiCorsSource(true, "", "POST", "Content-Type", false));
        assertThrows(IllegalArgumentException.class, () -> cors.apiCorsSource(true, "*", "POST", "Content-Type", true));
        assertTrue(cors.apiCorsSource(false, "", "POST", "Content-Type", false).getCorsConfigurations().isEmpty());
        assertFalse(new ApiAccessPolicy("", false).authoring(null));
        assertTrue(new ApiAccessPolicy("", true).authoring(null));
    }

    @Test
    void requiresAStrongConfiguredServiceToken() {
        for (String invalid : new String[]{null, "", "   ", "short-token", "a".repeat(31), "contains spaces in the middle 0123456789",
                "contains-a-quote\"-0123456789-0123456789", "=leading-padding-0123456789-0123456789"}) {
            assertThrows(IllegalStateException.class, () -> new ServiceTokenAuthFilter(invalid), String.valueOf(invalid));
        }
        assertDoesNotThrow(() -> new ServiceTokenAuthFilter("a".repeat(32)));
        assertDoesNotThrow(() -> new ServiceTokenAuthFilter("q0b4X8m2-Yt_LkPp3sV9cW1nZ7rD5eHuJ6fG0aBx4Q="));
    }
}
