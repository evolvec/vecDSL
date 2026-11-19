package org.ftn.vecDSL.generator.qdrant;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.BoolLit;
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

/**
 * Builds a Qdrant filter in the JSON form of the REST API, which the Python and TypeScript clients take as is and the
 * Java runtime converts to gRPC conditions: {"must": [...]}, {"should": [...]}, {"must_not": [...]} and field
 * conditions {"key": "price", "range": {"lte": 60}}. Qdrant matches exact values on strings, integers and booleans
 * only, so equality on float fields becomes a closed range.
 */
@SuppressWarnings("all")
public class QdrantFilter {
  private static final List<EScalarDType> FLOATING = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> TEMPORAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private static final List<EScalarDType> ORDERED = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private final List<Code.Expr> conjuncts = CollectionLiterals.<Code.Expr>newArrayList();

  public QdrantFilter where(final FilterBlock block) {
    QdrantFilter _xblockexpression = null;
    {
      if ((block != null)) {
        Code.Entries _render = this.render(block.getCondition());
        this.conjuncts.add(_render);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public QdrantFilter where(final Code.Entries condition) {
    QdrantFilter _xblockexpression = null;
    {
      this.conjuncts.add(condition);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public boolean isEmpty() {
    return this.conjuncts.isEmpty();
  }

  /**
   * The filter object: the conjuncts under "must", with nested conjunctions spliced in.
   */
  public Code.Expr code() {
    Code.Entries _xblockexpression = null;
    {
      boolean _isEmpty = this.conjuncts.isEmpty();
      if (_isEmpty) {
        return null;
      }
      final List<Code.Expr> flat = CollectionLiterals.<Code.Expr>newArrayList();
      for (final Code.Expr conjunct : this.conjuncts) {
        {
          final Code.Entries clause = ((Code.Entries) conjunct);
          if (((clause.entries().size() == 1) && clause.has("must"))) {
            Code.Expr _get = clause.get("must");
            List<Code.Expr> _items = ((Code.Items) _get).items();
            Iterables.<Code.Expr>addAll(flat, _items);
          } else {
            flat.add(conjunct);
          }
        }
      }
      _xblockexpression = QdrantSchema.kw("must", Code.items(flat));
    }
    return _xblockexpression;
  }

  private Code.Entries render(final Condition condition) {
    Code.Entries _switchResult = null;
    boolean _matched = false;
    if (condition instanceof OrCondition) {
      _matched=true;
      Code.Entries _render = this.render(((OrCondition)condition).getLeft());
      Code.Entries _render_1 = this.render(((OrCondition)condition).getRight());
      _switchResult = QdrantSchema.kw("should", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
    }
    if (!_matched) {
      if (condition instanceof AndCondition) {
        _matched=true;
        Code.Entries _render = this.render(((AndCondition)condition).getLeft());
        Code.Entries _render_1 = this.render(((AndCondition)condition).getRight());
        _switchResult = QdrantSchema.kw("must", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render, _render_1))));
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        Code.Entries _render = this.render(((NotCondition)condition).getOperand());
        _switchResult = QdrantSchema.kw("must_not", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_render))));
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
        _switchResult = this.element(((ElementPredicate)condition));
      }
    }
    if (!_matched) {
      throw Unsupported.of(condition.eClass().getName());
    }
    return _switchResult;
  }

  /**
   * Payload key of a field path: sub-fields joined by '.'; inside an element predicate, relative to the element.
   */
  private String key(final FieldPath path) {
    return QdrantSchema.indexPath(path);
  }

