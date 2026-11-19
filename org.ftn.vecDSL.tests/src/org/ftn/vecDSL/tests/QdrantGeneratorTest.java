package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.ftn.vecDSL.generator.qdrant.QdrantGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Qdrant clients (Python, TypeScript, Java) and the Docker Compose file of the Qdrant server for
 * samples/qdrant_demo.vecDSL into generated/qdrant, and checks the constructs Qdrant cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class QdrantGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "qdrant_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"http://localhost:6333\" grpc-port 6334";
	private static final String RECALL_UNSUPPORTED = "target-recall: Qdrant exposes no query-time recall target";

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
		System.clearProperty(QdrantGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "qdrant"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml",
				"python/requirements.txt", "python/vecdsl_qdrant_runtime.py", "typescript/package.json",
				"typescript/src/vecdslQdrantRuntime.ts", "typescript/tsconfig.json", "java/pom.xml",
				"java/src/main/java/vecdsl/productcatalog/QdrantRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"api_key\": os.environ.get(\"VECDSL_QDRANT_API_KEY\")"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"grpc_port\": int(os.environ.get(\"VECDSL_QDRANT_GRPC_PORT\", \"6334\"))"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("export class ProductCatalog {"));
		assertTrue(files.get(TYPESCRIPT).contains("async similar(queryVector: number[], maxPrice: number) {"));
		assertTrue(files.get(JAVA).contains("public final class ProductCatalog implements AutoCloseable {"));
		assertTrue(files.get(JAVA).contains("public List<Map<String, Object>> similar(List<Float> queryVector, double maxPrice) {"));
		assertTrue(files.get(JAVA).contains("\"grpc_port\", Integer.parseInt(envOr(\"VECDSL_QDRANT_GRPC_PORT\", \"6334\"))"), files.get(JAVA));
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: qdrant/qdrant:v1.20.0"));
		assertTrue(files.get("docker-compose.yml").contains("qdrant-storage:/qdrant/storage"));
		assertTrue(files.get("python/requirements.txt").startsWith("qdrant-client>=1.20.0,<1.21"));
		assertTrue(files.get("typescript/package.json").contains("\"@qdrant/js-client-rest\": \"~1.20.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>client</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAQdrantCollection() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("rt.create_collection("), python);
		assertTrue(python.contains("vectors={\"embedding\": {\"size\": 4, \"distance\": \"Cosine\", \"hnsw_config\": {\"m\": 16, \"ef_construct\": 200}}},"), python);
		assertTrue(python.contains("{\"field\": \"id\", \"schema\": {\"type\": \"integer\"}},"), python);
		assertTrue(python.contains("\"schema\": {\"type\": \"text\", \"tokenizer\": \"word\", \"phrase_matching\": True, \"scoring\": {\"type\": \"bm25\"}},"), python);
		assertTrue(python.contains("{\"field\": \"category\", \"schema\": {\"type\": \"keyword\"}},"), python);
		assertFalse(python.contains("\"titleSparse\""), python);
		assertTrue(python.contains("\"read_consistency\": \"all\""), python);
		assertTrue(python.contains("\"ordering\": \"strong\","), python);
		assertTrue(python.contains("\"key_type\": \"int\""), python);
		assertTrue(python.contains("\"limits\": {\"title\": {\"max_length\": 256}, \"category\": {\"max_length\": 64}, \"tags\": {\"max_items\": 8}},"), python);
		assertTrue(python.contains("Ignored on Qdrant: storage of titleSparse, which Qdrant computes as BM25 over the text index, language on Analyzer english."), python);
	}

	@Test
	public void mapsFiltersSearchesAndWrites() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("\"must\": [{\"key\": \"category\", \"match\": {\"value\": \"books\"}}, {\"key\": \"price\", \"range\": {\"lte\": max_price}}]"), python);
		assertTrue(python.contains("score_min=0.2,"), python);
		assertTrue(python.contains("{\"kind\": \"vector\", \"field\": \"embedding\", \"vector\": query_vector, \"metric\": \"cosine\"},"), python);
		assertTrue(python.contains("{\"kind\": \"keyword\", \"field\": \"title\", \"query\": query_text},"), python);
		assertTrue(python.contains("fusion=\"rrf\","), python);
		assertTrue(python.contains("k=60,"), python);
		assertTrue(python.contains("filter={\"must\": [{\"key\": \"category\", \"match\": {\"value\": \"books\"}}, {\"key\": \"price\", \"range\": {\"lt\": 40}}]},"), python);
		assertTrue(python.contains("positives=[1],"), python);
		assertTrue(python.contains("negatives=[3],"), python);
		assertTrue(python.contains("filter={\"must\": [{\"key\": \"tags\", \"match\": {\"value\": \"database\"}}]},"), python);
		assertTrue(python.contains("order={\"key\": \"id\"},"), python);
		assertTrue(python.contains("values={\"price\": 35},"), python);
		assertTrue(python.contains("wait=True"), python);
	}

	@Test
	public void keepsPerLegLimitsOfHybridSearch() throws Exception {
		String python = generate(replaced("using embedding }", "using embedding limit 10 }")).get(PYTHON);

		assertTrue(python.contains("\"metric\": \"cosine\", \"limit\": 10}"), python);
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(QdrantGenerator.LANGUAGES_PROPERTY, "java");
		Map<String, String> files = generate(sample());

		assertEquals(Set.of("java", "docker-compose.yml"),
				files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void followsTheLanguageHintOfTheScript() throws Exception {
		String source = sample();
		assertTrue(source.contains("version \"1.0\""));
		Map<String, String> files = generate(source.replace("version \"1.0\"", "version \"1.0\" language typescript"));

		assertEquals(Set.of("typescript", "docker-compose.yml"),
				files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void targetRecallIsUnsupportedInEveryLanguage() throws Exception {
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"qdrant\", \"" + RECALL_UNSUPPORTED), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"qdrant\", \"" + RECALL_UNSUPPORTED));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"qdrant\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void boundedConsistencyIsUnsupported() throws Exception {
		assertUnsupported(replaced("create collection products consistency strong {", "create collection products consistency bounded {"),
				"consistency bounded; Qdrant reads at a replica count (one, majority, quorum or all)");
	}

	@Test
	public void likePatternsWithInnerWildcardsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbookish: count in products where category like \"b%k\"\n",
				"like pattern 'b%k'; Qdrant matches exact values, prefixes and substrings");
	}

	@Test
	public void keywordSearchOnFieldsWithoutTextIndexIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyCategory: search in products top 3 query text $queryText fields [category] output [title]\n",
				"keyword search on category, which is not a full-text field");
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced("create collection products consistency strong {", "create collection products ttl 3600 consistency strong {"),
				"collection ttl; Qdrant points do not expire");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; the Qdrant TypeScript and Java clients connect to a server");
	}

	@Test
	public void apiKeysWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced("grpc-port 6334", "grpc-port 6334 api-key \"qdrant-written-in-the-script\""),
				"an API key written in the script; set the environment variable VECDSL_QDRANT_API_KEY");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"qdrant\", \"" + feature + "\")";
		String python = generate(source).get(PYTHON);
		assertTrue(python.contains(call), python);
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
			if (key.startsWith(QdrantGenerator.VENDOR)) {
				files.put(key.substring(QdrantGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
