package org.ftn.vecDSL.generator.milvus

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.common.Py
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.BoolLit
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.EComparisonOp
import org.ftn.vecDSL.vecDSL.EGeoUnit
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ETextOp
import org.ftn.vecDSL.vecDSL.EUnaryOp
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.EnvRef
import org.ftn.vecDSL.vecDSL.Field
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

/**
 * Builds a Milvus boolean expression from vecDSL conditions. Program parameters and environment values
 * become {name} placeholders that the runtime fills with escaped Milvus literals, so the same expression
 * runs on standalone and embedded deployments.
 */
class MilvusFilter {

	static val OPEN = String.valueOf(1 as char)
	static val CLOSE = String.valueOf(2 as char)

	val List<String> conjuncts = newArrayList
	val Map<String, Expr> values = new LinkedHashMap

	def MilvusFilter where(FilterBlock block) {
		if (block !== null) {
			conjuncts += render(block.condition)
		}
		this
	}

	def MilvusFilter add(String expression) {
		conjuncts += expression
		this
	}

	def boolean isEmpty() {
		conjuncts.empty
	}

	def String placeholder(String name, Expr value) {
		values.put(name, value)
		OPEN + name + CLOSE
	}

	def Code.Filter code() {
		if (conjuncts.empty) {
			return null
		}
		var template = if (conjuncts.size == 1) conjuncts.head else conjuncts.map['(' + it + ')'].join(' and ')
		for (name : values.keySet) {
			template = template.replace(OPEN + name + CLOSE, '{' + name + '}')
		}
		new Code.Filter(template, new LinkedHashMap(values))
	}

