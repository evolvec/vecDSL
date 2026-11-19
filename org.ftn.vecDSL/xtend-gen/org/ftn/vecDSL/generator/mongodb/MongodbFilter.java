package org.ftn.vecDSL.generator.mongodb;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.emf.common.util.EList;
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
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.GeoPoint;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;

/**
 * Translates a vecDSL filter into an MQL query document. Every value is the operand of an explicit operator ($eq, $in, ...),
 * so a value that looks like an operator stays a literal. A negation is $nor, which (like the other vecDSL targets) matches
 * documents without the field. In a $vectorSearch pre-filter only $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or
 * and $nor on indexed paths are allowed; other conditions raise Unsupported there.
 */
@SuppressWarnings("all")
public class MongodbFilter {
  private static final double EARTH_RADIUS_KM = 6378.1;

  private static final String VECTOR_REASON = "; the $vectorSearch pre-filter accepts $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or, $not and $nor on boolean, date, numeric, string and UUID values";

  private final MongodbCollection collection;

  private final boolean vector;

  public MongodbFilter(final MongodbCollection collection, final boolean vector) {
    this.collection = collection;
    this.vector = vector;
  }

  public Code.Entries query(final Condition node) {
    return this.query(node, false);
  }

  private Code.Entries query(final Condition node, final boolean element) {
    Code.Entries _switchResult = null;
    boolean _matched = false;
    if (node instanceof AndCondition) {
      _matched=true;
      _switchResult = MongodbCommands.obj("$and", MongodbCommands.arr(this.query(((AndCondition)node).getLeft(), element), this.query(((AndCondition)node).getRight(), element)));
    }
    if (!_matched) {
      if (node instanceof OrCondition) {
        _matched=true;
        _switchResult = MongodbCommands.obj("$or", MongodbCommands.arr(this.query(((OrCondition)node).getLeft(), element), this.query(((OrCondition)node).getRight(), element)));
      }
    }
    if (!_matched) {
      if (node instanceof NotCondition) {
        _matched=true;
        _switchResult = MongodbCommands.obj("$nor", MongodbCommands.arr(this.query(((NotCondition)node).getOperand(), element)));
      }
    }
    if (!_matched) {
      if (node instanceof Predicate) {
        _matched=true;
        _switchResult = this.predicate(((Predicate)node), element);
      }
    }
    if (!_matched) {
      if (node instanceof GeoPredicate) {
        _matched=true;
        _switchResult = this.geo(((GeoPredicate)node), element);
      }
    }
    if (!_matched) {
      if (node instanceof ElementPredicate) {
        _matched=true;
        _switchResult = this.elementQuery(((ElementPredicate)node), element);
      }
    }
    if (!_matched) {
      throw Unsupported.of(node.eClass().getName());
    }
    return _switchResult;
  }

