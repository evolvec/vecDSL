package org.ftn.vecDSL.lsp;

import java.util.Comparator;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

/** Applies a client's text edits: LSP positions (line, character) to offsets of the text, edits from the end. */
final class Edits {

	private Edits() {
	}

	static int offset(String text, Position position) {
		int offset = 0;
		for (int line = 0; line < position.getLine(); line++) {
			int newline = text.indexOf('\n', offset);
			offset = newline < 0 ? text.length() : newline + 1;
		}
		return offset + position.getCharacter();
	}

	/** The text after the edits, which must not overlap. */
	static String apply(String text, List<? extends TextEdit> edits) {
		StringBuilder result = new StringBuilder(text);
		edits.stream().sorted(Comparator.comparing((TextEdit edit) -> offset(text, edit.getRange().getStart())).reversed())
				.forEach(edit -> result.replace(offset(text, edit.getRange().getStart()),
						offset(text, edit.getRange().getEnd()), edit.getNewText()));
		return result.toString();
	}
}
