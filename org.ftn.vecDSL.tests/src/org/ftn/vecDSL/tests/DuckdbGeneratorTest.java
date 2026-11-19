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

import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.generator.VecDSLGenerator;
import org.ftn.vecDSL.generator.duckdb.DuckdbGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the DuckDB clients (Python, TypeScript, Java) and the README of the embedded deployment for
 * samples/duckdb_demo.vecDSL into generated/duckdb, and checks the constructs DuckDB and its vss and fts extensions cannot
 * express. The sample names no targets (VendorId lists no duckdb), so the tests call the DuckDB generator directly.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class DuckdbGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "duckdb_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local path \"vecdsl.duckdb\"";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String NO_MODELS = "; DuckDB has no embedding models in its core extensions, so generated programs supply their own vectors";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private DuckdbGenerator duckdb;

	@Inject
	private VecDSLGenerator delegate;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@AfterEach
	public void clearLanguageSelection() {
		System.clearProperty(DuckdbGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "duckdb"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "README.md", "python/requirements.txt", "python/vecdsl_duckdb_runtime.py",
				"typescript/package.json", "typescript/src/vecdslDuckdbRuntime.ts", "typescript/tsconfig.json", "java/pom.xml",
				"java/src/main/java/vecdsl/productcatalog/DuckdbRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"path\": os.environ.get(\"VECDSL_DUCKDB_PATH\", os.path.join(os.path.dirname(os.path.abspath(__file__)), \"vecdsl.duckdb\")),"),
				files.get(PYTHON));
		for (String fragment : List.of("path: process.env.VECDSL_DUCKDB_PATH ?? resolve(__dirname, \"..\", \"vecdsl.duckdb\"),",
				"readOnly: process.env.VECDSL_DUCKDB_READ_ONLY === \"true\",", "async similar(queryVector: number[], maxPrice: number) {",
				"params: [queryVector, \"books\", maxPrice, 0.2],")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of("\"path\", envOr(\"VECDSL_DUCKDB_PATH\", \"vecdsl.duckdb\"),",
				"\"extension_directory\", System.getenv(\"VECDSL_DUCKDB_EXTENSIONS\")", "public List<Object> similar(List<Float> queryVector, double maxPrice) {",
				"return DuckdbRuntime.query(", "\"params\", list(queryVector, \"books\", maxPrice, 0.2)", "public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("README.md").contains("There is no server to start."));
		assertTrue(files.get("python/requirements.txt").startsWith("duckdb>=2.0.0,<2.1\nduckdb-core-ext-vss==2.0.0\nduckdb-core-ext-fts==2.0.0"));
		assertTrue(files.get("typescript/package.json").contains("\"@duckdb/node-api\": \"2.0.0-r.1\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>duckdb_jdbc</artifactId>"));
	}

	@Test
	public void isRegisteredWithTheGeneratorDelegate() {
		assertTrue(delegate.generators().stream().anyMatch(generator -> DuckdbGenerator.VENDOR.equals(generator.vendor())));
	}

	@Test
	public void mapsTheSchemaToATableWithIndexes() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"{\"sql\": \"DROP SCHEMA IF EXISTS \\\"fts_main_products\\\" CASCADE\"},",
				"{\"sql\": \"DROP TABLE IF EXISTS \\\"main\\\".\\\"products\\\"\"},",
				"\"sql\": \"CREATE TABLE IF NOT EXISTS \\\"main\\\".\\\"products\\\" (\\\"id\\\" INTEGER, \\\"title\\\" VARCHAR CHECK (length(\\\"title\\\") <= 256), \\\"category\\\" VARCHAR CHECK (length(\\\"category\\\") <= 64), \\\"price\\\" FLOAT, \\\"tags\\\" VARCHAR[] CONSTRAINT \\\"tagLimit\\\" CHECK (len(\\\"tags\\\") <= 8), \\\"embedding\\\" FLOAT[4] NOT NULL, PRIMARY KEY (\\\"id\\\"))\",",
				"\"sql\": \"CREATE INDEX IF NOT EXISTS \\\"products_embeddingIndex\\\" ON \\\"main\\\".\\\"products\\\" USING HNSW (\\\"embedding\\\") WITH (metric = 'cosine', M = 16, ef_construction = 200)\",",
				"\"sql\": \"CREATE INDEX IF NOT EXISTS \\\"products_categoryIndex\\\" ON \\\"main\\\".\\\"products\\\" (\\\"category\\\")\",",
				"\"sql\": \"PRAGMA create_fts_index('main.products', 'id', 'title', stemmer = 'english', stopwords = 'english', strip_accents = 0, lower = 1, overwrite = 1)\",",
				"\"\"\"Ignored on DuckDB: consistency on VectorCollection.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToParameterizedSql() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"FROM (SELECT unnest(json_transform($1::JSON, '[{\\\"id\\\":\\\"INTEGER\\\",\\\"title\\\":\\\"VARCHAR\\\",\\\"category\\\":\\\"VARCHAR\\\",\\\"price\\\":\\\"FLOAT\\\",\\\"tags\\\":\\\"VARCHAR[]\\\",\\\"embedding\\\":\\\"FLOAT[4]\\\"}]')) AS r, unnest($1::JSON[]) AS doc) e",
				"counter=\"insert_count\",",
				"refresh=[",
				"\"PRAGMA create_fts_index('main.products', 'id', 'title', stemmer = 'english', stopwords = 'english', strip_accents = 0, lower = 1, overwrite = 1)\",",
				"array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance FROM \\\"main\\\".\\\"products\\\" t WHERE (t.\\\"embedding\\\" IS NOT NULL) AND ((t.\\\"category\\\" = $2::VARCHAR) AND (t.\\\"price\\\" <= $3::FLOAT)) ORDER BY vecdsl_distance LIMIT 3)",
				"FROM hits h WHERE 1 - h.vecdsl_distance >= $4::DOUBLE ORDER BY h.vecdsl_distance, h.\\\"id\\\"\",",
				"params=[query_vector, \"books\", max_price, 0.2],",
				"\\\"fts_main_products\\\".match_bm25(t.\\\"id\\\", $1::VARCHAR, conjunctive := 1) AS vecdsl_score",
				"f AS (SELECT key, sum(1.0 / (60 + rank)) AS score FROM r GROUP BY key)",
				"params=[query_vector, query_text],",
				"JOIN (SELECT unnest(l.keys) AS key, generate_subscripts(l.keys, 1) AS ord FROM (SELECT $1::JSON::INTEGER[] AS keys) l) k",
				"sql=\"SELECT to_json(count(*)) FROM \\\"main\\\".\\\"products\\\" t WHERE (t.\\\"category\\\" = $1::VARCHAR) AND (t.\\\"price\\\" < $2::FLOAT)\",",
				"SET \\\"price\\\" = $1::FLOAT WHERE list_contains($2::JSON::INTEGER[], t.\\\"id\\\")",
				"params=[35, [1]],",
				"lambda z: 2 * z[1] - z[2]",
				"params=[[1], [3], [1, 3]],",
				"WHERE list_contains(t.\\\"tags\\\", $1::VARCHAR) ORDER BY t.\\\"id\\\" LIMIT 10",
				"DELETE FROM \\\"main\\\".\\\"products\\\" AS t WHERE list_contains($1::JSON::INTEGER[], t.\\\"id\\\")",
				"return rt.value(self.client, sql=\"SELECT to_json(count(*)) FROM \\\"main\\\".\\\"products\\\" t\")")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void bindsHostileValuesAsParameters() throws Exception {
		String hostile = "x'; DROP TABLE products; -- \\\\ /* $1 */";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq \"" + hostile + "\"",
				"hostileWrite: insert into products values [ { id: 9, title: \"" + hostile + "\", embedding: [0.1, 0.2, 0.3, 0.4] } ]",
				"hostileSearch: search in products top 3 query text \"" + hostile + "\" fields [title] output [title]",
				"hostileCursor: search in products top 3 query text \"a\" fields [title] cursor \"" + hostile + "\" output [title]",
				"hostileBoost: vector-search in products top 3 query vector [0.1, 0.2, 0.3, 0.4] boost { boost-fn named kind condition weight 2.0 when category eq \""
						+ hostile + "\" } output [title]",
				"hostileImport: bulk-import into products from \"file://" + hostile + ".csv\" format csv", "")).get(PYTHON);

		assertTrue(python.contains("sql=\"SELECT to_json(count(*)) FROM \\\"main\\\".\\\"products\\\" t WHERE t.\\\"category\\\" = $1::VARCHAR\","), python);
		assertTrue(python.contains("params=[\"x'; DROP TABLE products; -- \\\\ /* $1 */\"],"), python);
		assertTrue(python.contains("\"title\": \"x'; DROP TABLE products; -- \\\\ /* $1 */\","), python);
		assertTrue(python.contains("match_bm25(t.\\\"id\\\", $1::VARCHAR, conjunctive := 1)"), python);
		assertTrue(python.contains("params=[\"a\", \"x'; DROP TABLE products; -- \\\\ /* $1 */\"],"), python);
		assertTrue(python.contains("s.key = $2::INTEGER)"), python);
		assertTrue(python.contains("CASE WHEN t.\\\"category\\\" = $2::VARCHAR THEN 2::DOUBLE END"), python);
		assertTrue(python.contains("params=[\"x'; DROP TABLE products; -- \\\\ /* $1 */.csv\"],"), python);
		assertTrue(python.contains("FROM read_csv($1::VARCHAR) f"), python);
		assertFalse(python.contains("= 'x"), python);
		assertFalse(python.contains("read_csv('"), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(DuckdbGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: hnsw_ef_search is an effort setting, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"duckdb\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"duckdb\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"duckdb\", \"" + recall));
	}

	@Test
	public void appliesSearchSettingsForOneStatement() throws Exception {
		String python = generate(sample() + "\nwide: vector-search in products top 3 query vector $queryVector using embedding"
				+ " params { candidates 200 filter-strategy post_filter } output [title]\n").get(PYTHON);

		assertTrue(python.contains("settings={\"hnsw_ef_search\": \"200\", \"hnsw_prefilter\": \"false\"},"), python);
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"),
				"ttl; DuckDB rows do not expire and an embedded database runs no background jobs");
	}

	@Test
	public void phraseSearchIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nexactTitle: search in products top 3 query text \"vector database\" text-query phrase fields [title] output [title]\n",
				"text-query phrase; the fts index stores which terms occur in which field of a document (table terms: termid, docid, fieldid), not their positions, so match_bm25 scores bags of terms and cannot match a phrase");
	}

	@Test
	public void loadIsNotApplicable() throws Exception {
		assertUnsupported(sample() + "\nwarm: load products\n",
				"load and release; DuckDB reads table data on demand through its buffer manager, and an HNSW index is loaded into memory when its table is first accessed after a restart (vss documentation, \\\"Persistence\\\"), so there is nothing to load or release");
	}

	@Test
	public void ifVersionIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nguarded: update products set { price: 1.0 } ids [1] if-version 3\n",
				"if-version; DuckDB rows carry no version number (MVCC versions are internal), and the generator adds no version column");
	}

	@Test
	public void quantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced("vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
				"vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200 { quantization type scalar bits 8 }"),
				"quantization; the vss extension indexes FLOAT[n] arrays only");
	}

	@Test
	public void serverDeploymentsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local uri \"md:catalog\""),
				"the uri md:catalog; the generated programs open a local database file");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"duckdb\", \"" + feature;
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
		duckdb.generate(script, fsa);
		Map<String, String> files = new TreeMap<>();
		fsa.getTextFiles().forEach((key, content) -> {
			if (key.startsWith(DuckdbGenerator.VENDOR)) {
				files.put(key.substring(DuckdbGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
