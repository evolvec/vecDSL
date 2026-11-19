# vecDSL to Qdrant mapping

Note (2026-11-10): this table describes the demo mapping of 9 Nov. Full coverage changed several rows (partitions, tenants,
namespaces, array-append and array-remove, delete with limit, vector indexes on existing collections are now mapped);
`COVERAGE.md` is the current status of every construct.

Server 1.20.0 (`qdrant/qdrant:v1.20.0`), Python `qdrant-client` 1.20.0 (REST), TypeScript `@qdrant/js-client-rest`
1.20.0 (REST), Java `io.qdrant:client` 1.20.0 (gRPC). Checked 2026-11-09 against the documentation at qdrant.tech
(`DOC/x` abbreviates `https://qdrant.tech/documentation/x.md`), the REST specification of the server
(SPEC = `docs/redoc/master/openapi.json` of github.com/qdrant/qdrant at tag v1.20.0, commit 222b2e4), the gRPC
definitions (PROTO = `lib/api/src/grpc/proto/*.proto` at the same tag), the server source (SRV, same tag) and the SDK
sources at the pinned tags (PY = github.com/qdrant/qdrant-client v1.20.0, `qdrant_client/qdrant_client.py` and
`qdrant_client/http/models/models.py`; TS = github.com/qdrant/qdrant-js v1.20.0,
`packages/js-client-rest/src/qdrant-client.ts`; JV = github.com/qdrant/java-client v1.20.0,
`src/main/java/io/qdrant/client/QdrantClient.java` and the factories beside it).

Status: **native** (one SDK call), **concealed** (the generator or runtime composes documented calls),
**unsupported** (raises `Unsupported.of(...)`), **UNKNOWN** (not confirmed from documentation or source).

Notation: `c` is the client (`QdrantClient(url=, api_key=)` in Python, `new QdrantClient({url, apiKey})` in
TypeScript, `new QdrantClient(QdrantGrpcClient.newBuilder(host, grpcPort, tls).withApiKey(key).build())` in Java).
`F` is the mapped filter (rows 50 to 69), always in the JSON form of SPEC `Filter`; Python validates it into
`models.Filter`, TypeScript passes it unchanged, Java converts it to PROTO `Filter` (`QdrantRuntime.filter`). `id(k)`
is the point id of key `k` (note 3). Java request messages are built with their PROTO builders and sent with the
`...Async(request)` overloads of JV. In the generated code every row is a call into the runtime module of the host
language (`vecdsl_qdrant_runtime.py`, `vecdslQdrantRuntime.ts`, `QdrantRuntime.java`), which makes the calls listed
here.

## Deployment and connection

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 1 | self-hosted server | native | Docker Compose service `qdrant/qdrant:v1.20.0`, REST 6333, gRPC 6334, named volume on `/qdrant/storage` | same | same | SRV `Dockerfile` (`EXPOSE 6333`, `EXPOSE 6334`, `WORKDIR /qdrant`), SRV `config/config.yaml` (`storage_path: ./storage`); SRV `.github/workflows/docker-image.yml` (tag `qdrant/qdrant:<ref>`) |
| 2 | create connection uri | native | `QdrantClient(url=uri)` | `new QdrantClient({url: uri})` | `QdrantGrpcClient.newBuilder(host(uri), grpcPort, https)` | PY `QdrantClient.__init__`; TS `QdrantClientParams`; JV `QdrantGrpcClient.newBuilder(String, int, boolean)` |
| 3 | connection host, port, tls | concealed | URI `http(s)://host:port` | same | host, gRPC port, TLS flag | as row 2 |
| 4 | grpc-port | native (Java) | not used (REST) | not used (REST) | gRPC port, default 6334 | SRV `config/config.yaml` (`grpc_port: 6334`) |
| 5 | api-key env("X"); else optional `VECDSL_QDRANT_API_KEY` | native (note 1) | `api_key=` | `apiKey` | `withApiKey(key)` (header `api-key`) | DOC/security; SRV `config/config.yaml` (`api_key`) |
| 6 | api-key or token as text | unsupported (note 1) | | | | generator rule |
| 7 | connection timeout | native | `timeout=` (seconds) | `timeout` (milliseconds) | `withTimeout(Duration)` | PY `QdrantClient.__init__`; TS `QdrantClientParams.timeout`; JV `Builder.withTimeout` |
| 8 | path, in-memory, deployment embedded | unsupported (note 2) | | | | PY `QdrantClient(path=, location=":memory:")` exists, TS and JV have no local mode |
| 9 | connection extension parameters | unsupported | | | | |

