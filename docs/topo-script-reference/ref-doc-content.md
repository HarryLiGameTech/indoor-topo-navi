# TopoScript Reference Documentation Content Plan

**Document type:** Progress and task tracker  
**Target:** Modular, text-only HTML reference documentation  
**Content status:** Planning only; this file does not define TopoScript behavior

## 1. Purpose and Scope

This specification tracks what must be written for the formal TopoScript reference. It is an outline of required subjects, writing tasks, dependencies, and completion criteria—not the reference documentation itself.

The finished reference should:

- describe the language as implemented by the engine;
- separate syntax, static semantics, compilation behavior, routing behavior, and output guarantees;
- use stable terminology across all modules;
- be divided into modular HTML articles that can be published independently or assembled into a larger website;
- distinguish implemented behavior from limitations, experimental behavior, and planned behavior;
- derive normative claims from code and tests rather than assumptions or marketing language.

Out of scope for this tracking document:

- tutorial prose;
- actual grammar definitions;
- definitive semantic rules;
- complete TopoScript examples;
- generated API documentation;
- visual styling or website layout.

## 2. Progress Conventions

Use these task states:

- `[ ]` Not started
- `[-]` In progress
- `[x]` Complete and verified
- `[!]` Blocked or behavior requiring an engine decision

A documentation item is complete only when:

- the relevant implementation and tests have been inspected;
- the written rule matches the tagged engine version;
- examples have been compiled or tested;
- known limitations and undefined behavior have been identified;
- terminology and links have been checked against the rest of the reference.

## 3. Planned Document Set

```text
topo-script-reference/
  index.html
  language/
    language-and-execution-model.html
    lexical-structure.html
    types-expressions-and-units.html
  building/
    building-includes.html
    global-declarations.html
  topology/
    topomap.html
    nodes.html
    paths.html
    submaps.html
  transport/
    transport.html
    elevators.html
    escalators-stairs-and-generic-transports.html
  routing/
    routing-semantics.html
    constraints-and-parameters.html
    preferences-and-modes.html
    route-results.html
  validation/
    diagnostics.html
  examples/
    feature-by-feature-tutorials.html
    skyrim-tower.html
  glossary.html
```

The exact file split may be adjusted while writing, but the nine content areas below must remain identifiable and linkable.

## 4. Content Area 1 — Language and Execution Model

**Primary output:** `language/language-and-execution-model.html` and supporting language articles

### Points to write

- [x] Define the intended audience and prerequisite knowledge.
- [x] Explain what category of system TopoScript describes.
- [x] Introduce the major language entities and their relationships.
- [x] Explain the TopoScript lifecycle: source files, compiled building model, per-request route planning, and navigation result.
- [x] Document source-file and project boundaries.
- [x] Document lexical structure.
- [x] Document identifiers and qualified names.
- [x] Document name scopes and visibility at a general level.
- [x] Document primitive and domain-specific types.
- [x] Document literals, expressions, operators, and evaluation rules.
- [x] Identify implementation-defined routing behavior, application-defined annotation semantics, and experimental features.
- [!] State the language/version relationship and compatibility policy. Deferred pending a project-level versioning decision.

### Verification tasks

- [ ] Cross-check terminology against parser grammar, AST types, type checker, compiler, and public DTOs.
- [ ] Identify syntax accepted by the parser but rejected later in compilation.
- [ ] Identify constructs whose current behavior is not covered by tests.
- [ ] Resolve discrepancies between documentation terminology and implementation names.

### Completion criteria

- [ ] Every later article can link to one shared definition of compilation phase, routing phase, scope, type, and value.
- [ ] No general language rule is inferred solely from an example.

## 5. Content Area 2 — Whole-Building Composition (`building-includes`)

**Primary output:** `building/building-includes.html`

### Points to write

- [x] Describe the role of the whole-building entry point.
- [x] List which file or declaration categories may be included.
- [x] Document include syntax and path-resolution rules.
- [x] Document how included maps, transports, and global declarations form one project.
- [x] Document ordering rules and whether source order is significant.
- [x] Document cross-file name resolution.
- [x] Document missing-reference handling.
- [x] Document repeated, recursive, or cyclic include behavior.
- [x] Document project-root and filesystem assumptions.
- [x] Document the relationship between declared content and the compiled building model.
- [x] Identify portability and packaging constraints.

### Verification tasks

