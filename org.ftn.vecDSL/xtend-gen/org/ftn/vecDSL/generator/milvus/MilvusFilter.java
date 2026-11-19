package org.ftn.vecDSL.generator.milvus;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.common.Py;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EComparisonOp;
import org.ftn.vecDSL.vecDSL.EGeoRelation;
import org.ftn.vecDSL.vecDSL.EGeoUnit;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETextOp;
import org.ftn.vecDSL.vecDSL.EUnaryOp;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.EnvRef;
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
import org.ftn.vecDSL.vecDSL.VecDSLFactory;

/**
 * Builds a Milvus boolean expression from vecDSL conditions. Program parameters and environment values
 * become {name} placeholders that the runtime fills with escaped Milvus literals, so the same expression
 * runs on standalone and embedded deployments.
 */
@SuppressWarnings("all")
public class MilvusFilter {
  private static final String OPEN = String.valueOf(((char) 1));

  private static final String CLOSE = String.valueOf(((char) 2));

  private final List<String> conjuncts = CollectionLiterals.<String>newArrayList();

  private final Map<String, Code.Expr> values = new LinkedHashMap<String, Code.Expr>();

  public MilvusFilter where(final FilterBlock block) {
    MilvusFilter _xblockexpression = null;
    {
      if ((block != null)) {
        String _render = this.render(block.getCondition());
        this.conjuncts.add(_render);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public MilvusFilter add(final String expression) {
    MilvusFilter _xblockexpression = null;
    {
      this.conjuncts.add(expression);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public boolean isEmpty() {
    return this.conjuncts.isEmpty();
  }

  public String placeholder(final String name, final Code.Expr value) {
    String _xblockexpression = null;
    {
      this.values.put(name, value);
      _xblockexpression = ((MilvusFilter.OPEN + name) + MilvusFilter.CLOSE);
    }
    return _xblockexpression;
  }

  public Code.Filter code() {
    Code.Filter _xblockexpression = null;
    {
      boolean _isEmpty = this.conjuncts.isEmpty();
      if (_isEmpty) {
        return null;
      }
      String _xifexpression = null;
      int _size = this.conjuncts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        _xifexpression = IterableExtensions.<String>head(this.conjuncts);
      } else {
        final Function1<String, String> _function = (String it) -> {
          return (("(" + it) + ")");
        };
        _xifexpression = IterableExtensions.join(ListExtensions.<String, String>map(this.conjuncts, _function), " and ");
      }
      String template = _xifexpression;
      Set<String> _keySet = this.values.keySet();
      for (final String name : _keySet) {
        template = template.replace(((MilvusFilter.OPEN + name) + MilvusFilter.CLOSE), (("{" + name) + "}"));
      }
      LinkedHashMap<String, Code.Expr> _linkedHashMap = new LinkedHashMap<String, Code.Expr>(this.values);
      _xblockexpression = new Code.Filter(template, _linkedHashMap);
    }
    return _xblockexpression;
  }

  public String render(final Condition condition) {
    String _switchResult = null;
    boolean _matched = false;
    if (condition instanceof OrCondition) {
      _matched=true;
      String _render = this.render(((OrCondition)condition).getLeft());
      String _plus = (_render + " or ");
      String _render_1 = this.render(((OrCondition)condition).getRight());
      _switchResult = (_plus + _render_1);
    }
    if (!_matched) {
      if (condition instanceof AndCondition) {
        _matched=true;
        String _grouped = this.grouped(((AndCondition)condition).getLeft());
        String _plus = (_grouped + " and ");
        String _grouped_1 = this.grouped(((AndCondition)condition).getRight());
        _switchResult = (_plus + _grouped_1);
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        String _render = this.render(((NotCondition)condition).getOperand());
        String _plus = ("not (" + _render);
        _switchResult = (_plus + ")");
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

  private String grouped(final Condition condition) {
    String _xifexpression = null;
    if ((condition instanceof OrCondition)) {
      String _render = this.render(condition);
      String _plus = ("(" + _render);
      _xifexpression = (_plus + ")");
    } else {
      _xifexpression = this.render(condition);
    }
    return _xifexpression;
  }

  public String value(final Literal literal) {
    String _switchResult = null;
    boolean _matched = false;
    if (literal instanceof StringLit) {
      _matched=true;
      _switchResult = Py.str(((StringLit)literal).getValue());
    }
    if (!_matched) {
      if (literal instanceof NumberLit) {
        _matched=true;
        _switchResult = Py.num(((NumberLit)literal).getValue());
      }
    }
    if (!_matched) {
      if (literal instanceof BoolLit) {
        _matched=true;
        String _xifexpression = null;
        boolean _isValue = ((BoolLit)literal).isValue();
        if (_isValue) {
          _xifexpression = "true";
        } else {
          _xifexpression = "false";
        }
        _switchResult = _xifexpression;
      }
    }
    if (!_matched) {
      if (literal instanceof ListLit) {
        _matched=true;
        final Function1<Literal, String> _function = (Literal it) -> {
          return this.value(it);
        };
        String _join = IterableExtensions.join(ListExtensions.<Literal, String>map(((ListLit)literal).getItems(), _function), ", ");
        String _plus = ("[" + _join);
        _switchResult = (_plus + "]");
      }
    }
    if (!_matched) {
      if (literal instanceof ParamRef) {
        _matched=true;
        String _ident = Py.ident(((ParamRef)literal).getParam().getName());
        String _name = ((ParamRef)literal).getParam().getName();
        Code.Param _param = new Code.Param(_name);
        _switchResult = this.placeholder(_ident, _param);
      }
    }
    if (!_matched) {
      if (literal instanceof EnvRef) {
        _matched=true;
        String _ident = Py.ident(((EnvRef)literal).getVariable());
        String _plus = ("env_" + _ident);
        String _variable = ((EnvRef)literal).getVariable();
        Code.Env _env = new Code.Env(_variable);
        _switchResult = this.placeholder(_plus, _env);
      }
    }
    if (!_matched) {
      if (literal instanceof NullLit) {
        _matched=true;
        throw Unsupported.of("null literals in comparisons; use is_null");
      }
    }
    if (!_matched) {
      String _name = literal.eClass().getName();
      String _plus = (_name + " in filters");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  public static String path(final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        final String[] segments = path.getRawPath().split("\\.");
        String _head = IterableExtensions.<String>head(((Iterable<String>)Conversions.doWrapArray(segments)));
        final Function1<String, String> _function = (String it) -> {
          return (("[\"" + it) + "\"]");
        };
        String _join = IterableExtensions.join(IterableExtensions.<String, String>map(IterableExtensions.<String>tail(((Iterable<String>)Conversions.doWrapArray(segments))), _function));
        return (_head + _join);
      }
      final String root = ModelUtil.fieldName(path.getField());
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        return root;
      }
      boolean _isJson = MilvusFilter.isJson(path.getField());
      boolean _not = (!_isJson);
      if (_not) {
        throw Unsupported.of((("nested paths into " + root) + ", which is not a JSON field"));
      }
      final Function1<Field, String> _function_1 = (Field it) -> {
        String _fieldName = ModelUtil.fieldName(it);
        String _plus = ("[\"" + _fieldName);
        return (_plus + "\"]");
      };
      String _join_1 = IterableExtensions.join(ListExtensions.<Field, String>map(path.getSubFields(), _function_1));
      _xblockexpression = (root + _join_1);
    }
    return _xblockexpression;
  }

  public static boolean isJson(final Field field) {
    return ((field instanceof PayloadField) && Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.JSON, EScalarDType.MAP, EScalarDType.STRUCT)).contains(((PayloadField) field).getType()));
  }

  public static boolean isArray(final Field field) {
    return ((field instanceof PayloadField) && Objects.equals(((PayloadField) field).getType(), EScalarDType.ARRAY));
  }

  private String predicate(final Predicate predicate) {
    String _xblockexpression = null;
    {
      final String field = MilvusFilter.path(predicate.getTarget());
      final boolean jsonPath = (((predicate.getTarget().getRawPath() != null) || (!predicate.getTarget().getSubFields().isEmpty())) || MilvusFilter.isJson(predicate.getTarget().getField()));
      Literal _value = predicate.getValue();
      boolean _tripleNotEquals = (_value != null);
      if (_tripleNotEquals) {
        return this.comparison(field, ModelUtil.leaf(predicate.getTarget()), jsonPath, predicate.getOp(), predicate.getValue());
      }
      Literal _text = predicate.getText();
      boolean _tripleNotEquals_1 = (_text != null);
      if (_tripleNotEquals_1) {
        return this.text(field, predicate.getTextOp(), predicate.getText());
      }
      Literal _lower = predicate.getLower();
      boolean _tripleNotEquals_2 = (_lower != null);
      if (_tripleNotEquals_2) {
        String _value_1 = this.value(predicate.getLower());
        String _plus = ((field + " >= ") + _value_1);
        String _plus_1 = (_plus + " and ");
        String _plus_2 = (_plus_1 + field);
        String _plus_3 = (_plus_2 + " <= ");
        String _value_2 = this.value(predicate.getUpper());
        return (_plus_3 + _value_2);
      }
      boolean _written = ModelUtil.written(predicate, "lengthOp");
      if (_written) {
        String _operator = this.operator(predicate.getLengthOp());
        String _plus_4 = ((("ARRAY_LENGTH(" + field) + ") ") + _operator);
        String _plus_5 = (_plus_4 + " ");
        int _length = predicate.getLength();
        return (_plus_5 + Integer.valueOf(_length));
      }
      _xblockexpression = this.unary(field, jsonPath, predicate.getUnaryOp());
    }
    return _xblockexpression;
  }

  private String comparison(final String field, final Field leaf, final boolean jsonPath, final EComparisonOp op, final Literal literal) {
    String _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        boolean _equals = Objects.equals(op, EComparisonOp.EQ);
        if (_equals) {
          return (field + " is null");
        }
        boolean _equals_1 = Objects.equals(op, EComparisonOp.NEQ);
        if (_equals_1) {
          return (field + " is not null");
        }
      }
      final String symbol = this.operator(op);
      if ((symbol != null)) {
        String _value = this.value(literal);
        return ((((field + " ") + symbol) + " ") + _value);
      }
      String _switchResult = null;
      if (op != null) {
        switch (op) {
          case IN:
            String _value_1 = this.value(literal);
            _switchResult = ((field + " in ") + _value_1);
            break;
          case NOT_IN:
            String _value_2 = this.value(literal);
            _switchResult = ((field + " not in ") + _value_2);
            break;
          case LIKE:
            String _value_3 = this.value(literal);
            _switchResult = ((field + " like ") + _value_3);
            break;
          case PREFIX:
            String _constant = MilvusFilter.constant(literal, "prefix");
            String _plus = (_constant + "%");
            String _str = Py.str(_plus);
            _switchResult = ((field + " like ") + _str);
            break;
          case CONTAINS:
            String _xifexpression = null;
            if (jsonPath) {
              String _value_4 = this.value(literal);
              String _plus_1 = ((("JSON_CONTAINS(" + field) + ", ") + _value_4);
              _xifexpression = (_plus_1 + ")");
            } else {
              String _xifexpression_1 = null;
              boolean _isArray = MilvusFilter.isArray(leaf);
              if (_isArray) {
                String _value_5 = this.value(literal);
                String _plus_2 = ((("ARRAY_CONTAINS(" + field) + ", ") + _value_5);
                _xifexpression_1 = (_plus_2 + ")");
              } else {
                String _constant_1 = MilvusFilter.constant(literal, "contains");
                String _plus_3 = ("%" + _constant_1);
                String _plus_4 = (_plus_3 + "%");
                String _str_1 = Py.str(_plus_4);
                _xifexpression_1 = ((field + " like ") + _str_1);
              }
              _xifexpression = _xifexpression_1;
            }
            _switchResult = _xifexpression;
            break;
          case CONTAINS_ANY:
            String _xifexpression_2 = null;
            if (jsonPath) {
              _xifexpression_2 = "JSON_CONTAINS_ANY(";
            } else {
              _xifexpression_2 = "ARRAY_CONTAINS_ANY(";
            }
            String _plus_5 = (_xifexpression_2 + field);
            String _plus_6 = (_plus_5 + ", ");
            String _value_6 = this.value(literal);
            String _plus_7 = (_plus_6 + _value_6);
            _switchResult = (_plus_7 + ")");
            break;
          case CONTAINS_ALL:
            String _xifexpression_3 = null;
            if (jsonPath) {
              _xifexpression_3 = "JSON_CONTAINS_ALL(";
            } else {
              _xifexpression_3 = "ARRAY_CONTAINS_ALL(";
            }
            String _plus_8 = (_xifexpression_3 + field);
            String _plus_9 = (_plus_8 + ", ");
            String _value_7 = this.value(literal);
            String _plus_10 = (_plus_9 + _value_7);
            _switchResult = (_plus_10 + ")");
            break;
          default:
            String _literal = op.getLiteral();
            String _plus_11 = ("operator " + _literal);
            throw Unsupported.of(_plus_11);
        }
      } else {
        String _literal = op.getLiteral();
        String _plus_11 = ("operator " + _literal);
        throw Unsupported.of(_plus_11);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private String operator(final EComparisonOp op) {
    String _switchResult = null;
    if (op != null) {
      switch (op) {
        case EQ:
          _switchResult = "==";
          break;
        case NEQ:
          _switchResult = "!=";
          break;
        case GT:
          _switchResult = ">";
          break;
        case GTE:
          _switchResult = ">=";
          break;
        case LT:
          _switchResult = "<";
          break;
        case LTE:
          _switchResult = "<=";
          break;
        default:
          _switchResult = null;
          break;
      }
    } else {
      _switchResult = null;
    }
    return _switchResult;
  }

  private String text(final String field, final ETextOp op, final Literal literal) {
    String _switchResult = null;
    if (op != null) {
      switch (op) {
        case MATCH:
          String _value = this.value(literal);
          String _plus = ((("TEXT_MATCH(" + field) + ", ") + _value);
          _switchResult = (_plus + ")");
          break;
        case MATCH_ANY_TERM:
          String _value_1 = this.value(literal);
          String _plus_1 = ((("TEXT_MATCH(" + field) + ", ") + _value_1);
          _switchResult = (_plus_1 + ")");
          break;
        case CONTAINS_TEXT:
          String _value_2 = this.value(literal);
          String _plus_2 = ((("TEXT_MATCH(" + field) + ", ") + _value_2);
          _switchResult = (_plus_2 + ")");
          break;
        case MATCH_ALL_TERMS:
          final Function1<String, String> _function = (String it) -> {
            String _str = Py.str(it);
            String _plus_3 = ((("TEXT_MATCH(" + field) + ", ") + _str);
            return (_plus_3 + ")");
          };
          _switchResult = IterableExtensions.join(ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(MilvusFilter.constant(literal, "match_all_terms").trim().split("\\s+"))), _function), " and ");
          break;
        case PHRASE:
          String _value_3 = this.value(literal);
          String _plus_3 = ((("PHRASE_MATCH(" + field) + ", ") + _value_3);
          _switchResult = (_plus_3 + ")");
          break;
        case WILDCARD:
          String _str = Py.str(MilvusFilter.constant(literal, "wildcard").replace("*", "%").replace("?", "_"));
          _switchResult = ((field + " like ") + _str);
          break;
        default:
          String _literal = op.getLiteral();
          String _plus_4 = ("text operator " + _literal);
          throw Unsupported.of(_plus_4);
      }
    } else {
      String _literal = op.getLiteral();
      String _plus_4 = ("text operator " + _literal);
      throw Unsupported.of(_plus_4);
    }
    return _switchResult;
  }

  private String unary(final String field, final boolean jsonPath, final EUnaryOp op) {
    String _switchResult = null;
    if (op != null) {
      switch (op) {
        case IS_NULL:
          _switchResult = (field + " is null");
          break;
        case IS_NOT_NULL:
          _switchResult = (field + " is not null");
          break;
        case EXISTS:
          String _xifexpression = null;
          if (jsonPath) {
            _xifexpression = ("exists " + field);
          } else {
            _xifexpression = (field + " is not null");
          }
          _switchResult = _xifexpression;
          break;
        case NOT_EXISTS:
          String _xifexpression_1 = null;
          if (jsonPath) {
            _xifexpression_1 = ("not exists " + field);
          } else {
            _xifexpression_1 = (field + " is null");
          }
          _switchResult = _xifexpression_1;
          break;
        case IS_EMPTY:
          _switchResult = (("ARRAY_LENGTH(" + field) + ") == 0");
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }

  private String geo(final GeoPredicate geo) {
    String _xblockexpression = null;
    {
      final String field = MilvusFilter.path(geo.getTarget());
      GeoPoint _center = geo.getCenter();
      boolean _tripleNotEquals = (_center != null);
      if (_tripleNotEquals) {
        String _point = MilvusFilter.point(geo.getCenter());
        String _plus = ((("ST_DWITHIN(" + field) + ", \'") + _point);
        String _plus_1 = (_plus + "\', ");
        String _num = Py.num(MilvusFilter.meters(geo.getDistance(), geo.getUnit()));
        String _plus_2 = (_plus_1 + _num);
        return (_plus_2 + ")");
      }
      GeoPoint _topLeft = geo.getTopLeft();
      boolean _tripleNotEquals_1 = (_topLeft != null);
      if (_tripleNotEquals_1) {
        final GeoPoint tl = geo.getTopLeft();
        final GeoPoint br = geo.getBottomRight();
        GeoPoint _corner = MilvusFilter.corner(tl, br);
        GeoPoint _corner_1 = MilvusFilter.corner(br, tl);
        String _polygon = MilvusFilter.polygon(Collections.<GeoPoint>unmodifiableList(CollectionLiterals.<GeoPoint>newArrayList(tl, _corner, br, _corner_1)));
        String _plus_3 = ((("ST_WITHIN(" + field) + ", \'") + _polygon);
        return (_plus_3 + "\')");
      }
      boolean _isEmpty = geo.getVertices().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _polygon_1 = MilvusFilter.polygon(geo.getVertices());
        String _plus_4 = ((("ST_WITHIN(" + field) + ", \'") + _polygon_1);
        return (_plus_4 + "\')");
      }
      String _replace = geo.getWkt().replace("\'", "");
      String _plus_5 = ("\'" + _replace);
      final String wkt = (_plus_5 + "\'");
      String _switchResult = null;
      EGeoRelation _relation = geo.getRelation();
      if (_relation != null) {
        switch (_relation) {
          case INTERSECTS:
            _switchResult = (((("ST_INTERSECTS(" + field) + ", ") + wkt) + ")");
            break;
          case WITHIN:
            _switchResult = (((("ST_WITHIN(" + field) + ", ") + wkt) + ")");
            break;
          case CONTAINS:
            _switchResult = (((("ST_CONTAINS(" + field) + ", ") + wkt) + ")");
            break;
          case DISJOINT:
            _switchResult = (((("not ST_INTERSECTS(" + field) + ", ") + wkt) + ")");
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private static String point(final GeoPoint point) {
    String _num = Py.num(point.getLongitude());
    String _plus = ("POINT(" + _num);
    String _plus_1 = (_plus + " ");
    String _num_1 = Py.num(point.getLatitude());
    String _plus_2 = (_plus_1 + _num_1);
    return (_plus_2 + ")");
  }

  private static GeoPoint corner(final GeoPoint latitudeOf, final GeoPoint longitudeOf) {
    GeoPoint _xblockexpression = null;
    {
      final GeoPoint corner = VecDSLFactory.eINSTANCE.createGeoPoint();
      corner.setLatitude(latitudeOf.getLatitude());
      corner.setLongitude(longitudeOf.getLongitude());
      _xblockexpression = corner;
    }
    return _xblockexpression;
  }

  private static String polygon(final List<GeoPoint> vertices) {
    String _xblockexpression = null;
    {
      final ArrayList<String> ring = CollectionLiterals.<String>newArrayList();
      final Function1<GeoPoint, String> _function = (GeoPoint it) -> {
        String _num = Py.num(it.getLongitude());
        String _plus = (_num + " ");
        String _num_1 = Py.num(it.getLatitude());
        return (_plus + _num_1);
      };
      List<String> _map = ListExtensions.<GeoPoint, String>map(vertices, _function);
      Iterables.<String>addAll(ring, _map);
      String _head = IterableExtensions.<String>head(ring);
      String _last = ring.getLast();
      boolean _notEquals = (!Objects.equals(_head, _last));
      if (_notEquals) {
        String _head_1 = IterableExtensions.<String>head(ring);
        ring.add(_head_1);
      }
      String _join = IterableExtensions.join(ring, ", ");
      String _plus = ("POLYGON((" + _join);
      _xblockexpression = (_plus + "))");
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
