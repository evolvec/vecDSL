# vecDSL CI

`.github/workflows/ci.yml` runs one script of this folder per job. Every script runs the same way on a developer
machine, so a red job is reproduced locally with one command. The expected numbers live in `expected.env` only.

```
bash ci/run_all.sh                 # every job in workflow order; ci/.work/<job>.log per job, a summary at the end
bash ci/run_all.sh samples site    # only these jobs
bash ci/samples.sh                 # one job
```

Where: Linux and macOS with bash 4 or later; on Windows, **Git Bash** (Git for Windows), or `ci\run_all.ps1` from
PowerShell, which starts Git Bash's bash. WSL works only with a Linux JDK, Maven, Node.js and Python inside WSL (the
Windows ones are not Linux programs), so Git Bash is the supported way on Windows. Needs on the PATH: Java 21 or later,
Maven 3.9 or later, Node.js 22.13 or later with npm, Python 3.10 or later (as `python3` or `python`; `npm run
check-grammar` calls `python`), and the coreutils of Git Bash or Linux (`timeout`, `cmp`, `find`, `cksum`).

## Jobs and scripts

| job (script) | needs | does | expected (expected.env) |
|---|---|---|---|
| coverage-index (`coverage-index.sh`) | Python | `coverage_index.py --check` (every coverage JSON equals its COVERAGE.md); `build_site.py` into `ci/.work/site` and `build_site.py --check` on it; `build_site.py --check` on `coverage-site/site` when one was built there | 14 indexes up to date; `SITE_FILES` 516 |
| build-cli (`build-cli.sh`) | Java, Maven, Python | `mvn -q clean package` in vecdsl-cli; surefire runs per class; `vecdsl version`; `check` of `spec/negative/wf_recall_one.vecDSL` | `CLI_TESTS` ArgsTest 8, VecdslTest 13; exit 1 with "1 program, 1 error, 0 warnings" |
| build-lsp (`build-lsp.sh`) | build-cli's jar | `mvn -q clean package` in vecdsl-lsp (surefire, shade, failsafe against the jar); `lsp_client.py` (every check); `preview_sweep.py --record` | `LSP_SUREFIRE` 28, `LSP_FAILSAFE` 26 (54 runs); `LSP_CLIENT_SUMMARY` 25 of 25; `LSP_SWEEP_SUMMARY` 9282 passed, 0 errors |
| tests (`tests.sh`) | Java, Maven | org.ftn.vecDSL.tests without Eclipse: `mvn -q -f ci/tests/pom.xml clean test`; writes `org.ftn.vecDSL.tests/generated` | `CORE_TESTS` 1959 |
| build-extension (`build-extension.sh`) | Node, Python, build-lsp's jar | `npm ci`, `check-grammar`, `compile`, `lint`, `npm test`, `check-notices`, `vsce package` | `EXT_GRAMMAR` 584 keywords; `EXT_TESTS` 19; `EXT_VSIX_FILES` 10 |
| extension-integration (`extension-integration.sh`), optional | build-extension, a VS Code, a display | `npm run test:integration` (xvfb-run on Linux without a display); `continue-on-error` in CI; locally only with `CI_INTEGRATION=1` | 8 numbered checks |
| samples (`samples.sh`) | build-cli's jar, the tests job's `generated/` | `check` of the samples; `gen` of each sample for its own target, compared file by file with `org.ftn.vecDSL.tests/generated`; `gen` of each sample without its targets line through every generator; `ir --check`; `fmt --write` on a copy, then `fmt --check` and a second `fmt --write` | `CHECK_SUMMARY`; `GEN_FILES_PER_SAMPLE`, `GEN_OWN_FILES` 324; `GEN_ALL_FILES` 4481 with the `GEN_ALL_KNOWN_FAILURES`; `IR_CHECK_SUMMARY` |
| site (`site.sh`) | build-cli's jar, every job above | `build_site.py` into `coverage-site/site` (CI passes the run for the badges), `--check`, `check_agreement.py` against `vecdsl coverage --json`; CI uploads `coverage-site/site` | all counts and cells equal |

Helpers: `lib.sh` (strict mode, the repository root, `expected.env`, Python and vecdsl-cli commands, `expect` and
`finish`), `junit_counts.py` (runs per class of a surefire or failsafe report folder), `tests/pom.xml` (the Maven build of
the test project). `run_all.sh` gives each job 90 minutes; long waits inside the jobs have their own limits.

The vecdsl-cli command of the scripts is `java -jar vecdsl-cli/target/vecdsl-cli.jar`, with
`--sun-misc-unsafe-memory-access=allow` from Java 24 on, as the launchers in `vecdsl-cli/bin` do. Set `VECDSL` to use
another command line (words separated by spaces).

## expected.env

One `NAME=value` per number, read by `source`. A 15th vendor changes `VENDORS`, `GEN_FILES_PER_SAMPLE` (its files per
sample), `SAMPLES`, `CHECK_SUMMARY`, `GEN_OWN_FILES`, `GEN_ALL_FILES`, `IR_CHECK_SUMMARY`, `SITE_FILES` and the test
counts its tests add; nothing in the workflow. Where the values come from:

- `CLI_TESTS`, `LSP_SUREFIRE`, `LSP_FAILSAFE`, `CORE_TESTS`, `EXT_TESTS`: the test classes of vecdsl-cli, vecdsl-lsp,
  org.ftn.vecDSL.tests and vscode-vecdsl, as Maven, the Eclipse JUnit runner and npm count them.
- `LSP_CLIENT_SUMMARY`, `LSP_SWEEP_SUMMARY`: the summary lines of `lsp_client.py` and `preview_sweep.py` against the
  jar built from the repository.
- `CHECK_SUMMARY`, `GEN_*`, `IR_CHECK_SUMMARY`, `SITE_FILES`, `COVERAGE_ROWS`: counted with vecdsl-cli on the samples
  and the coverage matrices of the repository.
- `GEN_ALL_KNOWN_FAILURES`: the Weaviate generator stops with an internal error (exit 3, "no Python form for
  Sparse[...]", likewise for TypeScript and Java) on the five samples that write a sparse vector literal, when they
  are generated for Weaviate without their targets line. `samples.sh` generates these pairs apart and requires the
  failure, so a fix turns the job red until the pair is removed here and `GEN_ALL_FILES` is raised.

## What runs where

| check | CI (GitHub Actions, ubuntu-latest, Temurin 21, Node 22, Python 3.12) | developer machine | release time |
|---|---|---|---|
| coverage index, site, agreement | every push | `run_all.sh` | |
| vecdsl-cli and vecdsl-lsp Maven builds and tests | every push | `run_all.sh` | |
| org.ftn.vecDSL.tests (1959) | every push (Maven) | `run_all.sh`, Eclipse JUnit | |
| org.ftn.vecDSL.ui.tests (15) | no: a JUnit Plug-in Test needs the Eclipse workbench | Eclipse only | |
| vscode-vecdsl build, tests, vsix | every push | `run_all.sh` | |
| VS Code test | every push, optional | `CI_INTEGRATION=1`, or `npm run test:integration` with `VSCODE_EXECUTABLE` | |
| samples through the generators | every push, no vendor server | `run_all.sh` | |
| generated programs against live vendor instances | no | Docker, as the root README describes ("Running generated code") | yes: a system's tier (the `Tier:` line of its COVERAGE.md) decides whether its generated code runs in CI against a local instance (Tier 1) or at release time (Tier 2) |
