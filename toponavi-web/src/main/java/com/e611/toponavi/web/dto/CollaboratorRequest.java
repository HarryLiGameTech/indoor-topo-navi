package com.e611.toponavi.web.dto;

/**
 * Request body for adding a collaborator to a map.
 */
public class CollaboratorRequest {
    public String userId;   // platform user UUID
    /** "editor" | "viewer" */
    public String role;
}

