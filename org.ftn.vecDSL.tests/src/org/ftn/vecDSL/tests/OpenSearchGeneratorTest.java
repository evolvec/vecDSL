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
import org.ftn.vecDSL.generator.opensearch.OpenSearchBodies;
import org.ftn.vecDSL.generator.opensearch.OpenSearchGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the OpenSearch clients (Python, TypeScript, Java) and the Docker Compose file of a single-node OpenSearch server
 * for samples/opensearch_demo.vecDSL into generated/opensearch, and checks the constructs OpenSearch cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class OpenSearchGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "opensearch_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"http://127.0.0.1:9201\" username \"admin\" password env(\"VECDSL_OPENSEARCH_PASSWORD\")";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String NO_MODELS = "; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors";
	private static final String INDEX = "vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200";

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
		System.clearProperty(OpenSearchGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "opensearch"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_opensearch_runtime.py", "typescript/package.json", "typescript/src/vecdslOpenSearchRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/OpenSearchRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		for (String fragment : List.of(
				"\"url\": os.environ.get(\"VECDSL_OPENSEARCH_URL\", \"http://127.0.0.1:9201\"),",
				"\"user\": \"admin\",",
				"\"user_variable\": \"VECDSL_OPENSEARCH_USER\",",
				"\"password_variable\": \"VECDSL_OPENSEARCH_PASSWORD\",",
				"\"timeout_seconds\": 30,")) {
			assertTrue(files.get(PYTHON).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"url: process.env.VECDSL_OPENSEARCH_URL ?? \"http://127.0.0.1:9201\",",
				"userVariable: \"VECDSL_OPENSEARCH_USER\",",
				"passwordVariable: \"VECDSL_OPENSEARCH_PASSWORD\",",
				"async similar(queryVector: number[], maxPrice: number) {",
				"filter: [{ bool: { filter: [{ term: { category: \"books\" } }, { range: { price: { lte: maxPrice } } }] } }],",
				"score: { kind: \"cosinesimil\" },",
				"bounds: { min: 0.2 },",
				"score: { kind: \"fused\" },")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"url\", envOr(\"VECDSL_OPENSEARCH_URL\", \"http://127.0.0.1:9201\"),",
				"\"password_variable\", \"VECDSL_OPENSEARCH_PASSWORD\",",
				"public Object similar(List<Float> queryVector, double maxPrice) {",
				"return OpenSearchRuntime.search(",
				"\"filter\", list(args(\"term\", args(\"category\", \"books\")), args(\"range\", args(\"price\", args(\"lte\", maxPrice)))))))))))),",
				"\"bounds\", args(\"min\", 0.2)));",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		String compose = files.get("docker-compose.yml");
		assertTrue(compose.contains("image: opensearchproject/opensearch:3.9.0"));
		assertTrue(compose.contains("OPENSEARCH_INITIAL_ADMIN_PASSWORD: ${VECDSL_OPENSEARCH_PASSWORD:?set VECDSL_OPENSEARCH_PASSWORD}"));
		assertTrue(compose.contains("\"127.0.0.1:9201:9200\""));
		assertTrue(compose.contains("plugins.security.ssl.http.enabled: \"false\""));
		assertTrue(compose.contains("cluster.search.enabled_system_generated_factories: mmr_over_sample_factory,mmr_rerank_factory"));
		assertTrue(compose.contains("path.repo: /usr/share/opensearch/snapshots"));
		assertTrue(compose.contains("opensearch-data:/usr/share/opensearch/data"));
		assertTrue(files.get("python/requirements.txt").startsWith("opensearch-py>=3.2.0,<3.3"));
		assertTrue(files.get("typescript/package.json").contains("\"@opensearch-project/opensearch\": \"~3.10.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>opensearch-java</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAnIndexWithMappings() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"rt.request(self.client, method=\"DELETE\", path=\"/products\", ignore=[404])",
				"if not rt.exists(self.client, path=\"/products\"):",
				"\"number_of_shards\": 1,",
				"\"number_of_replicas\": 0,",
				"\"index.knn\": True,",
				"\"index.knn.derived_source.enabled\": False,",
				"\"filter\": [\"lowercase\", \"english_stop\", \"english_stemmer\"],",
				"\"english_stop\": {\"type\": \"stop\", \"stopwords\": \"_english_\"},",
				"\"english_stemmer\": {\"type\": \"stemmer\", \"language\": \"english\"},",
				"\"dynamic\": \"strict\",",
				"\"title\": {\"type\": \"text\", \"analyzer\": \"english\", \"fields\": {\"raw\": {\"type\": \"keyword\", \"ignore_above\": 256}}},",
				"\"category\": {\"type\": \"keyword\", \"ignore_above\": 64},",
				"\"tags\": {\"type\": \"keyword\"},",
				"\"type\": \"knn_vector\",",
				"\"dimension\": 4,",
				"\"data_type\": \"float\",",
				"\"space_type\": \"cosinesimil\",",
				"\"method\": {\"name\": \"hnsw\", \"engine\": \"faiss\", \"parameters\": {\"m\": 16, \"ef_construction\": 200}},",
				"\"\"\"Ignored on OpenSearch: consistency on VectorCollection.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToRequestBodies() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"return rt.write(",
				"op=\"create\",",
				"counter=\"insert_count\",",
				"\"knn\": {",
				"\"vector\": query_vector,",
				"\"filter\": [{\"bool\": {\"filter\": [{\"term\": {\"category\": \"books\"}}, {\"range\": {\"price\": {\"lte\": max_price}}}]}}],",
				"\"_source\": {\"includes\": [\"id\", \"title\", \"category\", \"price\"], \"excludes\": [\"embedding\"]},",
				"score={\"kind\": \"cosinesimil\"},",
				"bounds={\"min\": 0.2},",
				"\"query\": {\"bool\": {\"must\": [{\"match\": {\"title\": {\"query\": query_text}}}]}},",
				"score={\"kind\": \"relevance\"},",
				"\"match_all\": {}",
				"\"pagination_depth\": 3,",
				"\"phase_results_processors\": [{\"score-ranker-processor\": {\"combination\": {\"technique\": \"rrf\", \"rank_constant\": 60}}}],",
				"score={\"kind\": \"fused\"},",
				"path=\"/products/_count\",",
				"path=\"/products/_update_by_query\",",
				"params={\"refresh\": \"true\", \"conflicts\": \"proceed\"},",
				"\"query\": {\"bool\": {\"filter\": [{\"ids\": {\"values\": [\"1\"]}}]}},",
				"\"set\": {\"price\": 35}, \"remove\": [], \"append\": {}, \"pull\": {}},",
				"wrap=\"update_count\",",
				"return rt.recommend(",
				"\"vector\": None,",
				"\"filter\": {\"bool\": {\"filter\": [{\"bool\": {\"must_not\": [{\"ids\": {\"values\": [\"1\", \"3\"]}}]}}]}},",
				"positive_ids=[1],",
				"negative_ids=[3],",
				"\"sort\": [{\"id\": {\"order\": \"asc\"}}],",
				"\"query\": {\"bool\": {\"filter\": [{\"term\": {\"tags\": \"database\"}}]}},",
				"path=\"/products/_delete_by_query\",",
				"wrap=\"delete_count\",",
				"body={\"query\": {\"match_all\": {}}},")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void sendsHostileValuesAsJsonValues() throws Exception {
		String hostile = "x\"}}, {\"match_all\": {}}, {\"script\": \"ctx._source.clear()\"} \\ end .* ^$ ${x} '; DROP";
		String literal = "\"" + hostile.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
		String parameter = "param queryText : text = \"vector database\"";
		String python = generate(replaced(parameter, parameter + "\nparam hostileText : text = " + literal) + String.join("\n", "",
				"hostileParameter: search in products top 1 query text $hostileText fields [title] output [title]",
				"hostileFilter: count in products where category eq " + literal,
				"hostileWrite: insert into products values [ { id: 9, title: " + literal + ", embedding: [0.1, 0.2, 0.3, 0.4] } ]",
				"hostileScript: update products set { title: " + literal + " } ids [9]",
				"hostilePatch: update products values [ { id: 9, title: " + literal + " } ] ids [9]", "")).get(PYTHON);
		String quoted = Syntax.quote(hostile);

		assertTrue(python.matches("(?s).*\\{\\s*\"category\": " + java.util.regex.Pattern.quote(quoted) + ",?\\s*\\}.*"), python);
		assertTrue(python.contains("\"title\": " + quoted + ","), python);
		assertTrue(python.matches("(?s).*\"set\": \\{\\s*\"title\": " + java.util.regex.Pattern.quote(quoted) + ",?\\s*\\}.*"), python);
		assertTrue(python.contains("values.setdefault(\"hostile_text\", " + quoted + ")"), python);
		assertTrue(python.contains("{\"match\": {\"title\": {\"query\": hostile_text}}}"), python);
		assertTrue(python.contains("op=\"patch\""), python);
		assertEquals(3, python.split(java.util.regex.Pattern.quote("\"title\": " + quoted), -1).length - 1, python);
		assertTrue(python.contains(Syntax.quote(OpenSearchBodies.UPDATE_SCRIPT)), python);
		assertFalse(python.contains("{\"match_all\": {}}, {\"script\""), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(OpenSearchGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: the knn query takes k, ef_search and an oversample factor, effort settings with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"opensearch\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"opensearch\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"opensearch\", \"" + recall));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"),
				"ttl; OpenSearch documents do not expire (Index State Management deletes whole indices, not single documents)");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; OpenSearch runs as a server that the clients reach over HTTP");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local uri \"http://127.0.0.1:9201\" password \"written-in-the-script\""),
				"a secret written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void transactionsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbundle: transaction { delete from products ids [1] }\n",
				"transactions; OpenSearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)");
	}

	@Test
	public void ivfIndexesNeedATrainedModel() throws Exception {
		assertUnsupported(replaced(INDEX, "vector-index embeddingIndex family ivf"),
				"ivf indexes; the Faiss ivf method needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare");
	}

	@Test
	public void productQuantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced(INDEX, INDEX + " { quantization type product subvectors 2 }"),
				"product quantization; the Faiss pq encoder needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare");
	}

	@Test
	public void eightBitScalarQuantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced(INDEX, INDEX + " { quantization type scalar bits 8 }"),
				"scalar quantization to 8.0 bits; the Faiss sq encoder quantizes to 1, 2, 4 or 16 bits and the Lucene sq encoder to 1, 2, 4 or 7 bits");
	}

	@Test
	public void hybridResultClausesGoToTheSharedModule() throws Exception {
		Map<String, String> files = generate(sample() + String.join("\n", "",
				"groupedHybrid: hybrid-search in products top 3 fusion rrf group-by category size 2 {",
				"    vector { query vector $queryVector using embedding } keyword { query text $queryText fields [title] } } output [title]",
				"diverseHybrid: hybrid-search in products top 2 fusion relative_score offset 1 mmr lambda 0.7 candidates 8 {",
				"    vector { query vector $queryVector using embedding } keyword { query text $queryText fields [title] } } output [title]", ""));
		String python = files.get(PYTHON);
		for (String fragment : List.of(
				"    def grouped_hybrid(self, query_vector, query_text):\n        return rt.shape(\n            self.client,\n            candidates=rt.search(",
				"                    \"size\": 30,\n",
				"                    \"_source\": {\"includes\": [\"id\", \"title\", \"category\"]},\n",
				"                \"group\": {\"field\": [\"category\"], \"size\": 2},\n",
				"            vectors=[],\n",
				"    def diverse_hybrid(self, query_vector, query_text):\n        return rt.shape(\n            self.client,\n            candidates=rt.hybrid(",
				"                            \"_source\": {\"includes\": [\"id\", \"title\", \"embedding\"]},\n",
				"                fusion=\"relative_score\",\n                k=60,\n                size=8,\n                offset=0,\n                key=\"id\",\n                candidates=True,\n",
				"                \"offset\": 1,\n                \"limit\": 2,\n",
				"                \"mmr\": {\"lambda\": 0.7, \"candidates\": 8, \"vectorField\": \"embedding\", \"metric\": \"cosine\"},\n",
				"                \"keyField\": \"id\",\n",
				"            vectors=[\"embedding\"],\n")) {
			assertTrue(python.contains(fragment), fragment);
		}
		assertEquals(2, python.split("candidates=True,", -1).length - 1, python);
		for (String fragment : List.of("return await rt.shape(this.client, {", "candidates: await rt.search(this.client, {", "candidates: await rt.hybrid(this.client, {",
				"size: 30,", "size: 8,", "candidates: true,", "group: { field: [\"category\"], size: 2 },", "vectors: [\"embedding\"],")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of("return OpenSearchRuntime.shape(", "\"candidates\", OpenSearchRuntime.search(", "\"candidates\", OpenSearchRuntime.hybrid(",
				"\"size\", 30,", "\"size\", 8,", "\"candidates\", true)),",
				"\"mmr\", args(\"lambda\", 0.7, \"candidates\", 8, \"vectorField\", \"embedding\", \"metric\", \"cosine\"),", "\"vectors\", list(\"embedding\")));")) {
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
		assertTrue(files.get("python/vecdsl_opensearch_runtime.py").contains("import vecdsl_shapes\n"));
		assertTrue(files.get("typescript/src/vecdslOpenSearchRuntime.ts").contains("from \"./vecdslShapes\";"));
		assertTrue(files.get("java/src/main/java/vecdsl/productcatalog/OpenSearchRuntime.java").contains("return Shapes.shape(candidates, spec);"));
	}

	/** A hybrid search without result clauses keeps the request it had before the shared result-shape module. */
	@Test
	public void plainHybridSearchKeepsItsRequest() throws Exception {
		String python = generate(sample()).get(PYTHON);
		String expected = String.join("\n",
				"    def hybrid(self, query_vector, query_text):",
				"        return rt.search(",
				"            self.client,",
				"            index=\"products\",",
				"            body={",
				"                \"query\": {",
				"                    \"hybrid\": {",
				"                        \"queries\": [",
				"                            {\"knn\": {\"embedding\": {\"vector\": query_vector, \"k\": 3, \"filter\": {\"match_all\": {}}}}},",
				"                            {\"bool\": {\"must\": [{\"match\": {\"title\": {\"query\": query_text}}}]}},",
				"                        ],",
				"                        \"pagination_depth\": 3,",
				"                    },",
				"                },",
				"                \"size\": 3,",
				"                \"from\": 0,",
				"                \"search_pipeline\": {",
				"                    \"phase_results_processors\": [{\"score-ranker-processor\": {\"combination\": {\"technique\": \"rrf\", \"rank_constant\": 60}}}],",
				"                },",
				"                \"_source\": {\"includes\": [\"id\", \"title\", \"price\"], \"excludes\": [\"embedding\"]},",
				"            },",
				"            key=\"id\",",
				"            score={\"kind\": \"fused\"},",
				"        )",
				"");
		assertTrue(python.contains(expected), python);
		assertFalse(python.contains("rt.shape("), python);
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"opensearch\", \"" + feature + "\")";
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
			if (key.startsWith(OpenSearchGenerator.VENDOR)) {
				files.put(key.substring(OpenSearchGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
