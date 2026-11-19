package org.ftn.vecDSL.generator.pgvector;

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
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * SQL text for PostgreSQL with pgvector. Every statement is built when the program is generated, so Python, TypeScript and
 * Java send the same text; values are bound as numbered text parameters ($1, $2, ...) with explicit casts, never written
 * into the SQL. Queries return one json column per row, which the runtimes decode the same way. The Xtend mapping
 * translates vecDSL concepts into the descriptors below (Table, Column, Vec, Cond) and calls the builders.
 */
public final class PgvectorSql {

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
	private static final int NAME_LIMIT = 63;
	public static final String ALIAS = "t";

	/** Built-in text search configurations of PostgreSQL 18 (pg_ts_config of a fresh cluster). */
	public static final Set<String> CONFIGURATIONS = Set.of("arabic", "armenian", "basque", "catalan", "danish", "dutch", "english",
			"finnish", "french", "german", "greek", "hindi", "hungarian", "indonesian", "irish", "italian", "lithuanian", "nepali",
			"norwegian", "portuguese", "romanian", "russian", "serbian", "simple", "spanish", "swedish", "tamil", "turkish", "yiddish");

	private static final Map<String, String> LANGUAGES = Map.ofEntries(Map.entry("ar", "arabic"), Map.entry("hy", "armenian"),
			Map.entry("eu", "basque"), Map.entry("ca", "catalan"), Map.entry("da", "danish"), Map.entry("nl", "dutch"),
			Map.entry("en", "english"), Map.entry("fi", "finnish"), Map.entry("fr", "french"), Map.entry("de", "german"),
			Map.entry("el", "greek"), Map.entry("hi", "hindi"), Map.entry("hu", "hungarian"), Map.entry("id", "indonesian"),
			Map.entry("ga", "irish"), Map.entry("it", "italian"), Map.entry("lt", "lithuanian"), Map.entry("ne", "nepali"),
			Map.entry("no", "norwegian"), Map.entry("pt", "portuguese"), Map.entry("ro", "romanian"), Map.entry("ru", "russian"),
			Map.entry("sr", "serbian"), Map.entry("es", "spanish"), Map.entry("sv", "swedish"), Map.entry("ta", "tamil"),
			Map.entry("tr", "turkish"), Map.entry("yi", "yiddish"));

	private PgvectorSql() {
	}

	// ---------------------------------------------------------------- descriptors

	/** A vector column: pgvector type (vector, halfvec, bit, sparsevec), dimension (0 if open), metric and quantized index. */
	public record Vec(String type, int dim, String metric, String quantization, double oversampling) {

		/** The column type with its dimension: vector(4), sparsevec, bit(8). */
		public String sql() {
			return dim > 0 ? type + "(" + dim + ")" : type;
		}
	}

	/**
	 * A table column: name, SQL type (for arrays the array type), element type of an array, json for jsonb columns, the text
	 * search configuration of a full-text column, nullability, declared default and CHECK conditions.
	 */
	public record Column(String name, String type, String element, boolean json, Vec vec, String config, boolean nullable,
			Expr defaultValue, List<String> checks) {

		public boolean array() {
			return element != null;
		}

		/** The type a bound parameter is cast to: varchar(n) compares as text (an explicit cast to varchar(n) would truncate). */
		public String paramType() {
			if (vec != null) return vec.sql();
			return type.startsWith("varchar") ? "text" : type;
		}
	}

