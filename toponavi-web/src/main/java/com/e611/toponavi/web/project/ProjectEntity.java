package com.e611.toponavi.web.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Persistence mapping for project metadata. Never leaves {@link PostgresProjectStore}. */
@Entity
@Table(schema = "toponavi_projects", name = "projects")
class ProjectEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "building_id", nullable = false, updatable = false, length = 255)
    private String buildingId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;

    protected ProjectEntity() {
    }

    ProjectEntity(UUID id, String buildingId, String name, Instant createdAt) {
        this.id = id;
        this.buildingId = buildingId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    void replaceName(String name, Instant updatedAt) {
        this.name = name;
        this.updatedAt = updatedAt;
    }

    ProjectSummary summary() {
        return new ProjectSummary(id, buildingId, name, createdAt, updatedAt);
    }

    UUID id() {
        return id;
    }
}
