# Arithmetic validation findings

Updated 2026-09-28 after the approved division implementation. Division is implemented in the syntax visitor, `OpKind`, `Term.infer`, `TypeChecker`, and the interpreter. Mixed addition, subtraction, and multiplication remain deferred.

The acceptance suite is `toponavi-dsl/src/test/scala/ArithmeticCompilationTest.scala`. It checks division and root functions through both in-memory and directory compilation. `DivisionTest.scala` separately exercises parsing, type inference, type checking, evaluation, and arithmetic errors.

Run it with:

```sh
./gradlew :toponavi-dsl:test --tests ArithmeticCompilationTest
./gradlew :toponavi-dsl:test --tests DivisionTest
```

The arithmetic suite intentionally retains six failing acceptance tests for mixed addition, subtraction, and multiplication. Those operations were excluded from the approved division scope. The dedicated division suite passes.

## Observed results

| Cases | Result | Failure |
|---|---|---|
| Same-type `Int` and `Float` addition, subtraction, multiplication | 6 pass | — |
| Division with `Int/Int`, `Float/Float`, `Int/Float`, `Float/Int`, both compiler entry points | 8 pass | — |
| Mixed addition, subtraction, multiplication, both operand orders | 6 fail | `Interpreter: Binary operation on incompatible types` |
| Documented `timeWhenRushing` with 150, 240, 300, and 450 seconds/km, both compiler entry points | 8 pass | — |
| Zero-denominator and negative-path-cost validation, both compiler entry points | 4 pass | — |

Arithmetic compilation: **32 tests, 26 passed, 6 failed**. Dedicated division: **46 tests, all passed**.

## Implemented division contract

The existing `/` grammar now maps to `OpKind.Div`. Every numeric operand combination returns `FloatType` in both `Term.infer` and `TypeChecker`; evaluation returns `FloatVal` after converting integer operands to `Double`.

- Division preserves fractional ratios, including for two integer operands: `3 / 2 = 1.5`.
- Division by `0`, `0.0`, or `-0.0` throws `ArithmeticException("Division by zero")`.
- Nonfinite operands and overflow to a nonfinite result throw clear arithmetic errors.
- Nonnumeric operands are rejected by inference, type checking, and evaluation.
- Division shares multiplication's precedence and associates to the left.
- Floating-point rounding applies; converting integers beyond 2^53 can lose precision.

## Mixed addition, subtraction, and multiplication have no numeric promotion

Mixed expressions such as `5 + 2.5` parse successfully. In the tested path-data route, they reach `Interpreter.evalTramp`, whose addition, subtraction, and multiplication cases support `IntVal`/`IntVal` and `FloatVal`/`FloatVal` but not mixed pairs. The fallback throws the incompatible-types exception.

Separately, `Term.infer` in `corelang/Syntax.scala` and `checkBinOp` in `corelang/TypeChecker.scala` also lack mixed rules for these three operators. Those are additional implementation gaps identified by inspection; they are not the immediate exception source for these path-cost tests. A future change must align numeric promotion and result types across these layers, including both operand orders.

## The documented calculation needs fractional division

The suite uses the function published in `docs/topo-script-reference/building/global-declarations.html`:

```text
def timeWhenRushing(stdCost: Float, timeToRun1KmInSeconds: Int): Float = {
  stdCost * (timeToRun1KmInSeconds / 300)
}
```

For a standard cost of `10.0`, the verified proportional results are:

| Seconds/km | Ratio | Expected cost |
|---|---|---|
| 150 | 0.5 | 5.0 |
| 240 | 0.8 | 8.0 |
| 300 | 1.0 | 10.0 |
| 450 | 1.5 | 15.0 |

All four cases pass through both compiler entry points. Because division returns a `Float`, the outer multiplication is `Float * Float` and needs no mixed-multiplication support.

## Full-suite verification, 2026-09-30

`./gradlew test --continue` produced these results:

- Core: 42 tests passed.
- Web: 21 tests passed.
- DSL: 403 tests, 8 failures. All 55 `TypeCheckerTest` cases, six `FixpointTest` cases, two `CompilationSerializationTest` cases, and 29 `InterpreterTest` cases passed.

Service compilation, validation, and on-demand navigation now use a fresh compiler per request. The isolation test exercises 96 concurrent calls across those entry points and their parameter overloads. Root definitions are evaluated in dependency order before root constraints, with compilation tests covering map paths and transport permissions.

Six DSL failures are the deferred mixed-arithmetic acceptance tests above.

The two former `TesterCacheGenerator` tests are now an explicit [developer utility](../toponavi-dsl/src/developer/README.md), alongside the manual route testers that consume its output. Cache generation accepts explicit root parameters and is excluded from the regular test suite. `CompilationSerializationTest` replaces their regression coverage with a controlled project and an in-memory serialization round trip, verifying graph references, access-dependent walking routes, directed transport edges, and both route-planning modes.

The fixpoint failure was a type-checker bug, not a stale expectation. For `fix f: T. body`, the checker now binds `f: T` and checks that `body` has type `T`; it previously demanded `T -> T`. The original factorial test remains unchanged. New tests cover parsed `fix` and `let rec`, captured variables, non-function results, and rejection of the extra function layer previously accepted by the checker.

The remaining two failures are invalid example data now caught by the duplicate-node check:

| Test | Source | Duplicate declarations |
|---|---|---|
| `SwfcTransportGraphGroundTruthTest` | `examples/swfc/HotelBar` | `triv_2` at lines 14 and 37, both unannotated |
| `CompilerTest` low-rise project | `examples/trent/Floor4` | `room_467` at lines 43 and 49; the first has `description = "CPC secretary office"`, while the second is unannotated |

These tests now fail during map parsing, before comparing transport data or constructing the route graphs. The example declarations and ground-truth fixture were left unchanged. Further duplicates may surface after these first errors are corrected; this run is not an exhaustive audit of all example maps.
