package com.e611.toponavi.web.repository;

import com.e611.toponavi.web.model.SketchMapPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SketchMapPermissionRepository extends JpaRepository<SketchMapPermission, SketchMapPermission.PK> {

    List<SketchMapPermission> findByMapId(UUID mapId);

    Optional<SketchMapPermission> findByMapIdAndUserId(UUID mapId, UUID userId);

    void deleteByMapIdAndUserId(UUID mapId, UUID userId);

    void deleteByMapId(UUID mapId);
}

