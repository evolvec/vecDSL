package org.ftn.vecDSL.generator.duckdb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * SQL text for DuckDB with the vss and fts extensions. Every statement is built when the program is generated, so Python,
 * TypeScript and Java send the same text; values are bound as numbered text parameters ($1, $2, ...) and cast in the SQL
 * (lists, vectors and records through JSON), never written into the SQL. Queries return one JSON column per row, which the
 * runtimes decode the same way. The Xtend mapping translates vecDSL concepts into the descriptors below (Table, Column, Vec,
 * Cond) and calls the builders.
 */
public final class DuckdbSql {

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
	public static final String ALIAS = "t";
	/** The schema of tables outside a logical database. */
	public static final String MAIN = "main";

	/** Stemmers of the fts extension (DuckDB 2.0 documentation, full_text_search, create_fts_index). */
	public static final Set<String> STEMMERS = Set.of("arabic", "basque", "catalan", "danish", "dutch", "english", "finnish", "french",
			"german", "greek", "hindi", "hungarian", "indonesian", "irish", "italian", "lithuanian", "nepali", "norwegian", "porter",
			"portuguese", "romanian", "russian", "serbian", "spanish", "swedish", "tamil", "turkish");

	private static final Map<String, String> LANGUAGES = Map.ofEntries(Map.entry("ar", "arabic"), Map.entry("eu", "basque"),
			Map.entry("ca", "catalan"), Map.entry("da", "danish"), Map.entry("nl", "dutch"), Map.entry("en", "english"),
			Map.entry("fi", "finnish"), Map.entry("fr", "french"), Map.entry("de", "german"), Map.entry("el", "greek"),
			Map.entry("hi", "hindi"), Map.entry("hu", "hungarian"), Map.entry("id", "indonesian"), Map.entry("ga", "irish"),
			Map.entry("it", "italian"), Map.entry("lt", "lithuanian"), Map.entry("ne", "nepali"), Map.entry("no", "norwegian"),
			Map.entry("pt", "portuguese"), Map.entry("ro", "romanian"), Map.entry("ru", "russian"), Map.entry("sr", "serbian"),
			Map.entry("es", "spanish"), Map.entry("sv", "swedish"), Map.entry("ta", "tamil"), Map.entry("tr", "turkish"));

	private DuckdbSql() {
	}

	// ---------------------------------------------------------------- descriptors

	/** A vector column: FLOAT[dim] with its metric (cosine, euclidean or dot). */
	public record Vec(int dim, String metric) {

		public String sql() {
			return "FLOAT[" + dim + "]";
		}
	}

	/**
	 * A table column: name, SQL type (for lists the list type), element type of a list, json for JSON columns, the text
	 * analysis of a full-text column, nullability, declared default and CHECK conditions ({} stands for the column).
	 */
	public record Column(String name, String type, String element, boolean json, Vec vec, Analysis analysis, boolean nullable,
			Expr defaultValue, List<String> checks) {

		public boolean array() {
			return element != null;
		}

		public boolean fullText() {
			return analysis != null;
		}

		/** The type a bound text parameter is cast to. */
		public String paramType() {
			return vec != null ? vec.sql() : type;
		}
	}

	/** Text analysis of a full-text field: the fts index options (stemmer, stop words, accents, lower case). */
	public record Analysis(String stemmer, String stopwords, boolean stripAccents, boolean lower) {

		public static final Analysis DEFAULT = new Analysis("none", "none", false, true);
	}

