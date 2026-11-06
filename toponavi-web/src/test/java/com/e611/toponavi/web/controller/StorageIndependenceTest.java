package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiErrors;
import com.e611.toponavi.web.auth.ApiAccessPolicy;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.project.InMemoryProjectStore;
import com.e611.toponavi.web.query.FilterParser;
import com.e611.toponavi.web.query.TopologyQueryService;
import com.e611.toponavi.web.service.AuthoringApiService;
import com.e611.toponavi.web.service.BuildingService;
import com.e611.toponavi.web.service.CompilationParameterResolver;
import com.e611.toponavi.web.service.NavigationApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Health and request-supplied validation must work without touching project storage, even when it is down. */
class StorageIndependenceTest {
    @TempDir Path cache;

    @Test
    void healthAndValidationNeverCallTheProjectStore() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InMemoryProjectStore store = new InMemoryProjectStore();
        store.setUnavailable(true);
        var parameters = new CompilationParameterResolver();
        var buildings = new BuildingService(store, new CompilationCacheService(cache), parameters, mapper);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new TopoController(buildings,
                        new TopologyQueryService(buildings, new FilterParser(16, 256, 1024, 16384, 256), mapper),
                        new NavigationApiService(buildings, mapper), new AuthoringApiService(parameters, mapper, 512, 4194304, 2),
                        new ApiAccessPolicy("", true)))
                .setControllerAdvice(new ApiErrors()).build();

        mvc.perform(get("/api/v1/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
        String sources = """
                {"files":{"configuration":"building-includes {\\n submap Lobby\\n}\\n",
                  "root":"root Building(allowed: Bool) {\\n constraint Access { require allowed }\\n}\\n",
                  "Lobby":"topo-map Lobby() {\\n let params = {area = 1}\\n topo-node entrance\\n}\\n"},
                 "userParams":{"allowed":true}}
                """;
        mvc.perform(post("/api/v1/authoring/validate").contentType("application/json").content(sources))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
        mvc.perform(post("/api/v1/authoring/validate").contentType("application/json")
                        .content("{\"files\":{\"configuration\":\"building-includes {\"}}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error.code").value("INVALID_TOPOSCRIPT"));
        assertEquals(0, store.calls());

        // The stored-building path does use storage and reports its outage with the retained codes.
        mvc.perform(get("/api/v1/buildings")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("BUILDING_CATALOG_UNAVAILABLE"));
        mvc.perform(post("/api/v1/buildings/swfc/nodes/query").contentType("application/json").content("{}"))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.error.code").value("BUILDING_UNAVAILABLE"));
    }
}
