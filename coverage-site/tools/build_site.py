#!/usr/bin/env python3
"""Builds the vecDSL Capability Matrix, a static site, from the coverage indexes.

Reads org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index/<vendor>.json (written by coverage_index.py from each
vendor's COVERAGE.md), the vendor order of VecDSLOutputConfigurationProvider.java, model/generated/VecDSL.ecore and
VecDSL.xtext (the area of each construct), each COVERAGE.md (its header paragraphs, checked against the SHA-256 the
index records, and a tier when the header names one) and the samples, and writes coverage-site/site/:
index.html, about.html, style.css, vendors/, constructs/, samples/, badges/ and manifest.json. README.md describes the
site.

Usage: build_site.py [--workspace DIR] [--out DIR] [--date YYYY-MM-DD] [--ci-run TEXT] [--check]
  --workspace DIR  the repository root (default: two levels above this file's folder)
  --out DIR        the site folder (default: coverage-site/site of the repository)
  --date DATE      the generation date shown on every page (default: today, UTC)
  --ci-run TEXT    the CI run shown on the badges, for example "passing, run 12"
  --check          write nothing; exit 1 when an input changed after the site was built or a page was edited
"""

import argparse
import datetime
import hashlib
import html
import json
import re
import shutil
import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

BUNDLE = Path("org.ftn.vecDSL")
INDEX = BUNDLE / "src/org/ftn/vecDSL/coverage/index"
GENERATOR = BUNDLE / "src/org/ftn/vecDSL/generator"
VENDOR_LIST = GENERATOR / "VecDSLOutputConfigurationProvider.java"
ECORE = BUNDLE / "model/generated/VecDSL.ecore"
GRAMMAR = BUNDLE / "src/org/ftn/vecDSL/VecDSL.xtext"
SAMPLES = Path("org.ftn.vecDSL.tests/samples")
SITE_SOURCES = Path("coverage-site")
STYLESHEET = SITE_SOURCES / "style.css"
XSI_TYPE = "{http://www.w3.org/2001/XMLSchema-instance}type"
STATEMENT_ROOT = "Method"

STATUSES = ("native", "concealed", "unsupported", "not-applicable")
COLUMNS = STATUSES + ("pending",)
CODE = {"native": "N", "concealed": "C", "unsupported": "U", "not-applicable": "-"}
MARK = {"construct": "*", "part": "+", "none": ""}
LANGUAGES = (("python", "Python"), ("typescript", "TypeScript"), ("java", "Java"))
UNASSIGNED = "to be assigned"


class SiteError(Exception):
    pass


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def esc(text):
    return html.escape(text, quote=True)


def cell(entry):
    """The cell vecdsl coverage counts a row under: pending when the generator does not emit the construct yet."""
    return "pending" if entry["pending"] == "construct" else entry["status"]


def slug(construct):
    return re.sub(r"[^A-Za-z0-9.]+", "-", construct)


def short(url):
    """Link text of a URL: the host and the last path segment."""
    host, _, path = url.split("://", 1)[1].partition("/")
    segments = [segment for segment in re.split(r"[/#]", path) if segment]
    return host if not segments else f"{host}/{segments[0]}" if len(segments) == 1 else f"{host}/.../{segments[-1]}"


def inline(text):
    """Text with `code` spans and URLs as markup."""
    out = []
    for index, part in enumerate(re.split(r"`([^`]*)`", text)):
        if index % 2:
            out.append(f"<code>{esc(part)}</code>")
            continue
        position = 0
        for match in re.finditer(r"https?://[^\s;,)]+", part):
            out.append(esc(part[position:match.start()]))
            url = match.group(0).rstrip(".")
            out.append(f'<a href="{esc(url)}">{esc(short(url))}</a>')
            position = match.start() + len(url)
        out.append(esc(part[position:]))
    return "".join(out)


class Inputs:
    """Everything the site is built from, each file read once and hashed for the manifest."""

    def __init__(self, workspace):
        self.workspace = workspace
        self.digests = {}

    def read(self, relative):
        path = self.workspace / relative
        if not path.is_file():
            raise SiteError(f"missing input {relative.as_posix()}")
        data = path.read_bytes()
        self.digests[relative.as_posix()] = sha256(data)
        return data.decode("utf-8")


