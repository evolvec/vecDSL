# vecDSL

vecDSL is a domain-specific language for vector databases. A vecDSL program declares collections, vector fields, indexes
and the operations on them (writes, vector, keyword and hybrid searches, recommendations, snapshots, tenants) once, and
a code generator translates it into Python, TypeScript and Java programs for each target system, using that system's
official client libraries. Where a target lacks a construct, the generator raises an error with the reason instead of
emitting code that would behave differently. vecDSL 2.0, this repository, implements the vecDSL 1.0 language: an Xtext
grammar and metamodel, a validator, an Eclipse editor, an intermediate representation (IR), fourteen code generators, a
command-line toolchain, a language server, a Visual Studio Code extension and the Capability Matrix, a site that shows
what each target supports.

## Target systems

| target | vendor id | kind |
|---|---|---|
| Milvus | `milvus` | server |
| Pinecone | `pinecone` | managed service |
| Weaviate | `weaviate` | server |
| Qdrant | `qdrant` | server |
| Chroma | `chroma` | server |
| PostgreSQL with pgvector | `pgvector` | server |
| Redis Open Source (Query Engine) | `redis` | server |
| MongoDB with mongot (Atlas Local) | `mongodb` | server |
| Oracle AI Database 26ai | `oracledb` | server |
| Elasticsearch | `elasticsearch` | server |
| OpenSearch | `opensearch` | server |
| Vespa | `vespa` | server |
| LanceDB | `lancedb` | embedded library |
| DuckDB with the vss and fts extensions | `duckdb` | embedded library |

The versions each generator targets are pinned in `generator/<vendor>/<vendor>.properties`; the generated projects take
their versions from that file. vecDSL 1.0 has no `duckdb` vendor id: DuckDB programs omit the `targets` clause and are
generated for DuckDB with the generator's target setting.

## Repository layout

| path | contents |
|---|---|
| `org.ftn.vecDSL/` | the runtime bundle: grammar `src/org/ftn/vecDSL/VecDSL.xtext`, generated metamodel `model/generated/`, validator, formatter, IR (`ir/`), coverage index (`coverage/`), editor services shared by every host (`editor/`), the generators (`generator/`) and the capability manifests (`capabilities/`) |
| `org.ftn.vecDSL/src/org/ftn/vecDSL/generator/<vendor>/` | one generator per target: the Xtend and Java code, the runtimes and templates (`python/`, `typescript/`, `java/`), the local environment (`environment/`, with its `README.md`), `COVERAGE.md` and `MAPPING.md` |
| `org.ftn.vecDSL/src/org/ftn/vecDSL/generator/common/shapes/` | the shared result-shape module (boost, cursor, mmr, group-by, sort, totals, aggregations) in Python, TypeScript and Java, with its `README.md` |
| `org.ftn.vecDSL/tools/` | `coverage_index.py` (builds the coverage index from the `COVERAGE.md` files) and `grammar_report.py` (grammar exports) |
| `org.ftn.vecDSL.ide/` | editor services for language servers (content assist, hover, quick fixes, formatting) |
| `org.ftn.vecDSL.ui/` | the Eclipse editor |
| `org.ftn.vecDSL.tests/` | the JUnit 5 tests; `samples/` holds 28 sample programs (a demo and a full sample per target) as a project of its own; `scenarios/` holds 12 scenario programs and `scenarios/negative/` 27 programs that each violate one well-formedness judgment |
| `org.ftn.vecDSL.ui.tests/` | the editor tests (JUnit Plug-in Test) |
| `spec/` | the language specification: `VecDSL-1.0.xtext`, `GRAMMAR.md`, `SEMANTICS.md`, `METAMODEL.md`, `KEYWORDS.txt`, `VERSIONING.md`, `CHANGELOG.md`, `README.md` (what 1.0 is and how to check it), the IR schema (`ir/`), the grammar exports (`csv/`), seven rejected programs (`negative/`), the conformance corpus (`conformance/`) and the scripts that build these files (`tools/`) |
| `vecdsl-cli/` | the command-line toolchain: `vecdsl check`, `fmt`, `ir`, `gen`, `explain`, `coverage`, `version` in one runnable jar (Maven) |
| `vecdsl-lsp/` | the language server: Xtext's language server with the editor services of `org.ftn.vecDSL.ide` and the preview request `vecdsl/generate` (Maven) |
| `vscode-vecdsl/` | the Visual Studio Code extension: highlighting, the language server as client, a generated-code preview (npm) |
| `coverage-site/` | the Capability Matrix: `tools/build_site.py` writes a static site from the coverage index into `coverage-site/site/` |
| `ci/`, `.github/workflows/ci.yml` | continuous integration: one script per job, the same on GitHub Actions and on a developer machine, and the expected numbers (`ci/expected.env`) |

