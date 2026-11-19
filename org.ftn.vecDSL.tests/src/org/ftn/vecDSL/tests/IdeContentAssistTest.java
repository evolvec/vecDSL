package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.contentassist.ContentAssistService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * textDocument/completion through the language server's ContentAssistService, on the programs and with the expectations
 * of VecDSLContentAssistTest (org.ftn.vecDSL.ui.tests): the Eclipse display string "keyword  status" is here the item's
 * label and detail.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLIdeInjectorProvider.class)
public class IdeContentAssistTest {

	static final String BOOKS = "create collection books { primary id field id type int field title type varchar "
			+ "full-text vector-field embedding type float_vector dim 2 metric cosine }\n"
			+ "create collection notes { primary key field key type int vector-field a type float_vector dim 2 }\n";

	static final String FUZZINESS_REASON = "Milvus BM25 has no fuzzy matching";
	static final String FUZZINESS_ON_MILVUS = "unsupported on milvus: " + FUZZINESS_REASON;
	static final String FUZZINESS_ON_ELASTICSEARCH_AND_REDIS = "elasticsearch: native; redis: native";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ContentAssistService contentAssist;

	/** The completion items at the end of the text, in the order the server sends them (by sortText). */
	private List<CompletionItem> complete(String text) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		Document document = new Document(1, text);
		CompletionParams params = new CompletionParams(new TextDocumentIdentifier(resource.getURI().toString()),
				document.getPosition(text.length()));
		return contentAssist.createCompletionList(document, resource, params, () -> false).getItems();
	}

	private static CompletionItem item(List<CompletionItem> items, String label) {
		return items.stream().filter(item -> item.getLabel().equals(label)).findFirst().orElseThrow();
	}

	@Test
	public void unsupportedClauseIsMarkedAndRankedLast() throws Exception {
		List<CompletionItem> items = complete("script s targets [milvus, elasticsearch]\n" + BOOKS
				+ "k: search in books top 3 query text \"x\" ");
		CompletionItem fuzziness = item(items, "fuzziness");
		assertTrue(fuzziness.getDetail().contains(FUZZINESS_ON_MILVUS), fuzziness.getDetail());
		assertEquals(MarkupKind.MARKDOWN, fuzziness.getDocumentation().getRight().getKind());
		assertTrue(fuzziness.getDocumentation().getRight().getValue().contains(FUZZINESS_REASON));
		List<Boolean> unsupported = items.stream()
				.map(item -> item.getDetail() != null && item.getDetail().contains("unsupported on")).toList();
		assertTrue(unsupported.subList(unsupported.indexOf(true), unsupported.size()).stream().allMatch(flag -> flag),
				items.stream().map(item -> item.getLabel() + "  " + item.getDetail()).toList().toString());
	}

	@Test
	public void proposalsShowTheStatusOfEveryTarget() throws Exception {
		List<CompletionItem> items = complete("script s targets [elasticsearch, redis]\n" + BOOKS
				+ "k: search in books top 3 query text \"x\" ");
		assertEquals(FUZZINESS_ON_ELASTICSEARCH_AND_REDIS, item(items, "fuzziness").getDetail());
	}

	@Test
	public void nestedCreateProposesTheFieldsOfItsCollectionFirst() throws Exception {
		List<CompletionItem> items = complete("script s\n" + BOOKS + "more: create books with { create vector-index extra on ");
		assertEquals("embedding", items.get(0).getLabel(), items.stream().map(CompletionItem::getLabel).toList().toString());
	}

	@Test
	public void searchProposesOnlyTheDeclaredMetric() throws Exception {
		List<CompletionItem> items = complete("script s\n" + BOOKS
				+ "q: vector-search in books top 2 query vector [1, 0] using embedding metric ");
		assertEquals(List.of("cosine"), items.stream().map(CompletionItem::getLabel).toList());
	}
}
