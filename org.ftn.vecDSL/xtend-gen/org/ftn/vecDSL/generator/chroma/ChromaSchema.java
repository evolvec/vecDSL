package org.ftn.vecDSL.generator.chroma;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
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
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to Chroma collections (server 1.5.9). A Chroma collection holds one dense embedding per record,
 * an optional document and flat metadata; its HNSW space and graph parameters are set when it is created. The single vector
 * field is the embedding, the document-body field (or the only full-text field) is the document, and every other payload
 * field is a metadata key. Record ids are the key's text, and the key is also stored in the metadata under its own name,
 * typed and filterable.
 */
@SuppressWarnings("all")
public class ChromaSchema {
  private static final List<EScalarDType> INTEGRAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8));

  private static final List<EScalarDType> NUMERIC = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> TEXTUAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final String NAME = "[a-zA-Z0-9][a-zA-Z0-9._-]{1,510}[a-zA-Z0-9]";

  private static final Set<EPayloadIndexKind> KEYS = Collections.<EPayloadIndexKind>unmodifiableSet(CollectionLiterals.<EPayloadIndexKind>newHashSet(EPayloadIndexKind.KEYWORD, EPayloadIndexKind.INTEGER, EPayloadIndexKind.FLOAT, EPayloadIndexKind.BOOL, EPayloadIndexKind.UUID));

  /**
   * Metadata keys that hold tenant and namespace values when the collection declares no partition-key field.
   */
  public static final String TENANT_KEY = "vecdsl_tenant";

  public static final String NAMESPACE_KEY = "vecdsl_namespace";

  private final List<String> notes;

  public ChromaSchema(final List<String> notes) {
    this.notes = notes;
  }

  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("collections derived from external sources; Chroma stores the records of every collection itself");
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of("collection ttl; Chroma records do not expire");
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; Chroma record ids are the primary key text");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of("deletion-protection; Chroma collections have no deletion guard");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; Chroma records hold no references to other collections");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + ChromaStatements.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of("storage settings; Chroma keeps every collection in its persist path");
      }
      boolean _isEmpty_1 = ModelUtil.partitions(c).isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
      }
      boolean _isEmpty_2 = c.getAliases().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        throw Unsupported.of(ChromaStatements.NO_ALIASES);
      }
      boolean _isEmpty_3 = c.getFunctions().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + ChromaStatements.NO_RANKED_TEXT);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_4 = c.getAnalyzers().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        throw Unsupported.of(("analyzers; " + ChromaStatements.NO_ANALYZERS));
      }
      Set<String> _keySet = ModelUtil.extParams(c, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "limits");
      ChromaSchema.checkName(c.getName());
      this.keyType(c);
      final VectorField field = this.vectorField(c);
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      final Code.Entries args = ChromaSchema.kw("name", Code.text(c.getName()));
      final String database = ChromaSchema.databaseOf(c);
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      args.put("metric", Code.text(ChromaSchema.space(this.metric(field, index))));
      if ((index != null)) {
        final Code.Entries graph = this.hnsw(index);
        boolean _isEmpty_5 = graph.entries().isEmpty();
        boolean _not_5 = (!_isEmpty_5);
        if (_not_5) {
          args.put("hnsw", graph);
        }
      }
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField payload : _payloadFields) {
        this.checkPayloadField(c, payload);
      }
      this.documentField(c);
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex declared : _filter) {
        this.payloadIndex(c, declared);
      }
      final Code.Entries metadata = Code.entries();
      String _description = c.getDescription();
      boolean _tripleNotEquals_3 = (_description != null);
      if (_tripleNotEquals_3) {
        metadata.put("description", Code.text(c.getDescription()));
      }
      boolean _isEmpty_6 = c.getTags().isEmpty();
      boolean _not_6 = (!_isEmpty_6);
      if (_not_6) {
        metadata.put("tags", Code.texts(c.getTags()));
      }
      boolean _isEmpty_7 = metadata.entries().isEmpty();
      boolean _not_7 = (!_isEmpty_7);
      if (_not_7) {
        args.put("metadata", metadata);
      }
      Code.Call _runtime = Code.runtime("create_collection", args);
      Code.Run _run = new Code.Run(_runtime);
      final Code.Stmt create = ((Code.Stmt) _run);
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create));
      }
      Code.Call _runtime_1 = Code.runtime("has_collection", this.place(c));
      Code.When _when = new Code.When(_runtime_1, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Name and database of a collection, the arguments of the calls that address a collection by name.
   */
  public Code.Entries place(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = ChromaSchema.kw("name", Code.text(c.getName()));
      final String database = ChromaSchema.databaseOf(c);
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * Collection identity every record call needs: name, database, key, metric, vector field and document field.
   */
  public Code.Entries layout(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = this.place(c);
      args.put("key", Code.text(ModelUtil.fieldName(c.getPrimaryKey())));
      args.put("key_type", Code.text(this.keyType(c)));
      final VectorField field = this.vectorField(c);
      args.put("metric", Code.text(ChromaSchema.space(this.metric(field, ModelUtil.vectorIndex(c, field)))));
      args.put("vector", Code.text(ModelUtil.fieldName(field)));
      int _dimension = field.getDimension();
      boolean _greaterThan = (_dimension > 0);
      if (_greaterThan) {
        args.put("dim", Code.num(field.getDimension()));
      }
      final PayloadField document = this.documentField(c);
      if ((document != null)) {
        args.put("document", Code.text(ModelUtil.fieldName(document)));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * The layout plus what writes need: integer fields, declared limits and defaults.
   */
  public Code.Entries writeLayout(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = this.layout(c);
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf((ChromaSchema.INTEGRAL.contains(it.getType()) || (Objects.equals(it.getType(), EScalarDType.ARRAY) && ChromaSchema.INTEGRAL.contains(it.getElementType()))));
      };
      final Function1<PayloadField, String> _function_1 = (PayloadField it) -> {
        return ModelUtil.fieldName(it);
      };
      final List<String> integers = IterableExtensions.<String>toList(IterableExtensions.<PayloadField, String>map(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function), _function_1));
      boolean _isEmpty = integers.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("integers", Code.texts(integers));
      }
      final Code.Entries declared = this.limits(c);
      boolean _isEmpty_1 = declared.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("limits", declared);
      }
      final Code.Entries defaults = Code.entries();
      final Function1<PayloadField, Boolean> _function_2 = (PayloadField it) -> {
        Literal _defaultValue = it.getDefaultValue();
        return Boolean.valueOf((_defaultValue != null));
      };
      Iterable<PayloadField> _filter = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_2);
      for (final PayloadField field : _filter) {
        defaults.put(ModelUtil.fieldName(field), Literals.of(field.getDefaultValue()));
      }
      boolean _isEmpty_2 = defaults.entries().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        args.put("defaults", defaults);
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * The Chroma database of a collection declared in a logical-db, else null for the program's database.
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      if ((database == null)) {
        return null;
      }
      ChromaSchema.checkName(database.getName());
      _xblockexpression = database.getName();
    }
    return _xblockexpression;
  }

  public static void checkName(final String name) {
    boolean _matches = name.matches(ChromaSchema.NAME);
    boolean _not = (!_matches);
    if (_not) {
      throw Unsupported.of((("the name \'" + name) + "\'; Chroma names have 3 to 512 characters from [a-zA-Z0-9._-] and start and end with a letter or digit"));
    }
  }

  /**
   * The one dense vector field: Chroma stores one embedding per record.
   */
  public VectorField vectorField(final VectorCollection c) {
    VectorField _xblockexpression = null;
    {
      final List<VectorField> fields = ModelUtil.vectorFields(c);
      for (final VectorField field : fields) {
        EVectorDType _type = field.getType();
        boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
        if (_equals) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus = ("sparse vector field " + _fieldName);
          String _plus_1 = (_plus + "; ");
          String _plus_2 = (_plus_1 + ChromaStatements.NO_RANKED_TEXT);
          throw Unsupported.of(_plus_2);
        }
      }
      int _size = fields.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        int _size_1 = fields.size();
        String _plus_3 = ("a collection with " + Integer.valueOf(_size_1));
        String _plus_4 = (_plus_3 + " vector fields; a Chroma collection holds one embedding per record");
        throw Unsupported.of(_plus_4);
      }
      final VectorField field_1 = IterableExtensions.<VectorField>head(fields);
      final String name = ModelUtil.fieldName(field_1);
      EVectorDType _type_1 = field_1.getType();
      boolean _notEquals_1 = (!Objects.equals(_type_1, EVectorDType.FLOAT_VECTOR));
      if (_notEquals_1) {
        String _literal = field_1.getType().getLiteral();
        String _plus_5 = (_literal + " fields; Chroma stores float32 embeddings");
        throw Unsupported.of(_plus_5);
      }
      boolean _isMultiVector = field_1.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; Chroma stores one embedding per record"));
      }
      if (((field_1.getModel() != null) || (ModelUtil.producingFunction(c, field_1) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + ChromaStatements.NO_MODELS));
      }
      this.note(field_1, "normalized", "onDisk", "nullable", "limits");
      _xblockexpression = field_1;
    }
    return _xblockexpression;
  }

  public String keyType(final VectorCollection c) {
    final Field key = c.getPrimaryKey();
    if ((!(key instanceof PayloadField))) {
      throw Unsupported.of("a vector field as primary key");
    }
    final EScalarDType type = ((PayloadField) key).getType();
    boolean _contains = ChromaSchema.INTEGRAL.contains(type);
    if (_contains) {
      return "int";
    }
    boolean _contains_1 = ChromaSchema.TEXTUAL.contains(type);
    if (_contains_1) {
      return "text";
    }
    String _literal = type.getLiteral();
    String _plus = ("primary key of type " + _literal);
    String _plus_1 = (_plus + "; Chroma ids are strings");
    throw Unsupported.of(_plus_1);
  }

  /**
   * The field stored as the Chroma document: the document-body field, else the only full-text field.
   */
  public PayloadField documentField(final VectorCollection c) {
    PayloadField _xblockexpression = null;
    {
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(it.isDocumentBody());
      };
      final List<PayloadField> bodies = IterableExtensions.<PayloadField>toList(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function));
      final Function1<PayloadField, Boolean> _function_1 = (PayloadField it) -> {
        return Boolean.valueOf(((it.isFullText() || (it.getAnalyzer() != null)) || this.fullTextIndexed(c, it)));
      };
      final List<PayloadField> searched = IterableExtensions.<PayloadField>toList(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_1));
      int _size = bodies.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        throw Unsupported.of("several document-body fields; a Chroma record has one document");
      }
      PayloadField _elvis = null;
      PayloadField _head = IterableExtensions.<PayloadField>head(bodies);
      if (_head != null) {
        _elvis = _head;
      } else {
        PayloadField _head_1 = IterableExtensions.<PayloadField>head(searched);
        _elvis = _head_1;
      }
      final PayloadField chosen = _elvis;
      for (final PayloadField field : searched) {
        if ((field != chosen)) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus = ("full-text field " + _fieldName);
          String _plus_1 = (_plus + " beside the document ");
          String _fieldName_1 = ModelUtil.fieldName(chosen);
          String _plus_2 = (_plus_1 + _fieldName_1);
          String _plus_3 = (_plus_2 + "; a Chroma record has one document");
          throw Unsupported.of(_plus_3);
        }
      }
      if (((chosen != null) && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(chosen.getType())))) {
        String _fieldName_2 = ModelUtil.fieldName(chosen);
        String _plus_4 = ("document field " + _fieldName_2);
        String _plus_5 = (_plus_4 + " of type ");
        String _literal = chosen.getType().getLiteral();
        String _plus_6 = (_plus_5 + _literal);
        String _plus_7 = (_plus_6 + "; Chroma documents are text");
        throw Unsupported.of(_plus_7);
      }
      Field _primaryKey = c.getPrimaryKey();
      boolean _tripleEquals = (chosen == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("the primary key as document; Chroma keeps the key in the metadata");
      }
      _xblockexpression = chosen;
    }
    return _xblockexpression;
  }

  private boolean fullTextIndexed(final VectorCollection c, final PayloadField field) {
    final Function1<PayloadIndex, Boolean> _function = (PayloadIndex index) -> {
      return Boolean.valueOf(((index.getTarget().getField() == field) && Objects.equals(index.getKind(), EPayloadIndexKind.FULL_TEXT)));
    };
    return IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function);
  }

  /**
   * Checks a payload field against what Chroma metadata holds: strings, integers, floats, booleans and non-empty arrays of one of them.
   */
  public void checkPayloadField(final VectorCollection c, final PayloadField field) {
    final String name = ModelUtil.fieldName(field);
    if ((field.isPartitionKey() && (!ChromaSchema.TEXTUAL.contains(field.getType())))) {
      String _literal = field.getType().getLiteral();
      String _plus = ((("partition key " + name) + " of type ") + _literal);
      String _plus_1 = (_plus + "; tenant and namespace names are text");
      throw Unsupported.of(_plus_1);
    }
    if (((!field.getSubFields().isEmpty()) || field.isNested())) {
      throw Unsupported.of((("nested field " + name) + "; Chroma metadata is flat"));
    }
    EScalarDType _xifexpression = null;
    EScalarDType _type = field.getType();
    boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
    if (_equals) {
      _xifexpression = field.getElementType();
    } else {
      _xifexpression = field.getType();
    }
    final EScalarDType type = _xifexpression;
    if ((Objects.equals(field.getType(), EScalarDType.ARRAY) && (!ModelUtil.written(field, "elementType")))) {
      throw Unsupported.of((("array field " + name) + " without element-type"));
    }
    if ((((!ChromaSchema.NUMERIC.contains(type)) && (!ChromaSchema.TEXTUAL.contains(type))) && (!Objects.equals(type, EScalarDType.BOOL)))) {
      String _literal_1 = type.getLiteral();
      String _plus_2 = (_literal_1 + " field ");
      String _plus_3 = (_plus_2 + name);
      String _plus_4 = (_plus_3 + "; Chroma metadata holds strings, integers, floats, booleans and arrays of them");
      throw Unsupported.of(_plus_4);
    }
    this.note(field, "nullable");
  }

  /**
   * Declared payload indexes: Chroma's default schema already keeps an inverted index on every metadata key.
   */
  public void payloadIndex(final VectorCollection c, final PayloadIndex index) {
    final FieldPath path = index.getTarget();
    if (((path.getRawPath() != null) || (!path.getSubFields().isEmpty()))) {
      throw Unsupported.of("a payload index on a nested path; Chroma metadata is flat");
    }
    EPayloadIndexKind _kind = index.getKind();
    boolean _equals = Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT);
    if (_equals) {
      Field _field = path.getField();
      PayloadField _documentField = this.documentField(c);
      boolean _tripleNotEquals = (_field != _documentField);
      if (_tripleNotEquals) {
        String _fieldName = ModelUtil.fieldName(path.getField());
        String _plus = ("a full_text index on " + _fieldName);
        String _plus_1 = (_plus + ", which is not the document; Chroma indexes the document only");
        throw Unsupported.of(_plus_1);
      }
    } else {
      boolean _contains = ChromaSchema.KEYS.contains(index.getKind());
      boolean _not = (!_contains);
      if (_not) {
        String _literal = index.getKind().getLiteral();
        String _plus_2 = (_literal + " payload indexes; Chroma indexes metadata strings, integers, floats and booleans");
        throw Unsupported.of(_plus_2);
      } else {
        Field _field_1 = path.getField();
        boolean _not_1 = (!(_field_1 instanceof PayloadField));
        if (_not_1) {
          throw Unsupported.of("a payload index on a vector field");
        }
      }
    }
    this.note(index, "structure", "castType", "analyzer", "onDisk", "lowercase", "prefixMatch", "minTokenLength", "maxTokenLength");
    Set<String> _keySet = ModelUtil.extParams(index, ChromaGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * Chroma's HNSW parameters of a vector index: max-degree, build-candidates and search-candidates.
   */
  public Code.Entries hnsw(final VectorIndex index) {
    Code.Entries _xblockexpression = null;
    {
      EIndexFamily _family = index.getFamily();
      if (_family != null) {
        switch (_family) {
          case AUTO:
          case HNSW:
            break;
          default:
            String _literal = index.getFamily().getLiteral();
            String _plus = (_literal + " indexes; the local Chroma server builds HNSW graphs (SPANN is distributed only)");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = index.getFamily().getLiteral();
        String _plus = (_literal + " indexes; the local Chroma server builds HNSW graphs (SPANN is distributed only)");
        throw Unsupported.of(_plus);
      }
      Quantization _quantization = index.getQuantization();
      boolean _tripleNotEquals = (_quantization != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("quantization; the local Chroma server stores full-precision HNSW graphs");
      }
      this.note(index, "accelerator", "centroids", "onDisk", "online");
      Set<String> _keySet = ModelUtil.extParams(index, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Code.Entries result = Code.entries();
      boolean _written = ModelUtil.written(index, "maxDegree");
      if (_written) {
        result.put("max_neighbors", Code.num(index.getMaxDegree()));
      }
      boolean _written_1 = ModelUtil.written(index, "buildCandidates");
      if (_written_1) {
        result.put("ef_construction", Code.num(index.getBuildCandidates()));
      }
      boolean _written_2 = ModelUtil.written(index, "searchCandidates");
      if (_written_2) {
        result.put("ef_search", Code.num(index.getSearchCandidates()));
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

  /**
   * Chroma's HNSW space of a metric: l2 is squared Euclidean distance, ip and cosine are 1 minus the similarity.
   */
  public static String space(final EMetric metric) {
    String _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = "cosine";
          break;
        case DOT:
          _switchResult = "ip";
          break;
        case EUCLIDEAN:
          _switchResult = "l2";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric; Chroma measures l2, ip and cosine distances");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric; Chroma measures l2, ip and cosine distances");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * The metadata key of tenant or namespace values: the partition-key field, else a reserved key.
   */
  public static String sliceKey(final VectorCollection c, final boolean namespace) {
    String _xblockexpression = null;
    {
      final Field key = ModelUtil.partitionKey(c);
      if ((key != null)) {
        return ModelUtil.fieldName(key);
      }
      String _xifexpression = null;
      if (namespace) {
        _xifexpression = ChromaSchema.NAMESPACE_KEY;
      } else {
        _xifexpression = ChromaSchema.TENANT_KEY;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private Code.Entries limits(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          final Code.Entries limit = ChromaSchema.kw();
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
      _xblockexpression = result;
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
