package com.e611.toponavi.web.map;

import com.e611.toponavi.web.model.SketchMap;
import com.e611.toponavi.web.model.SketchMapPermission;
import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.SketchMapPermissionRepository;
import com.e611.toponavi.web.repository.SketchMapRepository;
import com.e611.toponavi.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Enforces access-control rules at the platform database level.
 * All map operations must go through this service before touching GitHub.
 */
@Service
public class MapAccessService {

    private final SketchMapRepository sketchMapRepository;
    private final SketchMapPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public MapAccessService(SketchMapRepository sketchMapRepository,
                            SketchMapPermissionRepository permissionRepository,
                            UserRepository userRepository) {
        this.sketchMapRepository = sketchMapRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    /** Returns true if the user can read the map (owner, editor, viewer, or public map). */
    public boolean canRead(User user, SketchMap sketchMap) {
        if ("public".equals(sketchMap.getVisibility())) return true;
        if (sketchMap.getOwner().getId().equals(user.getId())) return true;
        return permissionRepository.findByMapIdAndUserId(sketchMap.getId(), user.getId()).isPresent();
    }

    /** Returns true if the user can write (save) to the map (owner or editor). */
    public boolean canWrite(User user, SketchMap sketchMap) {
        if (sketchMap.getOwner().getId().equals(user.getId())) return true;
        return permissionRepository.findByMapIdAndUserId(sketchMap.getId(), user.getId())
                .map(p -> SketchMapPermission.ROLE_EDITOR.equals(p.getRole()))
                .orElse(false);
    }

    /** Returns true if the user is the owner of the map. */
    public boolean isOwner(User user, SketchMap sketchMap) {
        return sketchMap.getOwner().getId().equals(user.getId());
    }

    /**
     * Grants a collaborator role on the map.
     * Only the owner can call this.
     */
    @Transactional
    public void grantPermission(UUID mapId, UUID granteeUserId, String role) {
        SketchMapPermission.PK pk = new SketchMapPermission.PK(mapId, granteeUserId);
        SketchMapPermission perm = permissionRepository.findById(pk)
                .orElse(new SketchMapPermission(mapId, granteeUserId, role));
        perm.setRole(role);
        permissionRepository.save(perm);
    }

    /**
     * Revokes a collaborator's access to the map.
     * Only the owner can call this.
     */
    @Transactional
    public void revokePermission(UUID mapId, UUID granteeUserId) {
        permissionRepository.deleteByMapIdAndUserId(mapId, granteeUserId);
    }
}

