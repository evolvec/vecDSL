package org.ftn.vecDSL.generator.opensearch;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.ftn.vecDSL.vecDSL.Cluster;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to OpenSearch indices (server 3.9 with the k-NN plugin): one index per collection with an explicit
 * mapping (dynamic strict unless the collection is dynamic), one shard and no replica unless the collection declares sharding or
 * replication, index.knn on and derived source off (so _source keeps the vectors as written), analyzers and BM25 parameters in
 * the index settings, and declared tenants, namespaces, partitions and aliases as aliases (filtered for the slices). Payload
 * fields are keyword, numeric, date, boolean, object, nested and geo fields; full-text fields are text fields with a keyword
 * sub-field for exact filters; dense vector fields are knn_vector fields (hnsw of the Faiss engine), sparse vector fields are
 * rank_features fields.
 */
@SuppressWarnings("all")
public class OpenSearchSchema {
  private static final Map<EScalarDType, String> NUMBERS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "integer"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "long"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "byte"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "float"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "double"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "boolean"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.BYTES, "binary"), Pair.<EScalarDType, String>of(EScalarDType.GEO_POINT, "geo_point"), Pair.<EScalarDType, String>of(EScalarDType.GEOMETRY, "geo_shape")));

  private static final Map<EScalarDType, String> DATES = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.DATE, "strict_date"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "strict_date_optional_time"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "strict_date_optional_time")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<String, String> TOKENIZERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("standard", "standard"), Pair.<String, String>of("whitespace", "whitespace"), Pair.<String, String>of("keyword", "keyword"), Pair.<String, String>of("ngram", "ngram"), Pair.<String, String>of("edge_ngram", "edge_ngram"), Pair.<String, String>of("language", "standard")));

  private static final Map<String, String> LANGUAGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("en", "english"), Pair.<String, String>of("english", "english"), Pair.<String, String>of("de", "german"), Pair.<String, String>of("german", "german"), Pair.<String, String>of("fr", "french"), Pair.<String, String>of("french", "french"), Pair.<String, String>of("es", "spanish"), Pair.<String, String>of("spanish", "spanish"), Pair.<String, String>of("it", "italian"), Pair.<String, String>of("italian", "italian"), Pair.<String, String>of("pt", "portuguese"), Pair.<String, String>of("portuguese", "portuguese"), Pair.<String, String>of("nl", "dutch"), Pair.<String, String>of("dutch", "dutch"), Pair.<String, String>of("ru", "russian"), Pair.<String, String>of("russian", "russian"), Pair.<String, String>of("sv", "swedish"), Pair.<String, String>of("swedish", "swedish"), Pair.<String, String>of("da", "danish"), Pair.<String, String>of("danish", "danish"), Pair.<String, String>of("fi", "finnish"), Pair.<String, String>of("finnish", "finnish"), Pair.<String, String>of("no", "norwegian"), Pair.<String, String>of("norwegian", "norwegian"), Pair.<String, String>of("hu", "hungarian"), Pair.<String, String>of("hungarian", "hungarian"), Pair.<String, String>of("ro", "romanian"), Pair.<String, String>of("romanian", "romanian"), Pair.<String, String>of("tr", "turkish"), Pair.<String, String>of("turkish", "turkish"), Pair.<String, String>of("sr", "serbian"), Pair.<String, String>of("serbian", "serbian")));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, OpenSearchIndex> evolved = CollectionLiterals.<VectorCollection, OpenSearchIndex>newHashMap();

  public OpenSearchSchema(final List<String> notes, final Script script) {
    this.notes = notes;
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
    for (final EObject object : _iterable) {
      this.slices(object);
    }
  }

  /**
   * Collections whose statements name a tenant or a namespace get the hidden keyword fields.
   */
  private void slices(final EObject object) {
    final VectorCollection collection = ModelUtil.targetCollection(object);
    if ((collection == null)) {
      return;
    }
    final EStructuralFeature tenantFeature = object.eClass().getEStructuralFeature("tenant");
    if (((tenantFeature != null) && (object.eGet(tenantFeature) != null))) {
      this.tenanted.add(collection);
    }
    final EStructuralFeature namespaceFeature = object.eClass().getEStructuralFeature("namespace");
    if ((((namespaceFeature != null) && Objects.equals(namespaceFeature.getEType().getName(), "EString")) && (object.eGet(namespaceFeature) != null))) {
      this.namespaced.add(collection);
    }
  }

  /**
   * The index of a collection as the statements so far leave it (alter statements change fields and the name).
   */
  public OpenSearchIndex index(final VectorCollection c) {
    OpenSearchIndex _elvis = null;
    OpenSearchIndex _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      OpenSearchIndex _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final OpenSearchIndex next) {
    if ((next == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, next);
    }
  }

  private OpenSearchIndex declared(final VectorCollection c) {
    OpenSearchIndex _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!OpenSearchSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys documents (_id) by an integer, text or uuid field");
        throw Unsupported.of(_plus_1);
      }
      final List<OpenSearchIndex.Field> fields = CollectionLiterals.<OpenSearchIndex.Field>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field declaredField : _fields) {
        OpenSearchIndex.Field _field = this.field(c, declaredField);
        fields.add(_field);
      }
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(it.isPartitionKey());
      };
      final PayloadField partitionKey = IterableExtensions.<PayloadField>findFirst(ModelUtil.payloadFields(c), _function);
      String _xifexpression_1 = null;
      if (((!c.getTenants().isEmpty()) || this.tenanted.contains(c))) {
        String _xifexpression_2 = null;
        if ((partitionKey == null)) {
          _xifexpression_2 = OpenSearchBodies.TENANT_FIELD;
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
          _xifexpression_4 = OpenSearchBodies.NAMESPACE_FIELD;
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
        _xifexpression_5 = OpenSearchBodies.PARTITION_FIELD;
      }
      final String partition = _xifexpression_5;
      String _indexName = OpenSearchBodies.indexName(OpenSearchSchema.databaseOf(c), c.getName());
      String _fieldName = ModelUtil.fieldName(key);
      HashMap<String, String> _newHashMap = CollectionLiterals.<String, String>newHashMap();
      String _databaseOf = OpenSearchSchema.databaseOf(c);
      _xblockexpression = new OpenSearchIndex(_indexName, _fieldName, fields, tenant, namespace, partition, _newHashMap, _databaseOf);
    }
    return _xblockexpression;
  }

  /**
   * The logical database of a collection declared in a logical-db, else null.
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      String _xifexpression = null;
      if ((database == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = database.getName();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public OpenSearchIndex.Field field(final VectorCollection c, final Field declaredField) {
    OpenSearchIndex.Field _switchResult = null;
    boolean _matched = false;
    if (declaredField instanceof VectorField) {
      _matched=true;
      _switchResult = this.vectorField(c, ((VectorField)declaredField));
    }
    if (!_matched) {
      if (declaredField instanceof PayloadField) {
        _matched=true;
        _switchResult = this.payloadField(c, ((PayloadField)declaredField));
      }
    }
    return _switchResult;
  }

  private OpenSearchIndex.Field payloadField(final VectorCollection c, final PayloadField f) {
    OpenSearchIndex.Field _xblockexpression = null;
    {
      final String name = OpenSearchBodies.field(ModelUtil.fieldName(f));
      this.note(f, "nullable", "documentBody");
      boolean _isEmpty = f.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _literal = IterableExtensions.<FieldLimit>head(f.getLimits()).getType().getLiteral();
        String _plus = ("field limits (" + _literal);
        String _plus_1 = (_plus + "); ");
        String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
        String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
        throw Unsupported.of(_plus_3);
      }
      Code.Expr _xifexpression = null;
      Literal _defaultValue = f.getDefaultValue();
      boolean _tripleEquals = (_defaultValue == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = Literals.of(f.getDefaultValue());
      }
      final Code.Expr defaultValue = _xifexpression;
      if ((((!f.getSubFields().isEmpty()) || f.isNested()) || Objects.equals(f.getType(), EScalarDType.STRUCT))) {
        final Code.Entries properties = Code.entries();
        EList<Field> _subFields = f.getSubFields();
        for (final Field sub : _subFields) {
          {
            final OpenSearchIndex.Field inner = this.field(c, sub);
            properties.put(inner.name(), inner.mapping());
          }
        }
        String _xifexpression_1 = null;
        boolean _isNested = f.isNested();
        if (_isNested) {
          _xifexpression_1 = "nested";
        } else {
          _xifexpression_1 = "object";
        }
        Code.Text _text = OpenSearchBodies.text(_xifexpression_1);
        Code.Entries _xifexpression_2 = null;
        boolean _isEmpty_1 = properties.entries().isEmpty();
        if (_isEmpty_1) {
          _xifexpression_2 = null;
        } else {
          _xifexpression_2 = properties;
        }
        Code.Text _xifexpression_3 = null;
        boolean _isEmpty_2 = f.getSubFields().isEmpty();
        if (_isEmpty_2) {
          _xifexpression_3 = OpenSearchBodies.text("true");
        } else {
          _xifexpression_3 = null;
        }
        final Code.Entries mapping = OpenSearchBodies.obj("type", _text, "properties", _xifexpression_2, 
          "dynamic", _xifexpression_3);
        boolean _isNested_1 = f.isNested();
        return new OpenSearchIndex.Field(name, mapping, false, null, _isNested_1, defaultValue);
      }
      if ((Objects.equals(f.getType(), EScalarDType.JSON) || Objects.equals(f.getType(), EScalarDType.MAP))) {
        boolean _isFullText = f.isFullText();
        if (_isFullText) {
          throw Unsupported.of((("full-text search on json field " + name) + "; a json field is an object whose sub-fields OpenSearch maps when they arrive"));
        }
        Code.Entries _obj = OpenSearchBodies.obj("type", OpenSearchBodies.text("object"), "dynamic", OpenSearchBodies.text("true"));
        return new OpenSearchIndex.Field(name, _obj, false, null, false, defaultValue);
      }
      EScalarDType _xifexpression_4 = null;
      EScalarDType _type = f.getType();
      boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
      if (_equals) {
        EScalarDType _xblockexpression_1 = null;
        {
          boolean _written = ModelUtil.written(f, "elementType");
          boolean _not_1 = (!_written);
          if (_not_1) {
            throw Unsupported.of((("array field " + name) + " without element-type"));
          }
          if ((((Objects.equals(f.getElementType(), EScalarDType.ARRAY) || Objects.equals(f.getElementType(), EScalarDType.JSON)) || Objects.equals(f.getElementType(), EScalarDType.MAP)) || Objects.equals(f.getElementType(), EScalarDType.STRUCT))) {
            String _literal_1 = f.getElementType().getLiteral();
            String _plus_4 = ("arrays of " + _literal_1);
            String _plus_5 = (_plus_4 + " in field ");
            String _plus_6 = (_plus_5 + name);
            String _plus_7 = (_plus_6 + "; ");
            String _plus_8 = (_plus_7 + OpenSearchStatements.PENDING);
            String _plus_9 = (_plus_8 + " (nested fields)");
            throw Unsupported.of(_plus_9);
          }
          _xblockexpression_1 = f.getElementType();
        }
        _xifexpression_4 = _xblockexpression_1;
      } else {
        _xifexpression_4 = f.getType();
      }
      final EScalarDType scalarType = _xifexpression_4;
      final Analyzer analyzer = f.getAnalyzer();
      if ((f.isFullText() || (analyzer != null))) {
        if (((!Objects.equals(scalarType, EScalarDType.VARCHAR)) && (!Objects.equals(scalarType, EScalarDType.TEXT)))) {
          String _literal_1 = scalarType.getLiteral();
          String _plus_4 = ("full-text search on " + _literal_1);
          String _plus_5 = (_plus_4 + " field ");
          String _plus_6 = (_plus_5 + name);
          throw Unsupported.of(_plus_6);
        }
        Code.Text _text_1 = OpenSearchBodies.text("text");
        Code.Text _xifexpression_5 = null;
        if ((analyzer == null)) {
          _xifexpression_5 = null;
        } else {
          _xifexpression_5 = OpenSearchBodies.text(OpenSearchBodies.aliasName(analyzer.getName()));
        }
        Code.Text _xifexpression_6 = null;
        if (((analyzer != null) && OpenSearchSchema.bm25(analyzer))) {
          String _aliasName = OpenSearchBodies.aliasName(analyzer.getName());
          String _plus_7 = (_aliasName + "_bm25");
          _xifexpression_6 = OpenSearchBodies.text(_plus_7);
        } else {
          _xifexpression_6 = null;
        }
        int _xifexpression_7 = (int) 0;
        boolean _written = ModelUtil.written(f, "maxLength");
        if (_written) {
          _xifexpression_7 = f.getMaxLength();
        } else {
          _xifexpression_7 = 8191;
        }
        final Code.Entries mapping_1 = OpenSearchBodies.obj("type", _text_1, "analyzer", _xifexpression_5, 
          "similarity", _xifexpression_6, 
          "fields", OpenSearchBodies.obj(OpenSearchBodies.RAW, OpenSearchBodies.obj("type", OpenSearchBodies.text("keyword"), "ignore_above", OpenSearchBodies.num(_xifexpression_7))));
        return new OpenSearchIndex.Field(name, mapping_1, true, null, false, defaultValue);
      }
      final Code.Entries mapping_2 = this.scalar(scalarType, name, f);
      _xblockexpression = new OpenSearchIndex.Field(name, mapping_2, false, null, false, defaultValue);
    }
    return _xblockexpression;
  }

  private Code.Entries scalar(final EScalarDType type, final String name, final PayloadField f) {
    Code.Entries _xblockexpression = null;
    {
      final String date = OpenSearchSchema.DATES.get(type);
      if ((date != null)) {
        return OpenSearchBodies.obj("type", OpenSearchBodies.text("date"), "format", OpenSearchBodies.text(date));
      }
      final String mapped = OpenSearchSchema.NUMBERS.get(type);
      if ((mapped == null)) {
        String _literal = type.getLiteral();
        String _plus = (_literal + " field ");
        String _plus_1 = (_plus + name);
        String _plus_2 = (_plus_1 + "; OpenSearch stores media as URLs or binary (base64) values, not ");
        String _literal_1 = type.getLiteral();
        String _plus_3 = (_plus_2 + _literal_1);
        String _plus_4 = (_plus_3 + " objects");
        throw Unsupported.of(_plus_4);
      }
      if ((Objects.equals(mapped, "keyword") && ModelUtil.written(f, "maxLength"))) {
        return OpenSearchBodies.obj("type", OpenSearchBodies.text("keyword"), "ignore_above", OpenSearchBodies.num(f.getMaxLength()));
      }
      _xblockexpression = OpenSearchBodies.obj("type", OpenSearchBodies.text(mapped));
    }
    return _xblockexpression;
  }

  private OpenSearchIndex.Field vectorField(final VectorCollection c, final VectorField f) {
    OpenSearchIndex.Field _xblockexpression = null;
    {
      final String name = OpenSearchBodies.field(ModelUtil.fieldName(f));
      boolean _isMultiVector = f.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; a knn_vector field holds one vector per document (several vectors per document need nested fields, one vector each)"));
      }
      if (((f.getModel() != null) || (ModelUtil.producingFunction(c, f) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + OpenSearchStatements.NO_MODELS));
      }
      this.note(f, "nullable");
      boolean _isEmpty = f.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _literal = IterableExtensions.<FieldLimit>head(f.getLimits()).getType().getLiteral();
        String _plus = ("field limits (" + _literal);
        String _plus_1 = (_plus + "); ");
        String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
        String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
        throw Unsupported.of(_plus_3);
      }
      final VectorIndex index = this.indexOf(c, f);
      String _literal_1 = f.getType().getLiteral();
      boolean _equals = Objects.equals(_literal_1, "sparse_float_vector");
      if (_equals) {
        if ((ModelUtil.written(f, "modifier") && Objects.equals(f.getModifier().getLiteral(), "idf"))) {
          throw Unsupported.of((("the idf modifier on " + name) + "; the neural_sparse query scores a rank_features field by the dot product of the given weights"));
        }
        final String sparseMetric = this.metric(f, index);
        boolean _notEquals = (!Objects.equals(sparseMetric, "dot"));
        if (_notEquals) {
          throw Unsupported.of((((("the " + sparseMetric) + " metric on sparse field ") + name) + "; the neural_sparse query scores rank_features by the dot product of the weights"));
        }
        if (((index != null) && (!Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("auto", "sparse_inverted")).contains(index.getFamily().getLiteral())))) {
          String _literal_2 = index.getFamily().getLiteral();
          String _plus_4 = (_literal_2 + " indexes on sparse field ");
          String _plus_5 = (_plus_4 + name);
          String _plus_6 = (_plus_5 + "; rank_features fields are inverted indexes");
          throw Unsupported.of(_plus_6);
        }
        Code.Entries _obj = OpenSearchBodies.obj("type", OpenSearchBodies.text("rank_features"));
        OpenSearchIndex.Vector _vector = new OpenSearchIndex.Vector("sparse", 0, "dot", "dot", true, null, false);
        return new OpenSearchIndex.Field(name, _obj, false, _vector, false, null);
      }
      String _switchResult = null;
      String _literal_3 = f.getType().getLiteral();
      if (_literal_3 != null) {
        switch (_literal_3) {
          case "float_vector":
            _switchResult = "float";
            break;
          case "float16_vector":
            _switchResult = "half_float";
            break;
          case "bfloat16_vector":
            _switchResult = "bfloat16";
            break;
          case "int8_vector":
            _switchResult = "byte";
            break;
          case "binary_vector":
            _switchResult = "binary";
            break;
          case "uint8_vector":
            throw Unsupported.of("uint8_vector fields; knn_vector byte vectors are signed 8-bit integers in [-128, 127]");
          default:
            String _literal_4 = f.getType().getLiteral();
            String _plus_7 = (_literal_4 + " fields; knn_vector fields hold float, half_float, byte (signed int8) and binary elements (bfloat16 through the Faiss sq encoder)");
            throw Unsupported.of(_plus_7);
        }
      } else {
        String _literal_4 = f.getType().getLiteral();
        String _plus_7 = (_literal_4 + " fields; knn_vector fields hold float, half_float, byte (signed int8) and binary elements (bfloat16 through the Faiss sq encoder)");
        throw Unsupported.of(_plus_7);
      }
      final String element = _switchResult;
      int _dimension = f.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      if ((Objects.equals(element, "binary") && ((f.getDimension() % 8) != 0))) {
        int _dimension_1 = f.getDimension();
        String _plus_8 = ((("binary vector field " + name) + " with ") + Integer.valueOf(_dimension_1));
        String _plus_9 = (_plus_8 + " dimensions; binary knn_vector dimensions are a multiple of 8");
        throw Unsupported.of(_plus_9);
      }
      int _dimension_2 = f.getDimension();
      boolean _greaterThan = (_dimension_2 > 16000);
      if (_greaterThan) {
        int _dimension_3 = f.getDimension();
        String _plus_10 = ((("vector field " + name) + " with ") + Integer.valueOf(_dimension_3));
        String _plus_11 = (_plus_10 + " dimensions; knn_vector fields have at most 16,000");
        throw Unsupported.of(_plus_11);
      }
      final String chosenMetric = this.metric(f, index);
      String _xifexpression = null;
      boolean _equals_1 = Objects.equals(element, "bfloat16");
      if (_equals_1) {
        _xifexpression = "float";
      } else {
        _xifexpression = element;
      }
      final String dataType = _xifexpression;
      final String spaceType = OpenSearchBodies.space(chosenMetric, dataType);
      final List<Object> chosenMethod = this.knnMethod(f, index, element);
      Code.Text _text = OpenSearchBodies.text("knn_vector");
      Code.Num _num = OpenSearchBodies.num(f.getDimension());
      Code.Text _text_1 = OpenSearchBodies.text(dataType);
      Code.Text _text_2 = OpenSearchBodies.text(spaceType);
      Code.Text _xifexpression_1 = null;
      if ((((index != null) && index.isOnDisk()) && (index.getQuantization() == null))) {
        _xifexpression_1 = OpenSearchBodies.text("on_disk");
      } else {
        _xifexpression_1 = null;
      }
      Object _get = chosenMethod.get(0);
      final Code.Entries mapping = OpenSearchBodies.obj("type", _text, "dimension", _num, "data_type", _text_1, "space_type", _text_2, 
        "mode", _xifexpression_1, "method", ((Code.Entries) _get));
      final boolean flatIndex = ((index != null) && Objects.equals(index.getFamily().getLiteral(), "flat"));
      int _dimension_4 = f.getDimension();
      Object _get_1 = chosenMethod.get(1);
      OpenSearchIndex.Vector _vector_1 = new OpenSearchIndex.Vector(dataType, _dimension_4, spaceType, chosenMetric, false, ((Double) _get_1), flatIndex);
      _xblockexpression = new OpenSearchIndex.Field(name, mapping, false, _vector_1, false, null);
    }
    return _xblockexpression;
  }

  /**
   * The method of a knn_vector field and the oversample factor its searches rescore with: hnsw of the Faiss engine with m,
   * ef_construction and ef_search from the declared index, and the encoder of a declared quantization (sq with 1, 2, 4 or 16
   * bits, binary with 1 or 2 bits); 7-bit scalar quantization is the sq encoder of the Lucene engine. A flat index keeps the
   * hnsw graph and searches exactly (knn_score). bfloat16 vectors are float values under the sq encoder of type bf16.
   */
  private List<Object> knnMethod(final VectorField f, final VectorIndex index, final String element) {
    List<Object> _xblockexpression = null;
    {
      String engine = "faiss";
      final Code.Entries parameters = Code.entries();
      Code.Entries _xifexpression = null;
      boolean _equals = Objects.equals(element, "bfloat16");
      if (_equals) {
        _xifexpression = OpenSearchBodies.obj("name", OpenSearchBodies.text("sq"), "parameters", OpenSearchBodies.obj("type", OpenSearchBodies.text("bf16"), "bits", OpenSearchBodies.num(16)));
      } else {
        _xifexpression = null;
      }
      Code.Entries encoder = _xifexpression;
      Double oversample = null;
      if ((index != null)) {
        this.note(index, "online");
        if ((index.isOnDisk() && (index.getQuantization() != null))) {
          this.notes.add("onDisk on VectorIndex, whose declared quantization replaces the on_disk mode");
        }
        if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
          throw Unsupported.of("gpu acceleration; GPU index builds run in the remote vector index build service, a separate deployment that the remote_vector_index_build settings point to");
        }
        String _literal = index.getFamily().getLiteral();
        if (_literal != null) {
          switch (_literal) {
            case "auto":
            case "hnsw":
            case "flat":
              break;
            case "ivf":
              throw Unsupported.of("ivf indexes; the Faiss ivf method needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare");
            case "vamana":
              throw Unsupported.of("vamana indexes; DiskANN is the jvector engine of the opensearch-jvector plugin, which the image does not include");
            default:
              String _literal_1 = index.getFamily().getLiteral();
              String _plus = (_literal_1 + " indexes on dense field ");
              String _fieldName = ModelUtil.fieldName(f);
              String _plus_1 = (_plus + _fieldName);
              String _plus_2 = (_plus_1 + "; knn_vector fields are indexed with hnsw");
              throw Unsupported.of(_plus_2);
          }
        } else {
          String _literal_1 = index.getFamily().getLiteral();
          String _plus = (_literal_1 + " indexes on dense field ");
          String _fieldName = ModelUtil.fieldName(f);
          String _plus_1 = (_plus + _fieldName);
          String _plus_2 = (_plus_1 + "; knn_vector fields are indexed with hnsw");
          throw Unsupported.of(_plus_2);
        }
        boolean _written = ModelUtil.written(index, "centroids");
        if (_written) {
          throw Unsupported.of("centroids; Faiss partitions vectors into lists only in its ivf method, which needs a trained model");
        }
        boolean _written_1 = ModelUtil.written(index, "maxDegree");
        if (_written_1) {
          parameters.put("m", OpenSearchBodies.num(index.getMaxDegree()));
        }
        boolean _written_2 = ModelUtil.written(index, "buildCandidates");
        if (_written_2) {
          parameters.put("ef_construction", OpenSearchBodies.num(index.getBuildCandidates()));
        }
        boolean _written_3 = ModelUtil.written(index, "searchCandidates");
        if (_written_3) {
          parameters.put("ef_search", OpenSearchBodies.num(index.getSearchCandidates()));
        }
        final Quantization q = index.getQuantization();
        if (((q != null) && (!Objects.equals(q.getType().getLiteral(), "none")))) {
          this.note(q, "keepOriginals", "truncateDimension");
          boolean _notEquals = (!Objects.equals(element, "float"));
          if (_notEquals) {
            throw Unsupported.of((("quantization of " + element) + " vectors; the Faiss and Lucene encoders quantize float vectors"));
          }
          Code.Entries _switchResult_1 = null;
          String _literal_2 = q.getType().getLiteral();
          if (_literal_2 != null) {
            switch (_literal_2) {
              case "scalar":
                Code.Entries _xblockexpression_1 = null;
                {
                  double _xifexpression_1 = (double) 0;
                  boolean _written_4 = ModelUtil.written(q, "bits");
                  if (_written_4) {
                    _xifexpression_1 = q.getBits();
                  } else {
                    _xifexpression_1 = 16.0;
                  }
                  final double bits = _xifexpression_1;
                  Code.Entries _xifexpression_2 = null;
                  if ((bits == 7.0)) {
                    Code.Entries _xblockexpression_2 = null;
                    {
                      engine = "lucene";
                      _xblockexpression_2 = OpenSearchBodies.obj("name", OpenSearchBodies.text("sq"), "parameters", OpenSearchBodies.obj("bits", OpenSearchBodies.num(7)));
                    }
                    _xifexpression_2 = _xblockexpression_2;
                  } else {
                    Code.Entries _xifexpression_3 = null;
                    if ((bits == 16.0)) {
                      _xifexpression_3 = OpenSearchBodies.obj("name", OpenSearchBodies.text("sq"), "parameters", OpenSearchBodies.obj("type", OpenSearchBodies.text("fp16"), "bits", OpenSearchBodies.num(16)));
                    } else {
                      Code.Entries _xifexpression_4 = null;
                      if ((((bits == 1.0) || (bits == 2.0)) || (bits == 4.0))) {
                        _xifexpression_4 = OpenSearchBodies.obj("name", OpenSearchBodies.text("sq"), "parameters", OpenSearchBodies.obj("bits", OpenSearchBodies.num(bits)));
                      } else {
                        throw Unsupported.of((("scalar quantization to " + Double.valueOf(bits)) + " bits; the Faiss sq encoder quantizes to 1, 2, 4 or 16 bits and the Lucene sq encoder to 1, 2, 4 or 7 bits"));
                      }
                      _xifexpression_3 = _xifexpression_4;
                    }
                    _xifexpression_2 = _xifexpression_3;
                  }
                  _xblockexpression_1 = _xifexpression_2;
                }
                _switchResult_1 = _xblockexpression_1;
                break;
              case "binary":
                Code.Entries _xblockexpression_2 = null;
                {
                  double _xifexpression_1 = (double) 0;
                  boolean _written_4 = ModelUtil.written(q, "bits");
                  if (_written_4) {
                    _xifexpression_1 = q.getBits();
                  } else {
                    _xifexpression_1 = 1.0;
                  }
                  final double bits = _xifexpression_1;
                  if ((((bits != 1.0) && (bits != 2.0)) && (bits != 4.0))) {
                    throw Unsupported.of((("binary quantization with " + Double.valueOf(bits)) + " bits; the Faiss binary encoder keeps 1, 2 or 4 bits per dimension"));
                  }
                  _xblockexpression_2 = OpenSearchBodies.obj("name", OpenSearchBodies.text("binary"), "parameters", OpenSearchBodies.obj("bits", OpenSearchBodies.num(bits)));
                }
                _switchResult_1 = _xblockexpression_2;
                break;
              default:
                throw Unsupported.of("product quantization; the Faiss pq encoder needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare");
            }
          } else {
            throw Unsupported.of("product quantization; the Faiss pq encoder needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare");
          }
          encoder = _switchResult_1;
          if ((q.isRescore() || ModelUtil.written(q, "oversampling"))) {
            double _xifexpression_1 = (double) 0;
            boolean _written_4 = ModelUtil.written(q, "oversampling");
            if (_written_4) {
              _xifexpression_1 = q.getOversampling();
            } else {
              _xifexpression_1 = 1.0;
            }
            oversample = Double.valueOf(_xifexpression_1);
          }
        } else {
          if (((q != null) && (q.isRescore() || ModelUtil.written(q, "oversampling")))) {
            throw Unsupported.of("rescoring without quantization; the rescore of a knn query applies to quantized vectors");
          }
        }
      }
      if ((Objects.equals(engine, "lucene") && parameters.has("ef_search"))) {
        parameters.entries().remove("ef_search");
        this.notes.add("searchCandidates on VectorIndex, which the Lucene engine replaces with k");
      }
      if ((encoder != null)) {
        parameters.put("encoder", encoder);
      }
      Code.Text _text = OpenSearchBodies.text("hnsw");
      Code.Text _text_1 = OpenSearchBodies.text(engine);
      Code.Entries _xifexpression_2 = null;
      boolean _isEmpty = parameters.entries().isEmpty();
      if (_isEmpty) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = parameters;
      }
      final Code.Entries hnsw = OpenSearchBodies.obj("name", _text, "engine", _text_1, "parameters", _xifexpression_2);
      final List<Object> result = CollectionLiterals.<Object>newArrayList();
      result.add(hnsw);
      result.add(oversample);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public VectorIndex indexOf(final VectorCollection c, final VectorField f) {
    return ModelUtil.vectorIndex(c, f);
  }

  /**
   * The metric of a vector field: the field's, else its index's, else cosine.
   */
  public String metric(final VectorField f, final VectorIndex index) {
    String _xblockexpression = null;
    {
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(f, "metric");
      if (_written) {
        _xifexpression = f.getMetric();
      } else {
        EMetric _xifexpression_1 = null;
        if (((index != null) && ModelUtil.written(index, "metric"))) {
          _xifexpression_1 = index.getMetric();
        } else {
          EMetric _xifexpression_2 = null;
          String _literal = f.getType().getLiteral();
          boolean _equals = Objects.equals(_literal, "sparse_float_vector");
          if (_equals) {
            _xifexpression_2 = EMetric.DOT;
          } else {
            _xifexpression_2 = EMetric.COSINE;
          }
          _xifexpression_1 = _xifexpression_2;
        }
        _xifexpression = _xifexpression_1;
      }
      final EMetric chosen = _xifexpression;
      _xblockexpression = chosen.getLiteral();
    }
    return _xblockexpression;
  }

  public static boolean bm25(final Analyzer analyzer) {
    return (ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"));
  }

  /**
   * The custom analyzer of an analyzer declaration with its filters, as index settings.
   */
  public Code.Entries analysis(final List<Analyzer> analyzers) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries analyzerSettings = Code.entries();
      final Code.Entries filterSettings = Code.entries();
      for (final Analyzer analyzer : analyzers) {
        {
          boolean _isEmpty = analyzer.getResources().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of(("analyzer resources; " + OpenSearchStatements.NO_FILES));
          }
          final String tokenizer = OpenSearchSchema.TOKENIZERS.get(analyzer.getTokenizer().getLiteral());
          if ((tokenizer == null)) {
            String _literal = analyzer.getTokenizer().getLiteral();
            String _plus = ("the " + _literal);
            String _plus_1 = (_plus + " tokenizer; the icu tokenizer is in the analysis-icu plugin, which the image does not include");
            throw Unsupported.of(_plus_1);
          }
          String _xifexpression = null;
          String _language = analyzer.getLanguage();
          boolean _tripleEquals = (_language == null);
          if (_tripleEquals) {
            _xifexpression = null;
          } else {
            _xifexpression = OpenSearchSchema.LANGUAGES.get(analyzer.getLanguage().toLowerCase());
          }
          final String language = _xifexpression;
          if (((analyzer.getLanguage() != null) && (language == null))) {
            String _language_1 = analyzer.getLanguage();
            String _plus_2 = ("the language " + _language_1);
            String _plus_3 = (_plus_2 + "; ");
            String _plus_4 = (_plus_3 + OpenSearchStatements.LANGUAGES_REFERENCE);
            throw Unsupported.of(_plus_4);
          }
          final String name = OpenSearchBodies.aliasName(analyzer.getName());
          final List<Code.Expr> chain = CollectionLiterals.<Code.Expr>newArrayList();
          Code.Text _text = OpenSearchBodies.text("lowercase");
          chain.add(_text);
          EList<AnalyzerFilter> _filters = analyzer.getFilters();
          for (final AnalyzerFilter analyzerFilter : _filters) {
            {
              String _switchResult = null;
              String _literal_1 = analyzerFilter.getType().getLiteral();
              if (_literal_1 != null) {
                switch (_literal_1) {
                  case "lowercase":
                    _switchResult = null;
                    break;
                  case "stop":
                    String _xblockexpression_1 = null;
                    {
                      if ((language == null)) {
                        throw Unsupported.of("the stop filter without a language; the stop filter takes the stop words of a language");
                      }
                      final String filterName = (name + "_stop");
                      filterSettings.put(filterName, OpenSearchBodies.obj("type", OpenSearchBodies.text("stop"), "stopwords", OpenSearchBodies.text((("_" + language) + "_"))));
                      _xblockexpression_1 = filterName;
                    }
                    _switchResult = _xblockexpression_1;
                    break;
                  case "stemmer":
                    String _xblockexpression_2 = null;
                    {
                      if ((language == null)) {
                        throw Unsupported.of("the stemmer filter without a language");
                      }
                      final String filterName = (name + "_stemmer");
                      filterSettings.put(filterName, OpenSearchBodies.obj("type", OpenSearchBodies.text("stemmer"), "language", OpenSearchBodies.text(language)));
                      _xblockexpression_2 = filterName;
                    }
                    _switchResult = _xblockexpression_2;
                    break;
                  case "ascii_folding":
                    _switchResult = "asciifolding";
                    break;
                  case "length":
                    throw Unsupported.of("the length filter; vecDSL gives no bounds, and without them the OpenSearch length filter (min 0, max Integer.MAX_VALUE) keeps every token");
                  case "synonym":
                    throw Unsupported.of(("the synonym filter; vecDSL declares synonyms in file resources, " + OpenSearchStatements.NO_FILES));
                  case "decompounder":
                    throw Unsupported.of(("the decompounder filter; dictionary_decompounder needs a word list, " + OpenSearchStatements.NO_FILES));
                  default:
                    throw Unsupported.of("the phonetic filter; it is in the analysis-phonetic plugin, which the image does not include");
                }
              } else {
                throw Unsupported.of("the phonetic filter; it is in the analysis-phonetic plugin, which the image does not include");
              }
              final String step = _switchResult;
              if ((step != null)) {
                Code.Text _text_1 = OpenSearchBodies.text(step);
                chain.add(_text_1);
              }
            }
          }
          if (((language != null) && analyzer.getFilters().isEmpty())) {
            filterSettings.put((name + "_stop"), OpenSearchBodies.obj("type", OpenSearchBodies.text("stop"), "stopwords", OpenSearchBodies.text((("_" + language) + "_"))));
            filterSettings.put((name + "_stemmer"), OpenSearchBodies.obj("type", OpenSearchBodies.text("stemmer"), "language", OpenSearchBodies.text(language)));
            Code.Text _text_1 = OpenSearchBodies.text((name + "_stop"));
            chain.add(_text_1);
            Code.Text _text_2 = OpenSearchBodies.text((name + "_stemmer"));
            chain.add(_text_2);
          }
          if ((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "none")))) {
            if (((!analyzer.getStopwords().startsWith("_")) || (!analyzer.getStopwords().endsWith("_")))) {
              String _stopwords = analyzer.getStopwords();
              String _plus_5 = ("stopwords " + _stopwords);
              String _plus_6 = (_plus_5 + "; OpenSearch names built-in stop-word lists _english_, _german_ and so on");
              throw Unsupported.of(_plus_6);
            }
            filterSettings.put((name + "_stopwords"), OpenSearchBodies.obj("type", OpenSearchBodies.text("stop"), "stopwords", OpenSearchBodies.text(analyzer.getStopwords())));
            Code.Text _text_3 = OpenSearchBodies.text((name + "_stopwords"));
            chain.add(_text_3);
          }
          analyzerSettings.put(name, OpenSearchBodies.obj("type", OpenSearchBodies.text("custom"), "tokenizer", OpenSearchBodies.text(tokenizer), "filter", OpenSearchBodies.arr(chain)));
          Set<String> _keySet = ModelUtil.extParams(analyzer, OpenSearchGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
        }
      }
      Code.Entries _xifexpression = null;
      boolean _isEmpty = analyzerSettings.entries().isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = analyzerSettings;
      }
      Code.Entries _xifexpression_1 = null;
      boolean _isEmpty_1 = filterSettings.entries().isEmpty();
      if (_isEmpty_1) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = filterSettings;
      }
      _xblockexpression = OpenSearchBodies.obj("analyzer", _xifexpression, "filter", _xifexpression_1);
    }
    return _xblockexpression;
  }

  /**
   * BM25 similarities of analyzers with k1 or b, named after the analyzer.
   */
  public Code.Entries similarities(final List<Analyzer> analyzers) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      final Function1<Analyzer, Boolean> _function = (Analyzer a) -> {
        return Boolean.valueOf(OpenSearchSchema.bm25(a));
      };
      Iterable<Analyzer> _filter = IterableExtensions.<Analyzer>filter(analyzers, _function);
      for (final Analyzer analyzer : _filter) {
        String _aliasName = OpenSearchBodies.aliasName(analyzer.getName());
        String _plus = (_aliasName + "_bm25");
        Code.Text _text = OpenSearchBodies.text("BM25");
        Code.Num _xifexpression = null;
        boolean _written = ModelUtil.written(analyzer, "bm25K1");
        if (_written) {
          _xifexpression = OpenSearchBodies.num(analyzer.getBm25K1());
        } else {
          _xifexpression = null;
        }
        Code.Num _xifexpression_1 = null;
        boolean _written_1 = ModelUtil.written(analyzer, "bm25B");
        if (_written_1) {
          _xifexpression_1 = OpenSearchBodies.num(analyzer.getBm25B());
        } else {
          _xifexpression_1 = null;
        }
        result.put(_plus, OpenSearchBodies.obj("type", _text, "k1", _xifexpression, 
          "b", _xifexpression_1));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The create-index request of a collection: settings (shards, replicas, analysis, similarities, field limits), the mapping
   * and the aliases. With if-not-exists the request runs only when the index is missing (HEAD /index).
   */
  public List<Code.Stmt> createIndex(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(OpenSearchStatements.EXTERNAL);
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(OpenSearchStatements.NO_TTL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of((("auto-id; " + OpenSearchStatements.PENDING) + " (documents indexed without _id get generated ids)"));
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(OpenSearchStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; OpenSearch has no references between indices (joins only within one index, through the join field)");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + OpenSearchStatements.NO_MODELS));
      }
      boolean _isEmpty_1 = c.getFunctions().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = OpenSearchStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      this.note(c, "idType", "generativeModel", "consistency");
      if (fresh) {
        this.evolve(c, null);
      }
      final OpenSearchIndex current = this.index(c);
      Code.Num _num = OpenSearchBodies.num(1);
      Code.Num _num_1 = OpenSearchBodies.num(0);
      Code.Flag _flag = new Code.Flag(true);
      Code.Flag _flag_1 = new Code.Flag(false);
      final Code.Entries settings = OpenSearchBodies.obj("number_of_shards", _num, "number_of_replicas", _num_1, "index.knn", _flag, 
        "index.knn.derived_source.enabled", _flag_1);
      OpenSearchStatements.storage(c.getStorage(), settings);
      Cluster _elvis = null;
      Cluster _cluster = c.getCluster();
      if (_cluster != null) {
        _elvis = _cluster;
      } else {
        Cluster _containerOfType = EcoreUtil2.<Cluster>getContainerOfType(c, Cluster.class);
        _elvis = _containerOfType;
      }
      OpenSearchStatements.clusterSettings(_elvis, settings);
      EList<CollectionLimit> _limits = c.getLimits();
      for (final CollectionLimit limit : _limits) {
        OpenSearchStatements.collectionLimit(limit, settings);
      }
      Set<String> _keySet = ModelUtil.extParams(c, OpenSearchGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final Code.Entries analysisSettings = this.analysis(c.getAnalyzers());
      boolean _isEmpty_2 = analysisSettings.entries().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        settings.put("analysis", analysisSettings);
      }
      final Code.Entries similaritySettings = this.similarities(c.getAnalyzers());
      boolean _isEmpty_3 = similaritySettings.entries().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        settings.put("similarity", similaritySettings);
      }
      Code.Text _xifexpression = null;
      String _description = c.getDescription();
      boolean _tripleEquals = (_description == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.text(c.getDescription());
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_4 = c.getTags().isEmpty();
      if (_isEmpty_4) {
        _xifexpression_1 = null;
      } else {
        final Function1<String, Code.Expr> _function = (String t) -> {
          Code.Text _text = OpenSearchBodies.text(t);
          return ((Code.Expr) _text);
        };
        _xifexpression_1 = OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(c.getTags(), _function)));
      }
      final Code.Entries meta = OpenSearchBodies.obj("description", _xifexpression, 
        "tags", _xifexpression_1);
      String _xifexpression_2 = null;
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        _xifexpression_2 = "true";
      } else {
        _xifexpression_2 = "strict";
      }
      Code.Text _text = OpenSearchBodies.text(_xifexpression_2);
      Code.Entries _xifexpression_3 = null;
      boolean _isEmpty_5 = meta.entries().isEmpty();
      if (_isEmpty_5) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = meta;
      }
      final Code.Entries mappings = OpenSearchBodies.obj("dynamic", _text, "_meta", _xifexpression_3, "properties", this.properties(current));
      boolean _isEmpty_6 = c.getStoredSourceFields().isEmpty();
      boolean _not_4 = (!_isEmpty_6);
      if (_not_4) {
        final Function1<Field, Code.Expr> _function_1 = (Field s) -> {
          Code.Text _text_1 = OpenSearchBodies.text(ModelUtil.fieldName(s));
          return ((Code.Expr) _text_1);
        };
        mappings.put("_source", OpenSearchBodies.obj("includes", OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Field, Code.Expr>map(c.getStoredSourceFields(), _function_1)))));
      }
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex payloadIndex : _filter) {
        this.checkPayloadIndex(current, payloadIndex);
      }
      final Code.Entries aliases = Code.entries();
      EList<Alias> _aliases = c.getAliases();
      for (final Alias alias : _aliases) {
        aliases.put(OpenSearchBodies.aliasName(alias.getName()), Code.entries());
      }
      EList<Tenant> _tenants = c.getTenants();
      for (final Tenant tenant : _tenants) {
        aliases.put(OpenSearchBodies.sliceAlias(current.name(), "tenant", tenant.getName()), OpenSearchBodies.obj("filter", OpenSearchSchema.term(current.tenant(), OpenSearchBodies.text(tenant.getName()))));
      }
      EList<Namespace> _namespaces = c.getNamespaces();
      for (final Namespace namespace : _namespaces) {
        aliases.put(OpenSearchBodies.sliceAlias(current.name(), "namespace", namespace.getKey()), OpenSearchBodies.obj("filter", OpenSearchSchema.term(current.namespace(), OpenSearchBodies.text(namespace.getKey()))));
      }
      List<Partition> _partitions = ModelUtil.partitions(c);
      for (final Partition partition : _partitions) {
        {
          OpenSearchStatements.checkPartition(partition);
          aliases.put(OpenSearchBodies.sliceAlias(current.name(), "partition", partition.getName()), OpenSearchBodies.obj("filter", OpenSearchSchema.term(current.partition(), OpenSearchBodies.text(partition.getName()))));
        }
      }
      Code.Entries _xifexpression_4 = null;
      boolean _isEmpty_7 = aliases.entries().isEmpty();
      if (_isEmpty_7) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = aliases;
      }
      final Code.Entries body = OpenSearchBodies.obj("settings", settings, "mappings", mappings, "aliases", _xifexpression_4);
      String _name = current.name();
      String _plus_2 = ("/" + _name);
      Code.Call _request = OpenSearchBodies.request("PUT", _plus_2, null, body);
      final Code.Run creation = new Code.Run(_request);
      ArrayList<Code.Stmt> _xifexpression_5 = null;
      if (ifNotExists) {
        String _name_1 = current.name();
        String _plus_3 = ("/" + _name_1);
        Code.Call _call = OpenSearchBodies.call("exists", "path", OpenSearchBodies.text(_plus_3));
        Code.When _when = new Code.When(_call, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(creation)));
        _xifexpression_5 = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
      } else {
        _xifexpression_5 = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) creation));
      }
      _xblockexpression = _xifexpression_5;
    }
    return _xblockexpression;
  }

  /**
   * The properties of an index: its fields and the hidden keyword fields of tenants, namespaces and partitions.
   */
  public Code.Entries properties(final OpenSearchIndex current) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<OpenSearchIndex.Field> _fields = current.fields();
      for (final OpenSearchIndex.Field f : _fields) {
        result.put(f.name(), f.mapping());
      }
      String _tenant = current.tenant();
      String _namespace = current.namespace();
      String _partition = current.partition();
      Iterable<String> _filterNull = IterableExtensions.<String>filterNull(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_tenant, _namespace, _partition)));
      for (final String hidden : _filterNull) {
        boolean _has = result.has(hidden);
        boolean _not = (!_has);
        if (_not) {
          result.put(hidden, OpenSearchBodies.obj("type", OpenSearchBodies.text("keyword")));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A declared payload index: every mapped field is indexed already (index: true), so the declaration is checked against the
   * field's mapping; lowercase and prefix-match need mapping parameters that only a new field can take.
   */
  public void checkPayloadIndex(final OpenSearchIndex current, final PayloadIndex payloadIndex) {
    this.note(payloadIndex, "onDisk", "minTokenLength", "maxTokenLength");
    Set<String> _keySet = ModelUtil.extParams(payloadIndex, OpenSearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    if ((ModelUtil.written(payloadIndex, "structure") && Objects.equals(payloadIndex.getStructure().getLiteral(), "bitmap"))) {
      throw Unsupported.of("bitmap payload indexes; OpenSearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type");
    }
    boolean _isLowercase = payloadIndex.isLowercase();
    if (_isLowercase) {
      throw Unsupported.of((("lowercase payload indexes; " + OpenSearchStatements.PENDING) + " (a keyword field with a lowercase normalizer)"));
    }
    boolean _isPrefixMatch = payloadIndex.isPrefixMatch();
    if (_isPrefixMatch) {
      throw Unsupported.of((("prefix-match payload indexes; " + OpenSearchStatements.PENDING) + " (index_prefixes on a text field)"));
    }
    Analyzer _analyzer = payloadIndex.getAnalyzer();
    boolean _tripleNotEquals = (_analyzer != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("an analyzer on a payload index; the analyzer of a text field is part of its mapping (declare it on the field)");
    }
    final FieldPath path = payloadIndex.getTarget();
    String _rawPath = path.getRawPath();
    boolean _tripleNotEquals_1 = (_rawPath != null);
    if (_tripleNotEquals_1) {
      return;
    }
    Field _field = path.getField();
    boolean _not = (!(_field instanceof PayloadField));
    if (_not) {
      throw Unsupported.of("a payload index on a vector field; knn_vector fields are indexed through their method");
    }
    final Field leaf = ModelUtil.leaf(path);
    final OpenSearchIndex.Field target = current.field(ModelUtil.fieldName(path.getField()));
    final String kind = payloadIndex.getKind().getLiteral();
    if ((Objects.equals(kind, "full_text") && (!(target.fullText() || ((leaf instanceof PayloadField) && ((PayloadField) leaf).isFullText()))))) {
      String _name = target.name();
      String _plus = ("a full_text index on " + _name);
      String _plus_1 = (_plus + ", which is not a full-text field (OpenSearch fixes the type of a field when it is mapped)");
      throw Unsupported.of(_plus_1);
    }
    if ((((Objects.equals(kind, "geo") && (leaf instanceof PayloadField)) && (!Objects.equals(((PayloadField) leaf).getType(), EScalarDType.GEO_POINT))) && (!Objects.equals(((PayloadField) leaf).getType(), EScalarDType.GEOMETRY)))) {
      throw Unsupported.of("a geo index on a field that is not geo_point or geometry");
    }
  }

  public static Code.Entries term(final String fieldName, final Code.Expr value) {
    return OpenSearchBodies.obj("term", OpenSearchBodies.obj(fieldName, value));
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
}
