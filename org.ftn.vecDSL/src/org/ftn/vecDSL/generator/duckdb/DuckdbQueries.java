package org.ftn.vecDSL.generator.duckdb;

import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.col;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.id;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.number;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.object;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.output;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.table;
import static org.ftn.vecDSL.generator.duckdb.DuckdbSql.whereClause;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Column;
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Query;
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Table;

/**
 * Query statements over one table, each returning one JSON value per row. Nearest-neighbour candidates are taken in a
 * materialized CTE ordered by the distance alone and limited, the shape the vss optimizer serves from an HNSW index (vss
 * README, "Usage"); scores, bounds and the final order with the key as tie-breaker are applied outside it, because a second
 * ORDER BY key makes the optimizer fall back to an exact scan (DuckDB 2.0.0). An exact search orders by distance
 * and key inside the CTE, which is that exact scan. The CTE never carries an OFFSET: the vss top-k rewrite of DuckDB 2.0.0
 * returns the first k rows and drops the offset, so the CTE takes limit plus offset candidates and the page is cut
 * after it.
 */
public final class DuckdbQueries {

	/** Internal column names of the candidate CTEs, chosen so that no vecDSL field (letters, digits, underscores) collides by accident. */
	static final String DISTANCE = "vecdsl_distance";
	static final String SCORE = "vecdsl_score";

	private DuckdbQueries() {
	}

	/** Result fields beyond id and score; with vectors, the vector columns too. */
	public record Projection(List<String> output, List<String> vectors) {
	}

	/**
	 * A nearest-neighbour search: the vector column and metric, the query vector as SQL (a bound parameter or a scalar
	 * subquery), CTEs it needs, limit and offset, the WHERE condition, the projection, native or canonical scores, bounds on the
	 * score, and exact (no index).
	 */
	public record Search(Table table, Column vector, String metric, String query, String prelude, int limit, int offset, String where,
			Projection projection, boolean nativeScore, Expr min, Expr max, boolean exact) {
	}

	public static String vectorSearch(Query q, Search s) {
		String distance = DuckdbSql.distance(s.metric()) + "(" + col(s.vector().name()) + ", " + s.query() + ")";
		String condition = DuckdbSql.and(List.of(col(s.vector().name()) + " IS NOT NULL", s.where() == null ? "" : s.where()));
		String select = String.join(", ", carried(s.table(), s.projection()).values());
		String order = DISTANCE + (s.exact() ? ", " + col(s.table().key()) : "");
		String hits = "candidates AS MATERIALIZED (SELECT " + select + ", " + distance + " AS " + DISTANCE + " FROM " + table(s.table()) + " "
				+ DuckdbSql.ALIAS + whereClause(condition) + " ORDER BY " + order + " LIMIT " + (s.limit() + s.offset()) + ")";
		hits = s.offset() == 0 ? hits.replaceFirst("^candidates AS", "hits AS")
				: hits + ", hits AS (SELECT * FROM candidates ORDER BY " + DISTANCE + ", " + id(s.table().key()) + " LIMIT " + s.limit() + offset(s.offset()) + ")";
		String score = s.nativeScore() ? "h." + DISTANCE : DuckdbSql.similarity(s.metric(), "h." + DISTANCE);
		return "WITH " + (s.prelude() == null ? "" : s.prelude() + ", ") + hits + " SELECT " + hitObject(s.table(), s.projection(), score)
				+ " FROM hits h" + whereClause(bounds(q, score, s.min(), s.max())) + " ORDER BY h." + DISTANCE + ", h." + id(s.table().key());
	}

