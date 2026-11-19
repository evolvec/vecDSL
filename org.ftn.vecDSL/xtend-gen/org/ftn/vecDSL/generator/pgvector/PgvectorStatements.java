package org.ftn.vecDSL.generator.pgvector;

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
import org.ftn.vecDSL.vecDSL.DmlMethod;
import org.ftn.vecDSL.vecDSL.Drop;
import org.ftn.vecDSL.vecDSL.DropFieldOp;
import org.ftn.vecDSL.vecDSL.DropFunctionOp;
import org.ftn.vecDSL.vecDSL.DropSnapshot;
import org.ftn.vecDSL.vecDSL.EAuthType;
import org.ftn.vecDSL.vecDSL.EBoostKind;
import org.ftn.vecDSL.vecDSL.ECloneMode;
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
import org.ftn.vecDSL.vecDSL.Prefetch;
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
 * Maps each vecDSL statement to SQL for PostgreSQL with pgvector in the host-language-neutral code model. The SQL text is
 * built here (through PgvectorSql and PgvectorQueries) and passed to the runtime with its parameters, so the three host
 * languages send identical statements: query and value return json rows, execute runs DDL in one transaction, transaction
 * runs a vecDSL transaction, autocommit runs what PostgreSQL refuses inside a transaction block.
 */
