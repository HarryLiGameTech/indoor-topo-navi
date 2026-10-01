# Manual cache and route testers

These developer utilities are separate from automated regression tests and transport benchmarks. The Gradle `developer` source set is compiled only when requested; `test`, `check`, and application packaging do not run or include it.

## Generate a cache

Run from the repository root. Pass the project's Bool and Int root parameters explicitly; the generator does not grant access by default.

```sh
./gradlew :toponavi-dsl:generateTesterCache --args='--help'

./gradlew :toponavi-dsl:generateTesterCache \
  --args='--project examples/swfc --param haveStaffCard=true --param haveManagementCard=true --param haveRoomKey=true --param id=1919810 --param aggregatedWeight=10'

./gradlew :toponavi-dsl:generateTesterCache \
  --args='--project examples/trent --param haveStaffCard=true --param timeOfDay=36000'
```

The example parameter values describe a test user. `timeOfDay=36000` means 10:00. Choose values appropriate to the route being investigated. Repeated parameters and malformed values are rejected.

`--project DIRECTORY` also accepts a separate corrected copy of a project. The shipped SWFC and Trent maps currently contain duplicate node declarations (`HotelBar::triv_2` and `floor4::room_467`), and Trent's root references `timeOfDat` instead of `timeOfDay`. These data errors must be corrected before generating their caches; the generator applies normal compiler validation and does not bypass them.

By default, the serialized `CompilationResult` is written to `~/.toponavi/tester_<directory-name>`, preserving the manual testers' existing cache paths. Use `--output FILE` to select another path. The output is replaced only after compilation and serialization succeed. New files are writable; the old read-only permission assertion is no longer part of the workflow. A failed compilation exits unsuccessfully and leaves the previous cache untouched.

## Inspect routes

```sh
./gradlew :toponavi-dsl:swfcRoutePlanningTester
./gradlew :toponavi-dsl:trentRoutePlanningTester

# Read a cache generated with --output instead of the default path.
./gradlew :toponavi-dsl:swfcRoutePlanningTester --args='/absolute/path/to/tester_swfc'
```

These programs print routes and diagnostics for their hard-coded example endpoints. Regenerate caches after changing compiler or graph classes; Java serialization is not a stable interchange format across code revisions. The web service's `CompilationCacheService` uses a separate cache and is not exercised by these utilities.

Automated serialization coverage lives in `CompilationSerializationTest`. It compiles a small parameterized project, writes and reads an in-memory object stream, verifies restored graph references, and checks walking and transport routes under both planning modes. It does not depend on a user's home directory or the example datasets.
