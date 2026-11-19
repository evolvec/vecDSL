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
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.vespa.VespaGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Vespa clients (Python, TypeScript, Java) and the Docker Compose file of a single Vespa node for
 * samples/vespa_demo.vecDSL into generated/vespa, and checks the constructs Vespa cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VespaGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "vespa_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"http://127.0.0.1:8090\"";

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
		System.clearProperty(VespaGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "vespa"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_vespa_runtime.py", "typescript/package.json", "typescript/src/vecdslVespaRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/VespaRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{"), path));
		for (String fragment : List.of(
				"class ProductCatalog:",
				"def similar(self, query_vector, max_price):",
				"\"url\": os.environ.get(\"VECDSL_VESPA_URL\", \"http://127.0.0.1:8090\"),",
				"\"config_url\": os.environ.get(\"VECDSL_VESPA_CONFIG_URL\", \"http://127.0.0.1:19071\"),",
				"\"token_variable\": \"VECDSL_VESPA_TOKEN\",")) {
			assertTrue(files.get(PYTHON).contains(fragment), fragment + "\n" + files.get(PYTHON));
		}
		for (String fragment : List.of(
				"url: process.env.VECDSL_VESPA_URL ?? \"http://127.0.0.1:8090\"",
				"configUrl: process.env.VECDSL_VESPA_CONFIG_URL ?? \"http://127.0.0.1:19071\"",
				"async similar(queryVector: number[], maxPrice: number) {",
				"texts: { p0: \"books\" },",
				"numbers: { p1: maxPrice },")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment + "\n" + files.get(TYPESCRIPT));
		}
		for (String fragment : List.of(
				"envOr(\"VECDSL_VESPA_URL\", \"http://127.0.0.1:8090\")",
				"envOr(\"VECDSL_VESPA_CONFIG_URL\", \"http://127.0.0.1:19071\")",
				"public Object similar(List<Float> queryVector, double maxPrice) {",
				"return VespaRuntime.search(",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment + "\n" + files.get(JAVA));
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		String compose = files.get("docker-compose.yml");
		assertTrue(compose.contains("image: vespaengine/vespa:8.781.11"), compose);
		assertTrue(compose.contains("127.0.0.1:8090:8080"), compose);
		assertTrue(compose.contains("127.0.0.1:19071:19071"), compose);
		assertTrue(files.get("java/pom.xml").contains("<artifactId>gson</artifactId>"));
		assertTrue(files.get("typescript/package.json").contains("\"typescript\": \"~5.9.3\""), files.get("typescript/package.json"));
	}

	@Test
	public void deploysTheSchemaInAnApplicationPackage() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"rt.deploy(self.client, files={}, dropped=[\"products\"], ignore_missing=True)",
				"<document type=\\\"products\\\" mode=\\\"index\\\"/>",
				"field title type string {\\n            indexing: index | summary\\n            index: enable-bm25\\n        }",
				"field category type string {\\n            indexing: attribute | summary\\n            match: cased\\n            attribute: fast-search\\n        }",
				"field embedding type tensor<float>(x[4]) {",
				"distance-metric: angular",
				"max-links-per-node: 16\\n                    neighbors-to-explore-at-insert: 200",
				"field vecdsl_exact_title type string {\\n        indexing: input title | attribute\\n        match: cased\\n    }",
				"rank-profile vecdsl_nn_embedding {",
				"match-features: distance(field,embedding)",
				"expression: query(vecdsl_w_title) * bm25(title)",
				"created=[\"products\"],\n            skip_existing=True,",
				"\"\"\"Ignored on Vespa: consistency on VectorCollection, maxLength on PayloadField.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToRequestsWithParameters() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"policy=\"error\",\n            counter=\"insert_count\",\n            vectors={\"embedding\": 4},",
				"\"yql\": \"select * from products where (({targetHits: 3, distanceThreshold: 1.369438406004566}nearestNeighbor(embedding, vecdsl_q)) and (category contains @p0 and price <= @p1))\",",
				"\"input\": {\"query(vecdsl_q)\": query_vector},",
				"texts={\"p0\": \"books\"},\n            numbers={\"p1\": max_price},",
				"metric=\"cosine\",\n            feature=\"distance(field,embedding)\",\n            low=0.2,",
				"{defaultIndex: \\\"title\\\", grammar.composite: \\\"and\\\", grammar.tokenization: \\\"linguistics\\\", grammar.syntax: \\\"none\\\"}userInput(@p0)",
				"return rt.hybrid(",
				"fusion=\"rrf\",",
				"k=60,",
				"return rt.get(self.client, document_type=\"products\", key=\"id\", ids=[1, 2], output=[\"title\", \"price\"])",
				"\"yql\": \"select * from products where (category contains @p0 and price < @p1)\",",
				"set={\"price\": 35},",
				"(true and !((id = @p0 or id = @p1)))",
				"lookup={\"document_type\": \"products\", \"field\": \"embedding\", \"positive_ids\": [1], \"negative_ids\": [3]},",
				"\"yql\": \"select * from products where tags contains @p0 order by id asc\",",
				"scores=False,",
				"return rt.delete(self.client, document_type=\"products\", key=\"id\", ids=[4])")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void bindsHostileValuesAsParameters() throws Exception {
		String hostile = "x\\\" or true) or (title contains \\\"y @p0 ' \\\\ ";
		String value = "x\" or true) or (title contains \"y @p0 ' \\ ";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq \"" + hostile + "\"",
				"hostileText: search in products top 3 query text \"" + hostile + "\" fields [title] output [title]",
				"hostileWrite: insert into products values [ { id: 9, title: \"" + hostile + "\", embedding: [0.1, 0.2, 0.3, 0.4] } ]", "")).get(PYTHON);

		assertTrue(python.contains("\"yql\": \"select * from products where category contains @p0\","), python);
		assertTrue(python.contains("texts={\"p0\": " + Syntax.quote(value) + "},"), python);
		assertTrue(python.contains("\"title\": " + Syntax.quote(value) + ","), python);
		for (String line : python.split("\n")) {
			if (line.contains("\"yql\":")) {
				assertFalse(line.contains("or true"), line);
			}
		}
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(VespaGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: targetHits and hnsw.exploreAdditionalHits are effort settings, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"vespa\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"vespa\", \"" + recall), files.get(TYPESCRIPT));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"vespa\", \"" + recall), files.get(JAVA));
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; Vespa runs as a service, and the programs use its HTTP APIs");
	}

	@Test
	public void tokensWrittenOnTheConnectionAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, CONNECTION + " token \"written-in-the-script\""),
				"api keys and tokens on the connection; a self-hosted Vespa node authenticates clients with mTLS certificates (data-plane tokens are a Vespa Cloud feature; the runtime sends VECDSL_VESPA_TOKEN when it is set)");
	}

	@Test
	public void connectionUriWithoutSchemeIsReadAsHttp() throws Exception {
		Map<String, String> files = generate(replaced(CONNECTION, "create connection local uri \"127.0.0.1:1521/FREEPDB1\""));

		assertConnection(files, "http://127.0.0.1:1521/FREEPDB1", "http://127.0.0.1:19071");
		assertConnection(generate(replaced(CONNECTION, "create connection local uri \"vespa.example:8443\" tls")),
				"https://vespa.example:8443", "https://vespa.example:19071");
	}

	@Test
	public void connectionUriWithSchemeHostAndPortOnly() throws Exception {
		assertConnection(generate(replaced(CONNECTION, "create connection local uri \"https://vespa.example:8443\"")),
				"https://vespa.example:8443", "https://vespa.example:19071");
	}

	@Test
	public void connectionUriOfAnotherSchemeIsUnsupported() throws Exception {
		String source = replaced(CONNECTION, "create connection local uri \"grpc://127.0.0.1:8090\"");

		assertUnsupported(source, "the connection uri grpc://127.0.0.1:8090; Vespa programs take an http or https URL with a host, "
				+ "or host:port (read as http://host:port)");
		assertConnection(generate(source), "grpc://127.0.0.1:8090", "http://127.0.0.1:19071");
	}

	@Test
	public void transactionsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbundle: transaction { update products set { price: 1.0 } ids [1] }\n",
				"transactions; Vespa applies each document operation atomically on its own and has no transaction over several documents");
	}

	@Test
	public void aliasesAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nview: alter products { add-alias alias shelf for products }\n",
				"aliases; Vespa addresses a document type by its schema name and has no alias for a schema or document type");
	}

	@Test
	public void regularExpressionFiltersAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyPattern: count in products where title regex \"^Gr.*\"\n",
				"the regex text operator; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)");
	}

	@Test
	public void iterativeFilteringIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nscan: vector-search in products top 3 query vector $queryVector using embedding params { filter-strategy iterative } output [title]\n",
				"filter-strategy iterative; Vespa pre-filters or post-filters a nearestNeighbor query (with targetHits adjusted for the filter), with no iterative scan");
	}

	@Test
	public void releaseIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nunload: release products\n",
				"release; Vespa keeps a deployed schema's attributes in memory and has no request that evicts one document type");
	}

	@Test
	public void snapshotsArePending() throws Exception {
		assertUnsupported(sample() + "\nnightly: snapshot products\n",
				"snapshots; has no Vespa mapping yet (visiting every document of the schema into a local JSON Lines file, and feeding it back to restore; backups are a Vespa Cloud feature)");
	}

	/** The default query and config server URLs of the three programs. */
	private static void assertConnection(Map<String, String> files, String url, String configUrl) {
		for (String fragment : List.of("\"url\": os.environ.get(\"VECDSL_VESPA_URL\", \"" + url + "\"),",
				"\"config_url\": os.environ.get(\"VECDSL_VESPA_CONFIG_URL\", \"" + configUrl + "\"),")) {
			assertTrue(files.get(PYTHON).contains(fragment), fragment + "\n" + files.get(PYTHON));
		}
		for (String fragment : List.of("url: process.env.VECDSL_VESPA_URL ?? \"" + url + "\"",
				"configUrl: process.env.VECDSL_VESPA_CONFIG_URL ?? \"" + configUrl + "\"")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment + "\n" + files.get(TYPESCRIPT));
		}
		for (String fragment : List.of("envOr(\"VECDSL_VESPA_URL\", \"" + url + "\")",
				"envOr(\"VECDSL_VESPA_CONFIG_URL\", \"" + configUrl + "\")")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment + "\n" + files.get(JAVA));
		}
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"vespa\", " + Syntax.quote(feature) + ")";
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
			if (key.startsWith(VespaGenerator.VENDOR)) {
				files.put(key.substring(VespaGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
