#!/usr/bin/env python3
"""Seed of the vecDSL conformance corpus (spec/conformance/), the start of a technology compatibility kit.

One folder per program: program.vecDSL (a copy), ir.json (the IR, valid programs only) and diagnostics.json (an empty
diagnostics document for a valid program, the expected diagnostics for a rejected one), plus MANIFEST.md with the md5 of
every file and the time the samples were taken. The IR and diagnostics documents come from the Java exporter:

  java ... org.ftn.vecDSL.ir.VecDSLIrMain --export <samples dir> <exported dir>
  java ... org.ftn.vecDSL.ir.VecDSLIrMain --export spec/negative <exported dir>

Usage: conformance.py --exported DIR [--samples DIR] [--negative DIR] [--samples-time TEXT] [--out DIR]
  --samples        default org.ftn.vecDSL.tests/samples
  --samples-time   the time the samples were taken, for MANIFEST.md (default: now, UTC)
"""

import argparse
import hashlib
import json
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

WORKSPACE = Path(__file__).resolve().parents[2]
EMPTY = '{\n  "vecdsl-ir": "1.0",\n  "diagnostics": []\n}\n'


def md5(path):
    return hashlib.md5(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--exported", type=Path, required=True)
    parser.add_argument("--samples", type=Path, default=WORKSPACE / "org.ftn.vecDSL.tests/samples")
    parser.add_argument("--negative", type=Path, default=WORKSPACE / "spec/negative")
    parser.add_argument("--samples-time", default=datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
    parser.add_argument("--out", type=Path, default=WORKSPACE / "spec/conformance")
    args = parser.parse_args()
    rows, problems = [], []
    for kind, source in (("sample", args.samples), ("rejected", args.negative)):
        for program in sorted(source.glob("*.vecDSL")):
            name = program.stem
            exported = args.exported / f"{name}.json"
            if not exported.exists():
                problems.append(f"{exported} is missing; run VecDSLIrMain --export on {source}")
                continue
            document = json.loads(exported.read_text(encoding="utf-8"))
            valid = "program" in document
            if valid != (kind == "sample"):
                problems.append(f"{name}: a {kind} program was {'accepted' if valid else 'rejected'}")
            folder = args.out / name
            folder.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(program, folder / "program.vecDSL")
            if valid:
                shutil.copyfile(exported, folder / "ir.json")
                (folder / "diagnostics.json").write_text(EMPTY, encoding="utf-8", newline="\n")
            else:
                (folder / "ir.json").unlink(missing_ok=True)
                shutil.copyfile(exported, folder / "diagnostics.json")
            for file in sorted(folder.iterdir()):
                rows.append((kind, name, file.name, md5(file), file.stat().st_size))
    if problems:
        print("\n".join(problems), file=sys.stderr)
        return 1
    lines = ["# vecDSL conformance corpus, seed (vecDSL 1.0)", "",
             f"Built by `spec/tools/conformance.py` from the samples of {args.samples_time} and the rejected programs of "
             "`spec/negative/`. Each folder holds the program, its IR (valid programs) and its diagnostics document "
             "(empty for a valid program). Expected native calls and results per vendor are not part of the seed.", "",
             f"Programs: {len({r[1] for r in rows if r[0] == 'sample'})} samples, "
             f"{len({r[1] for r in rows if r[0] == 'rejected'})} rejected.", "",
             "| kind | program | file | md5 | bytes |", "|---|---|---|---|---|"]
    lines += [f"| {kind} | {name} | {file} | {digest} | {size} |" for kind, name, file, digest, size in rows]
    (args.out / "MANIFEST.md").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    print(f"{args.out}: {len(rows)} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
