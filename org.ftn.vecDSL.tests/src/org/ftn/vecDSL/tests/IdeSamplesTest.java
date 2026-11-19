package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.ide.server.contentassist.ContentAssistService;
import org.eclipse.xtext.ide.server.hover.IHoverService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * What the language server answers on two unmodified samples, the values a scripted LSP client must see (positions are
 * 0-based LSP positions). Diagnostics: the vecdsl.coverage.unsupported warnings at the places `vecdsl check samples`
 * reports (1-based: pinecone_demo.vecDSL 15:5, 21:5, 24:5, 26:5; chroma_demo.vecDSL 19:5); hover on
 * the keyword of the statement 'similar'; completion at the start of the clause line after it; the code actions of the
 * first warning once a second target is added. Statuses and reasons come from the generated coverage index, so only stable entries are asserted.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLIdeInjectorProvider.class)
public class IdeSamplesTest {

	/** A published diagnostic: start, end and the start of its message. */
	record Expected(Range range, String message) {
	}

	static Range range(int line, int start, int end) {
		return new Range(new Position(line, start), new Position(line, end));
	}

	static final List<Expected> PINECONE_DIAGNOSTICS = List.of(
			new Expected(range(14, 4, 12), "unsupported on pinecone: Pinecone configures text analysis on each full-text "
					+ "field when it creates the index."),
			new Expected(range(20, 4, 16), "unsupported on pinecone: the Pinecone index schema is fixed at creation. in a "
					+ "collection declaration the float_vector field is the dense vector"),
			new Expected(range(23, 4, 16), "unsupported on pinecone: the Pinecone index schema is fixed at creation. in a "
					+ "collection declaration the float_vector field is the dense vector"),
			new Expected(range(25, 4, 12), "unsupported on pinecone: the Pinecone index schema is fixed at creation. inside "
					+ "a collection declaration a bm25 function makes its inputs full-text fields"));

	static final List<Expected> CHROMA_DIAGNOSTICS = List.of(new Expected(range(18, 4, 16),
			"unsupported on chroma: a Chroma collection holds one embedding per record. in a collection declaration the "
					+ "single float_vector field is the embedding"));

	static final Position PINECONE_HOVER = new Position(35, 9);
	static final Range PINECONE_HOVER_RANGE = range(35, 9, 22);
	static final List<String> PINECONE_HOVER_FRAGMENTS = List.of("**vector-search**, construct `Method VectorSearch`",
			"Nearest-neighbour search over one vector field.", "| vendor | status | Python | TypeScript | Java | reference |",
			"| pinecone | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |");

	static final Position CHROMA_HOVER = new Position(31, 9);
	static final Range CHROMA_HOVER_RANGE = range(31, 9, 22);
	static final List<String> CHROMA_HOVER_FRAGMENTS = List.of("**vector-search**, construct `Method VectorSearch`",
			"Nearest-neighbour search over one vector field.",
			"| chroma | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |");

	static final Position PINECONE_COMPLETION = new Position(36, 4);
	static final Map<String, String> PINECONE_PROPOSALS = Map.of("metric", "pinecone: native", "output",
			"pinecone: native", "score", "pinecone: concealed", "sort",
			"unsupported on pinecone: Pinecone orders search matches by score");

	static final Position CHROMA_COMPLETION = new Position(32, 4);
	static final Map<String, String> CHROMA_PROPOSALS = Map.of("metric", "chroma: native", "output", "chroma: native",
			"score", "chroma: concealed", "sort", "unsupported on chroma: Chroma orders query results by distance");

	/**
	 * Every sample targets one vendor, and removing the only target is not offered (TargetsEdit); the client first adds
	 * a second target to line 2, then asks for the code actions of the first warning of the sample's vendor.
	 */
	static final String PINECONE_TWO_TARGETS = "targets [ pinecone, milvus ]";
	static final String PINECONE_FIX = "Remove 'pinecone' from targets";
	static final String PINECONE_TARGETS_AFTER_FIX = "targets [ milvus ]";
	static final String CHROMA_TWO_TARGETS = "targets [ chroma, qdrant ]";
	static final String CHROMA_FIX = "Remove 'chroma' from targets";
	static final String CHROMA_TARGETS_AFTER_FIX = "targets [ qdrant ]";

	@Inject
	private Provider<XtextResourceSet> resourceSets;

	@Inject
	private IResourceValidator validator;

	@Inject
	private IHoverService hoverService;

	@Inject
	private ContentAssistService contentAssist;

	@Inject
	private ICodeActionService2 codeActions;

	private XtextResource resource;
	private String text;

