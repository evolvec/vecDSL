package org.ftn.vecDSL.generator.chroma

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
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
import org.ftn.vecDSL.vecDSL.VectorCollection

import static org.ftn.vecDSL.generator.chroma.ChromaSchema.kw
import static org.ftn.vecDSL.generator.code.Code.*

/**
 * Builds a Chroma filter: the metadata part 'where' ({"$and": [...]}, {"price": {"$lte": 60}}) and the document part
 * 'where_document' ({"$contains": "..."}, {"$regex": "..."}), which Chroma applies as two conjuncts. Predicates on the
 * document field go to where_document, all others to where; a disjunction or negation must stay within one part.
 * Negation is pushed to the leaves, where Chroma has exact complements: $eq and $ne, $in and $nin, $contains and
 * $not_contains, $regex and $not_regex (the negated operators also match records without the key).
 */
class ChromaFilter {

	static val ORDERED = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val COMPLEMENTS = #{'$eq' -> '$ne', '$ne' -> '$eq', '$in' -> '$nin', '$nin' -> '$in', '$contains' -> '$not_contains',
		'$not_contains' -> '$contains', '$regex' -> '$not_regex', '$not_regex' -> '$regex'}
	static val META = '.+*?()|[]{}^$\\'

	val VectorCollection collection
	val PayloadField document
	val List<Expr> metadata = newArrayList
	val List<Expr> documents = newArrayList

	new(VectorCollection collection, PayloadField document) {
		this.collection = collection
		this.document = document
	}

	def ChromaFilter where(FilterBlock block) {
		if (block !== null) split(block.condition)
		this
	}

	/** Adds a metadata condition, for example a tenant's slice key. */
	def ChromaFilter where(Entries condition) {
		metadata += condition
		this
	}

	def boolean isEmpty() {
		metadata.empty && documents.empty
	}

	/** The filter argument of a runtime call: {"where": ..., "where_document": ...}, each part only when present. */
	def Entries code() {
		if (empty) return null
		val result = kw()
		if (!metadata.empty) result.put('where', all('$and', metadata))
		if (!documents.empty) result.put('where_document', all('$and', documents))
		result
	}

	/** Top-level conjuncts go to their part; other conditions must stay within one part. */
	def private void split(Condition condition) {
		if (condition instanceof AndCondition) {
			split(condition.left)
			split(condition.right)
		} else if (onDocument(condition)) {
			documents += render(condition, false)
		} else {
			metadata += render(condition, false)
		}
	}

	def private static Expr all(String operator, List<Expr> parts) {
		if (parts.size == 1) parts.head else kw(operator, items(parts))
	}

	/** Whether a condition tests the document; mixing both parts below the top level has no Chroma form. */
	def private boolean onDocument(Condition condition) {
		switch condition {
			AndCondition: same(condition.left, condition.right)
			OrCondition: same(condition.left, condition.right)
			NotCondition: onDocument(condition.operand)
			Predicate: document !== null && condition.target.rawPath === null && condition.target.field === document
			default: false
		}
	}

	def private boolean same(Condition left, Condition right) {
		val first = onDocument(left)
		if (first != onDocument(right)) {
			throw Unsupported.of('a disjunction or negation that mixes the document and metadata; Chroma applies where and where_document as separate conjuncts')
		}
		first
	}

	/** A condition in Chroma's form; negated pushes a negation to the leaves (De Morgan). */
	def private Expr render(Condition condition, boolean negated) {
		switch condition {
			AndCondition: kw(if (negated) '$or' else '$and', items(#[render(condition.left, negated), render(condition.right, negated)]))
			OrCondition: kw(if (negated) '$and' else '$or', items(#[render(condition.left, negated), render(condition.right, negated)]))
			NotCondition: render(condition.operand, !negated)
			Predicate: if (onDocument(condition)) documentPredicate(condition, negated) else predicate(condition, negated)
			GeoPredicate: throw Unsupported.of('geo predicates; Chroma metadata has no geo values')
			ElementPredicate: throw Unsupported.of("element predicates ('any'); Chroma metadata is flat")
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	def private String key(FieldPath path) {
		if (path.rawPath !== null || !path.subFields.empty) throw Unsupported.of('nested field paths; Chroma metadata is flat')
		ModelUtil.fieldName(path.field)
	}

	def private EScalarDType scalarType(FieldPath path) {
		val field = path.field
		if (field instanceof PayloadField) field.type else null
	}

	def private boolean isArray(FieldPath path) {
		scalarType(path) == EScalarDType.ARRAY
	}

	/** A leaf test {"key": {"$op": value}}, or its complement when negated. */
	def private Entries leaf(FieldPath target, String operator, Expr value, boolean negated) {
		kw(key(target), kw(if (negated) COMPLEMENTS.get(operator) else operator, value))
	}

	/** An ordered comparison has no Chroma complement (it excludes records without the key), except on the primary key. */
	def private Entries ordered(FieldPath target, String operator, String complement, Literal literal, boolean negated) {
		val type = if (isArray(target)) (target.field as PayloadField).elementType else scalarType(target)
		if (type !== null && !ORDERED.contains(type)) {
			throw Unsupported.of('ordered comparisons on ' + type.literal + ' field ' + key(target) + '; Chroma compares only numbers')
		}
		if (negated && target.field !== collection.primaryKey) {
			throw Unsupported.of('a negated range on ' + key(target) + "; Chroma's ranges exclude records without the key, so the negation has no exact form")
		}
		kw(key(target), kw(if (negated) complement else operator, operand(literal)))
	}

	def private Entries predicate(Predicate predicate, boolean negated) {
		val target = predicate.target
		if (predicate.text !== null) throw Unsupported.of('text operators on metadata field ' + key(target) + '; Chroma matches text in the document only')
		if (predicate.lower !== null) {
			val parts = #[ordered(target, '$gte', '$lt', predicate.lower, negated), ordered(target, '$lte', '$gt', predicate.upper, negated)]
			return kw(if (negated) '$or' else '$and', items(parts))
		}
		if (ModelUtil.written(predicate, 'lengthOp')) throw Unsupported.of('length tests; Chroma metadata filters have no length operator')
		if (predicate.value === null) {
			throw Unsupported.of(predicate.unaryOp.literal + ' tests; Chroma metadata filters have no null or existence operator')
		}
		comparison(target, predicate.op, predicate.value, negated)
	}

	def private Entries comparison(FieldPath target, EComparisonOp op, Literal literal, boolean negated) {
		if (literal instanceof NullLit) throw Unsupported.of('comparisons with null; Chroma metadata filters have no null operator')
		switch op {
			case EQ: leaf(target, '$eq', operand(literal), negated)
			case NEQ: leaf(target, '$ne', operand(literal), negated)
			case GT: ordered(target, '$gt', '$lte', literal, negated)
			case GTE: ordered(target, '$gte', '$lt', literal, negated)
			case LT: ordered(target, '$lt', '$gte', literal, negated)
			case LTE: ordered(target, '$lte', '$gt', literal, negated)
			case IN: leaf(target, '$in', list(literal), negated)
			case NOT_IN: leaf(target, '$nin', list(literal), negated)
			case CONTAINS: {
				if (!isArray(target)) throw Unsupported.of('contains on ' + key(target) + ', which is not an array; Chroma matches substrings in the document only')
				leaf(target, '$contains', operand(literal), negated)
			}
			case CONTAINS_ANY: each(target, literal, if (negated) '$and' else '$or', negated)
			case CONTAINS_ALL: each(target, literal, if (negated) '$or' else '$and', negated)
			case LIKE,
			case PREFIX: throw Unsupported.of(op.literal + ' on metadata field ' + key(target) + '; Chroma matches patterns in the document only')
		}
	}

	/** contains_any and contains_all: one $contains per listed item. */
	def private Entries each(FieldPath target, Literal literal, String operator, boolean negated) {
		if (!isArray(target)) throw Unsupported.of('contains_any and contains_all on ' + key(target) + ', which is not an array')
		if (!(literal instanceof ListLit)) throw Unsupported.of('contains_any and contains_all with a value that is not a list literal')
		val List<Expr> parts = newArrayList
		for (item : (literal as ListLit).items) parts += leaf(target, '$contains', operand(item), negated)
		if (parts.size == 1) parts.head as Entries else kw(operator, items(parts))
	}

	/** Document predicates: substrings with $contains, everything else as a regular expression (Rust regex syntax). */
	def private Entries documentPredicate(Predicate predicate, boolean negated) {
		if (predicate.text !== null) return textPredicate(predicate, negated)
		if (predicate.value === null || predicate.lower !== null) {
			throw Unsupported.of('this test on the document; Chroma tests documents by substring and regular expression')
		}
		switch predicate.op {
			case EQ: regex('^' + escape(pattern(predicate.value)) + '$', negated)
			case NEQ: regex('^' + escape(pattern(predicate.value)) + '$', !negated)
			case CONTAINS: kw(if (negated) '$not_contains' else '$contains', text(pattern(predicate.value)))
			case PREFIX: regex('^' + escape(pattern(predicate.value)), negated)
			case LIKE: regex('^' + like(pattern(predicate.value)) + '$', negated)
			default: throw Unsupported.of(predicate.op.literal + ' on the document; Chroma tests documents by substring and regular expression')
		}
	}

	def private Entries textPredicate(Predicate predicate, boolean negated) {
		val value = pattern(predicate.text)
		val terms = value.trim.split('\\s+').filter[!empty].map['\\b' + escape(it) + '\\b'].toList
		switch predicate.textOp {
			case CONTAINS_TEXT: kw(if (negated) '$not_contains' else '$contains', text(value))
			case REGEX: regex(value, negated)
			case PHRASE: regex('(?i)' + String.join('\\s+', terms), negated)
			case MATCH,
			case MATCH_ANY_TERM: anyOf(terms, if (negated) '$and' else '$or', negated)
			case MATCH_ALL_TERMS: anyOf(terms, if (negated) '$or' else '$and', negated)
			default: throw Unsupported.of('text operator ' + predicate.textOp.literal + '; Chroma matches substrings and regular expressions')
		}
	}

	def private Entries anyOf(List<String> terms, String operator, boolean negated) {
		if (terms.empty) throw Unsupported.of('a text match without terms')
		val List<Expr> parts = newArrayList
		for (term : terms) parts += regex('(?i)' + term, negated)
		if (parts.size == 1) parts.head as Entries else kw(operator, items(parts))
	}

	def private Entries regex(String expression, boolean negated) {
		kw(if (negated) '$not_regex' else '$regex', text('(?s)' + expression))
	}

	/** The text of a document pattern; Chroma's regular expressions are built when the program is generated. */
	def private String pattern(Literal literal) {
		if (!(literal instanceof StringLit)) throw Unsupported.of('a document test with a value that is not a string literal; the generator builds the Chroma pattern from the literal')
		(literal as StringLit).value
	}

	/** A like pattern as a regular expression: % is any text, _ one character. */
	def private static String like(String source) {
		val out = new StringBuilder
		for (c : source.toCharArray) {
			if (c == '%'.charAt(0)) out.append('.*') else if (c == '_'.charAt(0)) out.append('.') else out.append(escape(String.valueOf(c)))
		}
		out.toString
	}

	def private static String escape(String value) {
		val out = new StringBuilder
		for (c : value.toCharArray) {
			if (META.indexOf(c) >= 0) out.append('\\')
			out.append(c)
		}
		out.toString
	}

	def private Expr list(Literal literal) {
		if (!(literal instanceof ListLit || literal instanceof ParamRef)) throw Unsupported.of('in and not_in with a value that is not a list')
		operand(literal)
	}

	def private Expr operand(Literal literal) {
		if (!(literal instanceof StringLit || literal instanceof NumberLit || literal instanceof ListLit || literal instanceof ParamRef
			|| literal instanceof BoolLit)) {
			throw Unsupported.of(literal.eClass.name + ' in a filter')
		}
		Literals.of(literal)
	}
}
