package org.ftn.vecDSL.generator.oracledb;

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
import java.util.function.Supplier;
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
import org.ftn.vecDSL.vecDSL.EParamType;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
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
 * Maps each vecDSL statement to SQL for Oracle AI Database 26ai in the host-language-neutral code model. The SQL text is built
 * here (through OracledbSql and OracledbQueries) and passed to the runtime with its bind values, so the three host languages
 * send identical statements: query and value return JSON rows, modify returns the affected row count, execute runs DDL and
 * PL/SQL blocks (each DDL statement commits), transaction runs a vecDSL transaction.
 */
@SuppressWarnings("all")
public class OracledbStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; OracledbCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Oracle: ";

  /**
   * Suffix of constructs Oracle supports that the generator does not produce yet.
   */
  public static final String PENDING = OracledbSql.PENDING;

  public static final String NO_MODELS = "; generated Oracle programs load no ONNX embedding model into the database (VECTOR_EMBEDDING and hybrid vector indexes need one), so they supply their own vectors";

  public static final String EXTERNAL = "collections derived from external sources; an Oracle external table reads its files on the database server through a directory object, which only a user with the CREATE ANY DIRECTORY privilege creates, so a generated program can neither create the directory nor place the files";

  public static final String NO_TTL = "ttl; Oracle rows do not expire (a DBMS_SCHEDULER job deleting old rows needs CREATE JOB and is not generated)";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; Oracle has no per-table drop guard for the owner of a table";

  public static final String NO_STORAGE = "storage settings; generated programs create tables in the user\'s default tablespace (tablespaces are created by a DBA)";

  public static final String NO_TAGS = "collection tags; an Oracle table carries one comment (the description) and no tags";

  public static final String NO_BM25 = "BM25 parameters; Oracle Text scores with its own algorithm (SCORE from 0 to 100) and takes no k1 or b";

  public static final String NO_SNAPSHOTS = "snapshots; Oracle backs up with RMAN and Data Pump, outside a SQL session (restore points need SYSDBA or the SELECT ANY DICTIONARY and CREATE ANY RESTORE POINT privileges and flashback logging)";

  public static final String SINGLE_SERVER = "one Oracle AI Database Free instance holds the data; the Free edition has no Real Application Clusters and no Data Guard (Licensing Information, Free column)";

  private static final String NO_GENERATION = "generated Oracle programs call no generative model (DBMS_VECTOR_CHAIN.UTL_TO_GENERATE_TEXT needs a third-party provider and credentials)";

  private static final String NO_RERANKERS = "Oracle has no reranking model in the database for generated programs";

  private static final String NO_METRICS = "metrics configuration; Oracle statistics and diagnostics are instance settings and V$ views, not objects a generated program creates";

  private static final String NO_SCHEDULES = "backup schedules; Oracle backs up with RMAN, outside a SQL session";

  private static final String NO_CLUSTER_LIMITS = "cluster limits; Oracle has no setting that limits the number of tables of a user (quotas limit space)";

  private static final String NO_FILES = "file resources; Oracle Text thesauri and dictionaries are loaded with ctxload from the server\'s file system";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why Oracle cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("explain", "Oracle explains query plans (EXPLAIN PLAN), not scores"), Pair.<String, String>of("reranker", OracledbStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated Oracle programs embed no query text" + OracledbStatements.NO_MODELS)), Pair.<String, String>of("minShouldMatch", "an Oracle Text query combines terms with AND, OR and ACCUM and has no minimum number of matching terms")));

  /**
   * Clauses Oracle supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "JSON_EXISTS with a filter on array elements")));

  private static final Map<String, String> PRIVILEGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("read", "SELECT"), Pair.<String, String>of("write", "INSERT, UPDATE, DELETE"), Pair.<String, String>of("admin", "ALL"), Pair.<String, String>of("all", "ALL"), Pair.<String, String>of("select", "SELECT"), Pair.<String, String>of("insert", "INSERT"), Pair.<String, String>of("update", "UPDATE"), Pair.<String, String>of("delete", "DELETE"), Pair.<String, String>of("references", "REFERENCES"), Pair.<String, String>of("alter", "ALTER"), Pair.<String, String>of("index", "INDEX")));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final OracledbSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public OracledbStatements(final Script script) {
    this.script = script;
    OracledbSchema _oracledbSchema = new OracledbSchema(this.notes, script);
    this.schema = _oracledbSchema;
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
          _switchResult = "Oracle Text indexes text columns directly (CONTEXT indexes) and has no BM25 function producing sparse vectors";
          break;
        case "custom":
          _switchResult = (OracledbStatements.PENDING + " (virtual columns)");
          break;
        default:
          _switchResult = "generated Oracle programs load no ONNX model into the database, which model functions need";
          break;
      }
    } else {
      _switchResult = "generated Oracle programs load no ONNX model into the database, which model functions need";
    }
    return _switchResult;
  }

  public static String collectionLimit(final String type) {
    return (("collection limits (" + type) + "); Oracle has fixed limits (1000 columns per table, 65535 vector dimensions) and no per-table limit settings");
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
            throw Unsupported.of(OracledbStatements.NO_METRICS);
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
            throw Unsupported.of(OracledbStatements.NO_CLUSTER_LIMITS);
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + OracledbStatements.SINGLE_SERVER));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, OracledbGenerator.VENDOR).keySet();
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
          OracledbSchema.physical(((Database)concept).getName(), "x");
          boolean _contains = ((Database)concept).getName().contains(OracledbSchema.DATABASE_SEPARATOR);
          if (_contains) {
            String _name = ((Database)concept).getName();
            String _plus = ("the logical database name " + _name);
            String _plus_1 = (_plus + "; the generator separates database and collection names with ");
            String _plus_2 = (_plus_1 + OracledbSchema.DATABASE_SEPARATOR);
            throw Unsupported.of(_plus_2);
          }
          String _name_1 = ((Database)concept).getName();
          String _plus_3 = ("logical-db " + _name_1);
          String _plus_4 = (_plus_3 + ", a table-name prefix that exists through its collections");
          this.notes.add(_plus_4);
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
          final OracledbSql.Table table = this.schema.table(((Alias)concept).getCollection());
          String _createView = OracledbSchema.createView(((Alias)concept).getName(), table);
          Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)));
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
          String _functionReason = OracledbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          List<Code.Stmt> _createAnalyzer = this.schema.createAnalyzer(((Analyzer)concept));
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
          throw Unsupported.of(("embedding models" + OracledbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + OracledbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + OracledbStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization outside a vector index; Oracle quantizes in the QUANTIZATION clause of CREATE VECTOR INDEX");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + OracledbStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + OracledbStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_STORAGE);
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
          throw Unsupported.of(OracledbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (OracledbStatements.NOT_MAPPED + _name);
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
      throw Unsupported.of("embedded deployments; the Oracle drivers connect to a database server");
    }
    Literal _apiKey = concept.getApiKey();
    boolean _tripleNotEquals = (_apiKey != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("api keys; Oracle authenticates database users with passwords, certificates, Kerberos or tokens of an identity provider");
    }
    Literal _token = concept.getToken();
    boolean _tripleNotEquals_1 = (_token != null);
    if (_tripleNotEquals_1) {
      throw Unsupported.of("tokens; has no Oracle mapping yet (OAuth 2.0 access tokens of an identity provider, which the Free container is not configured for)");
    }
    Literal _password = concept.getPassword();
    if ((_password instanceof StringLit)) {
      throw Unsupported.of("a password written in the script; read it from an environment variable with env(...)");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; Oracle Net listens on one port");
    }
    if (((concept.getUri() != null) && (OracledbGenerator.dsnOf(concept.getUri()) == null))) {
      String _uri = concept.getUri();
      String _plus = ("the connection uri " + _uri);
      String _plus_1 = (_plus + "; Oracle programs take an Easy Connect string host:port/service, with the user in username and the password from env(...)");
      throw Unsupported.of(_plus_1);
    }
    Set<String> _keySet = ModelUtil.extParams(concept, OracledbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
    this.schema.note(concept, "timeoutMillis", "cloud", "region", "project");
  }

  /**
   * Password authentication is the connection's; roles are Oracle roles; other methods are server configuration.
   */
  private List<Code.Stmt> authentication(final AuthConfig auth, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((!Objects.equals(auth.getType(), EAuthType.NONE)) && (!Objects.equals(auth.getType(), EAuthType.USERNAME_PASSWORD)))) {
        String _literal = auth.getType().getLiteral();
        String _plus = (_literal + " authentication; Oracle authenticates users with passwords, certificates, Kerberos, RADIUS or an identity provider, configured on the server, and has no api keys");
        throw Unsupported.of(_plus);
      }
      Literal _token = auth.getToken();
      if ((_token instanceof StringLit)) {
        throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
      }
      String _certificatePath = auth.getCertificatePath();
      boolean _tripleNotEquals = (_certificatePath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("client certificates; has no Oracle mapping yet (TCPS with a wallet)");
      }
      if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
        throw Unsupported.of("customer-managed keys and field encryption; Transparent Data Encryption needs a keystore that a DBA configures");
      }
      boolean _isAuditLog = auth.isAuditLog();
      if (_isAuditLog) {
        throw Unsupported.of("audit logs; Oracle audit policies (CREATE AUDIT POLICY) need the AUDIT_ADMIN role, which the generated programs\' user does not have");
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
   * A role and its object privileges on a collection (or on every table of the program's schema is not expressible: one table is required).
   */
  private List<Code.Stmt> role(final RoleBinding binding, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = binding.getRoleName();
      OracledbSql.id(name);
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        throw Unsupported.of("a role without a collection; Oracle grants object privileges table by table (GRANT ... ON ANY TABLE is a system privilege)");
      }
      final OracledbSql.Table table = this.schema.table(binding.getCollection());
      final List<String> grants = CollectionLiterals.<String>newArrayList();
      EList<String> _privileges = binding.getPrivileges();
      for (final String privilege : _privileges) {
        {
          final String sql = OracledbStatements.PRIVILEGES.get(privilege.toLowerCase());
          if ((sql == null)) {
            throw Unsupported.of((("the privilege " + privilege) + "; Oracle grants SELECT, INSERT, UPDATE, DELETE, REFERENCES, ALTER and INDEX on tables"));
          }
          grants.add(sql);
        }
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      String _id = OracledbSql.id(name);
      String _plus = ("CREATE ROLE " + _id);
      final Code.Stmt create = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)));
      if (ifNotExists) {
        Code.Call _roleExists = this.roleExists(name);
        Code.When _when = new Code.When(_roleExists, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
        result.add(_when);
      } else {
        result.add(create);
      }
      boolean _isEmpty = grants.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _join = IterableExtensions.join(grants, ", ");
        String _plus_1 = ("GRANT " + _join);
        String _plus_2 = (_plus_1 + " ON ");
        String _table = OracledbSql.table(table);
        String _plus_3 = (_plus_2 + _table);
        String _plus_4 = (_plus_3 + " TO ");
        String _id_1 = OracledbSql.id(name);
        String _plus_5 = (_plus_4 + _id_1);
        Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_5)));
        result.add(_execute);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Call roleExists(final String name) {
    Code.Call _xblockexpression = null;
    {
      final OracledbSql.Query q = new OracledbSql.Query();
      String _scalar = OracledbQueries.scalar("COUNT(*)");
      String _plus = ("SELECT " + _scalar);
      String _plus_1 = (_plus + " FROM user_role_privs WHERE granted_role = ");
      String _bind = q.bind(Code.text(name), "string");
      String _plus_2 = (_plus_1 + _bind);
      _xblockexpression = OracledbSchema.call("exists", _plus_2, q, null);
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
    throw Unsupported.of((what + " outside a collection; Oracle adds it to a table"));
  }

  /**
   * A filter of an analyzer: the lexer preference applies it already, so creating it checks the preference exists.
   */
  private List<Code.Stmt> analyzerFilter(final AnalyzerFilter filter) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(filter, Analyzer.class);
      if ((analyzer == null)) {
        throw Unsupported.of("an analyzer filter outside an analyzer; a lexer preference takes its attributes when it is created");
      }
      String _literal = filter.getType().getLiteral();
      String _xifexpression = null;
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = OracledbSchema.stemmer(analyzer.getLanguage());
      }
      OracledbSchema.checkFilter(_literal, _xifexpression);
      final OracledbSql.Query q = new OracledbSql.Query();
      String _scalar = OracledbQueries.scalar("COUNT(*)");
      String _plus = ("SELECT " + _scalar);
      String _plus_1 = (_plus + " FROM ctx_user_preferences WHERE pre_name = ");
      String _bind = q.bind(Code.text(analyzer.getName().toUpperCase()), "string");
      String _plus_2 = (_plus_1 + _bind);
      Code.Call _call = OracledbSchema.call("value", _plus_2, q, null);
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
      final OracledbSql.Table table = this.schema.table(collection);
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
      final String check = OracledbSchema.check(limit, field);
      String _table = OracledbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD ");
      String _replace = check.replace("{}", OracledbSql.id(column.name()));
      String _plus_2 = (_plus_1 + _replace);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * An index created on an existing collection, unless an index of that name exists.
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
        final OracledbSql.Table table = this.schema.table(collection);
        final String sql = this.schema.vectorIndex(collection, table, field, ((VectorIndex)index));
        if ((sql == null)) {
          return this.require(collection);
        }
        Code.Stmt _guarded = OracledbSchema.guarded(OracledbSchema.indexName(collection, ((VectorIndex)index).getName()), sql);
        _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_guarded));
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
          final OracledbSql.Table table = this.schema.table(collection);
          final String name = OracledbSchema.indexName(collection, ((PayloadIndex)index).getName());
          if ((Objects.equals(((PayloadIndex)index).getKind(), EPayloadIndexKind.FULL_TEXT) && (field instanceof PayloadField))) {
            String _textIndexName = OracledbSchema.textIndexName(collection, ((PayloadField) field));
            Code.Stmt _guarded = OracledbSchema.guarded(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name, _textIndexName)), this.schema.payloadIndex(collection, table, ((PayloadIndex)index)));
            return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_guarded));
          }
          Code.Stmt _guarded_1 = OracledbSchema.guarded(name, this.schema.payloadIndex(collection, table, ((PayloadIndex)index)));
          _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_guarded_1));
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
      final OracledbSql.Table table = this.schema.table(collection);
      String _table = OracledbSql.table(table);
      String _plus = ("SELECT NULL FROM " + _table);
      String _plus_1 = (_plus + " WHERE 1 = 0");
      Code.Call _call = OracledbSchema.call("query", _plus_1, null, null);
      Code.Run _run = new Code.Run(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * ALTER TABLE ADD; a vector field with an index gets the index, a full-text field its CONTEXT index.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(collection);
      final List<Code.Stmt> result = this.addColumn(collection, table, field);
      this.schema.evolve(collection, table.with(this.schema.column(collection, field)));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> addColumn(final VectorCollection collection, final OracledbSql.Table table, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Column column = this.schema.column(collection, field);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      OracledbSql.Column _xifexpression = null;
      if ((field instanceof VectorField)) {
        _xifexpression = this.nullableVector(column);
      } else {
        _xifexpression = column;
      }
      final String definition = OracledbSql.columnDefinition(_xifexpression, false);
      String _table = OracledbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD (");
      String _plus_2 = (_plus_1 + definition);
      String _plus_3 = (_plus_2 + ")");
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_3)));
      result.add(_execute);
      if ((field instanceof VectorField)) {
        final VectorIndex index = this.schema.indexOf(collection, ((VectorField) field));
        if ((index != null)) {
          final String sql = this.schema.vectorIndex(collection, table.with(column), ((VectorField) field), index);
          if ((sql != null)) {
            Code.Stmt _guarded = OracledbSchema.guarded(OracledbSchema.indexName(collection, index.getName()), sql);
            result.add(_guarded);
          }
        }
      }
      if ((column.text() && (field instanceof PayloadField))) {
        final String name = OracledbSchema.textIndexName(collection, ((PayloadField) field));
        Code.Stmt _guarded_1 = OracledbSchema.guarded(name, OracledbSql.createPayloadIndex(table.with(column), name, column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null, "text"));
        result.add(_guarded_1);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Rows that exist when a vector column is added have no vector, so the added column is nullable.
   */
  private OracledbSql.Column nullableVector(final OracledbSql.Column column) {
    String _name = column.name();
    String _type = column.type();
    OracledbSql.Vec _vec = column.vec();
    return new OracledbSql.Column(_name, _type, null, false, _vec, null, true, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
  }

  /**
   * A list partition added to the table; automatic list partitioning would also create it on the first write.
   */
  private List<Code.Stmt> addPartition(final VectorCollection collection, final Partition partition, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(collection);
      String _partition = table.partition();
      boolean _tripleEquals = (_partition == null);
      if (_tripleEquals) {
        throw Unsupported.of("a partition of a collection declared without partitions; has no Oracle mapping yet (ALTER TABLE MODIFY PARTITION BY converts a table online)");
      }
      if (((partition.getKey() != null) || (!partition.getSubPartitions().isEmpty()))) {
        throw Unsupported.of("partitions by key and sub-partitions; has no Oracle mapping yet (hash and composite partitioning)");
      }
      String _table = OracledbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD ");
      String _partitionClause = OracledbSql.partitionClause(partition.getName());
      String _plus_2 = (_plus_1 + _partitionClause);
      final Code.Stmt add = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(add));
      }
      final OracledbSql.Query q = new OracledbSql.Query();
      String _scalar = OracledbQueries.scalar("COUNT(*)");
      String _plus_3 = ("SELECT " + _scalar);
      String _plus_4 = (_plus_3 + " FROM user_tab_partitions WHERE table_name = ");
      String _bind = q.bind(Code.text(table.name()), "string");
      String _plus_5 = (_plus_4 + _bind);
      String _plus_6 = (_plus_5 + " AND partition_name = ");
      String _bind_1 = q.bind(Code.text(partition.getName()), "string");
      String _plus_7 = (_plus_6 + _bind_1);
      Code.Call _call = OracledbSchema.call("exists", _plus_7, q, null);
      Code.When _when = new Code.When(_call, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(add)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
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
        String _id = OracledbSql.id(alias.getName());
        String _plus = ("RENAME " + _id);
        String _plus_1 = (_plus + " TO ");
        String _id_1 = OracledbSql.id(it.getNewName());
        String _plus_2 = (_plus_1 + _id_1);
        Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus_3 = ("altering a " + _name);
        String _plus_4 = (_plus_3 + "; Oracle alters tables and views");
        throw Unsupported.of(_plus_4);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      OracledbSql.Table table = this.schema.table(collection);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        {
          List<Code.Stmt> _alter = this.alter(collection, table, operation);
          Iterables.<Code.Stmt>addAll(result, _alter);
          table = this.evolved(collection, table, operation);
        }
      }
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        final String renamed = OracledbSchema.physical(table.database(), it.getNewName());
        String _table = OracledbSql.table(table);
        String _plus_5 = ("ALTER TABLE " + _table);
        String _plus_6 = (_plus_5 + " RENAME TO ");
        String _id_2 = OracledbSql.id(renamed);
        String _plus_7 = (_plus_6 + _id_2);
        Code.Stmt _execute_1 = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_7)));
        result.add(_execute_1);
        table = table.named(renamed);
      }
      boolean _isEmpty = result.isEmpty();
      if (_isEmpty) {
        List<Code.Stmt> _require = this.require(collection);
        Iterables.<Code.Stmt>addAll(result, _require);
      }
      this.schema.evolve(collection, table);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The table after an alter operation, for the statements that follow it.
   */
  private OracledbSql.Table evolved(final VectorCollection collection, final OracledbSql.Table table, final AlterOperation operation) {
    OracledbSql.Table _switchResult = null;
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

  public List<Code.Stmt> alter(final VectorCollection collection, final OracledbSql.Table table, final AlterOperation operation) {
    List<Code.Stmt> _switchResult = null;
    boolean _matched = false;
    if (operation instanceof AddFieldOp) {
      _matched=true;
      _switchResult = this.addColumn(collection, table, ((AddFieldOp)operation).getNewField());
    }
    if (!_matched) {
      if (operation instanceof DropFieldOp) {
        _matched=true;
        String _dropColumn = this.dropColumn(collection, table, ((DropFieldOp)operation).getField());
        Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_dropColumn)));
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
    }
    if (!_matched) {
      if (operation instanceof RenameFieldOp) {
        _matched=true;
        List<Code.Stmt> _xblockexpression = null;
        {
          Field _field = ((RenameFieldOp)operation).getField();
          Field _primaryKey = collection.getPrimaryKey();
          boolean _tripleEquals = (_field == _primaryKey);
          if (_tripleEquals) {
            throw Unsupported.of("renaming the primary key; the generated statements address the key by its declared name");
          }
          OracledbSql.id(((RenameFieldOp)operation).getNewName());
          String _table = OracledbSql.table(table);
          String _plus = ("ALTER TABLE " + _table);
          String _plus_1 = (_plus + " RENAME COLUMN ");
          String _id = OracledbSql.id(table.column(ModelUtil.fieldName(((RenameFieldOp)operation).getField())).name());
          String _plus_2 = (_plus_1 + _id);
          String _plus_3 = (_plus_2 + " TO ");
          String _id_1 = OracledbSql.id(((RenameFieldOp)operation).getNewName());
          String _plus_4 = (_plus_3 + _id_1);
          Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_4)));
          _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
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
        String _createView = OracledbSchema.createView(((AddAliasOp)operation).getNewAlias().getName(), table);
        Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)));
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = OracledbStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = OracledbStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (OracledbStatements.NOT_MAPPED + _name);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private String dropColumn(final VectorCollection collection, final OracledbSql.Table table, final Field field) {
    String _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
      }
      String _table = OracledbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " DROP COLUMN ");
      String _id = OracledbSql.id(table.column(ModelUtil.fieldName(field)).name());
      _xblockexpression = (_plus_1 + _id);
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: the description is the table comment; consistency is met by every committed write.
   */
  private List<Code.Stmt> setProperty(final OracledbSql.Table table, final SetPropertyOp operation) {
    List<Code.Stmt> _switchResult = null;
    ECollectionProperty _property = operation.getProperty();
    if (_property != null) {
      switch (_property) {
        case DESCRIPTION:
          List<Code.Stmt> _xblockexpression = null;
          {
            Literal _value = operation.getValue();
            boolean _not = (!(_value instanceof StringLit));
            if (_not) {
              throw Unsupported.of("a description that is not a string literal; COMMENT takes a constant");
            }
            String _table = OracledbSql.table(table);
            String _plus = ("COMMENT ON TABLE " + _table);
            String _plus_1 = (_plus + " IS ");
            Literal _value_1 = operation.getValue();
            String _literal = OracledbSql.literal(((StringLit) _value_1).getValue());
            String _plus_2 = (_plus_1 + _literal);
            Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
            _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression;
          break;
        case CONSISTENCY:
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
          break;
        case TAGS:
          throw Unsupported.of(OracledbStatements.NO_TAGS);
        case DYNAMIC:
          throw Unsupported.of((("dynamic; " + OracledbStatements.PENDING) + " (a JSON column for undeclared fields)"));
        case TTL:
          throw Unsupported.of(OracledbStatements.NO_TTL);
        case DELETION_PROTECTION:
          throw Unsupported.of(OracledbStatements.NO_DELETION_PROTECTION);
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
          final OracledbSql.Table table = this.schema.table(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          String _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            _xifexpression = "IF EXISTS ";
          } else {
            _xifexpression = "";
          }
          String _plus = ("DROP TABLE " + _xifexpression);
          String _table = OracledbSql.table(table);
          String _plus_1 = (_plus + _table);
          String _plus_2 = (_plus_1 + " CASCADE CONSTRAINTS PURGE");
          Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final String prefix = OracledbSchema.physical(((Database)concept).getName(), "");
            int _length = prefix.length();
            String _plus = ("SELECT table_name FROM user_tables WHERE SUBSTR(table_name, 1, " + Integer.valueOf(_length));
            String _plus_1 = (_plus + ") = ");
            String _literal = OracledbSql.literal(prefix);
            final String tables = (_plus_1 + _literal);
            Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList((("BEGIN FOR r IN (" + tables) + ") LOOP EXECUTE IMMEDIATE \'DROP TABLE \"\' || r.table_name || \'\" CASCADE CONSTRAINTS PURGE\'; END LOOP; END;"))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
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
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            String _id = OracledbSql.id(((Alias)concept).getName());
            final String drop = ("DROP VIEW " + _id);
            String _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              _xifexpression = OracledbSql.tolerant(drop, (-942));
            } else {
              _xifexpression = drop;
            }
            Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression)));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final VectorCollection collection = this.member(concept, "a partition");
            final OracledbSql.Table table = this.schema.table(collection);
            String _partition = table.partition();
            boolean _tripleEquals = (_partition == null);
            if (_tripleEquals) {
              throw Unsupported.of("a partition of a collection declared without partitions");
            }
            String _table = OracledbSql.table(table);
            String _plus = ("ALTER TABLE " + _table);
            String _plus_1 = (_plus + " DROP PARTITION ");
            String _id = OracledbSql.id(((Partition)concept).getName());
            String _plus_2 = (_plus_1 + _id);
            final String drop = (_plus_2 + " UPDATE INDEXES");
            String _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              _xifexpression = OracledbSql.tolerant(drop, (-2149));
            } else {
              _xifexpression = drop;
            }
            Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression)));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          _switchResult = this.dropIndex(((Index)concept), it.isIfExists());
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          _switchResult = this.dropIndex(((Index)concept), it.isIfExists());
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = OracledbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; the quantization belongs to its vector index (drop the index)");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not an Oracle object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; the program drops tables and their objects, not the database");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + OracledbStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            String _literal = OracledbSql.literal(((Analyzer)concept).getName());
            String _plus = ("BEGIN CTX_DDL.DROP_PREFERENCE(" + _literal);
            final String drop = (_plus + "); END;");
            String _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              _xifexpression = OracledbSql.tolerant(drop, (-20000));
            } else {
              _xifexpression = drop;
            }
            Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression)));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; a lexer preference keeps the attributes of its language (drop the analyzer)");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          _switchResult = this.dropLimit(((FieldLimit)concept), it.isIfExists());
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + OracledbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + OracledbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + OracledbStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + OracledbStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + OracledbStatements.SINGLE_SERVER));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; Oracle authentication methods are server configuration");
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
          throw Unsupported.of(OracledbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(OracledbStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((OracledbStatements.NOT_MAPPED + "dropping a ") + _name);
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
      final OracledbSql.Table table = this.schema.table(collection);
      final OracledbSql.Query q = new OracledbSql.Query();
      String _table = OracledbSql.table(table);
      String _plus = ("DELETE FROM " + _table);
      String _plus_1 = (_plus + " ");
      String _plus_2 = (_plus_1 + OracledbSql.ALIAS);
      String _plus_3 = (_plus_2 + " WHERE ");
      String _col = OracledbSql.col(column);
      String _plus_4 = (_plus_3 + _col);
      String _plus_5 = (_plus_4 + " = ");
      String _bind = q.bind(Code.text(value), "string");
      final String sql = (_plus_5 + _bind);
      Code.Call _modify = this.modify(sql, q, "delete_count");
      Code.Return _return = new Code.Return(_modify);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(collection);
      String _dropColumn = this.dropColumn(collection, table, field);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_dropColumn)));
      final List<Code.Stmt> result = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      this.schema.evolve(collection, table.without(ModelUtil.fieldName(field)));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropIndex(final Index index, final boolean ifExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final boolean domain = ((index instanceof PayloadIndex) && Collections.<EPayloadIndexKind>unmodifiableList(CollectionLiterals.<EPayloadIndexKind>newArrayList(EPayloadIndexKind.FULL_TEXT, EPayloadIndexKind.JSON)).contains(((PayloadIndex) index).getKind()));
      String _id = OracledbSql.id(this.indexName(index));
      String _plus = ("DROP INDEX " + _id);
      String _xifexpression = null;
      if (domain) {
        _xifexpression = " FORCE";
      } else {
        _xifexpression = "";
      }
      final String drop = (_plus + _xifexpression);
      String _xifexpression_1 = null;
      if (ifExists) {
        _xifexpression_1 = OracledbSql.tolerant(drop, (-1418));
      } else {
        _xifexpression_1 = drop;
      }
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression_1)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  private String indexName(final Index index) {
    String _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(index);
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      _xblockexpression = OracledbSchema.indexName(collection, index.getName());
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

  private List<Code.Stmt> dropLimit(final FieldLimit limit, final boolean ifExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = EcoreUtil2.<Field>getContainerOfType(limit, Field.class);
      if ((field == null)) {
        throw Unsupported.of("a field limit outside a field declaration");
      }
      final OracledbSql.Table table = this.schema.table(this.member(field, "a field limit"));
      String _table = OracledbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " DROP CONSTRAINT ");
      String _id = OracledbSql.id(limit.getName());
      final String drop = (_plus_1 + _id);
      String _xifexpression = null;
      if (ifExists) {
        _xifexpression = OracledbSql.tolerant(drop, (-2443));
      } else {
        _xifexpression = drop;
      }
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * DROP ROLE revokes the role's grants.
   */
  private List<Code.Stmt> dropRole(final RoleBinding binding) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = binding.getRoleName();
      OracledbSql.id(name);
      Code.Call _roleExists = this.roleExists(name);
      String _id = OracledbSql.id(name);
      String _plus = ("DROP ROLE " + _id);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)));
      Code.When _when = new Code.When(_roleExists, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
    }
    return _xblockexpression;
  }

  /**
   * An alias is a view: the swap replaces its query (CREATE OR REPLACE VIEW).
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(it.getNewCollection());
      String _createView = OracledbSchema.createView(it.getAlias().getName(), table);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * A DML statement whose runtime result is {counter: affected rows}; skipped reports the records an insert did not write.
   */
  private Code.Call modify(final String sql, final OracledbSql.Query q, final String counter) {
    Code.Call _xblockexpression = null;
    {
      final Code.Call call = OracledbSchema.call("modify", sql, q, null);
      call.args().put("counter", Code.text(counter));
      _xblockexpression = call;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(OracledbStatements.NO_TTL);
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
        throw Unsupported.of(OracledbStatements.NO_TTL);
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
   * One write of the rows (as a JSON array parameter) with the slice values of the statement.
   */
  private Code.Call write(final VectorCollection collection, final RecordSource source, final String policy, final String counter, final EObject statement) {
    Code.Call _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(collection);
      final OracledbSql.Query q = new OracledbSql.Query();
      final Code.Expr records = this.rows(source, table);
      final Code.Call result = this.modify(OracledbSql.insertRows(q, table, records, this.slices(statement, collection, table), policy), q, counter);
      boolean _equals = Objects.equals(policy, "ignore");
      if (_equals) {
        Code.Expr _get = result.args().get("params");
        result.args().put("skipped", Code.num(((Code.Items) _get).items().indexOf(records)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Column values for tenant, namespace and partition of a write; partitioned tables default to the default partition.
   */
  private Map<String, Code.Expr> slices(final EObject statement, final VectorCollection collection, final OracledbSql.Table table) {
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
          String _defaultPartition = OracledbSchema.defaultPartition(collection);
          _elvis_1 = _defaultPartition;
        }
        if (_elvis_1 != null) {
          _elvis = _elvis_1;
        } else {
          _elvis = OracledbSchema.DEFAULT_PARTITION;
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
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final OracledbSql.Table table = this.schema.table(it.getCollection());
      final OracledbSql.Query q = new OracledbSql.Query();
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if (((((it.getValues() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty()))) {
          throw Unsupported.of("update from records with \'set\', remove-fields, array-append or array-remove; each record carries its own values");
        }
        boolean _written = ModelUtil.written(it, "ifVersion");
        if (_written) {
          throw Unsupported.of("if-version with an update from records; one version cannot guard records that each carry their own values");
        }
        final Code.Expr records = this.rows(it.getSource(), table);
        final List<String> restrictions = CollectionLiterals.<String>newArrayList();
        boolean _isEmpty = it.getIds().isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          String _keyIn = OracledbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
          restrictions.add(_keyIn);
        }
        String _where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
        restrictions.add(_where);
        final String fromRecords = OracledbSql.updateRows(q, table, records, (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE)), OracledbSql.and(restrictions));
        Code.Call _modify = this.modify(fromRecords, q, "update_count");
        Code.Return _return = new Code.Return(_modify);
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
            final OracledbSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
            final String name = column.name();
            given.add(name);
            boolean _contains = it.getAppendFields().contains(entry.getField());
            if (_contains) {
              this.arrayField(entry.getField(), column);
              String _id = OracledbSql.id(name);
              String _plus = (_id + " = ");
              String _arrayMerge = this.arrayMerge(q, column, this.convert(entry.getValue(), column), false);
              String _plus_1 = (_plus + _arrayMerge);
              sets.add(_plus_1);
            } else {
              boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
              if (_contains_1) {
                this.arrayField(entry.getField(), column);
                String _id_1 = OracledbSql.id(name);
                String _plus_2 = (_id_1 + " = ");
                String _arrayMerge_1 = this.arrayMerge(q, column, this.convert(entry.getValue(), column), true);
                String _plus_3 = (_plus_2 + _arrayMerge_1);
                sets.add(_plus_3);
              } else {
                String _id_2 = OracledbSql.id(name);
                String _plus_4 = (_id_2 + " = ");
                String _value = OracledbSql.value(q, column, this.convert(entry.getValue(), column));
                String _plus_5 = (_plus_4 + _value);
                sets.add(_plus_5);
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
          final OracledbSql.Column column = table.column(ModelUtil.fieldName(field_1));
          if (((column.vec() != null) && (!column.nullable()))) {
            String _name = column.name();
            String _plus_3 = ("remove-fields on vector field " + _name);
            String _plus_4 = (_plus_3 + ", which is not nullable");
            throw Unsupported.of(_plus_4);
          }
          String _id = OracledbSql.id(column.name());
          String _plus_5 = (_id + " = NULL");
          sets.add(_plus_5);
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        List<OracledbSql.Column> _columns = table.columns();
        for (final OracledbSql.Column column : _columns) {
          if (((((!Objects.equals(column.name(), table.key())) && (column.vec() == null)) && (!given.contains(column.name()))) && (!IterableExtensions.<Field>exists(it.getRemovedFields(), ((Function1<Field, Boolean>) (Field f) -> {
            String _name = table.column(ModelUtil.fieldName(f)).name();
            String _name_1 = column.name();
            return Boolean.valueOf(Objects.equals(_name, _name_1));
          }))))) {
            String _id = OracledbSql.id(column.name());
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
        String _keyIn_1 = OracledbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function_1)));
        conditions.add(_keyIn_1);
      }
      String _where_1 = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
      conditions.add(_where_1);
      boolean _written_1 = ModelUtil.written(it, "ifVersion");
      if (_written_1) {
        String _bind = q.bind(Code.num(it.getIfVersion()), "number");
        String _plus_4 = ("ORA_ROWSCN = " + _bind);
        conditions.add(_plus_4);
      }
      String _table = OracledbSql.table(table);
      String _plus_5 = ("UPDATE " + _table);
      String _plus_6 = (_plus_5 + " ");
      String _plus_7 = (_plus_6 + OracledbSql.ALIAS);
      String _plus_8 = (_plus_7 + " SET ");
      String _join = IterableExtensions.join(sets, ", ");
      String _plus_9 = (_plus_8 + _join);
      String _whereClause = OracledbSql.whereClause(OracledbSql.and(conditions));
      final String update = (_plus_9 + _whereClause);
      Code.Call _modify_1 = this.modify(update, q, "update_count");
      Code.Return _return_1 = new Code.Return(_modify_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  /**
   * The array of a JSON array column after appending the items (in order) or removing every element equal to one of them: the
   * elements are read with JSON_TABLE and aggregated again with JSON_ARRAYAGG in their order.
   */
  private String arrayMerge(final OracledbSql.Query q, final OracledbSql.Column column, final Code.Expr itemsValue, final boolean remove) {
    String _xblockexpression = null;
    {
      String _xifexpression = null;
      String _element = column.element();
      boolean _equals = Objects.equals(_element, "number");
      if (_equals) {
        _xifexpression = "NUMBER";
      } else {
        String _xifexpression_1 = null;
        String _element_1 = column.element();
        boolean _equals_1 = Objects.equals(_element_1, "boolean");
        if (_equals_1) {
          _xifexpression_1 = "BOOLEAN";
        } else {
          _xifexpression_1 = "VARCHAR2(4000)";
        }
        _xifexpression = _xifexpression_1;
      }
      final String type = _xifexpression;
      String _col = OracledbSql.col(column.name());
      String _plus = ("SELECT p, v FROM JSON_TABLE(" + _col);
      String _plus_1 = (_plus + ", \'$[*]\' COLUMNS (p FOR ORDINALITY, v ");
      String _plus_2 = (_plus_1 + type);
      final String stored = (_plus_2 + " PATH \'$\'))");
      String _json = q.json(itemsValue);
      String _plus_3 = ("JSON_TABLE(" + _json);
      String _plus_4 = (_plus_3 + ", \'$[*]\' COLUMNS (p FOR ORDINALITY, v ");
      String _plus_5 = (_plus_4 + type);
      final String given = (_plus_5 + " PATH \'$\'))");
      String _xifexpression_2 = null;
      if (remove) {
        _xifexpression_2 = (((("SELECT 1 AS o, s.p, s.v FROM (" + stored) + ") s WHERE s.v NOT IN (SELECT g.v FROM ") + given) + " g WHERE g.v IS NOT NULL)");
      } else {
        _xifexpression_2 = (((("SELECT 1 AS o, s.p, s.v FROM (" + stored) + ") s UNION ALL SELECT 2 AS o, g.p, g.v FROM ") + given) + " g");
      }
      final String merged = _xifexpression_2;
      _xblockexpression = (("NVL((SELECT JSON_ARRAYAGG(m.v ORDER BY m.o, m.p RETURNING CLOB) FROM (" + merged) + ") m), \'[]\')");
    }
    return _xblockexpression;
  }

  private void arrayField(final Field field, final OracledbSql.Column column) {
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
      final OracledbSql.Table table = this.schema.table(it.getCollection());
      final OracledbSql.Query q = new OracledbSql.Query();
      final List<String> conditions = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        String _keyIn = OracledbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
        conditions.add(_keyIn);
      }
      String _where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), true).parts());
      conditions.add(_where);
      final String condition = OracledbSql.and(conditions);
      if (((condition == null) && (!it.isDeleteAll()))) {
        throw Unsupported.of("a delete without ids, filter or \'all\'");
      }
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        String _table = OracledbSql.table(table);
        String _plus = ((((OracledbSql.ALIAS + ".ROWID IN (SELECT ") + OracledbSql.ALIAS) + ".ROWID FROM ") + _table);
        String _plus_1 = (_plus + " ");
        String _plus_2 = (_plus_1 + OracledbSql.ALIAS);
        String _whereClause = OracledbSql.whereClause(condition);
        String _plus_3 = (_plus_2 + _whereClause);
        String _plus_4 = (_plus_3 + " ORDER BY ");
        String _col = OracledbSql.col(table.key());
        String _plus_5 = (_plus_4 + _col);
        String _plus_6 = (_plus_5 + " FETCH FIRST ");
        int _deleteLimit = it.getDeleteLimit();
        String _plus_7 = (_plus_6 + Integer.valueOf(_deleteLimit));
        _xifexpression = (_plus_7 + " ROWS ONLY)");
      } else {
        _xifexpression = condition;
      }
      final String target = _xifexpression;
      String _table_1 = OracledbSql.table(table);
      String _plus_8 = ("DELETE FROM " + _table_1);
      String _plus_9 = (_plus_8 + " ");
      String _plus_10 = (_plus_9 + OracledbSql.ALIAS);
      String _whereClause_1 = OracledbSql.whereClause(target);
      final String delete = (_plus_10 + _whereClause_1);
      Code.Call _modify = this.modify(delete, q, "delete_count");
      Code.Return _return = new Code.Return(_modify);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The runtime reads a local JSON or JSON Lines file and writes it in batches with one statement (the batch is the JSON array
   * parameter at position rows), all in one transaction. SQL*Loader and external tables would read files on the server, not the client.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Oracle has no ingestion pipeline a generated program sets up (GoldenGate is a separate product)");
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
        throw Unsupported.of((("import from " + scheme) + "; has no Oracle mapping yet (DBMS_CLOUD reads object stores with a credential)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + OracledbStatements.PENDING) + " (CSV records read by the runtimes)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; has no Oracle mapping yet (external tables read ORACLE_LOADER and ORACLE_DATAPUMP files, and parquet through ORACLE_BIGDATA)");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async");
      final OracledbSql.Table table = this.schema.table(it.getCollection());
      final OracledbSql.Query q = new OracledbSql.Query();
      String _xifexpression_1 = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression_1 = it.getOnConflict().getLiteral();
      } else {
        _xifexpression_1 = "error";
      }
      final String policy = _xifexpression_1;
      final Code.Nil batch = new Code.Nil();
      String _insertRows = OracledbSql.insertRows(q, table, batch, this.slices(it, it.getCollection(), table), policy);
      String _xifexpression_2 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_2 = "insert_count";
      } else {
        _xifexpression_2 = "upsert_count";
      }
      final Code.Call statement = this.modify(_insertRows, q, _xifexpression_2);
      final Code.Entries args = OracledbSchema.kw("sql", statement.args().get("sql"), "location", Code.text(it.getLocation()));
      Code.Expr _get = statement.args().get("params");
      final Code.Items params = ((Code.Items) _get);
      args.put("params", params);
      args.put("rows", Code.num(params.items().indexOf(batch)));
      args.put("counter", statement.args().get("counter"));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
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
   * The statements of a vecDSL transaction in one Oracle transaction: every step runs, then one COMMIT; any failure rolls back
   * all of them. Read committed is Oracle's default; serializable (and repeatable read and snapshot, which Oracle's serializable
   * level, a snapshot of the transaction start, covers) is set with SET TRANSACTION.
   */
  protected List<Code.Stmt> _body(final Transaction it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      EList<DmlMethod> _methods = it.getMethods();
      for (final DmlMethod statement : _methods) {
        {
          if ((statement instanceof BulkImport)) {
            throw Unsupported.of((("bulk-import inside a transaction; " + OracledbStatements.PENDING) + " (the import as one step of the transaction)"));
          }
          final List<Code.Stmt> inner = this.body(statement);
          if (((inner.size() != 1) || (!(IterableExtensions.<Code.Stmt>head(inner) instanceof Code.Return)))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          Code.Stmt _head = IterableExtensions.<Code.Stmt>head(inner);
          Code.Expr _value = ((Code.Return) _head).value();
          final Code.Call invocation = ((Code.Call) _value);
          boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("modify", "query", "value")).contains(invocation.op());
          boolean _not = (!_contains);
          if (_not) {
            throw Unsupported.of("this statement inside a transaction");
          }
          final Code.Entries step = OracledbSchema.kw("op", Code.text(invocation.op()), "sql", invocation.args().get("sql"));
          for (final String key : Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("params", "counter", "skipped"))) {
            boolean _has = invocation.args().has(key);
            if (_has) {
              step.put(key, invocation.args().get(key));
            }
          }
          steps.add(step);
        }
      }
      final Code.Entries args = OracledbSchema.kw("steps", Code.items(steps));
      boolean _written = ModelUtil.written(it, "isolationLevel");
      if (_written) {
        final String level = it.getIsolationLevel().getLiteral();
        boolean _equals = Objects.equals(level, "read_uncommitted");
        if (_equals) {
          this.notes.add("read_uncommitted on Transaction, which Oracle runs as read committed");
        }
        String _xifexpression = null;
        if ((Objects.equals(level, "read_uncommitted") || Objects.equals(level, "read_committed"))) {
          _xifexpression = "read_committed";
        } else {
          _xifexpression = "serializable";
        }
        args.put("isolation", Code.text(_xifexpression));
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
    Set<String> _keySet = ModelUtil.extParams(statement, OracledbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * A statement's filter with its tenant, namespace and partition conditions.
   */
  private OracledbFilter filterOf(final EObject statement, final VectorCollection collection, final OracledbSql.Table table, final FilterBlock block, final boolean write) {
    OracledbFilter _xblockexpression = null;
    {
      final OracledbFilter result = new OracledbFilter().where(block);
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
   * Loading into the In-Memory column store at high priority, so that the database populates the table without waiting for a full
   * scan (In-Memory Guide, Enabling Objects for In-Memory Population); the store needs INMEMORY_SIZE above 0, an instance
   * parameter the setup script sets.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _execute = OracledbSchema.execute(this.inMemory(this.schema.table(it.getCollection()), it.getPartitions(), "INMEMORY PRIORITY HIGH"));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * NO INMEMORY evicts a populated table, as every change of an INMEMORY attribute other than PRIORITY does.
   */
  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _execute = OracledbSchema.execute(this.inMemory(this.schema.table(it.getCollection()), it.getPartitions(), "NO INMEMORY"));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * ALTER TABLE with an INMEMORY clause for the table, or for each named partition (MODIFY PARTITION).
   */
  private List<String> inMemory(final OracledbSql.Table table, final List<Partition> named, final String clause) {
    List<String> _xblockexpression = null;
    {
      boolean _isEmpty = named.isEmpty();
      if (_isEmpty) {
        String _table = OracledbSql.table(table);
        String _plus = ("ALTER TABLE " + _table);
        String _plus_1 = (_plus + " ");
        String _plus_2 = (_plus_1 + clause);
        return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2));
      }
      String _partition = table.partition();
      boolean _tripleEquals = (_partition == null);
      if (_tripleEquals) {
        throw Unsupported.of("partitions of a collection declared without partitions");
      }
      final Function1<Partition, String> _function = (Partition p) -> {
        String _table_1 = OracledbSql.table(table);
        String _plus_3 = ("ALTER TABLE " + _table_1);
        String _plus_4 = (_plus_3 + " MODIFY PARTITION ");
        String _id = OracledbSql.id(p.getName());
        String _plus_5 = (_plus_4 + _id);
        String _plus_6 = (_plus_5 + " ");
        return (_plus_6 + clause);
      };
      _xblockexpression = IterableExtensions.<String>toList(ListExtensions.<Partition, String>map(named, _function));
    }
    return _xblockexpression;
  }

  /**
   * A commit is durable once the redo is written, so a flush only checks the table.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  /**
   * Online segment shrink (Administrator's Guide, Shrinking Database Segments Online): row movement first, then SHRINK SPACE on
   * the table, then SHRINK SPACE on each non-partitioned B-tree index of it. Not SHRINK SPACE CASCADE: on 23.26 it fails with
   * ORA-51926 (cannot alter an existing vector index) when the table has a vector index (an IVF index
   * included); the table alone shrinks. Vector, Oracle Text (domain) and LOB indexes are left as they are. Indexes are read from
   * USER_INDEXES at run time because a vector index can come from a later create vector-index statement. Tables with
   * function-based indexes cannot be shrunk; the generator writes one for every payload index on a sub-field of a JSON field.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "targetSegments", "async");
      final Function1<PayloadIndex, Boolean> _function = (PayloadIndex index) -> {
        return Boolean.valueOf(((index.getTarget().getRawPath() == null) && (!index.getTarget().getSubFields().isEmpty())));
      };
      boolean _exists = IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(it.getCollection().getIndexes(), PayloadIndex.class), _function);
      if (_exists) {
        throw Unsupported.of("compacting a collection with a payload index on a sub-field; that index is function-based, and Oracle cannot shrink tables with function-based indexes (Administrator\'s Guide, Shrinking Database Segments Online)");
      }
      final OracledbSql.Table target = this.schema.table(it.getCollection());
      final String table = OracledbSql.table(target);
      String _literal = OracledbSql.literal(target.name());
      String _plus = ("BEGIN FOR r IN (SELECT index_name FROM user_indexes WHERE table_name = " + _literal);
      final String indexes = (_plus + " AND index_type = \'NORMAL\' AND partitioned = \'NO\') LOOP EXECUTE IMMEDIATE \'ALTER INDEX \"\' || r.index_name || \'\" SHRINK SPACE\'; END LOOP; END;");
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList((("ALTER TABLE " + table) + " ENABLE ROW MOVEMENT"), (("ALTER TABLE " + table) + " SHRINK SPACE"), indexes)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(it.getCollection());
      final OracledbSql.Query q = new OracledbSql.Query();
      Code.Call _call = OracledbSchema.call("value", OracledbQueries.describe(q, table, it.isIncludeStats(), it.isIncludeIndexProgress()), q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + OracledbStatements.NO_MODELS));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(OracledbStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(OracledbStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(OracledbStatements.NO_SNAPSHOTS);
  }

  /**
   * CREATE TABLE ... AS SELECT copies the columns and rows; the primary key is added after it. Indexes, comments and views are not
   * copied (DBMS_METADATA would have to rewrite their names).
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; a vecDSL database block is the server connection (PDB cloning needs CREATE PLUGGABLE DATABASE)");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; Oracle has no copy-on-write tables in the Free edition (snapshot copies of PDBs need a copy-on-write file system)");
      }
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final OracledbSql.Table table = this.schema.table(it.getSourceCollection());
      final String target = OracledbSql.id(OracledbSchema.physical(table.database(), it.getTargetName()));
      String _name = table.name();
      String _plus = ("the indexes, comment and aliases of " + _name);
      String _plus_1 = (_plus + ", which the clone does not copy");
      this.notes.add(_plus_1);
      String _xifexpression = null;
      Literal _asOf = it.getAsOf();
      boolean _tripleEquals = (_asOf == null);
      if (_tripleEquals) {
        _xifexpression = OracledbSql.table(table);
      } else {
        String _table = OracledbSql.table(table);
        String _constant = this.past(table, it.getAsOf()).flashback().constant();
        _xifexpression = (_table + _constant);
      }
      final String copied = _xifexpression;
      String _id = OracledbSql.id(table.key());
      String _plus_2 = ((("ALTER TABLE " + target) + " ADD PRIMARY KEY (") + _id);
      String _plus_3 = (_plus_2 + ")");
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(((("CREATE TABLE " + target) + " AS SELECT * FROM ") + copied), _plus_3)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * ALTER INDEX ... REBUILD; a vector index is rebuilt by dropping and creating it, which the generator does not do yet.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Index _index = it.getIndex();
      if ((_index instanceof VectorIndex)) {
        throw Unsupported.of((("rebuilding a vector index; " + OracledbStatements.PENDING) + " (DBMS_VECTOR.REBUILD_INDEX)"));
      }
      Index _index_1 = it.getIndex();
      final PayloadIndex payload = ((PayloadIndex) _index_1);
      final String name_1 = OracledbSql.id(this.indexName(it.getIndex()));
      String _xifexpression = null;
      if ((Objects.equals(payload.getKind(), EPayloadIndexKind.FULL_TEXT) || Objects.equals(payload.getKind(), EPayloadIndexKind.JSON))) {
        _xifexpression = (("ALTER INDEX " + name_1) + " REBUILD");
      } else {
        String _xifexpression_1 = null;
        boolean _isConcurrently = it.isConcurrently();
        if (_isConcurrently) {
          _xifexpression_1 = " ONLINE";
        } else {
          _xifexpression_1 = "";
        }
        _xifexpression = ((("ALTER INDEX " + name_1) + " REBUILD") + _xifexpression_1);
      }
      final String sql = _xifexpression;
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(OracledbStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Table table = this.schema.table(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        return this.dropSlice(it.getCollection(), table.namespace(), it.getNamespace());
      }
      String _table = OracledbSql.table(table);
      String _plus = ("TRUNCATE TABLE " + _table);
      Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final OracledbSql.Query q = new OracledbSql.Query();
      final Concept owner = it.getScope();
      String _switchResult = null;
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            String _xifexpression = null;
            if ((owner instanceof Database)) {
              _xifexpression = OracledbQueries.collections(q, ((Database) owner).getName());
            } else {
              String _xifexpression_1 = null;
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing collections in a " + _name);
                String _plus_1 = (_plus + "; Oracle lists the tables of a schema");
                throw Unsupported.of(_plus_1);
              } else {
                _xifexpression_1 = OracledbQueries.collections();
              }
              _xifexpression = _xifexpression_1;
            }
            _switchResult = _xifexpression;
            break;
          case DATABASES:
            String _xblockexpression_1 = null;
            {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name_1 = owner.eClass().getName();
                String _plus_2 = ("listing databases in a " + _name_1);
                throw Unsupported.of(_plus_2);
              }
              _xblockexpression_1 = OracledbQueries.databases();
            }
            _switchResult = _xblockexpression_1;
            break;
          case INDEXES:
            String _xblockexpression_2 = null;
            {
              final OracledbSql.Table table = this.scopeTable(owner, "indexes");
              _xblockexpression_2 = OracledbQueries.indexes(q, table, table.name());
            }
            _switchResult = _xblockexpression_2;
            break;
          case PARTITIONS:
            _switchResult = OracledbQueries.partitions(q, this.scopeTable(owner, "partitions"));
            break;
          case ALIASES:
            OracledbSql.Table _xifexpression_2 = null;
            if ((owner == null)) {
              _xifexpression_2 = null;
            } else {
              _xifexpression_2 = this.scopeTable(owner, "aliases");
            }
            _switchResult = OracledbQueries.aliases(q, _xifexpression_2);
            break;
          case TENANTS:
          case NAMESPACES:
            String _xblockexpression_3 = null;
            {
              final OracledbSql.Table table = this.scopeTable(owner, it.getKind().getLiteral());
              String _xifexpression_3 = null;
              String _literal = it.getKind().getLiteral();
              boolean _equals = Objects.equals(_literal, "tenants");
              if (_equals) {
                _xifexpression_3 = table.tenant();
              } else {
                _xifexpression_3 = table.namespace();
              }
              final String column = _xifexpression_3;
              if ((column == null)) {
                String _literal_1 = it.getKind().getLiteral();
                String _plus_2 = ("listing " + _literal_1);
                String _plus_3 = (_plus_2 + " of a collection without ");
                String _literal_2 = it.getKind().getLiteral();
                String _plus_4 = (_plus_3 + _literal_2);
                throw Unsupported.of(_plus_4);
              }
              _xblockexpression_3 = OracledbQueries.slices(table, column);
            }
            _switchResult = _xblockexpression_3;
            break;
          case SNAPSHOTS:
            throw Unsupported.of(OracledbStatements.NO_SNAPSHOTS);
          default:
            break;
        }
      }
      final String source = _switchResult;
      Code.Text _xifexpression_3 = null;
      String _prefix = it.getPrefix();
      boolean _tripleEquals = (_prefix == null);
      if (_tripleEquals) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = Code.text(it.getPrefix());
      }
      Integer _xifexpression_4 = null;
      boolean _written = ModelUtil.written(it, "listLimit");
      if (_written) {
        _xifexpression_4 = Integer.valueOf(it.getListLimit());
      } else {
        _xifexpression_4 = null;
      }
      final String sql = OracledbQueries.names(q, source, _xifexpression_3, _xifexpression_4);
      Code.Call _call = OracledbSchema.call("value", sql, q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private OracledbSql.Table scopeTable(final Concept owner, final String what) {
    OracledbSql.Table _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.table(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * CREATE INDEX returns when the index is built (CONTEXT indexes then sync on commit), so awaiting one reads its state once.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state", "timeoutSeconds");
      final OracledbSql.Query q = new OracledbSql.Query();
      Code.Call _call = OracledbSchema.call("value", OracledbQueries.indexState(q, this.indexName(it.getIndex())), q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "elementScope", "explain", "reranker", "queryModel");
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression = it.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      final OracledbSql.Query q = new OracledbSql.Query();
      final QueryInput searched = it.getInput();
      OracledbSql.Query _query = new OracledbSql.Query();
      this.queryVector(_query, searched, it.getCollection(), table, column);
      final List<String> stages = CollectionLiterals.<String>newArrayList();
      final List<String> staged = this.prefetchStages(it, q, it.getCollection(), table, it.getPrefetches(), it.getInput(), field, stages);
      String _where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      String _within = OracledbQueries.within(staged);
      final String where = OracledbSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_where, _within)));
      final OracledbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field, Collections.<OracledbQueries.Terms>unmodifiableList(CollectionLiterals.<OracledbQueries.Terms>newArrayList()));
      final OracledbQueries.Fetch fetch = this.fetchOf(it.getCollection(), field, column, metric, it.getParams(), it.isExact());
      String _xifexpression_1 = null;
      boolean _isEmpty = stages.isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = IterableExtensions.join(stages, ", ");
      }
      final String prelude = _xifexpression_1;
      final Supplier<String> _function = () -> {
        return this.queryVector(q, searched, it.getCollection(), table, column);
      };
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      OracledbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final OracledbQueries.Search search = new OracledbQueries.Search(table, column, metric, _function, prelude, _topK, _offsetOf, where, _projection, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf, fetch);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = OracledbSchema.call(_xifexpression_2, OracledbQueries.vectorSearch(q, search, shape), q, this.merged(this.timeout(it.getParams()), this.extensionSettings(it)));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "explain", "reranker", "minShouldMatch");
      this.queryExtensions(it);
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "targetRecall", "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
      final List<OracledbQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, it.getSearchFields());
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
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      final boolean fuzzy = this.fuzzyOf(it, op);
      final Integer span = this.spanOf(it, op);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      OracledbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final OracledbQueries.Keywords keywords = new OracledbQueries.Keywords(table, fields, op, words, _topK, _offsetOf, where, _projection, _minOf, _maxOf, fuzzy, span);
      OracledbQueries.Terms _terms = new OracledbQueries.Terms(op, words, fuzzy, span);
      final OracledbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null, Collections.<OracledbQueries.Terms>unmodifiableList(CollectionLiterals.<OracledbQueries.Terms>newArrayList(_terms)));
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = OracledbSchema.call(_xifexpression_2, OracledbQueries.keywordSearch(q, keywords, shape), q, this.merged(this.timeout(it.getParams()), this.extensionSettings(it)));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = this.highlighting(shape, _return);
    }
    return _xblockexpression;
  }

  /**
   * Hybrid search fused in SQL: each leg ranks its candidates in a CTE, and the fusion function of vecDSL combines them.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "explain", "reranker");
      this.queryExtensions(it);
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final List<OracledbQueries.Terms> terms = CollectionLiterals.<OracledbQueries.Terms>newArrayList();
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
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
      final List<OracledbQueries.Leg> parts = CollectionLiterals.<OracledbQueries.Leg>newArrayList();
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
          final String legWhere = OracledbSql.where(q, table, this.filterOf(it, _collection, table, _xifexpression_2, false).parts());
          Set<String> _keySet = ModelUtil.extParams(leg, OracledbGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of((("extension parameter " + name) + " on a hybrid leg"));
          }
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
            boolean _tripleNotEquals = (_bounds != null);
            if (_tripleNotEquals) {
              throw Unsupported.of((("bounds on a hybrid leg; " + OracledbStatements.PENDING) + " (a score condition in the leg CTE)"));
            }
            final VectorField field = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
            final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
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
            final QueryInput legInput = _elvis;
            OracledbSql.Query _query = new OracledbSql.Query();
            this.queryVector(_query, legInput, it.getCollection(), table, column);
            final OracledbQueries.Fetch fetch = this.fetchOf(it.getCollection(), field, column, metric, ((VectorLeg)leg).getParams(), ((VectorLeg)leg).isExact());
            SearchParams _params = ((VectorLeg)leg).getParams();
            boolean _tripleNotEquals_1 = (_params != null);
            if (_tripleNotEquals_1) {
              options.putAll(this.timeout(((VectorLeg)leg).getParams()));
            }
            final Supplier<String> _function = () -> {
              return this.queryVector(q, legInput, it.getCollection(), table, column);
            };
            OracledbQueries.Leg _leg = new OracledbQueries.Leg(column, metric, _function, fetch, null, null, null, (limit).intValue(), legWhere, (weight).doubleValue());
            parts.add(_leg);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              boolean _written_2 = ModelUtil.written(leg, "minShouldMatch");
              if (_written_2) {
                String _get = OracledbStatements.REASONS.get("minShouldMatch");
                String _plus = ("the \'min-should-match\' clause; " + _get);
                throw Unsupported.of(_plus);
              }
              final List<OracledbQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, ((KeywordLeg)leg).getSearchFields());
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
              OracledbQueries.Terms _terms = new OracledbQueries.Terms(op, legText, false, null);
              terms.add(_terms);
              OracledbQueries.Leg _leg = new OracledbQueries.Leg(null, null, null, null, fields, op, legText, (limit).intValue(), legWhere, (weight).doubleValue());
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
      OracledbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final OracledbQueries.Hybrid hybrid = new OracledbQueries.Hybrid(table, parts, fusionKind, k, null, _topK_1, _offsetOf_1, _projection, _minOf, _maxOf);
      options.putAll(this.extensionSettings(it));
      final OracledbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null, terms);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = OracledbSchema.call(_xifexpression_2, OracledbQueries.hybridSearch(q, hybrid, shape), q, options);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = this.highlighting(shape, _return);
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
    throw Unsupported.of((("a filter on both the hybrid search and a leg; " + OracledbStatements.PENDING) + " (the conjunction of both)"));
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
      this.reject(it, "explain", "reranker");
      this.queryExtensions(it);
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final OracledbSql.Table lookup = this.past(this.schema.table(_elvis), it.getAsOf());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
      final OracledbSql.Column lookupColumn = lookup.column(column.name());
      final OracledbSql.Query q = new OracledbSql.Query();
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
      final String prelude = OracledbQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors);
      final List<OracledbSql.Cond> conditions = CollectionLiterals.<OracledbSql.Cond>newArrayList();
      List<OracledbSql.Cond> _parts = this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts();
      Iterables.<OracledbSql.Cond>addAll(conditions, _parts);
      final List<String> parts = CollectionLiterals.<String>newArrayList();
      String _where = OracledbSql.where(q, table, conditions);
      parts.add(_where);
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final List<Code.Expr> examples = CollectionLiterals.<Code.Expr>newArrayList();
        Iterables.<Code.Expr>addAll(examples, positiveIds);
        Iterables.<Code.Expr>addAll(examples, negativeIds);
        boolean _isEmpty_1 = examples.isEmpty();
        boolean _not = (!_isEmpty_1);
        if (_not) {
          String _notTrue = OracledbSql.notTrue(OracledbSql.keyIn(q, table, Code.items(examples)));
          parts.add(_notTrue);
        }
      }
      final String where = OracledbSql.and(parts);
      final OracledbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field, Collections.<OracledbQueries.Terms>unmodifiableList(CollectionLiterals.<OracledbQueries.Terms>newArrayList()));
      final OracledbQueries.Fetch fetch = this.fetchOf(it.getCollection(), field, column, column.vec().metric(), it.getParams(), false);
      String _metric = column.vec().metric();
      final Supplier<String> _function = () -> {
        return "(SELECT v FROM q)";
      };
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      OracledbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final OracledbQueries.Search search = new OracledbQueries.Search(table, column, _metric, _function, prelude, _topK, _offsetOf, where, _projection, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf, fetch);
      String _xifexpression = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression = "value";
      } else {
        _xifexpression = "query";
      }
      Code.Call _call = OracledbSchema.call(_xifexpression, OracledbQueries.vectorSearch(q, search, shape), q, this.merged(this.timeout(it.getParams()), this.extensionSettings(it)));
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
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
      final OracledbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, null, false).parts());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      Code.Call _call = OracledbSchema.call("query", OracledbQueries.getRows(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), names, where), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      Code.Call _call = OracledbSchema.call("value", OracledbQueries.count(q, table, where), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
      final OracledbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      final List<OracledbQueries.Order> order = CollectionLiterals.<OracledbQueries.Order>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        {
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
          }
          OracledbQueries.Ref _fieldRefOf = this.fieldRefOf(it.getCollection(), table, clause.getField(), "sort");
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          OracledbQueries.Order _order = new OracledbQueries.Order(_fieldRefOf, _equals);
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
        final Function1<OracledbQueries.Order, String> _function = (OracledbQueries.Order o) -> {
          String _sql = o.field().sql(OracledbSql.ALIAS);
          String _xifexpression_1 = null;
          boolean _descending = o.descending();
          if (_descending) {
            _xifexpression_1 = " DESC NULLS LAST";
          } else {
            _xifexpression_1 = " ASC NULLS LAST";
          }
          return (_sql + _xifexpression_1);
        };
        final List<String> terms = IterableExtensions.<String>toList(ListExtensions.<OracledbQueries.Order, String>map(order, _function));
        Code.Call _call = OracledbSchema.call("query", OracledbQueries.scroll(q, table, names, where, terms, limit, this.offsetOf(it)), q, this.extensionSettings(it));
        Code.Return _return = new Code.Return(_call);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
      }
      final boolean sliced = ModelUtil.written(it, "sliceTotal");
      if ((sliced && (((it.getSliceTotal() < 1) || (it.getSliceIndex() < 0)) || (it.getSliceIndex() >= it.getSliceTotal())))) {
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
      int _offsetOf = this.offsetOf(it);
      Integer _xifexpression_2 = null;
      if (sliced) {
        _xifexpression_2 = Integer.valueOf(it.getSliceIndex());
      } else {
        _xifexpression_2 = null;
      }
      Integer _xifexpression_3 = null;
      if (sliced) {
        _xifexpression_3 = Integer.valueOf(it.getSliceTotal());
      } else {
        _xifexpression_3 = null;
      }
      Code.Call _call_1 = OracledbSchema.call("query", OracledbQueries.scroll(q, table, names, where, order, limit, _offsetOf, cursorKey, _xifexpression_2, _xifexpression_3), q, this.extensionSettings(it));
      Code.Return _return_1 = new Code.Return(_call_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final OracledbSql.Query q = new OracledbSql.Query();
      final OracledbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      Code.Call _call = OracledbSchema.call("query", OracledbQueries.sample(q, table, shape.output(), where, it.getSampleSize(), first), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final OracledbSql.Table table = this.past(this.schema.table(it.getCollection()), it.getAsOf());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
      final OracledbSql.Query q = new OracledbSql.Query();
      final String where = OracledbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter(), false).parts());
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
      Code.Call _call = OracledbSchema.call("query", OracledbQueries.distanceMatrix(q, table, column, column.vec().metric(), where, sample, limit), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + OracledbStatements.NO_GENERATION));
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
    String _plus = (OracledbStatements.NOT_MAPPED + _name);
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
   * Output fields (the key is always returned) and the vector fields returned with 'with-vectors' or named in the output.
   */
  private OracledbQueries.Projection projection(final VectorCollection collection, final OracledbSql.Table table, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    OracledbQueries.Projection _xblockexpression = null;
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
      _xblockexpression = new OracledbQueries.Projection(fields, vectors);
    }
    return _xblockexpression;
  }

  /**
   * How a vector search fetches: approximately through the field's HNSW or IVF index when there is one, the search is not exact
   * and its metric is the index's (VECTOR_DISTANCE with another metric runs an exact search, SQL Language Reference); the target
   * accuracy is ceil(100 q) of target-recall (recall 1 is an exact fetch, as the capability manifest records), else EFSEARCH of
   * the candidate pool (or the index's search-candidates) for HNSW and NEIGHBOR PARTITION PROBES for IVF.
   */
  private OracledbQueries.Fetch fetchOf(final VectorCollection collection, final VectorField field, final OracledbSql.Column column, final String metric, final SearchParams params, final boolean exact) {
    OracledbQueries.Fetch _xblockexpression = null;
    {
      final VectorIndex index = this.schema.indexOf(collection, field);
      final String family = column.vec().family();
      if ((params != null)) {
        this.schema.note(params, "scanBudget", "oversampling", "rescore");
        Set<String> _keySet = ModelUtil.extParams(params, OracledbGenerator.VENDOR).keySet();
        for (final String name : _keySet) {
          throw Unsupported.of(("extension parameter " + name));
        }
        if ((ModelUtil.written(params, "filterStrategy") && (!Objects.equals(params.getFilterStrategy().getLiteral(), "auto")))) {
          this.notes.add("filter-strategy on SearchParams, which the Oracle optimizer chooses");
        }
      }
      final boolean exhaustive = ((exact || ((params != null) && params.isExhaustive())) || (((params != null) && ModelUtil.written(params, "targetRecall")) && (params.getTargetRecall() >= 1.0)));
      if ((((family == null) || exhaustive) || (!Objects.equals(metric, column.vec().metric())))) {
        if ((params != null)) {
          this.schema.note(params, "targetRecall", "candidatePool", "probes");
        }
        return OracledbQueries.Fetch.EXACT;
      }
      if (((params != null) && ModelUtil.written(params, "targetRecall"))) {
        double _targetRecall = params.getTargetRecall();
        boolean _lessEqualsThan = (_targetRecall <= 0);
        if (_lessEqualsThan) {
          throw Unsupported.of("a target-recall of 0 or less");
        }
        int _accuracy = OracledbStatements.accuracy(params.getTargetRecall());
        return new OracledbQueries.Fetch(true, Integer.valueOf(_accuracy), null);
      }
      boolean _equals = Objects.equals(family, "hnsw");
      if (_equals) {
        if (((params != null) && ModelUtil.written(params, "probes"))) {
          throw Unsupported.of("probes on an hnsw index; neighbor partition probes belong to ivf indexes");
        }
        int _xifexpression = (int) 0;
        if (((params != null) && ModelUtil.written(params, "candidatePool"))) {
          _xifexpression = params.getCandidatePool();
        } else {
          int _xifexpression_1 = (int) 0;
          boolean _written = ModelUtil.written(index, "searchCandidates");
          if (_written) {
            _xifexpression_1 = index.getSearchCandidates();
          } else {
            _xifexpression_1 = 0;
          }
          _xifexpression = _xifexpression_1;
        }
        final int candidates = _xifexpression;
        String _xifexpression_2 = null;
        if ((candidates > 0)) {
          _xifexpression_2 = ("EFSEARCH " + Integer.valueOf(candidates));
        } else {
          _xifexpression_2 = null;
        }
        return new OracledbQueries.Fetch(true, null, _xifexpression_2);
      }
      if (((params != null) && ModelUtil.written(params, "candidatePool"))) {
        throw Unsupported.of("candidates on an ivf index; candidate lists belong to hnsw indexes");
      }
      String _xifexpression_3 = null;
      if (((params != null) && ModelUtil.written(params, "probes"))) {
        int _probes = params.getProbes();
        _xifexpression_3 = ("NEIGHBOR PARTITION PROBES " + Integer.valueOf(_probes));
      } else {
        _xifexpression_3 = null;
      }
      _xblockexpression = new OracledbQueries.Fetch(true, null, _xifexpression_3);
    }
    return _xblockexpression;
  }

  /**
   * The target accuracy of a recall: ceil(100 q), at most 100.
   */
  public static int accuracy(final double recall) {
    double _ceil = Math.ceil(BigDecimal.valueOf(recall).multiply(BigDecimal.valueOf(100)).doubleValue());
    return Math.min(100, ((int) _ceil));
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

  /**
   * The call timeout of a search, which the runtimes set on the connection for that call.
   */
  private Map<String, String> timeout(final SearchParams params) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      if (((params != null) && ModelUtil.written(params, "timeoutMillis"))) {
        result.put("call_timeout", String.valueOf(params.getTimeoutMillis()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Oracle extension blocks of queries carry no settings: every search parameter is part of the SQL text.
   */
  private Map<String, String> extensionSettings(final EObject host) {
    LinkedHashMap<String, String> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(host, OracledbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of((("extension parameter " + name) + "; Oracle search parameters are written in the SQL text, and the generator takes them from vecDSL clauses"));
      }
      _xblockexpression = CollectionLiterals.<String, String>newLinkedHashMap();
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses Oracle lacks, with their reason, and clauses not generated yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = OracledbStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = OracledbStatements.PENDING_CLAUSES.get(name);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + OracledbStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = OracledbStatements.REASONS.get(name);
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
   * Oracle extension blocks of queries: extension items of other kinds than parameters are rejected.
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
      boolean _equals = Objects.equals(_vendor, OracledbGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          if ((!(item instanceof ExtParam))) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on Oracle");
            throw Unsupported.of(_plus);
          }
        }
      }
    }
    this.extensionSettings(host);
  }

  /**
   * The query vector as SQL: a bound TO_VECTOR, or the stored vector of a record (a scalar subquery).
   */
  private String queryVector(final OracledbSql.Query q, final QueryInput input, final VectorCollection collection, final OracledbSql.Table table, final OracledbSql.Column column) {
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
              boolean _sparse = column.vec().sparse();
              boolean _notEquals = (_equals != _sparse);
              if (_notEquals) {
                String _literal = input.getKind().getLiteral();
                String _plus = (_literal + " input on ");
                String _xifexpression = null;
                boolean _sparse_1 = column.vec().sparse();
                if (_sparse_1) {
                  _xifexpression = "sparse";
                } else {
                  _xifexpression = "dense";
                }
                String _plus_1 = (_plus + _xifexpression);
                String _plus_2 = (_plus_1 + " field ");
                String _name = column.name();
                String _plus_3 = (_plus_2 + _name);
                throw Unsupported.of(_plus_3);
              }
              _xblockexpression_1 = q.vector(this.convert(input.getValue(), column), column.vec());
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
              final OracledbSql.Table source = this.schema.table(_elvis);
              final OracledbSql.Column stored = source.column(column.name());
              String _id = OracledbSql.id(stored.name());
              String _plus = ("(SELECT x." + _id);
              String _plus_1 = (_plus + " FROM ");
              String _table = OracledbSql.table(source);
              String _plus_2 = (_plus_1 + _table);
              String _plus_3 = (_plus_2 + " x WHERE x.");
              String _id_1 = OracledbSql.id(source.key());
              String _plus_4 = (_plus_3 + _id_1);
              String _plus_5 = (_plus_4 + " = ");
              String _bind = q.bind(Literals.of(input.getValue()), source.keyColumn().kind());
              String _plus_6 = (_plus_5 + _bind);
              _xblockexpression_2 = (_plus_6 + ")");
            }
            _switchResult = _xblockexpression_2;
            break;
          case TEXT:
            throw Unsupported.of(("text input" + OracledbStatements.NO_MODELS));
          case MULTI_VECTOR:
            throw Unsupported.of((("multi-vector input; " + OracledbStatements.PENDING) + " (rows of chunk vectors ranked with FETCH ... PARTITIONS BY)"));
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " input");
            String _plus_1 = (_plus + OracledbStatements.NO_MODELS);
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " input");
        String _plus_1 = (_plus + OracledbStatements.NO_MODELS);
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The table a read sees: as of a past SCN (a number) or a past time (a string, an ISO 8601 timestamp with offset), or the
   * current table without an 'as-of' clause.
   */
  private OracledbSql.Table past(final OracledbSql.Table table, final Literal value) {
    OracledbSql.Table _xblockexpression = null;
    {
      if ((value == null)) {
        return table;
      }
      EParamType _xifexpression = null;
      if ((value instanceof ParamRef)) {
        _xifexpression = ((ParamRef) value).getParam().getType();
      } else {
        _xifexpression = null;
      }
      final EParamType type = _xifexpression;
      final boolean scn = (((value instanceof NumberLit) || Objects.equals(type, EParamType.INT)) || Objects.equals(type, EParamType.FLOAT));
      if ((((!scn) && (!(value instanceof StringLit))) && (!Objects.equals(type, EParamType.TEXT)))) {
        throw Unsupported.of("an \'as-of\' value that is neither a number (an SCN) nor a string (a timestamp)");
      }
      Code.Expr _of = Literals.of(value);
      OracledbSql.Flashback _flashback = new OracledbSql.Flashback(_of, scn);
      _xblockexpression = table.at(_flashback);
    }
    return _xblockexpression;
  }

  /**
   * Fuzziness AUTO as Oracle Text's FUZZY with its default similarity score and expansion count; FUZZY takes a similarity score from
   * 1 to 80 and no edit distance (Text Reference, FUZZY), so numeric fuzziness has no Oracle counterpart.
   */
  private boolean fuzzyOf(final KeywordSearch search, final String op) {
    boolean _xblockexpression = false;
    {
      String _fuzziness = search.getFuzziness();
      boolean _tripleEquals = (_fuzziness == null);
      if (_tripleEquals) {
        return false;
      }
      String _upperCase = search.getFuzziness().toUpperCase();
      boolean _notEquals = (!Objects.equals(_upperCase, "AUTO"));
      if (_notEquals) {
        String _fuzziness_1 = search.getFuzziness();
        String _plus = ("fuzziness " + _fuzziness_1);
        String _plus_1 = (_plus + "; Oracle Text\'s FUZZY operator takes a similarity score from 1 to 80, not an edit distance, and its reference gives no conversion (fuzziness AUTO maps to FUZZY with its defaults)");
        throw Unsupported.of(_plus_1);
      }
      boolean _equals = Objects.equals(op, "phrase");
      if (_equals) {
        throw Unsupported.of("fuzziness with text-query phrase; Oracle Text\'s FUZZY operator expands single words");
      }
      _xblockexpression = true;
    }
    return _xblockexpression;
  }

  /**
   * Slop of a phrase as the max_span of NEAR with the words in order (Text Reference, NEAR: at most 100); slop 0 is the phrase.
   */
  private Integer spanOf(final KeywordSearch search, final String op) {
    Integer _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(search, "slop");
      boolean _not = (!_written);
      if (_not) {
        return null;
      }
      boolean _notEquals = (!Objects.equals(op, "phrase"));
      if (_notEquals) {
        throw Unsupported.of("slop without text-query phrase; slop is the distance allowed between the words of a phrase");
      }
      int _slop = search.getSlop();
      boolean _greaterThan = (_slop > 100);
      if (_greaterThan) {
        throw Unsupported.of("slop above 100; Oracle Text returns an error for a NEAR span greater than 100");
      }
      Integer _xifexpression = null;
      int _slop_1 = search.getSlop();
      boolean _equals = (_slop_1 == 0);
      if (_equals) {
        _xifexpression = null;
      } else {
        _xifexpression = Integer.valueOf(search.getSlop());
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * A search that highlights first makes CTX_DOC.SNIPPET read its text keys as ROWIDs in the session.
   */
  private List<Code.Stmt> highlighting(final OracledbQueries.Shape shape, final Code.Stmt search) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      boolean _isEmpty = shape.highlights().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        Code.Stmt _execute = OracledbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(OracledbQueries.ROWID_KEYS)));
        result.add(_execute);
      }
      result.add(search);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The result shape of a search: the keyset cursor, boosts, MMR, groups, total, aggregations, highlights of the text inputs and
   * sort keys. The searched vector field is null for keyword and hybrid searches; terms are empty for searches without text.
   */
  private OracledbQueries.Shape shapeOf(final EObject search, final VectorCollection collection, final OracledbSql.Table table, final VectorField searched, final List<OracledbQueries.Terms> terms) {
    OracledbQueries.Shape _xblockexpression = null;
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
      OracledbQueries.Group _xifexpression = null;
      if ((grouping == null)) {
        _xifexpression = null;
      } else {
        OracledbQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, grouping, "group-by");
        Integer _xifexpression_1 = null;
        boolean _written = ModelUtil.written(search, "groupSize");
        if (_written) {
          Object _feature_5 = this.feature(search, "groupSize");
          _xifexpression_1 = ((Integer) _feature_5);
        } else {
          _xifexpression_1 = Integer.valueOf(1);
        }
        _xifexpression = new OracledbQueries.Group(_fieldRefOf, (_xifexpression_1).intValue());
      }
      final OracledbQueries.Group group = _xifexpression;
      final List<OracledbQueries.Aggregate> aggregates = CollectionLiterals.<OracledbQueries.Aggregate>newArrayList();
      Object _elvis_1 = null;
      Object _feature_6 = this.feature(search, "aggregations");
      if (_feature_6 != null) {
        _elvis_1 = _feature_6;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      for (final AggregationClause clause : ((List<AggregationClause>) _elvis_1)) {
        OracledbQueries.Aggregate _aggregateOf = this.aggregateOf(collection, table, clause);
        aggregates.add(_aggregateOf);
      }
      final List<OracledbSql.Column> marked = CollectionLiterals.<OracledbSql.Column>newArrayList();
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
            throw Unsupported.of("the \'highlight\' clause on a search without text; CTX_DOC.SNIPPET marks the terms of a text query");
          }
          final OracledbSql.Column markedColumn = table.column(ModelUtil.fieldName(highlighted));
          boolean _text = markedColumn.text();
          boolean _not = (!_text);
          if (_not) {
            String _name = markedColumn.name();
            String _plus = ("highlighting " + _name);
            String _plus_1 = (_plus + ", which is not a full-text field");
            throw Unsupported.of(_plus_1);
          }
          marked.add(markedColumn);
        }
      }
      final List<OracledbQueries.Order> order = CollectionLiterals.<OracledbQueries.Order>newArrayList();
      for (final SortClause clause_1 : sortClauses) {
        OracledbQueries.Ref _xifexpression_2 = null;
        boolean _isByRelevance = clause_1.isByRelevance();
        if (_isByRelevance) {
          _xifexpression_2 = null;
        } else {
          _xifexpression_2 = this.fieldRefOf(collection, table, clause_1.getField(), "sort");
        }
        ESortDirection _direction = clause_1.getDirection();
        boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
        OracledbQueries.Order _order = new OracledbQueries.Order(_xifexpression_2, _equals);
        order.add(_order);
      }
      Code.Text _xifexpression_3 = null;
      if ((cursorText == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = Code.text(cursorText);
      }
      final Code.Text cursorKey = _xifexpression_3;
      OracledbQueries.Boost _xifexpression_4 = null;
      if ((boosting == null)) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = this.boostOf(collection, table, boosting);
      }
      OracledbQueries.Mmr _xifexpression_5 = null;
      if ((diversity == null)) {
        _xifexpression_5 = null;
      } else {
        Object _feature_8 = this.feature(search, "topK");
        int _offsetOf = this.offsetOf(search);
        int _plus = ((((Integer) _feature_8)).intValue() + _offsetOf);
        _xifexpression_5 = this.mmrOf(collection, table, diversity, searched, _plus);
      }
      boolean _written_1 = ModelUtil.written(search, "withTotal");
      _xblockexpression = new OracledbQueries.Shape(cursorKey, _xifexpression_4, _xifexpression_5, group, _written_1, aggregates, marked, terms, order);
    }
    return _xblockexpression;
  }

  /**
   * Prefetch stages as CTEs of the rows each stage retrieves (its own input, vector field, metric, filter and limit, ranking only
   * the rows its nested stages return); the names of the given stages, in order. A stage without input or field takes the search's.
   */
  private List<String> prefetchStages(final EObject search, final OracledbSql.Query q, final VectorCollection collection, final OracledbSql.Table table, final List<Prefetch> given, final QueryInput input, final VectorField field, final List<String> stages) {
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
          final OracledbSql.Column stageColumn = table.column(ModelUtil.fieldName(stageField));
          String _xifexpression_1 = null;
          boolean _written = ModelUtil.written(stage, "metric");
          if (_written) {
            _xifexpression_1 = stage.getMetric().getLiteral();
          } else {
            _xifexpression_1 = stageColumn.vec().metric();
          }
          final String stageMetric = _xifexpression_1;
          final String stageQuery = this.queryVector(q, stageInput, collection, table, stageColumn);
          final String stageWhere = OracledbSql.where(q, table, this.filterOf(search, collection, table, stage.getFilter(), false).parts());
          int _size = stages.size();
          final String name = ("stage_" + Integer.valueOf(_size));
          String _within = OracledbQueries.within(inner);
          String _stage = OracledbQueries.stage(q, name, table, stageColumn, stageMetric, stageQuery, OracledbSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(stageWhere, _within))), stage.getStageLimit());
          stages.add(_stage);
          names.add(name);
        }
      }
      _xblockexpression = names;
    }
    return _xblockexpression;
  }

  /**
   * A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a JSON column.
   */
  private OracledbQueries.Ref fieldRefOf(final VectorCollection collection, final OracledbSql.Table table, final FieldPath path, final String purpose) {
    OracledbQueries.Ref _xblockexpression = null;
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
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        return new OracledbQueries.Ref(column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null);
      }
      final Function1<Field, String> _function = (Field sub) -> {
        return ModelUtil.fieldName(sub);
      };
      List<String> _list = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      String _leafKind = OracledbFilter.leafKind(path);
      _xblockexpression = new OracledbQueries.Ref(column, _list, _leafKind);
    }
    return _xblockexpression;
  }

  /**
   * An aggregation over the matching records, named by its alias or kind_field as on the other targets.
   */
  private OracledbQueries.Aggregate aggregateOf(final VectorCollection collection, final OracledbSql.Table table, final AggregationClause clause) {
    OracledbQueries.Aggregate _xblockexpression = null;
    {
      final String kind = clause.getKind().getLiteral();
      final OracledbQueries.Ref source = this.fieldRefOf(collection, table, clause.getField(), "aggregations");
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
      OracledbQueries.Aggregate _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case "range":
            throw Unsupported.of("range aggregations; vecDSL gives no range bounds to count records in");
          case "histogram":
            OracledbQueries.Aggregate _xblockexpression_1 = null;
            {
              if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                throw Unsupported.of("a histogram without a numeric interval");
              }
              Code.Num _num = Code.num(Double.parseDouble(clause.getInterval()));
              _xblockexpression_1 = new OracledbQueries.Aggregate(name, kind, source, _num, size);
            }
            _switchResult = _xblockexpression_1;
            break;
          case "date_histogram":
            OracledbQueries.Aggregate _xblockexpression_2 = null;
            {
              String _interval = clause.getInterval();
              boolean _tripleEquals = (_interval == null);
              if (_tripleEquals) {
                throw Unsupported.of("a date histogram without an interval");
              }
              Code.Text _text = Code.text(clause.getInterval());
              _xblockexpression_2 = new OracledbQueries.Aggregate(name, kind, source, _text, size);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            _switchResult = new OracledbQueries.Aggregate(name, kind, source, null, size);
            break;
        }
      } else {
        _switchResult = new OracledbQueries.Aggregate(name, kind, source, null, size);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Boost functions: defaults weight 1, decay rate 0.5 and offset 0, combine and apply multiply.
   */
  private OracledbQueries.Boost boostOf(final VectorCollection collection, final OracledbSql.Table table, final BoostBlock block) {
    OracledbQueries.Boost _xblockexpression = null;
    {
      final List<OracledbQueries.BoostFn> functions = CollectionLiterals.<OracledbQueries.BoostFn>newArrayList();
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
          final List<OracledbSql.Cond> when = CollectionLiterals.<OracledbSql.Cond>newArrayList();
          Condition _condition = fn.getCondition();
          boolean _tripleNotEquals = (_condition != null);
          if (_tripleNotEquals) {
            OracledbSql.Cond _condition_1 = new OracledbFilter().condition(fn.getCondition());
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
                OracledbQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
                OracledbQueries.BoostFn _boostFn = new OracledbQueries.BoostFn("field_value", _fieldRefOf, weight, null, null, null, null, 0.5, when);
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
                OracledbQueries.Ref _fieldRefOf_1 = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
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
                OracledbQueries.BoostFn _boostFn_1 = new OracledbQueries.BoostFn("decay", _fieldRefOf_1, weight, _literal, _of, _of_1, _xifexpression_2, rate, when);
                functions.add(_boostFn_1);
                break;
              case CONDITION:
                boolean _isEmpty = when.isEmpty();
                if (_isEmpty) {
                  throw Unsupported.of("a condition boost without a condition");
                }
                OracledbQueries.BoostFn _boostFn_2 = new OracledbQueries.BoostFn("condition", null, weight, null, null, null, null, 0.5, when);
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
      _xblockexpression = new OracledbQueries.Boost(functions, _xifexpression, _xifexpression_1);
    }
    return _xblockexpression;
  }

  /**
   * MMR over a candidate pool (the given size, else ten times limit plus offset), diversified on the named, searched or only vector field.
   */
  private OracledbQueries.Mmr mmrOf(final VectorCollection collection, final OracledbSql.Table table, final MmrConfig config, final VectorField searched, final int fetch) {
    OracledbQueries.Mmr _xblockexpression = null;
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
      final OracledbSql.Column column = table.column(ModelUtil.fieldName(field));
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
      _xblockexpression = new OracledbQueries.Mmr(_diversityLambda, pool, column, _metric);
    }
    return _xblockexpression;
  }

  /**
   * Keyword fields: the listed full-text columns with boosts, else every full-text column of the collection.
   */
  private List<OracledbQueries.KeywordField> keywordFields(final VectorCollection collection, final OracledbSql.Table table, final List<SearchFieldRef> refs) {
    List<OracledbQueries.KeywordField> _xblockexpression = null;
    {
      final List<OracledbQueries.KeywordField> result = CollectionLiterals.<OracledbQueries.KeywordField>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<OracledbSql.Column> _columns = table.columns();
        for (final OracledbSql.Column column : _columns) {
          boolean _text = column.text();
          if (_text) {
            OracledbQueries.KeywordField _keywordField = new OracledbQueries.KeywordField(column, 1.0);
            result.add(_keywordField);
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final OracledbSql.Column column_1 = table.column(ModelUtil.fieldName(ref.getField()));
            boolean _text_1 = column_1.text();
            boolean _not = (!_text_1);
            if (_not) {
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
            OracledbQueries.KeywordField _keywordField_1 = new OracledbQueries.KeywordField(column_1, _xifexpression);
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
   * The Oracle Text form of a text query type: match (all terms with AND, any term with ACCUM) and phrase.
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
        case "wildcard":
          throw Unsupported.of((((("text-query " + type) + "; ") + OracledbStatements.PENDING) + " (the % wildcard of Oracle Text on escaped terms)"));
        case "fuzzy":
          throw Unsupported.of((("text-query fuzzy; " + OracledbStatements.PENDING) + " (the FUZZY operator of Oracle Text)"));
        case "term":
        case "query_string":
          throw Unsupported.of((((("text-query " + type) + "; ") + OracledbStatements.PENDING) + " (a query string passed to CONTAINS would need a parser that admits only safe Oracle Text operators)"));
        default:
          throw Unsupported.of((("text-query " + type) + "; Oracle Text queries have no regular-expression operator"));
      }
    } else {
      throw Unsupported.of((("text-query " + type) + "; Oracle Text queries have no regular-expression operator"));
    }
    return _switchResult;
  }

  private Code.Expr rows(final RecordSource source, final OracledbSql.Table table) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", OracledbSchema.kw("location", Code.text(((FileSource)source).getLocation())));
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

  private Code.Expr record(final RecordLiteral record, final OracledbSql.Table table) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final OracledbSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
          result.put(column.name(), this.convert(entry.getValue(), column));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A literal in the form Oracle reads for its column: sparse vectors as the JSON array [dims, [indexes], [values]] with indexes
   * from 1 (the AI Vector Search User's Guide's SPARSE textual form; vecDSL counts from 0, as Milvus, Qdrant and Pinecone), binary
   * vectors as the bytes of 8 bits each, first bit highest; everything else as written.
   */
  private Code.Expr convert(final Literal literal, final OracledbSql.Column column) {
    Code.Expr _xblockexpression = null;
    {
      OracledbSql.Vec _vec = column.vec();
      boolean _tripleEquals = (_vec == null);
      if (_tripleEquals) {
        return Literals.of(literal);
      }
      boolean _sparse = column.vec().sparse();
      if (_sparse) {
        if ((literal instanceof SparseLit)) {
          int _dim = column.vec().dim();
          boolean _lessEqualsThan = (_dim <= 0);
          if (_lessEqualsThan) {
            throw Unsupported.of("a sparse vector field without dim; the generator writes the dimension count of sparse vectors");
          }
          final Function1<SparseEntry, Integer> _function = (SparseEntry e) -> {
            return Integer.valueOf(e.getIndex());
          };
          final List<SparseEntry> entries = IterableExtensions.<SparseEntry, Integer>sortBy(((SparseLit) literal).getEntries(), _function);
          Code.Num _num = Code.num(column.vec().dim());
          final Function1<SparseEntry, Code.Expr> _function_1 = (SparseEntry e) -> {
            Code.Num _num_1 = Code.num(e.getIndex());
            return ((Code.Expr) _num_1);
          };
          Code.Items _items = Code.items(ListExtensions.<SparseEntry, Code.Expr>map(entries, _function_1));
          final Function1<SparseEntry, Code.Expr> _function_2 = (SparseEntry e) -> {
            Code.Num _num_1 = Code.num(e.getWeight());
            return ((Code.Expr) _num_1);
          };
          Code.Items _items_1 = Code.items(ListExtensions.<SparseEntry, Code.Expr>map(entries, _function_2));
          return Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_num, _items, _items_1)));
        }
        if ((literal instanceof ParamRef)) {
          throw Unsupported.of((("a sparse vector parameter; " + OracledbStatements.PENDING) + " (conversion of the parameter to the SPARSE textual form in the runtimes)"));
        }
      }
      String _format = column.vec().format();
      boolean _equals = Objects.equals(_format, "BINARY");
      if (_equals) {
        if ((literal instanceof ListLit)) {
          final Function1<Literal, Boolean> _function_3 = (Literal item) -> {
            return Boolean.valueOf(((item instanceof NumberLit) && (((NumberLit) item).getValue() != 0)));
          };
          final List<Boolean> bits = IterableExtensions.<Boolean>toList(ListExtensions.<Literal, Boolean>map(((ListLit) literal).getItems(), _function_3));
          int _size = bits.size();
          int _modulo = (_size % 8);
          boolean _notEquals = (_modulo != 0);
          if (_notEquals) {
            int _size_1 = bits.size();
            String _plus = ("a binary vector of " + Integer.valueOf(_size_1));
            String _plus_1 = (_plus + " bits; BINARY vectors have a multiple of 8 dimensions");
            throw Unsupported.of(_plus_1);
          }
          final List<Code.Expr> bytes = CollectionLiterals.<Code.Expr>newArrayList();
          {
            int i = 0;
            int _size_2 = bits.size();
            boolean _lessThan = (i < _size_2);
            boolean _while = _lessThan;
            while (_while) {
              {
                int value = 0;
                for (int j = 0; (j < 8); j++) {
                  Boolean _get = bits.get((i + j));
                  if ((_get).booleanValue()) {
                    value = (value + (1 << (7 - j)));
                  }
                }
                Code.Num _num_1 = Code.num(value);
                bytes.add(_num_1);
              }
              int _i = i;
              i = (_i + 8);
              int _size_3 = bits.size();
              boolean _lessThan_1 = (i < _size_3);
              _while = _lessThan_1;
            }
          }
          return Code.items(bytes);
        }
        if ((literal instanceof ParamRef)) {
          throw Unsupported.of((("a binary vector parameter; " + OracledbStatements.PENDING) + " (packing of the parameter\'s bits in the runtimes)"));
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
