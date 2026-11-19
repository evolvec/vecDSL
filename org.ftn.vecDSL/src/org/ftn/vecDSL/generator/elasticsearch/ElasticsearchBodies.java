package org.ftn.vecDSL.generator.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * Request bodies and names for Elasticsearch, built at generation time as code-model values: the runtimes of the three host
 * languages send them as they are. Values reach the server only as JSON values of a body (never inside a script or a path):
 * painless scripts are constants that read their params.
 */
public final class ElasticsearchBodies {

	public static final String RAW = "raw";
	public static final String TENANT_FIELD = "vecdsl_tenant";
	public static final String NAMESPACE_FIELD = "vecdsl_namespace";
	public static final String PARTITION_FIELD = "vecdsl_partition";
	public static final String DEFAULT_PARTITION = "default";
	public static final String GROUP = "group";
	public static final int MAX_CANDIDATES = 10000;

	/** Sets, removes, appends to and removes items from fields of _source, all from params (update and update by query). */
	public static final String UPDATE_SCRIPT = String.join(" ",
			"for (e in params.set.entrySet()) { ctx._source[e.getKey()] = e.getValue(); }",
			"for (f in params.remove) { ctx._source.remove(f); }",
			"for (e in params.append.entrySet()) { def c = ctx._source[e.getKey()]; if (c == null) { c = new ArrayList(); }",
			"else if (!(c instanceof List)) { def l = new ArrayList(); l.add(c); c = l; } c.addAll(e.getValue()); ctx._source[e.getKey()] = c; }",
			"for (e in params.pull.entrySet()) { def c = ctx._source[e.getKey()]; def drop = e.getValue();",
			"if (c instanceof List) { c.removeIf(x -> drop.contains(x)); } else if (c != null && drop.contains(c)) { ctx._source.remove(e.getKey()); } }");
	/** Moves the value of params.from to params.to (a renamed field). */
	public static final String RENAME_SCRIPT = "if (ctx._source.containsKey(params.from)) { ctx._source[params.to] = ctx._source.remove(params.from); } else { ctx.op = 'noop'; }";
	/** Removes params.field from _source (a dropped field). */
	public static final String DROP_SCRIPT = "if (ctx._source.containsKey(params.field)) { ctx._source.remove(params.field); } else { ctx.op = 'noop'; }";
	/** Writes params.value into params.field where it is missing (an added field with a default). */
	public static final String DEFAULT_SCRIPT = "if (ctx._source.containsKey(params.field)) { ctx.op = 'noop'; } else { ctx._source[params.field] = params.value; }";
	/** Compares the number of values of params.field with params.value (doc values: distinct values for keyword fields). */
	public static final String LENGTH_SCRIPT = String.join(" ", "int n = doc.containsKey(params.field) ? doc[params.field].size() : 0;",
			"if (params.op == 'eq') { return n == params.value; } if (params.op == 'neq') { return n != params.value; }",
			"if (params.op == 'gt') { return n > params.value; } if (params.op == 'gte') { return n >= params.value; }",
			"if (params.op == 'lt') { return n < params.value; } return n <= params.value;");

	private static final Pattern NAME = Pattern.compile("[a-z0-9][a-z0-9_.-]*");
	private static final Pattern FIELD = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
	private static final Pattern TIME_VALUE = Pattern.compile("[0-9]+(d|h|m|s|ms|micros|nanos)");

	private ElasticsearchBodies() {
	}

	/** An object with the given name-value pairs; null values are left out. */
	public static Entries obj(Object... pairs) {
		Entries result = Code.entries();
		for (int i = 0; i < pairs.length; i += 2) {
			if (pairs[i + 1] != null) {
				result.put((String) pairs[i], (Expr) pairs[i + 1]);
			}
		}
		return result;
	}

	public static Items arr(List<? extends Expr> items) {
		return Code.items(items);
	}

	public static Items arr(Expr... items) {
		return Code.items(List.of(items));
	}

	public static Code.Text text(String value) {
		return Code.text(value);
	}

	public static Code.Num num(double value) {
		return Code.num(value);
	}

