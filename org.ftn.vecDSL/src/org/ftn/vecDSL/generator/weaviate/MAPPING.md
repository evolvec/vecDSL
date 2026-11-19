# vecDSL to Weaviate mapping

Server 1.40.4, Python `weaviate-client` 4.24.1, TypeScript `@weaviate/node` 3.15.3, Java `io.weaviate:client6` 6.3.1.
Checked 2026-11-08 against docs.weaviate.io, the tested snippets the docs pages are built from
(github.com/weaviate/docs, `_includes/code`) and the SDK sources at the pinned tags.

Status: **native** (one SDK call), **concealed** (the generator or runtime composes documented calls),
**unsupported** (raises `Unsupported.of(...)`). COVERAGE.md (2026-11-10) is the complete construct matrix; this file keeps
the type, filter and search details its rows refer to, and the rows below are updated to agree with it.

Notation: `client` is the connected client; `col` is the collection handle, obtained with
`client.collections.use("Products")` in all three SDKs. `U(k)` is the deterministic object UUID of primary key `k`
(note 2). `F` is the mapped filter (rows 55 to 63). In the generated code every row is a call into the runtime module
of the host language (`vecdsl_weaviate_runtime.py`, `vecdslWeaviateRuntime.ts`, `WeaviateRuntime.java`), which makes
the SDK calls listed here.

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 1 | create connection local uri | native | `weaviate.connect_to_custom(http_host=, http_port=, http_secure=, grpc_host=, grpc_port=, grpc_secure=)`; `client.close()` | `await weaviate.connectToCustom({httpHost, httpPort, httpSecure, grpcHost, grpcPort, grpcSecure})`; `await client.close()` | `WeaviateClient.connectToCustom(c -> c.scheme(s).httpHost(h).httpPort(p).grpcHost(h).grpcPort(g))`; `client.close()` | https://docs.weaviate.io/weaviate/connections/connect-local |
| 2 | collection name | concealed (note 1) | `"Products"` | `'Products'` | `"Products"` | https://docs.weaviate.io/weaviate/manage-collections/collection-operations |
| 3 | create collection | native | `client.collections.create(name, properties=[...], vector_config=[...], inverted_index_config=...)` | `await client.collections.create({name, properties, vectorizers, invertedIndex})` | `client.collections.create(name, c -> c.properties(...).vectorConfig(...).invertedIndex(...))` | https://docs.weaviate.io/weaviate/manage-collections/collection-operations |
| 4 | create ... if-not-exists | concealed | `client.collections.exists(name)` then create | `await client.collections.exists(name)` then create | `client.collections.exists(name)` then create | https://docs.weaviate.io/weaviate/manage-collections/collection-operations |
| 5 | drop collection | native | `client.collections.delete(name)` | `await client.collections.delete(name)` | `client.collections.delete(name)` | https://docs.weaviate.io/weaviate/manage-collections/collection-operations |
| 6 | drop ... if-exists | concealed (note 3) | `exists` then `delete` | `exists` then `delete` | `exists` then `delete` | https://docs.weaviate.io/weaviate/manage-collections/collection-operations |
| 7 | consistency strong / bounded / session / eventual | concealed (note 4) | `col.with_consistency_level(ConsistencyLevel.ALL)` | `col.withConsistency('ALL')` | `col.withConsistencyLevel(ConsistencyLevel.ALL)` | https://docs.weaviate.io/weaviate/concepts/replication-architecture/consistency |
| 8 | primary id (int, varchar, uuid) | concealed (note 2) | `generate_uuid5(str(k))` as object id; key stored as a property | `generateUuid5(String(k))`; key stored as a property | runtime UUIDv5 (no SDK helper); key stored as a property | https://docs.weaviate.io/weaviate/manage-objects/create |
| 9 | field type int | native | `Property(name, data_type=DataType.INT)` | `{name, dataType: 'int'}` | `new Property.Builder(name, "int")` | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 10 | field type float | native | `DataType.NUMBER` | `'number'` | `"number"` | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 11 | field type varchar / text | native | `DataType.TEXT` | `'text'` | `"text"` | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 12 | varchar max-length | concealed (note 5) | runtime check before each write | runtime check | runtime check | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 13 | array element-type varchar / int / float / bool / date / uuid | native | `DataType.TEXT_ARRAY`, `INT_ARRAY`, `NUMBER_ARRAY`, ... | `'text[]'`, `'int[]'`, `'number[]'`, ... | `"text[]"`, `"int[]"`, `"number[]"`, ... | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 14 | array field-limit array_len | concealed (note 5) | runtime check before each write | runtime check | runtime check | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 15 | json with sub-fields | native | `DataType.OBJECT` with `nested_properties=[...]` | `'object'` with `nestedProperties` | `"object"` with `.nestedProperties(...)` | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 16 | json without sub-fields | unsupported (note 6) | | | | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 17 | nullable | concealed (note 7) | no flag | no flag | no flag | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 18 | default value | concealed | runtime fills the default before each insert | same | same | https://docs.weaviate.io/weaviate/config-refs/datatypes |
| 19 | analyzer tokenizer standard / whitespace / keyword | concealed | `tokenization=Tokenization.WORD` / `WHITESPACE` (`LOWERCASE` with a lowercase filter) / `FIELD` | `tokenization: 'word'` ... | `.tokenization(Tokenization.WORD)` ... | https://docs.weaviate.io/weaviate/config-refs/collections |
| 20 | analyzer language "en" / stopwords "en" or "none" | concealed (note 8) | `Configure.inverted_index(stopwords_preset=StopwordsPreset.EN)` | `configure.invertedIndex({stopwordsPreset: 'en'})` | `InvertedIndex` `.stopwords(s -> s.preset("en"))` | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 21 | analyzer ascii_folding filter | native | `text_analyzer=Configure.text_analyzer(ascii_fold=True)` | `textAnalyzer: {asciiFold: true}` | `.textAnalyzer(TextAnalyzer.of(t -> t.foldAscii(true)))` | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 22 | analyzer stemming, length, synonym, decompounder, phonetic filters; ngram, edge_ngram, icu tokenizers | unsupported | | | | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 23 | full-text analyzer on a field | native | `index_searchable=True` | `indexSearchable: true` | `.indexSearchable(true)` | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 24 | vector-field float_vector dim n | concealed (note 9) | `Configure.Vectors.self_provided(name=, vector_index_config=...)` | `vectors.selfProvided({name, vectorIndexConfig})` | `VectorConfig.selfProvided(name, c -> c.vectorIndex(...))` | https://docs.weaviate.io/weaviate/manage-collections/vector-config |
| 25 | vector-index hnsw max-degree, build-candidates, search-candidates | native | `Configure.VectorIndex.hnsw(max_connections=, ef_construction=, ef=, distance_metric=)` | `configure.vectorIndex.hnsw({maxConnections, efConstruction, ef, distanceMetric})` | `Hnsw.of(h -> h.maxConnections(m).efConstruction(e).ef(f).distance(d))` | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 26 | vector-index flat with cosine | native | `Configure.VectorIndex.flat(distance_metric=)` | `configure.vectorIndex.flat({distanceMetric})` | `Flat.of()` | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 27 | vector-index flat with another metric | unsupported (note 21) | | | | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 28 | vector-index ivf, vamana, sparse_inverted; GPU | unsupported | | | | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 28a | quantization scalar (8 bits), binary, product | native (COVERAGE.md, Concept Quantization) | `Configure.VectorIndex.Quantizer.sq/bq/pq` | `configure.vectorIndex.quantizer.*` | SDK JSON adapter | https://docs.weaviate.io/weaviate/configuration/compression |
| 29 | metric cosine / dot / euclidean / manhattan / hamming | native, score concealed (note 10) | `VectorDistances.COSINE`, `DOT`, `L2_SQUARED`, `MANHATTAN`, `HAMMING` | `'cosine'`, `'dot'`, `'l2-squared'`, `'manhattan'`, `'hamming'` | `Distance.COSINE`, ... | https://docs.weaviate.io/weaviate/config-refs/distances |
| 30 | metric jaccard | unsupported | | | | https://docs.weaviate.io/weaviate/config-refs/distances |
| 31 | sparse_float_vector field + function bm25 | concealed (note 11) | BM25 on the function's input properties | same | same | https://docs.weaviate.io/weaviate/search/bm25 |
| 32 | sparse_float_vector field without bm25 function | unsupported | | | | https://docs.weaviate.io/weaviate/manage-collections/vector-config |
| 32a | text-embedding functions; vector field model; collection embedding-model | native (COVERAGE.md, Concept EmbeddingModel) | `Configure.Vectors.custom(module_name="text2vec-<provider>")` | vectorizer `{name, config}` | SDK JSON adapter | https://docs.weaviate.io/weaviate/model-providers |
| 33 | payload-index kind keyword (inside create) | concealed | `tokenization=Tokenization.FIELD, index_filterable=True` | `tokenization: 'field', indexFilterable: true` | `.tokenization(Tokenization.FIELD).indexFilterable(true)` | https://docs.weaviate.io/weaviate/config-refs/collections |
| 34 | payload-index kind integer / float / datetime | native | `index_range_filters=True` | `indexRangeFilters: true` | `.indexRangeFilters(true)` | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 35 | payload-index on an existing collection | native (note 12) | REST `PUT /schema/{c}/properties/{p}/index/{i}` | same | same | https://github.com/weaviate/weaviate/blob/v1.40.4/openapi-specs/schema.json |
| 36 | inverted-index (extension 17.3) | native | `Configure.inverted_index(bm25_k1=, bm25_b=, index_timestamps=, index_null_state=, index_property_length=, stopwords_preset=)` | `configure.invertedIndex({bm25k1, bm25b, indexTimestamps, indexNullState, indexPropertyLength, stopwordsPreset})` | `InvertedIndex` `bm25`, `indexTimestamps`, `indexNulls`, `indexPropertyLength`, `stopwords` | https://docs.weaviate.io/weaviate/config-refs/indexing/inverted-index |
| 37 | partitions | unsupported | | | | https://docs.weaviate.io/weaviate/manage-collections/multi-tenancy |
| 37a | tenants, namespaces (as tenants), aliases, collection ttl | native (COVERAGE.md) | `tenants.create`, `alias.create`, `Configure.ObjectTTL` | same | same | https://docs.weaviate.io/weaviate/manage-collections/multi-tenancy |
| 37b | auto-id, cross-ref | native in Weaviate; generation pending | | | | https://docs.weaviate.io/weaviate/manage-collections/cross-references |
| 38 | insert into ... values | concealed (note 13) | existence check, then `col.data.insert_many([DataObject(properties=, uuid=, vector={...})])` | `await col.data.insertMany([{properties, id, vectors}])` | `col.data.insertMany(List.of(WeaviateObject.of(o -> o.uuid(u).properties(p).vectors(...))))` | https://docs.weaviate.io/weaviate/manage-objects/import |
| 39 | insert ... wait | concealed (note 14) | no call | no call | no call | https://docs.weaviate.io/weaviate/manage-objects/import |
| 40 | upsert; insert on-conflict update / replace | concealed (note 13) | `col.data.replace(uuid=, properties=, vector=)` for existing ids, `insert_many` for new | `col.data.replace({id, properties, vectors})`, `insertMany` | `col.data.replace(uuid, r -> ...)`, `insertMany` | https://docs.weaviate.io/weaviate/manage-objects/update |
| 41 | update set ... ids | native | `col.data.update(uuid=U(k), properties={...})` per id | `await col.data.update({id: U(k), properties})` | `col.data.update(U(k), u -> u.properties(...))` | https://docs.weaviate.io/weaviate/manage-objects/update |
| 42 | update set ... where; update mode replace | concealed | fetch matching uuids with `fetch_objects(filters=F)`, then `update` (`replace` in replace mode) per object | same | same | https://docs.weaviate.io/weaviate/manage-objects/update |
| 43 | update array-append, array-remove, if-version | unsupported | | | | https://docs.weaviate.io/weaviate/manage-objects/update |
| 43a | update remove-fields | native in Weaviate (replace without the fields); generation pending | | | | https://docs.weaviate.io/weaviate/manage-objects/update |
| 44 | delete ids | native | `col.data.delete_many(where=Filter.by_id().contains_any([U(k)...]))` | `await col.data.deleteMany(col.filter.byId().containsAny([...]))` | `col.data.deleteMany(Filter.uuid().containsAny(...))` | https://docs.weaviate.io/weaviate/manage-objects/delete |
| 45 | delete where | native | `col.data.delete_many(where=F)` | `await col.data.deleteMany(F)` | `col.data.deleteMany(F)` | https://docs.weaviate.io/weaviate/manage-objects/delete |
| 46 | delete all; delete with limit | concealed (COVERAGE.md, Delete) | `delete_many(Filter.by_id().not_equal(nil UUID))`; first n in key order, then `delete_many` | same | same | https://docs.weaviate.io/weaviate/manage-objects/delete |
| 47 | get by ids, output [...] | native | `col.query.fetch_objects(filters=Filter.by_id().contains_any([...]), return_properties=[...])` | `await col.query.fetchObjects({filters, returnProperties})` | `col.query.fetchObjects(q -> q.filters(Filter.uuid().containsAny(...)).returnProperties(...))` | https://docs.weaviate.io/weaviate/manage-objects/read |
| 48 | count [where] | native | `col.aggregate.over_all(total_count=True, filters=F).total_count` | `(await col.aggregate.overAll({filters})).totalCount` | `col.aggregate.overAll(a -> a.filters(F).includeTotalCount(true)).totalCount()` | https://docs.weaviate.io/weaviate/search/aggregate |
| 49 | vector-search top k using field | native | `col.query.near_vector(near_vector=v, target_vector=f, limit=k, filters=F, return_properties=, return_metadata=MetadataQuery(distance=True))` | `await col.query.nearVector(v, {targetVector, limit, filters, returnProperties, returnMetadata: ['distance']})` | `col.query.nearVector(Target.vector(f, v), q -> q.limit(k).filters(F).returnProperties(...).returnMetadata(Metadata.DISTANCE))` | https://docs.weaviate.io/weaviate/search/similarity |
| 50 | score similarity bounds min s / max s | concealed (note 10) | `distance=` max distance derived from min s; max s applied by the runtime | `distance:` | `.distance(d)` | https://docs.weaviate.io/weaviate/search/similarity |
| 51 | target-recall q | unsupported | | | | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 52 | query-time candidates (ef) | unsupported (note 15) | | | | https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index |
| 53 | autocut (extension parameter) | native | `auto_limit=n` | `autoLimit: n` | `.autolimit(n)` | https://docs.weaviate.io/weaviate/search/similarity |
| 54 | keyword search text fields [...] | native | `col.query.bm25(query=t, query_properties=[...], limit=k, filters=F, return_properties=, return_metadata=MetadataQuery(score=True))` | `await col.query.bm25(t, {queryProperties, limit, filters, returnProperties, returnMetadata: ['score']})` | `col.query.bm25(t, q -> q.queryProperties(...).limit(k).filters(F).returnProperties(...))` | https://docs.weaviate.io/weaviate/search/bm25 |
| 55 | keyword per-field boost; operator and; min-should-match | native | `query_properties=["title^2"]`; `operator=BM25Operator.and_()`; `BM25Operator.or_(minimum_match=n)` | `'title^2'`; `Bm25Operator.and()`; `Bm25Operator.or({minimumMatch})` | `"title^2"`; `SearchOperator.and()`; `SearchOperator.or(n)` | https://docs.weaviate.io/weaviate/search/bm25 |
| 56 | hybrid fusion rrf, fusion-k 60 | native | `col.query.hybrid(query=t, vector=v, target_vector=f, query_properties=[...], fusion_type=HybridFusion.RANKED, alpha=, limit=k)` | `await col.query.hybrid(t, {vector, targetVector, queryProperties, fusionType: 'Ranked', alpha, limit})` | `col.query.hybrid(Target.text(f, t), q -> q.nearVector(NearVector.of(v)).queryProperties(...).fusionType(FusionType.RANKED).alpha(a).limit(k))` | https://docs.weaviate.io/weaviate/search/hybrid |
| 57 | hybrid fusion-k other than 60; weighted fusion | unsupported (note 16) | | | | https://docs.weaviate.io/weaviate/concepts/search/hybrid-search |
| 58 | hybrid fusion relative-score; alpha; leg weights | native | `fusion_type=HybridFusion.RELATIVE_SCORE, alpha=` (leg weights become `alpha = wv / (wv + wk)`, default 0.5) | `fusionType: 'RelativeScore'` | `FusionType.RELATIVE_SCORE` | https://docs.weaviate.io/weaviate/search/hybrid |
| 59 | hybrid per-leg limit; per-leg filter; more than one leg of a kind | unsupported (note 17) | | | | https://docs.weaviate.io/weaviate/search/hybrid |
| 60 | recommend positive / negative using field | concealed (note 18) | fetch example vectors (`include_vector=[f]`), centroid in runtime, `near_vector` with `Filter.not_(Filter.by_id().contains_any(examples))` | same with `includeVector`, `Filters.not(...)` | same with `includeVector`, `Filter.not(Filter.uuid().containsAny(...))` | https://docs.weaviate.io/weaviate/search/filters |
| 61 | scroll limit n [where] | native (note 19) | `col.query.fetch_objects(limit=n, filters=F, sort=Sort.by_property(key), return_properties=[...])` | `await col.query.fetchObjects({limit, filters, sort, returnProperties})` | `col.query.fetchObjects(q -> q.limit(n).filters(F).sort(SortBy.property(key)).returnProperties(...))` | https://docs.weaviate.io/weaviate/api/graphql/additional-operators |
| 62 | output [...] projection | native | `return_properties=[...]` | `returnProperties: [...]` | `.returnProperties(...)` | https://docs.weaviate.io/weaviate/search/filters |
| 63 | filter eq, neq, lt, lte, gt, gte | native (note 22) | `Filter.by_property(p).equal / not_equal / less_than / less_or_equal / greater_than / greater_or_equal` | `col.filter.byProperty(p).equal / notEqual / lessThan / lessOrEqual / greaterThan / greaterOrEqual` | `Filter.property(p).eq / ne / lt / lte / gt / gte` | https://docs.weaviate.io/weaviate/search/filters |
| 64 | filter and, or, not | native | `Filter.all_of([...])`, `Filter.any_of([...])`, `Filter.not_(...)` | `Filters.and(...)`, `Filters.or(...)`, `Filters.not(...)` | `Filter.and`, `Filter.or`, `Filter.not` | https://docs.weaviate.io/weaviate/search/filters |
| 65 | filter in / not in | native | `contains_any([...])` / `contains_none([...])` | `containsAny` / `containsNone` | `containsAny` / `containsNone` | https://docs.weaviate.io/weaviate/search/filters |
| 66 | filter contains (array element); contains (text) | native; concealed | `contains_any([value])`; `like("*value*")` | `containsAny([value])`; `like` | `containsAny(value)`; `like` | https://docs.weaviate.io/weaviate/search/filters |
| 67 | filter contains-all | native | `contains_all([...])` | `containsAll` | `containsAll` | https://docs.weaviate.io/weaviate/search/filters |
| 68 | filter like, prefix, wildcard | concealed | `like` with `%` and `_` turned into `*` and `?` | same | same | https://docs.weaviate.io/weaviate/search/filters |
| 69 | filter match, match_any_term, contains_text / match_all_terms | concealed | `contains_any(terms)` / `contains_all(terms)` on the text property | same | same | https://docs.weaviate.io/weaviate/search/filters |
| 70 | filter phrase, fuzzy, regex | unsupported | | | | https://docs.weaviate.io/weaviate/search/filters |
| 71 | filter is_null, is_not_null, exists, not_exists | native, needs index-null-state | `is_none(True)` | `isNull(true)` | `isNull()` | https://docs.weaviate.io/weaviate/search/filters |
| 72 | filter length, is_empty | native, needs index-property-length | `Filter.by_property(p, length=True)` | `col.filter.byProperty(p, true)` | `Filter.propertyLen(p)` | https://docs.weaviate.io/weaviate/search/filters |
| 73 | filter geo radius | native | `within_geo_range(coordinate=GeoCoordinate(...), distance=)` | `withinGeoRange({latitude, longitude, distance})` | `withinGeoRange(lat, lon, distance)` | https://docs.weaviate.io/weaviate/search/filters |
| 74 | filter geo box, polygon, wkt; element predicates | unsupported | | | | https://docs.weaviate.io/weaviate/search/filters |
| 75 | filter on the primary key | concealed (note 2) | filters on the stored key property | same | same | https://docs.weaviate.io/weaviate/search/filters |
| 76 | filter nested path in json | unsupported (note 20) | | | | https://docs.weaviate.io/weaviate/search/filters |

