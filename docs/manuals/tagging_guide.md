# TopoScript Tagging Guide

Status: Living Manual

## Purpose

This guide defines the canonical tag vocabulary for TopoScript maps. Map authors should use these definitions consistently across all submaps so that navigation requests, route explanations, and AI-agent interpretation have stable semantics.

Do not introduce a synonym or a new tag in a map file without first adding its definition to this guide.

## General Rules

- Write tags in lowercase `snake_case`.
- Store tags in the plural `tags` attribute as a list of strings.
- Tags describe observable, reasonably stable properties. Do not use them for real-time conditions such as whether a place is crowded at this moment.
- Combine independent facts instead of inventing compound names. For example, use `service_area`, `vehicle_prone`, and `outdoor` together when all three apply.
- Do not infer physical semantics from a node identifier. A name ending in `_out` or `_outside` may mean "outside an elevator hall" while still being inside the building.
- Access tags do not replace `requires` or `subject-to`. Tags describe the route or facility; constraints determine whether traversal is allowed or uncertain.

Example:

```toposcript
topo-node loading_bay {
  tags = ["service_area", "vehicle_prone", "outdoor"]
}
```

## Node and Path Placement

Use node tags to describe a place, point of interest, or side of a boundary. Use path tags to describe what a user experiences while traversing a segment.

```toposcript
topo-node gate_out {
  tags = ["outdoor", "door", "card_locked_door"]
}

topo-node gate_in {
  tags = ["indoor", "door", "card_locked_door"]
}

atomic-path [gate_out <-> gate_in] {
  cost = 3,
  tags = ["door", "card_locked_door"]
}
```

For a door, turnstile, or airlock boundary:

- Tag both boundary-side nodes so either side can be discovered and explained correctly.
- Tag the crossing path because it is authoritative for traversal filtering and preference scoring.
- Do not interpret the duplicated node tags as multiple facilities. A route crosses one facility once when it traverses the tagged path.

## Node Narration

The optional `narration` attribute controls whether an intermediate node should be presented as a named place in user-facing directions. It is a string-based pseudo-enum with three canonical values:

```text
explicit | auto | implicit
```

### `explicit`

The route narration must explicitly mention that the user reaches or passes this place. Use it for locations that are operationally important or consistently useful as landmarks.

An `explicit` node must have a human-readable `description` or future display-label attribute. A compiler should warn when an `explicit` node has no human-readable representation.

### `auto`

The default behavior when `narration` is omitted. The response layer exposes the node's human-readable metadata and allows the navigation agent to decide whether mentioning the place improves the instruction.

### `implicit`

The node remains part of routing and geometry but should not be introduced as a named place when it is an intermediate waypoint. Use it for topology-only connectors and other locations whose names would add noise.

`implicit` suppresses mention of the place, not actions. A turn or required traversal action associated with the route must still be narrated without exposing the node identifier.

### Narration Overrides

- Route start and destination locations must still be expressed in human-readable form even when their nodes are `implicit`.
- Runtime maneuvers, such as turns derived from route geometry, are narrated independently of the node's `narration` value.
- Edge `requiredActions` must always be narrated and override node-level suppression.
- A node identifier is internal routing data and must never be used as a user-facing fallback when a label or description is unavailable. Omit the place name or produce an unnamed maneuver instead.

Example:

```toposcript
topo-node reception {
  narration = "explicit",
  description = "Office reception desk"
}

topo-node corridor_geometry_joint {
  narration = "implicit"
}
```

## Edge Required Actions

The optional `requiredActions` attribute is an ordered list of traversal instructions that must be communicated when a route uses an `atomic-path`.

When the attribute is omitted, the edge represents ordinary walking and no `simply_walk` value should be stored. Because the current surface language cannot infer the element type of an empty list, do not write `requiredActions = []`; omit the field instead.

Actions are composable and must be executed and narrated in list order:

