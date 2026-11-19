package org.ftn.vecDSL.generator.mongodb

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
import org.ftn.vecDSL.vecDSL.ListLit
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.SparseLit
import org.ftn.vecDSL.vecDSL.StringLit

import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.*

/**
 * Translates a vecDSL filter into an MQL query document. Every value is the operand of an explicit operator ($eq, $in, ...),
 * so a value that looks like an operator stays a literal. A negation is $nor, which (like the other vecDSL targets) matches
 * documents without the field. In a $vectorSearch pre-filter only $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or
 * and $nor on indexed paths are allowed; other conditions raise Unsupported there.
 */
class MongodbFilter {

	static val EARTH_RADIUS_KM = 6378.1
	static val VECTOR_REASON = '; the $vectorSearch pre-filter accepts $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or, $not and $nor on boolean, date, numeric, string and UUID values'

	val MongodbCollection collection
	val boolean vector

	new(MongodbCollection collection, boolean vector) {
		this.collection = collection
		this.vector = vector
	}

	def Entries query(Condition node) {
		query(node, false)
	}

	def private Entries query(Condition node, boolean element) {
		switch node {
			AndCondition: obj('$and', arr(query(node.left, element), query(node.right, element)))
			OrCondition: obj('$or', arr(query(node.left, element), query(node.right, element)))
			NotCondition: obj('$nor', arr(query(node.operand, element)))
			Predicate: predicate(node, element)
			GeoPredicate: geo(node, element)
			ElementPredicate: elementQuery(node, element)
			default: throw Unsupported.of(node.eClass.name)
		}
	}

	/** A field path as MongoDB names it: _id for the key, dotted sub-fields, relative paths inside $elemMatch. */
	def private String resolve(FieldPath path, boolean element) {
		if (path.rawPath !== null) {
			for (segment : path.rawPath.split('\\.')) MongodbCommands.field(segment)
			return path.rawPath
		}
		val List<String> segments = newArrayList
		segments += if (element) ModelUtil.fieldName(path.field) else collection.path(ModelUtil.fieldName(path.field))
		segments += path.subFields.map[s | ModelUtil.fieldName(s)]
		val joined = segments.join('.')
		if (vector && !collection.filterPaths.contains(joined)) throw Unsupported.of('a condition on ' + joined + ', which the vectorSearch index does not filter' + VECTOR_REASON)
		joined
	}

