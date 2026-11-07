package com.e611.toponavi.web.project;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test-only {@link ProjectStore} holding immutable snapshots in memory. It mirrors the PostgreSQL store's identity
 * rules (exact lookup, case-only conflicts) but does not represent persisted deployments.
 */
public class InMemoryProjectStore implements ProjectStore {
    private final Map<UUID, ProjectSnapshot> projects = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private volatile boolean unavailable;

    /** Creates or fully replaces the project with this building ID; a convenience for fixtures. */
    public synchronized ProjectSummary put(String buildingId, Map<String, String> files) {
        Optional<ProjectSnapshot> existing = projects.values().stream().filter(p -> p.buildingId().equals(buildingId)).findFirst();
        if (existing.isPresent()) return replace(existing.get().summary().id(), buildingId, files).orElseThrow();
        return create(buildingId, buildingId, files);
    }

    /** Removes every project and restores availability. */
    public void clear() {
        projects.clear();
        unavailable = false;
    }

    /** Makes every later call fail as if the database were unreachable. */
    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public int calls() {
        return calls.get();
    }

    @Override
    public List<String> eligibleBuildingIds() {
        enter();
        return projects.values().stream().filter(ProjectSnapshot::hasConfiguration).map(ProjectSnapshot::buildingId).toList();
    }

    @Override
    public ProjectPage list(int page, int size) {
        enter();
        List<ProjectSummary> all = projects.values().stream().map(ProjectSnapshot::summary)
                .sorted(Comparator.comparing(ProjectSummary::buildingId)).toList();
        long from = Math.min((long) page * size, all.size());
        return new ProjectPage(all.subList((int) from, (int) Math.min(from + size, all.size())), page, size, all.size());
    }

    @Override
    public Optional<ProjectSnapshot> findById(UUID id) {
        enter();
        return Optional.ofNullable(projects.get(id));
    }

    @Override
    public Optional<ProjectSnapshot> findByBuildingId(String buildingId) {
        enter();
        return projects.values().stream().filter(p -> p.buildingId().equals(buildingId)).findFirst();
    }

    @Override
    public synchronized ProjectSummary create(String buildingId, String name, Map<String, String> files) {
        enter();
        String folded = buildingId.toLowerCase(Locale.ROOT);
        if (projects.values().stream().anyMatch(p -> p.buildingId().toLowerCase(Locale.ROOT).equals(folded)))
            throw new BuildingIdConflictException(buildingId);
        Instant now = now();
        ProjectSummary summary = new ProjectSummary(UUID.randomUUID(), buildingId, name, now, now);
        projects.put(summary.id(), new ProjectSnapshot(summary, files));
        return summary;
    }

    @Override
    public synchronized Optional<ProjectSummary> replace(UUID id, String name, Map<String, String> files) {
        enter();
        ProjectSnapshot existing = projects.get(id);
        if (existing == null) return Optional.empty();
        ProjectSummary old = existing.summary();
        ProjectSummary summary = new ProjectSummary(id, old.buildingId(), name, old.createdAt(), now());
        projects.put(id, new ProjectSnapshot(summary, files));
        return Optional.of(summary);
    }

    @Override
    public synchronized boolean delete(UUID id) {
        enter();
        return projects.remove(id) != null;
    }

    private void enter() {
        calls.incrementAndGet();
        if (unavailable) throw new ProjectStoreUnavailableException("Simulated outage", null);
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    /** Snapshot of the current building IDs, for assertions. */
    public List<String> buildingIds() {
        return new ArrayList<>(projects.values().stream().map(ProjectSnapshot::buildingId).sorted().toList());
    }
}
