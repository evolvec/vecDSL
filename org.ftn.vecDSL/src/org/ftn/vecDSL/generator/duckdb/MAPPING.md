# vecDSL to DuckDB: mapping notes

DuckDB 2.0.0 (21 Oct 2026, MIT) with the core extensions vss (HNSW indexes, experimental) and fts (BM25), embedded: the library
runs inside each generated program over one database file. Each note names the construct, the SQL the generator builds and the
reference that fixes the behavior; COVERAGE.md has one row per grammar construct. Abbreviations: `DOCS` =
https://duckdb.org/docs/2.0/, `VSS` = https://github.com/duckdb/duckdb-vss/tree/3685131eac/ (the vss commit of the 2.0.0
extension build). "Measured" means checked with Python duckdb 2.0.0 and the extension wheels duckdb-core-ext-vss and
duckdb-core-ext-fts 2.0.0 on 14 Nov 2026. Each note ends with
**[pgvector: agrees | diverges]** and **[LanceDB: agrees | diverges]**, comparing with `generator/pgvector/MAPPING.md` and
`generator/lancedb/MAPPING.md`.

## Paradigm placement

DuckDB is the 14th system and the second one in P4 (embedded), after LanceDB. It has a SQL surface, which would suggest P2
(relational database with a vector extension, where pgvector sits), but the facts that decide the paradigm are about where the
engine runs and where the index lives:

1. **In-process.** The client library is the engine; there is no separate server process. DOCS `connect/concurrency.html`
   describes concurrency "in in-process mode": one process reads and writes a database file, or several processes read it
   read-only.
