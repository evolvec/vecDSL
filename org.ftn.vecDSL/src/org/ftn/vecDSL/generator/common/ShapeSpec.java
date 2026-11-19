package org.ftn.vecDSL.generator.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AggregationClause;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.BoostBlock;
import org.ftn.vecDSL.vecDSL.BoostFunction;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.HybridSearch;
import org.ftn.vecDSL.vecDSL.KeywordSearch;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.MmrConfig;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.OutputSpec;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.QueryMethod;
import org.ftn.vecDSL.vecDSL.RecommendSearch;
import org.ftn.vecDSL.vecDSL.ScoreBounds;
import org.ftn.vecDSL.vecDSL.Scroll;
import org.ftn.vecDSL.vecDSL.SortClause;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorSearch;

/**
 * The runtime {@code spec} of the shared result-shape module (generator/common/shapes/README.md) for one query, so that a vendor's
 * statements emit {@code rt.shape(<candidates>, <spec>)} with one call. Modeled on pgvector's {@code PgvectorQueries.Shape} and
 * {@code PgvectorStatements.shapeOf}; a clause the program does not write is null (N1: {@link ModelUtil#written}), and the module
 * holds the defaults (group size 1, weight 1, rate 0.5, offset 0, combine and apply multiply, 10 buckets).
 *
 * <p>Keys, in order, with the grammar (VecDSL.xtext, vecDSL 1.0) lines they read: offset (Paging, 437), limit ('top' 484 and the
 * same line of each search; Scroll 'limit' 644), cursor (Paging, 438), bounds (ScoreSpec 453, ScoreBounds 685 to 688), score
 * (ScoreSpec 452; null for a scroll), boost (Reordering 459, BoostBlock 753 to 757, BoostFunction 762 to 768, conditions 782 to
 * 813), mmr (Reordering 458, MmrConfig 699 to 702), group (ResultShape 465), sort (ResultShape 470, Scroll 647, SortClause 740 to
 * 741), withTotal (ResultShape 467), aggregations (ResultShape 471, AggregationClause 745 to 749), output (OutputSpec 680 to 681),
 * exclude (ResultShape 468), withVectors (ResultShape 466, Scroll 648), keyField (the collection's primary key). Field paths
 * (FieldPath 818 to 820) are lists of segments.
 */
public final class ShapeSpec {

	private final Entries spec;
	private final boolean plain;
	private final Expr window;
	private final List<String> reads;
	private final String diversityField;

	private ShapeSpec(Entries spec, boolean plain, Expr window, List<String> reads, String diversityField) {
		this.spec = spec;
		this.plain = plain;
		this.window = window;
		this.reads = reads;
		this.diversityField = diversityField;
	}

	/** The spec of a vector, keyword, hybrid or recommend search, or of a scroll. */
	public static ShapeSpec of(QueryMethod query) {
		if (!(query instanceof VectorSearch || query instanceof KeywordSearch || query instanceof HybridSearch || query instanceof RecommendSearch
				|| query instanceof Scroll)) {
			throw new IllegalArgumentException("result shapes apply to vector, keyword, hybrid and recommend searches and to scrolls, not to "
					+ query.eClass().getName());
		}
		return new Builder(query).build();
	}

	/**
	 * The shared module's source for a host language (python, typescript or java), which a vendor generator writes next to its
	 * runtime under {@link #moduleFile(String)}; the Java text still holds the {@code {{java.package}}} placeholder.
	 */
	public static String module(String language) {
		String path = "shapes/" + language + "/" + switch (language) {
		case "python" -> "vecdsl_shapes.py";
		case "typescript" -> "vecdslShapes.ts";
		case "java" -> "Shapes.java.template";
		default -> throw new IllegalArgumentException("no shared result-shape module for " + language);
		};
		try (InputStream in = ShapeSpec.class.getResourceAsStream(path)) {
			if (in == null) throw new IllegalStateException("missing resource " + path);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException error) {
			throw new UncheckedIOException(error);
		}
	}

