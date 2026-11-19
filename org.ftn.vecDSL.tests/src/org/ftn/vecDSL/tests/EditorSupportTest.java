package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.Construct;
import org.ftn.vecDSL.editor.ConstructLocator;
import org.ftn.vecDSL.editor.FieldContext;
import org.ftn.vecDSL.editor.GrammarDocs;
import org.ftn.vecDSL.editor.HoverText;
import org.ftn.vecDSL.editor.SearchMetrics;
import org.ftn.vecDSL.editor.TargetStatus;
import org.ftn.vecDSL.services.VecDSLGrammarAccess;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Insert;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.SearchParams;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * The editor logic that runs without the workbench: which construct a keyword stands for, the vendor status shown
 * with a proposal, the metric a search may name, the collection whose fields come first, and the hover texts.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class EditorSupportTest {

	private static final Path GRAMMAR = Path.of("..", "org.ftn.vecDSL", "src", "org", "ftn", "vecDSL", "VecDSL.xtext");
	private static final String BOOKS = "script s targets [milvus, pinecone, redis]\n"
			+ "create collection books { primary id field id type int field title type varchar max-length 64 full-text "
			+ "vector-field embedding type float_vector dim 2 metric cosine }\n"
			+ "create collection notes { primary key field key type int vector-field a type float_vector dim 2 "
			+ "vector-field b type float_vector dim 2 { vector-index bIndex family hnsw metric dot } }\n";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private VecDSLGrammarAccess grammar;

	private Keyword keyword(String rule, String value) {
		return GrammarUtil.containedKeywords(GrammarUtil.findRuleForName(grammar.getGrammar(), rule)).stream()
				.filter(keyword -> keyword.getValue().equals(value)).findFirst().orElseThrow();
	}

	private Script parse(String statements) throws Exception {
		return parseHelper.parse(BOOKS + statements);
	}

	private static <T extends EObject> T first(EObject root, Class<T> type) {
		return EcoreUtil2.eAllOfType(root, type).get(0);
	}

	@Test
	public void statementKeywordStandsForTheClass() {
		assertEquals("Method VectorSearch", ConstructLocator.locate(keyword("VectorSearch", "vector-search"), null).id());
		assertEquals("Concept VectorCollection", ConstructLocator.locate(keyword("VectorCollection", "collection"), null).id());
		assertEquals("AlterOperation AddFieldOp", ConstructLocator.locate(keyword("AddFieldOp", "add-field"), null).id());
	}

	@Test
	public void clauseKeywordStandsForItsFeature() {
		assertEquals("KeywordSearch.fuzziness", ConstructLocator.locate(keyword("KeywordSearch", "fuzziness"), null).id());
		assertEquals("VectorSearch.exact", ConstructLocator.locate(keyword("VectorSearch", "exact"), null).id());
		assertEquals("HybridSearch.alpha", ConstructLocator.locate(keyword("HybridSearch", "alpha"), null).id());
	}

	@Test
	public void fragmentKeywordStandsForAFeatureOfTheStatement() throws Exception {
		VectorSearch search = first(parse("q: vector-search in books top 2 query vector [1, 0] output *"), VectorSearch.class);
		assertEquals("VectorSearch.offset", ConstructLocator.locate(keyword("Paging", "offset"), search).id());
		assertEquals("VectorSearch.tenant", ConstructLocator.locate(keyword("ReadScope", "tenant"), search).id());
	}

	@Test
	public void helperRuleKeywordStandsForTheFeatureThatHoldsIt() throws Exception {
		Script script = parse("q: vector-search in books top 2 query vector [1, 0] params { timeout 5 } output *\n"
				+ "i: insert into books values [{ id: 1 }]");
		VectorSearch search = first(script, VectorSearch.class);
		assertEquals("VectorSearch.input", ConstructLocator.locate(keyword("QueryInput", "query"), search).id());
		assertEquals("VectorSearch.params", ConstructLocator.locate(keyword("SearchParams", "candidates"),
				first(script, SearchParams.class)).id());
		assertEquals("Insert.source", ConstructLocator.locate(keyword("InlineRecords", "values"),
				first(script, Insert.class)).id());
	}

	@Test
	public void unsupportedTargetRanksLastWithItsReason() {
		CoverageIndex index = CoverageIndex.load(Set.of("milvus", "pinecone"), vendor -> json(vendor,
				vendor.equals("milvus") ? "unsupported" : "native", vendor.equals("pinecone") ? "construct" : "none"));
		Construct fuzziness = ConstructLocator.locate(keyword("KeywordSearch", "fuzziness"), null);
		TargetStatus status = TargetStatus.of(index, List.of("milvus", "pinecone", "redis"), fuzziness);
		assertTrue(status.isUnsupported());
		assertEquals("unsupported on milvus: no fuzzy matching", status.summary());
		assertEquals("supported by pinecone, not generated yet", status.vendors().get(1).label());
		assertEquals("no coverage data", status.vendors().get(2).label());
	}

	@Test
	public void optionWithoutAFeatureRowUsesTheClassRow() {
		CoverageIndex index = CoverageIndex.load(Set.of("milvus"), vendor -> new ByteArrayInputStream((
				"{\"vendor\": \"milvus\", \"constructs\": [" + row("Concept VectorCollection", "VectorCollection", "native",
						"none") + "]}").getBytes(StandardCharsets.UTF_8)));
		Construct consistency = ConstructLocator.locate(keyword("VectorCollection", "consistency"), null);
		assertEquals("VectorCollection.consistency", consistency.id());
		assertEquals("milvus: native", TargetStatus.of(index, List.of("milvus"), consistency).summary());
	}

	@Test
	public void searchMayNameOnlyTheDeclaredMetric() throws Exception {
		Script script = parse("a: vector-search in books top 2 query vector [1, 0] output *\n"
				+ "b: vector-search in notes top 2 query vector [1, 0] using b output *\n"
				+ "c: vector-search in notes top 2 query vector [1, 0] using a output *");
		List<VectorSearch> searches = EcoreUtil2.eAllOfType(script, VectorSearch.class);
		assertEquals(EMetric.COSINE, SearchMetrics.declared(searches.get(0)));
		assertEquals(EMetric.DOT, SearchMetrics.declared(searches.get(1)));
		assertNull(SearchMetrics.declared(searches.get(2)));
	}

	@Test
	public void nestedCreateNamesTheFieldsOfItsCollectionFirst() throws Exception {
		Script script = parse("more: create books with { create vector-index extra on embedding family flat }");
		VectorIndex index = first(script.getMethods().get(2), VectorIndex.class);
		VectorCollection books = (VectorCollection) ((Create) script.getMethods().get(0)).getDefinition();
		VectorCollection notes = (VectorCollection) ((Create) script.getMethods().get(1)).getDefinition();
		assertEquals(books, FieldContext.collection(index));
		assertTrue(FieldContext.isMember(EcoreUtil.getURI(books.getFields().get(2)), books));
		assertFalse(FieldContext.isMember(EcoreUtil.getURI(notes.getFields().get(1)), books));
	}

	@Test
	public void grammarCommentsExplainRulesAndClauses() throws IOException {
		GrammarDocs docs = grammarDocs();
		assertEquals("Nearest-neighbour search over one vector field.", docs.rule("VectorSearch"));
		assertTrue(docs.clause("HybridSearch", "alpha").startsWith("weight of the first vector leg"));
		assertEquals(Set.of(3), GrammarDocs.notes(docs.rule("ScoreSpec")));
	}

	@Test
	public void keywordHoverShowsConceptNotesAndCoverage() throws Exception {
		CoverageIndex index = CoverageIndex.load(Set.of("milvus"), vendor -> json(vendor, "native", "none"));
		VectorSearch search = first(parse("q: vector-search in books top 2 query vector [1, 0] output *"), VectorSearch.class);
		Construct bounds = ConstructLocator.locate(keyword("ScoreSpec", "bounds"), search);
		assertEquals("VectorSearch.bounds", bounds.id());
		String html = HoverText.keyword("bounds", "ScoreSpec", grammarDocs(),
				TargetStatus.of(index, List.of("milvus", "redis"), bounds));
		assertTrue(html.contains("Score form and score bounds (D3)."), html);
		assertTrue(html.contains("<i>D3:</i>"), html);
		assertTrue(html.contains("<td>milvus</td><td>native</td><td><code>search</code></td><td><code>search</code></td>"), html);
		assertTrue(html.contains("<td>redis</td><td>no coverage data</td>"), html);
	}

	@Test
	public void referenceHoversDescribeTheirTarget() throws Exception {
		Script script = parseHelper.parse(BOOKS.replaceFirst("\n", "\nparam q: vector = [1, 0]\n")
				+ "q1: vector-search in notes top 2 query vector $q using b output *\n"
				+ "create connection c uri \"x\" api-key env(\"KEY\")");
		VectorCollection notes = (VectorCollection) ((Create) script.getMethods().get(1)).getDefinition();
		VectorField b = (VectorField) notes.getFields().get(2);
		assertEquals("<p><b>b</b>: vector field of type float_vector, dim 2, metric dot<br/>in collection <b>notes</b></p>",
				HoverText.field(b));
		assertTrue(HoverText.collection(notes).contains("<li><code>field key type int</code></li>"));
		assertEquals("<p><b>parameter</b> <code>param q: vector = [1, 0]</code></p>",
				HoverText.parameter(first(script, Parameter.class)));
		assertTrue(HoverText.environment(first(script, EnvRef.class)).contains("<code>KEY</code>"));
	}

	private static GrammarDocs grammarDocs() throws IOException {
		try (InputStream in = Files.newInputStream(GRAMMAR)) {
			return GrammarDocs.read(in);
		}
	}

	private static InputStream json(String vendor, String status, String pending) {
		String text = "{\"vendor\": \"" + vendor + "\", \"constructs\": [" + row("Method KeywordSearch", "KeywordSearch",
				"native", "none") + ", " + row("KeywordSearch.fuzziness", "KeywordSearch", status, pending) + ", "
				+ row("Method VectorSearch", "VectorSearch", "native", "none") + "]}";
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}

	private static String row(String construct, String eClass, String status, String pending) {
		return "{\"construct\": \"" + construct + "\", \"eClass\": \"" + eClass + "\", \"feature\": null, \"rule\": \""
				+ eClass + "\", \"status\": \"" + status + "\", \"pending\": \"" + pending + "\", \"reason\": \""
				+ "no fuzzy matching\", \"calls\": {\"python\": \"`search`\", \"typescript\": \"`search`\", \"java\": \"x\"}, "
				+ "\"reference\": \"https://example.org/a (op)\", \"test\": \"t\", \"notes\": \"n\"}";
	}
}
