#!/usr/bin/env python3
"""Builds the coverage index from the vendor coverage matrices.

Reads every org.ftn.vecDSL/src/org/ftn/vecDSL/generator/<vendor>/COVERAGE.md and writes
org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index/<vendor>.json, one entry per table row in table
order. CoverageIndex.fromClasspath() reads them as class resources of the runtime bundle for the
vendors of VecDSLOutputConfigurationProvider.VENDORS. Fails on a missing or extra column, an
unknown status or a construct id that does not name a class or a structural feature of
model/generated/VecDSL.ecore. After writing, press F5 on org.ftn.vecDSL in Eclipse so that the
Java builder copies the files to bin.

Usage: coverage_index.py [--workspace DIR] [--check]
  --workspace DIR  the Eclipse workspace (default: two levels above this file)
  --check          write nothing; exit 1 if any index file differs from the matrices
"""

import argparse
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

HEADER = ["construct", "grammar rule", "status", "Python call", "TypeScript call", "Java call",
          "reference", "test name", "notes"]
STATUSES = {"native", "concealed", "unsupported", "not-applicable"}
CLASS_KINDS = {"Method", "Concept", "AlterOperation", "ExtensionItem"}
GENERATOR = Path("org.ftn.vecDSL/src/org/ftn/vecDSL/generator")
ECORE = Path("org.ftn.vecDSL/model/generated/VecDSL.ecore")
TARGET = Path("org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index")
ECORE_NS = "{http://www.eclipse.org/emf/2002/Ecore}"
XSI_TYPE = "{http://www.w3.org/2001/XMLSchema-instance}type"
REASON_LIMIT = 200


class CoverageError(Exception):
    pass


def metamodel(path):
    """Class name -> (supertype names, own feature names) from the generated Ecore model."""
    classes = {}
    for classifier in ElementTree.parse(path).getroot().findall("eClassifiers"):
        if classifier.get(XSI_TYPE) != "ecore:EClass":
            continue
        supertypes = [ref.split("//")[-1] for ref in classifier.get("eSuperTypes", "").split()]
        features = {feature.get("name") for feature in classifier.findall("eStructuralFeatures")}
        classes[classifier.get("name")] = (supertypes, features)
    if not classes:
        raise CoverageError(f"{path}: no classes found")
    return classes


def all_features(classes, name, seen=None):
    seen = seen if seen is not None else set()
    if name in seen or name not in classes:
        return set()
    seen.add(name)
    supertypes, features = classes[name]
    result = set(features)
    for supertype in supertypes:
        result |= all_features(classes, supertype, seen)
    return result


def resolve(construct, classes):
    """(class, feature or None) named by a construct id, or a CoverageError."""
    parts = construct.split(" ")
    if len(parts) == 2 and parts[0] in CLASS_KINDS:
        if parts[1] not in classes:
            raise CoverageError(f"construct '{construct}' names no class of the metamodel")
        return parts[1], None
    match = re.fullmatch(r"([A-Za-z]+)\.([A-Za-z]+)", construct)
    if not match:
        raise CoverageError(f"construct '{construct}' is neither '<Kind> <Class>' nor '<Class>.<feature>'")
    owner, feature = match.groups()
    if owner not in classes or feature not in all_features(classes, owner):
        raise CoverageError(f"construct '{construct}' names no feature of the metamodel")
    return owner, feature


def cells(line):
    """Cells of a Markdown table row; '\\|' is a literal bar."""
    body = line.strip()
    if not (body.startswith("|") and body.endswith("|")):
        raise CoverageError(f"not a table row: {line.strip()}")
    parts = re.split(r"(?<!\\)\|", body[1:-1])
    return [part.strip().replace("\\|", "|") for part in parts]


def pending(notes):
    """'construct' when the generator does not emit the construct yet, 'part' when only a part of it waits."""
    if notes.startswith("generation pending") or "mapping yet" in notes:
        return "construct"
    return "part" if "generation pending" in notes else "none"