	/** The file name of the shared module in a generated project of the host language. */
	public static String moduleFile(String language) {
		return switch (language) {
		case "python" -> "vecdsl_shapes.py";
		case "typescript" -> "vecdslShapes.ts";
		case "java" -> "Shapes.java";
		default -> throw new IllegalArgumentException("no shared result-shape module for " + language);
		};
	}

	/** The runtime spec, with exactly the keys the module reads, in its order. */
	public Entries spec() {
		return spec;
	}

	/** True when no shape clause (cursor, boost, mmr, group-by, sort, with-total, aggregate) is written: the target maps it natively. */
	public boolean plain() {
		return plain;
	}

	/** The number of candidates to retrieve (the module's windowSize), or Nil for every matching record. */
	public Expr window() {
		return window;
	}

	/** Top-level fields the module reads beyond the output: group, sort, boost and aggregation fields; the candidates carry them. */
	public List<String> reads() {
		return reads;
	}

	/** The vector field MMR compares, which the candidates carry in their vectors; null without mmr. */
	public String diversityField() {
		return diversityField;
	}

	private static final class Builder {

		private final QueryMethod query;
		private final VectorCollection collection;
		private final Set<String> reads = new LinkedHashSet<>();
		private String diversity;

		Builder(QueryMethod query) {
			this.query = query;
			this.collection = (VectorCollection) feature("collection");
		}

		private Object feature(String name) {
			return feature(query, name);
		}

		private static Object feature(EObject object, String name) {
			EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
			return feature == null ? null : object.eGet(feature);
		}

		private List<?> list(String name) {
			Object value = feature(name);
			return value instanceof List<?> items ? items : List.of();
		}

		private boolean written(String name) {
			return ModelUtil.written(query, name);
		}

		ShapeSpec build() {
			boolean scroll = query instanceof Scroll;
			Object cursor = feature("cursor");
			FieldPath grouping = (FieldPath) feature("groupBy");
			MmrConfig mmr = (MmrConfig) feature("mmr");
			BoostBlock boost = (BoostBlock) feature("boost");
			List<?> sortClauses = list("sortClauses");
			boolean nativeScore = written("scoreForm") && feature("scoreForm") == EScoreForm.NATIVE;
			if (cursor != null && (grouping != null || mmr != null)) {
				throw Unsupported.of("the 'cursor' clause with group-by or mmr; keyset pagination continues after one record in an order that each record's own values decide");
			}
			if (grouping != null && !sortClauses.isEmpty()) throw Unsupported.of("sort with group-by; groups follow the relevance of their best hit");
			if (grouping != null && mmr != null) throw Unsupported.of("mmr with group-by; MMR selects single hits, groups select records per group value");
			if (boost != null && nativeScore) throw Unsupported.of("boosts with score native; boosts apply to the canonical similarity, where higher is better");
			if (mmr != null && nativeScore) throw Unsupported.of("mmr with score native; MMR weighs the canonical similarity, which a native score does not give");

			int offset = written("offset") ? (Integer) feature("offset") : 0;
			Integer limit = scroll ? (written("scrollLimit") ? (Integer) feature("scrollLimit") : null) : (Integer) feature("topK");
			Entries result = Code.entries();
			result.put("offset", written("offset") ? Code.num(offset) : new Code.Nil());
			result.put("limit", limit == null ? new Code.Nil() : Code.num(limit));
			result.put("cursor", cursor == null ? new Code.Nil() : Code.text((String) cursor));
			result.put("bounds", bounds());
			result.put("score", scroll ? new Code.Nil() : Code.text(nativeScore ? "native" : "similarity"));
			result.put("boost", boost == null ? new Code.Nil() : boost(boost));
			result.put("mmr", mmr == null ? new Code.Nil() : mmr(mmr));
			result.put("group", grouping == null ? new Code.Nil() : group(grouping));
			result.put("sort", sort(sortClauses, scroll));
			result.put("withTotal", Code.flag(Boolean.TRUE.equals(feature("withTotal"))));
			result.put("aggregations", aggregations());
			result.put("output", output());
			result.put("exclude", names(list("excluded")));
			result.put("withVectors", Code.flag(Boolean.TRUE.equals(feature("withVectors"))));
			Field key = collection.getPrimaryKey();
			result.put("keyField", Code.text(key == null ? "id" : ModelUtil.fieldName(key)));

			boolean shaped = cursor != null || boost != null || mmr != null || grouping != null || !sortClauses.isEmpty()
					|| Boolean.TRUE.equals(feature("withTotal")) || !list("aggregations").isEmpty();
			return new ShapeSpec(result, !shaped, window(scroll, limit, offset, mmr, shaped, cursor != null, !sortClauses.isEmpty()),
					List.copyOf(reads), diversity);
		}

