package org.ftn.vecDSL.tests;

import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Issue;

/** What the language server does around the .ide services, for tests that call those services directly. */
final class IdeProtocol {

	private IdeProtocol() {
	}

	/** An issue as LanguageServerImpl.toDiagnostic publishes it (Xtext 2.42.0): 0-based line and character. */
	static Diagnostic diagnostic(Issue issue) {
		Diagnostic result = new Diagnostic();
		result.setCode(issue.getCode());
		result.setData(issue.getData());
		result.setMessage(issue.getMessage());
		result.setSeverity(switch (issue.getSeverity()) {
			case ERROR -> DiagnosticSeverity.Error;
			case WARNING -> DiagnosticSeverity.Warning;
			case INFO -> DiagnosticSeverity.Information;
			case IGNORE -> DiagnosticSeverity.Hint;
		});
		result.setRange(new Range(new Position(Math.max(0, issue.getLineNumber() - 1), Math.max(0, issue.getColumn() - 1)),
				new Position(Math.max(0, issue.getLineNumberEnd() - 1), Math.max(0, issue.getColumnEnd() - 1))));
		return result;
	}

	/** The issues the server publishes: all but those of severity IGNORE. */
	static List<Diagnostic> diagnostics(List<Issue> issues) {
		return issues.stream().filter(issue -> issue.getSeverity() != Severity.IGNORE).map(IdeProtocol::diagnostic)
				.toList();
	}

	/** The server's access to one open document: reads run on it, the live scope is its resource set. */
	@SuppressWarnings("unchecked")
	static ILanguageServerAccess access(XtextResource resource, Document document) {
		return (ILanguageServerAccess) Proxy.newProxyInstance(IdeProtocol.class.getClassLoader(),
				new Class<?>[] { ILanguageServerAccess.class }, (proxy, method, arguments) -> switch (method.getName()) {
					case "doSyncRead" -> ((Function<ILanguageServerAccess.Context, ?>) arguments[1])
							.apply(new ILanguageServerAccess.Context(resource, document, true, CancelIndicator.NullImpl));
					case "newLiveScopeResourceSet" -> resource.getResourceSet();
					case "getInitializeParams", "getInitializeResult", "getLanguageClient" -> null;
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	/** Title to resulting text of every code action the service offers for the diagnostic, in the server's order. */
	static Map<String, String> codeActions(ICodeActionService2 service, XtextResource resource, String text,
			Diagnostic diagnostic) {
		String uri = resource.getURI().toString();
		ICodeActionService2.Options options = new ICodeActionService2.Options();
		options.setURI(uri);
		options.setLanguageServerAccess(access(resource, new Document(1, text)));
		options.setCancelIndicator(() -> false);
		options.setCodeActionParams(new CodeActionParams(new TextDocumentIdentifier(uri), diagnostic.getRange(),
				new CodeActionContext(List.of(diagnostic))));
		Map<String, String> result = new LinkedHashMap<>();
		for (Either<Command, CodeAction> action : service.getCodeActions(options)) {
			result.put(action.getRight().getTitle(), apply(text, action.getRight().getEdit().getChanges().get(uri)));
		}
		return result;
	}

	/** The text after the edits, which must not overlap. */
	static String apply(String text, List<? extends TextEdit> edits) {
		Document document = new Document(1, text);
		StringBuilder result = new StringBuilder(text);
		edits.stream().sorted(Comparator.comparing((TextEdit edit) -> document.getOffSet(edit.getRange().getStart()))
				.reversed()).forEach(edit -> result.replace(document.getOffSet(edit.getRange().getStart()),
						document.getOffSet(edit.getRange().getEnd()), edit.getNewText()));
		return result.toString();
	}
}
