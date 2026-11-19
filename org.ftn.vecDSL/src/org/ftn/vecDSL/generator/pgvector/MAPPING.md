# vecDSL to pgvector: mapping notes

pgvector is the first target where vecDSL meets a general-purpose query language instead of a vendor SDK. Most of the
grammar maps onto plain SQL; concealment is needed where vecDSL names a concept that PostgreSQL expresses as a table
design (tenants, namespaces, partitions, aliases) or as a combination of statements (hybrid search, clone, swap-alias).
COVERAGE.md lists every construct with its status; these notes explain the rows. Every claim cites
the PostgreSQL 18 manual (`https://www.postgresql.org/docs/18/...`) or the pgvector 0.8.7 README
(`https://github.com/pgvector/pgvector/blob/v0.8.7/README.md`) unless another source is named.

## Versions (checked 2026-11-11)

| part | version | source |
|---|---|---|
| server image | `pgvector/pgvector:0.8.7-pg18-trixie`, digest `sha256:9d9c930220cb9bf2f956d10a8f909cf9973d9672ca0278aef2c1e6facccad2e0` | https://hub.docker.com/r/pgvector/pgvector/tags |
| pgvector | 0.8.7 | https://github.com/pgvector/pgvector/tags |
| PostgreSQL | 18 | https://www.postgresql.org/docs/18/release-18.html |
| Python driver | `psycopg[binary]` 3.3.8, pinned `>=3.3.8,<3.4` | https://pypi.org/project/psycopg/ |
| TypeScript driver | `pg` 8.23.1 with `@types/pg` 8.23.1, TypeScript 5.9.3, `@types/node` 22.20.5 | https://www.npmjs.com/package/pg |
| Java driver | `org.postgresql:postgresql` 42.7.14, Gson 2.13.2, release 17 | https://jdbc.postgresql.org/changelogs/ |

## Notes

1. **Where the SQL lives.** All SQL text is built at generation time by `PgvectorSql.java` and `PgvectorQueries.java`;
   the Xtend classes translate the model into descriptors (`Table`, `Column`, `Vec`, `Cond`) and the three runtimes only
   execute. The generated programs therefore contain the exact statements the thesis discusses, and the three languages
   send byte-identical SQL (checked with a recording JDBC driver and the psycopg and node-postgres logs).

2. **Values are always parameters.** Every literal and every script parameter is a numbered placeholder `$n` with an
   explicit cast (`$3::vector`, `$4::jsonb`). Values travel as text: lists and objects as JSON text, numbers and booleans
   in JSON form, null as SQL NULL. node-postgres sends `$n` as written; psycopg receives `%(pN)s` (literal `%` doubled);
   JDBC receives `?` with one binding per occurrence. Identifiers are quoted with `"` and embedded quotes doubled
   (`sql-syntax-lexical.html`, 4.1.1). PostgreSQL has no placeholders for identifiers, so names are the only text the
   generator writes into SQL, and they come from the script, never from runtime input.

3. **One json column per row.** Each query returns `json_build_object(...)` or `to_json(...)` rows, so the runtimes need
   no type mapping per column and the three languages print the same JSON (`functions-json.html`). Writes return their
   counts from a data-modifying CTE (`WITH w AS (INSERT ... RETURNING 1) SELECT json_build_object('insert_count',
   count(*)) FROM w`, `queries-with.html` 7.8.4).

4. **Transactions.** Every generated method runs in one transaction (`BEGIN [ISOLATION LEVEL ...]; ...; COMMIT`, ROLLBACK
   on error) on a connection in autocommit mode. Query settings are set with `SELECT set_config(name, value, true)`,
   which is `SET LOCAL` (`functions-admin.html`, 9.28.1), so a setting never outlives its statement. The vecDSL
   `transaction { ... }` block becomes one such transaction around all its statements; its isolation level maps to
   `READ COMMITTED`, `REPEATABLE READ` or `SERIALIZABLE` (`sql-set-transaction.html`). Statements that PostgreSQL refuses
   inside a transaction block run through `autocommit`: `CREATE DATABASE`, `DROP DATABASE ... WITH (FORCE)`, `VACUUM`,
   `CREATE INDEX CONCURRENTLY`, `REINDEX ... CONCURRENTLY` (each manual page states the restriction).

