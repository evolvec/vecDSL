package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.util.CancelIndicator;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.editor.StatementCode;
import org.ftn.vecDSL.generator.VecDSLGenerator;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * StatementCode without the workbench: the code vecdsl gen writes for one statement, the cases of vecdsl-lsp's
 * LspClientTest (pinecone_demo similar and createProducts, an unlabeled statement), a label whose generated name
 * collides with another's, and every statement of pinecone_demo for pinecone in the three host languages.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class StatementCodeTest {

	private static final Path PINECONE_DEMO = Path.of("samples", "pinecone_demo.vecDSL");
	private static final String PINECONE_PROGRAM = "pinecone/python/product_catalog.py";
	private static final String COLLECTION = "script s\ncreate collection c { primary id field id type int field place "
			+ "type geo_point vector-field v type float_vector dim 2 metric cosine }\n";
	private static final List<String> LEADERS = List.of("@", "/*", "*", "//", "#");

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	private Script parse(String text) throws Exception {
		Script script = parseHelper.parse(text);
		validation.assertNoErrors(script);
		return script;
	}

	private static Method labeled(Script script, String label) {
		return script.getMethods().stream().filter(method -> label.equals(method.getName())).findFirst().orElseThrow();
	}

	private static StatementCode.Code code(Method statement, String target, String language) {
		return StatementCode.generate((XtextResource) statement.eResource(), statement, StatementCode.targets(target),
				StatementCode.languages(language), CancelIndicator.NullImpl);
	}

	private static List<String> lines(StatementCode.Code code, StatementCode.Section section) {
		return code.files().get(section.path()).lines().toList();
	}

	private static String header(StatementCode.Code code, StatementCode.Section section) {
		return lines(code, section).subList(section.start(), section.end()).stream()
				.filter(line -> LEADERS.stream().noneMatch(line.strip()::startsWith)).findFirst().orElse("");
	}

	private static String sample() throws Exception {
		return Files.readString(PINECONE_DEMO, StandardCharsets.UTF_8);
	}

	@Test
	public void locatesTheStatementInTheFilesGenWrites() throws Exception {
		StatementCode.Code code = code(labeled(parse(sample()), "similar"), "pinecone", "py");

		List<StatementCode.Section> sections = code.sections();
		assertEquals(2, sections.size(), sections.toString());
		assertTrue(sections.stream().allMatch(section -> section.path().equals(PINECONE_PROGRAM)), sections.toString());
		List<String> lines = lines(code, sections.get(0));
		List<String> member = lines.subList(sections.get(0).start(), sections.get(0).end());
		assertEquals("    def similar(self, query_vector, max_price):", member.get(0));
		assertEquals("        )", member.get(member.size() - 1));
		assertEquals("", lines.get(sections.get(0).end()));
		assertEquals(1, sections.get(1).end() - sections.get(1).start());
		assertTrue(lines.get(sections.get(1).start()).contains("program.similar("), sections.toString());
	}

	@Test
	public void restoresTheSelectionProperties() throws Exception {
		Method statement = labeled(parse(sample()), "similar");
		System.setProperty(VecDSLGenerator.TARGETS_PROPERTY, "milvus");
		try {
			code(statement, "pinecone", "ts");
			assertEquals("milvus", System.getProperty(VecDSLGenerator.TARGETS_PROPERTY));
			assertNull(System.getProperty(StatementCode.LANGUAGES_PROPERTY));
		} finally {
			System.clearProperty(VecDSLGenerator.TARGETS_PROPERTY);
		}
	}

	@Test
	public void unsupportedConstructsOfAStatementAreTheWarningsOfThatStatement() throws Exception {
		Script script = parse(sample());
		Method statement = labeled(script, "createProducts");
		CoverageIndex index = CoverageIndex.fromClasspath();

		List<CoverageCheck.Finding> findings = CoverageCheck.findings(statement, List.of("pinecone"), index);
		assertEquals(4, findings.size(), findings.toString());
		assertEquals(CoverageCheck.findings(script, index).stream().map(CoverageCheck.Finding::message).toList(),
				findings.stream().map(CoverageCheck.Finding::message).toList());
		assertEquals(List.of(15, 21, 24, 26), findings.stream()
				.map(finding -> NodeModelUtils.getLineAndColumn(finding.node(), finding.node().getOffset()).getLine())
				.toList());
		assertFalse(code(statement, "pinecone", "py").sections().isEmpty());
	}

	@Test
	public void locatesAnUnlabeledStatement() throws Exception {
		Script script = parse(COLLECTION + "count in c\n");
		Method statement = script.getMethods().get(1);
		StatementCode.Code code = code(statement, "milvus", "java");

		assertNull(statement.getName());
		String header = header(code, code.sections().get(0));
		assertTrue(header.contains("count") && header.endsWith("{"), header);
	}

	@Test
	public void separatesStatementsWhoseGeneratedNamesCollide() throws Exception {
		Script script = parse(COLLECTION + "byId: count in c\nby_id: count in c\n");
		StatementCode.Code first = code(labeled(script, "byId"), "milvus", "py");
		StatementCode.Code second = code(labeled(script, "by_id"), "milvus", "py");

		String firstHeader = header(first, first.sections().get(0));
		String secondHeader = header(second, second.sections().get(0));
		assertTrue(firstHeader.strip().startsWith("def by_id(") && firstHeader.endsWith(":"), firstHeader);
		assertTrue(secondHeader.strip().startsWith("def by_id_") && secondHeader.endsWith(":"), secondHeader);
		assertNotEquals(first.sections(), second.sections());
	}

	@TestFactory
	public Stream<DynamicTest> locatesEveryStatementOfTheDemoInEveryLanguage() throws Exception {
		Script script = parse(sample());
		List<DynamicTest> tests = new ArrayList<>();
		for (String language : List.of("py", "ts", "java")) {
			for (Method statement : script.getMethods()) {
				tests.add(DynamicTest.dynamicTest(statement.getName() + " " + language, () -> {
					StatementCode.Code code = code(statement, "pinecone", language);
					assertFalse(code.sections().isEmpty(), statement.getName());
					for (StatementCode.Section section : code.sections()) {
						assertTrue(0 <= section.start() && section.start() < section.end()
								&& section.end() <= lines(code, section).size(), section.toString());
					}
					StatementCode.Section first = code.sections().get(0);
					String header = header(code, first);
					List<String> lines = lines(code, first);
					if (language.equals("py")) {
						assertTrue(header.endsWith(":"), header);
					} else {
						assertTrue(header.endsWith("{"), header);
						assertTrue(lines.get(first.end() - 1).strip().startsWith("}"), lines.get(first.end() - 1));
					}
				}));
			}
		}
		return tests.stream();
	}
}
