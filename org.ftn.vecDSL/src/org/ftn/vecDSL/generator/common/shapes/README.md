# Shared result-shape module

The result-shape clauses of a vector, keyword, recommend or hybrid search, and of a scroll (boost, cursor, mmr, group-by
with size, sort, offset and limit, score bounds, with-total, aggregate, exclude, with-vectors), implemented once in Python,
TypeScript and Java for the targets without SQL. The reference is the pgvector generator (`PgvectorQueries.shaped`),
verified on PostgreSQL and, through the same design, on Oracle and DuckDB. The MongoDB and OpenSearch generators use the
module.

| file | what |
|---|---|
| `python/vecdsl_shapes.py` | `shape(candidates, spec)`, `window_size(spec)`, `UnsupportedShape` |
| `typescript/vecdslShapes.ts` | `shape`, `windowSize`, `UnsupportedShape` (no imports; `tsc --strict`) |
| `java/Shapes.java.template` | `Shapes.shape`, `Shapes.windowSize`, `Shapes.UnsupportedShape` (`{{java.package}}`, Java 21) |
| `../ShapeSpec.java` | the generator side: a `QueryMethod` to the runtime `spec` as a Code `Entries` |
| `fixture.json` | 101 cases and 14 window cases, run by the three test suites |
| `python/test_vecdsl_shapes.py`, `typescript/vecdslShapes.test.ts` with `tsconfig.json`, `java/ShapesCheck.java.template` | the suites |

## 1. Contract

**Candidates.** The list the target retrieved as the candidate window, in any order, each an object:

| key | value |
|---|---|
| `key` | the record key: a string or a finite number (not a boolean) |
| `score` | the canonical similarity of D3, higher is better; the native score under `score native`; null for a scroll |
| `fields` | the payload fields: the output fields plus every field the spec reads (`ShapeSpec.reads()`); nested values as objects |
| `vectors` | optional: stored vectors by field name; the diversity field when mmr is written, the vector fields with with-vectors |
| `highlight` | optional: kept as the target gave it |
| `explanation` | optional: kept as the target gave it |

**Spec.** These keys, in this order. A clause the program does not write is null (N1); the module holds the defaults.

