# vecDSL to Vespa: mapping notes

Vespa 8.781.11 (`vespaengine/vespa:8.781.11`), one node. Each note names the construct, the requests the generator builds and the
reference that fixes the behavior. COVERAGE.md has one row per grammar construct; this file explains the choices behind the rows.
Abbreviations: `DOCS` = https://docs.vespa.ai/en/ (checked against the documentation repository at commit
7acd7ad152f0ce1eb2d087153ffdaffe403bfe9e), `SRC` = https://github.com/vespa-engine/vespa/blob/v8.781.11/. "ES" and "OS" name the
Elasticsearch and OpenSearch generators, the two other engines with a query language and BM25; each note says where Vespa agrees
with them and where it diverges.

## Application package

1. **The program owns one application.** Vespa has no "create index" request: schemas, rank profiles and clusters live in an
   application package that the config server deploys as a whole (DOCS `reference/api/deploy-v2.html#prepareandactivate`). The
   generator therefore tracks the package as the script's statements change it and every DDL statement deploys the complete
   package: `services.xml`, `schemas/<type>.sd` for every collection that exists at that point, and the default query profile. The
   runtime zips the files and POSTs them to `/application/v2/tenant/default/prepareandactivate`. Diverges from ES and OS, which
   create and change one index per request. Consequence: a program replaces whatever application was deployed on the node before.
2. **services.xml.** One container cluster `default` with `<document-api/>` and `<search/>`, one content cluster `vecdsl` with every
   document type in `mode="index"`, one node `node1` and `min-redundancy` 1. No `hosts.xml`: a package without one is deployed on
   the config server's own host (`SRC/config-model/src/main/java/com/yahoo/config/model/provision/SingleNodeProvisioner.java`).
   The query profile `default` raises `maxHits` to 10000 and `maxOffset` to 100000 so that scrolls and id selections page in 1000s
   (DOCS `reference/querying/query-profiles.html`).
3. **Created and dropped types are checked by the runtime.** Before deploying, the runtime reads the active package
   (`GET .../content/schemas/`, DOCS `reference/api/deploy-v2.html#get-application-content`): `create` of an existing type fails unless
   `if-not-exists`, `drop` of a missing type fails unless `if-exists`. A drop first deletes the type's documents (document/v1
   delete where the selection is true, DOCS `reference/api/document-v1.html#delete-where`) and deploys with a
   `validation-overrides.xml` that allows `schema-removal` until tomorrow (the runtime fills the date; DOCS
   `reference/applications/validation-overrides.html`). When the last schema is dropped the runtime deletes the application
   (`DELETE /application/v2/tenant/default/application/default`, DOCS `reference/api/deploy-v2.html#delete-application`).
4. **Waiting for a deployment.** After activation the runtime polls `serviceconverge` until `converged` is true and
   `currentGeneration` is at least the deployed session id (600 s limit), then `/status.html` and a query of each new document
   type, and a document/v1 GET (300 s limit). Polls run every 2 s; 429 and 503 answers are retried for up to 60 s. Every wait names
   what it waits for and its limit in its error message.

## Data model

5. **Collections are document types; ids are `id:vecdsl:<type>::<key>`.** The key field's value is the user part of the document
   id (DOCS `schemas/documents.html#id-scheme`); the namespace part is the constant `vecdsl`. Logical database `d` prefixes the
   document type: `d_<collection>`. Agrees with ES and OS (index name prefixes).
6. **Fields.** int, bigint, int8, float, double: `int`, `long`, `byte`, `float`, `double` attributes; bool: `bool`; varchar, text,
   uuid and the date types: `string` attributes with `match: cased` (exact, case-sensitive, as on the other targets); arrays:
   `array<...>`; a payload index adds `attribute: fast-search` (DOCS `reference/schemas/schemas.html#field`). A full-text field is an
   `index` field with `index: enable-bm25` plus a synthetic attribute `vecdsl_exact_<f>` (`indexing: input f | attribute`) that serves
   equality, `in` and `like` (DOCS `basics/schemas.html#synthetic-fields`). Agrees with ES and OS (a keyword sub-field).
