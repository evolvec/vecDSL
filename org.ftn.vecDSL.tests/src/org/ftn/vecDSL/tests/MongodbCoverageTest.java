package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.mongodb.MongodbGenerator;
import org.ftn.vecDSL.generator.mongodb.MongodbJava;
import org.ftn.vecDSL.generator.mongodb.MongodbManifest;
import org.ftn.vecDSL.generator.mongodb.MongodbPrinter;
import org.ftn.vecDSL.generator.mongodb.MongodbPython;
import org.ftn.vecDSL.generator.mongodb.MongodbStatements;
import org.ftn.vecDSL.generator.mongodb.MongodbTypeScript;
import org.ftn.vecDSL.vecDSL.Alter;
import org.ftn.vecDSL.vecDSL.AlterOperation;
import org.ftn.vecDSL.vecDSL.Concept;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLFactory;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Full coverage of the vecDSL grammar for MongoDB (generator/mongodb/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented reason, constructs not generated
 * yet raise "has no MongoDB mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped" guard of
 * MongodbStatements, and every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class MongodbCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "mongodb_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/mongodb_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/mongodbCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/mongodbcoverage/MongodbCoverage.java";
	private static final String CASES = "python/coverage_cases.py";
	private static final List<String> DATA_FILES = List.of("books.jsonl", "notes.jsonl", "team.jsonl", "logs.jsonl");

	/** Concepts whose bare instance emits no command: the endpoint and containers of other concepts. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "Database");

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ mongodb ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"mongodb://127.0.0.1:27017/?directConnection=true\" password env(\"VECDSL_MONGODB_PASSWORD\")",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\"",
			"    field id type int",
			"    field title type varchar max-length 128 full-text analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field half type float_vector dim 4 metric euclidean",
			"    payload-index priceIndex on price kind float",
			"    tenant east",
			"}",
			"createParts: create collection parts {",
			"    primary id",
			"    field id type int",
			"    vector-field v type float_vector dim 2 metric euclidean",
			"    partition p1",
			"}",
			"createArchive: create collection archive { primary id field id type int vector-field embedding type float_vector dim 4 metric cosine }",
			"");

	private static final String NO_MODELS = "; the Atlas Local image runs mongot without the embedding block (Voyage AI API key files) that automated embedding needs, so generated programs supply their own vectors";
	private static final String NO_SNAPSHOTS = "snapshots; MongoDB backs up with mongodump, mongorestore and file system snapshots, tools outside the database commands, and cloud backups and their schedules are Atlas features";
	private static final String NO_RESIDENCY = "WiredTiger reads documents and indexes into its cache when they are accessed, and MongoDB has no command that loads or evicts a collection";
	private static final String SINGLE = "the Atlas Local image runs one mongod as a single-node replica set; more members and shards are server deployments, not database commands";
	private static final String NO_RERANK = "MongoDB has no reranking stage in the Atlas Local deployment (Voyage AI reranking is an Atlas model API, outside the database)";
	private static final String AS_OF = "the 'as-of' clause; $vectorSearch and $search read mongot's current index, and snapshot reads at a past cluster time reach back only minSnapshotHistoryWindowInSeconds (300 s by default)";
	private static final String RERANK = "the 'rerank' clause; " + NO_RERANK;
	private static final String HYBRID_DETAILS = " clause on a hybrid search; has no MongoDB mapping yet (score details and highlights through $rankFusion and the runtime fusion)";
	private static final String HYBRID_LEGS = " { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private GeneratorDelegate generator;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@Test
	public void generatesTheFullSampleInEveryLanguage() throws Exception {
		Map<String, String> files = generate(Files.readString(SAMPLE, StandardCharsets.UTF_8));
		Path output = Path.of("generated", "mongodb-full");
		write(files, output);
		MongodbManifest manifest = MongodbManifest.load();
		for (String data : DATA_FILES) {
			Files.writeString(output.resolve(data), manifest.render("environment/" + data), StandardCharsets.UTF_8);
		}
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"if not rt.exists(self.client, database=\"catalog\", collection=\"notes\"):",
				"{\"dropDatabase\": 1}",
				"\"maxProperties\": 20",
				"\"maxItems\": 8",
				"\"description\": \"Books about databases\"",
				"\"description\": \"Books about vector databases\"",
				"\"hnswOptions\": {\"maxEdges\": 16, \"numEdgeCandidates\": 200}",
				"\"indexingMethod\": \"flat\"",
				"\"quantization\": \"scalar\"",
				"\"quantization\": \"binary\"",
				"\"similarity\": \"dotProduct\"",
				"{\"type\": \"filter\", \"path\": \"vecdsl_partition\"}",
				"{\"type\": \"filter\", \"path\": \"vecdsl_tenant\"}",
				"{\"type\": \"filter\", \"path\": \"vecdsl_namespace\"}",
				"{\"dropRole\": \"vecdsl_reader\"}",
				"\"createRole\": \"vecdsl_reader\"",
				"\"place\": {\"$geoPoint\": north_place}",
				"\"$centerSphere\": [[19.85, 45.25]",
				"return rt.import_file(",
				"ignore_conflicts=True",
				"ttl_seconds=3600",
				"\"$push\": {\"tags\": {\"$each\": [\"classic\"]}}",
				"\"$pull\": {\"tags\": {\"$in\": [\"graph\"]}}",
				"\"$unset\": {\"tags\": \"\"}",
				"\"$inc\": {\"vecdsl_version\": 1}",
				"{\"vecdsl_version\": {\"$eq\": 0}}",
				"return rt.transaction(",
				"read_concern=\"snapshot\"",
				"{\"fsync\": 1}",
				"{\"compact\": \"books\", \"force\": True}",
				"\"$rankFusion\": {",
				"return rt.hybrid(",
				"return rt.recommend(",
				"\"$facet\": {",
				"{\"$unset\": \"hits.vecdsl_group\"}",
				"\"highlight\": {\"path\": [\"title\"]}",
				"\"$toHashedIndexKey\"",
				"\"$similarityCosine\"",
				"\"$similarityEuclidean\"",
				"\"viewOn\": \"booksCopy\"",
				"{\"renameCollection\": \"vecdsl.books\", \"to\": \"vecdsl.shelf\"}",
				"kind=\"distinct\"",
				"kind=\"views\"",
				"kind=\"search_indexes\"",
				"kind=\"databases\"",
				"kind=\"collections\"",
				"return rt.shape(",
				"candidates=rt.aggregate(",
				"candidates=rt.recommend(",
				"candidates=rt.hybrid(",
				"                    {\"$limit\": 20},\n",
				"                \"mmr\": {\"lambda\": 0.5, \"candidates\": 6, \"vectorField\": \"embedding\", \"metric\": \"cosine\"},\n",
				"                \"aggregations\": [{\"name\": \"topPrice\", \"kind\": \"max\", \"field\": [\"price\"], \"interval\": None, \"size\": None}],\n",
				"                \"group\": {\"field\": [\"genre\"], \"size\": 2},\n",
				"            vectors=[\"embedding\"],\n")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"rt.importFile(this.client, {",
				"rt.transaction(this.client, {",
				"rt.hybrid(this.client, {",
				"rt.recommend(this.client, {",
				"rt.awaitIndex(this.client, {",
				"return await rt.shape(this.client, {",
				"candidates: true,")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"MongodbRuntime.importFile(",
				"MongodbRuntime.transaction(",
				"MongodbRuntime.hybrid(",
				"MongodbRuntime.awaitIndex(",
				"public Object pairs() {",
				"return MongodbRuntime.shape(",
				"\"candidates\", true)),")) {
			assertTrue(java.contains(fragment), fragment);
		}
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildEmbedding: rebuild-index embeddingIndex concurrently",
				"rebuildPrice: rebuild-index priceIndex",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"compactLater: compact items async",
				"deleteAll: delete from items all",
				"explained: search in items top 2 query text \"a\" fields [title] explain exclude [note] output * extension mongodb { maxTimeMS = 1000 }",
				"unvectored: vector-search in items top 2 query vector $queryVector using embedding exclude [note] output * extension mongodb { comment = \"probe\" }",
				"fused: hybrid-search in items top 2 exclude [note]" + HYBRID_LEGS.replace("output [title]", "output *") + " extension mongodb { allowDiskUse = true }",
				"liked: recommend in items top 2 positive [1] using embedding exclude [note] output * extension mongodb { maxTimeMS = 1000 }",
				"counted: count in items extension mongodb { comment = \"count\" }",
				"fetched: get from items ids [1] with-vectors output [title] extension mongodb { comment = \"get\" }",
				"paged: scroll in items limit 2 output [title] extension mongodb { comment = \"scroll\" }",
				"drawn: sample in items size 1 output [title] extension mongodb { comment = \"sample\" }",
				"matrix: distance-matrix in items using embedding sample 2 extension mongodb { comment = \"matrix\" }",
				"loaded: bulk-import into items from \"file://items.json\" on-conflict update",
				"merged: upsert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] batch 50",
				"listed: list collections limit 5",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"addFieldLater: create items if-not-exists with { create field rating type int }",
				"addVectorLater: create items if-not-exists with { create vector-field later type float_vector dim 2 metric cosine }",
				"addAlias: create items if-not-exists with { create alias itemsView for items }",
				"addFilter: create items if-not-exists with { create analyzer folded tokenizer standard language \"en\" { filter lower type lowercase filter folding type ascii_folding } }",
				"replicas: create cluster replicated { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } } if-not-exists with { create replication r1 factor 1 }",
				"storageCheck: create items if-not-exists with { create storage s1 type disk }",
				"likeGroups: recommend in items top 2 positive [1] using embedding group-by note size 2 output [title]",
				"likeShaped: recommend in items top 2 positive [1] using embedding with-total sort { sort-by price asc } aggregate { agg avg on price } output [title]",
				"fileUpdate: update items from \"file://changes.json\" ids [1]",
				"")).get(CASES);
		for (String fragment : List.of(
				"\"updateSearchIndex\": \"items\"",
				"{\"dropIndexes\": \"items\", \"index\": \"priceIndex\"}",
				"return [self.both_query_0(), self.both_query_1()]",
				"{\"compact\": \"items\", \"force\": True}",
				"return rt.delete(",
				"\"scoreDetails\": True",
				"\"explanation\": {\"$meta\": \"searchScoreDetails\"}",
				"\"note\": 0",
				"\"maxTimeMS\": 1000",
				"\"comment\": \"probe\"",
				"\"allowDiskUse\": True",
				"op=\"merge\"",
				"batch_size=50",
				"limit=5",
				"\"viewOn\": \"items\"",
				"\"asciiFolding\"",
				"\"replSetGetStatus\": 1",
				"\"serverStatus\": 1",
				"\"$group\": {\"_id\": \"$vecdsl_group\"",
				"\"total\": [{\"$count\": \"n\"}]",
				"op=\"patch\",",
				"only=[1],")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: update items values [ { id: 1, note: \"a\" } ] where price gt 1",
				"update from records with a filter, a slice, field clauses, if-version or mode replace; update commands without upsert address documents by _id, and each record carries the values it sets");
		cases.put("probe: restore items from \"weekly\"", "restoring " + NO_SNAPSHOTS);
		cases.put("probe: hybrid-search in items top 3 fusion rrf group-by note size 2 sort { sort-by price asc }" + HYBRID_LEGS,
				"sort with group-by; groups follow the relevance of their best hit");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding mmr lambda 0.5 group-by note output [title]",
				"mmr with group-by; MMR selects single hits, groups select records per group value");
		cases.put("probe: drop-snapshot \"weekly\"", "dropping " + NO_SNAPSHOTS);
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\" retention 3", "backup schedules; " + NO_SNAPSHOTS);
		cases.put("probe: load parts partitions [p1]", "loading a collection; " + NO_RESIDENCY);
		cases.put("probe: release parts partitions [p1]", "releasing a collection; " + NO_RESIDENCY);
		cases.put("probe: refresh items",
				"collections derived from external sources; MongoDB reads parquet, lance, iceberg or vortex files neither as collections nor through a database command (Atlas Data Federation is an Atlas service)");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; MongoDB has no generative model stage");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; " + NO_RERANK);
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; MongoDB has no generative model stage");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; Atlas Search reads synonyms from a collection and has no file-based word lists or dictionaries");
		cases.put("probe: create auth a1 type token", "token authentication; OIDC needs MongoDB Enterprise or Atlas");
		cases.put("probe: create auth a2 type mtls", "mtls authentication; the Atlas Local image serves without TLS, which X.509 authentication needs");
		cases.put("probe: create auth a3 type sso", "sso authentication; LDAP, Kerberos and OIDC need MongoDB Enterprise or Atlas");
		cases.put("probe: create auth a4 type username_password certificate \"client.crt\"", "client certificates; the Atlas Local image serves without TLS");
		cases.put("probe: create auth a5 type username_password audit-log", "audit logs; auditing needs MongoDB Enterprise or Atlas");
		cases.put("probe: create connection other uri \"mongodb://127.0.0.1:27017\" token \"t\"",
				"bearer tokens; the database authenticates SCRAM users (OIDC tokens need MongoDB Enterprise or Atlas)");
		cases.put("probe: create connection other uri \"mongodb://127.0.0.1:27017\" grpc-port 6334", "grpc-port; MongoDB serves the wire protocol only");
		cases.put("probe: create connection lite path \"./vecdsl.db\"", "embedded deployments; MongoDB runs as a server that the drivers reach over the wire protocol");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; MongoDB has no setting that limits the number of collections or databases");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v2 type float_vector dim 2 } sharding shards 2 }",
				"sharding; " + SINGLE);
		cases.put("probe: create cluster c3 { collection inner3 { primary id field id type int vector-field v3 type float_vector dim 2 } replication factor 3 }",
				"a replication factor of 3; " + SINGLE);
		cases.put("probe: create database prod { connection main uri \"mongodb://127.0.0.1:27017\" metrics m1 enabled }",
				"metrics configuration; MongoDB metrics come from serverStatus and mongot's Prometheus endpoint, both configured on the servers, not per program");
		cases.put("probe: create collection memo { primary id field id type int vector-field v type float_vector dim 2 storage s1 type memory }",
				"memory storage; the in-memory storage engine is MongoDB Enterprise only");
		cases.put("probe: create collection dots { primary id field id type int vector-field v type float_vector dim 2 metric dot }",
				"the dot metric on vectors not declared normalized; mongot's dotProduct requires vectors normalized to unit length at index and query time");
		cases.put("probe: create collection tagged tags [\"a\"] { primary id field id type int vector-field v type float_vector dim 2 }",
				"collection tags; MongoDB collection options carry a validator, not tags (the description goes into the $jsonSchema)");
		cases.put("probe: create collection guarded deletion-protection { primary id field id type int vector-field v type float_vector dim 2 }",
				"deletion-protection; MongoDB has no per-collection guard against drop (only roles without the dropCollection action)");
		cases.put("probe: create collection clustered { primary id field id type int vector-field v type float_vector dim 2 { vector-index ivfIndex family ivf } }",
				"ivf indexes; mongot builds hnsw and flat vector indexes");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: numCandidates is an effort setting, with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding output [title] where title prefix \"a\"",
				"prefix conditions in a vector search; the $vectorSearch pre-filter accepts $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or, $not and $nor on boolean, date, numeric, string and UUID values");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding highlight [title] output [title]",
				"the 'highlight' clause; highlights mark the terms of a text query, which a vector search has not");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("probe: count in items as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: get from items ids [1] as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("probe: scroll in items limit 2 as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("probe: sample in items size 1 as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("probe: distance-matrix in items using embedding sample 2 as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("probe: hybrid-search in items top 3 fusion rrf as-of \"2026-01-01\"" + HYBRID_LEGS, AS_OF);
		cases.put("probe: recommend in items top 2 positive [1] using embedding as-of \"2026-01-01\" output [title]", AS_OF);
		cases.put("r1: create reranker r1 provider \"cohere\"\nprobe: vector-search in items top 3 query vector $queryVector using embedding rerank r1 output [title]", RERANK);
		cases.put("r1: create reranker r1 provider \"cohere\"\nprobe: search in items top 3 query text \"a\" fields [title] rerank r1 output [title]", RERANK);
		cases.put("r1: create reranker r1 provider \"cohere\"\nprobe: hybrid-search in items top 3 fusion rrf rerank r1" + HYBRID_LEGS, RERANK);
		cases.put("r1: create reranker r1 provider \"cohere\"\nprobe: recommend in items top 2 positive [1] using embedding rerank r1 output [title]", RERANK);
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: search in items top 3 query text \"a b c\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; the text operator matches any or all terms (matchCriteria), and compound minimumShouldMatch counts clauses, not the terms of one query");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"fuzziness AUTO; the text operator takes maxEdits 1 or 2 and has no length-dependent edit distance");
		cases.put("probe: search in items top 3 query text \"a\" fields [note] output [title]",
				"keyword search on note, which is not a full-text field of the search index");
		cases.put("probe: compact items target-segments 1", "the 'target-segments' clause; WiredTiger compacts collection files and has no segments");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; MongoDB pulls no streams itself (Atlas Stream Processing is an Atlas service)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; MongoDB stores documents that a client sends (mongoimport and Atlas Data Federation are outside the database commands)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet", "import format parquet; the generated programs read JSON documents");
		cases.put("probe: transaction isolation serializable { delete from items ids [1] }",
				"serializable isolation; MongoDB transactions read a snapshot and detect write-write conflicts only (snapshot isolation)");
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; MongoDB has no per-collection guard against drop (only roles without the dropCollection action)");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; the key is _id, which cannot be renamed");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; _id cannot be removed");
		cases.put("probe: alter items { add-field vector-field wide type float64_vector dim 2 }",
				"float64_vector fields; mongot indexes vectors of doubles and BinData vectors of float32, int8 and int1");
		cases.put("probe: alter items { add-field vector-field terms2 type sparse_float_vector metric dot }",
				"sparse vectors; vectorSearch indexes take dense vectors only (vector and filter fields)");
		cases.put("probe: drop english",
				"dropping an analyzer; analyzers are removed with the search index, not while fields use them");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding within tags output [title]",
				"the 'within' clause; has no MongoDB mapping yet (nestedRoot vector indexes over arrays of embedded documents)");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding prefetch { stage limit 10 } output [title]",
				"the 'prefetch' clause; has no MongoDB mapping yet (prefetch stages as input pipelines)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf explain" + HYBRID_LEGS, "the 'explain'" + HYBRID_DETAILS);
		cases.put("probe: hybrid-search in items top 3 fusion rrf highlight [title]" + HYBRID_LEGS, "the 'highlight'" + HYBRID_DETAILS);
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv",
				"import format csv; has no MongoDB mapping yet (CSV records in the runtime)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; has no MongoDB mapping yet (ObjectId keys returned as strings)");
		cases.put("probe: create collection small { primary id field id type int vector-field v type int8_vector dim 2 }",
				"int8_vector fields; has no MongoDB mapping yet (BinData vectors of subtype int8 and int1 in the three runtimes)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey().lines().reduce((first, last) -> last).orElseThrow(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"mongodb\", " + Syntax.quote(entry.getValue()) + ")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Scripts without targets generate for every registered vendor: the other vendors' samples must map without errors. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ mongodb ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new MongodbGenerator().program(script);
			MongodbManifest manifest = MongodbManifest.load();
			for (MongodbPrinter printer : List.<MongodbPrinter>of(new MongodbPython(), new MongodbTypeScript(), new MongodbJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAMongodbMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(MongodbStatements.class.getDeclaredMethods())
				.filter(method -> method.getName().equals("_body")).map(Method::getParameterTypes)
				.filter(types -> types.length == 1).map(types -> types[0].getSimpleName()).toList();
		Script script = VecDSLFactory.eINSTANCE.createScript();
		for (EClass type : concrete(model.getMethod())) {
			if (!dispatched.contains(type.getName())) {
				unmapped.add(type.getName() + " has no dispatch case");
			}
		}
		for (EClass type : concrete(model.getConcept())) {
			Create create = VecDSLFactory.eINSTANCE.createCreate();
			create.setDefinition((Concept) named(type, "probe"));
			reached(new MongodbStatements(script, "vecdsl"), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new MongodbStatements(script, "vecdsl"), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(MongodbStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept, List<String> unmapped) {
		List<Program.Method> methods;
		try {
			methods = statements.methods(statement, "probe");
		} catch (RuntimeException e) {
			return;
		}
		boolean empty = true;
		for (Program.Method method : methods) {
			empty &= method.body().isEmpty();
			for (Code.Stmt stmt : method.body()) {
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(MongodbStatements.NOT_MAPPED)) {
					unmapped.add(type.getName() + ": " + failure.feature());
				}
			}
		}
		if (concept && empty && !EMPTY_BY_DESIGN.contains(type.getName())) {
			unmapped.add(type.getName() + " generates nothing");
		}
	}

	private static List<EClass> concrete(EClass root) {
		return VecDSLPackage.eINSTANCE.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
				.filter(type -> !type.isAbstract() && !type.isInterface() && root.isSuperTypeOf(type) && type != root
						&& VecDSLPackage.eINSTANCE.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
								.noneMatch(sub -> sub != type && type.isSuperTypeOf(sub)))
				.collect(Collectors.toList());
	}

	private static EObject named(EClass type, String name) {
		EObject instance = VecDSLFactory.eINSTANCE.create(type);
		EStructuralFeature feature = type.getEStructuralFeature("name");
		if (feature != null) {
			instance.eSet(feature, name);
		}
		return instance;
	}

	private void write(Map<String, String> files, Path output) throws IOException {
		for (Map.Entry<String, String> file : files.entrySet()) {
			Path path = output.resolve(file.getKey());
			Files.createDirectories(path.getParent());
			Files.writeString(path, file.getValue(), StandardCharsets.UTF_8);
		}
	}

	private Map<String, String> generate(String source) throws Exception {
		Script script = parseHelper.parse(source);
		validation.assertNoErrors(script);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		fsa.setOutputConfigurations(outputConfigurations.getOutputConfigurations().stream()
				.collect(Collectors.toMap(OutputConfiguration::getName, configuration -> configuration)));
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(() -> false);
		generator.generate(script.eResource(), fsa, context);
		Map<String, String> files = new TreeMap<>();
		fsa.getTextFiles().forEach((key, content) -> {
			if (key.startsWith(MongodbGenerator.VENDOR)) {
				files.put(key.substring(MongodbGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
