# vecDSL to Pinecone mapping

API version 2026-07, Python `pinecone` 10.3.0, TypeScript `@pinecone-database/pinecone` 9.2.0, Java: REST API 2026-07
through `java.net.http` and Gson 2.13.2 (note 20). Checked 2026-11-08 against docs.pinecone.io, the OpenAPI
specifications of API 2026-07 (github.com/pinecone-io/pinecone-api at commit 7855e85, abbreviated SPEC-C for
`db_control_2026-07.oas.yaml`, SPEC-D for `db_data_2026-07.oas.yaml` and SPEC-I for `inference_2026-07.oas.yaml`) and the SDK sources at the pinned tags
(PY = github.com/pinecone-io/python-sdk at v10.3.0, TS = github.com/pinecone-io/pinecone-ts-client at v9.2.0).

Status: **native** (one SDK call), **concealed** (the generator or runtime composes documented calls),
**unsupported** (raises `Unsupported.of(...)`), **UNKNOWN** (not confirmed from documentation or source).

Full coverage (2026-11-11): `COVERAGE.md` is the construct matrix of every Method, Concept, AlterOperation and ExtensionItem
class and every Method feature (456 rows, generated from `VecDSL.ecore`). The rows below are the mapping notes of the demo
(2026-11-08), updated where the full coverage changed them; notes 24 to 37 hold the facts of the full coverage.

Notation: `pc` is the client (`Pinecone(api_key=, host=)` in Python, `new Pinecone({apiKey, controllerHostUrl, fetchApi})`
in TypeScript); `docs` is the documents handle of index `products` in namespace `__default__`
(`pc.index(name="products").documents` with `namespace=` on each call in Python;
`pc.index({name: "products", namespace: "__default__"}).documents` in TypeScript). Java calls are REST requests:
`C` is the control plane (`https://api.pinecone.io`), `D` is the index host from `GET C/indexes/products`, and
`D/docs/op` abbreviates `POST D/namespaces/__default__/documents/op`. Every request carries `Api-Key` and
`X-Pinecone-Api-Version: 2026-07` (SPEC-C `securitySchemes`). `F` is the mapped filter (rows 56 to 65). In the
generated code every row is a call into the runtime module of the host language (`vecdsl_pinecone_runtime.py`,
`vecdslPineconeRuntime.ts`, `PineconeRuntime.java`), which makes the calls listed here.

## Deployment and connection

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 1 | self-hosted server | unsupported (note 21) | | | | https://docs.pinecone.io/release-notes/2026 |
| 2 | create connection uri | native | `Pinecone(api_key=, host=uri)` | `new Pinecone({apiKey, controllerHostUrl: uri})` | base URL `C` | PY `pinecone/_client.py`; TS `src/data/vectors/types.ts` |
| 3 | api-key env("X") | native (note 23) | `api_key=os.environ["X"]` | `apiKey: process.env.X` | header `Api-Key` | SPEC-C `securitySchemes` |
| 4 | api-key or token as text | unsupported (note 23) | | | | generator rule |
| 5 | cloud, region | native | `deployment={"deployment_type": "managed", "cloud": , "region": }` | `deployment: {deploymentType: 'managed', cloud, region}` | `"deployment"` in `POST C/indexes` | SPEC-C `ManagedDeployment` |
| 6 | connection host / port | unsupported | | | | SPEC-C `servers` (one URL) |
| 7 | path, in-memory, deployment embedded | unsupported (note 21) | | | | https://docs.pinecone.io/release-notes/2026 |
| 8 | timeout | native | `Pinecone(timeout=)` | UNKNOWN (no per-client timeout option found) | `HttpRequest.timeout` | PY `pinecone/_client.py` |

