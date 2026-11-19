# vecDSL to Oracle AI Database 26ai: mapping notes

Oracle is the second SQL target after pgvector, and the generator follows the pgvector design: the Xtend classes translate the
model into descriptors (`Table`, `Column`, `Vec`, `Cond`), `OracledbSql.java` and `OracledbQueries.java` write all SQL at
generation time, and the three runtimes only execute it. This file says where Oracle agrees with pgvector and numbers every
divergence (O1, O2, ...) so that COVERAGE.md can cite them. Facts cite the references at the end of this file by id (R1 to R68); COVERAGE.md gives the row
status of every construct.

## Versions (checked 2026-11-14)

| part | version | reference |
|---|---|---|
| server image | `container-registry.oracle.com/database/free:23.26.3.0`, image id `cdf2f86bedfa`, no registry login | R19 |
| documentation | Oracle AI Database 26ai, Release 26 books (release update 23.26.4) | R1 to R18, R23 to R54 |
| Python driver | `oracledb` 26.1.0, pinned `>=26.1.0,<27`, Python 3.10 or later, Thin mode | R20, R22 |
| TypeScript driver | `oracledb` 26.0.0 with `@types/oracledb` 26.0.1, TypeScript 5.9.3, `@types/node` 22.20.5 | R21 |
| Java driver | `com.oracle.database.jdbc:ojdbc17` 23.26.4.0.0, Gson 2.13.2, release 17 | R37 |

## Where Oracle agrees with pgvector

- All SQL is written by the generator; the three languages send identical statement text and binds (checked with
  recording fake drivers).
- Every value is a bind variable; identifiers are the only text taken from the script, always quoted.
- Each query returns one JSON text per row, so the runtimes need no per-column type mapping.
- Collections are tables, fields are columns, the primary key is a primary key; tenants and namespaces are hidden columns
  (`vecdsl_tenant`, `vecdsl_namespace`) filled from the statement's slice and added to every filter; partitions are list
  partitions on a hidden `vecdsl_partition` column; aliases are views; clone is a copy of the table.
- Hybrid search is SQL fusion over common table expressions with the same three fusions (rrf, weighted, relative_score) and
  the same canonical scores (D3); recommend averages the positive and negative examples into one query vector.
- Writes: insert, upsert with ignore, update or replace policies, update by ids or filter, delete by ids, filter or all,
  transactions with read committed or serializable isolation, bulk import from local JSON or JSONL files in batches.
- Unsupported for the same reason: embedding models, generative models, rerankers, snapshots, ttl, api keys.

## Divergences

O1. **Positional binds.** Oracle binds are named or positional placeholders `:1 .. :n` (R5 shows `:query_vector`). The
generator numbers them in textual order and binds each occurrence once (`OracledbSql.finish` renumbers and reorders); a
value used twice, such as the query vector in the select list and the ORDER BY, is bound twice. python-oracledb and
node-oracledb take `:n` as written; the Java runtime rewrites `:n` to `?` outside quoted text for JDBC. pgvector uses `$n`
with casts.

O2. **No casts; JSON text as CLOB.** Lists and objects are bound as JSON text with the driver's CLOB type and read with
`JSON_TABLE` (R42) or `JSON(...)`; vectors are converted with `TO_VECTOR(:n, dim, format[, SPARSE])` (R25). Python sets
`DB_TYPE_CLOB` with `setinputsizes`, Node binds `{type: oracledb.DB_TYPE_CLOB, val}`, Java calls `setClob`.

O3. **Rows as JSON.** Rows come back as `JSON_OBJECT(... NULL ON NULL RETURNING CLOB)` (R26) and scalars as
`JSON_SERIALIZE(JSON_SCALAR(x) RETURNING CLOB)` (R44); vectors in results are `FROM_VECTOR(v RETURNING CLOB FORMAT DENSE)
FORMAT JSON` (R24). pgvector uses `json_build_object` and `to_json`.

O4. **Identifiers keep vecDSL case.** Quoted identifiers are case-sensitive and may be reserved words; unquoted ones are
stored in upper case (R23). The generator quotes every table, column and index name, rejects names over 128 bytes, names
containing `"`, and the column name `ROWID` (R23). Plain catalog queries therefore compare with the exact vecDSL name.

O5. **No IS NOT TRUE.** pgvector writes negation as `(...) IS NOT TRUE` so that NULL counts as not matching. The Oracle
BOOLEAN test condition page could not be read ("Reference pending" below), so negation and `not_in` are written as
`CASE WHEN cond THEN 1 ELSE 0 END = 0`, which gives the same three-valued result, and `neq` as `DECODE(ref, :v, 1, 0) = 0`,
where DECODE treats two nulls as equal (R41).

O6. **Logical databases are table-name prefixes.** pgvector creates a PostgreSQL database per vecDSL logical database.
An Oracle schema is a user, and creating users or tables in other schemas needs `CREATE USER` and `CREATE ANY TABLE`
privileges (R31) that the generated program's user should not hold; PDBs are created by a DBA. A collection `c` of database
`d` is therefore the table `d__c`; creating the database creates its collections, listing databases lists the prefixes,
and dropping the database drops the prefixed tables in a PL/SQL loop.

O7. **DDL commits.** Oracle commits each DDL statement implicitly, so a method that creates a table and its indexes is not
atomic; every DDL step is guarded (`IF NOT EXISTS` (R33, R39), or an existence query on `USER_INDEXES` (R18),
`CTX_USER_PREFERENCES` (R48), `USER_ROLE_PRIVS` (R49)) so that a rerun completes it. pgvector wraps DDL in the method's
transaction.

