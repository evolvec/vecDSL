package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.ShapeSpec;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.vecDSL.QueryMethod;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * The runtime spec ShapeSpec builds for the result-shape clauses (generator/common/shapes/README.md), printed as JSON. The
 * statements of the pgvector full sample are those whose expected values the shared module's fixture checks.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ShapeSpecTest {

	private static final String BOOKS = "script shapes\n"
			+ "param queryVector : vector = [0.9, 0.1, 0.0, 0.1]\n"
			+ "param queryText : text = \"vector databases\"\n"
			+ "create collection books {\n"
			+ "    primary id\n"
			+ "    field id type int\n"
			+ "    field title type varchar max-length 256\n"
			+ "    field genre type varchar max-length 64\n"
			+ "    field price type float\n"
			+ "    field tags type array element-type varchar\n"
			+ "    field published type datetime\n"
			+ "    field details type struct {\n"
			+ "        field pageCount type int\n"
			+ "        field lang type varchar\n"
			+ "    }\n"
			+ "    vector-field embedding type float_vector dim 4 metric cosine\n"
			+ "    vector-field alt type float_vector dim 4\n"
			+ "}\n";

	/** Every key with nothing written, in the module's order; a case replaces the keys it writes. */
	private static final String[][] EMPTY = { { "offset", "null" }, { "limit", "null" }, { "cursor", "null" }, { "bounds", "null" },
			{ "score", "\"similarity\"" }, { "boost", "null" }, { "mmr", "null" }, { "group", "null" }, { "sort", "[]" }, { "withTotal", "false" },
			{ "aggregations", "[]" }, { "output", "null" }, { "exclude", "[]" }, { "withVectors", "false" }, { "keyField", "\"id\"" } };

	@Inject
	private ParseHelper<Script> parseHelper;

	private List<QueryMethod> queries(String statements) throws Exception {
		Script script = parseHelper.parse(BOOKS + statements);
		assertTrue(script.eResource().getErrors().isEmpty(), () -> "syntax errors: " + script.eResource().getErrors());
		List<QueryMethod> result = new ArrayList<>();
		script.eAllContents().forEachRemaining(object -> {
			if (object instanceof QueryMethod query) result.add(query);
		});
		return result;
	}

	private ShapeSpec shape(String statement) throws Exception {
		List<QueryMethod> found = queries(statement);
		assertEquals(1, found.size());
		return ShapeSpec.of(found.get(0));
	}

	/** The expected spec: the empty spec with the given keys replaced, as compact JSON. */
	private static String expected(String... replaced) {
		List<String> parts = new ArrayList<>();
		for (String[] entry : EMPTY) {
			String value = entry[1];
			for (int i = 0; i < replaced.length; i += 2) {
				if (replaced[i].equals(entry[0])) value = replaced[i + 1];
			}
			parts.add("\"" + entry[0] + "\":" + value);
		}
		return "{" + String.join(",", parts) + "}";
	}

	private static String json(Code.Expr expr) {
		return switch (expr) {
		case Code.Nil nil -> "null";
		case Code.Flag flag -> Boolean.toString(flag.value());
		case Code.Num num -> Syntax.number(num.value());
		case Code.Text text -> Syntax.quote(text.value());
		case Code.Param param -> "$" + param.name();
		case Code.Items items -> "[" + String.join(",", items.items().stream().map(ShapeSpecTest::json).toList()) + "]";
		case Code.Entries entries -> {
			List<String> parts = new ArrayList<>();
			for (Map.Entry<String, Code.Expr> entry : entries.entries().entrySet()) parts.add(Syntax.quote(entry.getKey()) + ":" + json(entry.getValue()));
			yield "{" + String.join(",", parts) + "}";
		}
		default -> throw new IllegalArgumentException(expr.toString());
		};
	}

	@Test
	public void aSearchWithoutShapeClausesIsPlain() throws Exception {
		ShapeSpec spec = shape("similar: vector-search in books top 3 query vector $queryVector using embedding output [title, price] where genre eq \"databases\"\n");
		assertTrue(spec.plain());
		assertEquals("3", json(spec.window()));
		assertEquals(expected("limit", "3", "output", "[\"title\",\"price\"]"), json(spec.spec()));
		assertEquals(List.of(), spec.reads());
		assertNull(spec.diversityField());
	}

	@Test
	public void offsetZeroIsWrittenAndNoOffsetIsNull() throws Exception {
		List<QueryMethod> found = queries("a: vector-search in books top 2 query vector $queryVector using embedding offset 0 output [title]\n"
				+ "b: vector-search in books top 2 query vector $queryVector using embedding output [title]\n");
		assertEquals(expected("offset", "0", "limit", "2", "output", "[\"title\"]"), json(ShapeSpec.of(found.get(0)).spec()));
		assertEquals(expected("limit", "2", "output", "[\"title\"]"), json(ShapeSpec.of(found.get(1)).spec()));
	}

	@Test
	public void boundsScoreFormAndVectors() throws Exception {
		ShapeSpec spec = shape("pagedSimilar: vector-search in books top 2 query vector $queryVector using embedding offset 1\n"
				+ "    score similarity bounds min 0.5 with-vectors output [title]\n");
		assertTrue(spec.plain());
		assertEquals("3", json(spec.window()));
		assertEquals(expected("offset", "1", "limit", "2", "bounds", "{\"min\":0.5,\"max\":null}", "output", "[\"title\"]", "withVectors", "true"),
				json(spec.spec()));
	}

	@Test
	public void cursorTotalAndAggregation() throws Exception {
		ShapeSpec after = shape("pageAfter: vector-search in books top 2 query vector $queryVector using embedding cursor \"1\" output [title]\n");
		assertFalse(after.plain());
		assertEquals("20", json(after.window()));
		assertEquals(expected("limit", "2", "cursor", "\"1\"", "output", "[\"title\"]"), json(after.spec()));
		ShapeSpec counted = shape("counted: vector-search in books top 2 query vector $queryVector using embedding with-total aggregate { agg max on price }"
				+ " output [title] where genre eq \"databases\"\n");
		assertEquals(expected("limit", "2", "withTotal", "true",
				"aggregations", "[{\"name\":\"max_price\",\"kind\":\"max\",\"field\":[\"price\"],\"interval\":null,\"size\":null}]", "output", "[\"title\"]"),
				json(counted.spec()));
		assertEquals(List.of("price"), counted.reads());
	}

	@Test
	public void aggregationNamesIntervalsAndSizes() throws Exception {
		ShapeSpec spec = shape("genreStats: search in books top 3 query text \"databases\" fields [title] with-total aggregate {\n"
				+ "    agg terms on genre as genres size 5\n"
				+ "    agg stats on price\n"
				+ "    agg histogram on details.pageCount interval \"100\"\n"
				+ "    agg date_histogram on published interval \"1d\"\n"
				+ "    agg percentiles on price\n"
				+ "} output [title]\n");
		assertEquals("30", json(spec.window()));
		assertEquals(expected("limit", "3", "withTotal", "true", "aggregations", "["
				+ "{\"name\":\"genres\",\"kind\":\"terms\",\"field\":[\"genre\"],\"interval\":null,\"size\":5},"
				+ "{\"name\":\"stats_price\",\"kind\":\"stats\",\"field\":[\"price\"],\"interval\":null,\"size\":null},"
				+ "{\"name\":\"histogram_details_pageCount\",\"kind\":\"histogram\",\"field\":[\"details\",\"pageCount\"],\"interval\":100,\"size\":null},"
				+ "{\"name\":\"date_histogram_published\",\"kind\":\"date_histogram\",\"field\":[\"published\"],\"interval\":\"1d\",\"size\":null},"
				+ "{\"name\":\"percentiles_price\",\"kind\":\"percentiles\",\"field\":[\"price\"],\"interval\":null,\"size\":null}]",
				"output", "[\"title\"]"), json(spec.spec()));
		assertEquals(List.of("genre", "price", "details", "published"), spec.reads());
	}

	@Test
	public void boostsOfTheSample() throws Exception {
		ShapeSpec spec = shape("boosted: vector-search in books top 3 query vector $queryVector using embedding boost {\n"
				+ "    boost-fn cheap kind decay field price decay gauss origin 30.0 scale 20.0\n"
				+ "    boost-fn sqlTag kind condition weight 2.0 when tags contains \"sql\"\n"
				+ "    combine sum\n"
				+ "} output [title, price]\n");
		assertEquals("30", json(spec.window()));
		assertEquals(expected("limit", "3", "boost", "{\"functions\":["
				+ "{\"name\":\"cheap\",\"kind\":\"decay\",\"field\":[\"price\"],\"weight\":null,\"shape\":\"gauss\",\"origin\":30,\"scale\":20,\"offset\":null,\"rate\":null,\"when\":null},"
				+ "{\"name\":\"sqlTag\",\"kind\":\"condition\",\"field\":null,\"weight\":2,\"shape\":null,\"origin\":null,\"scale\":null,\"offset\":null,\"rate\":null,"
				+ "\"when\":{\"field\":[\"tags\"],\"op\":\"contains\",\"value\":\"sql\"}}],"
				+ "\"combine\":\"sum\",\"apply\":null}", "output", "[\"title\",\"price\"]"), json(spec.spec()));
		assertEquals(List.of("price", "tags"), spec.reads());
	}

	@Test
	public void boostDecayOverTimeAndConditions() throws Exception {
		ShapeSpec spec = shape("recent: vector-search in books top 2 query vector $queryVector using embedding boost {\n"
				+ "    boost-fn kind decay field published decay exp origin \"2026-11-16T00:00:00Z\" scale \"1d\" offset \"12h\" rate 0.25\n"
				+ "    boost-fn kind field_value field details.pageCount weight 0.5 when not (genre eq \"kitchen\" or price is_null)\n"
				+ "    boost-fn kind condition weight 3 when price between 10 to 40 and tags length gt 2 and title contains_text \"vector\"\n"
				+ "    combine max\n"
				+ "    apply sum\n"
				+ "} output *\n");
		String functions = "[{\"name\":null,\"kind\":\"decay\",\"field\":[\"published\"],\"weight\":null,\"shape\":\"exp\",\"origin\":\"2026-11-16T00:00:00Z\","
				+ "\"scale\":\"1d\",\"offset\":\"12h\",\"rate\":0.25,\"when\":null},"
				+ "{\"name\":null,\"kind\":\"field_value\",\"field\":[\"details\",\"pageCount\"],\"weight\":0.5,\"shape\":null,\"origin\":null,\"scale\":null,"
				+ "\"offset\":null,\"rate\":null,\"when\":{\"not\":{\"or\":[{\"field\":[\"genre\"],\"op\":\"eq\",\"value\":\"kitchen\"},"
				+ "{\"field\":[\"price\"],\"op\":\"is_null\"}]}}},"
				+ "{\"name\":null,\"kind\":\"condition\",\"field\":null,\"weight\":3,\"shape\":null,\"origin\":null,\"scale\":null,\"offset\":null,\"rate\":null,"
				+ "\"when\":{\"and\":[{\"and\":[{\"field\":[\"price\"],\"op\":\"between\",\"value\":10,\"upper\":40},"
				+ "{\"field\":[\"tags\"],\"op\":\"length_gt\",\"value\":2}]},{\"field\":[\"title\"],\"op\":\"contains_text\",\"value\":\"vector\"}]}}]";
		assertEquals(expected("limit", "2", "boost", "{\"functions\":" + functions + ",\"combine\":\"max\",\"apply\":\"sum\"}"), json(spec.spec()));
	}

	@Test
	public void mmrPoolFieldAndMetric() throws Exception {
		ShapeSpec diverse = shape("diverse: vector-search in books top 3 query vector $queryVector using embedding mmr lambda 0.2 candidates 6 output [title]\n");
		assertEquals("6", json(diverse.window()));
		assertEquals("embedding", diverse.diversityField());
		assertEquals(expected("limit", "3", "mmr", "{\"lambda\":0.2,\"candidates\":6,\"vectorField\":\"embedding\",\"metric\":\"cosine\"}",
				"output", "[\"title\"]"), json(diverse.spec()));
		ShapeSpec other = shape("b: vector-search in books top 2 query vector $queryVector using embedding mmr lambda 0.5 using alt output [title]\n");
		assertEquals("20", json(other.window()));
		assertEquals(expected("limit", "2", "mmr", "{\"lambda\":0.5,\"candidates\":null,\"vectorField\":\"alt\",\"metric\":null}", "output", "[\"title\"]"),
				json(other.spec()));
	}

	@Test
	public void groupsOnAHybridSearch() throws Exception {
		ShapeSpec spec = shape("hybridGrouped: hybrid-search in books top 2 fusion rrf group-by genre size 1 with-total highlight [title] {\n"
				+ "    vector { query vector $queryVector using embedding limit 6 }\n"
				+ "    keyword { query text $queryText fields [title] limit 6 }\n"
				+ "} output [title]\n");
		assertEquals("20", json(spec.window()));
		assertEquals(expected("limit", "2", "group", "{\"field\":[\"genre\"],\"size\":1}", "withTotal", "true", "output", "[\"title\"]"), json(spec.spec()));
	}

	@Test
	public void recommendationWithMmrAndSort() throws Exception {
		ShapeSpec spec = shape("suggestDiverse: recommend in books top 2 positive [1] using embedding mmr lambda 0.3 sort { sort-by price desc } output [title, price]\n");
		assertEquals("20", json(spec.window()));
		assertEquals(expected("limit", "2", "mmr", "{\"lambda\":0.3,\"candidates\":null,\"vectorField\":\"embedding\",\"metric\":\"cosine\"}",
				"sort", "[{\"field\":[\"price\"],\"direction\":\"desc\"}]", "output", "[\"title\",\"price\"]"), json(spec.spec()));
	}

	@Test
	public void nativeScoresAndRelevanceSort() throws Exception {
		ShapeSpec spec = shape("a: vector-search in books top 2 query vector $queryVector using embedding score native exclude [tags]"
				+ " sort { sort-by relevance asc } output *\n");
		assertEquals(expected("limit", "2", "score", "\"native\"", "sort", "[{\"relevance\":true,\"direction\":\"asc\"}]", "exclude", "[\"tags\"]"),
				json(spec.spec()));
	}

	@Test
	public void scrolls() throws Exception {
		ShapeSpec after = shape("scrollAfter: scroll in books limit 2 cursor \"5\" sort { sort-by price desc } output [title, price]\n");
		assertFalse(after.plain());
		assertEquals("null", json(after.window()));
		assertEquals(expected("limit", "2", "cursor", "\"5\"", "score", "null", "sort", "[{\"field\":[\"price\"],\"direction\":\"desc\"}]",
				"output", "[\"title\",\"price\"]"), json(after.spec()));
		ShapeSpec all = shape("everything: scroll in books output [title]\n");
		assertTrue(all.plain());
		assertEquals("null", json(all.window()));
		assertEquals(expected("score", "null", "output", "[\"title\"]"), json(all.spec()));
	}

	@Test
	public void theModuleSourcesAreResources() {
		assertTrue(ShapeSpec.module("python").contains("def shape(candidates"));
		assertTrue(ShapeSpec.module("typescript").contains("export function shape(candidates"));
		assertTrue(ShapeSpec.module("java").startsWith("package {{java.package}};"));
		assertEquals("Shapes.java", ShapeSpec.moduleFile("java"));
		assertThrows(IllegalArgumentException.class, () -> ShapeSpec.module("go"));
	}

	@Test
	public void combinationsPgvectorRefuses() throws Exception {
		String search = "a: vector-search in books top 2 query vector $queryVector using embedding ";
		String[][] refused = {
				{ "cursor \"1\" group-by genre output [title]",
						"the 'cursor' clause with group-by or mmr; keyset pagination continues after one record in an order that each record's own values decide" },
				{ "group-by genre sort { sort-by price asc } output [title]", "sort with group-by; groups follow the relevance of their best hit" },
				{ "mmr lambda 0.5 group-by genre output [title]", "mmr with group-by; MMR selects single hits, groups select records per group value" },
				{ "score native boost { boost-fn kind condition weight 2 when price gt 10 } output [title]",
						"boosts with score native; boosts apply to the canonical similarity, where higher is better" },
				{ "aggregate { agg range on price } output [title]", "range aggregations; vecDSL gives no range bounds to count records in" },
				{ "aggregate { agg histogram on price interval \"1d\" } output [title]", "a histogram without a numeric interval" },
				{ "boost { boost-fn kind condition weight 2 when title match \"vector\" } output [title]",
						"the text operator match in a boost condition; the shared result-shape module tests values, not analyzed text (contains_text is a substring test)" } };
		for (String[] entry : refused) {
			QueryMethod query = queries(search + entry[0] + "\n").get(0);
			Unsupported error = assertThrows(Unsupported.class, () -> ShapeSpec.of(query), entry[0]);
			assertEquals(entry[1], error.getMessage());
		}
		QueryMethod count = queries("n: count in books\n").get(0);
		assertThrows(IllegalArgumentException.class, () -> ShapeSpec.of(count));
	}
}
