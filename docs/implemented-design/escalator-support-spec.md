# Escalator Support Spec

Status: Accepted for implementation

## Context

TopoScript already parses `transport ... is Escalator` declarations, and the
`indigoBJ` example registers three escalators in its building configuration.
Compilation currently stops in `TopoScriptCompiler.buildLinearTransport` with:

```text
Escalator building logic not yet implemented
```

The missing piece is the conversion from the already elaborated
`TransportValue` into a concrete `LinearTransport`. Elevator and staircase
builders cannot be reused directly because they require station data that the
existing escalator declarations intentionally do not contain.

This work is time-sensitive. The implementation must follow the minimum-change
principle and prioritize predictable delivery over a complete physical model of
real-world escalators.

## Goals

- Implement `buildEscalator` and remove the current escalator compilation
  failure.
- Compile the existing `indigoBJ` escalator files without changing their map
  schema or data.
- Model an escalator as a fixed-time, bidirectional `LinearTransport`.
- Preserve existing station permission behavior.
- Keep REST and MCP navigation response contracts unchanged.
- Prove the result locally with unit, compiler, exact-node route, REST, and MCP
  tests.

## Non-Goals

- Modeling an escalator's real physical direction.
- Adding `up`, `down`, or runtime direction changes to TopoScript.
- Modeling queues, waiting time, velocity, acceleration, capacity, outages, or
  congestion.
- Generalizing the transport-discovery endpoint beyond elevators.
- Refactoring elevator or staircase compilation.
- Changing the route response schema or adding an escalator-specific step type.
- Testing the AI Agent, Agent Runtime, NAS deployment, or public-network path.
- Changing existing `indigoBJ` map files unless an independently verified map
  defect blocks an acceptance route.

## Accepted Behavior

### TopoScript declaration

The existing declaration shape remains valid:

```toposcript
transport CentralEscalator is Escalator {
    let params: {time: Int} = {
        time = 30
    }

    let displayName: String = "The escalator in the central part of the mall"

    station L2 at Level2::shop_248_outside {}
    station L1 at Level1::mid_bridge_inner_end {}
}
```

No `location`, `departureRate`, `directSegmentIndex`, velocity, or acceleration
field is required.

### Fixed travel time

`params.time` is the complete travel time in seconds between the two escalator
stations.

- `time` is required.
- `Int` and `Float` values are accepted to match existing compiler conventions.
- The value must be finite and strictly greater than zero.
- For two different stations, both `netTimeBetweenStations` and
  `travelTimeBetweenStations` return exactly `time`.
- A same-station query returns `0`.
- There is no additional waiting cost.

Missing, non-numeric, non-finite, zero, and negative values must stop
compilation with a descriptive runtime exception that includes the escalator
identifier and identifies `params.time` as the invalid field.

Recommended messages:

```text
Escalator '<identifier>' must contain numeric 'params.time'
Escalator '<identifier>' params.time must be finite and greater than 0
```

### Stations and direction

The first implementation supports exactly two stations per escalator. This
matches every current `indigoBJ` escalator and avoids inventing ambiguous
multi-stop `time` semantics.

- Fewer or more than two stations cause a descriptive compilation exception.
- Both stations must resolve to existing nodes in two distinct submaps.
- Travel is bidirectional by default and has the same cost in both directions.
- TopoScript declaration order does not imply direction.
- Directional escalators are deferred to a later specification.

Recommended station-count message:

```text
Escalator '<identifier>' must have exactly 2 stations
```

### Station permissions

The builder must read the elaborator-injected `_permission` field in the same
way as `buildElevatorBank`:

- absent or unrecognized: `FullyGranted`
- `NoAccess`: neither arrival nor departure
- `ArriveOnly`: arrival only
- `DepartOnly`: departure only

`canArriveAt` and `canDepartFrom` must also return false for a graph that is not
a station of this escalator.

### Route representation

Escalator traversal remains a normal `RouteEdgeCategory.Transport` edge.
Existing route descriptions continue to use:

```text
Take <escalator identifier> from <source graph> to <destination graph>
```

No REST or MCP response field changes are permitted. In particular, structured
steps continue to report `type: "Transport"`; downstream clients may recognize
the escalator from its identifier or description.

