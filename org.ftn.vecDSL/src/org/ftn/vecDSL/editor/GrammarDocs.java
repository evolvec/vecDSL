package org.ftn.vecDSL.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ftn.vecDSL.VecDSLRuntimeModule;

/**
 * The comments of VecDSL.xtext: the comment above each rule, which names the domain concept, and the comment at the
 * end of a clause line, which explains that clause. The grammar is read from the runtime bundle, where it sits next
 * to the runtime module.
 */
public final class GrammarDocs {

	private static final Pattern RULE = Pattern
			.compile("^(?:fragment\\s+|enum\\s+|terminal\\s+)?([A-Z]\\w*)(?:\\s+returns\\s+\\S+)?\\*?\\s*:\\s*$");
	private static final Pattern KEYWORD = Pattern.compile("'([^']+)'");
	private static final Pattern NOTE = Pattern.compile("\\bD(\\d{1,2})\\b");

	private final Map<String, String> rules = new HashMap<>();
	private final Map<String, String> clauses = new HashMap<>();

	private GrammarDocs() {
	}

	public static GrammarDocs empty() {
		return new GrammarDocs();
	}

	/** The comments of the grammar shipped with the runtime bundle, read once. */
	public static GrammarDocs ofLanguage() {
		return Language.DOCS;
	}

	private static final class Language {
		private static final GrammarDocs DOCS = read(VecDSLRuntimeModule.class.getResourceAsStream("VecDSL.xtext"));
	}

	public static GrammarDocs read(InputStream grammar) {
		GrammarDocs docs = new GrammarDocs();
		if (grammar == null) {
			return docs;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(grammar, StandardCharsets.UTF_8))) {
			List<String> comment = new ArrayList<>();
			String rule = null;
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				String text = line.strip();
				Matcher header = RULE.matcher(text);
				if (rule == null && header.matches()) {
					rule = header.group(1);
					docs.rules.put(rule, String.join(" ", comment));
				} else if (rule != null && text.equals(";")) {
					rule = null;
				} else if (rule != null) {
					docs.readClause(rule, text);
				}
				if (text.startsWith("//") && !text.startsWith("// ===") && !text.startsWith("// ---")
						&& !text.matches("// \\d+\\..*")) {
					comment.add(text.substring(2).strip());
				} else {
					comment.clear();
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return docs;
	}

	private void readClause(String rule, String line) {
		int comment = line.indexOf("//");
		if (comment < 0) {
			return;
		}
		Matcher keyword = KEYWORD.matcher(line.substring(0, comment));
		while (keyword.find()) {
			clauses.putIfAbsent(rule + " " + keyword.group(1), line.substring(comment + 2).strip());
		}
	}

	/** The comment above a rule, or the empty string. */
	public String rule(String rule) {
		return rules.getOrDefault(rule, "");
	}

	/** The comment at the end of the line of a rule that holds the keyword, or the empty string. */
	public String clause(String rule, String keyword) {
		return clauses.getOrDefault(rule + " " + keyword, "");
	}

	/** Design note numbers (D1 to D15) a text cites, in order of appearance. */
	public static Set<Integer> notes(String text) {
		Set<Integer> result = new LinkedHashSet<>();
		Matcher note = NOTE.matcher(text);
		while (note.find()) {
			result.add(Integer.valueOf(note.group(1)));
		}
		return result;
	}
}
