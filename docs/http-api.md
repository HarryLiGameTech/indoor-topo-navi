# HTTP API guide

Use the TopoNavi HTTP API to discover a building's topology, inspect transport services, calculate routes, and validate TopoScript projects. All endpoints in this guide are relative to `/api/v1`.

## Connect and make a first request

Use the service URL supplied by your deployment and a platform JWT obtained through its sign-in flow. Send the token as `Authorization: Bearer <token>`; an identity provider's access token is not a substitute for the platform token.

The following shell examples use a local service and the shipped SWFC project. Replace the URL and token before running them.

```sh
export TOPONAVI_URL='http://localhost:8080'
export TOPONAVI_TOKEN='<platform-jwt>'

curl -sS "$TOPONAVI_URL/api/v1/buildings" \
  -H "Authorization: Bearer $TOPONAVI_TOKEN"
```

The response contains exact, case-sensitive building IDs:

```json
{"status":"success","buildings":["indigoBJ","nbc4","swfc","trent"]}
```

The list depends on the deployment and your access. A listed building is installed; listing does not compile it or guarantee that a route exists.

To calculate a short SWFC route:

```sh
curl -sS "$TOPONAVI_URL/api/v1/buildings/swfc/find-route" \
  -H "Authorization: Bearer $TOPONAVI_TOKEN" \
  -H 'Content-Type: application/json' \
  --data-binary '{
    "startNode": "LowerLobby::gate_1",
    "endNode": "LowerLobby::cafe",
    "userParams": {
      "haveStaffCard": false,
      "haveManagementCard": false,
      "haveRoomKey": false,
      "id": 0,
      "aggregatedWeight": 0
    },
    "routingMode": "fullyInformed"
  }'
```

With the shipped SWFC data, this route has `totalCostSeconds: 5`. Read its ordered `steps` to build navigation instructions. The destination's display name is `Starbucks Coffee`.

Authentication is required by default for building operations and authoring validation. A deployment can allow anonymous access to selected demo buildings and independently allow guest authoring. In that case, omit the authorization header. Only `GET /health` is always public.

## Request conventions

- Send POST inputs as a JSON object with `Content-Type: application/json`. Do not place operation inputs in URL query parameters.
- Use the exact building ID from `/buildings`, URL-encoded as one path segment. Node references use `mapId::localNodeId`, for example `LowerLobby::cafe`.
- Supply the project's declared compilation inputs in `userParams`. Omission means `{}`; required inputs are not given invented defaults. Unknown inputs, missing inputs, and incorrect JSON types are errors.
- `Bool`, `Int`, `Float`, and `String` inputs accept JSON booleans, signed 64-bit integers, finite numbers, and strings respectively. Do not send numeric or boolean inputs as strings.
- Reuse the same `userParams` across discovery, detail expansion, and routing to keep the same compilation context. These values describe map policies; they do not authenticate a caller.
- Unknown request fields are rejected. Optional objects such as `selection` may be omitted, but must not be null.

SWFC requires the five inputs shown above. Other projects declare their own inputs. Obtain them from the project's documentation or, when you have its source files, the authoring endpoint's `parameters` catalogue. `/buildings` does not return parameter schemas.

## Endpoint reference

| Method | Path | Result |
|---|---|---|
| GET | `/health` | `{"status":"ok","service":"toponavi-web"}`; liveness only |
| GET | `/buildings` | `{"status":"success","buildings":[...]}` |
| POST | `/buildings/{buildingId}/find-route` | Route steps, cost, and applied preferences |
| POST | `/buildings/{buildingId}/maps/query` | `maps` array |
| POST | `/buildings/{buildingId}/nodes/query` | `nodes` array |
| POST | `/buildings/{buildingId}/edges/query` | `edges` array |
| POST | `/buildings/{buildingId}/nodes/neighbors` | `neighbors` array and count/limit metadata |
| POST | `/buildings/{buildingId}/transports/query` | `transports` array |
| POST | `/authoring/validate` | Validation diagnostics, constraints, and parameter declarations |

### Route calculation

`POST /buildings/{buildingId}/find-route` accepts:

| Field | Required/default | Meaning |
|---|---|---|
| `startNode`, `endNode` | Required strings | Qualified node references |
| `userParams` | `{}` | Compilation inputs |
| `routingMode` | `"partiallyInformed"` | `"partiallyInformed"` or `"fullyInformed"`; selects the routing algorithm |
| `traversalPreference` | `{}` | Options below |

