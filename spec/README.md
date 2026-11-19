# vecDSL 1.0 specification

vecDSL 1.0 freezes the language and defines its intermediate representation (IR). Every generated document in `spec/`
names its source and can be rebuilt by a script in `spec/tools/`.

## What 1.0 is

vecDSL 1.0 is grammar 7.1 with no rule changed, so GenerateVecDSL.mwe2 does not need to run for it. `VecDSL.xtext`
differs from 7.1 in its header comment only (`spec/tools/freeze_header.py` checks that nothing below the header changes
and keeps the CRLF line endings). The EMF namespace URI stays `http://www.ftn.org/vecDSL/VecDSL/7`, because the
metamodel is unchanged and VERSIONING.md ties a change of the URI to a major release. The validator and
`capabilities/` are the same as for 7.1.

1.0 adds the IR: a JSON document per program with a JSON Schema, normalization rules N1 to N14 (SEMANTICS.md), an
exporter and an importer in `org.ftn.vecDSL.ir`, and the `ir` command of vecdsl-cli. Besides `spec/` and the `ir`
package, `org.ftn.vecDSL/META-INF/MANIFEST.MF` exports `org.ftn.vecDSL.ir`.

## Files

| file | what | how to rebuild |
|---|---|---|
| spec/KEYWORDS.txt | the 563 hard keywords of 1.0 | `python spec/tools/keywords.py --out spec/KEYWORDS.txt [programs]` |
| spec/VecDSL-1.0.xtext | the grammar with the 1.0 header | `python spec/tools/freeze_header.py <VecDSL.xtext> spec/VecDSL-1.0.xtext` |
| spec/GRAMMAR.md | every rule in EBNF with its concept and the language design decisions it cites | `python spec/tools/grammar_doc.py` |
| spec/METAMODEL.md | every EClass with its features; the 456-row derivation | `python spec/tools/metamodel_doc.py` |
| spec/csv/*.csv | the grammar exports | `python org.ftn.vecDSL/tools/grammar_report.py spec/VecDSL-1.0.xtext [<VecDSL-7.0.xtext>] --admissibility org.ftn.vecDSL/src/org/ftn/vecDSL/validation/search-params-admissibility.properties --out spec/csv` |
| spec/ir/vecdsl-ir-1.0.schema.json | JSON Schema of the IR (draft 2020-12) | `python spec/tools/ir_schema.py [--check FILES]` |
| spec/SEMANTICS.md | concept definitions, N1 to N14, divergence notes | by hand |
| spec/negative/*.vecDSL | seven rejected programs with their expected code | by hand |
| spec/conformance/ | conformance corpus: 28 samples and 7 rejected programs | `VecDSLIrMain --export` on the samples and on spec/negative, then `python spec/tools/conformance.py --exported DIR` |
| spec/CHANGELOG.md, spec/VERSIONING.md | 7.0 to 7.1 to 1.0; the versioning policy and how a change to the language is proposed | by hand |
| org.ftn.vecDSL/src/org/ftn/vecDSL/ir/ | Json, IrSource, IrNames, IrNormalizer, IrWriter, IrReader, VecDSLIr, VecDSLIrMain | |
| org.ftn.vecDSL.tests/src/org/ftn/vecDSL/tests/ | VecDSLIrRoundTripTest, VecDSLIrSchemaTest, VecDSLIrNormalizationTest | |

The `v7.1` column of `spec/csv/metrics.csv` is the 1.0 grammar (the script's label); the 1.0 and 7.1 grammars give
identical exports.

## The IR

- **Document.** One JSON document per program, `{"vecdsl-ir": "1.0", "program", "spans", "literals"}`. The program body
  has one object per model element: `"$type"` (the EClass name) first, then every structural feature in the order of
  `eAllStructuralFeatures` (schema order), then `"$norm"`. There are no EMF identifiers. Every feature is present: an
  undeclared single value is `null`, a flag `false`, an empty list `[]` (N1). Numbers are JSON numbers, vectors arrays
  of numbers, and a literal whose value a double cannot hold keeps its text in `literals` (N14). Positions are in
  `spans`, keyed by the JSON pointer of the element in `program`, so two programs that differ only in layout have
  identical bodies. A program with an error gives a diagnostics document `{"vecdsl-ir": "1.0", "diagnostics": [...]}`
  instead.
- **Normalization under `"$norm"`.** The normalized view (canonical similarity with the native flag, index
  decomposition, merged vector field, resolved defaults, parameter slots, extensions by vendor) is written as derived
  entries next to the declared values, not in their place, because the IR must rebuild the exact model and a driver
  needs both what the program wrote and what it means. The importer ignores `"$norm"`; the writer recomputes it, so it
  cannot drift from the body. The index split (D4) and the merge of named vector configurations into VectorField (D5)
  are structural in the grammar; N3 and N4 state their per-field view.
- **Declared versus default.** The exporter decides whether a value was written with the node model, as the validator
  does (`ModelUtil.written`). A model rebuilt from an IR carries the same facts in an `IrSource` adapter (declared
  attributes, span, literal texts), so the IR of a rebuilt model is byte-identical. A model built by hand, with
  neither, falls back to EMF's `eIsSet`.
- **Defaults.** The IR resolves only the defaults that the grammar, the validator or the admissibility table state: the
  score form (D3), the name of an unnamed vector field (D5), the addressed vector field and the metric in force, the
  shared hybrid input, the snapshot name, the admitted params keys. The first literal of an enumeration is not a
  default: an undeclared consistency, metric or fusion is `null` and target-defined.
- **References.** A reference is the path of its target (`books.title`; `books.vector` for an unnamed vector field;
  `docs.e.ix` for an index inside field `e`), with `#k` only when several elements of the reference's type share the
  path (N12). Element names come from `VecDSLQualifiedNameProvider`, so the IR and the linker agree on names.
- **Extensions.** `extensions` is an object keyed by vendor id; the value lists the vendor's blocks and each block its
  items, and the spans of the blocks follow the same grouped order. Grouping by vendor is the one order the IR does not
  keep (N13): the round trip compares the rebuilt model with the parsed model after grouping its blocks by vendor
  (`VecDSLIr.groupedByVendor`).
- **JSON.** `org.ftn.vecDSL.ir.Json` is a small parser and writer, so the bundle needs no JSON library: parsed numbers
  are BigDecimal, and written output is deterministic.
- **Entry point.** `org.ftn.vecDSL.ir.VecDSLIrMain`: `ir <program> [out]`, `ir --check <dir>`,
  `ir --export <dir> <out-dir>`. vecdsl-cli wraps it.

## Rejected programs

`spec/negative/` holds seven programs, each rejected for one stated code (syntax, linking, wf.recall.one,
wf.update.noSelector, wf.delete.severalSelectors, t.vf.ambiguous, wf.create.notTopLevel); the round-trip and schema
tests and the conformance corpus use them. The negative corpus that NegativeProgramsTest reads
(`org.ftn.vecDSL.tests/scenarios/negative`) records judgments, and some of its programs are valid.

## Schema validation

VecDSLIrSchemaTest checks the structural part in Java (every `$type` defined, every element's keys exactly the
definition's) and writes the documents to `generated/ir/`. The full draft 2020-12 validation runs in Python:
`python spec/tools/ir_schema.py --check FILES` (jsonschema 4.26). The schema is generated from VecDSL.ecore and
VecDSL.xtext except the `$norm` entries, which the script holds (`NORM`).

Xtext marks no class abstract, so the generator reads the grammar: a class is admitted where an action creates it or a
rule assigns to it before any action. That gives 131 element definitions; a supertype such as Literal or Condition
appears only as a union (`Literal.any`).

## Keyword scan

`spec/tools/keywords.py` is the generators' keyword check with two differences: comments are removed outside strings
before names are collected, and a hard keyword after a word that takes an optional name (boost-fn, stage,
quantization, vector-field) is not reported.
