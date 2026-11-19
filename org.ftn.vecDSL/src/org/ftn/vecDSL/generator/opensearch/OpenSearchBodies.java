package org.ftn.vecDSL.generator.opensearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * Request bodies and names for OpenSearch, built at generation time as code-model values: the runtimes of the three host
 * languages send them as they are. Values reach the server only as JSON values of a body (never inside a script or a path):
 * painless scripts are constants that read their params, and exact vector search is the knn_score script with the query vector
 * in its params.
 */
public final class OpenSearchBodies {

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
	private static final Pattern TIME_VALUE = Pattern.compile("([0-9]+)(d|h|m)");

	private OpenSearchBodies() {
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
			throw Unsupported.of("the field name " + name + "; OpenSearch reserves names starting with _ for metadata fields");
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
			throw Unsupported.of("the " + what + " name " + name + "; OpenSearch names are lower case, start with a letter or digit and have at most 255 bytes");
		}
		return name;
	}

	/**
	 * The score form the runtime turns into the canonical similarity: the space type of the field (approximate search and
	 * the knn_score script give the same _score for float vectors of an index created by 2.19 or later), cosine_exact_byte for
	 * the knn_score script on byte vectors (1 + cos), native to keep the server's value.
	 */
	public static Entries scoreForm(String space, boolean nativeScore) {
		return nativeScore ? obj("kind", text("native")) : obj("kind", text(space));
	}

	/** The score form of a relevance score (BM25 and rank_features dot products): the score as the server computes it. */
	public static Entries relevance() {
		return obj("kind", text("relevance"));
	}

	/**
	 * The k-NN space type of a vecDSL metric on a knn_vector field of the given data type: cosinesimil, l2 and innerproduct on
	 * float, half_float and byte vectors, hamming on binary vectors.
	 */
	public static String space(String metric, String element) {
		boolean binary = "binary".equals(element);
		return switch (metric) {
		case "cosine" -> binary ? unsupportedMetric(metric, element) : "cosinesimil";
		case "euclidean" -> binary ? unsupportedMetric(metric, element) : "l2";
		case "dot" -> binary ? unsupportedMetric(metric, element) : "innerproduct";
		case "hamming" -> binary ? "hamming" : unsupportedMetric(metric, element);
		case "manhattan" -> throw Unsupported.of("the manhattan metric on a knn_vector field; the Faiss and Lucene engines index l2, cosinesimil, innerproduct"
				+ " and hamming (l1 only in the deprecated NMSLIB engine), so manhattan is an exact search (knn_score with space l1)");
		default -> throw Unsupported.of("the " + metric + " metric; the k-NN plugin has the spaces l1, l2, linf, cosinesimil, innerproduct and hamming");
		};
	}

	private static String unsupportedMetric(String metric, String element) {
		throw Unsupported.of("the " + metric + " metric on " + element + " vectors; binary knn_vector fields support only the hamming space,"
				+ " and hamming applies only to binary fields");
	}

	/** The space type of a metric written on an exact search (the knn_score script also scores l1 and linf). */
	public static String exactSpace(String metric, String element) {
		return switch (metric) {
		case "manhattan" -> "binary".equals(element) ? unsupportedMetric(metric, element) : "l1";
		default -> space(metric, element);
		};
	}

	/** The score form of an exact search: the knn_score script scores byte vectors in cosinesimil as 1 + cos. */
	public static String exactForm(String space, String element) {
		return "cosinesimil".equals(space) && "byte".equals(element) ? "cosine_exact_byte" : space;
	}

	/** The knn_score script of an exact search (lang knn), with the query vector and the space in its params. */
	public static Entries exactScript(String field, Expr queryVector, String space) {
		return obj("source", text("knn_score"), "lang", text("knn"), "params", obj("field", text(field), "query_value", queryVector, "space_type", text(space)));
	}

	/** A painless script with its params. */
	public static Entries script(String source, Entries params) {
		return obj("source", text(source), "params", params);
	}

	/**
	 * A snapshot management schedule: a five-field cron expression (minute hour day month weekday) as it is, or an interval
	 * (30m, 6h, 1d) as the cron expression that fires at its multiples from midnight UTC.
	 */
	public static String schedule(String interval) {
		String trimmed = interval.trim();
		Matcher matcher = TIME_VALUE.matcher(trimmed);
		if (matcher.matches()) {
			int amount = Integer.parseInt(matcher.group(1));
			return switch (matcher.group(2)) {
			case "m" -> amount > 0 && 60 % amount == 0 ? (amount == 60 ? "0 * * * *" : "*/" + amount + " * * * *") : badInterval(interval);
			case "h" -> amount > 0 && 24 % amount == 0 ? (amount == 24 ? "0 0 * * *" : "0 */" + amount + " * * *") : badInterval(interval);
			case "d" -> amount == 1 ? "0 0 * * *" : badInterval(interval);
			default -> badInterval(interval);
			};
		}
		if (trimmed.split("\\s+").length != 5) {
			return badInterval(interval);
		}
		return trimmed;
	}

	private static String badInterval(String interval) {
		throw Unsupported.of("the backup interval " + interval + "; snapshot management schedules are cron expressions, and only intervals that"
				+ " divide an hour or a day (such as 15m, 6h or 1d) have one");
	}

	/** Index action groups of the security plugin for a vecDSL role (read, write, admin, all or action group names). */
	public static List<Expr> privileges(List<String> declared) {
		List<Expr> result = new ArrayList<>();
		for (String privilege : declared) {
			String name = switch (privilege.toLowerCase(Locale.ROOT)) {
			case "read" -> "read";
			case "write" -> "write";
			case "admin", "all" -> "indices_all";
			case "get", "delete", "crud", "search", "suggest", "create_index", "indices_monitor", "index", "data_access", "manage_aliases",
					"manage", "indices_all" -> privilege.toLowerCase(Locale.ROOT);
			default -> throw Unsupported.of("the privilege " + privilege + "; the index action groups of the security plugin are read, write, get,"
					+ " delete, crud, search, suggest, create_index, indices_monitor, index, data_access, manage_aliases, manage and indices_all");
			};
			result.add(text(name));
		}
		return result;
	}

	/** The ef_search of a knn query: the requested candidate pool, at least k (an hnsw search visits at least k candidates). */
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