	/** Columns a CTE carries for the projection, as select expressions over t, keyed by name. */
	private static Map<String, String> carried(Table table, Projection projection) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(table.key(), col(table.key()));
		for (String name : projection.output()) result.put(name, col(table.column(name).name()));
		for (String name : projection.vectors()) result.put(name, col(table.column(name).name()));
		return result;
	}

	/** The hit object: id, score, then the output fields and vectors, read from the CTE alias h. */
	private static String hitObject(Table table, Projection projection, String score) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", "h." + id(table.key()));
		fields.put("score", score);
		for (String name : projection.output()) fields.put(name, output(table.column(name), "h"));
		for (String name : projection.vectors()) fields.put(name, output(table.column(name), "h"));
		return object(fields);
	}

	private static String bounds(Query q, String score, Expr min, Expr max) {
		List<String> parts = new ArrayList<>();
		if (min != null) parts.add(score + " >= " + q.bind(min, "DOUBLE"));
		if (max != null) parts.add(score + " <= " + q.bind(max, "DOUBLE"));
		return DuckdbSql.and(parts);
	}

	private static String offset(int offset) {
		return offset > 0 ? " OFFSET " + offset : "";
	}

	/** A field searched by keywords: the full-text column and its boost (a field weight of the bm25f scoring). */
	public record KeywordField(Column column, double boost) {
	}

	/** Keyword search: BM25 of the fts index over the listed fields; operator and requires every term, or any term. */
	public record Keywords(Table table, List<KeywordField> fields, String operator, Expr text, int limit, int offset, String where,
			Projection projection, Expr min, Expr max) {
	}

	public static String keywordSearch(Query q, Keywords k) {
		String select = String.join(", ", carried(k.table(), k.projection()).values());
		String scored = "SELECT " + select + ", " + match(q, k.table(), k.fields(), k.operator(), k.text()) + " AS " + SCORE + " FROM "
				+ table(k.table()) + " " + DuckdbSql.ALIAS + whereClause(k.where());
		String key = "s." + id(k.table().key());
		return "WITH hits AS MATERIALIZED (SELECT * FROM (" + scored + ") s WHERE s." + SCORE + " IS NOT NULL ORDER BY s." + SCORE + " DESC, " + key
				+ " LIMIT " + k.limit() + offset(k.offset()) + ") SELECT " + hitObject(k.table(), k.projection(), "h." + SCORE) + " FROM hits h"
				+ whereClause(bounds(q, "h." + SCORE, k.min(), k.max())) + " ORDER BY h." + SCORE + " DESC, h." + id(k.table().key());
	}

	private static String match(Query q, Table table, List<KeywordField> fields, String operator, Expr text) {
		return DuckdbSql.match(q, table, fields.stream().map(f -> Map.entry(f.column().name(), f.boost())).toList(), operator, text,
				q.bind(text, "VARCHAR"));
	}

	/**
	 * One leg of a hybrid search: a vector leg (vector column, metric, query SQL, bounds on its canonical similarity, exact) or a
	 * keyword leg (fields, operator, text).
	 */
	public record Leg(Column vector, String metric, String query, List<KeywordField> fields, String operator, Expr text, int limit,
			String where, double weight, Expr min, Expr max, boolean exact) {
	}

	/** Hybrid search fused in SQL: rrf sum of 1 / (k + rank); weighted sum of weight times similarity; relative_score
	 *  sum of weight times the min-max normalized leg score. Ties order by key. */
	public record Hybrid(Table table, List<Leg> legs, String fusion, int k, int limit, int offset, Projection projection, Expr min,
			Expr max) {
	}

	public static String hybridSearch(Query q, Hybrid h) {
		List<String> ctes = fusion(q, h);
		String key = col(h.table().key());
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", key);
		fields.put("score", "f.score");
		for (String name : h.projection().output()) fields.put(name, output(h.table().column(name), DuckdbSql.ALIAS));
		for (String name : h.projection().vectors()) fields.put(name, output(h.table().column(name), DuckdbSql.ALIAS));
		return "WITH " + String.join(", ", ctes) + " SELECT " + object(fields) + " FROM f JOIN " + table(h.table()) + " " + DuckdbSql.ALIAS + " ON "
				+ key + " = f.key" + whereClause(bounds(q, "f.score", h.min(), h.max())) + " ORDER BY f.score DESC, " + key + " LIMIT " + h.limit()
				+ offset(h.offset());
	}

	/** The CTEs of the legs and of the fusion; the last, f, holds the fused score of every key a leg returned. */
	private static List<String> fusion(Query q, Hybrid h) {
		List<String> ctes = new ArrayList<>();
		List<String> ranked = new ArrayList<>();
		String key = col(h.table().key());
		for (int i = 0; i < h.legs().size(); i++) {
			Leg leg = h.legs().get(i);
			String candidates;
			if (leg.vector() != null) {
				String distance = DuckdbSql.distance(leg.metric()) + "(" + col(leg.vector().name()) + ", " + leg.query() + ")";
				String condition = DuckdbSql.and(List.of(col(leg.vector().name()) + " IS NOT NULL", leg.where() == null ? "" : leg.where()));
				String similarity = DuckdbSql.similarity(leg.metric(), "c." + DISTANCE);
				candidates = "SELECT c.key, " + similarity + " AS score FROM (SELECT " + key + " AS key, " + distance + " AS " + DISTANCE + " FROM "
						+ table(h.table()) + " " + DuckdbSql.ALIAS + whereClause(condition) + " ORDER BY " + DISTANCE + (leg.exact() ? ", " + key : "")
						+ " LIMIT " + leg.limit() + ") c" + whereClause(bounds(q, similarity, leg.min(), leg.max()));
			} else {
				String scored = "SELECT " + key + " AS key, " + match(q, h.table(), leg.fields(), leg.operator(), leg.text()) + " AS score FROM "
						+ table(h.table()) + " " + DuckdbSql.ALIAS + whereClause(leg.where());
				candidates = "SELECT s.key, s.score FROM (" + scored + ") s WHERE s.score IS NOT NULL ORDER BY s.score DESC, s.key LIMIT " + leg.limit();
			}
			ctes.add("l" + i + " AS MATERIALIZED (" + candidates + ")");
			ranked.add("SELECT key, score, " + i + " AS leg, row_number() OVER (ORDER BY score DESC, key) AS rank FROM l" + i);
		}
		ctes.add("r AS (" + String.join(" UNION ALL ", ranked) + ")");
		String weight = "CASE leg " + IntStream.range(0, h.legs().size()).mapToObj(i -> "WHEN " + i + " THEN " + number(h.legs().get(i).weight()))
				.collect(Collectors.joining(" ")) + " END";
		switch (h.fusion()) {
		case "rrf" -> ctes.add("f AS (SELECT key, sum(1.0 / (" + h.k() + " + rank)) AS score FROM r GROUP BY key)");
		case "weighted" -> ctes.add("f AS (SELECT key, sum(" + weight + " * score) AS score FROM r GROUP BY key)");
		case "relative_score" -> {
			ctes.add("n AS (SELECT key, leg, CASE WHEN max(score) OVER (PARTITION BY leg) > min(score) OVER (PARTITION BY leg) THEN "
					+ "(score - min(score) OVER (PARTITION BY leg)) / (max(score) OVER (PARTITION BY leg) - min(score) OVER (PARTITION BY leg)) "
					+ "ELSE 1 END AS score FROM r)");
			ctes.add("f AS (SELECT key, sum(" + weight + " * score) AS score FROM n GROUP BY key)");
		}
		default -> throw new IllegalArgumentException(h.fusion());
		}
		return ctes;
	}

	/**
	 * The CTE of a recommendation query vector (Qdrant's average_vector strategy, as the other generators): the element-wise
	 * average of the positive examples moved away from the average of the negatives, 2 avg(positive) - avg(negative). Examples
	 * are ids looked up in the lookup table or vector literals. DuckDB has no avg over arrays, so the average is taken per
	 * element position.
	 */
	public static String recommendVector(Query q, Table lookup, Column vector, List<Expr> positiveIds, List<Expr> positiveVectors,
			List<Expr> negativeIds, List<Expr> negativeVectors) {
		String type = vector.vec().sql();
		String positive = average(examples(q, lookup, vector, positiveIds, positiveVectors), type);
		String query = negativeIds.isEmpty() && negativeVectors.isEmpty() ? positive
				: "(SELECT list_transform(list_zip(p.v::FLOAT[], n.v::FLOAT[]), lambda z: 2 * z[1] - z[2])::" + type + " FROM (SELECT " + positive
						+ " AS v) p, (SELECT " + average(examples(q, lookup, vector, negativeIds, negativeVectors), type) + " AS v) n)";
		return "q AS MATERIALIZED (SELECT " + query + " AS v)";
	}

	private static String average(String examples, String type) {
		return "(SELECT list(a.x ORDER BY a.i)::" + type + " FROM (SELECT e.i, avg(e.x) AS x FROM (SELECT unnest(s.v::FLOAT[]) AS x, "
				+ "generate_subscripts(s.v::FLOAT[], 1) AS i FROM (" + examples + ") s) e GROUP BY e.i) a)";
	}

	private static String examples(Query q, Table lookup, Column vector, List<Expr> ids, List<Expr> vectors) {
		List<String> parts = new ArrayList<>();
		if (!ids.isEmpty()) {
			parts.add("SELECT x." + id(vector.name()) + " AS v FROM " + table(lookup) + " x WHERE list_contains("
					+ q.array(Code.items(ids), lookup.keyColumn().paramType()) + ", x." + id(lookup.key()) + ")");
		}
		for (Expr value : vectors) parts.add("SELECT " + q.json(value, vector.vec().sql()) + " AS v");
		return String.join(" UNION ALL ", parts);
	}

	// ---------------------------------------------------------------- result shapes

	/** Group-by: the group key and the number of hits kept per group. */
	public record Group(Ref field, int size) {
	}

	/** Maximal marginal relevance: lambda, the candidate pool, and the vector column and metric of the diversity term. */
	public record Mmr(double lambda, int candidates, Column vector, String metric) {
	}

	/** An aggregation over the matching records: result name, kind, field, histogram interval and bucket count. */
	public record Aggregate(String name, String kind, Ref field, Expr interval, int size) {
	}

	/** A boost function: kind, field, weight, decay shape with origin, scale, offset and rate, and the condition. */
	public record BoostFn(String kind, Ref field, double weight, String shape, Expr origin, Expr scale, Expr offset, double rate,
			List<DuckdbSql.Cond> when) {
	}

	/** Boost functions, how their values combine, and how the combined boost applies to the relevance. */
	public record Boost(List<BoostFn> functions, String combine, String apply) {
	}

	/**
	 * The shape of a search result beyond a plain hit list: a keyset cursor (the key of the last hit of the previous page),
	 * boosts, MMR, groups, the total, aggregations and sort keys (a sort key with a null field is the relevance).
	 */
	public record Shape(Expr cursor, Boost boost, Mmr mmr, Group group, boolean total, List<Aggregate> aggregations, List<Order> sort) {

		public boolean plain() {
			return cursor == null && boost == null && mmr == null && group == null && !total && aggregations.isEmpty() && sort.isEmpty();
		}

		/** One object with the hits and the total or aggregations, instead of a list of hits. */
		public boolean wrapped() {
			return total || !aggregations.isEmpty();
		}

		/** Candidates a search scores: limit plus offset, the MMR pool, or every match when boosts or groups rank them all. */
		Integer pool(int fetch) {
			if (boost != null || group != null) return null;
			return mmr == null ? fetch : Math.max(mmr.candidates(), fetch);
		}
	}

	/** A prefetch stage: the keys of the stage's nearest rows by its vector column, metric and query, ties by key. */
	public static String stage(String name, Table table, Column vector, String metric, String query, String where, int limit) {
		String distance = DuckdbSql.distance(metric) + "(" + col(vector.name()) + ", " + query + ")";
		String condition = DuckdbSql.and(List.of(col(vector.name()) + " IS NOT NULL", where == null ? "" : where));
		return name + " AS MATERIALIZED (SELECT " + col(table.key()) + " AS key FROM " + table(table) + " " + DuckdbSql.ALIAS + whereClause(condition)
				+ " ORDER BY " + distance + ", " + col(table.key()) + " LIMIT " + limit + ")";
	}

	/** The condition that a row is among those the stages return; null without stages. */
	public static String within(Table table, List<String> stages) {
		if (stages.isEmpty()) return null;
		return col(table.key()) + " IN (" + stages.stream().map(s -> "SELECT key FROM " + s).collect(Collectors.joining(" UNION ALL ")) + ")";
	}

	private static String joined(Table table, String alias) {
		return " JOIN " + table(table) + " " + DuckdbSql.ALIAS + " ON " + col(table.key()) + " = " + alias + ".key";
	}

	/** A record after the cursor record in the order (ord, key): ord is the expression over t, the cursor row is cursor_row. */
	private static String afterCursor(String ord, String key) {
		return "(" + ord + " > (SELECT ord FROM cursor_row) OR (" + ord + " = (SELECT ord FROM cursor_row) AND " + key
				+ " > (SELECT key FROM cursor_row)))";
	}

	/** A nearest-neighbour search with a result shape; a plain shape gives the statement of vectorSearch(q, s). */
	public static String vectorSearch(Query q, Search s, Shape shape) {
		if (shape.plain()) return vectorSearch(q, s);
		String key = col(s.table().key());
		String full = DuckdbSql.distance(s.metric()) + "(" + col(s.vector().name()) + ", " + s.query() + ")";
		List<String> conditions = new ArrayList<>(List.of(col(s.vector().name()) + " IS NOT NULL", s.where() == null ? "" : s.where()));
		String matching = DuckdbSql.and(conditions);
		Integer pool = shape.pool(s.limit() + s.offset());
		List<String> ctes = new ArrayList<>();
		if (s.prelude() != null) ctes.add(s.prelude());
		boolean early = shape.cursor() != null && pool != null;
		if (early) {
			ctes.add("cursor_row AS MATERIALIZED (SELECT " + full + " AS ord, " + key + " AS key FROM " + table(s.table()) + " " + DuckdbSql.ALIAS
					+ " WHERE " + key + " = " + q.bind(shape.cursor(), s.table().keyColumn().paramType()) + ")");
			conditions.add(afterCursor(full, key));
		}
		ctes.add("candidates AS MATERIALIZED (SELECT " + key + " AS key, " + full + " AS " + DISTANCE + " FROM " + table(s.table()) + " "
				+ DuckdbSql.ALIAS + whereClause(DuckdbSql.and(conditions)) + " ORDER BY " + DISTANCE + (s.exact() ? ", " + key : "")
				+ (pool == null ? "" : " LIMIT " + pool) + ")");
		String score = s.nativeScore() ? DISTANCE : DuckdbSql.similarity(s.metric(), DISTANCE);
		ctes.add("scored AS (SELECT key, " + score + " AS score, " + DISTANCE + " AS ord, " + DuckdbSql.similarity(s.metric(), DISTANCE)
				+ " AS rel FROM candidates)");
		String everything = "SELECT " + key + " AS key, " + (s.nativeScore() ? full : DuckdbSql.similarity(s.metric(), "(" + full + ")")) + " AS score FROM "
				+ table(s.table()) + " " + DuckdbSql.ALIAS + whereClause(matching);
		return shaped(q, s.table(), ctes, pool == null ? null : everything, early, s.limit(), s.offset(), s.projection(), s.min(), s.max(), shape);
	}

	/** A keyword search with a result shape; a plain shape gives the statement of keywordSearch(q, k). */
	public static String keywordSearch(Query q, Keywords k, Shape shape) {
		if (shape.plain()) return keywordSearch(q, k);
		String key = col(k.table().key());
		String match = match(q, k.table(), k.fields(), k.operator(), k.text());
		String scoredRows = "SELECT " + key + " AS key, " + match + " AS score FROM " + table(k.table()) + " " + DuckdbSql.ALIAS + whereClause(k.where());
		List<String> conditions = new ArrayList<>(List.of("s.score IS NOT NULL"));
		Integer pool = shape.pool(k.limit() + k.offset());
		List<String> ctes = new ArrayList<>();
		boolean early = shape.cursor() != null && pool != null;
		if (early) {
			ctes.add("cursor_row AS MATERIALIZED (SELECT -s.score AS ord, s.key FROM (" + scoredRows + ") s WHERE s.key = "
					+ q.bind(shape.cursor(), k.table().keyColumn().paramType()) + ")");
			conditions.add(afterCursor("-s.score", "s.key"));
		}
		ctes.add("matches AS MATERIALIZED (" + scoredRows + ")");
		ctes.add("candidates AS MATERIALIZED (SELECT s.key, s.score FROM matches s" + whereClause(DuckdbSql.and(conditions)) + " ORDER BY s.score DESC, s.key"
				+ (pool == null ? "" : " LIMIT " + pool) + ")");
		ctes.add("scored AS (SELECT key, score, -score AS ord, score AS rel FROM candidates)");
		String everything = "SELECT key, score FROM matches WHERE score IS NOT NULL";
		return shaped(q, k.table(), ctes, pool == null ? null : everything, early, k.limit(), k.offset(), k.projection(), k.min(), k.max(), shape);
	}

	/** A hybrid search with a result shape; a plain shape gives the statement of hybridSearch(q, h). */
	public static String hybridSearch(Query q, Hybrid h, Shape shape) {
		if (shape.plain()) return hybridSearch(q, h);
		List<String> ctes = new ArrayList<>(fusion(q, h));
		ctes.add("scored AS (SELECT key, score, -score AS ord, score AS rel FROM f)");
		return shaped(q, h.table(), ctes, null, false, h.limit(), h.offset(), h.projection(), h.min(), h.max(), shape);
	}

	/**
	 * The stages after the scored candidates (CTE scored: key, score, ord ascending from the best, rel the canonical
	 * relevance): boosts re-score, a cursor continues after its record, MMR, groups or limit and offset select the page, score
	 * bounds filter it, sort keys order it, and total and aggregations count the matching records (everything, or scored when
	 * it holds every match). Rows rejoin the table by key.
	 */
	private static String shaped(Query q, Table table, List<String> ctes, String everything, boolean cursorApplied, int limit, int offset,
			Projection projection, Expr min, Expr max, Shape shape) {
		String last = "scored";
		if (shape.boost() != null) {
			String boosted = apply(shape.boost().apply(), "x.rel", combine(q, table, shape.boost()));
			ctes.add("boosted AS (SELECT x.key, b.score, -b.score AS ord, b.score AS rel FROM scored x" + joined(table, "x")
					+ " CROSS JOIN LATERAL (SELECT " + boosted + " AS score) b)");
			last = "boosted";
		}
		String matched = everything == null ? last : "everything";
		if (shape.cursor() != null && !cursorApplied) {
			String c = q.bind(shape.cursor(), table.keyColumn().paramType());
			ctes.add("cursor_row AS MATERIALIZED (SELECT ord, key FROM " + last + " WHERE key = " + c + ")");
			ctes.add("after_cursor AS (SELECT * FROM " + last + " x WHERE " + afterCursor("x.ord", "x.key") + ")");
			last = "after_cursor";
		}
		String bounds = bounds(q, "x.score", min, max);
		String hit = hitObject(table, projection);
		String rows;
		if (shape.group() != null) {
			String groupKey = shape.group().field().sql(DuckdbSql.ALIAS);
			ctes.add("grouped AS (SELECT x.key, x.score, x.ord, " + groupKey + " AS grp, row_number() OVER (PARTITION BY " + groupKey
					+ " ORDER BY x.ord, x.key) AS member FROM " + last + " x" + joined(table, "x") + ")");
			ctes.add("leaders AS MATERIALIZED (SELECT grp, row_number() OVER (ORDER BY ord, key) AS pos FROM grouped WHERE member = 1 ORDER BY ord, key LIMIT "
					+ limit + offset(offset) + ")");
			ctes.add("page AS (SELECT g.key, g.score, g.ord, g.grp, l.pos, g.member FROM grouped g JOIN leaders l ON l.grp IS NOT DISTINCT FROM g.grp WHERE g.member <= "
					+ shape.group().size() + ")");
			rows = "hits AS (SELECT json_object('group', to_json(x.grp), 'hits', to_json(list(" + hit + " ORDER BY x.member))) AS hit, x.pos AS n FROM page x"
					+ joined(table, "x") + whereClause(bounds) + " GROUP BY x.pos, x.grp)";
		} else {
			if (shape.mmr() != null) {
				Mmr mmr = shape.mmr();
				String diversity = DuckdbSql.similarity(mmr.metric(), DuckdbSql.distance(mmr.metric()) + "(c.v, d.v)");
				ctes.add("pool AS MATERIALIZED (SELECT x.key, x.score, x.ord, x.rel, " + col(mmr.vector().name()) + " AS v FROM " + last + " x" + joined(table, "x")
						+ " WHERE " + col(mmr.vector().name()) + " IS NOT NULL ORDER BY x.ord, x.key LIMIT " + mmr.candidates() + ")");
				ctes.add("picked (keys, n) AS (SELECT []::" + table.keyColumn().paramType() + "[], 0 UNION ALL SELECT list_append(p.keys, c.key), p.n + 1 FROM picked p "
						+ "CROSS JOIN LATERAL (SELECT c.key FROM pool c WHERE NOT list_contains(p.keys, c.key) ORDER BY " + number(mmr.lambda()) + " * c.rel - "
						+ number(complement(mmr.lambda())) + " * coalesce((SELECT max(" + diversity + ") FROM pool d WHERE list_contains(p.keys, d.key)), 0) DESC, c.ord, c.key LIMIT 1) c "
						+ "WHERE p.n < " + (limit + offset) + ")");
				ctes.add("page AS (SELECT c.key, c.score, c.ord, o.pos FROM (SELECT keys FROM picked ORDER BY n DESC LIMIT 1) l "
						+ "CROSS JOIN unnest(l.keys) WITH ORDINALITY AS o(key, pos) JOIN pool c ON c.key = o.key WHERE o.pos > " + offset + ")");
			} else {
				ctes.add("page AS MATERIALIZED (SELECT key, score, ord, row_number() OVER (ORDER BY ord, key) AS pos FROM " + last
						+ " ORDER BY ord, key LIMIT " + limit + offset(offset) + ")");
			}
			List<String> order = new ArrayList<>();
			for (Order o : shape.sort()) {
				order.add(o.field() == null ? "x.ord" + (o.descending() ? "" : " DESC") : o.field().sql(DuckdbSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
			}
			order.add("x.pos");
			rows = "hits AS (SELECT " + hit + " AS hit, row_number() OVER (ORDER BY " + String.join(", ", order) + ") AS n FROM page x" + joined(table, "x")
					+ whereClause(bounds) + ")";
		}
		ctes.add(rows);
		String with = shape.mmr() == null ? "WITH " : "WITH RECURSIVE ";
		if (!shape.wrapped()) return with + String.join(", ", ctes) + " SELECT hit FROM hits ORDER BY n";
		if (everything != null) ctes.add("everything AS (" + everything + ")");
		ctes.add("matched AS (SELECT t.* FROM " + matched + " x" + joined(table, "x") + whereClause(bounds(q, "x.score", min, max)) + ")");
		Map<String, String> result = new LinkedHashMap<>();
		result.put("hits", "coalesce((SELECT to_json(list(hit ORDER BY n)) FROM hits), '[]'::JSON)");
		if (shape.total()) result.put("total", "(SELECT count(*) FROM matched)");
		if (!shape.aggregations().isEmpty()) {
			Map<String, String> aggregations = new LinkedHashMap<>();
			for (Aggregate a : shape.aggregations()) aggregations.put(a.name(), aggregate(q, a));
			result.put("aggregations", object(aggregations));
		}
		return with + String.join(", ", ctes) + " SELECT " + object(result);
	}

	/** The hit object of a shaped search: id, score, then the output fields and vectors read from t. */
	private static String hitObject(Table table, Projection projection) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", col(table.key()));
		fields.put("score", "x.score");
		for (String name : projection.output()) fields.put(name, output(table.column(name), DuckdbSql.ALIAS));
		for (String name : projection.vectors()) fields.put(name, output(table.column(name), DuckdbSql.ALIAS));
		return object(fields);
	}

	/** An aggregation over the matching records (alias m), as one JSON value; list columns aggregate their elements. */
	private static String aggregate(Query q, Aggregate a) {
		String value = a.field().sql("m");
		boolean array = a.field().path().isEmpty() && a.field().column().array();
		String from = array ? " FROM matched m CROSS JOIN LATERAL (SELECT unnest(" + value + ") AS v) e" : " FROM matched m";
		String v = array ? "e.v" : value;
		return switch (a.kind()) {
		case "count" -> "(SELECT to_json(count(" + v + "))" + from + ")";
		case "sum", "avg", "min", "max" -> "(SELECT to_json(" + a.kind() + "(" + v + "))" + from + ")";
		case "distinct_count", "cardinality" -> "(SELECT to_json(count(DISTINCT " + v + "))" + from + ")";
		case "stats" -> "(SELECT json_object('count', count(" + v + "), 'min', min(" + v + "), 'max', max(" + v + "), 'avg', avg(" + v + "), 'sum', sum(" + v + "))" + from + ")";
		case "terms", "facet" -> "(SELECT coalesce(to_json(list(json_object('key', b.k, 'count', b.n) ORDER BY b.n DESC, b.k)), '[]'::JSON) FROM (SELECT " + v
				+ " AS k, count(*) AS n" + from + " WHERE " + v + " IS NOT NULL GROUP BY " + v + " ORDER BY count(*) DESC, " + v + " LIMIT " + a.size() + ") b)";
		case "histogram" -> {
			String interval = q.bind(a.interval(), "DOUBLE");
			yield buckets("floor(" + v + " / " + interval + ") * " + interval, v, from);
		}
		case "date_histogram" -> buckets("time_bucket(" + q.bind(a.interval(), "INTERVAL") + ", " + v + "::TIMESTAMP, TIMESTAMP '1970-01-01')", v, from);
		case "percentiles" -> "(SELECT json_object(" + PERCENTS.stream().map(p -> DuckdbSql.literal(number(p)) + ", quantile_cont(" + v + ", " + number(p / 100) + ")")
				.collect(Collectors.joining(", ")) + ")" + from + ")";
		default -> throw new IllegalArgumentException(a.kind());
		};
	}

	/** Elasticsearch's default percents, so that the percentile aggregation reads the same on every target. */
	private static final List<Double> PERCENTS = List.of(1.0, 5.0, 25.0, 50.0, 75.0, 95.0, 99.0);

	private static String buckets(String key, String v, String from) {
		return "(SELECT coalesce(to_json(list(json_object('key', b.k, 'count', b.n) ORDER BY b.k)), '[]'::JSON) FROM (SELECT " + key + " AS k, count(*) AS n"
				+ from + " WHERE " + v + " IS NOT NULL GROUP BY 1) b)";
	}

	/** 1 - x in decimal arithmetic, so that 1 - 0.7 prints as 0.3. */
	private static double complement(double x) {
		return BigDecimal.ONE.subtract(BigDecimal.valueOf(x)).doubleValue();
	}

	/** The combined value of the boost functions that apply to a record (null when none applies), over t. */
	private static String combine(Query q, Table table, Boost boost) {
		List<String> values = new ArrayList<>();
		for (BoostFn fn : boost.functions()) values.add(boostValue(q, table, fn));
		if (values.size() == 1) return values.get(0);
		String applying = "(" + values.stream().map(v -> "CASE WHEN " + v + " IS NULL THEN 0 ELSE 1 END").collect(Collectors.joining(" + ")) + ")";
		String some = applying + " > 0";
		return switch (boost.combine()) {
		case "sum" -> "CASE WHEN " + some + " THEN " + values.stream().map(v -> "coalesce(" + v + ", 0)").collect(Collectors.joining(" + ")) + " END";
		case "multiply" -> "CASE WHEN " + some + " THEN " + values.stream().map(v -> "coalesce(" + v + ", 1)").collect(Collectors.joining(" * ")) + " END";
		case "max" -> "greatest(" + String.join(", ", values) + ")";
		case "avg" -> "CASE WHEN " + some + " THEN (" + values.stream().map(v -> "coalesce(" + v + ", 0)").collect(Collectors.joining(" + ")) + ") / " + applying + " END";
		case "replace" -> "coalesce(" + String.join(", ", values) + ")";
		default -> throw new IllegalArgumentException(boost.combine());
		};
	}

	/** The relevance after the boost: unchanged where no function applies. */
	private static String apply(String mode, String relevance, String boost) {
		String b = "(" + boost + ")";
		String applied = switch (mode) {
		case "sum" -> relevance + " + " + b;
		case "multiply" -> relevance + " * " + b;
		case "max" -> "greatest(" + relevance + ", " + b + ")";
		case "avg" -> "(" + relevance + " + " + b + ") / 2";
		case "replace" -> b;
		default -> throw new IllegalArgumentException(mode);
		};
		return "coalesce(" + applied + ", " + relevance + ")";
	}

	/**
	 * One boost function over t: the weighted field value, the weighted decay of the field's distance from the origin, or the
	 * weight; null where its field is null or its condition does not hold.
	 */
	private static String boostValue(Query q, Table table, BoostFn fn) {
		String value = functionValue(q, fn);
		if (fn.when().isEmpty()) return value;
		return "CASE WHEN " + DuckdbSql.where(q, table, fn.when()) + " THEN " + value + " END";
	}

	private static String functionValue(Query q, BoostFn fn) {
		String weight = number(fn.weight());
		String factor = fn.weight() == 1.0 ? "" : weight + " * ";
		return switch (fn.kind()) {
		case "field_value" -> factor + fn.field().sql(DuckdbSql.ALIAS) + "::DOUBLE";
		case "condition" -> weight + "::DOUBLE";
		case "decay" -> {
			String type = fn.field().type() == null ? fn.field().column().type() : fn.field().type();
			boolean time = List.of("DATE", "TIMESTAMP", "TIMESTAMPTZ").contains(type);
			String field = fn.field().sql(DuckdbSql.ALIAS);
			String distance = time ? "abs(epoch(" + field + "::TIMESTAMPTZ - " + q.bind(fn.origin(), "TIMESTAMPTZ") + "))"
					: "abs(" + field + "::DOUBLE - " + q.bind(fn.origin(), "DOUBLE") + ")";
			String scale = time ? "epoch(" + q.bind(fn.scale(), "INTERVAL") + ")" : q.bind(fn.scale(), "DOUBLE");
			String offset = fn.offset() == null ? "0" : time ? "epoch(" + q.bind(fn.offset(), "INTERVAL") + ")" : q.bind(fn.offset(), "DOUBLE");
			String d = "greatest(" + distance + " - " + offset + ", 0) / " + scale;
			String rate = number(fn.rate());
			String decay = switch (fn.shape()) {
			case "gauss" -> "power(" + rate + ", power(" + d + ", 2))";
			case "exp" -> "power(" + rate + ", " + d + ")";
			case "linear" -> "greatest(1 - " + d + " * " + number(complement(fn.rate())) + ", 0)";
			default -> throw new IllegalArgumentException(fn.shape());
			};
			yield factor + decay;
		}
		default -> throw new IllegalArgumentException(fn.kind());
		};
	}

	/** Rows by key, in the order of the keys. */
	public static String getRows(Query q, Table table, Code.Items ids, List<String> output, String where) {
		String keys = q.array(ids, table.keyColumn().paramType());
		return "SELECT " + object(DuckdbSql.fields(table, output, DuckdbSql.ALIAS)) + " FROM " + table(table) + " " + DuckdbSql.ALIAS
				+ " JOIN (SELECT unnest(l.keys) AS key, generate_subscripts(l.keys, 1) AS ord FROM (SELECT " + keys + " AS keys) l) k ON "
				+ col(table.key()) + " = k.key" + whereClause(where) + " ORDER BY k.ord";
	}

	public static String count(Table table, String where) {
		return "SELECT to_json(count(*)) FROM " + table(table) + " " + DuckdbSql.ALIAS + whereClause(where);
	}

	/** A field a sort key reads: a column, or a typed sub-field of a JSON column. */
	public record Ref(Column column, List<String> path, String type) {

		public String sql(String alias) {
			return DuckdbSql.reference(alias, column, path, type);
		}
	}

	/** A sort key of a scroll: the field and the direction. */
	public record Order(Ref field, boolean descending) {
	}

	/**
	 * Records in sort order (the key breaks ties, nulls last), with limit and offset, continuing after a cursor record
	 * (keyset pagination) and restricted to one of n slices of the keys (the first 28 bits of the md5 of the key text,
	 * modulo n, as the pgvector generator slices).
	 */
	public static String scroll(Query q, Table table, List<String> output, String where, List<Order> order, Integer limit, int offset,
			Expr cursor, Integer slice, Integer slices) {
		List<String> conditions = new ArrayList<>();
		conditions.add(where == null ? "" : where);
		String key = col(table.key());
		if (cursor != null) {
			String c = q.bind(cursor, table.keyColumn().paramType());
			List<String> alternatives = new ArrayList<>();
			List<String> equal = new ArrayList<>();
			for (Order o : order) {
				String ref = o.field().sql(DuckdbSql.ALIAS);
				String value = "(SELECT " + o.field().sql("x") + " FROM " + table(table) + " x WHERE x." + id(table.key()) + " = "
						+ c + ")";
				String after = "(" + value + " IS NOT NULL AND (" + ref + (o.descending() ? " < " : " > ") + value + " OR " + ref + " IS NULL))";
				alternatives.add(DuckdbSql.and(append(equal, after)));
				equal.add(ref + " IS NOT DISTINCT FROM " + value);
			}
			alternatives.add(DuckdbSql.and(append(equal, key + " > " + c)));
			conditions.add(alternatives.size() == 1 ? alternatives.get(0) : alternatives.stream().map(a -> "(" + a + ")").collect(Collectors.joining(" OR ")));
		}
		if (slices != null) {
			conditions.add("CAST('0x' || substr(md5(" + key + "::VARCHAR), 1, 7) AS BIGINT) % " + slices + " = " + slice);
		}
		List<String> keys = new ArrayList<>();
		for (Order o : order) keys.add(o.field().sql(DuckdbSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
		keys.add(key);
		return "SELECT " + object(DuckdbSql.fields(table, output, DuckdbSql.ALIAS)) + " FROM " + table(table) + " " + DuckdbSql.ALIAS
				+ whereClause(DuckdbSql.and(conditions)) + " ORDER BY " + String.join(", ", keys) + (limit == null ? "" : " LIMIT " + limit) + offset(offset);
	}

	private static List<String> append(List<String> parts, String last) {
		List<String> result = new ArrayList<>(parts);
		result.add(last);
		return result;
	}

	/** Seed of the reservoir samples, so that the three host languages draw the same records from the same data. */
	public static final int SEED = 42;

	/**
	 * A reservoir sample of the matching records with a fixed seed (USING SAMPLE reservoir(n ROWS) REPEATABLE), or the first
	 * records in key order; results are ordered by key.
	 */
	public static String sample(Table table, List<String> output, String where, int size, boolean first) {
		String matching = "SELECT * FROM " + table(table) + " " + DuckdbSql.ALIAS + whereClause(where);
		String inner = first ? matching + " ORDER BY " + col(table.key()) + " LIMIT " + size
				: "SELECT * FROM (" + matching + ") USING SAMPLE reservoir(" + size + " ROWS) REPEATABLE (" + SEED + ")";
		return "SELECT " + object(DuckdbSql.fields(table, output, DuckdbSql.ALIAS)) + " FROM (" + inner + ") " + DuckdbSql.ALIAS + " ORDER BY "
				+ col(table.key());
	}

	/** A reservoir sample (fixed seed), each sampled record with its nearest neighbours among the sample (Qdrant's distance matrix). */
	public static String distanceMatrix(Table table, Column vector, String metric, String where, int sample, int limit) {
		String condition = DuckdbSql.and(List.of(col(vector.name()) + " IS NOT NULL", where == null ? "" : where));
		String fn = DuckdbSql.distance(metric);
		return "WITH s AS MATERIALIZED (SELECT * FROM (SELECT " + col(table.key()) + " AS key, " + col(vector.name()) + " AS v FROM " + table(table)
				+ " " + DuckdbSql.ALIAS + whereClause(condition) + ") USING SAMPLE reservoir(" + sample + " ROWS) REPEATABLE (" + SEED
				+ ")) SELECT json_object('a', a.key, 'b', b.key, 'score', " + DuckdbSql.similarity(metric, "b.d") + ") FROM s a, LATERAL (SELECT o.key, "
				+ fn + "(a.v, o.v) AS d FROM s o WHERE o.key <> a.key ORDER BY d, o.key LIMIT " + limit + ") b ORDER BY a.key, b.d, b.key";
	}

	/** Table name, schema, description, columns and indexes from the catalog functions; stats adds the row count. */
	public static String describe(Query q, Table table, boolean stats, boolean progress) {
		String schema = q.bind(Code.text(table.schema()), "VARCHAR");
		String name = q.bind(Code.text(table.name()), "VARCHAR");
		StringBuilder fields = new StringBuilder("'name', d.table_name, 'database', d.schema_name, 'description', d.comment, ")
				.append("'columns', (SELECT to_json(list(json_object('name', c.column_name, 'type', c.data_type) ORDER BY c.column_index)) ")
				.append("FROM duckdb_columns() c WHERE c.schema_name = d.schema_name AND c.table_name = d.table_name), ")
				.append("'indexes', coalesce((SELECT to_json(list(json_object('name', i.index_name, 'method', ").append(METHOD)
				.append(") ORDER BY i.index_name)) FROM duckdb_indexes() i WHERE i.schema_name = d.schema_name AND i.table_name = d.table_name), '[]'::JSON)");
		if (stats) fields.append(", 'count', (SELECT count(*) FROM ").append(table(table)).append(")");
		if (progress) {
			fields.append(", 'index_progress', coalesce((SELECT to_json(map_from_entries(list((i.index_name, 'ready') ORDER BY i.index_name))) ")
					.append("FROM duckdb_indexes() i WHERE i.schema_name = d.schema_name AND i.table_name = d.table_name), '{}'::JSON)");
		}
		return "SELECT json_object(" + fields + ") FROM duckdb_tables() d WHERE d.database_name = current_database() AND d.schema_name = " + schema
				+ " AND d.table_name = " + name;
	}

	private static final String METHOD = "CASE WHEN contains(upper(i.sql), 'USING HNSW') THEN 'hnsw' ELSE 'art' END";

	/** The state of an index: ready when it exists (CREATE INDEX returns once the index is built), missing otherwise. */
	public static String indexState(Query q, Table table, String index) {
		return "SELECT to_json(CASE WHEN EXISTS (SELECT 1 FROM duckdb_indexes() i WHERE i.database_name = current_database() AND i.schema_name = "
				+ q.bind(Code.text(table.schema()), "VARCHAR") + " AND i.index_name = " + q.bind(Code.text(index), "VARCHAR")
				+ ") THEN 'ready' ELSE 'missing' END)";
	}

	/** A sorted JSON list of names from a catalog query of one 'name' column, with an optional prefix and limit. */
	public static String names(Query q, String source, Expr prefix, Integer limit) {
		String condition = prefix == null ? "" : " WHERE starts_with(name, " + q.bind(prefix, "VARCHAR") + ")";
		return "SELECT coalesce(to_json(list(name ORDER BY name)), '[]'::JSON) FROM (SELECT name FROM (" + source + ") s" + condition
				+ " ORDER BY name" + (limit == null ? "" : " LIMIT " + limit) + ") s";
	}

	/** Tables of a schema, without the snapshot tables. */
	public static String collections(Query q, String schema) {
		return "SELECT table_name AS name FROM duckdb_tables() WHERE database_name = current_database() AND schema_name = "
				+ q.bind(Code.text(schema), "VARCHAR") + " AND NOT starts_with(table_name, " + q.bind(Code.text(SNAPSHOT_PREFIX), "VARCHAR") + ")";
	}

	/** Logical databases: the schemas of the file other than main and the fts index schemas. */
	public static String databases() {
		return "SELECT schema_name AS name FROM duckdb_schemas() WHERE database_name = current_database() AND NOT internal "
				+ "AND schema_name <> 'main' AND NOT starts_with(schema_name, 'fts_')";
	}

	/** The indexes of a table under their vecDSL names: the collection prefix of the generated names removed. */
	public static String indexes(Query q, Table table, String collection) {
		String prefix = q.bind(Code.text(collection + "_"), "VARCHAR");
		return "SELECT CASE WHEN starts_with(index_name, " + prefix + ") THEN substr(index_name, length(" + prefix + ") + 1) ELSE index_name END AS name "
				+ "FROM duckdb_indexes() WHERE database_name = current_database() AND schema_name = " + q.bind(Code.text(table.schema()), "VARCHAR")
				+ " AND table_name = " + q.bind(Code.text(table.name()), "VARCHAR");
	}

	/** Views (aliases) of a schema; with a table, the views that select that table (as the generator writes them). */
	public static String aliases(Query q, Table table, String schema) {
		String source = "SELECT view_name AS name FROM duckdb_views() WHERE database_name = current_database() AND NOT internal AND schema_name = "
				+ q.bind(Code.text(schema), "VARCHAR");
		if (table == null) return source;
		return source + " AND ends_with(replace(sql, '\"', ''), " + q.bind(Code.text(" FROM " + table.schema() + "." + table.name() + ";"), "VARCHAR") + ")";
	}

	public static String slices(Table table, String column) {
		return "SELECT DISTINCT " + id(column) + " AS name FROM " + table(table) + " WHERE " + id(column) + " IS NOT NULL";
	}

	/** Snapshots are tables named vecdsl_snapshot__<name> in the main schema (DropSnapshot names no collection). */
	public static final String SNAPSHOT_PREFIX = "vecdsl_snapshot__";

	public static String snapshotTable(String snapshot) {
		return SNAPSHOT_PREFIX + snapshot;
	}

	public static String snapshots(Query q) {
		String prefix = q.bind(Code.text(SNAPSHOT_PREFIX), "VARCHAR");
		return "SELECT substr(table_name, length(" + prefix + ") + 1) AS name FROM duckdb_tables() WHERE database_name = current_database() "
				+ "AND schema_name = 'main' AND starts_with(table_name, " + prefix + ")";
	}
}
