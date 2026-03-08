package com.e611.toponavi.web.repository;

import com.e611.toponavi.web.model.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MapRepository extends JpaRepository<Map, UUID> {

    List<Map> findByOwnerId(UUID ownerId);

    /**
     * Returns all maps where the given user has any permission entry
     * (covers maps shared with the user as editor/viewer as well as owned).
     */
    @Query("""
        SELECT m FROM Map m
        WHERE m.owner.id = :userId
           OR EXISTS (
               SELECT 1 FROM MapPermission p
               WHERE p.mapId = m.id AND p.userId = :userId
           )
    """)
    List<Map> findAccessibleByUserId(@Param("userId") UUID userId);
}

