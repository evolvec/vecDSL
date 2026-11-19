package org.ftn.vecDSL.generator.qdrant

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Items
import org.ftn.vecDSL.generator.code.Code.Num
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.BoolLit
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.EComparisonOp
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.GeoPoint
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.ListLit
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.NumberLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.ParamRef
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.StringLit

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.qdrant.QdrantSchema.kw

/**
 * Builds a Qdrant filter in the JSON form of the REST API, which the Python and TypeScript clients take as is and the
 * Java runtime converts to gRPC conditions: {"must": [...]}, {"should": [...]}, {"must_not": [...]} and field
 * conditions {"key": "price", "range": {"lte": 60}}. Qdrant matches exact values on strings, integers and booleans
 * only, so equality on float fields becomes a closed range.
 */
class QdrantFilter {

	static val FLOATING = #[EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val TEMPORAL = #[EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]
	static val ORDERED = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE,
		EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]

	val List<Expr> conjuncts = newArrayList

	def QdrantFilter where(FilterBlock block) {
		if (block !== null) conjuncts += render(block.condition)
		this
	}

	def QdrantFilter where(Entries condition) {
		conjuncts += condition
		this
	}

	def boolean isEmpty() {
		conjuncts.empty
	}

	/** The filter object: the conjuncts under "must", with nested conjunctions spliced in. */
	def Expr code() {
		if (conjuncts.empty) return null
		val List<Expr> flat = newArrayList
		for (conjunct : conjuncts) {
			val clause = conjunct as Entries
			if (clause.entries().size == 1 && clause.has('must')) flat += (clause.get('must') as Items).items() else flat += conjunct
		}
		kw('must', items(flat))
	}

	def private Entries render(Condition condition) {
		switch condition {
			OrCondition: kw('should', items(#[render(condition.left), render(condition.right)]))
			AndCondition: kw('must', items(#[render(condition.left), render(condition.right)]))
			NotCondition: kw('must_not', items(#[render(condition.operand)]))
			Predicate: predicate(condition)
			GeoPredicate: geo(condition)
			ElementPredicate: element(condition)
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	/** Payload key of a field path: sub-fields joined by '.'; inside an element predicate, relative to the element. */
	def private String key(FieldPath path) {
		QdrantSchema.indexPath(path)
	}

	def private EScalarDType scalarType(FieldPath path) {
		val leaf = ModelUtil.leaf(path)
		if (path.rawPath !== null || !(leaf instanceof PayloadField)) return null
		(leaf as PayloadField).type
	}

	def private boolean isArray(FieldPath path) {
		scalarType(path) == EScalarDType.ARRAY
	}

	/** Value type the predicate compares against: the element type of an array field. */
	def private EScalarDType valueType(FieldPath path) {
		val leaf = ModelUtil.leaf(path)
		if (isArray(path)) return (leaf as PayloadField).elementType
		scalarType(path)
	}

	def private Entries condition(FieldPath target, String kind, Expr value) {
		kw('key', text(key(target)), kind, value)
	}

	def private Entries predicate(Predicate predicate) {
		val target = predicate.target
		if (predicate.text !== null) return textPredicate(target, predicate)
		if (predicate.value !== null) return comparison(target, predicate.op, predicate.value)
		if (predicate.lower !== null) {
			ordered(target, 'between')
			return condition(target, 'range', kw('gte', operand(predicate.lower), 'lte', operand(predicate.upper)))
		}
		if (ModelUtil.written(predicate, 'lengthOp')) return length(target, predicate.lengthOp, predicate.length)
		val emptiness = kw('is_empty', kw('key', text(key(target))))
		switch predicate.unaryOp {
			case IS_EMPTY: emptiness
			case IS_NULL,
			case NOT_EXISTS: {
				scalarOnly(target, predicate.unaryOp.literal)
				emptiness
			}
			case IS_NOT_NULL,
			case EXISTS: {
				scalarOnly(target, predicate.unaryOp.literal)
				kw('must_not', items(#[emptiness]))
			}
		}
	}

	/** Qdrant's is_empty also matches an empty array, so null tests are generated for non-array fields only. */
	def private void scalarOnly(FieldPath target, String operator) {
		if (isArray(target)) throw Unsupported.of(operator + ' on array field ' + key(target) + '; Qdrant tests an array for emptiness, not for null')
	}

	def private void ordered(FieldPath target, String operator) {
		val type = valueType(target)
		if (type !== null && !ORDERED.contains(type)) {
			throw Unsupported.of(operator + ' on ' + type.literal + ' field ' + key(target) + '; Qdrant ranges compare numbers and datetimes')
		}
	}

	/** Exact-value condition; floats match through a closed range because Qdrant matches only strings, integers and booleans. */
	def private Entries equal(FieldPath target, Literal literal) {
		if (literal instanceof NullLit) return kw('is_empty', kw('key', text(key(target))))
		val type = valueType(target)
		val value = operand(literal)
		if (FLOATING.contains(type) || TEMPORAL.contains(type) || (type === null && value instanceof Num && !(value as Num).integral())) {
			return condition(target, 'range', kw('gte', value, 'lte', value))
		}
		condition(target, 'match', kw('value', value))
	}

	def private Entries comparison(FieldPath target, EComparisonOp op, Literal literal) {
		switch op {
			case EQ: equal(target, literal)
			case NEQ: kw('must_not', items(#[equal(target, literal)]))
			case GT: bound(target, op, 'gt', literal)
			case GTE: bound(target, op, 'gte', literal)
			case LT: bound(target, op, 'lt', literal)
			case LTE: bound(target, op, 'lte', literal)
			case IN: oneOf(target, literal)
			case NOT_IN: kw('must_not', items(#[oneOf(target, literal)]))
			case CONTAINS: equal(target, literal)
			case CONTAINS_ANY: {
				if (!isArray(target)) throw Unsupported.of('contains_any on ' + key(target) + ', which is not an array')
				oneOf(target, literal)
			}
			case CONTAINS_ALL: {
				if (!isArray(target)) throw Unsupported.of('contains_all on ' + key(target) + ', which is not an array')
				if (!(literal instanceof ListLit)) throw Unsupported.of('contains_all with a value that is not a list literal')
				kw('must', items((literal as ListLit).items.map[equal(target, it)]))
			}
			case PREFIX: {
				textual(target, op.literal)
				condition(target, 'match', kw('prefix', operand(literal)))
			}
			case LIKE: like(target, literal)
		}
	}

	def private Entries bound(FieldPath target, EComparisonOp op, String kind, Literal literal) {
		ordered(target, op.literal)
		condition(target, 'range', kw(kind, operand(literal)))
	}

	/** Membership: match any for strings and integers, a disjunction of closed ranges for floats. */
	def private Entries oneOf(FieldPath target, Literal literal) {
		val type = valueType(target)
		if (FLOATING.contains(type)) {
			if (!(literal instanceof ListLit)) throw Unsupported.of('in on float field ' + key(target) + ' with a value that is not a list literal')
			return kw('should', items((literal as ListLit).items.map[equal(target, it)]))
		}
		condition(target, 'match', kw('any', operand(literal)))
	}

	def private void textual(FieldPath target, String operator) {
		val type = valueType(target)
		if (type !== null && type != EScalarDType.VARCHAR && type != EScalarDType.TEXT && type != EScalarDType.UUID) {
			throw Unsupported.of(operator + ' on ' + type.literal + ' field ' + key(target))
		}
	}

	/** like: a pattern without wildcards is equality, 'p%' a prefix and '%s%' a substring; other patterns have no Qdrant form. */
	def private Entries like(FieldPath target, Literal literal) {
		textual(target, 'like')
		if (!(literal instanceof StringLit)) throw Unsupported.of('like with a pattern that is not a string literal')
		val pattern = (literal as StringLit).value
		val core = pattern.replaceAll('^%|%$', '')
		if (core.contains('%') || core.contains('_')) {
			throw Unsupported.of("like pattern '" + pattern + "'; Qdrant matches exact values, prefixes and substrings")
		}
		if (pattern.length > 1 && pattern.startsWith('%') && pattern.endsWith('%')) return condition(target, 'match', kw('substring', text(core)))
		if (pattern.endsWith('%') && !pattern.startsWith('%')) return condition(target, 'match', kw('prefix', text(core)))
		if (!pattern.contains('%')) return condition(target, 'match', kw('value', text(core)))
		throw Unsupported.of("like pattern '" + pattern + "'; Qdrant matches exact values, prefixes and substrings")
	}

	/** Array length through values_count. */
	def private Entries length(FieldPath target, EComparisonOp op, int size) {
		if (!isArray(target)) throw Unsupported.of('length on ' + key(target) + ', which is not an array')
		val count = num(size)
		switch op {
			case EQ: condition(target, 'values_count', kw('gte', count, 'lte', count))
			case NEQ: kw('must_not', items(#[condition(target, 'values_count', kw('gte', count, 'lte', count))]))
			case GT: condition(target, 'values_count', kw('gt', count))
			case GTE: condition(target, 'values_count', kw('gte', count))
			case LT: condition(target, 'values_count', kw('lt', count))
			case LTE: condition(target, 'values_count', kw('lte', count))
			default: throw Unsupported.of('length with operator ' + op.literal)
		}
	}

	def private Entries textPredicate(FieldPath target, Predicate predicate) {
		textual(target, predicate.textOp.literal)
		switch predicate.textOp {
			case MATCH,
			case MATCH_ANY_TERM,
			case CONTAINS_TEXT: condition(target, 'match', kw('text_any', operand(predicate.text)))
			case MATCH_ALL_TERMS: condition(target, 'match', kw('text', operand(predicate.text)))
			case PHRASE: condition(target, 'match', kw('phrase', operand(predicate.text)))
			default: throw Unsupported.of('text operator ' + predicate.textOp.literal + '; Qdrant matches terms and phrases')
		}
	}

	def private Entries geo(GeoPredicate predicate) {
		val target = predicate.target
		if (predicate.center !== null) {
			val meters = switch predicate.unit {
				case KILOMETERS: predicate.distance * 1000
				case MILES: predicate.distance * 1609.344
				default: predicate.distance
			}
			return condition(target, 'geo_radius', kw('center', point(predicate.center), 'radius', num(meters)))
		}
		if (predicate.topLeft !== null) {
			return condition(target, 'geo_bounding_box', kw('top_left', point(predicate.topLeft), 'bottom_right', point(predicate.bottomRight)))
		}
		if (!predicate.vertices.empty) {
			val List<Expr> ring = newArrayList
			ring += predicate.vertices.map[point(it)]
			val first = predicate.vertices.head
			val last = predicate.vertices.last
			if (first.latitude != last.latitude || first.longitude != last.longitude) ring += point(first)
			return condition(target, 'geo_polygon', kw('exterior', kw('points', items(ring))))
		}
		throw Unsupported.of('geo relation predicates with WKT; Qdrant filters by radius, bounding box and polygon')
	}

	def private Entries point(GeoPoint point) {
		kw('lon', num(point.longitude), 'lat', num(point.latitude))
	}

	/** 'any' over an array of objects: a nested condition whose keys are relative to the array element. */
	def private Entries element(ElementPredicate predicate) {
		if (!isArray(predicate.target)) throw Unsupported.of("element predicates ('any') on " + key(predicate.target) + ', which is not an array')
		val inner = render(predicate.condition)
		val filter = if (inner.has('must') || inner.has('should') || inner.has('must_not')) inner else kw('must', items(#[inner]))
		kw('nested', kw('key', text(key(predicate.target)), 'filter', filter))
	}

	def private Expr operand(Literal literal) {
		if (literal instanceof NullLit) throw Unsupported.of('null literals in comparisons other than eq and neq')
		if (!(literal instanceof StringLit || literal instanceof NumberLit || literal instanceof ListLit || literal instanceof ParamRef
			|| literal instanceof BoolLit)) {
			throw Unsupported.of(literal.eClass.name + ' in a filter')
		}
		Literals.of(literal)
	}
}
