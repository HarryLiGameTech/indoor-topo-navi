# Endpoint Design

Status: Implemented API contract.

Updated: 2026-10-02.

For authentication, runnable requests, response fields, and application error handling, start with the [HTTP API guide](http-api.md).

This document defines the navigation and authoring API replacing the former test/demo endpoints. It incorporates the agreed endpoint names, selection behavior, shared data predicates, transport parameter disclosure, and configurable CORS policy. The backend, MCP client, and editor use these endpoints; the former test/demo handlers have been removed.

Project-derived constraint discovery is included in authoring validation. The editor consumes declaration names and scope without treating evaluated false constraints as absent.

## Scope

- Use stable building IDs independently of the eventual map-storage service. A building ID is not a filesystem path or storage URL.
- Preserve the existing compiler, routing algorithms, access rules, and finalized [traversal preferences](roadmap/traversal-preference-spec.md).
- Separate navigation results, authoring diagnostics, and operator controls.
- Keep compilation parameters in request bodies and use one method per operation.
- Defer map-storage APIs and readiness checks until the storage architecture is finalized.
- Defer native scheduled/runtime escalator reversal. Time-dependent `ride-from` rules may use `timeOfDay` in compilation parameters; changing those parameters requires a corresponding compiled result. Such policies do not mutate an existing graph or override a declared one-way direction.

Existing map-store and authentication routes outside `/api/v1` are not redesigned here.

## Endpoint inventory

All paths below are relative to `/api/v1`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Existing HTTP liveness check |
| GET | `/buildings` | List available building IDs and optional display names |
| POST | `/buildings/{buildingId}/find-route` | Calculate a route |
| POST | `/buildings/{buildingId}/maps/query` | Query submaps with summary or detailed metadata |
| POST | `/buildings/{buildingId}/nodes/query` | Query or expand nodes; return all nodes when selection and filtering are omitted |
| POST | `/buildings/{buildingId}/edges/query` | Query directed walking edges with summary or detailed metadata |
| POST | `/buildings/{buildingId}/nodes/neighbors` | Resolve one selected node and return its outgoing walking neighbors, with optional details |
| POST | `/buildings/{buildingId}/transports/query` | Query elevators, escalators, and stairs with optional details; always include every transport `let params` field |
| POST | `/authoring/validate` | Validate submitted TopoScript and return structured authoring diagnostics |

Building operations load server-managed projects. Only authoring validation accepts source files. There is no consumer endpoint for arbitrary-source route execution, forcing recompilation, invalidating caches, or returning internal compiler objects.

`GET /buildings` reports the available inventory. Inclusion does not assert successful compilation for every parameter set or the availability of a route. The existing `/health` remains a liveness check, not a readiness check.

## Common request rules

- `{buildingId}` is a canonical identifier returned by the building catalogue. Human-language building resolution remains a client/MCP responsibility.
- All operation inputs other than the building ID belong in a JSON body. POST queries require a JSON object; `{}` is valid when the operation requires no compilation inputs or explicit selection.
- `userParams` contains compilation-time values, with an empty object as the default. Required inputs are validated against project declarations. Missing, unknown, or incorrectly typed inputs produce explicit input errors; the API does not invent access permissions.
- `traversalPreference` affects route selection and remains separate from compilation parameters and cache identity.
- IDs are exact and case-sensitive. Fuzzy place-name interpretation remains in MCP; it is not part of identifier disambiguation.
- Unknown request fields are rejected. A supplied `selection: null` is invalid; omission means no selection filter.
- Topology operations accept an optional top-level `filter` predicate, combined with `selection` using AND. Omitting `filter` applies no predicate; `filter: null` and `filter: {}` are invalid.
- Query results describe the building compiled with the supplied parameters. A listed node or transport does not guarantee reachability from a particular origin.
- Results use stable ordering. Collection queries do not silently truncate their results. If a resource limit prevents a complete response, return an explicit error instead of an apparently complete subset.

Cache lookup and compilation reuse are internal implementation details. Storage paths, cache hashes, and `forceRecompile` are not part of the consumer contract.

## Topology response detail

All five topology operations accept a top-level `inDetail: boolean`: `maps/query`, `nodes/query`, `edges/query`, `nodes/neighbors`, and `transports/query`.

- Default: `false`. Omission requests the summary representation.
- `true` returns the same selected and filtered entities with their detailed metadata.
- Only JSON booleans are accepted. Strings such as `"true"`, numbers, and null are invalid.
- `inDetail` belongs alongside `userParams`, `selection`, and `filter`; it is not a compilation parameter or a selection filter.
- The response echoes the effective `inDetail` value at the top level. A detailed record retains its summary fields and adds the fields described below. Fields omitted in summary mode must not be represented as empty values that could be mistaken for actual data.
- Detail mode does not change identifier resolution, selection, predicate evaluation, ordering, limits, permissions, edge direction, traversal costs, or the compilation cache identity. A detail projection failure returns an error rather than silently falling back to a summary.
- `find-route`, `/buildings`, `/health`, and authoring validation are outside this switch. In particular, route action and uncertainty information is not suppressed by a summary option.

