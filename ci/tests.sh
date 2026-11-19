#!/usr/bin/env bash
# Job "tests": the whole org.ftn.vecDSL.tests project without Eclipse (the generator, coverage, IR, formatter, editor
# and ide tests; it writes org.ftn.vecDSL.tests/generated, which the samples job compares with). ci/tests/pom.xml
# compiles org.ftn.vecDSL, org.ftn.vecDSL.ide and the test project against Xtext 2.42.0 from Maven Central and runs
# every *Test class with surefire from org.ftn.vecDSL.tests.
# org.ftn.vecDSL.ui.tests is a JUnit Plug-in Test and runs only in Eclipse.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

status=0
timeout 2400 mvn -B -q -f "$ROOT/ci/tests/pom.xml" clean test > "$WORK/tests.txt" 2>&1 || status=$?
tail -n 20 "$WORK/tests.txt"
expect "mvn clean test exit status" "$status" 0
reports=$("$PYTHON" -I "$ROOT/ci/junit_counts.py" "$ROOT/ci/tests/target/surefire-reports" || true)
total=$(head -n 1 <<< "$reports" | tr ' ' '\n' | awk -F= '{ sum += $2 } END { print sum + 0 }')
expect "surefire test runs" "$total" "$CORE_TESTS"
expect "surefire failures" "$(sed -n 2p <<< "$reports")" "failures 0 errors 0 skipped 0"

finish