	/**
	 * A collection as a table: schema (main outside a logical database), name, key column, columns in declaration order, the
	 * columns that hold tenant and namespace names (null when the collection has none), and fields renamed by earlier alters.
	 */
	public record Table(String schema, String name, String key, List<Column> columns, String tenant, String namespace,
			Map<String, String> renames) {

		/** The column of a field, by its declared name; a field renamed by an earlier alter resolves to its new column. */
		public Column column(String field) {
			String column = renames.getOrDefault(field, field);
			for (Column c : columns) {
				if (c.name().equals(column)) return c;
			}
			if (column.equals(tenant) || column.equals(namespace)) {
				return new Column(column, "VARCHAR", null, false, null, null, true, null, List.of());
			}
			throw Unsupported.of("field " + column + ", which is not a field of collection " + name);
		}

		public Table with(Column added) {
			List<Column> result = new ArrayList<>(columns);
			result.add(added);
			return new Table(schema, name, key, List.copyOf(result), tenant, namespace, renames);
		}

		public Table without(String field) {
			String column = column(field).name();
			return new Table(schema, name, key, columns.stream().filter(c -> !c.name().equals(column)).toList(), tenant, namespace, renames);
		}

		public Table renamed(String field, String newName) {
			Column old = column(field);
			Column next = new Column(newName, old.type(), old.element(), old.json(), old.vec(), old.analysis(), old.nullable(), old.defaultValue(),
					old.checks());
			Map<String, String> names = new LinkedHashMap<>(renames);
			names.put(field, newName);
			return new Table(schema, name, key, columns.stream().map(c -> c.name().equals(old.name()) ? next : c).toList(), tenant, namespace,
					Map.copyOf(names));
		}

		public Table named(String newName) {
			return new Table(schema, newName, key, columns, tenant, namespace, renames);
		}

		public Column keyColumn() {
			return column(key);
		}

		public List<String> sliceColumns() {
			List<String> result = new ArrayList<>();
			for (String slice : new String[] { tenant, namespace }) {
				if (slice != null && columns.stream().noneMatch(c -> c.name().equals(slice))) result.add(slice);
			}
			return result;
		}

		public List<Column> fullTextColumns() {
			return columns.stream().filter(Column::fullText).toList();
		}
	}

	/** A filter condition: conjunction, disjunction, negation or a test on one column (a JSON path below it when given). */
	public sealed interface Cond permits And, Or, Not, Test, Raw {
	}

	public record And(List<Cond> parts) implements Cond {
	}

	public record Or(List<Cond> parts) implements Cond {
	}

	public record Not(Cond operand) implements Cond {
	}

	/**
	 * A test: column, JSON path below a JSON column (empty otherwise) with the SQL type of the leaf, operator, value and the
	 * upper bound of 'between'. Operators: eq, neq, gt, gte, lt, lte, in, not_in, like, prefix, contains, contains_any,
	 * contains_all, between, is_null, is_not_null, exists, not_exists, is_empty, length_(eq|neq|gt|gte|lt|lte), match,
	 * match_all_terms, match_any_term, phrase, regex, contains_text, wildcard.
	 */
	public record Test(String column, List<String> path, String pathType, String op, Expr value, Expr upper) implements Cond {
	}

	/** A condition on a slice column with fixed values: tenant or namespace. */
	public record Raw(String column, List<Expr> values) implements Cond {
	}

	/** A statement under construction: numbered parameters with their values. */
	public static final class Query {

		private final List<Expr> params = new ArrayList<>();

		/** A placeholder for the value, cast to the given type: $3::INTEGER. */
		public String bind(Expr value, String cast) {
			params.add(value);
			return "$" + params.size() + "::" + cast;
		}

		/** A list or vector value read from its JSON text: $3::JSON::VARCHAR[]. */
		public String json(Expr value, String type) {
			params.add(value);
			return "$" + params.size() + "::JSON::" + type;
		}

		/** A list value as a list of the element type; a null value stays null. */
		public String array(Expr value, String element) {
			return json(value, element + "[]");
		}

		public Code.Items params() {
			return Code.items(params);
		}

		public int size() {
			return params.size();
		}
	}

	// ---------------------------------------------------------------- names and literals

	/** A quoted identifier; vecDSL names are checked against the identifier rule of the generated SQL. */
	public static String id(String name) {
		if (!IDENTIFIER.matcher(name).matches()) {
			throw Unsupported.of("the name '" + name + "'; generated SQL uses identifiers of letters, digits and underscores");
		}
		return "\"" + name + "\"";
	}

	/** A column of the query's table alias: t."price". */
	public static String col(String name) {
		return ALIAS + "." + id(name);
	}

	/** A string constant for DDL and pragmas, where DuckDB takes no parameters (defaults, comments, fts options). */
	public static String literal(String value) {
		if (value.indexOf('\0') >= 0) throw Unsupported.of("a string with a NUL character; DuckDB VARCHAR values cannot hold it");
		return "'" + value.replace("'", "''") + "'";
	}

