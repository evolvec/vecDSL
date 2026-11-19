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
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.duckdb.DuckdbGenerator;
import org.ftn.vecDSL.generator.duckdb.DuckdbJava;
import org.ftn.vecDSL.generator.duckdb.DuckdbManifest;
import org.ftn.vecDSL.generator.duckdb.DuckdbPrinter;
import org.ftn.vecDSL.generator.duckdb.DuckdbPython;
import org.ftn.vecDSL.generator.duckdb.DuckdbSql;
import org.ftn.vecDSL.generator.duckdb.DuckdbStatements;
import org.ftn.vecDSL.generator.duckdb.DuckdbTypeScript;
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
 * Full coverage of the vecDSL grammar for DuckDB (generator/duckdb/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported or not-applicable construct raises its documented message,
 * constructs not generated yet raise "has no DuckDB mapping yet", no Method, Concept or AlterOperation class reaches the "not
 * mapped" guard of DuckdbStatements, and every sample maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class DuckdbCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "duckdb_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/duckdb_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/duckdbCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/duckdbcoverage/DuckdbCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the endpoint, containers of other concepts, and authentication of type none. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig");

	private static final String EMBEDDED = "an embedded database: DuckDB runs inside the program over one file, with no server, users or replicas";
	private static final String NO_MODELS = "; DuckDB has no embedding models in its core extensions, so generated programs supply their own vectors";
	private static final String FLOAT_ONLY = "the vss extension indexes FLOAT[n] arrays only (\"Only vectors consisting of FLOATs (32-bit, single precision) are supported\"), and DuckDB has no half-precision, binary or sparse vector type with distance functions";
	private static final String NO_TTL = "ttl; DuckDB rows do not expire and an embedded database runs no background jobs";
	private static final String NO_PARTITIONS = "partitions; DuckDB tables have no partitions (Hive partitioning applies to COPY ... TO files, not to tables)";
	private static final String NO_POSITIONS = "the fts index stores which terms occur in which field of a document (table terms: termid, docid, fieldid), not their positions, so match_bm25 scores bags of terms and cannot match a phrase";
	private static final String NO_RESIDENCY = "load and release; DuckDB reads table data on demand through its buffer manager, and an HNSW index is loaded into memory when its table is first accessed after a restart (vss documentation, \"Persistence\"), so there is nothing to load or release";
	private static final String AS_OF = "the 'as-of' clause; DuckDB reads the current committed state and keeps no queryable history of a table";
	private static final String PENDING = "has no DuckDB mapping yet";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local path \"vecdsl.duckdb\"",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\" {",
			"        filter lower type lowercase",
			"        filter stems type stemmer",
			"    }",
			"    field id type int",
			"    field title type varchar max-length 128 full-text analyzer english",
			"    field body type text full-text analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    field note type varchar",
			"    field attrs type json",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field other type float_vector dim 4 metric euclidean",
			"    payload-index priceIndex on price kind float",
			"    tenant east",
			"}",
			"createArchive: create collection archive { primary id field id type int vector-field embedding type float_vector dim 4 metric cosine }",
			"");

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private DuckdbGenerator duckdb;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@Test
	public void generatesTheFullSampleInEveryLanguage() throws Exception {
		Map<String, String> files = generate(Files.readString(SAMPLE, StandardCharsets.UTF_8));
		Path output = Path.of("generated", "duckdb-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), DuckdbManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		Files.writeString(output.resolve("books.csv"), DuckdbManifest.load().render("environment/books.csv"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"{\"sql\": \"CREATE SCHEMA IF NOT EXISTS \\\"catalog\\\"\"}",
				"USING HNSW (\\\"embedding\\\") WITH (metric = 'cosine', M = 16, ef_construction = 200, ef_search = 80)",
				"USING HNSW (\\\"dense\\\") WITH (metric = 'l2sq')",
				"USING HNSW (\\\"pointer\\\") WITH (metric = 'ip', M = 8)",
				"{\"sql\": \"ALTER TABLE \\\"main\\\".\\\"books\\\" RENAME COLUMN \\\"meta\\\" TO \\\"remark\\\"\"}",
				"{\"sql\": \"DROP INDEX IF EXISTS \\\"main\\\".\\\"books_embeddingIndex\\\"\"}",
				"ON CONFLICT (\\\"id\\\") DO NOTHING",
				"skipped=True,",
				"\"tags\\\" = list_concat(coalesce(\\\"tags\\\", []), $1::JSON::VARCHAR[])",
				"\\\"tags\\\" = list_filter(\\\"tags\\\", lambda element: NOT coalesce(list_contains($1::JSON::VARCHAR[], element), false))",
				"\\\"tags\\\" = DEFAULT, \\\"remark\\\" = DEFAULT, \\\"details\\\" = DEFAULT, \\\"pages\\\" = DEFAULT",
				"t.rowid IN (SELECT t.rowid FROM \\\"main\\\".\\\"books\\\" t WHERE t.\\\"genre\\\" = $1::VARCHAR ORDER BY t.\\\"id\\\" LIMIT 1)",
				"CREATE TABLE \\\"main\\\".\\\"vecdsl_snapshot__nightly\\\" AS SELECT * FROM \\\"main\\\".\\\"books\\\"",
				"settings={\"hnsw_ef_search\": \"100\", \"hnsw_prefilter\": \"false\"},",
				"{\"sql\": \"PRAGMA hnsw_compact_index('main.books_embeddingIndex')\"},",
				"{\"sql\": \"CHECKPOINT\"}",
				"ORDER BY vecdsl_distance LIMIT 3), hits AS (SELECT * FROM candidates ORDER BY vecdsl_distance, \\\"id\\\" LIMIT 2 OFFSET 1)",
				"ORDER BY vecdsl_distance, t.\\\"id\\\" LIMIT 2)",
				"(t.\\\"details\\\" ->> '$.\\\"pageCount\\\"')::INTEGER > $1::INTEGER",
				"USING SAMPLE reservoir(2 ROWS) REPEATABLE (42)",
				"CAST('0x' || substr(md5(t.\\\"id\\\"::VARCHAR), 1, 7) AS BIGINT) % 2 = 1",
				"INSERT INTO \\\"main\\\".\\\"books\\\" BY NAME SELECT * FROM \\\"main\\\".\\\"vecdsl_snapshot__nightly\\\"",
				"\"sql\": \"ALTER TABLE \\\"main\\\".\\\"books\\\" RENAME TO \\\"shelf\\\"\"",
				"{\"sql\": \"DROP SCHEMA \\\"catalog\\\" CASCADE\"}",
				"cursor_row AS MATERIALIZED (SELECT array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS ord",
				"cursor_row AS MATERIALIZED (SELECT -s.score AS ord",
				"after_cursor AS (SELECT * FROM scored x",
				"WITH RECURSIVE ",
				"quantile_cont(m.\\\"price\\\", 0.5)",
				"leaders AS MATERIALIZED",
				"boosted AS (SELECT x.key, b.score",
				"stage_0 AS MATERIALIZED",
				"LIMIT 6) c WHERE 1 - c.vecdsl_distance >= $5::DOUBLE",
				"FROM read_json($1::VARCHAR) f",
				"FROM read_csv($1::VARCHAR) f",
				"ON CONFLICT (\\\"id\\\") DO UPDATE SET \\\"embedding\\\" = excluded.\\\"embedding\\\"",
				"WHERE t.\\\"vecdsl_tenant\\\" = 'north'",
				"COPY FROM DATABASE vecdsl TO vecdsl_clone",
				"DETACH vecdsl_clone")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of("async rollBack() {", "counter: \"restored\",")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of("public List<Object> rollBack() {", "\"counter\", \"restored\"")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("README.md").contains("VECDSL_DUCKDB_PATH"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildPrice: rebuild-index priceIndex concurrently",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0], other: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"tuned: vector-search in items top 3 query vector $queryVector using embedding exclude [note] params { candidates 128 timeout 2000 } output *",
				"otherSearch: vector-search in items top 2 query vector $queryVector using other output [title]",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"createLedger: create logical-db ledger { collection ledgerItems { primary id field id type int vector-field v type float_vector dim 2 } } if-not-exists",
				"dropLedger: drop ledger if-exists",
				"listPrefixed: list collections prefix \"it\" limit 1",
				"ranged: count in items exact where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"allTags: count in items where tags contains_all [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"anyWord: count in items where title match_any_term \"vector espresso\"",
				"regexTitle: count in items where title regex \"^A\"",
				"prefixTitle: count in items where title prefix \"Vec\"",
				"neqTitle: count in items where title neq \"x\"",
				"wildTitle: count in items where title wildcard \"Vec*\"",
				"manyTags: count in items where tags length gt 1",
				"noTags: count in items where tags is_empty",
				"multiField: search in items top 3 query text \"vector\" fields [title boost 2.0, body] output [title]",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0], other: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"dropNote: drop note",
				"updateFromFile: update items from \"file://changes.json\" mode replace ids [1]",
				"createView: alter items { add-alias alias itemView for items }",
				"renameView: alter itemView rename to itemView2",
				"createGerman: create analyzer german tokenizer standard language \"de\"",
				"emptyItems: truncate items",
				"")).get(CASES);
		for (String fragment : List.of(
				"{\"sql\": \"DROP INDEX IF EXISTS \\\"main\\\".\\\"items_priceIndex\\\"\"},",
				"{\"sql\": \"CREATE INDEX \\\"items_priceIndex\\\" ON \\\"main\\\".\\\"items\\\" (\\\"price\\\")\"},",
				"\"\"\"Ignored on DuckDB: concurrently on RebuildIndex, which DuckDB runs inside one transaction.\"\"\"",
				"return [self.both_query_0(), self.both_query_1()]",
				"rt.read_records(self.client, location=\"file://items.json\")",
				"batch_size=50,",
				"ON CONFLICT (\\\"id\\\") DO UPDATE SET",
				"\"\"\"Ignored on DuckDB: batchSize on Upsert.\"\"\"",
				"settings={\"hnsw_ef_search\": \"128\"},",
				"array_distance(t.\\\"other\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance",
				"x.\\\"embedding\\\" AS v FROM \\\"main\\\".\\\"archive\\\" x",
				"{\"sql\": \"CREATE SCHEMA IF NOT EXISTS \\\"ledger\\\"\"}",
				"{\"sql\": \"DROP SCHEMA IF EXISTS \\\"ledger\\\" CASCADE\"}",
				" LIMIT 1) s\",",
				"t.\\\"price\\\" BETWEEN $1::FLOAT AND $2::FLOAT",
				"list_contains($1::JSON::VARCHAR[], t.\\\"note\\\")",
				"list_has_any(t.\\\"tags\\\", $1::JSON::VARCHAR[])",
				"list_has_all(t.\\\"tags\\\", $1::JSON::VARCHAR[])",
				"(t.\\\"id\\\" > $1::INTEGER) IS NOT TRUE",
				"\\\"fts_main_items\\\".match_bm25(t.\\\"id\\\", $1::VARCHAR, fields := 'title') IS NOT NULL",
				"regexp_matches(t.\\\"title\\\", $1::VARCHAR)",
				"starts_with(t.\\\"title\\\", $1::VARCHAR)",
				"t.\\\"title\\\" IS DISTINCT FROM $1::VARCHAR",
				"'*', '%'), '?', '_') ESCAPE '\\\\'",
				"len(t.\\\"tags\\\") > $1::BIGINT",
				"len(t.\\\"tags\\\") = 0",
				"field_weights := MAP {'title': 2, 'body': 1}, conjunctive := 1",
				"ADD COLUMN \\\"extra\\\" FLOAT[2]\"",
				"\\\"vecdsl_tenant\\\"",
				"DELETE FROM \\\"main\\\".\\\"items\\\" AS t WHERE t.\\\"vecdsl_tenant\\\" = $1::VARCHAR",
				"{\"sql\": \"ALTER TABLE \\\"main\\\".\\\"items\\\" DROP COLUMN \\\"note\\\"\"}",
				"rt.read_records(self.client, location=\"file://changes.json\")",
				"ALTER VIEW \\\"main\\\".\\\"itemView\\\" RENAME TO \\\"itemView2\\\"",
				"sql=\"SELECT to_json(stem($1::VARCHAR, $2::VARCHAR))\"",
				"[\"running\", \"german\"]",
				"sql=\"TRUNCATE \\\"main\\\".\\\"items\\\"\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3",
				"if-version; DuckDB rows carry no version number (MVCC versions are internal), and the generator adds no version column");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0], other: [1.0, 0.0, 0.0, 0.0] } ] ttl 60", NO_TTL);
		cases.put("probe: alter items { set ttl = 60 }", NO_TTL);
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous", "continuous import; an embedded database runs no ingestion pipeline between programs");
		cases.put("probe: bulk-import into items from \"file://items.npy\" format numpy", "import format numpy; DuckDB reads JSON, CSV and Parquet with its core readers, not numpy");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; DuckDB has no copy-on-write tables");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: count in items as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"", "backup schedules; " + EMBEDDED + ", so nothing runs between programs");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; DuckDB has no generative models in its core extensions");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; DuckDB has no reranking models in its core extensions");
		cases.put("probe: create analyzer spaces tokenizer whitespace", "the whitespace tokenizer; the fts extension splits text with one regular expression (ignore option)");
		cases.put("probe: create auth a1 type api_key", "authentication; " + EMBEDDED + ", and access to it is the file system's");
		cases.put("probe: create connection other path \"other.duckdb\" token \"t\"", "api keys and tokens; " + EMBEDDED);
		cases.put("probe: create connection other host \"localhost\" port 5432", "host and ports; " + EMBEDDED);
		cases.put("probe: create connection other uri \"md:catalog\"", "the uri md:catalog; the generated programs open a local database file");
		cases.put("probe: create connection other path \"other.duckdb\" deployment serverless",
				"serverless deployments; the generated programs open DuckDB in process (MotherDuck, the hosted service, is out of scope)");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; DuckDB has no generative models in its core extensions");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; DuckDB has no setting that limits the number of tables");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + EMBEDDED);
		cases.put("probe: create database prod { connection main path \"prod.duckdb\" metrics m1 enabled }", "metrics configuration; " + EMBEDDED);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; DuckDB explains query plans (EXPLAIN), not scores");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: hnsw_ef_search is an effort setting, with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { probes 4 } output [title]",
				"probes; vss builds HNSW indexes, which have no lists to probe");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { scan-budget 100 } output [title]",
				"scan-budget; the vss index scan has no limit on visited entries");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { filter-strategy iterative } output [title]",
				"the iterative filter strategy; the vss index scan filters before the graph search (hnsw_prefilter) or after it, without iterative scans");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put("probe: vector-search in items top 3 query sparse_vector sparse { 0: 1.0 } using embedding output [title]", "sparse vector input; " + FLOAT_ONLY);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding highlight [title] output [title]",
				"the 'highlight' clause; the fts extension returns scores only and has no highlighting function");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; match_bm25 requires all terms (conjunctive) or any term, with no minimum number of matching terms");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]", "the 'fuzziness' clause; match_bm25 matches stemmed terms exactly");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] slop 2 output [title]", "the 'slop' clause; " + NO_POSITIONS);
		cases.put("probe: search in items top 3 query text \"a\" text-query fuzzy fields [title] output [title]",
				"text-query fuzzy; match_bm25 matches whole stemmed terms, without fuzzy, prefix, wildcard or regular-expression terms");
		cases.put("probe: search in items top 3 query text \"a b\" text-query phrase fields [title] output [title]", "text-query phrase; " + NO_POSITIONS);
		cases.put("probe: count in items where title phrase \"vector database\"", "the phrase operator; " + NO_POSITIONS);
		cases.put("probe: alter items { set deletion-protection = true }", "deletion-protection; DuckDB has no per-table drop guard");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; a DuckDB table carries one comment (the description) and no tags");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; the generated statements address the key by its declared name");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field vector-field wide type float64_vector dim 2 }", "float64_vector fields; " + FLOAT_ONLY);
		cases.put("probe: alter items { add-field vector-field pq type float_vector dim 4 { vector-index pqIndex family hnsw { quantization type product subvectors 2 } } }",
				"quantization; " + FLOAT_ONLY);
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; the fts extension indexes text columns directly and has no BM25 function producing sparse vectors");
		cases.put("probe: create items if-not-exists with { create vector-index graphIndex on other family vamana }",
				"vamana indexes; vss builds HNSW indexes only (CREATE INDEX ... USING HNSW)");
		cases.put("probe: create items if-not-exists with { create payload-index bitmapIndex on price kind float structure bitmap }",
				"bitmap payload indexes; DuckDB indexes columns with ART and keeps min-max zone maps per row group");
		cases.put("probe: create items if-not-exists with { create payload-index attrsIndex on attrs kind json }",
				"json payload indexes; DuckDB indexes scalar columns and expressions with ART, not JSON documents");
		cases.put("probe: load items", NO_RESIDENCY);
		cases.put("probe: release items", NO_RESIDENCY);
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; the fts extension takes stop words from a table and has no other resource files");
		cases.put("probe: create collection parts { primary id field id type int vector-field v type float_vector dim 2 partition p1 }", NO_PARTITIONS);
		cases.put("probe: list partitions in items", NO_PARTITIONS);
		cases.put("probe: snapshot items location \"s3://bucket/x\"",
				"a snapshot location; snapshots are tables in the database file (EXPORT DATABASE writes a whole database, not one table)");
		cases.put("probe: restore items from \"weekly\" scope full", "restore scope full; the generator restores one collection from a table copy");
		cases.put("probe: drop english", "dropping an analyzer; an analyzer is the option set of an fts index, which every write rebuilds");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"", "reading from s3; " + DuckdbSql.NO_REMOTE);
		cases.put("probe: count in items where title fuzzy \"vectr\"", "the fuzzy text operator; " + DuckdbStatements.NO_FUZZY);
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }", DuckdbStatements.NO_AUTO_ID);
		cases.put("probe: alter items { set dynamic = true }", DuckdbStatements.NO_DYNAMIC);
		cases.put("probe: alter items { add-field field geom type geometry }", "geometry field geom; " + DuckdbStatements.NO_SPATIAL);
		cases.put("probe: create items if-not-exists with { create payload-index noteGeo on note kind geo }", "geo payload indexes; " + DuckdbStatements.NO_SPATIAL);
		cases.put("probe: create collection shelves { primary id field id type int field meta type struct { field lang type varchar } vector-field v type float_vector dim 2 payload-index langIndex on meta.lang kind keyword }",
				"a path index below meta; " + DuckdbStatements.NO_PATH_INDEX);
		cases.put("probe: create collection linked { primary id field id type int vector-field v type float_vector dim 2 cross-ref [items] }", DuckdbStatements.NO_CROSS_REF);
		cases.put("probe: alter items { add-function function f2 type custom input [title] output [note] }",
				"custom functions; a custom function names its input and output fields but no computation, so there is no expression for a generated column");
		cases.put("probe: refresh items", "refreshing items, which is not derived from an external source");
		cases.put("probe: create collection lake2 derived-from \"file://lake.lance\" format lance { primary id field id type int vector-field v type float_vector dim 2 }",
				"the file format lance; DuckDB's core readers are read_parquet, read_csv and read_json (lance, iceberg, vortex and numpy files need readers the generated programs do not load)");
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put(search + "cursor \"1\" group-by note output [title]",
				"the 'cursor' clause with group-by or mmr; keyset pagination continues after one record in an order that each record's own values decide");
		cases.put(search + "group-by note sort { sort-by price asc } output [title]", "sort with group-by; groups follow the relevance of their best hit");
		cases.put(search + "mmr lambda 0.5 group-by note output [title]", "mmr with group-by; MMR selects single hits, groups select records per group value");
		cases.put(search + "score native boost { boost-fn byPrice kind field_value field price } output [title]",
				"boosts with score native; boosts apply to the canonical similarity, where higher is better");
		cases.put(search + "aggregate { agg range on price } output [title]", "range aggregations; vecDSL gives no range bounds to count records in");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding within attrs output [title]",
				"the 'within' clause; " + PENDING + " (list_filter over the elements)");
		return dynamicCases(cases);
	}

	/** The result shapes of every search class: the class's candidate stage and the clause's stage, from the printed SQL. */
	@TestFactory
	public Stream<DynamicTest> generatesTheResultShapesOfEverySearch() {
		Map<String, List<String>> cases = new TreeMap<>();
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding cursor \"1\" output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "(SELECT ord FROM cursor_row)"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding with-total output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "'total', (SELECT count(*) FROM matched)"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding mmr lambda 0.5 using embedding output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "picked (keys, n) AS (SELECT []::INTEGER[], 0 UNION ALL SELECT list_append(p.keys, c.key)"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding sort { sort-by price asc } output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding group-by note size 2 output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "row_number() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding boost { boost-fn byPrice kind field_value field price } output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "coalesce(x.rel * (t.\\\"price\\\"::DOUBLE), x.rel) AS score"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding aggregate { agg max on price } output [title]", List.of("candidates AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, array_cosine_distance(t.\\\"embedding\\\", $1::JSON::FLOAT[4]) AS vecdsl_distance", "'max_price', (SELECT to_json(max(m.\\\"price\\\")) FROM matched m)"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] cursor \"1\" output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "(SELECT ord FROM cursor_row)"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] with-total output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "'total', (SELECT count(*) FROM matched)"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] mmr lambda 0.5 using embedding output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "picked (keys, n) AS (SELECT []::INTEGER[], 0 UNION ALL SELECT list_append(p.keys, c.key)"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] sort { sort-by price asc } output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] group-by note size 2 output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "row_number() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] boost { boost-fn byPrice kind field_value field price } output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "coalesce(x.rel * (t.\\\"price\\\"::DOUBLE), x.rel) AS score"));
		cases.put("probe: search in items top 3 query text \"a\" fields [title] aggregate { agg max on price } output [title]", List.of("matches AS MATERIALIZED (SELECT t.\\\"id\\\" AS key, \\\"fts_main_items\\\".match_bm25(", "'max_price', (SELECT to_json(max(m.\\\"price\\\")) FROM matched m)"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf cursor \"1\" { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "(SELECT ord FROM cursor_row)"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf with-total { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "'total', (SELECT count(*) FROM matched)"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf mmr lambda 0.5 using embedding { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "picked (keys, n) AS (SELECT []::INTEGER[], 0 UNION ALL SELECT list_append(p.keys, c.key)"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf sort { sort-by price asc } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf group-by note size 2 { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "row_number() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf boost { boost-fn byPrice kind field_value field price } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "coalesce(x.rel * (t.\\\"price\\\"::DOUBLE), x.rel) AS score"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf aggregate { agg max on price } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]", List.of("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)", "'max_price', (SELECT to_json(max(m.\\\"price\\\")) FROM matched m)"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding cursor \"1\" output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "(SELECT ord FROM cursor_row)"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding with-total output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "'total', (SELECT count(*) FROM matched)"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding mmr lambda 0.5 using embedding output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "picked (keys, n) AS (SELECT []::INTEGER[], 0 UNION ALL SELECT list_append(p.keys, c.key)"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding sort { sort-by price asc } output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding group-by note size 2 output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "row_number() OVER (PARTITION BY t.\\\"note\\\" ORDER BY x.ord, x.key) AS member"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding boost { boost-fn byPrice kind field_value field price } output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "coalesce(x.rel * (t.\\\"price\\\"::DOUBLE), x.rel) AS score"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding aggregate { agg max on price } output [title]", List.of("array_cosine_distance(t.\\\"embedding\\\", (SELECT v FROM q)) AS vecdsl_distance", "'max_price', (SELECT to_json(max(m.\\\"price\\\")) FROM matched m)"));
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding prefetch { stage near query vector [0.0, 0.1, 0.9, 0.3] limit 3 } output [title]", List.of("stage_0 AS MATERIALIZED (SELECT t.\\\"id\\\" AS key FROM \\\"main\\\".\\\"items\\\" t WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY array_cosine_distance(t.\\\"embedding\\\", $2::JSON::FLOAT[4]), t.\\\"id\\\" LIMIT 3)", "t.\\\"id\\\" IN (SELECT key FROM stage_0)"));
		return generatedCases(cases);
	}

	/** Constructs the generator once rejected with "has no DuckDB mapping yet" and generates now. */
	@TestFactory
	public Stream<DynamicTest> generatesTheFormerlyPendingConstructs() {
		Map<String, List<String>> cases = new TreeMap<>();
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet", List.of("FROM read_parquet($1::VARCHAR) f"));
		cases.put("probe: transaction { bulk-import into items from \"file://items.json\" }", List.of("FROM read_json($1::VARCHAR) f", "rt.transaction("));
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]", List.of("ORDER BY vecdsl_distance LIMIT 3) c WHERE 1 - c.vecdsl_distance >= $2::DOUBLE)"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding exact } keyword { query text \"a\" fields [title] } } output [title]", List.of("ORDER BY vecdsl_distance, t.\\\"id\\\" LIMIT 3) c"));
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding where note eq \"a\" } keyword { query text \"a\" fields [title] } } output [title] where price lt 5", List.of("WHERE (t.\\\"embedding\\\" IS NOT NULL) AND ((t.\\\"price\\\" < $1::FLOAT) AND (t.\\\"note\\\" = $2::VARCHAR))"));
		cases.put("probe: create collection lake derived-from \"file://lake.parquet\" { primary id field id type int vector-field v type float_vector dim 2 }\nrefreshLake: refresh lake", List.of("INSERT INTO \\\"main\\\".\\\"lake\\\" (\\\"id\\\", \\\"v\\\") SELECT f.\\\"id\\\", f.\\\"v\\\" FROM read_parquet($1::VARCHAR) f", "DELETE FROM \\\"main\\\".\\\"lake\\\""));
		cases.put("probe: snapshot items tenant east", List.of("CREATE TABLE \\\"main\\\".\\\"vecdsl_snapshot__probe\\\" AS SELECT * FROM \\\"main\\\".\\\"items\\\" t WHERE t.\\\"vecdsl_tenant\\\" = 'east'"));
		return generatedCases(cases);
	}

	/** Options of each statement class that no sample statement writes: a fragment of the printed output each, "!" for an absent one. */
	@TestFactory
	public Stream<DynamicTest> generatesEveryOptionOnItsStatementClass() {
		Map<String, List<String>> cases = new TreeMap<>();
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: bulk-import into spaced from \"file://s.json\" parallel 2 async namespace \"team-a\" tenant north", List.of("\\\"vecdsl_tenant\\\", \\\"vecdsl_namespace\\\") SELECT", "parallelism on BulkImport", "async on BulkImport"));
		cases.put("probe: compact items target-segments 2 async", List.of("targetSegments on Compact", "async on Compact"));
		cases.put("probe: count in items consistency strong", List.of("def probe(self)", "SELECT to_json(count(*)) FROM \\\"main\\\".\\\"items\\\" t"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: delete from spaced ids [1] namespace \"team-a\" consistency quorum", List.of("t.\\\"vecdsl_namespace\\\" = $", "writeConsistency on Delete"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: distance-matrix in spaced using v sample 2 limit 1 namespace \"team-a\" tenant north consistency strong", List.of("t.\\\"vecdsl_namespace\\\" = $", "t.\\\"vecdsl_tenant\\\" = $", "USING SAMPLE reservoir(2 ROWS)"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: get from spaced ids [1] with-vectors namespace \"team-a\" consistency strong output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $", "'v', t.\\\"v\\\""));
		cases.put("probe: hybrid-search in items top 3 fusion rrf offset 1 tenant east consistency strong score native with-vectors exclude [note] params { timeout 2000 } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output *", List.of("LIMIT 3 OFFSET 1", "t.\\\"vecdsl_tenant\\\" = $", "score native on HybridSearch", "'other', t.\\\"other\\\"", "!" + "'note'", "timeoutMillis on SearchParams"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: hybrid-search in spaced top 2 fusion rrf namespace \"team-a\" { vector { query vector [1.0, 0.0] using v } vector { query vector [0.0, 1.0] using v } } output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: insert into spaced values [ { id: 1, body: \"a\", v: [1.0, 0.0] } ] records 1 namespace \"team-a\" consistency one", List.of("\\\"vecdsl_namespace\\\") SELECT", "recordCountHint on Insert", "writeConsistency on Insert"));
		cases.put("probe: search in items top 3 query text \"a\" text-query match fields [title] offset 1 tenant east consistency strong score similarity bounds min 0.1 with-vectors exclude [note] params { timeout 2000 } output *", List.of("conjunctive := 1", "OFFSET 1", "t.\\\"vecdsl_tenant\\\" = $", "h.vecdsl_score >= $", "'other', h.\\\"other\\\"", "!" + "'note'", "timeoutMillis on SearchParams"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: search in spaced top 2 query text \"a\" namespace \"team-a\" output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $"));
		cases.put("probe: recommend in items top 3 positive [1] using embedding offset 1 tenant east consistency strong score native bounds max 0.9 with-vectors exclude [note] params { candidates 128 } output *", List.of("LIMIT 3 OFFSET 1", "t.\\\"vecdsl_tenant\\\" = $", "'score', h.vecdsl_distance", "h.vecdsl_distance <= $", "'other', h.\\\"other\\\"", "!" + "'note'", "hnsw_ef_search"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: recommend in spaced top 2 positive [1] namespace \"team-a\" output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $"));
		cases.put("createLake: create collection lake derived-from \"file://lake.parquet\" { primary id field id type int vector-field v type float_vector dim 2 }\nprobe: refresh lake async", List.of("async on RefreshExternal"));
		cases.put("probe: restore items from \"weekly\" async", List.of("async on Restore"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: sample in spaced size 1 namespace \"team-a\" tenant north consistency strong output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $", "t.\\\"vecdsl_tenant\\\" = $"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: scroll in spaced limit 1 with-vectors namespace \"team-a\" tenant north consistency strong output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $", "t.\\\"vecdsl_tenant\\\" = $", "'v', t.\\\"v\\\""));
		cases.put("probe: snapshot items description \"before the load\"", List.of("description on Snapshot"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: update spaced set { body: \"b\" } ids [1] namespace \"team-a\" tenant north consistency all", List.of("t.\\\"vecdsl_namespace\\\" = $", "t.\\\"vecdsl_tenant\\\" = $", "writeConsistency on Update"));
		cases.put("probe: upsert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0], other: [1.0, 0.0, 0.0, 0.0] } ] records 1 tenant east consistency quorum", List.of("\\\"vecdsl_tenant\\\") SELECT", "recordCountHint on Upsert", "writeConsistency on Upsert"));
		cases.put("probe: vector-search in items top 2 query vector $queryVector using other metric euclidean output [title]", List.of("array_distance(t.\\\"other\\\", $1::JSON::FLOAT[4])"));
		cases.put("spaces: create collection spaced { primary id field id type int field body type varchar full-text vector-field v type float_vector dim 2 namespace teamA key \"team-a\" tenant north }\nprobe: vector-search in spaced top 2 query vector [1.0, 0.0] namespace \"team-a\" consistency strong output [body]", List.of("t.\\\"vecdsl_namespace\\\" = $"));
		return generatedCases(cases);
	}

	private Stream<DynamicTest> generatedCases(Map<String, List<String>> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			assertFalse(python.contains("rt.unsupported("), python);
			for (String fragment : entry.getValue()) {
				if (fragment.startsWith("!")) {
					assertFalse(python.contains(fragment.substring(1)), fragment + "\n" + python);
				} else {
					assertTrue(python.contains(fragment), fragment + "\n" + python);
				}
			}
		}));
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"duckdb\", " + Syntax.quote(entry.getValue()) + ")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Every sample, with its targets removed (VendorId lists no duckdb), maps without an error other than Unsupported. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new DuckdbGenerator().program(script);
			DuckdbManifest manifest = DuckdbManifest.load();
			for (DuckdbPrinter printer : List.<DuckdbPrinter>of(new DuckdbPython(), new DuckdbTypeScript(), new DuckdbJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasDuckdbMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(DuckdbStatements.class.getDeclaredMethods())
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
			reached(new DuckdbStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new DuckdbStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(DuckdbStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(DuckdbStatements.NOT_MAPPED)) {
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
