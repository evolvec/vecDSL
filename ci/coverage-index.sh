#!/usr/bin/env bash
# Job "coverage-index": the coverage index and the Capability Matrix are up to date with the coverage matrices.
#   1. coverage_index.py --check: every org.ftn.vecDSL/.../coverage/index/<vendor>.json equals what its COVERAGE.md gives
#   2. build_site.py into the work folder (stops on an index whose SHA-256 differs from its COVERAGE.md, a construct
#      without a grammar section, two constructs sharing a page name), then build_site.py --check on it
#   3. build_site.py --check on coverage-site/site when a site was built there (locally: the site is not stale)
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

status=0
"$PYTHON" -I "$ROOT/org.ftn.vecDSL/tools/coverage_index.py" --workspace "$ROOT" --check > "$WORK/coverage-index.txt" ||
	status=$?
cat "$WORK/coverage-index.txt"
expect "coverage_index.py --check exit status" "$status" 0
expect "coverage indexes up to date" "$(grep -c ': up to date (' "$WORK/coverage-index.txt")" "$(wc -w <<< "$VENDORS" | tr -d ' ')"

site=$WORK/site
status=0
"$PYTHON" -I "$ROOT/coverage-site/tools/build_site.py" --workspace "$ROOT" --out "$site" || status=$?
expect "build_site.py exit status" "$status" 0
expect "site files" "$(find "$site" -type f ! -name manifest.json | wc -l | tr -d ' ')" "$SITE_FILES"
status=0
"$PYTHON" -I "$ROOT/coverage-site/tools/build_site.py" --workspace "$ROOT" --out "$site" --check || status=$?
expect "build_site.py --check exit status" "$status" 0

if [ -f "$ROOT/coverage-site/site/manifest.json" ]; then
	status=0
	"$PYTHON" -I "$ROOT/coverage-site/tools/build_site.py" --workspace "$ROOT" --check || status=$?
	expect "build_site.py --check of coverage-site/site" "$status" 0
fi

finish
