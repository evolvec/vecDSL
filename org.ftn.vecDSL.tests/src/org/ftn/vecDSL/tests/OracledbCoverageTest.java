package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.oracledb.OracledbGenerator;
import org.ftn.vecDSL.generator.oracledb.OracledbJava;
import org.ftn.vecDSL.generator.oracledb.OracledbManifest;
import org.ftn.vecDSL.generator.oracledb.OracledbPrinter;
import org.ftn.vecDSL.generator.oracledb.OracledbPython;
import org.ftn.vecDSL.generator.oracledb.OracledbStatements;
import org.ftn.vecDSL.generator.oracledb.OracledbTypeScript;
import org.ftn.vecDSL.vecDSL.Alter;
import org.ftn.vecDSL.vecDSL.AlterOperation;
import org.ftn.vecDSL.vecDSL.Concept;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLFactory;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Full coverage of the vecDSL grammar for Oracle AI Database 26ai (generator/oracledb/COVERAGE.md): every native and concealed
 * construct generates in all three host languages, every unsupported construct raises its documented message, constructs not
 * generated yet raise "has no Oracle mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped for Oracle"
 * guard of OracledbStatements, and every sample maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class OracledbCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "oracledb_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/oracledb_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/oracledbCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/oracledbcoverage/OracledbCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/**
	 * Concepts whose bare instance legitimately emits no call: the endpoint, containers of other concepts, authentication of type
	 * none, and a logical database, which on Oracle is a table-name prefix that exists only through its collections.
	 */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig", "Database");

	private static final String PENDING = "has no Oracle mapping yet";
	private static final String NO_MODELS = OracledbStatements.NO_MODELS;
	private static final String NO_SNAPSHOTS = OracledbStatements.NO_SNAPSHOTS;
	private static final String NO_TTL = OracledbStatements.NO_TTL;
	private static final String SINGLE_SERVER = OracledbStatements.SINGLE_SERVER;

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ oracledb ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"127.0.0.1:1521/FREEPDB1\" username \"VECDSL\"",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\" {",
			"        filter stems type stemmer",
			"    }",
			"    field id type int",
			"    field title type varchar max-length 128 full-text analyzer english",
			"    field body type text full-text",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    field note type varchar",
			"    field attrs type json",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    payload-index priceIndex on price kind float",
			"    payload-index attrsIndex on attrs kind json",
			"    partition p1",
			"    tenant east",
			"}",
			"createArchive: create collection archive { primary id field id type int vector-field embedding type float_vector dim 4 metric cosine }",
			"");

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private GeneratorDelegate generator;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@Test
	public void generatesTheFullSampleInEveryLanguage() throws Exception {
		Map<String, String> files = generate(Files.readString(SAMPLE, StandardCharsets.UTF_8));
		Path output = Path.of("generated", "oracledb-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), OracledbManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"\"sql\": \"BEGIN CTX_DDL.CREATE_PREFERENCE('english', 'BASIC_LEXER'); CTX_DDL.SET_ATTRIBUTE('english', 'INDEX_STEMS', 'ENGLISH'); END;\",",
				"CREATE TABLE IF NOT EXISTS \\\"catalog__notes\\\" (\\\"id\\\" NUMBER(10)",
				"\\\"tags\\\" JSON",
				"CREATE VECTOR INDEX \\\"books_embeddingIndex\\\" ON \\\"books\\\" (\\\"embedding\\\") ORGANIZATION INMEMORY NEIGHBOR GRAPH DISTANCE COSINE WITH TARGET ACCURACY 90 PARAMETERS (TYPE HNSW, NEIGHBORS 16, EFCONS",
				"INDEXTYPE IS CTXSYS.CONTEXT PARAMETERS ('LEXER english SYNC (ON COMMIT)')",
				"JSON_VALUE(\\\"details\\\", '$.\\\"pageCount\\\"' RETURNING NUMBER)",
				"COMMENT ON TABLE \\\"books\\\" IS 'Books about databases'",
				"rt.execute(self.client, steps=[{\"sql\": \"CREATE ROLE \\\"vecdsl_reader\\\"\"}])",
				"GRANT SELECT, INSERT, UPDATE, DELETE ON \\\"books\\\" TO \\\"vecdsl_reader\\\"",
				"sql=\"MERGE INTO \\\"books\\\" t USING (SELECT r.c0 AS \\\"id\\\"",
				"return rt.import_file(",
				"location=\"file://../books.jsonl\",",
				"DEFAULT 100)",
				"ALTER TABLE \\\"books\\\" RENAME COLUMN \\\"meta\\\" TO \\\"remark\\\"",
				"ALTER TABLE \\\"books\\\" DROP COLUMN \\\"note\\\"",
				"CREATE OR REPLACE VIEW \\\"shelfView\\\" AS SELECT * FROM \\\"books\\\"",
				"\\\"tags\\\" = NVL((SELECT JSON_ARRAYAGG(m.v ORDER BY m.o, m.p RETURNING CLOB) FROM (SELECT 1 AS o, s.p, s.v FROM (SEL",
				"\\\"tags\\\" = DEFAULT, \\\"remark\\\" = DEFAULT, \\\"details\\\" = DEFAULT, \\\"pages\\\" = DEFAULT",
				"ROWID IN (SELECT t.ROWID FROM \\\"books\\\" t WHERE",
				"return rt.transaction(",
				"isolation=\"serializable\",",
				"PARTITION BY LIST (\\\"vecdsl_partition\\\")",
				"ALTER TABLE \\\"logs\\\" ADD PARTITION \\\"warm\\\" VALUES ('warm')",
				"ORGANIZATION NEIGHBOR PARTITIONS DISTANCE EUCLIDEAN WITH TARGET ACCURACY 90 PARAMETERS (TYPE IVF, NEIGHBOR PARTITIONS 2)",
				"WITH TARGET ACCURACY PARAMETERS (NEIGHBOR PARTITION PROBES 2)",
				"ORGANIZATION INMEMORY NEIGHBOR GRAPH DISTANCE MANHATTAN",
				"\"sparseTerms\": [8, [0, 3], [0.5, 1]],",
				"\"flags\": [178],",
				"TO_VECTOR(:1, 8, FLOAT32, SPARSE), DOT)",
				"FETCH EXACT FIRST 2 ROWS ONLY)",
				"FETCH APPROX FIRST 3 ROWS ONLY WITH TARGET ACCURACY PARAMETERS (EFSEARCH 80))",
				"text_query(self.client, text=\"databases explained\", mode=\"phrase\", stem=True)",
				"text_query(self.client, text=\"espresso graph\", mode=\"any\", stem=True)",
				"CONTAINS(t.\\\"title\\\", :1, 1)",
				"WITH l0 AS (SELECT t.\\\"id\\\" AS key, 1 - (VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:1, 4, FLOAT32), COSINE)) AS score",
				"CASE WHEN MAX(score) OVER (PARTITION BY leg) > MIN(score) OVER (PARTITION BY leg) THEN (score - MIN",
				"WITH q AS (SELECT TO_VECTOR(p.v + (p.v - n.v), 4, FLOAT32) AS v FROM (SELECT AVG(v) AS v FROM",
				"DBMS_RANDOM.VALUE FETCH FIRST 2 ROWS ONLY",
				"CROSS APPLY (SELECT o.key, VECTOR_DISTANCE(a.v, o.v, COSINE) AS distanc",
				"\\\"books\\\" t WHERE t.\\\"remark\\\" IS NULL",
				"CASE WHEN (t.\\\"",
				"JSON_EXISTS(t.\\\"",
				"CASE WHEN i.status = 'VALID' AND NVL(i.domidx_opstatus, 'VALID') = 'VALID' THEN 'ready' ELSE 'build",
				"CREATE TABLE \\\"booksCopy\\\" AS SELECT * FROM \\\"books\\\"",
				"ALTER TABLE \\\"booksCopy\\\" ADD PRIMARY KEY (\\\"id\\\")",
				"CREATE OR REPLACE VIEW \\\"shelfView\\\" AS SELECT * FROM \\\"booksCopy\\\"",
				"TRUNCATE TABLE \\\"catalog__notes\\\"",
				"ALTER TABLE \\\"logs\\\" DROP PARTITION \\\"cold\\\" UPDATE INDEXES",
				"ALTER TABLE \\\"books\\\" RENAME TO \\\"shelf\\\"",
				"DROP ROLE \\\"vecdsl_reader\\\"",
				"BEGIN CTX_DDL.DROP_PREFERENCE('english'); END;",
				"user_tables WHERE SUBSTR(table_name, 1, 9) = 'catalog__') LOOP EXECUTE IMMEDIATE 'DROP TABLE",
				"cursor_row AS (SELECT VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:1, 4, FLOAT32), COSINE) AS ord, t.\\\"id\\\" AS key FROM \\\"books\\\" t WHERE t.\\\"id\\\" = :2)",
				"cursor_row AS (SELECT ord, key FROM scored WHERE key = :2), after_cursor AS (SELECT x.* FROM scored x WHERE (x.ord > (SELECT ord FROM cursor_row) OR (x.ord = (SELECT ord FROM cursor_row) AND x.key > (SELECT key FROM cursor_row))))",
				"WITH cursor_row AS (SELECT x.\\\"price\\\" AS c0, x.\\\"id\\\" AS k FROM \\\"books\\\" x WHERE x.\\\"id\\\" = :1)",
				"DECODE(t.\\\"price\\\", (SELECT c0 FROM cursor_row), 1, 0) = 1) AND (t.\\\"id\\\" > (SELECT k FROM cursor_row))",
				"WHERE ORA_HASH(t.\\\"id\\\", 1) = 0 ORDER BY t.\\\"id\\\"",
				"WHERE ORA_HASH(t.\\\"id\\\", 1) = 1 ORDER BY t.\\\"id\\\"",
				"'total' VALUE (SELECT COUNT(*) FROM matched), 'aggregations' VALUE JSON_OBJECT('max_price' VALUE (SELECT MAX(m.\\\"price\\\") FROM matched m)",
				"ROW_NUMBER() OVER (ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos) AS n",
				"ROW_NUMBER() OVER (PARTITION BY t.\\\"genre\\\" ORDER BY x.ord, x.key) AS member",
				"JOIN leaders l ON DECODE(l.grp, g.grp, 1, 0) = 1 WHERE g.member <= 2",
				"JSON_OBJECT('group' VALUE x.grp, 'hits' VALUE JSON_ARRAYAGG(",
				"CROSS APPLY JSON_TABLE(m.\\\"tags\\\", '$[*]' COLUMNS (v VARCHAR2(4000) PATH '$')) e",
				"FLOOR(JSON_VALUE(m.\\\"details\\\", '$.\\\"pageCount\\\"' RETURNING NUMBER) / w.w) * w.w AS k",
				"'50' VALUE PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY m.\\\"price\\\")",
				"CTX_DOC.SNIPPET('\\\"' || (SELECT MIN(i.index_name) FROM user_indexes i JOIN user_ind_columns c ON c.index_name = i.index_name WHERE i.table_name = :2 AND c.column_name = :3 AND i.ityp_name = 'CONTEXT') || '\\\"', ROWIDTOCHAR(t.ROWID), :4)",
				"\"sql\": \"BEGIN CTX_DOC.SET_KEY_TYPE('ROWID'); END;\"",
				"POWER(0.5, POWER(GREATEST(ABS(t.\\\"price\\\" - :3) - 0, 0) / :4, 2)) AS f0, CASE WHEN JSON_EXISTS(t.\\\"tags\\\", '$[*]?(@ == $v)' PASSING :5 AS \\\"v\\\") THEN 2 END AS f1",
				"NVL(b.rel * (CASE WHEN (NVL2(b.f0, 1, 0) + NVL2(b.f1, 1, 0)) > 0 THEN NVL(b.f0, 0) + NVL(b.f1, 0) END), b.rel) AS score",
				"picks (n, key, rid, score, ord, rel, picked, nearest, chosen) AS (SELECT 0, key, rid, score, ord, rel, CAST(NULL AS NUMBER), CAST(NULL AS BINARY_DOUBLE), FIRST_VALUE(key) OVER (ORDER BY 0.2 * rel DESC, ord, key) FROM pool UNION ALL",
				"FROM picks p JOIN pool c ON c.key = p.chosen JOIN pool d ON d.key = p.key WHERE p.n < 3), page AS (SELECT key, rid, score, ord, picked AS pos FROM picks WHERE n = 3 AND picked > 0)",
				"WITH stage_0 AS (SELECT t.ROWID AS rid FROM \\\"books\\\" t WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:1, 4, FLOAT32), COSINE), t.\\\"id\\\" FETCH FIRST 3 ROWS ONLY)",
				"(t.ROWID IN (SELECT rid FROM stage_0))",
				"f.score AS rel FROM f JOIN \\\"books\\\" t ON t.\\\"id\\\" = f.key), grouped AS (",
				"FIRST_VALUE(key) OVER (ORDER BY 0.3 * rel DESC, ord, key)",
				"text_query(self.client, text=\"databse desgn\", mode=\"all\", stem=True, fuzzy=True)",
				"text_query(self.client, text=\"vector design\", mode=\"phrase\", stem=True, span=2)",
				"AND (ORA_ROWSCN = :3)",
				"\"sql\": \"ALTER TABLE \\\"books\\\" INMEMORY PRIORITY HIGH\"",
				"\"sql\": \"ALTER TABLE \\\"books\\\" NO INMEMORY\"",
				"\"sql\": \"ALTER TABLE \\\"logs\\\" MODIFY PARTITION \\\"hot\\\" INMEMORY PRIORITY HIGH\"",
				"\"sql\": \"ALTER TABLE \\\"logs\\\" MODIFY PARTITION \\\"hot\\\" NO INMEMORY\"",
				"\"sql\": \"ALTER TABLE \\\"logs\\\" ENABLE ROW MOVEMENT\"",
				"\"sql\": \"ALTER TABLE \\\"logs\\\" SHRINK SPACE\"",
				"WHERE table_name = 'logs' AND index_type = 'NORMAL' AND partitioned = 'NO') LOOP EXECUTE IMMEDIATE 'ALTER INDEX \\\"' || r.index_name || '\\\" SHRINK SPACE'; END LOOP; END;",
				"QUANTIZATION SCALAR COMPRESSION RATIO 4 PARAMETERS (TYPE HNSW, NEIGHBORS 16, EFCONSTRUCTION 100, RESCORE FACTOR 2)")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"isolation: \"serializable\",",
				"async vectorsReady() {",
				"async repriceMany() {",
				"await rt.textQuery(this.client, { text: \"databases explained\", mode: \"phrase\", stem: true })",
				"await rt.textQuery(this.client, { text: \"databse desgn\", mode: \"all\", stem: true, fuzzy: true })",
				"await rt.textQuery(this.client, { text: \"vector design\", mode: \"phrase\", stem: true, span: 2 })",
				"async compactLogs() {")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"isolation\", \"serializable\"",
				"public Object vectorsReady() {",
				"public List<Object> pairs() {",
				"\"params\", list((Object) null),",
				"args(\"text\", \"databse desgn\", \"mode\", \"all\", \"stem\", true, \"fuzzy\", true)",
				"args(\"sql\", \"BEGIN CTX_DOC.SET_KEY_TYPE('ROWID'); END;\")",
				"WHERE ORA_HASH(t.\\\"id\\\", 1) = 1 ORDER BY t.\\\"id\\\"")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("container-registry.oracle.com/database/free:23.26.3.0"));
	}

	/** Filters and alterations the runnable sample does not contain; the filter text is OracledbSql's, shared with the sample. */
	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"ranged: count in items exact where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"allTags: count in items where tags contains_all [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"neqTitle: count in items where title neq \"x\"",
				"prefixTitle: count in items where title prefix \"Vec\"",
				"regexTitle: count in items where title regex \"^A\"",
				"wildTitle: count in items where title wildcard \"Vec*\"",
				"manyTags: count in items where tags length gt 1",
				"noTags: count in items where tags is_empty",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"phrasedTitle: count in items where title phrase \"vector database\"",
				"countScn: count in items as-of 4242",
				"getPast: get from items ids [1] as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"scrollPast: scroll in items limit 2 as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"samplePast: sample in items size 2 as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"pairsPast: distance-matrix in items using embedding sample 3 limit 1 as-of \"2026-11-15T08:00:00.000+00:00\"",
				"vectorPast: vector-search in items top 2 query vector $queryVector using embedding as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"keywordPast: search in items top 2 query text \"vector\" fields [title] as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"hybridPast: hybrid-search in items top 2 fusion rrf as-of \"2026-11-15T08:00:00.000+00:00\" { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"recommendPast: recommend in items top 2 positive [1] using embedding as-of \"2026-11-15T08:00:00.000+00:00\" output [title]",
				"clonePast: clone items to \"itemsCopy\" as-of \"2026-11-15T08:00:00.000+00:00\"",
				"keywordBoost: search in items top 2 query text \"vector\" fields [title] boost { boost-fn kind field_value field price } output [title]",
				"keywordMmr: search in items top 2 query text \"vector\" fields [title] mmr lambda 0.5 candidates 4 output [title]",
				"keywordGrouped: search in items top 2 query text \"vector\" fields [title] group-by note size 2 output [title]",
				"keywordSorted: search in items top 2 query text \"vector\" fields [title] sort { sort-by price desc } output [title]",
				"hybridAfter: hybrid-search in items top 2 fusion rrf cursor \"1\" { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridBoost: hybrid-search in items top 2 fusion rrf boost { boost-fn kind field_value field price } { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridMmr: hybrid-search in items top 2 fusion rrf mmr lambda 0.5 candidates 4 { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridSorted: hybrid-search in items top 2 fusion rrf sort { sort-by price desc } { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridSum: hybrid-search in items top 2 fusion rrf aggregate { agg sum on price } { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"recommendAfter: recommend in items top 2 positive [1] using embedding cursor \"2\" output [title]",
				"recommendBoost: recommend in items top 2 positive [1] using embedding boost { boost-fn kind field_value field price } output [title]",
				"recommendGrouped: recommend in items top 2 positive [1] using embedding group-by note size 1 output [title]",
				"recommendSum: recommend in items top 2 positive [1] using embedding with-total aggregate { agg sum on price } output [title]",
				"createSpaces: create collection spaces { primary id field id type int field title type varchar max-length 128 full-text vector-field embedding type float_vector dim 4 metric cosine namespace teamA key \"team-a\" }",
				"batchTwo: batch-query { count in items count in archive }",
				"rebuildPrice: rebuild-index priceIndex concurrently",
				"listSome: list collections limit 5",
				"bulkScoped: bulk-import into items from \"file://items.json\" format json batch 10 on-conflict update tenant east partition p1",
				"bulkSpace: bulk-import into spaces from \"file://spaces.json\" format json namespace \"team-a\"",
				"insertSpace: insert into spaces values [ { id: 1, title: \"one\", embedding: [1.0, 0.0, 0.0, 0.0] } ] namespace \"team-a\"",
				"upsertScoped: upsert into items values [ { id: 9, title: \"nine\", embedding: [0.0, 0.0, 1.0, 0.0] } ] batch 10 tenant east partition p1",
				"updateScoped: update items set { note: \"b\" } ids [1] tenant east partition p1",
				"updateSpace: update spaces set { title: \"two\" } ids [1] namespace \"team-a\"",
				"deletePart: delete from items all partition p1",
				"deleteSpace: delete from spaces all namespace \"team-a\"",
				"getScoped: get from items ids [1] with-vectors partitions [p1] output [title]",
				"getSpace: get from spaces ids [1] namespace \"team-a\" output [title]",
				"scrollScoped: scroll in items limit 2 with-vectors tenant east partitions [p1] output [title]",
				"scrollSpace: scroll in spaces limit 2 namespace \"team-a\" output [title]",
				"sampleScoped: sample in items size 2 tenant east partitions [p1] output [title]",
				"sampleSpace: sample in spaces size 2 namespace \"team-a\" output [title]",
				"pairsScoped: distance-matrix in items using embedding sample 3 limit 1 tenant east partitions [p1]",
				"pairsSpace: distance-matrix in spaces using embedding sample 3 limit 1 namespace \"team-a\"",
				"vectorSpace: vector-search in spaces top 2 query vector $queryVector using embedding namespace \"team-a\" output [title]",
				"keywordScoped: search in items top 2 query text \"vector\" fields [title] offset 1 tenant east partitions [p1] score similarity bounds min 0.0 with-vectors output [title]",
				"keywordSpace: search in spaces top 2 query text \"vector\" fields [title] namespace \"team-a\" output [title]",
				"hybridScoped: hybrid-search in items top 2 fusion rrf offset 1 tenant east partitions [p1] bounds min 0.0 with-vectors { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridSpace: hybrid-search in spaces top 2 fusion rrf namespace \"team-a\" { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"recommendScoped: recommend in items top 2 positive [1] using embedding offset 1 tenant east partitions [p1] score similarity bounds min 0.0 with-vectors params { candidates 20 } output [title]",
				"recommendSpace: recommend in spaces top 2 positive [1] using embedding namespace \"team-a\" output [title]",
				"dropNote: drop note",
				"")).get(CASES);
		for (String fragment : List.of(
				"t.\\\"price\\\" BETWEEN :1 AND :2",
				"t.\\\"note\\\" IN (SELECT v FROM JSON_TABLE(:1, '$[*]' COLUMNS (v VARCHAR2(4000) PATH '$')))",
				"EXISTS (SELECT 1 FROM JSON_TABLE(t.\\\"tags\\\", '$[*]' COLUMNS (e VARCHAR2(4000) PATH '$')) x WHERE x.e IN (SELECT v FROM JSON_TABLE(:1, '$[*]' COLUMNS (v VARCHAR2(4000) PATH '$'))))",
				"NOT EXISTS (SELECT v FROM JSON_TABLE(:1, '$[*]' COLUMNS (v VARCHAR2(4000) PATH '$')) MINUS SELECT x.e FROM JSON_TABLE(t.\\\"tags\\\", '$[*]' COLUMNS (e VARCHAR2(4000) PATH '$')) x)",
				"CASE WHEN t.\\\"id\\\" > :1 THEN 1 ELSE 0 END = 0",
				"DECODE(t.\\\"title\\\", :1, 1, 0) = 0",
				"SUBSTR(t.\\\"title\\\", 1, LENGTH(:1)) = :2",
				"REGEXP_LIKE(t.\\\"title\\\", :1)",
				"t.\\\"title\\\" LIKE :1 ESCAPE '\\\\'",
				"JSON_VALUE(t.\\\"tags\\\", '$.size()' RETURNING NUMBER) > :1",
				"JSON_VALUE(t.\\\"tags\\\", '$.size()' RETURNING NUMBER) = 0",
				"ALTER TABLE \\\"items\\\" ADD (\\\"extra\\\" VECTOR(2, FLOAT32))",
				"ALTER TABLE \\\"items\\\" DROP COLUMN \\\"note\\\"",
				"DELETE FROM \\\"items\\\" t WHERE t.\\\"vecdsl_tenant\\\" = :1",
				"mode=\"phrase\"",
				"FROM \\\"items\\\" AS OF SCN :1 t",
				"FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t JOIN JSON_TABLE(",
				"FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t ORDER BY t.\\\"id\\\" OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY",
				"(SELECT * FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t ORDER BY DBMS_RANDOM.VALUE",
				"t.\\\"embedding\\\" AS v FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY DBMS_RANDOM.VALUE",
				"AS distance FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:2, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:3, 4, FLOAT32), COSINE) FETCH EXACT FIRST 2 ROWS ONLY",
				"SCORE(1) AS score FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t WHERE CONTAINS(",
				"AS score FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:2, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') t WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY VECTOR_DISTANCE(t.\\\"embedding\\\", TO_VECTOR(:3, 4, FLOAT32), COSINE) FETCH EXACT FIRST 2 ROWS ONLY)",
				"SELECT x.\\\"embedding\\\" AS v FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ(:1, 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM') x WHERE",
				"CREATE TABLE \\\"itemsCopy\\\" AS SELECT * FROM \\\"items\\\" AS OF TIMESTAMP TO_TIMESTAMP_TZ('2026-11-15T08:00:00.000+00:00', 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.FFTZH:TZM')",
				"-score AS ord, score AS rel FROM candidates), boosted AS (SELECT key, rid, score, -score AS ord, score AS rel FROM (SELECT b.key, b.rid, NVL(b.rel * (b.f0), b.rel) AS score FROM (SELECT x.key, x.rid, x.rel, t.\\\"price\\\" AS f0 FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid) b))",
				"-score AS ord, score AS rel FROM candidates), pool AS (SELECT x.key, x.rid, x.score, x.ord, x.rel, t.\\\"embedding\\\" AS v FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY x.ord, x.key FETCH FIRST 4 ROWS ONLY), picks (n, key, rid, score, ord, rel, picked, nearest, chosen) AS (",
				"-score AS ord, score AS rel FROM candidates), grouped AS (SELECT x.key, x.rid, x.score, x.ord, t.\\\"note\\\" AS grp, ROW_NUMBER() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid), leaders AS (SELECT grp, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM grouped WHERE member = 1 ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), page AS (SELECT g.key, g.rid, g.score, g.ord, g.grp, l.pos, g.member FROM grouped g JOIN leaders l ON DECODE(l.grp, g.grp, 1, 0) = 1 WHERE g.member <= 2)",
				"-score AS ord, score AS rel FROM candidates), page AS (SELECT key, rid, score, ord, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM scored ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), hits AS (SELECT JSON_OBJECT('id' VALUE t.\\\"id\\\", 'score' VALUE x.score, 'title' VALUE t.\\\"title\\\" NULL ON NULL RETURNING CLOB) AS hit, ROW_NUMBER() OVER (ORDER BY t.\\\"price\\\" DESC NULLS LAST, x.pos) AS n",
				"f.score AS rel FROM f JOIN \\\"items\\\" t ON t.\\\"id\\\" = f.key), cursor_row AS (SELECT ord, key FROM scored WHERE key = :4), after_cursor AS (",
				"f.score AS rel FROM f JOIN \\\"items\\\" t ON t.\\\"id\\\" = f.key), boosted AS (SELECT key, rid, score, -score AS ord, score AS rel FROM (SELECT b.key, b.rid, NVL(b.rel * (b.f0), b.rel) AS score FROM (SELECT x.key, x.rid, x.rel, t.\\\"price\\\" AS f0",
				"f.score AS rel FROM f JOIN \\\"items\\\" t ON t.\\\"id\\\" = f.key), pool AS (SELECT x.key, x.rid, x.score, x.ord, x.rel, t.\\\"embedding\\\" AS v FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY x.ord, x.key FETCH FIRST 4 ROWS ONLY)",
				"f.score AS rel FROM f JOIN \\\"items\\\" t ON t.\\\"id\\\" = f.key), page AS (SELECT key, rid, score, ord, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM scored ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), hits AS (SELECT JSON_OBJECT('id' VALUE t.\\\"id\\\", 'score' VALUE x.score, 'title' VALUE t.\\\"title\\\" NULL ON NULL RETURNING CLOB) AS hit, ROW_NUMBER() OVER (ORDER BY t.\\\"price\\\" DESC NULLS LAST, x.pos) AS n",
				"f.score AS rel FROM f JOIN \\\"items\\\" t ON t.\\\"id\\\" = f.key), page AS (SELECT key, rid, score, ord, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM scored ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), hits AS (SELECT JSON_OBJECT('id' VALUE t.\\\"id\\\", 'score' VALUE x.score, 'title' VALUE t.\\\"title\\\" NULL ON NULL RETURNING CLOB) AS hit, ROW_NUMBER() OVER (ORDER BY x.pos) AS n FROM page x JOIN \\\"items\\\" t ON t.ROWID = x.rid), matched AS (SELECT t.* FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid) SELECT JSON_OBJECT('hits' VALUE NVL((SELECT JSON_ARRAYAGG(hit FORMAT JSON ORDER BY n RETURNING CLOB) FROM hits), TO_CLOB('[]')) FORMAT JSON, 'aggregations' VALUE JSON_OBJECT('sum_price' VALUE (SELECT SUM(m.\\\"price\\\") FROM matched m)",
				"cursor_row AS (SELECT VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE) AS ord, t.\\\"id\\\" AS key FROM \\\"items\\\" t WHERE t.\\\"id\\\" = :2), candidates AS (SELECT t.\\\"id\\\" AS key, t.ROWID AS rid, VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE) AS distance FROM \\\"items\\\" t WHERE (t.\\\"embedding\\\" IS NOT NULL) AND (CASE WHEN t.\\\"id\\\" IN (SELECT v FROM JSON_TABLE(:3, '$[*]' COLUMNS (v NUMBER PATH '$'))) THEN 1 ELSE 0 END = 0) AND ((VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE) > (SELECT ord FROM cursor_row) OR (VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE) = (SELECT ord FROM cursor_row) AND t.\\\"id\\\" > (SELECT key FROM cursor_row)))) ORDER BY VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE) FETCH APPROX FIRST 2 ROWS ONLY)",
				"VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE)), scored AS (SELECT key, rid, 1 - distance AS score, distance AS ord, 1 - distance AS rel FROM candidates), boosted AS (",
				"VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE)), scored AS (SELECT key, rid, 1 - distance AS score, distance AS ord, 1 - distance AS rel FROM candidates), grouped AS (SELECT x.key, x.rid, x.score, x.ord, t.\\\"note\\\" AS grp, ROW_NUMBER() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid), leaders AS (SELECT grp, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM grouped WHERE member = 1 ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), page AS (SELECT g.key, g.rid, g.score, g.ord, g.grp, l.pos, g.member FROM grouped g JOIN leaders l ON DECODE(l.grp, g.grp, 1, 0) = 1 WHERE g.member <= 1)",
				"VECTOR_DISTANCE(t.\\\"embedding\\\", (SELECT v FROM q), COSINE)), scored AS (SELECT key, rid, 1 - distance AS score, distance AS ord, 1 - distance AS rel FROM candidates), page AS (SELECT key, rid, score, ord, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM scored ORDER BY ord, key OFFSET 0 ROWS FETCH NEXT 2 ROWS ONLY), hits AS (SELECT JSON_OBJECT('id' VALUE t.\\\"id\\\", 'score' VALUE x.score, 'title' VALUE t.\\\"title\\\" NULL ON NULL RETURNING CLOB) AS hit, ROW_NUMBER() OVER (ORDER BY x.pos) AS n FROM page x JOIN \\\"items\\\" t ON t.ROWID = x.rid), matched AS (SELECT t.* FROM scored x JOIN \\\"items\\\" t ON t.ROWID = x.rid) SELECT JSON_OBJECT('hits' VALUE NVL((SELECT JSON_ARRAYAGG(hit FORMAT JSON ORDER BY n RETURNING CLOB) FROM hits), TO_CLOB('[]')) FORMAT JSON, 'total' VALUE (SELECT COUNT(*) FROM matched), 'aggregations' VALUE JSON_OBJECT('sum_price' VALUE (SELECT SUM(m.\\\"price\\\") FROM matched m)")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	/** Values of the new result shapes and reads are bound: the hostile text appears among the parameters, never in an SQL text. */
	@Test
	public void bindsHostileValuesInResultShapes() throws Exception {
		String hostile = "x'; DROP TABLE items; --";
		String python = generate(BASE + String.join("\n",
				"hostileCursor: vector-search in items top 2 query vector $queryVector using embedding cursor \"" + hostile + "\" output [title]",
				"hostileScroll: scroll in items limit 2 cursor \"" + hostile + "\" sort { sort-by price desc } output [title]",
				"hostileShapes: search in items top 2 query text \"" + hostile + "\" fields [title] fuzziness \"AUTO\" boost { boost-fn kind condition when note eq \"" + hostile + "\" }",
				"    highlight [title] output [title]",
				"hostilePast: count in items as-of \"" + hostile + "\"",
				"")).get(CASES);
		assertFalse(python.contains("rt.unsupported("), python);
		for (String line : python.split("\n")) {
			if (line.contains("\"sql\"") || line.trim().startsWith("sql=")) assertFalse(line.contains("DROP TABLE"), line);
		}
		assertTrue(python.contains("\"" + hostile + "\""), python);
		assertTrue(python.contains("text=\"" + hostile + "\", mode=\"all\", stem=True, fuzzy=True"), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: restore items from \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: drop-snapshot \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: list snapshots in items", NO_SNAPSHOTS);
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60", NO_TTL);
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Oracle has no ingestion pipeline a generated program sets up (GoldenGate is a separate product)");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow",
				"shallow clones; Oracle has no copy-on-write tables in the Free edition (snapshot copies of PDBs need a copy-on-write file system)");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; generated Oracle programs call no generative model (DBMS_VECTOR_CHAIN.UTL_TO_GENERATE_TEXT needs a third-party provider and credentials)");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; Oracle has no reranking model in the database for generated programs");
		cases.put("probe: create auth a1 type api_key",
				"api_key authentication; Oracle authenticates users with passwords, certificates, Kerberos, RADIUS or an identity provider, configured on the server, and has no api keys");
		cases.put("probe: create connection other uri \"127.0.0.1:1521/FREEPDB1\" grpc-port 6334", "grpc-port; Oracle Net listens on one port");
		cases.put("probe: create connection lite path \"./vecdsl.db\"", "embedded deployments; the Oracle drivers connect to a database server");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + SINGLE_SERVER);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; Oracle explains query plans (EXPLAIN PLAN), not scores");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; an Oracle Text query combines terms with AND, OR and ACCUM and has no minimum number of matching terms");
		cases.put("probe: alter items { set ttl = 60 }", NO_TTL);
		cases.put("probe: alter items { set deletion-protection = true }", OracledbStatements.NO_DELETION_PROTECTION);
		cases.put("probe: alter items { set tags = [\"a\"] }", OracledbStatements.NO_TAGS);
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; the generated statements address the key by its declared name");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; Oracle Text indexes text columns directly (CONTEXT indexes) and has no BM25 function producing sparse vectors");
		cases.put("probe: create items if-not-exists with { create vector-index graphIndex on embedding family vamana }",
				"vamana indexes; Oracle builds HNSW (in-memory neighbor graph) and IVF (neighbor partition) vector indexes");
		cases.put("probe: refresh items", OracledbStatements.EXTERNAL);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"2\" output [title]",
				"fuzziness 2; Oracle Text's FUZZY operator takes a similarity score from 1 to 80, not an edit distance, and its reference gives no conversion (fuzziness AUTO maps to FUZZY with its defaults)");
		cases.put("probe: search in items top 3 query text \"a b\" text-query phrase fields [title] fuzziness \"AUTO\" output [title]",
				"fuzziness with text-query phrase; Oracle Text's FUZZY operator expands single words");
		cases.put("probe: search in items top 3 query text \"a b\" fields [title] slop 2 output [title]",
				"slop without text-query phrase; slop is the distance allowed between the words of a phrase");
		cases.put("probe: search in items top 3 query text \"a b\" text-query phrase fields [title] slop 101 output [title]",
				"slop above 100; Oracle Text returns an error for a NEAR span greater than 100");
		cases.put("probe: update items values [ { id: 1, note: \"b\" } ] ids [1] if-version 3",
				"if-version with an update from records; one version cannot guard records that each carry their own values");
		cases.put("probe: create items if-not-exists with { create vector-index productIndex on embedding family hnsw { quantization type product } }",
				"product quantization; Oracle quantizes vector indexes with scalar quantization only");
		cases.put("probe: create items if-not-exists with { create vector-index narrowIndex on embedding family hnsw { quantization type scalar bits 4 } }",
				"scalar quantization to 4.0 bits of FLOAT32 vectors; Oracle compresses FLOAT32 to 16 or 8 bits and FLOAT64 to 32, 16 or 8 bits");
		cases.put("probe: count in items as-of true", "an 'as-of' value that is neither a number (an SCN) nor a string (a timestamp)");
		cases.put("probeDeep: create collection deep { primary id field id type int field meta type struct { field n type int } vector-field v type float_vector dim 2 payload-index nIndex on meta.n kind integer }\nprobe: compact deep",
				"compacting a collection with a payload index on a sub-field; that index is function-based, and Oracle cannot shrink tables with function-based indexes (Administrator's Guide, Shrinking Database Segments Online)");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; Oracle Text thesauri and dictionaries are loaded with ctxload from the server's file system");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put(search + "within attrs output [title]", "the 'within' clause; " + PENDING + " (JSON_EXISTS with a filter on array elements)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv", "import format csv; " + PENDING + " (CSV records read by the runtimes)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; " + PENDING + " (DBMS_CLOUD reads object stores with a credential)");
		cases.put("probe: count in items where title fuzzy \"vectr\"", "the fuzzy text operator; " + PENDING + " (the FUZZY operator of Oracle Text)");
		cases.put("probe: search in items top 3 query text \"vec\" text-query fuzzy fields [title] output [title]",
				"text-query fuzzy; " + PENDING + " (the FUZZY operator of Oracle Text)");
		cases.put("probe: transaction { bulk-import into items from \"file://items.json\" }",
				"bulk-import inside a transaction; " + PENDING + " (the import as one step of the transaction)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; " + PENDING + " (identity columns returned to the program)");
		cases.put("probe: alter items { set dynamic = true }", "dynamic; " + PENDING + " (a JSON column for undeclared fields)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"oracledb\", \"" + entry.getValue().replace("\\", "\\\\").replace("\"", "\\\"") + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Scripts without targets generate for every registered vendor: every sample must map without errors. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ oracledb ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new OracledbGenerator().program(script);
			OracledbManifest manifest = OracledbManifest.load();
			for (OracledbPrinter printer : List.<OracledbPrinter>of(new OracledbPython(), new OracledbTypeScript(), new OracledbJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAnOracledbMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(OracledbStatements.class.getDeclaredMethods())
				.filter(method -> method.getName().equals("_body")).map(Method::getParameterTypes)
				.filter(types -> types.length == 1).map(types -> types[0].getSimpleName()).toList();
		Script script = VecDSLFactory.eINSTANCE.createScript();
		for (EClass type : concrete(model.getMethod())) {
			if (!dispatched.contains(type.getName())) {
				unmapped.add(type.getName() + " has no dispatch case");
			}
		}
		for (EClass type : concrete(model.getConcept())) {
			Create create = VecDSLFactory.eINSTANCE.createCreate();
			create.setDefinition((Concept) named(type, "probe"));
			reached(new OracledbStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new OracledbStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(OracledbStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
			List<String> unmapped) {
		List<Program.Method> methods;
		try {
			methods = statements.methods(statement, "probe");
		} catch (RuntimeException e) {
			return;
		}
		boolean empty = true;
		for (Program.Method method : methods) {
			empty &= method.body().isEmpty();
			for (Code.Stmt stmt : method.body()) {
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(OracledbStatements.NOT_MAPPED)) {
					unmapped.add(type.getName() + ": " + failure.feature());
				}
			}
		}
		if (concept && empty && !EMPTY_BY_DESIGN.contains(type.getName())) {
			unmapped.add(type.getName() + " generates nothing");
		}
	}

	private static List<EClass> concrete(EClass root) {
		return VecDSLPackage.eINSTANCE.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
				.filter(type -> !type.isAbstract() && !type.isInterface() && root.isSuperTypeOf(type) && type != root
						&& VecDSLPackage.eINSTANCE.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
								.noneMatch(sub -> sub != type && type.isSuperTypeOf(sub)))
				.collect(Collectors.toList());
	}

	private static EObject named(EClass type, String name) {
		EObject instance = VecDSLFactory.eINSTANCE.create(type);
		EStructuralFeature feature = type.getEStructuralFeature("name");
		if (feature != null) {
			instance.eSet(feature, name);
		}
		return instance;
	}

	private void write(Map<String, String> files, Path output) throws IOException {
		for (Map.Entry<String, String> file : files.entrySet()) {
			Path path = output.resolve(file.getKey());
			Files.createDirectories(path.getParent());
			Files.writeString(path, file.getValue(), StandardCharsets.UTF_8);
		}
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
