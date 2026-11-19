# vecDSL to Redis: mapping notes

Redis Open Source 8.10.3 with the bundled Query Engine (RediSearch v8.10.1) and JSON (v8.10.0), image `redis:8.10.3-trixie`.
Each note names the construct, the commands the generator builds and the reference that fixes the behavior. COVERAGE.md has one
row per grammar construct; this file explains the choices behind the rows. Abbreviations: `DOCS` = https://redis.io/docs/latest/,
`RS` = https://github.com/RediSearch/RediSearch/blob/v8.10.1/, `VS` = https://github.com/RedisAI/VectorSimilarity/blob/dd6879e82d9d0167d5b2403b9356372c10732edd/.

## Data model

1. **Collections are Query Engine indexes over JSON documents.** `create collection c` is `FT.CREATE c ON JSON PREFIX 1 c: SCHEMA ...`
   and every record is the JSON document at key `c:<id>`. The alternative vector model, vector sets (`VADD`, `VSIM`), keeps one
   vector per element, has no BM25 text search, no hybrid search and no typed numeric or array attributes
   (https://github.com/redis/redis/blob/8.10.3/modules/vector-sets/README.md), so it cannot carry most of the grammar; it is
   not used. References: DOCS `commands/ft.create/`, DOCS `develop/ai/search-and-query/indexing/`.
2. **JSON rather than HASH.** JSON keeps numbers, booleans, nulls, arrays (`$.tags[*]`) and nested objects (`$.details.pageCount`), so
   array, struct, `is_null` and typed update operations map directly. A HASH stores flat strings and vectors only as blobs.
   Vectors are JSON arrays of numbers; the index reads them as the declared `TYPE` (DOCS `develop/ai/search-and-query/vectors/`).
3. **Logical databases are name prefixes.** The Query Engine indexes database 0 only, so `logical-db catalog { collection notes }`
   becomes the index `catalog:notes` over keys `catalog:notes:<id>`. Listing databases reads the index names (`FT._LIST`).
4. **Attributes.** int, bigint, int8, float, double: `NUMERIC INDEXMISSING SORTABLE`; varchar, text, uuid, dates: `TAG CASESENSITIVE
   INDEXMISSING SORTABLE`; a full-text field: `TEXT` (with `WEIGHT`, `NOSTEM` when its analyzer has no language, `INDEXMISSING`) plus a
   hidden `TAG` `vecdsl_exact_<field>` on the same path for equality, prefix and like; arrays: the element type on `$.f[*]`; struct
   sub-fields: `f_sub` on `$.f.sub`. INDEXMISSING makes `is_null` (`ismissing(@f)`) answerable (DOCS `commands/ft.create/`).
5. **Tenants, namespaces and partitions** are hidden TAG attributes `vecdsl_tenant`, `vecdsl_namespace`, `vecdsl_partition` written
   into each document and added to every filter. A partitioned collection writes `default` when no partition is named.
6. **Vector attributes.** No index: `FLAT`; `hnsw` and `auto`: `HNSW` with `M`, `EF_CONSTRUCTION`, `EF_RUNTIME`; `vamana`: `SVS-VAMANA`
   with `GRAPH_MAX_DEGREE`, `CONSTRUCTION_WINDOW_SIZE`, `SEARCH_WINDOW_SIZE`. Types FLOAT32, FLOAT16, BFLOAT16, FLOAT64, INT8 and UINT8;
   SVS-VAMANA takes FLOAT16 and FLOAT32 only (DOCS `develop/ai/search-and-query/vectors/`).
7. **Quantization** is `COMPRESSION LVQ8` of an SVS-VAMANA attribute, for `scalar` 8 bits only. The vectors page states that Intel's
   LVQ and LeanVec optimizations are not in Redis Open Source and that COMPRESSION falls back to 8-bit scalar quantization there
   (DOCS `develop/ai/search-and-query/vectors/`, "SVS-VAMANA"). HNSW and FLAT take no compression; binary and product: unsupported.
8. **Analyzers.** One `LANGUAGE` per index (the stemmer of the full-text fields); `STOPWORDS 0` unless the language is English with
   the default list, in which case no STOPWORDS argument is passed and the server's list applies (RS `src/stopwords.h`). The
   tokenizer is fixed (RS `src/toksep.h`); lowercase, stop and stemmer filters are what the engine does anyway; phonetic and synonym
   are pending; other filters and tokenizers are unsupported.

## Values and queries

9. **Values are never part of a query string.** Every filter value is a `PARAMS` entry (`$p0`, `$p1`, ...), queries use `DIALECT 2`
   (DOCS `develop/ai/search-and-query/advanced-concepts/dialects/`). The v2 lexer accepts parameters as terms, prefixes (`$p*`),
   fuzzy terms (`%$p%`) and wildcards (`w'$p'`): `attr = '$'.term`, `prefix = (term.star | number.star | attr.star)`, `wildcard =
   'w' . verbatim` with `QT_PARAM_WILDCARD` (RS `src/query_parser/v2/lexer.rl`, RS `src/query_parser/v2/parser.y`).
10. **Text is split by the runtime.** Keyword input is a value, so the generated query holds markers such as `{{and:t0}}`; the runtime
    splits the text at the engine's separator characters, drops the index's stop words and binds each term as `$t0_0`, `$t0_1`, ...
    The modes are and, or, fuzzy1 to fuzzy3 (`%`, `%%`, `%%%`), prefix, prefixlast (phrase_prefix) and tags (a list parameter in a
    TAG condition). A text with no terms left raises an error in the runtime rather than sending an empty query.
11. **Vector search** is `FT.SEARCH idx "<filter>=>[KNN k @f $vec <attributes> AS vecdsl_score]" SORTBY vecdsl_score ASC LIMIT o n
    RETURN 1 vecdsl_score DIALECT 2`. `candidates` sets `EF_RUNTIME` (HNSW) or `SEARCH_WINDOW_SIZE` (SVS-VAMANA); `exact`, `exhaustive`
    and `filter-strategy pre_filter` set `HYBRID_POLICY ADHOC_BF` on a filtered query, post_filter and iterative `BATCHES`
    (DOCS `develop/ai/search-and-query/vectors/`, "KNN query attributes"). `timeout` is `TIMEOUT` of FT.SEARCH.
12. **Scores (D3).** The engine returns distances: COSINE `1 - cos`, IP `1 - dot`, L2 the squared distance in FLAT and HNSW
    (VS `src/VecSim/spaces/IP/IP.cpp`, VS `src/VecSim/spaces/L2/L2.cpp`). The runtime converts: cosine and dot `1 - d`, euclidean
    `-sqrt(d)`; `score native` keeps `d`. Bounds compare the converted score in the runtime. Results are rounded to 6 digits in all
    three languages.
13. **Keyword search** is `FT.SEARCH ... WITHSCORES NOCONTENT SCORER BM25STD [LANGUAGE l] LIMIT o n DIALECT 2`, `VERBATIM` for
    `text-query term` (no stemming of the query). BM25STD uses k1 = 1.2 and b = 0.75 (RS `src/ext/default.c`). `phrase` and
    `phrase_prefix` add `=>{$slop: s; $inorder: true;}`; field boosts add `$weight`.
14. **Hybrid search.** One vector leg and one keyword leg with one window under `rrf`: `FT.HYBRID ... COMBINE RRF 4 CONSTANT k WINDOW w`,
    which is D3's rrf with ranks from 1 (DOCS `commands/ft.hybrid/`, Redis 8.4 and later). Every other case, and weighted and
    relative_score, runs each leg as its own search and fuses in the runtime, because FT.HYBRID's LINEAR normalizes scores
    differently from D3 (RS `src/vector_normalization.h`). Ties are broken by key.
15. **Recommend** averages the stored vectors of the examples in the runtime (`2 avg(positive) - avg(negative)`), excludes the
    example ids by filter and runs a KNN query.
16. **Scroll, keys of a filter and delete with limit** use `FT.AGGREGATE idx where LOAD 1 @__key SORTBY n ... @id ASC LIMIT o n`,
    so the order is total and repeatable (DOCS `commands/ft.aggregate/`).
17. **Counts** are `FT.SEARCH ... NOCONTENT LIMIT 0 0`; `with-total` adds such a count with its own parameters.

## Writes

18. **Every write is a plan under WATCH.** The runtime WATCHes the keys, reads them with `JSON.MGET` and `PTTL`, computes the new
    documents in memory and commits `JSON.SET $`, `DEL`, `EXPIRE` or `PEXPIRE` in one `MULTI`/`EXEC`; a nil `EXEC` (a watched key
    changed) raises a conflict error. Redis has no rollback, so nothing is queued until every step was planned
    (DOCS `develop/using-commands/transactions/`). Update keeps a key's remaining time to live.
19. **Visibility and `wait`.** A write is visible to the next command; the tiered HNSW index also searches its flat buffer
    (VS `src/VecSim/algorithms/hnsw/hnsw_tiered.h`). `wait` and `flush` therefore ask for durability: `WAITAOF 1 0 10000` returns
    when the local append-only file has fsynced every write (DOCS `commands/waitaof/`), which needs `appendonly yes` (set in the
    compose file).
20. **Transactions** hold writes by id: each step is planned against the documents the steps before it wrote, and all commit in one
    EXEC. Filtered steps are unsupported, because the replies of queued commands arrive only at EXEC.
21. **TTL.** `ttl` on a write is `EXPIRE` per key; `set ttl` on a collection expires every key under the prefix and is applied to
    later writes.

## Schema changes and operations

22. **FT.ALTER only adds attributes.** Dropping or renaming a field rewrites every document under the prefix in WATCH batches and
    creates the index again (`FT.DROPINDEX` without `DD` keeps the documents); renaming a collection `RENAME`s each key to the new
    prefix, drops the old index and creates the new one; a clone `COPY`s the keys instead (DOCS `commands/ft.alter/`,
    `commands/ft.dropindex/`, `commands/rename/`, `commands/copy/`).
23. **Recreating an index keeps its aliases.** `rt.recreate` reads `FT.ALIASLIST` (Redis Open Source 8.10.0 and later, DOCS
    `commands/ft.aliaslist/`), drops and creates the index, and adds each alias again, ignoring "alias already exists". The command
    pages do not say whether FT.DROPINDEX removes the aliases of an index, so the runtime works either way.
24. **Aliases** are `FT.ALIASADD`, `FT.ALIASUPDATE` (swap) and `FT.ALIASDEL`; an alias rename adds the new name and deletes the old.
25. **Roles** are ACL users that cannot log in (`ACL SETUSER name off resetkeys resetchannels -@all +@read ... ~prefix*`); `if-not-exists`
    checks `ACL GETUSER` first. Privileges map to the categories read, write, search and all (DOCS
    `operate/oss_and_stack/management/security/acl/`).
26. **Connections** read `VECDSL_REDIS_URL`, `VECDSL_REDIS_USER` and `VECDSL_REDIS_PASSWORD`; the URL never holds credentials, and a
    password written in the script is unsupported. All clients use RESP2 and send the generated argument arrays unchanged
    (redis-py `Connection.send_command`, node-redis `sendCommand`, Jedis `sendCommand`), with connect and command timeouts.
27. **Load, compact, release.** Everything is in memory: load and compact check that the index exists (the fork garbage collector
    compacts on its own, RS `src/fork_gc.c`); release has no command and is unsupported.
28. **Await-index** polls `FT.INFO` until `indexing` is 0, with a timeout (RS `src/info/info_command.c`).
29. **Snapshots, backups, replication, sharding, metrics, models** are server configuration or absent from Redis Open Source;
    each raises its message (COVERAGE.md has the references).

## Slices and fuzziness

30. **get by ids within a tenant, namespace or partition** reads the documents of the ids with JSON.MGET, as a plain get does
    (https://redis.io/docs/latest/commands/json.mget/), and keeps those whose slice attribute holds a named value: every write
    stores the tenant, namespace and partition in the document (`vecdsl_tenant`, or the declared partition key, `vecdsl_namespace`,
    `vecdsl_partition`), which is what the slice conditions of the searches query as TAG fields. A document outside the slice is
    skipped like a missing key.
31. **fuzziness "AUTO" stays unsupported**: the query syntax sets the Levenshtein distance only by the
    number of `%` around a term, one to three ("Use multiple pairs of '%' brackets, up to three deep, to increase the Levenshtein
    distance"; "The maximum LD for fuzzy matching is 3"), and has no distance chosen by the length of the term
    (https://redis.io/docs/latest/develop/ai/search-and-query/advanced-concepts/query_syntax/, "Fuzzy matching"). Elasticsearch's AUTO
    picks the distance per term from its length; reproducing it would mean the runtime rewriting each term with its own number of `%`,
    a different query than the one written. The status column says native because the numeric values are native; the value AUTO is a
    value-level divergence the column cannot express (a candidate validator rule).

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. **EF_RUNTIME with ADHOC_BF.** `candidates 200` with `filter-strategy pre_filter` produced `EF_RUNTIME 200
   HYBRID_POLICY ADHOC_BF`, which the server rejects (`SEARCH_ADHOC_EF_RUNTIME_IRRELEVANT`): the brute-force policy takes
   no index effort setting. Change: under ADHOC_BF neither EF_RUNTIME nor SEARCH_WINDOW_SIZE is sent, and the
   candidate pool is noted.
2. **FT.DROPINDEX removes the aliases of an index** (FT.INFO on the alias answers "Index not found" after the drop and
   after the index is created again), so `rt.recreate` adds the aliases back.
3. **Parameters in TAG wildcards and prefixes work:** `@t:{w'$p'}` with `p = Gr*` and `@x:($p*)` with `p = datab` match
   like their literal forms (DIALECT 2), so `like` keeps every value in PARAMS.

## Known limitations

- The Query Engine indexes database 0 only; logical databases are name prefixes.
- An exact search on an HNSW or SVS-VAMANA field without a filter is unsupported.
- Filtered steps inside a transaction are unsupported.
- Intel's LVQ and LeanVec optimizations are not in Redis Open Source; quantization of HNSW indexes is unsupported.
- Scores of FLOAT16, BFLOAT16 and compressed vectors are float32 distances rounded to six digits.