## Notes

1. Collection names must start with an upper case letter and property names with a lower case letter
   (config-refs/collections). The generator capitalizes the first letter of the collection name and reports results
   under the vecDSL names. A field name that starts with an upper case letter, is reserved, or ends in a reserved
   suffix is unsupported.
2. Weaviate object ids are UUIDs, and `id`, `_id` and `_additional` are reserved property names
   (config-refs/collections, "Reserved words"). The object UUID is the UUIDv5 of the key, computed as
   `uuid5(NAMESPACE_DNS, str(key))`, with integral numbers written without a decimal point. Python `generate_uuid5`
   hashes `namespace + identifier` and TypeScript `generateUuid5` hashes `identifier + namespace`, so both are called
   with an empty namespace; Java has no helper, so the Java runtime implements UUIDv5 itself (key 1 gives
   b04965e6-a9bb-591f-8f8a-1adcb2c8dc39 in all three). The key is also stored as a property, under the field name, or
   under `<name>_key` when the field name is reserved (`id_key` in the demo), and is returned under the vecDSL field
   name. Filters on the key use the stored property.
3. The docs do not state what `collections.delete` does for a missing collection in Python, TypeScript or Java,
   so the generator checks `exists` first.
4. Weaviate consistency is per request: ONE, QUORUM, ALL. `strong` maps to ALL, `bounded` and `session` to QUORUM,
   `eventual` to ONE; a statement's write consistency (one, quorum, all) maps one to one. In Java client 6.3.1 and 6.3.2
   the `ConsistencyLevel` enum maps ONE, QUORUM and ALL to the gRPC value `CONSISTENCY_LEVEL_ONE`
   (`ConsistencyLevel.java`, lines 13 to 24), so Java gRPC requests always run at ONE. No effect on a single node;
   it matters on a replicated cluster.