## Collections

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 9 | collection name | concealed (note 1) | `"products"` | `'products'` | `"products"` | SPEC-C `CreateIndexRequest.name` |
| 10 | create collection | native | `pc.indexes.create(name=, schema={"fields": {...}}, deployment=, timeout=)` (blocks until ready) | `await pc.indexes.create({name, schema: {fields}, deployment, waitUntilReady: true, timeout})` | `POST C/indexes`, then `GET C/indexes/{name}` until `status.ready` | SPEC-C `/indexes`; PY `pinecone/client/indexes.py` `create`; TS `src/control/indexes/createIndex.ts` |
| 11 | create ... if-not-exists | concealed | `pc.indexes.exists(name)` then create | `pc.indexes.describe(name)`, `Errors.PineconeNotFoundError` means absent | `GET C/indexes/{name}`, 404 means absent | PY `indexes.py` `exists`; TS `src/errors/http.ts` |
| 12 | drop collection | native (note 22) | `pc.indexes.delete(name, timeout=)` (blocks until gone) | `await pc.indexes.delete(name)`, then describe until 404 | `DELETE C/indexes/{name}` (202), then `GET` until 404 | SPEC-C `/indexes/{index_name}` delete; PY `indexes.py` `delete` |
| 13 | drop ... if-exists | concealed | `exists` then `delete` | `describe` then `delete` | `GET` then `DELETE` | as rows 11 and 12 |
| 14 | primary id (int, bigint, varchar, text, uuid) | concealed (note 3) | `"_id": str(key)` and the key as metadata | same | same | SPEC-D `DocumentRecord._id` |
| 15 | primary key of other types; auto-id | unsupported (note 3) | | | | SPEC-D `DocumentRecord._id` (string) |
| 16 | field type int, bigint, int8, float, double | concealed (note 4) | metadata number | same | same | SPEC-D `DocumentFieldValue`; SPEC-C `CreateIndexSchema` |
| 17 | field type bool | concealed (note 4) | metadata boolean | same | same | SPEC-D `DocumentFieldValue` |
| 18 | field type varchar, text, uuid (not searched) | concealed (note 4) | metadata string | same | same | SPEC-D `DocumentFieldValue` |
| 19 | array element-type varchar, text, uuid | concealed (note 4) | metadata string list | same | same | SPEC-D `DocumentFieldValue`; https://docs.pinecone.io/guides/search/filter-by-metadata |
| 20 | array of numbers, booleans, structs | unsupported (note 6) | | | | SPEC-D `DocumentFieldValue` |
| 21 | json, map, struct, sub-fields; date, datetime, timestamptz; bytes; geo_point | unsupported (note 6) | | | | SPEC-D `DocumentFieldValue` |
| 22 | varchar max-length, array field-limit array_len, vector dim | concealed (note 5) | runtime check before each write | same | same | SPEC-D `DocumentRecord` (no length limits) |
| 23 | nullable | concealed (note 4) | nulls dropped on write | same | same | PY `documents.py` `update` (null values rejected) |
| 24 | default value | concealed | runtime fills the default before each write | same | same | |
| 25 | full-text field (full-text, analyzer, bm25 input, full_text index) | native (note 7) | `{"type": "string", "full_text_search": {...}}` | `{type: 'string', fullTextSearch: {...}}` | `{"type": "string", "full_text_search": {...}}` | SPEC-C `StringField` |
| 26 | analyzer tokenizer standard or language, language "xx" | native (note 7) | `"language": "en"` | `language: 'en'` | `"language": "en"` | SPEC-C `StringField.full_text_search.language` |
| 27 | analyzer filter stemmer; stop (with stemmer) | native (note 7) | `"stemming": True`, `"stop_words": True` | `stemming: true`, `stopWords: true` | `"stemming": true`, `"stop_words": true` | SPEC-C `StringField.full_text_search` |
| 28 | analyzer filter lowercase | UNKNOWN (note 7) | recorded as a note | | | SPEC-C `StringField` (no case option) |
| 29 | stop without stemmer; whitespace, keyword, ngram, edge_ngram, icu tokenizers; ascii_folding, length, synonym, decompounder, phonetic filters; resources; bm25-k1, bm25-b | unsupported (note 7) | | | | SPEC-C `StringField.full_text_search` |
| 30 | vector-field float_vector dim n metric m | native | `{"type": "dense_vector", "dimension": n, "metric": m}` | `{type: 'dense_vector', dimension: n, metric: m}` | same JSON | SPEC-C `DenseVectorField` |
| 31 | metric cosine, dot, euclidean | native, score concealed (note 15) | `"cosine"`, `"dotproduct"`, `"euclidean"` | same | same | SPEC-C `DenseVectorField.metric` |
| 32 | metric manhattan, hamming, jaccard | unsupported | | | | SPEC-C `DenseVectorField.metric` |
| 33 | several dense vector fields | unsupported (note 10) | | | | SPEC-C `CreateIndexSchema` ("one dense vector slot") |
| 34 | sparse_float_vector field without bm25 function | native (note 10) | `{"type": "sparse_vector"}`; values `{"indices": [...], "values": [...]}` | same | same | SPEC-C `SparseVectorField`; SPEC-D `SparseValues` |
| 35 | sparse_float_vector field + function bm25 | concealed (note 7) | BM25 full-text search on the function's inputs; no schema field | same | same | SPEC-D `TextScoring` |
| 36 | float16, bfloat16, float64, int8, uint8, binary vectors; multi-vector | unsupported | | | | SPEC-C `DenseVectorField` |
| 36a | vector field model; text-embedding functions; query text for a vector field | native, generation pending (note 28) | | | | SPEC-C `CreateEmbedConfig`; SPEC-D `EmbedScoring` |
| 37 | vector-index family, max-degree, build-candidates, search-candidates, quantization, on-disk | concealed (note 9) | recorded as notes | same | same | SPEC-C `CreateIndexRequest` (no index settings) |
| 38 | vector-index on an existing collection; full_text index on an existing collection | unsupported (note 8) | | | | PY `indexes.py` `create` ("The schema can't change once the index exists") |
| 39 | payload-index keyword, integer, float, bool, uuid | concealed (note 8) | no call | no call | no call | SPEC-C `CreateIndexSchema` ("indexed automatically") |
| 40 | payload-index on a full-text field (other than full_text); geo, json, datetime kinds; nested paths | unsupported (note 8) | | | | https://docs.pinecone.io/guides/search/filter-by-metadata |
| 41 | deletion-protection | native | `deletion_protection="enabled"` | `deletionProtection: 'enabled'` | `"deletion_protection": "enabled"` | SPEC-C `DeletionProtection` |
| 42 | consistency eventual | native (note 19) | no call | no call | no call | PY `documents.py` `upsert` ("applies the upsert asynchronously") |
| 43 | consistency strong, bounded, session (collection or query) | unsupported (note 19) | | | | as row 42 |
| 44 | namespace "x" on a statement | native (note 2) | `namespace="x"` | `pc.index({name, namespace: "x"})` | path segment `/namespaces/x/` | SPEC-D `/namespaces/{namespace}/documents/*` |
| 45 | partitions, aliases, ttl, cross-ref, external sources | unsupported (notes 24, 31) | | | | SPEC-C `CreateIndexRequest` |
| 45a | tenants; vecDSL namespaces | native (note 24) | REST `POST D/namespaces`, the namespace in every documents path | same | same | SPEC-D `createNamespace`, `/namespaces/{namespace}/documents/*` |

