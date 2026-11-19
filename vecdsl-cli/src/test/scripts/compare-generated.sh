#!/usr/bin/env bash
# Generates every sample with `vecdsl gen` and compares the files byte for byte with org.ftn.vecDSL.tests/generated,
# where the test project wrote them: <vendor>_demo.vecDSL -> generated/<vendor>, <vendor>_full.vecDSL ->
# generated/<vendor>-full. Prints one line per sample and the files that differ; files the test project writes
# beside the generator's output (books.jsonl, run and server logs, build output) are counted as "extra", not compared.
#
# usage: compare-generated.sh <workspace> [vecdsl command...]      (default command: java -jar target/vecdsl-cli.jar)
# exit:  0 when every generated file is byte-identical and none is missing, 1 otherwise
set -u
workspace=$1; shift
if [ $# -eq 0 ]; then set -- java -jar "$(dirname "$0")/../../../target/vecdsl-cli.jar"; fi
samples=$workspace/org.ftn.vecDSL.tests/samples
generated=$workspace/org.ftn.vecDSL.tests/generated
out=$(mktemp -d)
status=0
for sample in "$samples"/*_demo.vecDSL "$samples"/*_full.vecDSL; do
	name=$(basename "$sample" .vecDSL)
	vendor=${name%_*}
	folder=$vendor; [ "${name##*_}" = full ] && folder=$vendor-full
	"$@" gen "$sample" --target "$vendor" --lang all -o "$out/$name" > "$out/$name.log" 2>&1 || { echo "$name: gen failed"; cat "$out/$name.log"; status=1; continue; }
	same=0; differ=0; missing=0
	while IFS= read -r file; do
		rel=${file#"$out/$name/$vendor/"}
		if [ ! -f "$generated/$folder/$rel" ]; then missing=$((missing+1)); echo "  missing in generated/$folder: $rel"
		elif cmp -s "$file" "$generated/$folder/$rel"; then same=$((same+1))
		else differ=$((differ+1)); echo "  differs: generated/$folder/$rel"; fi
	done < <(find "$out/$name/$vendor" -type f | sort)
	extra=$(cd "$generated/$folder" && find . -type f \
		-not -path '*/node_modules/*' -not -path '*/target/*' -not -path '*/__pycache__/*' -not -path '*/dist/*' \
		| sed 's|^\./||' | while IFS= read -r rel; do [ -f "$out/$name/$vendor/$rel" ] || echo "$rel"; done | wc -l)
	echo "$name -> generated/$folder: $same identical, $differ different, $missing missing, $extra extra"
	[ $differ -eq 0 ] && [ $missing -eq 0 ] || status=1
done
rm -rf "$out"
exit $status
