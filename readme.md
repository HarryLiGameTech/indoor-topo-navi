# TopoNavi: An Indoor Navigation Engine with Collaborative Topological Mapping

## Project Overview

TopoNavi is a indoor navigation engine built around **TopoScript**, a purpose-designed DSL (Domain-Specific Language) for describing the topological structure of buildings. Authors define floor maps, nodes, walking paths, and vertical transport (elevators, escalators, staircases) using plain-text TopoScript files. The engine compiles these files into an in-memory navigation graph and computes optimal multi-floor routes on demand.

The system is split into three layers: a **core graph model** (Scala), a **DSL compiler and navigation API** (Scala, built on ANTLR 4), and a **REST web service** (Spring Boot / Java) that exposes compilation, route planning, and map inspection endpoints. A compilation cache prevents re-parsing unchanged map sets on repeated requests.

Two real-world example maps are included: `swfc` (Shanghai World Financial Center — 100+ floors, 60+ elevator/escalator lines) and `trent` (The administrative building of UNNC), demonstrating how TopoScript scales from a single multi-floor building to a complex high-rise.

---

## Key Features

- **Multi-Floor Indoor Navigation**: Guides users through complex multi-floor indoor environments such as malls and office buildings. Navigation spans across floors via elevators, escalators, and staircases, stitched together by intra-floor walking paths.
- **Collaborative Topological Mapping**: Multiple contributors can author and maintain independent TopoScript submap files. A `configuration` file assembles them into a complete building, enabling low-cost incremental map updates without rewriting the whole structure.
- **TopoScript DSL**: A human-readable, declarative language for defining floor topology. Authors declare named nodes, weighted walking paths (`atomic-path`), and transport definitions (`transport … is Elevator/Stairs/Escalator`) with per-station location parameters — **no GIS tools or coordinate systems required**.
- **Flexible Route Planning Preferences**: **3 planning modes are supported** — `MinimizeTime` (shortest total travel time), `MinimizeTransfers` (fewest floor changes, with time as a tiebreaker), and `MinimizePhysicalDemands` (prefers elevators over staircases, weighted by floor distance).
- **Submap Reuse via Templating**: Identical floor layouts (e.g. a standard hotel guest-room floor) can be written once as a template and reused across many floors with a single `using` directive in the `configuration` file, eliminating repetition.
- **Compilation Cache**: The web layer caches compiled `CompilationResult` objects on disk. Repeated navigation requests against the same map set skip re-compilation entirely.

---

## Tech Stack

| Layer | Technology |
|---|---|
| DSL grammar | ANTLR 4 (generated lexer + parser) |
| Compiler & navigation engine | Scala 3.3 |
| Graph algorithms | Custom A* on `NavigationGraph` and `TransportGraph` |
| Functional utilities | Cats Effect 3 |
| Web service | Spring Boot 3 (Java) |
| Persistence | PostgreSQL + Spring Data JPA |
| Authentication | GitHub OAuth 2.0 + JWT |
| Collaborative map storage | GitHub App API (org-hosted repositories) |
| Build system | sbt (Scala modules) + Gradle (web module) |
| Testing | ScalaTest `AnyFunSuite`, manual `App`-based testers |

---

## Quick Start Guide

### 1. One-Click Service Deployment (Linux)

First, edit `toponavi-web/src/main/resources/application.yml` to set `examples-path` — the path to the TopoScript map directory the service should load on startup (e.g. `../examples/trent`).

If you want to enable collaborative map editing (GitHub-backed map storage and OAuth login), copy `.env.example` to `.env` and fill in your GitHub App credentials, installation ID, organisation name, and a public-facing domain. You will need to register your own GitHub App and configure a public domain for OAuth callbacks.

Then start the service with:

```bash
./run.sh
```

The REST API will be available at `http://localhost:8080/api/v1`. Key endpoints:

- `GET /api/v1/quick-demo-navigation?startNode=<A>&endNode=<B>&routePlanningPreference=<pref>` — navigate between two nodes
- `GET /api/v1/quick-demo-available-submaps` — list all compiled submaps
- `GET /api/v1/quick-demo-all-available-nodes` — list all nodes (optionally with attributes)
- `POST /api/v1/validate` — validate a set of TopoScript files without navigating

### 2. Navigation Quick-Demo (no server required)

To try out route planning directly without deploying the web service:

**Step 1 — Generate the test cache** (compiles the example map and saves the result to `~/.toponavi/`):

```bash
# From the toponavi-dsl module directory, run via sbt:
sbt "testOnly TesterCacheGenerator"
```

This produces `~/.toponavi/tester_swfc` and `~/.toponavi/tester_trent`.

**Step 2 — Run a navigation test**:

```bash
# SWFC high-rise example:
sbt "runMain SwfcRoutePlanningTester"

# Trent building example (includes diagnostic output):
sbt "runMain TrentRoutePlanningTester"
```

Both testers load the cached `CompilationResult`, run a sample route query, and print a step-by-step navigation plan to stdout.

---

## TopoScript Syntax at a Glance

**Floor map file** (e.g. `LowerLobby`):
```
topo-map LowerLobby() {
    topo-node entrance_main
    topo-node lobby_center
    topo-node elevator_hall {description = "Main elevator lobby"}

    atomic-path [entrance_main <-> lobby_center] {cost = 10}
    atomic-path [lobby_center <-> elevator_hall] {cost = 5}
}
```

**Elevator transport file** (e.g. `MainElevator`):
```
transport MainElevator is Elevator {
    let params: {maxSpeed: Float, acceleration: Float, cars: Int} = {
        maxSpeed = 2.0,
        acceleration = 1.0,
        cars = 3
    }
    station B1  at FloorB1::elevator_hall    {location = -5.0, departureRate = 0.05}
    station G   at LowerLobby::elevator_hall {location = 0.0,  departureRate = 0.9}
    station F3  at Floor3::elevator_hall     {location = 12.0, departureRate = 0.05}
}
```

**Staircase transport file** (e.g. `ST_LLT`):
```
transport ST_LLT is Stairs {
    let params: {turnBackCost: Int} = { 
        turnBackCost = 3 
    }
    station F3 at Floor3::Stair_LLT        {location = 8.0, directSegmentIndex = 4}
    station F2 at Floor2::room_201_out_1   {location = 3.5, directSegmentIndex = 2}
    station F1 at Floor1::stair_LLT        {location = 0.0, directSegmentIndex = 0}
}
```

**Configuration file** (`configuration`):
```
building-includes {
    submap Floor5 using StandardFloor   // reuse a template
    submap Floor4 using StandardFloor   // reuse a template
    submap Floor3
    submap Floor2
    submap Floor1
    
    vehicle MainElevator
    vehicle ST_LLT
}
```

## License
This project is licensed under the [Apache License 2.0](LICENSE).