| Operation | `inDetail: false` | Additional content with `inDetail: true` |
|---|---|---|
| `maps/query` | Map IDs and declared display names when available | Complete compiled map attributes/metadata |
| `nodes/query` | Qualified `nodeId`, `mapId`, `localNodeId`, and display name when available | Complete compiled node `attributes`, including custom fields |
| `edges/query` | Map ID, qualified endpoints, traversal cost, tags, and required actions | Complete compiled edge `attributes` |
| `nodes/neighbors` | Resolved source ID, neighbor identity/display fields, representative edge cost, edge tags, required actions, and limit/count metadata | Complete neighbor `nodeAttributes` and representative `edgeAttributes` |
| `transports/query` | Transport ID, type, display name, **complete `params`**, and station labels/qualified node IDs | Applicable station locations, permissions and attributes, plus compiled transport attributes and direction/ride-permission information |

Display names come from available map metadata, not invented names. Unavailable display names are null. Applicable detailed fields must distinguish an absent value from a real zero, false, or empty value. The API must not expose internal interpreter environments or graph implementation objects as metadata.

Transport `params` is deliberately complete in both modes. `inDetail: false` reduces other transport metadata; it never weakens the requirement to return everything declared in `let params`.

## Shared data filtering

### Service contract and execution order

The service layer provides one reusable predicate model and evaluator for `maps/query`, `nodes/query`, `edges/query`, `nodes/neighbors`, and `transports/query`. Predicates operate on query records with public identity fields and compiled metadata. They support custom attribute and parameter names without adding a controller branch for each field.

Requests use a structured JSON predicate in the top-level `filter` field. The initial contract supports combinations of field comparisons, membership tests, and presence checks. It does not accept executable code, field-to-field calculations, or expression strings such as `capacity != null && tags.include("facility")`. A future expression parser can translate such syntax into the same predicate model without changing its evaluation rules.

Process each request in this order:

1. Validate the request and the complete predicate tree, including branches that might later be short-circuited. Invalid predicates must fail even when selection would produce no candidates.
2. Obtain the building compiled with the supplied `userParams` and resolve the endpoint's selection. Required-resource and ambiguous-identifier errors retain their existing behavior.
3. Construct candidate query records. For neighbors, choose the representative edge for each distinct destination before evaluating the predicate.
4. Retain candidates that satisfy both selection and the predicate. Filtering sees the full queryable metadata regardless of `inDetail`.
5. Apply the endpoint's stable ordering, compute any matching-result count, and apply an explicitly supported result limit.
6. Project summary or detailed response fields according to `inDetail`.

Omitting both selection and filtering returns the complete collection for collection queries. Neighbors still requires a source node. A valid predicate with no matches returns an empty collection. Filtering does not imply an implicit result limit.

`filter` and `inDetail` do not change compilation inputs or the compilation cache identity. Filtering discovery records does not remove graph elements, change access permissions, or apply routing bans. Route preferences remain part of `find-route`, which does not accept this `filter` field.

### Predicate forms

Each predicate object has exactly one of these forms:

| Form | Meaning |
|---|---|
| `{"and": [predicate, ...]}` | Every child must match; the list must be nonempty |
| `{"or": [predicate, ...]}` | At least one child must match; the list must be nonempty |
| `{"not": predicate}` | Boolean negation of one child |
| `{"field": "path", "op": "operator", "value": ...}` | A comparison or membership test |
| `{"field": "path", "op": "operator"}` | A presence or null check that takes no value |

Forms cannot be mixed in one object. Unknown keys and operators are invalid. Operator names are case-sensitive.

| Operator | `value` | Meaning |
|---|---|---|
| `eq`, `ne` | Required JSON value, including null | Equality or inequality for a present field |
| `lt`, `lte`, `gt`, `gte` | Required number | Numeric comparison; a nonnumeric field does not match |
| `in` | Required list | The present field equals at least one listed value; an empty list matches nothing |
| `contains` | Required JSON value | A list-valued field contains an equal element; non-list fields do not match |
| `exists` | Must be omitted | The field is present, including when its value is null |
| `isNull` | Must be omitted | The field is present and explicitly null |
| `isNotNull` | Must be omitted | The field is present and non-null |

Equality preserves JSON value types, except that numbers compare by numeric value: `1` and `1.0` are equal, while `"1"`, `1`, and `true` are distinct. Strings match exactly and case-sensitively. Lists compare in order; records compare by keys and values independently of key order. Membership uses these same equality rules. Numeric comparison must not narrow integer values or coerce strings or booleans into numbers. No substring, fuzzy, wildcard, or regular-expression matching is implied by `contains`.

A missing field is distinct from an explicit null. All leaf predicates return false for a missing field, including `ne`; use `{"not": {"field": "path", "op": "exists"}}` to select missing fields. For a present field, `ne` is the complement of `eq`, including across different value types. Thus `ne` with `value: null` is equivalent to `isNotNull`.

