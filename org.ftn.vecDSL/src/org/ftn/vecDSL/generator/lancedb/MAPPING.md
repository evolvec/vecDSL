# vecDSL to LanceDB: mapping notes

LanceDB OSS 0.41.0 (29 Oct 2026, Apache 2.0) over Lance 14.0.2, embedded: the library runs inside each generated program and
keeps every table as a Lance dataset under one storage URI. Each note names the construct, the operations the generator builds
and the reference that fixes the behavior. COVERAGE.md has one row per grammar construct; this file explains the choices behind
the rows. Abbreviations: `LDB` = https://github.com/lancedb/lancedb/blob/v0.41.0/, `LANCE` =
https://github.com/lance-format/lance/blob/v14.0.2/. "Measured" means checked with lancedb 0.41.0 and @lancedb/lancedb 0.41.0 on
14 Nov 2026.

## Deployment and SDKs

1. **Embedded, no server.** The storage URI comes from `VECDSL_LANCEDB_URI`, default `lancedb-data` relative to the working
   directory; `create connection local path` or `uri` sets the default, `in-memory` is `memory://`. Hosts, `db://` URIs, credentials,
   TLS, gRPC ports and non-embedded deployments are unsupported with their reasons (LanceDB Cloud and Enterprise need an API key and
   a `db://` URI, LDB `docs/src/cloud/index.md`). The README replaces the Docker Compose file of the server vendors.
2. **Python** uses `lancedb` 0.41.0 (Python 3.10 or later), **TypeScript** `@lancedb/lancedb` 0.41.0 with `apache-arrow` 21.2.0
   (Node 22 or later, the package's `engines`). **Java** uses the Lance Java SDK `org.lance:lance-core` 14.0.2 with Arrow Java 18.3.0,
   because `com.lancedb:lancedb-core` works with LanceDB Cloud and Enterprise only (LDB `docs/src/java/java.md`). Both Java SDKs read
   the same Lance format, so the three languages read and write the same files. The Lance JNI library ships for Linux and macOS
   only (LANCE `.github/workflows/java-publish.yml`); on Windows the Java program runs in a Linux container.
3. **Every operation is a runtime call** (`vecdsl_lancedb_runtime.py`, `vecdslLancedbRuntime.ts`, `LancedbRuntime.java`) with plain
   data that the generator builds: table schemas, index plans, where strings, update expressions and search plans. The runtimes
   hold the SDK calls and do the same work in the three languages (D3 score conversion, fusion, sorting, bit packing).
4. **Read consistency interval 0**: every read checks for a newer table version, so a program sees its own and other processes'
   commits (LDB `python/python/lancedb/__init__.py`, `read_consistency_interval`). `consistency` settings are listed as ignored.

## Data model

5. **A collection is a table**, a logical database a sub-directory of the storage URI (`create database`, `list databases`, `drop`
   removes the tables and the empty directory). Table and database names are checked against the characters LanceDB accepts.
6. **Types** (LANCE `docs/src/format/table/schema.md`): int int32, bigint int64, int8, float float32, double float64, bool, varchar,
   text and uuid utf8, date date32, datetime timestamp(us), timestamptz timestamp(us, UTC), bytes and media binary, arrays
   lists, structs structs (sub-fields declared). Vectors are fixed-size lists of float32, float16 or float64; a multivector is a
   list of fixed-size lists (cosine only, LDB `docs/src/search/multivector.md`). json and geo types are pending (the `lance.json`
   extension type and GeoArrow); map, bfloat16 and sparse vectors do not exist in Lance; int8 and uint8 vectors are unsupported
   because a query vector must have the column's element type and the SDKs send float32 (LANCE `rust/lance/src/dataset/scanner.rs`,
   line 2416, measured).
7. **Binary vectors** are uint8 fixed-size lists of `dim / 8` bytes; programs write lists of 0 and 1 that the runtimes pack most
   significant bit first (numpy.packbits order), and hamming distance counts differing bits (measured). Java cannot search them:
   lance-core's `Query.setKey` takes `float[]` only.
8. **The key is unique by construction**: Lance has no primary keys, so inserts and upserts are merge inserts on the key column
   (`merge_insert(key)`); `on-conflict` error checks the keys first, ignore inserts only new keys, update and replace update matches.
