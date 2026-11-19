#!/usr/bin/env bash
# Job "build-cli": vecdsl-cli with Maven (vecdsl-cli/BUILD.md, step 2): compiles the runtime bundle with the CLI, runs
# the JUnit tests (surefire), writes vecdsl-cli/target/vecdsl-cli.jar; then the smoke test of BUILD.md, step 4.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

status=0
(cd "$ROOT/vecdsl-cli" && timeout 1800 mvn -B -q clean package) || status=$?
expect "mvn clean package exit status" "$status" 0
reports=$("$PYTHON" -I "$ROOT/ci/junit_counts.py" "$ROOT/vecdsl-cli/target/surefire-reports" || true)
expect "surefire test runs" "$(head -n 1 <<< "$reports")" "$CLI_TESTS"
expect "surefire failures" "$(sed -n 2p <<< "$reports")" "failures 0 errors 0 skipped 0"

status=0
version=$(vecdsl version 2>&1) || status=$?
expect "vecdsl version exit status" "$status" 0
expect "vecdsl version" "$(grep -c '^vecdsl-cli: ' <<< "$version")" 1
status=0
negative=$(vecdsl check "$ROOT/spec/negative/wf_recall_one.vecDSL" 2>&1) || status=$?
expect "check of spec/negative/wf_recall_one.vecDSL (exit, summary)" "$status $(tail -n 1 <<< "$negative" | tr -d '\r')" \
	"1 1 program, 1 error, 0 warnings"

finish
