package org.ftn.vecDSL.generator.redis;

import static org.ftn.vecDSL.generator.code.Code.entries;
import static org.ftn.vecDSL.generator.code.Code.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.redis.RedisIndex.Attr;
import org.ftn.vecDSL.generator.redis.RedisIndex.Index;

/**
 * Query Engine query strings and search commands, built at generation time. Values never enter a query string: each one is a
 * PARAMS entry ($p0, $p1, ...) that the server substitutes after parsing (DIALECT 2). A text operand becomes a marker such as
 * {{and:t0}}, which the runtime replaces by one parameter per term ($t0_0 $t0_1 ...), because the number of terms of a program
 * parameter is known only when the program runs.
 */
public final class RedisQuery {

	public static final String DIALECT = "2";

	private RedisQuery() {
	}

	public sealed interface Cond permits And, Or, Not, Test {
	}

	public record And(List<Cond> parts) implements Cond {
	}

	public record Or(List<Cond> parts) implements Cond {
	}

	public record Not(Cond operand) implements Cond {
	}

	/** One predicate on an attribute: a comparison, list, text or unary operator, with its operands (upper for between). */
	public record Test(Attr attr, String op, Expr value, Expr upper) implements Cond {
	}

	/** The PARAMS values and text markers of one command, numbered in the order the query string uses them. */
	public static final class Params {
		private final Entries values = entries();
		private final Entries texts = entries();
		private final String prefix;

		public Params() {
			this("");
		}

		/** Parameters named with a prefix, for the legs of a search that the runtime fuses. */
		public Params(String prefix) {
			this.prefix = prefix;
		}

		/** A value under a fixed name, such as the query vector. */
		public String named(String name, Expr value) {
			values.put(prefix + name, value);
			return "$" + prefix + name;
		}

		public String bind(Expr value) {
			String name = prefix + "p" + values.entries().size();
			values.put(name, value);
			return "$" + name;
		}

		/**
		 * A marker the runtime expands: and (all terms), or (any term), fuzzy1 to fuzzy3 (each term within that Levenshtein distance),
		 * prefix (each term as a prefix), prefixlast (the last term as a prefix), tags (the items of a list).
		 */
		public String terms(Expr value, String mode) {
			String name = prefix + "t" + texts.entries().size();
			texts.put(name, value);
			return "{{" + mode + ":" + name + "}}";
		}

		public Entries values() {
			return values;
		}

		public Entries texts() {
			return texts;
		}

		public boolean empty() {
			return values.entries().isEmpty() && texts.entries().isEmpty();
		}
	}

	/** The conjunction of the conditions as a query string; "*" (every document) when there are none. */
	public static String where(List<Cond> conditions, Params params) {
		List<String> parts = new ArrayList<>();
		for (Cond condition : conditions) {
			parts.add(group(render(condition, params)));
		}
		return parts.isEmpty() ? "*" : String.join(" ", parts);
	}

	public static String render(Cond cond, Params params) {
		return switch (cond) {
		case And and -> String.join(" ", and.parts().stream().map(part -> group(render(part, params))).toList());
		case Or or -> "(" + String.join(" | ", or.parts().stream().map(part -> group(render(part, params))).toList()) + ")";
		case Not not -> "-" + group(render(not.operand(), params));
		case Test test -> test(test, params);
		};
	}

	/** The query as one operand: as it is when it is already one group, one attribute test or a negated operand, else in parentheses. */
	static String group(String query) {
		return operand(query) || query.startsWith("-") && operand(query.substring(1)) ? query : "(" + query + ")";
	}

	private static boolean operand(String query) {
		return enclosed(query) || query.startsWith("@") && !query.contains(" ");
	}

