package com.e611.toponavi.web.map;

import com.e611.toponavi.web.github.GitHubContentsService;
import com.e611.toponavi.web.github.GitHubRepoService;
import com.e611.toponavi.web.model.SketchMap;
import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.SketchMapPermissionRepository;
import com.e611.toponavi.web.repository.SketchMapRepository;
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

    private final SketchMapRepository sketchMapRepository;
    private final SketchMapPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final MapAccessService accessService;
    private final GitHubRepoService repoService;
    private final GitHubContentsService contentsService;

    public MapService(SketchMapRepository sketchMapRepository,
                      SketchMapPermissionRepository permissionRepository,
                      UserRepository userRepository,
                      MapAccessService accessService,
                      GitHubRepoService repoService,
                      GitHubContentsService contentsService) {
        this.sketchMapRepository = sketchMapRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.repoService = repoService;
        this.contentsService = contentsService;
    }

    /**
     * Lists all maps that the given user can access (owned + shared).
     */
    public List<SketchMap> listAccessibleMaps(User user) {
        return sketchMapRepository.findAccessibleByUserId(user.getId());
    }

    /**
     * Creates a new sketch map:
     * 1. Creates a private GitHub repo under the platform org
     * 2. Writes initial map.json and metadata.json
     * 3. Persists Map entity in the DB
     */
    @Transactional
    public SketchMap createMap(User owner, String title, String initialMapJson) {
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
        SketchMap sketchMap = new SketchMap();
        sketchMap.setId(mapId);
        sketchMap.setTitle(title);
        sketchMap.setGithubRepo(repoService.getOrgName() + "/" + repoName);
        sketchMap.setVisibility("private");
        sketchMap.setOwner(owner);
        return sketchMapRepository.save(sketchMap);
    }

    /**
     * Returns map data (map.json content) for the given map, if the user has read access.
     */
    public String getMapContent(User user, UUID mapId) {
        SketchMap sketchMap = requireMap(mapId);
        if (!accessService.canRead(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(sketchMap.getGithubRepo());
        return contentsService.readFile(repoName, "map.json");
    }

    /**
     * Returns the Map entity if the user has read access.
     */
    public SketchMap getMap(User user, UUID mapId) {
        SketchMap sketchMap = requireMap(mapId);
        if (!accessService.canRead(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return sketchMap;
    }

    /**
     * Saves (commits) updated map data. Triggers a bot commit to GitHub.
     */
    @Transactional
    public SketchMap saveMap(User user, UUID mapId, String mapJson, String label) {
        SketchMap sketchMap = requireMap(mapId);
        if (!accessService.canWrite(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(sketchMap.getGithubRepo());
        String commitMsg = "[platform] " + user.getGithubLogin() + " saved"
                + (label != null && !label.isBlank() ? ": " + label : "");
        contentsService.writeFile(repoName, "map.json", mapJson, commitMsg);
        return sketchMapRepository.save(sketchMap);
    }

    /**
     * Deletes a map: removes the GitHub repo, permissions, and DB record.
     */
    @Transactional
    public void deleteMap(User user, UUID mapId) {
        SketchMap sketchMap = requireMap(mapId);
        if (!accessService.isOwner(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete a map");
        }
        String repoName = extractRepoName(sketchMap.getGithubRepo());
        repoService.deleteRepo(repoName);
        permissionRepository.deleteByMapId(mapId);
        sketchMapRepository.delete(sketchMap);
    }

    /**
     * Changes the visibility of a map, reflected immediately on GitHub.
     */
    @Transactional
    public SketchMap setVisibility(User user, UUID mapId, String visibility) {
        SketchMap sketchMap = requireMap(mapId);
        if (!accessService.isOwner(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can change visibility");
        }
        boolean isPublic = "public".equals(visibility);
        String repoName = extractRepoName(sketchMap.getGithubRepo());
        repoService.setRepoVisibility(repoName, isPublic);
        sketchMap.setVisibility(visibility);
        return sketchMapRepository.save(sketchMap);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    public SketchMap requireMap(UUID mapId) {
        return sketchMapRepository.findById(mapId)
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

