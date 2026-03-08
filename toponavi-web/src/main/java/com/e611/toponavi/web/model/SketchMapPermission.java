package com.e611.toponavi.web.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "map_permissions")
@IdClass(SketchMapPermission.PK.class)
public class SketchMapPermission {

    /** "owner" | "editor" | "viewer" */
    public static final String ROLE_OWNER  = "owner";
    public static final String ROLE_EDITOR = "editor";
    public static final String ROLE_VIEWER = "viewer";

    @Id
    @Column(name = "map_id")
    private UUID mapId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt = Instant.now();

    public SketchMapPermission() {}

    public SketchMapPermission(UUID mapId, UUID userId, String role) {
        this.mapId = mapId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getMapId() { return mapId; }
    public void setMapId(UUID mapId) { this.mapId = mapId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }

    // Composite PK class
    public static class PK implements Serializable {
        private UUID mapId;
        private UUID userId;

        public PK() {}
        public PK(UUID mapId, UUID userId) { this.mapId = mapId; this.userId = userId; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(mapId, pk.mapId) && Objects.equals(userId, pk.userId);
        }
        @Override public int hashCode() { return Objects.hash(mapId, userId); }
    }
}

