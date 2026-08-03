# Synology Container Manager deployment

This deployment runs `toponavi-web` as one container on a shared private Docker
network. It does not expose the TopoNavi API to the NAS LAN or the public
Internet. The Agent backend reaches it as `http://toponavi-web:8080`.

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

The deployment requires these paths to remain together:

```text
Dockerfile
docker-compose.yml
gradlew
gradle/
toponavi-core/
toponavi-dsl/
toponavi-web/
examples/
```

## 3. Create the Container Manager project

In DSM, open **Container Manager > Project > Create** and use:

- Project name: `indoor-topo-navi`
- Path: `/volume1/docker/indoor-topo-navi`
- Source: the repository's `docker-compose.yml`
- Web portal: disabled

Build and start the project. The first build downloads the JDK, Gradle, and
Maven dependencies, so it can take several minutes on a NAS.

## 4. Verify

The Compose file binds the diagnostic host port only to NAS loopback. From an
SSH session on the NAS, run:

```bash
curl --fail http://127.0.0.1:8080/api/v1/health
```

Expected response:

```json
{"service":"toponavi-web","status":"ok"}
```

The container should also become `healthy` in Container Manager.

## 5. Connect TopoNavi Agent

Join the Agent backend to `toponavi-agent-net` and configure:

```env
TOPONAVI_API_BASE_URL=http://toponavi-web:8080
```

The MCP child process inherits this value from the Agent backend. Do not create
an frp proxy for `toponavi-web`; only the Agent backend is an external entry.

## Notes

- The default `local` Spring profile uses an in-memory H2 database. This is
  sufficient for the route-engine demonstration but does not persist map-store
  accounts or management data.
- `examples/` is mounted read-only at `/maps`. Replace map files on the NAS and
  restart the container when a new recording map version is frozen.
- `127.0.0.1:8080` is for NAS-side diagnostics only. Other containers use
  `toponavi-web:8080` over the private Docker network.
