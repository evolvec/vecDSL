package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.validation.VecDSLValidator;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * textDocument/codeAction through the language server's ICodeActionService2, one test per fix, on the programs and with
 * the titles and resulting texts of VecDSLQuickfixTest (org.ftn.vecDSL.ui.tests). The diagnostics are those the server
 * publishes for the program; each code action's workspace edit is applied to the text.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLIdeInjectorProvider.class)
public class IdeQuickfixTest {

	static final String COLLECTION = "script s\ncreate collection c { primary id field id type int field place type geo_point "
			+ "vector-field v type float_vector dim 2 metric cosine }\n";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private IResourceValidator validator;

	@Inject
	private ICodeActionService2 codeActions;

	/** Title to resulting text of every code action for the first diagnostic with the code, in the server's order. */
	private Map<String, String> fixes(String text, String code) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		Diagnostic diagnostic = IdeProtocol.diagnostics(validator.validate(resource, CheckMode.ALL, () -> false)).stream()
				.filter(candidate -> code.equals(candidate.getCode().getLeft())).findFirst().orElseThrow();
		return IdeProtocol.codeActions(codeActions, resource, text, diagnostic);
	}

	@Test
	public void keywordLabelIsRenamed() throws Exception {
		assertEquals(Map.of("Rename 'batch' to 'batchStep'", COLLECTION + "batchStep: count in c\n"),
				fixes(COLLECTION + "batch: count in c\n", org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC));
	}

	@Test
	public void paramsAreReordered() throws Exception {
		assertEquals(Map.of("Put the clauses of the block in grammar order",
				COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { candidates 20 timeout 5 } output *\n"),
				fixes(COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { timeout 5 candidates 20 } output *\n",
						org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC));
	}

	@Test
	public void metricMismatchOffersTheDeclaredMetricAndRemoval() throws Exception {
		String search = "q: vector-search in c top 2 query vector [1, 0] using v metric dot output *\n";
		assertEquals(List.of(Map.entry("Remove the metric clause", COLLECTION + search.replace(" metric dot", "")),
				Map.entry("Use the declared metric 'cosine'", COLLECTION + search.replace("metric dot", "metric cosine"))),
				List.copyOf(fixes(COLLECTION + search, VecDSLValidator.METRIC_MISMATCH).entrySet()));
	}

	@Test
	public void geoPointStringBecomesAParameter() throws Exception {
		assertEquals(Map.of("Move the value into a parameter",
				"script s\nparam seedPlace: text = \"45.25,19.85\"\n" + COLLECTION.substring("script s\n".length())
						+ "seed: insert into c values [{ id: 1, place: $seedPlace, v: [1, 0] }]\n"),
				fixes(COLLECTION + "seed: insert into c values [{ id: 1, place: \"45.25,19.85\", v: [1, 0] }]\n",
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
				List.copyOf(fixes(script + search, CoverageCheck.UNSUPPORTED).entrySet()));
	}
}
