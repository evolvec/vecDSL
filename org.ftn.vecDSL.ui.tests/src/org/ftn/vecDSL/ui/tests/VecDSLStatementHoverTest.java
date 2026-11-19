package org.ftn.vecDSL.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.testing.AbstractHoverTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** The generated code of a statement in the hover over its label, on an opened editor. Run as a JUnit Plug-in Test. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLUiInjectorProvider.class)
public class VecDSLStatementHoverTest extends AbstractHoverTest {

	private static final String SCRIPT = "script s targets [milvus, qdrant]\n"
			+ "create collection books { primary id field id type int vector-field embedding type float_vector dim 2 "
			+ "metric cosine }\nnearestBooks: vector-search in books top 2 query vector [1, 0] using embedding output *\n";

	@Test
	public void labelShowsTheGeneratedCodeOfTheFirstTarget() {
		hasHoverOver(SCRIPT, "nearestBooks",
				"<b>nearestBooks</b>: the code vecdsl gen writes for <code>milvus</code>, Python (also targeted: qdrant)");
		hasHoverOver(SCRIPT, "nearestBooks", "<pre>    def nearest_books(self):");
	}

	@Test
	public void keywordKeepsItsConstructHover() {
		hasHoverOver(SCRIPT, "vector-search", "Nearest-neighbour search over one vector field.");
	}
}
