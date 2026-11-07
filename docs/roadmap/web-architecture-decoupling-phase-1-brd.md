# TopoNavi — Architecture Decoupling (Phase 1)

**Business Requirements Document (BRD)**

- Version: 0.3
- Date: November 6, 2026
- Repository: `HarryLiGameTech/indoor-topo-navi`
- Priority: P0
- Status: Scope Agreed — Implementation Pending
- Technical design: [Phase 1 technical specification](web-architecture-decoupling-phase-1-tech-spec.md)

## 1. Background

TopoNavi is transitioning into an independent spatial-topology computation engine.

The current repository combines computation with GitHub-backed authentication, map storage, collaboration, and version-control functionality.

The current PostgreSQL model stores users, permissions, and map metadata. Legacy editor contents are stored in GitHub, while navigation loads TopoScript projects from a configured filesystem directory.

Phase 1 will eliminate GitHub-dependent platform features and the legacy `map.json` storage representation, and introduce PostgreSQL persistence for native TopoScript project sources. This is new source-storage functionality, not merely retention of an existing database-backed source store.

All stored projects used by the web module will be loaded through one database-backed project-loading path. `EXAMPLES_PATH` and its filesystem-backed runtime map inventory will be removed. Source directories may remain as examples, test fixtures, and explicit import inputs.

The future editor backend, collaboration features, and version-control system will be developed together in a separate repository.

## 2. Business Objectives

**BR-01 — GitHub Independence**

TopoNavi must compile, query, and manage projects without requiring GitHub OAuth or GitHub App integration.

**BR-02 — Unified Project Representation**

TopoScript source files must become the canonical persisted project format. The legacy `map.json` format will be retired.

**BR-03 — Basic Persistence**

TopoNavi must introduce project-level CRUD operations backed by PostgreSQL for native TopoScript sources.

**BR-04 — Separation of Responsibilities**

TopoNavi must not provide editor-specific business logic, collaborative workflows, or version-control functionality.

Generic source validation, parameter discovery, compilation, routing, and topology queries remain engine responsibilities, including when an editor calls these capabilities. Compilation `userParams` describe the compilation context and are not user accounts or ownership records.

**BR-05 — Computational Stability**

Existing compiler, routing, validation, and topology-query capabilities must remain functional.

Retained building and computation APIs must remain backward compatible, including existing `buildingId` values and request/response contracts. This requirement does not preserve the explicitly retired platform APIs listed in Sections 7 and 9.

## 3. Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-01 | Remove GitHub OAuth and GitHub App integration | Must |
| FR-02 | Remove GitHub-dependent collaboration functionality | Must |
| FR-03 | Remove GitHub commit history, checkpoint and restore capabilities | Must |
| FR-04 | Retire legacy `map.json` storage and its associated data model | Must |
| FR-05 | Persist native TopoScript project files in PostgreSQL | Must |
| FR-06 | Provide generic project-level CRUD operations for stored TopoScript sources | Must |
| FR-07 | Retain independent project compilation and routing APIs | Must |
| FR-08 | Remove user-specific editor and collaboration backend logic | Must |
| FR-09 | Require service-level Bearer authentication for all project CRUD operations and authenticate computation by default, with explicit anonymous-computation configuration | Must |
| FR-10 | Assess and archive existing PostgreSQL and GitHub-backed legacy map data | Deferred |
| FR-11 | Update tests, documentation, configuration, and deployment dependencies | Must |
| FR-12 | Remove `EXAMPLES_PATH` and load all stored projects through the unified database-backed path | Must |
| FR-13 | Preserve building lookup by existing `buildingId` values and the contracts of retained building and computation APIs | Must |
| FR-14 | Preserve generic source validation and parameter discovery, including `/api/v1/authoring/validate` | Must |
| FR-15 | Allow projects that cannot currently compile to be created, read, and updated | Must |

## 4. Canonical Project Data

A persisted project represents a collection of TopoScript source files, rather than an independent JSON map definition.

Supported file types include:

- `configuration` / `configuration.tcfg`
- `root`
- `.tmap` files
- `.ttr` files
- Extensionless legacy TopoScript source files where supported by the compiler

The database shall preserve file paths and contents without converting them into a second topology schema.

**Recommended logical storage model**

`projects`

- `id`: UUID
- `building_id`: String, unique and stable external identifier used by existing building APIs
- `name`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

`project_files`

- `project_id`: UUID
- `path`: String
- `content`: Text

