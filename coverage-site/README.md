# vecDSL Capability Matrix

A static site generated from the coverage matrices of the 14 targets. `tools/build_site.py` writes it into `site/`;
`tools/check_agreement.py` compares it with `vecdsl coverage`. Python 3.10 or later, standard library only, both clean
under `python -I`. Run from the repository root:

```
python3 -I coverage-site/tools/build_site.py                 # writes coverage-site/site/ (516 files, about 7 MB)
python3 -I coverage-site/tools/build_site.py --check         # exit 1 when an input changed after the build
python3 -I coverage-site/tools/check_agreement.py -- java -jar vecdsl-cli/target/vecdsl-cli.jar
```

Open `coverage-site/site/index.html`. The site is not committed: it changes with every matrix change, so CI builds it
(job `site`) and uploads it, and the `coverage-index` job builds it into its work folder so that a broken input fails
early.

## Inputs

| input | used for |
|---|---|
| `org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index/<vendor>.json` | every row: construct, EClass, feature, rule, status, pending, reason, calls, reference, test, notes, and the source's SHA-256 |
| `generator/VecDSLOutputConfigurationProvider.java` | the vendors and their order (`VENDORS`), as `vecdsl coverage` lists them |
| `model/generated/VecDSL.ecore`, `VecDSL.xtext` | the area of each construct |
| `generator/<vendor>/COVERAGE.md` | checked against the index's SHA-256; its header paragraphs (versions, status definitions) on the vendor page; the `Tier: <n>` line of the header, when there is one |
| `org.ftn.vecDSL.tests/samples/<vendor>_*.vecDSL` | the sample pages |
| `coverage-site/style.css`, `tools/build_site.py` | the stylesheet, and the script itself (a changed script makes the site stale) |

The input paths are constants at the top of `build_site.py`.

## What the pages show

- **Areas.** Constructs are grouped by the grammar section of their class: the supertype chain of the EClass in
  VecDSL.ecore ends at an alternative of `Method` or at a root class, and that class's rule sits under a numbered
  section comment of VecDSL.xtext: 2 "Schema definition statements (DDL)", 3 "Data manipulation statements (DML)",
  4 "Operational statements", 5 "Query statements", 9 "Structural concepts", 17 "Extension mechanism". Feature rows
  (`VectorSearch.highlighted`) follow their class row. A construct whose class has no grammar section stops the build.
- **Statuses.** Each COVERAGE.md defines the four statuses (native, concealed, unsupported, not-applicable) in its
  header; the about page quotes the first vendor's and each vendor page shows its own. A row whose generation is pending
  (`pending: construct` in the index, "generation pending" or "no mapping yet" in the notes) is counted under pending
  and not under its status, as `vecdsl coverage` counts it; `pending: part` keeps its status and is only marked.
- **Cells.** N, C, U and "-" for not-applicable, `*` for a pending construct, `+` for a pending part; each cell has the
  full status as its title and links to the row on the vendor page. The letter carries the meaning and the color is a
  second signal, so the matrix reads in black and white.
- **Tiers.** Tier 1: the generated code is checked in CI against a local instance of the system. Tier 2: it is verified
  against the system at release time. Tier 3: the system is specified (coverage matrix and validation only). A system
  whose COVERAGE.md has no `Tier:` line shows "to be assigned"; the vendor page names the line the tier came from.
- **Provenance.** The index page lists each COVERAGE.md with the SHA-256 the index recorded; the build recomputes that
  hash and stops when the matrix changed after `coverage_index.py` ran. Every page shows the generation date.
  `manifest.json` records the date, the options, and the SHA-256 of every input and every written file.
- **Badges.** `badges/<vendor>.svg` shows the vendor (and its tier when assigned) and the five counts. With
  `--ci-run TEXT` (the CI job passes "passing, run N, date") a third segment shows the run, and `badges/ci.svg` shows
  that run or "not run in CI".
- **Links.** The link text of a reference is the host and the last path segment; the link itself is the full URL. Notes
  are shown on the vendor page only where they say more than the reason.
- **No JavaScript.** Static HTML and one stylesheet, readable in any browser, in a text browser and when saved. Printing
  uses A4 (the matrix and the vendor pages in landscape), with repeated table headers and the status colors kept.

## Checks

`--check` compares content, not file times, because a git checkout does not keep modification times. It recomputes the
SHA-256 of every input and fails when one differs from the manifest, when an input was added or removed, or when a
written file is missing or was edited. It writes nothing.

`check_agreement.py` reads the `data-vendor`, `data-status` and `data-cell` attributes of every count and matrix cell
and compares them with the summary and the five `--status` listings of the CLI.