5. **Collections are tables.** The primary key is the table's primary key; payload fields become typed columns (`int`
   integer, `bigint` bigint, `int8` smallint, since PostgreSQL has no one-byte integer, `float` real, `double` double
   precision, `varchar(n)`, arrays as `elem[]`, json, map, struct and nested values as `jsonb`; `datatype.html`). Payload
   columns are nullable because vecDSL payload is optional; vector columns are `NOT NULL` unless the field is declared
   nullable. Field limits become named `CHECK` constraints. The description becomes `COMMENT ON TABLE`.

6. **Vector types.** `vector(n)` for dense float vectors, `halfvec(n)` for float16, `sparsevec(n)` for sparse vectors and
   `bit(n)` for binary vectors (README, "Half-Precision Vectors", "Binary Vectors", "Sparse Vectors"). Sparse literals are
   written `{i:w,...}/n` with 1-based indexes (README, "Sparse Vectors"), while vecDSL indexes from 0, so the generator
   adds 1. Bit lists become bit strings. The dimension limits (2,000 for indexed `vector`, 4,000 for `halfvec`, 64,000 for
   `bit`, 1,000 non-zero elements for `sparsevec`) are the README's and are reported by the server.

7. **Metrics and scores (D3).** cosine `<=>`, dot `<#>` (negative inner product), euclidean `<->`, manhattan `<+>`, hamming
   `<~>`, jaccard `<%>` (README, "Querying"). vecDSL scores grow with similarity, so cosine and jaccard return `1 - d` and
   the others `-d`; `native-scores` returns the distance itself. Euclidean is the distance, not its square, because that
   is what `<->` returns.

8. **Indexes.** HNSW and IVFFlat with the operator class of the metric (`vector_cosine_ops`, `halfvec_l2_ops`,
   `bit_hamming_ops`, ...; README, "Indexing"). `m`, `ef_construction` and `lists` are index storage parameters;
   `search-candidates` becomes the session setting `hnsw.ef_search`, `probes` becomes `ivfflat.probes`. Index names are
   `<collection>_<index>` because PostgreSQL scopes index names to the schema, not the table (`sql-createindex.html`);
   `list indexes` strips the prefix again.

9. **Quantization.** Scalar quantization to float16 is an expression index on `(col::halfvec(n))`, binary quantization an
   expression index on `binary_quantize(col)::bit(n)` (README, "Indexing Subvectors", "Binary Quantization"). Searches on a
   quantized index read `limit * oversampling` candidates (default 4) through the index expression and re-rank them by
   the full vector, the README's re-ranking pattern. Product quantization is unsupported: pgvector has none.

10. **Search.** A vector search is a materialized CTE `hits` with `ORDER BY distance LIMIT n OFFSET m`; score bounds and
    the projection apply outside it and the final order is `ORDER BY h.distance + 0, key` (the `+ 0` keeps the planner
    from pushing the order back into the index scan and makes ties deterministic). `hnsw.ef_search` is raised to at least
    `limit + offset`, capped at 1000 (README, "Query Options": the HNSW scan returns at most `ef_search` rows).

11. **Filtered search.** With a filter, the generator turns on `hnsw.iterative_scan = strict_order` or
    `ivfflat.iterative_scan = relaxed_order` (README, "Iterative Index Scans", pgvector 0.8.0 and later), so a selective
    filter still returns `limit` rows. `exhaustive`, `exact` and `filter-strategy pre_filter` set `enable_indexscan` off,
    which makes the planner scan exactly (`runtime-config-query.html`).

12. **Filters.** vecDSL conditions become SQL predicates on columns; nested paths on jsonb columns use `->` and `->>` with
    casts; `contains-any` and `contains-all` on arrays use `&&` and `@>` (`functions-array.html`); `like` passes its
    pattern to `LIKE`, and the wildcard match escapes `\`, `%` and `_` and translates `*` and `?`
    (`functions-matching.html`). Comparisons with null are unsupported because SQL three-valued logic would make them
    silently false; `is null` and `is not null` are native.

13. **Keyword search.** PostgreSQL full-text search: `to_tsvector(config, coalesce(col, '')) @@ plainto_tsquery(config, q)`
    ranked by `ts_rank` (`textsearch-controls.html`); `phrase` uses `phraseto_tsquery`, `any` joins the terms with `|`.
    Several fields add `boost * ts_rank` per field. A full-text index is a GIN expression index on the same `to_tsvector`
    expression, so the planner can use it. An analyzer becomes `CREATE TEXT SEARCH CONFIGURATION ... (COPY =
    pg_catalog.<language>)` (`sql-createtsconfig.html`). BM25 is unsupported: `ts_rank` is not BM25 and the image ships no
    BM25 extension (`https://github.com/docker-library/postgres`; ParadeDB `pg_search` is a separate image).