`src-gen/` and `xtend-gen/` are committed, so the projects build without running the Xtext generator.

## Requirements

- Eclipse IDE, Eclipse Modeling Tools package, with Xtext 2.42 (Help > Install New Software, the Xtext update site)
- Java 21 (the bundles require JavaSE-21)
- Python 3.10 or later for the scripts in `org.ftn.vecDSL/tools/`, `spec/tools/`, `coverage-site/tools/` and `ci/`
  (standard library only, except `spec/tools/ir_schema.py --check`, which needs `jsonschema`)
- For the toolchain and the tests without Eclipse: Maven 3.9 or later with access to Maven Central
- For the extension: Node.js 22.13 or later with npm
- To run generated code: Python 3.10 or later, Node.js 22 or later, Java 17 or 21 with Maven, and Docker for the server
  targets. Each generated project carries a `README.md` or a `docker-compose.yml` with its own requirements.

## Import into Eclipse

1. Clone the repository and open an Eclipse workspace (the clone itself can be the workspace folder).
2. File > Import > General > Existing Projects into Workspace, select the clone, tick "Search for nested projects", and
   import the six projects: `org.ftn.vecDSL`, `org.ftn.vecDSL.ide`, `org.ftn.vecDSL.ui`, `org.ftn.vecDSL.tests`,
   `org.ftn.vecDSL.ui.tests` and `samples` (the sample programs inside `org.ftn.vecDSL.tests`). The folders of the
   other components are not Eclipse projects and are not imported.
3. Project > Clean, all projects. The workspace builds without errors.

Regenerating the language infrastructure (`org.ftn.vecDSL/src/org/ftn/vecDSL/GenerateVecDSL.mwe2`, Run As > MWE2 Workflow)
is needed only after a grammar change. On its first run Xtext downloads the ANTLR generator jar
(`.antlr-generator-3.2.0-patch.jar`), which is not committed.

## Run the tests

- `org.ftn.vecDSL.tests`: Run As > JUnit Test (JUnit 5). The project runs 1959 tests.
- `org.ftn.vecDSL.ui.tests`: Run As > JUnit Plug-in Test, not plain JUnit Test, because the editor tests need a
  workbench. The project runs 15 tests. TROUBLESHOOTING.md has the launch settings.

`ScenarioProgramsTest` and `NegativeProgramsTest` read `org.ftn.vecDSL.tests/scenarios` and `scenarios/negative` and write
`scenario-results.csv` and `negative-results.csv` next to the programs; the system properties `vecdsl.scenarios` and
`vecdsl.negative` point them to other folders. Several generator tests write generated code under
`org.ftn.vecDSL.tests/generated/`. Neither output is committed.

## Run the tests without Eclipse

`ci/tests/pom.xml` builds `org.ftn.vecDSL`, `org.ftn.vecDSL.ide` and `org.ftn.vecDSL.tests` with Maven and runs the
same 1959 tests with surefire, from the folder of the test project. It needs Java 21 or later, Maven 3.9 or later and
access to Maven Central (Xtext 2.42.0 and its dependencies). From the repository root:

```
mvn -f ci/tests/pom.xml clean test
```

Expected: the surefire summary `Tests run: 1959, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`; the reports
are in `ci/tests/target/surefire-reports`, and `python ci/junit_counts.py ci/tests/target/surefire-reports` prints the
runs per class. Like the Eclipse run, it writes `org.ftn.vecDSL.tests/generated/`. `org.ftn.vecDSL.ui.tests` needs the
Eclipse workbench and runs only in Eclipse.

## Verify your setup

After a clone, these are the results to expect:

| check | how | expected |
|---|---|---|
| Eclipse build | Project > Clean, all projects | 0 errors |
| core tests | `org.ftn.vecDSL.tests`, Run As > JUnit Test, or `mvn -f ci/tests/pom.xml clean test` | 1959 runs, 0 failures |
| editor tests | `org.ftn.vecDSL.ui.tests`, Run As > JUnit Plug-in Test | 15 runs, 0 failures |
| coverage index | `python org.ftn.vecDSL/tools/coverage_index.py --check` | 14 lines "up to date", exit 0 |
| vecdsl-cli on the samples | `vecdsl-cli/bin/vecdsl.sh check org.ftn.vecDSL.tests/samples` (Windows: `vecdsl-cli\bin\vecdsl.cmd`), after building vecdsl-cli | last line `28 programs, 0 errors, 18 warnings`, exit 0 |
| everything CI runs | `bash ci/run_all.sh` (Git Bash on Windows) | the jobs coverage-index, build-cli, build-lsp, tests, build-extension, samples and site each with `exit 0`, last line `run_all.sh: all checks passed`; extension-integration runs only with `CI_INTEGRATION=1` |