Boolean `not` negates the child's result normally. For example, negating a tag-membership test includes records whose tag list is absent; add an `exists` check when absence should be excluded. A present field of the wrong kind for numeric comparison or list membership simply does not match. Invalid predicate syntax or operand shapes are request errors, not nonmatches. These rules keep evaluation predictable across heterogeneous custom metadata.

### Field paths and query records

Field paths start at the endpoint's query record, not its response envelope or an internal compiler object. A string path uses dots between record keys, for example `params.inspection.intervalDays`. For literal keys containing dots, `field` may instead be a nonempty list of exact key segments, for example `["attributes", "vendor.capacity"]`. Empty paths and empty path segments are invalid. Keys are case-sensitive and are not trimmed or normalized.

Paths traverse records only. Missing keys, or an intermediate value that is not a record, resolve as missing. The initial contract has no array indexing, wildcard traversal, or per-element predicate binding; `contains` tests a list at the selected path.

| Operation | Queryable record fields |
|---|---|
| `maps/query` | `mapId`, `displayName`, `attributes` containing compiled map metadata |
| `nodes/query` | `nodeId`, `mapId`, `localNodeId`, `displayName`, `attributes` |
| `edges/query` | `mapId`, `fromNodeId`, `toNodeId`, `cost`, `tags`, `requiredActions`, `attributes` |
| `nodes/neighbors` | Neighbor `nodeId`, `mapId`, `localNodeId`, `displayName`; representative-edge `cost`, `tags`, `requiredActions`; `nodeAttributes` and `edgeAttributes` |
| `transports/query` | `transportId`, `type`, `displayName`, `params`, `stations`, `attributes`, `direction`, `ridePermissions` |

Identity and metadata values use the same meaning and value types as the detailed endpoint records. Derived edge `tags` and `requiredActions` must agree with their compiled attributes; they do not incorporate destination-node tags. `stations` uses the detailed station records regardless of `inDetail`. Custom metadata is nested under `attributes`, `nodeAttributes`, `edgeAttributes`, or `params`; it cannot shadow identity fields. A query record does not expose fields beyond its defined public metadata contract.

An unknown top-level field is invalid for that endpoint. An unknown custom key inside a metadata record is a valid path that resolves as missing where absent. Currently compiled node attributes have no explicit null variant; absent node attributes therefore use the missing-field rules. Existing nullable response fields, such as an unavailable `displayName`, remain explicitly null.

For example, a node query can select tagged facilities having a custom non-null capacity attribute:

```json
{
  "userParams": {},
  "selection": {"mapId": "Floor1"},
  "filter": {
    "and": [
      {"field": "attributes.capacity", "op": "isNotNull"},
      {"field": "attributes.tags", "op": "contains", "value": "facility"}
    ]
  },
  "inDetail": false
}
```

`capacity` and `facility` are illustrative values, not newly required attributes or additions to the [tagging vocabulary](manuals/tagging_guide.md). MCP can interpret user wording and submit canonical tags; the service evaluator performs exact comparisons. Multiple acceptable tags use `or` with `contains`; requiring every listed tag uses `and`; excluding a tag uses `not` with `contains`.

A transport query can use `{"field": "params.capacity", "op": "gte", "value": 12}`. This tests the evaluated declared parameter, not an undeclared routing default. A transport that omits this parameter does not match, even if its core routing model supplies a default capacity.

### Implementation boundary and errors

Use an immutable predicate tree and a pure evaluator shared by endpoint-specific query-record adapters. Keep value lookup and comparison out of individual controllers. A scan over the selected candidates is sufficient initially; indexes can be added without changing predicate semantics. The evaluator requires no changes to corelang or graph construction algorithms. Compilation results preserve discovery metadata independently of the routing graph, including complete declared transport parameters. Cache identity includes the discovery format version and typed compilation parameters.

Validate supported operators, field paths, and operand shapes before evaluation. Return HTTP 400 with `INVALID_FILTER`, including a JSON Pointer `details.path` into the request and an actionable explanation. Bound predicate depth, total predicates, path length, literal size, and membership-list size through service configuration; exceeding a structural bound returns HTTP 400 with `FILTER_TOO_COMPLEX`. Defaults are 16 levels of predicate/literal depth, 256 predicates, 16 field-path segments, 1,024 characters per field path, 16,384 UTF-8 bytes per literal, and 256 values in an `in` list. Configure these through `platform.api.filter.max-depth`, `max-predicates`, `max-path-length`, `max-literal-bytes`, and `max-membership`; `max-depth` also bounds field-path segments. An execution resource limit uses HTTP 503 under the common error rules and must never return a successful partial collection.

Filters use only the defined data operations. They cannot invoke methods, evaluate TopoScript, inspect interpreter environments, or access server resources. Existing compilation and metadata-projection errors retain their defined error behavior; filtering is not a way to silently discard malformed metadata.

## Route calculation

```http
POST /api/v1/buildings/swfc/find-route
Content-Type: application/json
```