7. **Vector fields** are `tensor<cell>(x[dim])` attributes with `distance-metric`: cosine `angular` (`prenormalized-angular` for
   normalized vectors), euclidean `euclidean`, dot `dotproduct`, hamming `hamming`; cells float, bfloat16, double, int8 (DOCS
   `reference/schemas/schemas.html#tensor`, `#distance-metric`). An hnsw or auto index adds `index` to the indexing statement and an
   `index { hnsw { max-links-per-node, neighbors-to-explore-at-insert } }` block when the script sets either value; without an index
   the attribute is searched exactly (DOCS `reference/schemas/schemas.html#index-hnsw`). float16 and uint8 have no Vespa cell type.
8. **Tenants, namespaces and partitions** are hidden `string` attributes `vecdsl_tenant`, `vecdsl_namespace`, `vecdsl_partition` with
   fast-search, written into each document and added to every filter (`vecdsl_tenant contains @p`). A partitioned collection writes
   `_default` when no partition is named. The id namespace (`id:<tenant>:...`) is not used, because a query cannot
   filter on it (only document selections can). Agrees with ES and OS (hidden keyword fields).
9. **TTL.** A collection that any statement gives a ttl gets a hidden `long` attribute `vecdsl_expires` (epoch seconds); every write
   sets it to now plus the ttl, or to 253402300799 without one. The content cluster has `garbage-collection="true"` and the document
   selection `<type>.vecdsl_expires > now()`, which removes expired documents (DOCS `schemas/documents.html#document-expiry`, DOCS
   `reference/applications/services/content.html#documents.garbage-collection`); because collection runs periodically, every query,
   count and get also skips documents with `vecdsl_expires <= now` (the runtime binds `@vecdsl_now`). `alter ... set ttl` updates
   `vecdsl_expires` of every document (update where, DOCS `reference/api/document-v1.html#update-where`). Diverges from ES and OS,
   which have no document TTL.
10. **if-version** adds a hidden `long` attribute `vecdsl_version` to the collection: a put sets 1, every update increments it, and an
    update with `if-version n` carries the test-and-set condition `<type>.vecdsl_version==n` (DOCS
    `reference/schemas/document-json-format.html#test-and-set`); a 412 answer is a version conflict. ES and OS use `if_seq_no`.

## Writes

11. **Insert** reads every id first (document/v1 GET) and fails before writing if one exists; each put then carries
    `"condition": "false", "create": true`, which fails with 412 if a concurrent writer created the document (DOCS
    `reference/api/document-v1.html#condition`, `#create`). `on-conflict ignore` skips 412 answers; `update` and `replace` and upsert
    send a plain put, which replaces the document. Vector sizes are checked before the first request.
12. **Updates** are partial updates: `assign` for set, `add` for array-append, `remove` for array-remove, `assign null` for
    remove-fields and for the fields `mode replace` does not set (DOCS `reference/schemas/document-json-format.html#update-operations`,
    `#clearing-a-field`). Every update has the condition `<type>` (or the version test), so an update of a missing document answers
    412 and is not counted. Agrees with ES and OS on the result shape (`update_count`).
13. **Filtered update and delete** select the keys with a query (`select * from <type> where <filter> order by <key>`, 1000 hits per
    page, `limit` stops after n keys) and then write each document by id. document/v1 update where and delete where are not
    used: their document selection has its own expression language without query parameters, so values would be written into
    the selection text.
14. **Bulk import** reads a local JSON array or JSON Lines file and puts each record. document/v1 takes one document per request,
    so `batch` is listed as ignored; the feed client (`vespa feed`) is a separate tool the programs do not depend on.
15. **Acknowledged writes are visible and durable**: Vespa acknowledges a put after the transaction log has it and the document is
    searchable (DOCS `writing/reads-and-writes.html`). `wait`, `flush`, `load`, `compact` and `await-index` therefore need no request
    beyond an existence check, and `await-index` waits for a converging deployment. ES and OS need `refresh`.

## Queries

16. **Every value is a query parameter.** The YQL text holds field names, document types, operators and numbers the generator
    writes; every value from the script or the caller is a request property `pN` referenced as `@pN`. Vespa substitutes a referenced
    value as a single term and never parses it as query syntax (DOCS `reference/querying/yql.html#parameter-substitution`,
    `SRC/container-search/src/main/java/com/yahoo/search/yql/YqlParser.java`). The runtime checks that each text, number and flag
    parameter has its type before sending. A list (`in`, `contains_any`) becomes `or` of one parameter per item, because a list
    parameter is split without quoting (`SRC/container-search/src/main/java/com/yahoo/search/yql/ParameterListParser.java`).
    ES and OS reach the same safety with JSON query DSL values.