5. The data types page states no length limit for `text` or arrays. The runtime checks `max-length` (characters, for
   strings and for each string of an array), `array_len` and the vector `dim` before each insert, upsert and update,
   and raises an error that names the field.
6. `object` stores nested JSON, and every documented example declares `nestedProperties`; no schemaless JSON type
   is listed. A vecDSL `json` field without sub-fields has no documented mapping.
7. The docs do not state whether properties are nullable by default. The runtime omits null values from written
   objects. Filtering on null needs `indexNullState` (inverted index).
8. Weaviate has no stemming and no language analyzers for Latin-script languages. `language "en"` maps to the `en`
   stopword preset, which is only applied with `word` tokenization; `ja` and `ko` on the language tokenizer map to
   `kagome_ja` and `kagome_kr`. Other languages are unsupported. `bm25-k1` and `bm25-b` on an analyzer set the
   collection's BM25 parameters; the Python client requires both, so a missing one takes the documented default
   (k1 1.2, b 0.75) in all three runtimes.
9. Self-provided vectors declare no dimension (none of the vector-config snippets passes one). The runtime checks
   the vector length against `dim` before each write.
10. Weaviate returns distances: cosine `1 - cos`, dot `-dot`, l2-squared `sum((a-b)^2)`, manhattan, hamming. Canonical
    similarity (D3): cosine `1 - d`, dot `-d`, euclidean `-sqrt(d)`, manhattan `-d`, hamming `-d`. A lower similarity
    bound `s` becomes the maximum distance `1 - s`, `-s` or `s^2`; for euclidean a positive `s` is rejected, as in
    Milvus. Weaviate has no minimum-distance argument, so an upper similarity bound is applied in the runtime. With
    `score native` scores are Weaviate distances and bounds are distance bounds. Scores and other floating-point
    results are rounded to six decimals, and integral values are reported as integers, so the three languages print
    the same results.