- [x] Trace entry-point loading through parser and compiler code.
- [ ] Add tests for missing sources and unsupported alias chains or cyclic reuse.
- [ ] Verify all path examples on supported operating systems.
- [x] Record unresolved source-loading and reusable-base behavior as engine or documentation decisions.

### Completion criteria

- [x] A reader can determine which files constitute a complete TopoScript building project.
- [x] Every current include and cross-file resolution failure has a documented diagnostic category.

## 6. Content Area 3 — Building-Global Declarations

**Primary output:** `building/global-declarations.html`

### Points to write

- [x] Inventory the stable declarations valid at building-global scope.
- [x] Link configuration composition to the Area 2 reference rather than duplicating it.
- [x] State that dedicated building-metadata declarations are not currently supported.
- [x] Document user-supplied compilation inputs.
- [x] Document parameter types, planned defaults, required values, and current validation limitations.
- [x] Document constraint declarations.
- [x] Document where constraints may be referenced and evaluated.
- [x] Document management-domain assignment at a general level and link its detailed composition semantics.
- [x] Introduce the access and ride-policy model without duplicating attachment-specific semantics.
- [!] Decide whether reusable root-level declarations should be limited to `def`; do not document global `let`, tag registries, or constants pending that decision.
- [x] Document naming, uniqueness, and scope rules for each stable declaration.
- [x] Document dependency and evaluation ordering between global declarations.
- [x] Separate compile-time validation from routing-time evaluation.
- [x] Identify global declarations that affect graph construction versus route eligibility.

### Verification tasks

- [x] Build an implementation inventory from grammar, AST, type checker, and compiler passes.
- [x] Confirm which parameters are mandatory for compilation, route requests, or both.
- [ ] Add or identify tests for missing, incorrectly typed, and unused declarations.
- [x] Confirm management-domain and ride-policy behavior against implemented tests.

### Completion criteria

- [x] Each documented stable global declaration covers syntax, scope, type rules, evaluation phase, errors, and examples.
- [x] Cross-cutting declarations link to existing reference and example modules rather than duplicating their rules.

## 7. Content Area 4 — Per-Floor and Per-Area Topology (`topomap`)

**Primary output:** articles under `topology/`

### Points to write

- [ ] Define the responsibilities and boundaries of a topo map.
- [ ] Document topo-map declaration and naming rules.
- [ ] Document nodes and their supported properties.
- [ ] Document paths and their supported properties.
- [ ] Document directed and bidirectional connectivity.
- [ ] Document path cost, time, distance, or other weight fields.
- [ ] Document coordinates and spatial metadata.
- [ ] Document tags on maps, nodes, and paths.
- [ ] Document required actions.
- [ ] Document spatial hints.
- [ ] Document turn hints.
- [ ] Document uncertain-access markers and their placement.
- [ ] Document conditional topology and constraint attachment points.
- [ ] Document submap declarations.
- [ ] Document reusable-submap instantiation.
- [ ] Document name qualification for instantiated content.
- [ ] Document connections between parent maps and submaps.
- [ ] Document disconnected, duplicate, and otherwise invalid topology.
- [ ] State which concepts represent intra-map movement.

### Verification tasks

- [ ] Inventory all map, node, path, and submap fields from the AST and compiler model.
- [ ] Trace each source property to its compiled representation and route output, if any.
- [ ] Verify directionality and cost behavior with focused route tests.
- [ ] Verify reusable-submap naming and collision behavior.
- [ ] Identify map properties currently parsed but unused by routing.

### Completion criteria

- [ ] Every topology construct has planned syntax, static-semantics, runtime-semantics, diagnostic, and example coverage.
- [ ] The reference clearly separates topology connectivity from cross-map transportation.

## 8. Content Area 5 — Cross-Map Transportation (`transport`)

**Primary output:** articles under `transport/`

### Points to write

- [ ] Define transport as connectivity between maps, floors, or areas.
- [ ] Inventory all supported transport forms.
- [ ] Document the common transport declaration model.
- [ ] Document transport endpoints, stops, entrances, exits, and connected maps.
- [ ] Document elevator-specific fields and rules.
- [ ] Document escalator-specific fields and rules.
- [ ] Document stair-specific fields and rules.
- [ ] Document generic transport behavior, if supported.
- [ ] Document directionality and service-range rules.
- [ ] Document entry, exit, transfer, and ride costs.
- [ ] Document transport constraints and user-parameter dependencies.
- [ ] Document management-domain and ride-policy interaction.
- [ ] Document availability and conditional selection.
- [ ] Document required actions and hints associated with transport usage.
- [ ] Document invalid stop, endpoint, and map references.
- [ ] Identify transport behavior that is simplified or not yet modeled.