9. **Tenants, namespaces and partitions** are hidden utf8 columns `vecdsl_tenant`, `vecdsl_namespace`, `vecdsl_partition` (or the
   field marked `partition-key`), written by every write that names them and added to every filter. A partitioned collection
   writes its default partition when none is named. Listing them is `SELECT DISTINCT` in the runtime.
10. **Descriptions and tags** live in the field metadata of the key column (`lancedb:description`, `vecdsl:tags`), since a Lance
    table has no table-level properties; `set description` uses `update_field_metadata` (Java: a transaction with
    `UpdateConfig.fieldMetadataUpdates`).

## Indexes

11. **Vector indexes** (LANCE `docs/src/format/index/vector/index.md`): hnsw and auto IVF_HNSW_SQ, flat and ivf IVF_FLAT, product
    quantization IVF_PQ or IVF_HNSW_PQ, binary quantization IVF_RQ (RaBitQ, ivf only), binary vectors IVF_FLAT with hamming.
    Scalar quantization on ivf is unsupported because TypeScript has no IVF_SQ builder (LDB `nodejs/lancedb/indices.ts`).
    `centroids` is `num_partitions`, `max-degree` `m`, `build-candidates` `ef_construction`; `search-candidates` is listed as
    ignored (ef is per query).
12. **Indexes are created with the table**, also on an empty table (measured for BTREE, BITMAP, LABEL_LIST, FTS, IVF_FLAT,
    IVF_HNSW_SQ, IVF_PQ). Rows written later are unindexed until `compact` (optimize); searches scan them exactly, so results are
    correct, but full-text fuzziness covers indexed rows only (measured). Lance 14.0.2 cannot add rows to a scalar index on a
    nested column that was created on an empty table (it looks the leaf name up as a top-level column, LANCE
    `rust/lance/src/index/scalar.rs` line 98, measured); `optimize` rebuilds such indexes first.
13. **Payload indexes**: keyword, integer, float, datetime and uuid BTREE, bool and `structure bitmap` or `inverted` BITMAP,
    arrays LABEL_LIST, full_text FTS. Nested paths (`details.pageCount`) are indexed directly.
14. **Full-text**: every field with `full-text` gets an FTS index `<field>_fts` unless a full_text payload index names it. FTS
    indexes keep positions (phrase queries need them). The analyzer's tokenizer maps to simple, whitespace, raw or ngram; a
    language sets the stemmer and stop words; listed filters (lowercase, stop, stemmer, ascii_folding) are taken exactly, an
    analyzer without filters keeps LanceDB's defaults (LANCE `docs/src/guide/tokenizer.md`).

## Writes

15. **Insert, upsert and import** convert records to Arrow with the declared schema (unknown fields rejected) and write in
    batches; `bulk-import` reads local JSON or JSON Lines (other schemes and formats pending).
16. **Update**: scalar values are SQL expressions of one `update(values_sql)`, literals quoted at generation time and parameters
    bound by the runtime; array-append is `array_concat`, array-remove `array_remove_all` (measured); remove-fields sets NULL.
    Vector, list and struct values rewrite the matching rows through a merge insert (`patch_rows`). `mode replace` rewrites the
    rows with nulls (or defaults) outside the given values and keeps vectors and slice columns, as pgvector does.
17. **Delete** with `limit` deletes the first rows in key order; `truncate` deletes all rows (or a slice) and keeps the table.
18. **Schema evolution**: add-field is `add_columns` (a default is `CAST(<literal> AS <type>)`, then the column is made nullable
    again, since LanceDB marks computed columns non-null, measured), drop-field `drop_columns`, rename-field `alter_columns`
    (rename). The generator tracks the evolved schema across statements, so later statements use the new names and columns.
19. **Rename of a table** is a copy and a drop (concealed): LanceDB OSS rejects `rename_table` (LDB
    `rust/lancedb/src/database/listing.rs` line 1264).

## Versions

