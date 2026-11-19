# vecdsl-cli

The headless toolchain of vecDSL 1.0: one runnable jar that parses, validates, formats, exports and checks the IR of,
generates client code for, and explains the coverage of a vecDSL program without Eclipse. It runs the same classes as
the Eclipse editor and the test project (the org.ftn.vecDSL runtime bundle, its Xtext 2.42.0 runtime, its generators
and the coverage index the editor reads, class resources of the runtime bundle). Build: [BUILD.md](BUILD.md).

![vecDSL CI](../coverage-site/site/badges/ci.svg)

```
bin\vecdsl.cmd <command> [options]          (Windows, cmd or PowerShell)
bin\vecdsl.ps1 <command> [options]          (PowerShell, where the execution policy allows scripts)
bin/vecdsl.sh <command> [options]           (Linux, macOS)
java -jar target/vecdsl-cli.jar <command> [options]
```

Runtime: Java 21 or later; the jar is built for Java 21. On Java 24 and 25 the JVM prints a one-time warning about
`sun.misc.Unsafe` from Guice 7.0.0 to standard error when the jar is run with plain `java -jar`; the launchers in `bin/`
pass `--sun-misc-unsafe-memory-access=allow` on those versions (JEP 498) and nothing on Java 21, which rejects the
option. Output and exit status are the same either way. The launchers use the java of
`JAVA_HOME`, else the one on the PATH.

Every command accepts `--verbose` (stack trace of an internal error). Exit status of every command:

| status | meaning |
|---|---|
| 0 | ok |
| 1 | diagnostics or differences: an error in a program, a file that would be reformatted, a failed round trip, nothing generated, no matching row |
| 2 | usage error: unknown command or option, missing value or operand, unknown vendor, language or status |
| 3 | internal error; the message names the exception, `--verbose` prints the stack trace |

Program operands are files, or folders whose `*.vecDSL` files are taken (not recursively, in name order). Programs are
read as UTF-8.

## check

```
vecdsl check <files or folders> [--no-coverage] [--json [-o <folder>]]
```

Parses and validates each program with the language's validator (syntax, linking and validation issues, the same as the
editor's markers) and, when the script writes `targets [...]`, adds the editor's `vecdsl.coverage.unsupported` warnings
(one per construct a targeted vendor does not support, from the coverage index), computed by the same
`org.ftn.vecDSL.editor.CoverageIssues` the Eclipse editor calls. `--no-coverage` leaves those out.
Prints `file:line:column: severity code message` per issue and a summary line.

```
$ vecdsl check ../spec/negative/wf_recall_one.vecDSL
../spec/negative/wf_recall_one.vecDSL:14:104: error wf.recall.one target-recall must be less than 1; recall 1 is written 'exact'
1 program, 1 error, 0 warnings
```