	/** A DDL constant for a vecDSL literal value of the given column. */
	public static String constant(Expr value, Column column) {
		String cast = column.paramType();
		return switch (value) {
		case Code.Text text -> literal(text.value()) + "::" + cast;
		case Code.Num num -> literal(number(num.value())) + "::" + cast;
		case Code.Flag flag -> literal(Boolean.toString(flag.value())) + "::" + cast;
		case Code.Nil nil -> "NULL";
		case Code.Items items when column.array() || column.vec() != null -> literal(json(items)) + "::JSON::" + cast;
		case Code.Items items when column.json() -> literal(json(items)) + "::JSON";
		case Code.Entries entries when column.json() -> literal(json(entries)) + "::JSON";
		default -> throw Unsupported.of("a default value of this kind for column " + column.name()
				+ "; defaults are string, number, boolean, null and list literals");
		};
	}

	static String number(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	/** JSON text of a literal, for DDL constants. */
	static String json(Expr value) {
		return switch (value) {
		case Code.Text text -> Syntax.quote(text.value());
		case Code.Num num -> number(num.value());
		case Code.Flag flag -> Boolean.toString(flag.value());
		case Code.Nil nil -> "null";
		case Code.Items items -> items.items().stream().map(DuckdbSql::json).collect(Collectors.joining(",", "[", "]"));
		case Code.Entries entries -> entries.entries().entrySet().stream().map(e -> Syntax.quote(e.getKey()) + ":" + json(e.getValue()))
				.collect(Collectors.joining(",", "{", "}"));
		default -> throw Unsupported.of("a parameter reference in a DDL constant; DuckDB DDL takes no parameters");
		};
	}

	/** The fts stemmer of a language name or code, else null. */
	public static String stemmer(String language) {
		String lower = language.toLowerCase(java.util.Locale.ROOT);
		if (STEMMERS.contains(lower)) return lower;
		return LANGUAGES.get(lower.length() > 2 && lower.charAt(2) == '-' ? lower.substring(0, 2) : lower);
	}

	/** The qualified table name: "schema"."name". */
	public static String table(Table table) {
		return id(table.schema()) + "." + id(table.name());
	}

	/** The qualified name of an object in the table's schema (index, view). */
	public static String qualified(Table table, String name) {
		return id(table.schema()) + "." + id(name);
	}

	// ---------------------------------------------------------------- metrics

	/** The distance function of a metric (DuckDB array functions; the vss index metrics cosine, l2sq and ip pair with them). */
	public static String distance(String metric) {
		return switch (metric) {
		case "cosine" -> "array_cosine_distance";
		case "euclidean" -> "array_distance";
		case "dot" -> "array_negative_inner_product";
		default -> throw Unsupported.of(metric + " metric; DuckDB measures cosine (array_cosine_distance), euclidean (array_distance) and "
				+ "dot (array_negative_inner_product) distances between FLOAT arrays");
		};
	}

	/** The vss index metric of a vecDSL metric (vss README, metric table). */
	public static String indexMetric(String metric) {
		return switch (metric) {
		case "cosine" -> "cosine";
		case "euclidean" -> "l2sq";
		case "dot" -> "ip";
		default -> throw Unsupported.of(metric + " metric for an HNSW index; vss indexes the metrics cosine, l2sq and ip");
		};
	}

	/** Canonical similarity of a distance d: cosine 1 - d, dot -d (the negative inner product), euclidean -d. */
	public static String similarity(String metric, String distance) {
		return "cosine".equals(metric) ? "1 - " + distance : "-" + distance;
	}

	// ---------------------------------------------------------------- DDL

	/** CREATE TABLE with columns, defaults, CHECK limits and the primary key. */
	public static String createTable(Table table, boolean ifNotExists) {
		List<String> parts = new ArrayList<>();
		for (Column column : table.columns()) parts.add(columnDefinition(column, column.name().equals(table.key())));
		for (String slice : table.sliceColumns()) parts.add(id(slice) + " VARCHAR");
		parts.add("PRIMARY KEY (" + id(table.key()) + ")");
		return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table(table) + " (" + String.join(", ", parts) + ")";
	}

	/** One column of CREATE TABLE or ALTER TABLE ADD COLUMN. */
	public static String columnDefinition(Column column, boolean key) {
		StringBuilder out = new StringBuilder(id(column.name())).append(' ').append(column.paramType());
		if (!column.nullable() && !key) out.append(" NOT NULL");
		if (column.defaultValue() != null) out.append(" DEFAULT ").append(constant(column.defaultValue(), column));
		for (String check : column.checks()) out.append(' ').append(check.replace("{}", id(column.name())));
		return out.toString();
	}

	/** An HNSW index (vss) with the metric of the column and the given options (ef_construction, ef_search, M, M0). */
	public static String createVectorIndex(Table table, String name, Column column, Map<String, Integer> options, boolean ifNotExists) {
		List<String> with = new ArrayList<>();
		with.add("metric = " + literal(indexMetric(column.vec().metric())));
		options.forEach((key, value) -> with.add(key + " = " + value));
		return "CREATE INDEX " + (ifNotExists ? "IF NOT EXISTS " : "") + id(name) + " ON " + table(table) + " USING HNSW (" + id(column.name())
				+ ") WITH (" + String.join(", ", with) + ")";
	}

	/** An ART index on a scalar column (DuckDB's CREATE INDEX). */
	public static String createPayloadIndex(Table table, String name, Column column, boolean ifNotExists) {
		return "CREATE INDEX " + (ifNotExists ? "IF NOT EXISTS " : "") + id(name) + " ON " + table(table) + " (" + id(column.name()) + ")";
	}

	/** The schema the fts extension creates for a table's index: fts_<schema>_<table>. */
	public static String ftsSchema(Table table) {
		return id("fts_" + table.schema() + "_" + table.name());
	}

	/**
	 * The fts index of a table over its full-text columns, rebuilt in place (overwrite): the index does not follow writes, and
	 * its incremental mode refuses ON CONFLICT DO UPDATE (DuckDB 2.0.0, measured), so every write statement rebuilds it.
	 */
	public static String createFtsIndex(Table table) {
		List<Column> columns = table.fullTextColumns();
		if (columns.isEmpty()) return null;
		Analysis analysis = analysis(table);
		return "PRAGMA create_fts_index(" + literal(table.schema() + "." + table.name()) + ", " + literal(table.key()) + ", "
				+ columns.stream().map(c -> literal(c.name())).collect(Collectors.joining(", ")) + ", stemmer = " + literal(analysis.stemmer())
				+ ", stopwords = " + literal(analysis.stopwords()) + ", strip_accents = " + (analysis.stripAccents() ? 1 : 0) + ", lower = "
				+ (analysis.lower() ? 1 : 0) + ", overwrite = 1)";
	}

	public static String dropFtsIndex(Table table) {
		return "PRAGMA drop_fts_index(" + literal(table.schema() + "." + table.name()) + ")";
	}

	/** The one text analysis of a table's full-text columns: the fts extension builds one index per table with one stemmer. */
	public static Analysis analysis(Table table) {
		List<Analysis> distinct = table.fullTextColumns().stream().map(Column::analysis).distinct().toList();
		if (distinct.size() > 1) {
			throw Unsupported.of("full-text fields with different analyzers in collection " + table.name()
					+ "; the fts extension builds one index per table with one stemmer and one stop-word list");
		}
		return distinct.get(0);
	}

	/** A column under an alias, or a sub-field read from its JSON value with ->> and cast to the sub-field's type. */
	public static String reference(String alias, Column column, List<String> path, String pathType) {
		String ref = alias + "." + id(column.name());
		if (path.isEmpty()) return ref;
		return "(" + ref + " ->> " + literal(jsonPath(path)) + ")" + ("VARCHAR".equals(pathType) ? "" : "::" + pathType);
	}

	/** A JSON path with quoted keys: $."a"."b". */
	static String jsonPath(List<String> path) {
		return "$" + path.stream().map(segment -> "." + Syntax.quote(segment)).collect(Collectors.joining());
	}

	// ---------------------------------------------------------------- filters

	/** A WHERE condition over the table alias t; null when there is no condition. */
	public static String where(Query q, Table table, List<Cond> conditions) {
		List<String> parts = new ArrayList<>();
		for (Cond condition : conditions) parts.add(condition(q, table, condition));
		return parts.isEmpty() ? null : String.join(" AND ", parts.stream().map(p -> parts.size() > 1 ? "(" + p + ")" : p).toList());
	}

	private static String condition(Query q, Table table, Cond cond) {
		return switch (cond) {
		case And and -> and.parts().stream().map(p -> "(" + condition(q, table, p) + ")").collect(Collectors.joining(" AND "));
		case Or or -> or.parts().stream().map(p -> "(" + condition(q, table, p) + ")").collect(Collectors.joining(" OR "));
		case Not not -> "(" + condition(q, table, not.operand()) + ") IS NOT TRUE";
		case Raw raw -> raw.values().size() == 1 ? col(raw.column()) + " = " + q.bind(raw.values().get(0), "VARCHAR")
				: "list_contains(" + q.array(Code.items(raw.values()), "VARCHAR") + ", " + col(raw.column()) + ")";
		case Test test -> test(q, table, test);
		};
	}

	private static final Set<String> ORDERED = Set.of("TINYINT", "SMALLINT", "INTEGER", "BIGINT", "FLOAT", "DOUBLE", "DATE", "TIMESTAMP",
			"TIMESTAMPTZ", "VARCHAR", "UUID");

	private static String test(Query q, Table table, Test test) {
		Column column = table.column(test.column());
		boolean nested = !test.path().isEmpty();
		String type = nested ? test.pathType() : column.paramType();
		String ref = nested ? reference(ALIAS, column, test.path(), type) : col(column.name());
		boolean array = !nested && column.array();
		String element = array ? column.element() : type;
		String op = test.op();
		if (op.startsWith("length_")) {
			String length = array ? "len(" + ref + ")" : "length(" + ref + ")";
			return length + " " + comparison(op.substring(7)) + " " + q.bind(test.value(), "BIGINT");
		}
		switch (op) {
		case "is_null", "not_exists":
			return ref + " IS NULL";
		case "is_not_null", "exists":
			return ref + " IS NOT NULL";
		case "is_empty":
			return array ? "len(" + ref + ") = 0" : ref + " = ''";
		default:
			break;
		}
		if (column.vec() != null) throw Unsupported.of("filters on vector field " + column.name());
		if (column.json() && !nested) throw Unsupported.of("comparisons on the whole json field " + column.name() + "; filter on its sub-fields");
		switch (op) {
		case "eq":
			return array ? ref + " = " + q.array(test.value(), element) : ref + " = " + q.bind(test.value(), type);
		case "neq":
			return ref + " IS DISTINCT FROM " + (array ? q.array(test.value(), element) : q.bind(test.value(), type));
		case "gt", "gte", "lt", "lte":
			ordered(column, type, array);
			return ref + " " + comparison(op) + " " + q.bind(test.value(), type);
		case "between":
			ordered(column, type, array);
			return ref + " BETWEEN " + q.bind(test.value(), type) + " AND " + q.bind(test.upper(), type);
		case "in":
			scalar(column, array, op);
			return "list_contains(" + q.array(test.value(), type) + ", " + ref + ")";
		case "not_in":
			scalar(column, array, op);
			return "(list_contains(" + q.array(test.value(), type) + ", " + ref + ")) IS NOT TRUE";
		case "contains":
			return array ? "list_contains(" + ref + ", " + q.bind(test.value(), element) + ")"
					: "strpos(" + text(ref, type) + ", " + q.bind(test.value(), "VARCHAR") + ") > 0";
		case "contains_any":
			arrayOnly(column, array, op);
			return "list_has_any(" + ref + ", " + q.array(test.value(), element) + ")";
		case "contains_all":
			arrayOnly(column, array, op);
			return "list_has_all(" + ref + ", " + q.array(test.value(), element) + ")";
		case "like":
			return text(ref, type) + " LIKE " + q.bind(test.value(), "VARCHAR") + " ESCAPE '\\'";
		case "prefix":
			return "starts_with(" + text(ref, type) + ", " + q.bind(test.value(), "VARCHAR") + ")";
		case "contains_text":
			return "strpos(" + text(ref, type) + ", " + q.bind(test.value(), "VARCHAR") + ") > 0";
		case "regex":
			return "regexp_matches(" + text(ref, type) + ", " + q.bind(test.value(), "VARCHAR") + ")";
		case "wildcard": {
			String p = q.bind(test.value(), "VARCHAR");
			return text(ref, type) + " LIKE replace(replace(replace(replace(replace(" + p
					+ ", '\\', '\\\\'), '%', '\\%'), '_', '\\_'), '*', '%'), '?', '_') ESCAPE '\\'";
		}
		case "match", "match_all_terms", "match_any_term":
			if (nested || !column.fullText()) {
				throw Unsupported.of("the " + op + " operator on " + column.name() + ", which is not a full-text field; the fts extension matches "
						+ "terms of the columns of its index");
			}
			return match(q, table, List.of(column), op.equals("match_any_term") ? "or" : "and", test.value()) + " IS NOT NULL";
		case "phrase":
			throw Unsupported.of("the phrase operator; " + NO_POSITIONS);
		default:
			throw new IllegalArgumentException(op);
		}
	}

	/** Why phrases have no DuckDB mapping: the fts index keeps no term positions. */
	public static final String NO_POSITIONS = "the fts index stores which terms occur in which field of a document (table terms: termid, docid, "
			+ "fieldid), not their positions, so match_bm25 scores bags of terms and cannot match a phrase";

	/**
	 * The BM25 score of the table's key over full-text columns (fts match_bm25): null for rows that do not match. Operator
	 * and requires every term (conjunctive := 1); boosts become field weights of the bm25f scoring.
	 */
	public static String match(Query q, Table table, List<Column> columns, String operator, Expr text) {
		return match(q, table, columns.stream().map(c -> Map.entry(c.name(), 1.0)).toList(), operator, text, q.bind(text, "VARCHAR"));
	}

	public static String match(Query q, Table table, List<Map.Entry<String, Double>> fields, String operator, Expr text, String bound) {
		StringBuilder out = new StringBuilder(ftsSchema(table)).append(".match_bm25(").append(col(table.key())).append(", ").append(bound);
		boolean all = fields.size() == table.fullTextColumns().size();
		if (!all) out.append(", fields := ").append(literal(fields.stream().map(Map.Entry::getKey).collect(Collectors.joining(","))));
		if (fields.stream().anyMatch(f -> f.getValue() != 1.0)) {
			out.append(", field_weights := MAP {").append(fields.stream().map(f -> literal(f.getKey()) + ": " + number(f.getValue()))
					.collect(Collectors.joining(", "))).append("}");
		}
		if (operator.equals("and")) out.append(", conjunctive := 1");
		return out.append(")").toString();
	}

	private static String text(String ref, String type) {
		return "VARCHAR".equals(type) ? ref : ref + "::VARCHAR";
	}

	private static void ordered(Column column, String type, boolean array) {
		if (array || !ORDERED.contains(type)) {
			throw Unsupported.of("ordered comparisons on " + column.name() + " of type " + (array ? column.type() : type));
		}
	}

	private static void scalar(Column column, boolean array, String op) {
		if (array) throw Unsupported.of(op + " on array field " + column.name() + "; use contains_any or contains_all");
	}

	private static void arrayOnly(Column column, boolean array, String op) {
		if (!array) throw Unsupported.of(op + " on " + column.name() + ", which is not an array");
	}

	private static String comparison(String op) {
		return switch (op) {
		case "eq" -> "=";
		case "neq" -> "<>";
		case "gt" -> ">";
		case "gte" -> ">=";
		case "lt" -> "<";
		case "lte" -> "<=";
		default -> throw new IllegalArgumentException(op);
		};
	}

	// ---------------------------------------------------------------- values

	/** A value written into a column: cast to its type, lists and vectors through JSON, json values as JSON text. */
	public static String value(Query q, Column column, Expr value) {
		if (column.array() || column.vec() != null) return q.json(value, column.paramType());
		if (column.json()) return q.bind(jsonValue(value), "JSON");
		return q.bind(value, column.paramType());
	}

	/** A json field value bound as JSON text, so that a string stays a JSON string. */
	private static Expr jsonValue(Expr value) {
		return value instanceof Code.Text text ? new Code.Text(Syntax.quote(text.value())) : value;
	}

	/** The output expression of a column in a result object: JSON columns as JSON values, everything else as it is. */
	public static String output(Column column, String alias) {
		return alias + "." + id(column.name());
	}

	/** json_object over name and expression pairs, in order. */
	public static String object(Map<String, String> fields) {
		return "json_object(" + fields.entrySet().stream().map(e -> literal(e.getKey()) + ", " + e.getValue()).collect(Collectors.joining(", "))
				+ ")";
	}

	/** The record fields of rows: the key under its own name, then the output columns. */
	public static Map<String, String> fields(Table table, List<String> output, String alias) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(table.key(), output(table.keyColumn(), alias));
		for (String name : output) result.put(name, output(table.column(name), alias));
		return result;
	}