## Writes

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 46 | insert into ... values / from-param | concealed (note 11) | `docs.fetch(namespace=, ids=, include_fields=[key])`, then `docs.upsert(namespace=, documents=[...])` for new ids | `await docs.fetch({ids, includeFields})`, `await docs.upsert({documents})` | `D/docs/fetch`, `D/docs/upsert` | SPEC-D `/documents/fetch`, `/documents/upsert`; PY `pinecone/client/documents.py` |
| 47 | upsert; insert on-conflict update or replace; batch n | native | `docs.upsert(namespace=, documents=)` in batches | `await docs.upsert({documents})` | `D/docs/upsert` | SPEC-D `UpsertDocumentsRequest` |
| 48 | update set ... ids / where; remove-fields | concealed (note 11) | ids resolved with `docs.fetch`, then `docs.update(namespace=, documents=[{"_id", ..., "_remove_fields"}])` | `await docs.update({documents})` | `D/docs/update` | SPEC-D `UpdateDocumentsRequest`, `UpdateDocumentRecord` |
| 49 | update mode replace | concealed (note 11) | `docs.upsert` of the merged record | same | same | SPEC-D `UpsertDocumentsRequest` |
| 50 | update array-append, array-remove | concealed (note 26) | fetch the arrays, change, `docs.update` with `_remove_fields` for emptied arrays | same | same | SPEC-D `UpdateDocumentRecord` |
| 50a | update if-version | unsupported | | | | SPEC-D `UpdateDocumentRecord` (no version) |
| 51 | delete ids / where | concealed (note 11) | ids resolved with `docs.fetch`, then `docs.delete(namespace=, ids=)` | `await docs.delete({ids})` | `D/docs/delete` | SPEC-D `DeleteDocumentsRequest` |
| 52 | delete all | native | `docs.delete(namespace=, delete_all=True)` | `await docs.delete({deleteAll: true})` | `D/docs/delete` with `"delete_all": true` | SPEC-D `DeleteDocumentsRequest` |
| 53 | delete with limit; records from files | concealed | the first n targets in key order; `rt.read_records` reads JSON or JSON Lines | same | same | SPEC-D `DeleteDocumentsRequest` |
| 53a | ttl | unsupported | | | | SPEC-D `DocumentRecord` (no expiry) |
| 54 | wait | concealed (note 12) | poll `docs.search(top_k=1)` until `response_info.lsn_reconciled >= lsn_committed` of the write | headers `x-pinecone-request-lsn`, `x-pinecone-max-indexed-lsn` read through `fetchApi` | same headers | PY `pinecone/models/response_info.py`; https://docs.pinecone.io/guides/index-data/check-data-freshness |
| 55 | write consistency one, quorum, all | concealed (note 12) | recorded as a note | same | same | SPEC-D (no write consistency option) |

## Filters

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 56 | eq, neq on numbers, strings, booleans | native | `{"f": {"$eq": v}}`, `{"f": {"$ne": v}}` | same JSON | same JSON | https://docs.pinecone.io/guides/search/filter-by-metadata |
| 57 | gt, gte, lt, lte, between on numbers | native | `$gt`, `$gte`, `$lt`, `$lte`; between as `$and` of `$gte`, `$lte` | same | same | same page |
| 58 | in, not_in | native | `$in`, `$nin` | same | same | same page |
| 59 | and, or, not | native (note 13) | `$and`, `$or`; not pushed to field `$not` | same | same | same page |
| 60 | is_null, not_exists / is_not_null, exists; eq null / neq null | concealed (note 13) | `{"f": {"$exists": false}}` / `true` | same | same | same page |
| 61 | contains, contains_any, contains_all on string arrays | native | `$eq`, `$in`, `$and` of `$eq` | same | same | same page ("matches when any value in the array satisfies it") |
| 62 | match, match_any_term, contains_text / match_all_terms / phrase on full-text fields | native | `$match_any` / `$match_all` / `$match_phrase` | same | same | same page; PY `documents.py` `search` |
| 63 | comparisons on full-text fields; text operators on metadata | unsupported (note 13) | | | | PY `documents.py` `search` (400 for the wrong operator kind) |
| 64 | like, prefix, contains on text; fuzzy, wildcard, regex; length; is_empty; geo; element predicates; nested paths | unsupported | | | | https://docs.pinecone.io/guides/search/filter-by-metadata |
| 65 | path "key" (dynamic field) | native | `{"key": {...}}` | same | same | same page |

## Queries

