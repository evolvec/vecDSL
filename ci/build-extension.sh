#!/usr/bin/env bash
# Job "build-extension": vscode-vecdsl (vscode-vecdsl/BUILD.md, "Commands"): npm ci, the grammar check, the TypeScript
# build, ESLint, the unit tests, the third-party notices and the vsix, which bundles vecdsl-lsp/target/vecdsl-lsp.jar
# (job build-lsp) or the jar named by VECDSL_LSP_JAR, into ci/.work (the vsix in vscode-vecdsl/ is left alone). The VS
# Code test is ci/extension-integration.sh.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"
cd "$ROOT/vscode-vecdsl" || exit 2

step() {
	local what=$1
	shift
	local status=0
	timeout 900 "$@" > "$WORK/extension-${what// /-}.txt" 2>&1 || status=$?
	expect "$what exit status" "$status" 0
}

step "npm ci" npm ci --no-audit --no-fund
step "check-grammar" npm run check-grammar
expect "grammar" "$(grep -o 'checked: .*' "$WORK/extension-check-grammar.txt" | tr -d '\r')" "$EXT_GRAMMAR"
step "compile" npm run compile
step "lint" npm run lint
step "test" npm test
expect "unit tests (pass, fail)" \
	"$(grep -o '^# pass [0-9]*' "$WORK/extension-test.txt" | tr -d '\r'), $(grep -o '^# fail [0-9]*' "$WORK/extension-test.txt" | tr -d '\r')" \
	"# pass $EXT_TESTS, # fail 0"
step "check-notices" npm run check-notices
expect "notices" "$(grep -c 'notices: up to date' "$WORK/extension-check-notices.txt")" 1
vsix=$WORK/vscode-vecdsl-$(node -p "require('./package.json').version").vsix
rm -f "$vsix"
step "vsce package" npx --no-install vsce package --out "$vsix"
expect "vsix files" "$(grep -o '([0-9]* files' "$WORK/extension-vsce-package.txt" | tr -d '(' | tr -d '\r')" "$EXT_VSIX_FILES files"
expect "vsix written" "$([ -f "$vsix" ] && echo yes || echo no)" yes

finish
