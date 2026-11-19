# vecDSL to Elasticsearch: mapping notes

How the Elasticsearch generator maps vecDSL 7.1 onto a self-hosted Elasticsearch 9.5.6 server with the basic (free)
license. COVERAGE.md has one row per grammar construct with its status and reference;
References point at the source tree of the release tag, https://github.com/elastic/elasticsearch/blob/v9.5.6/ (below
`SRC/`), and at the REST API specification in `SRC/rest-api-spec/src/main/resources/rest-api-spec/api/` (below `API/`).

## Versions (checked 2026-11-12)

| part | version | source |
|---|---|---|
| server | `docker.elastic.co/elasticsearch/elasticsearch:9.5.6` | https://github.com/elastic/elasticsearch/releases/tag/v9.5.6 |
| Python | `elasticsearch` 9.5.1, `>=9.5.1,<9.6` | https://pypi.org/project/elasticsearch/ |
| TypeScript | `@elastic/elasticsearch` 9.5.1 (Node 22 or later), TypeScript 5.9.3, `@types/node` 22.20.5 | https://www.npmjs.com/package/@elastic/elasticsearch |
| Java | `co.elastic.clients:elasticsearch-java` 9.5.6 (low-level `Rest5Client`), Gson 2.13.2, release 17 | https://github.com/elastic/elasticsearch-java/releases/tag/v9.5.6 |

## Notes

1. **One request model, three thin runtimes.** The generator builds every request body (index creation, mappings, query
   DSL, aggregations, scripts) as a language-neutral Code value, so the three printed programs carry the same JSON. The
   runtimes only send it (`perform_request` in Python, `transport.request` in TypeScript, `Rest5Client.performRequest` in
   Java) and shape the answer.
2. **Status codes in Java.** `Rest5Client` returns every answer below 500 without throwing
   (sources of elasticsearch-java 9.5.6, class
   `co.elastic.clients.transport.rest5_client.low_level.Rest5Client`, method `isCorrectServerResponse`: `statusCode < 500`). The Java runtime therefore checks the status itself: 2xx is a result, a listed `ignore` status is null,
   anything else raises `ElasticsearchFailure` with the server's reason. Python (`ApiError`) and TypeScript
   (`ResponseError`) raise on 4xx by themselves.
3. **Collections are indices.** Index names are the snake-case, lower-case collection names (`booksCopy` becomes
   `books_copy`), because index names must be lower case
   (`SRC/server/src/main/java/org/elasticsearch/cluster/metadata/MetadataCreateIndexService.java`, `validateIndexName`). Every index gets one shard and no
   replica unless a cluster declares otherwise; the mapping is `dynamic: strict`, so a misspelled field fails instead of
   creating a new mapping.
4. **Logical databases are name prefixes.** `catalog.notes` is the index of collection `notes` in logical database
   `catalog`. Dropping the database deletes `catalog.*` through `rt.drop_matching`, which lists the matching indices and
   deletes them by name (wildcard deletes are refused by `action.destructive_requires_name`, true by default in
   `SRC/server/src/main/java/org/elasticsearch/action/support/DestructiveOperations.java`).
5. **Tenants, namespaces and partitions are concealed.** Each becomes a hidden keyword field (`vecdsl_tenant`,
   `vecdsl_namespace`, `vecdsl_partition`) and a filtered alias `<index>-<kind>-<value>` per declared value
   (`API/indices.update_aliases.json`). Writes into a slice set the field; reads add a term filter.
6. **Vector fields.** `float_vector`, `bfloat16_vector`, `int8_vector` and `binary_vector` map to `dense_vector` with
   element type float, bfloat16, byte and bit (`SRC/server/.../DenseVectorFieldMapper.java`); `sparse_float_vector` maps to
   `sparse_vector` with `index_options.prune: false`, so no token is dropped. Without a declared index the generator writes
   `index_options: {type: hnsw}`, because the default since 9.0 is quantized (`int8_hnsw`, or `bbq_hnsw` from 384
   dimensions, `BBQ_DIMS_DEFAULT_THRESHOLD`) and would change scores silently.
7. **Quantization.** scalar 8 and 4 bits map to `int8_hnsw`/`int8_flat` and `int4_hnsw`/`int4_flat`, binary to
   `bbq_hnsw`/`bbq_flat` (64 dimensions or more, `BBQ_MIN_DIMS`); `rescore oversampling` is `rescore_vector.oversample`.
   Product quantization, IVF (`bbq_disk`, enterprise) and GPU indexing (enterprise) raise Unsupported.
8. **Scores (D3).** Elasticsearch reports a transformed similarity: l2 `1/(1+d²)`, cosine `(1+c)/2`, dot product
   `(1+d)/2` (byte vectors `0.5 + d/(dims·32768)`), max inner product `d<0 ? 1/(1-d) : d+1`, bit vectors `(dims-h)/dims`
   (https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/dense-vector, "similarity"). The runtimes invert
   these formulas into the canonical vecDSL similarity; `score native` keeps the server value. Bounds apply to the form the
   statement asks for.
