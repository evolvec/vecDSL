package org.ftn.vecDSL.ui.tests;

import org.eclipse.jface.text.Region;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.testing.AbstractHoverTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Hovers on an opened editor. Run as a JUnit Plug-in Test. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLUiInjectorProvider.class)
public class VecDSLHoverTest extends AbstractHoverTest {

	private static final String SCRIPT = "script s targets [milvus, redis]\nparam q: vector = [1, 0]\n"
			+ "create collection books { primary id field id type int field title type varchar full-text "
			+ "vector-field embedding type float_vector dim 2 metric cosine }\n"
			+ "k: search in books top 3 query text \"x\" fuzziness \"AUTO\" output *\n"
			+ "n: vector-search in books top 2 query vector $q using embedding output *\n"
			+ "create connection c uri \"x\" api-key env(\"VECDSL_KEY\")\n";

	@Test
	public void keywordShowsConceptAndCoverage() {
		hasHoverOver(SCRIPT, "fuzziness", "Milvus BM25 has no fuzzy matching");
		hasHoverOver(SCRIPT, "fuzziness", "<td>redis</td><td>native</td>");
		hasHoverOver(SCRIPT, "vector-search", "Nearest-neighbour search over one vector field.");
	}

	@Test
	public void fieldShowsTypeDimensionMetricAndCollection() {
		hasHoverOver(SCRIPT, "embedding", "vector field of type float_vector, dim 2, metric cosine");
	}

	@Test
	public void collectionShowsItsFields() {
		hasHoverOver(SCRIPT, "books", "<li><code>field title type varchar full-text</code></li>");
	}

	@Test
	public void parameterAndEnvironmentReferenceShowTheirDeclaration() {
		hasHoverOver(SCRIPT, new Region(SCRIPT.indexOf("$q") + 1, 1), "param q: vector = [1, 0]");
		hasHoverOver(SCRIPT, "VECDSL_KEY", "<code>VECDSL_KEY</code>");
	}
}