	// ---------------------------------------------------------------- writes

	/** The json_transform structure of a table's columns: {"name":"TYPE", ...}. */
	private static String structure(List<Column> columns) {
		return columns.stream().map(c -> Syntax.quote(c.name()) + ":" + Syntax.quote(c.json() ? "JSON" : c.paramType()))
				.collect(Collectors.joining(",", "[{", "}]"));
	}

	/** The records of a JSON array parameter as rows e(r, doc): r the typed record, doc the element (for json_exists). */
	private static String records(String rowsParam, List<Column> columns) {
		String p = rowsParam.substring(0, rowsParam.indexOf(':'));
		return "(SELECT unnest(json_transform(" + p + "::JSON, " + literal(structure(columns)) + ")) AS r, unnest(" + p + "::JSON[]) AS doc) e";
	}

	private static String present(Column column) {
		return "json_exists(e.doc, " + literal(jsonPath(List.of(column.name()))) + ")";
	}

	/**
	 * INSERT of rows given as one JSON array parameter ($1): each element is read with json_transform into the column types; a
	 * column with a default takes it when the element does not name the column. Slice values are bound parameters. The policy
	 * is error, ignore (ON CONFLICT DO NOTHING) or replace (ON CONFLICT DO UPDATE of every written column).
	 */
	public static String insertRows(Query q, Table table, Expr rows, Map<String, Expr> slices, String conflict) {
		String rowsParam = q.bind(rows, "JSON");
		List<String> names = new ArrayList<>();
		List<String> selected = new ArrayList<>();
		List<Column> read = new ArrayList<>();
		for (Column column : table.columns()) {
			if (slices.containsKey(column.name())) continue;
			names.add(id(column.name()));
			read.add(column);
			String value = "e.r." + id(column.name());
			selected.add(column.defaultValue() == null ? value
					: "CASE WHEN " + present(column) + " THEN " + value + " ELSE " + constant(column.defaultValue(), column) + " END");
		}
		for (Map.Entry<String, Expr> slice : slices.entrySet()) {
			names.add(id(slice.getKey()));
			selected.add(q.bind(slice.getValue(), "VARCHAR"));
		}
		return "INSERT INTO " + table(table) + " (" + String.join(", ", names) + ") SELECT " + String.join(", ", selected) + " FROM "
				+ records(rowsParam, read) + conflictClause(table, names, conflict);
	}

