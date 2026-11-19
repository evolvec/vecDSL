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
import org.ftn.vecDSL.generator.redis.RedisGenerator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Generates the Redis clients (Python, TypeScript, Java) and the Docker Compose file of Redis Open Source for
 * samples/redis_demo.vecDSL into generated/redis, and checks the constructs Redis Open Source cannot express.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class RedisGeneratorTest {

	private static final Path SAMPLE = Path.of("samples", "redis_demo.vecDSL");
	private static final String PYTHON = "python/product_catalog.py";
	private static final String TYPESCRIPT = "typescript/src/productCatalog.ts";
	private static final String JAVA = "java/src/main/java/vecdsl/productcatalog/ProductCatalog.java";
	private static final String CONNECTION = "create connection local uri \"redis://127.0.0.1:6379\" password env(\"VECDSL_REDIS_PASSWORD\")";
	private static final String INDEX = "vector-index embeddingIndex family hnsw max-degree 16 build-candidates 200";
	private static final String NO_MODELS = "; Redis Open Source runs no embedding models (the Query Engine indexes the vectors a client writes), so generated programs supply their own vectors";

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
		System.clearProperty(RedisGenerator.LANGUAGES_PROPERTY);
	}

	@Test
	public void generatesEveryHostLanguage() throws Exception {
		Map<String, String> files = generate(sample(), Path.of("generated", "redis"));

		assertTrue(files.keySet().containsAll(Set.of(PYTHON, TYPESCRIPT, JAVA, "docker-compose.yml", "python/requirements.txt",
				"python/vecdsl_redis_runtime.py", "typescript/package.json", "typescript/src/vecdslRedisRuntime.ts", "typescript/tsconfig.json",
				"java/pom.xml", "java/src/main/java/vecdsl/productcatalog/RedisRuntime.java")), files.keySet()::toString);
		files.forEach((path, content) -> assertFalse(content.contains("{{sdk") || content.contains("{{server") || content.contains("{{java"), path));
		assertTrue(files.get(PYTHON).contains("class ProductCatalog:"));
		assertTrue(files.get(PYTHON).contains("def similar(self, query_vector, max_price):"));
		assertTrue(files.get(PYTHON).contains("\"url\": os.environ.get(\"VECDSL_REDIS_URL\", \"redis://127.0.0.1:6379\"),"), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"user_variable\": \"VECDSL_REDIS_USER\","), files.get(PYTHON));
		assertTrue(files.get(PYTHON).contains("\"password_variable\": \"VECDSL_REDIS_PASSWORD\","), files.get(PYTHON));
		for (String fragment : List.of(
				"url: process.env.VECDSL_REDIS_URL ?? \"redis://127.0.0.1:6379\"",
				"userVariable: \"VECDSL_REDIS_USER\"",
				"passwordVariable: \"VECDSL_REDIS_PASSWORD\"",
				"async similar(queryVector: number[], maxPrice: number) {",
				"params: { vec: queryVector, p1: \"books\", p2: maxPrice },",
				"blobs: { vec: \"FLOAT32\" },")) {
			assertTrue(files.get(TYPESCRIPT).contains(fragment), fragment);
		}
		for (String fragment : List.of(
				"\"url\", envOr(\"VECDSL_REDIS_URL\", \"redis://127.0.0.1:6379\"),",
				"\"user_variable\", \"VECDSL_REDIS_USER\",",
				"\"password_variable\", \"VECDSL_REDIS_PASSWORD\"",
				"public Object similar(List<Float> queryVector, double maxPrice) {",
				"return RedisRuntime.search(",
				"\"params\", args(\"vec\", queryVector, \"p1\", \"books\", \"p2\", maxPrice),",
				"public Object seed() {")) {
			assertTrue(files.get(JAVA).contains(fragment), fragment);
		}
		assertFalse(files.get(PYTHON).contains("rt.unsupported("), files.get(PYTHON));
		assertFalse(files.get(TYPESCRIPT).contains("rt.unsupported("), files.get(TYPESCRIPT));
		assertFalse(files.get(JAVA).contains("throw unsupported("), files.get(JAVA));
		assertTrue(files.get("docker-compose.yml").contains("image: redis:8.10.3-trixie"));
		assertTrue(files.get("docker-compose.yml").contains("\"--requirepass\", \"${VECDSL_REDIS_PASSWORD:?set VECDSL_REDIS_PASSWORD}\""));
		assertTrue(files.get("docker-compose.yml").contains("\"127.0.0.1:6379:6379\""));
		assertTrue(files.get("docker-compose.yml").contains("redis-data:/data"));
		assertTrue(files.get("python/requirements.txt").startsWith("redis>=8.1.0,<8.2"));
		assertTrue(files.get("typescript/package.json").contains("\"redis\": \"~6.3.1\""));
		assertTrue(files.get("java/pom.xml").contains("<artifactId>jedis</artifactId>"));
	}

	@Test
	public void mapsTheSchemaToAJsonIndex() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"rt.drop_collection(self.client, index=\"products\", prefix=\"products:\", if_exists=True)",
				"\"FT.CREATE\",\n                \"products\",\n                \"ON\",\n                \"JSON\",\n                \"PREFIX\",\n                \"1\",\n                \"products:\",\n                \"LANGUAGE\",\n                \"english\",\n                \"SCHEMA\",",
				"\"$.title\",\n                \"AS\",\n                \"vecdsl_exact_title\",\n                \"TAG\",\n                \"CASESENSITIVE\",",
				"\"$.tags[*]\",\n                \"AS\",\n                \"tags\",\n                \"TAG\",",
				"\"VECTOR\",\n                \"HNSW\",\n                \"10\",\n                \"TYPE\",\n                \"FLOAT32\",\n                \"DIM\",\n                \"4\",\n                \"DISTANCE_METRIC\",\n                \"COSINE\",\n                \"M\",\n                \"16\",\n                \"EF_CONSTRUCTION\",\n                \"200\",",
				"ignore=[\"index already exists\"],",
				"\"\"\"Ignored on Redis: consistency on VectorCollection, maxLength on PayloadField.\"\"\"")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void mapsFiltersSearchesAndWritesToCommandsWithParameters() throws Exception {
		String python = generate(sample()).get(PYTHON);

		for (String fragment : List.of(
				"policy=\"error\",\n            counter=\"insert_count\",\n            vectors={\"embedding\": 4},\n            wait=True,",
				"\"(@category:{$p1} (@price:[-inf $p2]))=>[KNN 3 @embedding $vec AS vecdsl_score]\",",
				"params={\"vec\": query_vector, \"p1\": \"books\", \"p2\": max_price},",
				"metric=\"cosine\",\n            low=0.2,",
				"\"@title:({{and:t0}})\",\n                \"WITHSCORES\",\n                \"NOCONTENT\",\n                \"SCORER\",\n                \"BM25STD\",",
				"texts={\"t0\": query_text},",
				"\"FT.HYBRID\",",
				"\"COMBINE\",\n                \"RRF\",\n                \"4\",\n                \"CONSTANT\",\n                \"60\",\n                \"WINDOW\",\n                \"3\",",
				"return rt.get(self.client, prefix=\"products:\", key=\"id\", ids=[1, 2], output=[\"title\", \"price\"])",
				"\"(@category:{$p0} (@price:[-inf ($p1]))\",",
				"return rt.update(self.client, prefix=\"products:\", key=\"id\", set={\"price\": 35}, ids=[1], wait=True)",
				"\"(-(@id:[$p0 $p0]) -(@id:[$p1 $p1]))=>[KNN 2 @embedding $vec AS vecdsl_score]\",",
				"\"positive_ids\": [1],",
				"\"negative_ids\": [3],",
				"\"FT.AGGREGATE\",\n                \"products\",\n                \"@tags:{$p0}\",\n                \"LOAD\",\n                \"1\",\n                \"@__key\",\n                \"SORTBY\",\n                \"2\",\n                \"@id\",\n                \"ASC\",",
				"return rt.delete(self.client, prefix=\"products:\", ids=[4], wait=True)")) {
			assertTrue(python.contains(fragment), fragment + "\n" + python);
		}
	}

	@Test
	public void bindsHostileValuesAsParameters() throws Exception {
		String hostile = "x} | @price:[0 +inf] -@category:{y * %z% w'q' $p0 \\\" ' \\\\ ";
		String value = "x} | @price:[0 +inf] -@category:{y * %z% w'q' $p0 \" ' \\ ";
		String python = generate(sample() + String.join("\n", "",
				"hostileFilter: count in products where category eq \"" + hostile + "\"",
				"hostileText: search in products top 3 query text \"" + hostile + "\" fields [title] output [title]",
				"hostileWrite: insert into products values [ { id: 9, title: \"" + hostile + "\", embedding: [0.1, 0.2, 0.3, 0.4] } ]", "")).get(PYTHON);

		assertTrue(python.contains("\"@category:{$p0}\","), python);
		assertTrue(python.contains("params={\"p0\": " + Syntax.quote(value) + "},"), python);
		assertTrue(python.contains("texts={\"t0\": " + Syntax.quote(value) + "},"), python);
		assertTrue(python.contains("\"title\": " + Syntax.quote(value) + ","), python);
		assertFalse(python.contains("\"@category:{x}"), python);
		assertFalse(python.contains("@title:(x"), python);
	}

	@Test
	public void restrictsHostLanguagesThroughTheGeneratorParameter() throws Exception {
		System.setProperty(RedisGenerator.LANGUAGES_PROPERTY, "java");
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
		String recall = "target-recall: EF_RUNTIME and EPSILON are effort settings, with no declarable recall target";
		Map<String, String> files = generate(sample() + "\nrecallBound: vector-search in products top 3 query vector $queryVector"
				+ " using embedding params { target-recall 0.9 } output [title]\n");

		assertTrue(files.get(PYTHON).contains("rt.unsupported(\"redis\", \"" + recall), files.get(PYTHON));
		assertTrue(files.get(TYPESCRIPT).contains("return rt.unsupported(\"redis\", \"" + recall));
		assertTrue(files.get(JAVA).contains("throw unsupported(\"redis\", \"" + recall));
	}

	@Test
	public void textQueryInputIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyText: vector-search in products top 3 query text \"espresso\" using embedding output [title]\n", "text input" + NO_MODELS);
	}

	@Test
	public void embeddedConnectionsAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local path \"./product_catalog.db\""),
				"embedded deployments; the Redis clients connect to a server");
	}

	@Test
	public void passwordsWrittenInTheScriptAreUnsupported() throws Exception {
		assertUnsupported(replaced(CONNECTION, "create connection local uri \"redis://127.0.0.1:6379\" password \"written-in-the-script\""),
				"a password written in the script; read it from an environment variable with env(...)");
	}

	@Test
	public void snapshotsAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nnightly: snapshot products\n",
				"snapshots; SAVE and BGSAVE write one RDB file of the whole server under a fixed name (dir and dbfilename are protected configuration), and a restore needs a server restart");
	}

	@Test
	public void ifVersionIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nguarded: update products set { price: 1.0 } ids [1] if-version 3\n",
				"if-version; Redis keys carry no version number, and the generator adds no version field (WATCH protects a write only within one client's read-modify-write)");
	}

	@Test
	public void quantizationOfAnHnswIndexIsUnsupported() throws Exception {
		assertUnsupported(replaced(INDEX, INDEX + " { quantization type scalar bits 8 }"),
				"quantization of a HNSW index; the Query Engine compresses SVS-VAMANA indexes only (vector sets quantize to Q8 and BIN, but collections are Query Engine indexes)");
	}

	@Test
	public void regularExpressionFiltersAreUnsupported() throws Exception {
		assertUnsupported(sample() + "\nbyPattern: count in products where title regex \"^Gr.*\"\n",
				"the regex text operator; the Query Engine's query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions");
	}

	@Test
	public void releaseIsUnsupported() throws Exception {
		assertUnsupported(sample() + "\nunload: release products\n",
				"release; Redis keeps every document and index in memory and has no command that evicts one index");
	}

	private void assertUnsupported(String source, String feature) throws Exception {
		String call = "rt.unsupported(\"redis\", " + Syntax.quote(feature) + ")";
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
			if (key.startsWith(RedisGenerator.VENDOR)) {
				files.put(key.substring(RedisGenerator.VENDOR.length()), content.toString());
			}
		});
		return files;
	}
}