	/** A runtime call with name-value arguments; null values are left out. */
	public static Code.Call call(String op, Object... pairs) {
		return Code.runtime(op, obj(pairs));
	}

	/** One request: method, path, query parameters and body (each optional but the first two). */
	public static Code.Call request(String method, String path, Entries params, Expr body) {
		return call("request", "method", text(method), "path", text(path), "params", params == null || params.entries().isEmpty() ? null : params, "body", body);
	}

	/** A field name checked for use in mappings, queries and script params. */
	public static String field(String name) {
		if (!FIELD.matcher(name).matches() || name.startsWith("_")) {
			throw Unsupported.of("the field name " + name + "; Elasticsearch reserves names starting with _ for metadata fields");
		}
		return name;
	}

	/**
	 * The index of a collection: its name in snake case and lower case (index names must be lower case), prefixed with the
	 * logical database and a dot when the collection belongs to one.
	 */
	public static String indexName(String database, String collection) {
		String name = (database == null ? "" : snake(database) + ".") + snake(collection);
		return checked(name, "index");
	}

	/** An alias, tenant or partition alias name under the same rules as index names. */
	public static String aliasName(String name) {
		return checked(snake(name), "alias");
	}

	/** The name of the filtered alias that shows one tenant, namespace or partition of an index. */
	public static String sliceAlias(String index, String kind, String value) {
		return checked(index + "-" + kind + "-" + value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"), "alias");
	}

	private static String snake(String name) {
		return String.join("_", Syntax.words(name));
	}

	private static String checked(String name, String what) {
		if (!NAME.matcher(name).matches() || name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 255) {
			throw Unsupported.of("the " + what + " name " + name + "; Elasticsearch names are lower case, start with a letter or digit and have at most 255 bytes");
		}
		return name;
	}

	/** The score form the runtime turns into the canonical similarity. */
	public static Entries scoreForm(ElasticsearchIndex.Vector vector, boolean nativeScore) {
		if (nativeScore) {
			return obj("kind", text("native"));
		}
		String kind = switch (vector.similarity()) {
		case "l2_norm" -> "bit".equals(vector.element()) ? "hamming" : "l2_norm";
		case "dot_product" -> "byte".equals(vector.element()) ? "byte_dot_product" : "dot_product";
		default -> vector.similarity();
		};
		return obj("kind", text(kind), "dims", kind.equals("hamming") || kind.equals("byte_dot_product") ? num(vector.dims()) : null);
	}

	/** The score form of a relevance score (BM25 and sparse vectors): the score as the server computes it. */
	public static Entries relevance() {
		return obj("kind", text("relevance"));
	}

	/** The Elasticsearch similarity of a vecDSL metric for a dense_vector field. */
	public static String similarity(String metric, String element, boolean normalized) {
		return switch (metric) {
		case "cosine" -> "bit".equals(element) ? unsupportedMetric(metric, element) : "cosine";
		case "euclidean" -> "bit".equals(element) ? unsupportedMetric(metric, element) : "l2_norm";
		case "dot" -> "bit".equals(element) ? unsupportedMetric(metric, element) : normalized ? "dot_product" : "max_inner_product";
		case "hamming" -> "bit".equals(element) ? "l2_norm" : unsupportedMetric(metric, element);
		default -> throw Unsupported.of("the " + metric + " metric on a dense_vector field; Elasticsearch indexes l2_norm, cosine, dot_product and"
				+ " max_inner_product (hamming through l2_norm on bit vectors)");
		};
	}

	private static String unsupportedMetric(String metric, String element) {
		throw Unsupported.of("the " + metric + " metric on " + element + " vectors; bit vectors support only l2_norm, which is the hamming distance");
	}

	/**
	 * The exact score of a script_score query: the same _score the kNN search of the similarity gives, so the runtime converts
	 * both alike; l1_norm is exact only (no dense_vector similarity uses it).
	 */
	public static String exactScript(String kind) {
		return switch (kind) {
		case "cosine" -> "(cosineSimilarity(params.query_vector, params.field) + 1.0) / 2.0";
		case "dot_product" -> "(dotProduct(params.query_vector, params.field) + 1.0) / 2.0";
		case "max_inner_product" -> "double v = dotProduct(params.query_vector, params.field); return v < 0 ? 1 / (1 - v) : v + 1;";
		case "l2_norm" -> "double d = l2norm(params.query_vector, params.field); return 1 / (1 + d * d);";
		case "l1_norm" -> "1 / (1 + l1norm(params.query_vector, params.field))";
		case "hamming" -> "(params.dims - hamming(params.query_vector, params.field)) / params.dims";
		default -> throw Unsupported.of("exact search with the " + kind + " score");
		};
	}

	/** The exact score kind of a vecDSL metric written on a search, on a field of the given element type. */
	public static String exactKind(String metric, String element) {
		return switch (metric) {
		case "cosine" -> "cosine";
		case "dot" -> "max_inner_product";
		case "euclidean" -> "l2_norm";
		case "manhattan" -> "l1_norm";
		case "hamming" -> "bit".equals(element) ? "hamming" : unsupportedMetric(metric, element);
		default -> throw Unsupported.of("the " + metric + " metric; Elasticsearch scripts compute cosine, dot product, l1, l2 and hamming distances");
		};
	}

	/** A painless script with its params. */
	public static Entries script(String source, Entries params) {
		return obj("source", text(source), "params", params);
	}

	/**
	 * An SLM schedule: an interval (30m, 1d) as it is, or a five-field cron expression (minute hour day month weekday) in the
	 * six-field form of Elasticsearch's cron (seconds first, ? for the unspecified day field).
	 */
	public static String schedule(String interval) {
		String trimmed = interval.trim();
		if (TIME_VALUE.matcher(trimmed).matches()) {
			return trimmed;
		}
		String[] parts = trimmed.split("\\s+");
		if (parts.length != 5) {
			throw Unsupported.of("the backup interval " + interval + "; Elasticsearch takes an interval such as 1d or a cron expression");
		}
		String day = parts[2];
		String weekday = parts[4];
		if (!"*".equals(day) && !"*".equals(weekday)) {
			throw Unsupported.of("a cron expression with both a day of the month and a weekday; Elasticsearch's cron leaves one of them unspecified (?)");
		}
		return "*".equals(weekday) ? String.join(" ", "0", parts[0], parts[1], day, parts[3], "?")
				: String.join(" ", "0", parts[0], parts[1], "?", parts[3], weekday);
	}

	/** Index privileges of a vecDSL role (read, write, admin, all or Elasticsearch index privilege names). */
	public static List<Expr> privileges(List<String> declared) {
		List<Expr> result = new ArrayList<>();
		for (String privilege : declared) {
			String name = switch (privilege.toLowerCase(Locale.ROOT)) {
			case "read" -> "read";
			case "write" -> "write";
			case "admin", "all" -> "all";
			case "create", "create_doc", "create_index", "delete", "delete_index", "index", "maintenance", "manage", "monitor",
					"view_index_metadata", "auto_configure", "manage_ilm" -> privilege.toLowerCase(Locale.ROOT);
			default -> throw Unsupported.of("the privilege " + privilege + "; Elasticsearch index privileges are read, write, create, create_doc,"
					+ " create_index, delete, delete_index, index, maintenance, manage, monitor, view_index_metadata and all");
			};
			result.add(text(name));
		}
		return result;
	}

	/** Search params of a body: candidates of the kNN section within the server's limit of 10000. */
	public static int candidates(int requested, int k) {
		return Math.min(MAX_CANDIDATES, Math.max(requested, k));
	}

	/** The common parts of every write's code: the bulk operation, the counter and the policy for existing keys. */
	public static Map<String, String> writeMode(String policy) {
		return switch (policy) {
		case "error" -> Map.of("op", "create", "counter", "insert_count");
		case "ignore" -> Map.of("op", "create", "counter", "insert_count", "ignore", "true");
		case "update" -> Map.of("op", "update", "counter", "upsert_count");
		default -> Map.of("op", "index", "counter", "upsert_count");
		};
	}
}
