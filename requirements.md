# Collaborative Sketch-Map Drawing Platform — Requirements Document

**Project:** `HarryLiGameTech/indoor-topo-navi`
**Date:** 2026-03-08
**Status:** Draft v1.0
**Audience:** Backend/Frontend developers, IDE Agents, Collaborators

---

## 1. Project Overview

This platform is a **collaborative indoor sketch-sketchMap drawing tool** where users can create, edit, share, and version-control topological navigation maps (sketch maps). The platform leverages **GitHub as the storage and version-control engine**, while the application backend retains **full platform control** over all data.

Users interact exclusively through the platform's frontend — they are **never exposed to GitHub directly**.

---

## 2. Core Design Principles

| Principle | Description |
|---|---|
| **Platform-owned repos** | All sketch-sketchMap repos live under a **platform-owned GitHub Organization**, not under individual user accounts |
| **Bot-only Git actor** | All GitHub API calls (push, create, delete repos) are made exclusively by the **platform's GitHub App bot** |
| **User identity is internal** | Users authenticate via GitHub OAuth to identify themselves, but their GitHub accounts have no direct access to any sketch-sketchMap repo |
| **Dual-layer access control** | Permissions are enforced at the **platform DB layer** first, then optionally reflected on GitHub |
| **GitHub as storage backend** | GitHub repos store sketch-sketchMap file content and provide version history; all metadata lives in the platform DB |

---

## 3. System Architecture

```
┌──────────────────────────────────────────────────────┐
│                    Frontend (UI)                      │
│         Sketch-sketchMap drawing canvas + controls          │
│   Users log in via GitHub OAuth — identity only       │
└────────────────────┬─────────────────────────────────┘
                     │ REST API
┌────────────────────▼─────────────────────────────────┐
│              Spring Boot Backend                      │
│  (toponavi-web module)                                │
│                                                       │
│  ┌─────────────────┐   ┌───────────────────────────┐ │
│  │  Auth Layer      │   │  Map Service               │ │
│  │  GitHub OAuth    │   │  CRUD + Versioning         │ │
│  │  (user identity) │   │  Access Control            │ │
│  └─────────────────┘   └────────────┬──────────────┘ │
│                                     │                 │
│  ┌──────────────────────────────────▼──────────────┐ │
│  │         GitHub App Service                       │ │
│  │  - JWT → Installation Token exchange             │ │
│  │  - Calls GitHub API as bot (sole Git actor)      │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌──────────────────────────────────────────────────┐ │
│  │         Platform Database (PostgreSQL)            │ │
│  │  Users | Maps | Permissions | Version Metadata   │ │
│  └──────────────────────────────────────────────────┘ │
└────────────────────┬─────────────────────────────────┘
                     │ GitHub API (Installation Token)
┌────────────────────▼─────────────────────────────────┐
│           Platform GitHub Organization                │
│       (e.g., @toponavi-sketchMap-store)                      │
│                                                       │
│  toponavi-sketchMap-store/sketchMap-{uuid-A}   🔒 private         │
│  toponavi-sketchMap-store/sketchMap-{uuid-B}   🔒 private         │
│  toponavi-sketchMap-store/sketchMap-{uuid-C}   🌐 public          │
└──────────────────────────────────────────────────────┘
```

---

## 4. GitHub Infrastructure Setup

### 4.1 GitHub Organization
- Create a dedicated **GitHub Organization** (e.g., `@toponavi-sketchMap-store`) owned by the platform admin
- All user-generated sketch-sketchMap repos are created under this organization
- The organization is never directly accessible by end users

### 4.2 GitHub App (Bot)
- Create a **GitHub App** registered under the platform admin's personal account
- Install the GitHub App on the platform organization with the following **permissions**:
    - `Contents`: Read & Write
    - `Metadata`: Read-only
    - `Administration`: Read & Write (for repo creation/deletion/visibility)
    - `Members`: Read & Write (for collaborator management, if needed)
- Store securely as environment secrets:
    - `GITHUB_APP_ID`
    - `GITHUB_APP_PRIVATE_KEY` (PEM)
    - `GITHUB_APP_INSTALLATION_ID`

### 4.3 GitHub OAuth App (User Login)
- Separate from the GitHub App above
- Used **only** to authenticate end users and retrieve their GitHub identity (`login`, `id`, `name`, `email`)
- Store securely:
    - `GITHUB_OAUTH_CLIENT_ID`
    - `GITHUB_OAUTH_CLIENT_SECRET`
