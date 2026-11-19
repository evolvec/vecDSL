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
import org.ftn.vecDSL.generator.milvus.MilvusGenerator;
import org.ftn.vecDSL.generator.milvus.MilvusJava;
import org.ftn.vecDSL.generator.milvus.MilvusStatements;
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
 * Full coverage of the vecDSL grammar for Milvus (generator/milvus/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented message, and no Method,
 * Concept or AlterOperation class reaches the "not mapped" guard of MilvusStatements.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class MilvusCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "milvus_full.vecDSL");
	private static final String PYTHON = "python/milvus_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/milvusCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/milvuscoverage/MilvusCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the endpoint and the containers of other concepts. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster");

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ milvus ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://localhost:19530\"",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard",
			"    field id type int",
			"    field title type varchar max-length 128 analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field titleSparse type sparse_float_vector",
			"    payload-index priceIndex on price kind float",
			"    function titleBm25 type bm25 input [title] output [titleSparse]",
			"}",
			"createOrders: create collection orders {",
			"    primary id",
			"    field id type int",
			"    field region type varchar max-length 32 partition-key",
			"    vector-field orderVector type float_vector dim 2 metric cosine",
			"    tenant north",
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
		write(files, Path.of("generated", "milvus-full"));
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"return rt.check_analyzer(self.client, analyzer_params={\"tokenizer\": \"standard\"})",
				"return rt.bulk_import(self.client, collection_name=\"books\", files=[[\"vecdsl/books.jsonl\"]], wait=True)",
				"rt.drop_field(self.client, collection_name=\"books\", field_name=\"meta\")",
				"data_type=DataType.FLOAT_VECTOR,",
				"properties={\"dynamicfield.enabled\": \"true\"},",
				"properties={\"consistency_level\": \"Strong\"},",
				"return rt.list_objects(self.client, kind=\"tenants\", only=[\"north\", \"beta\", \"gamma\"], scope=\"notes\")",
				"return rt.delete_matching(self.client, collection_name=\"notes\", primary_key=\"id\", partition_name=\"north\")",
				"return rt.list_objects(self.client, kind=\"snapshots\", scope=\"books\")",
				"return rt.clone_collection(self.client, collection_name=\"books\", target_name=\"booksCopy\")",
				"rt.drop_snapshot(self.client, snapshot_name=\"nightly\", collection_name=\"books\")",
				"return rt.delete_matching(self.client, collection_name=\"notes\", primary_key=\"id\", partition_name=\"team-a\")")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"return await rt.bulkImport(this.client, { collection_name: \"books\", files: [[\"vecdsl/books.jsonl\"]], wait: true });",
				"await rt.dropField(this.client, { collection_name: \"books\", field_name: \"meta\" });",
				"return await rt.sampleRows(this.client, { collection_name: \"books\", primary_key: \"id\", size: 2, first: true, output_fields: [\"title\"] });",
				"return await rt.deleteMatching(this.client, { collection_name: \"notes\", primary_key: \"id\", partition_name: \"north\" });",
				"await rt.createSnapshot(this.client, { collection_name: \"books\", snapshot_name: \"nightly\", description: \"before cleanup\" });",
				"return await rt.cloneCollection(this.client, { collection_name: \"books\", target_name: \"booksCopy\" });",
				"properties: { \"dynamicfield.enabled\": \"true\" }")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"return checkAnalyzer(client, args(\"analyzer_params\", args(\"tokenizer\", \"standard\")));",
				"dropField(client, args(\"collection_name\", \"books\", \"field_name\", \"meta\"));",
				"return cloneCollection(client, args(\"collection_name\", \"books\", \"target_name\", \"booksCopy\"));",
				".dimension(2)",
				"public Map<String, Object> serverImport() {")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("import-files:"));
		for (String fragment : List.of(
				"limit=2, output_fields=[\"id\", \"title\"], offset=1, consistency_level=\"Strong\", )",
				"output_fields=[\"id\", \"title\", \"category\"], group_by_field=\"category\", group_size=1, )",
				"rt.get_rows( self.client, collection_name=\"books\", ids=[2], output_fields=[\"title\"], consistency_level=\"Strong\", )",
				"rt.scroll(self.client, collection_name=\"books\", output_fields=[\"title\"], limit=3, offset=2)",
				"filter=\"category == \\\"kitchen\\\"\", consistency_level=\"Strong\", )")) {
			assertTrue(squash(python).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"limit: 2, output_fields: [\"id\", \"title\"], offset: 1, consistency_level: ConsistencyLevelEnum.Strong, })",
				"group_by_field: \"category\", group_size: 1, })",
				"ids: [2], output_fields: [\"title\"], consistency_level: ConsistencyLevelEnum.Strong, })",
				"rt.scroll(this.client, { collection_name: \"books\", output_fields: [\"title\"], limit: 3, offset: 2 })",
				"filter: \"category == \\\"kitchen\\\"\", consistency_level: ConsistencyLevelEnum.Strong, })")) {
			assertTrue(squash(typescript).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				".limit(2) .outFields(List.of(\"id\", \"title\")) .offset(1) .consistencyLevel(ConsistencyLevel.STRONG) .build());",
				".groupByFieldName(\"category\") .groupSize(1) .build());",
				"\"ids\", List.of(2), \"output_fields\", List.of(\"title\"), \"consistency_level\", ConsistencyLevel.STRONG));",
				"\"output_fields\", List.of(\"title\"), \"limit\", 3, \"offset\", 2)",
				"\"filter\", \"category == \\\"kitchen\\\"\", \"consistency_level\", ConsistencyLevel.STRONG));")) {
			assertTrue(squash(java).contains(fragment), fragment);
		}
	}

	/**
	 * Rows mapped after the full-coverage pass: each clause on its statement class, with the call or arguments the printers emit
	 * in Python, TypeScript and Java (whitespace collapsed). A Java entry that is a message is the reason the Java SDK
	 * cannot send the clause.
	 */
	@TestFactory
	public Stream<DynamicTest> closedRowsPrintTheirCalls() {
		Map<String, List<String>> cases = new TreeMap<>();
		cases.put("probe: get from items ids [1] consistency strong output [title]", List.of(
				"rt.get_rows( self.client, collection_name=\"items\", ids=[1], output_fields=[\"title\"], consistency_level=\"Strong\", )",
				"rt.getRows(this.client, { collection_name: \"items\", ids: [1], output_fields: [\"title\"], consistency_level: ConsistencyLevelEnum.Strong, })",
				"\"ids\", List.of(1), \"output_fields\", List.of(\"title\"), \"consistency_level\", ConsistencyLevel.STRONG));"));
		cases.put("probe: scroll in items limit 3 offset 2 output [title]", List.of(
				"rt.scroll(self.client, collection_name=\"items\", output_fields=[\"title\"], limit=3, offset=2)",
				"rt.scroll(this.client, { collection_name: \"items\", output_fields: [\"title\"], limit: 3, offset: 2 })",
				"args(\"collection_name\", \"items\", \"output_fields\", List.of(\"title\"), \"limit\", 3, \"offset\", 2)"));
		cases.put("probe: scroll in items limit 10 consistency strong output [title]", List.of(
				"output_fields=[\"title\"], limit=10, consistency_level=\"Strong\", )",
				"output_fields: [\"title\"], limit: 10, consistency_level: ConsistencyLevelEnum.Strong, })",
				"throw unsupported(\"milvus\", \"" + MilvusJava.SCROLL_CONSISTENCY + "\");"));
		cases.put("probe: distance-matrix in items using embedding sample 3 consistency strong", List.of(
				"primary_key=\"id\", sample=3, consistency_level=\"Strong\", )",
				"primary_key: \"id\", sample: 3, consistency_level: ConsistencyLevelEnum.Strong, })",
				"\"primary_key\", \"id\", \"sample\", 3, \"consistency_level\", ConsistencyLevel.STRONG));"));
		cases.put("probe: hybrid-search in items top 2 fusion rrf offset 1 group-by title size 2 {\n"
				+ "    vector { query vector $queryVector using embedding }\n"
				+ "    keyword { query text \"vector\" fields [title] }\n"
				+ "} output [title]", List.of(
				"limit=2, output_fields=[\"id\", \"title\"], offset=1, group_by_field=\"title\", group_size=2, )",
				"limit: 2, output_fields: [\"id\", \"title\"], offset: 1, group_by_field: \"title\", group_size: 2, })",
				".limit(2) .outFields(List.of(\"id\", \"title\")) .offset(1) .groupByFieldName(\"title\") .groupSize(2) .build());"));
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey().lines().findFirst().orElseThrow(), () -> {
			Map<String, String> files = generate(BASE + entry.getKey() + "\n");
			String python = files.get(CASES);
			assertFalse(python.contains("rt.unsupported("), python);
			List<String> printed = List.of(squash(python), squash(files.get("typescript/src/coverageCases.ts")),
					squash(files.get("java/src/main/java/vecdsl/coveragecases/CoverageCases.java")));
			for (int i = 0; i < printed.size(); i++) {
				assertTrue(printed.get(i).contains(entry.getValue().get(i)), entry.getValue().get(i) + "\n" + printed.get(i));
			}
		}));
	}

	private static String squash(String code) {
		return code.replaceAll("\\s+", " ");
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"stopwordFile: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"createExtra: create collection extra { primary id field id type int vector-field extraVector type float_vector dim 2 } with {",
				"    create partition gamma if-not-exists",
				"}",
				"dropNorth: drop north",
				"dropSparse: drop titleBm25",
				"dropVector: alter items { drop-field titleSparse }",
				"reindexPrice: rebuild-index priceIndex",
				"importLater: bulk-import into items from \"s3://a-bucket/vecdsl/items.json\" async",
				"restoreLater: restore orders from \"weekly\" async",
				"dropWeekly: drop-snapshot \"weekly\"",
				"")).get(CASES);
		for (String fragment : List.of(
				"rt.add_file_resource(self.client, name=\"stopwordList\", path=\"vecdsl/stopwords.txt\")",
				"if not rt.has_partition(self.client, collection_name=\"extra\", partition_name=\"gamma\"):",
				"rt.delete_rows(self.client, collection_name=\"orders\", filter=\"region == \\\"north\\\"\")",
				"self.client.drop_collection_function(collection_name=\"items\", function_name=\"titleBm25\")",
				"rt.drop_field(self.client, collection_name=\"items\", field_name=\"titleSparse\")",
				"self.client.drop_index(collection_name=\"items\", index_name=\"priceIndex\")",
				"wait=False",
				"rt.drop_snapshot(self.client, snapshot_name=\"weekly\")")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: transaction { delete from items ids [1] }",
				"multi-statement transactions; Milvus has no transaction API (every write commits on its own)");
		cases.put("probe: alter items { rename-field title to heading }", "renaming a field; Milvus adds and drops fields but cannot rename them");
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; Milvus has no collection property that blocks dropping a collection");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; Milvus collections have no tags");
		cases.put("probe: update items set { title: \"b\" } ids [1] if-version 3", "if-version; Milvus writes have no version condition");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", price: 1.0, tags: [\"x\"], embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"per-statement ttl; Milvus expires records by the collection ttl or by a timestamptz ttl field");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\" continuous",
				"continuous import; a Milvus import job reads a fixed set of files");
		cases.put("probe: bulk-import into items from \"kafka://broker/items\"",
				"import from kafka; Milvus imports files from its object storage bucket");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.lance\" format lance",
				"import format lance; Milvus imports JSON, JSON Lines, Parquet, NumPy and CSV files");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\" on-conflict update",
				"the 'on-conflict' clause with a server-side import; Milvus import jobs take no conflict policy");
		cases.put("probe: snapshot items incremental", "incremental snapshots; a Milvus snapshot always records the whole collection");
		cases.put("probe: snapshot items scope full", "full snapshots; Milvus snapshots one collection at a time");
		cases.put("probe: snapshot orders tenant north", "tenant snapshots; Milvus snapshots whole collections");
		cases.put("weekly: snapshot items as \"weekly\"\nprobe: restore items from \"weekly\"",
				"restoring a snapshot into the collection it was taken from; Milvus restores only into a collection that does not exist");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; a Milvus restore copies the segment files");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", "the 'as-of' clause; Milvus snapshots record the current state only");
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"", "backup schedules; Milvus creates snapshots only on request");
		cases.put("probe: create embedding-model e1 provider \"openai\"",
				"embedding models; Milvus has no model objects: providers are configured in milvus.yaml and used through collection functions");
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; Milvus has no generation API (its functions are BM25, text embedding, rerank, MinHash and molecular fingerprints)");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; Milvus has no generation API (its functions are BM25, text embedding, rerank, MinHash and molecular fingerprints)");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Milvus sets them for the whole server in milvus.yaml (rootCoord.maxDatabaseNum, quotaAndLimits.limits.maxCollectionNum)");
		cases.put("probe: create database prod { connection main uri \"http://localhost:19530\" metrics m1 enabled }",
				"metrics configuration; Milvus exports Prometheus metrics on port 9091 and configures them in milvus.yaml");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Milvus reads only the current state of a collection");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding exact output [title]",
				"the 'exact' clause; Milvus has no per-query exact search (a FLAT index is always exhaustive)");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; Milvus returns no score explanation");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"the 'fuzziness' clause; Milvus BM25 has no fuzzy matching");
		cases.put("probe: scroll in items limit 10 slice 0 of 2 output [title]", "the 'slice' clause; Milvus has no parallel scroll slices");
		cases.put("probe: list tenants in orders",
				"listing the tenants or namespaces of a partition-key collection; Milvus does not enumerate partition key values");
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"milvus\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAMilvusMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(MilvusStatements.class.getDeclaredMethods())
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
			reached(new MilvusStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new MilvusStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(MilvusStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(MilvusStatements.NOT_MAPPED)) {
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
			if (key.startsWith(MilvusGenerator.VENDOR)) {
				files.put(key.substring(MilvusGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
