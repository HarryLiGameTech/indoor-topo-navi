package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.auth.*;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.repository.UserRepository;
import com.e611.toponavi.web.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TopoController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class)
@Import({SecurityConfig.class, ApiAccessPolicy.class, ApiCorsConfiguration.class, JwtAuthFilter.class,
        com.e611.toponavi.web.api.ApiWebConfiguration.class})
@TestPropertySource(properties = {"platform.api.allow-cors=true", "platform.api.cors.allowed-origins=https://client.example",
        "platform.api.anonymous-buildings=swfc", "platform.api.authoring.allow-guests=false"})
class ApiSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean BuildingService buildings;
    @MockBean TopologyQueryService topology;
    @MockBean NavigationApiService navigation;
    @MockBean AuthoringApiService authoring;
    @MockBean JwtService jwt;
    @MockBean UserRepository users;
    @MockBean GitHubOAuthSuccessHandler oauth;
    @MockBean ClientRegistrationRepository clients;

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
        mvc.perform(post("/api/v1/buildings/private/nodes/query").with(user("contributor")).contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/authoring/validate").with(user("contributor")).contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        when(buildings.buildings()).thenReturn(List.of("private", "swfc"));
        mvc.perform(get("/api/v1/buildings")).andExpect(jsonPath("$.buildings.length()").value(1))
                .andExpect(jsonPath("$.buildings[0]").value("swfc"));
        mvc.perform(get("/api/v1/buildings").with(user("contributor"))).andExpect(jsonPath("$.buildings.length()").value(2));
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
    void validatesCorsConfigurationAndGuestToggle() {
        var cors = new ApiCorsConfiguration();
        assertThrows(IllegalArgumentException.class, () -> cors.apiCorsSource(true, "", "POST", "Content-Type", false));
        assertThrows(IllegalArgumentException.class, () -> cors.apiCorsSource(true, "*", "POST", "Content-Type", true));
        assertTrue(cors.apiCorsSource(false, "", "POST", "Content-Type", false).getCorsConfigurations().isEmpty());
        assertFalse(new ApiAccessPolicy("", false).authoring(null));
        assertTrue(new ApiAccessPolicy("", true).authoring(null));
    }
}
