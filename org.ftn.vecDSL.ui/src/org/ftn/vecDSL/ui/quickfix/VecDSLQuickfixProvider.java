package org.ftn.vecDSL.ui.quickfix;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.editor.QuickFixes;
import org.ftn.vecDSL.editor.TextEdit;
import org.ftn.vecDSL.validation.VecDSLValidator;

import com.google.inject.Inject;

/** The fixes of {@link QuickFixes} as Eclipse quick fixes (Ctrl+1) applied to the editor's document. */
public class VecDSLQuickfixProvider extends DefaultQuickfixProvider {

	@Inject
	private QuickFixes fixes;

	@Fix(Diagnostic.SYNTAX_DIAGNOSTIC)
	public void fixSyntaxError(Issue issue, IssueResolutionAcceptor acceptor) {
		IXtextDocument document = document(issue);
		if (document != null && issue.getOffset() != null) {
			accept(issue, acceptor, fixes.syntaxError(document.get(), issue.getOffset()));
		}
	}

	@Fix(VecDSLValidator.METRIC_MISMATCH)
	public void fixMetricMismatch(Issue issue, IssueResolutionAcceptor acceptor) {
		IXtextDocument document = document(issue);
		if (document != null) {
			accept(issue, acceptor, document.readOnly(resource -> fixes.metricMismatch(issue, resource)));
		}
	}

	@Fix(VecDSLValidator.RECORD_TYPE)
	public void fixGeoPointString(Issue issue, IssueResolutionAcceptor acceptor) {
		IXtextDocument document = document(issue);
		if (document != null) {
			accept(issue, acceptor, document.readOnly(resource -> fixes.recordType(issue, resource)));
		}
	}

	@Fix(CoverageCheck.UNSUPPORTED)
	public void fixUnsupported(Issue issue, IssueResolutionAcceptor acceptor) {
		IXtextDocument document = document(issue);
		if (document != null) {
			accept(issue, acceptor, document.readOnly(resource -> fixes.unsupported(issue, resource)));
		}
	}

	private IXtextDocument document(Issue issue) {
		return getModificationContextFactory().createModificationContext(issue).getXtextDocument();
	}

	private static void accept(Issue issue, IssueResolutionAcceptor acceptor, List<QuickFixes.Fix> fixes) {
		for (QuickFixes.Fix fix : fixes) {
			acceptor.accept(issue, fix.label(), fix.description(), null,
					context -> apply(context.getXtextDocument(), fix.edits()));
		}
	}

	private static void apply(IXtextDocument document, List<TextEdit> edits) throws BadLocationException {
		for (TextEdit edit : edits.stream().sorted(Comparator.comparingInt(TextEdit::offset).reversed()).toList()) {
			document.replace(edit.offset(), edit.length(), edit.text());
		}
	}
}
