# Uncertain Access Spec

Status: Draft

## Context

Some indoor routes are marked as restricted but may be usable in practice:

- A door says "Card Access", but is sometimes open.
- An elevator floor button may work without a card at certain unknown times.
- Many visitors, couriers, or residents have successfully used the route before.

This is different from both hard access control and route preference:

- Hard access control means the route is unavailable unless the condition is satisfied.
- Traversal preference means the user would rather avoid or prefer a route quality.
- Uncertain access means the route may be physically usable, but availability is not guaranteed.

The navigation system should model this uncertainty explicitly instead of silently treating the route as either fully blocked or fully available.

## Goals

- Add a DSL-level way to annotate uncertain access without removing the path from compiled output.
- Let requests express a user's risk attitude toward uncertain access.
- Prefer the safest route by default.
- Allow opportunistic routes when they are meaningfully better or when no deterministic route exists.
- Always disclose uncertain access in route responses so an AI agent can explain it responsibly.

## Non-Goals

- Do not encourage bypassing access control.
- Do not describe exploits, forced entry, tailgating, or security circumvention.
- Do not replace `requires` hard constraints.
- Do not treat uncertain routes as normal routes without disclosure.

## DSL Syntax

Introduce `subject-to` with the same condition syntax as `requires`.

`subject-to` may optionally be followed by `because "..."`. The `because` text is explanatory metadata provided by the map author. It is not a guarantee and is not an instruction to bypass access control.

Examples:

```toposcript
atomic-path [lobby -> card_access_door] { cost = 4 } subject-to CardAccess
```

```toposcript
atomic-path [A -> B] { cost = 8 } subject-to <CardAccess && InBusinessHours>
```

```toposcript
atomic-path [lobby -> card_access_door] { cost = 4 }
  subject-to CardAccess because "High visitor throughput; this door is often unattended."
```

```toposcript
station F5 at Floor5::main_elevator_hall {
  location = 20.0,
  departureRate = 0.05
} subject-to WithinCapabilityLimit because "Venue capacity rules are not always strictly enforced."
```

```toposcript
station F5 at Floor5::main_elevator_hall {
  location = 20.0,
  departureRate = 0.05
} subject-to CardAccess
```

## Semantics

`requires` remains a hard deterministic constraint.

If a `requires` condition fails, the path or station is unavailable to the route planner.

`subject-to` is an uncertain access annotation.

If a `subject-to` condition is not known to be satisfied, the path or station remains in the compiled graph, but it is marked as uncertain. The route planner may include or avoid it depending on request-level `riskPreference`.

Recommended mental model:

```text
requires CardAccess
  means: unavailable without card access.

subject-to CardAccess
  means: may require card access; route is not necessarily available.
```

```text
subject-to CardAccess because "..."
  means: may require card access, with a map-author-provided explanation
  for why this segment may still be usable in practice.
```

In the initial design, an element should not use both `requires` and `subject-to`. If both appear on the same element, the compiler should reject it with a clear diagnostic.

If `subject-to` has multiple conditions, a single `because` clause explains the whole uncertain condition group:

```toposcript
subject-to <CardAccess && InBusinessHours> because "Often open during delivery traffic."
```

## Request Shape

`riskPreference` should be part of the navigation-time preference request. It may live inside `traversalPreference` together with the primary route preference and tag preferences:

```json
{
  "userParams": {
    "haveStaffCard": false
  },
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "banTags": [],
    "minimizeTag": null,
    "maximizeTag": null,
    "riskPreference": "conservative"
  }
}
```

Allowed values:

- `conservative`
- `permissive`
- `aggressive`

Default:

```text
conservative
```

## Risk Preference Modes

### `conservative`

Treat unresolved or unsatisfied `subject-to` access as unavailable.

If no deterministic route exists, automatically retry using `permissive`.

When this downgrade happens, the response must disclose that the applied risk preference changed:

```json
{
  "riskSummary": {
    "routeNotNecessarilyAvailable": true,
    "appliedRiskPreference": "permissive",
    "uncertaintyReasons": [
      {
        "type": "CardAccess",
        "reason": null
      }
    ],
    "disclaimer": "These uncertainty notes are provided by the map creator. They may be inaccurate or outdated; follow on-site rules and ask local staff if access does not work."
  }
}
```

Product meaning:

> Prefer the safest available route. If no safe route exists, provide an uncertain route with explicit warning.

### `permissive`

Compare deterministic routes against uncertain routes.

Choose an uncertain route only if one of the following is true:

- No deterministic route exists.
- The uncertain route meaningfully improves the primary route objective.

When multiple uncertain candidates qualify, prefer candidates that use fewer uncertain-access segments before applying final deterministic tie-breakers.

Any returned route that uses `subject-to` must include `riskSummary`.

### `aggressive`

Ignore `subject-to` annotations for ranking.

The route planner ranks routes according to the other traversal preferences. If the selected route uses uncertain access, the response still must disclose it with `riskSummary`.

Product meaning:

> Treat uncertain access as routable for planning, but do not hide the uncertainty from the user.

## Meaningful Improvement Thresholds

`permissive` mode should only choose an uncertain route over a deterministic route when the improvement is meaningful for the selected primary objective.

Initial hard-coded thresholds:

```text
MinimizeTime:
  uncertain route must save at least 15 seconds.

MinimizeTransfers:
  uncertain route must reduce transfers by at least 1.

MinimizePhysicalDemands:
  uncertain route must improve physical-demand score by at least 10%.
```

These thresholds intentionally avoid arbitrary user-configurable numeric penalties. They make behavior more stable, testable, and explainable.

## Response Shape

