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
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.vespa.VespaGenerator;
import org.ftn.vecDSL.generator.vespa.VespaJava;
import org.ftn.vecDSL.generator.vespa.VespaManifest;
import org.ftn.vecDSL.generator.vespa.VespaPrinter;
import org.ftn.vecDSL.generator.vespa.VespaPython;
import org.ftn.vecDSL.generator.vespa.VespaStatements;
import org.ftn.vecDSL.generator.vespa.VespaTypeScript;
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
 * Full coverage of the vecDSL grammar for Vespa (generator/vespa/COVERAGE.md): every native and concealed construct generates in
 * all three host languages, every unsupported construct raises its documented message, constructs not generated yet raise
 * "has no Vespa mapping yet", no Method, Concept or AlterOperation class reaches the "not mapped" guard of VespaStatements, and
 * every sample of the other vendors maps without an error other than Unsupported.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VespaCoverageTest {

	private static final Path SAMPLE = Path.of("samples", "vespa_full.vecDSL");
	private static final Path SAMPLES = Path.of("samples");
	private static final String PYTHON = "python/vespa_coverage.py";
	private static final String TYPESCRIPT = "typescript/src/vespaCoverage.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/vespacoverage/VespaCoverage.java";
	private static final String CASES = "python/coverage_cases.py";

	/** Concepts whose bare instance legitimately emits no call: the endpoint and containers of other concepts. */
	private static final Set<String> EMPTY_BY_DESIGN = Set.of("Connection", "VectorDatabase", "Cluster", "Database", "AuthConfig");

	private static final String PENDING = "has no Vespa mapping yet";
	private static final String NO_MODELS = "; generated programs deploy no models: an embedder is a component in services.xml whose model files must be in the application package";
	private static final String NO_SNAPSHOTS = "snapshots; " + PENDING
			+ " (visiting every document of the schema into a local JSON Lines file, and feeding it back to restore; backups are a Vespa Cloud feature)";
	private static final String SINGLE_NODE = "the program deploys a single-node application (one node in services.xml); replication and distribution need a multi-node application with hosts.xml";
	private static final String NO_RESTART = "a content node restart (adding or removing an HNSW index, or fast-search, takes effect only after the searchnode restarts), which the HTTP APIs of a running application do not offer";
	private static final String NO_RENAME = PENDING + " (Vespa has no rename: a new schema or field filled by visiting every document, then the old one removed)";
	private static final String NO_RERANKERS = "generated programs deploy no models: a cross-encoder reranks as an ONNX model in a global-phase rank expression, which needs the model file in the application package";

	private static final String BASE = String.join("\n",
			"script coverageCases",
			"targets [ vespa ]",
			"param queryVector : vector = [0.9, 0.1, 0.0, 0.1]",
			"connectLocal: create connection local uri \"http://127.0.0.1:8090\"",
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
			"    field tags type array element-type varchar",
			"    field note type varchar",
			"    vector-field embedding type float_vector dim 4 metric cosine {",
			"        vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200",
			"    }",
			"    vector-field plainVector type float_vector dim 4 metric euclidean",
			"    payload-index priceIndex on price kind float",
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
		Path output = Path.of("generated", "vespa-full");
		write(files, output);
		Files.writeString(output.resolve("books.jsonl"), VespaManifest.load().render("environment/books.jsonl"), StandardCharsets.UTF_8);
		String python = files.get(PYTHON);
		String typescript = files.get(TYPESCRIPT);
		String java = files.get(JAVA);

		assertFalse(python.contains("rt.unsupported("), python);
		assertFalse(typescript.contains("rt.unsupported("), typescript);
		assertFalse(java.contains("throw unsupported("), java);
		for (String fragment : List.of(
				"\"\"\"Ignored on Vespa: description on VectorCollection, maxLength on PayloadField.\"\"\"",
				"\"\"\"Ignored on Vespa: description on SetPropertyOp, which a Vespa schema does not store.\"\"\"",
				"\"\"\"Ignored on Vespa: state on AwaitIndex.\"\"\"",
				"rt.deploy(self.client, files={}, dropped=[\"books\"], ignore_missing=True)",
				"created=[\"catalog_notes\"],",
				"<documents garbage-collection=\\\"true\\\">",
				"<document type=\\\"logs\\\" mode=\\\"index\\\" selection=\\\"logs.vecdsl_expires &gt; now()\\\"/>",
				"rank-profile priced {\\n        inputs {\\n            query(vecdsl_q) tensor<float>(x[4])\\n        }\\n        function cheapness() {\\n            expression: 1.0 / (1.0 + attribute(price))\\n        }\\n        first-phase {\\n            expression: closeness(field, embedding) + cheapness\\n        }\\n        match-features: closeness(field,embedding) cheapness\\n    }",
				"field vecdsl_version type long {",
				"field vecdsl_tenant type string {",
				"field vecdsl_namespace type string {",
				"field brain type tensor<bfloat16>(x[2]) {",
				"field wide type tensor<double>(x[2]) {",
				"field small type tensor<int8>(x[2]) {",
				"distance-metric: dotproduct",
				"overrides=",
				"<allow until=\\\"{{until}}\\\">schema-removal</allow>",
				"location=\"file://../books.jsonl\",",
				"policy=\"ignore\",",
				"policy=\"replace\",\n            counter=\"upsert_count\",",
				"versioned=True,",
				"append={\"tags\": [\"classic\"]},",
				"remove={\"tags\": [\"graph\"]},",
				"version=4,",
				"clear=[\"tags\", \"meta\", \"pageCount\"],",
				"clear=[\"tags\"],",
				"limit=1,",
				"slices={\"vecdsl_tenant\": \"north\"},",
				"slices={\"vecdsl_namespace\": \"team-a\"},",
				"slices={\"vecdsl_partition\": \"hot\"},",
				"slices={\"vecdsl_partition\": \"_default\"},",
				"expiring=True,\n            ttl=86400,",
				"ttl=3600,",
				"return rt.expire(self.client, document_type=\"logs\", seconds=7200)",
				"vecdsl_partition contains @p0 and vecdsl_expires > @vecdsl_now",
				"clock=True,",
				"all(group(vecdsl_partition) max(1000) each(output(count())))",
				"rt.require_collection(self.client, document_type=\"books\")",
				"return rt.await_ready(self.client, document_type=\"books\", timeout=60)",
				"return rt.describe(",
				"progress=True,",
				"({targetHits: 3, hnsw.exploreAdditionalHits: 77}nearestNeighbor(embedding, vecdsl_q))",
				"({targetHits: 3, hnsw.exploreAdditionalHits: 77, distanceThreshold: 1.0471975511965979}nearestNeighbor(embedding, vecdsl_q))",
				"\"offset\": 1,",
				"vectors=[\"embedding\"],",
				"lookup={\"document_type\": \"books\", \"field\": \"embedding\", \"positive_ids\": [1]},",
				"native=True,",
				"approximate: false",
				"\"matching\": {\"postFilterThreshold\": 1},",
				"\"softtimeout\": {\"enable\": False},",
				"\"timeout\": \"5000ms\",",
				"hnsw.exploreAdditionalHits: 40",
				"\"matching\": {\"approximateThreshold\": 0.05}",
				"total={",
				"\"ranking\": {\"profile\": \"priced\"},",
				"grammar.composite: \\\"phrase\\\"",
				"grammar.composite: \\\"or\\\"",
				"fusion=\"weighted\",",
				"weights=[0.7, 0.3],",
				"fusion=\"relative_score\",",
				"weights=[0.6, 0.4],",
				"\"positive_ids\": [1], \"negative_ids\": [6]",
				"order by price desc, id asc",
				"every=True,",
				"\"ranking\": {\"profile\": \"vecdsl_random\"},",
				"return rt.distance_matrix(",
				"title contains ({prefix: true}@p0)",
				"(true and !((genre contains @p0 or tags contains @p1)))",
				"(price >= @p0 and price <= @p1)",
				"(genre contains @p0 or genre contains @p1)",
				"records=[{\"id\": 5, \"price\": 44}, {\"id\": 9, \"price\": 1}],",
				"return rt.names(self.client, kind=\"collections\")",
				"return rt.names(self.client, kind=\"collections\", database=\"catalog\")",
				"return rt.names(self.client, kind=\"databases\", prefix=\"catalog\")",
				"names=[\"genreIndex\", \"priceIndex\", \"tagIndex\", \"embeddingIndex\"]",
				"all=True",
				"rt.deploy(self.client, files={}, dropped=[\"catalog_notes\"])",
				"document-summary vecdsl_highlight {\\n        summary id {}\\n        summary title {}\\n",
				"summary vecdsl_hl_title {\\n            source: title\\n            bolding: on\\n        }",
				"\"ranking\": {\"profile\": \"vecdsl_nn_embedding\", \"listFeatures\": True},",
				"\"ranking\": {\"profile\": \"vecdsl_text\", \"listFeatures\": True},",
				"explain=True,",
				"order=[[\"price\", \"asc\"]],",
				"order=[[\"price\", \"desc\"]],",
				"({targetHits: 1000}nearestNeighbor(embedding, vecdsl_q))",
				"\"hits\": 1000,",
				"after=\"1\",\n            limit=2,",
				"after=\"1\",\n            limit=1,",
				"after=\"5\",",
				"after=\"2\",\n            limit=2,",
				"slice=[0, 2],",
				"slice=[1, 2],",
				" | all(group(genre) max(2) each(max(1) each(output(summary()))))",
				" | all(group(genre) max(2) each(max(2) each(output(summary()))))",
				" | all(group(genre) max(1) each(max(2) each(output(summary()))))",
				"groups={\"field\": \"genre\"},",
				"groups={\"field\": \"genre\", \"size\": 1},",
				"\"yql\": \"select * from books where (genre contains @p1 or ({defaultIndex: \\\"title\\\"",
				"grammar.composite: \\\"near\\\", grammar.tokenization: \\\"linguistics\\\", grammar.syntax: \\\"none\\\", distance: 2}",
				"grammar.composite: \\\"oNear\\\", grammar.tokenization: \\\"linguistics\\\", grammar.syntax: \\\"none\\\", distance: 2}",
				"\"presentation\": {\"format\": {\"tensors\": \"short-value\"}, \"summary\": \"vecdsl_highlight\"},",
				"highlight=[\"title\"],")) {
			assertTrue(python.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"async pricedNearest(queryVector: number[]) {",
				"async hybridWeighted(queryVector: number[], queryText: string) {",
				"return await rt.distanceMatrix(this.client, {",
				"return await rt.expire(this.client, { document_type: \"logs\", seconds: 7200 });",
				"ranking: { profile: \"vecdsl_text\", listFeatures: true },",
				"order: [[\"price\", \"asc\"]],",
				"slice: [0, 2],",
				"groups: { field: \"genre\", size: 1 },",
				"explain: true,")) {
			assertTrue(typescript.contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"public Object pricedNearest(List<Float> queryVector) {",
				"return VespaRuntime.distanceMatrix(",
				"return VespaRuntime.expire(client, args(\"document_type\", \"logs\", \"seconds\", 7200));",
				"\"ranking\", args(\"profile\", \"vecdsl_nn_embedding\", \"listFeatures\", true),",
				"\"order\", list(list(\"price\", \"asc\"))",
				"\"slice\", list(0, 2)",
				"\"groups\", args(\"field\", \"genre\", \"size\", 1)",
				"\"explain\", true")) {
			assertTrue(java.contains(fragment), fragment);
		}
		assertTrue(files.get("docker-compose.yml").contains("vespa-var:/opt/vespa/var"), files.get("docker-compose.yml"));
		String everything = String.join("\n", files.values());
		assertFalse(everything.contains("Authorization: Bearer "), "no token in a generated file");
	}

	@Test
	public void generatesConstructsOutsideTheRunnableSample() throws Exception {
		String python = generate(BASE + String.join("\n",
				"rebuildPrice: rebuild-index priceIndex concurrently",
				"fromFile: insert into items from \"file://items.json\"",
				"importUpdate: bulk-import into items from \"file://items.jsonl\" batch 50 on-conflict update",
				"upsertBatch: upsert into items values [ { id: 7, title: \"seven\", price: 7.0, embedding: [1.0, 0.0, 0.0, 0.0], plainVector: [1.0, 0.0, 0.0, 0.0] } ] batch 20",
				"lookedUp: recommend in items top 2 positive [1] lookup-in archive using embedding output [title]",
				"ranged: count in items where price between 1 to 5",
				"inList: count in items where note in [\"a\", \"b\"]",
				"anyTags: count in items where tags contains_any [\"x\", \"y\"]",
				"allTags: count in items where tags contains_all [\"x\", \"y\"]",
				"notId: count in items where not id gt 3",
				"phrasedTitle: count in items where title phrase \"vector database\"",
				"prefixTitle: count in items where title like \"Vec%\"",
				"neqTitle: count in items where note neq \"x\"",
				"notListed: count in items where note not_in [\"a\", \"b\"]",
				"eastWrite: insert into items values [ { id: 8, title: \"eight\", embedding: [0.0, 1.0, 0.0, 0.0], plainVector: [0.0, 1.0, 0.0, 0.0] } ] tenant east",
				"deleteEast: delete from items all tenant east",
				"eastGet: get from items ids [1] tenant east output [title]",
				"addVector: alter items { add-field vector-field extra type float_vector dim 2 }",
				"loadP1: load items partitions [p1]",
				"multiField: search in items top 3 query text \"vector\" fields [title boost 2.0, body] output [title]",
				"fromRecords: update items values [ { id: 1, note: \"n\" } ] ids [1]",
				"tuned: vector-search in items top 3 query vector $queryVector using embedding output [title] extension vespa { post_filter_threshold = 0.1 filter_first_threshold = 0.2 filter_first_exploration = 0.3 target_hits_max_adjustment_factor = 4 }",
				"both: batch-query { count in items get from items ids [1] output [title] }",
				"offsetAfter: vector-search in items top 2 query vector $queryVector using embedding offset 1 cursor \"3\" output [title]",
				"relevanceSorted: vector-search in items top 2 query vector $queryVector using embedding sort { sort-by relevance asc } output [title]",
				"textGroups: search in items top 2 query text \"a\" fields [title] offset 1 group-by title output [title]",
				"slicedPage: scroll in items limit 2 offset 1 slice 1 of 3 output [title]",
				"vsScoped: vector-search in items top 2 query vector $queryVector using embedding metric cosine offset 1 namespace \"n1\" tenant east partitions [p1]"
						+ " consistency strong score native bounds min 0.1 max 9 with-vectors exclude [note] params { timeout 100 } output [title]",
				"kwScoped: search in items top 2 query text \"a\" fields [title] offset 1 namespace \"n1\" tenant east partitions [p1] consistency strong"
						+ " score native bounds min 0.1 max 9 with-vectors exclude [note] params { timeout 100 } output [title]",
				"hyScoped: hybrid-search in items top 2 offset 1 namespace \"n1\" tenant east partitions [p1] consistency strong score native bounds min 0.001"
						+ " with-vectors exclude [note] params { timeout 100 } { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"vsGroups: vector-search in items top 2 query vector $queryVector using embedding group-by note size 3 output [title]",
				"hyGroups: hybrid-search in items top 2 group-by note size 2 { vector { query vector $queryVector using embedding } keyword { query text \"a\" fields [title] } } output [title]",
				"rcScoped: recommend in items top 2 positive [1] using embedding offset 1 namespace \"n1\" tenant east partitions [p1] consistency strong score native"
						+ " bounds min 0.1 with-vectors with-total exclude [note] params { timeout 100 } output [title]",
				"smScoped: sample in items size 2 namespace \"n1\" tenant east partitions [p1] consistency strong output [title]",
				"scScoped: scroll in items limit 2 with-vectors namespace \"n1\" tenant east partitions [p1] consistency strong output [title]",
				"dmScoped: distance-matrix in items using embedding sample 3 limit 1 namespace \"n1\" tenant east partitions [p1] consistency strong",
				"ctScoped: count in items exact namespace \"n1\" tenant east partitions [p1] consistency strong",
				"gtScoped: get from items ids [1] with-vectors namespace \"n1\" partitions [p1] consistency strong output [title]",
				"inScoped: insert into items values [ { id: 11, title: \"t\", embedding: [1.0, 0.0, 0.0, 0.0], plainVector: [1.0, 0.0, 0.0, 0.0] } ] records 1"
						+ " namespace \"n1\" consistency quorum",
				"upScoped: upsert into items values [ { id: 12, title: \"u\", embedding: [1.0, 0.0, 0.0, 0.0], plainVector: [1.0, 0.0, 0.0, 0.0] } ] records 1"
						+ " tenant east partition p1 consistency quorum",
				"upTtl: upsert into archive values [ { id: 2, embedding: [0.0, 1.0, 0.0, 0.0] } ] ttl 60",
				"udScoped: update items set { note: \"n\" } ids [1] namespace \"n1\" tenant east partition p1 consistency quorum",
				"dlScoped: delete from items ids [1] namespace \"n1\" partition p1 consistency quorum",
				"biScoped: bulk-import into items from \"file://items.jsonl\" parallel 2 async namespace \"n1\" tenant east partition p1",
				"cpScoped: compact items target-segments 2 async",
				"lsLimited: list collections limit 2",
				"dropNote: drop note",
				"")).get(CASES);
		for (String fragment : List.of(
				"\"\"\"Ignored on Vespa: concurrently on RebuildIndex.\"\"\"",
				"return rt.reindex(self.client, document_type=\"items\")",
				"rt.read_records(self.client, location=\"file://items.json\")",
				"\"\"\"Ignored on Vespa: batchSize on BulkImport.\"\"\"",
				"policy=\"replace\",",
				"\"\"\"Ignored on Vespa: batchSize on Upsert.\"\"\"",
				"\"document_type\": \"archive\"",
				"(price >= @p0 and price <= @p1)",
				"(note contains @p0 or note contains @p1)",
				"(tags contains @p0 or tags contains @p1)",
				"(tags contains @p0 and tags contains @p1)",
				"(true and !(id > @p0))",
				"grammar.composite: \\\"phrase\\\"",
				"vecdsl_exact_title contains ({prefix: true}@p0)",
				"(true and !(note contains @p0))",
				"(true and !((note contains @p0 or note contains @p1)))",
				"slices={\"vecdsl_tenant\": \"east\", \"vecdsl_partition\": \"_default\"}",
				"vecdsl_tenant contains @p0",
				"return rt.delete(",
				"slices={\"vecdsl_tenant\": [\"east\"]}",
				"field extra type tensor<float>(x[2]) {",
				"rt.require_collection(self.client, document_type=\"items\")",
				"\"input\": {\"query(vecdsl_w_title)\": 2}",
				"defaultIndex: \\\"body\\\"",
				"records=[{\"id\": 1, \"note\": \"n\"}],",
				"\"postFilterThreshold\": 0.1",
				"\"targetHitsMaxAdjustmentFactor\": 4",
				"return [self.both_query_0(), self.both_query_1()]",
				"after=\"3\",\n            limit=2,\n            offset=1,",
				"order=[[None, \"asc\"]],",
				" | all(group(vecdsl_exact_title) max(3) each(max(1) each(output(summary()))))",
				"groups={\"field\": \"vecdsl_exact_title\", \"offset\": 1},",
				"slice=[1, 3],\n            limit=2,\n            offset=1,",
				"vecdsl_namespace contains @p",
				"\"vecdsl_namespace\": \"n1\"",
				"\"timeout\": \"100ms\",",
				"vectors=[\"embedding\", \"plainVector\"],",
				"score native on HybridSearch, whose fused score has one form",
				"groups={\"field\": \"note\", \"size\": 2},",
				"({targetHits: 6}nearestNeighbor(embedding, vecdsl_q)) | all(group(note) max(2) each(max(3) each(output(summary()))))",
				"recordCountHint on Insert",
				"writeConsistency on Insert",
				"recordCountHint on Upsert",
				"writeConsistency on Upsert",
				"writeConsistency on Update",
				"writeConsistency on Delete",
				"parallelism on BulkImport",
				"async on BulkImport",
				"targetSegments on Compact",
				"async on Compact",
				"ttl=60,",
				"return rt.names(self.client, kind=\"collections\", limit=2)")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
		assertFalse(python.contains("rt.unsupported("), python);
	}

	@Test
	public void bindsHostileValuesInResultShapes() throws Exception {
		String hostile = "x\\\" or true) or (title contains \\\"y @p0 ' \\\\ ";
		String value = "x\" or true) or (title contains \"y @p0 ' \\ ";
		String python = generate(BASE + String.join("\n",
				"cursorHostile: vector-search in items top 2 query vector $queryVector using embedding cursor \"" + hostile + "\" output [title]",
				"scrollHostile: scroll in items limit 2 cursor \"" + hostile + "\" output [title]",
				"nearHostile: search in items top 2 query text \"" + hostile + "\" fields [title] slop 2 highlight [title] output [title]",
				"groupHostile: search in items top 2 query text \"" + hostile + "\" fields [title] group-by note output [title]",
				"unionHostile: hybrid-search in items top 2 with-total { vector { query vector $queryVector using embedding } keyword { query text \""
						+ hostile + "\" fields [title] } } output [title]",
				"")).get(CASES);
		assertTrue(python.contains("after=" + Syntax.quote(value) + ","), python);
		assertTrue(python.contains("texts={\"p0\": " + Syntax.quote(value) + "},"), python);
		for (String line : python.split("\n")) {
			if (line.contains("\"yql\":")) {
				assertFalse(line.contains("or true"), line);
			}
		}
	}

	@TestFactory
	public Stream<DynamicTest> unsupportedConstructsRaiseTheirReason() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put("probe: release items", "release; Vespa keeps a deployed schema's attributes in memory and has no request that evicts one document type");
		cases.put("probe: transaction { delete from items ids [1] }",
				"transactions; Vespa applies each document operation atomically on its own and has no transaction over several documents");
		cases.put("probe: alter items { add-alias alias view for items }",
				"aliases; Vespa addresses a document type by its schema name and has no alias for a schema or document type");
		cases.put("probe: refresh items", "collections derived from external sources; Vespa stores the documents it is fed and reads no external tables");
		cases.put("probe: create reranker r1 provider \"cohere\"", "rerankers; " + NO_RERANKERS);
		cases.put("probe: create auth a1 type api_key",
				"api_key authentication; Vespa authenticates clients with mTLS certificates (and Vespa Cloud data-plane tokens) and has no api_key method");
		cases.put("probe: create auth a1 type token",
				"token authentication; data-plane tokens are a Vespa Cloud feature (a self-hosted node authenticates clients with mTLS)");
		cases.put("probe: create connection other uri \"http://127.0.0.1:8090\" grpc-port 6334",
				"grpc-port; the generated programs use the HTTP APIs (document/v1, query, deploy)");
		cases.put("probe: create connection lite path \"./vecdsl.db\"", "embedded deployments; Vespa runs as a service, and the programs use its HTTP APIs");
		cases.put("probe: create cluster c1 { collection inner1 { primary id field id type int vector-field v1 type float_vector dim 2 } cluster-limit cl1 max_collections = 100 }",
				"cluster limits; Vespa has no setting that limits the number of schemas");
		cases.put("probe: create cluster c2 { collection inner2 { primary id field id type int vector-field v1 type float_vector dim 2 } replication factor 2 }",
				"replication and sharding; " + SINGLE_NODE);
		cases.put("probe: create database prod { connection main uri \"http://127.0.0.1:8090\" metrics m1 enabled }",
				"metrics configuration; Vespa reports metrics through /state/v1/metrics and the metrics proxy configured in services.xml, not through objects a client creates");
		cases.put(search + "params { target-recall 0.9 } output [title]",
				"target-recall: targetHits and hnsw.exploreAdditionalHits are effort settings, with no declarable recall target");
		cases.put(search + "params { probes 4 } output [title]", "probes; Vespa's vector index is HNSW (no IVF lists to probe)");
		cases.put(search + "params { filter-strategy iterative } output [title]",
				"filter-strategy iterative; Vespa pre-filters or post-filters a nearestNeighbor query (with targetHits adjusted for the filter), with no iterative scan");
		cases.put(search + "highlight [title] output [title]", "the 'highlight' clause on a search without text; dynamic summaries mark the terms of a text query");
		cases.put("r1: create reranker r1 provider \"cohere\"\n" + search + "rerank r1 output [title]", "the 'rerank' clause; " + NO_RERANKERS);
		cases.put("probe: search in items top 3 query text \"a\" fields [title] min-should-match 2 output [title]",
				"the 'min-should-match' clause; userInput combines the terms with and, or, weakAnd or phrase and has no minimum number of matching terms");
		cases.put("probe: search in items top 3 query text \"a\" text-query query_string fields [title] output [title]",
				"text-query query_string; the generator never parses a value as query syntax (userInput with grammar.syntax none), so user query strings are not parsed");
		cases.put("probe: count in items as-of \"2026-01-01\"", "the 'as-of' clause; Vespa keeps only the current version of each document");
		cases.put("probe: count in items where title regex \"^A\"",
				"the regex text operator; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)");
		cases.put("probe: count in items where tags length gt 1", "length predicates; YQL has no operator on the number of values of a field");
		cases.put("probe: count in items where note is_null",
				"the is_null test; YQL has no test for a missing value (YqlParser converts no IS_NULL or IS_NOT_NULL node)");
		cases.put("probe: alter items { set deletion-protection = true }",
				"deletion-protection; open-source Vespa has no drop guard on a schema (a deployment that removes a schema needs the schema-removal validation override, which every generated drop writes)");
		cases.put("probe: alter items { set tags = [\"a\"] }", "collection tags; a Vespa schema carries no metadata besides its definition");
		cases.put("probe: alter items { drop-field id }", "dropping the primary key; a vecDSL collection keeps its key");
		cases.put("probe: alter items { add-field vector-field half type float16_vector dim 2 }",
				"float16_vector fields; Vespa tensor cells are double, float, bfloat16 or int8 (no float16 and no unsigned int8)");
		cases.put("probe: alter items { add-function function f1 type bm25 input [title] output [embedding] }",
				"bm25 functions; Vespa computes BM25 over index fields itself (bm25(field) with enable-bm25) and has no function that writes sparse vectors");
		cases.put("probe: create items if-not-exists with { create vector-index ivfIndex on plainVector family ivf }",
				"creating a vector index on an existing collection; it needs " + NO_RESTART);
		cases.put("probe: drop embeddingIndex", "dropping a vector index; it needs " + NO_RESTART);
		cases.put("probe: drop priceIndex", "dropping a payload index; removing fast-search from an attribute needs " + NO_RESTART);
		cases.put("probe: bulk-import into items from \"file://items.json\" continuous",
				"continuous import; Vespa has no ingestion pipeline that pulls from a source (clients feed the documents)");
		cases.put("probe: bulk-import into items from \"s3://a-bucket/items.json\"",
				"import from s3; the runtime reads local files (Vespa reads no object stores; clients feed the documents)");
		cases.put("probe: bulk-import into items from \"file://items.parquet\" format parquet", "import format parquet; the runtime reads JSON and JSON Lines files");
		String keyword = "probe: search in items top 3 query text \"a b\" fields [title] ";
		String noFuzzy = "the fuzzy operator matches string attributes (single, array or weighted set), not the terms of an index field, so full-text fields have no fuzzy match";
		cases.put(keyword + "fuzziness \"AUTO\" output [title]", "the 'fuzziness' clause; " + noFuzzy);
		cases.put("probe: search in items top 3 query text \"a\" text-query fuzzy fields [title] output [title]", "text-query fuzzy; " + noFuzzy);
		cases.put("probe: count in items where title fuzzy \"vectr\"", "the fuzzy text operator; " + noFuzzy);
		cases.put(keyword + "operator or slop 2 output [title]", "slop with operator or; near and onear bound the distance of terms that must all match");
		cases.put(keyword + "cursor \"1\" group-by note output [title]",
				"the 'cursor' clause with 'group-by'; a cursor names a hit, and grouped results are pages of groups");
		cases.put(keyword + "group-by note sort { sort-by price desc } output [title]", "the 'sort' clause with 'group-by'; groups are ordered by their best hit");
		cases.put(keyword + "group-by tags output [title]",
				"group-by on tags; the generator groups by single-value fields (grouping treats array elements as separate values)");
		cases.put(keyword + "highlight [note] output [title]",
				"highlight on note, which is not a full-text field; bolding marks the matched terms of index fields of type string");
		cases.put(search + "sort { sort-by title asc } output [title]",
				"sorting by title; the generator sorts by single numeric, boolean and exact string attributes");
		cases.put("probe: scroll in items slice 2 of 2 output [title]", "slice 2 of 2; a slice index is at least 0 and below the slice count");
		cases.put("e1: create embedding-model e1 provider \"openai\"\ne2: create embedding-model e2 provider \"openai\"\nprobe: migrate-embeddings items from-model e1 to-model e2",
				"embedding migration" + NO_MODELS);
		cases.put("probe: generate in items top 3 query vector $queryVector using embedding output [title]",
				"generative search; generated programs deploy no models: generation needs an LLM client component in services.xml, with a local model file in the application package or an external service and its API key");
		cases.put("e1: create embedding-model e1 provider \"openai\"\n" + search + "model e1 output [title]", "the 'model' clause" + NO_MODELS);
		cases.put("probe: create collection ranked { primary id extension vespa {"
				+ " rank-profile rp first-phase \"1\" model m1 ml-model m1 format onnx path \"m.onnx\" } field id type int vector-field v type float_vector dim 2 }",
				"the model of rank profile rp; generated programs deploy no models: an onnx-model needs its model file in the application package");
		return dynamicCases(cases);
	}

	@TestFactory
	public Stream<DynamicTest> pendingConstructsRaiseTheirMessage() {
		Map<String, String> cases = new TreeMap<>();
		String search = "probe: vector-search in items top 3 query vector $queryVector using embedding ";
		cases.put("probe: snapshot items", NO_SNAPSHOTS);
		cases.put("probe: restore items from \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: drop-snapshot \"weekly\"", NO_SNAPSHOTS);
		cases.put("probe: list snapshots in items", NO_SNAPSHOTS);
		cases.put("probe: clone items to \"itemsCopy\"",
				"cloning a collection; " + PENDING + " (a new schema deployed, then every document visited and fed under the new document type)");
		cases.put("probe: alter items { rename-field note to remark }", "rename-field; " + NO_RENAME);
		cases.put("probe: alter items rename to goods", "renaming a collection; " + NO_RENAME);
		cases.put("probe: create embedding-model e1 provider \"openai\"", "embedding models" + NO_MODELS);
		cases.put("probe: create generative-model g1 provider \"openai\"",
				"generative models; generated programs deploy no models: generation needs an LLM client component in services.xml, with a local model file in the application package or an external service and its API key");
		cases.put("probe: vector-search in items top 3 query text \"a\" using embedding output [title]", "text input" + NO_MODELS);
		cases.put(search + "mmr lambda 0.5 output [title]", "the 'mmr' clause; " + PENDING + " (MMR over the candidates in the runtime)");
		cases.put(search + "aggregate { agg max on price } output [title]",
				"the 'aggregate' clause; " + PENDING + " (grouping with output(count(), sum(), avg(), min(), max()))");
		cases.put(search + "boost { boost-fn kind field_value field price } output [title]",
				"the 'boost' clause; " + PENDING + " (a first-phase expression over the attributes)");
		cases.put("probe: search in items top 3 query text \"a\" text-query prefix fields [title] output [title]",
				"text-query prefix; " + PENDING + " (the prefix and substring term annotations)");
		cases.put("probe: hybrid-search in items top 3 fusion rrf { vector { query vector $queryVector using embedding bounds min 0.5 } keyword { query text \"a\" fields [title] } } output [title]",
				"bounds on a hybrid leg; " + PENDING + " (the leg score filtered before fusion)");
		cases.put("probe: count in items where note like \"%x\"", "the like pattern %x; " + PENDING + " (the suffix and substring term annotations)");
		cases.put("probe: bulk-import into items from \"file://items.csv\" format csv", "import format csv; " + PENDING + " (CSV rows typed by the collection fields)");
		cases.put("probe: create collection autoItems auto-id { primary id field id type int vector-field v type float_vector dim 2 }",
				"auto-id; " + PENDING + " (document ids generated by the runtime)");
		cases.put("probe: alter items { add-field field place type geo_point }",
				"geo_point field place; " + PENDING + " (a position field and the geoLocation operator)");
		cases.put("probe: alter items { add-field field blob type bytes }", "bytes field blob; " + PENDING + " (a raw field, summary only)");
		cases.put("probe: create analyzer sounds tokenizer standard language \"en\" { filter ph type phonetic }",
				"the phonetic filter; " + PENDING + " (a custom linguistics component)");
		cases.put("probe: create analyzer german tokenizer standard language \"de\"",
				"the language de; " + PENDING + " (set_language in the indexing statements and the query language)");
		cases.put("probe: create auth a2 type mtls",
				"mtls authentication; " + PENDING + " (client certificates in the runtimes and a TLS-enabled node configured outside the application package)");
		return dynamicCases(cases);
	}

	private Stream<DynamicTest> dynamicCases(Map<String, String> cases) {
		return cases.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getValue(), () -> {
			String python = generate(BASE + entry.getKey() + "\n").get(CASES);
			String call = "rt.unsupported(\"vespa\", " + Syntax.quote(entry.getValue()) + ")";
			assertTrue(python.contains(call), call + "\n" + python);
		}));
	}

	/** Every sample, retargeted to Vespa, maps without errors: unsupported constructs become rt.unsupported calls. */
	@TestFactory
	public Stream<DynamicTest> everySampleMapsWithoutErrors() throws IOException {
		List<Path> samples;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			samples = listing.filter(path -> path.toString().endsWith(".vecDSL")).sorted().toList();
		}
		return samples.stream().map(path -> DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
			String source = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("targets \\[[^\\]]*\\]", "targets [ vespa ]");
			Script script = parseHelper.parse(source);
			validation.assertNoErrors(script);
			Program program = new VespaGenerator().program(script);
			VespaManifest manifest = VespaManifest.load();
			for (VespaPrinter printer : List.<VespaPrinter>of(new VespaPython(), new VespaTypeScript(), new VespaJava())) {
				assertFalse(printer.files(program, manifest, "http://127.0.0.1:19071").isEmpty(), printer.language());
			}
		}));
	}

	/** The machine-checked form of the coverage claim: no vecDSL statement, concept or alter operation is left unmapped. */
	@Test
	public void everyClassHasAVespaMapping() {
		List<String> unmapped = new ArrayList<>();
		VecDSLPackage model = VecDSLPackage.eINSTANCE;
		List<String> dispatched = Arrays.stream(VespaStatements.class.getDeclaredMethods())
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
			reached(new VespaStatements(script), create, type, true, unmapped);
		}
		for (EClass type : concrete(model.getAlterOperation())) {
			Alter alter = VecDSLFactory.eINSTANCE.createAlter();
			alter.setTarget((VectorCollection) named(model.getVectorCollection(), "probe"));
			alter.getOperations().add((AlterOperation) VecDSLFactory.eINSTANCE.create(type));
			reached(new VespaStatements(script), alter, type, false, unmapped);
		}
		if (!unmapped.isEmpty()) {
			fail(String.join("\n", unmapped));
		}
	}

	private static void reached(VespaStatements statements, org.ftn.vecDSL.vecDSL.Method statement, EClass type, boolean concept,
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
				if (stmt instanceof Code.Fail failure && failure.feature().startsWith(VespaStatements.NOT_MAPPED)) {
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
			if (key.startsWith(VespaGenerator.VENDOR)) {
				files.put(key.substring(VespaGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
