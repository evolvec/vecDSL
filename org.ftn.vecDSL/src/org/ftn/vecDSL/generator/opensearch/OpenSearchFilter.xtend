package org.ftn.vecDSL.generator.opensearch

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.EnvRef
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.GeoPoint
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.SparseLit
import org.ftn.vecDSL.vecDSL.StringLit

import static org.ftn.vecDSL.generator.opensearch.OpenSearchBodies.*

/**
 * Translates a vecDSL filter into the OpenSearch query DSL, for the filter context of a bool query or of a kNN search.
 * Comparisons use term, terms, range, exists, prefix, wildcard and regexp queries on the exact form of a field (the keyword
 * sub-field of a full-text field); text operators use match and match_phrase on the analyzed field; geo predicates use
 * geo_distance, geo_bounding_box and geo_shape; element predicates and paths into nested fields use nested queries. A
 * negation is a must_not clause, so documents without the field match it, as the other vecDSL targets treat records without
 * the field. Every value is a JSON value of the query.
 */
class OpenSearchFilter {

	val OpenSearchIndex index

	new(OpenSearchIndex index) {
		this.index = index
	}

	def Entries query(Condition node) {
		query(node, null)
	}

	def private Entries query(Condition node, String element) {
		switch node {
			AndCondition: obj('bool', obj('filter', arr(query(node.left, element), query(node.right, element))))
			OrCondition: obj('bool', obj('should', arr(query(node.left, element), query(node.right, element)), 'minimum_should_match', num(1)))
			NotCondition: obj('bool', obj('must_not', arr(query(node.operand, element))))
			Predicate: predicate(node, element)
			GeoPredicate: geo(node, element)
			ElementPredicate: elementQuery(node, element)
			default: throw Unsupported.of(node.eClass.name)
		}
	}