## Collections

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 10 | create collection | native | `c.create_collection(name, vectors_config={f: VectorParams}, sparse_vectors_config=, optimizers_config=, strict_mode_config=, metadata=)` | `c.createCollection(name, {vectors, sparse_vectors, optimizers_config, strict_mode_config, metadata})` | `createCollectionAsync(CreateCollection)` | DOC/manage-data/collections; SPEC `CreateCollection`; PROTO `CreateCollection` |
| 11 | if-not-exists | concealed | `c.collection_exists(name)` before row 10 | `c.collectionExists(name)` | `collectionExistsAsync(name)` | SPEC `/collections/{collection_name}/exists` |
| 12 | drop collection, if-exists | native | `c.delete_collection(name)` (after row 11 for if-exists) | `c.deleteCollection(name)` | `deleteCollectionAsync(name)` | SPEC `DELETE /collections/{collection_name}` |
| 13 | primary key (int, bigint, int8, varchar, text, uuid) | concealed (note 3) | point id `id(k)`, key stored in the payload | same | same | DOC/manage-data/points (ids are unsigned integers or UUIDs) |
| 14 | index on the primary key | concealed (note 3) | `integer` index (int keys) or `keyword` index (text keys), created with the collection | same | same | DOC/manage-data/indexing |
| 15 | primary key of other types, vector primary key | unsupported | | | | |
| 16 | auto-id | unsupported | | | | point ids are given by the client (SPEC `PointStruct.id` required) |
| 17 | consistency strong | concealed (note 13) | `consistency=ReadConsistencyType.ALL` on reads, `ordering=WriteOrdering.STRONG` on writes | `consistency: 'all'`, `ordering: 'strong'` | `ReadConsistency{type: All}`, `WriteOrdering{type: Strong}` | SPEC `ReadConsistency`, `WriteOrdering`; PROTO `ReadConsistencyType`, `WriteOrderingType` |
| 18 | consistency eventual | native | defaults (factor 1, weak ordering) | same | same | SPEC `ReadConsistency` ("Default value is `Factor(1)`"), `WriteOrdering` ("`weak` ... default") |
| 19 | consistency bounded, session | unsupported (note 13) | | | | SPEC `ReadConsistency` (replica counts only) |
| 20 | tags, description | native | `metadata={"tags": [...], "description": ...}` | `metadata` | `CreateCollection.metadata` | SPEC `CreateCollection.metadata` ("Arbitrary JSON metadata for the collection") |
| 21 | alias for collection (in the collection) | native | `c.update_collection_aliases([CreateAliasOperation])` after row 10 | `c.updateCollectionAliases({actions: [{create_alias}]})` | `createAliasAsync(alias, name)` | SPEC `/collections/aliases`; PROTO `AliasOperations` |
| 22 | create alias (statement) | native | as row 21 | as row 21 | as row 21 | as row 21 |
| 23 | swap-alias a from x to y | native | `c.update_collection_aliases([DeleteAliasOperation, CreateAliasOperation])` in one request | `actions: [{delete_alias}, {create_alias}]` | `updateAliasesAsync(List.of(delete, create))` | SPEC `ChangeAliasesOperation` |
| 24 | extension qdrant strict_mode = true | native | `strict_mode_config=StrictModeConfig(enabled=True)` | `strict_mode_config: {enabled}` | `StrictModeConfig.setEnabled` | SPEC `StrictModeConfig.enabled` |
| 25 | extension qdrant optimizers (indexing-threshold, memmap-threshold, default-segment-number, max-segment-size, flush-interval) | native | `optimizers_config=OptimizersConfigDiff(...)` | `optimizers_config` | `OptimizersConfigDiff` | SPEC `OptimizersConfigDiff`; PROTO `OptimizersConfigDiff` |
| 26 | other collection extension parameters and items | unsupported | | | | |
| 27 | ttl, ttl-field, deletion-protection, external source, cross-ref, embedding-model | unsupported | | | | SPEC `CreateCollection` (no such settings) |
| 28 | partitions, tenants, namespaces, partition-key | unsupported (note 14) | | | | DOC/manage-data/collections |
| 29 | id-type, storage, stored-source, generative-model, collection limits | concealed | recorded as notes | same | same | |
| 30 | dynamic | native | payloads are schemaless JSON | same | same | DOC/manage-data/payload |

