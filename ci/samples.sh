#!/usr/bin/env bash
# Job "samples": every sample program through vecdsl-cli, without any vendor server (the generators are headless).
#   1. check of all samples: the summary line of expected.env
#   2. gen of each sample for its own target in every language: the file count per vendor, and every file present in
#      and byte-identical to org.ftn.vecDSL.tests/generated/<vendor>[-full], which the tests job writes
#   3. gen of each sample without its targets line through every generator in every language: exit 0, a folder per
#      vendor, the total file count; the pairs of GEN_ALL_KNOWN_FAILURES are generated apart and must still fail with
#      an internal error (exit 3), so that a fix shows up as a change to expected.env
#   4. ir --check: every sample round-trips through the IR
#   5. fmt --write on a copy, then fmt --check on it (nothing to change) and a second fmt --write (no file changes)
# The samples themselves are only read. Uses vecdsl-cli/target/vecdsl-cli.jar (job build-cli), or the command in VECDSL.
# shellcheck source=lib.sh
source "$(dirname "$0")/lib.sh"

SAMPLES_DIR=$ROOT/org.ftn.vecDSL.tests/samples
REFERENCE=$ROOT/org.ftn.vecDSL.tests/generated
OUT=$WORK/samples
rm -rf "$OUT"
mkdir -p "$OUT/own" "$OUT/untargeted" "$OUT/all" "$OUT/known" "$OUT/fmt"
samples=("$SAMPLES_DIR"/*.vecDSL)
expect "sample programs" "${#samples[@]}" "$SAMPLES"

status=0
vecdsl check "$SAMPLES_DIR" > "$OUT/check.txt" 2>&1 || status=$?
expect "check exit status" "$status" 0
expect "check summary" "$(tail -n 1 "$OUT/check.txt" | tr -d '\r')" "$CHECK_SUMMARY"

declare -A per_vendor
for pair in $GEN_FILES_PER_SAMPLE; do per_vendor[${pair%=*}]=${pair#*=}; done
total=0
for sample in "${samples[@]}"; do
	name=$(basename "$sample" .vecDSL)
	vendor=${name%_*}
	folder=$vendor
	[ "${name##*_}" = full ] && folder=$vendor-full
	status=0
	vecdsl gen "$sample" --target "$vendor" --lang all -o "$OUT/own/$name" > "$OUT/own/$name.log" 2>&1 || status=$?
	files=0 missing=0 differ=0
	if [ "$status" -eq 0 ]; then
		while IFS= read -r file; do
			files=$((files + 1))
			relative=${file#"$OUT/own/$name/$vendor/"}
			if [ ! -f "$REFERENCE/$folder/$relative" ]; then
				missing=$((missing + 1))
				echo "      not in generated/$folder: $relative"
			elif ! cmp -s "$file" "$REFERENCE/$folder/$relative"; then
				differ=$((differ + 1))
				echo "      differs from generated/$folder: $relative"
			fi
		done < <(find "$OUT/own/$name/$vendor" -type f | sort)
	fi
	total=$((total + files))
	expect "gen $name --target $vendor (exit, files, missing, different)" "$status $files $missing $differ" \
		"0 ${per_vendor[$vendor]:-?} 0 0"
done
expect "gen for the own target, files of all samples" "$total" "$GEN_OWN_FILES"

read -r -a all_vendors <<< "$VENDORS"
total=0
for sample in "${samples[@]}"; do
	name=$(basename "$sample" .vecDSL)
	sed -E '/^targets \[[^]]*\]\r?$/d' "$sample" > "$OUT/untargeted/$name.vecDSL"
	targets=() known=()
	for vendor in "${all_vendors[@]}"; do
		if [[ " $GEN_ALL_KNOWN_FAILURES " == *" $name:$vendor "* ]]; then known+=("$vendor"); else targets+=("$vendor"); fi
	done
	status=0
	vecdsl gen "$OUT/untargeted/$name.vecDSL" --target "$(IFS=,; echo "${targets[*]}")" --lang all -o "$OUT/all/$name" \
		> "$OUT/all/$name.log" 2>&1 || status=$?
	folders=$(find "$OUT/all/$name" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
	files=$(find "$OUT/all/$name" -type f | wc -l | tr -d ' ')
	total=$((total + files))
	expect "gen $name without targets through ${#targets[@]} generators (exit, vendor folders)" "$status $folders" \
		"0 ${#targets[@]}"
	for vendor in "${known[@]}"; do
		status=0
		vecdsl gen "$OUT/untargeted/$name.vecDSL" --target "$vendor" --lang all -o "$OUT/known/$name" \
			> "$OUT/known/$name-$vendor.log" 2>&1 || status=$?
		expect "gen $name without targets through $vendor, known failure (exit)" "$status" 3
	done
done
expect "gen through every generator, files of all samples" "$total" "$GEN_ALL_FILES"

status=0
vecdsl ir --check "$SAMPLES_DIR" > "$OUT/ir.txt" 2>&1 || status=$?
expect "ir --check exit status" "$status" 0
expect "ir --check summary" "$(tail -n 1 "$OUT/ir.txt" | tr -d '\r')" "$IR_CHECK_SUMMARY"

cp "${samples[@]}" "$OUT/fmt/"
status=0
vecdsl fmt --write "$OUT/fmt" > "$OUT/fmt-write.txt" 2>&1 || status=$?
expect "fmt --write exit status" "$status" 0
status=0
vecdsl fmt --check "$OUT/fmt" > "$OUT/fmt-check.txt" 2>&1 || status=$?
expect "fmt --check after fmt --write (exit, lines)" "$status $(wc -l < "$OUT/fmt-check.txt" | tr -d ' ')" "0 0"
before=$(cat "$OUT"/fmt/*.vecDSL | cksum)
vecdsl fmt --write "$OUT/fmt" > /dev/null 2>&1
expect "second fmt --write changes nothing" "$(cat "$OUT"/fmt/*.vecDSL | cksum)" "$before"

finish