		/** windowSize of the module, at generation time (shapes/README.md, the window rule). */
		private Expr window(boolean scroll, Integer limit, int offset, MmrConfig mmr, boolean shaped, boolean cursor, boolean sorted) {
			if (limit == null) return new Code.Nil();
			int fetch = limit + offset;
			if (scroll) return cursor || sorted ? new Code.Nil() : Code.num(fetch);
			if (mmr != null && ModelUtil.written(mmr, "candidates")) return Code.num(Math.max(mmr.getCandidates(), fetch));
			return Code.num(shaped ? 10 * fetch : fetch);
		}

		private Expr bounds() {
			ScoreBounds bounds = (ScoreBounds) feature("bounds");
			if (bounds == null) return new Code.Nil();
			Entries result = Code.entries();
			result.put("min", ModelUtil.written(bounds, "min") ? Code.num(bounds.getMin()) : new Code.Nil());
			result.put("max", ModelUtil.written(bounds, "max") ? Code.num(bounds.getMax()) : new Code.Nil());
			return result;
		}

		private Expr boost(BoostBlock block) {
			List<Expr> functions = new ArrayList<>();
			for (BoostFunction fn : block.getFunctions()) {
				boolean decay = ModelUtil.written(fn, "shape");
				switch (fn.getKind()) {
				case FIELD_VALUE -> {
					if (fn.getTarget() == null) throw Unsupported.of("a field_value boost without a field");
				}
				case DECAY -> {
					if (fn.getTarget() == null || !decay) throw Unsupported.of("a decay boost without a field, decay shape, origin and scale");
					if (ModelUtil.written(fn, "decayRate") && (fn.getDecayRate() <= 0 || fn.getDecayRate() >= 1)) {
						throw Unsupported.of("a decay rate outside the open interval from 0 to 1");
					}
				}
				case CONDITION -> {
					if (fn.getCondition() == null) throw Unsupported.of("a condition boost without a condition");
				}
				}
				Entries entry = Code.entries();
				entry.put("name", fn.getName() == null ? new Code.Nil() : Code.text(fn.getName()));
				entry.put("kind", Code.text(fn.getKind().getLiteral()));
				entry.put("field", fn.getTarget() == null ? new Code.Nil() : path(fn.getTarget(), "boost"));
				entry.put("weight", ModelUtil.written(fn, "weight") ? Code.num(fn.getWeight()) : new Code.Nil());
				entry.put("shape", decay ? Code.text(fn.getShape().getLiteral()) : new Code.Nil());
				entry.put("origin", decay ? value(fn.getOrigin(), "a decay origin") : new Code.Nil());
				entry.put("scale", decay ? value(fn.getScale(), "a decay scale") : new Code.Nil());
				entry.put("offset", fn.getDecayOffset() == null ? new Code.Nil() : value(fn.getDecayOffset(), "a decay offset"));
				entry.put("rate", ModelUtil.written(fn, "decayRate") ? Code.num(fn.getDecayRate()) : new Code.Nil());
				entry.put("when", fn.getCondition() == null ? new Code.Nil() : condition(fn.getCondition()));
				functions.add(entry);
			}
			Entries result = Code.entries();
			result.put("functions", Code.items(functions));
			result.put("combine", ModelUtil.written(block, "combine") ? Code.text(block.getCombine().getLiteral()) : new Code.Nil());
			result.put("apply", ModelUtil.written(block, "apply") ? Code.text(block.getApply().getLiteral()) : new Code.Nil());
			return result;
		}