9. **Exact search and other metrics.** `exact`, a metric different from the field's, `filter-strategy pre_filter` and
   `exhaustive` become a `script_score` query over the filtered documents with the painless vector functions
   (`cosineSimilarity`, `dotProduct`, `l1norm`, `l2norm`, `hamming`). Manhattan distance exists only this way.
10. **kNN options.** `num_candidates` comes from `params { candidates }`, else from the index's `search-candidates`, else it
    is left to the server (`1.5 × k`, `NUM_CANDS_MULTIPLICATIVE_FACTOR` in
    `SRC/server/src/main/java/org/elasticsearch/search/vectors/KnnSearchBuilder.java`). `filter-strategy post_filter` moves the filter to `post_filter`.
11. **MMR.** `mmr lambda` uses the `diversify` retriever of type `mmr` (`SRC/server/.../DiversifyRetrieverBuilder.java`),
    which lives in the server module without a license check; `candidates` is its `rank_window_size`.
12. **Hybrid search is fused in the runtime.** The `rrf` and `linear` retrievers need an enterprise license
    (`SRC/x-pack/plugin/rank-rrf/.../RRFRankPlugin.java`), so each leg is its own search and the runtime applies the D3
    fusion: rrf sums `1/(k + rank)`, weighted sums `weight × similarity`, relative_score sums weighted min-max normalized
    similarities; ties go by key. This is the main concealment of the target: the same request on an enterprise cluster
    would be one retriever.
13. **Recommendation.** The runtime fetches the example vectors with `_mget` and searches with Qdrant's average-vector
    strategy, `avg(positive) + (avg(positive) - avg(negative))`, excluding the examples (`must_not ids`).
14. **Keyword search.** BM25 is the default similarity of text fields; an analyzer with `bm25-k1`/`bm25-b` adds a custom
    similarity. One field is `match`; several are `multi_match` of type `most_fields` with `^boost` per field; phrase,
    prefix, fuzzy, wildcard, regex and `query_string` map to the queries of the same names. Full-text fields carry a
    `raw` keyword sub-field so that equality filters and sorting stay exact.
