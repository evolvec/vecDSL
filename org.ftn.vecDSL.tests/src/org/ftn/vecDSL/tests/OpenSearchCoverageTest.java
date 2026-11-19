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
import org.ftn.vecDSL.generator.opensearch.OpenSearchGenerator;
import org.ftn.vecDSL.generator.opensearch.OpenSearchJava;
import org.ftn.vecDSL.generator.opensearch.OpenSearchManifest;
import org.ftn.vecDSL.generator.opensearch.OpenSearchPrinter;
import org.ftn.vecDSL.generator.opensearch.OpenSearchPython;
import org.ftn.vecDSL.generator.opensearch.OpenSearchStatements;
import org.ftn.vecDSL.generator.opensearch.OpenSearchTypeScript;
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
 * Full coverage of the vecDSL grammar for OpenSearch (generator/opensearch/COVERAGE.md): every native and concealed
 * construct generates in all three host languages, every unsupported construct raises its documented reason, constructs not
 * generated yet raise "has no OpenSearch mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped"
 * guard of OpenSearchStatements, and every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class OpenSearchCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "opensearch_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/opensearch_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/opensearchCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/opensearchcoverage/OpensearchCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance emits no request: the endpoint, containers of other concepts, and authentication without roles. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig", "Database");

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ opensearch ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://127.0.0.1:9201\" username \"admin\" password env(\"VECDSL_OPENSEARCH_PASSWORD\")",
			"createItems: create collection items {",
			"    primary id",
			"    analyzer english tokenizer standard language \"en\"",
			"    field id type int",
			"    field title type varchar max-length 128 full-text analyzer english",
			"    field price type float",
			"    field tags type array element-type varchar",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field half type float_vector dim 4 metric euclidean",
			"    payload-index priceIndex on price kind float",
			"    tenant east",
			"}",
			"createParts: create collection parts {",
			"    primary id",
			"    field id type int",
			"    vector-field v type float_vector dim 2 metric euclidean",
			"    partition p1",
			"}",
			"createArchive: create collection archive { primary id field id type int vector-field embedding type float_vector dim 4 metric cosine }",
			"createShelf: create collection shelf { primary id field id type int field title type varchar full-text vector-field v type float_vector dim 2 metric cosine partition s1 }",
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
		Path output = Path.of("generated", "opensearch-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), OpenSearchManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"rt.drop_matching(self.client, pattern=\"catalog.*\")",
				"rt.request(self.client, method=\"DELETE\", path=\"/books_copy\", ignore=[404])",
				"if not rt.exists(self.client, path=\"/catalog.notes\"):",
				"\"catalog.notes-tenant-north\": {\"filter\": {\"term\": {\"vecdsl_tenant\": \"north\"}}},",
				"\"catalog.notes-namespace-team-a\": {\"filter\": {\"term\": {\"vecdsl_namespace\": \"team-a\"}}},",
				"\"index.knn\": True,",
				"\"index.knn.derived_source.enabled\": False,",
				"\"index.mapping.total_fields.limit\": 200,",
				"\"_meta\": {\"description\": \"Books about databases\"},",
				"\"type\": \"knn_vector\",",
				"\"space_type\": \"cosinesimil\",",
				"\"method\": {\"name\": \"hnsw\", \"engine\": \"faiss\"},",
				"\"parameters\": {\"m\": 16, \"ef_construction\": 200, \"ef_search\": 80},",
				"path=\"/_plugins/_security/api/roles/vecdsl_reader\",",
				"body={\"index_permissions\": [{\"index_patterns\": [\"books\"], \"allowed_actions\": [\"read\", \"write\"]}]},",
				"body={\"_meta\": {\"description\": \"Books about vector databases\"}},",
				"return rt.import_file(",
				"return rt.update_if_version(",
				"\"filter\": {\"bool\": {\"filter\": [{\"term\": {\"genre\": \"databases\"}}]}}",
				"body={\"size\": 0, \"aggs\": {\"names\": {\"terms\": {\"field\": \"vecdsl_partition\", \"size\": 10000}}}},",
				"\"source\": \"knn_score\",",
				"\"lang\": \"knn\",",
				"\"params\": {\"field\": \"v\", \"query_value\": [1, 0], \"space_type\": \"l2\"},",
				"\"params\": {\"field\": \"embedding\", \"query_value\": query_vector, \"space_type\": \"cosinesimil\"},",
				"\"parameters\": {\"encoder\": {\"name\": \"sq\", \"parameters\": {\"type\": \"bf16\", \"bits\": 16}}},",
				"\"parameters\": {\"encoder\": {\"name\": \"sq\", \"parameters\": {\"type\": \"fp16\", \"bits\": 16}}},",
				"\"data_type\": \"half_float\",",
				"\"data_type\": \"byte\",",
				"\"data_type\": \"binary\",",
				"\"space_type\": \"hamming\",",
				"\"sparseTerms\": {\"type\": \"rank_features\"},",
				"\"query\": {\"bool\": {\"must\": [{\"neural_sparse\": {\"sparseTerms\": {\"query_tokens\": {\"0\": 1}}}}]}},",
				"\"vector\": [-78]",
				"\"filter\": {\"match_all\": {}}",
				"score={\"kind\": \"hamming\"},",
				"return rt.request(self.client, method=\"POST\", path=\"/logs/_close\", select=[\"acknowledged\"])",
				"rt.request(self.client, method=\"POST\", path=\"/logs/_open\")",
				"health = rt.wait_health(self.client, index=\"logs\", status=\"yellow\", timeout_seconds=60)",
				"rt.request(self.client, method=\"GET\", path=\"/_plugins/_knn/warmup/logs\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_flush\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_forcemerge\", params={\"max_num_segments\": 1})",
				"rt.request(self.client, method=\"GET\", path=\"/books/_stats\", select=[\"_all\", \"primaries\"]),",
				"rt.request(self.client, method=\"GET\", path=\"/_cluster/health/books\", select=[\"status\"]),",
				"\"_source\": {\"includes\": [\"id\", \"title\", \"price\"], \"excludes\": [\"embedding\"]},",
				"\"_source\": {\"includes\": [\"id\", \"title\", \"embedding\"]},",
				"\"method_parameters\": {\"ef_search\": 50}",
				"\"ext\": {\"mmr\": {\"diversity\": 0.5, \"candidates\": 6, \"vector_field_path\": \"embedding\"}},",
				"\"inner_hits\": {\"name\": \"group\", \"size\": 2, \"_source\": {\"includes\": [\"id\", \"title\"], \"excludes\": [\"embedding\"]}},",
				"\"aggs\": {\"meanPrice\": {\"avg\": {\"field\": \"price\"}}},",
				"top_k=2,",
				"\"query\": {\"bool\": {\"must\": [{\"match_phrase\": {\"title\": {\"query\": \"databases explained\", \"slop\": 1}}}]}},",
				"\"query\": {\"bool\": {\"must\": [{\"match\": {\"title\": {\"query\": \"databse\", \"fuzziness\": \"AUTO\"}}}]}},",
				"\"query\": {\"bool\": {\"must\": [{\"prefix\": {\"title\": {\"value\": \"vec\"}}}]}},",
				"\"bool\": {\"must\": [{\"query_string\": {\"query\": \"title:graph OR title:espresso\", \"fields\": [\"title\"]}}]},",
				"\"must\": [{\"multi_match\": {\"query\": \"databases\", \"fields\": [\"title^2\", \"genre\"], \"type\": \"most_fields\"}}],",
				"\"highlight\": {\"fields\": {\"title\": {}}},",
				"\"pagination_depth\": 3,",
				"\"phase_results_processors\": [{\"score-ranker-processor\": {\"combination\": {\"technique\": \"rrf\", \"rank_constant\": 60}}}],",
				"score={\"kind\": \"fused\"},",
				"return rt.hybrid(",
				"return rt.recommend(",
				"\"search_after\": [3],",
				"\"query\": {\"function_score\": {\"query\": {\"match_all\": {}}, \"random_score\": {}, \"boost_mode\": \"replace\"}},",
				"return rt.distance_matrix(",
				"\"bool\": {\"filter\": [{\"geo_distance\": {\"distance\": \"50km\", \"place\": {\"lat\": 45.25, \"lon\": 19.85}}}]},",
				"path=\"/_snapshot/vecdsl/books_nightly\",",
				"return rt.names(self.client, kind=\"snapshots\", path=\"/_snapshot/vecdsl/_all\", ignore=[404])",
				"path=\"/_snapshot/vecdsl/books_nightly/_restore\",",
				"path=\"/_plugins/_sm/policies/nightly\",",
				"\"creation\": {\"schedule\": {\"cron\": {\"expression\": \"0 2 * * *\", \"timezone\": \"UTC\"}}},",
				"rt.request(self.client, method=\"DELETE\", path=\"/_plugins/_sm/policies/nightly\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_clone/books_copy\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_clone/shelf\", body={\"aliases\": {}})",
				"return rt.names(self.client, kind=\"indices\", path=\"/_cat/indices/catalog.*\", strip=\"catalog.\")",
				"filters=[{\"terms\": {\"vecdsl_partition\": [\"hot\"]}}],",
				"filters=[{\"term\": {\"vecdsl_tenant\": \"north\"}}],",
				"filters=[{\"term\": {\"vecdsl_namespace\": \"team-a\"}}],",
				"\"functions\": [{\"field_value_factor\": {\"field\": \"price\", \"missing\": 0}, \"weight\": 0.01}],",
				"top_k=3,",
				"\"query\": {\"knn\": {\"embedding\": {\"vector\": query_vector, \"k\": 1000, \"filter\": {\"match_all\": {}}}}},",
				"\"sort\": [{\"_score\": {\"order\": \"desc\"}}, {\"id\": {\"order\": \"asc\"}}],",
				"after=2,",
				"after=1,",
				"\"response_processors\": [{\"hybrid_score_explanation\": {}}],",
				"\"explain\": True,",
				"\"sort\": [{\"price\": {\"order\": \"asc\", \"missing\": \"_last\"}}],",
				"\"track_scores\": True,",
				"\"slice\": {\"id\": 0, \"max\": 2},",
				"\"slice\": {\"id\": 1, \"max\": 2},",
				"op=\"patch\",",
				"only=[5, 9],",
				"    def hybrid_shaped(self, query_vector):\n        return rt.shape(\n            self.client,\n            candidates=rt.hybrid(",
				"                \"sort\": [{\"field\": [\"price\"], \"direction\": \"asc\"}],\n                \"withTotal\": True,\n",
				"                \"aggregations\": [{\"name\": \"topPrice\", \"kind\": \"max\", \"field\": [\"price\"], \"interval\": None, \"size\": None}],\n",
				"                fusion=\"relative_score\",\n                k=60,\n                size=20,\n                offset=0,\n                key=\"id\",\n                candidates=True,\n",
				"                \"group\": {\"field\": [\"genre\"], \"size\": 2},\n",
				"                size=6,\n",
				"                \"mmr\": {\"lambda\": 0.5, \"candidates\": 6, \"vectorField\": \"embedding\", \"metric\": \"cosine\"},\n",
				"            vectors=[\"embedding\"],\n",
				"                            \"kind\": \"field_value\",\n                            \"field\": [\"price\"],\n                            \"weight\": 0.01,\n",
				"                    \"combine\": \"sum\",\n                    \"apply\": \"sum\",\n",
				"                \"cursor\": \"2\",\n",
				"    def hybrid_rrf_counted(self, query_vector):\n        return rt.shape(\n            self.client,\n            candidates=rt.search(",
				"                    \"size\": 20,\n                    \"search_pipeline\": {\n")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"await rt.dropMatching(this.client, { pattern: \"catalog.*\" });",
				"return await rt.importFile(this.client, {",
				"return await rt.updateIfVersion(this.client, {",
				"async vectorsReady() {",
				"path: \"/_snapshot/vecdsl/books_nightly/_restore\",",
				"filters: [{ terms: { vecdsl_partition: [\"hot\"] } }],",
				"after: 2,",
				"slice: { id: 0, max: 2 },",
				"async hybridShaped(queryVector: number[]) {\n    return await rt.shape(this.client, {\n      candidates: await rt.hybrid(this.client, {",
				"group: { field: [\"genre\"], size: 2 },",
				"candidates: true,",
				"vectors: [\"embedding\"],")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"OpenSearchRuntime.dropMatching(client, args(\"pattern\", \"catalog.*\"));",
				"return OpenSearchRuntime.updateIfVersion(",
				"public Object vectorsReady() {",
				"public Object pairs() {",
				"\"after\", 2));",
				"\"slice\", args(\"id\", 0, \"max\", 2),",
				"\"op\", \"patch\",",
				"return OpenSearchRuntime.shape(",
				"\"candidates\", OpenSearchRuntime.hybrid(",
				"\"candidates\", OpenSearchRuntime.search(",
				"\"group\", args(\"field\", list(\"genre\"), \"size\", 2),",
				"\"vectors\", list(\"embedding\")));")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("path.repo: /usr/share/opensearch/snapshots"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildEmbedding: rebuild-index embeddingIndex concurrently",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"compactLater: compact items async",
				"deleteAll: delete from items all",
				"explained: vector-search in items top 2 query vector $queryVector using embedding explain exclude [note] output *",
				"withVectors: get from items ids [1] with-vectors output [title]",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"auditing: create auth a5 type username_password audit-log",
				"nearGroups: vector-search in items top 2 query id 1 using embedding group-by price output [title]",
				"nearBoosted: vector-search in items top 2 query id 1 using embedding boost { boost-fn kind field_value field price } output [title]",
				"boostedExact: vector-search in items top 2 query vector $queryVector using embedding exact boost { boost-fn kind field_value field price } output [title]",
				"sortedNear: vector-search in items top 2 query vector $queryVector using embedding sort { sort-by price asc } output [title]",
				"eastAfter: search in items top 2 query text \"a\" fields [title] cursor \"4\" tenant east output [title]",
				"eastById: get from items ids [1] tenant east output [title]",
				"fileUpdate: update items from \"file://changes.json\" ids [1]",
				"nearDiverse: vector-search in items top 2 query id 1 using embedding mmr lambda 0.5 output [title]",
				"nearExact: vector-search in items top 2 query id 1 using embedding exact output [title]",
				"scopedImport: bulk-import into items from \"file://items.json\" on-conflict ignore namespace \"ns\" tenant east",
				"partImport: bulk-import into parts from \"file://parts.json\" partition p1",
				"scopedInsert: insert into items values [ { id: 5, title: \"x\", embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] namespace \"ns\"",
				"scopedUpsert: upsert into items values [ { id: 5, title: \"x\", embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] batch 10 tenant east",
				"partUpsert: upsert into parts values [ { id: 1, v: [1.0, 0.0] } ] partition p1",
				"scopedDelete: delete from items ids [5] namespace \"ns\" tenant east",
				"partDelete: delete from parts ids [1] partition p1",
				"scopedUpdate: update items set { note: \"n\" } ids [5] namespace \"ns\" tenant east",
				"partUpdate: update parts set { v: [0.0, 1.0] } ids [1] partition p1",
				"scopedPairs: distance-matrix in items using embedding namespace \"ns\" tenant east",
				"partPairs: distance-matrix in parts partitions [p1]",
				"snapshotAll: snapshot items as \"all\" scope full location \"backups\"",
				"restoreAll: restore items from \"all\" location \"backups\" async",
				"dropAll: drop-snapshot \"all\" location \"backups\"",
				"firstNames: list collections limit 2",
				"scopedHybrid: hybrid-search in items top 2 fusion rrf offset 1 namespace \"ns\" tenant east bounds min 0.1 with-vectors exclude [note] {",
				"    vector { query vector $queryVector using embedding }",
				"    keyword { query text \"a\" fields [title] }",
				"} output [title]",
				"partHybrid: hybrid-search in parts top 2 fusion rrf partitions [p1] { vector { query vector [1.0, 0.0] using v } vector { query vector [0.0, 1.0] using v } } output [id]",
				"richKeywords: search in items top 2 query text \"a\" fields [title] explain offset 1 namespace \"ns\" score similarity bounds min 0.1",
				"    group-by price size 2 with-vectors with-total exclude [note] aggregate { agg avg on price } output [title]",
				"partKeywords: search in shelf top 2 query text \"a\" fields [title] partitions [s1] output [title]",
				"richSuggest: recommend in items top 2 positive [1] using embedding offset 1 namespace \"ns\" tenant east score native bounds min 0.1",
				"    with-vectors exclude [note] params { candidates 20 } output [title]",
				"partSuggest: recommend in shelf top 2 positive [1] using v partitions [s1] output [title]",
				"scopedSample: sample in items size 2 namespace \"ns\" tenant east output [title]",
				"partSample: sample in shelf size 2 partitions [s1] output [title]",
				"richScroll: scroll in items limit 2 with-vectors namespace \"ns\" output [title]",
				"partScroll: scroll in shelf limit 2 partitions [s1] output [title]",
				"richVector: vector-search in items top 2 query vector $queryVector using embedding metric cosine namespace \"ns\" sort { sort-by price asc } output [title]",
				"")).get(CASES);
		for (String fragment : List.of(
				"rt.request(self.client, method=\"POST\", path=\"/items/_forcemerge\", params={\"max_num_segments\": 1})",
				"return [self.both_query_0(), self.both_query_1()]",
				"params={\"wait_for_completion\": \"false\"}",
				"\"/items/_delete_by_query\"",
				"\"explain\": True",
				"\"excludes\": [\"note\"",
				"fields=[\"title\"]",
				"\"/items/_mapping\"",
				"\"type\": \"knn_vector\"",
				"path=\"/_plugins/_security/api/audit\"",
				"{\"op\": \"replace\", \"path\": \"/config/enabled\", \"value\": True}",
				"group=\"price\",",
				"top_k=2,",
				"\"function_score\": {",
				"\"script_score\": {",
				"\"track_scores\": True,",
				"after=4,",
				"filters=[{\"term\": {\"vecdsl_tenant\": \"east\"}}],",
				"records=rt.read_records(self.client, location=\"file://changes.json\"),",
				"op=\"patch\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: update items set { note: \"b\" } ids [1, 2] if-version 3",
				"if-version on other than one id; optimistic concurrency control (if_seq_no and if_primary_term) guards one document");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0], half: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"ttl; OpenSearch documents do not expire (Index State Management deletes whole indices, not single documents)");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; pull-based ingestion (experimental since 3.0) reads Kafka or Kinesis through the ingestion-kafka and ingestion-kinesis plugins, which the image does not include, into an index created with an ingestion_source");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; OpenSearch indexes documents that a client sends, and reads object stores only as snapshot repositories or through the experimental pull-based ingestion of an index created for it");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet",
				"import format parquet; the _bulk API takes JSON documents");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"",
				"the 'as-of' clause; OpenSearch searches the current state of an index (a point in time pins a state for paging, not a past one)");
		cases.put("probe: count in items as-of \"2026-01-01\"",
				"the 'as-of' clause; OpenSearch searches the current state of an index (a point in time pins a state for paging, not a past one)");
		cases.put("probe: create embedding-model e1 provider \"openai\"",
				"embedding models; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors");
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; generative models run through ML Commons connectors to remote services (the retrieval_augmented_generation response processor), which need a registered model and the credentials of that service");
		cases.put("probe: create reranker r1 provider \"cohere\"",
				"rerankers; the rerank response processor needs a cross-encoder model deployed through ML Commons (type ml_opensearch); its by_field type sorts by a document field and is no reranking model");
		cases.put("probe: create auth a1 type token",
				"token authentication; authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API");
		cases.put("probe: create auth a2 type mtls",
				"mtls authentication; authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API");
		cases.put("probe: create auth a3 type sso",
				"sso authentication; authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API");
		cases.put("probe: create auth a4 type username_password certificate \"client.crt\"",
				"client certificates; authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API");
		cases.put("probe: create connection other uri \"http://127.0.0.1:9201\" token \"t\"",
				"bearer tokens; authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API");
		cases.put("probe: create connection other uri \"http://127.0.0.1:9201\" grpc-port 6334",
				"grpc-port; the gRPC transport of OpenSearch serves only search and bulk requests, so the generated programs use the REST API");
		cases.put("probe: create connection lite path \"./vecdsl.db\"",
				"embedded deployments; OpenSearch runs as a server that the clients reach over HTTP");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; generative models run through ML Commons connectors to remote services (the retrieval_augmented_generation response processor), which need a registered model and the credentials of that service");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; OpenSearch limits shards per node (cluster.max_shards_per_node), not the number of indices");
		cases.put("probe: create database prod { connection main uri \"http://127.0.0.1:9201\" metrics m1 enabled }",
				"metrics configuration; OpenSearch metrics come from the stats APIs and the Performance Analyzer plugin, not from a per-program configuration");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: the knn query takes k, ef_search and an oversample factor, effort settings with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]",
				"text input; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors");
		cases.put("probe: alter items { set ttl = 60 }",
				"ttl; OpenSearch documents do not expire (Index State Management deletes whole indices, not single documents)");
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; OpenSearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)");
		cases.put("probe: alter items { rename-field id to itemKey }",
				"renaming the primary key; the generated requests address the key by its declared name");
		cases.put("probe: alter items { drop-field id }",
				"dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field vector-field wide type float64_vector dim 2 }",
				"float64_vector fields; knn_vector fields hold float, half_float, byte (signed int8) and binary elements (bfloat16 through the Faiss sq encoder)");
		cases.put("probe: alter items { add-field vector-field unsigned type uint8_vector dim 2 }",
				"uint8_vector fields; knn_vector byte vectors are signed 8-bit integers in [-128, 127]");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; OpenSearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (rank_features fields take their weights from the client or a sparse encoding model)");
		cases.put("probe: create items if-not-exists with { create payload-index bitmapIndex on price kind float structure bitmap }",
				"bitmap payload indexes; OpenSearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type");
		cases.put("probe: create collection graphs { primary id field id type int vector-field v type float_vector dim 2 { vector-index vi family vamana } }",
				"vamana indexes; DiskANN is the jvector engine of the opensearch-jvector plugin, which the image does not include");
		cases.put("probe: refresh items",
				"collections derived from external sources; OpenSearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; OpenSearch reads analysis files from the config directory of the node, which a client cannot write");
		cases.put("probe: transaction { delete from items ids [1] }",
				"transactions; OpenSearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)");
		cases.put("probe: load parts partitions [p1]",
				"loading partitions; OpenSearch opens and closes whole indices");
		cases.put("probe: release parts partitions [p1]",
				"releasing partitions; OpenSearch opens and closes whole indices");
		cases.put("probe: snapshot items tenant east",
				"tenant snapshots; a snapshot holds whole indices");
		cases.put("probe: drop embeddingIndex",
				"dropping a vector index; the method of a knn_vector field (engine, parameters and encoder) cannot be updated after the field is created");
		cases.put("probe: drop priceIndex",
				"dropping a payload index; whether a field is indexed is part of its mapping, which OpenSearch cannot change");
		cases.put("probe: drop english",
				"dropping an analyzer; analysis settings are removed with their index, not while fields use them");
		cases.put("probe: recommend in items top 2 positive [1] using embedding highlight [title] output [title]",
				"the 'highlight' clause on a recommendation; highlighting marks the terms of a text query, which a recommendation has not");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding highlight [title] output [title]",
				"the 'highlight' clause on a vector search; highlighting marks the terms of a text query, which a vector search has not");
		cases.put("probe: hybrid-search in items top 2 fusion rrf highlight [title] { vector { query vector $queryVector using embedding } vector { query vector [1.0, 0.0, 0.0, 0.0] using half } } output [title]",
				"the 'highlight' clause on a hybrid search without a keyword leg; highlighting marks the terms of a text query, which its vector legs have not");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding offset 1 cursor \"3\" output [title]",
				"the 'cursor' clause with offset; a cursor page continues after its record in the order of the search");
		cases.put("probe: search in items top 2 query text \"a\" fields [title] cursor \"3\" sort { sort-by price asc } output [title]",
				"the 'cursor' clause with sort; a cursor page continues after its record in the order of the search");
		cases.put("probe: recommend in items top 2 positive [1] using embedding cursor \"3\" group-by price output [title]",
				"the 'cursor' clause with group-by; a cursor page continues after its record in the order of the search");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding mmr lambda 0.5 boost { boost-fn kind field_value field price } output [title]",
				"the 'boost' clause with mmr; MMR reranking supports only a knn or neural query as the top-level query of a search");
		cases.put("probe: update items values [ { id: 1, note: \"a\" } ] where price gt 1",
				"update from records with a filter, a slice, field clauses, if-version or mode replace; bulk update operations address documents by id, and each record carries the values it merges");
		cases.put("probe: scroll in items limit 10 slice 2 of 2 output [title]",
				"slice 2 of 2; a slice number is at least 0 and below the slice count");
		cases.put("probe: hybrid-search in items top 2 fusion rrf group-by price sort { sort-by price asc } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"sort with group-by; groups follow the relevance of their best hit");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding within tags output [title]",
				"the 'within' clause; has no OpenSearch mapping yet (a knn query on a knn_vector field inside a nested field)");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding prefetch { stage limit 5 } output [title]",
				"the 'prefetch' clause; has no OpenSearch mapping yet (a rescore query over the stage candidates)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv",
				"import format csv; has no OpenSearch mapping yet (CSV records in the runtime)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; has no OpenSearch mapping yet (documents indexed without _id get generated ids)");
		cases.put("probe: create collection limited { primary id field id type int field tags type array element-type varchar field-limit tagLimit array_len = 8 vector-field v type float_vector dim 2 }",
				"field limits (array_len); has no OpenSearch mapping yet (an ingest pipeline with fail processors)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]",
				"bounds on a hybrid leg; has no OpenSearch mapping yet (a score condition on the leg in the runtime)");
		cases.put("probe: create items if-not-exists with { create payload-index lowerNote on note kind keyword lowercase }",
				"lowercase payload indexes; has no OpenSearch mapping yet (a keyword field with a lowercase normalizer)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey().lines().reduce((first, last) -> last).orElseThrow(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"opensearch\", \"" + entry.getValue() + "\")";
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
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ opensearch ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new OpenSearchGenerator().program(script);
			OpenSearchManifest manifest = OpenSearchManifest.load();
			for (OpenSearchPrinter printer : List.<OpenSearchPrinter>of(new OpenSearchPython(), new OpenSearchTypeScript(), new OpenSearchJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAnOpenSearchMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(OpenSearchStatements.class.getDeclaredMethods())
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
			reached(new OpenSearchStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new OpenSearchStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(OpenSearchStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(OpenSearchStatements.NOT_MAPPED)) {
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
			if (key.startsWith(OpenSearchGenerator.VENDOR)) {
				files.put(key.substring(OpenSearchGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
