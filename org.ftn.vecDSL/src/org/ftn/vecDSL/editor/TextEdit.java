package org.ftn.vecDSL.editor;

import java.util.Comparator;
import java.util.List;

/** A replacement of {@code length} characters at {@code offset} by {@code text}. */
public record TextEdit(int offset, int length, String text) {

	/** The document after the edits, which must not overlap. */
	public static String apply(String document, List<TextEdit> edits) {
		StringBuilder result = new StringBuilder(document);
		edits.stream().sorted(Comparator.comparingInt(TextEdit::offset).reversed())
				.forEach(edit -> result.replace(edit.offset(), edit.offset() + edit.length(), edit.text()));
		return result.toString();
	}
}
