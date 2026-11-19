#!/usr/bin/env bash
# Job "build-lsp": vecdsl-lsp with Maven (vecdsl-lsp/BUILD.md, step 2): surefire, the shaded jar, failsafe against the
# jar; then the scripted client (lsp_client.py, every check) and the preview sweep (preview_sweep.py, every statement
# of every sample for every target and language) against vecdsl-lsp/target/vecdsl-lsp.jar. The client formats with
# vecdsl-cli/target/vecdsl-cli.jar (job build-cli).
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

status=0
(cd "$ROOT/vecdsl-lsp" && timeout 2400 mvn -B -q clean package) || status=$?
expect "mvn clean package exit status" "$status" 0
for kind in surefire failsafe; do
	reports=$("$PYTHON" -I "$ROOT/ci/junit_counts.py" "$ROOT/vecdsl-lsp/target/$kind-reports" || true)
	expected=LSP_${kind^^}
	expect "$kind test runs" "$(head -n 1 <<< "$reports")" "${!expected}"
	expect "$kind failures" "$(sed -n 2p <<< "$reports")" "failures 0 errors 0 skipped 0"
done

jar=$ROOT/vecdsl-lsp/target/vecdsl-lsp.jar
scripts=$ROOT/vecdsl-lsp/src/test/scripts
status=0
timeout 900 "$PYTHON" -I "$scripts/lsp_client.py" --workspace "$ROOT" \
	--fmt "java -jar $(native "$ROOT/vecdsl-cli/target/vecdsl-cli.jar")" -- java -jar "$jar" > "$WORK/lsp-client.txt" 2>&1 ||
	status=$?
expect "lsp_client.py exit status" "$status" 0
expect "lsp_client.py summary" "$(grep -o '[0-9]* of [0-9]* checks passed' "$WORK/lsp-client.txt" | tail -n 1)" \
	"$LSP_CLIENT_SUMMARY"
status=0
timeout 900 "$PYTHON" -I "$scripts/preview_sweep.py" --workspace "$ROOT" --record "$WORK/sweep.jsonl" -- java -jar "$jar" \
	> "$WORK/sweep.txt" 2>&1 || status=$?
expect "preview_sweep.py exit status" "$status" 0
expect "preview_sweep.py summary" "$(grep -o '^[0-9]* statements, .* answered with an error' "$WORK/sweep.txt" | tail -n 1)" \
	"$LSP_SWEEP_SUMMARY"

finish