	/** The ON CONFLICT clause of a policy: error (none), ignore (DO NOTHING) or replace (DO UPDATE of every written column). */
	private static String conflictClause(Table table, List<String> names, String conflict) {
		String target = id(table.key());
		return switch (conflict) {
		case "error" -> "";
		case "ignore" -> " ON CONFLICT (" + target + ") DO NOTHING";
		default -> {
			List<String> sets = new ArrayList<>();
			for (String name : names) {
				if (!name.equals(target)) sets.add(name + " = excluded." + name);
			}
			yield " ON CONFLICT (" + target + ") DO " + (sets.isEmpty() ? "NOTHING" : "UPDATE SET " + String.join(", ", sets));
		}
		};
	}

	/** Why the generator reads local files only. */
	public static final String NO_REMOTE = "generated DuckDB programs read local files; remote locations go through the httpfs extension, which DuckDB "
			+ "downloads on first use and which reads S3, GCS and Azure storage with a secret holding credentials (CREATE SECRET), and the generated "
			+ "programs install no extension at run time beyond vss and fts and create no secrets";

	/** The local path of a file:// location or a plain path; other schemes are unsupported. */
	public static String localPath(String location) {
		int scheme = location.indexOf("://");
		if (scheme < 0) return location;
		String name = location.substring(0, scheme);
		if (!name.equals("file")) throw Unsupported.of("reading from " + name + "; " + NO_REMOTE);
		return location.substring(scheme + 3);
	}

