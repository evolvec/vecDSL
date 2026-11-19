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
import org.ftn.vecDSL.generator.lancedb.LancedbGenerator;
import org.ftn.vecDSL.generator.lancedb.LancedbJava;
import org.ftn.vecDSL.generator.lancedb.LancedbManifest;
import org.ftn.vecDSL.generator.lancedb.LancedbPrinter;
import org.ftn.vecDSL.generator.lancedb.LancedbPython;
import org.ftn.vecDSL.generator.lancedb.LancedbStatements;
import org.ftn.vecDSL.generator.lancedb.LancedbTypeScript;
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
 * Full coverage of the vecDSL grammar for LanceDB (generator/lancedb/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented message, constructs not generated
 * yet raise "has no LanceDB mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped" guard of
 * LancedbStatements, and every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class LancedbCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "lancedb_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/lancedb_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/lancedbCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/lancedbcoverage/LancedbCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the storage URI, containers and FTS options of other concepts. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "Analyzer", "AnalyzerFilter", "AuthConfig");

	private static final String EMBEDDED = "the embedded LanceDB library runs inside the program on one machine";
	private static final String NO_AUTH = "an embedded library has no users or authentication (access is the file system's)";
	private static final String NO_MODELS = "; generated LanceDB programs supply their own vectors (embedding functions and rerankers of the SDK registries call model libraries or remote services)";
	private static final String NO_FUNCTIONS = "LanceDB computes no columns with server-side functions (full-text search is an index; embedding functions run in the client SDK)";
	private static final String PENDING = " has no LanceDB mapping yet";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ lancedb ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local path \"lancedb-data\"",
			"dropItems: drop items if-exists",
			"dropArchive: drop archive if-exists",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\" {",
			"        filter lower type lowercase",
			"    }",
			"    field id type int",
			"    field title type varchar full-text analyzer english",
			"    field price type float",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine",
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
		Path output = Path.of("generated", "lancedb-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), LancedbManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"\"\"\"Ignored on LanceDB: maxLength on PayloadField, limits on PayloadField, searchCandidates on VectorIndex.\"\"\"",
				"\"\"\"Ignored on LanceDB: consistency on SetPropertyOp, since every read checks for newer versions (read_consistency_interval 0).\"\"\"",
				"{\"name\": \"tagIndex\", \"column\": \"tags\", \"kind\": \"label_list\"},",
				"{\"name\": \"flags\", \"type\": \"vector\", \"element\": \"uint8\", \"dim\": 1, \"bits\": 8, \"nullable\": False},",
				"{\"name\": \"half\", \"type\": \"vector\", \"element\": \"float16\", \"dim\": 2, \"nullable\": False},",
				"\"kind\": \"ivf_pq\",",
				"group={\"key\": \"genre\", \"size\": 2},",
				"\"kind\": \"ivf_rq\",",
				"indexes=[{\"name\": \"logIndex\", \"column\": \"v\", \"kind\": \"ivf_flat\", \"metric\": \"l2\", \"num_partitions\": 2}],",
				"on_conflict=\"skip\",",
				"location=\"file://../books.jsonl\",",
				"return rt.create_tag(self.client, table={\"name\": \"books\"}, tag=\"seeded\")",
				"values={\"pages\": \"CAST(100 AS INT)\"},",
				"rt.rename_column(self.client, table={\"name\": \"books\"}, name=\"meta\", new_name=\"remark\")",
				"values={\"tags\": \"array_concat(`tags`, ['classic'])\"},",
				"values={\"tags\": \"array_remove_all(`tags`, 'graph')\"},",
				"values={\"embedding\": [0.5, 0.5, 0.2, 0]},",
				"values={\"tags\": \"NULL\"},",
				"slice={\"vecdsl_tenant\": \"north\"},",
				"slice={\"vecdsl_namespace\": \"team-a\"},",
				"slice={\"vecdsl_partition\": \"hot\"},",
				"plan={\"column\": \"v\", \"metric\": \"l2\", \"where\": \"`vecdsl_partition` IN ('hot')\", \"nprobes\": 2},",
				"plan={\"column\": \"coarse\", \"metric\": \"cosine\", \"refine_factor\": 4},",
				"plan={\"column\": \"coarse\", \"metric\": \"cosine\", \"exact\": True},",
				"query_vector = rt.lookup_vector(",
				"\"where\": rt.sql(\"`price` >= {f0}\", {\"f0\": min_price}),",
				"\"prefilter\": False,",
				"{\"column\": \"embedding\", \"metric\": \"cosine\", \"kind\": \"vector\", \"vector\": query_vector, \"weight\": 0.7},",
				"fusion=\"relative_score\",",
				"order=[{\"key\": \"price\", \"direction\": \"desc\"}],",
				"where=\"contains_tokens(lower(`title`), 'vector design')\",",
				"where=\"NOT ((`genre` = 'kitchen' OR array_has(`tags`, 'ann')))\",",
				"where=\"array_has_any(`tags`, ['coffee', 'classic'])\",",
				"where=\"`details`.`pageCount` > 100\",",
				"where=\"`remark` IS NULL\",",
				"where=\"`price` BETWEEN 30 AND 50\",",
				"slice={\"index\": 1, \"total\": 2},",
				"version=\"seeded\",",
				"shallow=True,",
				"return rt.restore_tag(self.client, table={\"name\": \"books\"}, tag=\"seeded\")",
				"new_name=\"shelf\",",
				"where=\"true\",")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"async coarseNearest(queryVector: number[]) {",
				"async nearFirst() {",
				"async postFiltered(queryVector: number[], minPrice: number) {",
				"async vectorsReady() {")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"public List<Map<String, Object>> coarseNearest(List<Float> queryVector) {",
				"public List<Map<String, Object>> postFiltered(List<Float> queryVector, double minPrice) {",
				"public Map<String, Object> vectorsReady() {")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertFalse(files.containsKey("docker-compose.yml"), "an embedded deployment runs no server");
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"fromFile: insert into items from \"file://../items.json\"",
				"importUpdate: bulk-import into items from \"file://../items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven graphs\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"insertError: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0] } ] on-conflict error",
				"phrasedSlop: search in items top 3 query text \"vector design\" text-query phrase fields [title] slop 1 output [title]",
				"fuzzyAuto: search in items top 3 query text \"grapj\" fields [title] fuzziness \"AUTO\" output [title]",
				"postKeyword: search in items top 3 query text \"graph\" fields [title] params { filter-strategy post_filter } output [title] where price gt 1",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"bundle: batch-query { count in items vector-search in items top 2 query vector $queryVector using embedding output [title] }",
				"")).get(CASES);
		for (String fragment : List.of(
				"\"base_tokenizer\": \"simple\",",
				"\"stem\": False,",
				"\"lower_case\": True,",
				"\"ascii_folding\": False,",
				"rows=rt.read_records(self.client, location=\"file://../items.json\"),",
				"on_conflict=\"update\",",
				"batch_size=50,",
				"batch_size=20,",
				"on_conflict=\"error\",",
				"plan={\"columns\": [\"title\"], \"kind\": \"phrase\", \"slop\": 1},",
				"plan={\"columns\": [\"title\"], \"fuzziness\": 1},",
				"plan={\"columns\": [\"title\"], \"where\": \"`price` > 1\", \"prefilter\": False},",
				"lookup={\"name\": \"archive\", \"key\": \"id\", \"vectors\": {\"embedding\": \"cosine\"}},",
				"return [self.bundle_query_0(), self.bundle_query_1(query_vector)]",
				"def bundle_query_1(self, query_vector):")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3",
				"if-version; Lance versions whole tables, not records (a write commits a new table version)");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous", "continuous import; " + EMBEDDED + " and runs no ingestion pipeline");
		cases.put("probe: transaction { delete from items ids [1] }",
				"multi-statement transactions; LanceDB commits each write to one table as its own version (single-table commits are atomic)");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; LanceDB has no generation API (its queries return rows)");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; the reranker classes of the SDKs call model libraries or remote services");
		cases.put("probe: create auth a1 type api_key", "api_key authentication; " + NO_AUTH);
		cases.put("probe: create connection other uri \"db://my-project\"",
				"LanceDB Cloud and Enterprise URIs (db://); the generated programs use the embedded library");
		cases.put("probe: create connection other uri \"lancedb-data\" grpc-port 6334", "tls and grpc-port; " + EMBEDDED);
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + EMBEDDED);
		cases.put("probe: create database prod { connection main path \"lancedb-data\" metrics m1 enabled }",
				"metrics configuration; the embedded library exports no metrics endpoint");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall; LanceDB exposes no query-time recall target (nprobes, ef and refine_factor are effort settings)");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { scan-budget 100 } output [title]",
				"scan-budget; LanceDB bounds a search by nprobes and ef");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { filter-strategy iterative } output [title]",
				"the iterative filter strategy; LanceDB filters before or after the vector search");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; LanceDB explains query plans (explain_plan), not scores");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"min-should-match; LanceDB full-text queries join terms with and or or");
		cases.put("probe: search in items top 3 query text \"a\" text-query query_string fields [title] output [title]",
				"text-query query_string; LanceDB full-text queries are match, phrase, boolean, boost and multi_match (no prefix, wildcard, regexp or query string)");
		cases.put("probe: search in items top 3 query text \"a\" fields [note] output [title]",
				"a keyword search on note, which has no full-text index; LanceDB searches FTS-indexed columns");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; LanceDB has no generation API (its queries return rows)");
		cases.put("probe: alter items { set deletion-protection = true }", "deletion-protection; a LanceDB table is a directory that drop_table removes");
		cases.put("probe: alter items { set ttl = 60 }", "collection ttl; Lance rows do not expire");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; the generator keys rows by it");
		cases.put("probe: alter items { add-field vector-field splade type sparse_float_vector }",
				"sparse vector field splade; the Lance type system has no sparse vector type");
		cases.put("probe: alter items { add-field field props type map }",
				"map field props; the Lance type system has no map type (https://github.com/lance-format/lance/blob/v14.0.2/docs/src/format/table/schema.md)");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }", "bm25 functions; " + NO_FUNCTIONS);
		cases.put("probe: alter items { add-alias alias live for items }", "aliases; LanceDB has no table aliases");
		cases.put("probe: create items if-not-exists with { create vector-index vamanaIndex on embedding family vamana }",
				"vamana indexes; LanceDB builds IVF partitions with flat, PQ, RQ or HNSW sub-indexes");
		cases.put("probe: create items if-not-exists with { create vector-index sqIndex on embedding family ivf { quantization type scalar } }",
				"scalar quantization on an ivf index; the TypeScript SDK 0.41.0 builds no IVF_SQ index (Index.ivfFlat, ivfPq, ivfRq, hnswPq, hnswSq)");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; LanceDB full-text indexes read no dictionaries or synonym files");
		cases.put("probe: refresh items", "collections derived from external sources; a LanceDB table is a Lance dataset in its own directory");
		cases.put("probe: snapshot items location \"s3://bucket/snapshots\"", "a snapshot 'location'; LanceDB tags live in the table directory");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding group-by note sort { sort-by price asc } output [title]",
				"sort with group-by; groups follow the relevance of their best hit");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put(search + "cursor \"1\" output [title]", "the 'cursor' clause" + PENDING + " (keyset pagination after the key of the last hit)");
		cases.put(search + "with-total output [title]", "the 'with-total' clause" + PENDING + " (count_rows of the where string beside the hits)");
		cases.put(search + "mmr lambda 0.5 output [title]", "the 'mmr' clause" + PENDING + " (maximal marginal relevance over the candidates in the runtime)");
		cases.put(search + "aggregate { agg max on price } output [title]", "the 'aggregate' clause" + PENDING + " (aggregation over a scan in the runtime)");
		cases.put(search + "boost { boost-fn kind field_value field price } output [title]", "the 'boost' clause" + PENDING + " (score functions over the hits in the runtime)");
		cases.put("probe: scroll in items limit 2 cursor \"1\" output [title]", "the 'cursor' clause" + PENDING + " (keyset pagination after the key of the last hit)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv", "import format csv" + PENDING + " (the runtimes read JSON and JSON Lines)");
		cases.put("probe: bulk-import into items from \"s3://bucket/items.json\"", "import from s3 URIs" + PENDING + " (the runtimes read local files)");
		cases.put("probe: alter items { add-field field props type json }", "json field props" + PENDING + " (the lance.json Arrow extension type)");
		cases.put("probe: alter items { add-field field place type geo_point }", "geo_point field place" + PENDING + " (GeoArrow types and the R-tree index)");
		return dynamicCases(cases);
	}

	/** Group-by on each search class: the group argument of the runtime call, printed by the real printers. */
	@TestFactory
	public Stream<DynamicTest> generatesGroupedHitsOfEverySearch() {
		List<String> searches = List.of(
				"probe: vector-search in items top 3 query vector $queryVector using embedding group-by note size 2 output [title]",
				"probe: search in items top 3 query text \"a\" fields [title] group-by note size 2 output [title]",
				"probe: hybrid-search in items top 3 fusion rrf group-by note size 2 { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"probe: recommend in items top 3 positive [1] using embedding group-by note size 2 output [title]");
		return searches.stream().map(search -> DynamicTest.dynamicTest(search, () -> {
			Map<String, String> files = generate(BASE + search + "\n");
			String python = files.get(CASES);
			assertFalse(python.contains("rt.unsupported("), python);
			assertTrue(python.contains("group={\"key\": \"note\", \"size\": 2},"), python);
		}));
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"lancedb\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Every sample, retargeted to LanceDB, maps without errors: unsupported constructs become rt.unsupported calls. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ lancedb ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new LancedbGenerator().program(script);
			LancedbManifest manifest = LancedbManifest.load();
			for (LancedbPrinter printer : List.<LancedbPrinter>of(new LancedbPython(), new LancedbTypeScript(), new LancedbJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasALancedbMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(LancedbStatements.class.getDeclaredMethods())
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
			reached(new LancedbStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new LancedbStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(LancedbStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(LancedbStatements.NOT_MAPPED)) {
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
			if (key.startsWith(LancedbGenerator.VENDOR)) {
				files.put(key.substring(LancedbGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
