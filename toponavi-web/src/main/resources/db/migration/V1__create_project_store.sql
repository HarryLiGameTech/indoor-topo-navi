-- Native TopoScript project sources. Flyway creates and owns only the toponavi_projects schema and its
-- schema-history table; legacy platform tables in other schemas are never touched by these migrations.

CREATE TABLE toponavi_projects.projects (
    id          uuid PRIMARY KEY,
    building_id varchar(255) COLLATE "C" NOT NULL,
    name        varchar(255) NOT NULL,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz NOT NULL,
    CONSTRAINT projects_building_id_unique UNIQUE (building_id),
    CONSTRAINT projects_building_id_format
        CHECK (building_id ~ '^[A-Za-z0-9][A-Za-z0-9._-]{0,254}$'),
    CONSTRAINT projects_name_nonblank CHECK (length(btrim(name)) > 0)
);

-- Exact lookup stays case-sensitive, but case-only duplicates are rejected so case-insensitive clients
-- (such as the MCP building catalog) never see two IDs that differ only by case.
CREATE UNIQUE INDEX projects_building_id_folded_unique
    ON toponavi_projects.projects (lower(building_id));

CREATE TABLE toponavi_projects.project_files (
    project_id uuid NOT NULL
        REFERENCES toponavi_projects.projects(id) ON DELETE CASCADE,
    path       text COLLATE "C" NOT NULL,
    content    text NOT NULL,
    PRIMARY KEY (project_id, path),
    CONSTRAINT project_files_path_length
        CHECK (octet_length(path) BETWEEN 1 AND 512)
);
