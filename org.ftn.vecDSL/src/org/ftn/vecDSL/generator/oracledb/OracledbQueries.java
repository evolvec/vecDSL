package org.ftn.vecDSL.generator.oracledb;

import static org.ftn.vecDSL.generator.oracledb.OracledbSql.col;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.id;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.number;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.object;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.output;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.source;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.table;
import static org.ftn.vecDSL.generator.oracledb.OracledbSql.whereClause;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Column;
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Query;
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Table;

/**
 * Query statements over one table, each returning one JSON object (or one JSON value) per row. Nearest-neighbour candidates are
 * taken in a CTE ordered by VECTOR_DISTANCE with FETCH APPROX (served by the field's vector index, with the target accuracy of the
 * search) or FETCH EXACT, and scores, offset, bounds and the final order are applied outside it: the AI Vector Search User's
 * Guide's approximate search takes no OFFSET.
 */
public final class OracledbQueries {

	private OracledbQueries() {
	}

	/** Result fields beyond id and score; with vectors, the vector columns too. */
	public record Projection(List<String> output, List<String> vectors) {
	}

	/**
	 * How a search fetches its candidates: approximately through the vector index (with a target accuracy in percent or index
	 * parameters such as EFSEARCH k), or exactly.
	 */
	public record Fetch(boolean approximate, Integer accuracy, String parameters) {

		public static final Fetch EXACT = new Fetch(false, null, null);

		/** FETCH APPROX FIRST n ROWS ONLY WITH TARGET ACCURACY 90, or FETCH EXACT FIRST n ROWS ONLY. */
		public String clause(int rows) {
			if (!approximate) return " FETCH EXACT FIRST " + rows + " ROWS ONLY";
			String target = accuracy != null ? " WITH TARGET ACCURACY " + accuracy
					: parameters != null ? " WITH TARGET ACCURACY PARAMETERS (" + parameters + ")" : "";
			return " FETCH APPROX FIRST " + rows + " ROWS ONLY" + target;
		}
	}

	/**
	 * A nearest-neighbour search: the vector column and metric, the query vector as SQL (a bound TO_VECTOR or a scalar subquery),
	 * CTEs it needs, limit and offset, the WHERE condition, the projection, native or canonical scores, bounds on the score, and
	 * the fetch clause. The query vector expression is written twice (select list and ORDER BY), so it is a function of the
	 * query that binds a fresh parameter on each call.
	 */
	public record Search(Table table, Column vector, String metric, java.util.function.Supplier<String> query, String prelude, int limit,
			int offset, String where, Projection projection, boolean nativeScore, Expr min, Expr max, Fetch fetch) {
	}