O8. **Vector types.** `VECTOR(dim, FLOAT32 | FLOAT64 | INT8 | BINARY [, SPARSE])` (R2): float_vector is FLOAT32,
float64_vector FLOAT64, int8_vector INT8, binary_vector BINARY with the bits packed into bytes, most significant bit first
(R36), sparse_float_vector `FLOAT32, SPARSE` with the textual form `[dims, [indices], [values]]` and 0-based indices: the server (23.26.3) reads `[5,[2,4],[10,20]]` as `[0,0,10,0,20]`, although the R1 example describes 1-based indices, so vecDSL indices are passed unchanged (measured on 23.26.3.0).
float16, bfloat16 and uint8 vectors are unsupported (pgvector has halfvec and lacks float64).

O9. **Approximate search is requested explicitly.** `FETCH APPROX FIRST n ROWS ONLY` asks for the vector index; on Free the
APPROX keyword must be written (R5). `target-recall q` becomes `WITH TARGET ACCURACY ceil(100 q)` and a recall of 1 becomes
`FETCH EXACT` (R5, R8); candidates (HNSW) become `WITH TARGET ACCURACY PARAMETERS (EFSEARCH k)` and probes (IVF)
`NEIGHBOR PARTITION PROBES n` (R6); the `exact` clause becomes FETCH EXACT. A collection's `search-candidates` default is used
when a search gives none. pgvector has no recall target (its capability manifest records target-recall as absent); Oracle
has one, so D15 is native here.

O10. **Vector indexes.** `CREATE VECTOR INDEX ... ORGANIZATION INMEMORY NEIGHBOR GRAPH` for hnsw (NEIGHBORS from max-degree,
EFCONSTRUCTION from build-candidates) and `ORGANIZATION NEIGHBOR PARTITIONS` for ivf (NEIGHBOR PARTITIONS from centroids),
with `DISTANCE` from the field metric (R4). When PARAMETERS are given the generator also writes `WITH TARGET ACCURACY 90`,
following the documented examples (R4). An HNSW index lives in the vector pool, sized by `VECTOR_MEMORY_SIZE` (R9), which the
container setup script sets (O18). JACCARD indexes do not exist (R4); quantization and sparse vector indexes are pending.

O11. **Distance and similarity.** `VECTOR_DISTANCE(col, q, metric)` (R7): COSINE, DOT (the negated dot product), EUCLIDEAN,
MANHATTAN, HAMMING, JACCARD. The canonical similarity (D3) is `1 - d` for cosine and jaccard and `-d` otherwise; `score
native` returns d. The metric written in the query equals the index metric, which keeps the approximate path (R7).

O12. **Full-text search is Oracle Text.** A full-text field gets a CONTEXT index with `SYNC (ON COMMIT)` (R14), so a
committed write is searchable at once. Keyword search is `CONTAINS(col, :q, label) > 0` ranked by `SCORE(label)`, an integer
from 0 to 100 (R13), not BM25 and not ts_rank. The query text is built in the runtime by `text_query`: only words of
letters, digits and underscores survive, reserved words are wrapped in braces (R12), terms are joined with AND (all), ACCUM
(any) or written as one braced phrase, and `$` stems each term when the field's analyzer stems (O13). No operator of the
user's input reaches CONTAINS; an input without words raises an error instead of matching everything. Fuzzy, wildcard, NEAR
(slop) and query strings are pending.

O13. **Analyzers are lexer preferences.** `create analyzer` creates a BASIC_LEXER preference with `CTX_DDL.CREATE_PREFERENCE`
and sets `INDEX_STEMS` to ENGLISH for an English stemmer (R47); the CONTEXT index names it with `LEXER name`. The preference
name must be a plain identifier because Oracle Text parameter strings are not quoted SQL. Other tokenizers, languages and
filters are pending.

O14. **Arrays and json are JSON columns.** pgvector uses typed arrays and jsonb; Oracle has no array column type, so arrays,
maps, structs and json fields are `JSON` columns (R27). `contains` is `JSON_EXISTS(col, '$[*]?(@ == $v)' PASSING :n AS "v")`
(R28), array length is `JSON_VALUE(col, '$.size()' RETURNING NUMBER)` (R51), and array-append and array-remove rebuild the
array with `JSON_TABLE` and `JSON_ARRAYAGG` (R42, R43) in one UPDATE. A payload index on an array or json field is a JSON
search index (R50); on a sub-field it is a function-based index on `JSON_VALUE` (R52).

O15. **Upsert is MERGE.** pgvector's `INSERT ... ON CONFLICT` becomes `MERGE INTO t USING (rows from JSON_TABLE) ON (key)`
(R30): ignore inserts only when not matched (the skipped count is the batch size minus the inserted rows, computed in the
runtime), update and replace update and insert. Update from records is a MERGE with only the matched branch. Delete with
limit uses `ROWID IN (SELECT ROWID ... FETCH FIRST n ROWS ONLY)` (R34, R54).

O16. **Partitions.** `PARTITION BY LIST ("vecdsl_partition") AUTOMATIC` with the declared partitions and `default` (R39);
adding one is `ALTER TABLE ADD PARTITION`, dropping one `ALTER TABLE DROP PARTITION ... UPDATE INDEXES` (R40). The primary
key stays the declared key alone, so a key value is unique across partitions; pgvector had to widen the key with the
partition column.

O17. **Roles are granted per table.** `create role` creates a database role and grants object privileges on the role's
collection (R46); a role without a collection is unsupported, because privileges on all tables are the ANY system
privileges. pgvector grants on the schema.

