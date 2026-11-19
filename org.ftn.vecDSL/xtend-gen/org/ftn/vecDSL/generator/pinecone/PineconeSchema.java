package org.ftn.vecDSL.generator.pinecone;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETokenizer;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to Pinecone document indexes (API 2026-07). The index schema declares only searched
 * fields: one dense vector, one sparse vector and full-text strings. Every other field is metadata, which
 * Pinecone indexes for filtering on first write. Document ids are strings, so the runtime derives the id from the
 * primary key and also stores the key as metadata under its own name, which keeps it typed and filterable.
 */
@SuppressWarnings("all")
public class PineconeSchema {
  public static final String NO_PARTITIONS = "partitions; Pinecone isolates data by namespace only (vecDSL tenants and namespaces are Pinecone namespaces)";

  private static final String TAG = "[A-Za-z0-9_-]{1,80}";

  /**
   * Documents per upsert or update on an index that embeds a field (SPEC-D UpsertDocumentsRequest.documents).
   */
  private static final int EMBEDDED_BATCH = 96;

  private static final int MAX_TAGS = 20;

  private static final List<EScalarDType> INTEGRAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8));

  private static final List<EScalarDType> NUMERIC = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> TEXTUAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<String, String> LANGUAGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("english", "en"), Pair.<String, String>of("german", "de"), Pair.<String, String>of("french", "fr"), Pair.<String, String>of("spanish", "es"), Pair.<String, String>of("italian", "it"), Pair.<String, String>of("portuguese", "pt"), Pair.<String, String>of("dutch", "nl"), Pair.<String, String>of("russian", "ru"), Pair.<String, String>of("japanese", "ja"), Pair.<String, String>of("korean", "ko"), Pair.<String, String>of("chinese", "zh")));

  private final List<String> notes;

  public PineconeSchema(final List<String> notes) {
    this.notes = notes;
  }

  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final Code.Entries deployment) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("collections derived from external sources");
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of("collection ttl");
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; Pinecone document ids are derived from the primary key");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref");
      }
      boolean _isEmpty_1 = c.getAliases().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of("aliases (Pinecone aliases name namespaces, not indexes)");
      }
      boolean _isEmpty_2 = c.getPartitions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
      }
      boolean _written = ModelUtil.written(c, "consistency");
      if (_written) {
        this.consistency(c.getConsistency());
      }
      this.note(c, "dynamic", "idType", "description", "storage", "storedSourceFields", "generativeModel", "limits");
      final Code.Entries fields = this.indexFields(c);
      boolean _isEmpty_3 = fields.entries().isEmpty();
      if (_isEmpty_3) {
        throw Unsupported.of("a collection without vector or full-text fields; a Pinecone index declares at least one searched field");
      }
      final Code.Entries args = PineconeSchema.kw("name", Code.text(PineconeSchema.indexName(c)), "fields", fields);
      boolean _isEmpty_4 = deployment.entries().isEmpty();
      boolean _not_3 = (!_isEmpty_4);
      if (_not_3) {
        args.put("deployment", deployment);
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        args.put("deletion_protection", Code.text("enabled"));
      }
      boolean _isEmpty_5 = c.getTags().isEmpty();
      boolean _not_4 = (!_isEmpty_5);
      if (_not_4) {
        args.put("tags", this.tags(c.getTags()));
      }
      Code.Call _runtime = Code.runtime("create_collection", args);
      Code.Run _run = new Code.Run(_runtime);
      final List<Code.Stmt> created = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run));
      final List<String> slices = CollectionLiterals.<String>newArrayList();
      final Function1<Namespace, String> _function = (Namespace namespace) -> {
        return PineconeSchema.namespaceName(namespace.getKey());
      };
      List<String> _map = ListExtensions.<Namespace, String>map(c.getNamespaces(), _function);
      Iterables.<String>addAll(slices, _map);
      final Function1<Tenant, String> _function_1 = (Tenant tenant) -> {
        return PineconeSchema.namespaceName(tenant.getName());
      };
      List<String> _map_1 = ListExtensions.<Tenant, String>map(c.getTenants(), _function_1);
      Iterables.<String>addAll(slices, _map_1);
      List<String> _list = slices.stream().distinct().toList();
      for (final String slice : _list) {
        Code.Call _runtime_1 = Code.runtime("create_namespace", PineconeSchema.kw("name", Code.text(PineconeSchema.indexName(c)), "namespace", Code.text(slice)));
        Code.Run _run_1 = new Code.Run(_runtime_1);
        created.add(_run_1);
      }
      if ((!ifNotExists)) {
        return created;
      }
      Code.Call _runtime_2 = Code.runtime("has_collection", PineconeSchema.kw("name", Code.text(PineconeSchema.indexName(c))));
      Code.When _when = new Code.When(_runtime_2, true, created);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Index tags: Pinecone tags are key-value pairs, so each vecDSL tag becomes a key with the value "true".
   */
  public Code.Entries tags(final List<String> labels) {
    Code.Entries _xblockexpression = null;
    {
      int _size = labels.size();
      boolean _greaterThan = (_size > PineconeSchema.MAX_TAGS);
      if (_greaterThan) {
        throw Unsupported.of(((("more than " + Integer.valueOf(PineconeSchema.MAX_TAGS)) + " tags; a Pinecone index has at most ") + Integer.valueOf(PineconeSchema.MAX_TAGS)));
      }
      final Code.Entries result = Code.entries();
      for (final String label : labels) {
        {
          boolean _matches = label.matches(PineconeSchema.TAG);
          boolean _not = (!_matches);
          if (_not) {
            throw Unsupported.of((("tag \'" + label) + "\'; Pinecone tag keys are 1 to 80 letters, digits, \'_\' or \'-\'"));
          }
          result.put(label, Code.text("true"));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A Pinecone namespace name: 1 to 512 ASCII characters other than NUL.
   */
  public static String namespaceName(final String name) {
    String _xblockexpression = null;
    {
      if (((((name == null) || name.isEmpty()) || (name.length() > 512)) || name.chars().anyMatch(((IntPredicate) (int c) -> {
        return ((c == 0) || (c > 127));
      })))) {
        throw Unsupported.of((("namespace \'" + name) + "\'; Pinecone namespace names are 1 to 512 ASCII characters"));
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  /**
   * Pinecone index and backup names: the lower-case words of a vecDSL name joined by '-', at most 45 characters.
   */
  public static String resourceName(final String name, final String what) {
    String _xblockexpression = null;
    {
      final String result = String.join("-", Syntax.words(name));
      if (((result.isEmpty() || (result.length() > 45)) || (!result.matches("[a-z0-9]([a-z0-9-]*[a-z0-9])?")))) {
        throw Unsupported.of((((what + " name \'") + name) + "\'; Pinecone names are 1 to 45 lower-case letters, digits and \'-\'"));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A metadata key: names starting with '_' or '$' are reserved.
   */
  public static String checkKey(final String name) {
    String _xblockexpression = null;
    {
      if ((name.startsWith("_") || name.startsWith("$"))) {
        throw Unsupported.of((("field name \'" + name) + "\'; Pinecone reserves names that start with \'_\' or \'$\'"));
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  /**
   * A payload field added to an existing index: a metadata value of one of the Pinecone value types.
   */
  public void checkMetadataField(final PayloadField field) {
    this.payloadType(field, false);
  }

  /**
   * Collection identity every runtime call needs: index name, namespace and the primary key with its id form.
   */
  public Code.Entries layout(final VectorCollection c, final String namespace) {
    return PineconeSchema.kw("name", Code.text(PineconeSchema.indexName(c)), "namespace", Code.text(namespace), "key", Code.text(ModelUtil.fieldName(c.getPrimaryKey())), 
      "key_type", Code.text(PineconeSchema.keyType(c)));
  }

  /**
   * The layout plus what writes need: the search clause that observes freshness, limits and default values.
   */
  public Code.Entries writeLayout(final VectorCollection c, final String namespace) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = this.layout(c, namespace);
      args.put("probe", this.probe(c));
      final Code.Entries declared = this.limits(c);
      boolean _isEmpty = declared.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("limits", declared);
      }
      final Code.Entries defaults = Code.entries();
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        Literal _defaultValue = it.getDefaultValue();
        return Boolean.valueOf((_defaultValue != null));
      };
      Iterable<PayloadField> _filter = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function);
      for (final PayloadField field : _filter) {
        defaults.put(ModelUtil.fieldName(field), Literals.of(field.getDefaultValue()));
      }
      boolean _isEmpty_1 = defaults.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("defaults", defaults);
      }
      boolean _embeds = this.embeds(c);
      if (_embeds) {
        args.put("max_batch", Code.num(PineconeSchema.EMBEDDED_BATCH));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * Pinecone index name: lower-case words of the collection name joined by '-'.
   */
  public static String indexName(final VectorCollection c) {
    return PineconeSchema.resourceName(c.getName(), "collection");
  }

  public static String keyType(final VectorCollection c) {
    final Field key = c.getPrimaryKey();
    if ((!(key instanceof PayloadField))) {
      throw Unsupported.of("a vector field as primary key");
    }
    final EScalarDType type = ((PayloadField) key).getType();
    boolean _contains = PineconeSchema.INTEGRAL.contains(type);
    if (_contains) {
      return "int";
    }
    boolean _contains_1 = PineconeSchema.TEXTUAL.contains(type);
    if (_contains_1) {
      return "text";
    }
    String _literal = type.getLiteral();
    String _plus = ("primary key of type " + _literal);
    throw Unsupported.of(_plus);
  }

  /**
   * Field name as a Pinecone document key; names starting with '_' or '$' are reserved.
   */
  public static String fieldKey(final Field field) {
    return PineconeSchema.checkKey(ModelUtil.fieldName(field));
  }

  /**
   * The dense vector field, or null for a collection searched only by text.
   */
  public VectorField denseVector(final VectorCollection c) {
    VectorField _xblockexpression = null;
    {
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        EVectorDType _type = it.getType();
        return Boolean.valueOf((!Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR)));
      };
      final List<VectorField> dense = IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function));
      int _size = dense.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        throw Unsupported.of("several dense vector fields; a Pinecone index has one dense vector field");
      }
      _xblockexpression = IterableExtensions.<VectorField>head(dense);
    }
    return _xblockexpression;
  }

  /**
   * The sparse vector field stored as such: one that no bm25 function produces.
   */
  public VectorField sparseVector(final VectorCollection c) {
    VectorField _xblockexpression = null;
    {
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        return Boolean.valueOf((Objects.equals(it.getType(), EVectorDType.SPARSE_FLOAT_VECTOR) && (!PineconeSchema.isBm25Output(c, it))));
      };
      final List<VectorField> sparse = IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function));
      int _size = sparse.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        throw Unsupported.of("several sparse vector fields; a Pinecone index has one sparse vector field");
      }
      _xblockexpression = IterableExtensions.<VectorField>head(sparse);
    }
    return _xblockexpression;
  }

  public static boolean isBm25Output(final VectorCollection c, final Field field) {
    boolean _xblockexpression = false;
    {
      final Function function = ModelUtil.producingFunction(c, field);
      _xblockexpression = ((function != null) && Objects.equals(function.getType(), EFunctionType.BM25));
    }
    return _xblockexpression;
  }

  /**
   * Payload fields declared for full-text search: the schema's string fields with full_text_search, and the text fields of
   * embedded vector fields, which Pinecone stores and indexes for full-text search (SPEC-C StringField.store_text).
   */
  public List<PayloadField> fullTextFields(final VectorCollection c) {
    List<PayloadField> _xblockexpression = null;
    {
      final List<Field> inputs = this.bm25Inputs(c);
      final Function1<VectorField, Boolean> _function = (VectorField vector) -> {
        return Boolean.valueOf(((!PineconeSchema.isBm25Output(c, vector)) && (PineconeSchema.embeddingModel(c, vector) != null)));
      };
      final Function1<VectorField, Field> _function_1 = (VectorField vector) -> {
        return PineconeSchema.embeddedText(c, vector);
      };
      final List<Field> embedded = IterableExtensions.<Field>toList(IterableExtensions.<VectorField, Field>map(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function), _function_1));
      final Function1<PayloadField, Boolean> _function_2 = (PayloadField it) -> {
        return Boolean.valueOf(((((it.isFullText() || (it.getAnalyzer() != null)) || inputs.contains(it)) || embedded.contains(it)) || IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex index) -> {
          return Boolean.valueOf(((index.getTarget().getField() == it) && Objects.equals(index.getKind(), EPayloadIndexKind.FULL_TEXT)));
        }))));
      };
      _xblockexpression = IterableExtensions.<PayloadField>toList(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_2));
    }
    return _xblockexpression;
  }

  public boolean isFullText(final VectorCollection c, final Field field) {
    return this.fullTextFields(c).contains(field);
  }

  private Code.Entries indexFields(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      final VectorField dense = this.denseVector(c);
      if ((dense != null)) {
        EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(c, dense);
        boolean _tripleEquals = (_embeddingModel == null);
        if (_tripleEquals) {
          result.put(PineconeSchema.fieldKey(dense), this.denseField(c, dense));
        } else {
          this.embed(c, dense, result);
        }
      }
      final VectorField sparse = this.sparseVector(c);
      if ((sparse != null)) {
        this.note(sparse, "normalized", "onDisk", "nullable");
        EmbeddingModel _embeddingModel_1 = PineconeSchema.embeddingModel(c, sparse);
        boolean _tripleEquals_1 = (_embeddingModel_1 == null);
        if (_tripleEquals_1) {
          result.put(PineconeSchema.fieldKey(sparse), this.describe(PineconeSchema.kw("type", Code.text("sparse_vector")), sparse));
        } else {
          this.embed(c, sparse, result);
        }
      }
      final List<PayloadField> searched = this.fullTextFields(c);
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          this.payloadField(c, field, searched.contains(field));
          boolean _contains = searched.contains(field);
          if (_contains) {
            PineconeSchema.merge(result, PineconeSchema.fieldKey(field), this.stringField(c, field));
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Entries denseField(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      this.denseType(field);
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim; a Pinecone dense vector field declares its dimension"));
      }
      this.note(field, "normalized", "onDisk", "nullable");
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      if ((index != null)) {
        this.vectorIndex(index);
      }
      _xblockexpression = this.describe(PineconeSchema.kw("type", Code.text("dense_vector"), "dimension", Code.num(field.getDimension()), "metric", Code.text(this.metricName(this.metric(field, index)))), field);
    }
    return _xblockexpression;
  }

  private void denseType(final VectorField field) {
    boolean _isMultiVector = field.isMultiVector();
    if (_isMultiVector) {
      throw Unsupported.of("multi-vector fields");
    }
    EVectorDType _type = field.getType();
    boolean _notEquals = (!Objects.equals(_type, EVectorDType.FLOAT_VECTOR));
    if (_notEquals) {
      String _literal = field.getType().getLiteral();
      String _plus = (_literal + " fields; Pinecone stores float vectors");
      throw Unsupported.of(_plus);
    }
  }

  /**
   * The hosted model that embeds a vector field: the field's own model, or the model of the text_embedding
   * (dense) or sparse_encoding (sparse) function that produces it; null for a field the program writes as vectors.
   */
  public static EmbeddingModel embeddingModel(final VectorCollection c, final VectorField field) {
    EmbeddingModel _xblockexpression = null;
    {
      EmbeddingModel _model = field.getModel();
      boolean _tripleNotEquals = (_model != null);
      if (_tripleNotEquals) {
        return field.getModel();
      }
      final Function function = ModelUtil.producingFunction(c, field);
      if (((function == null) || (!Collections.<EFunctionType>unmodifiableList(CollectionLiterals.<EFunctionType>newArrayList(EFunctionType.TEXT_EMBEDDING, EFunctionType.SPARSE_ENCODING)).contains(function.getType())))) {
        return null;
      }
      EFunctionType _type = function.getType();
      boolean _equals = Objects.equals(_type, EFunctionType.SPARSE_ENCODING);
      EVectorDType _type_1 = field.getType();
      boolean _equals_1 = Objects.equals(_type_1, EVectorDType.SPARSE_FLOAT_VECTOR);
      boolean _notEquals = (_equals != _equals_1);
      if (_notEquals) {
        String _literal = function.getType().getLiteral();
        String _plus = ("a " + _literal);
        String _plus_1 = (_plus + " function producing ");
        String _literal_1 = field.getType().getLiteral();
        String _plus_2 = (_plus_1 + _literal_1);
        String _plus_3 = (_plus_2 + " field ");
        String _fieldName = ModelUtil.fieldName(field);
        String _plus_4 = (_plus_3 + _fieldName);
        String _plus_5 = (_plus_4 + "; Pinecone embeds dense vectors with embed and sparse vectors with sparse_embed");
        throw Unsupported.of(_plus_5);
      }
      EmbeddingModel _model_1 = function.getModel();
      boolean _tripleEquals = (_model_1 == null);
      if (_tripleEquals) {
        String _literal_2 = function.getType().getLiteral();
        String _plus_6 = ("a " + _literal_2);
        String _plus_7 = (_plus_6 + " function without a model; Pinecone integrated embedding runs a hosted model");
        throw Unsupported.of(_plus_7);
      }
      _xblockexpression = function.getModel();
    }
    return _xblockexpression;
  }

  /**
   * The field whose text Pinecone embeds into a vector field: its one source field, or the vector field itself.
   */
  public static Field embeddedText(final VectorCollection c, final VectorField field) {
    Field _xblockexpression = null;
    {
      EList<Field> _xifexpression = null;
      EmbeddingModel _model = field.getModel();
      boolean _tripleNotEquals = (_model != null);
      if (_tripleNotEquals) {
        _xifexpression = field.getSourceFields();
      } else {
        _xifexpression = ModelUtil.producingFunction(c, field).getInputs();
      }
      final List<Field> sources = _xifexpression;
      int _size = sources.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        int _size_1 = sources.size();
        String _plus = ("embedding " + Integer.valueOf(_size_1));
        String _plus_1 = (_plus + " source fields into ");
        String _fieldName = ModelUtil.fieldName(field);
        String _plus_2 = (_plus_1 + _fieldName);
        String _plus_3 = (_plus_2 + "; Pinecone embeds the text of one string field");
        throw Unsupported.of(_plus_3);
      }
      Field _elvis = null;
      Field _head = IterableExtensions.<Field>head(sources);
      if (_head != null) {
        _elvis = _head;
      } else {
        _elvis = field;
      }
      _xblockexpression = _elvis;
    }
    return _xblockexpression;
  }

  /**
   * The name of a hosted model, checked against the provider: Pinecone integrated inference runs the models it hosts.
   */
  public static String hostedName(final String provider, final String model) {
    String _xblockexpression = null;
    {
      if (((provider != null) && (!Objects.equals(provider.toLowerCase(), PineconeGenerator.VENDOR)))) {
        throw Unsupported.of((("models of provider " + provider) + "; Pinecone integrated inference runs the models it hosts"));
      }
      _xblockexpression = model;
    }
    return _xblockexpression;
  }

  /**
   * A string field with integrated embedding (SPEC-C CreateEmbedConfig, CreateSparseEmbedConfig): embed or sparse_embed.
   */
  private void embed(final VectorCollection c, final VectorField field, final Code.Entries fields) {
    final EmbeddingModel model = PineconeSchema.embeddingModel(c, field);
    final Field source = PineconeSchema.embeddedText(c, field);
    if (((source instanceof PayloadField) && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(((PayloadField) source).getType())))) {
      String _literal = ((PayloadField) source).getType().getLiteral();
      String _plus = ("embedding " + _literal);
      String _plus_1 = (_plus + " field ");
      String _fieldName = ModelUtil.fieldName(source);
      String _plus_2 = (_plus_1 + _fieldName);
      String _plus_3 = (_plus_2 + "; Pinecone embeds string fields");
      throw Unsupported.of(_plus_3);
    }
    if (((source instanceof VectorField) && (source != field))) {
      String _fieldName_1 = ModelUtil.fieldName(source);
      String _plus_4 = ("embedding vector field " + _fieldName_1);
      String _plus_5 = (_plus_4 + "; Pinecone embeds string fields");
      throw Unsupported.of(_plus_5);
    }
    String _localPath = model.getLocalPath();
    boolean _tripleNotEquals = (_localPath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("local model files; Pinecone runs the models it hosts");
    }
    String _provider = model.getProvider();
    String _elvis = null;
    String _modelId = model.getModelId();
    if (_modelId != null) {
      _elvis = _modelId;
    } else {
      String _name = model.getName();
      _elvis = _name;
    }
    final Code.Entries config = PineconeSchema.kw("model", Code.text(PineconeSchema.hostedName(_provider, _elvis)));
    EVectorDType _type = field.getType();
    boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
    if (_equals) {
      PineconeSchema.merge(fields, PineconeSchema.fieldKey(source), this.describe(PineconeSchema.kw("type", Code.text("string"), "sparse_embed", config), field));
      return;
    }
    this.denseType(field);
    if ((((field.getDimension() > 0) && ModelUtil.written(model, "dimension")) && (model.getDimension() != field.getDimension()))) {
      String _fieldName_2 = ModelUtil.fieldName(field);
      String _plus_6 = ("vector field " + _fieldName_2);
      String _plus_7 = (_plus_6 + " with dim ");
      int _dimension = field.getDimension();
      String _plus_8 = (_plus_7 + Integer.valueOf(_dimension));
      String _plus_9 = (_plus_8 + " and model ");
      String _name_1 = model.getName();
      String _plus_10 = (_plus_9 + _name_1);
      String _plus_11 = (_plus_10 + " with dim ");
      int _dimension_1 = model.getDimension();
      String _plus_12 = (_plus_11 + Integer.valueOf(_dimension_1));
      throw Unsupported.of(_plus_12);
    }
    int _dimension_2 = field.getDimension();
    boolean _greaterThan = (_dimension_2 > 0);
    if (_greaterThan) {
      config.put("dimension", Code.num(field.getDimension()));
    } else {
      boolean _written = ModelUtil.written(model, "dimension");
      if (_written) {
        config.put("dimension", Code.num(model.getDimension()));
      }
    }
    this.note(field, "normalized", "onDisk", "nullable");
    final VectorIndex index = ModelUtil.vectorIndex(c, field);
    if ((index != null)) {
      this.vectorIndex(index);
    }
    config.put("metric", Code.text(this.metricName(this.metric(field, index))));
    PineconeSchema.merge(fields, PineconeSchema.fieldKey(source), this.describe(PineconeSchema.kw("type", Code.text("string"), "embed", config), field));
  }

  /**
   * Adds a field configuration, combining it with one already declared under that name (a text field that is also embedded).
   */
  private static void merge(final Code.Entries fields, final String name, final Code.Entries config) {
    final Code.Expr present = fields.get(name);
    if ((present instanceof Code.Entries)) {
      Set<Map.Entry<String, Code.Expr>> _entrySet = config.entries().entrySet();
      for (final Map.Entry<String, Code.Expr> entry : _entrySet) {
        ((Code.Entries) present).put(entry.getKey(), entry.getValue());
      }
    } else {
      fields.put(name, config);
    }
  }

  /**
   * The key of the field a search scores: the embedded text field of a vector field with a model, else the vector field.
   */
  public static String searchKey(final VectorCollection c, final VectorField field) {
    String _xifexpression = null;
    EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(c, field);
    boolean _tripleEquals = (_embeddingModel == null);
    if (_tripleEquals) {
      _xifexpression = PineconeSchema.fieldKey(field);
    } else {
      _xifexpression = PineconeSchema.fieldKey(PineconeSchema.embeddedText(c, field));
    }
    return _xifexpression;
  }

  /**
   * The scoring of a vector field with a model: embed for dense fields, sparse_embed for sparse ones; null otherwise.
   */
  public static String embedScoring(final VectorCollection c, final VectorField field) {
    String _xifexpression = null;
    EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(c, field);
    boolean _tripleEquals = (_embeddingModel == null);
    if (_tripleEquals) {
      _xifexpression = null;
    } else {
      String _xifexpression_1 = null;
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
      if (_equals) {
        _xifexpression_1 = "sparse_embed";
      } else {
        _xifexpression_1 = "embed";
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  /**
   * True when any vector field of the collection is embedded by Pinecone, which limits write batches to 96 documents.
   */
  public boolean embeds(final VectorCollection c) {
    VectorField _denseVector = this.denseVector(c);
    VectorField _sparseVector = this.sparseVector(c);
    final Function1<VectorField, Boolean> _function = (VectorField field) -> {
      EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(c, field);
      return Boolean.valueOf((_embeddingModel != null));
    };
    return IterableExtensions.<VectorField>exists(IterableExtensions.<VectorField>filterNull(Collections.<VectorField>unmodifiableList(CollectionLiterals.<VectorField>newArrayList(_denseVector, _sparseVector))), _function);
  }

  private void vectorIndex(final VectorIndex index) {
    this.note(index, "family", "maxDegree", "buildCandidates", "searchCandidates", "centroids", "quantization", 
      "accelerator", "onDisk", "online");
  }

  /**
   * Checks a payload field against the value types of Pinecone metadata: numbers, strings, booleans, string lists.
   */
  private void payloadField(final VectorCollection c, final PayloadField field, final boolean searched) {
    this.payloadType(field, searched);
    final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
      Field _field = it.getTarget().getField();
      return Boolean.valueOf((_field == field));
    };
    Iterable<PayloadIndex> _filter = IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function);
    for (final PayloadIndex index : _filter) {
      this.payloadIndex(index, field, searched);
    }
    this.note(field, "nested", "documentBody");
  }

  private void payloadType(final PayloadField field, final boolean searched) {
    final String name = PineconeSchema.fieldKey(field);
    boolean _isPartitionKey = field.isPartitionKey();
    if (_isPartitionKey) {
      throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
    }
    boolean _isEmpty = field.getSubFields().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of((("sub-fields on " + name) + "; Pinecone metadata is flat"));
    }
    if ((searched && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(field.getType())))) {
      String _literal = field.getType().getLiteral();
      String _plus = ("full-text search on " + _literal);
      String _plus_1 = (_plus + " field ");
      String _plus_2 = (_plus_1 + name);
      throw Unsupported.of(_plus_2);
    }
    EScalarDType _type = field.getType();
    if (_type != null) {
      switch (_type) {
        case ARRAY:
          boolean _written = ModelUtil.written(field, "elementType");
          boolean _not_1 = (!_written);
          if (_not_1) {
            throw Unsupported.of((("array field " + name) + " without element-type"));
          }
          boolean _contains = PineconeSchema.TEXTUAL.contains(field.getElementType());
          boolean _not_2 = (!_contains);
          if (_not_2) {
            String _literal_1 = field.getElementType().getLiteral();
            String _plus_3 = ("arrays of " + _literal_1);
            String _plus_4 = (_plus_3 + "; Pinecone metadata lists hold strings");
            throw Unsupported.of(_plus_4);
          }
          break;
        case BOOL:
          break;
        default:
          if (((!PineconeSchema.NUMERIC.contains(field.getType())) && (!PineconeSchema.TEXTUAL.contains(field.getType())))) {
            String _literal_2 = field.getType().getLiteral();
            String _plus_5 = (_literal_2 + " field ");
            String _plus_6 = (_plus_5 + name);
            String _plus_7 = (_plus_6 + "; Pinecone metadata holds numbers, strings, booleans and string lists");
            throw Unsupported.of(_plus_7);
          }
          break;
      }
    } else {
      if (((!PineconeSchema.NUMERIC.contains(field.getType())) && (!PineconeSchema.TEXTUAL.contains(field.getType())))) {
        String _literal_2 = field.getType().getLiteral();
        String _plus_5 = (_literal_2 + " field ");
        String _plus_6 = (_plus_5 + name);
        String _plus_7 = (_plus_6 + "; Pinecone metadata holds numbers, strings, booleans and string lists");
        throw Unsupported.of(_plus_7);
      }
    }
  }

  /**
   * Metadata is indexed for filtering automatically, so only full-text indexes change the index schema.
   */
  public void payloadIndex(final PayloadIndex index, final PayloadField field, final boolean searched) {
    if (((!index.getTarget().getSubFields().isEmpty()) || (index.getTarget().getRawPath() != null))) {
      throw Unsupported.of("payload indexes on nested paths");
    }
    this.note(index, "structure", "castType", "onDisk", "prefixMatch", "minTokenLength", "maxTokenLength");
    EPayloadIndexKind _kind = index.getKind();
    if (_kind != null) {
      switch (_kind) {
        case FULL_TEXT:
          break;
        case KEYWORD:
        case INTEGER:
        case FLOAT:
        case BOOL:
        case UUID:
          if (searched) {
            String _literal = index.getKind().getLiteral();
            String _plus = ("a " + _literal);
            String _plus_1 = (_plus + " index on full-text field ");
            String _fieldName = ModelUtil.fieldName(field);
            String _plus_2 = (_plus_1 + _fieldName);
            String _plus_3 = (_plus_2 + "; Pinecone filters full-text fields by text match only");
            throw Unsupported.of(_plus_3);
          }
          break;
        default:
          String _literal_1 = index.getKind().getLiteral();
          String _plus_4 = (_literal_1 + " payload indexes");
          throw Unsupported.of(_plus_4);
      }
    } else {
      String _literal_1 = index.getKind().getLiteral();
      String _plus_4 = (_literal_1 + " payload indexes");
      throw Unsupported.of(_plus_4);
    }
  }

  private Code.Entries stringField(final VectorCollection c, final PayloadField field) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries search = Code.entries();
      Analyzer _elvis = null;
      Analyzer _analyzer = field.getAnalyzer();
      if (_analyzer != null) {
        _elvis = _analyzer;
      } else {
        final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
          Field _field = it.getTarget().getField();
          return Boolean.valueOf((_field == field));
        };
        final Function1<PayloadIndex, Analyzer> _function_1 = (PayloadIndex it) -> {
          return it.getAnalyzer();
        };
        Analyzer _head = IterableExtensions.<Analyzer>head(IterableExtensions.<Analyzer>filterNull(IterableExtensions.<PayloadIndex, Analyzer>map(IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function), _function_1)));
        _elvis = _head;
      }
      final Analyzer analyzer = _elvis;
      if ((analyzer != null)) {
        this.analyze(analyzer, search);
      }
      _xblockexpression = this.describe(PineconeSchema.kw("type", Code.text("string"), "full_text_search", search), field);
    }
    return _xblockexpression;
  }

  private void analyze(final Analyzer analyzer, final Code.Entries search) {
    boolean _isEmpty = analyzer.getResources().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("analyzer resources");
    }
    if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
      throw Unsupported.of("bm25-k1 and bm25-b; Pinecone sets the BM25 parameters");
    }
    ETokenizer _tokenizer = analyzer.getTokenizer();
    if (_tokenizer != null) {
      switch (_tokenizer) {
        case STANDARD:
        case LANGUAGE:
          break;
        default:
          String _literal = analyzer.getTokenizer().getLiteral();
          String _plus = (_literal + " tokenizer; Pinecone full-text search tokenizes words");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = analyzer.getTokenizer().getLiteral();
      String _plus = (_literal + " tokenizer; Pinecone full-text search tokenizes words");
      throw Unsupported.of(_plus);
    }
    final Function1<AnalyzerFilter, EAnalyzerFilterType> _function = (AnalyzerFilter it) -> {
      return it.getType();
    };
    final Set<EAnalyzerFilterType> filters = IterableExtensions.<EAnalyzerFilterType>toSet(ListExtensions.<AnalyzerFilter, EAnalyzerFilterType>map(analyzer.getFilters(), _function));
    for (final EAnalyzerFilterType filter : filters) {
      boolean _contains = Collections.<EAnalyzerFilterType>unmodifiableList(CollectionLiterals.<EAnalyzerFilterType>newArrayList(EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STEMMER, EAnalyzerFilterType.STOP)).contains(filter);
      boolean _not_1 = (!_contains);
      if (_not_1) {
        String _literal_1 = filter.getLiteral();
        String _plus_1 = (_literal_1 + " token filters");
        throw Unsupported.of(_plus_1);
      }
    }
    boolean _contains_1 = filters.contains(EAnalyzerFilterType.LOWERCASE);
    if (_contains_1) {
      String _name = analyzer.getName();
      String _plus_2 = ("lowercase filter on analyzer " + _name);
      this.notes.add(_plus_2);
    }
    final boolean stop = (filters.contains(EAnalyzerFilterType.STOP) || ((analyzer.getStopwords() != null) && (!Objects.equals(analyzer.getStopwords().toLowerCase(), "none"))));
    if ((stop && (!filters.contains(EAnalyzerFilterType.STEMMER)))) {
      throw Unsupported.of("stop words without a stemmer filter; Pinecone removes stop words only when it stems");
    }
    String _language = analyzer.getLanguage();
    boolean _tripleNotEquals = (_language != null);
    if (_tripleNotEquals) {
      search.put("language", Code.text(this.language(analyzer.getLanguage())));
    }
    boolean _contains_2 = filters.contains(EAnalyzerFilterType.STEMMER);
    if (_contains_2) {
      search.put("stemming", Code.flag(true));
    }
    if (stop) {
      if ((((analyzer.getStopwords() != null) && (analyzer.getLanguage() != null)) && (!Objects.equals(this.language(analyzer.getStopwords()), this.language(analyzer.getLanguage()))))) {
        String _stopwords = analyzer.getStopwords();
        String _plus_3 = ("stopwords \"" + _stopwords);
        String _plus_4 = (_plus_3 + "\" for language \"");
        String _language_1 = analyzer.getLanguage();
        String _plus_5 = (_plus_4 + _language_1);
        String _plus_6 = (_plus_5 + "\"");
        throw Unsupported.of(_plus_6);
      }
      search.put("stop_words", Code.flag(true));
    }
  }

  private String language(final String name) {
    String _xblockexpression = null;
    {
      final String lower = name.toLowerCase();
      _xblockexpression = PineconeSchema.LANGUAGES.getOrDefault(lower, lower);
    }
    return _xblockexpression;
  }

  private Code.Entries describe(final Code.Entries config, final Field field) {
    Code.Entries _xblockexpression = null;
    {
      String _description = field.getDescription();
      boolean _tripleNotEquals = (_description != null);
      if (_tripleNotEquals) {
        config.put("description", Code.text(field.getDescription()));
      }
      _xblockexpression = config;
    }
    return _xblockexpression;
  }

  /**
   * Text fields that feed a bm25 function; Pinecone scores them with BM25 through full-text search.
   */
  public List<Field> bm25Inputs(final VectorCollection c) {
    List<Field> _xblockexpression = null;
    {
      final List<Field> inputs = CollectionLiterals.<Field>newArrayList();
      EList<Function> _functions = c.getFunctions();
      for (final Function function : _functions) {
        EFunctionType _type = function.getType();
        if (_type != null) {
          switch (_type) {
            case BM25:
              EList<Field> _inputs = function.getInputs();
              for (final Field input : _inputs) {
                {
                  if (((!(input instanceof PayloadField)) || (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(((PayloadField) input).getType())))) {
                    String _fieldName = ModelUtil.fieldName(input);
                    String _plus = ("bm25 input " + _fieldName);
                    String _plus_1 = (_plus + ", which is not a text field");
                    throw Unsupported.of(_plus_1);
                  }
                  inputs.add(input);
                }
              }
              break;
            case TEXT_EMBEDDING:
            case SPARSE_ENCODING:
              break;
            default:
              String _literal = function.getType().getLiteral();
              String _plus = (_literal + " functions");
              throw Unsupported.of(_plus);
          }
        } else {
          String _literal = function.getType().getLiteral();
          String _plus = (_literal + " functions");
          throw Unsupported.of(_plus);
        }
      }
      _xblockexpression = inputs;
    }
    return _xblockexpression;
  }

  /**
   * A search clause the runtime sends to observe whether the index has caught up with a write.
   */
  private Code.Entries probe(final VectorCollection c) {
    final VectorField dense = this.denseVector(c);
    if (((dense != null) && (PineconeSchema.embeddingModel(c, dense) == null))) {
      return PineconeSchema.kw("type", Code.text("dense_vector"), "field", Code.text(PineconeSchema.fieldKey(dense)), "dim", Code.num(dense.getDimension()));
    }
    if ((dense != null)) {
      return PineconeSchema.kw("type", Code.text("text"), "field", Code.text(PineconeSchema.searchKey(c, dense)));
    }
    final List<PayloadField> searched = this.fullTextFields(c);
    boolean _isEmpty = searched.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      return PineconeSchema.kw("type", Code.text("text"), "field", Code.text(PineconeSchema.fieldKey(IterableExtensions.<PayloadField>head(searched))));
    }
    final VectorField sparse = this.sparseVector(c);
    if (((sparse != null) && (PineconeSchema.embeddingModel(c, sparse) != null))) {
      return PineconeSchema.kw("type", Code.text("text"), "field", Code.text(PineconeSchema.searchKey(c, sparse)));
    }
    throw Unsupported.of("writes to a collection without a dense vector or full-text field");
  }

  private Code.Entries limits(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          final Code.Entries limit = PineconeSchema.kw();
          boolean _written = ModelUtil.written(field, "maxLength");
          if (_written) {
            limit.put("max_length", Code.num(field.getMaxLength()));
          }
          final Function1<FieldLimit, Boolean> _function = (FieldLimit it) -> {
            EFieldLimit _type = it.getType();
            return Boolean.valueOf(Objects.equals(_type, EFieldLimit.ARRAY_LEN));
          };
          final FieldLimit capacity = IterableExtensions.<FieldLimit>findFirst(field.getLimits(), _function);
          if ((capacity != null)) {
            limit.put("max_items", Code.num(capacity.getValue()));
          }
          boolean _isEmpty = limit.entries().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            result.put(ModelUtil.fieldName(field), limit);
          }
        }
      }
      final VectorField dense = this.denseVector(c);
      if (((dense != null) && (PineconeSchema.embeddingModel(c, dense) == null))) {
        result.put(ModelUtil.fieldName(dense), PineconeSchema.kw("dim", Code.num(dense.getDimension())));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public EMetric metric(final VectorField field, final VectorIndex index) {
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
    return _xifexpression;
  }

  public String metricName(final EMetric metric) {
    String _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = "cosine";
          break;
        case DOT:
          _switchResult = "dotproduct";
          break;
        case EUCLIDEAN:
          _switchResult = "euclidean";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric; Pinecone scores by cosine, dotproduct or euclidean");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric; Pinecone scores by cosine, dotproduct or euclidean");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * Pinecone reads are eventually consistent and take no consistency level.
   */
  public void consistency(final EConsistencyLevel level) {
    boolean _notEquals = (!Objects.equals(level, EConsistencyLevel.EVENTUAL));
    if (_notEquals) {
      String _literal = level.getLiteral();
      String _plus = ("consistency " + _literal);
      String _plus_1 = (_plus + "; Pinecone reads are eventually consistent (use wait on writes)");
      throw Unsupported.of(_plus_1);
    }
  }

  public Map<String, Code.Expr> extValues(final EObject host) {
    LinkedHashMap<String, Code.Expr> _xblockexpression = null;
    {
      final LinkedHashMap<String, Code.Expr> values = new LinkedHashMap<String, Code.Expr>();
      if ((host != null)) {
        Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, PineconeGenerator.VENDOR).entrySet();
        for (final Map.Entry<String, Literal> entry : _entrySet) {
          values.put(entry.getKey(), Literals.of(entry.getValue()));
        }
      }
      _xblockexpression = values;
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
