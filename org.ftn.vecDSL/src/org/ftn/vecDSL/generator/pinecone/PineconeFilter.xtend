package org.ftn.vecDSL.generator.pinecone

import java.util.List
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Items
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AndCondition
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
import org.ftn.vecDSL.vecDSL.OrCondition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.Predicate
import org.ftn.vecDSL.vecDSL.VectorCollection

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.pinecone.PineconeSchema.kw

/**
 * Builds a Pinecone metadata filter, which every SDK and the REST API take as the same JSON: {"$and": [...]},
 * {"$or": [...]} and field clauses {"price": {"$lte": 60}}. Pinecone negates per field ({"f": {"$not": {...}}}),
 * so 'not' is pushed down to the field clauses. Full-text fields take only the text-match operators.
 */
class PineconeFilter {

	static val NUMERIC = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]

	val VectorCollection collection
	val PineconeSchema schema
	val List<Expr> conjuncts = newArrayList

	new(VectorCollection collection, PineconeSchema schema) {
		this.collection = collection
		this.schema = schema
	}

	def PineconeFilter where(FilterBlock block) {
		if (block !== null) conjuncts += render(block.condition)
		this
	}

	def boolean isEmpty() {
		conjuncts.empty
	}

	def Expr code() {
		if (conjuncts.empty) null else if (conjuncts.size == 1) conjuncts.head else kw('$and', items(conjuncts))
	}

	def private Entries render(Condition condition) {
		switch condition {
			OrCondition: kw('$or', items(#[render(condition.left), render(condition.right)]))
			AndCondition: kw('$and', items(#[render(condition.left), render(condition.right)]))
			NotCondition: negate(render(condition.operand))
			Predicate: predicate(condition)
			GeoPredicate: throw Unsupported.of('geo predicates; Pinecone metadata has no geo type')
			ElementPredicate: throw Unsupported.of("element predicates ('any')")
			default: throw Unsupported.of(condition.eClass.name)
		}
	}

	/** De Morgan for $and and $or; a field clause negates under $not, and a double negation cancels. */
	def private Entries negate(Entries clause) {
		if (clause.has('$and')) return kw('$or', items((clause.get('$and') as Items).items().map[negate(it as Entries)]))
		if (clause.has('$or')) return kw('$and', items((clause.get('$or') as Items).items().map[negate(it as Entries)]))
		val result = entries
		for (field : clause.entries().entrySet) {
			val condition = field.value as Entries
			result.put(field.key, if (condition.has('$not')) condition.get('$not') else kw('$not', condition))
		}
		result
	}

	def private String key(FieldPath path) {
		if (!path.subFields.empty) throw Unsupported.of('nested paths; Pinecone metadata is flat')
		if (path.rawPath !== null) {
			if (path.rawPath.contains('.') || path.rawPath.startsWith('_') || path.rawPath.startsWith('$')) {
				throw Unsupported.of("path '" + path.rawPath + "'; Pinecone metadata keys are flat names")
			}
			return path.rawPath
		}
		PineconeSchema.fieldKey(path.field)
	}

	def private boolean isSearched(FieldPath path) {
		path.rawPath === null && schema.isFullText(collection, path.field)
	}

	def private EScalarDType scalarType(FieldPath path) {
		if (path.rawPath !== null || !(path.field instanceof PayloadField)) return null
		(path.field as PayloadField).type
	}

	def private Entries clause(FieldPath target, String op, Expr value) {
		kw(key(target), kw(op, value))
	}

	def private Entries predicate(Predicate predicate) {
		val target = predicate.target
		if (predicate.text !== null) return textPredicate(target, predicate)
		if (isSearched(target)) {
			throw Unsupported.of('comparisons on full-text field ' + key(target) + '; Pinecone filters full-text fields by text match only')
		}
		if (predicate.value !== null) return comparison(target, predicate.op, predicate.value)
		if (predicate.lower !== null) {
			ordered(target, 'between')
			return kw('$and', items(#[clause(target, '$gte', operand(predicate.lower)), clause(target, '$lte', operand(predicate.upper))]))
		}
		if (ModelUtil.written(predicate, 'lengthOp')) throw Unsupported.of('length predicates; Pinecone filters have no length operator')
		switch predicate.unaryOp {
			case IS_NULL,
			case NOT_EXISTS: clause(target, '$exists', flag(false))
			case IS_NOT_NULL,
			case EXISTS: clause(target, '$exists', flag(true))
			default: throw Unsupported.of(predicate.unaryOp.literal + '; Pinecone filters have no emptiness test')
		}
	}

	def private void ordered(FieldPath target, String operator) {
		val type = scalarType(target)
		if (type !== null && !NUMERIC.contains(type)) {
			throw Unsupported.of(operator + ' on ' + type.literal + ' field ' + key(target) + '; Pinecone orders numbers only')
		}
	}

	def private Entries comparison(FieldPath target, EComparisonOp op, Literal literal) {
		if (literal instanceof NullLit) {
			if (op == EComparisonOp.EQ) return clause(target, '$exists', flag(false))
			if (op == EComparisonOp.NEQ) return clause(target, '$exists', flag(true))
		}
		val array = scalarType(target) == EScalarDType.ARRAY
		switch op {
			case EQ: clause(target, '$eq', operand(literal))
			case NEQ: clause(target, '$ne', operand(literal))
			case GT: {
				ordered(target, op.literal)
				clause(target, '$gt', operand(literal))
			}
			case GTE: {
				ordered(target, op.literal)
				clause(target, '$gte', operand(literal))
			}
			case LT: {
				ordered(target, op.literal)
				clause(target, '$lt', operand(literal))
			}
			case LTE: {
				ordered(target, op.literal)
				clause(target, '$lte', operand(literal))
			}
			case IN: clause(target, '$in', operand(literal))
			case NOT_IN: clause(target, '$nin', operand(literal))
			case CONTAINS: {
				if (!array) throw Unsupported.of('contains on ' + key(target) + ', which is not an array; Pinecone filters have no substring operator')
				clause(target, '$eq', operand(literal))
			}
			case CONTAINS_ANY: {
				if (!array) throw Unsupported.of('contains_any on ' + key(target) + ', which is not an array')
				clause(target, '$in', operand(literal))
			}
			case CONTAINS_ALL: {
				if (!array) throw Unsupported.of('contains_all on ' + key(target) + ', which is not an array')
				if (!(literal instanceof ListLit)) throw Unsupported.of('contains_all with a value that is not a list literal')
				kw('$and', items((literal as ListLit).items.map[clause(target, '$eq', operand(it))]))
			}
			default: throw Unsupported.of('operator ' + op.literal + '; Pinecone filters have no pattern operators')
		}
	}

	def private Entries textPredicate(FieldPath target, Predicate predicate) {
		if (!isSearched(target)) {
			throw Unsupported.of('text operator ' + predicate.textOp.literal + ' on ' + key(target) + ', which is not a full-text field')
		}
		switch predicate.textOp {
			case MATCH,
			case MATCH_ANY_TERM,
			case CONTAINS_TEXT: clause(target, '$match_any', operand(predicate.text))
			case MATCH_ALL_TERMS: clause(target, '$match_all', operand(predicate.text))
			case PHRASE: clause(target, '$match_phrase', operand(predicate.text))
			default: throw Unsupported.of('text operator ' + predicate.textOp.literal)
		}
	}

	def private Expr operand(Literal literal) {
		if (literal instanceof NullLit) throw Unsupported.of('null literals in comparisons other than eq and neq')
		Literals.of(literal)
	}
}
