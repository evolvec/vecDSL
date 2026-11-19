#!/usr/bin/env bash
# Runs the CI steps locally in the order of .github/workflows/ci.yml, each with a time limit, and prints the exit status
# and duration of each; the output of step <name> is in ci/.work/<name>.log. With step names as arguments, runs only
# those (for example: ci/run_all.sh samples site). CI_INTEGRATION=1 adds the optional VS Code test at the end.
# Exit 0 when every step passed.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

steps=("$@")
if [ ${#steps[@]} -eq 0 ]; then
	steps=(coverage-index build-cli build-lsp tests build-extension samples site)
	[ "${CI_INTEGRATION:-0}" = 1 ] && steps+=(extension-integration)
fi
summary=$WORK/run_all.txt
: > "$summary"
for step in "${steps[@]}"; do
	[ -f "$ROOT/ci/$step.sh" ] || { echo "unknown step: $step" >&2; exit 2; }
	echo "== $step"
	started=$(date +%s)
	status=0
	timeout 5400 bash "$ROOT/ci/$step.sh" > "$WORK/$step.log" 2>&1 || status=$?
	seconds=$(($(date +%s) - started))
	grep -E '^(ok|FAIL) ' "$WORK/$step.log" | sed 's/^/   /' || true
	printf '%-22s exit %-3s %4d s\n' "$step" "$status" "$seconds" | tee -a "$summary"
	[ "$status" -eq 0 ] || FAILURES=$((FAILURES + 1))
done
echo
cat "$summary"
finish
