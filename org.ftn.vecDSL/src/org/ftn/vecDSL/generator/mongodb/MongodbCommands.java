package org.ftn.vecDSL.generator.mongodb;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * Commands, index definitions, filters and aggregation pipelines for MongoDB, built at generation time as code-model values:
 * the runtimes of the three host languages send them as they are. Values reach the server only as operands of explicit query
 * or update operators, or wrapped in $literal inside aggregation expressions, so a value never acts as an operator.
 */
public final class MongodbCommands {

	public static final String TENANT_FIELD = "vecdsl_tenant";
	public static final String NAMESPACE_FIELD = "vecdsl_namespace";
	public static final String PARTITION_FIELD = "vecdsl_partition";
	public static final String VERSION_FIELD = "vecdsl_version";
	public static final String STAMP_FIELD = "vecdsl_stamp";
	public static final String EXPIRES_FIELD = "vecdsl_expires";
	public static final String DEFAULT_PARTITION = "default";
	public static final String TEXT_INDEX = "vecdsl_text";
	public static final String SCORE = "score";
	public static final int MAX_CANDIDATES = 10000;
	public static final int MAX_DIMENSIONS = 8192;
	public static final int AWAIT_SECONDS = 60;

	private static final Pattern FIELD = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
	private static final Pattern NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]*");
	private static final Pattern INTERVAL = Pattern.compile("([0-9]+)(ms|s|m|h|d|w)");

	private MongodbCommands() {
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

	public static Code.Flag flag(boolean value) {
		return Code.flag(value);
	}

	/** A runtime call with name-value arguments; null values are left out. */
	public static Code.Call call(String op, Object... pairs) {
		return Code.runtime(op, obj(pairs));
	}

	/** One database command: the command document, sent with runCommand on the database. */
	public static Code.Call command(String database, Entries document) {
		return call("command", "database", text(database), "command", document);
	}

	/** A literal operand inside an aggregation expression. */
	public static Entries literal(Expr value) {
		return obj("$literal", value);
	}

	/** A field name checked for use in documents, filters, projections and index definitions. */
	public static String field(String name) {
		if (!FIELD.matcher(name).matches() || name.startsWith("vecdsl_")) {
			throw Unsupported.of("the field name " + name + "; the generator writes field names of letters, digits and _ that do not start with vecdsl_ (its hidden fields)");
		}
		return name;
	}

	/** A collection, view or database name checked against MongoDB's naming rules. */
	public static String checkedName(String declared, String what) {
		if (!NAME.matcher(declared).matches() || declared.startsWith("system") || declared.getBytes(StandardCharsets.UTF_8).length > 64) {
			throw Unsupported.of("the " + what + " name " + declared
					+ "; the generator writes names of at most 64 letters, digits, _ and -, not starting with system (MongoDB reserves system.*)");
		}
		return declared;
	}

	/** A role name: letters, digits and _ . - (createRole). */
	public static String roleName(String declared) {
		if (!declared.matches("[A-Za-z0-9_][A-Za-z0-9_.-]*")) {
			throw Unsupported.of("the role name " + declared + "; the generator writes role names of letters, digits and _ . -");
		}
		return declared;
	}

	/** The similarity of a vectorSearch index field for a vecDSL metric. */
	public static String similarity(String metric, boolean normalized) {
		return switch (metric) {
		case "cosine" -> "cosine";
		case "euclidean" -> "euclidean";
		case "dot" -> {
			if (!normalized) {
				throw Unsupported.of("the dot metric on vectors not declared normalized; mongot's dotProduct requires vectors normalized to unit length at index and query time");
			}
			yield "dotProduct";
		}
		default -> throw Unsupported.of("the " + metric + " metric; vectorSearch indexes compare with euclidean, cosine and dotProduct");
		};
	}

	/**
	 * The canonical similarity from mongot's score, as an aggregation expression: mongot scores with Lucene's similarity
	 * functions, cosine and dotProduct as (1 + s) / 2 and euclidean as 1 / (1 + d^2).
	 */
	public static Expr canonicalScore(String similarity, String meta) {
		Entries score = obj("$meta", text(meta));
		return switch (similarity) {
		case "euclidean" -> obj("$multiply", arr(num(-1), obj("$sqrt", obj("$max", arr(num(0), obj("$subtract", arr(obj("$divide", arr(num(1), score)), num(1))))))));
		default -> obj("$subtract", arr(obj("$multiply", arr(num(2), score)), num(1)));
		};
	}

	/** The canonical similarity of an exact scan: the raw $similarity* value (cosine, dot) or the negated euclidean distance. */
	public static Expr exactScore(String metric, String path, Expr queryVector) {
		Items vectors = arr(text("$" + path), literal(queryVector));
		return switch (metric) {
		case "cosine" -> obj("$similarityCosine", vectors);
		case "dot" -> obj("$similarityDotProduct", vectors);
		case "euclidean" -> obj("$multiply", arr(num(-1), obj("$similarityEuclidean", vectors)));
		default -> throw Unsupported.of("the " + metric + " metric; MongoDB computes cosine ($similarityCosine), dot product ($similarityDotProduct) and euclidean ($similarityEuclidean) similarities");
		};
	}

	/** The canonical similarity of two vector expressions (the distance matrix): cosine, dot product or the negated euclidean distance. */
	public static Expr similarityOf(String metric, Expr first, Expr second) {
		Items vectors = arr(first, second);
		return switch (metric) {
		case "cosine" -> obj("$similarityCosine", vectors);
		case "dot" -> obj("$similarityDotProduct", vectors);
		default -> obj("$multiply", arr(num(-1), obj("$similarityEuclidean", vectors)));
		};
	}

	/** numCandidates of $vectorSearch: the requested pool (20 times the limit by default), at least the limit, at most 10000. */
	public static int candidates(Integer requested, int limit) {
		int pool = requested == null ? 20 * limit : requested;
		if (limit > MAX_CANDIDATES) {
			throw Unsupported.of("a vector search for " + limit + " hits; $vectorSearch returns at most numCandidates, which is at most 10000");
		}
		return Math.min(MAX_CANDIDATES, Math.max(pool, limit));
	}

	/** The privilege actions of vecDSL privileges (read, write, admin, all) or MongoDB action names. */
	public static List<Expr> actions(List<String> declared) {
		Set<String> result = new LinkedHashSet<>();
		for (String privilege : declared.isEmpty() ? List.of("read") : declared) {
			switch (privilege.toLowerCase(Locale.ROOT)) {
			case "read" -> result.addAll(List.of("find", "listIndexes", "listSearchIndexes", "collStats"));
			case "write" -> result.addAll(List.of("insert", "update", "remove"));
			case "admin", "all" -> result.addAll(List.of("find", "listIndexes", "listSearchIndexes", "collStats", "insert", "update", "remove",
					"createIndex", "dropIndex", "createSearchIndexes", "updateSearchIndex", "dropSearchIndex", "collMod", "compact", "createCollection",
					"dropCollection", "renameCollectionSameDB"));
			case "find", "insert", "update", "remove", "createindex", "dropindex", "collmod", "compact", "createcollection", "dropcollection",
					"listindexes", "listsearchindexes", "createsearchindexes", "updatesearchindex", "dropsearchindex", "collstats", "dbstats",
					"renamecollectionsamedb", "changestream" -> result.add(actionName(privilege));
			default -> throw Unsupported.of("the privilege " + privilege + "; MongoDB privilege actions the generator grants are read, write, admin, all and the"
					+ " collection actions find, insert, update, remove, createIndex, dropIndex, collMod, compact, createCollection, dropCollection,"
					+ " listIndexes, listSearchIndexes, createSearchIndexes, updateSearchIndex, dropSearchIndex, collStats, dbStats,"
					+ " renameCollectionSameDB and changeStream");
			}
		}
		List<Expr> items = new ArrayList<>();
		result.forEach(action -> items.add(text(action)));
		return items;
	}

	private static String actionName(String privilege) {
		for (String action : List.of("find", "insert", "update", "remove", "createIndex", "dropIndex", "collMod", "compact", "createCollection",
				"dropCollection", "listIndexes", "listSearchIndexes", "createSearchIndexes", "updateSearchIndex", "dropSearchIndex", "collStats", "dbStats",
				"renameCollectionSameDB", "changeStream")) {
			if (action.equalsIgnoreCase(privilege)) {
				return action;
			}
		}
		throw new IllegalArgumentException(privilege);
	}

	/** Seconds of a TTL written as an integer of seconds or as a number with the unit s, m, h, d or w. */
	public static long seconds(String interval) {
		var matcher = INTERVAL.matcher(interval.trim());
		if (!matcher.matches()) {
			throw Unsupported.of("the interval " + interval);
		}
		long value = Long.parseLong(matcher.group(1));
		return switch (matcher.group(2)) {
		case "ms" -> value / 1000;
		case "s" -> value;
		case "m" -> value * 60;
		case "h" -> value * 3600;
		case "d" -> value * 86400;
		default -> value * 604800;
		};
	}

	/** The write of a conflict policy: the runtime operation, the counter it reports, and whether duplicate keys are skipped. */
	public static Map<String, String> writeMode(String policy) {
		return switch (policy) {
		case "error" -> Map.of("op", "insert", "counter", "insert_count");
		case "ignore" -> Map.of("op", "insert", "counter", "insert_count", "ignore", "true");
		case "update" -> Map.of("op", "merge", "counter", "upsert_count");
		default -> Map.of("op", "replace", "counter", "upsert_count");
		};
	}
}
