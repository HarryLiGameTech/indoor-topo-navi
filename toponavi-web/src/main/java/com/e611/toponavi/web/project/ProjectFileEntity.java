package com.e611.toponavi.web.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence mapping for one stored source file. Deliberately has no JPA association to {@link ProjectEntity}:
 * the database foreign key owns referential integrity, and files are only read or written in bulk per project.
 */
@Entity
@Table(schema = "toponavi_projects", name = "project_files")
@IdClass(ProjectFileEntity.Key.class)
class ProjectFileEntity {
    @Id
    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    @Id
    @Column(name = "path", nullable = false, updatable = false, columnDefinition = "text")
    private String path;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    protected ProjectFileEntity() {
    }

    ProjectFileEntity(UUID projectId, String path, String content) {
        this.projectId = projectId;
        this.path = path;
        this.content = content;
    }

    String path() {
        return path;
    }

    String content() {
        return content;
    }

    /** Composite primary key (project_id, path). */
    static class Key implements Serializable {
        private UUID projectId;
        private String path;

        protected Key() {
        }

        Key(UUID projectId, String path) {
            this.projectId = projectId;
            this.path = path;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && Objects.equals(projectId, key.projectId) && Objects.equals(path, key.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, path);
        }
    }
}