- Callback URL: `https://<your-domain>/login/oauth2/code/github`

---

## 5. Authentication & Authorization Flow

### 5.1 User Login (OAuth)
```
1. User clicks "Login with GitHub" on the frontend
2. Redirected to GitHub OAuth authorization
3. GitHub redirects back with authorization code
4. Spring Boot backend exchanges code for user OAuth token
5. Backend extracts user identity (login, id, name, avatar_url)
6. Backend upserts user record in platform DB
7. Platform issues its own session token (JWT) to the frontend
8. GitHub OAuth token is DISCARDED after identity extraction
```

> The user's GitHub OAuth token is used **only once** to get their identity. It is never stored or reused.

### 5.2 Bot Actions (GitHub App)
```
1. Backend generates a signed JWT using GitHub App private key
2. Backend exchanges JWT for an Installation Access Token (IAT)
   via: POST /app/installations/{installation_id}/access_tokens
3. IAT is used for all GitHub API calls (create repo, push files, etc.)
4. IAT expires after 1 hour — backend must refresh as needed
```

---

## 6. Sketch-Map Repository Model

### 6.1 Naming Convention
Each sketch sketchMap gets a platform-internal UUID, mapped to a GitHub repo:
```
GitHub Repo:  toponavi-sketchMap-store/sketchMap-{uuid}
Example:      toponavi-sketchMap-store/sketchMap-a3f9c21b-4d77-4e12-b892-000abc123def
```

### 6.2 Repo File Structure
```
sketchMap-{uuid}/
├── sketchMap.json          # Serialized sketch sketchMap data (nodes, edges, rooms, etc.)
├── metadata.json     # Platform metadata (title, created_by, tags)
└── history/          # Optional: exported snapshots or named versions
```

### 6.3 Visibility Modes
| Mode | GitHub Repo Visibility | DB Permission Record |
|---|---|---|
| **Private** | `private` | Only owner has access |
| **Private + Collaborators** | `private` | Owner + listed collaborators |
| **Public** | `public` | Anyone can view (read-only via platform) |

> Visibility is changed by the bot calling `PATCH /repos/{org}/{repo}` — users cannot change this directly.

---

## 7. Version Control Model

| User Action | GitHub Action (by bot) |
|---|---|
| "Save" sketch sketchMap | Commit updated `sketchMap.json` to `main` branch |
| "Save Draft" | Commit to a feature branch `draft/{uuid}` |
| "View History" | `GET /repos/{org}/{repo}/commits` — displayed in frontend |
| "Restore Version" | Bot creates a new commit reverting to target commit's `sketchMap.json` |
| "Named Checkpoint" | Bot creates a Git tag `v{n}-{label}` |

Commit messages follow the format:
```
[platform] {username} saved: {optional label}   (e.g., "[platform] alice saved: north wing update")
```

---

## 8. Access Control — Dual-Layer Model

### Layer 1: Platform Database (enforced first)
```sql
-- Core permission table
CREATE TABLE map_permissions (
    map_id      UUID        NOT NULL REFERENCES maps(id),
    user_id     UUID        NOT NULL REFERENCES users(id),
    role        VARCHAR(20) NOT NULL,  -- 'owner' | 'editor' | 'viewer'
    granted_at  TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (map_id, user_id)
);

-- Maps table
CREATE TABLE maps (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255),
    github_repo     VARCHAR(255) UNIQUE,  -- 'toponavi-sketchMap-store/sketchMap-{uuid}'
    visibility      VARCHAR(20) NOT NULL, -- 'private' | 'private_collab' | 'public'
    owner_user_id   UUID        NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT now()
);

-- Users table
CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    github_login    VARCHAR(100) UNIQUE NOT NULL,
    github_id       BIGINT      UNIQUE NOT NULL,
    display_name    VARCHAR(255),
    avatar_url      TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT now()
);
```

### Layer 2: GitHub (storage enforcement)
- All sketch-sketchMap repos are **private by default**
- The **only actor with write access** is the GitHub App bot
- GitHub repo visibility is updated by the bot when the platform-level visibility changes
- End users are **never added as GitHub collaborators** (unless a future "power user" mode is explicitly designed)

---

## 9. Backend Module Responsibilities

Based on the existing multi-module Gradle structure:

