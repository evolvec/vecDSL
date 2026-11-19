package org.ftn.vecDSL.generator.redis;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.generator.redis.RedisIndex;
import org.ftn.vecDSL.generator.redis.RedisQuery;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AndCondition;
import org.ftn.vecDSL.vecDSL.Condition;
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
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Translates a vecDSL filter into RedisQuery conditions on the attributes of the collection's index. A negation matches documents
 * where the negated condition does not hold, so documents without the field match it, as on the other vecDSL targets.
 */
@SuppressWarnings("all")
public class RedisFilter {
  private final RedisSchema schema;

  private final VectorCollection collection;

  private final RedisIndex.Index index;

  private final List<RedisQuery.Cond> collected = CollectionLiterals.<RedisQuery.Cond>newArrayList();

  public RedisFilter(final RedisSchema schema, final VectorCollection collection) {
    this.schema = schema;
    this.collection = collection;
    this.index = schema.index(collection);
  }

  public RedisFilter where(final FilterBlock block) {
    RedisFilter _xblockexpression = null;
    {
      if ((block != null)) {
        RedisQuery.Cond _condition = this.condition(block.getCondition());
        this.collected.add(_condition);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  /**
   * A slice condition: the hidden tag (tenant, namespace or partition) equals one of the values.
   */
  public RedisFilter slice(final String attribute, final List<Code.Expr> values) {
    RedisFilter _xblockexpression = null;
    {
      final RedisIndex.Attr attr = this.index.attr(attribute);
      RedisQuery.Test _xifexpression = null;
      int _size = values.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        Code.Expr _head = IterableExtensions.<Code.Expr>head(values);
        _xifexpression = new RedisQuery.Test(attr, "eq", _head, null);
      } else {
        Code.Items _items = Code.items(values);
        _xifexpression = new RedisQuery.Test(attr, "in", _items, null);
      }
      this.collected.add(_xifexpression);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public RedisFilter add(final RedisQuery.Cond extra) {
    RedisFilter _xblockexpression = null;
    {
      this.collected.add(extra);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public List<RedisQuery.Cond> parts() {
    return this.collected;
  }

  public RedisQuery.Cond condition(final Condition node) {
    RedisQuery.Cond _switchResult = null;
    boolean _matched = false;
    if (node instanceof AndCondition) {
      _matched=true;
      RedisQuery.Cond _condition = this.condition(((AndCondition)node).getLeft());
      RedisQuery.Cond _condition_1 = this.condition(((AndCondition)node).getRight());
      _switchResult = new RedisQuery.And(Collections.<RedisQuery.Cond>unmodifiableList(CollectionLiterals.<RedisQuery.Cond>newArrayList(_condition, _condition_1)));
    }
    if (!_matched) {
      if (node instanceof OrCondition) {
        _matched=true;
        RedisQuery.Cond _condition = this.condition(((OrCondition)node).getLeft());
        RedisQuery.Cond _condition_1 = this.condition(((OrCondition)node).getRight());
        _switchResult = new RedisQuery.Or(Collections.<RedisQuery.Cond>unmodifiableList(CollectionLiterals.<RedisQuery.Cond>newArrayList(_condition, _condition_1)));
      }
    }
    if (!_matched) {
      if (node instanceof NotCondition) {
        _matched=true;
        RedisQuery.Cond _condition = this.condition(((NotCondition)node).getOperand());
        _switchResult = new RedisQuery.Not(_condition);
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
        throw Unsupported.of("geo predicates; has no Redis mapping yet (GEO attributes and @field:[lon lat radius unit])");
      }
    }
    if (!_matched) {
      if (node instanceof ElementPredicate) {
        _matched=true;
        throw Unsupported.of("element predicates (\'any\'); the Query Engine indexes each array path on its own and cannot require two conditions to hold for the same element");
      }
    }
    if (!_matched) {
      throw Unsupported.of(node.eClass().getName());
    }
    return _switchResult;
  }

  private RedisQuery.Cond test(final Predicate p) {
    RedisQuery.Test _xblockexpression = null;
    {
      final RedisIndex.Attr attr = this.index.attr(this.attribute(p.getTarget()));
      Literal _text = p.getText();
      boolean _tripleNotEquals = (_text != null);
      if (_tripleNotEquals) {
        String _literal = p.getTextOp().getLiteral();
        Code.Expr _operand = this.operand(p.getText());
        return new RedisQuery.Test(attr, _literal, _operand, null);
      }
      Literal _lower = p.getLower();
      boolean _tripleNotEquals_1 = (_lower != null);
      if (_tripleNotEquals_1) {
        Code.Expr _operand_1 = this.operand(p.getLower());
        Code.Expr _operand_2 = this.operand(p.getUpper());
        return new RedisQuery.Test(attr, "between", _operand_1, _operand_2);
      }
      boolean _written = ModelUtil.written(p, "lengthOp");
      if (_written) {
        String _literal_1 = p.getLengthOp().getLiteral();
        String _plus = ("length_" + _literal_1);
        Code.Num _num = Code.num(p.getLength());
        return new RedisQuery.Test(attr, _plus, _num, null);
      }
      Literal _value = p.getValue();
      boolean _tripleEquals = (_value == null);
      if (_tripleEquals) {
        String _literal_2 = p.getUnaryOp().getLiteral();
        return new RedisQuery.Test(attr, _literal_2, null, null);
      }
      Literal _value_1 = p.getValue();
      if ((_value_1 instanceof NullLit)) {
        throw Unsupported.of("comparisons with null; write is_null or is_not_null");
      }
      String _literal_3 = p.getOp().getLiteral();
      Code.Expr _operand_3 = this.operand(p.getValue());
      _xblockexpression = new RedisQuery.Test(attr, _literal_3, _operand_3, null);
    }
    return _xblockexpression;
  }

  /**
   * The attribute of a field path: the field's (as named after earlier renames), or field_sub for a sub-field of a json field.
   */
  public String attribute(final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("raw paths; the generator filters declared fields and their sub-fields");
      }
      String _name = this.schema.name(this.collection, path.getField());
      final Function1<Field, String> _function = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
      _xblockexpression = IterableExtensions.join(Iterables.<String>concat(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_name)), _map), "_");
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
