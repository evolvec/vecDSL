package org.ftn.vecDSL.generator.lancedb

import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
import org.ftn.vecDSL.vecDSL.Condition
import org.ftn.vecDSL.vecDSL.EComparisonOp
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ElementPredicate
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.GeoPredicate
import org.ftn.vecDSL.vecDSL.ListLit
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NotCondition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.StringLit

/**
 * Builds a LanceDB where string (SQL parsed by Lance with DataFusion) from a vecDSL condition. Identifiers are quoted with
 * backquotes and literals by LancedbSql; program parameters become placeholders the runtime fills with the same quoting.
 * Comparisons follow SQL's three-valued logic, so a negation keeps rows whose value is null out, as the other SQL target does.
 */
class LancedbFilter {

	static val ORDERED = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE,
		EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val TEXTUAL = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]

	val LancedbSql sql
	val (Field)=>String naming

	/** A filter whose placeholders start with the prefix; naming gives the current column of a field (after renames). */
	new(String prefix, (Field)=>String naming) {
		this.sql = new LancedbSql(prefix)
		this.naming = naming
	}

	def LancedbFilter where(FilterBlock block) {
		if (block !== null) sql.where(render(block.condition))
		this
	}

	/** Adds a condition written by the generator, for example a tenant's column test. */
	def LancedbFilter where(String condition) {
		sql.where(condition)
		this
	}

	/** Adds the test column = value with the value quoted (a literal) or bound (a parameter). */
	def LancedbFilter equal(String column, Expr value) {
		sql.where(LancedbSql.id(column) + ' = ' + sql.value(value, false))
		this
	}

	/** Adds the test column IN (values) for the ids of a statement. */
	def LancedbFilter keys(String column, java.util.List<Expr> values) {
		sql.where(LancedbSql.id(column) + ' IN ' + sql.value(org.ftn.vecDSL.generator.code.Code.items(values), true))
		this
	}

	def boolean isEmpty() {
		sql.empty
	}

	/** The where argument: a string, or a template with parameter placeholders; null without conditions. */
	def Expr code() {
		sql.code
	}

	def private String render(Condition condition) {
		switch condition {
			AndCondition: '(' + render(condition.left) + ' AND ' + render(condition.right) + ')'
			OrCondition: '(' + render(condition.left) + ' OR ' + render(condition.right) + ')'
			NotCondition: 'NOT (' + render(condition.operand) + ')'
			Predicate: predicate(condition)
			GeoPredicate: throw Unsupported.of('geo predicates' + LancedbStatements.PENDING + ' (GeoArrow types and the R-tree index)')
			ElementPredicate: throw Unsupported.of("element predicates ('any'); a LanceDB where string has no quantifier over list elements")
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	def private String column(FieldPath path) {
		if (path.rawPath !== null) throw Unsupported.of('raw field paths; Lance columns are declared in the table schema')
		val names = newArrayList(naming.apply(path.field))
		for (sub : path.subFields) names += ModelUtil.fieldName(sub)
		LancedbSql.id(names.join('.'))
	}

	def private EScalarDType type(FieldPath path) {
		val field = ModelUtil.leaf(path)
		if (field instanceof PayloadField) field.type else null
	}

	def private EScalarDType elementType(FieldPath path) {
		val field = ModelUtil.leaf(path)
		if (field instanceof PayloadField) field.elementType else null
	}

	def private String predicate(Predicate test) {
		val target = test.target
		val name = column(target)
		if (test.text !== null) return textPredicate(test, name)
		if (test.lower !== null) {
			ordered(target)
			return name + ' BETWEEN ' + operand(test.lower) + ' AND ' + operand(test.upper)
		}
		if (ModelUtil.written(test, 'lengthOp')) {
			val function = if (type(target) == EScalarDType.ARRAY) 'array_length' else if (TEXTUAL.contains(type(target))) 'char_length'
				else throw Unsupported.of('length tests on ' + type(target).literal + ' field ' + name)
			return function + '(' + name + ') ' + symbol(test.lengthOp) + ' ' + test.length
		}
		if (test.value === null) return unary(test, name)
		comparison(target, name, test.op, test.value)
	}

	def private String unary(Predicate test, String name) {
		switch test.unaryOp {
			case IS_NULL,
			case NOT_EXISTS: name + ' IS NULL'
			case IS_NOT_NULL,
			case EXISTS: name + ' IS NOT NULL'
			case IS_EMPTY: {
				if (type(test.target) == EScalarDType.ARRAY) 'array_length(' + name + ') = 0'
				else if (TEXTUAL.contains(type(test.target))) name + " = ''"
				else throw Unsupported.of('is_empty on a ' + type(test.target)?.literal + ' field')
			}
		}
	}

	def private String symbol(EComparisonOp op) {
		switch op {
			case EQ: '='
			case NEQ: '<>'
			case GT: '>'
			case GTE: '>='
			case LT: '<'
			case LTE: '<='
			default: throw Unsupported.of(op.literal + ' in a length test')
		}
	}

	def private void ordered(FieldPath target) {
		val kind = type(target)
		if (kind !== null && !ORDERED.contains(kind)) throw Unsupported.of('ordered comparisons on ' + kind.literal + ' field ' + column(target))
	}

	def private String comparison(FieldPath target, String name, EComparisonOp op, Literal literal) {
		if (literal instanceof NullLit) return nullTest(name, op)
		val array = type(target) == EScalarDType.ARRAY
		switch op {
			case EQ,
			case NEQ: {
				if (array) throw Unsupported.of('eq and neq on array field ' + name + '; use contains, contains_any or contains_all')
				name + ' ' + symbol(op) + ' ' + operand(literal)
			}
			case GT,
			case GTE,
			case LT,
			case LTE: {
				ordered(target)
				name + ' ' + symbol(op) + ' ' + operand(literal)
			}
			case IN: name + ' IN ' + list(literal)
			case NOT_IN: name + ' NOT IN ' + list(literal)
			case CONTAINS: {
				if (array) 'array_has(' + name + ', ' + operand(literal) + ')'
				else if (TEXTUAL.contains(type(target))) 'contains(' + name + ', ' + operand(literal) + ')'
				else throw Unsupported.of('contains on ' + type(target).literal + ' field ' + name)
			}
			case CONTAINS_ANY: 'array_has_any(' + arrayColumn(target, name) + ', ' + operand(literal) + ')'
			case CONTAINS_ALL: 'array_has_all(' + arrayColumn(target, name) + ', ' + operand(literal) + ')'
			case LIKE: name + ' LIKE ' + pattern(literal)
			case PREFIX: 'starts_with(' + name + ', ' + operand(literal) + ')'
		}
	}

	def private String nullTest(String name, EComparisonOp op) {
		if (op == EComparisonOp.EQ) return name + ' IS NULL'
		if (op == EComparisonOp.NEQ) return name + ' IS NOT NULL'
		throw Unsupported.of(op.literal + ' with null')
	}

	def private String arrayColumn(FieldPath target, String name) {
		if (type(target) != EScalarDType.ARRAY) throw Unsupported.of('contains_any and contains_all on ' + name + ', which is not an array')
		if (elementType(target) === null) throw Unsupported.of('contains_any and contains_all on an array without element-type')
		name
	}

	/** A like pattern: % and _ keep their meaning; a literal pattern's backslashes are escaped (LIKE's escape character). */
	def private String pattern(Literal literal) {
		if (literal instanceof StringLit) return LancedbSql.like(literal.value)
		throw Unsupported.of('like with a value that is not a string literal; the generator escapes the pattern when it is generated')
	}

	/**
	 * Text operators in a where string: substring tests, SQL regular expressions, and token tests (contains_tokens, which
	 * splits on non-alphanumeric characters and compares case-sensitively, lance-datafusion/src/udf.rs, so both sides are
	 * lower-cased).
	 */
	def private String textPredicate(Predicate test, String name) {
		switch test.textOp {
			case CONTAINS_TEXT: 'contains(' + name + ', ' + operand(test.text) + ')'
			case REGEX: 'regexp_like(' + name + ', ' + operand(test.text) + ')'
			case MATCH_ALL_TERMS: 'contains_tokens(lower(' + name + '), ' + LancedbSql.text(terms(test.text).join(' ')) + ')'
			case MATCH,
			case MATCH_ANY_TERM: '(' + terms(test.text).map['contains_tokens(lower(' + name + '), ' + LancedbSql.text(it) + ')'].join(' OR ') + ')'
			default: throw Unsupported.of('text operator ' + test.textOp.literal + ' in a where string; LanceDB filters text with contains, regexp_like and contains_tokens')
		}
	}

	/** The lower-cased terms of a literal text. */
	def private java.util.List<String> terms(Literal literal) {
		if (!(literal instanceof StringLit)) throw Unsupported.of('a token test with a value that is not a string literal; the generator splits the terms when it is generated')
		val result = (literal as StringLit).value.toLowerCase(java.util.Locale.ROOT).split('\\s+').filter[!empty].toList
		if (result.empty) throw Unsupported.of('a token test without terms')
		result
	}

	def private String list(Literal literal) {
		if (literal instanceof ListLit && (literal as ListLit).items.empty) throw Unsupported.of('in and not_in with an empty list')
		sql.value(Literals.of(literal), true)
	}

	def private String operand(Literal literal) {
		sql.value(Literals.of(literal), false)
	}
}
