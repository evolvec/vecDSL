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
import org.ftn.vecDSL.generator.weaviate.WeaviateGenerator;
import org.ftn.vecDSL.generator.weaviate.WeaviateStatements;
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
 * Full coverage of the vecDSL grammar for Weaviate (generator/weaviate/COVERAGE.md): every native and concealed
 * construct generates in all three host languages, every unsupported construct raises its documented message, and no
 * Method, Concept or AlterOperation class reaches the "not mapped" guard of WeaviateStatements.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class WeaviateCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "weaviate_full.vecDSL");
	private static final String PYTHON = "python/library_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/libraryCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/librarycoverage/LibraryCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ weaviate ]",
			"version \"1.0\"",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://localhost:8080\" grpc-port 50051",
			"createItems: create collection items {",
			"    primary id",
			"    field id type int",
			"    field title type varchar full-text",
			"    field price type float",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw",
			"    }",
			"    payload-index priceIndex on price kind float",
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
		write(files, Path.of("generated", "weaviate-full"));
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"generative={\"module\": \"generative-dummy\", \"config\": {}}",
				"ttl={\"filter_expired\": True, \"seconds\": 86400}",
				"rt.create_alias(self.client, alias=\"Shelf\", collection=\"Books\")",
				"rt.create_property_index(self.client, name=\"Books\", property=\"price\", index=\"rangeFilters\")",
				"multi_tenancy={\"enabled\": True, \"auto_tenant_creation\": True}",
				"rt.create_tenants(self.client, name=\"Notes\", tenants=[\"north\", \"beta\", \"team-a\"])",
				"rt.create_tenants(self.client, name=\"Notes\", tenants=[\"gamma\"])",
				"rt.add_property(self.client, name=\"Books\", property={\"name\": \"pages\", \"data_type\": \"int\"})",
				"rt.update_collection(self.client, name=\"Books\", description=\"Books about databases\")",
				"return rt.import_file(",
				"location=\"file://../books.jsonl\"",
				"return rt.await_index(",
				"rt.rebuild_property_index(self.client, name=\"Books\", property=\"price\", index=\"rangeFilters\")",
				"return rt.flush(self.client, name=\"Books\")",
				"return rt.load_collection(self.client, name=\"Books\", multi_tenant=False)",
				"return rt.sample(",
				"method=\"first\"",
				"return rt.distance_matrix(",
				"single_prompt=\"Summarize {title}\"",
				"grouped_task=\"List the topics\"",
				"return rt.describe(",
				"return rt.list_objects(self.client, kind=\"collections\", prefix=\"Book\")",
				"return rt.list_objects(self.client, kind=\"aliases\", collection=\"Books\")",
				"return rt.list_objects(self.client, kind=\"tenants\", collection=\"Notes\")",
				"\"tenant\": \"north\"",
				"\"tenant\": \"team-a\"",
				"return rt.release_collection(self.client, name=\"Notes\")",
				"return rt.load_collection(self.client, name=\"Notes\", multi_tenant=True)",
				"rt.drop_tenants(self.client, name=\"Notes\", tenants=[\"beta\"])",
				"return rt.truncate(",
				"return rt.delete_limited(",
				"rt.swap_alias(self.client, alias=\"Shelf\", source=\"Books\", target=\"Notes\")",
				"rt.drop_alias(self.client, alias=\"Stacks\")",
				"rt.create_backup(self.client, backup_id=\"nightly\", backend=\"filesystem\", collections=[\"Books\"])",
				"return rt.clone_collection(",
				"target=\"BooksCopy\"",
				"rt.restore_backup(self.client, backup_id=\"nightly\", backend=\"filesystem\", collections=[\"Books\"])")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"generative: { module: \"generative-dummy\", config: {} },",
				"await rt.createPropertyIndex(this.client, { name: \"Books\", property: \"price\", index: \"rangeFilters\" });",
				"await rt.createTenants(this.client, { name: \"Notes\", tenants: [\"north\", \"beta\", \"team-a\"] });",
				"return await rt.loadCollection(this.client, { name: \"Books\", multi_tenant: false });",
				"single_prompt: \"Summarize {title}\",",
				"return await rt.count(this.client, { collection: { name: \"Notes\", key: \"id\", key_property: \"id_key\", tenant: \"north\" } });",
				"await rt.swapAlias(this.client, { alias: \"Shelf\", source: \"Books\", target: \"Notes\" });",
				"return await rt.createBackup(")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"generative\", args(\"module\", \"generative-dummy\", \"config\", args())));",
				"createPropertyIndex(client, args(\"name\", \"Books\", \"property\", \"price\", \"index\", \"rangeFilters\"));",
				"createTenants(client, args(\"name\", \"Notes\", \"tenants\", list(\"north\", \"beta\", \"team-a\")));",
				"\"single_prompt\", \"Summarize {title}\",",
				"args(\"collection\", args(\"name\", \"Notes\", \"key\", \"id\", \"key_property\", \"id_key\", \"tenant\", \"north\")));",
				"swapAlias(client, args(\"alias\", \"Shelf\", \"source\", \"Books\", \"target\", \"Notes\"));",
				"public Map<String, Object> nightly() {",
				"public List<Map<String, Object>> pairs() {",
				"public List<String> collectionNames() {")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("ENABLE_MODULES: backup-filesystem,generative-dummy"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"create embedding-model e1 provider \"openai\" model-id \"text-embedding-3-small\"",
				"vectorized: create collection docs {",
				"    primary id",
				"    field id type int",
				"    field body type text",
				"    vector-field dense type float_vector dim 4 model e1 from [body] {",
				"        vector-index denseIndex family hnsw { quantization type product subvectors 2 }",
				"    }",
				"}",
				"clustered: create cluster c3 { collection inner3 { primary id field id type int vector-field v type float_vector dim 2 } replication factor 2 sharding shards 3 }",
				"secured: create auth a3 type api_key { role r2 \"reader\" privileges [\"read\"] collection items }",
				"widen: alter items { add-field vector-field summary type float_vector dim 4 set ttl = 3600 }",
				"narrow: alter items { drop-field embedding }",
				"wipe: delete from items all",
				"weekly: snapshot items as \"weekly\" incremental location \"s3://bucket/backups\"",
				"later: restore items from \"weekly\" location \"s3://bucket/backups\" async",
				"anyTwo: sample in items size 2 output [title]",
				"")).get(CASES);
		for (String fragment : List.of(
				"\"module\": \"text2vec-openai\"",
				"\"model\": \"text-embedding-3-small\"",
				"\"source_properties\": [\"body\"]",
				"\"quantizer\": {\"type\": \"pq\", \"segments\": 2}",
				"replication={\"factor\": 2}",
				"sharding={\"desired_count\": 3}",
				"role=\"reader\"",
				"\"actions\": [\"read_collections\", \"read_data\"]",
				"rt.add_vector(",
				"ttl={\"filter_expired\": True, \"seconds\": 3600}",
				"rt.drop_vector(self.client, name=\"Items\", vector=\"embedding\")",
				"return rt.truncate(",
				"backend=\"s3\"",
				"bucket=\"bucket\"",
				"path=\"backups\"",
				"incremental=True",
				"wait=False")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: transaction { delete from items ids [1] }",
				"transactions; Weaviate writes each object on its own and has no multi-object transactions");
		cases.put("probe: compact items", "compaction; Weaviate compacts its storage in the background and has no API to start it");
		cases.put("probe: drop-snapshot \"nightly\"",
				"drop-snapshot; Weaviate has no API that deletes a backup (DELETE /backups cancels a running one)");
		cases.put("create embedding-model m1 provider \"openai\"\ncreate embedding-model m2 provider \"cohere\"\nprobe: migrate-embeddings items from-model m1 to-model m2",
				"embedding migration; Weaviate cannot change the vectorizer of a named vector, and a named vector added later is empty for existing objects");
		cases.put("probe: refresh items", "collections derived from external sources; Weaviate stores the objects of every collection itself");
		cases.put("probe: release items",
				"releasing collection items; Weaviate can deactivate tenants but keeps a collection without tenants loaded");
		cases.put("probe: rebuild-index embeddingIndex", "rebuilding a vector index; Weaviate has no API that rebuilds a vector index");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; Weaviate has no copy-on-write collections");
		cases.put("probe: alter items rename to goods",
				"renaming a collection; Weaviate collection names are immutable (an alias serves another name)");
		cases.put("probe: alter items { drop-field price }", "dropping property price; Weaviate cannot remove a property from a collection");
		cases.put("probe: alter items { rename-field title to heading }", "renaming a field; Weaviate cannot rename properties or named vectors");
		cases.put("probe: alter items { set consistency = \"strong\" }",
				"setting consistency; Weaviate sets consistency per request, not on the collection");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; Weaviate collections have no tags");
		cases.put("probe: create collection parts { primary id field id type int vector-field v type float_vector dim 2 partition p1 }",
				"partitions; Weaviate places shards by object id, so they cannot be named, and a request on a multi-tenant collection targets one tenant");
		cases.put("probe: create database prod { connection main uri \"http://localhost:8080\" logical-db analytics { collection inner2 { primary id field id type int } } }",
				"logical databases; Weaviate namespaces need a license key and NAMESPACES_ENABLED, and no client SDK manages them");
		cases.put("probe: create database prod { connection main uri \"http://localhost:8080\" metrics m1 enabled }",
				"metrics configuration; Weaviate exports Prometheus metrics through server environment variables");
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"", "backup schedules; Weaviate creates backups only on request");
		cases.put("probe: create file-resource syn type synonyms path \"/res/syn.txt\"",
				"file resources; Weaviate takes stopword lists and tokenizer dictionaries inline in the collection definition");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Weaviate sets them only through server environment variables");
		cases.put("probe: create collection lim { primary id field id type int collection-limit lim1 max_fields = 64 }",
				"collection-limit max_fields; Weaviate has no per-collection capacity limits");
		cases.put("probe: create auth a1 type mtls", "mtls authentication; Weaviate accepts API keys and OIDC tokens");
		cases.put("probe: create collection mem { primary id field id type int storage type memory }",
				"storage memory; Weaviate persists every collection on disk");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int } sharding shards 2 strategy range }",
				"range sharding; Weaviate shards by a hash of the object id");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Weaviate keeps no object history");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding exact output [title]",
				"the 'exact' clause; Weaviate has no per-query exact search (a flat index is always exhaustive)");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] highlight [title] output [title]",
				"the 'highlight' clause; Weaviate returns no highlights");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"the 'fuzziness' clause; Weaviate BM25 has no fuzzy matching");
		cases.put("probe: scroll in items limit 10 slice 0 of 2 output [title]", "the 'slice' clause; Weaviate has no parallel scroll slices");
		cases.put("probe: update items set { title: \"b\" } ids [1] array-append [title]",
				"array-append and array-remove; Weaviate replaces array properties as a whole");
		cases.put("probe: update items set { title: \"b\" } ids [1] if-version 3",
				"if-version; Weaviate objects carry no version for conditional writes");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", price: 1.0, embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"per-statement ttl; Weaviate sets object ttl on the collection");
		cases.put("probe: bulk-import into items from \"s3://bucket/items.json\"",
				"bulk import from s3; Weaviate has no server-side ingestion, and the runtime reads local files");
		cases.put("probe: bulk-import into items from \"file:///data/items.parquet\" format parquet",
				"parquet bulk import; Weaviate has no server-side ingestion, and the runtime reads JSON and JSON Lines files");
		cases.put("probe: bulk-import into items from \"file:///data/items.json\" continuous",
				"continuous ingestion; Weaviate has no pull-based ingestion");
		cases.put("probe: snapshot items scope tenant", "tenant backups; Weaviate backs up whole collections");
		cases.put("probe: drop embeddingIndex",
				"dropping a vector index; Weaviate removes a vector index only together with the embeddings of its named vector");
		cases.put("probe: list partitions in items",
				"partitions; Weaviate places shards by object id, so they cannot be named, and a request on a multi-tenant collection targets one tenant");
		cases.put("probe: list databases",
				"logical databases; Weaviate namespaces need a license key and NAMESPACES_ENABLED, and no client SDK manages them");
		cases.put("probe: count in items namespace \"team\"",
				"a tenant or namespace on collection items, which declares none; Weaviate enables multi-tenancy only when it creates a collection");
		cases.put("probe: create collection small { primary id field id type int vector-field v type float_vector dim 4 { vector-index vi family hnsw { quantization type scalar bits 4 } } }",
				"scalar quantization with 4 bits; Weaviate SQ uses 8 bits");
		cases.put("probe: create embedding-model e1 provider \"acme\"", "embedding provider 'acme'; Weaviate has no vectorizer module for it");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search on collection items, which has no generative-model; Weaviate generates with the module of the collection");
		cases.put("probe: alter priceIndex rename to x",
				"altering a PayloadIndex; alter operations apply to collections, and Weaviate names are immutable");
		cases.put("probe: drop price", "dropping property price; Weaviate cannot remove a property from a collection");
		cases.put("probe: create collection extra { primary id field id type int } with { create vector-index vi2 family flat }",
				"creating a vector index on an existing collection; Weaviate fixes the index type of a named vector when it creates the vector");
		cases.put("probe: alter items { add-function function emb type text_embedding input [title] output [embedding] }",
				"adding a text-embedding function; Weaviate fixes the vectorizer of a named vector when it creates the vector");
		cases.put("probe: create auth a2 type api_key { role r1 \"writer\" privileges [\"publish\"] }",
				"privilege 'publish'; Weaviate collection and data permissions are all, delete, insert, manage, read, update, write");
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"weaviate\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAWeaviateMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(WeaviateStatements.class.getDeclaredMethods())
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
			reached(new WeaviateStatements(script), create, type, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new WeaviateStatements(script), alter, type, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(WeaviateStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, List<String> unmapped) {
		List<Program.Method> methods;
		try {
			methods = statements.methods(statement, "probe");
		} catch (RuntimeException e) {
			return;
		}
		for (Program.Method method : methods) {
			for (Code.Stmt stmt : method.body()) {
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(WeaviateStatements.NOT_MAPPED)) {
					unmapped.add(type.getName() + ": " + failure.feature());
				}
			}
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
			if (key.startsWith(WeaviateGenerator.VENDOR)) {
				files.put(key.substring(WeaviateGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
