package org.ftn.vecDSL.generator.vespa

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.vespa.VespaPackage.Schema
import org.ftn.vecDSL.generator.vespa.VespaQueries.And
import org.ftn.vecDSL.generator.vespa.VespaQueries.Cond
import org.ftn.vecDSL.generator.vespa.VespaQueries.Not
import org.ftn.vecDSL.generator.vespa.VespaQueries.Or
import org.ftn.vecDSL.generator.vespa.VespaQueries.Test
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
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.VectorCollection

import static org.ftn.vecDSL.generator.code.Code.*

/**
 * Translates a vecDSL filter into VespaQueries conditions on the fields of the collection's schema. Every value becomes an @parameter
 * of the YQL. A negation matches the documents where the negated condition does not hold, so documents without the field match it,
 * as on the other vecDSL targets.
 */
class VespaFilter {

	/** Why full-text fields have no fuzzy match in YQL. */
	public static val NO_FUZZY = 'the fuzzy operator matches string attributes (single, array or weighted set), not the terms of an index field, so full-text fields have no fuzzy match'

	val VespaSchema schema
	val VectorCollection collection
	val Schema current
	val List<Cond> collected = newArrayList

	new(VespaSchema schema, VectorCollection collection) {
		this.schema = schema
		this.collection = collection
		this.current = schema.schema(collection)
	}

	def VespaFilter where(FilterBlock block) {
		if (block !== null) collected += condition(block.condition)
		this
	}

	/** A slice condition: the hidden attribute (tenant, namespace or partition) equals one of the values. */
	def VespaFilter slice(String attribute, List<Expr> values) {
		if (!current.has(attribute)) throw Unsupported.of('a slice of collection ' + collection.name + ', which declares no ' + attribute.substring(7))
		collected += if (values.size == 1) new Test(attribute, 'STRING', 'eq', values.head, null) else new Test(attribute, 'STRING', 'in', items(values), null)
		this
	}

	def VespaFilter add(Cond extra) {
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
			GeoPredicate: throw Unsupported.of('geo predicates; has no Vespa mapping yet (position fields and the geoLocation operator)')
			ElementPredicate: throw Unsupported.of("element predicates ('any'); has no Vespa mapping yet (sameElement over array<struct> fields)")
			default: throw Unsupported.of(node.eClass.name)
		}
	}

	def private Cond test(Predicate p) {
		val target = field(p.target)
		if (p.text !== null) return textTest(target, p)
		if (p.lower !== null) return new Test(target.name, target.kind, 'between', operand(p.lower), operand(p.upper))
		if (ModelUtil.written(p, 'lengthOp')) throw Unsupported.of('length predicates; YQL has no operator on the number of values of a field')
		if (p.value === null) {
			throw Unsupported.of('the ' + p.unaryOp.literal + ' test; YQL has no test for a missing value (YqlParser converts no IS_NULL or IS_NOT_NULL node)')
		}
		if (p.value instanceof NullLit) throw Unsupported.of('comparisons with null; write is_null or is_not_null')
		val op = p.op.literal
		switch op {
			case 'like': like(target, p.value)
			case 'contains',
			case 'contains_any',
			case 'contains_all': {
				if (!target.array()) throw Unsupported.of(op + ' on ' + target.name + ', which is not an array field')
				new Test(exact(target).name, exact(target).kind, op, operand(p.value), null)
			}
			default: new Test(exact(target).name, exact(target).kind, op, operand(p.value), null)
		}
	}

	/** Text operators on full-text fields: all terms, any term or a phrase of the user text, which is never parsed as query syntax. */
	def private Cond textTest(VespaPackage.Field target, Predicate p) {
		if (!target.text()) throw Unsupported.of('the ' + p.textOp.literal + ' text operator on ' + target.name + ', which is not a full-text field')
		val op = switch p.textOp.literal {
			case 'match',
			case 'match_all_terms': 'text_all'
			case 'match_any_term': 'text_any'
			case 'phrase': 'text_phrase'
			case 'regex': throw Unsupported.of('the regex text operator; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)')
			case 'fuzzy': throw Unsupported.of('the fuzzy text operator; ' + NO_FUZZY)
			default: throw Unsupported.of('the ' + p.textOp.literal + ' text operator; has no Vespa mapping yet (the prefix and substring term annotations)')
		}
		new Test(target.name, target.kind, op, operand(p.text), null)
	}

	/** like with one trailing %: a prefix match on an exact string field; other patterns are not generated. */
	def private Cond like(VespaPackage.Field target, Literal value) {
		if (!(value instanceof StringLit)) throw Unsupported.of('like with a pattern from a parameter; the generator reads the pattern at generation time')
		val pattern = (value as StringLit).value
		val stem = pattern.substring(0, pattern.length - 1)
		if (!pattern.endsWith('%') || stem.contains('%') || stem.contains('_')) {
			throw Unsupported.of('the like pattern ' + pattern + '; has no Vespa mapping yet (the suffix and substring term annotations)')
		}
		new Test(exact(target).name, exact(target).kind, 'prefix', text(stem), null)
	}

	/** The exact attribute of a field: the field itself, or the vecdsl_exact_ companion of a full-text field. */
	def private VespaPackage.Field exact(VespaPackage.Field target) {
		if (target.text()) current.field(VespaPackage.EXACT + target.name) else target
	}

	/** The schema field of a path: a declared field, as named after earlier renames. */
	def VespaPackage.Field field(FieldPath path) {
		if (path.rawPath !== null) throw Unsupported.of('raw paths; the generator filters declared fields')
		if (!path.subFields.empty) throw Unsupported.of('sub-field paths; has no Vespa mapping yet (struct-field attributes)')
		val named = schema.name(collection, path.field)
		if (!current.has(named)) throw Unsupported.of('field ' + named + ', which collection ' + collection.name + ' does not have at this statement')
		current.field(named)
	}

	def private Expr operand(Literal literal) {
		switch literal {
			SparseLit: throw Unsupported.of('sparse vectors in a filter')
			EnvRef: throw Unsupported.of('environment references in a filter')
			default: Literals.of(literal)
		}
	}
}
