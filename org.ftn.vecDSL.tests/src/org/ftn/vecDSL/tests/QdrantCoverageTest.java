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
import org.ftn.vecDSL.generator.qdrant.QdrantGenerator;
import org.ftn.vecDSL.generator.qdrant.QdrantStatements;
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
 * Full coverage of the vecDSL grammar for Qdrant (generator/qdrant/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented message, and no Method,
 * Concept or AlterOperation class reaches the "not mapped" guard of QdrantStatements.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class QdrantCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "qdrant_full.vecDSL");
	private static final String PYTHON = "python/qdrant_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/qdrantCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/qdrantcoverage/QdrantCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/**
	 * Concepts whose bare instance legitimately emits no call: the endpoint, the containers of other concepts, analyzers
	 * (Qdrant applies them through the text index of each field that names them) and API key or token authentication (a
	 * client setting: the connection sends the key from the environment).
	 */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "Analyzer", "AnalyzerFilter",
			"AuthConfig");

	private static final String NO_DATABASES = "logical databases; Qdrant keeps all collections of a server in one namespace (it has no database API)";
	private static final String NO_GENERATION = "Qdrant has no generation API (its queries return points)";
	private static final String NO_MODELS = "; a self-hosted Qdrant has no model objects (inference runs in Qdrant Cloud)";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ qdrant ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://localhost:6333\" grpc-port 6334",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard",
			"    field id type int",
			"    field title type varchar max-length 128 analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200 {",
			"            quantization q8 type scalar bits 8",
			"        }",
			"    }",
			"    vector-field titleSparse type sparse_float_vector",
			"    payload-index priceIndex on price kind float",
			"    function titleBm25 type bm25 input [title] output [titleSparse]",
			"}",
			"createOrders: create collection orders {",
			"    primary id",
			"    default-partition north",
			"    field id type int",
			"    field region type varchar max-length 32 partition-key",
			"    vector-field orderVector type float_vector dim 2 metric cosine",
			"    partition north",
			"    tenant east",
			"}",
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
		write(files, Path.of("generated", "qdrant-full"));
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"rt.fill_field(self.client, name=\"books\", field=\"pages\", value=100)",
				"rt.drop_vector(self.client, name=\"books\", field=\"sketch\")",
				"rt.drop_field(self.client, name=\"books\", field=\"note\")",
				"{\"field\": \"vecdsl_tenant\", \"schema\": {\"type\": \"keyword\", \"is_tenant\": True}},",
				"shard_keys=[\"gamma\", \"delta\"],",
				"slice={\"vecdsl_tenant\": \"north\"},",
				"append=[\"tags\"],",
				"remove_items=[\"tags\"],",
				"group_by=\"genre\",",
				"return rt.describe(self.client, name=\"books\", stats=True, index_progress=True)",
				"return rt.list_objects(self.client, kind=\"slices\", key=\"vecdsl_tenant\", scope=\"notes\")",
				"return rt.list_objects(self.client, kind=\"snapshots\", scope=\"books\")",
				"return rt.clone_collection(self.client, name=\"books\", target=\"booksCopy\")",
				"return rt.restore_snapshot(self.client, name=\"archive\", snapshot=\"nightly\", source=\"books\")",
				"return rt.create_snapshot(self.client, name=\"books\", snapshot=\"nightly\")")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"slice: { vecdsl_tenant: \"north\" },",
				"shard_key: \"gamma\",",
				"return await rt.count(this.client, { collection: { name: \"orders\", key: \"id\", key_type: \"int\" }, shard_key: [\"gamma\"] });",
				"return await rt.cloneCollection(this.client, { name: \"books\", target: \"booksCopy\" });",
				"return await rt.restoreSnapshot(this.client, { name: \"archive\", snapshot: \"nightly\", source: \"books\" });",
				"await rt.dropShardKey(this.client, { name: \"orders\", shard_key: \"delta\" });",
				"await rt.renameCollection(this.client, { name: \"notes\", new_name: \"memos\" });")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"slice\", args(\"vecdsl_tenant\", \"north\")));",
				"return listObjects(client, args(\"kind\", \"slices\", \"key\", \"vecdsl_tenant\", \"scope\", \"notes\"));",
				"return describe(client, args(\"name\", \"books\", \"stats\", true, \"index_progress\", true));",
				"return cloneCollection(client, args(\"name\", \"books\", \"target\", \"booksCopy\"));",
				"public List<String> tenantNames() {",
				"dropShardKey(client, args(\"name\", \"orders\", \"shard_key\", \"delta\"));")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("qdrant-snapshots:/qdrant/snapshots"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"createExtra: create items if-not-exists with {",
				"    create vector-index extraIndex on embedding family hnsw max-degree 32",
				"}",
				"dropQuant: drop q8",
				"dropEmbeddingIndex: drop embeddingIndex",
				"dropPrice: drop priceIndex",
				"dropBm25: alter items { drop-function titleBm25 }",
				"renameVector: alter items { rename-field embedding to vec }",
				"createSouth: create orders if-not-exists with {",
				"    create partition south if-not-exists",
				"}",
				"createWest: create orders if-not-exists with {",
				"    create tenant west",
				"}",
				"writeEast: insert into orders values [ { id: 1, region: \"x\", orderVector: [1.0, 0.0] } ] tenant east",
				"deleteEast: delete from orders all tenant east",
				"restoreUrl: restore items from \"weekly\" location \"http://localhost:6333/collections/items/snapshots/weekly.snapshot\" async",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"fullBackup: snapshot items scope full",
				"storageCluster: create cluster c1 {",
				"    collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 }",
				"    replication factor 2",
				"    sharding shards 3",
				"}",
				"addDefault: alter items { add-field field rating type int default 3 }",
				"addFullText: alter items { add-field field blurb type varchar full-text }",
				"listNs: list namespaces in orders",
				"")).get(CASES);
		for (String fragment : List.of(
				"rt.alter_collection(self.client, name=\"items\", vectors={\"embedding\": {\"hnsw_config\": {\"m\": 32}}})",
				"vectors={\"embedding\": {\"quantization_config\": \"Disabled\"}},",
				"vectors={\"embedding\": {\"hnsw_config\": {\"m\": 0, \"payload_m\": 0}}},",
				"rt.drop_payload_index(self.client, name=\"items\", field=\"price\")",
				"schema={\"type\": \"text\", \"tokenizer\": \"word\", \"phrase_matching\": True},",
				"return rt.rename_vector(",
				"if not rt.has_shard_key(self.client, name=\"orders\", shard_key=\"south\"):",
				"schema={\"type\": \"keyword\", \"is_tenant\": True},",
				"slice={\"region\": \"east\"},",
				"shard_key=\"north\",",
				"filter={\"must\": [{\"key\": \"region\", \"match\": {\"value\": \"east\"}}]},",
				"location=\"http://localhost:6333/collections/items/snapshots/weekly.snapshot\",",
				"wait=False,",
				"rows=rt.read_records(self.client, location=\"file://items.json\"),",
				"on_conflict=\"update\",",
				"batch_size=50,",
				"return rt.create_snapshot(self.client, name=\"items\", snapshot=\"fullBackup\", scope=\"full\")",
				"replication_factor=2,",
				"shard_number=3,",
				"rt.fill_field(self.client, name=\"items\", field=\"rating\", value=3)",
				"schema={\"type\": \"text\", \"tokenizer\": \"word\", \"phrase_matching\": True, \"scoring\": {\"type\": \"bm25\"}},",
				"return rt.list_objects(self.client, kind=\"slices\", key=\"region\", scope=\"orders\")")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: transaction { delete from items ids [1] }",
				"multi-statement transactions; Qdrant applies each write on its own (a batch update is ordered, not atomic)");
		cases.put("probe: release items",
				"releasing a collection; Qdrant serves every collection and has no release (memory placement is set per component: cold, cached or pinned)");
		cases.put("probe: update items set { title: \"b\" } ids [1] if-version 3",
				"if-version; Qdrant conditional writes test a payload filter (update_filter), not a point version");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", price: 1.0, tags: [\"x\"], embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"per-statement ttl; Qdrant points do not expire");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Qdrant has no ingestion pipeline (the runtime imports a local file once)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; Qdrant has no server-side import of data files (the runtime reads local JSON files)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet",
				"import format parquet; the runtime reads JSON and JSON Lines files");
		cases.put("probe: snapshot items incremental", "incremental snapshots; a Qdrant snapshot always holds the whole collection");
		cases.put("probe: snapshot items location \"s3://a-bucket/snapshots\"",
				"snapshot locations; Qdrant writes snapshots to its snapshots path or to the S3, GCS or Azure storage configured on the server (storage.snapshots_config)");
		cases.put("probe: snapshot orders tenant east", "tenant snapshots; a Qdrant snapshot holds a whole collection, shard or storage");
		cases.put("weekly: snapshot items as \"weekly\"\nprobe: restore items from \"weekly\" scope full",
				"restoring a storage snapshot; Qdrant recovers storage snapshots only when it starts (--storage-snapshot)");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; a Qdrant recovery copies the segments of the snapshot");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", "the 'as-of' clause; Qdrant snapshots record the current state only");
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"", "backup schedules; Qdrant creates snapshots only on request");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; " + NO_GENERATION);
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers" + NO_MODELS);
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; " + NO_GENERATION);
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Qdrant sets max_collections for the whole server in config.yaml (storage.max_collections)");
		cases.put("probe: create database prod { connection main uri \"http://localhost:6333\" metrics m1 enabled }",
				"metrics configuration; Qdrant always serves Prometheus metrics at /metrics and configures telemetry in config.yaml");
		cases.put("probe: create database prod { connection main uri \"http://localhost:6333\" logical-db analytics { } }", NO_DATABASES);
		cases.put("probe: list databases", NO_DATABASES);
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Qdrant reads only the current state of a collection");
		cases.put("probe: get from items ids [1] as-of \"2026-01-01\" output [title]", "the 'as-of' clause; Qdrant reads only the current state of a collection");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; Qdrant returns no score explanation");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding with-total output [title]",
				"the 'with-total' clause; Qdrant returns no total hit count with query results");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"the 'fuzziness' clause; Qdrant text matching has no fuzzy terms");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] mmr lambda 0.5 output [title]",
				"the 'mmr' clause; Qdrant applies MMR to nearest-vector queries only");
		cases.put("probe: scroll in items limit 10 cursor \"abc\" output [title]",
				"the 'cursor' clause; Qdrant pages queries by offset and scrolls by point id, not by an opaque cursor");
		cases.put("probe: alter items { set ttl = 60 }", "collection ttl; Qdrant points do not expire");
		cases.put("probe: alter items { set consistency = \"strong\" }",
				"consistency as a collection property; Qdrant takes read consistency per request (the generator sends the level declared on the collection)");
		cases.put("probe: alter items { set deletion-protection = true }", "deletion-protection; Qdrant collections have no deletion guard");
		cases.put("probe: alter items { set dynamic = false }", "dynamic false; Qdrant payloads are schemaless and accept any key");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; Qdrant derives point ids from it");
		cases.put("probe: rebuild-index embeddingIndex",
				"rebuilding a vector index with unchanged parameters; Qdrant rebuilds HNSW when its parameters change (alter the index instead)");
		cases.put("probe: refresh items", "collections derived from external sources; Qdrant stores the points of every collection itself");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration; a self-hosted Qdrant embeds no data (inference runs in Qdrant Cloud)");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; Qdrant has no file resource objects (stop word sets are written inline in a text index)");
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"qdrant\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAQdrantMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(QdrantStatements.class.getDeclaredMethods())
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
			reached(new QdrantStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new QdrantStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(QdrantStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(QdrantStatements.NOT_MAPPED)) {
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
			if (key.startsWith(QdrantGenerator.VENDOR)) {
				files.put(key.substring(QdrantGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
