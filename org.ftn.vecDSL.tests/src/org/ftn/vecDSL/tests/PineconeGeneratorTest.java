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
import org.ftn.vecDSL.generator.pinecone.PineconeGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Pinecone clients (Python, TypeScript, Java) and the Docker Compose file of the API mock for
 * samples/pinecone_demo.vecDSL into generated/pinecone, and checks the constructs Pinecone cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class PineconeGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "pinecone_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection cloudProject uri \"https://api.pinecone.io\" api-key env(\"VECDSL_PINECONE_API_KEY\")";
	private static final String RECALL_UNSUPPORTED = "target-recall: Pinecone exposes no query-time recall target";

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
		System.clearProperty(PineconeGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "pinecone"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml",
				"python/requirements.txt", "python/vecdsl_pinecone_runtime.py", "typescript/package.json",
				"typescript/src/vecdslPineconeRuntime.ts", "typescript/tsconfig.json", "java/pom.xml",
				"java/src/main/java/vecdsl/productcatalog/PineconeRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"api_key\": rt.env(\"VECDSL_PINECONE_API_KEY\")"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("export class ProductCatalog {"));
		assertTrue(files.get(TYPESCRIPT).contains("async similar(queryVector: number[], maxPrice: number) {"));
		assertTrue(files.get(JAVA).contains("public final class ProductCatalog implements AutoCloseable {"));
		assertTrue(files.get(JAVA).contains("public List<Map<String, Object>> similar(List<Float> queryVector, double maxPrice) {"));
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: stoplight/prism:"));
		assertTrue(files.get("python/requirements.txt").startsWith("pinecone>="));
		assertTrue(files.get("typescript/package.json").contains("\"@pinecone-database/pinecone\": \"~"));
	}

	@Test
	public void mapsTheSchemaToAPineconeDocumentIndex() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("rt.create_collection("), python);
		assertTrue(python.contains("name=\"products\","), python);
		assertTrue(python.contains("\"embedding\": {\"type\": \"dense_vector\", \"dimension\": 4, \"metric\": \"cosine\"}"), python);
		assertTrue(python.contains("\"title\": {\"type\": \"string\", \"full_text_search\": {\"language\": \"en\"}}"), python);
		assertTrue(python.contains("deployment={\"cloud\": \"aws\", \"region\": \"us-east-1\"}"), python);
		assertFalse(python.contains("\"titleSparse\""), python);
		assertTrue(python.contains("\"namespace\": \"__default__\""), python);
		assertTrue(python.contains("\"key_type\": \"int\""), python);
		assertTrue(python.contains("Ignored on Pinecone: family on VectorIndex, maxDegree on VectorIndex, buildCandidates on VectorIndex."), python);
	}

	@Test
	public void mapsFiltersSearchesAndWrites() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("filter={\"$and\": [{\"category\": {\"$eq\": \"books\"}}, {\"price\": {\"$lte\": max_price}}]}"), python);
		assertTrue(python.contains("score_min=0.2"), python);
		assertTrue(python.contains("{\"kind\": \"keyword\", \"query\": query_text, \"fields\": [\"title\"]}"), python);
		assertTrue(python.contains("fusion=\"rrf\","), python);
		assertTrue(python.contains("k=60,"), python);
		assertTrue(python.contains("filter={\"id\": {\"$nin\": [1, 3]}}"), python);
		assertTrue(python.contains("filter={\"tags\": {\"$eq\": \"database\"}}"), python);
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
		System.setProperty(PineconeGenerator.LANGUAGES_PROPERTY, "java");
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

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"pinecone\", \"" + RECALL_UNSUPPORTED), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"pinecone\", \"" + RECALL_UNSUPPORTED));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"pinecone\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void readConsistencyOtherThanEventualIsUnsupported() throws Exception {
		assertUnsupported(replaced("create collection products {", "create collection products consistency strong {"),
				"consistency strong; Pinecone reads are eventually consistent (use wait on writes)");
	}

	@Test
	public void jsonFieldsAreUnsupported() throws Exception {
		assertUnsupported(replaced("array_len = 8", "array_len = 8\n    field meta type json nullable"),
				"json field meta; Pinecone metadata holds numbers, strings, booleans and string lists");
	}

	@Test
	public void severalDenseVectorFieldsAreUnsupported() throws Exception {
		assertUnsupported(replaced("    vector-field titleSparse", "    vector-field imageEmbedding type float_vector dim 4 metric cosine\n"
				+ "    vector-field titleSparse"), "several dense vector fields; a Pinecone index has one dense vector field");
	}

	@Test
	public void comparisonsOnFullTextFieldsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\ntitled: count in products where title eq \"Espresso machine\"\n",
				"comparisons on full-text field title; Pinecone filters full-text fields by text match only");
	}

	@Test
	public void patternOperatorsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbookish: count in products where category like \"bo%\"\n",
				"operator like; Pinecone filters have no pattern operators");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION + " cloud \"aws\" region \"us-east-1\"", "create connection local path \"./product_catalog.db\""),
				"embedded deployments; Pinecone is a managed service");
	}

	@Test
	public void apiKeysWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced("api-key env(\"VECDSL_PINECONE_API_KEY\")", "api-key \"pcsk-written-in-the-script\""),
				"an API key written in the script; set the environment variable VECDSL_PINECONE_API_KEY");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"pinecone\", \"" + feature + "\")";
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
			if (key.startsWith(PineconeGenerator.VENDOR)) {
				files.put(key.substring(PineconeGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