The modes are algorithm choices, not inferred building heights. `partiallyInformed` searches a hierarchy of transport landings and the exact endpoints, computing and caching floor-level walking connections as needed. `fullyInformed` searches individual walking and ride edges. Both optimize the selected primary objective using complete-route costs, including origin, transfer, and destination walking and intermediate-node dwell. Tag preferences do not change the selected primary-search strategy.

Preference fields:

| Field | Default | Accepted values / behavior |
|---|---|---|
| `routePlanningPreference` | `"MinimizeTime"` | Also `"MinimizeTransfers"`, `"MinimizePhysicalDemands"`, and `"MinimizeCustomData"` |
| `customObjective` | Required for `MinimizeCustomData`; otherwise omitted | Custom cost and ordered tie-breakers, below |
| `banTags` | `[]` | Hard exclusions by exact tag |
| `minimizeTag` | Omitted | One tag whose exposure should be reduced among near-tie routes |
| `riskPreference` | `"conservative"` | Also `"permissive"` and `"aggressive"` |

For `minimizeTag`, the near-tie allowance is +60 seconds, +2 transport-line changes, or +25% physical demand, according to the primary preference. Only that primary objective supplies the allowance. See the [traversal preference specification](roadmap/traversal-preference-spec.md) for exposure and physical-demand scoring. `maximizeTag` is not supported and returns an error.

`MinimizeCustomData` is a primary objective. For example:

```json
{
  "routePlanningPreference": "MinimizeCustomData",
  "customObjective": {
    "kind": "tagExposure",
    "tag": "outdoor",
    "tieBreakers": ["MinimizeTransfers", "MinimizeTime"]
  }
}
```

Put this object in `traversalPreference`. The planner minimizes custom cost first, then applies the two tie-breakers in the declared order. `tieBreakers` defaults to the order shown; both entries must occur exactly once, and reversing them is supported. Tie-breakers apply only when preceding scores are equal. There is no implicit time, transfer, or physical-demand tolerance in this mode.

Supported custom objectives:

- `{"kind":"tagExposure","tag":"outdoor"}`: sum tagged traversal seconds and explicit dwell seconds at tagged intermediate nodes. Point tags without dwell contribute zero; endpoint dwell is excluded.
- `{"kind":"edgeAttribute","attribute":"effort","missingValue":0}`: sum the specified top-level numeric attribute on every traversed atomic path. `Int` and `Float` are supported. Missing attributes use `missingValue`, which defaults to zero. Transport rides contribute zero for this metric; transport parameters and node attributes are not implicitly included. This is a direct attribute name, not a filter expression or executable function.

Custom numeric values and missing-value defaults must be finite and non-negative. A present nonnumeric, negative, or nonfinite value on an otherwise usable path returns HTTP 422 with `INVALID_ROUTING_DATA`. Invalid objective definitions return HTTP 400. `customObjective` is required with `MinimizeCustomData`, forbidden with other primary preferences, and cannot be combined with `minimizeTag`.

Conservative routing first searches without uncertain-access edges, then falls back to allowing them if no route exists. Permissive routing compares the two searches and uses uncertain access when it meaningfully improves the selected objective. Aggressive routing searches with uncertain access allowed. None of these modes overrides hard access restrictions. For `MinimizeCustomData`, permissive routing selects an uncertain alternative only for a strictly lower custom cost; improvements in tie-breakers alone do not relax access. Inspect the returned applied preference and uncertainty metadata even when requesting conservative routing.

Successful route fields:

| Field | Meaning |
|---|---|
| `status` | `"success"` |
| `totalCostSeconds` | Numeric total traversal time in seconds |
| `routingMode` | Applied primary-search strategy |
| `transferCount`, `physicalDemand` | Complete-route line changes and physical-demand score |
| `customCost` | Present for `MinimizeCustomData`; sum under the requested custom objective |
| `steps` | Ordered route legs |
| `appliedTraversalPreference` | Applied primary preference, risk preference, bans, requested `minimizeTag`, and normalized `customObjective` when present |
| `riskSummary` | Present when the route includes uncertain access; contains reasons and `routeNotNecessarilyAvailable` |
| `path` | Human-readable route text; use structured steps for application logic |
| `deprecations` | Field migration notices |

Steps have a one-based `step`, `type`, `costSeconds`, `waypoints`, `tags`, `requiredActions`, and `requiredActionEvents`. Walking types are `Walk` and `WalkStraight`, with `graph`, `from`, and `to`; straight segments can also contain `turnDirection` and `turnAtNode`. Transport steps use `type: "Transport"`, `description`, `fromGraph`, and `toGraph`. Preserve action events and any step-level uncertainty metadata when displaying instructions.

