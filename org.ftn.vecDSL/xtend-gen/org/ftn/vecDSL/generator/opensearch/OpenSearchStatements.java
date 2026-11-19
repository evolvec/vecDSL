package org.ftn.vecDSL.generator.opensearch;

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
import org.ftn.vecDSL.generator.common.ShapeSpec;
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
 * Maps each vecDSL statement to OpenSearch REST requests in the host-language-neutral code model. Every request body is built
 * here as a JSON value and passed to the runtime, so the three host languages send identical requests: request sends one
 * request, search and fetch shape hits into records with the canonical score, write sends _bulk requests, hybrid fuses the
 * legs of a weighted or relative-score hybrid search (rrf is the server's score-ranker-processor), and recommend computes the
 * query vector of a recommendation from stored examples.
 */
@SuppressWarnings("all")
public class OpenSearchStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; OpenSearchCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for OpenSearch: ";

  /**
   * Suffix of constructs OpenSearch supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no OpenSearch mapping yet";

  public static final String NO_MODELS = "; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors";

  public static final String EXTERNAL = "collections derived from external sources; OpenSearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines";

  public static final String NO_TTL = "ttl; OpenSearch documents do not expire (Index State Management deletes whole indices, not single documents)";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; OpenSearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)";

  public static final String NO_TAGS_REASON = "OpenSearch keeps them in the _meta of the mapping";

  public static final String NO_FILES = "OpenSearch reads analysis files from the config directory of the node, which a client cannot write";

  public static final String LANGUAGES_REFERENCE = "OpenSearch has stop-word lists and stemmers for its language analyzers (english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian, turkish, serbian and others)";

  public static final String NO_TRANSACTIONS = "transactions; OpenSearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)";

  public static final String NO_GENERATION = "generative models run through ML Commons connectors to remote services (the retrieval_augmented_generation response processor), which need a registered model and the credentials of that service";

  public static final String NO_RERANKERS = "the rerank response processor needs a cross-encoder model deployed through ML Commons (type ml_opensearch); its by_field type sorts by a document field and is no reranking model";

  public static final String NO_METRICS = "metrics configuration; OpenSearch metrics come from the stats APIs and the Performance Analyzer plugin, not from a per-program configuration";

  public static final String NO_CLUSTER_LIMITS = "cluster limits; OpenSearch limits shards per node (cluster.max_shards_per_node), not the number of indices";

  public static final String ONE_CLUSTER = "the program talks to one OpenSearch cluster; clusters are deployments, created outside the REST API";

  public static final String NO_SERVER_AUTH = "authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API";

  private static final String FIXED_SHARDS = "the shard count of an existing index is fixed (the split and shrink APIs copy it into a new index)";

  private static final String FIXED_METHOD = "the method of a knn_vector field (engine, parameters and encoder) cannot be updated after the field is created";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why OpenSearch cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "OpenSearch searches the current state of an index (a point in time pins a state for paging, not a past one)"), Pair.<String, String>of("reranker", OpenSearchStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated OpenSearch programs embed no query text" + OpenSearchStatements.NO_MODELS))));

  /**
   * Clauses OpenSearch supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "a knn query on a knn_vector field inside a nested field"), Pair.<String, String>of("prefetches", "a rescore query over the stage candidates")));

  /**
   * Hits a search reads for a cursor page: the k of its knn query, as the Elasticsearch generator's cursor.
   */
  private static final int CURSOR_WINDOW = 1000;

  /**
   * Highlighters mark the terms of a text query.
   */
  private static final String NO_QUERY_TERMS = "highlighting marks the terms of a text query, which ";

  /**
   * Search body parameters that an 'extension opensearch' block on a query may set.
   */
  private static final List<String> SEARCH_KEYS = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("terminate_after", "min_score", "track_scores", "timeout"));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final OpenSearchSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public OpenSearchStatements(final Script script) {
    this.script = script;
    OpenSearchSchema _openSearchSchema = new OpenSearchSchema(this.notes, script);
    this.schema = _openSearchSchema;
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
          _switchResult = "OpenSearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (rank_features fields take their weights from the client or a sparse encoding model)";
          break;
        case "custom":
          _switchResult = (("custom functions; " + OpenSearchStatements.PENDING) + " (an ingest pipeline with a script processor)");
          break;
        default:
          _switchResult = ("model functions run in ingest pipelines of ML Commons (text_embedding, sparse_encoding), which need a deployed model" + OpenSearchStatements.NO_MODELS);
          break;
      }
    } else {
      _switchResult = ("model functions run in ingest pipelines of ML Commons (text_embedding, sparse_encoding), which need a deployed model" + OpenSearchStatements.NO_MODELS);
    }
    return _switchResult;
  }

  /**
   * Index store settings of a storage block: mmap is the mmapfs store; the other kinds are node features.
   */
  public static void storage(final StorageConfig storage, final Code.Entries settings) {
    if ((storage == null)) {
      return;
    }
    String _location = storage.getLocation();
    boolean _tripleNotEquals = (_location != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a storage location; OpenSearch keeps index data under path.data, a node setting");
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
          settings.put("index.store.type", OpenSearchBodies.text(_xifexpression));
          break;
        case "memory":
          throw Unsupported.of("memory storage; OpenSearch stores indices on disk (index.store.type takes fs, niofs, mmapfs and hybridfs)");
        case "object_storage":
          throw Unsupported.of("object storage; searchable snapshots mount indices from a repository only on nodes with the search role, and remote-backed storage is a node setting, while the compose node has the default roles");
        default:
          throw Unsupported.of("embedded storage; OpenSearch runs as a server");
      }
    } else {
      throw Unsupported.of("embedded storage; OpenSearch runs as a server");
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
        String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
        String _plus_3 = (_plus_2 + " (custom routing)");
        throw Unsupported.of(_plus_3);
      }
      int _shards = sharding.getShards();
      boolean _lessThan = (_shards < 1);
      if (_lessThan) {
        throw Unsupported.of("a shard count below 1");
      }
      settings.put("number_of_shards", OpenSearchBodies.num(sharding.getShards()));
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
      settings.put("number_of_replicas", OpenSearchBodies.num(_minus));
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
          settings.put("index.mapping.total_fields.limit", OpenSearchBodies.num(limit.getValue()));
          break;
        default:
          String _literal_1 = limit.getType().getLiteral();
          String _plus = ("collection limits (" + _literal_1);
          String _plus_1 = (_plus + "); OpenSearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit");
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal_1 = limit.getType().getLiteral();
      String _plus = ("collection limits (" + _literal_1);
      String _plus_1 = (_plus + "); OpenSearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit");
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
      String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
      String _plus_3 = (_plus_2 + " (custom routing)");
      throw Unsupported.of(_plus_3);
    }
    boolean _isEmpty = partition.getSubPartitions().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of((("sub-partitions; " + OpenSearchStatements.PENDING) + " (a partition path in the keyword field)"));
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
            throw Unsupported.of(OpenSearchStatements.NO_METRICS);
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
            throw Unsupported.of(OpenSearchStatements.NO_CLUSTER_LIMITS);
          }
          Set<String> _keySet = ModelUtil.extParams(concept, OpenSearchGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
          if ((((Cluster)concept).getCollections().isEmpty() && ((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null)))) {
            throw Unsupported.of("replication and sharding of a cluster without collections; OpenSearch sets shards and replicas per index");
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
          final OpenSearchIndex target = this.schema.index(((Alias)concept).getCollection());
          Code.Call _request = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(OpenSearchBodies.obj("add", OpenSearchBodies.obj("index", OpenSearchBodies.text(target.name()), "alias", OpenSearchBodies.text(OpenSearchBodies.aliasName(((Alias)concept).getName())))))));
          Code.Run _run = new Code.Run(_request);
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of((("a vector index on an existing field; " + OpenSearchStatements.FIXED_METHOD) + " (declare the index with the field)"));
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
          String _functionReason = OpenSearchStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + OpenSearchStatements.NO_FILES));
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
          String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
          String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
          throw Unsupported.of(_plus_3);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + OpenSearchStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + OpenSearchStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + OpenSearchStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of((("quantization of an existing field; " + OpenSearchStatements.FIXED_METHOD) + " (declare the quantization with the field)"));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          final Cluster cluster = EcoreUtil2.<Cluster>getContainerOfType(concept, Cluster.class);
          if ((cluster == null)) {
            throw Unsupported.of(("replication outside a cluster; " + OpenSearchStatements.ONE_CLUSTER));
          }
          List<Code.Stmt> _replicas = this.replicas(cluster, ((ReplicationConfig)concept));
          Iterables.<Code.Stmt>addAll(result, _replicas);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding of existing indices; " + OpenSearchStatements.FIXED_SHARDS));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          final VectorCollection collection = EcoreUtil2.<VectorCollection>getContainerOfType(concept, VectorCollection.class);
          if ((collection == null)) {
            throw Unsupported.of("storage settings outside a collection; OpenSearch sets the store type per index");
          }
          final Code.Entries settings = Code.entries();
          OpenSearchStatements.storage(((StorageConfig)concept), settings);
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
          throw Unsupported.of(OpenSearchStatements.NO_METRICS);
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
          throw Unsupported.of(OpenSearchStatements.NO_CLUSTER_LIMITS);
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
          OpenSearchStatements.collectionLimit(((CollectionLimit)concept), settings);
          String _name = this.schema.index(collection).name();
          String _plus = ("/" + _name);
          String _plus_1 = (_plus + "/_settings");
          Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, settings);
          Code.Run _run = new Code.Run(_request);
          result.add(_run);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (OpenSearchStatements.NOT_MAPPED + _name);
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
      throw Unsupported.of("embedded deployments; OpenSearch runs as a server that the clients reach over HTTP");
    }
    Literal _token = concept.getToken();
    boolean _tripleNotEquals = (_token != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(("bearer tokens; " + OpenSearchStatements.NO_SERVER_AUTH));
    }
    if (((concept.getPassword() instanceof StringLit) || (concept.getApiKey() instanceof StringLit))) {
      throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; the gRPC transport of OpenSearch serves only search and bulk requests, so the generated programs use the REST API");
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals_1 = (_databaseName != null);
    if (_tripleNotEquals_1) {
      throw Unsupported.of("a database in the connection; OpenSearch has no databases (logical databases are index name prefixes)");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, OpenSearchGenerator.VENDOR).keySet();
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
        throw Unsupported.of("replication of a cluster without collections; OpenSearch sets replicas per index");
      }
      final Function1<VectorCollection, String> _function = (VectorCollection c) -> {
        return this.schema.index(c).name();
      };
      final String names = IterableExtensions.join(ListExtensions.<VectorCollection, String>map(cluster.getCollections(), _function), ",");
      int _factor_1 = replication.getFactor();
      int _minus = (_factor_1 - 1);
      Code.Call _request = OpenSearchBodies.request("PUT", (("/" + names) + "/_settings"), null, OpenSearchBodies.obj("index.number_of_replicas", OpenSearchBodies.num(_minus)));
      Code.Run _run = new Code.Run(_request);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Authentication: username_password and api_key are the connection's (internal users of the security plugin, API keys since
   * 3.7); roles are role definitions; audit logging is switched on through the audit API; the other authentication domains
   * are server configuration.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EAuthType _type = auth.getType();
      if (_type != null) {
        switch (_type) {
          case TOKEN:
            throw Unsupported.of(("token authentication; " + OpenSearchStatements.NO_SERVER_AUTH));
          case MTLS:
            throw Unsupported.of(("mtls authentication; " + OpenSearchStatements.NO_SERVER_AUTH));
          case SSO:
            throw Unsupported.of(("sso authentication; " + OpenSearchStatements.NO_SERVER_AUTH));
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
        throw Unsupported.of(("client certificates; " + OpenSearchStatements.NO_SERVER_AUTH));
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; OpenSearch encrypts at rest only through the file system or the cloud provider");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = auth.getRoles();
      for (final RoleBinding binding : _roles) {
        List<Code.Stmt> _role = this.role(binding);
        Iterables.<Code.Stmt>addAll(result, _role);
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        Code.Text _text = OpenSearchBodies.text("replace");
        Code.Text _text_1 = OpenSearchBodies.text("/config/enabled");
        Code.Flag _flag = new Code.Flag(true);
        Code.Call _request = OpenSearchBodies.request("PATCH", "/_plugins/_security/api/audit", null, OpenSearchBodies.arr(OpenSearchBodies.obj("op", _text, "path", _text_1, "value", _flag)));
        Code.Run _run = new Code.Run(_request);
        result.add(_run);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A role with index permissions (action groups) on a collection's index (or on every index); PUT creates or replaces it.
   */
  private List<Code.Stmt> role(final RoleBinding binding) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Code.Text _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = OpenSearchBodies.text("*");
      } else {
        _xifexpression = OpenSearchBodies.text(this.schema.index(binding.getCollection()).name());
      }
      final Code.Text names = _xifexpression;
      final List<Code.Expr> grants = OpenSearchBodies.privileges(binding.getPrivileges());
      Code.Text _xifexpression_1 = null;
      String _description = binding.getDescription();
      boolean _tripleEquals_1 = (_description == null);
      if (_tripleEquals_1) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.text(binding.getDescription());
      }
      List<Code.Expr> _xifexpression_2 = null;
      boolean _isEmpty = grants.isEmpty();
      if (_isEmpty) {
        Code.Text _text = OpenSearchBodies.text("read");
        _xifexpression_2 = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)));
      } else {
        _xifexpression_2 = grants;
      }
      final Code.Entries roleBody = OpenSearchBodies.obj("description", _xifexpression_1, 
        "index_permissions", OpenSearchBodies.arr(OpenSearchBodies.obj("index_patterns", OpenSearchBodies.arr(names), "allowed_actions", OpenSearchBodies.arr(_xifexpression_2))));
      String _roleName = this.roleName(binding.getRoleName());
      String _plus = ("/_plugins/_security/api/roles/" + _roleName);
      Code.Call _request = OpenSearchBodies.request("PUT", _plus, null, roleBody);
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
   * A snapshot management policy (index management plugin) on the fs repository of the compose file, scheduled by cron.
   */
  private List<Code.Stmt> backupSchedule(final BackupSchedule schedule) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String repository = this.repository(schedule.getLocation());
      final OpenSearchIndex target = this.schema.index(schedule.getCollection());
      final Code.Entries cron = OpenSearchBodies.obj("cron", OpenSearchBodies.obj("expression", OpenSearchBodies.text(OpenSearchBodies.schedule(schedule.getInterval())), "timezone", OpenSearchBodies.text("UTC")));
      Code.Entries _obj = OpenSearchBodies.obj("schedule", cron);
      Code.Entries _xifexpression = null;
      boolean _written = ModelUtil.written(schedule, "retentionCount");
      if (_written) {
        _xifexpression = OpenSearchBodies.obj("schedule", cron, "condition", OpenSearchBodies.obj("max_count", OpenSearchBodies.num(schedule.getRetentionCount()), "min_count", OpenSearchBodies.num(1)));
      } else {
        _xifexpression = null;
      }
      Code.Text _text = OpenSearchBodies.text(repository);
      Code.Text _text_1 = OpenSearchBodies.text(target.name());
      Code.Flag _flag = new Code.Flag(false);
      final Code.Entries policy = OpenSearchBodies.obj("creation", _obj, 
        "deletion", _xifexpression, 
        "snapshot_config", OpenSearchBodies.obj("repository", _text, "indices", _text_1, "include_global_state", _flag));
      Code.Stmt _registerRepository = this.registerRepository(repository);
      String _aliasName = OpenSearchBodies.aliasName(schedule.getName());
      String _plus = ("/_plugins/_sm/policies/" + _aliasName);
      Code.Call _request = OpenSearchBodies.request("POST", _plus, null, policy);
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
      _xifexpression = OpenSearchBodies.aliasName(location.replaceAll("[^A-Za-z0-9]+", "_"));
    }
    return _xifexpression;
  }

  private Code.Stmt registerRepository(final String repository) {
    Code.Call _request = OpenSearchBodies.request("PUT", ("/_snapshot/" + repository), null, OpenSearchBodies.obj("type", OpenSearchBodies.text("fs"), "settings", OpenSearchBodies.obj("location", OpenSearchBodies.text(repository))));
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
    throw Unsupported.of((what + " outside a collection; OpenSearch adds it to an index"));
  }

  /**
   * A tenant, namespace or partition: a filtered alias on the slice's keyword field, which the mapping gains when missing.
   */
  private List<Code.Stmt> slice(final VectorCollection collection, final String kind, final String value, final EObject concept) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((concept instanceof Partition)) {
        OpenSearchStatements.checkPartition(((Partition) concept));
      }
      OpenSearchIndex target = this.schema.index(collection);
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
          _xifexpression = OpenSearchBodies.TENANT_FIELD;
        } else {
          _xifexpression = OpenSearchBodies.NAMESPACE_FIELD;
        }
        sliceField = _xifexpression;
        String _name = target.name();
        String _plus = ("/" + _name);
        String _plus_1 = (_plus + "/_mapping");
        Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("properties", OpenSearchBodies.obj(sliceField, OpenSearchBodies.obj("type", OpenSearchBodies.text("keyword")))));
        Code.Run _run = new Code.Run(_request);
        result.add(_run);
        OpenSearchIndex _xifexpression_1 = null;
        boolean _equals_2 = Objects.equals(kind, "tenant");
        if (_equals_2) {
          String _name_1 = target.name();
          String _key = target.key();
          List<OpenSearchIndex.Field> _fields = target.fields();
          String _namespace = target.namespace();
          String _partition = target.partition();
          Map<String, String> _renames = target.renames();
          String _database = target.database();
          _xifexpression_1 = new OpenSearchIndex(_name_1, _key, _fields, sliceField, _namespace, _partition, _renames, _database);
        } else {
          String _name_2 = target.name();
          String _key_1 = target.key();
          List<OpenSearchIndex.Field> _fields_1 = target.fields();
          String _tenant = target.tenant();
          String _partition_1 = target.partition();
          Map<String, String> _renames_1 = target.renames();
          String _database_1 = target.database();
          _xifexpression_1 = new OpenSearchIndex(_name_2, _key_1, _fields_1, _tenant, sliceField, _partition_1, _renames_1, _database_1);
        }
        target = _xifexpression_1;
        this.schema.evolve(collection, target);
      }
      final Code.Entries alias = OpenSearchBodies.obj("index", OpenSearchBodies.text(target.name()), "alias", OpenSearchBodies.text(OpenSearchBodies.sliceAlias(target.name(), kind, value)), "filter", OpenSearchSchema.term(sliceField, OpenSearchBodies.text(value)));
      Code.Call _request_1 = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(OpenSearchBodies.obj("add", alias))));
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
        throw Unsupported.of("an analyzer outside a collection; OpenSearch defines analyzers in the settings of an index");
      }
      final Code.Entries settings = OpenSearchBodies.obj("analysis", this.schema.analysis(Collections.<Analyzer>unmodifiableList(CollectionLiterals.<Analyzer>newArrayList(analyzer))));
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
    Code.Call _request = OpenSearchBodies.request("POST", (("/" + name) + "/_close"), null, null);
    Code.Run _run = new Code.Run(_request);
    Code.Call _request_1 = OpenSearchBodies.request("PUT", (("/" + name) + "/_settings"), null, settings);
    Code.Run _run_1 = new Code.Run(_request_1);
    Code.Call _request_2 = OpenSearchBodies.request("POST", (("/" + name) + "/_open"), null, null);
    Code.Run _run_2 = new Code.Run(_request_2);
    Code.Call _call = OpenSearchBodies.call("wait_health", "index", OpenSearchBodies.text(name), "status", OpenSearchBodies.text("yellow"), "timeout_seconds", OpenSearchBodies.num(60));
    Code.Run _run_3 = new Code.Run(_call);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _run_2, _run_3));
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
      final OpenSearchIndex target = this.schema.index(collection);
      this.schema.checkPayloadIndex(target, payloadIndex);
      if ((owner == null)) {
        throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields");
      }
      final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(owner));
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_mapping");
      Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("properties", OpenSearchBodies.obj(mapped.name(), mapped.mapping())));
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
      final OpenSearchIndex target = this.schema.index(collection);
      final OpenSearchIndex.Field mapped = this.schema.field(collection, added);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_mapping");
      Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("properties", OpenSearchBodies.obj(mapped.name(), mapped.mapping())));
      Code.Run _run = new Code.Run(_request);
      result.add(_run);
      Code.Expr _defaultValue = mapped.defaultValue();
      boolean _tripleNotEquals = (_defaultValue != null);
      if (_tripleNotEquals) {
        Code.Call _byQuery = this.byQuery(target, "_update_by_query", OpenSearchBodies.obj("bool", OpenSearchBodies.obj("must_not", OpenSearchBodies.arr(OpenSearchBodies.obj("exists", OpenSearchBodies.obj("field", OpenSearchBodies.text(mapped.name())))))), 
          OpenSearchBodies.script(OpenSearchBodies.DEFAULT_SCRIPT, OpenSearchBodies.obj("field", OpenSearchBodies.text(mapped.name()), "value", mapped.defaultValue())), "updated", "update_count", null);
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
  private Code.Call byQuery(final OpenSearchIndex target, final String endpoint, final Code.Entries matching, final Code.Entries painless, final String counter, final String wrap, final Integer maxDocs) {
    Code.Call _xblockexpression = null;
    {
      Code.Num _xifexpression = null;
      if ((maxDocs == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.num((maxDocs).intValue());
      }
      final Code.Entries requestBody = OpenSearchBodies.obj("query", matching, "script", painless, "max_docs", _xifexpression);
      Code.Text _text = OpenSearchBodies.text("POST");
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/");
      String _plus_2 = (_plus_1 + endpoint);
      Code.Text _text_1 = OpenSearchBodies.text(_plus_2);
      Code.Entries _obj = OpenSearchBodies.obj("refresh", OpenSearchBodies.text("true"), "conflicts", OpenSearchBodies.text("proceed"));
      Code.Items _arr = OpenSearchBodies.arr(OpenSearchBodies.text(counter));
      Code.Text _xifexpression_1 = null;
      if ((wrap == null)) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.text(wrap);
      }
      _xblockexpression = OpenSearchBodies.call("request", "method", _text, "path", _text_1, "params", _obj, 
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
        final OpenSearchIndex aliasTarget = this.schema.index(alias.getCollection());
        Code.Call _request = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(
          OpenSearchBodies.obj("remove", OpenSearchBodies.obj("index", OpenSearchBodies.text(aliasTarget.name()), "alias", OpenSearchBodies.text(OpenSearchBodies.aliasName(alias.getName())))), 
          OpenSearchBodies.obj("add", OpenSearchBodies.obj("index", OpenSearchBodies.text(aliasTarget.name()), "alias", OpenSearchBodies.text(OpenSearchBodies.aliasName(it.getNewName())))))));
        Code.Run _run = new Code.Run(_request);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; OpenSearch alters indices and aliases");
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
        Code.Call _request_1 = OpenSearchBodies.request("GET", _plus_3, null, null);
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
      final OpenSearchIndex target = this.schema.index(collection);
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
            final OpenSearchIndex.Field old = target.field(ModelUtil.fieldName(((RenameFieldOp)operation).getField()));
            final String newField = OpenSearchBodies.field(((RenameFieldOp)operation).getNewName());
            final OpenSearchIndex renamedIndex = target.renamed(ModelUtil.fieldName(((RenameFieldOp)operation).getField()), newField);
            this.schema.evolve(collection, renamedIndex);
            String _name = target.name();
            String _plus = ("/" + _name);
            String _plus_1 = (_plus + "/_mapping");
            Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("properties", OpenSearchBodies.obj(newField, old.mapping())));
            Code.Run _run = new Code.Run(_request);
            Code.Call _byQuery = this.byQuery(target, "_update_by_query", OpenSearchBodies.obj("exists", OpenSearchBodies.obj("field", OpenSearchBodies.text(old.name()))), 
              OpenSearchBodies.script(OpenSearchBodies.RENAME_SCRIPT, OpenSearchBodies.obj("from", OpenSearchBodies.text(old.name()), "to", OpenSearchBodies.text(newField))), "updated", "update_count", null);
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
          Code.Call _request = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(OpenSearchBodies.obj("add", OpenSearchBodies.obj("index", OpenSearchBodies.text(target.name()), "alias", OpenSearchBodies.text(OpenSearchBodies.aliasName(((AddAliasOp)operation).getNewAlias().getName())))))));
          Code.Run _run = new Code.Run(_request);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = OpenSearchStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = OpenSearchStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (OpenSearchStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A field leaves the documents (update_by_query removes it from _source); the mapping keeps the field, since OpenSearch
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
      final OpenSearchIndex target = this.schema.index(collection);
      final OpenSearchIndex.Field old = target.field(ModelUtil.fieldName(dropped));
      this.schema.evolve(collection, target.without(ModelUtil.fieldName(dropped)));
      Code.Call _byQuery = this.byQuery(target, "_update_by_query", OpenSearchBodies.obj("exists", OpenSearchBodies.obj("field", OpenSearchBodies.text(old.exact()))), 
        OpenSearchBodies.script(OpenSearchBodies.DROP_SCRIPT, OpenSearchBodies.obj("field", OpenSearchBodies.text(old.name()))), "updated", "update_count", null);
      Code.Run _run = new Code.Run(_byQuery);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description and tags live in the _meta of the mapping, dynamic is the mapping's dynamic setting.
   */
  private List<Code.Stmt> setProperty(final OpenSearchIndex target, final SetPropertyOp operation) {
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
            Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("_meta", OpenSearchBodies.obj("description", Literals.of(operation.getValue()))));
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
            Code.Call _request_1 = OpenSearchBodies.request("PUT", _plus_3, null, OpenSearchBodies.obj("_meta", OpenSearchBodies.obj("tags", Literals.of(operation.getValue()))));
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
            Code.Call _request_2 = OpenSearchBodies.request("PUT", _plus_5, null, 
              OpenSearchBodies.obj("dynamic", OpenSearchBodies.text(_xifexpression)));
            Code.Run _run_2 = new Code.Run(_request_2);
            result.add(_run_2);
            break;
          case CONSISTENCY:
            this.notes.add("consistency on SetPropertyOp, which every refreshed write meets");
            break;
          case TTL:
            throw Unsupported.of(OpenSearchStatements.NO_TTL);
          case DELETION_PROTECTION:
            throw Unsupported.of(OpenSearchStatements.NO_DELETION_PROTECTION);
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
      final OpenSearchIndex target = this.schema.index(collection);
      final OpenSearchIndex renamedIndex = target.named(OpenSearchBodies.indexName(target.database(), newName));
      final Code.Entries aliases = Code.entries();
      EList<Alias> _aliases = collection.getAliases();
      for (final Alias alias : _aliases) {
        aliases.put(OpenSearchBodies.aliasName(alias.getName()), Code.entries());
      }
      this.schema.evolve(collection, renamedIndex);
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_settings");
      Code.Flag _flag = new Code.Flag(true);
      Code.Call _request = OpenSearchBodies.request("PUT", _plus_1, null, OpenSearchBodies.obj("index.blocks.write", _flag));
      Code.Run _run = new Code.Run(_request);
      String _name_1 = target.name();
      String _plus_2 = ("/" + _name_1);
      String _plus_3 = (_plus_2 + "/_clone/");
      String _name_2 = renamedIndex.name();
      String _plus_4 = (_plus_3 + _name_2);
      Code.Call _request_1 = OpenSearchBodies.request("POST", _plus_4, null, OpenSearchBodies.obj("aliases", aliases));
      Code.Run _run_1 = new Code.Run(_request_1);
      String _name_3 = target.name();
      String _plus_5 = ("/" + _name_3);
      Code.Call _request_2 = OpenSearchBodies.request("DELETE", _plus_5, null, null);
      Code.Run _run_2 = new Code.Run(_request_2);
      String _name_4 = renamedIndex.name();
      String _plus_6 = ("/" + _name_4);
      String _plus_7 = (_plus_6 + "/_settings");
      Code.Nil _nil = new Code.Nil();
      Code.Call _request_3 = OpenSearchBodies.request("PUT", _plus_7, null, OpenSearchBodies.obj("index.blocks.write", _nil));
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
        _xifexpression = OpenSearchBodies.arr(OpenSearchBodies.num(404));
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
          final OpenSearchIndex dropped = this.schema.index(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          String _name = dropped.name();
          String _plus = ("/" + _name);
          Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("DELETE"), "path", OpenSearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          Code.Call _call = OpenSearchBodies.call("drop_matching", "pattern", OpenSearchBodies.text(OpenSearchBodies.indexName(((Database)concept).getName(), "x").replaceFirst("x$", "*")));
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
          String _aliasName = OpenSearchBodies.aliasName(((Alias)concept).getName());
          String _plus = ("/*,-.*/_alias/" + _aliasName);
          Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("DELETE"), "path", OpenSearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a vector index; " + OpenSearchStatements.FIXED_METHOD));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of("dropping a payload index; whether a field is indexed is part of its mapping, which OpenSearch cannot change");
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = OpenSearchStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of(("dropping quantization; " + OpenSearchStatements.FIXED_METHOD));
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not an OpenSearch object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of(("dropping a deployment; " + OpenSearchStatements.ONE_CLUSTER));
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + OpenSearchStatements.ONE_CLUSTER));
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
          String _plus_2 = (_plus_1 + OpenSearchStatements.PENDING);
          String _plus_3 = (_plus_2 + " (an ingest pipeline with fail processors)");
          throw Unsupported.of(_plus_3);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + OpenSearchStatements.NO_FILES));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + OpenSearchStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + OpenSearchStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + OpenSearchStatements.NO_RERANKERS));
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
          throw Unsupported.of(("dropping sharding; " + OpenSearchStatements.FIXED_SHARDS));
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
          throw Unsupported.of("dropping authentication; the security plugin is configured on the server (plugins.security.disabled)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          String _roleName = this.roleName(((RoleBinding)concept).getRoleName());
          String _plus = ("/_plugins/_security/api/roles/" + _roleName);
          Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("DELETE"), "path", OpenSearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(OpenSearchStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          String _aliasName = OpenSearchBodies.aliasName(((BackupSchedule)concept).getName());
          String _plus = ("/_plugins/_sm/policies/" + _aliasName);
          Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("DELETE"), "path", OpenSearchBodies.text(_plus), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(OpenSearchStatements.NO_CLUSTER_LIMITS);
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
              String _plus_1 = (_plus + "); OpenSearch limits the fields of a mapping only");
              throw Unsupported.of(_plus_1);
            }
            String _name = this.schema.index(collection).name();
            String _plus_2 = ("/" + _name);
            String _plus_3 = (_plus_2 + "/_settings");
            Code.Nil _nil = new Code.Nil();
            Code.Call _request = OpenSearchBodies.request("PUT", _plus_3, null, OpenSearchBodies.obj("index.mapping.total_fields.limit", _nil));
            Code.Run _run = new Code.Run(_request);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((OpenSearchStatements.NOT_MAPPED + "dropping a ") + _name);
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
      final OpenSearchIndex target = this.schema.index(collection);
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
      Code.Text _text = OpenSearchBodies.text(target.name());
      Code.Text _text_1 = OpenSearchBodies.text(OpenSearchBodies.sliceAlias(target.name(), kind, value));
      Code.Flag _flag = new Code.Flag(false);
      Code.Call _request = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(OpenSearchBodies.obj("remove", OpenSearchBodies.obj("index", _text, "alias", _text_1, 
        "must_exist", _flag)))));
      Code.Run _run = new Code.Run(_request);
      Code.Call _byQuery = this.byQuery(target, "_delete_by_query", OpenSearchSchema.term(fieldName, OpenSearchBodies.text(value)), null, "deleted", "delete_count", null);
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
      final String aliasValue = OpenSearchBodies.aliasName(it.getAlias().getName());
      Code.Text _text = OpenSearchBodies.text(this.schema.index(it.getOldCollection()).name());
      Code.Text _text_1 = OpenSearchBodies.text(aliasValue);
      Code.Flag _flag = new Code.Flag(false);
      Code.Call _request = OpenSearchBodies.request("POST", "/_aliases", null, OpenSearchBodies.obj("actions", OpenSearchBodies.arr(
        OpenSearchBodies.obj("remove", OpenSearchBodies.obj("index", _text, "alias", _text_1, "must_exist", _flag)), 
        OpenSearchBodies.obj("add", OpenSearchBodies.obj("index", OpenSearchBodies.text(this.schema.index(it.getNewCollection()).name()), "alias", OpenSearchBodies.text(aliasValue))))));
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
        throw Unsupported.of(OpenSearchStatements.NO_TTL);
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
        throw Unsupported.of(OpenSearchStatements.NO_TTL);
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
      final OpenSearchIndex target = this.schema.index(collection);
      final Map<String, String> mode = OpenSearchBodies.writeMode(policy);
      Code.Text _text = OpenSearchBodies.text(target.name());
      Code.Expr _recordsOf = this.recordsOf(recordSource, target);
      Code.Text _text_1 = OpenSearchBodies.text(target.key());
      Code.Text _text_2 = OpenSearchBodies.text(mode.get("op"));
      Code.Text _text_3 = OpenSearchBodies.text(mode.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(statement, collection, target);
      Code.Entries _defaultsOf = this.defaultsOf(target);
      Code.Num _xifexpression = null;
      if ((batch == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.num((batch).intValue());
      }
      Code.Flag _xifexpression_1 = null;
      boolean _containsKey = mode.containsKey("ignore");
      if (_containsKey) {
        _xifexpression_1 = new Code.Flag(true);
      } else {
        _xifexpression_1 = null;
      }
      _xblockexpression = OpenSearchBodies.call("write", "index", _text, "records", _recordsOf, "key", _text_1, "op", _text_2, 
        "counter", _text_3, "extra", _sliceFields, "defaults", _defaultsOf, 
        "batch_size", _xifexpression, "ignore_conflicts", _xifexpression_1, 
        "vectors", this.vectorsOf(target));
    }
    return _xblockexpression;
  }

  /**
   * The float vector fields a write sends with a decimal point (OpenSearchIndex.floatVectorFields); null without any.
   */
  private Code.Items vectorsOf(final OpenSearchIndex target) {
    Code.Items _xblockexpression = null;
    {
      final List<String> names = target.floatVectorFields();
      Code.Items _xifexpression = null;
      boolean _isEmpty = names.isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        final Function1<String, Code.Text> _function = (String name) -> {
          return OpenSearchBodies.text(name);
        };
        _xifexpression = OpenSearchBodies.arr(ListExtensions.<String, Code.Text>map(names, _function));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private Code.Entries defaultsOf(final OpenSearchIndex target) {
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
  private Code.Entries sliceFields(final EObject statement, final VectorCollection collection, final OpenSearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.put(target.tenant(), OpenSearchBodies.text(tenant.getName()));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        result.put(target.namespace(), OpenSearchBodies.text(((String) namespace)));
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
          _elvis = OpenSearchBodies.DEFAULT_PARTITION;
        }
        result.put(target.partition(), OpenSearchBodies.text(_elvis));
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if ((((((((((it.getFilter() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty())) || ModelUtil.written(it, "ifVersion")) || (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) || (it.getTenant() != null)) || (it.getNamespace() != null)) || (it.getPartition() != null))) {
          throw Unsupported.of("update from records with a filter, a slice, field clauses, if-version or mode replace; bulk update operations address documents by id, and each record carries the values it merges");
        }
        final Function1<Literal, Code.Expr> _function = (Literal i) -> {
          return Literals.of(i);
        };
        Code.Call _call = OpenSearchBodies.call("write", "index", OpenSearchBodies.text(target.name()), "records", this.recordsOf(it.getSource(), target), "key", OpenSearchBodies.text(target.key()), "op", OpenSearchBodies.text("patch"), 
          "counter", OpenSearchBodies.text("update_count"), "only", Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function))), "vectors", this.vectorsOf(target));
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
            final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(entry.getField()));
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
          Code.Text _text = OpenSearchBodies.text(target.field(ModelUtil.fieldName(removedField)).name());
          removed.add(_text);
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        List<OpenSearchIndex.Field> _fields = target.fields();
        for (final OpenSearchIndex.Field mapped : _fields) {
          if (((((!Objects.equals(mapped.name(), target.key())) && (mapped.vector() == null)) && (!given.contains(mapped.name()))) && (!removed.contains(OpenSearchBodies.text(mapped.name()))))) {
            Code.Text _text = OpenSearchBodies.text(mapped.name());
            removed.add(_text);
          }
        }
      }
      if ((((assignments.entries().isEmpty() && appended.entries().isEmpty()) && pulled.entries().isEmpty()) && removed.isEmpty())) {
        throw Unsupported.of("an update without \'set\' or \'remove-fields\'");
      }
      final Code.Entries painless = OpenSearchBodies.script(OpenSearchBodies.UPDATE_SCRIPT, OpenSearchBodies.obj("set", assignments, "remove", OpenSearchBodies.arr(removed), "append", appended, "pull", pulled));
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        if ((((((it.getIds().size() != 1) || (it.getFilter() != null)) || (it.getTenant() != null)) || (it.getNamespace() != null)) || (it.getPartition() != null))) {
          throw Unsupported.of("if-version on other than one id; optimistic concurrency control (if_seq_no and if_primary_term) guards one document");
        }
        Code.Call _call_1 = OpenSearchBodies.call("update_if_version", "index", OpenSearchBodies.text(target.name()), "value", Literals.of(IterableExtensions.<Literal>head(it.getIds())), "seq_no", OpenSearchBodies.num(it.getIfVersion()), "body", OpenSearchBodies.obj("script", painless));
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
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
        throw Unsupported.of("continuous import; pull-based ingestion (experimental since 3.0) reads Kafka or Kinesis through the ingestion-kafka and ingestion-kinesis plugins, which the image does not include, into an index created with an ingestion_source");
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
        throw Unsupported.of((("import from " + scheme) + "; OpenSearch indexes documents that a client sends, and reads object stores only as snapshot repositories or through the experimental pull-based ingestion of an index created for it"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + OpenSearchStatements.PENDING) + " (CSV records in the runtime)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the _bulk API takes JSON documents");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async");
      Set<String> _keySet = ModelUtil.extParams(it, OpenSearchGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final Map<String, String> writeModeOf = OpenSearchBodies.writeMode(_xifexpression_1);
      Code.Text _text = OpenSearchBodies.text(it.getLocation());
      Code.Text _text_1 = OpenSearchBodies.text(target.name());
      Code.Text _text_2 = OpenSearchBodies.text(target.key());
      Code.Text _text_3 = OpenSearchBodies.text(writeModeOf.get("op"));
      Code.Text _text_4 = OpenSearchBodies.text(writeModeOf.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(it, it.getCollection(), target);
      Code.Entries _defaultsOf = this.defaultsOf(target);
      Code.Num _xifexpression_2 = null;
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        _xifexpression_2 = OpenSearchBodies.num(it.getBatchSize());
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
      Code.Call _call = OpenSearchBodies.call("import_file", "location", _text, "index", _text_1, "key", _text_2, "op", _text_3, 
        "counter", _text_4, "extra", _sliceFields, "defaults", _defaultsOf, 
        "batch_size", _xifexpression_2, "ignore_conflicts", _xifexpression_3, 
        "vectors", this.vectorsOf(target));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of(OpenSearchStatements.NO_TRANSACTIONS);
  }

  /**
   * Every write carries refresh=true, so 'wait' needs no call; write consistency is the server's (wait_for_active_shards 1).
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, OpenSearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  /**
   * Load opens the index, waits until it is usable and loads its vector graphs into native memory (k-NN warmup API).
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("loading partitions; OpenSearch opens and closes whole indices");
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_open");
      Code.Call _request = OpenSearchBodies.request("POST", _plus_1, null, null);
      Code.Run _run = new Code.Run(_request);
      Code.Call _call = OpenSearchBodies.call("wait_health", "index", OpenSearchBodies.text(target.name()), "status", OpenSearchBodies.text("yellow"), "timeout_seconds", OpenSearchBodies.num(60));
      Code.Let _let = new Code.Let("health", _call);
      String _name_1 = target.name();
      String _plus_2 = ("/_plugins/_knn/warmup/" + _name_1);
      Code.Call _request_1 = OpenSearchBodies.request("GET", _plus_2, null, null);
      Code.Run _run_1 = new Code.Run(_request_1);
      Code.Ref _ref = new Code.Ref("health");
      Code.Return _return = new Code.Return(_ref);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _let, _run_1, _return));
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
        throw Unsupported.of("releasing partitions; OpenSearch opens and closes whole indices");
      }
      String _name = this.schema.index(it.getCollection()).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_close");
      Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("POST"), "path", OpenSearchBodies.text(_plus_1), "select", OpenSearchBodies.arr(OpenSearchBodies.text("acknowledged")));
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
      Code.Call _request = OpenSearchBodies.request("POST", _plus_1, null, null);
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
        _xifexpression = OpenSearchBodies.num(it.getTargetSegments());
      } else {
        _xifexpression = null;
      }
      Code.Text _xifexpression_1 = null;
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        _xifexpression_1 = OpenSearchBodies.text("false");
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries forceMergeParams = OpenSearchBodies.obj("max_num_segments", _xifexpression, 
        "wait_for_completion", _xifexpression_1);
      String _name = this.schema.index(it.getCollection()).name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_forcemerge");
      Code.Text _text = OpenSearchBodies.text("true");
      Code.Text _xifexpression_2 = null;
      boolean _isAsync_1 = it.isAsync();
      if (_isAsync_1) {
        _xifexpression_2 = OpenSearchBodies.text("false");
      } else {
        _xifexpression_2 = null;
      }
      Code.Call _request = OpenSearchBodies.request("POST", _plus_1, OpenSearchBodies.obj("only_expunge_deletes", _text, "wait_for_completion", _xifexpression_2), null);
      Code.Run _run = new Code.Run(_request);
      String _name_1 = this.schema.index(it.getCollection()).name();
      String _plus_2 = ("/" + _name_1);
      String _plus_3 = (_plus_2 + "/_forcemerge");
      Code.Call _request_1 = OpenSearchBodies.request("POST", _plus_3, forceMergeParams, null);
      Code.Run _run_1 = new Code.Run(_request_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      final Code.Call info = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("GET"), "path", OpenSearchBodies.text(_plus), "select", OpenSearchBodies.arr(OpenSearchBodies.text(target.name())));
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
        Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("GET"), "path", OpenSearchBodies.text(_plus_2), "select", OpenSearchBodies.arr(OpenSearchBodies.text("_all"), OpenSearchBodies.text("primaries")));
        parts.add(_call);
      }
      boolean _isIncludeIndexProgress = it.isIncludeIndexProgress();
      if (_isIncludeIndexProgress) {
        String _name_2 = target.name();
        String _plus_3 = ("/_cluster/health/" + _name_2);
        Code.Call _call_1 = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("GET"), "path", OpenSearchBodies.text(_plus_3), "select", OpenSearchBodies.arr(OpenSearchBodies.text("status")));
        parts.add(_call_1);
      }
      Code.Items _items = Code.items(parts);
      Code.Return _return_1 = new Code.Return(_items);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + OpenSearchStatements.NO_MODELS));
  }

  /**
   * A snapshot of the index into the fs repository (path.repo); OpenSearch snapshots are incremental by design.
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
        _xifexpression = OpenSearchBodies.text("*");
      } else {
        _xifexpression = OpenSearchBodies.text(this.schema.index(it.getCollection()).name());
      }
      final Code.Text indices = _xifexpression;
      Code.Flag _flag = new Code.Flag(false);
      Code.Entries _xifexpression_1 = null;
      String _description = it.getDescription();
      boolean _tripleEquals = (_description == null);
      if (_tripleEquals) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.obj("description", OpenSearchBodies.text(it.getDescription()));
      }
      final Code.Entries snapshotBody = OpenSearchBodies.obj("indices", indices, "include_global_state", _flag, 
        "metadata", _xifexpression_1);
      Code.Stmt _registerRepository = this.registerRepository(repo);
      String _snapshotOf = this.snapshotOf(it);
      String _plus_2 = ((("/_snapshot/" + repo) + "/") + _snapshotOf);
      Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("PUT"), "path", OpenSearchBodies.text(_plus_2), 
        "params", OpenSearchBodies.obj("wait_for_completion", OpenSearchBodies.text("true")), "body", snapshotBody, "select", OpenSearchBodies.arr(OpenSearchBodies.text("snapshot"), OpenSearchBodies.text("state")));
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
    return OpenSearchBodies.aliasName(_elvis);
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus_2 = ("/" + _name);
      String _plus_3 = (_plus_2 + "/_close");
      Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("POST"), "path", OpenSearchBodies.text(_plus_3), "ignore", OpenSearchBodies.arr(OpenSearchBodies.num(404)));
      Code.Run _run = new Code.Run(_call);
      Code.Text _text = OpenSearchBodies.text("POST");
      String _aliasName = OpenSearchBodies.aliasName(it.getSnapshotName());
      String _plus_4 = ((("/_snapshot/" + repo) + "/") + _aliasName);
      String _plus_5 = (_plus_4 + "/_restore");
      Code.Text _text_1 = OpenSearchBodies.text(_plus_5);
      String _xifexpression = null;
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        _xifexpression = "false";
      } else {
        _xifexpression = "true";
      }
      Code.Entries _obj = OpenSearchBodies.obj("wait_for_completion", OpenSearchBodies.text(_xifexpression));
      Code.Text _text_2 = OpenSearchBodies.text(target.name());
      Code.Flag _flag = new Code.Flag(false);
      Code.Entries _obj_1 = OpenSearchBodies.obj("indices", _text_2, "include_global_state", _flag);
      Code.Items _xifexpression_1 = null;
      boolean _isAsync_1 = it.isAsync();
      if (_isAsync_1) {
        _xifexpression_1 = OpenSearchBodies.arr(OpenSearchBodies.text("accepted"));
      } else {
        _xifexpression_1 = OpenSearchBodies.arr(OpenSearchBodies.text("snapshot"), OpenSearchBodies.text("indices"));
      }
      Code.Call _call_1 = OpenSearchBodies.call("request", "method", _text, "path", _text_1, 
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
    String _aliasName = OpenSearchBodies.aliasName(it.getSnapshotName());
    String _plus_2 = (_plus_1 + _aliasName);
    Code.Call _request = OpenSearchBodies.request("DELETE", _plus_2, null, null);
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
        throw Unsupported.of(("cloning a deployment; " + OpenSearchStatements.ONE_CLUSTER));
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = OpenSearchStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      this.operationExtensions(it);
      this.schema.note(it, "mode");
      final String original = this.schema.index(it.getSourceCollection()).name();
      final String copy = OpenSearchBodies.indexName(this.schema.index(it.getSourceCollection()).database(), it.getTargetName());
      Code.Flag _flag = new Code.Flag(true);
      Code.Call _request = OpenSearchBodies.request("PUT", (("/" + original) + "/_settings"), null, OpenSearchBodies.obj("index.blocks.write", _flag));
      Code.Run _run = new Code.Run(_request);
      Code.Call _request_1 = OpenSearchBodies.request("POST", ((("/" + original) + "/_clone/") + copy), null, null);
      Code.Run _run_1 = new Code.Run(_request_1);
      Code.Nil _nil = new Code.Nil();
      Code.Call _request_2 = OpenSearchBodies.request("PUT", (("/" + original) + "/_settings"), null, OpenSearchBodies.obj("index.blocks.write", _nil));
      Code.Run _run_2 = new Code.Run(_request_2);
      Code.Nil _nil_1 = new Code.Nil();
      Code.Call _request_3 = OpenSearchBodies.request("PUT", (("/" + copy) + "/_settings"), null, OpenSearchBodies.obj("index.blocks.write", _nil_1));
      Code.Run _run_3 = new Code.Run(_request_3);
      Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("POST"), "path", OpenSearchBodies.text((("/" + copy) + "/_count")), "select", OpenSearchBodies.arr(OpenSearchBodies.text("count")), "wrap", OpenSearchBodies.text("copied"));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _run_2, _run_3, _return));
    }
    return _xblockexpression;
  }

  /**
   * OpenSearch rebuilds HNSW graphs when it merges segments: a force merge into one segment rebuilds the index's graph.
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
      Code.Call _request = OpenSearchBodies.request("POST", _plus_1, OpenSearchBodies.obj("only_expunge_deletes", OpenSearchBodies.text("true")), null);
      Code.Run _run = new Code.Run(_request);
      String _name_1 = this.schema.index(collection).name();
      String _plus_2 = ("/" + _name_1);
      String _plus_3 = (_plus_2 + "/_forcemerge");
      Code.Call _request_1 = OpenSearchBodies.request("POST", _plus_3, OpenSearchBodies.obj("max_num_segments", OpenSearchBodies.num(1)), null);
      Code.Run _run_1 = new Code.Run(_request_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(OpenSearchStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        String _namespace_1 = target.namespace();
        boolean _tripleEquals = (_namespace_1 == null);
        if (_tripleEquals) {
          throw Unsupported.of("a namespace of a collection without namespaces");
        }
        Code.Call _byQuery = this.byQuery(target, "_delete_by_query", OpenSearchSchema.term(target.namespace(), OpenSearchBodies.text(it.getNamespace())), null, "deleted", "delete_count", null);
        Code.Return _return = new Code.Return(_byQuery);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      Code.Call _byQuery_1 = this.byQuery(target, "_delete_by_query", OpenSearchBodies.obj("match_all", Code.entries()), null, "deleted", "delete_count", null);
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
        _xifexpression = OpenSearchBodies.num(it.getListLimit());
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
        _xifexpression_1 = OpenSearchBodies.text(it.getPrefix());
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
                final String pattern = OpenSearchBodies.indexName(database, "x").replaceFirst("x$", "");
                _xblockexpression_1 = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("indices"), "path", OpenSearchBodies.text((("/_cat/indices/" + pattern) + "*")), "strip", OpenSearchBodies.text(pattern), "prefix", prefixValue, "limit", limit);
              }
              _xifexpression_2 = _xblockexpression_1;
            } else {
              Code.Call _xblockexpression_2 = null;
              {
                if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                  String _name = owner.eClass().getName();
                  String _plus = ("listing collections in a " + _name);
                  String _plus_1 = (_plus + "; OpenSearch lists the indices of a cluster");
                  throw Unsupported.of(_plus_1);
                }
                _xblockexpression_2 = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("indices"), "path", OpenSearchBodies.text("/_cat/indices"), "prefix", prefixValue, "limit", limit);
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
              _xblockexpression_3 = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("databases"), "path", OpenSearchBodies.text("/_cat/indices"), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_3;
            break;
          case INDEXES:
            String _name = this.scopeIndex(owner, "indexes").name();
            String _plus = ("/" + _name);
            String _plus_1 = (_plus + "/_mapping");
            _switchResult = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("vector_fields"), "path", OpenSearchBodies.text(_plus_1), "prefix", prefixValue, "limit", limit);
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
              _xblockexpression_4 = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("aliases"), "path", OpenSearchBodies.text(path), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_4;
            break;
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            Code.Call _xblockexpression_5 = null;
            {
              final OpenSearchIndex target = this.scopeIndex(owner, it.getKind().getLiteral());
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
              _xblockexpression_5 = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("terms"), "path", OpenSearchBodies.text(_plus_6), 
                "body", OpenSearchBodies.obj("size", OpenSearchBodies.num(0), "aggs", OpenSearchBodies.obj("names", OpenSearchBodies.obj("terms", OpenSearchBodies.obj("field", OpenSearchBodies.text(fieldName), "size", OpenSearchBodies.num(OpenSearchBodies.MAX_CANDIDATES))))), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_5;
            break;
          case SNAPSHOTS:
            _switchResult = OpenSearchBodies.call("names", "kind", OpenSearchBodies.text("snapshots"), "path", OpenSearchBodies.text("/_snapshot/vecdsl/_all"), "prefix", prefixValue, "limit", limit, "ignore", OpenSearchBodies.arr(OpenSearchBodies.num(404)));
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

  private OpenSearchIndex scopeIndex(final Concept owner, final String what) {
    OpenSearchIndex _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.index(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * OpenSearch builds vector indexes with each segment, so awaiting one waits for the index's health.
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
      Code.Call _call = OpenSearchBodies.call("wait_health", "index", OpenSearchBodies.text(this.schema.index(collection).name()), "status", OpenSearchBodies.text("yellow"), 
        "timeout_seconds", OpenSearchBodies.num(_xifexpression));
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
    Set<String> _keySet = ModelUtil.extParams(statement, OpenSearchGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "elementScope", "asOf", "reranker", "queryModel", "prefetches");
      this.queryExtensions(it);
      this.combinations(it);
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of((("the \'highlight\' clause on a vector search; " + OpenSearchStatements.NO_QUERY_TERMS) + "a vector search has not"));
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(searched));
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
          throw Unsupported.of("the \'mmr\' clause on a sparse field; MMR reranking compares the knn_vector values of the hits");
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
        Code.Items _arr = OpenSearchBodies.arr(OpenSearchBodies.obj("neural_sparse", OpenSearchBodies.obj(mapped.name(), OpenSearchBodies.obj("query_tokens", this.convert(it.getInput().getValue(), mapped)))));
        Code.Items _xifexpression = null;
        boolean _isEmpty_1 = filters.isEmpty();
        if (_isEmpty_1) {
          _xifexpression = null;
        } else {
          _xifexpression = OpenSearchBodies.arr(filters);
        }
        final Code.Entries sparseBody = this.shape(it, it.getCollection(), target, OpenSearchBodies.obj("query", this.boostQuery(target, OpenSearchBodies.obj("bool", OpenSearchBodies.obj("must", _arr, 
          "filter", _xifexpression)), it.getBoost())), it.getOutput(), this.excludedOf(it), it.isWithVectors());
        sparseBody.put("size", OpenSearchBodies.num(it.getTopK()));
        int _offsetOf = this.offsetOf(it);
        boolean _greaterThan = (_offsetOf > 0);
        if (_greaterThan) {
          sparseBody.put("from", OpenSearchBodies.num(this.offsetOf(it)));
        }
        Code.Call _paged = this.paged(this.searchCall(target, this.cursorOrder(it, target, sparseBody), OpenSearchBodies.relevance(), it.getBounds(), this.groupOf(it, target)), it, target);
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
      final boolean exhaustive = (((it.isExact() || mapped.vector().exact()) || (!Objects.equals(searchMetric, mapped.vector().metric()))) || ((it.getParams() != null) && (it.getParams().isExhaustive() || (ModelUtil.written(it.getParams(), "filterStrategy") && Objects.equals(it.getParams().getFilterStrategy().getLiteral(), "pre_filter")))));
      final Code.Expr queryVector = this.queryVectorOf(it.getInput(), mapped);
      final String searchSpace = OpenSearchBodies.exactSpace(searchMetric, mapped.vector().element());
      Code.Entries _xifexpression_2 = null;
      if (exhaustive) {
        Code.Entries _xblockexpression_1 = null;
        {
          boolean _written_2 = ModelUtil.written(it, "mmr");
          if (_written_2) {
            throw Unsupported.of("the \'mmr\' clause with an exact search; MMR reranking oversamples the k of a knn query");
          }
          SearchParams _params = it.getParams();
          boolean _tripleNotEquals = (_params != null);
          if (_tripleNotEquals) {
            this.schema.note(it.getParams(), "candidatePool", "filterStrategy", "oversampling", "rescore");
          }
          Code.Entries _boostQuery = this.boostQuery(target, OpenSearchBodies.obj("script_score", OpenSearchBodies.obj("query", this.boolOf(filters), "script", OpenSearchBodies.exactScript(mapped.name(), queryVector, searchSpace))), it.getBoost());
          Code.Num _num = OpenSearchBodies.num(it.getTopK());
          Code.Num _xifexpression_3 = null;
          int _offsetOf_1 = this.offsetOf(it);
          boolean _greaterThan_1 = (_offsetOf_1 > 0);
          if (_greaterThan_1) {
            _xifexpression_3 = OpenSearchBodies.num(this.offsetOf(it));
          } else {
            _xifexpression_3 = null;
          }
          _xblockexpression_1 = OpenSearchBodies.obj("query", _boostQuery, "size", _num, 
            "from", _xifexpression_3);
        }
        _xifexpression_2 = _xblockexpression_1;
      } else {
        Code.Entries _xblockexpression_2 = null;
        {
          if ((((it.getMmr() != null) && (it.getMmr().getVectorField() != null)) && (it.getMmr().getVectorField() != searched))) {
            throw Unsupported.of("mmr on another vector field than the searched one");
          }
          _xblockexpression_2 = this.knnBody(it, target, mapped, queryVector, filters);
        }
        _xifexpression_2 = _xblockexpression_2;
      }
      final Code.Entries requestBody = _xifexpression_2;
      final boolean postFiltered = (((((!exhaustive) && (it.getParams() != null)) && ModelUtil.written(it.getParams(), "filterStrategy")) && Objects.equals(it.getParams().getFilterStrategy().getLiteral(), "post_filter")) && (!filters.isEmpty()));
      if (postFiltered) {
        Code.Expr _get = requestBody.get("query");
        this.knnLeaf(((Code.Entries) _get)).put("filter", OpenSearchBodies.obj("match_all", Code.entries()));
        requestBody.put("post_filter", this.boolOf(filters));
      }
      if (((it.getParams() != null) && ModelUtil.written(it.getParams(), "timeoutMillis"))) {
        int _timeoutMillis = it.getParams().getTimeoutMillis();
        String _plus_2 = (Integer.valueOf(_timeoutMillis) + "ms");
        requestBody.put("timeout", OpenSearchBodies.text(_plus_2));
      }
      Code.Entries _xifexpression_3 = null;
      if (exhaustive) {
        _xifexpression_3 = OpenSearchBodies.scoreForm(OpenSearchBodies.exactForm(searchSpace, mapped.vector().element()), false);
      } else {
        _xifexpression_3 = OpenSearchBodies.scoreForm(mapped.vector().space(), false);
      }
      final Code.Entries formKind = _xifexpression_3;
      Code.Entries _xifexpression_4 = null;
      BoostBlock _boost = it.getBoost();
      boolean _tripleNotEquals = (_boost != null);
      if (_tripleNotEquals) {
        _xifexpression_4 = OpenSearchBodies.relevance();
      } else {
        Code.Entries _xifexpression_5 = null;
        if (nativeScore) {
          _xifexpression_5 = OpenSearchBodies.scoreForm(mapped.vector().space(), true);
        } else {
          _xifexpression_5 = formKind;
        }
        _xifexpression_4 = _xifexpression_5;
      }
      final Code.Entries form = _xifexpression_4;
      final Code.Entries shaped = this.cursorOrder(it, target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), this.excludedOf(it), it.isWithVectors()));
      Code.Call _xifexpression_6 = null;
      EQueryInputMode _kind_1 = it.getInput().getKind();
      boolean _equals = Objects.equals(_kind_1, EQueryInputMode.ID);
      if (_equals) {
        _xifexpression_6 = this.byStoredVector(it, it.getInput(), it.getCollection(), mapped, shaped, form);
      } else {
        _xifexpression_6 = this.searchCall(target, shaped, form, it.getBounds(), this.groupOf(it, target));
      }
      final Code.Call search = _xifexpression_6;
      final Integer k = this.globalK(it, exhaustive, postFiltered);
      if ((k != null)) {
        search.args().put("top_k", OpenSearchBodies.num((k).intValue()));
      }
      Code.Call _paged_1 = this.paged(search, it, target);
      Code.Return _return_1 = new Code.Return(_paged_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  /**
   * The k of the runtime's global top k (top_k), or null. A knn query takes k per segment, and a total, aggregations, collapse
   * groups, a post filter, a sort and boost functions see every segment's hits, so their answer depended on how documents fell
   * into segments. The runtime first finds the global top k, then runs the request restricted to those documents (the meaning
   * these clauses have in Elasticsearch, where k is global). A cursor page's k is CURSOR_WINDOW.
   */
  private Integer globalK(final EObject statement, final boolean exhaustive, final boolean postFiltered) {
    int _xblockexpression = (int) 0;
    {
      if ((exhaustive || ModelUtil.written(statement, "mmr"))) {
        return null;
      }
      Object _elvis = null;
      Object _feature = this.feature(statement, "sortClauses");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      boolean _isEmpty = ((List<SortClause>) _elvis).isEmpty();
      final boolean sorted = (!_isEmpty);
      if (((((((!ModelUtil.written(statement, "withTotal")) && (!ModelUtil.written(statement, "aggregations"))) && (!ModelUtil.written(statement, "groupBy"))) && (!postFiltered)) && (!sorted)) && (!ModelUtil.written(statement, "boost")))) {
        return null;
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(statement, "cursor");
      if (_written) {
        _xifexpression = OpenSearchStatements.CURSOR_WINDOW;
      } else {
        Object _feature_1 = this.feature(statement, "topK");
        int _offsetOf = this.offsetOf(statement);
        _xifexpression = ((((Integer) _feature_1)).intValue() + _offsetOf);
      }
      _xblockexpression = _xifexpression;
    }
    return Integer.valueOf(_xblockexpression);
  }

  /**
   * The request of a dense knn search: a knn query, in a function_score query with a boost, and the MMR reranker of the k-NN
   * plugin (ext.mmr) for mmr. A cursor page reads the CURSOR_WINDOW nearest records.
   */
  private Code.Entries knnBody(final EObject statement, final OpenSearchIndex target, final OpenSearchIndex.Field mapped, final Code.Expr queryVector, final List<Code.Expr> filters) {
    Code.Entries _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "topK");
      final Integer size = ((Integer) _feature);
      Object _feature_1 = this.feature(statement, "mmr");
      final MmrConfig mmrConfig = ((MmrConfig) _feature_1);
      Object _feature_2 = this.feature(statement, "boost");
      final BoostBlock block = ((BoostBlock) _feature_2);
      if (((mmrConfig != null) && (block != null))) {
        throw Unsupported.of("the \'boost\' clause with mmr; MMR reranking supports only a knn or neural query as the top-level query of a search");
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(statement, "cursor");
      if (_written) {
        _xifexpression = OpenSearchStatements.CURSOR_WINDOW;
      } else {
        int _offsetOf = this.offsetOf(statement);
        _xifexpression = ((size).intValue() + _offsetOf);
      }
      final int k = _xifexpression;
      int _xifexpression_1 = (int) 0;
      if ((mmrConfig == null)) {
        _xifexpression_1 = k;
      } else {
        int _xifexpression_2 = (int) 0;
        boolean _written_1 = ModelUtil.written(mmrConfig, "candidates");
        if (_written_1) {
          _xifexpression_2 = Math.max(mmrConfig.getCandidates(), k);
        } else {
          _xifexpression_2 = (3 * k);
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final int window = _xifexpression_1;
      Code.Entries _boostQuery = this.boostQuery(target, this.knnQuery(statement, target, mapped, queryVector, window, filters), block);
      Code.Num _num = OpenSearchBodies.num((size).intValue());
      Code.Num _xifexpression_3 = null;
      int _offsetOf_1 = this.offsetOf(statement);
      boolean _greaterThan = (_offsetOf_1 > 0);
      if (_greaterThan) {
        _xifexpression_3 = OpenSearchBodies.num(this.offsetOf(statement));
      } else {
        _xifexpression_3 = null;
      }
      final Code.Entries searchBody = OpenSearchBodies.obj("query", _boostQuery, "size", _num, 
        "from", _xifexpression_3);
      if ((mmrConfig != null)) {
        final double diversity = BigDecimal.ONE.subtract(BigDecimal.valueOf(mmrConfig.getDiversityLambda())).doubleValue();
        searchBody.put("ext", OpenSearchBodies.obj("mmr", OpenSearchBodies.obj("diversity", OpenSearchBodies.num(diversity), "candidates", OpenSearchBodies.num(window), "vector_field_path", OpenSearchBodies.text(mapped.name()))));
      }
      _xblockexpression = searchBody;
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
          String _get = OpenSearchStatements.CLAUSES.get(clause);
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
  private Code.Entries cursorOrder(final EObject statement, final OpenSearchIndex target, final Code.Entries requestBody) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(statement, "cursor");
      if (_written) {
        requestBody.put("sort", OpenSearchBodies.arr(OpenSearchBodies.obj("_score", OpenSearchBodies.obj("order", OpenSearchBodies.text("desc"))), OpenSearchBodies.obj(target.keyField().exact(), OpenSearchBodies.obj("order", OpenSearchBodies.text("asc")))));
      }
      _xblockexpression = requestBody;
    }
    return _xblockexpression;
  }

  /**
   * The cursor of a search, for the runtime's search_after (the key of the last hit of the previous page).
   */
  private Code.Call paged(final Code.Call search, final EObject statement, final OpenSearchIndex target) {
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
   * The field object of a knn query ({"knn": {field: {...}}}), also inside the function_score query of a boost.
   */
  private Code.Entries knnLeaf(final Code.Entries query) {
    Code.Entries _xblockexpression = null;
    {
      Code.Entries _xifexpression = null;
      boolean _has = query.has("function_score");
      if (_has) {
        Code.Expr _get = query.get("function_score");
        Code.Expr _get_1 = ((Code.Entries) _get).get("query");
        _xifexpression = ((Code.Entries) _get_1);
      } else {
        _xifexpression = query;
      }
      final Code.Entries inner = _xifexpression;
      Code.Expr _get_2 = inner.get("knn");
      final Code.Entries fields = ((Code.Entries) _get_2);
      Code.Expr _head = IterableExtensions.<Code.Expr>head(fields.entries().values());
      _xblockexpression = ((Code.Entries) _head);
    }
    return _xblockexpression;
  }

  /**
   * A search by the stored vector of a document: the runtime reads the vector and puts it into the knn query (also inside a
   * function_score query) or the params of the exact search's script before it sends the search.
   */
  private Code.Call byStoredVector(final EObject statement, final QueryInput queryInput, final VectorCollection collection, final OpenSearchIndex.Field mapped, final Code.Entries searchBody, final Code.Entries form) {
    Code.Call _xblockexpression = null;
    {
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = queryInput.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        _elvis = collection;
      }
      final OpenSearchIndex lookup = this.schema.index(_elvis);
      Code.Text _text = OpenSearchBodies.text(this.schema.index(collection).name());
      Code.Text _text_1 = OpenSearchBodies.text(lookup.name());
      Code.Text _text_2 = OpenSearchBodies.text(mapped.name());
      Code.Text _text_3 = OpenSearchBodies.text(this.schema.index(collection).key());
      Code.Items _arr = OpenSearchBodies.arr(Literals.of(queryInput.getValue()));
      Object _feature = this.feature(statement, "bounds");
      Code.Entries _boundsOf = this.boundsOf(((ScoreBounds) _feature));
      Code.Text _xifexpression = null;
      String _groupOf = this.groupOf(statement, this.schema.index(collection));
      boolean _tripleEquals = (_groupOf == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.text(this.groupOf(statement, this.schema.index(collection)));
      }
      _xblockexpression = OpenSearchBodies.call("recommend", "index", _text, "lookup", _text_1, "field", _text_2, "key", _text_3, 
        "body", searchBody, "score", form, "positive_ids", _arr, "bounds", _boundsOf, 
        "group", _xifexpression);
    }
    return _xblockexpression;
  }

  /**
   * The knn query of a field: the vector, k, the filter (efficient filtering of the Faiss and Lucene engines), ef_search from
   * the search parameters (else the index's own), and the rescoring of a quantized field.
   */
  private Code.Entries knnQuery(final EObject statement, final OpenSearchIndex target, final OpenSearchIndex.Field mapped, final Code.Expr queryVector, final int k, final List<Code.Expr> filters) {
    Code.Entries _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "params");
      final SearchParams searchParams = ((SearchParams) _feature);
      Integer pool = null;
      Code.Entries _xifexpression = null;
      Double _oversample = mapped.vector().oversample();
      boolean _tripleEquals = (_oversample == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.obj("oversample_factor", OpenSearchBodies.num((mapped.vector().oversample()).doubleValue()));
      }
      Code.Expr rescore = _xifexpression;
      if ((searchParams != null)) {
        boolean _written = ModelUtil.written(searchParams, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: the knn query takes k, ef_search and an oversample factor, effort settings with no declarable recall target");
        }
        boolean _written_1 = ModelUtil.written(searchParams, "probes");
        if (_written_1) {
          throw Unsupported.of("probes; nprobes belongs to the Faiss ivf method, which needs a model trained with the Train API");
        }
        boolean _written_2 = ModelUtil.written(searchParams, "scanBudget");
        if (_written_2) {
          throw Unsupported.of("scan-budget; an hnsw search takes ef_search and no bound on visited vectors");
        }
        Set<String> _keySet = ModelUtil.extParams(searchParams, OpenSearchGenerator.VENDOR).keySet();
        for (final String key : _keySet) {
          throw Unsupported.of(("extension parameter " + key));
        }
        boolean _written_3 = ModelUtil.written(searchParams, "candidatePool");
        if (_written_3) {
          pool = Integer.valueOf(searchParams.getCandidatePool());
        }
        boolean _written_4 = ModelUtil.written(searchParams, "oversampling");
        if (_written_4) {
          rescore = OpenSearchBodies.obj("oversample_factor", OpenSearchBodies.num(searchParams.getOversampling()));
        } else {
          if ((searchParams.isRescore() && (rescore == null))) {
            Code.Flag _flag = new Code.Flag(true);
            rescore = _flag;
          }
        }
      }
      Code.Num _num = OpenSearchBodies.num(k);
      Code.Entries _xifexpression_1 = null;
      boolean _isEmpty = filters.isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = OpenSearchBodies.obj("match_all", Code.entries());
      } else {
        _xifexpression_1 = this.boolOf(filters);
      }
      Code.Entries _xifexpression_2 = null;
      if ((pool == null)) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = OpenSearchBodies.obj("ef_search", OpenSearchBodies.num(OpenSearchBodies.candidates((pool).intValue(), k)));
      }
      _xblockexpression = OpenSearchBodies.obj("knn", OpenSearchBodies.obj(mapped.name(), OpenSearchBodies.obj("vector", queryVector, "k", _num, "filter", _xifexpression_1, 
        "method_parameters", _xifexpression_2, "rescore", rescore)));
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
        throw Unsupported.of("the \'mmr\' clause on a keyword search; MMR reranking supports only a knn or neural query as the top-level query of a search");
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
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
      Code.Items _arr = OpenSearchBodies.arr(textQuery);
      Code.Items _xifexpression_4 = null;
      boolean _isEmpty = filters.isEmpty();
      if (_isEmpty) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = OpenSearchBodies.arr(filters);
      }
      final Code.Entries scored = this.boostQuery(target, OpenSearchBodies.obj("bool", OpenSearchBodies.obj("must", _arr, "filter", _xifexpression_4)), it.getBoost());
      Code.Num _num = OpenSearchBodies.num(it.getTopK());
      Code.Num _xifexpression_5 = null;
      int _offsetOf = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        _xifexpression_5 = OpenSearchBodies.num(this.offsetOf(it));
      } else {
        _xifexpression_5 = null;
      }
      final Code.Entries requestBody = OpenSearchBodies.obj("query", scored, "size", _num, "from", _xifexpression_5);
      if (((it.getParams() != null) && ModelUtil.written(it.getParams(), "timeoutMillis"))) {
        int _timeoutMillis = it.getParams().getTimeoutMillis();
        String _plus = (Integer.valueOf(_timeoutMillis) + "ms");
        requestBody.put("timeout", OpenSearchBodies.text(_plus));
      }
      final Code.Entries shaped = this.cursorOrder(it, target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), this.excludedOf(it), it.isWithVectors()));
      Code.Call _paged = this.paged(this.searchCall(target, shaped, OpenSearchBodies.relevance(), it.getBounds(), this.groupOf(it, target)), it, target);
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
  private Code.Entries keywordQuery(final OpenSearchIndex target, final List<SearchFieldRef> refs, final QueryInput queryInput, final String type, final String operator, final Integer minimum, final String fuzzinessValue, final Integer slopValue) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Expr words = this.keywordText(queryInput);
      final List<String> fields = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<OpenSearchIndex.Field> _fields = target.fields();
        for (final OpenSearchIndex.Field mapped : _fields) {
          boolean _fullText = mapped.fullText();
          if (_fullText) {
            String _name = mapped.name();
            fields.add(_name);
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final OpenSearchIndex.Field mapped_1 = target.field(ModelUtil.fieldName(ref.getField()));
            OpenSearchIndex.Vector _vector = mapped_1.vector();
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
              String _number = OpenSearchFilter.number(ref.getBoost());
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
        _xifexpression = OpenSearchBodies.text(operator);
      }
      Code.Num _xifexpression_1 = null;
      if ((minimum == null)) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.num((minimum).intValue());
      }
      Code.Text _xifexpression_2 = null;
      if ((fuzzinessValue == null)) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = OpenSearchBodies.text(fuzzinessValue);
      }
      Code.Num _xifexpression_3 = null;
      if ((slopValue == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = OpenSearchBodies.num((slopValue).intValue());
      }
      final Code.Entries options = OpenSearchBodies.obj("operator", _xifexpression, "minimum_should_match", _xifexpression_1, 
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
                final Code.Entries perField = OpenSearchBodies.obj("query", words);
                perField.entries().putAll(options.entries());
                return OpenSearchBodies.obj(single, OpenSearchBodies.obj(IterableExtensions.<String>head(fields), perField));
              }
              final Function1<String, Code.Expr> _function = (String f) -> {
                Code.Text _text = OpenSearchBodies.text(f);
                return ((Code.Expr) _text);
              };
              final Code.Entries multi = OpenSearchBodies.obj("query", words, "fields", OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(fields, _function))), "type", OpenSearchBodies.text(multiType));
              multi.entries().putAll(options.entries());
              _xblockexpression_1 = OpenSearchBodies.obj("multi_match", multi);
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
                Code.Text _text = OpenSearchBodies.text(f);
                return ((Code.Expr) _text);
              };
              final Code.Entries queryString = OpenSearchBodies.obj("query", words, "fields", OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(fields, _function))));
              queryString.entries().putAll(options.entries());
              _xblockexpression_2 = OpenSearchBodies.obj("query_string", queryString);
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
                  final OpenSearchIndex.Field mapped_1 = target.field(parts[0]);
                  Code.Num _xifexpression_4 = null;
                  int _length = parts.length;
                  boolean _greaterThan = (_length > 1);
                  if (_greaterThan) {
                    _xifexpression_4 = OpenSearchBodies.num(Double.parseDouble(parts[1]));
                  } else {
                    _xifexpression_4 = null;
                  }
                  Code.Text _xifexpression_5 = null;
                  boolean _equals = Objects.equals(type, "fuzzy");
                  if (_equals) {
                    _xifexpression_5 = OpenSearchBodies.text("AUTO");
                  } else {
                    _xifexpression_5 = null;
                  }
                  final Code.Entries leaf = OpenSearchBodies.obj("value", words, "boost", _xifexpression_4, "fuzziness", _xifexpression_5);
                  Code.Entries _obj = OpenSearchBodies.obj(type, OpenSearchBodies.obj(mapped_1.name(), leaf));
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
                _xifexpression_4 = OpenSearchBodies.obj("bool", OpenSearchBodies.obj("should", OpenSearchBodies.arr(perField), "minimum_should_match", OpenSearchBodies.num(1)));
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
              final OpenSearchIndex.Field mapped_1 = target.field(parts[0]);
              Code.Num _xifexpression_4 = null;
              int _length = parts.length;
              boolean _greaterThan = (_length > 1);
              if (_greaterThan) {
                _xifexpression_4 = OpenSearchBodies.num(Double.parseDouble(parts[1]));
              } else {
                _xifexpression_4 = null;
              }
              Code.Text _xifexpression_5 = null;
              boolean _equals = Objects.equals(type, "fuzzy");
              if (_equals) {
                _xifexpression_5 = OpenSearchBodies.text("AUTO");
              } else {
                _xifexpression_5 = null;
              }
              final Code.Entries leaf = OpenSearchBodies.obj("value", words, "boost", _xifexpression_4, "fuzziness", _xifexpression_5);
              Code.Entries _obj = OpenSearchBodies.obj(type, OpenSearchBodies.obj(mapped_1.name(), leaf));
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
            _xifexpression_4 = OpenSearchBodies.obj("bool", OpenSearchBodies.obj("should", OpenSearchBodies.arr(perField), "minimum_should_match", OpenSearchBodies.num(1)));
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
  private Code.Entries boostQuery(final OpenSearchIndex target, final Code.Entries inner, final BoostBlock block) {
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
            _xifexpression = OpenSearchBodies.num(boostFunction.getWeight());
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
            _xifexpression_1 = new OpenSearchFilter(target).query(boostFunction.getCondition());
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
                Code.Entries _obj = OpenSearchBodies.obj("field_value_factor", OpenSearchBodies.obj("field", OpenSearchBodies.text(path), "missing", OpenSearchBodies.num(0)), "weight", weight, "filter", applies);
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
                  _xifexpression_4 = OpenSearchBodies.num(boostFunction.getDecayRate());
                } else {
                  _xifexpression_4 = null;
                }
                final Code.Entries decay = OpenSearchBodies.obj("origin", _of, "scale", _of_1, 
                  "offset", _xifexpression_3, 
                  "decay", _xifexpression_4);
                Code.Entries _obj_1 = OpenSearchBodies.obj(boostFunction.getShape().getLiteral(), OpenSearchBodies.obj(path, decay), "weight", weight, "filter", applies);
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
                    Code.Num _num = OpenSearchBodies.num(1);
                    _elvis = _num;
                  }
                  Code.Entries _obj_2 = OpenSearchBodies.obj("filter", applies, "weight", _elvis);
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
                Code.Num _num = OpenSearchBodies.num(1);
                _elvis = _num;
              }
              Code.Entries _obj_2 = OpenSearchBodies.obj("filter", applies, "weight", _elvis);
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
      _xblockexpression = OpenSearchBodies.obj("function_score", OpenSearchBodies.obj("query", inner, "functions", OpenSearchBodies.arr(functions), "score_mode", OpenSearchBodies.text(_xifexpression_2), 
        "boost_mode", OpenSearchBodies.text(boostMode)));
    }
    return _xblockexpression;
  }

  private String fieldPath(final OpenSearchIndex target, final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        return OpenSearchBodies.field(path.getRawPath());
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
   * Hybrid search. rrf is the server's: one hybrid query whose subqueries are the legs (a knn query, or a text query with the
   * shared and the leg's filters), fused by the score-ranker-processor of a temporary search pipeline, which sums 1/(k + rank)
   * as vecDSL defines it; the runtime orders equal fused scores by key. weighted and relative_score are fused in the runtime from
   * one search per leg, because the normalization-processor's min_max floors normalized scores at 0.001, divides by the sum of
   * the weights of the legs that found a hit and normalizes the server's transformed scores, not the canonical similarities; rrf with
   * legs of different limits is fused in the runtime too (pagination_depth bounds every subquery alike). The result clauses
   * (cursor, with-total, sort, aggregate, boost, mmr, group-by) go to the shared result-shape module: the fused records of the
   * candidate window are its candidates.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "reranker");
      if (((!it.getHighlighted().isEmpty()) && (!IterableExtensions.<HybridLeg>exists(it.getLegs(), ((Function1<HybridLeg, Boolean>) (HybridLeg leg) -> {
        return Boolean.valueOf((leg instanceof KeywordLeg));
      }))))) {
        throw Unsupported.of((("the \'highlight\' clause on a hybrid search without a keyword leg; " + OpenSearchStatements.NO_QUERY_TERMS) + "its vector legs have not"));
      }
      this.queryExtensions(it);
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      final ShapeSpec shapes = ShapeSpec.of(it);
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "fusion");
      if (_written) {
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
      final List<Code.Expr> subqueries = CollectionLiterals.<Code.Expr>newArrayList();
      final Set<Integer> limits = CollectionLiterals.<Integer>newHashSet();
      int position = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Integer _xifexpression_1 = null;
          boolean _written_1 = ModelUtil.written(leg, "legLimit");
          if (_written_1) {
            Object _feature = this.feature(leg, "legLimit");
            _xifexpression_1 = ((Integer) _feature);
          } else {
            _xifexpression_1 = Integer.valueOf(defaultLimit);
          }
          final Integer legLimit = _xifexpression_1;
          limits.add(legLimit);
          final List<Code.Expr> legFilters = CollectionLiterals.<Code.Expr>newArrayList();
          List<Code.Expr> _clauses = this.clauses(it, it.getCollection(), target, it.getFilter(), null);
          Iterables.<Code.Expr>addAll(legFilters, _clauses);
          Object _feature_1 = this.feature(leg, "filter");
          final FilterBlock legFilter = ((FilterBlock) _feature_1);
          if ((legFilter != null)) {
            Code.Entries _query = new OpenSearchFilter(target).query(legFilter.getCondition());
            legFilters.add(_query);
          }
          Set<String> _keySet = ModelUtil.extParams(leg, OpenSearchGenerator.VENDOR).keySet();
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
                throw Unsupported.of((("bounds on a hybrid leg; " + OpenSearchStatements.PENDING) + " (a score condition on the leg in the runtime)"));
              }
              final VectorField legField = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
              final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(legField));
              boolean _sparse = mapped.vector().sparse();
              if (_sparse) {
                throw Unsupported.of((("a sparse vector leg; " + OpenSearchStatements.PENDING) + " (a neural_sparse query as a leg)"));
              }
              if ((((ModelUtil.written(leg, "metric") && (!Objects.equals(((VectorLeg)leg).getMetric().getLiteral(), mapped.vector().metric()))) || ((VectorLeg)leg).isExact()) || mapped.vector().exact())) {
                throw Unsupported.of((("an exact or re-metered vector leg; " + OpenSearchStatements.PENDING) + " (a knn_score script query as a leg)"));
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
              _xblockexpression_1 = this.knnQuery(leg, target, mapped, this.queryVectorOf(legInput, mapped), (legLimit).intValue(), legFilters);
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
                boolean _written_2 = ModelUtil.written(leg, "textQueryType");
                if (_written_2) {
                  _xifexpression_2 = ((KeywordLeg)leg).getTextQueryType().getLiteral();
                } else {
                  _xifexpression_2 = "match";
                }
                String _xifexpression_3 = null;
                boolean _written_3 = ModelUtil.written(leg, "matchOperator");
                if (_written_3) {
                  _xifexpression_3 = ((KeywordLeg)leg).getMatchOperator().getLiteral();
                } else {
                  _xifexpression_3 = null;
                }
                Integer _xifexpression_4 = null;
                boolean _written_4 = ModelUtil.written(leg, "minShouldMatch");
                if (_written_4) {
                  _xifexpression_4 = Integer.valueOf(((KeywordLeg)leg).getMinShouldMatch());
                } else {
                  _xifexpression_4 = null;
                }
                final Code.Entries textQuery = this.keywordQuery(target, _searchFields, _elvis, _xifexpression_2, _xifexpression_3, _xifexpression_4, null, null);
                Code.Items _arr = OpenSearchBodies.arr(textQuery);
                Code.Items _xifexpression_5 = null;
                boolean _isEmpty = legFilters.isEmpty();
                if (_isEmpty) {
                  _xifexpression_5 = null;
                } else {
                  _xifexpression_5 = OpenSearchBodies.arr(legFilters);
                }
                _xblockexpression_1 = OpenSearchBodies.obj("bool", OpenSearchBodies.obj("must", _arr, "filter", _xifexpression_5));
              }
              _switchResult = _xblockexpression_1;
            }
          }
          final Code.Entries legQuery = _switchResult;
          subqueries.add(legQuery);
          Code.Entries _switchResult_1 = null;
          boolean _matched_1 = false;
          if (leg instanceof VectorLeg) {
            _matched_1=true;
            _switchResult_1 = OpenSearchBodies.scoreForm(target.field(ModelUtil.fieldName(this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField()))).vector().space(), false);
          }
          if (!_matched_1) {
            _switchResult_1 = OpenSearchBodies.relevance();
          }
          final Code.Entries legForm = _switchResult_1;
          final Code.Entries legBody = OpenSearchBodies.obj("query", legQuery, "size", OpenSearchBodies.num((legLimit).intValue()));
          Code.Entries _xifexpression_2 = null;
          boolean _plain = shapes.plain();
          if (_plain) {
            _xifexpression_2 = this.shape(it, it.getCollection(), target, legBody, it.getOutput(), this.excludedOf(it), it.isWithVectors());
          } else {
            _xifexpression_2 = this.candidateBody(it, target, legBody, shapes);
          }
          Code.Entries _obj = OpenSearchBodies.obj("body", _xifexpression_2, 
            "score", legForm, "weight", OpenSearchBodies.num((weights.get(position)).doubleValue()));
          parts.add(_obj);
          position = (position + 1);
        }
      }
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "fusionConstant");
      if (_written_1) {
        _xifexpression_1 = it.getFusionConstant();
      } else {
        _xifexpression_1 = 60;
      }
      final int k = _xifexpression_1;
      if ((Objects.equals(fusionKind, "rrf") && (limits.size() == 1))) {
        if (((k < 1) || (k > 10000))) {
          throw Unsupported.of((("fusion-k " + Integer.valueOf(k)) + "; the rank_constant of the score-ranker-processor lies in [1, 10000]"));
        }
        Code.Items _arr = OpenSearchBodies.arr(OpenSearchBodies.obj("score-ranker-processor", OpenSearchBodies.obj("combination", OpenSearchBodies.obj("technique", OpenSearchBodies.text("rrf"), "rank_constant", OpenSearchBodies.num(k)))));
        Code.Items _xifexpression_2 = null;
        boolean _isExplain = it.isExplain();
        if (_isExplain) {
          _xifexpression_2 = OpenSearchBodies.arr(OpenSearchBodies.obj("hybrid_score_explanation", Code.entries()));
        } else {
          _xifexpression_2 = null;
        }
        final Code.Entries pipeline = OpenSearchBodies.obj("phase_results_processors", _arr, 
          "response_processors", _xifexpression_2);
        final Code.Entries hybridQuery = OpenSearchBodies.obj("hybrid", OpenSearchBodies.obj("queries", OpenSearchBodies.arr(subqueries), "pagination_depth", OpenSearchBodies.num(IterableExtensions.<Integer>head(limits).doubleValue())));
        boolean _plain = shapes.plain();
        if (_plain) {
          final Code.Entries hybridBody = this.shape(it, it.getCollection(), target, OpenSearchBodies.obj("query", hybridQuery, "size", OpenSearchBodies.num(it.getTopK()), "from", OpenSearchBodies.num(this.offsetOf(it)), "search_pipeline", pipeline), 
            it.getOutput(), this.excludedOf(it), it.isWithVectors());
          Code.Call _searchCall = this.searchCall(target, hybridBody, OpenSearchBodies.obj("kind", OpenSearchBodies.text("fused")), it.getBounds(), null);
          Code.Return _return = new Code.Return(_searchCall);
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
        }
        Code.Text _text = OpenSearchBodies.text(target.name());
        Code.Entries _candidateBody = this.candidateBody(it, target, OpenSearchBodies.obj("query", hybridQuery, "size", shapes.window(), 
          "search_pipeline", pipeline), shapes);
        Code.Text _text_1 = OpenSearchBodies.text(target.key());
        Code.Entries _obj = OpenSearchBodies.obj("kind", OpenSearchBodies.text("fused"));
        Code.Flag _flag = new Code.Flag(true);
        final Code.Call candidateSearch = OpenSearchBodies.call("search", "index", _text, "body", _candidateBody, "key", _text_1, "score", _obj, "candidates", _flag);
        Code.Call _shapedCall = this.shapedCall(candidateSearch, shapes, target);
        Code.Return _return_1 = new Code.Return(_shapedCall);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      Code.Text _text_2 = OpenSearchBodies.text(target.name());
      Code.Items _arr_1 = OpenSearchBodies.arr(parts);
      Code.Text _text_3 = OpenSearchBodies.text(fusionKind);
      Code.Num _num = OpenSearchBodies.num(k);
      Code.Expr _xifexpression_3 = null;
      boolean _plain_1 = shapes.plain();
      if (_plain_1) {
        _xifexpression_3 = OpenSearchBodies.num(it.getTopK());
      } else {
        _xifexpression_3 = shapes.window();
      }
      int _xifexpression_4 = (int) 0;
      boolean _plain_2 = shapes.plain();
      if (_plain_2) {
        _xifexpression_4 = this.offsetOf(it);
      } else {
        _xifexpression_4 = 0;
      }
      Code.Num _num_1 = OpenSearchBodies.num(_xifexpression_4);
      Code.Text _text_4 = OpenSearchBodies.text(target.key());
      Code.Entries _xifexpression_5 = null;
      boolean _plain_3 = shapes.plain();
      if (_plain_3) {
        _xifexpression_5 = this.boundsOf(it.getBounds());
      } else {
        _xifexpression_5 = null;
      }
      Code.Flag _xifexpression_6 = null;
      boolean _plain_4 = shapes.plain();
      if (_plain_4) {
        _xifexpression_6 = null;
      } else {
        _xifexpression_6 = new Code.Flag(true);
      }
      final Code.Call fused = OpenSearchBodies.call("hybrid", "index", _text_2, "legs", _arr_1, "fusion", _text_3, "k", _num, 
        "size", _xifexpression_3, "offset", _num_1, "key", _text_4, 
        "bounds", _xifexpression_5, "candidates", _xifexpression_6);
      Code.Call _xifexpression_7 = null;
      boolean _plain_5 = shapes.plain();
      if (_plain_5) {
        _xifexpression_7 = fused;
      } else {
        _xifexpression_7 = this.shapedCall(fused, shapes, target);
      }
      Code.Return _return_2 = new Code.Return(_xifexpression_7);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
    }
    return _xblockexpression;
  }

  /**
   * rt.shape over a candidate window: the module's spec in the index's names and the vector fields the candidates carry.
   */
  private Code.Call shapedCall(final Code.Call candidateSearch, final ShapeSpec shapes, final OpenSearchIndex target) {
    return OpenSearchBodies.call("shape", "candidates", candidateSearch, "spec", OpenSearchShapes.spec(shapes, target), "vectors", Code.texts(OpenSearchShapes.carried(shapes, target)));
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
        throw Unsupported.of((("the \'highlight\' clause on a recommendation; " + OpenSearchStatements.NO_QUERY_TERMS) + "a recommendation has not"));
      }
      this.queryExtensions(it);
      this.combinations(it);
      boolean _isEmpty_1 = it.getPositives().isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("recommend without positive examples");
      }
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final OpenSearchIndex lookup = this.schema.index(_elvis);
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(searched));
      boolean _sparse = mapped.vector().sparse();
      if (_sparse) {
        throw Unsupported.of("recommendations on a sparse field; the average-vector strategy averages dense vectors");
      }
      if ((Objects.equals(mapped.vector().element(), "binary") || Objects.equals(mapped.vector().element(), "byte"))) {
        String _element = mapped.vector().element();
        String _plus = ("recommendations on " + _element);
        String _plus_1 = (_plus + " vectors; the average of byte or binary vectors is no vector of the field");
        throw Unsupported.of(_plus_1);
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
          Code.Entries _obj = OpenSearchBodies.obj("bool", OpenSearchBodies.obj("must_not", OpenSearchBodies.arr(OpenSearchBodies.obj("ids", OpenSearchBodies.obj("values", this.idsOf(examples))))));
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
      Code.Text _text = OpenSearchBodies.text(target.name());
      Code.Text _text_1 = OpenSearchBodies.text(lookup.name());
      Code.Text _text_2 = OpenSearchBodies.text(mapped.name());
      Code.Text _text_3 = OpenSearchBodies.text(target.key());
      Code.Entries _xifexpression = null;
      BoostBlock _boost = it.getBoost();
      boolean _tripleNotEquals = (_boost != null);
      if (_tripleNotEquals) {
        _xifexpression = OpenSearchBodies.relevance();
      } else {
        _xifexpression = OpenSearchBodies.scoreForm(mapped.vector().space(), nativeScore);
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_3 = positiveIds.isEmpty();
      if (_isEmpty_3) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.arr(positiveIds);
      }
      Code.Items _xifexpression_2 = null;
      boolean _isEmpty_4 = positiveVectors.isEmpty();
      if (_isEmpty_4) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = OpenSearchBodies.arr(positiveVectors);
      }
      Code.Items _xifexpression_3 = null;
      boolean _isEmpty_5 = negativeIds.isEmpty();
      if (_isEmpty_5) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = OpenSearchBodies.arr(negativeIds);
      }
      Code.Items _xifexpression_4 = null;
      boolean _isEmpty_6 = negativeVectors.isEmpty();
      if (_isEmpty_6) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = OpenSearchBodies.arr(negativeVectors);
      }
      Code.Entries _boundsOf = this.boundsOf(it.getBounds());
      Code.Text _xifexpression_5 = null;
      if ((group == null)) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = OpenSearchBodies.text(group);
      }
      final Code.Call recommendation = OpenSearchBodies.call("recommend", "index", _text, "lookup", _text_1, "field", _text_2, "key", _text_3, 
        "body", requestBody, "score", _xifexpression, "positive_ids", _xifexpression_1, 
        "positive_vectors", _xifexpression_2, "negative_ids", _xifexpression_3, 
        "negative_vectors", _xifexpression_4, "bounds", _boundsOf, "group", _xifexpression_5);
      final Integer k = this.globalK(it, false, false);
      if ((k != null)) {
        recommendation.args().put("top_k", OpenSearchBodies.num((k).intValue()));
      }
      Code.Call _paged = this.paged(recommendation, it, target);
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
      _xifexpression = OpenSearchBodies.arr(value);
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      final List<Code.Expr> slices = this.clauses(it, it.getCollection(), target, null, null);
      final List<Code.Expr> fields = CollectionLiterals.<Code.Expr>newArrayList();
      if (((it.getOutput() != null) && (!it.getOutput().isAllFields()))) {
        EList<Field> _fields = it.getOutput().getFields();
        for (final Field f : _fields) {
          Field _primaryKey = it.getCollection().getPrimaryKey();
          boolean _tripleNotEquals = (f != _primaryKey);
          if (_tripleNotEquals) {
            Code.Text _text = OpenSearchBodies.text(target.field(ModelUtil.fieldName(f)).name());
            fields.add(_text);
          }
        }
      }
      final List<Code.Expr> hidden = CollectionLiterals.<Code.Expr>newArrayList();
      boolean _isWithVectors = it.isWithVectors();
      boolean _not = (!_isWithVectors);
      if (_not) {
        List<String> _vectorFields = target.vectorFields();
        for (final String v : _vectorFields) {
          boolean _contains = fields.contains(OpenSearchBodies.text(v));
          boolean _not_1 = (!_contains);
          if (_not_1) {
            Code.Text _text_1 = OpenSearchBodies.text(v);
            hidden.add(_text_1);
          }
        }
      }
      Code.Text _text_2 = OpenSearchBodies.text(target.name());
      final Function1<Literal, Code.Expr> _function = (Literal i) -> {
        return Literals.of(i);
      };
      Code.Items _items = Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      Code.Text _text_3 = OpenSearchBodies.text(target.key());
      Code.Items _xifexpression = null;
      if (((it.getOutput() == null) || it.getOutput().isAllFields())) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.arr(fields);
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty = hidden.isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.arr(hidden);
      }
      Code.Items _xifexpression_2 = null;
      boolean _isEmpty_1 = slices.isEmpty();
      if (_isEmpty_1) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = OpenSearchBodies.arr(slices);
      }
      Code.Call _call = OpenSearchBodies.call("fetch", "index", _text_2, "values", _items, "key", _text_3, 
        "fields", _xifexpression, "excludes", _xifexpression_1, 
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      String _name = target.name();
      String _plus = ("/" + _name);
      String _plus_1 = (_plus + "/_count");
      Code.Call _call = OpenSearchBodies.call("request", "method", OpenSearchBodies.text("POST"), "path", OpenSearchBodies.text(_plus_1), 
        "body", OpenSearchBodies.obj("query", this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null))), "select", OpenSearchBodies.arr(OpenSearchBodies.text("count")));
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
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
      final OpenSearchIndex.Field keyField = target.keyField();
      Code.Entries _obj = OpenSearchBodies.obj(keyField.exact(), OpenSearchBodies.obj("order", OpenSearchBodies.text("asc")));
      ordering.add(_obj);
      Code.Entries _boolOf = this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null));
      Code.Items _arr = OpenSearchBodies.arr(ordering);
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = it.getScrollLimit();
      } else {
        _xifexpression = 10;
      }
      Code.Num _num = OpenSearchBodies.num(_xifexpression);
      Code.Num _xifexpression_1 = null;
      int _offsetOf = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        _xifexpression_1 = OpenSearchBodies.num(this.offsetOf(it));
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries requestBody = OpenSearchBodies.obj("query", _boolOf, "sort", _arr, 
        "size", _num, "from", _xifexpression_1);
      String _cursor = it.getCursor();
      boolean _tripleNotEquals = (_cursor != null);
      if (_tripleNotEquals) {
        if (((!it.getSortClauses().isEmpty()) || (this.offsetOf(it) > 0))) {
          throw Unsupported.of("the \'cursor\' clause with sort or offset; search_after continues after the key of the last page");
        }
        requestBody.put("search_after", OpenSearchBodies.arr(this.cursorValue(keyField, it.getCursor())));
      }
      if ((ModelUtil.written(it, "sliceTotal") && (it.getSliceTotal() > 1))) {
        requestBody.put("slice", OpenSearchBodies.obj("id", OpenSearchBodies.num(it.getSliceIndex()), "max", OpenSearchBodies.num(it.getSliceTotal())));
      }
      Code.Call _searchCall = this.searchCall(target, this.shape(it, it.getCollection(), target, requestBody, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors()), null, null, null);
      Code.Return _return = new Code.Return(_searchCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr cursorValue(final OpenSearchIndex.Field keyField, final String cursorText) {
    Code.Text _xblockexpression = null;
    {
      Code.Expr _get = keyField.mapping().get("type");
      final String mappedType = ((Code.Text) _get).value();
      boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("integer", "long", "byte")).contains(mappedType);
      if (_contains) {
        try {
          return OpenSearchBodies.num(Long.parseLong(cursorText));
        } catch (final Throwable _t) {
          if (_t instanceof NumberFormatException) {
            throw Unsupported.of((("the cursor " + cursorText) + " on an integer key"));
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
      _xblockexpression = OpenSearchBodies.text(cursorText);
    }
    return _xblockexpression;
  }

  private Code.Entries sortEntry(final OpenSearchIndex target, final SortClause clause, final boolean scored) {
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
      final Code.Text order = OpenSearchBodies.text(_xifexpression);
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        if ((!scored)) {
          throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
        }
        return OpenSearchBodies.obj("_score", OpenSearchBodies.obj("order", order));
      }
      final FieldPath path = clause.getField();
      if (((path.getRawPath() == null) && (!(path.getField() instanceof PayloadField)))) {
        throw Unsupported.of("sorting by a vector field");
      }
      String _xifexpression_1 = null;
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        _xifexpression_1 = OpenSearchBodies.field(path.getRawPath());
      } else {
        String _xblockexpression_1 = null;
        {
          final OpenSearchIndex.Field top = target.field(ModelUtil.fieldName(path.getField()));
          final Field leaf = ModelUtil.leaf(path);
          final String dotted = this.fieldPath(target, path);
          String _xifexpression_2 = null;
          boolean _isEmpty = path.getSubFields().isEmpty();
          if (_isEmpty) {
            _xifexpression_2 = top.exact();
          } else {
            String _xifexpression_3 = null;
            if (((leaf instanceof PayloadField) && ((PayloadField) leaf).isFullText())) {
              _xifexpression_3 = ((dotted + ".") + OpenSearchBodies.RAW);
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
      _xblockexpression = OpenSearchBodies.obj(name, OpenSearchBodies.obj("order", order, "missing", OpenSearchBodies.text("_last")));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      final Code.Entries matching = this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null));
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      Code.Entries _xifexpression = null;
      if (first) {
        _xifexpression = OpenSearchBodies.obj("query", matching, "sort", OpenSearchBodies.arr(OpenSearchBodies.obj(target.keyField().exact(), OpenSearchBodies.obj("order", OpenSearchBodies.text("asc")))), "size", OpenSearchBodies.num(it.getSampleSize()));
      } else {
        _xifexpression = OpenSearchBodies.obj("query", OpenSearchBodies.obj("function_score", OpenSearchBodies.obj("query", matching, "random_score", Code.entries(), "boost_mode", OpenSearchBodies.text("replace"))), "size", OpenSearchBodies.num(it.getSampleSize()));
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
      final OpenSearchIndex target = this.schema.index(it.getCollection());
      final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(this.vectorFieldOf(it.getCollection(), it.getVectorField())));
      boolean _sparse = mapped.vector().sparse();
      if (_sparse) {
        throw Unsupported.of("a distance matrix of sparse vectors; the knn query of the runtime compares knn_vector values");
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
      Code.Call _call = OpenSearchBodies.call("distance_matrix", "index", OpenSearchBodies.text(target.name()), "field", OpenSearchBodies.text(mapped.name()), "key", OpenSearchBodies.text(target.key()), 
        "sample", OpenSearchBodies.num(_xifexpression), "limit", OpenSearchBodies.num(_xifexpression_1), 
        "score", OpenSearchBodies.scoreForm(mapped.vector().space(), false), "query", this.boolOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null)));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + OpenSearchStatements.NO_GENERATION));
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
    String _plus = (OpenSearchStatements.NOT_MAPPED + _name);
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
        _xifexpression = OpenSearchBodies.num(scoreBounds.getMin());
      } else {
        _xifexpression = null;
      }
      Code.Num _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(scoreBounds, "max");
      if (_written_1) {
        _xifexpression_1 = OpenSearchBodies.num(scoreBounds.getMax());
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries result = OpenSearchBodies.obj("min", _xifexpression, "max", _xifexpression_1);
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

  private Code.Call searchCall(final OpenSearchIndex target, final Code.Entries requestBody, final Code.Entries form, final ScoreBounds scoreBounds, final String group) {
    Code.Text _text = OpenSearchBodies.text(target.name());
    Code.Text _text_1 = OpenSearchBodies.text(target.key());
    Code.Entries _boundsOf = this.boundsOf(scoreBounds);
    Code.Text _xifexpression = null;
    if ((group == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = OpenSearchBodies.text(group);
    }
    return OpenSearchBodies.call("search", "index", _text, "body", requestBody, "key", _text_1, "score", form, "bounds", _boundsOf, 
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
  private List<Code.Expr> clauses(final EObject statement, final VectorCollection collection, final OpenSearchIndex target, final FilterBlock block, final Code.Expr idValues) {
    List<Code.Expr> _xblockexpression = null;
    {
      final List<Code.Expr> result = CollectionLiterals.<Code.Expr>newArrayList();
      if ((idValues != null)) {
        Code.Entries _obj = OpenSearchBodies.obj("ids", OpenSearchBodies.obj("values", idValues));
        result.add(_obj);
      }
      if ((block != null)) {
        Code.Entries _query = new OpenSearchFilter(target).query(block.getCondition());
        result.add(_query);
      }
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Entries _term = OpenSearchSchema.term(target.tenant(), OpenSearchBodies.text(tenant.getName()));
        result.add(_term);
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Entries _term_1 = OpenSearchSchema.term(target.namespace(), OpenSearchBodies.text(((String) namespace)));
        result.add(_term_1);
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition single = ((Partition) _feature_1);
      Object _feature_2 = this.feature(statement, "partitions");
      final List<Partition> several = ((List<Partition>) _feature_2);
      final List<Code.Expr> named = CollectionLiterals.<Code.Expr>newArrayList();
      if ((single != null)) {
        Code.Text _text = OpenSearchBodies.text(single.getName());
        named.add(_text);
      }
      if ((several != null)) {
        for (final Partition p : several) {
          Code.Text _text_1 = OpenSearchBodies.text(p.getName());
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
        Code.Entries _obj_1 = OpenSearchBodies.obj("terms", OpenSearchBodies.obj(target.partition(), OpenSearchBodies.arr(named)));
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
      _xifexpression = OpenSearchBodies.obj("match_all", Code.entries());
    } else {
      _xifexpression = OpenSearchBodies.obj("bool", OpenSearchBodies.obj("filter", OpenSearchBodies.arr(filters)));
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
            _xifexpression = OpenSearchFilter.number(((NumberLit) v).getValue());
          }
          Code.Text _text = OpenSearchBodies.text(_xifexpression);
          return ((Code.Expr) _text);
        };
        return OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(values, _function_1)));
      }
      final Function1<Literal, Code.Expr> _function_2 = (Literal v) -> {
        return Literals.of(v);
      };
      _xblockexpression = OpenSearchBodies.call("ids", "values", Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(values, _function_2))));
    }
    return _xblockexpression;
  }

  /**
   * The _source of the hits: the key and the output fields, vectors only with with-vectors (or named in the output).
   */
  private Code.Entries shape(final EObject statement, final VectorCollection collection, final OpenSearchIndex target, final Code.Entries requestBody, final OutputSpec outputSpec, final List<Field> excluded, final boolean vectors) {
    Code.Entries _xblockexpression = null;
    {
      final List<Code.Expr> includes = CollectionLiterals.<Code.Expr>newArrayList();
      boolean returnVectors = vectors;
      if (((outputSpec != null) && (!outputSpec.isAllFields()))) {
        Code.Text _text = OpenSearchBodies.text(target.key());
        includes.add(_text);
        EList<Field> _fields = outputSpec.getFields();
        for (final Field f : _fields) {
          {
            if ((f instanceof VectorField)) {
              returnVectors = true;
            }
            final Code.Text named = OpenSearchBodies.text(target.field(ModelUtil.fieldName(f)).name());
            if ((((f != collection.getPrimaryKey()) && (!excluded.contains(f))) && (!includes.contains(named)))) {
              includes.add(named);
            }
          }
        }
        if (vectors) {
          List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
          for (final VectorField v : _vectorFields) {
            {
              final Code.Text named = OpenSearchBodies.text(target.field(ModelUtil.fieldName(v)).name());
              if (((!excluded.contains(v)) && (!includes.contains(named)))) {
                includes.add(named);
              }
            }
          }
        }
      }
      final List<Code.Expr> excludes = CollectionLiterals.<Code.Expr>newArrayList();
      final Function1<Field, Code.Expr> _function = (Field f_1) -> {
        Code.Text _text_1 = OpenSearchBodies.text(target.field(ModelUtil.fieldName(f_1)).name());
        return ((Code.Expr) _text_1);
      };
      List<Code.Expr> _map = ListExtensions.<Field, Code.Expr>map(excluded, _function);
      Iterables.<Code.Expr>addAll(excludes, _map);
      if ((!returnVectors)) {
        List<String> _vectorFields_1 = target.vectorFields();
        for (final String v_1 : _vectorFields_1) {
          boolean _contains = excludes.contains(OpenSearchBodies.text(v_1));
          boolean _not = (!_contains);
          if (_not) {
            Code.Text _text_1 = OpenSearchBodies.text(v_1);
            excludes.add(_text_1);
          }
        }
      }
      Code.Items _xifexpression = null;
      boolean _isEmpty = includes.isEmpty();
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = OpenSearchBodies.arr(includes);
      }
      Code.Items _xifexpression_1 = null;
      boolean _isEmpty_1 = excludes.isEmpty();
      if (_isEmpty_1) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = OpenSearchBodies.arr(excludes);
      }
      requestBody.put("_source", OpenSearchBodies.obj("includes", _xifexpression, "excludes", _xifexpression_1));
      boolean _written = ModelUtil.written(statement, "withTotal");
      if (_written) {
        Code.Flag _flag = new Code.Flag(true);
        requestBody.put("track_total_hits", _flag);
      }
      this.marks(statement, target, requestBody);
      Object _elvis = null;
      Object _feature = this.feature(statement, "sortClauses");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<SortClause> sortClauses = ((List<SortClause>) _elvis);
      if (((!sortClauses.isEmpty()) && (!requestBody.has("sort")))) {
        final Function1<SortClause, Code.Expr> _function_1 = (SortClause c) -> {
          Code.Entries _sortEntry = this.sortEntry(target, c, true);
          return ((Code.Expr) _sortEntry);
        };
        requestBody.put("sort", OpenSearchBodies.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<SortClause, Code.Expr>map(sortClauses, _function_1))));
      }
      if ((IterableExtensions.<SortClause>exists(sortClauses, ((Function1<SortClause, Boolean>) (SortClause it) -> {
        boolean _isByRelevance = it.isByRelevance();
        return Boolean.valueOf((!_isByRelevance));
      })) && (!requestBody.has("track_scores")))) {
        Code.Flag _flag_1 = new Code.Flag(true);
        requestBody.put("track_scores", _flag_1);
      }
      Object _elvis_1 = null;
      Object _feature_1 = this.feature(statement, "aggregations");
      if (_feature_1 != null) {
        _elvis_1 = _feature_1;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      final List<AggregationClause> aggregations = ((List<AggregationClause>) _elvis_1);
      boolean _isEmpty_2 = aggregations.isEmpty();
      boolean _not_1 = (!_isEmpty_2);
      if (_not_1) {
        requestBody.put("aggs", this.aggs(target, aggregations));
      }
      final String group = this.groupOf(statement, target);
      if ((group != null)) {
        Integer _xifexpression_2 = null;
        boolean _written_1 = ModelUtil.written(statement, "groupSize");
        if (_written_1) {
          Object _feature_2 = this.feature(statement, "groupSize");
          _xifexpression_2 = ((Integer) _feature_2);
        } else {
          _xifexpression_2 = Integer.valueOf(1);
        }
        final Integer size = _xifexpression_2;
        requestBody.put("collapse", OpenSearchBodies.obj("field", OpenSearchBodies.text(group), "inner_hits", OpenSearchBodies.obj("name", OpenSearchBodies.text(OpenSearchBodies.GROUP), "size", OpenSearchBodies.num((size).intValue()), 
          "_source", requestBody.get("_source"))));
      }
      this.extensionBody(statement, requestBody);
      _xblockexpression = requestBody;
    }
    return _xblockexpression;
  }

  /**
   * explain and highlight, which every search body and every candidate request of the shared result-shape module carries.
   */
  private void marks(final EObject statement, final OpenSearchIndex target, final Code.Entries requestBody) {
    boolean _written = ModelUtil.written(statement, "explain");
    if (_written) {
      Code.Flag _flag = new Code.Flag(true);
      requestBody.put("explain", _flag);
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
    boolean _isEmpty = highlighted.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      final Code.Entries highlightFields = Code.entries();
      for (final Field f : highlighted) {
        {
          final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(f));
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
      requestBody.put("highlight", OpenSearchBodies.obj("fields", highlightFields));
    }
  }

  /**
   * A candidate request of the shared result-shape module: the fields the module reads and the vectors it compares or returns
   * (OpenSearchShapes), explain, highlight and the extension parameters; offset, cursor, sort, groups, bounds and totals are the
   * module's.
   */
  private Code.Entries candidateBody(final EObject statement, final OpenSearchIndex target, final Code.Entries requestBody, final ShapeSpec shapes) {
    Code.Entries _xblockexpression = null;
    {
      requestBody.put("_source", OpenSearchShapes.source(shapes, target));
      this.marks(statement, target, requestBody);
      this.extensionBody(statement, requestBody);
      _xblockexpression = requestBody;
    }
    return _xblockexpression;
  }

  /**
   * The collapse field of a grouped search: the exact form of the group-by field.
   */
  private String groupOf(final EObject statement, final OpenSearchIndex target) {
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
  private Code.Entries aggs(final OpenSearchIndex target, final List<AggregationClause> aggregationClauses) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      for (final AggregationClause clause : aggregationClauses) {
        {
          String _xifexpression = null;
          String _rawPath = clause.getField().getRawPath();
          boolean _tripleNotEquals = (_rawPath != null);
          if (_tripleNotEquals) {
            _xifexpression = OpenSearchBodies.field(clause.getField().getRawPath());
          } else {
            String _xblockexpression_1 = null;
            {
              final OpenSearchIndex.Field top = target.field(ModelUtil.fieldName(clause.getField().getField()));
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
          final Code.Text fieldName = OpenSearchBodies.text(path);
          Code.Num _xifexpression_1 = null;
          boolean _written = ModelUtil.written(clause, "bucketCount");
          if (_written) {
            _xifexpression_1 = OpenSearchBodies.num(clause.getBucketCount());
          } else {
            _xifexpression_1 = null;
          }
          final Code.Num size = _xifexpression_1;
          Code.Entries _switchResult = null;
          String _literal = clause.getKind().getLiteral();
          if (_literal != null) {
            switch (_literal) {
              case "count":
                _switchResult = OpenSearchBodies.obj("value_count", OpenSearchBodies.obj("field", fieldName));
                break;
              case "sum":
              case "avg":
              case "min":
              case "max":
              case "stats":
              case "percentiles":
              case "cardinality":
                _switchResult = OpenSearchBodies.obj(clause.getKind().getLiteral(), OpenSearchBodies.obj("field", fieldName));
                break;
              case "distinct_count":
                _switchResult = OpenSearchBodies.obj("cardinality", OpenSearchBodies.obj("field", fieldName));
                break;
              case "terms":
              case "facet":
                _switchResult = OpenSearchBodies.obj("terms", OpenSearchBodies.obj("field", fieldName, "size", size));
                break;
              case "histogram":
                Code.Entries _xblockexpression_2 = null;
                {
                  if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                    throw Unsupported.of("a histogram without a numeric interval");
                  }
                  _xblockexpression_2 = OpenSearchBodies.obj("histogram", OpenSearchBodies.obj("field", fieldName, "interval", OpenSearchBodies.num(Double.parseDouble(clause.getInterval()))));
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
                  _xblockexpression_3 = OpenSearchBodies.obj("date_histogram", OpenSearchBodies.obj("field", fieldName, "fixed_interval", OpenSearchBodies.text(clause.getInterval())));
                }
                _switchResult = _xblockexpression_3;
                break;
              default:
                throw Unsupported.of("range aggregations; vecDSL gives no ranges, which the OpenSearch range aggregation needs");
            }
          } else {
            throw Unsupported.of("range aggregations; vecDSL gives no ranges, which the OpenSearch range aggregation needs");
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
   * Search body parameters from an 'extension opensearch' block: terminate_after, min_score, track_scores, timeout.
   */
  private void extensionBody(final EObject host, final Code.Entries requestBody) {
    Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, OpenSearchGenerator.VENDOR).entrySet();
    for (final Map.Entry<String, Literal> entry : _entrySet) {
      {
        boolean _contains = OpenSearchStatements.SEARCH_KEYS.contains(entry.getKey());
        boolean _not = (!_contains);
        if (_not) {
          String _key = entry.getKey();
          String _plus = ("extension parameter " + _key);
          String _plus_1 = (_plus + "; OpenSearch search body parameters the generator passes are ");
          String _join = IterableExtensions.join(OpenSearchStatements.SEARCH_KEYS, ", ");
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
   * Rejects clauses OpenSearch lacks, with their reason, and clauses not generated yet.
   */
  private void rejectClauses(final EObject statement, final String... features) {
    for (final String featureName : features) {
      boolean _written = ModelUtil.written(statement, featureName);
      if (_written) {
        String _elvis = null;
        String _get = OpenSearchStatements.CLAUSES.get(featureName);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = featureName;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = OpenSearchStatements.PENDING_CLAUSES.get(featureName);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + OpenSearchStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = OpenSearchStatements.REASONS.get(featureName);
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
   * 'extension opensearch' blocks of queries carry search body parameters; extension items of other kinds are rejected.
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
      boolean _equals = Objects.equals(_vendor, OpenSearchGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on OpenSearch");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
  }

  /**
   * The query vector: a vector literal or parameter (bit vectors as hex strings), or for id input the stored vector (runtime).
   */
  private Code.Expr queryVectorOf(final QueryInput queryInput, final OpenSearchIndex.Field mapped) {
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
          throw Unsupported.of(("text input" + OpenSearchStatements.NO_MODELS));
        case MULTI_VECTOR:
          throw Unsupported.of("multi-vector input; a knn query compares one query vector with one knn_vector per document (or per nested document)");
        default:
          String _literal = queryInput.getKind().getLiteral();
          String _plus_1 = (_literal + " input");
          String _plus_2 = (_plus_1 + OpenSearchStatements.NO_MODELS);
          throw Unsupported.of(_plus_2);
      }
    } else {
      String _literal = queryInput.getKind().getLiteral();
      String _plus_1 = (_literal + " input");
      String _plus_2 = (_plus_1 + OpenSearchStatements.NO_MODELS);
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

  private Code.Expr recordsOf(final RecordSource recordSource, final OpenSearchIndex target) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (recordSource instanceof FileSource) {
      _matched=true;
      _switchResult = OpenSearchBodies.call("read_records", "location", OpenSearchBodies.text(((FileSource)recordSource).getLocation()));
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

  private Code.Entries recordOf(final RecordLiteral recordLiteral, final OpenSearchIndex target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = recordLiteral.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final OpenSearchIndex.Field mapped = target.field(ModelUtil.fieldName(entry.getField()));
          result.put(mapped.name(), this.convert(entry.getValue(), mapped));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A literal in the form OpenSearch reads for its field: sparse vectors as rank_features objects of token (the dimension
   * index as a string) to strictly positive weight, binary vectors as signed bytes (eight dimensions each, most significant bit
   * first), everything else as written.
   */
  private Code.Expr convert(final Literal literal, final OpenSearchIndex.Field mapped) {
    Code.Expr _xblockexpression = null;
    {
      OpenSearchIndex.Vector _vector = mapped.vector();
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
              String _plus_1 = (_plus + "; rank_features fields take strictly positive weights");
              throw Unsupported.of(_plus_1);
            }
            weights.put(String.valueOf(entry.getIndex()), OpenSearchBodies.num(entry.getWeight()));
          }
        }
        return weights;
      }
      String _element = mapped.vector().element();
      boolean _equals = Objects.equals(_element, "binary");
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
          final List<Code.Expr> packed = CollectionLiterals.<Code.Expr>newArrayList();
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
              Code.Num _num = OpenSearchBodies.num(_xifexpression);
              packed.add(_num);
              position = (position + 8);
            }
          }
          return Code.items(packed);
        }
        if ((literal instanceof ParamRef)) {
          String _name = mapped.name();
          String _plus = ("a parameter for binary vector field " + _name);
          String _plus_1 = (_plus + "; the generator packs binary vectors from literals into signed bytes");
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
