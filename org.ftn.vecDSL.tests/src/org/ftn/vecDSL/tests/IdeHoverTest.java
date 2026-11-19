package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.hover.IHoverService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * textDocument/hover through the language server's IHoverService, on the program and for the cases of VecDSLHoverTest
 * (org.ftn.vecDSL.ui.tests); the Eclipse hover is HTML, this one Markdown with the same content.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLIdeInjectorProvider.class)
public class IdeHoverTest {

	static final String SCRIPT = "script s targets [milvus, redis]\nparam q: vector = [1, 0]\n"
			+ "create collection books { primary id field id type int field title type varchar full-text "
			+ "vector-field embedding type float_vector dim 2 metric cosine }\n"
			+ "k: search in books top 3 query text \"x\" fuzziness \"AUTO\" output *\n"
			+ "n: vector-search in books top 2 query vector $q using embedding output *\n"
			+ "create connection c uri \"x\" api-key env(\"VECDSL_KEY\")\n";

	static final String FUZZINESS_UNSUPPORTED = "Milvus BM25 has no fuzzy matching";
	static final String FUZZINESS_REDIS_ROW = "| redis | native |";
	static final String VECTOR_SEARCH_CONCEPT = "Nearest-neighbour search over one vector field.";
	static final String EMBEDDING_FIELD = "**embedding**: vector field of type float_vector, dim 2, metric cosine";
	static final String BOOKS_TITLE_FIELD = "- `field title type varchar full-text`";
	static final String PARAMETER_Q = "**parameter** `param q: vector = [1, 0]`";
	static final String ENVIRONMENT_VARIABLE = "**environment variable** `VECDSL_KEY`";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private IHoverService hoverService;

	/** The Markdown of the hover at the offset of the script. */
	private String hover(int offset) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(SCRIPT).eResource();
		Document document = new Document(1, SCRIPT);
		Hover hover = hoverService.hover(document, resource,
				new HoverParams(new TextDocumentIdentifier(resource.getURI().toString()), document.getPosition(offset)),
				() -> false);
		assertEquals(MarkupKind.MARKDOWN, hover.getContents().getRight().getKind());
		return hover.getContents().getRight().getValue();
	}

	private void hasHoverOver(String text, String expected) throws Exception {
		String markdown = hover(SCRIPT.indexOf(text));
		assertTrue(markdown.contains(expected), markdown);
	}

	@Test
	public void keywordShowsConceptAndCoverage() throws Exception {
		hasHoverOver("fuzziness", FUZZINESS_UNSUPPORTED);
		hasHoverOver("fuzziness", FUZZINESS_REDIS_ROW);
		hasHoverOver("vector-search", VECTOR_SEARCH_CONCEPT);
	}

	@Test
	public void fieldShowsTypeDimensionMetricAndCollection() throws Exception {
		hasHoverOver("embedding", EMBEDDING_FIELD);
	}

	@Test
	public void collectionShowsItsFields() throws Exception {
		hasHoverOver("books", BOOKS_TITLE_FIELD);
	}

	@Test
	public void parameterAndEnvironmentReferenceShowTheirDeclaration() throws Exception {
		String markdown = hover(SCRIPT.indexOf("$q") + 1);
		assertTrue(markdown.contains(PARAMETER_Q), markdown);
		hasHoverOver("VECDSL_KEY", ENVIRONMENT_VARIABLE);
	}

	@Test
	public void hoverMarkdownHasNoHtml() throws Exception {
		for (String text : new String[] { "fuzziness", "embedding", "books", "VECDSL_KEY" }) {
			String markdown = hover(SCRIPT.indexOf(text));
			assertTrue(!markdown.contains("<p>") && !markdown.contains("<td>") && !markdown.contains("<code>"), markdown);
		}
	}
}
