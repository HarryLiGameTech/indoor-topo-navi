package com.e611.toponavi.web.map;

import com.e611.toponavi.web.dto.*;
import com.e611.toponavi.web.model.SketchMap;
import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for sketch-map CRUD, version management, and collaboration.
 *
 * All endpoints require a valid platform JWT (enforced by SecurityConfig + JwtAuthFilter).
 * The authenticated {@link User} is injected via {@code @AuthenticationPrincipal}.
 */
@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final MapService mapService;
    private final MapVersionService versionService;
    private final MapAccessService accessService;
    private final UserRepository userRepository;

    public MapController(MapService mapService,
                         MapVersionService versionService,
                         MapAccessService accessService,
                         UserRepository userRepository) {
        this.mapService = mapService;
        this.versionService = versionService;
        this.accessService = accessService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Map CRUD
    // -------------------------------------------------------------------------

    /**
     * POST /api/maps
     * Create a new sketch map (also creates the backing GitHub repo).
     */
    @PostMapping
    public ResponseEntity<MapSummaryDto> createMap(@AuthenticationPrincipal User user,
                                                   @RequestBody CreateMapRequest req) {
        SketchMap sketchMap = mapService.createMap(user, req.title, req.initialMapJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSummary(sketchMap));
    }

    /**
     * GET /api/maps
     * List all maps accessible to the current user.
     */
    @GetMapping
    public ResponseEntity<List<MapSummaryDto>> listMaps(@AuthenticationPrincipal User user) {
        List<MapSummaryDto> maps = mapService.listAccessibleMaps(user)
                .stream().map(this::toSummary).collect(Collectors.toList());
        return ResponseEntity.ok(maps);
    }

    /**
     * GET /api/maps/{id}
     * Get map metadata + current map.json content.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MapDetailDto> getMap(@AuthenticationPrincipal User user,
                                               @PathVariable UUID id) {
        SketchMap sketchMap = mapService.getMap(user, id);
        String content = mapService.getMapContent(user, id);
        return ResponseEntity.ok(new MapDetailDto(toSummary(sketchMap), content));
    }

    /**
     * PUT /api/maps/{id}
     * Save updated map data (triggers a bot commit to GitHub).
     */
    @PutMapping("/{id}")
    public ResponseEntity<MapSummaryDto> saveMap(@AuthenticationPrincipal User user,
                                                  @PathVariable UUID id,
                                                  @RequestBody SaveMapRequest req) {
        SketchMap sketchMap = mapService.saveMap(user, id, req.mapJson, req.label);
        return ResponseEntity.ok(toSummary(sketchMap));
    }

    /**
     * DELETE /api/maps/{id}
     * Delete a map and its GitHub repo. Owner only.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMap(@AuthenticationPrincipal User user,
                                          @PathVariable UUID id) {
        mapService.deleteMap(user, id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Visibility
    // -------------------------------------------------------------------------

    /**
     * PUT /api/maps/{id}/visibility
     * Change visibility of a map. Owner only.
     */
    @PutMapping("/{id}/visibility")
    public ResponseEntity<MapSummaryDto> setVisibility(@AuthenticationPrincipal User user,
                                                        @PathVariable UUID id,
                                                        @RequestBody VisibilityRequest req) {
        SketchMap sketchMap = mapService.setVisibility(user, id, req.visibility);
        return ResponseEntity.ok(toSummary(sketchMap));
    }

    // -------------------------------------------------------------------------
    // Version history
    // -------------------------------------------------------------------------

    /**
     * GET /api/maps/{id}/versions
     * List commit history for the map.
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<java.util.Map<String, String>>> listVersions(
            @AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(versionService.listVersions(user, id));
    }

    /**
     * POST /api/maps/{id}/restore/{commitSha}
     * Restore map to a previous version by creating a new commit.
     */
    @PostMapping("/{id}/restore/{commitSha}")
    public ResponseEntity<Void> restoreVersion(@AuthenticationPrincipal User user,
                                               @PathVariable UUID id,
                                               @PathVariable String commitSha) {
        versionService.restoreVersion(user, id, commitSha);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/maps/{id}/checkpoint
     * Create a named checkpoint (Git tag) at the current HEAD.
     */
    @PostMapping("/{id}/checkpoint")
    public ResponseEntity<Void> createCheckpoint(@AuthenticationPrincipal User user,
                                                  @PathVariable UUID id,
                                                  @RequestBody CheckpointRequest req) {
        versionService.createCheckpoint(user, id, req.label);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    /**
     * POST /api/maps/{id}/collaborators
     * Grant a user editor or viewer access. Owner only.
     */
    @PostMapping("/{id}/collaborators")
    public ResponseEntity<Void> addCollaborator(@AuthenticationPrincipal User user,
                                                @PathVariable UUID id,
                                                @RequestBody CollaboratorRequest req) {
        SketchMap sketchMap = mapService.requireMap(id);
        if (!accessService.isOwner(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can manage collaborators");
        }
        UUID granteeId = UUID.fromString(req.userId);
        // Verify grantee exists
        userRepository.findById(granteeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        accessService.grantPermission(id, granteeId, req.role);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/maps/{id}/collaborators/{userId}
     * Remove a collaborator. Owner only.
     */
    @DeleteMapping("/{id}/collaborators/{userId}")
    public ResponseEntity<Void> removeCollaborator(@AuthenticationPrincipal User user,
                                                    @PathVariable UUID id,
                                                    @PathVariable UUID userId) {
        SketchMap sketchMap = mapService.requireMap(id);
        if (!accessService.isOwner(user, sketchMap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can manage collaborators");
        }
        accessService.revokePermission(id, userId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // DTO helpers
    // -------------------------------------------------------------------------

    private MapSummaryDto toSummary(SketchMap sketchMap) {
        return new MapSummaryDto(
                sketchMap.getId(),
                sketchMap.getTitle(),
                sketchMap.getVisibility(),
                sketchMap.getOwner().getGithubLogin(),
                sketchMap.getCreatedAt(),
                sketchMap.getUpdatedAt()
        );
    }

    // -------------------------------------------------------------------------
    // Inline response DTOs (simple enough to nest here)
    // -------------------------------------------------------------------------

    public record MapSummaryDto(
            UUID id,
            String title,
            String visibility,
            String ownerLogin,
            java.time.Instant createdAt,
            java.time.Instant updatedAt
    ) {}

    public record MapDetailDto(
            MapSummaryDto metadata,
            String mapJson
    ) {}
}

