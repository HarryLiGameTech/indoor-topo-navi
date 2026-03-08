package com.e611.toponavi.web.map;

import com.e611.toponavi.web.github.GitHubContentsService;
import com.e611.toponavi.web.github.GitHubRepoService;
import com.e611.toponavi.web.model.Map;
import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.MapPermissionRepository;
import com.e611.toponavi.web.repository.MapRepository;
import com.e611.toponavi.web.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Core business logic for sketch-map lifecycle management.
 * All GitHub interactions are delegated to the GitHub services.
 */
@Service
public class MapService {

    private final MapRepository mapRepository;
    private final MapPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final MapAccessService accessService;
    private final GitHubRepoService repoService;
    private final GitHubContentsService contentsService;

    public MapService(MapRepository mapRepository,
                      MapPermissionRepository permissionRepository,
                      UserRepository userRepository,
                      MapAccessService accessService,
                      GitHubRepoService repoService,
                      GitHubContentsService contentsService) {
        this.mapRepository = mapRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.repoService = repoService;
        this.contentsService = contentsService;
    }

    /**
     * Lists all maps that the given user can access (owned + shared).
     */
    public List<Map> listAccessibleMaps(User user) {
        return mapRepository.findAccessibleByUserId(user.getId());
    }

    /**
     * Creates a new sketch map:
     * 1. Creates a private GitHub repo under the platform org
     * 2. Writes initial map.json and metadata.json
     * 3. Persists Map entity in the DB
     */
    @Transactional
    public Map createMap(User owner, String title, String initialMapJson) {
        UUID mapId = UUID.randomUUID();
        String repoName = "map-" + mapId;

        // Create GitHub repo (private by default)
        repoService.createRepo(repoName);

        // Write initial files
        String metadataJson = buildMetadataJson(title, owner.getGithubLogin());
        contentsService.writeFile(repoName, "map.json",
                initialMapJson != null ? initialMapJson : "{}",
                "[platform] " + owner.getGithubLogin() + " created map");
        contentsService.writeFile(repoName, "metadata.json",
                metadataJson,
                "[platform] " + owner.getGithubLogin() + " created map");

        // Persist
        Map map = new Map();
        map.setId(mapId);
        map.setTitle(title);
        map.setGithubRepo(repoService.getOrgName() + "/" + repoName);
        map.setVisibility("private");
        map.setOwner(owner);
        return mapRepository.save(map);
    }

    /**
     * Returns map data (map.json content) for the given map, if the user has read access.
     */
    public String getMapContent(User user, UUID mapId) {
        Map map = requireMap(mapId);
        if (!accessService.canRead(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        return contentsService.readFile(repoName, "map.json");
    }

    /**
     * Returns the Map entity if the user has read access.
     */
    public Map getMap(User user, UUID mapId) {
        Map map = requireMap(mapId);
        if (!accessService.canRead(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return map;
    }

    /**
     * Saves (commits) updated map data. Triggers a bot commit to GitHub.
     */
    @Transactional
    public Map saveMap(User user, UUID mapId, String mapJson, String label) {
        Map map = requireMap(mapId);
        if (!accessService.canWrite(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        String commitMsg = "[platform] " + user.getGithubLogin() + " saved"
                + (label != null && !label.isBlank() ? ": " + label : "");
        contentsService.writeFile(repoName, "map.json", mapJson, commitMsg);
        return mapRepository.save(map);
    }

    /**
     * Deletes a map: removes the GitHub repo, permissions, and DB record.
     */
    @Transactional
    public void deleteMap(User user, UUID mapId) {
        Map map = requireMap(mapId);
        if (!accessService.isOwner(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete a map");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        repoService.deleteRepo(repoName);
        permissionRepository.deleteByMapId(mapId);
        mapRepository.delete(map);
    }

    /**
     * Changes the visibility of a map, reflected immediately on GitHub.
     */
    @Transactional
    public Map setVisibility(User user, UUID mapId, String visibility) {
        Map map = requireMap(mapId);
        if (!accessService.isOwner(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can change visibility");
        }
        boolean isPublic = "public".equals(visibility);
        String repoName = extractRepoName(map.getGithubRepo());
        repoService.setRepoVisibility(repoName, isPublic);
        map.setVisibility(visibility);
        return mapRepository.save(map);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    public Map requireMap(UUID mapId) {
        return mapRepository.findById(mapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Map not found"));
    }

    private String extractRepoName(String fullRepoName) {
        // "org/map-uuid" → "map-uuid"
        int slash = fullRepoName.indexOf('/');
        return slash >= 0 ? fullRepoName.substring(slash + 1) : fullRepoName;
    }

    private String buildMetadataJson(String title, String createdBy) {
        return "{\"title\":\"" + escapeJson(title) + "\",\"created_by\":\"" + escapeJson(createdBy) + "\"}";
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