`ci/README.md` lists what each CI job checks and `ci/expected.env` every expected number. When a result differs,
[TROUBLESHOOTING.md](TROUBLESHOOTING.md) lists the known problems and their fixes.

## Generate code for a sample

1. Open a sample in the `samples` project, for example `qdrant_demo.vecDSL`. Its `targets [ qdrant ]` clause names the
   targets to generate for.
2. Save the file. The generator writes, for each target, a Python, a TypeScript and a Java project under
   `samples/generated/<vendor>/` (`python/` with `requirements.txt`, `typescript/` with `package.json`, `java/` with
   `pom.xml`). For a server target it also writes `docker-compose.yml`, which starts the pinned server image; the embedded
   targets (LanceDB, DuckDB) get a `README.md` instead.

Without Eclipse, vecdsl-cli writes the same files:
`vecdsl gen org.ftn.vecDSL.tests/samples/qdrant_demo.vecDSL --target qdrant --lang all -o out`.

The output folder of each target can be changed per project under Properties > VecDSL > Compiler.

## Running generated code

Server targets run in Docker: the generated `docker-compose.yml` of each target starts the pinned server image on
127.0.0.1, and its header comment says how to start it, which ports it opens, when it is ready and how its data is
removed. Run `docker compose up -d` in the target's folder (`samples/generated/<vendor>/`), then a program:

- Python: `pip install -r requirements.txt`, then `python <program>.py`
- TypeScript: `npm install`, `npm run build`, `npm start`
- Java: `mvn -q compile exec:java`, or import the folder into Eclipse as a Maven project

Endpoints and credentials come from environment variables named `VECDSL_<VENDOR>_...` (for example
`VECDSL_QDRANT_URI`, `VECDSL_PGVECTOR_URL`, `VECDSL_ORACLEDB_DSN`); without them the programs use the local defaults
of the compose file. Passwords are never generated into the files: for Elasticsearch, OpenSearch, MongoDB, Oracle,
pgvector and Redis, set the password variables the compose file names (`VECDSL_<VENDOR>_PASSWORD`, for MongoDB also
`VECDSL_MONGODB_USER`, for Oracle also `ORACLE_PWD`) before `docker compose up`, which refuses to start without them, and
give the programs the same values.

| target | what a run needs |
|---|---|
| Milvus, Weaviate, Qdrant, Chroma, pgvector, Redis, MongoDB, Elasticsearch, OpenSearch, Vespa | Docker only; no account |
| Oracle AI Database | Docker; the free image is pulled without a login, uses up to 2 GB of RAM (give Docker at least 4 GB), and its first start creates the database in several minutes |
| Pinecone | real results need a Pinecone project and its API key (`VECDSL_PINECONE_API_KEY`); the generated compose file runs a mock of the Pinecone API instead, which checks request shapes but stores nothing (its header names the specification checkout it needs and the variables that point the programs at it) |
| LanceDB, DuckDB | no server and no Docker: the library runs inside the program; the generated `README.md` names the variables (`VECDSL_LANCEDB_URI`, `VECDSL_DUCKDB_PATH`) |

## The editor

| action | how |
|---|---|
| Format a file or a selection | Ctrl+Shift+F; only whitespace changes, and a file with a syntax error is not formatted |
| Content assist | Ctrl+Space; each clause proposal shows its status on every target of `targets [...]`, and a clause unsupported on a target is ranked last with the reason |
| Hover | on a keyword: the grammar comment, the language design decisions (D1 to D15 of the grammar header) it cites and one row per target with status, calls and reference; on a field, collection, `$parameter` or `env(...)`: its declaration |
| Code of a statement | hover over a statement's label (`similar` in `similar: vector-search ...`): the code the generator writes for the first target, with the reasons a construct is unsupported |
| Quick fix | Ctrl+1 on a marker: rename a reserved word used as a name, put fixed-order clauses in grammar order, use the declared metric, move a `geo_point` value into a parameter, remove a target that does not support a clause |

