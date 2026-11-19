package org.ftn.vecDSL.generator.lancedb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
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
import org.ftn.vecDSL.vecDSL.StringLit;

/**
 * Builds a LanceDB where string (SQL parsed by Lance with DataFusion) from a vecDSL condition. Identifiers are quoted with
 * backquotes and literals by LancedbSql; program parameters become placeholders the runtime fills with the same quoting.
 * Comparisons follow SQL's three-valued logic, so a negation keeps rows whose value is null out, as the other SQL target does.
 */
@SuppressWarnings("all")
public class LancedbFilter {
  private static final List<EScalarDType> ORDERED = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final List<EScalarDType> TEXTUAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private final LancedbSql sql;

  private final Function1<? super Field, ? extends String> naming;

  /**
   * A filter whose placeholders start with the prefix; naming gives the current column of a field (after renames).
   */
  public LancedbFilter(final String prefix, final Function1<? super Field, ? extends String> naming) {
    LancedbSql _lancedbSql = new LancedbSql(prefix);
    this.sql = _lancedbSql;
    this.naming = naming;
  }

  public LancedbFilter where(final FilterBlock block) {
    LancedbFilter _xblockexpression = null;
    {
      if ((block != null)) {
        this.sql.where(this.render(block.getCondition()));
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * Adds a condition written by the generator, for example a tenant's column test.
   */
  public LancedbFilter where(final String condition) {
    LancedbFilter _xblockexpression = null;
    {
      this.sql.where(condition);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * Adds the test column = value with the value quoted (a literal) or bound (a parameter).
   */
  public LancedbFilter equal(final String column, final Code.Expr value) {
    LancedbFilter _xblockexpression = null;
    {
      String _id = LancedbSql.id(column);
      String _plus = (_id + " = ");
      String _value = this.sql.value(value, false);
      String _plus_1 = (_plus + _value);
      this.sql.where(_plus_1);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * Adds the test column IN (values) for the ids of a statement.
   */
  public LancedbFilter keys(final String column, final List<Code.Expr> values) {
    LancedbFilter _xblockexpression = null;
    {
      String _id = LancedbSql.id(column);
      String _plus = (_id + " IN ");
      String _value = this.sql.value(Code.items(values), true);
      String _plus_1 = (_plus + _value);
      this.sql.where(_plus_1);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public boolean isEmpty() {
    return this.sql.isEmpty();
  }

  /**
   * The where argument: a string, or a template with parameter placeholders; null without conditions.
   */
  public Code.Expr code() {
    return this.sql.code();
  }

  private String render(final Condition condition) {
    String _switchResult = null;
    boolean _matched = false;
    if (condition instanceof AndCondition) {
      _matched=true;
      String _render = this.render(((AndCondition)condition).getLeft());
      String _plus = ("(" + _render);
      String _plus_1 = (_plus + " AND ");
      String _render_1 = this.render(((AndCondition)condition).getRight());
      String _plus_2 = (_plus_1 + _render_1);
      _switchResult = (_plus_2 + ")");
    }
    if (!_matched) {
      if (condition instanceof OrCondition) {
        _matched=true;
        String _render = this.render(((OrCondition)condition).getLeft());
        String _plus = ("(" + _render);
        String _plus_1 = (_plus + " OR ");
        String _render_1 = this.render(((OrCondition)condition).getRight());
        String _plus_2 = (_plus_1 + _render_1);
        _switchResult = (_plus_2 + ")");
      }
    }
    if (!_matched) {
      if (condition instanceof NotCondition) {
        _matched=true;
        String _render = this.render(((NotCondition)condition).getOperand());
        String _plus = ("NOT (" + _render);
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
        throw Unsupported.of((("geo predicates" + LancedbStatements.PENDING) + " (GeoArrow types and the R-tree index)"));
      }
    }
    if (!_matched) {
      if (condition instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of("element predicates (\'any\'); a LanceDB where string has no quantifier over list elements");
      }
    }
    if (!_matched) {
      throw Unsupported.of(condition.eClass().getName());
    }
    return _switchResult;
  }

  private String column(final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("raw field paths; Lance columns are declared in the table schema");
      }
      final ArrayList<String> names = CollectionLiterals.<String>newArrayList(this.naming.apply(path.getField()));
      EList<Field> _subFields = path.getSubFields();
      for (final Field sub : _subFields) {
        String _fieldName = ModelUtil.fieldName(sub);
        names.add(_fieldName);
      }
      _xblockexpression = LancedbSql.id(IterableExtensions.join(names, "."));
    }
    return _xblockexpression;
  }

  private EScalarDType type(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      final Field field = ModelUtil.leaf(path);
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

  private EScalarDType elementType(final FieldPath path) {
    EScalarDType _xblockexpression = null;
    {
      final Field field = ModelUtil.leaf(path);
      EScalarDType _xifexpression = null;
      if ((field instanceof PayloadField)) {
        _xifexpression = ((PayloadField)field).getElementType();
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private String predicate(final Predicate test) {
    String _xblockexpression = null;
    {
      final FieldPath target = test.getTarget();
      final String name = this.column(target);
      Literal _text = test.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        return this.textPredicate(test, name);
      }
      Literal _lower = test.getLower();
      boolean _tripleNotEquals_1 = (_lower != null);
      if (_tripleNotEquals_1) {
        this.ordered(target);
        String _operand = this.operand(test.getLower());
        String _plus = ((name + " BETWEEN ") + _operand);
        String _plus_1 = (_plus + " AND ");
        String _operand_1 = this.operand(test.getUpper());
        return (_plus_1 + _operand_1);
      }
      boolean _written = ModelUtil.written(test, "lengthOp");
      if (_written) {
        String _xifexpression = null;
        EScalarDType _type = this.type(target);
        boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
        if (_equals) {
          _xifexpression = "array_length";
        } else {
          String _xifexpression_1 = null;
          boolean _contains = LancedbFilter.TEXTUAL.contains(this.type(target));
          if (_contains) {
            _xifexpression_1 = "char_length";
          } else {
            String _literal = this.type(target).getLiteral();
            String _plus_2 = ("length tests on " + _literal);
            String _plus_3 = (_plus_2 + " field ");
            String _plus_4 = (_plus_3 + name);
            throw Unsupported.of(_plus_4);
          }
          _xifexpression = _xifexpression_1;
        }
        final String function = _xifexpression;
        String _symbol = this.symbol(test.getLengthOp());
        String _plus_5 = ((((function + "(") + name) + ") ") + _symbol);
        String _plus_6 = (_plus_5 + " ");
        int _length = test.getLength();
        return (_plus_6 + Integer.valueOf(_length));
      }
      Literal _value = test.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        return this.unary(test, name);
      }
      _xblockexpression = this.comparison(target, name, test.getOp(), test.getValue());
    }
    return _xblockexpression;
  }

  private String unary(final Predicate test, final String name) {
    String _switchResult = null;
    EUnaryOp _unaryOp = test.getUnaryOp();
    if (_unaryOp != null) {
      switch (_unaryOp) {
        case IS_NULL:
        case NOT_EXISTS:
          _switchResult = (name + " IS NULL");
          break;
        case IS_NOT_NULL:
        case EXISTS:
          _switchResult = (name + " IS NOT NULL");
          break;
        case IS_EMPTY:
          String _xifexpression = null;
          EScalarDType _type = this.type(test.getTarget());
          boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
          if (_equals) {
            _xifexpression = (("array_length(" + name) + ") = 0");
          } else {
            String _xifexpression_1 = null;
            boolean _contains = LancedbFilter.TEXTUAL.contains(this.type(test.getTarget()));
            if (_contains) {
              _xifexpression_1 = (name + " = \'\'");
            } else {
              EScalarDType _type_1 = this.type(test.getTarget());
              String _literal = null;
              if (_type_1!=null) {
                _literal=_type_1.getLiteral();
              }
              String _plus = ("is_empty on a " + _literal);
              String _plus_1 = (_plus + " field");
              throw Unsupported.of(_plus_1);
            }
            _xifexpression = _xifexpression_1;
          }
          _switchResult = _xifexpression;
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }

  private String symbol(final EComparisonOp op) {
    String _switchResult = null;
    if (op != null) {
      switch (op) {
        case EQ:
          _switchResult = "=";
          break;
        case NEQ:
          _switchResult = "<>";
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
          String _literal = op.getLiteral();
          String _plus = (_literal + " in a length test");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = op.getLiteral();
      String _plus = (_literal + " in a length test");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private void ordered(final FieldPath target) {
    final EScalarDType kind = this.type(target);
    if (((kind != null) && (!LancedbFilter.ORDERED.contains(kind)))) {
      String _literal = kind.getLiteral();
      String _plus = ("ordered comparisons on " + _literal);
      String _plus_1 = (_plus + " field ");
      String _column = this.column(target);
      String _plus_2 = (_plus_1 + _column);
      throw Unsupported.of(_plus_2);
    }
  }

  private String comparison(final FieldPath target, final String name, final EComparisonOp op, final Literal literal) {
    String _xblockexpression = null;
    {
      if ((literal instanceof NullLit)) {
        return this.nullTest(name, op);
      }
      EScalarDType _type = this.type(target);
      final boolean array = Objects.equals(_type, EScalarDType.ARRAY);
      String _switchResult = null;
      if (op != null) {
        switch (op) {
          case EQ:
          case NEQ:
            String _xblockexpression_1 = null;
            {
              if (array) {
                throw Unsupported.of((("eq and neq on array field " + name) + "; use contains, contains_any or contains_all"));
              }
              String _symbol = this.symbol(op);
              String _plus = ((name + " ") + _symbol);
              String _plus_1 = (_plus + " ");
              String _operand = this.operand(literal);
              _xblockexpression_1 = (_plus_1 + _operand);
            }
            _switchResult = _xblockexpression_1;
            break;
          case GT:
          case GTE:
          case LT:
          case LTE:
            String _xblockexpression_2 = null;
            {
              this.ordered(target);
              String _symbol = this.symbol(op);
              String _plus = ((name + " ") + _symbol);
              String _plus_1 = (_plus + " ");
              String _operand = this.operand(literal);
              _xblockexpression_2 = (_plus_1 + _operand);
            }
            _switchResult = _xblockexpression_2;
            break;
          case IN:
            String _list = this.list(literal);
            _switchResult = ((name + " IN ") + _list);
            break;
          case NOT_IN:
            String _list_1 = this.list(literal);
            _switchResult = ((name + " NOT IN ") + _list_1);
            break;
          case CONTAINS:
            String _xifexpression = null;
            if (array) {
              String _operand = this.operand(literal);
              String _plus = ((("array_has(" + name) + ", ") + _operand);
              _xifexpression = (_plus + ")");
            } else {
              String _xifexpression_1 = null;
              boolean _contains = LancedbFilter.TEXTUAL.contains(this.type(target));
              if (_contains) {
                String _operand_1 = this.operand(literal);
                String _plus_1 = ((("contains(" + name) + ", ") + _operand_1);
                _xifexpression_1 = (_plus_1 + ")");
              } else {
                String _literal = this.type(target).getLiteral();
                String _plus_2 = ("contains on " + _literal);
                String _plus_3 = (_plus_2 + " field ");
                String _plus_4 = (_plus_3 + name);
                throw Unsupported.of(_plus_4);
              }
              _xifexpression = _xifexpression_1;
            }
            _switchResult = _xifexpression;
            break;
          case CONTAINS_ANY:
            String _arrayColumn = this.arrayColumn(target, name);
            String _plus_5 = ("array_has_any(" + _arrayColumn);
            String _plus_6 = (_plus_5 + ", ");
            String _operand_2 = this.operand(literal);
            String _plus_7 = (_plus_6 + _operand_2);
            _switchResult = (_plus_7 + ")");
            break;
          case CONTAINS_ALL:
            String _arrayColumn_1 = this.arrayColumn(target, name);
            String _plus_8 = ("array_has_all(" + _arrayColumn_1);
            String _plus_9 = (_plus_8 + ", ");
            String _operand_3 = this.operand(literal);
            String _plus_10 = (_plus_9 + _operand_3);
            _switchResult = (_plus_10 + ")");
            break;
          case LIKE:
            String _pattern = this.pattern(literal);
            _switchResult = ((name + " LIKE ") + _pattern);
            break;
          case PREFIX:
            String _operand_4 = this.operand(literal);
            String _plus_11 = ((("starts_with(" + name) + ", ") + _operand_4);
            _switchResult = (_plus_11 + ")");
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private String nullTest(final String name, final EComparisonOp op) {
    boolean _equals = Objects.equals(op, EComparisonOp.EQ);
    if (_equals) {
      return (name + " IS NULL");
    }
    boolean _equals_1 = Objects.equals(op, EComparisonOp.NEQ);
    if (_equals_1) {
      return (name + " IS NOT NULL");
    }
    String _literal = op.getLiteral();
    String _plus = (_literal + " with null");
    throw Unsupported.of(_plus);
  }

  private String arrayColumn(final FieldPath target, final String name) {
    String _xblockexpression = null;
    {
      EScalarDType _type = this.type(target);
      boolean _notEquals = (!Objects.equals(_type, EScalarDType.ARRAY));
      if (_notEquals) {
        throw Unsupported.of((("contains_any and contains_all on " + name) + ", which is not an array"));
      }
      EScalarDType _elementType = this.elementType(target);
      boolean _tripleEquals = (_elementType == null);
      if (_tripleEquals) {
        throw Unsupported.of("contains_any and contains_all on an array without element-type");
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  /**
   * A like pattern: % and _ keep their meaning; a literal pattern's backslashes are escaped (LIKE's escape character).
   */
  private String pattern(final Literal literal) {
    if ((literal instanceof StringLit)) {
      return LancedbSql.like(((StringLit)literal).getValue());
    }
    throw Unsupported.of("like with a value that is not a string literal; the generator escapes the pattern when it is generated");
  }

  /**
   * Text operators in a where string: substring tests, SQL regular expressions, and token tests (contains_tokens, which
   * splits on non-alphanumeric characters and compares case-sensitively, lance-datafusion/src/udf.rs, so both sides are
   * lower-cased).
   */
  private String textPredicate(final Predicate test, final String name) {
    String _switchResult = null;
    ETextOp _textOp = test.getTextOp();
    if (_textOp != null) {
      switch (_textOp) {
        case CONTAINS_TEXT:
          String _operand = this.operand(test.getText());
          String _plus = ((("contains(" + name) + ", ") + _operand);
          _switchResult = (_plus + ")");
          break;
        case REGEX:
          String _operand_1 = this.operand(test.getText());
          String _plus_1 = ((("regexp_like(" + name) + ", ") + _operand_1);
          _switchResult = (_plus_1 + ")");
          break;
        case MATCH_ALL_TERMS:
          String _text = LancedbSql.text(IterableExtensions.join(this.terms(test.getText()), " "));
          String _plus_2 = ((("contains_tokens(lower(" + name) + "), ") + _text);
          _switchResult = (_plus_2 + ")");
          break;
        case MATCH:
        case MATCH_ANY_TERM:
          final Function1<String, String> _function = (String it) -> {
            String _text_1 = LancedbSql.text(it);
            String _plus_3 = ((("contains_tokens(lower(" + name) + "), ") + _text_1);
            return (_plus_3 + ")");
          };
          String _join = IterableExtensions.join(ListExtensions.<String, String>map(this.terms(test.getText()), _function), " OR ");
          String _plus_3 = ("(" + _join);
          _switchResult = (_plus_3 + ")");
          break;
        default:
          String _literal = test.getTextOp().getLiteral();
          String _plus_4 = ("text operator " + _literal);
          String _plus_5 = (_plus_4 + " in a where string; LanceDB filters text with contains, regexp_like and contains_tokens");
          throw Unsupported.of(_plus_5);
      }
    } else {
      String _literal = test.getTextOp().getLiteral();
      String _plus_4 = ("text operator " + _literal);
      String _plus_5 = (_plus_4 + " in a where string; LanceDB filters text with contains, regexp_like and contains_tokens");
      throw Unsupported.of(_plus_5);
    }
    return _switchResult;
  }

  /**
   * The lower-cased terms of a literal text.
   */
  private List<String> terms(final Literal literal) {
    List<String> _xblockexpression = null;
    {
      if ((!(literal instanceof StringLit))) {
        throw Unsupported.of("a token test with a value that is not a string literal; the generator splits the terms when it is generated");
      }
      final Function1<String, Boolean> _function = (String it) -> {
        boolean _isEmpty = it.isEmpty();
        return Boolean.valueOf((!_isEmpty));
      };
      final List<String> result = IterableExtensions.<String>toList(IterableExtensions.<String>filter(((Iterable<String>)Conversions.doWrapArray(((StringLit) literal).getValue().toLowerCase(Locale.ROOT).split("\\s+"))), _function));
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("a token test without terms");
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String list(final Literal literal) {
    String _xblockexpression = null;
    {
      if (((literal instanceof ListLit) && ((ListLit) literal).getItems().isEmpty())) {
        throw Unsupported.of("in and not_in with an empty list");
      }
      _xblockexpression = this.sql.value(Literals.of(literal), true);
    }
    return _xblockexpression;
  }

  private String operand(final Literal literal) {
    return this.sql.value(Literals.of(literal), false);
  }
}
