package org.ftn.vecDSL.generator.weaviate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EComparisonOp;
import org.ftn.vecDSL.vecDSL.EGeoUnit;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETextOp;
import org.ftn.vecDSL.vecDSL.EUnaryOp;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.GeoPoint;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Builds a Weaviate filter as a data tree that each runtime turns into its SDK's filter objects:
 * {"and": [...]}, {"or": [...]}, {"not": f} and leaves {"path", "op", "value"}. Leaves on number properties
 * carry "number": true so that runtimes send floating-point values. SQL-style 'like' patterns are converted
 * to Weaviate wildcards by the runtime.
 */
@SuppressWarnings("all")
public class WeaviateFilter {
  private final VectorCollection collection;

  private final List<Code.Expr> conjuncts = CollectionLiterals.<Code.Expr>newArrayList();

  public WeaviateFilter(final VectorCollection collection) {
    this.collection = collection;
  }

  public WeaviateFilter where(final FilterBlock block) {
    WeaviateFilter _xblockexpression = null;
    {
      if ((block != null)) {
        Code.Expr _render = this.render(block.getCondition());
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
        _xifexpression_1 = WeaviateSchema.kw("and", Code.items(this.conjuncts));
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  private Code.Expr render(final Condition condition) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (condition instanceof OrCondition) {
      _matched=true;
      Code.Expr _render = this.render(((OrCondition)condition).getLeft());
      Code.Expr _render_1 = this.render(((OrCondition)condition).getRight());
      _switchResult = WeaviateSchema.kw("or", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
    }
    if (!_matched) {
      if (condition instanceof AndCondition) {
        _matched=true;
        Code.Expr _render = this.render(((AndCondition)condition).getLeft());
        Code.Expr _render_1 = this.render(((AndCondition)condition).getRight());
        _switchResult = WeaviateSchema.kw("and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        _switchResult = WeaviateSchema.kw("not", this.render(((NotCondition)condition).getOperand()));
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
        _switchResult = this.geo(((GeoPredicate)condition));
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

  private String path(final FieldPath path) {
    String _xblockexpression = null;
    {
      if (((path.getRawPath() != null) || (!path.getSubFields().isEmpty()))) {
        throw Unsupported.of("nested paths; Weaviate filters on nested object properties are a preview feature (PROVISIONAL)");
      }
      _xblockexpression = WeaviateSchema.propertyName(this.collection, path.getField());
    }
    return _xblockexpression;
  }

  private boolean isNumber(final FieldPath path) {
    boolean _xblockexpression = false;
    {
      final Field field = path.getField();
      _xblockexpression = ((field instanceof PayloadField) && Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.FLOAT, EScalarDType.DOUBLE)).contains(this.element(((PayloadField) field))));
    }
    return _xblockexpression;
  }

  private boolean isArray(final FieldPath path) {
    return ((path.getField() instanceof PayloadField) && Objects.equals(((PayloadField) path.getField()).getType(), EScalarDType.ARRAY));
  }

  private EScalarDType element(final PayloadField field) {
    EScalarDType _xifexpression = null;
    EScalarDType _type = field.getType();
    boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
    if (_equals) {
      _xifexpression = field.getElementType();
    } else {
      _xifexpression = field.getType();
    }
    return _xifexpression;
  }

  private Code.Entries leaf(final FieldPath target, final String op, final Code.Expr value) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = WeaviateSchema.kw("path", Code.text(this.path(target)), "op", Code.text(op));
      if ((value != null)) {
        result.put("value", value);
      }
      boolean _isNumber = this.isNumber(target);
      if (_isNumber) {
        result.put("number", Code.flag(true));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Expr predicate(final Predicate predicate) {
    Code.Expr _xblockexpression = null;
    {
      final FieldPath target = predicate.getTarget();
      Literal _value = predicate.getValue();
      boolean _tripleNotEquals = (_value != null);
      if (_tripleNotEquals) {
        return this.comparison(target, predicate.getOp(), predicate.getValue());
      }
      Literal _text = predicate.getText();
      boolean _tripleNotEquals_1 = (_text != null);
      if (_tripleNotEquals_1) {
        return this.textPredicate(target, predicate);
      }
      Literal _lower = predicate.getLower();
      boolean _tripleNotEquals_2 = (_lower != null);
      if (_tripleNotEquals_2) {
        Code.Entries _leaf = this.leaf(target, "gte", this.value(predicate.getLower()));
        Code.Entries _leaf_1 = this.leaf(target, "lte", this.value(predicate.getUpper()));
        return WeaviateSchema.kw("and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_leaf, _leaf_1))));
      }
      boolean _written = ModelUtil.written(predicate, "lengthOp");
      if (_written) {
        return this.length(target, predicate.getLengthOp(), predicate.getLength());
      }
      Code.Expr _switchResult = null;
      EUnaryOp _unaryOp = predicate.getUnaryOp();
      if (_unaryOp != null) {
        switch (_unaryOp) {
          case IS_NULL:
          case NOT_EXISTS:
            _switchResult = this.leaf(target, "is_null", Code.flag(true));
            break;
          case IS_NOT_NULL:
          case EXISTS:
            _switchResult = this.leaf(target, "is_null", Code.flag(false));
            break;
          case IS_EMPTY:
            _switchResult = this.length(target, EComparisonOp.EQ, 0);
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Expr comparison(final FieldPath target, final EComparisonOp op, final Literal literal) {
    Code.Entries _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        boolean _equals = Objects.equals(op, EComparisonOp.EQ);
        if (_equals) {
          return this.leaf(target, "is_null", Code.flag(true));
        }
        boolean _equals_1 = Objects.equals(op, EComparisonOp.NEQ);
        if (_equals_1) {
          return this.leaf(target, "is_null", Code.flag(false));
        }
      }
      Code.Entries _switchResult = null;
      if (op != null) {
        switch (op) {
          case EQ:
            _switchResult = this.leaf(target, "eq", this.value(literal));
            break;
          case NEQ:
            _switchResult = this.leaf(target, "neq", this.value(literal));
            break;
          case GT:
            _switchResult = this.leaf(target, "gt", this.value(literal));
            break;
          case GTE:
            _switchResult = this.leaf(target, "gte", this.value(literal));
            break;
          case LT:
            _switchResult = this.leaf(target, "lt", this.value(literal));
            break;
          case LTE:
            _switchResult = this.leaf(target, "lte", this.value(literal));
            break;
          case IN:
            _switchResult = this.leaf(target, "contains_any", this.value(literal));
            break;
          case NOT_IN:
            _switchResult = this.leaf(target, "contains_none", this.value(literal));
            break;
          case LIKE:
            _switchResult = this.leaf(target, "like", this.value(literal));
            break;
          case PREFIX:
            String _constant = WeaviateFilter.constant(literal, "prefix");
            String _plus = (_constant + "%");
            _switchResult = this.leaf(target, "like", Code.text(_plus));
            break;
          case CONTAINS:
            Code.Entries _xifexpression = null;
            boolean _isArray = this.isArray(target);
            if (_isArray) {
              Code.Expr _value = this.value(literal);
              _xifexpression = this.leaf(target, "contains_any", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_value))));
            } else {
              String _constant_1 = WeaviateFilter.constant(literal, "contains");
              String _plus_1 = ("%" + _constant_1);
              String _plus_2 = (_plus_1 + "%");
              _xifexpression = this.leaf(target, "like", Code.text(_plus_2));
            }
            _switchResult = _xifexpression;
            break;
          case CONTAINS_ANY:
            _switchResult = this.leaf(target, "contains_any", this.value(literal));
            break;
          case CONTAINS_ALL:
            _switchResult = this.leaf(target, "contains_all", this.value(literal));
            break;
          default:
            String _literal = op.getLiteral();
            String _plus_3 = ("operator " + _literal);
            throw Unsupported.of(_plus_3);
        }
      } else {
        String _literal = op.getLiteral();
        String _plus_3 = ("operator " + _literal);
        throw Unsupported.of(_plus_3);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Expr textPredicate(final FieldPath target, final Predicate predicate) {
    Code.Entries _switchResult = null;
    ETextOp _textOp = predicate.getTextOp();
    if (_textOp != null) {
      switch (_textOp) {
        case MATCH:
        case MATCH_ANY_TERM:
        case CONTAINS_TEXT:
          _switchResult = this.leaf(target, "any_terms", this.value(predicate.getText()));
          break;
        case MATCH_ALL_TERMS:
          _switchResult = this.leaf(target, "all_terms", this.value(predicate.getText()));
          break;
        case WILDCARD:
          _switchResult = this.leaf(target, "wildcard", this.value(predicate.getText()));
          break;
        default:
          String _literal = predicate.getTextOp().getLiteral();
          String _plus = ("text operator " + _literal);
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = predicate.getTextOp().getLiteral();
      String _plus = ("text operator " + _literal);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Code.Expr length(final FieldPath target, final EComparisonOp op, final int length) {
    Code.Entries _xblockexpression = null;
    {
      boolean _contains = Collections.<EComparisonOp>unmodifiableList(CollectionLiterals.<EComparisonOp>newArrayList(EComparisonOp.EQ, EComparisonOp.NEQ, EComparisonOp.GT, EComparisonOp.GTE, EComparisonOp.LT, EComparisonOp.LTE)).contains(op);
      boolean _not = (!_contains);
      if (_not) {
        String _literal = op.getLiteral();
        String _plus = ("length operator " + _literal);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = WeaviateSchema.kw("path", Code.text(this.path(target)), "op", Code.text("length"), "compare", Code.text(op.getLiteral()), "value", Code.num(length));
    }
    return _xblockexpression;
  }

  private Code.Expr geo(final GeoPredicate geo) {
    Code.Entries _xblockexpression = null;
    {
      GeoPoint _center = geo.getCenter();
      boolean _tripleEquals = (_center == null);
      if (_tripleEquals) {
        throw Unsupported.of("geo predicates other than radius");
      }
      _xblockexpression = WeaviateSchema.kw(
        "path", Code.text(this.path(geo.getTarget())), 
        "op", Code.text("geo"), 
        "latitude", Code.num(geo.getCenter().getLatitude()), 
        "longitude", Code.num(geo.getCenter().getLongitude()), 
        "distance", Code.num(WeaviateFilter.meters(geo.getDistance(), geo.getUnit())));
    }
    return _xblockexpression;
  }

  private Code.Expr value(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        throw Unsupported.of("null literals in comparisons other than eq and neq");
      }
      _xblockexpression = Literals.of(literal);
    }
    return _xblockexpression;
  }

  private static double meters(final double distance, final EGeoUnit unit) {
    double _switchResult = (double) 0;
    if (unit != null) {
      switch (unit) {
        case KILOMETERS:
          _switchResult = (distance * 1000);
          break;
        case MILES:
          _switchResult = (distance * 1609.344);
          break;
        default:
          _switchResult = distance;
          break;
      }
    } else {
      _switchResult = distance;
    }
    return _switchResult;
  }

  private static String constant(final Literal literal, final String clause) {
    String _xifexpression = null;
    if ((literal instanceof StringLit)) {
      _xifexpression = ((StringLit)literal).getValue();
    } else {
      throw Unsupported.of((clause + " with a non-constant value"));
    }
    return _xifexpression;
  }
}
