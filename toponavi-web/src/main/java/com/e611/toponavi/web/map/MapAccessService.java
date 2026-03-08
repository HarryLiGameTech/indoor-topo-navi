package com.e611.toponavi.web.map;

import com.e611.toponavi.web.model.Map;
import com.e611.toponavi.web.model.MapPermission;
import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.MapPermissionRepository;
import com.e611.toponavi.web.repository.MapRepository;
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

    private final MapRepository mapRepository;
    private final MapPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public MapAccessService(MapRepository mapRepository,
                            MapPermissionRepository permissionRepository,
                            UserRepository userRepository) {
        this.mapRepository = mapRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    /** Returns true if the user can read the map (owner, editor, viewer, or public map). */
    public boolean canRead(User user, Map map) {
        if ("public".equals(map.getVisibility())) return true;
        if (map.getOwner().getId().equals(user.getId())) return true;
        return permissionRepository.findByMapIdAndUserId(map.getId(), user.getId()).isPresent();
    }

    /** Returns true if the user can write (save) to the map (owner or editor). */
    public boolean canWrite(User user, Map map) {
        if (map.getOwner().getId().equals(user.getId())) return true;
        return permissionRepository.findByMapIdAndUserId(map.getId(), user.getId())
                .map(p -> MapPermission.ROLE_EDITOR.equals(p.getRole()))
                .orElse(false);
    }

    /** Returns true if the user is the owner of the map. */
    public boolean isOwner(User user, Map map) {
        return map.getOwner().getId().equals(user.getId());
    }

    /**
     * Grants a collaborator role on the map.
     * Only the owner can call this.
     */
    @Transactional
    public void grantPermission(UUID mapId, UUID granteeUserId, String role) {
        MapPermission.PK pk = new MapPermission.PK(mapId, granteeUserId);
        MapPermission perm = permissionRepository.findById(pk)
                .orElse(new MapPermission(mapId, granteeUserId, role));
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

