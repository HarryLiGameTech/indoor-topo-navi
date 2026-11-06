package com.e611.toponavi.web.project;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Stored project metadata without source contents. */
public record ProjectSummary(UUID id, String buildingId, String name, Instant createdAt, Instant updatedAt) {
    public ProjectSummary {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(buildingId, "buildingId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }
}
