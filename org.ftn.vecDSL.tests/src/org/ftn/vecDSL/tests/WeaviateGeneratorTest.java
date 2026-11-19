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
import org.ftn.vecDSL.generator.weaviate.WeaviateGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Weaviate clients (Python, TypeScript, Java) and the Docker Compose file for
 * samples/weaviate_demo.vecDSL into generated/weaviate, and checks the constructs Weaviate cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class WeaviateGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "weaviate_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String RECALL_UNSUPPORTED = "target-recall: Weaviate exposes no query-time recall target";

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
		System.clearProperty(WeaviateGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "weaviate"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml",
				"python/requirements.txt", "python/vecdsl_weaviate_runtime.py", "typescript/package.json",
				"typescript/src/vecdslWeaviateRuntime.ts", "java/pom.xml",
				"java/src/main/java/vecdsl/productcatalog/WeaviateRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(TYPESCRIPT).contains("export class ProductCatalog {"));
		assertTrue(files.get(TYPESCRIPT).contains("async similar(queryVector: number[], maxPrice: number) {"));
		assertTrue(files.get(JAVA).contains("public final class ProductCatalog implements AutoCloseable {"));
		assertTrue(files.get(JAVA).contains("public List<Map<String, Object>> similar(List<Float> queryVector, double maxPrice) {"));
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("cr.weaviate.io/semitechnologies/weaviate:"));
		assertTrue(files.get("python/requirements.txt").startsWith("weaviate-client>="));
	}

	@Test
	public void mapsTheSchemaToWeaviateConcepts() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("rt.create_collection("), python);
		assertTrue(python.contains("name=\"Products\""), python);
		assertTrue(python.contains("{\"name\": \"id_key\", \"data_type\": \"int\"}"), python);
		assertTrue(python.contains("\"tokenization\": \"field\", \"index_filterable\": True"), python);
		assertTrue(python.contains("inverted_index={\"stopwords_preset\": \"en\"}"), python);
		assertFalse(python.contains("titleSparse"), python);
		assertTrue(python.contains("max_distance=0.8"), python);
		assertTrue(python.contains("fusion=\"ranked\""), python);
		assertTrue(python.contains("exclude=[1, 3]"), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(WeaviateGenerator.LANGUAGES_PROPERTY, "java");
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

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"weaviate\", \"" + RECALL_UNSUPPORTED), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"weaviate\", \"" + RECALL_UNSUPPORTED));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"weaviate\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void hybridLegLimitsAreUnsupported() throws Exception {
		assertUnsupported(replaced("using embedding }", "using embedding limit 10 }"),
				"per-leg limits; Weaviate hybrid search takes one limit");
	}

	@Test
	public void fusionConstantsOtherThanSixtyAreUnsupported() throws Exception {
		assertUnsupported(replaced("fusion-k 60", "fusion-k 30"), "fusion-k 30; Weaviate ranked fusion uses k = 60");
	}

	@Test
	public void jsonFieldsWithoutSubFieldsAreUnsupported() throws Exception {
		assertUnsupported(replaced("array_len = 8", "array_len = 8\n    field meta type json nullable"),
				"json field meta without sub-fields; Weaviate object properties declare their nested properties");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced("create connection local uri \"http://localhost:8080\" grpc-port 50051",
				"create connection local path \"./product_catalog.db\""),
				"embedded deployments; the generated programs connect to a Weaviate server");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"weaviate\", \"" + feature + "\")";
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
			if (key.startsWith(WeaviateGenerator.VENDOR)) {
				files.put(key.substring(WeaviateGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
