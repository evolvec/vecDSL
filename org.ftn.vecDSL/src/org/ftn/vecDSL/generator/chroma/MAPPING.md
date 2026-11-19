# vecDSL to Chroma: mapping notes

Target: Chroma 1.5.9, the open-source server (`chromadb/chroma:1.5.9`, the Rust frontend started by `chroma run` with
`/config.yaml`, `persist_path: "/data"`). The construct matrix with one row per grammar class and statement feature is
`COVERAGE.md`; this file records the facts behind it. References point to the sources at tag 1.5.9
(https://github.com/chroma-core/chroma/tree/1.5.9) and to the OpenAPI document the server serves at `/openapi.json`. Facts
marked "observed" were checked against `chroma run` 1.5.9 on 2026-11-11 (MAPPING.md, "Server findings").

## Versions (checked 2026-11-11)

| component | version | evidence |
|---|---|---|
| server | 1.5.9 (5 May 2026), latest stable | `git ls-remote --tags chroma-core/chroma` (tag 1.5.9, commit 11f3c74); GitHub releases page marks 1.5.9 "latest"; the newer `latest` tag is a pre-release (1.5.10.dev) |
| image | `chromadb/chroma:1.5.9` (also `ghcr.io/chroma-core/chroma:1.5.9`) | `.github/workflows/_build_release_container.yml` (`DOCKERHUB_IMAGE_NAME: "chromadb/chroma"`, target `cli` of `rust/Dockerfile`) |
| Python SDK | `chromadb-client` 1.5.9 (HTTP only) | PyPI JSON: `chromadb` and `chromadb-client` latest 1.5.9 |
| TypeScript SDK | `chromadb` 3.5.0 (30 June 2026, npm `latest`) | npm registry; the source tree at tag 1.5.9 says 3.4.5, which was never published (3.4.3, then 3.5.0) |
| Java | no official client; `java.net.http` (HTTP/1.1) and Gson 2.13.2 (`ToNumberPolicy.LONG_OR_DOUBLE`) | docs list Python and JavaScript clients only; see note 3 |

## Notes

1. **Server image and health check.** The release image is built from `rust/Dockerfile`, stage `cli`: `debian:stable-slim`
   with `dumb-init`, `libssl-dev` and `ca-certificates` only, entrypoint `chroma run /config.yaml`, port 8000. It has no curl
   or wget; `bash` is part of every Debian image, so the health check opens `/dev/tcp/127.0.0.1/8000` and requests
   `/api/v2/heartbeat` (route in `rust/frontend/src/server.rs`). Data lives in `/data` (`docker_single_node.yaml`);
   settings can be overridden by environment variables with the prefix `CHROMA_` (`rust/frontend/src/config.rs`, figment
   `Env::prefixed("CHROMA_")`).

2. **No authentication.** `rust/cli/src/commands/run.rs` starts the frontend with `Arc::new(())` as its
   `AuthenticateAndAuthorize`, and the `()` implementation in `rust/frontend/src/auth/mod.rs` admits every request with the
   default identity. The open-source 1.x server has no token authentication, so the generated programs send no key, the
   compose file binds the port to 127.0.0.1, and credentials in a connection (api-key, token, password) and every
   authentication type other than none are unsupported. `VECDSL_CHROMA_API_KEY` is not read
   (Chroma Cloud is not a target).

3. **SDKs.** Python uses `chromadb-client`, the HTTP-only build: it carries no server, no ONNX runtime and no default
   embedding model (`clients/python/build_python_thin_client.sh`). TypeScript uses `chromadb` 3.5.0, whose default
   embedding function lives in the separate package `@chroma-core/default-embed`, which the generated `package.json` does not
   list. Java calls the HTTP API (`/api/v2/...`) directly: the community Java client, amikos-tech
   `chromadb-java-client` 0.2.0 (README: "Java client for ChromaDB v2 API", Chroma >= 1.0), is not maintained by Chroma and
   brings OkHttp and its own embedding-function layer, and the Spring AI Chroma vector store is a framework abstraction
   rather than a client of the whole API. The HTTP runtime sends the
   same requests as the Python SDK and was checked against the server.

4. **No embedding function runs.** Python creates and opens collections with `embedding_function=None`; TypeScript with
   `embeddingFunction: null` on create; Java sends no embedding-function configuration. Every write passes embeddings. The
   SDKs embed a document whose record is updated without an embedding (`CollectionCommon._embed`, raised as "You must provide
   an embedding function" when none is set): the runtimes therefore send the stored embedding with every document update.

5. **Ids and keys.** A Chroma id is a string; the runtimes use the key's text (`recordId`: integers without a fraction,
   strings unchanged), identical in the three languages. The key is also stored in the metadata under its own name with its
   type, so it can be filtered, sorted and returned typed.

6. **One embedding per collection.** A Chroma record has one dense embedding (`AddCollectionRecordsPayload.embeddings`). A
   collection must declare exactly one `float_vector` field; several vector fields, sparse fields, multi-vectors and other
   element types are unsupported. Sparse vectors exist in Chroma's schema, but they are searched only through the Search API,
   which the local executor does not implement (note 10).

7. **Document.** The `document-body` field, or else the only full-text field, is stored as the Chroma document; it is not
   also kept in the metadata. A second full-text field is unsupported. Predicates on the document become `where_document`
   (note 9).

8. **Metadata values.** Chroma metadata holds strings, integers, floats, booleans and non-empty homogeneous arrays of them
   (Python validator in `chromadb/api/types.py`; observed: nested objects, mixed arrays and empty arrays are rejected; a null
   value is rejected on add). Null values and empty arrays are therefore left out on writes, so they read back as null.
   Dates, JSON, geo points, structs and nested fields are unsupported. In update requests a null value deletes the key
   (observed), which is how remove-fields, drop-field, rename-field and replace work. The TypeScript SDK drops null metadata
   values before sending (`serializeMetadata` in `dist/cjs/chromadb.cjs` of 3.5.0), so the TypeScript runtime posts updates
   over HTTP itself. Collection metadata rejects arrays: the server panics on them (`rust/sqlite/src/helpers.rs`,
   `unreachable!("Array metadata values are written to embedding_metadata_array")`, observed as a dropped connection), so
   collection tags are stored as JSON text and decoded by `describe`.

9. **Filters.** `where` takes `$eq`, `$ne`, `$gt`, `$gte`, `$lt`, `$lte`, `$in`, `$nin`, `$contains`, `$not_contains`, `$and`,
   `$or` (`chromadb/api/types.py`, `validate_where`; evaluation in `rust/segment/src/sqlite_metadata.rs`). Ordered operators
   take numbers only; `$and` and `$or` need at least two operands, so single conditions are not wrapped. `where_document`
   takes `$contains` and `$not_contains` (a case-sensitive substring, SQL LIKE with escaping) and `$regex` and `$not_regex`
   (Rust regular expressions). The two parts are separate conjuncts, so a disjunction or negation that mixes the document
   and metadata is unsupported. There is no `$not`: negation is pushed to the leaves, where `$eq`/`$ne`, `$in`/`$nin`,
   `$contains`/`$not_contains` and `$regex`/`$not_regex` are exact complements (observed: `$ne`, `$nin` and `$not_contains`
   also match records without the key); a negated range has no exact form except on the primary key, which every record has.
   Not available on metadata: null and existence tests, like, prefix, length, text operators, geo, element predicates. On
   the document: eq and neq as anchored regular expressions, prefix and like as regular expressions, contains_text as
   `$contains`, regex as `$regex`, match and match_any_term as an `$or` of case-insensitive word-bounded terms,
   match_all_terms as their `$and`, phrase as the terms joined by `\s+`; fuzzy and wildcard are unsupported. Every
   expression is prefixed with `(?s)`; patterns are built from string literals when the program is generated.

10. **Ranked text search.** `LocalExecutor::search` returns "Search operation is not implemented for local executor"
    (`rust/frontend/src/executor/local.rs`), and the server answers `/search` with that error (observed). BM25, sparse
    vectors, rank expressions and RRF are reachable only through the Search API, so keyword search, hybrid search, bm25
    functions, sparse fields, group-by and aggregations are unsupported on the local server.

11. **Distances and scores.** Spaces `l2` (squared Euclidean: [1,1] to [3,4] is 13), `ip` (1 - dot) and `cosine`
    (1 - cosine similarity) (observed; `rust/distance/src`). The canonical similarity of D3, as in the Milvus generator, is
    `-sqrt(d)` for l2 and `1 - d` for ip and cosine. Chroma does not normalize stored vectors (observed: a cosine collection
    returns [3, 4] as stored). `score native` returns the distance, ranked ascending. Bounds, offset and the exclusion of
    example records are applied in the runtime; Chroma has no score threshold.

12. **Index configuration.** The local server builds HNSW (SPANN is distributed only: `CreateCollectionError::SpannNotImplemented`
    in `service_based_frontend.rs`). Space, `ef_construction` and `max_neighbors` are set at creation; on an existing
    collection the server accepts `ef_search`, `max_neighbors`, `num_threads`, `resize_factor`, `sync_threshold` and
    `batch_size` (`UpdateHnswConfiguration`; observed: "unknown field `space`"). Per-query effort cannot be set, so query
    `params` other than target-recall and exhaustive are listed as ignored. Quantization is unsupported.

13. **Writes and visibility.** `SqliteLog::push_logs` commits a write, then backfills it into the metadata and HNSW segments
    and purges the log before the request returns (`rust/log/src/sqlite_log.rs`). Every acknowledged write is therefore
    visible and indexed: `wait`, flush, compaction, await-index and every consistency level need no call. `add` silently
    skips existing ids and `upsert` merges metadata into an existing record (observed), so insert checks for existing ids
    first and upsert deletes existing records before adding them again (not atomic).

14. **Reads.** `get` returns records in insertion order with `limit` and `offset`; scroll orders by key (or by the sort fields)
    in the runtime. `count` without a filter is the count endpoint; with a filter the matching ids are counted page by page.
    `query` asks for `limit + offset` results.

15. **Databases, tenants, namespaces.** A Chroma server has tenants, databases and collections; the HTTP API creates and
    deletes databases (observed). A vecDSL logical-db is a Chroma database of `default_tenant`; the connection's `database`
    selects the program's database (overridable with `VECDSL_CHROMA_DATABASE`). vecDSL tenants and namespaces are slices of
    one collection, so they are values of a metadata key (the partition-key field, else `vecdsl_tenant` or
    `vecdsl_namespace`), as in the Qdrant generator. Partitions are unsupported (no shard keys).

16. **Operations without a server call.** Forking is Chroma Cloud only (`fork_logs` returns `ForkCollectionError::Local` for
    the SQLite log; observed "Collection forking is unsupported for local chroma"), so clone copies the records into a new
    collection. Chroma has no snapshots, aliases, rerankers, generation, transactions, release, rebuild or index listing.
    `indexing_status` is not implemented locally (observed "Method scout_logs is not implemented"); by note 13 an index is
    ready when its collection exists.

17. **Collection names.** Collection and database names have 3 to 512 characters from `[a-zA-Z0-9._-]` and start and end with
    a letter or digit (observed validation error); other names are unsupported when the program is generated.

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. Collection metadata holding an array makes the server panic (`helpers.rs`, `unreachable!`); the request fails with a
   500. Collection tags are therefore stored as JSON text.
2. The Python SDK's `update` with a document and no embedding asks for an embedding function, because it re-embeds the
   document; the runtimes send the stored embedding with every document update.
3. The TypeScript SDK's `serializeMetadata` drops null values, so an update could not delete a metadata key; the
   TypeScript runtime posts updates to `/update` itself.
4. `add` silently skips an existing id and `upsert` merges metadata.
5. `indexing_status` fails with "Method scout_logs is not implemented"; every acknowledged write is already indexed.
6. The thin Python client's `AdminClient` needs `chroma_api_impl="chromadb.api.fastapi.FastAPI"` in its settings.

## Known limitations

- Upsert is delete and add and is not atomic: a reader can briefly miss the record (note 13).
- Keyword, hybrid and sparse search raise Unsupported: the open-source server's local executor does not implement the
  Search API. Chroma Cloud, which has it, is not a target.
- One dense vector field per collection; several vector fields per record are unsupported.
- Text predicates work on the document field only and take string literals, not parameters.