14. **Hybrid search.** Each leg is a CTE (`l0`, `l1`, ...) ranked with `row_number()`; the fusion runs in SQL: `rrf`
    sums `1 / (k + rank)`, `weighted` sums the weighted leg scores, `relative_score` min-max normalizes each leg first.
    Ties break by key. Leg weights follow the Pinecone generator's `legWeights` (alpha gives alpha and 1 - alpha) so the
    two targets agree on the same script.

15. **Tenants, namespaces and partitions are columns.** PostgreSQL has none of the three as a concept for rows. Tenants
    and namespaces become the hidden columns `vecdsl_tenant` and `vecdsl_namespace` (or the field marked
    `partition-key`), added when the collection declares them or any statement in the script uses them, and every
    statement on the collection filters by them. Partitions become real declarative partitions: `vecdsl_partition` with
    `PARTITION BY LIST`, one table `<table>__<name>` per partition and a `default` partition
    (`ddl-partitioning.html`); the primary key becomes `(key, vecdsl_partition)` because a primary key on a partitioned
    table must include the partition key. Dropping a partition is `DROP TABLE` of the partition table.

16. **Roles.** `CREATE ROLE ... NOLOGIN`, `GRANT` on the table for each privilege, and for dropping, `DROP OWNED BY` before
    `DROP ROLE` (`sql-droprole.html`: a role with privileges cannot be dropped). Roles are created without a login and never with a
    password, so no secret reaches the generated code.

17. **Aliases are views.** `CREATE VIEW alias AS SELECT * FROM table`; swap-alias is `DROP VIEW` plus `CREATE VIEW` in one
    transaction, so readers see either the old or the new target (`sql-createview.html`; DDL is transactional,
    `https://wiki.postgresql.org/wiki/Transactional_DDL_in_PostgreSQL:_A_Competitive_Analysis`).

18. **Schema evolution.** An alter changes the table in one transaction (`ALTER TABLE ... ADD COLUMN, DROP COLUMN, RENAME
    COLUMN, RENAME TO`). Later statements in the same script use the evolved table: the generator keeps the current
    `Table` per collection, and `Table.renames` maps declared field names to current column names, so a query written
    against a renamed field reads the new column.

19. **Lifecycle.** `load` runs `pg_prewarm` on the table, its partitions and indexes (`pgprewarm.html`); `release` runs
    `pg_buffercache_evict_relation`, added in PostgreSQL 18 (`pgbuffercache.html`); `flush` is an existence check because
    a committed transaction is already durable with `synchronous_commit = on` (`wal-async-commit.html`); `compact` is
    `VACUUM (ANALYZE)` (not `VACUUM FULL`, which takes an exclusive lock); `clone` is `CREATE TABLE ... (LIKE ...
    INCLUDING ALL)` plus `INSERT ... SELECT` in one transaction; `await-index` reads `pg_index.indisvalid` and
    `indisready` once and reports `ready` or `building`.

20. **Writes.** Rows travel as one jsonb parameter read through `jsonb_array_elements` and `jsonb_to_record`
    (`functions-json.html`). A column with a default uses `CASE WHEN jsonb_exists(e.doc, 'col') THEN ... ELSE default`, so
    an absent field takes the default and an explicit null stays null. Conflicts map to `ON CONFLICT DO NOTHING`
    (reported as `skipped`) or `DO UPDATE SET col = EXCLUDED.col` (`sql-insert.html`). DELETE has no LIMIT, so a delete
    with a limit selects `(tableoid, ctid)` of the first n targets in key order.

21. **Bulk import.** `import` reads JSON Lines or JSON files in the runtime and sends them through the same insert
    statement in batches. Parquet and other binary formats are unsupported: core PostgreSQL reads text, CSV and binary COPY
    only (`sql-copy.html`).

22. **Database and catalog.** `create database` is `CREATE DATABASE`; listing reads `pg_database`, `pg_class`, `pg_index`,
    `pg_inherits` and `pg_views` (`catalogs.html`). A statement on another database runs on a connection whose URI names
    that database, since a PostgreSQL connection is bound to one database (`libpq-connect.html`).

23. **Every connection loads pgvector.** The runtimes run `CREATE EXTENSION IF NOT EXISTS vector` once per connection, so
    a fresh database works without a setup step (README, "Getting Started").

