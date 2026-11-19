package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.ClauseOrder;
import org.ftn.vecDSL.editor.ClauseRegion;
import org.ftn.vecDSL.editor.ClauseReorder;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.editor.KeywordRename;
import org.ftn.vecDSL.editor.ParameterExtraction;
import org.ftn.vecDSL.editor.SearchMetrics;
import org.ftn.vecDSL.editor.TargetsEdit;
import org.ftn.vecDSL.editor.TextEdit;
import org.ftn.vecDSL.services.VecDSLGrammarAccess;
import org.ftn.vecDSL.validation.VecDSLValidator;
import org.ftn.vecDSL.vecDSL.Count;
import org.ftn.vecDSL.vecDSL.KeywordSearch;
import org.ftn.vecDSL.vecDSL.RecordEntry;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/** The edits the quick fixes make, computed and checked without the workbench. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class EditorFixesTest {

	private static final String HEADER = "script s targets [milvus, qdrant]\n";
	private static final String BOOKS = "create collection books { primary id field id type int field genre type varchar "
			+ "field place type geo_point vector-field embedding type float_vector dim 2 metric cosine "
			+ "partition hot partition cold }\n";
	private static final VecDSLPackage P = VecDSLPackage.eINSTANCE;

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@Inject
	private VecDSLGrammarAccess grammar;

	private int syntaxErrors(String text) {
		try {
			XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
			int count = 0;
			for (Object ignored : resource.getParseResult().getSyntaxErrors()) {
				count++;
			}
			return count;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Optional<KeywordRename.Proposal> rename(String text, String word) {
		return KeywordRename.propose(text, text.indexOf(word), ClauseOrder.keywordWords(grammar.getGrammar()),
				this::syntaxErrors);
	}

	@Test
	public void keywordLabelsGetANameOfTheirOwn() {
		for (String keyword : List.of("batch", "between", "phrase", "alpha", "collections", "stopwords")) {
			String text = HEADER + BOOKS + keyword + ": count in books\n";
			KeywordRename.Proposal proposal = rename(text, keyword + ":").orElseThrow(() -> new AssertionError(keyword));
			assertEquals(keyword + "Step", proposal.name());
			assertEquals(0, syntaxErrors(TextEdit.apply(text, proposal.edits())), keyword);
		}
	}

	@Test
	public void keywordFieldIsRenamedWhereItIsANameOnly() {
		String text = HEADER + "create collection c { primary id field id type int field batch type int }\n"
				+ "w: upsert into c values [{ id: 1, batch: 2 }] batch 10\nn: count in c where batch gt 1\n";
		KeywordRename.Proposal proposal = rename(text, "batch type").orElseThrow();
		assertEquals("batchField", proposal.name());
		String fixed = TextEdit.apply(text, proposal.edits());
		assertEquals(HEADER + "create collection c { primary id field id type int field batchField type int }\n"
				+ "w: upsert into c values [{ id: 1, batchField: 2 }] batch 10\nn: count in c where batchField gt 1\n", fixed);
		assertEquals(0, syntaxErrors(fixed));
	}

	@Test
	public void wordThatIsNoKeywordGetsNoRename() {
		String text = HEADER + BOOKS + "n: count in books where genre eq\n";
		assertTrue(rename(text, "genre eq").isEmpty());
	}

	@Test
	public void paramsAreReorderedToGrammarOrder() {
		String text = HEADER + BOOKS + "q: vector-search in books top 2 query vector [1, 0] params { timeout 5 rescore candidates 20 } output *\n";
		TextEdit edit = ClauseReorder.reorder(text, text.indexOf("candidates"), ClauseOrder.blocks(grammar.getGrammar()),
				this::syntaxErrors).orElseThrow();
		String fixed = TextEdit.apply(text, List.of(edit));
		assertTrue(fixed.contains("params { candidates 20 timeout 5 rescore }"), fixed);
		assertEquals(0, syntaxErrors(fixed));
	}

	@Test
	public void multilineLegKeepsItsLayout() {
		String text = HEADER + BOOKS + "h: hybrid-search in books top 2 {\n    vector {\n        using embedding\n"
				+ "        query vector [1, 0]\n    }\n    vector {\n        query vector [0, 1]\n    }\n} output *\n";
		TextEdit edit = ClauseReorder.reorder(text, text.indexOf("query vector [1, 0]"),
				ClauseOrder.blocks(grammar.getGrammar()), this::syntaxErrors).orElseThrow();
		String fixed = TextEdit.apply(text, List.of(edit));
		assertTrue(fixed.contains("vector {\n        query vector [1, 0]\n        using embedding\n    }"), fixed);
	}

	@Test
	public void blockInOrderGetsNoReorder() {
		String text = HEADER + BOOKS + "q: vector-search in books top 2 query vector [1, 0] params { candidates 20 timeout 5 } output *\n";
		assertTrue(ClauseReorder.reorder(text, text.indexOf("timeout"), ClauseOrder.blocks(grammar.getGrammar()),
				this::syntaxErrors).isEmpty());
	}

	@Test
	public void metricMismatchIsFixedByTheDeclaredMetricOrByRemovingTheClause() throws Exception {
		String text = HEADER + BOOKS + "q: vector-search in books top 2 query vector [1, 0] using embedding metric dot output *\n";
		Script script = parseHelper.parse(text);
		validation.assertError(script, P.getVectorSearch(), VecDSLValidator.METRIC_MISMATCH);
		VectorSearch search = EcoreUtil2.eAllOfType(script, VectorSearch.class).get(0);
		String declared = SearchMetrics.declared(search).getLiteral();
		assertEquals("cosine", declared);
		validation.assertNoErrors(parseHelper.parse(text.replace("metric dot", "metric " + declared)));
		String removed = TextEdit.apply(text, List.of(ClauseRegion.removal(search, P.getVectorSearch_Metric())));
		assertTrue(removed.contains("using embedding output *"), removed);
		validation.assertNoErrors(parseHelper.parse(removed));
	}

	@Test
	public void clauseRemovalTakesTheWholeClause() throws Exception {
		String text = HEADER + BOOKS + "n: count in books exact\n    partitions [hot, cold]\n    where genre eq \"x\"\n";
		Count count = EcoreUtil2.eAllOfType(parseHelper.parse(text), Count.class).get(0);
		assertEquals(HEADER + BOOKS + "n: count in books exact\n    where genre eq \"x\"\n",
				TextEdit.apply(text, List.of(ClauseRegion.removal(count, P.getCount_Partitions()))));
		assertEquals(HEADER + BOOKS + "n: count in books\n    partitions [hot, cold]\n    where genre eq \"x\"\n",
				TextEdit.apply(text, List.of(ClauseRegion.removal(count, P.getCount_Exact()))));
		assertNull(ClauseRegion.removal(count, P.getCount_Tenant()));
	}

	@Test
	public void geoPointStringBecomesAParameter() throws Exception {
		String text = HEADER + "param q: vector = [1, 0]\n" + BOOKS
				+ "seed: insert into books values [{ id: 1, place: \"45.25,19.85\", embedding: [1, 0] }]\n";
		Script script = parseHelper.parse(text);
		validation.assertError(script, P.getRecordEntry(), VecDSLValidator.RECORD_TYPE);
		RecordEntry entry = EcoreUtil2.eAllOfType(script, RecordEntry.class).stream()
				.filter(ParameterExtraction::applies).findFirst().orElseThrow();
		String fixed = TextEdit.apply(text, ParameterExtraction.edits(entry));
		assertEquals(HEADER + "param q: vector = [1, 0]\nparam seedPlace: text = \"45.25,19.85\"\n" + BOOKS
				+ "seed: insert into books values [{ id: 1, place: $seedPlace, embedding: [1, 0] }]\n", fixed);
		validation.assertNoErrors(parseHelper.parse(fixed));
	}

	@Test
	public void vendorLeavesTheTargetsList() throws Exception {
		Script script = parseHelper.parse("script s targets [milvus, qdrant, chroma]\n");
		String text = "script s targets [milvus, qdrant, chroma]\n";
		assertEquals("script s targets [milvus, chroma]\n", TextEdit.apply(text, List.of(TargetsEdit.removeVendor(script, "qdrant"))));
		assertEquals("script s targets [qdrant, chroma]\n", TextEdit.apply(text, List.of(TargetsEdit.removeVendor(script, "milvus"))));
		assertNull(TargetsEdit.removeVendor(parseHelper.parse("script s targets [milvus]\n"), "milvus"));
	}

	@Test
	public void unsupportedClassesAndClausesAreFound() throws Exception {
		CoverageIndex index = CoverageIndex.load(Set.of("milvus", "qdrant"), vendor -> json(vendor, vendor.equals("milvus")
				? Map.of("Method KeywordSearch", "native", "KeywordSearch.fuzziness", "unsupported", "Method Count", "native")
				: Map.of("Method KeywordSearch", "unsupported", "KeywordSearch.fuzziness", "unsupported")));
		Script script = parseHelper.parse(HEADER + BOOKS
				+ "k: search in books top 2 query text \"x\" fuzziness \"AUTO\" output *\nn: count in books\n");
		List<CoverageCheck.Finding> findings = CoverageCheck.findings(script, index);
		assertEquals(2, findings.size(), findings.toString());
		assertEquals(P.getKeywordSearch_Fuzziness(), findings.get(0).feature());
		assertEquals("unsupported on milvus: no fuzzy matching", findings.get(0).message());
		assertTrue(findings.get(1).object() instanceof KeywordSearch);
		assertNull(findings.get(1).feature());
	}

	private static java.io.InputStream json(String vendor, Map<String, String> statuses) {
		StringBuilder rows = new StringBuilder();
		statuses.forEach((construct, status) -> {
			rows.append(rows.length() == 0 ? "" : ", ").append("{\"construct\": \"").append(construct)
					.append("\", \"eClass\": \"x\", \"feature\": null, \"rule\": \"x\", \"status\": \"").append(status)
					.append("\", \"pending\": \"none\", \"reason\": \"no fuzzy matching\", \"calls\": {\"python\": \"\", "
							+ "\"typescript\": \"\", \"java\": \"\"}, \"reference\": \"\", \"test\": \"\", \"notes\": \"\"}");
		});
		String text = "{\"vendor\": \"" + vendor + "\", \"constructs\": [" + rows + "]}";
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}
