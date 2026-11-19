package org.ftn.vecDSL.generator.lancedb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Flag;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Code.Nil;
import org.ftn.vecDSL.generator.code.Code.Num;
import org.ftn.vecDSL.generator.code.Code.Text;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * SQL text of LanceDB where strings and update expressions (DataFusion syntax as parsed by Lance). Every identifier is quoted
 * with backquotes, every literal known at generation time is written by {@link #literal(Expr)} (single quotes, inner quotes
 * doubled), and every program parameter becomes a placeholder that the runtime fills with the same quoting rules.
 */
public final class LancedbSql {

	private final List<String> conditions = new ArrayList<>();
	private final Map<String, Expr> values = new LinkedHashMap<>();
	private final String prefix;

	public LancedbSql(String prefix) {
		this.prefix = prefix;
	}

	/** A column path: each segment in backquotes, inner backquotes doubled, segments joined by dots. */
	public static String id(String path) {
		return List.of(path.split("\\.")).stream().map(part -> "`" + part.replace("`", "``") + "`")
				.collect(Collectors.joining("."));
	}

	/** The SQL literal of a value known at generation time. */
	public static String literal(Expr value) {
		return switch (value) {
		case Text text -> text(text.value());
		case Num num -> Syntax.number(num.value());
		case Flag flag -> flag.value() ? "TRUE" : "FALSE";
		case Nil nil -> "NULL";
		case Items items -> items.items().stream().map(LancedbSql::literal).collect(Collectors.joining(", ", "[", "]"));
		default -> throw new IllegalArgumentException("no SQL literal for " + value);
		};
	}

	public static String text(String value) {
		return "'" + value.replace("'", "''") + "'";
	}

	/** A like pattern whose backslashes stay literal: % and _ keep their meaning, a backslash is LIKE's escape character. */
	public static String like(String pattern) {
		return text(pattern.replace("\\", "\\\\"));
	}

	/** Whether a value is known at generation time (a literal) rather than a program parameter or environment variable. */
	public static boolean constant(Expr value) {
		return switch (value) {
		case Text text -> true;
		case Num num -> true;
		case Flag flag -> true;
		case Nil nil -> true;
		case Items items -> items.items().stream().allMatch(LancedbSql::constant);
		default -> false;
		};
	}

	/** The SQL of a value: its literal, or a placeholder filled at run time; 'list' renders a list as (a, b) for IN. */
	public String value(Expr value, boolean list) {
		if (constant(value)) {
			if (!list) {
				return literal(value);
			}
			if (!(value instanceof Items items)) {
				throw Unsupported.of("in and not_in with a value that is not a list");
			}
			return items.items().stream().map(LancedbSql::literal).collect(Collectors.joining(", ", "(", ")"));
		}
		String name = prefix + values.size();
		values.put(name, value);
		return "{" + name + (list ? ":list" : "") + "}";
	}

	public LancedbSql where(String condition) {
		conditions.add(condition);
		return this;
	}

	public boolean isEmpty() {
		return conditions.isEmpty();
	}

	/** The conjunction of the conditions: a string, or a template with the placeholders of program parameters. */
	public Expr code() {
		if (conditions.isEmpty()) {
			return null;
		}
		String template = conditions.size() == 1 ? conditions.get(0)
				: conditions.stream().map(condition -> "(" + condition + ")").collect(Collectors.joining(" AND "));
		return values.isEmpty() ? new Text(template) : new Code.Filter(template, new LinkedHashMap<>(values));
	}
}
