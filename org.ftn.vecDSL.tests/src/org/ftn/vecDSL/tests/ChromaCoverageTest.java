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
import org.ftn.vecDSL.generator.chroma.ChromaGenerator;
import org.ftn.vecDSL.generator.chroma.ChromaJava;
import org.ftn.vecDSL.generator.chroma.ChromaManifest;
import org.ftn.vecDSL.generator.chroma.ChromaPrinter;
import org.ftn.vecDSL.generator.chroma.ChromaPython;
import org.ftn.vecDSL.generator.chroma.ChromaStatements;
import org.ftn.vecDSL.generator.chroma.ChromaTypeScript;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;
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
 * Full coverage of the vecDSL grammar for Chroma (generator/chroma/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented message, no Method, Concept or
 * AlterOperation class reaches the "not mapped" guard of ChromaStatements, and every sample of the other vendors maps
 * without an error other than Unsupported (scripts without targets generate for Chroma too).
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ChromaCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "chroma_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/chroma_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/chromaCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/chromacoverage/ChromaCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/**
	 * Concepts whose bare instance legitimately emits no call: the endpoint, the containers of other concepts, and
	 * authentication of type none (the open-source server has no authentication to configure).
	 */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig");

	private static final String NO_RANKED_TEXT = "the local Chroma server ranks by vector distance only (its executor does not implement the Search API, which ranks BM25 and sparse vectors in Chroma Cloud)";
	private static final String NO_SNAPSHOTS = "snapshots; Chroma has no snapshot or backup API (a server keeps its data in its persist path)";
	private static final String NO_AUTH = "the open-source Chroma server has no authentication (chroma run allows every request)";
	private static final String NO_MODELS = "; generated Chroma programs supply their own vectors (Chroma embedding functions run in the client)";
	private static final String NO_INDEX_CHANGES = "Chroma keeps an HNSW index and an inverted index on every metadata key; the HTTP API changes no index after creation";
	private static final String NO_GENERATION = "Chroma has no generation API (its queries return records)";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ chroma ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://localhost:8000\"",
			"createItems: create collection items {",
			"    primary id",
			"    field id type int",
			"    field title type varchar max-length 128 document-body",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    payload-index priceIndex on price kind float",
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
		write(files, Path.of("generated", "chroma-full"));
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"rt.create_database(self.client, name=\"catalog\")",
				"rt.create_collection(self.client, name=\"notes\", database=\"catalog\", metric=\"l2\")",
				"metadata={\"description\": \"Books about databases\", \"tags\": [\"catalog\"]},",
				"on_conflict=\"ignore\",",
				"rt.alter_collection(self.client, name=\"books\", metadata={\"tags\": [\"catalog\", \"vectors\"]})",
				"rt.alter_collection(self.client, name=\"books\", hnsw={\"ef_search\": 80})",
				"append=[\"tags\"],",
				"remove_items=[\"tags\"],",
				"mode=\"replace\",",
				"remove=[\"tags\"],",
				"slice={\"vecdsl_tenant\": \"north\"},",
				"slice={\"vecdsl_namespace\": \"team-a\"},",
				"return rt.await_index(self.client, name=\"books\")",
				"return rt.describe(self.client, name=\"books\", stats=True, index_progress=True)",
				"query_vector = rt.lookup_vector(",
				"native=True,",
				"order=[{\"key\": \"price\", \"direction\": \"desc\"}],",
				"first=True,",
				"return rt.distance_matrix(",
				"return rt.list_objects(self.client, kind=\"collections\", scope={\"database\": \"catalog\"})",
				"return rt.list_objects(self.client, kind=\"databases\")",
				"return rt.clone_collection(",
				"rt.rename_collection(self.client, name=\"books\", new_name=\"shelf\")",
				"rt.drop_database(self.client, name=\"catalog\")")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"await rt.createDatabase(this.client, { name: \"catalog\" });",
				"await rt.createCollection(this.client, { name: \"notes\", database: \"catalog\", metric: \"l2\" });",
				"await rt.alterCollection(this.client, { name: \"books\", hnsw: { ef_search: 80 } });",
				"slice: { vecdsl_tenant: \"north\" },",
				"return await rt.awaitIndex(this.client, { name: \"books\" });",
				"return await rt.listObjects(this.client, { kind: \"collections\", scope: { database: \"catalog\" } });")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"createDatabase(client, args(\"name\", \"catalog\"));",
				"alterCollection(client, args(\"name\", \"books\", \"hnsw\", args(\"ef_search\", 80)));",
				"\"slice\", args(\"vecdsl_tenant\", \"north\")));",
				"public String vectorsReady() {",
				"public List<Map<String, Object>> pairs() {",
				"public List<String> tenantNames() {",
				"renameCollection(client, args(\"name\", \"books\", \"new_name\", \"shelf\"));",
				"dropDatabase(client, args(\"name\", \"catalog\"));")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("chroma-data:/data"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"createAgain: create items if-not-exists with {",
				"    create vector-index tighterIndex on embedding family hnsw max-degree 32",
				"}",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"tuned: vector-search in items top 3 query vector $queryVector using embedding exclude [note] params { candidates 64 } output *",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"createLedger: create logical-db ledger { collection ledgerItems { primary id field id type int vector-field v type float_vector dim 2 } } if-not-exists",
				"dropLedger: drop ledger if-exists",
				"listPrefixed: list collections prefix \"it\" limit 1",
				"ranged: count in items exact where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"dropNote: drop note",
				"phrased: count in items where title phrase \"vector database\"",
				"regexTitle: count in items where title regex \"^A\"",
				"prefixTitle: count in items where title prefix \"Vec\"",
				"neqTitle: count in items where title neq \"x\"",
				"")).get(CASES);
		for (String fragment : List.of(
				"rt.alter_collection(self.client, name=\"items\", hnsw={\"max_neighbors\": 32})",
				"return [self.both_query_0(), self.both_query_1()]",
				"rows=rt.read_records(self.client, location=\"file://items.json\"),",
				"on_conflict=\"update\",",
				"batch_size=50,",
				"batch_size=20,",
				"\"\"\"Ignored on Chroma: candidatePool on SearchParams.\"\"\"",
				"output=[\"title\", \"price\", \"tags\"],",
				"lookup={",
				"if not rt.has_database(self.client, name=\"ledger\"):",
				"if not rt.has_collection(self.client, name=\"ledgerItems\", database=\"ledger\"):",
				"if rt.has_database(self.client, name=\"ledger\"):",
				"return rt.list_objects(self.client, kind=\"collections\", prefix=\"it\", limit=1)",
				"filter={\"where\": {\"$and\": [{\"price\": {\"$gte\": 1}}, {\"price\": {\"$lte\": 5}}]}},",
				"filter={\"where\": {\"note\": {\"$in\": [\"a\", \"b\"]}}},",
				"filter={\"where\": {\"$or\": [{\"tags\": {\"$contains\": \"x\"}}, {\"tags\": {\"$contains\": \"y\"}}]}},",
				"filter={\"where\": {\"id\": {\"$lte\": 3}}},",
				"slice={\"vecdsl_tenant\": \"east\"},",
				"filter={\"where\": {\"vecdsl_tenant\": {\"$eq\": \"east\"}}},",
				"rt.drop_field(",
				"filter={\"where_document\": {\"$regex\": \"(?s)(?i)\\\\bvector\\\\b\\\\s+\\\\bdatabase\\\\b\"}},",
				"filter={\"where_document\": {\"$regex\": \"(?s)^A\"}},",
				"filter={\"where_document\": {\"$regex\": \"(?s)^Vec\"}},",
				"filter={\"where_document\": {\"$not_regex\": \"(?s)^x$\"}},")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: transaction { delete from items ids [1] }", "multi-statement transactions; Chroma applies each write request on its own");
		cases.put("probe: release items", "releasing a collection; Chroma loads HNSW segments on demand and has no release call");
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3", "if-version; Chroma records carry no version");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", price: 1.0, tags: [\"x\"], embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"per-statement ttl; Chroma records do not expire");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Chroma has no ingestion pipeline (the runtime imports a local file once)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; Chroma has no server-side import of data files (the runtime reads local JSON files)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet", "import format parquet; the runtime reads JSON and JSON Lines files");
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: restore items from \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: drop-snapshot \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; the local Chroma server cannot fork a collection (Chroma Cloud only)");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", "the 'as-of' clause; Chroma reads only the current state of a collection");
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"", "backup schedules; Chroma has no backups");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; " + NO_GENERATION);
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; the local Chroma server has no rerankers");
		cases.put("probe: create analyzer english tokenizer standard", "analyzers; Chroma has no analyzers: the document is indexed by a fixed full-text index");
		cases.put("probe: create auth a1 type api_key", "api_key authentication; " + NO_AUTH);
		cases.put("probe: create connection other uri \"http://localhost:8000\" token \"t\"", "credentials; " + NO_AUTH);
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; " + NO_GENERATION);
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Chroma has no server limits on collections");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; the open-source Chroma server is a single node without replicas or shards");
		cases.put("probe: create database prod { connection main uri \"http://localhost:8000\" metrics m1 enabled }",
				"metrics configuration; Chroma exports OpenTelemetry traces configured in its server config file (open_telemetry)");
		cases.put("probe: create items if-not-exists with { create partition south }", "partitions; a Chroma collection has no partitions or shard keys");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Chroma reads only the current state of a collection");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; Chroma returns no score explanation");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding with-total output [title]",
				"the 'with-total' clause; Chroma returns no total hit count with query results");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding mmr lambda 0.5 output [title]",
				"the 'mmr' clause; Chroma has no maximal marginal relevance");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding group-by price size 1 output [title]",
				"the 'group-by' clause; the local Chroma server has no grouping (the Search API is not implemented by its executor)");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding exact output [title]",
				"the 'exact' clause; Chroma searches its HNSW graph and has no exhaustive search option");
		cases.put("probe: scroll in items limit 10 cursor \"abc\" output [title]", "the 'cursor' clause; Chroma pages by limit and offset, not by a cursor");
		cases.put("probe: scroll in items limit 10 slice 0 of 2 output [title]", "scroll slices; Chroma pages by limit and offset only");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] output [title]", "keyword search; " + NO_RANKED_TEXT);
		cases.put("probe: alter items { set ttl = 60 }", "collection ttl; Chroma records do not expire");
		cases.put("probe: alter items { set deletion-protection = true }", "deletion-protection; Chroma collections have no deletion guard");
		cases.put("probe: alter items { set dynamic = false }", "dynamic false; Chroma metadata is schemaless and accepts any key");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; Chroma record ids are the key text");
		cases.put("probe: alter items { rename-field embedding to vec }", "renaming the embedding or the document; Chroma stores them without a field name");
		cases.put("probe: alter items { add-field vector-field extra type float_vector dim 2 }",
				"adding vector field extra; a Chroma collection holds one embedding per record");
		cases.put("probe: alter items { add-alias alias shelf for items }", "aliases; Chroma has no collection aliases");
		cases.put("probe: drop embeddingIndex", "dropping a vector index; " + NO_INDEX_CHANGES);
		cases.put("probe: drop priceIndex", "dropping a payload index; " + NO_INDEX_CHANGES);
		cases.put("probe: rebuild-index priceIndex", "rebuilding an index; Chroma maintains its indexes with every write and has no rebuild call");
		cases.put("probe: refresh items", "collections derived from external sources; Chroma stores the records of every collection itself");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration; generated Chroma programs embed no data");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"", "file resources; Chroma has no file resource objects");
		cases.put("probe: list aliases", "aliases; Chroma has no collection aliases");
		cases.put("probe: list indexes in items", "listing indexes; Chroma indexes are not named objects");
		cases.put("probe: list snapshots in items", NO_SNAPSHOTS);
		cases.put("probe: count in items where note like \"a%\"", "like on metadata field note; Chroma matches patterns in the document only");
		cases.put("probe: count in items where note is_null", "is_null tests; Chroma metadata filters have no null or existence operator");
		cases.put("probe: count in items where not price gt 10",
				"a negated range on price; Chroma's ranges exclude records without the key, so the negation has no exact form");
		cases.put("probe: count in items where title contains_text \"a\" or price gt 1",
				"a disjunction or negation that mixes the document and metadata; Chroma applies where and where_document as separate conjuncts");
		cases.put("probe: count in items where note match \"a\"", "text operators on metadata field note; Chroma matches text in the document only");
		cases.put("probe: count in items where title fuzzy \"a\"", "text operator fuzzy; Chroma matches substrings and regular expressions");
		cases.put("probe: count in items where tags length gt 1", "length tests; Chroma metadata filters have no length operator");
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"chroma\", \"" + entry.getValue() + "\")";
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
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ chroma ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new ChromaGenerator().program(script);
			ChromaManifest manifest = ChromaManifest.load();
			for (ChromaPrinter printer : List.<ChromaPrinter>of(new ChromaPython(), new ChromaTypeScript(), new ChromaJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAChromaMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(ChromaStatements.class.getDeclaredMethods())
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
			reached(new ChromaStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new ChromaStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(ChromaStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(ChromaStatements.NOT_MAPPED)) {
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
			if (key.startsWith(ChromaGenerator.VENDOR)) {
				files.put(key.substring(ChromaGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
