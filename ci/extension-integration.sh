#!/usr/bin/env bash
# Job "extension-integration" (optional): vscode-vecdsl's VS Code test (npm run test:integration, eight numbered checks
# in two windows) after build-extension. It needs a VS Code: VSCODE_EXECUTABLE names an installed one (no other VS Code
# window may be open), else @vscode/test-electron downloads VS Code 1.91.0; on Linux without a display it runs under
# xvfb-run.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"
cd "$ROOT/vscode-vecdsl" || exit 2

runner=()
if [ "$(uname -s)" = Linux ] && [ -z "${DISPLAY:-}" ]; then runner=(xvfb-run -a); fi
status=0
timeout 1200 "${runner[@]}" npm run test:integration > "$WORK/extension-integration.txt" 2>&1 || status=$?
grep -E '^ *[1-8]\. |^window with' "$WORK/extension-integration.txt" || true
expect "test:integration exit status" "$status" 0
expect "numbered checks" "$(grep -cE '^ *[1-8]\. ' "$WORK/extension-integration.txt")" 8

finish
