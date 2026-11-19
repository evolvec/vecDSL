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
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchBodies;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Elasticsearch clients (Python, TypeScript, Java) and the Docker Compose file of a single-node Elasticsearch
 * server for samples/elasticsearch_demo.vecDSL into generated/elasticsearch, and checks the constructs Elasticsearch (in its
 * basic license) cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ElasticsearchGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "elasticsearch_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"http://127.0.0.1:9200\" username \"elastic\" password env(\"VECDSL_ELASTICSEARCH_PASSWORD\")";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String NO_MODELS = "; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors";

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
		System.clearProperty(ElasticsearchGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "elasticsearch"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_elasticsearch_runtime.py", "typescript/package.json", "typescript/src/vecdslElasticsearchRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/ElasticsearchRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		for (String fragment : List.of(
				"\"url\": os.environ.get(\"VECDSL_ELASTICSEARCH_URL\", \"http://127.0.0.1:9200\"),",
				"\"user\": \"elastic\",",
				"\"password_variable\": \"VECDSL_ELASTICSEARCH_PASSWORD\",",
				"\"timeout_seconds\": 30,")) {
			assertTrue(files.get(PYTHON).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"url: process.env.VECDSL_ELASTICSEARCH_URL ?? \"http://127.0.0.1:9200\",",
				"passwordVariable: \"VECDSL_ELASTICSEARCH_PASSWORD\",",
				"async similar(queryVector: number[], maxPrice: number) {",
				"filter: [{ bool: { filter: [{ term: { category: \"books\" } }, { range: { price: { lte: maxPrice } } }] } }],",
				"score: { kind: \"cosine\" },",
				"bounds: { min: 0.2 },")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"url\", envOr(\"VECDSL_ELASTICSEARCH_URL\", \"http://127.0.0.1:9200\"),",
				"\"password_variable\", \"VECDSL_ELASTICSEARCH_PASSWORD\",",
				"public Object similar(List<Float> queryVector, double maxPrice) {",
				"return ElasticsearchRuntime.search(",
				"\"filter\", list(args(\"term\", args(\"category\", \"books\")), args(\"range\", args(\"price\", args(\"lte\", maxPrice)))))))),",
				"\"bounds\", args(\"min\", 0.2)));",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		String compose = files.get("docker-compose.yml");
		assertTrue(compose.contains("image: docker.elastic.co/elasticsearch/elasticsearch:9.5.6"));
		assertTrue(compose.contains("ELASTIC_PASSWORD: ${VECDSL_ELASTICSEARCH_PASSWORD:?set VECDSL_ELASTICSEARCH_PASSWORD}"));
		assertTrue(compose.contains("\"127.0.0.1:9200:9200\""));
		assertTrue(compose.contains("xpack.license.self_generated.type: basic"));
		assertTrue(compose.contains("path.repo: /usr/share/elasticsearch/snapshots"));
		assertTrue(compose.contains("elasticsearch-data:/usr/share/elasticsearch/data"));
		assertTrue(files.get("python/requirements.txt").startsWith("elasticsearch>=9.5.1,<9.6"));
		assertTrue(files.get("typescript/package.json").contains("\"@elastic/elasticsearch\": \"~9.5.1\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>elasticsearch-java</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAnIndexWithMappings() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"rt.request(self.client, method=\"DELETE\", path=\"/products\", ignore=[404])",
				"if not rt.exists(self.client, path=\"/products\"):",
				"\"number_of_shards\": 1,",
				"\"number_of_replicas\": 0,",
				"\"filter\": [\"lowercase\", \"english_stop\", \"english_stemmer\"],",
				"\"english_stop\": {\"type\": \"stop\", \"stopwords\": \"_english_\"},",
				"\"english_stemmer\": {\"type\": \"stemmer\", \"language\": \"english\"},",
				"\"dynamic\": \"strict\",",
				"\"title\": {\"type\": \"text\", \"analyzer\": \"english\", \"fields\": {\"raw\": {\"type\": \"keyword\", \"ignore_above\": 256}}},",
				"\"category\": {\"type\": \"keyword\", \"ignore_above\": 64},",
				"\"tags\": {\"type\": \"keyword\"},",
				"\"index_options\": {\"type\": \"hnsw\", \"m\": 16, \"ef_construction\": 200},",
				"\"\"\"Ignored on Elasticsearch: consistency on VectorCollection.\"\"\"")) {
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
				"\"filter\": [{\"bool\": {\"filter\": [{\"term\": {\"category\": \"books\"}}, {\"range\": {\"price\": {\"lte\": max_price}}}]}}],",
				"\"_source\": {\"includes\": [\"id\", \"title\", \"category\", \"price\"], \"exclude_vectors\": True},",
				"score={\"kind\": \"cosine\"},",
				"bounds={\"min\": 0.2},",
				"\"query\": {\"bool\": {\"must\": [{\"match\": {\"title\": {\"query\": query_text}}}]}},",
				"score={\"kind\": \"relevance\"},",
				"return rt.hybrid(",
				"\"knn\": {\"field\": \"embedding\", \"query_vector\": query_vector, \"k\": 3},",
				"fusion=\"rrf\",",
				"k=60,",
				"return rt.fetch(self.client, index=\"products\", values=[1, 2], key=\"id\", fields=[\"title\", \"price\"])",
				"path=\"/products/_count\",",
				"path=\"/products/_update_by_query\",",
				"params={\"refresh\": \"true\", \"conflicts\": \"proceed\"},",
				"\"query\": {\"bool\": {\"filter\": [{\"ids\": {\"values\": [\"1\"]}}]}},",
				"\"set\": {\"price\": 35}, \"remove\": [], \"append\": {}, \"pull\": {}},",
				"wrap=\"update_count\",",
				"return rt.recommend(",
				"\"filter\": [{\"bool\": {\"must_not\": [{\"ids\": {\"values\": [\"1\", \"3\"]}}]}}],",
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
		String hostile = "x\"}}, {\"match_all\": {}}, {\"script\": \"ctx._source.clear()\"} \\ end";
		String literal = "\"" + hostile.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq " + literal,
				"hostileWrite: insert into products values [ { id: 9, title: " + literal + ", embedding: [0.1, 0.2, 0.3, 0.4] } ]",
				"hostileScript: update products set { title: " + literal + " } ids [9]",
				"hostilePatch: update products values [ { id: 9, title: " + literal + " } ] ids [9]", "")).get(PYTHON);
		String quoted = Syntax.quote(hostile);

		assertTrue(python.contains("{\"category\": " + quoted + "}"), python);
		assertTrue(python.contains("\"title\": " + quoted + ","), python);
		assertTrue(python.contains("\"set\": {\"title\": " + quoted + "}"), python);
		assertTrue(python.contains("op=\"patch\""), python);
		assertEquals(3, python.split(java.util.regex.Pattern.quote("\"title\": " + quoted), -1).length - 1, python);
		assertTrue(python.contains(Syntax.quote(ElasticsearchBodies.UPDATE_SCRIPT)), python);
		assertFalse(python.contains("{\"match_all\": {}}, {\"script\""), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(ElasticsearchGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: num_candidates and the oversampling of quantized indexes are effort settings, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"elasticsearch\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"elasticsearch\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"elasticsearch\", \"" + recall));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"),
				"ttl; Elasticsearch documents do not expire (index lifecycle management deletes whole indices after rollover, not single documents)");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; Elasticsearch runs as a server that the clients reach over HTTP");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local uri \"http://127.0.0.1:9200\" password \"written-in-the-script\""),
				"a secret written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void transactionsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbundle: transaction { delete from products ids [1] }\n",
				"transactions; Elasticsearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)");
	}

	@Test
	public void ivfIndexesNeedAnEnterpriseLicense() throws Exception {
		assertUnsupported(replaced("vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200", "vector-index embeddingIndex family ivf"),
				"ivf indexes; the IVF index of Elasticsearch (bbq_disk) needs an enterprise license (DiskBBQPlugin, DISK_BBQ_FEATURE)");
	}

	@Test
	public void productQuantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced("vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
				"vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200 { quantization type product subvectors 2 }"),
				"product quantization; Elasticsearch quantizes with int8, int4 and bbq");
	}

	@Test
	public void groupingInHybridSearchIsMapped() throws Exception {
		// Per-leg requests read the group field, the runtime fusion groups the fused records.
		String python = generate(sample() + "\ngroupedHybrid: hybrid-search in products top 3 fusion rrf group-by category {"
				+ " vector { query vector $queryVector using embedding } keyword { query text $queryText fields [title] } } output [title]\n").get(PYTHON);
		for (String fragment : List.of("def grouped_hybrid(self, query_vector, query_text):", "\"docvalue_fields\": [\"category\"],", "group=\"category\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"elasticsearch\", \"" + feature + "\")";
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
			if (key.startsWith(ElasticsearchGenerator.VENDOR)) {
				files.put(key.substring(ElasticsearchGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
