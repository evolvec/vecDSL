package org.ftn.vecDSL.generator.vespa;

import java.util.Collections;
import java.util.List;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.vespa.VespaQueries;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Translates a vecDSL filter into VespaQueries conditions on the fields of the collection's schema. Every value becomes an @parameter
 * of the YQL. A negation matches the documents where the negated condition does not hold, so documents without the field match it,
 * as on the other vecDSL targets.
 */
@SuppressWarnings("all")
public class VespaFilter {
  /**
   * Why full-text fields have no fuzzy match in YQL.
   */
  public static final String NO_FUZZY = "the fuzzy operator matches string attributes (single, array or weighted set), not the terms of an index field, so full-text fields have no fuzzy match";

  private final VespaSchema schema;

  private final VectorCollection collection;

  private final VespaPackage.Schema current;

  private final List<VespaQueries.Cond> collected = CollectionLiterals.<VespaQueries.Cond>newArrayList();

  public VespaFilter(final VespaSchema schema, final VectorCollection collection) {
    this.schema = schema;
    this.collection = collection;
    this.current = schema.schema(collection);
  }

  public VespaFilter where(final FilterBlock block) {
    VespaFilter _xblockexpression = null;
    {
      if ((block != null)) {
        VespaQueries.Cond _condition = this.condition(block.getCondition());
        this.collected.add(_condition);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * A slice condition: the hidden attribute (tenant, namespace or partition) equals one of the values.
   */
  public VespaFilter slice(final String attribute, final List<Code.Expr> values) {
    VespaFilter _xblockexpression = null;
    {
      boolean _has = this.current.has(attribute);
      boolean _not = (!_has);
      if (_not) {
        String _name = this.collection.getName();
        String _plus = ("a slice of collection " + _name);
        String _plus_1 = (_plus + ", which declares no ");
        String _substring = attribute.substring(7);
        String _plus_2 = (_plus_1 + _substring);
        throw Unsupported.of(_plus_2);
      }
      VespaQueries.Test _xifexpression = null;
      int _size = values.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        Code.Expr _head = IterableExtensions.<Code.Expr>head(values);
        _xifexpression = new VespaQueries.Test(attribute, "STRING", "eq", _head, null);
      } else {
        Code.Items _items = Code.items(values);
        _xifexpression = new VespaQueries.Test(attribute, "STRING", "in", _items, null);
      }
      this.collected.add(_xifexpression);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public VespaFilter add(final VespaQueries.Cond extra) {
    VespaFilter _xblockexpression = null;
    {
      this.collected.add(extra);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public List<VespaQueries.Cond> parts() {
    return this.collected;
  }

  public VespaQueries.Cond condition(final Condition node) {
    VespaQueries.Cond _switchResult = null;
    boolean _matched = false;
    if (node instanceof AndCondition) {
      _matched=true;
      VespaQueries.Cond _condition = this.condition(((AndCondition)node).getLeft());
      VespaQueries.Cond _condition_1 = this.condition(((AndCondition)node).getRight());
      _switchResult = new VespaQueries.And(Collections.<VespaQueries.Cond>unmodifiableList(CollectionLiterals.<VespaQueries.Cond>newArrayList(_condition, _condition_1)));
    }
    if (!_matched) {
      if (node instanceof OrCondition) {
        _matched=true;
        VespaQueries.Cond _condition = this.condition(((OrCondition)node).getLeft());
        VespaQueries.Cond _condition_1 = this.condition(((OrCondition)node).getRight());
        _switchResult = new VespaQueries.Or(Collections.<VespaQueries.Cond>unmodifiableList(CollectionLiterals.<VespaQueries.Cond>newArrayList(_condition, _condition_1)));
      }
    }
    if (!_matched) {
      if (node instanceof NotCondition) {
        _matched=true;
        VespaQueries.Cond _condition = this.condition(((NotCondition)node).getOperand());
        _switchResult = new VespaQueries.Not(_condition);
      }
    }
    if (!_matched) {
      if (node instanceof Predicate) {
        _matched=true;
        _switchResult = this.test(((Predicate)node));
      }
    }
    if (!_matched) {
      if (node instanceof GeoPredicate) {
        _matched=true;
        throw Unsupported.of("geo predicates; has no Vespa mapping yet (position fields and the geoLocation operator)");
      }
    }
    if (!_matched) {
      if (node instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of("element predicates (\'any\'); has no Vespa mapping yet (sameElement over array<struct> fields)");
      }
    }
    if (!_matched) {
      throw Unsupported.of(node.eClass().getName());
    }
    return _switchResult;
  }

  private VespaQueries.Cond test(final Predicate p) {
    VespaQueries.Cond _xblockexpression = null;
    {
      final VespaPackage.Field target = this.field(p.getTarget());
      Literal _text = p.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        return this.textTest(target, p);
      }
      Literal _lower = p.getLower();
      boolean _tripleNotEquals_1 = (_lower != null);
      if (_tripleNotEquals_1) {
        String _name = target.name();
        String _kind = target.kind();
        Code.Expr _operand = this.operand(p.getLower());
        Code.Expr _operand_1 = this.operand(p.getUpper());
        return new VespaQueries.Test(_name, _kind, "between", _operand, _operand_1);
      }
      boolean _written = ModelUtil.written(p, "lengthOp");
      if (_written) {
        throw Unsupported.of("length predicates; YQL has no operator on the number of values of a field");
      }
      Literal _value = p.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        String _literal = p.getUnaryOp().getLiteral();
        String _plus = ("the " + _literal);
        String _plus_1 = (_plus + " test; YQL has no test for a missing value (YqlParser converts no IS_NULL or IS_NOT_NULL node)");
        throw Unsupported.of(_plus_1);
      }
      Literal _value_1 = p.getValue();
      if ((_value_1 instanceof NullLit)) {
        throw Unsupported.of("comparisons with null; write is_null or is_not_null");
      }
      final String op = p.getOp().getLiteral();
      VespaQueries.Cond _switchResult = null;
      if (op != null) {
        switch (op) {
          case "like":
            _switchResult = this.like(target, p.getValue());
            break;
          case "contains":
          case "contains_any":
          case "contains_all":
            VespaQueries.Test _xblockexpression_1 = null;
            {
              boolean _array = target.array();
              boolean _not = (!_array);
              if (_not) {
                String _name_1 = target.name();
                String _plus_2 = ((op + " on ") + _name_1);
                String _plus_3 = (_plus_2 + ", which is not an array field");
                throw Unsupported.of(_plus_3);
              }
              String _name_2 = this.exact(target).name();
              String _kind_1 = this.exact(target).kind();
              Code.Expr _operand_2 = this.operand(p.getValue());
              _xblockexpression_1 = new VespaQueries.Test(_name_2, _kind_1, op, _operand_2, null);
            }
            _switchResult = _xblockexpression_1;
            break;
          default:
            String _name_1 = this.exact(target).name();
            String _kind_1 = this.exact(target).kind();
            Code.Expr _operand_2 = this.operand(p.getValue());
            _switchResult = new VespaQueries.Test(_name_1, _kind_1, op, _operand_2, null);
            break;
        }
      } else {
        String _name_1 = this.exact(target).name();
        String _kind_1 = this.exact(target).kind();
        Code.Expr _operand_2 = this.operand(p.getValue());
        _switchResult = new VespaQueries.Test(_name_1, _kind_1, op, _operand_2, null);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Text operators on full-text fields: all terms, any term or a phrase of the user text, which is never parsed as query syntax.
   */
  private VespaQueries.Cond textTest(final VespaPackage.Field target, final Predicate p) {
    VespaQueries.Test _xblockexpression = null;
    {
      boolean _text = target.text();
      boolean _not = (!_text);
      if (_not) {
        String _literal = p.getTextOp().getLiteral();
        String _plus = ("the " + _literal);
        String _plus_1 = (_plus + " text operator on ");
        String _name = target.name();
        String _plus_2 = (_plus_1 + _name);
        String _plus_3 = (_plus_2 + ", which is not a full-text field");
        throw Unsupported.of(_plus_3);
      }
      String _switchResult = null;
      String _literal_1 = p.getTextOp().getLiteral();
      if (_literal_1 != null) {
        switch (_literal_1) {
          case "match":
          case "match_all_terms":
            _switchResult = "text_all";
            break;
          case "match_any_term":
            _switchResult = "text_any";
            break;
          case "phrase":
            _switchResult = "text_phrase";
            break;
          case "regex":
            throw Unsupported.of("the regex text operator; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)");
          case "fuzzy":
            throw Unsupported.of(("the fuzzy text operator; " + VespaFilter.NO_FUZZY));
          default:
            String _literal_2 = p.getTextOp().getLiteral();
            String _plus_4 = ("the " + _literal_2);
            String _plus_5 = (_plus_4 + " text operator; has no Vespa mapping yet (the prefix and substring term annotations)");
            throw Unsupported.of(_plus_5);
        }
      } else {
        String _literal_2 = p.getTextOp().getLiteral();
        String _plus_4 = ("the " + _literal_2);
        String _plus_5 = (_plus_4 + " text operator; has no Vespa mapping yet (the prefix and substring term annotations)");
        throw Unsupported.of(_plus_5);
      }
      final String op = _switchResult;
      String _name_1 = target.name();
      String _kind = target.kind();
      Code.Expr _operand = this.operand(p.getText());
      _xblockexpression = new VespaQueries.Test(_name_1, _kind, op, _operand, null);
    }
    return _xblockexpression;
  }

  /**
   * like with one trailing %: a prefix match on an exact string field; other patterns are not generated.
   */
  private VespaQueries.Cond like(final VespaPackage.Field target, final Literal value) {
    VespaQueries.Test _xblockexpression = null;
    {
      if ((!(value instanceof StringLit))) {
        throw Unsupported.of("like with a pattern from a parameter; the generator reads the pattern at generation time");
      }
      final String pattern = ((StringLit) value).getValue();
      int _length = pattern.length();
      int _minus = (_length - 1);
      final String stem = pattern.substring(0, _minus);
      if ((((!pattern.endsWith("%")) || stem.contains("%")) || stem.contains("_"))) {
        throw Unsupported.of((("the like pattern " + pattern) + "; has no Vespa mapping yet (the suffix and substring term annotations)"));
      }
      String _name = this.exact(target).name();
      String _kind = this.exact(target).kind();
      Code.Text _text = Code.text(stem);
      _xblockexpression = new VespaQueries.Test(_name, _kind, "prefix", _text, null);
    }
    return _xblockexpression;
  }

  /**
   * The exact attribute of a field: the field itself, or the vecdsl_exact_ companion of a full-text field.
   */
  private VespaPackage.Field exact(final VespaPackage.Field target) {
    VespaPackage.Field _xifexpression = null;
    boolean _text = target.text();
    if (_text) {
      String _name = target.name();
      String _plus = (VespaPackage.EXACT + _name);
      _xifexpression = this.current.field(_plus);
    } else {
      _xifexpression = target;
    }
    return _xifexpression;
  }

  /**
   * The schema field of a path: a declared field, as named after earlier renames.
   */
  public VespaPackage.Field field(final FieldPath path) {
    VespaPackage.Field _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("raw paths; the generator filters declared fields");
      }
      boolean _isEmpty = path.getSubFields().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("sub-field paths; has no Vespa mapping yet (struct-field attributes)");
      }
      final String named = this.schema.name(this.collection, path.getField());
      boolean _has = this.current.has(named);
      boolean _not_1 = (!_has);
      if (_not_1) {
        String _name = this.collection.getName();
        String _plus = ((("field " + named) + ", which collection ") + _name);
        String _plus_1 = (_plus + " does not have at this statement");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = this.current.field(named);
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
}
