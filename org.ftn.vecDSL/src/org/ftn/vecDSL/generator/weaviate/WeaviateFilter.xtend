package org.ftn.vecDSL.generator.weaviate

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.EComparisonOp
import org.ftn.vecDSL.vecDSL.EGeoUnit
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.VectorCollection

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.weaviate.WeaviateSchema.kw

/**
 * Builds a Weaviate filter as a data tree that each runtime turns into its SDK's filter objects:
 * {"and": [...]}, {"or": [...]}, {"not": f} and leaves {"path", "op", "value"}. Leaves on number properties
 * carry "number": true so that runtimes send floating-point values. SQL-style 'like' patterns are converted
 * to Weaviate wildcards by the runtime.
 */
class WeaviateFilter {

	val VectorCollection collection
	val List<Expr> conjuncts = newArrayList

	new(VectorCollection collection) {
		this.collection = collection
	}

	def WeaviateFilter where(FilterBlock block) {
		if (block !== null) conjuncts += render(block.condition)
		this
	}

	def boolean isEmpty() {
		conjuncts.empty
	}

	def Expr code() {
		if (conjuncts.empty) null else if (conjuncts.size == 1) conjuncts.head else kw('and', items(conjuncts))
	}

	def private Expr render(Condition condition) {
		switch condition {
			OrCondition: kw('or', items(#[render(condition.left), render(condition.right)]))
			AndCondition: kw('and', items(#[render(condition.left), render(condition.right)]))
			NotCondition: kw('not', render(condition.operand))
			Predicate: predicate(condition)
			GeoPredicate: geo(condition)
			ElementPredicate: throw Unsupported.of("element predicates ('any')")
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	def private String path(FieldPath path) {
		if (path.rawPath !== null || !path.subFields.empty) {
			throw Unsupported.of('nested paths; Weaviate filters on nested object properties are a preview feature (PROVISIONAL)')
		}
		WeaviateSchema.propertyName(collection, path.field)
	}

	def private boolean isNumber(FieldPath path) {
		val field = path.field
		field instanceof PayloadField && #[EScalarDType.FLOAT, EScalarDType.DOUBLE].contains(element(field as PayloadField))
	}

	def private boolean isArray(FieldPath path) {
		path.field instanceof PayloadField && (path.field as PayloadField).type == EScalarDType.ARRAY
	}

	def private EScalarDType element(PayloadField field) {
		if (field.type == EScalarDType.ARRAY) field.elementType else field.type
	}

	def private Entries leaf(FieldPath target, String op, Expr value) {
		val result = kw('path', text(path(target)), 'op', text(op))
		if (value !== null) result.put('value', value)
		if (isNumber(target)) result.put('number', flag(true))
		result
	}

	def private Expr predicate(Predicate predicate) {
		val target = predicate.target
		if (predicate.value !== null) return comparison(target, predicate.op, predicate.value)
		if (predicate.text !== null) return textPredicate(target, predicate)
		if (predicate.lower !== null) {
			return kw('and', items(#[leaf(target, 'gte', value(predicate.lower)), leaf(target, 'lte', value(predicate.upper))]))
		}
		if (ModelUtil.written(predicate, 'lengthOp')) return length(target, predicate.lengthOp, predicate.length)
		switch predicate.unaryOp {
			case IS_NULL,
			case NOT_EXISTS: leaf(target, 'is_null', flag(true))
			case IS_NOT_NULL,
			case EXISTS: leaf(target, 'is_null', flag(false))
			case IS_EMPTY: length(target, EComparisonOp.EQ, 0)
		}
	}

	def private Expr comparison(FieldPath target, EComparisonOp op, Literal literal) {
		if (literal instanceof NullLit) {
			if (op == EComparisonOp.EQ) return leaf(target, 'is_null', flag(true))
			if (op == EComparisonOp.NEQ) return leaf(target, 'is_null', flag(false))
		}
		switch op {
			case EQ: leaf(target, 'eq', value(literal))
			case NEQ: leaf(target, 'neq', value(literal))
			case GT: leaf(target, 'gt', value(literal))
			case GTE: leaf(target, 'gte', value(literal))
			case LT: leaf(target, 'lt', value(literal))
			case LTE: leaf(target, 'lte', value(literal))
			case IN: leaf(target, 'contains_any', value(literal))
			case NOT_IN: leaf(target, 'contains_none', value(literal))
			case LIKE: leaf(target, 'like', value(literal))
			case PREFIX: leaf(target, 'like', text(constant(literal, 'prefix') + '%'))
			case CONTAINS:
				if (isArray(target)) leaf(target, 'contains_any', items(#[value(literal)]))
				else leaf(target, 'like', text('%' + constant(literal, 'contains') + '%'))
			case CONTAINS_ANY: leaf(target, 'contains_any', value(literal))
			case CONTAINS_ALL: leaf(target, 'contains_all', value(literal))
			default: throw Unsupported.of('operator ' + op.literal)
		}
	}

	def private Expr textPredicate(FieldPath target, Predicate predicate) {
		switch predicate.textOp {
			case MATCH,
			case MATCH_ANY_TERM,
			case CONTAINS_TEXT: leaf(target, 'any_terms', value(predicate.text))
			case MATCH_ALL_TERMS: leaf(target, 'all_terms', value(predicate.text))
			case WILDCARD: leaf(target, 'wildcard', value(predicate.text))
			default: throw Unsupported.of('text operator ' + predicate.textOp.literal)
		}
	}

	def private Expr length(FieldPath target, EComparisonOp op, int length) {
		if (!#[EComparisonOp.EQ, EComparisonOp.NEQ, EComparisonOp.GT, EComparisonOp.GTE, EComparisonOp.LT, EComparisonOp.LTE].contains(op)) {
			throw Unsupported.of('length operator ' + op.literal)
		}
		kw('path', text(path(target)), 'op', text('length'), 'compare', text(op.literal), 'value', num(length))
	}

	def private Expr geo(GeoPredicate geo) {
		if (geo.center === null) throw Unsupported.of('geo predicates other than radius')
		kw(
			'path', text(path(geo.target)),
			'op', text('geo'),
			'latitude', num(geo.center.latitude),
			'longitude', num(geo.center.longitude),
			'distance', num(meters(geo.distance, geo.unit))
		)
	}

	def private Expr value(Literal literal) {
		if (literal instanceof NullLit) throw Unsupported.of('null literals in comparisons other than eq and neq')
		Literals.of(literal)
	}

	def private static double meters(double distance, EGeoUnit unit) {
		switch unit {
			case KILOMETERS: distance * 1000
			case MILES: distance * 1609.344
			default: distance
		}
	}

	def private static String constant(Literal literal, String clause) {
		if (literal instanceof StringLit) literal.value else throw Unsupported.of(clause + ' with a non-constant value')
	}
}
