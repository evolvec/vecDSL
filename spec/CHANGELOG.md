# vecDSL changelog

## 1.0 to 1.0.1 (16 Nov 2026)

Documentation patch (VERSIONING.md, "patch"): no program changes meaning, the grammar, metamodel and IR are unchanged.

```
Paging.cursor defined: the key of the last record of the previous page (SEMANTICS.md, Paging)    16 Nov 2026
SEMANTICS.md: highlight on vector searches, read consistency and fuzzy matching, as observed on
  the target servers                                                                             16 Nov 2026
```

## 7.1 to 1.0 (16 Nov 2026)

vecDSL 1.0 is grammar 7.1 frozen. Every 7.1 program is a 1.0 program with the same meaning, and the metamodel is
unchanged (namespace `http://www.ftn.org/vecDSL/VecDSL/7`).

```
Grammar: header comment only (version line; namespace kept)                                   -   16 Nov 2026
Reserved words frozen: 563 hard keywords, 21 soft keywords (spec/KEYWORDS.txt)                -      16 Nov 2026
IR 1.0: JSON document per program, schema spec/ir/vecdsl-ir-1.0.schema.json, normalization
  rules N1 to N14 (spec/SEMANTICS.md), exporter and importer org.ftn.vecDSL.ir                  -   16 Nov 2026
Semantics document, metamodel inventory, grammar document, conformance seed, versioning policy  -   16 Nov 2026
```

Recorded but not changed: the CSV exports of 7.1 (6 Nov) predate the additive 8 Nov change (Snapshot `as`), so
their `metrics.csv` gives MCC 1117 and AVS 8.75 where grammar 7.1 as it stands, and therefore 1.0, gives 1118 and 8.76;
the other four CSVs are identical (spec/csv/).

## 7.0 to 7.1 (6 and 8 Nov 2026)

From the CHANGED IN V7.1 block of the grammar. Additive: every 7.0 program parses with the
same meaning; namespace URI stays /7.

```
SearchParams 'target-recall' targetRecall=SignedNumber -> declared recall floor, first element,
  checked by WF-Recall; realizable only where the target exposes a query-time accuracy target   D15  6 Nov 2026
OptNameID for optional names before soft keywords      -> not adopted: v7.0 already parses these
  positions unambiguously, and excluding soft keywords from optional names contradicts D7 and
  would reject v7.0 programs such as "vector-field type type float_vector"                    D16  6 Nov 2026
Documentation versions re-pinned from one source type per system (header)                       -    6 Nov 2026
Validator: WF-Recall, generic SearchParams admissibility, WF-CreateTop, WF-Upd, WF-Del          D15  6 Nov 2026
Capability manifests for the 13 VendorIds, entry target-recall                                  D15  6 Nov 2026
Milvus generators: target-recall raises Unsupported in Python, TypeScript and Java              D15  6 Nov 2026
Snapshot ('as' snapshotName=STRING)?                    -> optional snapshot name, the string that
  Restore and DropSnapshot refer to; default is the statement label (additive)              -    8 Nov 2026
Extension banners 17.1 and 17.2 pinned to Vespa 8.781.11 and Qdrant 1.20.0, content       -    8 Nov 2026
  re-checked against the pinned documentation (17.1 placeholder removed)
```

The validator's type rules (T-Collection, T-Record, T-Ids, T-Cmp, T-VF, T-List-Vec, T-Bounds, T-Hybrid and the `top`
check) are in `VecDSLValidator.java`, are part of the 1.0 well-formedness judgments, and were not changed for 1.0.