15. **Writes.** Insert, upsert and import use `_bulk` with `refresh=true`, so every write is visible to the next
    statement and `wait` needs no request (https://www.elastic.co/docs/reference/elasticsearch/rest-apis/refresh-parameter).
    on-conflict error is `create` (409 raises), ignore is `create` with 409 counted as skipped, update is `update` with
    `doc_as_upsert`, replace is `index`.
16. **Updates and deletes by query.** One constant painless script reads `params.set`, `params.remove`, `params.append`
    and `params.pull`; values never enter the script source, so hostile strings stay data. `if-version` is optimistic
    concurrency control: `if_seq_no` with the document's primary term read before the update
    (https://www.elastic.co/docs/reference/elasticsearch/rest-apis/optimistic-concurrency-control).
17. **Schema evolution.** add-field is a mapping update (a default is written with `_update_by_query`). Elasticsearch
    cannot remove or rename mapped fields, so rename-field adds the new field and moves the values, and drop-field removes
    the values while the mapping keeps the old field. rename copies the write-blocked index with `_clone` (hard-linked
    segments) and deletes the old one.
18. **Operations.** load and release are `_open` and `_close`; compact is `_forcemerge`; rebuild-index is a force merge
    into one segment, which rebuilds the per-segment HNSW graph; await-index waits for index health yellow with a
    timeout; flush is `_flush`.
19. **Snapshots.** An `fs` repository named `vecdsl` under `path.repo` (set in the compose file) holds snapshots; backup
    schedules are snapshot lifecycle policies (SLM, basic license), with five-field cron expressions converted to the
    six-field Quartz form Elasticsearch expects.
20. **Security.** The compose file enables security with the `elastic` user and the password from
    `VECDSL_ELASTICSEARCH_PASSWORD`. Roles are `PUT _security/role` with index privileges (basic license). Token
    authentication needs standard, PKI and auditing gold, SAML/OIDC platinum (`SRC/x-pack/plugin/security/.../Security.java`);
    these raise Unsupported.
21. **What is not there.** Transactions, document TTL, models and inference (enterprise), rerankers (enterprise),
    multi-vector fields (`rank_vectors`, enterprise), external tables and per-index deletion protection raise
    Unsupported with the reasons in COVERAGE.md.

## Pending rows closed (2026-11-15)

22. **Cursors.** A cursor is the key of the last hit of the previous page. A vector, keyword or
    recommendation search with a cursor sorts by `_score` descending, then by the key, and the runtime sends `search_after`
    with the cursor record's score and key. search_after continues after the sort values of the last hit, and the query and
    sort must stay the same (https://www.elastic.co/docs/reference/elasticsearch/rest-apis/paginate-search-results). The score
    comes from a probe: the same query in a bool must with an ids filter on the cursor, size 1 (a clause in filter context does
    not contribute to the score, https://www.elastic.co/docs/explore-analyze/query-filter/languages/querydsl). A cursor the search does not match gives no hits. Vector searches use a knn query with k and
    num_candidates 1000. Unsupported with offset, sort, group-by
    and mmr (the pgvector generator; sort because search_after needs the score order).
23. **Boosts on vector searches and recommendations.** function_score around a knn query (or the exact script_score or the
    sparse query), with the functions of note 13
    (https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-function-score-query,
    https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-knn-query). The score is the function_score
    `_score`, returned unchanged (relevance form): a boost combines with Elasticsearch's transformed similarity, so the result
    is not the D3 similarity; vecDSL 1.0 does not standardize boosted scores across targets. Boost with mmr: unsupported (the diversify retriever ranks the similarity of
    its kNN retriever).
24. **Recommendation clauses.** The generator builds the full search body with an empty `query_vector` wherever the vector
    goes (knn section, knn query, diversify retriever, script_score params); the runtime computes the average vector and
    fills every empty `query_vector`. mmr, group-by, with-total, sort and aggregate are then the vector search's clauses. A
    query by stored vector (`query id`) uses the same path, so group-by, mmr and exact search now work with it.
    Highlight on a recommendation is unsupported: highlighters extract the terms of the query
    (https://www.elastic.co/docs/reference/elasticsearch/rest-apis/highlighting), and a recommendation has no text.
25. **Hybrid search clauses** (runtime, `rt.hybrid`). The fused records within the bounds are the result set . explain and highlight are sent with every leg request; a fused record carries one explanation per leg
    (null where the leg did not return it) and the first highlight. group-by: each leg asks for the group field as a doc
    value (`docvalue_fields`), the runtime groups the fused records in fused order. cursor: the page after the cursor
    record in fused order. mmr: maximal marginal relevance over the first `candidates` fused records (10 times limit plus
    offset by default), lambda times the fused score minus (1 - lambda) times the highest D3 similarity of the diversity
    field to a picked record; the vectors are read with `_mget`. sort: an ids query of the
    page with the sort clauses, so the server's sort semantics apply (missing last, exact forms). with-total: the number of
    fused records. aggregate: a size 0 ids query of the fused records with the aggs
    (https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-ids-query). Unsupported: sort with group-by
    and mmr with group-by, sort by relevance (the fused order is the relevance order, and the ids
    query has no fused score). boost stays pending.
26. **Get by id within a slice.** An ids query with the slice filter on `_search` (size the number of keys); the runtime
    returns the records in the order of the keys, as `_mget` does.
27. **Scroll slices.** `slice {id, max}` needs a scroll or a point in time; the runtime opens a point in time on the index,
    searches `/_search` with `pit.id` (the index is left out of the path) and closes it
    (`open_point_in_time.json`, `close_point_in_time.json`). Without a scroll the server slices with a DocIdSliceQuery
    (`server/src/main/java/org/elasticsearch/search/slice/SliceBuilder.java`, `createSliceQuery`), so which records a slice
    holds is the server's; the slices of one count together hold every record. `slice 0 of 1` sends no slice.
28. **Update from records.** `_bulk` update operations with `doc` and without `doc_as_upsert` (runtime op `patch`). An
    update of a missing document without an upsert raises DocumentMissingException
    (`server/src/main/java/org/elasticsearch/action/update/UpdateHelper.java`, `prepareUpsert`); the runtime does not count
    such a record. `ids` keep the records whose key is listed (the validator requires ids or where on an update); a
    filter, a slice, field clauses, if-version and mode replace are unsupported (bulk operations address documents by id).

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. **Alias drop.** `remove` with `index: *` and `must_exist: true` fails on the first index without the alias. Change:
   `drop <alias>` is `DELETE /*,-.*/_alias/<name>` (404 only when no index has it; `if-exists` ignores it).
2. **Grouped kNN search.** Collapse `inner_hits` rerun the request's query; a top-level `knn` section is not part of it,
   so the inner hits scored 0. Change: a grouped search sends a `knn` query instead of the top-level section.
3. **Bit vectors.** Elasticsearch 9 reads a string vector in a document as base64 but a string `query_vector` as hex, so
   hex documents were stored as other bits. Change: bit vectors are arrays of signed bytes in documents and queries.

## Known limitations

- Basic license only: RRF and linear retrievers, inference and rerankers raise Unsupported; hybrid fusion
  runs in the runtime.
- Boosted vector scores are Elasticsearch's function_score `_score`, not the D3 similarity.
- A cursor page reads the first 1000 hits; a cursor further down gives no hits.
- The BM25 scores depend on Lucene's segment statistics; the same program can score differently after merges.
