package org.ftn.vecDSL.generator.vespa;

import static org.ftn.vecDSL.generator.code.Code.entries;
import static org.ftn.vecDSL.generator.code.Code.num;
import static org.ftn.vecDSL.generator.code.Code.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * YQL and query request bodies. Every value a statement or a program parameter supplies travels as a request parameter and
 * enters the YQL as an @reference (YqlParser resolves the reference to the value and never parses it as query syntax); the
 * YQL text holds only field names, document type names, operators and numbers that come from the generator.
 */
public final class VespaQueries {

	/** The default number of hits per page of a scroll without a limit, and of an id selection. */
	public static final int PAGE = 1000;

	private VespaQueries() {
	}

	/** Request parameters bound to @references, with the type the runtime checks before sending: text, number or bool. */
	public static final class Params {

		private final String prefix;
		private final Map<String, Expr> texts = new LinkedHashMap<>();
		private final Map<String, Expr> numbers = new LinkedHashMap<>();
		private final Map<String, Expr> flags = new LinkedHashMap<>();
		private int next;

		public Params() {
			this("p");
		}

		public Params(String prefix) {
			this.prefix = prefix;
		}

		/** Binds a value and returns its @reference. */
		public String bind(Expr value, String type) {
			String name = prefix + next++;
			switch (type) {
			case "number" -> numbers.put(name, value);
			case "bool" -> flags.put(name, value);
			default -> texts.put(name, value);
			}
			return "@" + name;
		}

		public boolean empty() {
			return texts.isEmpty() && numbers.isEmpty() && flags.isEmpty();
		}

		/** Adds the bound values to the arguments of a runtime call: texts, numbers and flags, each only when present. */
		public void into(Entries args) {
			put(args, "texts", texts);
			put(args, "numbers", numbers);
			put(args, "flags", flags);
		}

		private static void put(Entries args, String name, Map<String, Expr> values) {
			if (!values.isEmpty()) {
				args.put(name, Code.entries(values));
			}
		}
	}

	public sealed interface Cond permits And, Or, Not, Test, Raw {
	}

	public record And(List<Cond> parts) implements Cond {
	}

	public record Or(List<Cond> parts) implements Cond {
	}

	public record Not(Cond operand) implements Cond {
	}

	/** A YQL fragment built by the generator from names and generator constants only. */
	public record Raw(String yql) implements Cond {
	}

	/**
	 * A predicate on one field. kind is the field's kind (NUMERIC, BOOL, STRING, TEXT); op is a vecDSL comparison, a
	 * text operator (text_*), between, prefix or eq. Values are code-model expressions bound as parameters.
	 */
	public record Test(String field, String kind, String op, Expr value, Expr upper) implements Cond {
	}

	public static String where(List<Cond> parts, Params bound) {
		if (parts.isEmpty()) {
			return "true";
		}
		return parts.size() == 1 ? yql(parts.get(0), bound) : yql(new And(parts), bound);
	}

	public static String yql(Cond cond, Params bound) {
		return switch (cond) {
		case And and -> "(" + String.join(" and ", and.parts().stream().map(part -> yql(part, bound)).toList()) + ")";
		case Or or -> "(" + String.join(" or ", or.parts().stream().map(part -> yql(part, bound)).toList()) + ")";
		case Not not -> "(true and !(" + yql(not.operand(), bound) + "))";
		case Raw raw -> raw.yql();
		case Test test -> test(test, bound);
		};
	}

	private static String test(Test t, Params bound) {
		String kind = t.kind();
		return switch (t.op()) {
		case "eq" -> equal(t.field(), kind, t.value(), bound);
		case "neq" -> "(true and !(" + equal(t.field(), kind, t.value(), bound) + "))";
		case "gt", "gte", "lt", "lte" -> {
			numeric(t);
			yield t.field() + " " + Map.of("gt", ">", "gte", ">=", "lt", "<", "lte", "<=").get(t.op()) + " " + bound.bind(t.value(), "number");
		}
		case "between" -> {
			numeric(t);
			yield "(" + t.field() + " >= " + bound.bind(t.value(), "number") + " and " + t.field() + " <= " + bound.bind(t.upper(), "number") + ")";
		}
		case "in", "contains_any" -> any(t.field(), kind, items(t.value()), bound);
		case "not_in" -> "(true and !(" + any(t.field(), kind, items(t.value()), bound) + "))";
		case "contains_all" -> {
			List<Expr> values = items(t.value());
			yield "(" + String.join(" and ", values.stream().map(value -> equal(t.field(), kind, value, bound)).toList()) + ")";
		}
		case "contains" -> equal(t.field(), kind, t.value(), bound);
		case "prefix" -> {
			if (!"STRING".equals(kind)) {
				throw Unsupported.of("prefix on " + t.field() + ", which is not an exact string field");
			}
			yield t.field() + " contains ({prefix: true}" + bound.bind(t.value(), "text") + ")";
		}
		case "text_all" -> userInput(t.field(), "and", t.value(), bound);
		case "text_any" -> userInput(t.field(), "or", t.value(), bound);
		case "text_phrase" -> userInput(t.field(), "phrase", t.value(), bound);
		default -> throw new IllegalArgumentException("no YQL form for " + t.op());
		};
	}

