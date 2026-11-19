package org.ftn.vecDSL.generator.elasticsearch;

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
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;

/**
 * Translates a vecDSL filter into the Elasticsearch query DSL, for the filter context of a bool query or of a kNN search.
 * Comparisons use term, terms, range, exists, prefix, wildcard and regexp queries on the exact form of a field (the keyword
 * sub-field of a full-text field); text operators use match and match_phrase on the analyzed field; geo predicates use
 * geo_distance, geo_bounding_box and geo_shape; element predicates and paths into nested fields use nested queries. A
 * negation is a must_not clause, so documents without the field match it, as the other vecDSL targets treat records without
 * the field. Every value is a JSON value of the query.
 */
@SuppressWarnings("all")
public class ElasticsearchFilter {
  private final ElasticsearchIndex index;

  public ElasticsearchFilter(final ElasticsearchIndex index) {
    this.index = index;
  }

  public Code.Entries query(final Condition node) {
    return this.query(node, null);
  }

  private Code.Entries query(final Condition node, final String element) {
    Code.Entries _switchResult = null;
    boolean _matched = false;
    if (node instanceof AndCondition) {
      _matched=true;
      _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("filter", ElasticsearchBodies.arr(this.query(((AndCondition)node).getLeft(), element), this.query(((AndCondition)node).getRight(), element))));
    }
    if (!_matched) {
      if (node instanceof OrCondition) {
        _matched=true;
        _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("should", ElasticsearchBodies.arr(this.query(((OrCondition)node).getLeft(), element), this.query(((OrCondition)node).getRight(), element)), "minimum_should_match", ElasticsearchBodies.num(1)));
      }
    }
    if (!_matched) {
      if (node instanceof NotCondition) {
        _matched=true;
        _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(this.query(((NotCondition)node).getOperand(), element))));
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
   * A field path as Elasticsearch names it (dotted), with the exact (keyword) form and the nested field it lies in.
   */
  private List<String> resolve(final FieldPath path, final String element) {
    List<String> _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        ElasticsearchBodies.field(path.getRawPath());
        String _rawPath_1 = path.getRawPath();
        String _rawPath_2 = path.getRawPath();
        return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_rawPath_1, _rawPath_2, (String)null));
      }
      final List<String> segments = CollectionLiterals.<String>newArrayList();
      String _fieldName = ModelUtil.fieldName(path.getField());
      segments.add(_fieldName);
      final Function1<Field, String> _function = (Field s) -> {
        return ModelUtil.fieldName(s);
      };
      List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
      Iterables.<String>addAll(segments, _map);
      final Field leaf = ModelUtil.leaf(path);
      final boolean fullText = ((leaf instanceof PayloadField) && (((PayloadField) leaf).isFullText() || (((PayloadField) leaf).getAnalyzer() != null)));
      if ((element != null)) {
        String _join = IterableExtensions.join(segments, ".");
        final String dotted = ((element + ".") + _join);
        String _xifexpression = null;
        if (fullText) {
          _xifexpression = ((dotted + ".") + ElasticsearchBodies.RAW);
        } else {
          _xifexpression = dotted;
        }
        return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(dotted, _xifexpression, (String)null));
      }
      final ElasticsearchIndex.Field top = this.index.field(IterableExtensions.<String>head(segments));
      String _name = top.name();
      Iterable<String> _tail = IterableExtensions.<String>tail(segments);
      final String name = IterableExtensions.join(Iterables.<String>concat(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_name)), _tail), ".");
      String _xifexpression_1 = null;
      if (fullText) {
        _xifexpression_1 = ((name + ".") + ElasticsearchBodies.RAW);
      } else {
        _xifexpression_1 = name;
      }
      String _xifexpression_2 = null;
      if ((top.nested() && (segments.size() > 1))) {
        _xifexpression_2 = top.name();
      } else {
        _xifexpression_2 = null;
      }
      _xblockexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name, _xifexpression_1, _xifexpression_2));
    }
    return _xblockexpression;
  }

  private Code.Entries nested(final String path, final Code.Entries inner) {
    Code.Entries _xifexpression = null;
    if ((path == null)) {
      _xifexpression = inner;
    } else {
      _xifexpression = ElasticsearchBodies.obj("nested", ElasticsearchBodies.obj("path", ElasticsearchBodies.text(path), "query", inner));
    }
    return _xifexpression;
  }

  private Code.Entries predicate(final Predicate p, final String element) {
    Code.Entries _xblockexpression = null;
    {
      final List<String> names = this.resolve(p.getTarget(), element);
      final String name = names.get(0);
      final String exact = names.get(1);
      String _get = names.get(2);
      Code.Entries _xifexpression = null;
      Literal _text = p.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        _xifexpression = this.textQuery(p.getTextOp().getLiteral(), name, exact, p.getText());
      } else {
        Code.Entries _xifexpression_1 = null;
        Literal _lower = p.getLower();
        boolean _tripleNotEquals_1 = (_lower != null);
        if (_tripleNotEquals_1) {
          _xifexpression_1 = ElasticsearchBodies.obj("range", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("gte", this.operand(p.getLower()), "lte", this.operand(p.getUpper()))));
        } else {
          Code.Entries _xifexpression_2 = null;
          boolean _written = ModelUtil.written(p, "lengthOp");
          if (_written) {
            _xifexpression_2 = ElasticsearchBodies.obj("script", ElasticsearchBodies.obj("script", ElasticsearchBodies.script(ElasticsearchBodies.LENGTH_SCRIPT, ElasticsearchBodies.obj("field", ElasticsearchBodies.text(exact), "op", ElasticsearchBodies.text(p.getLengthOp().getLiteral()), "value", ElasticsearchBodies.num(p.getLength())))));
          } else {
            Code.Entries _xifexpression_3 = null;
            Literal _value = p.getValue();
            boolean _tripleEquals = (_value == null);
            if (_tripleEquals) {
              Code.Entries _switchResult = null;
              String _literal = p.getUnaryOp().getLiteral();
              if (_literal != null) {
                switch (_literal) {
                  case "exists":
                  case "is_not_null":
                    _switchResult = ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(exact)));
                    break;
                  default:
                    _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(exact))))));
                    break;
                }
              } else {
                _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(exact))))));
              }
              _xifexpression_3 = _switchResult;
            } else {
              Code.Entries _xblockexpression_1 = null;
              {
                Literal _value_1 = p.getValue();
                if ((_value_1 instanceof NullLit)) {
                  throw Unsupported.of("comparisons with null; write is_null or is_not_null");
                }
                _xblockexpression_1 = this.comparison(p.getOp().getLiteral(), exact, p.getValue());
              }
              _xifexpression_3 = _xblockexpression_1;
            }
            _xifexpression_2 = _xifexpression_3;
          }
          _xifexpression_1 = _xifexpression_2;
        }
        _xifexpression = _xifexpression_1;
      }
      _xblockexpression = this.nested(_get, _xifexpression);
    }
    return _xblockexpression;
  }

  private Code.Entries comparison(final String op, final String exact, final Literal value) {
    Code.Entries _switchResult = null;
    if (op != null) {
      switch (op) {
        case "eq":
        case "contains":
          _switchResult = ElasticsearchBodies.obj("term", ElasticsearchBodies.obj(exact, this.operand(value)));
          break;
        case "neq":
          _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("term", ElasticsearchBodies.obj(exact, this.operand(value))))));
          break;
        case "gt":
        case "gte":
        case "lt":
        case "lte":
          _switchResult = ElasticsearchBodies.obj("range", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj(op, this.operand(value))));
          break;
        case "in":
        case "contains_any":
          _switchResult = ElasticsearchBodies.obj("terms", ElasticsearchBodies.obj(exact, this.operand(value)));
          break;
        case "not_in":
          _switchResult = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("terms", ElasticsearchBodies.obj(exact, this.operand(value))))));
          break;
        case "contains_all":
          _switchResult = ElasticsearchBodies.obj("terms_set", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("terms", this.operand(value), "minimum_should_match_script", ElasticsearchBodies.obj("source", ElasticsearchBodies.text("params.num_terms")))));
          break;
        case "prefix":
          _switchResult = ElasticsearchBodies.obj("prefix", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", this.operand(value))));
          break;
        case "like":
          _switchResult = ElasticsearchBodies.obj("wildcard", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", ElasticsearchBodies.text(ElasticsearchFilter.wildcard(this.constant(value, "like"))))));
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
   * Text operators: match queries on the analyzed field, wildcard and regexp on the exact form.
   */
  private Code.Entries textQuery(final String op, final String name, final String exact, final Literal value) {
    Code.Entries _switchResult = null;
    if (op != null) {
      switch (op) {
        case "match":
          _switchResult = ElasticsearchBodies.obj("match", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value))));
          break;
        case "match_all_terms":
          _switchResult = ElasticsearchBodies.obj("match", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value), "operator", ElasticsearchBodies.text("and"))));
          break;
        case "match_any_term":
          _switchResult = ElasticsearchBodies.obj("match", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value), "operator", ElasticsearchBodies.text("or"))));
          break;
        case "phrase":
          _switchResult = ElasticsearchBodies.obj("match_phrase", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value))));
          break;
        case "fuzzy":
          _switchResult = ElasticsearchBodies.obj("match", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value), "fuzziness", ElasticsearchBodies.text("AUTO"))));
          break;
        case "wildcard":
          _switchResult = ElasticsearchBodies.obj("wildcard", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", this.operand(value))));
          break;
        case "regex":
          _switchResult = ElasticsearchBodies.obj("regexp", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", this.operand(value))));
          break;
        default:
          Code.Entries _xifexpression = null;
          boolean _notEquals = (!Objects.equals(name, exact));
          if (_notEquals) {
            _xifexpression = ElasticsearchBodies.obj("match_phrase", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value))));
          } else {
            String _escaped = ElasticsearchFilter.escaped(this.constant(value, "contains_text"));
            String _plus = ("*" + _escaped);
            String _plus_1 = (_plus + "*");
            _xifexpression = ElasticsearchBodies.obj("wildcard", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", ElasticsearchBodies.text(_plus_1))));
          }
          _switchResult = _xifexpression;
          break;
      }
    } else {
      Code.Entries _xifexpression = null;
      boolean _notEquals = (!Objects.equals(name, exact));
      if (_notEquals) {
        _xifexpression = ElasticsearchBodies.obj("match_phrase", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("query", this.operand(value))));
      } else {
        String _escaped = ElasticsearchFilter.escaped(this.constant(value, "contains_text"));
        String _plus = ("*" + _escaped);
        String _plus_1 = (_plus + "*");
        _xifexpression = ElasticsearchBodies.obj("wildcard", ElasticsearchBodies.obj(exact, ElasticsearchBodies.obj("value", ElasticsearchBodies.text(_plus_1))));
      }
      _switchResult = _xifexpression;
    }
    return _switchResult;
  }

  private Code.Entries geo(final GeoPredicate g, final String element) {
    Code.Entries _xblockexpression = null;
    {
      final List<String> names = this.resolve(g.getTarget(), element);
      final String name = names.get(0);
      String _get = names.get(2);
      Code.Entries _xifexpression = null;
      GeoPoint _center = g.getCenter();
      boolean _tripleNotEquals = (_center != null);
      if (_tripleNotEquals) {
        Code.Entries _xblockexpression_1 = null;
        {
          String _switchResult = null;
          String _literal = g.getUnit().getLiteral();
          if (_literal != null) {
            switch (_literal) {
              case "km":
                _switchResult = "km";
                break;
              case "miles":
                _switchResult = "mi";
                break;
              default:
                _switchResult = "m";
                break;
            }
          } else {
            _switchResult = "m";
          }
          final String unit = _switchResult;
          String _number = ElasticsearchFilter.number(g.getDistance());
          String _plus = (_number + unit);
          _xblockexpression_1 = ElasticsearchBodies.obj("geo_distance", ElasticsearchBodies.obj("distance", ElasticsearchBodies.text(_plus), name, this.point(g.getCenter())));
        }
        _xifexpression = _xblockexpression_1;
      } else {
        Code.Entries _xifexpression_1 = null;
        GeoPoint _topLeft = g.getTopLeft();
        boolean _tripleNotEquals_1 = (_topLeft != null);
        if (_tripleNotEquals_1) {
          _xifexpression_1 = ElasticsearchBodies.obj("geo_bounding_box", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("top_left", this.point(g.getTopLeft()), "bottom_right", this.point(g.getBottomRight()))));
        } else {
          Code.Entries _xifexpression_2 = null;
          boolean _isEmpty = g.getVertices().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            Code.Entries _xblockexpression_2 = null;
            {
              final List<Code.Expr> ring = CollectionLiterals.<Code.Expr>newArrayList();
              EList<GeoPoint> _vertices = g.getVertices();
              for (final GeoPoint vertex : _vertices) {
                Code.Items _arr = ElasticsearchBodies.arr(ElasticsearchBodies.num(vertex.getLongitude()), ElasticsearchBodies.num(vertex.getLatitude()));
                ring.add(_arr);
              }
              Code.Items _arr_1 = ElasticsearchBodies.arr(ElasticsearchBodies.num(IterableExtensions.<GeoPoint>head(g.getVertices()).getLongitude()), ElasticsearchBodies.num(IterableExtensions.<GeoPoint>head(g.getVertices()).getLatitude()));
              ring.add(_arr_1);
              _xblockexpression_2 = ElasticsearchBodies.obj("geo_shape", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("shape", ElasticsearchBodies.obj("type", ElasticsearchBodies.text("polygon"), "coordinates", ElasticsearchBodies.arr(ElasticsearchBodies.arr(ring))), "relation", ElasticsearchBodies.text("within"))));
            }
            _xifexpression_2 = _xblockexpression_2;
          } else {
            _xifexpression_2 = ElasticsearchBodies.obj("geo_shape", ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("shape", ElasticsearchBodies.text(g.getWkt()), "relation", ElasticsearchBodies.text(g.getRelation().getLiteral()))));
          }
          _xifexpression_1 = _xifexpression_2;
        }
        _xifexpression = _xifexpression_1;
      }
      _xblockexpression = this.nested(_get, _xifexpression);
    }
    return _xblockexpression;
  }

  private Code.Entries point(final GeoPoint p) {
    return ElasticsearchBodies.obj("lat", ElasticsearchBodies.num(p.getLatitude()), "lon", ElasticsearchBodies.num(p.getLongitude()));
  }

  /**
   * 'any field (condition)': a nested query on a nested field, with the condition's paths below it.
   */
  private Code.Entries elementQuery(final ElementPredicate e, final String element) {
    Code.Entries _xblockexpression = null;
    {
      final List<String> names = this.resolve(e.getTarget(), element);
      final Field leaf = ModelUtil.leaf(e.getTarget());
      if ((((element == null) && (e.getTarget().getRawPath() == null)) && (!((leaf instanceof PayloadField) && ((PayloadField) leaf).isNested())))) {
        String _get = names.get(0);
        String _plus = ("element predicates (\'any\') on " + _get);
        String _plus_1 = (_plus + ", which is not a nested field; Elasticsearch matches the conditions of one array element only in nested fields");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = ElasticsearchBodies.obj("nested", ElasticsearchBodies.obj("path", ElasticsearchBodies.text(names.get(0)), "query", this.query(e.getCondition(), names.get(0))));
    }
    return _xblockexpression;
  }

  private Code.Expr operand(final Literal literal) {
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
   * A SQL LIKE pattern as an Elasticsearch wildcard pattern: % is *, _ is ?, and literal * ? \ are escaped.
   */
  public static String wildcard(final String like) {
    String _xblockexpression = null;
    {
      final StringBuilder out = new StringBuilder();
      char[] _charArray = like.toCharArray();
      for (final char c : _charArray) {
        String _valueOf = String.valueOf(c);
        if (_valueOf != null) {
          switch (_valueOf) {
            case "%":
              out.append("*");
              break;
            case "_":
              out.append("?");
              break;
            case "*":
            case "?":
            case "\\":
              out.append("\\").append(c);
              break;
            default:
              out.append(c);
              break;
          }
        } else {
          out.append(c);
        }
      }
      _xblockexpression = out.toString();
    }
    return _xblockexpression;
  }

  public static String escaped(final String value) {
    return value.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
  }

  public static String number(final double value) {
    String _xifexpression = null;
    if (((value == Math.rint(value)) && (Math.abs(value) < 1e15))) {
      _xifexpression = String.valueOf(((long) value));
    } else {
      _xifexpression = String.valueOf(value);
    }
    return _xifexpression;
  }
}