17. **Filters.** Comparisons on numeric attributes (`<`, `<=`, `>`, `>=`, `=`), `between` as two inclusive comparisons, equality and
    `in` on string attributes as `contains` (exact on `match: cased`), `like 'x%'` as `contains ({prefix: true}@p)` on the exact
    attribute, text operators as `userInput` over the full-text field, `not x` as `(true and !(x))` because YQL needs a positive
    term next to a negation (DOCS `reference/querying/yql.html#not`). No missing-value test exists in YQL, so `is_null` and
    `is_not_null` are unsupported (ES and OS have `exists`).
18. **Vector search** is `({targetHits: k}nearestNeighbor(<field>, vecdsl_q))` with the rank profile `vecdsl_nn_<field>`, whose
    first phase is `closeness(field,<field>)` and whose match feature is `distance(field,<field>)` (DOCS
    `reference/querying/yql.html#nearestneighbor`, `reference/schemas/schemas.html#match-features`). The query vector is
    `input.query(vecdsl_q)`. `targetHits` is limit plus offset; `candidates`, the index's `search-candidates` or the extension key
    `explore_additional_hits` give `hnsw.exploreAdditionalHits`; `exact` gives `approximate: false`; `filter-strategy` sets
    `ranking.matching.postFilterThreshold` (pre_filter 1, post_filter 0, DOCS `reference/api/query.html#ranking.matching`); a
    timeout sets `timeout` and disables the soft timeout so that a late query fails instead of returning partial hits.
19. **Scores (D3).** The runtime converts the distance match feature: cosine `cos(d)` (d is the angle), euclidean and hamming `-d`,
    dot `-d` (Vespa's dotproduct distance is the negated dot product). `score native` returns the relevance, `closeness = 1/(1+d)`.
    A `bounds min` becomes `distanceThreshold` (cosine `acos(min)`, others `-min`, DOCS
    `reference/querying/yql.html#distancethreshold`) and is checked again on the converted score; `max` is checked in the runtime.
    ES and OS convert their transformed similarities in the same place.
20. **Keyword search** is `({defaultIndex: "<field>", grammar.composite: "and|or|phrase", grammar.tokenization: "linguistics",
    grammar.syntax: "none"}userInput(@p))` per searched field, joined with `or`, ranked by the profile `vecdsl_text`: the first phase is
    the sum of `query(vecdsl_w_<field>) * bm25(<field>)` over the full-text fields, so field boosts are query inputs and a field
    that is not searched weighs 0 (DOCS `reference/querying/yql.html#userinput`, DOCS `ranking/bm25.html`). Analyzer `bm25-k1` and
    `bm25-b` become `rank-properties` `bm25(<field>).k1` and `.b`. Agrees with ES and OS (BM25, boosts); diverges in that Vespa
    stems with its default linguistics (English) and removes no stop words.
21. **Hybrid search is fused in the runtime**, one query per leg, with D3's rrf (`1/(k + rank)`, ranks from 1), weighted and
    relative_score. Vespa's `reciprocal_rank_fusion` global-phase function ranks the union of the first-phase hits by every listed
    score, including documents that a leg did not retrieve, which is not D3's per-leg rank. Agrees with ES and OS (both fuse in
    the runtime).
22. **Recommend** uses Qdrant's average-vector strategy (2 avg(positive) - avg(negative)) computed in the runtime from the stored
    vectors (document/v1 GET), excluding the example ids. Agrees with ES and OS.
23. **count** is the `totalCount` of a query with `hits: 0`; `with-total` adds such a count of the filter. **scroll** sorts by the
    sort clauses and the key (`order by price desc, id asc`), and without a limit reads every page. **sample** uses the rank
    profile `vecdsl_random` (the `random` rank feature, DOCS `reference/ranking/rank-features.html`) or the key order. Names of
    tenants, namespaces and partitions are the groups of their hidden attributes (`all(group(vecdsl_tenant) max(1000)
    each(output(count())))`, DOCS `reference/querying/grouping-language.html`).
24. **get** reads each id with document/v1 GET and checks tenant, namespace, partition and expiry on the returned document.

## Extensions

25. **Rank profiles** (`extension vespa { rank-profile ... }` in a collection) are written into the schema as declared: inputs,
    functions, first, second and global phase, rerank-count, match and summary features, `num-threads-per-search` (DOCS
    `reference/schemas/schemas.html#rank-profile`). The prefix `vecdsl_` is reserved. A query selects one with
    `extension vespa { rank <profile> }` (`ranking.profile`); a vector search needs the profile input `query(vecdsl_q)`, and its
    scores are native.