	/**
	 * The core reader of a local file: read_parquet, read_csv or read_json, by the declared format, else by the file suffix
	 * (.parquet, .csv, .json, .jsonl, .ndjson).
	 */
	public static String reader(String location, String format) {
		String path = localPath(location).toLowerCase(Locale.ROOT);
		String kind = format != null ? format
				: path.endsWith(".parquet") ? "parquet"
						: path.endsWith(".csv") ? "csv" : path.endsWith(".json") || path.endsWith(".jsonl") || path.endsWith(".ndjson") ? "json" : null;
		if (kind == null) throw Unsupported.of("the file " + location + " without a format; the generator reads .parquet, .csv, .json, .jsonl and .ndjson files");
		return switch (kind) {
		case "parquet" -> "read_parquet";
		case "csv" -> "read_csv";
		case "json" -> "read_json";
		default -> throw Unsupported.of("the file format " + kind + "; DuckDB's core readers are read_parquet, read_csv and read_json (lance, iceberg, "
				+ "vortex and numpy files need readers the generated programs do not load)");
		};
	}

	/**
	 * INSERT of the rows a core reader reads from a local file ($1, the path), the declared columns selected by name; slice values
	 * are bound parameters; the policy as for insertRows.
	 */
	public static String insertFromFile(Query q, Table table, String reader, String path, Map<String, Expr> slices, String conflict) {
		String file = q.bind(Code.text(path), "VARCHAR");
		List<String> names = new ArrayList<>();
		List<String> selected = new ArrayList<>();
		for (Column column : table.columns()) {
			if (slices.containsKey(column.name())) continue;
			names.add(id(column.name()));
			selected.add("f." + id(column.name()));
		}
		for (Map.Entry<String, Expr> slice : slices.entrySet()) {
			names.add(id(slice.getKey()));
			selected.add(q.bind(slice.getValue(), "VARCHAR"));
		}
		return "INSERT INTO " + table(table) + " (" + String.join(", ", names) + ") SELECT " + String.join(", ", selected) + " FROM " + reader + "("
				+ file + ") f" + conflictClause(table, names, conflict);
	}