	private static boolean enclosed(String query) {
		if (!query.startsWith("(")) {
			return false;
		}
		int depth = 0;
		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);
			if (c == '(') {
				depth++;
			} else if (c == ')' && --depth == 0) {
				return i == query.length() - 1;
			}
		}
		return false;
	}

	private static String test(Test test, Params params) {
		Attr attr = test.attr();
		String at = "@" + attr.name();
		String kind = attr.kind();
		return switch (test.op()) {
		case "is_null", "not_exists" -> "ismissing(" + at + ")";
		case "is_not_null", "exists" -> "-ismissing(" + at + ")";
		case "eq" -> kind.equals("NUMERIC") ? at + ":[" + num(test.value(), params) + " " + last(params) + "]" : tag(attr, test, params) + "{" + params.bind(test.value()) + "}";
		case "neq" -> "-" + test(new Test(attr, "eq", test.value(), null), params);
		case "gt" -> numeric(attr, test, "gt") + ":[(" + num(test.value(), params) + " +inf]";
		case "gte" -> numeric(attr, test, "gte") + ":[" + num(test.value(), params) + " +inf]";
		case "lt" -> numeric(attr, test, "lt") + ":[-inf (" + num(test.value(), params) + "]";
		case "lte" -> numeric(attr, test, "lte") + ":[-inf " + num(test.value(), params) + "]";
		case "between" -> numeric(attr, test, "between") + ":[" + num(test.value(), params) + " " + num(test.upper(), params) + "]";
		case "in", "contains_any" -> anyOf(attr, test, params);
		case "not_in" -> "-" + group(anyOf(attr, new Test(attr, "in", test.value(), null), params));
		case "contains" -> {
			if (!attr.array()) {
				throw Unsupported.of("contains on " + attr.name() + ", which is not an array field; use contains_text on a text field");
			}
			yield tag(attr, test, params) + "{" + params.bind(test.value()) + "}";
		}
		case "contains_all" -> {
			List<Expr> items = items(test);
			if (!attr.array() || items == null) {
				throw Unsupported.of("contains_all on " + attr.name() + (items == null ? " with a list parameter; the generator expands literal lists" : ", which is not an array field"));
			}
			yield "(" + String.join(" ", items.stream().map(item -> tag(attr, test, params) + "{" + params.bind(item) + "}").toList()) + ")";
		}
		case "prefix" -> tag(attr, test, params) + "{" + params.bind(test.value()) + "*}";
		case "like" -> {
			if (!(test.value() instanceof Code.Text pattern)) {
				throw Unsupported.of("like with an operand that is not a string literal; the generator converts the pattern when it generates the program");
			}
			yield tag(attr, test, params) + "{w'" + params.bind(text(wildcard(pattern.value()))) + "'}";
		}
		case "match", "match_all_terms" -> fullText(attr, test) + ":(" + params.terms(test.value(), "and") + ")";
		case "match_any_term" -> fullText(attr, test) + ":(" + params.terms(test.value(), "or") + ")";
		case "phrase" -> fullText(attr, test) + ":(" + params.terms(test.value(), "and") + ")=>{$slop: 0; $inorder: true;}";
		case "fuzzy" -> fullText(attr, test) + ":(" + params.terms(test.value(), "fuzzy1") + ")";
		case "wildcard" -> attr.kind().equals("TEXT") ? at + ":(w'" + params.bind(test.value()) + "')" : tag(attr, test, params) + "{w'" + params.bind(test.value()) + "'}";
		case "contains_text" -> attr.kind().equals("TEXT") ? at + ":(*" + params.bind(test.value()) + "*)" : tag(attr, test, params) + "{*" + params.bind(test.value()) + "*}";
		case "regex" -> throw Unsupported.of("the regex text operator; the Query Engine's query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions");
		case "is_empty" -> throw Unsupported.of("is_empty; has no Redis mapping yet (INDEXEMPTY attributes and an empty-value query)");
		default -> {
			if (test.op().startsWith("length_")) {
				throw Unsupported.of("length tests; the Query Engine's query syntax has no length operator (an APPLY expression of FT.AGGREGATE could compute one)");
			}
			throw Unsupported.of("the operator " + test.op());
		}
		};
	}

	private static String last(Params params) {
		Map<String, Expr> values = params.values().entries();
		return "$" + values.keySet().stream().reduce((first, second) -> second).orElseThrow();
	}

	private static String num(Expr value, Params params) {
		if (value instanceof Code.Text) {
			throw Unsupported.of("a string compared with a number field");
		}
		return params.bind(value);
	}

	private static String numeric(Attr attr, Test test, String op) {
		if (!attr.kind().equals("NUMERIC")) {
			throw Unsupported.of(op + " on " + attr.name() + ", which is not a number field; TAG attributes match values and prefixes, NUMERIC attributes compare ranges");
		}
		return "@" + attr.name();
	}

	private static String tag(Attr attr, Test test, Params params) {
		if (attr.kind().equals("TEXT")) {
			return "@" + RedisIndex.EXACT + attr.name() + ":";
		}
		if (!attr.kind().equals("TAG")) {
			throw Unsupported.of(test.op() + " on " + attr.name() + ", which is a " + attr.kind().toLowerCase() + " attribute");
		}
		return "@" + attr.name() + ":";
	}

	private static String fullText(Attr attr, Test test) {
		if (!attr.kind().equals("TEXT")) {
			throw Unsupported.of("the " + test.op() + " text operator on " + attr.name() + ", which is not a full-text field");
		}
		return "@" + attr.name();
	}

	private static List<Expr> items(Test test) {
		return test.value() instanceof Code.Items list ? list.items() : null;
	}

	private static String anyOf(Attr attr, Test test, Params params) {
		List<Expr> items = items(test);
		if (attr.kind().equals("NUMERIC")) {
			if (items == null) {
				throw Unsupported.of("in on number field " + attr.name() + " with a list parameter; the generator expands literal lists");
			}
			List<String> ranges = new ArrayList<>();
			for (Expr item : items) {
				String bound = num(item, params);
				ranges.add("@" + attr.name() + ":[" + bound + " " + bound + "]");
			}
			if (ranges.isEmpty()) {
				throw Unsupported.of("in with an empty list");
			}
			return "(" + String.join(" | ", ranges) + ")";
		}
		String head = tag(attr, test, params);
		if (items == null) {
			return head + "{" + params.terms(test.value(), "tags") + "}";
		}
		if (items.isEmpty()) {
			throw Unsupported.of("in with an empty list");
		}
		return head + "{" + String.join(" | ", items.stream().map(params::bind).toList()) + "}";
	}

	/** A like pattern as a wildcard: % is any run of characters, _ one character; other wildcard characters are escaped. */
	public static String wildcard(String like) {
		StringBuilder out = new StringBuilder();
		for (char c : like.toCharArray()) {
			switch (c) {
			case '%' -> out.append('*');
			case '_' -> out.append('?');
			case '*', '?', '\\', '\'' -> out.append('\\').append(c);
			default -> out.append(c);
			}
		}
		return out.toString();
	}

	// ---------------------------------------------------------------- commands

	/** KNN over the documents matching the filter, nearest first, with the distance as vecdsl_score. */
	public static List<String> knn(Index index, String where, Attr vector, int k, Map<String, String> attributes, String blob, int offset,
			int limit) {
		StringBuilder knn = new StringBuilder("[KNN " + k + " @" + vector.name() + " $" + blob);
		attributes.forEach((name, value) -> knn.append(' ').append(name).append(' ').append(value));
		knn.append(" AS ").append(RedisIndex.SCORE).append(']');
		String query = (where.equals("*") ? "*" : group(where)) + "=>" + knn;
		return new ArrayList<>(List.of("FT.SEARCH", index.name(), query, "SORTBY", RedisIndex.SCORE, "ASC", "LIMIT", String.valueOf(offset),
				String.valueOf(limit), "RETURN", "1", RedisIndex.SCORE, "DIALECT", DIALECT));
	}

	/** BM25 keyword search: the keys and scores of the best matches. */
	public static List<String> keyword(Index index, String query, int offset, int limit) {
		List<String> args = new ArrayList<>(List.of("FT.SEARCH", index.name(), query, "WITHSCORES", "NOCONTENT", "SCORER", "BM25STD"));
		language(index, args);
		args.addAll(List.of("LIMIT", String.valueOf(offset), String.valueOf(limit), "DIALECT", DIALECT));
		return args;
	}

	/** The number of matching documents (FT.SEARCH with LIMIT 0 0 returns only the total). */
	public static List<String> count(Index index, String where) {
		List<String> args = new ArrayList<>(List.of("FT.SEARCH", index.name(), where, "NOCONTENT"));
		language(index, args);
		args.addAll(List.of("LIMIT", "0", "0", "DIALECT", DIALECT));
		return args;
	}

	/**
	 * The keys of matching documents in a sort order (the key last, so that the order is total): FT.AGGREGATE loads @__key and
	 * sorts by several properties, which FT.SEARCH SORTBY cannot. A null limit makes the runtime read every page.
	 */
	public static List<String> ordered(Index index, String where, List<String> order, int offset, Integer limit) {
		List<String> args = new ArrayList<>(List.of("FT.AGGREGATE", index.name(), where, "LOAD", "1", "@__key", "SORTBY",
				String.valueOf(order.size())));
		args.addAll(order);
		args.addAll(List.of("LIMIT", String.valueOf(offset), String.valueOf(limit == null ? 1000 : limit), "DIALECT", DIALECT));
		return args;
	}

	/** Keys of the matching documents (for updates and deletes by filter), in key order, at most limit of them. */
	public static List<String> keys(Index index, String where, Integer limit) {
		return ordered(index, where, List.of("@" + index.key(), "ASC"), 0, limit);
	}

	/**
	 * FT.HYBRID with server-side RRF: one SEARCH (text) leg and one VSIM (vector) leg, each ranking its best window documents, fused
	 * as the sum of 1/(constant + rank) with ranks from 1 (the rrf of vecDSL).
	 */
	public static List<String> hybrid(Index index, String text, Attr vector, String blob, Map<String, String> knn, String vectorFilter,
			int window, int k, int offset, int limit) {
		List<String> args = new ArrayList<>(List.of("FT.HYBRID", index.name(), "SEARCH", text, "SCORER", "BM25STD", "VSIM", "@" + vector.name(),
				"$" + blob, "KNN", String.valueOf(2 + 2 * knn.size()), "K", String.valueOf(window)));
		knn.forEach((name, value) -> {
			args.add(name);
			args.add(value);
		});
		if (vectorFilter != null) {
			args.add("FILTER");
			args.add(vectorFilter);
		}
		args.addAll(List.of("COMBINE", "RRF", "4", "CONSTANT", String.valueOf(k), "WINDOW", String.valueOf(window), "LIMIT",
				String.valueOf(offset), String.valueOf(limit)));
		return args;
	}

	private static void language(Index index, List<String> args) {
		if (index.language() != null) {
			args.add("LANGUAGE");
			args.add(index.language());
		}
	}

	/**
	 * The arguments of a runtime search: the command, its parameters and text markers, and the stop words the runtime drops from
	 * the terms of a text (none given: the Query Engine's default list, which the runtimes hold).
	 */
	public static Entries search(List<String> command, Params params, Index index) {
		Entries args = entries();
		args.put("command", Code.texts(command));
		if (!params.values().entries().isEmpty()) {
			args.put("params", params.values());
		}
		if (!params.texts().entries().isEmpty()) {
			args.put("texts", params.texts());
			if (index.stopwords() != null) {
				args.put("stopwords", Code.texts(index.stopwords()));
			}
		}
		return args;
	}
}