def vendor_order(inputs):
    source = inputs.read(VENDOR_LIST)
    match = re.search(r"VENDORS\s*=\s*List\.of\(([^)]*)\)", source)
    if not match:
        raise SiteError(f"{VENDOR_LIST.as_posix()}: no VENDORS list")
    return re.findall(r'"([^"]+)"', match.group(1))


def supertypes(inputs):
    root = ElementTree.fromstring(inputs.read(ECORE))
    result = {}
    for classifier in root.findall("eClassifiers"):
        if classifier.get(XSI_TYPE) == "ecore:EClass":
            result[classifier.get("name")] = [ref.split("//")[-1] for ref in classifier.get("eSuperTypes", "").split()]
    return result


def sections(inputs):
    """Rule name -> (section number, section title) from the numbered section comments of the grammar."""
    result = {}
    current = None
    for line in inputs.read(GRAMMAR).splitlines():
        heading = re.fullmatch(r"// (\d+)\. (.+)", line.strip())
        if heading:
            current = (int(heading.group(1)), heading.group(2).strip())
            continue
        rule = re.match(r"([A-Z][A-Za-z0-9]*)\s*(?:returns\s+\S+\s*)?:", line)
        if rule and current:
            result.setdefault(rule.group(1), current)
    return result


def area_class(name, parents):
    """The class that decides a construct's area: the alternative of Method it descends from, else its root class."""
    chain = [name]
    while parents.get(chain[-1]):
        if STATEMENT_ROOT in parents[chain[-1]]:
            return chain[-1]
        chain.append(parents[chain[-1]][0])
    return chain[-1]


def title(heading):
    words, _, rest = heading.partition("(")
    return words.strip().capitalize() + (f" ({rest}" if rest else "")


def header_paragraphs(text):
    lines = text.splitlines()
    start = next(i for i, line in enumerate(lines) if line.startswith("| construct |"))
    body = "\n".join(line for line in lines[1:start] if not line.startswith("#")).strip()
    return [paragraph.replace("\n", " ") for paragraph in re.split(r"\n\s*\n", body) if paragraph.strip()]


def tier(inputs, vendor):
    """The "Tier: <n>" line of the header of the vendor's COVERAGE.md (above the table), with its line number."""
    relative = GENERATOR / vendor / "COVERAGE.md"
    for number, line in enumerate(inputs.read(relative).splitlines(), 1):
        if line.startswith("| construct |"):
            break
        match = re.match(r"Tier: ([123])\b", line)
        if match:
            return match.group(1), f"{relative.as_posix()}, line {number}"
    return UNASSIGNED, None


def load(workspace):
    inputs = Inputs(workspace)
    vendors = vendor_order(inputs)
    parents = supertypes(inputs)
    rules = sections(inputs)
    data = {}
    for vendor in vendors:
        relative = INDEX / f"{vendor}.json"
        if not (workspace / relative).is_file():
            data[vendor] = None
            continue
        index = json.loads(inputs.read(relative))
        matrix = Path(index["source"])
        text = inputs.read(matrix)
        if sha256(text.encode("utf-8")) != index["sourceSha256"]:
            raise SiteError(f"{relative.as_posix()} is stale: {matrix.as_posix()} changed after coverage_index.py ran")
        if len(index["constructs"]) != index["rows"]:
            raise SiteError(f"{relative.as_posix()}: rows {index['rows']}, entries {len(index['constructs'])}")
        level, source = tier(inputs, vendor)
        data[vendor] = {
            "index": index,
            "entries": {entry["construct"]: entry for entry in index["constructs"]},
            "header": header_paragraphs(text),
            "tier": level,
            "tierSource": source,
            "counts": {column: sum(1 for entry in index["constructs"] if cell(entry) == column) for column in COLUMNS},
        }
    if not any(data.values()):
        raise SiteError("no coverage index found")
    constructs = {}
    for vendor in vendors:
        for entry in data[vendor]["index"]["constructs"] if data[vendor] else []:
            constructs.setdefault(entry["construct"], entry)
    areas = {}
    for construct, entry in constructs.items():
        owner = area_class(entry["eClass"], parents)
        if owner not in rules:
            raise SiteError(f"construct '{construct}': no grammar section for rule {owner}")
        areas.setdefault(rules[owner], []).append(construct)
    ordered = []
    for (number, heading), names in sorted(areas.items()):
        names.sort(key=lambda name: (constructs[name]["eClass"], constructs[name]["feature"] is not None, name))
        ordered.append((number, title(heading), names))
    slugs = {}
    for construct in constructs:
        key = slug(construct).lower()
        if key in slugs:
            raise SiteError(f"constructs '{construct}' and '{slugs[key]}' share a file name")
        slugs[key] = construct
    samples = {}
    for vendor in vendors:
        names = sorted(path.name for path in (workspace / SAMPLES).glob(f"{vendor}_*.vecDSL"))
        samples[vendor] = [(Path(name).stem, inputs.read(SAMPLES / name)) for name in names]
    inputs.read(STYLESHEET)
    inputs.read(SITE_SOURCES / "tools/build_site.py")
    return inputs, vendors, data, constructs, ordered, samples


