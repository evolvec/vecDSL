# vecDSL to OpenSearch: mapping notes

How the OpenSearch generator maps vecDSL 7.1 onto a self-hosted OpenSearch 3.9.0 node with the bundled plugins (k-NN,
neural-search, security, index-management). COVERAGE.md has one row per grammar construct with its status and reference;
 References below use these prefixes:

- `SRC/` https://github.com/opensearch-project/OpenSearch/blob/3.9.0/
- `KNN/` https://github.com/opensearch-project/k-NN/blob/3.9.0.0/src/main/java/org/opensearch/knn/
- `NEURAL/` https://github.com/opensearch-project/neural-search/blob/3.9.0.0/src/main/java/org/opensearch/neuralsearch/
- `API/` https://github.com/opensearch-project/opensearch-api-specification/blob/2265a43e93a03e5cf216bdc82979cef089995c0a/spec/namespaces/
- `DOCS/` https://docs.opensearch.org/3.9/

The generator started as a copy of the Elasticsearch generator, so every note says whether OpenSearch kept the Elasticsearch mapping (`../elasticsearch/MAPPING.md`, cited
as "ES note n") or diverged from it.

## Versions (checked 2026-11-12)

| part | version | source |
|---|---|---|
| server | `opensearchproject/opensearch:3.9.0` (k-NN and neural-search 3.9.0.0) | https://github.com/opensearch-project/OpenSearch/releases/tag/3.9.0 |
| Python | `opensearch-py` 3.2.0, `>=3.2.0,<3.3`, Python 3.10 or later | https://pypi.org/project/opensearch-py/ |
| TypeScript | `@opensearch-project/opensearch` 3.10.0, TypeScript 5.9.3, `@types/node` 22.20.5 | https://www.npmjs.com/package/@opensearch-project/opensearch |
| Java | `org.opensearch.client:opensearch-java` 3.10.0 (generic client over `ApacheHttpClient5Transport`), Gson 2.13.2, release 17 | https://github.com/opensearch-project/opensearch-java/releases/tag/v3.10.0 |

## Notes