	/**
	 * A collection as a table: database (null for the program's), name, key column, columns in declaration order, and the
	 * columns that hold tenant, namespace and partition names (null when the collection has none).
	 */
	public record Table(String database, String name, String key, List<Column> columns, String tenant, String namespace,
			String partition, Map<String, String> renames) {

		/** The column of a field, by its declared name; a field renamed by an earlier alter resolves to its new column. */
		public Column column(String field) {
			String column = renames.getOrDefault(field, field);
			for (Column c : columns) {
				if (c.name().equals(column)) return c;
			}
			if (column.equals(tenant) || column.equals(namespace) || column.equals(partition)) {
				return new Column(column, "text", null, false, null, null, true, null, List.of());
			}
			throw Unsupported.of("field " + column + ", which is not a field of collection " + name);
		}

		/** The table after ALTER TABLE ADD COLUMN. */
		public Table with(Column added) {
			List<Column> result = new ArrayList<>(columns);
			result.add(added);
			return new Table(database, name, key, List.copyOf(result), tenant, namespace, partition, renames);
		}

		/** The table after ALTER TABLE DROP COLUMN of a field. */
		public Table without(String field) {
			String column = column(field).name();
			return new Table(database, name, key, columns.stream().filter(c -> !c.name().equals(column)).toList(), tenant, namespace,
					partition, renames);
		}

		/** The table after ALTER TABLE RENAME COLUMN: the field's column carries the new name. */
		public Table renamed(String field, String newName) {
			Column old = column(field);
			Column next = new Column(newName, old.type(), old.element(), old.json(), old.vec(), old.config(), old.nullable(), old.defaultValue(),
					old.checks());
			Map<String, String> names = new LinkedHashMap<>(renames);
			names.put(field, newName);
			return new Table(database, name, key, columns.stream().map(c -> c.name().equals(old.name()) ? next : c).toList(), tenant, namespace,
					partition, Map.copyOf(names));
		}

		/** The table after ALTER TABLE RENAME TO. */
		public Table named(String newName) {
			return new Table(database, newName, key, columns, tenant, namespace, partition, renames);
		}

		public Column keyColumn() {
			return column(key);
		}

		/** Key columns of the primary key and of ON CONFLICT: the key, plus the partition column of a partitioned table. */
		public List<String> conflictColumns() {
			return partition == null ? List.of(key) : List.of(key, partition);
		}

		public List<String> sliceColumns() {
			List<String> result = new ArrayList<>();
			for (String slice : new String[] { tenant, namespace, partition }) {
				if (slice != null && columns.stream().noneMatch(c -> c.name().equals(slice))) result.add(slice);
			}
			return result;
		}
	}

	/** A filter condition: conjunction, disjunction, negation or a test on one column (a json path below it when given). */
	public sealed interface Cond permits And, Or, Not, Test, Raw {
	}

	public record And(List<Cond> parts) implements Cond {
	}

	public record Or(List<Cond> parts) implements Cond {
	}

	public record Not(Cond operand) implements Cond {
	}

	/**
	 * A test: column, json path below a jsonb column (empty otherwise) with the SQL type of the leaf, operator, value and the
	 * upper bound of 'between'. Operators: eq, neq, gt, gte, lt, lte, in, not_in, like, prefix, contains, contains_any,
	 * contains_all, between, is_null, is_not_null, exists, not_exists, is_empty, length_(eq|neq|gt|gte|lt|lte), match,
	 * match_all_terms, match_any_term, phrase, regex, contains_text, wildcard.
	 */
	public record Test(String column, List<String> path, String pathType, String op, Expr value, Expr upper) implements Cond {
	}

	/** A condition on a slice column with a fixed value: tenant, namespace or partitions. */
	public record Raw(String column, List<Expr> values) implements Cond {
	}

	/** A statement under construction: numbered parameters with their values. */
	public static final class Query {

		private final List<Expr> params = new ArrayList<>();

		/** A placeholder for the value, cast to the given type: $3::integer. */
		public String bind(Expr value, String cast) {
			params.add(value);
			return "$" + params.size() + "::" + cast;
		}

		/** A list value as an array of the element type; a null value stays null. */
		public String array(Expr value, String element) {
			params.add(value);
			String p = "$" + params.size();
			String array = "ARRAY(SELECT jsonb_array_elements_text(" + p + "::jsonb))::" + element + "[]";
			return value instanceof Code.Items ? array : "CASE WHEN " + p + "::jsonb IS NULL THEN NULL ELSE " + array + " END";
		}

		public Code.Items params() {
			return Code.items(params);
		}

		public int size() {
			return params.size();
		}
	}

	/** SQL text with its parameters. */
	public record Sql(String text, Code.Items params) {
	}

	// ---------------------------------------------------------------- names and literals

	/** A quoted identifier; vecDSL names are checked against PostgreSQL's identifier rules and the 63-byte name limit. */
	public static String id(String name) {
		if (!IDENTIFIER.matcher(name).matches()) {
			throw Unsupported.of("the name '" + name + "'; generated SQL uses identifiers of letters, digits and underscores");
		}
		if (name.length() > NAME_LIMIT) {
			throw Unsupported.of("the name '" + name + "'; PostgreSQL names have at most 63 bytes (NAMEDATALEN - 1)");
		}
		return "\"" + name + "\"";
	}