20. **Snapshots are tags** of the current version (`tags.create`), restore is checkout of the tag and `restore()` (a new version
    with the tagged content), drop-snapshot deletes the tag, `as-of` reads a version number or tag. Clone `mode shallow` is
    `clone_table` with the source version or tag; `mode snapshot` copies rows, schema and declared indexes (deep clone is "not yet
    implemented", line 1099 of `listing.rs`).
21. **Compact** is `optimize()`: compaction, removal of untagged versions older than seven days (LanceDB's default, LDB
    `rust/lancedb/src/table/optimize.rs`) and index update. Load prewarms indexes (`prewarm_data` is remote-only, LDB
    `rust/lancedb/src/table.rs` line 3698); release, flush and wait need no call because a commit is durable on return.

## Queries

22. **Where strings are built at generation time** (LancedbSql): identifiers in backquotes, nested paths as `` `a`.`b` ``, string
    literals single-quoted with quotes doubled, LIKE patterns with backslashes escaped; parameters become `{name}` placeholders that
    the runtime fills with the same quoting (`sql()`). DataFusion accepts `-- comment` and `;` inside an unquoted filter (measured), so
    no value is ever spliced unquoted. Operators: comparisons, IN, BETWEEN, IS NULL, `array_has`, `array_has_any`,
    `array_has_all`, `array_length`, `starts_with`, `contains`, `regexp_like`, and `contains_tokens` on lower-cased text for the
    term operators (it compares tokens case-sensitively, LANCE `rust/lance-datafusion/src/udf.rs`). Geo predicates are pending.
23. **Scores per D3** in the runtimes: cosine `1 - d` (LanceDB's cosine distance is `1 - cos`), dot `1 - d` (its dot distance
    is `1 - x.y`), euclidean `-sqrt(d)` (LanceDB reports squared l2), hamming `-d`; `score native` keeps `_distance`; a multivector score is the number of query vectors minus the distance, the sum of
    the best cosine similarities (max_sim) (measured). Bounds and sort
    keys compare converted scores.
24. **Vector search**: `probes` nprobes, `candidates` ef, `oversampling` and `rescore` refine_factor, `exhaustive` and `exact`
    bypass_vector_index, `filter-strategy` prefilter; `target-recall`, `scan-budget` and iterative filtering are unsupported.
    `query id` reads the stored vector first.
25. **Keyword search** is FTS with BM25 (`_score`): MatchQuery, MultiMatchQuery with boosts, PhraseQuery with slop, fuzziness (AUTO
    one edit, at most 2), `operator and`. TypeScript's full-text query has no postfilter, so `filter-strategy post_filter` on a
    keyword search raises Unsupported in TypeScript at run time.
26. **Hybrid search** runs each leg and fuses in the runtime exactly as D3 defines rrf, weighted (alpha) and relative_score.
    **Recommend** searches with 2 mean(positive) minus mean(negative) (examples by id, also from `lookup-in`, or vectors).
27. **Scroll, sample, distance matrix, get by ids and count** are scans: scroll orders by key (or sort clauses), slices by an
    FNV-1a hash of the key's JSON text, sample is random (or first), the matrix samples rows and ranks the sample.
28. **Not generated yet** (pending): cursor, with-total, mmr, boost, aggregations, prefetch, json and geo fields, CSV and
    other import formats, object-store imports.
29. **Group-by and group size** (concealed, 2026-11-16): LanceDB has no grouped search (the query builders of table.py and
    query.py of lancedb 0.41.0 have no group parameter; the only
    "group" in either file is the writer's max_rows_per_group), so the search passes `group={key, size}` and the
    runtime ranks every row the where string selects (`count_rows(where)` is the candidate window), groups the hits by the
    key's value (a column or a sub-field path, JSON text as the group identity), keeps the groups in the order of their best hit
    and returns `{group, hits}` with the best `size` hits of each (1 by default, as the pgvector and DuckDB generators
    use); limit and offset count groups, score bounds remove hits after the size cut, and a group left without a hit
    is dropped. Sort with group-by is unsupported (groups follow the relevance of their best hit), as are grouping by a raw
    path (Lance columns are declared) and by a vector field. Cost: the search ranks every matching row, not limit plus offset
    candidates. Same result shape as pgvector (its MAPPING.md note 29) and DuckDB (its MAPPING.md note 42), computed in the runtime
    instead of SQL.

## Known limitations

- Embedded deployment only: LanceDB Cloud and Enterprise (`db://` URIs, API keys) are unsupported.
- Java runs in a Linux container on Windows, because the Lance JNI library ships for Linux and macOS only.
- Hamming search raises Unsupported at run time in Java, and keyword `post_filter` in TypeScript.
- Fuzzy matching and nested scalar indexes cover rows written before the last `compact` (note 12): a
  fuzzy search right after a write finds only exact matches among the new rows.
- Lance tables have no primary key; key uniqueness is a merge insert on the key column.