| Module | Responsibility |
|---|---|
| `toponavi-core` | Domain model: sketch-sketchMap graph, nodes, edges, rooms, topology data structures |
| `toponavi-dsl` | DSL compiler/parser for sketch-sketchMap definitions (existing functionality) |
| `toponavi-web` | Spring Boot REST API, GitHub integration services, auth, access control |

### New Services to Implement in `toponavi-web`

```
toponavi-web/src/main/java/.../
├── auth/
│   ├── GitHubOAuthController.java       # OAuth login/callback
│   ├── JwtService.java                  # Platform session JWT
│   └── SecurityConfig.java              # Spring Security config
├── github/
│   ├── GitHubAppTokenService.java       # JWT → Installation Token exchange
│   ├── GitHubRepoService.java           # Create/delete/update repos
│   └── GitHubContentsService.java       # Read/write files (sketchMap.json)
├── sketchMap/
│   ├── MapController.java               # REST endpoints for sketch maps
│   ├── MapService.java                  # Business logic + permission checks
│   ├── MapVersionService.java           # Version history, restore, tags
│   └── MapAccessService.java           # Access control enforcement
└── model/
    ├── User.java
    ├── Map.java
    └── MapPermission.java
```

---

## 10. Key API Endpoints (Backend)

| Method | Path | Description |
|---|---|---|
| `GET` | `/auth/github` | Redirect to GitHub OAuth |
| `GET` | `/login/oauth2/code/github` | OAuth callback, returns platform JWT |
| `POST` | `/api/maps` | Create new sketch sketchMap (+ GitHub repo) |
| `GET` | `/api/maps` | List maps accessible to current user |
| `GET` | `/api/maps/{id}` | Get sketchMap data |
| `PUT` | `/api/maps/{id}` | Save/update sketchMap (triggers bot commit) |
| `DELETE` | `/api/maps/{id}` | Delete sketchMap (+ GitHub repo deletion) |
| `GET` | `/api/maps/{id}/versions` | List version history |
| `POST` | `/api/maps/{id}/restore/{commitSha}` | Restore to a previous version |
| `PUT` | `/api/maps/{id}/visibility` | Change sketchMap visibility |
| `POST` | `/api/maps/{id}/collaborators` | Add a collaborator |
| `DELETE` | `/api/maps/{id}/collaborators/{userId}` | Remove a collaborator |

---

## 11. Secrets & Configuration

All secrets must be stored as **environment variables** or **GitHub/Dependabot secrets** — never hardcoded.

```yaml
# application.yml (values injected from environment)
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_OAUTH_CLIENT_ID}
            client-secret: ${GITHUB_OAUTH_CLIENT_SECRET}

platform:
  github:
    app-id: ${GITHUB_APP_ID}
    private-key: ${GITHUB_APP_PRIVATE_KEY}
    installation-id: ${GITHUB_APP_INSTALLATION_ID}
    org-name: ${GITHUB_ORG_NAME}   # e.g., "toponavi-sketchMap-store"
```

---

## 12. Setup Checklist (One-Time, by Platform Admin)

```
[ ] 1. Create GitHub Organization (e.g., @toponavi-sketchMap-store)
[ ] 2. Create GitHub OAuth App → store client-id + client-secret as secrets
[ ] 3. Create GitHub App → download private key (.pem)
[ ] 4. Install GitHub App on the Organization (grant: Contents R/W, Admin R/W)
[ ] 5. Store App ID, Installation ID, Private Key as env/Dependabot secrets
[ ] 6. Set up PostgreSQL database and run schema migrations
[ ] 7. Configure application.yml with all injected secrets
[ ] 8. Verify bot can create/delete repos in the org via API test
```

---

## 13. What GitHub Sees vs. What Users See

| | GitHub Side | User (Platform) Side |
|---|---|---|
| **Repo owner** | `@toponavi-sketchMap-store` (org) | "Your sketchMap: North Wing v3" |
| **Committer** | `🤖 toponavi-bot` | "Saved by alice at 14:32" |
| **File contents** | `sketchMap.json` (raw JSON/DSL) | Visual sketch-sketchMap canvas |
| **Version** | Git commit SHA | "Version 5 — north wing update" |
| **Visibility** | `private` / `public` GitHub repo | "Private" / "Public" toggle |
| **Access control** | No collaborators (bot only) | "Share with bob (editor)" |

---

## 14. Out of Scope (Current Version)

- Real-time collaborative editing (WebSocket / CRDT) — future feature
- Users directly cloning/pushing to sketch-sketchMap repos via Git CLI
- Self-hosted GitHub Enterprise support
- Mobile native applications