If a route uses any `subject-to` path or station, the top-level response should include:

```json
{
  "riskSummary": {
    "routeNotNecessarilyAvailable": true,
    "appliedRiskPreference": "permissive",
    "uncertaintyReasons": [
      {
        "type": "CardAccess",
        "reason": "High visitor throughput; this door is often unattended."
      }
    ],
    "disclaimer": "These uncertainty notes are provided by the map creator. They may be inaccurate or outdated; follow on-site rules and ask local staff if access does not work."
  }
}
```

Fields:

### `routeNotNecessarilyAvailable`

Type: boolean

True when the selected route depends on at least one uncertain-access segment or station.

### `appliedRiskPreference`

Type: string

The actual risk mode used to produce the returned route.

This may differ from the requested value when `conservative` automatically downgrades to `permissive` because no deterministic route exists.

### `uncertaintyReasons`

Type: list of objects

The distinct `subject-to` condition names used by the selected route, optionally paired with map-author-provided reasons.

Example:

```json
[
  {
    "type": "CardAccess",
    "reason": "High visitor throughput; this door is often unattended."
  },
  {
    "type": "UnknownOpeningHours",
    "reason": null
  }
]
```

Fields:

- `type`: the `subject-to` condition name.
- `reason`: optional explanatory text from `because`; null when no reason was provided.

This replaces the earlier `uncertaintyTypes` field. The type names are already present as keys inside `uncertaintyReasons`, so a separate list would duplicate semantics.

### `disclaimer`

Type: string

Required when `riskSummary.routeNotNecessarilyAvailable` is true.

Recommended value:

```text
These uncertainty notes are provided by the map creator. They may be inaccurate or outdated; follow on-site rules and ask local staff if access does not work.
```

MCP tools and AI agents should surface this disclaimer, or an equivalent localized version, whenever they expose uncertain-access reasons.

## Step-Level Disclosure

In addition to the top-level `riskSummary`, route steps should eventually identify where uncertain access occurs:

```json
{
  "step": 2,
  "type": "Walk",
  "graph": "Lobby",
  "from": "lobby",
  "to": "card_access_door",
  "costSeconds": 4.0,
  "uncertainAccess": {
    "uncertaintyReasons": [
      {
        "type": "CardAccess",
        "reason": "High visitor throughput; this door is often unattended."
      }
    ],
    "message": "This segment may require card access and may not always be available."
  }
}
```

Top-level disclosure is required for the first implementation. Step-level disclosure may be added later but is strongly recommended for agentic navigation.

The top-level `disclaimer` should be emitted once per route response. Step-level objects should focus on location-specific uncertainty and should not repeat the disclaimer on every step.

## Agent Behavior

For delivery-style use cases, the agent should frame uncertain routes carefully.

Example user request:

> I need to deliver something to room 5/C in this building, but I do not have a card. How do I get there?

Agent behavior:

1. Try deterministic routing first.
2. If none exists, use the route returned after conservative-to-permissive downgrade.
3. Clearly state that the route may not be available.
4. Describe the normal physical path without suggesting bypass tactics.
5. Provide a fallback such as contacting the resident, security desk, property management, or visitor registration.
6. Clarify that uncertainty reasons are map-author annotations and may be inaccurate or outdated.

Example response style:

> I checked for a route that works without relying on uncertain access. There does not appear to be a fully guaranteed one. There is a possible route: enter through the main door if it is currently open, turn left, take the main elevator, and try selecting floor 5. This may not work if the door or elevator requires card access. If it does not work, tell me and I will guide you to the visitor or building-management option.

The agent must not recommend forced entry, tailgating, exploiting a door, or bypassing access control.

MCP-layer disclosure:

> These uncertainty notes are provided by the map creator. I cannot verify that they are true or currently applicable. Please follow on-site rules and ask local staff if access does not work.

## Planner Algorithm Sketch

Given a request:

1. Apply normal hard `requires` filtering.
2. Keep `subject-to` annotations in graph metadata.
3. Apply `banTags` and other traversal-preference filters.
4. Plan according to `riskPreference`.

For `conservative`:

1. Exclude unresolved or unsatisfied `subject-to` edges and stations.
2. If a route exists, return it without risk summary.
3. If no route exists, retry as `permissive`.
4. If the permissive route uses uncertain access, return `riskSummary.appliedRiskPreference = "permissive"`.

For `permissive`:

1. Compute the best deterministic route if one exists.
2. Compute uncertain candidate routes.
3. Select an uncertain route only if no deterministic route exists or it passes the meaningful-improvement threshold.
4. Among qualifying uncertain candidates, prefer fewer uncertain-access segments.
5. Return `riskSummary` if the selected route uses uncertain access.

For `aggressive`:

1. Include uncertain edges and stations without ranking penalty.
2. Rank using the primary route objective and tag preferences.
3. Return `riskSummary` if the selected route uses uncertain access.

## Compatibility Plan

Short-term:

- Add `subject-to` parsing.
- Add optional `because "..."` parsing.
- Preserve `subject-to` annotations in compiled graph metadata.
- Add `riskPreference` to `traversalPreference`.
- Implement `conservative` and `aggressive` first if full candidate comparison is expensive.
- Return top-level `riskSummary`.
- Return `uncertaintyReasons` and a disclaimer when uncertain access is used.

Medium-term:

- Implement full `permissive` comparison against deterministic routes.
- Add step-level uncertain-access disclosure.
- Return deterministic fallback or opportunistic alternative routes where feasible.

Long-term:

- Track evidence or freshness for uncertain access, such as "observed often", "reported recently", or "unknown".
- Support richer uncertainty categories without turning them into hard permissions.
