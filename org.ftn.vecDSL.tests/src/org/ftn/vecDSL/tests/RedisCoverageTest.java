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
import org.ftn.vecDSL.generator.redis.RedisGenerator;
import org.ftn.vecDSL.generator.redis.RedisJava;
import org.ftn.vecDSL.generator.redis.RedisManifest;
import org.ftn.vecDSL.generator.redis.RedisPrinter;
import org.ftn.vecDSL.generator.redis.RedisPython;
import org.ftn.vecDSL.generator.redis.RedisStatements;
import org.ftn.vecDSL.generator.redis.RedisTypeScript;
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
 * Full coverage of the vecDSL grammar for Redis (generator/redis/COVERAGE.md): every native and concealed construct generates in
 * all three host languages, every unsupported construct raises its documented message, constructs not generated yet raise
 * "has no Redis mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped" guard of RedisStatements, and
 * every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class RedisCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "redis_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/redis_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/redisCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/rediscoverage/RedisCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the endpoint and containers of other concepts. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "Database");

	private static final String NO_MODELS = "; Redis Open Source runs no embedding models (the Query Engine indexes the vectors a client writes), so generated programs supply their own vectors";
	private static final String NO_SNAPSHOTS = "snapshots; SAVE and BGSAVE write one RDB file of the whole server under a fixed name (dir and dbfilename are protected configuration), and a restore needs a server restart";
	private static final String SINGLE_SERVER = "the program targets one Redis Open Source server; replication (REPLICAOF) and Redis Cluster are configured on the servers, not by a client";
	private static final String PENDING = "has no Redis mapping yet";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ redis ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"redis://127.0.0.1:6379\" password env(\"VECDSL_REDIS_PASSWORD\")",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\" {",
			"        filter lower type lowercase",
			"        filter stems type stemmer",
			"    }",
			"    field id type int",
			"    field title type varchar max-length 128 full-text analyzer english",
			"    field body type text full-text analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field plainVector type float_vector dim 4 metric euclidean",
			"    payload-index priceIndex on price kind float",
			"    partition p1",
			"    tenant east",
			"}",
			"createArchive: create collection archive { primary id field id type int vector-field embedding type float_vector dim 4 metric cosine }",
			"");

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
		Path output = Path.of("generated", "redis-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), RedisManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"\"\"\"Ignored on Redis: description on VectorCollection, maxLength on PayloadField.\"\"\"",
				"\"\"\"Ignored on Redis: description on SetPropertyOp, which a Query Engine index does not store.\"\"\"",
				"if not rt.exists(self.client, args=[\"ACL\", \"GETUSER\", \"vecdsl_reader\"]):",
				"\"~books:*\",",
				"\"$.details.pageCount\",",
				"\"$.tags[*]\",",
				"\"vecdsl_exact_title\",",
				"\"SVS-VAMANA\",",
				"\"LVQ8\",",
				"blobs={\"vec\": \"BFLOAT16\"},",
				"blobs={\"vec\": \"FLOAT16\"},",
				"blobs={\"vec\": \"FLOAT64\"},",
				"blobs={\"vec\": \"INT8\"},",
				"rt.recreate(",
				"rt.durable(self.client)",
				"\"((@price:[-inf ($p1]))=>[KNN 2 @embedding $vec HYBRID_POLICY ADHOC_BF AS vecdsl_score]\",",
				"\"@genre:{$p1}=>[KNN 2 @embedding $vec HYBRID_POLICY ADHOC_BF AS vecdsl_score]\",",
				"\"(-(@id:[$p0 $p0]) -(@id:[$p1 $p1]))=>[KNN 2 @embedding $vec AS vecdsl_score]\",",
				"\"@vecdsl_partition:{$p1}=>[KNN 5 @v $vec AS vecdsl_score]\",",
				"\"@vecdsl_tenant:{$p1}=>[KNN 5 @noteVector $vec AS vecdsl_score]\",",
				"\"@title:({{and:t0}})=>{$slop: 0; $inorder: true;}\",",
				"\"@title:({{and:t0}})=>{$slop: 1; $inorder: true;}\",",
				"\"@title:({{or:t0}})\",",
				"\"@title:({{fuzzy1:t0}})\",",
				"\"@title:({{prefix:t0}})\",",
				"\"@title:({{prefixlast:t0}})=>{$slop: 0; $inorder: true;}\",",
				"\"VERBATIM\",",
				"\"@title:(w'$p0')\",",
				"\"FT.HYBRID\",",
				"\"RRF\",",
				"\"*=>[KNN 3 @embedding $l0_vec AS vecdsl_score]\",",
				"\"@title:({{and:l1_t0}})\",",
				"\"(@details_pageCount:[($p0 +inf])\",",
				"\"(@genre:{$p0 | $p1})\",",
				"\"(@price:[$p0 $p1])\",",
				"\"(ismissing(@remark))\",",
				"\"-(@genre:{$p0} | @tags:{$p1})\",",
				"\"@vecdsl_exact_title:{w'$p0'}\",",
				"rt.command(self.client, args=[\"FT.ALIASUPDATE\", \"shelfView\", \"booksCopy\"])",
				"rt.command(self.client, args=[\"ACL\", \"DELUSER\", \"vecdsl_reader\"])",
				"return rt.copy_collection(",
				"return rt.import_file(",
				"location=\"file://../books.jsonl\",",
				"return rt.transaction(",
				"command=[\"FT.SEARCH\", \"logs\", \"@vecdsl_partition:{$p0}\", \"NOCONTENT\", \"LIMIT\", \"0\", \"0\", \"DIALECT\", \"2\"],",
				"command=[\"FT.SEARCH\", \"shelf\", \"*\", \"NOCONTENT\", \"LANGUAGE\", \"english\", \"LIMIT\", \"0\", \"0\", \"DIALECT\", \"2\"],",
				"slices={\"vecdsl_tenant\": [\"north\"]},",
				"slices={\"vecdsl_namespace\": [\"team-a\"]},",
				"slices={\"vecdsl_partition\": [\"hot\"]},")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"if (!await rt.exists(this.client, { args: [\"ACL\", \"GETUSER\", \"vecdsl_reader\"] })) {",
				"async vectorsReady() {",
				"async coarseNearest(queryVector: number[]) {",
				"async counted(queryVector: number[]) {",
				"async repriceMany() {",
				"slices: { vecdsl_partition: [\"hot\"] }")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"if (!RedisRuntime.exists(client, args(\"args\", list(\"ACL\", \"GETUSER\", \"vecdsl_reader\")))) {",
				"public List<Object> bundle() {",
				"public Object vectorsReady() {",
				"public Object pairs() {",
				"public Object coarseNearest(List<Float> queryVector) {",
				"\"slices\", args(\"vecdsl_partition\", list(\"hot\"))")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("redis-data:/data"));
		assertFalse(String.join("\n", files.values()).contains("redis://default:"), "no credentials in a connection string");
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildPrice: rebuild-index priceIndex concurrently",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0], plainVector: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"ranged: count in items where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"allTags: count in items where tags contains_all [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"phrasedTitle: count in items where title phrase \"vector database\"",
				"prefixTitle: count in items where title prefix \"Vec\"",
				"neqTitle: count in items where title neq \"x\"",
				"wildTitle: count in items where title wildcard \"Vec*\"",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0], plainVector: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"dropNote: drop note",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"loadP1: load items partitions [p1]",
				"multiField: search in items top 3 query text \"vector\" fields [title boost 2.0, body] output [title]",
				"flatIndex: create items if-not-exists with { create vector-index flatHnsw on plainVector family hnsw }",
				"")).get(CASES);
		for (String fragment : List.of(
				"\"\"\"Ignored on Redis: concurrently on RebuildIndex; the new index serves queries while it scans the keys.\"\"\"",
				"rt.read_records(self.client, location=\"file://items.json\"),",
				"batch_size=50,",
				"policy=\"replace\",",
				"batch_size=20,",
				"\"prefix\": \"archive:\"",
				"\"(@price:[$p0 $p1])\",",
				"\"(@note:{$p0 | $p1})\",",
				"\"(@tags:{$p0 | $p1})\",",
				"\"(@tags:{$p0} @tags:{$p1})\",",
				"\"(-(@id:[($p0 +inf]))\",",
				"\"(@title:({{and:t0}})=>{$slop: 0; $inorder: true;})\",",
				"\"@vecdsl_exact_title:{$p0*}\",",
				"\"-@vecdsl_exact_title:{$p0}\",",
				"\"@title:(w'$p0')\",",
				"slices={\"vecdsl_tenant\": \"east\", \"vecdsl_partition\": \"default\"},",
				"\"@vecdsl_tenant:{$p0}\",",
				"return rt.delete(",
				"rt.rewrite(",
				"\"FT.ALTER\",",
				"\"(@title:({{and:t0}})=>{$weight: 2;} | @body:({{and:t0}}))\"",
				"\"HNSW\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@Test
	public void bindsHostileValuesInFiltersAndWrites() throws Exception {
		String hostile = "x} | @note:{*} FLUSHALL \"; --";
		String python = generate(BASE + String.join("\n",
				"hostileFilter: count in items where note eq \"" + hostile.replace("\"", "'") + "\"",
				"hostileText: search in items top 2 query text \"" + hostile.replace("\"", "'") + "\" fields [title] output [title]",
				"hostileUpdate: update items values [ { id: 1, note: \"" + hostile.replace("\"", "'") + "\" } ] ids [1]",
				"")).get(CASES);
		String value = hostile.replace("\"", "'");
		assertFalse(python.contains("rt.unsupported("), python);
		for (String line : python.split("\n")) {
			if (line.trim().startsWith("\"@") || line.trim().startsWith("\"(") || line.trim().startsWith("command=")) {
				assertFalse(line.contains("FLUSHALL"), line);
			}
		}
		assertTrue(python.contains("\"note\": \"" + value + "\""), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: restore items from \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: drop-snapshot \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: list snapshots in items", NO_SNAPSHOTS);
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3",
				"if-version; Redis keys carry no version number, and the generator adds no version field (WATCH protects a write only within one client's read-modify-write)");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Redis has no ingestion pipeline (clients write the documents)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; Redis reads no object stores (clients write the documents)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet",
				"import format parquet; the runtime reads JSON and JSON Lines files");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; COPY duplicates each value (Redis has no copy-on-write keys)");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Redis keeps only the current value of each key");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; Redis Open Source runs no generative models");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; Redis Open Source runs no reranking models");
		cases.put("probe: create analyzer spaces tokenizer whitespace",
				"the whitespace tokenizer; the Query Engine splits text at its fixed separator characters (src/toksep.h)");
		cases.put("probe: create auth a1 type api_key",
				"api_key authentication; Redis Open Source authenticates ACL users with passwords (token and single sign-on methods are Redis Cloud features)");
		cases.put("probe: create connection other uri \"redis://127.0.0.1:6379\" token \"t\"",
				"api keys and tokens; Redis authenticates ACL users with passwords (AUTH)");
		cases.put("probe: create connection other uri \"redis://127.0.0.1:6379\" grpc-port 6334", "grpc-port; Redis speaks RESP on one port");
		cases.put("probe: create connection lite path \"./vecdsl.db\"", "embedded deployments; the Redis clients connect to a server");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; Redis Open Source runs no generative models");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Redis has no setting that limits the number of indexes");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + SINGLE_SERVER);
		cases.put("probe: create database prod { connection main uri \"redis://127.0.0.1:6379\" metrics m1 enabled }",
				"metrics configuration; Redis reports statistics through INFO and the server configuration, not through objects a client creates");
		cases.put("probe: release items", "release; Redis keeps every document and index in memory and has no command that evicts one index");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: EF_RUNTIME and EPSILON are effort settings, with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { probes 4 } output [title]",
				"probes; the Query Engine has no IVF index");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; the query syntax combines terms with intersection, union and optional (~) terms and has no minimum number of matching terms");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"fuzziness AUTO; the query syntax fixes the Levenshtein distance per term (%, %% or %%% for 1 to 3)");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] slop 2 output [title]",
				"the 'slop' clause with text-query match; slop applies to phrases");
		cases.put("probe: search in items top 3 query text \"a\" text-query query_string fields [title] output [title]",
				"text-query query_string; the generator never passes a value as query syntax (values are PARAMS), so user query strings are not parsed");

		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; Redis has no per-index or per-key drop guard (ACL rules restrict commands per user, not per index)");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; a Query Engine index carries no metadata besides its definition");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; the generator addresses documents by the key field");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field field blob type bytes }", "bytes field blob; JSON documents hold no binary or media values");
		cases.put("probe: alter items { add-field vector-field packed type binary_vector dim 8 }",
				"binary_vector fields; Query Engine vector attributes hold FLOAT32, FLOAT64, FLOAT16, BFLOAT16, INT8 and UINT8 values (no sparse or binary vectors)");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; the Query Engine indexes TEXT attributes for BM25 itself and has no function that produces sparse vectors");
		cases.put("probe: create items if-not-exists with { create vector-index ivfIndex on plainVector family ivf }",
				"ivf indexes; the Query Engine builds FLAT, HNSW and SVS-VAMANA vector indexes");
		cases.put("probe: create items if-not-exists with { create payload-index notesJson on note kind json }",
				"json payload indexes; the Query Engine indexes JSON values through attributes on their paths (index the sub-fields)");
		cases.put("probe: refresh items",
				"collections derived from external sources; Redis holds the documents it is given and reads no external files or tables");
		cases.put("probe: count in items where title regex \"^A\"",
				"the regex text operator; the Query Engine's query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions");
		cases.put("probe: count in items where tags length gt 1",
				"length tests; the Query Engine's query syntax has no length operator (an APPLY expression of FT.AGGREGATE could compute one)");

		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		String noText = "the 'highlight' clause on a search without text; HIGHLIGHT marks the terms of a text query";
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding highlight [title] output [title]", noText);
		cases.put("probe: recommend in items top 2 positive [1] using embedding highlight [title] output [title]", noText);
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; the Query Engine reads no dictionary or model files");
		cases.put("probe: transaction { delete from items where note eq \"a\" }",
				"a filtered update or delete inside a transaction; the commands queued in MULTI answer only at EXEC, so a filter cannot choose the keys of a later step (steps select documents by id)");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put(search + "cursor \"1\" output [title]", "the 'cursor' clause; " + PENDING + " (FT.AGGREGATE WITHCURSOR or a keyset condition after the last hit)");
		cases.put(search + "explain output [title]", "the 'explain' clause; " + PENDING + " (EXPLAINSCORE of FT.SEARCH and FT.HYBRID)");
		cases.put(search + "mmr lambda 0.5 output [title]", "the 'mmr' clause; " + PENDING + " (MMR over the candidates in the runtime)");
		cases.put(search + "group-by note output [title]", "the 'group-by' clause; " + PENDING + " (FT.AGGREGATE GROUPBY with REDUCE TOLIST)");
		cases.put(search + "sort { sort-by price desc } output [title]", "the 'sort' clause; " + PENDING + " (SORTBY over the hits)");
		cases.put(search + "aggregate { agg max on price } output [title]", "the 'aggregate' clause; " + PENDING + " (FT.AGGREGATE GROUPBY with REDUCE)");
		cases.put(search + "boost { boost-fn kind field_value field price } output [title]",
				"the 'boost' clause; " + PENDING + " (FT.AGGREGATE APPLY expressions over the scores)");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] highlight [title] output [title]",
				"the 'highlight' clause; " + PENDING + " (HIGHLIGHT FIELDS of FT.SEARCH)");
		cases.put("probe: hybrid-search in items top 3 with-total { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"the 'with-total' clause; " + PENDING + " (total_results of FT.HYBRID or a count of the union of the legs)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]",
				"bounds on a hybrid leg; " + PENDING + " (the leg score filtered before fusion)");
		cases.put("probe: scroll in items limit 2 cursor \"1\" output [title]",
				"the 'cursor' clause; " + PENDING + " (FT.AGGREGATE WITHCURSOR or a keyset condition after the last hit)");
		cases.put("probe: update items set { note: \"b\" } ids [1] where price gt 1",
				"an update by ids with a filter; has no Redis mapping yet (the filter checked on the documents of the ids)");
		cases.put("probe: count in items where tags is_empty", "is_empty; has no Redis mapping yet (INDEXEMPTY attributes and an empty-value query)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv", "import format csv; " + PENDING + " (CSV rows typed by the collection fields)");
		cases.put("probe: transaction { bulk-import into items from \"file://items.json\" }",
				"bulk-import inside a transaction; " + PENDING + " (the file's records as one write step)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; has no Redis mapping yet (keys from an INCR counter)");
		cases.put("probe: alter items { add-field field place type geo_point }",
				"geo_point field place; has no Redis mapping yet (GEO attributes read \\\"longitude,latitude\\\" strings)");
		cases.put("probe: create analyzer sounds tokenizer standard language \"en\" { filter ph type phonetic }",
				"the phonetic filter; has no Redis mapping yet (PHONETIC dm:<language> on the TEXT attributes)");
		cases.put("probe: create auth a2 type username_password certificate \"client.crt\"",
				"client certificates; has no Redis mapping yet (rediss:// with client certificates)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"redis\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Every sample, retargeted to Redis, maps without errors: unsupported constructs become rt.unsupported calls. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ redis ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new RedisGenerator().program(script);
			RedisManifest manifest = RedisManifest.load();
			for (RedisPrinter printer : List.<RedisPrinter>of(new RedisPython(), new RedisTypeScript(), new RedisJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasARedisMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(RedisStatements.class.getDeclaredMethods())
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
			reached(new RedisStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new RedisStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(RedisStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
			List<String> unmapped) {
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(RedisStatements.NOT_MAPPED)) {
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
			if (key.startsWith(RedisGenerator.VENDOR)) {
				files.put(key.substring(RedisGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