O18. **Free edition limits.** Two CPU cores, 2 GB of memory, 12 GB of user data, one installation per environment (R10);
Partitioning, Flashback and In-Memory are available, RAC and Active Data Guard are not (R11). Replication and sharding
therefore stay unsupported. The container setup script creates the user VECDSL and sets `VECTOR_MEMORY_SIZE` with
`SCOPE=SPFILE`, which needs one restart (R9, R19); the environment restarts the container once.

O19. **No server-side embedding.** Oracle can embed text with ONNX models loaded into the database, and hybrid vector
indexes require such a model (R15). Generated programs load no model, so text input, embedding models and hybrid vector
indexes are unsupported, and hybrid search uses SQL fusion (agreement above) instead of a hybrid vector index.

O20. **Timeouts.** Connections use a 10 s connect timeout and a 60 s call timeout by default (python-oracledb
`tcp_connect_timeout`, node-oracledb `connectTimeout`/`callTimeout` (R20, R21), JDBC `DriverManager.setLoginTimeout` and `Statement.setQueryTimeout` of java.sql); a search
`timeout` sets the call timeout of that statement.

O21. **Index state.** `await-index` and index readiness read `USER_INDEXES.STATUS` and `DOMIDX_OPSTATUS` (R18); `VALID`
on both is ready. pgvector reads `pg_index.indisvalid`.

## Result shapes and the remaining clauses

O22. **Result shapes as stages, as on pgvector.** Cursor, boosts, MMR, groups, total, aggregations, highlights and sort keys are
CTEs after the scored candidates (`OracledbQueries.shaped`), the design of the pgvector generator; a plain search keeps its
statement byte for byte. Rows rejoin the table by `ROWID` (R54), where pgvector uses `(tableoid, ctid)`.

O23. **Each value bound once changes the shape of a few stages.** Oracle placeholders are bound once each (O1), so an
expression with bind variables cannot be written twice. Consequences: (a) with-total and aggregations rank every match instead of
counting in a second scan with the filter repeated (the pgvector generator counts separately); (b) each boost function is a column
computed once and the combination reads the columns; (c) a keyword search with a cursor ranks every match and continues after the
cursor record in the scored set; (d) a vector search with a cursor writes the distance once per use with fresh binds; (e) the
histogram interval is bound once in a one-row join.

O24. **No row-value ordering, no IS NOT DISTINCT FROM, no NUM_NONNULLS, GREATEST is null-strict.** `(ord, key) > (a, b)` is written
as `ord > a OR (ord = a AND key > b)`; group and scroll equality use `DECODE(x, y, 1, 0) = 1`, which treats nulls as equal (R41);
the boost combination counts present values with `NVL2` and takes the maximum as `GREATEST(COALESCE(f0, f0, f1), COALESCE(f1, f0, f1))`.

O25. **MMR is a recursive subquery without arrays and aggregates.** The recursive member of a recursive subquery may not aggregate
or query its own name in a subquery, but may use analytic functions (R34). pgvector carries an array of picked keys and takes
`max()` over them; Oracle carries every pool row through each step with the step it was picked at and its highest similarity to
the picks, and names the next pick with `FIRST_VALUE` over the step's rows; the next step joins the pick's vector. Same order of
picks: highest `lambda * rel - (1 - lambda) * max similarity`, ties by distance and key.

