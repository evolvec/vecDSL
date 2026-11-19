package org.ftn.vecDSL.ide.quickfix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.editor.quickfix.QuickFix;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.editor.QuickFixes;
import org.ftn.vecDSL.editor.TextEdit;
import org.ftn.vecDSL.validation.VecDSLValidator;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The fixes of {@link QuickFixes} as code actions for textDocument/codeAction, one {@link QuickFix} method per issue
 * code. A diagnostic carries neither the object at fault nor, after its JSON round trip, the issue data as strings, and
 * Xtext's declarative provider would give a fix method neither the diagnostic nor the document. So this provider finds
 * the issue the diagnostic was published from by validating the resource again (same code, message and start) and
 * hands it to {@link QuickFixes}, as the Eclipse provider hands its marker's issue.
 */
public class VecDSLIdeQuickfixProvider implements IQuickFixProvider {

	private static final Map<String, Method> FIXES = Arrays.stream(VecDSLIdeQuickfixProvider.class.getMethods())
			.filter(method -> method.isAnnotationPresent(QuickFix.class))
			.collect(Collectors.toMap(method -> method.getAnnotation(QuickFix.class).value(), method -> method));

	@Inject
	private QuickFixes fixes;

	@Inject
	private IResourceValidator validator;

	@Inject
	private Provider<DiagnosticResolutionAcceptor> acceptors;

	@QuickFix(org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC)
	public List<QuickFixes.Fix> fixSyntaxError(Issue issue, XtextResource resource, Document document) {
		return fixes.syntaxError(document.getContents(), issue.getOffset());
	}

	@QuickFix(VecDSLValidator.METRIC_MISMATCH)
	public List<QuickFixes.Fix> fixMetricMismatch(Issue issue, XtextResource resource, Document document) {
		return fixes.metricMismatch(issue, resource);
	}

	@QuickFix(VecDSLValidator.RECORD_TYPE)
	public List<QuickFixes.Fix> fixGeoPointString(Issue issue, XtextResource resource, Document document) {
		return fixes.recordType(issue, resource);
	}

	@QuickFix(CoverageCheck.UNSUPPORTED)
	public List<QuickFixes.Fix> fixUnsupported(Issue issue, XtextResource resource, Document document) {
		return fixes.unsupported(issue, resource);
	}

	@Override
	public boolean handlesDiagnostic(Diagnostic diagnostic) {
		return diagnostic.getCode() != null && FIXES.containsKey(diagnostic.getCode().getLeft());
	}

	@Override
	public List<DiagnosticResolution> getResolutions(Options options, Diagnostic diagnostic) {
		DiagnosticResolutionAcceptor acceptor = acceptors.get();
		if (handlesDiagnostic(diagnostic) && options.getResource() instanceof XtextResource resource) {
			Issue issue = issue(resource, diagnostic, options);
			if (issue != null) {
				for (QuickFixes.Fix fix : fixes(FIXES.get(issue.getCode()), issue, resource, options.getDocument())) {
					acceptor.accept(fix.label(), (problem, object, document) -> edits(document, fix.edits()));
				}
			}
		}
		return acceptor.getDiagnosticResolutions(options, diagnostic);
	}

	private Issue issue(XtextResource resource, Diagnostic diagnostic, Options options) {
		Position start = diagnostic.getRange().getStart();
		for (Issue issue : validator.validate(resource, CheckMode.ALL, options.getCancelIndicator())) {
			if (diagnostic.getCode().getLeft().equals(issue.getCode()) && issue.getMessage().equals(diagnostic.getMessage().getLeft())
					&& issue.getLineNumber() - 1 == start.getLine() && issue.getColumn() - 1 == start.getCharacter()) {
				return issue;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<QuickFixes.Fix> fixes(Method fix, Issue issue, XtextResource resource, Document document) {
		try {
			return (List<QuickFixes.Fix>) fix.invoke(this, issue, resource, document);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw e.getCause() instanceof RuntimeException runtime ? runtime : new IllegalStateException(e.getCause());
		}
	}

	private static List<org.eclipse.lsp4j.TextEdit> edits(Document document, List<TextEdit> edits) {
		return edits.stream().map(edit -> new org.eclipse.lsp4j.TextEdit(new Range(document.getPosition(edit.offset()),
				document.getPosition(edit.offset() + edit.length())), edit.text())).toList();
	}
}