24. **What stays unsupported.** Embedding and generative models, reranking models, TTL, snapshots, deletion protection,
    storage tiers, tags, product quantization, version checks, as-of reads and replication and sharding settings have no
    counterpart in PostgreSQL with the pgvector image. Each guard message names the reason and COVERAGE.md gives the
    source. Clauses that SQL can express but the generator does not map yet raise "has no pgvector mapping yet" with the
    SQL construct that would implement them; The rows marked "generation pending" are (only `within`
    among the pending rows; notes 25 to 36 describe the clauses mapped then).

## Result shapes and the remaining clauses

Facts checked against the PostgreSQL 18 documentation and the sources at the tag REL_18_6 (the server version of
`pgvector/pgvector:0.8.7-pg18-trixie`), and the pgvector sources at v0.8.7. Every construct below uses core
PostgreSQL and the pgvector distance operators; no extension beyond `vector` is needed, so nothing depends on the contrib
modules of the image.

25. **One pipeline for result shapes.** A search with any of cursor, boost, mmr, group-by, with-total, aggregate, highlight
    or sort is built by `PgvectorQueries.shaped`: the search scores its candidates in a CTE `scored` (key, row id
    `(tableoid, ctid)`, reported score, `ord` ascending from the best, `rel` the canonical D3 relevance), then optional
    stages follow in a fixed order: `boosted`, `after_cursor`, `pool` and `picked` (MMR) or `grouped` and `leaders`, `page`,
    `hits`, and for totals and aggregations `everything` and `matched`. A plain search (none of the clauses) keeps the
    statement it had before, byte for byte (`PgvectorQueries.vectorSearch(q, s, shape)` returns `vectorSearch(q, s)`).
    Rows are joined back by `(tableoid, ctid)`, which identifies a row of a partitioned table too, where the key alone is
    unique only per partition (`ddl-system-columns.html`).

26. **Cursor (keyset pagination).** vecDSL's `cursor` is a string; the generator reads it as the key of the last hit of the
    previous page, the convention of the Elasticsearch generator's `search_after`. Row constructor comparison
    `(a, b) > (c, d)` is lexicographic (`functions-comparisons.html`, "Row Constructor Comparison"), so a vector search adds
    `(distance, key) > (SELECT ord, key FROM cursor_row)` inside its candidate CTE, a keyword search `(-rank, key) > ...`;
    after fusion or boosts the condition applies to the scored rows. A cursor whose record does not exist compares with
    NULL and returns no rows. A scroll compares the sort keys one by one with nulls last: for each key, the earlier keys
    are `IS NOT DISTINCT FROM` the cursor record's and this one is after it (`>` ascending, `<` descending, or NULL when the
    cursor's value is not NULL), then the key itself.

27. **With-total.** `count(*)` of the records the search matches (`functions-aggregate.html`), computed in its own CTE
    `everything` without the limit, so the nearest-neighbour CTE keeps its `ORDER BY ... LIMIT` and can use the index. A
    window `count(*) OVER ()` in the candidate CTE would be evaluated over every row before the LIMIT and force a full scan
    (`queries-table-expressions.html`, "Window Function Processing"; `sql-select.html`, Description: output rows, step 5,
    before ORDER BY and LIMIT, steps 8 and 9).

28. **Sort clauses.** The search selects its hits by relevance, then `ORDER BY` the sort keys over the page
    (`sql-select.html`, ORDER BY with NULLS LAST), the relevance order breaking ties. Sub-fields sort by their typed value
    (`#>>` with a cast, as in filters, note 11).

29. **Group-by.** `row_number() OVER (PARTITION BY key ORDER BY ord, key)` numbers the hits of each group
    (`functions-window.html`); the top groups are the groups whose first hit ranks highest (`leaders`, limit and offset
    count groups), each with at most `size` hits (1 by default). Grouping ranks every matching record, not an index
    candidate list, so a group that has a match is never missed. Rows are `{group, hits}`, the shape of the Qdrant and
    Elasticsearch generators.

