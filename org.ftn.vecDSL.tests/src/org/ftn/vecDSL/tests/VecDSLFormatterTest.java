package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper;
import org.eclipse.xtext.testing.formatter.FormatterTestRequest;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.ExceptionAcceptor;
import org.ftn.vecDSL.formatting2.VecDSLFormatter;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Formatter rules, one test each, and three properties over every sample program: formatting is idempotent, the
 * formatted program parses to the same model, and formatting changes whitespace only.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLFormatterTest {

	private static final Path SAMPLES = Path.of("samples");
	private static final String COLLECTION = "create collection c { primary id field id type int vector-field v type float_vector dim 2 }\n";

	@Inject
	private FormatterTestHelper formatter;

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private Provider<IFormatter2> formatterProvider;

	@Inject
	private Provider<FormatterRequest> requestProvider;

	@Inject
	private TextRegionAccessBuilder regionAccessBuilder;

	private void assertFormatted(String input, String expected) {
		formatter.assertFormatted(request -> configure(request).setToBeFormatted(input).setExpectation(expected));
	}

	private static FormatterTestRequest configure(FormatterTestRequest request) {
		return request.setUseSerializer(false).preferences(preferences -> {
			preferences.put(FormatterPreferenceKeys.indentation, "    ");
			preferences.put(FormatterPreferenceKeys.lineSeparator, "\n");
		});
	}

	@Test
	public void longStatementPutsEachClauseOnAnIndentedLine() {
		assertFormatted("script s\n" + COLLECTION
				+ "near: vector-search in c top 3 query vector [0.1, 0.2] using v score similarity bounds min 0.2 output [id] where id gt 1",
				"script s\n\n" + expandedCollection()
						+ "\n\nnear: vector-search in c\n    top 3\n    query vector [0.1, 0.2]\n    using v\n    score similarity\n"
						+ "    bounds min 0.2\n    output [id]\n    where id gt 1\n");
	}

	@Test
	public void shortStatementWithoutBlockStaysOnOneLine() {
		assertFormatted("script s\nn:   count   in c\n   where id gt 1", "script s\n\nn: count in c where id gt 1\n");
	}

	@Test
	public void shortStatementWidthIsAPreference() {
		formatter.assertFormatted(request -> configure(request)
				.preferences(preferences -> preferences.put(VecDSLFormatter.SHORT_STATEMENT_WIDTH, 20))
				.setToBeFormatted("script s\nn: count in c where id gt 1")
				.setExpectation("script s\n\nn: count in c\n    where id gt 1\n"));
	}

	@Test
	public void blockElementsGoOnePerLineOneLevelDeeper() {
		assertFormatted("script s\nq: vector-search in c top 2 query vector [1, 0] params { candidates 20 timeout 5 } output *",
				"script s\n\nq: vector-search in c\n    top 2\n    query vector [1, 0]\n    params {\n        candidates 20\n"
						+ "        timeout 5\n    }\n    output *\n");
	}

	@Test
	public void collectionBodyHasOneMemberPerLine() {
		assertFormatted("script s\n" + COLLECTION, "script s\n\n" + expandedCollection() + "\n");
	}

	@Test
	public void nestedBlocksIndentOneLevelPerBlock() {
		assertFormatted("script s\ncreate collection c { primary id field id type int vector-field v type float_vector dim 2 "
				+ "{ vector-index vi family hnsw { quantization type scalar bits 8 } } } if-not-exists",
				"script s\n\ncreate collection c {\n    primary id\n    field id type int\n"
						+ "    vector-field v type float_vector dim 2 {\n        vector-index vi family hnsw {\n"
						+ "            quantization type scalar bits 8\n        }\n    }\n} if-not-exists\n");
	}

	@Test
	public void nestedCreateStatementsGoOnePerLine() {
		assertFormatted("script s\naddIndex: create c with { create payload-index p on id kind integer create tenant t }",
				"script s\n\naddIndex: create c with {\n    create payload-index p on id kind integer\n    create tenant t\n}\n");
	}

	@Test
	public void hybridLegsFormABlockClause() {
		assertFormatted("script s\nh: hybrid-search in c top 3 fusion rrf { vector { query vector [1, 0] using v } "
				+ "keyword { query text \"x\" } } output *",
				"script s\n\nh: hybrid-search in c\n    top 3\n    fusion rrf\n    {\n        vector {\n"
						+ "            query vector [1, 0]\n            using v\n        }\n        keyword {\n"
						+ "            query text \"x\"\n        }\n    }\n    output *\n");
	}

	@Test
	public void valueListOfSeveralRecordsHasOneRecordPerLine() {
		assertFormatted("script s\nseed: insert into c values [{id: 1, v: [1, 0]},{id: 2, v: [0, 1]}] wait",
				"script s\n\nseed: insert into c\n    values [\n        { id: 1, v: [1, 0] },\n        { id: 2, v: [0, 1] }\n"
						+ "    ]\n    wait\n");
	}

	@Test
	public void singleRecordStaysInline() {
		assertFormatted("script s\nsingle: insert into c values [ {id: 1, v: [1, 0]} ] wait",
				"script s\n\nsingle: insert into c values [{ id: 1, v: [1, 0] }] wait\n");
	}

	@Test
	public void statementsAreSeparatedByOneBlankLine() {
		assertFormatted("script s\na: count in c\n\n\n\nb: count in c c2: count in c",
				"script s\n\na: count in c\n\nb: count in c\n\nc2: count in c\n");
	}

	@Test
	public void scriptHeaderHasOneEntryPerLine() {
		assertFormatted("script s targets [ milvus , qdrant ] version \"1\"   param q : vector = [ 1 , -2.5 ] param k : int",
				"script s\ntargets [milvus, qdrant]\nversion \"1\"\nparam q: vector = [1, -2.5]\nparam k: int\n");
	}

	@Test
	public void commentsArePreserved() {
		assertFormatted("// header\nscript s\n// before\na: count in c // after\nb: count in c",
				"// header\nscript s\n\n// before\na: count in c // after\n\nb: count in c\n");
	}

	@Test
	public void commentInsideAStatementLaysItOutOverSeveralLines() {
		assertFormatted("script s\na: count in c /* why */ where id gt 1",
				"script s\n\na: count in c /* why */\n    where id gt 1\n");
	}

	@Test
	public void multiLineCommentKeepsItsLines() {
		assertFormatted("script s\n/* first\n   second\n*/\na: count in c",
				"script s\n\n/* first\n   second\n*/\na: count in c\n");
	}

	@Test
	public void punctuationIsSpacedConsistently() {
		assertFormatted("script s\ne: get from c ids [ 1 , 2 ] output [ id , v ]\nf: count in c where any v ( id eq $k ) and v . id eq env ( \"K\" )",
				"script s\n\ne: get from c ids [1, 2] output [id, v]\n\nf: count in c where any v (id eq $k) and v.id eq env(\"K\")\n");
	}

	@Test
	public void documentWithSyntaxErrorsIsLeftAsItIs() {
		String broken = "script s\nq: vector-search in c top 3   query vector [1 output [id]\nr:   count in c";
		formatter.assertFormatted(request -> configure(request).setAllowSyntaxErrors(true).setToBeFormatted(broken)
				.setExpectation(broken));
	}

	@TestFactory
	public Stream<DynamicTest> formattingIsIdempotent() throws IOException {
		return samples().map(sample -> dynamicTest(sample.getFileName().toString(), () -> {
			String once = format(read(sample));
			assertEquals(once, format(once));
		}));
	}

	@TestFactory
	public Stream<DynamicTest> formattedProgramParsesToTheSameModel() throws IOException {
		return samples().map(sample -> dynamicTest(sample.getFileName().toString(), () -> {
			String text = read(sample);
			Script original = parseHelper.parse(text);
			Script formatted = parseHelper.parse(format(text));
			assertTrue(formatted.eResource().getErrors().isEmpty(), () -> formatted.eResource().getErrors().toString());
			EcoreUtil.resolveAll(original);
			EcoreUtil.resolveAll(formatted);
			assertTrue(EcoreUtil.equals(original, formatted));
		}));
	}

	@TestFactory
	public Stream<DynamicTest> formattingChangesWhitespaceOnly() throws IOException {
		return samples().map(sample -> dynamicTest(sample.getFileName().toString(), () -> {
			String text = read(sample);
			String formatted = format(text);
			assertEquals(tokens(text), tokens(formatted));
			assertEquals(text.replaceAll("\\s", ""), formatted.replaceAll("\\s", ""));
		}));
	}

	private Stream<Path> samples() throws IOException {
		List<Path> files;
		try (Stream<Path> listing = Files.list(SAMPLES)) {
			files = listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
		}
		assertFalse(files.isEmpty(), () -> "no samples in " + SAMPLES.toAbsolutePath());
		return files.stream();
	}

	private static String read(Path sample) throws IOException {
		return Files.readString(sample, StandardCharsets.UTF_8);
	}

	private String format(String text) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		assertTrue(resource.getErrors().isEmpty(), () -> resource.getErrors().toString());
		ITextRegionAccess access = regionAccessBuilder.forNodeModel(resource).create();
		FormatterRequest request = requestProvider.get();
		request.setTextRegionAccess(access);
		request.setExceptionHandler(ExceptionAcceptor.THROWING);
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
		preferences.put(FormatterPreferenceKeys.indentation, "    ");
		preferences.put(FormatterPreferenceKeys.lineSeparator, "\n");
		request.setPreferences(preferences);
		return access.getRewriter().renderToString(formatterProvider.get().format(request));
	}

	private List<String> tokens(String text) throws Exception {
		List<String> result = new ArrayList<>();
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		for (ILeafNode leaf : resource.getParseResult().getRootNode().getLeafNodes()) {
			if (!leaf.isHidden()) {
				result.add(leaf.getText());
			}
		}
		return result;
	}

	private static String expandedCollection() {
		return "create collection c {\n    primary id\n    field id type int\n    vector-field v type float_vector dim 2\n}";
	}
}