| # | Construct | Status | Python | TypeScript | Java | Source |
|---|---|---|---|---|---|---|
| 66 | vector-search query vector top k using f where F | native | `docs.search(namespace=, top_k=k, score_by=[{"type": "dense_vector", "field": f, "values": v}], include_fields=, filter=F)` | `await docs.search({topK, scoreBy, includeFields, filter})` | `D/docs/search` | SPEC-D `SearchDocumentsRequest`, `DenseVectorScoring` |
| 67 | query id x (lookup-in) | concealed | `docs.fetch(ids=[x], include_fields=[f])`, then row 66 | same | same | SPEC-D `/documents/fetch` |
| 68 | sparse_vector input on the sparse field | native (note 42) | `score_by=[{"type": "sparse_vector", "field": f, "sparse_values": {"indices": [...], "values": [...]}}]` | same clause (the SDK takes `sparseValues`) | `"sparse_values"` | SPEC-D `SparseVectorScoring` |
| 68a | query text for a vector field with a model; the model clause | native (notes 47, 48) | `{"type": "embed" or "sparse_embed", "field": text field, "query": q}`; a field without a model: Inference `POST /embed`, then row 66 or 68 | same | same | SPEC-D `EmbedScoring`, `SparseEmbedScoring`; SPEC-I `embed` |
| 69 | output [...], with-vectors, exclude | native | `include_fields=[key, ...]` | `includeFields` | `"include_fields"` | SPEC-D `SearchDocumentsRequest.include_fields` |
| 70 | score similarity (default), score native | concealed (note 15) | runtime converts `_score` | same | same | PY `pinecone/models/enums.py` `Metric` |
| 71 | bounds min, max | concealed (note 14) | runtime drops hits outside the bounds | same | same | SPEC-D `SearchDocumentsRequest` (no threshold) |
| 72 | offset | concealed (note 14) | `top_k = limit + offset`, runtime slices | same | same | SPEC-D `SearchDocumentsRequest` (no offset) |
| 73 | search ... query text fields [f, ...] | native | `score_by=[{"type": "text", "fields": [...], "query": q}]` | same clause | same clause | SPEC-D `TextScoring` |
| 74 | search without fields | concealed | every full-text field of the collection | same | same | SPEC-D `TextScoring` |
| 75 | text-query query_string | native | `{"type": "query_string", "query": q}`; with fields: `f:(q)` per field, joined by OR | same | same | SPEC-D `QueryStringScoring` |
| 76 | operator and | concealed | text clause plus filter `{"f": {"$match_all": q}}` (`$or` over several fields) | same | same | https://docs.pinecone.io/guides/search/filter-by-metadata |
| 77 | min-should-match; text-query prefix (one term), term, wildcard | unsupported (note 41) | | | | SPEC-D `TextScoring`, `QueryStringScoring`; https://docs.pinecone.io/guides/search/full-text-search/query-syntax |
| 77a | fuzziness, slop, field boosts, text-query phrase, phrase_prefix, fuzzy, regexp (keyword searches and keyword legs) | native (note 41) | `{"type": "query_string", "query": rt.query_string_text(q, fields, rewrite)}` | `queryStringText` | `queryStringText` | https://docs.pinecone.io/guides/search/full-text-search/query-syntax |
| 78 | hybrid-search fusion rrf fusion-k k, legs | concealed (note 16) | one `docs.search` per leg, fused in the runtime | same | same | SPEC-D `SearchDocumentsRequest.score_by` ("a dense_vector ... clause must be the only clause") |
| 79 | hybrid fusion weighted, relative_score; leg weights; alpha | concealed (note 16) | runtime fusion | same | same | as row 78 |
| 80 | hybrid per-leg limit, per-leg filter, vector-leg bounds | concealed (note 16) | per-leg `top_k`, `filter`, runtime bounds | same | same | as row 78 |
| 81 | leg weights or alpha with rrf; fusion-k with other fusions | unsupported (note 16) | | | | vecDSL D3 (rrf is unweighted) |
| 82 | recommend positive [...] negative [...] | concealed (note 18) | `docs.fetch` of the examples, centroid 2p - n, then row 66 with filter `{key: {"$nin": [...]}}` | same | same | SPEC-D `/documents/fetch` |
| 83 | get from ... ids [...] | native | `docs.fetch(namespace=, ids=, include_fields=)` | `await docs.fetch({ids, includeFields})` | `D/docs/fetch` | SPEC-D `FetchDocumentsRequest` |
| 84 | count | concealed (note 17) | `docs.list(namespace=, limit=100)` pages | `await docs.list({limit, paginationToken})` | `D/docs/list` | SPEC-D `/documents/list` |
| 85 | count where F | concealed (note 17) | `docs.fetch(filter=F, include_fields=[key], limit=1000, pagination_token=)` pages | same | `D/docs/fetch` | SPEC-D `FetchDocumentsRequest.filter`, `pagination_token` |
| 86 | scroll limit n offset m where F | concealed (note 17) | ids by list or filtered fetch, sorted by key, then `docs.fetch(ids=)` | same | same | SPEC-D `/documents/list`, `/documents/fetch` |
| 87 | scroll sort | concealed | metadata values fetched and sorted in the runtime, missing values last | same | same | SPEC-D `/documents/fetch` |
| 87a | scroll cursor, slice | unsupported | | | | SPEC-D `ListDocumentsRequest` |
| 88 | batch-query | concealed | one helper method per query | same | same | |
| 89 | target-recall (D15) | unsupported | | | | https://docs.pinecone.io/guides/search/semantic-search; capability manifest `capabilities/pinecone.properties` |
| 90 | params candidates | unsupported | | | | SPEC-D `SearchDocumentsRequest` (no search effort) |
| 91 | params probes, scan-budget, timeout, oversampling, rescore, filter-strategy, exhaustive | concealed | recorded as notes | same | same | SPEC-D `SearchDocumentsRequest` |
| 92 | exact, explain, mmr, boost, group-by, with-total, highlight, sort, aggregate, prefetch, within, as-of, cursor, vendor extension parameters | unsupported | | | | SPEC-D `SearchDocumentsRequest` |
| 93 | rerank (keyword, vector with text input, hybrid) | concealed (note 44) | the search, then `POST /rerank` with the hits' rank field; recommend: unsupported (no query text) | same | same | SPEC-I `rerank` |

## Notes

1. The index name is the lower-case words of the collection name joined by `-` (`productCatalog` becomes
   `product-catalog`); names longer than 45 characters are unsupported (SPEC-C `CreateIndexRequest.name`).
2. The documents API addresses a namespace in every request path (PY `documents_helpers.py`
   `_encode_document_namespace`). Statements without a namespace use `__default__`, the namespace requests address
   when they omit one (SPEC-D, `describe_namespace` parameter). A statement's `namespace "x"` is passed through;
   namespaces are created by the first write (PY `grpc/__init__.py`, "upserting documents into `__default__`
   creates it").
3. Document ids are ASCII strings of 1 to 512 characters (SPEC-D `DocumentRecord._id`). The runtime maps an integer
   key to its decimal text (`1` and `1.0` both become `"1"`) and a text key to itself; other key types, non-ASCII
   text and non-integral numbers are rejected identically in the three runtimes (checked against the API mock). The key
   is also stored as metadata under its own field name, so it is typed and filterable (`id $nin [1, 3]` excludes the
   recommend examples); results report the key parsed back from `_id` by the declared key type.
4. Pinecone declares only searched fields in the schema; metadata "isn't declared in the schema; it's indexed
   automatically from the documents you upsert" (SPEC-C `CreateIndexSchema`). Metadata values are numbers, strings,
   booleans or string lists (SPEC-D `DocumentFieldValue`); numbers are normalized to float at upsert (SPEC-C
   `IntegerField`), so the runtimes return integral floats as integers. Pinecone stores no null values (PY
   `documents.py` `update`: a `None` value is rejected), so null and absent are the same.
5. Declared limits are not enforced by Pinecone; the runtime checks `max-length`, `array_len` and `dim` before each
   write, as the Weaviate runtime does.
6. Unsupported field types follow from the metadata value types of note 4: Pinecone has no object, date, binary or
   geo values and no numeric lists.
7. A field is a full-text field when it is declared `full-text`, has an analyzer, feeds a bm25 function or has a
   `full_text` payload index (SPEC-C `StringField`). `full_text_search` takes `language`, `stemming`, `stop_words`
   ("`stop_words` requires `stemming: true`") and `ngram`. The analyzer's language is passed as given (`english`
   becomes `en`); the set of languages Pinecone accepts is UNKNOWN. Whether Pinecone lowercases tokens is not
   documented (row 28); a lowercase filter is recorded as a note. BM25 parameters are not configurable. The sparse
   output of a bm25 function is not stored: keyword search scores its input fields with BM25 (SPEC-D `TextScoring`).
