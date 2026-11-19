package org.ftn.vecDSL.generator.duckdb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.duckdb.DuckdbSql;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.GeoPredicate;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NotCondition;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.OrCondition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.SparseLit;

/**
 * Translates a vecDSL filter into DuckdbSql conditions, which DuckdbSql renders as a WHERE clause with bound parameters.
 * Fields are columns; a path below a nested or json field reads the JSON value with ->> and casts it to the sub-field's type.
 * A negation matches rows where the negated condition is not true, so rows with a null value match it, as the other vecDSL
 * targets treat records without the field.
 */
@SuppressWarnings("all")
public class DuckdbFilter {
  private static final Map<EScalarDType, String> PATH_TYPES = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "INTEGER"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "BIGINT"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "TINYINT"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "FLOAT"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "DOUBLE"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "BOOLEAN"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "VARCHAR"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "VARCHAR"), Pair.<EScalarDType, String>of(EScalarDType.DATE, "DATE"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "TIMESTAMP"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "TIMESTAMPTZ"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "UUID")));

  private final List<DuckdbSql.Cond> collected = CollectionLiterals.<DuckdbSql.Cond>newArrayList();

  public DuckdbFilter where(final FilterBlock block) {
    DuckdbFilter _xblockexpression = null;
    {
      if ((block != null)) {
        DuckdbSql.Cond _condition = this.condition(block.getCondition());
        this.collected.add(_condition);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * A slice condition: tenant or namespace.
   */
  public DuckdbFilter slice(final String column, final List<Code.Expr> values) {
    DuckdbFilter _xblockexpression = null;
    {
      DuckdbSql.Raw _raw = new DuckdbSql.Raw(column, values);
      this.collected.add(_raw);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public List<DuckdbSql.Cond> parts() {
    return this.collected;
  }

  public boolean isEmpty() {
    return this.collected.isEmpty();
  }

  public DuckdbSql.Cond condition(final Condition node) {
    DuckdbSql.Cond _switchResult = null;
    boolean _matched = false;
    if (node instanceof AndCondition) {
      _matched=true;
      DuckdbSql.Cond _condition = this.condition(((AndCondition)node).getLeft());
      DuckdbSql.Cond _condition_1 = this.condition(((AndCondition)node).getRight());
      _switchResult = new DuckdbSql.And(Collections.<DuckdbSql.Cond>unmodifiableList(CollectionLiterals.<DuckdbSql.Cond>newArrayList(_condition, _condition_1)));
    }
    if (!_matched) {
      if (node instanceof OrCondition) {
        _matched=true;
        DuckdbSql.Cond _condition = this.condition(((OrCondition)node).getLeft());
        DuckdbSql.Cond _condition_1 = this.condition(((OrCondition)node).getRight());
        _switchResult = new DuckdbSql.Or(Collections.<DuckdbSql.Cond>unmodifiableList(CollectionLiterals.<DuckdbSql.Cond>newArrayList(_condition, _condition_1)));
      }
    }
    if (!_matched) {
      if (node instanceof NotCondition) {
        _matched=true;
        DuckdbSql.Cond _condition = this.condition(((NotCondition)node).getOperand());
        _switchResult = new DuckdbSql.Not(_condition);
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
        throw Unsupported.of(("geo predicates; " + DuckdbStatements.NO_SPATIAL));
      }
    }
    if (!_matched) {
      if (node instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of((("element predicates (\'any\'); " + DuckdbStatements.PENDING) + " (list_any over the elements)"));
      }
    }
    if (!_matched) {
      throw Unsupported.of(node.eClass().getName());
    }
    return _switchResult;
  }

  private DuckdbSql.Cond test(final Predicate p) {
    DuckdbSql.Test _xblockexpression = null;
    {
      final FieldPath path = p.getTarget();
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("raw paths; the generator filters declared fields and their sub-fields");
      }
      final String column = ModelUtil.fieldName(path.getField());
      final Function1<Field, String> _function = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      final List<String> segments = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      String _xifexpression = null;
      boolean _isEmpty = segments.isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = DuckdbFilter.leafType(path);
      }
      final String pathType = _xifexpression;
      Literal _text = p.getText();
      boolean _tripleNotEquals_1 = (_text != null);
      if (_tripleNotEquals_1) {
        String _literal = p.getTextOp().getLiteral();
        boolean _equals = Objects.equals(_literal, "fuzzy");
        if (_equals) {
          throw Unsupported.of(("the fuzzy text operator; " + DuckdbStatements.NO_FUZZY));
        }
        String _literal_1 = p.getTextOp().getLiteral();
        Code.Expr _operand = this.operand(p.getText());
        return new DuckdbSql.Test(column, segments, pathType, _literal_1, _operand, null);
      }
      Literal _lower = p.getLower();
      boolean _tripleNotEquals_2 = (_lower != null);
      if (_tripleNotEquals_2) {
        Code.Expr _operand_1 = this.operand(p.getLower());
        Code.Expr _operand_2 = this.operand(p.getUpper());
        return new DuckdbSql.Test(column, segments, pathType, "between", _operand_1, _operand_2);
      }
      boolean _written = ModelUtil.written(p, "lengthOp");
      if (_written) {
        boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("eq", "neq", "gt", "gte", "lt", "lte")).contains(p.getLengthOp().getLiteral());
        boolean _not = (!_contains);
        if (_not) {
          String _literal_2 = p.getLengthOp().getLiteral();
          String _plus = ("length " + _literal_2);
          String _plus_1 = (_plus + "; length tests compare with a number");
          throw Unsupported.of(_plus_1);
        }
        String _literal_3 = p.getLengthOp().getLiteral();
        String _plus_2 = ("length_" + _literal_3);
        Code.Num _num = Code.num(p.getLength());
        return new DuckdbSql.Test(column, segments, pathType, _plus_2, _num, null);
      }
      Literal _value = p.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        String _literal_4 = p.getUnaryOp().getLiteral();
        return new DuckdbSql.Test(column, segments, pathType, _literal_4, null, null);
      }
      Literal _value_1 = p.getValue();
      if ((_value_1 instanceof NullLit)) {
        throw Unsupported.of("comparisons with null; write is_null or is_not_null");
      }
      String _literal_5 = p.getOp().getLiteral();
      Code.Expr _operand_3 = this.operand(p.getValue());
      _xblockexpression = new DuckdbSql.Test(column, segments, pathType, _literal_5, _operand_3, null);
    }
    return _xblockexpression;
  }

  /**
   * The SQL type of a sub-field read from JSON: its declared scalar type.
   */
  public static String leafType(final FieldPath path) {
    String _xblockexpression = null;
    {
      final Field leaf = ModelUtil.leaf(path);
      if ((!(leaf instanceof PayloadField))) {
        throw Unsupported.of("a vector field below a nested field");
      }
      final String type = DuckdbFilter.PATH_TYPES.get(((PayloadField) leaf).getType());
      if ((type == null)) {
        String _fieldName = ModelUtil.fieldName(leaf);
        String _plus = ("filters on sub-field " + _fieldName);
        String _plus_1 = (_plus + " of type ");
        String _literal = ((PayloadField) leaf).getType().getLiteral();
        String _plus_2 = (_plus_1 + _literal);
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = type;
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
