package org.ftn.vecDSL.ide.contentassist;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.contentassist.ContentAssistService;

/**
 * Sends a proposal's documentation as Markdown: Xtext's service passes it as a plain string, which clients show
 * with the Markdown markup visible.
 */
public class VecDSLContentAssistService extends ContentAssistService {

	@Override
	protected CompletionItem toCompletionItem(ContentAssistEntry entry, int caretOffset, Position caretPosition,
			Document document) {
		CompletionItem item = super.toCompletionItem(entry, caretOffset, caretPosition, document);
		if (entry.getDocumentation() != null) {
			item.setDocumentation(Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, entry.getDocumentation())));
		}
		return item;
	}
}
