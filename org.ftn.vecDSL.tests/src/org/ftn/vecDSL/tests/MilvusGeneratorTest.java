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
import org.ftn.vecDSL.generator.milvus.MilvusGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Milvus clients (Python, TypeScript, Java) and the standalone Docker Compose file for
 * samples/milvus_demo.vecDSL into generated/milvus, and a Milvus Lite variant into generated/milvus-lite.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class MilvusGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "milvus_demo.vecDSL");
	private static final String SERVER_CONNECTION = "create connection local uri \"http://localhost:19530\"";
	private static final String LITE_CONNECTION = "create connection local path \"./product_catalog.db\"";
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String RECALL_UNSUPPORTED = "target-recall: Milvus exposes no query-time recall target";

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
		System.clearProperty(MilvusGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguageForStandalone() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "milvus"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml",
				"python/requirements.txt", "typescript/package.json", "java/pom.xml")), files.keySet()::toString);
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
		assertTrue(files.get("docker-compose.yml").contains("milvusdb/milvus:v"));
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(MilvusGenerator.LANGUAGES_PROPERTY, "java");
		Map<String, String> files = generate(sample());

		assertEquals(Set.of("java", "docker-compose.yml"),
				files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void defaultsToTheLiteFileForAnEmbeddedConnection() throws Exception {
		String source = sample();
		assertTrue(source.contains(SERVER_CONNECTION));
		Map<String, String> files = generate(source.replace(SERVER_CONNECTION, LITE_CONNECTION),
				Path.of("generated", "milvus-lite"));

		assertTrue(files.get(PYTHON).contains("\"./product_catalog.db\""));
		assertTrue(files.get("python/requirements-lite.txt").contains("milvus-lite>="));
	}

	@Test
	public void targetRecallIsUnsupportedInPython() throws Exception {
		assertTrue(withTargetRecall().get(PYTHON).contains("rt.unsupported(\"milvus\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void targetRecallIsUnsupportedInTypeScript() throws Exception {
		assertTrue(withTargetRecall().get(TYPESCRIPT).contains("return rt.unsupported(\"milvus\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void targetRecallIsUnsupportedInJava() throws Exception {
		assertTrue(withTargetRecall().get(JAVA).contains("throw unsupported(\"milvus\", \"" + RECALL_UNSUPPORTED));
	}

	private Map<String, String> withTargetRecall() throws Exception {
		return generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector using embedding"
				+ " params { target-recall 0.9 } output [title]\n");
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
			if (key.startsWith(MilvusGenerator.VENDOR)) {
				files.put(key.substring(MilvusGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
