package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * A light lexer over vecDSL text for edits that must work on documents the parser rejects: words, punctuation and
 * brace nesting outside string literals and comments.
 */
final class SourceText {

	/** A word or punctuation character and its offset. */
	record Token(String text, int offset) {

		int end() {
			return offset + text.length();
		}

		boolean isWord() {
			return Character.isLetter(text.charAt(0)) || text.charAt(0) == '_' || text.charAt(0) == '^';
		}
	}

	private SourceText() {
	}

	static List<Token> tokens(String text) {
		List<Token> tokens = new ArrayList<>();
		int i = 0;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
			} else if (text.startsWith("//", i)) {
				int end = text.indexOf('\n', i);
				i = end < 0 ? text.length() : end;
			} else if (text.startsWith("/*", i)) {
				int end = text.indexOf("*/", i + 2);
				i = end < 0 ? text.length() : end + 2;
			} else if (c == '"' || c == '\'') {
				int end = i + 1;
				while (end < text.length() && text.charAt(end) != c) {
					end += text.charAt(end) == '\\' ? 2 : 1;
				}
				end = Math.min(end + 1, text.length());
				tokens.add(new Token(text.substring(i, end), i));
				i = end;
			} else if (Character.isLetter(c) || c == '_' || c == '^') {
				int end = i + 1;
				while (end < text.length() && (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '_'
						|| text.charAt(end) == '-' && end + 1 < text.length() && Character.isLetter(text.charAt(end + 1)))) {
					end++;
				}
				tokens.add(new Token(text.substring(i, end), i));
				i = end;
			} else if (Character.isDigit(c)) {
				int end = i + 1;
				while (end < text.length() && (Character.isDigit(text.charAt(end)) || text.charAt(end) == '.')) {
					end++;
				}
				tokens.add(new Token(text.substring(i, end), i));
				i = end;
			} else {
				tokens.add(new Token(String.valueOf(c), i));
				i++;
			}
		}
		return tokens;
	}
}
