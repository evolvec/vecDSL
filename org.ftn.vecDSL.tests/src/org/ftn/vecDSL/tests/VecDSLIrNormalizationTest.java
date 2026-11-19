package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.ir.IrReader;
import org.ftn.vecDSL.ir.Json;
import org.ftn.vecDSL.ir.VecDSLIr;
import org.ftn.vecDSL.vecDSL.Insert;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/** One test per normalization rule of spec/SEMANTICS.md (N1 to N14), each on a minimal program. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLIrNormalizationTest {

	private static final String DOCS = "script s\n"
			+ "create collection docs { primary id field id type int field title type varchar vector-field type float_vector dim 2 metric dot }\n";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private VecDSLIr ir;

	private Map<?, ?> program(String source) throws Exception {
		Map<?, ?> document = (Map<?, ?>) Json.parse(ir.export(parseHelper.parse(source).eResource()));
		assertTrue(document.containsKey("program"), () -> "rejected: " + document.get("diagnostics"));
		return document;
	}

	private Map<?, ?> method(Map<?, ?> document, int index) {
		return map(list(map(document.get("program")).get("methods")).get(index));
	}

	private static Map<?, ?> norm(Map<?, ?> element) {
		return map(element.get("$norm"));
	}

	private static Map<?, ?> map(Object value) {
		return (Map<?, ?>) value;
	}

	private static List<?> list(Object value) {
		return (List<?>) value;
	}

	private static BigDecimal number(Object value) {
		return (BigDecimal) value;
	}

	@Test
	public void n01UndeclaredFeaturesAreNullEvenWhenTheyEqualTheDefaultLiteral() throws Exception {
		Map<?, ?> document = program(DOCS + "a: count in docs\nb: count in docs consistency strong\n");
		assertNull(method(document, 1).get("consistency"));
		assertEquals("strong", method(document, 2).get("consistency"));
		assertEquals(Boolean.FALSE, method(document, 1).get("exact"));
	}

	@Test
	public void n02ScoreFormIsSimilarityUnlessNative() throws Exception {
		Map<?, ?> document = program(DOCS + "a: vector-search in docs top 2 query vector [1, 0] output *\n"
				+ "b: vector-search in docs top 2 query vector [1, 0] score native output *\n");
		Map<?, ?> similarity = map(norm(method(document, 1)).get("score"));
		assertEquals("similarity", similarity.get("form"));
		assertEquals(Boolean.TRUE, similarity.get("higherIsBetter"));
		assertEquals(List.of(), list(similarity.get("range")).stream().filter(bound -> bound != null).toList());
		Map<?, ?> nativeScore = map(norm(method(document, 2)).get("score"));
		assertEquals("native", nativeScore.get("form"));
		assertNull(nativeScore.get("range"));
	}

	@Test
	public void n03UnnamedVectorFieldIsNamedVector() throws Exception {
		Map<?, ?> document = program(DOCS + "a: vector-search in docs top 2 query vector [1, 0] using ^vector output *\n");
		Map<?, ?> field = map(list(map(method(document, 0).get("definition")).get("fields")).get(2));
		assertNull(field.get("name"));
		assertEquals("vector", norm(field).get("name"));
		assertEquals("docs.vector", method(document, 1).get("vectorField"));
	}

	@Test
	public void n04IndexIsAttachedToItsFieldAndDecomposed() throws Exception {
		Map<?, ?> document = program("script s\ncreate collection docs { primary id field id type int "
				+ "vector-field e type float_vector dim 2 { vector-index ix family hnsw { quantization type scalar bits 8 } } }\n");
		Map<?, ?> field = map(list(map(method(document, 0).get("definition")).get("fields")).get(1));
		assertEquals("docs.e.ix", norm(field).get("index"));
		Map<?, ?> index = norm(map(field.get("index")));
		assertEquals("docs.e", index.get("field"));
		assertEquals("hnsw", index.get("family"));
		assertEquals("scalar", index.get("quantization"));
		assertNull(index.get("accelerator"));
	}

	@Test
	public void n05TheOnlyVectorFieldIsAddressed() throws Exception {
		Map<?, ?> document = program(DOCS + "a: vector-search in docs top 2 query vector [1, 0] output *\n");
		assertNull(method(document, 1).get("vectorField"));
		assertEquals("docs.vector", norm(method(document, 1)).get("field"));
	}

	@Test
	public void n06MetricInForceComesFromTheQueryTheFieldOrItsIndex() throws Exception {
		Map<?, ?> fromField = program(DOCS + "a: vector-search in docs top 2 query vector [1, 0] output *\n");
		assertEquals("dot", norm(method(fromField, 1)).get("metric"));
		Map<?, ?> fromIndex = program("script s\ncreate collection docs { primary id field id type int "
				+ "vector-field e type float_vector dim 2 { vector-index ix family flat metric euclidean } }\n"
				+ "a: vector-search in docs top 2 query vector [1, 0] output *\n");
		assertEquals("euclidean", norm(method(fromIndex, 1)).get("metric"));
		assertEquals(List.of(), list(map(norm(method(fromIndex, 1)).get("score")).get("range")).subList(0, 1)
				.stream().filter(bound -> bound != null).toList());
	}

	@Test
	public void n07LegsUseTheirOwnOrTheSharedInput() throws Exception {
		Map<?, ?> document = program(DOCS + "a: hybrid-search in docs top 2 query text \"databases\" fusion rrf "
				+ "{ vector { query vector [1, 0] } keyword { fields [title] } } output *\n");
		List<?> legs = list(method(document, 1).get("legs"));
		assertEquals("own", norm(map(legs.get(0))).get("input"));
		assertEquals("shared", norm(map(legs.get(1))).get("input"));
	}

	@Test
	public void n08SnapshotNameIsTheAsClauseOrTheLabel() throws Exception {
		Map<?, ?> document = program(DOCS + "nightly: snapshot docs\nweekly: snapshot docs as \"w1\"\n");
		assertEquals("nightly", norm(method(document, 1)).get("snapshotName"));
		assertEquals("w1", norm(method(document, 2)).get("snapshotName"));
	}

	@Test
	public void n09ParametersAndEnvironmentAreTypedSlots() throws Exception {
		Map<?, ?> document = program("script s\nparam q : vector\n"
				+ "c: create connection main uri \"http://localhost\" api-key env(\"KEY\")\n"
				+ "create collection docs { primary id field id type int vector-field type float_vector dim 2 }\n"
				+ "a: vector-search in docs top 2 query vector $q output *\n");
		Map<?, ?> slot = map(norm(map(map(method(document, 2).get("input")).get("value"))).get("slot"));
		assertEquals("param", slot.get("kind"));
		assertEquals("vector", slot.get("type"));
		assertEquals(Boolean.FALSE, slot.get("hasDefault"));
		Map<?, ?> env = map(norm(map(map(method(document, 0).get("definition")).get("apiKey"))).get("slot"));
		assertEquals("env", env.get("kind"));
		assertEquals("KEY", env.get("name"));
	}

	@Test
	public void n10SearchParamsListTheAdmittedAndTheWrittenKeys() throws Exception {
		Map<?, ?> document = program(DOCS + "a: vector-search in docs top 2 query vector [1, 0] params { candidates 20 } output *\n"
				+ "b: search in docs top 2 query text \"x\" fields [title] params { timeout 5 } output *\n");
		Map<?, ?> vector = norm(map(method(document, 1).get("params")));
		assertTrue(list(vector.get("admitted")).contains("target-recall"));
		assertEquals(List.of("candidates"), vector.get("written"));
		Map<?, ?> keyword = norm(map(method(document, 2).get("params")));
		assertFalse(list(keyword.get("admitted")).contains("target-recall"));
		assertEquals("KeywordSearch", keyword.get("form"));
	}

	@Test
	public void n11TargetsAreKeptAsDeclared() throws Exception {
		Map<?, ?> restricted = program("script s targets [ qdrant, milvus ]\n");
		assertEquals(List.of("qdrant", "milvus"), map(restricted.get("program")).get("targets"));
		assertEquals(Boolean.TRUE, norm(map(restricted.get("program"))).get("restricted"));
		assertEquals(Boolean.FALSE, norm(map(program("script s\n").get("program"))).get("restricted"));
	}

	@Test
	public void n12ReferencesAreQualifiedPathsWithAnOccurrenceIndex() throws Exception {
		Map<?, ?> document = program("script s\n"
				+ "create collection docs { primary id field id type int }\n"
				+ "create collection notes { primary id field id type int }\n"
				+ "a: count in notes where id gt 1\n");
		Map<?, ?> count = method(document, 2);
		assertEquals("notes", count.get("collection"));
		assertEquals("notes.id", map(map(map(count.get("filter")).get("condition")).get("target")).get("field"));
		Map<?, ?> twice = program("script s\n"
				+ "create collection docs { primary id field id type int }\n"
				+ "d: drop docs\n"
				+ "create collection docs { primary id field id type int }\n"
				+ "a: count in docs\n");
		assertTrue(String.valueOf(method(twice, 3).get("collection")).matches("docs#[12]"));
	}

	@Test
	public void n13ExtensionsAreGroupedByVendor() throws Exception {
		String source = DOCS + "w: insert into docs values [ { id: 1 } ] "
				+ "extension milvus { a = 1 } extension qdrant { b = 2 } extension milvus { c = 3 }\n";
		VecDSLIr.RoundTrip roundTrip = ir.roundTrip(parseHelper.parse(source).eResource());
		assertTrue(roundTrip.passed(), () -> String.join("; ", roundTrip.failures()));
		Map<?, ?> document = program(source);
		Map<?, ?> extensions = map(method(document, 1).get("extensions"));
		assertEquals(List.of("milvus", "qdrant"), List.copyOf(extensions.keySet()));
		assertEquals(2, list(extensions.get("milvus")).size());
		Script rebuilt = new IrReader().read(document);
		Insert insert = (Insert) rebuilt.getMethods().get(1);
		assertEquals(List.of("milvus", "milvus", "qdrant"),
				insert.getExtensions().stream().map(block -> block.getVendor()).toList());
	}

	@Test
	public void n14NumbersAreCanonicalAndLongLiteralsKeepTheirText() throws Exception {
		Map<?, ?> document = program(DOCS + "a: vector-search in docs top 2 query vector [0.12345678901234567890123, 1.50] output *\n");
		List<?> items = list(map(map(method(document, 1).get("input")).get("value")).get("items"));
		assertEquals(0, new BigDecimal("1.5").compareTo(number(map(items.get(1)).get("value"))));
		Map<?, ?> literals = map(document.get("literals"));
		assertEquals(1, literals.size());
		assertEquals("0.12345678901234567890123", literals.values().iterator().next());
	}
}