Route waypoints use a **local** `nodeId` plus `graph`; form `${graph}::${nodeId}` when querying their details. This differs from topology-query records, whose `nodeId` is already qualified. Waypoints also carry available display metadata and `isTrivial`/`isIntermediate` flags. Prefer `waypoints` over the legacy human-readable `namedWaypoints` field. Walking `from`/`to` strings are display text, not stable node IDs.

### Topology queries

All five topology endpoints accept `userParams`, optional `selection`, optional `filter`, and `inDetail` (default `false`). Neighbors additionally accepts `limit` (default `5`). Only JSON booleans are accepted for `inDetail`.

Each response has `status: "success"`, the effective `inDetail`, and its named result array. Collection queries return all matching records without pagination or an implicit limit. No matches produces an empty array. Neighbors is the exception: it requires a source node and applies its explicit/default limit.

| Endpoint | Selection fields | Summary record fields | Added with `inDetail: true` |
|---|---|---|---|
| `maps/query` | `mapIds: string[]` | `mapId`, `displayName` | `attributes` |
| `nodes/query` | `mapId: string`, `nodeIds: string[]` | `nodeId`, `mapId`, `localNodeId`, `displayName` | `attributes` |
| `edges/query` | `mapId: string`, qualified `fromNodeId` and/or `toNodeId` | `mapId`, endpoints, `cost`, `tags`, `requiredActions` | `attributes` |
| `nodes/neighbors` | Required `nodeId: string`; optional `mapId` | Destination node identity/display fields, `cost`, `tags`, `requiredActions` | `nodeAttributes`, `edgeAttributes` |
| `transports/query` | `transportIds: string[]`, `types: string[]` | `transportId`, `type`, `displayName`, full `params`, stations' `label` and `nodeId` | Station metadata, `attributes`, `direction`, `ridePermissions` |

Missing display names are null. Edge and neighbor `cost` values are normal traversal times in seconds. Walking edges are directed; parallel edges remain separate in `edges/query`. Transport rides are represented by transport discovery and route results.

Selection fields combine with AND; values within a list combine with OR. An empty supplied list selects nothing. Without selection or filtering, collection queries return the complete compiled collection. An explicit unknown map produces `MAP_NOT_FOUND`; unmatched node or transport list selectors produce empty collections.

For `nodes/query`, local IDs without `mapId` match all occurrences across maps. To expand exactly one node, use a qualified ID and `inDetail: true`. Conflicting qualifications and `mapId` values are errors. A node's presence in discovery does not guarantee that a route can reach it.

Transports have types `Elevator`, `Escalator`, or `Stairs`. Their `params` always contains all evaluated fields declared in `let params`, including custom and nested fields. Undeclared routing defaults are not inserted. Declared transports with no permitted ride still appear. In detail mode, `direction` contains directed station-node pairs, or null when no direction was declared; `ridePermissions` contains evaluated `source`, `target`, and `allowed` policy records. Station metadata includes applicable locations, permissions, attributes, and uncertainty information.

### JavaScript examples

The following helper uses the standard Fetch API in a browser or a JavaScript runtime that provides `fetch`. Set `baseUrl` and `token` for your deployment. HTTP errors throw an `Error` retaining the response's `status`, `code`, `details`, and full `body`.

```js
const baseUrl = "http://localhost:8080/api/v1";
const token = "<platform-jwt>";
const buildingId = "swfc";
const userParams = {
  haveStaffCard: false,
  haveManagementCard: false,
  haveRoomKey: false,
  id: 0,
  aggregatedWeight: 0,
};

async function post(path, body) {
  const response = await fetch(`${baseUrl}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });
  const data = await response.json();
  if (!response.ok) {
    throw Object.assign(new Error(data.error?.message ?? response.statusText), {
      status: response.status,
      code: data.error?.code,
      details: data.error?.details,
      body: data,
    });
  }
  return data;
}

function query(operation, options = {}) {
  return post(`/buildings/${encodeURIComponent(buildingId)}/${operation}`, {
    userParams,
    ...options,
  });
}

const maps = await query("maps/query");
const allNodes = await query("nodes/query");
const cafe = await query("nodes/query", {
  selection: { nodeIds: ["LowerLobby::cafe"] },
  inDetail: true,
});
console.log(cafe.nodes[0].attributes.shop_name); // Starbucks Coffee

