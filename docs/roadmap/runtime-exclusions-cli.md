# Runtime exclusion CLI proposal

Status: Planned; not implemented.

The next step for temporary outages is a navigation CLI that accepts a request-scoped exclusion list, conceptually `--exclude '[n1, n2, n3, e12, e23]'`. This records unavailable topology for one route request without modifying the TopoScript project or its compiled graph.

## Proposed contract

- Nodes use qualified map-instance and node identifiers so repeated local names are unambiguous.
- Edges use directed, qualified endpoints. An endpoint-pair selector excludes every parallel edge matching that direction; excluding both directions requires both selectors.
- A transport selector can disable an entire elevator bank without enumerating all of its ride edges.
- Unknown or ambiguous selectors fail clearly rather than being silently ignored. An excluded route endpoint is a request error.
- Exclusions can only remove permitted traversals; they cannot override compile-time access control.
- All routing modes apply the exclusions during search, including walking connections, rides, and transfers. A valid request with no remaining connection returns the existing structured no-route result.
- The exclusion overlay belongs to one invocation and leaves the cached compilation reusable. Omitting an exclusion on the next invocation restores that traversal if compile-time rules permit it. Persistent outages, scheduled expiry, and a live status feed are outside this initial CLI scope.

A possible shell-safe argument format is:

```text
--exclude '["node:Floor1::n1", "edge:Floor1::n2->Floor1::n3", "transport:MainLift"]'
```

This is proposed syntax, not an available command. Short edge names such as `e12` need a stable edge-ID or alias mapping before they can be accepted; atomic paths currently identify their endpoints rather than declaring those names.

## Future verification

Cover individual nodes and directed edges, parallel edges, complete transport exclusion, alternative transport selection, excluded endpoints, ambiguous identifiers, and both routing modes. Verify that access restrictions remain effective and that one invocation's exclusions do not alter another invocation or the stored compilation.

The current [transport benchmark testers](../../toponavi-dsl/src/benchmark/README.md) use compile-time `requires` and `out-of-order` fixtures. They provide baseline restricted-connectivity and no-route workloads without implementing this proposal.
