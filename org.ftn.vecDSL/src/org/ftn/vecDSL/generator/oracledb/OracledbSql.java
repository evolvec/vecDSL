package org.ftn.vecDSL.generator.oracledb;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * SQL text for Oracle AI Database 26ai. Every statement is built when the program is generated, so Python, TypeScript and Java
 * send the same text; values are bound as positional bind variables (:1, :2, ... in textual order, each used once), never written
 * into the SQL. Lists and objects travel as JSON text (bound as CLOB by the runtimes) and are read with JSON_TABLE, JSON_VALUE or
 * TO_VECTOR. Queries return one JSON object per row (JSON_OBJECT ... RETURNING CLOB), which the runtimes decode the same way.
 * Identifiers are always quoted, so vecDSL names keep their case and may be Oracle reserved words.
 */
public final class OracledbSql {

	/** Suffix of constructs Oracle supports that the generator does not produce yet. */
	public static final String PENDING = "has no Oracle mapping yet";
	private static final int NAME_LIMIT = 128;
	public static final String ALIAS = "t";

	/** Oracle Text reserved words; a query term equal to one is escaped with braces (Text Reference, reserved words). */
	public static final Set<String> TEXT_RESERVED = Set.of("ABOUT", "ACCUM", "AND", "BT", "BTG", "BTI", "BTP", "EQUIV", "FUZZY",
			"HASPATH", "INPATH", "MDATA", "MINUS", "NEAR", "NOT", "NT", "NTG", "NTI", "NTP", "OR", "PT", "RT", "SQE", "SYN", "TR",
			"TRSYN", "TT", "WITHIN");

	private OracledbSql() {
	}

	// ---------------------------------------------------------------- descriptors

	/**
	 * A vector column: dimension format (FLOAT32, FLOAT64, INT8, BINARY), dimension (0 if flexible), sparse storage, metric and
	 * the index family that serves approximate searches on it (null without a vector index).
	 */
	public record Vec(String format, int dim, boolean sparse, String metric, String family) {

		/** The column type: VECTOR(4, FLOAT32), VECTOR(*, FLOAT32, SPARSE). */
		public String sql() {
			return "VECTOR(" + (dim > 0 ? Integer.toString(dim) : "*") + ", " + format + (sparse ? ", SPARSE" : "") + ")";
		}

		/** The bound value as a vector: TO_VECTOR(:n, 4, FLOAT32). */
		public String of(String bind) {
			return "TO_VECTOR(" + bind + ", " + (dim > 0 ? Integer.toString(dim) : "*") + ", " + format + (sparse ? ", SPARSE" : "") + ")";
		}

		public Vec withMetric(String newMetric) {
			return new Vec(format, dim, sparse, newMetric, family);
		}

		public Vec withFamily(String newFamily) {
			return new Vec(format, dim, sparse, metric, newFamily);
		}
	}

	/**
	 * A table column: name, SQL type, element kind of an array (arrays are JSON columns), json for JSON columns, the vector
	 * descriptor, the Oracle Text lexer of a full-text column ("" for the default lexer), nullability, declared default and CHECK
	 * conditions. The kind of a scalar decides how a bound value is converted: number, string, boolean, date, timestamp,
	 * timestamptz.
	 */
	public record Column(String name, String type, String element, boolean json, Vec vec, String lexer, boolean nullable,
			Expr defaultValue, List<String> checks) {

		public boolean array() {
			return element != null;
		}

		public boolean text() {
			return lexer != null;
		}

		public String kind() {
			return kindOf(type);
		}
	}

	/** The value kind of an SQL type. */
	public static String kindOf(String type) {
		if (type.startsWith("NUMBER") || type.startsWith("BINARY_")) return "number";
		if (type.startsWith("VARCHAR2")) return "string";
		return switch (type) {
		case "BOOLEAN" -> "boolean";
		case "DATE" -> "date";
		case "TIMESTAMP" -> "timestamp";
		case "TIMESTAMP WITH TIME ZONE" -> "timestamptz";
		case "JSON" -> "json";
		default -> "other";
		};
	}

