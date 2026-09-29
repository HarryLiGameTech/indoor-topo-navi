# Transport benchmark testers

This directory contains opt-in performance testers for contributors. They are separate from unit and regression tests: `test`, `check`, and normal application packaging do not run or include them. The Gradle `benchmark` source set compiles them only when requested. They need no database, web server, external datasets, or additional libraries.

The testers generate deterministic synthetic TopoScript projects and exercise the real compiler and navigation API. Compilation uses a fresh compiler each time. Navigation reuses a compiled project and measures both low-rise and high-rise `MinimizeTime` routing.

## Run

From the repository root, using the same JDK as the normal build:

```sh
# Quick fixture, report, and correctness verification.
./gradlew :toponavi-dsl:transportBenchmark --args='--profile smoke'

# Standard measurements across all ten scenarios.
./gradlew :toponavi-dsl:transportBenchmark

# Longer measurements of selected scenarios, with a larger heap.
./gradlew :toponavi-dsl:transportBenchmark -PbenchmarkHeap=4g \
  --args='--profile stress --scenarios dense-large,zoned-transfers --warmups 3 --iterations 10 --queries 24'
```

The task starts a separate JVM with a 2 GiB maximum heap by default. It runs sequentially. Avoid concurrent builds or other heavy work while collecting comparable results. Each invocation creates a timestamped directory under `toponavi-dsl/build/reports/benchmarks/transport/` and prints its absolute path. `--output DIRECTORY` selects a different directory; existing report files are never overwritten.

| Option | Default | Meaning |
|---|---|---|
| `--profile` | `standard` | `smoke`, `standard`, or `stress`; changes fixture sizes |
| `--warmups` | 2; smoke 1 | Unmeasured compilations and full query rounds per routing mode |
| `--iterations` | 5; smoke 1 | Measured compilations and query rounds per routing mode |
| `--queries` | 12; smoke 4 | Queries per round; minimum 4 |
| `--scenarios` | All | Comma-separated scenario names from the table below |
| `--output` | Timestamped build directory | Destination for reports |

The first four queries include bottom-to-top, top-to-bottom, and intermediate-floor journeys. Remaining queries use seed `611`. Identical options generate identical input projects and query sequences.

## Scenarios

| Scenario | Standard workload | Purpose |
|---|---|---|
| `dense-small` | 8 floors, 2 elevator banks | Small reference point |
| `dense-medium` | 16 floors, 5 elevator banks | More stations and transfer choices |
| `dense-large` | 32 floors, 10 elevator banks | Dense ride and transfer graph |
| `zoned-transfers` | 33 floors, 4 service zones, 3 banks per zone | Mandatory transfers through shared boundary floors |
| `mixed-transports` | 16 floors, 5 banks, 1 staircase, 15 escalators | Competing transport types and multi-leg routes |
| `access-granted` | 16 floors, 5 banks, 3 staff-restricted banks | Authorized profile with all banks available |
| `access-partially-denied` | Same source as `access-granted`, staff access false | Reduced connectivity from compile-time access control |
| `partial-outage` | 16 floors, 5 banks, 3 out of order | Alternatives with some banks unavailable |
| `all-access-denied` | 8 floors, 3 restricted banks, staff access false | Expected `NO_ROUTE_FOUND` |
| `all-out-of-order` | 8 floors, 3 out-of-order banks | Expected `NO_ROUTE_FOUND` |

Each floor has a hub and distinct transport landings, connected by directed walking edges in both directions. Transfer walking costs vary by landing. Outages use existing station `out-of-order` declarations during compilation; these testers do not implement runtime exclusions or the proposed exclusion CLI.

Smoke reduces the largest dense case to 8 floors and 3 banks, and the zoned case to 9 floors. Stress increases those cases to 64 floors and 16 banks, and 65 floors across 8 zones. Stress can be expensive; select individual scenarios when investigating growth.

## Reports and correctness

- `summary.csv`: one row per scenario and phase (`compile`, `route-low-rise`, `route-high-rise`), with sample count, nearest-rank p50/p95/max milliseconds, graph sizes, unavailable-line counts, heap observations, GC count/time, no-route count, and maximum route-cost gap from the reference optimum.
- `samples.csv`: every measured compilation or query, including endpoints, result, elapsed time, route cost, and reference cost. Warmups are excluded.
- `environment.properties`: timestamp, JVM/OS/architecture, processor count, JVM arguments, maximum heap, profile, iteration settings, and query seed.
- `SUCCESS`: written only after every scenario completes and all correctness checks pass. A failed run may leave partial CSVs without this marker; failures exit with a nonzero status.

`TransportReference.scala` independently runs Dijkstra over a flattened physical graph containing walking edges and compiled ride edges. It verifies reachability, endpoints, continuity, permitted edges, and costs. Low-rise results must equal the reference optimum. High-rise results must be valid; their optimality gap is reported because that planner is heuristic. The reference also checks expected station/floor counts and rejects ride edges on unavailable lines. Both no-route fixtures must be unreachable in the reference and return `NO_ROUTE_FOUND` through the API.

Timing excludes source generation, reference search, correctness assertions, and CSV writes. It includes compilation itself or one public `findRoutePlan` call, including its automatic no-route retry. Compiler and routing diagnostic output is suppressed. Warmed measurements use one JVM and a fixed scenario order; they are application-level benchmarks, not isolated JVM microbenchmarks or cold-start measurements.

Heap values are JVM heap observations, not process RSS or exact retained graph sizes. `heap_pool_peak_sum_bytes` sums each heap pool's peak usage after resetting peak counters at the phase boundary; those peaks need not occur simultaneously. Heap/GC observations cover the whole phase, including assertions and report writing between timed calls. No forced GC is performed. Compare repeated runs with the same JVM, heap, profile, warmups, and machine conditions. Small sample sets, especially smoke, are correctness checks rather than reliable tail-latency estimates.

There are no wall-clock pass/fail thresholds. The results establish measured behavior for these fixtures, not a general capacity guarantee. The reference shares compiled edge costs, so it does not independently validate the elevator timing model or resolve the documented stair turn-continuity limitation.

## Extend

Add fixtures in `scala/benchmarks/TransportScenarios.scala`, keeping identifiers and queries deterministic. Put new performance testers in this source set rather than `src/test`. Use explicit expected reachability and correctness checks so a faster but invalid route cannot pass unnoticed. Update this scenario table when adding workloads. Keep generated reports under `build/`; when sharing results, include all three report files, the source revision, and any uncommitted changes.