A project is uniquely identified internally by its UUID and is also resolvable by its unique external `buildingId`. Existing identifiers such as `swfc` must continue to resolve to the corresponding imported projects; clients must not be required to replace them with UUIDs. The display name is separate from both identifiers. Each file path must be unique within that project.

Project metadata and source files must remain logically distinct from compiled navigation graphs.

Stored source files may be incomplete or fail parsing, type checking, or compilation. Persistence does not certify that a project is currently usable for navigation.

## 5. Project CRUD

TopoNavi shall provide the following minimum capabilities:

| Operation | Behavior |
|---|---|
| Create | Create a TopoScript project |
| Read | Retrieve metadata and source files |
| Update | Replace or update project source files |
| Delete | Delete a project and its stored source files |
| List | Enumerate available stored projects |

Project modifications must be performed atomically.

Create and update operations validate the storage envelope, including file paths, content types, and configured size limits. They must not require successful compilation or a particular set of `userParams`. Generic validation remains a separate operation. Computation against invalid stored sources must report failure through the retained API error contract.

The CRUD API is a generic storage interface. It must not include editor UI state, collaborative editing sessions, presence tracking, conflict-resolution workflows, or Git-like commits.

The legacy `/api/maps` platform interfaces may be removed and replaced by `/api/v1/projects` storage interfaces. This explicit retirement must be documented for legacy platform clients. It does not permit breaking changes to retained building and computation APIs.

## 6. Authentication and Authorization

GitHub OAuth and GitHub-based identity mapping will be removed.

During Phase 1:

- All project CRUD operations, including listing projects and reading source files, shall require service-level Bearer authentication.
- The service credential must be configured securely.
- No multi-user permission or sharing model is required.
- Computation endpoints shall require the service credential by default. Anonymous computation may be enabled only by explicit configuration.
- Anonymous building discovery and building-scoped computation must honor the explicitly configured accessible buildings. Such access does not grant access to project CRUD or source downloads.
- The generic validation endpoint may accept anonymous requests only when explicitly enabled; health checks remain public.
- GitHub-derived platform session tokens are replaced by the service credential. Retained clients continue to use the `Authorization: Bearer ...` transport with the new configured credential.

Unauthenticated users must not receive unrestricted access to the project database.

## 7. Removed Functionality

The following platform functionality will be removed from the current repository:

- GitHub OAuth login and callback.
- GitHub App authentication and repository management.
- GitHub-backed file storage.
- Legacy `map.json` and `metadata.json` storage representation.
- User collaboration and collaborator invitations.
- Per-user map ownership and editor/viewer permission management.
- Git commit history, restoration, and checkpoints.
- Editor-specific business logic.

GitHub Actions and GitHub Pages used for development, testing, and documentation publication are unaffected.

Generic `/api/v1/authoring/validate` functionality is retained. Its name or use by an editor does not make it editor-specific business logic.

## 8. Out of Scope

- New editor frontend or backend.
- Collaborative editing infrastructure.
- User registration and account management.
- Project version-control services.
- Real-time synchronization.
- Migration of all project persistence into the future collaboration platform.
- New TopoScript grammar or compiler semantics.
- Complete removal of PostgreSQL or state from `toponavi-web`.
- Assessment, backup, and archival of existing deployed PostgreSQL and GitHub-backed legacy map data; this work is deferred.

## 9. Migration and Compatibility

### 9.1 Retained API Compatibility

The following existing APIs must retain their paths, HTTP methods, request/response contracts, and computation semantics:

- `GET /api/v1/buildings`, including its building-ID list response.
- Building-scoped navigation and topology APIs under `/api/v1/buildings/{buildingId}/...`.
- `POST /api/v1/authoring/validate`.
- `GET /api/v1/health`.

Building lookup must resolve existing external identifiers through the database-backed project store. Moving sources out of the filesystem must not require MCP or other clients to adopt UUIDs or new computation endpoints. The agreed service-credential transition is described in Section 6.

The compatibility guarantee excludes the explicitly retired `/api/maps`, GitHub login, user-profile, collaboration, and version-control platform APIs. Their removal is permitted and must be documented.

### 9.2 Native Source Import

Existing TopoScript source projects may be explicitly imported into PostgreSQL with their relative file paths, contents, and existing `buildingId` preserved. Imports use the same storage checks as project creation; unsuccessful compilation does not prevent storage. Projects needed for existing navigation behavior must be imported and their computational regression checks must pass before switching that deployment to database-backed loading.

The web module must not retain `EXAMPLES_PATH` as a second live source or a fallback when a database project is missing. Request-supplied sources for generic validation remain supported and need not be persisted.