  private EScalarDType scalarType(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      final Field leaf = ModelUtil.leaf(path);
      if (((path.getRawPath() != null) || (!(leaf instanceof PayloadField)))) {
        return null;
      }
      _xblockexpression = ((PayloadField) leaf).getType();
    }
    return _xblockexpression;
  }

  private boolean isArray(final FieldPath path) {
    EScalarDType _scalarType = this.scalarType(path);
    return Objects.equals(_scalarType, EScalarDType.ARRAY);
  }

  /**
   * Value type the predicate compares against: the element type of an array field.
   */
  private EScalarDType valueType(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      final Field leaf = ModelUtil.leaf(path);
      boolean _isArray = this.isArray(path);
      if (_isArray) {
        return ((PayloadField) leaf).getElementType();
      }
      _xblockexpression = this.scalarType(path);
    }
    return _xblockexpression;
  }

  private Code.Entries condition(final FieldPath target, final String kind, final Code.Expr value) {
    return QdrantSchema.kw("key", Code.text(this.key(target)), kind, value);
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
      Literal _value = predicate.getValue();
      boolean _tripleNotEquals_1 = (_value != null);
      if (_tripleNotEquals_1) {
        return this.comparison(target, predicate.getOp(), predicate.getValue());
      }
      Literal _lower = predicate.getLower();
      boolean _tripleNotEquals_2 = (_lower != null);
      if (_tripleNotEquals_2) {
        this.ordered(target, "between");
        return this.condition(target, "range", QdrantSchema.kw("gte", this.operand(predicate.getLower()), "lte", this.operand(predicate.getUpper())));
      }
      boolean _written = ModelUtil.written(predicate, "lengthOp");
      if (_written) {
        return this.length(target, predicate.getLengthOp(), predicate.getLength());
      }
      final Code.Entries emptiness = QdrantSchema.kw("is_empty", QdrantSchema.kw("key", Code.text(this.key(target))));
      Code.Entries _switchResult = null;
      EUnaryOp _unaryOp = predicate.getUnaryOp();
      if (_unaryOp != null) {
        switch (_unaryOp) {
          case IS_EMPTY:
            _switchResult = emptiness;
            break;
          case IS_NULL:
          case NOT_EXISTS:
            Code.Entries _xblockexpression_1 = null;
            {
              this.scalarOnly(target, predicate.getUnaryOp().getLiteral());
              _xblockexpression_1 = emptiness;
            }
            _switchResult = _xblockexpression_1;
            break;
          case IS_NOT_NULL:
          case EXISTS:
            Code.Entries _xblockexpression_2 = null;
            {
              this.scalarOnly(target, predicate.getUnaryOp().getLiteral());
              _xblockexpression_2 = QdrantSchema.kw("must_not", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(emptiness))));
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Qdrant's is_empty also matches an empty array, so null tests are generated for non-array fields only.
   */
  private void scalarOnly(final FieldPath target, final String operator) {
    boolean _isArray = this.isArray(target);
    if (_isArray) {
      String _key = this.key(target);
      String _plus = ((operator + " on array field ") + _key);
      String _plus_1 = (_plus + "; Qdrant tests an array for emptiness, not for null");
      throw Unsupported.of(_plus_1);
    }
  }

  private void ordered(final FieldPath target, final String operator) {
    final EScalarDType type = this.valueType(target);
    if (((type != null) && (!QdrantFilter.ORDERED.contains(type)))) {
      String _literal = type.getLiteral();
      String _plus = ((operator + " on ") + _literal);
      String _plus_1 = (_plus + " field ");
      String _key = this.key(target);
      String _plus_2 = (_plus_1 + _key);
      String _plus_3 = (_plus_2 + "; Qdrant ranges compare numbers and datetimes");
      throw Unsupported.of(_plus_3);
    }
  }

  /**
   * Exact-value condition; floats match through a closed range because Qdrant matches only strings, integers and booleans.
   */
  private Code.Entries equal(final FieldPath target, final Literal literal) {
    Code.Entries _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        return QdrantSchema.kw("is_empty", QdrantSchema.kw("key", Code.text(this.key(target))));
      }
      final EScalarDType type = this.valueType(target);
      final Code.Expr value = this.operand(literal);
      if (((QdrantFilter.FLOATING.contains(type) || QdrantFilter.TEMPORAL.contains(type)) || (((type == null) && (value instanceof Code.Num)) && (!((Code.Num) value).integral())))) {
        return this.condition(target, "range", QdrantSchema.kw("gte", value, "lte", value));
      }
      _xblockexpression = this.condition(target, "match", QdrantSchema.kw("value", value));
    }
    return _xblockexpression;
  }

  private Code.Entries comparison(final FieldPath target, final EComparisonOp op, final Literal literal) {
    Code.Entries _switchResult = null;
    if (op != null) {
      switch (op) {
        case EQ:
          _switchResult = this.equal(target, literal);
          break;
        case NEQ:
          Code.Entries _equal = this.equal(target, literal);
          _switchResult = QdrantSchema.kw("must_not", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_equal))));
          break;
        case GT:
          _switchResult = this.bound(target, op, "gt", literal);
          break;
        case GTE:
          _switchResult = this.bound(target, op, "gte", literal);
          break;
        case LT:
          _switchResult = this.bound(target, op, "lt", literal);
          break;
        case LTE:
          _switchResult = this.bound(target, op, "lte", literal);
          break;
        case IN:
          _switchResult = this.oneOf(target, literal);
          break;
        case NOT_IN:
          Code.Entries _oneOf = this.oneOf(target, literal);
          _switchResult = QdrantSchema.kw("must_not", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_oneOf))));
          break;
        case CONTAINS:
          _switchResult = this.equal(target, literal);
          break;
        case CONTAINS_ANY:
          Code.Entries _xblockexpression = null;
          {
            boolean _isArray = this.isArray(target);
            boolean _not = (!_isArray);
            if (_not) {
              String _key = this.key(target);
              String _plus = ("contains_any on " + _key);
              String _plus_1 = (_plus + ", which is not an array");
              throw Unsupported.of(_plus_1);
            }
            _xblockexpression = this.oneOf(target, literal);
          }
          _switchResult = _xblockexpression;
          break;
        case CONTAINS_ALL:
          Code.Entries _xblockexpression_1 = null;
          {
            boolean _isArray = this.isArray(target);
            boolean _not = (!_isArray);
            if (_not) {
              String _key = this.key(target);
              String _plus = ("contains_all on " + _key);
              String _plus_1 = (_plus + ", which is not an array");
              throw Unsupported.of(_plus_1);
            }
            if ((!(literal instanceof ListLit))) {
              throw Unsupported.of("contains_all with a value that is not a list literal");
            }
            final Function1<Literal, Code.Entries> _function = (Literal it) -> {
              return this.equal(target, it);
            };
            _xblockexpression_1 = QdrantSchema.kw("must", Code.items(ListExtensions.<Literal, Code.Entries>map(((ListLit) literal).getItems(), _function)));
          }
          _switchResult = _xblockexpression_1;
          break;
        case PREFIX:
          Code.Entries _xblockexpression_2 = null;
          {
            this.textual(target, op.getLiteral());
            _xblockexpression_2 = this.condition(target, "match", QdrantSchema.kw("prefix", this.operand(literal)));
          }
          _switchResult = _xblockexpression_2;
          break;
        case LIKE:
          _switchResult = this.like(target, literal);
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }

  private Code.Entries bound(final FieldPath target, final EComparisonOp op, final String kind, final Literal literal) {
    Code.Entries _xblockexpression = null;
    {
      this.ordered(target, op.getLiteral());
      _xblockexpression = this.condition(target, "range", QdrantSchema.kw(kind, this.operand(literal)));
    }
    return _xblockexpression;
  }

  /**
   * Membership: match any for strings and integers, a disjunction of closed ranges for floats.
   */
  private Code.Entries oneOf(final FieldPath target, final Literal literal) {
    Code.Entries _xblockexpression = null;
    {
      final EScalarDType type = this.valueType(target);
      boolean _contains = QdrantFilter.FLOATING.contains(type);
      if (_contains) {
        if ((!(literal instanceof ListLit))) {
          String _key = this.key(target);
          String _plus = ("in on float field " + _key);
          String _plus_1 = (_plus + " with a value that is not a list literal");
          throw Unsupported.of(_plus_1);
        }
        final Function1<Literal, Code.Entries> _function = (Literal it) -> {
          return this.equal(target, it);
        };
        return QdrantSchema.kw("should", Code.items(ListExtensions.<Literal, Code.Entries>map(((ListLit) literal).getItems(), _function)));
      }
      _xblockexpression = this.condition(target, "match", QdrantSchema.kw("any", this.operand(literal)));
    }
    return _xblockexpression;
  }

  private void textual(final FieldPath target, final String operator) {
    final EScalarDType type = this.valueType(target);
    if (((((type != null) && (!Objects.equals(type, EScalarDType.VARCHAR))) && (!Objects.equals(type, EScalarDType.TEXT))) && (!Objects.equals(type, EScalarDType.UUID)))) {
      String _literal = type.getLiteral();
      String _plus = ((operator + " on ") + _literal);
      String _plus_1 = (_plus + " field ");
      String _key = this.key(target);
      String _plus_2 = (_plus_1 + _key);
      throw Unsupported.of(_plus_2);
    }
  }

  /**
   * like: a pattern without wildcards is equality, 'p%' a prefix and '%s%' a substring; other patterns have no Qdrant form.
   */
  private Code.Entries like(final FieldPath target, final Literal literal) {
    this.textual(target, "like");
    if ((!(literal instanceof StringLit))) {
      throw Unsupported.of("like with a pattern that is not a string literal");
    }
    final String pattern = ((StringLit) literal).getValue();
    final String core = pattern.replaceAll("^%|%$", "");
    if ((core.contains("%") || core.contains("_"))) {
      throw Unsupported.of((("like pattern \'" + pattern) + "\'; Qdrant matches exact values, prefixes and substrings"));
    }
    if ((((pattern.length() > 1) && pattern.startsWith("%")) && pattern.endsWith("%"))) {
      return this.condition(target, "match", QdrantSchema.kw("substring", Code.text(core)));
    }
    if ((pattern.endsWith("%") && (!pattern.startsWith("%")))) {
      return this.condition(target, "match", QdrantSchema.kw("prefix", Code.text(core)));
    }
    boolean _contains = pattern.contains("%");
    boolean _not = (!_contains);
    if (_not) {
      return this.condition(target, "match", QdrantSchema.kw("value", Code.text(core)));
    }
    throw Unsupported.of((("like pattern \'" + pattern) + "\'; Qdrant matches exact values, prefixes and substrings"));
  }

  /**
   * Array length through values_count.
   */
  private Code.Entries length(final FieldPath target, final EComparisonOp op, final int size) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isArray = this.isArray(target);
      boolean _not = (!_isArray);
      if (_not) {
        String _key = this.key(target);
        String _plus = ("length on " + _key);
        String _plus_1 = (_plus + ", which is not an array");
        throw Unsupported.of(_plus_1);
      }
      final Code.Num count = Code.num(size);
      Code.Entries _switchResult = null;
      if (op != null) {
        switch (op) {
          case EQ:
            _switchResult = this.condition(target, "values_count", QdrantSchema.kw("gte", count, "lte", count));
            break;
          case NEQ:
            Code.Entries _condition = this.condition(target, "values_count", QdrantSchema.kw("gte", count, "lte", count));
            _switchResult = QdrantSchema.kw("must_not", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_condition))));
            break;
          case GT:
            _switchResult = this.condition(target, "values_count", QdrantSchema.kw("gt", count));
            break;
          case GTE:
            _switchResult = this.condition(target, "values_count", QdrantSchema.kw("gte", count));
            break;
          case LT:
            _switchResult = this.condition(target, "values_count", QdrantSchema.kw("lt", count));
            break;
          case LTE:
            _switchResult = this.condition(target, "values_count", QdrantSchema.kw("lte", count));
            break;
          default:
            String _literal = op.getLiteral();
            String _plus_2 = ("length with operator " + _literal);
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal = op.getLiteral();
        String _plus_2 = ("length with operator " + _literal);
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries textPredicate(final FieldPath target, final Predicate predicate) {
    Code.Entries _xblockexpression = null;
    {
      this.textual(target, predicate.getTextOp().getLiteral());
      Code.Entries _switchResult = null;
      ETextOp _textOp = predicate.getTextOp();
      if (_textOp != null) {
        switch (_textOp) {
          case MATCH:
          case MATCH_ANY_TERM:
          case CONTAINS_TEXT:
            _switchResult = this.condition(target, "match", QdrantSchema.kw("text_any", this.operand(predicate.getText())));
            break;
          case MATCH_ALL_TERMS:
            _switchResult = this.condition(target, "match", QdrantSchema.kw("text", this.operand(predicate.getText())));
            break;
          case PHRASE:
            _switchResult = this.condition(target, "match", QdrantSchema.kw("phrase", this.operand(predicate.getText())));
            break;
          default:
            String _literal = predicate.getTextOp().getLiteral();
            String _plus = ("text operator " + _literal);
            String _plus_1 = (_plus + "; Qdrant matches terms and phrases");
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = predicate.getTextOp().getLiteral();
        String _plus = ("text operator " + _literal);
        String _plus_1 = (_plus + "; Qdrant matches terms and phrases");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries geo(final GeoPredicate predicate) {
    final FieldPath target = predicate.getTarget();
    GeoPoint _center = predicate.getCenter();
    boolean _tripleNotEquals = (_center != null);
    if (_tripleNotEquals) {
      double _switchResult = (double) 0;
      EGeoUnit _unit = predicate.getUnit();
      if (_unit != null) {
        switch (_unit) {
          case KILOMETERS:
            double _distance = predicate.getDistance();
            _switchResult = (_distance * 1000);
            break;
          case MILES:
            double _distance_1 = predicate.getDistance();
            _switchResult = (_distance_1 * 1609.344);
            break;
          default:
            _switchResult = predicate.getDistance();
            break;
        }
      } else {
        _switchResult = predicate.getDistance();
      }
      final double meters = _switchResult;
      return this.condition(target, "geo_radius", QdrantSchema.kw("center", this.point(predicate.getCenter()), "radius", Code.num(meters)));
    }
    GeoPoint _topLeft = predicate.getTopLeft();
    boolean _tripleNotEquals_1 = (_topLeft != null);
    if (_tripleNotEquals_1) {
      return this.condition(target, "geo_bounding_box", QdrantSchema.kw("top_left", this.point(predicate.getTopLeft()), "bottom_right", this.point(predicate.getBottomRight())));
    }
    boolean _isEmpty = predicate.getVertices().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      final List<Code.Expr> ring = CollectionLiterals.<Code.Expr>newArrayList();
      final Function1<GeoPoint, Code.Entries> _function = (GeoPoint it) -> {
        return this.point(it);
      };
      List<Code.Entries> _map = ListExtensions.<GeoPoint, Code.Entries>map(predicate.getVertices(), _function);
      Iterables.<Code.Expr>addAll(ring, _map);
      final GeoPoint first = IterableExtensions.<GeoPoint>head(predicate.getVertices());
      final GeoPoint last = predicate.getVertices().getLast();
      if (((first.getLatitude() != last.getLatitude()) || (first.getLongitude() != last.getLongitude()))) {
        Code.Entries _point = this.point(first);
        ring.add(_point);
      }
      return this.condition(target, "geo_polygon", QdrantSchema.kw("exterior", QdrantSchema.kw("points", Code.items(ring))));
    }
    throw Unsupported.of("geo relation predicates with WKT; Qdrant filters by radius, bounding box and polygon");
  }

  private Code.Entries point(final GeoPoint point) {
    return QdrantSchema.kw("lon", Code.num(point.getLongitude()), "lat", Code.num(point.getLatitude()));
  }

  /**
   * 'any' over an array of objects: a nested condition whose keys are relative to the array element.
   */
  private Code.Entries element(final ElementPredicate predicate) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isArray = this.isArray(predicate.getTarget());
      boolean _not = (!_isArray);
      if (_not) {
        String _key = this.key(predicate.getTarget());
        String _plus = ("element predicates (\'any\') on " + _key);
        String _plus_1 = (_plus + ", which is not an array");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries inner = this.render(predicate.getCondition());
      Code.Entries _xifexpression = null;
      if (((inner.has("must") || inner.has("should")) || inner.has("must_not"))) {
        _xifexpression = inner;
      } else {
        _xifexpression = QdrantSchema.kw("must", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(inner))));
      }
      final Code.Entries filter = _xifexpression;
      _xblockexpression = QdrantSchema.kw("nested", QdrantSchema.kw("key", Code.text(this.key(predicate.getTarget())), "filter", filter));
    }
    return _xblockexpression;
  }

  private Code.Expr operand(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        throw Unsupported.of("null literals in comparisons other than eq and neq");
      }
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