const edges = await query("edges/query", {
  selection: { fromNodeId: "LowerLobby::gate_1" },
});
const nearby = await query("nodes/neighbors", {
  selection: { nodeId: "LowerLobby::gate_1" },
  limit: 10,
  inDetail: true,
});
const elevators = await query("transports/query", {
  selection: { types: ["Elevator"] },
  inDetail: true,
});
```

Neighbors are directly outgoing walking destinations, not geometric proximity or multi-step routes. A short source ID resolves automatically only when unique across maps; otherwise the endpoint returns `AMBIGUOUS_NODE_ID` with qualified candidates. A missing source returns `NODE_NOT_FOUND`.

The neighbor response includes `resolvedNodeId`, `total` (matching distinct destinations before the limit), `limit`, and `truncated`. For each destination, the lowest-cost parallel edge is selected **before** filtering. Results sort by cost, then qualified ID. Use `edges/query` when you need to filter every parallel alternative.

## Filter by tags and custom metadata

Add a structured `filter` to any topology query. It combines with selection using AND and evaluates full metadata before detail projection and limits. You can filter attributes while returning summary records.

Using the helper above, find SWFC cafes:

```js
const cafes = await query("nodes/query", {
  filter: { field: "attributes.tags", op: "contains", value: "cafe" },
});
```

For a project's custom facility and capacity metadata, combine conditions:

```json
{
  "and": [
    {"field":"attributes.capacity","op":"isNotNull"},
    {"field":"attributes.tags","op":"contains","value":"facility"}
  ]
}
```

This is a filter object, not a complete request; put it under `filter` alongside that project's `userParams`. `capacity` and `facility` are illustrative metadata, not required SWFC fields.

| Form / operator | Meaning |
|---|---|
| `{"and":[...]}`, `{"or":[...]}` | Combine a nonempty list of predicates |
| `{"not":{...}}` | Negate one predicate |
| `eq`, `ne` | Compare a present field to `value` |
| `lt`, `lte`, `gt`, `gte` | Compare a numeric field to a numeric `value` |
| `in` | Field equals one member of a list-valued `value` |
| `contains` | List-valued field contains an element equal to `value` |
| `exists`, `isNull`, `isNotNull` | Presence/null checks; omit `value` |

Leaf predicates use `{"field":"path","op":"operator","value":...}`; omit `value` only for the three presence/null checks. Each predicate has exactly one form. Expression strings such as `capacity != null` are not accepted.

Field paths start at the result record. Use `attributes.tags` for nodes, `tags` for edges, `nodeAttributes.tags` for a neighbor's node tags, and `params.capacity` for transport parameters. For a literal key containing a dot, use segments: `"field": ["attributes", "vendor.capacity"]`. Paths traverse records, not array elements. Unknown public top-level fields are invalid; absent custom metadata keys are allowed.

Strings match exactly and case-sensitively; `contains` is list membership, not substring matching. Numbers compare numerically (`1` equals `1.0`); other JSON types are not coerced. All leaf predicates return false for a missing field, including `ne`. Use `not` around `exists` to select absent fields. Negating `contains` also includes records where the field is missing.

Invalid predicates fail even if the selected collection is empty. `filter: null`, an empty predicate, and unsupported operators return `INVALID_FILTER`. Default bounds are 16 nesting levels/path segments, 256 predicates, 1,024 characters per field path, 16,384 UTF-8 bytes per literal, and 256 `in` values; exceeding a bound returns `FILTER_TOO_COMPLEX`. Deployments can adjust these limits.

Filtering changes discovery results only. It does not close nodes, remove routes, or enforce access policies. Use `userParams` for declared compilation policies and route preferences for routing bans.

## Validate source and discover constraints

`POST /authoring/validate` accepts `files`, an object mapping project filenames to source strings, and optional `userParams`. Include `configuration` or `configuration.tcfg`, all referenced map/transport files, and `root` when the project has one. See the [project file reference](topo-script-reference/building/building-includes.html) for source layout.

Example request body for a small project:

```json
{
  "files": {
    "configuration": "building-includes {\n submap Lobby\n}\n",
    "root": "root Example(allowed: Bool) {\n constraint Access { require allowed }\n}\n",
    "Lobby": "topo-map Lobby() {\n let params = {area = 1}\n topo-node entrance\n}\n"
  },
  "userParams": {"allowed": true}
}
```

After source inspection, the response includes:

| Field | Meaning |
|---|---|
| `valid` | Whether the project compiled successfully |
| `constraints.entries` | Declarations with `name`, `scope: {kind, name}`, `source: {file, line, column}`, and `evaluation: "notEvaluated"` |
| `constraints.complete` | Whether parsing produced a complete declaration catalogue |
| `parameters` | Root input declarations with `name`, `type`, and `source` |
| `diagnostics` | Diagnostic `stage`, `message`, and source location where available |

Constraint scope kinds are `root`, `map`, and `transport`. Locations are one-based. The catalogue lists declarations, including constraints that evaluate to false; it does not report their truth values. Unfinished source can return partial entries with `complete: false`.

Successful validation returns HTTP 200 and `status: "success"`. Missing/invalid invocation parameters return 400; parsing or compilation failures return 422. These failures retain the catalogue and diagnostics alongside the error envelope, so read the response body even on failure. Malformed requests and source-size rejection can occur before inspection and lack a catalogue.

Default limits are 512 files, 4 MiB of UTF-8 source, and two concurrent validations. Saturation returns 503 `AUTHORING_BUSY`. This endpoint validates submitted files without publishing them or changing installed buildings. It requires login unless guest validation is explicitly enabled.

## Handle errors

API errors use this envelope; `details` varies by code:

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

| HTTP status | Common codes | Application handling |
|---|---|---|
| 400 | `INVALID_REQUEST`, `INVALID_USER_PARAMS`, `INVALID_FILTER`, `FILTER_TOO_COMPLEX`, `CONFLICTING_NODE_SELECTION` | Correct input; parameter errors include `missing` and `invalid`, filter errors include a JSON Pointer `path` |
| 401 | `AUTHENTICATION_REQUIRED` | Sign in or renew the platform token |
| 403 | `ACCESS_DENIED` | Access is not permitted for this caller |
| 404 | `BUILDING_NOT_FOUND`, `MAP_NOT_FOUND`, `NODE_NOT_FOUND` | Refresh discovery or correct the required resource ID |
| 422 | `NO_ROUTE_FOUND`, `AMBIGUOUS_NODE_ID`, `UNSUPPORTED_TRAVERSAL_PREFERENCE`, `INVALID_TOPOSCRIPT` | Handle the specific outcome; offer candidate IDs for ambiguity or display authoring diagnostics |
| 503 | `AUTHORING_BUSY`, `BUILDING_CATALOG_UNAVAILABLE`, `BUILDING_UNAVAILABLE`, `TRAVERSAL_PREFERENCE_SEARCH_LIMIT` | Capacity, resources, or a bounded search prevented completion; repeated identical searches may hit the same bound |
| 500 | `INVALID_BUILDING`, `UNSERIALIZABLE_TRANSPORT_PARAM`, `UNSERIALIZABLE_METADATA`, `INTERNAL_ERROR` | Report the code to the deployment operator |

`NO_ROUTE_FOUND` does not by itself prove a credential or access-parameter problem. Empty collection results are successful queries; missing required route/neighbor nodes are errors. Network, proxy, or unsupported HTTP method/content-type errors may occur outside this envelope, so applications should also handle non-JSON responses.

## Browser access and deployment compatibility

Cross-origin browser requests require deployment configuration. CORS is disabled by default; enabling it requires explicit allowed origins. CORS does not replace authentication. Same-origin requests and server-to-server calls do not require cross-origin browser permission.

| Environment setting | Default / meaning |
|---|---|
| `TOPONAVI_ALLOW_CORS` | `false`; enable API CORS with `true` |
| `TOPONAVI_CORS_ALLOWED_ORIGINS` | Comma-separated explicit HTTP(S) origins; required when CORS is enabled |
| `TOPONAVI_CORS_ALLOW_CREDENTIALS` | `false`; controls credentialed cross-origin browser requests |
| `TOPONAVI_ANONYMOUS_BUILDINGS` | Empty; comma-separated canonical demo building IDs permitted anonymously |
| `TOPONAVI_ALLOW_GUEST_AUTHORING` | `false`; opt in to anonymous source validation |

Legacy `/quick-demo-*`, `/available-buildings`, `/validate`, and `/test-constraints` handlers are removed. Use the paths and JSON bodies above; there are no compatibility aliases. Coordinate backend and client upgrades. Map storage, map publication, cache administration, and readiness checks are outside this API guide.

For detailed selection and predicate semantics, see the [endpoint contract](endpoint-design.md). For map tags and routing behavior, see the [tagging guide](manuals/tagging_guide.md) and [routing reference](topo-script-reference/routing/routing-semantics.html).
