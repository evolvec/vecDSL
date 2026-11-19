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
import org.ftn.vecDSL.generator.pgvector.PgvectorGenerator;
import org.ftn.vecDSL.generator.pgvector.PgvectorJava;
import org.ftn.vecDSL.generator.pgvector.PgvectorManifest;
import org.ftn.vecDSL.generator.pgvector.PgvectorPrinter;
import org.ftn.vecDSL.generator.pgvector.PgvectorPython;
import org.ftn.vecDSL.generator.pgvector.PgvectorStatements;
import org.ftn.vecDSL.generator.pgvector.PgvectorTypeScript;
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
 * Full coverage of the vecDSL grammar for pgvector (generator/pgvector/COVERAGE.md): every native and concealed construct
 * generates in all three host languages, every unsupported construct raises its documented message, constructs not generated
 * yet raise "has no pgvector mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped" guard of
 * PgvectorStatements, and every sample of the other vendors maps without an error other than Unsupported (scripts without
 * targets generate for pgvector too).
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class PgvectorCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "pgvector_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/pgvector_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/pgvectorCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/pgvectorcoverage/PgvectorCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the endpoint, containers of other concepts, and authentication of type none. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig");

	private static final String NO_MODELS = "; PostgreSQL has no embedding models (the pgvector image includes no model extension such as pgai), so generated programs supply their own vectors";
	private static final String NO_SNAPSHOTS = "snapshots; PostgreSQL backs up with pg_dump and pg_basebackup, client programs outside SQL (pg_export_snapshot names a snapshot only for the transaction that exports it)";
	private static final String NO_TTL = "ttl; PostgreSQL rows do not expire, and the pgvector image has no scheduler (pg_cron) to delete them";
	private static final String SINGLE_SERVER = "one PostgreSQL server holds the data; streaming replication is configured on the servers, and core PostgreSQL has no sharding";
	private static final String AS_OF = "the 'as-of' clause; PostgreSQL reads the current committed state and keeps no queryable history of a table";
	private static final String PENDING = "has no pgvector mapping yet";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ pgvector ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"postgresql://postgres@127.0.0.1:5432/vecdsl\"",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\" {",
			"        filter lower type lowercase",
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
			"    vector-field half type float_vector dim 4 metric euclidean {",
			"        vector-index halfIndex family hnsw { quantization type scalar bits 16 }",
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
		Path output = Path.of("generated", "pgvector-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), PgvectorManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"rt.autocommit(self.client, sql=\"CREATE DATABASE \\\"catalog\\\"\")",
				"\"sql\": \"CREATE TABLE IF NOT EXISTS \\\"notes\\\" (\\\"id\\\" integer, \\\"body\\\" text, \\\"noteVector\\\" vector(2) NOT NULL, \\\"vecdsl_tenant\\\" text, \\\"vecdsl_namespace\\\" text, PRIMARY KEY (\\\"id\\\"))\",",
				"database=\"catalog\",",
				"\\\"details\\\" jsonb",
				"USING btree (((\\\"details\\\" #>> '{pageCount}')))",
				"{\"sql\": \"COMMENT ON TABLE \\\"books\\\" IS 'Books about databases'\"},",
				"steps=[{\"sql\": \"CREATE ROLE \\\"vecdsl_reader\\\" NOLOGIN\"}]",
				"GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE \\\"books\\\" TO \\\"vecdsl_reader\\\"",
				"ON CONFLICT (\\\"id\\\") DO NOTHING RETURNING 1) SELECT json_build_object('insert_count', count(*), 'skipped', jsonb_array_length($1::jsonb) - count(*)) FROM w",
				"return rt.import_file(",
				"location=\"file://../books.jsonl\",",
				"{\"sql\": \"ALTER TABLE \\\"books\\\" ADD COLUMN \\\"pages\\\" integer DEFAULT '100'::integer\"},",
				"steps=[{\"sql\": \"ALTER TABLE \\\"books\\\" RENAME COLUMN \\\"meta\\\" TO \\\"remark\\\"\"}]",
				"steps=[{\"sql\": \"ALTER TABLE \\\"books\\\" DROP COLUMN \\\"note\\\"\"}]",
				"steps=[{\"sql\": \"CREATE VIEW \\\"shelfView\\\" AS SELECT * FROM \\\"books\\\"\"}]",
				"CASE WHEN jsonb_exists(e.doc, 'pages') THEN r.\\\"pages\\\" ELSE $2::integer END",
				"SET \\\"tags\\\" = coalesce(\\\"tags\\\", '{}') || ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::text[]",
				"WHERE x <> ALL(ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::text[]) ORDER BY i)",
				"\\\"tags\\\" = DEFAULT, \\\"remark\\\" = DEFAULT, \\\"details\\\" = DEFAULT, \\\"pages\\\" = DEFAULT",
				"(t.tableoid, t.ctid) IN (SELECT t.tableoid, t.ctid FROM \\\"books\\\" t WHERE t.\\\"genre\\\" = $1::text ORDER BY t.\\\"id\\\" LIMIT 1)",
				"return rt.transaction(",
				"isolation=\"serializable\",",
				"PARTITION BY LIST (\\\"vecdsl_partition\\\")",
				"CREATE TABLE IF NOT EXISTS \\\"logs__default\\\" PARTITION OF \\\"logs\\\" DEFAULT",
				"USING ivfflat (\\\"v\\\" vector_l2_ops) WITH (lists = 2)",
				"settings={\"ivfflat.probes\": \"2\", \"ivfflat.iterative_scan\": \"relaxed_order\"},",
				"USING hnsw (\\\"dense\\\" vector_l1_ops)",
				"USING hnsw ((binary_quantize(\\\"coarse\\\")::bit(4)) bit_hamming_ops)",
				"\"sparseTerms\": \"{1:0.5,4:1}/8\",",
				"\"flags\": \"10110010\",",
				"ORDER BY binary_quantize(t.\\\"coarse\\\")::bit(4) <~> binary_quantize($1::vector(4)) LIMIT 8)",
				"pg_buffercache_evict_relation(c.oid)",
				"sum(pg_prewarm(c.oid))",
				"rt.autocommit(self.client, sql=\"VACUUM (ANALYZE) \\\"books\\\"\")",
				"settings={\"enable_indexscan\": \"off\"},",
				"phraseto_tsquery('public.\\\"english\\\"'::regconfig, $1::text)",
				"coalesce((SELECT string_agg(quote_literal(lexeme), ' | ') FROM unnest(tsvector_to_array(to_tsvector('public.\\\"english\\\"'::regconfig, $1::text))) AS lexeme), '')::tsquery",
				"f AS (SELECT key, sum(CASE leg WHEN 0 THEN 0.7 WHEN 1 THEN 0.3 END * score) AS score FROM r GROUP BY key)",
				"ELSE 1 END AS score FROM r), f AS (SELECT key, sum(CASE leg WHEN 0 THEN 0.6 WHEN 1 THEN 0.4 END * score) AS score FROM n GROUP BY key)",
				"'remark', t.\\\"remark\\\"",
				"ORDER BY t.\\\"price\\\" DESC NULLS LAST, t.\\\"id\\\" LIMIT 3 OFFSET 1",
				"ORDER BY random() LIMIT 2",
				"ORDER BY a.key, b.key",
				"(t.\\\"details\\\" #>> '{pageCount}')::integer > $1::integer",
				"WHERE t.\\\"remark\\\" IS NULL",
				"((t.\\\"genre\\\" = $1::text) OR ($2::text = ANY(t.\\\"tags\\\"))) IS NOT TRUE",
				"CREATE TABLE \\\"booksCopy\\\" (LIKE \\\"books\\\" INCLUDING ALL)",
				"{\"sql\": \"CREATE VIEW \\\"shelfView\\\" AS SELECT * FROM \\\"booksCopy\\\"\"},",
				"steps=[{\"sql\": \"TRUNCATE TABLE \\\"notes\\\"\"}], database=\"catalog\"",
				"steps=[{\"sql\": \"ALTER TABLE \\\"books\\\" RENAME TO \\\"shelf\\\"\"}]",
				"sql=\"SELECT to_json(count(*)) FROM \\\"shelf\\\" t\"",
				"steps=[{\"sql\": \"DROP OWNED BY \\\"vecdsl_reader\\\"\"}, {\"sql\": \"DROP ROLE \\\"vecdsl_reader\\\"\"}],",
				"steps=[{\"sql\": \"DROP TEXT SEARCH CONFIGURATION public.\\\"english\\\"\"}]",
				"sql=\"DROP DATABASE \\\"catalog\\\" WITH (FORCE)\",",
				"cursor_row AS (SELECT t.\\\"embedding\\\" <=> $1::vector(4) AS ord, t.\\\"id\\\" AS key FROM \\\"books\\\" t WHERE t.\\\"id\\\" = $2::integer)",
				"AND ((t.\\\"embedding\\\" <=> $1::vector(4), t.\\\"id\\\") > (SELECT ord, key FROM cursor_row)) ORDER BY distance LIMIT 2)",
				"cursor_row AS (SELECT -(ts_rank(to_tsvector('public.\\\"english\\\"'::regconfig, coalesce(t.\\\"title\\\", '')), plainto_tsquery('public.\\\"english\\\"'::regconfig,",
				"t.\\\"price\\\" IS NOT DISTINCT FROM (SELECT x.\\\"price\\\" FROM \\\"books\\\" x WHERE x.\\\"id\\\" = $1::integer)) AND (t.\\\"id\\\" > $1::integer)) ORDER BY t.\\\"price\\\" DESC NULLS LAST, t.\\\"id\\\" LIMIT 2",
				"WHERE mod(('x' || substr(md5(t.\\\"id\\\"::text), 1, 7))::bit(28)::integer, 2) = 1 ORDER BY t.\\\"id\\\"",
				"'total', (SELECT count(*) FROM matched)",
				"everything AS (SELECT t.tableoid AS tid, t.ctid AS rid, 1 - (t.\\\"embedding\\\" <=> $1::vector(4)) AS score FROM \\\"books\\\" t WHERE (t.\\\"embedding\\\" IS NOT NULL) AND ($1::vector(4) IS NOT NULL) AND (t.\\\"genre\\\" = $2::text))",
				"row_number() OVER (ORDER BY t.\\\"price\\\" ASC NULLS LAST, x.pos) AS n",
				"row_number() OVER (PARTITION BY t.\\\"genre\\\" ORDER BY x.ord, x.key) AS member",
				"json_build_object('group', to_json(x.grp), 'hits', json_agg(json_build_object('id', t.\\\"id\\\", 'score', x.score, 'title', t.\\\"title\\\") ORDER BY x.member)) AS hit",
				"'genres', (SELECT coalesce(json_agg(json_build_object('key', b.k, 'count', b.n) ORDER BY b.n DESC, b.k), '[]'::json) FROM (SELECT m.\\\"genre\\\" AS k, count(*) AS n FROM matched m WHERE m.\\\"genre\\\" IS NOT NULL GROUP BY m.\\\"genre\\\" ORDER BY count(*) DESC, m.\\\"genre\\\" LIMIT 10) b)",
				"'meanPrice', (SELECT to_json(avg(m.\\\"price\\\")) FROM matched m)",
				"'stats_price', (SELECT json_build_object('count', count(m.\\\"price\\\"), 'min', min(m.\\\"price\\\"), 'max', max(m.\\\"price\\\"), 'avg', avg(m.\\\"price\\\"), 'sum', sum(m.\\\"price\\\")) FROM matched m)",
				"FROM matched m CROSS JOIN LATERAL unnest(m.\\\"tags\\\") AS e(v) WHERE e.v IS NOT NULL GROUP BY e.v",
				"floor((m.\\\"details\\\" #>> '{pageCount}')::integer / $2::double precision) * $2::double precision AS k",
				"'50', percentile_cont(0.5) WITHIN GROUP (ORDER BY m.\\\"price\\\")",
				"'highlight', json_build_object('title', json_build_array(ts_headline('public.\\\"english\\\"'::regconfig, coalesce(t.\\\"title\\\", ''), plainto_tsquery('public.\\\"english\\\"'::regconfig, $2::text))))",
				"CROSS JOIN LATERAL (SELECT coalesce(x.rel * (CASE WHEN num_nonnulls(power(0.5, power(greatest(abs(t.\\\"price\\\"::double precision - $2::double precision) - 0, 0) / $3::double precision, 2)), CASE WHEN $4::text = ANY(t.\\\"tags\\\") THEN 2::double precision END) > 0 THEN coalesce(power(0.5, power(greatest(abs(t.\\\"price\\\"::double precision - $2::double precision) - 0, 0) / $3::double precision, 2)), 0) + coalesce(CASE WHEN $4::text = ANY(t.\\\"tags\\\") THEN 2::double precision END, 0) END), x.rel) AS score) b",
				"WITH RECURSIVE candidates AS MATERIALIZED",
				"picked (keys, n) AS (SELECT ARRAY[]::integer[], 0 UNION ALL SELECT p.keys || c.key, p.n + 1 FROM picked p CROSS JOIN LATERAL (SELECT c.key FROM pool c WHERE c.key <> ALL(p.keys) ORDER BY 0.2 * c.rel - 0.8 * coalesce((SELECT max(1 - (c.v <=> d.v)) FROM pool d WHERE d.key = ANY(p.keys)), 0) DESC, c.ord, c.key LIMIT 1) c WHERE p.n < 3)",
				"WITH stage_0 AS MATERIALIZED (SELECT t.tableoid AS tid, t.ctid AS rid FROM \\\"books\\\" t WHERE (t.\\\"embedding\\\" IS NOT NULL) AND ($2::vector(4) IS NOT NULL) ORDER BY t.\\\"embedding\\\" <=> $2::vector(4), t.\\\"id\\\" LIMIT 3)",
				"AND ((t.tableoid, t.ctid) IN (SELECT tid, rid FROM stage_0)) ORDER BY distance LIMIT 2)",
				"scored AS (SELECT f.key, t.tableoid AS tid, t.ctid AS rid, f.score, -f.score AS ord, f.score AS rel FROM f JOIN \\\"books\\\" t ON t.\\\"id\\\" = f.key)",
				"WITH RECURSIVE q AS MATERIALIZED (SELECT (SELECT avg(v) FROM (SELECT x.\\\"embedding\\\" AS v FROM \\\"books\\\" x WHERE x.\\\"id\\\" = ANY(",
				"row_number() OVER (ORDER BY t.\\\"price\\\" DESC NULLS LAST, x.pos) AS n",
				"\\\"price\\\" = CASE WHEN jsonb_exists(e.doc, 'price') THEN r.\\\"price\\\" ELSE t.\\\"price\\\" END",
				"FROM jsonb_array_elements($2::jsonb) AS e(doc) CROSS JOIN LATERAL jsonb_to_record(e.doc) AS r(\\\"id\\\" integer, \\\"title\\\" varchar(256)",
				"WHERE (t.\\\"id\\\" = r.\\\"id\\\") AND (t.\\\"id\\\" = ANY(ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::integer[])) RETURNING 1) SELECT json_build_object('update_count', count(*)) FROM w",
				"closes=\"catalog\")")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"await rt.autocommit(this.client, { sql: \"CREATE DATABASE \\\"catalog\\\"\" });",
				"isolation: \"serializable\",",
				"closes: \"catalog\" });",
				"async vectorsReady() {",
				"async counted(queryVector: number[]) {",
				"async repriceMany() {")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"PgvectorRuntime.autocommit(client, args(\"sql\", \"CREATE DATABASE \\\"catalog\\\"\"));",
				"\"isolation\", \"serializable\"",
				"public Object vectorsReady() {",
				"public List<Object> pairs() {",
				"public Object counted(List<Float> queryVector) {",
				"public List<Object> perGenre(List<Float> queryVector) {",
				"\"closes\", \"catalog\"")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("pgvector-data:/var/lib/postgresql"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildPrice: rebuild-index priceIndex concurrently",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"tuned: vector-search in items top 3 query vector $queryVector using embedding exclude [note]",
				"    params { candidates 64 scan-budget 5000 timeout 2000 filter-strategy iterative } output * extension pgvector { \"hnsw.scan_mem_multiplier\" = 2 }",
				"halfSearch: vector-search in items top 2 query vector $queryVector using half params { oversampling 3 } output [title]",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"createLedger: create logical-db ledger { collection ledgerItems { primary id field id type int vector-field v type float_vector dim 2 } } if-not-exists",
				"dropLedger: drop ledger if-exists",
				"listPrefixed: list collections prefix \"it\" limit 1",
				"ranged: count in items exact where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"allTags: count in items where tags contains_all [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0], half: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"dropNote: drop note",
				"phrasedTitle: count in items where title phrase \"vector database\"",
				"regexTitle: count in items where title regex \"^A\"",
				"prefixTitle: count in items where title prefix \"Vec\"",
				"neqTitle: count in items where title neq \"x\"",
				"wildTitle: count in items where title wildcard \"Vec*\"",
				"manyTags: count in items where tags length gt 1",
				"noTags: count in items where tags is_empty",
				"multiField: search in items top 3 query text \"vector\" fields [title boost 2.0, body] output [title]",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"loadP1: load items partitions [p1]",
				"hybridShaped: hybrid-search in items top 2 cursor \"1\" boost { boost-fn kind field_value field price } with-total sort { sort-by relevance desc }",
				"    aggregate { agg max on price } { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"hybridDiverse: hybrid-search in items top 2 mmr lambda 0.5 candidates 7 using embedding { vector { query vector $queryVector using embedding } keyword { query text \"vector\" fields [title] } } output [title]",
				"keywordGrouped: search in items top 2 query text \"vector\" fields [title] group-by price size 2 output [title]",
				"keywordDiverse: search in items top 2 query text \"vector\" fields [title] mmr lambda 0.5 using embedding output [title]",
				"keywordBoosted: search in items top 2 query text \"vector\" fields [title]",
				"    boost { boost-fn kind decay field price decay linear origin 10.0 scale 5.0 offset 1.0 rate 0.4 combine max apply sum } output [title]",
				"keywordSorted: search in items top 2 query text \"vector\" fields [title] sort { sort-by price desc } output [title]",
				"recommendShaped: recommend in items top 2 positive [1] using embedding cursor \"2\" with-total sort { sort-by relevance asc }",
				"    aggregate { agg count on price } output [title]",
				"recommendGrouped: recommend in items top 2 positive [1] using embedding group-by price size 1 output [title]",
				"recommendBoosted: recommend in items top 2 positive [1] using embedding boost { boost-fn kind condition when price gt 3 } output [title]",
				"recommendDiverse: recommend in items top 2 positive [1] using embedding mmr lambda 0.7 candidates 4 output [title]",
				"updateFromFile: update items from \"file://changes.json\" mode replace ids [1]",
				"")).get(CASES);
		for (String fragment : List.of(
				"rt.autocommit(self.client, sql=\"REINDEX INDEX CONCURRENTLY \\\"items_priceIndex\\\"\")",
				"return [self.both_query_0(), self.both_query_1()]",
				"rt.read_records(self.client, location=\"file://items.json\"),",
				"batch_size=50,",
				"ON CONFLICT (\\\"id\\\", \\\"vecdsl_partition\\\") DO UPDATE SET",
				"\"\"\"Ignored on pgvector: batchSize on Upsert.\"\"\"",
				"\"hnsw.ef_search\": \"64\",",
				"\"hnsw.max_scan_tuples\": \"5000\",",
				"\"statement_timeout\": \"2000\",",
				"\"hnsw.scan_mem_multiplier\": \"2\",",
				"'title', h.\\\"title\\\", 'body', h.\\\"body\\\", 'price', h.\\\"price\\\", 'tags', h.\\\"tags\\\", 'attrs', h.\\\"attrs\\\"",
				"ORDER BY t.\\\"half\\\"::halfvec(4) <-> ($1::vector(4))::halfvec(4) LIMIT 6)",
				"FROM \\\"archive\\\" x WHERE x.\\\"id\\\" = ANY(",
				"rt.autocommit(self.client, sql=\"CREATE DATABASE \\\"ledger\\\"\")",
				"database=\"ledger\",",
				"sql=\"DROP DATABASE IF EXISTS \\\"ledger\\\" WITH (FORCE)\",",
				"WHERE starts_with(name, $1::text) ORDER BY name LIMIT 1) s",
				"t.\\\"price\\\" BETWEEN $1::real AND $2::real",
				"t.\\\"note\\\" = ANY(ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::text[])",
				"t.\\\"tags\\\" && ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::text[]",
				"t.\\\"tags\\\" @> ARRAY(SELECT jsonb_array_elements_text($1::jsonb))::text[]",
				"(t.\\\"id\\\" > $1::integer) IS NOT TRUE",
				"\\\"vecdsl_tenant\\\", \\\"vecdsl_partition\\\")",
				"DELETE FROM \\\"items\\\" t WHERE t.\\\"vecdsl_tenant\\\" = $1::text",
				"steps=[{\"sql\": \"ALTER TABLE \\\"items\\\" DROP COLUMN \\\"note\\\"\"}]",
				"t.\\\"title\\\" ~ $1::text",
				"starts_with(t.\\\"title\\\", $1::text)",
				"t.\\\"title\\\" IS DISTINCT FROM $1::text",
				"t.\\\"title\\\" LIKE replace(replace(replace(replace(replace($1::text, '\\\\', '\\\\\\\\'), '%', '\\\\%'), '_', '\\\\_'), '*', '%'), '?', '_')",
				"cardinality(t.\\\"tags\\\") > $1::integer",
				"cardinality(t.\\\"tags\\\") = 0",
				"2 * ts_rank(to_tsvector('public.\\\"english\\\"'::regconfig, coalesce(t.\\\"title\\\", '')), plainto_tsquery('public.\\\"english\\\"'::regconfig, $1::text)) + ts_rank(to_tsvector('pg_catalog.simple'::regconfig, coalesce(t.\\\"body\\\", ''))",
				"steps=[{\"sql\": \"ALTER TABLE \\\"items\\\" ADD COLUMN \\\"extra\\\" vector(2)\"}]",
				"params=[[\"\\\"items__p1\\\"\"]],",
				"after_cursor AS (SELECT * FROM boosted WHERE (ord, key) > (SELECT ord, key FROM boosted WHERE key = $3::integer))",
				"CROSS JOIN LATERAL (SELECT coalesce(x.rel * (t.\\\"price\\\"::double precision), x.rel) AS score) b)",
				"row_number() OVER (ORDER BY x.ord, x.pos) AS n",
				"'max_price', (SELECT to_json(max(m.\\\"price\\\")) FROM matched m)",
				"row_number() OVER (PARTITION BY t.\\\"price\\\" ORDER BY x.ord, x.key) AS member",
				"WHERE g.member <= 2)",
				"pool AS MATERIALIZED (SELECT x.key, x.tid, x.rid, x.score, x.ord, x.rel, t.\\\"embedding\\\" AS v FROM scored x JOIN \\\"items\\\" t ON t.tableoid = x.tid AND t.ctid = x.rid WHERE t.\\\"embedding\\\" IS NOT NULL ORDER BY x.ord, x.key LIMIT 20)",
				"coalesce(x.rel + (greatest(1 - greatest(abs(t.\\\"price\\\"::double precision - $2::double precision) - $4::double precision, 0) / $3::double precision * 0.6, 0)), x.rel) AS score",
				"row_number() OVER (ORDER BY t.\\\"price\\\" DESC NULLS LAST, x.pos) AS n",
				"cursor_row AS (SELECT t.\\\"embedding\\\" <=> (SELECT v FROM q) AS ord, t.\\\"id\\\" AS key FROM \\\"items\\\" t WHERE t.\\\"id\\\" = $3::integer)",
				"row_number() OVER (ORDER BY x.ord DESC, x.pos) AS n",
				"'count_price', (SELECT to_json(count(m.\\\"price\\\")) FROM matched m)",
				"CROSS JOIN LATERAL (SELECT coalesce(x.rel * (CASE WHEN t.\\\"price\\\" > $3::real THEN 1::double precision END), x.rel) AS score) b)",
				"ORDER BY 0.7 * c.rel - 0.3 * coalesce((SELECT max(1 - (c.v <=> d.v)) FROM pool d WHERE d.key = ANY(p.keys)), 0) DESC, c.ord, c.key LIMIT 1) c WHERE p.n < 2)",
				"\\\"title\\\" = CASE WHEN jsonb_exists(e.doc, 'title') THEN r.\\\"title\\\" ELSE NULL END",
				"\\\"embedding\\\" = CASE WHEN jsonb_exists(e.doc, 'embedding') THEN r.\\\"embedding\\\" ELSE t.\\\"embedding\\\" END",
				"ORDER BY x.ord, x.key LIMIT 7)",
				"WHERE g.member <= 1)")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@Test
	public void bindsHostileValuesInResultShapes() throws Exception {
		String hostile = "x'; DROP TABLE items; --";
		String python = generate(BASE + String.join("\n",
				"hostileCursor: vector-search in items top 2 query vector $queryVector using embedding cursor \"" + hostile + "\" output [title]",
				"hostileScroll: scroll in items limit 2 cursor \"" + hostile + "\" sort { sort-by price desc } output [title]",
				"hostileShapes: search in items top 2 query text \"" + hostile + "\" fields [title] boost { boost-fn kind condition when note eq \"" + hostile + "\" }",
				"    highlight [title] output [title]",
				"hostileUpdate: update items values [ { id: 1, note: \"" + hostile + "\" } ] ids [1]",
				"")).get(CASES);
		assertFalse(python.contains("rt.unsupported("), python);
		for (String line : python.split("\n")) {
			if (line.trim().startsWith("sql=")) assertFalse(line.contains("DROP TABLE"), line);
		}
		assertTrue(python.contains("params=[query_vector, \"" + hostile + "\"],"), python);
		assertTrue(python.contains("\"note\": \"" + hostile + "\""), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: restore items from \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: drop-snapshot \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: list snapshots in items", NO_SNAPSHOTS);
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3",
				"if-version; PostgreSQL rows carry no version number (xmin is the id of the inserting transaction), and the generator adds no version column");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] ttl 60", NO_TTL);
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; PostgreSQL has no ingestion pipeline (logical replication subscribes to another PostgreSQL server)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; PostgreSQL reads object stores only through extensions the pgvector image does not include");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet",
				"import format parquet; PostgreSQL reads text, CSV and binary COPY data, not parquet");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; PostgreSQL has no copy-on-write tables");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: count in items as-of \"2026-01-01\"", AS_OF);
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\"",
				"backup schedules; PostgreSQL has no scheduler in the pgvector image (pg_cron) and no SQL backup");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; PostgreSQL has no generative models (the pgvector image includes no model extension)");
		cases.put("probe: create reranker r1 provider \"cohere\"",
				"rerankers; PostgreSQL has no reranking models (the pgvector image includes no model extension)");
		cases.put("probe: create analyzer spaces tokenizer whitespace",
				"the whitespace tokenizer; PostgreSQL ships one text search parser (default), which splits words like the standard tokenizer");
		cases.put("probe: create auth a1 type api_key",
				"api_key authentication; PostgreSQL authenticates with passwords, certificates, Kerberos, LDAP and RADIUS, configured on the server (pg_hba.conf), and has no api keys or tokens");
		cases.put("probe: create connection other uri \"postgresql://localhost:5432/vecdsl\" token \"t\"",
				"api keys and tokens; PostgreSQL authenticates users with passwords, certificates or the server's other methods");
		cases.put("probe: create connection other uri \"postgresql://localhost:5432/vecdsl\" grpc-port 6334",
				"grpc-port; PostgreSQL speaks its own protocol on one port");
		cases.put("probe: create connection lite path \"./vecdsl.db\"",
				"embedded deployments; the PostgreSQL drivers connect to a server (an embedded or WASM PostgreSQL is not the target)");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; PostgreSQL has no generative models (the pgvector image includes no model extension)");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; PostgreSQL has no setting that limits the number of tables");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + SINGLE_SERVER);
		cases.put("probe: create database prod { connection main uri \"postgresql://localhost:5432/vecdsl\" metrics m1 enabled }",
				"metrics configuration; PostgreSQL statistics are server settings (postgresql.conf, shared_preload_libraries for pg_stat_statements), not SQL objects");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; PostgreSQL explains query plans (EXPLAIN), not scores");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: hnsw.ef_search, ivfflat.probes and iterative scans are effort settings, with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; a tsquery combines terms with and, or and phrase operators and has no minimum number of matching terms");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO\" output [title]",
				"the 'fuzziness' clause; full-text search matches normalized lexemes exactly (pg_trgm similarity is a separate operator without ts_rank scores)");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] slop 2 output [title]",
				"the 'slop' clause; a tsquery phrase fixes the distance between terms (<N>) and has no slop");
		cases.put("probe: search in items top 3 query text \"a\" text-query fuzzy fields [title] output [title]",
				"text-query fuzzy; PostgreSQL full-text search matches normalized lexemes, without fuzzy, wildcard or regular-expression terms");
		cases.put("probe: alter items { set ttl = 60 }", NO_TTL);
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; PostgreSQL has no per-table drop guard (event triggers need a superuser and act on the whole database)");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; a PostgreSQL table carries one comment (the description) and no tags");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; the generated statements address the key by its declared name");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field field geom type geometry }",
				"geometry field geom; PostgreSQL has no geometry type without PostGIS, which the pgvector image does not include");
		cases.put("probe: alter items { add-field vector-field wide type float64_vector dim 2 }",
				"float64_vector fields; pgvector stores vector (float32), halfvec (float16), bit and sparsevec values");
		cases.put("probe: alter items { add-field vector-field pq type float_vector dim 4 { vector-index pqIndex family hnsw { quantization type product subvectors 2 } } }",
				"product quantization; pgvector has no product quantization");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; PostgreSQL full-text search indexes text columns directly (tsvector) and has no BM25 function producing sparse vectors");
		cases.put("probe: create items if-not-exists with { create vector-index graphIndex on half family vamana }",
				"vamana indexes; pgvector builds hnsw and ivfflat indexes");
		cases.put("probe: create items if-not-exists with { create payload-index bitmapIndex on price kind float structure bitmap }",
				"bitmap payload indexes; PostgreSQL builds bitmaps at query time and has no bitmap index type");
		cases.put("probe: refresh items",
				"collections derived from external sources; PostgreSQL reads foreign files through file_fdw, which reads text and CSV, not parquet, lance, iceberg or vortex");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		String noText = "the 'highlight' clause on a search without text; ts_headline marks the terms of a text query";
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding highlight [title] output [title]", noText);
		cases.put("probe: recommend in items top 2 positive [1] using embedding highlight [title] output [title]", noText);
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding cursor \"1\" group-by note output [title]",
				"the 'cursor' clause with group-by or mmr; keyset pagination continues after one record in an order that each record's own values decide");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding mmr lambda 0.5 group-by note output [title]",
				"mmr with group-by; MMR selects single hits, groups select records per group value");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] group-by note sort { sort-by price asc } output [title]",
				"sort with group-by; groups follow the relevance of their best hit");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding score native boost { boost-fn kind field_value field price } output [title]",
				"boosts with score native; boosts apply to the canonical similarity, where higher is better");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] aggregate { agg range on price } output [title]",
				"range aggregations; vecDSL gives no range bounds to count records in");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; text search dictionaries read files from the server's tsearch_data directory, which the driver cannot write");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put(search + "within attrs output [title]", "the 'within' clause; " + PENDING + " (jsonb_path_exists)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv", "import format csv; " + PENDING + " (COPY FROM STDIN with FORMAT csv)");
		cases.put("probe: count in items where title fuzzy \"vectr\"", "the fuzzy text operator; " + PENDING + " (pg_trgm in contrib)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; " + PENDING + " (identity columns and gen_random_uuid)");
		cases.put("probe: alter items { set dynamic = true }", "dynamic; " + PENDING + " (a jsonb column for undeclared fields)");
		cases.put("probe: search in items top 3 query text \"vec\" text-query prefix fields [title] output [title]",
				"text-query prefix; " + PENDING + " (to_tsquery prefixes, simple lexemes and websearch_to_tsquery)");
		cases.put("probe: transaction { bulk-import into items from \"file://items.json\" }",
				"bulk-import inside a transaction; " + PENDING + " (the import as one step of the transaction)");
		cases.put("probe: create analyzer folded tokenizer standard language \"en\" { filter fold type ascii_folding }",
				"the ascii_folding filter; " + PENDING + " (the unaccent dictionary in contrib)");
		cases.put("probe: create auth a2 type username_password certificate \"client.crt\"",
				"client certificates; " + PENDING + " (sslcert and sslkey connection parameters)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]",
				"bounds on a hybrid leg; " + PENDING + " (a score condition in the leg CTE)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"pgvector\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Scripts without targets generate for every registered vendor: the other vendors' samples must map without errors. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ pgvector ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new PgvectorGenerator().program(script);
			PgvectorManifest manifest = PgvectorManifest.load();
			for (PgvectorPrinter printer : List.<PgvectorPrinter>of(new PgvectorPython(), new PgvectorTypeScript(), new PgvectorJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAPgvectorMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(PgvectorStatements.class.getDeclaredMethods())
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
			reached(new PgvectorStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new PgvectorStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(PgvectorStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(PgvectorStatements.NOT_MAPPED)) {
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
			if (key.startsWith(PgvectorGenerator.VENDOR)) {
				files.put(key.substring(PgvectorGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
