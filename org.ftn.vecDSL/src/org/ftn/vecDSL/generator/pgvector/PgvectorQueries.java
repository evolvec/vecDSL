package org.ftn.vecDSL.generator.pgvector;

import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.col;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.id;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.literal;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.number;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.object;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.output;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.table;
import static org.ftn.vecDSL.generator.pgvector.PgvectorSql.whereClause;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Column;
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Query;
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Table;
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Vec;

/**
 * Query statements over one table, each returning one json value per row. Nearest-neighbour candidates are taken in a
 * materialized CTE ordered by the pgvector distance operator (so an HNSW or IVFFlat index can serve the ORDER BY ... LIMIT),
 * and scores, bounds and the final order are applied outside it, as the pgvector README recommends for distance filters.
 */
public final class PgvectorQueries {

	private PgvectorQueries() {
	}

	/** Result fields beyond id and score; with vectors, the vector columns too. */
	public record Projection(List<String> output, List<String> vectors) {
	}

	/**
	 * A nearest-neighbour search: the vector column and metric, the query vector as SQL (a bound parameter or a scalar
	 * subquery), CTEs it needs, limit and offset, the WHERE condition, the projection, native or canonical scores, bounds
	 * on the score, and the candidate factor of a quantized index (1 without quantization).
	 */
	public record Search(Table table, Column vector, String metric, String query, String prelude, int limit, int offset, String where,
			Projection projection, boolean nativeScore, Expr min, Expr max) {
	}

	public static String vectorSearch(Query q, Search s) {
		Vec vec = s.vector().vec();
		String op = PgvectorSql.operator(vec, s.metric());
		String full = col(s.vector().name()) + " " + op + " " + s.query();
		String candidates;
		String order;
		int fetch = s.limit() + s.offset();
		boolean quantized = vec.quantization() != null;
		if ("binary".equals(vec.quantization())) {
			order = "binary_quantize(" + col(s.vector().name()) + ")::bit(" + vec.dim() + ") <~> binary_quantize(" + s.query() + ")";
		} else if ("halfvec".equals(vec.quantization())) {
			order = col(s.vector().name()) + "::halfvec(" + vec.dim() + ") " + op + " (" + s.query() + ")::halfvec(" + vec.dim() + ")";
		} else {
			order = full;
		}
		String condition = PgvectorSql.and(List.of(col(s.vector().name()) + " IS NOT NULL", s.query() + " IS NOT NULL", s.where() == null ? "" : s.where()));
		Map<String, String> carried = carried(s.table(), s.projection());
		String select = carried.values().stream().collect(Collectors.joining(", "));
		if (quantized) {
			int pool = (int) Math.ceil(fetch * Math.max(1.0, vec.oversampling()));
			candidates = "c AS MATERIALIZED (SELECT " + select + ", " + full + " AS distance FROM " + table(s.table()) + " " + PgvectorSql.ALIAS
					+ whereClause(condition) + " ORDER BY " + order + " LIMIT " + pool + "), hits AS MATERIALIZED (SELECT * FROM c ORDER BY distance, "
					+ id(s.table().key()) + " LIMIT " + s.limit() + offset(s.offset()) + ")";
		} else {
			candidates = "hits AS MATERIALIZED (SELECT " + select + ", " + full + " AS distance FROM " + table(s.table()) + " " + PgvectorSql.ALIAS
					+ whereClause(condition) + " ORDER BY distance LIMIT " + s.limit() + offset(s.offset()) + ")";
		}
		String score = s.nativeScore() ? "h.distance" : PgvectorSql.similarity(s.metric(), "h.distance");
		return "WITH " + (s.prelude() == null ? "" : s.prelude() + ", ") + candidates + " SELECT " + hitObject(s.table(), s.projection(), score)
				+ " FROM hits h" + whereClause(bounds(q, score, s.min(), s.max())) + " ORDER BY h.distance + 0, h." + id(s.table().key());
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
		if (min != null) parts.add(score + " >= " + q.bind(min, "double precision"));
		if (max != null) parts.add(score + " <= " + q.bind(max, "double precision"));
		return PgvectorSql.and(parts);
	}

	private static String offset(int offset) {
		return offset > 0 ? " OFFSET " + offset : "";
	}

	/** A field searched by keywords: the full-text column, its boost, and the tsquery operator (match, match_any_term, phrase). */
	public record KeywordField(Column column, double boost) {
	}

	/** Keyword search: ts_rank over the fields' tsvectors (weighted sum with boosts), matching any listed field. */
	public record Keywords(Table table, List<KeywordField> fields, String op, Expr text, int limit, int offset, String where,
			Projection projection, Expr min, Expr max) {
	}