### 9.3 Deferred Legacy Data Work

Assessment, backup, and archival of existing PostgreSQL legacy records and GitHub-backed map contents are deferred. Their completion is not a Phase 1 code-acceptance gate, and Phase 1 must not claim that archival or live legacy-data migration has been verified.

Legacy `map.json` must not be automatically interpreted as a valid TopoScript project.

Removing runtime code and endpoints does not authorize deleting deployed legacy records or GitHub repositories. A later legacy-data migration or decommissioning task must establish the actual inventory and the required backup/archive procedure. Native TopoScript source import remains part of this phase and is distinct from this deferred legacy-data work.

## 10. Nonfunctional Requirements

- Core computation must remain independent of persistence.
- Source files must survive application restarts.
- The normal persisted deployment must use PostgreSQL with durable storage; an in-memory test database does not satisfy this requirement.
- Updates must be atomic and consistent.
- Compilation must consume one consistent project source snapshot. Source changes must not reuse an incompatible compiled cache entry.
- Project file paths must be validated against invalid or unsafe paths.
- Compilation must remain isolated across independent requests.
- Unauthorized project mutations must be rejected.
- GitHub credentials must not be needed at runtime.
- Existing route planning behavior must remain unchanged.
- Retained building and computation APIs must remain compatible while their stored source-loading path changes.

## 11. Acceptance Criteria

- [ ] The application starts without GitHub OAuth and GitHub App credentials.
- [ ] No runtime GitHub API dependency remains.
- [ ] Legacy `map.json` handling is removed.
- [ ] PostgreSQL preserves all submitted project source paths and contents, including incomplete or non-compiling projects.
- [ ] Project CRUD functions correctly.
- [ ] Invalid storage envelopes are rejected independently of compilation validity.
- [ ] Stored source files survive application restarts in the PostgreSQL deployment.
- [ ] A stored project can be loaded into the existing compilation workflow.
- [ ] `EXAMPLES_PATH` and filesystem-backed runtime map loading are removed; all stored projects use the unified database-backed loading path.
- [ ] Existing native source projects required by the deployment are imported with their existing `buildingId` values preserved.
- [ ] Existing building lookup, catalog, routing, topology-query, and validation API contracts remain compatible, including for MCP clients.
- [ ] All project CRUD requests, including list and source reads, are protected by the agreed service credential.
- [ ] Computation requires service authentication by default, and explicitly configured anonymous access is tested.
- [ ] Generic source validation and parameter discovery remain available independently of project persistence.
- [ ] Collaboration and version-control endpoints are removed.
- [ ] Existing compiler and navigation regression tests pass.
- [ ] Deferred legacy-data assessment and archival are documented without claiming completion or deleting deployed legacy data.
- [ ] The new architectural responsibilities are documented.

## 12. Implementation Estimate

The following is the original v0.2 estimate, retained for reference only. It has not been re-estimated for the confirmed v0.3 scope: unified database-backed loading, strict retained-API compatibility, non-compiling project persistence, and deferred legacy-data archival. It is not a revised Phase 1 commitment.

| Workstream | Estimated Effort |
|---|---:|
| Remove GitHub integration | 5–8 hours |
| Replace legacy map model with TopoScript project storage | 8–14 hours |
| Simplify CRUD API and security | 5–9 hours |
| Data migration, tests and documentation | 7–12 hours |
| **Total** | **25–43 hours** |

## 13. Future Architecture

The new collaboration platform will eventually own editor workflows, user identities, collaborative projects, and version history.

TopoNavi will remain the computational source of truth for TopoScript compilation and navigation semantics.

The project persistence layer retained in Phase 1 may subsequently become an internal adapter, a service-owned project store, or be extracted into the collaboration platform.

This future migration is not required for Phase 1 completion.

## 14. Confirmed Scope Decisions (November 6, 2026)

- Introduce database storage for native project sources; the current database does not already provide this capability.
- Remove editor-specific business features while preserving generic validation, parameter discovery, compilation, and computation parameters.
- Remove `EXAMPLES_PATH`; all stored-map reads use one database-backed project-loading path.
- Preserve existing external `buildingId` lookup and all retained building/computation API contracts. Explicitly retired platform APIs may be removed.
- Protect all project CRUD with a service Bearer credential; protect computation by default and permit anonymous computation only by explicit configuration.
- Permit storing incomplete or non-compiling projects; validate storage separately from compilation.
- Defer assessment and archival of actual legacy data. Do not treat that deferral as authorization to delete legacy data or as evidence that it has been migrated.
