package com.e611.toponavi.web.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_login", unique = true, nullable = false, length = 100)
    private String githubLogin;

    @Column(name = "github_id", unique = true, nullable = false)
    private Long githubId;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getGithubLogin() { return githubLogin; }
    public void setGithubLogin(String githubLogin) { this.githubLogin = githubLogin; }

    public Long getGithubId() { return githubId; }
    public void setGithubId(Long githubId) { this.githubId = githubId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