A construct that a target of the program does not support is marked with a warning that names the target and the
reason. The width up to which a statement stays on one line (100 columns) is the preference
`vecdsl.format.shortStatementWidth` (qualifier `org.ftn.vecDSL.VecDSL`), set per project in
`<project>/.settings/org.ftn.vecDSL.VecDSL.prefs`. Outside Eclipse, the same services come from the language server
(`vecdsl-lsp/`) and the VS Code extension (`vscode-vecdsl/`).

## Coverage matrices

For each target, `org.ftn.vecDSL/src/org/ftn/vecDSL/generator/<vendor>/` holds:

- `COVERAGE.md`: one row for each of the 456 constructs of the metamodel, with its status (native, concealed,
  unsupported, not-applicable), the Python, TypeScript and Java calls, the reference to the target's documentation or
  source, the test that covers it, and notes. A row whose notes start with "generation pending" is supported by the target
  but not generated yet; the editor shows it as "supported by <vendor>, not generated yet".
- `MAPPING.md`: the facts behind the mapping, numbered, each with its source, then the findings from runs against the
  target's server ("Server findings") and the known limitations.

A `COVERAGE.md` whose header carries a `Tier:` line assigns the target's tier: Tier 1 targets run their generated code
in CI against a local instance, Tier 2 targets at release time.

The editor, the language server and the command-line checker read the coverage index
`org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index/<vendor>.json`, which is built from the `COVERAGE.md` files. After a
change to a `COVERAGE.md`, from the repository root:

```
python org.ftn.vecDSL/tools/coverage_index.py          # writes the 14 index files
python org.ftn.vecDSL/tools/coverage_index.py --check  # exit 1 if an index differs from its COVERAGE.md
```

then refresh `org.ftn.vecDSL` in Eclipse (F5). `CoverageIndexTest` fails while an index is out of date. The Capability
Matrix shows all 14 matrices side by side: `python coverage-site/tools/build_site.py` writes it into
`coverage-site/site/` (open `index.html`); `coverage-site/README.md` describes the site.

## Other components

Each component is a folder of this repository. Its builds read the bundles of the repository as sources
(`../org.ftn.vecDSL`, `../org.ftn.vecDSL.ide`), so build them from a clone with the folder layout unchanged.

| component | build | documentation |
|---|---|---|
| `vecdsl-cli/` | `mvn -q clean package` in `vecdsl-cli/`: 21 tests, then `vecdsl-cli/target/vecdsl-cli.jar`; run it with `bin/vecdsl.sh`, `bin\vecdsl.cmd` or `bin\vecdsl.ps1` | `README.md` (commands), `BUILD.md` |
| `vecdsl-lsp/` | `mvn -q clean package` in `vecdsl-lsp/`: 54 test runs, then `vecdsl-lsp/target/vecdsl-lsp.jar` | `README.md` (protocol, editors), `BUILD.md` |
| `vscode-vecdsl/` | `npm ci`, `npm run check-grammar`, `npm run compile`, `npm run lint`, `npm test` (19 tests), `npm run check-notices` in `vscode-vecdsl/`; `npx @vscode/vsce package` writes the `.vsix`, with the jar of vecdsl-lsp | `README.md` (installation, settings), `BUILD.md`, `CHANGELOG.md` |
| `coverage-site/` | `python coverage-site/tools/build_site.py`, then `--check`; `coverage-site/tools/check_agreement.py -- java -jar vecdsl-cli/target/vecdsl-cli.jar` compares it with the CLI | `README.md` |
| `ci/` | `bash ci/run_all.sh` from the repository root, or one job: `bash ci/<job>.sh` | `README.md`, `expected.env` |

## License

Copyright 2025-2026 Elena Akik.

vecDSL is licensed under the [Apache License 2.0](LICENSE) (see also [NOTICE](NOTICE)): the grammar and the
language specification, the bundles, the generators and their runtimes, vecdsl-cli, vecdsl-lsp, the VS Code
extension, the Capability Matrix scripts and CI.

The code vecDSL generates from your programs, including the runtime files it copies into a generated project, is
yours: use, change and distribute it under any terms you choose, without the notice requirements of the license.

The jars and the vsix bundle third-party components (Eclipse Xtext, EMF and LSP4J under EPL 2.0, Guava and Guice
under Apache 2.0, ANTLR 3 under BSD, npm packages under MIT); `vscode-vecdsl/THIRD-PARTY-NOTICES.txt` lists those of the
server jar and the vsix with their license texts.

## Previous version

vecDSL v1.0, the prototype described in the journal paper, is the v1.0 release and the vecDSL-v1.0 branch of this
repository.