2. **No server, no users.** There is no network endpoint, no authentication and no roles in DuckDB itself; access to the data is
   access to the file (DOCS `connect/concurrency.html`). MotherDuck, the hosted service, uses `md:` connection strings
   (https://motherduck.com/docs/integrations/jdbc-driver) and is out of scope.
3. **File persistence.** A database is one file (plus its `.wal` while open), attached with `ATTACH` (DOCS
   `sql/statements/attach.html`); `:memory:` is a database that disappears with the process.
4. **The index is built in host memory.** An HNSW index is not buffer managed: it must fit in RAM, is not counted in
   `memory_limit`, is serialized into the file at checkpoints, and is loaded into memory when the table is first accessed after a
   restart (DOCS `core_extensions/vss.html`, "Persistence" and "Limitations").

Boundary case: the SQL surface and the delivery of the vector index as an extension (`LOAD vss`) are P2 traits. The generator
keeps the P2 code shape of pgvector (SQL built at generation time, bound parameters, one JSON column per row) and the P4
deployment of LanceDB (no compose file, a database path instead of an endpoint, server options unsupported). The paradigm
follows the runtime facts above, not the query language.

## Deployment and clients

1. **Embedded, no server.** The database file comes from `VECDSL_DUCKDB_PATH`, default `vecdsl.duckdb` next to the program
   (Python: the script's folder; TypeScript: the package folder; Java: the working directory); `create connection local path`
   or a scheme-less `uri` sets the default, `in-memory` is `:memory:`. `VECDSL_DUCKDB_READ_ONLY=true` attaches the file read-only.
   Hosts, ports, keys, tokens, users, URIs with a scheme (`md:`) and non-embedded deployments are unsupported with their reasons.
   The README replaces the compose file. **[pgvector: diverges]** (server, `VECDSL_PGVECTOR_URL`) **[LanceDB: agrees]** (one
   storage location from an environment variable).
2. **Clients.** Python `duckdb` 2.0.0 (Python 3.11 or later, https://pypi.org/project/duckdb/2.0.0/), TypeScript
   `@duckdb/node-api` 2.0.0-r.1 (the "Neo" client, https://www.npmjs.com/package/@duckdb/node-api; the older `duckdb` npm package
   stops at 1.4.4), Java `org.duckdb:duckdb_jdbc` 2.0.0.1 (https://central.sonatype.com/artifact/org.duckdb/duckdb_jdbc), which
   bundles the native library for Windows, Linux and macOS. DOCS `clients/overview.html` lists Python, Node.js (Neo) and Java
   (JDBC) as primary clients. Unlike LanceDB, the Java program runs on Windows. **[LanceDB: diverges]** (Lance JNI only on Linux and
   macOS).
3. **Extensions.** Each program opens `:memory:`, sets `extension_directory` when `VECDSL_DUCKDB_EXTENSIONS` is given, runs
   `LOAD vss` and `LOAD fts` (`INSTALL` once when missing, with a clear message offline), enables
   `hnsw_enable_experimental_persistence` and `hnsw_prefilter`, then `ATTACH`es the file and `USE`s it. Loading vss before the
   attach is required for a write-ahead log that contains HNSW changes (DOCS `core_extensions/vss.html`, "Persistence"). The
   Python requirements include the official extension wheels `duckdb-core-ext-vss` and `duckdb-core-ext-fts` 2.0.0
   (https://github.com/duckdb/duckdb-python-extensions), which work without network access. **[pgvector: agrees]** (`CREATE
   EXTENSION` per connection) **[LanceDB: diverges]** (no extensions).
4. **Experimental persistence.** `hnsw_enable_experimental_persistence` is required to create an HNSW index in a file database;
   WAL recovery is not implemented for custom indexes, so a crash with uncommitted index changes can lose or corrupt the index
   (DOCS `core_extensions/vss.html`, "Persistence"). The README states this; the generator still uses the file, because a vector
   database that forgets its data at exit would not meet the vecDSL semantics of `create collection`.

## Code shape

5. **SQL at generation time.** `DuckdbSql.java` and `DuckdbQueries.java` build every statement; the runtimes
   (`vecdsl_duckdb_runtime.py`, `vecdslDuckdbRuntime.ts`, `DuckdbRuntime.java`) run it with the same operations in the three
   languages (`query`, `value`, `exists`, `change`, `execute`, `transaction`, `read_records`, `import_file`). **[pgvector: agrees]**
   **[LanceDB: diverges]** (plans interpreted by the runtimes).
6. **Every value is a parameter.** Literals and script parameters become `$n` bound as VARCHAR text and cast in SQL (`$1::INTEGER`);
   lists, vectors and records travel as JSON text (`$1::JSON::FLOAT[4]`, `$1::JSON::VARCHAR[]`), so the three clients bind one
   type and no value reaches the SQL text (DuckdbGeneratorTest#bindsHostileValuesAsParameters). Identifiers are quoted; names
   outside letters, digits and underscores are unsupported. **[pgvector: agrees]**
7. **One JSON column per row.** Queries return `json_object(...)` or `to_json(...)` (DOCS `data/json/json_functions.html`), so
   the runtimes parse one string per row. `json_group_array` is a macro in 2.0.0, so lists are `to_json(list(x ORDER BY y))`
   (measured). **[pgvector: agrees]**
8. **Transactions.** Every method runs in one transaction; a vecDSL `transaction` is one DuckDB transaction of its steps.
   DuckDB has one isolation level (snapshot isolation, DOCS `sql/statements/transactions.html`), so `isolation` is listed as
   ignored. **[pgvector: diverges]** (isolation levels) **[LanceDB: diverges]** (no cross-table transactions).
9. **Search settings per statement.** `SET hnsw_ef_search` and `SET hnsw_prefilter` run before the statement and `RESET` after it;
   DuckDB has no `SET LOCAL` (measured). **[pgvector: diverges]** (`SET LOCAL`)

## Data model

10. **A collection is a table**, a logical database a schema of the file (`CREATE SCHEMA`, DOCS `sql/statements/create_schema.html`;
    `list databases` reads the schemas other than `main` and the fts schemas). **[pgvector: diverges]** (`CREATE DATABASE`)
    **[LanceDB: diverges]** (a sub-directory)
11. **Types.** int INTEGER, bigint BIGINT, int8 TINYINT, float FLOAT, double DOUBLE, bool BOOLEAN, varchar and text VARCHAR, date,
    timestamp, timestamptz, uuid, bytes BLOB, json, map and struct JSON, arrays `T[]`, dense float vectors `FLOAT[n]` (DOCS
    `sql/data_types/array.html`). `max-length` becomes a CHECK on `length(...)`, because `VARCHAR(n)` is accepted but not enforced
    (measured). Half-precision, binary, sparse and multi-vectors are unsupported: vss indexes FLOAT arrays only (VSS `README.md`).
    geo and geometry fields: generation pending (the spatial extension). **[pgvector: diverges]** (halfvec, sparsevec, bit)
12. **Tenants and namespaces are hidden columns** `vecdsl_tenant` and `vecdsl_namespace`; partitions are unsupported, because
    DuckDB tables have no partitions (Hive partitioning applies to files, DOCS `data/partitioning/hive_partitioning.html`).
    **[pgvector: agrees]** for tenants and namespaces, diverges for partitions (declarative partitions) **[LanceDB: agrees]** for
    tenants and namespaces, diverges for partitions (a hidden column)
13. **Aliases are views** (`CREATE VIEW`, `ALTER VIEW ... RENAME TO`); swap-alias drops and creates the view in one transaction.
    Views break when their table is renamed (measured), so the generator keeps a map of views per table and creates them again
    after a rename. **[pgvector: agrees]** **[LanceDB: diverges]** (no aliases)
14. **Descriptions** are table comments (`COMMENT ON TABLE`, DOCS `sql/statements/comment_on.html`); tags, TTL and deletion
    protection are unsupported. **[pgvector: agrees]**

## Indexes

15. **HNSW** (`CREATE INDEX ... USING HNSW (col) WITH (metric = ..., M = ..., ef_construction = ..., ef_search = ...)`, DOCS
    `core_extensions/vss.html`): `max-degree` M, `build-candidates` ef_construction, `search-candidates` ef_search; metrics cosine
    `cosine`, euclidean `l2sq`, dot `ip`, which serve `array_cosine_distance`, `array_distance` and
    `array_negative_inner_product` (VSS `README.md`, metric table). `flat` creates no index (exact scan); vamana, ivf, diskann,
    quantization and GPU builds are unsupported. **[pgvector: diverges]** (IVFFlat, halfvec quantization)
16. **Payload indexes** are ART indexes (`CREATE INDEX`, DOCS `sql/indexes.html`) on scalar columns; bitmap and json indexes are
    unsupported (ART only, with min-max zone maps per row group), path indexes pending. **[pgvector: diverges]** (GIN on jsonb)
17. **Full-text** is one fts index per table over all its full-text columns: `PRAGMA create_fts_index('schema.table', 'key',
    cols..., stemmer, stopwords, strip_accents, lower, overwrite = 1)` in the schema `fts_<schema>_<table>` (DOCS
    `core_extensions/full_text_search.html`). The analyzer gives the options: the language's Snowball stemmer, English stop words,
    `ascii_folding` as `strip_accents`; other tokenizers and filters are unsupported, BM25 parameters pending (the `k` and `b`
    arguments of `match_bm25`). All full-text fields of a table share one analyzer.
18. **The fts index is rebuilt by every write**, in the transaction of the write and once per transaction. The documentation says
    "By default, the FTS index will not update automatically"; the `incremental` option misses updates and refuses
    `ON CONFLICT DO UPDATE` ("Not implemented Error: ON CONFLICT DO UPDATE is not yet supported with REFERENCING NEW TABLE AS
    triggers", measured), and two `create_fts_index` calls in one transaction fail at commit (measured), so the runtimes collect
    the rebuild statements of a transaction and run each once before the commit. **[pgvector: diverges]** (a GIN expression
    index on `to_tsvector` follows the table) **[LanceDB: diverges]** (one FTS index per field, with positions)
19. **Indexes block restructuring.** `ALTER TABLE ... RENAME COLUMN`, `DROP COLUMN` and `RENAME TO` fail while secondary indexes
    depend on the table ("Cannot alter entry ... because there are entries that depend on it"), and indexes dropped earlier in the
    same transaction still count ("an index depends on a column after it"); ADD COLUMN works (all measured). An alter that renames
    or drops therefore runs two transactions: the first drops the HNSW, ART and fts indexes, the second alters the table, creates
    the indexes again and recreates the views. **[pgvector: diverges]** (one transaction) **[LanceDB: diverges]** (copy and drop)
20. **Persistence and compaction.** `flush` is `CHECKPOINT` (DOCS `sql/statements/checkpoint.html`), which also serializes the HNSW
    indexes; `compact` is `PRAGMA hnsw_compact_index` per HNSW index plus `CHECKPOINT`, because deletes are only marked in an HNSW
    index until it is compacted (DOCS `core_extensions/vss.html`, "Inserts, Updates, Deletes and Re-Compaction"). `load` and
    `release` are not applicable: the buffer manager reads table data on demand and the index loads itself on first access.
    **[pgvector: diverges]** (`pg_prewarm`)

## Writes

21. **Rows are one JSON parameter.** `INSERT INTO t (cols) SELECT e.r."c", ... FROM (SELECT unnest(json_transform($1::JSON,
    '[{...}]')) AS r, unnest($1::JSON[]) AS doc) e` with defaults through `json_exists(doc, '$.c')`, so a missing field takes the
    column default and an explicit null stays null. `json_transform` (not `json_transform_strict`, which fails on missing keys,
    measured). on-conflict: error (constraint error), ignore `ON CONFLICT DO NOTHING` with the skipped count, update and replace
    `ON CONFLICT ("key") DO UPDATE SET c = excluded.c` (DOCS `sql/statements/insert.html`). **[pgvector: agrees]**
22. **Update** is `UPDATE t AS t SET ...`; `remove-fields` sets `DEFAULT`, `array-append` `list_concat`, `array-remove`
    `list_filter(..., lambda element: ...)` (the arrow form of lambdas raises a deprecation error in 2.0.0, measured). Update from
    records joins the records on the key (`UPDATE ... FROM records e WHERE t.key = e.r.key`); mode replace sets missing fields to
    their defaults. **[pgvector: agrees]**
23. **Delete** with `limit` deletes `t.rowid IN (SELECT ... ORDER BY key LIMIT n)`, since DELETE has no LIMIT (DOCS
    `sql/statements/delete.html`); `truncate` is `TRUNCATE`, a namespace truncate a DELETE. **[pgvector: diverges]** (ctid)
    **[LanceDB: agrees]** (first rows in key order)
24. **Bulk import** reads a local JSON or JSON Lines file in the runtime and sends batches through the insert of note 21 in one
    transaction; parquet and csv (`read_parquet`, `read_csv`) and object stores (httpfs) are pending. **[pgvector: agrees]**

## Snapshots and clones

25. **A snapshot is a table copy** `main.vecdsl_snapshot__<name>` (`CREATE TABLE ... AS SELECT *`), restore is `DELETE` plus
    `INSERT ... BY NAME SELECT *` with the fts rebuild in one transaction, drop-snapshot `DROP TABLE IF EXISTS`; `list snapshots`
    reads the snapshot tables and `list collections` leaves them out. Snapshot tables live in `main`, because `drop-snapshot`
    names no collection. `EXPORT DATABASE` preserves HNSW and fts indexes (measured) but writes a whole database, so it does not
    fit a per-collection snapshot. **[pgvector: diverges]** (snapshots unsupported) **[LanceDB: diverges]** (tags of versions)
26. **Clone** writes the target's `CREATE TABLE` from the source descriptor (key, defaults, CHECKs), copies the rows with
    `INSERT ... BY NAME`, then creates the HNSW and fts indexes, in one transaction; `CREATE TABLE AS SELECT` would drop the key
    and constraints. Shallow and as-of clones are unsupported. **[pgvector: agrees]**

## Queries

27. **Scores (D3).** cosine `1 - array_cosine_distance`, euclidean `-array_distance`, dot `-array_negative_inner_product`, the
    same canonical similarities as pgvector; `score native` returns the distance. **[pgvector: agrees]**
28. **Vector search uses the index only in one shape.** The vss optimizer rewrites `ORDER BY distance LIMIT n` into an index scan
    (VSS `README.md`); measured: it also does so with bound parameters (`$1::JSON::FLOAT[4]`) and inside a materialized CTE, but
    not with a second sort key (`ORDER BY d, key`) or with OR filters and IN lists, which scan exactly. The generator therefore
    selects `candidates AS MATERIALIZED (... ORDER BY vecdsl_distance LIMIT limit+offset)` and sorts the page by distance and key
    outside the CTE; `exact` adds the key to the inner ORDER BY, which forces the exact scan. **[pgvector: agrees]** (a `hits` CTE)
29. **OFFSET is applied outside the index scan.** Measured on 2.0.0: the vss top-k rewrite drops the OFFSET (`LIMIT 2 OFFSET 1`
    returned the first two rows), so the inner CTE fetches `limit + offset` rows and a second CTE cuts the page with
    `ORDER BY vecdsl_distance, key LIMIT n OFFSET m`. **[pgvector: diverges]** (OFFSET in place)
30. **Filtered search.** vss commit aa39a73 (3 Sep 2026) added `hnsw_prefilter`, on by default, which applies the filter before
    the graph search (VSS `README.md`, "Pre-filtered searches"; the DuckDB documentation page does not mention it yet). Measured at
    5000 rows: with prefilter every filtered search returned its full k; with `hnsw_prefilter = false` a filtered search returned
    1 of 2 hits. `filter-strategy post_filter` sets it to false (and accepts underfilled pages), `pre_filter` and `auto` keep the
    default, `iterative` is unsupported. **[pgvector: diverges]** (`hnsw.iterative_scan`)
31. **Effort.** `candidates` sets `hnsw_ef_search` (at least limit plus offset); without it the generator raises ef_search only
    when the page needs more than the index's ef_search. `target-recall` (D15) is unsupported: ef_search is an effort setting.
    `probes` and `scan-budget` are unsupported, `timeout`, `oversampling` and `rescore` are listed as ignored.
    **[pgvector: agrees]** **[LanceDB: agrees]**
32. **Keyword search** is `fts_<schema>_<table>.match_bm25(key, text, fields := ..., field_weights := MAP {...}, conjunctive := 1)`
    (DOCS `core_extensions/full_text_search.html`): operator and is `conjunctive := 1`, field boosts are bm25f field weights;
    phrase, slop, fuzziness and min-should-match are unsupported, because the index keeps no term positions (table `terms`:
    termid, docid, fieldid) and matches whole stemmed terms. **[pgvector: diverges]** (tsquery phrases) **[LanceDB: diverges]**
    (PhraseQuery, fuzziness)
33. **Hybrid search** runs each leg as a CTE, ranks it with `row_number()` and fuses in SQL: rrf (`1 / (k + rank)`), weighted
    (weights or alpha over canonical similarities) and relative_score (min-max per leg), ties by key. **[pgvector: agrees]**
    **[LanceDB: diverges]** (fusion in the runtime)
34. **Recommend** averages the positive and negative vectors element-wise in SQL (`unnest` with `generate_subscripts`) and
    searches `2 * avg(positive) - avg(negative)` (`list_transform(list_zip(p, n), lambda z: 2 * z[1] - z[2])`), excluding the
    examples. **[pgvector: agrees]**
35. **Scroll, sample, distance matrix, get and count.** Scroll orders by key with keyset cursors; slices are
    `CAST('0x' || substr(md5(key::VARCHAR), 1, 7) AS BIGINT) % n`; sample and distance matrix use
    `USING SAMPLE reservoir(n ROWS) REPEATABLE (42)`, which returned the same rows across connections and languages (measured;
    DOCS `sql/samples.html`). **[pgvector: agrees]** (md5 slices) **[LanceDB: diverges]** (random samples)
36. **Not generated yet**: `within` on vector searches and the element predicate `any` of filters, for the reason
    pgvector and Oracle give: a runnable statement needs a field of struct elements with record literals that the
    frozen validator accepts; and the k and b arguments of `match_bm25`
    (BM25 parameters on an analyzer; DOCS `core_extensions/full_text_search.html` lists `k := 1.2, b := 0.75`). Notes 37 to 52
    record the mappings that followed. **[pgvector: agrees]** (within pending) **[LanceDB: agrees]**

## Result shapes, imports, clones and snapshots

37. **One pipeline for result shapes**, ported from pgvector (its MAPPING.md note 25): a search with
    any of cursor, boost, mmr, group-by, with-total, aggregate or sort scores its candidates in a CTE `scored` (key, reported
    score, `ord` ascending from the best, `rel` the canonical D3 relevance), then `boosted`, `cursor_row` and `after_cursor`,
    `pool` and `picked` (MMR) or `grouped` and `leaders`, `page`, `hits`, and for totals and aggregations `everything` and
    `matched`. A plain search keeps its plain statement.
    **Divergence from pgvector:** rows rejoin the table by key, not by `(tableoid, ctid)`: a DuckDB table has no partitions and
    the key is its primary key (DOCS `sql/statements/create_table.html`), so the key identifies a row. **[pgvector: diverges]**
38. **Cursor.** The key of the last hit of the previous page. The record after the cursor is written
    term by term, `ord > c.ord OR (ord = c.ord AND key > c.key)`, not as a row comparison: DuckDB compares `(a, b) > (c, d)`
    as STRUCT values in which NULL sorts last, so `(1, NULL) > (1, 5)` is true on 2.0.0 (measured), where PostgreSQL's row
    comparison yields NULL. A cursor record that does not exist makes every comparison NULL and returns no hits, as on pgvector.
    A vector search applies the cursor inside its candidate CTE so that the page is full; the condition on the distance makes
    the plan an exact scan (note 44). **[pgvector: agrees]** (meaning) **[pgvector: diverges]** (comparison form, plan)
39. **With-total and aggregations** count the records the search matches within the score bounds, in their
    own CTE, so the candidate CTE keeps `ORDER BY distance LIMIT n` and the HNSW index (note 44). Aggregates (DOCS
    `sql/functions/aggregates.html`): count, sum, avg, min, max, `count(DISTINCT ...)`, stats, terms and facet (count descending,
    key; `list(... ORDER BY ...)` makes the JSON list deterministic), histogram `floor(v / i) * i`, date histogram
    `time_bucket(interval, v, TIMESTAMP '1970-01-01')` with the origin written because DuckDB's default origin is
    2000-01-03 for widths below a month (DOCS `sql/functions/timestamp.html`), percentiles with `quantile_cont`, "the
    interpolated pos-quantile", at Elasticsearch's percents; `'1d'` and `'12h'` read as intervals (measured). Range: unsupported
   . **[pgvector: agrees]**
40. **Boosts (D8)** with Elasticsearch's function_score defaults (pgvector note 32): `power`, `abs`, `greatest`,
    `coalesce`, `CASE` (DOCS `sql/functions/numeric.html`); dates decay by seconds with `epoch(TIMESTAMPTZ - TIMESTAMPTZ)` and
    `epoch(interval)` (DOCS `sql/functions/interval.html`). DuckDB 2.0.0 has no `num_nonnulls` (measured: Catalog Error), so the
    number of functions that apply is a sum of `CASE WHEN v IS NULL THEN 0 ELSE 1 END`; `greatest` skips NULL arguments
    (measured), as on PostgreSQL. **[pgvector: agrees]** (values)
41. **MMR** as a recursive CTE (pgvector note 33): DuckDB's recursive term "refers to the rows produced by the last
    iteration" (DOCS `sql/query_syntax/with.html`); the picked keys are a list (`list_append`, `list_contains`), the page reads
    them with `unnest(...) WITH ORDINALITY`; a LATERAL subquery with ORDER BY, LIMIT and `max` inside the recursive term runs on
    2.0.0 (measured). Diversity uses the diversity field's distance function. **[pgvector: agrees]**
42. **Group-by and sort** as on pgvector (notes 28 and 29): `row_number() OVER (PARTITION BY key ORDER BY ord, key)` (DOCS
    `sql/functions/window_functions.html`), leaders by their first hit, rows `{group, hits}`; sort keys order the selected page
    `NULLS LAST`, the relevance order breaking ties. **[pgvector: agrees]**
43. **Prefetch stages** are materialized CTEs of keys ordered by distance and key (pgvector note 34); the search ranks
    only the rows of its top-level stages (`key IN (...)`); a stage's weight and params are listed as ignored. **[pgvector: agrees]**
44. **Plans and results, measured** (EXPLAIN at 5000 random rows): the HNSW index
    serves with-total (counted), sort (by_price), MMR (diverse) and the cursor-less hybrid legs; the cursor (page_after,
    suggest_after), boosts, groups, prefetch stages and exact legs plan exact scans. Every statement returned the same hits as
    the exact search with the index dropped. **[pgvector: diverges]** (pgvector 0.8 serves filtered scans iteratively)
45. **Hybrid legs.** A leg's filter and the search's filter both hold (their conjunction); leg bounds are a condition on the
    leg's canonical similarity after the leg's limit, so a fused record gets a leg's contribution only within that leg's bounds;
    an exact leg orders its CTE by distance and key (the exact form of note 28). **[pgvector: diverges]** (pgvector raises leg bounds as pending)
46. **Imports read by DuckDB.** csv and parquet imports, and every import inside a transaction (json too), are one
    `INSERT INTO t (declared columns) SELECT f.<columns> FROM read_csv($1) f` (`read_parquet`, `read_json`) with the path bound:
    DuckDB reads the file relative to the working directory ("The path can either be a relative path (relative to the current
    working directory) or an absolute path", DOCS `data/csv/overview.html`) and detects header and types; JSON and CSV text
    cast to `FLOAT[n]` and `VARCHAR[]` columns on insert (measured). A json import outside a transaction keeps the runtime
    path. Remote locations are unsupported: httpfs "will be, by default, autoloaded on first use" (DOCS
    `core_extensions/httpfs/overview.html`) from the network, and S3, GCS and Azure need a secret with credentials, which the
    generated programs do not create. **[pgvector: diverges]** (COPY FROM a server file is unsupported there)
47. **Derived collections** (`derived-from`) are tables filled by the reader of their file (by format, else by suffix) before
    their indexes are built, in one transaction; `refresh` deletes the rows and reads the file again. Not a view: DuckDB refuses
    an index on a view ("can only create an index on a base table") and `create_fts_index` on it ("w is not an table"), both
    measured on 2.0.0, so a view would lose the HNSW and fts indexes. lance, iceberg and vortex sources: unsupported (readers the
    programs do not load). **[pgvector: diverges]** (file_fdw: unsupported)
48. **Deployment clones** (`clone database`): the deployment is the database file the program attached as `vecdsl`; the target
    file is attached and filled by `COPY FROM DATABASE vecdsl TO vecdsl_clone`, which "copies the entire content ... including
    constraints, indexes, sequences, macros and the data itself" (DOCS `sql/statements/copy.html`), and detached after the
    commit: DETACH inside the transaction fails ("Cannot detach database ... because the current transaction has outstanding
    work on it", measured). The target path is relative to the working directory; a program run on `:memory:` has no
    `vecdsl` database to copy. **[pgvector: diverges]** (unsupported there)
49. **Tenant snapshots** copy the tenant's rows (`CREATE TABLE AS SELECT ... WHERE tenant column = 'tenant'`); a restore of a
    snapshot taken with a tenant earlier in the same script deletes and restores that tenant's rows only (the generator knows
    the snapshot by name); a snapshot taken by another program restores the whole collection. **[pgvector: diverges]**
50. **Unsupported, each with its reason** (were "pending"): geo fields, predicates and payload indexes (the spatial extension
    "is not autoloadable", DOCS `core_extensions/spatial/overview.html`, and the programs load vss and fts only); a payload index
    on a path below a JSON field (DuckDB 2.0.0 refuses `->>` and `json_extract_string` in index expressions, measured); the fuzzy
    text operator (`levenshtein` and `jaro_winkler_similarity` compare whole strings, DOCS `sql/functions/text.html`); custom
    functions (no computation to put in a generated column); auto-id (the runtimes report changed row counts, not assigned keys);
    dynamic (fixed typed columns); cross-ref (no field holds the reference, so no FOREIGN KEY column).
51. **Hostile values** in the new shapes stay parameters: the cursor key, decay origins, scales and offsets, histogram and date
    intervals, file paths and tenant names in restores are bound; aggregation names and snapshot tenant names are vecDSL
    identifiers written as SQL string constants with quotes doubled (DuckdbGeneratorTest#bindsHostileValuesAsParameters).
52. **Expected values** of the result-shape statements of the full sample are computed from their definitions (float32
    similarities), with the BM25 scores of the keyword baseline `termsBase` and every shape computed on them.

## Known limitations

- `hnsw_enable_experimental_persistence`: a crash with uncommitted HNSW changes can lose or corrupt the
  index (vss documentation, https://duckdb.org/docs/2.0/core_extensions/vss.html).
- The fts index is rebuilt after every write transaction, a full index build each time; fine for small
  tables, slow for large ones.
- vecDSL 1.0 has no `duckdb` vendor id: programs for DuckDB write no `targets` clause and no `extension duckdb` block.
- A cursor inside the vector candidate CTE makes the plan exact.
- Spatial constructs, remote imports, auto-id, dynamic and cross-ref fields are unsupported.
