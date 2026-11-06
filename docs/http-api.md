# HTTP API guide

Use the TopoNavi HTTP API to discover a building's topology, inspect transport services, calculate routes, validate TopoScript projects, and store native TopoScript project sources. All endpoints in this guide are relative to `/api/v1`.

## Connect and make a first request

Use the service URL supplied by your deployment and its configured service credential (`TOPONAVI_SERVICE_TOKEN` on the server). Send it as `Authorization: Bearer <token>`. There are no user accounts, sign-in flow, or token issuance; platform JWTs and GitHub sign-in are no longer accepted. An absent or wrong credential authenticates nothing: protected operations return 401, while operations a deployment has made public still run anonymously.

The following shell examples use a local service and the shipped SWFC project. Replace the URL and token before running them.

```sh
export TOPONAVI_URL='http://localhost:8080'
export TOPONAVI_TOKEN='<service-token>'

curl -sS "$TOPONAVI_URL/api/v1/buildings" \
  -H "Authorization: Bearer $TOPONAVI_TOKEN"
```

The response contains exact, case-sensitive building IDs:

```json
{"status":"success","buildings":["indigoBJ","nbc4","swfc","trent"]}
```

The list depends on the deployment and your access. A listed building is a stored project that has a root `configuration` or `configuration.tcfg` file; listing does not compile it or guarantee that a route exists. Stored projects without a configuration file are omitted here and answer `BUILDING_NOT_FOUND`, but remain available through the [project API](#store-and-manage-projects).

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

Authentication is required by default for building operations and authoring validation. A deployment can allow anonymous access to selected demo buildings and independently allow guest authoring. In that case, omit the authorization header. Project storage operations always require the service credential, even for an anonymously computable building. Only `GET /health` is always public.

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
| POST, GET, PUT, DELETE | `/projects`, `/projects/{id}`, `/projects/by-building-id/{buildingId}` | Stored project sources; see [Store and manage projects](#store-and-manage-projects) |

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

`nodes/query` also returns a top-level `graphSequence: string[]` in both detail
modes. It is the complete `configuration` submap declaration order, which map
contributors must write from highest to lowest, including basement levels.
For `submap Floor7 using Zone1`, the sequence contains the instance `Floor7`;
the template `Zone1` appears only if it is also explicitly declared as a submap.
The sequence is independent of `selection` and `filter`, including when `nodes`
is empty. Node records keep their existing ID sort order and may include template
graphs; use `graphSequence` to identify and order declared instances.

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
const token = "<service-token>";
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

Default limits are 512 files, 4 MiB of UTF-8 source, and two concurrent validations. Saturation returns 503 `AUTHORING_BUSY`. This endpoint validates submitted files without storing them or changing stored projects, and it does not read project storage. It requires the service credential unless guest validation is explicitly enabled. Its limits are separate settings from the project-store limits; if a deployment configures them differently, a stored project can exceed what this endpoint accepts.

## Store and manage projects

A stored project is a set of native TopoScript source files plus metadata. It is the source of every building served by `/buildings`. Saving never compiles: incomplete and non-compiling sources can be created, read, and replaced. Use `/authoring/validate` to check sources, and `/buildings/{buildingId}/...` to compute with stored ones.

All project routes require the service credential. They are additive; the building and computation routes above are unchanged and keep using `buildingId`.

| Method | Path | Success | Behavior |
|---|---|---|---|
| POST | `/projects` | 201 | Create metadata and the submitted source set; returns a summary and a `Location` header |
| GET | `/projects?page=0&size=50` | 200 | Summaries sorted by exact `buildingId`, including projects without a configuration file; no source contents |
| GET | `/projects/{id}` | 200 | Metadata plus the complete current source set |
| GET | `/projects/by-building-id/{buildingId}` | 200 | Exact, case-sensitive lookup returning the same detail representation |
| PUT | `/projects/{id}` | 200 | Atomically replace `name` and the entire `files` object; returns a summary |
| DELETE | `/projects/{id}` | 204 | Delete metadata and all source files |

Identifiers:

- `id` is a server-generated UUID used by the project routes.
- `buildingId` is required on creation, never changes, and is the ID used by `/buildings`. It is 1–255 ASCII letters, digits, `.`, `_`, or `-`, starting with a letter or digit. Lookup is exact: `SWFC` does not find `swfc`. IDs that differ only by case cannot coexist; such a create returns 409 `BUILDING_ID_CONFLICT`. Deleting a project frees its ID; reusing it creates a new UUID.
- `name` is a nonblank display label of at most 255 characters.

Create:

```sh
curl -sS "$TOPONAVI_URL/api/v1/projects" \
  -H "Authorization: Bearer $TOPONAVI_TOKEN" \
  -H 'Content-Type: application/json' \
  --data-binary '{
    "buildingId": "LobbyDemo",
    "name": "Lobby demonstration",
    "files": {
      "configuration": "building-includes {\n submap Lobby\n}\n",
      "Lobby.tmap": "topo-map Lobby() {\n let params = {area = 1}\n topo-node entrance\n}\n"
    }
  }'
```

```json
{"status":"success","project":{"id":"2c1f…","buildingId":"LobbyDemo","name":"Lobby demonstration","createdAt":"2026-11-06T21:50:48.123456Z","updatedAt":"2026-11-06T21:50:48.123456Z"}}
```

Detail responses add `files`, an object from relative path to source string. List responses are `{"status":"success","projects":[...],"page":0,"size":50,"total":4}`; `page` is a nonnegative integer, `size` is 1–200 (default 50), and other or repeated query parameters are rejected. A page is not a snapshot across requests.

Replace requires both `name` and `files` and must not contain `buildingId`. Paths omitted from `files` are deleted, and `files: {}` clears the project. There is no PATCH, per-file update, or merge. Concurrent replacements of one project are applied one at a time and the last committed replacement wins; there is no version token or conflict detection. Do not blindly retry a failed create: look up the `buildingId` first, because the first attempt may have committed.

Storage rules, checked before anything is saved:

| Item | Rule |
|---|---|
| Envelope | JSON object with exactly the documented fields; no nulls; duplicate object keys are rejected (case-sensitive) |
| Paths | Relative POSIX paths of 1–512 UTF-8 bytes; no leading/trailing `/`, no backslash (`\`), no empty, `.` or `..` segments, control characters, or drive/URI prefix. Paths are never normalized: `floor/../Lobby.tmap` is rejected, not rewritten |
| File types | Extensionless, or ending exactly in `.tmap`, `.ttr`, or `.tcfg`. `.json` and other files are not stored |
| Source text | Stored exactly, including line endings, whitespace, and a byte-order mark. NUL characters and invalid Unicode are rejected |
| Limits | 512 files, 4 MiB of UTF-8 source, and a 32 MiB request body by default; exceeding one returns 413 `PROJECT_TOO_LARGE` with the setting name in `details.limit`. Compressed request bodies (`Content-Encoding`) are rejected |

Nested paths are stored as submitted, but the compiler's own file lookup rules still decide what a building uses; `configuration.tcfg` takes precedence over `configuration`.

| Status | Code | Meaning |
|---|---|---|
| 400 | `INVALID_REQUEST` | Bad envelope, path, text, UUID, or pagination; `details.path` is a JSON Pointer such as `/files/Lobby.tmap` |
| 401 | `AUTHENTICATION_REQUIRED` | Missing or wrong service credential |
| 404 | `PROJECT_NOT_FOUND` | No project has this UUID or exact `buildingId` (including a repeated delete) |
| 409 | `BUILDING_ID_CONFLICT` | The `buildingId` is taken, exactly or differing only by case |
| 413 | `PROJECT_TOO_LARGE` | A configured body, file-count, or source-size limit was exceeded |
| 503 | `PROJECT_STORE_UNAVAILABLE` | The database was unreachable or a lock was not obtained in time; the request may be retried |

A building request that obtained its sources before a concurrent replacement or deletion finishes with those sources. A request that starts after the change commits sees the new sources, or `BUILDING_NOT_FOUND` after deletion. Compiled results are cached by exact source content and `userParams`, so a replaced project is never served from an older compilation, and invalid replacement sources report `INVALID_BUILDING` rather than the previous result.

To import a native source directory, use the explicit importer; the service never scans directories:

```sh
TOPONAVI_SERVICE_TOKEN='<service-token>' python3 tools/import_toposcript_project.py \
  --base-url "$TOPONAVI_URL" --directory examples/swfc --building-id swfc \
  --name "Shanghai World Financial Center"
```

See the [deployment guide](synology-container-manager.md#5-import-map-projects) for options and verification.

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
| 401 | `AUTHENTICATION_REQUIRED` | Send the deployment's service credential |
| 403 | `ACCESS_DENIED` | Access is not permitted for this caller |
| 404 | `BUILDING_NOT_FOUND`, `MAP_NOT_FOUND`, `NODE_NOT_FOUND`, `PROJECT_NOT_FOUND` | Refresh discovery or correct the required resource ID |
| 409 | `BUILDING_ID_CONFLICT` | Choose another building ID or replace the existing project |
| 413 | `PROJECT_TOO_LARGE` | Reduce the project or ask the operator about limits |
| 422 | `NO_ROUTE_FOUND`, `AMBIGUOUS_NODE_ID`, `UNSUPPORTED_TRAVERSAL_PREFERENCE`, `INVALID_TOPOSCRIPT` | Handle the specific outcome; offer candidate IDs for ambiguity or display authoring diagnostics |
| 503 | `AUTHORING_BUSY`, `BUILDING_CATALOG_UNAVAILABLE`, `BUILDING_UNAVAILABLE`, `PROJECT_STORE_UNAVAILABLE`, `TRAVERSAL_PREFERENCE_SEARCH_LIMIT` | Capacity, storage, or a bounded search prevented completion; repeated identical searches may hit the same bound |
| 500 | `INVALID_BUILDING`, `UNSERIALIZABLE_TRANSPORT_PARAM`, `UNSERIALIZABLE_METADATA`, `INTERNAL_ERROR` | Report the code to the deployment operator. `INVALID_BUILDING` means the stored sources of a listed building do not currently compile |

`NO_ROUTE_FOUND` does not by itself prove a credential or access-parameter problem. Empty collection results are successful queries; missing required route/neighbor nodes are errors. Network, proxy, or unsupported HTTP method/content-type errors may occur outside this envelope, so applications should also handle non-JSON responses.

## Browser access and deployment compatibility

Cross-origin browser requests require deployment configuration. CORS is disabled by default; enabling it requires explicit allowed origins. CORS does not replace authentication. Same-origin requests and server-to-server calls do not require cross-origin browser permission.

| Environment setting | Default / meaning |
|---|---|
| `TOPONAVI_SERVICE_TOKEN` | Required; the opaque service credential (at least 32 characters). The service does not start without it |
| `TOPONAVI_ALLOW_CORS` | `false`; enable API CORS with `true` |
| `TOPONAVI_CORS_ALLOWED_ORIGINS` | Comma-separated explicit HTTP(S) origins; required when CORS is enabled |
| `TOPONAVI_CORS_ALLOW_CREDENTIALS` | `false`; controls credentialed cross-origin browser requests |
| `platform.api.cors.allowed-methods` | `GET,POST,PUT,DELETE,OPTIONS`; responses expose `Location` to browser clients |
| `TOPONAVI_ANONYMOUS_BUILDINGS` | Empty; comma-separated canonical demo building IDs permitted anonymously |
| `TOPONAVI_ALLOW_GUEST_AUTHORING` | `false`; opt in to anonymous source validation |

Legacy `/quick-demo-*`, `/available-buildings`, `/validate`, and `/test-constraints` handlers are removed. Use the paths and JSON bodies above; there are no compatibility aliases. Coordinate backend and client upgrades. Map publication, cache administration, and readiness checks are outside this API guide.

**Retired platform endpoints.** GitHub sign-in and the GitHub-backed editor platform were removed. `/auth/github`, `/oauth2/**`, `/login/oauth2/**`, `/api/me`, and every `/api/maps/**` route (map CRUD, collaborators, visibility, checkpoints, and history) now return 404 without redirects, and platform JWTs are no longer accepted. Store native TopoScript sources through `/projects` instead; legacy `map.json` editor state is not accepted.

For detailed selection and predicate semantics, see the [endpoint contract](endpoint-design.md). For map tags and routing behavior, see the [tagging guide](manuals/tagging_guide.md) and [routing reference](topo-script-reference/routing/routing-semantics.html).
