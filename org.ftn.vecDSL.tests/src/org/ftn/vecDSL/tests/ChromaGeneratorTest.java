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
import org.ftn.vecDSL.generator.chroma.ChromaGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Chroma clients (Python, TypeScript, Java) and the Docker Compose file of the Chroma server for
 * samples/chroma_demo.vecDSL into generated/chroma, and checks the constructs Chroma cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ChromaGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "chroma_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"http://localhost:8000\"";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String RECALL_UNSUPPORTED = "target-recall: Chroma exposes no query-time recall target";
	private static final String NO_RANKED_TEXT = "the local Chroma server ranks by vector distance only (its executor does not implement the Search API, which ranks BM25 and sparse vectors in Chroma Cloud)";

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
		System.clearProperty(ChromaGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "chroma"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml",
				"python/requirements.txt", "python/vecdsl_chroma_runtime.py", "typescript/package.json",
				"typescript/src/vecdslChromaRuntime.ts", "typescript/tsconfig.json", "java/pom.xml",
				"java/src/main/java/vecdsl/productcatalog/ChromaRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"uri\": os.environ.get(\"VECDSL_CHROMA_URI\", \"http://localhost:8000\"),"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"database\": os.environ.get(\"VECDSL_CHROMA_DATABASE\", \"default_database\"),"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("export class ProductCatalog {"));
		assertTrue(files.get(TYPESCRIPT).contains("async similar(queryVector: number[], maxPrice: number) {"));
		assertTrue(files.get(JAVA).contains("public final class ProductCatalog implements AutoCloseable {"));
		assertTrue(files.get(JAVA).contains("public List<Map<String, Object>> similar(List<Float> queryVector, double maxPrice) {"));
		assertTrue(files.get(JAVA).contains("\"database\", envOr(\"VECDSL_CHROMA_DATABASE\", \"default_database\")"), files.get(JAVA));
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: chromadb/chroma:1.5.9"));
		assertTrue(files.get("docker-compose.yml").contains("chroma-data:/data"));
		assertTrue(files.get("python/requirements.txt").startsWith("chromadb-client>=1.5.9,<1.6"));
		assertTrue(files.get("typescript/package.json").contains("\"chromadb\": \"~3.5.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>gson</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAChromaCollection() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("if not rt.has_collection(self.client, name=\"products\"):"), python);
		assertTrue(python.contains("metric=\"cosine\","), python);
		assertTrue(python.contains("hnsw={\"max_neighbors\": 16, \"ef_construction\": 200},"), python);
		assertTrue(python.contains("\"key_type\": \"int\","), python);
		assertTrue(python.contains("\"vector\": \"embedding\","), python);
		assertTrue(python.contains("\"document\": \"title\","), python);
		assertTrue(python.contains("\"limits\": {\"title\": {\"max_length\": 256}, \"category\": {\"max_length\": 64}, \"tags\": {\"max_items\": 8}},"), python);
		assertFalse(python.contains("Ignored on Chroma"), python);
	}

	@Test
	public void mapsFiltersSearchesAndWrites() throws Exception {
		String python = generate(sample()).get(PYTHON);

		assertTrue(python.contains("filter={\"where\": {\"$and\": [{\"category\": {\"$eq\": \"books\"}}, {\"price\": {\"$lte\": max_price}}]}},"), python);
		assertTrue(python.contains("score_min=0.2,"), python);
		assertTrue(python.contains("filter={\"where_document\": {\"$contains\": \"database\"}},"), python);
		assertTrue(python.contains("filter={\"where\": {\"$and\": [{\"category\": {\"$eq\": \"books\"}}, {\"price\": {\"$lt\": 40}}]}},"), python);
		assertTrue(python.contains("positives=[1],"), python);
		assertTrue(python.contains("negatives=[3],"), python);
		assertTrue(python.contains("filter={\"where\": {\"tags\": {\"$contains\": \"database\"}}},"), python);
		assertTrue(python.contains("values={\"price\": 35},"), python);
		assertTrue(python.contains("return rt.delete_rows("), python);
	}

	@Test
	public void mapsDocumentPredicatesAndNegations() throws Exception {
		String python = generate(sample() + String.join("\n", "",
				"graphTitles: count in products where title like \"Graph%\"",
				"vectorTitles: count in products where title match \"vector database\"",
				"notKitchen: count in products where not (category eq \"kitchen\" or tags contains \"ann\")", "")).get(PYTHON);

		assertTrue(python.contains("filter={\"where_document\": {\"$regex\": \"(?s)^Graph.*$\"}},"), python);
		assertTrue(python.contains("\"where_document\": {\"$or\": [{\"$regex\": \"(?s)(?i)\\\\bvector\\\\b\"}, {\"$regex\": \"(?s)(?i)\\\\bdatabase\\\\b\"}]},"), python);
		assertTrue(python.contains("filter={\"where\": {\"$and\": [{\"category\": {\"$ne\": \"kitchen\"}}, {\"tags\": {\"$not_contains\": \"ann\"}}]}},"), python);
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(ChromaGenerator.LANGUAGES_PROPERTY, "java");
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

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"chroma\", \"" + RECALL_UNSUPPORTED), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"chroma\", \"" + RECALL_UNSUPPORTED));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"chroma\", \"" + RECALL_UNSUPPORTED));
	}

	@Test
	public void keywordSearchIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: search in products top 3 query text \"vector\" fields [title] output [title]\n",
				"keyword search; " + NO_RANKED_TEXT);
	}

	@Test
	public void hybridSearchIsUnsupported() throws Exception {
		assertUnsupported(sample() + String.join("\n", "", "hybrid: hybrid-search in products top 3 fusion rrf fusion-k 60 {",
				"    vector { query vector $queryVector using embedding }", "    keyword { query text \"vector\" fields [title] }",
				"} output [title, price]", ""), "hybrid search; " + NO_RANKED_TEXT);
	}

	@Test
	public void sparseVectorFieldsAreUnsupported() throws Exception {
		assertUnsupported(replaced("    payload-index categoryIndex", "    vector-field titleSparse type sparse_float_vector\n    payload-index categoryIndex"),
				"sparse vector field titleSparse; " + NO_RANKED_TEXT);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"), "collection ttl; Chroma records do not expire");
	}

	@Test
	public void metadataPatternsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbookish: count in products where category like \"b%k\"\n",
				"like on metadata field category; Chroma matches patterns in the document only");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; the Chroma TypeScript and Java clients connect to a server");
	}

	@Test
	public void credentialsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, CONNECTION + " api-key \"written-in-the-script\""),
				"credentials; the open-source Chroma server has no authentication (chroma run allows every request)");
	}

	@Test
	public void snapshotsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nnightly: snapshot products\n",
				"snapshots; Chroma has no snapshot or backup API (a server keeps its data in its persist path)");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"chroma\", \"" + feature + "\")";
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
			if (key.startsWith(ChromaGenerator.VENDOR)) {
				files.put(key.substring(ChromaGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