| key | value | grammar (VecDSL.xtext, 1.0) |
|---|---|---|
| `offset` | integer or null (0) | Paging 437 |
| `limit` | `top`, or a scroll's `limit`, or null (no limit) | 484 and each search's `top`; Scroll 644 |
| `cursor` | the key text of the last record of the previous page, or null | Paging 438; SEMANTICS.md Paging 1.0.1 |
| `bounds` | `{min, max}` (each a number or null), or null | ScoreSpec 453, ScoreBounds 685 to 688 |
| `score` | `"similarity"`, `"native"`, or null for a scroll | ScoreSpec 452 |
| `boost` | `{functions: [{name, kind, field, weight, shape, origin, scale, offset, rate, when}], combine, apply}` or null | Reordering 459, BoostBlock 753 to 757, BoostFunction 762 to 768 |
| `mmr` | `{lambda, candidates, vectorField, metric}` or null | Reordering 458, MmrConfig 699 to 702 |
| `group` | `{field, size}` or null | ResultShape 465 |
| `sort` | `[{field, direction}` or `{relevance: true, direction}]` | ResultShape 470, Scroll 647, SortClause 740 to 741 |
| `withTotal` | boolean | ResultShape 467 |
| `aggregations` | `[{name, kind, field, interval, size}]` | ResultShape 471, AggregationClause 745 to 749 |
| `output` | field names, or null for `output *` | OutputSpec 680 to 681 |
| `exclude` | field names | ResultShape 468 |
| `withVectors` | boolean | ResultShape 466, Scroll 648 |
| `keyField` | the name the key takes in a hit (the collection's primary key) | |

A field (`field` of group, sort, boost, aggregation and condition) is a list of path segments, FieldPath 818 to 820. A boost
condition `when` is `{and: [c, c]}`, `{or: [c, c]}`, `{not: c}` or a predicate `{field, op, value?, upper?}` (Predicate 806 to
813) with `op` one of eq, neq, gt, gte, lt, lte, in, not_in, like, prefix, contains, contains_any, contains_all, contains_text,
between (with `upper`), is_null, is_not_null, exists, not_exists, is_empty, length_eq to length_lte.

**Result.** One of three shapes, the ones the vendor runtimes use (pgvector `shaped`, Elasticsearch `search` and `hybrid`,
Vespa `search`):

- a list of hits `{<keyField>: key, score, <output fields>, <vectors>, highlight?, explanation?}` (no `score` on a scroll);
- with group-by, a list of `{group, hits}`;
- with with-total or aggregate, `{hits, total?, aggregations?}` with either of the above as `hits`.

Numbers are rounded to six fraction digits and integral values are integers, as the runtimes print them.

## 2. Pipeline

The stages and their order are those of `PgvectorQueries.shaped` (MAPPING note 25):

| stage | rule | pgvector |
|---|---|---|
| 1. rank | score descending, then key: numbers before strings, numbers by value, strings by code point. Native scores keep the target's order. A scroll ranks every record by its sort keys, then by key | `ord, key` in every CTE; `_order` of the runtimes |
| 2. boost | each function's value (None where its field is null or its `when` is not true); combine (sum, multiply, max, avg, replace; default multiply); apply to the relevance (default multiply); a record no function applies to keeps its relevance; rank again | dec 45, note 32, `combine` and `apply` |
| 3. cursor | continue after the record with the cursor key in this order; none found gives an empty page | dec 36, note 26, SEMANTICS.md Paging 1.0.1 |
| 4a. group | groups in the order of their best hit; offset and limit count groups; `size` hits each (1); a record without the group field is in the group null | dec 42, note 29 |
| 4b. mmr | pool: the first `max(candidates, limit + offset)` records with a diversity vector, else `max(10 (limit + offset), 10)`; pick limit + offset times the record maximizing `lambda * relevance - (1 - lambda) * max similarity to the picked`, rank breaking ties; drop the first offset; every hit keeps its score | dec 43, 44, 52, note 33 |
| 4c. page | offset, then limit | `page` CTE |
| 5. sort | the selected hits by the sort keys, missing values last in both directions, the previous order breaking ties | dec 41, note 28 |
| 6. bounds | hits whose score lies outside the bounds are dropped (a group left empty is dropped) | `hits ... WHERE bounds` |
| 7. projection | key, score, output fields, vectors, highlight, explanation | `hitObject` |
| 8. total, aggregations | over the ranked window before the cursor, within the bounds | dec 39, 40, 47, notes 27 and 30 |

Refusals (`UnsupportedShape`, pgvector's wording of dec 51 and of the generator's checks): cursor with group-by or mmr; sort
with group-by; mmr with group-by; boosts with score native; mmr with score native; boost, mmr or group-by on a
scroll; mmr lambda outside 0 to 1; decay rate outside the open interval 0 to 1; range aggregations. `ShapeSpec` raises the same
messages at generation time as `Unsupported`.

## 3. The window rule

`windowSize(spec)` (Python `window_size`), and `ShapeSpec.window()` at generation time, give the number of candidates a target
retrieves:

1. a scroll: none (every matching record) when it sorts or resumes after a cursor, else limit plus offset;
2. no limit: none;
3. mmr with `candidates` written: `max(candidates, limit + offset)`;
4. any of cursor, boost, mmr, group-by, sort, with-total, aggregate written: `10 * (limit + offset)`;
5. otherwise `limit + offset`.

Totals and aggregations are computed over that window within the score bounds. **This diverges from the pgvector generator**,
which counts every record the search matches; a target that can count natively should keep doing so, and a generator that
uses the module states the reading in the vendor's MAPPING.md (step 3 of section 5). Groups and boosts rank the window too, where pgvector
ranks every match: a group whose best record lies outside the window is missing, and a boost cannot raise a
record from outside the window.

## 4. Divergences

From pgvector:

- total, aggregations, groups and boosts over the window, not every match (section 3);
- a cursor record outside the window gives an empty page, as a cursor that matches nothing does;
- mmr with score native is refused, where pgvector computes the canonical relevance next to the native score;
- MMR similarities in double, where pgvector accumulates in float;
- `sum` and `avg` in double, where PostgreSQL's `sum(real)` returns real.

From the runtimes of the vendors that do not use the module (a generator that replaces their code with the module changes the
vendor's output in these cases and says so in its MAPPING.md):

- Elasticsearch `hybrid`: score bounds filter the fused records before paging and before groups (pgvector after); a cursor
  replaces the offset (pgvector applies the offset after the cursor); sort through a second `_search` over the page, ordered by
  the server; mmr with `1 - lambda` in double; bounds with a 1e-9 tolerance; aggregations computed by the server over the fused
  set (an ids query).
- LanceDB `_page`: bounds filter before the window of limit plus offset (pgvector after); bounds with a 1e-9 tolerance;
  `_groups` agrees with pgvector (null group, bounds after selection, empty groups dropped).
- Vespa `search`: groups from YQL grouping in Vespa's order (vespa note 36); a hit with no score passes the bounds; sort keys
  compared as numbers or strings without a rank for booleans.
- TypeScript runtimes round with `toFixed`, which rounds exact binary ties away from zero (0.0078125 to 0.007813); the module
  rounds them to even in all three languages, as Python's `round` and Java's `HALF_EVEN` do.

Where these disagree, the module follows pgvector in every case listed.

## 5. Using the module in a generator

1. **Candidate request.** Call `ShapeSpec.of(query)`. If `plain()`, keep the native mapping. Otherwise request
   `window()` candidates (Nil: every matching record) with the statement's filter, read scope and score form, but without the
   clauses the module applies (offset, cursor, sort, group, bounds unless the target applies them natively, and totals); fetch
   the output fields plus `reads()`, the stored vector `diversityField()` when it is not null, and every vector field when
   `withVectors` is set. Convert scores to D3 as the plain path does. A target that has a native operator for a clause keeps
   it (Milvus group-by, Elasticsearch collapse and search_after, Vespa grouping) and calls the module without that clause.
2. **Call shape.** Write `ShapeSpec.module(language)` next to the vendor runtime under `ShapeSpec.moduleFile(language)`
   (Python `vecdsl_shapes.py`, TypeScript `vecdslShapes.ts`, Java `Shapes.java` with `{{java.package}}` replaced by the runtime's
   package) and emit `rt.shape(<candidates>, <spec>)`, `spec` being
   `ShapeSpec.spec()`; set `metric` with `Entries.put` when it is null and the target has a default metric, and `keyField`
   when the target names the key otherwise. Map `UnsupportedShape` to the runtime's unsupported error.
3. **Mark rows concealed** in COVERAGE.md and add one MAPPING.md note naming the module, the window rule and its divergence
   from the pgvector generator, and how boosts are combined; then regenerate the coverage index
   (`python org.ftn.vecDSL/tools/coverage_index.py`).
4. **Fragment tests.** In `<Vendor>GeneratorTest` or the coverage test, assert the window size and the `rt.shape` call for
   each clause, and that a plain search keeps its statement byte for byte.
5. **Expected values.** Add the statements to the vendor's full sample with expected values computed by the module from the
   sample's seed records (the pgvector cases of `fixture.json` show how), and run them on the server.

## 6. Running the suites

From this folder:

- Python: `python -I -m unittest discover -s python`
- TypeScript: in a copy of `typescript/` with `typescript` and `@types/node` installed: `npx tsc -p . && node --test dist/vecdslShapes.test.js`
- Java: render both templates with a package (`{{java.package}}` to `vecdsl.shapes`), `javac --release 21 -Xlint:all -Werror`,
  then `java -cp <classes> vecdsl.shapes.ShapesCheck fixture.json`

`fixture.json` is generated; do not edit it by hand.

Each suite runs the 101 cases and the 14 window cases of `fixture.json`; equal input gives equal output in the three
languages, apart from how Java prints small numbers (`6.0E-6`).