	/**
	 * A collection as a table: the logical database (null for the program's schema), the physical table name (prefixed with the
	 * logical database), the vecDSL name, the key column, columns in declaration order, the hidden columns of tenant, namespace
	 * and partition names (null when absent), and field renames done by earlier alters.
	 */
	public record Table(String database, String name, String key, List<Column> columns, String tenant, String namespace,
			String partition, Map<String, String> renames, Flashback flashback) {

		public Table(String database, String name, String key, List<Column> columns, String tenant, String namespace, String partition,
				Map<String, String> renames) {
			this(database, name, key, columns, tenant, namespace, partition, renames, null);
		}

		/** The table as a read sees it at a past time or SCN (flashback query). */
		public Table at(Flashback past) {
			return new Table(database, name, key, columns, tenant, namespace, partition, renames, past);
		}

		public Column column(String field) {
			String column = renames.getOrDefault(field, field);
			for (Column c : columns) {
				if (c.name().equals(column)) return c;
			}
			if (column.equals(tenant) || column.equals(namespace) || column.equals(partition)) {
				return new Column(column, "VARCHAR2(512)", null, false, null, null, true, null, List.of());
			}
			throw Unsupported.of("field " + column + ", which is not a field of collection " + name);
		}

		public Table with(Column added) {
			List<Column> result = new ArrayList<>(columns);
			result.add(added);
			return new Table(database, name, key, List.copyOf(result), tenant, namespace, partition, renames, flashback);
		}

		public Table without(String field) {
			String column = column(field).name();
			return new Table(database, name, key, columns.stream().filter(c -> !c.name().equals(column)).toList(), tenant, namespace,
					partition, renames, flashback);
		}

		public Table renamed(String field, String newName) {
			Column old = column(field);
			Column next = new Column(newName, old.type(), old.element(), old.json(), old.vec(), old.lexer(), old.nullable(),
					old.defaultValue(), old.checks());
			Map<String, String> names = new LinkedHashMap<>(renames);
			names.put(field, newName);
			return new Table(database, name, key, columns.stream().map(c -> c.name().equals(old.name()) ? next : c).toList(), tenant,
					namespace, partition, Map.copyOf(names), flashback);
		}

		public Table named(String newName) {
			return new Table(database, newName, key, columns, tenant, namespace, partition, renames, flashback);
		}

		public Column keyColumn() {
			return column(key);
		}

		public List<String> sliceColumns() {
			List<String> result = new ArrayList<>();
			for (String slice : new String[] { tenant, namespace, partition }) {
				if (slice != null && !result.contains(slice) && columns.stream().noneMatch(c -> c.name().equals(slice))) result.add(slice);
			}
			return result;
		}
	}

	/**
	 * The past state a read sees (SQL Language Reference, SELECT, flashback_query_clause; Development Guide, Using Oracle Flashback
	 * Query): AS OF SCN for a number, AS OF TIMESTAMP for a string, an ISO 8601 timestamp with offset.
	 */
	public record Flashback(Expr value, boolean scn) {

		/** The clause with the value bound, for queries. */
		public String clause(Query q) {
			return scn ? " AS OF SCN " + q.bind(value, "number") : " AS OF TIMESTAMP " + q.bind(value, "timestamptz");
		}

		/** The clause with the value written as a constant, for CREATE TABLE ... AS SELECT, which takes no bind variables. */
		public String constant() {
			if (scn && value instanceof Code.Num num) return " AS OF SCN " + number(num.value());
			if (!scn && value instanceof Code.Text text) {
				return " AS OF TIMESTAMP TO_TIMESTAMP_TZ(" + literal(text.value()) + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FFTZH:TZM')";
			}
			throw Unsupported.of("a parameter in the 'as-of' clause of a clone; CREATE TABLE ... AS SELECT is DDL, which takes no bind variables");
		}
	}

	/** A filter condition: conjunction, disjunction, negation, a test on one column (a JSON path below it when given), or a slice. */
	public sealed interface Cond permits And, Or, Not, Test, Raw {
	}

	public record And(List<Cond> parts) implements Cond {
	}

	public record Or(List<Cond> parts) implements Cond {
	}

	public record Not(Cond operand) implements Cond {
	}

	/**
	 * A test: column, JSON path below a JSON column (empty otherwise) with the kind of the leaf, operator, value and the upper
	 * bound of 'between'. Operators as in the other SQL target: eq, neq, gt, gte, lt, lte, in, not_in, like, prefix, contains,
	 * contains_any, contains_all, between, is_null, is_not_null, exists, not_exists, is_empty, length_(op), match,
	 * match_all_terms, match_any_term, phrase, regex, contains_text, wildcard.
	 */
	public record Test(String column, List<String> path, String pathKind, String op, Expr value, Expr upper) implements Cond {
	}

	/** A condition on a slice column with fixed values: tenant, namespace or partitions. */
	public record Raw(String column, List<Expr> values) implements Cond {
	}

	/** A statement under construction: positional bind variables in textual order. */
	public static final class Query {

		private final List<Expr> params = new ArrayList<>();

		/** A placeholder :n for the value, converted to the kind: TO_DATE for dates, plain otherwise. */
		public String bind(Expr value, String kind) {
			params.add(value);
			String p = ":" + params.size();
			return switch (kind) {
			case "date" -> "TO_DATE(" + p + ", 'YYYY-MM-DD')";
			case "timestamp" -> "TO_TIMESTAMP(" + p + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FF')";
			case "timestamptz" -> "TO_TIMESTAMP_TZ(" + p + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FFTZH:TZM')";
			default -> p;
			};
		}

