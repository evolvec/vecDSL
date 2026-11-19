package org.ftn.vecDSL.ui.tests;

import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.testing.AbstractQuickfixTest;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.validation.VecDSLValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Quick fixes on an opened editor, one test per fix. Run as a JUnit Plug-in Test. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLUiInjectorProvider.class)
public class VecDSLQuickfixTest extends AbstractQuickfixTest {

	private static final String COLLECTION = "script s\ncreate collection c { primary id field id type int field place type geo_point "
			+ "vector-field v type float_vector dim 2 metric cosine }\n";

	@Test
	public void keywordLabelIsRenamed() {
		testQuickfixesOn(COLLECTION + "batch: count in c\n", Diagnostic.SYNTAX_DIAGNOSTIC,
				new Quickfix("Rename 'batch' to 'batchStep'",
						"'batch' is a keyword of vecDSL. Renames it wherever it is used as a name.",
						COLLECTION + "batchStep: count in c\n"));
	}

	@Test
	public void paramsAreReordered() {
		testQuickfixesOn(COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { timeout 5 candidates 20 } output *\n",
				Diagnostic.SYNTAX_DIAGNOSTIC,
				new Quickfix("Put the clauses of the block in grammar order",
						"Moves every clause of the block to the position the grammar expects.",
						COLLECTION + "q: vector-search in c top 2 query vector [1, 0] params { candidates 20 timeout 5 } output *\n"));
	}

	@Test
	public void metricMismatchOffersTheDeclaredMetricAndRemoval() {
		String search = "q: vector-search in c top 2 query vector [1, 0] using v metric dot output *\n";
		testQuickfixesOn(COLLECTION + search, VecDSLValidator.METRIC_MISMATCH,
				new Quickfix("Remove the metric clause", "The search then uses the metric the field declares.",
						COLLECTION + search.replace(" metric dot", "")),
				new Quickfix("Use the declared metric 'cosine'",
						"Replaces the search metric by the metric the vector field declares.",
						COLLECTION + search.replace("metric dot", "metric cosine")));
	}

	@Test
	public void geoPointStringBecomesAParameter() {
		testQuickfixesOn(COLLECTION + "seed: insert into c values [{ id: 1, place: \"45.25,19.85\", v: [1, 0] }]\n",
				VecDSLValidator.RECORD_TYPE,
				new Quickfix("Move the value into a parameter",
						"Declares a text parameter with this value at the top of the script and refers to it here.",
						"script s\nparam seedPlace: text = \"45.25,19.85\"\n" + COLLECTION.substring("script s\n".length())
								+ "seed: insert into c values [{ id: 1, place: $seedPlace, v: [1, 0] }]\n"));
	}

	@Test
	public void unsupportedClauseOffersVendorAndClauseRemoval() {
		String script = "script s targets [milvus, elasticsearch]\ncreate collection books { primary id field id type int "
				+ "field title type varchar full-text }\n";
		String search = "k: search in books top 3 query text \"x\" fuzziness \"AUTO\" output *\n";
		testQuickfixesOn(script + search, CoverageCheck.UNSUPPORTED,
				new Quickfix("Remove 'milvus' from targets", "The script no longer targets milvus.",
						script.replace("[milvus, elasticsearch]", "[elasticsearch]") + search),
				new Quickfix("Remove the 'fuzziness' clause", "Removes the clause milvus does not support.",
						script + search.replace(" fuzziness \"AUTO\"", "")));
	}
}
