package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.CancelIndicator;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.HoverDocument;
import org.ftn.vecDSL.editor.StatementHover;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/** The Eclipse hover over a statement's label (StatementHover), without the workbench. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class StatementHoverTest {

	private static final CoverageIndex INDEX = CoverageIndex.fromClasspath();

	@Inject
	private ParseHelper<Script> parseHelper;

	private Script sample(String name) throws Exception {
		return parseHelper.parse(Files.readString(Path.of("samples", name), StandardCharsets.UTF_8));
	}

	private static Method labeled(Script script, String label) {
		return script.getMethods().stream().filter(method -> label.equals(method.getName())).findFirst().orElseThrow();
	}

	private static String html(Method statement) {
		return StatementHover.of(statement, INDEX, CancelIndicator.NullImpl).html();
	}

	private static long count(String text, String part) {
		return (text.length() - text.replace(part, "").length()) / part.length();
	}

	@Test
	public void theLabelRegionSelectsTheStatement() throws Exception {
		Script script = sample("pinecone_demo.vecDSL");
		XtextResource resource = (XtextResource) script.eResource();
		String text = resource.getParseResult().getRootNode().getText();
		int offset = text.indexOf("similar:");

		assertSame(labeled(script, "similar"), StatementHover.labeled(resource, offset, "similar".length()));
		assertNull(StatementHover.labeled(resource, offset, 3));
		assertNull(StatementHover.labeled(resource, text.indexOf("vector-search", offset), "vector-search".length()));
	}

	@Test
	public void showsTheMemberWithItsFileAndLines() throws Exception {
		String html = html(labeled(sample("pinecone_demo.vecDSL"), "similar"));

		assertTrue(html.startsWith("<p><b>similar</b>: the code vecdsl gen writes for <code>pinecone</code>, Python</p>"),
				html);
		assertTrue(html.contains("<p><code>pinecone/python/product_catalog.py</code>, lines "), html);
		assertTrue(html.contains("<pre>    def similar(self, query_vector, max_price):\n"), html);
		assertEquals(2, count(html, "<pre>"), html);
	}

	@Test
	public void listsTheUnsupportedConstructsFirst() throws Exception {
		String html = html(labeled(sample("pinecone_demo.vecDSL"), "createProducts"));

		int reasons = html.indexOf("<ul><li>unsupported on pinecone: Pinecone configures text analysis");
		assertTrue(reasons > 0, html);
		assertEquals(4, count(html.substring(reasons, html.indexOf("</ul>")), "<li>"), html);
		assertTrue(html.indexOf("<pre>") > reasons, html);
	}

	@Test
	public void keepsToTheLineLimit() throws Exception {
		HoverDocument document = StatementHover.of(labeled(sample("milvus_full.vecDSL"), "createBooks"), INDEX,
				CancelIndicator.NullImpl);

		long lines = document.blocks().stream().filter(HoverDocument.Lines.class::isInstance)
				.mapToLong(block -> ((HoverDocument.Lines) block).lines().stream().filter(line -> !line.equals("..."))
						.count())
				.sum();
		assertEquals(StatementHover.MAX_LINES, lines);
		assertTrue(document.html().contains("\n...</pre>") || document.html().endsWith("<p>...</p>"),
				document.html());
	}

	@Test
	public void followsTheLanguageHint() throws Exception {
		Script script = parseHelper.parse("script s targets [milvus] language java\ncreate collection c { primary id "
				+ "field id type int vector-field v type float_vector dim 2 metric cosine }\nhowMany: count in c\n");

		String html = html(labeled(script, "howMany"));
		assertTrue(html.contains("<code>milvus</code>, Java"), html);
		assertTrue(html.contains("<code>milvus/java/"), html);
	}

	@Test
	public void explainsAScriptWithoutTargets() throws Exception {
		Script script = parseHelper.parse("script s\ncreate collection c { primary id field id type int vector-field v "
				+ "type float_vector dim 2 metric cosine }\nhowMany: count in c\n");

		String html = html(labeled(script, "howMany"));
		assertTrue(html.contains("the script declares no targets"), html);
		assertFalse(html.contains("<pre>"), html);
	}

	@Test
	public void explainsAProgramWithErrors() throws Exception {
		Script script = parseHelper.parse("script s targets [milvus]\ncreate collection c { primary id field id type "
				+ "int vector-field v type float_vector dim 2 metric cosine }\nhowMany: count in nowhere\n");

		String html = html(labeled(script, "howMany"));
		assertTrue(html.contains("The program has 1 error, so vecdsl gen writes no code; line 3: "), html);
		assertFalse(html.contains("<pre>"), html);
	}
}
