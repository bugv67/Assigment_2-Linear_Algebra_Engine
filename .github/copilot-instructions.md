# Copilot instructions — Linear Algebra Engine

Quick orientation
- Purpose: a small Linear Algebra Engine that parses JSON expressions, builds a computation tree (`ComputationNode`), and resolves it using worker threads (`TiredExecutor`) and shared, lock-protected data structures (`SharedMatrix` / `SharedVector`). See `src/main/java/spl/lae/LinearAlgebraEngine.java` for the runtime flow.

Architecture & data flow (high level)
- Input: JSON files in `Examples/` — `InputParser` (`src/main/java/parser/InputParser.java`) parses into a `ComputationNode` tree: operator nodes with `operator` + `operands`, and matrix nodes as 2D arrays.
- Execution: `LinearAlgebraEngine` resolves the tree by repeatedly calling `findResolvable()` on the root, loading child matrices into `SharedMatrix` (row or column major), creating row-level `Runnable` tasks, submitting them to `TiredExecutor`, and finally calling `node.resolve(resultMatrix)`.
- Memory: `SharedMatrix` holds `SharedVector[]` with per-vector locking. Loading replaces the internal `vectors` array after acquiring write locks on the old array; reads acquire read locks. Respect this locking discipline when changing low-level behavior.

Developer workflows (how to build / run / test)
- Build (recommended): `mvn -DskipTests compile` (pom sets Java release 21). To produce a runnable jar: `mvn package` (shade plugin produces an executable jar with main `spl.lae.Main`).
- Run examples and compare outputs: `scripts/run_examples_and_compare.sh [threads]` — this compiles (if `mvn` available), runs `spl.lae.Main` on `Examples/example*.json` and diffs with `Examples/out*.json` into `script_output/`.
- Tests: standard Maven tests in `src/test/java`. Run all tests: `mvn test`. Run single test: `mvn -Dtest=memory.TestSharedMatrix test`.

Project-specific patterns & gotchas (do not change these lightly)
- Parsing rules: `InputParser` rejects 1D arrays (vectors) as standalone nodes and requires uniform row lengths for matrices. Operator nodes must have `operator` and `operands` fields — follow this JSON schema when adding tests or examples.
- Computation resolution: the engine depends on `ComputationNode.findResolvable()` and `associativeNesting()` to reorganize n-ary operations. Changes to node resolution logic must preserve `findResolvable()` semantics used across tests.
- Locking model: `SharedMatrix` and `SharedVector` expose per-vector read/write locks. Loads replace the `vectors` array atomically while holding write locks on the previous array. Tasks operate on returned `SharedVector` instances — maintain or respect these locks when mutating rows/columns.
- Task granularity: tasks are row-level Runnables created by `createAddTasks()`, `createMultiplyTasks()`, `createNegateTasks()`, `createTransposeTasks()` in `LinearAlgebraEngine`. Keep task side-effects localized to a single row/vector to avoid needing coarser synchronization.

Integration & external dependencies
- Runtime: Java 21 (pom property `<maven.compiler.release>21</maven.compiler.release>`).
- Libraries: `jackson-databind` for JSON parsing; `junit-jupiter` for tests.
- Entrypoint: `spl.lae.Main` (packaged via Maven jar/shade plugin). Scripts rely on `target/classes` and optional runtime classpath built by Maven.

Examples to consult when making changes
- Parsing and node structure: `src/main/java/parser/InputParser.java` and `src/main/java/parser/ComputationNode.java` (use these to understand JSON→tree mapping).
- Execution & tasks: `src/main/java/spl/lae/LinearAlgebraEngine.java` (task creation and submission patterns).
- Locking & representation: `src/main/java/memory/SharedMatrix.java` and `src/main/java/memory/SharedVector.java` (vector-level locks and orientation).
- Runner script: `scripts/run_examples_and_compare.sh` (automates compile+run+diff for Examples/*.json).

What an AI agent should do first
- When changing computation or memory code: run `mvn -DskipTests compile` then the `run_examples_and_compare.sh` script to validate behavior against `Examples/out*.json`.
- When adding/removing public API (parsing format, Main signature, or output structure), update `Examples/` and `Examples/out*.json` accordingly and include a script run to regenerate `script_output/`.

If something is unclear or you want me to expand examples, tell me which area (parsing, scheduling, memory) and I will add targeted instructions and snippets.
