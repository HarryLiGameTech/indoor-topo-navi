package com.e611.toponavi.web.project;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Detached, immutable view of one committed project state: its metadata plus a defensive copy of every source string.
 * A snapshot never references JPA entities or lazy collections, so it can be used after its read transaction closes.
 */
public record ProjectSnapshot(ProjectSummary summary, Map<String, String> files) {
    /** Root-level filenames that make a stored project discoverable as a building. */
    public static final List<String> CONFIGURATION_PATHS = List.of("configuration", "configuration.tcfg");

    public ProjectSnapshot {
        Objects.requireNonNull(summary, "summary");
        files = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNull(files, "files")));
    }

    public String buildingId() {
        return summary.buildingId();
    }

    /** A presence check on filenames only; an empty or non-compiling configuration still counts. */
    public boolean hasConfiguration() {
        return CONFIGURATION_PATHS.stream().anyMatch(files::containsKey);
    }
}