	/** A column of the query's table alias: t."price". */
	public static String col(String name) {
		return ALIAS + "." + id(name);
	}

	/** A string constant for DDL, where PostgreSQL takes no parameters (defaults, comments, text search names). */
	public static String literal(String value) {
		if (value.indexOf('\0') >= 0) throw Unsupported.of("a string with a NUL character; PostgreSQL text cannot hold it");
		return "'" + value.replace("'", "''") + "'";
	}

	/** A DDL constant for a vecDSL literal value of the given column. */
	public static String constant(Expr value, Column column) {
		String cast = column.vec() != null ? column.vec().sql() : column.type();
		return switch (value) {
		case Code.Text text -> literal(text.value()) + "::" + cast;
		case Code.Num num -> literal(number(num.value())) + "::" + cast;
		case Code.Flag flag -> literal(Boolean.toString(flag.value())) + "::" + cast;
		case Code.Nil nil -> "NULL";
		case Code.Items items when column.array() -> literal(arrayText(items)) + "::" + cast;
		case Code.Items items when column.json() -> literal(json(items)) + "::jsonb";
		case Code.Items items when column.vec() != null -> literal(json(items)) + "::" + cast;
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

	private static String arrayText(Code.Items items) {
		return items.items().stream().map(item -> switch (item) {
		case Code.Text text -> "\"" + text.value().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
		case Code.Num num -> number(num.value());
		case Code.Flag flag -> Boolean.toString(flag.value());
		case Code.Nil nil -> "NULL";
		default -> throw Unsupported.of("nested lists in an array default");
		}).collect(Collectors.joining(",", "{", "}"));
	}

	/** JSON text of a literal list, for DDL constants. */
	static String json(Expr value) {
		return switch (value) {
		case Code.Text text -> org.ftn.vecDSL.generator.code.Syntax.quote(text.value());
		case Code.Num num -> number(num.value());
		case Code.Flag flag -> Boolean.toString(flag.value());
		case Code.Nil nil -> "null";
		case Code.Items items -> items.items().stream().map(PgvectorSql::json).collect(Collectors.joining(",", "[", "]"));
		case Code.Entries entries -> entries.entries().entrySet().stream()
				.map(e -> org.ftn.vecDSL.generator.code.Syntax.quote(e.getKey()) + ":" + json(e.getValue()))
				.collect(Collectors.joining(",", "{", "}"));
		default -> throw Unsupported.of("a parameter reference in a DDL constant; PostgreSQL DDL takes no parameters");
		};
	}

	/** The text search configuration of a language name or code, else null. */
	public static String configuration(String language) {
		String lower = language.toLowerCase(Locale.ROOT);
		if (CONFIGURATIONS.contains(lower)) return lower;
		return LANGUAGES.get(lower.length() > 2 && lower.charAt(2) == '-' ? lower.substring(0, 2) : lower);
	}

	/** A regconfig constant: a built-in configuration or an analyzer created in the public schema. */
	public static String regconfig(String config) {
		return config.contains(".") ? literal(config) + "::regconfig" : literal("pg_catalog." + config) + "::regconfig";
	}

	public static String table(Table table) {
		return id(table.name());
	}

	// ---------------------------------------------------------------- metrics

	/** The distance operator of a metric for a vector type (pgvector README, "Vector Operators", "Bit Operators"). */
	public static String operator(Vec vec, String metric) {
		boolean bit = vec.type().equals("bit");
		String op = switch (metric) {
		case "euclidean" -> bit ? null : "<->";
		case "dot" -> bit ? null : "<#>";
		case "cosine" -> bit ? null : "<=>";
		case "manhattan" -> bit ? null : "<+>";
		case "hamming" -> bit ? "<~>" : null;
		case "jaccard" -> bit ? "<%>" : null;
		default -> null;
		};
		if (op == null) {
			throw Unsupported.of(metric + " metric on " + vec.type() + " fields; pgvector measures euclidean, dot, cosine and manhattan "
					+ "distances on vector, halfvec and sparsevec, hamming and jaccard on bit");
		}
		return op;
	}

	/** The operator class of an index: vector_cosine_ops, bit_hamming_ops (pgvector README, "HNSW", "IVFFlat"). */
	public static String opclass(String type, String metric, String method) {
		String suffix = switch (metric) {
		case "euclidean" -> "l2";
		case "dot" -> "ip";
		case "cosine" -> "cosine";
		case "manhattan" -> "l1";
		case "hamming" -> "hamming";
		case "jaccard" -> "jaccard";
		default -> throw new IllegalArgumentException(metric);
		};
		if (method.equals("ivfflat") && (type.equals("sparsevec") || suffix.equals("l1") || suffix.equals("jaccard"))) {
			throw Unsupported.of("an ivf index on " + type + " with the " + metric + " metric; pgvector's IVFFlat indexes vector and "
					+ "halfvec with l2, ip and cosine, and bit with hamming");
		}
		return type + "_" + suffix + "_ops";
	}

	/** Canonical similarity of a pgvector distance d: cosine 1 - d, dot -d (<#> is the negative inner product), the
	 *  distances -d (<-> is not squared), jaccard 1 - d. */
	public static String similarity(String metric, String distance) {
		return switch (metric) {
		case "cosine", "jaccard" -> "1 - " + distance;
		default -> "-" + distance;
		};
	}

	// ---------------------------------------------------------------- DDL

	/** CREATE TABLE with columns, defaults, CHECK limits and the primary key; partitioned by the partition column if any. */
	public static String createTable(Table table, boolean ifNotExists) {
		List<String> parts = new ArrayList<>();
		for (Column column : table.columns()) parts.add(columnDefinition(column, column.name().equals(table.key())));
		for (String slice : table.sliceColumns()) {
			parts.add(id(slice) + " text" + (slice.equals(table.partition()) ? " NOT NULL" : ""));
		}
		parts.add("PRIMARY KEY (" + table.conflictColumns().stream().map(PgvectorSql::id).collect(Collectors.joining(", ")) + ")");
		return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table(table) + " (" + String.join(", ", parts) + ")"
				+ (table.partition() == null ? "" : " PARTITION BY LIST (" + id(table.partition()) + ")");
	}

	/** One column of CREATE TABLE or ALTER TABLE ADD COLUMN. */
	public static String columnDefinition(Column column, boolean key) {
		StringBuilder out = new StringBuilder(id(column.name())).append(' ').append(column.vec() != null ? column.vec().sql() : column.type());
		if (!column.nullable() && !key) out.append(" NOT NULL");
		if (column.defaultValue() != null) out.append(" DEFAULT ").append(constant(column.defaultValue(), column));
		for (String check : column.checks()) out.append(' ').append(check.replace("{}", id(column.name())));
		return out.toString();
	}

	public static String partitionTable(Table table, String partition) {
		return id(table.name() + "__" + partition);
	}

	/** A list partition holding the rows of one vecDSL partition name, or the default partition. */
	public static String createPartition(Table table, String partition, boolean isDefault, boolean ifNotExists) {
		return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + partitionTable(table, partition) + " PARTITION OF " + table(table)
				+ (isDefault ? " DEFAULT" : " FOR VALUES IN (" + literal(partition) + ")");
	}

	/**
	 * A vector index: hnsw (m, ef_construction) or ivfflat (lists) with the metric's operator class; with quantization, an
	 * expression index on the halfvec cast or on binary_quantize (pgvector README, "Half-Precision Indexing", "Binary
	 * Quantization").
	 */
	public static String createVectorIndex(Table table, String name, Column column, String method, Map<String, Integer> options,
			boolean ifNotExists) {
		Vec vec = column.vec();
		String target = id(column.name());
		String type = vec.type();
		String metric = vec.metric();
		if ("halfvec".equals(vec.quantization())) {
			target = "(" + target + "::halfvec(" + vec.dim() + "))";
			type = "halfvec";
		} else if ("binary".equals(vec.quantization())) {
			target = "(binary_quantize(" + target + ")::bit(" + vec.dim() + "))";
			type = "bit";
			metric = "hamming";
		}
		String with = options.isEmpty() ? ""
				: " WITH (" + options.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).collect(Collectors.joining(", ")) + ")";
		return "CREATE INDEX " + (ifNotExists ? "IF NOT EXISTS " : "") + id(name) + " ON " + table(table) + " USING " + method + " ("
				+ target + " " + opclass(type, metric, method) + ")" + with;
	}

	/** A payload index: B-tree, GIN on arrays and jsonb, or GIN on the tsvector of a full-text column. */
	public static String createPayloadIndex(Table table, String name, Column column, List<String> path, String structure, boolean ifNotExists) {
		String target;
		String method;
		if (column.config() != null && "fts".equals(structure)) {
			target = "(" + tsvector(column, false) + ")";
			method = "gin";
		} else if (!path.isEmpty()) {
			target = "((" + id(column.name()) + " #>> " + literal(pathText(path)) + "))";
			method = "btree";
		} else if (column.array() || column.json()) {
			target = id(column.name()) + (column.json() ? " jsonb_path_ops" : "");
			method = "gin";
		} else {
			target = id(column.name());
			method = "btree";
		}
		return "CREATE INDEX " + (ifNotExists ? "IF NOT EXISTS " : "") + id(name) + " ON " + table(table) + " USING " + method + " (" + target + ")";
	}

	static String pathText(List<String> path) {
		return path.stream().map(segment -> segment.replace("\\", "\\\\").replace(",", "\\,").replace("{", "\\{").replace("}", "\\}"))
				.collect(Collectors.joining(",", "{", "}"));
	}

	/** A column under a table alias, or a sub-field read from its jsonb value with #>> and cast to the sub-field's type. */
	public static String reference(String alias, Column column, List<String> path, String pathType) {
		String ref = alias + "." + id(column.name());
		if (path.isEmpty()) return ref;
		return "(" + ref + " #>> " + literal(pathText(path)) + ")" + ("text".equals(pathType) ? "" : "::" + pathType);
	}

	/** The tsvector of a full-text column, the expression its GIN index and every keyword query use. */
	public static String tsvector(Column column, boolean qualified) {
		String ref = qualified ? col(column.name()) : id(column.name());
		return "to_tsvector(" + regconfig(column.config()) + ", coalesce(" + ref + ", ''))";
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
		case Raw raw -> raw.values().size() == 1 ? col(raw.column()) + " = " + q.bind(raw.values().get(0), "text")
				: col(raw.column()) + " = ANY(" + q.array(Code.items(raw.values()), "text") + ")";
		case Test test -> test(q, table, test);
		};
	}