class Writer:
    def __init__(self, out, date):
        self.out = out
        self.date = date
        self.files = {}

    def add(self, relative, text):
        self.files[relative] = text.encode("utf-8")

    def page(self, relative, heading, body, wide=False):
        depth = relative.count("/")
        root = "../" * depth
        nav = (f'<nav><a href="{root}index.html">Matrix</a> <a href="{root}about.html">About</a></nav>')
        self.add(relative, f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{esc(heading)} - vecDSL Capability Matrix</title>
<link rel="stylesheet" href="{root}style.css">
</head>
<body{' class="wide"' if wide else ''}>
<header>{nav}<p class="site">vecDSL Capability Matrix</p><h1>{esc(heading)}</h1></header>
<main>
{body}
</main>
<footer>Generated {self.date} by build_site.py from the coverage indexes of the vecDSL runtime bundle.</footer>
</body>
</html>
""")

    def write(self, inputs, options):
        if self.out.exists():
            shutil.rmtree(self.out)
        for relative, data in self.files.items():
            target = self.out / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
        manifest = {
            "generator": "coverage-site/tools/build_site.py",
            "date": self.date,
            "options": options,
            "inputs": dict(sorted(inputs.digests.items())),
            "outputs": {relative: sha256(data) for relative, data in sorted(self.files.items())},
        }
        (self.out / "manifest.json").write_text(json.dumps(manifest, indent=1) + "\n", encoding="utf-8", newline="\n")


def count_cells(vendor, counts):
    return "".join(f'<td class="num" data-vendor="{vendor}" data-status="{column}">{counts[column]}</td>'
                   for column in COLUMNS)


def status_definitions(vendor, paragraphs):
    """The four definitions of a COVERAGE.md header: 'Status: native (...), concealed (...), ...'."""
    pattern = r"Status: native \((.*?)\), concealed \((.*?)\), unsupported \((.*?)\), not-applicable \((.*?)\)\."
    for paragraph in paragraphs:
        match = re.search(pattern, paragraph)
        if match:
            return dict(zip(STATUSES, match.groups()))
    raise SiteError(f"{vendor}: COVERAGE.md has no 'Status: native (...), concealed (...), ...' sentence")


def tier_label(level):
    return level if level == UNASSIGNED else f"Tier {level}"


def status_text(entry, vendor):
    if entry["pending"] == "construct":
        return f"{entry['status']}, supported by {vendor}, not generated yet"
    if entry["pending"] == "part":
        return f"{entry['status']}, a part not generated yet"
    return entry["status"]


def badge(vendor, level, counts, ci_run):
    label = vendor if level == UNASSIGNED else f"{vendor} (tier {level})"
    value = " | ".join(f"{column if column != 'not-applicable' else 'n/a'} {counts[column]}" for column in COLUMNS)
    return svg([(label, "#555"), (value, "#2b5b84")] + ([(f"CI {ci_run}", "#2e7d32")] if ci_run else []))


def ci_badge(ci_run, date):
    return svg([("vecDSL CI", "#555"), (ci_run, "#2e7d32") if ci_run else (f"not run in CI; site built {date}", "#777")])


def svg(segments):
    """A flat badge: one rectangle per (text, color) segment, monospace text so that widths need no font metrics."""
    widths = [len(text) * 7 + 12 for text, _ in segments]
    total = sum(widths)
    parts = []
    x = 0
    for (text, color), width in zip(segments, widths):
        parts.append(f'<rect x="{x}" width="{width}" height="20" fill="{color}"/>'
                     f'<text x="{x + width // 2}" y="14">{esc(text)}</text>')
        x += width
    summary = ": ".join(text for text, _ in segments[:2]) + "".join(f"; {text}" for text, _ in segments[2:])
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{total}" height="20" role="img" '
            f'aria-label="{esc(summary)}"><title>{esc(summary)}</title>'
            f'<g font-family="DejaVu Sans Mono,Consolas,monospace" font-size="11.5" fill="#fff" '
            f'text-anchor="middle">{"".join(parts)}</g></svg>\n')


def build(workspace, out, date, ci_run):
    inputs, vendors, data, constructs, areas, samples = load(workspace)
    writer = Writer(out, date)
    writer.add("style.css", (workspace / STYLESHEET).read_text(encoding="utf-8"))
    present = [vendor for vendor in vendors if data[vendor]]

    legend = ('<p class="legend">Cells: <b>N</b> native, <b>C</b> concealed, <b>U</b> unsupported, <b>-</b> '
              'not-applicable; <b>*</b> supported by the vendor, not generated yet (counted as pending); <b>+</b> a '
              'part of the construct not generated yet. Definitions on the <a href="about.html">About</a> page.</p>')
    summary_rows = []
    for vendor in vendors:
        if not data[vendor]:
            summary_rows.append(f'<tr><th scope="row">{vendor}</th><td colspan="9">no coverage data</td></tr>')
            continue
        info = data[vendor]
        index = info["index"]
        summary_rows.append(
            f'<tr><th scope="row"><a href="vendors/{vendor}.html">{vendor}</a></th><td>{tier_label(info["tier"])}</td>'
            f'<td class="num" data-vendor="{vendor}" data-status="rows">{index["rows"]}</td>'
            f'{count_cells(vendor, info["counts"])}'
            f'<td><code>{esc(index["source"])}</code><br><code class="hash">{index["sourceSha256"]}</code></td>'
            f'<td><a href="badges/{vendor}.svg">{vendor}.svg</a></td></tr>')
    head_names = "".join(f'<th scope="col" class="vendor"><a href="vendors/{v}.html">{v}</a></th>' for v in present)
    head_counts = "".join(
        f'<tr class="counts"><th scope="row">{CODE.get(column, "*")} {column}</th>'
        + "".join(f'<td class="num" data-vendor="{v}" data-status="{column}">{data[v]["counts"][column]}</td>'
                  for v in present) + "</tr>"
        for column in COLUMNS)
    body_rows = []
    for number, heading, names in areas:
        body_rows.append(f'<tr class="area"><th colspan="{len(present) + 1}" id="area-{number}">{esc(heading)}</th></tr>')
        for construct in names:
            cells = []
            for vendor in present:
                entry = data[vendor]["entries"].get(construct)
                if entry is None:
                    cells.append('<td class="none">?</td>')
                    continue
                cells.append(f'<td class="{entry["status"]}" data-vendor="{vendor}" data-cell="{cell(entry)}" '
                             f'title="{vendor}: {esc(status_text(entry, vendor))}">'
                             f'<a href="vendors/{vendor}.html#{slug(construct)}">'
                             f'{CODE[entry["status"]]}{MARK[entry["pending"]]}</a></td>')
            body_rows.append(f'<tr data-construct="{esc(construct)}"><th scope="row">'
                             f'<a href="constructs/{slug(construct)}.html">{esc(construct)}</a></th>{"".join(cells)}</tr>')
    toc = " ".join(f'<a href="#area-{number}">{esc(heading)}</a>' for number, heading, _ in areas)
    writer.page("index.html", "Capability Matrix", f"""
<p>Status of every vecDSL 1.0 construct for each of the {len(present)} target systems, as each system's coverage matrix
(COVERAGE.md) states it. Rows are the constructs of the metamodel ({len(constructs)}), grouped by the grammar section
of the statement or concept they belong to; columns are the systems. Generated {date}.</p>
<h2>Systems</h2>
<table class="summary">
<thead><tr><th scope="col">system</th><th scope="col">tier</th><th scope="col">rows</th>
{''.join(f'<th scope="col">{column}</th>' for column in COLUMNS)}<th scope="col">source and SHA-256</th>
<th scope="col">badge</th></tr></thead>
<tbody>
{chr(10).join(summary_rows)}
</tbody>
</table>
<p class="note">Pending counts the rows whose construct the system supports but the generator does not emit yet; such
a row is not counted under its status. The same counts are printed by <code>vecdsl coverage</code>. A tier is shown
where the system's COVERAGE.md names one (<a href="about.html#tiers">tiers</a>); the others are to be assigned.</p>
<h2>Matrix</h2>
<p class="toc">{toc}</p>
{legend}
<table class="matrix">
<thead><tr><th scope="col">construct</th>{head_names}</tr>
{head_counts}</thead>
<tbody>
{chr(10).join(body_rows)}
</tbody>
</table>
""", wide=True)

    for vendor in present:
        info = data[vendor]
        index = info["index"]
        rows = []
        for number, heading, names in areas:
            rows.append(f'<tr class="area"><th colspan="9">{esc(heading)}</th></tr>')
            for construct in names:
                entry = info["entries"].get(construct)
                if entry is None:
                    continue
                notes = "" if entry["notes"] == entry["reason"] else inline(entry["notes"])
                rows.append(
                    f'<tr id="{slug(construct)}"><th scope="row"><a href="../constructs/{slug(construct)}.html">'
                    f'{esc(construct)}</a></th><td class="{entry["status"]}">{esc(status_text(entry, vendor))}</td>'
                    f'<td>{inline(entry["reason"])}</td>'
                    + "".join(f'<td>{inline(entry["calls"][key])}</td>' for key, _ in LANGUAGES)
                    + f'<td class="ref">{inline(entry["reference"])}</td><td>{esc(entry["test"])}</td>'
                    f'<td>{notes}</td></tr>')
        sample_links = ", ".join(f'<a href="../samples/{name}.html">{name}</a>' for name, _ in samples[vendor]) or "none"
        tier_line = tier_label(info["tier"]) + (f" ({esc(info['tierSource'])})" if info["tierSource"] else "")
        header = "\n".join(f"<p>{inline(paragraph)}</p>" for paragraph in info["header"])
        writer.page(f"vendors/{vendor}.html", vendor, f"""
<table class="summary">
<thead><tr><th scope="col">rows</th>{''.join(f'<th scope="col">{column}</th>' for column in COLUMNS)}</tr></thead>
<tbody><tr><td class="num" data-vendor="{vendor}" data-status="rows">{index['rows']}</td>{count_cells(vendor, info['counts'])}</tr></tbody>
</table>
<p><img src="../badges/{vendor}.svg" alt="coverage badge of {vendor}"></p>
<dl>
<dt>Tier</dt><dd>{tier_line}</dd>
<dt>Source</dt><dd><code>{esc(index['source'])}</code>, SHA-256 <code class="hash">{index['sourceSha256']}</code></dd>
<dt>Sample programs</dt><dd>{sample_links}</dd>
</dl>
<h2>Versions and statuses, from COVERAGE.md</h2>
{header}
<h2>Constructs</h2>
<p class="note">Notes are shown where they say more than the reason.</p>
<table class="rows">
<thead><tr><th scope="col">construct</th><th scope="col">status</th><th scope="col">reason</th>
{''.join(f'<th scope="col">{label} call</th>' for _, label in LANGUAGES)}<th scope="col">reference</th>
<th scope="col">test</th><th scope="col">notes</th></tr></thead>
<tbody>
{chr(10).join(rows)}
</tbody>
</table>
""", wide=True)
        for name, text in samples[vendor]:
            numbered = "\n".join(f'<span class="ln">{number:4d}</span> {esc(line)}'
                                 for number, line in enumerate(text.splitlines(), 1))
            writer.page(f"samples/{name}.html", f"{name}.vecDSL", f"""
<p>Sample program of <a href="../vendors/{vendor}.html">{vendor}</a>, <code>{SAMPLES.as_posix()}/{name}.vecDSL</code>
(SHA-256 <code class="hash">{sha256(text.encode('utf-8'))}</code>).</p>
<pre class="source">{numbered}</pre>
""")
        writer.add(f"badges/{vendor}.svg", badge(vendor, info["tier"], info["counts"], ci_run))

    writer.add("badges/ci.svg", ci_badge(ci_run, date))

    area_of = {construct: heading for _, heading, names in areas for construct in names}
    for construct, first in constructs.items():
        rows = []
        tally = {column: 0 for column in COLUMNS}
        for vendor in present:
            entry = data[vendor]["entries"].get(construct)
            if entry is None:
                rows.append(f'<tr><th scope="row">{vendor}</th><td colspan="2">not in the matrix</td></tr>')
                continue
            tally[cell(entry)] += 1
            rows.append(f'<tr><th scope="row"><a href="../vendors/{vendor}.html#{slug(construct)}">{vendor}</a></th>'
                        f'<td class="{entry["status"]}">{esc(status_text(entry, vendor))}</td>'
                        f'<td>{inline(entry["notes"] if entry["reason"].endswith(" ...") else entry["reason"])}</td></tr>')
        feature = f"<dt>Feature</dt><dd><code>{esc(first['feature'])}</code></dd>" if first["feature"] else ""
        writer.page(f"constructs/{slug(construct)}.html", construct, f"""
<dl>
<dt>Area</dt><dd>{esc(area_of[construct])}</dd>
<dt>Metamodel class</dt><dd><code>{esc(first['eClass'])}</code></dd>{feature}
<dt>Grammar rule</dt><dd><code>{esc(first['rule'])}</code></dd>
<dt>Across the systems</dt><dd>{', '.join(f'{column} {tally[column]}' for column in COLUMNS)}</dd>
</dl>
<table class="rows">
<thead><tr><th scope="col">system</th><th scope="col">status</th><th scope="col">reason</th></tr></thead>
<tbody>
{chr(10).join(rows)}
</tbody>
</table>
<p class="note">The reason is the index's; where the index cut it at 200 characters, the full notes of COVERAGE.md are
shown. References, calls and tests are on each system's page, linked from the system's name.</p>
""")

    definitions = status_definitions(present[0], data[present[0]]["header"])
    writer.page("about.html", "About the matrix", f"""
<h2>Statuses</h2>
<p>Every row of a system's coverage matrix has one of four statuses (the vocabulary of the vecDSL editor; the
repository README, "Coverage matrices", describes how the editor shows them). Each COVERAGE.md defines
them for its system in its header; in the words of the first one,
{present[0]}'s:</p>
<dl>
<dt>native</dt><dd>{inline(definitions['native'])}</dd>
<dt>concealed</dt><dd>{inline(definitions['concealed'])}</dd>
<dt>unsupported</dt><dd>{inline(definitions['unsupported'])}</dd>
<dt>not-applicable</dt><dd>{inline(definitions['not-applicable'])}</dd>
</dl>
<p>A row whose notes say "generation pending", or that no mapping exists yet, is a construct the system supports
(native or concealed) that the generator does not emit yet: the editor shows it as "supported by &lt;system&gt;, not
generated yet", the matrix marks it <b>*</b>, and the counts list it under pending instead of its status. A row whose
notes say that only a part is pending is marked <b>+</b> and counted under its status. Each system's page quotes its
own definitions.</p>
<h2>How the matrix is produced</h2>
<ol>
<li>Each generator's <code>COVERAGE.md</code> (<code>{GENERATOR.as_posix()}/&lt;system&gt;/</code>) lists every
construct of the metamodel with its status, the Python, TypeScript and Java call, a reference, the test that covers it
and notes.</li>
<li><code>coverage_index.py</code> turns each matrix into <code>{INDEX.as_posix()}/&lt;system&gt;.json</code>, checking
every construct id against <code>{ECORE.as_posix()}</code> and recording the SHA-256 of the matrix. The editor, the
language server and <code>vecdsl-cli</code> read these files; <code>coverage_index.py --check</code> fails when a
matrix changed without rebuilding its index.</li>
<li><code>build_site.py</code> writes these pages from the indexes. It checks each index against its matrix (the
SHA-256), takes the area of a construct from the metamodel (the statement kind or root class it descends from) and the
grammar section that defines it, and records the SHA-256 of every input and page in <code>manifest.json</code>;
<code>build_site.py --check</code> fails when an input changed after the site was built.</li>
<li>Continuous integration runs <code>coverage_index.py --check</code> and <code>build_site.py --check</code>, builds
the toolchain, runs every sample through every generator, compares the counts shown here with
<code>vecdsl coverage --json</code> and publishes the site.</li>
</ol>
<h2>What the matrix does not say</h2>
<p>A status describes the generated code and the system's documented interface at the versions each COVERAGE.md
names. What was observed on a running instance of a system is recorded in its MAPPING.md ("Server findings"), not
here.</p>
<h2 id="tiers">Tiers</h2>
<p>Tier 1: the generated code is checked in continuous integration against a local instance of the system. Tier 2:
the generated code is verified against the system at release time. Tier 3: the system is specified, with a coverage
matrix and validation only. A system's tier is the <code>Tier:</code> line in the header of its COVERAGE.md;
a system without one is shown as to be assigned.</p>
<h2>Badges</h2>
<p>Each system has a badge with its counts, <code>badges/&lt;system&gt;.svg</code>, and the toolchain has one with the
last CI run that built this site, <code>badges/ci.svg</code>, for README files. They are files of this site, not of a
badge service.</p>
""")
    return inputs, writer


def check(out, workspace):
    manifest_path = out / "manifest.json"
    if not manifest_path.is_file():
        print(f"build_site: {out} has no manifest.json; run build_site.py", file=sys.stderr)
        return 1
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    inputs, _ = build(workspace, out, manifest["date"], manifest["options"].get("ciRun"))
    problems = []
    for relative in sorted(set(manifest["inputs"]) | set(inputs.digests)):
        if manifest["inputs"].get(relative) != inputs.digests.get(relative):
            problems.append(f"input changed since the site was built: {relative}")
    for relative, digest in manifest["outputs"].items():
        page = out / relative
        if not page.is_file() or sha256(page.read_bytes()) != digest:
            problems.append(f"page missing or edited: {relative}")
    for problem in problems:
        print(problem)
    print(f"{out}: {'stale' if problems else 'up to date'} ({len(manifest['inputs'])} inputs, "
          f"{len(manifest['outputs'])} files, built {manifest['date']})")
    return 1 if problems else 0


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--workspace", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--out", type=Path)
    parser.add_argument("--date", default=datetime.datetime.now(datetime.timezone.utc).date().isoformat())
    parser.add_argument("--ci-run")
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    workspace = arguments.workspace.resolve()
    out = (arguments.out or workspace / SITE_SOURCES / "site").resolve()
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", arguments.date):
        raise SiteError(f"--date {arguments.date}: expected YYYY-MM-DD")
    if arguments.check:
        return check(out, workspace)
    inputs, writer = build(workspace, out, arguments.date, arguments.ci_run)
    writer.write(inputs, {"ciRun": arguments.ci_run} if arguments.ci_run else {})
    print(f"{out}: {len(writer.files)} files from {len(inputs.digests)} inputs, {arguments.date}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except SiteError as error:
        print(f"build_site: {error}", file=sys.stderr)
        sys.exit(2)
