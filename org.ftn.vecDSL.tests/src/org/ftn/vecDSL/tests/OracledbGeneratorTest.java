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
import org.ftn.vecDSL.generator.oracledb.OracledbGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Oracle AI Database clients (Python, TypeScript, Java), the Docker Compose file and the setup script of Oracle AI
 * Database Free for samples/oracledb_demo.vecDSL into generated/oracledb, and checks the constructs Oracle cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class OracledbGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "oracledb_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"127.0.0.1:1521/FREEPDB1\" username \"VECDSL\"";
	private static final String CREATE = "create collection products consistency strong {";
	private static final String NO_MODELS = "; generated Oracle programs load no ONNX embedding model into the database (VECTOR_EMBEDDING and hybrid vector indexes need one), so they supply their own vectors";

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
		System.clearProperty(OracledbGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "oracledb"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "setup/01-vecdsl-user.sh", "python/requirements.txt",
				"python/vecdsl_oracledb_runtime.py", "typescript/package.json", "typescript/src/vecdslOracledbRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/OracledbRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"dsn\": os.environ.get(\"VECDSL_ORACLEDB_DSN\", \"127.0.0.1:1521/FREEPDB1\"),"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"user\": os.environ.get(\"VECDSL_ORACLEDB_USER\", \"VECDSL\"),"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"password_variable\": \"VECDSL_ORACLEDB_PASSWORD\","), files.get(PYTHON));
		for (String fragment : List.of(
				"dsn: process.env.VECDSL_ORACLEDB_DSN ?? \"127.0.0.1:1521/FREEPDB1\"",
				"user: process.env.VECDSL_ORACLEDB_USER ?? \"VECDSL\"",
				"passwordVariable: \"VECDSL_ORACLEDB_PASSWORD\"",
				"async similar(queryVector: number[], maxPrice: number) {",
				"params: [queryVector, \"books\", maxPrice, queryVector, 0.2],")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"dsn\", envOr(\"VECDSL_ORACLEDB_DSN\", \"127.0.0.1:1521/FREEPDB1\"),",
				"\"user\", envOr(\"VECDSL_ORACLEDB_USER\", \"VECDSL\"),",
				"\"password_variable\", \"VECDSL_ORACLEDB_PASSWORD\"",
				"public List<Object> similar(List<Float> queryVector, double maxPrice) {",
				"return OracledbRuntime.query(",
				"\"params\", list(queryVector, \"books\", maxPrice, queryVector, 0.2)));",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: container-registry.oracle.com/database/free:23.26.3.0"));
		assertTrue(files.get("docker-compose.yml").contains("ORACLE_PWD: ${ORACLE_PWD:?set ORACLE_PWD}"));
		assertTrue(files.get("docker-compose.yml").contains("\"127.0.0.1:${VECDSL_ORACLEDB_PORT:-1521}:1521\""));
		assertTrue(files.get("setup/01-vecdsl-user.sh").contains("CREATE USER VECDSL IDENTIFIED BY \"${VECDSL_ORACLEDB_PASSWORD}\";"));
		assertTrue(files.get("python/requirements.txt").startsWith("oracledb>=26.1.0,<27"));
		assertTrue(files.get("typescript/package.json").contains("\"oracledb\": \"~26.0.0\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>ojdbc17</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToATableWithIndexes() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"steps=[{\"sql\": \"DROP TABLE IF EXISTS \\\"products\\\" CASCADE CONSTRAINTS PURGE\"}],",
				"\"sql\": \"BEGIN CTX_DDL.CREATE_PREFERENCE('english', 'BASIC_LEXER'); CTX_DDL.SET_ATTRIBUTE('english', 'INDEX_STEMS', 'ENGLISH'); END;\",",
				"\"sql\": \"CREATE TABLE IF NOT EXISTS \\\"products\\\" (\\\"id\\\" NUMBER(10), \\\"title\\\" VARCHAR2(256), \\\"category\\\" VARCHAR2(64), \\\"price\\\" BINARY_FLOAT, \\\"tags\\\" JSON CONSTRAINT \\\"tagLimit\\\" CHECK (JSON_VALUE(\\\"tags\\\", '$.size()' RETURNING NUMBER) <= 8), \\\"embedding\\\" VECTOR(4, FLOAT32) NOT NULL, PRIMARY KEY (\\\"id\\\"))\",",
				"\"sql\": \"CREATE VECTOR INDEX \\\"products_embeddingIndex\\\" ON \\\"products\\\" (\\\"embedding\\\") ORGANIZATION INMEMORY NEIGHBOR GRAPH DISTANCE COSINE WITH TARGET ACCURACY 90 PARAMETERS (TYPE HNSW, NEIGHBORS 16, EFCONSTRUCTION 200)\",",
				"steps=[{\"sql\": \"CREATE INDEX \\\"products_categoryIndex\\\" ON \\\"products\\\" (\\\"category\\\")\"}],",
				"INDEXTYPE IS CTXSYS.CONTEXT PARAMETERS ('LEXER english SYNC (ON COMMIT)')",
				"params=[\"products_embeddingIndex\"],",
				"\"\"\"Ignored on Oracle: consistency on VectorCollection.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToBoundSql() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"FROM JSON_TABLE(:1, '$[*]' COLUMNS (c0 NUMBER PATH '$.\\\"id\\\"', c1 VARCHAR2(4000) PATH '$.\\\"title\\\"'",
				"TO_VECTOR(r.c5, 4, FLOAT32) AS \\\"embedding\\\"",
				"counter=\"insert_count\",",
				"WHERE (t.\\\"embedding\\\" IS NOT NULL) AND ((t.\\\"category\\\" = :2) AND (t.\\\"price\\\" <= :3)) ORDER BY VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:4, 4, FLOAT32), COSINE) FETCH APPROX FIRST 3 ROWS ONLY)",
				"WHERE 1 - h.distance >= :5 ORDER BY h.distance, h.\\\"id\\\"\",",
				"params=[query_vector, \"books\", max_price, query_vector, 0.2],",
				"CONTAINS(t.\\\"title\\\", :1, 1) > 0 ORDER BY score DESC, t.\\\"id\\\" OFFSET 0 ROWS FETCH NEXT 3 ROWS ONLY",
				"params=[rt.text_query(self.client, text=query_text, mode=\"all\", stem=True)],",
				"f AS (SELECT key, SUM(1 / (60 + rnk)) AS score FROM r GROUP BY key)",
				"JOIN JSON_TABLE(:1, '$[*]' COLUMNS (ord FOR ORDINALITY, key NUMBER PATH '$')) k ON t.\\\"id\\\" = k.key",
				"sql=\"SELECT JSON_SERIALIZE(JSON_SCALAR(COUNT(*)) RETURNING CLOB) FROM \\\"products\\\" t WHERE (t.\\\"category\\\" = :1) AND (t.\\\"price\\\" < :2)\",",
				"sql=\"UPDATE \\\"products\\\" t SET \\\"price\\\" = :1 WHERE t.\\\"id\\\" IN (SELECT v FROM JSON_TABLE(:2, '$[*]' COLUMNS (v NUMBER PATH '$')))\",",
				"params=[35, [1]],",
				"TO_VECTOR(p.v + (p.v - n.v), 4, FLOAT32)",
				"params=[[1], [3], [1, 3]],",
				"WHERE JSON_EXISTS(t.\\\"tags\\\", '$[*]?(@ == $v)' PASSING :1 AS \\\"v\\\") ORDER BY t.\\\"id\\\" OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY",
				"sql=\"DELETE FROM \\\"products\\\" t WHERE t.\\\"id\\\" IN (",
				"counter=\"delete_count\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void bindsHostileValuesAsParameters() throws Exception {
		String hostile = "x'; DROP TABLE products; -- {near} & $stem % _ /* */";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq \"" + hostile + "\"",
				"hostileWrite: insert into products values [ { id: 9, title: \"" + hostile + "\", embedding: [0.1, 0.2, 0.3, 0.4] } ]",
				"hostileText: search in products top 3 query text \"" + hostile + "\" fields [title] output [title]", "")).get(PYTHON);

		assertTrue(python.contains("sql=\"SELECT JSON_SERIALIZE(JSON_SCALAR(COUNT(*)) RETURNING CLOB) FROM \\\"products\\\" t WHERE t.\\\"category\\\" = :1\","), python);
		assertTrue(python.contains("params=[\"" + hostile + "\"],"), python);
		assertTrue(python.contains("\"title\": \"" + hostile + "\","), python);
		assertTrue(python.contains("text=\"" + hostile + "\""), python);
		for (String line : python.split("\n")) {
			if (line.contains("sql=")) {
				assertFalse(line.contains("DROP TABLE products"), line);
				assertFalse(line.contains("{near}"), line);
			}
		}
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(OracledbGenerator.LANGUAGES_PROPERTY, "java");
		Map<String, String> files = generate(sample());

		assertEquals(Set.of("java", "docker-compose.yml", "setup"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void followsTheLanguageHintOfTheScript() throws Exception {
		String source = sample();
		assertTrue(source.contains("version \"1.0\""));
		Map<String, String> files = generate(source.replace("version \"1.0\"", "version \"1.0\" language typescript"));

		assertEquals(Set.of("typescript", "docker-compose.yml", "setup"), files.keySet().stream().map(path -> path.split("/")[0]).collect(Collectors.toSet()));
	}

	@Test
	public void targetRecallSetsTheTargetAccuracyAndExactFetchesExact() throws Exception {
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n"
				+ "recallExact: vector-search in products top 3 query vector $queryVector using embedding exact output [title]\n");

		assertTrue(files.get(PYTHON).contains("FETCH APPROX FIRST 3 ROWS ONLY WITH TARGET ACCURACY 90)"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("FETCH EXACT FIRST 3 ROWS ONLY)"), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("WITH TARGET ACCURACY 90)"));
		assertTrue(files.get(JAVA).contains("WITH TARGET ACCURACY 90)"));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void collectionTtlIsUnsupported() throws Exception {
		assertUnsupported(replaced(CREATE, "create collection products ttl 3600 consistency strong {"),
				"ttl; Oracle rows do not expire (a DBMS_SCHEDULER job deleting old rows needs CREATE JOB and is not generated)");
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; the Oracle drivers connect to a database server");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, CONNECTION + " password \"written-in-the-script\""),
				"a password written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void snapshotsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nnightly: snapshot products\n",
				"snapshots; Oracle backs up with RMAN and Data Pump, outside a SQL session (restore points need SYSDBA or the SELECT ANY DICTIONARY and CREATE ANY RESTORE POINT privileges and flashback logging)");
	}

	@Test
	public void halfPrecisionVectorsAreUnsupported() throws Exception {
		assertUnsupported(replaced("vector-field embedding type float_vector dim 4", "vector-field embedding type float16_vector dim 4"),
				"float16_vector fields; Oracle VECTOR dimensions are INT8, FLOAT32, FLOAT64 or BINARY");
	}

	@Test
	public void ifVersionIsMapped() throws Exception {
		// if-version n becomes ORA_ROWSCN = :n in the WHERE clause.
		assertMapped(sample() + "\nguarded: update products set { price: 1.0 } ids [1] if-version 3\n", "AND (ORA_ROWSCN = :3)");
	}

	@Test
	public void groupByIsMapped() throws Exception {
		// pgvector's group pipeline with ROW_NUMBER() OVER (PARTITION BY the group key).
		assertMapped(sample() + "\ngrouped: vector-search in products top 3 query vector $queryVector using embedding group-by category size 1 output [title]\n",
				"PARTITION BY t.\\\"category\\\" ORDER BY x.ord, x.key");
	}

	private void assertMapped(String source, String fragment) throws Exception {
		String python = generate(source).get(PYTHON);
		assertTrue(python.contains(fragment), fragment + "\n" + python);
		assertFalse(python.contains("rt.unsupported("), python);
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"oracledb\", \"" + feature + "\")";
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
			if (key.startsWith(OracledbGenerator.VENDOR)) {
				files.put(key.substring(OracledbGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