```json
{
  "startNode": "LowerLobby::gate_1",
  "endNode": "LowerLobby::cafe",
  "userParams": {
    "haveStaffCard": false,
    "haveManagementCard": false,
    "haveRoomKey": false,
    "id": 0,
    "aggregatedWeight": 0
  },
  "traversalPreference": {
    "routePlanningPreference": "MinimizeTime",
    "banTags": [],
    "riskPreference": "conservative"
  }
}
```

Route endpoints use qualified `mapId::localNodeId` references. The short-ID resolution specified for neighbors does not implicitly change route endpoint resolution.

Support native `MinimizeTime`, `MinimizeTransfers`, `MinimizePhysicalDemands`, and `MinimizeCustomData` objectives. Preserve hard `banTags`, tolerance-based `minimizeTag`, and the existing risk-preference behavior for the original objectives. `maximizeTag` remains deferred and must not be accepted and silently ignored. Applied preferences and any uncertain-access fallback remain visible in the response.

The response retains ordered structured steps, waypoint metadata, required actions, action events, and uncertainty information. It adds a numeric `totalCostSeconds` taken from the route's total traversal cost. Compilation messages, loaded-file inventories, cache hashes, and exception chains are excluded.

`routingMode` accepts `partiallyInformed` (the default, preserving the former API's `isHighRise: true`) or `fullyInformed` (the former `isHighRise: false`). The response echoes the selected mode. MCP explicitly requests `fullyInformed`, preserving its previous algorithm choice. These names select algorithms, independently of building height.

`MinimizeCustomData` requires `traversalPreference.customObjective`: either `{kind: "tagExposure", tag: "outdoor"}` or `{kind: "edgeAttribute", attribute: "effort", missingValue: 0}`. Optional `tieBreakers` contains `MinimizeTransfers` and `MinimizeTime` exactly once; this is also the default order. Custom cost is primary, without a near-tie allowance. The objective cannot coexist with `minimizeTag` or another primary mode. Numeric atomic-path costs must be finite and non-negative; absent attributes use the declared default (zero when omitted), and rides contribute zero to an atomic-path attribute sum. Invalid custom map data returns `INVALID_ROUTING_DATA` (422). See the [HTTP guide](http-api.md#route-calculation) for the complete request contract.

Primary search now uses compact location/last-line state. Partially informed routing searches transport landings with cached floor-level connections; fully informed routing searches physical edges. Both include complete-route walking and dwell costs. The response includes `transferCount`, `physicalDemand`, and, for custom objectives, `customCost` and the normalized applied objective. Permissive custom routing requires a strictly lower custom cost before selecting an uncertain alternative; tie-breaker improvements alone do not qualify.

Secondary-preference trade-offs and multiple-route presentation are assigned to a deferred agent-layer workflow. The target engine contract performs one native primary optimization per search, with deterministic tie-breakers and complete route metrics; the agent controller compares a bounded set of results under the user's tolerances. Requested tag-exposure metrics independent of the search objective and revision consistency still need a contract. See the [deferred architecture and rain-exposure example](roadmap/traversal-preference-spec.md#deferred-reliable-secondary-preferences-and-route-alternatives), including the limits of candidate-based comparison. Candidate-count controls and multi-route output belong to orchestration and are not part of the current endpoint contract. Existing engine `minimizeTag` remains supported pending an explicit client migration.

## Topology selection

### Maps

`POST /buildings/{buildingId}/maps/query` accepts `userParams`, `inDetail`, optional `filter`, and an optional `selection.mapIds` list. Omitting selection and filtering returns all compiled submaps, including maps with no nodes. An explicitly empty ID list selects nothing. Detailed mode expands map metadata without automatically embedding every node or edge; those collections have their own queries.

### Nodes

**Omitting both `selection` and `filter` returns all nodes in the parameter-specific compiled building.** This means every compiled submap, not a default floor, a sample, or only nodes reachable from some implicit origin. Omitting `selection`, or supplying `selection: {}`, leaves all nodes as candidates; an optional `filter` can then narrow that collection.

```json
{
  "userParams": {},
  "inDetail": false,
  "selection": {
    "mapId": "Floor1",
    "nodeIds": ["entrance", "hall"]
  }
}
```

- `mapId` selects one map. An unknown map produces `MAP_NOT_FOUND`.
- `nodeIds` selects exact local IDs or qualified `mapId::localNodeId` references. Omission selects all nodes within the selected scope; an empty list selects none.
- With no `mapId`, local IDs match every occurrence across maps. This is a collection query, so multiple matches are valid and are returned with qualified IDs.
- A qualified node reference and an explicit `mapId` must agree; contradictory selectors are rejected.
- Different selection fields are combined with AND; entries within an ID list are combined with OR. Unmatched node filters yield an empty collection.
- An optional top-level `filter` further restricts these candidates using the shared predicate rules; it does not change node-ID resolution.
- Always return qualified `nodeId`, `mapId`, `localNodeId`, and the display-name field. Include the complete compiled `attributes` when `inDetail` is true. Sort by qualified `nodeId`.

This endpoint covers the existing all-node, per-map-node, and single-node information use cases.

#### Expand a specific node

Use the same node query with a qualified selection and `inDetail: true`. A separate node-details endpoint is unnecessary.

```http
POST /api/v1/buildings/swfc/nodes/query
Content-Type: application/json
```

```json
{
  "userParams": {
    "haveStaffCard": false,
    "haveManagementCard": false,
    "haveRoomKey": false,
    "id": 0,
    "aggregatedWeight": 0
  },
  "selection": {
    "nodeIds": ["LowerLobby::cafe"]
  },
  "inDetail": true
}
```

With no additional filter, an existing selected node returns a one-element node collection with all its compiled attributes. A missing node returns an empty collection under the collection-query rules. Alternatively, select `mapId: "LowerLobby"` and `nodeIds: ["cafe"]`. An unqualified ID without a map may select multiple occurrences; use a qualified ID when expanding one specific result from an earlier query. Reuse the same `userParams` when expanding a result to retain its compilation context.

Omitting selection and filtering with `inDetail: true` returns all nodes with full details. `inDetail` does not impose an implicit selection or result limit.

### Walking edges

`POST /buildings/{buildingId}/edges/query` accepts `userParams`, `inDetail`, optional `filter`, and optional selection by `mapId`, qualified `fromNodeId`, and qualified `toNodeId`. Omitting selection and filtering returns all compiled directed walking edges. Selection fields and the predicate are combined with AND; no matching edges yields an empty collection.

Return qualified endpoints, traversal cost, tags, and required actions in both modes; add the complete compiled `attributes` in detailed mode. Preserve parallel edges as separate records. Transport rides are described through transport discovery and route results rather than being mislabeled as walking edges. Sort by map, source, destination, and cost, with deterministic handling of equal keys.

### Neighbors and automatic identifier disambiguation

`POST /buildings/{buildingId}/nodes/neighbors` requires `selection.nodeId`. It never defaults to all nodes or an arbitrary starting node.

```json
{
  "userParams": {},
  "inDetail": true,
  "selection": {
    "nodeId": "landing",
    "mapId": "Floor1"
  },
  "limit": 5
}
```

Resolution rules:

| Input | Resolution |
|---|---|
| `nodeId: "Floor1::landing"`, no `mapId` | Resolve that exact qualified node |
| `nodeId: "landing"`, `mapId: "Floor1"` | Resolve `Floor1::landing` |
| `nodeId: "Floor1::landing"`, `mapId: "Floor1"` | Accept the consistent qualification |
| Qualified `nodeId` contradicts `mapId` | Reject with `CONFLICTING_NODE_SELECTION` |
| Short `nodeId`, no `mapId`, exactly one matching node | Automatically resolve that node |
| Short `nodeId`, no `mapId`, multiple matches | Return `AMBIGUOUS_NODE_ID` and sorted qualified candidates |
| No matching node | Return `NODE_NOT_FOUND` |
| Missing, empty, or malformed `selection.nodeId` | Reject the request |

Automatic disambiguation is exact identifier resolution. It must not select the first map, guess a floor, or use fuzzy names to hide an ambiguity. An ambiguity response may include:

```json
{
  "error": {
    "code": "AMBIGUOUS_NODE_ID",
    "message": "Provide a mapId or a qualified nodeId.",
    "details": {
      "nodeId": "landing",
      "candidates": ["Floor1::landing", "Floor2::landing"]
    }
  }
}
```

The successful response includes `resolvedNodeId` and the effective `inDetail`. Neighbors are destinations of directly outgoing compiled walking edges from that node. They are not geometric nearest points, reverse-only connections, transport rides, or multi-step paths. A node with no outgoing walking edges returns an empty collection. Detailed mode expands each returned neighbor and its representative walking edge; it does not implicitly request the source node's full attributes. Use `nodes/query` to expand the source itself.

`limit` is a positive integer, defaulting to 5. Preserve the current distinct-neighbor behavior: where parallel edges reach the same neighbor, use the lowest normal traversal cost and that representative edge's metadata, with deterministic tie handling. Apply an optional `filter` to these neighbor records, then sort by cost and qualified node ID. Return the total matching distinct-neighbor count before the limit and whether the explicit limit truncated the matching result.

The predicate targets returned neighbors and their representative edges, not the source node. It cannot resolve an ambiguous source ID or replace the representative edge with a more expensive parallel edge that happens to match. Use `edges/query` to filter all parallel-edge alternatives individually. Do not infer that those alternatives have identical tags or required actions.

## Transport discovery and complete parameters

`POST /buildings/{buildingId}/transports/query` replaces the elevator-only endpoint. It covers `Elevator`, `Escalator`, and `Stairs` declarations.

```json
{
  "userParams": {},
  "inDetail": true,
  "selection": {
    "types": ["Elevator", "Escalator"],
    "transportIds": ["MainLift", "LobbyEscalator"]
  }
}
```

Selection and the shared `filter` predicate are optional. Without either, return all compiled transport declarations. Type and ID selectors and the predicate are combined with AND, values within each selector list with OR, and an empty supplied list selects nothing. Unknown type names are invalid input; valid filters with no matches return an empty collection. Predicates can inspect complete evaluated declared `params`, including nested custom fields, in both detail modes.

Each transport record includes, regardless of `inDetail`:

- `transportId`: its exact declared identifier.
- `type`: `Elevator`, `Escalator`, or `Stairs`.
- `displayName`: the declared display name when present, otherwise null.
- `params`: **every field from that transport's evaluated `let params` record**.
- `stations`: station labels and qualified node references. Detailed mode additionally returns applicable station locations, permissions, and attributes, plus compiled transport attributes and direction/ride-permission information.

For example, this declaration:

```toposcript
transport LobbyEscalator is Escalator {
  let params = {
    time = 18.0,
    distance = 8.5,
    operatorNote = "East atrium",
    inspection = {team = "Facilities", intervalDays = 30}
  }
  station Lower at Floor1::landing {}
  station Upper at Floor2::landing {}
}
```

must retain all four parameter fields in the result:

```json
{
  "transportId": "LobbyEscalator",
  "type": "Escalator",
  "displayName": null,
  "params": {
    "time": 18.0,
    "distance": 8.5,
    "operatorNote": "East atrium",
    "inspection": {"team": "Facilities", "intervalDays": 30}
  },
  "stations": [
    {"label": "Lower", "nodeId": "Floor1::landing"},
    {"label": "Upper", "nodeId": "Floor2::landing"}
  ]
}
```

The example is a summary transport record (`inDetail: false`), not a complete response envelope. The response envelope echoes `inDetail`. Detailed mode must additionally return applicable station and transport metadata without fabricating absent values; all four `params` fields remain present in either mode.

Parameter projection requirements:

1. Preserve built-in and custom keys, including fields unused by routing. Do not restrict output to properties retained by the current core transport classes.
2. Return evaluated values for the supplied `userParams`, not raw expression text. Changing compilation inputs may change these values.
3. Preserve JSON-compatible booleans, numbers, strings, lists, and nested records. Do not stringify entire records, drop nested fields, or narrow integer values during projection.
4. If a value cannot be represented by the defined JSON projection, fail explicitly with `UNSERIALIZABLE_TRANSPORT_PARAM`, identifying the transport and parameter path. Never report success with fields silently omitted. The projection of any additional core value kinds must be specified before accepting them in this response.
5. `params` contains the evaluated declared record, not unrelated local definitions, inherited root variables, or interpreter environments. Compiler-injected defaults must not be presented as explicitly declared fields. Any future effective-default view must be separate.
6. Every returned transport includes its complete `params`, including when `inDetail` is false. The detail switch must not remove these fields; clients may condense the response for presentation.
7. Inventory comes from compiled declarations, not naming conventions for hall nodes or only transports that happen to have usable ride edges. A declared transport with no permitted ride must not disappear solely for that reason. Listing a transport is not a promise that it is usable for a given journey.

Station data, `direction`, and `ride-from` policies are separate declarations and are not inserted into `params`. Detailed records expose `direction` as allowed directed station-reference pairs (`fromNodeId`, `toNodeId`), or null when no direction is declared. `ridePermissions` contains evaluated `source`, `target`, and `allowed` policy records. These describe separate inputs to ride eligibility; direction and policies still intersect in the planner.

Detailed stations include their declared location when present, effective hard `permissions`, declared `attributes`, and any departure/arrival `uncertainAccess` reasons. Discovery inventory is captured from elaborated declarations, including transports with no permitted ride. Map `attributes.params` preserves the map's declared parameter record; management-domain overrides are exposed when declared.

## Authoring validation and constraint migration

`POST /authoring/validate` accepts JSON with `files`, a filename-to-TopoScript-source map, and optional `userParams`. It returns structured diagnostics with stages and source locations where available. Invalid submitted source is an authoring result, not an unexplained internal server error.

Only JSON is accepted. The endpoint does not publish a map, mutate installed projects, or return internal compiler/graph objects.

The response always includes `valid`, `constraints`, `parameters`, and `diagnostics` after source inspection. A successful compilation returns HTTP 200 and `status: "success"`. Parse or compilation failures return HTTP 422 with the common error envelope alongside the catalogue; invalid invocation parameters return HTTP 400 with the catalogue. Input-shape and source-size failures can be rejected before inspection.

`constraints` contains `entries` and `complete`. Each entry has a `name`, `scope: {kind, name}`, `source: {file, line, column}`, and `evaluation: "notEvaluated"`. Scope kinds are `root`, `map`, and `transport`; line and column are one-based. The catalogue describes declarations rather than truth values, so false constraints remain available for completion. Root declarations apply across the project; local declarations belong to their declared map or transport. Source metadata distinguishes the declaration's origin even when maps are instantiated under aliases.

`parameters` lists root invocation parameter names, declared types, and source locations. Missing parameters do not prevent declaration discovery. Parsing uses the configured project's referenced sources, with token recovery for unfinished declaration bodies. `complete: false` identifies a partial catalogue; diagnostics explain parse failures. Compilation diagnostics identify the compile stage; source locations are included where available rather than guessed from an exception. Validation does not imply catalogue entries have been separately evaluated.

The editor sends its current source after a debounce and cancels superseded requests. It shows root and current-map declarations. Its current single-map editor submits a generated configuration plus that map; it cannot invent constraints from an unavailable building root. `/test-constraints` and the old `/validate` overloads have been removed.

Authoring limits default to 512 files, 4,194,304 UTF-8 source bytes, and two concurrent inspections/compilations. Configure `platform.api.authoring.max-files`, `max-source-bytes`, and `max-concurrent-compilations`. Saturation returns HTTP 503 `AUTHORING_BUSY`; file/size limits return HTTP 400. These concurrency and input limits do not impose a hard execution deadline on a running interpreter.

## Responses and errors

Use a consistent error envelope:

```json
{
  "error": {
    "code": "NO_ROUTE_FOUND",
    "message": "No route satisfies the supplied request.",
    "details": {}
  }
}
```

| HTTP status | Meaning |
|---|---|
| 200 | Successful route or query, including empty collection results; successful validation |
| 400 | Invalid request shape, contradictory selection, invalid or overly complex filter, or invalid compilation inputs |
| 401 | Required authentication is missing or invalid |
| 403 | Caller lacks permission for the operation or building |
| 404 | A requested building, explicitly selected map, or required single node does not exist |
| 422 | Valid request with no route, ambiguous single-node selection, unsupported requested preference, or invalid submitted TopoScript |
| 503 | Service or a required resource is temporarily unavailable, or execution reaches a defined resource limit |
| 500 | Unexpected internal failure or an invalid server-managed project |

An installed project that fails compilation is distinguished from a client submitting invalid source to authoring validation. An unrepresentable transport parameter is a server-managed project/projection failure and must identify its field safely.

Empty collection queries succeed; a missing required single resource does not. Do not translate every no-route result into an access-parameter failure merely because `userParams` was supplied. Parameter-specific explanations require evidence.

Consumer errors expose stable codes and actionable details. They exclude exception class names, stack traces, server paths, and exception cause chains. Detailed authoring diagnostics belong to the authoring contract and must not expose unrelated server internals.

## Authorization and configurable CORS

Access policy is explicit per operation:

- `/health` permits anonymous liveness probes.
- Navigation and discovery require the configured service credential (`TOPONAVI_SERVICE_TOKEN`, sent as a Bearer token). `TOPONAVI_ANONYMOUS_BUILDINGS` optionally supplies a comma-separated, case-sensitive list of demo building IDs. Anonymous catalogue responses expose only those stored buildings. Authenticated clients can query the stored inventory.
- Project source storage (`/api/v1/projects`) always requires the service credential, including for anonymously computable buildings. See the [HTTP API guide](http-api.md#store-and-manage-projects).
- Authoring validation requires the service credential by default. `TOPONAVI_ALLOW_GUEST_AUTHORING=true` explicitly enables guest validation while retaining source-size and concurrent-compilation limits. There are no user accounts or platform JWTs.
- Operator cache operations are not included in the consumer API. If retained, they require a separate protected administrative contract.

Do not retain a blanket `/api/v1/**` authorization exception. Authentication authorizes service access; `userParams` describes compilation inputs and is not proof of a user's identity or possession of a credential.

`allowCors` must be configurable through application configuration, with environment overrides. Application YAML:

```yaml
platform:
  api:
    allow-cors: ${TOPONAVI_ALLOW_CORS:false}
    anonymous-buildings: ${TOPONAVI_ANONYMOUS_BUILDINGS:}
    authoring:
      allow-guests: ${TOPONAVI_ALLOW_GUEST_AUTHORING:false}
    cors:
      allowed-origins: ${TOPONAVI_CORS_ALLOWED_ORIGINS:}
      allowed-methods: GET,POST,OPTIONS
      allowed-headers: Content-Type,Authorization
      allow-credentials: ${TOPONAVI_CORS_ALLOW_CREDENTIALS:false}
```

- `allow-cors: false` does not emit cross-origin permission headers. Same-origin clients and server-to-server MCP calls remain usable subject to authentication.
- `allow-cors: true` enables CORS for explicitly configured origins, methods, and headers. It does not mean that every origin is allowed.
- Environment-supplied origin lists are comma-separated and trimmed. Reject invalid enabled configurations, including an empty origin list, wildcard origins, or values that are not explicit HTTP(S) origins.
- Allowed preflight requests are handled without requiring an application credential on the OPTIONS request. The actual operation still enforces authorization.
- CORS controls browser access; enabling it must not bypass authentication, building permissions, or execution limits.
- These settings cover the redesigned `/api/v1` API. Authentication and map-store routes outside this scope require their own explicit policy review.

The configuration is consumed by the application. CORS remains disabled by default. MCP accepts `TOPONAVI_API_TOKEN` to send a platform bearer token to protected endpoints; otherwise an explicitly configured anonymous building is required.

## Migration

| Previous endpoint | Replacement or disposition |
|---|---|
| `/health` | Liveness behavior retained |
| `/available-buildings` | `/buildings`; MCP startup catalogue loading migrated |
| `/quick-demo-navigation` | POST `/buildings/{buildingId}/find-route` |
| `/quick-demo-available-submaps` | `/buildings/{buildingId}/maps/query` |
| `/quick-demo-all-available-nodes`, `/quick-demo-available-nodes-in-map`, `/quick-demo-node-info` | `/buildings/{buildingId}/nodes/query` with optional selection and predicate filtering; `inDetail: true` replaces attribute expansion and specific-node details |
| `/quick-demo-all-available-edges` | `/buildings/{buildingId}/edges/query` |
| `/quick-demo-proximity-nodes` | `/buildings/{buildingId}/nodes/neighbors` with required node selection |
| `/quick-demo-elevators` | `/buildings/{buildingId}/transports/query`; the elevator MCP tool selects `Elevator` and maps `simple` to `inDetail` |
| JSON `/validate` | `/authoring/validate`; editor requests and diagnostics migrated |
| Multipart `/validate` | Removed; callers must submit the JSON authoring contract |
| `/test-constraints` | Removed; authoring validation returns a project-derived declaration catalogue consumed by the editor |
| `/test-building-query` | Removed together with the registered MCP placeholder tool |
| `/testNavigate` | Arbitrary-source navigation entry point removed |
| `/quick-demo-compile-check` | Removed; submitted-source diagnostics are available through authoring validation |
| `/cache/status`, `/cache/invalidate` | Removed from the consumer API; operator tooling requires a separate contract |

Backend, editor, and MCP must be deployed together. This refactor removes legacy handlers without compatibility aliases; external callers must migrate to the paths and JSON bodies above. There is no GET-to-POST conversion.

Replace the old `withNodesAttributes` and `simple` switches with `inDetail`. Existing MCP place resolution and filtered discovery must explicitly request the details they consume; the new summary default must not silently strip required attributes. An old `simple: true` transport request maps to `inDetail: false`, and `simple: false` to `inDetail: true`, while both new modes include complete transport parameters.

MCP uses shared service predicates after canonical field/tag interpretation. Explicit predicate composition preserves its existing OR-within-field and AND-across-fields behavior. Fuzzy vocabulary discovery still requires metadata queries; filtering support alone does not replace that discovery step. Detail fields are requested when the caller needs to consume them, not merely to make a predicate work. Regression tests cover exact tag inclusion, exclusion, missing attributes, and filter-before-limit behavior.

The commented `CompilerController.java` and `DTOController.java`, together with the unused legacy navigation request DTOs, have been removed.

## Regression checks

- Node queries without selection or filtering return the complete compiled node collection; scoped queries, predicates, and empty selections follow the documented rules.
- All five topology operations default `inDetail` to false, accept only booleans, echo the applied mode, and preserve entity selection and ordering across summary/detail requests.
- All five topology operations share predicate semantics, combine predicates with selection, and filter full query records before result limits and detail projection. Filters do not affect compilation cache identity, graph contents, or route preferences.
- Predicate tests cover nested boolean composition, custom and dotted-key paths, missing versus null, negation, mixed numeric comparisons, exact string/list membership, structural equality, heterogeneous field types, and empty membership lists.
- Invalid operators, operand shapes, field roots, and predicate forms fail even for empty candidate collections or short-circuited branches. Structural bounds produce explicit errors without partial results.
- Expanding a qualified node with `inDetail: true` returns its full compiled attributes, including custom fields. Requesting all nodes in detail does not silently truncate results.
- Neighbor requests require a node ID and cover qualified IDs, map-scoped local IDs, unique unqualified IDs, conflicting qualification, missing nodes, and ambiguous candidates.
- Neighbor direction, parallel-edge selection, filtering after representative-edge choice, matching counts before limits, ordering, and explicit result limits are tested. Predicates cannot disambiguate the source or substitute a more expensive parallel edge.
- Transport queries cover all three transport types, custom and nested parameters, parameter-dependent values, and declarations without permitted rides. Complete declared parameters survive and are filterable in both detail modes; absent declared parameters are not replaced with routing defaults for filtering.
- Editor validation and constraint completion use project-derived data; removal of `/test-constraints` is verified against the migrated editor.
- MCP navigation, place resolution, filtered discovery, and elevator discovery use the new contracts without losing required action or uncertainty information.
- CORS enabled/disabled behavior, origin selection, preflight handling, and authorization are tested independently.
- Legacy handlers are removed, and the migrated clients use the replacement contracts. Readiness, map-storage design, `maximizeTag`, and native runtime reversal remain deferred.
