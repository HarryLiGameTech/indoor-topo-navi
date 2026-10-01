# TopoNavi

**Developer Preview** — an indoor routing engine built around TopoScript. Describe floor topology, access rules, and transports in text files, compile them into navigation graphs, and request routes through the Scala API or REST service.

## Modules

| Module | Responsibility |
|---|---|
| `toponavi-core` | Graphs, transport models, and route planning |
| `toponavi-dsl` | TopoScript parser, compiler, and navigation API |
| `toponavi-web` | Spring Boot REST service and map management |

## Build and test

Use JDK 20 (tested with 20.0.2) and the included Gradle 8.4 wrapper. Run commands from the repository root; Scala and other build dependencies are managed by Gradle.

```sh
./gradlew assemble
./gradlew :toponavi-dsl:test --tests CompilationValidationTest --tests RootConstraintTest --tests CompilationIsolationTest
```

Run the full test suite with:

```sh
./gradlew test --continue
```

The full suite currently has known failures in mixed arithmetic and example data; see the [validation findings](docs/arithmetic-validation-findings.md).

Run the opt-in [transport benchmark testers](toponavi-dsl/src/benchmark/README.md) with `./gradlew :toponavi-dsl:transportBenchmark`. They report compilation and routing performance separately from the test suite; use `--args='--profile smoke'` for a quick correctness check.

The [manual cache and route testers](toponavi-dsl/src/developer/README.md) have separate developer tasks. Run `./gradlew :toponavi-dsl:generateTesterCache --args='--help'` for cache generation options and supply root parameters explicitly.

The web service additionally requires database and authentication configuration. See [application.yml](toponavi-web/src/main/resources/application.yml) and [.env.example](.env.example) for its settings.

## Example maps

The [examples](examples/) directory contains four datasets. Their root declarations specify these compilation inputs:

| Dataset | Inputs |
|---|---|
| `indigoBJ` | None |
| `trent` | `haveStaffCard: Bool`, `timeOfDay: Int` |
| `swfc` | `haveStaffCard: Bool`, `haveManagementCard: Bool`, `haveRoomKey: Bool`, `id: Int`, `aggregatedWeight: Int` |
| `nbc4` | `haveStaffCard: Bool`, `haveManagementCard: Bool`, `id: Int`, `aggregatedWeight: Int` |

Supply inputs through the compiler parameter map or the API's `userParams` field. These datasets include legacy syntax and validation issues; their presence does not guarantee successful compilation. Use the synthetic projects in the compiler tests for reproducible examples.

`GET /api/v1/available-buildings` lists installed map projects under the configured `EXAMPLES_PATH`: immediate subdirectories containing a readable `configuration.tcfg` or `configuration`. The response is `{"status":"success","buildings":["indigoBJ","nbc4","swfc","trent"]}` for the shipped examples. This inventory does not compile the maps or validate a user's access parameters. An empty directory returns an empty list; an unreadable inventory returns HTTP 503. MCP reads this endpoint at startup, so restart MCP after changing installed projects.

## Documentation and limitations

- [TopoScript reference](docs/topo-script-reference/index.html)
- [Project structure and source files](docs/topo-script-reference/building/building-includes.html)
- [Root parameters, functions, and constraints](docs/topo-script-reference/building/global-declarations.html)
- [Routing semantics](docs/topo-script-reference/routing/routing-semantics.html)

High-rise routing is heuristic. Graphs are compiled in memory, and resource requirements depend on map size and transport connectivity; no general capacity guarantee is established. APIs and language behavior are evolving, and accessibility depends on the supplied map data and policies.

## Contributing

Keep changes focused, follow the surrounding code style, and add regression tests for behavior changes. Run the relevant tests and report any remaining full-suite failures. After editing the language reference, rebuild its search index with `node docs/topo-script-reference/tools/build-search-index.mjs`.

## License

[Apache License 2.0](LICENSE).