	def String render(Condition condition) {
		switch condition {
			OrCondition: render(condition.left) + ' or ' + render(condition.right)
			AndCondition: grouped(condition.left) + ' and ' + grouped(condition.right)
			NotCondition: 'not (' + render(condition.operand) + ')'
			Predicate: predicate(condition)
			GeoPredicate: geo(condition)
			ElementPredicate: throw Unsupported.of("element predicates ('any')")
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	def private String grouped(Condition condition) {
		if (condition instanceof OrCondition) '(' + render(condition) + ')' else render(condition)
	}

	def String value(Literal literal) {
		switch literal {
			StringLit: Py.str(literal.value)
			NumberLit: Py.num(literal.value)
			BoolLit: if (literal.value) 'true' else 'false'
			ListLit: '[' + literal.items.map[value(it)].join(', ') + ']'
			ParamRef: placeholder(Py.ident(literal.param.name), new Code.Param(literal.param.name))
			EnvRef: placeholder('env_' + Py.ident(literal.variable), new Code.Env(literal.variable))
			NullLit: throw Unsupported.of("null literals in comparisons; use is_null")
			default: throw Unsupported.of(literal.eClass.name + ' in filters')
		}
	}

	def static String path(FieldPath path) {
		if (path.rawPath !== null) {
			val segments = path.rawPath.split('\\.')
			return segments.head + segments.tail.map['["' + it + '"]'].join
		}
		val root = ModelUtil.fieldName(path.field)
		if (path.subFields.empty) {
			return root
		}
		if (!isJson(path.field)) {
			throw Unsupported.of('nested paths into ' + root + ', which is not a JSON field')
		}
		root + path.subFields.map['["' + ModelUtil.fieldName(it) + '"]'].join
	}

	def static boolean isJson(Field field) {
		field instanceof PayloadField && #[EScalarDType.JSON, EScalarDType.MAP, EScalarDType.STRUCT].contains((field as PayloadField).type)
	}

	def static boolean isArray(Field field) {
		field instanceof PayloadField && (field as PayloadField).type == EScalarDType.ARRAY
	}

	def private String predicate(Predicate predicate) {
		val field = path(predicate.target)
		val jsonPath = predicate.target.rawPath !== null || !predicate.target.subFields.empty || isJson(predicate.target.field)
		if (predicate.value !== null) {
			return comparison(field, ModelUtil.leaf(predicate.target), jsonPath, predicate.op, predicate.value)
		}
		if (predicate.text !== null) {
			return text(field, predicate.textOp, predicate.text)
		}
		if (predicate.lower !== null) {
			return field + ' >= ' + value(predicate.lower) + ' and ' + field + ' <= ' + value(predicate.upper)
		}
		if (ModelUtil.written(predicate, 'lengthOp')) {
			return 'ARRAY_LENGTH(' + field + ') ' + operator(predicate.lengthOp) + ' ' + predicate.length
		}
		unary(field, jsonPath, predicate.unaryOp)
	}

	def private String comparison(String field, Field leaf, boolean jsonPath, EComparisonOp op, Literal literal) {
		if (literal instanceof NullLit) {
			if (op == EComparisonOp.EQ) return field + ' is null'
			if (op == EComparisonOp.NEQ) return field + ' is not null'
		}
		val symbol = operator(op)
		if (symbol !== null) {
			return field + ' ' + symbol + ' ' + value(literal)
		}
		switch op {
			case IN: field + ' in ' + value(literal)
			case NOT_IN: field + ' not in ' + value(literal)
			case LIKE: field + ' like ' + value(literal)
			case PREFIX: field + ' like ' + Py.str(constant(literal, 'prefix') + '%')
			case CONTAINS:
				if (jsonPath) 'JSON_CONTAINS(' + field + ', ' + value(literal) + ')'
				else if (isArray(leaf)) 'ARRAY_CONTAINS(' + field + ', ' + value(literal) + ')'
				else field + ' like ' + Py.str('%' + constant(literal, 'contains') + '%')
			case CONTAINS_ANY: (if (jsonPath) 'JSON_CONTAINS_ANY(' else 'ARRAY_CONTAINS_ANY(') + field + ', ' + value(literal) + ')'
			case CONTAINS_ALL: (if (jsonPath) 'JSON_CONTAINS_ALL(' else 'ARRAY_CONTAINS_ALL(') + field + ', ' + value(literal) + ')'
			default: throw Unsupported.of('operator ' + op.literal)
		}
	}

	def private String operator(EComparisonOp op) {
		switch op {
			case EQ: '=='
			case NEQ: '!='
			case GT: '>'
			case GTE: '>='
			case LT: '<'
			case LTE: '<='
			default: null
		}
	}

	def private String text(String field, ETextOp op, Literal literal) {
		switch op {
			case MATCH: 'TEXT_MATCH(' + field + ', ' + value(literal) + ')'
			case MATCH_ANY_TERM: 'TEXT_MATCH(' + field + ', ' + value(literal) + ')'
			case CONTAINS_TEXT: 'TEXT_MATCH(' + field + ', ' + value(literal) + ')'
			case MATCH_ALL_TERMS: constant(literal, 'match_all_terms').trim.split('\\s+').map['TEXT_MATCH(' + field + ', ' + Py.str(it) + ')'].join(' and ')
			case PHRASE: 'PHRASE_MATCH(' + field + ', ' + value(literal) + ')'
			case WILDCARD: field + ' like ' + Py.str(constant(literal, 'wildcard').replace('*', '%').replace('?', '_'))
			default: throw Unsupported.of('text operator ' + op.literal)
		}
	}

	def private String unary(String field, boolean jsonPath, EUnaryOp op) {
		switch op {
			case IS_NULL: field + ' is null'
			case IS_NOT_NULL: field + ' is not null'
			case EXISTS: if (jsonPath) 'exists ' + field else field + ' is not null'
			case NOT_EXISTS: if (jsonPath) 'not exists ' + field else field + ' is null'
			case IS_EMPTY: 'ARRAY_LENGTH(' + field + ') == 0'
		}
	}

	def private String geo(GeoPredicate geo) {
		val field = path(geo.target)
		if (geo.center !== null) {
			return 'ST_DWITHIN(' + field + ", '" + point(geo.center) + "', " + Py.num(meters(geo.distance, geo.unit)) + ')'
		}
		if (geo.topLeft !== null) {
			val tl = geo.topLeft
			val br = geo.bottomRight
			return 'ST_WITHIN(' + field + ", '" + polygon(#[tl, corner(tl, br), br, corner(br, tl)]) + "')"
		}
		if (!geo.vertices.empty) {
			return 'ST_WITHIN(' + field + ", '" + polygon(geo.vertices) + "')"
		}
		val wkt = "'" + geo.wkt.replace("'", '') + "'"
		switch geo.relation {
			case INTERSECTS: 'ST_INTERSECTS(' + field + ', ' + wkt + ')'
			case WITHIN: 'ST_WITHIN(' + field + ', ' + wkt + ')'
			case CONTAINS: 'ST_CONTAINS(' + field + ', ' + wkt + ')'
			case DISJOINT: 'not ST_INTERSECTS(' + field + ', ' + wkt + ')'
		}
	}

	def private static String point(GeoPoint point) {
		'POINT(' + Py.num(point.longitude) + ' ' + Py.num(point.latitude) + ')'
	}

	def private static GeoPoint corner(GeoPoint latitudeOf, GeoPoint longitudeOf) {
		val corner = org.ftn.vecDSL.vecDSL.VecDSLFactory.eINSTANCE.createGeoPoint
		corner.latitude = latitudeOf.latitude
		corner.longitude = longitudeOf.longitude
		corner
	}

	def private static String polygon(List<GeoPoint> vertices) {
		val ring = newArrayList
		ring += vertices.map[Py.num(longitude) + ' ' + Py.num(latitude)]
		if (ring.head != ring.last) {
			ring += ring.head
		}
		'POLYGON((' + ring.join(', ') + '))'
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
