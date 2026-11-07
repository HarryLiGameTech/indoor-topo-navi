# Synology Container Manager deployment

This deployment runs `toponavi-web` and a PostgreSQL 16 database on private
Docker networks. It does not expose the TopoNavi API or the database to the NAS
LAN or the public Internet. The Agent backend reaches the API as
`http://toponavi-web:8080`.

Map projects are stored in PostgreSQL. The container does not read a map
directory: after the first start, import each project explicitly (step 5). No
GitHub credentials, OAuth application, or GitHub network access are required.

## 1. Prepare the shared network

Create the network once from an SSH session on the NAS:

```bash
sudo docker network create toponavi-agent-net
```

If it already exists, leave it in place. Both the TopoNavi project and the Agent
project must join this exact network.

## 2. Copy the repository

Copy the complete repository to a persistent shared folder, for example:

```text
/volume1/docker/indoor-topo-navi
```

The image build requires these paths to remain together:

```text
Dockerfile
docker-compose.yml
gradlew
gradle/
toponavi-core/
toponavi-dsl/
toponavi-web/
```

`examples/` and `tools/` are only needed on the machine that runs the importer;
the running container never reads them.

## 3. Configure secrets

Create `.env` next to `docker-compose.yml` (see `.env.example`). Compose refuses
to start when either required value is missing.

```env
TOPONAVI_DB_PASSWORD=<strong database password>
TOPONAVI_SERVICE_TOKEN=<service credential>
```

Generate the service credential once, for example with
`python3 -c "import secrets; print(secrets.token_urlsafe(32))"`. It must be at
least 32 characters; the service does not start otherwise. Keep `.env` out of
version control and never put the credential in a URL.

Optional settings keep their existing meaning: `TOPONAVI_ANONYMOUS_BUILDINGS`
(comma-separated exact building IDs computable without the credential) and
`TOPONAVI_ALLOW_GUEST_AUTHORING`.

## 4. Create the Container Manager project

In DSM, open **Container Manager > Project > Create** and use:

- Project name: `indoor-topo-navi`
- Path: `/volume1/docker/indoor-topo-navi`
- Source: the repository's `docker-compose.yml`
- Web portal: disabled

Build and start the project. The first build downloads the JDK, Gradle, and
Maven dependencies, so it can take several minutes on a NAS. The database starts
first; `toponavi-web` waits until it reports ready, then creates its
`toponavi_projects` schema on first start.

Project data lives in the named volume `indoor-topo-navi-db-data`
(`TOPONAVI_DB_VOLUME` overrides the name). Rebuilding or recreating containers
keeps it; deleting the volume deletes every stored project. Back it up with your
usual PostgreSQL tooling (for example `pg_dump`).

To use an existing PostgreSQL server instead, set `SPRING_DATASOURCE_URL`, and
`TOPONAVI_DB_USER`/`TOPONAVI_DB_PASSWORD` for its credentials. The service only
creates and migrates its own `toponavi_projects` schema; it never alters,
baselines, or deletes other schemas or legacy platform tables. The database must
use UTF-8 encoding.

## 5. Import map projects

From an SSH session on the NAS, in the repository folder, import every project
the deployment should serve, using its existing building ID exactly:

```bash
export TOPONAVI_SERVICE_TOKEN='<service credential>'
for building in indigoBJ nbc4 swfc trent; do
  python3 tools/import_toposcript_project.py --base-url http://127.0.0.1:8080 \
    --directory "examples/$building" --building-id "$building" \
    --manifest imports.jsonl || break
done
```

Each import is its own transaction; a batch is not atomic. The loop stops at the
first failure, and `imports.jsonl` keeps one record (UUID, building ID, file
count, and content digest) per successful import.

- `--dry-run` validates locally and prints the manifest without contacting the
  service or needing the credential.
- `--name` sets the display name (default: the building ID).
- An existing building ID is never overwritten unless you pass `--replace`, which
  fully replaces that project's name and source set.
- The importer reads only `configuration`, `configuration.tcfg`, `root`, `.tmap`,
  `.ttr`, and other extensionless files, preserving bytes and line endings.
  Markdown, JSON (including legacy `map.json` editor state), and other files are
  reported as skipped and never converted. Symbolic links are rejected.
- After each import it reads the project back and compares every path and source.

Incomplete or non-compiling sources are stored as submitted. A stored project
appears in `/buildings` only when it has a root `configuration` or
`configuration.tcfg` file.

## 6. Verify

By default the Compose file binds the host port only to NAS loopback. From an
SSH session on the NAS, run:

```bash
curl --fail http://127.0.0.1:8080/api/v1/health
```

Expected response:

```json
{"service":"toponavi-web","status":"ok"}
```

The container should also become `healthy` in Container Manager. Health is
liveness only; it does not check the database or any project. Also confirm the
catalog, a topology query, and a representative route for each imported
building:

```bash
curl -sS http://127.0.0.1:8080/api/v1/buildings -H "Authorization: Bearer $TOPONAVI_SERVICE_TOKEN"
curl -sS http://127.0.0.1:8080/api/v1/buildings/indigoBJ/maps/query \
  -H "Authorization: Bearer $TOPONAVI_SERVICE_TOKEN" -H 'Content-Type: application/json' --data-binary '{}'
```

Restarting the project, or the NAS, must leave `GET /api/v1/projects` and the
catalog unchanged.

## 7. Connect TopoNavi Agent

Join the Agent backend to `toponavi-agent-net` and configure:

```env
TOPONAVI_API_BASE_URL=http://toponavi-web:8080
TOPONAVI_API_TOKEN=<the same value as TOPONAVI_SERVICE_TOKEN>
```

The MCP child process inherits these values from the Agent backend and sends the
token as a Bearer credential. Platform JWTs are no longer accepted. The MCP
server reads the building catalog at startup, so restart the Agent backend after
importing, adding, or deleting buildings. Do not create an frp proxy for
`toponavi-web`; only the Agent backend is an external entry.

## Upgrading from the directory-mounted release

- `EXAMPLES_PATH`, `PLATFORM_EXAMPLES_PATH`, and the `/maps` bind mount no longer
  install maps. They are ignored; nothing falls back to a directory. Import the
  projects (step 5) before switching clients to the new version.
- The `local` Spring profile no longer uses an in-memory H2 database; it expects
  PostgreSQL. Compose does not activate it.
- GitHub OAuth/App settings, `JWT_SECRET`, and `FRONTEND_URL` are no longer used
  and can be removed. The GitHub-backed editor endpoints (`/auth/github`,
  `/api/me`, `/api/maps/**`) return 404.
- Legacy platform tables (users, permissions, map metadata) are left untouched;
  assessing and archiving them is a separate task.
- Prepare and verify the new version against its own database before directing
  clients to it. The previous image and its source directory remain a rollback
  target, but projects created or replaced through the new API are not copied
  back to that directory. Export those sources (`GET /api/v1/projects/{id}`)
  before rolling back, or accept the divergence.

## Notes

- `127.0.0.1:8080` is for NAS-side diagnostics only. Other containers use
  `toponavi-web:8080` over the private Docker network. The database has no
  published port.
- To accept connections from other machines, set `TOPONAVI_HOST_BIND=0.0.0.0`
  (and optionally `TOPONAVI_HOST_PORT`) in `.env`, then recreate the project.
  The API is plain HTTP, so the service token crosses the network
  unencrypted; prefer a private network or a TLS reverse proxy. Published
  Docker ports bypass host firewall rules such as `ufw`.
- Compiled models are cached under the container user's home directory. The
  cache is disposable and is not a source of truth.
