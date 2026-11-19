package org.ftn.vecDSL.generator.qdrant;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
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
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPartitionStrategy;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EPayloadIndexStructure;
import org.ftn.vecDSL.vecDSL.EQuantizationKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EStorageType;
import org.ftn.vecDSL.vecDSL.ETokenizer;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
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
import org.ftn.vecDSL.vecDSL.ReplicationConfig;
import org.ftn.vecDSL.vecDSL.SegmentOptimizers;
import org.ftn.vecDSL.vecDSL.ShardConfig;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps vecDSL collections to Qdrant collections (server 1.20). Every vector field becomes a named vector (dense or
 * sparse); payload fields are schemaless JSON, so the schema consists of the payload indexes: the declared ones, a
 * BM25-scored text index per full-text field and an index on the primary key. Point ids are UUIDv5 of the key's text
 * (the Weaviate convention), so the runtime also stores the key in the payload under its own name, typed and filterable.
 */
@SuppressWarnings("all")
public class QdrantSchema {
  /**
   * Payload keys that hold tenant and namespace values when the collection declares no partition-key field.
   */
  public static final String TENANT_KEY = "vecdsl_tenant";

  public static final String NAMESPACE_KEY = "vecdsl_namespace";

  private static final List<EScalarDType> INTEGRAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8));

  private static final List<EScalarDType> NUMERIC = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> TEXTUAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final List<EScalarDType> TEMPORAL = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private static final List<EScalarDType> STRUCTURED = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.JSON, EScalarDType.STRUCT, EScalarDType.MAP));

  private static final Map<String, String> LANGUAGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("en", "english"), Pair.<String, String>of("de", "german"), Pair.<String, String>of("fr", "french"), Pair.<String, String>of("es", "spanish"), Pair.<String, String>of("it", "italian"), Pair.<String, String>of("pt", "portuguese"), Pair.<String, String>of("nl", "dutch"), Pair.<String, String>of("ru", "russian"), Pair.<String, String>of("da", "danish"), Pair.<String, String>of("fi", "finnish"), Pair.<String, String>of("sv", "swedish"), Pair.<String, String>of("no", "norwegian"), Pair.<String, String>of("hu", "hungarian"), Pair.<String, String>of("ro", "romanian"), Pair.<String, String>of("tr", "turkish"), Pair.<String, String>of("el", "greek"), Pair.<String, String>of("ar", "arabic")));

  private static final Set<String> STOPWORD_LANGUAGES = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("arabic", "azerbaijani", "basque", "bengali", "catalan", "chinese", "danish", "dutch", "english", "finnish", "french", "german", "greek", "hebrew", "hinglish", "hungarian", "indonesian", "italian", "japanese", "kazakh", "nepali", "norwegian", "portuguese", "romanian", "russian", "slovene", "spanish", "swedish", "tajik", "turkish"));

  private static final Set<String> STEMMER_LANGUAGES = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("arabic", "armenian", "danish", "dutch", "english", "finnish", "french", "german", "greek", "hungarian", "italian", "norwegian", "portuguese", "romanian", "russian", "spanish", "swedish", "tamil", "turkish"));

  private static final Map<String, String> OPTIMIZERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("indexingThreshold", "indexing_threshold"), Pair.<String, String>of("memmapThreshold", "memmap_threshold"), Pair.<String, String>of("defaultSegmentNumber", "default_segment_number"), Pair.<String, String>of("maxSegmentSize", "max_segment_size"), Pair.<String, String>of("flushIntervalSec", "flush_interval_sec")));

  private final List<String> notes;

  public QdrantSchema(final List<String> notes) {
    this.notes = notes;
  }

  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("collections derived from external sources");
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of("collection ttl; Qdrant points do not expire");
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; Qdrant point ids are derived from the primary key");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of("deletion-protection; Qdrant collections have no deletion guard");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("embedding-model (Qdrant Cloud Inference is not generated)");
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "limits");
      boolean _written = ModelUtil.written(c, "consistency");
      if (_written) {
        this.readConsistency(c.getConsistency());
      }
      QdrantSchema.keyType(c);
      final Code.Text name = Code.text(c.getName());
      final Code.Entries args = QdrantSchema.kw("name", name);
      final Code.Entries dense = Code.entries();
      final Code.Entries sparse = Code.entries();
      List<VectorField> _vectorFields = ModelUtil.vectorFields(c);
      for (final VectorField field : _vectorFields) {
        EVectorDType _type = field.getType();
        boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
        if (_equals) {
          boolean _isBm25Output = QdrantSchema.isBm25Output(c, field);
          if (_isBm25Output) {
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("storage of " + _fieldName);
            String _plus_1 = (_plus + ", which Qdrant computes as BM25 over the text index");
            this.notes.add(_plus_1);
          } else {
            sparse.put(QdrantSchema.fieldKey(field), this.sparseVector(c, field));
          }
        } else {
          dense.put(QdrantSchema.fieldKey(field), this.denseVector(c, field));
        }
      }
      boolean _isEmpty_1 = dense.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("vectors", dense);
      }
      boolean _isEmpty_2 = sparse.entries().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        args.put("sparse_vectors", sparse);
      }
      args.put("indexes", Code.items(this.payloadIndexes(c)));
      this.clusterSettings(c, args);
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        args.put("payload", QdrantSchema.kw("memory", Code.text(QdrantSchema.payloadMemory(c.getStorage()))));
      }
      final List<Partition> partitions = ModelUtil.partitions(c);
      boolean _isEmpty_3 = partitions.isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        for (final Partition partition : partitions) {
          QdrantSchema.shardKey(partition);
        }
        args.put("sharding_method", Code.text("custom"));
        final Function1<Partition, String> _function = (Partition it) -> {
          return it.getName();
        };
        args.put("shard_keys", Code.texts(ListExtensions.<Partition, String>map(partitions, _function)));
      }
      this.collectionExtensions(c, args);
      final Code.Entries metadata = Code.entries();
      boolean _isEmpty_4 = c.getTags().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        metadata.put("tags", Code.texts(c.getTags()));
      }
      String _description = c.getDescription();
      boolean _tripleNotEquals_3 = (_description != null);
      if (_tripleNotEquals_3) {
        metadata.put("description", Code.text(c.getDescription()));
      }
      boolean _isEmpty_5 = metadata.entries().isEmpty();
      boolean _not_5 = (!_isEmpty_5);
      if (_not_5) {
        args.put("metadata", metadata);
      }
      boolean _isEmpty_6 = c.getAliases().isEmpty();
      boolean _not_6 = (!_isEmpty_6);
      if (_not_6) {
        final Function1<Alias, String> _function_1 = (Alias it) -> {
          return it.getName();
        };
        args.put("aliases", Code.texts(ListExtensions.<Alias, String>map(c.getAliases(), _function_1)));
      }
      Code.Call _runtime = Code.runtime("create_collection", args);
      Code.Run _run = new Code.Run(_runtime);
      final Code.Stmt create = ((Code.Stmt) _run);
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create));
      }
      Code.Call _runtime_1 = Code.runtime("has_collection", QdrantSchema.kw("name", name));
      Code.When _when = new Code.When(_runtime_1, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Collection identity every runtime call needs: name, primary key and key form, read consistency.
   */
  public Code.Entries layout(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = QdrantSchema.kw("name", Code.text(c.getName()), "key", Code.text(ModelUtil.fieldName(c.getPrimaryKey())), "key_type", Code.text(QdrantSchema.keyType(c)));
      if ((ModelUtil.written(c, "consistency") && (this.readConsistency(c.getConsistency()) != null))) {
        args.put("read_consistency", Code.text(this.readConsistency(c.getConsistency())));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * Replication and shard count of the cluster that holds the collection (by reference or as its member).
   */
  private void clusterSettings(final VectorCollection c, final Code.Entries args) {
    Cluster _elvis = null;
    Cluster _cluster = c.getCluster();
    if (_cluster != null) {
      _elvis = _cluster;
    } else {
      Cluster _containerOfType = EcoreUtil2.<Cluster>getContainerOfType(c, Cluster.class);
      _elvis = _containerOfType;
    }
    final Cluster cluster = _elvis;
    if ((cluster == null)) {
      return;
    }
    ReplicationConfig _replication = cluster.getReplication();
    boolean _tripleNotEquals = (_replication != null);
    if (_tripleNotEquals) {
      args.put("replication_factor", Code.num(cluster.getReplication().getFactor()));
    }
    ShardConfig _sharding = cluster.getSharding();
    boolean _tripleNotEquals_1 = (_sharding != null);
    if (_tripleNotEquals_1) {
      args.put("shard_number", Code.num(QdrantSchema.shardNumber(cluster.getSharding())));
    }
  }

  public static int shardNumber(final ShardConfig sharding) {
    int _xblockexpression = (int) 0;
    {
      if (((sharding.getKey() != null) || (ModelUtil.written(sharding, "strategy") && (!Objects.equals(sharding.getStrategy(), EPartitionStrategy.HASH))))) {
        String _xifexpression = null;
        Field _key = sharding.getKey();
        boolean _tripleNotEquals = (_key != null);
        if (_tripleNotEquals) {
          _xifexpression = "a key field";
        } else {
          _xifexpression = sharding.getStrategy().getLiteral();
        }
        String _plus = ("sharding by " + _xifexpression);
        String _plus_1 = (_plus + "; Qdrant distributes points over shards by hashing the point id, or by shard keys named in each write (custom sharding, see partitions)");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = sharding.getShards();
    }
    return _xblockexpression;
  }

  /**
   * Qdrant memory placement of the payload storage: memory pre-loads it into RAM, disk and mmap keep it cold.
   */
  public static String payloadMemory(final StorageConfig storage) {
    String _xblockexpression = null;
    {
      if ((((storage.isTiered() || (storage.getLocation() != null)) || Objects.equals(storage.getType(), EStorageType.OBJECT_STORAGE)) || Objects.equals(storage.getType(), EStorageType.EMBEDDED))) {
        String _xifexpression = null;
        boolean _isTiered = storage.isTiered();
        if (_isTiered) {
          _xifexpression = "tiering";
        } else {
          String _xifexpression_1 = null;
          String _location = storage.getLocation();
          boolean _tripleNotEquals = (_location != null);
          if (_tripleNotEquals) {
            _xifexpression_1 = "locations";
          } else {
            _xifexpression_1 = storage.getType().getLiteral();
          }
          _xifexpression = _xifexpression_1;
        }
        String _plus = ("storage " + _xifexpression);
        String _plus_1 = (_plus + "; Qdrant keeps collection data in its storage path and places it in RAM (cached, pinned) or leaves it on disk (cold)");
        throw Unsupported.of(_plus_1);
      }
      String _xifexpression_2 = null;
      if ((Objects.equals(storage.getType(), EStorageType.MEMORY) && (!storage.isMmap()))) {
        _xifexpression_2 = "cached";
      } else {
        _xifexpression_2 = "cold";
      }
      _xblockexpression = _xifexpression_2;
    }
    return _xblockexpression;
  }

  /**
   * A partition is a Qdrant shard key of a custom-sharded collection; writes name it, reads select it.
   */
  public static String shardKey(final Partition partition) {
    String _xblockexpression = null;
    {
      if ((ModelUtil.written(partition, "strategy") && Collections.<EPartitionStrategy>unmodifiableList(CollectionLiterals.<EPartitionStrategy>newArrayList(EPartitionStrategy.HASH, EPartitionStrategy.RANGE)).contains(partition.getStrategy()))) {
        String _literal = partition.getStrategy().getLiteral();
        String _plus = ("partition strategy " + _literal);
        String _plus_1 = (_plus + "; Qdrant routes a point to the shard key named in its write (custom sharding)");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = partition.getName();
    }
    return _xblockexpression;
  }

  /**
   * The payload key of tenant or namespace values: the partition-key field, else a reserved key.
   */
  public static String sliceKey(final VectorCollection c, final boolean namespace) {
    String _xblockexpression = null;
    {
      final Field key = ModelUtil.partitionKey(c);
      if ((key != null)) {
        return QdrantSchema.fieldKey(key);
      }
      String _xifexpression = null;
      if (namespace) {
        _xifexpression = QdrantSchema.NAMESPACE_KEY;
      } else {
        _xifexpression = QdrantSchema.TENANT_KEY;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * The tenant index of a slice key: a keyword index with is_tenant, so Qdrant co-locates the points of each value.
   */
  public static Code.Entries sliceIndex() {
    return QdrantSchema.kw("type", Code.text("keyword"), "is_tenant", Code.flag(true));
  }

  /**
   * The layout plus what writes need: vector names, integer fields, declared limits, defaults and write ordering.
   */
  public Code.Entries writeLayout(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = this.layout(c);
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        boolean _isBm25Output = QdrantSchema.isBm25Output(c, it);
        return Boolean.valueOf((!_isBm25Output));
      };
      final Function1<VectorField, String> _function_1 = (VectorField it) -> {
        return QdrantSchema.fieldKey(it);
      };
      final List<String> vectors = IterableExtensions.<String>toList(IterableExtensions.<VectorField, String>map(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function), _function_1));
      boolean _isEmpty = vectors.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(vectors));
      }
      final Function1<PayloadField, Boolean> _function_2 = (PayloadField it) -> {
        return Boolean.valueOf(QdrantSchema.INTEGRAL.contains(it.getType()));
      };
      final Function1<PayloadField, String> _function_3 = (PayloadField it) -> {
        return ModelUtil.fieldName(it);
      };
      final List<String> integers = IterableExtensions.<String>toList(IterableExtensions.<PayloadField, String>map(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_2), _function_3));
      boolean _isEmpty_1 = integers.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("integers", Code.texts(integers));
      }
      final Code.Entries declared = this.limits(c);
      boolean _isEmpty_2 = declared.entries().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        args.put("limits", declared);
      }
      final Code.Entries defaults = Code.entries();
      final Function1<PayloadField, Boolean> _function_4 = (PayloadField it) -> {
        Literal _defaultValue = it.getDefaultValue();
        return Boolean.valueOf((_defaultValue != null));
      };
      Iterable<PayloadField> _filter = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_4);
      for (final PayloadField field : _filter) {
        defaults.put(ModelUtil.fieldName(field), Literals.of(field.getDefaultValue()));
      }
      boolean _isEmpty_3 = defaults.entries().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        args.put("defaults", defaults);
      }
      if ((ModelUtil.written(c, "consistency") && Objects.equals(c.getConsistency(), EConsistencyLevel.STRONG))) {
        args.put("ordering", Code.text("strong"));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  public static String keyType(final VectorCollection c) {
    final Field key = c.getPrimaryKey();
    if ((!(key instanceof PayloadField))) {
      throw Unsupported.of("a vector field as primary key");
    }
    final EScalarDType type = ((PayloadField) key).getType();
    boolean _contains = QdrantSchema.INTEGRAL.contains(type);
    if (_contains) {
      return "int";
    }
    boolean _contains_1 = QdrantSchema.TEXTUAL.contains(type);
    if (_contains_1) {
      return "text";
    }
    String _literal = type.getLiteral();
    String _plus = ("primary key of type " + _literal);
    throw Unsupported.of(_plus);
  }

  /**
   * Field name as a Qdrant payload key or vector name; '.' and '[' would be read as a nested path.
   */
  public static String fieldKey(final Field field) {
    String _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      if ((name.contains(".") || name.contains("["))) {
        throw Unsupported.of((("field name \'" + name) + "\'; Qdrant reads \'.\' and \'[\' in payload keys as nested paths"));
      }
      _xblockexpression = name;
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
   * Payload fields searched by keyword: declared full-text, analyzed, BM25 inputs or under a full-text index.
   */
  public List<PayloadField> fullTextFields(final VectorCollection c) {
    List<PayloadField> _xblockexpression = null;
    {
      final List<Field> inputs = this.bm25Inputs(c);
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf((((it.isFullText() || (it.getAnalyzer() != null)) || inputs.contains(it)) || IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex index) -> {
          return Boolean.valueOf((((index.getTarget().getField() == it) && index.getTarget().getSubFields().isEmpty()) && Objects.equals(index.getKind(), EPayloadIndexKind.FULL_TEXT)));
        }))));
      };
      _xblockexpression = IterableExtensions.<PayloadField>toList(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function));
    }
    return _xblockexpression;
  }

  public boolean isFullText(final VectorCollection c, final Field field) {
    return this.fullTextFields(c).contains(field);
  }

  /**
   * The analyzer of a full-text field: its own, or the analyzer of its full-text index.
   */
  public Analyzer analyzerOf(final VectorCollection c, final PayloadField field) {
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
    return _elvis;
  }

  /**
   * Per-query BM25 parameters of a field, from its analyzer's bm25-k1 and bm25-b.
   */
  public Code.Entries bm25Params(final VectorCollection c, final PayloadField field) {
    Code.Entries _xblockexpression = null;
    {
      final Analyzer analyzer = this.analyzerOf(c, field);
      if ((analyzer == null)) {
        return null;
      }
      final Code.Entries result = Code.entries();
      boolean _written = ModelUtil.written(analyzer, "bm25K1");
      if (_written) {
        result.put("k", Code.num(analyzer.getBm25K1()));
      }
      boolean _written_1 = ModelUtil.written(analyzer, "bm25B");
      if (_written_1) {
        result.put("b", Code.num(analyzer.getBm25B()));
      }
      Code.Entries _xifexpression = null;
      boolean _isEmpty = result.entries().isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = result;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * Qdrant parameters of a named vector: VectorParams for dense fields, SparseVectorParams for sparse ones.
   */
  public Code.Entries vectorParams(final VectorCollection c, final VectorField field) {
    Code.Entries _xifexpression = null;
    EVectorDType _type = field.getType();
    boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
    if (_equals) {
      _xifexpression = this.sparseVector(c, field);
    } else {
      _xifexpression = this.denseVector(c, field);
    }
    return _xifexpression;
  }

  private Code.Entries denseVector(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + " (Qdrant Cloud Inference is not generated)"));
      }
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim; a Qdrant dense vector declares its size"));
      }
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      final Code.Entries params = QdrantSchema.kw("size", Code.num(field.getDimension()), "distance", Code.text(this.distance(this.metric(field, index))));
      EVectorDType _type = field.getType();
      if (_type != null) {
        switch (_type) {
          case FLOAT_VECTOR:
            break;
          case FLOAT16_VECTOR:
            params.put("datatype", Code.text("float16"));
            break;
          case UINT8_VECTOR:
            params.put("datatype", Code.text("uint8"));
            break;
          default:
            String _literal = field.getType().getLiteral();
            String _plus = (_literal + " fields; Qdrant stores float32, float16 and uint8 vectors");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " fields; Qdrant stores float32, float16 and uint8 vectors");
        throw Unsupported.of(_plus);
      }
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        params.put("multivector_config", QdrantSchema.kw("comparator", Code.text("max_sim")));
      }
      boolean _isOnDisk = field.isOnDisk();
      if (_isOnDisk) {
        params.put("memory", Code.text("cold"));
      }
      this.note(field, "normalized");
      if ((index != null)) {
        this.vectorIndex(index, params);
      }
      _xblockexpression = params;
    }
    return _xblockexpression;
  }

  private Code.Entries sparseVector(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries params = QdrantSchema.kw();
      boolean _written = ModelUtil.written(field, "modifier");
      if (_written) {
        params.put("modifier", Code.text(field.getModifier().getLiteral()));
      }
      this.note(field, "normalized", "onDisk");
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      if ((index != null)) {
        if (((!Objects.equals(index.getFamily(), EIndexFamily.SPARSE_INVERTED)) && (!Objects.equals(index.getFamily(), EIndexFamily.AUTO)))) {
          String _literal = index.getFamily().getLiteral();
          String _plus = (_literal + " index on sparse field ");
          String _fieldName = ModelUtil.fieldName(field);
          String _plus_1 = (_plus + _fieldName);
          String _plus_2 = (_plus_1 + "; Qdrant indexes sparse vectors with an inverted index");
          throw Unsupported.of(_plus_2);
        }
        this.note(index, "maxDegree", "buildCandidates", "searchCandidates", "centroids", "accelerator", "onDisk", "online", "quantization");
      }
      _xblockexpression = params;
    }
    return _xblockexpression;
  }

  public void vectorIndex(final VectorIndex index, final Code.Entries params) {
    final Code.Entries hnsw = Code.entries();
    EIndexFamily _family = index.getFamily();
    if (_family != null) {
      switch (_family) {
        case AUTO:
        case HNSW:
          boolean _written = ModelUtil.written(index, "maxDegree");
          if (_written) {
            hnsw.put("m", Code.num(index.getMaxDegree()));
          }
          boolean _written_1 = ModelUtil.written(index, "buildCandidates");
          if (_written_1) {
            hnsw.put("ef_construct", Code.num(index.getBuildCandidates()));
          }
          break;
        case FLAT:
          hnsw.put("m", Code.num(0));
          break;
        default:
          String _literal = index.getFamily().getLiteral();
          String _plus = (_literal + " indexes; Qdrant builds HNSW graphs (family flat disables the graph)");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = index.getFamily().getLiteral();
      String _plus = (_literal + " indexes; Qdrant builds HNSW graphs (family flat disables the graph)");
      throw Unsupported.of(_plus);
    }
    boolean _isOnDisk = index.isOnDisk();
    if (_isOnDisk) {
      hnsw.put("memory", Code.text("cold"));
    }
    boolean _isEmpty = hnsw.entries().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      params.put("hnsw_config", hnsw);
    }
    this.note(index, "searchCandidates", "centroids", "accelerator", "online");
    Quantization _quantization = index.getQuantization();
    boolean _tripleNotEquals = (_quantization != null);
    if (_tripleNotEquals) {
      final Code.Entries config = this.quantizationConfig(index.getQuantization());
      if ((config != null)) {
        params.put("quantization_config", config);
      }
    }
    Set<String> _keySet = ModelUtil.extParams(index, QdrantGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  public Code.Entries quantizationConfig(final Quantization q) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(q, "truncateDimension");
      if (_written) {
        throw Unsupported.of("truncate-dim; Qdrant quantizes the full vector");
      }
      this.note(q, "rescore", "oversampling", "keepOriginals");
      Code.Entries _switchResult = null;
      EQuantizationKind _type = q.getType();
      if (_type != null) {
        switch (_type) {
          case NONE:
            _switchResult = null;
            break;
          case SCALAR:
            Code.Entries _xblockexpression_1 = null;
            {
              if ((ModelUtil.written(q, "bits") && (q.getBits() != 8))) {
                double _bits = q.getBits();
                String _plus = ("scalar quantization with " + Double.valueOf(_bits));
                String _plus_1 = (_plus + " bits; Qdrant scalar quantization is int8");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = QdrantSchema.kw("scalar", QdrantSchema.kw("type", Code.text("int8")));
            }
            _switchResult = _xblockexpression_1;
            break;
          case BINARY:
            if (((!ModelUtil.written(q, "bits")) || (q.getBits() == 1))) {
              return QdrantSchema.kw("binary", QdrantSchema.kw());
            }
            double _bits = q.getBits();
            boolean _equals = (_bits == 1.5);
            if (_equals) {
              return QdrantSchema.kw("binary", QdrantSchema.kw("encoding", Code.text("one_and_half_bits")));
            }
            double _bits_1 = q.getBits();
            boolean _equals_1 = (_bits_1 == 2);
            if (_equals_1) {
              return QdrantSchema.kw("binary", QdrantSchema.kw("encoding", Code.text("two_bits")));
            }
            double _bits_2 = q.getBits();
            String _plus = ("binary quantization with " + Double.valueOf(_bits_2));
            String _plus_1 = (_plus + " bits; Qdrant encodes 1, 1.5 or 2 bits");
            throw Unsupported.of(_plus_1);
          case PRODUCT:
            throw Unsupported.of("product quantization with sub-vectors; Qdrant sets product quantization by compression ratio");
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Payload indexes in creation order: the primary key, then each field's declared or implied index.
   */
  private List<Code.Expr> payloadIndexes(final VectorCollection c) {
    ArrayList<Code.Expr> _xblockexpression = null;
    {
      final Map<String, Code.Expr> result = new LinkedHashMap<String, Code.Expr>();
      final List<PayloadIndex> declared = IterableExtensions.<PayloadIndex>toList(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class));
      Field _primaryKey = c.getPrimaryKey();
      final PayloadField key = ((PayloadField) _primaryKey);
      final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
        return Boolean.valueOf(((it.getTarget().getField() == key) && it.getTarget().getSubFields().isEmpty()));
      };
      boolean _exists = IterableExtensions.<PayloadIndex>exists(declared, _function);
      boolean _not = (!_exists);
      if (_not) {
        String _xifexpression = null;
        String _keyType = QdrantSchema.keyType(c);
        boolean _equals = Objects.equals(_keyType, "int");
        if (_equals) {
          _xifexpression = "integer";
        } else {
          _xifexpression = "keyword";
        }
        result.put(ModelUtil.fieldName(key), this.indexEntry(ModelUtil.fieldName(key), QdrantSchema.kw("type", Code.text(_xifexpression))));
      }
      final List<String> slices = CollectionLiterals.<String>newArrayList();
      if (((ModelUtil.partitionKey(c) != null) || (!c.getTenants().isEmpty()))) {
        String _sliceKey = QdrantSchema.sliceKey(c, false);
        slices.add(_sliceKey);
      }
      if (((!c.getNamespaces().isEmpty()) && (!slices.contains(QdrantSchema.sliceKey(c, true))))) {
        String _sliceKey_1 = QdrantSchema.sliceKey(c, true);
        slices.add(_sliceKey_1);
      }
      for (final String slice : slices) {
        {
          final Function1<PayloadIndex, Boolean> _function_1 = (PayloadIndex it) -> {
            return Boolean.valueOf((((it.getTarget().getField() != null) && it.getTarget().getSubFields().isEmpty()) && Objects.equals(QdrantSchema.fieldKey(it.getTarget().getField()), slice)));
          };
          boolean _exists_1 = IterableExtensions.<PayloadIndex>exists(declared, _function_1);
          if (_exists_1) {
            throw Unsupported.of((("a payload index on partition key " + slice) + "; Qdrant gives a tenant key its own tenant index"));
          }
          result.put(slice, this.indexEntry(slice, QdrantSchema.sliceIndex()));
        }
      }
      final List<PayloadField> searched = this.fullTextFields(c);
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          this.checkPayloadField(field, searched.contains(field));
          final Function1<PayloadIndex, Boolean> _function_1 = (PayloadIndex it) -> {
            return Boolean.valueOf((((it.getTarget().getField() == field) && it.getTarget().getSubFields().isEmpty()) && (it.getTarget().getRawPath() == null)));
          };
          final List<PayloadIndex> own = IterableExtensions.<PayloadIndex>toList(IterableExtensions.<PayloadIndex>filter(declared, _function_1));
          if ((searched.contains(field) && (!IterableExtensions.<PayloadIndex>exists(own, ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
            EPayloadIndexKind _kind = it.getKind();
            return Boolean.valueOf(Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT));
          }))))) {
            boolean _isEmpty = own.isEmpty();
            boolean _not_1 = (!_isEmpty);
            if (_not_1) {
              String _literal = IterableExtensions.<PayloadIndex>head(own).getKind().getLiteral();
              String _plus = ("a " + _literal);
              String _plus_1 = (_plus + " index on full-text field ");
              String _fieldName = ModelUtil.fieldName(field);
              String _plus_2 = (_plus_1 + _fieldName);
              String _plus_3 = (_plus_2 + "; a Qdrant payload field has one index");
              throw Unsupported.of(_plus_3);
            }
            result.put(QdrantSchema.fieldKey(field), this.indexEntry(QdrantSchema.fieldKey(field), this.textIndex(c, field, null)));
          }
        }
      }
      for (final PayloadIndex index : declared) {
        {
          final String path = QdrantSchema.indexPath(index.getTarget());
          boolean _containsKey = result.containsKey(path);
          if (_containsKey) {
            throw Unsupported.of((("two payload indexes on " + path) + "; a Qdrant payload field has one index"));
          }
          result.put(path, this.indexEntry(path, this.payloadIndex(c, index)));
        }
      }
      _xblockexpression = CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr[])Conversions.unwrapArray(result.values(), Code.Expr.class)));
    }
    return _xblockexpression;
  }

  private Code.Entries indexEntry(final String path, final Code.Entries schema) {
    return QdrantSchema.kw("field", Code.text(path), "schema", schema);
  }

  /**
   * Index path of a field path: sub-fields joined by '.', array-of-object segments marked with '[]'.
   */
  public static String indexPath(final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        return path.getRawPath();
      }
      final ArrayList<Field> segments = CollectionLiterals.<Field>newArrayList(path.getField());
      EList<Field> _subFields = path.getSubFields();
      Iterables.<Field>addAll(segments, _subFields);
      final List<String> names = CollectionLiterals.<String>newArrayList();
      for (int i = 0; (i < segments.size()); i++) {
        {
          final Field segment = segments.get(i);
          final boolean array = ((segment instanceof PayloadField) && Objects.equals(((PayloadField) segment).getType(), EScalarDType.ARRAY));
          String _fieldKey = QdrantSchema.fieldKey(segment);
          String _xifexpression = null;
          if ((array && (i < (segments.size() - 1)))) {
            _xifexpression = "[]";
          } else {
            _xifexpression = "";
          }
          String _plus = (_fieldKey + _xifexpression);
          names.add(_plus);
        }
      }
      _xblockexpression = String.join(".", names);
    }
    return _xblockexpression;
  }

  /**
   * The index schema of a declared payload index (create collection or create payload-index).
   */
  public Code.Entries payloadIndex(final VectorCollection c, final PayloadIndex index) {
    Code.Entries _xblockexpression = null;
    {
      this.note(index, "castType");
      if ((ModelUtil.written(index, "structure") && (!Objects.equals(index.getStructure(), EPayloadIndexStructure.AUTO)))) {
        this.notes.add("structure on PayloadIndex");
      }
      final Field field = ModelUtil.leaf(index.getTarget());
      Code.Entries _switchResult = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case KEYWORD:
            Code.Entries _xblockexpression_1 = null;
            {
              final Code.Entries keyword = QdrantSchema.kw("type", Code.text("keyword"));
              boolean _isPrefixMatch = index.isPrefixMatch();
              if (_isPrefixMatch) {
                keyword.put("prefix", Code.flag(true));
              }
              _xblockexpression_1 = keyword;
            }
            _switchResult = _xblockexpression_1;
            break;
          case FULL_TEXT:
            Code.Entries _xblockexpression_2 = null;
            {
              if (((field != null) && (!((field instanceof PayloadField) && QdrantSchema.TEXTUAL.contains(((PayloadField) field).getType()))))) {
                String _fieldName = ModelUtil.fieldName(field);
                String _plus = ("a full_text index on " + _fieldName);
                String _plus_1 = (_plus + ", which is not a text field");
                throw Unsupported.of(_plus_1);
              }
              PayloadField _xifexpression = null;
              if ((field instanceof PayloadField)) {
                _xifexpression = ((PayloadField) field);
              } else {
                _xifexpression = null;
              }
              _xblockexpression_2 = this.textIndex(c, _xifexpression, index);
            }
            _switchResult = _xblockexpression_2;
            break;
          case INTEGER:
            _switchResult = QdrantSchema.kw("type", Code.text("integer"));
            break;
          case FLOAT:
            _switchResult = QdrantSchema.kw("type", Code.text("float"));
            break;
          case DATETIME:
            _switchResult = QdrantSchema.kw("type", Code.text("datetime"));
            break;
          case BOOL:
            _switchResult = QdrantSchema.kw("type", Code.text("bool"));
            break;
          case UUID:
            _switchResult = QdrantSchema.kw("type", Code.text("uuid"));
            break;
          case GEO:
            _switchResult = QdrantSchema.kw("type", Code.text("geo"));
            break;
          case JSON:
            throw Unsupported.of("json payload indexes; Qdrant indexes typed paths (keyword, integer, float, ...)");
          default:
            break;
        }
      }
      final Code.Entries schema = _switchResult;
      EPayloadIndexKind _kind_1 = index.getKind();
      boolean _notEquals = (!Objects.equals(_kind_1, EPayloadIndexKind.FULL_TEXT));
      if (_notEquals) {
        this.note(index, "lowercase", "minTokenLength", "maxTokenLength", "analyzer");
      }
      EPayloadIndexKind _kind_2 = index.getKind();
      boolean _notEquals_1 = (!Objects.equals(_kind_2, EPayloadIndexKind.KEYWORD));
      if (_notEquals_1) {
        this.note(index, "prefixMatch");
      }
      boolean _isOnDisk = index.isOnDisk();
      if (_isOnDisk) {
        schema.put("memory", Code.text("cold"));
      }
      Set<String> _keySet = ModelUtil.extParams(index, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = schema;
    }
    return _xblockexpression;
  }

  /**
   * A text index scored by BM25, with the tokenizer, token filters and stop words of the field's analyzer.
   */
  public Code.Entries textIndex(final VectorCollection c, final PayloadField field, final PayloadIndex index) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries params = QdrantSchema.kw("type", Code.text("text"), "tokenizer", Code.text("word"));
      Analyzer _xifexpression = null;
      if (((index != null) && (index.getAnalyzer() != null))) {
        _xifexpression = index.getAnalyzer();
      } else {
        Analyzer _xifexpression_1 = null;
        if ((field != null)) {
          _xifexpression_1 = this.analyzerOf(c, field);
        }
        _xifexpression = _xifexpression_1;
      }
      final Analyzer analyzer = _xifexpression;
      if ((analyzer != null)) {
        this.analyze(analyzer, params);
      }
      if ((index != null)) {
        boolean _isLowercase = index.isLowercase();
        if (_isLowercase) {
          params.put("lowercase", Code.flag(true));
        }
        boolean _written = ModelUtil.written(index, "minTokenLength");
        if (_written) {
          params.put("min_token_len", Code.num(index.getMinTokenLength()));
        }
        boolean _written_1 = ModelUtil.written(index, "maxTokenLength");
        if (_written_1) {
          params.put("max_token_len", Code.num(index.getMaxTokenLength()));
        }
      }
      params.put("phrase_matching", Code.flag(true));
      params.put("scoring", QdrantSchema.kw("type", Code.text("bm25")));
      _xblockexpression = params;
    }
    return _xblockexpression;
  }

  public void analyze(final Analyzer analyzer, final Code.Entries params) {
    boolean _isEmpty = analyzer.getResources().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("analyzer resources");
    }
    ETokenizer _tokenizer = analyzer.getTokenizer();
    if (_tokenizer != null) {
      switch (_tokenizer) {
        case STANDARD:
          break;
        case WHITESPACE:
          params.put("tokenizer", Code.text("whitespace"));
          break;
        case EDGE_NGRAM:
          params.put("tokenizer", Code.text("prefix"));
          break;
        case ICU:
        case LANGUAGE:
          params.put("tokenizer", Code.text("multilingual"));
          break;
        default:
          String _literal = analyzer.getTokenizer().getLiteral();
          String _plus = (_literal + " tokenizer; Qdrant tokenizes by word, whitespace, prefix, multilingual or code");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = analyzer.getTokenizer().getLiteral();
      String _plus = (_literal + " tokenizer; Qdrant tokenizes by word, whitespace, prefix, multilingual or code");
      throw Unsupported.of(_plus);
    }
    final Function1<AnalyzerFilter, EAnalyzerFilterType> _function = (AnalyzerFilter it) -> {
      return it.getType();
    };
    final Set<EAnalyzerFilterType> filters = IterableExtensions.<EAnalyzerFilterType>toSet(ListExtensions.<AnalyzerFilter, EAnalyzerFilterType>map(analyzer.getFilters(), _function));
    for (final EAnalyzerFilterType filter : filters) {
      boolean _contains = Collections.<EAnalyzerFilterType>unmodifiableList(CollectionLiterals.<EAnalyzerFilterType>newArrayList(EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STOP, EAnalyzerFilterType.STEMMER, EAnalyzerFilterType.ASCII_FOLDING)).contains(filter);
      boolean _not_1 = (!_contains);
      if (_not_1) {
        String _literal_1 = filter.getLiteral();
        String _plus_1 = (_literal_1 + " token filters");
        throw Unsupported.of(_plus_1);
      }
    }
    boolean _contains_1 = filters.contains(EAnalyzerFilterType.LOWERCASE);
    if (_contains_1) {
      params.put("lowercase", Code.flag(true));
    }
    boolean _contains_2 = filters.contains(EAnalyzerFilterType.ASCII_FOLDING);
    if (_contains_2) {
      params.put("ascii_folding", Code.flag(true));
    }
    final boolean stop = (filters.contains(EAnalyzerFilterType.STOP) || ((analyzer.getStopwords() != null) && (!Objects.equals(analyzer.getStopwords().toLowerCase(), "none"))));
    if (stop) {
      String _elvis = null;
      String _stopwords = analyzer.getStopwords();
      if (_stopwords != null) {
        _elvis = _stopwords;
      } else {
        String _language = analyzer.getLanguage();
        _elvis = _language;
      }
      final String language = this.languageName(_elvis, "stop words");
      boolean _contains_3 = QdrantSchema.STOPWORD_LANGUAGES.contains(language);
      boolean _not_2 = (!_contains_3);
      if (_not_2) {
        throw Unsupported.of((("stop words for \"" + language) + "\"; Qdrant has no such stop word list"));
      }
      params.put("stopwords", Code.text(language));
    }
    boolean _contains_4 = filters.contains(EAnalyzerFilterType.STEMMER);
    if (_contains_4) {
      final String language_1 = this.languageName(analyzer.getLanguage(), "a stemmer");
      boolean _contains_5 = QdrantSchema.STEMMER_LANGUAGES.contains(language_1);
      boolean _not_3 = (!_contains_5);
      if (_not_3) {
        throw Unsupported.of((("stemming for \"" + language_1) + "\"; Qdrant has no such Snowball stemmer"));
      }
      params.put("stemmer", QdrantSchema.kw("type", Code.text("snowball"), "language", Code.text(language_1)));
    }
    if ((((analyzer.getLanguage() != null) && (!stop)) && (!filters.contains(EAnalyzerFilterType.STEMMER)))) {
      String _name = analyzer.getName();
      String _plus_2 = ("language on Analyzer " + _name);
      this.notes.add(_plus_2);
    }
  }

  private String languageName(final String name, final String purpose) {
    String _xblockexpression = null;
    {
      if ((name == null)) {
        throw Unsupported.of((purpose + " without a language"));
      }
      final String lower = name.toLowerCase();
      _xblockexpression = QdrantSchema.LANGUAGES.getOrDefault(lower, lower);
    }
    return _xblockexpression;
  }

  /**
   * Checks a payload field against the JSON values Qdrant payloads hold.
   */
  public void checkPayloadField(final PayloadField field, final boolean searched) {
    final String name = QdrantSchema.fieldKey(field);
    if ((field.isPartitionKey() && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(field.getType())))) {
      String _literal = field.getType().getLiteral();
      String _plus = ((("partition key " + name) + " of type ") + _literal);
      String _plus_1 = (_plus + "; Qdrant tenant indexes are keyword indexes, and tenant names are text");
      throw Unsupported.of(_plus_1);
    }
    if ((searched && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(field.getType())))) {
      String _literal_1 = field.getType().getLiteral();
      String _plus_2 = ("full-text search on " + _literal_1);
      String _plus_3 = (_plus_2 + " field ");
      String _plus_4 = (_plus_3 + name);
      throw Unsupported.of(_plus_4);
    }
    EScalarDType _xifexpression = null;
    if ((Objects.equals(field.getType(), EScalarDType.ARRAY) && ModelUtil.written(field, "elementType"))) {
      _xifexpression = field.getElementType();
    } else {
      _xifexpression = field.getType();
    }
    final EScalarDType type = _xifexpression;
    if ((Objects.equals(field.getType(), EScalarDType.ARRAY) && (!ModelUtil.written(field, "elementType")))) {
      throw Unsupported.of((("array field " + name) + " without element-type"));
    }
    if (((((((!QdrantSchema.NUMERIC.contains(type)) && (!QdrantSchema.TEXTUAL.contains(type))) && (!QdrantSchema.TEMPORAL.contains(type))) && (!QdrantSchema.STRUCTURED.contains(type))) && (!Objects.equals(type, EScalarDType.BOOL))) && (!Objects.equals(type, EScalarDType.GEO_POINT)))) {
      String _literal_2 = type.getLiteral();
      String _plus_5 = (_literal_2 + " field ");
      String _plus_6 = (_plus_5 + name);
      String _plus_7 = (_plus_6 + "; Qdrant payloads hold JSON numbers, strings, booleans, objects and arrays");
      throw Unsupported.of(_plus_7);
    }
    EList<Field> _subFields = field.getSubFields();
    for (final Field sub : _subFields) {
      if ((sub instanceof PayloadField)) {
        this.checkPayloadField(((PayloadField) sub), false);
      } else {
        String _fieldName = ModelUtil.fieldName(sub);
        String _plus_8 = ("vector sub-field " + _fieldName);
        throw Unsupported.of(_plus_8);
      }
    }
    this.note(field, "nested", "documentBody");
  }

  /**
   * Text fields that feed a bm25 function; Qdrant scores them with BM25 through their text index.
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
              throw Unsupported.of("text-embedding functions (Qdrant Cloud Inference is not generated)");
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

  private Code.Entries limits(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          final Code.Entries limit = QdrantSchema.kw();
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

  /**
   * Collection extensions: strict_mode (ExtParam) and the segment optimizer settings.
   */
  private void collectionExtensions(final VectorCollection c, final Code.Entries args) {
    Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(c, QdrantGenerator.VENDOR).entrySet();
    for (final Map.Entry<String, Literal> entry : _entrySet) {
      String _key = entry.getKey();
      if (_key != null) {
        switch (_key) {
          case "strict_mode":
            args.put("strict_mode", Literals.of(entry.getValue()));
            break;
          default:
            String _key_1 = entry.getKey();
            String _plus = ("extension parameter " + _key_1);
            throw Unsupported.of(_plus);
        }
      } else {
        String _key_1 = entry.getKey();
        String _plus = ("extension parameter " + _key_1);
        throw Unsupported.of(_plus);
      }
    }
    Iterable<SegmentOptimizers> _filter = Iterables.<SegmentOptimizers>filter(QdrantSchema.extensionItems(c), SegmentOptimizers.class);
    for (final SegmentOptimizers optimizers : _filter) {
      {
        final Code.Entries config = Code.entries();
        Set<Map.Entry<String, String>> _entrySet_1 = QdrantSchema.OPTIMIZERS.entrySet();
        for (final Map.Entry<String, String> feature : _entrySet_1) {
          boolean _written = ModelUtil.written(optimizers, feature.getKey());
          if (_written) {
            Object _eGet = optimizers.eGet(optimizers.eClass().getEStructuralFeature(feature.getKey()));
            config.put(feature.getValue(), Code.num(((Integer) _eGet).doubleValue()));
          }
        }
        args.put("optimizers", config);
      }
    }
    final Function1<ExtensionItem, Boolean> _function = (ExtensionItem it) -> {
      return Boolean.valueOf(((!(it instanceof SegmentOptimizers)) && (!(it instanceof ExtParam))));
    };
    Iterable<ExtensionItem> _filter_1 = IterableExtensions.<ExtensionItem>filter(QdrantSchema.extensionItems(c), _function);
    for (final ExtensionItem item : _filter_1) {
      String _name = item.eClass().getName();
      String _plus_1 = (_name + " on a collection");
      throw Unsupported.of(_plus_1);
    }
  }

  /**
   * Items of the host's qdrant extension blocks.
   */
  public static List<ExtensionItem> extensionItems(final EObject host) {
    List<ExtensionItem> _xblockexpression = null;
    {
      final EStructuralFeature feature = host.eClass().getEStructuralFeature("extensions");
      final List<ExtensionItem> result = CollectionLiterals.<ExtensionItem>newArrayList();
      if ((feature == null)) {
        return result;
      }
      Object _eGet = host.eGet(feature);
      Iterable<VendorExtension> _filter = Iterables.<VendorExtension>filter(((List<?>) _eGet), VendorExtension.class);
      for (final VendorExtension block : _filter) {
        String _vendor = block.getVendor();
        boolean _equals = Objects.equals(_vendor, QdrantGenerator.VENDOR);
        if (_equals) {
          result.addAll(block.getItems());
        }
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

  public String distance(final EMetric metric) {
    String _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = "Cosine";
          break;
        case DOT:
          _switchResult = "Dot";
          break;
        case EUCLIDEAN:
          _switchResult = "Euclid";
          break;
        case MANHATTAN:
          _switchResult = "Manhattan";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric; Qdrant scores by Cosine, Dot, Euclid or Manhattan");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric; Qdrant scores by Cosine, Dot, Euclid or Manhattan");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * Qdrant read consistency of a vecDSL level: strong reads every replica; eventual is the default.
   */
  public String readConsistency(final EConsistencyLevel level) {
    String _switchResult = null;
    if (level != null) {
      switch (level) {
        case STRONG:
          _switchResult = "all";
          break;
        case EVENTUAL:
          _switchResult = null;
          break;
        default:
          String _literal = level.getLiteral();
          String _plus = ("consistency " + _literal);
          String _plus_1 = (_plus + "; Qdrant reads at a replica count (one, majority, quorum or all)");
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal = level.getLiteral();
      String _plus = ("consistency " + _literal);
      String _plus_1 = (_plus + "; Qdrant reads at a replica count (one, majority, quorum or all)");
      throw Unsupported.of(_plus_1);
    }
    return _switchResult;
  }

  public Map<String, Code.Expr> extValues(final EObject host) {
    LinkedHashMap<String, Code.Expr> _xblockexpression = null;
    {
      final LinkedHashMap<String, Code.Expr> values = new LinkedHashMap<String, Code.Expr>();
      if ((host != null)) {
        Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, QdrantGenerator.VENDOR).entrySet();
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
