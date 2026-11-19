package org.ftn.vecDSL.ui.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest;
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Vendor-aware content assist. Run as a JUnit Plug-in Test; the coverage index comes from the ui bundle. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLUiInjectorProvider.class)
public class VecDSLContentAssistTest extends AbstractContentAssistTest {

	private static final String BOOKS = "create collection books { primary id field id type int field title type varchar "
			+ "full-text vector-field embedding type float_vector dim 2 metric cosine }\n"
			+ "create collection notes { primary key field key type int vector-field a type float_vector dim 2 }\n";

	@Test
	public void unsupportedClauseIsMarkedAndRankedLast() throws Exception {
		List<String> proposals = proposals("script s targets [milvus, elasticsearch]\n" + BOOKS
				+ "k: search in books top 3 query text \"x\" ");
		String fuzziness = proposals.stream().filter(display -> display.startsWith("fuzziness")).findFirst().orElseThrow();
		assertTrue(fuzziness.contains("unsupported on milvus: Milvus BM25 has no fuzzy matching"), fuzziness);
		int firstUnsupported = proposals.indexOf(proposals.stream().filter(display -> display.contains("unsupported on"))
				.findFirst().orElseThrow());
		assertTrue(proposals.subList(firstUnsupported, proposals.size()).stream()
				.allMatch(display -> display.contains("unsupported on")), proposals.toString());
	}

	@Test
	public void proposalsShowTheStatusOfEveryTarget() throws Exception {
		List<String> proposals = proposals("script s targets [elasticsearch, redis]\n" + BOOKS
				+ "k: search in books top 3 query text \"x\" ");
		assertTrue(proposals.contains("fuzziness  elasticsearch: native; redis: native"), proposals.toString());
	}

	@Test
	public void nestedCreateProposesTheFieldsOfItsCollectionFirst() throws Exception {
		List<String> proposals = proposals("script s\n" + BOOKS + "more: create books with { create vector-index extra on ");
		assertTrue(proposals.get(0).startsWith("embedding"), proposals.toString());
	}

	/**
	 * Display strings in the order the content assist processor returns them (priority, then name). Not
	 * ContentAssistProcessorTestBuilder.toDisplayString, which sorts them alphabetically and so hides the ranking.
	 */
	private List<String> proposals(String model) throws Exception {
		ContentAssistProcessorTestBuilder builder = newBuilder().append(model);
		return Arrays.stream(builder.computeCompletionProposals()).map(ICompletionProposal::getDisplayString).toList();
	}

	@Test
	public void searchProposesOnlyTheDeclaredMetric() throws Exception {
		newBuilder().append("script s\n" + BOOKS + "q: vector-search in books top 2 query vector [1, 0] using embedding metric ")
				.assertText("cosine");
	}
}