8. Metadata needs no payload index (note 4), so keyword, integer, float, bool and uuid indexes, inside create or on
   an existing collection, emit no call. A full-text field is a data field: it takes only text-match filters
   (PY `documents.py` `search`), so a filtering index on it is unsupported. The schema is fixed at creation, so a
   full-text or vector index on an existing collection is unsupported.
9. Pinecone exposes no vector index settings (SPEC-C `CreateIndexRequest`); they are listed as "Ignored on Pinecone"
   in the generated method's comment.
10. "An index has one dense vector slot and one sparse vector slot" (SPEC-C `CreateIndexSchema`). A second dense
    vector field is unsupported.
11. `upsert` overwrites a document with the same `_id` (PY `documents.py` `upsert`), so insert first fetches the ids
    and rejects existing records (on-conflict error, the default) or skips them (ignore). Update and delete resolve
    their targets first (fetch by ids, or paginated fetch by filter, which also supports text-match filters, unlike
    filtered update and delete; https://docs.pinecone.io/guides/search/filter-by-metadata), then patch or delete by
    id, so `update_count` and `delete_count` are exact in all languages. A per-id update keeps fields it does not
    name and removes the `_remove_fields` (SPEC-D `UpdateDocumentRecord`).
12. Pinecone applies writes asynchronously. Writes return the log position `x-pinecone-request-lsn` and searches
    report `x-pinecone-max-indexed-lsn`; a search has seen a write when the second is at least the first
    (https://docs.pinecone.io/guides/index-data/check-data-freshness; PY `pinecone/models/response_info.py`,
    which also says fetch reports no position and that fetch and search see a write at different times). With
    `wait`, the runtime polls a one-result search every 0.5 s until that holds, for at most
    `VECDSL_PINECONE_WAIT_SECONDS` (default 60). Without `wait` there is no extra call. When a response carries no
    position (the API mock), the wait ends at once. Checked in all three runtimes against a test server.
13. The filter is Pinecone's JSON and is passed unchanged to the SDKs. Pinecone negates per field
    (`{"genre": {"$not": {"$eq": "drama"}}}`), so `not` is pushed down with De Morgan. Null tests map to `$exists`
    (note 4). Comparison operators on a full-text field and text-match operators on metadata are rejected by the
    server with 400 (PY `documents.py` `search`), so the generator rejects them first.
14. Pinecone search has no score threshold and no offset (SPEC-D `SearchDocumentsRequest`); the runtime requests
    `limit + offset` hits, drops hits outside the bounds and slices. Bounds can therefore return fewer than `top` hits.
15. Canonical similarity (D3): cosine and dotproduct scores are similarities and are reported unchanged; a euclidean
    score is a distance ("`EUCLIDEAN` scores straight-line distance, so a smaller score is a closer match",
    PY `pinecone/models/enums.py`). The documentation does not say whether it is the distance or its square; a measurement
    on Pinecone (12 Nov 2026) gave the square (`betaDistances`: 0.799997 and 1.999995 for distances 0.894427 and 1.414214),
    so a euclidean score is reported as `-sqrt(score)`. BM25 scores are reported unchanged.
    `score native` reports `_score` unchanged.
16. A search request scores by several clauses only when all are `text` or `query_string`; a dense clause "must be
    the only clause" (SPEC-D `SearchDocumentsRequest.score_by`). Hybrid search therefore runs one search per leg and
    fuses in the runtime, exactly as D3 defines the fusions: rrf `sum 1 / (k + rank)` with rank from 1,
    weighted `sum w * similarity`, relative_score `sum w * (s - min) / (max - min)` (1 when all scores of a leg are
    equal). Leg weights default to 1; `alpha` sets the first vector leg's weight and `1 - alpha` the other's. A leg
    without `limit` retrieves `top + offset` hits. Ties are broken by key order. Unlike Weaviate, per-leg limits,
    filters and fusion-k other than 60 are therefore supported.
17. There is no count operation for documents. `count` pages through `list` (ids only, 100 per page) or, with a
    filter, through filtered `fetch` (1000 per page); both are exact. `scroll` collects the matching ids, sorts them
    by key (numerically for integer keys) and fetches the page, so its order is the key order in all languages.
18. Pinecone has no recommend operation. As in the Weaviate runtime, the query vector is the mean of the positive
    examples moved away from the mean of the negatives (`2p - n`); examples of the same collection are excluded with
    a `$nin` filter on the stored key.
19. Reads take no consistency level (SPEC-D). `consistency eventual` is the native behaviour; strong, bounded and
    session are unsupported. `wait` on writes (note 12) gives read-your-writes for the following searches.
20. `io.pinecone:pinecone-client` 6.2.0, the current Java SDK on Maven Central, targets API 2025-10
    (https://docs.pinecone.io/reference/pinecone-sdks lists Java for 2026-07 as "Coming soon"; `CHANGELOG.md` and
    `codegen/buf.yaml` at tag v6.2.0) and has no documents API. The Java runtime therefore sends the REST requests of
    SPEC-C and SPEC-D with `java.net.http` and Gson, and retries 429 and 5xx responses three times. When a Java SDK
    for 2026-07 is released, only `PineconeRuntime.java` changes.
21. Pinecone Local, the Docker emulator, "has reached end of life and is no longer available"
    (https://docs.pinecone.io/release-notes/2026; end of life on 7 October 2026 per
    https://docs.pinecone.io/guides/production/mock-the-api). Its images `ghcr.io/pinecone-io/pinecone-local` and
    `pinecone-index` served API 2025-01, which has no documents API. The generated `docker-compose.yml` runs the mock
    that Pinecone documents as the replacement: Prism `stoplight/prism:5.14.3` serving the API specifications, which validates
    requests and answers with canned examples. Results can be verified only against a Pinecone project.
22. Index creation and deletion are asynchronous (SPEC-C `/indexes/{index_name}` delete returns 202). The runtimes
    wait until a new index is ready and a deleted index is gone, for at most `VECDSL_PINECONE_INDEX_TIMEOUT` seconds
    (default 300); 0 disables both waits, which the API mock needs because it always describes the index as present.
23. The API key is read from the environment variable named in the script (`api-key env("X")`), by default
    `VECDSL_PINECONE_API_KEY`; a key written as text in the script is unsupported.
24. Tenants and vecDSL namespaces are Pinecone namespaces of the collection's index ("namespaces as tenants",
    https://docs.pinecone.io/guides/index-data/implement-multitenancy). A statement's `tenant t` addresses namespace `t`,
    `namespace "x"` addresses `x`; both on one statement are unsupported. A collection declaration creates its tenants and
    namespaces with the index (SPEC-D `createNamespace`); `create tenant` and `drop tenant` call `createNamespace` and
    `deleteNamespace` (the drop waits until the namespace is gone). A Starter index holds 100 namespaces
    (https://docs.pinecone.io/reference/api/database-limits/object-limits).
25. Operations the demo did not use (configure, namespaces, backups, backup schedules, restore jobs, imports, statistics,
    models, clone) go over REST in all three runtimes through one helper per runtime (`_send`, `send`, `send`), with the
    API key, `X-Pinecone-Api-Version: 2026-07` and three retries of 429 and 5xx. The three languages therefore send the same
    requests; the SDK methods for these operations were not used.
26. Pinecone metadata lists hold strings (SPEC-D `DocumentFieldValue`); array-append and array-remove read the stored
    array, append or remove the items of the set record, and write the result with a per-id update. An array emptied
    by array-remove is removed (`_remove_fields`), as Pinecone stores no empty value for a missing one.
27. Index tags are key-value pairs (SPEC-C `IndexTags`: at most 20, keys of letters, digits, `_` and `-`, at most 80
    characters; a value of `""` removes a tag). A vecDSL tag becomes a key with the value `"true"`; `set tags` replaces
    them: tags not in the new list are set to `""`. `describe` returns the tag keys.
28. Integrated embedding needs only the API key: hosted models embed a `string` field declared with `embed`
    (SPEC-C `CreateEmbedConfig`), queries score it with `embed` (SPEC-D `EmbedScoring`), and the Starter plan includes
    embedding tokens per model (https://www.pinecone.io/pricing/). Generated (note 47); `create
    embedding-model` and `create reranker` with provider pinecone (or none) check that the hosted model exists (SPEC-I
    `get_model`); the rerank clause is note 44.
29. Backups (SPEC-C `create_backup`), restore (`create_index_from_backup_operation`, `describe_restore_job`), backup
    schedules (`create_backup_schedule`) and bulk import from object storage (SPEC-D `startBulkImport`, JSON Lines for
    document indexes, https://docs.pinecone.io/guides/index-data/import-data) are available on Standard and Enterprise
    plans only (https://www.pinecone.io/pricing/; backups "N/A" for Starter on the object-limits page). They are generated
    and checked against the API mock, and left out of the runnable sample. A restore replaces the index (deleted, then
    created from the newest ready backup of that name).
30. A clone copies: the runtime creates an index with the source's described schema (server-set keys such as
    `lowercase` and `max_term_len` removed, which create rejects; PY `schema_builder.py`), deployment and tags, copies
    every namespace through list, fetch and upsert, and waits until searches of the copy reach the last write's log
    position. Starter projects have no backups, so a clone does not use backup and restore.
31. Pinecone namespace aliases (SPEC-D `createNamespaceAlias`) resolve to a namespace of the same index; a vecDSL alias
    names a collection, so aliases and swap-alias stay unsupported.
32. Collections (SPEC-C `/collections`) "are supported only for pod-based indexes", and "pod-based indexes cannot be
    created with API version 2026-07"; replication and sharding are pod settings, so they are unsupported.
33. Starter plan (https://www.pinecone.io/pricing/, object-limits page): AWS us-east-1 only, one project, five indexes,
    100 namespaces per index, 2 GB storage, 2M write units and 1M read units a month; no backups, restore, import, dedicated
    read nodes, BYOC, private endpoints, CMEK or audit logs.
34. The Java runtime parses JSON numbers as Long or Double (Gson `ToNumberPolicy.LONG_OR_DOUBLE`), so a described
    `dimension` is sent back as `1536`, not `1536.0`. A statement label equal to a runtime function (for example
    `dropBackup`) hides the static import in Java; the printer then calls `PineconeRuntime.dropBackup(...)`.
35. The `query_string` scoring takes the Lucene operators `AND`, `OR`, `NOT`, `*`, `~` and `^`, and fuzzy matching works
    only there (https://docs.pinecone.io/guides/search/full-text-search). Fuzziness, slop, field boosts and the phrase,
    phrase prefix, fuzzy and regexp query types are generated in that syntax (note 41); minimum-should-match, term and
    wildcard are not documented and stay unsupported. Searching the sparse field by a sparse query vector is note 42.
36. `describe ... stats` reads `describe_index_stats` (SPEC-D), whose counts are eventually consistent; serverless
    indexes reject a filter there, so filtered counts page through fetch (note 17).
37. Server-set full-text settings: the SDK documents that describe responses show `lowercase` and `max_term_len`, which
    the server sets (PY `schema_builder.py`); a vecDSL lowercase filter is therefore listed as a note. The expected
    matches of the full sample assume that the server lowercases tokens ("Vector" matches "vector").

38. Filtered fetches lag behind searches and report no log position (measured on a Starter project, AWS
    us-east-1, 12 Nov 2026): after an upsert, a search with a filter saw the document after 0.3 s, while `fetch` with a filter saw it
    after 6.4 to 6.8 s; deletes were visible to both within 0.4 s. Upsert responses carry `x-pinecone-request-lsn` and
    search responses `x-pinecone-max-indexed-lsn`, but fetch responses (by id and by filter) carry no log position, so
    note 12's wait does not cover them. Fetch by id and list were current at once. The filters themselves (`$and`,
    `$eq` and `$in` on a string list) behave as documented (SPEC-D `fetchDocuments`). The runtimes therefore wait for
    filtered fetches after the log position.
39. Dense-vector scores on Pinecone are close to, not equal to, exact cosine values once an index has settled (measured
    12 Nov 2026): in a first demo run, whose searches followed the writes within a second, the scores matched
    the exact values to six decimals (1, 0.977841, 0.754831); in the second, with each waited write about 6.5 s longer
    (note 38), the same searches returned 0.999436, 0.977632 and 0.753786, identical in the three languages, with the
    same order. The largest deviation was 0.0015. BM25 and fusion results matched exactly. The documentation states no
    score precision; the difference is recorded as observed.
40. A namespace exists only after a write to it or its creation, the default namespace `__default__` included: on
    Pinecone (12 Nov 2026), `delete` with `delete_all` on the never-written default namespace of `notes` answered
    `404 NOT_FOUND: Namespace not found`, while listing it returned no documents. The runtimes therefore send the
    delete-all only when the listing found documents; truncating an empty or missing namespace deletes 0.

## Query rewrites, sparse search, embedding and rerank

Checked against SPEC-C, SPEC-D and SPEC-I at commit 7855e85 of `pinecone-io/pinecone-api` (cloned on 12 Nov 2026) and the
documentation pages named below (read on 12 Nov 2026). All of these are
generated; notes 47 to 50 add the details of the generated mapping.

41. **query_string syntax** (https://docs.pinecone.io/guides/search/full-text-search/query-syntax). A `query_string`
    clause takes no `field` or `fields`; a field qualifier `field:(clause)` targets a field, and the query is at most
    10,000 bytes (SPEC-D `QueryStringScoring`). Unquoted terms combine with OR; `AND` or `+` require a term. Fuzzy
    matching is `term~` (automatic distance by term length: under 4 characters exact, 4 to 7 one edit, 8 or more two)
    or `term~N` with N 0, 1 or 2 (above 2 is a 400), on bare terms only, not on quoted phrases. Phrase proximity is
    `"phrase"~N`. `*` works only after a quoted phrase of at least two terms (`"james w"*`, the final term expands to the
    first 50 matching terms); a single-term prefix such as `auto*` "returns no matches rather than an error", so the
    vecDSL prefix type is unsupported. Boosts `^N` attach to a term, a quoted phrase or a parenthesized group, with N a
    non-negative literal number (no scientific notation, no leading decimal point, no spaces); boosting a field clause is
    not described, so a field boost wraps the field clause in parentheses. Regular expressions `field:/pattern/` exist
    with `query_string` only, so regexp is generated.
    The page describes no exact-token query (term) and no wildcards besides the phrase prefix, and no escape character;
    matching is case-insensitive and runs on analyzed tokens. A generated query therefore cannot rely on backslash
    escaping: the query rewrite reduces the text to analyzer-like terms instead.
42. **Sparse vector search** (SPEC-D `SparseVectorScoring`): `{"type": "sparse_vector", "field": f, "sparse_values":
    {"indices": [...], "values": [...]}}` on a `sparse_vector` field; like a dense clause it must be the only clause of
    `score_by` (note 16).
43. **Integrated embedding** (SPEC-C `CreateEmbedConfig`, `CreateSparseEmbedConfig`; SPEC-D `EmbedScoring`,
    `SparseEmbedScoring`; https://docs.pinecone.io/guides/search/integrated-embedding.md). A `string` field declares
    `embed` (model, default `llama-text-embed-v2`; dimension, default the model's; metric cosine, dotproduct or euclidean,
    default the model's; read and write parameters) or `sparse_embed` (default `pinecone-sparse-english-v0`), fixed at
    creation. Writes then accept at most 96 documents per upsert or by-id update instead of 1,000; the text is stored and
    returned unless `store_text` is false; an `embed` or `sparse_embed` clause takes the query text (at most 10,000 bytes,
    longer is a 400) and must be the only clause. The models `gemini-embedding-2`, `text-embedding-3-small` and
    `text-embedding-3-large` take a dimension from a range; `gemini-embedding-2` is not on the Starter plan. A field may
    declare `embed`, `sparse_embed` and `full_text_search` together, unless `store_text` is false.
44. **Reranking** (SPEC-I `RerankRequest`, `RerankResult`; https://docs.pinecone.io/guides/search/rerank-results). The
    Documents API search has no rerank parameter (only the legacy Records search has `rerank`, SPEC-D), so a rerank
    clause is a second request to Inference `rerank`: model, query, documents, `rank_fields` (default `["text"]`),
    `top_n` (default all), `return_documents`, `parameters` (for example `truncate`); the answer lists the documents
    with their relevance and `usage.rerank_units`. Hosted models: `cohere-rerank-4-fast` (several rank fields),
    `bge-reranker-v2-m3` (one rank field; query and document together at most 1024 tokens, truncated only with
    `truncate: END`, otherwise an error), `pinecone-rerank-v0` (deprecated).
45. **Starter plan and cost** (https://www.pinecone.io/pricing/, read on 12 Nov 2026). Starter includes 5M tokens a
    month for each of `llama-text-embed-v2`, `multilingual-e5-large` and `pinecone-sparse-english-v0`, and 500
    requests a month of `bge-reranker-v2-m3` (the other rerankers are not offered on Starter). Paid prices: $0.16 per
    million tokens (`llama-text-embed-v2`), $0.08 (`pinecone-sparse-english-v0`, `multilingual-e5-large`), $2 per 1,000
    rerank requests (`bge-reranker-v2-m3`). A full-sample run with integrated embedding (about ten short documents,
    a few queries) and a few rerank calls stays far inside the Starter allowance, so none of the planned statements
    needs a paid plan, provided the sample uses `llama-text-embed-v2`, `pinecone-sparse-english-v0` and
    `bge-reranker-v2-m3`.
47. **Integrated embedding as generated** (SPEC-C `StringField`, `CreateEmbedConfig`, `CreateSparseEmbedConfig`; SPEC-D
    `upsertDocuments`, `updateDocuments`; https://docs.pinecone.io/guides/search/integrated-embedding.md, read on 12 Nov
    2026). A vector field with a hosted model, or the output of a `text_embedding` (dense) or `sparse_encoding` (sparse)
    function with one, becomes `embed` or `sparse_embed` on the string field whose text Pinecone embeds: the field's one
    source field (`from [f]`, or the function's input), or the vector field itself. The vecDSL dim and metric become the
    embed `dimension` and `metric` (the metric is always sent, as for dense fields). A stored embed field "is BM25-searchable
    unless you opt out" (integrated-embedding page; SPEC-C `StringField.store_text`), so the text field also counts as a
    full-text field: keyword search on it is allowed and comparison filters on it are rejected, as for other full-text
    fields (note 13). Writes to such an index carry at most 96 documents per upsert or update ("An index that embeds a field
    accepts at most 96 documents per request", SPEC-D), and an `embed` field is required on every upserted document;
    the write layout carries `max_batch` 96, and clone sizes its copy batches the same way. Searches send the query text with
    `embed` or `sparse_embed` scoring; the runtime never embeds text. Pinecone returns the text of an embedded field, not its
    vector, so query by id, recommend, distance matrix and with-vectors on such a field are unsupported or left out (noted),
    and a record value for the vector field of a field embedded from another field is rejected. Waits (note 12)
    probe with a `text` clause on the embedded field, which costs no embedding tokens; the runtime's retries repeat a
    document write, which "is metered, including retries" (SPEC-D `updateDocuments`).
48. **Inference embed for the model clause** (SPEC-I `embed`, `EmbedRequest`, `DenseEmbedding`, `SparseEmbedding`). A
    vector search with a query text and a `model` clause on a field Pinecone does not embed sends `POST /embed` with the
    model's hosted name, `parameters` `{"input_type": "query", "truncate": "END"}` (plus `dimension` when the vecDSL model
    declares `dim`, which must equal the field's), and searches with the returned dense values or sparse indices and values.
    The parameter names are the request example of SPEC-I `EmbedRequest.parameters`; which models accept `input_type` and
    `dimension` is model-specific (the integrated-embedding page names `truncate` and, for `gemini-embedding-2`, `task_type`
    only), so this path is checked against the specification only: no runnable statement needs it,
    because a matching dense field would need 1,024-dimensional vectors written as literals. The same clause on a field
    Pinecone embeds is the field's `embed` scoring when it names the field's model, and unsupported otherwise (the model is
    fixed at creation, SPEC-C `CreateEmbedConfig`).
49. **Scores of the new clauses.** `sparse_vector` and `sparse_embed` scores are reported unchanged as similarities. SPEC-D
    `SparseVectorScoring` says "sparse vector similarity" without a formula; the Pinecone Local page lists `dotproduct` as
    the only metric "when storing only sparse vectors" (https://docs.pinecone.io/guides/operations/local-development.md,
    `METRIC`, legacy vector indexes), so the generator
    rejects a sparse field declared with another metric and the expected values are dot products over shared indices; a server
    check decides. Whether a sparse search returns documents that share no index with the query is not documented
    (SPEC-D says so only for `sparse_embed`); the sample asks only for hits with a shared index. Rerank relevance is
    "normalized between 0 and 1" (SPEC-I `RankedDocument.score`) and replaces the hit's score.
50. **The TypeScript SDK and sparse clauses.** `@pinecone-database/pinecone` 9.2.0 serializes a scoring clause by reading
    `sparseValues` and writing `sparse_values` (TS `dist/data/documents/searchDocuments.js`, `DocumentScoringMethodToJSON`,
    at the pinned version), so the TypeScript runtime passes `sparseValues`; a clause with `sparse_values` reached the server
    without the vector (observed with the pinned SDK).

## Known limitations

- No self-hosted server exists for API 2026-07; the environment is the Prism API mock, which checks request shapes,
  and results need a Pinecone project.
- Reads are eventual; `wait` on writes gives read-your-writes for the following statements.
- Paid-plan constructs (backups, restore, backup schedules, object-storage import) cannot run on a free Starter
  project; COVERAGE.md marks them "paid plan".
- A nested `create vector-index ... on <field>` resolves the field by simple name across collections.
- Operator `and` with a query_string text query keeps the `$match_all` filter of the raw query text, which counts
  Lucene operators such as `OR` as terms.
- One dense vector field per collection.
