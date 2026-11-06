package com.e611.toponavi.web.service;

import api.AuthoringService;
import api.TopoNaviService;
import compiler.CompilationResult;
import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.cache.CompilationCacheService;
import com.e611.toponavi.web.project.ProjectSnapshot;
import com.e611.toponavi.web.project.ProjectStore;
import com.e611.toponavi.web.project.ProjectStoreUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves retained building IDs to stored project sources and compiles them. A stored project is a discoverable
 * building only when it has a root {@code configuration} or {@code configuration.tcfg} file; other stored projects
 * remain reachable through the project API but are absent here.
 */
@Service
public class BuildingService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BuildingService.class);
    private final ProjectStore store;
    private final CompilationCacheService cache;
    private final CompilationParameterResolver parameters;
    private final ObjectMapper mapper;

    public BuildingService(ProjectStore store, CompilationCacheService cache, CompilationParameterResolver parameters,
                           ObjectMapper mapper) {
        this.store = store;
        this.cache = cache;
        this.parameters = parameters;
        this.mapper = mapper;
    }

    public List<String> buildings() {
        try {
            return store.eligibleBuildingIds().stream().sorted().toList();
        } catch (ProjectStoreUnavailableException error) {
            throw new ApiException(503, "BUILDING_CATALOG_UNAVAILABLE", "The configured map inventory could not be read.");
        }
    }

    /** Source files of one eligible building, from a single immutable snapshot. */
    public Map<String, String> files(String buildingId) {
        return snapshot(buildingId).files();
    }

    public CompilationResult compile(String buildingId, JsonNode userParams) {
        // One snapshot supplies both the cache identity and the compilation inputs.
        Map<String, String> files = files(buildingId);
        JsonNode inspection = mapper.valueToTree(AuthoringService.inspect(files));
        if (!inspection.path("diagnostics").isEmpty())
            throw new ApiException(500, "INVALID_BUILDING", "The installed building has invalid or missing source files.");
        Map<String, Object> params = parameters.resolve(inspection, userParams);
        return cache.load(buildingId, files, params).map(CompilationCacheService.CachedResult::getResult).orElseGet(() -> {
            CompilationResult result;
            try { result = TopoNaviService.compile(files, params); }
            catch (RuntimeException error) {
                LOG.error("Installed building compilation failed: {}", buildingId, error);
                throw new ApiException(500, "INVALID_BUILDING", "The installed building could not be compiled.");
            }
            cache.save(buildingId, files, params, result);
            return result;
        });
    }

    private ProjectSnapshot snapshot(String buildingId) {
        Optional<ProjectSnapshot> snapshot;
        try {
            snapshot = store.findByBuildingId(buildingId);
        } catch (ProjectStoreUnavailableException error) {
            throw new ApiException(503, "BUILDING_UNAVAILABLE", "The building source could not be read.");
        }
        // Eligibility is checked against the loaded snapshot itself, not a separate catalog query.
        if (snapshot.isEmpty() || !snapshot.get().hasConfiguration())
            throw new ApiException(404, "BUILDING_NOT_FOUND", "Unknown building ID.");
        return snapshot.get();
    }
}