26. **Query settings** by key on searches: `explore_additional_hits`, `post_filter_threshold`, `approximate_threshold`,
    `filter_first_threshold`, `filter_first_exploration`, `target_hits_max_adjustment_factor` (DOCS
    `reference/api/query.html#ranking.matching`). Other keys and keys on writes are rejected by name.

## Operations and limits

27. **rebuild-index** starts reindexing of the document type (`POST .../reindex?documentType=<type>`) and waits until its status
    is successful (DOCS `reference/api/deploy-v2.html#reindex`, DOCS `operations/reindexing.html`).
28. **Schema changes that need a content node restart** are unsupported: adding or removing an HNSW index on an existing field and
    adding or removing fast-search take effect only after the search node restarts (DOCS
    `reference/schemas/schemas.html#changes-that-require-restart-but-not-re-feed`), and a running application offers no restart
    through its HTTP APIs. Adding and removing fields deploy without a restart or re-feed. Removed fields keep their data on the
    content node until it is compacted away; the generated method says so.
29. **Not there.** Aliases (a schema has no alias), transactions (each document operation is atomic on its own), users and roles
    (Vespa authorizes clients by mTLS certificates, DOCS `reference/security/mtls.html`), release, per-schema storage settings,
    metrics objects, backup schedules. Generation pending (Vespa features exist): snapshots and restore (visit), clone and renames
    (deploy plus visit), embedders, generative search, sparse, binary and multi-vector fields, struct, map, geo and bytes fields,
    sort, group-by, aggregate, highlight, explain, cursor, mmr, boost, prefetch and the prefix, fuzzy and substring term types.

## Result shapes, slop and slices

References are the Vespa documentation pages at https://docs.vespa.ai/en/, read on 2026-11-15.

30. **explain** is `ranking.listFeatures: true` in the query body: "Set to true to request all rank-features to be calculated and
    returned"; they arrive in the summary field `rankfeatures` (`reference/api/query.html#ranking.listFeatures`). The runtime puts that
    field into the hit as `explanation`. The same page calls it a feature dump for training and advises against production use; vecDSL's
    explain is a diagnostic clause, so the dump is what it asks for. Hybrid search sets it on every leg and reports the legs' features in
    leg order (null where a leg did not return the hit).
31. **slop** maps to `userInput` with `grammar.composite` near (a match with operator and) or onear (a phrase) and the annotation
    `distance` = slop + 1: the YQL reference gives "distance (for near/oNear)" among the userInput annotations, near "matches if all
    argument terms occur within the specified distance, in any order", onear requires the query order, and distance is the "Maximum
    position difference for terms to match" (default 2) (`reference/querying/yql.html`, sections userInput, near, onear). Adjacent
    terms differ by one position, so slop 0 is distance 1. A match with operator or has no distance: unsupported.
32. **fuzziness, text-query fuzzy and the fuzzy text operator** are unsupported: the fuzzy operator supports "Only string attribute
    fields in documents ... (single, array or weightedset)" (`reference/querying/yql.html#fuzzy`); a keyword search and the text
    operators address index fields (enable-bm25), whose terms fuzzy cannot reach. The exact companion attribute `vecdsl_exact_<f>`
    holds the whole value, so fuzzy over it would compare the whole title with the query, which is not vecDSL's term fuzziness.
33. **highlight** reads a document summary (`reference/schemas/schemas.html#document-summary`, `querying/document-summaries.html`).
    Every schema with full-text fields gets `document-summary vecdsl_highlight` listing every document field and, per full-text field
    f, `summary vecdsl_hl_<f> { source: f  bolding: on }`. Bolding is to "Highlight matching query terms in the summary", the default
    element is `<hi>`, and it is "only supported for index fields of type string or array<string>"; bolding is "Contained in field or
    summary" (`#bolding`). A highlighted search sends `presentation.summary: vecdsl_highlight`; the hit carries
    `highlight: {f: [text]}` (one fragment per field, the shape of the Elasticsearch and pgvector targets). A separate summary keeps
    the default summary unbolded for every other query. Highlight on a field that is not full-text: unsupported.