## Fields and indexes

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 31 | vector-field float_vector dim d metric m | native | named vector `{"size": d, "distance": "Cosine" \| "Dot" \| "Euclid" \| "Manhattan"}` | same JSON | `VectorParams{size, distance}` | DOC/manage-data/vectors; SPEC `VectorParams`, `Distance` |
| 32 | several vector fields | native | one named vector per field | same | `VectorParamsMap` | SPEC `VectorsConfig` |
| 33 | float16_vector, uint8_vector | native | `"datatype": "float16" \| "uint8"` | same | `Datatype.Float16`, `Datatype.Uint8` | SPEC `Datatype` |
| 34 | int8, bfloat16, float64, binary vectors | unsupported | | | | SPEC `Datatype` |
| 35 | multivector comparator max_sim | native | `"multivector_config": {"comparator": "max_sim"}` | same | `MultiVectorConfig{MaxSim}` | SPEC `MultiVectorConfig` |
| 36 | sparse_float_vector, modifier idf | native | `sparse_vectors_config={f: SparseVectorParams(modifier="idf")}` | `sparse_vectors` | `SparseVectorConfig` | SPEC `SparseVectorParams`, `Modifier` |
| 37 | metric hamming, jaccard | unsupported | | | | SPEC `Distance` |
| 38 | vector-field on-disk | native | `"memory": "cold"` | same | `Memory.Cold` | SPEC `VectorParams.memory` |
| 39 | vector-index family hnsw (auto) max-degree, build-candidates | native | `"hnsw_config": {"m", "ef_construct"}` | same | `HnswConfigDiff{m, ef_construct}` | DOC/manage-data/indexing; SPEC `HnswConfigDiff` |
| 40 | vector-index family flat | native | `"hnsw_config": {"m": 0}` | same | same | SPEC `HnswConfigDiff.m` ("Set to 0 to disable the main HNSW graph") |
| 41 | vector-index on-disk | native | `"hnsw_config": {"memory": "cold"}` | same | `HnswConfigDiff.memory` | SPEC `HnswConfigDiff`; PROTO `HnswConfigDiff.memory` |
| 42 | vector-index ivf, vamana; index on an existing collection | unsupported | | | | SPEC `HnswConfigDiff` |
| 43 | search-candidates, centroids, accelerator, online, normalized | concealed | recorded as notes | same | same | |
| 44 | quantization scalar (bits 8) | native | `"quantization_config": {"scalar": {"type": "int8"}}` | same | `ScalarQuantization{Int8}` | DOC/manage-data/quantization; SPEC `ScalarQuantizationConfig` |
| 45 | quantization binary bits 1, 1.5, 2 | native | `{"binary": {"encoding": "one_bit" \| "one_and_half_bits" \| "two_bits"}}` (1 bit is the default) | same | `BinaryQuantizationEncoding` | SPEC `BinaryQuantizationEncoding` |
| 46 | quantization product (sub-vectors), truncate-dim, other bit widths | unsupported | | | | SPEC `ProductQuantizationConfig` (compression ratio) |
| 47 | payload-index kind keyword, integer, float, datetime, bool, uuid, geo (prefix-match, on-disk) | native | `c.create_payload_index(name, field_name=, field_schema=KeywordIndexParams(prefix=, memory=) ...)` | `c.createPayloadIndex(name, {field_name, field_schema})` | `createPayloadIndexAsync(name, field, PayloadSchemaType, PayloadIndexParams, true, null, null)` | DOC/manage-data/indexing; SPEC `PayloadSchemaParams`; PROTO `PayloadIndexParams` |
| 48 | payload-index kind full_text; field full-text, analyzer, bm25 function inputs | native (note 10) | `field_schema=TextIndexParams(type="text", tokenizer=, stopwords=, stemmer=, ascii_folding=, lowercase=, min_token_len=, max_token_len=, phrase_matching=True, scoring={"type": "bm25"})` | same JSON | `TextIndexParams` | DOC/search/text-search/full-text-search; SPEC `TextIndexParams`, `TextScoringInterface` |
| 49 | payload-index kind json; two indexes on one field; payload index on a vector field | unsupported | | | | SPEC `PayloadSchemaParams` |

