package com.e611.toponavi.web.project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Configured bounds for stored projects and project mutation requests. */
@Component
public class ProjectLimits {
    private final int maxFiles;
    private final long maxSourceBytes;
    private final long maxRequestBytes;
    private final long lockTimeoutMs;

    public ProjectLimits(@Value("${platform.projects.max-files:512}") int maxFiles,
                         @Value("${platform.projects.max-source-bytes:4194304}") long maxSourceBytes,
                         @Value("${platform.projects.max-request-bytes:33554432}") long maxRequestBytes,
                         @Value("${platform.projects.lock-timeout-ms:5000}") long lockTimeoutMs) {
        if (maxFiles < 1 || maxSourceBytes < 1 || maxRequestBytes < 1 || lockTimeoutMs < 1)
            throw new IllegalArgumentException("Project limits must be positive");
        if (maxRequestBytes > Integer.MAX_VALUE)
            throw new IllegalArgumentException("platform.projects.max-request-bytes must not exceed " + Integer.MAX_VALUE);
        this.maxFiles = maxFiles;
        this.maxSourceBytes = maxSourceBytes;
        this.maxRequestBytes = maxRequestBytes;
        this.lockTimeoutMs = lockTimeoutMs;
    }

    public int maxFiles() { return maxFiles; }
    public long maxSourceBytes() { return maxSourceBytes; }
    public long maxRequestBytes() { return maxRequestBytes; }
    public long lockTimeoutMs() { return lockTimeoutMs; }
}
