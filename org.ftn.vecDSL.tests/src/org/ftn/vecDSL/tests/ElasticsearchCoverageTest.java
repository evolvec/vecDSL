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
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchGenerator;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchJava;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchManifest;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchPrinter;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchPython;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchStatements;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchTypeScript;
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
 * Full coverage of the vecDSL grammar for Elasticsearch (generator/elasticsearch/COVERAGE.md): every native and concealed
 * construct generates in all three host languages, every unsupported construct raises its documented reason, constructs not
 * generated yet raise "has no Elasticsearch mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped"
 * guard of ElasticsearchStatements, and every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ElasticsearchCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "elasticsearch_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/elasticsearch_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/elasticsearchCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/elasticsearchcoverage/ElasticsearchCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance emits no request: the endpoint, containers of other concepts, and authentication without roles. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig", "Database");

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ elasticsearch ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://127.0.0.1:9200\" username \"elastic\" password env(\"VECDSL_ELASTICSEARCH_PASSWORD\")",
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
		Path output = Path.of("generated", "elasticsearch-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), ElasticsearchManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
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
				"\"index.mapping.total_fields.limit\": 200,",
				"\"_meta\": {\"description\": \"Books about databases\"},",
				"\"index_options\": {\"type\": \"hnsw\", \"m\": 16, \"ef_construction\": 200},",
				"path=\"/_security/role/vecdsl_reader\",",
				"body={\"_meta\": {\"description\": \"Books about vector databases\"}},",
				"return rt.import_file(",
				"return rt.update_if_version(",
				"\"filter\": [{\"bool\": {\"filter\": [{\"term\": {\"genre\": \"kitchen\"}}, {\"range\": {\"price\": {\"gt\": 100}}}]}}],",
				"extra={\"vecdsl_tenant\": \"north\"},",
				"\"index_options\": {\"type\": \"flat\"},",
				"body={\"size\": 0, \"aggs\": {\"names\": {\"terms\": {\"field\": \"vecdsl_partition\", \"size\": 10000}}}},",
				"\"settings\": {\"number_of_shards\": 1, \"number_of_replicas\": 0, \"index.store.type\": \"mmapfs\"},",
				"\"element_type\": \"bfloat16\",",
				"\"element_type\": \"byte\",",
				"\"element_type\": \"bit\",",
				"\"sparseTerms\": {\"type\": \"sparse_vector\", \"index_options\": {\"prune\": False}},",
				"\"index_options\": {\"type\": \"int8_hnsw\", \"rescore_vector\": {\"oversample\": 4}},",
				"\"query\": {\"bool\": {\"must\": [{\"sparse_vector\": {\"field\": \"sparseTerms\", \"query_vector\": {\"0\": 1}}}]}},",
				"\"knn\": {\"field\": \"flags\", \"query_vector\": [-78], \"k\": 2},",
				"\"knn\": {\"field\": \"coarse\", \"query_vector\": query_vector, \"k\": 2, \"rescore_vector\": {\"oversample\": 4}},",
				"return rt.request(self.client, method=\"POST\", path=\"/logs/_close\", select=[\"acknowledged\"])",
				"rt.request(self.client, method=\"POST\", path=\"/books/_flush\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_forcemerge\", params={\"max_num_segments\": 1})",
				"rt.request(self.client, method=\"GET\", path=\"/books/_stats\", select=[\"_all\", \"primaries\"]),",
				"rt.request(self.client, method=\"GET\", path=\"/_cluster/health/books\", select=[\"status\"]),",
				"\"_source\": {\"includes\": [\"id\", \"title\", \"embedding\"], \"exclude_vectors\": False},",
				"\"knn\": {\"field\": \"embedding\", \"query_vector\": query_vector, \"k\": 3, \"num_candidates\": 50},",
				"\"retriever\": {\"knn\": {\"field\": \"embedding\", \"query_vector\": query_vector, \"k\": 2, \"num_candidates\": 80}},",
				"\"inner_hits\": {\"name\": \"group\", \"size\": 2, \"_source\": {\"includes\": [\"id\", \"title\"], \"exclude_vectors\": True}},",
				"\"aggs\": {\"meanPrice\": {\"avg\": {\"field\": \"price\"}}},",
				"\"query\": {\"bool\": {\"must\": [{\"match_phrase\": {\"title\": {\"query\": \"databases explained\", \"slop\": 1}}}]}},",
				"\"query\": {\"bool\": {\"must\": [{\"match\": {\"title\": {\"query\": \"databse\", \"fuzziness\": \"AUTO\"}}}]}},",
				"\"query\": {\"bool\": {\"must\": [{\"prefix\": {\"title\": {\"value\": \"vec\"}}}]}},",
				"\"bool\": {\"must\": [{\"query_string\": {\"query\": \"title:graph OR title:espresso\", \"fields\": [\"title\"]}}]},",
				"\"must\": [{\"multi_match\": {\"query\": \"databases\", \"fields\": [\"title^2\", \"genre\"], \"type\": \"most_fields\"}}],",
				"\"highlight\": {\"fields\": {\"title\": {}}},",
				"return rt.hybrid(",
				"return rt.recommend(",
				"\"search_after\": [3],",
				"\"query\": {\"function_score\": {\"query\": {\"match_all\": {}}, \"random_score\": {}, \"boost_mode\": \"replace\"}},",
				"return rt.distance_matrix(",
				"\"bool\": {\"filter\": [{\"geo_distance\": {\"distance\": \"50km\", \"place\": {\"lat\": 45.25, \"lon\": 19.85}}}]},",
				"path=\"/_snapshot/vecdsl/books_nightly\",",
				"return rt.names(self.client, kind=\"snapshots\", path=\"/_snapshot/vecdsl/_all\", ignore=[404])",
				"path=\"/_snapshot/vecdsl/books_nightly/_restore\",",
				"path=\"/_slm/policy/nightly\",",
				"rt.request(self.client, method=\"POST\", path=\"/books/_clone/books_copy\")",
				"rt.request(self.client, method=\"POST\", path=\"/books/_clone/shelf\", body={\"aliases\": {}})",
				"return rt.names(self.client, kind=\"indices\", path=\"/_cat/indices/catalog.*\", strip=\"catalog.\")",
				"filters=[{\"terms\": {\"vecdsl_partition\": [\"hot\"]}}],",
				"filters=[{\"term\": {\"vecdsl_tenant\": \"north\"}}],",
				"filters=[{\"term\": {\"vecdsl_namespace\": \"team-a\"}}],",
				"\"query\": {\"knn\": {\"field\": \"embedding\", \"query_vector\": query_vector, \"k\": 1000, \"num_candidates\": 1000}},",
				"\"sort\": [{\"_score\": {\"order\": \"desc\"}}, {\"id\": {\"order\": \"asc\"}}],",
				"after=2,",
				"after=1,",
				"\"boost_mode\": \"sum\",",
				"\"docvalue_fields\": [\"genre\"],",
				"group_size=2,",
				"mmr={\"field\": \"embedding\", \"kind\": \"cosine\", \"lambda\": 0.5, \"window\": 6},",
				"sort=[{\"price\": {\"order\": \"asc\", \"missing\": \"_last\"}}],",
				"total=True,",
				"aggs={\"meanPrice\": {\"avg\": {\"field\": \"price\"}}},",
				"\"explain\": True,",
				"\"query_vector\": None,",
				"\"aggs\": {\"topPrice\": {\"max\": {\"field\": \"price\"}}},",
				"\"slice\": {\"id\": 1, \"max\": 2},",
				"op=\"patch\",",
				"only=[5, 9],")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"await rt.dropMatching(this.client, { pattern: \"catalog.*\" });",
				"return await rt.importFile(this.client, {",
				"return await rt.updateIfVersion(this.client, {",
				"async vectorsReady() {",
				"path: \"/_snapshot/vecdsl/books_nightly/_restore\",",
				"after: 2,",
				"filters: [{ terms: { vecdsl_partition: [\"hot\"] } }],",
				"op: \"patch\",")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"ElasticsearchRuntime.dropMatching(client, args(\"pattern\", \"catalog.*\"));",
				"return ElasticsearchRuntime.updateIfVersion(",
				"public Object vectorsReady() {",
				"public Object pairs() {",
				"\"after\", 2));",
				"\"filters\", list(args(\"terms\", args(\"vecdsl_partition\", list(\"hot\"))))));")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("path.repo: /usr/share/elasticsearch/snapshots"));
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
				"nearGroups: vector-search in items top 2 query id 1 using embedding group-by price output [title]",
				"nearDiverse: vector-search in items top 2 query id 1 using embedding mmr lambda 0.5 output [title]",
				"nearExact: vector-search in items top 2 query id 1 using embedding exact output [title]",
				"boostedExact: vector-search in items top 2 query vector $queryVector using embedding exact boost { boost-fn kind field_value field price } output [title]",
				"sparseAfter: search in items top 2 query text \"a\" fields [title] cursor \"4\" tenant east output [title]",
				"eastById: get from items ids [1] tenant east output [title]",
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
				"\"excludes\": [\"note\"]",
				"vectors=True",
				"\"/items/_mapping\"",
				"group=\"price\",",
				"\"diversify\": {",
				"\"source\": \"(cosineSimilarity(params.query_vector, params.field) + 1.0) / 2.0\"",
				"\"function_score\": {",
				"after=4,",
				"filters=[{\"term\": {\"vecdsl_tenant\": \"east\"}}],",
				"{\"terms\": {\"vecdsl_partition\": [\"s1\"]}}",
				"extra={\"vecdsl_tenant\": \"east\", \"vecdsl_namespace\": \"ns\"},",
				"path=\"/_snapshot/backups/all/_restore\",")) {
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
				"ttl; Elasticsearch documents do not expire (index lifecycle management deletes whole indices after rollover, not single documents)");
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Elasticsearch pulls no streams itself (Logstash and Elastic Agent are separate programs)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; Elasticsearch indexes documents that a client sends, and reads object stores only as snapshot repositories");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet",
				"import format parquet; the _bulk API takes JSON documents");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"",
				"the 'as-of' clause; Elasticsearch searches the current state of an index (a point in time pins a state for paging, not a past one)");
		cases.put("probe: count in items as-of \"2026-01-01\"",
				"the 'as-of' clause; Elasticsearch searches the current state of an index (a point in time pins a state for paging, not a past one)");
		cases.put("probe: create embedding-model e1 provider \"openai\"",
				"embedding models; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors");
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; generative models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)");
		cases.put("probe: create reranker r1 provider \"cohere\"",
				"rerankers; the text_similarity_reranker retriever needs an enterprise license (TextSimilarityRankBuilder, TEXT_SIMILARITY_RERANKER_FEATURE) and an inference endpoint");
		cases.put("probe: create auth a1 type token",
				"token authentication; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)");
		cases.put("probe: create auth a2 type mtls",
				"mtls authentication; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)");
		cases.put("probe: create auth a3 type sso",
				"sso authentication; the SAML and OpenID Connect realms need a platinum license (Security, SAML_REALM_FEATURE, OIDC_REALM_FEATURE)");
		cases.put("probe: create auth a4 type username_password certificate \"client.crt\"",
				"client certificates; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)");
		cases.put("probe: create auth a5 type username_password audit-log",
				"audit logs; auditing needs a gold license (Security, AUDITING_FEATURE)");
		cases.put("probe: create connection other uri \"http://127.0.0.1:9200\" token \"t\"",
				"bearer tokens; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)");
		cases.put("probe: create connection other uri \"http://127.0.0.1:9200\" grpc-port 6334",
				"grpc-port; Elasticsearch serves its REST API over HTTP only");
		cases.put("probe: create connection lite path \"./vecdsl.db\"",
				"embedded deployments; Elasticsearch runs as a server that the clients reach over HTTP");
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; generative models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Elasticsearch limits shards per node (cluster.max_shards_per_node), not the number of indices");
		cases.put("probe: create database prod { connection main uri \"http://127.0.0.1:9200\" metrics m1 enabled }",
				"metrics configuration; Elasticsearch metrics are collected by Elastic Agent or Metricbeat from the stats APIs, not configured per program");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { target-recall 0.9 } output [title]",
				"target-recall: num_candidates and the oversampling of quantized indexes are effort settings, with no declarable recall target");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]",
				"text input; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors");
		cases.put("probe: alter items { set ttl = 60 }",
				"ttl; Elasticsearch documents do not expire (index lifecycle management deletes whole indices after rollover, not single documents)");
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; Elasticsearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)");
		cases.put("probe: alter items { rename-field id to itemKey }",
				"renaming the primary key; the generated requests address the key by its declared name");
		cases.put("probe: alter items { drop-field id }",
				"dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field vector-field wide type float64_vector dim 2 }",
				"float64_vector fields; Elasticsearch dense_vector fields hold float, bfloat16, byte (signed int8) and bit elements");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; Elasticsearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (sparse_vector fields take their weights from the client or an inference model)");
		cases.put("probe: create items if-not-exists with { create payload-index bitmapIndex on price kind float structure bitmap }",
				"bitmap payload indexes; Elasticsearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type");
		cases.put("probe: refresh items",
				"collections derived from external sources; Elasticsearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines");
		cases.put("m1: create embedding-model e1 provider \"openai\"\nm2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors");
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"",
				"file resources; Elasticsearch reads analysis files from the config directory of the node, which a client cannot write");
		cases.put("probe: transaction { delete from items ids [1] }",
				"transactions; Elasticsearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)");
		cases.put("probe: load parts partitions [p1]",
				"loading partitions; Elasticsearch opens and closes whole indices");
		cases.put("probe: release parts partitions [p1]",
				"releasing partitions; Elasticsearch opens and closes whole indices");
		cases.put("probe: snapshot items tenant east",
				"tenant snapshots; a snapshot holds whole indices");
		cases.put("probe: drop embeddingIndex",
				"dropping a vector index; the index_options of a dense_vector field are part of its mapping, which Elasticsearch cannot remove");
		cases.put("probe: drop priceIndex",
				"dropping a payload index; whether a field is indexed is part of its mapping, which Elasticsearch cannot change");
		cases.put("probe: drop english",
				"dropping an analyzer; analysis settings are removed with their index, not while fields use them");
		cases.put("probe: recommend in items top 2 positive [1] using embedding highlight [title] output [title]",
				"the 'highlight' clause on a recommendation; highlighting marks the terms of a text query, which a recommendation has not");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding highlight [title] output [title]",
				"the 'highlight' clause on a vector search; highlighting marks the terms of a text query, which a vector search has not");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding offset 1 cursor \"3\" output [title]",
				"the 'cursor' clause with offset; a cursor page continues after its record in the order of the search");
		cases.put("probe: search in items top 2 query text \"a\" fields [title] cursor \"3\" sort { sort-by price asc } output [title]",
				"the 'cursor' clause with sort; a cursor page continues after its record in the order of the search");
		cases.put("probe: recommend in items top 2 positive [1] using embedding cursor \"3\" group-by price output [title]",
				"the 'cursor' clause with group-by; a cursor page continues after its record in the order of the search");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding mmr lambda 0.5 boost { boost-fn kind field_value field price } output [title]",
				"the 'boost' clause with mmr; the generated diversify retriever ranks the similarity of its kNN retriever");
		cases.put("probe: hybrid-search in items top 2 fusion rrf group-by price sort { sort-by price asc } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"the 'group-by' clause with sort or mmr on a hybrid search; groups are ordered by their best fused hit");
		cases.put("probe: hybrid-search in items top 2 fusion rrf sort { sort-by relevance desc } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"sort by relevance on a hybrid search; the server sorts the fused page by field values, and the fused order is the relevance order");
		cases.put("probe: update items values [ { id: 1, note: \"a\" } ] where price gt 1",
				"update from records with a filter, a slice, field clauses, if-version or mode replace; bulk update operations address documents by id, and each record carries the values it merges");
		cases.put("probe: scroll in items limit 10 slice 2 of 2 output [title]",
				"slice 2 of 2; a slice number is at least 0 and below the slice count");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding within tags output [title]",
				"the 'within' clause; has no Elasticsearch mapping yet (a kNN search inside a nested field)");
		cases.put("probe: vector-search in items top 2 query vector $queryVector using embedding prefetch { stage limit 5 } output [title]",
				"the 'prefetch' clause; has no Elasticsearch mapping yet (nested retrievers)");
		cases.put("probe: hybrid-search in items top 2 fusion rrf boost { boost-fn kind field_value field price } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"the 'boost' clause on a hybrid search; has no Elasticsearch mapping yet (boost functions over the fused records)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv",
				"import format csv; has no Elasticsearch mapping yet (CSV records in the runtime)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; has no Elasticsearch mapping yet (documents indexed without _id get generated ids)");
		cases.put("probe: create collection limited { primary id field id type int field tags type array element-type varchar field-limit tagLimit array_len = 8 vector-field v type float_vector dim 2 }",
				"field limits (array_len); has no Elasticsearch mapping yet (an ingest pipeline with fail processors)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]",
				"bounds on a hybrid leg; has no Elasticsearch mapping yet (a score condition on the leg in the runtime)");
		cases.put("probe: create items if-not-exists with { create payload-index lowerNote on note kind keyword lowercase }",
				"lowercase payload indexes; has no Elasticsearch mapping yet (a keyword field with a lowercase normalizer)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey().lines().reduce((first, last) -> last).orElseThrow(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"elasticsearch\", \"" + entry.getValue() + "\")";
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
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ elasticsearch ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new ElasticsearchGenerator().program(script);
			ElasticsearchManifest manifest = ElasticsearchManifest.load();
			for (ElasticsearchPrinter printer : List.<ElasticsearchPrinter>of(new ElasticsearchPython(), new ElasticsearchTypeScript(), new ElasticsearchJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAnElasticsearchMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(ElasticsearchStatements.class.getDeclaredMethods())
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
			reached(new ElasticsearchStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new ElasticsearchStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(ElasticsearchStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(ElasticsearchStatements.NOT_MAPPED)) {
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
			if (key.startsWith(ElasticsearchGenerator.VENDOR)) {
				files.put(key.substring(ElasticsearchGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
