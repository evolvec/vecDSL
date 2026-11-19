#!/usr/bin/env python3
"""Prints the test runs per class of a surefire or failsafe report folder, sorted by class: "A=n B=m", and on a second
line "failures f errors e skipped s". Usage: junit_counts.py <reports folder>"""

import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path


def main():
    folder = Path(sys.argv[1])
    runs = {}
    totals = {"failures": 0, "errors": 0, "skipped": 0}
    for report in sorted(folder.glob("TEST-*.xml")):
        suite = ElementTree.parse(report).getroot()
        name = suite.get("name").rsplit(".", 1)[-1]
        runs[name] = runs.get(name, 0) + int(suite.get("tests"))
        for key in totals:
            totals[key] += int(suite.get(key, "0"))
    if not runs:
        print(f"no TEST-*.xml in {folder}", file=sys.stderr)
        return 1
    print(" ".join(f"{name}={count}" for name, count in sorted(runs.items())))
    print(" ".join(f"{key} {value}" for key, value in totals.items()))
    return 0


if __name__ == "__main__":
    sys.exit(main())