## Filters

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 50 | and, or, not | native | `{"must": [...]}`, `{"should": [...]}`, `{"must_not": [...]}` | same | `Filter.addMust/addShould/addMustNot` | DOC/search/filtering; SPEC `Filter` |
| 51 | eq, neq on strings, integers, booleans | native | `{"key": f, "match": {"value": v}}`; neq under `must_not` | same | `Match.keyword/integer/boolean` | SPEC `MatchValue` |
| 52 | eq, neq on float and datetime fields | concealed (note 7) | `{"key": f, "range": {"gte": v, "lte": v}}` | same | `Range`, `DatetimeRange` | SRV `lib/segment/src/payload_storage/condition_checker.rs` (`MatchValue` compares strings, integers, booleans) |
| 53 | gt, gte, lt, lte, between | native | `{"key": f, "range": {...}}` (RFC 3339 strings for datetimes) | same | `Range`; `DatetimeRange` when the bound is a string | SPEC `Range`, `DatetimeRange` |
| 54 | in, not_in | native | `{"match": {"any": [...]}}`; not_in under `must_not`; floats as a `should` of closed ranges | same | `Match.keywords/integers` | SPEC `MatchAny` |
| 55 | contains, contains_any, contains_all (arrays) | native | element match `{"match": {"value": v}}`, `{"match": {"any": [...]}}`, `must` of element matches | same | as rows 51, 54 | DOC/search/filtering (arrays match when one element matches) |
| 56 | prefix; like 'p%' | native | `{"match": {"prefix": p}}` | same | `Match.prefix` | SPEC `MatchPrefix` |
| 57 | like '%s%' | native | `{"match": {"substring": s}}` | same | `Match.substring` | SPEC `MatchSubstring` (1.20) |
| 58 | like without wildcards | native | `{"match": {"value": s}}` | same | `Match.keyword` | SPEC `MatchValue` |
| 59 | like with other patterns | unsupported | | | | SPEC `Match` |
| 60 | is_empty | native | `{"is_empty": {"key": f}}` | same | `IsEmptyCondition` | SPEC `IsEmptyCondition` |
| 61 | is_null, not_exists; is_not_null, exists (non-array fields) | concealed (note 8) | `is_empty`; under `must_not` for the positive tests | same | same | SPEC `IsEmptyCondition`, `IsNullCondition` |
| 62 | null tests on arrays | unsupported (note 8) | | | | |
| 63 | match, match_any_term, contains_text | native | `{"match": {"text_any": q}}` | same | `Match.text_any` | SPEC `MatchTextAny` |
| 64 | match_all_terms | native | `{"match": {"text": q}}` | same | `Match.text` | SPEC `MatchText` |
| 65 | phrase | native | `{"match": {"phrase": q}}` (text indexes are created with `phrase_matching`) | same | `Match.phrase` | SPEC `MatchPhrase`, `TextIndexParams.phrase_matching` |
| 66 | fuzzy, wildcard, regex text operators | unsupported | | | | SPEC `Match` |
| 67 | length op n (arrays) | native | `{"values_count": {...}}` | same | `ValuesCount` | SPEC `ValuesCount` |
| 68 | geo radius, box, polygon | native | `geo_radius` (meters), `geo_bounding_box`, `geo_polygon` (ring closed by the generator) | same | `GeoRadius`, `GeoBoundingBox`, `GeoPolygon` | SPEC `GeoRadius`, `GeoBoundingBox`, `GeoPolygon` |
| 69 | any f (c) over arrays of objects; nested paths `a.b`, `a[].b`; raw paths | native | `{"nested": {"key": f, "filter": F}}`; keys as paths | same | `NestedCondition` | SPEC `NestedCondition`; DOC/search/filtering |
| 70 | geo relation with WKT | unsupported | | | | SPEC `Condition` |

