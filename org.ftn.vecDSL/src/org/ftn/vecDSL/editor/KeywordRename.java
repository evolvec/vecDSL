package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.ftn.vecDSL.editor.SourceText.Token;

/**
 * Renames a name that is a grammar keyword (a label 'batch:', a field 'geo'), which the parser rejects. The new name
 * is the keyword with a suffix taken from the context. Other occurrences of the word are renamed only where the
 * rename removes syntax errors, so keyword uses of the same word stay as they are.
 */
public final class KeywordRename {

	/** The keyword, its replacement and the edits that rename the declaration and its references. */
	public record Proposal(String keyword, String name, List<TextEdit> edits) {
	}

	private KeywordRename() {
	}

	/**
	 * @param text the document
	 * @param offset the offset of the syntax error
	 * @param keywords the keywords of the grammar
	 * @param syntaxErrors the number of syntax errors of a document text
	 */
	public static Optional<Proposal> propose(String text, int offset, Set<String> keywords,
			ToIntFunction<String> syntaxErrors) {
		List<Token> tokens = SourceText.tokens(text);
		int index = indexAt(tokens, offset);
		if (index < 0 || !keywords.contains(tokens.get(index).text())
				|| !tokens.get(index).text().matches("[a-z][A-Za-z0-9_]*")) {
			return Optional.empty();
		}
		String keyword = tokens.get(index).text();
		String name = name(tokens, index, keywords);
		int baseline = syntaxErrors.applyAsInt(text);
		List<TextEdit> edits = new ArrayList<>(List.of(rename(tokens.get(index), name)));
		int best = syntaxErrors.applyAsInt(TextEdit.apply(text, edits));
		for (int i = 0; i < tokens.size(); i++) {
			if (i == index || !tokens.get(i).text().equals(keyword)) {
				continue;
			}
			List<TextEdit> candidate = new ArrayList<>(edits);
			candidate.add(rename(tokens.get(i), name));
			int errors = syntaxErrors.applyAsInt(TextEdit.apply(text, candidate));
			if (errors < best) {
				edits = candidate;
				best = errors;
			}
		}
		return best < baseline ? Optional.of(new Proposal(keyword, name, List.copyOf(edits))) : Optional.empty();
	}

	private static int indexAt(List<Token> tokens, int offset) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).offset() <= offset && offset < tokens.get(i).end() && tokens.get(i).isWord()) {
				return i;
			}
		}
		return -1;
	}

	private static TextEdit rename(Token token, String name) {
		return new TextEdit(token.offset(), token.text().length(), name);
	}

	/** keyword + "Step" for a label, keyword + the declaring keyword for a declared name, keyword + "Name" otherwise. */
	private static String name(List<Token> tokens, int index, Set<String> keywords) {
		String keyword = tokens.get(index).text();
		String suffix = "Name";
		if (index + 1 < tokens.size() && tokens.get(index + 1).text().equals(":")
				&& (index == 0 || !tokens.get(index - 1).text().equals(","))) {
			suffix = index > 0 && tokens.get(index - 1).text().equals("param") ? "Param" : "Step";
		} else if (index > 0 && tokens.get(index - 1).isWord() && keywords.contains(tokens.get(index - 1).text())) {
			String declaring = tokens.get(index - 1).text();
			String last = declaring.substring(declaring.lastIndexOf('-') + 1);
			suffix = Character.toUpperCase(last.charAt(0)) + last.substring(1);
		}
		Set<String> taken = new HashSet<>(keywords);
		tokens.stream().filter(Token::isWord).map(Token::text).forEach(taken::add);
		String name = keyword + suffix;
		for (int n = 2; taken.contains(name); n++) {
			name = keyword + suffix + n;
		}
		return name;
	}
}
