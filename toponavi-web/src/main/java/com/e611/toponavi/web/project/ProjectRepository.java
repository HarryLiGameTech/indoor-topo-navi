package com.e611.toponavi.web.project;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    /** Exact match; the column's "C" collation keeps the comparison case- and byte-sensitive. */
    Optional<ProjectEntity> findByBuildingId(String buildingId);

    /** Uses the same folding expression as the case-insensitive unique index. */
    @Query("select count(p) > 0 from ProjectEntity p where lower(p.buildingId) = lower(:buildingId)")
    boolean existsByFoldedBuildingId(@Param("buildingId") String buildingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProjectEntity p where p.id = :id")
    Optional<ProjectEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("select p.buildingId from ProjectEntity p where exists ("
            + "select f.path from ProjectFileEntity f where f.projectId = p.id and f.path in :paths)")
    List<String> findBuildingIdsHavingAnyPath(@Param("paths") Collection<String> paths);
}