```toposcript
atomic-path [lobby -> secure_hall] {
  cost = 5,
  tags = ["door", "card_locked_door"],
  requiredActions = ["tap_access_card", "cross_door"]
}
```

### Canonical Actions

Boundary traversal:

```text
cross_door
cross_airlock
cross_turnstile
```

Credential and checkpoint actions:

```text
unlock_with_key
tap_access_card
scan_qr_code
enter_access_code
show_id
pass_security_check
use_intercom
request_staff_access
```

Movement actions:

```text
ride_travelator
up_slope
down_slope
```

Use composition instead of inventing compound action names:

```text
["unlock_with_key", "cross_door"]
["tap_access_card", "cross_door"]
["tap_access_card", "cross_turnstile"]
["show_id", "pass_security_check", "cross_door"]
```

Do not use `cross_keyed_door` to cover both physical keys and access cards. The action list must tell the agent which credential interaction to describe.

### Directionality

A bidirectional `<->` path may only carry actions that apply in the same way in both directions. When the actions differ by direction, define two directed paths:

```toposcript
atomic-path [outside -> inside] {
  cost = 5,
  requiredActions = ["tap_access_card", "cross_door"]
}

atomic-path [inside -> outside] {
  cost = 3,
  requiredActions = ["cross_door"]
}
```

Direction-sensitive actions such as `up_slope` and `down_slope` should use directed paths for now. Automatic action inversion for `<->` paths is deferred.

### Relationship to Tags and Constraints

- Tags describe stable route or facility properties; `requiredActions` describe what the route instruction must tell the user to do.
- `requiredActions` do not grant access and do not replace `requires` or `subject-to`.
- Unknown action strings should produce a compiler warning. Add new canonical actions to this guide before using them across maps.

## Environment Tags

### `indoor`

A location or path inside a substantially enclosed building envelope.

Do not infer `indoor` merely from the absence of `outdoor`. If the physical classification is uncertain, omit both tags until the site is verified.

### `outdoor`

A fully external space with no more than one surrounding wall. Street-side eaves, covered sidewalks, arcades, and colonnades still count as `outdoor` under this rule.

A roof or canopy does not make a place indoor. Consequently, `outdoor` does not imply `rain_exposed`, `daylight`, or `direct_daylight`; add those tags independently when their own definitions apply.

### `rain_exposed`

A location or path that can be directly reached by rainfall under ordinary bad-weather conditions. Include areas that are routinely reached by wind-driven rain. Do not apply it merely because the place is outdoor.

### `daylight`

A location or path exposed to unobstructed natural daylight. There must be no permanent architectural obstruction, such as a roof, canopy, ceiling, or glazing, between the location and the relevant open sky.

This definition is intentionally strict for users who need to minimize ultraviolet exposure. A bright interior, a glazed atrium, or a space with large windows is not tagged `daylight` merely because it receives natural-looking illumination.

### `direct_daylight`

A location or path where direct sunlight reaches the user at any time during a normal day or season. There is no minimum duration: even a short daily period of direct solar exposure qualifies.

`direct_daylight` implies `daylight`; apply both tags. Reflected light and diffuse sky light do not qualify as `direct_daylight`.

## Service and Traffic Tags

### `service_area`

A passage or area used for freight movement, deliveries, loading operations, waste movement, or related back-of-house logistics. It applies whether the route is indoor or outdoor.

Use `service_area` as the only canonical tag. Do not use `service_corridor`, `logistics_area`, or `logistics_corridor` as separate tags; they are synonyms of `service_area` and should be migrated to it.

Do not apply `service_area` merely because staff use a place. Staff offices, break rooms, and private administrative areas are not freight routes unless they also serve a logistics function.

### `vehicle_prone`

A pedestrian location or route where moving vehicles routinely, or reasonably predictably, enter, cross, or share the same movement space. Typical examples include loading bays, driveways, and mixed pedestrian-vehicle service lanes.

Do not apply it to a sidewalk or indoor path merely because vehicles are visible nearby. A physically separated roadway does not make the pedestrian route `vehicle_prone`.