## Minimal Implementation Design

### Core model

Add one concrete `LinearTransport` implementation, preferably named
`Escalator`, alongside `StairCase` and `ElevatorBank` in:

```text
toponavi-core/src/main/scala/data/LinearTransport.scala
```

It should contain only the data required by the existing trait:

- `identifier`
- `stationNodes`
- `stationLocations`
- `stationPermissions`
- fixed `travelTimeSeconds`
- `stationLabels`
- optional `displayName`

Use synthetic topological station locations `0.0` and `1.0`, assigned from
declaration order. These are internal ordering values, not physical meters.
`distanceBetweenStations` therefore returns `0.0` for the same station and
`1.0` for opposite stations. Set `maxVelocity` and `acceleration` to `0.0`.

Do not introduce a new general transport abstraction or modify elevator and
staircase behavior in this delivery.

### DSL compiler

In:

```text
toponavi-dsl/src/main/scala/compiler/TopoScriptCompiler.scala
```

replace the current exception branch with:

```scala
case "Escalator" => buildEscalator(transVal, graphs)
```

`buildEscalator` must:

1. Read the `params` record from the transport context.
2. Extract and validate positive, finite numeric `time`.
3. Require exactly two station declarations.
4. Resolve both referenced submaps and nodes using the same failure behavior as
   existing transport builders.
5. Reject two stations that collapse to the same graph key.
6. Assign internal locations `0.0` and `1.0`.
7. Copy station labels and `displayName` through existing compiler helpers.
8. Decode `_permission` consistently with `buildElevatorBank`.
9. Return the new core escalator instance.

Small local duplication of permission decoding is acceptable for this
deadline. A cross-builder refactor is explicitly not required.

### Files expected to change during implementation

Required production changes are limited to:

```text
toponavi-core/src/main/scala/data/LinearTransport.scala
toponavi-dsl/src/main/scala/compiler/TopoScriptCompiler.scala
```

Focused test files may be added or extended under:

```text
toponavi-core/src/test/scala/
toponavi-dsl/src/test/scala/
```

No production change is expected in `toponavi-web` or
`toponavi-mcp-server`.

## Local Test Plan

All tests in this specification are run on the development Mac. NAS sync,
container restart, and NAS verification are owned by the project maintainer and
are outside this implementation's definition of done.

### 1. Core unit tests

Add focused tests for the new `LinearTransport` implementation:

- forward travel returns the configured fixed time;
- reverse travel returns the same fixed time;
- net time and full travel time are identical;
- same-station travel returns zero;
- distance is zero for the same station and one for opposite stations;
- `FullyGranted`, `ArriveOnly`, `DepartOnly`, and `NoAccess` are respected;
- unknown graphs cannot be used for arrival or departure.

### 2. DSL compiler tests

Add focused compiler tests proving that:

- a valid two-station escalator compiles to the new core escalator type;
- identifier, nodes, labels, display name, permissions, and time survive
  compilation;
- travel works in both directions regardless of declaration order;
- missing `time` fails compilation;
- non-numeric `time` fails compilation;
- `time = 0` fails compilation;
- a negative `time` fails compilation;
- a non-finite floating-point time fails compilation if the surface language
  can express one;
- fewer or more than two stations fail compilation;
- two station declarations resolving to the same submap fail compilation.

### 3. Exact-node `indigoBJ` route acceptance

Compile the real `examples/indigoBJ` project and request all routes with
`MinimizeTime`. Assertions must inspect structured route edges or structured
steps, not only formatted prose.

| Case | Exact start | Exact destination | Required result |
|---|---|---|---|
| Two-escalator mall route | `Level1::mid_gate_inside` | `Level3::shop_341_outside` | Success; transport identifiers are exactly `CentralEscalator`, then `Escalator_18`, in that order; no other transport is used; both transport costs equal `30` seconds. |
| McDonald's to cinema | `Level3::shop_359_outside` | `Penthouse::check_in_gate_outside` | Success; exactly one transport edge is used; it is `PenthouseEscalator`; its cost equals `35` seconds. |
| Starbucks to subway | `LowerGround::shop_036` | `LowerGround::subway_station` | Success; no transport edge is used; the structured response contains exactly one step and that step is `Walk` or `WalkStraight`. |