	private void open(String sample) throws Exception {
		Path path = Path.of("samples", sample);
		text = Files.readString(path);
		resource = (XtextResource) resourceSets.get()
				.createResource(URI.createFileURI(path.toAbsolutePath().toString()));
		resource.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), null);
	}

	private TextDocumentIdentifier id() {
		return new TextDocumentIdentifier(resource.getURI().toString());
	}

	private List<Diagnostic> diagnostics() {
		return IdeProtocol.diagnostics(validator.validate(resource, CheckMode.ALL, () -> false));
	}

	private void publishes(String sample, List<Expected> expected) throws Exception {
		open(sample);
		List<Diagnostic> diagnostics = diagnostics();
		assertEquals(expected.size(), diagnostics.size(), diagnostics.toString());
		for (int i = 0; i < expected.size(); i++) {
			Diagnostic diagnostic = diagnostics.get(i);
			assertEquals(CoverageCheck.UNSUPPORTED, diagnostic.getCode().getLeft());
			assertEquals(DiagnosticSeverity.Warning, diagnostic.getSeverity());
			assertEquals(expected.get(i).range(), diagnostic.getRange(), diagnostic.getMessage().getLeft());
			assertTrue(diagnostic.getMessage().getLeft().startsWith(expected.get(i).message()),
					diagnostic.getMessage().getLeft());
		}
	}

	private void hovers(String sample, Position position, Range range, List<String> fragments) throws Exception {
		open(sample);
		Hover hover = hoverService.hover(new Document(1, text), resource, new HoverParams(id(), position), () -> false);
		assertEquals(range, hover.getRange());
		String markdown = hover.getContents().getRight().getValue();
		fragments.forEach(fragment -> assertTrue(markdown.contains(fragment), markdown));
	}

	private void completes(String sample, Position position, Map<String, String> proposals) throws Exception {
		open(sample);
		List<CompletionItem> items = contentAssist
				.createCompletionList(new Document(1, text), resource, new CompletionParams(id(), position), () -> false)
				.getItems();
		proposals.forEach((label, detail) -> assertEquals(detail, items.stream()
				.filter(item -> item.getLabel().equals(label)).findFirst().orElseThrow().getDetail(), label));
		List<Boolean> unsupported = items.stream().map(item -> item.getDetail().startsWith("unsupported on")).toList();
		assertTrue(unsupported.subList(unsupported.indexOf(true), unsupported.size()).stream().allMatch(flag -> flag));
	}

	private void fixes(String sample, String vendor, String targets, String title, String after) throws Exception {
		open(sample);
		text = text.replace("targets [ " + vendor + " ]", targets);
		resource.reparse(text);
		Diagnostic first = diagnostics().stream()
				.filter(diagnostic -> diagnostic.getMessage().getLeft().startsWith("unsupported on " + vendor + ":"))
				.findFirst().orElseThrow();
		Map<String, String> actions = IdeProtocol.codeActions(codeActions, resource, text, first);
		assertEquals(List.of(title), List.copyOf(actions.keySet()));
		assertEquals(after, actions.get(title).lines().toList().get(1));
	}

	@Test
	public void pineconeDemoDiagnostics() throws Exception {
		publishes("pinecone_demo.vecDSL", PINECONE_DIAGNOSTICS);
	}

	@Test
	public void chromaDemoDiagnostics() throws Exception {
		publishes("chroma_demo.vecDSL", CHROMA_DIAGNOSTICS);
	}

	@Test
	public void pineconeDemoHover() throws Exception {
		hovers("pinecone_demo.vecDSL", PINECONE_HOVER, PINECONE_HOVER_RANGE, PINECONE_HOVER_FRAGMENTS);
	}

	@Test
	public void chromaDemoHover() throws Exception {
		hovers("chroma_demo.vecDSL", CHROMA_HOVER, CHROMA_HOVER_RANGE, CHROMA_HOVER_FRAGMENTS);
	}

	@Test
	public void pineconeDemoCompletion() throws Exception {
		completes("pinecone_demo.vecDSL", PINECONE_COMPLETION, PINECONE_PROPOSALS);
	}

	@Test
	public void chromaDemoCompletion() throws Exception {
		completes("chroma_demo.vecDSL", CHROMA_COMPLETION, CHROMA_PROPOSALS);
	}

	@Test
	public void pineconeDemoCodeAction() throws Exception {
		fixes("pinecone_demo.vecDSL", "pinecone", PINECONE_TWO_TARGETS, PINECONE_FIX, PINECONE_TARGETS_AFTER_FIX);
	}

	@Test
	public void chromaDemoCodeAction() throws Exception {
		fixes("chroma_demo.vecDSL", "chroma", CHROMA_TWO_TARGETS, CHROMA_FIX, CHROMA_TARGETS_AFTER_FIX);
	}
}