	public static String vectorSearch(Query q, Search s) {
		String prelude = s.prelude() == null ? "" : s.prelude() + ", ";
		String carried = carried(s.table(), s.projection()).values().stream().collect(Collectors.joining(", "));
		String column = col(s.vector().name());
		String distance = OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric());
		String condition = OracledbSql.and(List.of(column + " IS NOT NULL", s.where() == null ? "" : s.where()));
		String order = OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric());
		String hits = "hits AS (SELECT " + carried + ", " + distance + " AS distance FROM " + source(q, s.table()) + " " + OracledbSql.ALIAS
				+ whereClause(condition) + " ORDER BY " + order + fetchOf(s.table(), s.fetch()).clause(s.limit() + s.offset()) + ")";
		String score = s.nativeScore() ? "h.distance" : OracledbSql.similarity(s.metric(), "h.distance");
		String page = "SELECT h.* FROM hits h ORDER BY h.distance, h." + id(s.table().key()) + " OFFSET " + s.offset() + " ROWS";
		return "WITH " + prelude + hits + " SELECT " + hitObject(s.table(), s.projection(), score) + " FROM (" + page + ") h"
				+ whereClause(bounds(q, score, s.min(), s.max())) + " ORDER BY h.distance, h." + id(s.table().key());
	}

	/** Columns a CTE carries for the projection, as select expressions over t, keyed by name. */
	private static Map<String, String> carried(Table table, Projection projection) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(table.key(), col(table.key()));
		for (String name : projection.output()) result.put(name, col(table.column(name).name()));
		for (String name : projection.vectors()) result.put(name, col(table.column(name).name()));
		return result;
	}

	/** The hit object: id, score, then the output fields and vectors, read from the alias h. */
	private static String hitObject(Table table, Projection projection, String score) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", "h." + id(table.key()));
		fields.put("score", score);
		for (String name : projection.output()) fields.put(name, output(table.column(name), "h"));
		for (String name : projection.vectors()) fields.put(name, output(table.column(name), "h"));
		return object(fields);
	}

	/**
	 * The fetch of a search: as given, or exact for a read as of a past time, because a vector index holds the current rows and the
	 * AI Vector Search User's Guide documents approximate search on current data only.
	 */
	private static Fetch fetchOf(Table table, Fetch fetch) {
		return table.flashback() == null ? fetch : Fetch.EXACT;
	}

	private static String bounds(Query q, String score, Expr min, Expr max) {
		List<String> parts = new ArrayList<>();
		if (min != null) parts.add(score + " >= " + q.bind(min, "number"));
		if (max != null) parts.add(score + " <= " + q.bind(max, "number"));
		return OracledbSql.and(parts);
	}

	/** A field searched by keywords: the full-text column and its boost. */
	public record KeywordField(Column column, double boost) {
	}

	/** Keyword search: Oracle Text SCORE over the fields' CONTAINS conditions (weighted sum with boosts), matching any field. */
	public record Keywords(Table table, List<KeywordField> fields, String op, Expr text, int limit, int offset, String where,
			Projection projection, Expr min, Expr max, boolean fuzzy, Integer span) {
	}

	public static String keywordSearch(Query q, Keywords k) {
		String[] parts = keywordParts(q, k.fields(), k.op(), k.text(), k.fuzzy(), k.span(), new int[] { 0 });
		String condition = OracledbSql.and(List.of(parts[1], k.where() == null ? "" : k.where()));
		String carried = carried(k.table(), k.projection()).values().stream().collect(Collectors.joining(", "));
		return "WITH hits AS (SELECT " + carried + ", " + parts[0] + " AS score FROM " + source(q, k.table()) + " " + OracledbSql.ALIAS
				+ whereClause(condition) + " ORDER BY score DESC, " + col(k.table().key()) + " OFFSET " + k.offset() + " ROWS FETCH NEXT "
				+ k.limit() + " ROWS ONLY) SELECT " + hitObject(k.table(), k.projection(), "h.score") + " FROM hits h"
				+ whereClause(bounds(q, "h.score", k.min(), k.max())) + " ORDER BY h.score DESC, h." + id(k.table().key());
	}

	/** The score expression and the match condition of keyword fields; labels continue from the counter. */
	private static String[] keywordParts(Query q, List<KeywordField> fields, String op, Expr text, boolean fuzzy, Integer span, int[] label) {
		List<String> scores = new ArrayList<>();
		List<String> matches = new ArrayList<>();
		for (KeywordField field : fields) {
			int n = ++label[0];
			String contains = "CONTAINS(" + col(field.column().name()) + ", " + q.bind(OracledbSql.textQuery(text, op, field.column(), fuzzy, span), "string")
					+ ", " + n + ") > 0";
			scores.add(field.boost() == 1.0 ? "SCORE(" + n + ")" : number(field.boost()) + " * SCORE(" + n + ")");
			matches.add(contains);
		}
		String condition = matches.size() == 1 ? matches.get(0) : matches.stream().map(m -> "(" + m + ")").collect(Collectors.joining(" OR "));
		return new String[] { scores.size() == 1 ? scores.get(0) : "(" + String.join(" + ", scores) + ")", condition };
	}

	/** One leg of a hybrid search: a vector leg (vector column, metric, query SQL, fetch) or a keyword leg (fields, op, text). */
	public record Leg(Column vector, String metric, java.util.function.Supplier<String> query, Fetch fetch, List<KeywordField> fields,
			String op, Expr text, int limit, String where, double weight) {
	}

	/**
	 * Hybrid search fused in SQL, as on the pgvector target: rrf sum of 1 / (k + rank); weighted sum of weight times
	 * similarity; relative_score sum of weight times the min-max normalized leg score. Ties order by key.
	 */
	public record Hybrid(Table table, List<Leg> legs, String fusion, int k, String prelude, int limit, int offset, Projection projection,
			Expr min, Expr max) {
	}

	public static String hybridSearch(Query q, Hybrid h) {
		List<String> ctes = fusion(q, h);
		String key = col(h.table().key());
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", key);
		fields.put("score", "f.score");
		for (String name : h.projection().output()) fields.put(name, output(h.table().column(name), OracledbSql.ALIAS));
		for (String name : h.projection().vectors()) fields.put(name, output(h.table().column(name), OracledbSql.ALIAS));
		return "WITH " + String.join(", ", ctes) + " SELECT " + object(fields) + " FROM f JOIN " + source(q, h.table()) + " " + OracledbSql.ALIAS
				+ " ON " + key + " = f.key" + whereClause(bounds(q, "f.score", h.min(), h.max())) + " ORDER BY f.score DESC, " + key + " OFFSET "
				+ h.offset() + " ROWS FETCH NEXT " + h.limit() + " ROWS ONLY";
	}

	/** The leg CTEs, the ranked union r and the fused scores f (key, score) of a hybrid search. */
	private static List<String> fusion(Query q, Hybrid h) {
		List<String> ctes = new ArrayList<>();
		if (h.prelude() != null) ctes.add(h.prelude());
		List<String> ranked = new ArrayList<>();
		String key = col(h.table().key());
		int[] label = { 0 };
		for (int i = 0; i < h.legs().size(); i++) {
			Leg leg = h.legs().get(i);
			if (leg.vector() != null) {
				String column = col(leg.vector().name());
				String distance = OracledbSql.distance(column, leg.query().get(), leg.vector().vec(), leg.metric());
				String condition = OracledbSql.and(List.of(column + " IS NOT NULL", leg.where() == null ? "" : leg.where()));
				String order = OracledbSql.distance(column, leg.query().get(), leg.vector().vec(), leg.metric());
				ctes.add("l" + i + " AS (SELECT " + key + " AS key, " + OracledbSql.similarity(leg.metric(), "(" + distance + ")") + " AS score FROM "
						+ source(q, h.table()) + " " + OracledbSql.ALIAS + whereClause(condition) + " ORDER BY " + order + fetchOf(h.table(), leg.fetch()).clause(leg.limit()) + ")");
			} else {
				String[] parts = keywordParts(q, leg.fields(), leg.op(), leg.text(), false, null, label);
				String condition = OracledbSql.and(List.of(parts[1], leg.where() == null ? "" : leg.where()));
				ctes.add("l" + i + " AS (SELECT " + key + " AS key, " + parts[0] + " AS score FROM " + source(q, h.table()) + " " + OracledbSql.ALIAS
						+ whereClause(condition) + " ORDER BY score DESC, " + key + " FETCH FIRST " + leg.limit() + " ROWS ONLY)");
			}
			ranked.add("SELECT key, score, " + i + " AS leg, ROW_NUMBER() OVER (ORDER BY score DESC, key) AS rnk FROM l" + i);
		}
		ctes.add("r AS (" + String.join(" UNION ALL ", ranked) + ")");
		String weight = "CASE leg " + IntStream.range(0, h.legs().size()).mapToObj(i -> "WHEN " + i + " THEN " + number(h.legs().get(i).weight()))
				.collect(Collectors.joining(" ")) + " END";
		switch (h.fusion()) {
		case "rrf" -> ctes.add("f AS (SELECT key, SUM(1 / (" + h.k() + " + rnk)) AS score FROM r GROUP BY key)");
		case "weighted" -> ctes.add("f AS (SELECT key, SUM(" + weight + " * score) AS score FROM r GROUP BY key)");
		case "relative_score" -> {
			ctes.add("n AS (SELECT key, leg, CASE WHEN MAX(score) OVER (PARTITION BY leg) > MIN(score) OVER (PARTITION BY leg) THEN "
					+ "(score - MIN(score) OVER (PARTITION BY leg)) / (MAX(score) OVER (PARTITION BY leg) - MIN(score) OVER (PARTITION BY leg)) "
					+ "ELSE 1 END AS score FROM r)");
			ctes.add("f AS (SELECT key, SUM(" + weight + " * score) AS score FROM n GROUP BY key)");
		}
		default -> throw new IllegalArgumentException(h.fusion());
		}
		return ctes;
	}

	/**
	 * The CTE of a recommendation query vector (the average-vector strategy of the other generators): the average of the positive
	 * examples moved away from the average of the negatives, p + (p - n), with AVG over vectors and vector arithmetic (AI Vector
	 * Search User's Guide, AVG and arithmetic operators), cast back to the column's format. Examples are ids looked up in the
	 * lookup table or vector literals.
	 */
	public static String recommendVector(Query q, Table lookup, Column vector, List<Expr> positiveIds, List<Expr> positiveVectors,
			List<Expr> negativeIds, List<Expr> negativeVectors) {
		if (vector.vec().sparse() || vector.vec().format().equals("BINARY")) {
			throw Unsupported.of("recommendation on sparse or binary fields; Oracle averages dense vectors (AVG returns FLOAT64)");
		}
		String positive = "(SELECT AVG(v) AS v FROM (" + examples(q, lookup, vector, positiveIds, positiveVectors) + ")) p";
		if (negativeIds.isEmpty() && negativeVectors.isEmpty()) return "q AS (SELECT " + vector.vec().of("p.v") + " AS v FROM " + positive + ")";
		String negative = "(SELECT AVG(v) AS v FROM (" + examples(q, lookup, vector, negativeIds, negativeVectors) + ")) n";
		return "q AS (SELECT " + vector.vec().of("p.v + (p.v - n.v)") + " AS v FROM " + positive + ", " + negative + ")";
	}

	private static String examples(Query q, Table lookup, Column vector, List<Expr> ids, List<Expr> vectors) {
		List<String> parts = new ArrayList<>();
		if (!ids.isEmpty()) {
			parts.add("SELECT x." + id(vector.name()) + " AS v FROM " + source(q, lookup) + " x WHERE x." + id(lookup.key()) + " IN ("
					+ q.list(Code.items(ids), lookup.keyColumn().kind()) + ")");
		}
		for (Expr value : vectors) parts.add("SELECT " + q.vector(value, vector.vec()) + " AS v FROM dual");
		return String.join(" UNION ALL ", parts);
	}

	// ---------------------------------------------------------------- result shapes

	/** A field a sort key, group key, aggregation or boost reads: a column, or a typed sub-field of a JSON column. */
	public record Ref(Column column, List<String> path, String kind) {

		public String sql(String alias) {
			return OracledbSql.reference(alias, column, path, kind);
		}
	}

	/** A sort key of the returned page: a field, or the relevance when the field is null. */
	public record Order(Ref field, boolean descending) {
	}

	/** Group-by: the group key and the number of hits kept per group. */
	public record Group(Ref field, int size) {
	}

	/** Maximal marginal relevance: lambda, the candidate pool, and the vector column and metric of the diversity term. */
	public record Mmr(double lambda, int candidates, Column vector, String metric) {
	}

	/** An aggregation over the matching records: result name, kind, field, histogram interval and bucket count. */
	public record Aggregate(String name, String kind, Ref field, Expr interval, int size) {
	}

	/** One boost function. */
	public record BoostFn(String kind, Ref field, double weight, String shape, Expr origin, Expr scale, Expr offset, double rate,
			List<OracledbSql.Cond> when) {
	}

	public record Boost(List<BoostFn> functions, String combine, String apply) {
	}

	/** A keyword input whose terms a highlight marks. */
	public record Terms(String op, Expr text, boolean fuzzy, Integer span) {
	}

	/**
	 * The shape of a search result beyond a plain hit list: a keyset cursor, boosts, MMR, groups, the total, aggregations,
	 * highlighted full-text columns and sort keys.
	 */
	public record Shape(Expr cursor, Boost boost, Mmr mmr, Group group, boolean total, List<Aggregate> aggregations,
			List<Column> highlights, List<Terms> terms, List<Order> sort) {

		public boolean plain() {
			return cursor == null && boost == null && mmr == null && group == null && !total && aggregations.isEmpty()
					&& highlights.isEmpty() && sort.isEmpty();
		}

		public boolean wrapped() {
			return total || !aggregations.isEmpty();
		}

		/**
		 * Candidates a search scores: limit plus offset, or the MMR pool; null (every match) when boosts or groups rank all matches,
		 * when a total or aggregations count them (Oracle binds each value once, so the filter is not written a second time for
		 * the count), or when a keyword search continues after a cursor (SCORE is read in the select list, not in a condition).
		 */
		Integer pool(int fetch, boolean keyword) {
			if (boost != null || group != null || wrapped() || (keyword && cursor != null)) return null;
			return mmr == null ? fetch : Math.max(mmr.candidates(), fetch);
		}
	}

	/** The PL/SQL block that makes CTX_DOC.SNIPPET read its text key as a ROWID in this session (Text Reference, SET_KEY_TYPE). */
	public static final String ROWID_KEYS = "BEGIN CTX_DOC.SET_KEY_TYPE('ROWID'); END;";

	private static final String ROW = OracledbSql.ALIAS + ".ROWID AS rid";

	/** A prefetch stage: the ROWIDs of the stage's nearest rows by its vector column, metric and query. */
	public static String stage(Query q, String name, Table table, Column vector, String metric, String query, String where, int limit) {
		String column = col(vector.name());
		String condition = OracledbSql.and(List.of(column + " IS NOT NULL", where == null ? "" : where));
		return name + " AS (SELECT " + ROW + " FROM " + source(q, table) + " " + OracledbSql.ALIAS + whereClause(condition) + " ORDER BY "
				+ OracledbSql.distance(column, query, vector.vec(), metric) + ", " + col(table.key()) + " FETCH FIRST " + limit + " ROWS ONLY)";
	}

	/** The condition that a row is among those the stages return; null without stages. */
	public static String within(List<String> stages) {
		return stages.isEmpty() ? null
				: OracledbSql.ALIAS + ".ROWID IN (" + stages.stream().map(s -> "SELECT rid FROM " + s).collect(Collectors.joining(" UNION ALL ")) + ")";
	}

	/** The rows of the table (alias t) that the rows of a stage (alias, column rid) name. */
	private static String joined(Query q, Table table, String alias) {
		return " JOIN " + source(q, table) + " " + OracledbSql.ALIAS + " ON " + OracledbSql.ALIAS + ".ROWID = " + alias + ".rid";
	}

	/**
	 * (ord, key) after (afterOrd, afterKey) in ascending order; Oracle compares row values only for equality. The order expression
	 * is written twice, so it is a function that binds fresh parameters on each call.
	 */
	private static String after(Supplier<String> ord, String key, String afterOrd, String afterKey) {
		return "(" + ord.get() + " > " + afterOrd + " OR (" + ord.get() + " = " + afterOrd + " AND " + key + " > " + afterKey + "))";
	}

	/** A nearest-neighbour search with a result shape; a plain shape gives the statement of vectorSearch(q, s). */
	public static String vectorSearch(Query q, Search s, Shape shape) {
		if (shape.plain()) return vectorSearch(q, s);
		String column = col(s.vector().name());
		String key = col(s.table().key());
		List<String> conditions = new ArrayList<>(List.of(column + " IS NOT NULL", s.where() == null ? "" : s.where()));
		Integer pool = shape.pool(s.limit() + s.offset(), false);
		List<String> ctes = new ArrayList<>();
		if (s.prelude() != null) ctes.add(s.prelude());
		boolean early = shape.cursor() != null && pool != null;
		if (early) {
			ctes.add("cursor_row AS (SELECT " + OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric()) + " AS ord, " + key
					+ " AS key FROM " + source(q, s.table()) + " " + OracledbSql.ALIAS + " WHERE " + key + " = "
					+ q.bind(shape.cursor(), s.table().keyColumn().kind()) + ")");
			conditions.add(after(() -> OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric()), key, "(SELECT ord FROM cursor_row)",
					"(SELECT key FROM cursor_row)"));
		}
		String distance = OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric());
		String order = OracledbSql.distance(column, s.query().get(), s.vector().vec(), s.metric());
		ctes.add("candidates AS (SELECT " + key + " AS key, " + ROW + ", " + distance + " AS distance FROM " + source(q, s.table()) + " "
				+ OracledbSql.ALIAS + whereClause(OracledbSql.and(conditions)) + " ORDER BY " + order
				+ (pool == null ? "" : fetchOf(s.table(), s.fetch()).clause(pool)) + ")");
		String score = s.nativeScore() ? "distance" : OracledbSql.similarity(s.metric(), "distance");
		ctes.add("scored AS (SELECT key, rid, " + score + " AS score, distance AS ord, " + OracledbSql.similarity(s.metric(), "distance")
				+ " AS rel FROM candidates)");
		return shaped(q, s.table(), ctes, early, s.limit(), s.offset(), s.projection(), s.min(), s.max(), shape);
	}

	/** A keyword search with a result shape; a plain shape gives the statement of keywordSearch(q, k). */
	public static String keywordSearch(Query q, Keywords k, Shape shape) {
		if (shape.plain()) return keywordSearch(q, k);
		String[] parts = keywordParts(q, k.fields(), k.op(), k.text(), k.fuzzy(), k.span(), new int[] { 0 });
		String key = col(k.table().key());
		Integer pool = shape.pool(k.limit() + k.offset(), true);
		List<String> ctes = new ArrayList<>();
		ctes.add("candidates AS (SELECT " + key + " AS key, " + ROW + ", " + parts[0] + " AS score FROM " + source(q, k.table()) + " "
				+ OracledbSql.ALIAS + whereClause(OracledbSql.and(List.of(parts[1], k.where() == null ? "" : k.where()))) + " ORDER BY score DESC, "
				+ key + (pool == null ? "" : " FETCH FIRST " + pool + " ROWS ONLY") + ")");
		ctes.add("scored AS (SELECT key, rid, score, -score AS ord, score AS rel FROM candidates)");
		return shaped(q, k.table(), ctes, false, k.limit(), k.offset(), k.projection(), k.min(), k.max(), shape);
	}

	/** A hybrid search with a result shape; a plain shape gives the statement of hybridSearch(q, h). */
	public static String hybridSearch(Query q, Hybrid h, Shape shape) {
		if (shape.plain()) return hybridSearch(q, h);
		List<String> ctes = new ArrayList<>(fusion(q, h));
		String key = col(h.table().key());
		ctes.add("scored AS (SELECT f.key, " + ROW + ", f.score, -f.score AS ord, f.score AS rel FROM f JOIN " + source(q, h.table()) + " "
				+ OracledbSql.ALIAS + " ON " + key + " = f.key)");
		return shaped(q, h.table(), ctes, false, h.limit(), h.offset(), h.projection(), h.min(), h.max(), shape);
	}

	/**
	 * The stages after the scored candidates (CTE scored: key, rid, score, ord ascending from the best, rel the canonical
	 * relevance), as on the pgvector target: boosts re-score, a cursor continues after its record, MMR, groups or limit and
	 * offset select the page, score bounds filter it, sort keys order it, and total and aggregations count the matching records.
	 */
	private static String shaped(Query q, Table table, List<String> ctes, boolean cursorApplied, int limit, int offset, Projection projection,
			Expr min, Expr max, Shape shape) {
		String last = "scored";
		if (shape.boost() != null) {
			Boost boost = shape.boost();
			List<String> values = new ArrayList<>();
			for (int i = 0; i < boost.functions().size(); i++) values.add(boostValue(q, table, boost.functions().get(i)) + " AS f" + i);
			List<String> names = IntStream.range(0, values.size()).mapToObj(i -> "b.f" + i).toList();
			ctes.add("boosted AS (SELECT key, rid, score, -score AS ord, score AS rel FROM (SELECT b.key, b.rid, "
					+ apply(boost.apply(), "b.rel", combine(boost.combine(), names)) + " AS score FROM (SELECT x.key, x.rid, x.rel, "
					+ String.join(", ", values) + " FROM scored x" + joined(q, table, "x") + ") b))");
			last = "boosted";
		}
		String matched = last;
		if (shape.cursor() != null && !cursorApplied) {
			ctes.add("cursor_row AS (SELECT ord, key FROM " + last + " WHERE key = " + q.bind(shape.cursor(), table.keyColumn().kind()) + ")");
			ctes.add("after_cursor AS (SELECT x.* FROM " + last + " x WHERE "
					+ after(() -> "x.ord", "x.key", "(SELECT ord FROM cursor_row)", "(SELECT key FROM cursor_row)") + ")");
			last = "after_cursor";
		}
		String hit = hitObject(q, table, projection, shape);
		String rows;
		if (shape.group() != null) {
			String groupKey = shape.group().field().sql(OracledbSql.ALIAS);
			ctes.add("grouped AS (SELECT x.key, x.rid, x.score, x.ord, " + groupKey + " AS grp, ROW_NUMBER() OVER (PARTITION BY " + groupKey
					+ " ORDER BY x.ord, x.key) AS member FROM " + last + " x" + joined(q, table, "x") + ")");
			ctes.add("leaders AS (SELECT grp, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM grouped WHERE member = 1 ORDER BY ord, key OFFSET "
					+ offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY)");
			ctes.add("page AS (SELECT g.key, g.rid, g.score, g.ord, g.grp, l.pos, g.member FROM grouped g JOIN leaders l ON DECODE(l.grp, g.grp, 1, 0) = 1"
					+ " WHERE g.member <= " + shape.group().size() + ")");
			rows = "hits AS (SELECT JSON_OBJECT('group' VALUE x.grp, 'hits' VALUE JSON_ARRAYAGG(" + hit
					+ " FORMAT JSON ORDER BY x.member RETURNING CLOB) FORMAT JSON NULL ON NULL RETURNING CLOB) AS hit, x.pos AS n FROM page x"
					+ joined(q, table, "x") + whereClause(bounds(q, "x.score", min, max)) + " GROUP BY x.pos, x.grp)";
		} else {
			if (shape.mmr() != null) {
				mmr(table, ctes, last, shape.mmr(), limit, offset, q);
			} else {
				ctes.add("page AS (SELECT key, rid, score, ord, ROW_NUMBER() OVER (ORDER BY ord, key) AS pos FROM " + last
						+ " ORDER BY ord, key OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY)");
			}
			List<String> order = new ArrayList<>();
			for (Order o : shape.sort()) {
				order.add(o.field() == null ? "x.ord" + (o.descending() ? "" : " DESC")
						: o.field().sql(OracledbSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
			}
			order.add("x.pos");
			rows = "hits AS (SELECT " + hit + " AS hit, ROW_NUMBER() OVER (ORDER BY " + String.join(", ", order) + ") AS n FROM page x"
					+ joined(q, table, "x") + whereClause(bounds(q, "x.score", min, max)) + ")";
		}
		ctes.add(rows);
		if (!shape.wrapped()) return "WITH " + String.join(", ", ctes) + " SELECT hit FROM hits ORDER BY n";
		ctes.add("matched AS (SELECT " + OracledbSql.ALIAS + ".* FROM " + matched + " x" + joined(q, table, "x")
				+ whereClause(bounds(q, "x.score", min, max)) + ")");
		Map<String, String> result = new LinkedHashMap<>();
		result.put("hits", "NVL((SELECT JSON_ARRAYAGG(hit FORMAT JSON ORDER BY n RETURNING CLOB) FROM hits), TO_CLOB('[]')) FORMAT JSON");
		if (shape.total()) result.put("total", "(SELECT COUNT(*) FROM matched)");
		if (!shape.aggregations().isEmpty()) {
			Map<String, String> aggregations = new LinkedHashMap<>();
			for (Aggregate a : shape.aggregations()) aggregations.put(a.name(), aggregate(q, a));
			result.put("aggregations", object(aggregations));
		}
		return "WITH " + String.join(", ", ctes) + " SELECT " + object(result) + " FROM dual";
	}

	/**
	 * MMR as a recursive subquery (SQL Language Reference, SELECT, recursive subquery factoring: the recursive member may not
	 * aggregate or query its own name in a subquery, but analytic functions are permitted). Each step carries every pool row with
	 * the step it was picked at and its highest similarity to the picked rows, and names the next pick with FIRST_VALUE; the
	 * following step joins that pick's vector. The page is the picks after the offset, in pick order.
	 */
	private static void mmr(Table table, List<String> ctes, String last, Mmr mmr, int limit, int offset, Query q) {
		String v = id(mmr.vector().name());
		String lambda = number(mmr.lambda());
		String rest = number(complement(mmr.lambda()));
		ctes.add("pool AS (SELECT x.key, x.rid, x.score, x.ord, x.rel, " + OracledbSql.ALIAS + "." + v + " AS v FROM " + last + " x" + joined(q, table, "x")
				+ " WHERE " + OracledbSql.ALIAS + "." + v + " IS NOT NULL ORDER BY x.ord, x.key FETCH FIRST " + mmr.candidates() + " ROWS ONLY)");
		String similarity = OracledbSql.similarity(mmr.metric(), "(" + OracledbSql.distance("d.v", "c.v", mmr.vector().vec(), mmr.metric()) + ")");
		String pickedAt = "CASE WHEN p.picked IS NULL AND p.key = p.chosen THEN p.n + 1 ELSE p.picked END";
		String nearest = "CASE WHEN p.nearest IS NULL OR " + similarity + " > p.nearest THEN " + similarity + " ELSE p.nearest END";
		ctes.add("picks (n, key, rid, score, ord, rel, picked, nearest, chosen) AS (SELECT 0, key, rid, score, ord, rel, CAST(NULL AS NUMBER), "
				+ "CAST(NULL AS BINARY_DOUBLE), FIRST_VALUE(key) OVER (ORDER BY " + lambda + " * rel DESC, ord, key) FROM pool UNION ALL SELECT p.n + 1, p.key, "
				+ "p.rid, p.score, p.ord, p.rel, " + pickedAt + ", " + nearest + ", FIRST_VALUE(p.key) OVER (ORDER BY CASE WHEN " + pickedAt
				+ " IS NULL THEN 0 ELSE 1 END, " + lambda + " * p.rel - " + rest + " * NVL(" + nearest + ", 0) DESC, p.ord, p.key) FROM picks p "
				+ "JOIN pool c ON c.key = p.chosen JOIN pool d ON d.key = p.key WHERE p.n < " + (limit + offset) + ")");
		ctes.add("page AS (SELECT key, rid, score, ord, picked AS pos FROM picks WHERE n = " + (limit + offset) + " AND picked > " + offset + ")");
	}

	/** The hit object of a shaped search: id, score, output fields and vectors read from t, then the highlights. */
	private static String hitObject(Query q, Table table, Projection projection, Shape shape) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", col(table.key()));
		fields.put("score", "x.score");
		for (String name : projection.output()) fields.put(name, output(table.column(name), OracledbSql.ALIAS));
		for (String name : projection.vectors()) fields.put(name, output(table.column(name), OracledbSql.ALIAS));
		if (!shape.highlights().isEmpty()) {
			Map<String, String> marked = new LinkedHashMap<>();
			for (Column column : shape.highlights()) marked.put(column.name(), "JSON_ARRAY(" + snippet(q, table, column, shape.terms()) + ")");
			fields.put("highlight", object(marked) + " FORMAT JSON");
		}
		return object(fields);
	}

	/**
	 * CTX_DOC.SNIPPET of a full-text column for the text queries of the search (OR of each input's query): the column's CONTEXT
	 * index found in USER_INDEXES and USER_IND_COLUMNS, the row's ROWID as the text key, Oracle's default markers. The index name is
	 * passed in double quotation marks: CTX_DOC folds an unquoted name to upper case, and the generator creates case-sensitive
	 * names (otherwise DRG-10502 "index books_title_text does not exist").
	 */
	private static String snippet(Query q, Table table, Column column, List<Terms> terms) {
		String index = "(SELECT MIN(i.index_name) FROM user_indexes i JOIN user_ind_columns c ON c.index_name = i.index_name WHERE i.table_name = "
				+ q.bind(Code.text(table.name()), "string") + " AND c.column_name = " + q.bind(Code.text(column.name()), "string")
				+ " AND i.ityp_name = 'CONTEXT')";
		String query = terms.size() == 1 ? q.bind(textQuery(terms.get(0), column), "string")
				: terms.stream().map(t -> "'(' || " + q.bind(textQuery(t, column), "string") + " || ')'")
						.collect(Collectors.joining(" || ' OR ' || "));
		return "CTX_DOC.SNIPPET('\"' || " + index + " || '\"', ROWIDTOCHAR(" + OracledbSql.ALIAS + ".ROWID), " + query + ")";
	}

	private static Expr textQuery(Terms terms, Column column) {
		return OracledbSql.textQuery(terms.text(), terms.op(), column, terms.fuzzy(), terms.span());
	}

	/** An aggregation over the matching records (alias m), as one JSON value. */
	private static String aggregate(Query q, Aggregate a) {
		boolean array = a.field().path().isEmpty() && a.field().column().array();
		String from = array ? " FROM matched m CROSS APPLY JSON_TABLE(" + a.field().sql("m") + ", '$[*]' COLUMNS (v "
				+ OracledbSql.jsonTableType(a.field().column().element()) + " PATH '$')) e" : " FROM matched m";
		String v = array ? "e.v" : a.field().sql("m");
		return switch (a.kind()) {
		case "count" -> "(SELECT COUNT(" + v + ")" + from + ")";
		case "sum", "avg", "min", "max" -> "(SELECT " + a.kind().toUpperCase() + "(" + v + ")" + from + ")";
		case "distinct_count", "cardinality" -> "(SELECT COUNT(DISTINCT " + v + ")" + from + ")";
		case "stats" -> "(SELECT JSON_OBJECT('count' VALUE COUNT(" + v + "), 'min' VALUE MIN(" + v + "), 'max' VALUE MAX(" + v + "), 'avg' VALUE AVG(" + v
				+ "), 'sum' VALUE SUM(" + v + ") NULL ON NULL RETURNING CLOB)" + from + ") FORMAT JSON";
		case "terms", "facet" -> "(SELECT NVL(JSON_ARRAYAGG(JSON_OBJECT('key' VALUE b.k, 'count' VALUE b.n) ORDER BY b.n DESC, b.k RETURNING CLOB), TO_CLOB('[]')) FROM (SELECT "
				+ v + " AS k, COUNT(*) AS n" + from + " WHERE " + v + " IS NOT NULL GROUP BY " + v + " ORDER BY COUNT(*) DESC, " + v + " FETCH FIRST "
				+ a.size() + " ROWS ONLY) b) FORMAT JSON";
		case "histogram" -> buckets("FLOOR(" + v + " / w.w) * w.w", v, from + " CROSS JOIN (SELECT " + q.bind(a.interval(), "number") + " AS w FROM dual) w");
		case "date_histogram" -> buckets("CAST(DATE '1970-01-01' AS TIMESTAMP) + NUMTODSINTERVAL(FLOOR((CAST(" + v
				+ " AS DATE) - DATE '1970-01-01') * 86400 / w.w) * w.w, 'SECOND')", v, from + " CROSS JOIN (SELECT " + q.bind(Code.num(seconds(a.interval())), "number")
				+ " AS w FROM dual) w");
		case "percentiles" -> "(SELECT JSON_OBJECT(" + PERCENTS.stream().map(p -> OracledbSql.literal(number(p)) + " VALUE PERCENTILE_CONT(" + number(p / 100)
				+ ") WITHIN GROUP (ORDER BY " + v + ")").collect(Collectors.joining(", ")) + " NULL ON NULL RETURNING CLOB)" + from + ") FORMAT JSON";
		default -> throw new IllegalArgumentException(a.kind());
		};
	}

	/** Elasticsearch's default percents, so that the percentile aggregation reads the same on every target. */
	private static final List<Double> PERCENTS = List.of(1.0, 5.0, 25.0, 50.0, 75.0, 95.0, 99.0);

	private static String buckets(String key, String v, String from) {
		return "(SELECT NVL(JSON_ARRAYAGG(JSON_OBJECT('key' VALUE b.k, 'count' VALUE b.n) ORDER BY b.k RETURNING CLOB), TO_CLOB('[]')) FROM (SELECT k, COUNT(*) AS n FROM (SELECT "
				+ key + " AS k" + from + " WHERE " + v + " IS NOT NULL) GROUP BY k) b) FORMAT JSON";
	}

	/** Seconds of a fixed interval: a number with s, m, h, d or w (Elasticsearch's fixed units, and weeks). */
	static double seconds(Expr interval) {
		String text = interval instanceof Code.Text t ? t.value().trim() : "";
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(s|m|h|d|w)").matcher(text);
		if (!m.matches()) {
			throw Unsupported.of("the interval '" + text + "'; the Oracle generator buckets times by a fixed interval in seconds, minutes, hours, days or "
					+ "weeks (for example 1d), and calendar months and years have no fixed length");
		}
		double unit = switch (m.group(2)) {
		case "s" -> 1;
		case "m" -> 60;
		case "h" -> 3600;
		case "d" -> 86400;
		default -> 604800;
		};
		return Double.parseDouble(m.group(1)) * unit;
	}

	/** 1 - x in decimal arithmetic, so that 1 - 0.7 prints as 0.3. */
	private static double complement(double x) {
		return BigDecimal.ONE.subtract(BigDecimal.valueOf(x)).doubleValue();
	}

	/**
	 * The combined value of boost function columns (null where none applies): Oracle has no NUM_NONNULLS, and GREATEST is null when
	 * one argument is, so each argument of the maximum is replaced by any non-null value when it is null.
	 */
	private static String combine(String mode, List<String> values) {
		if (values.size() == 1) return values.get(0);
		String some = "(" + values.stream().map(v -> "NVL2(" + v + ", 1, 0)").collect(Collectors.joining(" + ")) + ")";
		return switch (mode) {
		case "sum" -> "CASE WHEN " + some + " > 0 THEN " + values.stream().map(v -> "NVL(" + v + ", 0)").collect(Collectors.joining(" + ")) + " END";
		case "multiply" -> "CASE WHEN " + some + " > 0 THEN " + values.stream().map(v -> "NVL(" + v + ", 1)").collect(Collectors.joining(" * ")) + " END";
		case "max" -> "GREATEST(" + values.stream().map(v -> "COALESCE(" + v + ", " + String.join(", ", values) + ")").collect(Collectors.joining(", ")) + ")";
		case "avg" -> "CASE WHEN " + some + " > 0 THEN (" + values.stream().map(v -> "NVL(" + v + ", 0)").collect(Collectors.joining(" + ")) + ") / " + some + " END";
		case "replace" -> "COALESCE(" + String.join(", ", values) + ")";
		default -> throw new IllegalArgumentException(mode);
		};
	}

	/** The relevance after the boost: unchanged where no function applies. */
	private static String apply(String mode, String relevance, String boost) {
		String b = "(" + boost + ")";
		String applied = switch (mode) {
		case "sum" -> relevance + " + " + b;
		case "multiply" -> relevance + " * " + b;
		case "max" -> "GREATEST(" + relevance + ", " + b + ")";
		case "avg" -> "(" + relevance + " + " + b + ") / 2";
		case "replace" -> b;
		default -> throw new IllegalArgumentException(mode);
		};
		return "NVL(" + applied + ", " + relevance + ")";
	}

	/** One boost function over t: null where its field is null or its condition does not hold. */
	private static String boostValue(Query q, Table table, BoostFn fn) {
		String value = functionValue(q, fn);
		if (fn.when().isEmpty()) return value;
		return "CASE WHEN " + OracledbSql.where(q, table, fn.when()) + " THEN " + value + " END";
	}

	private static String functionValue(Query q, BoostFn fn) {
		String weight = number(fn.weight());
		String factor = fn.weight() == 1.0 ? "" : weight + " * ";
		return switch (fn.kind()) {
		case "field_value" -> factor + fn.field().sql(OracledbSql.ALIAS);
		case "condition" -> weight;
		case "decay" -> {
			String kind = fn.field().kind() == null ? fn.field().column().kind() : fn.field().kind();
			boolean time = List.of("date", "timestamp", "timestamptz").contains(kind);
			String field = fn.field().sql(OracledbSql.ALIAS);
			String distance = time ? "ABS((CAST(" + field + " AS DATE) - CAST(" + q.bind(fn.origin(), "timestamptz") + " AS DATE)) * 86400)"
					: "ABS(" + field + " - " + q.bind(fn.origin(), "number") + ")";
			String scale = time ? q.bind(Code.num(seconds(fn.scale())), "number") : q.bind(fn.scale(), "number");
			String offset = fn.offset() == null ? "0" : time ? q.bind(Code.num(seconds(fn.offset())), "number") : q.bind(fn.offset(), "number");
			String d = "GREATEST(" + distance + " - " + offset + ", 0) / " + scale;
			String rate = number(fn.rate());
			String decay = switch (fn.shape()) {
			case "gauss" -> "POWER(" + rate + ", POWER(" + d + ", 2))";
			case "exp" -> "POWER(" + rate + ", " + d + ")";
			case "linear" -> "GREATEST(1 - " + d + " * " + number(complement(fn.rate())) + ", 0)";
			default -> throw new IllegalArgumentException(fn.shape());
			};
			yield factor + decay;
		}
		default -> throw new IllegalArgumentException(fn.kind());
		};
	}

	// ---------------------------------------------------------------- reads

	public static String getRows(Query q, Table table, Code.Items ids, List<String> output, String where) {
		String keys = "JSON_TABLE(" + q.json(ids) + ", '$[*]' COLUMNS (ord FOR ORDINALITY, key " + OracledbSql.jsonTableType(table.keyColumn().kind())
				+ " PATH '$')) k";
		return "SELECT " + object(OracledbSql.fields(table, output, OracledbSql.ALIAS)) + " FROM " + source(q, table) + " " + OracledbSql.ALIAS
				+ " JOIN " + keys + " ON " + col(table.key()) + " = k.key" + whereClause(where) + " ORDER BY k.ord";
	}

	/** A scalar as JSON text: JSON_SERIALIZE(JSON_SCALAR(x)). */
	public static String scalar(String expression) {
		return "JSON_SERIALIZE(JSON_SCALAR(" + expression + ") RETURNING CLOB)";
	}

	public static String count(Query q, Table table, String where) {
		return "SELECT " + scalar("COUNT(*)") + " FROM " + source(q, table) + " " + OracledbSql.ALIAS + whereClause(where);
	}

	public static String scroll(Query q, Table table, List<String> output, String where, List<String> order, Integer limit, int offset) {
		String terms = order.isEmpty() ? col(table.key()) : String.join(", ", order) + ", " + col(table.key());
		return "SELECT " + object(OracledbSql.fields(table, output, OracledbSql.ALIAS)) + " FROM " + source(q, table) + " " + OracledbSql.ALIAS
				+ whereClause(where) + " ORDER BY " + terms + " OFFSET " + offset + " ROWS" + (limit == null ? "" : " FETCH NEXT " + limit + " ROWS ONLY");
	}

	/**
	 * A scroll continuing after a cursor record (keyset pagination over the sort keys and the key, nulls last, the cursor record's
	 * values read once in a CTE) and restricted to one of n slices of the keys: ORA_HASH of the key into n buckets (SQL Language
	 * Reference, ORA_HASH: max_bucket n - 1 gives buckets 0 to n - 1).
	 */
	public static String scroll(Query q, Table table, List<String> output, String where, List<Order> order, Integer limit, int offset, Expr cursor,
			Integer slice, Integer slices) {
		List<String> conditions = new ArrayList<>();
		conditions.add(where == null ? "" : where);
		String key = col(table.key());
		String with = "";
		if (cursor != null) {
			List<String> columns = new ArrayList<>();
			for (int i = 0; i < order.size(); i++) columns.add(order.get(i).field().sql("x") + " AS c" + i);
			columns.add("x." + id(table.key()) + " AS k");
			with = "WITH cursor_row AS (SELECT " + String.join(", ", columns) + " FROM " + source(q, table) + " x WHERE x." + id(table.key()) + " = "
					+ q.bind(cursor, table.keyColumn().kind()) + ") ";
			List<String> alternatives = new ArrayList<>();
			List<String> equal = new ArrayList<>();
			for (int i = 0; i < order.size(); i++) {
				Order o = order.get(i);
				String ref = o.field().sql(OracledbSql.ALIAS);
				String value = "(SELECT c" + i + " FROM cursor_row)";
				alternatives.add(OracledbSql.and(append(equal, "(" + value + " IS NOT NULL AND (" + ref + (o.descending() ? " < " : " > ") + value + " OR "
						+ ref + " IS NULL))")));
				equal.add("DECODE(" + ref + ", " + value + ", 1, 0) = 1");
			}
			alternatives.add(OracledbSql.and(append(equal, key + " > (SELECT k FROM cursor_row)")));
			conditions.add(alternatives.size() == 1 ? alternatives.get(0) : alternatives.stream().map(a -> "(" + a + ")").collect(Collectors.joining(" OR ")));
		}
		if (slices != null) conditions.add("ORA_HASH(" + key + ", " + (slices - 1) + ") = " + slice);
		List<String> keys = new ArrayList<>();
		for (Order o : order) keys.add(o.field().sql(OracledbSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
		keys.add(key);
		return with + "SELECT " + object(OracledbSql.fields(table, output, OracledbSql.ALIAS)) + " FROM " + source(q, table) + " " + OracledbSql.ALIAS
				+ whereClause(OracledbSql.and(conditions)) + " ORDER BY " + String.join(", ", keys) + " OFFSET " + offset + " ROWS"
				+ (limit == null ? "" : " FETCH NEXT " + limit + " ROWS ONLY");
	}

	private static List<String> append(List<String> parts, String last) {
		List<String> result = new ArrayList<>(parts);
		result.add(last);
		return result;
	}

	/** A random sample (ORDER BY DBMS_RANDOM.VALUE) or the first rows by key, returned in key order. */
	public static String sample(Query q, Table table, List<String> output, String where, int size, boolean first) {
		String inner = "SELECT * FROM " + source(q, table) + " " + OracledbSql.ALIAS + whereClause(where) + " ORDER BY "
				+ (first ? col(table.key()) : "DBMS_RANDOM.VALUE") + " FETCH FIRST " + size + " ROWS ONLY";
		return "SELECT " + object(OracledbSql.fields(table, output, OracledbSql.ALIAS)) + " FROM (" + inner + ") " + OracledbSql.ALIAS
				+ " ORDER BY " + col(table.key());
	}

	/** Pairs of a random sample with each member's nearest other members (CROSS APPLY), scored by canonical similarity. */
	public static String distanceMatrix(Query q, Table table, Column vector, String metric, String where, int sample, int limit) {
		String condition = OracledbSql.and(List.of(col(vector.name()) + " IS NOT NULL", where == null ? "" : where));
		return "WITH s AS (SELECT " + col(table.key()) + " AS key, " + col(vector.name()) + " AS v FROM " + source(q, table) + " " + OracledbSql.ALIAS
				+ whereClause(condition) + " ORDER BY DBMS_RANDOM.VALUE FETCH FIRST " + sample + " ROWS ONLY) SELECT JSON_OBJECT('a' VALUE a.key, 'b' VALUE b.key, "
				+ "'score' VALUE " + OracledbSql.similarity(metric, "b.distance") + " RETURNING CLOB) FROM s a CROSS APPLY (SELECT o.key, "
				+ OracledbSql.distance("a.v", "o.v", vector.vec(), metric) + " AS distance FROM s o WHERE o.key <> a.key ORDER BY distance, o.key FETCH FIRST "
				+ limit + " ROWS ONLY) b ORDER BY a.key, b.key";
	}

	/** Generated objects of Oracle Text and vector indexes, which USER_TABLES and USER_INDEXES list beside the user's own. */
	private static String userObject(String column) {
		return column + " NOT LIKE 'DR$%' AND " + column + " NOT LIKE 'SYS\\_%' ESCAPE '\\' AND " + column + " NOT LIKE 'VECTOR$%'";
	}

	public static String describe(Query q, Table table, boolean stats, boolean progress) {
		StringBuilder fields = new StringBuilder("'name' VALUE ").append(q.bind(Code.text(table.name()), "string"))
				.append(", 'description' VALUE (SELECT c.comments FROM user_tab_comments c WHERE c.table_name = u.table_name), ")
				.append("'columns' VALUE (SELECT JSON_ARRAYAGG(JSON_OBJECT('name' VALUE c.column_name, 'type' VALUE c.data_type) ORDER BY c.column_id RETURNING CLOB) ")
				.append("FROM user_tab_columns c WHERE c.table_name = u.table_name), ")
				.append("'indexes' VALUE (SELECT JSON_ARRAYAGG(JSON_OBJECT('name' VALUE i.index_name, 'type' VALUE i.index_type) ORDER BY i.index_name RETURNING CLOB) ")
				.append("FROM user_indexes i WHERE i.table_name = u.table_name AND ").append(userObject("i.index_name")).append(")");
		if (stats) {
			fields.append(", 'count' VALUE (SELECT COUNT(*) FROM ").append(table(table)).append("), 'bytes' VALUE (SELECT NVL(SUM(s.bytes), 0) FROM user_segments s WHERE s.segment_name = u.table_name)");
		}
		if (progress) {
			fields.append(", 'index_progress' VALUE (SELECT JSON_OBJECTAGG(KEY i.index_name VALUE ").append(readiness("i"))
					.append(" RETURNING CLOB) FROM user_indexes i WHERE i.table_name = u.table_name AND ").append(userObject("i.index_name")).append(")");
		}
		return "SELECT JSON_OBJECT(" + fields + " RETURNING CLOB) FROM user_tables u WHERE u.table_name = " + q.bind(Code.text(table.name()), "string");
	}

	/** ready when the index is valid (and a domain index's last operation succeeded), else building. */
	private static String readiness(String alias) {
		return "CASE WHEN " + alias + ".status = 'VALID' AND NVL(" + alias + ".domidx_opstatus, 'VALID') = 'VALID' THEN 'ready' ELSE 'building' END";
	}

	public static String indexState(Query q, String index) {
		return "SELECT " + scalar("NVL((SELECT " + readiness("i") + " FROM user_indexes i WHERE i.index_name = " + q.bind(Code.text(index), "string")
				+ "), 'missing')") + " FROM dual";
	}

	/** The sorted names of a source query (one column name) as one JSON array, with an optional prefix and limit. */
	public static String names(Query q, String source, Expr prefix, Integer limit) {
		String condition = prefix == null ? "" : " WHERE SUBSTR(name, 1, LENGTH(" + q.bind(prefix, "string") + ")) = " + q.bind(prefix, "string");
		return "SELECT NVL(JSON_ARRAYAGG(name ORDER BY name RETURNING CLOB), TO_CLOB('[]')) FROM (SELECT name FROM (" + source + ") s" + condition
				+ " ORDER BY name" + (limit == null ? "" : " FETCH FIRST " + limit + " ROWS ONLY") + ")";
	}

	/** The tables of the program's schema that belong to no logical database (no "__" in the name). */
	public static String collections() {
		return "SELECT table_name AS name FROM user_tables WHERE " + userObject("table_name") + " AND INSTR(table_name, '__') = 0";
	}

	/** The tables of one logical database, under their collection names. */
	public static String collections(Query q, String database) {
		String prefix = q.bind(Code.text(database + "__"), "string");
		return "SELECT SUBSTR(table_name, LENGTH(" + prefix + ") + 1) AS name FROM user_tables WHERE SUBSTR(table_name, 1, LENGTH("
				+ q.bind(Code.text(database + "__"), "string") + ")) = " + q.bind(Code.text(database + "__"), "string");
	}

	/** Logical databases: the prefixes of table names before "__". */
	public static String databases() {
		return "SELECT DISTINCT SUBSTR(table_name, 1, INSTR(table_name, '__') - 1) AS name FROM user_tables WHERE INSTR(table_name, '__') > 1 AND "
				+ userObject("table_name");
	}

	/** The indexes of a table under their vecDSL names: the collection prefix of the generated names removed. */
	public static String indexes(Query q, Table table, String collection) {
		String prefix = collection + "_";
		return "SELECT CASE WHEN SUBSTR(index_name, 1, " + prefix.length() + ") = " + q.bind(Code.text(prefix), "string") + " THEN SUBSTR(index_name, "
				+ (prefix.length() + 1) + ") ELSE index_name END AS name FROM user_indexes WHERE table_name = " + q.bind(Code.text(table.name()), "string")
				+ " AND " + userObject("index_name");
	}

	public static String partitions(Query q, Table table) {
		return "SELECT partition_name AS name FROM user_tab_partitions WHERE table_name = " + q.bind(Code.text(table.name()), "string");
	}

	public static String aliases(Query q, Table table) {
		String source = "SELECT DISTINCT name FROM user_dependencies WHERE type = 'VIEW' AND referenced_type = 'TABLE'";
		return table == null ? source : source + " AND referenced_name = " + q.bind(Code.text(table.name()), "string");
	}

	public static String slices(Table table, String column) {
		return "SELECT DISTINCT " + id(column) + " AS name FROM " + table(table) + " WHERE " + id(column) + " IS NOT NULL";
	}
}
