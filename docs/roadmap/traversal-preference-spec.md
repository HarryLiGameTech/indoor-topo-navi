# Traversal Preference Spec

Status: Finalized

Implementation: native primary objectives (including `MinimizeCustomData`), hard bans and `minimizeTag` are supported in the engine and building-scoped REST API. MCP soft-preference forwarding is separate adapter work. `maximizeTag` remains deferred.

Roadmap direction (2026-10-08): move tolerance-based secondary preferences and route-alternative orchestration to the agent layer. Keep engine searches focused on one primary objective with deterministic tie-breakers. The deferred section below describes the target architecture; existing endpoint behavior remains in place until an explicit migration.

## Context

Users may ask route-quality questions such as:

- "Will this route expose me to rain?"
- "Do I need to walk outside?"
- "I am moving goods; please avoid the smelly kitchen back door if possible."
- "I like routes with more shops."

These requirements are not access-control constraints. They describe route environment, route character, and user preference. Encoding them as compile-time DSL constraints such as `requires acceptOutdoorEnvironment` is semantically awkward because it treats preference as permission and forces root parameters such as `acceptOutdoor: Bool` into the map model.

The preferred design is to annotate navigable segments with tags and pass user traversal preferences at navigation time.

Canonical tag names and map-authoring rules are defined in the [TopoScript Tagging Guide](../manuals/tagging_guide.md). New map files and examples in this specification should use that vocabulary.

## Goals

- Represent route environment semantics directly with tags on paths and, where useful, nodes.
- Keep access-control constraints separate from traversal preferences.
- Apply hard bans independently of soft route ranking.
- Support soft preferences through stable, deterministic near-tie-breaking.
- Keep the request shape friendly for an AI navigation agent.

## Non-Goals

- No user-configurable numeric penalties in the initial design.
- No multi-tag weighted scoring model in the initial design.
- No global optimization over arbitrary soft-preference utility functions.
- No replacement of existing `requires` access-control semantics.

## DSL Tagging Model

Tags are ordinary attributes on both `atomic-path` and `topo-node` records. Path tags describe the experience of traversing a segment; node tags describe entering or occupying a place, facility, boundary side, or area.

Example:

```toposcript
atomic-path [gate_elevator_T <-> outdoor_LT] {
  cost = 9,
  tags = ["outdoor", "rain_exposed"]
}
```

Node tags also support description, search, POI classification, or area semantics:

```toposcript
topo-node outdoor_LT {
  description = "Door near the riverside outdoor path",
  tags = ["outdoor"]
}
```

For hard bans, the planner evaluates node and path tags independently. A path is not usable when either the path itself has a banned tag or its target node has a banned tag. Node tags are not implicitly projected onto incident paths.

## Access Constraints vs Traversal Tags

Use `requires` for objective access control:

```toposcript
atomic-path [service_corr_A -> office_hall] { cost = 5 } requires TenantsOnly
station F51 at Floor51::L4_hall { location = 220.0, departureRate = 0.01 } requires TenantsOnly
```

Use `tags` for route qualities:

```toposcript
atomic-path [corridor_A <-> kitchen_back_door] {
  cost = 6,
  tags = ["service_area", "odor_prone"]
}
```

This keeps "can the user pass?" separate from "does the user prefer this route?"

## Request Shape

The `/api/v1/buildings/{buildingId}/find-route` POST request accepts `traversalPreference` in the body:

```json
{
  "userParams": {
    "haveStaffCard": true,
    "haveManagementCard": false
  },
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "banTags": ["outdoor", "rain_exposed"],
    "minimizeTag": "odor_prone"
  }
}
```

`userParams` remain compile-time parameters for DSL constraints.

`traversalPreference` describes navigation-time route preferences.

## Traversal Preference Fields

### `routePlanningPreference`

Type: string

Allowed values:

- `MinimizeTime`
- `MinimizeTransfers`
- `MinimizePhysicalDemands`
- `MinimizeCustomData`

Default: `MinimizeTime`

This field belongs to the JSON body of building-scoped route requests. Legacy query-string preferences are not supported.

### `customObjective`