## Writes

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 71 | insert values [...] | concealed (note 4) | `c.retrieve(ids=)` then `c.upsert(points=, update_mode=UpdateMode.INSERT_ONLY, wait=, ordering=)` | `c.retrieve`, `c.upsert({points, update_mode: 'insert_only'})` | `retrieveAsync(GetPoints)`, `upsertAsync(UpsertPoints{update_mode: InsertOnly})` | SPEC `PointsList.update_mode`, `UpdateMode` |
| 72 | insert on-conflict ignore | native | as row 71 without the error | same | same | SPEC `UpdateMode` ("`insert_only` - only insert new points") |
| 73 | insert on-conflict update, replace; upsert | native | `c.upsert(points=)` | `c.upsert` | `upsertAsync` | DOC/manage-data/points |
| 74 | batch n | native | upserts of n points | same | same | |
| 75 | wait | native (note 5) | `wait=True` | `wait: true` | `setWait(true)` | SPEC `wait` query parameter ("result will be returned only when all changes are applied") |
| 76 | extension qdrant ordering = "weak" \| "medium" \| "strong" | native | `ordering=WriteOrdering(...)` | `ordering` | `WriteOrdering{type}` | SPEC `WriteOrdering` |
| 77 | write consistency one, quorum, all | concealed | recorded as a note (Qdrant sets `write_consistency_factor` per collection) | same | same | SPEC `CreateCollection.write_consistency_factor` |
| 78 | update set {...} ids / where | concealed (note 6) | targets by retrieve or scroll, then `c.set_payload(payload=, points=)` and `c.update_vectors(points=[PointVectors])` | `c.setPayload`, `c.updateVectors` | `setPayloadAsync(SetPayloadPoints)`, `updateVectorsAsync(UpdatePointVectors)` | DOC/manage-data/payload; SPEC `SetPayload`, `UpdateVectors` |
| 79 | update remove-fields | native | `c.delete_payload(keys=, points=)` | `c.deletePayload` | `deletePayloadAsync(DeletePayloadPoints)` | SPEC `DeletePayload` |
| 80 | update mode replace | native | `c.overwrite_payload(payload=, points=)` (key kept) and row 78 for vectors | `c.overwritePayload` | `overwritePayloadAsync(SetPayloadPoints)` | SPEC `/collections/{collection_name}/points/payload` (PUT) |
| 81 | update from records | concealed | per record as row 78 | same | same | |
| 82 | update if-version; array-append, array-remove | unsupported | | | | SPEC `PointsList.update_filter` (conditions test the payload, not a version); SPEC `SetPayload` |
| 83 | delete ids / where | concealed (note 6) | targets, then `c.delete(points_selector=PointIdsList(points=))` | `c.delete({points})` | `deleteAsync(DeletePoints)` | SPEC `PointsSelector` |
| 84 | delete all | native | `c.count` then `c.delete(points_selector=FilterSelector(filter=Filter()))` | `c.delete({filter: {}})` | `PointsSelector{filter: {}}` | SPEC `FilterSelector` |
| 85 | delete limit, ttl, file record sources, namespace, tenant, partition | unsupported | | | | |