1. **One request model, three thin runtimes (kept, ES note 1).** The generator builds every request body at generation
   time; `vecdsl_opensearch_runtime.py`, `vecdslOpenSearchRuntime.ts` and `OpenSearchRuntime.java` send them through the
   transport layer of the official clients (`client.transport.perform_request`, `client.transport.request`,
   `OpenSearchGenericClient.execute`) and convert scores, fuse hybrid lists and average recommendation vectors. Every
   client call carries the connection timeout (30 s default): opensearch-py reads a `timeout` query parameter as its own
   request timeout when no `request_timeout` is given (`Transport.perform_request`,
   https://github.com/opensearch-project/opensearch-py/blob/v3.2.0/opensearchpy/transport.py), so the runtime always
   passes `timeout=` itself.
2. **Status codes in Java (diverged in the mechanism).** `ApacheHttpClient5Transport.convertResponse` returns 400, 401,
   403, 404 and 405 answers and throws `ResponseException` for the other error statuses
   (https://github.com/opensearch-project/opensearch-java/blob/v3.10.0/java-client/src/main/java/org/opensearch/client/transport/httpclient5/ApacheHttpClient5Transport.java);
   the runtime catches it, takes the status and parses the body from the message, so an `if-version` conflict (409)
   takes the same path as in Python and TypeScript. Elasticsearch's `Rest5Client` returns the answer
   instead (ES note 2).
3. **Collections are indices; logical databases are name prefixes; tenants, namespaces and partitions are keyword fields
   with filtered aliases (kept, ES notes 3 to 5).** Index APIs: `API/indices.yaml`.
4. **Every index is a k-NN index (diverged).** `index.knn: true` is required for `knn_vector` fields that use a native
   engine (`DOCS/vector-search/settings/`). The generator also sets `index.knn.derived_source.enabled: false`: with derived
   source on (the default for k-NN indexes since 3.0, same page), `_source` vectors are rebuilt from the Faiss index, and a
   `cosinesimil` field returns normalized vectors, so `with-vectors` would not return the vectors that were written.
5. **Vector fields (diverged).** `float_vector`, `float16_vector`, `int8_vector` and `binary_vector` map to `knn_vector`
   with `data_type` float, half_float, byte and binary (`DOCS/mappings/supported-field-types/knn-vector/`,
   `KNN/index/mapper/KNNVectorFieldMapper.java`); `bfloat16_vector` is a float field with the Faiss `sq` encoder of type
   `bf16`. `uint8_vector` and `float64_vector` raise Unsupported (byte vectors are signed int8). Dimensions go up to
   16,000. Elasticsearch maps the same types to `dense_vector` with `element_type` (ES note 6).
6. **Method and quantization (diverged).** Every dense field gets `method: {name: hnsw, engine: faiss}` with `m`,
   `ef_construction` and `ef_search` from the declared index (`DOCS/mappings/supported-field-types/knn-methods-engines/`).
   scalar 16 bits (the default) is the Faiss `sq` encoder of type fp16; 1, 2 and 4 bits are `sq` with `bits`; 7 bits is the
   `sq` encoder of the Lucene engine; binary 1, 2 and 4 bits is the Faiss `binary` encoder. `ivf` and product quantization
   need a model trained with the Train API on vectors that exist before the field, which vecDSL cannot declare, and raise
   Unsupported; so do `vamana` (the jvector plugin is not in the image) and GPU builds (a separate remote build service).
   `on-disk` without a quantization is `mode: on_disk`. Elasticsearch has `int8_hnsw`, `int4_hnsw` and `bbq_hnsw` index
   options instead (ES note 7); 8-bit scalar quantization therefore exists on Elasticsearch and not on OpenSearch.
7. **The method is fixed (diverged in the reason, same result).** Engine, parameters and encoder of a `knn_vector` field
   cannot be updated after the field is created (same methods page), so a vector index or quantization added to an
   existing field, and dropping either, raise Unsupported.
8. **Scores (D3, diverged).** `_score` is the space's score translation (`KNN/index/SpaceType.java`, `scoreTranslation`):
   cosinesimil `(1 + c) / 2`, l2 `1 / (1 + d²)`, innerproduct `1 / (1 - ip)` for ip below 0 and `ip + 1` otherwise, l1 and
   linf `1 / (1 + d)`, hamming `1 / (1 + d)`. The runtimes invert them: cosine `2s - 1`, l2 `-sqrt(1/s - 1)`, innerproduct
   `s - 1` when s ≥ 1 and `1 - 1/s` otherwise, l1, linf and hamming `-(1/s - 1)`. The exact path (note 9) on byte vectors
   with cosine scores `1 + c` (`KNN/plugin/script/KNNScoringSpace.java`), which the runtime turns into `s - 1`. The cosine
   and l2 forms equal Elasticsearch's; the inner product form does not (ES note 8 for `dot_product`).
9. **Exact search (diverged).** `exact`, a flat index, a metric other than the field's, `filter-strategy pre_filter` and
   `exhaustive` produce a `script_score` query with the `knn_score` script of language `knn` (`DOCS/vector-search/
   vector-search-techniques/knn-score-script/`); manhattan is `l1`. Elasticsearch uses Painless vector functions (ES note 9).
10. **The knn query (diverged).** `{"query": {"knn": {field: {vector, k, filter, method_parameters: {ef_search},
    rescore: {oversample_factor}}}}}` (`DOCS/query-dsl/specialized/k-nn/`). `candidates` becomes `ef_search`, at least k;
    `oversampling` on a quantized field becomes `rescore`. `target-recall`, `probes` and `scan-budget` raise Unsupported
    (no recall target; nprobes belongs to ivf; hnsw has no visit bound). Elasticsearch has a top-level `knn` section with
    `num_candidates` (ES note 10).
11. **MMR (diverged).** `mmr lambda` is `ext.mmr {diversity: 1 - lambda, candidates, vector_field_path}` on a knn query
    (`DOCS/vector-search/specialized-operations/vector-search-mmr/`, `KNN/search/processor/mmr/MMRRerankProcessor.java`).
    The node needs `cluster.search.enabled_system_generated_factories: mmr_over_sample_factory,mmr_rerank_factory`, which
    the compose file sets. Hits keep their knn score. Elasticsearch uses the `diversify` retriever (ES note 11).
12. **Hybrid search (diverged: rrf is native).** With rrf fusion and equal leg limits the generator sends a `hybrid` query
    with `pagination_depth` and an inline `search_pipeline` whose `score-ranker-processor` combines with technique `rrf`
    and `rank_constant` = fusion-k (`DOCS/vector-search/ai-search/hybrid-search/rrf/`,
    `NEURAL/processor/normalization/RRFNormalizationTechnique.java`). The runtime only re-sorts equal fused scores by key
    (D3 ties). Weighted and relative_score fusion, and rrf with different leg limits, stay in the runtime as on
    Elasticsearch, where every server-side fusion needs an enterprise license (ES note 12): the min-max normalization of
    neural-search keeps a floor of 0.001 and the arithmetic mean divides by the weights of the present subqueries
    (`NEURAL/processor/normalization/MinMaxScoreNormalizationTechnique.java`,
    `NEURAL/processor/combination/ArithmeticMeanScoreCombinationTechnique.java`), which D3's relative_score does not.
13. **Recommendation (kept, ES note 13).** The runtime fetches the examples and searches with the average-vector strategy;
    byte and binary fields raise Unsupported because the average is no vector of the field.
14. **Sparse vectors (diverged).** `sparse_float_vector` fields map to `rank_features`; a search is a `neural_sparse`
    query with `query_tokens`, scored by the dot product of the weights
    (`DOCS/vector-search/ai-search/neural-sparse-with-raw-vectors/`, `NEURAL/query/NeuralSparseQueryBuilder.java`). Weights
    must be positive. The Elasticsearch generator uses `sparse_vector` fields.
15. **Keyword search, writes, updates and deletes by query (kept, ES notes 14 to 16).** BM25, `match`, `match_phrase`,
    `multi_match most_fields`, `query_string`, `prefix`, fuzziness; `_bulk` with `refresh=true`; one constant Painless
    script for updates; `if_seq_no` and `if_primary_term` for `if-version` (`DOCS/api-reference/document-apis/update-document/`).
16. **Returned fields (diverged in form).** `_source` takes `includes` and `excludes`, and the generator lists the vector
    fields in `excludes` unless the output asks for them (`API/_core.yaml`, search); the Elasticsearch generator sets
    `exclude_vectors` instead.
17. **Schema evolution (kept, ES note 17).** add-field is a mapping update, rename-field and drop-field move or remove values
    with `_update_by_query`, rename is write block, `_clone`, delete, unblock.
18. **Operations (diverged in load).** load is `_open`, a wait for yellow health and the k-NN warmup
    (`GET /_plugins/_knn/warmup/<index>`, `DOCS/vector-search/api/knn/#warmup-operation`), which loads the native graphs
    into memory; release is `_close`; compact and rebuild-index are `_forcemerge`.
19. **Snapshots (kept) and schedules (diverged).** An `fs` repository `vecdsl` under `path.repo` holds snapshots; restore
    closes the index first. Backup schedules are Snapshot Management policies (`POST /_plugins/_sm/policies/<name>`,
    `DOCS/tuning-your-cluster/availability-and-recovery/snapshots/sm-api/`) with a five-field cron in UTC, so vecDSL cron
    passes through; Elasticsearch SLM takes a six-field Quartz cron (ES note 19).
20. **Security (diverged).** The security plugin is on with its demo configuration: user `admin`, password from
    `VECDSL_OPENSEARCH_PASSWORD` through `OPENSEARCH_INITIAL_ADMIN_PASSWORD`, HTTP without TLS on 127.0.0.1. Roles are
    `PUT /_plugins/_security/api/roles/<name>` with index permissions and action groups (`API/security.yaml`); audit-log is
    a `PATCH` of `/config/enabled` on the audit API (`DOCS/security/api/audit/patch-audit-configuration/`). Token, mTLS, SSO
    and client certificates are authentication domains in `config.yml` on the server
    (`DOCS/security/configuration/configuration/`), so they raise Unsupported. On Elasticsearch the same constructs are
    license questions (ES note 20); OpenSearch has no license tiers (Apache 2.0, `SRC/LICENSE.txt`).
21. **What is not there.** Transactions, document TTL (Index State Management deletes whole indices), models, generation
    and rerankers (ML Commons needs a registered and deployed model, `DOCS/ml-commons-plugin/`), continuous import (the
    pull-based ingestion plugins are not in the image, `DOCS/im-plugin/pull-based-ingestion/index/`), embedded mode,
    collection limits other than the field limit, bitmap indexes, external collections. COVERAGE.md gives each reason.
22. **Agreement with Elasticsearch, in numbers.** Both COVERAGE.md files have the same 456 rows. 452 rows have the same
    status on both targets; the 4 that differ are `Method HybridSearch`, `HybridSearch.fusion`,
    `HybridSearch.fusionConstant` and `HybridSearch.legs`, native on OpenSearch (note 12) and concealed on Elasticsearch.
    The other divergences in notes 2 to 20 change how a construct is generated, or which variants of a row raise
    Unsupported (8-bit scalar quantization on OpenSearch, uint8 on both, float16 on Elasticsearch), not the row's status.

## Native rows

Each note says where OpenSearch 3.9.0 keeps the Elasticsearch design (Elasticsearch MAPPING.md notes 22 to 28) and where it
differs. Nothing was assumed equal: every difference below has its own reference.

23. **Cursors (kept, ES note 22; diverged in k).** A cursor is the key of the last hit of the previous page (pgvector decision
    36). A vector, keyword or recommendation search with a cursor sorts by `_score` descending, then by the key, and the runtime
    sends `search_after` with the cursor record's score and key, read by a probe (the same query in a bool must with an ids
    filter, size 1). search_after continues after the sort values of the last hit
    (https://docs.opensearch.org/3.9/search-plugins/searching-data/paginate/). Difference: a knn query takes k per segment
    (MAPPING.md, server finding 2), so the cursor page's knn query with k 1000 reads the 1000 nearest records of
    each segment, a superset of the global 1000 nearest; the page after the cursor is the same as long as the cursor lies within
    the global window. No num_candidates (Elasticsearch only). Unsupported with offset, sort, group-by and mmr.
24. **Boosts on vector searches and recommendations (kept, ES note 23; diverged in candidates).** function_score around the knn
    query (or the exact script_score query, or the neural_sparse query), with the functions of note 13
    (https://docs.opensearch.org/3.9/query-dsl/compound/function-score/, https://docs.opensearch.org/3.9/query-dsl/specialized/k-nn/).
    Difference: the knn query's k per segment would let the functions rank more than the k nearest records, so the runtime first
    finds the global top k of the knn query alone and restricts the knn filter to those ids (`top_k`, as for totals and groups).
    The score is the function_score `_score` (relevance form), not the D3 similarity (vecDSL 1.0 leaves it target-defined). Boost with mmr is unsupported: the MMR reranker takes only a knn or neural query as the top-level query
    (https://docs.opensearch.org/3.9/vector-search/specialized-operations/vector-search-mmr/, Limitations).
25. **Recommendation clauses (kept, ES note 24; diverged in MMR).** The generator builds the vector search's body with a null
    `vector` in the knn query (also inside a function_score query); the runtime computes the average vector and fills it.
    mmr, boost, group-by, with-total, sort and aggregate are then the vector search's clauses, with `top_k` where the vector
    search has it. MMR is the `ext.mmr` reranker of the request, not Elasticsearch's diversify retriever; it reads the knn
    query's hits and their stored vectors, so the filled vector is the only change. Highlight is unsupported (note 30).
26. **Get by id within a slice (kept, ES note 26; diverged in source filtering).** An ids query with the slice filter on
    `_search` (size the number of keys); the runtime returns the records in the order of the keys, as `_mget` does.
    OpenSearch has no `exclude_vectors`; the vector fields go to `_source.excludes`, as in the rest of the generator (note 16).
27. **Scroll slices (kept in design, ES note 27; diverged in the API).** The point-in-time API of OpenSearch is
    `POST /<index>/_search/point_in_time?keep_alive=1m` (answer `pit_id`) and `DELETE /_search/point_in_time` with
    `{"pit_id": [...]}`; a search with a PIT names no index in the path
    (https://docs.opensearch.org/3.9/api-reference/search-apis/point-in-time-api/), and search slicing splits a PIT search into
    slices `{id, max}` (https://docs.opensearch.org/3.9/search-plugins/searching-data/point-in-time/, Search slicing).
    Elasticsearch's endpoints are `_pit` and `/_pit`, with the id in `id`. The runtime opens and closes a PIT around each sliced
    request; which records a slice holds is the server's; `slice 0 of 1` sends no slice.
28. **Hybrid result clauses wait for the shared module (diverged from ES note 25).** OpenSearch's hybrid query has its own
    sort, search_after, collapse and aggregations, but they work per subquery before normalization: a field sort fetches each
    subquery's hits by the sort and returns null scores
    (https://docs.opensearch.org/3.9/vector-search/ai-search/hybrid-search/sorting/), search_after needs that sort, collapse groups
    each subquery's hits before fusion (https://docs.opensearch.org/3.9/vector-search/ai-search/hybrid-search/collapse/, the
    results may differ from the collapse response processor), and aggregations count every document a subquery matches. vecDSL
    applies these clauses to the fused records within the bounds, and the weighted and relative_score
    fusions run in the runtime anyway, so cursor, with-total, sort, aggregate, group-by, mmr and boost on a hybrid
    search stay pending for the shared result-shape module of generator/common . Note 32
    wires them.
29. **Hybrid explain (diverged from ES note 25 on the rrf path).** rrf runs in the server's temporary search pipeline (decision
    8), and explain on a hybrid query needs the `hybrid_score_explanation` response processor in that pipeline
    (https://docs.opensearch.org/3.9/vector-search/ai-search/hybrid-search/explain/); the generator adds it to the pipeline's
    `response_processors` with `explain: true`, and each hit carries the server's hybrid explanation. The documentation shows
    the processor with the normalization processor; that it explains the score-ranker-processor too is not
    documented. Weighted and relative_score: explain true in every leg request, a fused record carries one explanation per
    leg (null where the leg did not return it), as on Elasticsearch.
30. **Highlight and track_scores.** Highlight on a vector search and on a recommendation is unsupported: highlighters mark the
    terms of a text query and a knn query has none (observed on Elasticsearch
    9.5.6 and Vespa 8.781.11). Highlight on a hybrid search needs a keyword leg; the hybrid query passes the highlighter's visitor
    to every subquery (neural-search `HybridQuery.visit`,
    https://github.com/opensearch-project/neural-search/blob/main/src/main/java/org/opensearch/neuralsearch/query/HybridQuery.java),
    so the keyword leg's terms are marked; on the runtime paths every leg request asks for the highlight and a fused record keeps
    the first. track_scores: the search API returns scores with a field sort only with `track_scores=true`
    (https://docs.opensearch.org/3.9/api-reference/search-apis/search/, sort), as on Elasticsearch, so a field sort
    sends `"track_scores": true`. A vector search's sort also runs on the global top k (`top_k`), like totals, aggregations, groups and the
    post filter (MAPPING.md, server finding 2).
31. **Update from records (kept, ES note 28).** `_bulk` update operations with `doc` and without `doc_as_upsert` (runtime op
    `patch`); without an upsert and with doc_as_upsert false a missing document raises DocumentMissingException
    (https://github.com/opensearch-project/OpenSearch/blob/3.9.0/server/src/main/java/org/opensearch/action/update/UpdateHelper.java,
    `prepareUpsert`), a 404 bulk item, which the runtime does not count. ids keep the records whose key is listed; a filter, a
    slice, field clauses, if-version and mode replace are unsupported. Float vector fields keep their decimal points (MAPPING.md, server finding 6), as in every other write.

## Shared result-shape module

32. **Hybrid result clauses through the shared result-shape module (diverged from ES note 25).** cursor, with-total, sort,
    aggregate, boost, mmr and group-by with size on a hybrid search go to `rt.shape`, which hands the candidates and the
    statement's spec to the shared module of generator/common/shapes (`vecdsl_shapes.py`, `vecdslShapes.ts`, `Shapes.java`,
    written next to the runtime of every generated project; contract in generator/common/shapes/README.md). The spec is `ShapeSpec.of(statement)` with
    the field names the index has after rename-field, `keyField` the index key and, where ShapeSpec leaves it null, the diversity
    field's metric (`OpenSearchShapes`). The candidates are the fused records of the candidate window `ShapeSpec.window()`:
    ten times limit plus offset, or `max(candidates, limit + offset)` when mmr writes candidates (generator/common/shapes/README.md, "The window rule"). On the
    rrf path the hybrid query asks for that many hits (`size` the window, no `from`), and `pagination_depth` stays the leg limit
    (https://docs.opensearch.org/3.9/vector-search/ai-search/hybrid-search/pagination/: the depth bounds each subquery per
    shard), so the fusion is the one of note 12; on the runtime fusion `rt.hybrid` keeps that many fused records with offset 0,
    without bounds and unrounded. The legs keep their limits, because the relative_score normalization and the rrf ranks depend
    on them. Each candidate request reads the key, the output fields, the fields the spec reads (group, sort, boost and
    aggregation fields), the diversity field's stored vector and, with with-vectors, every vector field (`_source.includes`;
    with `output *` every field but the vectors the module does not need); explain and highlight pass through. Offset, cursor,
    bounds, sort, groups, totals and aggregations are the module's. **Window rule, diverged from the pgvector generator:** totals,
    aggregations, groups and boosts cover the window, not every record the search matches; a group whose best record lies
    outside the window is missing, and a boost cannot raise a record from outside it. With the default leg limits the fused set
    (at most the sum of the leg limits) fits in the window, so the window holds every fused record unless leg limits are written
    larger or the mmr candidates are fewer than the fused records. Boosts follow the pgvector generator: the
    functions apply to the fused score with their combine and apply modes. The result forms are the module's (pgvector's): a
    with-total or aggregate result is `{hits, total, aggregations}`, and an aggregation value prints as pgvector prints it (a max
    is a number), not as OpenSearch's aggregation answer on a vector search (`{"value": ...}`). A cursor follows the module:
    the page after the cursor record in the fused order, with an offset applied after it (generator/common/shapes/README.md), where the
    cursor of a vector, keyword or recommendation search refuses an offset (note 23). The refusals of the pgvector generator are
    raised at generation time by ShapeSpec; the module's UnsupportedShape maps to the runtime's unsupported error. A sparse
    diversity field is unsupported (MMR compares dense vectors). Kept native, the module not used: cursor, boost, mmr,
    group-by, with-total, sort and aggregate of vector, keyword and recommendation searches (notes 10, 11, 23 to 25, 30) and the
    sort and cursor of scrolls (note 27), because OpenSearch has an operator for each of them.

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. **Deleted documents in the Faiss graph.** A knn query without a filter fills its k slots from the graph, deleted
   copies included, and drops them afterwards, so `k 2` could return one hit. A force merge does not purge them at once,
   because soft-delete retention leases keep them. Change: every knn query carries a filter, `match_all` when the
   statement has none; with a filter the graph search runs on the live documents' bitset. `compact` also sends
   `only_expunge_deletes` before `max_num_segments`.
2. **k per segment.** A knn query takes k per Lucene segment; totals, aggregations, collapse groups and a post filter
   see every segment's hits, so their answers depend on how documents fall into segments. Change: for `with-total`,
   `aggregate`, `group-by` and the `post_filter` strategy the runtime first finds the global top k (`top_k`), then runs
   the request restricted to those ids, which is the meaning these clauses have on Elasticsearch.
3. **hybrid with rrf** keeps a run-dependent order of the lower-ranked hits: the knn leg runs inside the search pipeline
   with k per segment, which a second request cannot restrict.
4. **System indices.** The demo security configuration writes `security-auditlog-*` and the query insights plugin
   writes `top_queries-*`; both appeared in `list collections`. Change: the compose file sets
   `plugins.security.audit.type: noop` and disables the three top_queries metrics.
5. **Alias drop.** `remove` with `index: *` and `must_exist: true` fails on the first index without the alias. Change:
   `drop <alias>` is `DELETE /*,-.*/_alias/<name>` (404 only when no index has it).
6. **Float vectors need a decimal point.** The k-NN MMR reranker reads stored vectors from `_source` and casts every
   element to Double; an integral float written without a fraction is stored as an Integer and the search fails with
   400. Change: the runtimes write float and half_float vector fields with a decimal point; byte and binary vectors
   stay integers.
7. **Dropping an alias with the security plugin.** `"index": "*"` also names `.opendistro_security`, which even the
   admin user may not change (403). Change: the request names `["*", "-.*"]` (vecDSL index names never start with
   a dot).

## Known limitations

- `update ... set` of a vector field goes through `_update_by_query` script parameters and still sends integral
  elements without a fraction (server finding 6).
- `rescore.oversample_factor` accepts values in [1.0, 100.0] (https://docs.opensearch.org/3.9/query-dsl/specialized/k-nn/);
  the generator passes the declared oversampling without checking that range, so the server reports a value outside it.
- hybrid search with rrf returns a run-dependent order of its lower-ranked hits (server finding 3).
- Boost with mmr on a vector search is unsupported; `KeywordSearch.mmr` is unsupported.
- The cursor of a knn search reads the first 1000 hits.
