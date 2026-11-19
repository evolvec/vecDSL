package org.ftn.vecDSL.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.validation.VecDSLValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A scripted LSP client against vecdsl-lsp (LSP4J's launcher on the client side, Connection) on the samples, the
 * rejected conformance programs and the quick fix programs. Expected values are those of
 * org.ftn.vecDSL.tests IdeSamplesTest and IdeQuickfixTest: LSP positions are 0-based, the 1-based line and column of
 * an Xtext issue less one. Surefire runs this class with the server in the test's JVM, failsafe again with the shaded
 * jar as a subprocess.
 */
public class LspClientTest {

	static final Path WORKSPACE = Path.of(System.getProperty("vecdsl.workspace", "..")).toAbsolutePath().normalize();
	static final Path SAMPLES = WORKSPACE.resolve("org.ftn.vecDSL.tests/samples");
	static final Path CONFORMANCE = WORKSPACE.resolve("spec/conformance");

	/** A published diagnostic: range and the start of its message. */
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

	static final String VECTOR_SEARCH = "**vector-search**, construct `Method VectorSearch`";
	static final String VECTOR_SEARCH_COMMENT = "Nearest-neighbour search over one vector field.";

	static final List<String> PINECONE_HOVER = List.of(VECTOR_SEARCH, VECTOR_SEARCH_COMMENT,
			"| vendor | status | Python | TypeScript | Java | reference |",
			"| pinecone | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |");

	static final List<String> CHROMA_HOVER = List.of(VECTOR_SEARCH, VECTOR_SEARCH_COMMENT,
			"| chroma | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |");

	static final Map<String, String> PINECONE_PROPOSALS = Map.of("metric", "pinecone: native", "output",
			"pinecone: native", "score", "pinecone: concealed", "sort",
			"unsupported on pinecone: Pinecone orders search matches by score");

	static final Map<String, String> CHROMA_PROPOSALS = Map.of("metric", "chroma: native", "output", "chroma: native",
			"score", "chroma: concealed", "sort", "unsupported on chroma: Chroma orders query results by distance");

	/** The program of IdeQuickfixTest and VecDSLQuickfixTest. */
	static final String COLLECTION = "script s\ncreate collection c { primary id field id type int field place type geo_point "
			+ "vector-field v type float_vector dim 2 metric cosine }\n";

	static final String SYNTAX = org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC;

	/** The labels of pinecone_demo.vecDSL's top-level statements, in order. */
	static final List<String> PINECONE_STATEMENTS = List.of("connectCloud", "dropOld", "createProducts", "seed",
			"similar", "keywords", "hybrid", "byId", "cheapBooks", "reprice", "similarToFirst", "browse", "removeOne",
			"remaining");

	static final String PINECONE_PROGRAM = "pinecone/python/product_catalog.py";

	static final List<String> REJECTED = List.of("linking_unknown_field", "syntax_params_order", "t_vf_ambiguous",
			"wf_create_top_alias", "wf_delete_two_selectors", "wf_recall_one", "wf_update_no_selector");

	private static Connection connection;

	@BeforeAll
	public static void start() throws Exception {
		System.out.println("LspClientTest: server " + Connection.mode());
		connection = Connection.start();
	}

	@AfterAll
	public static void stop() throws Exception {
		connection.end();
	}

	static String sample(String name) throws Exception {
		return Files.readString(SAMPLES.resolve(name));
	}

	static TextDocumentIdentifier id(String uri) {
		return new TextDocumentIdentifier(uri);
	}

	private static void publishes(String sample, List<Expected> expected) throws Exception {
		String uri = connection.open(sample, sample(sample));
		List<Diagnostic> diagnostics = connection.diagnostics(uri);
		connection.close(uri);
		assertPublished(expected, diagnostics);
	}

	static void assertPublished(List<Expected> expected, List<Diagnostic> diagnostics) {
		assertEquals(expected.size(), diagnostics.size(), diagnostics.toString());
		for (int i = 0; i < expected.size(); i++) {
			Diagnostic diagnostic = diagnostics.get(i);
			String message = diagnostic.getMessage().getLeft();
			assertEquals(CoverageCheck.UNSUPPORTED, diagnostic.getCode().getLeft());
			assertEquals(DiagnosticSeverity.Warning, diagnostic.getSeverity());
			assertEquals(expected.get(i).range(), diagnostic.getRange(), message);
			assertTrue(message.startsWith(expected.get(i).message()), message);
		}
	}

	private static void hovers(String sample, Position position, Range range, List<String> fragments)
			throws Exception {
		String uri = connection.open(sample, sample(sample));
		connection.diagnostics(uri);
		Hover hover = connection.get(connection.server().getTextDocumentService().hover(new HoverParams(id(uri), position)));
		connection.close(uri);
		assertHover(range, fragments, hover);
	}

	static void assertHover(Range range, List<String> fragments, Hover hover) {
		assertEquals(range, hover.getRange());
		assertEquals("markdown", hover.getContents().getRight().getKind());
		String markdown = hover.getContents().getRight().getValue();
		fragments.forEach(fragment -> assertTrue(markdown.contains(fragment), markdown));
	}

	private static void completes(String sample, Position position, Map<String, String> proposals) throws Exception {
		String uri = connection.open(sample, sample(sample));
		connection.diagnostics(uri);
		Either<List<CompletionItem>, CompletionList> result = connection
				.get(connection.server().getTextDocumentService().completion(new CompletionParams(id(uri), position)));
		connection.close(uri);
		List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();
		proposals.forEach((label, detail) -> assertEquals(detail, items.stream()
				.filter(item -> item.getLabel().equals(label)).findFirst().orElseThrow().getDetail(), label));
		List<Boolean> unsupported = items.stream().map(item -> item.getDetail().startsWith("unsupported on")).toList();
		assertTrue(unsupported.subList(unsupported.indexOf(true), unsupported.size()).stream().allMatch(flag -> flag));
	}

	/** Title to resulting text of every code action for the diagnostic, in the server's order. */
	private static Map<String, String> codeActions(String uri, Diagnostic diagnostic) throws Exception {
		List<Either<Command, CodeAction>> actions = connection.get(connection.server().getTextDocumentService()
				.codeAction(new CodeActionParams(id(uri), diagnostic.getRange(), new CodeActionContext(List.of(diagnostic)))));
		Map<String, String> result = new LinkedHashMap<>();
		for (Either<Command, CodeAction> action : actions) {
			result.put(action.getRight().getTitle(),
					Edits.apply(connection.text(uri), action.getRight().getEdit().getChanges().get(uri)));
		}
		return result;
	}

	private static void fixes(String sample, String vendor, String targets, String title, String after)
			throws Exception {
		String text = sample(sample);
		String uri = connection.open(sample, text);
		Diagnostic single = connection.diagnostics(uri).stream()
				.filter(diagnostic -> diagnostic.getMessage().getLeft().startsWith("unsupported on " + vendor + ":"))
				.findFirst().orElseThrow();
		Map<String, String> none = codeActions(uri, single);
		connection.change(uri, text.replace("targets [ " + vendor + " ]", targets));
		Diagnostic first = connection.diagnostics(uri).stream()
				.filter(diagnostic -> diagnostic.getMessage().getLeft().startsWith("unsupported on " + vendor + ":"))
				.findFirst().orElseThrow();
		Map<String, String> actions = codeActions(uri, first);
		connection.close(uri);
		assertEquals(Map.of(), none, "removing the only target is not offered");
		assertEquals(List.of(title), List.copyOf(actions.keySet()));
		assertEquals(after, actions.get(title).lines().toList().get(1));
	}

	/** Title to resulting text of every code action for the first published diagnostic with the code. */
	private static Map<String, String> quickFixes(String text, String code) throws Exception {
		String uri = connection.open("quickfix.vecDSL", text);
		Diagnostic diagnostic = connection.diagnostics(uri).stream()
				.filter(candidate -> code.equals(candidate.getCode().getLeft())).findFirst().orElseThrow();
		Map<String, String> actions = codeActions(uri, diagnostic);
		connection.close(uri);
		return actions;
	}

	private static String format(String uri, boolean insertSpaces) throws Exception {
		List<? extends TextEdit> edits = connection.get(connection.server().getTextDocumentService()
				.formatting(new DocumentFormattingParams(id(uri), new FormattingOptions(4, insertSpaces))));
		return Edits.apply(connection.text(uri), edits);
	}

	/**
	 * Every sample, with the line separator given, formatted by the server and by vecdsl fmt: FormattingOptions tabSize
	 * 4, insertSpaces true against fmt's default (four spaces), insertSpaces false against fmt --indent tab.
	 */
	private static void formatsLikeFmt(String separator) throws Exception {
		List<Path> samples;
		try (Stream<Path> files = Files.list(SAMPLES)) {
			samples = files.filter(file -> file.toString().endsWith(".vecDSL")).sorted().toList();
		}
		assertEquals(28, samples.size());
		List<String> differences = new ArrayList<>();
		for (Path sample : samples) {
			String text = Files.readString(sample).replace("\r\n", "\n").replace("\n", separator);
			String uri = connection.open(sample.getFileName().toString(), text);
			connection.diagnostics(uri);
			if (!format(uri, true).equals(References.fmt(text, "    "))) {
				differences.add(sample.getFileName() + " (spaces)");
			}
			if (!format(uri, false).equals(References.fmt(text, "\t"))) {
				differences.add(sample.getFileName() + " (tab)");
			}
			connection.close(uri);
		}
		assertEquals(List.of(), differences);
	}

	@Test
	public void initializeAnnouncesTheEditorServices() {
		ServerCapabilities capabilities = connection.capabilities;
		assertEquals(Boolean.TRUE, capabilities.getHoverProvider().getLeft());
		assertFalse(capabilities.getCompletionProvider().getResolveProvider());
		assertEquals(Boolean.TRUE, capabilities.getCodeActionProvider().getLeft());
		assertEquals(Boolean.TRUE, capabilities.getDocumentFormattingProvider().getLeft());
		assertEquals(TextDocumentSyncKind.Incremental, capabilities.getTextDocumentSync().getLeft());
		JsonObject experimental = assertInstanceOf(JsonObject.class, capabilities.getExperimental());
		JsonObject vecdsl = experimental.getAsJsonObject("vecdsl");
		assertEquals(GenerateEndpoint.METHOD, vecdsl.get("generate").getAsString());
		assertEquals(GenerateEndpoint.VERSION, vecdsl.get("generateVersion").getAsInt());
		assertEquals(14, vecdsl.getAsJsonArray("targets").size());
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
	public void rejectedProgramsGiveTheirConformanceErrors() throws Exception {
		for (String name : REJECTED) {
			Path folder = CONFORMANCE.resolve(name);
			String uri = connection.open(name + ".vecDSL", Files.readString(folder.resolve("program.vecDSL")));
			List<String> errors = connection.diagnostics(uri).stream()
					.filter(diagnostic -> diagnostic.getSeverity() == DiagnosticSeverity.Error)
					.map(diagnostic -> diagnostic.getCode().getLeft() + " " + (diagnostic.getRange().getStart().getLine() + 1)
							+ ":" + (diagnostic.getRange().getStart().getCharacter() + 1) + " "
							+ diagnostic.getMessage().getLeft())
					.toList();
			connection.close(uri);
			List<String> expected = new ArrayList<>();
			for (JsonElement element : JsonParser.parseString(Files.readString(folder.resolve("diagnostics.json")))
					.getAsJsonObject().getAsJsonArray("diagnostics")) {
				JsonObject diagnostic = element.getAsJsonObject();
				expected.add(diagnostic.get("code").getAsString() + " " + diagnostic.get("line").getAsInt() + ":"
						+ diagnostic.get("column").getAsInt() + " " + diagnostic.get("message").getAsString());
			}
			assertFalse(expected.isEmpty(), name);
			assertEquals(expected, errors, name);
		}
	}

	@Test
	public void pineconeDemoHover() throws Exception {
		hovers("pinecone_demo.vecDSL", new Position(35, 9), range(35, 9, 22), PINECONE_HOVER);
	}

	@Test
	public void chromaDemoHover() throws Exception {
		hovers("chroma_demo.vecDSL", new Position(31, 9), range(31, 9, 22), CHROMA_HOVER);
	}

	@Test
	public void pineconeDemoCompletion() throws Exception {
		completes("pinecone_demo.vecDSL", new Position(36, 4), PINECONE_PROPOSALS);
	}

	@Test
	public void chromaDemoCompletion() throws Exception {
		completes("chroma_demo.vecDSL", new Position(32, 4), CHROMA_PROPOSALS);
	}

	@Test
	public void pineconeDemoCodeAction() throws Exception {
		fixes("pinecone_demo.vecDSL", "pinecone", "targets [ pinecone, milvus ]", "Remove 'pinecone' from targets",
				"targets [ milvus ]");
	}

	@Test
	public void chromaDemoCodeAction() throws Exception {
		fixes("chroma_demo.vecDSL", "chroma", "targets [ chroma, qdrant ]", "Remove 'chroma' from targets",
				"targets [ qdrant ]");
	}

	@Test
	public void keywordLabelIsRenamed() throws Exception {
		assertEquals(Map.of("Rename 'batch' to 'batchStep'", COLLECTION + "batchStep: count in c\n"),
				quickFixes(COLLECTION + "batch: count in c\n", SYNTAX));
	}

	@Test
	public void paramsAreReordered() throws Exception {
		assertEquals(Map.of("Put the clauses of the block in grammar order",
				COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { candidates 20 timeout 5 } output *\n"),
				quickFixes(COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { timeout 5 candidates 20 } output *\n",
						SYNTAX));
	}

	@Test
	public void metricMismatchOffersTheDeclaredMetricAndRemoval() throws Exception {
		String search = "q: vector-search in c top 2 query vector [1, 0] using v metric dot output *\n";
		assertEquals(List.of(Map.entry("Remove the metric clause", COLLECTION + search.replace(" metric dot", "")),
				Map.entry("Use the declared metric 'cosine'", COLLECTION + search.replace("metric dot", "metric cosine"))),
				List.copyOf(quickFixes(COLLECTION + search, VecDSLValidator.METRIC_MISMATCH).entrySet()));
	}

	@Test
	public void geoPointStringBecomesAParameter() throws Exception {
		assertEquals(Map.of("Move the value into a parameter",
				"script s\nparam seedPlace: text = \"45.25,19.85\"\n" + COLLECTION.substring("script s\n".length())
						+ "seed: insert into c values [{ id: 1, place: $seedPlace, v: [1, 0] }]\n"),
				quickFixes(COLLECTION + "seed: insert into c values [{ id: 1, place: \"45.25,19.85\", v: [1, 0] }]\n",
						VecDSLValidator.RECORD_TYPE));
	}

	@Test
	public void unsupportedClauseOffersVendorAndClauseRemoval() throws Exception {
		String script = "script s targets [milvus, elasticsearch]\ncreate collection books { primary id field id type int "
				+ "field title type varchar full-text }\n";
		String search = "k: search in books top 3 query text \"x\" fuzziness \"AUTO\" output *\n";
		assertEquals(List.of(
				Map.entry("Remove 'milvus' from targets", script.replace("[milvus, elasticsearch]", "[elasticsearch]") + search),
				Map.entry("Remove the 'fuzziness' clause", script + search.replace(" fuzziness \"AUTO\"", ""))),
				List.copyOf(quickFixes(script + search, CoverageCheck.UNSUPPORTED).entrySet()));
	}

	@Test
	public void formattingEqualsFmtOnEverySample() throws Exception {
		formatsLikeFmt("\n");
	}

	@Test
	public void formattingKeepsCrlfOnEverySample() throws Exception {
		formatsLikeFmt("\r\n");
	}

	@Test
	public void brokenEditGivesASyntaxErrorAndTheRepairClearsIt() throws Exception {
		String text = sample("chroma_demo.vecDSL");
		String uri = connection.open("broken.vecDSL", text);
		connection.diagnostics(uri);
		connection.change(uri, text.replaceFirst("vector-search in", "vector-search inn"));
		List<Diagnostic> broken = connection.diagnostics(uri);
		connection.change(uri, text);
		List<Diagnostic> repaired = connection.diagnostics(uri);
		connection.close(uri);
		assertTrue(broken.stream().anyMatch(diagnostic -> diagnostic.getSeverity() == DiagnosticSeverity.Error
				&& SYNTAX.equals(diagnostic.getCode().getLeft())), broken.toString());
		assertTrue(repaired.stream().noneMatch(diagnostic -> diagnostic.getSeverity() == DiagnosticSeverity.Error),
				repaired.toString());
	}

	@Test
	public void generatePreviewEqualsGen() throws Exception {
		Path program = SAMPLES.resolve("pinecone_demo.vecDSL");
		String uri = connection.open("preview.vecDSL", Files.readString(program));
		connection.diagnostics(uri);
		GenerateEndpoint.GenerateResult result = connection.get(connection.server()
				.generate(new GenerateEndpoint.GenerateParams(uri, range(35, 2, 2), "pinecone", "py")));
		connection.close(uri);
		Path folder = Files.createTempDirectory("vecdsl-gen");
		Map<String, String> written = References.gen(program, "pinecone", "python", folder);
		Map<String, String> preview = new LinkedHashMap<>();
		result.files().forEach(file -> preview.put(file.path(), file.text()));
		assertEquals("similar", result.statement());
		assertEquals(List.of(), result.errors());
		assertFalse(written.isEmpty());
		assertEquals(written, new TreeMap<>(preview));
	}

	@Test
	public void generateWithoutTargetOutlinesTheScript() throws Exception {
		String uri = connection.open("outline.vecDSL", sample("pinecone_demo.vecDSL"));
		connection.diagnostics(uri);
		GenerateEndpoint.GenerateResult result = connection.get(
				connection.server().generate(new GenerateEndpoint.GenerateParams(uri, range(35, 2, 2), null, null)));
		connection.close(uri);
		assertEquals("similar", result.statement());
		assertEquals(List.of(), result.files());
		assertEquals(List.of(), result.errors());
		assertEquals(List.of("pinecone"), result.targets());
		assertEquals(PINECONE_STATEMENTS, result.statements().stream().map(GenerateEndpoint.Statement::name).toList());
		assertEquals(new Range(new Position(35, 0), new Position(38, 53)), result.statements().get(4).range());
		assertEquals(List.of(), result.sections());
		assertEquals(List.of(), result.unsupported());
	}

	@Test
	public void generateLocatesTheStatementInTheFiles() throws Exception {
		Path program = SAMPLES.resolve("pinecone_demo.vecDSL");
		String uri = connection.open("sections.vecDSL", Files.readString(program));
		connection.diagnostics(uri);
		GenerateEndpoint.GenerateResult result = connection.get(connection.server()
				.generate(new GenerateEndpoint.GenerateParams(uri, range(35, 0, 0), "pinecone", "py")));
		connection.close(uri);
		List<String> lines = References.gen(program, "pinecone", "python", Files.createTempDirectory("vecdsl-gen"))
				.get(PINECONE_PROGRAM).lines().toList();
		List<GenerateEndpoint.Section> sections = result.sections();
		assertEquals(2, sections.size(), sections.toString());
		assertTrue(sections.stream().allMatch(section -> section.path().equals(PINECONE_PROGRAM)), sections.toString());
		List<String> member = lines.subList(sections.get(0).start(), sections.get(0).end());
		assertEquals("    def similar(self, query_vector, max_price):", member.get(0));
		assertEquals("        )", member.get(member.size() - 1));
		assertEquals("", lines.get(sections.get(0).end()));
		assertEquals(1, sections.get(1).end() - sections.get(1).start());
		assertTrue(lines.get(sections.get(1).start()).contains("program.similar("), sections.toString());
	}

	@Test
	public void generateLocatesAnUnlabeledStatement() throws Exception {
		String uri = connection.open("unlabeled.vecDSL", COLLECTION + "count in c\n");
		connection.diagnostics(uri);
		GenerateEndpoint.GenerateResult result = connection.get(connection.server()
				.generate(new GenerateEndpoint.GenerateParams(uri, range(2, 0, 0), "milvus", "java")));
		connection.close(uri);
		assertNull(result.statement());
		assertNull(result.statements().get(1).name());
		Map<String, String> files = new LinkedHashMap<>();
		result.files().forEach(file -> files.put(file.path(), file.text()));
		GenerateEndpoint.Section member = result.sections().get(0);
		String header = files.get(member.path()).lines().toList().get(member.start());
		assertTrue(header.contains("count") && header.endsWith("{"), header);
	}

	@Test
	public void generateReportsTheUnsupportedConstructsAsTheWarningsDo() throws Exception {
		String uri = connection.open("unsupported.vecDSL", sample("pinecone_demo.vecDSL"));
		List<Diagnostic> warnings = connection.diagnostics(uri);
		GenerateEndpoint.GenerateResult result = connection.get(connection.server()
				.generate(new GenerateEndpoint.GenerateParams(uri, range(12, 0, 0), "pinecone", "py")));
		connection.close(uri);
		assertEquals("createProducts", result.statement());
		assertEquals(
				warnings.stream().map(warning -> warning.getRange() + " " + warning.getMessage().getLeft()).toList(),
				result.unsupported().stream().map(entry -> entry.range() + " " + entry.message()).toList());
		assertTrue(result.unsupported().stream().allMatch(entry -> entry.vendor().equals("pinecone")));
	}

	@Test
	public void generateRejectsAnUnknownTarget() throws Exception {
		String uri = connection.open("unknown-target.vecDSL", sample("chroma_demo.vecDSL"));
		connection.diagnostics(uri);
		ExecutionException failure = assertThrows(ExecutionException.class, () -> connection.get(
				connection.server().generate(new GenerateEndpoint.GenerateParams(uri, null, "nosuchvendor", null))));
		connection.close(uri);
		ResponseErrorException error = assertInstanceOf(ResponseErrorException.class, failure.getCause());
		assertEquals(ResponseErrorCode.InvalidParams.getValue(), error.getResponseError().getCode());
	}

	@Test
	public void shutdownAndExitEndTheServerWithStatusZero() throws Exception {
		assertEquals(0, Connection.start().shutdownAndExit());
	}
}
