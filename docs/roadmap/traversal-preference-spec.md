# Traversal Preference Spec

Status: Draft

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
- Support hard bans in the short term without requiring major route-planner scoring changes.
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

Node tags may also be supported for description, search, POI classification, or area semantics:

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

The quick-demo navigation POST request should accept `traversalPreference` in the body:

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

Default: `MinimizeTime`

This replaces or subsumes the existing query-string `routePlanningPreference` for POST requests. The existing query parameter may remain temporarily for backward compatibility.

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

`minDwellSeconds` represents actual mandatory dwell rather than an arbitrary preference weight. When implemented, an intermediate node's dwell must also contribute to the route's total time under `MinimizeTime`, whether or not a soft tag preference is present.

## Near-Tie Rule

Soft tag preferences are only applied when candidate routes are close enough under the primary objective.

Initial hard-coded thresholds:

```text
MinimizeTime:            +15 seconds
MinimizeTransfers:       +1 transfer
MinimizePhysicalDemands: +10 percent
```

For `MinimizeTime`, a route is a near-tie if:

```text
candidate.totalCost <= bestPrimary.totalCost + 15 seconds
```

For `MinimizeTransfers`, a route is a near-tie if:

```text
candidate.transferCount <= bestPrimary.transferCount + 1
```

For `MinimizePhysicalDemands`, a route is a near-tie if:

```text
candidate.physicalDemand <= bestPrimary.physicalDemand * 1.10
```

The exact implementation can be refined later, but the product semantics are:

> Soft preferences may choose among essentially comparable routes; they must not cause large detours.

## Sorting Semantics

Recommended ordering:

1. Exclude any route containing a banned tag.
2. Find the best value under the primary `routePlanningPreference`.
3. Keep loop-free candidates within that objective's near-tie threshold.
4. Apply the one requested `minimizeTag` or `maximizeTag`, if present.
5. Use the primary value, then total time, as deterministic tie-breakers.

The planner must not turn tag scores into hidden time penalties or rewards.

## Soft-Preference Search Requirements

The existing shortest-path algorithms retain only the best-known way to reach each node. That is sufficient for hard bans but not for near-tie soft preferences: a slightly slower partial route may have a substantially better tag exposure score and still finish within the allowed threshold.

The eventual implementation should therefore:

1. Run the primary planner to obtain the best primary value.
2. Derive the objective-specific near-tie limit.
3. Explore loop-free alternative routes within that limit.
4. Retain multiple non-dominated partial ways to reach a node when they trade primary cost against tag exposure.
5. Select the complete candidate with the best requested tag score and apply deterministic tie-breakers.

Candidate generation must have deterministic resource limits so that dense maps cannot cause unbounded search. This is a larger planner change than `banTags` filtering and is intentionally deferred. The quick-demo request schema may parse `minimizeTag` and `maximizeTag`, but the endpoint should continue returning `TRAVERSAL_PREFERENCE_NOT_IMPLEMENTED` until this search behavior is implemented end to end.

## Why Not Numeric Penalties Initially

The initial API intentionally avoids a user-facing `tagPenalty` map.

Numeric penalties can make route selection harder to reason about, test, and explain. A route could become slower by an arbitrary amount because of hidden scoring weights rather than a clear rule. This is especially unfriendly for an AI navigation agent that needs to explain choices in natural language.

The proposed design keeps behavior stable:

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

Request:

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

Short-term:

- Keep existing quick-demo query parameters working.
- Add body-level `traversalPreference`.
- Treat query-string `routePlanningPreference` as a fallback when body `traversalPreference.routePlanningPreference` is absent.
- Apply `banTags` at runtime to both paths and entered nodes without mutating the cached compilation result.

Medium-term:

- Add candidate route enumeration for stable near-tie handling.
- Implement `minimizeTag` and `maximizeTag` as near-tie-breakers.
- Add `minDwellSeconds` to node-time accounting and soft tag scoring.

Long-term:

- Consider richer internal scoring, but avoid exposing arbitrary numeric penalties until the behavior is explainable and testable.