	private static final Set<String> ORDERED = Set.of("smallint", "integer", "bigint", "real", "double precision", "date",
			"timestamp", "timestamptz", "text", "uuid");

	private static String test(Query q, Table table, Test test) {
		Column column = table.column(test.column());
		boolean nested = !test.path().isEmpty();
		String type = nested ? test.pathType() : column.paramType();
		String ref = nested ? "(" + col(column.name()) + " #>> " + literal(pathText(test.path())) + ")" + ("text".equals(type) ? "" : "::" + type)
				: col(column.name());
		boolean array = !nested && column.array();
		String element = array ? column.element() : type;
		String op = test.op();
		if (op.startsWith("length_")) {
			String length = array ? "cardinality(" + ref + ")" : "char_length(" + ref + ")";
			return length + " " + comparison(op.substring(7)) + " " + q.bind(test.value(), "integer");
		}
		switch (op) {
		case "is_null", "not_exists":
			return ref + " IS NULL";
		case "is_not_null", "exists":
			return ref + " IS NOT NULL";
		case "is_empty":
			return array ? "cardinality(" + ref + ") = 0" : ref + " = ''";
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
			return ref + " = ANY(" + q.array(test.value(), type) + ")";
		case "not_in":
			scalar(column, array, op);
			return "(" + ref + " = ANY(" + q.array(test.value(), type) + ")) IS NOT TRUE";
		case "contains":
			return array ? q.bind(test.value(), element) + " = ANY(" + ref + ")" : "strpos(" + text(ref, type) + ", " + q.bind(test.value(), "text") + ") > 0";
		case "contains_any":
			arrayOnly(column, array, op);
			return ref + " && " + q.array(test.value(), element);
		case "contains_all":
			arrayOnly(column, array, op);
			return ref + " @> " + q.array(test.value(), element);
		case "like":
			return text(ref, type) + " LIKE " + q.bind(test.value(), "text");
		case "prefix":
			return "starts_with(" + text(ref, type) + ", " + q.bind(test.value(), "text") + ")";
		case "contains_text":
			return "strpos(" + text(ref, type) + ", " + q.bind(test.value(), "text") + ") > 0";
		case "regex":
			return text(ref, type) + " ~ " + q.bind(test.value(), "text");
		case "wildcard": {
			String p = q.bind(test.value(), "text");
			return text(ref, type) + " LIKE replace(replace(replace(replace(replace(" + p + ", '\\', '\\\\'), '%', '\\%'), '_', '\\_'), '*', '%'), '?', '_')";
		}
		case "match", "match_all_terms", "match_any_term", "phrase":
			return fullText(q, column, nested, ref, op, test.value());
		default:
			throw new IllegalArgumentException(op);
		}
	}

