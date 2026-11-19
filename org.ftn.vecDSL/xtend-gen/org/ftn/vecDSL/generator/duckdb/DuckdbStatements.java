package org.ftn.vecDSL.generator.duckdb;

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
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
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

/**
 * Maps each vecDSL statement to SQL for DuckDB with the vss and fts extensions in the host-language-neutral code model. The
 * SQL text is built here (through DuckdbSql and DuckdbQueries) and passed to the runtime with its parameters, so the three
 * host languages send identical statements: query and value return JSON rows, change runs a write and the fts rebuild of its
 * table in one transaction, execute runs DDL in one transaction, transaction runs a vecDSL transaction.
 */
@SuppressWarnings("all")
public class DuckdbStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; DuckdbCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for DuckDB: ";

  /**
   * Suffix of constructs DuckDB supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no DuckDB mapping yet";

  public static final String NO_MODELS = "; DuckDB has no embedding models in its core extensions, so generated programs supply their own vectors";

  public static final String FLOAT_ONLY = "the vss extension indexes FLOAT[n] arrays only (\"Only vectors consisting of FLOATs (32-bit, single precision) are supported\"), and DuckDB has no half-precision, binary or sparse vector type with distance functions";

  public static final String NO_SPATIAL = "geo fields, predicates and indexes need the spatial extension (ST_ functions, R-tree indexes), which DuckDB does not autoload and which the generated programs do not load (they load vss and fts only)";

  public static final String NO_AUTO_ID = "auto-id; the generated runtimes report how many rows a write changed, not the keys DuckDB would assign (a sequence or uuid() default), so records written without a key could not be addressed by id afterwards";

  public static final String NO_CROSS_REF = "cross-ref; a cross-ref names collections but no field that holds the reference, so there is no column to declare a FOREIGN KEY on";

  public static final String NO_PATH_INDEX = "DuckDB 2.0.0 refuses JSON functions in index expressions (CREATE INDEX on json_extract_string fails with Binder Error: Cannot use \"json_extract_string\" in this context, measured)";

  public static final String NO_FUZZY = "match_bm25 matches stemmed terms exactly, and DuckDB\'s string similarity functions (levenshtein, jaro_winkler_similarity) compare whole strings, not the terms of a text";

  public static final String NO_DYNAMIC = "dynamic; a DuckDB table has a fixed set of typed columns, and the generator keeps no undeclared fields (a JSON column for them would hide them from filters, indexes and full-text search)";

  public static final String NO_TTL = "ttl; DuckDB rows do not expire and an embedded database runs no background jobs";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; DuckDB has no per-table drop guard";

  public static final String NO_STORAGE = "storage settings; a DuckDB database is one file, whose path is the connection";

  public static final String NO_TAGS = "collection tags; a DuckDB table carries one comment (the description) and no tags";

  public static final String NO_PARTITIONS = "partitions; DuckDB tables have no partitions (Hive partitioning applies to COPY ... TO files, not to tables)";

  public static final String EMBEDDED = "an embedded database: DuckDB runs inside the program over one file, with no server, users or replicas";

  private static final String NO_GENERATION = "DuckDB has no generative models in its core extensions";

  private static final String NO_RERANKERS = "DuckDB has no reranking models in its core extensions";

  private static final String NO_METRICS = ("metrics configuration; " + DuckdbStatements.EMBEDDED);

  private static final String NO_SCHEDULES = (("backup schedules; " + DuckdbStatements.EMBEDDED) + ", so nothing runs between programs");

  private static final String NO_CLUSTER_LIMITS = "cluster limits; DuckDB has no setting that limits the number of tables";

  private static final String NO_FILES = "file resources; the fts extension takes stop words from a table and has no other resource files";

  private static final String NO_RESIDENCY = "load and release; DuckDB reads table data on demand through its buffer manager, and an HNSW index is loaded into memory when its table is first accessed after a restart (vss documentation, \"Persistence\"), so there is nothing to load or release";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why DuckDB cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "DuckDB reads the current committed state and keeps no queryable history of a table"), Pair.<String, String>of("explain", "DuckDB explains query plans (EXPLAIN), not scores"), Pair.<String, String>of("reranker", DuckdbStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", ("generated DuckDB programs embed no query text" + DuckdbStatements.NO_MODELS)), Pair.<String, String>of("minShouldMatch", "match_bm25 requires all terms (conjunctive) or any term, with no minimum number of matching terms"), Pair.<String, String>of("fuzziness", "match_bm25 matches stemmed terms exactly"), Pair.<String, String>of("slop", DuckdbSql.NO_POSITIONS), Pair.<String, String>of("highlighted", "the fts extension returns scores only and has no highlighting function")));

  /**
   * Clauses DuckDB supports that the generator does not produce yet, with the SQL that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "list_filter over the elements")));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  /**
   * Snapshots of one tenant taken earlier in the script, by name: their restore replaces that tenant's rows only.
   */
  private final Map<String, String> tenantSnapshots = CollectionLiterals.<String, String>newHashMap();

  private final DuckdbSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public DuckdbStatements(final Script script) {
    this.script = script;
    DuckdbSchema _duckdbSchema = new DuckdbSchema(this.notes, script);
    this.schema = _duckdbSchema;
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
          _switchResult = "the fts extension indexes text columns directly and has no BM25 function producing sparse vectors";
          break;
        case "custom":
          _switchResult = "a custom function names its input and output fields but no computation, so there is no expression for a generated column";
          break;
        default:
          _switchResult = "DuckDB has no model functions in its core extensions";
          break;
      }
    } else {
      _switchResult = "DuckDB has no model functions in its core extensions";
    }
    return _switchResult;
  }

  public static String collectionLimit(final String type) {
    return (("collection limits (" + type) + "); DuckDB has no per-table limit settings");
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
            throw Unsupported.of(DuckdbStatements.NO_METRICS);
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
            throw Unsupported.of(DuckdbStatements.NO_CLUSTER_LIMITS);
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + DuckdbStatements.EMBEDDED));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, DuckdbGenerator.VENDOR).keySet();
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
          String _xifexpression = null;
          if (ifNotExists) {
            _xifexpression = "IF NOT EXISTS ";
          } else {
            _xifexpression = "";
          }
          String _plus = ("CREATE SCHEMA " + _xifexpression);
          String _id = DuckdbSql.id(((Database)concept).getName());
          String _plus_1 = (_plus + _id);
          Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_1)));
          result.add(_execute);
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
          String _createView = DuckdbSchema.createView(((Alias)concept).getName(), this.schema.table(((Alias)concept).getCollection()), ifNotExists);
          Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView)));
          result.add(_execute);
          this.schema.view(((Alias)concept).getName(), ((Alias)concept).getCollection());
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
          throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
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
          String _functionReason = DuckdbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          List<Code.Stmt> _checkAnalyzer = this.checkAnalyzer(((Analyzer)concept));
          Iterables.<Code.Stmt>addAll(result, _checkAnalyzer);
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer == null)) {
            throw Unsupported.of("an analyzer filter outside an analyzer; the fts options belong to an analyzer");
          }
          List<Code.Stmt> _checkAnalyzer = this.checkAnalyzer(analyzer);
          Iterables.<Code.Stmt>addAll(result, _checkAnalyzer);
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("a field limit on an existing field; DuckDB adds CHECK constraints only in CREATE TABLE (DuckDB 2.0.0: \"No support for adding CHECK constraints with ALTER TABLE\")");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + DuckdbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + DuckdbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + DuckdbStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of(("quantization; " + DuckdbStatements.FLOAT_ONLY));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          this.authentication(((AuthConfig)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of((("roles and grants; " + DuckdbStatements.EMBEDDED) + ", and access to it is the file system\'s"));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (DuckdbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The program's database file: checked here, opened by connect().
   */
  private void connection(final Connection concept) {
    if ((ModelUtil.written(concept, "deployment") && (!Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED)))) {
      String _literal = concept.getDeployment().getLiteral();
      String _plus = (_literal + " deployments; the generated programs open DuckDB in process (MotherDuck, the hosted service, is out of scope)");
      throw Unsupported.of(_plus);
    }
    if (((concept.getApiKey() != null) || (concept.getToken() != null))) {
      throw Unsupported.of(("api keys and tokens; " + DuckdbStatements.EMBEDDED));
    }
    if (((concept.getPassword() != null) || (concept.getUsername() != null))) {
      throw Unsupported.of(("users and passwords; " + DuckdbStatements.EMBEDDED));
    }
    if ((((concept.getHost() != null) || (concept.getPort() > 0)) || (concept.getGrpcPort() > 0))) {
      throw Unsupported.of(("host and ports; " + DuckdbStatements.EMBEDDED));
    }
    if (((concept.getUri() != null) && concept.getUri().matches("[A-Za-z][A-Za-z0-9+.-]+:.*"))) {
      String _uri = concept.getUri();
      String _plus_1 = ("the uri " + _uri);
      String _plus_2 = (_plus_1 + "; the generated programs open a local database file");
      throw Unsupported.of(_plus_2);
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      DuckdbSql.id(concept.getDatabaseName());
    }
    Set<String> _keySet = ModelUtil.extParams(concept, DuckdbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
    this.schema.note(concept, "timeoutMillis", "cloud", "region", "project", "tls");
  }

  /**
   * Authentication: none is the only mode of an embedded database.
   */
  private void authentication(final AuthConfig auth) {
    if ((((((((!Objects.equals(auth.getType(), EAuthType.NONE)) || (!auth.getRoles().isEmpty())) || (auth.getToken() != null)) || (auth.getCertificatePath() != null)) || (auth.getCmekKeyId() != null)) || auth.isFieldEncryption()) || auth.isAuditLog())) {
      throw Unsupported.of((("authentication; " + DuckdbStatements.EMBEDDED) + ", and access to it is the file system\'s"));
    }
  }

  /**
   * An analyzer is the option set of its table's fts index; creating it checks the stemmer in the extension.
   */
  private List<Code.Stmt> checkAnalyzer(final Analyzer analyzer) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Analysis analysis = DuckdbSchema.analysis(analyzer);
      final DuckdbSql.Query q = new DuckdbSql.Query();
      String _bind = q.bind(Code.text("running"), "VARCHAR");
      String _plus = ("SELECT to_json(stem(" + _bind);
      String _plus_1 = (_plus + ", ");
      String _bind_1 = q.bind(Code.text(analysis.stemmer()), "VARCHAR");
      String _plus_2 = (_plus_1 + _bind_1);
      String _plus_3 = (_plus_2 + "))");
      Code.Call _call = DuckdbSchema.call("value", _plus_3, q, null);
      Code.Run _run = new Code.Run(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
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
    throw Unsupported.of((what + " outside a collection; DuckDB adds it to a table"));
  }

  /**
   * An index created on an existing collection; full_text indexes are the table's fts index, rebuilt with every write.
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
        final DuckdbSql.Table table = this.schema.table(collection);
        final String sql = this.schema.vectorIndex(collection, table, field, ((VectorIndex)index), ifNotExists);
        if ((sql == null)) {
          return this.require(collection);
        }
        Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(sql)));
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
          final DuckdbSql.Table table = this.schema.table(collection);
          final String sql = this.schema.payloadIndex(collection, table, ((PayloadIndex)index), ifNotExists);
          String _elvis_1 = null;
          if (sql != null) {
            _elvis_1 = sql;
          } else {
            String _createFtsIndex = DuckdbSql.createFtsIndex(table);
            _elvis_1 = _createFtsIndex;
          }
          Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_elvis_1)));
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
      final DuckdbSql.Table table = this.schema.table(collection);
      String _table = DuckdbSql.table(table);
      String _plus = ("SELECT NULL::JSON FROM " + _table);
      String _plus_1 = (_plus + " WHERE false");
      Code.Call _call = DuckdbSchema.call("query", _plus_1, null, null);
      Code.Run _run = new Code.Run(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * ALTER TABLE ADD COLUMN; a vector field with an index gets the index, a full-text field the rebuilt fts index.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(collection);
      final List<String> ddl = this.addColumn(collection, table, field);
      final DuckdbSql.Table after = table.with(this.schema.column(collection, field));
      if (((field instanceof PayloadField) && after.column(ModelUtil.fieldName(field)).fullText())) {
        String _createFtsIndex = DuckdbSql.createFtsIndex(after);
        ddl.add(_createFtsIndex);
      }
      this.schema.evolve(collection, after);
      Code.Stmt _execute = DuckdbSchema.execute(ddl);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  private List<String> addColumn(final VectorCollection collection, final DuckdbSql.Table table, final Field field) {
    List<String> _xblockexpression = null;
    {
      final DuckdbSql.Column column = this.schema.column(collection, field);
      boolean _isEmpty = column.checks().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("a field limit on an added field; DuckDB adds CHECK constraints only in CREATE TABLE");
      }
      final List<String> result = CollectionLiterals.<String>newArrayList();
      DuckdbSql.Column _xifexpression = null;
      if ((field instanceof VectorField)) {
        _xifexpression = this.nullableVector(column);
      } else {
        _xifexpression = column;
      }
      final String definition = DuckdbSql.columnDefinition(_xifexpression, false);
      String _table = DuckdbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " ADD COLUMN ");
      String _plus_2 = (_plus_1 + definition);
      result.add(_plus_2);
      if ((field instanceof VectorField)) {
        final VectorIndex index = this.schema.indexOf(collection, ((VectorField) field));
        if ((index != null)) {
          String _vectorIndex = this.schema.vectorIndex(collection, table.with(column), ((VectorField) field), index, false);
          result.add(_vectorIndex);
        }
      }
      _xblockexpression = IterableExtensions.<String>toList(IterableExtensions.<String>filterNull(result));
    }
    return _xblockexpression;
  }

  /**
   * Rows that exist when a vector column is added have no vector, so the added column is nullable.
   */
  private DuckdbSql.Column nullableVector(final DuckdbSql.Column column) {
    String _name = column.name();
    String _type = column.type();
    DuckdbSql.Vec _vec = column.vec();
    return new DuckdbSql.Column(_name, _type, null, false, _vec, null, true, null, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
  }

  /**
   * ALTER TABLE in one transaction. DuckDB refuses to drop or rename a column, or to rename a table, while secondary indexes
   * depend on it ("Cannot alter entry ... because there are entries that depend on it" on DuckDB 2.0.0), and indexes
   * dropped in the same transaction still count ("an index depends on a column after it"), so such an alter first drops the
   * table's indexes in a transaction of their own, then alters the table and creates the indexes again on it; a renamed
   * table gets its views again.
   */
  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        if (((!it.getOperations().isEmpty()) || (it.getNewName() == null))) {
          throw Unsupported.of("alter operations on an alias; an alias is a view, which can be renamed");
        }
        final Alias alias = ((Alias) concept);
        final DuckdbSql.Table table = this.schema.table(alias.getCollection());
        DuckdbSql.id(it.getNewName());
        this.schema.view(alias.getName(), null);
        this.schema.view(it.getNewName(), alias.getCollection());
        String _qualified = DuckdbSql.qualified(table, alias.getName());
        String _plus = ("ALTER VIEW " + _qualified);
        String _plus_1 = (_plus + " RENAME TO ");
        String _id = DuckdbSql.id(it.getNewName());
        String _plus_2 = (_plus_1 + _id);
        Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus_3 = ("altering a " + _name);
        String _plus_4 = (_plus_3 + "; DuckDB alters tables and views");
        throw Unsupported.of(_plus_4);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      final DuckdbSql.Table before = this.schema.table(collection);
      DuckdbSql.Table table_1 = before;
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      final boolean restructures = ((it.getNewName() != null) || IterableExtensions.<AlterOperation>exists(it.getOperations(), ((Function1<AlterOperation, Boolean>) (AlterOperation op) -> {
        return Boolean.valueOf(((op instanceof DropFieldOp) || (op instanceof RenameFieldOp)));
      })));
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        {
          List<String> _alter = this.alter(collection, table_1, operation);
          Iterables.<String>addAll(ddl, _alter);
          table_1 = this.evolved(collection, table_1, operation);
        }
      }
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        String _table = DuckdbSql.table(table_1);
        String _plus_5 = ("ALTER TABLE " + _table);
        String _plus_6 = (_plus_5 + " RENAME TO ");
        String _id_1 = DuckdbSql.id(it.getNewName());
        String _plus_7 = (_plus_6 + _id_1);
        ddl.add(_plus_7);
        table_1 = table_1.named(it.getNewName());
      }
      if (restructures) {
        List<String> _indexes = this.schema.indexes(collection, table_1, false);
        Iterables.<String>addAll(ddl, _indexes);
        String _newName_1 = it.getNewName();
        boolean _tripleNotEquals_1 = (_newName_1 != null);
        if (_tripleNotEquals_1) {
          List<String> _viewsOf = this.schema.viewsOf(collection);
          for (final String alias_1 : _viewsOf) {
            String _createView = DuckdbSchema.createView(alias_1, table_1, true);
            ddl.add(_createView);
          }
        }
      } else {
        final Function1<AlterOperation, Boolean> _function = (AlterOperation op) -> {
          return Boolean.valueOf(((op instanceof AddFieldOp) && (((AddFieldOp) op).getNewField() instanceof PayloadField)));
        };
        boolean _exists = IterableExtensions.<AlterOperation>exists(it.getOperations(), _function);
        if (_exists) {
          boolean _isEmpty = table_1.fullTextColumns().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            String _createFtsIndex = DuckdbSql.createFtsIndex(table_1);
            ddl.add(_createFtsIndex);
          }
        }
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      if (restructures) {
        Code.Stmt _execute_1 = DuckdbSchema.execute(this.schema.dropIndexes(collection, before));
        result.add(_execute_1);
      }
      boolean _isEmpty_1 = ddl.isEmpty();
      if (_isEmpty_1) {
        List<Code.Stmt> _require = this.require(collection);
        Iterables.<Code.Stmt>addAll(result, _require);
      } else {
        Code.Stmt _execute_2 = DuckdbSchema.execute(ddl);
        result.add(_execute_2);
      }
      this.schema.evolve(collection, table_1);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The table after an alter operation, for the statements that follow it.
   */
  private DuckdbSql.Table evolved(final VectorCollection collection, final DuckdbSql.Table table, final AlterOperation operation) {
    DuckdbSql.Table _switchResult = null;
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

  public List<String> alter(final VectorCollection collection, final DuckdbSql.Table table, final AlterOperation operation) {
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
          DuckdbSql.id(((RenameFieldOp)operation).getNewName());
          String _table = DuckdbSql.table(table);
          String _plus = ("ALTER TABLE " + _table);
          String _plus_1 = (_plus + " RENAME COLUMN ");
          String _id = DuckdbSql.id(table.column(ModelUtil.fieldName(((RenameFieldOp)operation).getField())).name());
          String _plus_2 = (_plus_1 + _id);
          String _plus_3 = (_plus_2 + " TO ");
          String _id_1 = DuckdbSql.id(((RenameFieldOp)operation).getNewName());
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
        List<String> _xblockexpression = null;
        {
          this.schema.view(((AddAliasOp)operation).getNewAlias().getName(), collection);
          String _createView = DuckdbSchema.createView(((AddAliasOp)operation).getNewAlias().getName(), table, false);
          _xblockexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createView));
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = DuckdbStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = DuckdbStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (DuckdbStatements.NOT_MAPPED + _name);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private String dropColumn(final VectorCollection collection, final DuckdbSql.Table table, final Field field) {
    String _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
      }
      String _table = DuckdbSql.table(table);
      String _plus = ("ALTER TABLE " + _table);
      String _plus_1 = (_plus + " DROP COLUMN ");
      String _id = DuckdbSql.id(table.column(ModelUtil.fieldName(field)).name());
      _xblockexpression = (_plus_1 + _id);
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: the description is the table comment; consistency is met by every committed write.
   */
  private List<String> setProperty(final DuckdbSql.Table table, final SetPropertyOp operation) {
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
            String _table = DuckdbSql.table(table);
            String _plus = ("COMMENT ON TABLE " + _table);
            String _plus_1 = (_plus + " IS ");
            Literal _value_1 = operation.getValue();
            String _literal = DuckdbSql.literal(((StringLit) _value_1).getValue());
            String _plus_2 = (_plus_1 + _literal);
            _xblockexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2));
          }
          _switchResult = _xblockexpression;
          break;
        case CONSISTENCY:
          _switchResult = CollectionLiterals.<String>newArrayList();
          break;
        case TAGS:
          throw Unsupported.of(DuckdbStatements.NO_TAGS);
        case DYNAMIC:
          throw Unsupported.of(DuckdbStatements.NO_DYNAMIC);
        case TTL:
          throw Unsupported.of(DuckdbStatements.NO_TTL);
        case DELETION_PROTECTION:
          throw Unsupported.of(DuckdbStatements.NO_DELETION_PROTECTION);
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
          final DuckdbSql.Table table = this.schema.table(((VectorCollection)concept));
          this.schema.evolve(((VectorCollection)concept), null);
          final List<String> ddl = CollectionLiterals.<String>newArrayList();
          String _ftsSchema = DuckdbSql.ftsSchema(table);
          String _plus = ("DROP SCHEMA IF EXISTS " + _ftsSchema);
          String _plus_1 = (_plus + " CASCADE");
          ddl.add(_plus_1);
          String _table = DuckdbSql.table(table);
          String _plus_2 = (("DROP TABLE " + exists) + _table);
          ddl.add(_plus_2);
          Code.Stmt _execute = DuckdbSchema.execute(ddl);
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final List<String> ddl = CollectionLiterals.<String>newArrayList();
            EList<VectorCollection> _collections = ((Database)concept).getCollections();
            for (final VectorCollection collection : _collections) {
              String _ftsSchema = DuckdbSql.ftsSchema(this.schema.table(collection));
              String _plus = ("DROP SCHEMA IF EXISTS " + _ftsSchema);
              String _plus_1 = (_plus + " CASCADE");
              ddl.add(_plus_1);
            }
            String _id = DuckdbSql.id(((Database)concept).getName());
            String _plus_2 = (("DROP SCHEMA " + exists) + _id);
            String _plus_3 = (_plus_2 + " CASCADE");
            ddl.add(_plus_3);
            Code.Stmt _execute = DuckdbSchema.execute(ddl);
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
            this.schema.view(((Alias)concept).getName(), null);
            String _qualified = DuckdbSql.qualified(this.schema.table(((Alias)concept).getCollection()), ((Alias)concept).getName());
            String _plus = (("DROP VIEW " + exists) + _qualified);
            Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
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
          String _functionReason = DuckdbStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of(("quantization; " + DuckdbStatements.FLOAT_ONLY));
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program\'s database file, not a DuckDB object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; the program drops schemas and tables, not the database file");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; an analyzer is the option set of an fts index, which every write rebuilds");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; an analyzer is the option set of an fts index");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("dropping a field limit; DuckDB drops no constraints with ALTER TABLE (DuckDB 2.0.0: \"No support for that ALTER TABLE option yet\")");
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + DuckdbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + DuckdbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + DuckdbStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("roles and grants; " + DuckdbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(DuckdbStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((DuckdbStatements.NOT_MAPPED + "dropping a ") + _name);
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
      final DuckdbSql.Table table = this.schema.table(collection);
      final DuckdbSql.Query q = new DuckdbSql.Query();
      String _table = DuckdbSql.table(table);
      String _plus = ("DELETE FROM " + _table);
      String _plus_1 = (_plus + " AS ");
      String _plus_2 = (_plus_1 + DuckdbSql.ALIAS);
      String _plus_3 = (_plus_2 + " WHERE ");
      String _col = DuckdbSql.col(column);
      String _plus_4 = (_plus_3 + _col);
      String _plus_5 = (_plus_4 + " = ");
      String _bind = q.bind(Code.text(value), "VARCHAR");
      final String sql = (_plus_5 + _bind);
      Code.Call _change = DuckdbSchema.change(sql, q, "delete_count", table, false);
      Code.Return _return = new Code.Return(_change);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * ALTER TABLE DROP COLUMN, with the table's indexes dropped first (own transaction) and created again on the remaining columns.
   */
  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(collection);
      final DuckdbSql.Table after = table.without(ModelUtil.fieldName(field));
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      String _dropColumn = this.dropColumn(collection, table, field);
      ddl.add(_dropColumn);
      List<String> _indexes = this.schema.indexes(collection, after, false);
      Iterables.<String>addAll(ddl, _indexes);
      this.schema.evolve(collection, after);
      Code.Stmt _execute = DuckdbSchema.execute(this.schema.dropIndexes(collection, table));
      Code.Stmt _execute_1 = DuckdbSchema.execute(ddl);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute, _execute_1));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropIndex(final Index index, final String exists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(index);
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      final DuckdbSql.Table table = this.schema.table(collection);
      if (((index instanceof PayloadIndex) && Objects.equals(((PayloadIndex) index).getKind(), EPayloadIndexKind.FULL_TEXT))) {
        throw Unsupported.of("dropping a full_text index; the fts index of a table covers all its full-text fields and every write rebuilds it");
      }
      String _qualified = DuckdbSql.qualified(table, DuckdbSchema.indexName(collection, index.getName()));
      String _plus = (("DROP INDEX " + exists) + _qualified);
      Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
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

  /**
   * An alias is a view: the swap replaces its query in one transaction.
   */
  protected List<Code.Stmt> _body(final SwapAlias it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(it.getNewCollection());
      final DuckdbSql.Table old = this.schema.table(it.getOldCollection());
      String _schema = table.schema();
      String _schema_1 = old.schema();
      boolean _notEquals = (!Objects.equals(_schema, _schema_1));
      if (_notEquals) {
        throw Unsupported.of("swapping an alias across databases; a view lives in the schema of its table");
      }
      this.schema.view(it.getAlias().getName(), it.getNewCollection());
      String _qualified = DuckdbSql.qualified(table, it.getAlias().getName());
      String _plus = ("DROP VIEW IF EXISTS " + _qualified);
      String _createView = DuckdbSchema.createView(it.getAlias().getName(), table, false);
      Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus, _createView)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(DuckdbStatements.NO_TTL);
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
        throw Unsupported.of(DuckdbStatements.NO_TTL);
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
   * One INSERT of the rows (as a JSON array parameter) with the slice values of the statement, then the fts rebuild.
   */
  private Code.Call write(final VectorCollection collection, final RecordSource source, final String policy, final String counter, final EObject statement) {
    Code.Call _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(collection);
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final String sql = DuckdbSql.insertRows(q, table, this.rows(source, table), this.slices(statement, collection, table), policy);
      boolean _equals = Objects.equals(policy, "ignore");
      _xblockexpression = DuckdbSchema.change(sql, q, counter, table, _equals);
    }
    return _xblockexpression;
  }

  /**
   * Column values for tenant and namespace of a write.
   */
  private Map<String, Code.Expr> slices(final EObject statement, final VectorCollection collection, final DuckdbSql.Table table) {
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
      boolean _tripleNotEquals = (_feature_2 != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
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
        throw Unsupported.of("if-version; DuckDB rows carry no version number (MVCC versions are internal), and the generator adds no version column");
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
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
          String _keyIn = DuckdbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
          restrictions.add(_keyIn);
        }
        String _where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
        restrictions.add(_where);
        final String fromRecords = DuckdbSql.updateRows(q, table, records, (ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE)), DuckdbSql.and(restrictions));
        Code.Call _change = DuckdbSchema.change(fromRecords, q, "update_count", table, false);
        Code.Return _return = new Code.Return(_change);
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
            final DuckdbSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
            final String columnName = column.name();
            given.add(columnName);
            final String assigned = DuckdbSql.value(q, column, Literals.of(entry.getValue()));
            boolean _contains = it.getAppendFields().contains(entry.getField());
            if (_contains) {
              this.arrayField(entry.getField(), column);
              String _id = DuckdbSql.id(columnName);
              String _plus = (_id + " = list_concat(coalesce(");
              String _id_1 = DuckdbSql.id(columnName);
              String _plus_1 = (_plus + _id_1);
              String _plus_2 = (_plus_1 + ", []), ");
              String _plus_3 = (_plus_2 + assigned);
              String _plus_4 = (_plus_3 + ")");
              sets.add(_plus_4);
            } else {
              boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
              if (_contains_1) {
                this.arrayField(entry.getField(), column);
                String _id_2 = DuckdbSql.id(columnName);
                String _plus_5 = (_id_2 + " = list_filter(");
                String _id_3 = DuckdbSql.id(columnName);
                String _plus_6 = (_plus_5 + _id_3);
                String _plus_7 = (_plus_6 + ", lambda element: NOT coalesce(list_contains(");
                String _plus_8 = (_plus_7 + assigned);
                String _plus_9 = (_plus_8 + ", element), false))");
                sets.add(_plus_9);
              } else {
                String _id_4 = DuckdbSql.id(columnName);
                String _plus_10 = (_id_4 + " = ");
                String _plus_11 = (_plus_10 + assigned);
                sets.add(_plus_11);
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
          final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field_1));
          if (((column.vec() != null) && (!column.nullable()))) {
            String _name = column.name();
            String _plus_3 = ("remove-fields on vector field " + _name);
            String _plus_4 = (_plus_3 + ", which is not nullable");
            throw Unsupported.of(_plus_4);
          }
          String _id = DuckdbSql.id(column.name());
          String _plus_5 = (_id + " = NULL");
          sets.add(_plus_5);
        }
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        List<DuckdbSql.Column> _columns = table.columns();
        for (final DuckdbSql.Column column : _columns) {
          if (((((!Objects.equals(column.name(), table.key())) && (column.vec() == null)) && (!given.contains(column.name()))) && (!IterableExtensions.<Field>exists(it.getRemovedFields(), ((Function1<Field, Boolean>) (Field f) -> {
            String _name = table.column(ModelUtil.fieldName(f)).name();
            String _name_1 = column.name();
            return Boolean.valueOf(Objects.equals(_name, _name_1));
          }))))) {
            String _id = DuckdbSql.id(column.name());
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
        String _keyIn_1 = DuckdbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function_1)));
        conditions.add(_keyIn_1);
      }
      String _where_1 = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      conditions.add(_where_1);
      String _table = DuckdbSql.table(table);
      String _plus_4 = ("UPDATE " + _table);
      String _plus_5 = (_plus_4 + " AS ");
      String _plus_6 = (_plus_5 + DuckdbSql.ALIAS);
      String _plus_7 = (_plus_6 + " SET ");
      String _join = IterableExtensions.join(sets, ", ");
      String _plus_8 = (_plus_7 + _join);
      String _whereClause = DuckdbSql.whereClause(DuckdbSql.and(conditions));
      final String update = (_plus_8 + _whereClause);
      Code.Call _change_1 = DuckdbSchema.change(update, q, "update_count", table, false);
      Code.Return _return_1 = new Code.Return(_change_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  private void arrayField(final Field field, final DuckdbSql.Column column) {
    boolean _array = column.array();
    boolean _not = (!_array);
    if (_not) {
      String _fieldName = ModelUtil.fieldName(field);
      String _plus = ("array-append or array-remove on " + _fieldName);
      String _plus_1 = (_plus + ", which is not an array field");
      throw Unsupported.of(_plus_1);
    }
  }

  /**
   * DELETE by ids, filter or all; with a limit, the rows are the first ones in key order (a rowid subquery).
   */
  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final List<String> conditions = CollectionLiterals.<String>newArrayList();
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        String _keyIn = DuckdbSql.keyIn(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
        conditions.add(_keyIn);
      }
      String _where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      conditions.add(_where);
      final String condition = DuckdbSql.and(conditions);
      if (((condition == null) && (!it.isDeleteAll()))) {
        throw Unsupported.of("a delete without ids, filter or \'all\'");
      }
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        String _table = DuckdbSql.table(table);
        String _plus = ((((DuckdbSql.ALIAS + ".rowid IN (SELECT ") + DuckdbSql.ALIAS) + ".rowid FROM ") + _table);
        String _plus_1 = (_plus + " ");
        String _plus_2 = (_plus_1 + DuckdbSql.ALIAS);
        String _whereClause = DuckdbSql.whereClause(condition);
        String _plus_3 = (_plus_2 + _whereClause);
        String _plus_4 = (_plus_3 + " ORDER BY ");
        String _col = DuckdbSql.col(table.key());
        String _plus_5 = (_plus_4 + _col);
        String _plus_6 = (_plus_5 + " LIMIT ");
        int _deleteLimit = it.getDeleteLimit();
        String _plus_7 = (_plus_6 + Integer.valueOf(_deleteLimit));
        _xifexpression = (_plus_7 + ")");
      } else {
        _xifexpression = condition;
      }
      final String target = _xifexpression;
      String _table_1 = DuckdbSql.table(table);
      String _plus_8 = ("DELETE FROM " + _table_1);
      String _plus_9 = (_plus_8 + " AS ");
      String _plus_10 = (_plus_9 + DuckdbSql.ALIAS);
      String _whereClause_1 = DuckdbSql.whereClause(target);
      final String delete = (_plus_10 + _whereClause_1);
      Code.Call _change = DuckdbSchema.change(delete, q, "delete_count", table, false);
      Code.Return _return = new Code.Return(_change);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The runtime reads a local JSON or JSON Lines file and writes it in batches with one INSERT statement, all in one
   * transaction, as the pgvector generator does; csv and parquet files, and imports inside a transaction, are read by DuckDB.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; an embedded database runs no ingestion pipeline between programs");
      }
      DuckdbSql.localPath(it.getLocation());
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        if ((Objects.equals(it.getFormat(), EImportFormat.PARQUET) || Objects.equals(it.getFormat(), EImportFormat.CSV))) {
          Code.Call _fileImport = this.fileImport(it);
          Code.Return _return = new Code.Return(_fileImport);
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; DuckDB reads JSON, CSV and Parquet with its core readers, not ");
        String _literal_1 = it.getFormat().getLiteral();
        String _plus_2 = (_plus_1 + _literal_1);
        throw Unsupported.of(_plus_2);
      }
      this.schema.note(it, "parallelism", "async");
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      final String policy = _xifexpression;
      Code.Nil _nil = new Code.Nil();
      final String sql = DuckdbSql.insertRows(q, table, _nil, this.slices(it, it.getCollection(), table), policy);
      final List<Code.Expr> extra = q.params().items().subList(1, q.params().items().size());
      String _xifexpression_1 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_1 = "insert_count";
      } else {
        _xifexpression_1 = "upsert_count";
      }
      final Code.Entries args = DuckdbSchema.kw("sql", Code.text(sql), "location", Code.text(it.getLocation()), "counter", Code.text(_xifexpression_1));
      boolean _isEmpty = extra.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("params", Code.items(extra));
      }
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      final String refresh = DuckdbSql.createFtsIndex(table);
      if ((refresh != null)) {
        Code.Text _text = Code.text(refresh);
        args.put("refresh", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)))));
      }
      boolean _equals = Objects.equals(policy, "ignore");
      if (_equals) {
        args.put("skipped", Code.flag(true));
      }
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return_1 = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  /**
   * An import read by DuckDB's core reader of the file (read_parquet, read_csv or read_json) in one INSERT, then the fts rebuild;
   * the path is a bound parameter relative to the working directory.
   */
  private Code.Call fileImport(final BulkImport it) {
    Code.Call _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; an embedded database runs no ingestion pipeline between programs");
      }
      this.schema.note(it, "parallelism", "async", "batchSize");
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        _xifexpression = it.getOnConflict().getLiteral();
      } else {
        _xifexpression = "error";
      }
      final String policy = _xifexpression;
      String _location = it.getLocation();
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(it, "format");
      if (_written_1) {
        _xifexpression_1 = it.getFormat().getLiteral();
      } else {
        _xifexpression_1 = null;
      }
      final String reader = DuckdbSql.reader(_location, _xifexpression_1);
      final String sql = DuckdbSql.insertFromFile(q, table, reader, DuckdbSql.localPath(it.getLocation()), this.slices(it, it.getCollection(), table), policy);
      String _xifexpression_2 = null;
      if ((Objects.equals(policy, "error") || Objects.equals(policy, "ignore"))) {
        _xifexpression_2 = "insert_count";
      } else {
        _xifexpression_2 = "upsert_count";
      }
      _xblockexpression = DuckdbSchema.change(sql, q, _xifexpression_2, table, false);
    }
    return _xblockexpression;
  }

  /**
   * The statements of a vecDSL transaction in one DuckDB transaction (DuckDB has one isolation level, snapshot isolation); an
   * import is one INSERT through DuckDB's file reader, so that it commits or rolls back with the other steps.
   */
  protected List<Code.Stmt> _body(final Transaction it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      EList<DmlMethod> _methods = it.getMethods();
      for (final DmlMethod statement : _methods) {
        {
          List<Code.Stmt> _xifexpression = null;
          if ((statement instanceof BulkImport)) {
            Code.Call _fileImport = this.fileImport(((BulkImport) statement));
            Code.Return _return = new Code.Return(_fileImport);
            _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
          } else {
            _xifexpression = this.body(statement);
          }
          final List<Code.Stmt> inner = _xifexpression;
          if (((inner.size() != 1) || (!(IterableExtensions.<Code.Stmt>head(inner) instanceof Code.Return)))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          Code.Stmt _head = IterableExtensions.<Code.Stmt>head(inner);
          final Code.Expr returned = ((Code.Return) _head).value();
          if (((!(returned instanceof Code.Call)) || (!((Code.Call) returned).args().has("sql")))) {
            throw Unsupported.of("this statement inside a transaction");
          }
          final Code.Call invocation = ((Code.Call) returned);
          boolean _has = invocation.args().has("settings");
          if (_has) {
            this.notes.add("search settings inside a transaction, which apply to single statements only");
          }
          final Code.Entries step = DuckdbSchema.kw("sql", invocation.args().get("sql"));
          for (final String key : Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("params", "counter", "refresh", "skipped"))) {
            boolean _has_1 = invocation.args().has(key);
            if (_has_1) {
              step.put(key, invocation.args().get(key));
            }
          }
          steps.add(step);
        }
      }
      boolean _written = ModelUtil.written(it, "isolationLevel");
      if (_written) {
        String _literal = it.getIsolationLevel().getLiteral();
        String _plus = ("isolationLevel " + _literal);
        String _plus_1 = (_plus + " on Transaction, which DuckDB runs with snapshot isolation");
        this.notes.add(_plus_1);
      }
      Code.Call _runtime = Code.runtime("transaction", DuckdbSchema.kw("steps", Code.items(steps)));
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
    Set<String> _keySet = ModelUtil.extParams(statement, DuckdbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * A statement's filter with its tenant and namespace conditions.
   */
  private DuckdbFilter filterOf(final EObject statement, final VectorCollection collection, final DuckdbSql.Table table, final FilterBlock block) {
    DuckdbFilter _xblockexpression = null;
    {
      final DuckdbFilter result = new DuckdbFilter().where(block);
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
      boolean _tripleNotEquals = (_feature_1 != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
      }
      Object _feature_2 = this.feature(statement, "partitions");
      final List<Partition> partitions = ((List<Partition>) _feature_2);
      if (((partitions != null) && (!partitions.isEmpty()))) {
        throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Load it) {
    throw Unsupported.of(DuckdbStatements.NO_RESIDENCY);
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of(DuckdbStatements.NO_RESIDENCY);
  }

  /**
   * CHECKPOINT writes the write-ahead log into the database file (and serializes the HNSW indexes).
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      List<Code.Stmt> _require = this.require(it.getCollection());
      Iterables.<Code.Stmt>addAll(result, _require);
      Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("CHECKPOINT")));
      result.add(_execute);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * PRAGMA hnsw_compact_index removes deleted entries from each HNSW index of the table (vss documentation); then CHECKPOINT.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "targetSegments", "async");
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final List<String> ddl = CollectionLiterals.<String>newArrayList();
      final Function1<VectorField, Boolean> _function = (VectorField it_1) -> {
        return Boolean.valueOf(DuckdbSchema.present(table, it_1));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(it.getCollection()), _function);
      for (final VectorField field : _filter) {
        {
          final VectorIndex index = this.schema.indexOf(it.getCollection(), field);
          if (((index != null) && (!Objects.equals(index.getFamily().getLiteral(), "flat")))) {
            String _schema = table.schema();
            String _plus = (_schema + ".");
            String _indexName = DuckdbSchema.indexName(it.getCollection(), index.getName());
            String _plus_1 = (_plus + _indexName);
            String _literal = DuckdbSql.literal(_plus_1);
            String _plus_2 = ("PRAGMA hnsw_compact_index(" + _literal);
            String _plus_3 = (_plus_2 + ")");
            ddl.add(_plus_3);
          }
        }
      }
      ddl.add("CHECKPOINT");
      Code.Stmt _execute = DuckdbSchema.execute(ddl);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      Code.Call _call = DuckdbSchema.call("value", DuckdbQueries.describe(q, table, it.isIncludeStats(), it.isIncludeIndexProgress()), q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + DuckdbStatements.NO_MODELS));
  }

  /**
   * A snapshot is a copy of the table in the database file: main.vecdsl_snapshot__<name>.
   */
  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope().getLiteral(), "collection")))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("snapshot scope " + _literal);
        String _plus_1 = (_plus + "; the generator snapshots one collection as a table copy");
        throw Unsupported.of(_plus_1);
      }
      boolean _isIncremental = it.isIncremental();
      if (_isIncremental) {
        throw Unsupported.of("incremental snapshots; a table copy is always complete");
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a snapshot location; snapshots are tables in the database file (EXPORT DATABASE writes a whole database, not one table)");
      }
      this.schema.note(it, "description");
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      String _id = DuckdbSql.id(DuckdbSql.MAIN);
      String _plus_2 = ("CREATE TABLE " + _id);
      String _plus_3 = (_plus_2 + ".");
      String _id_1 = DuckdbSql.id(DuckdbQueries.snapshotTable(this.snapshotOf(it)));
      String _plus_4 = (_plus_3 + _id_1);
      String _plus_5 = (_plus_4 + " AS SELECT * FROM ");
      String _table = DuckdbSql.table(table);
      final String copy = (_plus_5 + _table);
      Tenant _tenant = it.getTenant();
      boolean _tripleEquals = (_tenant == null);
      if (_tripleEquals) {
        Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(copy)));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      String _tenant_1 = table.tenant();
      boolean _tripleEquals_1 = (_tenant_1 == null);
      if (_tripleEquals_1) {
        String _name = it.getCollection().getName();
        String _plus_6 = ("a tenant snapshot of " + _name);
        String _plus_7 = (_plus_6 + ", which has no tenants");
        throw Unsupported.of(_plus_7);
      }
      this.tenantSnapshots.put(this.snapshotOf(it), it.getTenant().getName());
      String _id_2 = DuckdbSql.id(table.tenant());
      String _plus_8 = ((copy + " t WHERE t.") + _id_2);
      String _plus_9 = (_plus_8 + " = ");
      String _literal_1 = DuckdbSql.literal(it.getTenant().getName());
      String _plus_10 = (_plus_9 + _literal_1);
      Code.Stmt _execute_1 = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_10)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute_1));
    }
    return _xblockexpression;
  }

  private String snapshotOf(final Snapshot snapshot) {
    String _elvis = null;
    String _snapshotName = snapshot.getSnapshotName();
    if (_snapshotName != null) {
      _elvis = _snapshotName;
    } else {
      String _name = snapshot.getName();
      _elvis = _name;
    }
    return _elvis;
  }

  /**
   * Restore replaces the rows of the collection with the snapshot's, by column name, in one transaction.
   */
  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope().getLiteral(), "collection")))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("restore scope " + _literal);
        String _plus_1 = (_plus + "; the generator restores one collection from a table copy");
        throw Unsupported.of(_plus_1);
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a snapshot location; snapshots are tables in the database file");
      }
      this.schema.note(it, "async");
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      String _id = DuckdbSql.id(DuckdbSql.MAIN);
      String _plus_2 = (_id + ".");
      String _id_1 = DuckdbSql.id(DuckdbQueries.snapshotTable(it.getSnapshotName()));
      final String snapshot = (_plus_2 + _id_1);
      String _xifexpression = null;
      String _tenant = table.tenant();
      boolean _tripleEquals = (_tenant == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.tenantSnapshots.get(it.getSnapshotName());
      }
      final String tenantName = _xifexpression;
      String _table = DuckdbSql.table(table);
      String _plus_3 = ("DELETE FROM " + _table);
      final Code.Entries delete = DuckdbSchema.kw("sql", Code.text(_plus_3), "counter", Code.text("delete_count"));
      if ((tenantName != null)) {
        final DuckdbSql.Query q = new DuckdbSql.Query();
        String _table_1 = DuckdbSql.table(table);
        String _plus_4 = ("DELETE FROM " + _table_1);
        String _plus_5 = (_plus_4 + " AS t WHERE t.");
        String _id_2 = DuckdbSql.id(table.tenant());
        String _plus_6 = (_plus_5 + _id_2);
        String _plus_7 = (_plus_6 + " = ");
        String _bind = q.bind(Code.text(tenantName), "VARCHAR");
        String _plus_8 = (_plus_7 + _bind);
        delete.put("sql", Code.text(_plus_8));
        delete.put("params", q.params());
      }
      String _table_2 = DuckdbSql.table(table);
      String _plus_9 = ("INSERT INTO " + _table_2);
      String _plus_10 = (_plus_9 + " BY NAME SELECT * FROM ");
      String _plus_11 = (_plus_10 + snapshot);
      Code.Expr _restoreStep = this.restoreStep(_plus_11, table);
      final List<Code.Expr> steps = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) delete), _restoreStep));
      Code.Call _runtime = Code.runtime("transaction", DuckdbSchema.kw("steps", Code.items(steps)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr restoreStep(final String sql, final DuckdbSql.Table table) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries step = DuckdbSchema.kw("sql", Code.text(sql), "counter", Code.text("restored"));
      final String refresh = DuckdbSql.createFtsIndex(table);
      if ((refresh != null)) {
        Code.Text _text = Code.text(refresh);
        step.put("refresh", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)))));
      }
      _xblockexpression = step;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a snapshot location; snapshots are tables in the database file");
      }
      String _id = DuckdbSql.id(DuckdbSql.MAIN);
      String _plus = ("DROP TABLE IF EXISTS " + _id);
      String _plus_1 = (_plus + ".");
      String _id_1 = DuckdbSql.id(DuckdbQueries.snapshotTable(it.getSnapshotName()));
      String _plus_2 = (_plus_1 + _id_1);
      Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus_2)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
    }
    return _xblockexpression;
  }

  /**
   * A new table with the source's columns, key and indexes, filled from it, in one transaction.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; DuckDB has no copy-on-write tables");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals = (_asOf != null);
      if (_tripleNotEquals) {
        String _get = DuckdbStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals_1 = (_sourceDatabase != null);
      if (_tripleNotEquals_1) {
        return this.cloneDatabase(it.getTargetName());
      }
      final DuckdbSql.Table table = this.schema.table(it.getSourceCollection());
      final DuckdbSql.Table copy = table.named(it.getTargetName());
      final List<Code.Expr> steps = CollectionLiterals.<Code.Expr>newArrayList();
      Code.Entries _kw = DuckdbSchema.kw("sql", Code.text(DuckdbSql.createTable(copy, false)));
      steps.add(_kw);
      String _table = DuckdbSql.table(copy);
      String _plus_1 = ("INSERT INTO " + _table);
      String _plus_2 = (_plus_1 + " BY NAME SELECT * FROM ");
      String _table_1 = DuckdbSql.table(table);
      String _plus_3 = (_plus_2 + _table_1);
      final Code.Entries insert = DuckdbSchema.kw("sql", Code.text(_plus_3), "counter", Code.text("copied"));
      steps.add(insert);
      final Function1<VectorField, Boolean> _function = (VectorField it_1) -> {
        return Boolean.valueOf(DuckdbSchema.present(table, it_1));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(it.getSourceCollection()), _function);
      for (final VectorField field : _filter) {
        {
          final VectorIndex index = this.schema.indexOf(it.getSourceCollection(), field);
          if (((index != null) && (!Objects.equals(index.getFamily().getLiteral(), "flat")))) {
            final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
            final String sql = this.schema.vectorIndex(it.getSourceCollection(), table, field, index, false);
            String _id = DuckdbSql.id(DuckdbSchema.indexName(it.getSourceCollection(), index.getName()));
            String _plus_4 = ("CREATE INDEX " + _id);
            String _targetName = it.getTargetName();
            String _plus_5 = (_targetName + "_");
            String _name = index.getName();
            String _plus_6 = (_plus_5 + _name);
            String _id_1 = DuckdbSql.id(_plus_6);
            String _plus_7 = ("CREATE INDEX " + _id_1);
            String _replace = sql.replace(_plus_4, _plus_7);
            String _table_2 = DuckdbSql.table(table);
            String _plus_8 = (" ON " + _table_2);
            String _plus_9 = (_plus_8 + " USING HNSW (");
            String _id_2 = DuckdbSql.id(column.name());
            String _plus_10 = (_plus_9 + _id_2);
            String _table_3 = DuckdbSql.table(copy);
            String _plus_11 = (" ON " + _table_3);
            String _plus_12 = (_plus_11 + " USING HNSW (");
            String _id_3 = DuckdbSql.id(column.name());
            String _plus_13 = (_plus_12 + _id_3);
            Code.Entries _kw_1 = DuckdbSchema.kw("sql", Code.text(_replace.replace(_plus_10, _plus_13)));
            steps.add(_kw_1);
          }
        }
      }
      final String refresh = DuckdbSql.createFtsIndex(copy);
      if ((refresh != null)) {
        Code.Entries _kw_1 = DuckdbSchema.kw("sql", Code.text(refresh));
        steps.add(_kw_1);
      }
      Code.Call _runtime = Code.runtime("transaction", DuckdbSchema.kw("steps", Code.items(steps)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A deployment is the database file the program attached as vecdsl: the target file is attached, COPY FROM DATABASE copies
   * schemas, tables, indexes, sequences, macros and data into it in one transaction, and it is detached after the commit
   * (DuckDB refuses DETACH while the transaction has work on it, measured). The result counts the tables copied.
   */
  private List<Code.Stmt> cloneDatabase(final String target) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String clone = "vecdsl_clone";
      String _literal = DuckdbSql.literal(DuckdbSql.localPath(target));
      String _plus = ("ATTACH " + _literal);
      String _plus_1 = (_plus + " AS ");
      String _plus_2 = (_plus_1 + clone);
      Code.Entries _kw = DuckdbSchema.kw("sql", Code.text(_plus_2));
      Code.Entries _kw_1 = DuckdbSchema.kw("sql", Code.text(("COPY FROM DATABASE vecdsl TO " + clone)));
      Code.Entries _kw_2 = DuckdbSchema.kw("sql", Code.text((("SELECT count(*) FROM duckdb_tables() WHERE database_name = \'" + clone) + "\' AND NOT starts_with(schema_name, \'fts_\')")));
      final List<? extends Code.Expr> steps = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _kw), _kw_1, _kw_2));
      Code.Call _runtime = Code.runtime("transaction", DuckdbSchema.kw("steps", Code.items(steps)));
      Code.Let _let = new Code.Let("copied", _runtime);
      Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(("DETACH " + clone))));
      Code.Ref _ref = new Code.Ref("copied");
      Code.Return _return = new Code.Return(_ref);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _let), _execute, _return));
    }
    return _xblockexpression;
  }

  /**
   * An HNSW or ART index is dropped and created again; the fts index of a table is rebuilt in place.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      boolean _isConcurrently = it.isConcurrently();
      if (_isConcurrently) {
        this.notes.add("concurrently on RebuildIndex, which DuckDB runs inside one transaction");
      }
      final VectorCollection collection = this.indexCollection(it.getIndex());
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      final DuckdbSql.Table table = this.schema.table(collection);
      final Index target = it.getIndex();
      String _switchResult = null;
      boolean _matched = false;
      if (target instanceof VectorIndex) {
        _matched=true;
        String _xblockexpression_1 = null;
        {
          VectorField _elvis = null;
          VectorField _field = ((VectorIndex)target).getField();
          if (_field != null) {
            _elvis = _field;
          } else {
            VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(target, VectorField.class);
            _elvis = _containerOfType;
          }
          final VectorField field = _elvis;
          if ((field == null)) {
            throw Unsupported.of("a vector index without a vector field");
          }
          _xblockexpression_1 = this.schema.vectorIndex(collection, table, field, ((VectorIndex)target), false);
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (target instanceof PayloadIndex) {
          _matched=true;
          _switchResult = this.schema.payloadIndex(collection, table, ((PayloadIndex)target), false);
        }
      }
      final String sql = _switchResult;
      if ((sql == null)) {
        String _createFtsIndex = DuckdbSql.createFtsIndex(table);
        Code.Stmt _execute = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_createFtsIndex)));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute));
      }
      String _qualified = DuckdbSql.qualified(table, DuckdbSchema.indexName(collection, it.getIndex().getName()));
      String _plus = ("DROP INDEX IF EXISTS " + _qualified);
      Code.Stmt _execute_1 = DuckdbSchema.execute(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus, sql)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_execute_1));
    }
    return _xblockexpression;
  }

  /**
   * The rows of a derived collection are replaced by the rows its file holds now, in one transaction, then the fts rebuild.
   */
  protected List<Code.Stmt> _body(final RefreshExternal it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = it.getCollection().getExternal();
      boolean _tripleEquals = (_external == null);
      if (_tripleEquals) {
        String _name = it.getCollection().getName();
        String _plus = ("refreshing " + _name);
        String _plus_1 = (_plus + ", which is not derived from an external source");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "async");
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final ExternalSource source = it.getCollection().getExternal();
      String _location = source.getLocation();
      String _xifexpression = null;
      boolean _written = ModelUtil.written(source, "format");
      if (_written) {
        _xifexpression = source.getFormat().getLiteral();
      } else {
        _xifexpression = null;
      }
      final String sql = DuckdbSql.insertFromFile(q, table, DuckdbSql.reader(_location, _xifexpression), 
        DuckdbSql.localPath(source.getLocation()), CollectionLiterals.<String, Code.Expr>newLinkedHashMap(), "error");
      final Code.Entries insert = DuckdbSchema.kw("sql", Code.text(sql), "params", q.params(), "counter", Code.text("refreshed"));
      final String refresh = DuckdbSql.createFtsIndex(table);
      if ((refresh != null)) {
        Code.Text _text = Code.text(refresh);
        insert.put("refresh", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _text)))));
      }
      String _table = DuckdbSql.table(table);
      String _plus_2 = ("DELETE FROM " + _table);
      Code.Entries _kw = DuckdbSchema.kw("sql", Code.text(_plus_2), "counter", Code.text("delete_count"));
      final List<? extends Code.Expr> steps = Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(((Code.Expr) _kw), insert));
      Code.Call _runtime = Code.runtime("transaction", DuckdbSchema.kw("steps", Code.items(steps)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        return this.dropSlice(it.getCollection(), table.namespace(), it.getNamespace());
      }
      String _table = DuckdbSql.table(table);
      String _plus = ("TRUNCATE " + _table);
      Code.Call _change = DuckdbSchema.change(_plus, null, "delete_count", table, false);
      Code.Return _return = new Code.Return(_change);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final Concept owner = it.getScope();
      String _switchResult = null;
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            String _xblockexpression_1 = null;
            {
              String _xifexpression = null;
              if ((owner instanceof Database)) {
                _xifexpression = ((Database) owner).getName();
              } else {
                _xifexpression = DuckdbSql.MAIN;
              }
              final String schemaName = _xifexpression;
              if ((((owner != null) && (!(owner instanceof VectorDatabase))) && (!(owner instanceof Database)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing collections in a " + _name);
                String _plus_1 = (_plus + "; DuckDB lists the tables of a schema");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = DuckdbQueries.collections(q, schemaName);
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
              _xblockexpression_2 = DuckdbQueries.databases();
            }
            _switchResult = _xblockexpression_2;
            break;
          case INDEXES:
            _switchResult = DuckdbQueries.indexes(q, this.scopeTable(owner, "indexes"), ((VectorCollection) owner).getName());
            break;
          case PARTITIONS:
            throw Unsupported.of(DuckdbStatements.NO_PARTITIONS);
          case ALIASES:
            String _xblockexpression_3 = null;
            {
              DuckdbSql.Table _xifexpression = null;
              if ((owner == null)) {
                _xifexpression = null;
              } else {
                _xifexpression = this.scopeTable(owner, "aliases");
              }
              final DuckdbSql.Table table = _xifexpression;
              String _elvis = null;
              String _schema = null;
              if (table!=null) {
                _schema=table.schema();
              }
              if (_schema != null) {
                _elvis = _schema;
              } else {
                _elvis = DuckdbSql.MAIN;
              }
              _xblockexpression_3 = DuckdbQueries.aliases(q, table, _elvis);
            }
            _switchResult = _xblockexpression_3;
            break;
          case TENANTS:
          case NAMESPACES:
            String _xblockexpression_4 = null;
            {
              final DuckdbSql.Table table = this.scopeTable(owner, it.getKind().getLiteral());
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
              _xblockexpression_4 = DuckdbQueries.slices(table, column);
            }
            _switchResult = _xblockexpression_4;
            break;
          case SNAPSHOTS:
            _switchResult = DuckdbQueries.snapshots(q);
            break;
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
      final String sql = DuckdbQueries.names(q, source, _xifexpression, _xifexpression_1);
      Code.Call _call = DuckdbSchema.call("value", sql, q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private DuckdbSql.Table scopeTable(final Concept owner, final String what) {
    DuckdbSql.Table _xblockexpression = null;
    {
      if ((!(owner instanceof VectorCollection))) {
        throw Unsupported.of((("listing " + what) + " without a collection"));
      }
      _xblockexpression = this.schema.table(((VectorCollection) owner));
    }
    return _xblockexpression;
  }

  /**
   * CREATE INDEX returns once the index is built, so awaiting one reads its state once.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state", "timeoutSeconds");
      final VectorCollection collection = this.indexCollection(it.getIndex());
      if ((collection == null)) {
        throw Unsupported.of("an index outside a collection");
      }
      final DuckdbSql.Query q = new DuckdbSql.Query();
      Code.Call _call = DuckdbSchema.call("value", DuckdbQueries.indexState(q, this.schema.table(collection), DuckdbSchema.indexName(collection, it.getIndex().getName())), q, null);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "elementScope", "asOf", "explain", "reranker", "queryModel", "highlighted");
      this.queryExtensions(it);
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression = it.getMetric().getLiteral();
      } else {
        _xifexpression = column.vec().metric();
      }
      final String metric = _xifexpression;
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final String query = this.queryVector(q, it.getInput(), it.getCollection(), table, column);
      final List<String> stages = CollectionLiterals.<String>newArrayList();
      final List<String> staged = this.prefetchStages(it, q, it.getCollection(), table, it.getPrefetches(), it.getInput(), field, stages);
      String _where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      String _within = DuckdbQueries.within(table, staged);
      final String where = DuckdbSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_where, _within)));
      final DuckdbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field);
      final boolean exhaustive = (it.isExact() || ((it.getParams() != null) && it.getParams().isExhaustive()));
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus = (_topK + _offsetOf);
      final Map<String, String> options = this.settings(it.getCollection(), field, it.getParams(), this.fetchOf(shape, _plus), exhaustive);
      String _xifexpression_1 = null;
      boolean _isEmpty = stages.isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = IterableExtensions.join(stages, ", ");
      }
      final String prelude = _xifexpression_1;
      int _topK_1 = it.getTopK();
      int _offsetOf_1 = this.offsetOf(it);
      DuckdbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final DuckdbQueries.Search search = new DuckdbQueries.Search(table, column, metric, query, prelude, _topK_1, _offsetOf_1, where, _projection, 
        (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf, exhaustive);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = DuckdbSchema.call(_xifexpression_2, DuckdbQueries.vectorSearch(q, search, shape), q, options);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker", "minShouldMatch", "fuzziness", "slop", "highlighted");
      this.queryExtensions(it);
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "timeoutMillis");
      }
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final List<DuckdbQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, it.getSearchFields());
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
      final String operator = this.keywordOperator(_xifexpression, _xifexpression_1);
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      DuckdbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final DuckdbQueries.Keywords keywords = new DuckdbQueries.Keywords(table, fields, operator, words, _topK, _offsetOf, where, _projection, _minOf, _maxOf);
      final DuckdbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = DuckdbSchema.call(_xifexpression_2, DuckdbQueries.keywordSearch(q, keywords, shape), q, this.extensionSettings(it));
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
      this.reject(it, "asOf", "explain", "reranker", "highlighted");
      this.queryExtensions(it);
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "timeoutMillis");
      }
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
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
      final List<DuckdbQueries.Leg> parts = CollectionLiterals.<DuckdbQueries.Leg>newArrayList();
      int index = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
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
          final List<DuckdbSql.Cond> legConditions = CollectionLiterals.<DuckdbSql.Cond>newArrayList();
          List<DuckdbSql.Cond> _parts = this.filterOf(it, it.getCollection(), table, it.getFilter()).parts();
          Iterables.<DuckdbSql.Cond>addAll(legConditions, _parts);
          Object _feature_1 = this.feature(leg, "filter");
          boolean _tripleNotEquals_1 = (_feature_1 != null);
          if (_tripleNotEquals_1) {
            Object _feature_2 = this.feature(leg, "filter");
            List<DuckdbSql.Cond> _parts_1 = new DuckdbFilter().where(((FilterBlock) _feature_2)).parts();
            Iterables.<DuckdbSql.Cond>addAll(legConditions, _parts_1);
          }
          final String legWhere = DuckdbSql.where(q, table, legConditions);
          Set<String> _keySet = ModelUtil.extParams(leg, DuckdbGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of((("extension parameter " + name) + " on a hybrid leg"));
          }
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            final VectorField field = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
            final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
            String _xifexpression_2 = null;
            boolean _written_2 = ModelUtil.written(leg, "metric");
            if (_written_2) {
              _xifexpression_2 = ((VectorLeg)leg).getMetric().getLiteral();
            } else {
              _xifexpression_2 = column.vec().metric();
            }
            final String metric = _xifexpression_2;
            QueryInput _elvis = null;
            QueryInput _input = ((VectorLeg)leg).getInput();
            if (_input != null) {
              _elvis = _input;
            } else {
              _elvis = shared;
            }
            final String query = this.queryVector(q, _elvis, it.getCollection(), table, column);
            options.putAll(this.settings(it.getCollection(), field, ((VectorLeg)leg).getParams(), (limit).intValue(), ((VectorLeg)leg).isExact()));
            Code.Expr _minOf = this.minOf(((VectorLeg)leg).getBounds());
            Code.Expr _maxOf = this.maxOf(((VectorLeg)leg).getBounds());
            boolean _isExact = ((VectorLeg)leg).isExact();
            DuckdbQueries.Leg _leg = new DuckdbQueries.Leg(column, metric, query, null, null, null, (limit).intValue(), legWhere, (weight).doubleValue(), _minOf, _maxOf, _isExact);
            parts.add(_leg);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              boolean _written_2 = ModelUtil.written(leg, "minShouldMatch");
              if (_written_2) {
                String _get = DuckdbStatements.REASONS.get("minShouldMatch");
                String _plus = ("the \'min-should-match\' clause; " + _get);
                throw Unsupported.of(_plus);
              }
              final List<DuckdbQueries.KeywordField> fields = this.keywordFields(it.getCollection(), table, ((KeywordLeg)leg).getSearchFields());
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
                _xifexpression_3 = "and";
              }
              final String operator = this.keywordOperator(_xifexpression_2, _xifexpression_3);
              QueryInput _elvis = null;
              QueryInput _input = ((KeywordLeg)leg).getInput();
              if (_input != null) {
                _elvis = _input;
              } else {
                _elvis = shared;
              }
              Code.Expr _keywordText = this.keywordText(_elvis);
              DuckdbQueries.Leg _leg = new DuckdbQueries.Leg(null, null, null, fields, operator, _keywordText, (limit).intValue(), legWhere, (weight).doubleValue(), null, null, false);
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
      DuckdbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final DuckdbQueries.Hybrid hybrid = new DuckdbQueries.Hybrid(table, parts, fusionKind, k, _topK_1, _offsetOf_1, _projection, _minOf, _maxOf);
      options.putAll(this.extensionSettings(it));
      final DuckdbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, null);
      String _xifexpression_2 = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression_2 = "value";
      } else {
        _xifexpression_2 = "query";
      }
      Code.Call _call = DuckdbSchema.call(_xifexpression_2, DuckdbQueries.hybridSearch(q, hybrid, shape), q, options);
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
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
   * The average-vector strategy over example records or vectors, computed in SQL; the examples are not returned.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "explain", "reranker", "highlighted");
      this.queryExtensions(it);
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final DuckdbSql.Table lookup = this.schema.table(_elvis);
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
      final DuckdbSql.Column lookupColumn = lookup.column(column.name());
      final DuckdbSql.Query q = new DuckdbSql.Query();
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
      final String prelude = DuckdbQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors);
      final List<String> parts = CollectionLiterals.<String>newArrayList();
      String _where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      parts.add(_where);
      if (((it.getLookupCollection() == null) || (it.getLookupCollection() == it.getCollection()))) {
        final List<Code.Expr> examples = CollectionLiterals.<Code.Expr>newArrayList();
        Iterables.<Code.Expr>addAll(examples, positiveIds);
        Iterables.<Code.Expr>addAll(examples, negativeIds);
        boolean _isEmpty_1 = examples.isEmpty();
        boolean _not = (!_isEmpty_1);
        if (_not) {
          String _keyIn = DuckdbSql.keyIn(q, table, Code.items(examples));
          String _plus = ("(" + _keyIn);
          String _plus_1 = (_plus + ") IS NOT TRUE");
          parts.add(_plus_1);
        }
      }
      final String where = DuckdbSql.and(parts);
      final DuckdbQueries.Shape shape = this.shapeOf(it, it.getCollection(), table, field);
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus_2 = (_topK + _offsetOf);
      final Map<String, String> options = this.settings(it.getCollection(), field, it.getParams(), this.fetchOf(shape, _plus_2), false);
      String _metric = column.vec().metric();
      int _topK_1 = it.getTopK();
      int _offsetOf_1 = this.offsetOf(it);
      DuckdbQueries.Projection _projection = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      Code.Expr _minOf = this.minOf(it.getBounds());
      Code.Expr _maxOf = this.maxOf(it.getBounds());
      final DuckdbQueries.Search search = new DuckdbQueries.Search(table, column, _metric, "(SELECT v FROM q)", prelude, _topK_1, _offsetOf_1, where, _projection, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), _minOf, _maxOf, false);
      String _xifexpression = null;
      boolean _wrapped = shape.wrapped();
      if (_wrapped) {
        _xifexpression = "value";
      } else {
        _xifexpression = "query";
      }
      Code.Call _call = DuckdbSchema.call(_xifexpression, DuckdbQueries.vectorSearch(q, search, shape), q, options);
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
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final DuckdbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, null).parts());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      Code.Call _call = DuckdbSchema.call("query", DuckdbQueries.getRows(q, table, Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), names, where), q, this.extensionSettings(it));
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
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      Code.Call _call = DuckdbSchema.call("value", DuckdbQueries.count(table, where), q, this.extensionSettings(it));
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
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final DuckdbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), this.excludedOf(it), it.isWithVectors());
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<String> _output = shape.output();
      Iterables.<String>addAll(names, _output);
      List<String> _vectors = shape.vectors();
      Iterables.<String>addAll(names, _vectors);
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      final List<DuckdbQueries.Order> order = CollectionLiterals.<DuckdbQueries.Order>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        {
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
          }
          DuckdbQueries.Ref _fieldRefOf = this.fieldRefOf(it.getCollection(), table, clause.getField(), "sort");
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          DuckdbQueries.Order _order = new DuckdbQueries.Order(_fieldRefOf, _equals);
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
      Code.Call _call = DuckdbSchema.call("query", DuckdbQueries.scroll(q, table, names, where, order, limit, this.offsetOf(it), cursorKey, Integer.valueOf(it.getSliceIndex()), slices), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final DuckdbQueries.Projection shape = this.projection(it.getCollection(), table, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      Code.Call _call = DuckdbSchema.call("query", DuckdbQueries.sample(table, shape.output(), where, it.getSampleSize(), first), q, this.extensionSettings(it));
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
      final DuckdbSql.Table table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
      final DuckdbSql.Query q = new DuckdbSql.Query();
      final String where = DuckdbSql.where(q, table, this.filterOf(it, it.getCollection(), table, it.getFilter()).parts());
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
      Code.Call _call = DuckdbSchema.call("query", DuckdbQueries.distanceMatrix(table, column, column.vec().metric(), where, sample, limit), q, this.extensionSettings(it));
      Code.Return _return = new Code.Return(_call);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + DuckdbStatements.NO_GENERATION));
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
          final String helper = ((owner + "_query_") + Integer.valueOf(index));
          this.current = helper;
          List<String> _parameterNames = ModelUtil.parameterNames(query);
          List<Code.Stmt> _body = this.body(query);
          ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
          ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList();
          Program.Method _method = new Program.Method(helper, _parameterNames, _body, _newArrayList, _newArrayList_1, true);
          this.helpers.add(_method);
          Code.Call _self = Code.self(helper);
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
    String _plus = (DuckdbStatements.NOT_MAPPED + _name);
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
  private DuckdbQueries.Projection projection(final VectorCollection collection, final DuckdbSql.Table table, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    DuckdbQueries.Projection _xblockexpression = null;
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
        if ((((field != collection.getPrimaryKey()) && (!excluded.contains(field))) && DuckdbSchema.present(table, field))) {
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
          boolean _present = DuckdbSchema.present(table, field_1);
          if (_present) {
            final String vectorName = table.column(ModelUtil.fieldName(field_1)).name();
            if (((!vectors.contains(vectorName)) && (!excluded.contains(field_1)))) {
              vectors.add(vectorName);
            }
          }
        }
      }
      _xblockexpression = new DuckdbQueries.Projection(fields, vectors);
    }
    return _xblockexpression;
  }

  /**
   * Search settings applied with SET for one statement: hnsw_ef_search of at least limit plus offset when the field has an
   * HNSW index, and the pre- or post-filtering of the vss index scan (hnsw_prefilter, on by default since vss 2026-09-03).
   */
  private Map<String, String> settings(final VectorCollection collection, final VectorField field, final SearchParams params, final int fetch, final boolean exact) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      final VectorIndex index = this.schema.indexOf(collection, field);
      final boolean hnsw = ((index != null) && (!Objects.equals(index.getFamily().getLiteral(), "flat")));
      if ((params != null)) {
        boolean _written = ModelUtil.written(params, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: hnsw_ef_search is an effort setting, with no declarable recall target");
        }
        boolean _written_1 = ModelUtil.written(params, "probes");
        if (_written_1) {
          throw Unsupported.of("probes; vss builds HNSW indexes, which have no lists to probe");
        }
        boolean _written_2 = ModelUtil.written(params, "scanBudget");
        if (_written_2) {
          throw Unsupported.of("scan-budget; the vss index scan has no limit on visited entries");
        }
        Set<String> _keySet = ModelUtil.extParams(params, DuckdbGenerator.VENDOR).keySet();
        for (final String name : _keySet) {
          throw Unsupported.of(("extension parameter " + name));
        }
        this.schema.note(params, "timeoutMillis", "oversampling", "rescore");
      }
      if ((exact || (!hnsw))) {
        if ((params != null)) {
          this.schema.note(params, "candidatePool", "filterStrategy");
        }
        return result;
      }
      int _xifexpression = (int) 0;
      if (((params != null) && ModelUtil.written(params, "candidatePool"))) {
        _xifexpression = params.getCandidatePool();
      } else {
        int _xifexpression_1 = (int) 0;
        boolean _written_3 = ModelUtil.written(index, "searchCandidates");
        if (_written_3) {
          _xifexpression_1 = index.getSearchCandidates();
        } else {
          _xifexpression_1 = 64;
        }
        _xifexpression = _xifexpression_1;
      }
      final int candidates = _xifexpression;
      if (((candidates < fetch) || ((params != null) && ModelUtil.written(params, "candidatePool")))) {
        result.put("hnsw_ef_search", String.valueOf(Math.max(candidates, fetch)));
      }
      String _xifexpression_2 = null;
      if (((params != null) && ModelUtil.written(params, "filterStrategy"))) {
        _xifexpression_2 = params.getFilterStrategy().getLiteral();
      } else {
        _xifexpression_2 = "auto";
      }
      final String strategy = _xifexpression_2;
      if (strategy != null) {
        switch (strategy) {
          case "post_filter":
            result.put("hnsw_prefilter", "false");
            break;
          case "iterative":
            throw Unsupported.of("the iterative filter strategy; the vss index scan filters before the graph search (hnsw_prefilter) or after it, without iterative scans");
          default:
            break;
        }
      } else {
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * DuckDB settings written in an 'extension duckdb' block cannot be named: the vendor id is not a VendorId of the grammar.
   */
  private Map<String, String> extensionSettings(final EObject host) {
    Map<String, String> _xblockexpression = null;
    {
      final Map<String, String> result = CollectionLiterals.<String, String>newLinkedHashMap();
      Set<String> _keySet = ModelUtil.extParams(host, DuckdbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses DuckDB lacks, with their reason, and clauses not generated yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = DuckdbStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String reason = DuckdbStatements.REASONS.get(name);
        if ((reason != null)) {
          throw Unsupported.of(((clause + "; ") + reason));
        }
        final String pending = DuckdbStatements.PENDING_CLAUSES.get(name);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + DuckdbStatements.PENDING) + " (") + pending) + ")"));
        }
        throw Unsupported.of(clause);
      }
    }
  }

  /**
   * Extension blocks name a vendor id; duckdb is not one of the grammar, so none applies here.
   */
  private void queryExtensions(final EObject host) {
    this.extensionSettings(host);
  }

  /**
   * The query vector as SQL: a bound vector, or the stored vector of a record (a scalar subquery).
   */
  private String queryVector(final DuckdbSql.Query q, final QueryInput input, final VectorCollection collection, final DuckdbSql.Table table, final DuckdbSql.Column column) {
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
            _switchResult = q.json(Literals.of(input.getValue()), column.vec().sql());
            break;
          case ID:
            String _xblockexpression_1 = null;
            {
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final DuckdbSql.Table source = this.schema.table(_elvis);
              final DuckdbSql.Column stored = source.column(column.name());
              String _id = DuckdbSql.id(stored.name());
              String _plus = ("(SELECT x." + _id);
              String _plus_1 = (_plus + " FROM ");
              String _table = DuckdbSql.table(source);
              String _plus_2 = (_plus_1 + _table);
              String _plus_3 = (_plus_2 + " x WHERE x.");
              String _id_1 = DuckdbSql.id(source.key());
              String _plus_4 = (_plus_3 + _id_1);
              String _plus_5 = (_plus_4 + " = ");
              String _bind = q.bind(Literals.of(input.getValue()), source.keyColumn().paramType());
              String _plus_6 = (_plus_5 + _bind);
              _xblockexpression_1 = (_plus_6 + " LIMIT 1)");
            }
            _switchResult = _xblockexpression_1;
            break;
          case SPARSE_VECTOR:
            throw Unsupported.of(("sparse vector input; " + DuckdbStatements.FLOAT_ONLY));
          case TEXT:
            throw Unsupported.of(("text input" + DuckdbStatements.NO_MODELS));
          case MULTI_VECTOR:
            throw Unsupported.of("multi-vector input; a DuckDB array column holds one vector");
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " input");
            String _plus_1 = (_plus + DuckdbStatements.NO_MODELS);
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " input");
        String _plus_1 = (_plus + DuckdbStatements.NO_MODELS);
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The result shape of a search: the keyset cursor, boosts, MMR, groups, total, aggregations and sort keys. The searched
   * vector field is null for keyword and hybrid searches.
   */
  private DuckdbQueries.Shape shapeOf(final EObject search, final VectorCollection collection, final DuckdbSql.Table table, final VectorField searched) {
    DuckdbQueries.Shape _xblockexpression = null;
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
      DuckdbQueries.Group _xifexpression = null;
      if ((grouping == null)) {
        _xifexpression = null;
      } else {
        DuckdbQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, grouping, "group-by");
        Integer _xifexpression_1 = null;
        boolean _written = ModelUtil.written(search, "groupSize");
        if (_written) {
          Object _feature_5 = this.feature(search, "groupSize");
          _xifexpression_1 = ((Integer) _feature_5);
        } else {
          _xifexpression_1 = Integer.valueOf(1);
        }
        _xifexpression = new DuckdbQueries.Group(_fieldRefOf, (_xifexpression_1).intValue());
      }
      final DuckdbQueries.Group group = _xifexpression;
      final List<DuckdbQueries.Aggregate> aggregates = CollectionLiterals.<DuckdbQueries.Aggregate>newArrayList();
      Object _elvis_1 = null;
      Object _feature_6 = this.feature(search, "aggregations");
      if (_feature_6 != null) {
        _elvis_1 = _feature_6;
      } else {
        ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
        _elvis_1 = _newArrayList_1;
      }
      for (final AggregationClause clause : ((List<AggregationClause>) _elvis_1)) {
        DuckdbQueries.Aggregate _aggregateOf = this.aggregateOf(collection, table, clause);
        aggregates.add(_aggregateOf);
      }
      final List<DuckdbQueries.Order> order = CollectionLiterals.<DuckdbQueries.Order>newArrayList();
      for (final SortClause clause_1 : sortClauses) {
        DuckdbQueries.Ref _xifexpression_2 = null;
        boolean _isByRelevance = clause_1.isByRelevance();
        if (_isByRelevance) {
          _xifexpression_2 = null;
        } else {
          _xifexpression_2 = this.fieldRefOf(collection, table, clause_1.getField(), "sort");
        }
        ESortDirection _direction = clause_1.getDirection();
        boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
        DuckdbQueries.Order _order = new DuckdbQueries.Order(_xifexpression_2, _equals);
        order.add(_order);
      }
      Code.Text _xifexpression_3 = null;
      if ((cursorText == null)) {
        _xifexpression_3 = null;
      } else {
        _xifexpression_3 = Code.text(cursorText);
      }
      final Code.Text cursorKey = _xifexpression_3;
      DuckdbQueries.Boost _xifexpression_4 = null;
      if ((boosting == null)) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = this.boostOf(collection, table, boosting);
      }
      DuckdbQueries.Mmr _xifexpression_5 = null;
      if ((diversity == null)) {
        _xifexpression_5 = null;
      } else {
        Object _feature_7 = this.feature(search, "topK");
        int _offsetOf = this.offsetOf(search);
        int _plus = ((((Integer) _feature_7)).intValue() + _offsetOf);
        _xifexpression_5 = this.mmrOf(collection, table, diversity, searched, _plus);
      }
      boolean _written_1 = ModelUtil.written(search, "withTotal");
      _xblockexpression = new DuckdbQueries.Shape(cursorKey, _xifexpression_4, _xifexpression_5, group, _written_1, aggregates, order);
    }
    return _xblockexpression;
  }

  /**
   * Prefetch stages as CTEs of the keys each stage retrieves (its own input, vector field, metric, filter and limit, ranking only
   * the rows its nested stages return); the names of the given stages, in order. A stage without input or field takes the search's.
   */
  private List<String> prefetchStages(final EObject search, final DuckdbSql.Query q, final VectorCollection collection, final DuckdbSql.Table table, final List<Prefetch> given, final QueryInput input, final VectorField field, final List<String> stages) {
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
          final DuckdbSql.Column stageColumn = table.column(ModelUtil.fieldName(stageField));
          String _xifexpression_1 = null;
          boolean _written = ModelUtil.written(stage, "metric");
          if (_written) {
            _xifexpression_1 = stage.getMetric().getLiteral();
          } else {
            _xifexpression_1 = stageColumn.vec().metric();
          }
          final String stageMetric = _xifexpression_1;
          final String stageQuery = this.queryVector(q, stageInput, collection, table, stageColumn);
          final String stageWhere = DuckdbSql.where(q, table, this.filterOf(search, collection, table, stage.getFilter()).parts());
          int _size = stages.size();
          final String name = ("stage_" + Integer.valueOf(_size));
          String _within = DuckdbQueries.within(table, inner);
          String _stage = DuckdbQueries.stage(name, table, stageColumn, stageMetric, stageQuery, DuckdbSql.and(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(stageWhere, _within))), stage.getStageLimit());
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
  private int fetchOf(final DuckdbQueries.Shape shape, final int fetch) {
    int _xifexpression = (int) 0;
    DuckdbQueries.Mmr _mmr = shape.mmr();
    boolean _tripleEquals = (_mmr == null);
    if (_tripleEquals) {
      _xifexpression = fetch;
    } else {
      _xifexpression = Math.max(shape.mmr().candidates(), fetch);
    }
    return _xifexpression;
  }

  /**
   * An aggregation over the matching records, named by its alias or kind_field as on the other targets.
   */
  private DuckdbQueries.Aggregate aggregateOf(final VectorCollection collection, final DuckdbSql.Table table, final AggregationClause clause) {
    DuckdbQueries.Aggregate _xblockexpression = null;
    {
      final String kind = clause.getKind().getLiteral();
      final DuckdbQueries.Ref source = this.fieldRefOf(collection, table, clause.getField(), "aggregations");
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
      DuckdbQueries.Aggregate _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case "range":
            throw Unsupported.of("range aggregations; vecDSL gives no range bounds to count records in");
          case "histogram":
            DuckdbQueries.Aggregate _xblockexpression_1 = null;
            {
              if (((clause.getInterval() == null) || (!clause.getInterval().matches("[0-9]+(\\.[0-9]+)?")))) {
                throw Unsupported.of("a histogram without a numeric interval");
              }
              Code.Num _num = Code.num(Double.parseDouble(clause.getInterval()));
              _xblockexpression_1 = new DuckdbQueries.Aggregate(name, kind, source, _num, size);
            }
            _switchResult = _xblockexpression_1;
            break;
          case "date_histogram":
            DuckdbQueries.Aggregate _xblockexpression_2 = null;
            {
              String _interval = clause.getInterval();
              boolean _tripleEquals = (_interval == null);
              if (_tripleEquals) {
                throw Unsupported.of("a date histogram without an interval");
              }
              Code.Text _text = Code.text(clause.getInterval());
              _xblockexpression_2 = new DuckdbQueries.Aggregate(name, kind, source, _text, size);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            _switchResult = new DuckdbQueries.Aggregate(name, kind, source, null, size);
            break;
        }
      } else {
        _switchResult = new DuckdbQueries.Aggregate(name, kind, source, null, size);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Boost functions: defaults weight 1, decay rate 0.5 and offset 0, combine and apply multiply.
   */
  private DuckdbQueries.Boost boostOf(final VectorCollection collection, final DuckdbSql.Table table, final BoostBlock block) {
    DuckdbQueries.Boost _xblockexpression = null;
    {
      final List<DuckdbQueries.BoostFn> functions = CollectionLiterals.<DuckdbQueries.BoostFn>newArrayList();
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
          final List<DuckdbSql.Cond> when = CollectionLiterals.<DuckdbSql.Cond>newArrayList();
          Condition _condition = fn.getCondition();
          boolean _tripleNotEquals = (_condition != null);
          if (_tripleNotEquals) {
            DuckdbSql.Cond _condition_1 = new DuckdbFilter().condition(fn.getCondition());
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
                DuckdbQueries.Ref _fieldRefOf = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
                DuckdbQueries.BoostFn _boostFn = new DuckdbQueries.BoostFn("field_value", _fieldRefOf, weight, null, null, null, null, 0.5, when);
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
                DuckdbQueries.Ref _fieldRefOf_1 = this.fieldRefOf(collection, table, fn.getTarget(), "boost");
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
                DuckdbQueries.BoostFn _boostFn_1 = new DuckdbQueries.BoostFn("decay", _fieldRefOf_1, weight, _literal, _of, _of_1, _xifexpression_2, rate, when);
                functions.add(_boostFn_1);
                break;
              case CONDITION:
                boolean _isEmpty = when.isEmpty();
                if (_isEmpty) {
                  throw Unsupported.of("a condition boost without a condition");
                }
                DuckdbQueries.BoostFn _boostFn_2 = new DuckdbQueries.BoostFn("condition", null, weight, null, null, null, null, 0.5, when);
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
      _xblockexpression = new DuckdbQueries.Boost(functions, _xifexpression, _xifexpression_1);
    }
    return _xblockexpression;
  }

  /**
   * MMR over a candidate pool (the given size, else ten times limit plus offset), diversified on the named, searched or only vector field.
   */
  private DuckdbQueries.Mmr mmrOf(final VectorCollection collection, final DuckdbSql.Table table, final MmrConfig config, final VectorField searched, final int fetch) {
    DuckdbQueries.Mmr _xblockexpression = null;
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
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(field));
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
      _xblockexpression = new DuckdbQueries.Mmr(_diversityLambda, pool, column, _metric);
    }
    return _xblockexpression;
  }

  /**
   * A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a JSON column.
   */
  private DuckdbQueries.Ref fieldRefOf(final VectorCollection collection, final DuckdbSql.Table table, final FieldPath path, final String purpose) {
    DuckdbQueries.Ref _xblockexpression = null;
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
      final DuckdbSql.Column column = table.column(ModelUtil.fieldName(path.getField()));
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        return new DuckdbQueries.Ref(column, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), null);
      }
      final Function1<Field, String> _function = (Field sub) -> {
        return ModelUtil.fieldName(sub);
      };
      List<String> _list = IterableExtensions.<String>toList(ListExtensions.<Field, String>map(path.getSubFields(), _function));
      String _leafType = DuckdbFilter.leafType(path);
      _xblockexpression = new DuckdbQueries.Ref(column, _list, _leafType);
    }
    return _xblockexpression;
  }

  /**
   * Keyword fields: the listed full-text columns with boosts, else every full-text column of the collection.
   */
  private List<DuckdbQueries.KeywordField> keywordFields(final VectorCollection collection, final DuckdbSql.Table table, final List<SearchFieldRef> refs) {
    List<DuckdbQueries.KeywordField> _xblockexpression = null;
    {
      final List<DuckdbQueries.KeywordField> result = CollectionLiterals.<DuckdbQueries.KeywordField>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<DuckdbSql.Column> _columns = table.columns();
        for (final DuckdbSql.Column column : _columns) {
          boolean _fullText = column.fullText();
          if (_fullText) {
            DuckdbQueries.KeywordField _keywordField = new DuckdbQueries.KeywordField(column, 1.0);
            result.add(_keywordField);
          }
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final DuckdbSql.Column column_1 = table.column(ModelUtil.fieldName(ref.getField()));
            boolean _fullText_1 = column_1.fullText();
            boolean _not = (!_fullText_1);
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
            DuckdbQueries.KeywordField _keywordField_1 = new DuckdbQueries.KeywordField(column_1, _xifexpression);
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
   * The match_bm25 form of a text query type: match with all terms (and) or any term (or).
   */
  private String keywordOperator(final String type, final String operator) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case "match":
          String _xifexpression = null;
          boolean _equals = Objects.equals(operator, "or");
          if (_equals) {
            _xifexpression = "or";
          } else {
            _xifexpression = "and";
          }
          _switchResult = _xifexpression;
          break;
        case "phrase":
        case "phrase_prefix":
          throw Unsupported.of(((("text-query " + type) + "; ") + DuckdbSql.NO_POSITIONS));
        case "term":
        case "query_string":
          throw Unsupported.of((("text-query " + type) + "; match_bm25 tokenizes, stems and filters the query text and has no query syntax or unanalyzed terms"));
        default:
          throw Unsupported.of((("text-query " + type) + "; match_bm25 matches whole stemmed terms, without fuzzy, prefix, wildcard or regular-expression terms"));
      }
    } else {
      throw Unsupported.of((("text-query " + type) + "; match_bm25 matches whole stemmed terms, without fuzzy, prefix, wildcard or regular-expression terms"));
    }
    return _switchResult;
  }

  private Code.Expr rows(final RecordSource source, final DuckdbSql.Table table) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", DuckdbSchema.kw("location", Code.text(((FileSource)source).getLocation())));
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

  private Code.Expr record(final RecordLiteral record, final DuckdbSql.Table table) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        {
          final DuckdbSql.Column column = table.column(ModelUtil.fieldName(entry.getField()));
          result.put(column.name(), Literals.of(entry.getValue()));
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
