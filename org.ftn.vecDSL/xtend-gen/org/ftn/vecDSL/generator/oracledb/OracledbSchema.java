package org.ftn.vecDSL.generator.oracledb;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to Oracle AI Database 26ai tables. Every field is a typed column in declaration order: payload fields
 * as SQL types (nested, json and array fields as JSON columns), vector fields as VECTOR columns with their dimension and format,
 * each with its own vector index; full-text fields carry an Oracle Text CONTEXT index synchronized on commit. The primary key is
 * the key column; tenant, namespace and partition names live in the partition-key field or in hidden VARCHAR2 columns. A logical
 * database is a table-name prefix (database__collection) in the program's schema.
 */
@SuppressWarnings("all")
public class OracledbSchema {
  public static final String TENANT_COLUMN = "vecdsl_tenant";

  public static final String NAMESPACE_COLUMN = "vecdsl_namespace";

  public static final String PARTITION_COLUMN = "vecdsl_partition";

  public static final String DEFAULT_PARTITION = "default";

  public static final String DATABASE_SEPARATOR = "__";

  private static final Map<EScalarDType, String> SCALARS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "NUMBER(10)"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "NUMBER(19)"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "NUMBER(3)"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "BINARY_FLOAT"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "BINARY_DOUBLE"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "BOOLEAN"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "VARCHAR2(4000)"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "VARCHAR2(4000)"), Pair.<EScalarDType, String>of(EScalarDType.DATE, "DATE"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "TIMESTAMP"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "TIMESTAMP WITH TIME ZONE"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "VARCHAR2(36)"), Pair.<EScalarDType, String>of(EScalarDType.JSON, "JSON"), Pair.<EScalarDType, String>of(EScalarDType.MAP, "JSON"), Pair.<EScalarDType, String>of(EScalarDType.STRUCT, "JSON")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted;

  private final Set<VectorCollection> namespaced;

  private final Map<VectorCollection, OracledbSql.Table> evolved = CollectionLiterals.<VectorCollection, OracledbSql.Table>newHashMap();

  public OracledbSchema(final List<String> notes, final Script script) {
    this.notes = notes;
    this.tenanted = CollectionLiterals.<VectorCollection>newHashSet();
    this.namespaced = CollectionLiterals.<VectorCollection>newHashSet();
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
    for (final EObject object : _iterable) {
      this.slices(object);
    }
  }

  /**
   * Collections whose statements name a tenant or a namespace get the slice columns.
   */
  private void slices(final EObject object) {
    final VectorCollection collection = ModelUtil.targetCollection(object);
    if ((collection == null)) {
      return;
    }
    final EStructuralFeature tenant = object.eClass().getEStructuralFeature("tenant");
    if (((tenant != null) && (object.eGet(tenant) != null))) {
      this.tenanted.add(collection);
    }
    final EStructuralFeature namespace = object.eClass().getEStructuralFeature("namespace");
    if ((((namespace != null) && Objects.equals(namespace.getEType().getName(), "EString")) && (object.eGet(namespace) != null))) {
      this.namespaced.add(collection);
    }
  }

  /**
   * The table of a collection as the statements so far leave it (alters change columns and the name).
   */
  public OracledbSql.Table table(final VectorCollection c) {
    OracledbSql.Table _elvis = null;
    OracledbSql.Table _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      OracledbSql.Table _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final OracledbSql.Table table) {
    if ((table == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, table);
    }
  }

  private OracledbSql.Table declared(final VectorCollection c) {
    OracledbSql.Table _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!OracledbSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys tables by an integer, text or uuid column (Oracle cannot key a table by a VECTOR column)");
        throw Unsupported.of(_plus_1);
      }
      final List<OracledbSql.Column> columns = CollectionLiterals.<OracledbSql.Column>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        OracledbSql.Column _column = this.column(c, field);
        columns.add(_column);
      }
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(it.isPartitionKey());
      };
      final PayloadField partitionKey = IterableExtensions.<PayloadField>findFirst(ModelUtil.payloadFields(c), _function);
      String _xifexpression_1 = null;
      if (((!c.getTenants().isEmpty()) || this.tenanted.contains(c))) {
        String _xifexpression_2 = null;
        if ((partitionKey == null)) {
          _xifexpression_2 = OracledbSchema.TENANT_COLUMN;
        } else {
          _xifexpression_2 = partitionKey.getName();
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String tenant = _xifexpression_1;
      String _xifexpression_3 = null;
      if (((!c.getNamespaces().isEmpty()) || this.namespaced.contains(c))) {
        String _xifexpression_4 = null;
        if ((partitionKey == null)) {
          _xifexpression_4 = OracledbSchema.NAMESPACE_COLUMN;
        } else {
          _xifexpression_4 = partitionKey.getName();
        }
        _xifexpression_3 = _xifexpression_4;
      }
      final String namespace = _xifexpression_3;
      String _xifexpression_5 = null;
      boolean _isEmpty = ModelUtil.partitions(c).isEmpty();
      if (_isEmpty) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = OracledbSchema.PARTITION_COLUMN;
      }
      final String partition = _xifexpression_5;
      final String database = OracledbSchema.databaseOf(c);
      String _physical = OracledbSchema.physical(database, c.getName());
      String _fieldName = ModelUtil.fieldName(key);
      Map<String, String> _emptyMap = CollectionLiterals.<String, String>emptyMap();
      _xblockexpression = new OracledbSql.Table(database, _physical, _fieldName, columns, tenant, namespace, partition, _emptyMap);
    }
    return _xblockexpression;
  }

  /**
   * The table name of a collection: prefixed with its logical database.
   */
  public static String physical(final String database, final String collection) {
    String _xblockexpression = null;
    {
      String _xifexpression = null;
      if ((database == null)) {
        _xifexpression = collection;
      } else {
        _xifexpression = ((database + OracledbSchema.DATABASE_SEPARATOR) + collection);
      }
      final String name = _xifexpression;
      OracledbSql.id(name);
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  /**
   * The logical database of a collection declared in a logical-db, else null for the program's schema.
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      if ((database == null)) {
        return null;
      }
      boolean _contains = database.getName().contains(OracledbSchema.DATABASE_SEPARATOR);
      if (_contains) {
        String _name = database.getName();
        String _plus = ("the logical database name " + _name);
        String _plus_1 = (_plus + "; the generator separates database and collection names with ");
        String _plus_2 = (_plus_1 + OracledbSchema.DATABASE_SEPARATOR);
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = database.getName();
    }
    return _xblockexpression;
  }

  public OracledbSql.Column column(final VectorCollection c, final Field field) {
    OracledbSql.Column _switchResult = null;
    boolean _matched = false;
    if (field instanceof VectorField) {
      _matched=true;
      _switchResult = this.vectorColumn(c, ((VectorField)field));
    }
    if (!_matched) {
      if (field instanceof PayloadField) {
        _matched=true;
        _switchResult = this.payloadColumn(c, ((PayloadField)field));
      }
    }
    return _switchResult;
  }

  private OracledbSql.Column payloadColumn(final VectorCollection c, final PayloadField field) {
    OracledbSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      OracledbSql.id(name);
      this.note(field, "nullable");
      final boolean nested = ((!field.getSubFields().isEmpty()) || field.isNested());
      final String lexer = this.lexerOf(c, field);
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = OracledbSchema.check(limit, field);
        checks.add(_check);
      }
      if ((((nested || Objects.equals(field.getType(), EScalarDType.JSON)) || Objects.equals(field.getType(), EScalarDType.MAP)) || Objects.equals(field.getType(), EScalarDType.STRUCT))) {
        if ((lexer != null)) {
          throw Unsupported.of(("full-text search on json field " + name));
        }
        Code.Expr _defaultOf = OracledbSchema.defaultOf(field);
        return new OracledbSql.Column(name, "JSON", null, true, null, null, true, _defaultOf, checks);
      }
      EScalarDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
      if (_equals) {
        boolean _written = ModelUtil.written(field, "elementType");
        boolean _not = (!_written);
        if (_not) {
          throw Unsupported.of((("array field " + name) + " without element-type"));
        }
        final String element = this.scalar(field.getElementType(), name);
        boolean _equals_1 = Objects.equals(element, "JSON");
        if (_equals_1) {
          String _literal = field.getElementType().getLiteral();
          String _plus = ("arrays of " + _literal);
          String _plus_1 = (_plus + " in field ");
          String _plus_2 = (_plus_1 + name);
          throw Unsupported.of(_plus_2);
        }
        if ((lexer != null)) {
          throw Unsupported.of(("full-text search on array field " + name));
        }
        String _kindOf = OracledbSql.kindOf(element);
        Code.Expr _defaultOf_1 = OracledbSchema.defaultOf(field);
        return new OracledbSql.Column(name, "JSON", _kindOf, true, null, null, true, _defaultOf_1, checks);
      }
      final String base = this.scalar(field.getType(), name);
      String _xifexpression = null;
      if ((ModelUtil.written(field, "maxLength") && base.startsWith("VARCHAR2"))) {
        String _xblockexpression_1 = null;
        {
          int _maxLength = field.getMaxLength();
          boolean _greaterThan = (_maxLength > 4000);
          if (_greaterThan) {
            int _maxLength_1 = field.getMaxLength();
            String _plus_3 = ("max-length " + Integer.valueOf(_maxLength_1));
            String _plus_4 = (_plus_3 + " on ");
            String _plus_5 = (_plus_4 + name);
            String _plus_6 = (_plus_5 + "; the generator writes VARCHAR2 columns of at most 4000 bytes, the limit of MAX_STRING_SIZE STANDARD");
            throw Unsupported.of(_plus_6);
          }
          int _maxLength_2 = field.getMaxLength();
          String _plus_7 = ("VARCHAR2(" + Integer.valueOf(_maxLength_2));
          _xblockexpression_1 = (_plus_7 + ")");
        }
        _xifexpression = _xblockexpression_1;
      } else {
        _xifexpression = base;
      }
      final String sqlType = _xifexpression;
      if (((lexer != null) && (!base.startsWith("VARCHAR2")))) {
        String _literal_1 = field.getType().getLiteral();
        String _plus_3 = ("full-text search on " + _literal_1);
        String _plus_4 = (_plus_3 + " field ");
        String _plus_5 = (_plus_4 + name);
        throw Unsupported.of(_plus_5);
      }
      Code.Expr _defaultOf_2 = OracledbSchema.defaultOf(field);
      _xblockexpression = new OracledbSql.Column(name, sqlType, null, false, null, lexer, true, _defaultOf_2, checks);
    }
    return _xblockexpression;
  }

  private static Code.Expr defaultOf(final PayloadField field) {
    Code.Expr _xifexpression = null;
    Literal _defaultValue = field.getDefaultValue();
    boolean _tripleEquals = (_defaultValue == null);
    if (_tripleEquals) {
      _xifexpression = null;
    } else {
      _xifexpression = Literals.of(field.getDefaultValue());
    }
    return _xifexpression;
  }

  private String scalar(final EScalarDType type, final String field) {
    final String sql = OracledbSchema.SCALARS.get(type);
    if ((sql != null)) {
      return sql;
    }
    if (type != null) {
      switch (type) {
        case BYTES:
          throw Unsupported.of((("bytes field " + field) + "; has no Oracle mapping yet (RAW and BLOB columns with base64 values in the three runtimes)"));
        case GEO_POINT:
        case GEOMETRY:
          String _literal = type.getLiteral();
          String _plus = (_literal + " field ");
          String _plus_1 = (_plus + field);
          String _plus_2 = (_plus_1 + "; has no Oracle mapping yet (SDO_GEOMETRY of Oracle Spatial)");
          throw Unsupported.of(_plus_2);
        default:
          String _literal_1 = type.getLiteral();
          String _plus_3 = (_literal_1 + " field ");
          String _plus_4 = (_plus_3 + field);
          String _plus_5 = (_plus_4 + "; Oracle has no media column types (BLOB holds bytes without media semantics)");
          throw Unsupported.of(_plus_5);
      }
    } else {
      String _literal_1 = type.getLiteral();
      String _plus_3 = (_literal_1 + " field ");
      String _plus_4 = (_plus_3 + field);
      String _plus_5 = (_plus_4 + "; Oracle has no media column types (BLOB holds bytes without media semantics)");
      throw Unsupported.of(_plus_5);
    }
  }

  /**
   * A field limit as a named CHECK constraint; {} stands for the column.
   */
  public static String check(final FieldLimit limit, final Field field) {
    String _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      String _switchResult = null;
      EFieldLimit _type = limit.getType();
      if (_type != null) {
        switch (_type) {
          case ARRAY_LEN:
            String _xblockexpression_1 = null;
            {
              if (((!(field instanceof PayloadField)) || (!Objects.equals(((PayloadField) field).getType(), EScalarDType.ARRAY)))) {
                throw Unsupported.of((("array_len on " + name) + ", which is not an array"));
              }
              int _value = limit.getValue();
              _xblockexpression_1 = ("JSON_VALUE({}, \'$.size()\' RETURNING NUMBER) <= " + Integer.valueOf(_value));
            }
            _switchResult = _xblockexpression_1;
            break;
          case MAX_SIZE:
            String _xblockexpression_2 = null;
            {
              if ((field instanceof VectorField)) {
                throw Unsupported.of(("max_size on vector field " + name));
              }
              int _value = limit.getValue();
              _xblockexpression_2 = ("LENGTHB(JSON_SERIALIZE(JSON_SCALAR({}) RETURNING CLOB)) <= " + Integer.valueOf(_value));
            }
            _switchResult = _xblockexpression_2;
            break;
          case VECTOR_DIM:
            String _xblockexpression_3 = null;
            {
              if ((!(field instanceof VectorField))) {
                throw Unsupported.of(("vector_dim on payload field " + name));
              }
              int _value = limit.getValue();
              _xblockexpression_3 = ("VECTOR_DIMENSION_COUNT({}) <= " + Integer.valueOf(_value));
            }
            _switchResult = _xblockexpression_3;
            break;
          default:
            break;
        }
      }
      final String condition = _switchResult;
      String _id = OracledbSql.id(limit.getName());
      String _plus = ("CONSTRAINT " + _id);
      String _plus_1 = (_plus + " CHECK (");
      String _plus_2 = (_plus_1 + condition);
      _xblockexpression = (_plus_2 + ")");
    }
    return _xblockexpression;
  }

  /**
   * The Oracle Text lexer of a full-text field: "" for the default lexer, the analyzer's lexer preference otherwise; null for a
   * field without full-text search.
   */
  public String lexerOf(final VectorCollection c, final PayloadField field) {
    String _xblockexpression = null;
    {
      final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
        return Boolean.valueOf(((it.getTarget().getField() == field) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
      };
      final List<PayloadIndex> indexed = IterableExtensions.<PayloadIndex>toList(IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function));
      if (((((!field.isFullText()) && (!field.isDocumentBody())) && (field.getAnalyzer() == null)) && indexed.isEmpty())) {
        return null;
      }
      Analyzer _elvis = null;
      Analyzer _analyzer = field.getAnalyzer();
      if (_analyzer != null) {
        _elvis = _analyzer;
      } else {
        final Function1<PayloadIndex, Analyzer> _function_1 = (PayloadIndex it) -> {
          return it.getAnalyzer();
        };
        Analyzer _head = IterableExtensions.<Analyzer>head(IterableExtensions.<Analyzer>filterNull(ListExtensions.<PayloadIndex, Analyzer>map(indexed, _function_1)));
        _elvis = _head;
      }
      final Analyzer analyzer = _elvis;
      String _xifexpression = null;
      if ((analyzer == null)) {
        _xifexpression = "";
      } else {
        _xifexpression = OracledbSchema.lexerName(analyzer);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * An analyzer is a BASIC_LEXER preference of the program's user, named like the analyzer. Oracle Text stores preference names in
   * upper case and reads them unquoted in index parameters, so the name must be a plain identifier.
   */
  public static String lexerName(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      boolean _matches = analyzer.getName().matches("[A-Za-z][A-Za-z0-9_]*");
      boolean _not = (!_matches);
      if (_not) {
        String _name = analyzer.getName();
        String _plus = ("the analyzer name " + _name);
        String _plus_1 = (_plus + "; the generator names lexer preferences with plain identifiers");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = analyzer.getName();
    }
    return _xblockexpression;
  }

  private OracledbSql.Column vectorColumn(final VectorCollection c, final VectorField field) {
    OracledbSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      OracledbSql.id(name);
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; an Oracle VECTOR column holds one vector (multi-vector search ranks rows of chunks with PARTITIONS BY)"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + OracledbStatements.NO_MODELS));
      }
      if ((ModelUtil.written(field, "modifier") && Objects.equals(field.getModifier().getLiteral(), "idf"))) {
        throw Unsupported.of((("the idf modifier on " + name) + "; Oracle stores sparse vectors as given"));
      }
      this.note(field, "normalized", "onDisk");
      boolean sparse = false;
      String _switchResult = null;
      EVectorDType _type = field.getType();
      if (_type != null) {
        switch (_type) {
          case FLOAT_VECTOR:
            _switchResult = "FLOAT32";
            break;
          case FLOAT64_VECTOR:
            _switchResult = "FLOAT64";
            break;
          case INT8_VECTOR:
            _switchResult = "INT8";
            break;
          case BINARY_VECTOR:
            _switchResult = "BINARY";
            break;
          case SPARSE_FLOAT_VECTOR:
            String _xblockexpression_1 = null;
            {
              sparse = true;
              _xblockexpression_1 = "FLOAT32";
            }
            _switchResult = _xblockexpression_1;
            break;
          default:
            String _literal = field.getType().getLiteral();
            String _plus = (_literal + " fields; Oracle VECTOR dimensions are INT8, FLOAT32, FLOAT64 or BINARY");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " fields; Oracle VECTOR dimensions are INT8, FLOAT32, FLOAT64 or BINARY");
        throw Unsupported.of(_plus);
      }
      final String format = _switchResult;
      if (((!sparse) && (field.getDimension() <= 0))) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      if ((Objects.equals(format, "BINARY") && ((field.getDimension() % 8) != 0))) {
        int _dimension = field.getDimension();
        String _plus_1 = ("a binary vector field with " + Integer.valueOf(_dimension));
        String _plus_2 = (_plus_1 + " dimensions; BINARY vectors have a multiple of 8 dimensions");
        throw Unsupported.of(_plus_2);
      }
      final VectorIndex index = this.indexOf(c, field);
      final String metric = this.metric(field, index);
      String _xifexpression = null;
      if (((index == null) || Objects.equals(index.getFamily().getLiteral(), "flat"))) {
        _xifexpression = null;
      } else {
        String _xifexpression_1 = null;
        String _literal_1 = index.getFamily().getLiteral();
        boolean _equals = Objects.equals(_literal_1, "ivf");
        if (_equals) {
          _xifexpression_1 = "ivf";
        } else {
          _xifexpression_1 = "hnsw";
        }
        _xifexpression = _xifexpression_1;
      }
      final String family = _xifexpression;
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = OracledbSchema.check(limit, field);
        checks.add(_check);
      }
      int _dimension_1 = field.getDimension();
      OracledbSql.Vec _vec = new OracledbSql.Vec(format, _dimension_1, sparse, metric, family);
      boolean _isNullable = field.isNullable();
      _xblockexpression = new OracledbSql.Column(name, "VECTOR", null, false, _vec, null, _isNullable, null, checks);
    }
    return _xblockexpression;
  }

  public VectorIndex indexOf(final VectorCollection c, final VectorField field) {
    return ModelUtil.vectorIndex(c, field);
  }

  /**
   * The metric of a vector field: the field's, else its index's, else cosine (hamming for binary fields).
   */
  public String metric(final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(field, "metric");
      if (_written) {
        return field.getMetric().getLiteral();
      }
      if (((index != null) && ModelUtil.written(index, "metric"))) {
        return index.getMetric().getLiteral();
      }
      String _xifexpression = null;
      String _literal = field.getType().getLiteral();
      boolean _equals = Objects.equals(_literal, "binary_vector");
      if (_equals) {
        _xifexpression = "hamming";
      } else {
        _xifexpression = EMetric.COSINE.getLiteral();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * A collection's table with its objects: lexer preferences of its analyzers, the table, its vector and payload indexes, the
   * CONTEXT indexes of full-text fields, the comment and the views of its aliases. Oracle commits each DDL statement on its own,
   * so a failure leaves the objects created before it.
   */
  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(OracledbStatements.EXTERNAL);
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(OracledbStatements.NO_TTL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; has no Oracle mapping yet (identity columns returned to the program)");
      }
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        throw Unsupported.of("dynamic; has no Oracle mapping yet (a JSON column for undeclared fields)");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(OracledbStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; has no Oracle mapping yet (foreign keys)");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + OracledbStatements.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of(OracledbStatements.NO_STORAGE);
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(OracledbStatements.NO_TAGS);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = OracledbStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        throw Unsupported.of(OracledbStatements.collectionLimit(IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral()));
      }
      Set<String> _keySet = ModelUtil.extParams(c, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "consistency");
      if (fresh) {
        this.evolve(c, null);
      }
      final OracledbSql.Table table = this.table(c);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<Analyzer> _analyzers = c.getAnalyzers();
      for (final Analyzer analyzer : _analyzers) {
        List<Code.Stmt> _createAnalyzer = this.createAnalyzer(analyzer);
        Iterables.<Code.Stmt>addAll(result, _createAnalyzer);
      }
      String _createTable = OracledbSql.createTable(table, this.partitionNames(c, table), ifNotExists);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createTable)));
      result.add(_execute);
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        return Boolean.valueOf(OracledbSchema.present(table, it));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function);
      for (final VectorField field : _filter) {
        {
          final VectorIndex index = this.indexOf(c, field);
          if ((index != null)) {
            final String sql = this.vectorIndex(c, table, field, index);
            if ((sql != null)) {
              Code.Stmt _guarded = OracledbSchema.guarded(OracledbSchema.indexName(c, index.getName()), sql);
              result.add(_guarded);
            }
          }
        }
      }
      final Function1<PayloadIndex, Boolean> _function_1 = (PayloadIndex it) -> {
        return Boolean.valueOf(((it.getTarget().getField() == null) || OracledbSchema.present(table, it.getTarget().getField())));
      };
      Iterable<PayloadIndex> _filter_1 = IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_1);
      for (final PayloadIndex declared : _filter_1) {
        Code.Stmt _guarded = OracledbSchema.guarded(OracledbSchema.indexName(c, declared.getName()), this.payloadIndex(c, table, declared));
        result.add(_guarded);
      }
      final Function1<PayloadField, Boolean> _function_2 = (PayloadField it) -> {
        return Boolean.valueOf(OracledbSchema.present(table, it));
      };
      Iterable<PayloadField> _filter_2 = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_2);
      for (final PayloadField field_1 : _filter_2) {
        {
          final OracledbSql.Column column = table.column(ModelUtil.fieldName(field_1));
          if ((column.text() && (!IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
            return Boolean.valueOf(((it.getTarget().getField() == field_1) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
          }))))) {
            Code.Stmt _guarded_1 = OracledbSchema.guarded(OracledbSchema.textIndexName(c, field_1), OracledbSql.createPayloadIndex(table, OracledbSchema.textIndexName(c, field_1), column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null, "text"));
            result.add(_guarded_1);
          }
        }
      }
      if ((fresh || (!ifNotExists))) {
        String _description = c.getDescription();
        boolean _tripleNotEquals_3 = (_description != null);
        if (_tripleNotEquals_3) {
          String _table = OracledbSql.table(table);
          String _plus_2 = ("COMMENT ON TABLE " + _table);
          String _plus_3 = (_plus_2 + " IS ");
          String _literal_1 = OracledbSql.literal(c.getDescription());
          String _plus_4 = (_plus_3 + _literal_1);
          Code.Stmt _execute_1 = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_4)));
          result.add(_execute_1);
        }
        EList<Alias> _aliases = c.getAliases();
        for (final Alias alias : _aliases) {
          String _createView = OracledbSchema.createView(alias.getName(), table);
          Code.Stmt _execute_2 = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)));
          result.add(_execute_2);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * An index created unless an index of that name exists (USER_INDEXES), so that a rerun of an if-not-exists create succeeds.
   */
  public static Code.Stmt guarded(final String index, final String sql) {
    Code.Call _indexExists = OracledbSchema.indexExists(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(index)));
    Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)));
    return new Code.When(_indexExists, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute)));
  }

  /**
   * An index created unless one of the names exists: a CONTEXT index of a column may exist under its implicit name.
   */
  public static Code.Stmt guarded(final List<String> names, final String sql) {
    Code.Call _indexExists = OracledbSchema.indexExists(names);
    Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)));
    return new Code.When(_indexExists, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute)));
  }

  public static Code.Call indexExists(final List<String> names) {
    Code.Call _xblockexpression = null;
    {
      final OracledbSql.Query q = new OracledbSql.Query();
      String _scalar = OracledbQueries.scalar("COUNT(*)");
      String _plus = ("SELECT " + _scalar);
      String _plus_1 = (_plus + " FROM user_indexes WHERE index_name ");
      String _xifexpression = null;
      int _size = names.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        String _bind = q.bind(Code.text(IterableExtensions.<String>head(names)), "string");
        _xifexpression = ("= " + _bind);
      } else {
        final Function1<String, String> _function = (String name) -> {
          return q.bind(Code.text(name), "string");
        };
        String _join = IterableExtensions.join(ListExtensions.<String, String>map(names, _function), ", ");
        String _plus_2 = ("IN (" + _join);
        _xifexpression = (_plus_2 + ")");
      }
      String _plus_3 = (_plus_1 + _xifexpression);
      _xblockexpression = OracledbSchema.call("exists", _plus_3, q, null);
    }
    return _xblockexpression;
  }

  private static boolean present(final OracledbSql.Table table, final Field field) {
    boolean _xblockexpression = false;
    {
      final String name = table.renames().getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field));
      final Function1<OracledbSql.Column, Boolean> _function = (OracledbSql.Column column) -> {
        String _name = column.name();
        return Boolean.valueOf(Objects.equals(_name, name));
      };
      _xblockexpression = IterableExtensions.<OracledbSql.Column>exists(table.columns(), _function);
    }
    return _xblockexpression;
  }

  /**
   * The CONTEXT index of a full-text field without a declared full_text index.
   */
  public static String textIndexName(final VectorCollection c, final PayloadField field) {
    String _fieldName = ModelUtil.fieldName(field);
    String _plus = (_fieldName + "_text");
    return OracledbSchema.indexName(c, _plus);
  }

  /**
   * The Oracle name of a vecDSL index: index names belong to the schema, so the name carries the collection's table name.
   */
  public static String indexName(final VectorCollection c, final String index) {
    String _xblockexpression = null;
    {
      final String database = OracledbSchema.databaseOf(c);
      String _physical = OracledbSchema.physical(database, c.getName());
      String _plus = (_physical + "_");
      final String name = (_plus + index);
      OracledbSql.id(name);
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  private List<String> partitionNames(final VectorCollection c, final OracledbSql.Table table) {
    List<String> _xblockexpression = null;
    {
      final List<String> result = CollectionLiterals.<String>newArrayList();
      String _partition = table.partition();
      boolean _tripleEquals = (_partition == null);
      if (_tripleEquals) {
        return result;
      }
      List<Partition> _partitions = ModelUtil.partitions(c);
      for (final Partition partition : _partitions) {
        {
          if (((partition.getKey() != null) || (ModelUtil.written(partition, "strategy") && (!Objects.equals(partition.getStrategy().getLiteral(), "list"))))) {
            String _literal = partition.getStrategy().getLiteral();
            String _plus = ("partitions by key or with the " + _literal);
            String _plus_1 = (_plus + " strategy; has no Oracle mapping yet (hash and range partitioning)");
            throw Unsupported.of(_plus_1);
          }
          boolean _isEmpty = partition.getSubPartitions().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of("sub-partitions; has no Oracle mapping yet (composite partitioning)");
          }
          OracledbSql.id(partition.getName());
          String _name = partition.getName();
          result.add(_name);
        }
      }
      boolean _contains = result.contains(OracledbSchema.DEFAULT_PARTITION);
      boolean _not = (!_contains);
      if (_not) {
        result.add(OracledbSchema.DEFAULT_PARTITION);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public static String defaultPartition(final VectorCollection c) {
    String _xifexpression = null;
    Partition _defaultPartition = c.getDefaultPartition();
    boolean _tripleNotEquals = (_defaultPartition != null);
    if (_tripleNotEquals) {
      _xifexpression = c.getDefaultPartition().getName();
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  /**
   * The vector index of a field: null for a flat index (exact search needs none).
   */
  public String vectorIndex(final VectorCollection c, final OracledbSql.Table table, final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
      final Map<String, Integer> options = CollectionLiterals.<String, Integer>newLinkedHashMap();
      this.note(index, "onDisk", "online");
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; Oracle builds and searches vector indexes on the database server\'s CPUs");
      }
      Set<String> _keySet = ModelUtil.extParams(index, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      String _literal = index.getFamily().getLiteral();
      boolean _equals = Objects.equals(_literal, "flat");
      if (_equals) {
        if (((index.getQuantization() != null) && (!Objects.equals(index.getQuantization().getType().getLiteral(), "none")))) {
          throw Unsupported.of("quantization of a flat index; Oracle quantizes HNSW vector indexes");
        }
        return null;
      }
      boolean _sparse = column.vec().sparse();
      if (_sparse) {
        String _name = column.name();
        String _plus = ("a vector index on sparse field " + _name);
        String _plus_1 = (_plus + "; has no Oracle mapping yet (sparse vector indexes)");
        throw Unsupported.of(_plus_1);
      }
      String _switchResult = null;
      EIndexFamily _family = index.getFamily();
      if (_family != null) {
        switch (_family) {
          case AUTO:
          case HNSW:
            String _xblockexpression_1 = null;
            {
              boolean _written = ModelUtil.written(index, "centroids");
              if (_written) {
                throw Unsupported.of("centroids on an hnsw index; neighbor partitions belong to ivf indexes");
              }
              boolean _written_1 = ModelUtil.written(index, "maxDegree");
              if (_written_1) {
                options.put("NEIGHBORS", Integer.valueOf(index.getMaxDegree()));
              }
              boolean _written_2 = ModelUtil.written(index, "buildCandidates");
              if (_written_2) {
                options.put("EFCONSTRUCTION", Integer.valueOf(index.getBuildCandidates()));
              }
              _xblockexpression_1 = "hnsw";
            }
            _switchResult = _xblockexpression_1;
            break;
          case IVF:
            String _xblockexpression_2 = null;
            {
              if ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates"))) {
                throw Unsupported.of("max-degree and build-candidates on an ivf index; IVF takes neighbor partitions");
              }
              boolean _written = ModelUtil.written(index, "centroids");
              if (_written) {
                options.put("NEIGHBOR PARTITIONS", Integer.valueOf(index.getCentroids()));
              }
              _xblockexpression_2 = "ivf";
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            String _literal_1 = index.getFamily().getLiteral();
            String _plus_2 = (_literal_1 + " indexes; Oracle builds HNSW (in-memory neighbor graph) and IVF (neighbor partition) vector indexes");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus_2 = (_literal_1 + " indexes; Oracle builds HNSW (in-memory neighbor graph) and IVF (neighbor partition) vector indexes");
        throw Unsupported.of(_plus_2);
      }
      final String family = _switchResult;
      String _xifexpression = null;
      boolean _written = ModelUtil.written(index, "metric");
      if (_written) {
        _xifexpression = index.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      String _name_1 = column.name();
      String _type = column.type();
      OracledbSql.Vec _withMetric = column.vec().withMetric(metric);
      boolean _nullable = column.nullable();
      final OracledbSql.Column indexed = new OracledbSql.Column(_name_1, _type, null, false, _withMetric, null, _nullable, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      _xblockexpression = OracledbSql.createVectorIndex(table, OracledbSchema.indexName(c, index.getName()), indexed, family, options, null, this.compressionOf(index.getQuantization(), family, column.vec().format(), options));
    }
    return _xblockexpression;
  }

  /**
   * Scalar quantization of an HNSW index as the compression ratio of the column's format to the quantized width (AI Vector Search
   * User's Guide, HNSW Index Syntax and Parameters: ratio 2 or 4 for FLOAT32, 2, 4 or 8 for FLOAT64; 8 bits by default); oversampling
   * as RESCORE FACTOR, an integer from 1 to 100. Null without quantization.
   */
  private Integer compressionOf(final Quantization quantization, final String family, final String format, final Map<String, Integer> options) {
    int _xblockexpression = (int) 0;
    {
      if (((quantization == null) || Objects.equals(quantization.getType().getLiteral(), "none"))) {
        return null;
      }
      this.note(quantization, "subvectors", "keepOriginals", "description");
      Set<String> _keySet = ModelUtil.extParams(quantization, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      String _literal = quantization.getType().getLiteral();
      boolean _notEquals = (!Objects.equals(_literal, "scalar"));
      if (_notEquals) {
        String _literal_1 = quantization.getType().getLiteral();
        String _plus = (_literal_1 + " quantization; Oracle quantizes vector indexes with scalar quantization only");
        throw Unsupported.of(_plus);
      }
      boolean _notEquals_1 = (!Objects.equals(family, "hnsw"));
      if (_notEquals_1) {
        throw Unsupported.of("quantization of an ivf index; Oracle quantizes HNSW vector indexes");
      }
      boolean _written = ModelUtil.written(quantization, "truncateDimension");
      if (_written) {
        throw Unsupported.of("truncate-dim; Oracle quantizes every dimension of the vector");
      }
      int _switchResult = (int) 0;
      if (format != null) {
        switch (format) {
          case "FLOAT32":
            _switchResult = 32;
            break;
          case "FLOAT64":
            _switchResult = 64;
            break;
          default:
            _switchResult = 0;
            break;
        }
      } else {
        _switchResult = 0;
      }
      final int source = _switchResult;
      if ((source == 0)) {
        throw Unsupported.of((("quantization of " + format) + " vectors; Oracle quantizes FLOAT32 and FLOAT64 vectors"));
      }
      double _xifexpression = (double) 0;
      boolean _written_1 = ModelUtil.written(quantization, "bits");
      if (_written_1) {
        _xifexpression = quantization.getBits();
      } else {
        _xifexpression = 8.0;
      }
      final double bits = _xifexpression;
      final double ratio = (source / bits);
      if ((((ratio != Math.rint(ratio)) || (!Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(2.0), Double.valueOf(4.0), Double.valueOf(8.0))).contains(Double.valueOf(ratio)))) || ((source == 32) && (ratio == 8.0)))) {
        throw Unsupported.of((((("scalar quantization to " + Double.valueOf(bits)) + " bits of ") + format) + " vectors; Oracle compresses FLOAT32 to 16 or 8 bits and FLOAT64 to 32, 16 or 8 bits"));
      }
      boolean _written_2 = ModelUtil.written(quantization, "oversampling");
      if (_written_2) {
        final double factor = quantization.getOversampling();
        if ((((factor != Math.rint(factor)) || (factor < 1)) || (factor > 100))) {
          throw Unsupported.of((("oversampling " + Double.valueOf(factor)) + "; the Oracle rescore factor is an integer from 1 to 100"));
        }
        options.put("RESCORE FACTOR", Integer.valueOf(((int) factor)));
      }
      _xblockexpression = ((int) ratio);
    }
    return Integer.valueOf(_xblockexpression);
  }

  public String payloadIndex(final VectorCollection c, final OracledbSql.Table table, final PayloadIndex index) {
    String _xblockexpression = null;
    {
      final FieldPath path = index.getTarget();
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields and their sub-fields");
      }
      Field _field = path.getField();
      boolean _not = (!(_field instanceof PayloadField));
      if (_not) {
        throw Unsupported.of("a payload index on a vector field");
      }
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      this.note(index, "onDisk", "lowercase", "prefixMatch", "minTokenLength", "maxTokenLength");
      Set<String> _keySet = ModelUtil.extParams(index, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Function1<Field, String> _function = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      final List<String> segments = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      if ((ModelUtil.written(index, "structure") && Objects.equals(index.getStructure().getLiteral(), "bitmap"))) {
        if (((!segments.isEmpty()) || column.json())) {
          throw Unsupported.of("bitmap indexes on JSON values");
        }
        String _id = OracledbSql.id(OracledbSchema.indexName(c, index.getName()));
        String _plus = ("CREATE BITMAP INDEX " + _id);
        String _plus_1 = (_plus + " ON ");
        String _table = OracledbSql.table(table);
        String _plus_2 = (_plus_1 + _table);
        String _plus_3 = (_plus_2 + " (");
        String _id_1 = OracledbSql.id(column.name());
        String _plus_4 = (_plus_3 + _id_1);
        return (_plus_4 + ")");
      }
      String _switchResult = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case FULL_TEXT:
            String _xblockexpression_1 = null;
            {
              boolean _text = column.text();
              boolean _not_1 = (!_text);
              if (_not_1) {
                String _name = column.name();
                String _plus_5 = ("a full_text index on " + _name);
                String _plus_6 = (_plus_5 + ", which is not a text field");
                throw Unsupported.of(_plus_6);
              }
              _xblockexpression_1 = OracledbSql.createPayloadIndex(table, OracledbSchema.indexName(c, index.getName()), column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null, "text");
            }
            _switchResult = _xblockexpression_1;
            break;
          case GEO:
            throw Unsupported.of("geo payload indexes; has no Oracle mapping yet (Oracle Spatial)");
          case JSON:
            String _xblockexpression_2 = null;
            {
              boolean _json = column.json();
              boolean _not_1 = (!_json);
              if (_not_1) {
                String _name = column.name();
                String _plus_5 = ("a json index on " + _name);
                String _plus_6 = (_plus_5 + ", which is not a json field");
                throw Unsupported.of(_plus_6);
              }
              _xblockexpression_2 = OracledbSql.createPayloadIndex(table, OracledbSchema.indexName(c, index.getName()), column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null, "json");
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            String _xblockexpression_3 = null;
            {
              if (((!segments.isEmpty()) && (!column.json()))) {
                String _name = column.name();
                String _plus_5 = ("a path index below " + _name);
                String _plus_6 = (_plus_5 + ", which is not a json field");
                throw Unsupported.of(_plus_6);
              }
              String _xifexpression = null;
              boolean _isEmpty = segments.isEmpty();
              if (_isEmpty) {
                _xifexpression = null;
              } else {
                _xifexpression = OracledbFilter.leafKind(path);
              }
              final String kind = _xifexpression;
              _xblockexpression_3 = OracledbSql.createPayloadIndex(table, OracledbSchema.indexName(c, index.getName()), column, segments, kind, "auto");
            }
            _switchResult = _xblockexpression_3;
            break;
        }
      } else {
        String _xblockexpression_3 = null;
        {
          if (((!segments.isEmpty()) && (!column.json()))) {
            String _name = column.name();
            String _plus_5 = ("a path index below " + _name);
            String _plus_6 = (_plus_5 + ", which is not a json field");
            throw Unsupported.of(_plus_6);
          }
          String _xifexpression = null;
          boolean _isEmpty = segments.isEmpty();
          if (_isEmpty) {
            _xifexpression = null;
          } else {
            _xifexpression = OracledbFilter.leafKind(path);
          }
          final String kind = _xifexpression;
          _xblockexpression_3 = OracledbSql.createPayloadIndex(table, OracledbSchema.indexName(c, index.getName()), column, segments, kind, "auto");
        }
        _switchResult = _xblockexpression_3;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * An analyzer as a BASIC_LEXER preference (CTX_DDL.CREATE_PREFERENCE): with a language, stemming of that language at index
   * time (INDEX_STEMS); the default English stop list applies to every CONTEXT index.
   */
  public List<Code.Stmt> createAnalyzer(final Analyzer analyzer) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      if (((!Objects.equals(analyzer.getTokenizer().getLiteral(), "standard")) && (!Objects.equals(analyzer.getTokenizer().getLiteral(), "language")))) {
        String _literal = analyzer.getTokenizer().getLiteral();
        String _plus = ("the " + _literal);
        String _plus_1 = (_plus + " tokenizer; has no Oracle mapping yet (lexer preferences other than BASIC_LEXER)");
        throw Unsupported.of(_plus_1);
      }
      if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
        throw Unsupported.of(OracledbStatements.NO_BM25);
      }
      boolean _isEmpty = analyzer.getResources().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("analyzer resources; Oracle Text dictionaries and thesauri are loaded with ctxload, outside SQL");
      }
      String _xifexpression = null;
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = OracledbSchema.stemmer(analyzer.getLanguage());
      }
      final String stems = _xifexpression;
      if (((analyzer.getLanguage() != null) && (stems == null))) {
        String _language_1 = analyzer.getLanguage();
        String _plus_2 = ("the language " + _language_1);
        String _plus_3 = (_plus_2 + "; has no Oracle mapping yet (stemmers other than English)");
        throw Unsupported.of(_plus_3);
      }
      if ((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "english")))) {
        String _stopwords = analyzer.getStopwords();
        String _plus_4 = ("stopwords " + _stopwords);
        String _plus_5 = (_plus_4 + "; has no Oracle mapping yet (stop list preferences)");
        throw Unsupported.of(_plus_5);
      }
      EList<AnalyzerFilter> _filters = analyzer.getFilters();
      for (final AnalyzerFilter filter : _filters) {
        OracledbSchema.checkFilter(filter.getType().getLiteral(), stems);
      }
      final String name = OracledbSchema.lexerName(analyzer);
      final List<String> calls = CollectionLiterals.<String>newArrayList();
      String _literal_1 = OracledbSql.literal(name);
      String _plus_6 = ("CTX_DDL.CREATE_PREFERENCE(" + _literal_1);
      String _plus_7 = (_plus_6 + ", \'BASIC_LEXER\');");
      calls.add(_plus_7);
      if ((stems != null)) {
        String _literal_2 = OracledbSql.literal(name);
        String _plus_8 = ("CTX_DDL.SET_ATTRIBUTE(" + _literal_2);
        String _plus_9 = (_plus_8 + ", \'INDEX_STEMS\', ");
        String _literal_3 = OracledbSql.literal(stems);
        String _plus_10 = (_plus_9 + _literal_3);
        String _plus_11 = (_plus_10 + ");");
        calls.add(_plus_11);
      }
      final OracledbSql.Query q = new OracledbSql.Query();
      String _scalar = OracledbQueries.scalar("COUNT(*)");
      String _plus_12 = ("SELECT " + _scalar);
      String _plus_13 = (_plus_12 + " FROM ctx_user_preferences WHERE pre_name = ");
      String _bind = q.bind(Code.text(name.toUpperCase()), "string");
      final String check = (_plus_13 + _bind);
      Code.Call _call = OracledbSchema.call("exists", check, q, null);
      String _join = IterableExtensions.join(calls, " ");
      String _plus_14 = ("BEGIN " + _join);
      String _plus_15 = (_plus_14 + " END;");
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_15)));
      Code.When _when = new Code.When(_call, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * The stemmer of a language name or code; Oracle Text's BASIC_LEXER stems English here.
   */
  public static String stemmer(final String language) {
    String _xblockexpression = null;
    {
      final String lower = language.toLowerCase();
      String _xifexpression = null;
      if (((Objects.equals(lower, "en") || Objects.equals(lower, "english")) || lower.startsWith("en-"))) {
        _xifexpression = "ENGLISH";
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * Filters a lexer preference applies: lowercase always (BASIC_LEXER is case-insensitive by default), stop and stemmer with a language.
   */
  public static void checkFilter(final String filter, final String stems) {
    if (filter != null) {
      switch (filter) {
        case "lowercase":
          break;
        case "stop":
          break;
        case "stemmer":
          if ((stems == null)) {
            throw Unsupported.of("the stemmer filter without a language; BASIC_LEXER stems with INDEX_STEMS of a language");
          }
          break;
        case "ascii_folding":
          throw Unsupported.of("the ascii_folding filter; has no Oracle mapping yet (BASE_LETTER of BASIC_LEXER)");
        default:
          throw Unsupported.of((("the " + filter) + " filter; has no Oracle mapping yet (Oracle Text lexer attributes)"));
      }
    } else {
      throw Unsupported.of((("the " + filter) + " filter; has no Oracle mapping yet (Oracle Text lexer attributes)"));
    }
  }

  public static String createView(final String alias, final OracledbSql.Table table) {
    String _id = OracledbSql.id(alias);
    String _plus = ("CREATE OR REPLACE VIEW " + _id);
    String _plus_1 = (_plus + " AS SELECT * FROM ");
    String _table = OracledbSql.table(table);
    return (_plus_1 + _table);
  }

  /**
   * One runtime call with SQL text and parameters (renumbered in textual order); settings only when present.
   */
  public static Code.Call call(final String op, final String sql, final OracledbSql.Query q, final Map<String, String> settings) {
    Code.Call _xblockexpression = null;
    {
      Code.Items _xifexpression = null;
      if ((q == null)) {
        _xifexpression = Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList()));
      } else {
        _xifexpression = q.params();
      }
      final OracledbSql.Sql finished = OracledbSql.finish(sql, _xifexpression);
      final Code.Entries args = OracledbSchema.kw("sql", Code.text(finished.text()));
      boolean _isEmpty = finished.params().items().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("params", finished.params());
      }
      if (((settings != null) && (!settings.isEmpty()))) {
        final Code.Entries values = Code.entries();
        final BiConsumer<String, String> _function = (String name, String value) -> {
          values.put(name, Code.text(value));
        };
        settings.forEach(_function);
        args.put("settings", values);
      }
      _xblockexpression = Code.runtime(op, args);
    }
    return _xblockexpression;
  }

  /**
   * DDL or PL/SQL statements without parameters, run in order (Oracle commits each DDL statement).
   */
  public static Code.Stmt execute(final List<String> statements) {
    final Function1<String, Code.Expr> _function = (String sql) -> {
      Code.Entries _kw = OracledbSchema.kw("sql", Code.text(sql));
      return ((Code.Expr) _kw);
    };
    Code.Call _runtime = Code.runtime("execute", OracledbSchema.kw("steps", Code.items(IterableExtensions.<Code.Expr>toList(IterableExtensions.<String, Code.Expr>map(IterableExtensions.<String>filterNull(statements), _function)))));
    return new Code.Run(_runtime);
  }

  public void note(final EObject host, final String... features) {
    for (final String feature : features) {
      boolean _written = ModelUtil.written(host, feature);
      if (_written) {
        String _name = host.eClass().getName();
        String _plus = ((feature + " on ") + _name);
        this.notes.add(_plus);
      }
    }
  }

  public static Code.Entries kw(final Object... pairs) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      {
        int i = 0;
        int _length = pairs.length;
        boolean _lessThan = (i < _length);
        boolean _while = _lessThan;
        while (_while) {
          Object _get = pairs[i];
          Object _get_1 = pairs[(i + 1)];
          result.put(((String) _get), ((Code.Expr) _get_1));
          int _i = i;
          i = (_i + 2);
          int _length_1 = pairs.length;
          boolean _lessThan_1 = (i < _length_1);
          _while = _lessThan_1;
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }
}
