package org.ftn.vecDSL.generator.chroma;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EComparisonOp;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETextOp;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Builds a Chroma filter: the metadata part 'where' ({"$and": [...]}, {"price": {"$lte": 60}}) and the document part
 * 'where_document' ({"$contains": "..."}, {"$regex": "..."}), which Chroma applies as two conjuncts. Predicates on the
 * document field go to where_document, all others to where; a disjunction or negation must stay within one part.
 * Negation is pushed to the leaves, where Chroma has exact complements: $eq and $ne, $in and $nin, $contains and
 * $not_contains, $regex and $not_regex (the negated operators also match records without the key).
 */
@SuppressWarnings("all")
public class ChromaFilter {
  private static final List<EScalarDType> ORDERED = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final Map<String, String> COMPLEMENTS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("$eq", "$ne"), Pair.<String, String>of("$ne", "$eq"), Pair.<String, String>of("$in", "$nin"), Pair.<String, String>of("$nin", "$in"), Pair.<String, String>of("$contains", "$not_contains"), Pair.<String, String>of("$not_contains", "$contains"), Pair.<String, String>of("$regex", "$not_regex"), Pair.<String, String>of("$not_regex", "$regex")));

  private static final String META = ".+*?()|[]{}^$\\";

  private final VectorCollection collection;

  private final PayloadField document;

  private final List<Code.Expr> metadata = CollectionLiterals.<Code.Expr>newArrayList();

  private final List<Code.Expr> documents = CollectionLiterals.<Code.Expr>newArrayList();

  public ChromaFilter(final VectorCollection collection, final PayloadField document) {
    this.collection = collection;
    this.document = document;
  }

  public ChromaFilter where(final FilterBlock block) {
    ChromaFilter _xblockexpression = null;
    {
      if ((block != null)) {
        this.split(block.getCondition());
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * Adds a metadata condition, for example a tenant's slice key.
   */
  public ChromaFilter where(final Code.Entries condition) {
    ChromaFilter _xblockexpression = null;
    {
      this.metadata.add(condition);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public boolean isEmpty() {
    return (this.metadata.isEmpty() && this.documents.isEmpty());
  }

  /**
   * The filter argument of a runtime call: {"where": ..., "where_document": ...}, each part only when present.
   */
  public Code.Entries code() {
    Code.Entries _xblockexpression = null;
    {
      boolean _isEmpty = this.isEmpty();
      if (_isEmpty) {
        return null;
      }
      final Code.Entries result = ChromaSchema.kw();
      boolean _isEmpty_1 = this.metadata.isEmpty();
      boolean _not = (!_isEmpty_1);
      if (_not) {
        result.put("where", ChromaFilter.all("$and", this.metadata));
      }
      boolean _isEmpty_2 = this.documents.isEmpty();
      boolean _not_1 = (!_isEmpty_2);
      if (_not_1) {
        result.put("where_document", ChromaFilter.all("$and", this.documents));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Top-level conjuncts go to their part; other conditions must stay within one part.
   */
  private void split(final Condition condition) {
    if ((condition instanceof AndCondition)) {
      this.split(((AndCondition)condition).getLeft());
      this.split(((AndCondition)condition).getRight());
    } else {
      boolean _onDocument = this.onDocument(condition);
      if (_onDocument) {
        Code.Expr _render = this.render(condition, false);
        this.documents.add(_render);
      } else {
        Code.Expr _render_1 = this.render(condition, false);
        this.metadata.add(_render_1);
      }
    }
  }

  private static Code.Expr all(final String operator, final List<Code.Expr> parts) {
    Code.Expr _xifexpression = null;
    int _size = parts.size();
    boolean _equals = (_size == 1);
    if (_equals) {
      _xifexpression = IterableExtensions.<Code.Expr>head(parts);
    } else {
      _xifexpression = ChromaSchema.kw(operator, Code.items(parts));
    }
    return _xifexpression;
  }

  /**
   * Whether a condition tests the document; mixing both parts below the top level has no Chroma form.
   */
  private boolean onDocument(final Condition condition) {
    boolean _switchResult = false;
    boolean _matched = false;
    if (condition instanceof AndCondition) {
      _matched=true;
      _switchResult = this.same(((AndCondition)condition).getLeft(), ((AndCondition)condition).getRight());
    }
    if (!_matched) {
      if (condition instanceof OrCondition) {
        _matched=true;
        _switchResult = this.same(((OrCondition)condition).getLeft(), ((OrCondition)condition).getRight());
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        _switchResult = this.onDocument(((NotCondition)condition).getOperand());
      }
    }
    if (!_matched) {
      if (condition instanceof Predicate) {
        _matched=true;
        _switchResult = (((this.document != null) && (((Predicate)condition).getTarget().getRawPath() == null)) && (((Predicate)condition).getTarget().getField() == this.document));
      }
    }
    if (!_matched) {
      _switchResult = false;
    }
    return _switchResult;
  }

  private boolean same(final Condition left, final Condition right) {
    boolean _xblockexpression = false;
    {
      final boolean first = this.onDocument(left);
      boolean _onDocument = this.onDocument(right);
      boolean _notEquals = (first != _onDocument);
      if (_notEquals) {
        throw Unsupported.of("a disjunction or negation that mixes the document and metadata; Chroma applies where and where_document as separate conjuncts");
      }
      _xblockexpression = first;
    }
    return _xblockexpression;
  }

  /**
   * A condition in Chroma's form; negated pushes a negation to the leaves (De Morgan).
   */
  private Code.Expr render(final Condition condition, final boolean negated) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (condition instanceof AndCondition) {
      _matched=true;
      String _xifexpression = null;
      if (negated) {
        _xifexpression = "$or";
      } else {
        _xifexpression = "$and";
      }
      Code.Expr _render = this.render(((AndCondition)condition).getLeft(), negated);
      Code.Expr _render_1 = this.render(((AndCondition)condition).getRight(), negated);
      _switchResult = ChromaSchema.kw(_xifexpression, Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
    }
    if (!_matched) {
      if (condition instanceof OrCondition) {
        _matched=true;
        String _xifexpression = null;
        if (negated) {
          _xifexpression = "$and";
        } else {
          _xifexpression = "$or";
        }
        Code.Expr _render = this.render(((OrCondition)condition).getLeft(), negated);
        Code.Expr _render_1 = this.render(((OrCondition)condition).getRight(), negated);
        _switchResult = ChromaSchema.kw(_xifexpression, Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        _switchResult = this.render(((NotCondition)condition).getOperand(), (!negated));
      }
    }
    if (!_matched) {
      if (condition instanceof Predicate) {
        _matched=true;
        Code.Entries _xifexpression = null;
        boolean _onDocument = this.onDocument(condition);
        if (_onDocument) {
          _xifexpression = this.documentPredicate(((Predicate)condition), negated);
        } else {
          _xifexpression = this.predicate(((Predicate)condition), negated);
        }
        _switchResult = _xifexpression;
      }
    }
    if (!_matched) {
      if (condition instanceof GeoPredicate) {
        _matched=true;
        throw Unsupported.of("geo predicates; Chroma metadata has no geo values");
      }
    }
    if (!_matched) {
      if (condition instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of("element predicates (\'any\'); Chroma metadata is flat");
      }
    }
    if (!_matched) {
      throw Unsupported.of(condition.eClass().getName());
    }
    return _switchResult;
  }

  private String key(final FieldPath path) {
    String _xblockexpression = null;
    {
      if (((path.getRawPath() != null) || (!path.getSubFields().isEmpty()))) {
        throw Unsupported.of("nested field paths; Chroma metadata is flat");
      }
      _xblockexpression = ModelUtil.fieldName(path.getField());
    }
    return _xblockexpression;
  }

  private EScalarDType scalarType(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      final Field field = path.getField();
      EScalarDType _xifexpression = null;
      if ((field instanceof PayloadField)) {
        _xifexpression = ((PayloadField)field).getType();
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private boolean isArray(final FieldPath path) {
    EScalarDType _scalarType = this.scalarType(path);
    return Objects.equals(_scalarType, EScalarDType.ARRAY);
  }

  /**
   * A leaf test {"key": {"$op": value}}, or its complement when negated.
   */
  private Code.Entries leaf(final FieldPath target, final String operator, final Code.Expr value, final boolean negated) {
    String _xifexpression = null;
    if (negated) {
      _xifexpression = ChromaFilter.COMPLEMENTS.get(operator);
    } else {
      _xifexpression = operator;
    }
    return ChromaSchema.kw(this.key(target), ChromaSchema.kw(_xifexpression, value));
  }

  /**
   * An ordered comparison has no Chroma complement (it excludes records without the key), except on the primary key.
   */
  private Code.Entries ordered(final FieldPath target, final String operator, final String complement, final Literal literal, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      EScalarDType _xifexpression = null;
      boolean _isArray = this.isArray(target);
      if (_isArray) {
        Field _field = target.getField();
        _xifexpression = ((PayloadField) _field).getElementType();
      } else {
        _xifexpression = this.scalarType(target);
      }
      final EScalarDType type = _xifexpression;
      if (((type != null) && (!ChromaFilter.ORDERED.contains(type)))) {
        String _literal = type.getLiteral();
        String _plus = ("ordered comparisons on " + _literal);
        String _plus_1 = (_plus + " field ");
        String _key = this.key(target);
        String _plus_2 = (_plus_1 + _key);
        String _plus_3 = (_plus_2 + "; Chroma compares only numbers");
        throw Unsupported.of(_plus_3);
      }
      if ((negated && (target.getField() != this.collection.getPrimaryKey()))) {
        String _key_1 = this.key(target);
        String _plus_4 = ("a negated range on " + _key_1);
        String _plus_5 = (_plus_4 + "; Chroma\'s ranges exclude records without the key, so the negation has no exact form");
        throw Unsupported.of(_plus_5);
      }
      String _xifexpression_1 = null;
      if (negated) {
        _xifexpression_1 = complement;
      } else {
        _xifexpression_1 = operator;
      }
      _xblockexpression = ChromaSchema.kw(this.key(target), ChromaSchema.kw(_xifexpression_1, this.operand(literal)));
    }
    return _xblockexpression;
  }

  private Code.Entries predicate(final Predicate predicate, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      final FieldPath target = predicate.getTarget();
      Literal _text = predicate.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        String _key = this.key(target);
        String _plus = ("text operators on metadata field " + _key);
        String _plus_1 = (_plus + "; Chroma matches text in the document only");
        throw Unsupported.of(_plus_1);
      }
      Literal _lower = predicate.getLower();
      boolean _tripleNotEquals_1 = (_lower != null);
      if (_tripleNotEquals_1) {
        Code.Entries _ordered = this.ordered(target, "$gte", "$lt", predicate.getLower(), negated);
        Code.Entries _ordered_1 = this.ordered(target, "$lte", "$gt", predicate.getUpper(), negated);
        final List<Code.Entries> parts = Collections.<Code.Entries>unmodifiableList(CollectionLiterals.<Code.Entries>newArrayList(_ordered, _ordered_1));
        String _xifexpression = null;
        if (negated) {
          _xifexpression = "$or";
        } else {
          _xifexpression = "$and";
        }
        return ChromaSchema.kw(_xifexpression, Code.items(parts));
      }
      boolean _written = ModelUtil.written(predicate, "lengthOp");
      if (_written) {
        throw Unsupported.of("length tests; Chroma metadata filters have no length operator");
      }
      Literal _value = predicate.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        String _literal = predicate.getUnaryOp().getLiteral();
        String _plus_2 = (_literal + " tests; Chroma metadata filters have no null or existence operator");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = this.comparison(target, predicate.getOp(), predicate.getValue(), negated);
    }
    return _xblockexpression;
  }

  private Code.Entries comparison(final FieldPath target, final EComparisonOp op, final Literal literal, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        throw Unsupported.of("comparisons with null; Chroma metadata filters have no null operator");
      }
      Code.Entries _switchResult = null;
      if (op != null) {
        switch (op) {
          case EQ:
            _switchResult = this.leaf(target, "$eq", this.operand(literal), negated);
            break;
          case NEQ:
            _switchResult = this.leaf(target, "$ne", this.operand(literal), negated);
            break;
          case GT:
            _switchResult = this.ordered(target, "$gt", "$lte", literal, negated);
            break;
          case GTE:
            _switchResult = this.ordered(target, "$gte", "$lt", literal, negated);
            break;
          case LT:
            _switchResult = this.ordered(target, "$lt", "$gte", literal, negated);
            break;
          case LTE:
            _switchResult = this.ordered(target, "$lte", "$gt", literal, negated);
            break;
          case IN:
            _switchResult = this.leaf(target, "$in", this.list(literal), negated);
            break;
          case NOT_IN:
            _switchResult = this.leaf(target, "$nin", this.list(literal), negated);
            break;
          case CONTAINS:
            Code.Entries _xblockexpression_1 = null;
            {
              boolean _isArray = this.isArray(target);
              boolean _not = (!_isArray);
              if (_not) {
                String _key = this.key(target);
                String _plus = ("contains on " + _key);
                String _plus_1 = (_plus + ", which is not an array; Chroma matches substrings in the document only");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = this.leaf(target, "$contains", this.operand(literal), negated);
            }
            _switchResult = _xblockexpression_1;
            break;
          case CONTAINS_ANY:
            String _xifexpression = null;
            if (negated) {
              _xifexpression = "$and";
            } else {
              _xifexpression = "$or";
            }
            _switchResult = this.each(target, literal, _xifexpression, negated);
            break;
          case CONTAINS_ALL:
            String _xifexpression_1 = null;
            if (negated) {
              _xifexpression_1 = "$or";
            } else {
              _xifexpression_1 = "$and";
            }
            _switchResult = this.each(target, literal, _xifexpression_1, negated);
            break;
          case LIKE:
          case PREFIX:
            String _literal = op.getLiteral();
            String _plus = (_literal + " on metadata field ");
            String _key = this.key(target);
            String _plus_1 = (_plus + _key);
            String _plus_2 = (_plus_1 + "; Chroma matches patterns in the document only");
            throw Unsupported.of(_plus_2);
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * contains_any and contains_all: one $contains per listed item.
   */
  private Code.Entries each(final FieldPath target, final Literal literal, final String operator, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isArray = this.isArray(target);
      boolean _not = (!_isArray);
      if (_not) {
        String _key = this.key(target);
        String _plus = ("contains_any and contains_all on " + _key);
        String _plus_1 = (_plus + ", which is not an array");
        throw Unsupported.of(_plus_1);
      }
      if ((!(literal instanceof ListLit))) {
        throw Unsupported.of("contains_any and contains_all with a value that is not a list literal");
      }
      final List<Code.Expr> parts = CollectionLiterals.<Code.Expr>newArrayList();
      EList<Literal> _items = ((ListLit) literal).getItems();
      for (final Literal item : _items) {
        Code.Entries _leaf = this.leaf(target, "$contains", this.operand(item), negated);
        parts.add(_leaf);
      }
      Code.Entries _xifexpression = null;
      int _size = parts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        Code.Expr _head = IterableExtensions.<Code.Expr>head(parts);
        _xifexpression = ((Code.Entries) _head);
      } else {
        _xifexpression = ChromaSchema.kw(operator, Code.items(parts));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * Document predicates: substrings with $contains, everything else as a regular expression (Rust regex syntax).
   */
  private Code.Entries documentPredicate(final Predicate predicate, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      Literal _text = predicate.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        return this.textPredicate(predicate, negated);
      }
      if (((predicate.getValue() == null) || (predicate.getLower() != null))) {
        throw Unsupported.of("this test on the document; Chroma tests documents by substring and regular expression");
      }
      Code.Entries _switchResult = null;
      EComparisonOp _op = predicate.getOp();
      if (_op != null) {
        switch (_op) {
          case EQ:
            String _escape = ChromaFilter.escape(this.pattern(predicate.getValue()));
            String _plus = ("^" + _escape);
            String _plus_1 = (_plus + "$");
            _switchResult = this.regex(_plus_1, negated);
            break;
          case NEQ:
            String _escape_1 = ChromaFilter.escape(this.pattern(predicate.getValue()));
            String _plus_2 = ("^" + _escape_1);
            String _plus_3 = (_plus_2 + "$");
            _switchResult = this.regex(_plus_3, (!negated));
            break;
          case CONTAINS:
            String _xifexpression = null;
            if (negated) {
              _xifexpression = "$not_contains";
            } else {
              _xifexpression = "$contains";
            }
            _switchResult = ChromaSchema.kw(_xifexpression, Code.text(this.pattern(predicate.getValue())));
            break;
          case PREFIX:
            String _escape_2 = ChromaFilter.escape(this.pattern(predicate.getValue()));
            String _plus_4 = ("^" + _escape_2);
            _switchResult = this.regex(_plus_4, negated);
            break;
          case LIKE:
            String _like = ChromaFilter.like(this.pattern(predicate.getValue()));
            String _plus_5 = ("^" + _like);
            String _plus_6 = (_plus_5 + "$");
            _switchResult = this.regex(_plus_6, negated);
            break;
          default:
            String _literal = predicate.getOp().getLiteral();
            String _plus_7 = (_literal + " on the document; Chroma tests documents by substring and regular expression");
            throw Unsupported.of(_plus_7);
        }
      } else {
        String _literal = predicate.getOp().getLiteral();
        String _plus_7 = (_literal + " on the document; Chroma tests documents by substring and regular expression");
        throw Unsupported.of(_plus_7);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries textPredicate(final Predicate predicate, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      final String value = this.pattern(predicate.getText());
      final Function1<String, Boolean> _function = (String it) -> {
        boolean _isEmpty = it.isEmpty();
        return Boolean.valueOf((!_isEmpty));
      };
      final Function1<String, String> _function_1 = (String it) -> {
        String _escape = ChromaFilter.escape(it);
        String _plus = ("\\b" + _escape);
        return (_plus + "\\b");
      };
      final List<String> terms = IterableExtensions.<String>toList(IterableExtensions.<String, String>map(IterableExtensions.<String>filter(((Iterable<String>)Conversions.doWrapArray(value.trim().split("\\s+"))), _function), _function_1));
      Code.Entries _switchResult = null;
      ETextOp _textOp = predicate.getTextOp();
      if (_textOp != null) {
        switch (_textOp) {
          case CONTAINS_TEXT:
            String _xifexpression = null;
            if (negated) {
              _xifexpression = "$not_contains";
            } else {
              _xifexpression = "$contains";
            }
            _switchResult = ChromaSchema.kw(_xifexpression, Code.text(value));
            break;
          case REGEX:
            _switchResult = this.regex(value, negated);
            break;
          case PHRASE:
            String _join = String.join("\\s+", terms);
            String _plus = ("(?i)" + _join);
            _switchResult = this.regex(_plus, negated);
            break;
          case MATCH:
          case MATCH_ANY_TERM:
            String _xifexpression_1 = null;
            if (negated) {
              _xifexpression_1 = "$and";
            } else {
              _xifexpression_1 = "$or";
            }
            _switchResult = this.anyOf(terms, _xifexpression_1, negated);
            break;
          case MATCH_ALL_TERMS:
            String _xifexpression_2 = null;
            if (negated) {
              _xifexpression_2 = "$or";
            } else {
              _xifexpression_2 = "$and";
            }
            _switchResult = this.anyOf(terms, _xifexpression_2, negated);
            break;
          default:
            String _literal = predicate.getTextOp().getLiteral();
            String _plus_1 = ("text operator " + _literal);
            String _plus_2 = (_plus_1 + "; Chroma matches substrings and regular expressions");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal = predicate.getTextOp().getLiteral();
        String _plus_1 = ("text operator " + _literal);
        String _plus_2 = (_plus_1 + "; Chroma matches substrings and regular expressions");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries anyOf(final List<String> terms, final String operator, final boolean negated) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isEmpty = terms.isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("a text match without terms");
      }
      final List<Code.Expr> parts = CollectionLiterals.<Code.Expr>newArrayList();
      for (final String term : terms) {
        Code.Entries _regex = this.regex(("(?i)" + term), negated);
        parts.add(_regex);
      }
      Code.Entries _xifexpression = null;
      int _size = parts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        Code.Expr _head = IterableExtensions.<Code.Expr>head(parts);
        _xifexpression = ((Code.Entries) _head);
      } else {
        _xifexpression = ChromaSchema.kw(operator, Code.items(parts));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private Code.Entries regex(final String expression, final boolean negated) {
    String _xifexpression = null;
    if (negated) {
      _xifexpression = "$not_regex";
    } else {
      _xifexpression = "$regex";
    }
    return ChromaSchema.kw(_xifexpression, Code.text(("(?s)" + expression)));
  }

  /**
   * The text of a document pattern; Chroma's regular expressions are built when the program is generated.
   */
  private String pattern(final Literal literal) {
    String _xblockexpression = null;
    {
      if ((!(literal instanceof StringLit))) {
        throw Unsupported.of("a document test with a value that is not a string literal; the generator builds the Chroma pattern from the literal");
      }
      _xblockexpression = ((StringLit) literal).getValue();
    }
    return _xblockexpression;
  }

  /**
   * A like pattern as a regular expression: % is any text, _ one character.
   */
  private static String like(final String source) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder();
      char[] _charArray = source.toCharArray();
      for (final char c : _charArray) {
        char _charAt = "%".charAt(0);
        boolean _equals = (c == _charAt);
        if (_equals) {
          out.append(".*");
        } else {
          char _charAt_1 = "_".charAt(0);
          boolean _equals_1 = (c == _charAt_1);
          if (_equals_1) {
            out.append(".");
          } else {
            out.append(ChromaFilter.escape(String.valueOf(c)));
          }
        }
      }
      _xblockexpression = out.toString();
    }
    return _xblockexpression;
  }

  private static String escape(final String value) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder();
      char[] _charArray = value.toCharArray();
      for (final char c : _charArray) {
        {
          int _indexOf = ChromaFilter.META.indexOf(c);
          boolean _greaterEqualsThan = (_indexOf >= 0);
          if (_greaterEqualsThan) {
            out.append("\\");
          }
          out.append(c);
        }
      }
      _xblockexpression = out.toString();
    }
    return _xblockexpression;
  }

  private Code.Expr list(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      if ((!((literal instanceof ListLit) || (literal instanceof ParamRef)))) {
        throw Unsupported.of("in and not_in with a value that is not a list");
      }
      _xblockexpression = this.operand(literal);
    }
    return _xblockexpression;
  }

  private Code.Expr operand(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      if ((!(((((literal instanceof StringLit) || (literal instanceof NumberLit)) || (literal instanceof ListLit)) || (literal instanceof ParamRef)) || (literal instanceof BoolLit)))) {
        String _name = literal.eClass().getName();
        String _plus = (_name + " in a filter");
        throw Unsupported.of(_plus);
      }
      _xblockexpression = Literals.of(literal);
    }
    return _xblockexpression;
  }
}
