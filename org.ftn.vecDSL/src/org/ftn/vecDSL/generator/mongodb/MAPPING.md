# vecDSL on MongoDB: mapping notes

Target: MongoDB 9.0.4 with mongot 1.70.1, deployed as the image `mongodb/mongodb-atlas-local:9.0.4` (a single-node replica set
with MongoDB Search and Vector Search). Clients: Python `pymongo` 4.20, TypeScript `mongodb` 7.9, Java
`org.mongodb:mongodb-driver-sync` 5.14 with Gson 2.13. COVERAGE.md lists every construct with its status and reference; the
notes below explain the choices that shape many rows.

1. **Everything is built at generation time.** Collection options, `$jsonSchema` validators, index definitions, filters and
   aggregation pipelines are written into the generated program as literal documents; the runtimes only send them and turn the
   replies into records. This is the Elasticsearch approach (request bodies at generation time), applied to database commands
   (https://www.mongodb.com/docs/manual/reference/command/).
2. **Collections are documents keyed by `_id`.** The declared primary key's value is `_id` (typed: int or long for integer
   keys, string for varchar, text and uuid); results carry it back under the declared key name. The validator lists the
   declared fields with `additionalProperties: false` unless the collection is `dynamic`
   (https://www.mongodb.com/docs/manual/core/schema-validation/specify-json-schema/).
3. **Numbers keep one BSON type in every language.** Integral values are int32 (int64 beyond its range), other numbers
   doubles, and vectors are always arrays of doubles; `{"$date": ISO}` values become BSON dates. Without this rule the
   Python, TypeScript and Java programs would send different BSON types for the same script (Python sends `0.0` as an int).
4. **Search indexes.** Each vector index is one `vectorSearch` index whose fields are the vector and every filterable field
   (plus `_id` and the hidden fields), so any filter of the script is a `$vectorSearch` pre-filter
   (https://github.com/mongodb/mongot/blob/v1.70.1/packages/search-index-schema/schema/vectorSearch/). Full-text fields form
   one search index `vecdsl_text` (string with the declared analyzer, plus autocomplete for prefix queries), with `_id` and
   `vecdsl_stamp` for the visibility probes.
5. **Scores follow D3.** mongot reports `(1 + cos) / 2`, `(1 + dot) / 2` and `1 / (1 + d²)` (Lucene `VectorSimilarityFunction`,
   https://github.com/mongodb/mongot/blob/v1.70.1/); the pipelines convert them back to the canonical similarity (`2s - 1`,
   `-sqrt(1/s - 1)`) in an `$addFields` stage, so bounds, sorting and fusion use one scale.
6. **Hidden fields.** `vecdsl_tenant`, `vecdsl_namespace`, `vecdsl_partition` (slices), `vecdsl_version` (if-version),
   `vecdsl_stamp` (visibility) and `vecdsl_expires` (ttl, with a TTL index) are written by the runtimes and removed from every
   result. Declared field names may not start with `vecdsl_`.
7. **Tenants, namespaces and partitions are values, with views.** A slice is a value of a hidden field, a filter field of
   every vector index; `create tenant x` adds a filtered view `<collection>_tenant_x`, and dropping a slice deletes its
   documents and the view.
8. **Aliases are views.** A view with an empty pipeline; swapping an alias is `collMod` with a new `viewOn`
   (https://www.mongodb.com/docs/manual/core/views/).
9. **Visibility.** mongot indexes from the change stream, so a search may miss a write for a while (eventual consistency,
   https://www.mongodb.com/docs/atlas/atlas-vector-search/vector-search-stage/). A `wait` write, or every write into a
   collection with `consistency strong`, stamps its documents and polls each search index of the collection until it shows
   them (deletes: until the keys are gone), with a timeout and a clear error.
10. **Hybrid search.** `fusion rrf` with k 60 and unweighted legs is `$rankFusion` (MongoDB 8.0+, `1 / (60 + rank)`, ranks
    from 1, https://www.mongodb.com/docs/manual/reference/operator/aggregation/rankFusion/); other fusions run in the runtime
    over one aggregate per leg.
11. **Recommendation** uses the average-vector strategy: `avg(positive) + (avg(positive) - avg(negative))` as the query vector
    of `$vectorSearch`, with the examples excluded by a pre-filter.
12. **Grouping.** A grouped search carries the group value as `vecdsl_group` through the projection, groups with `$group`
    and `$push`, keeps `size` hits per group with `$sortArray` and `$slice`, and removes `vecdsl_group` with `$unset`.
13. **Geo values.** vecDSL writes a geo_point value as a parameter (the validator admits no literal); the generator marks it
    `{"$geoPoint": "lat,lon"}` and the runtimes store a GeoJSON point (longitude first) for the 2dsphere index and
    `$geoWithin` with `$centerSphere` (https://www.mongodb.com/docs/manual/reference/operator/query/centerSphere/).
14. **Transactions** run the steps' commands in one session with read concern snapshot (or majority, local) and write
    concern majority, then wait for mongot after the commit
    (https://www.mongodb.com/docs/manual/core/transactions/).
15. **Credentials** come only from `VECDSL_MONGODB_USER` and `VECDSL_MONGODB_PASSWORD`; user information in a declared URI is
    removed at generation time, and the runtimes refuse a URI that carries credentials.

## Native rows

16. **Recommendation clauses** (`group by`, `group size`, `aggregate`, `with total`, `sort`) are the vector search's stages after
    the computed `$vectorSearch`: the average-vector query of note 11 replaces the query vector, and the generator builds the
    same `$group`, `$facet` and `$sort` stages as for a vector search (notes 12 and the vector-search rows of COVERAGE.md;
    https://www.mongodb.com/docs/manual/reference/operator/aggregation/group/,
    https://www.mongodb.com/docs/manual/reference/operator/aggregation/facet/,
    https://www.mongodb.com/docs/manual/reference/operator/aggregation/sort/). The positive and negative records stay out of the
    results as before.
17. **Update from records** (`update ... from <records>`) is one `update` command per record with `$set` of the record's fields
    (without the key and the version field), `$inc` of `vecdsl_version` when the collection is versioned, and `upsert: false`
    (https://www.mongodb.com/docs/manual/reference/command/update/: with upsert false a query that matches no document updates
    nothing). The runtimes call it write op `patch`. The count is the number of matched documents, so a record whose document is
    missing is skipped without an error. `ids` keeps the records with those keys. A filter, a slice, field clauses
    (`set`, `remove`, `append`), `if version` or `mode replace` together with records are unsupported: each would need a second
    query per record whose result the statement does not define.
18. **Rows that wait for the shared result-shape module** (generator/common/shapes): `mmr` and `boost` on
    vector, keyword and recommendation searches, and the result clauses of a hybrid search (`with total`, `sort`, `aggregate`,
    `boost`, `mmr`, `group by`). Each needs one implementation over the ranked records that serves both the `$rankFusion` path
    and the runtime fusion of note 10; writing it here would duplicate the module. Note 20 wires them
    through the module.
19. **Hybrid explain and highlight** stay pending. `$rankFusion` documents `scoreDetails`
    (https://www.mongodb.com/docs/manual/reference/operator/aggregation/rankFusion/), but its documentation says nothing about
    passing search highlights of the text leg through, and the runtime fusion has no score details for its vector legs
    (https://www.mongodb.com/docs/atlas/atlas-search/highlighting/ documents highlights only for `$search`). They wait for a
    server check.

## Shared result-shape module

20. **Boost and mmr, and the result clauses of a hybrid search, through the shared result-shape module.** MongoDB has no stage
    for maximal marginal relevance or for vecDSL's boost functions, and its `$rankFusion` and the runtime fusion end in a list of
    fused records. These clauses therefore go to `rt.shape`, which hands the candidates and the statement's spec to the shared
    module of generator/common/shapes (`vecdsl_shapes.py`, `vecdslShapes.ts`, `Shapes.java`, written next to the runtime of every
    generated project; contract in generator/common/shapes/README.md). The spec is `ShapeSpec.of(statement)` with the field names the collection has
    after rename-field, `keyField` the declared key and, where ShapeSpec leaves it null, the diversity field's metric
    (`MongodbShapes`). The candidates are the candidate window `ShapeSpec.window()`: ten times limit plus offset, or
    `max(candidates, limit + offset)` when mmr writes candidates (generator/common/shapes/README.md, "The window rule"). A vector search or a recommendation asks
    `$vectorSearch` for that many records (`limit` the window, `numCandidates` from it as for any limit,
    https://www.mongodb.com/docs/atlas/atlas-vector-search/vector-search-stage/); a keyword search keeps its `$search` and filters;
    a hybrid search keeps its legs and their limits and fuses as before (`$rankFusion` for rrf with k 60, else the runtime); then
    `$sort` by score and key, `$limit` the window (https://www.mongodb.com/docs/manual/reference/operator/aggregation/limit/) and a
    projection of the key, the score, the output fields, the fields the spec reads (group, sort, boost and aggregation fields), the
    diversity field's stored vector and, with with-vectors, every vector field; the runtime fusion keeps the window's fused records
    with offset 0, without bounds and unrounded. A keyword search's highlights and score details pass through (a hit's `highlights`
    becomes the module's `highlight`). A statement that writes boost or mmr runs all its result clauses in the module (offset,
    bounds, sort, groups, totals, aggregations), because they apply after the boost or the selection; a vector, keyword or
    recommendation search without them keeps its stages (`$skip`, `$limit`, `$group`, `$sort`, `$facet`; notes 12 and 16), the
    module not used. **Window rule, diverged from the pgvector generator:** totals, aggregations, groups and boosts cover the window,
    not every record the search matches; a group whose best record lies outside the window is missing, and a boost cannot raise a
    record from outside it. Boosts follow the pgvector generator: the functions apply to the canonical score (the
    fused score of a hybrid search) with their combine and apply modes. The result forms are the module's (pgvector's): a
    with-total or aggregate result is `{hits, total, aggregations}`, and an aggregation value prints as pgvector prints it (a max
    is a number), not as the `$facet` answer of a vector search (`[{"value": ...}]`). The refusals of the pgvector generator are
    raised at generation time by ShapeSpec (sort or mmr with group-by, boost or mmr with score native); the module's
    UnsupportedShape maps to the runtime's unsupported error. Not changed: cursors stay unsupported on searches (their reason in
    COVERAGE.md), the hybrid explain and highlight stay pending (note 19), mmr on a keyword search stays unsupported.

## Known limitations

- Atlas Local only: an Atlas cluster is not a target, and features of paid Atlas tiers raise Unsupported.
- mongot indexes asynchronously; reads after a write use the runtime's stamp and probes (note 9).
- The cursor rows of vector, keyword, recommend and hybrid searches are unsupported: ranked results have no key order
  to continue after.
- The Python, TypeScript and Java call columns of COVERAGE.md are empty for most native and concealed rows; the notes
  and the generated code name the commands.