		private Expr mmr(MmrConfig config) {
			if (config.getDiversityLambda() < 0 || config.getDiversityLambda() > 1) throw Unsupported.of("an mmr lambda outside 0 to 1");
			VectorField field = config.getVectorField() != null ? config.getVectorField() : searched();
			diversity = ModelUtil.fieldName(field);
			Entries result = Code.entries();
			result.put("lambda", Code.num(config.getDiversityLambda()));
			result.put("candidates", ModelUtil.written(config, "candidates") ? Code.num(config.getCandidates()) : new Code.Nil());
			result.put("vectorField", Code.text(diversity));
			result.put("metric", metric(field));
			return result;
		}

		/** The searched vector field (N5): the one named by using, else the collection's only vector field. */
		private VectorField searched() {
			Object named = feature("vectorField");
			if (named instanceof VectorField field) return field;
			List<VectorField> fields = ModelUtil.vectorFields(collection);
			if (fields.size() != 1) throw Unsupported.of("mmr in a collection with several vector fields without 'using'; name the diversity field in the mmr clause");
			return fields.get(0);
		}

		/** The diversity field's metric: the field's own, else its index's; null leaves it to the target's default. */
		private Expr metric(VectorField field) {
			if (ModelUtil.written(field, "metric")) return Code.text(field.getMetric().getLiteral());
			VectorIndex index = ModelUtil.vectorIndex(collection, field);
			if (index != null && ModelUtil.written(index, "metric")) return Code.text(index.getMetric().getLiteral());
			return new Code.Nil();
		}

		private Expr group(FieldPath grouping) {
			Entries result = Code.entries();
			result.put("field", path(grouping, "group-by"));
			result.put("size", written("groupSize") ? Code.num((Integer) feature("groupSize")) : new Code.Nil());
			return result;
		}

		private Expr sort(List<?> clauses, boolean scroll) {
			List<Expr> result = new ArrayList<>();
			for (Object item : clauses) {
				SortClause clause = (SortClause) item;
				Entries entry = Code.entries();
				if (clause.isByRelevance()) {
					if (scroll) throw Unsupported.of("sort by relevance on a scroll, whose records have no score");
					entry.put("relevance", Code.flag(true));
				} else {
					entry.put("field", path(clause.getField(), "sort"));
				}
				entry.put("direction", Code.text(clause.getDirection().getLiteral()));
				result.add(entry);
			}
			return Code.items(result);
		}