### `odor_prone`

A location or route with a recurring, operationally predictable risk of strong or unpleasant odors, such as a waste-handling passage or kitchen service exit.

The tag expresses a stable tendency, not a claim about the odor at the exact time of a request. Do not apply it because of a one-off incident or an unverified assumption based only on a room name.

## Boundary and Access-Facility Tags

### `door`

A physical door that must be crossed. Tag both side nodes and the crossing path.

### `airlock`

A two-door passage in which the doors form a controlled sequential enclosure, comparable to an airlock. A normal vestibule with two unrelated doors is not necessarily an `airlock`.

Tag the outer and inner boundary nodes and the path representing traversal through the complete airlock. Also apply `door`.

### `turnstile`

A turnstile or similar body-width mechanical gate that a user must pass through. Tag both side nodes and the crossing path.

Do not also apply `door` unless a separate physical door is part of the same represented crossing.

### `card_locked_door`

A physical door normally controlled by a card or equivalent electronic credential in at least one direction. Tag both side nodes and the crossing path. Also apply `door`.

This tag describes the facility and remains present even when a particular user has access. Add the appropriate `requires` or `subject-to` condition separately to represent actual access behavior.

The former generic tag `security_gate` is not canonical. Use `turnstile` or `card_locked_door` according to the physical facility.

## Transportation Facility Tags

### `elevator_hall`

A node located within the waiting, boarding, or alighting area directly serving one or more elevators.

Apply it to nodes representing the elevator hall itself. Do not apply it to a nearby corridor, an entrance leading toward the hall, or a node merely described as being outside the hall. The tag identifies the physical function of the location and does not imply that a particular elevator service is available to the user.

## Point-of-Interest Tags

POI tags identify the usable inside or service point of a facility. Do not put them on an outside waiting node, nearby corridor, or entrance-only node unless that node itself represents the usable facility.

### `shop`

The customer-accessible inside or service point of a retail shop. A retail corridor or connector is not itself a `shop`.

### `cafe`

The customer-accessible inside or service point of a cafe. Do not automatically add `restaurant`; apply both only when the venue genuinely serves both roles.

### `restaurant`

The customer-accessible inside or service point of a restaurant.

### `toilet`

The usable inside node of a toilet facility. In particular, do not tag the outside or approach node as `toilet`.

### `staffed`

A facility whose normal service model involves on-site personnel rather than being fully self-service. Apply it to staffed cafes, restaurants, shops, reception desks, concierge desks, and similar service points.

`staffed` describes normal operation, not whether an employee is present at the exact time of a request. Do not apply it to a fully self-service facility. This tag may be minimized by users who prefer to avoid staffed interactions.

## Composition Examples

### Covered street arcade

```toposcript
tags = ["outdoor"]
```

Add `rain_exposed` only if rain ordinarily reaches the represented location. Do not add `daylight` when a permanent roof or canopy blocks the open sky.

### Open plaza with direct sun

```toposcript
tags = ["outdoor", "rain_exposed", "daylight", "direct_daylight"]
```

### Staffed cafe

```toposcript
tags = ["indoor", "cafe", "staffed"]
```

### Outdoor loading lane

```toposcript
tags = ["outdoor", "service_area", "vehicle_prone"]
```

### Card-controlled door

```toposcript
tags = ["door", "card_locked_door"]
```

## Review Checklist

Before accepting tags in a submap, verify that:

1. Every tag is defined in this guide and uses its canonical spelling.
2. Physical facts were verified rather than inferred from node names.
3. POI tags are applied only to usable inside or service-point nodes.
4. Boundary tags are applied to both side nodes and the crossing path.
5. Access restrictions still use `requires` or `subject-to` where appropriate.
6. Dynamic or subjective observations have not been represented as permanent facts.
7. Every `explicit` node has a human-readable description or display label.
8. `implicit` nodes are not exposed through identifier fallbacks.
9. `requiredActions` are canonical, ordered, and valid for the path direction.