11. BM25 is built into the inverted index of every searchable `text` property, so Weaviate has no sparse vector
    field. A sparse field that is the output of a `bm25` function is not created; keyword search on it, or on the
    function's input fields, becomes `bm25` with `query_properties` set to the function's inputs.
12. Existing properties cannot be modified after creation (config-refs/collections, "Mutability"), but the 1.40 REST API
    upserts and rebuilds a property's inverted index (`schema.objects.index.upsert`, `schema.objects.index.rebuild` in the
    OpenAPI specification of the tag). None of the three SDKs has the call, so the runtimes send the REST request.
13. The docs say a duplicate id raises an error for single-object creation (manage-objects/create), but do not say what
    a batch import does with an existing id. Insert therefore checks existing ids with
    `Filter.by_id().contains_any` and fails on duplicates (or skips them with `on-conflict ignore`) before
    `insert_many`; upsert uses `replace` for existing ids and `insert_many` for new ones. Batch results report
    per-object errors (`has_errors`/`errors`, `hasErrors`/`errors`, `errors()`), which the runtime checks explicitly.
14. The docs do not state when an imported object becomes visible to queries. `wait` emits no call; the 2026-11-08 server
    run showed every write visible to the next query.
15. `ef` is an HNSW index setting (`search-candidates` on the index), changed through a collection update, not a
    per-query argument.