Required only with `MinimizeCustomData`. A native custom objective uses lexicographic ordering: custom cost, followed by the declared tie-breakers. This is primary optimization, not tolerance-based soft avoidance. Supported definitions and validation rules are specified in the [HTTP API guide](../http-api.md#route-calculation).

`kind: "tagExposure"` uses the tag exposure score below. `kind: "edgeAttribute"` sums a finite, non-negative numeric atomic-path attribute, with an explicit missing-value policy defaulting to zero. Rides contribute zero to the atomic-path attribute sum. The default tie-breakers are `MinimizeTransfers`, then `MinimizeTime`; reversing those two is supported. Native custom objectives cannot be combined with `minimizeTag`.

### `banTags`

Type: list of strings

Default: empty list

Semantics: request-scoped hard ban. A traversal is excluded when either its path or the node being entered has one of these tags.

The compiled graph remains unchanged and reusable across requests. The route planner applies `banTags` while expanding neighbors; `banTags` must not enter the compilation cache key.

Endpoint rules:

- A tagged source node may be departed because the user is already there. Its outgoing paths are still checked normally.
- A tagged destination is a request conflict and returns `DESTINATION_HAS_BANNED_TAG`.
- Tagged intermediate nodes are not entered.
- A valid request for which no route remains returns `NO_ROUTE_WITH_BAN_TAGS`.

### `minimizeTag`

Type: nullable string

Default: null

Semantics: soft avoidance. Among routes that are near-ties under the primary route objective, prefer the route with the lower exposure score for this tag.

Only one `minimizeTag` is supported in the initial design.

Example: a goods mover says "avoid the kitchen back door if possible"; the agent maps this to:

```json
{
  "minimizeTag": "odor_prone"
}
```

### `maximizeTag`

Deferred: this section describes future behavior and is excluded from the current implementation scope.

Type: nullable string

Default: null

Semantics: soft attraction. Among routes that are near-ties under the primary route objective, prefer the route with the higher exposure score for this tag.

Only one `maximizeTag` is supported in the initial design.

Example: a user says "I like routes with many shops"; the agent maps this to:

```json
{
  "maximizeTag": "shop"
}
```

This preference affects ranking only when the map provides positive `shop` exposure through truthful node dwell or tagged-edge traversal. A `shop` node with no `minDwellSeconds` does not receive an artificial occurrence score.

`minimizeTag` and `maximizeTag` are mutually exclusive in the initial design. A request that provides both is invalid and should return HTTP 400 with `CONFLICTING_TAG_PREFERENCES`.

## Tag Exposure Score

For a route `R` and requested tag `t`, the soft-preference score is:

```text
tagExposureScore(R, t) =
    sum(traversalSeconds(edge) for each tagged edge in R)
  + sum(minDwellSeconds(node) for each tagged intermediate node in R)
```

Rules:

- An edge contributes its traversal time when its `tags` contain `t`.
- Only intermediate nodes contribute. The route source and destination are excluded because they are fixed across candidates.
- A tagged intermediate node contributes its explicit `minDwellSeconds` value.
- Missing `minDwellSeconds` contributes `0`, not an implicit occurrence count.
- `minDwellSeconds` must be finite and non-negative.
- When an edge and an intermediate node both carry `t`, both contributions are added. The edge represents traversal exposure; the node represents a separate mandatory dwell.
- A point-only POI tag does not influence soft ranking merely because its node appears in a route. It needs a truthful positive `minDwellSeconds` or separately modeled tagged-edge exposure.
- Candidate routes must not repeat a `GlobalNode`. This prevents `maximizeTag` from selecting routes that loop through the same tagged place to inflate the score.

`minDwellSeconds` represents actual mandatory dwell rather than an arbitrary preference weight. An intermediate node's dwell also contributes to the route's total time under `MinimizeTime`, whether or not a soft tag preference is present.

## Near-Tie Rule

Soft tag preferences are only applied when candidate routes are close enough under the primary objective.

Initial hard-coded thresholds:

```text
MinimizeTime:            +60 seconds
MinimizeTransfers:       +2 transfers
MinimizePhysicalDemands: +25 percent
```

For `MinimizeTime`, a route is a near-tie if:

```text
candidate.totalCost <= bestPrimary.totalCost + 60 seconds
```

Within this allowance, the requested tag score takes priority; a smaller improvement in exposure does not reduce the allowed extra time. For example, if the fastest permitted route takes 60 seconds, a 120-second route with lower unwanted-tag exposure is eligible, while a 121-second route is not. Equal exposure is resolved in favor of the faster route.

For `MinimizeTransfers`, a route is a near-tie if:

```text
candidate.transferCount <= bestPrimary.transferCount + 2
```

`transferCount` counts actual changes between transport lines. A walking-only route or a route using one transport line has zero transfers. Consecutive rides on the same line do not add transfers; switching to another line adds one, including when walking connects the two lines. For example, `LiftA → walk → LiftB → EscalatorC` has two transfers. High-rise transport-plan ranking and complete-route scoring both use this definition.

For `MinimizePhysicalDemands`, a route is a near-tie if:

```text
candidate.physicalDemand <= bestPrimary.physicalDemand * 1.25
```

The threshold applies only to the selected primary objective. Transfer and physical-demand preferences do not impose an additional 60-second time cap. If the best physical-demand score is zero, only zero-score candidates qualify.

### Physical-Demand Metric

Planning and returned-route scoring use the same per-traversal criteria:

| Traversal | Physical-demand score |
| --- | --- |
| Elevator | `0.1 × vertical metres + 0.5 × seconds` |
| Escalator | `0.1 × distance metres + 0.65 × seconds` |
| Stairs | `10 × vertical metres` |
| Walking and mandatory intermediate-node dwell | `1 × seconds` |

Elevator seconds include the estimated waiting and ride time. Escalator seconds are `params.time`; distance is optional `params.distance`, a finite, non-negative `Int` or `Float` in metres along the escalator. A legacy time-only escalator contributes zero to the distance term; its synthetic station indices are never interpreted as metres. Stair turn costs remain elapsed time and do not alter its distance-based physical score.

The near-tie comparison uses complete routes, including walking to and from transport and mandatory intermediate-node dwell. Both primary-search strategies include all walking, ride, and dwell contributions in selection. This is a relative routing score, not a physiological measurement.

## Sorting Semantics

Native primary search uses one ordered score:

- `MinimizeTime`: complete traversal seconds.
- `MinimizeTransfers`: actual line changes, then complete traversal seconds.
- `MinimizePhysicalDemands`: complete physical-demand score, then traversal seconds.
- `MinimizeCustomData`: custom cost, followed by the two declared tie-breakers.

Hard permissions come from the compiled model. Banned tags and uncertain-access policy filter the model per request. A fixed canonical edge and state order resolves exact score ties deterministically. Routes returned to callers do not repeat a `GlobalNode`.

For the separate, backwards-compatible `minimizeTag` policy:

1. Find the native primary optimum using the selected routing mode.
2. If its tag exposure is zero, return it immediately: zero is the minimum possible exposure, and the primary tie-breaks are already optimal.
3. Otherwise derive the objective-specific near-tie threshold.
4. Search loop-free routes within that threshold, minimizing tag exposure, then primary value, total time, and canonical route order.

The tag policy does not introduce hidden time penalties or rewards. `maximizeTag` remains deferred.

## Search Structure and Resource Limits

`partiallyInformed` uses a transport hierarchy whose vertices are transport landings and the exact origin and destination. Walking connections are searched under the native objective and cached per departure node. Their full costs participate in building-wide selection. `fullyInformed` searches the walking and ride edges directly. Both retain one best native score per location and, when transfers affect ranking, previous transport line. Primary search does not retain competing visited-node histories.

Adding a tag preference or mandatory node dwell does not replace the selected primary-search strategy. Mandatory intermediate-node dwell is included in native scoring; source and destination dwell is excluded. The optional tolerance-based tag phase still retains non-dominated route histories because its constrained objective can require alternatives that primary search safely discards.

Each invocation shares a budget of 50,000 admitted labels and 200,000 expansion attempts across primary search, floor-level connection searches, and any constrained tag phase. Exceeding either limit returns HTTP 503 with `TRAVERSAL_PREFERENCE_SEARCH_LIMIT`; it does not return an unproven partial result as optimal. Counts refer to search states and candidate extensions, not distinct physical nodes. Graph preparation is outside these counters. Positive-exposure constrained tag searches can still exhaust their budget; native custom objectives avoid that second phase. These bounds do not replace deployment-level input, memory, or request-time limits.

Risk preferences search the strict and/or uncertain-access graph independently. The original objectives retain their existing risk-improvement thresholds. For `MinimizeCustomData`, permissive routing requires a strictly lower custom cost before choosing uncertain access; equal-cost improvements in transfers or time alone do not qualify.

`maximizeTag` alone returns HTTP 422 with `UNSUPPORTED_TRAVERSAL_PREFERENCE`; combining both soft fields returns HTTP 400 with `CONFLICTING_TAG_PREFERENCES`.

## Deferred: Reliable Secondary Preferences and Route Alternatives

Architecture direction agreed on 2026-10-08; implementation remains deferred. The agent layer owns secondary-preference trade-offs and the presentation of route alternatives. The engine supplies bounded single-primary-objective searches and authoritative route metrics. This replaces the proposed engine-side expansion of tolerance-constrained alternative search.

### Responsibilities

- **Engine:** optimize one primary objective per search, such as time, line changes, physical demand, or a selected additive custom cost. Retain deterministic tie-breakers for equal primary scores; these do not grant a tolerance for sacrificing the primary objective. Include all walking, transport, and intermediate dwell in scoring. Continue consuming compiled access permissions and enforcing request bans and uncertain-access policy.
- **Route metrics:** provide complete time, line changes, physical demand, and requested tag exposure for each selected route, independently of which metric drove selection. Adding observation metrics must not change route choice or start another optimization. The current API does not yet provide a general requested-tag-exposure summary for every primary mode; specify this contract before implementing the agent workflow.
- **Agent controller:** issue a bounded set of engine requests, retain the primary result, compare candidates against the user's tolerances, deduplicate routes, and rank or present useful alternatives. Numeric comparisons, request limits, and termination belong in deterministic orchestration code. The language model interprets intent and explains verified results.
- **Applications:** may use the same orchestration policy without an LLM. The engine's native routing API remains independently usable.

### Intended Workflow

1. Request the primary optimum, for example `MinimizeTime`, and record its complete time and requested tag exposure.
2. Request a different native optimum, for example `MinimizeCustomData` with `customObjective: {kind: "tagExposure", tag: "rain_exposed"}` and time before transfers in its tie-breakers. Each engine request continues to optimize its own primary objective.
3. Compare returned routes in the agent controller. For the existing time policy, retain a tag-improving alternative only when it is within 60 seconds of the fastest result. Preserve the primary result for presentation. Other supported primary policies retain their documented tolerances unless explicitly revised.
4. Make any additional candidate probes within a defined request and elapsed-time budget. Stop when the budget or candidate target is reached; reuse identical queries instead of repeatedly calling the engine.
5. Present the baseline and useful alternatives with measured time and exposure. Remove duplicates and candidates that are no better on any selected metric and worse on at least one.

Comparisons must use the same building revision or compiled snapshot, endpoints, user parameters, bans, and risk policy. Preserve each candidate's uncertainty disclosure. Define the required revision/snapshot contract, total call budget, bounded concurrency, candidate-count policy, and failure handling before implementation. A failed alternative request must not discard a successful primary route. Candidate-count controls and multi-route output belong to the orchestration contract; the engine's current route endpoint returns one route per request.

### Example

Example for travel time as the primary objective and `rain_exposed` as the secondary preference:

| Result | Route | Travel time | Rain exposure |
|---|---|---|---|
| Found route | A–B–C | 2 min 30 sec | 35 sec |
| Alternative | A–D–E–C | 3 min | 5 sec |

The alternative takes 30 seconds longer and reduces rain exposure by 30 seconds. It fits the existing +60-second time tolerance. Both options remain useful because one is faster and the other has less exposure.

### Guarantees and Limits

Single-objective search avoids the tolerance-constrained route-history explosion, but its work still depends on graph size, transport connectivity, and search state. Engine resource limits remain necessary. Multiple engine calls also incur orchestration and network cost; moving the policy does not make total work trivial.

A few independent optima do not guarantee discovery of the best compromise. If the fastest route is A–B–C at 150 seconds with 35 seconds of exposure, and the least-exposed route takes 600 seconds with zero exposure, those two queries alone can miss A–D–E–C at 180 seconds with 5 seconds of exposure. The controller must describe results as the best among the candidates explored. Finding no suitable candidate does not prove that none exists. A globally optimal secondary result under a time bound would require a separately specified constrained-search guarantee.

Before shipping, test the bounded workflow on the example above, a missed-compromise case, duplicate optima, absent tags, failed or budget-exhausted alternative searches, and mismatched building revisions. Define the candidate-generation policy before promising alternative coverage. Existing restrictions on user-configurable numeric penalties and multi-tag weights remain unchanged.

### Migration

The agent workflow and route-metric contract are deferred. Existing engine `minimizeTag` behavior remains available until its callers have an explicit migration path; later deprecation or removal of its constrained second pass is separate implementation work. This documentation change does not remove it. Native `MinimizeCustomData` keeps strict custom-first ordering. `riskPreference: "permissive"` continues to govern uncertain access independently of objective tolerances, and `maximizeTag` remains separately deferred.

## Why Not Numeric Penalties Initially

The initial API intentionally avoids a user-facing `tagPenalty` map.

Numeric penalties can make route selection harder to reason about, test, and explain. A route could become slower by an arbitrary amount because of hidden scoring weights rather than a clear rule. This is especially unfriendly for an AI navigation agent that needs to explain choices in natural language.

The design keeps behavior stable:

- `banTags` are hard and easy to explain.
- `minimizeTag` and `maximizeTag` are soft and only affect near-ties.
- Objective-specific near-tie thresholds prevent soft preferences from overwhelming the primary objective.

Numeric penalties may be revisited later as an internal configuration mechanism, but they should not be exposed in the first request contract.

## Agent Mapping Examples

User: "Will this route expose me to rain?"

Agent behavior:

- Request a normal route.
- Inspect returned steps or route metadata for `rain_exposed`.
- Answer whether the route contains rain-exposed segments.

User: "Do not make me walk outside."

Request:

```json
{
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "banTags": ["outdoor"]
  }
}
```

User: "Try not to take me through the kitchen back door."

Request:

```json
{
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "minimizeTag": "odor_prone"
  }
}
```

User: "I prefer the route with more shops."

Deferred request shape (currently returns HTTP 501):

```json
{
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "maximizeTag": "shop"
  }
}
```

## Response Considerations

Route responses should eventually include route tags at either the step or edge level so that agents can answer route-quality questions without recomputing:

```json
{
  "step": 2,
  "type": "Walk",
  "graph": "Floor1",
  "from": "gate_elevator_T",
  "to": "outdoor_LT",
  "costSeconds": 9.0,
  "tags": ["outdoor", "rain_exposed"]
}
```

This is not required for the first hard-ban implementation, but it is important for agentic route explanation.

## Compatibility Plan

Supported:

- Requests use the body fields specified in [Endpoint Design](../endpoint-design.md); legacy quick-demo query parameters are removed.
- Traversal preferences are supplied only through the JSON body.
- `banTags` and `minimizeTag` apply per request without mutating cached compilation results or entering the compilation cache key.
- `minDwellSeconds` contributes to mandatory intermediate-node time and soft tag exposure.
- Recompile persisted building caches created before traversal metadata or escalator distance was introduced.

Deferred:

- Implement bounded agent-layer orchestration for secondary preferences and useful alternatives, with engine-provided route metrics and an explicit migration of legacy engine `minimizeTag`, as described above.
- Keep `maximizeTag` deferred until its implementation is explicitly scheduled.
- Consider richer internal scoring, but avoid exposing arbitrary numeric penalties until the behavior is explainable and testable.
