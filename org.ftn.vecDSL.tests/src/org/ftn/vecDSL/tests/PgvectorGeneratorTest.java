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
import org.ftn.vecDSL.generator.pgvector.PgvectorGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the pgvector clients (Python, TypeScript, Java) and the Docker Compose file of PostgreSQL with pgvector for
 * samples/pgvector_demo.vecDSL into generated/pgvector, and checks the constructs PostgreSQL and pgvector cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class PgvectorGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "pgvector_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"postgresql://postgres@127.0.0.1:5432/vecdsl\"";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String NO_MODELS = "; PostgreSQL has no embedding models (the pgvector image includes no model extension such as pgai), so generated programs supply their own vectors";

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
		System.clearProperty(PgvectorGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "pgvector"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_pgvector_runtime.py", "typescript/package.json", "typescript/src/vecdslPgvectorRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/PgvectorRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"url\": os.environ.get(\"VECDSL_PGVECTOR_URL\", \"postgresql://postgres@127.0.0.1:5432/vecdsl\"),"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"password_variable\": \"VECDSL_PGVECTOR_PASSWORD\","), files.get(PYTHON));
		for (String fragment : List.of(
				"url: process.env.VECDSL_PGVECTOR_URL ?? \"postgresql://postgres@127.0.0.1:5432/vecdsl\"",
				"passwordVariable: \"VECDSL_PGVECTOR_PASSWORD\"",
				"async similar(queryVector: number[], maxPrice: number) {",
				"settings: { \"hnsw.ef_search\": \"40\", \"hnsw.iterative_scan\": \"strict_order\" },",
				"params: [queryVector, \"books\", maxPrice, 0.2],")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"url\", envOr(\"VECDSL_PGVECTOR_URL\", \"postgresql://postgres@127.0.0.1:5432/vecdsl\"),",
				"\"password_variable\", \"VECDSL_PGVECTOR_PASSWORD\"",
				"public List<Object> similar(List<Float> queryVector, double maxPrice) {",
				"return PgvectorRuntime.query(",
				"\"params\", list(queryVector, \"books\", maxPrice, 0.2),",
				"\"settings\", args(\"hnsw.ef_search\", \"40\", \"hnsw.iterative_scan\", \"strict_order\")));",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: pgvector/pgvector:0.8.7-pg18-trixie"));
		assertTrue(files.get("docker-compose.yml").contains("POSTGRES_PASSWORD: ${VECDSL_PGVECTOR_PASSWORD:?set VECDSL_PGVECTOR_PASSWORD}"));
		assertTrue(files.get("docker-compose.yml").contains("pgvector-data:/var/lib/postgresql"));
		assertTrue(files.get("python/requirements.txt").startsWith("psycopg[binary]>=3.3.8,<3.4"));
		assertTrue(files.get("typescript/package.json").contains("\"pg\": \"~8.23.1\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>postgresql</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToATableWithIndexes() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"steps=[{\"sql\": \"DROP TABLE IF EXISTS \\\"products\\\"\"}]",
				"steps=[{\"sql\": \"CREATE TEXT SEARCH CONFIGURATION public.\\\"english\\\" (COPY = pg_catalog.english)\"}],",
				"\"sql\": \"CREATE TABLE IF NOT EXISTS \\\"products\\\" (\\\"id\\\" integer, \\\"title\\\" varchar(256), \\\"category\\\" varchar(64), \\\"price\\\" real, \\\"tags\\\" text[] CONSTRAINT \\\"tagLimit\\\" CHECK (cardinality(\\\"tags\\\") <= 8), \\\"embedding\\\" vector(4) NOT NULL, PRIMARY KEY (\\\"id\\\"))\",",
				"\"sql\": \"CREATE INDEX IF NOT EXISTS \\\"products_embeddingIndex\\\" ON \\\"products\\\" USING hnsw (\\\"embedding\\\" vector_cosine_ops) WITH (m = 16, ef_construction = 200)\",",
				"\"sql\": \"CREATE INDEX IF NOT EXISTS \\\"products_categoryIndex\\\" ON \\\"products\\\" USING btree (\\\"category\\\")\",",
				"USING gin ((to_tsvector('public.\\\"english\\\"'::regconfig, coalesce(\\\"title\\\", ''))))",
				"\"\"\"Ignored on pgvector: consistency on VectorCollection.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToParameterizedSql() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"jsonb_array_elements($1::jsonb) AS e(doc) CROSS JOIN LATERAL jsonb_to_record(e.doc)",
				"AND ((t.\\\"category\\\" = $2::text) AND (t.\\\"price\\\" <= $3::real)) ORDER BY distance LIMIT 3)",
				"FROM hits h WHERE 1 - h.distance >= $4::double precision ORDER BY h.distance + 0, h.\\\"id\\\"\",",
				"params=[query_vector, \"books\", max_price, 0.2],",
				"settings={\"hnsw.ef_search\": \"40\", \"hnsw.iterative_scan\": \"strict_order\"},",
				"@@ plainto_tsquery('public.\\\"english\\\"'::regconfig, $1::text) ORDER BY score DESC",
				"f AS (SELECT key, sum(1.0 / (60 + rank)) AS score FROM r GROUP BY key)",
				"params=[query_vector, query_text],",
				"JOIN unnest(ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::integer[]) WITH ORDINALITY AS k(key, ord)",
				"sql=\"SELECT to_json(count(*)) FROM \\\"products\\\" t WHERE (t.\\\"category\\\" = $1::text) AND (t.\\\"price\\\" < $2::real)\",",
				"SET \\\"price\\\" = $1::real WHERE",
				"params=[35, [1]],",
				"SELECT p.v + (p.v - n.v)",
				"params=[[1], [3], [1, 3]],",
				"WHERE $1::text = ANY(t.\\\"tags\\\") ORDER BY t.\\\"id\\\" LIMIT 10",
				"DELETE FROM \\\"products\\\" t WHERE t.\\\"id\\\" = ANY(",
				"return rt.value(self.client, sql=\"SELECT to_json(count(*)) FROM \\\"products\\\" t\")")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void bindsHostileValuesAsParameters() throws Exception {
		String hostile = "x'; DROP TABLE products; --";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq \"" + hostile + "\"",
				"hostileWrite: insert into products values [ { id: 9, title: \"" + hostile + "\", embedding: [0.1, 0.2, 0.3, 0.4] } ]", "")).get(PYTHON);

		assertTrue(python.contains("sql=\"SELECT to_json(count(*)) FROM \\\"products\\\" t WHERE t.\\\"category\\\" = $1::text\","), python);
		assertTrue(python.contains("params=[\"" + hostile + "\"],"), python);
		assertTrue(python.contains("\"title\": \"" + hostile + "\","), python);
		assertFalse(python.contains("= '" + hostile), python);
		assertFalse(python.contains("sql=\"SELECT to_json(count(*)) FROM \\\"products\\\" t WHERE t.\\\"category\\\" = 'x"), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(PgvectorGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: hnsw.ef_search, ivfflat.probes and iterative scans are effort settings, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"pgvector\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"pgvector\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"pgvector\", \"" + recall));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"),
				"ttl; PostgreSQL rows do not expire, and the pgvector image has no scheduler (pg_cron) to delete them");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; the PostgreSQL drivers connect to a server (an embedded or WASM PostgreSQL is not the target)");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, CONNECTION + " password \"written-in-the-script\""),
				"a password written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void snapshotsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nnightly: snapshot products\n",
				"snapshots; PostgreSQL backs up with pg_dump and pg_basebackup, client programs outside SQL (pg_export_snapshot names a snapshot only for the transaction that exports it)");
	}

	@Test
	public void ifVersionIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nguarded: update products set { price: 1.0 } ids [1] if-version 3\n",
				"if-version; PostgreSQL rows carry no version number (xmin is the id of the inserting transaction), and the generator adds no version column");
	}

	@Test
	public void productQuantizationIsUnsupported() throws Exception {
		assertUnsupported(replaced("vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
				"vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200 { quantization type product subvectors 2 }"),
				"product quantization; pgvector has no product quantization");
	}

	@Test
	public void groupsHitsByAField() throws Exception {
		String python = generate(sample() + "\ngrouped: vector-search in products top 3 query vector $queryVector using embedding group-by category size 1 output [title]\n").get(PYTHON);
		assertTrue(python.contains("row_number() OVER (PARTITION BY t.\\\"category\\\" ORDER BY x.ord, x.key) AS member"), python);
		assertFalse(python.contains("rt.unsupported("), python);
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"pgvector\", \"" + feature + "\")";
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
			if (key.startsWith(PgvectorGenerator.VENDOR)) {
				files.put(key.substring(PgvectorGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