`--json` prints the diagnostics document of `spec/conformance/<name>/diagnostics.json` (errors only, in the
validator's order), byte for byte, so a rejected program can be diffed with its conformance document. With several
programs, `-o <folder>` writes `<name>.json` per program.

```
$ vecdsl check --json ../spec/negative/wf_recall_one.vecDSL > out.json
$ fc out.json ..\spec\conformance\wf_recall_one\diagnostics.json        (no differences)
```

Exit 1 when any program has an error; warnings do not change the status.

## fmt

```
vecdsl fmt <files or folders> [--check | --write] [--width N] [--indent N|tab]
```

Formats with the language's formatter (VecDSLFormatter, the Formatter2 of Ctrl+Shift+F). Without `--check` or
`--write` it prints the formatted text of one program. `--check` lists every file that would change (exit 1 if any);
`--write` rewrites the files that change. `--width` sets `vecdsl.format.shortStatementWidth` (default 100); `--indent`
sets the indentation (default 4 spaces; `tab` for a tab). The program's own line separator (LF or CRLF) is kept.

Before anything is printed or written, the formatted text is parsed again: it must have the same tokens and give a model
equal to the original (EcoreUtil.equals), otherwise the command stops with status 3 and writes nothing. A program with
syntax errors is not formatted (as in the editor) and counts as a finding.

```
$ vecdsl fmt --check ../org.ftn.vecDSL.tests/samples
would reformat ../org.ftn.vecDSL.tests/samples/chroma_demo.vecDSL
...
$ vecdsl fmt --write copies/ && vecdsl fmt --check copies/        (second command prints nothing, exit 0)
```

## ir

```
vecdsl ir <file> [-o out.json]
vecdsl ir --check <folder>
vecdsl ir --export <folder> <out-folder>
```

A thin wrapper over `org.ftn.vecDSL.ir.VecDSLIrMain` with its exit statuses: the IR of a program (or its diagnostics
document when it is rejected, status 1), the round trip of every program of a folder (status 1 if one fails), the IR or
diagnostics document of every program of a folder (status 1 only when the folder has no program).

```
$ vecdsl ir --check ../org.ftn.vecDSL.tests/samples
ok   chroma_demo.vecDSL (52203 bytes of IR)
...
28 programs, 28 round trips passed, 0 failed
```

There is no `--validate`: no JSON Schema validator is on the classpath without a new dependency. Validate IR documents with `python spec/tools/ir_schema.py --check FILES`.

## gen

```
vecdsl gen <file> --target <vendor,...|all> [--lang <py,ts,java|all>] -o <folder> [--dry-run]
```

Runs the language's generators as the Eclipse builder does and writes `<folder>/<vendor>/<language>/...` plus each
vendor's environment files (`docker-compose.yml`, `README.md`). The script's `targets [...]` still applies: `--target`
selects among them. Without `--lang` the script's `language` hint applies, otherwise every language; `--lang all`
generates every language whatever the hint. `--dry-run` lists the files without writing. The output folder is not
cleaned first. A program with errors is not generated (status 1, its errors printed); selecting no vendor of the
script's targets is status 1 as well.

```
$ vecdsl gen ../org.ftn.vecDSL.tests/samples/redis_demo.vecDSL --target redis --lang all -o out
wrote out/redis/python/...
...
11 files
```

Vendors: milvus, pinecone, weaviate, qdrant, chroma, pgvector, redis, mongodb, oracledb, elasticsearch, opensearch,
vespa, lancedb, duckdb. Languages: `py` (python), `ts` (typescript), `java`.

## explain

```
vecdsl explain <file> [--statement <label>] [--target <vendor,...>] [--json]
```

For each statement (or the labeled one), the constructs it uses and, per target, the coverage status, the Python,
TypeScript and Java calls and the reference: the data of the editor's keyword hover. A statement uses the class row of
every object it contains that a matrix lists (statements, concepts, alter operations, extension items) and the feature
row of every clause written on such an object. Targets: `--target`, otherwise the script's `targets`, otherwise every
vendor with coverage data. A program with errors is not explained (status 1).

```
$ vecdsl explain ../org.ftn.vecDSL.tests/samples/milvus_demo.vecDSL --statement byId
../org.ftn.vecDSL.tests/samples/milvus_demo.vecDSL (targets: milvus)

statement byId (Method GetById), line 49
  Method GetById
    milvus: native
      python: `get`
      ...
```

The label of a row is `native`, `concealed`, `unsupported`, `not-applicable`, `supported by <vendor>, not generated
yet` or `no coverage data`; an unsupported or not yet generated row also shows its reason.

## coverage

```
vecdsl coverage [--target <vendor>] [--construct <id>] [--status native|concealed|unsupported|not-applicable|pending] [--json]
```

Without a filter, the summary per vendor: rows, and the rows per status, where a row whose generation is pending
counts as `pending` (the "+pending" rule of the coverage index). With a filter, the matching rows (`vendor`, `construct`, cell and,
for unsupported and pending rows, the reason); status 1 when no row matches.

```
$ vecdsl coverage --target qdrant --status pending
qdrant	Snapshot.scope	pending	collection and full; shard: native in Qdrant; generation pending ...
...
12 rows
```

## version

```
$ vecdsl version
language: vecDSL 1.0 (1.0.1 documentation patch)
IR: 1.0
grammar namespace: http://www.ftn.org/vecDSL/VecDSL/7
vecdsl-cli: 0.1.0 (implements vecDSL >=1.0,<2.0)
```

## CI

The job `build-cli` of `.github/workflows/ci.yml` runs `ci/build-cli.sh`: `mvn -q clean package` with its 21 tests
(`ArgsTest` 8, `VecdslTest` 13) and the smoke test of BUILD.md, step 4. The job `samples` runs `ci/samples.sh` with the
jar: `check` of the 28 samples, `gen` of each sample for its own target (compared file by file with
`org.ftn.vecDSL.tests/generated`) and through every generator, `ir --check`, and `fmt --write` then `fmt --check` on a
copy. The job `site` compares the Capability Matrix with `vecdsl coverage --json`. Locally, from the repository root in
Git Bash or on Linux: `bash ci/run_all.sh build-cli samples` (samples needs `org.ftn.vecDSL.tests/generated`, which the
job `tests` or an Eclipse test run writes). Jobs: `ci/README.md`; expected numbers: `ci/expected.env`. The badge above is
`coverage-site/site/badges/ci.svg`, written by `coverage-site/tools/build_site.py` (it shows the CI run once CI builds the
site, and is missing until the site is built).