	public static String keywordSearch(Query q, Keywords k) {
		String[] parts = keywordParts(q, k.fields(), k.op(), k.text());
		String condition = PgvectorSql.and(List.of(parts[1], k.where() == null ? "" : k.where()));
		String select = carried(k.table(), k.projection()).values().stream().collect(Collectors.joining(", "));
		return "WITH hits AS MATERIALIZED (SELECT " + select + ", " + parts[0] + " AS score FROM " + table(k.table()) + " " + PgvectorSql.ALIAS
				+ whereClause(condition) + " ORDER BY score DESC, " + col(k.table().key()) + " LIMIT " + k.limit() + offset(k.offset()) + ") SELECT "
				+ hitObject(k.table(), k.projection(), "h.score") + " FROM hits h" + whereClause(bounds(q, "h.score", k.min(), k.max()))
				+ " ORDER BY h.score DESC, h." + id(k.table().key());
	}

	/** The rank expression and the match condition of keyword fields. */
	private static String[] keywordParts(Query q, List<KeywordField> fields, String op, Expr text) {
		List<String> ranks = new ArrayList<>();
		List<String> matches = new ArrayList<>();
		for (KeywordField field : fields) {
			String vector = PgvectorSql.tsvector(field.column(), true);
			String query = PgvectorSql.tsquery(q, field.column().config(), op, text);
			String rank = "ts_rank(" + vector + ", " + query + ")";
			ranks.add(field.boost() == 1.0 ? rank : number(field.boost()) + " * " + rank);
			matches.add(vector + " @@ " + query);
		}
		String condition = matches.size() == 1 ? matches.get(0) : matches.stream().map(m -> "(" + m + ")").collect(Collectors.joining(" OR "));
		return new String[] { String.join(" + ", ranks), condition };
	}

	/** One leg of a hybrid search: a vector leg (vector column, metric, query SQL) or a keyword leg (fields, op, text). */
	public record Leg(Column vector, String metric, String query, List<KeywordField> fields, String op, Expr text, int limit, String where,
			double weight) {
	}

	/** Hybrid search fused in SQL: rrf sum of 1 / (k + rank); weighted sum of weight times similarity; relative_score
	 *  sum of weight times the min-max normalized leg score. Ties order by key. */
	public record Hybrid(Table table, List<Leg> legs, String fusion, int k, String prelude, int limit, int offset, Projection projection,
			Expr min, Expr max) {
	}

	public static String hybridSearch(Query q, Hybrid h) {
		List<String> ctes = fusion(q, h);
		String key = col(h.table().key());
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", key);
		fields.put("score", "f.score");
		for (String name : h.projection().output()) fields.put(name, output(h.table().column(name), PgvectorSql.ALIAS));
		for (String name : h.projection().vectors()) fields.put(name, output(h.table().column(name), PgvectorSql.ALIAS));
		return "WITH " + String.join(", ", ctes) + " SELECT " + object(fields) + " FROM f JOIN " + table(h.table()) + " " + PgvectorSql.ALIAS
				+ " ON " + key + " = f.key" + whereClause(bounds(q, "f.score", h.min(), h.max())) + " ORDER BY f.score DESC, " + key + " LIMIT "
				+ h.limit() + offset(h.offset());
	}

