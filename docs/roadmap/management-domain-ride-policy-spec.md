# Management Domain Ride Policy Spec

Status: Draft

## Context

Large indoor buildings may have complex elevator dispatch policies that cannot be expressed by per-station permissions alone.

Example:

- From office floors, passengers may go down to the public lobby.
- From office floors, passengers may not go directly to hotel floors or equipment floors.
- From equipment floors, property-management staff may be able to go to any floor.

This is an origin-destination policy for a transport line, not merely an `on-depart` or `on-arrive` station rule.

The system also has a collaborative authoring model:

- The configuration file is usually maintained by landlords or authors focused on building-level/floor-level assembly.
- Individual `topo-map` files are often maintained by floor workers, couriers, or mappers who know the real current usage of a floor.

Therefore, a floor's configured management domain may be an early design intent rather than ground truth. A `topo-map` author must be able to override it when the actual floor usage changes.

## Goals

- Assign each submap to a management domain.
- Use management domains in elevator ride-origin/destination policies.
- Allow concrete `topo-map` files to override configured management domains.
- Avoid forcing authors to write raw floor-to-floor OD matrices.
- Keep domain resolution deterministic and compiler-diagnosable.

## Non-Goals

- Do not replace station-level `on-depart` and `on-arrive` permissions.
- Do not require all domains to be explicitly declared in the initial design.
- Do not implement template-specific override restrictions in the initial design.

## Management Domains in Configuration

The configuration file may assign a management domain to each submap using `managed-by`.

Example:

```toposcript
building-includes {
  submap Floor1 managed-by PublicLobby
  submap Floor32 managed-by OfficeMain
  submap Floor88 managed-by Hotel
  submap FloorM2 managed-by Facilities

  vehicle AllFloorElevator
}
```

When used with template reuse:

```toposcript
building-includes {
  submap Floor32 using StandardOfficeFloor managed-by OfficeMain
  submap Floor33 using StandardOfficeFloor managed-by OfficeMain
  submap Floor99 using StandardUpperFloor managed-by OfficeMain

  vehicle AllFloorElevator
}
```

`managed-by` assigns a submap to a management domain used by transport ride policies and access reasoning.

## Topo-Map Domain Override

A `topo-map` file may override the configured management domain using `override-managed-by` in the map header.

Example:

```toposcript
topo-map Floor99() override-managed-by Hotel {
  topo-node elevator_hall
  atomic-path [elevator_hall <-> corridor] { cost = 5 }
}
```

Semantics:

```text
configured domain = OfficeMain
topo-map override = Hotel
effective domain  = Hotel
```

This models cases where the building-level configuration reflects original planning, but the floor's real current usage has changed.

Example scenario:

```toposcript
building-includes {
  submap Floor98 managed-by OfficeMain
  submap Floor99 managed-by OfficeMain
  submap Floor100 managed-by OfficeMain
}
```

Later, the top floors are converted into hotel space:

```toposcript
topo-map Floor99() override-managed-by Hotel {
  ...
}
```

## Effective Domain Resolution

Each compiled submap has exactly one effective management domain.

Resolution order:

1. `topo-map ... override-managed-by X` wins.
2. Otherwise, `submap ... managed-by X` from configuration is used.
3. Otherwise, the backend assigns the default domain `Misc`.

`Misc` is the fallback domain for miscellaneous floors whose management domain has never been specified.

The compiler should internally maintain:

```text
Map[submapName, effectiveManagementDomain]
```

It may also keep configured and override domains separately for diagnostics.

## Compiler Diagnostics

### Redundant Override

If a topo-map override matches the configured domain:

```toposcript
building-includes {
  submap Floor32 managed-by OfficeMain
}
```

```toposcript
topo-map Floor32() override-managed-by OfficeMain {
  ...
}
```

Emit warning:

```text
Redundant override for submap Floor32
```

### Missing Config Domain

If a topo-map uses `override-managed-by X` but the configuration has no `managed-by` for that submap, emit warning:

```text
Submap domain not specified in config file
```

The effective domain is still `X`.

### No Domain Specified Anywhere

If neither configuration nor topo-map specifies a domain:

```text
effective domain = Misc
```

No warning is required in the initial design, because `Misc` is the intended fallback for unspecified floors.

### Station Label Collision

Station labels used by `ride-from` must uniquely identify a station within that transport.

If a transport with ride policies declares the same station label more than once, emit a compiler error.