### Verification tasks

- [ ] Trace each transport type from grammar through graph generation and route output.
- [ ] Verify direction, stop ordering, and service-range behavior with tests.
- [ ] Verify management-domain ride-policy scenarios.
- [ ] Verify behavior when no usable transport exists.
- [ ] Identify differences between declared transport costs and routing costs.

### Completion criteria

- [ ] Each supported transport type has its own linkable rule set.
- [ ] Shared transport rules are documented once and referenced by specialized articles.
- [ ] Planned wording does not imply dynamic capabilities not implemented by the engine.

## 9. Content Area 6 — Routing Semantics

**Primary output:** articles under `routing/`

### Points to write

- [ ] Describe how compiled declarations become a routable graph.
- [ ] Document route-request inputs.
- [ ] Document origin and destination resolution.
- [ ] Document user-parameter substitution and validation.
- [ ] Document constraint evaluation during routing.
- [ ] Separate hard eligibility rules from soft route preferences.
- [ ] Document banned-tag behavior.
- [ ] Document each route preference and its intended cost effect.
- [ ] Document visiting modes and their effects.
- [ ] Document route cost composition.
- [ ] Document tie-breaking behavior.
- [ ] Document low-rise routing behavior.
- [ ] Document high-rise routing behavior.
- [ ] Explicitly identify exact, heuristic, approximate, or implementation-dependent behavior.
- [ ] Document transport discovery and selection.
- [ ] Document uncertain-access propagation.
- [ ] Document required-action propagation.
- [ ] Document failure and no-route outcomes.
- [ ] Document determinism expectations.
- [ ] Document relevant performance and scalability boundaries without unsupported guarantees.

### Verification tasks

- [ ] Trace all routing entry points and algorithms in the engine.
- [ ] Establish which request fields currently change route selection.
- [ ] Verify each documented preference with a route-difference test.
- [ ] Verify low-rise and high-rise behavior independently.
- [ ] Record accepted-but-unused inputs as limitations or engine tasks.
- [ ] Identify any mismatch between public service defaults and lower-level routing capabilities.

### Completion criteria

- [ ] The reference states what “best route” means for every supported routing mode.
- [ ] Exactness, heuristics, ignored inputs, and undefined tie-breaking are not obscured.
- [ ] All semantic claims are backed by implementation evidence or executable tests.

## 10. Content Area 7 — Validation and Diagnostics

**Primary output:** `validation/diagnostics.html`

### Points to write

- [ ] Define the validation phases.
- [ ] Document lexical and syntax error categories.
- [ ] Document type error categories.
- [ ] Document name-resolution error categories.
- [ ] Document include and project-composition errors.
- [ ] Document invalid topology errors.
- [ ] Document invalid transport errors.
- [ ] Document constraint and parameter errors.
- [ ] Document compiler consistency errors.
- [ ] Distinguish errors, warnings, and informational diagnostics.
- [ ] Document source-location reporting.
- [ ] Document whether multiple diagnostics may be returned in one run.
- [ ] Document diagnostic stability expectations for tool integrations.
- [ ] Provide a planned index from diagnostic identifiers to relevant reference clauses.

### Verification tasks

- [ ] Inventory diagnostic types and messages in parser, type checker, compiler, and web layer.
- [ ] Identify diagnostics represented only as exceptions or unstructured strings.
- [ ] Verify source positions and multi-error behavior.
- [ ] Create a test fixture for every documented diagnostic family.

### Completion criteria

- [ ] Every invalid example used by the reference maps to an expected diagnostic family.
- [ ] Tool authors can distinguish stable diagnostic fields from human-readable message text.

## 11. Content Area 8 — Compiled and Runtime Outputs

**Primary output:** `routing/route-results.html` plus links to API documentation

### Points to write

- [ ] Describe the boundary between internal compiler structures and supported public output.
- [ ] Document the compiled-building summary exposed to consumers, if supported.
- [ ] Document route-result top-level fields.
- [ ] Document route-step variants.
- [ ] Document node, path, map, and transport identifiers in output.
- [ ] Document labels and human-readable descriptions.
- [ ] Document duration, distance, cost, and their units.
- [ ] Document floor and map transitions.
- [ ] Document transport-entry, ride, transfer, and exit representation.
- [ ] Document required actions.
- [ ] Document uncertainty and access warnings.
- [ ] Document spatial and turn hints.
- [ ] Document empty, partial, failed, and no-route responses.
- [ ] Document field optionality and ordering guarantees.
- [ ] Document serialization and compatibility expectations.
- [ ] Separate language guarantees from REST- or MCP-specific envelopes.

