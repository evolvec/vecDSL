package org.ftn.vecDSL.generator.pinecone;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EComparisonOp;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETextOp;
import org.ftn.vecDSL.vecDSL.EUnaryOp;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Builds a Pinecone metadata filter, which every SDK and the REST API take as the same JSON: {"$and": [...]},
 * {"$or": [...]} and field clauses {"price": {"$lte": 60}}. Pinecone negates per field ({"f": {"$not": {...}}}),
 * so 'not' is pushed down to the field clauses. Full-text fields take only the text-match operators.
 */
@SuppressWarnings("all")
public class PineconeFilter {
  private static final List<EScalarDType> NUMERIC = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private final VectorCollection collection;

  private final PineconeSchema schema;

  private final List<Code.Expr> conjuncts = CollectionLiterals.<Code.Expr>newArrayList();

  public PineconeFilter(final VectorCollection collection, final PineconeSchema schema) {
    this.collection = collection;
    this.schema = schema;
  }

  public PineconeFilter where(final FilterBlock block) {
    PineconeFilter _xblockexpression = null;
    {
      if ((block != null)) {
        Code.Entries _render = this.render(block.getCondition());
        this.conjuncts.add(_render);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public boolean isEmpty() {
    return this.conjuncts.isEmpty();
  }

  public Code.Expr code() {
    Code.Expr _xifexpression = null;
    boolean _isEmpty = this.conjuncts.isEmpty();
    if (_isEmpty) {
      _xifexpression = null;
    } else {
      Code.Expr _xifexpression_1 = null;
      int _size = this.conjuncts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        _xifexpression_1 = IterableExtensions.<Code.Expr>head(this.conjuncts);
      } else {
        _xifexpression_1 = PineconeSchema.kw("$and", Code.items(this.conjuncts));
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  private Code.Entries render(final Condition condition) {
    Code.Entries _switchResult = null;
    boolean _matched = false;
    if (condition instanceof OrCondition) {
      _matched=true;
      Code.Entries _render = this.render(((OrCondition)condition).getLeft());
      Code.Entries _render_1 = this.render(((OrCondition)condition).getRight());
      _switchResult = PineconeSchema.kw("$or", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
    }
    if (!_matched) {
      if (condition instanceof AndCondition) {
        _matched=true;
        Code.Entries _render = this.render(((AndCondition)condition).getLeft());
        Code.Entries _render_1 = this.render(((AndCondition)condition).getRight());
        _switchResult = PineconeSchema.kw("$and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        _switchResult = this.negate(this.render(((NotCondition)condition).getOperand()));
      }
    }
    if (!_matched) {
      if (condition instanceof Predicate) {
        _matched=true;
        _switchResult = this.predicate(((Predicate)condition));
      }
    }
    if (!_matched) {
      if (condition instanceof GeoPredicate) {
        _matched=true;
        throw Unsupported.of("geo predicates; Pinecone metadata has no geo type");
      }
    }
    if (!_matched) {
      if (condition instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of("element predicates (\'any\')");
      }
    }
    if (!_matched) {
      throw Unsupported.of(condition.eClass().getName());
    }
    return _switchResult;
  }

  /**
   * De Morgan for $and and $or; a field clause negates under $not, and a double negation cancels.
   */
  private Code.Entries negate(final Code.Entries clause) {
    Code.Entries _xblockexpression = null;
    {
      boolean _has = clause.has("$and");
      if (_has) {
        Code.Expr _get = clause.get("$and");
        final Function1<Code.Expr, Code.Entries> _function = (Code.Expr it) -> {
          return this.negate(((Code.Entries) it));
        };
        return PineconeSchema.kw("$or", Code.items(ListExtensions.<Code.Expr, Code.Entries>map(((Code.Items) _get).items(), _function)));
      }
      boolean _has_1 = clause.has("$or");
      if (_has_1) {
        Code.Expr _get_1 = clause.get("$or");
        final Function1<Code.Expr, Code.Entries> _function_1 = (Code.Expr it) -> {
          return this.negate(((Code.Entries) it));
        };
        return PineconeSchema.kw("$and", Code.items(ListExtensions.<Code.Expr, Code.Entries>map(((Code.Items) _get_1).items(), _function_1)));
      }
      final Code.Entries result = Code.entries();
      Set<Map.Entry<String, Code.Expr>> _entrySet = clause.entries().entrySet();
      for (final Map.Entry<String, Code.Expr> field : _entrySet) {
        {
          Code.Expr _value = field.getValue();
          final Code.Entries condition = ((Code.Entries) _value);
          String _key = field.getKey();
          Code.Expr _xifexpression = null;
          boolean _has_2 = condition.has("$not");
          if (_has_2) {
            _xifexpression = condition.get("$not");
          } else {
            _xifexpression = PineconeSchema.kw("$not", condition);
          }
          result.put(_key, _xifexpression);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String key(final FieldPath path) {
    String _xblockexpression = null;
    {
      boolean _isEmpty = path.getSubFields().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("nested paths; Pinecone metadata is flat");
      }
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        if (((path.getRawPath().contains(".") || path.getRawPath().startsWith("_")) || path.getRawPath().startsWith("$"))) {
          String _rawPath_1 = path.getRawPath();
          String _plus = ("path \'" + _rawPath_1);
          String _plus_1 = (_plus + "\'; Pinecone metadata keys are flat names");
          throw Unsupported.of(_plus_1);
        }
        return path.getRawPath();
      }
      _xblockexpression = PineconeSchema.fieldKey(path.getField());
    }
    return _xblockexpression;
  }

  private boolean isSearched(final FieldPath path) {
    return ((path.getRawPath() == null) && this.schema.isFullText(this.collection, path.getField()));
  }

  private EScalarDType scalarType(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      if (((path.getRawPath() != null) || (!(path.getField() instanceof PayloadField)))) {
        return null;
      }
      Field _field = path.getField();
      _xblockexpression = ((PayloadField) _field).getType();
    }
    return _xblockexpression;
  }

  private Code.Entries clause(final FieldPath target, final String op, final Code.Expr value) {
    return PineconeSchema.kw(this.key(target), PineconeSchema.kw(op, value));
  }

  private Code.Entries predicate(final Predicate predicate) {
    Code.Entries _xblockexpression = null;
    {
      final FieldPath target = predicate.getTarget();
      Literal _text = predicate.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        return this.textPredicate(target, predicate);
      }
      boolean _isSearched = this.isSearched(target);
      if (_isSearched) {
        String _key = this.key(target);
        String _plus = ("comparisons on full-text field " + _key);
        String _plus_1 = (_plus + "; Pinecone filters full-text fields by text match only");
        throw Unsupported.of(_plus_1);
      }
      Literal _value = predicate.getValue();
      boolean _tripleNotEquals_1 = (_value != null);
      if (_tripleNotEquals_1) {
        return this.comparison(target, predicate.getOp(), predicate.getValue());
      }
      Literal _lower = predicate.getLower();
      boolean _tripleNotEquals_2 = (_lower != null);
      if (_tripleNotEquals_2) {
        this.ordered(target, "between");
        Code.Entries _clause = this.clause(target, "$gte", this.operand(predicate.getLower()));
        Code.Entries _clause_1 = this.clause(target, "$lte", this.operand(predicate.getUpper()));
        return PineconeSchema.kw("$and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_clause, _clause_1))));
      }
      boolean _written = ModelUtil.written(predicate, "lengthOp");
      if (_written) {
        throw Unsupported.of("length predicates; Pinecone filters have no length operator");
      }
      Code.Entries _switchResult = null;
      EUnaryOp _unaryOp = predicate.getUnaryOp();
      if (_unaryOp != null) {
        switch (_unaryOp) {
          case IS_NULL:
          case NOT_EXISTS:
            _switchResult = this.clause(target, "$exists", Code.flag(false));
            break;
          case IS_NOT_NULL:
          case EXISTS:
            _switchResult = this.clause(target, "$exists", Code.flag(true));
            break;
          default:
            String _literal = predicate.getUnaryOp().getLiteral();
            String _plus_2 = (_literal + "; Pinecone filters have no emptiness test");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal = predicate.getUnaryOp().getLiteral();
        String _plus_2 = (_literal + "; Pinecone filters have no emptiness test");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private void ordered(final FieldPath target, final String operator) {
    final EScalarDType type = this.scalarType(target);
    if (((type != null) && (!PineconeFilter.NUMERIC.contains(type)))) {
      String _literal = type.getLiteral();
      String _plus = ((operator + " on ") + _literal);
      String _plus_1 = (_plus + " field ");
      String _key = this.key(target);
      String _plus_2 = (_plus_1 + _key);
      String _plus_3 = (_plus_2 + "; Pinecone orders numbers only");
      throw Unsupported.of(_plus_3);
    }
  }

  private Code.Entries comparison(final FieldPath target, final EComparisonOp op, final Literal literal) {
    Code.Entries _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        boolean _equals = Objects.equals(op, EComparisonOp.EQ);
        if (_equals) {
          return this.clause(target, "$exists", Code.flag(false));
        }
        boolean _equals_1 = Objects.equals(op, EComparisonOp.NEQ);
        if (_equals_1) {
          return this.clause(target, "$exists", Code.flag(true));
        }
      }
      EScalarDType _scalarType = this.scalarType(target);
      final boolean array = Objects.equals(_scalarType, EScalarDType.ARRAY);
      Code.Entries _switchResult = null;
      if (op != null) {
        switch (op) {
          case EQ:
            _switchResult = this.clause(target, "$eq", this.operand(literal));
            break;
          case NEQ:
            _switchResult = this.clause(target, "$ne", this.operand(literal));
            break;
          case GT:
            Code.Entries _xblockexpression_1 = null;
            {
              this.ordered(target, op.getLiteral());
              _xblockexpression_1 = this.clause(target, "$gt", this.operand(literal));
            }
            _switchResult = _xblockexpression_1;
            break;
          case GTE:
            Code.Entries _xblockexpression_2 = null;
            {
              this.ordered(target, op.getLiteral());
              _xblockexpression_2 = this.clause(target, "$gte", this.operand(literal));
            }
            _switchResult = _xblockexpression_2;
            break;
          case LT:
            Code.Entries _xblockexpression_3 = null;
            {
              this.ordered(target, op.getLiteral());
              _xblockexpression_3 = this.clause(target, "$lt", this.operand(literal));
            }
            _switchResult = _xblockexpression_3;
            break;
          case LTE:
            Code.Entries _xblockexpression_4 = null;
            {
              this.ordered(target, op.getLiteral());
              _xblockexpression_4 = this.clause(target, "$lte", this.operand(literal));
            }
            _switchResult = _xblockexpression_4;
            break;
          case IN:
            _switchResult = this.clause(target, "$in", this.operand(literal));
            break;
          case NOT_IN:
            _switchResult = this.clause(target, "$nin", this.operand(literal));
            break;
          case CONTAINS:
            Code.Entries _xblockexpression_5 = null;
            {
              if ((!array)) {
                String _key = this.key(target);
                String _plus = ("contains on " + _key);
                String _plus_1 = (_plus + ", which is not an array; Pinecone filters have no substring operator");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_5 = this.clause(target, "$eq", this.operand(literal));
            }
            _switchResult = _xblockexpression_5;
            break;
          case CONTAINS_ANY:
            Code.Entries _xblockexpression_6 = null;
            {
              if ((!array)) {
                String _key = this.key(target);
                String _plus = ("contains_any on " + _key);
                String _plus_1 = (_plus + ", which is not an array");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_6 = this.clause(target, "$in", this.operand(literal));
            }
            _switchResult = _xblockexpression_6;
            break;
          case CONTAINS_ALL:
            Code.Entries _xblockexpression_7 = null;
            {
              if ((!array)) {
                String _key = this.key(target);
                String _plus = ("contains_all on " + _key);
                String _plus_1 = (_plus + ", which is not an array");
                throw Unsupported.of(_plus_1);
              }
              if ((!(literal instanceof ListLit))) {
                throw Unsupported.of("contains_all with a value that is not a list literal");
              }
              final Function1<Literal, Code.Entries> _function = (Literal it) -> {
                return this.clause(target, "$eq", this.operand(it));
              };
              _xblockexpression_7 = PineconeSchema.kw("$and", Code.items(ListExtensions.<Literal, Code.Entries>map(((ListLit) literal).getItems(), _function)));
            }
            _switchResult = _xblockexpression_7;
            break;
          default:
            String _literal = op.getLiteral();
            String _plus = ("operator " + _literal);
            String _plus_1 = (_plus + "; Pinecone filters have no pattern operators");
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = op.getLiteral();
        String _plus = ("operator " + _literal);
        String _plus_1 = (_plus + "; Pinecone filters have no pattern operators");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries textPredicate(final FieldPath target, final Predicate predicate) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isSearched = this.isSearched(target);
      boolean _not = (!_isSearched);
      if (_not) {
        String _literal = predicate.getTextOp().getLiteral();
        String _plus = ("text operator " + _literal);
        String _plus_1 = (_plus + " on ");
        String _key = this.key(target);
        String _plus_2 = (_plus_1 + _key);
        String _plus_3 = (_plus_2 + ", which is not a full-text field");
        throw Unsupported.of(_plus_3);
      }
      Code.Entries _switchResult = null;
      ETextOp _textOp = predicate.getTextOp();
      if (_textOp != null) {
        switch (_textOp) {
          case MATCH:
          case MATCH_ANY_TERM:
          case CONTAINS_TEXT:
            _switchResult = this.clause(target, "$match_any", this.operand(predicate.getText()));
            break;
          case MATCH_ALL_TERMS:
            _switchResult = this.clause(target, "$match_all", this.operand(predicate.getText()));
            break;
          case PHRASE:
            _switchResult = this.clause(target, "$match_phrase", this.operand(predicate.getText()));
            break;
          default:
            String _literal_1 = predicate.getTextOp().getLiteral();
            String _plus_4 = ("text operator " + _literal_1);
            throw Unsupported.of(_plus_4);
        }
      } else {
        String _literal_1 = predicate.getTextOp().getLiteral();
        String _plus_4 = ("text operator " + _literal_1);
        throw Unsupported.of(_plus_4);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Expr operand(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        throw Unsupported.of("null literals in comparisons other than eq and neq");
      }
      _xblockexpression = Literals.of(literal);
    }
    return _xblockexpression;
  }
}
