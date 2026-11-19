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
import org.ftn.vecDSL.generator.lancedb.LancedbGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the embedded LanceDB programs (Python, TypeScript, Java) and the README of the embedded deployment for
 * samples/lancedb_demo.vecDSL into generated/lancedb, and checks the constructs LanceDB cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class LancedbGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "lancedb_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local path \"lancedb-data\"";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String EMBEDDED = "the embedded LanceDB library runs inside the program on one machine";
	private static final String NO_AUTH = "an embedded library has no users or authentication (access is the file system's)";
	private static final String RECALL = "target-recall; LanceDB exposes no query-time recall target (nprobes, ef and refine_factor are effort settings)";

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
		System.clearProperty(LancedbGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "lancedb"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "README.md", "python/requirements.txt",
				"python/vecdsl_lancedb_runtime.py", "typescript/package.json", "typescript/src/vecdslLancedbRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/LancedbRuntime.java")), files.keySet()::toString);
		assertFalse(files.containsKey("docker-compose.yml"), "an embedded deployment runs no server");
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("{\"uri\": os.environ.get(\"VECDSL_LANCEDB_URI\", \"lancedb-data\"), \"interval\": 0}"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("export class ProductCatalog {"));
		assertTrue(files.get(TYPESCRIPT).contains("async similar(queryVector: number[], maxPrice: number) {"));
		assertTrue(files.get(TYPESCRIPT).contains("uri: process.env.VECDSL_LANCEDB_URI ?? \"lancedb-data\", interval: 0"), files.get(TYPESCRIPT));
		assertTrue(files.get(JAVA).contains("public final class ProductCatalog implements AutoCloseable {"));
		assertTrue(files.get(JAVA).contains("public List<Map<String, Object>> similar(List<Float> queryVector, double maxPrice) {"));
		assertTrue(files.get(JAVA).contains("\"uri\", envOr(\"VECDSL_LANCEDB_URI\", \"lancedb-data\")"), files.get(JAVA));
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("python/requirements.txt").startsWith("lancedb>=0.41.0,<0.42"));
		assertTrue(files.get("typescript/package.json").contains("\"@lancedb/lancedb\": \"~0.41.0\""));
		assertTrue(files.get("typescript/package.json").contains("\"apache-arrow\": \"~21.2.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>lance-core</artifactId>"));
		assertTrue(files.get("README.md").contains("VECDSL_LANCEDB_URI"));
	}

	@Test
	public void mapsTheSchemaToALanceTable() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"\"\"\"Ignored on LanceDB: consistency on VectorCollection, maxLength on PayloadField, limits on PayloadField.\"\"\"",
				"{\"name\": \"id\", \"type\": \"int32\", \"nullable\": False},",
				"{\"name\": \"tags\", \"type\": \"list\", \"element\": {\"type\": \"utf8\"}},",
				"{\"name\": \"embedding\", \"type\": \"vector\", \"element\": \"float32\", \"dim\": 4, \"nullable\": False},",
				"\"kind\": \"ivf_hnsw_sq\",",
				"\"ef_construction\": 200,",
				"{\"name\": \"categoryIndex\", \"column\": \"category\", \"kind\": \"btree\"},",
				"\"name\": \"title_fts\",",
				"\"with_position\": True,",
				"\"language\": \"English\",",
				"exist_ok=True,",
				"table={\"name\": \"products\", \"key\": \"id\", \"vectors\": {\"embedding\": \"cosine\"}},")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWrites() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"\"where\": rt.sql(\"(`category` = 'books' AND `price` <= {f0})\", {\"f0\": max_price}),",
				"score_min=0.2,",
				"where=\"(`category` = 'books' AND `price` < 40)\",",
				"where=\"`id` IN (1)\",",
				"values={\"price\": \"35\"},",
				"where=\"array_has(`tags`, 'database')\",",
				"{\"column\": \"embedding\", \"metric\": \"cosine\", \"kind\": \"vector\", \"vector\": query_vector},",
				"{\"columns\": [\"title\"], \"kind\": \"keyword\", \"text\": query_text},",
				"fusion=\"rrf\",",
				"positives=[1],",
				"negatives=[3],",
				"return rt.delete(")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void quotesHostileValuesAtGenerationTime() throws Exception {
		String python = generate(sample() + String.join("\n", "",
				"hostile: count in products where title eq \"O'Brien \\\\ x; DROP TABLE products; --\"",
				"pattern: count in products where title like \"50\\\\%\"", "")).get(PYTHON);

		assertTrue(python.contains("where=\"`title` = 'O''Brien \\\\ x; DROP TABLE products; --'\","), python);
		assertTrue(python.contains("where=\"`title` LIKE '50\\\\\\\\%'\","), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(LancedbGenerator.LANGUAGES_PROPERTY, "java");
		Map<String, String> files = generate(sample());

		assertEquals(Set.of("java", "README.md"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void followsTheLanguageHintOfTheScript() throws Exception {
		String source = sample();
		assertTrue(source.contains("version \"1.0\""));
		Map<String, String> files = generate(source.replace("version \"1.0\"", "version \"1.0\" language typescript"));

		assertEquals(Set.of("typescript", "README.md"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void targetRecallIsUnsupportedInEveryLanguage() throws Exception {
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"lancedb\", \"" + RECALL + "\")"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"lancedb\", \"" + RECALL + "\");"));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"lancedb\", \"" + RECALL + "\");"));
	}

	@Test
	public void sparseVectorFieldsAreUnsupported() throws Exception {
		assertUnsupported(replaced("    payload-index categoryIndex", "    vector-field titleSparse type sparse_float_vector\n    payload-index categoryIndex"),
				"sparse vector field titleSparse; the Lance type system has no sparse vector type");
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"), "collection ttl; Lance rows do not expire");
	}

	@Test
	public void serverConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection remote host \"127.0.0.1\" port 8080"),
				"'host' connections; " + EMBEDDED + " (LanceDB Cloud and Enterprise are reached through db:// URIs and an API key)");
	}

	@Test
	public void credentialsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, CONNECTION + " api-key env(\"VECDSL_LANCEDB_KEY\")"), "credentials; " + NO_AUTH);
	}

	@Test
	public void transactionsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbundle: transaction { delete from products ids [2] }\n",
				"multi-statement transactions; LanceDB commits each write to one table as its own version (single-table commits are atomic)");
	}

	@Test
	public void generativeSearchIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nanswer: generate in products top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]\n",
				"generative search; LanceDB has no generation API (its queries return rows)");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"lancedb\", \"" + feature + "\")";
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
			if (key.startsWith(LancedbGenerator.VENDOR)) {
				files.put(key.substring(LancedbGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
