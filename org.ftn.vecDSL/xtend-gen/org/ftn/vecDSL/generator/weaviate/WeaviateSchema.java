package org.ftn.vecDSL.generator.weaviate;

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
import org.eclipse.xtext.EcoreUtil2;
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
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.Cluster;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType;
import org.ftn.vecDSL.vecDSL.ECollectionLimit;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPartitionStrategy;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EQuantizationKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EStorageType;
import org.ftn.vecDSL.vecDSL.ETokenizer;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.GenerativeModel;
import org.ftn.vecDSL.vecDSL.InvertedIndexSettings;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.ReplicationConfig;
import org.ftn.vecDSL.vecDSL.Reranker;
import org.ftn.vecDSL.vecDSL.ShardConfig;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps vecDSL collections to Weaviate collection definitions: properties, self-provided named vectors and the
 * inverted index. Weaviate object ids are UUIDs and 'id' is a reserved property name, so the primary key is
 * stored as a property (renamed when reserved) and the object UUID is derived from it by the runtime.
 */
@SuppressWarnings("all")
public class WeaviateSchema {
  private static final Set<String> RESERVED = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("id", "_id", "_additional"));

  private static final List<String> RESERVED_SUFFIXES = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("_searchable", "_rangeable", "_temp", "__meta_count", "_propertyLength", "_nullState"));

  public static final String PARTITIONS = "partitions; Weaviate places shards by object id, so they cannot be named, and a request on a multi-tenant collection targets one tenant";

  public static final String EXTERNAL = "collections derived from external sources; Weaviate stores the objects of every collection itself";

  private static final String TENANT_NAME = "[A-Za-z0-9_-]{1,64}";

  private static final Map<String, String> VECTORIZERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("aws", "text2vec-aws"), Pair.<String, String>of("cohere", "text2vec-cohere"), Pair.<String, String>of("databricks", "text2vec-databricks"), Pair.<String, String>of("google", "text2vec-google"), Pair.<String, String>of("huggingface", "text2vec-huggingface"), Pair.<String, String>of("jinaai", "text2vec-jinaai"), Pair.<String, String>of("mistral", "text2vec-mistral"), Pair.<String, String>of("model2vec", "text2vec-model2vec"), Pair.<String, String>of("nvidia", "text2vec-nvidia"), Pair.<String, String>of("ollama", "text2vec-ollama"), Pair.<String, String>of("openai", "text2vec-openai"), Pair.<String, String>of("transformers", "text2vec-transformers"), Pair.<String, String>of("voyageai", "text2vec-voyageai"), Pair.<String, String>of("weaviate", "text2vec-weaviate")));

  private static final Map<String, String> GENERATORS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("anthropic", "generative-anthropic"), Pair.<String, String>of("aws", "generative-aws"), Pair.<String, String>of("cohere", "generative-cohere"), Pair.<String, String>of("databricks", "generative-databricks"), Pair.<String, String>of("deepseek", "generative-deepseek"), Pair.<String, String>of("dummy", "generative-dummy"), Pair.<String, String>of("google", "generative-google"), Pair.<String, String>of("mistral", "generative-mistral"), Pair.<String, String>of("nvidia", "generative-nvidia"), Pair.<String, String>of("ollama", "generative-ollama"), Pair.<String, String>of("openai", "generative-openai"), Pair.<String, String>of("xai", "generative-xai")));

  private static final Map<String, String> RERANKERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("cohere", "reranker-cohere"), Pair.<String, String>of("dummy", "reranker-dummy"), Pair.<String, String>of("jinaai", "reranker-jinaai"), Pair.<String, String>of("nvidia", "reranker-nvidia"), Pair.<String, String>of("transformers", "reranker-transformers"), Pair.<String, String>of("voyageai", "reranker-voyageai")));

  private final List<String> notes;

  public WeaviateSchema(final List<String> notes) {
    this.notes = notes;
  }

  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(WeaviateSchema.EXTERNAL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id has no Weaviate mapping yet");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref has no Weaviate mapping yet");
      }
      if (((!c.getPartitions().isEmpty()) || (c.getDefaultPartition() != null))) {
        throw Unsupported.of(WeaviateSchema.PARTITIONS);
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of("deletion-protection; Weaviate has no setting that blocks dropping a collection");
      }
      this.note(c, "idType", "tags", "storedSourceFields");
      final Function1<PayloadField, Code.Entries> _function = (PayloadField it) -> {
        return this.property(c, it, false);
      };
      final Function1<VectorField, Code.Entries> _function_1 = (VectorField it) -> {
        return this.vector(c, it);
      };
      final Code.Entries args = WeaviateSchema.kw(
        "name", Code.text(WeaviateSchema.className(c)), 
        "properties", Code.items(ListExtensions.<PayloadField, Code.Entries>map(ModelUtil.payloadFields(c), _function)), 
        "vectors", Code.items(ListExtensions.<VectorField, Code.Entries>map(this.storedVectors(c), _function_1)));
      final Code.Entries inverted = this.invertedIndex(c);
      boolean _isEmpty_1 = inverted.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("inverted_index", inverted);
      }
      String _description = c.getDescription();
      boolean _tripleNotEquals_1 = (_description != null);
      if (_tripleNotEquals_1) {
        args.put("description", Code.text(c.getDescription()));
      }
      this.collectionSettings(c, args);
      final List<Code.Stmt> body = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _runtime = Code.runtime("create_collection", args);
      Code.Run _run = new Code.Run(_runtime);
      body.add(_run);
      final List<String> tenants = this.tenantNames(c);
      boolean _isEmpty_2 = tenants.isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        Code.Call _runtime_1 = Code.runtime("create_tenants", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(c)), "tenants", Code.texts(tenants)));
        Code.Run _run_1 = new Code.Run(_runtime_1);
        body.add(_run_1);
      }
      EList<Alias> _aliases = c.getAliases();
      for (final Alias alias : _aliases) {
        Code.Call _runtime_2 = Code.runtime("create_alias", WeaviateSchema.kw("alias", Code.text(WeaviateSchema.className(alias.getName())), "collection", Code.text(WeaviateSchema.className(c))));
        Code.Run _run_2 = new Code.Run(_runtime_2);
        body.add(_run_2);
      }
      if ((!ifNotExists)) {
        return body;
      }
      Code.Call _runtime_3 = Code.runtime("has_collection", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(c))));
      Code.When _when = new Code.When(_runtime_3, true, body);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Settings beyond the schema, added only when the collection declares them.
   */
  private void collectionSettings(final VectorCollection c, final Code.Entries args) {
    boolean _multiTenant = this.multiTenant(c);
    if (_multiTenant) {
      final Code.Entries tenancy = WeaviateSchema.kw("enabled", Code.flag(true));
      boolean _isEmpty = c.getNamespaces().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        tenancy.put("auto_tenant_creation", Code.flag(true));
      }
      args.put("multi_tenancy", tenancy);
    }
    if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
      Field _ttlField = c.getTtlField();
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(c, "ttlSeconds");
      if (_written) {
        _xifexpression = Integer.valueOf(c.getTtlSeconds());
      } else {
        _xifexpression = null;
      }
      args.put("ttl", this.ttl(c, _ttlField, _xifexpression));
    }
    Cluster _elvis = null;
    Cluster _cluster = c.getCluster();
    if (_cluster != null) {
      _elvis = _cluster;
    } else {
      Cluster _containerOfType = EcoreUtil2.<Cluster>getContainerOfType(c, Cluster.class);
      _elvis = _containerOfType;
    }
    final Cluster cluster = _elvis;
    if ((cluster != null)) {
      boolean _isEmpty_1 = cluster.getLimits().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of("cluster limits; Weaviate sets them only through server environment variables");
      }
      ReplicationConfig _replication = cluster.getReplication();
      boolean _tripleNotEquals = (_replication != null);
      if (_tripleNotEquals) {
        args.put("replication", this.replication(cluster.getReplication()));
      }
      ShardConfig _sharding = cluster.getSharding();
      boolean _tripleNotEquals_1 = (_sharding != null);
      if (_tripleNotEquals_1) {
        args.put("sharding", this.sharding(cluster.getSharding()));
      }
      boolean _isEmpty_2 = this.extValues(cluster).isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        throw Unsupported.of("extension parameters on a cluster");
      }
    }
    StorageConfig _storage = c.getStorage();
    boolean _tripleNotEquals_2 = (_storage != null);
    if (_tripleNotEquals_2) {
      this.storage(c.getStorage());
    }
    EList<CollectionLimit> _limits = c.getLimits();
    for (final CollectionLimit limit : _limits) {
      {
        ECollectionLimit _type = limit.getType();
        boolean _notEquals = (!Objects.equals(_type, ECollectionLimit.MAX_VECTOR_DIM));
        if (_notEquals) {
          String _literal = limit.getType().getLiteral();
          String _plus = ("collection-limit " + _literal);
          String _plus_1 = (_plus + "; Weaviate has no per-collection capacity limits");
          throw Unsupported.of(_plus_1);
        }
        List<VectorField> _storedVectors = this.storedVectors(c);
        for (final VectorField field : _storedVectors) {
          if (((field.getDimension() <= 0) || (field.getDimension() > limit.getValue()))) {
            String _fieldName = ModelUtil.fieldName(field);
            String _plus_2 = ("vector field " + _fieldName);
            String _plus_3 = (_plus_2 + " without a dimension within max_vector_dim ");
            int _value = limit.getValue();
            String _plus_4 = (_plus_3 + Integer.valueOf(_value));
            String _plus_5 = (_plus_4 + "; Weaviate checks no dimension limit, the runtime checks the declared dimension");
            throw Unsupported.of(_plus_5);
          }
        }
      }
    }
    GenerativeModel _elvis_1 = null;
    GenerativeModel _generativeModel = c.getGenerativeModel();
    if (_generativeModel != null) {
      _elvis_1 = _generativeModel;
    } else {
      GenerativeModel _head = IterableExtensions.<GenerativeModel>head(c.getGenerativeModels());
      _elvis_1 = _head;
    }
    final GenerativeModel generative = _elvis_1;
    if (((c.getGenerativeModels().size() > 1) || (((c.getGenerativeModel() != null) && (!c.getGenerativeModels().isEmpty())) && (!c.getGenerativeModels().contains(c.getGenerativeModel()))))) {
      throw Unsupported.of("several generative models on one collection; Weaviate configures one generative module per collection");
    }
    if ((generative != null)) {
      args.put("generative", this.generativeModule(generative));
    }
  }

  public boolean multiTenant(final VectorCollection c) {
    return ((!c.getTenants().isEmpty()) || (!c.getNamespaces().isEmpty()));
  }

  /**
   * Weaviate tenants declared by a collection: its tenants and its namespaces (by key).
   */
  public List<String> tenantNames(final VectorCollection c) {
    List<String> _xblockexpression = null;
    {
      final List<String> names = CollectionLiterals.<String>newArrayList();
      EList<Tenant> _tenants = c.getTenants();
      for (final Tenant tenant : _tenants) {
        {
          this.note(tenant, "extensions");
          String _tenantName = WeaviateSchema.tenantName(tenant.getName());
          names.add(_tenantName);
        }
      }
      EList<Namespace> _namespaces = c.getNamespaces();
      for (final Namespace namespace : _namespaces) {
        String _tenantName = WeaviateSchema.tenantName(namespace.getKey());
        names.add(_tenantName);
      }
      _xblockexpression = names;
    }
    return _xblockexpression;
  }

  public static String tenantName(final String name) {
    String _xblockexpression = null;
    {
      boolean _matches = name.matches(WeaviateSchema.TENANT_NAME);
      boolean _not = (!_matches);
      if (_not) {
        throw Unsupported.of((("tenant name \'" + name) + "\'; Weaviate tenant names have 1 to 64 letters, digits, \'-\' or \'_\'"));
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  public Code.Entries ttl(final VectorCollection c, final Field field, final Integer seconds) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = WeaviateSchema.kw("filter_expired", Code.flag(true));
      if ((field != null)) {
        if (((!(field instanceof PayloadField)) || (!Objects.equals(this.dataType(c, ((PayloadField) field)), "date")))) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus = ("ttl-field " + _fieldName);
          String _plus_1 = (_plus + "; Weaviate measures object ttl from a date property");
          throw Unsupported.of(_plus_1);
        }
        result.put("date_property", Code.text(WeaviateSchema.propertyName(c, field)));
      }
      if ((seconds != null)) {
        result.put("seconds", Code.num((seconds).intValue()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Entries replication(final ReplicationConfig config) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isEmpty = this.extValues(config).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on replication");
      }
      _xblockexpression = WeaviateSchema.kw("factor", Code.num(config.getFactor()));
    }
    return _xblockexpression;
  }

  private Code.Entries sharding(final ShardConfig config) {
    Code.Entries _xblockexpression = null;
    {
      Field _key = config.getKey();
      boolean _tripleNotEquals = (_key != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("shard keys; Weaviate shards by object id");
      }
      if ((ModelUtil.written(config, "strategy") && (!Objects.equals(config.getStrategy(), EPartitionStrategy.HASH)))) {
        String _literal = config.getStrategy().getLiteral();
        String _plus = (_literal + " sharding; Weaviate shards by a hash of the object id");
        throw Unsupported.of(_plus);
      }
      boolean _isEmpty = this.extValues(config).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on sharding");
      }
      _xblockexpression = WeaviateSchema.kw("desired_count", Code.num(config.getShards()));
    }
    return _xblockexpression;
  }

  /**
   * Weaviate persists every collection on disk; no other storage medium is selectable per collection.
   */
  private void storage(final StorageConfig config) {
    EStorageType _type = config.getType();
    boolean _notEquals = (!Objects.equals(_type, EStorageType.DISK));
    if (_notEquals) {
      String _literal = config.getType().getLiteral();
      String _plus = ("storage " + _literal);
      String _plus_1 = (_plus + "; Weaviate persists every collection on disk");
      throw Unsupported.of(_plus_1);
    }
    String _location = config.getLocation();
    boolean _tripleNotEquals = (_location != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("storage location; Weaviate stores collections under its PERSISTENCE_DATA_PATH");
    }
    if ((config.isMmap() || config.isTiered())) {
      throw Unsupported.of("mmap and tiered storage per collection; Weaviate sets its storage strategy for the whole server");
    }
    boolean _isEmpty = this.extValues(config).isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("extension parameters on storage");
    }
  }

  public Code.Entries generativeModule(final GenerativeModel model) {
    Code.Entries _xblockexpression = null;
    {
      String _elvis = null;
      String _provider = model.getProvider();
      if (_provider != null) {
        _elvis = _provider;
      } else {
        _elvis = "";
      }
      final String module = WeaviateSchema.GENERATORS.get(_elvis.toLowerCase());
      if ((module == null)) {
        String _provider_1 = model.getProvider();
        String _plus = ("generative provider \'" + _provider_1);
        String _plus_1 = (_plus + "\'; Weaviate has no generative module for it");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries config = WeaviateSchema.kw();
      String _modelId = model.getModelId();
      boolean _tripleNotEquals = (_modelId != null);
      if (_tripleNotEquals) {
        config.put("model", Code.text(model.getModelId()));
      }
      boolean _written = ModelUtil.written(model, "temperature");
      if (_written) {
        config.put("temperature", Code.num(model.getTemperature()));
      }
      boolean _written_1 = ModelUtil.written(model, "maxTokens");
      if (_written_1) {
        config.put("maxTokens", Code.num(model.getMaxTokens()));
      }
      boolean _written_2 = ModelUtil.written(model, "topP");
      if (_written_2) {
        config.put("topP", Code.num(model.getTopP()));
      }
      this.putExtensions(model, config);
      _xblockexpression = WeaviateSchema.kw("module", Code.text(module), "config", config);
    }
    return _xblockexpression;
  }

  public Code.Entries rerankerModule(final Reranker reranker) {
    Code.Entries _xblockexpression = null;
    {
      String _elvis = null;
      String _provider = reranker.getProvider();
      if (_provider != null) {
        _elvis = _provider;
      } else {
        _elvis = "";
      }
      final String module = WeaviateSchema.RERANKERS.get(_elvis.toLowerCase());
      if ((module == null)) {
        String _provider_1 = reranker.getProvider();
        String _plus = ("reranker provider \'" + _provider_1);
        String _plus_1 = (_plus + "\'; Weaviate has no reranker module for it");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries config = WeaviateSchema.kw();
      String _modelId = reranker.getModelId();
      boolean _tripleNotEquals = (_modelId != null);
      if (_tripleNotEquals) {
        config.put("model", Code.text(reranker.getModelId()));
      }
      this.putExtensions(reranker, config);
      _xblockexpression = WeaviateSchema.kw("module", Code.text(module), "config", config);
    }
    return _xblockexpression;
  }

  public Code.Entries vectorizerModule(final EmbeddingModel model) {
    Code.Entries _xblockexpression = null;
    {
      if (((model.getLocalPath() != null) || model.isBuiltIn())) {
        throw Unsupported.of("built-in and local embedding models; Weaviate embeds through vectorizer modules");
      }
      if (((ModelUtil.written(model, "chunkStrategy") || ModelUtil.written(model, "chunkSize")) || ModelUtil.written(model, "chunkOverlap"))) {
        throw Unsupported.of("chunking; Weaviate vectorizes each object as a whole");
      }
      String _elvis = null;
      String _provider = model.getProvider();
      if (_provider != null) {
        _elvis = _provider;
      } else {
        _elvis = "";
      }
      final String module = WeaviateSchema.VECTORIZERS.get(_elvis.toLowerCase());
      if ((module == null)) {
        String _provider_1 = model.getProvider();
        String _plus = ("embedding provider \'" + _provider_1);
        String _plus_1 = (_plus + "\'; Weaviate has no vectorizer module for it");
        throw Unsupported.of(_plus_1);
      }
      this.note(model, "version", "deprecated", "replacedBy");
      final Code.Entries config = WeaviateSchema.kw();
      String _modelId = model.getModelId();
      boolean _tripleNotEquals = (_modelId != null);
      if (_tripleNotEquals) {
        config.put("model", Code.text(model.getModelId()));
      }
      boolean _written = ModelUtil.written(model, "dimension");
      if (_written) {
        config.put("dimensions", Code.num(model.getDimension()));
      }
      this.putExtensions(model, config);
      _xblockexpression = WeaviateSchema.kw("module", Code.text(module), "config", config);
    }
    return _xblockexpression;
  }

  /**
   * Module options from the Weaviate extension of a model concept, passed to the module unchanged.
   */
  private void putExtensions(final EObject model, final Code.Entries config) {
    Set<Map.Entry<String, Code.Expr>> _entrySet = this.extValues(model).entrySet();
    for (final Map.Entry<String, Code.Expr> entry : _entrySet) {
      config.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Collection identity every runtime call needs: Weaviate name, key field and the property that stores it.
   */
  public Code.Entries layout(final VectorCollection c, final String level) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(c)), "key", Code.text(ModelUtil.fieldName(c.getPrimaryKey())), "key_property", Code.text(WeaviateSchema.keyProperty(c)));
      String _elvis = null;
      if (level != null) {
        _elvis = level;
      } else {
        String _xifexpression = null;
        boolean _written = ModelUtil.written(c, "consistency");
        if (_written) {
          _xifexpression = this.consistency(c.getConsistency());
        } else {
          _xifexpression = null;
        }
        _elvis = _xifexpression;
      }
      final String effective = _elvis;
      if ((effective != null)) {
        args.put("consistency", Code.text(effective));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * The layout plus what writes need: vector fields, declared limits and default values.
   */
  public Code.Entries writeLayout(final VectorCollection c, final String level) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = this.layout(c, level);
      final Function1<VectorField, String> _function = (VectorField it) -> {
        return ModelUtil.fieldName(it);
      };
      args.put("vectors", Code.texts(ListExtensions.<VectorField, String>map(this.storedVectors(c), _function)));
      final Code.Entries declared = this.limits(c);
      boolean _isEmpty = declared.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("limits", declared);
      }
      final Code.Entries defaults = Code.entries();
      final Function1<PayloadField, Boolean> _function_1 = (PayloadField it) -> {
        Literal _defaultValue = it.getDefaultValue();
        return Boolean.valueOf((_defaultValue != null));
      };
      Iterable<PayloadField> _filter = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function_1);
      for (final PayloadField field : _filter) {
        defaults.put(ModelUtil.fieldName(field), Literals.of(field.getDefaultValue()));
      }
      boolean _isEmpty_1 = defaults.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("defaults", defaults);
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  public static String className(final VectorCollection c) {
    return WeaviateSchema.className(c.getName());
  }

  public static String className(final String name) {
    String _upperCase = name.substring(0, 1).toUpperCase();
    String _substring = name.substring(1);
    return (_upperCase + _substring);
  }

  public static String keyProperty(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(c.getPrimaryKey());
      String _xifexpression = null;
      boolean _contains = WeaviateSchema.RESERVED.contains(name);
      if (_contains) {
        _xifexpression = (name + "_key");
      } else {
        _xifexpression = name;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public static String propertyName(final VectorCollection c, final Field field) {
    String _xblockexpression = null;
    {
      Field _primaryKey = c.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        return WeaviateSchema.keyProperty(c);
      }
      _xblockexpression = WeaviateSchema.checkedName(ModelUtil.fieldName(field));
    }
    return _xblockexpression;
  }

  private static String checkedName(final String name) {
    String _xblockexpression = null;
    {
      boolean _contains = WeaviateSchema.RESERVED.contains(name);
      if (_contains) {
        throw Unsupported.of((("field name \'" + name) + "\', which Weaviate reserves"));
      }
      boolean _isUpperCase = Character.isUpperCase(name.charAt(0));
      if (_isUpperCase) {
        throw Unsupported.of((("field name \'" + name) + "\'; Weaviate property names start with a lower case letter"));
      }
      final Function1<String, Boolean> _function = (String it) -> {
        return Boolean.valueOf(name.endsWith(it));
      };
      final String suffix = IterableExtensions.<String>findFirst(WeaviateSchema.RESERVED_SUFFIXES, _function);
      if ((suffix != null)) {
        throw Unsupported.of(((("field name \'" + name) + "\'; Weaviate reserves the suffix ") + suffix));
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }

  /**
   * Vector fields stored as named vectors: all except the sparse outputs of bm25 functions.
   */
  public List<VectorField> storedVectors(final VectorCollection c) {
    final Function1<VectorField, Boolean> _function = (VectorField it) -> {
      boolean _isBm25Output = WeaviateSchema.isBm25Output(c, it);
      return Boolean.valueOf((!_isBm25Output));
    };
    return IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function));
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
   * Property definition of a payload field added to an existing collection.
   */
  public Code.Entries propertySpec(final VectorCollection c, final PayloadField field) {
    return this.property(c, field, false);
  }

  /**
   * Named-vector definition of a vector field added to an existing collection.
   */
  public Code.Entries vectorSpec(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isBm25Output = WeaviateSchema.isBm25Output(c, field);
      if (_isBm25Output) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("adding the bm25 output " + _fieldName);
        String _plus_1 = (_plus + "; Weaviate has no sparse vectors");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = this.vector(c, field);
    }
    return _xblockexpression;
  }

  public String tokenizationOf(final Analyzer analyzer) {
    return this.tokenization(analyzer);
  }

  private Code.Entries property(final VectorCollection c, final PayloadField field, final boolean nested) {
    Code.Entries _xblockexpression = null;
    {
      String _xifexpression = null;
      if (nested) {
        _xifexpression = WeaviateSchema.checkedName(ModelUtil.fieldName(field));
      } else {
        _xifexpression = WeaviateSchema.propertyName(c, field);
      }
      final String name = _xifexpression;
      boolean _isPartitionKey = field.isPartitionKey();
      if (_isPartitionKey) {
        throw Unsupported.of(WeaviateSchema.PARTITIONS);
      }
      final String type = this.dataType(c, field);
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(name), "data_type", Code.text(type));
      final boolean textual = (Objects.equals(type, "text") || Objects.equals(type, "text[]"));
      final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
        Field _field = it.getTarget().getField();
        return Boolean.valueOf((_field == field));
      };
      final List<PayloadIndex> indexes = IterableExtensions.<PayloadIndex>toList(IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function));
      for (final PayloadIndex index : indexes) {
        {
          if (((!index.getTarget().getSubFields().isEmpty()) || (index.getTarget().getRawPath() != null))) {
            throw Unsupported.of("payload indexes on nested paths");
          }
          this.note(index, "structure", "castType", "onDisk", "prefixMatch", "minTokenLength", "maxTokenLength");
        }
      }
      Analyzer _elvis = null;
      Analyzer _analyzer = field.getAnalyzer();
      if (_analyzer != null) {
        _elvis = _analyzer;
      } else {
        final Function1<PayloadIndex, Analyzer> _function_1 = (PayloadIndex it) -> {
          return it.getAnalyzer();
        };
        Analyzer _head = IterableExtensions.<Analyzer>head(IterableExtensions.<Analyzer>filterNull(ListExtensions.<PayloadIndex, Analyzer>map(indexes, _function_1)));
        _elvis = _head;
      }
      final Analyzer analyzer = _elvis;
      final boolean searchable = (((field.isFullText() || (analyzer != null)) || this.bm25Inputs(c).contains(field)) || IterableExtensions.<PayloadIndex>exists(indexes, ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
        EPayloadIndexKind _kind = it.getKind();
        return Boolean.valueOf(Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT));
      })));
      if (searchable) {
        if ((!textual)) {
          throw Unsupported.of(((("full-text search on " + type) + " field ") + name));
        }
        args.put("index_searchable", Code.flag(true));
      }
      if ((analyzer != null)) {
        args.put("tokenization", Code.text(this.tokenization(analyzer)));
        final Function1<AnalyzerFilter, Boolean> _function_2 = (AnalyzerFilter it) -> {
          EAnalyzerFilterType _type = it.getType();
          return Boolean.valueOf(Objects.equals(_type, EAnalyzerFilterType.ASCII_FOLDING));
        };
        boolean _exists = IterableExtensions.<AnalyzerFilter>exists(analyzer.getFilters(), _function_2);
        if (_exists) {
          args.put("ascii_fold", Code.flag(true));
        }
      }
      for (final PayloadIndex index_1 : indexes) {
        EPayloadIndexKind _kind = index_1.getKind();
        if (_kind != null) {
          switch (_kind) {
            case KEYWORD:
              if ((!textual)) {
                throw Unsupported.of(((("a keyword index on " + type) + " field ") + name));
              }
              if (searchable) {
                throw Unsupported.of((("a keyword index on " + name) + ", which is also full-text searchable"));
              }
              String _xifexpression_1 = null;
              boolean _isLowercase = index_1.isLowercase();
              if (_isLowercase) {
                _xifexpression_1 = "lowercase";
              } else {
                _xifexpression_1 = "field";
              }
              args.put("tokenization", Code.text(_xifexpression_1));
              args.put("index_filterable", Code.flag(true));
              break;
            case INTEGER:
            case FLOAT:
            case DATETIME:
              boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("int", "number", "date")).contains(type);
              boolean _not = (!_contains);
              if (_not) {
                throw Unsupported.of(((("a range index on " + type) + " field ") + name));
              }
              args.put("index_range_filters", Code.flag(true));
              break;
            case BOOL:
            case UUID:
              args.put("index_filterable", Code.flag(true));
              break;
            case FULL_TEXT:
              break;
            case GEO:
              boolean _notEquals = (!Objects.equals(type, "geoCoordinates"));
              if (_notEquals) {
                throw Unsupported.of(((("a geo index on " + type) + " field ") + name));
              }
              break;
            default:
              String _literal = index_1.getKind().getLiteral();
              String _plus = (_literal + " payload indexes");
              throw Unsupported.of(_plus);
          }
        } else {
          String _literal = index_1.getKind().getLiteral();
          String _plus = (_literal + " payload indexes");
          throw Unsupported.of(_plus);
        }
      }
      final Function1<Field, Code.Entries> _function_3 = (Field it) -> {
        Code.Entries _xblockexpression_1 = null;
        {
          if ((!(it instanceof PayloadField))) {
            throw Unsupported.of(("vector fields inside " + name));
          }
          _xblockexpression_1 = this.property(c, ((PayloadField) it), true);
        }
        return _xblockexpression_1;
      };
      final List<Code.Entries> subFields = ListExtensions.<Field, Code.Entries>map(field.getSubFields(), _function_3);
      boolean _isEmpty = subFields.isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        args.put("nested_properties", Code.items(subFields));
      }
      String _description = field.getDescription();
      boolean _tripleNotEquals = (_description != null);
      if (_tripleNotEquals) {
        args.put("description", Code.text(field.getDescription()));
      }
      this.note(field, "nested", "documentBody");
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private String dataType(final VectorCollection c, final PayloadField field) {
    String _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      if (((field == c.getPrimaryKey()) && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID)).contains(field.getType())))) {
        String _literal = field.getType().getLiteral();
        String _plus = ("primary key of type " + _literal);
        throw Unsupported.of(_plus);
      }
      String _switchResult = null;
      EScalarDType _type = field.getType();
      if (_type != null) {
        switch (_type) {
          case JSON:
          case MAP:
          case STRUCT:
            _switchResult = this.objectType(field, name, "object");
            break;
          case ARRAY:
            String _xblockexpression_1 = null;
            {
              boolean _written = ModelUtil.written(field, "elementType");
              boolean _not = (!_written);
              if (_not) {
                throw Unsupported.of((("array field " + name) + " without element-type"));
              }
              String _switchResult_1 = null;
              EScalarDType _elementType = field.getElementType();
              if (_elementType != null) {
                switch (_elementType) {
                  case JSON:
                  case MAP:
                  case STRUCT:
                    _switchResult_1 = this.objectType(field, name, "object[]");
                    break;
                  case BYTES:
                  case GEO_POINT:
                  case ARRAY:
                    String _literal_1 = field.getElementType().getLiteral();
                    String _plus_1 = ("arrays of " + _literal_1);
                    throw Unsupported.of(_plus_1);
                  default:
                    String _scalarType = this.scalarType(field.getElementType(), name);
                    _switchResult_1 = (_scalarType + "[]");
                    break;
                }
              } else {
                String _scalarType = this.scalarType(field.getElementType(), name);
                _switchResult_1 = (_scalarType + "[]");
              }
              _xblockexpression_1 = _switchResult_1;
            }
            _switchResult = _xblockexpression_1;
            break;
          default:
            String _xblockexpression_2 = null;
            {
              boolean _isEmpty = field.getSubFields().isEmpty();
              boolean _not = (!_isEmpty);
              if (_not) {
                String _literal_1 = field.getType().getLiteral();
                String _plus_1 = ("sub-fields on " + _literal_1);
                String _plus_2 = (_plus_1 + " fields");
                throw Unsupported.of(_plus_2);
              }
              _xblockexpression_2 = this.scalarType(field.getType(), name);
            }
            _switchResult = _xblockexpression_2;
            break;
        }
      } else {
        String _xblockexpression_2 = null;
        {
          boolean _isEmpty = field.getSubFields().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            String _literal_1 = field.getType().getLiteral();
            String _plus_1 = ("sub-fields on " + _literal_1);
            String _plus_2 = (_plus_1 + " fields");
            throw Unsupported.of(_plus_2);
          }
          _xblockexpression_2 = this.scalarType(field.getType(), name);
        }
        _switchResult = _xblockexpression_2;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private String objectType(final PayloadField field, final String name, final String type) {
    String _xblockexpression = null;
    {
      boolean _isEmpty = field.getSubFields().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of((("json field " + name) + " without sub-fields; Weaviate object properties declare their nested properties"));
      }
      _xblockexpression = type;
    }
    return _xblockexpression;
  }

  private String scalarType(final EScalarDType type, final String name) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case INT:
        case BIGINT:
        case INT8:
          _switchResult = "int";
          break;
        case FLOAT:
        case DOUBLE:
          _switchResult = "number";
          break;
        case BOOL:
          _switchResult = "boolean";
          break;
        case VARCHAR:
        case TEXT:
          _switchResult = "text";
          break;
        case UUID:
          _switchResult = "uuid";
          break;
        case DATE:
        case DATETIME:
        case TIMESTAMPTZ:
          _switchResult = "date";
          break;
        case BYTES:
          _switchResult = "blob";
          break;
        case GEO_POINT:
          _switchResult = "geoCoordinates";
          break;
        default:
          String _literal = type.getLiteral();
          String _plus = (_literal + " fields");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = type.getLiteral();
      String _plus = (_literal + " fields");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Code.Entries vector(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of("multi-vector fields");
      }
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
      if (_equals) {
        throw Unsupported.of((("sparse vector field " + name) + " without a bm25 function"));
      }
      EVectorDType _type_1 = field.getType();
      boolean _notEquals = (!Objects.equals(_type_1, EVectorDType.FLOAT_VECTOR));
      if (_notEquals) {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " fields");
        throw Unsupported.of(_plus);
      }
      final Code.Entries embedding = this.embedding(c, field);
      this.note(field, "normalized", "onDisk", "nullable");
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      final EMetric chosen = this.metric(field, index);
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(name), "distance", Code.text(this.distance(chosen)));
      if ((embedding != null)) {
        args.put("vectorizer", embedding);
      }
      if ((index != null)) {
        this.vectorIndex(index, args);
      }
      if ((((index != null) && Objects.equals(index.getFamily(), EIndexFamily.FLAT)) && (!Objects.equals(chosen, EMetric.COSINE)))) {
        throw Unsupported.of("flat indexes with a metric other than cosine; io.weaviate:client6 6.3.1 sets no flat-index distance");
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * Vectorizer module of a vector field: its own model, a text-embedding function, or the collection's model.
   */
  public Code.Entries embedding(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final Function function = ModelUtil.producingFunction(c, field);
      EmbeddingModel _elvis = null;
      EmbeddingModel _elvis_1 = null;
      EmbeddingModel _model = null;
      if (function!=null) {
        _model=function.getModel();
      }
      if (_model != null) {
        _elvis_1 = _model;
      } else {
        EmbeddingModel _model_1 = field.getModel();
        _elvis_1 = _model_1;
      }
      if (_elvis_1 != null) {
        _elvis = _elvis_1;
      } else {
        EmbeddingModel _embeddingModel = c.getEmbeddingModel();
        _elvis = _embeddingModel;
      }
      final EmbeddingModel model = _elvis;
      if ((model == null)) {
        if ((function != null)) {
          String _name = function.getName();
          String _plus = ("text-embedding function " + _name);
          String _plus_1 = (_plus + " without a model");
          throw Unsupported.of(_plus_1);
        }
        return null;
      }
      final Code.Entries result = this.vectorizerModule(model);
      EList<Field> _xifexpression = null;
      if ((function != null)) {
        _xifexpression = function.getInputs();
      } else {
        _xifexpression = field.getSourceFields();
      }
      final EList<Field> sources = _xifexpression;
      boolean _isEmpty = sources.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        for (final Field source : sources) {
          if ((!(source instanceof PayloadField))) {
            String _fieldName = ModelUtil.fieldName(source);
            String _plus_2 = ("vectorizing vector field " + _fieldName);
            throw Unsupported.of(_plus_2);
          }
        }
        final Function1<Field, String> _function = (Field input) -> {
          return WeaviateSchema.propertyName(c, input);
        };
        result.put("source_properties", Code.texts(ListExtensions.<Field, String>map(sources, _function)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public Code.Entries quantizer(final Quantization quantization) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(quantization, "oversampling");
      if (_written) {
        throw Unsupported.of("oversampling; Weaviate rescores with a fixed candidate limit, not a factor");
      }
      boolean _written_1 = ModelUtil.written(quantization, "truncateDimension");
      if (_written_1) {
        throw Unsupported.of("truncate-dim; Weaviate quantizes the full vector");
      }
      boolean _isEmpty = this.extValues(quantization).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on quantization");
      }
      this.note(quantization, "rescore", "keepOriginals");
      double _xifexpression = (double) 0;
      boolean _written_2 = ModelUtil.written(quantization, "bits");
      if (_written_2) {
        _xifexpression = quantization.getBits();
      } else {
        _xifexpression = 0.0;
      }
      final double bits = _xifexpression;
      Code.Entries _switchResult = null;
      EQuantizationKind _type = quantization.getType();
      if (_type != null) {
        switch (_type) {
          case NONE:
            _switchResult = WeaviateSchema.kw("type", Code.text("none"));
            break;
          case SCALAR:
            Code.Entries _xblockexpression_1 = null;
            {
              if (((bits != 0) && (bits != 8))) {
                String _number = Syntax.number(bits);
                String _plus = ("scalar quantization with " + _number);
                String _plus_1 = (_plus + " bits; Weaviate SQ uses 8 bits");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = WeaviateSchema.kw("type", Code.text("sq"));
            }
            _switchResult = _xblockexpression_1;
            break;
          case BINARY:
            Code.Entries _xblockexpression_2 = null;
            {
              if (((bits != 0) && (bits != 1))) {
                String _number = Syntax.number(bits);
                String _plus = ("binary quantization with " + _number);
                String _plus_1 = (_plus + " bits; Weaviate BQ uses 1 bit");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_2 = WeaviateSchema.kw("type", Code.text("bq"));
            }
            _switchResult = _xblockexpression_2;
            break;
          case PRODUCT:
            Code.Entries _xblockexpression_3 = null;
            {
              boolean _written_3 = ModelUtil.written(quantization, "bits");
              if (_written_3) {
                throw Unsupported.of("product quantization bits; Weaviate PQ uses 256 centroids per segment");
              }
              final Code.Entries result = WeaviateSchema.kw("type", Code.text("pq"));
              boolean _written_4 = ModelUtil.written(quantization, "subvectors");
              if (_written_4) {
                result.put("segments", Code.num(quantization.getSubvectors()));
              }
              _xblockexpression_3 = result;
            }
            _switchResult = _xblockexpression_3;
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private void vectorIndex(final VectorIndex index, final Code.Entries args) {
    Quantization _quantization = index.getQuantization();
    boolean _tripleNotEquals = (_quantization != null);
    if (_tripleNotEquals) {
      args.put("quantizer", this.quantizer(index.getQuantization()));
    }
    boolean _written = ModelUtil.written(index, "accelerator");
    if (_written) {
      String _literal = index.getAccelerator().getLiteral();
      String _plus = (_literal + " accelerated indexes");
      throw Unsupported.of(_plus);
    }
    boolean _written_1 = ModelUtil.written(index, "centroids");
    if (_written_1) {
      throw Unsupported.of("centroids; Weaviate has no IVF index");
    }
    this.note(index, "onDisk", "online");
    EIndexFamily _family = index.getFamily();
    if (_family != null) {
      switch (_family) {
        case AUTO:
          break;
        case HNSW:
          args.put("index", Code.text("hnsw"));
          break;
        case FLAT:
          args.put("index", Code.text("flat"));
          break;
        default:
          String _literal_1 = index.getFamily().getLiteral();
          String _plus_1 = (_literal_1 + " indexes");
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal_1 = index.getFamily().getLiteral();
      String _plus_1 = (_literal_1 + " indexes");
      throw Unsupported.of(_plus_1);
    }
    EIndexFamily _family_1 = index.getFamily();
    final boolean graph = (!Objects.equals(_family_1, EIndexFamily.FLAT));
    final boolean tuned = ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates")) || ModelUtil.written(index, "searchCandidates"));
    if (((!graph) && tuned)) {
      throw Unsupported.of("graph parameters on a flat index");
    }
    boolean _written_2 = ModelUtil.written(index, "maxDegree");
    if (_written_2) {
      args.put("max_connections", Code.num(index.getMaxDegree()));
    }
    boolean _written_3 = ModelUtil.written(index, "buildCandidates");
    if (_written_3) {
      args.put("ef_construction", Code.num(index.getBuildCandidates()));
    }
    boolean _written_4 = ModelUtil.written(index, "searchCandidates");
    if (_written_4) {
      args.put("ef", Code.num(index.getSearchCandidates()));
    }
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
          _switchResult = "cosine";
          break;
        case DOT:
          _switchResult = "dot";
          break;
        case EUCLIDEAN:
          _switchResult = "l2-squared";
          break;
        case MANHATTAN:
          _switchResult = "manhattan";
          break;
        case HAMMING:
          _switchResult = "hamming";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * Text fields that feed a bm25 function; BM25 is part of their inverted index, so the function emits nothing.
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
              break;
            case SPARSE_ENCODING:
              throw Unsupported.of("sparse-encoding functions; Weaviate has no sparse vectors");
            case RERANK:
              throw Unsupported.of("rerank functions; Weaviate reranks at query time with the collection\'s reranker module");
            default:
              String _literal = function.getType().getLiteral();
              String _plus = (_literal + " functions; Weaviate runs no custom server-side functions");
              throw Unsupported.of(_plus);
          }
        } else {
          String _literal = function.getType().getLiteral();
          String _plus = (_literal + " functions; Weaviate runs no custom server-side functions");
          throw Unsupported.of(_plus);
        }
      }
      _xblockexpression = inputs;
    }
    return _xblockexpression;
  }

  private String tokenization(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      final Function1<AnalyzerFilter, EAnalyzerFilterType> _function = (AnalyzerFilter it) -> {
        return it.getType();
      };
      final Set<EAnalyzerFilterType> filters = IterableExtensions.<EAnalyzerFilterType>toSet(ListExtensions.<AnalyzerFilter, EAnalyzerFilterType>map(analyzer.getFilters(), _function));
      for (final EAnalyzerFilterType filter : filters) {
        boolean _contains = Collections.<EAnalyzerFilterType>unmodifiableList(CollectionLiterals.<EAnalyzerFilterType>newArrayList(EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STOP, EAnalyzerFilterType.ASCII_FOLDING)).contains(filter);
        boolean _not = (!_contains);
        if (_not) {
          String _literal = filter.getLiteral();
          String _plus = (_literal + " token filters");
          throw Unsupported.of(_plus);
        }
      }
      if ((filters.contains(EAnalyzerFilterType.STOP) && (this.preset(analyzer) == null))) {
        throw Unsupported.of("a stop filter without stopwords or language \"en\"");
      }
      String _switchResult = null;
      ETokenizer _tokenizer = analyzer.getTokenizer();
      if (_tokenizer != null) {
        switch (_tokenizer) {
          case STANDARD:
            _switchResult = "word";
            break;
          case WHITESPACE:
            String _xifexpression = null;
            boolean _contains_1 = filters.contains(EAnalyzerFilterType.LOWERCASE);
            if (_contains_1) {
              _xifexpression = "lowercase";
            } else {
              _xifexpression = "whitespace";
            }
            _switchResult = _xifexpression;
            break;
          case KEYWORD:
            String _xblockexpression_1 = null;
            {
              boolean _contains_2 = filters.contains(EAnalyzerFilterType.LOWERCASE);
              if (_contains_2) {
                throw Unsupported.of("a lowercase filter on the keyword tokenizer");
              }
              _xblockexpression_1 = "field";
            }
            _switchResult = _xblockexpression_1;
            break;
          case LANGUAGE:
            String _switchResult_1 = null;
            String _language = this.language(analyzer);
            if (_language != null) {
              switch (_language) {
                case "en":
                  _switchResult_1 = "word";
                  break;
                case "ja":
                  _switchResult_1 = "kagome_ja";
                  break;
                case "ko":
                  _switchResult_1 = "kagome_kr";
                  break;
                default:
                  String _language_1 = analyzer.getLanguage();
                  String _plus_1 = ("language analyzer " + _language_1);
                  throw Unsupported.of(_plus_1);
              }
            } else {
              String _language_1 = analyzer.getLanguage();
              String _plus_1 = ("language analyzer " + _language_1);
              throw Unsupported.of(_plus_1);
            }
            _switchResult = _switchResult_1;
            break;
          default:
            String _literal_1 = analyzer.getTokenizer().getLiteral();
            String _plus_2 = (_literal_1 + " tokenizer");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal_1 = analyzer.getTokenizer().getLiteral();
        String _plus_2 = (_literal_1 + " tokenizer");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private String language(final Analyzer analyzer) {
    String _switchResult = null;
    String _elvis = null;
    String _language = analyzer.getLanguage();
    if (_language != null) {
      _elvis = _language;
    } else {
      _elvis = "en";
    }
    String _lowerCase = _elvis.toLowerCase();
    if (_lowerCase != null) {
      switch (_lowerCase) {
        case "en":
        case "english":
          _switchResult = "en";
          break;
        case "ja":
        case "japanese":
          _switchResult = "ja";
          break;
        case "ko":
        case "korean":
          _switchResult = "ko";
          break;
        default:
          _switchResult = analyzer.getLanguage();
          break;
      }
    } else {
      _switchResult = analyzer.getLanguage();
    }
    return _switchResult;
  }

  private String preset(final Analyzer analyzer) {
    Object _xblockexpression = null;
    {
      String _stopwords = analyzer.getStopwords();
      boolean _tripleNotEquals = (_stopwords != null);
      if (_tripleNotEquals) {
        final String preset = analyzer.getStopwords().toLowerCase();
        if (((!Objects.equals(preset, "en")) && (!Objects.equals(preset, "none")))) {
          String _stopwords_1 = analyzer.getStopwords();
          String _plus = ("stopword list \"" + _stopwords_1);
          String _plus_1 = (_plus + "\"; Weaviate stopword presets are en and none");
          throw Unsupported.of(_plus_1);
        }
        return preset;
      }
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        return null;
      }
      String _language_1 = this.language(analyzer);
      boolean _equals = Objects.equals(_language_1, "en");
      if (_equals) {
        return "en";
      }
      ETokenizer _tokenizer = analyzer.getTokenizer();
      boolean _notEquals = (!Objects.equals(_tokenizer, ETokenizer.LANGUAGE));
      if (_notEquals) {
        String _language_2 = analyzer.getLanguage();
        String _plus_2 = ("language \"" + _language_2);
        String _plus_3 = (_plus_2 + "\" on the ");
        String _literal = analyzer.getTokenizer().getLiteral();
        String _plus_4 = (_plus_3 + _literal);
        String _plus_5 = (_plus_4 + " tokenizer; Weaviate has stopword presets only for en");
        throw Unsupported.of(_plus_5);
      }
      _xblockexpression = null;
    }
    return ((String)_xblockexpression);
  }

  private Code.Entries invertedIndex(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<Analyzer> _analyzers = c.getAnalyzers();
      for (final Analyzer analyzer : _analyzers) {
        {
          boolean _isEmpty = analyzer.getResources().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of("analyzer resources");
          }
          final String stopwords = this.preset(analyzer);
          if ((stopwords != null)) {
            this.set(result, "stopwords_preset", Code.text(stopwords));
          }
          boolean _written = ModelUtil.written(analyzer, "bm25K1");
          if (_written) {
            this.set(result, "bm25_k1", Code.num(analyzer.getBm25K1()));
          }
          boolean _written_1 = ModelUtil.written(analyzer, "bm25B");
          if (_written_1) {
            this.set(result, "bm25_b", Code.num(analyzer.getBm25B()));
          }
        }
      }
      final Function1<VendorExtension, Boolean> _function = (VendorExtension it) -> {
        String _vendor = it.getVendor();
        return Boolean.valueOf(Objects.equals(_vendor, WeaviateGenerator.VENDOR));
      };
      final Function1<VendorExtension, EList<ExtensionItem>> _function_1 = (VendorExtension it) -> {
        return it.getItems();
      };
      Iterable<InvertedIndexSettings> _filter = Iterables.<InvertedIndexSettings>filter(IterableExtensions.<VendorExtension, ExtensionItem>flatMap(IterableExtensions.<VendorExtension>filter(c.getExtensions(), _function), _function_1), InvertedIndexSettings.class);
      for (final InvertedIndexSettings settings : _filter) {
        {
          boolean _isIndexTimestamps = settings.isIndexTimestamps();
          if (_isIndexTimestamps) {
            this.set(result, "index_timestamps", Code.flag(true));
          }
          boolean _isIndexNullState = settings.isIndexNullState();
          if (_isIndexNullState) {
            this.set(result, "index_null_state", Code.flag(true));
          }
          boolean _isIndexPropertyLength = settings.isIndexPropertyLength();
          if (_isIndexPropertyLength) {
            this.set(result, "index_property_length", Code.flag(true));
          }
          boolean _written = ModelUtil.written(settings, "bm25K1");
          if (_written) {
            this.set(result, "bm25_k1", Code.num(settings.getBm25K1()));
          }
          boolean _written_1 = ModelUtil.written(settings, "bm25B");
          if (_written_1) {
            this.set(result, "bm25_b", Code.num(settings.getBm25B()));
          }
          String _stopwords = settings.getStopwords();
          boolean _tripleNotEquals = (_stopwords != null);
          if (_tripleNotEquals) {
            this.set(result, "stopwords_preset", Code.text(settings.getStopwords()));
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private void set(final Code.Entries settings, final String key, final Code.Expr value) {
    if ((settings.has(key) && (!Objects.equals(settings.get(key), value)))) {
      throw Unsupported.of((("conflicting " + key) + " settings"));
    }
    settings.put(key, value);
  }

  private Code.Entries limits(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field : _payloadFields) {
        {
          final Code.Entries limit = WeaviateSchema.kw();
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
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        int _dimension = it.getDimension();
        return Boolean.valueOf((_dimension > 0));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(this.storedVectors(c), _function);
      for (final VectorField field_1 : _filter) {
        result.put(ModelUtil.fieldName(field_1), WeaviateSchema.kw("dim", Code.num(field_1.getDimension())));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public String consistency(final EConsistencyLevel level) {
    String _switchResult = null;
    if (level != null) {
      switch (level) {
        case STRONG:
          _switchResult = "ALL";
          break;
        case BOUNDED:
          _switchResult = "QUORUM";
          break;
        case SESSION:
          _switchResult = "QUORUM";
          break;
        case EVENTUAL:
          _switchResult = "ONE";
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }

  public Map<String, Code.Expr> extValues(final EObject host) {
    LinkedHashMap<String, Code.Expr> _xblockexpression = null;
    {
      final LinkedHashMap<String, Code.Expr> values = new LinkedHashMap<String, Code.Expr>();
      if ((host != null)) {
        Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, WeaviateGenerator.VENDOR).entrySet();
        for (final Map.Entry<String, Literal> entry : _entrySet) {
          values.put(entry.getKey(), Literals.of(entry.getValue()));
        }
      }
      _xblockexpression = values;
    }
    return _xblockexpression;
  }

  public String extString(final EObject host, final String key) {
    String _xblockexpression = null;
    {
      if ((host == null)) {
        return null;
      }
      final Literal value = ModelUtil.extParams(host, WeaviateGenerator.VENDOR).get(key);
      String _xifexpression = null;
      if ((value instanceof StringLit)) {
        _xifexpression = ((StringLit)value).getValue();
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
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
