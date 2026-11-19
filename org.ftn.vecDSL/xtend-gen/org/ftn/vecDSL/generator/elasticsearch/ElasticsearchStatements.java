package org.ftn.vecDSL.generator.elasticsearch;

import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.XbaseGenerated;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.AddAliasOp;
import org.ftn.vecDSL.vecDSL.AddFieldOp;
import org.ftn.vecDSL.vecDSL.AddFunctionOp;
import org.ftn.vecDSL.vecDSL.AggregationClause;
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Alter;
import org.ftn.vecDSL.vecDSL.AlterOperation;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.AuthConfig;
import org.ftn.vecDSL.vecDSL.AwaitIndex;
import org.ftn.vecDSL.vecDSL.BackupSchedule;
import org.ftn.vecDSL.vecDSL.BatchQuery;
import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.BoostBlock;
import org.ftn.vecDSL.vecDSL.BoostFunction;
import org.ftn.vecDSL.vecDSL.BulkImport;
import org.ftn.vecDSL.vecDSL.Clone;
import org.ftn.vecDSL.vecDSL.Cluster;
import org.ftn.vecDSL.vecDSL.ClusterLimit;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Compact;
import org.ftn.vecDSL.vecDSL.Concept;
import org.ftn.vecDSL.vecDSL.Condition;
import org.ftn.vecDSL.vecDSL.Connection;
import org.ftn.vecDSL.vecDSL.Count;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.DdlMethod;
import org.ftn.vecDSL.vecDSL.Delete;
import org.ftn.vecDSL.vecDSL.Describe;
import org.ftn.vecDSL.vecDSL.DistanceMatrix;
import org.ftn.vecDSL.vecDSL.Drop;
import org.ftn.vecDSL.vecDSL.DropFieldOp;
import org.ftn.vecDSL.vecDSL.DropFunctionOp;
import org.ftn.vecDSL.vecDSL.DropSnapshot;
import org.ftn.vecDSL.vecDSL.EAuthType;
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FileResource;
import org.ftn.vecDSL.vecDSL.FileSource;
import org.ftn.vecDSL.vecDSL.FilterBlock;
import org.ftn.vecDSL.vecDSL.Flush;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.GenerativeModel;
import org.ftn.vecDSL.vecDSL.GenerativeSearch;
import org.ftn.vecDSL.vecDSL.GetById;
import org.ftn.vecDSL.vecDSL.HybridLeg;
import org.ftn.vecDSL.vecDSL.HybridSearch;
import org.ftn.vecDSL.vecDSL.Index;
import org.ftn.vecDSL.vecDSL.InlineRecords;
import org.ftn.vecDSL.vecDSL.Insert;
import org.ftn.vecDSL.vecDSL.KeywordLeg;
import org.ftn.vecDSL.vecDSL.KeywordSearch;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.ListObjects;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Load;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.MetricsConfig;
import org.ftn.vecDSL.vecDSL.MmrConfig;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.OutputSpec;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.ParamSource;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.QueryInput;
import org.ftn.vecDSL.vecDSL.QueryMethod;
import org.ftn.vecDSL.vecDSL.RebuildIndex;
import org.ftn.vecDSL.vecDSL.RecommendSearch;
import org.ftn.vecDSL.vecDSL.RecordEntry;
import org.ftn.vecDSL.vecDSL.RecordLiteral;
import org.ftn.vecDSL.vecDSL.RecordSource;
import org.ftn.vecDSL.vecDSL.RefreshExternal;
import org.ftn.vecDSL.vecDSL.Release;
import org.ftn.vecDSL.vecDSL.RenameFieldOp;
import org.ftn.vecDSL.vecDSL.ReplicationConfig;
import org.ftn.vecDSL.vecDSL.Reranker;
import org.ftn.vecDSL.vecDSL.Restore;
import org.ftn.vecDSL.vecDSL.RoleBinding;
import org.ftn.vecDSL.vecDSL.Sample;
import org.ftn.vecDSL.vecDSL.ScoreBounds;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.Scroll;
import org.ftn.vecDSL.vecDSL.SearchFieldRef;
import org.ftn.vecDSL.vecDSL.SearchParams;
import org.ftn.vecDSL.vecDSL.SetPropertyOp;
import org.ftn.vecDSL.vecDSL.ShardConfig;
import org.ftn.vecDSL.vecDSL.Snapshot;
import org.ftn.vecDSL.vecDSL.SortClause;
import org.ftn.vecDSL.vecDSL.SparseEntry;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.SwapAlias;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.Transaction;
import org.ftn.vecDSL.vecDSL.Truncate;
import org.ftn.vecDSL.vecDSL.Update;
import org.ftn.vecDSL.vecDSL.Upsert;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorDatabase;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps each vecDSL statement to Elasticsearch REST requests in the host-language-neutral code model. Every request body is
 * built here as a JSON value and passed to the runtime, so the three host languages send identical requests: request sends
 * one request, search and fetch shape hits into records with the canonical score, write sends _bulk requests, hybrid
 * fuses the legs of a hybrid search (the rrf and linear retrievers need an enterprise license), and recommend computes the
 * query vector of a recommendation from stored examples.
 */
