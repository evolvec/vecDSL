package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.ftn.vecDSL.editor.SourceText.Token;

/**
 * Puts the elements of a fixed-order block ('params { }', a hybrid leg) into grammar order, the fix for a parser
 * error such as "mismatched input 'timeout' expecting '}'". The block is found around the error offset, split at its
 * clause keywords and sorted; the edit is offered only when it removes syntax errors.
 */
public final class ClauseReorder {

	private ClauseReorder() {
	}

	/**
	 * @param orders clause keywords in grammar order, keyed by the word that opens the block
	 */
	public static Optional<TextEdit> reorder(String text, int offset, Map<String, List<String>> orders,
			ToIntFunction<String> syntaxErrors) {
		List<Token> tokens = SourceText.tokens(text);
		int open = enclosingOpener(tokens, offset);
		if (open < 1) {
			return Optional.empty();
		}
		List<String> order = orders.get(tokens.get(open - 1).text());
		int close = matchingCloser(tokens, open);
		if (order == null || close < 0 || close == open + 1) {
			return Optional.empty();
		}
		List<int[]> elements = new ArrayList<>();
		int depth = 0;
		for (int i = open + 1; i < close; i++) {
			Token token = tokens.get(i);
			if (depth == 0 && order.contains(token.text())) {
				elements.add(new int[] { i, order.indexOf(token.text()) });
			} else if (i == open + 1) {
				return Optional.empty();
			}
			depth += token.text().equals("{") || token.text().equals("[") ? 1
					: token.text().equals("}") || token.text().equals("]") ? -1 : 0;
		}
		List<String> parts = new ArrayList<>();
		for (int k = 0; k < elements.size(); k++) {
			int start = tokens.get(elements.get(k)[0]).offset();
			int end = k + 1 < elements.size() ? tokens.get(elements.get(k + 1)[0]).offset() : tokens.get(close).offset();
			parts.add(text.substring(start, end).strip());
		}
		List<Integer> sorted = new ArrayList<>();
		for (int k = 0; k < elements.size(); k++) {
			sorted.add(k);
		}
		sorted.sort(Comparator.comparingInt(k -> elements.get(k)[1]));
		if (sorted.equals(sorted.stream().sorted().toList())) {
			return Optional.empty();
		}
		int first = tokens.get(open + 1).offset();
		int last = tokens.get(close).offset();
		String content = text.substring(tokens.get(open).end(), last);
		String separator = content.contains("\n") ? "\n" + indentation(text, first) : " ";
		String body = String.join(separator, sorted.stream().map(parts::get).toList());
		int end = first + text.substring(first, last).stripTrailing().length();
		TextEdit edit = new TextEdit(first, end - first, body);
		return syntaxErrors.applyAsInt(TextEdit.apply(text, List.of(edit))) < syntaxErrors.applyAsInt(text)
				? Optional.of(edit) : Optional.empty();
	}

	private static int enclosingOpener(List<Token> tokens, int offset) {
		int depth = 0;
		for (int i = tokens.size() - 1; i >= 0; i--) {
			Token token = tokens.get(i);
			if (token.offset() >= offset) {
				continue;
			}
			if (token.text().equals("}")) {
				depth++;
			} else if (token.text().equals("{")) {
				if (depth == 0) {
					return i;
				}
				depth--;
			}
		}
		return -1;
	}

	private static int matchingCloser(List<Token> tokens, int open) {
		int depth = 0;
		for (int i = open + 1; i < tokens.size(); i++) {
			if (tokens.get(i).text().equals("{")) {
				depth++;
			} else if (tokens.get(i).text().equals("}")) {
				if (depth == 0) {
					return i;
				}
				depth--;
			}
		}
		return -1;
	}

	private static String indentation(String text, int offset) {
		int lineStart = text.lastIndexOf('\n', offset - 1) + 1;
		int end = lineStart;
		while (end < offset && (text.charAt(end) == ' ' || text.charAt(end) == '\t')) {
			end++;
		}
		return text.substring(lineStart, end);
	}
}
