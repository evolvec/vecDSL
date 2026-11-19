#!/usr/bin/env python3
"""Checks that every number and cell the Capability Matrix shows equals what vecdsl-cli's coverage command prints.

Runs `<vecdsl> coverage --json` (the summary per vendor) and `<vecdsl> coverage --status <s> --json` for each of the
five cells (native, concealed, unsupported, not-applicable, pending), then reads the built site: every element with
data-vendor and data-status (counts in index.html, its matrix header and the vendor pages), every matrix cell
(data-vendor and data-cell in the row of data-construct) and the counts in each badge. Exit 0 when all agree, 1 when
any differs.

Usage: check_agreement.py [--site DIR] -- <vecdsl command...>
  --site DIR  the built site (default: coverage-site/site beside this file's folder)
"""

import argparse
import json
import re
import subprocess
import sys
from html.parser import HTMLParser
from pathlib import Path

CELLS = ("native", "concealed", "unsupported", "not-applicable", "pending")
TIMEOUT = 300


class Shown(HTMLParser):
    """Collects the counts (vendor, status) -> values and the cells (vendor, construct) -> cell of one page."""

    def __init__(self, page, counts, cells):
        super().__init__()
        self.page = page
        self.counts = counts
        self.cells = cells
        self.construct = None
        self.capture = None

    def handle_starttag(self, tag, attributes):
        attributes = dict(attributes)
        if tag == "tr":
            self.construct = attributes.get("data-construct")
        if "data-status" in attributes:
            self.capture = (attributes["data-vendor"], attributes["data-status"])
            self.counts.setdefault(self.capture, []).append([self.page, ""])
        if "data-cell" in attributes:
            key = (attributes["data-vendor"], self.construct)
            if key in self.cells:
                raise ValueError(f"{self.page}: two cells for {key}")
            self.cells[key] = attributes["data-cell"]

    def handle_data(self, data):
        if self.capture:
            self.counts[self.capture][-1][1] += data

    def handle_endtag(self, tag):
        if tag in ("td", "th"):
            self.capture = None


def run(command):
    result = subprocess.run(command, capture_output=True, text=True, encoding="utf-8", timeout=TIMEOUT)
    if result.returncode not in (0, 1):
        raise RuntimeError(f"{' '.join(command)}: exit {result.returncode}\n{result.stderr}")
    return json.loads(result.stdout)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--site", type=Path, default=Path(__file__).resolve().parents[1] / "site")
    parser.add_argument("vecdsl", nargs=argparse.REMAINDER)
    arguments = parser.parse_args()
    command = arguments.vecdsl[1:] if arguments.vecdsl[:1] == ["--"] else arguments.vecdsl
    if not command:
        parser.error("the vecdsl command is missing")
    site = arguments.site

    summary = {row["vendor"]: row for row in run(command + ["coverage", "--json"]) if row["hasData"]}
    expected_cells = {}
    for column in CELLS:
        for row in run(command + ["coverage", "--status", column, "--json"]):
            expected_cells[(row["vendor"], row["construct"])] = row["cell"]

    counts, cells = {}, {}
    pages = sorted(site.glob("*.html")) + sorted(site.glob("vendors/*.html"))
    for page in pages:
        Shown(page.relative_to(site).as_posix(), counts, cells).feed(page.read_text(encoding="utf-8"))

    problems = []
    for (vendor, status), shown in sorted(counts.items()):
        expected = summary.get(vendor, {}).get(status)
        for page, value in shown:
            if str(expected) != value.strip():
                problems.append(f"{page}: {vendor} {status} shows {value.strip()!r}, vecdsl coverage {expected}")
    for vendor, row in summary.items():
        for status in ("rows",) + CELLS:
            if not any(page.startswith("vendors/") for page, _ in counts.get((vendor, status), [])):
                problems.append(f"vendors/{vendor}.html: no {status} count")
        badge = site / "badges" / f"{vendor}.svg"
        text = badge.read_text(encoding="utf-8") if badge.is_file() else ""
        label = re.search(r"<title>([^<]*)</title>", text)
        for status in CELLS:
            name = "n/a" if status == "not-applicable" else status
            found = re.search(rf"\b{re.escape(name)} (\d+)", label.group(1)) if label else None
            if not found or int(found.group(1)) != row[status]:
                problems.append(f"badges/{vendor}.svg: {status} {found.group(1) if found else 'missing'}, "
                                f"vecdsl coverage {row[status]}")
    if set(cells) != set(expected_cells):
        missing = sorted(set(expected_cells) - set(cells))
        extra = sorted(set(cells) - set(expected_cells))
        problems.append(f"index.html: {len(missing)} cells missing, {len(extra)} cells not in vecdsl coverage "
                        f"(first: {(missing + extra)[0]})")
    differing = [key for key in expected_cells if key in cells and cells[key] != expected_cells[key]]
    for vendor, construct in differing[:20]:
        problems.append(f"index.html: {vendor} {construct} shows {cells[(vendor, construct)]}, "
                        f"vecdsl coverage {expected_cells[(vendor, construct)]}")

    for problem in problems:
        print(problem)
    places = sum(len(shown) for shown in counts.values())
    print(f"{len(summary)} vendors: {places} counts on {len(pages)} pages, {len(summary) * len(CELLS)} badge counts and "
          f"{len(cells)} matrix cells compared with vecdsl coverage: "
          f"{'all equal' if not problems else f'{len(problems)} differences'}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
