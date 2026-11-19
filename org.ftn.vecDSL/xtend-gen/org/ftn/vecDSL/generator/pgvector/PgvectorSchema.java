package org.ftn.vecDSL.generator.pgvector;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.ftn.vecDSL.vecDSL.EQuantizationKind;
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
 * Maps vecDSL collections to PostgreSQL tables with pgvector columns (server 18, pgvector 0.8.7). Every field is a typed
 * column in declaration order: payload fields as SQL types (nested and json fields as jsonb, arrays as SQL arrays), vector
 * fields as vector, halfvec, bit or sparsevec columns with their dimension, each with its own index. The primary key is the
 * key column; tenant, namespace and partition names live in the partition-key field or in hidden text columns.
 */
@SuppressWarnings("all")
public class PgvectorSchema {
  public static final String TENANT_COLUMN = "vecdsl_tenant";

  public static final String NAMESPACE_COLUMN = "vecdsl_namespace";

  public static final String PARTITION_COLUMN = "vecdsl_partition";

  public static final String DEFAULT_PARTITION = "default";

  private static final Map<EScalarDType, String> SCALARS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "integer"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "bigint"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "smallint"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "real"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "double precision"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "boolean"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "text"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "text"), Pair.<EScalarDType, String>of(EScalarDType.DATE, "date"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "timestamp"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "timestamptz"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "uuid"), Pair.<EScalarDType, String>of(EScalarDType.BYTES, "bytea"), Pair.<EScalarDType, String>of(EScalarDType.JSON, "jsonb"), Pair.<EScalarDType, String>of(EScalarDType.MAP, "jsonb"), Pair.<EScalarDType, String>of(EScalarDType.STRUCT, "jsonb")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted;

  private final Set<VectorCollection> namespaced;

  private final Map<VectorCollection, PgvectorSql.Table> evolved = CollectionLiterals.<VectorCollection, PgvectorSql.Table>newHashMap();

  public PgvectorSchema(final List<String> notes, final Script script) {
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
   * The table of a collection as the statements so far leave it: the declaration, changed by the alter statements mapped
   * before (added, dropped and renamed columns, a new table name). The same descriptor serves DDL, writes and queries.
   */
  public PgvectorSql.Table table(final VectorCollection c) {
    PgvectorSql.Table _elvis = null;
    PgvectorSql.Table _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      PgvectorSql.Table _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  /**
   * Records the table an alter statement leaves; create and drop return the collection to its declaration.
   */
  public void evolve(final VectorCollection c, final PgvectorSql.Table table) {
    if ((table == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, table);
    }
  }

  private PgvectorSql.Table declared(final VectorCollection c) {
    PgvectorSql.Table _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!PgvectorSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys tables by an integer, text or uuid column");
        throw Unsupported.of(_plus_1);
      }
      final List<PgvectorSql.Column> columns = CollectionLiterals.<PgvectorSql.Column>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        PgvectorSql.Column _column = this.column(c, field);
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
          _xifexpression_2 = PgvectorSchema.TENANT_COLUMN;
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
          _xifexpression_4 = PgvectorSchema.NAMESPACE_COLUMN;
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
        _xifexpression_5 = PgvectorSchema.PARTITION_COLUMN;
      }
      final String partition = _xifexpression_5;
      String _databaseOf = PgvectorSchema.databaseOf(c);
      String _name = c.getName();
      String _fieldName = ModelUtil.fieldName(key);
      Map<String, String> _emptyMap = CollectionLiterals.<String, String>emptyMap();
      _xblockexpression = new PgvectorSql.Table(_databaseOf, _name, _fieldName, columns, tenant, namespace, partition, _emptyMap);
    }
    return _xblockexpression;
  }

  /**
   * The PostgreSQL database of a collection declared in a logical-db, else null for the program's database.
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      if ((database == null)) {
        return null;
      }
      PgvectorSql.id(database.getName());
      _xblockexpression = database.getName();
    }
    return _xblockexpression;
  }

  public PgvectorSql.Column column(final VectorCollection c, final Field field) {
    PgvectorSql.Column _switchResult = null;
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

  private PgvectorSql.Column payloadColumn(final VectorCollection c, final PayloadField field) {
    PgvectorSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      PgvectorSql.id(name);
      this.note(field, "nullable");
      final boolean nested = ((!field.getSubFields().isEmpty()) || field.isNested());
      final String config = this.textConfig(c, field);
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = PgvectorSchema.check(limit, field);
        checks.add(_check);
      }
      if ((((nested || Objects.equals(field.getType(), EScalarDType.JSON)) || Objects.equals(field.getType(), EScalarDType.MAP)) || Objects.equals(field.getType(), EScalarDType.STRUCT))) {
        if ((config != null)) {
          throw Unsupported.of(("full-text search on json field " + name));
        }
        Code.Expr _defaultOf = PgvectorSchema.defaultOf(field);
        return new PgvectorSql.Column(name, "jsonb", null, true, null, null, true, _defaultOf, checks);
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
        if ((Objects.equals(element, "jsonb") || Objects.equals(element, "bytea"))) {
          String _literal = field.getElementType().getLiteral();
          String _plus = ("arrays of " + _literal);
          String _plus_1 = (_plus + " in field ");
          String _plus_2 = (_plus_1 + name);
          throw Unsupported.of(_plus_2);
        }
        Code.Expr _defaultOf_1 = PgvectorSchema.defaultOf(field);
        return new PgvectorSql.Column(name, (element + "[]"), element, false, null, null, true, _defaultOf_1, checks);
      }
      final String base = this.scalar(field.getType(), name);
      String _xifexpression = null;
      if ((ModelUtil.written(field, "maxLength") && Objects.equals(base, "text"))) {
        int _maxLength = field.getMaxLength();
        String _plus_3 = ("varchar(" + Integer.valueOf(_maxLength));
        _xifexpression = (_plus_3 + ")");
      } else {
        _xifexpression = base;
      }
      final String sqlType = _xifexpression;
      if (((config != null) && (!Objects.equals(base, "text")))) {
        String _literal_1 = field.getType().getLiteral();
        String _plus_4 = ("full-text search on " + _literal_1);
        String _plus_5 = (_plus_4 + " field ");
        String _plus_6 = (_plus_5 + name);
        throw Unsupported.of(_plus_6);
      }
      Code.Expr _defaultOf_2 = PgvectorSchema.defaultOf(field);
      _xblockexpression = new PgvectorSql.Column(name, sqlType, null, false, null, config, true, _defaultOf_2, checks);
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
    final String sql = PgvectorSchema.SCALARS.get(type);
    if ((sql != null)) {
      return sql;
    }
    if (type != null) {
      switch (type) {
        case GEO_POINT:
          throw Unsupported.of((("geo_point field " + field) + "; has no pgvector mapping yet (earthdistance in contrib)"));
        case GEOMETRY:
          throw Unsupported.of((("geometry field " + field) + "; PostgreSQL has no geometry type without PostGIS, which the pgvector image does not include"));
        default:
          String _literal = type.getLiteral();
          String _plus = (_literal + " field ");
          String _plus_1 = (_plus + field);
          String _plus_2 = (_plus_1 + "; PostgreSQL has no media types (bytea holds bytes without media semantics)");
          throw Unsupported.of(_plus_2);
      }
    } else {
      String _literal = type.getLiteral();
      String _plus = (_literal + " field ");
      String _plus_1 = (_plus + field);
      String _plus_2 = (_plus_1 + "; PostgreSQL has no media types (bytea holds bytes without media semantics)");
      throw Unsupported.of(_plus_2);
    }
  }

  /**
   * A field limit as a named CHECK constraint; {} stands for the column (pgvector checks vector dimensions in the type).
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
              _xblockexpression_1 = ("cardinality({}) <= " + Integer.valueOf(_value));
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
              _xblockexpression_2 = ("octet_length({}::text) <= " + Integer.valueOf(_value));
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
              _xblockexpression_3 = ("vector_dims({}) <= " + Integer.valueOf(_value));
            }
            _switchResult = _xblockexpression_3;
            break;
          default:
            break;
        }
      }
      final String condition = _switchResult;
      String _id = PgvectorSql.id(limit.getName());
      String _plus = ("CONSTRAINT " + _id);
      String _plus_1 = (_plus + " CHECK (");
      String _plus_2 = (_plus_1 + condition);
      _xblockexpression = (_plus_2 + ")");
    }
    return _xblockexpression;
  }

  /**
   * The text search configuration of a full-text field: its analyzer's, else simple (lowercased words, no stemming).
   */
  public String textConfig(final VectorCollection c, final PayloadField field) {
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
        _xifexpression = "simple";
      } else {
        _xifexpression = PgvectorSchema.configName(analyzer);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * An analyzer is a text search configuration in the public schema, named like the analyzer.
   */
  public static String configName(final Analyzer analyzer) {
    String _id = PgvectorSql.id(analyzer.getName());
    return ("public." + _id);
  }

  private PgvectorSql.Column vectorColumn(final VectorCollection c, final VectorField field) {
    PgvectorSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      PgvectorSql.id(name);
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; pgvector stores one vector per column"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + PgvectorStatements.NO_MODELS));
      }
      if ((ModelUtil.written(field, "modifier") && Objects.equals(field.getModifier().getLiteral(), "idf"))) {
        throw Unsupported.of((("the idf modifier on " + name) + "; pgvector stores sparse vectors as given"));
      }
      this.note(field, "normalized", "onDisk");
      String _switchResult = null;
      EVectorDType _type = field.getType();
      if (_type != null) {
        switch (_type) {
          case FLOAT_VECTOR:
            _switchResult = "vector";
            break;
          case FLOAT16_VECTOR:
            _switchResult = "halfvec";
            break;
          case BINARY_VECTOR:
            _switchResult = "bit";
            break;
          case SPARSE_FLOAT_VECTOR:
            _switchResult = "sparsevec";
            break;
          default:
            String _literal = field.getType().getLiteral();
            String _plus = (_literal + " fields; pgvector stores vector (float32), halfvec (float16), bit and sparsevec values");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " fields; pgvector stores vector (float32), halfvec (float16), bit and sparsevec values");
        throw Unsupported.of(_plus);
      }
      final String vectorType = _switchResult;
      if (((!Objects.equals(vectorType, "sparsevec")) && (field.getDimension() <= 0))) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      final VectorIndex index = this.indexOf(c, field);
      final String metric = this.metric(field, index);
      String quantization = null;
      double oversampling = 1.0;
      if (((index != null) && (index.getQuantization() != null))) {
        final Quantization q = index.getQuantization();
        this.note(q, "keepOriginals", "truncateDimension");
        String _switchResult_1 = null;
        EQuantizationKind _type_1 = q.getType();
        if (_type_1 != null) {
          switch (_type_1) {
            case NONE:
              _switchResult_1 = null;
              break;
            case BINARY:
              String _xblockexpression_1 = null;
              {
                if ((ModelUtil.written(q, "bits") && (q.getBits() != 1))) {
                  double _bits = q.getBits();
                  String _plus_1 = ("binary quantization with " + Double.valueOf(_bits));
                  String _plus_2 = (_plus_1 + " bits; binary_quantize keeps one bit per dimension");
                  throw Unsupported.of(_plus_2);
                }
                _xblockexpression_1 = "binary";
              }
              _switchResult_1 = _xblockexpression_1;
              break;
            case SCALAR:
              String _xblockexpression_2 = null;
              {
                if (((!ModelUtil.written(q, "bits")) || (q.getBits() != 16))) {
                  String _xifexpression = null;
                  boolean _written = ModelUtil.written(q, "bits");
                  if (_written) {
                    double _bits = q.getBits();
                    _xifexpression = (Double.valueOf(_bits) + " bits");
                  } else {
                    _xifexpression = "the default width";
                  }
                  String _plus_1 = ("scalar quantization to " + _xifexpression);
                  String _plus_2 = (_plus_1 + "; pgvector quantizes to halfvec (16 bits) and to bits only");
                  throw Unsupported.of(_plus_2);
                }
                _xblockexpression_2 = "halfvec";
              }
              _switchResult_1 = _xblockexpression_2;
              break;
            case PRODUCT:
              throw Unsupported.of("product quantization; pgvector has no product quantization");
            default:
              break;
          }
        }
        quantization = _switchResult_1;
        if (((quantization != null) && (!Objects.equals(vectorType, "vector")))) {
          throw Unsupported.of((((("quantization of " + vectorType) + " field ") + name) + "; pgvector quantizes vector columns"));
        }
        if ((((Objects.equals(quantization, "binary") && (!Objects.equals(metric, "euclidean"))) && (!Objects.equals(metric, "cosine"))) && (!Objects.equals(metric, "dot")))) {
          throw Unsupported.of((("binary quantization with the " + metric) + " metric"));
        }
        boolean _written = ModelUtil.written(q, "oversampling");
        if (_written) {
          oversampling = q.getOversampling();
        } else {
          if ((quantization != null)) {
            oversampling = 4.0;
          }
        }
      }
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = PgvectorSchema.check(limit, field);
        checks.add(_check);
      }
      int _dimension = field.getDimension();
      PgvectorSql.Vec _vec = new PgvectorSql.Vec(vectorType, _dimension, metric, quantization, oversampling);
      boolean _isNullable = field.isNullable();
      _xblockexpression = new PgvectorSql.Column(name, vectorType, null, false, _vec, null, _isNullable, null, checks);
    }
    return _xblockexpression;
  }

  /**
   * The vector index of a field: declared on the field or in the collection.
   */
  public VectorIndex indexOf(final VectorCollection c, final VectorField field) {
    return ModelUtil.vectorIndex(c, field);
  }

  /**
   * The metric of a vector field: the field's, else its index's, else cosine.
   */
  public String metric(final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(field, "metric");
      if (_written) {
        _xifexpression = field.getMetric();
      } else {
        EMetric _xifexpression_1 = null;
        if (((index != null) && ModelUtil.written(index, "metric"))) {
          _xifexpression_1 = index.getMetric();
        } else {
          _xifexpression_1 = EMetric.COSINE;
        }
        _xifexpression = _xifexpression_1;
      }
      final EMetric metric = _xifexpression;
      _xblockexpression = metric.getLiteral();
    }
    return _xblockexpression;
  }

  /**
   * A collection's table with its objects. A declaration (fresh) starts from the declared fields; a create that names an
   * existing collection keeps the columns earlier alter statements changed, and skips fields and indexes they dropped.
   */
  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(PgvectorStatements.EXTERNAL);
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(PgvectorStatements.NO_TTL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; has no pgvector mapping yet (identity columns and gen_random_uuid)");
      }
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        throw Unsupported.of("dynamic; has no pgvector mapping yet (a jsonb column for undeclared fields)");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(PgvectorStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; has no pgvector mapping yet (foreign keys)");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + PgvectorStatements.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of(PgvectorStatements.NO_STORAGE);
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(PgvectorStatements.NO_TAGS);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = PgvectorStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        throw Unsupported.of(PgvectorStatements.collectionLimit(IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral()));
      }
      Set<String> _keySet = ModelUtil.extParams(c, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "consistency");
      if (fresh) {
        this.evolve(c, null);
      }
      final PgvectorSql.Table table = this.table(c);
      final String database = table.database();
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<Analyzer> _analyzers = c.getAnalyzers();
      for (final Analyzer analyzer : _analyzers) {
        List<Code.Stmt> _createAnalyzer = this.createAnalyzer(analyzer, database);
        Iterables.<Code.Stmt>addAll(result, _createAnalyzer);
      }
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      String _createTable = PgvectorSql.createTable(table, ifNotExists);
      ddl.add(_createTable);
      List<String> _partitions = this.partitions(c, table, ifNotExists);
      Iterables.<String>addAll(ddl, _partitions);
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        return Boolean.valueOf(PgvectorSchema.present(table, it));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function);
      for (final VectorField field : _filter) {
        {
          final VectorIndex index = this.indexOf(c, field);
          if ((index != null)) {
            String _vectorIndex = this.vectorIndex(c, table, field, index, ifNotExists);
            ddl.add(_vectorIndex);
          }
        }
      }
      final Function1<PayloadIndex, Boolean> _function_1 = (PayloadIndex it) -> {
        return Boolean.valueOf(((it.getTarget().getField() == null) || PgvectorSchema.present(table, it.getTarget().getField())));
      };
      Iterable<PayloadIndex> _filter_1 = IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_1);
      for (final PayloadIndex declared : _filter_1) {
        String _payloadIndex = this.payloadIndex(c, table, declared, ifNotExists);
        ddl.add(_payloadIndex);
      }
      final Function1<PayloadField, Boolean> _function_2 = (PayloadField it) -> {
        return Boolean.valueOf(PgvectorSchema.present(table, it));
      };
      Iterable<PayloadField> _filter_2 = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_2);
      for (final PayloadField field_1 : _filter_2) {
        {
          final String config = table.column(ModelUtil.fieldName(field_1)).config();
          if (((config != null) && (!IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
            return Boolean.valueOf(((it.getTarget().getField() == field_1) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
          }))))) {
            String _createPayloadIndex = PgvectorSql.createPayloadIndex(table, PgvectorSchema.ftsIndexName(c, field_1), table.column(ModelUtil.fieldName(field_1)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), "fts", true);
            ddl.add(_createPayloadIndex);
          }
        }
      }
      if ((fresh || (!ifNotExists))) {
        String _description = c.getDescription();
        boolean _tripleNotEquals_3 = (_description != null);
        if (_tripleNotEquals_3) {
          String _table = PgvectorSql.table(table);
          String _plus_2 = ("COMMENT ON TABLE " + _table);
          String _plus_3 = (_plus_2 + " IS ");
          String _literal_1 = PgvectorSql.literal(c.getDescription());
          String _plus_4 = (_plus_3 + _literal_1);
          ddl.add(_plus_4);
        }
        EList<Alias> _aliases = c.getAliases();
        for (final Alias alias : _aliases) {
          String _createView = PgvectorSchema.createView(alias.getName(), table, ifNotExists);
          ddl.add(_createView);
        }
      }
      Code.Stmt _execute = PgvectorSchema.execute(ddl, database);
      result.add(_execute);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Whether a field still has its column (an alter statement may have dropped it).
   */
  private static boolean present(final PgvectorSql.Table table, final Field field) {
    boolean _xblockexpression = false;
    {
      final String name = table.renames().getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field));
      final Function1<PgvectorSql.Column, Boolean> _function = (PgvectorSql.Column column) -> {
        String _name = column.name();
        return Boolean.valueOf(Objects.equals(_name, name));
      };
      _xblockexpression = IterableExtensions.<PgvectorSql.Column>exists(table.columns(), _function);
    }
    return _xblockexpression;
  }

  /**
   * The GIN index of a full-text field without a declared full_text index.
   */
  public static String ftsIndexName(final VectorCollection c, final PayloadField field) {
    String _fieldName = ModelUtil.fieldName(field);
    String _plus = (_fieldName + "_fts");
    return PgvectorSchema.indexName(c, _plus);
  }

  /**
   * The PostgreSQL name of a vecDSL index: PostgreSQL scopes index names to the schema, vecDSL to the collection, so the name
   * carries the collection's declared name (a renamed table keeps the names of its indexes).
   */
  public static String indexName(final VectorCollection c, final String index) {
    String _xblockexpression = null;
    {
      String _name = c.getName();
      String _plus = (_name + "_");
      final String name = (_plus + index);
      int _length = name.length();
      boolean _greaterThan = (_length > 63);
      if (_greaterThan) {
        throw Unsupported.of((("the index name " + name) + "; PostgreSQL names have at most 63 bytes"));
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  private List<String> partitions(final VectorCollection c, final PgvectorSql.Table table, final boolean ifNotExists) {
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
            String _plus_1 = (_plus + " strategy; has no pgvector mapping yet (declarative hash and range partitioning)");
            throw Unsupported.of(_plus_1);
          }
          boolean _isEmpty = partition.getSubPartitions().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of("sub-partitions; has no pgvector mapping yet (sub-partitioned tables)");
          }
          String _createPartition = PgvectorSql.createPartition(table, partition.getName(), false, ifNotExists);
          result.add(_createPartition);
        }
      }
      String _createPartition = PgvectorSql.createPartition(table, PgvectorSchema.DEFAULT_PARTITION, true, ifNotExists);
      result.add(_createPartition);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The partition a write goes to without a partition clause: the collection's default-partition, else the default partition.
   */
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

  public String vectorIndex(final VectorCollection c, final PgvectorSql.Table table, final VectorField field, final VectorIndex index, final boolean ifNotExists) {
    String _xblockexpression = null;
    {
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      final Map<String, Integer> options = CollectionLiterals.<String, Integer>newLinkedHashMap();
      this.note(index, "onDisk", "online");
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; pgvector builds and searches indexes on the CPU");
      }
      Set<String> _keySet = ModelUtil.extParams(index, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      String _literal = index.getFamily().getLiteral();
      boolean _equals = Objects.equals(_literal, "flat");
      if (_equals) {
        Quantization _quantization = index.getQuantization();
        boolean _tripleNotEquals = (_quantization != null);
        if (_tripleNotEquals) {
          throw Unsupported.of("quantization without an index; pgvector quantizes through hnsw and ivfflat expression indexes");
        }
        return null;
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
                throw Unsupported.of("centroids on an hnsw index; lists belong to ivf indexes");
              }
              boolean _written_1 = ModelUtil.written(index, "maxDegree");
              if (_written_1) {
                options.put("m", Integer.valueOf(index.getMaxDegree()));
              }
              boolean _written_2 = ModelUtil.written(index, "buildCandidates");
              if (_written_2) {
                options.put("ef_construction", Integer.valueOf(index.getBuildCandidates()));
              }
              _xblockexpression_1 = "hnsw";
            }
            _switchResult = _xblockexpression_1;
            break;
          case IVF:
            String _xblockexpression_2 = null;
            {
              if ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates"))) {
                throw Unsupported.of("max-degree and build-candidates on an ivf index; IVFFlat takes lists only");
              }
              boolean _written = ModelUtil.written(index, "centroids");
              if (_written) {
                options.put("lists", Integer.valueOf(index.getCentroids()));
              }
              _xblockexpression_2 = "ivfflat";
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            String _literal_1 = index.getFamily().getLiteral();
            String _plus = (_literal_1 + " indexes; pgvector builds hnsw and ivfflat indexes");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus = (_literal_1 + " indexes; pgvector builds hnsw and ivfflat indexes");
        throw Unsupported.of(_plus);
      }
      final String method = _switchResult;
      String _xifexpression = null;
      boolean _written = ModelUtil.written(index, "metric");
      if (_written) {
        _xifexpression = index.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      String _name = column.name();
      String _type = column.type();
      String _type_1 = column.vec().type();
      int _dim = column.vec().dim();
      String _quantization_1 = column.vec().quantization();
      double _oversampling = column.vec().oversampling();
      PgvectorSql.Vec _vec = new PgvectorSql.Vec(_type_1, _dim, metric, _quantization_1, _oversampling);
      boolean _nullable = column.nullable();
      final PgvectorSql.Column indexed = new PgvectorSql.Column(_name, _type, null, false, _vec, null, _nullable, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      _xblockexpression = PgvectorSql.createVectorIndex(table, PgvectorSchema.indexName(c, index.getName()), indexed, method, options, ifNotExists);
    }
    return _xblockexpression;
  }

  public String payloadIndex(final VectorCollection c, final PgvectorSql.Table table, final PayloadIndex index, final boolean ifNotExists) {
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
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      this.note(index, "onDisk", "lowercase", "prefixMatch", "minTokenLength", "maxTokenLength");
      Set<String> _keySet = ModelUtil.extParams(index, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      if ((ModelUtil.written(index, "structure") && Objects.equals(index.getStructure().getLiteral(), "bitmap"))) {
        throw Unsupported.of("bitmap payload indexes; PostgreSQL builds bitmaps at query time and has no bitmap index type");
      }
      final Function1<Field, String> _function = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      final List<String> segments = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      String _switchResult = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case FULL_TEXT:
            String _xblockexpression_1 = null;
            {
              String _config = column.config();
              boolean _tripleEquals = (_config == null);
              if (_tripleEquals) {
                String _name = column.name();
                String _plus = ("a full_text index on " + _name);
                String _plus_1 = (_plus + ", which is not a text field");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = PgvectorSql.createPayloadIndex(table, PgvectorSchema.indexName(c, index.getName()), column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), "fts", ifNotExists);
            }
            _switchResult = _xblockexpression_1;
            break;
          case GEO:
            throw Unsupported.of("geo payload indexes; has no pgvector mapping yet (earthdistance in contrib)");
          case JSON:
            String _xblockexpression_2 = null;
            {
              boolean _json = column.json();
              boolean _not_1 = (!_json);
              if (_not_1) {
                String _name = column.name();
                String _plus = ("a json index on " + _name);
                String _plus_1 = (_plus + ", which is not a json field");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_2 = PgvectorSql.createPayloadIndex(table, PgvectorSchema.indexName(c, index.getName()), column, segments, "gin", ifNotExists);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            String _xblockexpression_3 = null;
            {
              if (((!segments.isEmpty()) && (!column.json()))) {
                String _name = column.name();
                String _plus = ("a path index below " + _name);
                String _plus_1 = (_plus + ", which is not a json field");
                throw Unsupported.of(_plus_1);
              }
              String _indexName = PgvectorSchema.indexName(c, index.getName());
              String _xifexpression = null;
              boolean _written = ModelUtil.written(index, "structure");
              if (_written) {
                _xifexpression = index.getStructure().getLiteral();
              } else {
                _xifexpression = "auto";
              }
              _xblockexpression_3 = PgvectorSql.createPayloadIndex(table, _indexName, column, segments, _xifexpression, ifNotExists);
            }
            _switchResult = _xblockexpression_3;
            break;
        }
      } else {
        String _xblockexpression_3 = null;
        {
          if (((!segments.isEmpty()) && (!column.json()))) {
            String _name = column.name();
            String _plus = ("a path index below " + _name);
            String _plus_1 = (_plus + ", which is not a json field");
            throw Unsupported.of(_plus_1);
          }
          String _indexName = PgvectorSchema.indexName(c, index.getName());
          String _xifexpression = null;
          boolean _written = ModelUtil.written(index, "structure");
          if (_written) {
            _xifexpression = index.getStructure().getLiteral();
          } else {
            _xifexpression = "auto";
          }
          _xblockexpression_3 = PgvectorSql.createPayloadIndex(table, _indexName, column, segments, _xifexpression, ifNotExists);
        }
        _switchResult = _xblockexpression_3;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A text search configuration copied from the built-in configuration of the analyzer's language (simple without one).
   */
  public List<Code.Stmt> createAnalyzer(final Analyzer analyzer, final String database) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      if (((!Objects.equals(analyzer.getTokenizer().getLiteral(), "standard")) && (!Objects.equals(analyzer.getTokenizer().getLiteral(), "language")))) {
        String _literal = analyzer.getTokenizer().getLiteral();
        String _plus = ("the " + _literal);
        String _plus_1 = (_plus + " tokenizer; PostgreSQL ships one text search parser (default), which splits words like the standard tokenizer");
        throw Unsupported.of(_plus_1);
      }
      if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
        throw Unsupported.of(PgvectorStatements.NO_BM25);
      }
      boolean _isEmpty = analyzer.getResources().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("analyzer resources; text search dictionaries read files from the server\'s tsearch_data directory, which the driver cannot write");
      }
      String _xifexpression = null;
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression = "simple";
      } else {
        _xifexpression = PgvectorSql.configuration(analyzer.getLanguage());
      }
      final String source = _xifexpression;
      if ((source == null)) {
        String _language_1 = analyzer.getLanguage();
        String _plus_2 = ("the language " + _language_1);
        String _plus_3 = (_plus_2 + "; PostgreSQL has no built-in text search configuration for it");
        throw Unsupported.of(_plus_3);
      }
      if (((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), source))) && (!Objects.equals(analyzer.getStopwords(), "none")))) {
        String _stopwords = analyzer.getStopwords();
        String _plus_4 = ("stopwords " + _stopwords);
        String _plus_5 = (_plus_4 + "; a text search configuration uses the stop-word list of its language");
        throw Unsupported.of(_plus_5);
      }
      if ((Objects.equals(analyzer.getStopwords(), "none") && (!Objects.equals(source, "simple")))) {
        throw Unsupported.of("stopwords none with a language; the snowball dictionaries of PostgreSQL remove the stop words of their language");
      }
      EList<AnalyzerFilter> _filters = analyzer.getFilters();
      for (final AnalyzerFilter filter : _filters) {
        PgvectorSchema.checkFilter(filter.getType().getLiteral(), source);
      }
      String _id = PgvectorSql.id(analyzer.getName());
      String _plus_6 = ("CREATE TEXT SEARCH CONFIGURATION public." + _id);
      String _plus_7 = (_plus_6 + " (COPY = pg_catalog.");
      String _plus_8 = (_plus_7 + source);
      final String create = (_plus_8 + ")");
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.text(analyzer.getName()), "text");
      String _plus_9 = ("SELECT to_json(EXISTS (SELECT 1 FROM pg_ts_config c JOIN pg_namespace n ON n.oid = c.cfgnamespace WHERE n.nspname = \'public\' AND c.cfgname = " + _bind);
      final String check = (_plus_9 + "))");
      Code.Call _call = PgvectorSchema.call("exists", check, q, null, database);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(create)), database);
      Code.When _when = new Code.When(_call, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Filters a copied configuration already applies: lowercase always, stop and stemmer with a language.
   */
  public static void checkFilter(final String filter, final String source) {
    if (filter != null) {
      switch (filter) {
        case "lowercase":
          break;
        case "stop":
        case "stemmer":
          boolean _equals = Objects.equals(source, "simple");
          if (_equals) {
            throw Unsupported.of((("the " + filter) + " filter without a language; the simple configuration only lowercases"));
          }
          break;
        case "ascii_folding":
          throw Unsupported.of("the ascii_folding filter; has no pgvector mapping yet (the unaccent dictionary in contrib)");
        default:
          throw Unsupported.of((("the " + filter) + " filter; PostgreSQL text search dictionaries for it read files from the server\'s tsearch_data directory"));
      }
    } else {
      throw Unsupported.of((("the " + filter) + " filter; PostgreSQL text search dictionaries for it read files from the server\'s tsearch_data directory"));
    }
  }

  public static String createView(final String alias, final PgvectorSql.Table table, final boolean ifNotExists) {
    String _xifexpression = null;
    if (ifNotExists) {
      _xifexpression = "OR REPLACE ";
    } else {
      _xifexpression = "";
    }
    String _plus = ("CREATE " + _xifexpression);
    String _plus_1 = (_plus + "VIEW ");
    String _id = PgvectorSql.id(alias);
    String _plus_2 = (_plus_1 + _id);
    String _plus_3 = (_plus_2 + " AS SELECT * FROM ");
    String _table = PgvectorSql.table(table);
    return (_plus_3 + _table);
  }

  /**
   * One runtime call with SQL text and parameters; settings and database only when present.
   */
  public static Code.Call call(final String op, final String sql, final PgvectorSql.Query q, final Map<String, String> settings, final String database) {
    Code.Call _xblockexpression = null;
    {
      final Code.Entries args = PgvectorSchema.kw("sql", Code.text(sql));
      if (((q != null) && (q.size() > 0))) {
        args.put("params", q.params());
      }
      if (((settings != null) && (!settings.isEmpty()))) {
        args.put("settings", PgvectorSql.settings(settings));
      }
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      _xblockexpression = Code.runtime(op, args);
    }
    return _xblockexpression;
  }

  /**
   * DDL statements in one transaction.
   */
  public static Code.Stmt execute(final List<String> statements, final String database) {
    Code.Run _xblockexpression = null;
    {
      final Function1<String, Code.Expr> _function = (String sql) -> {
        Code.Entries _kw = PgvectorSchema.kw("sql", Code.text(sql));
        return ((Code.Expr) _kw);
      };
      final Code.Entries args = PgvectorSchema.kw("steps", Code.items(IterableExtensions.<Code.Expr>toList(IterableExtensions.<String, Code.Expr>map(IterableExtensions.<String>filterNull(statements), _function))));
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      Code.Call _runtime = Code.runtime("execute", args);
      _xblockexpression = new Code.Run(_runtime);
    }
    return _xblockexpression;
  }

  /**
   * A statement outside a transaction block; closes names a database whose connection must close first.
   */
  public static Code.Stmt autocommit(final String sql, final String database, final String closes) {
    Code.Run _xblockexpression = null;
    {
      final Code.Entries args = PgvectorSchema.kw("sql", Code.text(sql));
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      if ((closes != null)) {
        args.put("closes", Code.text(closes));
      }
      Code.Call _runtime = Code.runtime("autocommit", args);
      _xblockexpression = new Code.Run(_runtime);
    }
    return _xblockexpression;
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
