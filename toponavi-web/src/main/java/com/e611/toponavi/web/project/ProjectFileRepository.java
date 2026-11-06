package com.e611.toponavi.web.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface ProjectFileRepository extends JpaRepository<ProjectFileEntity, ProjectFileEntity.Key> {

    List<ProjectFileEntity> findByProjectId(UUID projectId);

    /**
     * Bulk delete executed immediately, so replacement rows may reuse the same primary keys afterwards. Write
     * transactions never load file entities, so no stale managed copies of the deleted rows remain.
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from ProjectFileEntity f where f.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") UUID projectId);
}