O26. **Highlights through CTX_DOC.SNIPPET.** `JSON_ARRAY(CTX_DOC.SNIPPET(index, ROWIDTOCHAR(t.ROWID), :query))` with Oracle's
default markers `<b>` and `</b>` (R60, the same markers as pgvector's `ts_headline`); the index is the column's CONTEXT index found
in USER_INDEXES and USER_IND_COLUMNS (R18, R62), and the method first runs `CTX_DOC.SET_KEY_TYPE('ROWID')` because the session
default comes from a system parameter (R61). Divergence: snippets are fragments around the hits (radius 25, at most 250 characters),
`ts_headline` returns the marked text; a hit no term matches has no fragment.

O27. **As-of reads are flashback queries.** `AS OF TIMESTAMP TO_TIMESTAMP_TZ(:n, ...)` for a string and `AS OF SCN :n` for a number,
on every table reference of the read (R55, R34); searches as of a past time fetch exactly, because the vector index holds the
current rows and no page documents approximate search on past data. Clone as-of is `CREATE TABLE ... AS SELECT ... AS OF` with
the value as a constant (DDL takes no bind variables, O2). pgvector and DuckDB have no queryable history (unsupported there).
The past must lie within undo retention and after the table's last structural DDL (ORA-01466), so the runnable sample cannot
contain an as-of statement with a fixed literal; the rows are covered by fragments.

O28. **Load and release are In-Memory population.** `ALTER TABLE ... INMEMORY PRIORITY HIGH` and `NO INMEMORY`, per partition with
`MODIFY PARTITION` (R57); the column store needs `INMEMORY_SIZE` of at least 100M (R58), included in Free (R59), set by the setup
script (`inmemory.size`, 128M) with the vector pool. Milvus's load makes a collection searchable; Oracle searches a table that is
not in memory, so load is a performance hint here.

O29. **Compact is online segment shrink.** `ENABLE ROW MOVEMENT` and `SHRINK SPACE CASCADE` (R56); tables with function-based
indexes are excluded by the documentation, so a collection with a payload index on a JSON sub-field is unsupported. Vector and
Oracle Text indexes are not named.

O30. **Fuzziness and slop are Oracle Text operators with other units.** Fuzziness AUTO is `fuzzy(word)` per word of three or more
characters with Oracle's default similarity score (R63); Elasticsearch's fuzziness is an edit distance, Oracle's a similarity score
from 1 to 80 with no conversion, so numeric fuzziness is unsupported (a value-level divergence the status column cannot show). Slop
on a phrase is `NEAR((words), slop, TRUE)` (R64): the words in order with at most slop words between the first and the last;
Elasticsearch's slop also admits reordered words at a higher cost.

O31. **If-version is ORA_ROWSCN.** `UPDATE ... WHERE ... AND ORA_ROWSCN = :v` (R65). Generated tables have no ROWDEPENDENCIES, so
the SCN is the block's: a change of the row always refuses the update, an unrelated change in the same block also does. Generated
reads do not return ORA_ROWSCN; the script supplies the value.

O32. **Slices by ORA_HASH.** `ORA_HASH(key, n - 1) = i` (R66); pgvector uses the md5 of the key text. Both partition
the records; the sizes of the slices are server values.

O33. **Scalar-quantized HNSW indexes.** `QUANTIZATION SCALAR COMPRESSION RATIO r` before `PARAMETERS`, with `RESCORE FACTOR n`
for oversampling (R67, R68); the ratio is the column format's bits over the quantized bits (8 by default). Oracle quantizes the
index only (the table keeps the vectors) and always rescores (factor 1 by default).

## References

One line per documentation page; "Read" is the date the page was read.

| Id | URL | Book or source | Read | Facts used |
|---|---|---|---|---|
| R1 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/sparse-vectors.html | AI Vector Search User's Guide | 2026-11-14 | VECTOR(n,fmt,SPARSE); text form '[dims,[indices],[values]]', the page example implies 1-based indices ([5,[2,4],[10,20]] = dense [0,10,0,20,0]), but the 23.26.3 server returned [0,0,10,0,20] on 2026-11-15, so indices are 0-based in practice; dense text into sparse col fails ORA-51833; TO_VECTOR(...,DENSE) converts; FROM_VECTOR(v RETURNING CLOB FORMAT DENSE). |
| R2 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/create-tables-using-vector-data-type.html | AI Vector Search User's Guide | 2026-11-14 | VECTOR, VECTOR(n,fmt[,DENSE/SPARSE]); fmt INT8 FLOAT32 FLOAT64 BINARY; dims 1..65535 (BINARY 65528, multiple of 8); restrictions: VECTOR cannot be PK, FK, unique, check (except NULL/NOT NULL), default value, MODIFY column, partition key, IOT, GTT, non-vector index (B-tree, Text...); no DISTINCT, ORDER BY, GROUP BY, join, comparison on VECTOR cols. NULL vector allowed, NULL element not. |
| R3 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/overview-hierarchical-navigable-small-world-indexes.html | AI Vector Search User's Guide | 2026-11-14 | HNSW params NEIGHBORS, EFCONSTRUCTION; query target accuracy % or EFSEARCH; 100 still approximate; no query accuracy -> inherits index creation accuracy. |
| R4 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/create-vector-index.html | SQL Language Reference | 2026-11-14 | CREATE VECTOR INDEX name ON t (col) ORGANIZATION INMEMORY NEIGHBOR GRAPH / NEIGHBOR PARTITIONS; DISTANCE EUCLIDEAN/L2_SQUARED(EUCLIDEAN_SQUARED)/COSINE/DOT/MANHATTAN/HAMMING (default COSINE; no JACCARD for index); WITH TARGET ACCURACY 1..100; PARAMETERS (TYPE HNSW, NEIGHBORS 2..2048 (=M), EFCONSTRUCTION 1..65535) / (TYPE IVF, NEIGHBOR PARTITIONS 1..10000000, SAMPLES_PER_PARTITION, MIN_VECTORS_PER_PARTITION); rescore_factor 1..100; QUANTIZATION SCALAR ratio 2/4/8; ONLINE; LOCAL (per partition); INCLUDE columns; function-based HNSW. |
| R5 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/approximate-search-using-hnsw.html | AI Vector Search User's Guide | 2026-11-14 | query ORDER BY VECTOR_DISTANCE(col, :q, COSINE) FETCH [APPROX/APPROXIMATE] FIRST n ROWS ONLY [WITH TARGET ACCURACY p / WITH TARGET ACCURACY PARAMETERS (EFSEARCH k)]; without accuracy inherits index accuracy; APPROX keyword optional; omitted -> approximate attempted only on ADB-S (so on Free write APPROX explicitly); TO_VECTOR not needed for FLOAT32. |
| R6 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/approximate-search-using-ivf.html | AI Vector Search User's Guide | 2026-11-14 | ... WITH TARGET ACCURACY PARAMETERS (NEIGHBOR PARTITION PROBES n). |
| R7 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/vector_distance.html | SQL Language Reference | 2026-11-14 | VECTOR_DISTANCE(e1,e2[,metric]) returns BINARY_DOUBLE; default COSINE (BINARY: HAMMING); without metric uses index metric; metric conflicting with index -> exact search; approximate only if metric matches index metric and one column; metrics COSINE (cosine distance), DOT (negated dot product), EUCLIDEAN (L2), EUCLIDEAN_SQUARED (L2_SQUARED), HAMMING, MANHATTAN (L1), JACCARD (BINARY only); NULL if either NULL; shorthand <-> <=> <#>; functions L1_DISTANCE L2_DISTANCE COSINE_DISTANCE INNER_PRODUCT HAMMING_DISTANCE JACCARD_DISTANCE. |
| R8 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/perform-exact-similarity-search.html | AI Vector Search User's Guide | 2026-11-14 | FETCH EXACT FIRST n ROWS ONLY forces exact search; bind :query_vector. |
| R9 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/size-vector-pool.html | AI Vector Search User's Guide | 2026-11-14 | HNSW indexes live in the Vector Pool (SGA), sized by VECTOR_MEMORY_SIZE; modifiable at CDB level (current size) and PDB level (max usage); ALTER SYSTEM SET ... SCOPE=SPFILE; if VECTOR_MEMORY_SIZE=1 and sga_target>0 at CDB init, HNSW creation grows the pool automatically (not persisted). |
| R10 | https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinl/licensing-restrictions.html | Database Free Installation Guide for Linux | 2026-11-14 | (Free Installation Guide for Linux); Free limits processing to two CPU cores; one installation per logical environment (container counts; ORA-00442); max 12 GB user data (ORA-12954); no more than 2 GB RAM. |
| R11 | https://docs.oracle.com/en/database/oracle/oracle-database/26/dblic/Licensing-Information.html | Licensing Information User Manual | 2026-11-14 | (Licensing Information User Manual 26); Free column: Oracle Partitioning Y, Flashback Table Y, Flashback Database Y, Oracle Database In-Memory Y, Oracle RAC N, Oracle Active Data Guard N, Multitenant # of PDBs 16 (Free), Oracle Machine Learning Y; Free released under Oracle Free Use Terms and Conditions (https://www.oracle.com/downloads/licenses/oracle-free-license.html). Oracle Text is not listed as an option or restriction (available in the Free image: confirmed on the server, CTXSYS). |
| R12 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/escape-characters.html | Text Reference | 2026-11-14 | escape with {} (string) or \ (single char); reserved chars listed in ccref/reserved-words-and-characters.html (& / ~ , = ; - $ ! > * % _ ? {} () [] and words AND OR NOT ACCUM NEAR ABOUT FUZZY WITHIN MINUS etc). |
| R13 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/score.html | Text Reference | 2026-11-14 | + ccref/contains.html; SCORE(label) in SELECT/ORDER BY; CONTAINS(col, text_query, label); text_query limited to 4000 bytes VARCHAR2 / 64000 CLOB; default score INTEGER 0..100 (algorithm COUNT/DEFAULT), capped at 100; FLOAT variant via query template. |
| R14 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/create-index.html | Text Reference | 2026-11-14 | CREATE INDEX i ON t(col) INDEXTYPE IS CTXSYS.CONTEXT PARAMETERS ('SYNC (ON COMMIT)'); CONTEXT index needs sync after DML otherwise; no CTXAPP role needed, CREATE INDEX privilege on the column suffices. |
| R15 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/guidelines-and-restrictions-hybrid-vector-indexes.html | AI Vector Search User's Guide | 2026-11-14 | hybrid vector indexes support only in-database ONNX embedding models (no third-party, no imported vectors into $VR); HAMMING and JACCARD unsupported; IS JSON text columns unsupported. |
| R16 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/perform-multi-vector-similarity-search.html | AI Vector Search User's Guide | 2026-11-14 | FETCH [EXACT] FIRST 2 PARTITIONS BY bookId, 3 PARTITIONS BY paragraphId, 4 ROWS ONLY [WITH TARGET ACCURACY 90]. |
| R17 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/vector-index-and-hybrid-vector-index-views.html | AI Vector Search User's Guide | 2026-11-14 | V$VECTOR_INDEX, V$VECTOR_GRAPH_INDEX etc. are fixed views (diagnostic; need V$ privileges). |
| R18 | https://docs.oracle.com/en/database/oracle/oracle-database/26/refrn/ALL_INDEXES.html | Database Reference | 2026-11-14 | USER/ALL_INDEXES columns INDEX_NAME, INDEX_TYPE (... VECTOR), STATUS VALID/UNUSABLE, ITYP_NAME, DOMIDX_STATUS, DOMIDX_OPSTATUS VALID/FAILED. |
| R19 | https://container-registry.oracle.com/ords/ocr/ba/database/free | Oracle Container Registry | 2026-11-14 | container-registry.oracle.com/ords/ocr/ba/database/free (repository page, read 2026-11-14): Full image container-registry.oracle.com/database/free (latest = 23.26.3.0, linux/amd64 3.45 GB, image id cdf2f86bedfa, about 4 months old) and Lite (-lite, 854 MB); FREEPDB1 service, port 1521, CDB SID FREE; ORACLE_PWD (SYS, SYSTEM, PDBADMIN; random if omitted); ENABLE_ARCHIVELOG; scripts in /opt/oracle/scripts/setup (after new database setup) and /opt/oracle/scripts/startup (after start), .sh and .sql; readiness log line "DATABASE IS READY TO USE!"; licensed under Oracle Free Use Terms and Conditions; pull uses no database secret, the registry may present a terms prompt; Lite lacks among others Data Pump, RMAN, Oracle Text lexers other than BASIC_LEXER (so Text ships in both); pin a versioned tag. |
| R20 | https://pypi.org/pypi/oracledb/json | PyPI | 2026-11-14 | PyPI oracledb JSON (2026-11-14): 26.1.0, uploaded 2026-10-27, requires Python >=3.10. Source check in venv: oracledb.DB_TYPE_VECTOR, oracledb.SparseVector, connect(tcp_connect_timeout=...). |
| R21 | https://registry.npmjs.org/oracledb | npm registry | 2026-11-14 | npm oracledb 26.0.0 (latest dist-tag, node >=14.17); @types/oracledb 26.0.1 declares connectTimeout, transportConnectTimeout, callTimeout, DB_TYPE_VECTOR, SparseVector, Float32Array binds. |
| R22 | https://python-oracledb.readthedocs.io/en/latest/user_guide/vector_data_type.html | python-oracledb User Guide | 2026-11-14 | python-oracledb.readthedocs.io user_guide/vector_data_type.html: binds array.array('f'/'d'/'b'), binary 'B' (8 dims per byte); fetched as array.array; SparseVector(dims, indices, values); outconverter=list via outputtypehandler. |
| R23 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/Database-Object-Names-and-Qualifiers.html | SQL Language Reference | 2026-11-14 | quoted identifiers case-sensitive and may be reserved words; nonquoted cannot be reserved words and are stored uppercase; names 1..128 bytes (COMPATIBLE >= 12.2); uppercase ROWID never allowed as column name; PDB names always case-insensitive. |
| R24 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/from_vector.html | SQL Language Reference | 2026-11-14 | FROM_VECTOR(v [RETURNING VARCHAR2(n)/CLOB/BLOB] [FORMAT SPARSE/DENSE]); FLOAT32 text uses exponent form, e.g. [1.10000002E+000,...]. |
| R25 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/to_vector.html | SQL Language Reference | 2026-11-14 | TO_VECTOR(expr[, dims, format[, DENSE/SPARSE]]) takes VARCHAR2, CLOB, BLOB or JSON array; string form [1, 3.4, -05.60, 3e+4]. |
| R26 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_OBJECT.html | SQL Language Reference | 2026-11-14 | KEY VALUE entries; FORMAT JSON after an expression embeds JSON text unquoted; NULL ON NULL / ABSENT ON NULL; numeric values become JSON numbers; RETURNING clause. |
| R27 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/Data-Types.html | SQL Language Reference | 2026-11-14 | VARCHAR2 max 4000 bytes (STANDARD) or 32767 (EXTENDED); BINARY_DOUBLE; JSON type up to 32 MB (Oracle recommends JSON type for JSON columns; textual JSON is wrapped implicitly); BOOLEAN (True, False); VECTOR. |
| R28 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/json_exists-condition.html | SQL Language Reference | 2026-11-14 | JSON_EXISTS(expr, path [PASSING expr AS var]); path text literal with variables from the passing clause. |
| R29 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_TRANSFORM.html | SQL Language Reference | 2026-11-14 | operations APPEND (to array), INSERT, SET, REMOVE, KEEP, ADD_SET. |
| R30 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/MERGE.html | SQL Language Reference | 2026-11-14 | MERGE INTO t USING (...) ON (...) WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...; DELETE WHERE. |
| R31 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/CREATE-USER.html | SQL Language Reference | 2026-11-14 | needs CREATE USER system privilege; IF NOT EXISTS supported. |
| R32 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/DROP-TABLE.html | SQL Language Reference | 2026-11-14 | IF EXISTS; PURGE releases space and bypasses the recycle bin (FLASHBACK TABLE recovers non-purged drops). |
| R33 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/CREATE-INDEX.html | SQL Language Reference | 2026-11-14 | IF NOT EXISTS supported. |
| R34 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/SELECT.html | SQL Language Reference | 2026-11-14 | row_limiting_clause OFFSET n ROWS FETCH FIRST/NEXT n ROWS ONLY / PERCENT / WITH TIES; sample_clause SAMPLE (percent) [SEED n]. |
| R35 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/avg_vecse.html | AI Vector Search User's Guide | 2026-11-14 | AVG(vector) aggregate returns FLOAT64 vector (centroid); vecse/arithmetic-operators.html: +, -, * dimension-wise on vectors. |
| R36 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/binary-vectors.html | AI Vector Search User's Guide | 2026-11-14 | BINARY vectors stored as packed UINT8 bytes, 8 dimensions per byte; inserted as UINT8 array text '[201, 15]' of length dims/8. |
| R37 | https://central.sonatype.com/artifact/com.oracle.database.jdbc/ojdbc17 | Maven Central | 2026-11-14 | central.sonatype.com/artifact/com.oracle.database.jdbc/ojdbc17 (2026-11-14): latest 23.26.4.0.0 (16 days old), "Oracle JDBC Driver compatible with JDK17, JDK19, and JDK21", JDBC 4.3, license Oracle Free Use Terms and Conditions; oracle.com JDBC downloads page: 26ai ojdbc17 certified with JDK17, 19, 21 and 25. |
| R38 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/SET-TRANSACTION.html | SQL Language Reference | 2026-11-14 | SET TRANSACTION ISOLATION LEVEL SERIALIZABLE / READ COMMITTED (default) as the first statement of a transaction; applies to the current transaction only. |
| R39 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/CREATE-TABLE.html | SQL Language Reference | 2026-11-14 | IF NOT EXISTS; list partitioning with AUTOMATIC (a new partition is created for a new value); partitions named with VALUES lists. |
| R40 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/ALTER-TABLE.html | SQL Language Reference | 2026-11-14 | ADD (column definitions), RENAME COLUMN a TO b, DROP COLUMN, RENAME TO, ADD PARTITION ... VALUES (...), DROP PARTITION ... UPDATE INDEXES, ADD PRIMARY KEY. |
| R41 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/DECODE.html | SQL Language Reference | 2026-11-14 | DECODE(expr, search, result, default) compares expr to search, and two nulls are considered equal (so DECODE(x, :v, 1, 0) = 0 is a null-safe inequality). |
| R42 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_TABLE.html | SQL Language Reference | 2026-11-14 | JSON_TABLE(expr, path COLUMNS (name FOR ORDINALITY, name type PATH path, name type EXISTS PATH path, name FORMAT JSON PATH path)). |
| R43 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_ARRAYAGG.html | SQL Language Reference | 2026-11-14 | JSON_ARRAYAGG(expr ORDER BY ... RETURNING CLOB). |
| R44 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_SERIALIZE.html | SQL Language Reference | 2026-11-14 | and sqlrf/json_scalar.html; JSON_SERIALIZE(expr RETURNING CLOB); JSON_SCALAR turns a SQL scalar into a JSON scalar. |
| R45 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/Pattern-matching-Conditions.html | SQL Language Reference | 2026-11-14 | LIKE with ESCAPE; REGEXP_LIKE(source, pattern). |
| R46 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/TRUNCATE-TABLE.html, | SQL Language Reference | 2026-11-14 | sqlrf/COMMENT.html (COMMENT ON TABLE t IS 'text'), sqlrf/CREATE-VIEW.html (CREATE OR REPLACE VIEW), sqlrf/CREATE-ROLE.html, sqlrf/GRANT.html (object privileges SELECT, INSERT, UPDATE, DELETE, REFERENCES, ALTER, INDEX on a table).;  |
| R47 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/ctx_ddl-package.html | Text Reference | 2026-11-14 | CTX_DDL.CREATE_PREFERENCE, SET_ATTRIBUTE, DROP_PREFERENCE; ccref/basic_lexer-example.html: begin ctx_ddl.create_preference('mylex', 'BASIC_LEXER'); ctx_ddl.set_attribute(...); end; ccref/basic_lexer-attributes.html: index_stems takes a language name (for example ENGLISH) or NONE. |
| R48 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/oracle-text-views.html | Text Reference | 2026-11-14 | CTX_USER_PREFERENCES lists the preferences of the current user. |
| R49 | https://docs.oracle.com/en/database/oracle/oracle-database/26/refrn/USER_ROLE_PRIVS.html | Database Reference | 2026-11-14 | (GRANTED_ROLE), refrn/ALL_TABLES.html (TABLE_NAME; USER_TABLES has the same columns without OWNER), refrn/ALL_DEPENDENCIES.html (REFERENCED_NAME; USER_DEPENDENCIES likewise).;  |
| R50 | https://docs.oracle.com/en/database/oracle/oracle-database/26/adjsn/json-search-index-ad-hoc-queries-and-full-text-search.html | JSON Developer's Guide | 2026-11-14 | CREATE SEARCH INDEX ... FOR JSON. |
| R51 | https://docs.oracle.com/en/database/oracle/oracle-database/26/adjsn/sql-json-path-expression-item-methods.html | JSON Developer's Guide | 2026-11-14 | item method size() of an array; sqlrf/JSON_VALUE.html: JSON_VALUE(expr, path RETURNING type). |
| R52 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/CREATE-INDEX.html | SQL Language Reference | 2026-11-14 | function-based indexes on expressions. |
| R53 | https://docs.oracle.com/en/database/oracle/oracle-database/26/arpls/DBMS_RANDOM.html | PL/SQL Packages and Types Reference | 2026-11-14 | DBMS_RANDOM.VALUE returns a random number in [0, 1). |
| R54 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/ROWID-Pseudocolumn.html | SQL Language Reference | 2026-11-14 | ROWID addresses a row; sqlrf/SELECT.html: CROSS APPLY joins a lateral inline view. |

| R55 | https://docs.oracle.com/en/database/oracle/oracle-database/26/adfns/flashback.html | Development Guide | 2026-11-15 | Using Oracle Flashback Query (SELECT AS OF): the AS OF clause follows the table reference, per table; AS OF TIMESTAMP or SCN; the past is reachable within undo retention; structural DDL makes earlier data unreadable (ORA-01466); a timestamp may resolve up to 3 seconds earlier; FLASHBACK and READ/SELECT privileges for other users' objects; INSERT or CREATE TABLE AS SELECT with AS OF act on past results. SELECT (R34): expr of AS OF cannot be a column expression or subquery and cannot be NULL; no flashback_query_clause on a query_name. |
| R56 | https://docs.oracle.com/en/database/oracle/oracle-database/26/admin/managing-space-for-schema-objects.html | Administrator's Guide | 2026-11-15 | Shrinking Database Segments Online: online and in place, DML allowed, indexes maintained; row movement must be enabled first; ASSM tablespaces; not eligible: IOT mapping tables, tables with rowid-based materialized views, tables with function-based indexes, SECUREFILE LOBs, basic, warehouse and archive compressed tables; CASCADE shrinks dependent segments. Domain and vector indexes are not named. |
| R57 | https://docs.oracle.com/en/database/oracle/oracle-database/23/inmem/populating-objects-in-memory.html | In-Memory Guide | 2026-11-15 | ALTER TABLE t INMEMORY / NO INMEMORY; MODIFY PARTITION p INMEMORY PRIORITY ...; PRIORITY NONE (default) populates on a full scan; changing an INMEMORY attribute other than PRIORITY evicts a populated table. |
| R58 | https://docs.oracle.com/en/database/oracle/oracle-database/23/inmem/enabling-the-im-column-store.html | In-Memory Guide | 2026-11-15 | INMEMORY_SIZE above 0 in the SPFILE, then restart the CDB; at least 100M; the In-Memory area is taken from SGA_TARGET; it can grow at run time but not shrink. |
| R59 | https://docs.oracle.com/en/database/oracle/oracle-database/23/dblic/Licensing-Information.html | Licensing Information User Manual | 2026-11-15 | Table 1-9: Oracle Database In-Memory, Free Y. |
| R60 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/snippet.html | Text Reference | 2026-11-15 | CTX_DOC.SNIPPET(index_name, textkey, text_query, starttag '<b>', endtag '</b>', ..., separator '<b>...</b>', radius 25, max_length 250) returns VARCHAR2; textkey is the primary key or the ROWID (SET_KEY_TYPE). |
| R61 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/set_key_type.html | Text Reference | 2026-11-15 | CTX_DOC.SET_KEY_TYPE('ROWID' or 'PRIMARY_KEY') for the invoking session; default from the CTX_DOC_KEY_TYPE system parameter. |
| R62 | https://docs.oracle.com/en/database/oracle/oracle-database/26/refrn/ALL_IND_COLUMNS.html | Database Reference | 2026-11-15 | USER_IND_COLUMNS: INDEX_NAME, TABLE_NAME, COLUMN_NAME of the user's indexes. |
| R63 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/fuzzy.html | Text Reference | 2026-11-15 | fuzzy(term, score, numresults, weight): term of at least 3 characters; score a similarity from 1 to 80 (default 60), numresults 1 to 5000 (default 100); a word, not a phrase. |
| R64 | https://docs.oracle.com/en/database/oracle/oracle-database/26/ccref/near.html | Text Reference | 2026-11-15 | NEAR((word1, ..., wordn) [, max_span [, order [, minreqd]]]): max_span counts the words between the first and the last (default 1, error above 100); order TRUE keeps the order (needs max_span); terms may be phrases. |
| R65 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/ORA_ROWSCN-Pseudocolumn.html | SQL Language Reference | 2026-11-15 | ORA_ROWSCN: SCN of the most recent change, block-level without ROWDEPENDENCIES; usable in the WHERE clause of UPDATE and DELETE; not in flashback queries or on views. |
| R66 | https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/ORA_HASH.html | SQL Language Reference | 2026-11-15 | ORA_HASH(expr, max_bucket, seed): buckets 0 to max_bucket (hash MOD (max_bucket + 1)); expr not LONG or LOB. |
| R67 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/hierarchical-navigable-small-world-index-syntax-and-parameters.html | AI Vector Search User's Guide | 2026-11-15 | ORGANIZATION INMEMORY NEIGHBOR GRAPH [DISTANCE m] [WITH TARGET ACCURACY p] [QUANTIZATION SCALAR COMPRESSION RATIO {2,4,8}] [PARAMETERS (TYPE HNSW, NEIGHBORS, EFCONSTRUCTION, RESCORE FACTOR 1..100, ALGORITHM UNIFORM_QUANTIZATION)]; FLOAT32 to Float16 (2) or Int8 (4), FLOAT64 to Float32 (2), Float16 (4) or Int8 (8). |
| R68 | https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/scalar-quantized-hnsw-indexes.html | AI Vector Search User's Guide | 2026-11-15 | scalar quantization is supported with HNSW indexes at index creation; feature of RU 23.26.1 (vecse/january-2026-release-update-23.26.1.html). Error help ORA-52402: quantized index creation needs patches 37956731 and 38586272 where they are not part of the release. |

## Reference pending

Pages that could not be read; no row of COVERAGE.md rests on them alone.

| URL tried | Result | What depends on it |
|---|---|---|
| https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/boolean-test-condition.html | page read without its text | nothing: IS NOT TRUE is not generated; negation uses CASE WHEN ... THEN 1 ELSE 0 END = 0 (R41 style, plain CASE) |
| Oracle Text availability in the Free image | not stated in the Licensing Information Free column (R11) | confirmed on the server (CTXSYS user and a CONTEXT index on the demo) |
| https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_SCALAR-function.html, REGEXP_LIKE-Condition.html, LIKE-condition.html | HTTP 404 | the same facts were read on json_scalar.html and Pattern-matching-Conditions.html (R44, R45) |
| Flashback queries over a CONTAINS condition (Oracle Text) or through a vector index | no page found (Development Guide R55, SELECT R34, AI Vector Search User's Guide searched) | as-of searches fetch exactly (FETCH EXACT); whether CONTAINS answers a flashback query is not documented (KeywordSearch.asOf, HybridSearch.asOf) |

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. **Sparse indices are 0-based.** `FROM_VECTOR(TO_VECTOR('[5,[2,4],[10,20]]', 5, FLOAT32, SPARSE) RETURNING CLOB FORMAT
   DENSE)` returns `[0,0,1.0E+001,0,2.0E+001]` on 23.26.3.0, contrary to the reading of the R1 example. The generator
   passes vecDSL sparse indices unchanged.
2. **Oracle Text ships in the Free image** (CTXSYS is present), although the Licensing Information Free column (R11)
   does not list it.
3. **The prebuilt Free database skips `/opt/oracle/scripts/setup`** on its first start; the environment runs the setup
   script itself when its marker is missing.

## Known limitations

- `weighted` hybrid fusion sums weight times the raw leg score, as on pgvector, so its result depends on the scale of
  the keyword score (Oracle Text SCORE is 0 to 100); `rrf` and `relative_score` are scale-free.
- CONTEXT index synchronization is ON COMMIT: keyword results right after a write in the same transaction do not see it.
- The first database creation of the Free image takes several minutes and needs about 2 GB of memory (R10).
- No server-side embedding and no hybrid vector index: a hybrid vector index needs an in-database ONNX
  model (R15).
- Whether CONTAINS answers a flashback query is not documented; as-of searches fetch exactly.