@SuppressWarnings("all")
public class ElasticsearchStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; ElasticsearchCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Elasticsearch: ";

  /**
   * Suffix of constructs Elasticsearch supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no Elasticsearch mapping yet";

  public static final String NO_MODELS = "; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors";

  public static final String EXTERNAL = "collections derived from external sources; Elasticsearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines";

  public static final String NO_TTL = "ttl; Elasticsearch documents do not expire (index lifecycle management deletes whole indices after rollover, not single documents)";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; Elasticsearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)";

  public static final String NO_TAGS_REASON = "Elasticsearch keeps them in the _meta of the mapping";

  public static final String NO_FILES = "Elasticsearch reads analysis files from the config directory of the node, which a client cannot write";

  public static final String LANGUAGES_REFERENCE = "Elasticsearch has stop-word lists and stemmers for its language analyzers (english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian, turkish, serbian and others)";

  public static final String NO_TRANSACTIONS = "transactions; Elasticsearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)";

  public static final String NO_GENERATION = "generative models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)";

  public static final String NO_RERANKERS = "the text_similarity_reranker retriever needs an enterprise license (TextSimilarityRankBuilder, TEXT_SIMILARITY_RERANKER_FEATURE) and an inference endpoint";

  public static final String NO_METRICS = "metrics configuration; Elasticsearch metrics are collected by Elastic Agent or Metricbeat from the stats APIs, not configured per program";

  public static final String NO_CLUSTER_LIMITS = "cluster limits; Elasticsearch limits shards per node (cluster.max_shards_per_node), not the number of indices";

  public static final String ONE_CLUSTER = "the program talks to one Elasticsearch cluster; clusters are deployments, created outside the REST API";

  private static final String FIXED_SHARDS = "the shard count of an existing index is fixed (the split and shrink APIs copy it into a new index)";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why Elasticsearch (in the basic license) cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Elasticsearch searches the current state of an index (a point in time pins a state for paging, not a past one)"), Pair.<String, String>of("reranker", ElasticsearchStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated Elasticsearch programs embed no query text" + ElasticsearchStatements.NO_MODELS))));

  /**
   * Clauses Elasticsearch supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "a kNN search inside a nested field"), Pair.<String, String>of("prefetches", "nested retrievers")));

  /**
   * Hits a search reads for a cursor page: the k of its kNN query, as Vespa's cursor reads the first 1000 hits of its order.
   */
  private static final int CURSOR_WINDOW = 1000;

  /**
   * Search body parameters that an 'extension elasticsearch' block on a query may set.
   */
  private static final List<String> SEARCH_KEYS = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("terminate_after", "min_score", "track_scores", "timeout"));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final ElasticsearchSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public ElasticsearchStatements(final Script script) {
    this.script = script;
    ElasticsearchSchema _elasticsearchSchema = new ElasticsearchSchema(this.notes, script);
    this.schema = _elasticsearchSchema;
  }

  /**
   * The method for one statement, followed by helper methods it calls (batch queries).
   */
  public List<Program.Method> methods(final Method statement, final String name) {
    List<Program.Method> _xblockexpression = null;
    {
      this.notes.clear();
      this.helpers.clear();
      this.current = name;
      List<Code.Stmt> _xtrycatchfinallyexpression = null;
      try {
        _xtrycatchfinallyexpression = this.body(statement);
      } catch (final Throwable _t) {
        if (_t instanceof Unsupported) {
          final Unsupported e = (Unsupported)_t;
          ArrayList<Code.Stmt> _xblockexpression_1 = null;
          {
            this.helpers.clear();
            String _message = e.getMessage();
            Code.Fail _fail = new Code.Fail(_message);
            _xblockexpression_1 = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _fail));
          }
          _xtrycatchfinallyexpression = _xblockexpression_1;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      final List<Code.Stmt> statements = _xtrycatchfinallyexpression;
      final List<Program.Method> result = CollectionLiterals.<Program.Method>newArrayList();
      List<String> _parameterNames = ModelUtil.parameterNames(statement);
      List<String> _list = this.notes.stream().distinct().toList();
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
      Program.Method _method = new Program.Method(name, _parameterNames, statements, _list, _newArrayList, false);
      result.add(_method);
      Iterables.<Program.Method>addAll(result, this.helpers);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public static String functionReason(final String type) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case "bm25":
          _switchResult = "Elasticsearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (sparse_vector fields take their weights from the client or an inference model)";
          break;
        case "custom":
          _switchResult = (("custom functions; " + ElasticsearchStatements.PENDING) + " (an ingest pipeline with a script processor)");
          break;
        default:
          _switchResult = "model functions run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)";
          break;
      }
    } else {
      _switchResult = "model functions run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)";
    }
    return _switchResult;
  }

  /**
   * Index store settings of a storage block: mmap is the mmapfs store; the other kinds are node or license features.
   */
  public static void storage(final StorageConfig storage, final Code.Entries settings) {
    if ((storage == null)) {
      return;
    }
    String _location = storage.getLocation();
    boolean _tripleNotEquals = (_location != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a storage location; Elasticsearch keeps index data under path.data, a node setting");
    }
    boolean _isTiered = storage.isTiered();
    if (_isTiered) {
      throw Unsupported.of("tiered storage; data tiers move indices between nodes of different roles, and one node has one tier");
    }
    String _literal = storage.getType().getLiteral();
    if (_literal != null) {
      switch (_literal) {
        case "disk":
          String _xifexpression = null;
          boolean _isMmap = storage.isMmap();
          if (_isMmap) {
            _xifexpression = "mmapfs";
          } else {
            _xifexpression = "niofs";
          }
          settings.put("index.store.type", ElasticsearchBodies.text(_xifexpression));
          break;
        case "memory":
          throw Unsupported.of("memory storage; Elasticsearch stores indices on disk (the memory store type was removed in 7.0)");
        case "object_storage":
          throw Unsupported.of("object storage; searchable snapshots, which mount indices from a repository, need an enterprise license");
        default:
          throw Unsupported.of("embedded storage; Elasticsearch runs as a server");
      }
    } else {
      throw Unsupported.of("embedded storage; Elasticsearch runs as a server");
    }
  }

  /**
   * Shards and replicas of the indices of a cluster's collections: the shard count and the replication factor minus one.
   */
  public static void clusterSettings(final Cluster cluster, final Code.Entries settings) {
    if ((cluster == null)) {
      return;
    }
    ShardConfig _sharding = cluster.getSharding();
    boolean _tripleNotEquals = (_sharding != null);
    if (_tripleNotEquals) {
      final ShardConfig sharding = cluster.getSharding();
      if (((sharding.getKey() != null) || (ModelUtil.written(sharding, "strategy") && (!Objects.equals(sharding.getStrategy().getLiteral(), "hash"))))) {
        String _literal = sharding.getStrategy().getLiteral();
        String _plus = ("sharding by key or with the " + _literal);
        String _plus_1 = (_plus + " strategy; ");
        String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
        String _plus_3 = (_plus_2 + " (custom routing)");
        throw Unsupported.of(_plus_3);
      }
      int _shards = sharding.getShards();
      boolean _lessThan = (_shards < 1);
      if (_lessThan) {
        throw Unsupported.of("a shard count below 1");
      }
      settings.put("number_of_shards", ElasticsearchBodies.num(sharding.getShards()));
    }
    ReplicationConfig _replication = cluster.getReplication();
    boolean _tripleNotEquals_1 = (_replication != null);
    if (_tripleNotEquals_1) {
      int _factor = cluster.getReplication().getFactor();
      boolean _lessThan_1 = (_factor < 1);
      if (_lessThan_1) {
        throw Unsupported.of("a replication factor below 1");
      }
      int _factor_1 = cluster.getReplication().getFactor();
      int _minus = (_factor_1 - 1);
      settings.put("number_of_replicas", ElasticsearchBodies.num(_minus));
    }
  }

  /**
   * Collection limits as index settings: max_fields is index.mapping.total_fields.limit.
   */
  public static void collectionLimit(final CollectionLimit limit, final Code.Entries settings) {
    String _literal = limit.getType().getLiteral();
    if (_literal != null) {
      switch (_literal) {
        case "max_fields":
          settings.put("index.mapping.total_fields.limit", ElasticsearchBodies.num(limit.getValue()));
          break;
        default:
          String _literal_1 = limit.getType().getLiteral();
          String _plus = ("collection limits (" + _literal_1);
          String _plus_1 = (_plus + "); Elasticsearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit");
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal_1 = limit.getType().getLiteral();
      String _plus = ("collection limits (" + _literal_1);
      String _plus_1 = (_plus + "); Elasticsearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit");
      throw Unsupported.of(_plus_1);
    }
  }

  /**
   * Partitions are values of a keyword field with a filtered alias each; strategies other than list need routing.
   */
  public static void checkPartition(final Partition partition) {
    if (((partition.getKey() != null) || (ModelUtil.written(partition, "strategy") && (!Objects.equals(partition.getStrategy().getLiteral(), "list"))))) {
      String _literal = partition.getStrategy().getLiteral();
      String _plus = ("partitions by key or with the " + _literal);
      String _plus_1 = (_plus + " strategy; ");
      String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
      String _plus_3 = (_plus_2 + " (custom routing)");
      throw Unsupported.of(_plus_3);
    }
    boolean _isEmpty = partition.getSubPartitions().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of((("sub-partitions; " + ElasticsearchStatements.PENDING) + " (a partition path in the keyword field)"));
    }
  }

  protected List<Code.Stmt> _body(final Create it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Concept _elvis = null;
      Concept _definition = it.getDefinition();
      if (_definition != null) {
        _elvis = _definition;
      } else {
        Concept _target = it.getTarget();
        _elvis = _target;
      }
      boolean _isIfNotExists = it.isIfNotExists();
      Concept _definition_1 = it.getDefinition();
      boolean _tripleNotEquals = (_definition_1 != null);
      List<Code.Stmt> _define = this.define(_elvis, _isIfNotExists, _tripleNotEquals);
      Iterables.<Code.Stmt>addAll(result, _define);
      EList<DdlMethod> _nested = it.getNested();
      for (final DdlMethod statement : _nested) {
        List<Code.Stmt> _body = this.body(statement);
        Iterables.<Code.Stmt>addAll(result, _body);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public List<Code.Stmt> define(final Concept concept, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      boolean _matched = false;
      if (concept instanceof VectorCollection) {
        _matched=true;
        List<Code.Stmt> _createIndex = this.schema.createIndex(((VectorCollection)concept), ifNotExists, fresh);
        Iterables.<Code.Stmt>addAll(result, _createIndex);
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          this.connection(((Connection)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          MetricsConfig _metrics = ((VectorDatabase)concept).getMetrics();
          boolean _tripleNotEquals = (_metrics != null);
          if (_tripleNotEquals) {
            throw Unsupported.of(ElasticsearchStatements.NO_METRICS);
          }
          Connection _connection = ((VectorDatabase)concept).getConnection();
          boolean _tripleNotEquals_1 = (_connection != null);
          if (_tripleNotEquals_1) {
            this.connection(((VectorDatabase)concept).getConnection());
          }
          EList<Cluster> _clusters = ((VectorDatabase)concept).getClusters();
          for (final Cluster cluster : _clusters) {
            List<Code.Stmt> _define = this.define(cluster, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _define);
          }
          EList<Database> _databases = ((VectorDatabase)concept).getDatabases();
          for (final Database database : _databases) {
            List<Code.Stmt> _define_1 = this.define(database, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _define_1);
          }
          AuthConfig _auth = ((VectorDatabase)concept).getAuth();
          boolean _tripleNotEquals_2 = (_auth != null);
          if (_tripleNotEquals_2) {
            List<Code.Stmt> _define_2 = this.define(((VectorDatabase)concept).getAuth(), ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _define_2);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          boolean _isEmpty = ((Cluster)concept).getLimits().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of(ElasticsearchStatements.NO_CLUSTER_LIMITS);
          }
          Set<String> _keySet = ModelUtil.extParams(concept, ElasticsearchGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
          if ((((Cluster)concept).getCollections().isEmpty() && ((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null)))) {
            throw Unsupported.of("replication and sharding of a cluster without collections; Elasticsearch sets shards and replicas per index");
          }
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createIndex = this.schema.createIndex(collection, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createIndex);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          EList<VectorCollection> _collections = ((Database)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createIndex = this.schema.createIndex(collection, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createIndex);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          final ElasticsearchIndex target = this.schema.index(((Alias)concept).getCollection());
          Code.Call _request = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(ElasticsearchBodies.obj("add", ElasticsearchBodies.obj("index", ElasticsearchBodies.text(target.name()), "alias", ElasticsearchBodies.text(ElasticsearchBodies.aliasName(((Alias)concept).getName())))))));
          Code.Run _run = new Code.Run(_request);
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          List<Code.Stmt> _updateVectorIndex = this.updateVectorIndex(((VectorIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _updateVectorIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _payloadIndex = this.payloadIndex(((PayloadIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _payloadIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          List<Code.Stmt> _addField = this.addField(this.member(concept, "a field"), ((Field)concept));
          Iterables.<Code.Stmt>addAll(result, _addField);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          List<Code.Stmt> _addField = this.addField(this.member(concept, "a field"), ((Field)concept));
          Iterables.<Code.Stmt>addAll(result, _addField);
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          List<Code.Stmt> _slice = this.slice(this.member(concept, "a partition"), "partition", ((Partition)concept).getName(), concept);
          Iterables.<Code.Stmt>addAll(result, _slice);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _slice = this.slice(this.member(concept, "a tenant"), "tenant", ((Tenant)concept).getName(), concept);
          Iterables.<Code.Stmt>addAll(result, _slice);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _slice = this.slice(this.member(concept, "a namespace"), "namespace", ((Namespace)concept).getKey(), concept);
          Iterables.<Code.Stmt>addAll(result, _slice);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = ElasticsearchStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + ElasticsearchStatements.NO_FILES));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          List<Code.Stmt> _reanalyze = this.reanalyze(((Analyzer)concept));
          Iterables.<Code.Stmt>addAll(result, _reanalyze);
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer == null)) {
            throw Unsupported.of("an analyzer filter outside an analyzer; a token filter belongs to the analyzer that names it");
          }
          List<Code.Stmt> _reanalyze = this.reanalyze(analyzer);
          Iterables.<Code.Stmt>addAll(result, _reanalyze);
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          String _literal = ((FieldLimit)concept).getType().getLiteral();
          String _plus = ("field limits (" + _literal);
          String _plus_1 = (_plus + "); ");
          String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
          String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
          throw Unsupported.of(_plus_3);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + ElasticsearchStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + ElasticsearchStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + ElasticsearchStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          final VectorIndex owner = EcoreUtil2.<VectorIndex>getContainerOfType(concept, VectorIndex.class);
          if ((owner == null)) {
            throw Unsupported.of("quantization outside a vector index; Elasticsearch quantizes through the index_options of a dense_vector field");
          }
          List<Code.Stmt> _updateVectorIndex = this.updateVectorIndex(owner);
          Iterables.<Code.Stmt>addAll(result, _updateVectorIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          final Cluster cluster = EcoreUtil2.<Cluster>getContainerOfType(concept, Cluster.class);
          if ((cluster == null)) {
            throw Unsupported.of(("replication outside a cluster; " + ElasticsearchStatements.ONE_CLUSTER));
          }
          List<Code.Stmt> _replicas = this.replicas(cluster, ((ReplicationConfig)concept));
          Iterables.<Code.Stmt>addAll(result, _replicas);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding of existing indices; " + ElasticsearchStatements.FIXED_SHARDS));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          final VectorCollection collection = EcoreUtil2.<VectorCollection>getContainerOfType(concept, VectorCollection.class);
          if ((collection == null)) {
            throw Unsupported.of("storage settings outside a collection; Elasticsearch sets the store type per index");
          }
          final Code.Entries settings = Code.entries();
          ElasticsearchStatements.storage(((StorageConfig)concept), settings);
          List<Code.Stmt> _reopened = this.reopened(this.schema.index(collection).name(), settings);
          Iterables.<Code.Stmt>addAll(result, _reopened);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          List<Code.Stmt> _authentication = this.authentication(((AuthConfig)concept));
          Iterables.<Code.Stmt>addAll(result, _authentication);
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          List<Code.Stmt> _role = this.role(((RoleBinding)concept));
          Iterables.<Code.Stmt>addAll(result, _role);
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(ElasticsearchStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          List<Code.Stmt> _backupSchedule = this.backupSchedule(((BackupSchedule)concept));
          Iterables.<Code.Stmt>addAll(result, _backupSchedule);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(ElasticsearchStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          final VectorCollection collection = EcoreUtil2.<VectorCollection>getContainerOfType(concept, VectorCollection.class);
          if ((collection == null)) {
            throw Unsupported.of("a collection limit outside a collection");
          }
          final Code.Entries settings = Code.entries();
          ElasticsearchStatements.collectionLimit(((CollectionLimit)concept), settings);
          String _name = this.schema.index(collection).name();
          String _plus = ("/" + _name);
          String _plus_1 = (_plus + "/_settings");
          Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, settings);
          Code.Run _run = new Code.Run(_request);
          result.add(_run);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (ElasticsearchStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The program endpoint: checked here, opened by connect(). Passwords and API keys come from the environment only.
   */
  private void connection(final Connection concept) {
    if ((((concept.getLocalPath() != null) || concept.isInMemory()) || Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED))) {
      throw Unsupported.of("embedded deployments; Elasticsearch runs as a server that the clients reach over HTTP");
    }
    Literal _token = concept.getToken();
    boolean _tripleNotEquals = (_token != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("bearer tokens; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)");
    }
    if (((concept.getPassword() instanceof StringLit) || (concept.getApiKey() instanceof StringLit))) {
      throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; Elasticsearch serves its REST API over HTTP only");
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals_1 = (_databaseName != null);
    if (_tripleNotEquals_1) {
      throw Unsupported.of("a database in the connection; Elasticsearch has no databases (logical databases are index name prefixes)");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, ElasticsearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    this.schema.note(concept, "cloud", "region", "project");
  }

  /**
   * Replicas of the indices of a cluster's collections: number_of_replicas is the replication factor minus one.
   */
  private List<Code.Stmt> replicas(final Cluster cluster, final ReplicationConfig replication) {
    List<Code.Stmt> _xblockexpression = null;
    {
      int _factor = replication.getFactor();
      boolean _lessThan = (_factor < 1);
      if (_lessThan) {
        throw Unsupported.of("a replication factor below 1");
      }
      boolean _isEmpty = cluster.getCollections().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("replication of a cluster without collections; Elasticsearch sets replicas per index");
      }
      final Function1<VectorCollection, String> _function = (VectorCollection c) -> {
        return this.schema.index(c).name();
      };
      final String names = IterableExtensions.join(ListExtensions.<VectorCollection, String>map(cluster.getCollections(), _function), ",");
      int _factor_1 = replication.getFactor();
      int _minus = (_factor_1 - 1);
      Code.Call _request = ElasticsearchBodies.request("PUT", (("/" + names) + "/_settings"), null, ElasticsearchBodies.obj("index.number_of_replicas", ElasticsearchBodies.num(_minus)));
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Authentication: username_password and api_key are the connection's; roles are role definitions; the rest needs licenses.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EAuthType _type = auth.getType();
      if (_type != null) {
        switch (_type) {
          case TOKEN:
            throw Unsupported.of("token authentication; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)");
          case MTLS:
            throw Unsupported.of("mtls authentication; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)");
          case SSO:
            throw Unsupported.of("sso authentication; the SAML and OpenID Connect realms need a platinum license (Security, SAML_REALM_FEATURE, OIDC_REALM_FEATURE)");
          default:
            break;
        }
      } else {
      }
      Literal _token = auth.getToken();
      if ((_token instanceof StringLit)) {
        throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
      }
      String _certificatePath = auth.getCertificatePath();
      boolean _tripleNotEquals = (_certificatePath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("client certificates; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)");
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; Elasticsearch encrypts at rest only through the file system or the cloud provider");
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        throw Unsupported.of("audit logs; auditing needs a gold license (Security, AUDITING_FEATURE)");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = auth.getRoles();
      for (final RoleBinding binding : _roles) {
        List<Code.Stmt> _role = this.role(binding);
        Iterables.<Code.Stmt>addAll(result, _role);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A role with index privileges on a collection's index (or on every index); PUT creates or updates it.
   */
  private List<Code.Stmt> role(final RoleBinding binding) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Code.Text _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = ElasticsearchBodies.text("*");
      } else {
        _xifexpression = ElasticsearchBodies.text(this.schema.index(binding.getCollection()).name());
      }
      final Code.Text names = _xifexpression;
      final List<Code.Expr> grants = ElasticsearchBodies.privileges(binding.getPrivileges());
      List<Code.Expr> _xifexpression_1 = null;
      boolean _isEmpty = grants.isEmpty();
      if (_isEmpty) {
        Code.Text _text = ElasticsearchBodies.text("read");
        _xifexpression_1 = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)));
      } else {
        _xifexpression_1 = grants;
      }
      Code.Items _arr = ElasticsearchBodies.arr(ElasticsearchBodies.obj("names", ElasticsearchBodies.arr(names), "privileges", ElasticsearchBodies.arr(_xifexpression_1)));
      Code.Text _xifexpression_2 = null;
      String _description = binding.getDescription();
      boolean _tripleEquals_1 = (_description == null);
      if (_tripleEquals_1) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.text(binding.getDescription());
      }
      final Code.Entries roleBody = ElasticsearchBodies.obj("indices", _arr, 
        "description", _xifexpression_2);
      String _roleName = this.roleName(binding.getRoleName());
      String _plus = ("/_security/role/" + _roleName);
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus, null, roleBody);
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private String roleName(final String declared) {
    String _xblockexpression = null;
    {
      boolean _matches = declared.matches("[A-Za-z0-9_][A-Za-z0-9_.@-]*");
      boolean _not = (!_matches);
      if (_not) {
        throw Unsupported.of((("the role name " + declared) + "; the generator writes role names of letters, digits and _ . @ -"));
      }
      _xblockexpression = declared;
    }
    return _xblockexpression;
  }

  /**
   * A snapshot lifecycle policy (SLM, basic license) on the fs repository of the compose file.
   */
  private List<Code.Stmt> backupSchedule(final BackupSchedule schedule) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String repository = this.repository(schedule.getLocation());
      final ElasticsearchIndex target = this.schema.index(schedule.getCollection());
      Code.Text _text = ElasticsearchBodies.text(ElasticsearchBodies.schedule(schedule.getInterval()));
      String _aliasName = ElasticsearchBodies.aliasName(schedule.getName());
      String _plus = ("<" + _aliasName);
      String _plus_1 = (_plus + "-{now/d}>");
      Code.Text _text_1 = ElasticsearchBodies.text(_plus_1);
      Code.Text _text_2 = ElasticsearchBodies.text(repository);
      Code.Items _arr = ElasticsearchBodies.arr(ElasticsearchBodies.text(target.name()));
      Code.Flag _flag = new Code.Flag(false);
      Code.Entries _obj = ElasticsearchBodies.obj("indices", _arr, "include_global_state", _flag);
      Code.Entries _xifexpression = null;
      boolean _written = ModelUtil.written(schedule, "retentionCount");
      if (_written) {
        _xifexpression = ElasticsearchBodies.obj("max_count", ElasticsearchBodies.num(schedule.getRetentionCount()), "min_count", ElasticsearchBodies.num(1));
      } else {
        _xifexpression = null;
      }
      final Code.Entries policy = ElasticsearchBodies.obj("schedule", _text, "name", _text_1, 
        "repository", _text_2, "config", _obj, 
        "retention", _xifexpression);
      Code.Stmt _registerRepository = this.registerRepository(repository);
      String _aliasName_1 = ElasticsearchBodies.aliasName(schedule.getName());
      String _plus_2 = ("/_slm/policy/" + _aliasName_1);
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_2, null, policy);
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_registerRepository, _run));
    }
    return _xblockexpression;
  }

  /**
   * The snapshot repository of a location: vecdsl (directory vecdsl under path.repo) by default, else the location's name.
   */
  private String repository(final String location) {
    String _xifexpression = null;
    if ((location == null)) {
      _xifexpression = "vecdsl";
    } else {
      _xifexpression = ElasticsearchBodies.aliasName(location.replaceAll("[^A-Za-z0-9]+", "_"));
    }
    return _xifexpression;
  }

  private Code.Stmt registerRepository(final String repository) {
    Code.Call _request = ElasticsearchBodies.request("PUT", ("/_snapshot/" + repository), null, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("fs"), "settings", ElasticsearchBodies.obj("location", ElasticsearchBodies.text(repository))));
    return new Code.Run(_request);
  }

  /**
   * The collection of a member concept: its container, or the collection of the enclosing create.
   */
  private VectorCollection member(final EObject concept, final String what) {
    final VectorCollection direct = ModelUtil.owningCollection(concept);
    if ((direct != null)) {
      return direct;
    }
    final Create statement = EcoreUtil2.<Create>getContainerOfType(concept, Create.class);
    EObject _xifexpression = null;
    if ((statement == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = statement.eContainer();
    }
    final EObject outer = _xifexpression;
    if ((outer instanceof Create)) {
      Concept _elvis = null;
      Concept _definition = ((Create) outer).getDefinition();
      if (_definition != null) {
        _elvis = _definition;
      } else {
        Concept _target = ((Create) outer).getTarget();
        _elvis = _target;
      }
      final Concept enclosing = _elvis;
      if ((enclosing instanceof VectorCollection)) {
        return ((VectorCollection) enclosing);
      }
    }
    throw Unsupported.of((what + " outside a collection; Elasticsearch adds it to an index"));
  }

  /**
   * A tenant, namespace or partition: a filtered alias on the slice's keyword field, which the mapping gains when missing.
   */
  private List<Code.Stmt> slice(final VectorCollection collection, final String kind, final String value, final EObject concept) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((concept instanceof Partition)) {
        ElasticsearchStatements.checkPartition(((Partition) concept));
      }
      ElasticsearchIndex target = this.schema.index(collection);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      String _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case "tenant":
            _switchResult = target.tenant();
            break;
          case "namespace":
            _switchResult = target.namespace();
            break;
          default:
            _switchResult = target.partition();
            break;
        }
      } else {
        _switchResult = target.partition();
      }
      final String fieldName = _switchResult;
      String sliceField = fieldName;
      if ((fieldName == null)) {
        boolean _equals = Objects.equals(kind, "partition");
        if (_equals) {
          throw Unsupported.of("a partition of a collection declared without partitions; declare a partition in the collection, which adds the partition field");
        }
        String _xifexpression = null;
        boolean _equals_1 = Objects.equals(kind, "tenant");
        if (_equals_1) {
          _xifexpression = ElasticsearchBodies.TENANT_FIELD;
        } else {
          _xifexpression = ElasticsearchBodies.NAMESPACE_FIELD;
        }
        sliceField = _xifexpression;
        String _name = target.name();
        String _plus = ("/" + _name);
        String _plus_1 = (_plus + "/_mapping");
        Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("properties", ElasticsearchBodies.obj(sliceField, ElasticsearchBodies.obj("type", ElasticsearchBodies.text("keyword")))));
        Code.Run _run = new Code.Run(_request);
        result.add(_run);
        ElasticsearchIndex _xifexpression_1 = null;
        boolean _equals_2 = Objects.equals(kind, "tenant");
        if (_equals_2) {
          String _name_1 = target.name();
          String _key = target.key();
          List<ElasticsearchIndex.Field> _fields = target.fields();
          String _namespace = target.namespace();
          String _partition = target.partition();
          Map<String, String> _renames = target.renames();
          String _database = target.database();
          _xifexpression_1 = new ElasticsearchIndex(_name_1, _key, _fields, sliceField, _namespace, _partition, _renames, _database);
        } else {
          String _name_2 = target.name();
          String _key_1 = target.key();
          List<ElasticsearchIndex.Field> _fields_1 = target.fields();
          String _tenant = target.tenant();
          String _partition_1 = target.partition();
          Map<String, String> _renames_1 = target.renames();
          String _database_1 = target.database();
          _xifexpression_1 = new ElasticsearchIndex(_name_2, _key_1, _fields_1, _tenant, sliceField, _partition_1, _renames_1, _database_1);
        }
        target = _xifexpression_1;
        this.schema.evolve(collection, target);
      }
      final Code.Entries alias = ElasticsearchBodies.obj("index", ElasticsearchBodies.text(target.name()), "alias", ElasticsearchBodies.text(ElasticsearchBodies.sliceAlias(target.name(), kind, value)), "filter", ElasticsearchSchema.term(sliceField, ElasticsearchBodies.text(value)));
      Code.Call _request_1 = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(ElasticsearchBodies.obj("add", alias))));
      Code.Run _run_1 = new Code.Run(_request_1);
      result.add(_run_1);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Analysis settings change on a closed index: close, update the analyzer, open again and wait until it is usable.
   */
  private List<Code.Stmt> reanalyze(final Analyzer analyzer) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = ModelUtil.owningCollection(analyzer);
      if ((collection == null)) {
        throw Unsupported.of("an analyzer outside a collection; Elasticsearch defines analyzers in the settings of an index");
      }
      final Code.Entries settings = ElasticsearchBodies.obj("analysis", this.schema.analysis(Collections.<Analyzer>unmodifiableList(CollectionLiterals.<Analyzer>newArrayList(analyzer))));
      final Code.Entries similarityMap = this.schema.similarities(Collections.<Analyzer>unmodifiableList(CollectionLiterals.<Analyzer>newArrayList(analyzer)));
      boolean _isEmpty = similarityMap.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        settings.put("similarity", similarityMap);
      }
      _xblockexpression = this.reopened(this.schema.index(collection).name(), settings);
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> reopened(final String name, final Code.Entries settings) {
    Code.Call _request = ElasticsearchBodies.request("POST", (("/" + name) + "/_close"), null, null);
    Code.Run _run = new Code.Run(_request);
    Code.Call _request_1 = ElasticsearchBodies.request("PUT", (("/" + name) + "/_settings"), null, settings);
    Code.Run _run_1 = new Code.Run(_request_1);
    Code.Call _request_2 = ElasticsearchBodies.request("POST", (("/" + name) + "/_open"), null, null);
    Code.Run _run_2 = new Code.Run(_request_2);
    Code.Call _call = ElasticsearchBodies.call("wait_health", "index", ElasticsearchBodies.text(name), "status", ElasticsearchBodies.text("yellow"), "timeout_seconds", ElasticsearchBodies.num(60));
    Code.Run _run_3 = new Code.Run(_call);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _run_2, _run_3));
  }

  /**
   * A vector index created for an existing field: its index_options in a mapping update (the server allows quantization upgrades).
   */
  private List<Code.Stmt> updateVectorIndex(final VectorIndex vectorIndex) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorField _elvis = null;
      VectorField _field = vectorIndex.getField();
      if (_field != null) {
        _elvis = _field;
      } else {
        VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(vectorIndex, VectorField.class);
        _elvis = _containerOfType;
      }
      final VectorField owner = _elvis;
      if ((owner == null)) {
        throw Unsupported.of("a vector index without a vector field");
      }
      final VectorCollection collection = this.member(owner, "a vector index");
      final ElasticsearchIndex.Field updated = this.schema.field(collection, owner);
      final ElasticsearchIndex target = this.schema.index(collection);
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_mapping");
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("properties", ElasticsearchBodies.obj(target.field(updated.name()).name(), updated.mapping())));
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A payload index on an existing field: every mapped field is indexed, so the request re-sends the field's mapping.
   */
  private List<Code.Stmt> payloadIndex(final PayloadIndex payloadIndex) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field owner = payloadIndex.getTarget().getField();
      VectorCollection _elvis = null;
      VectorCollection _targetCollection = ModelUtil.targetCollection(payloadIndex);
      if (_targetCollection != null) {
        _elvis = _targetCollection;
      } else {
        VectorCollection _xifexpression = null;
        if ((owner == null)) {
          _xifexpression = null;
        } else {
          _xifexpression = ModelUtil.owningCollection(owner);
        }
        _elvis = _xifexpression;
      }
      final VectorCollection collection = _elvis;
      if ((collection == null)) {
        throw Unsupported.of("a payload index outside a collection");
      }
      final ElasticsearchIndex target = this.schema.index(collection);
      this.schema.checkPayloadIndex(target, payloadIndex);
      if ((owner == null)) {
        throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields");
      }
      final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(owner));
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_mapping");
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("properties", ElasticsearchBodies.obj(mapped.name(), mapped.mapping())));
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A field added to an existing index; a default value is written into the documents that lack it.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field added) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      final ElasticsearchIndex.Field mapped = this.schema.field(collection, added);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_mapping");
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("properties", ElasticsearchBodies.obj(mapped.name(), mapped.mapping())));
      Code.Run _run = new Code.Run(_request);
      result.add(_run);
      Code.Expr _defaultValue = mapped.defaultValue();
      boolean _tripleNotEquals = (_defaultValue != null);
      if (_tripleNotEquals) {
        Code.Call _byQuery = this.byQuery(target, "_update_by_query", ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(mapped.name())))))), 
          ElasticsearchBodies.script(ElasticsearchBodies.DEFAULT_SCRIPT, ElasticsearchBodies.obj("field", ElasticsearchBodies.text(mapped.name()), "value", mapped.defaultValue())), "updated", "update_count", null);
        Code.Run _run_1 = new Code.Run(_byQuery);
        result.add(_run_1);
      }
      this.schema.evolve(collection, target.with(mapped));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * An update_by_query or delete_by_query request with refresh, counting the documents it changed.
   */
  private Code.Call byQuery(final ElasticsearchIndex target, final String endpoint, final Code.Entries matching, final Code.Entries painless, final String counter, final String wrap, final Integer maxDocs) {
    Code.Call _xblockexpression = null;
    {
      Code.Num _xifexpression = null;
      if ((maxDocs == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.num((maxDocs).intValue());
      }
      final Code.Entries requestBody = ElasticsearchBodies.obj("query", matching, "script", painless, "max_docs", _xifexpression);
      Code.Text _text = ElasticsearchBodies.text("POST");
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/");
      String _plus_2 = (_plus_1 + endpoint);
      Code.Text _text_1 = ElasticsearchBodies.text(_plus_2);
      Code.Entries _obj = ElasticsearchBodies.obj("refresh", ElasticsearchBodies.text("true"), "conflicts", ElasticsearchBodies.text("proceed"));
      Code.Items _arr = ElasticsearchBodies.arr(ElasticsearchBodies.text(counter));
      Code.Text _xifexpression_1 = null;
      if ((wrap == null)) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.text(wrap);
      }
      _xblockexpression = ElasticsearchBodies.call("request", "method", _text, "path", _text_1, "params", _obj, 
        "body", requestBody, "select", _arr, "wrap", _xifexpression_1);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        if (((!it.getOperations().isEmpty()) || (it.getNewName() == null))) {
          throw Unsupported.of("alter operations on an alias; an alias can be renamed (removed and added in one _aliases request)");
        }
        final Alias alias = ((Alias) concept);
        final ElasticsearchIndex aliasTarget = this.schema.index(alias.getCollection());
        Code.Call _request = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(
          ElasticsearchBodies.obj("remove", ElasticsearchBodies.obj("index", ElasticsearchBodies.text(aliasTarget.name()), "alias", ElasticsearchBodies.text(ElasticsearchBodies.aliasName(alias.getName())))), 
          ElasticsearchBodies.obj("add", ElasticsearchBodies.obj("index", ElasticsearchBodies.text(aliasTarget.name()), "alias", ElasticsearchBodies.text(ElasticsearchBodies.aliasName(it.getNewName())))))));
        Code.Run _run = new Code.Run(_request);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; Elasticsearch alters indices and aliases");
        throw Unsupported.of(_plus_1);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        List<Code.Stmt> _alter = this.alter(collection, operation);
        Iterables.<Code.Stmt>addAll(result, _alter);
      }
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        List<Code.Stmt> _rename = this.rename(collection, it.getNewName());
        Iterables.<Code.Stmt>addAll(result, _rename);
      }
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        String _name_1 = this.schema.index(collection).name();
        String _plus_2 = ("/" + _name_1);
        String _plus_3 = (_plus_2 + "/_mapping");
        Code.Call _request_1 = ElasticsearchBodies.request("GET", _plus_3, null, null);
        Code.Run _run_1 = new Code.Run(_request_1);
        result.add(_run_1);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (operation instanceof AddFieldOp) {
        _matched=true;
        _switchResult = this.addField(collection, ((AddFieldOp)operation).getNewField());
      }
      if (!_matched) {
        if (operation instanceof DropFieldOp) {
          _matched=true;
          _switchResult = this.dropField(collection, ((DropFieldOp)operation).getField());
        }
      }
      if (!_matched) {
        if (operation instanceof RenameFieldOp) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            Field _field = ((RenameFieldOp)operation).getField();
            Field _primaryKey = collection.getPrimaryKey();
            boolean _tripleEquals = (_field == _primaryKey);
            if (_tripleEquals) {
              throw Unsupported.of("renaming the primary key; the generated requests address the key by its declared name");
            }
            final ElasticsearchIndex.Field old = target.field(ModelUtil.fieldName(((RenameFieldOp)operation).getField()));
            final String newField = ElasticsearchBodies.field(((RenameFieldOp)operation).getNewName());
            final ElasticsearchIndex renamedIndex = target.renamed(ModelUtil.fieldName(((RenameFieldOp)operation).getField()), newField);
            this.schema.evolve(collection, renamedIndex);
            String _name = target.name();
            String _plus = ("/" + _name);
            String _plus_1 = (_plus + "/_mapping");
            Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("properties", ElasticsearchBodies.obj(newField, old.mapping())));
            Code.Run _run = new Code.Run(_request);
            Code.Call _byQuery = this.byQuery(target, "_update_by_query", ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(old.name()))), 
              ElasticsearchBodies.script(ElasticsearchBodies.RENAME_SCRIPT, ElasticsearchBodies.obj("from", ElasticsearchBodies.text(old.name()), "to", ElasticsearchBodies.text(newField))), "updated", "update_count", null);
            Code.Run _run_1 = new Code.Run(_byQuery);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof SetPropertyOp) {
          _matched=true;
          _switchResult = this.setProperty(target, ((SetPropertyOp)operation));
        }
      }
      if (!_matched) {
        if (operation instanceof AddAliasOp) {
          _matched=true;
          Code.Call _request = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(ElasticsearchBodies.obj("add", ElasticsearchBodies.obj("index", ElasticsearchBodies.text(target.name()), "alias", ElasticsearchBodies.text(ElasticsearchBodies.aliasName(((AddAliasOp)operation).getNewAlias().getName())))))));
          Code.Run _run = new Code.Run(_request);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = ElasticsearchStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = ElasticsearchStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (ElasticsearchStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A field leaves the documents (update_by_query removes it from _source); the mapping keeps the field, since Elasticsearch
   * cannot remove a field from a mapping.
   */
  private List<Code.Stmt> dropField(final VectorCollection collection, final Field dropped) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (dropped == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
      }
      final ElasticsearchIndex target = this.schema.index(collection);
      final ElasticsearchIndex.Field old = target.field(ModelUtil.fieldName(dropped));
      this.schema.evolve(collection, target.without(ModelUtil.fieldName(dropped)));
      Code.Call _byQuery = this.byQuery(target, "_update_by_query", ElasticsearchBodies.obj("exists", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(old.exact()))), 
        ElasticsearchBodies.script(ElasticsearchBodies.DROP_SCRIPT, ElasticsearchBodies.obj("field", ElasticsearchBodies.text(old.name()))), "updated", "update_count", null);
      Code.Run _run = new Code.Run(_byQuery);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description and tags live in the _meta of the mapping, dynamic is the mapping's dynamic setting.
   */
  private List<Code.Stmt> setProperty(final ElasticsearchIndex target, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case DESCRIPTION:
            Literal _value = operation.getValue();
            boolean _not = (!(_value instanceof StringLit));
            if (_not) {
              throw Unsupported.of("a description that is not a string literal");
            }
            String _name = target.name();
            String _plus = ("/" + _name);
            String _plus_1 = (_plus + "/_mapping");
            Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("_meta", ElasticsearchBodies.obj("description", Literals.of(operation.getValue()))));
            Code.Run _run = new Code.Run(_request);
            result.add(_run);
            break;
          case TAGS:
            Literal _value_1 = operation.getValue();
            boolean _not_1 = (!(_value_1 instanceof ListLit));
            if (_not_1) {
              throw Unsupported.of("tags that are not a list literal");
            }
            String _name_1 = target.name();
            String _plus_2 = ("/" + _name_1);
            String _plus_3 = (_plus_2 + "/_mapping");
            Code.Call _request_1 = ElasticsearchBodies.request("PUT", _plus_3, null, ElasticsearchBodies.obj("_meta", ElasticsearchBodies.obj("tags", Literals.of(operation.getValue()))));
            Code.Run _run_1 = new Code.Run(_request_1);
            result.add(_run_1);
            break;
          case DYNAMIC:
            String _name_2 = target.name();
            String _plus_4 = ("/" + _name_2);
            String _plus_5 = (_plus_4 + "/_mapping");
            String _xifexpression = null;
            if (((operation.getValue() instanceof BoolLit) && ((BoolLit) operation.getValue()).isValue())) {
              _xifexpression = "true";
            } else {
              _xifexpression = "strict";
            }
            Code.Call _request_2 = ElasticsearchBodies.request("PUT", _plus_5, null, 
              ElasticsearchBodies.obj("dynamic", ElasticsearchBodies.text(_xifexpression)));
            Code.Run _run_2 = new Code.Run(_request_2);
            result.add(_run_2);
            break;
          case CONSISTENCY:
            this.notes.add("consistency on SetPropertyOp, which every refreshed write meets");
            break;
          case TTL:
            throw Unsupported.of(ElasticsearchStatements.NO_TTL);
          case DELETION_PROTECTION:
            throw Unsupported.of(ElasticsearchStatements.NO_DELETION_PROTECTION);
          default:
            break;
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A renamed index: the write-blocked index is cloned under the new name (hard-linked segments, same mapping and settings),
   * the old index deleted and the write block lifted. Aliases declared in the collection move with it.
   */
  private List<Code.Stmt> rename(final VectorCollection collection, final String newName) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      final ElasticsearchIndex renamedIndex = target.named(ElasticsearchBodies.indexName(target.database(), newName));
      final Code.Entries aliases = Code.entries();
      EList<Alias> _aliases = collection.getAliases();
      for (final Alias alias : _aliases) {
        aliases.put(ElasticsearchBodies.aliasName(alias.getName()), Code.entries());
      }
      this.schema.evolve(collection, renamedIndex);
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_settings");
      Code.Flag _flag = new Code.Flag(true);
      Code.Call _request = ElasticsearchBodies.request("PUT", _plus_1, null, ElasticsearchBodies.obj("index.blocks.write", _flag));
      Code.Run _run = new Code.Run(_request);
      String _name_1 = target.name();
      String _plus_2 = ("/" + _name_1);
      String _plus_3 = (_plus_2 + "/_clone/");
      String _name_2 = renamedIndex.name();
      String _plus_4 = (_plus_3 + _name_2);
      Code.Call _request_1 = ElasticsearchBodies.request("POST", _plus_4, null, ElasticsearchBodies.obj("aliases", aliases));
      Code.Run _run_1 = new Code.Run(_request_1);
      String _name_3 = target.name();
      String _plus_5 = ("/" + _name_3);
      Code.Call _request_2 = ElasticsearchBodies.request("DELETE", _plus_5, null, null);
      Code.Run _run_2 = new Code.Run(_request_2);
      String _name_4 = renamedIndex.name();
      String _plus_6 = ("/" + _name_4);
      String _plus_7 = (_plus_6 + "/_settings");
      Code.Nil _nil = new Code.Nil();
      Code.Call _request_3 = ElasticsearchBodies.request("PUT", _plus_7, null, ElasticsearchBodies.obj("index.blocks.write", _nil));
      Code.Run _run_3 = new Code.Run(_request_3);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _run_2, _run_3));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Drop it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      Code.Items _xifexpression = null;
      boolean _isIfExists = it.isIfExists();
      if (_isIfExists) {
        _xifexpression = ElasticsearchBodies.arr(ElasticsearchBodies.num(404));
      } else {
        _xifexpression = null;
      }
      final Code.Items ignore = _xifexpression;
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (concept instanceof VectorCollection) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          final ElasticsearchIndex dropped = this.schema.index(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          String _name = dropped.name();
          String _plus = ("/" + _name);
          Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("DELETE"), "path", ElasticsearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          Code.Call _call = ElasticsearchBodies.call("drop_matching", "pattern", ElasticsearchBodies.text(ElasticsearchBodies.indexName(((Database)concept).getName(), "x").replaceFirst("x$", "*")));
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), "tenant", ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), "namespace", ((Namespace)concept).getKey());
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a partition"), "partition", ((Partition)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          String _aliasName = ElasticsearchBodies.aliasName(((Alias)concept).getName());
          String _plus = ("/*,-.*/_alias/" + _aliasName);
          Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("DELETE"), "path", ElasticsearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of("dropping a vector index; the index_options of a dense_vector field are part of its mapping, which Elasticsearch cannot remove");
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of("dropping a payload index; whether a field is indexed is part of its mapping, which Elasticsearch cannot change");
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = ElasticsearchStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; Elasticsearch upgrades index_options (hnsw to int8_hnsw to int4_hnsw to bbq_hnsw) but cannot return to raw vectors");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not an Elasticsearch object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of(("dropping a deployment; " + ElasticsearchStatements.ONE_CLUSTER));
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + ElasticsearchStatements.ONE_CLUSTER));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; analysis settings are removed with their index, not while fields use them");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; analysis settings are removed with their index, not while fields use them");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          String _literal = ((FieldLimit)concept).getType().getLiteral();
          String _plus = ("field limits (" + _literal);
          String _plus_1 = (_plus + "); ");
          String _plus_2 = (_plus_1 + ElasticsearchStatements.PENDING);
          String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
          throw Unsupported.of(_plus_3);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + ElasticsearchStatements.NO_FILES));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + ElasticsearchStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + ElasticsearchStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + ElasticsearchStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of("dropping replication; set the replication factor 1 instead (number_of_replicas 0)");
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("dropping sharding; " + ElasticsearchStatements.FIXED_SHARDS));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("dropping storage settings; the store type of an index is fixed while it is open");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; security is configured on the server (xpack.security.enabled)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          String _roleName = this.roleName(((RoleBinding)concept).getRoleName());
          String _plus = ("/_security/role/" + _roleName);
          Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("DELETE"), "path", ElasticsearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(ElasticsearchStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          String _aliasName = ElasticsearchBodies.aliasName(((BackupSchedule)concept).getName());
          String _plus = ("/_slm/policy/" + _aliasName);
          Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("DELETE"), "path", ElasticsearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(ElasticsearchStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final VectorCollection collection = EcoreUtil2.<VectorCollection>getContainerOfType(concept, VectorCollection.class);
            if (((collection == null) || (!Objects.equals(((CollectionLimit)concept).getType().getLiteral(), "max_fields")))) {
              String _literal = ((CollectionLimit)concept).getType().getLiteral();
              String _plus = ("collection limits (" + _literal);
              String _plus_1 = (_plus + "); Elasticsearch limits the fields of a mapping only");
              throw Unsupported.of(_plus_1);
            }
            String _name = this.schema.index(collection).name();
            String _plus_2 = ("/" + _name);
            String _plus_3 = (_plus_2 + "/_settings");
            Code.Nil _nil = new Code.Nil();
            Code.Call _request = ElasticsearchBodies.request("PUT", _plus_3, null, ElasticsearchBodies.obj("index.mapping.total_fields.limit", _nil));
            Code.Run _run = new Code.Run(_request);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((ElasticsearchStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant, namespace or partition deletes its documents and its filtered alias.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String kind, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      String _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case "tenant":
            _switchResult = target.tenant();
            break;
          case "namespace":
            _switchResult = target.namespace();
            break;
          default:
            _switchResult = target.partition();
            break;
        }
      } else {
        _switchResult = target.partition();
      }
      final String fieldName = _switchResult;
      if ((fieldName == null)) {
        throw Unsupported.of((((("a " + kind) + " of a collection without ") + kind) + "s"));
      }
      Code.Text _text = ElasticsearchBodies.text(target.name());
      Code.Text _text_1 = ElasticsearchBodies.text(ElasticsearchBodies.sliceAlias(target.name(), kind, value));
      Code.Flag _flag = new Code.Flag(false);
      Code.Call _request = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(ElasticsearchBodies.obj("remove", ElasticsearchBodies.obj("index", _text, "alias", _text_1, 
        "must_exist", _flag)))));
      Code.Run _run = new Code.Run(_request);
      Code.Call _byQuery = this.byQuery(target, "_delete_by_query", ElasticsearchSchema.term(fieldName, ElasticsearchBodies.text(value)), null, "deleted", "delete_count", null);
      Code.Return _return = new Code.Return(_byQuery);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _return));
    }
    return _xblockexpression;
  }

  /**
   * An alias moves between indices in one atomic _aliases request.
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String aliasValue = ElasticsearchBodies.aliasName(it.getAlias().getName());
      Code.Text _text = ElasticsearchBodies.text(this.schema.index(it.getOldCollection()).name());
      Code.Text _text_1 = ElasticsearchBodies.text(aliasValue);
      Code.Flag _flag = new Code.Flag(false);
      Code.Call _request = ElasticsearchBodies.request("POST", "/_aliases", null, ElasticsearchBodies.obj("actions", ElasticsearchBodies.arr(
        ElasticsearchBodies.obj("remove", ElasticsearchBodies.obj("index", _text, "alias", _text_1, "must_exist", _flag)), 
        ElasticsearchBodies.obj("add", ElasticsearchBodies.obj("index", ElasticsearchBodies.text(this.schema.index(it.getNewCollection()).name()), "alias", ElasticsearchBodies.text(aliasValue))))));
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(ElasticsearchStatements.NO_TTL);
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "onConflict");
      if (_written_1) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      Code.Call _writeCall = this.writeCall(_collection, _source, _xifexpression, null, it);
      Code.Return _return = new Code.Return(_writeCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(ElasticsearchStatements.NO_TTL);
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      Integer _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        _xifexpression = Integer.valueOf(it.getBatchSize());
      } else {
        _xifexpression = null;
      }
      Code.Call _writeCall = this.writeCall(_collection, _source, "replace", _xifexpression, it);
      Code.Return _return = new Code.Return(_writeCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * One _bulk write of the records with the slice fields of the statement and the defaults of the index.
   */
  private Code.Call writeCall(final VectorCollection collection, final RecordSource recordSource, final String policy, final Integer batch, final EObject statement) {
    Code.Call _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      final Map<String, String> mode = ElasticsearchBodies.writeMode(policy);
      Code.Text _text = ElasticsearchBodies.text(target.name());
      Code.Expr _recordsOf = this.recordsOf(recordSource, target);
      Code.Text _text_1 = ElasticsearchBodies.text(target.key());
      Code.Text _text_2 = ElasticsearchBodies.text(mode.get("op"));
      Code.Text _text_3 = ElasticsearchBodies.text(mode.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(statement, collection, target);
      Code.Entries _defaultsOf = this.defaultsOf(target);
      Code.Num _xifexpression = null;
      if ((batch == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.num((batch).intValue());
      }
      Code.Flag _xifexpression_1 = null;
      boolean _containsKey = mode.containsKey("ignore");
      if (_containsKey) {
        _xifexpression_1 = new Code.Flag(true);
      } else {
        _xifexpression_1 = null;
      }
      _xblockexpression = ElasticsearchBodies.call("write", "index", _text, "records", _recordsOf, "key", _text_1, "op", _text_2, 
        "counter", _text_3, "extra", _sliceFields, "defaults", _defaultsOf, 
        "batch_size", _xifexpression, "ignore_conflicts", _xifexpression_1);
    }
    return _xblockexpression;
  }

  private Code.Entries defaultsOf(final ElasticsearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final Map<String, Code.Expr> values = target.defaults();
      Code.Entries _xifexpression = null;
      boolean _isEmpty = values.isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = Code.entries(values);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * The slice fields of a write: tenant, namespace and partition; partitioned collections default to the default partition.
   */
  private Code.Entries sliceFields(final EObject statement, final VectorCollection collection, final ElasticsearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.put(target.tenant(), ElasticsearchBodies.text(tenant.getName()));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        result.put(target.namespace(), ElasticsearchBodies.text(((String) namespace)));
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_1);
      if (((partition != null) && (target.partition() == null))) {
        throw Unsupported.of("a partition of a collection declared without partitions");
      }
      String _partition = target.partition();
      boolean _tripleNotEquals = (_partition != null);
      if (_tripleNotEquals) {
        String _elvis = null;
        String _elvis_1 = null;
        String _name = null;
        if (partition!=null) {
          _name=partition.getName();
        }
        if (_name != null) {
          _elvis_1 = _name;
        } else {
          Partition _defaultPartition = collection.getDefaultPartition();
          String _name_1 = null;
          if (_defaultPartition!=null) {
            _name_1=_defaultPartition.getName();
          }
          _elvis_1 = _name_1;
        }
        if (_elvis_1 != null) {
          _elvis = _elvis_1;
        } else {
          _elvis = ElasticsearchBodies.DEFAULT_PARTITION;
        }
        result.put(target.partition(), ElasticsearchBodies.text(_elvis));
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

  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if ((((((((((it.getFilter() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty())) || ModelUtil.written(it, "ifVersion")) || (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) || (it.getTenant() != null)) || (it.getNamespace() != null)) || (it.getPartition() != null))) {
          throw Unsupported.of("update from records with a filter, a slice, field clauses, if-version or mode replace; bulk update operations address documents by id, and each record carries the values it merges");
        }
        final Function1<Literal, Code.Expr> _function = (Literal i) -> {
          return Literals.of(i);
        };
        Code.Call _call = ElasticsearchBodies.call("write", "index", ElasticsearchBodies.text(target.name()), "records", this.recordsOf(it.getSource(), target), "key", ElasticsearchBodies.text(target.key()), "op", ElasticsearchBodies.text("patch"), 
          "counter", ElasticsearchBodies.text("update_count"), "only", Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function))));
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      final Code.Entries assignments = Code.entries();
      final Code.Entries appended = Code.entries();
      final Code.Entries pulled = Code.entries();
      final List<Code.Expr> removed = CollectionLiterals.<Code.Expr>newArrayList();
      final List<String> given = CollectionLiterals.<String>newArrayList();
      RecordLiteral _values = it.getValues();
      boolean _tripleNotEquals_1 = (_values != null);
      if (_tripleNotEquals_1) {
        EList<RecordEntry> _entries = it.getValues().getEntries();
        for (final RecordEntry entry : _entries) {
          {
            Field _field = entry.getField();
            Field _primaryKey = it.getCollection().getPrimaryKey();
            boolean _tripleEquals = (_field == _primaryKey);
            if (_tripleEquals) {
              throw Unsupported.of("updating the primary key; the key is the document id");
            }
            final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(entry.getField()));
            String _name = mapped.name();
            given.add(_name);
            final Code.Expr converted = this.convert(entry.getValue(), mapped);
            boolean _contains = it.getAppendFields().contains(entry.getField());
            if (_contains) {
              appended.put(mapped.name(), this.listOf(converted));
            } else {
              boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
              if (_contains_1) {
                pulled.put(mapped.name(), this.listOf(converted));
              } else {
                assignments.put(mapped.name(), converted);
              }
            }
          }
        }
      }
      EList<Field> _appendFields = it.getAppendFields();
      EList<Field> _arrayRemoveFields = it.getArrayRemoveFields();
      Iterable<Field> _plus = Iterables.<Field>concat(_appendFields, _arrayRemoveFields);
      for (final Field arrayField : _plus) {
        boolean _contains = given.contains(target.field(ModelUtil.fieldName(arrayField)).name());
        boolean _not = (!_contains);
        if (_not) {
          String _fieldName = ModelUtil.fieldName(arrayField);
          String _plus_1 = ("array-append or array-remove on " + _fieldName);
          String _plus_2 = (_plus_1 + " without items in \'set\'");
          throw Unsupported.of(_plus_2);
        }
      }
      EList<Field> _removedFields = it.getRemovedFields();
      for (final Field removedField : _removedFields) {
        {
          Field _primaryKey = it.getCollection().getPrimaryKey();
          boolean _tripleEquals = (removedField == _primaryKey);
          if (_tripleEquals) {
            throw Unsupported.of("removing the primary key");
          }
          Code.Text _text = ElasticsearchBodies.text(target.field(ModelUtil.fieldName(removedField)).name());
          removed.add(_text);
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        List<ElasticsearchIndex.Field> _fields = target.fields();
        for (final ElasticsearchIndex.Field mapped : _fields) {
          if (((((!Objects.equals(mapped.name(), target.key())) && (mapped.vector() == null)) && (!given.contains(mapped.name()))) && (!removed.contains(ElasticsearchBodies.text(mapped.name()))))) {
            Code.Text _text = ElasticsearchBodies.text(mapped.name());
            removed.add(_text);
          }
        }
      }
      if ((((assignments.entries().isEmpty() && appended.entries().isEmpty()) && pulled.entries().isEmpty()) && removed.isEmpty())) {
        throw Unsupported.of("an update without \'set\' or \'remove-fields\'");
      }
      final Code.Entries painless = ElasticsearchBodies.script(ElasticsearchBodies.UPDATE_SCRIPT, ElasticsearchBodies.obj("set", assignments, "remove", ElasticsearchBodies.arr(removed), "append", appended, "pull", pulled));
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        if ((((((it.getIds().size() != 1) || (it.getFilter() != null)) || (it.getTenant() != null)) || (it.getNamespace() != null)) || (it.getPartition() != null))) {
          throw Unsupported.of("if-version on other than one id; optimistic concurrency control (if_seq_no and if_primary_term) guards one document");
        }
        Code.Call _call_1 = ElasticsearchBodies.call("update_if_version", "index", ElasticsearchBodies.text(target.name()), "value", Literals.of(IterableExtensions.<Literal>head(it.getIds())), "seq_no", ElasticsearchBodies.num(it.getIfVersion()), "body", ElasticsearchBodies.obj("script", painless));
        Code.Return _return_1 = new Code.Return(_call_1);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      Code.Call _byQuery = this.byQuery(target, "_update_by_query", this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), this.idsOf(it.getIds()))), painless, "updated", "update_count", null);
      Code.Return _return_2 = new Code.Return(_byQuery);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      final List<Code.Expr> matching = this.clauses(it, it.getCollection(), target, it.getFilter(), this.idsOf(it.getIds()));
      if (((it.getIds().isEmpty() && (it.getFilter() == null)) && (!it.isDeleteAll()))) {
        throw Unsupported.of("a delete without ids, filter or \'all\'");
      }
      Code.Entries _boolOf = this.boolOf(matching);
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getDeleteLimit());
      } else {
        _xifexpression = null;
      }
      Code.Call _byQuery = this.byQuery(target, "_delete_by_query", _boolOf, null, "deleted", "delete_count", _xifexpression);
      Code.Return _return = new Code.Return(_byQuery);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The runtime reads a local JSON or JSON Lines file and writes it with _bulk requests of batch-size records.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Elasticsearch pulls no streams itself (Logstash and Elastic Agent are separate programs)");
      }
      String _xifexpression = null;
      boolean _contains = it.getLocation().contains("://");
      if (_contains) {
        _xifexpression = it.getLocation().substring(0, it.getLocation().indexOf("://"));
      } else {
        _xifexpression = "file";
      }
      final String scheme = _xifexpression;
      boolean _notEquals = (!Objects.equals(scheme, "file"));
      if (_notEquals) {
        throw Unsupported.of((("import from " + scheme) + "; Elasticsearch indexes documents that a client sends, and reads object stores only as snapshot repositories"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + ElasticsearchStatements.PENDING) + " (CSV records in the runtime)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the _bulk API takes JSON documents");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async");
      Set<String> _keySet = ModelUtil.extParams(it, ElasticsearchGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final Map<String, String> writeModeOf = ElasticsearchBodies.writeMode(_xifexpression_1);
      Code.Text _text = ElasticsearchBodies.text(it.getLocation());
      Code.Text _text_1 = ElasticsearchBodies.text(target.name());
      Code.Text _text_2 = ElasticsearchBodies.text(target.key());
      Code.Text _text_3 = ElasticsearchBodies.text(writeModeOf.get("op"));
      Code.Text _text_4 = ElasticsearchBodies.text(writeModeOf.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(it, it.getCollection(), target);
      Code.Entries _defaultsOf = this.defaultsOf(target);
      Code.Num _xifexpression_2 = null;
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        _xifexpression_2 = ElasticsearchBodies.num(it.getBatchSize());
      } else {
        _xifexpression_2 = null;
      }
      Code.Flag _xifexpression_3 = null;
      boolean _containsKey = writeModeOf.containsKey("ignore");
      if (_containsKey) {
        _xifexpression_3 = new Code.Flag(true);
      } else {
        _xifexpression_3 = null;
      }
      Code.Call _call = ElasticsearchBodies.call("import_file", "location", _text, "index", _text_1, "key", _text_2, "op", _text_3, 
        "counter", _text_4, "extra", _sliceFields, "defaults", _defaultsOf, 
        "batch_size", _xifexpression_2, "ignore_conflicts", _xifexpression_3);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of(ElasticsearchStatements.NO_TRANSACTIONS);
  }

  /**
   * Every write carries refresh=true, so 'wait' needs no call; write consistency is the server's (wait_for_active_shards 1).
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, ElasticsearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("loading partitions; Elasticsearch opens and closes whole indices");
      }
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_open");
      Code.Call _request = ElasticsearchBodies.request("POST", _plus_1, null, null);
      Code.Run _run = new Code.Run(_request);
      Code.Call _call = ElasticsearchBodies.call("wait_health", "index", ElasticsearchBodies.text(target.name()), "status", ElasticsearchBodies.text("yellow"), "timeout_seconds", ElasticsearchBodies.num(60));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("releasing partitions; Elasticsearch opens and closes whole indices");
      }
      String _name = this.schema.index(it.getCollection()).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_close");
      Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("POST"), "path", ElasticsearchBodies.text(_plus_1), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("acknowledged")));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      String _name = this.schema.index(it.getCollection()).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_flush");
      Code.Call _request = ElasticsearchBodies.request("POST", _plus_1, null, null);
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      Code.Num _xifexpression = null;
      boolean _written = ModelUtil.written(it, "targetSegments");
      if (_written) {
        _xifexpression = ElasticsearchBodies.num(it.getTargetSegments());
      } else {
        _xifexpression = null;
      }
      Code.Text _xifexpression_1 = null;
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        _xifexpression_1 = ElasticsearchBodies.text("false");
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries forceMergeParams = ElasticsearchBodies.obj("max_num_segments", _xifexpression, 
        "wait_for_completion", _xifexpression_1);
      String _name = this.schema.index(it.getCollection()).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_forcemerge");
      Code.Call _request = ElasticsearchBodies.request("POST", _plus_1, forceMergeParams, null);
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      final Code.Call info = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("GET"), "path", ElasticsearchBodies.text(_plus), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text(target.name())));
      if (((!it.isIncludeStats()) && (!it.isIncludeIndexProgress()))) {
        Code.Return _return = new Code.Return(info);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      final List<Code.Expr> parts = CollectionLiterals.<Code.Expr>newArrayList();
      parts.add(info);
      boolean _isIncludeStats = it.isIncludeStats();
      if (_isIncludeStats) {
        String _name_1 = target.name();
        String _plus_1 = ("/" + _name_1);
        String _plus_2 = (_plus_1 + "/_stats");
        Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("GET"), "path", ElasticsearchBodies.text(_plus_2), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("_all"), ElasticsearchBodies.text("primaries")));
        parts.add(_call);
      }
      boolean _isIncludeIndexProgress = it.isIncludeIndexProgress();
      if (_isIncludeIndexProgress) {
        String _name_2 = target.name();
        String _plus_3 = ("/_cluster/health/" + _name_2);
        Code.Call _call_1 = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("GET"), "path", ElasticsearchBodies.text(_plus_3), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("status")));
        parts.add(_call_1);
      }
      Code.Items _items = Code.items(parts);
      Code.Return _return_1 = new Code.Return(_items);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + ElasticsearchStatements.NO_MODELS));
  }

  /**
   * A snapshot of the index into the fs repository (path.repo); Elasticsearch snapshots are incremental by design.
   */
  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      Tenant _tenant = it.getTenant();
      boolean _tripleNotEquals = (_tenant != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("tenant snapshots; a snapshot holds whole indices");
      }
      if (((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope().getLiteral(), "collection"))) && (!Objects.equals(it.getScope().getLiteral(), "full")))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("snapshots of a " + _literal);
        String _plus_1 = (_plus + "; a snapshot holds whole indices or the cluster");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "incremental");
      final String repo = this.repository(it.getLocation());
      Code.Text _xifexpression = null;
      if ((ModelUtil.written(it, "scope") && Objects.equals(it.getScope().getLiteral(), "full"))) {
        _xifexpression = ElasticsearchBodies.text("*");
      } else {
        _xifexpression = ElasticsearchBodies.text(this.schema.index(it.getCollection()).name());
      }
      final Code.Text indices = _xifexpression;
      Code.Flag _flag = new Code.Flag(false);
      Code.Entries _xifexpression_1 = null;
      String _description = it.getDescription();
      boolean _tripleEquals = (_description == null);
      if (_tripleEquals) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.obj("description", ElasticsearchBodies.text(it.getDescription()));
      }
      final Code.Entries snapshotBody = ElasticsearchBodies.obj("indices", indices, "include_global_state", _flag, 
        "metadata", _xifexpression_1);
      Code.Stmt _registerRepository = this.registerRepository(repo);
      String _snapshotOf = this.snapshotOf(it);
      String _plus_2 = ((("/_snapshot/" + repo) + "/") + _snapshotOf);
      Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("PUT"), "path", ElasticsearchBodies.text(_plus_2), 
        "params", ElasticsearchBodies.obj("wait_for_completion", ElasticsearchBodies.text("true")), "body", snapshotBody, "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("snapshot"), ElasticsearchBodies.text("state")));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_registerRepository, _return));
    }
    return _xblockexpression;
  }

  private String snapshotOf(final Snapshot snapshot) {
    String _elvis = null;
    String _snapshotName = snapshot.getSnapshotName();
    if (_snapshotName != null) {
      _elvis = _snapshotName;
    } else {
      String _name = this.schema.index(snapshot.getCollection()).name();
      String _plus = (_name + "-snapshot");
      _elvis = _plus;
    }
    return ElasticsearchBodies.aliasName(_elvis);
  }

  /**
   * Restore closes the index (a restore replaces a closed index) and reopens it from the snapshot.
   */
  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope().getLiteral(), "collection")))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("restoring a " + _literal);
        String _plus_1 = (_plus + "; the generator restores the index of the collection");
        throw Unsupported.of(_plus_1);
      }
      final String repo = this.repository(it.getLocation());
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus_2 = ("/" + _name);
      String _plus_3 = (_plus_2 + "/_close");
      Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("POST"), "path", ElasticsearchBodies.text(_plus_3), "ignore", ElasticsearchBodies.arr(ElasticsearchBodies.num(404)));
      Code.Run _run = new Code.Run(_call);
      Code.Text _text = ElasticsearchBodies.text("POST");
      String _aliasName = ElasticsearchBodies.aliasName(it.getSnapshotName());
      String _plus_4 = ((("/_snapshot/" + repo) + "/") + _aliasName);
      String _plus_5 = (_plus_4 + "/_restore");
      Code.Text _text_1 = ElasticsearchBodies.text(_plus_5);
      String _xifexpression = null;
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        _xifexpression = "false";
      } else {
        _xifexpression = "true";
      }
      Code.Entries _obj = ElasticsearchBodies.obj("wait_for_completion", ElasticsearchBodies.text(_xifexpression));
      Code.Text _text_2 = ElasticsearchBodies.text(target.name());
      Code.Flag _flag = new Code.Flag(false);
      Code.Entries _obj_1 = ElasticsearchBodies.obj("indices", _text_2, "include_global_state", _flag);
      Code.Items _xifexpression_1 = null;
      boolean _isAsync_1 = it.isAsync();
      if (_isAsync_1) {
        _xifexpression_1 = ElasticsearchBodies.arr(ElasticsearchBodies.text("accepted"));
      } else {
        _xifexpression_1 = ElasticsearchBodies.arr(ElasticsearchBodies.text("snapshot"), ElasticsearchBodies.text("indices"));
      }
      Code.Call _call_1 = ElasticsearchBodies.call("request", "method", _text, "path", _text_1, 
        "params", _obj, "body", _obj_1, 
        "select", _xifexpression_1);
      Code.Return _return = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    String _repository = this.repository(it.getLocation());
    String _plus = ("/_snapshot/" + _repository);
    String _plus_1 = (_plus + "/");
    String _aliasName = ElasticsearchBodies.aliasName(it.getSnapshotName());
    String _plus_2 = (_plus_1 + _aliasName);
    Code.Call _request = ElasticsearchBodies.request("DELETE", _plus_2, null, null);
    Code.Run _run = new Code.Run(_request);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  /**
   * _clone copies a write-blocked index (hard-linked segments, same mapping and settings); both blocks are lifted after.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("cloning a deployment; " + ElasticsearchStatements.ONE_CLUSTER));
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = ElasticsearchStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      this.operationExtensions(it);
      this.schema.note(it, "mode");
      final String original = this.schema.index(it.getSourceCollection()).name();
      final String copy = ElasticsearchBodies.indexName(this.schema.index(it.getSourceCollection()).database(), it.getTargetName());
      Code.Flag _flag = new Code.Flag(true);
      Code.Call _request = ElasticsearchBodies.request("PUT", (("/" + original) + "/_settings"), null, ElasticsearchBodies.obj("index.blocks.write", _flag));
      Code.Run _run = new Code.Run(_request);
      Code.Call _request_1 = ElasticsearchBodies.request("POST", ((("/" + original) + "/_clone/") + copy), null, null);
      Code.Run _run_1 = new Code.Run(_request_1);
      Code.Nil _nil = new Code.Nil();
      Code.Call _request_2 = ElasticsearchBodies.request("PUT", (("/" + original) + "/_settings"), null, ElasticsearchBodies.obj("index.blocks.write", _nil));
      Code.Run _run_2 = new Code.Run(_request_2);
      Code.Nil _nil_1 = new Code.Nil();
      Code.Call _request_3 = ElasticsearchBodies.request("PUT", (("/" + copy) + "/_settings"), null, ElasticsearchBodies.obj("index.blocks.write", _nil_1));
      Code.Run _run_3 = new Code.Run(_request_3);
      Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("POST"), "path", ElasticsearchBodies.text((("/" + copy) + "/_count")), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("count")), "wrap", ElasticsearchBodies.text("copied"));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _run_2, _run_3, _return));
    }
    return _xblockexpression;
  }

  /**
   * Elasticsearch rebuilds HNSW graphs when it merges segments: a force merge into one segment rebuilds the index's graph.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      this.schema.note(it, "concurrently");
      final VectorCollection collection = this.indexCollection(it.getIndex());
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      String _name = this.schema.index(collection).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_forcemerge");
      Code.Call _request = ElasticsearchBodies.request("POST", _plus_1, ElasticsearchBodies.obj("max_num_segments", ElasticsearchBodies.num(1)), null);
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(ElasticsearchStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        String _namespace_1 = target.namespace();
        boolean _tripleEquals = (_namespace_1 == null);
        if (_tripleEquals) {
          throw Unsupported.of("a namespace of a collection without namespaces");
        }
        Code.Call _byQuery = this.byQuery(target, "_delete_by_query", ElasticsearchSchema.term(target.namespace(), ElasticsearchBodies.text(it.getNamespace())), null, "deleted", "delete_count", null);
        Code.Return _return = new Code.Return(_byQuery);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      Code.Call _byQuery_1 = this.byQuery(target, "_delete_by_query", ElasticsearchBodies.obj("match_all", Code.entries()), null, "deleted", "delete_count", null);
      Code.Return _return_1 = new Code.Return(_byQuery_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept owner = it.getScope();
      Code.Num _xifexpression = null;
      boolean _written = ModelUtil.written(it, "listLimit");
      if (_written) {
        _xifexpression = ElasticsearchBodies.num(it.getListLimit());
      } else {
        _xifexpression = null;
      }
      final Code.Num limit = _xifexpression;
      Code.Text _xifexpression_1 = null;
      String _prefix = it.getPrefix();
      boolean _tripleEquals = (_prefix == null);
      if (_tripleEquals) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.text(it.getPrefix());
      }
      final Code.Text prefixValue = _xifexpression_1;
      Code.Call _switchResult = null;
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            Code.Call _xifexpression_2 = null;
            if ((owner instanceof Database)) {
              Code.Call _xblockexpression_1 = null;
              {
                final String database = ((Database) owner).getName();
                final String pattern = ElasticsearchBodies.indexName(database, "x").replaceFirst("x$", "");
                _xblockexpression_1 = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("indices"), "path", ElasticsearchBodies.text((("/_cat/indices/" + pattern) + "*")), "strip", ElasticsearchBodies.text(pattern), "prefix", prefixValue, "limit", limit);
              }
              _xifexpression_2 = _xblockexpression_1;
            } else {
              Code.Call _xblockexpression_2 = null;
              {
                if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                  String _name = owner.eClass().getName();
                  String _plus = ("listing collections in a " + _name);
                  String _plus_1 = (_plus + "; Elasticsearch lists the indices of a cluster");
                  throw Unsupported.of(_plus_1);
                }
                _xblockexpression_2 = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("indices"), "path", ElasticsearchBodies.text("/_cat/indices"), "prefix", prefixValue, "limit", limit);
              }
              _xifexpression_2 = _xblockexpression_2;
            }
            _switchResult = _xifexpression_2;
            break;
          case DATABASES:
            Code.Call _xblockexpression_3 = null;
            {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing databases in a " + _name);
                throw Unsupported.of(_plus);
              }
              _xblockexpression_3 = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("databases"), "path", ElasticsearchBodies.text("/_cat/indices"), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_3;
            break;
          case INDEXES:
            String _name = this.scopeIndex(owner, "indexes").name();
            String _plus = ("/" + _name);
            String _plus_1 = (_plus + "/_mapping");
            _switchResult = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("vector_fields"), "path", ElasticsearchBodies.text(_plus_1), "prefix", prefixValue, "limit", limit);
            break;
          case ALIASES:
            Code.Call _xblockexpression_4 = null;
            {
              String _xifexpression_3 = null;
              if ((owner == null)) {
                _xifexpression_3 = "/_alias";
              } else {
                String _name_1 = this.scopeIndex(owner, "aliases").name();
                String _plus_2 = ("/" + _name_1);
                _xifexpression_3 = (_plus_2 + "/_alias");
              }
              final String path = _xifexpression_3;
              _xblockexpression_4 = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("aliases"), "path", ElasticsearchBodies.text(path), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_4;
            break;
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            Code.Call _xblockexpression_5 = null;
            {
              final ElasticsearchIndex target = this.scopeIndex(owner, it.getKind().getLiteral());
              String _switchResult_1 = null;
              EListKind _kind_1 = it.getKind();
              if (_kind_1 != null) {
                switch (_kind_1) {
                  case TENANTS:
                    _switchResult_1 = target.tenant();
                    break;
                  case NAMESPACES:
                    _switchResult_1 = target.namespace();
                    break;
                  default:
                    _switchResult_1 = target.partition();
                    break;
                }
              } else {
                _switchResult_1 = target.partition();
              }
              final String fieldName = _switchResult_1;
              if ((fieldName == null)) {
                String _literal = it.getKind().getLiteral();
                String _plus_2 = ("listing " + _literal);
                String _plus_3 = (_plus_2 + " of a collection without ");
                String _literal_1 = it.getKind().getLiteral();
                String _plus_4 = (_plus_3 + _literal_1);
                throw Unsupported.of(_plus_4);
              }
              String _name_1 = target.name();
              String _plus_5 = ("/" + _name_1);
              String _plus_6 = (_plus_5 + "/_search");
              _xblockexpression_5 = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("terms"), "path", ElasticsearchBodies.text(_plus_6), 
                "body", ElasticsearchBodies.obj("size", ElasticsearchBodies.num(0), "aggs", ElasticsearchBodies.obj("names", ElasticsearchBodies.obj("terms", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(fieldName), "size", ElasticsearchBodies.num(ElasticsearchBodies.MAX_CANDIDATES))))), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_5;
            break;
          case SNAPSHOTS:
            _switchResult = ElasticsearchBodies.call("names", "kind", ElasticsearchBodies.text("snapshots"), "path", ElasticsearchBodies.text("/_snapshot/vecdsl/_all"), "prefix", prefixValue, "limit", limit, "ignore", ElasticsearchBodies.arr(ElasticsearchBodies.num(404)));
            break;
          default:
            break;
        }
      }
      final Code.Call listing = _switchResult;
      Code.Return _return = new Code.Return(listing);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private ElasticsearchIndex scopeIndex(final Concept owner, final String what) {
    ElasticsearchIndex _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.index(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * Elasticsearch builds vector indexes with each segment, so awaiting one waits for the index's health.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state");
      final VectorCollection collection = this.indexCollection(it.getIndex());
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "timeoutSeconds");
      if (_written) {
        _xifexpression = it.getTimeoutSeconds();
      } else {
        _xifexpression = 60;
      }
      Code.Call _call = ElasticsearchBodies.call("wait_health", "index", ElasticsearchBodies.text(this.schema.index(collection).name()), "status", ElasticsearchBodies.text("yellow"), 
        "timeout_seconds", ElasticsearchBodies.num(_xifexpression));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private VectorCollection indexCollection(final Index declaredIndex) {
    VectorCollection _switchResult = null;
    boolean _matched = false;
    if (declaredIndex instanceof VectorIndex) {
      _matched=true;
      VectorCollection _xblockexpression = null;
      {
        VectorField _elvis = null;
        VectorField _field = ((VectorIndex)declaredIndex).getField();
        if (_field != null) {
          _elvis = _field;
        } else {
          VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(declaredIndex, VectorField.class);
          _elvis = _containerOfType;
        }
        final VectorField owner = _elvis;
        VectorCollection _xifexpression = null;
        if ((owner == null)) {
          _xifexpression = null;
        } else {
          VectorCollection _elvis_1 = null;
          VectorCollection _owningCollection = ModelUtil.owningCollection(owner);
          if (_owningCollection != null) {
            _elvis_1 = _owningCollection;
          } else {
            VectorCollection _targetCollection = ModelUtil.targetCollection(declaredIndex);
            _elvis_1 = _targetCollection;
          }
          _xifexpression = _elvis_1;
        }
        _xblockexpression = _xifexpression;
      }
      _switchResult = _xblockexpression;
    }
    if (!_matched) {
      if (declaredIndex instanceof PayloadIndex) {
        _matched=true;
        VectorCollection _elvis = null;
        VectorCollection _targetCollection = ModelUtil.targetCollection(declaredIndex);
        if (_targetCollection != null) {
          _elvis = _targetCollection;
        } else {
          VectorCollection _xifexpression = null;
          Field _field = ((PayloadIndex)declaredIndex).getTarget().getField();
          boolean _tripleEquals = (_field == null);
          if (_tripleEquals) {
            _xifexpression = null;
          } else {
            _xifexpression = ModelUtil.owningCollection(((PayloadIndex)declaredIndex).getTarget().getField());
          }
          _elvis = _xifexpression;
        }
        _switchResult = _elvis;
      }
    }
    return _switchResult;
  }

  private void operationExtensions(final EObject statement) {
    Set<String> _keySet = ModelUtil.extParams(statement, ElasticsearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "elementScope", "asOf", "reranker", "queryModel", "prefetches");
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("the \'highlight\' clause on a vector search; highlighting marks the terms of a text query, which a vector search has not");
      }
      this.queryExtensions(it);
      this.combinations(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(searched));
      final boolean nativeScore = (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE));
      final List<Code.Expr> filters = this.clauses(it, it.getCollection(), target, it.getFilter(), null);
      QueryInput _input = it.getInput();
      boolean _tripleEquals = (_input == null);
      if (_tripleEquals) {
        throw Unsupported.of("a vector query without input");
      }
      boolean _sparse = mapped.vector().sparse();
      if (_sparse) {
        boolean _written = ModelUtil.written(it, "mmr");
        if (_written) {
          throw Unsupported.of("the \'mmr\' clause on a sparse field; the diversify retriever compares dense vectors");
        }
        EQueryInputMode _kind = it.getInput().getKind();
        boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.SPARSE_VECTOR));
        if (_notEquals) {
          String _literal = it.getInput().getKind().getLiteral();
          String _plus = (_literal + " input on sparse field ");
          String _name = mapped.name();
          String _plus_1 = (_plus + _name);
          throw Unsupported.of(_plus_1);
        }
        this.schema.note(it, "exact", "metric");
        Code.Items _arr = ElasticsearchBodies.arr(ElasticsearchBodies.obj("sparse_vector", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(mapped.name()), "query_vector", this.convert(it.getInput().getValue(), mapped))));
        Code.Items _xifexpression = null;
        boolean _isEmpty_1 = filters.isEmpty();
        if (_isEmpty_1) {
          _xifexpression = null;
        } else {
          _xifexpression = ElasticsearchBodies.arr(filters);
        }
        final Code.Entries sparseBody = this.shape(it, it.getCollection(), target, ElasticsearchBodies.obj("query", this.boostQuery(target, ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must", _arr, 
          "filter", _xifexpression)), it.getBoost())), it.getOutput(), this.excludedOf(it), it.isWithVectors());
        sparseBody.put("size", ElasticsearchBodies.num(it.getTopK()));
        int _offsetOf = this.offsetOf(it);
        boolean _greaterThan = (_offsetOf > 0);
        if (_greaterThan) {
          sparseBody.put("from", ElasticsearchBodies.num(this.offsetOf(it)));
        }
        Code.Call _paged = this.paged(this.searchCall(target, this.cursorOrder(it, target, sparseBody), ElasticsearchBodies.relevance(), it.getBounds(), this.groupOf(it, target)), it, target);
        Code.Return _return = new Code.Return(_paged);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(it, "metric");
      if (_written_1) {
        _xifexpression_1 = it.getMetric().getLiteral();
      } else {
        _xifexpression_1 = mapped.vector().metric();
      }
      final String searchMetric = _xifexpression_1;
      final boolean exhaustive = ((it.isExact() || (!Objects.equals(searchMetric, mapped.vector().metric()))) || ((it.getParams() != null) && (it.getParams().isExhaustive() || (ModelUtil.written(it.getParams(), "filterStrategy") && Objects.equals(it.getParams().getFilterStrategy().getLiteral(), "pre_filter")))));
      final Code.Expr queryVector = this.queryVectorOf(it.getInput(), mapped);
      Code.Entries _xifexpression_2 = null;
      if (exhaustive) {
        Code.Entries _xblockexpression_1 = null;
        {
          boolean _written_2 = ModelUtil.written(it, "mmr");
          if (_written_2) {
            throw Unsupported.of("the \'mmr\' clause with an exact search; the diversify retriever wraps a kNN search");
          }
          final String kind = ElasticsearchBodies.exactKind(searchMetric, mapped.vector().element());
          SearchParams _params = it.getParams();
          boolean _tripleNotEquals = (_params != null);
          if (_tripleNotEquals) {
            this.schema.note(it.getParams(), "candidatePool", "filterStrategy", "oversampling", "rescore");
          }
          Code.Text _text = ElasticsearchBodies.text(mapped.name());
          Code.Num _xifexpression_3 = null;
          boolean _equals = Objects.equals(kind, "hamming");
          if (_equals) {
            _xifexpression_3 = ElasticsearchBodies.num(mapped.vector().dims());
          } else {
            _xifexpression_3 = null;
          }
          final Code.Entries scriptParams = ElasticsearchBodies.obj("query_vector", queryVector, "field", _text, "dims", _xifexpression_3);
          Code.Entries _boostQuery = this.boostQuery(target, ElasticsearchBodies.obj("script_score", ElasticsearchBodies.obj("query", this.boolOf(filters), "script", ElasticsearchBodies.script(ElasticsearchBodies.exactScript(kind), scriptParams))), it.getBoost());
          Code.Num _num = ElasticsearchBodies.num(it.getTopK());
          Code.Num _xifexpression_4 = null;
          int _offsetOf_1 = this.offsetOf(it);
          boolean _greaterThan_1 = (_offsetOf_1 > 0);
          if (_greaterThan_1) {
            _xifexpression_4 = ElasticsearchBodies.num(this.offsetOf(it));
          } else {
            _xifexpression_4 = null;
          }
          _xblockexpression_1 = ElasticsearchBodies.obj("query", _boostQuery, "size", _num, 
            "from", _xifexpression_4);
        }
        _xifexpression_2 = _xblockexpression_1;
      } else {
        _xifexpression_2 = this.knnBody(it, target, mapped, queryVector, filters);
      }
      final Code.Entries requestBody = _xifexpression_2;
      if (((((requestBody.has("knn") && (it.getParams() != null)) && ModelUtil.written(it.getParams(), "filterStrategy")) && Objects.equals(it.getParams().getFilterStrategy().getLiteral(), "post_filter")) && (!filters.isEmpty()))) {
        Code.Expr _get = requestBody.get("knn");
        ((Code.Entries) _get).entries().remove("filter");
        requestBody.put("post_filter", this.boolOf(filters));
      }
      if ((((it.getMmr() != null) && (it.getMmr().getVectorField() != null)) && (it.getMmr().getVectorField() != searched))) {
        throw Unsupported.of("mmr on another vector field than the searched one");
      }
      if (((it.getParams() != null) && ModelUtil.written(it.getParams(), "timeoutMillis"))) {
        int _timeoutMillis = it.getParams().getTimeoutMillis();
        String _plus_2 = (Integer.valueOf(_timeoutMillis) + "ms");
        requestBody.put("timeout", ElasticsearchBodies.text(_plus_2));
      }
      Code.Entries _xifexpression_3 = null;
      if (exhaustive) {
        _xifexpression_3 = ElasticsearchBodies.obj("kind", ElasticsearchBodies.text(ElasticsearchBodies.exactKind(searchMetric, mapped.vector().element())), "dims", ElasticsearchBodies.num(mapped.vector().dims()));
      } else {
        _xifexpression_3 = ElasticsearchBodies.scoreForm(mapped.vector(), false);
      }
      final Code.Entries formKind = _xifexpression_3;
      Code.Entries _xifexpression_4 = null;
      BoostBlock _boost = it.getBoost();
      boolean _tripleNotEquals = (_boost != null);
      if (_tripleNotEquals) {
        _xifexpression_4 = ElasticsearchBodies.relevance();
      } else {
        Code.Entries _xifexpression_5 = null;
        if (nativeScore) {
          _xifexpression_5 = ElasticsearchBodies.scoreForm(mapped.vector(), true);
        } else {
          _xifexpression_5 = formKind;
        }
        _xifexpression_4 = _xifexpression_5;
      }
      final Code.Entries form = _xifexpression_4;
      final Code.Entries shaped = this.cursorOrder(it, target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), this.excludedOf(it), it.isWithVectors()));
      EQueryInputMode _kind_1 = it.getInput().getKind();
      boolean _equals = Objects.equals(_kind_1, EQueryInputMode.ID);
      if (_equals) {
        Code.Call _paged_1 = this.paged(this.byStoredVector(it, it.getInput(), it.getCollection(), mapped, shaped, form), it, target);
        Code.Return _return_1 = new Code.Return(_paged_1);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      Code.Call _paged_2 = this.paged(this.searchCall(target, shaped, form, it.getBounds(), this.groupOf(it, target)), it, target);
      Code.Return _return_2 = new Code.Return(_paged_2);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
    }
    return _xblockexpression;
  }

  /**
   * A search by the stored vector of a document: the runtime reads the vector and puts it into every query_vector of the body,
   * which the generator leaves empty (kNN section or query, diversify retriever, script_score params).
   */
  private Code.Call byStoredVector(final EObject statement, final QueryInput queryInput, final VectorCollection collection, final ElasticsearchIndex.Field mapped, final Code.Entries searchBody, final Code.Entries form) {
    Code.Call _xblockexpression = null;
    {
      final ElasticsearchIndex target = this.schema.index(collection);
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = queryInput.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        _elvis = collection;
      }
      final ElasticsearchIndex lookup = this.schema.index(_elvis);
      final String group = this.groupOf(statement, target);
      Code.Text _text = ElasticsearchBodies.text(target.name());
      Code.Text _text_1 = ElasticsearchBodies.text(lookup.name());
      Code.Text _text_2 = ElasticsearchBodies.text(mapped.name());
      Code.Text _text_3 = ElasticsearchBodies.text(target.key());
      Code.Items _arr = ElasticsearchBodies.arr(Literals.of(queryInput.getValue()));
      Object _feature = this.feature(statement, "bounds");
      Code.Entries _boundsOf = this.boundsOf(((ScoreBounds) _feature));
      Code.Text _xifexpression = null;
      if ((group == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.text(group);
      }
      _xblockexpression = ElasticsearchBodies.call("recommend", "index", _text, "lookup", _text_1, "field", _text_2, "key", _text_3, 
        "body", searchBody, "score", form, "positive_ids", _arr, "bounds", _boundsOf, 
        "group", _xifexpression);
    }
    return _xblockexpression;
  }

  /**
   * The request of a dense kNN search: the top-level knn section; the diversify retriever around the knn retriever (mmr); or a
   * knn query (group-by, boost or cursor), in a function_score query with a boost. Collapse inner_hits rerun the request's
   * query, which a top-level knn section is not part of, and search_after and function_score need a query.
   * A cursor page reads the CURSOR_WINDOW nearest records.
   */
  private Code.Entries knnBody(final EObject statement, final ElasticsearchIndex target, final ElasticsearchIndex.Field mapped, final Code.Expr queryVector, final List<Code.Expr> filters) {
    Code.Entries _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "topK");
      final Integer size = ((Integer) _feature);
      Object _feature_1 = this.feature(statement, "mmr");
      final MmrConfig mmrConfig = ((MmrConfig) _feature_1);
      Object _feature_2 = this.feature(statement, "boost");
      final BoostBlock block = ((BoostBlock) _feature_2);
      final boolean paging = ModelUtil.written(statement, "cursor");
      int _xifexpression = (int) 0;
      if (paging) {
        _xifexpression = ElasticsearchStatements.CURSOR_WINDOW;
      } else {
        int _offsetOf = this.offsetOf(statement);
        _xifexpression = ((size).intValue() + _offsetOf);
      }
      final int k = _xifexpression;
      final Code.Entries knn = this.knnSection(statement, target, mapped, queryVector, k, filters);
      if ((paging && (!knn.has("num_candidates")))) {
        knn.put("num_candidates", ElasticsearchBodies.num(ElasticsearchStatements.CURSOR_WINDOW));
      }
      Code.Num _xifexpression_1 = null;
      int _offsetOf_1 = this.offsetOf(statement);
      boolean _greaterThan = (_offsetOf_1 > 0);
      if (_greaterThan) {
        _xifexpression_1 = ElasticsearchBodies.num(this.offsetOf(statement));
      } else {
        _xifexpression_1 = null;
      }
      final Code.Num from = _xifexpression_1;
      if ((mmrConfig != null)) {
        if ((block != null)) {
          throw Unsupported.of("the \'boost\' clause with mmr; the generated diversify retriever ranks the similarity of its kNN retriever");
        }
        int _xifexpression_2 = (int) 0;
        boolean _written = ModelUtil.written(mmrConfig, "candidates");
        if (_written) {
          _xifexpression_2 = Math.max(mmrConfig.getCandidates(), k);
        } else {
          _xifexpression_2 = Math.max((10 * k), 10);
        }
        final int window = _xifexpression_2;
        boolean _has = knn.has("num_candidates");
        boolean _not = (!_has);
        if (_not) {
          knn.put("num_candidates", ElasticsearchBodies.num(ElasticsearchBodies.candidates(window, k)));
        }
        return ElasticsearchBodies.obj("retriever", ElasticsearchBodies.obj("diversify", ElasticsearchBodies.obj("type", ElasticsearchBodies.text("mmr"), "field", ElasticsearchBodies.text(mapped.name()), "query_vector", queryVector, "lambda", ElasticsearchBodies.num(mmrConfig.getDiversityLambda()), 
          "size", ElasticsearchBodies.num(k), "rank_window_size", ElasticsearchBodies.num(window), "retriever", ElasticsearchBodies.obj("knn", knn))), "size", ElasticsearchBodies.num((size).intValue()), "from", from);
      }
      if ((((block != null) || paging) || (this.groupOf(statement, target) != null))) {
        return ElasticsearchBodies.obj("query", this.boostQuery(target, ElasticsearchBodies.obj("knn", knn), block), "size", ElasticsearchBodies.num((size).intValue()), "from", from);
      }
      _xblockexpression = ElasticsearchBodies.obj("knn", knn, "size", ElasticsearchBodies.num((size).intValue()), "from", from);
    }
    return _xblockexpression;
  }

  /**
   * Clause combinations the generated requests do not express.
   */
  private void combinations(final EObject statement) {
    boolean _written = ModelUtil.written(statement, "cursor");
    if (_written) {
      for (final String clause : Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("offset", "groupBy", "mmr", "sortClauses"))) {
        boolean _written_1 = ModelUtil.written(statement, clause);
        if (_written_1) {
          String _elvis = null;
          String _get = ElasticsearchStatements.CLAUSES.get(clause);
          if (_get != null) {
            _elvis = _get;
          } else {
            _elvis = clause;
          }
          String _plus = ("the \'cursor\' clause with " + _elvis);
          String _plus_1 = (_plus + "; a cursor page continues after its record in the order of the search");
          throw Unsupported.of(_plus_1);
        }
      }
    }
  }

  /**
   * A cursor page sorts by score, then by key, so that search_after continues after the cursor record.
   */
  private Code.Entries cursorOrder(final EObject statement, final ElasticsearchIndex target, final Code.Entries requestBody) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(statement, "cursor");
      if (_written) {
        requestBody.put("sort", ElasticsearchBodies.arr(ElasticsearchBodies.obj("_score", ElasticsearchBodies.obj("order", ElasticsearchBodies.text("desc"))), ElasticsearchBodies.obj(target.keyField().exact(), ElasticsearchBodies.obj("order", ElasticsearchBodies.text("asc")))));
      }
      _xblockexpression = requestBody;
    }
    return _xblockexpression;
  }

  /**
   * The cursor of a search, for the runtime's search_after (the key of the last hit of the previous page).
   */
  private Code.Call paged(final Code.Call search, final EObject statement, final ElasticsearchIndex target) {
    Code.Call _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(statement, "cursor");
      if (_written) {
        Object _feature = this.feature(statement, "cursor");
        search.args().put("after", this.cursorValue(target.keyField(), ((String) _feature)));
      }
      _xblockexpression = search;
    }
    return _xblockexpression;
  }

  /**
   * The kNN section: k, candidates (num_candidates from the search parameters, else the index's search-candidates, else the
   * server default of 1.5 k), the pre-filter and the oversampling of a quantized index.
   */
  private Code.Entries knnSection(final EObject statement, final ElasticsearchIndex target, final ElasticsearchIndex.Field mapped, final Code.Expr queryVector, final int k, final List<Code.Expr> filters) {
    Code.Entries _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "params");
      final SearchParams searchParams = ((SearchParams) _feature);
      final VectorCollection collection = ModelUtil.targetCollection(statement);
      final Function1<VectorField, Boolean> _function = (VectorField v) -> {
        String _name = target.field(ModelUtil.fieldName(v)).name();
        String _name_1 = mapped.name();
        return Boolean.valueOf(Objects.equals(_name, _name_1));
      };
      final VectorField declared = IterableExtensions.<VectorField>findFirst(Iterables.<VectorField>filter(collection.getFields(), VectorField.class), _function);
      VectorIndex _xifexpression = null;
      if ((declared == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = this.schema.indexOf(collection, declared);
      }
      final VectorIndex vectorIndex = _xifexpression;
      Integer _xifexpression_1 = null;
      if (((vectorIndex != null) && ModelUtil.written(vectorIndex, "searchCandidates"))) {
        _xifexpression_1 = Integer.valueOf(vectorIndex.getSearchCandidates());
      } else {
        _xifexpression_1 = null;
      }
      Integer pool = _xifexpression_1;
      Code.Entries rescore = null;
      if ((searchParams != null)) {
        boolean _written = ModelUtil.written(searchParams, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: num_candidates and the oversampling of quantized indexes are effort settings, with no declarable recall target");
        }
        boolean _written_1 = ModelUtil.written(searchParams, "probes");
        if (_written_1) {
          throw Unsupported.of("probes; Elasticsearch probes clusters only in its IVF index (bbq_disk, visit_percentage), which needs an enterprise license");
        }
        boolean _written_2 = ModelUtil.written(searchParams, "scanBudget");
        if (_written_2) {
          throw Unsupported.of("scan-budget; an HNSW search in Elasticsearch takes num_candidates and no bound on visited vectors");
        }
        Set<String> _keySet = ModelUtil.extParams(searchParams, ElasticsearchGenerator.VENDOR).keySet();
        for (final String key : _keySet) {
          throw Unsupported.of(("extension parameter " + key));
        }
        boolean _written_3 = ModelUtil.written(searchParams, "candidatePool");
        if (_written_3) {
          pool = Integer.valueOf(searchParams.getCandidatePool());
        }
        boolean _written_4 = ModelUtil.written(searchParams, "oversampling");
        if (_written_4) {
          rescore = ElasticsearchBodies.obj("oversample", ElasticsearchBodies.num(searchParams.getOversampling()));
        }
        if ((searchParams.isRescore() && (rescore == null))) {
          this.notes.add("rescore on SearchParams, which the index_options of the field decide");
        }
      }
      Code.Text _text = ElasticsearchBodies.text(mapped.name());
      Code.Num _num = ElasticsearchBodies.num(k);
      Code.Num _xifexpression_2 = null;
      if ((pool == null)) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.num(ElasticsearchBodies.candidates((pool).intValue(), k));
      }
      Code.Items _xifexpression_3 = null;
      boolean _isEmpty = filters.isEmpty();
      if (_isEmpty) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = ElasticsearchBodies.arr(filters);
      }
      _xblockexpression = ElasticsearchBodies.obj("field", _text, "query_vector", queryVector, "k", _num, "num_candidates", _xifexpression_2, 
        "filter", _xifexpression_3, "rescore_vector", rescore);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "reranker");
      this.queryExtensions(it);
      this.combinations(it);
      boolean _written = ModelUtil.written(it, "mmr");
      if (_written) {
        throw Unsupported.of("the \'mmr\' clause on a keyword search; the diversify retriever compares the hits\' dense vectors with a query vector, which a text query has not");
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      EList<SearchFieldRef> _searchFields = it.getSearchFields();
      QueryInput _input = it.getInput();
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "textQueryType");
      if (_written_1) {
        _xifexpression = it.getTextQueryType().getLiteral();
      } else {
        _xifexpression = "match";
      }
      String _xifexpression_1 = null;
      boolean _written_2 = ModelUtil.written(it, "matchOperator");
      if (_written_2) {
        _xifexpression_1 = it.getMatchOperator().getLiteral();
      } else {
        _xifexpression_1 = null;
      }
      Integer _xifexpression_2 = null;
      boolean _written_3 = ModelUtil.written(it, "minShouldMatch");
      if (_written_3) {
        _xifexpression_2 = Integer.valueOf(it.getMinShouldMatch());
      } else {
        _xifexpression_2 = null;
      }
      String _fuzziness = it.getFuzziness();
      Integer _xifexpression_3 = null;
      boolean _written_4 = ModelUtil.written(it, "slop");
      if (_written_4) {
        _xifexpression_3 = Integer.valueOf(it.getSlop());
      } else {
        _xifexpression_3 = null;
      }
      final Code.Entries textQuery = this.keywordQuery(target, _searchFields, _input, _xifexpression, _xifexpression_1, _xifexpression_2, _fuzziness, _xifexpression_3);
      final List<Code.Expr> filters = this.clauses(it, it.getCollection(), target, it.getFilter(), null);
      Code.Items _arr = ElasticsearchBodies.arr(textQuery);
      Code.Items _xifexpression_4 = null;
      boolean _isEmpty = filters.isEmpty();
      if (_isEmpty) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = ElasticsearchBodies.arr(filters);
      }
      final Code.Entries scored = this.boostQuery(target, ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must", _arr, "filter", _xifexpression_4)), it.getBoost());
      Code.Num _num = ElasticsearchBodies.num(it.getTopK());
      Code.Num _xifexpression_5 = null;
      int _offsetOf = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        _xifexpression_5 = ElasticsearchBodies.num(this.offsetOf(it));
      } else {
        _xifexpression_5 = null;
      }
      final Code.Entries requestBody = ElasticsearchBodies.obj("query", scored, "size", _num, "from", _xifexpression_5);
      if (((it.getParams() != null) && ModelUtil.written(it.getParams(), "timeoutMillis"))) {
        int _timeoutMillis = it.getParams().getTimeoutMillis();
        String _plus = (Integer.valueOf(_timeoutMillis) + "ms");
        requestBody.put("timeout", ElasticsearchBodies.text(_plus));
      }
      final Code.Entries shaped = this.cursorOrder(it, target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), this.excludedOf(it), it.isWithVectors()));
      Code.Call _paged = this.paged(this.searchCall(target, shaped, ElasticsearchBodies.relevance(), it.getBounds(), this.groupOf(it, target)), it, target);
      Code.Return _return = new Code.Return(_paged);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A text query: match (or multi_match over several fields, most_fields so that the field scores add up), match_phrase with
   * slop, match_phrase_prefix, query_string, and the term-level prefix, term, fuzzy, wildcard and regexp queries on the
   * analyzed terms of each field, with boosts per field.
   */
  private Code.Entries keywordQuery(final ElasticsearchIndex target, final List<SearchFieldRef> refs, final QueryInput queryInput, final String type, final String operator, final Integer minimum, final String fuzzinessValue, final Integer slopValue) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Expr words = this.keywordText(queryInput);
      final List<String> fields = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<ElasticsearchIndex.Field> _fields = target.fields();
        for (final ElasticsearchIndex.Field mapped : _fields) {
          boolean _fullText = mapped.fullText();
          if (_fullText) {
            String _name = mapped.name();
            fields.add(_name);
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final ElasticsearchIndex.Field mapped_1 = target.field(ModelUtil.fieldName(ref.getField()));
            ElasticsearchIndex.Vector _vector = mapped_1.vector();
            boolean _tripleNotEquals = (_vector != null);
            if (_tripleNotEquals) {
              String _name_1 = mapped_1.name();
              String _plus = ("keyword search on vector field " + _name_1);
              throw Unsupported.of(_plus);
            }
            String _name_2 = mapped_1.name();
            String _xifexpression = null;
            boolean _written = ModelUtil.written(ref, "boost");
            if (_written) {
              String _number = ElasticsearchFilter.number(ref.getBoost());
              _xifexpression = ("^" + _number);
            } else {
              _xifexpression = "";
            }
            String _plus_1 = (_name_2 + _xifexpression);
            fields.add(_plus_1);
          }
        }
      }
      boolean _isEmpty_1 = fields.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search in a collection without full-text fields");
      }
      if (((slopValue != null) && (!Objects.equals(type, "phrase")))) {
        throw Unsupported.of((("the \'slop\' clause with text-query " + type) + "; slop belongs to phrase queries"));
      }
      if (((fuzzinessValue != null) && (!Objects.equals(type, "match")))) {
        throw Unsupported.of((("the \'fuzziness\' clause with text-query " + type) + "; fuzziness belongs to match queries (text-query fuzzy has its own)"));
      }
      if (((minimum != null) && (!Objects.equals(type, "match")))) {
        throw Unsupported.of(("the \'min-should-match\' clause with text-query " + type));
      }
      Code.Text _xifexpression = null;
      if ((operator == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.text(operator);
      }
      Code.Num _xifexpression_1 = null;
      if ((minimum == null)) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.num((minimum).intValue());
      }
      Code.Text _xifexpression_2 = null;
      if ((fuzzinessValue == null)) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.text(fuzzinessValue);
      }
      Code.Num _xifexpression_3 = null;
      if ((slopValue == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = ElasticsearchBodies.num((slopValue).intValue());
      }
      final Code.Entries options = ElasticsearchBodies.obj("operator", _xifexpression, "minimum_should_match", _xifexpression_1, 
        "fuzziness", _xifexpression_2, "slop", _xifexpression_3);
      Code.Entries _switchResult = null;
      if (type != null) {
        switch (type) {
          case "match":
          case "phrase":
          case "phrase_prefix":
            Code.Entries _xblockexpression_1 = null;
            {
              String _switchResult_1 = null;
              if (type != null) {
                switch (type) {
                  case "match":
                    _switchResult_1 = "most_fields";
                    break;
                  case "phrase":
                    _switchResult_1 = "phrase";
                    break;
                  default:
                    _switchResult_1 = "phrase_prefix";
                    break;
                }
              } else {
                _switchResult_1 = "phrase_prefix";
              }
              final String multiType = _switchResult_1;
              if (((fields.size() == 1) && (!IterableExtensions.<String>head(fields).contains("^")))) {
                String _switchResult_2 = null;
                if (type != null) {
                  switch (type) {
                    case "match":
                      _switchResult_2 = "match";
                      break;
                    case "phrase":
                      _switchResult_2 = "match_phrase";
                      break;
                    default:
                      _switchResult_2 = "match_phrase_prefix";
                      break;
                  }
                } else {
                  _switchResult_2 = "match_phrase_prefix";
                }
                final String single = _switchResult_2;
                final Code.Entries perField = ElasticsearchBodies.obj("query", words);
                perField.entries().putAll(options.entries());
                return ElasticsearchBodies.obj(single, ElasticsearchBodies.obj(IterableExtensions.<String>head(fields), perField));
              }
              final Function1<String, Code.Expr> _function = (String f) -> {
                Code.Text _text = ElasticsearchBodies.text(f);
                return ((Code.Expr) _text);
              };
              final Code.Entries multi = ElasticsearchBodies.obj("query", words, "fields", ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(fields, _function))), "type", ElasticsearchBodies.text(multiType));
              multi.entries().putAll(options.entries());
              _xblockexpression_1 = ElasticsearchBodies.obj("multi_match", multi);
            }
            _switchResult = _xblockexpression_1;
            break;
          case "query_string":
            Code.Entries _xblockexpression_2 = null;
            {
              if ((operator != null)) {
                options.entries().put("default_operator", options.entries().remove("operator"));
              }
              final Function1<String, Code.Expr> _function = (String f) -> {
                Code.Text _text = ElasticsearchBodies.text(f);
                return ((Code.Expr) _text);
              };
              final Code.Entries queryString = ElasticsearchBodies.obj("query", words, "fields", ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(fields, _function))));
              queryString.entries().putAll(options.entries());
              _xblockexpression_2 = ElasticsearchBodies.obj("query_string", queryString);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            Code.Entries _xblockexpression_3 = null;
            {
              if ((operator != null)) {
                throw Unsupported.of((("the \'operator\' clause with text-query " + type) + "; it applies to analyzed queries"));
              }
              final List<Code.Expr> perField = CollectionLiterals.<Code.Expr>newArrayList();
              for (final String f : fields) {
                {
                  final String[] parts = f.split("\\^");
                  final ElasticsearchIndex.Field mapped_1 = target.field(parts[0]);
                  Code.Num _xifexpression_4 = null;
                  int _length = parts.length;
                  boolean _greaterThan = (_length > 1);
                  if (_greaterThan) {
                    _xifexpression_4 = ElasticsearchBodies.num(Double.parseDouble(parts[1]));
                  } else {
                    _xifexpression_4 = null;
                  }
                  Code.Text _xifexpression_5 = null;
                  boolean _equals = Objects.equals(type, "fuzzy");
                  if (_equals) {
                    _xifexpression_5 = ElasticsearchBodies.text("AUTO");
                  } else {
                    _xifexpression_5 = null;
                  }
                  final Code.Entries leaf = ElasticsearchBodies.obj("value", words, "boost", _xifexpression_4, "fuzziness", _xifexpression_5);
                  Code.Entries _obj = ElasticsearchBodies.obj(type, ElasticsearchBodies.obj(mapped_1.name(), leaf));
                  perField.add(_obj);
                }
              }
              Code.Entries _xifexpression_4 = null;
              int _size = perField.size();
              boolean _equals = (_size == 1);
              if (_equals) {
                Code.Expr _head = IterableExtensions.<Code.Expr>head(perField);
                _xifexpression_4 = ((Code.Entries) _head);
              } else {
                _xifexpression_4 = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("should", ElasticsearchBodies.arr(perField), "minimum_should_match", ElasticsearchBodies.num(1)));
              }
              _xblockexpression_3 = _xifexpression_4;
            }
            _switchResult = _xblockexpression_3;
            break;
        }
      } else {
        Code.Entries _xblockexpression_3 = null;
        {
          if ((operator != null)) {
            throw Unsupported.of((("the \'operator\' clause with text-query " + type) + "; it applies to analyzed queries"));
          }
          final List<Code.Expr> perField = CollectionLiterals.<Code.Expr>newArrayList();
          for (final String f : fields) {
            {
              final String[] parts = f.split("\\^");
              final ElasticsearchIndex.Field mapped_1 = target.field(parts[0]);
              Code.Num _xifexpression_4 = null;
              int _length = parts.length;
              boolean _greaterThan = (_length > 1);
              if (_greaterThan) {
                _xifexpression_4 = ElasticsearchBodies.num(Double.parseDouble(parts[1]));
              } else {
                _xifexpression_4 = null;
              }
              Code.Text _xifexpression_5 = null;
              boolean _equals = Objects.equals(type, "fuzzy");
              if (_equals) {
                _xifexpression_5 = ElasticsearchBodies.text("AUTO");
              } else {
                _xifexpression_5 = null;
              }
              final Code.Entries leaf = ElasticsearchBodies.obj("value", words, "boost", _xifexpression_4, "fuzziness", _xifexpression_5);
              Code.Entries _obj = ElasticsearchBodies.obj(type, ElasticsearchBodies.obj(mapped_1.name(), leaf));
              perField.add(_obj);
            }
          }
          Code.Entries _xifexpression_4 = null;
          int _size = perField.size();
          boolean _equals = (_size == 1);
          if (_equals) {
            Code.Expr _head = IterableExtensions.<Code.Expr>head(perField);
            _xifexpression_4 = ((Code.Entries) _head);
          } else {
            _xifexpression_4 = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("should", ElasticsearchBodies.arr(perField), "minimum_should_match", ElasticsearchBodies.num(1)));
          }
          _xblockexpression_3 = _xifexpression_4;
        }
        _switchResult = _xblockexpression_3;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Boost functions as a function_score query: field_value_factor, the gauss, exp and linear decay functions and weights of
   * filters; combine is score_mode and apply is boost_mode.
   */
  private Code.Entries boostQuery(final ElasticsearchIndex target, final Code.Entries inner, final BoostBlock block) {
    Code.Entries _xblockexpression = null;
    {
      if ((block == null)) {
        return inner;
      }
      final List<Code.Expr> functions = CollectionLiterals.<Code.Expr>newArrayList();
      EList<BoostFunction> _functions = block.getFunctions();
      for (final BoostFunction boostFunction : _functions) {
        {
          Code.Num _xifexpression = null;
          boolean _written = ModelUtil.written(boostFunction, "weight");
          if (_written) {
            _xifexpression = ElasticsearchBodies.num(boostFunction.getWeight());
          } else {
            _xifexpression = null;
          }
          final Code.Num weight = _xifexpression;
          Code.Entries _xifexpression_1 = null;
          Condition _condition = boostFunction.getCondition();
          boolean _tripleEquals = (_condition == null);
          if (_tripleEquals) {
            _xifexpression_1 = null;
          } else {
            _xifexpression_1 = new ElasticsearchFilter(target).query(boostFunction.getCondition());
          }
          final Code.Entries applies = _xifexpression_1;
          String _xifexpression_2 = null;
          FieldPath _target = boostFunction.getTarget();
          boolean _tripleEquals_1 = (_target == null);
          if (_tripleEquals_1) {
            _xifexpression_2 = null;
          } else {
            _xifexpression_2 = this.fieldPath(target, boostFunction.getTarget());
          }
          final String path = _xifexpression_2;
          String _literal = boostFunction.getKind().getLiteral();
          if (_literal != null) {
            switch (_literal) {
              case "field_value":
                if ((path == null)) {
                  throw Unsupported.of("a field_value boost without a field");
                }
                Code.Entries _obj = ElasticsearchBodies.obj("field_value_factor", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(path), "missing", ElasticsearchBodies.num(0)), "weight", weight, "filter", applies);
                functions.add(_obj);
                break;
              case "decay":
                if (((path == null) || (boostFunction.getOrigin() == null))) {
                  throw Unsupported.of("a decay boost without field, origin and scale");
                }
                Code.Expr _of = Literals.of(boostFunction.getOrigin());
                Code.Expr _of_1 = Literals.of(boostFunction.getScale());
                Code.Expr _xifexpression_3 = null;
                Literal _decayOffset = boostFunction.getDecayOffset();
                boolean _tripleEquals_2 = (_decayOffset == null);
                if (_tripleEquals_2) {
                  _xifexpression_3 = null;
                } else {
                  _xifexpression_3 = Literals.of(boostFunction.getDecayOffset());
                }
                Code.Num _xifexpression_4 = null;
                boolean _written_1 = ModelUtil.written(boostFunction, "decayRate");
                if (_written_1) {
                  _xifexpression_4 = ElasticsearchBodies.num(boostFunction.getDecayRate());
                } else {
                  _xifexpression_4 = null;
                }
                final Code.Entries decay = ElasticsearchBodies.obj("origin", _of, "scale", _of_1, 
                  "offset", _xifexpression_3, 
                  "decay", _xifexpression_4);
                Code.Entries _obj_1 = ElasticsearchBodies.obj(boostFunction.getShape().getLiteral(), ElasticsearchBodies.obj(path, decay), "weight", weight, "filter", applies);
                functions.add(_obj_1);
                break;
              default:
                {
                  if ((applies == null)) {
                    throw Unsupported.of("a condition boost without a condition");
                  }
                  Code.Num _elvis = null;
                  if (weight != null) {
                    _elvis = weight;
                  } else {
                    Code.Num _num = ElasticsearchBodies.num(1);
                    _elvis = _num;
                  }
                  Code.Entries _obj_2 = ElasticsearchBodies.obj("filter", applies, "weight", _elvis);
                  functions.add(_obj_2);
                }
                break;
            }
          } else {
            {
              if ((applies == null)) {
                throw Unsupported.of("a condition boost without a condition");
              }
              Code.Num _elvis = null;
              if (weight != null) {
                _elvis = weight;
              } else {
                Code.Num _num = ElasticsearchBodies.num(1);
                _elvis = _num;
              }
              Code.Entries _obj_2 = ElasticsearchBodies.obj("filter", applies, "weight", _elvis);
              functions.add(_obj_2);
            }
          }
        }
      }
      String _xifexpression = null;
      boolean _written = ModelUtil.written(block, "combine");
      if (_written) {
        _xifexpression = block.getCombine().getLiteral();
      } else {
        _xifexpression = "multiply";
      }
      final String scoreMode = _xifexpression;
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(block, "apply");
      if (_written_1) {
        _xifexpression_1 = block.getApply().getLiteral();
      } else {
        _xifexpression_1 = "multiply";
      }
      final String boostMode = _xifexpression_1;
      String _xifexpression_2 = null;
      boolean _equals = Objects.equals(scoreMode, "replace");
      if (_equals) {
        _xifexpression_2 = "first";
      } else {
        _xifexpression_2 = scoreMode;
      }
      _xblockexpression = ElasticsearchBodies.obj("function_score", ElasticsearchBodies.obj("query", inner, "functions", ElasticsearchBodies.arr(functions), "score_mode", ElasticsearchBodies.text(_xifexpression_2), 
        "boost_mode", ElasticsearchBodies.text(boostMode)));
    }
    return _xblockexpression;
  }

  private String fieldPath(final ElasticsearchIndex target, final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        return ElasticsearchBodies.field(path.getRawPath());
      }
      final List<String> segments = CollectionLiterals.<String>newArrayList();
      String _name = target.field(ModelUtil.fieldName(path.getField())).name();
      segments.add(_name);
      final Function1<Field, String> _function = (Field s) -> {
        return ModelUtil.fieldName(s);
      };
      List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
      Iterables.<String>addAll(segments, _map);
      _xblockexpression = IterableExtensions.join(segments, ".");
    }
    return _xblockexpression;
  }

  /**
   * Hybrid search fused in the runtime: each leg is one search request (a kNN search or a text query with the shared and the
   * leg's filters), and the fusion function of vecDSL combines the legs' ranked hits. The rrf and linear retrievers
   * would fuse on the server but need an enterprise license.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "reranker");
      boolean _written = ModelUtil.written(it, "boost");
      if (_written) {
        throw Unsupported.of((("the \'boost\' clause on a hybrid search; " + ElasticsearchStatements.PENDING) + " (boost functions over the fused records)"));
      }
      this.queryExtensions(it);
      this.combinations(it);
      if ((ModelUtil.written(it, "groupBy") && (ModelUtil.written(it, "sortClauses") || ModelUtil.written(it, "mmr")))) {
        throw Unsupported.of("the \'group-by\' clause with sort or mmr on a hybrid search; groups are ordered by their best fused hit");
      }
      final Function1<SortClause, Boolean> _function = (SortClause c) -> {
        return Boolean.valueOf(c.isByRelevance());
      };
      boolean _exists = IterableExtensions.<SortClause>exists(it.getSortClauses(), _function);
      if (_exists) {
        throw Unsupported.of("sort by relevance on a hybrid search; the server sorts the fused page by field values, and the fused order is the relevance order");
      }
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "fusion");
      if (_written_1) {
        _xifexpression = it.getFusion().getLiteral();
      } else {
        _xifexpression = "rrf";
      }
      final String fusionKind = _xifexpression;
      final List<Double> weights = this.legWeights(it, fusionKind);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      final int defaultLimit = (_topK + _offsetOf);
      final List<Code.Expr> parts = CollectionLiterals.<Code.Expr>newArrayList();
      int position = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Integer _xifexpression_1 = null;
          boolean _written_2 = ModelUtil.written(leg, "legLimit");
          if (_written_2) {
            Object _feature = this.feature(leg, "legLimit");
            _xifexpression_1 = ((Integer) _feature);
          } else {
            _xifexpression_1 = Integer.valueOf(defaultLimit);
          }
          final Integer legLimit = _xifexpression_1;
          final List<Code.Expr> legFilters = CollectionLiterals.<Code.Expr>newArrayList();
          List<Code.Expr> _clauses = this.clauses(it, it.getCollection(), target, it.getFilter(), null);
          Iterables.<Code.Expr>addAll(legFilters, _clauses);
          Object _feature_1 = this.feature(leg, "filter");
          final FilterBlock legFilter = ((FilterBlock) _feature_1);
          if ((legFilter != null)) {
            Code.Entries _query = new ElasticsearchFilter(target).query(legFilter.getCondition());
            legFilters.add(_query);
          }
          Set<String> _keySet = ModelUtil.extParams(leg, ElasticsearchGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of((("extension parameter " + key) + " on a hybrid leg"));
          }
          Code.Entries _switchResult = null;
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            Code.Entries _xblockexpression_1 = null;
            {
              ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
              boolean _tripleNotEquals = (_bounds != null);
              if (_tripleNotEquals) {
                throw Unsupported.of((("bounds on a hybrid leg; " + ElasticsearchStatements.PENDING) + " (a score condition on the leg in the runtime)"));
              }
              final VectorField legField = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
              final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(legField));
              boolean _sparse = mapped.vector().sparse();
              if (_sparse) {
                throw Unsupported.of((("a sparse vector leg; " + ElasticsearchStatements.PENDING) + " (a sparse_vector query as a leg)"));
              }
              if (((ModelUtil.written(leg, "metric") && (!Objects.equals(((VectorLeg)leg).getMetric().getLiteral(), mapped.vector().metric()))) || ((VectorLeg)leg).isExact())) {
                throw Unsupported.of((("an exact or re-metered vector leg; " + ElasticsearchStatements.PENDING) + " (a script_score query as a leg)"));
              }
              SearchParams _params = ((VectorLeg)leg).getParams();
              boolean _tripleNotEquals_1 = (_params != null);
              if (_tripleNotEquals_1) {
                this.schema.note(((VectorLeg)leg).getParams(), "filterStrategy", "exhaustive");
              }
              QueryInput _elvis = null;
              QueryInput _input = ((VectorLeg)leg).getInput();
              if (_input != null) {
                _elvis = _input;
              } else {
                QueryInput _input_1 = it.getInput();
                _elvis = _input_1;
              }
              final QueryInput legInput = _elvis;
              if (((legInput == null) || Objects.equals(legInput.getKind(), EQueryInputMode.ID))) {
                throw Unsupported.of("a vector leg without a vector input");
              }
              Code.Text _text = ElasticsearchBodies.text(mapped.name());
              Code.Expr _queryVectorOf = this.queryVectorOf(legInput, mapped);
              Code.Num _num = ElasticsearchBodies.num((legLimit).intValue());
              Code.Num _xifexpression_2 = null;
              if (((((VectorLeg)leg).getParams() != null) && ModelUtil.written(((VectorLeg)leg).getParams(), "candidatePool"))) {
                _xifexpression_2 = ElasticsearchBodies.num(ElasticsearchBodies.candidates(((VectorLeg)leg).getParams().getCandidatePool(), (legLimit).intValue()));
              } else {
                _xifexpression_2 = null;
              }
              Code.Items _xifexpression_3 = null;
              boolean _isEmpty = legFilters.isEmpty();
              if (_isEmpty) {
                _xifexpression_3 = null;
              } else {
                _xifexpression_3 = ElasticsearchBodies.arr(legFilters);
              }
              _xblockexpression_1 = ElasticsearchBodies.obj("knn", ElasticsearchBodies.obj("field", _text, "query_vector", _queryVectorOf, "k", _num, 
                "num_candidates", _xifexpression_2, 
                "filter", _xifexpression_3), "size", ElasticsearchBodies.num((legLimit).intValue()));
            }
            _switchResult = _xblockexpression_1;
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              Code.Entries _xblockexpression_1 = null;
              {
                EList<SearchFieldRef> _searchFields = ((KeywordLeg)leg).getSearchFields();
                QueryInput _elvis = null;
                QueryInput _input = ((KeywordLeg)leg).getInput();
                if (_input != null) {
                  _elvis = _input;
                } else {
                  QueryInput _input_1 = it.getInput();
                  _elvis = _input_1;
                }
                String _xifexpression_2 = null;
                boolean _written_3 = ModelUtil.written(leg, "textQueryType");
                if (_written_3) {
                  _xifexpression_2 = ((KeywordLeg)leg).getTextQueryType().getLiteral();
                } else {
                  _xifexpression_2 = "match";
                }
                String _xifexpression_3 = null;
                boolean _written_4 = ModelUtil.written(leg, "matchOperator");
                if (_written_4) {
                  _xifexpression_3 = ((KeywordLeg)leg).getMatchOperator().getLiteral();
                } else {
                  _xifexpression_3 = null;
                }
                Integer _xifexpression_4 = null;
                boolean _written_5 = ModelUtil.written(leg, "minShouldMatch");
                if (_written_5) {
                  _xifexpression_4 = Integer.valueOf(((KeywordLeg)leg).getMinShouldMatch());
                } else {
                  _xifexpression_4 = null;
                }
                final Code.Entries textQuery = this.keywordQuery(target, _searchFields, _elvis, _xifexpression_2, _xifexpression_3, _xifexpression_4, null, null);
                Code.Items _arr = ElasticsearchBodies.arr(textQuery);
                Code.Items _xifexpression_5 = null;
                boolean _isEmpty = legFilters.isEmpty();
                if (_isEmpty) {
                  _xifexpression_5 = null;
                } else {
                  _xifexpression_5 = ElasticsearchBodies.arr(legFilters);
                }
                _xblockexpression_1 = ElasticsearchBodies.obj("query", ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must", _arr, "filter", _xifexpression_5)), "size", ElasticsearchBodies.num((legLimit).intValue()));
              }
              _switchResult = _xblockexpression_1;
            }
          }
          final Code.Entries legBody = _switchResult;
          Code.Entries _switchResult_1 = null;
          boolean _matched_1 = false;
          if (leg instanceof VectorLeg) {
            _matched_1=true;
            _switchResult_1 = ElasticsearchBodies.scoreForm(target.field(ModelUtil.fieldName(this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField()))).vector(), false);
          }
          if (!_matched_1) {
            _switchResult_1 = ElasticsearchBodies.relevance();
          }
          final Code.Entries legForm = _switchResult_1;
          Code.Entries _obj = ElasticsearchBodies.obj("body", this.legShape(it, it.getCollection(), target, legBody), "score", legForm, "weight", ElasticsearchBodies.num((weights.get(position)).doubleValue()));
          parts.add(_obj);
          position = (position + 1);
        }
      }
      final String group = this.groupOf(it, target);
      Code.Text _text = ElasticsearchBodies.text(target.name());
      Code.Items _arr = ElasticsearchBodies.arr(parts);
      Code.Text _text_1 = ElasticsearchBodies.text(fusionKind);
      int _xifexpression_1 = (int) 0;
      boolean _written_2 = ModelUtil.written(it, "fusionConstant");
      if (_written_2) {
        _xifexpression_1 = it.getFusionConstant();
      } else {
        _xifexpression_1 = 60;
      }
      Code.Num _num = ElasticsearchBodies.num(_xifexpression_1);
      Code.Num _num_1 = ElasticsearchBodies.num(it.getTopK());
      Code.Num _num_2 = ElasticsearchBodies.num(this.offsetOf(it));
      Code.Text _text_2 = ElasticsearchBodies.text(target.key());
      Code.Entries _boundsOf = this.boundsOf(it.getBounds());
      Code.Text _xifexpression_2 = null;
      if ((group == null)) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.text(group);
      }
      Code.Num _xifexpression_3 = null;
      boolean _written_3 = ModelUtil.written(it, "groupSize");
      if (_written_3) {
        _xifexpression_3 = ElasticsearchBodies.num(it.getGroupSize());
      } else {
        _xifexpression_3 = null;
      }
      Code.Entries _xifexpression_4 = null;
      MmrConfig _mmr = it.getMmr();
      boolean _tripleEquals = (_mmr == null);
      if (_tripleEquals) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = this.fusedMmr(it, target);
      }
      Code.Items _xifexpression_5 = null;
      boolean _isEmpty = it.getSortClauses().isEmpty();
      if (_isEmpty) {
        _xifexpression_5 = null;
      } else {
        final Function1<SortClause, Code.Expr> _function_1 = (SortClause c) -> {
          Code.Entries _sortEntry = this.sortEntry(target, c, true);
          return ((Code.Expr) _sortEntry);
        };
        _xifexpression_5 = ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<SortClause, Code.Expr>map(it.getSortClauses(), _function_1)));
      }
      Code.Flag _xifexpression_6 = null;
      boolean _isWithTotal = it.isWithTotal();
      if (_isWithTotal) {
        _xifexpression_6 = new Code.Flag(true);
      } else {
        _xifexpression_6 = null;
      }
      Code.Entries _xifexpression_7 = null;
      boolean _isEmpty_1 = it.getAggregations().isEmpty();
      if (_isEmpty_1) {
        _xifexpression_7 = null;
      } else {
        _xifexpression_7 = this.aggs(target, it.getAggregations());
      }
      final Code.Call fused = ElasticsearchBodies.call("hybrid", "index", _text, "legs", _arr, "fusion", _text_1, 
        "k", _num, "size", _num_1, "offset", _num_2, "key", _text_2, 
        "bounds", _boundsOf, "group", _xifexpression_2, "group_size", _xifexpression_3, 
        "mmr", _xifexpression_4, "sort", _xifexpression_5, 
        "total", _xifexpression_6, "aggs", _xifexpression_7);
      Code.Call _paged = this.paged(fused, it, target);
      Code.Return _return = new Code.Return(_paged);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The _source, explain and highlight of a hybrid leg, and the group field as a doc value; the total, sort, aggregations, groups,
   * cursor and mmr apply to the fused records in the runtime.
   */
  private Code.Entries legShape(final HybridSearch search, final VectorCollection collection, final ElasticsearchIndex target, final Code.Entries legBody) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries shaped = this.shape(search, collection, target, legBody, search.getOutput(), search.getExcluded(), search.isWithVectors(), false);
      final String group = this.groupOf(search, target);
      if ((group != null)) {
        shaped.put("docvalue_fields", ElasticsearchBodies.arr(ElasticsearchBodies.text(group)));
      }
      _xblockexpression = shaped;
    }
    return _xblockexpression;
  }

  /**
   * MMR over the fused records: the diversity field (named, else the collection's only vector field), its similarity and the window.
   */
  private Code.Entries fusedMmr(final HybridSearch search, final ElasticsearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final ElasticsearchIndex.Field diversity = target.field(ModelUtil.fieldName(this.vectorFieldOf(search.getCollection(), search.getMmr().getVectorField())));
      boolean _sparse = diversity.vector().sparse();
      if (_sparse) {
        throw Unsupported.of("the \'mmr\' clause on a sparse field; maximal marginal relevance compares dense vectors");
      }
      int _topK = search.getTopK();
      int _offsetOf = this.offsetOf(search);
      final int count = (_topK + _offsetOf);
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(search.getMmr(), "candidates");
      if (_written) {
        _xifexpression = Math.max(search.getMmr().getCandidates(), count);
      } else {
        _xifexpression = Math.max((10 * count), 10);
      }
      _xblockexpression = ElasticsearchBodies.obj("field", ElasticsearchBodies.text(diversity.name()), "kind", ElasticsearchBodies.text(ElasticsearchBodies.exactKind(diversity.vector().metric(), diversity.vector().element())), 
        "lambda", ElasticsearchBodies.num(search.getMmr().getDiversityLambda()), "window", ElasticsearchBodies.num(_xifexpression));
    }
    return _xblockexpression;
  }

  /**
   * Leg weights for weighted and relative-score fusion; rrf is unweighted by definition.
   */
  private List<Double> legWeights(final HybridSearch search, final String fusionKind) {
    List<Double> _xblockexpression = null;
    {
      final Function1<HybridLeg, Boolean> _function = (HybridLeg leg) -> {
        return Boolean.valueOf(ModelUtil.written(leg, "weight"));
      };
      final int weighted = IterableExtensions.size(IterableExtensions.<HybridLeg>filter(search.getLegs(), _function));
      boolean _equals = Objects.equals(fusionKind, "rrf");
      if (_equals) {
        if (((weighted > 0) || ModelUtil.written(search, "alpha"))) {
          throw Unsupported.of("leg weights or alpha with rrf fusion, which is unweighted");
        }
        final Function1<HybridLeg, Double> _function_1 = (HybridLeg it) -> {
          return Double.valueOf(1.0);
        };
        return IterableExtensions.<Double>toList(ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_1));
      }
      boolean _written = ModelUtil.written(search, "alpha");
      if (_written) {
        if ((((search.getLegs().size() != 2) || (!(IterableExtensions.<HybridLeg>head(search.getLegs()) instanceof VectorLeg))) || (weighted > 0))) {
          throw Unsupported.of("alpha on other than one vector leg followed by one other leg without weights");
        }
        double _alpha = search.getAlpha();
        double _doubleValue = BigDecimal.ONE.subtract(BigDecimal.valueOf(search.getAlpha())).doubleValue();
        return Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(_alpha), Double.valueOf(_doubleValue)));
      }
      if ((weighted == 0)) {
        final Function1<HybridLeg, Double> _function_2 = (HybridLeg it) -> {
          return Double.valueOf(1.0);
        };
        return IterableExtensions.<Double>toList(ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_2));
      }
      int _size = search.getLegs().size();
      boolean _notEquals = (weighted != _size);
      if (_notEquals) {
        throw Unsupported.of("weights on some hybrid legs only");
      }
      final Function1<HybridLeg, Double> _function_3 = (HybridLeg leg) -> {
        Object _feature = this.feature(leg, "weight");
        return ((Double) _feature);
      };
      _xblockexpression = IterableExtensions.<Double>toList(ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_3));
    }
    return _xblockexpression;
  }

  /**
   * The average-vector strategy over example documents or vectors, computed by the runtime; the examples are not returned.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "reranker");
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("the \'highlight\' clause on a recommendation; highlighting marks the terms of a text query, which a recommendation has not");
      }
      this.queryExtensions(it);
      this.combinations(it);
      boolean _isEmpty_1 = it.getPositives().isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("recommend without positive examples");
      }
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final ElasticsearchIndex lookup = this.schema.index(_elvis);
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(searched));
      boolean _sparse = mapped.vector().sparse();
      if (_sparse) {
        throw Unsupported.of("recommendations on a sparse field; the average-vector strategy averages dense vectors");
      }
      final List<Code.Expr> positiveIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> positiveVectors = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeVectors = CollectionLiterals.<Code.Expr>newArrayList();
      EList<Literal> _positives = it.getPositives();
      for (final Literal example : _positives) {
        boolean _isVector = this.isVector(example);
        if (_isVector) {
          Code.Expr _convert = this.convert(example, mapped);
          positiveVectors.add(_convert);
        } else {
          Code.Expr _of = Literals.of(example);
          positiveIds.add(_of);
        }
      }
      EList<Literal> _negatives = it.getNegatives();
      for (final Literal example_1 : _negatives) {
        boolean _isVector_1 = this.isVector(example_1);
        if (_isVector_1) {
          Code.Expr _convert_1 = this.convert(example_1, mapped);
          negativeVectors.add(_convert_1);
        } else {
          Code.Expr _of_1 = Literals.of(example_1);
          negativeIds.add(_of_1);
        }
      }
      final List<Code.Expr> filters = this.clauses(it, it.getCollection(), target, it.getFilter(), null);
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final List<Literal> examples = CollectionLiterals.<Literal>newArrayList();
        final Function1<Literal, Boolean> _function = (Literal e) -> {
          boolean _isVector_2 = this.isVector(e);
          return Boolean.valueOf((!_isVector_2));
        };
        Iterable<Literal> _filter = IterableExtensions.<Literal>filter(it.getPositives(), _function);
        Iterables.<Literal>addAll(examples, _filter);
        final Function1<Literal, Boolean> _function_1 = (Literal e) -> {
          boolean _isVector_2 = this.isVector(e);
          return Boolean.valueOf((!_isVector_2));
        };
        Iterable<Literal> _filter_1 = IterableExtensions.<Literal>filter(it.getNegatives(), _function_1);
        Iterables.<Literal>addAll(examples, _filter_1);
        boolean _isEmpty_2 = examples.isEmpty();
        boolean _not_1 = (!_isEmpty_2);
        if (_not_1) {
          Code.Entries _obj = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("must_not", ElasticsearchBodies.arr(ElasticsearchBodies.obj("ids", ElasticsearchBodies.obj("values", this.idsOf(examples))))));
          filters.add(_obj);
        }
      }
      if ((((it.getMmr() != null) && (it.getMmr().getVectorField() != null)) && (it.getMmr().getVectorField() != searched))) {
        throw Unsupported.of("mmr on another vector field than the searched one");
      }
      Code.Nil _nil = new Code.Nil();
      final Code.Entries requestBody = this.cursorOrder(it, target, this.shape(it, it.getCollection(), target, this.knnBody(it, target, mapped, _nil, filters), it.getOutput(), this.excludedOf(it), it.isWithVectors()));
      final boolean nativeScore = (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE));
      final String group = this.groupOf(it, target);
      Code.Text _text = ElasticsearchBodies.text(target.name());
      Code.Text _text_1 = ElasticsearchBodies.text(lookup.name());
      Code.Text _text_2 = ElasticsearchBodies.text(mapped.name());
      Code.Text _text_3 = ElasticsearchBodies.text(target.key());
      Code.Entries _xifexpression = null;
      BoostBlock _boost = it.getBoost();
      boolean _tripleNotEquals = (_boost != null);
      if (_tripleNotEquals) {
        _xifexpression = ElasticsearchBodies.relevance();
      } else {
        _xifexpression = ElasticsearchBodies.scoreForm(mapped.vector(), nativeScore);
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_3 = positiveIds.isEmpty();
      if (_isEmpty_3) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.arr(positiveIds);
      }
      Code.Items _xifexpression_2 = null;
      boolean _isEmpty_4 = positiveVectors.isEmpty();
      if (_isEmpty_4) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.arr(positiveVectors);
      }
      Code.Items _xifexpression_3 = null;
      boolean _isEmpty_5 = negativeIds.isEmpty();
      if (_isEmpty_5) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = ElasticsearchBodies.arr(negativeIds);
      }
      Code.Items _xifexpression_4 = null;
      boolean _isEmpty_6 = negativeVectors.isEmpty();
      if (_isEmpty_6) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = ElasticsearchBodies.arr(negativeVectors);
      }
      Code.Entries _boundsOf = this.boundsOf(it.getBounds());
      Code.Text _xifexpression_5 = null;
      if ((group == null)) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = ElasticsearchBodies.text(group);
      }
      Code.Call _paged = this.paged(ElasticsearchBodies.call("recommend", "index", _text, "lookup", _text_1, "field", _text_2, "key", _text_3, 
        "body", requestBody, "score", _xifexpression, "positive_ids", _xifexpression_1, 
        "positive_vectors", _xifexpression_2, "negative_ids", _xifexpression_3, 
        "negative_vectors", _xifexpression_4, "bounds", _boundsOf, "group", _xifexpression_5), it, target);
      Code.Return _return = new Code.Return(_paged);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr listOf(final Code.Expr value) {
    Code.Items _xifexpression = null;
    if ((value instanceof Code.Items)) {
      _xifexpression = ((Code.Items)value);
    } else {
      _xifexpression = ElasticsearchBodies.arr(value);
    }
    return _xifexpression;
  }

  private boolean isVector(final Literal example) {
    return (((example instanceof ListLit) || (example instanceof SparseLit)) || ((example instanceof ParamRef) && Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("vector", "sparse_vector")).contains(((ParamRef) example).getParam().getType().getLiteral())));
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      final List<Code.Expr> slices = this.clauses(it, it.getCollection(), target, null, null);
      final List<Code.Expr> fields = CollectionLiterals.<Code.Expr>newArrayList();
      if (((it.getOutput() != null) && (!it.getOutput().isAllFields()))) {
        EList<Field> _fields = it.getOutput().getFields();
        for (final Field f : _fields) {
          Field _primaryKey = it.getCollection().getPrimaryKey();
          boolean _tripleNotEquals = (f != _primaryKey);
          if (_tripleNotEquals) {
            Code.Text _text = ElasticsearchBodies.text(target.field(ModelUtil.fieldName(f)).name());
            fields.add(_text);
          }
        }
      }
      Code.Text _text_1 = ElasticsearchBodies.text(target.name());
      final Function1<Literal, Code.Expr> _function = (Literal i) -> {
        return Literals.of(i);
      };
      Code.Items _items = Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      Code.Text _text_2 = ElasticsearchBodies.text(target.key());
      Code.Items _xifexpression = null;
      if (((it.getOutput() == null) || it.getOutput().isAllFields())) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.arr(fields);
      }
      Code.Flag _xifexpression_1 = null;
      boolean _isWithVectors = it.isWithVectors();
      if (_isWithVectors) {
        _xifexpression_1 = new Code.Flag(true);
      } else {
        _xifexpression_1 = null;
      }
      Code.Items _xifexpression_2 = null;
      boolean _isEmpty = slices.isEmpty();
      if (_isEmpty) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = ElasticsearchBodies.arr(slices);
      }
      Code.Call _call = ElasticsearchBodies.call("fetch", "index", _text_1, "values", _items, "key", _text_2, 
        "fields", _xifexpression, "vectors", _xifexpression_1, 
        "filters", _xifexpression_2);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      this.schema.note(it, "exact");
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_count");
      Code.Call _call = ElasticsearchBodies.call("request", "method", ElasticsearchBodies.text("POST"), "path", ElasticsearchBodies.text(_plus_1), 
        "body", ElasticsearchBodies.obj("query", this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null))), "select", ElasticsearchBodies.arr(ElasticsearchBodies.text("count")));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      if ((ModelUtil.written(it, "sliceTotal") && (((it.getSliceTotal() < 1) || (it.getSliceIndex() < 0)) || (it.getSliceIndex() >= it.getSliceTotal())))) {
        int _sliceIndex = it.getSliceIndex();
        String _plus = ("slice " + Integer.valueOf(_sliceIndex));
        String _plus_1 = (_plus + " of ");
        int _sliceTotal = it.getSliceTotal();
        String _plus_2 = (_plus_1 + Integer.valueOf(_sliceTotal));
        String _plus_3 = (_plus_2 + "; a slice number is at least 0 and below the slice count");
        throw Unsupported.of(_plus_3);
      }
      final List<Code.Expr> ordering = CollectionLiterals.<Code.Expr>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        Code.Entries _sortEntry = this.sortEntry(target, clause, false);
        ordering.add(_sortEntry);
      }
      final ElasticsearchIndex.Field keyField = target.keyField();
      Code.Entries _obj = ElasticsearchBodies.obj(keyField.exact(), ElasticsearchBodies.obj("order", ElasticsearchBodies.text("asc")));
      ordering.add(_obj);
      Code.Entries _boolOf = this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null));
      Code.Items _arr = ElasticsearchBodies.arr(ordering);
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = it.getScrollLimit();
      } else {
        _xifexpression = 10;
      }
      Code.Num _num = ElasticsearchBodies.num(_xifexpression);
      Code.Num _xifexpression_1 = null;
      int _offsetOf = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        _xifexpression_1 = ElasticsearchBodies.num(this.offsetOf(it));
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries requestBody = ElasticsearchBodies.obj("query", _boolOf, "sort", _arr, 
        "size", _num, "from", _xifexpression_1);
      String _cursor = it.getCursor();
      boolean _tripleNotEquals = (_cursor != null);
      if (_tripleNotEquals) {
        if (((!it.getSortClauses().isEmpty()) || (this.offsetOf(it) > 0))) {
          throw Unsupported.of("the \'cursor\' clause with sort or offset; search_after continues after the key of the last page");
        }
        requestBody.put("search_after", ElasticsearchBodies.arr(this.cursorValue(keyField, it.getCursor())));
      }
      if ((ModelUtil.written(it, "sliceTotal") && (it.getSliceTotal() > 1))) {
        requestBody.put("slice", ElasticsearchBodies.obj("id", ElasticsearchBodies.num(it.getSliceIndex()), "max", ElasticsearchBodies.num(it.getSliceTotal())));
      }
      Code.Call _searchCall = this.searchCall(target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors()), null, null, null);
      Code.Return _return = new Code.Return(_searchCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr cursorValue(final ElasticsearchIndex.Field keyField, final String cursorText) {
    Code.Text _xblockexpression = null;
    {
      Code.Expr _get = keyField.mapping().get("type");
      final String mappedType = ((Code.Text) _get).value();
      boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("integer", "long", "byte")).contains(mappedType);
      if (_contains) {
        try {
          return ElasticsearchBodies.num(Long.parseLong(cursorText));
        } catch (final Throwable _t) {
          if (_t instanceof NumberFormatException) {
            throw Unsupported.of((("the cursor " + cursorText) + " on an integer key"));
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
      _xblockexpression = ElasticsearchBodies.text(cursorText);
    }
    return _xblockexpression;
  }

  private Code.Entries sortEntry(final ElasticsearchIndex target, final SortClause clause, final boolean scored) {
    Code.Entries _xblockexpression = null;
    {
      String _xifexpression = null;
      ESortDirection _direction = clause.getDirection();
      boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
      if (_equals) {
        _xifexpression = "desc";
      } else {
        _xifexpression = "asc";
      }
      final Code.Text order = ElasticsearchBodies.text(_xifexpression);
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        if ((!scored)) {
          throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
        }
        return ElasticsearchBodies.obj("_score", ElasticsearchBodies.obj("order", order));
      }
      final FieldPath path = clause.getField();
      if (((path.getRawPath() == null) && (!(path.getField() instanceof PayloadField)))) {
        throw Unsupported.of("sorting by a vector field");
      }
      String _xifexpression_1 = null;
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        _xifexpression_1 = ElasticsearchBodies.field(path.getRawPath());
      } else {
        String _xblockexpression_1 = null;
        {
          final ElasticsearchIndex.Field top = target.field(ModelUtil.fieldName(path.getField()));
          final Field leaf = ModelUtil.leaf(path);
          final String dotted = this.fieldPath(target, path);
          String _xifexpression_2 = null;
          boolean _isEmpty = path.getSubFields().isEmpty();
          if (_isEmpty) {
            _xifexpression_2 = top.exact();
          } else {
            String _xifexpression_3 = null;
            if (((leaf instanceof PayloadField) && ((PayloadField) leaf).isFullText())) {
              _xifexpression_3 = ((dotted + ".") + ElasticsearchBodies.RAW);
            } else {
              _xifexpression_3 = dotted;
            }
            _xifexpression_2 = _xifexpression_3;
          }
          _xblockexpression_1 = _xifexpression_2;
        }
        _xifexpression_1 = _xblockexpression_1;
      }
      final String name = _xifexpression_1;
      _xblockexpression = ElasticsearchBodies.obj(name, ElasticsearchBodies.obj("order", order, "missing", ElasticsearchBodies.text("_last")));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      final Code.Entries matching = this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null));
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      Code.Entries _xifexpression = null;
      if (first) {
        _xifexpression = ElasticsearchBodies.obj("query", matching, "sort", ElasticsearchBodies.arr(ElasticsearchBodies.obj(target.keyField().exact(), ElasticsearchBodies.obj("order", ElasticsearchBodies.text("asc")))), "size", ElasticsearchBodies.num(it.getSampleSize()));
      } else {
        _xifexpression = ElasticsearchBodies.obj("query", ElasticsearchBodies.obj("function_score", ElasticsearchBodies.obj("query", matching, "random_score", Code.entries(), "boost_mode", ElasticsearchBodies.text("replace"))), "size", ElasticsearchBodies.num(it.getSampleSize()));
      }
      final Code.Entries requestBody = _xifexpression;
      Code.Call _searchCall = this.searchCall(target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false), null, null, null);
      Code.Return _return = new Code.Return(_searchCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      final ElasticsearchIndex target = this.schema.index(it.getCollection());
      final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(this.vectorFieldOf(it.getCollection(), it.getVectorField())));
      boolean _sparse = mapped.vector().sparse();
      if (_sparse) {
        throw Unsupported.of("a distance matrix of sparse vectors; the kNN search of the runtime compares dense vectors");
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        _xifexpression = it.getSampleSize();
      } else {
        _xifexpression = 10;
      }
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        _xifexpression_1 = it.getMatrixLimit();
      } else {
        _xifexpression_1 = 3;
      }
      Code.Call _call = ElasticsearchBodies.call("distance_matrix", "index", ElasticsearchBodies.text(target.name()), "field", ElasticsearchBodies.text(mapped.name()), "key", ElasticsearchBodies.text(target.key()), 
        "sample", ElasticsearchBodies.num(_xifexpression), "limit", ElasticsearchBodies.num(_xifexpression_1), 
        "score", ElasticsearchBodies.scoreForm(mapped.vector(), false), "query", this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null)));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + ElasticsearchStatements.NO_GENERATION));
  }

  protected List<Code.Stmt> _body(final BatchQuery it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> calls = CollectionLiterals.<Code.Expr>newArrayList();
      final String owner = this.current;
      int position = 0;
      EList<QueryMethod> _queries = it.getQueries();
      for (final QueryMethod batched : _queries) {
        {
          final String helperName = ((owner + "_query_") + Integer.valueOf(position));
          this.current = helperName;
          List<String> _parameterNames = ModelUtil.parameterNames(batched);
          List<Code.Stmt> _body = this.body(batched);
          ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
          ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList();
          Program.Method _method = new Program.Method(helperName, _parameterNames, _body, _newArrayList, _newArrayList_1, true);
          this.helpers.add(_method);
          Code.Call _self = Code.self(helperName);
          calls.add(_self);
          position = (position + 1);
        }
      }
      this.current = owner;
      Code.Items _items = Code.items(calls);
      Code.Return _return = new Code.Return(_items);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Method it) {
    String _name = it.eClass().getName();
    String _plus = (ElasticsearchStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private Object feature(final EObject object, final String featureName) {
    Object _xblockexpression = null;
    {
      final EStructuralFeature slot = object.eClass().getEStructuralFeature(featureName);
      Object _xifexpression = null;
      if ((slot == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = object.eGet(slot);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private int offsetOf(final EObject statement) {
    Integer _xifexpression = null;
    boolean _written = ModelUtil.written(statement, "offset");
    if (_written) {
      Object _feature = this.feature(statement, "offset");
      _xifexpression = ((Integer) _feature);
    } else {
      _xifexpression = Integer.valueOf(0);
    }
    return (_xifexpression).intValue();
  }

  private List<Field> excludedOf(final EObject statement) {
    Object _elvis = null;
    Object _feature = this.feature(statement, "excluded");
    if (_feature != null) {
      _elvis = _feature;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    return ((List<Field>) _elvis);
  }

  private Code.Entries boundsOf(final ScoreBounds scoreBounds) {
    Code.Entries _xblockexpression = null;
    {
      if ((scoreBounds == null)) {
        return null;
      }
      Code.Num _xifexpression = null;
      boolean _written = ModelUtil.written(scoreBounds, "min");
      if (_written) {
        _xifexpression = ElasticsearchBodies.num(scoreBounds.getMin());
      } else {
        _xifexpression = null;
      }
      Code.Num _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(scoreBounds, "max");
      if (_written_1) {
        _xifexpression_1 = ElasticsearchBodies.num(scoreBounds.getMax());
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries result = ElasticsearchBodies.obj("min", _xifexpression, "max", _xifexpression_1);
      Code.Entries _xifexpression_2 = null;
      boolean _isEmpty = result.entries().isEmpty();
      if (_isEmpty) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = result;
      }
      _xblockexpression = _xifexpression_2;
    }
    return _xblockexpression;
  }

  private Code.Call searchCall(final ElasticsearchIndex target, final Code.Entries requestBody, final Code.Entries form, final ScoreBounds scoreBounds, final String group) {
    Code.Text _text = ElasticsearchBodies.text(target.name());
    Code.Text _text_1 = ElasticsearchBodies.text(target.key());
    Code.Entries _boundsOf = this.boundsOf(scoreBounds);
    Code.Text _xifexpression = null;
    if ((group == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = ElasticsearchBodies.text(group);
    }
    return ElasticsearchBodies.call("search", "index", _text, "body", requestBody, "key", _text_1, "score", form, "bounds", _boundsOf, 
      "group", _xifexpression);
  }

  /**
   * The searched vector field: the named one, else the collection's only vector field.
   */
  private VectorField vectorFieldOf(final VectorCollection collection, final VectorField named) {
    VectorField _xblockexpression = null;
    {
      if ((named != null)) {
        boolean _contains = collection.getFields().contains(named);
        boolean _not = (!_contains);
        if (_not) {
          String _fieldName = ModelUtil.fieldName(named);
          String _plus = ("vector field " + _fieldName);
          String _plus_1 = (_plus + ", which is not a field of collection ");
          String _name = collection.getName();
          String _plus_2 = (_plus_1 + _name);
          throw Unsupported.of(_plus_2);
        }
        return named;
      }
      final List<VectorField> vectorFields = ModelUtil.vectorFields(collection);
      int _size = vectorFields.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        int _size_1 = vectorFields.size();
        String _plus_3 = ("a search without \'using\' in a collection with " + Integer.valueOf(_size_1));
        String _plus_4 = (_plus_3 + " vector fields");
        throw Unsupported.of(_plus_4);
      }
      _xblockexpression = IterableExtensions.<VectorField>head(vectorFields);
    }
    return _xblockexpression;
  }

  /**
   * A statement's filter, its ids and its tenant, namespace and partition conditions, as filter clauses.
   */
  private List<Code.Expr> clauses(final EObject statement, final VectorCollection collection, final ElasticsearchIndex target, final FilterBlock block, final Code.Expr idValues) {
    List<Code.Expr> _xblockexpression = null;
    {
      final List<Code.Expr> result = CollectionLiterals.<Code.Expr>newArrayList();
      if ((idValues != null)) {
        Code.Entries _obj = ElasticsearchBodies.obj("ids", ElasticsearchBodies.obj("values", idValues));
        result.add(_obj);
      }
      if ((block != null)) {
        Code.Entries _query = new ElasticsearchFilter(target).query(block.getCondition());
        result.add(_query);
      }
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Entries _term = ElasticsearchSchema.term(target.tenant(), ElasticsearchBodies.text(tenant.getName()));
        result.add(_term);
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Entries _term_1 = ElasticsearchSchema.term(target.namespace(), ElasticsearchBodies.text(((String) namespace)));
        result.add(_term_1);
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition single = ((Partition) _feature_1);
      Object _feature_2 = this.feature(statement, "partitions");
      final List<Partition> several = ((List<Partition>) _feature_2);
      final List<Code.Expr> named = CollectionLiterals.<Code.Expr>newArrayList();
      if ((single != null)) {
        Code.Text _text = ElasticsearchBodies.text(single.getName());
        named.add(_text);
      }
      if ((several != null)) {
        for (final Partition p : several) {
          Code.Text _text_1 = ElasticsearchBodies.text(p.getName());
          named.add(_text_1);
        }
      }
      boolean _isEmpty = named.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _partition = target.partition();
        boolean _tripleEquals = (_partition == null);
        if (_tripleEquals) {
          throw Unsupported.of("partitions of a collection declared without partitions");
        }
        Code.Entries _obj_1 = ElasticsearchBodies.obj("terms", ElasticsearchBodies.obj(target.partition(), ElasticsearchBodies.arr(named)));
        result.add(_obj_1);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Entries boolOf(final List<Code.Expr> filters) {
    Code.Entries _xifexpression = null;
    boolean _isEmpty = filters.isEmpty();
    if (_isEmpty) {
      _xifexpression = ElasticsearchBodies.obj("match_all", Code.entries());
    } else {
      _xifexpression = ElasticsearchBodies.obj("bool", ElasticsearchBodies.obj("filter", ElasticsearchBodies.arr(filters)));
    }
    return _xifexpression;
  }

  /**
   * Document ids as JSON strings (_id is a string); parameters become strings in the runtime.
   */
  private Code.Expr idsOf(final List<Literal> values) {
    Code.Call _xblockexpression = null;
    {
      boolean _isEmpty = values.isEmpty();
      if (_isEmpty) {
        return null;
      }
      final Function1<Literal, Boolean> _function = (Literal v) -> {
        return Boolean.valueOf(((v instanceof StringLit) || (v instanceof NumberLit)));
      };
      boolean _forall = IterableExtensions.<Literal>forall(values, _function);
      if (_forall) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal v) -> {
          String _xifexpression = null;
          if ((v instanceof StringLit)) {
            _xifexpression = ((StringLit) v).getValue();
          } else {
            _xifexpression = ElasticsearchFilter.number(((NumberLit) v).getValue());
          }
          Code.Text _text = ElasticsearchBodies.text(_xifexpression);
          return ((Code.Expr) _text);
        };
        return ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(values, _function_1)));
      }
      final Function1<Literal, Code.Expr> _function_2 = (Literal v) -> {
        return Literals.of(v);
      };
      _xblockexpression = ElasticsearchBodies.call("ids", "values", Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(values, _function_2))));
    }
    return _xblockexpression;
  }

  /**
   * The _source of the hits: the key and the output fields, vectors only with with-vectors (or named in the output).
   */
  private Code.Entries shape(final EObject statement, final VectorCollection collection, final ElasticsearchIndex target, final Code.Entries requestBody, final OutputSpec outputSpec, final List<Field> excluded, final boolean vectors) {
    return this.shape(statement, collection, target, requestBody, outputSpec, excluded, vectors, true);
  }

  /**
   * The shape of one request; without the result clauses (total, sort, aggregations, groups) for the legs of a hybrid search.
   */
  private Code.Entries shape(final EObject statement, final VectorCollection collection, final ElasticsearchIndex target, final Code.Entries requestBody, final OutputSpec outputSpec, final List<Field> excluded, final boolean vectors, final boolean results) {
    Code.Entries _xblockexpression = null;
    {
      final List<Code.Expr> includes = CollectionLiterals.<Code.Expr>newArrayList();
      boolean returnVectors = vectors;
      if (((outputSpec != null) && (!outputSpec.isAllFields()))) {
        Code.Text _text = ElasticsearchBodies.text(target.key());
        includes.add(_text);
        EList<Field> _fields = outputSpec.getFields();
        for (final Field f : _fields) {
          {
            if ((f instanceof VectorField)) {
              returnVectors = true;
            }
            final Code.Text named = ElasticsearchBodies.text(target.field(ModelUtil.fieldName(f)).name());
            if ((((f != collection.getPrimaryKey()) && (!excluded.contains(f))) && (!includes.contains(named)))) {
              includes.add(named);
            }
          }
        }
        if (vectors) {
          List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
          for (final VectorField v : _vectorFields) {
            {
              final Code.Text named = ElasticsearchBodies.text(target.field(ModelUtil.fieldName(v)).name());
              if (((!excluded.contains(v)) && (!includes.contains(named)))) {
                includes.add(named);
              }
            }
          }
        }
      }
      final Function1<Field, Code.Expr> _function = (Field f_1) -> {
        Code.Text _text_1 = ElasticsearchBodies.text(target.field(ModelUtil.fieldName(f_1)).name());
        return ((Code.Expr) _text_1);
      };
      final List<Code.Expr> excludes = IterableExtensions.<Code.Expr>toList(ListExtensions.<Field, Code.Expr>map(excluded, _function));
      Code.Items _xifexpression = null;
      boolean _isEmpty = includes.isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = ElasticsearchBodies.arr(includes);
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_1 = excludes.isEmpty();
      if (_isEmpty_1) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = ElasticsearchBodies.arr(excludes);
      }
      Code.Flag _flag = new Code.Flag((!returnVectors));
      requestBody.put("_source", ElasticsearchBodies.obj("includes", _xifexpression, "excludes", _xifexpression_1, 
        "exclude_vectors", _flag));
      if ((results && ModelUtil.written(statement, "withTotal"))) {
        Code.Flag _flag_1 = new Code.Flag(true);
        requestBody.put("track_total_hits", _flag_1);
      }
      boolean _written = ModelUtil.written(statement, "explain");
      if (_written) {
        Code.Flag _flag_2 = new Code.Flag(true);
        requestBody.put("explain", _flag_2);
      }
      Object _elvis = null;
      Object _feature = this.feature(statement, "highlighted");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<Field> highlighted = ((List<Field>) _elvis);
      boolean _isEmpty_2 = highlighted.isEmpty();
      boolean _not = (!_isEmpty_2);
      if (_not) {
        final Code.Entries highlightFields = Code.entries();
        for (final Field f_1 : highlighted) {
          {
            final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(f_1));
            boolean _fullText = mapped.fullText();
            boolean _not_1 = (!_fullText);
            if (_not_1) {
              String _name = mapped.name();
              String _plus = ("highlighting " + _name);
              String _plus_1 = (_plus + ", which is not a full-text field");
              throw Unsupported.of(_plus_1);
            }
            highlightFields.put(mapped.name(), Code.entries());
          }
        }
        requestBody.put("highlight", ElasticsearchBodies.obj("fields", highlightFields));
      }
      if ((!results)) {
        this.extensionBody(statement, requestBody);
        return requestBody;
      }
      Object _elvis_1 = null;
      Object _feature_1 = this.feature(statement, "sortClauses");
      if (_feature_1 != null) {
        _elvis_1 = _feature_1;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      final List<SortClause> sortClauses = ((List<SortClause>) _elvis_1);
      if (((!sortClauses.isEmpty()) && (!requestBody.has("sort")))) {
        final Function1<SortClause, Code.Expr> _function_1 = (SortClause c) -> {
          Code.Entries _sortEntry = this.sortEntry(target, c, true);
          return ((Code.Expr) _sortEntry);
        };
        requestBody.put("sort", ElasticsearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<SortClause, Code.Expr>map(sortClauses, _function_1))));
      }
      if ((IterableExtensions.<SortClause>exists(sortClauses, ((Function1<SortClause, Boolean>) (SortClause it) -> {
        boolean _isByRelevance = it.isByRelevance();
        return Boolean.valueOf((!_isByRelevance));
      })) && (!requestBody.has("track_scores")))) {
        Code.Flag _flag_3 = new Code.Flag(true);
        requestBody.put("track_scores", _flag_3);
      }
      Object _elvis_2 = null;
      Object _feature_2 = this.feature(statement, "aggregations");
      if (_feature_2 != null) {
        _elvis_2 = _feature_2;
      } else {
        ArrayList<Object> _newArrayList_2 = CollectionLiterals.<Object>newArrayList();
        _elvis_2 = _newArrayList_2;
      }
      final List<AggregationClause> aggregations = ((List<AggregationClause>) _elvis_2);
      boolean _isEmpty_3 = aggregations.isEmpty();
      boolean _not_1 = (!_isEmpty_3);
      if (_not_1) {
        requestBody.put("aggs", this.aggs(target, aggregations));
      }
      final String group = this.groupOf(statement, target);
      if ((group != null)) {
        Integer _xifexpression_2 = null;
        boolean _written_1 = ModelUtil.written(statement, "groupSize");
        if (_written_1) {
          Object _feature_3 = this.feature(statement, "groupSize");
          _xifexpression_2 = ((Integer) _feature_3);
        } else {
          _xifexpression_2 = Integer.valueOf(1);
        }
        final Integer size = _xifexpression_2;
        requestBody.put("collapse", ElasticsearchBodies.obj("field", ElasticsearchBodies.text(group), "inner_hits", ElasticsearchBodies.obj("name", ElasticsearchBodies.text(ElasticsearchBodies.GROUP), "size", ElasticsearchBodies.num((size).intValue()), 
          "_source", requestBody.get("_source"))));
      }
      this.extensionBody(statement, requestBody);
      _xblockexpression = requestBody;
    }
    return _xblockexpression;
  }

  /**
   * The collapse field of a grouped search: the exact form of the group-by field.
   */
  private String groupOf(final EObject statement, final ElasticsearchIndex target) {
    String _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "groupBy");
      final FieldPath path = ((FieldPath) _feature);
      if ((path == null)) {
        return null;
      }
      if (((path.getRawPath() == null) && (!(path.getField() instanceof PayloadField)))) {
        throw Unsupported.of("grouping by a vector field");
      }
      if (((path.getRawPath() != null) || (!path.getSubFields().isEmpty()))) {
        return this.fieldPath(target, path);
      }
      _xblockexpression = target.field(ModelUtil.fieldName(path.getField())).exact();
    }
    return _xblockexpression;
  }

  /**
   * Aggregations by name (the alias, else kind_field): metrics, terms, histograms, ranges and percentiles.
   */
  private Code.Entries aggs(final ElasticsearchIndex target, final List<AggregationClause> aggregationClauses) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      for (final AggregationClause clause : aggregationClauses) {
        {
          String _xifexpression = null;
          String _rawPath = clause.getField().getRawPath();
          boolean _tripleNotEquals = (_rawPath != null);
          if (_tripleNotEquals) {
            _xifexpression = ElasticsearchBodies.field(clause.getField().getRawPath());
          } else {
            String _xblockexpression_1 = null;
            {
              final ElasticsearchIndex.Field top = target.field(ModelUtil.fieldName(clause.getField().getField()));
              String _xifexpression_1 = null;
              boolean _isEmpty = clause.getField().getSubFields().isEmpty();
              if (_isEmpty) {
                _xifexpression_1 = top.exact();
              } else {
                _xifexpression_1 = this.fieldPath(target, clause.getField());
              }
              _xblockexpression_1 = _xifexpression_1;
            }
            _xifexpression = _xblockexpression_1;
          }
          final String path = _xifexpression;
          final Code.Text fieldName = ElasticsearchBodies.text(path);
          Code.Num _xifexpression_1 = null;
          boolean _written = ModelUtil.written(clause, "bucketCount");
          if (_written) {
            _xifexpression_1 = ElasticsearchBodies.num(clause.getBucketCount());
          } else {
            _xifexpression_1 = null;
          }
          final Code.Num size = _xifexpression_1;
          Code.Entries _switchResult = null;
          String _literal = clause.getKind().getLiteral();
          if (_literal != null) {
            switch (_literal) {
              case "count":
                _switchResult = ElasticsearchBodies.obj("value_count", ElasticsearchBodies.obj("field", fieldName));
                break;
              case "sum":
              case "avg":
              case "min":
              case "max":
              case "stats":
              case "percentiles":
              case "cardinality":
                _switchResult = ElasticsearchBodies.obj(clause.getKind().getLiteral(), ElasticsearchBodies.obj("field", fieldName));
                break;
              case "distinct_count":
                _switchResult = ElasticsearchBodies.obj("cardinality", ElasticsearchBodies.obj("field", fieldName));
                break;
              case "terms":
              case "facet":
                _switchResult = ElasticsearchBodies.obj("terms", ElasticsearchBodies.obj("field", fieldName, "size", size));
                break;
              case "histogram":
                Code.Entries _xblockexpression_2 = null;
                {
                  if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                    throw Unsupported.of("a histogram without a numeric interval");
                  }
                  _xblockexpression_2 = ElasticsearchBodies.obj("histogram", ElasticsearchBodies.obj("field", fieldName, "interval", ElasticsearchBodies.num(Double.parseDouble(clause.getInterval()))));
                }
                _switchResult = _xblockexpression_2;
                break;
              case "date_histogram":
                Code.Entries _xblockexpression_3 = null;
                {
                  String _interval = clause.getInterval();
                  boolean _tripleEquals = (_interval == null);
                  if (_tripleEquals) {
                    throw Unsupported.of("a date histogram without an interval");
                  }
                  _xblockexpression_3 = ElasticsearchBodies.obj("date_histogram", ElasticsearchBodies.obj("field", fieldName, "fixed_interval", ElasticsearchBodies.text(clause.getInterval())));
                }
                _switchResult = _xblockexpression_3;
                break;
              default:
                throw Unsupported.of("range aggregations; vecDSL gives no ranges, which the Elasticsearch range aggregation needs");
            }
          } else {
            throw Unsupported.of("range aggregations; vecDSL gives no ranges, which the Elasticsearch range aggregation needs");
          }
          final Code.Entries definition = _switchResult;
          String _elvis = null;
          String _alias = clause.getAlias();
          if (_alias != null) {
            _elvis = _alias;
          } else {
            String _literal_1 = clause.getKind().getLiteral();
            String _plus = (_literal_1 + "_");
            String _replace = path.replace(".", "_");
            String _plus_1 = (_plus + _replace);
            _elvis = _plus_1;
          }
          result.put(_elvis, definition);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Search body parameters from an 'extension elasticsearch' block: terminate_after, min_score, track_scores, timeout.
   */
  private void extensionBody(final EObject host, final Code.Entries requestBody) {
    Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, ElasticsearchGenerator.VENDOR).entrySet();
    for (final Map.Entry<String, Literal> entry : _entrySet) {
      {
        boolean _contains = ElasticsearchStatements.SEARCH_KEYS.contains(entry.getKey());
        boolean _not = (!_contains);
        if (_not) {
          String _key = entry.getKey();
          String _plus = ("extension parameter " + _key);
          String _plus_1 = (_plus + "; Elasticsearch search body parameters the generator passes are ");
          String _join = IterableExtensions.join(ElasticsearchStatements.SEARCH_KEYS, ", ");
          String _plus_2 = (_plus_1 + _join);
          throw Unsupported.of(_plus_2);
        }
        if (((entry.getValue() instanceof ParamRef) || (entry.getValue() instanceof EnvRef))) {
          String _key_1 = entry.getKey();
          String _plus_3 = ("extension parameter " + _key_1);
          String _plus_4 = (_plus_3 + " with a value that is not a literal");
          throw Unsupported.of(_plus_4);
        }
        requestBody.put(entry.getKey(), Literals.of(entry.getValue()));
      }
    }
  }

  /**
   * Rejects clauses Elasticsearch lacks, with their reason, and clauses not generated yet.
   */
  private void rejectClauses(final EObject statement, final String... features) {
    for (final String featureName : features) {
      boolean _written = ModelUtil.written(statement, featureName);
      if (_written) {
        String _elvis = null;
        String _get = ElasticsearchStatements.CLAUSES.get(featureName);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = featureName;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = ElasticsearchStatements.PENDING_CLAUSES.get(featureName);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + ElasticsearchStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = ElasticsearchStatements.REASONS.get(featureName);
        String _xifexpression = null;
        if ((reason == null)) {
          _xifexpression = clause;
        } else {
          _xifexpression = ((clause + "; ") + reason);
        }
        throw Unsupported.of(_xifexpression);
      }
    }
  }

  /**
   * 'extension elasticsearch' blocks of queries carry search body parameters; extension items of other kinds are rejected.
   */
  private void queryExtensions(final EObject host) {
    final EStructuralFeature slot = host.eClass().getEStructuralFeature("extensions");
    if ((slot == null)) {
      return;
    }
    Object _eGet = host.eGet(slot);
    Iterable<VendorExtension> _filter = Iterables.<VendorExtension>filter(((List<?>) _eGet), VendorExtension.class);
    for (final VendorExtension block : _filter) {
      String _vendor = block.getVendor();
      boolean _equals = Objects.equals(_vendor, ElasticsearchGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on Elasticsearch");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
  }

  /**
   * The query vector: a vector literal or parameter (bit vectors as signed bytes), or for id input the stored vector (runtime).
   */
  private Code.Expr queryVectorOf(final QueryInput queryInput, final ElasticsearchIndex.Field mapped) {
    Code.Expr _switchResult = null;
    EQueryInputMode _kind = queryInput.getKind();
    if (_kind != null) {
      switch (_kind) {
        case VECTOR:
          Code.Expr _xblockexpression = null;
          {
            boolean _sparse = mapped.vector().sparse();
            if (_sparse) {
              String _name = mapped.name();
              String _plus = ("vector input on sparse field " + _name);
              throw Unsupported.of(_plus);
            }
            _xblockexpression = this.convert(queryInput.getValue(), mapped);
          }
          _switchResult = _xblockexpression;
          break;
        case ID:
          _switchResult = new Code.Nil();
          break;
        case SPARSE_VECTOR:
          String _name = mapped.name();
          String _plus = ("sparse_vector input on dense field " + _name);
          throw Unsupported.of(_plus);
        case TEXT:
          throw Unsupported.of(("text input" + ElasticsearchStatements.NO_MODELS));
        case MULTI_VECTOR:
          throw Unsupported.of("multi-vector input; Elasticsearch compares one vector per dense_vector field (rank_vectors needs an enterprise license)");
        default:
          String _literal = queryInput.getKind().getLiteral();
          String _plus_1 = (_literal + " input");
          String _plus_2 = (_plus_1 + ElasticsearchStatements.NO_MODELS);
          throw Unsupported.of(_plus_2);
      }
    } else {
      String _literal = queryInput.getKind().getLiteral();
      String _plus_1 = (_literal + " input");
      String _plus_2 = (_plus_1 + ElasticsearchStatements.NO_MODELS);
      throw Unsupported.of(_plus_2);
    }
    return _switchResult;
  }

  private Code.Expr keywordText(final QueryInput queryInput) {
    Code.Expr _xblockexpression = null;
    {
      if (((queryInput == null) || (!Objects.equals(queryInput.getKind(), EQueryInputMode.TEXT)))) {
        throw Unsupported.of("keyword search without text input");
      }
      boolean _not = (!((queryInput.getValue() instanceof StringLit) || (queryInput.getValue() instanceof ParamRef)));
      if (_not) {
        throw Unsupported.of("keyword input that is not a string");
      }
      _xblockexpression = Literals.of(queryInput.getValue());
    }
    return _xblockexpression;
  }

  private Code.Expr recordsOf(final RecordSource recordSource, final ElasticsearchIndex target) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (recordSource instanceof FileSource) {
      _matched=true;
      _switchResult = ElasticsearchBodies.call("read_records", "location", ElasticsearchBodies.text(((FileSource)recordSource).getLocation()));
    }
    if (!_matched) {
      if (recordSource instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Expr> _function = (RecordLiteral r) -> {
          Code.Entries _recordOf = this.recordOf(r, target);
          return ((Code.Expr) _recordOf);
        };
        _switchResult = Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<RecordLiteral, Code.Expr>map(((InlineRecords)recordSource).getRecords(), _function)));
      }
    }
    if (!_matched) {
      if (recordSource instanceof ParamSource) {
        _matched=true;
        _switchResult = Literals.of(((ParamSource)recordSource).getParam());
      }
    }
    return _switchResult;
  }

  private Code.Entries recordOf(final RecordLiteral recordLiteral, final ElasticsearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = recordLiteral.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final ElasticsearchIndex.Field mapped = target.field(ModelUtil.fieldName(entry.getField()));
          result.put(mapped.name(), this.convert(entry.getValue(), mapped));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A literal in the form Elasticsearch reads for its field: sparse vectors as objects of token (the dimension index as a
   * string) to strictly positive weight, bit vectors as arrays of signed bytes, everything else as written.
   */
  private Code.Expr convert(final Literal literal, final ElasticsearchIndex.Field mapped) {
    Code.Expr _xblockexpression = null;
    {
      ElasticsearchIndex.Vector _vector = mapped.vector();
      boolean _tripleEquals = (_vector == null);
      if (_tripleEquals) {
        return Literals.of(literal);
      }
      if ((mapped.vector().sparse() && (literal instanceof SparseLit))) {
        final Code.Entries weights = Code.entries();
        EList<SparseEntry> _entries = ((SparseLit) literal).getEntries();
        for (final SparseEntry entry : _entries) {
          {
            double _weight = entry.getWeight();
            boolean _lessEqualsThan = (_weight <= 0);
            if (_lessEqualsThan) {
              double _weight_1 = entry.getWeight();
              String _plus = ("a sparse weight of " + Double.valueOf(_weight_1));
              String _plus_1 = (_plus + "; sparse_vector fields take strictly positive weights (SparseVectorFieldMapper)");
              throw Unsupported.of(_plus_1);
            }
            weights.put(String.valueOf(entry.getIndex()), ElasticsearchBodies.num(entry.getWeight()));
          }
        }
        return weights;
      }
      String _element = mapped.vector().element();
      boolean _equals = Objects.equals(_element, "bit");
      if (_equals) {
        if ((literal instanceof ListLit)) {
          final Function1<Literal, Integer> _function = (Literal b) -> {
            int _xifexpression = (int) 0;
            if (((b instanceof NumberLit) && (((NumberLit) b).getValue() != 0))) {
              _xifexpression = 1;
            } else {
              _xifexpression = 0;
            }
            return Integer.valueOf(_xifexpression);
          };
          final List<Integer> bits = IterableExtensions.<Integer>toList(ListExtensions.<Literal, Integer>map(((ListLit) literal).getItems(), _function));
          final List<Code.Expr> octets = CollectionLiterals.<Code.Expr>newArrayList();
          int position = 0;
          while ((position < bits.size())) {
            {
              int octet = 0;
              List<Integer> _subList = bits.subList(position, Math.min(bits.size(), (position + 8)));
              for (final Integer bit : _subList) {
                octet = ((octet * 2) + (bit).intValue());
              }
              int _xifexpression = (int) 0;
              if ((octet > 127)) {
                _xifexpression = (octet - 256);
              } else {
                _xifexpression = octet;
              }
              Code.Num _num = ElasticsearchBodies.num(_xifexpression);
              octets.add(_num);
              position = (position + 8);
            }
          }
          return ElasticsearchBodies.arr(octets);
        }
        if ((literal instanceof ParamRef)) {
          String _name = mapped.name();
          String _plus = ("a parameter for bit vector field " + _name);
          String _plus_1 = (_plus + "; the generator writes bit vectors from literals as signed bytes");
          throw Unsupported.of(_plus_1);
        }
      }
      _xblockexpression = Literals.of(literal);
    }
    return _xblockexpression;
  }

  @XbaseGenerated
  public List<Code.Stmt> body(final Method it) {
    if (it instanceof Alter) {
      return _body((Alter)it);
    } else if (it instanceof AwaitIndex) {
      return _body((AwaitIndex)it);
    } else if (it instanceof BatchQuery) {
      return _body((BatchQuery)it);
    } else if (it instanceof BulkImport) {
      return _body((BulkImport)it);
    } else if (it instanceof Clone) {
      return _body((Clone)it);
    } else if (it instanceof Compact) {
      return _body((Compact)it);
    } else if (it instanceof Count) {
      return _body((Count)it);
    } else if (it instanceof Create) {
      return _body((Create)it);
    } else if (it instanceof Delete) {
      return _body((Delete)it);
    } else if (it instanceof Describe) {
      return _body((Describe)it);
    } else if (it instanceof DistanceMatrix) {
      return _body((DistanceMatrix)it);
    } else if (it instanceof Drop) {
      return _body((Drop)it);
    } else if (it instanceof DropSnapshot) {
      return _body((DropSnapshot)it);
    } else if (it instanceof EmbeddingMigration) {
      return _body((EmbeddingMigration)it);
    } else if (it instanceof Flush) {
      return _body((Flush)it);
    } else if (it instanceof GenerativeSearch) {
      return _body((GenerativeSearch)it);
    } else if (it instanceof GetById) {
      return _body((GetById)it);
    } else if (it instanceof HybridSearch) {
      return _body((HybridSearch)it);
    } else if (it instanceof Insert) {
      return _body((Insert)it);
    } else if (it instanceof KeywordSearch) {
      return _body((KeywordSearch)it);
    } else if (it instanceof ListObjects) {
      return _body((ListObjects)it);
    } else if (it instanceof Load) {
      return _body((Load)it);
    } else if (it instanceof RebuildIndex) {
      return _body((RebuildIndex)it);
    } else if (it instanceof RecommendSearch) {
      return _body((RecommendSearch)it);
    } else if (it instanceof RefreshExternal) {
      return _body((RefreshExternal)it);
    } else if (it instanceof Release) {
      return _body((Release)it);
    } else if (it instanceof Restore) {
      return _body((Restore)it);
    } else if (it instanceof Sample) {
      return _body((Sample)it);
    } else if (it instanceof Scroll) {
      return _body((Scroll)it);
    } else if (it instanceof Snapshot) {
      return _body((Snapshot)it);
    } else if (it instanceof SwapAlias) {
      return _body((SwapAlias)it);
    } else if (it instanceof Truncate) {
      return _body((Truncate)it);
    } else if (it instanceof Update) {
      return _body((Update)it);
    } else if (it instanceof Upsert) {
      return _body((Upsert)it);
    } else if (it instanceof VectorSearch) {
      return _body((VectorSearch)it);
    } else if (it instanceof Transaction) {
      return _body((Transaction)it);
    } else if (it != null) {
      return _body(it);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(it).toString());
    }
  }
}