  /**
   * A field path as MongoDB names it: _id for the key, dotted sub-fields, relative paths inside $elemMatch.
   */
  private String resolve(final FieldPath path, final boolean element) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        String[] _split = path.getRawPath().split("\\.");
        for (final String segment : _split) {
          MongodbCommands.field(segment);
        }
        return path.getRawPath();
      }
      final List<String> segments = CollectionLiterals.<String>newArrayList();
      String _xifexpression = null;
      if (element) {
        _xifexpression = ModelUtil.fieldName(path.getField());
      } else {
        _xifexpression = this.collection.path(ModelUtil.fieldName(path.getField()));
      }
      segments.add(_xifexpression);
      final Function1<Field, String> _function = (Field s) -> {
        return ModelUtil.fieldName(s);
      };
      List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
      Iterables.<String>addAll(segments, _map);
      final String joined = IterableExtensions.join(segments, ".");
      if ((this.vector && (!this.collection.filterPaths().contains(joined)))) {
        throw Unsupported.of(((("a condition on " + joined) + ", which the vectorSearch index does not filter") + MongodbFilter.VECTOR_REASON));
      }
      _xblockexpression = joined;
    }
    return _xblockexpression;
  }

  private boolean dated(final FieldPath path) {
    boolean _xblockexpression = false;
    {
      final Field leaf = ModelUtil.leaf(path);
      _xblockexpression = (((leaf instanceof PayloadField) && Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("date", "datetime", "timestamptz")).contains(((PayloadField) leaf).getType().getLiteral())) || (((leaf instanceof PayloadField) && Objects.equals(((PayloadField) leaf).getType().getLiteral(), "array")) && Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("date", "datetime", "timestamptz")).contains(((PayloadField) leaf).getElementType().getLiteral())));
    }
    return _xblockexpression;
  }

  private Code.Entries predicate(final Predicate p, final boolean element) {
    Code.Entries _xblockexpression = null;
    {
      final String path = this.resolve(p.getTarget(), element);
      final boolean dates = this.dated(p.getTarget());
      Literal _text = p.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        return this.textCondition(p.getTextOp().getLiteral(), path, p.getText());
      }
      Literal _lower = p.getLower();
      boolean _tripleNotEquals_1 = (_lower != null);
      if (_tripleNotEquals_1) {
        return MongodbCommands.obj(path, MongodbCommands.obj("$gte", this.operand(p.getLower(), dates), "$lte", this.operand(p.getUpper(), dates)));
      }
      boolean _written = ModelUtil.written(p, "lengthOp");
      if (_written) {
        if (this.vector) {
          throw Unsupported.of(("length conditions in a vector search" + MongodbFilter.VECTOR_REASON));
        }
        final Code.Entries size = MongodbCommands.obj("$cond", MongodbCommands.arr(MongodbCommands.obj("$isArray", MongodbCommands.arr(MongodbCommands.text(("$" + path)))), MongodbCommands.obj("$size", MongodbCommands.text(("$" + path))), MongodbCommands.obj("$cond", MongodbCommands.arr(MongodbCommands.obj("$eq", MongodbCommands.arr(MongodbCommands.obj("$type", MongodbCommands.text(("$" + path))), MongodbCommands.text("missing"))), MongodbCommands.num(0), MongodbCommands.num(1)))));
        String _lengthOperator = this.lengthOperator(p.getLengthOp().getLiteral());
        String _plus = ("$" + _lengthOperator);
        return MongodbCommands.obj("$expr", MongodbCommands.obj(_plus, MongodbCommands.arr(size, MongodbCommands.num(p.getLength()))));
      }
      Literal _value = p.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        Code.Entries _switchResult = null;
        String _literal = p.getUnaryOp().getLiteral();
        if (_literal != null) {
          switch (_literal) {
            case "exists":
              _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(true)));
              break;
            case "is_not_null":
              Code.Entries _xblockexpression_1 = null;
              {
                if (this.vector) {
                  throw Unsupported.of(("is_not_null in a vector search; null is not a filterable value" + MongodbFilter.VECTOR_REASON));
                }
                _xblockexpression_1 = MongodbCommands.obj(path, MongodbCommands.obj("$ne", this.nil()));
              }
              _switchResult = _xblockexpression_1;
              break;
            case "is_null":
              Code.Entries _xblockexpression_2 = null;
              {
                if (this.vector) {
                  throw Unsupported.of(("is_null in a vector search; null is not a filterable value" + MongodbFilter.VECTOR_REASON));
                }
                _xblockexpression_2 = MongodbCommands.obj(path, MongodbCommands.obj("$eq", this.nil()));
              }
              _switchResult = _xblockexpression_2;
              break;
            default:
              _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(false)));
              break;
          }
        } else {
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(false)));
        }
        return _switchResult;
      }
      Literal _value_1 = p.getValue();
      if ((_value_1 instanceof NullLit)) {
        throw Unsupported.of("comparisons with null; write is_null or is_not_null");
      }
      _xblockexpression = this.comparison(p.getOp().getLiteral(), path, p.getValue(), dates);
    }
    return _xblockexpression;
  }

  private String lengthOperator(final String op) {
    String _switchResult = null;
    if (op != null) {
      switch (op) {
        case "neq":
          _switchResult = "ne";
          break;
        default:
          _switchResult = op;
          break;
      }
    } else {
      _switchResult = op;
    }
    return _switchResult;
  }

  private Code.Expr nil() {
    return new Code.Nil();
  }

  private Code.Entries comparison(final String op, final String path, final Literal value, final boolean dates) {
    Code.Entries _switchResult = null;
    if (op != null) {
      switch (op) {
        case "eq":
        case "contains":
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$eq", this.operand(value, dates)));
          break;
        case "neq":
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$ne", this.operand(value, dates)));
          break;
        case "gt":
        case "gte":
        case "lt":
        case "lte":
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj(("$" + op), this.operand(value, dates)));
          break;
        case "in":
        case "contains_any":
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$in", this.operand(value, dates)));
          break;
        case "not_in":
          _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$nin", this.operand(value, dates)));
          break;
        case "contains_all":
          Code.Entries _xifexpression = null;
          if ((!this.vector)) {
            _xifexpression = MongodbCommands.obj(path, MongodbCommands.obj("$all", this.operand(value, dates)));
          } else {
            Code.Entries _xifexpression_1 = null;
            if ((value instanceof ListLit)) {
              final Function1<Literal, Code.Expr> _function = (Literal item) -> {
                Code.Entries _obj = MongodbCommands.obj(path, MongodbCommands.obj("$eq", this.operand(item, dates)));
                return ((Code.Expr) _obj);
              };
              _xifexpression_1 = MongodbCommands.obj("$and", MongodbCommands.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(((ListLit) value).getItems(), _function))));
            } else {
              throw Unsupported.of("contains_all with a parameter in a vector search; $all is not a pre-filter operator, and the generator spells it as $and of $eq from a list literal");
            }
            _xifexpression = _xifexpression_1;
          }
          _switchResult = _xifexpression;
          break;
        case "prefix":
          Code.Entries _xblockexpression = null;
          {
            if (this.vector) {
              throw Unsupported.of(("prefix conditions in a vector search" + MongodbFilter.VECTOR_REASON));
            }
            Code.Entries _xifexpression_2 = null;
            if ((value instanceof StringLit)) {
              String _escaped = MongodbFilter.escaped(((StringLit) value).getValue());
              String _plus = ("^" + _escaped);
              _xifexpression_2 = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(_plus)));
            } else {
              _xifexpression_2 = MongodbCommands.obj("$expr", MongodbCommands.obj("$eq", MongodbCommands.arr(MongodbCommands.obj("$indexOfCP", MongodbCommands.arr(MongodbCommands.obj("$ifNull", MongodbCommands.arr(MongodbCommands.text(("$" + path)), MongodbCommands.text(""))), this.operand(value, false))), MongodbCommands.num(0))));
            }
            _xblockexpression = _xifexpression_2;
          }
          _switchResult = _xblockexpression;
          break;
        case "like":
          Code.Entries _xblockexpression_1 = null;
          {
            if (this.vector) {
              throw Unsupported.of(("like conditions in a vector search" + MongodbFilter.VECTOR_REASON));
            }
            _xblockexpression_1 = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(MongodbFilter.likePattern(this.constant(value, "like")))));
          }
          _switchResult = _xblockexpression_1;
          break;
        default:
          throw Unsupported.of(("the comparison " + op));
      }
    } else {
      throw Unsupported.of(("the comparison " + op));
    }
    return _switchResult;
  }

  /**
   * Text operators outside $search: regular expressions on the stored string; analyzed matching belongs to keyword search.
   */
  private Code.Entries textCondition(final String op, final String path, final Literal value) {
    Code.Entries _xblockexpression = null;
    {
      if (this.vector) {
        throw Unsupported.of(("text conditions in a vector search" + MongodbFilter.VECTOR_REASON));
      }
      Code.Entries _switchResult = null;
      if (op != null) {
        switch (op) {
          case "regex":
            _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(this.constant(value, "regex"))));
            break;
          case "wildcard":
            _switchResult = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(MongodbFilter.wildcardPattern(this.constant(value, "wildcard")))));
            break;
          case "match":
          case "match_all_terms":
          case "match_any_term":
          case "phrase":
          case "fuzzy":
            throw Unsupported.of((("the text operator " + op) + " in a filter; MQL compares stored values, and analyzed matching is the $search stage of a keyword search"));
          default:
            Code.Entries _xifexpression = null;
            if ((value instanceof StringLit)) {
              _xifexpression = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(MongodbFilter.escaped(((StringLit) value).getValue()))));
            } else {
              _xifexpression = MongodbCommands.obj("$expr", MongodbCommands.obj("$gte", MongodbCommands.arr(MongodbCommands.obj("$indexOfCP", MongodbCommands.arr(MongodbCommands.obj("$ifNull", MongodbCommands.arr(MongodbCommands.text(("$" + path)), MongodbCommands.text(""))), this.operand(value, false))), MongodbCommands.num(0))));
            }
            _switchResult = _xifexpression;
            break;
        }
      } else {
        Code.Entries _xifexpression = null;
        if ((value instanceof StringLit)) {
          _xifexpression = MongodbCommands.obj(path, MongodbCommands.obj("$regex", MongodbCommands.text(MongodbFilter.escaped(((StringLit) value).getValue()))));
        } else {
          _xifexpression = MongodbCommands.obj("$expr", MongodbCommands.obj("$gte", MongodbCommands.arr(MongodbCommands.obj("$indexOfCP", MongodbCommands.arr(MongodbCommands.obj("$ifNull", MongodbCommands.arr(MongodbCommands.text(("$" + path)), MongodbCommands.text(""))), this.operand(value, false))), MongodbCommands.num(0))));
        }
        _switchResult = _xifexpression;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Entries geo(final GeoPredicate g, final boolean element) {
    Code.Entries _xblockexpression = null;
    {
      if (this.vector) {
        throw Unsupported.of(("geo conditions in a vector search" + MongodbFilter.VECTOR_REASON));
      }
      final String path = this.resolve(g.getTarget(), element);
      GeoPoint _center = g.getCenter();
      boolean _tripleNotEquals = (_center != null);
      if (_tripleNotEquals) {
        double _switchResult = (double) 0;
        String _literal = g.getUnit().getLiteral();
        if (_literal != null) {
          switch (_literal) {
            case "km":
              _switchResult = g.getDistance();
              break;
            case "miles":
              double _distance = g.getDistance();
              _switchResult = (_distance * 1.609344);
              break;
            default:
              double _distance_1 = g.getDistance();
              _switchResult = (_distance_1 / 1000.0);
              break;
          }
        } else {
          double _distance_1 = g.getDistance();
          _switchResult = (_distance_1 / 1000.0);
        }
        final double kilometres = _switchResult;
        return MongodbCommands.obj(path, MongodbCommands.obj("$geoWithin", MongodbCommands.obj("$centerSphere", MongodbCommands.arr(this.point(g.getCenter()), MongodbCommands.num((kilometres / MongodbFilter.EARTH_RADIUS_KM))))));
      }
      final List<Code.Expr> ring = CollectionLiterals.<Code.Expr>newArrayList();
      GeoPoint _topLeft = g.getTopLeft();
      boolean _tripleNotEquals_1 = (_topLeft != null);
      if (_tripleNotEquals_1) {
        Code.Expr _point = this.point(g.getTopLeft());
        ring.add(_point);
        Code.Items _arr = MongodbCommands.arr(MongodbCommands.num(g.getBottomRight().getLongitude()), MongodbCommands.num(g.getTopLeft().getLatitude()));
        ring.add(_arr);
        Code.Expr _point_1 = this.point(g.getBottomRight());
        ring.add(_point_1);
        Code.Items _arr_1 = MongodbCommands.arr(MongodbCommands.num(g.getTopLeft().getLongitude()), MongodbCommands.num(g.getBottomRight().getLatitude()));
        ring.add(_arr_1);
        Code.Expr _point_2 = this.point(g.getTopLeft());
        ring.add(_point_2);
      } else {
        boolean _isEmpty = g.getVertices().isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          EList<GeoPoint> _vertices = g.getVertices();
          for (final GeoPoint vertex : _vertices) {
            Code.Expr _point_3 = this.point(vertex);
            ring.add(_point_3);
          }
          Code.Expr _point_4 = this.point(IterableExtensions.<GeoPoint>head(g.getVertices()));
          ring.add(_point_4);
        } else {
          throw Unsupported.of("WKT geo relations; MQL takes GeoJSON geometries ($geoWithin, $geoIntersects), and the generator does not parse WKT");
        }
      }
      _xblockexpression = MongodbCommands.obj(path, MongodbCommands.obj("$geoWithin", MongodbCommands.obj("$geometry", MongodbCommands.obj("type", MongodbCommands.text("Polygon"), "coordinates", MongodbCommands.arr(MongodbCommands.arr(ring))))));
    }
    return _xblockexpression;
  }

  private Code.Expr point(final GeoPoint p) {
    return MongodbCommands.arr(MongodbCommands.num(p.getLongitude()), MongodbCommands.num(p.getLatitude()));
  }

  /**
   * 'any field (condition)': $elemMatch with the condition's paths relative to the array element.
   */
  private Code.Entries elementQuery(final ElementPredicate e, final boolean element) {
    Code.Entries _xblockexpression = null;
    {
      if (this.vector) {
        throw Unsupported.of(("element predicates (\'any\') in a vector search" + MongodbFilter.VECTOR_REASON));
      }
      final String path = this.resolve(e.getTarget(), element);
      _xblockexpression = MongodbCommands.obj(path, MongodbCommands.obj("$elemMatch", this.query(e.getCondition(), true)));
    }
    return _xblockexpression;
  }

  /**
   * A filter value; strings compared with date fields become extended JSON dates, which the runtimes send as BSON dates.
   */
  private Code.Expr operand(final Literal literal, final boolean dates) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (literal instanceof SparseLit) {
      _matched=true;
      throw Unsupported.of("sparse vectors in a filter");
    }
    if (!_matched) {
      if (literal instanceof EnvRef) {
        _matched=true;
        throw Unsupported.of("environment references in a filter");
      }
    }
    if (!_matched) {
      if (literal instanceof StringLit) {
        _matched=true;
        Code.Expr _xifexpression = null;
        if (dates) {
          _xifexpression = MongodbCommands.obj("$date", MongodbCommands.text(((StringLit)literal).getValue()));
        } else {
          _xifexpression = Literals.of(literal);
        }
        _switchResult = _xifexpression;
      }
    }
    if (!_matched) {
      if (literal instanceof ListLit) {
        _matched=true;
        Code.Expr _xifexpression = null;
        if (dates) {
          final Function1<Literal, Code.Expr> _function = (Literal item) -> {
            return this.operand(item, true);
          };
          _xifexpression = MongodbCommands.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(((ListLit)literal).getItems(), _function)));
        } else {
          _xifexpression = Literals.of(literal);
        }
        _switchResult = _xifexpression;
      }
    }
    if (!_matched) {
      _switchResult = Literals.of(literal);
    }
    return _switchResult;
  }

  private String constant(final Literal literal, final String op) {
    String _xblockexpression = null;
    {
      if ((!(literal instanceof StringLit))) {
        throw Unsupported.of((("the " + op) + " operator with a value that is not a string literal; its pattern is built from the literal"));
      }
      _xblockexpression = ((StringLit) literal).getValue();
    }
    return _xblockexpression;
  }

  /**
   * A regular expression that matches the text literally.
   */
  public static String escaped(final String value) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder();
      char[] _charArray = value.toCharArray();
      for (final char c : _charArray) {
        {
          int _indexOf = "\\^$.|?*+()[]{}/-".indexOf(c);
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

  /**
   * A SQL LIKE pattern as an anchored regular expression: % is .*, _ is ., everything else literal.
   */
  public static String likePattern(final String like) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder("^");
      char[] _charArray = like.toCharArray();
      for (final char c : _charArray) {
        String _valueOf = String.valueOf(c);
        if (_valueOf != null) {
          switch (_valueOf) {
            case "%":
              out.append(".*");
              break;
            case "_":
              out.append(".");
              break;
            default:
              out.append(MongodbFilter.escaped(String.valueOf(c)));
              break;
          }
        } else {
          out.append(MongodbFilter.escaped(String.valueOf(c)));
        }
      }
      _xblockexpression = out.append("$").toString();
    }
    return _xblockexpression;
  }

  /**
   * A wildcard pattern (* any characters, ? one character) as an anchored regular expression.
   */
  public static String wildcardPattern(final String wildcard) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder("^");
      char[] _charArray = wildcard.toCharArray();
      for (final char c : _charArray) {
        String _valueOf = String.valueOf(c);
        if (_valueOf != null) {
          switch (_valueOf) {
            case "*":
              out.append(".*");
              break;
            case "?":
              out.append(".");
              break;
            default:
              out.append(MongodbFilter.escaped(String.valueOf(c)));
              break;
          }
        } else {
          out.append(MongodbFilter.escaped(String.valueOf(c)));
        }
      }
      _xblockexpression = out.append("$").toString();
    }
    return _xblockexpression;
  }
}
