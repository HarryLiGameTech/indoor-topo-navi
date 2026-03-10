package com.e611.toponavi.web.repository;

import com.e611.toponavi.web.model.SketchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SketchMapRepository extends JpaRepository<SketchMap, UUID> {

    List<SketchMap> findByOwnerId(UUID ownerId);

    /**
     * Returns all maps where the given user has any permission entry
     * (covers maps shared with the user as editor/viewer as well as owned).
     */
    @Query("""
        SELECT m FROM SketchMap m
        WHERE m.owner.id = :userId
           OR EXISTS (
               SELECT 1 FROM SketchMapPermission p
               WHERE p.mapId = m.id AND p.userId = :userId
           )
    """)
    List<SketchMap> findAccessibleByUserId(@Param("userId") UUID userId);
}

