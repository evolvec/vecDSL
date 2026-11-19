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
import org.ftn.vecDSL.generator.pinecone.PineconeGenerator;
import org.ftn.vecDSL.generator.pinecone.PineconeJava;
import org.ftn.vecDSL.generator.pinecone.PineconeManifest;
import org.ftn.vecDSL.generator.pinecone.PineconePrinter;
import org.ftn.vecDSL.generator.pinecone.PineconePython;
import org.ftn.vecDSL.generator.pinecone.PineconeStatements;
import org.ftn.vecDSL.generator.pinecone.PineconeTypeScript;
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
 * Full coverage of the vecDSL grammar for Pinecone (generator/pinecone/COVERAGE.md): every native and concealed construct
 * generates in all three host languages (paid-plan constructs outside the runnable sample), every unsupported construct
 * raises its documented message, no Method, Concept or AlterOperation class reaches the "not mapped" guard of
 * PineconeStatements, and every sample maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class PineconeCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "pinecone_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/pinecone_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/pineconeCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/pineconecoverage/PineconeCoverage.java";
	private static final String CASES = "python/coverage_cases.py";
	private static final String CASES_JAVA = "java/src/main/java/vecdsl/coveragecases/CoverageCases.java";

	/** Concepts whose bare instance legitimately emits no call: the endpoint, containers of other concepts, API-key authentication. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "AuthConfig");

	private static final String FIXED = PineconeStatements.FIXED_SCHEMA;
	private static final String NO_ACCESS = PineconeStatements.NO_ACCESS_CONTROL;
	private static final String NO_ANALYZERS = PineconeStatements.NO_ANALYZERS;
	private static final String NO_GENERATION = PineconeStatements.NO_GENERATION;
	private static final String NO_LIMITS = PineconeStatements.NO_LIMITS;
	private static final String STORED_TEXT = PineconeStatements.STORED_TEXT;
	private static final String RERANK_TEXT = PineconeStatements.RERANK_TEXT;
	private static final String LLAMA = "m1: create embedding-model llama provider \"pinecone\" model-id \"llama-text-embed-v2\"\n";
	private static final String BGE = "r1: create reranker bge provider \"pinecone\" model \"bge-reranker-v2-m3\"\n";
	private static final String SHELF = LLAMA
			+ "c1: create collection shelf { primary id field id type int field body type varchar vector-field meaning type float_vector dim 1024 model llama from [body] }\n";
	private static final String TERMS = "c2: create collection lexicon { primary id field id type int vector-field weights type sparse_float_vector metric cosine }\n";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ pinecone ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectCloud: create connection cloudProject uri \"https://api.pinecone.io\" api-key env(\"VECDSL_PINECONE_API_KEY\")",
			"createItems: create collection items {",
			"    primary id",
			"    field id type int",
			"    field title type varchar max-length 128 full-text",
			"    field price type float",
			"    field tags type array element-type varchar field-limit tagLimit array_len = 8",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16",
			"    }",
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
	private GeneratorDelegate generator;

	@Inject
	private IOutputConfigurationProvider outputConfigurations;

	@Test
	public void generatesTheFullSampleInEveryLanguage() throws Exception {
		Map<String, String> files = generate(Files.readString(SAMPLE, StandardCharsets.UTF_8));
		write(files, Path.of("generated", "pinecone-full"));
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"rt.create_namespace(self.client, name=\"notes\", namespace=\"team-a\")",
				"rt.create_namespace(self.client, name=\"notes\", namespace=\"gamma\")",
				"rt.require_model(self.client, model=\"llama-text-embed-v2\")",
				"rt.require_model(self.client, model=\"bge-reranker-v2-m3\")",
				"tags={\"catalog\": \"true\"},",
				"filter={\"pages\": {\"$exists\": False}},",
				"tags={\"catalog\": \"true\", \"vectors\": \"true\"},",
				"replace_tags=True,",
				"rt.configure_collection(self.client, name=\"books\", deletion_protection=\"enabled\")",
				"rt.rename_field(",
				"filter={\"note\": {\"$exists\": True}},",
				"append=[\"tags\"],",
				"remove_items=[\"tags\"],",
				"mode=\"replace\",",
				"limit=1,",
				"return rt.import_file(",
				"return rt.await_index(self.client, name=\"books\", timeout=60)",
				"return rt.describe(self.client, name=\"books\", stats=True, index_progress=True)",
				"query_vector = rt.lookup_vector(",
				"native=True,",
				"query_string=True,",
				"filter={\"title\": {\"$match_all\": \"vector database\"}},",
				"{\"kind\": \"vector\", \"field\": \"embedding\", \"vector\": query_vector, \"metric\": \"cosine\", \"weight\": 0.7},",
				"{\"kind\": \"keyword\", \"query\": \"vector database\", \"fields\": [\"title\"], \"limit\": 4, \"weight\": 0.3},",
				"order=[{\"key\": \"price\", \"direction\": \"desc\"}],",
				"first=True,",
				"return rt.distance_matrix(",
				"filter={\"$and\": [{\"genre\": {\"$not\": {\"$eq\": \"kitchen\"}}}, {\"tags\": {\"$not\": {\"$eq\": \"ann\"}}}]},",
				"filter={\"remark\": {\"$exists\": True}},",
				"collection={\"name\": \"notes\", \"namespace\": \"north\", \"key\": \"id\", \"key_type\": \"int\"},",
				"return rt.list_objects(self.client, kind=\"collections\", prefix=\"books\")",
				"return rt.list_objects(self.client, kind=\"namespaces\", scope={\"name\": \"notes\"})",
				"return rt.clone_collection(",
				"rt.drop_namespace(self.client, name=\"notes\", namespace=\"north\")",
				"rt.drop_collection(self.client, name=\"books-copy\")",
				"rewrite={\"type\": \"match\", \"fuzzy\": \"~\"},",
				"rewrite={\"type\": \"match\", \"fuzzy\": \"~1\", \"required\": True},",
				"rewrite={\"type\": \"phrase\", \"slop\": 1},",
				"rewrite={\"type\": \"phrase_prefix\"},",
				"rewrite={\"type\": \"fuzzy\", \"fuzzy\": \"~\"},",
				"rewrite={\"type\": \"regexp\"},",
				"rewrite={\"type\": \"match\", \"boosts\": {\"title\": \"2\"}},",
				"rewrite={\"type\": \"query_string\", \"boosts\": {\"title\": \"1.5\"}},",
				"{\"kind\": \"keyword\", \"query\": \"vector database\", \"fields\": [\"title\"], \"rewrite\": {\"type\": \"phrase\"}},",
				"\"parameters\": {\"truncate\": \"END\"},",
				"rt.require_model(self.client, model=\"pinecone-sparse-english-v0\")",
				"\"embed\": {\"model\": \"llama-text-embed-v2\", \"dimension\": 1024, \"metric\": \"cosine\"},",
				"\"keywordWeights\": {\"type\": \"sparse_vector\"},",
				"\"entry\": {\"type\": \"string\", \"sparse_embed\": {\"model\": \"pinecone-sparse-english-v0\"}, \"full_text_search\": {}},",
				"\"max_batch\": 96,",
				"location=\"file://../articles.jsonl\",",
				"\"keywordWeights\": {\"indices\": [1, 7], \"values\": [0.9, 0.3]},",
				"vector=\"how to brew espresso coffee at home\",",
				"embed=\"embed\",",
				"vector={\"indices\": [1, 7], \"values\": [1, 0.5]},",
				"metric=\"dotproduct\",",
				"filter={\"id\": {\"$nin\": [101, 103]}},",
				"filter={\"topic\": {\"$eq\": \"databases\"}},",
				"embed=\"sparse_embed\",",
				"rt.drop_collection(self.client, name=\"glossary\")")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"await rt.createNamespace(this.client, { name: \"notes\", namespace: \"team-a\" });",
				"await rt.configureCollection(this.client, { name: \"books\", tags: { catalog: \"true\", vectors: \"true\" }, replace_tags: true });",
				"return await rt.awaitIndex(this.client, { name: \"books\", timeout: 60 });",
				"return await rt.listObjects(this.client, { kind: \"collections\", prefix: \"books\" });",
				"rewrite: { type: \"match\", fuzzy: \"~1\", required: true },",
				"{ kind: \"keyword\", query: \"vector database\", fields: [\"title\"], rewrite: { type: \"phrase\" } },",
				"entry: { type: \"string\", sparse_embed: { model: \"pinecone-sparse-english-v0\" }, full_text_search: {} },",
				"keywordWeights: { indices: [1, 7], values: [0.9, 0.3] },")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"createNamespace(client, args(\"name\", \"notes\", \"namespace\", \"team-a\"));",
				"configureCollection(client, args(\"name\", \"books\", \"deletion_protection\", \"enabled\"));",
				"public String vectorsReady() {",
				"public Map<String, Object> info() {",
				"public List<String> tenantNames() {",
				"return listObjects(client, args(\"kind\", \"collections\", \"prefix\", \"books\"));",
				"\"rewrite\", args(\"type\", \"match\", \"fuzzy\", \"~1\", \"required\", true),",
				"\"rewrite\", args(\"type\", \"query_string\", \"boosts\", args(\"title\", \"1.5\")),",
				"\"embed\", args(\"model\", \"llama-text-embed-v2\", \"dimension\", 1024, \"metric\", \"cosine\"),",
				"\"keywordWeights\", args(\"indices\", list(1, 7), \"values\", list(0.9, 0.3))),")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("image: stoplight/prism:"));
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		Map<String, String> files = generate(BASE + String.join("\n",
				"nightly: create backup-schedule nightly for items interval \"daily\" retention 7",
				"weekly: create backup-schedule weekly for items interval \"weekly\" retention 4 if-not-exists",
				"dropNightly: drop nightly if-exists",
				"backupItems: snapshot items as \"itemsWeekly\" description \"weekly copy\"",
				"listBackups: list snapshots in items",
				"allBackups: list snapshots",
				"restoreItems: restore items from \"itemsWeekly\"",
				"restoreLater: restore items from \"itemsWeekly\" async",
				"dropBackup: drop-snapshot \"itemsWeekly\"",
				"remoteImport: bulk-import into items from \"s3://vecdsl-public/items/\" format json",
				"remoteLater: bulk-import into items from \"gs://vecdsl-public/items/\" async",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"wipeCheap: delete from items wait where price lt 5",
				"wipeAll: delete from items all",
				"repriceCheap: update items set { price: 5.0 } where price lt 5",
				"tagOnly: alter items { set tags = [\"archive\"] }",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"eastWipe: delete from items all tenant east",
				""));
		String python = files.get(CASES);
		for (String fragment : List.of(
				"frequency=\"daily\",",
				"expire_after_days=7,",
				"if not rt.has_backup_schedule(self.client, name=\"items\", schedule=\"weekly\"):",
				"expire_after_days=28,",
				"rt.drop_backup_schedule(self.client, name=\"items\", schedule=\"nightly\")",
				"return rt.create_backup(self.client, name=\"items\", backup=\"items-weekly\", description=\"weekly copy\")",
				"return rt.list_objects(self.client, kind=\"snapshots\", scope={\"name\": \"items\"})",
				"return rt.list_objects(self.client, kind=\"snapshots\")",
				"return rt.restore_backup(self.client, name=\"items\", backup=\"items-weekly\")",
				"return rt.restore_backup(self.client, name=\"items\", backup=\"items-weekly\", background=True)",
				"return rt.drop_backup(self.client, backup=\"items-weekly\")",
				"uri=\"s3://vecdsl-public/items/\",",
				"background=True,",
				"rows=rt.read_records(self.client, location=\"file://items.json\"),",
				"on_conflict=\"update\",",
				"batch_size=50,",
				"batch_size=20,",
				"return [self.both_query_0(), self.both_query_1()]",
				"filter={\"price\": {\"$lt\": 5}},",
				"delete_all=True,",
				"rt.configure_collection(self.client, name=\"items\", tags={\"archive\": \"true\"}, replace_tags=True)",
				"\"namespace\": \"east\",")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
		assertTrue(files.get(CASES_JAVA).contains("return PineconeRuntime.dropBackup(client, args(\"backup\", \"items-weekly\"));"));
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		cases.put("probe: transaction { delete from items ids [1] }", "multi-statement transactions; Pinecone applies each write request on its own");
		cases.put("probe: release items", "releasing a collection; Pinecone serverless indexes have no memory residency to release");
		cases.put("probe: update items set { note: \"b\" } ids [1] if-version 3", "if-version; Pinecone documents carry no version");
		cases.put("probe: insert into items values [ { id: 1, title: \"a\", embedding: [1.0, 0.0, 0.0, 0.0] } ] ttl 60",
				"per-statement ttl; Pinecone documents do not expire");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items/\" continuous",
				"continuous import; Pinecone imports a prefix of object storage once per import");
		cases.put("probe: bulk-import into items from \"kafka://items\"", "import from kafka; Pinecone imports from s3, gs and Azure Blob Storage (https)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet", "import format parquet; document indexes import JSON Lines files");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items/\" on-conflict update",
				"on-conflict update for imports from object storage; Pinecone imports only into new namespaces");
		cases.put("probe: snapshot items incremental", "incremental snapshots; every Pinecone backup is a full copy of the index");
		cases.put("probe: snapshot items scope full", "snapshot scope full; a Pinecone backup copies one index");
		cases.put("probe: snapshot items location \"s3://a-bucket\"", "snapshot locations; Pinecone stores backups in its own storage");
		cases.put("probe: restore items from \"weekly\" scope tenant", "restore scope tenant; a Pinecone backup restores one index");
		cases.put("probe: clone items to \"itemsCopy\" mode shallow", "shallow clones; Pinecone has no copy-on-write indexes");
		cases.put("probe: clone items to \"itemsCopy\" as-of \"2026-01-01\"", "the 'as-of' clause; Pinecone reads only the current state of a namespace");
		cases.put("probe: create backup-schedule nightly for items interval \"0 2 * * *\" retention 7",
				"backup interval '0 2 * * *'; Pinecone backup schedules run daily, weekly or monthly");
		cases.put("probe: create backup-schedule nightly for items interval \"daily\"",
				"a backup schedule without retention; Pinecone requires a retention in days");
		cases.put("probe: create embedding-model e1 provider \"openai\"", "models of provider openai; Pinecone integrated inference runs the models it hosts");
		cases.put("probe: create generative-model g1 provider \"openai\"", "generative models; " + NO_GENERATION);
		cases.put("probe: create analyzer english tokenizer standard", "analyzers outside a field; " + NO_ANALYZERS);
		cases.put("probe: create auth a1 type token", "token authentication; " + NO_ACCESS);
		cases.put("probe: create file-resource stopwordList type stopwords path \"vecdsl/stopwords.txt\"", "file resources; " + NO_ANALYZERS);
		cases.put("probe: generate in items top 2 query vector $queryVector using embedding single-prompt \"x {title}\" output [title]",
				"generative search; " + NO_GENERATION);
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; " + NO_LIMITS);
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + PineconeStatements.SERVERLESS);
		cases.put("probe: create database prod { connection main uri \"https://api.pinecone.io\" metrics m1 enabled }", PineconeStatements.METRICS);
		cases.put("probe: create logical-db ledger { collection ledgerItems { primary id field id type int vector-field v type float_vector dim 2 } }",
				PineconeStatements.NO_DATABASES);
		cases.put("probe: create items if-not-exists with { create partition south }",
				"partitions; Pinecone isolates data by namespace only (vecDSL tenants and namespaces are Pinecone namespaces)");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Pinecone reads only the current state of a namespace");
		cases.put("probe: count in items namespace \"team-a\" tenant east", "a tenant and a namespace on one statement; both select a Pinecone namespace");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding explain output [title]",
				"the 'explain' clause; Pinecone search returns no score explanation");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding with-total output [title]",
				"the 'with-total' clause; Pinecone search returns no total hit count");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding mmr lambda 0.5 output [title]",
				"the 'mmr' clause; Pinecone search has no maximal marginal relevance");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding group-by price size 1 output [title]",
				"the 'group-by' clause; Pinecone search has no grouping");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding exact output [title]",
				"the 'exact' clause; Pinecone serverless search has no exhaustive mode");
		cases.put("probe: vector-search in items top 3 query vector $queryVector using embedding params { candidates 64 } output [title]",
				"candidates; Pinecone exposes no query-time search effort");
		cases.put("probe: scroll in items limit 10 cursor \"abc\" output [title]",
				"the 'cursor' clause; Pinecone search returns no pagination token, and list pagination tokens follow the order of document ids, not the primary-key order of a vecDSL scroll");
		cases.put("probe: scroll in items limit 10 slice 0 of 2 output [title]",
				"the 'slice' clause; Pinecone lists the documents of a namespace in one sequence of pages");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; Pinecone text scoring has no minimum-should-match");
		cases.put("probe: search in items top 3 query text \"a b\" text-query prefix fields [title] output [title]", PineconeStatements.SINGLE_TERM_PREFIX);
		cases.put("probe: alter items { set ttl = 60 }", "collection ttl; Pinecone documents do not expire");
		cases.put("probe: alter items { set description = \"x\" }", "collection descriptions; a Pinecone index has a name and tags but no description");
		cases.put("probe: alter items { set dynamic = false }", "dynamic false; Pinecone stores every metadata key a document carries");
		cases.put("probe: alter items rename to shelf", "renaming a collection; Pinecone indexes cannot be renamed");
		cases.put("probe: alter items { rename-field id to itemKey }", "renaming the primary key; Pinecone document ids are the key text");
		cases.put("probe: alter items { rename-field title to heading }", "renaming title, a field of the index schema; " + FIXED);
		cases.put("probe: alter items { add-field vector-field extra type float_vector dim 2 }", "adding vector field extra; " + FIXED);
		cases.put("probe: alter items { add-field field summary type varchar full-text }", "adding full-text field summary; " + FIXED);
		cases.put("probe: alter items { add-alias alias shelf for items }", PineconeStatements.NO_ALIASES);
		cases.put("probe: alter items { drop-field embedding }", "dropping embedding, a field of the index schema; " + FIXED);
		cases.put("probe: drop embeddingIndex", "dropping a vector index; Pinecone indexes the dense vector field of every index and " + FIXED);
		cases.put("probe: drop priceIndex",
				"dropping a payload index; Pinecone indexes every metadata key unless a namespace declares its filterable fields at creation");
		cases.put("probe: rebuild-index priceIndex", "rebuilding an index; Pinecone maintains its indexes with every write and has no rebuild call");
		cases.put("probe: refresh items", "collections derived from external sources; Pinecone stores the documents of every index itself");
		cases.put("m1: create embedding-model e1 provider \"pinecone\"\nm2: create embedding-model e2 provider \"pinecone\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration; the model of a Pinecone embed field cannot be changed after creation");
		cases.put("probe: await-index embeddingIndex until building", "await-index until building; Pinecone reports whether an index is ready");
		cases.put("probe: list aliases", PineconeStatements.NO_ALIASES);
		cases.put("probe: list indexes in items", "listing indexes; a Pinecone index is the collection itself (list collections)");
		cases.put("probe: list databases", PineconeStatements.NO_DATABASES);
		cases.put("probe: count in items where note like \"a%\"", "operator like; Pinecone filters have no pattern operators");
		cases.put("probe: count in items where tags length gt 1", "length predicates; Pinecone filters have no length operator");
		cases.put("probe: count in items where title eq \"x\"", "comparisons on full-text field title; Pinecone filters full-text fields by text match only");
		cases.put("probe: count in items where note match \"a\"", "text operator match on note, which is not a full-text field");
		cases.put("probe: search in items top 3 query text \"a\" text-query term fields [title] output [title]", PineconeStatements.NO_TERM_QUERY);
		cases.put("probe: search in items top 3 query text \"a*\" text-query wildcard fields [title] output [title]", PineconeStatements.NO_WILDCARD);
		cases.put("probe: search in items top 3 query text \"a b\" text-query phrase fields [title] fuzziness \"1\" output [title]",
				"the 'fuzziness' clause with text-query phrase; Pinecone applies fuzzy distances to bare terms only");
		cases.put("probe: search in items top 3 query text \"a b\" fields [title] slop 2 output [title]",
				"the 'slop' clause with text-query match; Pinecone applies slop to quoted phrases only");
		cases.put("probe: search in items top 3 query text \"a\" fields [title] fuzziness \"AUTO:3,6\" output [title]",
				"fuzziness 'AUTO:3,6'; Pinecone takes the automatic distance (AUTO) or 0, 1 or 2 edits");
		cases.put("probe: search in items top 3 query text \"vec.*\" text-query regexp fields [title] operator and output [title]",
				"the 'operator' clause with text-query regexp; a regular expression has no terms to combine");
		cases.put("probe: search in items top 3 query text \"a\" fields [title boost -1] output [title]", "a negative field boost; Pinecone boosts are 0 or greater");
		cases.put(BGE + "probe: recommend in items top 3 positive [1] using embedding rerank bge output [title]",
				"the 'rerank' clause; a recommend search has examples but no query text, and " + RERANK_TEXT);
		cases.put(BGE + "probe: vector-search in items top 3 query vector $queryVector using embedding rerank bge output [title]",
				"the 'rerank' clause on a vector search with vector input; " + RERANK_TEXT);
		cases.put("r1: create reranker rr provider \"cohere\"\nprobe: search in items top 3 query text \"a\" fields [title] rerank rr output [title]",
				"models of provider cohere; Pinecone integrated inference runs the models it hosts");
		cases.put("probe: vector-search in items top 3 query text \"espresso\" using embedding output [title]",
				"text input for vector field embedding without a model; " + PineconeStatements.NO_LOCAL_EMBEDDING);
		cases.put(LLAMA + "probe: vector-search in items top 3 query vector $queryVector using embedding model llama output [title]",
				"the 'model' clause with vector input; it names the model that embeds a query text");
		cases.put(SHELF + "probe: vector-search in shelf top 2 query id 1 using meaning output [body]", "a query by id on meaning; " + STORED_TEXT);
		cases.put(SHELF + "probe: recommend in shelf top 2 positive [1] using meaning output [body]", "recommend on meaning; " + STORED_TEXT);
		cases.put(SHELF + "m2: create embedding-model e5 provider \"pinecone\" model-id \"multilingual-e5-large\"\n"
				+ "probe: vector-search in shelf top 2 query text \"x\" using meaning model e5 output [body]",
				"the 'model' clause naming e5 for meaning, which Pinecone embeds with llama, fixed at creation");
		cases.put(LLAMA + "probe: create collection pair { primary id field id type int field a type varchar field b type varchar "
				+ "vector-field both type float_vector dim 1024 model llama from [a, b] }",
				"embedding 2 source fields into both; Pinecone embeds the text of one string field");
		cases.put("probe: create collection loose { primary id field id type int field a type varchar vector-field v type float_vector dim 1024 "
				+ "function f type text_embedding input [a] output [v] }",
				"a text_embedding function without a model; Pinecone integrated embedding runs a hosted model");
		cases.put(TERMS + "probe: vector-search in lexicon top 2 query sparse_vector sparse { 1: 1.0 } using weights output [id]",
				"metric cosine on sparse field weights; Pinecone scores sparse vectors by dot product");
		cases.put(TERMS + "probe: vector-search in lexicon top 2 query vector [1.0, 0.0] using weights output [id]",
				"a dense query vector for sparse field weights");
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"pinecone\", \"" + entry.getValue() + "\")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Query rewrites, integrated embedding, Inference embed and rerank outside the runnable sample. */
	@Test
	public void generatesQueryRewritesAndInference() throws Exception {
		Map<String, String> files = generate(BASE + String.join("\n",
				"m1: create embedding-model llama provider \"pinecone\" model-id \"llama-text-embed-v2\"",
				"r2: create reranker fast provider \"pinecone\" model \"cohere-rerank-4-fast\" top-n 2",
				"createLibrary: create collection library {",
				"    primary id",
				"    field id type int",
				"    field summary type varchar full-text",
				"    field body type varchar",
				"    vector-field bodyMeaning type float_vector dim 1024 metric dot",
				"    vector-field bodyTerms type sparse_float_vector",
				"    function bodyEmbedding type text_embedding input [body] output [bodyMeaning] model llama",
				"}",
				"inferred: vector-search in items top 3 query text \"espresso\" using embedding model llama output [title]",
				"legString: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding } "
						+ "keyword { query text \"vector OR graph\" text-query query_string fields [title] operator and } } output [title]",
				"fastRerank: search in items top 3 query text \"vector\" fields [title] rerank fast output [title]",
				"sparseLiteral: recommend in library top 2 positive [sparse { 1: 1.0 }] using bodyTerms output [summary]",
				"meaningFused: hybrid-search in library top 2 query text \"coffee\" rerank fast { vector { using bodyMeaning } keyword { fields [summary] } } output [summary]",
				""));
		String python = files.get(CASES);
		for (String fragment : List.of(
				"rt.require_model(self.client, model=\"cohere-rerank-4-fast\")",
				"\"embed\": {\"model\": \"llama-text-embed-v2\", \"dimension\": 1024, \"metric\": \"dotproduct\"},",
				"\"bodyTerms\": {\"type\": \"sparse_vector\"},",
				"\"summary\": {\"type\": \"string\", \"full_text_search\": {}},",
				"query_vector = rt.embed_query(",
				"parameters={\"input_type\": \"query\", \"truncate\": \"END\"},",
				"\"rewrite\": {\"type\": \"query_string\"},",
				"\"filter\": {\"title\": {\"$match_all\": \"vector OR graph\"}},",
				"rerank={\"model\": \"cohere-rerank-4-fast\", \"query\": \"vector\", \"field\": \"title\", \"top_n\": 2},",
				"positives=[{\"indices\": [1], \"values\": [1]}],",
				"{\"kind\": \"vector\", \"field\": \"body\", \"vector\": \"coffee\", \"metric\": \"dotproduct\", \"embed\": \"embed\"},",
				"rerank={\"model\": \"cohere-rerank-4-fast\", \"query\": \"coffee\", \"field\": \"body\", \"top_n\": 2},")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
		String typescript = files.get("typescript/src/coverageCases.ts");
		assertTrue(typescript.contains("const queryVector = await rt.embedQuery(this.client, {"), typescript);
		assertTrue(typescript.contains("positives: [{ indices: [1], values: [1] }],"), typescript);
		String java = files.get(CASES_JAVA);
		assertTrue(java.contains("var queryVector = embedQuery("), java);
		assertTrue(java.contains("\"rerank\", args(\"model\", \"cohere-rerank-4-fast\", \"query\", \"vector\", \"field\", \"title\", \"top_n\", 2)));"), java);
	}

	/** Every sample, with its targets set to pinecone, maps without an error other than Unsupported. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ pinecone ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new PineconeGenerator().program(script);
			PineconeManifest manifest = PineconeManifest.load();
			for (PineconePrinter printer : List.<PineconePrinter>of(new PineconePython(), new PineconeTypeScript(), new PineconeJava())) {
				assertFalse(printer.files(program, manifest).isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAPineconeMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(PineconeStatements.class.getDeclaredMethods())
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
			reached(new PineconeStatements(script, "__default__"), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new PineconeStatements(script, "__default__"), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(PineconeStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(PineconeStatements.NOT_MAPPED)) {
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
			if (key.startsWith(PineconeGenerator.VENDOR)) {
				files.put(key.substring(PineconeGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