34. **sort** orders the hits the search selected by relevance, in the runtime: the keys in order, missing values last, the earlier order
    breaking ties; `sort-by relevance` sorts by the score. YQL `order by` "overrides that ordering" (`yql.html#order-by`), which for a
    keyword search sorts every match and drops the relevance selection; the pgvector generator (sort the selected hits) is the meaning
    vecDSL's targets share. Full-text, vector and array fields: unsupported, as for scroll.
35. **cursor** is the key of the last hit of the previous page. Vespa's paging is `hits` and `offset`
    (`reference/api/query.html#hits`, `#offset`; the query profile raises maxHits to 10000 and maxOffset to 100000). The query reads the
    first 1000 hits of its order (nearestNeighbor targetHits 1000) and the runtime returns the hits after the cursor record, then
    offset and limit; a cursor whose record is not among them returns no hits, as in pgvector (a NULL comparison). A scroll pages in
    its total order (sort keys, then the key) until it passes the cursor record.
36. **group-by** is Vespa grouping in the same query: `| all(group(f) max(groups) each(max(m) each(output(summary()))))` with
    `hits: 0` (`reference/querying/grouping-language.html`); the groups come in the default order "-max(relevance())" (`#order--max`),
    each with its best m hits. Grouping covers the documents the query matches; for nearestNeighbor these are the targetHits nearest,
    so the generator sets targetHits to groups times hits per group (Qdrant's groups are drawn from a candidate list the same way).
    A full-text field groups by its exact attribute; array fields are unsupported, because grouping treats "each element ... as if
    they were all individual values in separate documents" (`#multivalue-attributes`). Results are `[{group, hits}]`, offset counts
    groups. A hybrid search groups the fused hits in the runtime (fusion runs there). A cursor or sort with group-by: unsupported.
37. **with-total of a hybrid search** is one count query of the union of the legs: a vector leg matches its filter (nearestNeighbor
    ranks every document that passes it), a keyword leg its text and filter; the totalCount of a query with hits 0 is exact for such
    conditions (`reference/api/query.html#hits`).
38. **scroll slices** are pgvector's (its MAPPING note 35): slice i of n holds the records whose key text's md5, in its
    first 28 bits, is i modulo n. The runtime pages the scroll in key order and keeps the slice's hits, so the slices are disjoint,
    cover the collection and are the same on every target.


## Connection strings

39. **A connection uri is an http or https URL; a uri without a scheme is read as `http://<uri>`** (`https://<uri>` with
    `tls`), the scheme a host and port without one get from `host` and `port` (the URL the generator builds from them).
    The query and document API URL of the program and the config server URL come from that one reading
    (`VespaGenerator.endpoint`): the URL is the uri so read, the config server URL its scheme and host with port 19071.
    A uri that does not parse as a URL with a host, or whose scheme is not http or https (`grpc://`, `mongodb://`), makes
    the connection statement raise "the connection uri ...; Vespa programs take an http or https URL with a host, or
    host:port (read as http://host:port)", and the program keeps the uri as written and the default config server URL.
    Until this note `configUrl` parsed the uri with `java.net.URI.create`, which throws on `127.0.0.1:1521/FREEPDB1`
    (an Oracle Easy Connect string: "Illegal character in scheme name") and on a uri with a space, so `vecdsl gen` ended
    with exit 3; `localhost:8080` parsed with the scheme `localhost` and no host, so the program used the default config
    server URL and the unusable query URL `localhost:8080`. Vespa's APIs are HTTP (DOCS `reference/api/query.html`,
    `reference/api/document-v1.html`, `reference/api/deploy-v2.html`); a uri without a scheme is accepted, so the
    common `host:port` works.

## Server findings

Behavior observed on the server at the pinned version that the documentation does not state, with the change it led to.

1. **bfloat16 by truncation.** Vespa converts values to bfloat16 by truncating the float, not by IEEE
   round-to-nearest: [0.6, 0.8] is stored as [0.59765625, 0.796875] and scores exactly 0.6 (cosine) against [1, 0].
2. **Empty slice values.** Listing tenants or namespaces groups the hidden slice attribute; documents written without
   the slice hold the empty string there. Change: the three runtimes skip the empty value.

## Known limitations

- A program replaces the application deployed on the node.
- The first deployment on a fresh node can take more than a minute; the runtime waits up to 600 s for convergence.
- Schema changes that need a restart are unsupported.
- A cursor page reads the first 1000 hits of its order.
- fuzziness is unsupported.
