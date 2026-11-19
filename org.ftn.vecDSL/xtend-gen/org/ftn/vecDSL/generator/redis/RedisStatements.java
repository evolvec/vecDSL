package org.ftn.vecDSL.generator.redis;

import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
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
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Alter;
import org.ftn.vecDSL.vecDSL.AlterOperation;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.AuthConfig;
import org.ftn.vecDSL.vecDSL.AwaitIndex;
import org.ftn.vecDSL.vecDSL.BackupSchedule;
import org.ftn.vecDSL.vecDSL.BatchQuery;
import org.ftn.vecDSL.vecDSL.BulkImport;
import org.ftn.vecDSL.vecDSL.Clone;
import org.ftn.vecDSL.vecDSL.Cluster;
import org.ftn.vecDSL.vecDSL.ClusterLimit;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Compact;
import org.ftn.vecDSL.vecDSL.Concept;
import org.ftn.vecDSL.vecDSL.Connection;
import org.ftn.vecDSL.vecDSL.Count;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.DdlMethod;
import org.ftn.vecDSL.vecDSL.Delete;
import org.ftn.vecDSL.vecDSL.Describe;
import org.ftn.vecDSL.vecDSL.DistanceMatrix;
import org.ftn.vecDSL.vecDSL.DmlMethod;
import org.ftn.vecDSL.vecDSL.Drop;
import org.ftn.vecDSL.vecDSL.DropFieldOp;
import org.ftn.vecDSL.vecDSL.DropFunctionOp;
import org.ftn.vecDSL.vecDSL.DropSnapshot;
import org.ftn.vecDSL.vecDSL.EAuthType;
import org.ftn.vecDSL.vecDSL.ECloneMode;
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
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
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.SwapAlias;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.Transaction;
import org.ftn.vecDSL.vecDSL.Truncate;
import org.ftn.vecDSL.vecDSL.Update;
import org.ftn.vecDSL.vecDSL.Upsert;
import org.ftn.vecDSL.vecDSL.VecDSLFactory;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorDatabase;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps each vecDSL statement to Redis commands in the host-language-neutral code model. Command argument arrays (FT.CREATE,
 * FT.SEARCH, FT.HYBRID, FT.AGGREGATE, ACL SETUSER, ...) are built here, with values as PARAMS or separate arguments, so the three
 * host languages send identical commands; the runtimes send them, read the documents and shape the results.
 */
