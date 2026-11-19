# vecDSL 1.0 semantics

The meaning of every vecDSL concept, in grammar order, as the IR records it. This is the object the COLA paper formalizes:
each definition names the IR feature it constrains, each normalization rule (N1 to N14) is implemented by
`org.ftn.vecDSL.ir` and tested by `VecDSLIrNormalizationTest`, and each divergence note cites the vendor document that
records it. Concept definitions name no vendor; vendor names appear only in the divergence notes.

Sources of a definition, in this order: the rule and its comment in `spec/VecDSL-1.0.xtext` ("grammar"), the decisions
D1 to D15 of its header, the validator (`VecDSLValidator.java`, whose rules are the well-formedness judgments of 1.0), the
admissibility table `search-params-admissibility.properties`, and `VecDSL.ecore`. Divergence notes cite
`generator/<vendor>/MAPPING.md` ("note n", "server finding n", for Oracle also its references "R n").

## 1. Program, IR and statuses

A **program** is a text accepted by the grammar whose model the validator reports no error for. Its **IR** is the JSON
document `org.ftn.vecDSL.ir.IrWriter` writes (`spec/ir/vecdsl-ir-1.0.schema.json`); a text that is not a program has a
**diagnostics document** instead (one entry per error, with the validator's code). The IR body determines the program up
to layout: two programs with equal bodies have equal models (`VecDSLIrRoundTripTest`).

**Capability statuses.** A target assigns each construct one of four statuses, with the definitions of the COVERAGE.md
headers, verbatim (the Milvus matrix; the SQL and Redis targets name SQL statements or commands instead of calls):

> Status: native (one SDK or REST call), concealed (the runtime composes documented calls with the exact vecDSL semantics),
> unsupported (Milvus lacks it; the generator raises the message), not-applicable (cannot occur for Milvus).

A construct "pending" in a matrix has one of the first two statuses and is not generated yet (the notes of the row say "generation pending"). Every
concept below may receive any of the four statuses unless a note says otherwise; the per-vendor values are in the
fourteen COVERAGE.md files and the coverage index.

**Unspecified in 1.0.** Where this document says "target-defined", 1.0 fixes no value; a later minor release may fix it
(VERSIONING.md). A program that relies on a target-defined value is portable only between targets that agree.

## 2. Normalization rules

| rule | statement | implemented in |
|---|---|---|
| N1 | A single-valued attribute the program does not write is `null` in the IR, even when the EMF model holds the first enumeration literal or 0; a flag is `false` unless written; an absent element is `null`, an empty list `[]`. "Written" is the node-model test the validator uses (`ModelUtil.written`). | IrSource.declared, IrWriter |
| N2 | The score of a query is a similarity, higher is better, unless the query writes `score native` (D3). `$norm.score` holds the form, `higherIsBetter` (true or null) and the canonical range of the metric in force (D3 table; the validator's T-Bounds), or null when it is unbounded, unknown or native. | IrNormalizer.score |
| N3 | A vector field without a name is named `vector` (D5, VecDSLQualifiedNameProvider); `$norm.name` holds the name in force. | IrNormalizer |
| N4 | A vector index belongs to one vector field: the field named by `on`, or the field that contains it. `$norm` on the index holds that field, the family, the accelerator if written and the quantization kind of its quantization member (D4); `$norm.index` on the field names its index. | IrNormalizer |
| N5 | A vector search or vector leg addresses the field named by `using`, or the only vector field of its collection (the validator's vf(c, using), T-VF). | IrNormalizer.addressedField |
| N6 | The metric in force of a vector search or leg is the written query metric, else the field's written metric, else the written metric of the field's index; otherwise null (target-defined). | IrNormalizer.metricInForce |
| N7 | A hybrid leg without its own input uses the hybrid search's input (grammar comment on HybridSearch.input; validator T-Hybrid); `$norm.input` is `own`, `shared` or null. | IrNormalizer.legInput |
| N8 | A snapshot's name is its `as` string, else its statement label (grammar comment on Snapshot). | IrNormalizer |
| N9 | A parameter reference `$p` and an environment reference `env("V")` are unresolved slots: `$norm.slot` records the kind, the name, the declared type (`text` for the environment) and whether a default exists. The IR never holds a resolved value. | IrNormalizer |
| N10 | A `params` block lists the keys its owning form admits (search-params-admissibility.properties) and the keys it writes, in the order of the table. | IrNormalizer |
| N11 | `targets` is kept as declared; `$norm.restricted` is false when the list is empty (no restriction, D1). | IrNormalizer |
| N12 | A reference is the qualified path of its target: the names of its named ancestors and its own, joined by dots (statements and the script add no segment). When several elements of the reference's type share the path, `#k` gives the target's 1-based position in document order. | IrNames |
| N13 | The vendor extension blocks of a host are an object keyed by vendor id in first-occurrence order; each value lists that vendor's blocks, each block its items, verbatim. The order of blocks of different vendors on one host carries no meaning (section 17). | IrWriter.extensions |
| N14 | Numbers are canonical JSON numbers (integral values below 10^15 without a fraction, other values in the shortest form that reads back to the same double). When the literal text and that number differ in value, `literals` keeps the text, keyed by the attribute's path. Positions live only in `spans`. | Json.number, IrSource.literal |

## 3. Statements (grammar sections 1 to 4)

**Script.** A named program (D1). `targets` restricts the targets the program is meant for (N11); 1.0 checks no
capability against it (no validator rule checks `targets` against the capability tables). `language` is
a hint the generator parameter overrides. Hints and provenance describe generated code, not data.
Divergence: none.

**Parameter.** A typed input bound by the signature of the generated function (N9). A default is a literal.

**Labeled.** A statement's label names it for output and, for a snapshot, its default name (N8). Labels are not
referenceable (VecDSLQualifiedNameProvider returns no name for a statement).

**Create.** Defines a concept inline or references an existing one, optionally nesting member creates. Well-formedness
(WF-CreateTop): a top-level create defines or references only VectorDatabase, Database, Cluster, VectorCollection,
Connection, EmbeddingModel, GenerativeModel, Reranker, Analyzer, FileResource, AuthConfig or BackupSchedule; a nested create
defines a top-level concept or a member of the enclosing concept. Adding a member to an existing collection is written
`create <collection> if-not-exists with { create... }`. `if-not-exists` makes the statement idempotent; DDL is not
atomic across its steps.
Divergences: Oracle commits every DDL statement (oracledb note O7); pgvector qualifies index names with the collection
; Vespa needs a content node restart for some index changes and reports them unsupported
(vespa note 28).

**Alter, AlterOperation.** Schema evolution of an existing concept: add, drop or rename a field (payload or vector, D5),
set a portable collection property, add an alias, add or drop a server-side function, rename the concept.
Divergences: Redis rewrites every document to drop or rename a field (redis note 22); Elasticsearch adds fields by mapping
update and moves values to rename (elasticsearch note 17).

**Drop.** Removes a concept; `if-exists` makes it idempotent.

**WriteScope, WriteOptions.** A write addresses at most one namespace, tenant and partition of its collection; `consistency`
is the acknowledgement level (one, quorum, all), `wait` asks that the write be durable and visible before the statement
ends. Undeclared values are target-defined (N1).
Divergence: Vespa acknowledges a write only once it is searchable, so `wait` holds without a request (vespa note 15).

**RecordSource.** Records come from a file or object-store location, from literal records, or from a `records` parameter.
A record literal gives values by field reference (resolved in the target collection, D6); struct and json fields take their
values from parameters in 1.0.

**Insert.** Writes new records; an existing id fails the statement unless `on-conflict` says otherwise (grammar comment).
`ttl` is a per-statement time to live in seconds.
Divergences: Milvus checks existence before writing (milvus COVERAGE.md, Method Insert, concealed); Vespa reads every id
first (vespa note 11); LanceDB writes merge inserts on the key column (lancedb note 8).

**Upsert.** Writes records, replacing whole records that share an id.

**Update.** Changes existing records selected by `ids` or a filter (WF-Upd: at most one of source and `set`, at least one
effect, at least one selector). `mode merge` changes the named fields, `replace` the whole record. `if-version` is
optimistic concurrency.
Divergences: Oracle compares ORA_ROWSCN (note O31); Vespa keeps a hidden version attribute (vespa note 10); Elasticsearch
sends `_bulk` updates without upsert for updates from records (elasticsearch note 28).

**Delete.** Removes records selected by exactly one of `ids`, `all` or a filter (WF-Del). `limit` bounds the number removed.
Divergence: LanceDB deletes the first rows in key order under a limit (lancedb note 17).

**BulkImport.** Server-side ingestion from a URI whose scheme selects the store or stream.
Divergence: Milvus reads `file://` locations in the runtime and inserts (milvus COVERAGE.md, Method BulkImport).

**Transaction.** A group of writes executed atomically where the target supports it; `isolation` names the level.
Divergences: Redis commits by WATCH, MULTI and EXEC without rollback (redis note 18, note 20); DuckDB runs one database
transaction per method (duckdb note 8); LanceDB commits per table (lancedb CP item 4).

**Operational statements.** Load and release make a collection resident or evict it; flush is a persistence barrier; compact
merges segments and purges deleted records; describe introspects one collection; migrate-embeddings re-embeds under a new
model; snapshot, restore, drop-snapshot and clone manage copies (D9: clone `shallow` or `snapshot`); rebuild-index,
swap-alias, refresh (an external collection), truncate (all records, schema kept), list and await-index act on indexes,
aliases and catalogs.
Divergences: Oracle loads and releases through In-Memory population (note O28) and compacts by segment shrink (note O29);
LanceDB snapshots are tags (lancedb note 20); Redis aliases survive index recreation (redis note 23); Milvus keeps
snapshots per collection, so restore needs the source collection.

## 4. Queries (grammar sections 5 and 6)

**Paging.** `offset` skips results; `cursor` resumes after a previous page. 1.0.1 (16 Nov 2026): the cursor is the
key of the last record of the previous page, and the page continues after that record in the statement's result order;
a cursor that matches no record gives an empty page (elasticsearch note 22, vespa note 35). A cursor
with `group-by` or `mmr` is target-defined (the pgvector generator rejects it).

**ReadScope.** A read covers the named namespace, tenant and partitions; `consistency` is the read level (strong, bounded,
session, eventual) and is target-defined when not written (N1); `as-of` reads the state at a time or version.
Divergences: Oracle reads as of a timestamp or SCN through flashback queries (note O27); Milvus Java scrolls ignore a
requested level (milvus note 3); MongoDB search is eventually consistent with writes (mongodb note 9).

**ScoreSpec, ScoreBounds.** N2. Bounds restrict the score in its form: on the canonical similarity, or on the native form
under `score native` (D3). T-Bounds: `min` is at most `max`, and both lie in the canonical range of the metric in force.
Divergences in the native forms, which the generators convert back to D3: Elasticsearch `(1+c)/2`, `1/(1+d^2)` and others
(elasticsearch note 8); OpenSearch score translations (opensearch note 8); Redis distances (redis note 12); MongoDB (mongodb
note 5); Vespa from the distance feature (vespa note 19); LanceDB (lancedb note 23); DuckDB (duckdb note 27). A boosted
Elasticsearch knn search reports the function_score `_score` (elasticsearch note 23).

**Reordering.** `mmr` diversifies by maximal marginal relevance with `lambda` over `candidates` hits; `boost` applies boost
functions (D8); `rerank` reorders with a declared reranker. On a hybrid search and a recommendation these clauses act on
the statement's result list after fusion or example search. The combination of boost functions and of boost and
relevance (`combine`, `apply`) is target-defined when not written.
Divergences: Elasticsearch MMR through the diversify retriever (elasticsearch note 11); OpenSearch `ext.mmr` (opensearch note
11); Oracle MMR as a recursive subquery (note O25); Elasticsearch boosts around a knn query (elasticsearch note 23).

**ResultShape.** `group-by` with `size` groups hits by a field; `with-vectors` returns vectors; `with-total` adds the count of
matching records; `exclude` drops fields; `highlight` marks query terms in the named fields; `sort` orders the selected
hits; `aggregate` computes aggregations over the matching records. 1.0 does not require query terms for `highlight`;
on targets whose highlighter marks query terms, `highlight` on a vector search or a recommendation marks nothing (observed on Elasticsearch
9.5.6 and Vespa 8.781.11) and the generators map it as unsupported.
On a hybrid search and a recommendation these clauses act on the result list after fusion or example search; the
targets without a native operator share one result-shape implementation (`generator/common/shapes/`).
Range aggregations have no buckets in 1.0.
Divergences: OpenSearch knn queries take k per segment, so totals, aggregations and groups depend on segmentation unless
the runtime compensates (opensearch MAPPING.md, server finding 2); Vespa groups through YQL grouping (vespa
note 36) and sorts in the runtime (vespa note 34); Oracle highlights through CTX_DOC.SNIPPET (note O26).

**QueryTail.** `output` projects fields (`*` for all); a filter restricts the records; extension blocks carry vendor
parameters (N13).

**VectorSearch.** The `top` k records nearest to the query input over the addressed field (N5) under the metric in force
(N6). `exact` asks for exhaustive evaluation (recall 1, D15); `explain` returns score details; `within` searches inside
arrays of structs or embeddings; `prefetch` stages feed the search with candidates; `model` names the query embedding
model.
Divergences: DuckDB pre-filters through `hnsw_prefilter` and returns underfilled pages under `post_filter` (duckdb note 30);
DuckDB applies OFFSET outside the index scan (duckdb note 29); Redis needs a filter for an exact search on an HNSW field
 and rejects EF_RUNTIME together with HYBRID_POLICY ADHOC_BF (redis MAPPING.md, server finding 1); Vespa stores bfloat16 cells by truncation, not by rounding (vespa MAPPING.md, server finding 1);
Elasticsearch reads a string bit vector as base64 in documents and as hex in queries, so the generators send byte arrays
(elasticsearch MAPPING.md, server finding 3).

**KeywordSearch.** The `top` k records by lexical relevance (BM25 family, D3) over the named fields, with optional boosts,
query type, operator, minimum match, `fuzziness` (a string: an edit distance or `AUTO`) and `slop`. `target-recall` is not
admitted (search-params-admissibility.properties).
Divergences: Redis has no length-dependent fuzziness, so `AUTO` is unsupported while numeric distances are native (redis note
31); Vespa's fuzzy operator matches string attributes, not index terms (vespa note 32); Oracle Text fuzzy
and NEAR use other units (note O30); Elasticsearch adds a custom similarity for analyzers with `bm25-k1` and `bm25-b`
(elasticsearch note 14).

**HybridSearch, VectorLeg, KeywordLeg.** Two or more legs fused by `fusion`: `rrf` (sum over legs of 1 / (k + rank)),
`weighted` (sum of weight times leg similarity; `alpha` for two legs) or `relative_score` (sum of weights times min-max
normalized leg scores) (grammar comment on EFusionType). A leg without input uses the shared input (N7). T-Hybrid: every
leg has an input; `alpha` needs exactly two legs and `fusion weighted`; `fusion-k` needs `fusion rrf`. `fusion` is
target-defined when not written (N1).
Divergences: MongoDB `$rankFusion` is native rrf with k 60 only (mongodb note 10); OpenSearch fuses rrf in a server
pipeline (opensearch note 12); Elasticsearch fuses in the runtime because the rrf retriever needs an enterprise license
(elasticsearch note 12); Vespa fuses in the runtime because its global-phase reciprocal rank fusion is another function
(vespa note 21); Redis fuses one vector and one keyword leg with FT.HYBRID (redis note 14).

**RecommendSearch.** The `top` k records similar to positive examples and dissimilar to negative ones (ids or vectors);
`lookup-in` resolves ids in another collection.
Divergences: the average-vector strategy in the runtime on several targets (elasticsearch note 13, vespa note 22, redis note
15, mongodb note 11); Elasticsearch recommendations cannot highlight (elasticsearch note 24).

**GetById, GenerativeSearch, DistanceMatrix, Count, Scroll, Sample, BatchQuery.** Records by primary key; generation over
search results with a generative model; the pairwise similarity matrix of a sample; the number of matching records (`exact`
for an exact count); full iteration with stable cursors and `slice i of n`; a random or first sample; independent queries
sent as one request.
Divergences: Oracle slices by ORA_HASH and pgvector by the md5 of the key text, so slices differ in membership but partition
the records alike (note O32); Elasticsearch scroll slices run in a point in time (elasticsearch note 27); Milvus's Node SDK
has no offset on its query iterator, so TypeScript conceals it (milvus note 2); Milvus distance matrices are exact for the
rows the server returns, in no defined order (milvus COVERAGE.md, Method DistanceMatrix).

**QueryInput.** A vector, sparse vector, multi-vector, text, id or image; `lookup-in` resolves an id to a stored vector.
T-List-Vec: a dense vector is a list of numbers of the field's dimension.

**ElementScope.** Search inside arrays of structs or embeddings, collapsing element scores (`max`, `sum`, `avg`,
`top_k`) with an optional element condition. Not generated yet on eight targets (their COVERAGE.md).

**MmrConfig, Prefetch.** See Reordering; a prefetch stage has its own input, field, `limit`, metric, params, nested stages
and filter; the meaning of a stage `weight` is target-defined.

**SearchParams.** Search-time effort and accuracy, by domain concept: `target-recall` (a declared recall floor q,
0 < q < 1, D15), `candidates`, `probes`, `scan-budget`, `timeout` (milliseconds), `oversampling`, `rescore`,
`filter-strategy`, `exhaustive`. WF-Recall: q in (0, 1); q excludes `exact`, `exhaustive`, `candidates`, `probes` and
`scan-budget`. Admissibility per form: N10.
Divergences: only Oracle realizes a recall target (oracledb CP; capability manifests, every other vendor `absent` or, for
Pinecone, `concealed` with a proposal to change it); `candidates` sets ef_search on DuckDB, pgvector, Qdrant and
LanceDB (duckdb note 31) and EF_RUNTIME or SEARCH_WINDOW_SIZE on Redis (redis note 11).

**SearchFieldRef, SortClause, AggregationClause, BoostBlock, BoostFunction.** A weighted text field; a sort key (field path
or relevance) with direction; an aggregation of a kind over a field path with alias, interval and bucket count; boost
functions of kind field value, decay (gauss, exp, linear around an origin with scale, offset and rate) or condition, with a
weight (D8).

## 5. Filters and literals (grammar sections 7 and 8)

**FilterBlock, conditions.** A boolean condition: `or` below `and` below `not`, parentheses, predicates. A predicate
compares a field path with a value (T-Cmp: the value's sort fits the field's), tests a unary property (null, existence,
emptiness), matches text, tests a range (`between` lower and upper, inclusive) or an array length. A field path resolves by
simple name in the target collection (D6) with nested sub-fields, or is a raw path for dynamic fields. Geo predicates test
a radius, a box, a polygon or a WKT relation; an element predicate tests that some element of an array of structs
satisfies a condition.
Divergences: Vespa has no missing-value test, so `is_null` and `is_not_null` are unsupported; MongoDB
vector-search pre-filters accept a fixed operator set (MongoDB `$vectorSearch` filter); LanceDB filters are SQL strings built at generation
time (lancedb note 22); Redis passes every value as a query parameter (redis note 9), as does Vespa (vespa note 16).

**Literals.** String, number (N14), boolean, null, list (dense vectors, id lists, IN lists, matrices), sparse vector
(index-weight pairs), parameter reference and environment reference (N9). Secrets are written as strings or environment
references only. There is no object literal.

## 6. Structural concepts (grammar sections 9 to 16)

**Connection.** Endpoint and credentials of a deployment: a host and port, a URI, a local path (embedded targets) or
in-memory, with optional API key, user, password, token, TLS, cloud, region, project, database and deployment mode.
Divergence: embedded targets reject server options in the generator, not in the validator.

**VectorDatabase, Database, Cluster.** A deployment with its connection, clusters, logical databases, authentication and
metrics (D12); a logical database is a named container of collections; a cluster groups collections with replication and
sharding.
Divergences: logical databases are name prefixes on Elasticsearch, OpenSearch, Redis and Oracle (elasticsearch note 4,
redis note 3, note O6), schemas on DuckDB (duckdb note 10), sub-directories on LanceDB (lancedb note 5).

**VectorCollection.** A set of records with one primary key, payload and vector fields (D5), indexes, partitions,
functions, namespaces, tenants, aliases, limits, generative models, storage, stored-source fields and cross-references.
T-Collection: the primary key is a payload field of sort int, bigint, uuid or varchar, and field names are unique in a
collection. Uniqueness of the key per collection or per partition is target-defined.
Divergences: pgvector widens the key with the partition column; Oracle keeps the declared key (note
O16); Elasticsearch and OpenSearch index names are lower-case snake case (elasticsearch note 3).

**Partition, Tenant, Namespace, Alias, BackupSchedule, Function.** A physical slice with a strategy and key; an isolation
unit; a schema-less slice with a key; a switchable alternative name; recurring snapshots with an interval and a retention
count; a server-side function that derives output fields from input fields (bm25, text_embedding, sparse_encoding,
rerank, custom).
Divergences: tenants, namespaces and partitions are hidden fields on Elasticsearch, OpenSearch, Redis, Vespa, MongoDB and
LanceDB (elasticsearch note 5, redis note 5, vespa note 8, mongodb note 7, lancedb note 9); aliases are views on DuckDB and
MongoDB (duckdb note 13, mongodb note 8); DuckDB has no partitions (duckdb note 12); `ttl-field` reads as an expiry time on
Milvus and a start time on Weaviate.

**Field, VectorField, PayloadField.** A typed attribute. A vector field has an element type, a dimension (optional for
sparse vectors), a metric, `normalized` (unit length), `multivector` with comparator `max_sim`, a sparse modifier, an
embedding model with source fields, `nullable`, `on-disk`, limits and an inline index (N3, N4). A payload field has a
scalar or structured type, length and element type, flags (nullable, nested, partition key, document body, full-text), a
default, an analyzer, limits and sub-fields.
Divergences: Vespa has no float16 or uint8 cells (vespa note 7); OpenSearch maps sparse vectors to rank_features (opensearch
note 14); Elasticsearch stores bfloat16, int8 and binary vectors as dense_vector element types (elasticsearch note 6);
LanceDB packs binary vectors into bytes (lancedb note 7); MongoDB keeps one BSON number type per value across languages
(mongodb note 3).

**Index, VectorIndex, PayloadIndex.** A vector index has a family (auto, flat, hnsw, ivf, vamana, sparse_inverted), a metric,
an accelerator, structural parameters (max degree, build and search candidates, centroids), `on-disk`, `online`, vendor
extensions and a quantization member (D4, N4). A payload index (D10) covers a field path with a value kind, a structure,
a cast type, an analyzer and token options.
Divergences: Redis quantizes only SVS-VAMANA with LVQ8 (redis note 7); Oracle builds scalar-quantized HNSW indexes with a
rescore factor (note O33); OpenSearch chooses the Faiss or Lucene encoder by bit width (opensearch note 6);
LanceDB maps hnsw to IVF_HNSW_SQ (lancedb note 11); DuckDB keeps the HNSW index in host memory and needs experimental
persistence in a file database (duckdb MAPPING.md, note 4 of each of its two numbered lists).

**Limits, models, analyzers, quantization, infrastructure, security, metrics.** Declared capacity limits of a cluster,
collection or field; embedding, generative and reranking models (provider validated against a catalogue, D13); analyzers
with a tokenizer, filters and BM25 parameters; file resources; quantization (none, scalar, binary, product, with bits,
sub-vectors, rescore, oversampling, keep-originals, truncated dimension); replication factor; sharding; storage medium;
authentication and roles; metrics export.
Divergences: rotation-based quantization has no kind of its own in 1.0; length filters
have no bounds.

**GeneratorHint, ProvenanceMetadata.** Code-shape preferences and the record of the generator run; neither affects data.

## 7. Vendor extensions (grammar section 17)

**VendorExtension, ExtensionItem.** A block of one vendor's parameters and constructs, ignored by every other target
(D2); legal hosts are the constructs that declare `extensions`; legality of keys is decided by the vendor manifest, not by
syntax (grammar comment). The IR keeps blocks verbatim, grouped by vendor (N13). Structured items: Vespa rank profiles,
their selection and deployed models (D8); Qdrant segment optimizers (D10), discovery and relevance feedback;
Weaviate inverted-index settings (D10, preview features PROVISIONAL under D14).