		/** A vector placeholder: TO_VECTOR(:n, dim, format). */
		public String vector(Expr value, Vec vec) {
			params.add(value);
			return vec.of(":" + params.size());
		}

		/** A list value bound as JSON text, read as rows of v. */
		public String list(Expr value, String kind) {
			params.add(value);
			return "SELECT v FROM JSON_TABLE(" + ":" + params.size() + ", '$[*]' COLUMNS (v " + jsonTableType(kind) + " PATH '$'))";
		}

		/** A JSON document bound as text. */
		public String json(Expr value) {
			params.add(value);
			return ":" + params.size();
		}

		public Code.Items params() {
			return Code.items(params);
		}

		public int size() {
			return params.size();
		}
	}

	/** SQL text with its parameters in the order of their placeholders. */
	public record Sql(String text, Code.Items params) {
	}

	/**
	 * Renumbers the placeholders of a statement in textual order and orders its parameters to match, because the three drivers
	 * bind a parameter list by position (the order of appearance), not by the number in the placeholder. Quoted literals and
	 * identifiers are skipped.
	 */
	public static Sql finish(String text, Code.Items params) {
		StringBuilder out = new StringBuilder();
		List<Expr> ordered = new ArrayList<>();
		int i = 0;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (c == '\'' || c == '"') {
				int end = i + 1;
				while (end < text.length()) {
					if (text.charAt(end) == c) {
						if (c == '\'' && end + 1 < text.length() && text.charAt(end + 1) == '\'') {
							end += 2;
							continue;
						}
						break;
					}
					end++;
				}
				out.append(text, i, Math.min(end + 1, text.length()));
				i = end + 1;
			} else if (c == ':' && i + 1 < text.length() && Character.isDigit(text.charAt(i + 1))) {
				int end = i + 1;
				while (end < text.length() && Character.isDigit(text.charAt(end))) end++;
				ordered.add(params.items().get(Integer.parseInt(text.substring(i + 1, end)) - 1));
				out.append(':').append(ordered.size());
				i = end;
			} else {
				out.append(c);
				i++;
			}
		}
		if (ordered.size() != params.items().size()) {
			throw new IllegalStateException("a bind variable is missing or repeated in " + text);
		}
		return new Sql(out.toString(), Code.items(ordered));
	}

	/** The JSON_TABLE column type that reads a value of the kind. */
	static String jsonTableType(String kind) {
		return switch (kind) {
		case "number" -> "NUMBER";
		case "boolean" -> "BOOLEAN";
		default -> "VARCHAR2(4000)";
		};
	}

	// ---------------------------------------------------------------- names and literals

	/** A quoted identifier: 1 to 128 bytes, no double quote or NUL, never the reserved column name ROWID. */
	public static String id(String name) {
		if (name.isEmpty() || name.indexOf('"') >= 0 || name.indexOf('\0') >= 0) {
			throw Unsupported.of("the name '" + name + "'; an Oracle quoted identifier cannot be empty or contain a double quotation mark or NUL");
		}
		if (name.getBytes(StandardCharsets.UTF_8).length > NAME_LIMIT) {
			throw Unsupported.of("the name '" + name + "'; Oracle names have at most 128 bytes");
		}
		if (name.equals("ROWID")) throw Unsupported.of("the name ROWID; Oracle reserves it as a column name even when quoted");
		return "\"" + name + "\"";
	}

	/** A column of the query's table alias: t."price". */
	public static String col(String name) {
		return ALIAS + "." + id(name);
	}

	/** A string constant for DDL, where Oracle takes no bind variables (defaults, comments, index parameters). */
	public static String literal(String value) {
		if (value.indexOf('\0') >= 0) throw Unsupported.of("a string with a NUL character");
		return "'" + value.replace("'", "''") + "'";
	}

	public static String table(Table table) {
		return id(table.name());
	}

	/** The table of a read, with its flashback clause when the read is as of a past time. */
	public static String source(Query q, Table table) {
		return table.flashback() == null ? table(table) : table(table) + table.flashback().clause(q);
	}

	static String number(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	/** JSON text of a literal value, for DDL constants and JSON column defaults. */
	static String json(Expr value) {
		return switch (value) {
		case Code.Text text -> Syntax.quote(text.value());
		case Code.Num num -> number(num.value());
		case Code.Flag flag -> Boolean.toString(flag.value());
		case Code.Nil nil -> "null";
		case Code.Items items -> items.items().stream().map(OracledbSql::json).collect(Collectors.joining(",", "[", "]"));
		case Code.Entries entries -> entries.entries().entrySet().stream().map(e -> Syntax.quote(e.getKey()) + ":" + json(e.getValue()))
				.collect(Collectors.joining(",", "{", "}"));
		default -> throw Unsupported.of("a parameter reference in a DDL constant; Oracle DDL takes no bind variables");
		};
	}

	/** A DDL constant for a vecDSL literal default of a column. */
	public static String constant(Expr value, Column column) {
		if (value instanceof Code.Nil) return "NULL";
		if (column.json()) return literal(json(value));
		return switch (value) {
		case Code.Text text -> switch (column.kind()) {
			case "date" -> "DATE " + literal(text.value());
			case "timestamp", "timestamptz" -> "TO_TIMESTAMP_TZ(" + literal(text.value()) + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FFTZH:TZM')";
			default -> literal(text.value());
		};
		case Code.Num num -> number(num.value());
		case Code.Flag flag -> flag.value() ? "TRUE" : "FALSE";
		default -> throw Unsupported.of("a default value of this kind for column " + column.name()
				+ "; defaults are string, number, boolean, null and, for JSON fields, list literals");
		};
	}

	// ---------------------------------------------------------------- metrics

	/** The VECTOR_DISTANCE metric of a vecDSL metric for a vector format (SQL Language Reference, VECTOR_DISTANCE). */
	public static String metric(Vec vec, String metric) {
		boolean binary = vec.format().equals("BINARY");
		String result = switch (metric) {
		case "cosine" -> "COSINE";
		case "dot" -> "DOT";
		case "euclidean" -> "EUCLIDEAN";
		case "manhattan" -> "MANHATTAN";
		case "hamming" -> "HAMMING";
		case "jaccard" -> binary ? "JACCARD" : null;
		default -> null;
		};
		if (result == null) throw Unsupported.of(metric + " metric on " + vec.format() + " fields; JACCARD needs BINARY vectors");
		return result;
	}

	/** VECTOR_DISTANCE of the column and a query vector expression. */
	public static String distance(String column, String query, Vec vec, String metric) {
		return "VECTOR_DISTANCE(" + column + ", " + query + ", " + metric(vec, metric) + ")";
	}

	/**
	 * Canonical similarity of an Oracle distance d: cosine 1 - d (COSINE is the cosine distance), dot -d (DOT is the
	 * negated dot product), the distances -d (EUCLIDEAN is not squared), jaccard 1 - d. The same forms as the pgvector target.
	 */
	public static String similarity(String metric, String distance) {
		return switch (metric) {
		case "cosine", "jaccard" -> "1 - " + distance;
		default -> "-" + distance;
		};
	}

	// ---------------------------------------------------------------- DDL

	/** CREATE TABLE with columns, defaults, CHECK limits and the primary key; automatic list partitions by the partition column. */
	public static String createTable(Table table, List<String> partitions, boolean ifNotExists) {
		List<String> parts = new ArrayList<>();
		for (Column column : table.columns()) parts.add(columnDefinition(column, column.name().equals(table.key())));
		for (String slice : table.sliceColumns()) {
			parts.add(id(slice) + " VARCHAR2(512)" + (slice.equals(table.partition()) ? " NOT NULL" : ""));
		}
		parts.add("PRIMARY KEY (" + id(table.key()) + ")");
		StringBuilder sql = new StringBuilder("CREATE TABLE ").append(ifNotExists ? "IF NOT EXISTS " : "").append(table(table)).append(" (")
				.append(String.join(", ", parts)).append(")");
		if (table.partition() != null) {
			sql.append(" PARTITION BY LIST (").append(id(table.partition())).append(") AUTOMATIC (")
					.append(partitions.stream().map(p -> partitionClause(p)).collect(Collectors.joining(", "))).append(")");
		}
		return sql.toString();
	}

	/** One partition of the list: named like the vecDSL partition, holding its rows. */
	public static String partitionClause(String partition) {
		return "PARTITION " + id(partition) + " VALUES (" + literal(partition) + ")";
	}

	/** One column of CREATE TABLE or ALTER TABLE ADD. */
	public static String columnDefinition(Column column, boolean key) {
		StringBuilder out = new StringBuilder(id(column.name())).append(' ').append(column.vec() != null ? column.vec().sql() : column.type());
		if (column.defaultValue() != null) out.append(" DEFAULT ").append(constant(column.defaultValue(), column));
		if (!column.nullable() && !key) out.append(" NOT NULL");
		for (String check : column.checks()) out.append(' ').append(check.replace("{}", id(column.name())));
		return out.toString();
	}

	/**
	 * A vector index (SQL Language Reference, CREATE VECTOR INDEX): hnsw as INMEMORY NEIGHBOR GRAPH with NEIGHBORS and
	 * EFCONSTRUCTION, ivf as NEIGHBOR PARTITIONS with the partition count; the distance is the field's metric. A scalar-quantized
	 * HNSW index takes QUANTIZATION SCALAR COMPRESSION RATIO before PARAMETERS (AI Vector Search User's Guide, HNSW Index Syntax
	 * and Parameters).
	 */
	public static String createVectorIndex(Table table, String name, Column column, String family, Map<String, Integer> options,
			Integer accuracy, Integer compression) {
		String metric = metric(column.vec(), column.vec().metric());
		if (metric.equals("JACCARD")) throw Unsupported.of("a vector index with the jaccard metric; Oracle vector indexes measure EUCLIDEAN, EUCLIDEAN_SQUARED, COSINE, DOT, MANHATTAN and HAMMING");
		String organization = family.equals("ivf") ? "NEIGHBOR PARTITIONS" : "INMEMORY NEIGHBOR GRAPH";
		StringBuilder sql = new StringBuilder("CREATE VECTOR INDEX ").append(id(name)).append(" ON ").append(table(table)).append(" (")
				.append(id(column.name())).append(") ORGANIZATION ").append(organization).append(" DISTANCE ").append(metric);
		if (accuracy != null) sql.append(" WITH TARGET ACCURACY ").append(accuracy);
		if (!options.isEmpty() && accuracy == null) sql.append(" WITH TARGET ACCURACY 90");
		if (compression != null) sql.append(" QUANTIZATION SCALAR COMPRESSION RATIO ").append(compression);
		if (!options.isEmpty()) {
			sql.append(" PARAMETERS (TYPE ").append(family.equals("ivf") ? "IVF" : "HNSW");
			options.forEach((key, value) -> sql.append(", ").append(key).append(' ').append(value));
			sql.append(")");
		}
		return sql.toString();
	}

	/**
	 * A payload index: B-tree on a scalar column, a function-based index on a typed JSON_VALUE below a JSON column, a JSON search
	 * index on an array or JSON column, or an Oracle Text CONTEXT index synchronized on commit on a full-text column.
	 */
	public static String createPayloadIndex(Table table, String name, Column column, List<String> path, String pathKind, String structure) {
		String target = id(column.name());
		if ("text".equals(structure)) {
			String parameters = (column.lexer() == null || column.lexer().isEmpty() ? "" : "LEXER " + column.lexer() + " ") + "SYNC (ON COMMIT)";
			return "CREATE INDEX " + id(name) + " ON " + table(table) + " (" + target + ") INDEXTYPE IS CTXSYS.CONTEXT PARAMETERS ("
					+ literal(parameters) + ")";
		}
		if (!path.isEmpty()) {
			return "CREATE INDEX " + id(name) + " ON " + table(table) + " (" + jsonValue(target, path, pathKind) + ")";
		}
		if (column.array() || column.json()) {
			return "CREATE SEARCH INDEX " + id(name) + " ON " + table(table) + " (" + target + ") FOR JSON";
		}
		return "CREATE INDEX " + id(name) + " ON " + table(table) + " (" + target + ")";
	}

	/** A JSON path literal of member names: '$."a"."b"'. */
	static String pathText(List<String> path) {
		StringBuilder out = new StringBuilder("$");
		for (String segment : path) out.append('.').append(Syntax.quote(segment));
		return literal(out.toString());
	}

	/** JSON_VALUE of a path below a JSON column, returning the SQL type of the leaf kind. */
	public static String jsonValue(String ref, List<String> path, String kind) {
		return "JSON_VALUE(" + ref + ", " + pathText(path) + " RETURNING " + switch (kind) {
		case "number" -> "NUMBER";
		case "boolean" -> "BOOLEAN";
		case "date" -> "DATE";
		case "timestamp", "timestamptz" -> "TIMESTAMP";
		default -> "VARCHAR2(4000)";
		} + ")";
	}

	/** A column under a table alias, or a sub-field read with JSON_VALUE. */
	public static String reference(String alias, Column column, List<String> path, String pathKind) {
		String ref = alias + "." + id(column.name());
		return path.isEmpty() ? ref : jsonValue(ref, path, pathKind);
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
		case Not not -> notTrue(condition(q, table, not.operand()));
		case Raw raw -> raw.values().size() == 1 ? col(raw.column()) + " = " + q.bind(raw.values().get(0), "string")
				: col(raw.column()) + " IN (" + q.list(Code.items(raw.values()), "string") + ")";
		case Test test -> test(q, table, test);
		};
	}

	private static final Set<String> ORDERED = Set.of("number", "string", "date", "timestamp", "timestamptz");

	private static String test(Query q, Table table, Test test) {
		Column column = table.column(test.column());
		boolean nested = !test.path().isEmpty();
		String kind = nested ? test.pathKind() : column.kind();
		String ref = nested ? jsonValue(col(column.name()), test.path(), kind) : col(column.name());
		boolean array = !nested && column.array();
		String op = test.op();
		if (op.startsWith("length_")) {
			String length = array ? "JSON_VALUE(" + ref + ", '$.size()' RETURNING NUMBER)" : "LENGTH(" + ref + ")";
			return length + " " + comparison(op.substring(7)) + " " + q.bind(test.value(), "number");
		}
		switch (op) {
		case "is_null", "not_exists":
			return ref + " IS NULL";
		case "is_not_null", "exists":
			return ref + " IS NOT NULL";
		case "is_empty":
			return array ? "JSON_VALUE(" + ref + ", '$.size()' RETURNING NUMBER) = 0" : "(" + ref + " IS NULL)";
		default:
			break;
		}
		if (column.vec() != null) throw Unsupported.of("filters on vector field " + column.name());
		if (column.json() && !nested && !array) throw Unsupported.of("comparisons on the whole json field " + column.name() + "; filter on its sub-fields");
		String element = array ? column.element() : kind;
		switch (op) {
		case "eq":
			if (array) return "JSON_EQUAL(" + ref + ", JSON(" + q.json(test.value()) + "))";
			return ref + " = " + q.bind(test.value(), kind);
		case "neq":
			if (array) return notTrue("JSON_EQUAL(" + ref + ", JSON(" + q.json(test.value()) + "))");
			return "DECODE(" + ref + ", " + q.bind(test.value(), kind) + ", 1, 0) = 0";
		case "gt", "gte", "lt", "lte":
			ordered(column, kind, array);
			return ref + " " + comparison(op) + " " + q.bind(test.value(), kind);
		case "between":
			ordered(column, kind, array);
			return ref + " BETWEEN " + q.bind(test.value(), kind) + " AND " + q.bind(test.upper(), kind);
		case "in":
			scalar(column, array, op);
			return ref + " IN (" + q.list(test.value(), kind) + ")";
		case "not_in":
			scalar(column, array, op);
			return notTrue(ref + " IN (" + q.list(test.value(), kind) + ")");
		case "contains":
			if (array) return "JSON_EXISTS(" + ref + ", '$[*]?(@ == $v)' PASSING " + q.bind(test.value(), element) + " AS \"v\")";
			return "INSTR(" + ref + ", " + q.bind(test.value(), "string") + ") > 0";
		case "contains_any":
			arrayOnly(column, array, op);
			return "EXISTS (SELECT 1 FROM JSON_TABLE(" + ref + ", '$[*]' COLUMNS (e " + jsonTableType(element) + " PATH '$')) x WHERE x.e IN ("
					+ q.list(test.value(), element) + "))";
		case "contains_all":
			arrayOnly(column, array, op);
			return "NOT EXISTS (" + q.list(test.value(), element) + " MINUS SELECT x.e FROM JSON_TABLE(" + ref + ", '$[*]' COLUMNS (e "
					+ jsonTableType(element) + " PATH '$')) x)";
		case "like":
			return ref + " LIKE " + q.bind(test.value(), "string");
		case "prefix":
			return "SUBSTR(" + ref + ", 1, LENGTH(" + q.bind(test.value(), "string") + ")) = " + q.bind(test.value(), "string");
		case "contains_text":
			return "INSTR(" + ref + ", " + q.bind(test.value(), "string") + ") > 0";
		case "regex":
			return "REGEXP_LIKE(" + ref + ", " + q.bind(test.value(), "string") + ")";
		case "wildcard":
			return ref + " LIKE " + q.bind(Code.runtime("wildcard_pattern", args("pattern", test.value())), "string") + " ESCAPE '\\'";
		case "match", "match_all_terms", "match_any_term", "phrase":
			if (nested || !column.text()) {
				throw Unsupported.of(op + " on " + column.name() + ", which has no Oracle Text index; CONTAINS needs a CONTEXT index on the column");
			}
			return "CONTAINS(" + ref + ", " + q.bind(textQuery(test.value(), op, column), "string") + ") > 0";
		default:
			throw new IllegalArgumentException(op);
		}
	}

	/**
	 * The Oracle Text query of a keyword input, built by the runtime function text_query: the input's words (letters, digits and
	 * underscores only, so no query operator or special character of the input reaches CONTAINS), joined with AND (all terms),
	 * ACCUM (any term, ranked by how many match) or as one phrase in braces; reserved words are escaped with braces, and a column
	 * with a stemming lexer stems each term ($).
	 */
	public static Expr textQuery(Expr text, String op, Column column) {
		return textQuery(text, op, column, false, null);
	}

	/**
	 * The text query with fuzzy expansion of each word (Text Reference, FUZZY, with its default similarity score and expansion
	 * count) or, for a phrase, the words within span words of each other in order (Text Reference, NEAR, max_span and order).
	 */
	public static Expr textQuery(Expr text, String op, Column column, boolean fuzzy, Integer span) {
		String mode = switch (op) {
		case "phrase" -> "phrase";
		case "match_any_term" -> "any";
		default -> "all";
		};
		Code.Entries args = args("text", text, "mode", Code.text(mode));
		if (column.lexer() != null && !column.lexer().isEmpty()) args.put("stem", Code.flag(true));
		if (fuzzy) args.put("fuzzy", Code.flag(true));
		if (span != null) args.put("span", Code.num(span));
		return Code.runtime("text_query", args);
	}

	private static Code.Entries args(Object... pairs) {
		Code.Entries result = Code.entries();
		for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], (Expr) pairs[i + 1]);
		return result;
	}

	private static void ordered(Column column, String kind, boolean array) {
		if (array || !ORDERED.contains(kind)) {
			throw Unsupported.of("ordered comparisons on " + column.name() + " of type " + (array ? "array" : column.type()));
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

	/** A value written into a column: vectors through TO_VECTOR, JSON values as JSON text, scalars by kind. */
	public static String value(Query q, Column column, Expr value) {
		if (column.vec() != null) return q.vector(value, column.vec());
		if (column.json()) return "JSON(" + q.json(jsonValue(value)) + ")";
		return q.bind(value, column.kind());
	}

	/** A JSON field value bound as JSON text, so that a string stays a JSON string. */
	static Expr jsonValue(Expr value) {
		return value instanceof Code.Text text ? new Code.Text(Syntax.quote(text.value())) : value;
	}

	/** The output expression of a column in a result object: vectors as JSON arrays (FROM_VECTOR), JSON as JSON. */
	public static String output(Column column, String alias) {
		String ref = alias + "." + id(column.name());
		if (column.vec() != null) return "FROM_VECTOR(" + ref + " RETURNING CLOB FORMAT DENSE) FORMAT JSON";
		return ref;
	}

	/** JSON_OBJECT over name and expression pairs, in order, with nulls kept. */
	public static String object(Map<String, String> fields) {
		return "JSON_OBJECT(" + fields.entrySet().stream().map(e -> literal(e.getKey()) + " VALUE " + e.getValue())
				.collect(Collectors.joining(", ")) + " NULL ON NULL RETURNING CLOB)";
	}

	/** The record fields of rows: the key under its own name, then the output columns. */
	public static Map<String, String> fields(Table table, List<String> output, String alias) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(table.key(), output(table.keyColumn(), alias));
		for (String name : output) result.put(name, output(table.column(name), alias));
		return result;
	}

	// ---------------------------------------------------------------- writes

	/** The JSON_TABLE column that reads a column's value from a record (vectors and JSON values as JSON text). */
	private static String recordColumn(Column column, String alias) {
		String path = literal("$." + Syntax.quote(column.name()));
		if (column.vec() != null || column.json()) return alias + " CLOB FORMAT JSON PATH " + path;
		return alias + " " + switch (column.kind()) {
		case "number" -> "NUMBER";
		case "boolean" -> "VARCHAR2(5)";
		default -> "VARCHAR2(4000)";
		} + " PATH " + path;
	}

	/** The value of a record column in the column's type. */
	private static String recordValue(Column column, String ref) {
		if (column.vec() != null) return column.vec().of(ref);
		if (column.json()) return "JSON(" + ref + ")";
		return switch (column.kind()) {
		case "boolean" -> "CASE " + ref + " WHEN 'true' THEN TRUE WHEN 'false' THEN FALSE END";
		case "date" -> "TO_DATE(SUBSTR(" + ref + ", 1, 10), 'YYYY-MM-DD')";
		case "timestamp" -> "TO_TIMESTAMP(" + ref + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FF')";
		case "timestamptz" -> "TO_TIMESTAMP_TZ(" + ref + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FFTZH:TZM')";
		default -> ref;
		};
	}

	/**
	 * The rows of a JSON array parameter as a query: JSON_TABLE reads each record's columns, a column with a default takes it
	 * when the record does not name the column, and the slice values are bound parameters.
	 */
	public static String records(Query q, Table table, Expr rows, Map<String, Expr> slices, List<String> names) {
		String rowsParam = q.json(rows);
		List<String> selected = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		int index = 0;
		for (Column column : table.columns()) {
			if (slices.containsKey(column.name())) continue;
			String alias = "c" + index;
			names.add(column.name());
			columns.add(recordColumn(column, alias));
			String read = recordValue(column, "r." + alias);
			if (column.defaultValue() == null) {
				selected.add(read + " AS " + id(column.name()));
			} else {
				columns.add("h" + index + " VARCHAR2(5) EXISTS PATH " + literal("$." + Syntax.quote(column.name())));
				selected.add("CASE WHEN r.h" + index + " = 'true' THEN " + read + " ELSE " + constant(column.defaultValue(), column) + " END AS "
						+ id(column.name()));
			}
			index++;
		}
		for (Map.Entry<String, Expr> slice : slices.entrySet()) {
			names.add(slice.getKey());
			selected.add(q.bind(slice.getValue(), "string") + " AS " + id(slice.getKey()));
		}
		return "SELECT " + String.join(", ", selected) + " FROM JSON_TABLE(" + rowsParam + ", '$[*]' COLUMNS (" + String.join(", ", columns)
				+ ")) r";
	}

	/**
	 * A write of rows given as one JSON array parameter: INSERT ... SELECT for the error policy, MERGE with only the insert
	 * branch for ignore, MERGE with both branches for update and replace (upsert). The runtime returns the affected row count.
	 */
	public static String insertRows(Query q, Table table, Expr rows, Map<String, Expr> slices, String conflict) {
		List<String> names = new ArrayList<>();
		String source = records(q, table, rows, slices, names);
		String columns = names.stream().map(OracledbSql::id).collect(Collectors.joining(", "));
		if (conflict.equals("error")) return "INSERT INTO " + table(table) + " (" + columns + ") " + source;
		String key = id(table.key());
		StringBuilder sql = new StringBuilder("MERGE INTO ").append(table(table)).append(" ").append(ALIAS).append(" USING (").append(source)
				.append(") s ON (").append(ALIAS).append(".").append(key).append(" = s.").append(key).append(")");
		if (!conflict.equals("ignore")) {
			List<String> sets = new ArrayList<>();
			for (String name : names) {
				if (!name.equals(table.key()) && !name.equals(table.partition())) sets.add(ALIAS + "." + id(name) + " = s." + id(name));
			}
			if (!sets.isEmpty()) sql.append(" WHEN MATCHED THEN UPDATE SET ").append(String.join(", ", sets));
		}
		sql.append(" WHEN NOT MATCHED THEN INSERT (").append(columns).append(") VALUES (")
				.append(names.stream().map(n -> "s." + id(n)).collect(Collectors.joining(", "))).append(")");
		return sql.toString();
	}

	/**
	 * MERGE of records matched by key into existing rows only: merge sets the columns a record carries, replace also resets the
	 * record's other non-vector columns to their defaults; a record without a stored row changes nothing.
	 */
	public static String updateRows(Query q, Table table, Expr rows, boolean replace, String where) {
		String rowsParam = q.json(rows);
		List<String> columns = new ArrayList<>();
		List<String> sets = new ArrayList<>();
		String key = id(table.key());
		int index = 0;
		for (Column column : table.columns()) {
			String alias = "c" + index;
			columns.add(recordColumn(column, alias));
			if (!column.name().equals(table.key())) {
				columns.add("h" + index + " VARCHAR2(5) EXISTS PATH " + literal("$." + Syntax.quote(column.name())));
				String kept = replace && column.vec() == null ? (column.defaultValue() == null ? "NULL" : constant(column.defaultValue(), column))
						: col(column.name());
				sets.add(col(column.name()) + " = CASE WHEN s.h" + index + " = 'true' THEN " + recordValue(column, "s." + alias) + " ELSE " + kept
						+ " END");
			}
			index++;
		}
		String keyAlias = "c" + table.columns().indexOf(table.keyColumn());
		return "MERGE INTO " + table(table) + " " + ALIAS + " USING (SELECT * FROM JSON_TABLE(" + rowsParam + ", '$[*]' COLUMNS ("
				+ String.join(", ", columns) + "))) s ON (" + col(table.key()) + " = " + recordValue(table.keyColumn(), "s." + keyAlias)
				+ ") WHEN MATCHED THEN UPDATE SET " + String.join(", ", sets) + (where == null ? "" : " WHERE " + where);
	}

	/** The key condition of ids: t."id" IN (rows of the bound JSON list). */
	public static String keyIn(Query q, Table table, Code.Items ids) {
		return col(table.key()) + " IN (" + q.list(ids, table.keyColumn().kind()) + ")";
	}

	/** A condition that holds when the given one is false or unknown (a null operand), as the other vecDSL targets treat a negation. */
	public static String notTrue(String condition) {
		return "CASE WHEN " + condition + " THEN 1 ELSE 0 END = 0";
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

	/** A PL/SQL block that runs one DDL statement and ignores the error code of an object that already exists or is missing. */
	public static String tolerant(String ddl, int error) {
		return "BEGIN EXECUTE IMMEDIATE " + literal(ddl) + "; EXCEPTION WHEN OTHERS THEN IF SQLCODE <> " + error + " THEN RAISE; END IF; END;";
	}
}