	private static String fullText(Query q, Column column, boolean nested, String ref, String op, Expr value) {
		String config = !nested && column.config() != null ? column.config() : "simple";
		String vector = !nested && column.config() != null ? tsvector(column, true) : "to_tsvector(" + regconfig(config) + ", coalesce(" + ref + ", ''))";
		return vector + " @@ " + tsquery(q, config, op, value);
	}

	/** The tsquery of a keyword input: all terms (plainto_tsquery), any term, or a phrase (phraseto_tsquery). */
	public static String tsquery(Query q, String config, String op, Expr value) {
		String p = q.bind(value, "text");
		return switch (op) {
		case "phrase" -> "phraseto_tsquery(" + regconfig(config) + ", " + p + ")";
		case "match_any_term" -> "coalesce((SELECT string_agg(quote_literal(lexeme), ' | ') FROM unnest(tsvector_to_array(to_tsvector("
				+ regconfig(config) + ", " + p + "))) AS lexeme), '')::tsquery";
		default -> "plainto_tsquery(" + regconfig(config) + ", " + p + ")";
		};
	}

	private static String text(String ref, String type) {
		return "text".equals(type) ? ref : ref + "::text";
	}

	private static void ordered(Column column, String type, boolean array) {
		if (array || !ORDERED.contains(type) && !type.startsWith("varchar")) {
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

	/** A value written into a column: cast to its type, lists through jsonb, json values as jsonb. */
	public static String value(Query q, Column column, Expr value) {
		if (column.array()) return q.array(value, column.element());
		if (column.json()) return q.bind(jsonValue(value), "jsonb");
		return q.bind(value, column.paramType());
	}

	/** A json field value bound as JSON text, so that a string stays a JSON string. */
	private static Expr jsonValue(Expr value) {
		return value instanceof Code.Text text ? new Code.Text(org.ftn.vecDSL.generator.code.Syntax.quote(text.value())) : value;
	}

	/** The output expression of a column in a result object. */
	public static String output(Column column, String alias) {
		String ref = alias + "." + id(column.name());
		if (column.vec() == null) return ref;
		return switch (column.vec().type()) {
		case "vector", "halfvec" -> ref + "::real[]";
		default -> ref + "::text";
		};
	}

	/** json_build_object over name and expression pairs, in order. */
	public static String object(Map<String, String> fields) {
		return "json_build_object(" + fields.entrySet().stream().map(e -> literal(e.getKey()) + ", " + e.getValue())
				.collect(Collectors.joining(", ")) + ")";
	}

	/** The record fields of rows: the key under its own name, then the output columns. */
	public static Map<String, String> fields(Table table, List<String> output, String alias) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(table.key(), output(table.keyColumn(), alias));
		for (String name : output) result.put(name, output(table.column(name), alias));
		return result;
	}

	// ---------------------------------------------------------------- writes

	/**
	 * INSERT of rows given as one json array parameter: each element is read with jsonb_to_record into the column types;
	 * a column with a default takes it when the element does not name the column. Slice values are bound parameters.
	 */
	public static String insertRows(Query q, Table table, Expr rows, Map<String, Expr> slices, String conflict, String counter) {
		String rowsParam = q.bind(rows, "jsonb");
		List<String> names = new ArrayList<>();
		List<String> selected = new ArrayList<>();
		List<String> record = new ArrayList<>();
		for (Column column : table.columns()) {
			if (slices.containsKey(column.name())) continue;
			names.add(id(column.name()));
			record.add(id(column.name()) + " " + (column.vec() != null ? column.vec().sql() : column.type()));
			String read = "r." + id(column.name());
			selected.add(column.defaultValue() == null ? read
					: "CASE WHEN jsonb_exists(e.doc, " + literal(column.name()) + ") THEN " + read + " ELSE " + value(q, column, column.defaultValue()) + " END");
		}
		for (Map.Entry<String, Expr> slice : slices.entrySet()) {
			names.add(id(slice.getKey()));
			selected.add(q.bind(slice.getValue(), "text"));
		}
		StringBuilder sql = new StringBuilder("WITH w AS (INSERT INTO ").append(table(table)).append(" (").append(String.join(", ", names))
				.append(") SELECT ").append(String.join(", ", selected)).append(" FROM jsonb_array_elements(").append(rowsParam)
				.append(") AS e(doc) CROSS JOIN LATERAL jsonb_to_record(e.doc) AS r(").append(String.join(", ", record)).append(")");
		String target = table.conflictColumns().stream().map(PgvectorSql::id).collect(Collectors.joining(", "));
		switch (conflict) {
		case "error" -> {
		}
		case "ignore" -> sql.append(" ON CONFLICT (").append(target).append(") DO NOTHING");
		default -> {
			List<String> sets = new ArrayList<>();
			for (String name : names) {
				if (!table.conflictColumns().contains(unquote(name))) sets.add(name + " = EXCLUDED." + name);
			}
			sql.append(" ON CONFLICT (").append(target).append(") DO ").append(sets.isEmpty() ? "NOTHING" : "UPDATE SET " + String.join(", ", sets));
		}
		}
		sql.append(" RETURNING 1) SELECT json_build_object(").append(literal(counter)).append(", count(*)");
		if (conflict.equals("ignore")) sql.append(", 'skipped', jsonb_array_length(").append(rowsParam.substring(0, rowsParam.indexOf(':'))).append("::jsonb) - count(*)");
		return sql.append(") FROM w").toString();
	}

	/**
	 * UPDATE ... FROM the records of a json parameter, matched by key: merge sets the columns a record carries, replace also
	 * resets the record's other columns that are not vectors to their defaults; a record without a stored row changes nothing.
	 */
	public static String updateRows(Query q, Table table, Expr rows, boolean replace, String where) {
		String rowsParam = q.bind(rows, "jsonb");
		List<String> record = new ArrayList<>();
		List<String> sets = new ArrayList<>();
		for (Column column : table.columns()) {
			record.add(id(column.name()) + " " + (column.vec() != null ? column.vec().sql() : column.type()));
			if (column.name().equals(table.key())) continue;
			String kept = replace && column.vec() == null ? (column.defaultValue() == null ? "NULL" : value(q, column, column.defaultValue())) : col(column.name());
			sets.add(id(column.name()) + " = CASE WHEN jsonb_exists(e.doc, " + literal(column.name()) + ") THEN r." + id(column.name()) + " ELSE " + kept + " END");
		}
		String condition = and(List.of(col(table.key()) + " = r." + id(table.key()), where == null ? "" : where));
		return "UPDATE " + table(table) + " " + ALIAS + " SET " + String.join(", ", sets) + " FROM jsonb_array_elements(" + rowsParam
				+ ") AS e(doc) CROSS JOIN LATERAL jsonb_to_record(e.doc) AS r(" + String.join(", ", record) + ")" + whereClause(condition);
	}

	private static String unquote(String quoted) {
		return quoted.substring(1, quoted.length() - 1);
	}

	/** A data-modifying statement wrapped so that it returns {counter: affected rows}. */
	public static String counted(String modification, String counter) {
		return "WITH w AS (" + modification + " RETURNING 1) SELECT json_build_object(" + literal(counter) + ", count(*)) FROM w";
	}

	/** The key condition of ids: t."id" = ANY(...). */
	public static String keyIn(Query q, Table table, Code.Items ids) {
		return col(table.key()) + " = ANY(" + q.array(ids, table.keyColumn().paramType()) + ")";
	}

	// ---------------------------------------------------------------- reads

	/** Settings applied with set_config(name, value, true) at the start of the statement's transaction (SET LOCAL). */
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