16. Ranked fusion scores each result as `1/(RANK + 60)` (concepts/search/hybrid-search); the constant is fixed.
    Weaviate fuses by rank or by normalized relative score, so `weighted` (raw score) fusion has no counterpart.
17. None of the three SDK `hybrid` signatures accepts per-leg limits or filters (Python `hybrid` arguments;
    TypeScript `HybridSearchOptions`; Java `Hybrid.Builder`), and a hybrid query has one text and one vector.
18. Same contract as the Milvus runtime `recommend_vector`: centroid of the positives, reflected away from the centroid
    of the negatives (`2c - a`), examples excluded from the result. The TypeScript id filter offers only `equal`,
    `notEqual` and `containsAny`, so all three runtimes exclude with `not(id containsAny ...)`.
19. Cursor paging (`after`) cannot be combined with filters (api/graphql/additional-operators); scroll pages with
    `limit` and `offset`, sorted by the key property so that every language returns the same order.
20. Filtering on nested object paths is a preview from Weaviate 1.38, gated by `WEAVIATE_PREVIEW_NESTED_FILTERING=on`
    (search/filters, "Filter on nested object properties"). Under D14 it is PROVISIONAL and is not generated.
21. The Java client's `Flat` index record has only `vectorCacheMaxObjects` (client6 6.3.1 `vectorindex/Flat.java`), so a
    flat index with a metric other than the cosine default cannot be created from Java; the generator rejects it for
    all three languages to keep their output identical.
22. Filter leaves on `number` properties carry `"number": true`; the Python and Java runtimes send their values as
    floating point. The TypeScript client sends integral JavaScript numbers as integer filter values
    (`FilterGuards.isInt` in `@weaviate/core`); the server (1.40.4) accepted them on `number` properties (the demo's
    `price lte 60` and `price lt 40`).

## Known limitations

- Java client 6.3.x sends every consistency level as gRPC ONE (`ConsistencyLevel.java`); no effect on one node.
- A vector field added with `alter` is not in the write layout of later inserts (the layout is built from the
  collection definition).
- Recommend and query-by-id look up example vectors without the tenant of the statement.
- Clone copies the schema as the server returns it; a server that rejects a read-only field in the POST would need the
  field removed.
- Partitions, logical databases and transactions are unsupported.
- `weaviate-client` prints an `AuthlibDeprecationWarning` from a dependency on stderr; harmless.