def reason(notes):
    text = re.sub(r"^message: [^;]*; ", "", notes)
    if len(text) <= REASON_LIMIT:
        return text
    cut = text.rfind("; ", 0, REASON_LIMIT)
    return text[:cut if cut > 0 else REASON_LIMIT].rstrip() + " ..."


def calls(row):
    result = {}
    previous = ""
    for language, cell in zip(("python", "typescript", "java"), row[3:6]):
        if cell == "same":
            cell = previous
        elif cell.startswith("same "):
            cell = f"{previous} ({cell})"
        result[language] = cell
        previous = cell
    return result


def index(vendor, matrix, classes):
    text = matrix.read_text(encoding="utf-8")
    lines = text.splitlines()
    try:
        start = next(i for i, line in enumerate(lines) if line.startswith("| construct |"))
    except StopIteration:
        raise CoverageError(f"{matrix}: no coverage table") from None
    header = cells(lines[start])
    if header != HEADER:
        missing = [column for column in HEADER if column not in header]
        raise CoverageError(f"{matrix}: table columns {header}, expected {HEADER}"
                            + (f" (missing: {', '.join(missing)})" if missing else ""))
    entries = []
    seen = set()
    for number, line in enumerate(lines[start + 2:], start + 3):
        if not line.startswith("|"):
            break
        row = cells(line)
        if len(row) != len(HEADER):
            raise CoverageError(f"{matrix}:{number}: {len(row)} columns, expected {len(HEADER)}")
        construct, rule, status = row[0], row[1], row[2]
        if status not in STATUSES:
            raise CoverageError(f"{matrix}:{number}: unknown status '{status}' for {construct}")
        if construct in seen:
            raise CoverageError(f"{matrix}:{number}: construct '{construct}' listed twice")
        seen.add(construct)
        owner, feature = resolve(construct, classes)
        notes = row[8]
        entries.append({
            "construct": construct,
            "eClass": owner,
            "feature": feature,
            "rule": rule,
            "status": status,
            "pending": pending(notes),
            "reason": reason(notes),
            "calls": calls(row),
            "reference": row[6],
            "test": row[7],
            "notes": notes,
        })
    if not entries:
        raise CoverageError(f"{matrix}: the coverage table has no rows")
    return {
        "vendor": vendor,
        "source": str(GENERATOR / vendor / "COVERAGE.md").replace("\\", "/"),
        "sourceSha256": hashlib.sha256(text.encode("utf-8")).hexdigest(),
        "rows": len(entries),
        "constructs": entries,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--workspace", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    workspace = arguments.workspace
    classes = metamodel(workspace / ECORE)
    matrices = sorted((workspace / GENERATOR).glob("*/COVERAGE.md"))
    if not matrices:
        raise CoverageError(f"no COVERAGE.md under {workspace / GENERATOR}")
    stale = []
    for matrix in matrices:
        vendor = matrix.parent.name
        rendered = json.dumps(index(vendor, matrix, classes), indent=1, ensure_ascii=False) + "\n"
        target = workspace / TARGET / f"{vendor}.json"
        counts = {}
        for entry in json.loads(rendered)["constructs"]:
            key = entry["status"] + ("+pending" if entry["pending"] == "construct" else "")
            counts[key] = counts.get(key, 0) + 1
        summary = ", ".join(f"{key} {value}" for key, value in sorted(counts.items()))
        if arguments.check:
            current = target.read_text(encoding="utf-8") if target.exists() else None
            if current != rendered:
                stale.append(vendor)
            print(f"{vendor}: {'stale' if current != rendered else 'up to date'} ({summary})")
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(rendered, encoding="utf-8", newline="\n")
            print(f"{vendor}: {target.relative_to(workspace)} ({summary})")
    return 1 if stale else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except CoverageError as error:
        print(f"coverage_index: {error}", file=sys.stderr)
        sys.exit(2)
