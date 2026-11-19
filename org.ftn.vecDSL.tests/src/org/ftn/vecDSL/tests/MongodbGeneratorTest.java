package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.ShapeSpec;
import org.ftn.vecDSL.generator.mongodb.MongodbGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the MongoDB clients (Python, TypeScript, Java) and the Docker Compose file of the Atlas Local image (mongod with
 * mongot) for samples/mongodb_demo.vecDSL into generated/mongodb, and checks the constructs MongoDB, the Atlas Local deployment
 * or the Community tier cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class MongodbGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "mongodb_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"mongodb://127.0.0.1:27017/?directConnection=true\" password env(\"VECDSL_MONGODB_PASSWORD\") timeout 30000";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String INDEX = "vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200";
	private static final String NO_MODELS = "; the Atlas Local image runs mongot without the embedding block (Voyage AI API key files) that automated embedding needs, so generated programs supply their own vectors";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private GeneratorDelegate generator;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@AfterEach
	public void clearLanguageSelection() {
		System.clearProperty(MongodbGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "mongodb"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_mongodb_runtime.py", "typescript/package.json", "typescript/src/vecdslMongodbRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/MongodbRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{") && !path.equals("docker-compose.yml"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		for (String fragment : List.of(
				"\"url\": os.environ.get(\"VECDSL_MONGODB_URI\", \"mongodb://127.0.0.1:27017/?directConnection=true\"),",
				"\"user\": None,",
				"\"user_variable\": \"VECDSL_MONGODB_USER\",",
				"\"password_variable\": \"VECDSL_MONGODB_PASSWORD\",",
				"\"timeout_seconds\": 30,")) {
			assertTrue(files.get(PYTHON).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"url: process.env.VECDSL_MONGODB_URI ?? \"mongodb://127.0.0.1:27017/?directConnection=true\",",
				"passwordVariable: \"VECDSL_MONGODB_PASSWORD\",",
				"async similar(queryVector: number[], maxPrice: number) {",
				"filter: { $and: [{ category: { $eq: \"books\" } }, { price: { $lte: maxPrice } }] },",
				"filter: { _id: { $nin: [1, 3] } },")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"url\", envOr(\"VECDSL_MONGODB_URI\", \"mongodb://127.0.0.1:27017/?directConnection=true\"),",
				"\"password_variable\", \"VECDSL_MONGODB_PASSWORD\",",
				"public Object similar(List<Float> queryVector, double maxPrice) {",
				"return MongodbRuntime.aggregate(",
				"\"filter\", args(\"$and\", list(args(\"category\", args(\"$eq\", \"books\")), args(\"price\", args(\"$lte\", maxPrice)))))),",
				"args(\"$match\", args(\"score\", args(\"$gte\", 0.199999999))),",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		String compose = files.get("docker-compose.yml");
		assertTrue(compose.contains("image: mongodb/mongodb-atlas-local:9.0.4"));
		assertTrue(compose.contains("MONGODB_INITDB_ROOT_USERNAME: ${VECDSL_MONGODB_USER:?set VECDSL_MONGODB_USER}"));
		assertTrue(compose.contains("MONGODB_INITDB_ROOT_PASSWORD: ${VECDSL_MONGODB_PASSWORD:?set VECDSL_MONGODB_PASSWORD}"));
		assertTrue(compose.contains("\"127.0.0.1:27017:27017\""));
		assertTrue(compose.contains("mongodb-mongot:/data/mongot"));
		assertTrue(files.get("python/requirements.txt").startsWith("pymongo>=4.20.0,<4.21"));
		assertTrue(files.get("typescript/package.json").contains("\"mongodb\": \"~7.9.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>mongodb-driver-sync</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAValidatedCollectionWithSearchIndexes() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"command={\"drop\": \"products\"},",
				"ignore=[\"NamespaceNotFound\", \"IndexNotFound\"],",
				"if not rt.exists(self.client, database=\"vecdsl\", collection=\"products\"):",
				"\"_id\": {\"bsonType\": [\"int\", \"long\"]},",
				"\"title\": {\"bsonType\": \"string\", \"maxLength\": 256},",
				"\"price\": {\"bsonType\": \"number\"},",
				"\"embedding\": {\"bsonType\": \"array\", \"minItems\": 4, \"maxItems\": 4, \"items\": {\"bsonType\": \"double\"}},",
				"\"additionalProperties\": False,",
				"command={\"createIndexes\": \"products\", \"indexes\": [{\"key\": {\"category\": 1}, \"name\": \"categoryIndex\"}]},",
				"\"type\": \"vectorSearch\",",
				"\"similarity\": \"cosine\",",
				"\"hnswOptions\": {\"maxEdges\": 16, \"numEdgeCandidates\": 200},",
				"{\"type\": \"filter\", \"path\": \"category\"},",
				"{\"type\": \"string\", \"analyzer\": \"lucene.english\"},",
				"\"vecdsl_stamp\": {\"type\": \"token\"},",
				"index=\"embeddingIndex\",",
				"timeout_seconds=60,")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToCommandsAndPipelines() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"return rt.write(",
				"op=\"insert\",",
				"counter=\"insert_count\",",
				"vectors=[\"embedding\"],",
				"{\"index\": \"embeddingIndex\", \"kind\": \"vector\", \"path\": \"embedding\", \"dims\": 4},",
				"\"filter\": {\"$and\": [{\"category\": {\"$eq\": \"books\"}}, {\"price\": {\"$lte\": max_price}}]},",
				"{\"$addFields\": {\"score\": {\"$subtract\": [{\"$multiply\": [2, {\"$meta\": \"vectorSearchScore\"}]}, 1]}}},",
				"{\"$match\": {\"score\": {\"$gte\": 0.199999999}}},",
				"{\"$sort\": {\"score\": -1, \"_id\": 1}},",
				"{\"$project\": {\"_id\": 0, \"id\": \"$_id\", \"score\": 1, \"title\": 1, \"category\": 1, \"price\": 1}},",
				"{\"$search\": {\"index\": \"vecdsl_text\", \"text\": {\"query\": query_text, \"path\": \"title\"}}},",
				"{\"$addFields\": {\"score\": {\"$meta\": \"searchScore\"}}},",
				"\"$rankFusion\": {",
				"{\"$addFields\": {\"score\": {\"$meta\": \"score\"}}},",
				"{\"$addFields\": {\"vecdsl_order\": {\"$indexOfArray\": [{\"$literal\": [1, 2]}, \"$_id\"]}}},",
				"{\"$match\": {\"$and\": [{\"category\": {\"$eq\": \"books\"}}, {\"price\": {\"$lt\": 40}}]}},",
				"select=[\"count\"],",
				"default=0,",
				"return rt.update(",
				"filter={\"_id\": {\"$in\": [1]}},",
				"update={\"$set\": {\"price\": 35}},",
				"return rt.recommend(",
				"\"queryVector\": None,",
				"positive_ids=[1],",
				"negative_ids=[3],",
				"{\"$match\": {\"tags\": {\"$eq\": \"database\"}}},",
				"return rt.delete(",
				"filter={\"_id\": {\"$in\": [4]}},",
				"pipeline=[{\"$match\": {}}, {\"$count\": \"count\"}],")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void keepsHostileValuesAsLiterals() throws Exception {
		String hostile = "x\"}}, {\"$where\": \"sleep(1000)\"}, {\"$gt\": \"\"} \\ $ne";
		String literal = "\"" + hostile.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq " + literal,
				"hostileWrite: insert into products values [ { id: 9, title: " + literal + ", embedding: [0.1, 0.2, 0.3, 0.4] } ]",
				"hostileUpdate: update products set { title: " + literal + " } ids [9]",
				"hostilePrefix: count in products where title prefix " + literal,
				"hostileParameter: count in products where category eq $queryText", "")).get(PYTHON);
		String quoted = Syntax.quote(hostile);

		assertTrue(python.contains("{\"category\": {\"$eq\": " + quoted + "}}"), python);
		assertTrue(python.contains("\"title\": " + quoted + ","), python);
		assertTrue(python.contains("update={\"$set\": {\"title\": " + quoted + "}},"), python);
		assertTrue(python.contains("{\"category\": {\"$eq\": query_text}}"), python);
		assertTrue(python.contains("\"$regex\": \"^x\\\"\\\\}\\\\}, \\\\{"), python);
		assertFalse(python.contains("{\"$where\": \"sleep(1000)\"}, {"), python);
	}

	@Test
	public void runsTransactionsAsOneSession() throws Exception {
		String python = generate(sample() + "\nbundle: transaction isolation snapshot { delete from products ids [1] update products set { price: 30.0 } ids [2] }\n")
				.get(PYTHON);

		for (String fragment : List.of("return rt.transaction(", "\"op\": \"delete\"", "\"op\": \"update\"", "read_concern=\"snapshot\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(MongodbGenerator.LANGUAGES_PROPERTY, "java");
		Map<String, String> files = generate(sample());

		assertEquals(Set.of("java", "docker-compose.yml"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void followsTheLanguageHintOfTheScript() throws Exception {
		String source = sample();
		assertTrue(source.contains("version \"1.0\""));
		Map<String, String> files = generate(source.replace("version \"1.0\"", "version \"1.0\" language typescript"));

		assertEquals(Set.of("typescript", "docker-compose.yml"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void targetRecallIsUnsupportedInEveryLanguage() throws Exception {
		String recall = "target-recall: numCandidates is an effort setting, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"mongodb\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"mongodb\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"mongodb\", \"" + recall));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void regexFiltersInVectorSearchAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nprefixed: vector-search in products top 3 query vector $queryVector using embedding output [title] where title prefix \"Gr\"\n",
				"prefix conditions in a vector search; the $vectorSearch pre-filter accepts $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or, $not and $nor on boolean, date, numeric, string and UUID values");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; MongoDB runs as a server that the drivers reach over the wire protocol");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local uri \"mongodb://127.0.0.1:27017/?directConnection=true\" password \"written-in-the-script\""),
				"a secret written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void snapshotsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbackup: snapshot products as \"nightly\"\n",
				"snapshots; MongoDB backs up with mongodump, mongorestore and file system snapshots, tools outside the database commands, and cloud backups and their schedules are Atlas features");
	}

	@Test
	public void collectionTagsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products consistency strong tags [\"shop\"] {"),
				"collection tags; MongoDB collection options carry a validator, not tags (the description goes into the $jsonSchema)");
	}

	@Test
	public void ivfIndexesAreUnsupported() throws Exception {
		assertUnsupported(replaced(INDEX, "vector-index embeddingIndex family ivf"), "ivf indexes; mongot builds hnsw and flat vector indexes");
	}

	@Test
	public void productQuantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced(INDEX, INDEX + " { quantization type product subvectors 2 }"),
				"product quantization; mongot quantizes with scalar and binary quantization");
	}

	@Test
	public void hybridResultClausesGoToTheSharedModule() throws Exception {
		Map<String, String> files = generate(sample() + String.join("\n", "",
				"boostedNear: vector-search in products top 2 query vector $queryVector using embedding",
				"    boost { boost-fn kind field_value field price weight 0.01 combine sum apply sum } output [title, price]",
				"groupedHybrid: hybrid-search in products top 3 fusion relative_score group-by category size 2 {",
				"    vector { query vector $queryVector using embedding } keyword { query text $queryText fields [title] } } output [title]",
				"diverseSuggestion: recommend in products top 2 positive [1] negative [3] using embedding mmr lambda 0.7 candidates 8 output [title]", ""));
		String python = files.get(PYTHON);
		for (String fragment : List.of(
				"    def boosted_near(self, query_vector):\n        return rt.shape(\n            self.client,\n            candidates=rt.aggregate(",
				"                            \"numCandidates\": 400,\n                            \"limit\": 20,\n",
				"                    {\"$sort\": {\"score\": -1, \"_id\": 1}},\n                    {\"$limit\": 20},\n",
				"                    {\"$project\": {\"_id\": 0, \"id\": \"$_id\", \"score\": 1, \"title\": 1, \"price\": 1}},\n                ],\n                key=\"id\",\n                candidates=True,\n",
				"                            \"kind\": \"field_value\",\n                            \"field\": [\"price\"],\n                            \"weight\": 0.01,\n",
				"    def grouped_hybrid(self, query_vector, query_text):\n        return rt.shape(\n            self.client,\n            candidates=rt.hybrid(",
				"                            {\"$project\": {\"_id\": 0, \"id\": \"$_id\", \"score\": 1, \"title\": 1, \"category\": 1}},\n",
				"                fusion=\"relative_score\",\n                k=60,\n                size=30,\n                offset=0,\n                key=\"id\",\n                candidates=True,\n",
				"                \"group\": {\"field\": [\"category\"], \"size\": 2},\n",
				"    def diverse_suggestion(self):\n        return rt.shape(\n            self.client,\n            candidates=rt.recommend(",
				"                            \"numCandidates\": 160,\n                            \"limit\": 8,\n",
				"                    {\"$project\": {\"_id\": 0, \"id\": \"$_id\", \"score\": 1, \"title\": 1, \"embedding\": 1}},\n",
				"                negative_ids=[3],\n                candidates=True,\n",
				"                \"mmr\": {\"lambda\": 0.7, \"candidates\": 8, \"vectorField\": \"embedding\", \"metric\": \"cosine\"},\n",
				"            vectors=[\"embedding\"],\n")) {
			assertTrue(python.contains(fragment), fragment);
		}
		assertEquals(3, python.split("candidates=True,", -1).length - 1, python);
		for (String fragment : List.of("return await rt.shape(this.client, {", "candidates: await rt.aggregate(this.client, {", "candidates: await rt.hybrid(this.client, {",
				"candidates: await rt.recommend(this.client, {", "size: 30,", "candidates: true,", "vectors: [\"embedding\"],")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of("return MongodbRuntime.shape(", "\"candidates\", MongodbRuntime.aggregate(", "\"candidates\", MongodbRuntime.hybrid(",
				"\"candidates\", MongodbRuntime.recommend(", "\"size\", 30,", "\"vectors\", list(\"embedding\")));")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
	}

	@Test
	public void writesTheSharedResultShapeModuleInEveryLanguage() throws Exception {
		Map<String, String> files = generate(sample());
		assertEquals(ShapeSpec.module("python"), files.get("python/" + ShapeSpec.moduleFile("python")));
		assertEquals(ShapeSpec.module("typescript"), files.get("typescript/src/" + ShapeSpec.moduleFile("typescript")));
		String java = files.get("java/src/main/java/vecdsl/productcatalog/" + ShapeSpec.moduleFile("java"));
		assertEquals(ShapeSpec.module("java").replace("{{java.package}}", "vecdsl.productcatalog"), java);
		assertTrue(java.startsWith("package vecdsl.productcatalog;\n"), java);
		assertTrue(files.get("python/vecdsl_mongodb_runtime.py").contains("import vecdsl_shapes\n"));
		assertTrue(files.get("typescript/src/vecdslMongodbRuntime.ts").contains("from \"./vecdslShapes\";"));
		assertTrue(files.get("java/src/main/java/vecdsl/productcatalog/MongodbRuntime.java").contains("return Shapes.shape(candidates, spec);"));
	}

	/** A hybrid search without result clauses keeps the pipeline it had before the shared result-shape module. */
	@Test
	public void plainHybridSearchKeepsItsPipeline() throws Exception {
		String python = generate(sample()).get(PYTHON);
		String expected = String.join("\n",
				"    def hybrid(self, query_vector, query_text):",
				"        return rt.aggregate(",
				"            self.client,",
				"            database=\"vecdsl\",",
				"            collection=\"products\",",
				"            pipeline=[",
				"                {",
				"                    \"$rankFusion\": {",
				"                        \"input\": {",
				"                            \"pipelines\": {",
				"                                \"leg0\": [",
				"                                    {",
				"                                        \"$vectorSearch\": {",
				"                                            \"index\": \"embeddingIndex\",",
				"                                            \"path\": \"embedding\",",
				"                                            \"queryVector\": query_vector,",
				"                                            \"numCandidates\": 60,",
				"                                            \"limit\": 3,",
				"                                        },",
				"                                    },",
				"                                ],",
				"                                \"leg1\": [",
				"                                    {\"$search\": {\"index\": \"vecdsl_text\", \"text\": {\"query\": query_text, \"path\": \"title\"}}},",
				"                                    {\"$limit\": 3},",
				"                                ],",
				"                            },",
				"                        },",
				"                    },",
				"                },",
				"                {\"$addFields\": {\"score\": {\"$meta\": \"score\"}}},",
				"                {\"$sort\": {\"score\": -1, \"_id\": 1}},",
				"                {\"$limit\": 3},",
				"                {\"$project\": {\"_id\": 0, \"id\": \"$_id\", \"score\": 1, \"title\": 1, \"price\": 1}},",
				"            ],",
				"            key=\"id\",",
				"        )",
				"");
		assertTrue(python.contains(expected), python);
		assertFalse(python.contains("rt.shape("), python);
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"mongodb\", " + Syntax.quote(feature) + ")";
		String python = generate(source).get(PYTHON);
		assertTrue(python.contains(call), call + "\n" + python);
	}

	private static String replaced(String target, String replacement) throws IOException {
		String source = sample();
		assertTrue(source.contains(target), target);
		return source.replace(target, replacement);
	}

	private static String sample() throws IOException {
		return Files.readString(SAMPLE, StandardCharsets.UTF_8);
	}

	private Map<String, String> generate(String source, Path output) throws Exception {
		Map<String, String> files = generate(source);
		for (Map.Entry<String, String> file : files.entrySet()) {
			Path path = output.resolve(file.getKey());
			Files.createDirectories(path.getParent());
			Files.writeString(path, file.getValue(), StandardCharsets.UTF_8);
		}
		return files;
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
