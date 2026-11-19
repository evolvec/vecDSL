package org.ftn.vecDSL.generator.redis

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.redis.RedisIndex.Index
import org.ftn.vecDSL.generator.redis.RedisQuery.And
import org.ftn.vecDSL.generator.redis.RedisQuery.Cond
import org.ftn.vecDSL.generator.redis.RedisQuery.Not
import org.ftn.vecDSL.generator.redis.RedisQuery.Or
import org.ftn.vecDSL.generator.redis.RedisQuery.Test
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.EnvRef
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.SparseLit
import org.ftn.vecDSL.vecDSL.VectorCollection

import static org.ftn.vecDSL.generator.code.Code.*

/**
 * Translates a vecDSL filter into RedisQuery conditions on the attributes of the collection's index. A negation matches documents
 * where the negated condition does not hold, so documents without the field match it, as on the other vecDSL targets.
 */
class RedisFilter {

	val RedisSchema schema
	val VectorCollection collection
	val Index index
	val List<Cond> collected = newArrayList

	new(RedisSchema schema, VectorCollection collection) {
		this.schema = schema
		this.collection = collection
		this.index = schema.index(collection)
	}

	def RedisFilter where(FilterBlock block) {
		if (block !== null) collected += condition(block.condition)
		this
	}

	/** A slice condition: the hidden tag (tenant, namespace or partition) equals one of the values. */
	def RedisFilter slice(String attribute, List<Expr> values) {
		val attr = index.attr(attribute)
		collected += if (values.size == 1) new Test(attr, 'eq', values.head, null) else new Test(attr, 'in', items(values), null)
		this
	}

	def RedisFilter add(Cond extra) {
		collected += extra
		this
	}

	def List<Cond> parts() {
		collected
	}

	def Cond condition(Condition node) {
		switch node {
			AndCondition: new And(#[condition(node.left), condition(node.right)])
			OrCondition: new Or(#[condition(node.left), condition(node.right)])
			NotCondition: new Not(condition(node.operand))
			Predicate: test(node)
			GeoPredicate: throw Unsupported.of('geo predicates; has no Redis mapping yet (GEO attributes and @field:[lon lat radius unit])')
			ElementPredicate: throw Unsupported.of("element predicates ('any'); the Query Engine indexes each array path on its own and cannot require two conditions to hold for the same element")
			default: throw Unsupported.of(node.eClass.name)
		}
	}

	def private Cond test(Predicate p) {
		val attr = index.attr(attribute(p.target))
		if (p.text !== null) return new Test(attr, p.textOp.literal, operand(p.text), null)
		if (p.lower !== null) return new Test(attr, 'between', operand(p.lower), operand(p.upper))
		if (ModelUtil.written(p, 'lengthOp')) return new Test(attr, 'length_' + p.lengthOp.literal, num(p.length), null)
		if (p.value === null) return new Test(attr, p.unaryOp.literal, null, null)
		if (p.value instanceof NullLit) throw Unsupported.of('comparisons with null; write is_null or is_not_null')
		new Test(attr, p.op.literal, operand(p.value), null)
	}

	/** The attribute of a field path: the field's (as named after earlier renames), or field_sub for a sub-field of a json field. */
	def String attribute(FieldPath path) {
		if (path.rawPath !== null) throw Unsupported.of('raw paths; the generator filters declared fields and their sub-fields')
		(#[schema.name(collection, path.field)] + path.subFields.map[ModelUtil.fieldName(it)]).join('_')
	}

	def private Expr operand(Literal literal) {
		switch literal {
			SparseLit: throw Unsupported.of('sparse vectors in a filter')
			EnvRef: throw Unsupported.of('environment references in a filter')
			default: Literals.of(literal)
		}
	}
}