	def private boolean dated(FieldPath path) {
		val leaf = ModelUtil.leaf(path)
		leaf instanceof PayloadField && #['date', 'datetime', 'timestamptz'].contains((leaf as PayloadField).type.literal)
			|| (leaf instanceof PayloadField && (leaf as PayloadField).type.literal == 'array' && #['date', 'datetime', 'timestamptz'].contains((leaf as PayloadField).elementType.literal))
	}

	def private Entries predicate(Predicate p, boolean element) {
		val path = resolve(p.target, element)
		val dates = dated(p.target)
		if (p.text !== null) return textCondition(p.textOp.literal, path, p.text)
		if (p.lower !== null) return obj(path, obj('$gte', operand(p.lower, dates), '$lte', operand(p.upper, dates)))
		if (ModelUtil.written(p, 'lengthOp')) {
			if (vector) throw Unsupported.of('length conditions in a vector search' + VECTOR_REASON)
			val size = obj('$cond', arr(obj('$isArray', arr(text('$' + path))), obj('$size', text('$' + path)), obj('$cond', arr(obj('$eq', arr(obj('$type', text('$' + path)), text('missing'))), num(0), num(1)))))
			return obj('$expr', obj('$' + lengthOperator(p.lengthOp.literal), arr(size, num(p.length))))
		}
		if (p.value === null) {
			return switch p.unaryOp.literal {
				case 'exists': obj(path, obj('$exists', flag(true)))
				case 'is_not_null': {
					if (vector) throw Unsupported.of('is_not_null in a vector search; null is not a filterable value' + VECTOR_REASON)
					obj(path, obj('$ne', nil))
				}
				case 'is_null': {
					if (vector) throw Unsupported.of('is_null in a vector search; null is not a filterable value' + VECTOR_REASON)
					obj(path, obj('$eq', nil))
				}
				default: obj(path, obj('$exists', flag(false)))
			}
		}
		if (p.value instanceof NullLit) throw Unsupported.of('comparisons with null; write is_null or is_not_null')
		comparison(p.op.literal, path, p.value, dates)
	}

	def private String lengthOperator(String op) {
		switch op { case 'neq': 'ne' default: op }
	}

	def private Expr nil() {
		new org.ftn.vecDSL.generator.code.Code.Nil
	}

	def private Entries comparison(String op, String path, Literal value, boolean dates) {
		switch op {
			case 'eq',
			case 'contains': obj(path, obj('$eq', operand(value, dates)))
			case 'neq': obj(path, obj('$ne', operand(value, dates)))
			case 'gt',
			case 'gte',
			case 'lt',
			case 'lte': obj(path, obj('$' + op, operand(value, dates)))
			case 'in',
			case 'contains_any': obj(path, obj('$in', operand(value, dates)))
			case 'not_in': obj(path, obj('$nin', operand(value, dates)))
			case 'contains_all': {
				if (!vector) {
					obj(path, obj('$all', operand(value, dates)))
				} else if (value instanceof ListLit) {
					obj('$and', arr((value as ListLit).items.map[item | obj(path, obj('$eq', operand(item, dates))) as Expr].toList))
				} else {
					throw Unsupported.of('contains_all with a parameter in a vector search; $all is not a pre-filter operator, and the generator spells it as $and of $eq from a list literal')
				}
			}
			case 'prefix': {
				if (vector) throw Unsupported.of('prefix conditions in a vector search' + VECTOR_REASON)
				if (value instanceof StringLit) obj(path, obj('$regex', text('^' + escaped((value as StringLit).value))))
				else obj('$expr', obj('$eq', arr(obj('$indexOfCP', arr(obj('$ifNull', arr(text('$' + path), text(''))), operand(value, false))), num(0))))
			}
			case 'like': {
				if (vector) throw Unsupported.of('like conditions in a vector search' + VECTOR_REASON)
				obj(path, obj('$regex', text(likePattern(constant(value, 'like')))))
			}
			default: throw Unsupported.of('the comparison ' + op)
		}
	}

	/** Text operators outside $search: regular expressions on the stored string; analyzed matching belongs to keyword search. */
	def private Entries textCondition(String op, String path, Literal value) {
		if (vector) throw Unsupported.of('text conditions in a vector search' + VECTOR_REASON)
		switch op {
			case 'regex': obj(path, obj('$regex', text(constant(value, 'regex'))))
			case 'wildcard': obj(path, obj('$regex', text(wildcardPattern(constant(value, 'wildcard')))))
			case 'match',
			case 'match_all_terms',
			case 'match_any_term',
			case 'phrase',
			case 'fuzzy': throw Unsupported.of('the text operator ' + op + ' in a filter; MQL compares stored values, and analyzed matching is the $search stage of a keyword search')
			default: {
				if (value instanceof StringLit) obj(path, obj('$regex', text(escaped((value as StringLit).value))))
				else obj('$expr', obj('$gte', arr(obj('$indexOfCP', arr(obj('$ifNull', arr(text('$' + path), text(''))), operand(value, false))), num(0))))
			}
		}
	}

	def private Entries geo(GeoPredicate g, boolean element) {
		if (vector) throw Unsupported.of('geo conditions in a vector search' + VECTOR_REASON)
		val path = resolve(g.target, element)
		if (g.center !== null) {
			val kilometres = switch g.unit.literal { case 'km': g.distance case 'miles': g.distance * 1.609344 default: g.distance / 1000.0 }
			return obj(path, obj('$geoWithin', obj('$centerSphere', arr(point(g.center), num(kilometres / EARTH_RADIUS_KM)))))
		}
		val List<Expr> ring = newArrayList
		if (g.topLeft !== null) {
			ring += point(g.topLeft)
			ring += arr(num(g.bottomRight.longitude), num(g.topLeft.latitude))
			ring += point(g.bottomRight)
			ring += arr(num(g.topLeft.longitude), num(g.bottomRight.latitude))
			ring += point(g.topLeft)
		} else if (!g.vertices.empty) {
			for (vertex : g.vertices) ring += point(vertex)
			ring += point(g.vertices.head)
		} else {
			throw Unsupported.of('WKT geo relations; MQL takes GeoJSON geometries ($geoWithin, $geoIntersects), and the generator does not parse WKT')
		}
		obj(path, obj('$geoWithin', obj('$geometry', obj('type', text('Polygon'), 'coordinates', arr(arr(ring))))))
	}

	def private Expr point(GeoPoint p) {
		arr(num(p.longitude), num(p.latitude))
	}

	/** 'any field (condition)': $elemMatch with the condition's paths relative to the array element. */
	def private Entries elementQuery(ElementPredicate e, boolean element) {
		if (vector) throw Unsupported.of("element predicates ('any') in a vector search" + VECTOR_REASON)
		val path = resolve(e.target, element)
		obj(path, obj('$elemMatch', query(e.condition, true)))
	}

	/** A filter value; strings compared with date fields become extended JSON dates, which the runtimes send as BSON dates. */
	def private Expr operand(Literal literal, boolean dates) {
		switch literal {
			SparseLit: throw Unsupported.of('sparse vectors in a filter')
			EnvRef: throw Unsupported.of('environment references in a filter')
			StringLit: if (dates) obj('$date', text(literal.value)) else Literals.of(literal)
			ListLit: if (dates) arr(literal.items.map[item | operand(item, true)].toList) else Literals.of(literal)
			default: Literals.of(literal)
		}
	}

	def private String constant(Literal literal, String op) {
		if (!(literal instanceof StringLit)) throw Unsupported.of('the ' + op + ' operator with a value that is not a string literal; its pattern is built from the literal')
		(literal as StringLit).value
	}

	/** A regular expression that matches the text literally. */
	def static String escaped(String value) {
		val out = new StringBuilder
		for (c : value.toCharArray) {
			if ('\\^$.|?*+()[]{}/-'.indexOf(c) >= 0) out.append('\\')
			out.append(c)
		}
		out.toString
	}

	/** A SQL LIKE pattern as an anchored regular expression: % is .*, _ is ., everything else literal. */
	def static String likePattern(String like) {
		val out = new StringBuilder('^')
		for (c : like.toCharArray) {
			switch String.valueOf(c) {
				case '%': out.append('.*')
				case '_': out.append('.')
				default: out.append(escaped(String.valueOf(c)))
			}
		}
		out.append('$').toString
	}

	/** A wildcard pattern (* any characters, ? one character) as an anchored regular expression. */
	def static String wildcardPattern(String wildcard) {
		val out = new StringBuilder('^')
		for (c : wildcard.toCharArray) {
			switch String.valueOf(c) {
				case '*': out.append('.*')
				case '?': out.append('.')
				default: out.append(escaped(String.valueOf(c)))
			}
		}
		out.append('$').toString
	}
}
