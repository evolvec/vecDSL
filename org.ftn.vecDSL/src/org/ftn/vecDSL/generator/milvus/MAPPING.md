# Milvus mapping notes

Facts behind mappings closed after the full-coverage pass, numbered so that COVERAGE.md can cite them.
Each fact was read in the pinned SDK or server source, never taken from memory. Pins: server 3.0.4, pymilvus 3.0.2,
@zilliz/milvus2-sdk-node 3.0.6, milvus-sdk-java 3.0.12.

1. **Consistency on get and on queries.** pymilvus `MilvusClient.get` sends `conn.query(...)` with the caller's keyword
   arguments, and `Prepare.query_request` sets `consistency_level` from them (a name such as "Strong" becomes the enum value;
   checked by building the request with pymilvus 3.0.2). The Node SDK's `GetReq` and `QueryReq` have `consistency_level`
   (dist/milvus/types/Data.d.ts). The Java SDK's `GetReq` has no consistency level; `VectorService.get` turns it into a
   `QueryReq` by primary keys (`toQueryReq`), and `QueryReq.builder().ids(...)` builds the same primary key expression.
   So the Java runtime sends a get with a consistency level as `QueryReq.ids(...).consistencyLevel(...)`: the same
   QueryRequest as pymilvus and Node.
   Sources: https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/milvus_client/milvus_client.py (get);
   https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/client/prepare.py (query_request);
   https://unpkg.com/@zilliz/milvus2-sdk-node@3.0.6/dist/milvus/types/Data.d.ts (GetReq, QueryReq);
   https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/v2/service/vector/VectorService.java
   (get, buildQueryRequest); https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/v2/service/vector/request/QueryReq.java.

2. **Offset on scroll.** pymilvus `query_iterator` passes its keyword arguments to `QueryIterator` as `rpc_options`, and the
   iterator seeks `offset` rows by primary key before the first page (`__seek_to_offset`). milvus-sdk-java
   `QueryIteratorReq.offset(long)` is read by `QueryIterator`, which seeks the same way. The Node SDK's `QueryIteratorReq`
   is declared as `Omit<QueryReq, 'ids' | 'offset' | 'limit'>` plus `limit` and `batchSize`, so it has no offset; the
   TypeScript runtime asks for offset + limit rows and drops the first offset rows. Concealed in TypeScript, native in
   Python and Java; the rows are the same because every iterator pages in primary key order.
   Sources: https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/client/iterator/query_iterator.py;
   https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/orm/iterator/QueryIterator.java;
   https://unpkg.com/@zilliz/milvus2-sdk-node@3.0.6/dist/milvus/types/Data.d.ts (QueryIteratorReq).

3. **Consistency on scroll differs in Java.** pymilvus copies `consistency_level` into every query of the iterator (the
   first one fixes the snapshot timestamp). The Node SDK's `queryIterator` spreads the request, `consistency_level`
   included, into each page query (dist/milvus/grpc/Data.js). milvus-sdk-java 3.0.12 `QueryIterator.executeQuery` copies
   the level into the request and then calls `setUseDefaultConsistency(true)`; with that flag the proxy takes the
   collection's level and ignores the request's (`QueryTask.PreExecute`, task_query.go). The Java generator therefore
   raises "the 'consistency' clause on scroll in Java; milvus-sdk-java 3.0.12 query iterators send use_default_consistency,
   so Milvus applies the collection's level" (`MilvusJava.SCROLL_CONSISTENCY`) instead of emitting a clause the server
   would drop. Worth reporting to milvus-sdk-java, like the flush-state defect under "Known limitations".
   Sources: https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/orm/iterator/QueryIterator.java
   (executeQuery); https://github.com/milvus-io/milvus/blob/v3.0.4/internal/proxy/dql/task_query.go (useDefaultConsistency);
   https://unpkg.com/@zilliz/milvus2-sdk-node@3.0.6/dist/milvus/grpc/Data.js (queryIterator);
   https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/client/iterator/query_iterator.py.

4. **Offset on hybrid search.** pymilvus `Prepare.hybrid_search_request_with_ranker` writes `offset` (default 0) into the
   rank params beside `limit`, so the offset applies to the fused list. The Node SDK's `HybridSearchReq` keeps `offset`
   from `SearchSimpleReq`; milvus-sdk-java has `HybridSearchReq.offset(long)`. Checked by building the request with
   pymilvus 3.0.2: rank params `limit 2`, `offset 1`.
   Sources: https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/client/prepare.py;
   https://unpkg.com/@zilliz/milvus2-sdk-node@3.0.6/dist/milvus/types/Search.d.ts (HybridSearchReq);
   https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/v2/service/vector/request/HybridSearchReq.java.

5. **Group-by on hybrid search.** pymilvus appends `group_by_field` and `group_size` to the rank params of a hybrid search;
   the Node SDK's `HybridSearchReq` has `group_by_field` and `group_size`; milvus-sdk-java has
   `HybridSearchReq.groupByFieldName(String)` and `groupSize(Integer)`. The proxy copies the group field and size into every
   sub-request (`GroupByFieldId` and `GroupSize` in task_search.go), so each leg groups before fusion; the expected values of
   `hybridGroups` follow that order. A hybrid group-by forces a requery.
   Sources: https://github.com/milvus-io/pymilvus/blob/v3.0.2/pymilvus/client/prepare.py;
   https://unpkg.com/@zilliz/milvus2-sdk-node@3.0.6/dist/milvus/types/Search.d.ts;
   https://github.com/milvus-io/milvus-sdk-java/blob/v3.0.12/sdk-core/src/main/java/io/milvus/v2/service/vector/request/HybridSearchReq.java;
   https://github.com/milvus-io/milvus/blob/v3.0.4/internal/proxy/dql/task_search.go.

## Known limitations

- `distance_matrix` reads the first `sample` rows the server returns, which Milvus does not order; the values are exact
  for those rows, but the rows can differ between runs.
- The TypeScript runtime's `count` and `sampleRows` do not pass `consistency_level` to the SDK.
- Standard-tokenizer analyzers with a language ignore the language.
- A snapshot holds the writes older than the channel checkpoint. The runtimes flush and wait for the flush state with
  the collection name before they snapshot (the Java SDK's own wait omits the collection name, so the Java runtime
  polls `GetFlushState` itself).
- A rebuilt scalar index was seen reporting rows pending for minutes; `await-index` after a rebuild waits for them.
- The Java SDK's query iterators use the collection's consistency level, so a scroll with a level raises Unsupported
  in Java.