	/** The leg CTEs, the ranked union r and the fused scores f (key, score) of a hybrid search. */
	private static List<String> fusion(Query q, Hybrid h) {
		List<String> ctes = new ArrayList<>();
		if (h.prelude() != null) ctes.add(h.prelude());
		List<String> ranked = new ArrayList<>();
		String key = col(h.table().key());
		for (int i = 0; i < h.legs().size(); i++) {
			Leg leg = h.legs().get(i);
			if (leg.vector() != null) {
				String op = PgvectorSql.operator(leg.vector().vec(), leg.metric());
				String distance = col(leg.vector().name()) + " " + op + " " + leg.query();
				String condition = PgvectorSql.and(List.of(col(leg.vector().name()) + " IS NOT NULL", leg.query() + " IS NOT NULL",
						leg.where() == null ? "" : leg.where()));
				ctes.add("l" + i + " AS MATERIALIZED (SELECT " + key + " AS key, " + PgvectorSql.similarity(leg.metric(), "(" + distance + ")")
						+ " AS score FROM " + table(h.table()) + " " + PgvectorSql.ALIAS + whereClause(condition) + " ORDER BY " + distance + ", "
						+ key + " LIMIT " + leg.limit() + ")");
			} else {
				String[] parts = keywordParts(q, leg.fields(), leg.op(), leg.text());
				String condition = PgvectorSql.and(List.of(parts[1], leg.where() == null ? "" : leg.where()));
				ctes.add("l" + i + " AS MATERIALIZED (SELECT " + key + " AS key, " + parts[0] + " AS score FROM " + table(h.table()) + " "
						+ PgvectorSql.ALIAS + whereClause(condition) + " ORDER BY score DESC, " + key + " LIMIT " + leg.limit() + ")");
			}
			ranked.add("SELECT key, score, " + i + " AS leg, row_number() OVER (ORDER BY score DESC, key) AS rank FROM l" + i);
		}
		ctes.add("r AS (" + String.join(" UNION ALL ", ranked) + ")");
		String weight = "CASE leg " + java.util.stream.IntStream.range(0, h.legs().size())
				.mapToObj(i -> "WHEN " + i + " THEN " + number(h.legs().get(i).weight())).collect(Collectors.joining(" ")) + " END";
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
	 * The CTE of a recommendation query vector (Qdrant's average_vector strategy, as the other generators): the average of the
	 * positive examples moved away from the average of the negatives, 2 avg(positive) - avg(negative). Examples are ids looked up
	 * in the lookup table or vector literals.
	 */
	public static String recommendVector(Query q, Table lookup, Column vector, List<Expr> positiveIds, List<Expr> positiveVectors,
			List<Expr> negativeIds, List<Expr> negativeVectors) {
		if (!List.of("vector", "halfvec").contains(vector.vec().type())) {
			throw Unsupported.of("recommendation on " + vector.vec().type() + " fields; pgvector averages vector and halfvec values only (avg aggregate)");
		}
		String positive = examples(q, lookup, vector, positiveIds, positiveVectors);
		String query = negativeIds.isEmpty() && negativeVectors.isEmpty() ? "(SELECT avg(v) FROM (" + positive + ") s)"
				: "(SELECT p.v + (p.v - n.v) FROM (SELECT avg(v) AS v FROM (" + positive + ") s) p, (SELECT avg(v) AS v FROM ("
						+ examples(q, lookup, vector, negativeIds, negativeVectors) + ") s) n)";
		return "q AS MATERIALIZED (SELECT " + query + " AS v)";
	}

	private static String examples(Query q, Table lookup, Column vector, List<Expr> ids, List<Expr> vectors) {
		List<String> parts = new ArrayList<>();
		if (!ids.isEmpty()) {
			parts.add("SELECT x." + id(vector.name()) + " AS v FROM " + table(lookup) + " x WHERE x." + id(lookup.key()) + " = ANY("
					+ q.array(Code.items(ids), lookup.keyColumn().paramType()) + ")");
		}
		for (Expr value : vectors) parts.add("SELECT " + q.bind(value, vector.vec().sql()) + " AS v");
		return String.join(" UNION ALL ", parts);
	}

	// ---------------------------------------------------------------- result shapes

	/** A field a sort key, group key, aggregation or boost reads: a column, or a typed sub-field of a jsonb column. */
	public record Ref(Column column, List<String> path, String type) {

		public String sql(String alias) {
			return PgvectorSql.reference(alias, column, path, type);
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

	/** A boost function: kind, field, weight, decay shape with origin, scale, offset and rate, and the condition. */
	public record BoostFn(String kind, Ref field, double weight, String shape, Expr origin, Expr scale, Expr offset, double rate,
			List<PgvectorSql.Cond> when) {
	}

	/** Boost functions, how their values combine, and how the combined boost applies to the relevance. */
	public record Boost(List<BoostFn> functions, String combine, String apply) {
	}

	/** A keyword input whose terms a highlight marks: the tsquery operator and the text. */
	public record Terms(String op, Expr text) {
	}

	/**
	 * The shape of a search result beyond a plain hit list: a keyset cursor (the key of the last hit of the previous page),
	 * boosts, MMR, groups, the total, aggregations, highlighted full-text columns with the inputs they mark, and sort keys.
	 */
	public record Shape(Expr cursor, Boost boost, Mmr mmr, Group group, boolean total, List<Aggregate> aggregations,
			List<Column> highlights, List<Terms> terms, List<Order> sort) {

		public boolean plain() {
			return cursor == null && boost == null && mmr == null && group == null && !total && aggregations.isEmpty()
					&& highlights.isEmpty() && sort.isEmpty();
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

	/** A prefetch stage: the row ids (tableoid, ctid) of the stage's nearest rows by its vector column, metric and query. */
	public static String stage(String name, Table table, Column vector, String metric, String query, String where, int limit) {
		String distance = col(vector.name()) + " " + PgvectorSql.operator(vector.vec(), metric) + " " + query;
		String condition = PgvectorSql.and(List.of(col(vector.name()) + " IS NOT NULL", query + " IS NOT NULL", where == null ? "" : where));
		return name + " AS MATERIALIZED (SELECT " + ROW + " FROM " + table(table) + " " + PgvectorSql.ALIAS + whereClause(condition) + " ORDER BY "
				+ distance + ", " + col(table.key()) + " LIMIT " + limit + ")";
	}

	/** The condition that a row is among those the stages return; null without stages. */
	public static String within(List<String> stages) {
		if (stages.isEmpty()) return null;
		return "(t.tableoid, t.ctid) IN (" + stages.stream().map(s -> "SELECT tid, rid FROM " + s).collect(Collectors.joining(" UNION ALL ")) + ")";
	}

	private static final String ROW = "t.tableoid AS tid, t.ctid AS rid";

	private static String joined(Table table, String alias) {
		return " JOIN " + table(table) + " " + PgvectorSql.ALIAS + " ON " + PgvectorSql.ALIAS + ".tableoid = " + alias + ".tid AND "
				+ PgvectorSql.ALIAS + ".ctid = " + alias + ".rid";
	}

	/** A nearest-neighbour search with a result shape; a plain shape gives the statement of vectorSearch(q, s). */
	public static String vectorSearch(Query q, Search s, Shape shape) {
		if (shape.plain()) return vectorSearch(q, s);
		String full = col(s.vector().name()) + " " + PgvectorSql.operator(s.vector().vec(), s.metric()) + " " + s.query();
		List<String> conditions = new ArrayList<>(List.of(col(s.vector().name()) + " IS NOT NULL", s.query() + " IS NOT NULL",
				s.where() == null ? "" : s.where()));
		String matching = PgvectorSql.and(conditions);
		Integer pool = shape.pool(s.limit() + s.offset());
		List<String> ctes = new ArrayList<>();
		if (s.prelude() != null) ctes.add(s.prelude());
		boolean early = shape.cursor() != null && pool != null;
		if (early) {
			ctes.add("cursor_row AS (SELECT " + full + " AS ord, " + col(s.table().key()) + " AS key FROM " + table(s.table()) + " "
					+ PgvectorSql.ALIAS + " WHERE " + col(s.table().key()) + " = " + q.bind(shape.cursor(), s.table().keyColumn().paramType()) + ")");
			conditions.add("(" + full + ", " + col(s.table().key()) + ") > (SELECT ord, key FROM cursor_row)");
		}
		ctes.add("candidates AS MATERIALIZED (SELECT " + col(s.table().key()) + " AS key, " + ROW + ", " + full + " AS distance FROM "
				+ table(s.table()) + " " + PgvectorSql.ALIAS + whereClause(PgvectorSql.and(conditions)) + " ORDER BY distance"
				+ (pool == null ? "" : " LIMIT " + pool) + ")");
		String score = s.nativeScore() ? "distance" : PgvectorSql.similarity(s.metric(), "distance");
		ctes.add("scored AS (SELECT key, tid, rid, " + score + " AS score, distance AS ord, " + PgvectorSql.similarity(s.metric(), "distance")
				+ " AS rel FROM candidates)");
		String everything = "SELECT " + ROW + ", " + (s.nativeScore() ? full : PgvectorSql.similarity(s.metric(), "(" + full + ")")) + " AS score FROM "
				+ table(s.table()) + " " + PgvectorSql.ALIAS + whereClause(matching);
		return shaped(q, s.table(), ctes, pool == null ? null : everything, early, s.limit(), s.offset(), s.projection(), s.min(), s.max(), shape);
	}

	/** A keyword search with a result shape; a plain shape gives the statement of keywordSearch(q, k). */
	public static String keywordSearch(Query q, Keywords k, Shape shape) {
		if (shape.plain()) return keywordSearch(q, k);
		String[] parts = keywordParts(q, k.fields(), k.op(), k.text());
		List<String> conditions = new ArrayList<>(List.of(parts[1], k.where() == null ? "" : k.where()));
		String matching = PgvectorSql.and(conditions);
		Integer pool = shape.pool(k.limit() + k.offset());
		List<String> ctes = new ArrayList<>();
		boolean early = shape.cursor() != null && pool != null;
		if (early) {
			ctes.add("cursor_row AS (SELECT -(" + parts[0] + ") AS ord, " + col(k.table().key()) + " AS key FROM " + table(k.table()) + " "
					+ PgvectorSql.ALIAS + " WHERE " + col(k.table().key()) + " = " + q.bind(shape.cursor(), k.table().keyColumn().paramType()) + ")");
			conditions.add("(-(" + parts[0] + "), " + col(k.table().key()) + ") > (SELECT ord, key FROM cursor_row)");
		}
		ctes.add("candidates AS MATERIALIZED (SELECT " + col(k.table().key()) + " AS key, " + ROW + ", " + parts[0] + " AS score FROM "
				+ table(k.table()) + " " + PgvectorSql.ALIAS + whereClause(PgvectorSql.and(conditions)) + " ORDER BY score DESC, "
				+ col(k.table().key()) + (pool == null ? "" : " LIMIT " + pool) + ")");
		ctes.add("scored AS (SELECT key, tid, rid, score, -score AS ord, score AS rel FROM candidates)");
		String everything = "SELECT " + ROW + ", " + parts[0] + " AS score FROM " + table(k.table()) + " " + PgvectorSql.ALIAS
				+ whereClause(matching);
		return shaped(q, k.table(), ctes, pool == null ? null : everything, early, k.limit(), k.offset(), k.projection(), k.min(), k.max(), shape);
	}

	/** A hybrid search with a result shape; a plain shape gives the statement of hybridSearch(q, h). */
	public static String hybridSearch(Query q, Hybrid h, Shape shape) {
		if (shape.plain()) return hybridSearch(q, h);
		List<String> ctes = new ArrayList<>(fusion(q, h));
		String key = col(h.table().key());
		ctes.add("scored AS (SELECT f.key, " + ROW + ", f.score, -f.score AS ord, f.score AS rel FROM f JOIN " + table(h.table()) + " "
				+ PgvectorSql.ALIAS + " ON " + key + " = f.key)");
		return shaped(q, h.table(), ctes, null, false, h.limit(), h.offset(), h.projection(), h.min(), h.max(), shape);
	}

	/**
	 * The stages after the scored candidates (CTE scored: key, tid, rid, score, ord ascending from the best, rel the
	 * canonical relevance): boosts re-score, a cursor continues after its record, MMR, groups or limit and offset select the
	 * page, score bounds filter it, sort keys order it, and total and aggregations count the matching records (everything,
	 * or scored when it holds every match).
	 */
	private static String shaped(Query q, Table table, List<String> ctes, String everything, boolean cursorApplied, int limit, int offset,
			Projection projection, Expr min, Expr max, Shape shape) {
		String last = "scored";
		if (shape.boost() != null) {
			String boosted = apply(shape.boost().apply(), "x.rel", combine(q, table, shape.boost()));
			ctes.add("boosted AS (SELECT x.key, x.tid, x.rid, b.score, -b.score AS ord, b.score AS rel FROM scored x" + joined(table, "x")
					+ " CROSS JOIN LATERAL (SELECT " + boosted + " AS score) b)");
			last = "boosted";
		}
		String matched = everything == null ? last : "everything";
		if (shape.cursor() != null && !cursorApplied) {
			ctes.add("after_cursor AS (SELECT * FROM " + last + " WHERE (ord, key) > (SELECT ord, key FROM " + last + " WHERE key = "
					+ q.bind(shape.cursor(), table.keyColumn().paramType()) + "))");
			last = "after_cursor";
		}
		String bounds = bounds(q, "x.score", min, max);
		String hit = hitObject(table, projection, shape, q);
		String rows;
		if (shape.group() != null) {
			String groupKey = shape.group().field().sql(PgvectorSql.ALIAS);
			ctes.add("grouped AS (SELECT x.key, x.tid, x.rid, x.score, x.ord, " + groupKey + " AS grp, row_number() OVER (PARTITION BY " + groupKey
					+ " ORDER BY x.ord, x.key) AS member FROM " + last + " x" + joined(table, "x") + ")");
			ctes.add("leaders AS MATERIALIZED (SELECT grp, row_number() OVER (ORDER BY ord, key) AS pos FROM grouped WHERE member = 1 ORDER BY ord, key LIMIT "
					+ limit + offset(offset) + ")");
			ctes.add("page AS (SELECT g.key, g.tid, g.rid, g.score, g.ord, g.grp, l.pos, g.member FROM grouped g JOIN leaders l ON l.grp IS NOT DISTINCT FROM g.grp WHERE g.member <= "
					+ shape.group().size() + ")");
			rows = "hits AS (SELECT json_build_object('group', to_json(x.grp), 'hits', json_agg(" + hit + " ORDER BY x.member)) AS hit, x.pos AS n FROM page x"
					+ joined(table, "x") + whereClause(bounds) + " GROUP BY x.pos, x.grp)";
		} else {
			if (shape.mmr() != null) {
				Mmr mmr = shape.mmr();
				String v = id(mmr.vector().name());
				String diversity = PgvectorSql.similarity(mmr.metric(), "(c.v " + PgvectorSql.operator(mmr.vector().vec(), mmr.metric()) + " d.v)");
				ctes.add("pool AS MATERIALIZED (SELECT x.key, x.tid, x.rid, x.score, x.ord, x.rel, t." + v + " AS v FROM " + last + " x" + joined(table, "x")
						+ " WHERE t." + v + " IS NOT NULL ORDER BY x.ord, x.key LIMIT " + mmr.candidates() + ")");
				ctes.add("picked (keys, n) AS (SELECT ARRAY[]::" + table.keyColumn().paramType() + "[], 0 UNION ALL SELECT p.keys || c.key, p.n + 1 FROM picked p "
						+ "CROSS JOIN LATERAL (SELECT c.key FROM pool c WHERE c.key <> ALL(p.keys) ORDER BY " + number(mmr.lambda()) + " * c.rel - "
						+ number(complement(mmr.lambda())) + " * coalesce((SELECT max(" + diversity + ") FROM pool d WHERE d.key = ANY(p.keys)), 0) DESC, c.ord, c.key LIMIT 1) c "
						+ "WHERE p.n < " + (limit + offset) + ")");
				ctes.add("page AS (SELECT c.key, c.tid, c.rid, c.score, c.ord, o.pos FROM (SELECT keys FROM picked ORDER BY n DESC LIMIT 1) l "
						+ "CROSS JOIN LATERAL unnest(l.keys) WITH ORDINALITY AS o(key, pos) JOIN pool c ON c.key = o.key WHERE o.pos > " + offset + ")");
			} else {
				ctes.add("page AS MATERIALIZED (SELECT key, tid, rid, score, ord, row_number() OVER (ORDER BY ord, key) AS pos FROM " + last
						+ " ORDER BY ord, key LIMIT " + limit + offset(offset) + ")");
			}
			List<String> order = new ArrayList<>();
			for (Order o : shape.sort()) {
				order.add(o.field() == null ? "x.ord" + (o.descending() ? "" : " DESC") : o.field().sql(PgvectorSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
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
		result.put("hits", "coalesce((SELECT json_agg(hit ORDER BY n) FROM hits), '[]'::json)");
		if (shape.total()) result.put("total", "(SELECT count(*) FROM matched)");
		if (!shape.aggregations().isEmpty()) {
			Map<String, String> aggregations = new LinkedHashMap<>();
			for (Aggregate a : shape.aggregations()) aggregations.put(a.name(), aggregate(q, a));
			result.put("aggregations", object(aggregations));
		}
		return with + String.join(", ", ctes) + " SELECT " + object(result);
	}

	/** The hit object of a shaped search: id, score, output fields and vectors read from t, then the highlights. */
	private static String hitObject(Table table, Projection projection, Shape shape, Query q) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("id", col(table.key()));
		fields.put("score", "x.score");
		for (String name : projection.output()) fields.put(name, output(table.column(name), PgvectorSql.ALIAS));
		for (String name : projection.vectors()) fields.put(name, output(table.column(name), PgvectorSql.ALIAS));
		if (!shape.highlights().isEmpty()) {
			Map<String, String> marked = new LinkedHashMap<>();
			for (Column column : shape.highlights()) {
				String query = shape.terms().stream().map(t -> PgvectorSql.tsquery(q, column.config(), t.op(), t.text())).collect(Collectors.joining(" || "));
				marked.put(column.name(), "json_build_array(ts_headline(" + PgvectorSql.regconfig(column.config()) + ", coalesce(" + col(column.name())
						+ ", ''), " + query + "))");
			}
			fields.put("highlight", object(marked));
		}
		return object(fields);
	}

	/** An aggregation over the matching records (alias m), as one json value. */
	private static String aggregate(Query q, Aggregate a) {
		String value = a.field().sql("m");
		boolean array = a.field().path().isEmpty() && a.field().column().array();
		String from = array ? " FROM matched m CROSS JOIN LATERAL unnest(" + value + ") AS e(v)" : " FROM matched m";
		String v = array ? "e.v" : value;
		return switch (a.kind()) {
		case "count" -> "(SELECT to_json(count(" + v + "))" + from + ")";
		case "sum", "avg", "min", "max" -> "(SELECT to_json(" + a.kind() + "(" + v + "))" + from + ")";
		case "distinct_count", "cardinality" -> "(SELECT to_json(count(DISTINCT " + v + "))" + from + ")";
		case "stats" -> "(SELECT json_build_object('count', count(" + v + "), 'min', min(" + v + "), 'max', max(" + v + "), 'avg', avg(" + v + "), 'sum', sum(" + v + "))" + from + ")";
		case "terms", "facet" -> "(SELECT coalesce(json_agg(json_build_object('key', b.k, 'count', b.n) ORDER BY b.n DESC, b.k), '[]'::json) FROM (SELECT " + v
				+ " AS k, count(*) AS n" + from + " WHERE " + v + " IS NOT NULL GROUP BY " + v + " ORDER BY count(*) DESC, " + v + " LIMIT " + a.size() + ") b)";
		case "histogram" -> {
			String interval = q.bind(a.interval(), "double precision");
			yield buckets("floor(" + v + " / " + interval + ") * " + interval, v, from);
		}
		case "date_histogram" -> buckets("date_bin(" + q.bind(a.interval(), "interval") + ", " + v + "::timestamp, timestamp '1970-01-01')", v, from);
		case "percentiles" -> "(SELECT json_build_object(" + PERCENTS.stream().map(p -> literal(number(p)) + ", percentile_cont(" + number(p / 100) + ") WITHIN GROUP (ORDER BY " + v + ")")
				.collect(Collectors.joining(", ")) + ")" + from + ")";
		default -> throw new IllegalArgumentException(a.kind());
		};
	}

	/** Elasticsearch's default percents, so that the percentile aggregation reads the same on every target. */
	private static final List<Double> PERCENTS = List.of(1.0, 5.0, 25.0, 50.0, 75.0, 95.0, 99.0);

	private static String buckets(String key, String v, String from) {
		return "(SELECT coalesce(json_agg(json_build_object('key', b.k, 'count', b.n) ORDER BY b.k), '[]'::json) FROM (SELECT " + key + " AS k, count(*) AS n"
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
		String all = String.join(", ", values);
		String some = "num_nonnulls(" + all + ") > 0";
		return switch (boost.combine()) {
		case "sum" -> "CASE WHEN " + some + " THEN " + values.stream().map(v -> "coalesce(" + v + ", 0)").collect(Collectors.joining(" + ")) + " END";
		case "multiply" -> "CASE WHEN " + some + " THEN " + values.stream().map(v -> "coalesce(" + v + ", 1)").collect(Collectors.joining(" * ")) + " END";
		case "max" -> "greatest(" + all + ")";
		case "avg" -> "CASE WHEN " + some + " THEN (" + values.stream().map(v -> "coalesce(" + v + ", 0)").collect(Collectors.joining(" + ")) + ") / num_nonnulls(" + all + ") END";
		case "replace" -> "coalesce(" + all + ")";
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
		return "CASE WHEN " + PgvectorSql.where(q, table, fn.when()) + " THEN " + value + " END";
	}

	private static String functionValue(Query q, BoostFn fn) {
		String weight = number(fn.weight());
		String factor = fn.weight() == 1.0 ? "" : weight + " * ";
		return switch (fn.kind()) {
		case "field_value" -> factor + fn.field().sql(PgvectorSql.ALIAS) + "::double precision";
		case "condition" -> weight + "::double precision";
		case "decay" -> {
			boolean time = List.of("date", "timestamp", "timestamptz").contains(fn.field().type() == null ? fn.field().column().type() : fn.field().type());
			String field = fn.field().sql(PgvectorSql.ALIAS);
			String distance = time ? "abs(extract(epoch FROM " + field + "::timestamptz - " + q.bind(fn.origin(), "timestamptz") + "))"
					: "abs(" + field + "::double precision - " + q.bind(fn.origin(), "double precision") + ")";
			String scale = time ? "extract(epoch FROM " + q.bind(fn.scale(), "interval") + ")" : q.bind(fn.scale(), "double precision");
			String offset = fn.offset() == null ? "0" : time ? "extract(epoch FROM " + q.bind(fn.offset(), "interval") + ")" : q.bind(fn.offset(), "double precision");
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
		return "SELECT " + object(PgvectorSql.fields(table, output, PgvectorSql.ALIAS)) + " FROM " + table(table) + " " + PgvectorSql.ALIAS
				+ " JOIN unnest(" + q.array(ids, table.keyColumn().paramType()) + ") WITH ORDINALITY AS k(key, ord) ON " + col(table.key())
				+ " = k.key" + whereClause(where) + " ORDER BY k.ord";
	}

	public static String count(Table table, String where) {
		return "SELECT to_json(count(*)) FROM " + table(table) + " " + PgvectorSql.ALIAS + whereClause(where);
	}

	/** Records in sort order (the key breaks ties), with limit and offset. */
	public static String scroll(Table table, List<String> output, String where, List<String> order, Integer limit, int offset) {
		List<String> keys = new ArrayList<>(order);
		keys.add(col(table.key()));
		return "SELECT " + object(PgvectorSql.fields(table, output, PgvectorSql.ALIAS)) + " FROM " + table(table) + " " + PgvectorSql.ALIAS
				+ whereClause(where) + " ORDER BY " + String.join(", ", keys) + (limit == null ? "" : " LIMIT " + limit) + offset(offset);
	}

	/**
	 * A scroll continuing after a cursor record (keyset pagination over the sort keys and the key, nulls last) and restricted
	 * to one of n slices of the keys (the first 28 bits of the md5 of the key text, modulo n).
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
				String ref = o.field().sql(PgvectorSql.ALIAS);
				String value = "(SELECT " + o.field().sql("x") + " FROM " + table(table) + " x WHERE x." + id(table.key()) + " = " + c + ")";
				String after = "(" + value + " IS NOT NULL AND (" + ref + (o.descending() ? " < " : " > ") + value + " OR " + ref + " IS NULL))";
				alternatives.add(PgvectorSql.and(append(equal, after)));
				equal.add(ref + " IS NOT DISTINCT FROM " + value);
			}
			alternatives.add(PgvectorSql.and(append(equal, key + " > " + c)));
			conditions.add(alternatives.size() == 1 ? alternatives.get(0) : alternatives.stream().map(a -> "(" + a + ")").collect(Collectors.joining(" OR ")));
		}
		if (slices != null) {
			conditions.add("mod(('x' || substr(md5(" + key + "::text), 1, 7))::bit(28)::integer, " + slices + ") = " + slice);
		}
		List<String> keys = new ArrayList<>();
		for (Order o : order) keys.add(o.field().sql(PgvectorSql.ALIAS) + (o.descending() ? " DESC" : " ASC") + " NULLS LAST");
		keys.add(key);
		return "SELECT " + object(PgvectorSql.fields(table, output, PgvectorSql.ALIAS)) + " FROM " + table(table) + " " + PgvectorSql.ALIAS
				+ whereClause(PgvectorSql.and(conditions)) + " ORDER BY " + String.join(", ", keys) + (limit == null ? "" : " LIMIT " + limit) + offset(offset);
	}

	private static List<String> append(List<String> parts, String last) {
		List<String> result = new ArrayList<>(parts);
		result.add(last);
		return result;
	}

	/** A uniform random sample (ORDER BY random()) or the first records in key order; results are ordered by key. */
	public static String sample(Table table, List<String> output, String where, int size, boolean first) {
		String inner = "SELECT * FROM " + table(table) + " " + PgvectorSql.ALIAS + whereClause(where) + " ORDER BY "
				+ (first ? col(table.key()) : "random()") + " LIMIT " + size;
		return "SELECT " + object(PgvectorSql.fields(table, output, PgvectorSql.ALIAS)) + " FROM (" + inner + ") " + PgvectorSql.ALIAS
				+ " ORDER BY " + col(table.key());
	}

	/** A random sample, each sampled record with its nearest neighbours among the sample (Qdrant's distance matrix). */
	public static String distanceMatrix(Table table, Column vector, String metric, String where, int sample, int limit) {
		String op = PgvectorSql.operator(vector.vec(), metric);
		String v = id(vector.name());
		String key = id(table.key());
		String condition = PgvectorSql.and(List.of(col(vector.name()) + " IS NOT NULL", where == null ? "" : where));
		return "WITH s AS MATERIALIZED (SELECT " + col(table.key()) + " AS key, " + col(vector.name()) + " AS v FROM " + table(table) + " "
				+ PgvectorSql.ALIAS + whereClause(condition) + " ORDER BY random() LIMIT " + sample + ") SELECT json_build_object('a', a.key, 'b', b.key, 'score', "
				+ PgvectorSql.similarity(metric, "b.distance") + ") FROM s a CROSS JOIN LATERAL (SELECT o.key, a.v " + op
				+ " o.v AS distance FROM s o WHERE o.key <> a.key ORDER BY distance, o.key LIMIT " + limit + ") b ORDER BY a.key, b.key";
	}

	/** Table name, description, columns and indexes from the catalogs; stats adds the row count and the size in bytes. */
	public static String describe(Query q, Table table, boolean stats, boolean progress) {
		String relation = q.bind(Code.text(id(table.name())), "text");
		StringBuilder fields = new StringBuilder("'name', c.relname, 'database', current_database(), 'description', obj_description(c.oid, 'pg_class'), ")
				.append("'columns', (SELECT json_agg(json_build_object('name', a.attname, 'type', format_type(a.atttypid, a.atttypmod)) ORDER BY a.attnum) ")
				.append("FROM pg_attribute a WHERE a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped), ")
				.append("'indexes', (SELECT json_agg(json_build_object('name', i.relname, 'method', am.amname) ORDER BY i.relname) ")
				.append("FROM pg_index x JOIN pg_class i ON i.oid = x.indexrelid JOIN pg_am am ON am.oid = i.relam WHERE x.indrelid = c.oid)");
		if (stats) {
			fields.append(", 'count', (SELECT count(*) FROM ").append(table(table)).append("), 'bytes', pg_total_relation_size(c.oid)");
		}
		if (progress) {
			fields.append(", 'index_progress', (SELECT json_object_agg(i.relname, CASE WHEN x.indisvalid AND x.indisready THEN 'ready' ELSE 'building' END) ")
					.append("FROM pg_index x JOIN pg_class i ON i.oid = x.indexrelid WHERE x.indrelid = c.oid)");
		}
		return "SELECT json_build_object(" + fields + ") FROM pg_class c WHERE c.oid = to_regclass(" + relation + ")";
	}

	/** The state of an index: ready when it is valid and ready for inserts, building otherwise, missing when absent. */
	public static String indexState(Query q, String index) {
		return "SELECT to_json(coalesce((SELECT CASE WHEN x.indisvalid AND x.indisready THEN 'ready' ELSE 'building' END FROM pg_index x "
				+ "WHERE x.indexrelid = to_regclass(" + q.bind(Code.text(id(index)), "text") + ")), 'missing'))";
	}

	/** A sorted list of names from a catalog query of one 'name' column, with an optional prefix and limit. */
	public static String names(Query q, String source, Expr prefix, Integer limit) {
		String condition = prefix == null ? "" : " WHERE starts_with(name, " + q.bind(prefix, "text") + ")";
		return "SELECT coalesce(json_agg(name ORDER BY name), '[]'::json) FROM (SELECT name FROM (" + source + ") s" + condition
				+ " ORDER BY name" + (limit == null ? "" : " LIMIT " + limit) + ") s";
	}

	public static String collections() {
		return "SELECT c.relname::text AS name FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' "
				+ "AND c.relkind IN ('r', 'p') AND NOT c.relispartition";
	}

	public static String databases() {
		return "SELECT datname::text AS name FROM pg_database WHERE NOT datistemplate";
	}

	/** The indexes of a table under their vecDSL names: the collection prefix of the generated names removed. */
	public static String indexes(Query q, Table table, String collection) {
		String prefix = q.bind(Code.text(collection + "_"), "text");
		return "SELECT CASE WHEN starts_with(indexname, " + prefix + ") THEN substr(indexname, length(" + prefix + ") + 1) ELSE indexname::text END AS name "
				+ "FROM pg_indexes WHERE schemaname = 'public' AND tablename = " + q.bind(Code.text(table.name()), "text");
	}

	public static String partitions(Query q, Table table) {
		return "SELECT substr(c.relname, length(" + q.bind(Code.text(table.name() + "__"), "text") + ") + 1) AS name FROM pg_inherits h "
				+ "JOIN pg_class c ON c.oid = h.inhrelid WHERE h.inhparent = to_regclass(" + q.bind(Code.text(id(table.name())), "text") + ")";
	}

	public static String aliases(Query q, Table table) {
		String source = "SELECT DISTINCT view_name::text AS name FROM information_schema.view_table_usage WHERE view_schema = 'public'";
		return table == null ? source : source + " AND table_name = " + q.bind(Code.text(table.name()), "text");
	}

	public static String slices(Table table, String column) {
		return "SELECT DISTINCT " + id(column) + " AS name FROM " + table(table) + " WHERE " + id(column) + " IS NOT NULL";
	}
}