## Queries

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 86 | vector-search top k query vector v using f | native | `c.query_points(name, query=v, using=f, query_filter=F, limit=k, offset=, with_payload=[key, ...], with_vectors=)` | `c.query(name, {query: v, using, filter, limit, offset, with_payload, with_vector})` | `queryAsync(QueryPoints{query: nearest})` | DOC/search/search; SPEC `QueryRequest` |
| 87 | score bounds min (similarity) | native (note 9) | `score_threshold=` (negated for Euclid and Manhattan) | same | `setScoreThreshold` | SPEC `QueryRequest.score_threshold` ("scores better than this threshold") |
| 88 | score bounds max; score native | concealed (note 9) | runtime filter on the converted score | same | same | |
| 89 | query sparse_vector, multi_vector | native | `query=SparseVector(...)`, `query=[[...]]` | same JSON | `VectorInput.sparse`, `multi_dense` | SPEC `VectorInput` |
| 90 | query id v (lookup-in) | concealed (note 16) | `c.retrieve(ids=[id(v)], with_vectors=[f])` then row 86 | same | same | SPEC `PointRequest` |
| 91 | exact; params exhaustive | native | `search_params=SearchParams(exact=True)` | `params: {exact}` | `SearchParams.exact` | SPEC `SearchParams.exact` |
| 92 | params candidates | native | `SearchParams(hnsw_ef=n)` | `params: {hnsw_ef}` | `SearchParams.hnsw_ef` | SPEC `SearchParams.hnsw_ef` |
| 93 | params rescore, oversampling | native | `SearchParams(quantization=QuantizationSearchParams(rescore=, oversampling=))` | same | `QuantizationSearchParams` | SPEC `QuantizationSearchParams` |
| 94 | params probes, scan-budget, timeout, filter-strategy | concealed | recorded as notes | same | same | SPEC `SearchParams` |
| 95 | target-recall (D15) | unsupported | | | | capability manifest `capabilities/qdrant.properties` (`target-recall=absent`); SPEC `SearchParams` |
| 96 | mmr lambda l candidates n | native (note 15) | `query=NearestQuery(nearest=v, mmr=Mmr(diversity=1 - l, candidates_limit=n))` | `{nearest, mmr}` | `NearestInputWithMmr` | SPEC `Mmr` ("A higher value favors diversity"); SRV `lib/shard/src/query/mmr/mod.rs` |
| 97 | prefetch stages (limit, using, filter, params, nested) | native | `prefetch=[Prefetch(query=, using=, limit=, filter=, params=, prefetch=)]` | `prefetch` | `PrefetchQuery` | DOC/search/hybrid-queries; SPEC `Prefetch` |
| 98 | stage weight | unsupported | | | | vecDSL D3 (weights belong to fusion) |
| 99 | search (keyword) query text q fields [f] | native (note 10) | `query=TextQuery(text=q), using=f` | `query: {text: q}, using` | `Query.text(TextQuery)` | DOC/search/text-search/full-text-search; SPEC `TextQuery` |
| 100 | analyzer bm25-k1, bm25-b | native | `TextQuery(text=TextQueryInput(query=, scoring=Bm25Scoring(bm25=Bm25Params(k=, b=))))` | `{text: {query, scoring: {bm25: {k, b}}}}` | `TextQuery.bm25` | SPEC `Bm25Params`; DOC/search/text-search/full-text-search |
| 101 | operator and; text-query phrase | concealed (note 11) | text condition (`text`, `phrase`) in the filter beside the BM25 ranking | same | same | SPEC `TextQuery` ("required or excluded terms belong in the request's filter") |
| 102 | keyword search over several fields; field boosts; min-should-match, fuzziness, slop; other text-query types | unsupported | | | | SPEC `QueryRequest.using` (one text index per `text` query) |
| 103 | hybrid-search fusion rrf fusion-k k | native (note 12) | `query_points(prefetch=[leg, ...], query=RrfQuery(rrf=Rrf(k=k + 1)))` | `{prefetch, query: {rrf: {k}}}` | `Query.rrf(Rrf{k})` | DOC/search/hybrid-queries; SPEC `Rrf`; SRV `lib/segment/src/common/reciprocal_rank_fusion.rs` |
| 104 | hybrid per-leg limit, filter, params, exact, vector-leg bounds | native | per-prefetch `limit`, `filter`, `params`, `score_threshold` | same | `PrefetchQuery` | SPEC `Prefetch` |
| 105 | hybrid fusion weighted, relative_score; leg weights; alpha | concealed (note 12) | `c.query_batch_points(requests=[QueryRequest per leg])`, fused in the runtime | `c.queryBatch` | `queryBatchAsync` | SPEC `/collections/{collection_name}/points/query/batch` |
| 106 | leg weights or alpha with rrf; fusion-k with other fusions | unsupported | | | | vecDSL D3 (rrf is unweighted) |
| 107 | recommend positive [...] negative [...] (ids or vectors), lookup-in | native (note 16) | `query=RecommendQuery(recommend=RecommendInput(positive=, negative=, strategy="average_vector")), lookup_from=LookupLocation(collection=, vector=)` | `{recommend: {...}}`, `lookup_from` | `Query.recommend(RecommendInput)` | DOC/search/explore; SPEC `RecommendInput`, `RecommendStrategy`, `LookupLocation` |
| 108 | extension qdrant discover target t { context p vs n } with score native | native | `query=DiscoverQuery(...)` or `ContextQuery(...)` without target | `{discover}`, `{context}` | `Query.discover`, `Query.context` | DOC/search/explore; SPEC `DiscoverInput`, `ContextPair` |
| 109 | discover without score native | unsupported | | | | vecDSL D3 (discovery scores are not similarities) |
| 110 | extension qdrant feedback | unsupported | | | | SPEC `NaiveFeedbackStrategyParams` (`a`, `b`, `c` required) |
| 111 | get from ... ids [...] (with-vectors) | native | `c.retrieve(name, ids=[id(k)], with_payload=[key, ...], with_vectors=)` | `c.retrieve` | `retrieveAsync(GetPoints)` | SPEC `PointRequest` |
| 112 | count, count where F, count exact | native | `c.count(name, count_filter=F, exact=True)` | `c.count(name, {filter, exact: true})` | `countAsync(name, F, true)` | SPEC `CountRequest` |
| 113 | scroll limit n offset m (integer key) | native (note 17) | `c.scroll(scroll_filter=F, limit=m + n, order_by=OrderBy(key=key))`, first m dropped | `c.scroll({order_by})` | `scrollAsync(ScrollPoints{order_by})` | SPEC `ScrollRequest.order_by`, `OrderBy` |
| 114 | scroll (text key) | concealed (note 17) | paginated `c.scroll(offset=)`, sorted by key in the runtime | same | same | SPEC `ScrollRequest.offset` |
| 115 | scroll sort-by f asc/desc (integer, float or datetime index) | native | `order_by=OrderBy(key=f, direction=)` | same | `OrderBy` | SPEC `OrderBy` |
| 116 | scroll slice i of n | native | condition `{"slice": {"index": i, "total": n}}` | same | `SliceCondition` | SPEC `SliceCondition` (1.19) |
| 117 | scroll cursor, several sort clauses, sort by relevance, sort without a range index | unsupported | | | | SPEC `OrderBy` |
| 118 | output [...], exclude [...], with-vectors | native | `with_payload=[key, ...]`, `with_vectors=[names]` | `with_payload`, `with_vector` | `WithPayloadSelector.include`, `WithVectorsSelector.include` | SPEC `WithPayloadInterface`, `WithVector` |
| 119 | batch-query | concealed | one helper method per query | same | same | |
| 120 | explain, rerank, boost, group-by, with-total, highlight, sort and aggregate on searches, within, as-of, cursor, model, query extension parameters | unsupported | | | | SPEC `QueryRequest` (group-by is `query_points_groups`, boosting is `FormulaQuery`; neither is generated) |
| 121 | alter, describe, snapshot, restore, distance-matrix, sample, truncate, list, flush, compact, load, release | see COVERAGE.md | | | | mapped in the full-coverage pass |

