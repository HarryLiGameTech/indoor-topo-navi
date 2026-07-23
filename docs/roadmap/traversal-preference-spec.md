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

Tags should be ordinary attributes on `atomic-path` records. Path tags are the primary source of traversal semantics because exposure usually belongs to a segment, not only to a point.

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

Planner behavior should primarily inspect path tags. Node tags may be projected onto incident edges later, but that should be an explicit implementation choice rather than an implicit default.

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
    "minimizeTag": "odor_prone",
    "maximizeTag": "shop"
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

Semantics: hard ban. Any path tagged with one of these tags is excluded from traversal.

Short-term implementation may compile or project a graph with banned tagged paths removed before invoking the current route algorithm. This is acceptable because the resulting graph is equivalent to a compile-time filtered graph, but the semantics stay at the traversal-preference layer rather than becoming root DSL parameters.

### `minimizeTag`

Type: nullable string

Default: null

Semantics: soft avoidance. Among routes that are near-ties under the primary route objective, prefer the route with fewer occurrences of this tag.

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

Semantics: soft attraction. Among routes that are near-ties under the primary route objective, prefer the route with more occurrences of this tag.

Only one `maximizeTag` is supported in the initial design.

Example: a user says "I like routes with many shops"; the agent maps this to:

```json
{
  "maximizeTag": "shop"
}
```

## Near-Tie Rule

Soft tag preferences are only applied when candidate routes are close enough under the primary objective.

Initial hard-coded threshold:

```text
nearTieThresholdSeconds = 15
```

For `MinimizeTime`, a route is a near-tie if:

```text
candidate.totalCost <= bestPrimary.totalCost + 15 seconds
```

For `MinimizeTransfers` and `MinimizePhysicalDemands`, implementation should preserve the existing primary ordering first. The 15-second threshold may be applied to the time component among candidates with equivalent primary-class scores.

The exact implementation can be refined later, but the product semantics are:

> Soft preferences may choose among essentially comparable routes; they must not cause large detours.

## Sorting Semantics

Recommended ordering:

1. Exclude any route containing a banned tag.
2. Rank by the primary `routePlanningPreference`.
3. Keep candidates within the near-tie threshold.
4. Apply `minimizeTag` as a tie-breaker, if present.
5. Apply `maximizeTag` as a tie-breaker, if present.
6. Use total time as the final deterministic tie-breaker.

If both `minimizeTag` and `maximizeTag` are provided, both may be applied in the order above. If this proves confusing in product use, the API can later reject requests that provide both.

## Why Not Numeric Penalties Initially

The initial API intentionally avoids a user-facing `tagPenalty` map.

Numeric penalties can make route selection harder to reason about, test, and explain. A route could become slower by an arbitrary amount because of hidden scoring weights rather than a clear rule. This is especially unfriendly for an AI navigation agent that needs to explain choices in natural language.

The proposed design keeps behavior stable:

- `banTags` are hard and easy to explain.
- `minimizeTag` and `maximizeTag` are soft and only affect near-ties.
- The 15-second threshold prevents soft preferences from overwhelming the primary objective.

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
- Implement `banTags` by graph filtering before invoking the current route algorithm.

Medium-term:

- Return tags in structured route steps.
- Add candidate route enumeration for stable near-tie handling.
- Implement `minimizeTag` and `maximizeTag` as near-tie-breakers.

Long-term:

- Consider richer internal scoring, but avoid exposing arbitrary numeric penalties until the behavior is explainable and testable.