	private static void numeric(Test t) {
		if (!"NUMERIC".equals(t.kind())) {
			throw Unsupported.of("range comparisons on " + t.field() + ", which is not a numeric field; YQL compares numeric attributes"
					+ " with <, <=, >, >= and range() (string attributes match words, prefixes and regular expressions)");
		}
	}

	private static String equal(String field, String kind, Expr value, Params bound) {
		return switch (kind) {
		case "NUMERIC" -> field + " = " + bound.bind(value, "number");
		case "BOOL" -> field + " = " + bound.bind(value, "bool");
		case "STRING" -> field + " contains " + bound.bind(value, "text");
		case "TENSOR" -> throw Unsupported.of("a filter on vector field " + field + "; YQL searches tensors with nearestNeighbor only");
		default -> throw Unsupported.of("exact comparison on " + field + ", which is a full-text field");
		};
	}

	private static String any(String field, String kind, List<Expr> values, Params bound) {
		if (values.isEmpty()) {
			return "false";
		}
		return "(" + String.join(" or ", values.stream().map(value -> equal(field, kind, value, bound)).toList()) + ")";
	}

	private static List<Expr> items(Expr value) {
		if (value instanceof Code.Items list) {
			return list.items();
		}
		throw Unsupported.of("a value list from a parameter; YQL parameter lists cannot carry quoted strings safely"
				+ " (ParameterListParser.stringTokens has no escape), so the generator binds each listed value on its own");
	}

	/** Text of a full-text field matched as all terms, any term or a phrase; the user text is never parsed as query syntax. */
	public static String userInput(String field, String composite, Expr value, Params bound) {
		return userInput(field, composite, value, bound, null);
	}

	/** userInput with a composite near or onear and the distance annotation: the largest position difference of the terms. */
	public static String userInput(String field, String composite, Expr value, Params bound, Integer distance) {
		String annotations = "defaultIndex: \"" + field + "\", grammar.composite: \"" + composite
				+ "\", grammar.tokenization: \"linguistics\", grammar.syntax: \"none\"";
		if (distance != null) {
			annotations += ", distance: " + distance;
		}
		return "({" + annotations + "}userInput(" + bound.bind(value, "text") + "))";
	}

	/** The grouping expression of group-by: the groups ordered by their best hit (-max(relevance())), with their hits. */
	public static String grouping(String attribute, int groups, int hitsPerGroup) {
		return " | all(group(" + attribute + ") max(" + groups + ") each(max(" + hitsPerGroup + ") each(output(summary()))))";
	}

	/** Nearest-neighbour operator over a tensor field with the query tensor vecdsl_q and its annotations. */
	public static String nearest(String field, int targetHits, boolean exact, Double distanceThreshold, Integer exploreAdditionalHits) {
		List<String> annotations = new ArrayList<>();
		annotations.add("targetHits: " + targetHits);
		if (exact) {
			annotations.add("approximate: false");
		}
		if (exploreAdditionalHits != null) {
			annotations.add("hnsw.exploreAdditionalHits: " + exploreAdditionalHits);
		}
		if (distanceThreshold != null) {
			annotations.add("distanceThreshold: " + VespaPackage.number(distanceThreshold));
		}
		return "({" + String.join(", ", annotations) + "}nearestNeighbor(" + field + ", " + VespaPackage.QUERY_TENSOR + "))";
	}

	/** select * from <type> where <where> [order by ...]: hits and offset travel as request parameters. */
	public static String select(String documentType, String where, List<String> order) {
		String yql = "select * from " + documentType + " where " + where;
		return order.isEmpty() ? yql : yql + " order by " + String.join(", ", order);
	}

	/**
	 * A query request body: yql, hits, offset, rank profile, tensors rendered as plain arrays, and the optional timeout (milliseconds,
	 * with the soft timeout off so that a late query fails instead of returning partial hits).
	 */
	public static Entries body(String yql, int hits, int offset, String profile, Integer timeoutMillis) {
		Entries body = entries();
		body.put("yql", text(yql));
		body.put("hits", num(hits));
		if (offset > 0) {
			body.put("offset", num(offset));
		}
		Entries ranking = entries();
		ranking.put("profile", text(profile));
		if (timeoutMillis != null) {
			Entries soft = entries();
			soft.put("enable", Code.flag(false));
			ranking.put("softtimeout", soft);
		}
		body.put("ranking", ranking);
		Entries format = entries();
		format.put("tensors", text("short-value"));
		Entries presentation = entries();
		presentation.put("format", format);
		body.put("presentation", presentation);
		if (timeoutMillis != null) {
			body.put("timeout", text(timeoutMillis + "ms"));
		}
		return body;
	}
}
