package com.e611.toponavi.web.project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The only production {@link ProjectStore}. It owns its transaction boundaries and translates every database failure,
 * including failures raised while beginning or committing a transaction, into internal store errors.
 *
 * <p>Transactions are started with explicit {@link TransactionTemplate}s rather than {@code @Transactional}, so no
 * boundary depends on proxy self-invocation and the translation wraps the complete transaction, commit included.
 * Reads run in short read-only {@code REPEATABLE READ} transactions, so metadata and files always come from one
 * committed state, and return detached snapshots. Writes use {@code READ COMMITTED} with a transaction-local lock
 * timeout; replacement and deletion lock the parent project row before touching it or its files.
 */
@Component
public class PostgresProjectStore implements ProjectStore {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresProjectStore.class);
    private static final Set<String> BUILDING_ID_CONSTRAINTS =
            Set.of("projects_building_id_unique", "projects_building_id_folded_unique");

    private final ProjectRepository projects;
    private final ProjectFileRepository files;
    private final EntityManager entityManager;
    private final TransactionTemplate reads;
    private final TransactionTemplate writes;
    private final long lockTimeoutMs;

    public PostgresProjectStore(ProjectRepository projects, ProjectFileRepository files, EntityManager entityManager,
                                PlatformTransactionManager transactions, ProjectLimits limits) {
        this.projects = projects;
        this.files = files;
        this.entityManager = entityManager;
        this.reads = new TransactionTemplate(transactions);
        reads.setReadOnly(true);
        reads.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.writes = new TransactionTemplate(transactions);
        writes.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.lockTimeoutMs = limits.lockTimeoutMs();
    }

    @Override
    public List<String> eligibleBuildingIds() {
        return read(() -> projects.findBuildingIdsHavingAnyPath(ProjectSnapshot.CONFIGURATION_PATHS));
    }

    @Override
    public ProjectPage list(int page, int size) {
        return read(() -> {
            var result = projects.findAll(PageRequest.of(page, size, Sort.by("buildingId")));
            return new ProjectPage(result.map(ProjectEntity::summary).getContent(), page, size, result.getTotalElements());
        });
    }

    @Override
    public Optional<ProjectSnapshot> findById(UUID id) {
        return read(() -> projects.findById(id).map(this::snapshot));
    }

    @Override
    public Optional<ProjectSnapshot> findByBuildingId(String buildingId) {
        return read(() -> projects.findByBuildingId(buildingId).map(this::snapshot));
    }

    @Override
    public ProjectSummary create(String buildingId, String name, Map<String, String> sources) {
        return write(buildingId, () -> {
            // Friendly preflight; the unique constraints remain authoritative for concurrent creation.
            if (projects.existsByFoldedBuildingId(buildingId)) throw new BuildingIdConflictException(buildingId);
            ProjectEntity project = new ProjectEntity(UUID.randomUUID(), buildingId, name, now());
            entityManager.persist(project);
            entityManager.flush(); // The parent row must exist before file rows reference it.
            insertFiles(project.id(), sources);
            return project.summary();
        });
    }

    @Override
    public Optional<ProjectSummary> replace(UUID id, String name, Map<String, String> sources) {
        return write(null, () -> {
            Optional<ProjectEntity> locked = projects.findByIdForUpdate(id);
            if (locked.isEmpty()) return Optional.empty();
            ProjectEntity project = locked.get();
            // Set explicitly: replacing only child rows would not make the parent entity dirty.
            project.replaceName(name, now());
            files.deleteByProjectId(id); // Flushes the metadata update, then deletes before keys are reused.
            insertFiles(id, sources);
            return Optional.of(project.summary());
        });
    }

    @Override
    public boolean delete(UUID id) {
        return write(null, () -> {
            Optional<ProjectEntity> locked = projects.findByIdForUpdate(id);
            if (locked.isEmpty()) return false;
            files.deleteByProjectId(id);
            entityManager.remove(locked.get());
            entityManager.flush();
            return true;
        });
    }

    /** Test seam between the metadata and file reads of one snapshot transaction. */
    protected void afterMetadataRead(UUID projectId) {
    }

    private ProjectSnapshot snapshot(ProjectEntity project) {
        afterMetadataRead(project.id());
        Map<String, String> sources = new HashMap<>();
        for (ProjectFileEntity file : files.findByProjectId(project.id())) sources.put(file.path(), file.content());
        return new ProjectSnapshot(project.summary(), sources);
    }

    private void insertFiles(UUID projectId, Map<String, String> sources) {
        sources.forEach((path, content) -> entityManager.persist(new ProjectFileEntity(projectId, path, content)));
        entityManager.flush();
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS); // PostgreSQL timestamptz precision.
    }

    private <T> T read(Supplier<T> work) {
        try {
            return reads.execute(status -> work.get());
        } catch (RuntimeException error) {
            throw translate(error, null);
        }
    }

    private <T> T write(String buildingId, Supplier<T> work) {
        try {
            return writes.execute(status -> {
                // Transaction-local, so pooled connections do not keep it for unrelated work.
                entityManager.createNativeQuery("select set_config('lock_timeout', :timeout, true)")
                        .setParameter("timeout", lockTimeoutMs + "ms").getSingleResult();
                return work.get();
            });
        } catch (RuntimeException error) {
            throw translate(error, buildingId);
        }
    }

    private RuntimeException translate(RuntimeException error, String buildingId) {
        if (error instanceof BuildingIdConflictException) return error;
        String constraint = violatedConstraint(error);
        String sqlState = sqlState(error);
        if (constraint != null && BUILDING_ID_CONSTRAINTS.contains(constraint) && buildingId != null)
            return new BuildingIdConflictException(buildingId);
        if (constraint != null || (sqlState != null && sqlState.startsWith("23")))
            return new IllegalStateException("Unexpected project store integrity violation: " + constraint, error);
        if (error instanceof DataAccessException || error instanceof TransactionException || error instanceof PersistenceException) {
            LOG.warn("Project store operation failed (SQLState {}): {}", sqlState, error.toString());
            return new ProjectStoreUnavailableException("Project storage is unavailable.", error);
        }
        return error;
    }

    private static String violatedConstraint(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) return violation.getConstraintName();
            if (cause.getCause() == cause) break;
        }
        return null;
    }

    private static String sqlState(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) return sql.getSQLState();
            if (cause.getCause() == cause) break;
        }
        return null;
    }
}
