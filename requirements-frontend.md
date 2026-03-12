# Frontend Requirements Document

**Project:** `indoor-topo-navi` — Collaborative Sketch-Map Drawing Platform
**Date:** 2026-03-11
**Status:** Draft v1.0
**Depends on:** `requirements.md` (backend spec)

---

## 1. Overview

The frontend is a **single-page application (SPA)** that provides:
- GitHub OAuth login (identity only — users never see GitHub internals)
- **Guest mode**: solo map editing without login, stored locally only
- Map management (list, share, version history, restore) — requires login
- Collaborator management — requires login

Users interact **exclusively through this frontend** — they are never exposed to GitHub repos, commits, or the bot.

### Guest vs. Authenticated Modes

| Feature | Guest (no login) | Authenticated |
|---|---|---|
| Open the map editor | ✅ | ✅ |
| Draw / edit map locally | ✅ | ✅ |
| Export map JSON (download) | ✅ | ✅ |
| Import map JSON (upload file) | ✅ | ✅ |
| Save to backend / GitHub | ❌ (prompt to log in) | ✅ |
| View own map list | ❌ | ✅ |
| Version history & restore | ❌ | ✅ |
| Share / collaborators | ❌ | ✅ |

---

## 2. Technology Choices (Recommended)

| Concern | Choice |
|---|---|
| Framework | React |
| Language | TypeScript |
| State management | Zustand |
| HTTP client | `axios` with a shared instance |
| Canvas/drawing | `react-konva` or `fabric.js` |
| Routing | React Router v6 |
| Styling | Tailwind CSS |

---

## 3. Authentication Flow (URL Param Approach)

This is the **most critical flow** to implement correctly.

### 3.1 Step-by-step

```
1. User clicks "Login with GitHub" button
   → Frontend navigates (window.location.href) to: GET /auth/github

2. Backend redirects → GitHub OAuth consent page

3. GitHub redirects back → Backend OAuth callback handler

4. Backend issues platform JWT, redirects to:
   GET /auth/callback?token=eyJhbGciOiJIUzI1NiJ9...

5. Frontend /auth/callback page:
   a. Reads token from URL:  new URLSearchParams(window.location.search).get("token")
   b. Stores token:          localStorage.setItem("jwt", token)
   c. Strips token from URL: window.history.replaceState({}, "", "/")
   d. Redirects to:          / (home / map list page)
```

### 3.2 Token Storage

- Store as `"jwt"` key in `localStorage`
- **Never** log it, expose it in URLs again, or embed it in HTML

### 3.3 Attaching the Token to Every API Call

Create a shared Axios instance:

```ts
// src/api/client.ts
const api = axios.create({ baseURL: "/" });

api.interceptors.request.use(config => {
  const token = localStorage.getItem("jwt");
  if (token) config.headers["Authorization"] = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem("jwt");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);
```

### 3.4 Session Restore on App Load

On app startup, check for a stored JWT in `localStorage`. If one exists, call `GET /api/me` (see §5.1) to validate it and hydrate the user state. If it returns `401` or no token is present, the app runs in **guest mode** — do not redirect to `/login` automatically.

### 3.5 Logout

- `DELETE /api/me/session` (optional, backend can be stateless)
- Remove `"jwt"` from `localStorage`
- Redirect to `/login`

---

## 4. Pages & Routes

| Route | Page | Auth Required | Guest Behaviour |
|---|---|---|---|
| `/login` | Login page | No | — |
| `/auth/callback` | Token capture page (no UI) | No | — |
| `/editor` | Guest scratchpad editor | No | Full local editor; auto-saves to `localStorage` |
| `/` | Map list (dashboard) | **Yes** | Redirect to `/login` |
| `/maps/new` | Create & save new map | **Yes** | Redirect to `/login` |
| `/maps/:id` | Map editor (canvas) | **Yes** | Redirect to `/login` |
| `/maps/:id/versions` | Version history | **Yes** | Redirect to `/login` |
| `/maps/:id/settings` | Map settings (visibility, collaborators) | **Yes** | Redirect to `/login` |

> **`/editor` vs `/maps/new`:** `/editor` is the persistent guest scratchpad — no backend, no account needed. `/maps/new` is the authenticated "create and immediately save to backend" flow.

---

## 5. API Integration

All requests use the shared Axios instance with the `Authorization: Bearer` header.