## Notes

1. Qdrant takes the API key in the `api-key` header (DOC/security). The local server of `docker-compose.yml` sets
   `QDRANT__SERVICE__API_KEY` to `VECDSL_QDRANT_API_KEY` or the placeholder `vecdsl-local-key`. A program reads the
   variable named by the script's `api-key env("X")` (required) or, without it, `VECDSL_QDRANT_API_KEY` if set. A key
   written as text is unsupported, as for Pinecone. `/healthz`, `/livez` and `/readyz` need no key
   (SRV `src/actix/mod.rs`, auth whitelist).
2. Only the Python client has a local mode (`QdrantClient(path=...)`, `location=":memory:"`; PY `QdrantClient.__init__`).
   The generator keeps the three languages on one server.
3. Point ids are unsigned integers or UUIDs (DOC/manage-data/points; SPEC `ExtendedPointId`). The runtime derives
   `id(k)` as UUIDv5 in the DNS namespace of the key's decimal or string text, the convention of the Weaviate
   generator (identical UUIDs, checked against `weaviate.util.generate_uuid5`). The key is stored in the payload under
   its own name, typed, so filters, scroll order and results use it; an `integer` or `keyword` index is created on it.
4. Existing ids are retrieved first, so `on-conflict error` can fail before writing and the counts are exact;
   `update_mode: insert_only` keeps a concurrent insert from being overwritten.
5. vecDSL `wait` is Qdrant's `wait` (true). Without it the runtime sends `wait=false`, Qdrant's REST default
   (the Python and Java helpers default to true, so the runtime always passes the flag).
6. Update and delete resolve their targets first (retrieve for ids, paginated scroll for filters), then write by
   id: counts are exact in the three languages. Native filtered writes return no count.
7. `match.value` compares strings, integers and booleans only (SRV `condition_checker.rs`), so equality on float
   and datetime fields becomes a closed range. Integral floats sent for integer fields are converted to integers
   by the runtimes (Python and Java), since a stored `52.0` does not match the integer 52.
8. `is_empty` matches a missing field, null and an empty array (SPEC `IsEmptyCondition`); `is_null` matches only an
   explicit null. vecDSL null tests (absent or null) use `is_empty` on non-array fields; on arrays they would also
   match `[]`, so they are unsupported there. `is_empty` itself maps natively.