	/**
	 * UPDATE ... FROM the records of a JSON parameter, matched by key: merge sets the columns a record carries, replace also
	 * resets the record's other columns that are not vectors to their defaults; a record without a stored row changes nothing.
	 */
	public static String updateRows(Query q, Table table, Expr rows, boolean replace, String where) {
		String rowsParam = q.bind(rows, "JSON");
		List<String> sets = new ArrayList<>();
		for (Column column : table.columns()) {
			if (column.name().equals(table.key())) continue;
			String kept = replace && column.vec() == null ? (column.defaultValue() == null ? "NULL" : constant(column.defaultValue(), column)) : col(column.name());
			sets.add(id(column.name()) + " = CASE WHEN " + present(column) + " THEN e.r." + id(column.name()) + " ELSE " + kept + " END");
		}
		String condition = and(List.of(col(table.key()) + " = e.r." + id(table.key()), where == null ? "" : where));
		return "UPDATE " + table(table) + " AS " + ALIAS + " SET " + String.join(", ", sets) + " FROM " + records(rowsParam, table.columns())
				+ whereClause(condition);
	}

	/** The key condition of ids: list_contains(..., t."id"). */
	public static String keyIn(Query q, Table table, Code.Items ids) {
		return "list_contains(" + q.array(ids, table.keyColumn().paramType()) + ", " + col(table.key()) + ")";
	}

	// ---------------------------------------------------------------- reads

	/** Settings applied with SET before the statement and RESET after it (DuckDB has no SET LOCAL). */
	public static Code.Entries settings(Map<String, String> values) {
		Code.Entries result = Code.entries();
		values.forEach((name, value) -> result.put(name, Code.text(value)));
		return result;
	}

	/** Join a list of SQL parts that may be null. */
	public static String and(List<String> parts) {
		List<String> present = parts.stream().filter(p -> p != null && !p.isBlank()).toList();
		if (present.isEmpty()) return null;
		return present.size() == 1 ? present.get(0) : present.stream().map(p -> "(" + p + ")").collect(Collectors.joining(" AND "));
	}

	public static String whereClause(String condition) {
		return condition == null ? "" : " WHERE " + condition;
	}
}