30. **Aggregations.** Over the matching records (`matched`): `count`, `sum`, `avg`, `min`, `max`, `count(DISTINCT ...)`,
    `percentile_cont(p) WITHIN GROUP (ORDER BY v)` (`functions-aggregate.html`, ordered-set aggregates), terms as grouped
    counts (array columns through `unnest`, `functions-array.html`), histograms as `floor(v / interval) * interval`, date
    histograms with `date_bin(interval, v, timestamp '1970-01-01')` (`functions-datetime.html`; `pg_proc.dat` at REL_18_6
    lists `date_bin` for timestamp and timestamptz). Interval strings such as `1d` and `12h` are PostgreSQL interval input
    (checked on the server: `'1d'::interval` is `1 day`). Percentiles use Elasticsearch's default percents 1, 5, 25, 50,
    75, 95, 99 (https://www.elastic.co/docs/reference/aggregations/search-aggregations-metrics-percentile-aggregation), so
    the result keys agree with the Elasticsearch target. A range aggregation needs range bounds, which `AggregationClause`
    does not carry.

31. **Highlights.** `ts_headline(config, document, query)` marks the words of a document that match a tsquery
    (`textsearch-controls.html`, "Highlighting Results"); its default markers are `<b>` and `</b>` (`wparser_def.c` at
    REL_18_6, `prs->startsel = pstrdup("<b>")`). The tsquery is the one the search matches (the keyword input, or the OR of
    the keyword legs of a hybrid search, `||` on tsquery). A vector or recommend search has no query text, so there is
    nothing to mark: unsupported.

32. **Boosts (D8).** Computed in SQL with `power`, `abs`, `greatest`, `coalesce`, `CASE` and `num_nonnulls`
    (`functions-math.html`, `functions-conditional.html`, `functions-comparison.html`). The decay curves are those of the
    Elasticsearch function_score query with its defaults (rate 0.5 at distance scale, offset 0; gauss
    `rate^((d/scale)^2)`, exp `rate^(d/scale)`, linear `max(0, 1 - d(1 - rate)/scale)`;
    https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-function-score-query), and combine and apply
    default to multiply, as its `score_mode` and `boost_mode` do. Dates decay by seconds (`extract(epoch FROM ...)`,
    `functions-datetime.html`). Boosts rank every matching record (note 29's reasoning).

33. **MMR.** Maximal marginal relevance (Carbonell and Goldstein, SIGIR 1998) as a recursive CTE: each step picks, from
    the candidate pool, the record maximizing `lambda * rel - (1 - lambda) * max(similarity to the picked records)`, with
    the D3 similarity of the diversity field's metric (pgvector operators, README "Querying"). PostgreSQL forbids a
    recursive reference inside a subquery (`parse_cte.c` at REL_18_6, "recursive reference to query ... must not appear
    within a subquery") and aggregates, ORDER BY and LIMIT at the level of the recursive term (`parse_agg.c`,
    `parse_cte.c`); the generated term references only the columns of the previous row (`p.keys`) from a LATERAL
    subquery, whose own ORDER BY, LIMIT and `max` belong to that subquery. The pool is `candidates`, else ten times limit
    plus offset (the Elasticsearch generator's window); hits keep their own scores, in pick order.

34. **Prefetch stages.** Each stage is a materialized CTE of the row ids `(tableoid, ctid)` nearest to its own input,
    vector field, metric and filter (`queries-with.html`, MATERIALIZED), restricted to the rows of its nested stages; the
    search ranks only the rows of its top-level stages, `(t.tableoid, t.ctid) IN (...)`. This is the semantics of Qdrant's
    prefetch (the outer query re-scores the union of the prefetched candidates). PostgreSQL sets search parameters per
    statement (`SET LOCAL`), so a stage's own `params` and `weight` are listed as ignored.

35. **Scroll slices.** A slice is the set of records whose key text's `md5` (`functions-string.html`) begins, in its first
    28 bits, with a value congruent to the slice index modulo the slice count; `('x' || hex)::bit(28)::integer` converts the
    hex digits (`functions-bitstring.html`, casting bit strings to integer). The slices are disjoint, cover the
    collection, and are the same on every server.

36. **Update from records.** `UPDATE ... FROM jsonb_array_elements(records) ... jsonb_to_record` joins each record to
    its row by key (`sql-update.html`, the FROM clause; `functions-json.html`). Merge keeps a column the record does not
    carry (`jsonb_exists`); replace resets the record's other payload columns to their defaults, as `set` with mode
    replace does. A record without a stored row updates nothing; if two records carry the same key, PostgreSQL applies
    one of them, and which one is not predictable (`sql-update.html`, Notes).

## Known limitations

- BM25 is unsupported; keyword search ranks with `ts_rank`.
- Groups and boosts rank every matching record, which scans every match.
- `within` (element scope) is not generated.
- On Windows, `localhost` resolves to `::1` first; the samples and the runtimes use `127.0.0.1` with a 10-second
  connection timeout.
- `release` needs `pg_buffercache_evict_relation`, which exists from PostgreSQL 18.