### 5.1 User / Session

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/me` | Get current user info (validate token, restore session) |

**`/api/me` response shape:**
```json
{
  "id": "uuid",
  "githubLogin": "alice",
  "displayName": "Alice Smith",
  "avatarUrl": "https://avatars.githubusercontent.com/..."
}
```

> ⚠️ This endpoint **does not yet exist** in the backend. It must be added before frontend session restore can work. It reads the `User` from the JWT principal and returns the above fields.

### 5.2 Maps

| Method | Endpoint | Body | Purpose |
|---|---|---|---|
| `GET` | `/api/maps` | — | List accessible maps |
| `POST` | `/api/maps` | `{ title, initialMapJson }` | Create a map |
| `GET` | `/api/maps/:id` | — | Get map detail + content |
| `PUT` | `/api/maps/:id` | `{ mapJson, label? }` | Save map content |
| `DELETE` | `/api/maps/:id` | — | Delete map |
| `PUT` | `/api/maps/:id/visibility` | `{ visibility }` | Change visibility |

**Map summary shape (list items):**
```json
{
  "id": "uuid",
  "title": "NorthWingLobby",
  "visibility": "private",
  "ownerLogin": "alice",
  "role": "owner",
  "createdAt": "2026-03-11T10:00:00Z",
  "updatedAt": "2026-03-11T12:00:00Z"
}
```

### 5.3 Versions

| Method | Endpoint | Body | Purpose |
|---|---|---|---|
| `GET` | `/api/maps/:id/versions` | — | List commit history |
| `POST` | `/api/maps/:id/restore/:sha` | — | Restore to a commit |
| `POST` | `/api/maps/:id/checkpoint` | `{ label }` | Create a named tag |

**Version item shape:**
```json
{
  "sha": "abc123",
  "message": "[platform] alice saved: north wing lobby update",
  "date": "2026-03-11T11:30:00Z",
  "author": "alice"
}
```

### 5.4 Collaborators

| Method | Endpoint | Body | Purpose |
|---|---|---|---|
| `POST` | `/api/maps/:id/collaborators` | `{ userId, role }` | Add collaborator |
| `DELETE` | `/api/maps/:id/collaborators/:userId` | — | Remove collaborator |

`role` is one of: `"editor"` or `"viewer"`

---

## 6. Auth State (Global Store)

```ts
interface AuthState {
  user: { id: string; githubLogin: string; displayName: string; avatarUrl: string } | null;
  token: string | null;
  isLoading: boolean;
  isGuest: boolean;   // true when no valid token is present

  login: () => void;          // navigate to /auth/github
  logout: () => void;         // clear token + redirect to /login
  restoreSession: () => Promise<void>;  // call /api/me on app load; sets isGuest if no token
}
```

`isGuest` is derived: `isGuest = token === null`. Components use this flag to conditionally render "Login to save" prompts instead of save/share/history buttons.

---

## 7. Canvas / Map Editor

The map editor is shared between guest and authenticated users — only the persistence layer differs.

### 7.1 Shared (all users)
- Render the topological map on a canvas (nodes = rooms/waypoints, edges = corridors)
- Toolbar: add node, add edge, delete, pan, zoom
- **Auto-save to `localStorage`** under key `"guest-map-draft"` (debounced, ~2 s) — acts as a local crash-recovery buffer for both guest and authenticated users
- **Export** button: download current map as `map.json` file
- **Import** button: load a `map.json` file from disk into the canvas

### 7.2 Authenticated users only
- **"Save to cloud"** button → `PUT /api/maps/:id`; shows last-saved timestamp + "Saving..." indicator
- **"View History"** button → navigate to `/maps/:id/versions`
- On load at `/maps/:id`: fetch map content from `GET /api/maps/:id`; this overrides the `localStorage` draft

### 7.3 Guest users
- No save-to-cloud, share, or history buttons
- A persistent **banner** at the top of the editor:  
  _"You're editing locally — changes are not backed up. [Log in to save & share →]"_
- Clicking "Log in" navigates to `/login`
- After successful OAuth, detect `"guest-map-draft"` in `localStorage` and offer a **one-time prompt**: _"You have an unsaved local map. Upload it as a new map?"_ — confirming creates it via `POST /api/maps` and clears the draft

The `mapJson` format is defined by the `toponavi-core` and `toponavi-dsl` modules — the frontend must serialize/deserialize exactly that format.

---

## 8. Key UX Rules

| Rule | Detail |
|---|---|
| **GitHub is invisible** | Users never see repo names, commit SHAs (raw), or bot activity |
| **Token never re-appears in URL** | Use `history.replaceState` immediately after capture |
| **401 = auto-logout** | Any 401 from the API clears the token and redirects to `/login`, with a dev-only warning message in the browser console |
| **No forced login wall** | The app never blocks access on load; unauthenticated users land on `/editor`, not a login gate |
| **Protected routes redirect** | `/`, `/maps/*` routes redirect to `/login` if no valid JWT is present |
| **Disabled, not hidden** | For guest users, cloud-dependent controls are visible but disabled (or replaced by the banner prompt), so the UI shape is consistent |
| **Guest draft survives login** | After OAuth completes, a `"guest-map-draft"` in `localStorage` triggers an upload prompt before it is discarded |
| **Optimistic UI** | Show map changes immediately; roll back on API error |

---

## 9. ~~Backend Gap~~: `/api/me` Endpoint ✅

> **Implemented** in `AuthController.java`.

---

## 10. Out of Scope (This Version)

- Real-time collaborative editing (WebSocket)
- Mobile / responsive layout (desktop-first)
- i18n / localization
