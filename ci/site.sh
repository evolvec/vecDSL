#!/usr/bin/env bash
# Job "site": builds the Capability Matrix into coverage-site/site (CI_RUN, when set, goes on the badges), checks it,
# and compares every count and matrix cell it shows with `vecdsl coverage` (coverage-site/tools/check_agreement.py).
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

options=()
[ -n "${CI_RUN:-}" ] && options=(--ci-run "$CI_RUN")
status=0
"$PYTHON" -I "$ROOT/coverage-site/tools/build_site.py" --workspace "$ROOT" "${options[@]}" || status=$?
expect "build_site.py exit status" "$status" 0
status=0
"$PYTHON" -I "$ROOT/coverage-site/tools/build_site.py" --workspace "$ROOT" --check || status=$?
expect "build_site.py --check exit status" "$status" 0
status=0
timeout 600 "$PYTHON" -I "$ROOT/coverage-site/tools/check_agreement.py" --site "$ROOT/coverage-site/site" -- \
	"${VECDSL_CMD[@]}" > "$WORK/agreement.txt" 2>&1 || status=$?
cat "$WORK/agreement.txt"
expect "site and vecdsl coverage agree (exit status)" "$status" 0

finish
