# Sourced by every ci/ script: strict mode, the workspace root, the expected numbers, the tools and the checks.
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
# shellcheck source=expected.env
source "$ROOT/ci/expected.env"
WORK=${CI_WORK:-$ROOT/ci/.work}
mkdir -p "$WORK"
FAILURES=0

# A Python 3.10 or later; on Windows "python3" can be the Microsoft Store stub, so each candidate is run once.
PYTHON=${PYTHON:-}
if [ -z "$PYTHON" ]; then
	for candidate in python3 python; do
		if "$candidate" -c 'import sys; sys.exit(sys.version_info < (3, 10))' > /dev/null 2>&1; then
			PYTHON=$candidate
			break
		fi
	done
fi
[ -n "$PYTHON" ] || { echo "no Python 3.10 or later on the PATH" >&2; exit 2; }

# A path as the native tools of the platform read it inside a string (Git Bash: C:/...; elsewhere unchanged).
native() {
	if command -v cygpath > /dev/null 2>&1; then cygpath -m "$1"; else printf '%s\n' "$1"; fi
}

# The vecdsl-cli command line: java -jar vecdsl-cli/target/vecdsl-cli.jar, with the option of the launchers in
# vecdsl-cli/bin from Java 24 on (JEP 498); a plain command, so that Python scripts can run it
# on Windows too. VECDSL overrides it (words separated by spaces).
if [ -n "${VECDSL:-}" ]; then
	read -r -a VECDSL_CMD <<< "$VECDSL"
else
	java_major=$(java -version 2>&1 | sed -n 's/.* version "\([0-9]*\)[."].*/\1/p' | head -n 1)
	VECDSL_CMD=(java)
	[ "${java_major:-0}" -ge 24 ] && VECDSL_CMD+=(--sun-misc-unsafe-memory-access=allow)
	VECDSL_CMD+=(-jar "$ROOT/vecdsl-cli/target/vecdsl-cli.jar")
fi
vecdsl() { "${VECDSL_CMD[@]}" "$@"; }

# expect <what> <actual> <expected>: one line per check; a mismatch is counted and reported by finish.
expect() {
	if [ "$2" = "$3" ]; then
		echo "ok    $1: $2"
	else
		echo "FAIL  $1: $2 (expected $3)"
		FAILURES=$((FAILURES + 1))
	fi
}

finish() {
	if [ "$FAILURES" -ne 0 ]; then
		echo "$(basename "$0"): $FAILURES check(s) failed"
		exit 1
	fi
	echo "$(basename "$0"): all checks passed"
}
