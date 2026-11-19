package org.ftn.vecDSL.generator.duckdb;

import com.google.common.collect.Iterables;
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
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to DuckDB tables (DuckDB 2.0.0 with vss and fts). Every field is a typed column in declaration
 * order: payload fields as SQL types (nested and json fields as JSON, arrays as lists), vector fields as FLOAT[dim] columns,
 * each with its own HNSW index. The primary key is the key column; tenant and namespace names live in the partition-key field
 * or in hidden VARCHAR columns. A logical database is a schema of the database file. The full-text fields of a table share one
 * fts index, rebuilt after every write.
 */
@SuppressWarnings("all")
public class DuckdbSchema {
  public static final String TENANT_COLUMN = "vecdsl_tenant";

  public static final String NAMESPACE_COLUMN = "vecdsl_namespace";

  private static final Map<EScalarDType, String> SCALARS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "INTEGER"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "BIGINT"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "TINYINT"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "FLOAT"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "DOUBLE"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "BOOLEAN"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "VARCHAR"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "VARCHAR"), Pair.<EScalarDType, String>of(EScalarDType.DATE, "DATE"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "TIMESTAMP"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "TIMESTAMPTZ"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "UUID"), Pair.<EScalarDType, String>of(EScalarDType.BYTES, "BLOB"), Pair.<EScalarDType, String>of(EScalarDType.JSON, "JSON"), Pair.<EScalarDType, String>of(EScalarDType.MAP, "JSON"), Pair.<EScalarDType, String>of(EScalarDType.STRUCT, "JSON")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted;

  private final Set<VectorCollection> namespaced;

  private final Map<VectorCollection, DuckdbSql.Table> evolved = CollectionLiterals.<VectorCollection, DuckdbSql.Table>newHashMap();

  private final Map<String, VectorCollection> views = CollectionLiterals.<String, VectorCollection>newLinkedHashMap();

  public DuckdbSchema(final List<String> notes, final Script script) {
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
  public DuckdbSql.Table table(final VectorCollection c) {
    DuckdbSql.Table _elvis = null;
    DuckdbSql.Table _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      DuckdbSql.Table _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  /**
   * Records the table an alter statement leaves; create and drop return the collection to its declaration.
   */
  public void evolve(final VectorCollection c, final DuckdbSql.Table table) {
    if ((table == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, table);
    }
  }

  private DuckdbSql.Table declared(final VectorCollection c) {
    DuckdbSql.Table _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!DuckdbSchema.KEYS.contains(((PayloadField) key).getType())))) {
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
      final List<DuckdbSql.Column> columns = CollectionLiterals.<DuckdbSql.Column>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        DuckdbSql.Column _column = this.column(c, field);
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
          _xifexpression_2 = DuckdbSchema.TENANT_COLUMN;
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
          _xifexpression_4 = DuckdbSchema.NAMESPACE_COLUMN;
        } else {
          _xifexpression_4 = partitionKey.getName();
        }
        _xifexpression_3 = _xifexpression_4;
      }
      final String namespace = _xifexpression_3;
      String _schemaOf = DuckdbSchema.schemaOf(c);
      String _name = c.getName();
      String _fieldName = ModelUtil.fieldName(key);
      Map<String, String> _emptyMap = CollectionLiterals.<String, String>emptyMap();
      _xblockexpression = new DuckdbSql.Table(_schemaOf, _name, _fieldName, columns, tenant, namespace, _emptyMap);
    }
    return _xblockexpression;
  }

  /**
   * The schema of a collection: its logical database, else main.
   */
  public static String schemaOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      if ((database == null)) {
        return DuckdbSql.MAIN;
      }
      DuckdbSql.id(database.getName());
      _xblockexpression = database.getName();
    }
    return _xblockexpression;
  }

  public DuckdbSql.Column column(final VectorCollection c, final Field field) {
    DuckdbSql.Column _switchResult = null;
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

  private DuckdbSql.Column payloadColumn(final VectorCollection c, final PayloadField field) {
    DuckdbSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      DuckdbSql.id(name);
      this.note(field, "nullable");
      final boolean nested = ((!field.getSubFields().isEmpty()) || field.isNested());
      final DuckdbSql.Analysis analysis = this.analysis(c, field);
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = DuckdbSchema.check(limit, field);
        checks.add(_check);
      }
      if ((((nested || Objects.equals(field.getType(), EScalarDType.JSON)) || Objects.equals(field.getType(), EScalarDType.MAP)) || Objects.equals(field.getType(), EScalarDType.STRUCT))) {
        if ((analysis != null)) {
          throw Unsupported.of(("full-text search on json field " + name));
        }
        Code.Expr _defaultOf = DuckdbSchema.defaultOf(field);
        return new DuckdbSql.Column(name, "JSON", null, true, null, null, true, _defaultOf, checks);
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
        if ((Objects.equals(element, "JSON") || Objects.equals(element, "BLOB"))) {
          String _literal = field.getElementType().getLiteral();
          String _plus = ("arrays of " + _literal);
          String _plus_1 = (_plus + " in field ");
          String _plus_2 = (_plus_1 + name);
          throw Unsupported.of(_plus_2);
        }
        Code.Expr _defaultOf_1 = DuckdbSchema.defaultOf(field);
        return new DuckdbSql.Column(name, (element + "[]"), element, false, null, null, true, _defaultOf_1, checks);
      }
      final String sqlType = this.scalar(field.getType(), name);
      if ((ModelUtil.written(field, "maxLength") && Objects.equals(sqlType, "VARCHAR"))) {
        int _maxLength = field.getMaxLength();
        String _plus_3 = ("CHECK (length({}) <= " + Integer.valueOf(_maxLength));
        String _plus_4 = (_plus_3 + ")");
        checks.add(_plus_4);
      }
      if (((analysis != null) && (!Objects.equals(sqlType, "VARCHAR")))) {
        String _literal_1 = field.getType().getLiteral();
        String _plus_5 = ("full-text search on " + _literal_1);
        String _plus_6 = (_plus_5 + " field ");
        String _plus_7 = (_plus_6 + name);
        throw Unsupported.of(_plus_7);
      }
      Code.Expr _defaultOf_2 = DuckdbSchema.defaultOf(field);
      _xblockexpression = new DuckdbSql.Column(name, sqlType, null, false, null, analysis, true, _defaultOf_2, checks);
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
    final String sql = DuckdbSchema.SCALARS.get(type);
    if ((sql != null)) {
      return sql;
    }
    if (type != null) {
      switch (type) {
        case GEO_POINT:
          throw Unsupported.of(((("geo_point field " + field) + "; ") + DuckdbStatements.NO_SPATIAL));
        case GEOMETRY:
          throw Unsupported.of(((("geometry field " + field) + "; ") + DuckdbStatements.NO_SPATIAL));
        default:
          String _literal = type.getLiteral();
          String _plus = (_literal + " field ");
          String _plus_1 = (_plus + field);
          String _plus_2 = (_plus_1 + "; DuckDB has no media types (BLOB holds bytes without media semantics)");
          throw Unsupported.of(_plus_2);
      }
    } else {
      String _literal = type.getLiteral();
      String _plus = (_literal + " field ");
      String _plus_1 = (_plus + field);
      String _plus_2 = (_plus_1 + "; DuckDB has no media types (BLOB holds bytes without media semantics)");
      throw Unsupported.of(_plus_2);
    }
  }

  /**
   * A field limit as a CHECK constraint of the column; {} stands for the column.
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
              _xblockexpression_1 = ("len({}) <= " + Integer.valueOf(_value));
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
              _xblockexpression_2 = ("octet_length(CAST({} AS VARCHAR)::BLOB) <= " + Integer.valueOf(_value));
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
              _xblockexpression_3 = ("array_length({}) <= " + Integer.valueOf(_value));
            }
            _switchResult = _xblockexpression_3;
            break;
          default:
            break;
        }
      }
      final String condition = _switchResult;
      String _id = DuckdbSql.id(limit.getName());
      String _plus = ("CONSTRAINT " + _id);
      String _plus_1 = (_plus + " CHECK (");
      String _plus_2 = (_plus_1 + condition);
      _xblockexpression = (_plus_2 + ")");
    }
    return _xblockexpression;
  }

  /**
   * The text analysis of a full-text field, null for other fields: its analyzer's stemmer and stop words, else no stemming,
   * no stop words and lower case, which is what the other generators do without an analyzer.
   */
  public DuckdbSql.Analysis analysis(final VectorCollection c, final PayloadField field) {
    DuckdbSql.Analysis _xblockexpression = null;
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
      DuckdbSql.Analysis _xifexpression = null;
      if ((analyzer == null)) {
        _xifexpression = DuckdbSql.Analysis.DEFAULT;
      } else {
        _xifexpression = DuckdbSchema.analysis(analyzer);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * The fts options of an analyzer: the language's stemmer, its stop words (English only), accents stripped by ascii_folding.
   */
  public static DuckdbSql.Analysis analysis(final Analyzer analyzer) {
    DuckdbSql.Analysis _xblockexpression = null;
    {
      if (((!Objects.equals(analyzer.getTokenizer().getLiteral(), "standard")) && (!Objects.equals(analyzer.getTokenizer().getLiteral(), "language")))) {
        String _literal = analyzer.getTokenizer().getLiteral();
        String _plus = ("the " + _literal);
        String _plus_1 = (_plus + " tokenizer; the fts extension splits text with one regular expression (ignore option)");
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty = analyzer.getResources().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("analyzer resources; the fts extension reads stop words from a table, not from files");
      }
      String _xifexpression = null;
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression = "none";
      } else {
        _xifexpression = DuckdbSql.stemmer(analyzer.getLanguage());
      }
      final String stemmer = _xifexpression;
      if ((stemmer == null)) {
        String _language_1 = analyzer.getLanguage();
        String _plus_2 = ("the language " + _language_1);
        String _plus_3 = (_plus_2 + "; the fts extension has no stemmer for it");
        throw Unsupported.of(_plus_3);
      }
      final Function1<AnalyzerFilter, String> _function = (AnalyzerFilter it) -> {
        return it.getType().getLiteral();
      };
      final List<String> filters = IterableExtensions.<String>toList(ListExtensions.<AnalyzerFilter, String>map(analyzer.getFilters(), _function));
      for (final String filter : filters) {
        if (filter != null) {
          switch (filter) {
            case "lowercase":
            case "ascii_folding":
              break;
            case "stop":
            case "stemmer":
              boolean _equals = Objects.equals(stemmer, "none");
              if (_equals) {
                throw Unsupported.of((("the " + filter) + " filter without a language"));
              }
              break;
            default:
              throw Unsupported.of((("the " + filter) + " filter; the fts extension lowercases, strips accents, stems and removes stop words only"));
          }
        } else {
          throw Unsupported.of((("the " + filter) + " filter; the fts extension lowercases, strips accents, stems and removes stop words only"));
        }
      }
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(analyzer, "stopwords");
      if (_written) {
        _xifexpression_1 = analyzer.getStopwords();
      } else {
        String _xifexpression_2 = null;
        boolean _equals_1 = Objects.equals(stemmer, "english");
        if (_equals_1) {
          _xifexpression_2 = "english";
        } else {
          _xifexpression_2 = "none";
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String stopwords = _xifexpression_1;
      if ((((!Objects.equals(stopwords, "none")) && (!Objects.equals(stopwords, "english"))) && (!Objects.equals(stopwords, "en")))) {
        throw Unsupported.of((("stopwords " + stopwords) + "; the fts extension has an English stop-word list built in, other lists are tables"));
      }
      if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
        throw Unsupported.of((("BM25 parameters on an analyzer; " + DuckdbStatements.PENDING) + " (the k and b arguments of match_bm25)"));
      }
      String _xifexpression_3 = null;
      boolean _equals_2 = Objects.equals(stopwords, "none");
      if (_equals_2) {
        _xifexpression_3 = "none";
      } else {
        _xifexpression_3 = "english";
      }
      boolean _contains = filters.contains("ascii_folding");
      _xblockexpression = new DuckdbSql.Analysis(stemmer, _xifexpression_3, _contains, true);
    }
    return _xblockexpression;
  }

  private DuckdbSql.Column vectorColumn(final VectorCollection c, final VectorField field) {
    DuckdbSql.Column _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      DuckdbSql.id(name);
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; a DuckDB array column holds one vector"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + DuckdbStatements.NO_MODELS));
      }
      this.note(field, "normalized", "onDisk", "modifier");
      String _literal = field.getType().getLiteral();
      boolean _notEquals = (!Objects.equals(_literal, "float_vector"));
      if (_notEquals) {
        String _literal_1 = field.getType().getLiteral();
        String _plus = (_literal_1 + " fields; ");
        String _plus_1 = (_plus + DuckdbStatements.FLOAT_ONLY);
        throw Unsupported.of(_plus_1);
      }
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      final VectorIndex index = this.indexOf(c, field);
      final List<String> checks = CollectionLiterals.<String>newArrayList();
      EList<FieldLimit> _limits = field.getLimits();
      for (final FieldLimit limit : _limits) {
        String _check = DuckdbSchema.check(limit, field);
        checks.add(_check);
      }
      int _dimension_1 = field.getDimension();
      String _plus_2 = ("FLOAT[" + Integer.valueOf(_dimension_1));
      String _plus_3 = (_plus_2 + "]");
      int _dimension_2 = field.getDimension();
      String _metric = this.metric(field, index);
      DuckdbSql.Vec _vec = new DuckdbSql.Vec(_dimension_2, _metric);
      boolean _isNullable = field.isNullable();
      _xblockexpression = new DuckdbSql.Column(name, _plus_3, null, false, _vec, null, _isNullable, null, checks);
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
      DuckdbSql.distance(metric.getLiteral());
      _xblockexpression = metric.getLiteral();
    }
    return _xblockexpression;
  }

  /**
   * A collection's table with its objects in one transaction. A declaration (fresh) starts from the declared fields; a create
   * that names an existing collection keeps the columns earlier alter statements changed, and skips fields and indexes they
   * dropped.
   */
  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(DuckdbStatements.NO_TTL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of(DuckdbStatements.NO_AUTO_ID);
      }
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        throw Unsupported.of(DuckdbStatements.NO_DYNAMIC);
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(DuckdbStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(DuckdbStatements.NO_CROSS_REF);
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals = (_embeddingModel != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("embedding-model" + DuckdbStatements.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_1 = (_storage != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(DuckdbStatements.NO_STORAGE);
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(DuckdbStatements.NO_TAGS);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = DuckdbStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        throw Unsupported.of(DuckdbStatements.collectionLimit(IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral()));
      }
      boolean _isEmpty_4 = ModelUtil.partitions(c).isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
      }
      Set<String> _keySet = ModelUtil.extParams(c, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "consistency");
      if (fresh) {
        this.evolve(c, null);
      }
      final DuckdbSql.Table table = this.table(c);
      EList<Analyzer> _analyzers = c.getAnalyzers();
      for (final Analyzer analyzer : _analyzers) {
        DuckdbSchema.analysis(analyzer);
      }
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      String _schema = table.schema();
      boolean _notEquals = (!Objects.equals(_schema, DuckdbSql.MAIN));
      if (_notEquals) {
        String _id = DuckdbSql.id(table.schema());
        String _plus_2 = ("CREATE SCHEMA IF NOT EXISTS " + _id);
        ddl.add(_plus_2);
      }
      String _createTable = DuckdbSql.createTable(table, ifNotExists);
      ddl.add(_createTable);
      final List<String> rest = CollectionLiterals.<String>newArrayList();
      List<String> _indexes = this.indexes(c, table, ifNotExists);
      Iterables.<String>addAll(rest, _indexes);
      if ((fresh || (!ifNotExists))) {
        String _description = c.getDescription();
        boolean _tripleNotEquals_2 = (_description != null);
        if (_tripleNotEquals_2) {
          String _table = DuckdbSql.table(table);
          String _plus_3 = ("COMMENT ON TABLE " + _table);
          String _plus_4 = (_plus_3 + " IS ");
          String _literal_1 = DuckdbSql.literal(c.getDescription());
          String _plus_5 = (_plus_4 + _literal_1);
          rest.add(_plus_5);
        }
        EList<Alias> _aliases = c.getAliases();
        for (final Alias alias : _aliases) {
          {
            String _createView = DuckdbSchema.createView(alias.getName(), table, ifNotExists);
            rest.add(_createView);
            this.view(alias.getName(), c);
          }
        }
      }
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals_3 = (_external != null);
      if (_tripleNotEquals_3) {
        return this.derived(c.getExternal(), table, ddl, rest, ifNotExists);
      }
      Iterables.<String>addAll(ddl, rest);
      Code.Stmt _execute = DuckdbSchema.execute(ddl);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * A collection derived from an external file: its table is filled by DuckDB's reader of the file (read_parquet, read_csv or
   * read_json, the path bound) before the indexes are built, in one transaction; RefreshExternal reads the file again.
   */
  private List<Code.Stmt> derived(final ExternalSource source, final DuckdbSql.Table table, final List<String> ddl, final List<String> rest, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Query q = new DuckdbSql.Query();
      String _location = source.getLocation();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(source, "format");
      if (_written) {
        _xifexpression = source.getFormat().getLiteral();
      } else {
        _xifexpression = null;
      }
      String _reader = DuckdbSql.reader(_location, _xifexpression);
      String _localPath = DuckdbSql.localPath(source.getLocation());
      String _xifexpression_1 = null;
      if (ifNotExists) {
        _xifexpression_1 = "ignore";
      } else {
        _xifexpression_1 = "error";
      }
      final String insert = DuckdbSql.insertFromFile(q, table, _reader, _localPath, CollectionLiterals.<String, Code.Expr>newLinkedHashMap(), _xifexpression_1);
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      for (final String sql : ddl) {
        steps.add(DuckdbSchema.kw("sql", Code.text(sql)));
      }
      steps.add(DuckdbSchema.kw("sql", Code.text(insert), "params", q.params()));
      for (final String sql_1 : rest) {
        steps.add(DuckdbSchema.kw("sql", Code.text(sql_1)));
      }
      Code.Call _runtime = Code.runtime("execute", DuckdbSchema.kw("steps", Code.items(steps)));
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * The secondary indexes of a table: HNSW per indexed vector field, ART per declared payload index, the fts index.
   */
  public List<String> indexes(final VectorCollection c, final DuckdbSql.Table table, final boolean ifNotExists) {
    List<String> _xblockexpression = null;
    {
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        return Boolean.valueOf(DuckdbSchema.present(table, it));
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
        return Boolean.valueOf(((it.getTarget().getField() == null) || DuckdbSchema.present(table, it.getTarget().getField())));
      };
      Iterable<PayloadIndex> _filter_1 = IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_1);
      for (final PayloadIndex declared : _filter_1) {
        String _payloadIndex = this.payloadIndex(c, table, declared, ifNotExists);
        ddl.add(_payloadIndex);
      }
      String _createFtsIndex = DuckdbSql.createFtsIndex(table);
      ddl.add(_createFtsIndex);
      _xblockexpression = IterableExtensions.<String>toList(IterableExtensions.<String>filterNull(ddl));
    }
    return _xblockexpression;
  }

  /**
   * DROP INDEX for every secondary index of a table and its fts index, before an ALTER that DuckDB refuses while they exist.
   */
  public List<String> dropIndexes(final VectorCollection c, final DuckdbSql.Table table) {
    List<String> _xblockexpression = null;
    {
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        return Boolean.valueOf(DuckdbSchema.present(table, it));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function);
      for (final VectorField field : _filter) {
        {
          final VectorIndex index = this.indexOf(c, field);
          if (((index != null) && (!Objects.equals(index.getFamily().getLiteral(), "flat")))) {
            String _qualified = DuckdbSql.qualified(table, DuckdbSchema.indexName(c, index.getName()));
            String _plus = ("DROP INDEX IF EXISTS " + _qualified);
            ddl.add(_plus);
          }
        }
      }
      final Function1<PayloadIndex, Boolean> _function_1 = (PayloadIndex it) -> {
        EPayloadIndexKind _kind = it.getKind();
        return Boolean.valueOf((!Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT)));
      };
      Iterable<PayloadIndex> _filter_1 = IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_1);
      for (final PayloadIndex declared : _filter_1) {
        String _qualified = DuckdbSql.qualified(table, DuckdbSchema.indexName(c, declared.getName()));
        String _plus = ("DROP INDEX IF EXISTS " + _qualified);
        ddl.add(_plus);
      }
      boolean _isEmpty = table.fullTextColumns().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _ftsSchema = DuckdbSql.ftsSchema(table);
        String _plus_1 = ("DROP SCHEMA IF EXISTS " + _ftsSchema);
        String _plus_2 = (_plus_1 + " CASCADE");
        ddl.add(_plus_2);
      }
      _xblockexpression = ddl;
    }
    return _xblockexpression;
  }

  /**
   * Records which collection a view reads after the statements mapped so far (create, add-alias, swap-alias, drop).
   */
  public void view(final String alias, final VectorCollection c) {
    if ((c == null)) {
      this.views.remove(alias);
    } else {
      this.views.put(alias, c);
    }
  }

  /**
   * The views that read a collection now, which a renamed table must get again.
   */
  public List<String> viewsOf(final VectorCollection c) {
    final Function1<Map.Entry<String, VectorCollection>, Boolean> _function = (Map.Entry<String, VectorCollection> it) -> {
      VectorCollection _value = it.getValue();
      return Boolean.valueOf((_value == c));
    };
    final Function1<Map.Entry<String, VectorCollection>, String> _function_1 = (Map.Entry<String, VectorCollection> it) -> {
      return it.getKey();
    };
    return IterableExtensions.<String>toList(IterableExtensions.<Map.Entry<String, VectorCollection>, String>map(IterableExtensions.<Map.Entry<String, VectorCollection>>filter(this.views.entrySet(), _function), _function_1));
  }

  /**
   * Whether a field still has its column (an alter statement may have dropped it).
   */
  public static boolean present(final DuckdbSql.Table table, final Field field) {
    boolean _xblockexpression = false;
    {
      final String name = table.renames().getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field));
      final Function1<DuckdbSql.Column, Boolean> _function = (DuckdbSql.Column column) -> {
        String _name = column.name();
        return Boolean.valueOf(Objects.equals(_name, name));
      };
      _xblockexpression = IterableExtensions.<DuckdbSql.Column>exists(table.columns(), _function);
    }
    return _xblockexpression;
  }

  /**
   * The DuckDB name of a vecDSL index: the collection's declared name and the index name (vecDSL scopes index names to the collection).
   */
  public static String indexName(final VectorCollection c, final String index) {
    String _name = c.getName();
    String _plus = (_name + "_");
    return (_plus + index);
  }

  /**
   * An HNSW index (vss): max-degree is M, build-candidates ef_construction, search-candidates ef_search; flat means no index.
   */
  public String vectorIndex(final VectorCollection c, final DuckdbSql.Table table, final VectorField field, final VectorIndex index, final boolean ifNotExists) {
    String _xblockexpression = null;
    {
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
      final Map<String, Integer> options = CollectionLiterals.<String, Integer>newLinkedHashMap();
      this.note(index, "onDisk", "online");
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; vss builds and searches HNSW indexes on the CPU (usearch)");
      }
      Set<String> _keySet = ModelUtil.extParams(index, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      if (((index.getQuantization() != null) && (!Objects.equals(index.getQuantization().getType().getLiteral(), "none")))) {
        throw Unsupported.of(("quantization; " + DuckdbStatements.FLOAT_ONLY));
      }
      String _literal = index.getFamily().getLiteral();
      boolean _equals = Objects.equals(_literal, "flat");
      if (_equals) {
        return null;
      }
      if (((!Objects.equals(index.getFamily().getLiteral(), "hnsw")) && (!Objects.equals(index.getFamily().getLiteral(), "auto")))) {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus = (_literal_1 + " indexes; vss builds HNSW indexes only (CREATE INDEX ... USING HNSW)");
        throw Unsupported.of(_plus);
      }
      boolean _written = ModelUtil.written(index, "centroids");
      if (_written) {
        throw Unsupported.of("centroids on an hnsw index; lists belong to ivf indexes");
      }
      boolean _written_1 = ModelUtil.written(index, "maxDegree");
      if (_written_1) {
        options.put("M", Integer.valueOf(index.getMaxDegree()));
      }
      boolean _written_2 = ModelUtil.written(index, "buildCandidates");
      if (_written_2) {
        options.put("ef_construction", Integer.valueOf(index.getBuildCandidates()));
      }
      boolean _written_3 = ModelUtil.written(index, "searchCandidates");
      if (_written_3) {
        options.put("ef_search", Integer.valueOf(index.getSearchCandidates()));
      }
      String _xifexpression = null;
      boolean _written_4 = ModelUtil.written(index, "metric");
      if (_written_4) {
        _xifexpression = index.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      String _name = column.name();
      String _type = column.type();
      int _dim = column.vec().dim();
      DuckdbSql.Vec _vec = new DuckdbSql.Vec(_dim, metric);
      boolean _nullable = column.nullable();
      final DuckdbSql.Column indexed = new DuckdbSql.Column(_name, _type, null, false, _vec, null, _nullable, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      _xblockexpression = DuckdbSql.createVectorIndex(table, DuckdbSchema.indexName(c, index.getName()), indexed, options, ifNotExists);
    }
    return _xblockexpression;
  }

  /**
   * An ART index on a payload column; a full_text index is the table's fts index, built with the table.
   */
  public String payloadIndex(final VectorCollection c, final DuckdbSql.Table table, final PayloadIndex index, final boolean ifNotExists) {
    String _xblockexpression = null;
    {
      final FieldPath path = index.getTarget();
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields");
      }
      Field _field = path.getField();
      boolean _not = (!(_field instanceof PayloadField));
      if (_not) {
        throw Unsupported.of("a payload index on a vector field");
      }
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      this.note(index, "onDisk", "lowercase", "prefixMatch", "minTokenLength", "maxTokenLength");
      Set<String> _keySet = ModelUtil.extParams(index, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      if ((ModelUtil.written(index, "structure") && Objects.equals(index.getStructure().getLiteral(), "bitmap"))) {
        throw Unsupported.of("bitmap payload indexes; DuckDB indexes columns with ART and keeps min-max zone maps per row group");
      }
      boolean _isEmpty = path.getSubFields().isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        String _name = column.name();
        String _plus = ("a path index below " + _name);
        String _plus_1 = (_plus + "; ");
        String _plus_2 = (_plus_1 + DuckdbStatements.NO_PATH_INDEX);
        throw Unsupported.of(_plus_2);
      }
      String _switchResult = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case FULL_TEXT:
            Object _xblockexpression_1 = null;
            {
              boolean _fullText = column.fullText();
              boolean _not_2 = (!_fullText);
              if (_not_2) {
                String _name_1 = column.name();
                String _plus_3 = ("a full_text index on " + _name_1);
                String _plus_4 = (_plus_3 + ", which is not a text field");
                throw Unsupported.of(_plus_4);
              }
              _xblockexpression_1 = null;
            }
            _switchResult = ((String)_xblockexpression_1);
            break;
          case GEO:
            throw Unsupported.of(("geo payload indexes; " + DuckdbStatements.NO_SPATIAL));
          case JSON:
            throw Unsupported.of("json payload indexes; DuckDB indexes scalar columns and expressions with ART, not JSON documents");
          default:
            String _xblockexpression_2 = null;
            {
              if ((column.array() || column.json())) {
                String _name_1 = column.name();
                String _plus_3 = ("an index on " + _name_1);
                String _plus_4 = (_plus_3 + "; ART indexes keys of scalar types, not lists or JSON");
                throw Unsupported.of(_plus_4);
              }
              _xblockexpression_2 = DuckdbSql.createPayloadIndex(table, DuckdbSchema.indexName(c, index.getName()), column, ifNotExists);
            }
            _switchResult = _xblockexpression_2;
            break;
        }
      } else {
        String _xblockexpression_2 = null;
        {
          if ((column.array() || column.json())) {
            String _name_1 = column.name();
            String _plus_3 = ("an index on " + _name_1);
            String _plus_4 = (_plus_3 + "; ART indexes keys of scalar types, not lists or JSON");
            throw Unsupported.of(_plus_4);
          }
          _xblockexpression_2 = DuckdbSql.createPayloadIndex(table, DuckdbSchema.indexName(c, index.getName()), column, ifNotExists);
        }
        _switchResult = _xblockexpression_2;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  public static String createView(final String alias, final DuckdbSql.Table table, final boolean ifNotExists) {
    String _xifexpression = null;
    if (ifNotExists) {
      _xifexpression = "OR REPLACE ";
    } else {
      _xifexpression = "";
    }
    String _plus = ("CREATE " + _xifexpression);
    String _plus_1 = (_plus + "VIEW ");
    String _qualified = DuckdbSql.qualified(table, alias);
    String _plus_2 = (_plus_1 + _qualified);
    String _plus_3 = (_plus_2 + " AS SELECT * FROM ");
    String _table = DuckdbSql.table(table);
    return (_plus_3 + _table);
  }

  /**
   * One runtime call with SQL text and parameters; settings only when present.
   */
  public static Code.Call call(final String op, final String sql, final DuckdbSql.Query q, final Map<String, String> settings) {
    Code.Call _xblockexpression = null;
    {
      final Code.Entries args = DuckdbSchema.kw("sql", Code.text(sql));
      if (((q != null) && (q.size() > 0))) {
        args.put("params", q.params());
      }
      if (((settings != null) && (!settings.isEmpty()))) {
        args.put("settings", DuckdbSql.settings(settings));
      }
      _xblockexpression = Code.runtime(op, args);
    }
    return _xblockexpression;
  }

  /**
   * A write: the statement, its counter and the fts rebuild of the table when it has full-text columns.
   */
  public static Code.Call change(final String sql, final DuckdbSql.Query q, final String counter, final DuckdbSql.Table table, final boolean skipped) {
    Code.Call _xblockexpression = null;
    {
      final Code.Entries args = DuckdbSchema.kw("sql", Code.text(sql), "counter", Code.text(counter));
      if (((q != null) && (q.size() > 0))) {
        args.put("params", q.params());
      }
      final String refresh = DuckdbSql.createFtsIndex(table);
      if ((refresh != null)) {
        Code.Text _text = Code.text(refresh);
        args.put("refresh", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)))));
      }
      if (skipped) {
        args.put("skipped", Code.flag(true));
      }
      _xblockexpression = Code.runtime("change", args);
    }
    return _xblockexpression;
  }

  /**
   * DDL statements in one transaction.
   */
  public static Code.Stmt execute(final List<String> statements) {
    final Function1<String, Code.Expr> _function = (String sql) -> {
      Code.Entries _kw = DuckdbSchema.kw("sql", Code.text(sql));
      return ((Code.Expr) _kw);
    };
    Code.Call _runtime = Code.runtime("execute", DuckdbSchema.kw("steps", Code.items(IterableExtensions.<Code.Expr>toList(IterableExtensions.<String, Code.Expr>map(IterableExtensions.<String>filterNull(statements), _function)))));
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