	/** A field path as OpenSearch names it (dotted), with the exact (keyword) form and the nested field it lies in. */
	def private List<String> resolve(FieldPath path, String element) {
		if (path.rawPath !== null) {
			OpenSearchBodies.field(path.rawPath)
			return #[path.rawPath, path.rawPath, null]
		}
		val List<String> segments = newArrayList
		segments += ModelUtil.fieldName(path.field)
		segments += path.subFields.map[s | ModelUtil.fieldName(s)]
		val leaf = ModelUtil.leaf(path)
		val fullText = leaf instanceof PayloadField && ((leaf as PayloadField).fullText || (leaf as PayloadField).analyzer !== null)
		if (element !== null) {
			val dotted = element + '.' + segments.join('.')
			return #[dotted, if (fullText) dotted + '.' + RAW else dotted, null]
		}
		val top = index.field(segments.head)
		val name = (#[top.name] + segments.tail).join('.')
		#[name, if (fullText) name + '.' + RAW else name, if (top.nested && segments.size > 1) top.name else null]
	}

	def private Entries nested(String path, Entries inner) {
		if (path === null) inner else obj('nested', obj('path', text(path), 'query', inner))
	}

	def private Entries predicate(Predicate p, String element) {
		val names = resolve(p.target, element)
		val name = names.get(0)
		val exact = names.get(1)
		nested(names.get(2), if (p.text !== null) {
			textQuery(p.textOp.literal, name, exact, p.text)
		} else if (p.lower !== null) {
			obj('range', obj(exact, obj('gte', operand(p.lower), 'lte', operand(p.upper))))
		} else if (ModelUtil.written(p, 'lengthOp')) {
			obj('script', obj('script', script(LENGTH_SCRIPT, obj('field', text(exact), 'op', text(p.lengthOp.literal), 'value', num(p.length)))))
		} else if (p.value === null) {
			switch p.unaryOp.literal {
				case 'exists',
				case 'is_not_null': obj('exists', obj('field', text(exact)))
				default: obj('bool', obj('must_not', arr(obj('exists', obj('field', text(exact))))))
			}
		} else {
			if (p.value instanceof NullLit) throw Unsupported.of('comparisons with null; write is_null or is_not_null')
			comparison(p.op.literal, exact, p.value)
		})
	}

	def private Entries comparison(String op, String exact, Literal value) {
		switch op {
			case 'eq',
			case 'contains': obj('term', obj(exact, operand(value)))
			case 'neq': obj('bool', obj('must_not', arr(obj('term', obj(exact, operand(value))))))
			case 'gt',
			case 'gte',
			case 'lt',
			case 'lte': obj('range', obj(exact, obj(op, operand(value))))
			case 'in',
			case 'contains_any': obj('terms', obj(exact, operand(value)))
			case 'not_in': obj('bool', obj('must_not', arr(obj('terms', obj(exact, operand(value))))))
			case 'contains_all': obj('terms_set', obj(exact, obj('terms', operand(value), 'minimum_should_match_script', obj('source', text('params.num_terms')))))
			case 'prefix': obj('prefix', obj(exact, obj('value', operand(value))))
			case 'like': obj('wildcard', obj(exact, obj('value', text(wildcard(constant(value, 'like'))))))
			default: throw Unsupported.of('the comparison ' + op)
		}
	}

	/** Text operators: match queries on the analyzed field, wildcard and regexp on the exact form. */
	def private Entries textQuery(String op, String name, String exact, Literal value) {
		switch op {
			case 'match': obj('match', obj(name, obj('query', operand(value))))
			case 'match_all_terms': obj('match', obj(name, obj('query', operand(value), 'operator', text('and'))))
			case 'match_any_term': obj('match', obj(name, obj('query', operand(value), 'operator', text('or'))))
			case 'phrase': obj('match_phrase', obj(name, obj('query', operand(value))))
			case 'fuzzy': obj('match', obj(name, obj('query', operand(value), 'fuzziness', text('AUTO'))))
			case 'wildcard': obj('wildcard', obj(exact, obj('value', operand(value))))
			case 'regex': obj('regexp', obj(exact, obj('value', operand(value))))
			default: {
				if (name != exact) obj('match_phrase', obj(name, obj('query', operand(value))))
				else obj('wildcard', obj(exact, obj('value', text('*' + escaped(constant(value, 'contains_text')) + '*'))))
			}
		}
	}

	def private Entries geo(GeoPredicate g, String element) {
		val names = resolve(g.target, element)
		val name = names.get(0)
		nested(names.get(2), if (g.center !== null) {
			val unit = switch g.unit.literal { case 'km': 'km' case 'miles': 'mi' default: 'm' }
			obj('geo_distance', obj('distance', text(number(g.distance) + unit), name, point(g.center)))
		} else if (g.topLeft !== null) {
			obj('geo_bounding_box', obj(name, obj('top_left', point(g.topLeft), 'bottom_right', point(g.bottomRight))))
		} else if (!g.vertices.empty) {
			val List<Expr> ring = newArrayList
			for (vertex : g.vertices) ring += arr(num(vertex.longitude), num(vertex.latitude))
			ring += arr(num(g.vertices.head.longitude), num(g.vertices.head.latitude))
			obj('geo_shape', obj(name, obj('shape', obj('type', text('polygon'), 'coordinates', arr(arr(ring))), 'relation', text('within'))))
		} else {
			obj('geo_shape', obj(name, obj('shape', text(g.wkt), 'relation', text(g.relation.literal))))
		})
	}

	def private Entries point(GeoPoint p) {
		obj('lat', num(p.latitude), 'lon', num(p.longitude))
	}

	/** 'any field (condition)': a nested query on a nested field, with the condition's paths below it. */
	def private Entries elementQuery(ElementPredicate e, String element) {
		val names = resolve(e.target, element)
		val leaf = ModelUtil.leaf(e.target)
		if (element === null && e.target.rawPath === null && !(leaf instanceof PayloadField && (leaf as PayloadField).nested)) {
			throw Unsupported.of("element predicates ('any') on " + names.get(0) + ', which is not a nested field; OpenSearch matches the conditions of one array element only in nested fields')
		}
		obj('nested', obj('path', text(names.get(0)), 'query', query(e.condition, names.get(0))))
	}

	def private Expr operand(Literal literal) {
		switch literal {
			SparseLit: throw Unsupported.of('sparse vectors in a filter')
			EnvRef: throw Unsupported.of('environment references in a filter')
			default: Literals.of(literal)
		}
	}

	def private String constant(Literal literal, String op) {
		if (!(literal instanceof StringLit)) throw Unsupported.of('the ' + op + ' operator with a value that is not a string literal; its pattern is built from the literal')
		(literal as StringLit).value
	}

	/** A SQL LIKE pattern as an OpenSearch wildcard pattern: % is *, _ is ?, and literal * ? \ are escaped. */
	def static String wildcard(String like) {
		val out = new StringBuilder
		for (c : like.toCharArray) {
			switch String.valueOf(c) {
				case '%': out.append('*')
				case '_': out.append('?')
				case '*',
				case '?',
				case '\\': out.append('\\').append(c)
				default: out.append(c)
			}
		}
		out.toString
	}

	def static String escaped(String value) {
		value.replace('\\', '\\\\').replace('*', '\\*').replace('?', '\\?')
	}

	def static String number(double value) {
		if (value == Math.rint(value) && Math.abs(value) < 1e15) String.valueOf(value as long) else String.valueOf(value)
	}
}
