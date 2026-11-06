package com.e611.toponavi.web.project;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal port for persisted TopoScript projects. Implementations never expose persistence entities; every read
 * returns detached values from one committed state. Callers validate storage requests before invoking mutations.
 *
 * <p>Every method may throw {@link ProjectStoreUnavailableException} when storage cannot be reached in time.
 */
public interface ProjectStore {

    /** Building IDs of projects with a root {@code configuration} or {@code configuration.tcfg} file, unsorted. */
    List<String> eligibleBuildingIds();

    /** Summaries sorted by exact building ID ascending; includes projects that are not eligible buildings. */
    ProjectPage list(int page, int size);

    Optional<ProjectSnapshot> findById(UUID id);

    /** Exact, case-sensitive lookup. */
    Optional<ProjectSnapshot> findByBuildingId(String buildingId);

    /**
     * Creates metadata and all files in one transaction.
     *
     * @throws BuildingIdConflictException when the ID collides exactly or by case with a stored project
     */
    ProjectSummary create(String buildingId, String name, Map<String, String> files);

    /** Atomically replaces the name and the complete source set; empty if the project does not exist. */
    Optional<ProjectSummary> replace(UUID id, String name, Map<String, String> files);

    /** Deletes the project and its sources; false if it did not exist. */
    boolean delete(UUID id);
}
