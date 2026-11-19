package org.ftn.vecDSL.editor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.services.VecDSLGrammarAccess;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.RecordEntry;
import org.ftn.vecDSL.vecDSL.Script;

import com.google.inject.Inject;

/**
 * The quick fixes of vecDSL as labeled text edits, computed when they are offered: a name that is a keyword and
 * clauses of a block out of grammar order (both parser errors), a search metric other than the declared one, a
 * geo_point written as a string, and a construct a targeted vendor does not support. The Eclipse quick fix provider
 * and the language server's offer exactly these.
 */
public class QuickFixes {

	/** One fix: what the user picks and the edits it makes, which do not overlap. */
	public record Fix(String label, String description, List<TextEdit> edits) {
	}

	@Inject
	private VecDSLGrammarAccess grammar;

	@Inject
	private IParser parser;

	/** For a syntax error at the offset of the text. */
	public List<Fix> syntaxError(String text, int offset) {
		List<Fix> fixes = new ArrayList<>();
		Set<String> keywords = ClauseOrder.keywordWords(grammar.getGrammar());
		KeywordRename.propose(text, offset, keywords, this::syntaxErrors).ifPresent(proposal -> fixes.add(new Fix(
				"Rename '" + proposal.keyword() + "' to '" + proposal.name() + "'",
				"'" + proposal.keyword() + "' is a keyword of vecDSL. Renames it wherever it is used as a name.",
				proposal.edits())));
		ClauseReorder.reorder(text, offset, ClauseOrder.blocks(grammar.getGrammar()), this::syntaxErrors)
				.ifPresent(edit -> fixes.add(new Fix("Put the clauses of the block in grammar order",
						"Moves every clause of the block to the position the grammar expects.", List.of(edit))));
		return fixes;
	}

	/** For {@code t.metric.mismatch}. */
	public List<Fix> metricMismatch(Issue issue, XtextResource resource) {
		List<Fix> fixes = new ArrayList<>();
		EObject search = object(resource, issue);
		EMetric declared = search == null ? null : SearchMetrics.declared(search);
		if (declared != null) {
			fixes.add(new Fix("Use the declared metric '" + declared.getLiteral() + "'",
					"Replaces the search metric by the metric the vector field declares.",
					List.of(new TextEdit(issue.getOffset(), issue.getLength(), declared.getLiteral()))));
		}
		EStructuralFeature metric = search == null ? null : search.eClass().getEStructuralFeature("metric");
		TextEdit removal = metric == null ? null : ClauseRegion.removal(search, metric);
		if (removal != null) {
			fixes.add(new Fix("Remove the metric clause", "The search then uses the metric the field declares.",
					List.of(removal)));
		}
		return fixes;
	}

	/** For {@code t.record.type}: a geo_point given as a string. */
	public List<Fix> recordType(Issue issue, XtextResource resource) {
		List<TextEdit> edits = object(resource, issue) instanceof RecordEntry entry ? ParameterExtraction.edits(entry)
				: List.of();
		return edits.isEmpty() ? List.of()
				: List.of(new Fix("Move the value into a parameter",
						"Declares a text parameter with this value at the top of the script and refers to it here.",
						edits));
	}

	/** For {@code vecdsl.coverage.unsupported}, whose data is the vendor and the feature name (empty for a class). */
	public List<Fix> unsupported(Issue issue, XtextResource resource) {
		String[] data = issue.getData();
		if (data == null || data.length < 2) {
			return List.of();
		}
		List<Fix> fixes = new ArrayList<>();
		String vendor = data[0];
		TextEdit target = resource.getContents().isEmpty() || !(resource.getContents().get(0) instanceof Script script)
				? null : TargetsEdit.removeVendor(script, vendor);
		if (target != null) {
			fixes.add(new Fix("Remove '" + vendor + "' from targets", "The script no longer targets " + vendor + ".",
					List.of(target)));
		}
		if (!data[1].isEmpty()) {
			EObject object = object(resource, issue);
			EStructuralFeature feature = object == null ? null : object.eClass().getEStructuralFeature(data[1]);
			TextEdit clause = feature == null ? null : ClauseRegion.removal(object, feature);
			if (clause != null) {
				fixes.add(new Fix("Remove the '" + data[1] + "' clause",
						"Removes the clause " + vendor + " does not support.", List.of(clause)));
			}
		}
		return fixes;
	}

	private static EObject object(XtextResource resource, Issue issue) {
		return issue.getUriToProblem() == null ? null : resource.getEObject(issue.getUriToProblem().fragment());
	}

	private int syntaxErrors(String text) {
		int count = 0;
		for (Object ignored : parser.parse(new StringReader(text)).getSyntaxErrors()) {
			count++;
		}
		return count;
	}
}
