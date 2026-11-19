"""Checks `vecdsl explain --json` against the COVERAGE.md rows it reports: for every construct of the explained
statements and every target, the status (or the pending label), the three calls and the reference must equal the
cells of the vendor's COVERAGE.md, read here independently of coverage_index.py.

usage: python check_explain.py <workspace> <explain.json>
exit:  0 when every reported row matches its COVERAGE.md row, 1 otherwise
"""
import json
import sys
from pathlib import Path

workspace, document = Path(sys.argv[1]), json.loads(Path(sys.argv[2]).read_text(encoding="utf-8"))


def rows(vendor):
    table = {}
    path = workspace / "org.ftn.vecDSL/src/org/ftn/vecDSL/generator" / vendor / "COVERAGE.md"
    header = None
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.startswith("| "):
            continue
        cells = [cell.strip().replace("\\|", "|") for cell in line.strip()[1:-1].replace("\\|", "\0").split("|")]
        cells = [cell.replace("\0", "|") for cell in cells]
        if cells[0] == "construct":
            header = cells
            continue
        if header and not set(cells[0]) <= set("-"):
            table[cells[0]] = dict(zip(header, cells))
    return table


tables, checked, failures = {}, 0, []
for statement in document["statements"]:
    for construct in statement["constructs"]:
        for vendor in construct["vendors"]:
            if "row" not in vendor:
                continue
            row = tables.setdefault(vendor["vendor"], rows(vendor["vendor"]))[vendor["row"]]
            expected = {"status": row["status"], "python": row["Python call"], "typescript": row["TypeScript call"],
                        "java": row["Java call"], "reference": row["reference"]}
            for key, value in expected.items():
                if vendor[key] != value:
                    failures.append(f"{statement['label']} {construct['construct']} {vendor['vendor']} {key}: "
                                    f"{vendor[key]!r} != {value!r}")
            checked += 1
print(f"{checked} rows checked against COVERAGE.md, {len(failures)} mismatches")
for failure in failures:
    print("  " + failure)
sys.exit(1 if failures else 0)