The first case deliberately ends at `shop_341_outside`, the L3 station of
`Escalator_18`, rather than inside the restaurant. The second deliberately
starts at `shop_359_outside`, matching "L3 McDonald's outside". The third starts
inside the mapped Starbucks POI and may include the shop-door action inside its
single grouped walking step.

If an acceptance route selects a different transport, first inspect map
connectivity and costs. Do not broaden the escalator implementation or add
route-specific planner conditions merely to force the expected result.

### 4. Module regression tests

Run the existing local suites after the focused tests:

```bash
./gradlew :toponavi-core:test
./gradlew :toponavi-dsl:test
./gradlew :toponavi-web:test
```

No existing elevator, staircase, low-rise, high-rise, traversal-preference, or
web response-contract test may regress.

### 5. Local REST smoke test

Start `toponavi-web` locally with `EXAMPLES_PATH` pointing at the repository's
`examples` directory. Use `POST /api/v1/quick-demo-navigation` and set
`forceRecompile=true` for the first request so a pre-change cache cannot hide a
compiler failure.

Example primary request:

```bash
curl --fail-with-body --request POST \
  'http://127.0.0.1:8080/api/v1/quick-demo-navigation?buildingName=indigoBJ&startNode=Level1%3A%3Amid_gate_inside&endNode=Level3%3A%3Ashop_341_outside&routePlanningPreference=MinimizeTime&forceRecompile=true' \
  --header 'Content-Type: application/json' \
  --data '{"userParams":{},"traversalPreference":{"routePlanningPreference":"MinimizeTime","banTags":[]}}'
```

Repeat for the other two exact-node pairs. Apply the same assertions as the
route acceptance matrix, including transport order, transport count, fixed
costs, and the single walking step.

### 6. Local MCP smoke test, without Agent

Run `toponavi-web` locally, then run `toponavi-mcp-server` locally with:

```bash
TOPONAVI_API_BASE_URL=http://127.0.0.1:8080 npm run dev
```

Invoke the MCP tool `indoor-navigation-path-query` directly through an MCP
client or Inspector. Do not send the request through Agent Backend or Agent
Runtime.

Primary tool input:

```json
{
  "buildingName": "indigoBJ",
  "startNode": "Level1::mid_gate_inside",
  "endNode": "Level3::shop_341_outside",
  "userParams": {},
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "banTags": []
  }
}
```

Repeat with:

```text
Level3::shop_359_outside -> Penthouse::check_in_gate_outside
LowerGround::shop_036 -> LowerGround::subway_station
```

For every MCP call:

- `isError` is absent or false;
- `structuredContent.status` is `success`;
- the exact-node pair reaches the unchanged REST endpoint;
- `routeOverview.transportCount` is respectively `2`, `1`, and `0`;
- transport descriptions contain the required escalator identifiers in order;
- the first route has two transport steps, each with `costSeconds = 30`; the
  second has one transport step with `costSeconds = 35`; the third has none;
- the Starbucks route has exactly one structured walking step.

This is a compatibility smoke test. No MCP production change is expected unless
the existing unchanged route schema unexpectedly rejects a valid response.

## Delivery Constraints

- Implementation must occur on a non-`main` branch.
- Do not mix unrelated map cleanup, naming cleanup, parser redesign, transport
  discovery expansion, or planner refactoring into this change.
- Preserve existing public REST and MCP contracts.
- Prefer a small, explicit implementation over a premature shared abstraction.
- Do not add route-specific hard-coded behavior for the three acceptance pairs.
- Do not weaken or delete existing tests to obtain a pass.
- Local automated and smoke tests must complete before handoff for NAS sync.

## Definition of Done

The work is complete when:

1. `Escalator` declarations no longer throw the unimplemented-logic exception.
2. Invalid or non-positive `params.time` values fail compilation clearly.
3. Existing `indigoBJ` escalators compile without map schema changes.
4. The three exact-node route cases satisfy the acceptance matrix.
5. Core, DSL, and web regression tests pass locally.
6. Direct local MCP calls satisfy the same route assertions without involving
   the Agent.
7. Production changes remain limited to the smallest practical core/compiler
   surface.
8. The implementation is ready for the maintainer to sync and verify on the
   NAS.
