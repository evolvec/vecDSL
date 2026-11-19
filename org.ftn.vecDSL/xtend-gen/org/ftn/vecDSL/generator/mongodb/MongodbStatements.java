package org.ftn.vecDSL.generator.mongodb;

import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
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
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScalarDType;
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
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.NullLit;
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
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorDatabase;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps each vecDSL statement to MongoDB commands and aggregation pipelines in the host-language-neutral code model. Every
 * command document, index definition, filter and pipeline is built here, so the three host languages send identical commands:
 * command runs one database command, aggregate runs a pipeline (scores in the canonical form are computed inside it),
 * write, update and delete send the write commands and wait for mongot when asked, hybrid fuses legs that $rankFusion cannot,
 * recommend computes the query vector of a recommendation, and transaction runs writes in one multi-document transaction.
 */
@SuppressWarnings("all")
public class MongodbStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; MongodbCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for MongoDB: ";

  /**
   * Suffix of constructs MongoDB supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no MongoDB mapping yet";

  /**
   * The group value a grouped search carries through the projection to its $group stage.
   */
  private static final String GROUP = "vecdsl_group";

  /**
   * The marker of a geo_point value that the runtimes convert to a GeoJSON point.
   */
  private static final String GEO_POINT = "$geoPoint";

  public static final String NO_MODELS = "; the Atlas Local image runs mongot without the embedding block (Voyage AI API key files) that automated embedding needs, so generated programs supply their own vectors";

  public static final String EXTERNAL = "collections derived from external sources; MongoDB reads parquet, lance, iceberg or vortex files neither as collections nor through a database command (Atlas Data Federation is an Atlas service)";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; MongoDB has no per-collection guard against drop (only roles without the dropCollection action)";

  public static final String NO_TAGS = "collection tags; MongoDB collection options carry a validator, not tags (the description goes into the $jsonSchema)";

  public static final String NO_FILES = "Atlas Search reads synonyms from a collection and has no file-based word lists or dictionaries";

  public static final String NO_SNAPSHOTS = "snapshots; MongoDB backs up with mongodump, mongorestore and file system snapshots, tools outside the database commands, and cloud backups and their schedules are Atlas features";

  public static final String SINGLE = "the Atlas Local image runs one mongod as a single-node replica set; more members and shards are server deployments, not database commands";

  public static final String NO_RERANK = "MongoDB has no reranking stage in the Atlas Local deployment (Voyage AI reranking is an Atlas model API, outside the database)";

  public static final String NO_GENERATION = "MongoDB has no generative model stage";

  public static final String NO_METRICS = "metrics configuration; MongoDB metrics come from serverStatus and mongot\'s Prometheus endpoint, both configured on the servers, not per program";

  public static final String NO_CLUSTER_LIMITS = "cluster limits; MongoDB has no setting that limits the number of collections or databases";

  public static final String NO_RESIDENCY = "WiredTiger reads documents and indexes into its cache when they are accessed, and MongoDB has no command that loads or evicts a collection";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why MongoDB cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "$vectorSearch and $search read mongot\'s current index, and snapshot reads at a past cluster time reach back only minSnapshotHistoryWindowInSeconds (300 s by default)"), Pair.<String, String>of("reranker", MongodbStatements.NO_RERANK), Pair.<String, String>of("queryModel", ("generated MongoDB programs embed no query text" + MongodbStatements.NO_MODELS)), Pair.<String, String>of("cursor", "ranked results have no key order to continue after (searchAfter needs a sort on indexed fields, which vecDSL cursors do not give)"), Pair.<String, String>of("explain", "$vectorSearch returns no score details (explain describes the query plan, not the score)"), Pair.<String, String>of("highlighted", "highlights mark the terms of a text query, which a vector search has not")));

  /**
   * Clauses MongoDB supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "nestedRoot vector indexes over arrays of embedded documents"), Pair.<String, String>of("prefetches", "prefetch stages as input pipelines")));

  /**
   * Options of the aggregate command that an 'extension mongodb' block on a query may set.
   */
  private static final List<String> AGGREGATE_KEYS = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("maxTimeMS", "allowDiskUse", "comment"));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final MongodbSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public MongodbStatements(final Script script, final String fallbackDatabase) {
    this.script = script;
    MongodbSchema _mongodbSchema = new MongodbSchema(this.notes, script, fallbackDatabase);
    this.schema = _mongodbSchema;
  }

  /**
   * The method for one statement, followed by helper methods it calls (batch queries).
   */
  public List<Program.Method> methods(final Method statement, final String methodName) {
    List<Program.Method> _xblockexpression = null;
    {
      this.notes.clear();
      this.helpers.clear();
      this.current = methodName;
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
      Program.Method _method = new Program.Method(methodName, _parameterNames, statements, _list, _newArrayList, false);
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
          _switchResult = "Atlas Search scores string fields with BM25 directly and has no sparse vector field for BM25 weights";
          break;
        case "text_embedding":
        case "sparse_encoding":
          String _replaceFirst = MongodbStatements.NO_MODELS.replaceFirst("^; ", ": ");
          _switchResult = ("automated embedding" + _replaceFirst);
          break;
        default:
          _switchResult = (("MongoDB runs no " + type) + " functions on the server");
          break;
      }
    } else {
      _switchResult = (("MongoDB runs no " + type) + " functions on the server");
    }
    return _switchResult;
  }

  /**
   * Storage settings: disk is WiredTiger (the default storage engine); the other kinds are not available on this deployment.
   */
  public static void storage(final StorageConfig storage) {
    String _location = storage.getLocation();
    boolean _tripleNotEquals = (_location != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a storage location; mongod keeps data under its dbPath, a server setting");
    }
    boolean _isTiered = storage.isTiered();
    if (_isTiered) {
      throw Unsupported.of("tiered storage; Atlas Online Archive is an Atlas service");
    }
    String _literal = storage.getType().getLiteral();
    if (_literal != null) {
      switch (_literal) {
        case "disk":
          boolean _isMmap = storage.isMmap();
          if (_isMmap) {
            throw Unsupported.of("mmap storage; the MMAPv1 storage engine was removed in MongoDB 4.2");
          }
          break;
        case "memory":
          throw Unsupported.of("memory storage; the in-memory storage engine is MongoDB Enterprise only");
        case "object_storage":
          throw Unsupported.of("object storage; MongoDB keeps collections in WiredTiger files on local disks");
        default:
          throw Unsupported.of("embedded storage; MongoDB runs as a server");
      }
    } else {
      throw Unsupported.of("embedded storage; MongoDB runs as a server");
    }
  }

  /**
   * A cluster is the deployment's replica set: replication factor 1 and no sharding.
   */
  public static void cluster(final Cluster declared) {
    if ((declared == null)) {
      return;
    }
    ShardConfig _sharding = declared.getSharding();
    boolean _tripleNotEquals = (_sharding != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(("sharding; " + MongodbStatements.SINGLE));
    }
    if (((declared.getReplication() != null) && (declared.getReplication().getFactor() != 1))) {
      int _factor = declared.getReplication().getFactor();
      String _plus = ("a replication factor of " + Integer.valueOf(_factor));
      String _plus_1 = (_plus + "; ");
      String _plus_2 = (_plus_1 + MongodbStatements.SINGLE);
      throw Unsupported.of(_plus_2);
    }
    boolean _isEmpty = declared.getLimits().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of(MongodbStatements.NO_CLUSTER_LIMITS);
    }
    Set<String> _keySet = ModelUtil.extParams(declared, MongodbGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  /**
   * Partitions are values of a hidden field; partitioning by key, hash or range is sharding.
   */
  public static void checkPartition(final Partition partition) {
    if (((partition.getKey() != null) || (ModelUtil.written(partition, "strategy") && (!Objects.equals(partition.getStrategy().getLiteral(), "list"))))) {
      String _literal = partition.getStrategy().getLiteral();
      String _plus = ("partitions by key or with the " + _literal);
      String _plus_1 = (_plus + " strategy; ");
      String _plus_2 = (_plus_1 + MongodbStatements.SINGLE);
      throw Unsupported.of(_plus_2);
    }
    boolean _isEmpty = partition.getSubPartitions().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("sub-partitions; partitions are values of one hidden field");
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
            throw Unsupported.of(MongodbStatements.NO_METRICS);
          }
          Connection _connection = ((VectorDatabase)concept).getConnection();
          boolean _tripleNotEquals_1 = (_connection != null);
          if (_tripleNotEquals_1) {
            this.connection(((VectorDatabase)concept).getConnection());
          }
          EList<Cluster> _clusters = ((VectorDatabase)concept).getClusters();
          for (final Cluster declared : _clusters) {
            List<Code.Stmt> _define = this.define(declared, ifNotExists, fresh);
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
          MongodbStatements.cluster(((Cluster)concept));
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
          List<Code.Stmt> _view = this.view(this.schema.collection(((Alias)concept).getCollection()), ((Alias)concept).getName(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _view);
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
          String _functionReason = MongodbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + MongodbStatements.NO_FILES));
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
          List<Code.Stmt> _fieldLimit = this.fieldLimit(((FieldLimit)concept));
          Iterables.<Code.Stmt>addAll(result, _fieldLimit);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + MongodbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + MongodbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + MongodbStatements.NO_RERANK));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          final VectorIndex owner = EcoreUtil2.<VectorIndex>getContainerOfType(concept, VectorIndex.class);
          if ((owner == null)) {
            throw Unsupported.of("quantization outside a vector index; mongot quantizes the vector field of a vectorSearch index");
          }
          List<Code.Stmt> _updateVectorIndex = this.updateVectorIndex(owner);
          Iterables.<Code.Stmt>addAll(result, _updateVectorIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          int _factor = ((ReplicationConfig)concept).getFactor();
          boolean _notEquals = (_factor != 1);
          if (_notEquals) {
            int _factor_1 = ((ReplicationConfig)concept).getFactor();
            String _plus = ("a replication factor of " + Integer.valueOf(_factor_1));
            String _plus_1 = (_plus + "; ");
            String _plus_2 = (_plus_1 + MongodbStatements.SINGLE);
            throw Unsupported.of(_plus_2);
          }
          Set<String> _keySet = ModelUtil.extParams(concept, MongodbGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
          Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text("admin"), "command", MongodbCommands.obj("replSetGetStatus", MongodbCommands.num(1)), "select", MongodbCommands.arr(MongodbCommands.text("set")));
          Code.Return _return = new Code.Return(_call);
          result.add(_return);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + MongodbStatements.SINGLE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          MongodbStatements.storage(((StorageConfig)concept));
          Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text("admin"), "command", MongodbCommands.obj("serverStatus", MongodbCommands.num(1)), "select", MongodbCommands.arr(MongodbCommands.text("storageEngine"), MongodbCommands.text("name")));
          Code.Return _return = new Code.Return(_call);
          result.add(_return);
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
          throw Unsupported.of(MongodbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(("backup schedules; " + MongodbStatements.NO_SNAPSHOTS));
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(MongodbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          List<Code.Stmt> _collectionLimit = this.collectionLimit(((CollectionLimit)concept), false);
          Iterables.<Code.Stmt>addAll(result, _collectionLimit);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (MongodbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The program endpoint: checked here, opened by connect(). Passwords come from the environment only.
   */
  private void connection(final Connection concept) {
    if ((((concept.getLocalPath() != null) || concept.isInMemory()) || Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED))) {
      throw Unsupported.of("embedded deployments; MongoDB runs as a server that the drivers reach over the wire protocol");
    }
    Literal _token = concept.getToken();
    boolean _tripleNotEquals = (_token != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("bearer tokens; the database authenticates SCRAM users (OIDC tokens need MongoDB Enterprise or Atlas)");
    }
    Literal _apiKey = concept.getApiKey();
    boolean _tripleNotEquals_1 = (_apiKey != null);
    if (_tripleNotEquals_1) {
      throw Unsupported.of("API keys; Atlas API keys authenticate the Atlas Admin API, not the database");
    }
    Literal _password = concept.getPassword();
    if ((_password instanceof StringLit)) {
      throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; MongoDB serves the wire protocol only");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, MongodbGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    this.schema.note(concept, "cloud", "region", "project");
  }

  /**
   * Authentication: username_password is the connection's SCRAM user; roles are createRole commands; the rest needs Enterprise or Atlas.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EAuthType _type = auth.getType();
      if (_type != null) {
        switch (_type) {
          case API_KEY:
            throw Unsupported.of("api_key authentication; Atlas API keys authenticate the Atlas Admin API, not the database");
          case TOKEN:
            throw Unsupported.of("token authentication; OIDC needs MongoDB Enterprise or Atlas");
          case MTLS:
            throw Unsupported.of("mtls authentication; the Atlas Local image serves without TLS, which X.509 authentication needs");
          case SSO:
            throw Unsupported.of("sso authentication; LDAP, Kerberos and OIDC need MongoDB Enterprise or Atlas");
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
        throw Unsupported.of("client certificates; the Atlas Local image serves without TLS");
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        throw Unsupported.of("audit logs; auditing needs MongoDB Enterprise or Atlas");
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; automatic encryption needs MongoDB Enterprise or Atlas");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = auth.getRoles();
      for (final RoleBinding binding : _roles) {
        List<Code.Stmt> _role = this.role(binding);
        Iterables.<Code.Stmt>addAll(result, _role);
      }
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text("admin"), "command", MongodbCommands.obj("connectionStatus", MongodbCommands.num(1)), "select", MongodbCommands.arr(MongodbCommands.text("authInfo"), MongodbCommands.text("authenticatedUsers")));
        Code.Return _return = new Code.Return(_call);
        result.add(_return);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A role with privilege actions on a collection (or on every collection of the database); an earlier role of that name is replaced.
   */
  private List<Code.Stmt> role(final RoleBinding binding) {
    List<Code.Stmt> _xblockexpression = null;
    {
      MongodbCollection _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.schema.collection(binding.getCollection());
      }
      final MongodbCollection target = _xifexpression;
      String _elvis = null;
      String _database = null;
      if (target!=null) {
        _database=target.database;
      }
      if (_database != null) {
        _elvis = _database;
      } else {
        String _defaultDatabase = this.schema.defaultDatabase();
        _elvis = _defaultDatabase;
      }
      final String database = _elvis;
      String _elvis_1 = null;
      String _name = null;
      if (target!=null) {
        _name=target.name;
      }
      if (_name != null) {
        _elvis_1 = _name;
      } else {
        _elvis_1 = "";
      }
      final Code.Entries resource = MongodbCommands.obj("db", MongodbCommands.text(database), "collection", MongodbCommands.text(_elvis_1));
      final Code.Entries roleDocument = MongodbCommands.obj("createRole", MongodbCommands.text(MongodbCommands.roleName(binding.getRoleName())), "privileges", MongodbCommands.arr(MongodbCommands.obj("resource", resource, "actions", MongodbCommands.arr(MongodbCommands.actions(binding.getPrivileges())))), 
        "roles", MongodbCommands.arr());
      Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(database), "command", MongodbCommands.obj("dropRole", MongodbCommands.text(MongodbCommands.roleName(binding.getRoleName()))), "ignore", MongodbCommands.arr(MongodbCommands.text("RoleNotFound")));
      Code.Run _run = new Code.Run(_call);
      Code.Call _command = MongodbCommands.command(database, roleDocument);
      Code.Run _run_1 = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
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
    throw Unsupported.of((what + " outside a collection; MongoDB adds it to a collection"));
  }

  /**
   * A read-only alias: a view on the collection with an empty pipeline.
   */
  private List<Code.Stmt> view(final MongodbCollection target, final String aliasName, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = MongodbCommands.checkedName(aliasName, "view");
      Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("create", MongodbCommands.text(name), "viewOn", MongodbCommands.text(target.name), "pipeline", MongodbCommands.arr()));
      final Code.Run creation = new Code.Run(_command);
      List<Code.Stmt> _xifexpression = null;
      if (ifNotExists) {
        Code.Call _call = MongodbCommands.call("exists", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(name));
        Code.When _when = new Code.When(_call, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) creation))));
        _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when)));
      } else {
        _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) creation)));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * The view that shows one tenant, namespace or partition; the collection gains the slice field when it lacks it.
   */
  private List<Code.Stmt> slice(final VectorCollection collection, final String kind, final String value, final EObject concept) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((concept instanceof Partition)) {
        MongodbStatements.checkPartition(((Partition) concept));
      }
      MongodbCollection target = this.schema.collection(collection);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      final String fieldName = this.sliceField(target, kind);
      String sliceName = fieldName;
      if ((fieldName == null)) {
        boolean _equals = Objects.equals(kind, "partition");
        if (_equals) {
          throw Unsupported.of("a partition of a collection declared without partitions; declare a partition in the collection, which adds the partition field");
        }
        String _xifexpression = null;
        boolean _equals_1 = Objects.equals(kind, "tenant");
        if (_equals_1) {
          _xifexpression = MongodbCommands.TENANT_FIELD;
        } else {
          _xifexpression = MongodbCommands.NAMESPACE_FIELD;
        }
        sliceName = _xifexpression;
        final MongodbCollection next = target.copy();
        boolean _equals_2 = Objects.equals(kind, "tenant");
        if (_equals_2) {
          next.tenant = sliceName;
        } else {
          next.namespace = sliceName;
        }
        this.schema.evolve(collection, next);
        List<Code.Stmt> _redefine = this.redefine(next, false);
        Iterables.<Code.Stmt>addAll(result, _redefine);
        target = next;
      }
      String _replaceAll = value.replaceAll("[^A-Za-z0-9_]", "_");
      String _plus = ((((target.name + "_") + kind) + "_") + _replaceAll);
      final String name = MongodbCommands.checkedName(_plus, "view");
      Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("create", MongodbCommands.text(name), "viewOn", MongodbCommands.text(target.name), "pipeline", 
        MongodbCommands.arr(MongodbCommands.obj("$match", MongodbCommands.obj(sliceName, MongodbCommands.obj("$eq", MongodbCommands.text(value)))))));
      Code.Run _run = new Code.Run(_command);
      result.add(_run);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String sliceField(final MongodbCollection target, final String kind) {
    String _switchResult = null;
    if (kind != null) {
      switch (kind) {
        case "tenant":
          _switchResult = target.tenant;
          break;
        case "namespace":
          _switchResult = target.namespace;
          break;
        default:
          _switchResult = target.partition;
          break;
      }
    } else {
      _switchResult = target.partition;
    }
    return _switchResult;
  }

  /**
   * collMod with the collection's validator, then the search indexes updated to its fields (and awaited).
   */
  private List<Code.Stmt> redefine(final MongodbCollection target, final boolean textChanged) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("collMod", MongodbCommands.text(target.name), "validator", target.validator(), "validationLevel", MongodbCommands.text("strict")));
      Code.Run _run = new Code.Run(_command);
      result.add(_run);
      for (final MongodbCollection.VectorIndex index : target.vectorIndexes) {
        {
          Code.Call _command_1 = MongodbCommands.command(target.database, MongodbCommands.obj("updateSearchIndex", MongodbCommands.text(target.name), "name", MongodbCommands.text(index.name()), "definition", target.vectorDefinition(index)));
          Code.Run _run_1 = new Code.Run(_command_1);
          result.add(_run_1);
          Code.Stmt _awaitIndex = this.schema.awaitIndex(target, index.name(), MongodbCommands.AWAIT_SECONDS);
          result.add(_awaitIndex);
        }
      }
      if ((textChanged && target.hasText())) {
        Code.Call _call = MongodbCommands.call("ensure_search_index", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "index", MongodbCommands.text(MongodbCommands.TEXT_INDEX), 
          "type", MongodbCommands.text("search"), "definition", target.textDefinition());
        Code.Run _run_1 = new Code.Run(_call);
        result.add(_run_1);
        Code.Stmt _awaitIndex = this.schema.awaitIndex(target, MongodbCommands.TEXT_INDEX, MongodbCommands.AWAIT_SECONDS);
        result.add(_awaitIndex);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The search index vecdsl_text with the collection's analyzers, re-defined after an analyzer changes.
   */
  private List<Code.Stmt> reanalyze(final Analyzer analyzer) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = this.member(analyzer, "an analyzer");
      final List<Analyzer> declared = CollectionLiterals.<Analyzer>newArrayList(((Analyzer[])Conversions.unwrapArray(collection.getAnalyzers(), Analyzer.class)));
      boolean _contains = declared.contains(analyzer);
      boolean _not = (!_contains);
      if (_not) {
        declared.add(analyzer);
      }
      final MongodbCollection next = this.schema.collection(collection).copy();
      next.analyzers = this.schema.analyzers(declared);
      this.schema.evolve(collection, next);
      boolean _hasText = next.hasText();
      boolean _not_1 = (!_hasText);
      if (_not_1) {
        throw Unsupported.of("an analyzer in a collection without full-text fields; the analyzers live in the search index of the full-text fields");
      }
      Code.Call _call = MongodbCommands.call("ensure_search_index", "database", MongodbCommands.text(next.database), "collection", MongodbCommands.text(next.name), "index", MongodbCommands.text(MongodbCommands.TEXT_INDEX), "type", MongodbCommands.text("search"), 
        "definition", next.textDefinition());
      Code.Run _run = new Code.Run(_call);
      Code.Stmt _awaitIndex = this.schema.awaitIndex(next, MongodbCommands.TEXT_INDEX, MongodbCommands.AWAIT_SECONDS);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run), _awaitIndex));
    }
    return _xblockexpression;
  }

  /**
   * A field limit on an existing field: the field's $jsonSchema gains the keyword, collMod replaces the validator.
   */
  private List<Code.Stmt> fieldLimit(final FieldLimit limit) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field owner = EcoreUtil2.<Field>getContainerOfType(limit, Field.class);
      if ((owner == null)) {
        throw Unsupported.of("a field limit outside a field");
      }
      final VectorCollection collection = this.member(owner, "a field limit");
      final MongodbCollection next = this.schema.collection(collection).copy();
      next.replace(ModelUtil.fieldName(owner), this.schema.field(collection, owner));
      this.schema.evolve(collection, next);
      Code.Call _command = MongodbCommands.command(next.database, MongodbCommands.obj("collMod", MongodbCommands.text(next.name), "validator", next.validator(), "validationLevel", MongodbCommands.text("strict")));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> collectionLimit(final CollectionLimit limit, final boolean dropped) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = EcoreUtil2.<VectorCollection>getContainerOfType(limit, VectorCollection.class);
      if ((collection == null)) {
        throw Unsupported.of("a collection limit outside a collection");
      }
      final MongodbCollection next = this.schema.collection(collection).copy();
      String _literal = limit.getType().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "max_fields":
            Integer _xifexpression = null;
            if (dropped) {
              _xifexpression = null;
            } else {
              _xifexpression = Integer.valueOf(limit.getValue());
            }
            next.maxProperties = _xifexpression;
            break;
          case "max_vector_dim":
            if (dropped) {
              throw Unsupported.of("dropping max_vector_dim; the vector fields keep their declared dimensions");
            }
            List<MongodbCollection.Field> _vectorFields = next.vectorFields();
            for (final MongodbCollection.Field vectorField : _vectorFields) {
              int _dims = vectorField.vector().dims();
              int _value = limit.getValue();
              boolean _greaterThan = (_dims > _value);
              if (_greaterThan) {
                String _name = vectorField.name();
                String _plus = ("vector field " + _name);
                String _plus_1 = (_plus + " with more dimensions than max_vector_dim");
                throw Unsupported.of(_plus_1);
              }
            }
            break;
          default:
            String _literal_1 = limit.getType().getLiteral();
            String _plus_2 = ("collection limits (" + _literal_1);
            String _plus_3 = (_plus_2 + "); ");
            String _plus_4 = (_plus_3 + MongodbStatements.SINGLE);
            throw Unsupported.of(_plus_4);
        }
      } else {
        String _literal_1 = limit.getType().getLiteral();
        String _plus_2 = ("collection limits (" + _literal_1);
        String _plus_3 = (_plus_2 + "); ");
        String _plus_4 = (_plus_3 + MongodbStatements.SINGLE);
        throw Unsupported.of(_plus_4);
      }
      this.schema.evolve(collection, next);
      Code.Call _command = MongodbCommands.command(next.database, MongodbCommands.obj("collMod", MongodbCommands.text(next.name), "validator", next.validator(), "validationLevel", MongodbCommands.text("strict")));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A vector index created for an existing field: its vectorSearch index replaces the field's default one.
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
      final MongodbCollection next = this.schema.collection(collection).copy();
      final String fieldName = next.field(ModelUtil.fieldName(owner)).name();
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      final Function1<MongodbCollection.VectorIndex, Boolean> _function = (MongodbCollection.VectorIndex i) -> {
        String _field_1 = i.field();
        return Boolean.valueOf(Objects.equals(_field_1, fieldName));
      };
      final MongodbCollection.VectorIndex previous = IterableExtensions.<MongodbCollection.VectorIndex>findFirst(next.vectorIndexes, _function);
      final MongodbCollection.VectorIndex updated = this.schema.vectorIndex(collection, owner);
      final Predicate<MongodbCollection.VectorIndex> _function_1 = (MongodbCollection.VectorIndex i) -> {
        String _field_1 = i.field();
        return Objects.equals(_field_1, fieldName);
      };
      next.vectorIndexes.removeIf(_function_1);
      String _name = updated.name();
      Code.Entries _options = updated.options();
      Integer _searchCandidates = updated.searchCandidates();
      MongodbCollection.VectorIndex _vectorIndex = new MongodbCollection.VectorIndex(_name, fieldName, _options, _searchCandidates);
      next.vectorIndexes.add(_vectorIndex);
      this.schema.evolve(collection, next);
      final Code.Entries definition = next.vectorDefinition(next.vectorIndexOf(fieldName));
      if (((previous != null) && (!Objects.equals(previous.name(), updated.name())))) {
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(next.database), "command", MongodbCommands.obj("dropSearchIndex", MongodbCommands.text(next.name), "name", MongodbCommands.text(previous.name())), "ignore", MongodbCommands.arr(MongodbCommands.text("IndexNotFound")));
        Code.Run _run = new Code.Run(_call);
        result.add(_run);
      }
      Code.Call _call_1 = MongodbCommands.call("ensure_search_index", "database", MongodbCommands.text(next.database), "collection", MongodbCommands.text(next.name), "index", MongodbCommands.text(updated.name()), "type", MongodbCommands.text("vectorSearch"), 
        "definition", definition);
      Code.Run _run_1 = new Code.Run(_call_1);
      result.add(_run_1);
      Code.Stmt _awaitIndex = this.schema.awaitIndex(next, updated.name(), MongodbCommands.AWAIT_SECONDS);
      result.add(_awaitIndex);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A payload index on an existing field: createIndexes (geo: 2dsphere); a full_text index is the field's place in vecdsl_text.
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
      final MongodbCollection target = this.schema.collection(collection);
      final Code.Entries keys = this.schema.payloadIndexKeys(target, payloadIndex);
      if ((keys == null)) {
        Code.Stmt _awaitIndex = this.schema.awaitIndex(target, MongodbCommands.TEXT_INDEX, MongodbCommands.AWAIT_SECONDS);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_awaitIndex));
      }
      final MongodbCollection next = target.copy();
      final Predicate<MongodbCollection.PayloadIndex> _function = (MongodbCollection.PayloadIndex i) -> {
        String _name = i.name();
        String _name_1 = payloadIndex.getName();
        return Objects.equals(_name, _name_1);
      };
      next.payloadIndexes.removeIf(_function);
      String _field = MongodbCommands.field(payloadIndex.getName());
      MongodbCollection.PayloadIndex _payloadIndex = new MongodbCollection.PayloadIndex(_field, keys);
      next.payloadIndexes.add(_payloadIndex);
      this.schema.evolve(collection, next);
      Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("createIndexes", MongodbCommands.text(target.name), "indexes", MongodbCommands.arr(MongodbCommands.obj("key", keys, "name", MongodbCommands.text(payloadIndex.getName())))));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A field added to an existing collection: validator, search indexes, and the default written where the field is missing.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field added) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection next = this.schema.collection(collection).copy();
      final MongodbCollection.Field mapped = this.schema.field(collection, added);
      final Predicate<MongodbCollection.Field> _function = (MongodbCollection.Field f) -> {
        String _name = f.name();
        String _name_1 = mapped.name();
        return Objects.equals(_name, _name_1);
      };
      next.fields.removeIf(_function);
      next.fields.add(mapped);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      if ((added instanceof VectorField)) {
        final MongodbCollection.VectorIndex index = this.schema.vectorIndex(collection, ((VectorField) added));
        next.vectorIndexes.add(index);
        this.schema.evolve(collection, next);
        Code.Call _command = MongodbCommands.command(next.database, MongodbCommands.obj("collMod", MongodbCommands.text(next.name), "validator", next.validator(), "validationLevel", MongodbCommands.text("strict")));
        Code.Run _run = new Code.Run(_command);
        result.add(_run);
        Code.Call _call = MongodbCommands.call("ensure_search_index", "database", MongodbCommands.text(next.database), "collection", MongodbCommands.text(next.name), "index", MongodbCommands.text(index.name()), "type", MongodbCommands.text("vectorSearch"), 
          "definition", next.vectorDefinition(index));
        Code.Run _run_1 = new Code.Run(_call);
        result.add(_run_1);
        Code.Stmt _awaitIndex = this.schema.awaitIndex(next, index.name(), MongodbCommands.AWAIT_SECONDS);
        result.add(_awaitIndex);
        return result;
      }
      this.schema.evolve(collection, next);
      List<Code.Stmt> _redefine = this.redefine(next, mapped.fullText());
      Iterables.<Code.Stmt>addAll(result, _redefine);
      Code.Expr _defaultValue = mapped.defaultValue();
      boolean _tripleNotEquals = (_defaultValue != null);
      if (_tripleNotEquals) {
        Code.Call _maintenance = this.maintenance(next, MongodbCommands.obj(mapped.name(), MongodbCommands.obj("$exists", MongodbCommands.flag(false))), MongodbCommands.obj("$set", MongodbCommands.obj(mapped.name(), mapped.defaultValue())));
        Code.Run _run_2 = new Code.Run(_maintenance);
        result.add(_run_2);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * An update of every matching document that maintenance needs (defaults, renames, removed fields), past the validator.
   */
  private Code.Call maintenance(final MongodbCollection target, final Code.Entries matching, final Code.Entries change) {
    return MongodbCommands.command(target.database, MongodbCommands.obj("update", MongodbCommands.text(target.name), "updates", MongodbCommands.arr(MongodbCommands.obj("q", matching, "u", change, "multi", MongodbCommands.flag(true))), 
      "bypassDocumentValidation", MongodbCommands.flag(true), "writeConcern", MongodbCommands.obj("w", MongodbCommands.text("majority"))));
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        if (((!it.getOperations().isEmpty()) || (it.getNewName() == null))) {
          throw Unsupported.of("alter operations on an alias; an alias (a view) can be renamed");
        }
        final Alias alias = ((Alias) concept);
        final MongodbCollection aliasTarget = this.schema.collection(alias.getCollection());
        final List<Code.Stmt> renamed = CollectionLiterals.<Code.Stmt>newArrayList();
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(aliasTarget.database), "command", MongodbCommands.obj("drop", MongodbCommands.text(MongodbCommands.checkedName(alias.getName(), "view"))), "ignore", MongodbCommands.arr(MongodbCommands.text("NamespaceNotFound")));
        Code.Run _run = new Code.Run(_call);
        renamed.add(_run);
        List<Code.Stmt> _view = this.view(aliasTarget, it.getNewName(), false);
        Iterables.<Code.Stmt>addAll(renamed, _view);
        return renamed;
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; MongoDB alters collections and views");
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
        final MongodbCollection target = this.schema.collection(collection);
        Code.Call _call_1 = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("listCollections", MongodbCommands.num(1), "filter", MongodbCommands.obj("name", MongodbCommands.obj("$eq", MongodbCommands.text(target.name)))), 
          "select", MongodbCommands.arr(MongodbCommands.text("cursor"), MongodbCommands.text("firstBatch"), MongodbCommands.text("0"), MongodbCommands.text("options")));
        Code.Return _return = new Code.Return(_call_1);
        result.add(_return);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(collection);
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
            String _fieldName = ModelUtil.fieldName(((RenameFieldOp)operation).getField());
            boolean _equals = Objects.equals(_fieldName, target.key);
            if (_equals) {
              throw Unsupported.of("renaming the primary key; the key is _id, which cannot be renamed");
            }
            final MongodbCollection.Field old = target.field(ModelUtil.fieldName(((RenameFieldOp)operation).getField()));
            final String newField = MongodbCommands.field(((RenameFieldOp)operation).getNewName());
            final MongodbCollection next = target.copy();
            next.rename(ModelUtil.fieldName(((RenameFieldOp)operation).getField()), newField);
            this.schema.evolve(collection, next);
            final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
            Code.Call _maintenance = this.maintenance(target, MongodbCommands.obj(old.name(), MongodbCommands.obj("$exists", MongodbCommands.flag(true))), MongodbCommands.obj("$rename", MongodbCommands.obj(old.name(), MongodbCommands.text(newField))));
            Code.Run _run = new Code.Run(_maintenance);
            result.add(_run);
            List<Code.Stmt> _redefine = this.redefine(next, old.fullText());
            Iterables.<Code.Stmt>addAll(result, _redefine);
            _xblockexpression_1 = result;
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof SetPropertyOp) {
          _matched=true;
          _switchResult = this.setProperty(collection, target, ((SetPropertyOp)operation));
        }
      }
      if (!_matched) {
        if (operation instanceof AddAliasOp) {
          _matched=true;
          _switchResult = this.view(target, ((AddAliasOp)operation).getNewAlias().getName(), false);
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = MongodbStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = MongodbStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (MongodbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A field leaves every document ($unset), the validator and the search indexes; a vector field's index is dropped.
   */
  private List<Code.Stmt> dropField(final VectorCollection collection, final Field dropped) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(collection);
      String _fieldName = ModelUtil.fieldName(dropped);
      boolean _equals = Objects.equals(_fieldName, target.key);
      if (_equals) {
        throw Unsupported.of("dropping the primary key; _id cannot be removed");
      }
      final MongodbCollection.Field old = target.field(ModelUtil.fieldName(dropped));
      final MongodbCollection next = target.copy();
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _maintenance = this.maintenance(target, MongodbCommands.obj(old.name(), MongodbCommands.obj("$exists", MongodbCommands.flag(true))), MongodbCommands.obj("$unset", MongodbCommands.obj(old.name(), MongodbCommands.text(""))));
      Code.Run _run = new Code.Run(_maintenance);
      result.add(_run);
      MongodbCollection.Vector _vector = old.vector();
      boolean _tripleNotEquals = (_vector != null);
      if (_tripleNotEquals) {
        final MongodbCollection.VectorIndex index = target.vectorIndexOf(old.name());
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("dropSearchIndex", MongodbCommands.text(target.name), "name", MongodbCommands.text(index.name())), "ignore", MongodbCommands.arr(MongodbCommands.text("IndexNotFound")));
        Code.Run _run_1 = new Code.Run(_call);
        result.add(_run_1);
      }
      next.remove(ModelUtil.fieldName(dropped));
      this.schema.evolve(collection, next);
      List<Code.Stmt> _redefine = this.redefine(next, old.fullText());
      Iterables.<Code.Stmt>addAll(result, _redefine);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description in the $jsonSchema, dynamic as additionalProperties, ttl as the TTL index.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final MongodbCollection target, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection next = target.copy();
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      ECollectionProperty _property = operation.getProperty();
      boolean _equals = Objects.equals(_property, ECollectionProperty.CONSISTENCY);
      if (_equals) {
        this.notes.add("consistency on SetPropertyOp, which the collection declaration decides for the writes of the script");
        return result;
      }
      ECollectionProperty _property_1 = operation.getProperty();
      boolean _equals_1 = Objects.equals(_property_1, ECollectionProperty.TTL);
      if (_equals_1) {
        Literal _value = operation.getValue();
        boolean _not = (!(_value instanceof NumberLit));
        if (_not) {
          throw Unsupported.of("a ttl that is not a number of seconds");
        }
        Literal _value_1 = operation.getValue();
        double _value_2 = ((NumberLit) _value_1).getValue();
        next.ttlSeconds = Long.valueOf(((long) _value_2));
        this.schema.evolve(collection, next);
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("dropIndexes", MongodbCommands.text(target.name), "index", MongodbCommands.text("vecdsl_ttl")), "ignore", MongodbCommands.arr(MongodbCommands.text("IndexNotFound")));
        Code.Run _run = new Code.Run(_call);
        result.add(_run);
        Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("createIndexes", MongodbCommands.text(target.name), "indexes", MongodbCommands.arr(MongodbCommands.obj("key", MongodbCommands.obj(MongodbCommands.EXPIRES_FIELD, MongodbCommands.num(1)), "name", MongodbCommands.text("vecdsl_ttl"), "expireAfterSeconds", MongodbCommands.num(0)))));
        Code.Run _run_1 = new Code.Run(_command);
        result.add(_run_1);
        return result;
      }
      ECollectionProperty _property_2 = operation.getProperty();
      if (_property_2 != null) {
        switch (_property_2) {
          case DESCRIPTION:
            Literal _value_3 = operation.getValue();
            boolean _not_1 = (!(_value_3 instanceof StringLit));
            if (_not_1) {
              throw Unsupported.of("a description that is not a string literal");
            }
            Literal _value_4 = operation.getValue();
            next.description = ((StringLit) _value_4).getValue();
            break;
          case TAGS:
            throw Unsupported.of(MongodbStatements.NO_TAGS);
          case DYNAMIC:
            next.dynamic = ((operation.getValue() instanceof BoolLit) && ((BoolLit) operation.getValue()).isValue());
            break;
          case DELETION_PROTECTION:
            throw Unsupported.of(MongodbStatements.NO_DELETION_PROTECTION);
          default:
            break;
        }
      } else {
      }
      this.schema.evolve(collection, next);
      Code.Call _command_1 = MongodbCommands.command(next.database, MongodbCommands.obj("collMod", MongodbCommands.text(next.name), "validator", next.validator(), "validationLevel", MongodbCommands.text("strict")));
      Code.Run _run_2 = new Code.Run(_command_1);
      result.add(_run_2);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * renameCollection within the database; the search indexes are ensured on the new name.
   */
  private List<Code.Stmt> rename(final VectorCollection collection, final String newName) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(collection);
      final MongodbCollection next = target.copy();
      next.name = MongodbCommands.checkedName(newName, "collection");
      this.schema.evolve(collection, next);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _command = MongodbCommands.command("admin", MongodbCommands.obj("renameCollection", MongodbCommands.text(((target.database + ".") + target.name)), "to", MongodbCommands.text(((next.database + ".") + next.name))));
      Code.Run _run = new Code.Run(_command);
      result.add(_run);
      List<Code.Expr> _searchIndexes = next.searchIndexes();
      for (final Code.Expr index : _searchIndexes) {
        {
          final Code.Entries entries = ((Code.Entries) index);
          Code.Expr _get = entries.get("name");
          final String indexName = ((Code.Text) _get).value();
          Code.Call _call = MongodbCommands.call("ensure_search_index", "database", MongodbCommands.text(next.database), "collection", MongodbCommands.text(next.name), "index", MongodbCommands.text(indexName), "type", entries.get("type"), 
            "definition", entries.get("definition"));
          Code.Run _run_1 = new Code.Run(_call);
          result.add(_run_1);
          Code.Stmt _awaitIndex = this.schema.awaitIndex(next, indexName, MongodbCommands.AWAIT_SECONDS);
          result.add(_awaitIndex);
        }
      }
      _xblockexpression = result;
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
        _xifexpression = MongodbCommands.arr(MongodbCommands.text("NamespaceNotFound"), MongodbCommands.text("IndexNotFound"));
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
          final MongodbCollection dropped = this.schema.collection(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(dropped.database), "command", MongodbCommands.obj("drop", MongodbCommands.text(dropped.name)), "ignore", ignore);
          Code.Run _run = new Code.Run(_call);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          Code.Call _command = MongodbCommands.command(MongodbCommands.checkedName(((Database)concept).getName(), "database"), MongodbCommands.obj("dropDatabase", MongodbCommands.num(1)));
          Code.Run _run = new Code.Run(_command);
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
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final MongodbCollection aliasTarget = this.schema.collection(((Alias)concept).getCollection());
            Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(aliasTarget.database), "command", MongodbCommands.obj("drop", MongodbCommands.text(MongodbCommands.checkedName(((Alias)concept).getName(), "view"))), "ignore", ignore);
            Code.Run _run = new Code.Run(_call);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            VectorField _elvis = null;
            VectorField _field = ((VectorIndex)concept).getField();
            if (_field != null) {
              _elvis = _field;
            } else {
              VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(concept, VectorField.class);
              _elvis = _containerOfType;
            }
            final VectorField owner = _elvis;
            if ((owner == null)) {
              throw Unsupported.of("a vector index without a vector field");
            }
            final VectorCollection collection = this.member(owner, "a vector index");
            final MongodbCollection target = this.schema.collection(collection);
            final MongodbCollection next = target.copy();
            final Predicate<MongodbCollection.VectorIndex> _function = (MongodbCollection.VectorIndex i) -> {
              String _name = i.name();
              String _name_1 = ((VectorIndex)concept).getName();
              return Objects.equals(_name, _name_1);
            };
            next.vectorIndexes.removeIf(_function);
            this.schema.evolve(collection, next);
            Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("dropSearchIndex", MongodbCommands.text(target.name), "name", MongodbCommands.text(((VectorIndex)concept).getName())), "ignore", ignore);
            Code.Run _run = new Code.Run(_call);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            VectorCollection _elvis = null;
            VectorCollection _targetCollection = ModelUtil.targetCollection(concept);
            if (_targetCollection != null) {
              _elvis = _targetCollection;
            } else {
              VectorCollection _xifexpression_1 = null;
              Field _field = ((PayloadIndex)concept).getTarget().getField();
              boolean _tripleEquals = (_field == null);
              if (_tripleEquals) {
                _xifexpression_1 = null;
              } else {
                _xifexpression_1 = ModelUtil.owningCollection(((PayloadIndex)concept).getTarget().getField());
              }
              _elvis = _xifexpression_1;
            }
            final VectorCollection collection = _elvis;
            if ((collection == null)) {
              throw Unsupported.of("a payload index outside a collection");
            }
            final MongodbCollection target = this.schema.collection(collection);
            String _literal = ((PayloadIndex)concept).getKind().getLiteral();
            boolean _equals = Objects.equals(_literal, "full_text");
            if (_equals) {
              throw Unsupported.of("dropping a full_text payload index; a full-text field stays in the search index vecdsl_text while it is declared full-text");
            }
            Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("dropIndexes", MongodbCommands.text(target.name), "index", MongodbCommands.text(((PayloadIndex)concept).getName())), "ignore", ignore);
            Code.Run _run = new Code.Run(_call);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = MongodbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; redefine the vector index without it (rebuild-index sends the declared definition)");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a MongoDB object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of(("dropping a deployment; " + MongodbStatements.SINGLE));
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + MongodbStatements.SINGLE));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; analyzers are removed with the search index, not while fields use them");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; analyzers are removed with the search index, not while fields use them");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("dropping a field limit; redeclare the field without it (alter replaces the validator)");
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + MongodbStatements.NO_FILES));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + MongodbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + MongodbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + MongodbStatements.NO_RERANK));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of("dropping replication; multi-document transactions and change streams need the replica set");
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("dropping sharding; " + MongodbStatements.SINGLE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("dropping storage settings; WiredTiger is the storage engine of the deployment");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; access control is a server setting (security.authorization)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            String _xifexpression_1 = null;
            VectorCollection _collection = ((RoleBinding)concept).getCollection();
            boolean _tripleEquals = (_collection == null);
            if (_tripleEquals) {
              _xifexpression_1 = this.schema.defaultDatabase();
            } else {
              _xifexpression_1 = this.schema.collection(((RoleBinding)concept).getCollection()).database;
            }
            final String database = _xifexpression_1;
            Code.Text _text = MongodbCommands.text(database);
            Code.Entries _obj = MongodbCommands.obj("dropRole", MongodbCommands.text(MongodbCommands.roleName(((RoleBinding)concept).getRoleName())));
            Code.Items _xifexpression_2 = null;
            boolean _isIfExists_1 = it.isIfExists();
            if (_isIfExists_1) {
              _xifexpression_2 = MongodbCommands.arr(MongodbCommands.text("RoleNotFound"));
            } else {
              _xifexpression_2 = null;
            }
            Code.Call _call = MongodbCommands.call("command", "database", _text, "command", _obj, "ignore", _xifexpression_2);
            Code.Run _run = new Code.Run(_call);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(MongodbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(("backup schedules; " + MongodbStatements.NO_SNAPSHOTS));
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(MongodbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          _switchResult = this.collectionLimit(((CollectionLimit)concept), true);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((MongodbStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant, namespace or partition deletes its documents and its view.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String kind, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(collection);
      final String fieldName = this.sliceField(target, kind);
      if ((fieldName == null)) {
        throw Unsupported.of((((("a " + kind) + " of a collection without ") + kind) + "s"));
      }
      String _replaceAll = value.replaceAll("[^A-Za-z0-9_]", "_");
      String _plus = ((((target.name + "_") + kind) + "_") + _replaceAll);
      final String name = MongodbCommands.checkedName(_plus, "view");
      Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("drop", MongodbCommands.text(name)), "ignore", MongodbCommands.arr(MongodbCommands.text("NamespaceNotFound")));
      Code.Run _run = new Code.Run(_call);
      Code.Call _deleteCall = this.deleteCall(target, MongodbCommands.obj(fieldName, MongodbCommands.obj("$eq", MongodbCommands.text(value))), null, null);
      Code.Return _return = new Code.Return(_deleteCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _return));
    }
    return _xblockexpression;
  }

  /**
   * An alias (a view) points at another collection after one collMod.
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection next = this.schema.collection(it.getNewCollection());
      final MongodbCollection old = this.schema.collection(it.getOldCollection());
      boolean _notEquals = (!Objects.equals(next.database, old.database));
      if (_notEquals) {
        throw Unsupported.of("swapping an alias between databases; a view reads a collection of its own database");
      }
      Code.Call _command = MongodbCommands.command(next.database, MongodbCommands.obj("collMod", MongodbCommands.text(MongodbCommands.checkedName(it.getAlias().getName(), "view")), "viewOn", MongodbCommands.text(next.name), "pipeline", MongodbCommands.arr()));
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
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      Code.Call _writeCall = this.writeCall(_collection, _source, _xifexpression, null, it, this.ttlOf(it));
      Code.Return _return = new Code.Return(_writeCall);
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
      boolean _written = ModelUtil.written(it, "batchSize");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getBatchSize());
      } else {
        _xifexpression = null;
      }
      Code.Call _writeCall = this.writeCall(_collection, _source, "replace", _xifexpression, it, this.ttlOf(it));
      Code.Return _return = new Code.Return(_writeCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Integer ttlOf(final EObject statement) {
    Integer _xifexpression = null;
    boolean _written = ModelUtil.written(statement, "ttlSeconds");
    if (_written) {
      Object _feature = this.feature(statement, "ttlSeconds");
      _xifexpression = ((Integer) _feature);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  /**
   * One write of the records with the slice fields of the statement, the defaults and the coercions of the collection.
   */
  private Code.Call writeCall(final VectorCollection collection, final RecordSource recordSource, final String policy, final Integer batch, final EObject statement, final Integer ttl) {
    Code.Call _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(collection);
      final Map<String, String> mode = MongodbCommands.writeMode(policy);
      if ((((ttl != null) && (target.ttlSeconds == null)) && (!target.expiring))) {
        throw Unsupported.of("ttl on a write into a collection declared without ttl; declare ttl on the collection, which creates the TTL index on vecdsl_expires");
      }
      Code.Num _xifexpression = null;
      if ((ttl != null)) {
        _xifexpression = MongodbCommands.num((ttl).intValue());
      } else {
        Code.Num _xifexpression_1 = null;
        if (((target.ttlSeconds != null) && (collection.getTtlField() == null))) {
          _xifexpression_1 = MongodbCommands.num(target.ttlSeconds.doubleValue());
        } else {
          _xifexpression_1 = null;
        }
        _xifexpression = _xifexpression_1;
      }
      final Code.Num ttlSeconds = _xifexpression;
      Code.Text _text = MongodbCommands.text(target.database);
      Code.Text _text_1 = MongodbCommands.text(target.name);
      Code.Expr _recordsOf = this.recordsOf(recordSource, target);
      Code.Text _text_2 = MongodbCommands.text(target.key);
      Code.Text _text_3 = MongodbCommands.text(mode.get("op"));
      Code.Text _text_4 = MongodbCommands.text(mode.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(statement, collection, target);
      Code.Entries _defaults = target.defaults();
      Code.Expr _listOrNull = this.listOrNull(target.names(true, false));
      Code.Expr _listOrNull_1 = this.listOrNull(target.names(false, true));
      Code.Flag _xifexpression_2 = null;
      if (target.versioned) {
        _xifexpression_2 = MongodbCommands.flag(true);
      } else {
        _xifexpression_2 = null;
      }
      Code.Num _xifexpression_3 = null;
      if ((batch == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = MongodbCommands.num((batch).intValue());
      }
      Code.Flag _xifexpression_4 = null;
      boolean _containsKey = mode.containsKey("ignore");
      if (_containsKey) {
        _xifexpression_4 = MongodbCommands.flag(true);
      } else {
        _xifexpression_4 = null;
      }
      _xblockexpression = MongodbCommands.call("write", "database", _text, "collection", _text_1, "records", _recordsOf, "key", _text_2, 
        "op", _text_3, "counter", _text_4, "extra", _sliceFields, "defaults", _defaults, 
        "vectors", _listOrNull, "dates", _listOrNull_1, "versioned", _xifexpression_2, 
        "ttl_seconds", ttlSeconds, "batch_size", _xifexpression_3, "ignore_conflicts", _xifexpression_4, 
        "visible", this.visible(statement, collection, target));
    }
    return _xblockexpression;
  }

  private Code.Expr listOrNull(final List<Code.Expr> values) {
    Code.Items _xifexpression = null;
    boolean _isEmpty = values.isEmpty();
    if (_isEmpty) {
      _xifexpression = null;
    } else {
      _xifexpression = MongodbCommands.arr(values);
    }
    return _xifexpression;
  }

  /**
   * What the runtime waits for after a write: mongot shows it in every search index (with 'wait' or consistency strong).
   */
  private Code.Entries visible(final EObject statement, final VectorCollection collection, final MongodbCollection target) {
    Code.Entries _xblockexpression = null;
    {
      final boolean waited = (ModelUtil.written(statement, "waitForPersistence") || (ModelUtil.written(collection, "consistency") && Objects.equals(collection.getConsistency(), EConsistencyLevel.STRONG)));
      if (((!waited) || target.probes().items().isEmpty())) {
        return null;
      }
      _xblockexpression = MongodbCommands.obj("probes", target.probes(), "timeout_seconds", MongodbCommands.num(MongodbCommands.AWAIT_SECONDS));
    }
    return _xblockexpression;
  }

  /**
   * The slice fields of a write: tenant, namespace and partition; partitioned collections default to the default partition.
   */
  private Code.Entries sliceFields(final EObject statement, final VectorCollection collection, final MongodbCollection target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.put(target.tenant, MongodbCommands.text(tenant.getName()));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        result.put(target.namespace, MongodbCommands.text(((String) namespace)));
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_1);
      if (((partition != null) && (target.partition == null))) {
        throw Unsupported.of("a partition of a collection declared without partitions");
      }
      if ((target.partition != null)) {
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
          _elvis = MongodbCommands.DEFAULT_PARTITION;
        }
        result.put(target.partition, MongodbCommands.text(_elvis));
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
      final MongodbCollection target = this.schema.collection(it.getCollection());
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if ((((((((((it.getFilter() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty())) || ModelUtil.written(it, "ifVersion")) || (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) || (it.getTenant() != null)) || (it.getNamespace() != null)) || (it.getPartition() != null))) {
          throw Unsupported.of("update from records with a filter, a slice, field clauses, if-version or mode replace; update commands without upsert address documents by _id, and each record carries the values it sets");
        }
        Code.Text _text = MongodbCommands.text(target.database);
        Code.Text _text_1 = MongodbCommands.text(target.name);
        Code.Expr _recordsOf = this.recordsOf(it.getSource(), target);
        Code.Text _text_2 = MongodbCommands.text(target.key);
        Code.Text _text_3 = MongodbCommands.text("patch");
        Code.Text _text_4 = MongodbCommands.text("update_count");
        Code.Expr _listOrNull = this.listOrNull(target.names(true, false));
        Code.Expr _listOrNull_1 = this.listOrNull(target.names(false, true));
        Code.Flag _xifexpression = null;
        if (target.versioned) {
          _xifexpression = MongodbCommands.flag(true);
        } else {
          _xifexpression = null;
        }
        final Function1<Literal, Code.Expr> _function = (Literal i) -> {
          return Literals.of(i);
        };
        Code.Call _call = MongodbCommands.call("write", "database", _text, "collection", _text_1, "records", _recordsOf, 
          "key", _text_2, "op", _text_3, "counter", _text_4, "vectors", _listOrNull, 
          "dates", _listOrNull_1, "versioned", _xifexpression, "visible", this.visible(it, it.getCollection(), target), 
          "only", Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function))));
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      final Code.Entries assignments = Code.entries();
      final Code.Entries appended = Code.entries();
      final Code.Entries pulled = Code.entries();
      final Code.Entries removed = Code.entries();
      final List<String> given = CollectionLiterals.<String>newArrayList();
      RecordLiteral _values = it.getValues();
      boolean _tripleNotEquals_1 = (_values != null);
      if (_tripleNotEquals_1) {
        EList<RecordEntry> _entries = it.getValues().getEntries();
        for (final RecordEntry entry : _entries) {
          {
            String _fieldName = ModelUtil.fieldName(entry.getField());
            boolean _equals = Objects.equals(_fieldName, target.key);
            if (_equals) {
              throw Unsupported.of("updating the primary key; the key is _id, which cannot change");
            }
            final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(entry.getField()));
            String _name = mapped.name();
            given.add(_name);
            final Code.Expr converted = this.fieldValue(entry.getField(), entry.getValue());
            boolean _contains = it.getAppendFields().contains(entry.getField());
            if (_contains) {
              appended.put(mapped.name(), MongodbCommands.obj("$each", this.listOf(converted)));
            } else {
              boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
              if (_contains_1) {
                pulled.put(mapped.name(), MongodbCommands.obj("$in", this.listOf(converted)));
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
          String _fieldName_1 = ModelUtil.fieldName(removedField);
          boolean _equals = Objects.equals(_fieldName_1, target.key);
          if (_equals) {
            throw Unsupported.of("removing the primary key");
          }
          removed.put(target.field(ModelUtil.fieldName(removedField)).name(), MongodbCommands.text(""));
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        for (final MongodbCollection.Field mapped : target.fields) {
          if ((((!Objects.equals(mapped.name(), target.key)) && (mapped.vector() == null)) && (!given.contains(mapped.name())))) {
            removed.put(mapped.name(), MongodbCommands.text(""));
          }
        }
      }
      if ((((assignments.entries().isEmpty() && appended.entries().isEmpty()) && pulled.entries().isEmpty()) && removed.entries().isEmpty())) {
        throw Unsupported.of("an update without \'set\' or \'remove-fields\'");
      }
      Code.Entries _xifexpression_1 = null;
      boolean _isEmpty = assignments.entries().isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = assignments;
      }
      Code.Entries _xifexpression_2 = null;
      boolean _isEmpty_1 = removed.entries().isEmpty();
      if (_isEmpty_1) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = removed;
      }
      Code.Entries _xifexpression_3 = null;
      boolean _isEmpty_2 = appended.entries().isEmpty();
      if (_isEmpty_2) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = appended;
      }
      Code.Entries _xifexpression_4 = null;
      boolean _isEmpty_3 = pulled.entries().isEmpty();
      if (_isEmpty_3) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = pulled;
      }
      Code.Entries _xifexpression_5 = null;
      if (target.versioned) {
        _xifexpression_5 = MongodbCommands.obj(MongodbCommands.VERSION_FIELD, MongodbCommands.num(1));
      } else {
        _xifexpression_5 = null;
      }
      final Code.Entries change = MongodbCommands.obj("$set", _xifexpression_1, "$unset", _xifexpression_2, 
        "$push", _xifexpression_3, "$pull", _xifexpression_4, 
        "$inc", _xifexpression_5);
      final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), this.idsOf(it.getIds()), false);
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        Code.Entries _obj = MongodbCommands.obj(MongodbCommands.VERSION_FIELD, MongodbCommands.obj("$eq", MongodbCommands.num(it.getIfVersion())));
        conditions.add(_obj);
      }
      Code.Call _call_1 = MongodbCommands.call("update", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "filter", this.matchOf(conditions), "update", change, 
        "vectors", this.listOrNull(target.names(true, false)), "dates", this.listOrNull(target.names(false, true)), "visible", this.visible(it, it.getCollection(), target));
      Code.Return _return_1 = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final MongodbCollection target = this.schema.collection(it.getCollection());
      if (((it.getIds().isEmpty() && (it.getFilter() == null)) && (!it.isDeleteAll()))) {
        throw Unsupported.of("a delete without ids, filter or \'all\'");
      }
      final Code.Entries matching = this.matchOf(this.clauses(it, it.getCollection(), target, it.getFilter(), this.idsOf(it.getIds()), false));
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getDeleteLimit());
      } else {
        _xifexpression = null;
      }
      Code.Call _deleteCall = this.deleteCall(target, matching, _xifexpression, this.visible(it, it.getCollection(), target));
      Code.Return _return = new Code.Return(_deleteCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Call deleteCall(final MongodbCollection target, final Code.Entries matching, final Integer limit, final Code.Entries visibleSpec) {
    Code.Text _text = MongodbCommands.text(target.database);
    Code.Text _text_1 = MongodbCommands.text(target.name);
    Code.Num _xifexpression = null;
    if ((limit == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = MongodbCommands.num((limit).intValue());
    }
    return MongodbCommands.call("delete", "database", _text, "collection", _text_1, "filter", matching, "limit", _xifexpression, "visible", visibleSpec);
  }

  /**
   * The runtime reads a local JSON or JSON Lines file and writes it like an insert, batch-size documents per command.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; MongoDB pulls no streams itself (Atlas Stream Processing is an Atlas service)");
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
        throw Unsupported.of((("import from " + scheme) + "; MongoDB stores documents that a client sends (mongoimport and Atlas Data Federation are outside the database commands)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + MongodbStatements.PENDING) + " (CSV records in the runtime)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the generated programs read JSON documents");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async");
      Set<String> _keySet = ModelUtil.extParams(it, MongodbGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final MongodbCollection target = this.schema.collection(it.getCollection());
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final Map<String, String> mode = MongodbCommands.writeMode(_xifexpression_1);
      Code.Text _text = MongodbCommands.text(it.getLocation());
      Code.Text _text_1 = MongodbCommands.text(target.database);
      Code.Text _text_2 = MongodbCommands.text(target.name);
      Code.Text _text_3 = MongodbCommands.text(target.key);
      Code.Text _text_4 = MongodbCommands.text(mode.get("op"));
      Code.Text _text_5 = MongodbCommands.text(mode.get("counter"));
      Code.Entries _sliceFields = this.sliceFields(it, it.getCollection(), target);
      Code.Entries _defaults = target.defaults();
      Code.Expr _listOrNull = this.listOrNull(target.names(true, false));
      Code.Expr _listOrNull_1 = this.listOrNull(target.names(false, true));
      Code.Flag _xifexpression_2 = null;
      if (target.versioned) {
        _xifexpression_2 = MongodbCommands.flag(true);
      } else {
        _xifexpression_2 = null;
      }
      Code.Num _xifexpression_3 = null;
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        _xifexpression_3 = MongodbCommands.num(it.getBatchSize());
      } else {
        _xifexpression_3 = null;
      }
      Code.Flag _xifexpression_4 = null;
      boolean _containsKey = mode.containsKey("ignore");
      if (_containsKey) {
        _xifexpression_4 = MongodbCommands.flag(true);
      } else {
        _xifexpression_4 = null;
      }
      Code.Call _call = MongodbCommands.call("import_file", "location", _text, "database", _text_1, "collection", _text_2, "key", _text_3, 
        "op", _text_4, "counter", _text_5, "extra", _sliceFields, "defaults", _defaults, 
        "vectors", _listOrNull, "dates", _listOrNull_1, "versioned", _xifexpression_2, 
        "batch_size", _xifexpression_3, "ignore_conflicts", _xifexpression_4);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The writes of the transaction as steps of one session: startTransaction, the commands, commitTransaction (abort on error).
   */
  protected List<Code.Stmt> _body(final Transaction it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      EList<DmlMethod> _methods = it.getMethods();
      for (final DmlMethod statement : _methods) {
        {
          if ((statement instanceof BulkImport)) {
            throw Unsupported.of("bulk-import inside a transaction; an import is a sequence of batches, not one step");
          }
          final List<Code.Stmt> inner = this.body(statement);
          if (((inner.size() != 1) || (!(IterableExtensions.<Code.Stmt>head(inner) instanceof Code.Return)))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          Code.Stmt _head = IterableExtensions.<Code.Stmt>head(inner);
          Code.Expr _value = ((Code.Return) _head).value();
          final Code.Call invocation = ((Code.Call) _value);
          Code.Entries _obj = MongodbCommands.obj("op", MongodbCommands.text(invocation.op()), "args", invocation.args());
          steps.add(_obj);
        }
      }
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "isolationLevel");
      if (_written) {
        String _switchResult = null;
        String _literal = it.getIsolationLevel().getLiteral();
        if (_literal != null) {
          switch (_literal) {
            case "serializable":
              throw Unsupported.of("serializable isolation; MongoDB transactions read a snapshot and detect write-write conflicts only (snapshot isolation)");
            case "read_committed":
              _switchResult = "majority";
              break;
            case "read_uncommitted":
              _switchResult = "local";
              break;
            default:
              _switchResult = "snapshot";
              break;
          }
        } else {
          _switchResult = "snapshot";
        }
        _xifexpression = _switchResult;
      } else {
        _xifexpression = "snapshot";
      }
      final String concern = _xifexpression;
      Code.Call _call = MongodbCommands.call("transaction", "steps", MongodbCommands.arr(steps), "read_concern", MongodbCommands.text(concern));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Write consistency is the server's (write concern majority); extensions on writes are rejected.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, MongodbGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  protected List<Code.Stmt> _body(final Load it) {
    throw Unsupported.of(("loading a collection; " + MongodbStatements.NO_RESIDENCY));
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of(("releasing a collection; " + MongodbStatements.NO_RESIDENCY));
  }

  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      Code.Call _command = MongodbCommands.command("admin", MongodbCommands.obj("fsync", MongodbCommands.num(1)));
      Code.Run _run = new Code.Run(_command);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.operationExtensions(it);
      boolean _written = ModelUtil.written(it, "targetSegments");
      if (_written) {
        throw Unsupported.of("the \'target-segments\' clause; WiredTiger compacts collection files and has no segments");
      }
      this.schema.note(it, "async");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("compact", MongodbCommands.text(target.name), "force", MongodbCommands.flag(true)), "select", MongodbCommands.arr(MongodbCommands.text("bytesFreed")));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final Code.Call info = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("listCollections", MongodbCommands.num(1), "filter", MongodbCommands.obj("name", MongodbCommands.obj("$eq", MongodbCommands.text(target.name)))), 
        "select", MongodbCommands.arr(MongodbCommands.text("cursor"), MongodbCommands.text("firstBatch"), MongodbCommands.text("0"), MongodbCommands.text("options")));
      if (((!it.isIncludeStats()) && (!it.isIncludeIndexProgress()))) {
        Code.Return _return = new Code.Return(info);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      final List<Code.Expr> parts = CollectionLiterals.<Code.Expr>newArrayList();
      parts.add(info);
      boolean _isIncludeStats = it.isIncludeStats();
      if (_isIncludeStats) {
        Code.Call _aggregateCall = this.aggregateCall(target, MongodbCommands.arr(MongodbCommands.obj("$collStats", MongodbCommands.obj("storageStats", Code.entries())), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "count", MongodbCommands.text("$storageStats.count"), 
          "size", MongodbCommands.text("$storageStats.size"), "nindexes", MongodbCommands.text("$storageStats.nindexes")))), null, true);
        parts.add(_aggregateCall);
      }
      boolean _isIncludeIndexProgress = it.isIncludeIndexProgress();
      if (_isIncludeIndexProgress) {
        Code.Call _aggregateCall_1 = this.aggregateCall(target, MongodbCommands.arr(MongodbCommands.obj("$listSearchIndexes", Code.entries()), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "name", MongodbCommands.num(1), "type", MongodbCommands.num(1), 
          "status", MongodbCommands.num(1), "queryable", MongodbCommands.num(1))), MongodbCommands.obj("$sort", MongodbCommands.obj("name", MongodbCommands.num(1)))), null, false);
        parts.add(_aggregateCall_1);
      }
      Code.Items _items = Code.items(parts);
      Code.Return _return_1 = new Code.Return(_items);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + MongodbStatements.NO_MODELS));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(MongodbStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(("restoring " + MongodbStatements.NO_SNAPSHOTS));
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(("dropping " + MongodbStatements.NO_SNAPSHOTS));
  }

  /**
   * A copy with the source's validator, documents ($out), payload indexes and search indexes.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; the program talks to one deployment, and copying a database between deployments is a mongodump and mongorestore job");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = MongodbStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      this.operationExtensions(it);
      this.schema.note(it, "mode");
      final MongodbCollection source = this.schema.collection(it.getSourceCollection());
      final MongodbCollection copy = source.copy();
      copy.name = MongodbCommands.checkedName(it.getTargetName(), "collection");
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _command = MongodbCommands.command(copy.database, MongodbCommands.obj("create", MongodbCommands.text(copy.name), "validator", copy.validator(), "validationLevel", MongodbCommands.text("strict"), "validationAction", MongodbCommands.text("error")));
      Code.Run _run = new Code.Run(_command);
      result.add(_run);
      Code.Call _aggregateCall = this.aggregateCall(source, MongodbCommands.arr(MongodbCommands.obj("$out", MongodbCommands.obj("db", MongodbCommands.text(copy.database), "coll", MongodbCommands.text(copy.name)))), null, false);
      Code.Run _run_1 = new Code.Run(_aggregateCall);
      result.add(_run_1);
      List<Code.Stmt> _indexStatements = this.schema.indexStatements(copy, null);
      Iterables.<Code.Stmt>addAll(result, _indexStatements);
      Code.Call _aggregateCall_1 = this.aggregateCall(copy, MongodbCommands.arr(MongodbCommands.obj("$count", MongodbCommands.text("copied"))), null, true);
      Code.Return _return = new Code.Return(_aggregateCall_1);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * mongot rebuilds a search index for an updated definition; a payload index is dropped and created again.
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
      final MongodbCollection target = this.schema.collection(collection);
      List<Code.Stmt> _switchResult = null;
      Index _index = it.getIndex();
      boolean _matched = false;
      if (_index instanceof VectorIndex) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          VectorField _elvis = null;
          Index _index_1 = it.getIndex();
          VectorField _field = ((VectorIndex) _index_1).getField();
          if (_field != null) {
            _elvis = _field;
          } else {
            VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(it.getIndex(), VectorField.class);
            _elvis = _containerOfType;
          }
          final VectorField owner = _elvis;
          final Code.Entries definition = target.vectorDefinition(target.vectorIndexOf(target.field(ModelUtil.fieldName(owner)).name()));
          Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("updateSearchIndex", MongodbCommands.text(target.name), "name", MongodbCommands.text(it.getIndex().getName()), "definition", definition));
          Code.Run _run = new Code.Run(_command);
          Code.Stmt _awaitIndex = this.schema.awaitIndex(target, it.getIndex().getName(), MongodbCommands.AWAIT_SECONDS);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run), _awaitIndex));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (_index instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            Index _index_1 = it.getIndex();
            final Code.Entries keys = this.schema.payloadIndexKeys(target, ((PayloadIndex) _index_1));
            List<Code.Stmt> _xifexpression = null;
            if ((keys == null)) {
              Code.Call _command = MongodbCommands.command(target.database, MongodbCommands.obj("updateSearchIndex", MongodbCommands.text(target.name), "name", MongodbCommands.text(MongodbCommands.TEXT_INDEX), "definition", target.textDefinition()));
              Code.Run _run = new Code.Run(_command);
              Code.Stmt _awaitIndex = this.schema.awaitIndex(target, MongodbCommands.TEXT_INDEX, MongodbCommands.AWAIT_SECONDS);
              _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run), _awaitIndex));
            } else {
              Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("dropIndexes", MongodbCommands.text(target.name), "index", MongodbCommands.text(it.getIndex().getName())), "ignore", MongodbCommands.arr(MongodbCommands.text("IndexNotFound")));
              Code.Run _run_1 = new Code.Run(_call);
              Code.Call _command_1 = MongodbCommands.command(target.database, MongodbCommands.obj("createIndexes", MongodbCommands.text(target.name), "indexes", MongodbCommands.arr(MongodbCommands.obj("key", keys, "name", MongodbCommands.text(it.getIndex().getName())))));
              Code.Run _run_2 = new Code.Run(_command_1);
              _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run_1), _run_2));
            }
            _xblockexpression_1 = _xifexpression;
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = it.getIndex().eClass().getName();
        String _plus = (MongodbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(MongodbStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final MongodbCollection target = this.schema.collection(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        if ((target.namespace == null)) {
          throw Unsupported.of("a namespace of a collection without namespaces");
        }
        Code.Call _deleteCall = this.deleteCall(target, MongodbCommands.obj(target.namespace, MongodbCommands.obj("$eq", MongodbCommands.text(it.getNamespace()))), null, null);
        Code.Return _return = new Code.Return(_deleteCall);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      Code.Call _deleteCall_1 = this.deleteCall(target, Code.entries(), null, null);
      Code.Return _return_1 = new Code.Return(_deleteCall_1);
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
        _xifexpression = MongodbCommands.num(it.getListLimit());
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
        _xifexpression_1 = MongodbCommands.text(it.getPrefix());
      }
      final Code.Text prefixValue = _xifexpression_1;
      Code.Call _switchResult = null;
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            Code.Call _xblockexpression_1 = null;
            {
              String _xifexpression_2 = null;
              if ((owner instanceof Database)) {
                _xifexpression_2 = MongodbCommands.checkedName(((Database) owner).getName(), "database");
              } else {
                String _xblockexpression_2 = null;
                {
                  if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                    String _name = owner.eClass().getName();
                    String _plus = ("listing collections in a " + _name);
                    String _plus_1 = (_plus + "; MongoDB lists the collections of a database");
                    throw Unsupported.of(_plus_1);
                  }
                  _xblockexpression_2 = this.schema.defaultDatabase();
                }
                _xifexpression_2 = _xblockexpression_2;
              }
              final String database = _xifexpression_2;
              _xblockexpression_1 = MongodbCommands.call("names", "kind", MongodbCommands.text("collections"), "database", MongodbCommands.text(database), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_1;
            break;
          case DATABASES:
            Code.Call _xblockexpression_2 = null;
            {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing databases in a " + _name);
                throw Unsupported.of(_plus);
              }
              _xblockexpression_2 = MongodbCommands.call("names", "kind", MongodbCommands.text("databases"), "database", MongodbCommands.text("admin"), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_2;
            break;
          case INDEXES:
            Code.Call _xblockexpression_3 = null;
            {
              final MongodbCollection target = this.scopeCollection(owner, "indexes");
              _xblockexpression_3 = MongodbCommands.call("names", "kind", MongodbCommands.text("search_indexes"), "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_3;
            break;
          case ALIASES:
            Code.Call _xblockexpression_4 = null;
            {
              MongodbCollection _xifexpression_2 = null;
              if ((owner == null)) {
                _xifexpression_2 = null;
              } else {
                _xifexpression_2 = this.scopeCollection(owner, "aliases");
              }
              final MongodbCollection target = _xifexpression_2;
              Code.Text _text = MongodbCommands.text("views");
              String _elvis = null;
              String _database = null;
              if (target!=null) {
                _database=target.database;
              }
              if (_database != null) {
                _elvis = _database;
              } else {
                String _defaultDatabase = this.schema.defaultDatabase();
                _elvis = _defaultDatabase;
              }
              Code.Text _text_1 = MongodbCommands.text(_elvis);
              Code.Text _xifexpression_3 = null;
              if ((target == null)) {
                _xifexpression_3 = null;
              } else {
                _xifexpression_3 = MongodbCommands.text(target.name);
              }
              _xblockexpression_4 = MongodbCommands.call("names", "kind", _text, "database", _text_1, "collection", _xifexpression_3, 
                "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_4;
            break;
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            Code.Call _xblockexpression_5 = null;
            {
              final MongodbCollection target = this.scopeCollection(owner, it.getKind().getLiteral());
              String _switchResult_1 = null;
              EListKind _kind_1 = it.getKind();
              if (_kind_1 != null) {
                switch (_kind_1) {
                  case TENANTS:
                    _switchResult_1 = target.tenant;
                    break;
                  case NAMESPACES:
                    _switchResult_1 = target.namespace;
                    break;
                  default:
                    _switchResult_1 = target.partition;
                    break;
                }
              } else {
                _switchResult_1 = target.partition;
              }
              final String fieldName = _switchResult_1;
              if ((fieldName == null)) {
                String _literal = it.getKind().getLiteral();
                String _plus = ("listing " + _literal);
                String _plus_1 = (_plus + " of a collection without ");
                String _literal_1 = it.getKind().getLiteral();
                String _plus_2 = (_plus_1 + _literal_1);
                throw Unsupported.of(_plus_2);
              }
              _xblockexpression_5 = MongodbCommands.call("names", "kind", MongodbCommands.text("distinct"), "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "field", MongodbCommands.text(fieldName), "prefix", prefixValue, "limit", limit);
            }
            _switchResult = _xblockexpression_5;
            break;
          case SNAPSHOTS:
            throw Unsupported.of(("listing " + MongodbStatements.NO_SNAPSHOTS));
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

  private MongodbCollection scopeCollection(final Concept owner, final String what) {
    MongodbCollection _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.collection(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * mongot builds search indexes asynchronously: the runtime polls $listSearchIndexes until the index is queryable.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state");
      final VectorCollection collection = this.indexCollection(it.getIndex());
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      final MongodbCollection target = this.schema.collection(collection);
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "timeoutSeconds");
      if (_written) {
        _xifexpression = it.getTimeoutSeconds();
      } else {
        _xifexpression = MongodbCommands.AWAIT_SECONDS;
      }
      final int seconds = _xifexpression;
      if (((it.getIndex() instanceof PayloadIndex) && (this.schema.payloadIndexKeys(target, ((PayloadIndex) it.getIndex())) != null))) {
        Code.Call _call = MongodbCommands.call("command", "database", MongodbCommands.text(target.database), "command", MongodbCommands.obj("listIndexes", MongodbCommands.text(target.name)), 
          "select", MongodbCommands.arr(MongodbCommands.text("cursor"), MongodbCommands.text("firstBatch")));
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      String _switchResult = null;
      Index _index = it.getIndex();
      boolean _matched = false;
      if (_index instanceof VectorIndex) {
        _matched=true;
        Field _elvis = null;
        Index _index_1 = it.getIndex();
        VectorField _field = ((VectorIndex) _index_1).getField();
        if (_field != null) {
          _elvis = _field;
        } else {
          VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(it.getIndex(), VectorField.class);
          _elvis = _containerOfType;
        }
        _switchResult = target.vectorIndexOf(target.field(ModelUtil.fieldName(_elvis)).name()).name();
      }
      if (!_matched) {
        if (_index instanceof PayloadIndex) {
          _matched=true;
          _switchResult = MongodbCommands.TEXT_INDEX;
        }
      }
      if (!_matched) {
        String _name = it.getIndex().eClass().getName();
        String _plus = (MongodbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final String indexName = _switchResult;
      Code.Call _call_1 = MongodbCommands.call("await_index", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "index", MongodbCommands.text(indexName), "timeout_seconds", MongodbCommands.num(seconds));
      Code.Return _return_1 = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
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
    Set<String> _keySet = ModelUtil.extParams(statement, MongodbGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "elementScope", "asOf", "cursor", "reranker", "queryModel", "prefetches", "explain", "highlighted");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(searched));
      QueryInput _input = it.getInput();
      boolean _tripleEquals = (_input == null);
      if (_tripleEquals) {
        throw Unsupported.of("a vector query without input");
      }
      final Code.Expr queryVector = this.queryVectorOf(it.getInput(), mapped);
      final boolean nativeScore = (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE));
      final ShapeSpec shapes = this.moduled(it);
      int _xifexpression = (int) 0;
      if ((shapes == null)) {
        int _topK = it.getTopK();
        int _offsetOf = this.offsetOf(it);
        _xifexpression = (_topK + _offsetOf);
      } else {
        _xifexpression = this.windowOf(shapes);
      }
      final int k = _xifexpression;
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression_1 = it.getMetric().getLiteral();
      } else {
        _xifexpression_1 = mapped.vector().metric();
      }
      final String searchMetric = _xifexpression_1;
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      String _metric = mapped.vector().metric();
      boolean _notEquals = (!Objects.equals(searchMetric, _metric));
      if (_notEquals) {
        EQueryInputMode _kind = it.getInput().getKind();
        boolean _equals = Objects.equals(_kind, EQueryInputMode.ID);
        if (_equals) {
          throw Unsupported.of((("a query by stored vector with another metric than the field\'s; " + MongodbStatements.PENDING) + " (the stored vector in the exact scan)"));
        }
        if (nativeScore) {
          this.notes.add("score native on VectorSearch, whose exact scan computes the canonical similarity directly");
        }
        Code.Entries _obj = MongodbCommands.obj("$match", this.matchOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null, false)));
        stages.add(_obj);
        Code.Entries _obj_1 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.exactScore(searchMetric, mapped.name(), queryVector)));
        stages.add(_obj_1);
        if ((shapes != null)) {
          Code.Call _shapedCall = this.shapedCall(this.candidateWindow(target, stages, shapes, false, false, this.options(it, it.getParams())), shapes, target);
          Code.Return _return = new Code.Return(_shapedCall);
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
        }
        Code.Call _ranked = this.ranked(it, it.getCollection(), target, stages, true, false, this.options(it, it.getParams()));
        Code.Return _return_1 = new Code.Return(_ranked);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      final SearchParams searchParams = it.getParams();
      final boolean exhaustive = (it.isExact() || ((searchParams != null) && searchParams.isExhaustive()));
      final boolean postFilter = (((searchParams != null) && ModelUtil.written(searchParams, "filterStrategy")) && Objects.equals(searchParams.getFilterStrategy().getLiteral(), "post_filter"));
      if ((searchParams != null)) {
        boolean _written_1 = ModelUtil.written(searchParams, "targetRecall");
        if (_written_1) {
          throw Unsupported.of("target-recall: numCandidates is an effort setting, with no declarable recall target");
        }
        boolean _written_2 = ModelUtil.written(searchParams, "probes");
        if (_written_2) {
          throw Unsupported.of("probes; mongot builds hnsw and flat indexes, with no clusters to probe");
        }
        boolean _written_3 = ModelUtil.written(searchParams, "scanBudget");
        if (_written_3) {
          throw Unsupported.of("scan-budget; $vectorSearch takes numCandidates and no bound on visited vectors");
        }
        if ((ModelUtil.written(searchParams, "oversampling") || searchParams.isRescore())) {
          throw Unsupported.of("oversampling and rescore settings; mongot rescores quantized vectors itself, and numCandidates is the effort setting");
        }
        Set<String> _keySet = ModelUtil.extParams(searchParams, MongodbGenerator.VENDOR).keySet();
        for (final String key : _keySet) {
          throw Unsupported.of(("extension parameter " + key));
        }
      }
      final MongodbCollection.VectorIndex index = target.vectorIndexOf(mapped.name());
      Integer _xifexpression_2 = null;
      if (((searchParams != null) && ModelUtil.written(searchParams, "candidatePool"))) {
        _xifexpression_2 = Integer.valueOf(searchParams.getCandidatePool());
      } else {
        _xifexpression_2 = index.searchCandidates();
      }
      final Integer pool = _xifexpression_2;
      VectorCollection _collection = it.getCollection();
      FilterBlock _xifexpression_3 = null;
      if (postFilter) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = it.getFilter();
      }
      final List<Code.Expr> conditions = this.clauses(it, _collection, target, _xifexpression_3, null, true);
      if ((Objects.equals(it.getInput().getKind(), EQueryInputMode.ID) && (it.getInput().getLookupCollection() == null))) {
        Code.Entries _obj_2 = MongodbCommands.obj("_id", MongodbCommands.obj("$ne", Literals.of(it.getInput().getValue())));
        conditions.add(_obj_2);
      }
      Code.Text _text = MongodbCommands.text(index.name());
      Code.Text _text_1 = MongodbCommands.text(mapped.name());
      Code.Num _xifexpression_4 = null;
      if (exhaustive) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = MongodbCommands.num(MongodbCommands.candidates(pool, k));
      }
      Code.Num _num = MongodbCommands.num(k);
      Code.Entries _xifexpression_5 = null;
      boolean _isEmpty = conditions.isEmpty();
      if (_isEmpty) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = this.matchOf(conditions);
      }
      Code.Flag _xifexpression_6 = null;
      if (exhaustive) {
        _xifexpression_6 = MongodbCommands.flag(true);
      } else {
        _xifexpression_6 = null;
      }
      Code.Entries _obj_3 = MongodbCommands.obj("$vectorSearch", MongodbCommands.obj("index", _text, "path", _text_1, "queryVector", queryVector, 
        "numCandidates", _xifexpression_4, "limit", _num, "filter", _xifexpression_5, 
        "exact", _xifexpression_6));
      stages.add(_obj_3);
      if ((postFilter && (it.getFilter() != null))) {
        Code.Entries _obj_4 = MongodbCommands.obj("$match", new MongodbFilter(target, false).query(it.getFilter().getCondition()));
        stages.add(_obj_4);
      }
      Code.Expr _xifexpression_7 = null;
      if (nativeScore) {
        _xifexpression_7 = MongodbCommands.obj("$meta", MongodbCommands.text("vectorSearchScore"));
      } else {
        _xifexpression_7 = MongodbCommands.canonicalScore(mapped.vector().similarity(), "vectorSearchScore");
      }
      Code.Entries _obj_5 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, _xifexpression_7));
      stages.add(_obj_5);
      Code.Call _xifexpression_8 = null;
      if ((shapes == null)) {
        _xifexpression_8 = this.ranked(it, it.getCollection(), target, stages, true, false, this.options(it, searchParams));
      } else {
        _xifexpression_8 = this.candidateWindow(target, stages, shapes, false, false, this.options(it, searchParams));
      }
      final Code.Call search = _xifexpression_8;
      EQueryInputMode _kind_1 = it.getInput().getKind();
      boolean _equals_1 = Objects.equals(_kind_1, EQueryInputMode.ID);
      if (_equals_1) {
        VectorCollection _elvis = null;
        VectorCollection _lookupCollection = it.getInput().getLookupCollection();
        if (_lookupCollection != null) {
          _elvis = _lookupCollection;
        } else {
          VectorCollection _collection_1 = it.getCollection();
          _elvis = _collection_1;
        }
        final MongodbCollection lookup = this.schema.collection(_elvis);
        final Code.Call recommended = this.recommendCall(target, lookup, mapped, search, MongodbCommands.arr(Literals.of(it.getInput().getValue())), null, null, null);
        Code.Call _xifexpression_9 = null;
        if ((shapes == null)) {
          _xifexpression_9 = recommended;
        } else {
          _xifexpression_9 = this.shapedCall(recommended, shapes, target);
        }
        Code.Return _return_2 = new Code.Return(_xifexpression_9);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
      }
      Code.Call _xifexpression_10 = null;
      if ((shapes == null)) {
        _xifexpression_10 = search;
      } else {
        _xifexpression_10 = this.shapedCall(search, shapes, target);
      }
      Code.Return _return_3 = new Code.Return(_xifexpression_10);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_3));
    }
    return _xblockexpression;
  }

  private Code.Entries options(final EObject statement, final SearchParams searchParams) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      if (((searchParams != null) && ModelUtil.written(searchParams, "timeoutMillis"))) {
        result.put("maxTimeMS", MongodbCommands.num(searchParams.getTimeoutMillis()));
      }
      Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(statement, MongodbGenerator.VENDOR).entrySet();
      for (final Map.Entry<String, Literal> entry : _entrySet) {
        {
          boolean _contains = MongodbStatements.AGGREGATE_KEYS.contains(entry.getKey());
          boolean _not = (!_contains);
          if (_not) {
            String _key = entry.getKey();
            String _plus = ("extension parameter " + _key);
            String _plus_1 = (_plus + "; the aggregate options the generator passes are ");
            String _join = IterableExtensions.join(MongodbStatements.AGGREGATE_KEYS, ", ");
            String _plus_2 = (_plus_1 + _join);
            throw Unsupported.of(_plus_2);
          }
          if (((entry.getValue() instanceof ParamRef) || (entry.getValue() instanceof EnvRef))) {
            String _key_1 = entry.getKey();
            String _plus_3 = ("extension parameter " + _key_1);
            String _plus_4 = (_plus_3 + " with a value that is not a literal");
            throw Unsupported.of(_plus_4);
          }
          result.put(entry.getKey(), Literals.of(entry.getValue()));
        }
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

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "cursor", "reranker");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      boolean _written = ModelUtil.written(it, "mmr");
      if (_written) {
        throw Unsupported.of("the \'mmr\' clause on a keyword search; maximal marginal relevance compares the hits\' vectors with a query vector, which a text query has not");
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final MongodbCollection target = this.schema.collection(it.getCollection());
      boolean _hasText = target.hasText();
      boolean _not = (!_hasText);
      if (_not) {
        throw Unsupported.of("keyword search in a collection without full-text fields; $search needs the search index of the full-text fields");
      }
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
      boolean _written_3 = ModelUtil.written(it, "minShouldMatch");
      String _fuzziness = it.getFuzziness();
      Integer _xifexpression_2 = null;
      boolean _written_4 = ModelUtil.written(it, "slop");
      if (_written_4) {
        _xifexpression_2 = Integer.valueOf(it.getSlop());
      } else {
        _xifexpression_2 = null;
      }
      final Code.Entries operator = this.keywordOperator(target, _searchFields, _input, _xifexpression, _xifexpression_1, _written_3, _fuzziness, _xifexpression_2);
      final Code.Entries searchStage = MongodbCommands.obj("index", MongodbCommands.text(MongodbCommands.TEXT_INDEX));
      searchStage.entries().putAll(operator.entries());
      Object _elvis = null;
      Object _feature = this.feature(it, "highlighted");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<Field> highlighted = ((List<Field>) _elvis);
      boolean _isEmpty = highlighted.isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        final List<Code.Expr> paths = CollectionLiterals.<Code.Expr>newArrayList();
        for (final Field f : highlighted) {
          {
            final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(f));
            boolean _fullText = mapped.fullText();
            boolean _not_2 = (!_fullText);
            if (_not_2) {
              String _name = mapped.name();
              String _plus = ("highlighting " + _name);
              String _plus_1 = (_plus + ", which is not a full-text field");
              throw Unsupported.of(_plus_1);
            }
            Code.Text _text = MongodbCommands.text(mapped.name());
            paths.add(_text);
          }
        }
        searchStage.put("highlight", MongodbCommands.obj("path", MongodbCommands.arr(paths)));
      }
      boolean _isExplain = it.isExplain();
      if (_isExplain) {
        searchStage.put("scoreDetails", MongodbCommands.flag(true));
      }
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _obj = MongodbCommands.obj("$search", searchStage);
      stages.add(_obj);
      final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), null, false);
      boolean _isEmpty_1 = conditions.isEmpty();
      boolean _not_2 = (!_isEmpty_1);
      if (_not_2) {
        Code.Entries _obj_1 = MongodbCommands.obj("$match", this.matchOf(conditions));
        stages.add(_obj_1);
      }
      Code.Entries _obj_2 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.obj("$meta", MongodbCommands.text("searchScore"))));
      stages.add(_obj_2);
      final ShapeSpec shapes = this.moduled(it);
      if ((shapes != null)) {
        boolean _isEmpty_2 = highlighted.isEmpty();
        boolean _not_3 = (!_isEmpty_2);
        Code.Call _shapedCall = this.shapedCall(this.candidateWindow(target, stages, shapes, _not_3, it.isExplain(), this.options(it, it.getParams())), shapes, target);
        Code.Return _return = new Code.Return(_shapedCall);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      Code.Call _ranked = this.ranked(it, it.getCollection(), target, stages, true, true, this.options(it, it.getParams()));
      Code.Return _return_1 = new Code.Return(_ranked);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  /**
   * The $search operator of a text query: text (match and fuzzy), phrase with slop, autocomplete (prefix and phrase_prefix),
   * wildcard (term and wildcard), regex and queryString; several fields with boosts become compound should clauses, one per
   * field with a score boost, so the field scores add up.
   */
  private Code.Entries keywordOperator(final MongodbCollection target, final List<SearchFieldRef> refs, final QueryInput queryInput, final String type, final String operator, final boolean minimum, final String fuzzinessValue, final Integer slopValue) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Expr words = this.keywordText(queryInput);
      final List<String> fields = CollectionLiterals.<String>newArrayList();
      final List<Double> boosts = CollectionLiterals.<Double>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        for (final MongodbCollection.Field mapped : target.fields) {
          boolean _fullText = mapped.fullText();
          if (_fullText) {
            String _name = mapped.name();
            fields.add(_name);
            boosts.add(((Double) null));
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final MongodbCollection.Field mapped_1 = target.field(ModelUtil.fieldName(ref.getField()));
            boolean _fullText_1 = mapped_1.fullText();
            boolean _not = (!_fullText_1);
            if (_not) {
              String _name_1 = mapped_1.name();
              String _plus = ("keyword search on " + _name_1);
              String _plus_1 = (_plus + ", which is not a full-text field of the search index");
              throw Unsupported.of(_plus_1);
            }
            String _name_2 = mapped_1.name();
            fields.add(_name_2);
            Double _xifexpression = null;
            boolean _written = ModelUtil.written(ref, "boost");
            if (_written) {
              _xifexpression = Double.valueOf(ref.getBoost());
            } else {
              _xifexpression = null;
            }
            boosts.add(_xifexpression);
          }
        }
      }
      if (minimum) {
        throw Unsupported.of("the \'min-should-match\' clause; the text operator matches any or all terms (matchCriteria), and compound minimumShouldMatch counts clauses, not the terms of one query");
      }
      if (((slopValue != null) && (!Objects.equals(type, "phrase")))) {
        throw Unsupported.of((("the \'slop\' clause with text-query " + type) + "; slop belongs to phrase queries"));
      }
      if (((fuzzinessValue != null) && (!Objects.equals(type, "match")))) {
        throw Unsupported.of((("the \'fuzziness\' clause with text-query " + type) + "; fuzziness belongs to match queries (text-query fuzzy has its own)"));
      }
      if (((operator != null) && (!Objects.equals(type, "match")))) {
        throw Unsupported.of((("the \'operator\' clause with text-query " + type) + "; matchCriteria belongs to the text operator"));
      }
      Code.Num _xifexpression = null;
      if (((fuzzinessValue == null) || Objects.equals(fuzzinessValue, "0"))) {
        _xifexpression = null;
      } else {
        Code.Num _switchResult = null;
        if (fuzzinessValue != null) {
          switch (fuzzinessValue) {
            case "1":
              _switchResult = MongodbCommands.num(1);
              break;
            case "2":
              _switchResult = MongodbCommands.num(2);
              break;
            default:
              throw Unsupported.of((("fuzziness " + fuzzinessValue) + "; the text operator takes maxEdits 1 or 2 and has no length-dependent edit distance"));
          }
        } else {
          throw Unsupported.of((("fuzziness " + fuzzinessValue) + "; the text operator takes maxEdits 1 or 2 and has no length-dependent edit distance"));
        }
        _xifexpression = _switchResult;
      }
      final Code.Num maxEdits = _xifexpression;
      final List<Code.Entries> parts = CollectionLiterals.<Code.Entries>newArrayList();
      final boolean single = (((((((fields.size() == 1) || Objects.equals(type, "match")) || Objects.equals(type, "phrase")) || Objects.equals(type, "fuzzy")) || Objects.equals(type, "wildcard")) || Objects.equals(type, "regexp")) || Objects.equals(type, "term"));
      final boolean perField = (IterableExtensions.<Double>exists(boosts, ((Function1<Double, Boolean>) (Double b) -> {
        return Boolean.valueOf((b != null));
      })) || (!single));
      List<List<String>> _xifexpression_1 = null;
      if (perField) {
        final Function1<String, List<String>> _function = (String f) -> {
          return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(f));
        };
        _xifexpression_1 = IterableExtensions.<List<String>>toList(ListExtensions.<String, List<String>>map(fields, _function));
      } else {
        _xifexpression_1 = Collections.<List<String>>unmodifiableList(CollectionLiterals.<List<String>>newArrayList(fields));
      }
      final List<List<String>> groups = _xifexpression_1;
      for (final List<String> group : groups) {
        {
          Record _xifexpression_2 = null;
          int _size = group.size();
          boolean _equals = (_size == 1);
          if (_equals) {
            _xifexpression_2 = MongodbCommands.text(IterableExtensions.<String>head(group));
          } else {
            final Function1<String, Code.Expr> _function_1 = (String f) -> {
              Code.Text _text = MongodbCommands.text(f);
              return ((Code.Expr) _text);
            };
            _xifexpression_2 = MongodbCommands.arr(IterableExtensions.<Code.Expr>toList(ListExtensions.<String, Code.Expr>map(group, _function_1)));
          }
          final Code.Expr path = ((Code.Expr)_xifexpression_2);
          Code.Entries _switchResult_1 = null;
          if (type != null) {
            switch (type) {
              case "match":
                Code.Text _xifexpression_3 = null;
                if ((operator == null)) {
                  _xifexpression_3 = null;
                } else {
                  String _xifexpression_4 = null;
                  boolean _equals_1 = Objects.equals(operator, "and");
                  if (_equals_1) {
                    _xifexpression_4 = "all";
                  } else {
                    _xifexpression_4 = "any";
                  }
                  _xifexpression_3 = MongodbCommands.text(_xifexpression_4);
                }
                Code.Entries _xifexpression_5 = null;
                if ((maxEdits == null)) {
                  _xifexpression_5 = null;
                } else {
                  _xifexpression_5 = MongodbCommands.obj("maxEdits", maxEdits);
                }
                _switchResult_1 = MongodbCommands.obj("text", MongodbCommands.obj("query", words, "path", path, "matchCriteria", _xifexpression_3, 
                  "fuzzy", _xifexpression_5));
                break;
              case "fuzzy":
                _switchResult_1 = MongodbCommands.obj("text", MongodbCommands.obj("query", words, "path", path, "fuzzy", MongodbCommands.obj("maxEdits", MongodbCommands.num(2))));
                break;
              case "phrase":
                Code.Num _xifexpression_6 = null;
                if ((slopValue == null)) {
                  _xifexpression_6 = null;
                } else {
                  _xifexpression_6 = MongodbCommands.num((slopValue).intValue());
                }
                _switchResult_1 = MongodbCommands.obj("phrase", MongodbCommands.obj("query", words, "path", path, "slop", _xifexpression_6));
                break;
              case "phrase_prefix":
                _switchResult_1 = MongodbCommands.obj("autocomplete", MongodbCommands.obj("query", words, "path", path, "tokenOrder", MongodbCommands.text("sequential")));
                break;
              case "prefix":
                _switchResult_1 = MongodbCommands.obj("autocomplete", MongodbCommands.obj("query", words, "path", path, "tokenOrder", MongodbCommands.text("any")));
                break;
              case "term":
                _switchResult_1 = MongodbCommands.obj("wildcard", MongodbCommands.obj("query", MongodbCommands.text(MongodbFilter.escaped(this.constantText(queryInput, "term")).replace("\\.", ".").replace("\\-", "-")), 
                  "path", path, "allowAnalyzedField", MongodbCommands.flag(true)));
                break;
              case "wildcard":
                _switchResult_1 = MongodbCommands.obj("wildcard", MongodbCommands.obj("query", words, "path", path, "allowAnalyzedField", MongodbCommands.flag(true)));
                break;
              case "regexp":
                _switchResult_1 = MongodbCommands.obj("regex", MongodbCommands.obj("query", words, "path", path, "allowAnalyzedField", MongodbCommands.flag(true)));
                break;
              case "query_string":
                _switchResult_1 = MongodbCommands.obj("queryString", MongodbCommands.obj("defaultPath", path, "query", words));
                break;
              default:
                throw Unsupported.of(("text-query " + type));
            }
          } else {
            throw Unsupported.of(("text-query " + type));
          }
          final Code.Entries operatorBody = _switchResult_1;
          final Double boost = boosts.get(fields.indexOf(IterableExtensions.<String>head(group)));
          if (((boost != null) && perField)) {
            Code.Expr _head = IterableExtensions.<Code.Expr>head(operatorBody.entries().values());
            final Code.Entries inner = ((Code.Entries) _head);
            inner.put("score", MongodbCommands.obj("boost", MongodbCommands.obj("value", MongodbCommands.num((boost).doubleValue()))));
          }
          parts.add(operatorBody);
        }
      }
      Code.Entries _xifexpression_2 = null;
      int _size = parts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        _xifexpression_2 = IterableExtensions.<Code.Entries>head(parts);
      } else {
        _xifexpression_2 = MongodbCommands.obj("compound", MongodbCommands.obj("should", MongodbCommands.arr(parts), "minimumShouldMatch", MongodbCommands.num(1)));
      }
      _xblockexpression = _xifexpression_2;
    }
    return _xblockexpression;
  }

  private String constantText(final QueryInput queryInput, final String what) {
    String _xblockexpression = null;
    {
      Literal _value = queryInput.getValue();
      boolean _not = (!(_value instanceof StringLit));
      if (_not) {
        throw Unsupported.of((("text-query " + what) + " with a parameter; the generator escapes the wildcard characters of a string literal"));
      }
      Literal _value_1 = queryInput.getValue();
      _xblockexpression = ((StringLit) _value_1).getValue();
    }
    return _xblockexpression;
  }

  /**
   * Hybrid search: rrf with k 60 and unweighted legs is $rankFusion over one pipeline per leg; other fusions run in the runtime
   * over one aggregate per leg, each with the canonical similarities. The result clauses (with-total, sort, aggregate,
   * boost, mmr, group-by) go to the shared result-shape module: the fused records of the candidate window are its candidates.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf", "reranker", "cursor");
      for (final String clause : Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("explain", "highlighted"))) {
        boolean _written = ModelUtil.written(it, clause);
        if (_written) {
          String _elvis = null;
          String _get = MongodbStatements.CLAUSES.get(clause);
          if (_get != null) {
            _elvis = _get;
          } else {
            _elvis = clause;
          }
          String _plus = ("the \'" + _elvis);
          String _plus_1 = (_plus + "\' clause on a hybrid search; ");
          String _plus_2 = (_plus_1 + MongodbStatements.PENDING);
          String _plus_3 = (_plus_2 + " (score details and highlights through $rankFusion and the runtime fusion)");
          throw Unsupported.of(_plus_3);
        }
      }
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final ShapeSpec shapes = ShapeSpec.of(it);
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "fusion");
      if (_written_1) {
        _xifexpression = it.getFusion().getLiteral();
      } else {
        _xifexpression = "rrf";
      }
      final String fusionKind = _xifexpression;
      final List<Double> weights = this.legWeights(it, fusionKind);
      int _xifexpression_1 = (int) 0;
      boolean _written_2 = ModelUtil.written(it, "fusionConstant");
      if (_written_2) {
        _xifexpression_1 = it.getFusionConstant();
      } else {
        _xifexpression_1 = 60;
      }
      final int k = _xifexpression_1;
      final boolean nativeFusion = (Objects.equals(fusionKind, "rrf") && (k == 60));
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      final int defaultLimit = (_topK + _offsetOf);
      final Code.Entries pipelines = Code.entries();
      final List<Code.Expr> legParts = CollectionLiterals.<Code.Expr>newArrayList();
      int position = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Integer _xifexpression_2 = null;
          boolean _written_3 = ModelUtil.written(leg, "legLimit");
          if (_written_3) {
            Object _feature = this.feature(leg, "legLimit");
            _xifexpression_2 = ((Integer) _feature);
          } else {
            _xifexpression_2 = Integer.valueOf(defaultLimit);
          }
          final Integer legLimit = _xifexpression_2;
          Object _feature_1 = this.feature(leg, "filter");
          final FilterBlock legFilter = ((FilterBlock) _feature_1);
          Set<String> _keySet = ModelUtil.extParams(leg, MongodbGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of((("extension parameter " + key) + " on a hybrid leg"));
          }
          final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
            boolean _tripleNotEquals = (_bounds != null);
            if (_tripleNotEquals) {
              throw Unsupported.of((("bounds on a hybrid leg; " + MongodbStatements.PENDING) + " (a score condition on the leg)"));
            }
            final VectorField legField = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
            final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(legField));
            if (((ModelUtil.written(leg, "metric") && (!Objects.equals(((VectorLeg)leg).getMetric().getLiteral(), mapped.vector().metric()))) || ((VectorLeg)leg).isExact())) {
              throw Unsupported.of((("an exact or re-metered vector leg; " + MongodbStatements.PENDING) + " (an exact scan as a leg)"));
            }
            SearchParams _params = ((VectorLeg)leg).getParams();
            boolean _tripleNotEquals_1 = (_params != null);
            if (_tripleNotEquals_1) {
              this.schema.note(((VectorLeg)leg).getParams(), "filterStrategy", "exhaustive");
            }
            QueryInput _elvis_1 = null;
            QueryInput _input = ((VectorLeg)leg).getInput();
            if (_input != null) {
              _elvis_1 = _input;
            } else {
              QueryInput _input_1 = it.getInput();
              _elvis_1 = _input_1;
            }
            final QueryInput legInput = _elvis_1;
            if (((legInput == null) || Objects.equals(legInput.getKind(), EQueryInputMode.ID))) {
              throw Unsupported.of("a vector leg without a vector input");
            }
            final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), null, true);
            if ((legFilter != null)) {
              Code.Entries _query = new MongodbFilter(target, true).query(legFilter.getCondition());
              conditions.add(_query);
            }
            final MongodbCollection.VectorIndex index = target.vectorIndexOf(mapped.name());
            Integer _xifexpression_3 = null;
            if (((((VectorLeg)leg).getParams() != null) && ModelUtil.written(((VectorLeg)leg).getParams(), "candidatePool"))) {
              _xifexpression_3 = Integer.valueOf(((VectorLeg)leg).getParams().getCandidatePool());
            } else {
              _xifexpression_3 = index.searchCandidates();
            }
            final Integer pool = _xifexpression_3;
            Code.Text _text = MongodbCommands.text(index.name());
            Code.Text _text_1 = MongodbCommands.text(mapped.name());
            Code.Expr _queryVectorOf = this.queryVectorOf(legInput, mapped);
            Code.Num _num = MongodbCommands.num(MongodbCommands.candidates(pool, (legLimit).intValue()));
            Code.Num _num_1 = MongodbCommands.num((legLimit).intValue());
            Code.Entries _xifexpression_4 = null;
            boolean _isEmpty = conditions.isEmpty();
            if (_isEmpty) {
              _xifexpression_4 = null;
            } else {
              _xifexpression_4 = this.matchOf(conditions);
            }
            Code.Entries _obj = MongodbCommands.obj("$vectorSearch", MongodbCommands.obj("index", _text, "path", _text_1, "queryVector", _queryVectorOf, 
              "numCandidates", _num, "limit", _num_1, "filter", _xifexpression_4));
            stages.add(_obj);
            if ((!nativeFusion)) {
              Code.Entries _obj_1 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.canonicalScore(mapped.vector().similarity(), "vectorSearchScore")));
              stages.add(_obj_1);
            }
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              boolean _hasText = target.hasText();
              boolean _not = (!_hasText);
              if (_not) {
                throw Unsupported.of("a keyword leg in a collection without full-text fields");
              }
              EList<SearchFieldRef> _searchFields = ((KeywordLeg)leg).getSearchFields();
              QueryInput _elvis_1 = null;
              QueryInput _input = ((KeywordLeg)leg).getInput();
              if (_input != null) {
                _elvis_1 = _input;
              } else {
                QueryInput _input_1 = it.getInput();
                _elvis_1 = _input_1;
              }
              String _xifexpression_3 = null;
              boolean _written_4 = ModelUtil.written(leg, "textQueryType");
              if (_written_4) {
                _xifexpression_3 = ((KeywordLeg)leg).getTextQueryType().getLiteral();
              } else {
                _xifexpression_3 = "match";
              }
              String _xifexpression_4 = null;
              boolean _written_5 = ModelUtil.written(leg, "matchOperator");
              if (_written_5) {
                _xifexpression_4 = ((KeywordLeg)leg).getMatchOperator().getLiteral();
              } else {
                _xifexpression_4 = null;
              }
              final Code.Entries operator = this.keywordOperator(target, _searchFields, _elvis_1, _xifexpression_3, _xifexpression_4, ModelUtil.written(leg, "minShouldMatch"), null, null);
              final Code.Entries searchStage = MongodbCommands.obj("index", MongodbCommands.text(MongodbCommands.TEXT_INDEX));
              searchStage.entries().putAll(operator.entries());
              Code.Entries _obj = MongodbCommands.obj("$search", searchStage);
              stages.add(_obj);
              final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), null, false);
              if ((legFilter != null)) {
                Code.Entries _query = new MongodbFilter(target, false).query(legFilter.getCondition());
                conditions.add(_query);
              }
              boolean _isEmpty = conditions.isEmpty();
              boolean _not_1 = (!_isEmpty);
              if (_not_1) {
                Code.Entries _obj_1 = MongodbCommands.obj("$match", this.matchOf(conditions));
                stages.add(_obj_1);
              }
              Code.Entries _obj_2 = MongodbCommands.obj("$limit", MongodbCommands.num((legLimit).intValue()));
              stages.add(_obj_2);
              if ((!nativeFusion)) {
                Code.Entries _obj_3 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.obj("$meta", MongodbCommands.text("searchScore"))));
                stages.add(_obj_3);
              }
            }
          }
          if (nativeFusion) {
            pipelines.put(("leg" + Integer.valueOf(position)), MongodbCommands.arr(stages));
          } else {
            List<Code.Expr> _xifexpression_3 = null;
            boolean _plain = shapes.plain();
            if (_plain) {
              _xifexpression_3 = this.shape(it, it.getCollection(), target, true, false);
            } else {
              _xifexpression_3 = MongodbShapes.projection(shapes, target, false, false);
            }
            Iterables.<Code.Expr>addAll(stages, _xifexpression_3);
            Code.Entries _obj = MongodbCommands.obj("pipeline", MongodbCommands.arr(stages), "weight", MongodbCommands.num((weights.get(position)).doubleValue()));
            legParts.add(_obj);
          }
          position = (position + 1);
        }
      }
      if (nativeFusion) {
        final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
        Code.Entries _obj = MongodbCommands.obj("$rankFusion", MongodbCommands.obj("input", MongodbCommands.obj("pipelines", pipelines)));
        stages.add(_obj);
        Code.Entries _obj_1 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.obj("$meta", MongodbCommands.text("score"))));
        stages.add(_obj_1);
        boolean _plain = shapes.plain();
        boolean _not = (!_plain);
        if (_not) {
          Code.Call _shapedCall = this.shapedCall(this.candidateWindow(target, stages, shapes, false, false, this.options(it, it.getParams())), shapes, target);
          Code.Return _return = new Code.Return(_shapedCall);
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
        }
        Code.Call _ranked = this.ranked(it, it.getCollection(), target, stages, true, false, this.options(it, it.getParams()));
        Code.Return _return_1 = new Code.Return(_ranked);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      Code.Text _text = MongodbCommands.text(target.database);
      Code.Text _text_1 = MongodbCommands.text(target.name);
      Code.Items _arr = MongodbCommands.arr(legParts);
      Code.Text _text_2 = MongodbCommands.text(fusionKind);
      Code.Num _num = MongodbCommands.num(k);
      Code.Expr _xifexpression_2 = null;
      boolean _plain_1 = shapes.plain();
      if (_plain_1) {
        _xifexpression_2 = MongodbCommands.num(it.getTopK());
      } else {
        _xifexpression_2 = shapes.window();
      }
      int _xifexpression_3 = (int) 0;
      boolean _plain_2 = shapes.plain();
      if (_plain_2) {
        _xifexpression_3 = this.offsetOf(it);
      } else {
        _xifexpression_3 = 0;
      }
      Code.Num _num_1 = MongodbCommands.num(_xifexpression_3);
      Code.Text _text_3 = MongodbCommands.text(target.key);
      Code.Entries _xifexpression_4 = null;
      boolean _plain_3 = shapes.plain();
      if (_plain_3) {
        _xifexpression_4 = this.boundsOf(it.getBounds());
      } else {
        _xifexpression_4 = null;
      }
      Code.Flag _xifexpression_5 = null;
      boolean _plain_4 = shapes.plain();
      if (_plain_4) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = MongodbCommands.flag(true);
      }
      final Code.Call fused = MongodbCommands.call("hybrid", "database", _text, "collection", _text_1, "legs", _arr, "fusion", _text_2, 
        "k", _num, "size", _xifexpression_2, "offset", _num_1, "key", _text_3, 
        "bounds", _xifexpression_4, "candidates", _xifexpression_5);
      Code.Call _xifexpression_6 = null;
      boolean _plain_5 = shapes.plain();
      if (_plain_5) {
        _xifexpression_6 = fused;
      } else {
        _xifexpression_6 = this.shapedCall(fused, shapes, target);
      }
      Code.Return _return_2 = new Code.Return(_xifexpression_6);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
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
      this.rejectClauses(it, "asOf", "cursor", "reranker", "explain", "highlighted");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final MongodbCollection target = this.schema.collection(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final MongodbCollection lookup = this.schema.collection(_elvis);
      final VectorField searched = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(searched));
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
      final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), null, true);
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final List<Code.Expr> examples = CollectionLiterals.<Code.Expr>newArrayList();
        Iterables.<Code.Expr>addAll(examples, positiveIds);
        Iterables.<Code.Expr>addAll(examples, negativeIds);
        boolean _isEmpty_1 = examples.isEmpty();
        boolean _not = (!_isEmpty_1);
        if (_not) {
          Code.Entries _obj = MongodbCommands.obj("_id", MongodbCommands.obj("$nin", MongodbCommands.arr(examples)));
          conditions.add(_obj);
        }
      }
      final ShapeSpec shapes = this.moduled(it);
      int _xifexpression = (int) 0;
      if ((shapes == null)) {
        int _topK = it.getTopK();
        int _offsetOf = this.offsetOf(it);
        _xifexpression = (_topK + _offsetOf);
      } else {
        _xifexpression = this.windowOf(shapes);
      }
      final int k = _xifexpression;
      final MongodbCollection.VectorIndex index = target.vectorIndexOf(mapped.name());
      Integer _xifexpression_1 = null;
      if (((it.getParams() != null) && ModelUtil.written(it.getParams(), "candidatePool"))) {
        _xifexpression_1 = Integer.valueOf(it.getParams().getCandidatePool());
      } else {
        _xifexpression_1 = index.searchCandidates();
      }
      final Integer pool = _xifexpression_1;
      final boolean nativeScore = (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE));
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Text _text = MongodbCommands.text(index.name());
      Code.Text _text_1 = MongodbCommands.text(mapped.name());
      Code.Nil _nil = new Code.Nil();
      Code.Num _num = MongodbCommands.num(MongodbCommands.candidates(pool, k));
      Code.Num _num_1 = MongodbCommands.num(k);
      Code.Entries _xifexpression_2 = null;
      boolean _isEmpty_2 = conditions.isEmpty();
      if (_isEmpty_2) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = this.matchOf(conditions);
      }
      Code.Entries _obj_1 = MongodbCommands.obj("$vectorSearch", MongodbCommands.obj("index", _text, "path", _text_1, "queryVector", _nil, 
        "numCandidates", _num, "limit", _num_1, "filter", _xifexpression_2));
      stages.add(_obj_1);
      Code.Expr _xifexpression_3 = null;
      if (nativeScore) {
        _xifexpression_3 = MongodbCommands.obj("$meta", MongodbCommands.text("vectorSearchScore"));
      } else {
        _xifexpression_3 = MongodbCommands.canonicalScore(mapped.vector().similarity(), "vectorSearchScore");
      }
      Code.Entries _obj_2 = MongodbCommands.obj("$addFields", MongodbCommands.obj(MongodbCommands.SCORE, _xifexpression_3));
      stages.add(_obj_2);
      Code.Call _xifexpression_4 = null;
      if ((shapes == null)) {
        _xifexpression_4 = this.ranked(it, it.getCollection(), target, stages, true, false, this.options(it, it.getParams()));
      } else {
        _xifexpression_4 = this.candidateWindow(target, stages, shapes, false, false, this.options(it, it.getParams()));
      }
      final Code.Call search = _xifexpression_4;
      Code.Items _xifexpression_5 = null;
      boolean _isEmpty_3 = positiveIds.isEmpty();
      if (_isEmpty_3) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = MongodbCommands.arr(positiveIds);
      }
      Code.Items _xifexpression_6 = null;
      boolean _isEmpty_4 = positiveVectors.isEmpty();
      if (_isEmpty_4) {
        _xifexpression_6 = null;
      } else {
        _xifexpression_6 = MongodbCommands.arr(positiveVectors);
      }
      Code.Items _xifexpression_7 = null;
      boolean _isEmpty_5 = negativeIds.isEmpty();
      if (_isEmpty_5) {
        _xifexpression_7 = null;
      } else {
        _xifexpression_7 = MongodbCommands.arr(negativeIds);
      }
      Code.Items _xifexpression_8 = null;
      boolean _isEmpty_6 = negativeVectors.isEmpty();
      if (_isEmpty_6) {
        _xifexpression_8 = null;
      } else {
        _xifexpression_8 = MongodbCommands.arr(negativeVectors);
      }
      final Code.Call recommended = this.recommendCall(target, lookup, mapped, search, _xifexpression_5, _xifexpression_6, _xifexpression_7, _xifexpression_8);
      Code.Call _xifexpression_9 = null;
      if ((shapes == null)) {
        _xifexpression_9 = recommended;
      } else {
        _xifexpression_9 = this.shapedCall(recommended, shapes, target);
      }
      Code.Return _return = new Code.Return(_xifexpression_9);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The runtime computes the query vector from the examples and runs the search's pipeline with it as queryVector.
   */
  private Code.Call recommendCall(final MongodbCollection target, final MongodbCollection lookup, final MongodbCollection.Field mapped, final Code.Call search, final Code.Expr positiveIds, final Code.Expr positiveVectors, final Code.Expr negativeIds, final Code.Expr negativeVectors) {
    return MongodbCommands.call("recommend", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "lookup", MongodbCommands.text(lookup.name), "field", MongodbCommands.text(mapped.name()), 
      "pipeline", search.args().get("pipeline"), "key", MongodbCommands.text(target.key), "options", search.args().get("options"), "first", search.args().get("first"), 
      "positive_ids", positiveIds, "positive_vectors", positiveVectors, "negative_ids", negativeIds, "negative_vectors", negativeVectors, 
      "candidates", search.args().get("candidates"));
  }

  private Code.Expr listOf(final Code.Expr value) {
    Code.Items _xifexpression = null;
    if ((value instanceof Code.Items)) {
      _xifexpression = ((Code.Items)value);
    } else {
      _xifexpression = MongodbCommands.arr(value);
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
      this.schema.note(it, "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final Code.Expr keys = this.idsOf(it.getIds());
      final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, null, keys, false);
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _obj = MongodbCommands.obj("$match", this.matchOf(conditions));
      stages.add(_obj);
      Code.Entries _obj_1 = MongodbCommands.obj("$addFields", MongodbCommands.obj("vecdsl_order", MongodbCommands.obj("$indexOfArray", MongodbCommands.arr(MongodbCommands.literal(keys), MongodbCommands.text("$_id")))));
      stages.add(_obj_1);
      Code.Entries _obj_2 = MongodbCommands.obj("$sort", MongodbCommands.obj("vecdsl_order", MongodbCommands.num(1)));
      stages.add(_obj_2);
      List<Code.Expr> _shape = this.shape(it, it.getCollection(), target, false, false);
      Iterables.<Code.Expr>addAll(stages, _shape);
      Code.Call _aggregateCall = this.aggregateCall(target, MongodbCommands.arr(stages), null, false);
      Code.Return _return = new Code.Return(_aggregateCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      this.schema.note(it, "exact", "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final Code.Call counting = this.aggregateCall(target, MongodbCommands.arr(MongodbCommands.obj("$match", this.matchOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null, false))), MongodbCommands.obj("$count", MongodbCommands.text("count"))), this.options(it, null), true);
      counting.args().put("select", MongodbCommands.arr(MongodbCommands.text("count")));
      counting.args().put("default", MongodbCommands.num(0));
      Code.Return _return = new Code.Return(counting);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final List<Code.Expr> conditions = this.clauses(it, it.getCollection(), target, it.getFilter(), null, false);
      String _cursor = it.getCursor();
      boolean _tripleNotEquals = (_cursor != null);
      if (_tripleNotEquals) {
        if (((!it.getSortClauses().isEmpty()) || (this.offsetOf(it) > 0))) {
          throw Unsupported.of("the \'cursor\' clause with sort or offset; the cursor continues after the key of the last page");
        }
        Code.Entries _obj = MongodbCommands.obj("_id", MongodbCommands.obj("$gt", this.cursorValue(target, it.getCursor())));
        conditions.add(_obj);
      }
      if ((ModelUtil.written(it, "sliceIndex") || ModelUtil.written(it, "sliceTotal"))) {
        if (((((!ModelUtil.written(it, "sliceTotal")) || (it.getSliceTotal() < 1)) || (it.getSliceIndex() < 0)) || (it.getSliceIndex() >= it.getSliceTotal()))) {
          throw Unsupported.of("a slice index outside 0 to slice total - 1");
        }
        Code.Entries _obj_1 = MongodbCommands.obj("$expr", MongodbCommands.obj("$eq", MongodbCommands.arr(MongodbCommands.obj("$abs", MongodbCommands.obj("$mod", MongodbCommands.arr(MongodbCommands.obj("$toHashedIndexKey", MongodbCommands.text("$_id")), MongodbCommands.num(it.getSliceTotal())))), MongodbCommands.num(it.getSliceIndex()))));
        conditions.add(_obj_1);
      }
      final Code.Entries ordering = Code.entries();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        ordering.entries().putAll(this.sortEntry(target, clause, false).entries());
      }
      ordering.put("_id", MongodbCommands.num(1));
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _obj_2 = MongodbCommands.obj("$match", this.matchOf(conditions));
      stages.add(_obj_2);
      Code.Entries _obj_3 = MongodbCommands.obj("$sort", ordering);
      stages.add(_obj_3);
      int _offsetOf = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        Code.Entries _obj_4 = MongodbCommands.obj("$skip", MongodbCommands.num(this.offsetOf(it)));
        stages.add(_obj_4);
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = it.getScrollLimit();
      } else {
        _xifexpression = 10;
      }
      Code.Entries _obj_5 = MongodbCommands.obj("$limit", MongodbCommands.num(_xifexpression));
      stages.add(_obj_5);
      List<Code.Expr> _shape = this.shape(it, it.getCollection(), target, false, false);
      Iterables.<Code.Expr>addAll(stages, _shape);
      Code.Call _aggregateCall = this.aggregateCall(target, MongodbCommands.arr(stages), this.options(it, null), false);
      Code.Return _return = new Code.Return(_aggregateCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr cursorValue(final MongodbCollection target, final String cursorText) {
    Code.Text _xblockexpression = null;
    {
      boolean _equals = Objects.equals(target.keyKind, "number");
      if (_equals) {
        try {
          return MongodbCommands.num(Long.parseLong(cursorText));
        } catch (final Throwable _t) {
          if (_t instanceof NumberFormatException) {
            throw Unsupported.of((("the cursor " + cursorText) + " on an integer key"));
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
      _xblockexpression = MongodbCommands.text(cursorText);
    }
    return _xblockexpression;
  }

  private Code.Entries sortEntry(final MongodbCollection target, final SortClause clause, final boolean scored) {
    Code.Entries _xblockexpression = null;
    {
      int _xifexpression = (int) 0;
      ESortDirection _direction = clause.getDirection();
      boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
      if (_equals) {
        _xifexpression = (-1);
      } else {
        _xifexpression = 1;
      }
      final Code.Num order = MongodbCommands.num(_xifexpression);
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        if ((!scored)) {
          throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
        }
        return MongodbCommands.obj(MongodbCommands.SCORE, order);
      }
      final FieldPath path = clause.getField();
      if (((path.getRawPath() == null) && (!(path.getField() instanceof PayloadField)))) {
        throw Unsupported.of("sorting by a vector field");
      }
      _xblockexpression = MongodbCommands.obj(this.fieldPath(target, path), order);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _obj = MongodbCommands.obj("$match", this.matchOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null, false)));
      stages.add(_obj);
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        Code.Entries _obj_1 = MongodbCommands.obj("$sort", MongodbCommands.obj("_id", MongodbCommands.num(1)));
        stages.add(_obj_1);
        Code.Entries _obj_2 = MongodbCommands.obj("$limit", MongodbCommands.num(it.getSampleSize()));
        stages.add(_obj_2);
      } else {
        Code.Entries _obj_3 = MongodbCommands.obj("$sample", MongodbCommands.obj("size", MongodbCommands.num(it.getSampleSize())));
        stages.add(_obj_3);
      }
      List<Code.Expr> _shape = this.shape(it, it.getCollection(), target, false, false);
      Iterables.<Code.Expr>addAll(stages, _shape);
      Code.Call _aggregateCall = this.aggregateCall(target, MongodbCommands.arr(stages), this.options(it, null), false);
      Code.Return _return = new Code.Return(_aggregateCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Qdrant's definition in one aggregation: a sample, each member with its nearest neighbours among the sample (exact similarity).
   */
  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.rejectClauses(it, "asOf");
      this.queryExtensions(it);
      this.schema.note(it, "consistency");
      final MongodbCollection target = this.schema.collection(it.getCollection());
      final MongodbCollection.Field mapped = target.field(ModelUtil.fieldName(this.vectorFieldOf(it.getCollection(), it.getVectorField())));
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "matrixLimit");
      if (_written) {
        _xifexpression = it.getMatrixLimit();
      } else {
        _xifexpression = 3;
      }
      final int limit = _xifexpression;
      final Code.Entries neighbours = MongodbCommands.obj("$map", MongodbCommands.obj("input", MongodbCommands.obj("$filter", MongodbCommands.obj("input", MongodbCommands.text("$docs"), "as", MongodbCommands.text("b"), "cond", MongodbCommands.obj("$ne", MongodbCommands.arr(MongodbCommands.text("$$b._id"), MongodbCommands.text("$$a._id"))))), 
        "as", MongodbCommands.text("b"), "in", MongodbCommands.obj("id", MongodbCommands.text("$$b._id"), MongodbCommands.SCORE, MongodbCommands.similarityOf(mapped.vector().metric(), MongodbCommands.text("$$a.v"), MongodbCommands.text("$$b.v")))));
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _obj = MongodbCommands.obj("$match", this.matchOf(this.clauses(it, it.getCollection(), target, it.getFilter(), null, false)));
      stages.add(_obj);
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "sampleSize");
      if (_written_1) {
        _xifexpression_1 = it.getSampleSize();
      } else {
        _xifexpression_1 = 10;
      }
      Code.Entries _obj_1 = MongodbCommands.obj("$sample", MongodbCommands.obj("size", MongodbCommands.num(_xifexpression_1)));
      stages.add(_obj_1);
      String _name = mapped.name();
      String _plus = ("$" + _name);
      Code.Entries _obj_2 = MongodbCommands.obj("$project", MongodbCommands.obj("v", MongodbCommands.text(_plus)));
      stages.add(_obj_2);
      Code.Nil _nil = new Code.Nil();
      Code.Entries _obj_3 = MongodbCommands.obj("$group", MongodbCommands.obj("_id", _nil, "docs", MongodbCommands.obj("$push", MongodbCommands.text("$$ROOT"))));
      stages.add(_obj_3);
      Code.Entries _obj_4 = MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "rows", MongodbCommands.obj("$map", MongodbCommands.obj("input", MongodbCommands.text("$docs"), "as", MongodbCommands.text("a"), "in", MongodbCommands.obj("id", MongodbCommands.text("$$a._id"), 
        "neighbours", MongodbCommands.obj("$slice", MongodbCommands.arr(MongodbCommands.obj("$sortArray", MongodbCommands.obj("input", neighbours, "sortBy", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.num((-1)), "id", MongodbCommands.num(1)))), MongodbCommands.num(limit))))))));
      stages.add(_obj_4);
      Code.Entries _obj_5 = MongodbCommands.obj("$unwind", MongodbCommands.text("$rows"));
      stages.add(_obj_5);
      Code.Entries _obj_6 = MongodbCommands.obj("$replaceWith", MongodbCommands.text("$rows"));
      stages.add(_obj_6);
      Code.Entries _obj_7 = MongodbCommands.obj("$sort", MongodbCommands.obj("id", MongodbCommands.num(1)));
      stages.add(_obj_7);
      Code.Call _aggregateCall = this.aggregateCall(target, MongodbCommands.arr(stages), this.options(it, null), false);
      Code.Return _return = new Code.Return(_aggregateCall);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + MongodbStatements.NO_GENERATION));
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
    String _plus = (MongodbStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  /**
   * The stages after a ranking stage that has set the score: bounds, the sort (score, else the sort clauses, key last), group-by,
   * then either the page (skip, limit, projection) or, with with-total or aggregations, a $facet with the page, the total and the
   * aggregations.
   */
  private Code.Call ranked(final EObject statement, final VectorCollection collection, final MongodbCollection target, final List<Code.Expr> rankingStages, final boolean scored, final boolean textual, final Code.Entries aggregateOptions) {
    Code.Call _xblockexpression = null;
    {
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr[])Conversions.unwrapArray(rankingStages, Code.Expr.class)));
      Object _feature = this.feature(statement, "bounds");
      final Code.Entries scoreBounds = this.boundsOf(((ScoreBounds) _feature));
      if ((scoreBounds != null)) {
        final Code.Entries range = Code.entries();
        boolean _has = scoreBounds.has("min");
        if (_has) {
          Code.Expr _get = scoreBounds.get("min");
          double _value = ((Code.Num) _get).value();
          double _minus = (_value - 1e-9);
          range.put("$gte", MongodbCommands.num(_minus));
        }
        boolean _has_1 = scoreBounds.has("max");
        if (_has_1) {
          Code.Expr _get_1 = scoreBounds.get("max");
          double _value_1 = ((Code.Num) _get_1).value();
          double _plus = (_value_1 + 1e-9);
          range.put("$lte", MongodbCommands.num(_plus));
        }
        Code.Entries _obj = MongodbCommands.obj("$match", MongodbCommands.obj(MongodbCommands.SCORE, range));
        stages.add(_obj);
      }
      Object _elvis = null;
      Object _feature_1 = this.feature(statement, "sortClauses");
      if (_feature_1 != null) {
        _elvis = _feature_1;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<SortClause> sortClauses = ((List<SortClause>) _elvis);
      final Code.Entries ordering = Code.entries();
      for (final SortClause clause : sortClauses) {
        ordering.entries().putAll(this.sortEntry(target, clause, scored).entries());
      }
      boolean _has_2 = ordering.has(MongodbCommands.SCORE);
      boolean _not = (!_has_2);
      if (_not) {
        ordering.put(MongodbCommands.SCORE, MongodbCommands.num((-1)));
      }
      ordering.put("_id", MongodbCommands.num(1));
      final int offset = this.offsetOf(statement);
      Object _feature_2 = this.feature(statement, "topK");
      final Integer topK = ((Integer) _feature_2);
      final List<Code.Expr> page = CollectionLiterals.<Code.Expr>newArrayList();
      final String group = this.groupOf(statement, target);
      if ((group != null)) {
        Integer _xifexpression = null;
        boolean _written = ModelUtil.written(statement, "groupSize");
        if (_written) {
          Object _feature_3 = this.feature(statement, "groupSize");
          _xifexpression = ((Integer) _feature_3);
        } else {
          _xifexpression = Integer.valueOf(1);
        }
        final Integer size = _xifexpression;
        Code.Entries _obj_1 = MongodbCommands.obj("$sort", ordering);
        page.add(_obj_1);
        List<Code.Expr> _shape = this.shape(statement, collection, target, scored, textual);
        Iterables.<Code.Expr>addAll(page, _shape);
        Code.Entries _obj_2 = MongodbCommands.obj("$group", MongodbCommands.obj("_id", MongodbCommands.text(("$" + MongodbStatements.GROUP)), "hits", MongodbCommands.obj("$push", MongodbCommands.text("$$ROOT")), "best", MongodbCommands.obj("$max", MongodbCommands.text(("$" + MongodbCommands.SCORE)))));
        page.add(_obj_2);
        Code.Entries _obj_3 = MongodbCommands.obj("$sort", MongodbCommands.obj("best", MongodbCommands.num((-1)), "_id", MongodbCommands.num(1)));
        page.add(_obj_3);
        if ((offset > 0)) {
          Code.Entries _obj_4 = MongodbCommands.obj("$skip", MongodbCommands.num(offset));
          page.add(_obj_4);
        }
        Code.Entries _obj_5 = MongodbCommands.obj("$limit", MongodbCommands.num((topK).intValue()));
        page.add(_obj_5);
        Code.Entries _obj_6 = MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "group", MongodbCommands.text("$_id"), "hits", MongodbCommands.obj("$slice", MongodbCommands.arr(MongodbCommands.obj("$sortArray", MongodbCommands.obj("input", MongodbCommands.text("$hits"), 
          "sortBy", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.num((-1)), target.key, MongodbCommands.num(1)))), MongodbCommands.num((size).intValue())))));
        page.add(_obj_6);
        Code.Entries _obj_7 = MongodbCommands.obj("$unset", MongodbCommands.text(("hits." + MongodbStatements.GROUP)));
        page.add(_obj_7);
      } else {
        Code.Entries _obj_8 = MongodbCommands.obj("$sort", ordering);
        page.add(_obj_8);
        if ((offset > 0)) {
          Code.Entries _obj_9 = MongodbCommands.obj("$skip", MongodbCommands.num(offset));
          page.add(_obj_9);
        }
        Code.Entries _obj_10 = MongodbCommands.obj("$limit", MongodbCommands.num((topK).intValue()));
        page.add(_obj_10);
        List<Code.Expr> _shape_1 = this.shape(statement, collection, target, scored, textual);
        Iterables.<Code.Expr>addAll(page, _shape_1);
      }
      final boolean withTotal = ModelUtil.written(statement, "withTotal");
      Object _elvis_1 = null;
      Object _feature_4 = this.feature(statement, "aggregations");
      if (_feature_4 != null) {
        _elvis_1 = _feature_4;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      final List<AggregationClause> aggregations = ((List<AggregationClause>) _elvis_1);
      if (((!withTotal) && aggregations.isEmpty())) {
        Iterables.<Code.Expr>addAll(stages, page);
        return this.aggregateCall(target, MongodbCommands.arr(stages), aggregateOptions, false);
      }
      final Code.Entries facets = MongodbCommands.obj("hits", MongodbCommands.arr(page));
      final Code.Entries result = MongodbCommands.obj("_id", MongodbCommands.num(0), "hits", MongodbCommands.num(1));
      if (withTotal) {
        facets.put("total", MongodbCommands.arr(MongodbCommands.obj("$count", MongodbCommands.text("n"))));
        result.put("total", MongodbCommands.obj("$ifNull", MongodbCommands.arr(MongodbCommands.obj("$first", MongodbCommands.text("$total.n")), MongodbCommands.num(0))));
      }
      boolean _isEmpty = aggregations.isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        final Code.Entries named = Code.entries();
        for (final AggregationClause clause_1 : aggregations) {
          {
            final Pair<String, Code.Expr> aggregation = this.aggregationOf(target, clause_1);
            facets.put(aggregation.getKey(), aggregation.getValue());
            String _key = aggregation.getKey();
            String _plus_1 = ("$" + _key);
            named.put(aggregation.getKey(), MongodbCommands.text(_plus_1));
          }
        }
        result.put("aggregations", named);
      }
      Code.Entries _obj_11 = MongodbCommands.obj("$facet", facets);
      stages.add(_obj_11);
      Code.Entries _obj_12 = MongodbCommands.obj("$project", result);
      stages.add(_obj_12);
      _xblockexpression = this.aggregateCall(target, MongodbCommands.arr(stages), aggregateOptions, true);
    }
    return _xblockexpression;
  }

  /**
   * One aggregation as a $facet pipeline over the matching documents, under its alias (else kind_field).
   */
  private Pair<String, Code.Expr> aggregationOf(final MongodbCollection target, final AggregationClause clause) {
    Pair<String, Code.Expr> _xblockexpression = null;
    {
      String _xifexpression = null;
      String _rawPath = clause.getField().getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        _xifexpression = clause.getField().getRawPath();
      } else {
        _xifexpression = this.fieldPath(target, clause.getField());
      }
      final String path = _xifexpression;
      final Code.Text operand = MongodbCommands.text(("$" + path));
      int _xifexpression_1 = (int) 0;
      boolean _written = ModelUtil.written(clause, "bucketCount");
      if (_written) {
        _xifexpression_1 = clause.getBucketCount();
      } else {
        _xifexpression_1 = 10;
      }
      final int size = _xifexpression_1;
      Code.Items _switchResult = null;
      String _literal = clause.getKind().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "count":
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$match", MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(true)))), MongodbCommands.obj("$count", MongodbCommands.text("value")));
            break;
          case "sum":
          case "avg":
          case "min":
          case "max":
            Code.Nil _nil = new Code.Nil();
            String _literal_1 = clause.getKind().getLiteral();
            String _plus = ("$" + _literal_1);
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$group", MongodbCommands.obj("_id", _nil, "value", MongodbCommands.obj(_plus, operand))), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0))));
            break;
          case "stats":
            Code.Nil _nil_1 = new Code.Nil();
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$group", MongodbCommands.obj("_id", _nil_1, "count", MongodbCommands.obj("$sum", MongodbCommands.obj("$cond", MongodbCommands.arr(MongodbCommands.obj("$isNumber", operand), MongodbCommands.num(1), MongodbCommands.num(0)))), "min", MongodbCommands.obj("$min", operand), 
              "max", MongodbCommands.obj("$max", operand), "avg", MongodbCommands.obj("$avg", operand), "sum", MongodbCommands.obj("$sum", operand))), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0))));
            break;
          case "percentiles":
            Code.Nil _nil_2 = new Code.Nil();
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$group", MongodbCommands.obj("_id", _nil_2, "value", MongodbCommands.obj("$percentile", MongodbCommands.obj("input", operand, 
              "p", MongodbCommands.arr(MongodbCommands.num(0.01), MongodbCommands.num(0.05), MongodbCommands.num(0.25), MongodbCommands.num(0.5), MongodbCommands.num(0.75), MongodbCommands.num(0.95), MongodbCommands.num(0.99)), "method", MongodbCommands.text("approximate"))))), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0))));
            break;
          case "cardinality":
          case "distinct_count":
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$unwind", operand), MongodbCommands.obj("$group", MongodbCommands.obj("_id", operand)), MongodbCommands.obj("$count", MongodbCommands.text("value")));
            break;
          case "terms":
          case "facet":
            _switchResult = MongodbCommands.arr(MongodbCommands.obj("$unwind", operand), MongodbCommands.obj("$sortByCount", operand), MongodbCommands.obj("$limit", MongodbCommands.num(size)), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "key", MongodbCommands.text("$_id"), "count", MongodbCommands.num(1))));
            break;
          case "histogram":
            Code.Items _xblockexpression_1 = null;
            {
              if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                throw Unsupported.of("a histogram without a numeric interval");
              }
              final Code.Num width = MongodbCommands.num(Double.parseDouble(clause.getInterval()));
              _xblockexpression_1 = MongodbCommands.arr(MongodbCommands.obj("$match", MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(true)))), MongodbCommands.obj("$group", MongodbCommands.obj("_id", MongodbCommands.obj("$multiply", MongodbCommands.arr(MongodbCommands.obj("$floor", MongodbCommands.obj("$divide", MongodbCommands.arr(operand, width))), width)), 
                "count", MongodbCommands.obj("$sum", MongodbCommands.num(1)))), MongodbCommands.obj("$sort", MongodbCommands.obj("_id", MongodbCommands.num(1))), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "key", MongodbCommands.text("$_id"), "count", MongodbCommands.num(1))));
            }
            _switchResult = _xblockexpression_1;
            break;
          case "date_histogram":
            Code.Items _xblockexpression_2 = null;
            {
              final Pair<String, Integer> unit = this.dateUnit(clause.getInterval());
              _xblockexpression_2 = MongodbCommands.arr(MongodbCommands.obj("$match", MongodbCommands.obj(path, MongodbCommands.obj("$exists", MongodbCommands.flag(true)))), MongodbCommands.obj("$group", MongodbCommands.obj("_id", MongodbCommands.obj("$dateTrunc", MongodbCommands.obj("date", operand, "unit", MongodbCommands.text(unit.getKey()), "binSize", MongodbCommands.num(unit.getValue().doubleValue()))), 
                "count", MongodbCommands.obj("$sum", MongodbCommands.num(1)))), MongodbCommands.obj("$sort", MongodbCommands.obj("_id", MongodbCommands.num(1))), MongodbCommands.obj("$project", MongodbCommands.obj("_id", MongodbCommands.num(0), "key", MongodbCommands.text("$_id"), "count", MongodbCommands.num(1))));
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            throw Unsupported.of("range aggregations; vecDSL gives no ranges, which a $bucket stage needs as boundaries");
        }
      } else {
        throw Unsupported.of("range aggregations; vecDSL gives no ranges, which a $bucket stage needs as boundaries");
      }
      final Code.Expr definition = _switchResult;
      String _elvis = null;
      String _alias = clause.getAlias();
      if (_alias != null) {
        _elvis = _alias;
      } else {
        String _literal_2 = clause.getKind().getLiteral();
        String _plus_1 = (_literal_2 + "_");
        String _replace = path.replace(".", "_");
        String _plus_2 = (_plus_1 + _replace);
        _elvis = _plus_2;
      }
      _xblockexpression = Pair.<String, Code.Expr>of(_elvis, definition);
    }
    return _xblockexpression;
  }

  private Pair<String, Integer> dateUnit(final String interval) {
    Pair<String, Integer> _xblockexpression = null;
    {
      if ((interval == null)) {
        throw Unsupported.of("a date histogram without an interval");
      }
      final Matcher matcher = Pattern.compile("([0-9]+)(s|m|h|d|w|M|y)").matcher(interval.trim());
      boolean _matches = matcher.matches();
      boolean _not = (!_matches);
      if (_not) {
        throw Unsupported.of((("the date interval " + interval) + "; $dateTrunc takes a count of seconds, minutes, hours, days, weeks, months or years (30m, 1d, 1M)"));
      }
      String _switchResult = null;
      String _group = matcher.group(2);
      if (_group != null) {
        switch (_group) {
          case "s":
            _switchResult = "second";
            break;
          case "m":
            _switchResult = "minute";
            break;
          case "h":
            _switchResult = "hour";
            break;
          case "d":
            _switchResult = "day";
            break;
          case "w":
            _switchResult = "week";
            break;
          case "M":
            _switchResult = "month";
            break;
          default:
            _switchResult = "year";
            break;
        }
      } else {
        _switchResult = "year";
      }
      final String unit = _switchResult;
      Integer _valueOf = Integer.valueOf(matcher.group(1));
      _xblockexpression = Pair.<String, Integer>of(unit, _valueOf);
    }
    return _xblockexpression;
  }

  /**
   * The group-by path of a grouped search.
   */
  private String groupOf(final EObject statement, final MongodbCollection target) {
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
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        return path.getRawPath();
      }
      final String leafName = ModelUtil.fieldName(path.getField());
      String _xifexpression = null;
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        _xifexpression = target.field(leafName).name();
      } else {
        _xifexpression = this.fieldPath(target, path);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private String fieldPath(final MongodbCollection target, final FieldPath path) {
    String _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        String[] _split = path.getRawPath().split("\\.");
        for (final String segment : _split) {
          MongodbCommands.field(segment);
        }
        return path.getRawPath();
      }
      final List<String> segments = CollectionLiterals.<String>newArrayList();
      String _path = target.path(ModelUtil.fieldName(path.getField()));
      segments.add(_path);
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
   * The projection of the records: with an output list, an inclusion $project of the key (from _id), the score and the listed
   * fields; without one, the key added and _id, the vectors (unless with-vectors), the hidden fields and the excluded fields
   * removed. Highlights and score details come from the search metadata.
   */
  private List<Code.Expr> shape(final EObject statement, final VectorCollection collection, final MongodbCollection target, final boolean scored, final boolean textual) {
    List<Code.Expr> _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "output");
      final OutputSpec outputSpec = ((OutputSpec) _feature);
      Object _elvis = null;
      Object _feature_1 = this.feature(statement, "excluded");
      if (_feature_1 != null) {
        _elvis = _feature_1;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<Field> excluded = ((List<Field>) _elvis);
      Object _feature_2 = this.feature(statement, "withVectors");
      final boolean withVectors = Objects.equals(Boolean.TRUE, _feature_2);
      final boolean highlighted = (textual && (!((List<Field>) ObjectExtensions.<Object>operator_elvis(this.feature(statement, "highlighted"), CollectionLiterals.<Object>newArrayList())).isEmpty()));
      final boolean explained = (textual && Objects.equals(Boolean.TRUE, this.feature(statement, "explain")));
      final Function1<Field, String> _function = (Field f) -> {
        return target.path(ModelUtil.fieldName(f));
      };
      final List<String> excludedNames = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(excluded, _function));
      final String group = this.groupOf(statement, target);
      if (((outputSpec != null) && (!outputSpec.isAllFields()))) {
        final Code.Entries projection = MongodbCommands.obj("_id", MongodbCommands.num(0), target.key, MongodbCommands.text("$_id"));
        if (scored) {
          projection.put(MongodbCommands.SCORE, MongodbCommands.num(1));
        }
        if ((group != null)) {
          projection.put(MongodbStatements.GROUP, MongodbCommands.text(("$" + group)));
        }
        EList<Field> _fields = outputSpec.getFields();
        for (final Field f : _fields) {
          {
            final String fieldName = ModelUtil.fieldName(f);
            if (((!Objects.equals(fieldName, target.key)) && (!excludedNames.contains(target.path(fieldName))))) {
              projection.put(target.path(fieldName), MongodbCommands.num(1));
            }
          }
        }
        if (withVectors) {
          List<MongodbCollection.Field> _vectorFields = target.vectorFields();
          for (final MongodbCollection.Field v : _vectorFields) {
            boolean _contains = excludedNames.contains(v.name());
            boolean _not = (!_contains);
            if (_not) {
              projection.put(v.name(), MongodbCommands.num(1));
            }
          }
        }
        if (highlighted) {
          projection.put("highlights", MongodbCommands.obj("$meta", MongodbCommands.text("searchHighlights")));
        }
        if (explained) {
          projection.put("explanation", MongodbCommands.obj("$meta", MongodbCommands.text("searchScoreDetails")));
        }
        Code.Entries _obj = MongodbCommands.obj("$project", projection);
        return Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_obj));
      }
      final Code.Entries removed = MongodbCommands.obj("_id", MongodbCommands.num(0));
      if ((!withVectors)) {
        List<MongodbCollection.Field> _vectorFields_1 = target.vectorFields();
        for (final MongodbCollection.Field v_1 : _vectorFields_1) {
          removed.put(v_1.name(), MongodbCommands.num(0));
        }
      }
      List<String> _hidden = target.hidden();
      for (final String hidden : _hidden) {
        removed.put(hidden, MongodbCommands.num(0));
      }
      removed.put("vecdsl_order", MongodbCommands.num(0));
      for (final String name : excludedNames) {
        boolean _notEquals = (!Objects.equals(name, "_id"));
        if (_notEquals) {
          removed.put(name, MongodbCommands.num(0));
        }
      }
      final Code.Entries added = MongodbCommands.obj(target.key, MongodbCommands.text("$_id"));
      if ((group != null)) {
        added.put(MongodbStatements.GROUP, MongodbCommands.text(("$" + group)));
      }
      if (highlighted) {
        added.put("highlights", MongodbCommands.obj("$meta", MongodbCommands.text("searchHighlights")));
      }
      if (explained) {
        added.put("explanation", MongodbCommands.obj("$meta", MongodbCommands.text("searchScoreDetails")));
      }
      Code.Entries _obj_1 = MongodbCommands.obj("$addFields", added);
      Code.Entries _obj_2 = MongodbCommands.obj("$project", removed);
      _xblockexpression = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_obj_1, _obj_2));
    }
    return _xblockexpression;
  }

  /**
   * The spec of the shared result-shape module when a search writes a clause MongoDB has no stage for (boost, mmr), else null.
   */
  private ShapeSpec moduled(final QueryMethod statement) {
    ShapeSpec _xifexpression = null;
    if ((ModelUtil.written(statement, "boost") || ModelUtil.written(statement, "mmr"))) {
      _xifexpression = ShapeSpec.of(statement);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  /**
   * The number of records a search ranks for the shared result-shape module (its window rule).
   */
  private int windowOf(final ShapeSpec shapes) {
    Code.Expr _window = shapes.window();
    return Double.valueOf(((Code.Num) _window).value()).intValue();
  }

  /**
   * The candidate window of the shared result-shape module after a ranking stage that has set the score: the window's best records
   * by score, then key, with the fields the module reads (MongodbShapes); bounds, offset, sort, groups, totals and aggregations are
   * the module's.
   */
  private Code.Call candidateWindow(final MongodbCollection target, final List<Code.Expr> rankingStages, final ShapeSpec shapes, final boolean highlighted, final boolean explained, final Code.Entries aggregateOptions) {
    Code.Call _xblockexpression = null;
    {
      final List<Code.Expr> stages = CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr[])Conversions.unwrapArray(rankingStages, Code.Expr.class)));
      Code.Entries _obj = MongodbCommands.obj("$sort", MongodbCommands.obj(MongodbCommands.SCORE, MongodbCommands.num((-1)), "_id", MongodbCommands.num(1)));
      stages.add(_obj);
      Code.Entries _obj_1 = MongodbCommands.obj("$limit", shapes.window());
      stages.add(_obj_1);
      List<Code.Expr> _projection = MongodbShapes.projection(shapes, target, highlighted, explained);
      Iterables.<Code.Expr>addAll(stages, _projection);
      _xblockexpression = MongodbCommands.call("aggregate", "database", MongodbCommands.text(target.database), "collection", MongodbCommands.text(target.name), "pipeline", MongodbCommands.arr(stages), "key", MongodbCommands.text(target.key), 
        "options", aggregateOptions, "candidates", MongodbCommands.flag(true));
    }
    return _xblockexpression;
  }

  /**
   * rt.shape over a candidate window: the module's spec in the collection's names and the vector fields the candidates carry.
   */
  private Code.Call shapedCall(final Code.Call candidateSearch, final ShapeSpec shapes, final MongodbCollection target) {
    return MongodbCommands.call("shape", "candidates", candidateSearch, "spec", MongodbShapes.spec(shapes, target), "vectors", Code.texts(MongodbShapes.carried(shapes, target)));
  }

  private Code.Call aggregateCall(final MongodbCollection target, final Code.Items pipeline, final Code.Entries aggregateOptions, final boolean first) {
    Code.Text _text = MongodbCommands.text(target.database);
    Code.Text _text_1 = MongodbCommands.text(target.name);
    Code.Text _text_2 = MongodbCommands.text(target.key);
    Code.Flag _xifexpression = null;
    if (first) {
      _xifexpression = MongodbCommands.flag(true);
    } else {
      _xifexpression = null;
    }
    return MongodbCommands.call("aggregate", "database", _text, "collection", _text_1, "pipeline", pipeline, "key", _text_2, 
      "options", aggregateOptions, "first", _xifexpression);
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

  private Code.Entries boundsOf(final ScoreBounds scoreBounds) {
    Code.Entries _xblockexpression = null;
    {
      if ((scoreBounds == null)) {
        return null;
      }
      Code.Num _xifexpression = null;
      boolean _written = ModelUtil.written(scoreBounds, "min");
      if (_written) {
        _xifexpression = MongodbCommands.num(scoreBounds.getMin());
      } else {
        _xifexpression = null;
      }
      Code.Num _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(scoreBounds, "max");
      if (_written_1) {
        _xifexpression_1 = MongodbCommands.num(scoreBounds.getMax());
      } else {
        _xifexpression_1 = null;
      }
      final Code.Entries result = MongodbCommands.obj("min", _xifexpression, "max", _xifexpression_1);
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
   * A statement's filter, its keys and its tenant, namespace and partition conditions, as MQL conditions.
   */
  private List<Code.Expr> clauses(final EObject statement, final VectorCollection collection, final MongodbCollection target, final FilterBlock block, final Code.Expr keys, final boolean vector) {
    List<Code.Expr> _xblockexpression = null;
    {
      final List<Code.Expr> result = CollectionLiterals.<Code.Expr>newArrayList();
      if ((keys != null)) {
        Code.Entries _obj = MongodbCommands.obj("_id", MongodbCommands.obj("$in", keys));
        result.add(_obj);
      }
      if ((block != null)) {
        Code.Entries _query = new MongodbFilter(target, vector).query(block.getCondition());
        result.add(_query);
      }
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Entries _obj_1 = MongodbCommands.obj(target.tenant, MongodbCommands.obj("$eq", MongodbCommands.text(tenant.getName())));
        result.add(_obj_1);
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Entries _obj_2 = MongodbCommands.obj(target.namespace, MongodbCommands.obj("$eq", MongodbCommands.text(((String) namespace))));
        result.add(_obj_2);
      }
      Object _feature_1 = this.feature(statement, "partition");
      final Partition single = ((Partition) _feature_1);
      Object _feature_2 = this.feature(statement, "partitions");
      final List<Partition> several = ((List<Partition>) _feature_2);
      final List<Code.Expr> named = CollectionLiterals.<Code.Expr>newArrayList();
      if ((single != null)) {
        Code.Text _text = MongodbCommands.text(single.getName());
        named.add(_text);
      }
      if ((several != null)) {
        for (final Partition p : several) {
          Code.Text _text_1 = MongodbCommands.text(p.getName());
          named.add(_text_1);
        }
      }
      boolean _isEmpty = named.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        if ((target.partition == null)) {
          throw Unsupported.of("partitions of a collection declared without partitions");
        }
        Code.Entries _obj_3 = MongodbCommands.obj(target.partition, MongodbCommands.obj("$in", MongodbCommands.arr(named)));
        result.add(_obj_3);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Entries matchOf(final List<Code.Expr> conditions) {
    Code.Entries _xifexpression = null;
    boolean _isEmpty = conditions.isEmpty();
    if (_isEmpty) {
      _xifexpression = Code.entries();
    } else {
      Code.Entries _xifexpression_1 = null;
      int _size = conditions.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        Code.Expr _head = IterableExtensions.<Code.Expr>head(conditions);
        _xifexpression_1 = ((Code.Entries) _head);
      } else {
        _xifexpression_1 = MongodbCommands.obj("$and", MongodbCommands.arr(conditions));
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  /**
   * Keys as written (integers stay numbers, strings stay strings): _id holds the typed key.
   */
  private Code.Expr idsOf(final List<Literal> values) {
    Code.Items _xblockexpression = null;
    {
      boolean _isEmpty = values.isEmpty();
      if (_isEmpty) {
        return null;
      }
      final Function1<Literal, Code.Expr> _function = (Literal v) -> {
        return Literals.of(v);
      };
      _xblockexpression = Code.items(IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(values, _function)));
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses MongoDB lacks, with their reason, and clauses not generated yet.
   */
  private void rejectClauses(final EObject statement, final String... features) {
    for (final String featureName : features) {
      boolean _written = ModelUtil.written(statement, featureName);
      if (_written) {
        String _elvis = null;
        String _get = MongodbStatements.CLAUSES.get(featureName);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = featureName;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = MongodbStatements.PENDING_CLAUSES.get(featureName);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + MongodbStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = MongodbStatements.REASONS.get(featureName);
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
   * 'extension mongodb' blocks of queries carry aggregate options; extension items of other kinds are rejected.
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
      boolean _equals = Objects.equals(_vendor, MongodbGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on MongoDB");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
  }

  /**
   * The query vector: a vector literal or parameter (the runtimes send it as doubles), or for id input the stored vector (runtime).
   */
  private Code.Expr queryVectorOf(final QueryInput queryInput, final MongodbCollection.Field mapped) {
    Code.Expr _switchResult = null;
    EQueryInputMode _kind = queryInput.getKind();
    if (_kind != null) {
      switch (_kind) {
        case VECTOR:
          _switchResult = Literals.of(queryInput.getValue());
          break;
        case ID:
          _switchResult = new Code.Nil();
          break;
        case SPARSE_VECTOR:
          throw Unsupported.of("sparse_vector input; vectorSearch indexes take dense vectors only");
        case TEXT:
          throw Unsupported.of(("text input" + MongodbStatements.NO_MODELS));
        case MULTI_VECTOR:
          throw Unsupported.of("multi-vector input; a vectorSearch index compares one vector per document path");
        default:
          String _literal = queryInput.getKind().getLiteral();
          String _plus = (_literal + " input");
          String _plus_1 = (_plus + MongodbStatements.NO_MODELS);
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal = queryInput.getKind().getLiteral();
      String _plus = (_literal + " input");
      String _plus_1 = (_plus + MongodbStatements.NO_MODELS);
      throw Unsupported.of(_plus_1);
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

  private Code.Expr recordsOf(final RecordSource recordSource, final MongodbCollection target) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (recordSource instanceof FileSource) {
      _matched=true;
      _switchResult = MongodbCommands.call("read_records", "location", MongodbCommands.text(((FileSource)recordSource).getLocation()));
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

  /**
   * A record with the current field names; the key keeps its declared name (the runtime moves it to _id).
   */
  private Code.Entries recordOf(final RecordLiteral recordLiteral, final MongodbCollection target) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = recordLiteral.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final String fieldName = ModelUtil.fieldName(entry.getField());
          String _xifexpression = null;
          boolean _equals = Objects.equals(fieldName, target.key);
          if (_equals) {
            _xifexpression = target.key;
          } else {
            _xifexpression = target.field(fieldName).name();
          }
          result.put(_xifexpression, this.fieldValue(entry.getField(), entry.getValue()));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A record or set value: a geo_point value (a "lat,lon" string, which only a parameter can carry) is marked $geoPoint, which
   * the runtimes turn into a GeoJSON point, longitude first, for the 2dsphere index and $geoWithin.
   */
  private Code.Expr fieldValue(final Field declared, final Literal value) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Expr converted = Literals.of(value);
      if ((((!(declared instanceof PayloadField)) || (!Objects.equals(((PayloadField) declared).getType(), EScalarDType.GEO_POINT))) || (value instanceof NullLit))) {
        return converted;
      }
      _xblockexpression = MongodbCommands.obj(MongodbStatements.GEO_POINT, converted);
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