		private Expr aggregations() {
			List<Expr> result = new ArrayList<>();
			for (Object item : list("aggregations")) {
				AggregationClause clause = (AggregationClause) item;
				String kind = clause.getKind().getLiteral();
				List<String> segments = segments(clause.getField());
				Expr interval = new Code.Nil();
				switch (kind) {
				case "range" -> throw Unsupported.of("range aggregations; vecDSL gives no range bounds to count records in");
				case "histogram" -> {
					if (clause.getInterval() == null || !clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")) throw Unsupported.of("a histogram without a numeric interval");
					interval = Code.num(Double.parseDouble(clause.getInterval()));
				}
				case "date_histogram" -> {
					if (clause.getInterval() == null) throw Unsupported.of("a date histogram without an interval");
					interval = Code.text(clause.getInterval());
				}
				default -> {
				}
				}
				Entries entry = Code.entries();
				entry.put("name", Code.text(clause.getAlias() != null ? clause.getAlias() : kind + "_" + String.join("_", segments)));
				entry.put("kind", Code.text(kind));
				entry.put("field", path(clause.getField(), "aggregations"));
				entry.put("interval", interval);
				entry.put("size", ModelUtil.written(clause, "bucketCount") ? Code.num(clause.getBucketCount()) : new Code.Nil());
				result.add(entry);
			}
			return Code.items(result);
		}

		private Expr output() {
			OutputSpec output = (OutputSpec) feature("output");
			if (output == null || output.isAllFields()) return new Code.Nil();
			return names(output.getFields());
		}

		private static Expr names(List<?> fields) {
			List<String> result = new ArrayList<>();
			for (Object field : fields) result.add(ModelUtil.fieldName((Field) field));
			return Code.texts(result);
		}

		/** A field path as its segments: the field and its sub-fields, or a raw path split at its dots. */
		private Expr path(FieldPath path, String purpose) {
			List<String> segments = segments(path);
			if (segments.isEmpty()) throw Unsupported.of("an empty field path in " + purpose);
			reads.add(segments.get(0));
			return Code.texts(segments);
		}

		private static List<String> segments(FieldPath path) {
			if (path.getRawPath() != null) return List.of(path.getRawPath().split("\\.", -1));
			List<String> result = new ArrayList<>();
			result.add(ModelUtil.fieldName(path.getField()));
			for (Field sub : path.getSubFields()) result.add(ModelUtil.fieldName(sub));
			return result;
		}

		private static Expr value(Literal literal, String what) {
			if (literal instanceof SparseLit || literal instanceof EnvRef) throw Unsupported.of(what + " that is a sparse vector or an environment reference");
			return Literals.of(literal);
		}

		/** A boost condition as the module evaluates it: and, or, not, and field predicates without text analysis. */
		private Expr condition(Condition node) {
			Entries result = Code.entries();
			if (node instanceof AndCondition and) {
				result.put("and", Code.items(List.of(condition(and.getLeft()), condition(and.getRight()))));
			} else if (node instanceof OrCondition or) {
				result.put("or", Code.items(List.of(condition(or.getLeft()), condition(or.getRight()))));
			} else if (node instanceof NotCondition not) {
				result.put("not", condition(not.getOperand()));
			} else if (node instanceof Predicate p) {
				result.put("field", path(p.getTarget(), "a boost condition"));
				if (ModelUtil.written(p, "textOp")) {
					if (!"contains_text".equals(p.getTextOp().getLiteral())) {
						throw Unsupported.of("the text operator " + p.getTextOp().getLiteral()
								+ " in a boost condition; the shared result-shape module tests values, not analyzed text (contains_text is a substring test)");
					}
					result.put("op", Code.text("contains_text"));
					result.put("value", value(p.getText(), "a condition value"));
				} else if (p.getLower() != null) {
					result.put("op", Code.text("between"));
					result.put("value", value(p.getLower(), "a condition value"));
					result.put("upper", value(p.getUpper(), "a condition value"));
				} else if (ModelUtil.written(p, "lengthOp")) {
					String op = p.getLengthOp().getLiteral();
					if (!List.of("eq", "neq", "gt", "gte", "lt", "lte").contains(op)) throw Unsupported.of("length " + op + "; length tests compare with a number");
					result.put("op", Code.text("length_" + op));
					result.put("value", Code.num(p.getLength()));
				} else if (ModelUtil.written(p, "unaryOp")) {
					result.put("op", Code.text(p.getUnaryOp().getLiteral()));
				} else {
					if (p.getValue() instanceof NullLit) throw Unsupported.of("comparisons with null; write is_null or is_not_null");
					result.put("op", Code.text(p.getOp().getLiteral()));
					result.put("value", value(p.getValue(), "a condition value"));
				}
			} else {
				throw Unsupported.of(node.eClass().getName() + " in a boost condition; the shared result-shape module tests the fields of each candidate");
			}
			return result;
		}
	}
}