### Verification tasks

- [ ] Inventory route result and API DTO types.
- [ ] Capture deterministic outputs for canonical examples.
- [ ] Trace every output field to its source declaration or routing calculation.
- [ ] Identify internal identifiers or implementation details that should not become compatibility guarantees.
- [ ] Compare engine, REST, MCP, editor, and agent expectations for output shape.

### Completion criteria

- [ ] Consumer-visible fields have planned definitions for meaning, type, units, optionality, and stability.
- [ ] Transport transitions, actions, hints, and warnings are covered by verified examples.

## 12. Content Area 9 — Examples and Glossary

**Primary output:** articles under `examples/` and `glossary.html`

### Part 1: Feature-by-feature tutorials

- [-] Create `examples/feature-by-feature-tutorials.html` using fictional but realistic venue scenarios.
- [x] Cover minimal project composition.
- [x] Cover maps and non-overlapping node areas.
- [x] Cover atomic paths, costs, and intermediate nodes.
- [x] Cover directed and bidirectional movement.
- [x] Cover custom node and path annotations without assigning application-specific semantics.
- [x] Cover values and expressions.
- [x] Cover compilation parameters and access constraints.
- [x] Cover reusable topo maps.
- [x] Cover elevators.
- [x] Cover stairs and escalators.
- [x] Cover management domains and ride policies.
- [x] Cover uncertain access.
- [x] Give every valid and invalid example a code snippet unless no concrete coding guidance applies.
- [x] Classify invalid examples as compilation-invalid, modeling-invalid, or unsupported assumptions.
- [ ] Compile every complete valid example and record the expected result.
- [ ] Verify every compilation-invalid example and record the expected diagnostic category.

### Part 2: `SkyrimTower` mock building

- [-] Create a complete synthetic `SkyrimTower` project spanning the stable language features covered by the tutorials.
- [x] Document the 75-floor building program, special transfer floors, and map-identifier assumptions.
- [x] Document the complete 16-system elevator inventory with realistic selected parameters and explicit station declarations.
- [x] Define destination-sensitive ride policies for emergency state, access permissions, carried goods, and mall operation hours.
- [x] Demonstrate arrival-only station access and complete station lockout on individual floors.
- [x] Parse-check every current root, building-composition, and transport code block.
- [ ] Deferred: define the SkyrimTower topo-map and submap sources after the formal reference areas are complete.
- [ ] Explain how each completed source corresponds to the fictional physical building.
- [ ] Provide the complete project sources as code snippets and downloadable example files.
- [ ] Record representative compilation and routing results.

### Points to write: glossary

- [ ] Define every language keyword and domain term.
- [ ] Distinguish similarly named concepts.
- [ ] Standardize singular, plural, capitalization, and hyphenation.
- [ ] Link each term to its normative reference clause.
- [ ] Identify deprecated or compatibility terminology.
- [ ] Prevent marketing names from replacing precise language terms.

### Verification tasks

- [ ] Compile every valid example in automated tests or documentation checks.
- [ ] Assert expected failures for invalid examples.
- [ ] Run route requests for examples that claim runtime behavior.
- [ ] Check all glossary links and term usage.
- [ ] Ensure examples use synthetic data with clear licensing.

### Completion criteria

- [ ] Every major construct appears in at least one verified example.
- [ ] Every example identifies the engine version against which it was verified.
- [ ] The glossary contains no definition that conflicts with a normative article.

## 13. Standard Article Template

Each reference HTML article should plan for the following sections where applicable:

- [ ] Article title and stable identifier
- [ ] Scope and purpose
- [ ] Terminology used by the article
- [ ] Syntax
- [ ] Name and scope rules
- [ ] Static semantics and type rules
- [ ] Compilation behavior
- [ ] Routing-time or runtime semantics
- [ ] Validation and diagnostics
- [ ] Edge cases and limitations
- [ ] Valid examples
- [ ] Invalid examples
- [ ] Related clauses
- [ ] Version and compatibility notes
- [ ] Implementation and test references used for verification

Sections that do not apply should be omitted rather than filled with placeholder prose.

