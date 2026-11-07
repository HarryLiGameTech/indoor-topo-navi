package com.e611.toponavi.web.project;

/** Storage could not be reached, timed out waiting for a lock, or failed transiently. Never carries SQL to clients. */
public class ProjectStoreUnavailableException extends RuntimeException {
    public ProjectStoreUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