@SuppressWarnings("all")
public class RedisStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; RedisCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Redis: ";

  /**
   * Suffix of constructs Redis supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no Redis mapping yet";

  public static final String EXTERNAL = "collections derived from external sources; Redis holds the documents it is given and reads no external files or tables";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; Redis has no per-index or per-key drop guard (ACL rules restrict commands per user, not per index)";

  public static final String NO_STORAGE = "storage settings; Redis Open Source keeps data in memory and persists it with RDB and AOF files configured on the server";

  public static final String NO_TAGS = "collection tags; a Query Engine index carries no metadata besides its definition";

  public static final String NO_SNAPSHOTS = "snapshots; SAVE and BGSAVE write one RDB file of the whole server under a fixed name (dir and dbfilename are protected configuration), and a restore needs a server restart";

  public static final String SINGLE_SERVER = "the program targets one Redis Open Source server; replication (REPLICAOF) and Redis Cluster are configured on the servers, not by a client";

  private static final String NO_GENERATION = "Redis Open Source runs no generative models";

  private static final String NO_RERANKERS = "Redis Open Source runs no reranking models";

  private static final String NO_METRICS = "metrics configuration; Redis reports statistics through INFO and the server configuration, not through objects a client creates";

  private static final String NO_SCHEDULES = "backup schedules; RDB save points (the save configuration) snapshot the whole server under one file name, not named backups";

  private static final String NO_CLUSTER_LIMITS = "cluster limits; Redis has no setting that limits the number of indexes";

  private static final String NO_FILES = "file resources; the Query Engine reads no dictionary or model files";

  private static final String NO_RELEASE = "release; Redis keeps every document and index in memory and has no command that evicts one index";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why Redis cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Redis keeps only the current value of each key"), Pair.<String, String>of("reranker", RedisStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated Redis programs embed no query text" + RedisSchema.NO_MODELS)), Pair.<String, String>of("minShouldMatch", "the query syntax combines terms with intersection, union and optional (~) terms and has no minimum number of matching terms"), Pair.<String, String>of("elementScope", "a multi-value vector attribute scores a document by its nearest vector and does not report which element matched")));

  private static final String NO_TEXT = "the \'highlight\' clause on a search without text; HIGHLIGHT marks the terms of a text query";

  /**
   * Clauses Redis supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("cursor", "FT.AGGREGATE WITHCURSOR or a keyset condition after the last hit"), Pair.<String, String>of("explain", "EXPLAINSCORE of FT.SEARCH and FT.HYBRID"), Pair.<String, String>of("mmr", "MMR over the candidates in the runtime"), Pair.<String, String>of("highlighted", "HIGHLIGHT FIELDS of FT.SEARCH"), Pair.<String, String>of("sortClauses", "SORTBY over the hits"), Pair.<String, String>of("aggregations", "FT.AGGREGATE GROUPBY with REDUCE"), Pair.<String, String>of("prefetches", "staged KNN queries"), Pair.<String, String>of("groupBy", "FT.AGGREGATE GROUPBY with REDUCE TOLIST"), Pair.<String, String>of("boost", "FT.AGGREGATE APPLY expressions over the scores"), Pair.<String, String>of("withTotal", "total_results of FT.HYBRID or a count of the union of the legs"), Pair.<String, String>of("sliceIndex", "a hash of the key in an FT.AGGREGATE FILTER")));

  private static final Map<String, String> SEARCH_SETTINGS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("ef_runtime", "EF_RUNTIME"), Pair.<String, String>of("epsilon", "EPSILON"), Pair.<String, String>of("hybrid_policy", "HYBRID_POLICY"), Pair.<String, String>of("batch_size", "BATCH_SIZE"), Pair.<String, String>of("search_window_size", "SEARCH_WINDOW_SIZE"), Pair.<String, String>of("search_buffer_capacity", "SEARCH_BUFFER_CAPACITY"), Pair.<String, String>of("use_search_history", "USE_SEARCH_HISTORY")));

  private static final Map<String, List<String>> ACL_CATEGORIES = Collections.<String, List<String>>unmodifiableMap(CollectionLiterals.<String, List<String>>newHashMap(Pair.<String, List<String>>of("read", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@read"))), Pair.<String, List<String>>of("write", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@write"))), Pair.<String, List<String>>of("admin", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@all"))), Pair.<String, List<String>>of("all", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@all"))), Pair.<String, List<String>>of("select", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@read"))), Pair.<String, List<String>>of("insert", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@write"))), Pair.<String, List<String>>of("update", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@write"))), Pair.<String, List<String>>of("delete", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@write"))), Pair.<String, List<String>>of("search", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("+@search")))));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final RedisSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public RedisStatements(final Script script) {
    this.script = script;
    RedisSchema _redisSchema = new RedisSchema(this.notes, script);
    this.schema = _redisSchema;
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
      final List<Code.Stmt> body = _xtrycatchfinallyexpression;
      final List<Program.Method> result = CollectionLiterals.<Program.Method>newArrayList();
      List<String> _parameterNames = ModelUtil.parameterNames(statement);
      List<String> _list = this.notes.stream().distinct().toList();
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
      Program.Method _method = new Program.Method(name, _parameterNames, body, _list, _newArrayList, false);
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
          _switchResult = "the Query Engine indexes TEXT attributes for BM25 itself and has no function that produces sparse vectors";
          break;
        case "custom":
          _switchResult = "custom functions; Redis computes no derived fields when documents are written";
          break;
        default:
          _switchResult = ("Redis Open Source runs no model functions" + RedisSchema.NO_MODELS);
          break;
      }
    } else {
      _switchResult = ("Redis Open Source runs no model functions" + RedisSchema.NO_MODELS);
    }
    return _switchResult;
  }

  public static String collectionLimit(final String type) {
    return (("collection limits (" + type) + "); the Query Engine has fixed limits (1024 attributes per index, 128 of them TEXT) and no per-index limit settings");
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
        List<Code.Stmt> _createCollection = this.schema.createCollection(((VectorCollection)concept), ifNotExists, fresh);
        Iterables.<Code.Stmt>addAll(result, _createCollection);
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
            throw Unsupported.of(RedisStatements.NO_METRICS);
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
            throw Unsupported.of(RedisStatements.NO_CLUSTER_LIMITS);
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + RedisStatements.SINGLE_SERVER));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, RedisGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          RedisIndex.id(((Database)concept).getName());
          EList<VectorCollection> _collections = ((Database)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          final RedisIndex.Index index = this.schema.index(((Alias)concept).getCollection());
          String _id = RedisIndex.id(((Alias)concept).getName());
          String _name = index.name();
          List<String> _xifexpression = null;
          if (ifNotExists) {
            _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("alias already exists"));
          } else {
            _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
          }
          Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASADD", _id, _name)), _xifexpression);
          Code.Run _run = new Code.Run(_command);
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          List<Code.Stmt> _createVectorIndex = this.createVectorIndex(((VectorIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _createVectorIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          VectorCollection _elvis = null;
          VectorCollection _targetCollection = ModelUtil.targetCollection(concept);
          if (_targetCollection != null) {
            _elvis = _targetCollection;
          } else {
            VectorCollection _xifexpression = null;
            Field _field = ((PayloadIndex)concept).getTarget().getField();
            boolean _tripleEquals = (_field == null);
            if (_tripleEquals) {
              _xifexpression = null;
            } else {
              _xifexpression = ModelUtil.owningCollection(((PayloadIndex)concept).getTarget().getField());
            }
            _elvis = _xifexpression;
          }
          final VectorCollection collection = _elvis;
          if ((collection == null)) {
            throw Unsupported.of("a payload index outside a collection");
          }
          this.schema.checkPayloadIndex(collection, ((PayloadIndex)concept));
          List<Code.Stmt> _require = this.require(collection);
          Iterables.<Code.Stmt>addAll(result, _require);
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
          List<Code.Stmt> _require = this.require(this.member(concept, "a partition"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _require = this.require(this.member(concept, "a tenant"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _require = this.require(this.member(concept, "a namespace"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = RedisStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          RedisSchema.checkAnalyzer(((Analyzer)concept));
          List<Code.Stmt> _require = this.require(this.member(concept, "an analyzer"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer == null)) {
            throw Unsupported.of("an analyzer filter outside an analyzer");
          }
          RedisSchema.checkFilter(((AnalyzerFilter)concept).getType().getLiteral(), analyzer);
          List<Code.Stmt> _require = this.require(this.member(analyzer, "an analyzer"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("field limits; Redis does not validate document values (JSON documents have no constraints)");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + RedisSchema.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + RedisStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + RedisStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization outside a vector index; the Query Engine compresses vectors through the COMPRESSION option of an SVS-VAMANA index");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + RedisStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + RedisStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          List<Code.Stmt> _authentication = this.authentication(((AuthConfig)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _authentication);
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          List<Code.Stmt> _role = this.role(((RoleBinding)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _role);
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(RedisStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (RedisStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The program endpoint: checked here, opened by connect(). User and password come from the environment only.
   */
  private void connection(final Connection concept) {
    if ((((concept.getLocalPath() != null) || concept.isInMemory()) || Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED))) {
      throw Unsupported.of("embedded deployments; the Redis clients connect to a server");
    }
    if (((concept.getApiKey() != null) || (concept.getToken() != null))) {
      throw Unsupported.of("api keys and tokens; Redis authenticates ACL users with passwords (AUTH)");
    }
    Literal _password = concept.getPassword();
    if ((_password instanceof StringLit)) {
      throw Unsupported.of("a password written in the script; read it from an environment variable with env(...)");
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a database name on the connection; the Query Engine indexes database 0 only, and a vecDSL logical-db is a name prefix");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; Redis speaks RESP on one port");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, RedisGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    this.schema.note(concept, "timeoutMillis", "cloud", "region", "project");
  }

  /**
   * Password authentication is the connection's; roles are ACL users; other methods are not in Redis Open Source.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((!Objects.equals(auth.getType(), EAuthType.NONE)) && (!Objects.equals(auth.getType(), EAuthType.USERNAME_PASSWORD)))) {
        EAuthType _type = auth.getType();
        boolean _equals = Objects.equals(_type, EAuthType.MTLS);
        if (_equals) {
          throw Unsupported.of("mtls authentication; has no Redis mapping yet (rediss:// with client certificates, tls-auth-clients on the server)");
        }
        String _literal = auth.getType().getLiteral();
        String _plus = (_literal + " authentication; Redis Open Source authenticates ACL users with passwords (token and single sign-on methods are Redis Cloud features)");
        throw Unsupported.of(_plus);
      }
      Literal _token = auth.getToken();
      if ((_token instanceof StringLit)) {
        throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
      }
      String _certificatePath = auth.getCertificatePath();
      boolean _tripleNotEquals = (_certificatePath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("client certificates; has no Redis mapping yet (rediss:// with client certificates)");
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; Redis Open Source has no encryption at rest");
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        throw Unsupported.of("audit logs; Redis Open Source logs no access audit trail (ACL LOG records denied commands only)");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = auth.getRoles();
      for (final RoleBinding binding : _roles) {
        List<Code.Stmt> _role = this.role(binding, ifNotExists);
        Iterables.<Code.Stmt>addAll(result, _role);
      }
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("ACL", "WHOAMI")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
        Code.Run _run = new Code.Run(_command);
        result.add(_run);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * An ACL user that cannot log in (off), with command categories and the key pattern of a collection, or all keys.
   */
  private List<Code.Stmt> role(final RoleBinding binding, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = RedisIndex.id(binding.getRoleName());
      final List<String> rules = CollectionLiterals.<String>newArrayList("off", "resetkeys", "resetchannels", "-@all");
      EList<String> _privileges = binding.getPrivileges();
      for (final String privilege : _privileges) {
        {
          final List<String> categories = RedisStatements.ACL_CATEGORIES.get(privilege.toLowerCase());
          if ((categories == null)) {
            throw Unsupported.of((("the privilege " + privilege) + "; the generator grants the ACL categories read, write, search and all"));
          }
          Iterables.<String>addAll(rules, categories);
        }
      }
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        rules.add("~*");
      } else {
        String _prefix = this.schema.index(binding.getCollection()).prefix();
        String _plus = ("~" + _prefix);
        String _plus_1 = (_plus + "*");
        rules.add(_plus_1);
      }
      final List<String> args = CollectionLiterals.<String>newArrayList("ACL", "SETUSER", name);
      Iterables.<String>addAll(args, rules);
      Code.Call _command = RedisSchema.command(args, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      final Code.Run setUser = new Code.Run(_command);
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(setUser));
      }
      Code.Call _runtime = Code.runtime("exists", RedisSchema.kw("args", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("ACL", "GETUSER", name)))));
      Code.When _when = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(setUser)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
    }
    return _xblockexpression;
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
    throw Unsupported.of((what + " outside a collection; the Query Engine adds it to an index"));
  }

  /**
   * Existence check of a collection: FT.INFO fails for a missing index.
   */
  private List<Code.Stmt> require(final VectorCollection collection) {
    String _name = this.schema.index(collection).name();
    Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.INFO", _name)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
    Code.Run _run = new Code.Run(_command);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  /**
   * A vector index created on an existing collection: the index is created again with the new algorithm.
   */
  private List<Code.Stmt> createVectorIndex(final VectorIndex vectorIndex) {
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
      final VectorField field = _elvis;
      if ((field == null)) {
        throw Unsupported.of("a vector index without a vector field");
      }
      final VectorCollection collection = this.member(field, "a vector index");
      final RedisIndex.Index before = this.schema.index(collection);
      final String name = this.schema.name(collection, field);
      final String type = before.vector(name).vec().type();
      this.schema.note(vectorIndex, "online");
      RedisIndex.Vec _vec = this.schema.vec(field, vectorIndex, type);
      final RedisIndex.Attr attr = new RedisIndex.Attr(name, ("$." + name), "VECTOR", _vec, 1.0, false);
      String _database = before.database();
      String _collection = before.collection();
      String _key = before.key();
      boolean _numericKey = before.numericKey();
      final Function1<RedisIndex.Attr, RedisIndex.Attr> _function = (RedisIndex.Attr a) -> {
        RedisIndex.Attr _xifexpression = null;
        String _name = a.name();
        boolean _equals = Objects.equals(_name, name);
        if (_equals) {
          _xifexpression = attr;
        } else {
          _xifexpression = a;
        }
        return _xifexpression;
      };
      List<RedisIndex.Attr> _list = IterableExtensions.<RedisIndex.Attr>toList(ListExtensions.<RedisIndex.Attr, RedisIndex.Attr>map(before.attrs(), _function));
      List<String> _fields = before.fields();
      Map<String, Integer> _vectors = before.vectors();
      String _tenant = before.tenant();
      String _namespace = before.namespace();
      String _partition = before.partition();
      String _language = before.language();
      List<String> _stopwords = before.stopwords();
      Integer _ttl = before.ttl();
      final RedisIndex.Index after = new RedisIndex.Index(_database, _collection, _key, _numericKey, _list, _fields, _vectors, _tenant, _namespace, _partition, _language, _stopwords, _ttl);
      this.schema.evolve(collection, after);
      Code.Call _runtime = Code.runtime("recreate", RedisSchema.kw("index", Code.text(after.name()), "create", Code.texts(RedisIndex.definition(after))));
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * FT.ALTER SCHEMA ADD; existing documents with the field are indexed in the background.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final RedisIndex.Index before = this.schema.index(collection);
      final List<RedisIndex.Attr> added = this.schema.attributes(collection, field);
      Integer _xifexpression = null;
      if ((field instanceof VectorField)) {
        _xifexpression = Integer.valueOf(((VectorField) field).getDimension());
      } else {
        _xifexpression = null;
      }
      final Integer dim = _xifexpression;
      this.schema.evolve(collection, before.with(added, this.schema.name(collection, field), dim));
      boolean _isEmpty = added.isEmpty();
      if (_isEmpty) {
        return this.require(collection);
      }
      Code.Call _command = RedisSchema.command(RedisIndex.alter(before, added), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        if (((!it.getOperations().isEmpty()) || (it.getNewName() == null))) {
          throw Unsupported.of("alter operations on an alias; an alias can be renamed");
        }
        final Alias alias = ((Alias) concept);
        final RedisIndex.Index index = this.schema.index(alias.getCollection());
        String _id = RedisIndex.id(it.getNewName());
        String _name = index.name();
        Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASADD", _id, _name)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
        Code.Run _run = new Code.Run(_command);
        String _id_1 = RedisIndex.id(alias.getName());
        Code.Call _command_1 = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASDEL", _id_1)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
        Code.Run _run_1 = new Code.Run(_command_1);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name_1 = concept.eClass().getName();
        String _plus = ("altering a " + _name_1);
        String _plus_1 = (_plus + "; the generator alters collections and aliases");
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
        final RedisIndex.Index before = this.schema.index(collection);
        final RedisIndex.Index after = before.named(RedisIndex.id(it.getNewName()));
        Code.Call _runtime = Code.runtime("copy_collection", RedisSchema.kw("prefix", Code.text(before.prefix()), "target", Code.text(after.prefix()), "create", 
          Code.texts(RedisIndex.definition(after)), "move", Code.flag(true), "drop", Code.text(before.name())));
        Code.Run _run_2 = new Code.Run(_runtime);
        result.add(_run_2);
        this.schema.evolve(collection, after);
      }
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        List<Code.Stmt> _require = this.require(collection);
        Iterables.<Code.Stmt>addAll(result, _require);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final RedisIndex.Index before = this.schema.index(collection);
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (operation instanceof AddFieldOp) {
        _matched=true;
        _switchResult = this.addField(collection, ((AddFieldOp)operation).getNewField());
      }
      if (!_matched) {
        if (operation instanceof DropFieldOp) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final String name = this.schema.name(collection, ((DropFieldOp)operation).getField());
            String _key = before.key();
            boolean _equals = Objects.equals(name, _key);
            if (_equals) {
              throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
            }
            final RedisIndex.Index after = before.without(name);
            this.schema.evolve(collection, after);
            Code.Call _runtime = Code.runtime("rewrite", RedisSchema.kw("prefix", Code.text(before.prefix()), "delete", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name)))));
            Code.Run _run = new Code.Run(_runtime);
            Code.Call _runtime_1 = Code.runtime("recreate", RedisSchema.kw("index", Code.text(after.name()), "create", Code.texts(RedisIndex.definition(after))));
            Code.Run _run_1 = new Code.Run(_runtime_1);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof RenameFieldOp) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final String name = this.schema.name(collection, ((RenameFieldOp)operation).getField());
            final String newName = RedisIndex.id(((RenameFieldOp)operation).getNewName());
            String _key = before.key();
            boolean _equals = Objects.equals(name, _key);
            if (_equals) {
              throw Unsupported.of("renaming the primary key; the generator addresses documents by the key field");
            }
            final RedisIndex.Index after = before.renamed(name, newName);
            this.schema.evolve(collection, after);
            this.schema.rename(collection, name, newName);
            final Code.Entries renamed = Code.entries();
            renamed.put(name, Code.text(newName));
            Code.Call _runtime = Code.runtime("rewrite", RedisSchema.kw("prefix", Code.text(before.prefix()), "rename", renamed));
            Code.Run _run = new Code.Run(_runtime);
            Code.Call _runtime_1 = Code.runtime("recreate", RedisSchema.kw("index", Code.text(after.name()), "create", Code.texts(RedisIndex.definition(after))));
            Code.Run _run_1 = new Code.Run(_runtime_1);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof SetPropertyOp) {
          _matched=true;
          _switchResult = this.setProperty(collection, before, ((SetPropertyOp)operation));
        }
      }
      if (!_matched) {
        if (operation instanceof AddAliasOp) {
          _matched=true;
          String _id = RedisIndex.id(((AddAliasOp)operation).getNewAlias().getName());
          String _name = before.name();
          Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASADD", _id, _name)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
          Code.Run _run = new Code.Run(_command);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = RedisStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = RedisStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (RedisStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * ttl: EXPIRE on the existing keys, and later writes of the program set it; consistency and dynamic hold already.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final RedisIndex.Index before, final SetPropertyOp operation) {
    List<Code.Stmt> _switchResult = null;
    ECollectionProperty _property = operation.getProperty();
    if (_property != null) {
      switch (_property) {
        case TTL:
          List<Code.Stmt> _xblockexpression = null;
          {
            Literal _value = operation.getValue();
            boolean _not = (!(_value instanceof NumberLit));
            if (_not) {
              throw Unsupported.of("a ttl that is not a number literal");
            }
            Literal _value_1 = operation.getValue();
            double _value_2 = ((NumberLit) _value_1).getValue();
            final int seconds = ((int) _value_2);
            this.schema.evolve(collection, before.expiring(Integer.valueOf(seconds)));
            Code.Call _runtime = Code.runtime("expire", RedisSchema.kw("prefix", Code.text(before.prefix()), "seconds", Code.num(seconds)));
            Code.Run _run = new Code.Run(_runtime);
            _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          _switchResult = _xblockexpression;
          break;
        case DESCRIPTION:
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            this.notes.add("description on SetPropertyOp, which a Query Engine index does not store");
            _xblockexpression_1 = this.require(collection);
          }
          _switchResult = _xblockexpression_1;
          break;
        case CONSISTENCY:
        case DYNAMIC:
          _switchResult = this.require(collection);
          break;
        case TAGS:
          throw Unsupported.of(RedisStatements.NO_TAGS);
        case DELETION_PROTECTION:
          throw Unsupported.of(RedisStatements.NO_DELETION_PROTECTION);
        default:
          break;
      }
    }
    return _switchResult;
  }

  protected List<Code.Stmt> _body(final Drop it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (concept instanceof VectorCollection) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          final RedisIndex.Index index = this.schema.index(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          Code.Call _runtime = Code.runtime("drop_collection", RedisSchema.kw("index", Code.text(index.name()), "prefix", Code.text(index.prefix()), "if_exists", Code.flag(it.isIfExists())));
          Code.Run _run = new Code.Run(_runtime);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            RedisIndex.id(((Database)concept).getName());
            final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
            EList<VectorCollection> _collections = ((Database)concept).getCollections();
            for (final VectorCollection collection : _collections) {
              {
                final RedisIndex.Index index = this.schema.index(collection);
                Code.Call _runtime = Code.runtime("drop_collection", RedisSchema.kw("index", Code.text(index.name()), "prefix", Code.text(index.prefix()), "if_exists", Code.flag(it.isIfExists())));
                Code.Run _run = new Code.Run(_runtime);
                result.add(_run);
              }
            }
            boolean _isEmpty = result.isEmpty();
            if (_isEmpty) {
              throw Unsupported.of("dropping a logical-db without collections; a logical-db is a name prefix of its collections");
            }
            _xblockexpression_1 = result;
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), this.schema.index(this.member(concept, "a tenant")).tenant(), ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), this.schema.index(this.member(concept, "a namespace")).namespace(), ((Namespace)concept).getKey());
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a partition"), this.schema.index(this.member(concept, "a partition")).partition(), ((Partition)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          _switchResult = this.alter(this.member(concept, "a field"), this.dropOp(((Field)concept)));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          _switchResult = this.alter(this.member(concept, "a field"), this.dropOp(((Field)concept)));
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          String _id = RedisIndex.id(((Alias)concept).getName());
          List<String> _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("alias does not exist", "unknown index name"));
          } else {
            _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
          }
          Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASDEL", _id)), _xifexpression);
          Code.Run _run = new Code.Run(_command);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          _switchResult = this.dropVectorIndex(((VectorIndex)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of("dropping a payload index; every declared field is an attribute of the collection\'s index, which FT.ALTER cannot remove (drop the field)");
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = RedisStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; the compression belongs to its SVS-VAMANA index (drop the index)");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Redis object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; the program drops indexes and keys, not the server");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + RedisStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; the language and stop words belong to the index (drop the collection)");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; the index applies the filters of its language");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("field limits; Redis does not validate document values (JSON documents have no constraints)");
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + RedisSchema.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + RedisStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + RedisStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + RedisStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + RedisStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; password authentication is server configuration (requirepass and ACL files)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final String name = RedisIndex.id(((RoleBinding)concept).getRoleName());
            Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("ACL", "DELUSER", name)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
            Code.Run _run = new Code.Run(_command);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(RedisStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(RedisStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((RedisStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private DropFieldOp dropOp(final Field field) {
    DropFieldOp _xblockexpression = null;
    {
      final DropFieldOp operation = VecDSLFactory.eINSTANCE.createDropFieldOp();
      operation.setField(field);
      _xblockexpression = operation;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant, namespace or partition deletes its documents.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String attribute, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final RedisQuery.Params bound = new RedisQuery.Params();
      Code.Text _text = Code.text(value);
      final String where = RedisQuery.where(new RedisFilter(this.schema, collection).slice(attribute, Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text))).parts(), bound);
      Code.Call _runtime = Code.runtime("delete", RedisSchema.kw("prefix", Code.text(index.prefix()), "search", RedisQuery.search(RedisQuery.keys(index, where, null), bound, index)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Dropping a vector index leaves the field searchable exactly (FLAT): the index is created again.
   */
  private List<Code.Stmt> dropVectorIndex(final VectorIndex vectorIndex) {
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
      final VectorField field = _elvis;
      if ((field == null)) {
        throw Unsupported.of("a vector index without a vector field");
      }
      final VectorCollection collection = this.member(field, "a vector index");
      final RedisIndex.Index before = this.schema.index(collection);
      final String name = this.schema.name(collection, field);
      final RedisIndex.Vec old = before.vector(name).vec();
      String _type = old.type();
      int _dim = old.dim();
      String _metric = old.metric();
      LinkedHashMap<String, String> _newLinkedHashMap = CollectionLiterals.<String, String>newLinkedHashMap();
      final RedisIndex.Vec flat = new RedisIndex.Vec("FLAT", _type, _dim, _metric, _newLinkedHashMap);
      String _database = before.database();
      String _collection = before.collection();
      String _key = before.key();
      boolean _numericKey = before.numericKey();
      final Function1<RedisIndex.Attr, RedisIndex.Attr> _function = (RedisIndex.Attr a) -> {
        RedisIndex.Attr _xifexpression = null;
        String _name = a.name();
        boolean _equals = Objects.equals(_name, name);
        if (_equals) {
          String _path = a.path();
          _xifexpression = new RedisIndex.Attr(name, _path, "VECTOR", flat, 1.0, false);
        } else {
          _xifexpression = a;
        }
        return _xifexpression;
      };
      List<RedisIndex.Attr> _list = IterableExtensions.<RedisIndex.Attr>toList(ListExtensions.<RedisIndex.Attr, RedisIndex.Attr>map(before.attrs(), _function));
      List<String> _fields = before.fields();
      Map<String, Integer> _vectors = before.vectors();
      String _tenant = before.tenant();
      String _namespace = before.namespace();
      String _partition = before.partition();
      String _language = before.language();
      List<String> _stopwords = before.stopwords();
      Integer _ttl = before.ttl();
      final RedisIndex.Index after = new RedisIndex.Index(_database, _collection, _key, _numericKey, _list, _fields, _vectors, _tenant, _namespace, _partition, _language, _stopwords, _ttl);
      this.schema.evolve(collection, after);
      Code.Call _runtime = Code.runtime("recreate", RedisSchema.kw("index", Code.text(after.name()), "create", Code.texts(RedisIndex.definition(after))));
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * An alias swap points the alias at the new collection's index (FT.ALIASUPDATE moves it atomically).
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(it.getNewCollection());
      String _id = RedisIndex.id(it.getAlias().getName());
      String _name = index.name();
      Code.Call _command = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASUPDATE", _id, _name)), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      final String policy = _xifexpression;
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      String _xifexpression_1 = null;
      boolean _equals = Objects.equals(policy, "update");
      if (_equals) {
        _xifexpression_1 = "replace";
      } else {
        _xifexpression_1 = policy;
      }
      String _xifexpression_2 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_2 = "insert_count";
      } else {
        _xifexpression_2 = "upsert_count";
      }
      Integer _xifexpression_3 = null;
      boolean _written_1 = ModelUtil.written(it, "ttlSeconds");
      if (_written_1) {
        _xifexpression_3 = Integer.valueOf(it.getTtlSeconds());
      } else {
        _xifexpression_3 = null;
      }
      Code.Call _runtime = Code.runtime("write", this.writeArgs(it, _collection, _source, _xifexpression_1, _xifexpression_2, _xifexpression_3));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getTtlSeconds());
      } else {
        _xifexpression = null;
      }
      final Code.Entries args = this.writeArgs(it, _collection, _source, "replace", "upsert_count", _xifexpression);
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      Code.Call _runtime = Code.runtime("write", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The arguments of a write: prefix, key field, records, conflict policy, counter, vector dimensions, slices, ttl and wait.
   */
  private Code.Entries writeArgs(final EObject statement, final VectorCollection collection, final RecordSource source, final String policy, final String counter, final Integer ttl) {
    Code.Entries _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()), "key", Code.text(index.key()), "records", this.rows(source, collection), "policy", Code.text(policy), 
        "counter", Code.text(counter));
      final Code.Entries dims = Code.entries();
      final BiConsumer<String, Integer> _function = (String name, Integer dim) -> {
        dims.put(name, Code.num((dim).intValue()));
      };
      index.vectors().forEach(_function);
      boolean _isEmpty = index.vectors().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", dims);
      }
      final Code.Entries slices = this.slices(statement, collection);
      boolean _isEmpty_1 = slices.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("slices", slices);
      }
      Integer _elvis = null;
      if (ttl != null) {
        _elvis = ttl;
      } else {
        Integer _ttl = index.ttl();
        _elvis = _ttl;
      }
      final Integer seconds = _elvis;
      if ((seconds != null)) {
        args.put("ttl", Code.num((seconds).intValue()));
      }
      boolean _wait = this.wait(statement);
      if (_wait) {
        args.put("wait", Code.flag(true));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private boolean wait(final EObject statement) {
    return (ModelUtil.written(statement, "waitForPersistence") && Objects.equals(this.feature(statement, "waitForPersistence"), Boolean.TRUE));
  }

  /**
   * Hidden tag values for tenant, namespace and partition of a write; partitioned collections default to the default partition.
   */
  private Code.Entries slices(final EObject statement, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final Code.Entries result = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.put(index.tenant(), Code.text(tenant.getName()));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        result.put(index.namespace(), Code.text(namespace));
      }
      Object _feature_2 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_2);
      if (((partition != null) && (index.partition() == null))) {
        throw Unsupported.of("a partition of a collection declared without partitions");
      }
      String _partition = index.partition();
      boolean _tripleNotEquals = (_partition != null);
      if (_tripleNotEquals) {
        String _elvis = null;
        String _elvis_1 = null;
        String _elvis_2 = null;
        String _name = null;
        if (partition!=null) {
          _name=partition.getName();
        }
        if (_name != null) {
          _elvis_2 = _name;
        } else {
          Partition _defaultPartition = collection.getDefaultPartition();
          String _name_1 = null;
          if (_defaultPartition!=null) {
            _name_1=_defaultPartition.getName();
          }
          _elvis_2 = _name_1;
        }
        if (_elvis_2 != null) {
          _elvis_1 = _elvis_2;
        } else {
          final Function1<Partition, Boolean> _function = (Partition it) -> {
            return Boolean.valueOf(it.isIsDefault());
          };
          Partition _findFirst = IterableExtensions.<Partition>findFirst(ModelUtil.partitions(collection), _function);
          String _name_2 = null;
          if (_findFirst!=null) {
            _name_2=_findFirst.getName();
          }
          _elvis_1 = _name_2;
        }
        if (_elvis_1 != null) {
          _elvis = _elvis_1;
        } else {
          _elvis = RedisIndex.DEFAULT_PARTITION;
        }
        result.put(index.partition(), Code.text(_elvis));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        throw Unsupported.of("if-version; Redis keys carry no version number, and the generator adds no version field (WATCH protects a write only within one client\'s read-modify-write)");
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()), "key", Code.text(index.key()));
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if (((((it.getValues() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty()))) {
          throw Unsupported.of("update from records with \'set\', remove-fields, array-append or array-remove; each record carries its own values");
        }
        args.put("records", this.rows(it.getSource(), it.getCollection()));
      } else {
        final Code.Entries set = Code.entries();
        final Code.Entries append = Code.entries();
        final Code.Entries remove = Code.entries();
        RecordLiteral _values = it.getValues();
        boolean _tripleNotEquals_1 = (_values != null);
        if (_tripleNotEquals_1) {
          EList<RecordEntry> _entries = it.getValues().getEntries();
          for (final RecordEntry entry : _entries) {
            {
              final String name = this.schema.name(it.getCollection(), entry.getField());
              String _key = index.key();
              boolean _equals = Objects.equals(name, _key);
              if (_equals) {
                throw Unsupported.of("updating the primary key; the key names the document");
              }
              boolean _contains = it.getAppendFields().contains(entry.getField());
              if (_contains) {
                this.arrayField(it.getCollection(), entry.getField());
                append.put(name, this.listOf(entry.getValue()));
              } else {
                boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
                if (_contains_1) {
                  this.arrayField(it.getCollection(), entry.getField());
                  remove.put(name, this.listOf(entry.getValue()));
                } else {
                  set.put(name, Literals.of(entry.getValue()));
                }
              }
            }
          }
        }
        EList<Field> _appendFields = it.getAppendFields();
        EList<Field> _arrayRemoveFields = it.getArrayRemoveFields();
        Iterable<Field> _plus = Iterables.<Field>concat(_appendFields, _arrayRemoveFields);
        for (final Field field : _plus) {
          if (((!append.has(this.schema.name(it.getCollection(), field))) && (!remove.has(this.schema.name(it.getCollection(), field))))) {
            String _name = this.schema.name(it.getCollection(), field);
            String _plus_1 = ("array-append or array-remove on " + _name);
            String _plus_2 = (_plus_1 + " without items in \'set\'");
            throw Unsupported.of(_plus_2);
          }
        }
        final List<String> removed = CollectionLiterals.<String>newArrayList();
        EList<Field> _removedFields = it.getRemovedFields();
        for (final Field field_1 : _removedFields) {
          {
            final String name = this.schema.name(it.getCollection(), field_1);
            String _key = index.key();
            boolean _equals = Objects.equals(name, _key);
            if (_equals) {
              throw Unsupported.of("removing the primary key");
            }
            removed.add(name);
          }
        }
        if ((((set.entries().isEmpty() && append.entries().isEmpty()) && remove.entries().isEmpty()) && removed.isEmpty())) {
          throw Unsupported.of("an update without \'set\' or \'remove-fields\'");
        }
        boolean _isEmpty = set.entries().isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          args.put("set", set);
        }
        boolean _isEmpty_1 = append.entries().isEmpty();
        boolean _not_1 = (!_isEmpty_1);
        if (_not_1) {
          args.put("append", append);
        }
        boolean _isEmpty_2 = remove.entries().isEmpty();
        boolean _not_2 = (!_isEmpty_2);
        if (_not_2) {
          args.put("remove", remove);
        }
        boolean _isEmpty_3 = removed.isEmpty();
        boolean _not_3 = (!_isEmpty_3);
        if (_not_3) {
          args.put("delete", Code.texts(removed));
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        args.put("replace", this.replaceSpec(it.getCollection()));
      }
      final RedisFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty_4 = it.getIds().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        boolean _isEmpty_5 = restriction.parts().isEmpty();
        boolean _not_5 = (!_isEmpty_5);
        if (_not_5) {
          throw Unsupported.of("an update by ids with a filter; has no Redis mapping yet (the filter checked on the documents of the ids)");
        }
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      } else {
        boolean _isEmpty_6 = restriction.parts().isEmpty();
        boolean _not_6 = (!_isEmpty_6);
        if (_not_6) {
          final RedisQuery.Params bound = new RedisQuery.Params();
          args.put("search", RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts(), bound), null), bound, index));
        } else {
          RecordSource _source_1 = it.getSource();
          boolean _tripleEquals = (_source_1 == null);
          if (_tripleEquals) {
            throw Unsupported.of("an update without ids or a filter");
          }
        }
      }
      boolean _wait = this.wait(it);
      if (_wait) {
        args.put("wait", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("update", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Mode replace keeps the key, the vectors and the slices and removes every other field the update does not set.
   */
  private Code.Entries replaceSpec(final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final List<String> keep = CollectionLiterals.<String>newArrayList(index.key());
      Set<String> _keySet = index.vectors().keySet();
      Iterables.<String>addAll(keep, _keySet);
      String _tenant = index.tenant();
      String _namespace = index.namespace();
      String _partition = index.partition();
      Iterable<String> _filterNull = IterableExtensions.<String>filterNull(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_tenant, _namespace, _partition)));
      Iterables.<String>addAll(keep, _filterNull);
      _xblockexpression = RedisSchema.kw("keep", Code.texts(keep));
    }
    return _xblockexpression;
  }

  private Code.Expr listOf(final Literal value) {
    Code.Expr _xblockexpression = null;
    {
      final Code.Expr converted = Literals.of(value);
      Code.Expr _xifexpression = null;
      if (((converted instanceof Code.Items) || (value instanceof ParamRef))) {
        _xifexpression = converted;
      } else {
        _xifexpression = Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(converted)));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void arrayField(final VectorCollection collection, final Field field) {
    if (((!(field instanceof PayloadField)) || (!Objects.equals(((PayloadField) field).getType().getLiteral(), "array")))) {
      String _name = this.schema.name(collection, field);
      String _plus = ("array-append or array-remove on " + _name);
      String _plus_1 = (_plus + ", which is not an array field");
      throw Unsupported.of(_plus_1);
    }
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()));
      final RedisFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        if (((!restriction.parts().isEmpty()) || ModelUtil.written(it, "deleteLimit"))) {
          throw Unsupported.of("a delete by ids with a filter or limit; has no Redis mapping yet (the filter checked on the documents of the ids)");
        }
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      } else {
        if (((!restriction.parts().isEmpty()) || ModelUtil.written(it, "deleteLimit"))) {
          final RedisQuery.Params bound = new RedisQuery.Params();
          args.put("search", RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts(), bound), null), bound, index));
          boolean _written = ModelUtil.written(it, "deleteLimit");
          if (_written) {
            args.put("limit", Code.num(it.getDeleteLimit()));
          }
        } else {
          boolean _isDeleteAll = it.isDeleteAll();
          boolean _not_1 = (!_isDeleteAll);
          if (_not_1) {
            throw Unsupported.of("a delete without ids, filter or \'all\'");
          }
        }
      }
      boolean _wait = this.wait(it);
      if (_wait) {
        args.put("wait", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("delete", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A local JSON or JSON Lines file written in atomic batches.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Redis has no ingestion pipeline (clients write the documents)");
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
        throw Unsupported.of((("import from " + scheme) + "; Redis reads no object stores (clients write the documents)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + RedisStatements.PENDING) + " (CSV rows typed by the collection fields)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the runtime reads JSON and JSON Lines files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async");
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final String policy = _xifexpression_1;
      String _xifexpression_2 = null;
      boolean _equals_1 = Objects.equals(policy, "update");
      if (_equals_1) {
        _xifexpression_2 = "replace";
      } else {
        _xifexpression_2 = policy;
      }
      String _xifexpression_3 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_3 = "insert_count";
      } else {
        _xifexpression_3 = "upsert_count";
      }
      final Code.Entries args = RedisSchema.kw("location", Code.text(it.getLocation()), "prefix", Code.text(index.prefix()), "key", Code.text(index.key()), 
        "policy", Code.text(_xifexpression_2), "counter", Code.text(_xifexpression_3));
      final Code.Entries dims = Code.entries();
      final BiConsumer<String, Integer> _function = (String name, Integer dim) -> {
        dims.put(name, Code.num((dim).intValue()));
      };
      index.vectors().forEach(_function);
      boolean _isEmpty = index.vectors().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", dims);
      }
      final Code.Entries slices = this.slices(it, it.getCollection());
      boolean _isEmpty_1 = slices.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("slices", slices);
      }
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The writes of a vecDSL transaction in one MULTI/EXEC under WATCH: every step by key, each seeing the documents the steps before
   * it wrote. Redis runs the queued commands without interleaving and has no rollback; the runtime commits only after every step
   * was planned, so a failing step writes nothing.
   */
  protected List<Code.Stmt> _body(final Transaction it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      boolean wait = false;
      EList<DmlMethod> _methods = it.getMethods();
      for (final DmlMethod statement : _methods) {
        {
          if ((statement instanceof BulkImport)) {
            throw Unsupported.of((("bulk-import inside a transaction; " + RedisStatements.PENDING) + " (the file\'s records as one write step)"));
          }
          final List<Code.Stmt> inner = this.body(statement);
          if (((inner.size() != 1) || (!(IterableExtensions.<Code.Stmt>head(inner) instanceof Code.Return)))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          Code.Stmt _head = IterableExtensions.<Code.Stmt>head(inner);
          Code.Expr _value = ((Code.Return) _head).value();
          final Code.Call invocation = ((Code.Call) _value);
          boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("write", "update", "delete")).contains(invocation.op());
          boolean _not = (!_contains);
          if (_not) {
            throw Unsupported.of("this statement inside a transaction; a transaction holds writes");
          }
          boolean _has = invocation.args().has("search");
          if (_has) {
            throw Unsupported.of("a filtered update or delete inside a transaction; the commands queued in MULTI answer only at EXEC, so a filter cannot choose the keys of a later step (steps select documents by id)");
          }
          if ((Objects.equals(invocation.op(), "delete") && (!invocation.args().has("ids")))) {
            throw Unsupported.of("a delete of all documents inside a transaction; steps select documents by id");
          }
          final Code.Entries step = RedisSchema.kw("op", Code.text(invocation.op()));
          Set<Map.Entry<String, Code.Expr>> _entrySet = invocation.args().entries().entrySet();
          for (final Map.Entry<String, Code.Expr> entry : _entrySet) {
            String _key = entry.getKey();
            boolean _equals = Objects.equals(_key, "wait");
            if (_equals) {
              wait = true;
            } else {
              step.put(entry.getKey(), entry.getValue());
            }
          }
          steps.add(step);
        }
      }
      final Code.Entries args = RedisSchema.kw("steps", Code.items(steps));
      if (wait) {
        args.put("wait", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("transaction", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Writes are visible to the next command (the tiered HNSW index searches its flat buffer), so 'wait' asks for durability.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, RedisGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  /**
   * A statement's filter with its tenant, namespace and partition conditions.
   */
  private RedisFilter filterOf(final EObject statement, final VectorCollection collection, final FilterBlock block) {
    RedisFilter _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final RedisFilter result = new RedisFilter(this.schema, collection).where(block);
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Text _text = Code.text(tenant.getName());
        result.slice(index.tenant(), Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text)));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Text _text_1 = Code.text(((String) namespace));
        result.slice(index.namespace(), Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text_1)));
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition single = ((Partition) _feature_1);
      Object _feature_2 = this.feature(statement, "partitions");
      final List<Partition> partitions = ((List<Partition>) _feature_2);
      final List<Partition> named = CollectionLiterals.<Partition>newArrayList();
      if ((single != null)) {
        named.add(single);
      }
      if ((partitions != null)) {
        Iterables.<Partition>addAll(named, partitions);
      }
      boolean _isEmpty = named.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _partition = index.partition();
        boolean _tripleEquals = (_partition == null);
        if (_tripleEquals) {
          throw Unsupported.of("partitions of a collection declared without partitions");
        }
        final Function1<Partition, Code.Expr> _function = (Partition p) -> {
          Code.Text _text_2 = Code.text(p.getName());
          return ((Code.Expr) _text_2);
        };
        result.slice(index.partition(), IterableExtensions.<Code.Expr>toList(ListExtensions.<Partition, Code.Expr>map(named, _function)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Every document and index lives in memory, so a load checks that the index exists.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      if (((!it.getPartitions().isEmpty()) && (this.schema.index(it.getCollection()).partition() == null))) {
        throw Unsupported.of("partitions of a collection declared without partitions");
      }
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of(RedisStatements.NO_RELEASE);
  }

  /**
   * WAITAOF 1 0: returns when the server's append-only file has fsynced every write so far (appendonly yes in the compose file).
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      List<Code.Stmt> _require = this.require(it.getCollection());
      Iterables.<Code.Stmt>addAll(result, _require);
      Code.Call _runtime = Code.runtime("durable", Code.entries());
      Code.Run _run = new Code.Run(_runtime);
      result.add(_run);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The Query Engine's fork garbage collector compacts indexes on its own, so a compaction checks that the index exists.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      this.schema.note(it, "targetSegments", "async");
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = RedisSchema.kw("index", Code.text(this.schema.index(it.getCollection()).name()));
      boolean _isIncludeStats = it.isIncludeStats();
      if (_isIncludeStats) {
        args.put("stats", Code.flag(true));
      }
      boolean _isIncludeIndexProgress = it.isIncludeIndexProgress();
      if (_isIncludeIndexProgress) {
        args.put("progress", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("describe", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + RedisSchema.NO_MODELS));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(RedisStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(RedisStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(RedisStatements.NO_SNAPSHOTS);
  }

  /**
   * COPY of every key to the target prefix, then FT.CREATE of the target's index (which indexes the copies).
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("cloning a deployment; " + RedisStatements.SINGLE_SERVER));
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; COPY duplicates each value (Redis has no copy-on-write keys)");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = RedisStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final RedisIndex.Index source = this.schema.index(it.getSourceCollection());
      final RedisIndex.Index target = source.named(RedisIndex.id(it.getTargetName()));
      Code.Call _runtime = Code.runtime("copy_collection", RedisSchema.kw("prefix", Code.text(source.prefix()), "target", Code.text(target.prefix()), "create", Code.texts(RedisIndex.definition(target))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The index is dropped (documents kept) and created again; the new index scans the keys in the background.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final VectorCollection collection = this.indexCollection(it.getIndex());
      boolean _isConcurrently = it.isConcurrently();
      if (_isConcurrently) {
        this.notes.add("concurrently on RebuildIndex; the new index serves queries while it scans the keys");
      }
      final RedisIndex.Index current = this.schema.index(collection);
      Code.Call _runtime = Code.runtime("recreate", RedisSchema.kw("index", Code.text(current.name()), "create", Code.texts(RedisIndex.definition(current))));
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private VectorCollection indexCollection(final Index index) {
    VectorCollection _xblockexpression = null;
    {
      VectorCollection _switchResult = null;
      boolean _matched = false;
      if (index instanceof VectorIndex) {
        _matched=true;
        VectorCollection _xblockexpression_1 = null;
        {
          VectorField _elvis = null;
          VectorField _field = ((VectorIndex)index).getField();
          if (_field != null) {
            _elvis = _field;
          } else {
            VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(index, VectorField.class);
            _elvis = _containerOfType;
          }
          final VectorField field = _elvis;
          VectorCollection _xifexpression = null;
          if ((field == null)) {
            _xifexpression = null;
          } else {
            VectorCollection _elvis_1 = null;
            VectorCollection _owningCollection = ModelUtil.owningCollection(field);
            if (_owningCollection != null) {
              _elvis_1 = _owningCollection;
            } else {
              VectorCollection _targetCollection = ModelUtil.targetCollection(index);
              _elvis_1 = _targetCollection;
            }
            _xifexpression = _elvis_1;
          }
          _xblockexpression_1 = _xifexpression;
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (index instanceof PayloadIndex) {
          _matched=true;
          VectorCollection _elvis = null;
          VectorCollection _targetCollection = ModelUtil.targetCollection(index);
          if (_targetCollection != null) {
            _elvis = _targetCollection;
          } else {
            VectorCollection _xifexpression = null;
            Field _field = ((PayloadIndex)index).getTarget().getField();
            boolean _tripleEquals = (_field == null);
            if (_tripleEquals) {
              _xifexpression = null;
            } else {
              _xifexpression = ModelUtil.owningCollection(((PayloadIndex)index).getTarget().getField());
            }
            _elvis = _xifexpression;
          }
          _switchResult = _elvis;
        }
      }
      final VectorCollection collection = _switchResult;
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      _xblockexpression = collection;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(RedisStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        VectorCollection _collection = it.getCollection();
        String _elvis = null;
        String _namespace_1 = index.namespace();
        if (_namespace_1 != null) {
          _elvis = _namespace_1;
        } else {
          _elvis = RedisIndex.NAMESPACE;
        }
        return this.dropSlice(_collection, _elvis, it.getNamespace());
      }
      Code.Call _runtime = Code.runtime("truncate", RedisSchema.kw("prefix", Code.text(index.prefix())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept owner = it.getScope();
      final Code.Entries args = RedisSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner instanceof Database)) {
              args.put("database", Code.text(RedisIndex.id(((Database) owner).getName())));
            } else {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing collections in a " + _name);
                throw Unsupported.of(_plus);
              }
            }
            break;
          case DATABASES:
            if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
              String _name_1 = owner.eClass().getName();
              String _plus_1 = ("listing databases in a " + _name_1);
              throw Unsupported.of(_plus_1);
            }
            break;
          case INDEXES:
          case ALIASES:
            if ((!(owner instanceof VectorCollection))) {
              String _literal = it.getKind().getLiteral();
              String _plus_2 = ("listing " + _literal);
              String _plus_3 = (_plus_2 + " without a collection; FT.ALIASLIST lists the aliases of one index");
              throw Unsupported.of(_plus_3);
            }
            args.put("index", Code.text(this.schema.index(((VectorCollection) owner)).name()));
            break;
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            if ((!(owner instanceof VectorCollection))) {
              String _literal_1 = it.getKind().getLiteral();
              String _plus_4 = ("listing " + _literal_1);
              String _plus_5 = (_plus_4 + " without a collection");
              throw Unsupported.of(_plus_5);
            }
            final RedisIndex.Index index = this.schema.index(((VectorCollection) owner));
            String _switchResult_1 = null;
            EListKind _kind_1 = it.getKind();
            if (_kind_1 != null) {
              switch (_kind_1) {
                case TENANTS:
                  _switchResult_1 = index.tenant();
                  break;
                case NAMESPACES:
                  _switchResult_1 = index.namespace();
                  break;
                default:
                  _switchResult_1 = index.partition();
                  break;
              }
            } else {
              _switchResult_1 = index.partition();
            }
            final String attribute = _switchResult_1;
            if ((attribute == null)) {
              String _literal_2 = it.getKind().getLiteral();
              String _plus_6 = ("listing " + _literal_2);
              String _plus_7 = (_plus_6 + " of a collection without ");
              String _literal_3 = it.getKind().getLiteral();
              String _plus_8 = (_plus_7 + _literal_3);
              throw Unsupported.of(_plus_8);
            }
            args.put("index", Code.text(index.name()));
            args.put("attribute", Code.text(attribute));
            break;
          case SNAPSHOTS:
            throw Unsupported.of(RedisStatements.NO_SNAPSHOTS);
          default:
            break;
        }
      }
      String _prefix = it.getPrefix();
      boolean _tripleNotEquals = (_prefix != null);
      if (_tripleNotEquals) {
        args.put("prefix", Code.text(it.getPrefix()));
      }
      boolean _written = ModelUtil.written(it, "listLimit");
      if (_written) {
        args.put("limit", Code.num(it.getListLimit()));
      }
      Code.Call _runtime = Code.runtime("names", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Polls FT.INFO until the background scan has indexed every existing key.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state");
      final Code.Entries args = RedisSchema.kw("index", Code.text(this.schema.index(this.indexCollection(it.getIndex())).name()));
      boolean _written = ModelUtil.written(it, "timeoutSeconds");
      if (_written) {
        args.put("timeout", Code.num(it.getTimeoutSeconds()));
      }
      Code.Call _runtime = Code.runtime("await_index", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(RedisStatements.NO_TEXT);
      }
      this.reject(it, "elementScope", "asOf", "explain", "reranker", "queryModel", "cursor", "mmr", "highlighted", "sortClauses", 
        "aggregations", "prefetches", "groupBy", "boost");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final RedisIndex.Attr attr = index.vector(this.schema.name(it.getCollection(), field));
      this.checkMetric(it, attr);
      final RedisQuery.Params bound = new RedisQuery.Params();
      final Code.Entries blobs = Code.entries();
      final Code.Entries lookup = this.queryVector(bound, blobs, it.getInput(), it.getCollection(), attr);
      final List<RedisQuery.Cond> conditions = this.filterOf(it, it.getCollection(), it.getFilter()).parts();
      final String where = RedisQuery.where(conditions, bound);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      final int k = (_topK + _offsetOf);
      SearchParams _searchParams = this.searchParams(it);
      boolean _isExact = it.isExact();
      boolean _notEquals = (!Objects.equals(where, "*"));
      final List<String> command = RedisQuery.knn(index, where, attr, k, this.knnSettings(attr, _searchParams, _isExact, _notEquals, it), "vec", this.offsetOf(it), it.getTopK());
      this.timeout(command, this.searchParams(it));
      final Code.Entries args = this.searchArgs(command, bound, index, "knn", it.getCollection(), it.getOutput(), it);
      args.put("blobs", blobs);
      if ((lookup != null)) {
        args.put("lookup", lookup);
      }
      this.scoring(args, it, attr);
      this.total(args, it, index, conditions);
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private SearchParams searchParams(final EObject search) {
    Object _feature = this.feature(search, "params");
    return ((SearchParams) _feature);
  }

  private void checkMetric(final EObject search, final RedisIndex.Attr attr) {
    if ((ModelUtil.written(search, "metric") && (!Objects.equals(((EMetric) this.feature(search, "metric")).getLiteral(), attr.vec().metric())))) {
      throw Unsupported.of("a metric other than the field\'s; the DISTANCE_METRIC of a vector attribute is fixed when the index is created");
    }
  }

  /**
   * Similarity or native distance, and score bounds on the converted score.
   */
  private void scoring(final Code.Entries args, final EObject search, final RedisIndex.Attr attr) {
    args.put("metric", Code.text(attr.vec().metric()));
    if ((ModelUtil.written(search, "scoreForm") && Objects.equals(this.feature(search, "scoreForm"), EScoreForm.NATIVE))) {
      args.put("native", Code.flag(true));
    }
    Object _feature = this.feature(search, "bounds");
    this.bounds(args, ((ScoreBounds) _feature));
  }

  private void bounds(final Code.Entries args, final ScoreBounds bounds) {
    if (((bounds != null) && ModelUtil.written(bounds, "min"))) {
      args.put("low", Code.num(bounds.getMin()));
    }
    if (((bounds != null) && ModelUtil.written(bounds, "max"))) {
      args.put("high", Code.num(bounds.getMax()));
    }
  }

  /**
   * with-total: the number of documents the filter matches, counted separately with its own parameters.
   */
  private void total(final Code.Entries args, final EObject search, final RedisIndex.Index index, final List<RedisQuery.Cond> conditions) {
    boolean _written = ModelUtil.written(search, "withTotal");
    boolean _not = (!_written);
    if (_not) {
      return;
    }
    final RedisQuery.Params counted = new RedisQuery.Params();
    args.put("total", RedisQuery.search(RedisQuery.count(index, RedisQuery.where(conditions, counted)), counted, index));
  }

  private Code.Entries searchArgs(final List<String> command, final RedisQuery.Params bound, final RedisIndex.Index index, final String reply, final VectorCollection collection, final OutputSpec output, final EObject search) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = RedisQuery.search(command, bound, index);
      args.put("reply", Code.text(reply));
      args.put("key", Code.text(index.key()));
      Object _elvis = null;
      Object _feature = this.feature(search, "withVectors");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        _elvis = Boolean.valueOf(false);
      }
      final Pair<List<String>, List<String>> shape = this.projection(collection, output, this.excludedOf(search), (((Boolean) _elvis)).booleanValue());
      args.put("output", Code.texts(shape.getKey()));
      boolean _isEmpty = shape.getValue().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(shape.getValue()));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * KNN query attributes: EF_RUNTIME (candidates), HYBRID_POLICY (filter strategy, exact) and extension settings.
   */
  private Map<String, String> knnSettings(final RedisIndex.Attr attr, final SearchParams bound, final boolean exact, final boolean filtered, final EObject search) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      final String algorithm = attr.vec().algorithm();
      if ((bound != null)) {
        boolean _written = ModelUtil.written(bound, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: EF_RUNTIME and EPSILON are effort settings, with no declarable recall target");
        }
        boolean _written_1 = ModelUtil.written(bound, "probes");
        if (_written_1) {
          throw Unsupported.of("probes; the Query Engine has no IVF index");
        }
        boolean _written_2 = ModelUtil.written(bound, "scanBudget");
        if (_written_2) {
          throw Unsupported.of("scan-budget; the Query Engine has no setting that bounds the vectors a KNN query visits");
        }
        Set<String> _keySet = ModelUtil.extParams(bound, RedisGenerator.VENDOR).keySet();
        for (final String key : _keySet) {
          throw Unsupported.of((("extension parameter " + key) + " in search parameters"));
        }
        boolean _written_3 = ModelUtil.written(bound, "candidatePool");
        if (_written_3) {
          boolean _equals = Objects.equals(algorithm, "HNSW");
          if (_equals) {
            result.put("EF_RUNTIME", String.valueOf(bound.getCandidatePool()));
          } else {
            boolean _equals_1 = Objects.equals(algorithm, "SVS-VAMANA");
            if (_equals_1) {
              result.put("SEARCH_WINDOW_SIZE", String.valueOf(bound.getCandidatePool()));
            } else {
              this.notes.add("candidatePool on SearchParams, which a FLAT index does not use");
            }
          }
        }
        this.schema.note(bound, "oversampling", "rescore");
      }
      String _xifexpression = null;
      if (((bound != null) && ModelUtil.written(bound, "filterStrategy"))) {
        _xifexpression = bound.getFilterStrategy().getLiteral();
      } else {
        _xifexpression = "auto";
      }
      final String strategy = _xifexpression;
      final boolean exhaustive = (exact || ((bound != null) && bound.isExhaustive()));
      if ((exhaustive && (!Objects.equals(algorithm, "FLAT")))) {
        if ((!filtered)) {
          throw Unsupported.of((("an exact search without a filter on a " + algorithm) + " index; HYBRID_POLICY ADHOC_BF scores filtered documents exactly, and a FLAT index searches exactly"));
        }
        result.put("HYBRID_POLICY", "ADHOC_BF");
      } else {
        if ((filtered && Objects.equals(strategy, "pre_filter"))) {
          result.put("HYBRID_POLICY", "ADHOC_BF");
        } else {
          if ((filtered && (Objects.equals(strategy, "post_filter") || Objects.equals(strategy, "iterative")))) {
            result.put("HYBRID_POLICY", "BATCHES");
          }
        }
      }
      String _get = result.get("HYBRID_POLICY");
      boolean _equals_2 = Objects.equals(_get, "ADHOC_BF");
      if (_equals_2) {
        final String efRuntime = result.remove("EF_RUNTIME");
        final String windowSize = result.remove("SEARCH_WINDOW_SIZE");
        if (((efRuntime != null) || (windowSize != null))) {
          this.notes.add("candidatePool on SearchParams, which HYBRID_POLICY ADHOC_BF (a brute-force scan of the filtered documents) does not use");
        }
      }
      Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(search, RedisGenerator.VENDOR).entrySet();
      for (final Map.Entry<String, Literal> entry : _entrySet) {
        {
          final String setting = RedisStatements.SEARCH_SETTINGS.get(entry.getKey().toLowerCase());
          if ((setting == null)) {
            String _key = entry.getKey();
            String _plus = ("extension parameter " + _key);
            String _plus_1 = (_plus + "; Redis query attributes are ");
            String _join = IterableExtensions.join(RedisStatements.SEARCH_SETTINGS.keySet(), ", ");
            String _plus_2 = (_plus_1 + _join);
            throw Unsupported.of(_plus_2);
          }
          final Literal value = entry.getValue();
          String _switchResult = null;
          boolean _matched = false;
          if (value instanceof StringLit) {
            _matched=true;
            _switchResult = RedisIndex.id(((StringLit)value).getValue());
          }
          if (!_matched) {
            if (value instanceof NumberLit) {
              _matched=true;
              _switchResult = RedisIndex.number(((NumberLit)value).getValue());
            }
          }
          if (!_matched) {
            String _key_1 = entry.getKey();
            String _plus_3 = ("extension parameter " + _key_1);
            String _plus_4 = (_plus_3 + " with a value that is not a string or number literal");
            throw Unsupported.of(_plus_4);
          }
          result.put(setting, _switchResult);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private void timeout(final List<String> command, final SearchParams bound) {
    if (((bound == null) || (!ModelUtil.written(bound, "timeoutMillis")))) {
      return;
    }
    final int at = command.indexOf("DIALECT");
    int _xifexpression = (int) 0;
    if ((at < 0)) {
      _xifexpression = command.size();
    } else {
      _xifexpression = at;
    }
    String _valueOf = String.valueOf(bound.getTimeoutMillis());
    command.addAll(_xifexpression, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("TIMEOUT", _valueOf)));
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker", "minShouldMatch", "cursor", "mmr", "highlighted", "sortClauses", "aggregations", 
        "groupBy", "boost");
      this.queryExtensions(it);
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final RedisQuery.Params bound = new RedisQuery.Params();
      final String where = RedisQuery.where(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), bound);
      VectorCollection _collection = it.getCollection();
      EList<SearchFieldRef> _searchFields = it.getSearchFields();
      QueryInput _input = it.getInput();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "textQueryType");
      if (_written) {
        _xifexpression = it.getTextQueryType().getLiteral();
      } else {
        _xifexpression = "match";
      }
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(it, "matchOperator");
      if (_written_1) {
        _xifexpression_1 = it.getMatchOperator().getLiteral();
      } else {
        _xifexpression_1 = "and";
      }
      String _xifexpression_2 = null;
      boolean _written_2 = ModelUtil.written(it, "fuzziness");
      if (_written_2) {
        _xifexpression_2 = it.getFuzziness();
      } else {
        _xifexpression_2 = null;
      }
      Integer _xifexpression_3 = null;
      boolean _written_3 = ModelUtil.written(it, "slop");
      if (_written_3) {
        _xifexpression_3 = Integer.valueOf(it.getSlop());
      } else {
        _xifexpression_3 = null;
      }
      final String text = this.keywordQuery(_collection, index, _searchFields, _input, _xifexpression, _xifexpression_1, _xifexpression_2, _xifexpression_3, bound);
      String _xifexpression_4 = null;
      boolean _equals = Objects.equals(where, "*");
      if (_equals) {
        _xifexpression_4 = text;
      } else {
        _xifexpression_4 = ((where + " ") + text);
      }
      final String query = _xifexpression_4;
      final List<String> command = RedisQuery.keyword(index, query, this.offsetOf(it), it.getTopK());
      if ((ModelUtil.written(it, "textQueryType") && Objects.equals(it.getTextQueryType().getLiteral(), "term"))) {
        command.add(3, "VERBATIM");
      }
      this.timeout(command, it.getParams());
      final Code.Entries args = this.searchArgs(command, bound, index, "scores", it.getCollection(), it.getOutput(), it);
      this.bounds(args, it.getBounds());
      boolean _written_4 = ModelUtil.written(it, "withTotal");
      if (_written_4) {
        args.put("total", RedisQuery.search(RedisQuery.count(index, query), bound, index));
      }
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The text part of a keyword query over the listed full-text fields (every one without a list): one group per field, with the
   * field's boost as $weight, the groups joined with | (a document matches through any field).
   */
  private String keywordQuery(final VectorCollection collection, final RedisIndex.Index index, final List<SearchFieldRef> refs, final QueryInput input, final String type, final String operator, final Object fuzziness, final Object slop, final RedisQuery.Params bound) {
    String _xblockexpression = null;
    {
      final List<Pair<String, Double>> fields = CollectionLiterals.<Pair<String, Double>>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        final Function1<RedisIndex.Attr, Boolean> _function = (RedisIndex.Attr it) -> {
          String _kind = it.kind();
          return Boolean.valueOf(Objects.equals(_kind, "TEXT"));
        };
        Iterable<RedisIndex.Attr> _filter = IterableExtensions.<RedisIndex.Attr>filter(index.attrs(), _function);
        for (final RedisIndex.Attr attr : _filter) {
          String _name = attr.name();
          Pair<String, Double> _mappedTo = Pair.<String, Double>of(_name, Double.valueOf(1.0));
          fields.add(_mappedTo);
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final RedisIndex.Attr attr_1 = index.attr(this.schema.name(collection, ref.getField()));
            String _kind = attr_1.kind();
            boolean _notEquals = (!Objects.equals(_kind, "TEXT"));
            if (_notEquals) {
              String _name_1 = attr_1.name();
              String _plus = ("keyword search on " + _name_1);
              String _plus_1 = (_plus + ", which is not a full-text field");
              throw Unsupported.of(_plus_1);
            }
            String _name_2 = attr_1.name();
            double _xifexpression = (double) 0;
            boolean _written = ModelUtil.written(ref, "boost");
            if (_written) {
              _xifexpression = ref.getBoost();
            } else {
              _xifexpression = 1.0;
            }
            Pair<String, Double> _mappedTo_1 = Pair.<String, Double>of(_name_2, Double.valueOf(_xifexpression));
            fields.add(_mappedTo_1);
          }
        }
      }
      boolean _isEmpty_1 = fields.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search in a collection without full-text fields");
      }
      final Code.Expr words = this.keywordText(input);
      String _switchResult = null;
      if (type != null) {
        switch (type) {
          case "match":
            String _xifexpression = null;
            if ((fuzziness != null)) {
              String _xblockexpression_1 = null;
              {
                final int level = this.fuzzLevel(fuzziness);
                _xblockexpression_1 = bound.terms(words, ("fuzzy" + Integer.valueOf(level)));
              }
              _xifexpression = _xblockexpression_1;
            } else {
              String _xifexpression_1 = null;
              boolean _equals = Objects.equals(operator, "or");
              if (_equals) {
                _xifexpression_1 = "or";
              } else {
                _xifexpression_1 = "and";
              }
              _xifexpression = bound.terms(words, _xifexpression_1);
            }
            _switchResult = _xifexpression;
            break;
          case "phrase":
            _switchResult = bound.terms(words, "and");
            break;
          case "term":
            _switchResult = bound.terms(words, "and");
            break;
          case "prefix":
            _switchResult = bound.terms(words, "prefix");
            break;
          case "phrase_prefix":
            _switchResult = bound.terms(words, "prefixlast");
            break;
          case "fuzzy":
            int _xifexpression_2 = (int) 0;
            if ((fuzziness == null)) {
              _xifexpression_2 = 1;
            } else {
              _xifexpression_2 = this.fuzzLevel(fuzziness);
            }
            String _plus = ("fuzzy" + Integer.valueOf(_xifexpression_2));
            _switchResult = bound.terms(words, _plus);
            break;
          case "wildcard":
            String _bind = bound.bind(words);
            String _plus_1 = ("w\'" + _bind);
            _switchResult = (_plus_1 + "\'");
            break;
          case "query_string":
            throw Unsupported.of("text-query query_string; the generator never passes a value as query syntax (values are PARAMS), so user query strings are not parsed");
          default:
            throw Unsupported.of((("text-query " + type) + "; the query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions"));
        }
      } else {
        throw Unsupported.of((("text-query " + type) + "; the query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions"));
      }
      final String terms = _switchResult;
      if ((((fuzziness != null) && (!Objects.equals(type, "match"))) && (!Objects.equals(type, "fuzzy")))) {
        throw Unsupported.of(("the \'fuzziness\' clause with text-query " + type));
      }
      String _xifexpression_3 = null;
      if ((Objects.equals(type, "phrase") || Objects.equals(type, "phrase_prefix"))) {
        Object _elvis = null;
        if (slop != null) {
          _elvis = slop;
        } else {
          _elvis = Integer.valueOf(0);
        }
        String _plus_2 = ("=>{$slop: " + _elvis);
        _xifexpression_3 = (_plus_2 + "; $inorder: true;}");
      } else {
        String _xifexpression_4 = null;
        if ((slop != null)) {
          throw Unsupported.of((("the \'slop\' clause with text-query " + type) + "; slop applies to phrases"));
        } else {
          _xifexpression_4 = "";
        }
        _xifexpression_3 = _xifexpression_4;
      }
      final String attributes = _xifexpression_3;
      final Function1<Pair<String, Double>, String> _function_1 = (Pair<String, Double> f) -> {
        String _key = f.getKey();
        String _plus_3 = ("@" + _key);
        String _plus_4 = (_plus_3 + ":(");
        String _plus_5 = (_plus_4 + terms);
        String _plus_6 = (_plus_5 + ")");
        String _plus_7 = (_plus_6 + attributes);
        String _xifexpression_5 = null;
        Double _value = f.getValue();
        boolean _notEquals = ((_value).doubleValue() != 1.0);
        if (_notEquals) {
          String _number = RedisIndex.number((f.getValue()).doubleValue());
          String _plus_8 = ("=>{$weight: " + _number);
          _xifexpression_5 = (_plus_8 + ";}");
        } else {
          _xifexpression_5 = "";
        }
        return (_plus_7 + _xifexpression_5);
      };
      final List<String> groups = ListExtensions.<Pair<String, Double>, String>map(fields, _function_1);
      String _xifexpression_5 = null;
      int _size = groups.size();
      boolean _equals_1 = (_size == 1);
      if (_equals_1) {
        _xifexpression_5 = IterableExtensions.<String>head(groups);
      } else {
        String _join = IterableExtensions.join(groups, " | ");
        String _plus_3 = ("(" + _join);
        _xifexpression_5 = (_plus_3 + ")");
      }
      _xblockexpression = _xifexpression_5;
    }
    return _xblockexpression;
  }

  private int fuzzLevel(final Object fuzziness) {
    int _xblockexpression = (int) 0;
    {
      final String text = String.valueOf(fuzziness);
      if ((Objects.equals(text, "AUTO") || Objects.equals(text, "auto"))) {
        throw Unsupported.of("fuzziness AUTO; the query syntax fixes the Levenshtein distance per term (%, %% or %%% for 1 to 3)");
      }
      double _parseDouble = Double.parseDouble(text);
      final int level = ((int) _parseDouble);
      if (((level < 1) || (level > 3))) {
        throw Unsupported.of((("fuzziness " + text) + "; the query syntax matches Levenshtein distances 1 to 3"));
      }
      _xblockexpression = level;
    }
    return _xblockexpression;
  }

  private Code.Expr keywordText(final QueryInput input) {
    Code.Expr _xblockexpression = null;
    {
      if (((input == null) || (!Objects.equals(input.getKind(), EQueryInputMode.TEXT)))) {
        throw Unsupported.of("keyword search without text input");
      }
      boolean _not = (!((input.getValue() instanceof StringLit) || (input.getValue() instanceof ParamRef)));
      if (_not) {
        throw Unsupported.of("keyword input that is not a string");
      }
      _xblockexpression = Literals.of(input.getValue());
    }
    return _xblockexpression;
  }

  /**
   * Hybrid search: FT.HYBRID with COMBINE RRF when there is one keyword and one vector leg with the same window (vecDSL's rrf, ranks
   * from 1); every other combination runs each leg as its own search and fuses in the runtime (FT.HYBRID's LINEAR fusion normalizes
   * scores differently from vecDSL's weighted and relative_score).
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker", "cursor", "mmr", "highlighted", "sortClauses", "aggregations", "groupBy", "boost", "withTotal");
      this.queryExtensions(it);
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final RedisIndex.Index index = this.schema.index(it.getCollection());
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
      final int window = (_topK + _offsetOf);
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "fusionConstant");
      if (_written_1) {
        _xifexpression_1 = it.getFusionConstant();
      } else {
        _xifexpression_1 = 60;
      }
      final int k = _xifexpression_1;
      final List<VectorLeg> vectorLegs = IterableExtensions.<VectorLeg>toList(Iterables.<VectorLeg>filter(it.getLegs(), VectorLeg.class));
      final List<KeywordLeg> keywordLegs = IterableExtensions.<KeywordLeg>toList(Iterables.<KeywordLeg>filter(it.getLegs(), KeywordLeg.class));
      final Function1<HybridLeg, Boolean> _function = (HybridLeg leg) -> {
        return Boolean.valueOf(((!ModelUtil.written(leg, "legLimit")) || ((((Integer) this.feature(leg, "legLimit"))).intValue() == window)));
      };
      final boolean sameWindow = IterableExtensions.<HybridLeg>forall(it.getLegs(), _function);
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Set<String> _keySet = ModelUtil.extParams(leg, RedisGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of((("extension parameter " + key) + " on a hybrid leg"));
          }
          if (((leg instanceof VectorLeg) && (((VectorLeg) leg).getBounds() != null))) {
            throw Unsupported.of((("bounds on a hybrid leg; " + RedisStatements.PENDING) + " (the leg score filtered before fusion)"));
          }
        }
      }
      Code.Entries _xifexpression_2 = null;
      if (((((Objects.equals(fusionKind, "rrf") && (vectorLegs.size() == 1)) && (keywordLegs.size() == 1)) && sameWindow) && (it.getLegs().size() == 2))) {
        _xifexpression_2 = this.nativeHybrid(it, index, IterableExtensions.<VectorLeg>head(vectorLegs), IterableExtensions.<KeywordLeg>head(keywordLegs), window, k);
      } else {
        _xifexpression_2 = this.fusedHybrid(it, index, fusionKind, weights, k, window);
      }
      final Code.Entries args = _xifexpression_2;
      this.bounds(args, it.getBounds());
      String _xifexpression_3 = null;
      boolean _has = args.has("legs");
      if (_has) {
        _xifexpression_3 = "hybrid";
      } else {
        _xifexpression_3 = "search";
      }
      Code.Call _runtime = Code.runtime(_xifexpression_3, args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Entries nativeHybrid(final HybridSearch search, final RedisIndex.Index index, final VectorLeg vectorLeg, final KeywordLeg keywordLeg, final int window, final int k) {
    Code.Entries _xblockexpression = null;
    {
      final VectorCollection collection = search.getCollection();
      final RedisQuery.Params bound = new RedisQuery.Params();
      final Code.Entries blobs = Code.entries();
      final VectorField field = this.vectorFieldOf(collection, vectorLeg.getVectorField());
      final RedisIndex.Attr attr = index.vector(this.schema.name(collection, field));
      this.checkMetric(vectorLeg, attr);
      QueryInput _elvis = null;
      QueryInput _input = vectorLeg.getInput();
      if (_input != null) {
        _elvis = _input;
      } else {
        QueryInput _input_1 = search.getInput();
        _elvis = _input_1;
      }
      final Code.Entries lookup = this.queryVector(bound, blobs, _elvis, collection, attr);
      final String textWhere = RedisQuery.where(this.filterOf(search, collection, this.legFilter(search.getFilter(), keywordLeg.getFilter())).parts(), bound);
      boolean _written = ModelUtil.written(keywordLeg, "minShouldMatch");
      if (_written) {
        String _get = RedisStatements.REASONS.get("minShouldMatch");
        String _plus = ("the \'min-should-match\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      EList<SearchFieldRef> _searchFields = keywordLeg.getSearchFields();
      QueryInput _elvis_1 = null;
      QueryInput _input_2 = keywordLeg.getInput();
      if (_input_2 != null) {
        _elvis_1 = _input_2;
      } else {
        QueryInput _input_3 = search.getInput();
        _elvis_1 = _input_3;
      }
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(keywordLeg, "textQueryType");
      if (_written_1) {
        _xifexpression = keywordLeg.getTextQueryType().getLiteral();
      } else {
        _xifexpression = "match";
      }
      String _xifexpression_1 = null;
      boolean _written_2 = ModelUtil.written(keywordLeg, "matchOperator");
      if (_written_2) {
        _xifexpression_1 = keywordLeg.getMatchOperator().getLiteral();
      } else {
        _xifexpression_1 = "and";
      }
      final String text = this.keywordQuery(collection, index, _searchFields, _elvis_1, _xifexpression, _xifexpression_1, null, null, bound);
      final String vectorWhere = RedisQuery.where(this.filterOf(search, collection, this.legFilter(search.getFilter(), vectorLeg.getFilter())).parts(), bound);
      SearchParams _params = vectorLeg.getParams();
      boolean _isExact = vectorLeg.isExact();
      boolean _notEquals = (!Objects.equals(vectorWhere, "*"));
      final Function2<String, String, Boolean> _function = (String name, String value) -> {
        return Boolean.valueOf(Objects.equals(name, "EF_RUNTIME"));
      };
      final Map<String, String> knn = MapExtensions.<String, String>filter(this.knnSettings(attr, _params, _isExact, _notEquals, search), _function);
      String _xifexpression_2 = null;
      boolean _equals = Objects.equals(textWhere, "*");
      if (_equals) {
        _xifexpression_2 = text;
      } else {
        _xifexpression_2 = ((textWhere + " ") + text);
      }
      String _xifexpression_3 = null;
      boolean _equals_1 = Objects.equals(vectorWhere, "*");
      if (_equals_1) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = vectorWhere;
      }
      final List<String> command = RedisQuery.hybrid(index, _xifexpression_2, attr, "vec", knn, _xifexpression_3, window, k, this.offsetOf(search), search.getTopK());
      this.timeout(command, search.getParams());
      final Code.Entries args = this.searchArgs(command, bound, index, "hybrid", collection, search.getOutput(), search);
      args.put("blobs", blobs);
      if ((lookup != null)) {
        args.put("lookup", lookup);
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private Code.Entries fusedHybrid(final HybridSearch search, final RedisIndex.Index index, final String fusionKind, final List<Double> weights, final int k, final int window) {
    Code.Entries _xblockexpression = null;
    {
      final VectorCollection collection = search.getCollection();
      final List<Code.Expr> legArgs = CollectionLiterals.<Code.Expr>newArrayList();
      int number = 0;
      EList<HybridLeg> _legs = search.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          final RedisQuery.Params bound = new RedisQuery.Params((("l" + Integer.valueOf(number)) + "_"));
          Integer _xifexpression = null;
          boolean _written = ModelUtil.written(leg, "legLimit");
          if (_written) {
            Object _feature = this.feature(leg, "legLimit");
            _xifexpression = ((Integer) _feature);
          } else {
            _xifexpression = Integer.valueOf(window);
          }
          final Integer limit = _xifexpression;
          Object _feature_1 = this.feature(leg, "filter");
          final String where = RedisQuery.where(this.filterOf(search, collection, this.legFilter(search.getFilter(), ((FilterBlock) _feature_1))).parts(), bound);
          Code.Entries _switchResult = null;
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            Code.Entries _xblockexpression_1 = null;
            {
              final VectorField field = this.vectorFieldOf(collection, ((VectorLeg)leg).getVectorField());
              final RedisIndex.Attr attr = index.vector(this.schema.name(collection, field));
              this.checkMetric(leg, attr);
              final Code.Entries blobs = Code.entries();
              QueryInput _elvis = null;
              QueryInput _input = ((VectorLeg)leg).getInput();
              if (_input != null) {
                _elvis = _input;
              } else {
                QueryInput _input_1 = search.getInput();
                _elvis = _input_1;
              }
              final Code.Entries lookup = this.queryVector(bound, blobs, _elvis, collection, attr);
              SearchParams _params = ((VectorLeg)leg).getParams();
              boolean _isExact = ((VectorLeg)leg).isExact();
              boolean _notEquals = (!Objects.equals(where, "*"));
              final List<String> command = RedisQuery.knn(index, where, attr, (limit).intValue(), this.knnSettings(attr, _params, _isExact, _notEquals, search), (("l" + Integer.valueOf(number)) + "_vec"), 0, (limit).intValue());
              final Code.Entries result = RedisQuery.search(command, bound, index);
              result.put("reply", Code.text("knn"));
              result.put("blobs", blobs);
              result.put("metric", Code.text(attr.vec().metric()));
              if ((lookup != null)) {
                result.put("lookup", lookup);
              }
              _xblockexpression_1 = result;
            }
            _switchResult = _xblockexpression_1;
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              Code.Entries _xblockexpression_1 = null;
              {
                boolean _written_1 = ModelUtil.written(leg, "minShouldMatch");
                if (_written_1) {
                  String _get = RedisStatements.REASONS.get("minShouldMatch");
                  String _plus = ("the \'min-should-match\' clause; " + _get);
                  throw Unsupported.of(_plus);
                }
                EList<SearchFieldRef> _searchFields = ((KeywordLeg)leg).getSearchFields();
                QueryInput _elvis = null;
                QueryInput _input = ((KeywordLeg)leg).getInput();
                if (_input != null) {
                  _elvis = _input;
                } else {
                  QueryInput _input_1 = search.getInput();
                  _elvis = _input_1;
                }
                String _xifexpression_1 = null;
                boolean _written_2 = ModelUtil.written(leg, "textQueryType");
                if (_written_2) {
                  _xifexpression_1 = ((KeywordLeg)leg).getTextQueryType().getLiteral();
                } else {
                  _xifexpression_1 = "match";
                }
                String _xifexpression_2 = null;
                boolean _written_3 = ModelUtil.written(leg, "matchOperator");
                if (_written_3) {
                  _xifexpression_2 = ((KeywordLeg)leg).getMatchOperator().getLiteral();
                } else {
                  _xifexpression_2 = "and";
                }
                final String text = this.keywordQuery(collection, index, _searchFields, _elvis, _xifexpression_1, _xifexpression_2, null, null, bound);
                String _xifexpression_3 = null;
                boolean _equals = Objects.equals(where, "*");
                if (_equals) {
                  _xifexpression_3 = text;
                } else {
                  _xifexpression_3 = ((where + " ") + text);
                }
                final Code.Entries result = RedisQuery.search(RedisQuery.keyword(index, _xifexpression_3, 0, (limit).intValue()), bound, index);
                result.put("reply", Code.text("scores"));
                _xblockexpression_1 = result;
              }
              _switchResult = _xblockexpression_1;
            }
          }
          final Code.Entries legEntries = _switchResult;
          legArgs.add(legEntries);
          number = (number + 1);
        }
      }
      final Code.Entries args = RedisSchema.kw("legs", Code.items(legArgs), "fusion", Code.text(fusionKind), "key", Code.text(index.key()));
      final Pair<List<String>, List<String>> shape = this.projection(collection, search.getOutput(), this.excludedOf(search), search.isWithVectors());
      args.put("output", Code.texts(shape.getKey()));
      boolean _isEmpty = shape.getValue().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(shape.getValue()));
      }
      args.put("limit", Code.num(search.getTopK()));
      int _offsetOf = this.offsetOf(search);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        args.put("offset", Code.num(this.offsetOf(search)));
      }
      boolean _equals = Objects.equals(fusionKind, "rrf");
      if (_equals) {
        args.put("k", Code.num(k));
      } else {
        final Function1<Double, Code.Expr> _function = (Double w) -> {
          Code.Num _num = Code.num((w).doubleValue());
          return ((Code.Expr) _num);
        };
        args.put("weights", Code.items(ListExtensions.<Double, Code.Expr>map(weights, _function)));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * A leg filter replaces the search's filter; both at once are not generated.
   */
  private FilterBlock legFilter(final FilterBlock outer, final FilterBlock inner) {
    if ((outer == null)) {
      return inner;
    }
    if ((inner == null)) {
      return outer;
    }
    throw Unsupported.of((("a filter on both the hybrid search and a leg; " + RedisStatements.PENDING) + " (the conjunction of both)"));
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
   * The average-vector strategy (2 avg(positive) - avg(negative)) computed in the runtime; the examples are not returned.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(RedisStatements.NO_TEXT);
      }
      this.reject(it, "asOf", "explain", "reranker", "cursor", "mmr", "highlighted", "sortClauses", "aggregations", "groupBy", "boost");
      this.queryExtensions(it);
      boolean _isEmpty_1 = it.getPositives().isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("recommend without positive examples");
      }
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final RedisIndex.Index lookupIndex = this.schema.index(_elvis);
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final String name = this.schema.name(it.getCollection(), field);
      final RedisIndex.Attr attr = index.vector(name);
      final RedisQuery.Params bound = new RedisQuery.Params();
      final List<Code.Expr> positiveIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> positiveVectors = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeVectors = CollectionLiterals.<Code.Expr>newArrayList();
      EList<Literal> _positives = it.getPositives();
      for (final Literal example : _positives) {
        boolean _isVector = this.isVector(example);
        if (_isVector) {
          Code.Expr _of = Literals.of(example);
          positiveVectors.add(_of);
        } else {
          Code.Expr _of_1 = Literals.of(example);
          positiveIds.add(_of_1);
        }
      }
      EList<Literal> _negatives = it.getNegatives();
      for (final Literal example_1 : _negatives) {
        boolean _isVector_1 = this.isVector(example_1);
        if (_isVector_1) {
          Code.Expr _of_2 = Literals.of(example_1);
          negativeVectors.add(_of_2);
        } else {
          Code.Expr _of_3 = Literals.of(example_1);
          negativeIds.add(_of_3);
        }
      }
      final RedisFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final RedisIndex.Attr keyAttr = index.attr(index.key());
        Iterable<Code.Expr> _plus = Iterables.<Code.Expr>concat(positiveIds, negativeIds);
        for (final Code.Expr example_2 : _plus) {
          RedisQuery.Test _test = new RedisQuery.Test(keyAttr, "eq", example_2, null);
          RedisQuery.Not _not_1 = new RedisQuery.Not(_test);
          restriction.add(_not_1);
        }
      }
      final String where = RedisQuery.where(restriction.parts(), bound);
      bound.named("vec", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList())));
      final Code.Entries blobs = RedisSchema.kw("vec", Code.text(attr.vec().type()));
      final Code.Entries lookup = RedisSchema.kw("param", Code.text("vec"), "prefix", Code.text(lookupIndex.prefix()), "path", Code.text(name));
      boolean _isEmpty_2 = positiveIds.isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        lookup.put("positive_ids", Code.items(positiveIds));
      }
      boolean _isEmpty_3 = negativeIds.isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        lookup.put("negative_ids", Code.items(negativeIds));
      }
      boolean _isEmpty_4 = positiveVectors.isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        lookup.put("positive_vectors", Code.items(positiveVectors));
      }
      boolean _isEmpty_5 = negativeVectors.isEmpty();
      boolean _not_5 = (!_isEmpty_5);
      if (_not_5) {
        lookup.put("negative_vectors", Code.items(negativeVectors));
      }
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus_1 = (_topK + _offsetOf);
      SearchParams _searchParams = this.searchParams(it);
      boolean _notEquals = (!Objects.equals(where, "*"));
      final List<String> command = RedisQuery.knn(index, where, attr, _plus_1, this.knnSettings(attr, _searchParams, false, _notEquals, it), "vec", this.offsetOf(it), it.getTopK());
      this.timeout(command, this.searchParams(it));
      final Code.Entries args = this.searchArgs(command, bound, index, "knn", it.getCollection(), it.getOutput(), it);
      args.put("blobs", blobs);
      args.put("lookup", lookup);
      this.scoring(args, it, attr);
      this.total(args, it, index, restriction.parts());
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private boolean isVector(final Literal example) {
    return (((example instanceof ListLit) || (example instanceof SparseLit)) || ((example instanceof ParamRef) && Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("vector", "sparse_vector")).contains(((ParamRef) example).getParam().getType().getLiteral())));
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final Pair<List<String>, List<String>> shape = this.projection(it.getCollection(), it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()), "key", Code.text(index.key()), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), "output", Code.texts(shape.getKey()));
      boolean _isEmpty = shape.getValue().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(shape.getValue()));
      }
      final Code.Entries slices = this.slicesOf(it, it.getCollection());
      boolean _isEmpty_1 = slices.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("slices", slices);
      }
      Code.Call _runtime = Code.runtime("get", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The tenant, namespace and partitions a get checks on the documents of its ids: attribute -> the values a document may hold.
   */
  private Code.Entries slicesOf(final GetById statement, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final Code.Entries result = Code.entries();
      Tenant _tenant = statement.getTenant();
      boolean _tripleNotEquals = (_tenant != null);
      if (_tripleNotEquals) {
        Code.Text _text = Code.text(statement.getTenant().getName());
        result.put(index.tenant(), Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text))));
      }
      String _namespace = statement.getNamespace();
      boolean _tripleNotEquals_1 = (_namespace != null);
      if (_tripleNotEquals_1) {
        Code.Text _text_1 = Code.text(statement.getNamespace());
        result.put(index.namespace(), Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text_1))));
      }
      boolean _isEmpty = statement.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _partition = index.partition();
        boolean _tripleEquals = (_partition == null);
        if (_tripleEquals) {
          throw Unsupported.of("partitions of a collection declared without partitions");
        }
        final Function1<Partition, Code.Expr> _function = (Partition p) -> {
          Code.Text _text_2 = Code.text(p.getName());
          return ((Code.Expr) _text_2);
        };
        result.put(index.partition(), Code.items(ListExtensions.<Partition, Code.Expr>map(statement.getPartitions(), _function)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final RedisQuery.Params bound = new RedisQuery.Params();
      final String where = RedisQuery.where(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), bound);
      Code.Call _runtime = Code.runtime("count", RedisQuery.search(RedisQuery.count(index, where), bound, index));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * FT.AGGREGATE sorted by the sort clauses and the key, then the documents of the page.
   */
  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "sliceIndex");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final RedisQuery.Params bound = new RedisQuery.Params();
      final String where = RedisQuery.where(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), bound);
      final List<String> order = CollectionLiterals.<String>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        {
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
          }
          VectorCollection _collection = it.getCollection();
          final RedisIndex.Attr attr = index.attr(new RedisFilter(this.schema, _collection).attribute(clause.getField()));
          if (((Objects.equals(attr.kind(), "TEXT") || Objects.equals(attr.kind(), "VECTOR")) || attr.array())) {
            String _name = attr.name();
            String _plus = ("sorting by " + _name);
            String _plus_1 = (_plus + "; the generator sorts by single NUMERIC and TAG values");
            throw Unsupported.of(_plus_1);
          }
          String _name_1 = attr.name();
          String _plus_2 = ("@" + _name_1);
          String _xifexpression = null;
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          if (_equals) {
            _xifexpression = "DESC";
          } else {
            _xifexpression = "ASC";
          }
          Iterables.<String>addAll(order, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2, _xifexpression)));
        }
      }
      String _key = index.key();
      String _plus = ("@" + _key);
      Iterables.<String>addAll(order, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus, "ASC")));
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getScrollLimit());
      } else {
        _xifexpression = null;
      }
      final Integer limit = _xifexpression;
      final List<String> command = RedisQuery.ordered(index, where, order, this.offsetOf(it), limit);
      final Code.Entries args = this.searchArgs(command, bound, index, "keys", it.getCollection(), it.getOutput(), it);
      args.entries().remove("reply");
      if ((limit == null)) {
        args.put("every", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final Pair<List<String>, List<String>> shape = this.projection(it.getCollection(), it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()), "key", Code.text(index.key()), "size", Code.num(it.getSampleSize()), "output", Code.texts(shape.getKey()));
      final RedisFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = restriction.parts().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final RedisQuery.Params bound = new RedisQuery.Params();
        args.put("search", RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts(), bound), null), bound, index));
      }
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("sample", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final RedisIndex.Index index = this.schema.index(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final RedisIndex.Attr attr = index.vector(this.schema.name(it.getCollection(), field));
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
      final Code.Entries args = RedisSchema.kw("prefix", Code.text(index.prefix()), "key", Code.text(index.key()), "field", Code.text(attr.name()), "metric", Code.text(attr.vec().metric()), 
        "size", Code.num(_xifexpression), "limit", Code.num(_xifexpression_1));
      final RedisFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = restriction.parts().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final RedisQuery.Params bound = new RedisQuery.Params();
        args.put("search", RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts(), bound), null), bound, index));
      }
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + RedisStatements.NO_GENERATION));
  }

  protected List<Code.Stmt> _body(final BatchQuery it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> calls = CollectionLiterals.<Code.Expr>newArrayList();
      final String owner = this.current;
      int number = 0;
      EList<QueryMethod> _queries = it.getQueries();
      for (final QueryMethod query : _queries) {
        {
          final String name = ((owner + "_query_") + Integer.valueOf(number));
          this.current = name;
          List<String> _parameterNames = ModelUtil.parameterNames(query);
          List<Code.Stmt> _body = this.body(query);
          ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
          ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList();
          Program.Method _method = new Program.Method(name, _parameterNames, _body, _newArrayList, _newArrayList_1, true);
          this.helpers.add(_method);
          Code.Call _self = Code.self(name);
          calls.add(_self);
          number = (number + 1);
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
    String _plus = (RedisStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private Object feature(final EObject object, final String name) {
    Object _xblockexpression = null;
    {
      final EStructuralFeature slot = object.eClass().getEStructuralFeature(name);
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

  private int offsetOf(final EObject query) {
    Integer _xifexpression = null;
    boolean _written = ModelUtil.written(query, "offset");
    if (_written) {
      Object _feature = this.feature(query, "offset");
      _xifexpression = ((Integer) _feature);
    } else {
      _xifexpression = Integer.valueOf(0);
    }
    return (_xifexpression).intValue();
  }

  private List<Field> excludedOf(final EObject query) {
    Object _elvis = null;
    Object _feature = this.feature(query, "excluded");
    if (_feature != null) {
      _elvis = _feature;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    return ((List<Field>) _elvis);
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
      final List<VectorField> fields = ModelUtil.vectorFields(collection);
      int _size = fields.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        int _size_1 = fields.size();
        String _plus_3 = ("a search without \'using\' in a collection with " + Integer.valueOf(_size_1));
        String _plus_4 = (_plus_3 + " vector fields");
        throw Unsupported.of(_plus_4);
      }
      _xblockexpression = IterableExtensions.<VectorField>head(fields);
    }
    return _xblockexpression;
  }

  /**
   * Output fields (the key is always returned as id) and the vector fields returned with 'with-vectors' or named in the output.
   */
  private Pair<List<String>, List<String>> projection(final VectorCollection collection, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    Pair<List<String>, List<String>> _xblockexpression = null;
    {
      final RedisIndex.Index index = this.schema.index(collection);
      final List<String> fields = CollectionLiterals.<String>newArrayList();
      final List<String> vectors = CollectionLiterals.<String>newArrayList();
      List<? extends Field> _xifexpression = null;
      if (((output == null) || output.isAllFields())) {
        EList<Field> _fields = collection.getFields();
        _xifexpression = ((List<? extends Field>) _fields);
      } else {
        _xifexpression = output.getFields();
      }
      final List<? extends Field> listed = _xifexpression;
      for (final Field field : listed) {
        {
          final String name = this.schema.name(collection, field);
          if ((((!Objects.equals(name, index.key())) && (!excluded.contains(field))) && index.fields().contains(name))) {
            if ((field instanceof VectorField)) {
              if (((output != null) && (!output.isAllFields()))) {
                vectors.add(name);
              }
            } else {
              fields.add(name);
            }
          }
        }
      }
      if (withVectors) {
        List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
        for (final VectorField field_1 : _vectorFields) {
          {
            final String name = this.schema.name(collection, field_1);
            if ((((!vectors.contains(name)) && (!excluded.contains(field_1))) && index.fields().contains(name))) {
              vectors.add(name);
            }
          }
        }
      }
      _xblockexpression = Pair.<List<String>, List<String>>of(fields, vectors);
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses Redis lacks, with their reason, and clauses not generated yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = RedisStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = RedisStatements.PENDING_CLAUSES.get(name);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + RedisStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = RedisStatements.REASONS.get(name);
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
   * Redis extension blocks of queries carry query attributes only; extension items of other kinds are rejected.
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
      boolean _equals = Objects.equals(_vendor, RedisGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on Redis");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
  }

  /**
   * The query vector: a PARAMS blob of the given vector, or the stored vector of a record (looked up by the runtime).
   */
  private Code.Entries queryVector(final RedisQuery.Params bound, final Code.Entries blobs, final QueryInput input, final VectorCollection collection, final RedisIndex.Attr attr) {
    Code.Entries _xblockexpression = null;
    {
      if ((input == null)) {
        throw Unsupported.of("a vector query without input");
      }
      Code.Expr _xifexpression = null;
      EQueryInputMode _kind = input.getKind();
      boolean _equals = Objects.equals(_kind, EQueryInputMode.VECTOR);
      if (_equals) {
        _xifexpression = Literals.of(input.getValue());
      } else {
        _xifexpression = Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList()));
      }
      final String name = bound.named("vec", _xifexpression).substring(1);
      blobs.put(name, Code.text(attr.vec().type()));
      Code.Entries _switchResult = null;
      EQueryInputMode _kind_1 = input.getKind();
      if (_kind_1 != null) {
        switch (_kind_1) {
          case VECTOR:
            _switchResult = null;
            break;
          case ID:
            VectorCollection _elvis = null;
            VectorCollection _lookupCollection = input.getLookupCollection();
            if (_lookupCollection != null) {
              _elvis = _lookupCollection;
            } else {
              _elvis = collection;
            }
            Code.Expr _of = Literals.of(input.getValue());
            _switchResult = RedisSchema.kw("param", Code.text(name), "prefix", Code.text(this.schema.index(_elvis).prefix()), "path", Code.text(attr.name()), 
              "positive_ids", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_of))));
            break;
          case SPARSE_VECTOR:
            throw Unsupported.of("sparse_vector input; the Query Engine has no sparse vector attributes");
          case TEXT:
            throw Unsupported.of(("text input" + RedisSchema.NO_MODELS));
          case MULTI_VECTOR:
            throw Unsupported.of("multi-vector input; a KNN query takes one vector");
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " input");
            String _plus_1 = (_plus + RedisSchema.NO_MODELS);
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " input");
        String _plus_1 = (_plus + RedisSchema.NO_MODELS);
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Expr rows(final RecordSource source, final VectorCollection collection) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", RedisSchema.kw("location", Code.text(((FileSource)source).getLocation())));
    }
    if (!_matched) {
      if (source instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Expr> _function = (RecordLiteral item) -> {
          return this.record(item, collection);
        };
        _switchResult = Code.items(ListExtensions.<RecordLiteral, Code.Expr>map(((InlineRecords)source).getRecords(), _function));
      }
    }
    if (!_matched) {
      if (source instanceof ParamSource) {
        _matched=true;
        _switchResult = Literals.of(((ParamSource)source).getParam());
      }
    }
    return _switchResult;
  }

  private Code.Expr record(final RecordLiteral record, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          Literal _value = entry.getValue();
          if ((_value instanceof SparseLit)) {
            throw Unsupported.of("sparse vector values; the Query Engine has no sparse vector attributes");
          }
          result.put(this.schema.name(collection, entry.getField()), Literals.of(entry.getValue()));
        }
      }
      _xblockexpression = result;
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