@SuppressWarnings("all")
public class PgvectorStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; PgvectorCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for pgvector: ";

  /**
   * Suffix of constructs PostgreSQL supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no pgvector mapping yet";

  public static final String NO_MODELS = "; PostgreSQL has no embedding models (the pgvector image includes no model extension such as pgai), so generated programs supply their own vectors";

  public static final String EXTERNAL = "collections derived from external sources; PostgreSQL reads foreign files through file_fdw, which reads text and CSV, not parquet, lance, iceberg or vortex";

  public static final String NO_TTL = "ttl; PostgreSQL rows do not expire, and the pgvector image has no scheduler (pg_cron) to delete them";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; PostgreSQL has no per-table drop guard (event triggers need a superuser and act on the whole database)";

  public static final String NO_STORAGE = "storage settings; PostgreSQL keeps tables in its data directory or in tablespaces that a superuser creates for server directories";

  public static final String NO_TAGS = "collection tags; a PostgreSQL table carries one comment (the description) and no tags";

  public static final String NO_BM25 = "BM25 parameters; PostgreSQL full-text search ranks with ts_rank, which has no k1 or b (BM25 needs an extension such as ParadeDB pg_search, not in the pgvector image)";

  public static final String NO_SNAPSHOTS = "snapshots; PostgreSQL backs up with pg_dump and pg_basebackup, client programs outside SQL (pg_export_snapshot names a snapshot only for the transaction that exports it)";

  public static final String SINGLE_SERVER = "one PostgreSQL server holds the data; streaming replication is configured on the servers, and core PostgreSQL has no sharding";

  private static final String NO_GENERATION = "PostgreSQL has no generative models (the pgvector image includes no model extension)";

  private static final String NO_RERANKERS = "PostgreSQL has no reranking models (the pgvector image includes no model extension)";

  private static final String NO_METRICS = "metrics configuration; PostgreSQL statistics are server settings (postgresql.conf, shared_preload_libraries for pg_stat_statements), not SQL objects";

  private static final String NO_SCHEDULES = "backup schedules; PostgreSQL has no scheduler in the pgvector image (pg_cron) and no SQL backup";

  private static final String NO_CLUSTER_LIMITS = "cluster limits; PostgreSQL has no setting that limits the number of tables";

  private static final String NO_FILES = "file resources; text search dictionaries read files from the server\'s tsearch_data directory, which the driver cannot write";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why PostgreSQL cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "PostgreSQL reads the current committed state and keeps no queryable history of a table"), Pair.<String, String>of("explain", "PostgreSQL explains query plans (EXPLAIN), not scores"), Pair.<String, String>of("reranker", PgvectorStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated pgvector programs embed no query text" + PgvectorStatements.NO_MODELS)), Pair.<String, String>of("minShouldMatch", "a tsquery combines terms with and, or and phrase operators and has no minimum number of matching terms"), Pair.<String, String>of("fuzziness", "full-text search matches normalized lexemes exactly (pg_trgm similarity is a separate operator without ts_rank scores)"), Pair.<String, String>of("slop", "a tsquery phrase fixes the distance between terms (<N>) and has no slop")));

  /**
   * Clauses PostgreSQL supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "jsonb_path_exists")));

  private static final Map<String, String> PRIVILEGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("read", "SELECT"), Pair.<String, String>of("write", "INSERT, UPDATE, DELETE"), Pair.<String, String>of("admin", "ALL PRIVILEGES"), Pair.<String, String>of("all", "ALL PRIVILEGES"), Pair.<String, String>of("select", "SELECT"), Pair.<String, String>of("insert", "INSERT"), Pair.<String, String>of("update", "UPDATE"), Pair.<String, String>of("delete", "DELETE"), Pair.<String, String>of("truncate", "TRUNCATE"), Pair.<String, String>of("references", "REFERENCES"), Pair.<String, String>of("trigger", "TRIGGER"), Pair.<String, String>of("maintain", "MAINTAIN")));

  private static final List<String> SETTINGS = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("hnsw.ef_search", "hnsw.iterative_scan", "hnsw.max_scan_tuples", "hnsw.scan_mem_multiplier", "ivfflat.probes", "ivfflat.iterative_scan", "ivfflat.max_probes"));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final PgvectorSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public PgvectorStatements(final Script script) {
    this.script = script;
    PgvectorSchema _pgvectorSchema = new PgvectorSchema(this.notes, script);
    this.schema = _pgvectorSchema;
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
          _switchResult = "PostgreSQL full-text search indexes text columns directly (tsvector) and has no BM25 function producing sparse vectors";
          break;
        case "custom":
          _switchResult = "custom functions; has no pgvector mapping yet (generated columns)";
          break;
        default:
          _switchResult = "PostgreSQL has no model functions (the pgvector image includes no model extension such as pgai)";
          break;
      }
    } else {
      _switchResult = "PostgreSQL has no model functions (the pgvector image includes no model extension such as pgai)";
    }
    return _switchResult;
  }

  public static String collectionLimit(final String type) {
    return (("collection limits (" + type) + "); PostgreSQL has fixed limits (1600 columns per table, 2000 indexed and 16000 stored dimensions per vector) and no per-table limit settings");
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
            throw Unsupported.of(PgvectorStatements.NO_METRICS);
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
            throw Unsupported.of(PgvectorStatements.NO_CLUSTER_LIMITS);
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + PgvectorStatements.SINGLE_SERVER));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, PgvectorGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of(("extension parameter " + name));
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
          PgvectorSql.id(((Database)concept).getName());
          String _id = PgvectorSql.id(((Database)concept).getName());
          String _plus = ("CREATE DATABASE " + _id);
          final Code.Stmt create = PgvectorSchema.autocommit(_plus, null, null);
          if (ifNotExists) {
            Code.Call _databaseExists = this.databaseExists(((Database)concept).getName());
            Code.When _when = new Code.When(_databaseExists, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
            result.add(_when);
          } else {
            result.add(create);
          }
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
          final PgvectorSql.Table table = this.schema.table(((Alias)concept).getCollection());
          String _createView = PgvectorSchema.createView(((Alias)concept).getName(), table, ifNotExists);
          Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)), table.database());
          result.add(_execute);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          List<Code.Stmt> _createIndex = this.createIndex(((Index)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _createIndex = this.createIndex(((Index)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createIndex);
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
          List<Code.Stmt> _addPartition = this.addPartition(this.member(concept, "a partition"), ((Partition)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _addPartition);
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
          String _functionReason = PgvectorStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          List<Code.Stmt> _createAnalyzer = this.schema.createAnalyzer(((Analyzer)concept), this.analyzerDatabase(((Analyzer)concept)));
          Iterables.<Code.Stmt>addAll(result, _createAnalyzer);
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          List<Code.Stmt> _analyzerFilter = this.analyzerFilter(((AnalyzerFilter)concept));
          Iterables.<Code.Stmt>addAll(result, _analyzerFilter);
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          List<Code.Stmt> _addLimit = this.addLimit(((FieldLimit)concept));
          Iterables.<Code.Stmt>addAll(result, _addLimit);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + PgvectorStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + PgvectorStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + PgvectorStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization outside a vector index; pgvector quantizes through the expression of an hnsw or ivfflat index");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + PgvectorStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + PgvectorStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_STORAGE);
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
          throw Unsupported.of(PgvectorStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (PgvectorStatements.NOT_MAPPED + _name);
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
      throw Unsupported.of("embedded deployments; the PostgreSQL drivers connect to a server (an embedded or WASM PostgreSQL is not the target)");
    }
    if (((concept.getApiKey() != null) || (concept.getToken() != null))) {
      throw Unsupported.of("api keys and tokens; PostgreSQL authenticates users with passwords, certificates or the server\'s other methods");
    }
    Literal _password = concept.getPassword();
    if ((_password instanceof StringLit)) {
      throw Unsupported.of("a password written in the script; read it from an environment variable with env(...)");
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      PgvectorSql.id(concept.getDatabaseName());
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; PostgreSQL speaks its own protocol on one port");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, PgvectorGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
    this.schema.note(concept, "timeoutMillis", "cloud", "region", "project");
  }

  private Code.Call databaseExists(final String name) {
    Code.Call _xblockexpression = null;
    {
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.text(name), "text");
      String _plus = ("SELECT to_json(EXISTS (SELECT 1 FROM pg_database WHERE datname = " + _bind);
      String _plus_1 = (_plus + "))");
      _xblockexpression = PgvectorSchema.call("exists", _plus_1, q, null, null);
    }
    return _xblockexpression;
  }

  /**
   * Password authentication is the connection's; roles are PostgreSQL roles; other methods are server configuration.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((!Objects.equals(auth.getType(), EAuthType.NONE)) && (!Objects.equals(auth.getType(), EAuthType.USERNAME_PASSWORD)))) {
        String _literal = auth.getType().getLiteral();
        String _plus = (_literal + " authentication; PostgreSQL authenticates with passwords, certificates, Kerberos, LDAP and RADIUS, configured on the server (pg_hba.conf), and has no api keys or tokens");
        throw Unsupported.of(_plus);
      }
      Literal _token = auth.getToken();
      if ((_token instanceof StringLit)) {
        throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
      }
      String _certificatePath = auth.getCertificatePath();
      boolean _tripleNotEquals = (_certificatePath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("client certificates; has no pgvector mapping yet (sslcert and sslkey connection parameters)");
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; core PostgreSQL has no transparent data encryption");
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        throw Unsupported.of("audit logs; PostgreSQL audits through the pgaudit extension, which the pgvector image does not include");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = auth.getRoles();
      for (final RoleBinding binding : _roles) {
        List<Code.Stmt> _role = this.role(binding, ifNotExists);
        Iterables.<Code.Stmt>addAll(result, _role);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A role without login and its grants on a collection (or on every table of the public schema).
   */
  private List<Code.Stmt> role(final RoleBinding binding, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = binding.getRoleName();
      PgvectorSql.id(name);
      PgvectorSql.Table _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.schema.table(binding.getCollection());
      }
      final PgvectorSql.Table table = _xifexpression;
      String _database = null;
      if (table!=null) {
        _database=table.database();
      }
      final String database = _database;
      final List<String> grants = CollectionLiterals.<String>newArrayList();
      EList<String> _privileges = binding.getPrivileges();
      for (final String privilege : _privileges) {
        {
          final String sql = PgvectorStatements.PRIVILEGES.get(privilege.toLowerCase());
          if ((sql == null)) {
            throw Unsupported.of((("the privilege " + privilege) + "; PostgreSQL grants SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER and MAINTAIN on tables"));
          }
          grants.add(sql);
        }
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      String _id = PgvectorSql.id(name);
      String _plus = ("CREATE ROLE " + _id);
      String _plus_1 = (_plus + " NOLOGIN");
      final Code.Stmt create = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_1)), database);
      if (ifNotExists) {
        Code.Call _roleExists = this.roleExists(name, database);
        Code.When _when = new Code.When(_roleExists, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
        result.add(_when);
      } else {
        result.add(create);
      }
      boolean _isEmpty = grants.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _xifexpression_1 = null;
        if ((table == null)) {
          _xifexpression_1 = "ALL TABLES IN SCHEMA public";
        } else {
          String _table = PgvectorSql.table(table);
          _xifexpression_1 = ("TABLE " + _table);
        }
        final String on = _xifexpression_1;
        String _join = IterableExtensions.join(grants, ", ");
        String _plus_2 = ("GRANT " + _join);
        String _plus_3 = (_plus_2 + " ON ");
        String _plus_4 = (_plus_3 + on);
        String _plus_5 = (_plus_4 + " TO ");
        String _id_1 = PgvectorSql.id(name);
        String _plus_6 = (_plus_5 + _id_1);
        Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_6)), database);
        result.add(_execute);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Call roleExists(final String name, final String database) {
    Code.Call _xblockexpression = null;
    {
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.text(name), "text");
      String _plus = ("SELECT to_json(EXISTS (SELECT 1 FROM pg_roles WHERE rolname = " + _bind);
      String _plus_1 = (_plus + "))");
      _xblockexpression = PgvectorSchema.call("exists", _plus_1, q, null, database);
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
    throw Unsupported.of((what + " outside a collection; PostgreSQL adds it to a table"));
  }

  private String analyzerDatabase(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      final VectorCollection collection = ModelUtil.owningCollection(analyzer);
      String _xifexpression = null;
      if ((collection == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = PgvectorSchema.databaseOf(collection);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * A filter of an analyzer: the copied configuration applies it already, so creating it checks the configuration exists.
   */
  private List<Code.Stmt> analyzerFilter(final AnalyzerFilter filter) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(filter, Analyzer.class);
      if ((analyzer == null)) {
        throw Unsupported.of("an analyzer filter outside an analyzer; a text search configuration takes its dictionaries when it is created");
      }
      String _xifexpression = null;
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression = "simple";
      } else {
        _xifexpression = PgvectorSql.configuration(analyzer.getLanguage());
      }
      final String source = _xifexpression;
      String _literal = filter.getType().getLiteral();
      String _elvis = null;
      if (source != null) {
        _elvis = source;
      } else {
        _elvis = "simple";
      }
      PgvectorSchema.checkFilter(_literal, _elvis);
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.text(PgvectorSchema.configName(analyzer)), "text");
      String _plus = ("SELECT to_json(" + _bind);
      String _plus_1 = (_plus + "::regconfig::text)");
      Code.Call _call = PgvectorSchema.call("value", _plus_1, q, null, this.analyzerDatabase(analyzer));
      Code.Run _run = new Code.Run(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A field limit added to an existing column: a named CHECK constraint.
   */
  private List<Code.Stmt> addLimit(final FieldLimit limit) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = EcoreUtil2.<Field>getContainerOfType(limit, Field.class);
      if ((field == null)) {
        throw Unsupported.of("a field limit outside a field declaration; a CHECK constraint belongs to a column");
      }
      final VectorCollection collection = this.member(field, "a field limit");
      final PgvectorSql.Table table = this.schema.table(collection);
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      final String check = PgvectorSchema.check(limit, field);
      String _table = PgvectorSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD ");
      String _replace = check.replace("{}", PgvectorSql.id(column.name()));
      String _plus_2 = (_plus_1 + _replace);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)), table.database());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * An index created on an existing collection; online builds it CONCURRENTLY, outside a transaction block.
   */
  private List<Code.Stmt> createIndex(final Index index, final boolean ifNotExists) {
    List<Code.Stmt> _switchResult = null;
    boolean _matched = false;
    if (index instanceof VectorIndex) {
      _matched=true;
      List<Code.Stmt> _xblockexpression = null;
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
        if ((field == null)) {
          throw Unsupported.of("a vector index without a vector field");
        }
        final VectorCollection collection = this.member(field, "a vector index");
        final PgvectorSql.Table table = this.schema.table(collection);
        final String sql = this.schema.vectorIndex(collection, table, field, ((VectorIndex)index), ifNotExists);
        if ((sql == null)) {
          return this.require(collection);
        }
        boolean _isOnline = ((VectorIndex)index).isOnline();
        if (_isOnline) {
          Code.Stmt _autocommit = PgvectorSchema.autocommit(sql.replaceFirst("CREATE INDEX ", "CREATE INDEX CONCURRENTLY "), table.database(), null);
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_autocommit));
        }
        Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)), table.database());
        _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      _switchResult = _xblockexpression;
    }
    if (!_matched) {
      if (index instanceof PayloadIndex) {
        _matched=true;
        List<Code.Stmt> _xblockexpression = null;
        {
          final Field field = ((PayloadIndex)index).getTarget().getField();
          VectorCollection _elvis = null;
          VectorCollection _targetCollection = ModelUtil.targetCollection(index);
          if (_targetCollection != null) {
            _elvis = _targetCollection;
          } else {
            VectorCollection _xifexpression = null;
            if ((field == null)) {
              _xifexpression = null;
            } else {
              _xifexpression = ModelUtil.owningCollection(field);
            }
            _elvis = _xifexpression;
          }
          final VectorCollection collection = _elvis;
          if ((collection == null)) {
            throw Unsupported.of("a payload index outside a collection");
          }
          final PgvectorSql.Table table = this.schema.table(collection);
          String _payloadIndex = this.schema.payloadIndex(collection, table, ((PayloadIndex)index), ifNotExists);
          Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_payloadIndex)), table.database());
          _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
        _switchResult = _xblockexpression;
      }
    }
    return _switchResult;
  }

  /**
   * Existence check of a collection: the statement fails when the table is missing.
   */
  private List<Code.Stmt> require(final VectorCollection collection) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      String _table = PgvectorSql.table(table);
      String _plus = ("SELECT NULL::json FROM " + _table);
      String _plus_1 = (_plus + " WHERE false");
      Code.Call _call = PgvectorSchema.call("query", _plus_1, null, null, table.database());
      Code.Run _run = new Code.Run(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * ALTER TABLE ADD COLUMN; a vector field with an index gets the index in the same transaction.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      Code.Stmt _execute = PgvectorSchema.execute(this.addColumn(collection, table, field), table.database());
      final List<Code.Stmt> result = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      this.schema.evolve(collection, table.with(this.schema.column(collection, field)));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<String> addColumn(final VectorCollection collection, final PgvectorSql.Table table, final Field field) {
    List<String> _xblockexpression = null;
    {
      final PgvectorSql.Column column = this.schema.column(collection, field);
      final List<String> result = CollectionLiterals.<String>newArrayList();
      PgvectorSql.Column _xifexpression = null;
      if ((field instanceof VectorField)) {
        _xifexpression = this.nullableVector(column);
      } else {
        _xifexpression = column;
      }
      final String definition = PgvectorSql.columnDefinition(_xifexpression, false);
      String _table = PgvectorSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD COLUMN ");
      String _plus_2 = (_plus_1 + definition);
      result.add(_plus_2);
      if ((field instanceof VectorField)) {
        final VectorIndex index = this.schema.indexOf(collection, ((VectorField) field));
        if ((index != null)) {
          String _vectorIndex = this.schema.vectorIndex(collection, table, ((VectorField) field), index, false);
          result.add(_vectorIndex);
        }
      }
      if (((column.config() != null) && (field instanceof PayloadField))) {
        String _createPayloadIndex = PgvectorSql.createPayloadIndex(table, PgvectorSchema.ftsIndexName(collection, ((PayloadField) field)), column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), "fts", true);
        result.add(_createPayloadIndex);
      }
      _xblockexpression = IterableExtensions.<String>toList(IterableExtensions.<String>filterNull(result));
    }
    return _xblockexpression;
  }

  /**
   * Rows that exist when a vector column is added have no vector, so the added column is nullable.
   */
  private PgvectorSql.Column nullableVector(final PgvectorSql.Column column) {
    String _name = column.name();
    String _type = column.type();
    PgvectorSql.Vec _vec = column.vec();
    return new PgvectorSql.Column(_name, _type, null, false, _vec, null, true, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
  }

  private List<Code.Stmt> addPartition(final VectorCollection collection, final Partition partition, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      String _partition = table.partition();
      boolean _tripleEquals = (_partition == null);
      if (_tripleEquals) {
        throw Unsupported.of("a partition of a collection declared without partitions; PostgreSQL cannot partition an existing table in place");
      }
      if (((partition.getKey() != null) || (!partition.getSubPartitions().isEmpty()))) {
        throw Unsupported.of("partitions by key and sub-partitions; has no pgvector mapping yet (hash and sub-partitioned tables)");
      }
      String _createPartition = PgvectorSql.createPartition(table, partition.getName(), false, ifNotExists);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createPartition)), table.database());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        if (((!it.getOperations().isEmpty()) || (it.getNewName() == null))) {
          throw Unsupported.of("alter operations on an alias; an alias is a view, which can be renamed");
        }
        final Alias alias = ((Alias) concept);
        String _id = PgvectorSql.id(alias.getName());
        String _plus = ("ALTER VIEW " + _id);
        String _plus_1 = (_plus + " RENAME TO ");
        String _id_1 = PgvectorSql.id(it.getNewName());
        String _plus_2 = (_plus_1 + _id_1);
        Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)), this.schema.table(alias.getCollection()).database());
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus_3 = ("altering a " + _name);
        String _plus_4 = (_plus_3 + "; PostgreSQL alters tables and views");
        throw Unsupported.of(_plus_4);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      PgvectorSql.Table table = this.schema.table(collection);
      final String database = table.database();
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        {
          List<String> _alter = this.alter(collection, table, operation);
          Iterables.<String>addAll(ddl, _alter);
          table = this.evolved(collection, table, operation);
        }
      }
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        String _table = PgvectorSql.table(table);
        String _plus_5 = ("ALTER TABLE " + _table);
        String _plus_6 = (_plus_5 + " RENAME TO ");
        String _id_2 = PgvectorSql.id(it.getNewName());
        String _plus_7 = (_plus_6 + _id_2);
        ddl.add(_plus_7);
        table = table.named(it.getNewName());
      }
      List<Code.Stmt> _xifexpression = null;
      boolean _isEmpty = ddl.isEmpty();
      if (_isEmpty) {
        _xifexpression = this.require(collection);
      } else {
        Code.Stmt _execute_1 = PgvectorSchema.execute(ddl, database);
        _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute_1));
      }
      final List<Code.Stmt> result = _xifexpression;
      this.schema.evolve(collection, table);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The table after an alter operation, for the statements that follow it.
   */
  private PgvectorSql.Table evolved(final VectorCollection collection, final PgvectorSql.Table table, final AlterOperation operation) {
    PgvectorSql.Table _switchResult = null;
    boolean _matched = false;
    if (operation instanceof AddFieldOp) {
      _matched=true;
      _switchResult = table.with(this.schema.column(collection, ((AddFieldOp)operation).getNewField()));
    }
    if (!_matched) {
      if (operation instanceof DropFieldOp) {
        _matched=true;
        _switchResult = table.without(ModelUtil.fieldName(((DropFieldOp)operation).getField()));
      }
    }
    if (!_matched) {
      if (operation instanceof RenameFieldOp) {
        _matched=true;
        _switchResult = table.renamed(ModelUtil.fieldName(((RenameFieldOp)operation).getField()), ((RenameFieldOp)operation).getNewName());
      }
    }
    if (!_matched) {
      _switchResult = table;
    }
    return _switchResult;
  }

  public List<String> alter(final VectorCollection collection, final PgvectorSql.Table table, final AlterOperation operation) {
    List<String> _switchResult = null;
    boolean _matched = false;
    if (operation instanceof AddFieldOp) {
      _matched=true;
      _switchResult = this.addColumn(collection, table, ((AddFieldOp)operation).getNewField());
    }
    if (!_matched) {
      if (operation instanceof DropFieldOp) {
        _matched=true;
        String _dropColumn = this.dropColumn(collection, table, ((DropFieldOp)operation).getField());
        _switchResult = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_dropColumn));
      }
    }
    if (!_matched) {
      if (operation instanceof RenameFieldOp) {
        _matched=true;
        List<String> _xblockexpression = null;
        {
          Field _field = ((RenameFieldOp)operation).getField();
          Field _primaryKey = collection.getPrimaryKey();
          boolean _tripleEquals = (_field == _primaryKey);
          if (_tripleEquals) {
            throw Unsupported.of("renaming the primary key; the generated statements address the key by its declared name");
          }
          PgvectorSql.id(((RenameFieldOp)operation).getNewName());
          String _table = PgvectorSql.table(table);
          String _plus = ("ALTER TABLE " + _table);
          String _plus_1 = (_plus + " RENAME COLUMN ");
          String _id = PgvectorSql.id(table.column(ModelUtil.fieldName(((RenameFieldOp)operation).getField())).name());
          String _plus_2 = (_plus_1 + _id);
          String _plus_3 = (_plus_2 + " TO ");
          String _id_1 = PgvectorSql.id(((RenameFieldOp)operation).getNewName());
          String _plus_4 = (_plus_3 + _id_1);
          _xblockexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_4));
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (operation instanceof SetPropertyOp) {
        _matched=true;
        _switchResult = this.setProperty(table, ((SetPropertyOp)operation));
      }
    }
    if (!_matched) {
      if (operation instanceof AddAliasOp) {
        _matched=true;
        String _createView = PgvectorSchema.createView(((AddAliasOp)operation).getNewAlias().getName(), table, false);
        _switchResult = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView));
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = PgvectorStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = PgvectorStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (PgvectorStatements.NOT_MAPPED + _name);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private String dropColumn(final VectorCollection collection, final PgvectorSql.Table table, final Field field) {
    String _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
      }
      String _table = PgvectorSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " DROP COLUMN ");
      String _id = PgvectorSql.id(table.column(ModelUtil.fieldName(field)).name());
      _xblockexpression = (_plus_1 + _id);
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: the description is the table comment; consistency is met by every committed write.
   */
  private List<String> setProperty(final PgvectorSql.Table table, final SetPropertyOp operation) {
    List<String> _switchResult = null;
    ECollectionProperty _property = operation.getProperty();
    if (_property != null) {
      switch (_property) {
        case DESCRIPTION:
          List<String> _xblockexpression = null;
          {
            Literal _value = operation.getValue();
            boolean _not = (!(_value instanceof StringLit));
            if (_not) {
              throw Unsupported.of("a description that is not a string literal; COMMENT takes a constant");
            }
            String _table = PgvectorSql.table(table);
            String _plus = ("COMMENT ON TABLE " + _table);
            String _plus_1 = (_plus + " IS ");
            Literal _value_1 = operation.getValue();
            String _literal = PgvectorSql.literal(((StringLit) _value_1).getValue());
            String _plus_2 = (_plus_1 + _literal);
            _xblockexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2));
          }
          _switchResult = _xblockexpression;
          break;
        case CONSISTENCY:
          _switchResult = CollectionLiterals.<String>newArrayList();
          break;
        case TAGS:
          throw Unsupported.of(PgvectorStatements.NO_TAGS);
        case DYNAMIC:
          throw Unsupported.of((("dynamic; " + PgvectorStatements.PENDING) + " (a jsonb column for undeclared fields)"));
        case TTL:
          throw Unsupported.of(PgvectorStatements.NO_TTL);
        case DELETION_PROTECTION:
          throw Unsupported.of(PgvectorStatements.NO_DELETION_PROTECTION);
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
      String _xifexpression = null;
      boolean _isIfExists = it.isIfExists();
      if (_isIfExists) {
        _xifexpression = "IF EXISTS ";
      } else {
        _xifexpression = "";
      }
      final String exists = _xifexpression;
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (concept instanceof VectorCollection) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          final PgvectorSql.Table table = this.schema.table(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          String _table = PgvectorSql.table(table);
          String _plus = (("DROP TABLE " + exists) + _table);
          Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), table.database());
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            String _id = PgvectorSql.id(((Database)concept).getName());
            String _plus = (("DROP DATABASE " + exists) + _id);
            String _plus_1 = (_plus + " WITH (FORCE)");
            final Code.Stmt drop = PgvectorSchema.autocommit(_plus_1, null, ((Database)concept).getName());
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), this.schema.table(this.member(concept, "a tenant")).tenant(), ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), this.schema.table(this.member(concept, "a namespace")).namespace(), ((Namespace)concept).getKey());
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
          String _id = PgvectorSql.id(((Alias)concept).getName());
          String _plus = (("DROP VIEW " + exists) + _id);
          Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), this.schema.table(((Alias)concept).getCollection()).database());
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final VectorCollection collection = this.member(concept, "a partition");
            final PgvectorSql.Table table = this.schema.table(collection);
            String _partition = table.partition();
            boolean _tripleEquals = (_partition == null);
            if (_tripleEquals) {
              throw Unsupported.of("a partition of a collection declared without partitions");
            }
            String _partitionTable = PgvectorSql.partitionTable(table, ((Partition)concept).getName());
            String _plus = (("DROP TABLE " + exists) + _partitionTable);
            Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), table.database());
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          _switchResult = this.dropIndex(((Index)concept), exists);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          _switchResult = this.dropIndex(((Index)concept), exists);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = PgvectorStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; the quantized expression belongs to its vector index (drop the index)");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a PostgreSQL object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; the program drops databases and tables, not the server");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + PgvectorStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          String _id = PgvectorSql.id(((Analyzer)concept).getName());
          String _plus = ((("DROP TEXT SEARCH CONFIGURATION " + exists) + "public.") + _id);
          Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), this.analyzerDatabase(((Analyzer)concept)));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; a text search configuration keeps the dictionaries of its language (drop the analyzer)");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          _switchResult = this.dropLimit(((FieldLimit)concept), exists);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + PgvectorStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + PgvectorStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + PgvectorStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + PgvectorStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + PgvectorStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; PostgreSQL authentication methods are server configuration (pg_hba.conf)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          _switchResult = this.dropRole(((RoleBinding)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(PgvectorStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((PgvectorStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant or namespace deletes its rows.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String column, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _table = PgvectorSql.table(table);
      String _plus = ("DELETE FROM " + _table);
      String _plus_1 = (_plus + " ");
      String _plus_2 = (_plus_1 + PgvectorSql.ALIAS);
      String _plus_3 = (_plus_2 + " WHERE ");
      String _col = PgvectorSql.col(column);
      String _plus_4 = (_plus_3 + _col);
      String _plus_5 = (_plus_4 + " = ");
      String _bind = q.bind(Code.text(value), "text");
      String _plus_6 = (_plus_5 + _bind);
      final String sql = PgvectorSql.counted(_plus_6, "delete_count");
      Code.Call _call = PgvectorSchema.call("value", sql, q, null, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      String _dropColumn = this.dropColumn(collection, table, field);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_dropColumn)), table.database());
      final List<Code.Stmt> result = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      this.schema.evolve(collection, table.without(ModelUtil.fieldName(field)));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropIndex(final Index index, final String exists) {
    String _id = PgvectorSql.id(this.indexName(index));
    String _plus = (("DROP INDEX " + exists) + _id);
    Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), this.indexDatabase(index));
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
  }

  private String indexName(final Index index) {
    String _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(index);
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      _xblockexpression = PgvectorSchema.indexName(collection, index.getName());
    }
    return _xblockexpression;
  }

  private String indexDatabase(final Index index) {
    String _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(index);
      String _xifexpression = null;
      if ((collection == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = PgvectorSchema.databaseOf(collection);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private VectorCollection indexCollection(final Index index) {
    VectorCollection _switchResult = null;
    boolean _matched = false;
    if (index instanceof VectorIndex) {
      _matched=true;
      VectorCollection _xblockexpression = null;
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
        _xblockexpression = _xifexpression;
      }
      _switchResult = _xblockexpression;
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
    return _switchResult;
  }

  private List<Code.Stmt> dropLimit(final FieldLimit limit, final String exists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = EcoreUtil2.<Field>getContainerOfType(limit, Field.class);
      if ((field == null)) {
        throw Unsupported.of("a field limit outside a field declaration");
      }
      final PgvectorSql.Table table = this.schema.table(this.member(field, "a field limit"));
      String _table = PgvectorSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " DROP CONSTRAINT ");
      String _plus_2 = (_plus_1 + exists);
      String _id = PgvectorSql.id(limit.getName());
      String _plus_3 = (_plus_2 + _id);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_3)), table.database());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * DROP OWNED revokes the role's privileges in the database; then the role is dropped.
   */
  private List<Code.Stmt> dropRole(final RoleBinding binding) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = binding.getRoleName();
      PgvectorSql.id(name);
      String _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.schema.table(binding.getCollection()).database();
      }
      final String database = _xifexpression;
      String _id = PgvectorSql.id(name);
      String _plus = ("DROP OWNED BY " + _id);
      String _id_1 = PgvectorSql.id(name);
      String _plus_1 = ("DROP ROLE " + _id_1);
      final Code.Stmt drop = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus, _plus_1)), database);
      Code.Call _roleExists = this.roleExists(name, database);
      Code.When _when = new Code.When(_roleExists, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
    }
    return _xblockexpression;
  }

  /**
   * An alias is a view: the swap replaces its query in one transaction.
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(it.getNewCollection());
      final PgvectorSql.Table old = this.schema.table(it.getOldCollection());
      String _database = table.database();
      String _database_1 = old.database();
      boolean _notEquals = (!Objects.equals(_database, _database_1));
      if (_notEquals) {
        throw Unsupported.of("swapping an alias across databases; a view reads tables of its own database");
      }
      String _id = PgvectorSql.id(it.getAlias().getName());
      String _plus = ("DROP VIEW IF EXISTS " + _id);
      String _createView = PgvectorSchema.createView(it.getAlias().getName(), table, false);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus, _createView)), table.database());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(PgvectorStatements.NO_TTL);
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      String _xifexpression = null;
      boolean _written_1 = ModelUtil.written(it, "onConflict");
      if (_written_1) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      final String policy = _xifexpression;
      VectorCollection _collection = it.getCollection();
      RecordSource _source = it.getSource();
      String _xifexpression_1 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_1 = "insert_count";
      } else {
        _xifexpression_1 = "upsert_count";
      }
      Code.Call _write = this.write(_collection, _source, policy, _xifexpression_1, it);
      Code.Return _return = new Code.Return(_write);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(PgvectorStatements.NO_TTL);
      }
      this.schema.note(it, "recordCountHint", "batchSize");
      this.writeOptions(it);
      Code.Call _write = this.write(it.getCollection(), it.getSource(), "replace", "upsert_count", it);
      Code.Return _return = new Code.Return(_write);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * One INSERT of the rows (as a json array parameter) with the slice values of the statement.
   */
  private Code.Call write(final VectorCollection collection, final RecordSource source, final String policy, final String counter, final EObject statement) {
    Code.Call _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(collection);
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final String sql = PgvectorSql.insertRows(q, table, this.rows(source, table), this.slices(statement, collection, table), policy, counter);
      _xblockexpression = PgvectorSchema.call("value", sql, q, null, table.database());
    }
    return _xblockexpression;
  }

  /**
   * Column values for tenant, namespace and partition of a write; partitioned tables default to the default partition.
   */
  private Map<String, Code.Expr> slices(final EObject statement, final VectorCollection collection, final PgvectorSql.Table table) {
    Map<String, Code.Expr> _xblockexpression = null;
    {
      final Map<String, Code.Expr> result = CollectionLiterals.<String, Code.Expr>newLinkedHashMap();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.put(table.tenant(), Code.text(tenant.getName()));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        result.put(table.namespace(), Code.text(namespace));
      }
      Object _feature_2 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_2);
      if (((partition != null) && (table.partition() == null))) {
        throw Unsupported.of("a partition of a collection declared without partitions");
      }
      String _partition = table.partition();
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
          String _defaultPartition = PgvectorSchema.defaultPartition(collection);
          _elvis_1 = _defaultPartition;
        }
        if (_elvis_1 != null) {
          _elvis = _elvis_1;
        } else {
          _elvis = PgvectorSchema.DEFAULT_PARTITION;
        }
        result.put(table.partition(), Code.text(_elvis));
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
        throw Unsupported.of("if-version; PostgreSQL rows carry no version number (xmin is the id of the inserting transaction), and the generator adds no version column");
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if (((((it.getValues() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty()))) {
          throw Unsupported.of("update from records with \'set\', remove-fields, array-append or array-remove; each record carries its own values");
        }
        final Code.Expr records = this.rows(it.getSource(), table);
        final List<String> restrictions = CollectionLiterals.<String>newArrayList();
        boolean _isEmpty = it.getIds().isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          String _keyIn = PgvectorSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
          restrictions.add(_keyIn);
        }
        String _where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
        restrictions.add(_where);
        final String fromRecords = PgvectorSql.updateRows(q, table, records, (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE)), PgvectorSql.and(restrictions));
        Code.Call _call = PgvectorSchema.call("value", PgvectorSql.counted(fromRecords, "update_count"), q, null, table.database());
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
      }
      final List<String> sets = CollectionLiterals.<String>newArrayList();
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
              throw Unsupported.of("updating the primary key; the key identifies the row");
            }
            final PgvectorSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
            final String name = column.name();
            given.add(name);
            final String value = PgvectorSql.value(q, column, this.convert(entry.getValue(), column));
            boolean _contains = it.getAppendFields().contains(entry.getField());
            if (_contains) {
              this.arrayField(entry.getField(), column);
              String _id = PgvectorSql.id(name);
              String _plus = (_id + " = coalesce(");
              String _id_1 = PgvectorSql.id(name);
              String _plus_1 = (_plus + _id_1);
              String _plus_2 = (_plus_1 + ", \'{}\') || ");
              String _plus_3 = (_plus_2 + value);
              sets.add(_plus_3);
            } else {
              boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
              if (_contains_1) {
                this.arrayField(entry.getField(), column);
                String _id_2 = PgvectorSql.id(name);
                String _plus_4 = (_id_2 + " = ARRAY(SELECT x FROM unnest(");
                String _id_3 = PgvectorSql.id(name);
                String _plus_5 = (_plus_4 + _id_3);
                String _plus_6 = (_plus_5 + ") WITH ORDINALITY AS u(x, i) WHERE x <> ALL(");
                String _plus_7 = (_plus_6 + value);
                String _plus_8 = (_plus_7 + ") ORDER BY i)");
                sets.add(_plus_8);
              } else {
                String _id_4 = PgvectorSql.id(name);
                String _plus_9 = (_id_4 + " = ");
                String _plus_10 = (_plus_9 + value);
                sets.add(_plus_10);
              }
            }
          }
        }
      }
      EList<Field> _appendFields = it.getAppendFields();
      EList<Field> _arrayRemoveFields = it.getArrayRemoveFields();
      Iterable<Field> _plus = Iterables.<Field>concat(_appendFields, _arrayRemoveFields);
      for (final Field field : _plus) {
        boolean _contains = given.contains(table.column(ModelUtil.fieldName(field)).name());
        boolean _not_1 = (!_contains);
        if (_not_1) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus_1 = ("array-append or array-remove on " + _fieldName);
          String _plus_2 = (_plus_1 + " without items in \'set\'");
          throw Unsupported.of(_plus_2);
        }
      }
      EList<Field> _removedFields = it.getRemovedFields();
      for (final Field field_1 : _removedFields) {
        {
          Field _primaryKey = it.getCollection().getPrimaryKey();
          boolean _tripleEquals = (field_1 == _primaryKey);
          if (_tripleEquals) {
            throw Unsupported.of("removing the primary key");
          }
          final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field_1));
          if (((column.vec() != null) && (!column.nullable()))) {
            String _name = column.name();
            String _plus_3 = ("remove-fields on vector field " + _name);
            String _plus_4 = (_plus_3 + ", which is not nullable");
            throw Unsupported.of(_plus_4);
          }
          String _id = PgvectorSql.id(column.name());
          String _plus_5 = (_id + " = NULL");
          sets.add(_plus_5);
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        List<PgvectorSql.Column> _columns = table.columns();
        for (final PgvectorSql.Column column : _columns) {
          if (((((!Objects.equals(column.name(), table.key())) && (column.vec() == null)) && (!given.contains(column.name()))) && (!IterableExtensions.<Field>exists(it.getRemovedFields(), ((Function1<Field, Boolean>) (Field f) -> {
            String _name = table.column(ModelUtil.fieldName(f)).name();
            String _name_1 = column.name();
            return Boolean.valueOf(Objects.equals(_name, _name_1));
          }))))) {
            String _id = PgvectorSql.id(column.name());
            String _plus_3 = (_id + " = DEFAULT");
            sets.add(_plus_3);
          }
        }
      }
      boolean _isEmpty_1 = sets.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("an update without \'set\' or \'remove-fields\'");
      }
      final List<String> conditions = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty_2 = it.getIds().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        String _keyIn_1 = PgvectorSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function_1)));
        conditions.add(_keyIn_1);
      }
      String _where_1 = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
      conditions.add(_where_1);
      String _table = PgvectorSql.table(table);
      String _plus_4 = ("UPDATE " + _table);
      String _plus_5 = (_plus_4 + " ");
      String _plus_6 = (_plus_5 + PgvectorSql.ALIAS);
      String _plus_7 = (_plus_6 + " SET ");
      String _join = IterableExtensions.join(sets, ", ");
      String _plus_8 = (_plus_7 + _join);
      String _whereClause = PgvectorSql.whereClause(PgvectorSql.and(conditions));
      final String update = (_plus_8 + _whereClause);
      Code.Call _call_1 = PgvectorSchema.call("value", PgvectorSql.counted(update, "update_count"), q, null, table.database());
      Code.Return _return_1 = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  private void arrayField(final Field field, final PgvectorSql.Column column) {
    boolean _array = column.array();
    boolean _not = (!_array);
    if (_not) {
      String _fieldName = ModelUtil.fieldName(field);
      String _plus = ("array-append or array-remove on " + _fieldName);
      String _plus_1 = (_plus + ", which is not an array field");
      throw Unsupported.of(_plus_1);
    }
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final List<String> conditions = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        String _keyIn = PgvectorSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
        conditions.add(_keyIn);
      }
      String _where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
      conditions.add(_where);
      final String condition = PgvectorSql.and(conditions);
      if (((condition == null) && (!it.isDeleteAll()))) {
        throw Unsupported.of("a delete without ids, filter or \'all\'");
      }
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        String _table = PgvectorSql.table(table);
        String _plus = ((((((((("(" + PgvectorSql.ALIAS) + ".tableoid, ") + PgvectorSql.ALIAS) + ".ctid) IN (SELECT ") + PgvectorSql.ALIAS) + ".tableoid, ") + PgvectorSql.ALIAS) + ".ctid FROM ") + _table);
        String _plus_1 = (_plus + " ");
        String _plus_2 = (_plus_1 + PgvectorSql.ALIAS);
        String _whereClause = PgvectorSql.whereClause(condition);
        String _plus_3 = (_plus_2 + _whereClause);
        String _plus_4 = (_plus_3 + " ORDER BY ");
        String _col = PgvectorSql.col(table.key());
        String _plus_5 = (_plus_4 + _col);
        String _plus_6 = (_plus_5 + " LIMIT ");
        int _deleteLimit = it.getDeleteLimit();
        String _plus_7 = (_plus_6 + Integer.valueOf(_deleteLimit));
        _xifexpression = (_plus_7 + ")");
      } else {
        _xifexpression = condition;
      }
      final String target = _xifexpression;
      String _table_1 = PgvectorSql.table(table);
      String _plus_8 = ("DELETE FROM " + _table_1);
      String _plus_9 = (_plus_8 + " ");
      String _plus_10 = (_plus_9 + PgvectorSql.ALIAS);
      String _whereClause_1 = PgvectorSql.whereClause(target);
      final String delete = (_plus_10 + _whereClause_1);
      Code.Call _call = PgvectorSchema.call("value", PgvectorSql.counted(delete, "delete_count"), q, null, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The runtime reads a local JSON or JSON Lines file and writes it in batches with one INSERT statement, all in one
   * transaction. COPY FROM STDIN is not used: each driver streams it differently (psycopg copy, pg-copy-streams, CopyManager).
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; PostgreSQL has no ingestion pipeline (logical replication subscribes to another PostgreSQL server)");
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
        throw Unsupported.of((("import from " + scheme) + "; PostgreSQL reads object stores only through extensions the pgvector image does not include"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + PgvectorStatements.PENDING) + " (COPY FROM STDIN with FORMAT csv)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; PostgreSQL reads text, CSV and binary COPY data, not ");
        String _literal_1 = it.getFormat().getLiteral();
        String _plus_2 = (_plus_1 + _literal_1);
        throw Unsupported.of(_plus_2);
      }
      this.schema.note(it, "parallelism", "async");
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final String policy = _xifexpression_1;
      Code.Nil _nil = new Code.Nil();
      Map<String, Code.Expr> _slices = this.slices(it, it.getCollection(), table);
      String _xifexpression_2 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_2 = "insert_count";
      } else {
        _xifexpression_2 = "upsert_count";
      }
      final String sql = PgvectorSql.insertRows(q, table, _nil, _slices, policy, _xifexpression_2);
      final List<Code.Expr> extra = q.params().items().subList(1, q.params().items().size());
      final Code.Entries args = PgvectorSchema.kw("sql", Code.text(sql), "location", Code.text(it.getLocation()));
      boolean _isEmpty = extra.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("params", Code.items(extra));
      }
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      String _database = table.database();
      boolean _tripleNotEquals = (_database != null);
      if (_tripleNotEquals) {
        args.put("database", Code.text(table.database()));
      }
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The statements of a vecDSL transaction in one PostgreSQL transaction at the given isolation level.
   */
  protected List<Code.Stmt> _body(final Transaction it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      String database = null;
      boolean first = true;
      EList<DmlMethod> _methods = it.getMethods();
      for (final DmlMethod statement : _methods) {
        {
          if ((statement instanceof BulkImport)) {
            throw Unsupported.of((("bulk-import inside a transaction; " + PgvectorStatements.PENDING) + " (the import as one step of the transaction)"));
          }
          final List<Code.Stmt> inner = this.body(statement);
          if (((inner.size() != 1) || (!(IterableExtensions.<Code.Stmt>head(inner) instanceof Code.Return)))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          Code.Stmt _head = IterableExtensions.<Code.Stmt>head(inner);
          Code.Expr _value = ((Code.Return) _head).value();
          final Code.Call invocation = ((Code.Call) _value);
          Code.Expr _get = invocation.args().get("database");
          String _value_1 = null;
          if (((Code.Text) _get)!=null) {
            _value_1=((Code.Text) _get).value();
          }
          final String stepDatabase = _value_1;
          if (((!first) && (!Objects.equals(stepDatabase, database)))) {
            throw Unsupported.of("a transaction across databases; a PostgreSQL transaction runs in one database");
          }
          database = stepDatabase;
          first = false;
          final Code.Entries step = PgvectorSchema.kw("sql", invocation.args().get("sql"));
          boolean _has = invocation.args().has("params");
          if (_has) {
            step.put("params", invocation.args().get("params"));
          }
          steps.add(step);
        }
      }
      final Code.Entries args = PgvectorSchema.kw("steps", Code.items(steps));
      boolean _written = ModelUtil.written(it, "isolationLevel");
      if (_written) {
        String _xifexpression = null;
        String _literal = it.getIsolationLevel().getLiteral();
        boolean _equals = Objects.equals(_literal, "snapshot");
        if (_equals) {
          _xifexpression = "repeatable_read";
        } else {
          _xifexpression = it.getIsolationLevel().getLiteral();
        }
        args.put("isolation", Code.text(_xifexpression));
        String _literal_1 = it.getIsolationLevel().getLiteral();
        boolean _equals_1 = Objects.equals(_literal_1, "read_uncommitted");
        if (_equals_1) {
          this.notes.add("read_uncommitted on Transaction, which PostgreSQL runs as read committed");
        }
      }
      if ((database != null)) {
        args.put("database", Code.text(database));
      }
      Code.Call _runtime = Code.runtime("transaction", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A committed write is durable and visible to the next statement, so 'wait' needs no call; extensions are rejected.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, PgvectorGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * A statement's filter with its tenant, namespace and partition conditions.
   */
  private PgvectorFilter filterOf(final EObject statement, final VectorCollection collection, final PgvectorSql.Table table, final FilterBlock block, final boolean write) {
    PgvectorFilter _xblockexpression = null;
    {
      final PgvectorFilter result = new PgvectorFilter().where(block);
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Text _text = Code.text(tenant.getName());
        result.slice(table.tenant(), Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text)));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Text _text_1 = Code.text(((String) namespace));
        result.slice(table.namespace(), Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text_1)));
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
        String _partition = table.partition();
        boolean _tripleEquals = (_partition == null);
        if (_tripleEquals) {
          throw Unsupported.of("partitions of a collection declared without partitions");
        }
        final Function1<Partition, Code.Expr> _function = (Partition p) -> {
          Code.Text _text_2 = Code.text(p.getName());
          return ((Code.Expr) _text_2);
        };
        result.slice(table.partition(), IterableExtensions.<Code.Expr>toList(ListExtensions.<Partition, Code.Expr>map(named, _function)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * pg_prewarm (contrib, in the image) reads the table, its partitions and their indexes into shared buffers.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      List<String> _xifexpression = null;
      boolean _isEmpty = it.getPartitions().isEmpty();
      if (_isEmpty) {
        String _id = PgvectorSql.id(table.name());
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_id));
      } else {
        final Function1<Partition, String> _function = (Partition p) -> {
          return PgvectorSql.partitionTable(table, p.getName());
        };
        _xifexpression = IterableExtensions.<String>toList(ListExtensions.<Partition, String>map(it.getPartitions(), _function));
      }
      final List<String> relations = _xifexpression;
      if (((!it.getPartitions().isEmpty()) && (table.partition() == null))) {
        throw Unsupported.of("partitions of a collection declared without partitions");
      }
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.texts(relations), "jsonb");
      String _plus = ("SELECT relid FROM jsonb_array_elements_text(" + _bind);
      final String roots = (_plus + ") AS r(name), pg_partition_tree(to_regclass(r.name))");
      final String sql = (((("SELECT to_json(coalesce(sum(pg_prewarm(c.oid)), 0)) FROM pg_class c WHERE c.relkind IN (\'r\', \'i\') AND (c.oid IN (" + roots) + ") OR c.oid IN (SELECT indexrelid FROM pg_index WHERE indrelid IN (") + roots) + ")))");
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("CREATE EXTENSION IF NOT EXISTS pg_prewarm")), table.database());
      Code.Call _call = PgvectorSchema.call("value", sql, q, null, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute, _return));
    }
    return _xblockexpression;
  }

  /**
   * pg_buffercache_evict_relation (PostgreSQL 18 contrib, superuser) evicts the table's and its indexes' buffers.
   */
  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      if (((!it.getPartitions().isEmpty()) && (table.partition() == null))) {
        throw Unsupported.of("partitions of a collection declared without partitions");
      }
      List<String> _xifexpression = null;
      boolean _isEmpty = it.getPartitions().isEmpty();
      if (_isEmpty) {
        String _id = PgvectorSql.id(table.name());
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_id));
      } else {
        final Function1<Partition, String> _function = (Partition p) -> {
          return PgvectorSql.partitionTable(table, p.getName());
        };
        _xifexpression = IterableExtensions.<String>toList(ListExtensions.<Partition, String>map(it.getPartitions(), _function));
      }
      final List<String> relations = _xifexpression;
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _bind = q.bind(Code.texts(relations), "jsonb");
      String _plus = ("SELECT relid FROM jsonb_array_elements_text(" + _bind);
      final String roots = (_plus + ") AS r(name), pg_partition_tree(to_regclass(r.name))");
      final String sql = (((("SELECT to_json(count(*)) FROM pg_class c, LATERAL pg_buffercache_evict_relation(c.oid) WHERE c.relkind IN (\'r\', \'i\') AND (c.oid IN (" + roots) + ") OR c.oid IN (SELECT indexrelid FROM pg_index WHERE indrelid IN (") + roots) + ")))");
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("CREATE EXTENSION IF NOT EXISTS pg_buffercache")), table.database());
      Code.Call _call = PgvectorSchema.call("value", sql, q, null, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute, _return));
    }
    return _xblockexpression;
  }

  /**
   * A commit is durable once its WAL record is flushed (synchronous_commit on), so a flush only checks the table.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  /**
   * VACUUM (ANALYZE) reclaims dead rows and refreshes statistics; it cannot run inside a transaction block. VACUUM FULL is not used: it locks the table exclusively.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "targetSegments", "async");
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      String _table = PgvectorSql.table(table);
      String _plus = ("VACUUM (ANALYZE) " + _table);
      Code.Stmt _autocommit = PgvectorSchema.autocommit(_plus, table.database(), null);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_autocommit));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      Code.Call _call = PgvectorSchema.call("value", PgvectorQueries.describe(q, table, it.isIncludeStats(), it.isIncludeIndexProgress()), q, null, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + PgvectorStatements.NO_MODELS));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(PgvectorStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(PgvectorStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(PgvectorStatements.NO_SNAPSHOTS);
  }

  /**
   * A new table LIKE the source (columns, defaults, constraints, indexes, comments) filled from it, in one transaction.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; a vecDSL database block is the server connection (CREATE DATABASE ... TEMPLATE copies one database)");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; PostgreSQL has no copy-on-write tables");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = PgvectorStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final PgvectorSql.Table table = this.schema.table(it.getSourceCollection());
      final String target = PgvectorSql.id(it.getTargetName());
      String _partition = table.partition();
      boolean _tripleNotEquals_2 = (_partition != null);
      if (_tripleNotEquals_2) {
        String _name = table.name();
        String _plus_1 = ("the partitions of " + _name);
        String _plus_2 = (_plus_1 + ", which the clone holds in one table");
        this.notes.add(_plus_2);
      }
      String _table = PgvectorSql.table(table);
      String _plus_3 = ((("CREATE TABLE " + target) + " (LIKE ") + _table);
      String _plus_4 = (_plus_3 + " INCLUDING ALL)");
      Code.Entries _kw = PgvectorSchema.kw("sql", Code.text(_plus_4));
      String _table_1 = PgvectorSql.table(table);
      String _plus_5 = ((("INSERT INTO " + target) + " SELECT * FROM ") + _table_1);
      Code.Entries _kw_1 = PgvectorSchema.kw("sql", Code.text(PgvectorSql.counted(_plus_5, "copied")));
      final List<Code.Expr> steps = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _kw), ((Code.Expr) _kw_1)));
      final Code.Entries args = PgvectorSchema.kw("steps", Code.items(steps));
      String _database = table.database();
      boolean _tripleNotEquals_3 = (_database != null);
      if (_tripleNotEquals_3) {
        args.put("database", Code.text(table.database()));
      }
      Code.Call _runtime = Code.runtime("transaction", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * REINDEX rebuilds the index; concurrently it runs outside a transaction block.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final String database = this.indexDatabase(it.getIndex());
      String _xifexpression = null;
      boolean _isConcurrently = it.isConcurrently();
      if (_isConcurrently) {
        _xifexpression = "CONCURRENTLY ";
      } else {
        _xifexpression = "";
      }
      String _plus = ("REINDEX INDEX " + _xifexpression);
      String _id = PgvectorSql.id(this.indexName(it.getIndex()));
      final String sql = (_plus + _id);
      Code.Stmt _xifexpression_1 = null;
      boolean _isConcurrently_1 = it.isConcurrently();
      if (_isConcurrently_1) {
        _xifexpression_1 = PgvectorSchema.autocommit(sql, database, null);
      } else {
        _xifexpression_1 = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)), database);
      }
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(PgvectorStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        return this.dropSlice(it.getCollection(), table.namespace(), it.getNamespace());
      }
      String _table = PgvectorSql.table(table);
      String _plus = ("TRUNCATE TABLE " + _table);
      Code.Stmt _execute = PgvectorSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)), table.database());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final Concept owner = it.getScope();
      String database = null;
      String _switchResult = null;
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            String _xblockexpression_1 = null;
            {
              if ((owner instanceof Database)) {
                database = ((Database) owner).getName();
              } else {
                if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                  String _name = owner.eClass().getName();
                  String _plus = ("listing collections in a " + _name);
                  String _plus_1 = (_plus + "; PostgreSQL lists the tables of a database");
                  throw Unsupported.of(_plus_1);
                }
              }
              _xblockexpression_1 = PgvectorQueries.collections();
            }
            _switchResult = _xblockexpression_1;
            break;
          case DATABASES:
            String _xblockexpression_2 = null;
            {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing databases in a " + _name);
                throw Unsupported.of(_plus);
              }
              _xblockexpression_2 = PgvectorQueries.databases();
            }
            _switchResult = _xblockexpression_2;
            break;
          case INDEXES:
            String _xblockexpression_3 = null;
            {
              final PgvectorSql.Table table = this.scopeTable(owner, "indexes");
              database = table.database();
              _xblockexpression_3 = PgvectorQueries.indexes(q, table, ((VectorCollection) owner).getName());
            }
            _switchResult = _xblockexpression_3;
            break;
          case PARTITIONS:
            String _xblockexpression_4 = null;
            {
              final PgvectorSql.Table table = this.scopeTable(owner, "partitions");
              database = table.database();
              _xblockexpression_4 = PgvectorQueries.partitions(q, table);
            }
            _switchResult = _xblockexpression_4;
            break;
          case ALIASES:
            String _xblockexpression_5 = null;
            {
              PgvectorSql.Table _xifexpression = null;
              if ((owner == null)) {
                _xifexpression = null;
              } else {
                _xifexpression = this.scopeTable(owner, "aliases");
              }
              final PgvectorSql.Table table = _xifexpression;
              String _database = null;
              if (table!=null) {
                _database=table.database();
              }
              database = _database;
              _xblockexpression_5 = PgvectorQueries.aliases(q, table);
            }
            _switchResult = _xblockexpression_5;
            break;
          case TENANTS:
          case NAMESPACES:
            String _xblockexpression_6 = null;
            {
              final PgvectorSql.Table table = this.scopeTable(owner, it.getKind().getLiteral());
              database = table.database();
              String _xifexpression = null;
              String _literal = it.getKind().getLiteral();
              boolean _equals = Objects.equals(_literal, "tenants");
              if (_equals) {
                _xifexpression = table.tenant();
              } else {
                _xifexpression = table.namespace();
              }
              final String column = _xifexpression;
              if ((column == null)) {
                String _literal_1 = it.getKind().getLiteral();
                String _plus = ("listing " + _literal_1);
                String _plus_1 = (_plus + " of a collection without ");
                String _literal_2 = it.getKind().getLiteral();
                String _plus_2 = (_plus_1 + _literal_2);
                throw Unsupported.of(_plus_2);
              }
              _xblockexpression_6 = PgvectorQueries.slices(table, column);
            }
            _switchResult = _xblockexpression_6;
            break;
          case SNAPSHOTS:
            throw Unsupported.of(PgvectorStatements.NO_SNAPSHOTS);
          default:
            break;
        }
      }
      final String source = _switchResult;
      Code.Text _xifexpression = null;
      String _prefix = it.getPrefix();
      boolean _tripleEquals = (_prefix == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = Code.text(it.getPrefix());
      }
      Integer _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "listLimit");
      if (_written) {
        _xifexpression_1 = Integer.valueOf(it.getListLimit());
      } else {
        _xifexpression_1 = null;
      }
      final String sql = PgvectorQueries.names(q, source, _xifexpression, _xifexpression_1);
      Code.Call _call = PgvectorSchema.call("value", sql, q, null, database);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private PgvectorSql.Table scopeTable(final Concept owner, final String what) {
    PgvectorSql.Table _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.table(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * Indexes are built synchronously (CREATE INDEX returns when the index is valid), so awaiting one reads its state once.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state", "timeoutSeconds");
      final PgvectorSql.Query q = new PgvectorSql.Query();
      Code.Call _call = PgvectorSchema.call("value", PgvectorQueries.indexState(q, this.indexName(it.getIndex())), q, null, this.indexDatabase(it.getIndex()));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "elementScope", "asOf", "explain", "reranker", "queryModel");
      this.queryExtensions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression = it.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final String query = this.queryVector(q, it.getInput(), it.getCollection(), table, column);
      final List<String> stages = CollectionLiterals.<String>newArrayList();
      final List<String> staged = this.prefetchStages(it, q, it.getCollection(), table, it.getPrefetches(), it.getInput(), field, stages);
      String _where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      String _within = PgvectorQueries.within(staged);
      final String where = PgvectorSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_where, _within)));
      final PgvectorQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field, Collections.<PgvectorQueries.Terms>unmodifiableList(CollectionLiterals.<PgvectorQueries.Terms>newArrayList()));
      VectorCollection _collection = it.getCollection();
      SearchParams _params = it.getParams();
      Map<String, String> _extensionSettings = this.extensionSettings(it);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus = (_topK + _offsetOf);
      int _fetchOf = this.fetchOf(shape, _plus);
      final Map<String, String> options = this.settings(_collection, field, _params, _extensionSettings, _fetchOf, ((where != null) || (it.getCursor() != null)), it.isExact());
      String _xifexpression_1 = null;
      boolean _isEmpty = stages.isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = IterableExtensions.join(stages, ", ");
      }
      final String prelude = _xifexpression_1;
      PgvectorSql.Column _quantized = this.quantized(column, it.getParams());
      int _topK_1 = it.getTopK();
      int _offsetOf_1 = this.offsetOf(it);
      PgvectorQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final PgvectorQueries.Search search = new PgvectorQueries.Search(table, _quantized, metric, query, prelude, _topK_1, _offsetOf_1, where, _projection, 
        (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = PgvectorSchema.call(_xifexpression_2, PgvectorQueries.vectorSearch(q, search, shape), q, options, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker", "minShouldMatch", "fuzziness", "slop");
      this.queryExtensions(it);
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy");
      }
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final List<PgvectorQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, it.getSearchFields());
      final Code.Expr words = this.keywordText(it.getInput());
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
      final String op = this.keywordOperator(_xifexpression, _xifexpression_1);
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      PgvectorQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final PgvectorQueries.Keywords keywords = new PgvectorQueries.Keywords(table, fields, op, words, _topK, _offsetOf, where, _projection, _minOf, _maxOf);
      PgvectorQueries.Terms _terms = new PgvectorQueries.Terms(op, words);
      final PgvectorQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null, Collections.<PgvectorQueries.Terms>unmodifiableList(CollectionLiterals.<PgvectorQueries.Terms>newArrayList(_terms)));
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = PgvectorSchema.call(_xifexpression_2, PgvectorQueries.keywordSearch(q, keywords, shape), q, this.merged(this.timeout(it.getParams()), this.extensionSettings(it)), table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Hybrid search fused in SQL: each leg ranks its candidates in a CTE, and the fusion function of vecDSL combines them.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker");
      this.queryExtensions(it);
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final List<PgvectorQueries.Terms> terms = CollectionLiterals.<PgvectorQueries.Terms>newArrayList();
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "fusion");
      if (_written) {
        _xifexpression = it.getFusion().getLiteral();
      } else {
        _xifexpression = "rrf";
      }
      final String fusionKind = _xifexpression;
      final List<Double> weights = this.legWeights(it, fusionKind);
      final QueryInput shared = it.getInput();
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      final int defaultLimit = (_topK + _offsetOf);
      final Map<String, String> options = CollectionLiterals.<String, String>newLinkedHashMap();
      options.putAll(this.timeout(it.getParams()));
      final List<PgvectorQueries.Leg> parts = CollectionLiterals.<PgvectorQueries.Leg>newArrayList();
      int index = 0;
      List<EObject> _legList = this.legList(it);
      for (final EObject leg : _legList) {
        {
          final Double weight = weights.get(index);
          Integer _xifexpression_1 = null;
          boolean _written_1 = ModelUtil.written(leg, "legLimit");
          if (_written_1) {
            Object _feature = this.feature(leg, "legLimit");
            _xifexpression_1 = ((Integer) _feature);
          } else {
            _xifexpression_1 = Integer.valueOf(defaultLimit);
          }
          final Integer limit = _xifexpression_1;
          VectorCollection _collection = it.getCollection();
          FilterBlock _xifexpression_2 = null;
          Object _feature_1 = this.feature(leg, "filter");
          boolean _tripleEquals = (_feature_1 == null);
          if (_tripleEquals) {
            _xifexpression_2 = it.getFilter();
          } else {
            Object _feature_2 = this.feature(leg, "filter");
            _xifexpression_2 = this.combined(it.getFilter(), ((FilterBlock) _feature_2));
          }
          final String legWhere = PgvectorSql.where(q, table, this.filterOf(it, _collection, table, _xifexpression_2, false).parts());
          Set<String> _keySet = ModelUtil.extParams(leg, PgvectorGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of((("extension parameter " + name) + " on a hybrid leg"));
          }
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
            boolean _tripleNotEquals = (_bounds != null);
            if (_tripleNotEquals) {
              throw Unsupported.of((("bounds on a hybrid leg; " + PgvectorStatements.PENDING) + " (a score condition in the leg CTE)"));
            }
            final VectorField field = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
            final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
            String _xifexpression_3 = null;
            boolean _written_2 = ModelUtil.written(leg, "metric");
            if (_written_2) {
              _xifexpression_3 = ((VectorLeg)leg).getMetric().getLiteral();
            } else {
              _xifexpression_3 = column.vec().metric();
            }
            final String metric = _xifexpression_3;
            QueryInput _elvis = null;
            QueryInput _input = ((VectorLeg)leg).getInput();
            if (_input != null) {
              _elvis = _input;
            } else {
              _elvis = shared;
            }
            final String query = this.queryVector(q, _elvis, it.getCollection(), table, column);
            options.putAll(this.settings(it.getCollection(), field, ((VectorLeg)leg).getParams(), CollectionLiterals.<String, String>newLinkedHashMap(), (limit).intValue(), (legWhere != null), ((VectorLeg)leg).isExact()));
            PgvectorQueries.Leg _leg = new PgvectorQueries.Leg(column, metric, query, null, null, null, (limit).intValue(), legWhere, (weight).doubleValue());
            parts.add(_leg);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              boolean _written_2 = ModelUtil.written(leg, "minShouldMatch");
              if (_written_2) {
                String _get = PgvectorStatements.REASONS.get("minShouldMatch");
                String _plus = ("the \'min-should-match\' clause; " + _get);
                throw Unsupported.of(_plus);
              }
              final List<PgvectorQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, ((KeywordLeg)leg).getSearchFields());
              String _xifexpression_3 = null;
              boolean _written_3 = ModelUtil.written(leg, "textQueryType");
              if (_written_3) {
                _xifexpression_3 = ((KeywordLeg)leg).getTextQueryType().getLiteral();
              } else {
                _xifexpression_3 = "match";
              }
              String _xifexpression_4 = null;
              boolean _written_4 = ModelUtil.written(leg, "matchOperator");
              if (_written_4) {
                _xifexpression_4 = ((KeywordLeg)leg).getMatchOperator().getLiteral();
              } else {
                _xifexpression_4 = "and";
              }
              final String op = this.keywordOperator(_xifexpression_3, _xifexpression_4);
              QueryInput _elvis = null;
              QueryInput _input = ((KeywordLeg)leg).getInput();
              if (_input != null) {
                _elvis = _input;
              } else {
                _elvis = shared;
              }
              final Code.Expr legText = this.keywordText(_elvis);
              PgvectorQueries.Terms _terms = new PgvectorQueries.Terms(op, legText);
              terms.add(_terms);
              PgvectorQueries.Leg _leg = new PgvectorQueries.Leg(null, null, null, fields, op, legText, (limit).intValue(), legWhere, (weight).doubleValue());
              parts.add(_leg);
            }
          }
          index = (index + 1);
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
      int _topK_1 = it.getTopK();
      int _offsetOf_1 = this.offsetOf(it);
      PgvectorQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final PgvectorQueries.Hybrid hybrid = new PgvectorQueries.Hybrid(table, parts, fusionKind, k, null, _topK_1, _offsetOf_1, _projection, _minOf, _maxOf);
      options.putAll(this.extensionSettings(it));
      final PgvectorQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null, terms);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = PgvectorSchema.call(_xifexpression_2, PgvectorQueries.hybridSearch(q, hybrid, shape), q, options, table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private List<EObject> legList(final HybridSearch search) {
    List<EObject> _xblockexpression = null;
    {
      final List<EObject> result = CollectionLiterals.<EObject>newArrayList();
      EList<HybridLeg> _legs = search.getLegs();
      Iterables.<EObject>addAll(result, _legs);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A leg filter joins the statement's filter: both must hold.
   */
  private FilterBlock combined(final FilterBlock outer, final FilterBlock inner) {
    if ((outer == null)) {
      return inner;
    }
    throw Unsupported.of((("a filter on both the hybrid search and a leg; " + PgvectorStatements.PENDING) + " (the conjunction of both)"));
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
   * The average-vector strategy over example records or vectors, computed in SQL; the examples are not returned.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker");
      this.queryExtensions(it);
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final PgvectorSql.Table lookup = this.schema.table(_elvis);
      String _database = lookup.database();
      String _database_1 = table.database();
      boolean _notEquals = (!Objects.equals(_database, _database_1));
      if (_notEquals) {
        throw Unsupported.of("examples looked up in another database");
      }
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      final PgvectorSql.Column lookupColumn = lookup.column(column.name());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final List<Code.Expr> positiveIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> positiveVectors = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeIds = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Expr> negativeVectors = CollectionLiterals.<Code.Expr>newArrayList();
      EList<Literal> _positives = it.getPositives();
      for (final Literal example : _positives) {
        boolean _isVector = this.isVector(example);
        if (_isVector) {
          Code.Expr _convert = this.convert(example, column);
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
          Code.Expr _convert_1 = this.convert(example_1, column);
          negativeVectors.add(_convert_1);
        } else {
          Code.Expr _of_1 = Literals.of(example_1);
          negativeIds.add(_of_1);
        }
      }
      final String prelude = PgvectorQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors);
      final List<PgvectorSql.Cond> conditions = CollectionLiterals.<PgvectorSql.Cond>newArrayList();
      List<PgvectorSql.Cond> _parts = this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts();
      Iterables.<PgvectorSql.Cond>addAll(conditions, _parts);
      final List<String> parts = CollectionLiterals.<String>newArrayList();
      String _where = PgvectorSql.where(q, table, conditions);
      parts.add(_where);
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final List<Code.Expr> examples = CollectionLiterals.<Code.Expr>newArrayList();
        Iterables.<Code.Expr>addAll(examples, positiveIds);
        Iterables.<Code.Expr>addAll(examples, negativeIds);
        boolean _isEmpty_1 = examples.isEmpty();
        boolean _not = (!_isEmpty_1);
        if (_not) {
          String _keyIn = PgvectorSql.keyIn(q, table, Code.items(examples));
          String _plus = ("(" + _keyIn);
          String _plus_1 = (_plus + ") IS NOT TRUE");
          parts.add(_plus_1);
        }
      }
      final String where = PgvectorSql.and(parts);
      final PgvectorQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field, Collections.<PgvectorQueries.Terms>unmodifiableList(CollectionLiterals.<PgvectorQueries.Terms>newArrayList()));
      VectorCollection _collection_1 = it.getCollection();
      SearchParams _params = it.getParams();
      Map<String, String> _extensionSettings = this.extensionSettings(it);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus_2 = (_topK + _offsetOf);
      int _fetchOf = this.fetchOf(shape, _plus_2);
      final Map<String, String> options = this.settings(_collection_1, field, _params, _extensionSettings, _fetchOf, ((where != null) || (it.getCursor() != null)), false);
      String _metric = column.vec().metric();
      int _topK_1 = it.getTopK();
      int _offsetOf_1 = this.offsetOf(it);
      PgvectorQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final PgvectorQueries.Search search = new PgvectorQueries.Search(table, column, _metric, "(SELECT v FROM q)", prelude, _topK_1, _offsetOf_1, where, _projection, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf);
      String _xifexpression = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression = "value";
      } else {
        _xifexpression = "query";
      }
      Code.Call _call = PgvectorSchema.call(_xifexpression, PgvectorQueries.vectorSearch(q, search, shape), q, options, table.database());
      Code.Return _return = new Code.Return(_call);
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
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final PgvectorQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, null, false).parts());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      Code.Call _call = PgvectorSchema.call("query", PgvectorQueries.getRows(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), names, where), q, this.extensionSettings(it), table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      Code.Call _call = PgvectorSchema.call("value", PgvectorQueries.count(table, where), q, this.extensionSettings(it), table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final PgvectorQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      final List<PgvectorQueries.Order> order = CollectionLiterals.<PgvectorQueries.Order>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        {
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
          }
          PgvectorQueries.Ref _fieldRefOf = this.fieldRefOf(it.getCollection(), table, clause.getField(), "sort");
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          PgvectorQueries.Order _order = new PgvectorQueries.Order(_fieldRefOf, _equals);
          order.add(_order);
        }
      }
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = Integer.valueOf(it.getScrollLimit());
      } else {
        _xifexpression = null;
      }
      final Integer limit = _xifexpression;
      if (((it.getCursor() == null) && (!ModelUtil.written(it, "sliceTotal")))) {
        final Function1<PgvectorQueries.Order, String> _function = (PgvectorQueries.Order o) -> {
          String _sql = o.field().sql(PgvectorSql.ALIAS);
          String _xifexpression_1 = null;
          boolean _descending = o.descending();
          if (_descending) {
            _xifexpression_1 = " DESC NULLS LAST";
          } else {
            _xifexpression_1 = " ASC NULLS LAST";
          }
          return (_sql + _xifexpression_1);
        };
        final List<String> terms = IterableExtensions.<String>toList(ListExtensions.<PgvectorQueries.Order, String>map(order, _function));
        Code.Call _call = PgvectorSchema.call("query", PgvectorQueries.scroll(table, names, where, terms, limit, this.offsetOf(it)), q, this.extensionSettings(it), table.database());
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
      }
      if ((ModelUtil.written(it, "sliceTotal") && (((it.getSliceTotal() < 1) || (it.getSliceIndex() < 0)) || (it.getSliceIndex() >= it.getSliceTotal())))) {
        throw Unsupported.of("a slice index outside 0 to the slice count minus 1");
      }
      Code.Text _xifexpression_1 = null;
      String _cursor = it.getCursor();
      boolean _tripleEquals = (_cursor == null);
      if (_tripleEquals) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = Code.text(it.getCursor());
      }
      final Code.Text cursorKey = _xifexpression_1;
      Integer _xifexpression_2 = null;
      boolean _written_1 = ModelUtil.written(it, "sliceTotal");
      if (_written_1) {
        _xifexpression_2 = Integer.valueOf(it.getSliceTotal());
      } else {
        _xifexpression_2 = null;
      }
      final Integer slices = _xifexpression_2;
      Code.Call _call_1 = PgvectorSchema.call("query", PgvectorQueries.scroll(q, table, names, where, order, limit, this.offsetOf(it), cursorKey, Integer.valueOf(it.getSliceIndex()), slices), q, 
        this.extensionSettings(it), table.database());
      Code.Return _return_1 = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final PgvectorQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      Code.Call _call = PgvectorSchema.call("query", PgvectorQueries.sample(table, shape.output(), where, it.getSampleSize(), first), q, this.extensionSettings(it), table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final PgvectorSql.Table table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      final PgvectorSql.Query q = new PgvectorSql.Query();
      final String where = PgvectorSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        _xifexpression = it.getSampleSize();
      } else {
        _xifexpression = 10;
      }
      final int sample = _xifexpression;
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        _xifexpression_1 = it.getMatrixLimit();
      } else {
        _xifexpression_1 = 3;
      }
      final int limit = _xifexpression_1;
      Code.Call _call = PgvectorSchema.call("query", PgvectorQueries.distanceMatrix(table, column, column.vec().metric(), where, sample, limit), q, this.extensionSettings(it), table.database());
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + PgvectorStatements.NO_GENERATION));
  }

  protected List<Code.Stmt> _body(final BatchQuery it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> calls = CollectionLiterals.<Code.Expr>newArrayList();
      final String owner = this.current;
      int index = 0;
      EList<QueryMethod> _queries = it.getQueries();
      for (final QueryMethod query : _queries) {
        {
          final String name = ((owner + "_query_") + Integer.valueOf(index));
          this.current = name;
          List<String> _parameterNames = ModelUtil.parameterNames(query);
          List<Code.Stmt> _body = this.body(query);
          ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
          ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList();
          Program.Method _method = new Program.Method(name, _parameterNames, _body, _newArrayList, _newArrayList_1, true);
          this.helpers.add(_method);
          Code.Call _self = Code.self(name);
          calls.add(_self);
          index = (index + 1);
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
    String _plus = (PgvectorStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private Object feature(final EObject object, final String name) {
    Object _xblockexpression = null;
    {
      final EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
      Object _xifexpression = null;
      if ((feature == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = object.eGet(feature);
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

  private Code.Expr minOf(final ScoreBounds bounds) {
    Code.Num _xifexpression = null;
    if (((bounds != null) && ModelUtil.written(bounds, "min"))) {
      _xifexpression = Code.num(bounds.getMin());
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  private Code.Expr maxOf(final ScoreBounds bounds) {
    Code.Num _xifexpression = null;
    if (((bounds != null) && ModelUtil.written(bounds, "max"))) {
      _xifexpression = Code.num(bounds.getMax());
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
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
   * The column a search orders by, with the oversampling of the search parameters for a quantized index.
   */
  private PgvectorSql.Column quantized(final PgvectorSql.Column column, final SearchParams params) {
    PgvectorSql.Column _xblockexpression = null;
    {
      if ((((params == null) || (!ModelUtil.written(params, "oversampling"))) || (column.vec().quantization() == null))) {
        return column;
      }
      String _name = column.name();
      String _type = column.type();
      String _type_1 = column.vec().type();
      int _dim = column.vec().dim();
      String _metric = column.vec().metric();
      String _quantization = column.vec().quantization();
      double _oversampling = params.getOversampling();
      PgvectorSql.Vec _vec = new PgvectorSql.Vec(_type_1, _dim, _metric, _quantization, _oversampling);
      boolean _nullable = column.nullable();
      _xblockexpression = new PgvectorSql.Column(_name, _type, null, false, _vec, null, _nullable, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
    }
    return _xblockexpression;
  }

  /**
   * Output fields (the key is always returned) and the vector fields returned with 'with-vectors' or named in the output.
   */
  private PgvectorQueries.Projection projection(final VectorCollection collection, final PgvectorSql.Table table, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    PgvectorQueries.Projection _xblockexpression = null;
    {
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
        if (((field != collection.getPrimaryKey()) && (!excluded.contains(field)))) {
          if ((field instanceof VectorField)) {
            if (((output != null) && (!output.isAllFields()))) {
              String _name = table.column(ModelUtil.fieldName(field)).name();
              vectors.add(_name);
            }
          } else {
            String _name_1 = table.column(ModelUtil.fieldName(field)).name();
            fields.add(_name_1);
          }
        }
      }
      if (withVectors) {
        List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
        for (final VectorField field_1 : _vectorFields) {
          {
            final String name = table.column(ModelUtil.fieldName(field_1)).name();
            if (((!vectors.contains(name)) && (!excluded.contains(field_1)))) {
              vectors.add(name);
            }
          }
        }
      }
      _xblockexpression = new PgvectorQueries.Projection(fields, vectors);
    }
    return _xblockexpression;
  }

  /**
   * Search settings applied with SET LOCAL: candidate lists of at least limit + offset, iterative scans when filtered.
   */
  private Map<String, String> settings(final VectorCollection collection, final VectorField field, final SearchParams params, final Map<String, String> fromExtension, final int fetch, final boolean filtered, final boolean exact) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      final VectorIndex index = this.schema.indexOf(collection, field);
      String _xifexpression = null;
      if (((index == null) || Objects.equals(index.getFamily().getLiteral(), "flat"))) {
        _xifexpression = null;
      } else {
        String _xifexpression_1 = null;
        String _literal = index.getFamily().getLiteral();
        boolean _equals = Objects.equals(_literal, "ivf");
        if (_equals) {
          _xifexpression_1 = "ivfflat";
        } else {
          _xifexpression_1 = "hnsw";
        }
        _xifexpression = _xifexpression_1;
      }
      final String method = _xifexpression;
      if ((params != null)) {
        boolean _written = ModelUtil.written(params, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: hnsw.ef_search, ivfflat.probes and iterative scans are effort settings, with no declarable recall target");
        }
        Set<String> _keySet = ModelUtil.extParams(params, PgvectorGenerator.VENDOR).keySet();
        for (final String name : _keySet) {
          throw Unsupported.of(("extension parameter " + name));
        }
      }
      final boolean exhaustive = (exact || ((params != null) && (params.isExhaustive() || (ModelUtil.written(params, "filterStrategy") && Objects.equals(params.getFilterStrategy().getLiteral(), "pre_filter")))));
      if (exhaustive) {
        result.put("enable_indexscan", "off");
      } else {
        boolean _equals_1 = Objects.equals(method, "hnsw");
        if (_equals_1) {
          int _xifexpression_2 = (int) 0;
          if (((params != null) && ModelUtil.written(params, "candidatePool"))) {
            _xifexpression_2 = params.getCandidatePool();
          } else {
            int _xifexpression_3 = (int) 0;
            boolean _written_1 = ModelUtil.written(index, "searchCandidates");
            if (_written_1) {
              _xifexpression_3 = index.getSearchCandidates();
            } else {
              _xifexpression_3 = 40;
            }
            _xifexpression_2 = _xifexpression_3;
          }
          final int candidates = _xifexpression_2;
          result.put("hnsw.ef_search", String.valueOf(Math.min(1000, Math.max(candidates, fetch))));
          String _xifexpression_4 = null;
          if (((params != null) && ModelUtil.written(params, "filterStrategy"))) {
            _xifexpression_4 = params.getFilterStrategy().getLiteral();
          } else {
            _xifexpression_4 = "auto";
          }
          final String strategy = _xifexpression_4;
          if ((Objects.equals(strategy, "iterative") || (filtered && Objects.equals(strategy, "auto")))) {
            result.put("hnsw.iterative_scan", "strict_order");
          }
          if (((params != null) && ModelUtil.written(params, "scanBudget"))) {
            result.put("hnsw.max_scan_tuples", String.valueOf(params.getScanBudget()));
          }
          if (((params != null) && ModelUtil.written(params, "probes"))) {
            throw Unsupported.of("probes on an hnsw index; probes belong to ivf indexes");
          }
        } else {
          boolean _equals_2 = Objects.equals(method, "ivfflat");
          if (_equals_2) {
            if (((params != null) && ModelUtil.written(params, "probes"))) {
              result.put("ivfflat.probes", String.valueOf(params.getProbes()));
            }
            String _xifexpression_5 = null;
            if (((params != null) && ModelUtil.written(params, "filterStrategy"))) {
              _xifexpression_5 = params.getFilterStrategy().getLiteral();
            } else {
              _xifexpression_5 = "auto";
            }
            final String strategy_1 = _xifexpression_5;
            if ((Objects.equals(strategy_1, "iterative") || (filtered && Objects.equals(strategy_1, "auto")))) {
              result.put("ivfflat.iterative_scan", "relaxed_order");
            }
            if (((params != null) && ModelUtil.written(params, "scanBudget"))) {
              result.put("ivfflat.max_probes", String.valueOf(params.getScanBudget()));
            }
            if (((params != null) && ModelUtil.written(params, "candidatePool"))) {
              throw Unsupported.of("candidates on an ivf index; candidate lists belong to hnsw indexes");
            }
          } else {
            if ((params != null)) {
              this.schema.note(params, "candidatePool", "probes", "scanBudget", "filterStrategy");
            }
          }
        }
      }
      if ((params != null)) {
        result.putAll(this.timeout(params));
        boolean _and = false;
        if (!((field != null) && params.isRescore())) {
          _and = false;
        } else {
          VectorIndex _indexOf = this.schema.indexOf(collection, field);
          Quantization _quantization = null;
          if (_indexOf!=null) {
            _quantization=_indexOf.getQuantization();
          }
          boolean _tripleEquals = (_quantization == null);
          _and = _tripleEquals;
        }
        if (_and) {
          this.notes.add("rescore on SearchParams, without a quantized index");
        }
      }
      result.putAll(fromExtension);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Map<String, String> merged(final Map<String, String> first, final Map<String, String> second) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      result.putAll(first);
      result.putAll(second);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Map<String, String> timeout(final SearchParams params) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      if (((params != null) && ModelUtil.written(params, "timeoutMillis"))) {
        result.put("statement_timeout", String.valueOf(params.getTimeoutMillis()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * pgvector query settings written in an 'extension pgvector' block, by their setting names.
   */
  private Map<String, String> extensionSettings(final EObject host) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, PgvectorGenerator.VENDOR).entrySet();
      for (final Map.Entry<String, Literal> entry : _entrySet) {
        {
          boolean _contains = PgvectorStatements.SETTINGS.contains(entry.getKey());
          boolean _not = (!_contains);
          if (_not) {
            String _key = entry.getKey();
            String _plus = ("extension parameter " + _key);
            String _plus_1 = (_plus + "; pgvector query settings are ");
            String _join = IterableExtensions.join(PgvectorStatements.SETTINGS, ", ");
            String _plus_2 = (_plus_1 + _join);
            throw Unsupported.of(_plus_2);
          }
          final Literal value = entry.getValue();
          String _switchResult = null;
          boolean _matched = false;
          if (value instanceof StringLit) {
            _matched=true;
            _switchResult = ((StringLit)value).getValue();
          }
          if (!_matched) {
            if (value instanceof NumberLit) {
              _matched=true;
              _switchResult = PgvectorSql.number(((NumberLit)value).getValue());
            }
          }
          if (!_matched) {
            String _key_1 = entry.getKey();
            String _plus_3 = ("extension parameter " + _key_1);
            String _plus_4 = (_plus_3 + " with a value that is not a string or number literal");
            throw Unsupported.of(_plus_4);
          }
          final String setting = _switchResult;
          result.put(entry.getKey(), setting);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses PostgreSQL lacks, with their reason, and clauses not generated yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = PgvectorStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = PgvectorStatements.PENDING_CLAUSES.get(name);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + PgvectorStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = PgvectorStatements.REASONS.get(name);
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
   * pgvector extension blocks of queries carry settings only; extension items of other kinds are rejected.
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
      boolean _equals = Objects.equals(_vendor, PgvectorGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on pgvector");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
    this.extensionSettings(host);
  }

  /**
   * The query vector as SQL: a bound vector, or the stored vector of a record (a scalar subquery).
   */
  private String queryVector(final PgvectorSql.Query q, final QueryInput input, final VectorCollection collection, final PgvectorSql.Table table, final PgvectorSql.Column column) {
    String _xblockexpression = null;
    {
      if ((input == null)) {
        throw Unsupported.of("a vector query without input");
      }
      String _switchResult = null;
      EQueryInputMode _kind = input.getKind();
      if (_kind != null) {
        switch (_kind) {
          case VECTOR:
          case SPARSE_VECTOR:
            String _xblockexpression_1 = null;
            {
              EQueryInputMode _kind_1 = input.getKind();
              boolean _equals = Objects.equals(_kind_1, EQueryInputMode.SPARSE_VECTOR);
              String _type = column.vec().type();
              boolean _equals_1 = Objects.equals(_type, "sparsevec");
              boolean _notEquals = (_equals != _equals_1);
              if (_notEquals) {
                String _literal = input.getKind().getLiteral();
                String _plus = (_literal + " input on ");
                String _type_1 = column.vec().type();
                String _plus_1 = (_plus + _type_1);
                String _plus_2 = (_plus_1 + " field ");
                String _name = column.name();
                String _plus_3 = (_plus_2 + _name);
                throw Unsupported.of(_plus_3);
              }
              _xblockexpression_1 = q.bind(this.convert(input.getValue(), column), column.vec().sql());
            }
            _switchResult = _xblockexpression_1;
            break;
          case ID:
            String _xblockexpression_2 = null;
            {
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final PgvectorSql.Table source = this.schema.table(_elvis);
              String _database = source.database();
              String _database_1 = table.database();
              boolean _notEquals = (!Objects.equals(_database, _database_1));
              if (_notEquals) {
                throw Unsupported.of("a query vector looked up in another database");
              }
              final PgvectorSql.Column stored = source.column(column.name());
              String _id = PgvectorSql.id(stored.name());
              String _plus = ("(SELECT x." + _id);
              String _plus_1 = (_plus + " FROM ");
              String _table = PgvectorSql.table(source);
              String _plus_2 = (_plus_1 + _table);
              String _plus_3 = (_plus_2 + " x WHERE x.");
              String _id_1 = PgvectorSql.id(source.key());
              String _plus_4 = (_plus_3 + _id_1);
              String _plus_5 = (_plus_4 + " = ");
              String _bind = q.bind(Literals.of(input.getValue()), source.keyColumn().paramType());
              String _plus_6 = (_plus_5 + _bind);
              _xblockexpression_2 = (_plus_6 + " LIMIT 1)");
            }
            _switchResult = _xblockexpression_2;
            break;
          case TEXT:
            throw Unsupported.of(("text input" + PgvectorStatements.NO_MODELS));
          case MULTI_VECTOR:
            throw Unsupported.of("multi-vector input; pgvector compares one vector per column");
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " input");
            String _plus_1 = (_plus + PgvectorStatements.NO_MODELS);
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " input");
        String _plus_1 = (_plus + PgvectorStatements.NO_MODELS);
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The result shape of a search: the keyset cursor, boosts, MMR, groups, total, aggregations, highlights of the text inputs and
   * sort keys. The searched vector field is null for keyword and hybrid searches; terms are empty for searches without text.
   */
  private PgvectorQueries.Shape shapeOf(final EObject search, final VectorCollection collection, final PgvectorSql.Table table, final VectorField searched, final List<PgvectorQueries.Terms> terms) {
    PgvectorQueries.Shape _xblockexpression = null;
    {
      Object _feature = this.feature(search, "cursor");
      final String cursorText = ((String) _feature);
      Object _feature_1 = this.feature(search, "groupBy");
      final FieldPath grouping = ((FieldPath) _feature_1);
      Object _feature_2 = this.feature(search, "mmr");
      final MmrConfig diversity = ((MmrConfig) _feature_2);
      Object _feature_3 = this.feature(search, "boost");
      final BoostBlock boosting = ((BoostBlock) _feature_3);
      Object _elvis = null;
      Object _feature_4 = this.feature(search, "sortClauses");
      if (_feature_4 != null) {
        _elvis = _feature_4;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<SortClause> sortClauses = ((List<SortClause>) _elvis);
      if (((cursorText != null) && ((grouping != null) || (diversity != null)))) {
        throw Unsupported.of("the \'cursor\' clause with group-by or mmr; keyset pagination continues after one record in an order that each record\'s own values decide");
      }
      if (((grouping != null) && (!sortClauses.isEmpty()))) {
        throw Unsupported.of("sort with group-by; groups follow the relevance of their best hit");
      }
      if (((grouping != null) && (diversity != null))) {
        throw Unsupported.of("mmr with group-by; MMR selects single hits, groups select records per group value");
      }
      if ((((boosting != null) && ModelUtil.written(search, "scoreForm")) && Objects.equals(this.feature(search, "scoreForm"), EScoreForm.NATIVE))) {
        throw Unsupported.of("boosts with score native; boosts apply to the canonical similarity, where higher is better");
      }
      PgvectorQueries.Group _xifexpression = null;
      if ((grouping == null)) {
        _xifexpression = null;
      } else {
        PgvectorQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, grouping, "group-by");
        Integer _xifexpression_1 = null;
        boolean _written = ModelUtil.written(search, "groupSize");
        if (_written) {
          Object _feature_5 = this.feature(search, "groupSize");
          _xifexpression_1 = ((Integer) _feature_5);
        } else {
          _xifexpression_1 = Integer.valueOf(1);
        }
        _xifexpression = new PgvectorQueries.Group(_fieldRefOf, (_xifexpression_1).intValue());
      }
      final PgvectorQueries.Group group = _xifexpression;
      final List<PgvectorQueries.Aggregate> aggregates = CollectionLiterals.<PgvectorQueries.Aggregate>newArrayList();
      Object _elvis_1 = null;
      Object _feature_6 = this.feature(search, "aggregations");
      if (_feature_6 != null) {
        _elvis_1 = _feature_6;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      for (final AggregationClause clause : ((List<AggregationClause>) _elvis_1)) {
        PgvectorQueries.Aggregate _aggregateOf = this.aggregateOf(collection, table, clause);
        aggregates.add(_aggregateOf);
      }
      final List<PgvectorSql.Column> marked = CollectionLiterals.<PgvectorSql.Column>newArrayList();
      Object _elvis_2 = null;
      Object _feature_7 = this.feature(search, "highlighted");
      if (_feature_7 != null) {
        _elvis_2 = _feature_7;
      } else {
        ArrayList<Object> _newArrayList_2 = CollectionLiterals.<Object>newArrayList();
        _elvis_2 = _newArrayList_2;
      }
      for (final Field highlighted : ((List<Field>) _elvis_2)) {
        {
          boolean _isEmpty = terms.isEmpty();
          if (_isEmpty) {
            throw Unsupported.of("the \'highlight\' clause on a search without text; ts_headline marks the terms of a text query");
          }
          final PgvectorSql.Column markedColumn = table.column(ModelUtil.fieldName(highlighted));
          String _config = markedColumn.config();
          boolean _tripleEquals = (_config == null);
          if (_tripleEquals) {
            String _name = markedColumn.name();
            String _plus = ("highlighting " + _name);
            String _plus_1 = (_plus + ", which is not a full-text field");
            throw Unsupported.of(_plus_1);
          }
          marked.add(markedColumn);
        }
      }
      final List<PgvectorQueries.Order> order = CollectionLiterals.<PgvectorQueries.Order>newArrayList();
      for (final SortClause clause_1 : sortClauses) {
        PgvectorQueries.Ref _xifexpression_2 = null;
        boolean _isByRelevance = clause_1.isByRelevance();
        if (_isByRelevance) {
          _xifexpression_2 = null;
        } else {
          _xifexpression_2 = this.fieldRefOf(collection, table, clause_1.getField(), "sort");
        }
        ESortDirection _direction = clause_1.getDirection();
        boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
        PgvectorQueries.Order _order = new PgvectorQueries.Order(_xifexpression_2, _equals);
        order.add(_order);
      }
      Code.Text _xifexpression_3 = null;
      if ((cursorText == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = Code.text(cursorText);
      }
      final Code.Text cursorKey = _xifexpression_3;
      PgvectorQueries.Boost _xifexpression_4 = null;
      if ((boosting == null)) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = this.boostOf(collection, table, boosting);
      }
      PgvectorQueries.Mmr _xifexpression_5 = null;
      if ((diversity == null)) {
        _xifexpression_5 = null;
      } else {
        Object _feature_8 = this.feature(search, "topK");
        int _offsetOf = this.offsetOf(search);
        int _plus = ((((Integer) _feature_8)).intValue() + _offsetOf);
        _xifexpression_5 = this.mmrOf(collection, table, diversity, searched, _plus);
      }
      boolean _written_1 = ModelUtil.written(search, "withTotal");
      _xblockexpression = new PgvectorQueries.Shape(cursorKey, _xifexpression_4, _xifexpression_5, group, _written_1, aggregates, marked, terms, order);
    }
    return _xblockexpression;
  }

  /**
   * Prefetch stages as CTEs of the rows each stage retrieves (its own input, vector field, metric, filter and limit, ranking only
   * the rows its nested stages return); the names of the given stages, in order. A stage without input or field takes the search's.
   */
  private List<String> prefetchStages(final EObject search, final PgvectorSql.Query q, final VectorCollection collection, final PgvectorSql.Table table, final List<Prefetch> given, final QueryInput input, final VectorField field, final List<String> stages) {
    List<String> _xblockexpression = null;
    {
      final List<String> names = CollectionLiterals.<String>newArrayList();
      for (final Prefetch stage : given) {
        {
          this.schema.note(stage, "weight", "params");
          QueryInput _elvis = null;
          QueryInput _input = stage.getInput();
          if (_input != null) {
            _elvis = _input;
          } else {
            _elvis = input;
          }
          final QueryInput stageInput = _elvis;
          VectorField _xifexpression = null;
          VectorField _vectorField = stage.getVectorField();
          boolean _tripleNotEquals = (_vectorField != null);
          if (_tripleNotEquals) {
            _xifexpression = this.vectorFieldOf(collection, stage.getVectorField());
          } else {
            _xifexpression = field;
          }
          final VectorField stageField = _xifexpression;
          final List<String> inner = this.prefetchStages(search, q, collection, table, stage.getNested(), stageInput, stageField, stages);
          final PgvectorSql.Column stageColumn = table.column(ModelUtil.fieldName(stageField));
          String _xifexpression_1 = null;
          boolean _written = ModelUtil.written(stage, "metric");
          if (_written) {
            _xifexpression_1 = stage.getMetric().getLiteral();
          } else {
            _xifexpression_1 = stageColumn.vec().metric();
          }
          final String stageMetric = _xifexpression_1;
          final String stageQuery = this.queryVector(q, stageInput, collection, table, stageColumn);
          final String stageWhere = PgvectorSql.where(q, table, this.filterOf(search, collection, table, stage.getFilter(), false).parts());
          int _size = stages.size();
          final String name = ("stage_" + Integer.valueOf(_size));
          String _within = PgvectorQueries.within(inner);
          String _stage = PgvectorQueries.stage(name, table, stageColumn, stageMetric, stageQuery, PgvectorSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(stageWhere, _within))), stage.getStageLimit());
          stages.add(_stage);
          names.add(name);
        }
      }
      _xblockexpression = names;
    }
    return _xblockexpression;
  }

  /**
   * Candidates the index must deliver: the MMR pool when there is one, else limit plus offset.
   */
  private int fetchOf(final PgvectorQueries.Shape shape, final int fetch) {
    int _xifexpression = (int) 0;
    PgvectorQueries.Mmr _mmr = shape.mmr();
    boolean _tripleEquals = (_mmr == null);
    if (_tripleEquals) {
      _xifexpression = fetch;
    } else {
      _xifexpression = Math.max(shape.mmr().candidates(), fetch);
    }
    return _xifexpression;
  }

  /**
   * A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a jsonb column.
   */
  private PgvectorQueries.Ref fieldRefOf(final VectorCollection collection, final PgvectorSql.Table table, final FieldPath path, final String purpose) {
    PgvectorQueries.Ref _xblockexpression = null;
    {
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of((("raw paths in " + purpose) + "; the generator reads declared fields and their sub-fields"));
      }
      Field _field = path.getField();
      boolean _not = (!(_field instanceof PayloadField));
      if (_not) {
        String _fieldName = ModelUtil.fieldName(path.getField());
        String _plus = ((purpose + " on vector field ") + _fieldName);
        throw Unsupported.of(_plus);
      }
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        return new PgvectorQueries.Ref(column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null);
      }
      final Function1<Field, String> _function = (Field sub) -> {
        return ModelUtil.fieldName(sub);
      };
      List<String> _list = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      String _leafType = PgvectorFilter.leafType(path);
      _xblockexpression = new PgvectorQueries.Ref(column, _list, _leafType);
    }
    return _xblockexpression;
  }

  /**
   * An aggregation over the matching records, named by its alias or kind_field as on the other targets.
   */
  private PgvectorQueries.Aggregate aggregateOf(final VectorCollection collection, final PgvectorSql.Table table, final AggregationClause clause) {
    PgvectorQueries.Aggregate _xblockexpression = null;
    {
      final String kind = clause.getKind().getLiteral();
      final PgvectorQueries.Ref source = this.fieldRefOf(collection, table, clause.getField(), "aggregations");
      String _elvis = null;
      String _alias = clause.getAlias();
      if (_alias != null) {
        _elvis = _alias;
      } else {
        String _name = source.column().name();
        List<String> _path = source.path();
        String _join = IterableExtensions.join(Iterables.<String>concat(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_name)), _path), "_");
        String _plus = ((kind + "_") + _join);
        _elvis = _plus;
      }
      final String name = _elvis;
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(clause, "bucketCount");
      if (_written) {
        _xifexpression = clause.getBucketCount();
      } else {
        _xifexpression = 10;
      }
      final int size = _xifexpression;
      PgvectorQueries.Aggregate _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case "range":
            throw Unsupported.of("range aggregations; vecDSL gives no range bounds to count records in");
          case "histogram":
            PgvectorQueries.Aggregate _xblockexpression_1 = null;
            {
              if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                throw Unsupported.of("a histogram without a numeric interval");
              }
              Code.Num _num = Code.num(Double.parseDouble(clause.getInterval()));
              _xblockexpression_1 = new PgvectorQueries.Aggregate(name, kind, source, _num, size);
            }
            _switchResult = _xblockexpression_1;
            break;
          case "date_histogram":
            PgvectorQueries.Aggregate _xblockexpression_2 = null;
            {
              String _interval = clause.getInterval();
              boolean _tripleEquals = (_interval == null);
              if (_tripleEquals) {
                throw Unsupported.of("a date histogram without an interval");
              }
              Code.Text _text = Code.text(clause.getInterval());
              _xblockexpression_2 = new PgvectorQueries.Aggregate(name, kind, source, _text, size);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            _switchResult = new PgvectorQueries.Aggregate(name, kind, source, null, size);
            break;
        }
      } else {
        _switchResult = new PgvectorQueries.Aggregate(name, kind, source, null, size);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Boost functions: defaults weight 1, decay rate 0.5 and offset 0, combine and apply multiply.
   */
  private PgvectorQueries.Boost boostOf(final VectorCollection collection, final PgvectorSql.Table table, final BoostBlock block) {
    PgvectorQueries.Boost _xblockexpression = null;
    {
      final List<PgvectorQueries.BoostFn> functions = CollectionLiterals.<PgvectorQueries.BoostFn>newArrayList();
      EList<BoostFunction> _functions = block.getFunctions();
      for (final BoostFunction fn : _functions) {
        {
          double _xifexpression = (double) 0;
          boolean _written = ModelUtil.written(fn, "weight");
          if (_written) {
            _xifexpression = fn.getWeight();
          } else {
            _xifexpression = 1.0;
          }
          final double weight = _xifexpression;
          final List<PgvectorSql.Cond> when = CollectionLiterals.<PgvectorSql.Cond>newArrayList();
          Condition _condition = fn.getCondition();
          boolean _tripleNotEquals = (_condition != null);
          if (_tripleNotEquals) {
            PgvectorSql.Cond _condition_1 = new PgvectorFilter().condition(fn.getCondition());
            when.add(_condition_1);
          }
          EBoostKind _kind = fn.getKind();
          if (_kind != null) {
            switch (_kind) {
              case FIELD_VALUE:
                FieldPath _target = fn.getTarget();
                boolean _tripleEquals = (_target == null);
                if (_tripleEquals) {
                  throw Unsupported.of("a field_value boost without a field");
                }
                PgvectorQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
                PgvectorQueries.BoostFn _boostFn = new PgvectorQueries.BoostFn("field_value", _fieldRefOf, weight, null, null, null, null, 0.5, when);
                functions.add(_boostFn);
                break;
              case DECAY:
                if (((fn.getTarget() == null) || (!ModelUtil.written(fn, "shape")))) {
                  throw Unsupported.of("a decay boost without a field, decay shape, origin and scale");
                }
                double _xifexpression_1 = (double) 0;
                boolean _written_1 = ModelUtil.written(fn, "decayRate");
                if (_written_1) {
                  _xifexpression_1 = fn.getDecayRate();
                } else {
                  _xifexpression_1 = 0.5;
                }
                final double rate = _xifexpression_1;
                if (((rate <= 0) || (rate >= 1))) {
                  throw Unsupported.of("a decay rate outside the open interval from 0 to 1");
                }
                PgvectorQueries.Ref _fieldRefOf_1 = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
                String _literal = fn.getShape().getLiteral();
                Code.Expr _of = Literals.of(fn.getOrigin());
                Code.Expr _of_1 = Literals.of(fn.getScale());
                Code.Expr _xifexpression_2 = null;
                Literal _decayOffset = fn.getDecayOffset();
                boolean _tripleEquals_1 = (_decayOffset == null);
                if (_tripleEquals_1) {
                  _xifexpression_2 = null;
                } else {
                  _xifexpression_2 = Literals.of(fn.getDecayOffset());
                }
                PgvectorQueries.BoostFn _boostFn_1 = new PgvectorQueries.BoostFn("decay", _fieldRefOf_1, weight, _literal, _of, _of_1, _xifexpression_2, rate, when);
                functions.add(_boostFn_1);
                break;
              case CONDITION:
                boolean _isEmpty = when.isEmpty();
                if (_isEmpty) {
                  throw Unsupported.of("a condition boost without a condition");
                }
                PgvectorQueries.BoostFn _boostFn_2 = new PgvectorQueries.BoostFn("condition", null, weight, null, null, null, null, 0.5, when);
                functions.add(_boostFn_2);
                break;
              default:
                break;
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
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(block, "apply");
      if (_written_1) {
        _xifexpression_1 = block.getApply().getLiteral();
      } else {
        _xifexpression_1 = "multiply";
      }
      _xblockexpression = new PgvectorQueries.Boost(functions, _xifexpression, _xifexpression_1);
    }
    return _xblockexpression;
  }

  /**
   * MMR over a candidate pool (the given size, else ten times limit plus offset), diversified on the named, searched or only vector field.
   */
  private PgvectorQueries.Mmr mmrOf(final VectorCollection collection, final PgvectorSql.Table table, final MmrConfig config, final VectorField searched, final int fetch) {
    PgvectorQueries.Mmr _xblockexpression = null;
    {
      if (((config.getDiversityLambda() < 0) || (config.getDiversityLambda() > 1))) {
        throw Unsupported.of("an mmr lambda outside 0 to 1");
      }
      VectorField _xifexpression = null;
      VectorField _vectorField = config.getVectorField();
      boolean _tripleNotEquals = (_vectorField != null);
      if (_tripleNotEquals) {
        _xifexpression = this.vectorFieldOf(collection, config.getVectorField());
      } else {
        VectorField _elvis = null;
        if (searched != null) {
          _elvis = searched;
        } else {
          VectorField _vectorFieldOf = this.vectorFieldOf(collection, null);
          _elvis = _vectorFieldOf;
        }
        _xifexpression = _elvis;
      }
      final VectorField field = _xifexpression;
      final PgvectorSql.Column column = table.column(ModelUtil.fieldName(field));
      int _xifexpression_1 = (int) 0;
      boolean _written = ModelUtil.written(config, "candidates");
      if (_written) {
        _xifexpression_1 = Math.max(config.getCandidates(), fetch);
      } else {
        _xifexpression_1 = Math.max((10 * fetch), 10);
      }
      final int pool = _xifexpression_1;
      double _diversityLambda = config.getDiversityLambda();
      String _metric = column.vec().metric();
      _xblockexpression = new PgvectorQueries.Mmr(_diversityLambda, pool, column, _metric);
    }
    return _xblockexpression;
  }

  /**
   * Keyword fields: the listed full-text columns with boosts, else every full-text column of the collection.
   */
  private List<PgvectorQueries.KeywordField> keywordFields(final VectorCollection collection, final PgvectorSql.Table table, final List<SearchFieldRef> refs) {
    List<PgvectorQueries.KeywordField> _xblockexpression = null;
    {
      final List<PgvectorQueries.KeywordField> result = CollectionLiterals.<PgvectorQueries.KeywordField>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<PgvectorSql.Column> _columns = table.columns();
        for (final PgvectorSql.Column column : _columns) {
          String _config = column.config();
          boolean _tripleNotEquals = (_config != null);
          if (_tripleNotEquals) {
            PgvectorQueries.KeywordField _keywordField = new PgvectorQueries.KeywordField(column, 1.0);
            result.add(_keywordField);
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final PgvectorSql.Column column_1 = table.column(ModelUtil.fieldName(ref.getField()));
            String _config_1 = column_1.config();
            boolean _tripleEquals = (_config_1 == null);
            if (_tripleEquals) {
              String _name = column_1.name();
              String _plus = ("keyword search on " + _name);
              String _plus_1 = (_plus + ", which is not a full-text field");
              throw Unsupported.of(_plus_1);
            }
            double _xifexpression = (double) 0;
            boolean _written = ModelUtil.written(ref, "boost");
            if (_written) {
              _xifexpression = ref.getBoost();
            } else {
              _xifexpression = 1.0;
            }
            PgvectorQueries.KeywordField _keywordField_1 = new PgvectorQueries.KeywordField(column_1, _xifexpression);
            result.add(_keywordField_1);
          }
        }
      }
      boolean _isEmpty_1 = result.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search in a collection without full-text fields");
      }
      _xblockexpression = result;
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
   * The tsquery form of a text query type: match (all terms with and, any term with or) and phrase.
   */
  private String keywordOperator(final String type, final String operator) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case "match":
          String _xifexpression = null;
          boolean _equals = Objects.equals(operator, "or");
          if (_equals) {
            _xifexpression = "match_any_term";
          } else {
            _xifexpression = "match";
          }
          _switchResult = _xifexpression;
          break;
        case "phrase":
          _switchResult = "phrase";
          break;
        case "prefix":
        case "phrase_prefix":
        case "term":
        case "query_string":
          throw Unsupported.of((((("text-query " + type) + "; ") + PgvectorStatements.PENDING) + " (to_tsquery prefixes, simple lexemes and websearch_to_tsquery)"));
        default:
          throw Unsupported.of((("text-query " + type) + "; PostgreSQL full-text search matches normalized lexemes, without fuzzy, wildcard or regular-expression terms"));
      }
    } else {
      throw Unsupported.of((("text-query " + type) + "; PostgreSQL full-text search matches normalized lexemes, without fuzzy, wildcard or regular-expression terms"));
    }
    return _switchResult;
  }

  private Code.Expr rows(final RecordSource source, final PgvectorSql.Table table) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", PgvectorSchema.kw("location", Code.text(((FileSource)source).getLocation())));
    }
    if (!_matched) {
      if (source instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Expr> _function = (RecordLiteral item) -> {
          return this.record(item, table);
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

  private Code.Expr record(final RecordLiteral record, final PgvectorSql.Table table) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final PgvectorSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
          result.put(column.name(), this.convert(entry.getValue(), column));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A literal in the form PostgreSQL reads for its column: sparse vectors as {index:value}/dim with indexes from 1 (vecDSL
   * counts from 0, as Milvus, Qdrant and Pinecone), bit vectors as a bit string; everything else as written.
   */
  private Code.Expr convert(final Literal literal, final PgvectorSql.Column column) {
    Code.Expr _xblockexpression = null;
    {
      PgvectorSql.Vec _vec = column.vec();
      boolean _tripleEquals = (_vec == null);
      if (_tripleEquals) {
        return Literals.of(literal);
      }
      String _type = column.vec().type();
      if (_type != null) {
        switch (_type) {
          case "sparsevec":
            if ((literal instanceof SparseLit)) {
              final Function1<SparseEntry, String> _function = (SparseEntry e) -> {
                int _index = e.getIndex();
                int _plus = (_index + 1);
                String _valueOf = String.valueOf(_plus);
                String _plus_1 = (_valueOf + ":");
                String _number = PgvectorSql.number(e.getWeight());
                return (_plus_1 + _number);
              };
              final String weights = IterableExtensions.join(ListExtensions.<SparseEntry, String>map(((SparseLit) literal).getEntries(), _function), ",");
              int _dim = column.vec().dim();
              boolean _lessEqualsThan = (_dim <= 0);
              if (_lessEqualsThan) {
                throw Unsupported.of("a sparse vector field without dim; sparsevec text names its dimension");
              }
              int _dim_1 = column.vec().dim();
              String _plus = ((("{" + weights) + "}/") + Integer.valueOf(_dim_1));
              return Code.text(_plus);
            }
            break;
          case "bit":
            if ((literal instanceof ListLit)) {
              final Function1<Literal, String> _function_1 = (Literal item) -> {
                String _xifexpression = null;
                if (((item instanceof NumberLit) && (((NumberLit) item).getValue() != 0))) {
                  _xifexpression = "1";
                } else {
                  _xifexpression = "0";
                }
                return _xifexpression;
              };
              final String bits = IterableExtensions.join(ListExtensions.<Literal, String>map(((ListLit) literal).getItems(), _function_1));
              return Code.text(bits);
            }
            break;
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
