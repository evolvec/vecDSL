package org.ftn.vecDSL.generator.pgvector

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.And
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Cond
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Not
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Or
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Raw
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Test
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate

/**
 * Translates a vecDSL filter into PgvectorSql conditions, which PgvectorSql renders as a WHERE clause with bound
 * parameters. Fields are columns; a path below a nested or json field reads the jsonb value with #>> and casts it to the
 * sub-field's type. A negation matches rows where the negated condition is not true, so rows with a null value match it,
 * as the other vecDSL targets treat records without the field.
 */
class PgvectorFilter {

	static val PATH_TYPES = #{EScalarDType.INT -> 'integer', EScalarDType.BIGINT -> 'bigint', EScalarDType.INT8 -> 'smallint',
		EScalarDType.FLOAT -> 'real', EScalarDType.DOUBLE -> 'double precision', EScalarDType.BOOL -> 'boolean',
		EScalarDType.VARCHAR -> 'text', EScalarDType.TEXT -> 'text', EScalarDType.DATE -> 'date', EScalarDType.DATETIME -> 'timestamp',
		EScalarDType.TIMESTAMPTZ -> 'timestamptz', EScalarDType.UUID -> 'uuid'}

	val List<Cond> collected = newArrayList

	def PgvectorFilter where(FilterBlock block) {
		if (block !== null) collected += condition(block.condition)
		this
	}

	/** A slice condition: tenant, namespace or partitions. */
	def PgvectorFilter slice(String column, List<Expr> values) {
		collected += new Raw(column, values)
		this
	}

	def List<Cond> parts() {
		collected
	}

	def boolean isEmpty() {
		collected.empty
	}

	def Cond condition(Condition node) {
		switch node {
			AndCondition: new And(#[condition(node.left), condition(node.right)])
			OrCondition: new Or(#[condition(node.left), condition(node.right)])
			NotCondition: new Not(condition(node.operand))
			Predicate: test(node)
			GeoPredicate: throw Unsupported.of('geo predicates; has no pgvector mapping yet (earthdistance in contrib)')
			ElementPredicate: throw Unsupported.of("element predicates ('any'); has no pgvector mapping yet (jsonb_path_exists)")
			default: throw Unsupported.of(node.eClass.name)
		}
	}

	def private Cond test(Predicate p) {
		val path = p.target
		if (path.rawPath !== null) throw Unsupported.of('raw paths; the generator filters declared fields and their sub-fields')
		val column = ModelUtil.fieldName(path.field)
		val segments = path.subFields.map[ModelUtil.fieldName(it)].toList
		val pathType = if (segments.empty) null else leafType(path)
		if (p.text !== null) {
			if (p.textOp.literal == 'fuzzy') throw Unsupported.of('the fuzzy text operator; has no pgvector mapping yet (pg_trgm in contrib)')
			return new Test(column, segments, pathType, p.textOp.literal, operand(p.text), null)
		}
		if (p.lower !== null) {
			return new Test(column, segments, pathType, 'between', operand(p.lower), operand(p.upper))
		}
		if (ModelUtil.written(p, 'lengthOp')) {
			if (!#['eq', 'neq', 'gt', 'gte', 'lt', 'lte'].contains(p.lengthOp.literal)) throw Unsupported.of('length ' + p.lengthOp.literal + '; length tests compare with a number')
			return new Test(column, segments, pathType, 'length_' + p.lengthOp.literal, org.ftn.vecDSL.generator.code.Code.num(p.length), null)
		}
		if (p.value === null) return new Test(column, segments, pathType, p.unaryOp.literal, null, null)
		if (p.value instanceof NullLit) {
			throw Unsupported.of('comparisons with null; write is_null or is_not_null')
		}
		new Test(column, segments, pathType, p.op.literal, operand(p.value), null)
	}

	/** The SQL type of a sub-field read from jsonb: its declared scalar type. */
	def static String leafType(FieldPath path) {
		val leaf = ModelUtil.leaf(path)
		if (!(leaf instanceof PayloadField)) throw Unsupported.of('a vector field below a nested field')
		val type = PATH_TYPES.get((leaf as PayloadField).type)
		if (type === null) throw Unsupported.of('filters on sub-field ' + ModelUtil.fieldName(leaf) + ' of type ' + (leaf as PayloadField).type.literal)
		type
	}

	def private Expr operand(Literal literal) {
		switch literal {
			org.ftn.vecDSL.vecDSL.SparseLit: throw Unsupported.of('sparse vectors in a filter')
			org.ftn.vecDSL.vecDSL.EnvRef: throw Unsupported.of('environment references in a filter')
			default: Literals.of(literal)
		}
	}
}
