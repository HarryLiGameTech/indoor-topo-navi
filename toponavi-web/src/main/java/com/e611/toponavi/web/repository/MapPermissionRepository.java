package com.e611.toponavi.web.repository;

import com.e611.toponavi.web.model.MapPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MapPermissionRepository extends JpaRepository<MapPermission, MapPermission.PK> {

    List<MapPermission> findByMapId(UUID mapId);

    Optional<MapPermission> findByMapIdAndUserId(UUID mapId, UUID userId);

    void deleteByMapIdAndUserId(UUID mapId, UUID userId);

    void deleteByMapId(UUID mapId);
}