## 14. HTML Modularity Requirements

- [ ] Use semantic HTML without embedded visual styling.
- [ ] Give each article and normative subsection a stable, unique `id`.
- [ ] Use relative links between articles.
- [ ] Keep heading levels consistent when articles are viewed alone or assembled.
- [ ] Avoid duplicate element IDs across the complete document set.
- [ ] Keep navigation metadata separate from normative content where practical.
- [ ] Ensure code samples are copyable plain text within semantic code elements.
- [ ] Add machine-readable document title, version, and status metadata using one consistent convention.
- [ ] Decide whether articles are standalone HTML documents or includable article fragments before drafting begins.
- [ ] Provide an assembly manifest or ordered index for generating the complete reference.
- [ ] Validate HTML and internal links in CI.

## 15. Cross-Cutting Editorial Tasks

- [ ] Decide which clauses are normative and which are explanatory.
- [ ] Define consistent requirement words such as “must,” “may,” and “should.”
- [ ] Create and enforce a terminology list before parallel drafting begins.
- [ ] Use one canonical spelling for TopoScript and product-specific terms.
- [ ] Separate current behavior from proposed behavior.
- [ ] Mark deprecated and experimental constructs consistently.
- [ ] Avoid claims of accessibility certification, universal optimality, or production safety.
- [ ] Avoid describing unused inputs as effective features.
- [ ] Link limitations to the relevant roadmap or issue without making the reference depend on future work.
- [ ] Review examples for proprietary or sensitive building information.
- [ ] Review the complete reference for contradictions and duplicated rules.

## 16. Source-of-Truth Audit

Before drafting each article, inspect the applicable sources:

- [ ] Parser grammar
- [ ] AST and source-model types
- [ ] Type checker and validation passes
- [ ] Compiler stages and metadata context
- [ ] Compiled graph model
- [ ] Routing algorithms
- [ ] REST request and response DTOs
- [ ] MCP schemas where integration behavior is discussed
- [ ] Unit and integration tests
- [ ] Shipped example projects
- [ ] Existing design specifications under `docs/`

For every externally visible rule:

- [ ] Record at least one implementation location.
- [ ] Record at least one verifying test or add a test task.
- [ ] Record known inconsistencies or missing decisions.
- [ ] Avoid promoting accidental implementation details into guarantees without review.

## 17. Recommended Writing Order

- [ ] Establish glossary and terminology conventions.
- [ ] Complete the implementation and source-of-truth inventory.
- [ ] Draft Language and Execution Model.
- [ ] Draft Whole-Building Composition.
- [ ] Draft Building-Global Declarations.
- [ ] Draft Per-Floor and Per-Area Topology.
- [ ] Draft Cross-Map Transportation.
- [ ] Draft Routing Semantics.
- [ ] Draft Validation and Diagnostics.
- [ ] Draft Compiled and Runtime Outputs.
- [ ] Build and verify examples.
- [ ] Perform cross-reference and contradiction review.
- [ ] Validate and assemble the HTML document set.

## 18. Open Decisions to Resolve Before Normative Drafting

- [ ] Decide the exact version of TopoScript described by the first reference.
- [ ] Decide the compatibility and deprecation policy.
- [ ] Decide whether undocumented parser-accepted syntax is supported or accidental.
- [ ] Decide the formal boundary between building configuration and language syntax.
- [ ] Decide which compiled structures are public contracts.
- [ ] Decide which route-result fields receive compatibility guarantees.
- [ ] Decide how exact and heuristic routing modes are named.
- [ ] Decide how ignored or partially implemented route preferences are documented.
- [ ] Decide the intended semantics for compiler-global mutable state and concurrent compilation.
- [ ] Decide whether diagnostic identifiers will be stabilized for editor integrations.
- [ ] Decide whether HTML modules are standalone pages or assembly fragments.

## 19. Final Completion Checklist

- [ ] All nine content areas have complete HTML articles.
- [ ] Every implemented syntax construct is indexed.
- [ ] Every public semantic claim has code and test evidence.
- [ ] All executable examples pass against the target release.
- [ ] Known limitations and undefined behavior are explicit.
- [ ] Internal links and stable anchors pass automated validation.
- [ ] Terminology is consistent across the reference and public API documentation.
- [ ] Engine maintainers have reviewed normative behavior.
- [ ] A non-maintainer has completed a reference-based review or usage exercise.
- [ ] The assembled reference can be published without adding styling or rewriting its content structure.