A station label may equal a management domain name. In that case, the exact station label takes precedence.

Management domain names may equal submap names because submap names are not ride-policy operands.

## Transport Ride Policy

Transport files may define ride-origin/destination policy using `ride-from`.

Syntax:

```toposcript
ride-from SOURCE to TARGET requires ConstraintName
ride-from SOURCE to TARGET requires <ConstraintA && ConstraintB && ...>
```

`SOURCE` and `TARGET` may be:

- a station label declared by that transport
- a management domain
- `any`

Examples:

```toposcript
transport AllFloorElevator is Elevator {
  station F1 at Floor1::main_elevator_hall { location = 0.0, departureRate = 0.4 }
  station F32 at Floor32::main_elevator_hall { location = 150.0, departureRate = 0.02 }
  station F88 at Floor88::main_elevator_hall { location = 390.0, departureRate = 0.02 }
  station FM2 at FloorM2::main_elevator_hall { location = -10.0, departureRate = 0.01 }

  ride-from OfficeMain to Hotel requires HotelAccess
  ride-from OfficeMain to Facilities requires ManagementOnly
  ride-from F32 to Hotel requires SpecialEscort
}
```

This authoring model supports both exact station policies and policies over floor categories without forcing authors to list every possible floor-to-floor pair.

## Operand Resolution

For each `ride-from SOURCE to TARGET` operand:

1. If the operand is `any`, match all stations on the transport line.
2. Match an exact station label declared by that transport.
3. If no station label matches, match a management domain.
4. If none match, emit a compiler error.

Station labels are transport-local and resolve to their declared `Submap::node` references. Submap names themselves are not operands.

## Relationship to Station Permissions

This feature is separate from station-level permission scopes.

Recommended semantic layers:

```text
on-depart:
  whether this station can be used as an origin.

on-arrive:
  whether this station can be used as a destination.

ride-from SOURCE to TARGET:
  whether a ride from this origin domain/floor to this destination domain/floor is allowed.
```

Station permissions answer:

> Can a user board or arrive at this station?

Ride policies answer:

> Is this origin-destination pair allowed on this transport line?

Both layers may apply to the same transport edge.

## TransportGraph Generation

The current transport graph model tends toward complete connectivity between stations on the same transport line, subject to station permission.

With ride policies, transport edge generation should become policy-aware:

1. Generate candidate station-to-station rides for the line.
2. Resolve each station's owning submap and effective management domain.
3. Check station-level `on-depart` and `on-arrive` permissions.
4. Check matching `ride-from` rules.
5. Generate a transport edge only if all applicable hard constraints pass.

If no `ride-from` rules exist for a transport, the initial compatibility behavior should remain:

```text
all station-to-station rides are allowed, subject to station permissions
```

## Policy Matching and Specificity

The initial design should define deterministic matching order.

Recommended specificity:

1. exact station to exact station
2. exact station to domain
3. domain to exact station
4. domain to domain
5. any to exact station
6. exact station to any
7. any to domain
8. domain to any
9. any to any

More specific rules should override less specific rules.

If two rules have equal specificity and conflict, the compiler should emit an error.

In the first implementation, if conflict resolution is expensive, the compiler may reject overlapping rules and require authors to make policies unambiguous.

## Template Override Caveat

Long-term, `override-managed-by` should probably only be allowed in concrete topo-map files, not reusable templates.

Reason:

```toposcript
submap Floor98 using StandardUpperFloor managed-by OfficeMain
submap Floor99 using StandardUpperFloor managed-by OfficeMain
```

If `StandardUpperFloor` contains:

```toposcript
topo-map StandardUpperFloor() override-managed-by Hotel {
  ...
}
```

then every reused floor becomes effectively `Hotel`, which may surprise authors.

This restriction is deferred because reliably distinguishing concrete maps from templates may require broader compiler changes. For now, this should be documented as an authoring caveat.

## Recommended Implementation Phasing

This feature is larger than traversal tags and uncertain access. It affects configuration grammar, transport grammar, compiler metadata, and transport edge generation.

Recommended order:

1. Implement traversal tags and `traversalPreference`.
2. Implement `subject-to`, `because`, `riskPreference`, and `riskSummary`.
3. Implement station-level `on-depart` and `on-arrive`.
4. Implement `managed-by`, `override-managed-by`, and `ride-from` policy.

The first two phases establish metadata propagation and route-response explanation patterns that this feature can reuse.
