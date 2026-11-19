package org.ftn.vecDSL.generator.elasticsearch;

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
 * Maps vecDSL collections to Elasticsearch indices (server 9.5): one index per collection with an explicit mapping (dynamic
 * strict unless the collection is dynamic), one shard and no replica unless the collection declares sharding or replication,
 * analyzers and BM25 parameters in the index settings, and declared tenants, namespaces, partitions and aliases as aliases
 * (filtered for the slices). Payload fields are keyword, numeric, date, boolean, object, nested and geo fields; full-text
 * fields are text fields with a keyword sub-field for exact filters; vector fields are dense_vector (or sparse_vector) fields.
 */
@SuppressWarnings("all")
public class ElasticsearchSchema {
  private static final Map<EScalarDType, String> NUMBERS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "integer"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "long"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "byte"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "float"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "double"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "boolean"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "keyword"), Pair.<EScalarDType, String>of(EScalarDType.BYTES, "binary"), Pair.<EScalarDType, String>of(EScalarDType.GEO_POINT, "geo_point"), Pair.<EScalarDType, String>of(EScalarDType.GEOMETRY, "geo_shape")));

  private static final Map<EScalarDType, String> DATES = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.DATE, "strict_date"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "strict_date_optional_time"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "strict_date_optional_time")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<String, String> TOKENIZERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("standard", "standard"), Pair.<String, String>of("whitespace", "whitespace"), Pair.<String, String>of("keyword", "keyword"), Pair.<String, String>of("ngram", "ngram"), Pair.<String, String>of("edge_ngram", "edge_ngram"), Pair.<String, String>of("language", "standard")));

  private static final Map<String, String> LANGUAGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("en", "english"), Pair.<String, String>of("english", "english"), Pair.<String, String>of("de", "german"), Pair.<String, String>of("german", "german"), Pair.<String, String>of("fr", "french"), Pair.<String, String>of("french", "french"), Pair.<String, String>of("es", "spanish"), Pair.<String, String>of("spanish", "spanish"), Pair.<String, String>of("it", "italian"), Pair.<String, String>of("italian", "italian"), Pair.<String, String>of("pt", "portuguese"), Pair.<String, String>of("portuguese", "portuguese"), Pair.<String, String>of("nl", "dutch"), Pair.<String, String>of("dutch", "dutch"), Pair.<String, String>of("ru", "russian"), Pair.<String, String>of("russian", "russian"), Pair.<String, String>of("sv", "swedish"), Pair.<String, String>of("swedish", "swedish"), Pair.<String, String>of("da", "danish"), Pair.<String, String>of("danish", "danish"), Pair.<String, String>of("fi", "finnish"), Pair.<String, String>of("finnish", "finnish"), Pair.<String, String>of("no", "norwegian"), Pair.<String, String>of("norwegian", "norwegian"), Pair.<String, String>of("hu", "hungarian"), Pair.<String, String>of("hungarian", "hungarian"), Pair.<String, String>of("ro", "romanian"), Pair.<String, String>of("romanian", "romanian"), Pair.<String, String>of("tr", "turkish"), Pair.<String, String>of("turkish", "turkish"), Pair.<String, String>of("sr", "serbian"), Pair.<String, String>of("serbian", "serbian")));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, ElasticsearchIndex> evolved = CollectionLiterals.<VectorCollection, ElasticsearchIndex>newHashMap();

  public ElasticsearchSchema(final List<String> notes, final Script script) {
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
  public ElasticsearchIndex index(final VectorCollection c) {
    ElasticsearchIndex _elvis = null;
    ElasticsearchIndex _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      ElasticsearchIndex _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final ElasticsearchIndex next) {
    if ((next == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, next);
    }
  }

  private ElasticsearchIndex declared(final VectorCollection c) {
    ElasticsearchIndex _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!ElasticsearchSchema.KEYS.contains(((PayloadField) key).getType())))) {
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
      final List<ElasticsearchIndex.Field> fields = CollectionLiterals.<ElasticsearchIndex.Field>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field declaredField : _fields) {
        ElasticsearchIndex.Field _field = this.field(c, declaredField);
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
          _xifexpression_2 = ElasticsearchBodies.TENANT_FIELD;
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
          _xifexpression_4 = ElasticsearchBodies.NAMESPACE_FIELD;
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
        _xifexpression_5 = ElasticsearchBodies.PARTITION_FIELD;
      }
      final String partition = _xifexpression_5;
      String _indexName = ElasticsearchBodies.indexName(ElasticsearchSchema.databaseOf(c), c.getName());
      String _fieldName = ModelUtil.fieldName(key);
      HashMap<String, String> _newHashMap = CollectionLiterals.<String, String>newHashMap();
      String _databaseOf = ElasticsearchSchema.databaseOf(c);
      _xblockexpression = new ElasticsearchIndex(_indexName, _fieldName, fields, tenant, namespace, partition, _newHashMap, _databaseOf);
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

  public ElasticsearchIndex.Field field(final VectorCollection c, final Field declaredField) {
    ElasticsearchIndex.Field _switchResult = null;
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

  private ElasticsearchIndex.Field payloadField(final VectorCollection c, final PayloadField f) {
    ElasticsearchIndex.Field _xblockexpression = null;
    {
      final String name = ElasticsearchBodies.field(ModelUtil.fieldName(f));
      this.note(f, "nullable", "documentBody");
      boolean _isEmpty = f.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _literal = IterableExtensions.<FieldLimit>head(f.getLimits()).getType().getLiteral();
        String _plus = ("field limits (" + _literal);
        String _plus_1 = (_plus + "); ");
        String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
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
            final ElasticsearchIndex.Field inner = this.field(c, sub);
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
        Code.Text _text = ElasticsearchBodies.text(_xifexpression_1);
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
          _xifexpression_3 = ElasticsearchBodies.text("true");
        } else {
          _xifexpression_3 = null;
        }
        final Code.Entries mapping = ElasticsearchBodies.obj("type", _text, "properties", _xifexpression_2, 
          "dynamic", _xifexpression_3);
        boolean _isNested_1 = f.isNested();
        return new ElasticsearchIndex.Field(name, mapping, false, null, _isNested_1, defaultValue);
      }
      if ((Objects.equals(f.getType(), EScalarDType.JSON) || Objects.equals(f.getType(), EScalarDType.MAP))) {
        boolean _isFullText = f.isFullText();
        if (_isFullText) {
          throw Unsupported.of((("full-text search on json field " + name) + "; a json field is an object whose sub-fields Elasticsearch maps when they arrive"));
        }
        Code.Entries _obj = ElasticsearchBodies.obj("type", ElasticsearchBodies.text("object"), "dynamic", ElasticsearchBodies.text("true"));
        return new ElasticsearchIndex.Field(name, _obj, false, null, false, defaultValue);
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
            String _plus_8 = (_plus_7 + ElasticsearchStatements.PENDING);
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
        Code.Text _text_1 = ElasticsearchBodies.text("text");
        Code.Text _xifexpression_5 = null;
        if ((analyzer == null)) {
          _xifexpression_5 = null;
        } else {
          _xifexpression_5 = ElasticsearchBodies.text(ElasticsearchBodies.aliasName(analyzer.getName()));
        }
        Code.Text _xifexpression_6 = null;
        if (((analyzer != null) && ElasticsearchSchema.bm25(analyzer))) {
          String _aliasName = ElasticsearchBodies.aliasName(analyzer.getName());
          String _plus_7 = (_aliasName + "_bm25");
          _xifexpression_6 = ElasticsearchBodies.text(_plus_7);
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
        final Code.Entries mapping_1 = ElasticsearchBodies.obj("type", _text_1, "analyzer", _xifexpression_5, 
          "similarity", _xifexpression_6, 
          "fields", ElasticsearchBodies.obj(ElasticsearchBodies.RAW, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("keyword"), "ignore_above", ElasticsearchBodies.num(_xifexpression_7))));
        return new ElasticsearchIndex.Field(name, mapping_1, true, null, false, defaultValue);
      }
      final Code.Entries mapping_2 = this.scalar(scalarType, name, f);
      _xblockexpression = new ElasticsearchIndex.Field(name, mapping_2, false, null, false, defaultValue);
    }
    return _xblockexpression;
  }

  private Code.Entries scalar(final EScalarDType type, final String name, final PayloadField f) {
    Code.Entries _xblockexpression = null;
    {
      final String date = ElasticsearchSchema.DATES.get(type);
      if ((date != null)) {
        return ElasticsearchBodies.obj("type", ElasticsearchBodies.text("date"), "format", ElasticsearchBodies.text(date));
      }
      final String mapped = ElasticsearchSchema.NUMBERS.get(type);
      if ((mapped == null)) {
        String _literal = type.getLiteral();
        String _plus = (_literal + " field ");
        String _plus_1 = (_plus + name);
        String _plus_2 = (_plus_1 + "; Elasticsearch stores media as URLs or binary (base64) values, not ");
        String _literal_1 = type.getLiteral();
        String _plus_3 = (_plus_2 + _literal_1);
        String _plus_4 = (_plus_3 + " objects");
        throw Unsupported.of(_plus_4);
      }
      if ((Objects.equals(mapped, "keyword") && ModelUtil.written(f, "maxLength"))) {
        return ElasticsearchBodies.obj("type", ElasticsearchBodies.text("keyword"), "ignore_above", ElasticsearchBodies.num(f.getMaxLength()));
      }
      _xblockexpression = ElasticsearchBodies.obj("type", ElasticsearchBodies.text(mapped));
    }
    return _xblockexpression;
  }

  private ElasticsearchIndex.Field vectorField(final VectorCollection c, final VectorField f) {
    ElasticsearchIndex.Field _xblockexpression = null;
    {
      final String name = ElasticsearchBodies.field(ModelUtil.fieldName(f));
      boolean _isMultiVector = f.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; Elasticsearch stores several vectors per document in rank_vectors fields, which need an enterprise license (RankVectorsPlugin)"));
      }
      if (((f.getModel() != null) || (ModelUtil.producingFunction(c, f) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + ElasticsearchStatements.NO_MODELS));
      }
      this.note(f, "onDisk", "nullable");
      boolean _isEmpty = f.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _literal = IterableExtensions.<FieldLimit>head(f.getLimits()).getType().getLiteral();
        String _plus = ("field limits (" + _literal);
        String _plus_1 = (_plus + "); ");
        String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
        String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
        throw Unsupported.of(_plus_3);
      }
      final VectorIndex index = this.indexOf(c, f);
      String _literal_1 = f.getType().getLiteral();
      boolean _equals = Objects.equals(_literal_1, "sparse_float_vector");
      if (_equals) {
        if ((ModelUtil.written(f, "modifier") && Objects.equals(f.getModifier().getLiteral(), "idf"))) {
          throw Unsupported.of((("the idf modifier on " + name) + "; sparse_vector fields score with the weights as given"));
        }
        final String sparseMetric = this.metric(f, index);
        boolean _notEquals = (!Objects.equals(sparseMetric, "dot"));
        if (_notEquals) {
          throw Unsupported.of((((("the " + sparseMetric) + " metric on sparse field ") + name) + "; sparse_vector fields score by the dot product of the weights"));
        }
        if (((index != null) && (!Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("auto", "sparse_inverted")).contains(index.getFamily().getLiteral())))) {
          String _literal_2 = index.getFamily().getLiteral();
          String _plus_4 = (_literal_2 + " indexes on sparse field ");
          String _plus_5 = (_plus_4 + name);
          String _plus_6 = (_plus_5 + "; sparse_vector fields are inverted indexes");
          throw Unsupported.of(_plus_6);
        }
        Code.Flag _flag = new Code.Flag(false);
        Code.Entries _obj = ElasticsearchBodies.obj("type", ElasticsearchBodies.text("sparse_vector"), "index_options", ElasticsearchBodies.obj("prune", _flag));
        ElasticsearchIndex.Vector _vector = new ElasticsearchIndex.Vector("sparse", 0, "dot_product", "dot", true);
        return new ElasticsearchIndex.Field(name, _obj, false, _vector, false, null);
      }
      String _switchResult = null;
      String _literal_3 = f.getType().getLiteral();
      if (_literal_3 != null) {
        switch (_literal_3) {
          case "float_vector":
            _switchResult = "float";
            break;
          case "bfloat16_vector":
            _switchResult = "bfloat16";
            break;
          case "int8_vector":
            _switchResult = "byte";
            break;
          case "binary_vector":
            _switchResult = "bit";
            break;
          default:
            String _literal_4 = f.getType().getLiteral();
            String _plus_7 = (_literal_4 + " fields; Elasticsearch dense_vector fields hold float, bfloat16, byte (signed int8) and bit elements");
            throw Unsupported.of(_plus_7);
        }
      } else {
        String _literal_4 = f.getType().getLiteral();
        String _plus_7 = (_literal_4 + " fields; Elasticsearch dense_vector fields hold float, bfloat16, byte (signed int8) and bit elements");
        throw Unsupported.of(_plus_7);
      }
      final String element = _switchResult;
      int _dimension = f.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      if ((Objects.equals(element, "bit") && ((f.getDimension() % 8) != 0))) {
        int _dimension_1 = f.getDimension();
        String _plus_8 = ((("bit vector field " + name) + " with ") + Integer.valueOf(_dimension_1));
        String _plus_9 = (_plus_8 + " dimensions; bit vectors have a multiple of 8");
        throw Unsupported.of(_plus_9);
      }
      int _xifexpression = (int) 0;
      boolean _equals_1 = Objects.equals(element, "bit");
      if (_equals_1) {
        _xifexpression = 32768;
      } else {
        _xifexpression = 4096;
      }
      final int limit = _xifexpression;
      int _dimension_2 = f.getDimension();
      boolean _greaterThan = (_dimension_2 > limit);
      if (_greaterThan) {
        int _dimension_3 = f.getDimension();
        String _plus_10 = ((("vector field " + name) + " with ") + Integer.valueOf(_dimension_3));
        String _plus_11 = (_plus_10 + " dimensions; dense_vector fields have at most ");
        String _plus_12 = (_plus_11 + Integer.valueOf(limit));
        throw Unsupported.of(_plus_12);
      }
      final String chosenMetric = this.metric(f, index);
      final String sim = ElasticsearchBodies.similarity(chosenMetric, element, f.isNormalized());
      Code.Text _text = ElasticsearchBodies.text("dense_vector");
      Code.Text _text_1 = ElasticsearchBodies.text(element);
      Code.Num _num = ElasticsearchBodies.num(f.getDimension());
      Code.Flag _flag_1 = new Code.Flag(true);
      final Code.Entries mapping = ElasticsearchBodies.obj("type", _text, "element_type", _text_1, "dims", _num, "index", _flag_1, 
        "similarity", ElasticsearchBodies.text(sim), "index_options", this.indexOptions(f, index, element));
      int _dimension_4 = f.getDimension();
      ElasticsearchIndex.Vector _vector_1 = new ElasticsearchIndex.Vector(element, _dimension_4, sim, chosenMetric, false);
      _xblockexpression = new ElasticsearchIndex.Field(name, mapping, false, _vector_1, false, null);
    }
    return _xblockexpression;
  }

  /**
   * The index_options of a dense_vector field: hnsw (raw vectors) without a declared index, so that scores are exact; hnsw or
   * flat with int8, int4 or bbq quantization from the declared index. The server default for new float fields (int8_hnsw,
   * bbq_hnsw from 384 dimensions) is always overridden, since its scores are quantized approximations.
   */
  private Code.Entries indexOptions(final VectorField f, final VectorIndex index, final String element) {
    Code.Entries _xblockexpression = null;
    {
      if ((index == null)) {
        return ElasticsearchBodies.obj("type", ElasticsearchBodies.text("hnsw"));
      }
      this.note(index, "onDisk", "online");
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; GPU indexing needs an enterprise license (GPUPlugin, GPU_INDEXING_FEATURE)");
      }
      String _switchResult = null;
      String _literal = index.getFamily().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "auto":
          case "hnsw":
            _switchResult = "hnsw";
            break;
          case "flat":
            _switchResult = "flat";
            break;
          case "ivf":
            throw Unsupported.of("ivf indexes; the IVF index of Elasticsearch (bbq_disk) needs an enterprise license (DiskBBQPlugin, DISK_BBQ_FEATURE)");
          default:
            String _literal_1 = index.getFamily().getLiteral();
            String _plus = (_literal_1 + " indexes; Elasticsearch builds hnsw and flat indexes for dense_vector fields");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus = (_literal_1 + " indexes; Elasticsearch builds hnsw and flat indexes for dense_vector fields");
        throw Unsupported.of(_plus);
      }
      final String family = _switchResult;
      if ((Objects.equals(family, "flat") && (ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates")))) {
        throw Unsupported.of("max-degree and build-candidates on a flat index");
      }
      boolean _written = ModelUtil.written(index, "centroids");
      if (_written) {
        throw Unsupported.of("centroids; Elasticsearch partitions vectors into clusters only in its IVF index (bbq_disk), which needs an enterprise license");
      }
      String prefix = "";
      Code.Expr rescore = null;
      final Quantization q = index.getQuantization();
      if ((q != null)) {
        this.note(q, "keepOriginals", "truncateDimension");
        if (((!Objects.equals(element, "float")) && (!Objects.equals(q.getType().getLiteral(), "none")))) {
          throw Unsupported.of((("quantization of " + element) + " vectors; Elasticsearch quantizes float vectors"));
        }
        String _switchResult_1 = null;
        String _literal_2 = q.getType().getLiteral();
        if (_literal_2 != null) {
          switch (_literal_2) {
            case "none":
              _switchResult_1 = "";
              break;
            case "scalar":
              String _xblockexpression_1 = null;
              {
                double _xifexpression = (double) 0;
                boolean _written_1 = ModelUtil.written(q, "bits");
                if (_written_1) {
                  _xifexpression = q.getBits();
                } else {
                  _xifexpression = 8.0;
                }
                final double bits = _xifexpression;
                String _xifexpression_1 = null;
                if ((bits == 8.0)) {
                  _xifexpression_1 = "int8_";
                } else {
                  String _xifexpression_2 = null;
                  if ((bits == 4.0)) {
                    _xifexpression_2 = "int4_";
                  } else {
                    throw Unsupported.of((("scalar quantization to " + Double.valueOf(bits)) + " bits; Elasticsearch quantizes to int8 and int4"));
                  }
                  _xifexpression_1 = _xifexpression_2;
                }
                _xblockexpression_1 = _xifexpression_1;
              }
              _switchResult_1 = _xblockexpression_1;
              break;
            case "binary":
              String _xblockexpression_2 = null;
              {
                if ((ModelUtil.written(q, "bits") && (q.getBits() != 1.0))) {
                  double _bits = q.getBits();
                  String _plus_1 = ("binary quantization with " + Double.valueOf(_bits));
                  String _plus_2 = (_plus_1 + " bits; bbq keeps one bit per dimension");
                  throw Unsupported.of(_plus_2);
                }
                int _dimension = f.getDimension();
                boolean _lessThan = (_dimension < 64);
                if (_lessThan) {
                  int _dimension_1 = f.getDimension();
                  String _plus_3 = ("binary quantization of " + Integer.valueOf(_dimension_1));
                  String _plus_4 = (_plus_3 + " dimensions; bbq indexes need at least 64 (DenseVectorFieldMapper.BBQ_MIN_DIMS)");
                  throw Unsupported.of(_plus_4);
                }
                _xblockexpression_2 = "bbq_";
              }
              _switchResult_1 = _xblockexpression_2;
              break;
            default:
              throw Unsupported.of("product quantization; Elasticsearch quantizes with int8, int4 and bbq");
          }
        } else {
          throw Unsupported.of("product quantization; Elasticsearch quantizes with int8, int4 and bbq");
        }
        prefix = _switchResult_1;
        if ((q.isRescore() || ModelUtil.written(q, "oversampling"))) {
          boolean _equals = Objects.equals(prefix, "");
          if (_equals) {
            throw Unsupported.of("rescoring without quantization; rescore_vector applies to quantized index types");
          }
          double _xifexpression = (double) 0;
          boolean _written_1 = ModelUtil.written(q, "oversampling");
          if (_written_1) {
            _xifexpression = q.getOversampling();
          } else {
            _xifexpression = 3.0;
          }
          rescore = ElasticsearchBodies.obj("oversample", ElasticsearchBodies.num(_xifexpression));
        }
      }
      Code.Text _text = ElasticsearchBodies.text((prefix + family));
      Code.Num _xifexpression_1 = null;
      boolean _written_2 = ModelUtil.written(index, "maxDegree");
      if (_written_2) {
        _xifexpression_1 = ElasticsearchBodies.num(index.getMaxDegree());
      } else {
        _xifexpression_1 = null;
      }
      Code.Num _xifexpression_2 = null;
      boolean _written_3 = ModelUtil.written(index, "buildCandidates");
      if (_written_3) {
        _xifexpression_2 = ElasticsearchBodies.num(index.getBuildCandidates());
      } else {
        _xifexpression_2 = null;
      }
      _xblockexpression = ElasticsearchBodies.obj("type", _text, "m", _xifexpression_1, 
        "ef_construction", _xifexpression_2, "rescore_vector", rescore);
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
            throw Unsupported.of(("analyzer resources; " + ElasticsearchStatements.NO_FILES));
          }
          final String tokenizer = ElasticsearchSchema.TOKENIZERS.get(analyzer.getTokenizer().getLiteral());
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
            _xifexpression = ElasticsearchSchema.LANGUAGES.get(analyzer.getLanguage().toLowerCase());
          }
          final String language = _xifexpression;
          if (((analyzer.getLanguage() != null) && (language == null))) {
            String _language_1 = analyzer.getLanguage();
            String _plus_2 = ("the language " + _language_1);
            String _plus_3 = (_plus_2 + "; ");
            String _plus_4 = (_plus_3 + ElasticsearchStatements.LANGUAGES_REFERENCE);
            throw Unsupported.of(_plus_4);
          }
          final String name = ElasticsearchBodies.aliasName(analyzer.getName());
          final List<Code.Expr> chain = CollectionLiterals.<Code.Expr>newArrayList();
          Code.Text _text = ElasticsearchBodies.text("lowercase");
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
                      filterSettings.put(filterName, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("stop"), "stopwords", ElasticsearchBodies.text((("_" + language) + "_"))));
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
                      filterSettings.put(filterName, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("stemmer"), "language", ElasticsearchBodies.text(language)));
                      _xblockexpression_2 = filterName;
                    }
                    _switchResult = _xblockexpression_2;
                    break;
                  case "ascii_folding":
                    _switchResult = "asciifolding";
                    break;
                  case "length":
                    throw Unsupported.of("the length filter; vecDSL gives no bounds, and without them the Elasticsearch length filter (min 0, max Integer.MAX_VALUE) keeps every token");
                  case "synonym":
                    throw Unsupported.of(("the synonym filter; vecDSL declares synonyms in file resources, " + ElasticsearchStatements.NO_FILES));
                  case "decompounder":
                    throw Unsupported.of(("the decompounder filter; dictionary_decompounder needs a word list, " + ElasticsearchStatements.NO_FILES));
                  default:
                    throw Unsupported.of("the phonetic filter; it is in the analysis-phonetic plugin, which the image does not include");
                }
              } else {
                throw Unsupported.of("the phonetic filter; it is in the analysis-phonetic plugin, which the image does not include");
              }
              final String step = _switchResult;
              if ((step != null)) {
                Code.Text _text_1 = ElasticsearchBodies.text(step);
                chain.add(_text_1);
              }
            }
          }
          if (((language != null) && analyzer.getFilters().isEmpty())) {
            filterSettings.put((name + "_stop"), ElasticsearchBodies.obj("type", ElasticsearchBodies.text("stop"), "stopwords", ElasticsearchBodies.text((("_" + language) + "_"))));
            filterSettings.put((name + "_stemmer"), ElasticsearchBodies.obj("type", ElasticsearchBodies.text("stemmer"), "language", ElasticsearchBodies.text(language)));
            Code.Text _text_1 = ElasticsearchBodies.text((name + "_stop"));
            chain.add(_text_1);
            Code.Text _text_2 = ElasticsearchBodies.text((name + "_stemmer"));
            chain.add(_text_2);
          }
          if ((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "none")))) {
            if (((!analyzer.getStopwords().startsWith("_")) || (!analyzer.getStopwords().endsWith("_")))) {
              String _stopwords = analyzer.getStopwords();
              String _plus_5 = ("stopwords " + _stopwords);
              String _plus_6 = (_plus_5 + "; Elasticsearch names built-in stop-word lists _english_, _german_ and so on");
              throw Unsupported.of(_plus_6);
            }
            filterSettings.put((name + "_stopwords"), ElasticsearchBodies.obj("type", ElasticsearchBodies.text("stop"), "stopwords", ElasticsearchBodies.text(analyzer.getStopwords())));
            Code.Text _text_3 = ElasticsearchBodies.text((name + "_stopwords"));
            chain.add(_text_3);
          }
          analyzerSettings.put(name, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("custom"), "tokenizer", ElasticsearchBodies.text(tokenizer), "filter", ElasticsearchBodies.arr(chain)));
          Set<String> _keySet = ModelUtil.extParams(analyzer, ElasticsearchGenerator.VENDOR).keySet();
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
      _xblockexpression = ElasticsearchBodies.obj("analyzer", _xifexpression, "filter", _xifexpression_1);
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
        return Boolean.valueOf(ElasticsearchSchema.bm25(a));
      };
      Iterable<Analyzer> _filter = IterableExtensions.<Analyzer>filter(analyzers, _function);
      for (final Analyzer analyzer : _filter) {
        String _aliasName = ElasticsearchBodies.aliasName(analyzer.getName());
        String _plus = (_aliasName + "_bm25");
        Code.Text _text = ElasticsearchBodies.text("BM25");
        Code.Num _xifexpression = null;
        boolean _written = ModelUtil.written(analyzer, "bm25K1");
        if (_written) {
          _xifexpression = ElasticsearchBodies.num(analyzer.getBm25K1());
        } else {
          _xifexpression = null;
        }
        Code.Num _xifexpression_1 = null;
        boolean _written_1 = ModelUtil.written(analyzer, "bm25B");
        if (_written_1) {
          _xifexpression_1 = ElasticsearchBodies.num(analyzer.getBm25B());
        } else {
          _xifexpression_1 = null;
        }
        result.put(_plus, ElasticsearchBodies.obj("type", _text, "k1", _xifexpression, 
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
        throw Unsupported.of(ElasticsearchStatements.EXTERNAL);
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(ElasticsearchStatements.NO_TTL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of((("auto-id; " + ElasticsearchStatements.PENDING) + " (documents indexed without _id get generated ids)"));
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(ElasticsearchStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; Elasticsearch has no references between indices (joins only within one index, through the join field)");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + ElasticsearchStatements.NO_MODELS));
      }
      boolean _isEmpty_1 = c.getFunctions().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = ElasticsearchStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      this.note(c, "idType", "generativeModel", "consistency");
      if (fresh) {
        this.evolve(c, null);
      }
      final ElasticsearchIndex current = this.index(c);
      final Code.Entries settings = ElasticsearchBodies.obj("number_of_shards", ElasticsearchBodies.num(1), "number_of_replicas", ElasticsearchBodies.num(0));
      ElasticsearchStatements.storage(c.getStorage(), settings);
      Cluster _elvis = null;
      Cluster _cluster = c.getCluster();
      if (_cluster != null) {
        _elvis = _cluster;
      } else {
        Cluster _containerOfType = EcoreUtil2.<Cluster>getContainerOfType(c, Cluster.class);
        _elvis = _containerOfType;
      }
      ElasticsearchStatements.clusterSettings(_elvis, settings);
      EList<CollectionLimit> _limits = c.getLimits();
      for (final CollectionLimit limit : _limits) {
        ElasticsearchStatements.collectionLimit(limit, settings);
      }
      Set<String> _keySet = ModelUtil.extParams(c, ElasticsearchGenerator.VENDOR).keySet();
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
        _xifexpression = ElasticsearchBodies.text(c.getDescription());
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_4 = c.getTags().isEmpty();
      if (_isEmpty_4) {
        _xifexpression_1 = null;
      } else {
        final Function1<String, Code.Expr> _function = (String t) -> {
          Code.Text _text = ElasticsearchBodies.text(t);
          return ((Code.Expr) _text);
        };
        _xifexpression_1 = ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(c.getTags(), _function)));
      }
      final Code.Entries meta = ElasticsearchBodies.obj("description", _xifexpression, 
        "tags", _xifexpression_1);
      String _xifexpression_2 = null;
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        _xifexpression_2 = "true";
      } else {
        _xifexpression_2 = "strict";
      }
      Code.Text _text = ElasticsearchBodies.text(_xifexpression_2);
      Code.Entries _xifexpression_3 = null;
      boolean _isEmpty_5 = meta.entries().isEmpty();
      if (_isEmpty_5) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = meta;
      }
      final Code.Entries mappings = ElasticsearchBodies.obj("dynamic", _text, "_meta", _xifexpression_3, "properties", this.properties(current));
      boolean _isEmpty_6 = c.getStoredSourceFields().isEmpty();
      boolean _not_4 = (!_isEmpty_6);
      if (_not_4) {
        final Function1<Field, Code.Expr> _function_1 = (Field s) -> {
          Code.Text _text_1 = ElasticsearchBodies.text(ModelUtil.fieldName(s));
          return ((Code.Expr) _text_1);
        };
        mappings.put("_source", ElasticsearchBodies.obj("includes", ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Field, Code.Expr>map(c.getStoredSourceFields(), _function_1)))));
      }
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex payloadIndex : _filter) {
        this.checkPayloadIndex(current, payloadIndex);
      }
      final Code.Entries aliases = Code.entries();
      EList<Alias> _aliases = c.getAliases();
      for (final Alias alias : _aliases) {
        aliases.put(ElasticsearchBodies.aliasName(alias.getName()), Code.entries());
      }
      EList<Tenant> _tenants = c.getTenants();
      for (final Tenant tenant : _tenants) {
        aliases.put(ElasticsearchBodies.sliceAlias(current.name(), "tenant", tenant.getName()), ElasticsearchBodies.obj("filter", ElasticsearchSchema.term(current.tenant(), ElasticsearchBodies.text(tenant.getName()))));
      }
      EList<Namespace> _namespaces = c.getNamespaces();
      for (final Namespace namespace : _namespaces) {
        aliases.put(ElasticsearchBodies.sliceAlias(current.name(), "namespace", namespace.getKey()), ElasticsearchBodies.obj("filter", ElasticsearchSchema.term(current.namespace(), ElasticsearchBodies.text(namespace.getKey()))));
      }
      List<Partition> _partitions = ModelUtil.partitions(c);
      for (final Partition partition : _partitions) {
        {
          ElasticsearchStatements.checkPartition(partition);
          aliases.put(ElasticsearchBodies.sliceAlias(current.name(), "partition", partition.getName()), ElasticsearchBodies.obj("filter", ElasticsearchSchema.term(current.partition(), ElasticsearchBodies.text(partition.getName()))));
        }
      }
      Code.Entries _xifexpression_4 = null;
      boolean _isEmpty_7 = aliases.entries().isEmpty();
      if (_isEmpty_7) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = aliases;
      }
      final Code.Entries body = ElasticsearchBodies.obj("settings", settings, "mappings", mappings, "aliases", _xifexpression_4);
      String _name = current.name();
      String _plus_2 = ("/" + _name);
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_2, null, body);
      final Code.Run creation = new Code.Run(_request);
      ArrayList<Code.Stmt> _xifexpression_5 = null;
      if (ifNotExists) {
        String _name_1 = current.name();
        String _plus_3 = ("/" + _name_1);
        Code.Call _call = ElasticsearchBodies.call("exists", "path", ElasticsearchBodies.text(_plus_3));
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
  public Code.Entries properties(final ElasticsearchIndex current) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<ElasticsearchIndex.Field> _fields = current.fields();
      for (final ElasticsearchIndex.Field f : _fields) {
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
          result.put(hidden, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("keyword")));
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
  public void checkPayloadIndex(final ElasticsearchIndex current, final PayloadIndex payloadIndex) {
    this.note(payloadIndex, "onDisk", "minTokenLength", "maxTokenLength");
    Set<String> _keySet = ModelUtil.extParams(payloadIndex, ElasticsearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    if ((ModelUtil.written(payloadIndex, "structure") && Objects.equals(payloadIndex.getStructure().getLiteral(), "bitmap"))) {
      throw Unsupported.of("bitmap payload indexes; Elasticsearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type");
    }
    boolean _isLowercase = payloadIndex.isLowercase();
    if (_isLowercase) {
      throw Unsupported.of((("lowercase payload indexes; " + ElasticsearchStatements.PENDING) + " (a keyword field with a lowercase normalizer)"));
    }
    boolean _isPrefixMatch = payloadIndex.isPrefixMatch();
    if (_isPrefixMatch) {
      throw Unsupported.of((("prefix-match payload indexes; " + ElasticsearchStatements.PENDING) + " (index_prefixes on a text field)"));
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
      throw Unsupported.of("a payload index on a vector field; dense_vector fields are indexed through their index_options");
    }
    final Field leaf = ModelUtil.leaf(path);
    final ElasticsearchIndex.Field target = current.field(ModelUtil.fieldName(path.getField()));
    final String kind = payloadIndex.getKind().getLiteral();
    if ((Objects.equals(kind, "full_text") && (!(target.fullText() || ((leaf instanceof PayloadField) && ((PayloadField) leaf).isFullText()))))) {
      String _name = target.name();
      String _plus = ("a full_text index on " + _name);
      String _plus_1 = (_plus + ", which is not a full-text field (Elasticsearch fixes the type of a field when it is mapped)");
      throw Unsupported.of(_plus_1);
    }
    if ((((Objects.equals(kind, "geo") && (leaf instanceof PayloadField)) && (!Objects.equals(((PayloadField) leaf).getType(), EScalarDType.GEO_POINT))) && (!Objects.equals(((PayloadField) leaf).getType(), EScalarDType.GEOMETRY)))) {
      throw Unsupported.of("a geo index on a field that is not geo_point or geometry");
    }
  }

  public static Code.Entries term(final String fieldName, final Code.Expr value) {
    return ElasticsearchBodies.obj("term", ElasticsearchBodies.obj(fieldName, value));
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