9. Qdrant scores: Cosine and Dot are similarities; Euclid and Manhattan return distances
   (SRV `lib/segment/src/spaces/simple.rs`, `postprocess`: `score.abs().sqrt()` and `score.abs()`). Canonical
   similarity (D3): cosine and dot unchanged, euclidean and manhattan negated. A lower similarity bound is passed as
   `score_threshold` (negated for distances, where Qdrant keeps scores below the threshold); an upper bound and
   native-form bounds are applied by the runtime on the returned list.
10. Keyword search is BM25 over a full-text index with `scoring` (new in 1.20; https://qdrant.tech/blog/qdrant-1.20.x/,
    DOC/search/text-search/full-text-search). IDF is `ln(1 + (N - df + 0.5) / (df + 0.5))`
    (SRV `lib/segment/src/data_types/query_context.rs`, `fancy_idf`), the term score `idf * tf * (k1 + 1) /
    (tf + k1 * (1 - b + b * len / avgdl))` (SRV `.../full_text_index/inverted_index/bm25/mod.rs`), k1 1.2 and b 0.75 by
    default. The sparse output of a vecDSL bm25 function is not stored.
11. A `text` query scores points that hold any query term (SPEC `TextQuery`); operator `and` adds a `text` match
    (all terms) and `text-query phrase` a `phrase` match to the filter.
12. Qdrant's RRF scores position p (from 0) as `1 / (p + k)` with weight 1 (SRV `reciprocal_rank_fusion.rs`,
    `position_score`), D3 as `1 / (k + rank)` with rank from 1, so the runtime sends `k + 1`. Qdrant's own RRF weights
    stretch positions instead of multiplying scores, so D3's unweighted rrf is kept. Weighted and relative_score
    fusion (D3) have no server form (DBSF is a different normalization); the legs go in one batched request and are
    fused in the runtime, ties broken by key.
13. Qdrant reads at a replica count (`ReadConsistency`: a number, `majority`, `quorum`, `all`) and orders writes
    weak, medium or strong (SPEC). vecDSL `consistency strong` becomes reads from all replicas and strong write
    ordering; with `wait` on writes this gives read-your-writes. `eventual` keeps the defaults; `bounded` and
    `session` have no counterpart.
14. Qdrant separates tenants by a payload field with a tenant index (`is_tenant`) or by custom sharding; the
    generator derives neither from vecDSL partitions, tenants or namespaces.
15. Qdrant's MMR objective is `lambda * relevance - (1 - lambda) * max_similarity_to_selected` with
    `lambda = 1 - diversity` (SRV `lib/shard/src/query/mmr/mod.rs`; SRV `.../universal_query/collection_query.rs`), so
    vecDSL's lambda (weight of relevance) is sent as `diversity = 1 - lambda`. The returned score is the original
    similarity, not the MMR objective (same file: "Use original score, already postprocessed"), so it converts as
    note 9.
16. The Query API removes every point id referenced in the query from the results of the searched collection
    (SRV `lib/collection/src/operations/universal_query/collection_query.rs`, `try_into_shard_request` and
    `exclude_referenced_ids`; DOC/search/explore states it for discovery and context search). Recommendation therefore
    never returns its examples; `average_vector` computes `avg(positive) + avg(positive) - avg(negative)`
    (SPEC `RecommendStrategy`, DOC/search/explore), the formula of the Pinecone runtime. The same rule would drop the
    query point from a nearest-by-id query, so `query id` looks the vector up first (row 90), and the point stays in
    the results as on the other targets.
17. Qdrant's scroll `offset` is a point id, not a count, and `order_by` needs an integer, float or datetime index
    (SPEC `ScrollRequest`, `OrderBy`). Integer keys therefore scroll natively in key order; text keys (keyword
    index, no range order) are sorted by the runtime after a full scroll. A repeated page offset ends a scroll.

## Known limitations

- One field per keyword query.
- `bounded` and `session` consistency are unsupported.
- Partitions are shard keys, which need the cluster mode: the environment runs a one-peer cluster.
- Embedded deployments are unsupported, and so are ttl, if-version, transactions, release, generation,
  models and rerankers.
- The TypeScript SDK's `recoverSnapshot` cannot pass `wait`; the TypeScript runtime calls the generated API client
  (`client.api().recoverFromSnapshot`) for every recovery.
