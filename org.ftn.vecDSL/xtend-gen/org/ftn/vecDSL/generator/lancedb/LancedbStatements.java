package org.ftn.vecDSL.generator.lancedb;

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
import org.ftn.vecDSL.vecDSL.Drop;
import org.ftn.vecDSL.vecDSL.DropFieldOp;
import org.ftn.vecDSL.vecDSL.DropFunctionOp;
import org.ftn.vecDSL.vecDSL.DropSnapshot;
import org.ftn.vecDSL.vecDSL.EAuthType;
import org.ftn.vecDSL.vecDSL.ECloneMode;
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EConflictPolicy;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EFilterStrategy;
import org.ftn.vecDSL.vecDSL.EFusionType;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMatchOperator;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESnapshotScope;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.ETextQueryType;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
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
import org.ftn.vecDSL.vecDSL.ListObjects;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Load;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.MetricsConfig;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.OutputSpec;
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
import org.ftn.vecDSL.vecDSL.StorageConfig;
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
 * Maps each vecDSL statement to LanceDB operations in the host-language-neutral code model. Every operation is a call into
 * the per-language runtime module, which holds the SDK calls; the generator builds the table schemas, where strings,
 * update expressions, index plans and search plans, so the three host languages run the same operations on the same files.
 */
@SuppressWarnings("all")
public class LancedbStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; LancedbCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for LanceDB: ";

  /**
   * Suffix of the constructs LanceDB supports that are not generated yet.
   */
  public static final String PENDING = " has no LanceDB mapping yet";

  public static final String NO_MODELS = "; generated LanceDB programs supply their own vectors (embedding functions and rerankers of the SDK registries call model libraries or remote services)";

  public static final String NO_ALIASES = "aliases; LanceDB has no table aliases";

  public static final String NO_FUNCTIONS = "LanceDB computes no columns with server-side functions (full-text search is an index; embedding functions run in the client SDK)";

  public static final String NO_SPARSE = "the Lance type system has no sparse vector type";

  public static final String NO_TTL = "Lance rows do not expire";

  public static final String EXTERNAL = "a LanceDB table is a Lance dataset in its own directory";

  public static final String STORAGE = "an embedded LanceDB keeps each table in a directory of the storage URI";

  public static final String EMBEDDED = "the embedded LanceDB library runs inside the program on one machine";

  public static final String NO_AUTH = "an embedded library has no users or authentication (access is the file system\'s)";

  private static final String METRICS = "metrics configuration; the embedded library exports no metrics endpoint";

  private static final String SCHEDULES = "backup schedules; an embedded library runs no scheduler (tags are created by the program)";

  private static final String NO_TRANSACTIONS = "multi-statement transactions; LanceDB commits each write to one table as its own version (single-table commits are atomic)";

  private static final String NO_GENERATION = "LanceDB has no generation API (its queries return rows)";

  private static final String NO_RERANK = "rerankers; the reranker classes of the SDKs call model libraries or remote services";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model")));

  /**
   * Why LanceDB cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("explain", "LanceDB explains query plans (explain_plan), not scores"), Pair.<String, String>of("highlighted", "LanceDB full-text search returns no highlighted fragments"), Pair.<String, String>of("reranker", LancedbStatements.NO_RERANK), Pair.<String, String>of("elementScope", "a LanceDB query ranks rows, not list elements"), Pair.<String, String>of("queryModel", "generated LanceDB programs embed no query text")));

  /**
   * Clauses LanceDB can carry that are not generated yet: the runtime work they need.
   */
  private static final Map<String, String> LATER = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("cursor", "keyset pagination after the key of the last hit"), Pair.<String, String>of("withTotal", "count_rows of the where string beside the hits"), Pair.<String, String>of("mmr", "maximal marginal relevance over the candidates in the runtime"), Pair.<String, String>of("boost", "score functions over the hits in the runtime"), Pair.<String, String>of("aggregations", "aggregation over a scan in the runtime"), Pair.<String, String>of("prefetches", "staged nearest-neighbour queries")));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final LancedbSchema schema;

  private final Script script;

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private final Map<String, LancedbTable> tagged = CollectionLiterals.<String, LancedbTable>newHashMap();

  private String current;

  public LancedbStatements(final Script script) {
    this.script = script;
    LancedbSchema _lancedbSchema = new LancedbSchema(this.notes, script);
    this.schema = _lancedbSchema;
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
      List<Code.Stmt> _define = this.define(_elvis, it.isIfNotExists());
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

  public List<Code.Stmt> define(final Concept concept, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      boolean _matched = false;
      if (concept instanceof VectorCollection) {
        _matched=true;
        List<Code.Stmt> _createTable = this.schema.createTable(((VectorCollection)concept), ifNotExists);
        Iterables.<Code.Stmt>addAll(result, _createTable);
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
            throw Unsupported.of(LancedbStatements.METRICS);
          }
          Connection _connection = ((VectorDatabase)concept).getConnection();
          boolean _tripleNotEquals_1 = (_connection != null);
          if (_tripleNotEquals_1) {
            this.connection(((VectorDatabase)concept).getConnection());
          }
          EList<Cluster> _clusters = ((VectorDatabase)concept).getClusters();
          for (final Cluster cluster : _clusters) {
            List<Code.Stmt> _define = this.define(cluster, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _define);
          }
          EList<Database> _databases = ((VectorDatabase)concept).getDatabases();
          for (final Database database : _databases) {
            List<Code.Stmt> _define_1 = this.define(database, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _define_1);
          }
          AuthConfig _auth = ((VectorDatabase)concept).getAuth();
          boolean _tripleNotEquals_2 = (_auth != null);
          if (_tripleNotEquals_2) {
            List<Code.Stmt> _define_2 = this.define(((VectorDatabase)concept).getAuth(), ifNotExists);
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
            throw Unsupported.of(("cluster limits; " + LancedbStatements.EMBEDDED));
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + LancedbStatements.EMBEDDED));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, LancedbGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of(("extension parameter " + name));
          }
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createTable = this.schema.createTable(collection, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _createTable);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          LancedbSchema.checkName(((Database)concept).getName());
          Code.Stmt _run = LancedbStatements.run(Code.runtime("create_database", LancedbSchema.kw("database", Code.text(((Database)concept).getName()))));
          result.add(_run);
          EList<VectorCollection> _collections = ((Database)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createTable = this.schema.createTable(collection, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _createTable);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.NO_ALIASES);
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
          List<Code.Stmt> _createPayloadIndex = this.createPayloadIndex(((PayloadIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _createPayloadIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          List<Code.Stmt> _addField = this.addField(this.member(concept, "a vector field"), ((Field)concept));
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
          List<Code.Stmt> _requireTable = this.requireTable(this.member(concept, "a partition"));
          Iterables.<Code.Stmt>addAll(result, _requireTable);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _requireTable = this.requireTable(this.member(concept, "a tenant"));
          Iterables.<Code.Stmt>addAll(result, _requireTable);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _requireTable = this.requireTable(this.member(concept, "a namespace"));
          Iterables.<Code.Stmt>addAll(result, _requireTable);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _plus_1 = (_plus + LancedbStatements.NO_FUNCTIONS);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; LanceDB full-text indexes read no dictionaries or synonym files");
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          this.schema.tokenizer(((Analyzer)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer != null)) {
            this.schema.tokenizer(analyzer);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("a field limit outside a field declaration; Lance tables have no constraints");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + LancedbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + LancedbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.NO_RERANK);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization outside a vector index; LanceDB quantizes vectors in the index (IVF_PQ, IVF_RQ, IVF_HNSW_SQ, IVF_HNSW_PQ)");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(("storage settings; " + LancedbStatements.STORAGE));
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
          throw Unsupported.of(("role bindings; " + LancedbStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(("cluster limits; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          String _literal = ((CollectionLimit)concept).getType().getLiteral();
          String _plus = ("collection limits (" + _literal);
          String _plus_1 = (_plus + "); LanceDB OSS has no table limits");
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (LancedbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The storage URI: checked here, opened by connect(). A remote URI (db://) needs LanceDB Cloud or Enterprise.
   */
  private void connection(final Connection concept) {
    String _host = concept.getHost();
    boolean _tripleNotEquals = (_host != null);
    if (_tripleNotEquals) {
      throw Unsupported.of((("\'host\' connections; " + LancedbStatements.EMBEDDED) + " (LanceDB Cloud and Enterprise are reached through db:// URIs and an API key)"));
    }
    if (((concept.getUri() != null) && concept.getUri().startsWith("db://"))) {
      throw Unsupported.of("LanceDB Cloud and Enterprise URIs (db://); the generated programs use the embedded library");
    }
    if ((((concept.getDeployment() != null) && ModelUtil.written(concept, "deployment")) && (!Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED)))) {
      String _literal = concept.getDeployment().getLiteral();
      String _plus = ("deployment " + _literal);
      String _plus_1 = (_plus + "; ");
      String _plus_2 = (_plus_1 + LancedbStatements.EMBEDDED);
      throw Unsupported.of(_plus_2);
    }
    if (((((concept.getApiKey() != null) || (concept.getToken() != null)) || (concept.getPassword() != null)) || ModelUtil.written(concept, "username"))) {
      throw Unsupported.of(("credentials; " + LancedbStatements.NO_AUTH));
    }
    if ((concept.isTls() || ModelUtil.written(concept, "grpcPort"))) {
      throw Unsupported.of(("tls and grpc-port; " + LancedbStatements.EMBEDDED));
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals_1 = (_databaseName != null);
    if (_tripleNotEquals_1) {
      LancedbSchema.checkName(concept.getDatabaseName());
    }
    Set<String> _keySet = ModelUtil.extParams(concept, LancedbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  private void authentication(final AuthConfig auth) {
    EAuthType _type = auth.getType();
    boolean _notEquals = (!Objects.equals(_type, EAuthType.NONE));
    if (_notEquals) {
      String _literal = auth.getType().getLiteral();
      String _plus = (_literal + " authentication; ");
      String _plus_1 = (_plus + LancedbStatements.NO_AUTH);
      throw Unsupported.of(_plus_1);
    }
    if ((((((auth.getCertificatePath() != null) || (auth.getCmekKeyId() != null)) || auth.isAuditLog()) || auth.isFieldEncryption()) || (!auth.getRoles().isEmpty()))) {
      throw Unsupported.of(("certificates, customer-managed keys, audit logs, field encryption and roles; " + LancedbStatements.NO_AUTH));
    }
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
    final VectorCollection target = ModelUtil.targetCollection(concept);
    if ((target != null)) {
      return target;
    }
    throw Unsupported.of((what + " outside a collection; LanceDB adds it only to a table"));
  }

  private List<Code.Stmt> requireTable(final VectorCollection collection) {
    Code.Stmt _run = LancedbStatements.run(Code.runtime("require_table", LancedbSchema.kw("table", this.schema.table(collection).place())));
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  private List<Code.Stmt> createVectorIndex(final VectorIndex index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorField _elvis = null;
      VectorField _field = index.getField();
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
      final LancedbTable table = this.schema.table(collection);
      String _elvis_1 = null;
      String _get = table.vectors().get(this.schema.column(field));
      if (_get != null) {
        _elvis_1 = _get;
      } else {
        String _metricName = this.schema.metricName(field, index);
        _elvis_1 = _metricName;
      }
      final Code.Entries plan = this.schema.vectorIndex(index, field, _elvis_1);
      this.schema.evolve(collection, table.withIndex(plan));
      Code.Stmt _run = LancedbStatements.run(Code.runtime("create_index", LancedbSchema.kw("table", table.place(), "index", plan)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createPayloadIndex(final PayloadIndex index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = index.getTarget().getField();
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
        throw Unsupported.of("a payload index on a raw path outside a collection");
      }
      final LancedbTable table = this.schema.table(collection);
      final Code.Entries plan = this.schema.payloadIndex(collection, index);
      this.schema.evolve(collection, table.withIndex(plan));
      Code.Stmt _run = LancedbStatements.run(Code.runtime("create_index", LancedbSchema.kw("table", table.place(), "index", plan)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A field added to an existing table: a nullable column; a default fills existing rows through a SQL expression.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      final String name = this.schema.column(field);
      boolean _has = table.has(name);
      if (_has) {
        throw Unsupported.of((("adding field " + name) + ", which the table already has"));
      }
      final Code.Entries column = this.schema.addedField(collection, field);
      final Code.Entries args = LancedbSchema.kw("table", table.place(), "fields", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(column))));
      if (((field instanceof VectorField) && (((VectorField) field).getIndex() != null))) {
        throw Unsupported.of("a vector index declared with an added field; LanceDB indexes existing columns (create the index after the field)");
      }
      LancedbTable next = table.withField(column);
      if ((field instanceof VectorField)) {
        next = next.withVector(name, this.schema.metricName(((VectorField)field), null));
      }
      if ((field instanceof PayloadField)) {
        Literal _defaultValue = ((PayloadField)field).getDefaultValue();
        boolean _tripleNotEquals = (_defaultValue != null);
        if (_tripleNotEquals) {
          final LancedbSql sql = new LancedbSql("d");
          final String value = sql.value(Literals.of(((PayloadField)field).getDefaultValue()), false);
          boolean _constant = LancedbSql.constant(Literals.of(((PayloadField)field).getDefaultValue()));
          boolean _not = (!_constant);
          if (_not) {
            throw Unsupported.of("a default from a parameter; the generator writes the default into the column expression");
          }
          String _castType = this.castType(((PayloadField)field));
          String _plus = ((("CAST(" + value) + " AS ") + _castType);
          String _plus_1 = (_plus + ")");
          args.put("values", LancedbSchema.kw(name, Code.text(_plus_1)));
        }
        boolean _searchable = this.schema.searchable(((PayloadField)field));
        if (_searchable) {
          final Code.Entries plan = this.schema.fts((name + "_fts"), name, ((PayloadField)field).getAnalyzer());
          next = next.withIndex(plan);
          this.schema.evolve(collection, next);
          Code.Stmt _run = LancedbStatements.run(Code.runtime("add_columns", args));
          Code.Stmt _run_1 = LancedbStatements.run(Code.runtime("create_index", LancedbSchema.kw("table", table.place(), "index", plan)));
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
        }
      }
      this.schema.evolve(collection, next);
      Code.Stmt _run_2 = LancedbStatements.run(Code.runtime("add_columns", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_2));
    }
    return _xblockexpression;
  }

  /**
   * The SQL type of a default expression (DataFusion names).
   */
  private String castType(final PayloadField field) {
    String _switchResult = null;
    EScalarDType _type = field.getType();
    if (_type != null) {
      switch (_type) {
        case INT:
          _switchResult = "INT";
          break;
        case BIGINT:
          _switchResult = "BIGINT";
          break;
        case INT8:
          _switchResult = "TINYINT";
          break;
        case FLOAT:
          _switchResult = "FLOAT";
          break;
        case DOUBLE:
          _switchResult = "DOUBLE";
          break;
        case BOOL:
          _switchResult = "BOOLEAN";
          break;
        case VARCHAR:
        case TEXT:
        case UUID:
          _switchResult = "VARCHAR";
          break;
        case DATE:
          _switchResult = "DATE";
          break;
        case DATETIME:
          _switchResult = "TIMESTAMP";
          break;
        default:
          String _literal = field.getType().getLiteral();
          String _plus = ("a default on a " + _literal);
          String _plus_1 = (_plus + " field; the generator writes defaults of scalar columns");
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal = field.getType().getLiteral();
      String _plus = ("a default on a " + _literal);
      String _plus_1 = (_plus + " field; the generator writes defaults of scalar columns");
      throw Unsupported.of(_plus_1);
    }
    return _switchResult;
  }

  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      final String name = this.schema.column(field);
      String _key = table.key();
      boolean _equals = Objects.equals(name, _key);
      if (_equals) {
        throw Unsupported.of("dropping the primary key; the generator keys rows by it");
      }
      boolean _has = table.has(name);
      boolean _not = (!_has);
      if (_not) {
        throw Unsupported.of((("dropping field " + name) + ", which the table no longer has"));
      }
      this.schema.evolve(collection, table.withoutField(name));
      Code.Stmt _run = LancedbStatements.run(Code.runtime("drop_columns", LancedbSchema.kw("table", table.place(), "names", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name))))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> renameField(final VectorCollection collection, final RenameFieldOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      final String name = this.schema.column(operation.getField());
      boolean _has = table.has(name);
      boolean _not = (!_has);
      if (_not) {
        throw Unsupported.of((("renaming field " + name) + ", which the table no longer has"));
      }
      boolean _has_1 = table.has(operation.getNewName());
      if (_has_1) {
        String _newName = operation.getNewName();
        String _plus = ((("renaming field " + name) + " to ") + _newName);
        String _plus_1 = (_plus + ", which the table already has");
        throw Unsupported.of(_plus_1);
      }
      this.schema.evolve(collection, table.withRenamedField(name, operation.getNewName()));
      this.schema.rename(operation.getField(), operation.getNewName());
      Code.Stmt _run = LancedbStatements.run(Code.runtime("rename_column", LancedbSchema.kw("table", table.place(), "name", Code.text(name), "new_name", Code.text(operation.getNewName()))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description and tags live in the metadata of the key column.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      final Code.Expr value = Literals.of(operation.getValue());
      List<Code.Stmt> _switchResult = null;
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case DESCRIPTION:
            Code.Stmt _run = LancedbStatements.run(Code.runtime("set_metadata", LancedbSchema.kw("table", table.layout(), "metadata", LancedbSchema.kw(LancedbSchema.DESCRIPTION, value))));
            _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            break;
          case TAGS:
            Code.Stmt _run_1 = LancedbStatements.run(Code.runtime("set_metadata", LancedbSchema.kw("table", table.layout(), "metadata", LancedbSchema.kw(LancedbSchema.TAGS, value))));
            _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_1));
            break;
          case DYNAMIC:
            List<Code.Stmt> _xblockexpression_1 = null;
            {
              if (((!(value instanceof Code.Flag)) || ((Code.Flag) value).value())) {
                throw Unsupported.of("dynamic true; a Lance table has a declared Arrow schema (add-field adds columns)");
              }
              _xblockexpression_1 = this.requireTable(collection);
            }
            _switchResult = _xblockexpression_1;
            break;
          case CONSISTENCY:
            List<Code.Stmt> _xblockexpression_2 = null;
            {
              this.notes.add("consistency on SetPropertyOp, since every read checks for newer versions (read_consistency_interval 0)");
              _xblockexpression_2 = this.requireTable(collection);
            }
            _switchResult = _xblockexpression_2;
            break;
          case TTL:
            throw Unsupported.of(("collection ttl; " + LancedbStatements.NO_TTL));
          case DELETION_PROTECTION:
            throw Unsupported.of("deletion-protection; a LanceDB table is a directory that drop_table removes");
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; LanceDB alters tables");
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
        LancedbSchema.checkName(it.getNewName());
        final LancedbTable table = this.schema.table(collection);
        Code.Stmt _run = LancedbStatements.run(Code.runtime("rename_table", LancedbSchema.kw("table", table.place(), "new_name", Code.text(it.getNewName()), "schema", table.schema(), "indexes", table.indexPlans())));
        result.add(_run);
        this.schema.evolve(collection, table.renamed(it.getNewName()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
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
        _switchResult = this.renameField(collection, ((RenameFieldOp)operation));
      }
    }
    if (!_matched) {
      if (operation instanceof SetPropertyOp) {
        _matched=true;
        _switchResult = this.setProperty(collection, ((SetPropertyOp)operation));
      }
    }
    if (!_matched) {
      if (operation instanceof AddAliasOp) {
        _matched=true;
        throw Unsupported.of(LancedbStatements.NO_ALIASES);
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + LancedbStatements.NO_FUNCTIONS);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + LancedbStatements.NO_FUNCTIONS);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (LancedbStatements.NOT_MAPPED + _name);
      throw Unsupported.of(_plus);
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
          final Code.Entries args = LancedbSchema.kw("table", this.schema.placeOf(((VectorCollection)concept)));
          this.schema.evolve(((VectorCollection)concept), null);
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            args.put("missing_ok", Code.flag(true));
          }
          Code.Stmt _run = LancedbStatements.run(Code.runtime("drop_table", args));
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            LancedbSchema.checkName(((Database)concept).getName());
            Code.Stmt _run = LancedbStatements.run(Code.runtime("drop_database", LancedbSchema.kw("database", Code.text(((Database)concept).getName()))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
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
        if (concept instanceof Partition) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a partition"), LancedbSchema.PARTITION_COLUMN, ((Partition)concept).getName());
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
          _switchResult = this.dropField(this.member(concept, "a vector field"), ((Field)concept));
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
            final VectorField field = _elvis;
            if ((field == null)) {
              throw Unsupported.of("a vector index without a vector field");
            }
            _xblockexpression_1 = this.dropIndex(this.member(field, "a vector index"), ((VectorIndex)concept).getName());
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final Field field = ((PayloadIndex)concept).getTarget().getField();
            VectorCollection _elvis = null;
            VectorCollection _targetCollection = ModelUtil.targetCollection(concept);
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
              throw Unsupported.of("a payload index on a raw path outside a collection");
            }
            _xblockexpression_1 = this.dropIndex(collection, ((PayloadIndex)concept).getName());
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _plus_1 = (_plus + LancedbStatements.NO_FUNCTIONS);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; drop the vector index that quantizes");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the storage URI of the program, not a LanceDB object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; LanceDB drops tables and logical databases");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; an analyzer is the options of FTS indexes (drop the index)");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; an analyzer is the options of FTS indexes (drop the index)");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("dropping a field limit; Lance tables have no constraints");
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; LanceDB full-text indexes read no dictionaries or synonym files");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + LancedbStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + LancedbStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.NO_RERANK);
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(("storage settings; " + LancedbStatements.STORAGE));
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication; " + LancedbStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings; " + LancedbStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(LancedbStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(("cluster limits; " + LancedbStatements.EMBEDDED));
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of("collection limits; LanceDB OSS has no table limits");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((LancedbStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropIndex(final VectorCollection collection, final String index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      this.schema.evolve(collection, table.withoutIndex(index));
      Code.Stmt _run = LancedbStatements.run(Code.runtime("drop_index", LancedbSchema.kw("table", table.place(), "name", Code.text(index))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant, namespace or partition deletes its rows.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String column, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(collection);
      if ((column == null)) {
        throw Unsupported.of("a slice the collection does not declare");
      }
      String _id = LancedbSql.id(column);
      String _plus = (_id + " = ");
      String _text = LancedbSql.text(value);
      String _plus_1 = (_plus + _text);
      Code.Call _runtime = Code.runtime("delete", LancedbSchema.kw("table", table.layout(), "where", Code.text(_plus_1)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    throw Unsupported.of(LancedbStatements.NO_ALIASES);
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(("per-statement ttl; " + LancedbStatements.NO_TTL));
      }
      this.schema.note(it, "recordCountHint");
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = this.writeArgs(it, table, it.getCollection());
      args.put("rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "onConflict");
      if (_written_1) {
        args.put("on_conflict", Code.text(this.conflict(it.getOnConflict())));
      }
      this.writeOptions(it);
      Code.Call _runtime = Code.runtime("insert", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private String conflict(final EConflictPolicy policy) {
    String _switchResult = null;
    if (policy != null) {
      switch (policy) {
        case ERROR:
          _switchResult = "error";
          break;
        case IGNORE:
          _switchResult = "skip";
          break;
        case UPDATE:
        case REPLACE:
          _switchResult = "update";
          break;
        default:
          String _literal = policy.getLiteral();
          String _plus = ("on-conflict " + _literal);
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = policy.getLiteral();
      String _plus = ("on-conflict " + _literal);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of(("per-statement ttl; " + LancedbStatements.NO_TTL));
      }
      this.schema.note(it, "recordCountHint");
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = this.writeArgs(it, table, it.getCollection());
      args.put("rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      this.writeOptions(it);
      Code.Call _runtime = Code.runtime("upsert", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Table, schema, field defaults and the slice values a write puts into the tenant, namespace and partition columns.
   */
  private Code.Entries writeArgs(final EObject statement, final LancedbTable table, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "schema", table.schema());
      final Code.Entries defaults = Code.entries();
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(((it.getDefaultValue() != null) && table.has(this.schema.column(it))));
      };
      Iterable<PayloadField> _filter = IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(collection), _function);
      for (final PayloadField field : _filter) {
        defaults.put(this.schema.column(field), Literals.of(field.getDefaultValue()));
      }
      boolean _isEmpty = defaults.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("defaults", defaults);
      }
      final Code.Entries slice = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        slice.put(table.tenant(), Code.text(tenant.getName()));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        slice.put(table.namespace(), Code.text(namespace));
      }
      Object _feature_2 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_2);
      if ((partition != null)) {
        slice.put(LancedbSchema.PARTITION_COLUMN, Code.text(partition.getName()));
      } else {
        String _partition = table.partition();
        boolean _tripleNotEquals = (_partition != null);
        if (_tripleNotEquals) {
          slice.put(LancedbSchema.PARTITION_COLUMN, Code.text(this.defaultPartition(collection)));
        }
      }
      boolean _isEmpty_1 = slice.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("slice", slice);
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private String defaultPartition(final VectorCollection collection) {
    String _xblockexpression = null;
    {
      Partition _elvis = null;
      Partition _defaultPartition = collection.getDefaultPartition();
      if (_defaultPartition != null) {
        _elvis = _defaultPartition;
      } else {
        final Function1<Partition, Boolean> _function = (Partition it) -> {
          return Boolean.valueOf(it.isIsDefault());
        };
        Partition _findFirst = IterableExtensions.<Partition>findFirst(ModelUtil.partitions(collection), _function);
        _elvis = _findFirst;
      }
      final Partition declared = _elvis;
      if ((declared == null)) {
        throw Unsupported.of("a write without a partition to a partitioned collection without a default partition");
      }
      _xblockexpression = declared.getName();
    }
    return _xblockexpression;
  }

  /**
   * Updates by id or filter. Scalar values become SQL expressions of one UPDATE (literals quoted by the generator,
   * parameters bound by the runtime); vector, list and struct values, replace mode and update from records rewrite the
   * matching rows through a merge insert on the key. array-append and array-remove are array_append and array_remove_all.
   */
  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        throw Unsupported.of("if-version; Lance versions whole tables, not records (a write commits a new table version)");
      }
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "u");
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        where.keys(table.key(), IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals = (_source != null);
      if (_tripleNotEquals) {
        if (((((it.getValues() != null) || (!it.getRemovedFields().isEmpty())) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty()))) {
          throw Unsupported.of("update from records with \'set\', remove-fields or array operations");
        }
        final Code.Entries args = LancedbSchema.kw("table", table.layout(), "schema", table.schema(), "rows", this.rows(it.getSource()));
        boolean _isEmpty_1 = where.isEmpty();
        boolean _not_1 = (!_isEmpty_1);
        if (_not_1) {
          args.put("where", where.code());
        }
        Code.Call _runtime = Code.runtime("update_from", args);
        Code.Return _return = new Code.Return(_runtime);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      boolean _isEmpty_2 = where.isEmpty();
      if (_isEmpty_2) {
        throw Unsupported.of("an update without ids or a filter");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        if ((((!it.getRemovedFields().isEmpty()) || (!it.getAppendFields().isEmpty())) || (!it.getArrayRemoveFields().isEmpty()))) {
          throw Unsupported.of("replace mode with remove-fields or array operations");
        }
        Code.Entries _layout = table.layout();
        Code.Items _schema = table.schema();
        Code.Expr _code = where.code();
        Code.Entries _xifexpression = null;
        RecordLiteral _values = it.getValues();
        boolean _tripleEquals = (_values == null);
        if (_tripleEquals) {
          _xifexpression = Code.entries();
        } else {
          _xifexpression = this.record(it.getValues());
        }
        final Code.Entries args_1 = LancedbSchema.kw("table", _layout, "schema", _schema, "where", _code, "values", _xifexpression);
        Code.Call _runtime_1 = Code.runtime("replace_rows", args_1);
        Code.Return _return_1 = new Code.Return(_runtime_1);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      final LancedbSql sql = new LancedbSql("v");
      final Code.Entries expressions = Code.entries();
      final Code.Entries rewritten = Code.entries();
      EList<Field> _appendFields = it.getAppendFields();
      EList<Field> _arrayRemoveFields = it.getArrayRemoveFields();
      final Iterable<Field> arrays = Iterables.<Field>concat(_appendFields, _arrayRemoveFields);
      RecordLiteral _values_1 = it.getValues();
      boolean _tripleNotEquals_1 = (_values_1 != null);
      if (_tripleNotEquals_1) {
        EList<RecordEntry> _entries = it.getValues().getEntries();
        for (final RecordEntry entry : _entries) {
          {
            final String columnName = this.schema.column(entry.getField());
            String _key = table.key();
            boolean _equals = Objects.equals(columnName, _key);
            if (_equals) {
              throw Unsupported.of("updating the primary key; the generator keys rows by it");
            }
            boolean _contains = IterableExtensions.<Field>toList(arrays).contains(entry.getField());
            boolean _not_2 = (!_contains);
            if (_not_2) {
              boolean _scalarField = this.scalarField(entry.getField());
              if (_scalarField) {
                expressions.put(columnName, this.sqlValue(sql, Literals.of(entry.getValue())));
              } else {
                rewritten.put(columnName, Literals.of(entry.getValue()));
              }
            }
          }
        }
      }
      EList<Field> _removedFields = it.getRemovedFields();
      for (final Field field : _removedFields) {
        {
          final String columnName = this.schema.column(field);
          String _key = table.key();
          boolean _equals = Objects.equals(columnName, _key);
          if (_equals) {
            throw Unsupported.of("removing the primary key");
          }
          expressions.put(columnName, Code.text("NULL"));
        }
      }
      EList<Field> _appendFields_1 = it.getAppendFields();
      for (final Field field_1 : _appendFields_1) {
        expressions.put(this.schema.column(field_1), this.arrayExpression(it, "array_concat", field_1));
      }
      EList<Field> _arrayRemoveFields_1 = it.getArrayRemoveFields();
      for (final Field field_2 : _arrayRemoveFields_1) {
        expressions.put(this.schema.column(field_2), this.arrayExpression(it, "array_remove", field_2));
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      boolean _isEmpty_3 = rewritten.entries().isEmpty();
      boolean _not_2 = (!_isEmpty_3);
      if (_not_2) {
        boolean _isEmpty_4 = expressions.entries().isEmpty();
        boolean _not_3 = (!_isEmpty_4);
        if (_not_3) {
          throw Unsupported.of("one update of scalar and vector, list or struct values; the generator writes them in separate updates");
        }
        Code.Call _runtime_2 = Code.runtime("patch_rows", LancedbSchema.kw("table", table.layout(), "schema", table.schema(), "where", where.code(), "values", rewritten));
        Code.Return _return_2 = new Code.Return(_runtime_2);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
      }
      boolean _isEmpty_5 = expressions.entries().isEmpty();
      if (_isEmpty_5) {
        throw Unsupported.of("an update without values");
      }
      Code.Call _runtime_3 = Code.runtime("update", LancedbSchema.kw("table", table.layout(), "where", where.code(), "values", expressions));
      Code.Return _return_3 = new Code.Return(_runtime_3);
      result.add(_return_3);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private boolean scalarField(final Field field) {
    return (((field instanceof PayloadField) && ((PayloadField) field).getSubFields().isEmpty()) && (!Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.ARRAY, EScalarDType.STRUCT)).contains(((PayloadField) field).getType())));
  }

  private Code.Expr sqlValue(final LancedbSql sql, final Code.Expr value) {
    Record _xblockexpression = null;
    {
      final String token = sql.value(value, false);
      Record _xifexpression = null;
      boolean _constant = LancedbSql.constant(value);
      if (_constant) {
        _xifexpression = Code.text(token);
      } else {
        int _length = token.length();
        int _minus = (_length - 1);
        String _substring = token.substring(1, _minus);
        Pair<String, Code.Expr> _mappedTo = Pair.<String, Code.Expr>of(_substring, value);
        LinkedHashMap<String, Code.Expr> _newLinkedHashMap = CollectionLiterals.<String, Code.Expr>newLinkedHashMap(_mappedTo);
        _xifexpression = new Code.Filter(token, _newLinkedHashMap);
      }
      _xblockexpression = ((Record)_xifexpression);
    }
    return ((Code.Expr)_xblockexpression);
  }

  /**
   * array_append over every item of the set record's list (array_concat), or array_remove_all of each item.
   */
  private Code.Expr arrayExpression(final Update update, final String function, final Field field) {
    Code.Text _xblockexpression = null;
    {
      if (((!(field instanceof PayloadField)) || (!Objects.equals(((PayloadField) field).getType(), EScalarDType.ARRAY)))) {
        String _column = this.schema.column(field);
        String _plus = ("array-append or array-remove on " + _column);
        String _plus_1 = (_plus + ", which is not an array field");
        throw Unsupported.of(_plus_1);
      }
      RecordLiteral _values = update.getValues();
      EList<RecordEntry> _entries = null;
      if (_values!=null) {
        _entries=_values.getEntries();
      }
      RecordEntry _findFirst = null;
      if (_entries!=null) {
        final Function1<RecordEntry, Boolean> _function = (RecordEntry e) -> {
          Field _field = e.getField();
          return Boolean.valueOf((_field == field));
        };
        _findFirst=IterableExtensions.<RecordEntry>findFirst(_entries, _function);
      }
      final RecordEntry entry = _findFirst;
      final String name = LancedbSql.id(this.schema.column(field));
      if ((entry == null)) {
        String _column_1 = this.schema.column(field);
        String _plus_2 = ("array-append or array-remove on " + _column_1);
        String _plus_3 = (_plus_2 + " without items in \'set\'");
        throw Unsupported.of(_plus_3);
      }
      final Code.Expr items = Literals.of(entry.getValue());
      if (((!LancedbSql.constant(items)) || (!(items instanceof Code.Items)))) {
        throw Unsupported.of("array items that are not a list literal");
      }
      String expression = name;
      boolean _equals = Objects.equals(function, "array_concat");
      if (_equals) {
        String _literal = LancedbSql.literal(items);
        String _plus_4 = ((("array_concat(" + name) + ", ") + _literal);
        String _plus_5 = (_plus_4 + ")");
        expression = _plus_5;
      } else {
        List<Code.Expr> _items = ((Code.Items) items).items();
        for (final Code.Expr item : _items) {
          String _literal_1 = LancedbSql.literal(item);
          String _plus_6 = ((("array_remove_all(" + expression) + ", ") + _literal_1);
          String _plus_7 = (_plus_6 + ")");
          expression = _plus_7;
        }
      }
      _xblockexpression = Code.text(expression);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.writeOptions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "w");
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        where.keys(table.key(), IterableExtensions.<Code.Expr>toList(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      final Code.Entries args = LancedbSchema.kw("table", table.layout());
      boolean _isEmpty_1 = where.isEmpty();
      if (_isEmpty_1) {
        boolean _isDeleteAll = it.isDeleteAll();
        boolean _not_1 = (!_isDeleteAll);
        if (_not_1) {
          throw Unsupported.of("a delete without ids, filter or all");
        }
        args.put("where", Code.text("true"));
      } else {
        args.put("where", where.code());
      }
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        args.put("limit", Code.num(it.getDeleteLimit()));
      }
      Code.Call _runtime = Code.runtime("delete", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A local JSON or JSON Lines file written as an insert in batches; LanceDB has no server-side import.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of((("continuous import; " + LancedbStatements.EMBEDDED) + " and runs no ingestion pipeline"));
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
        throw Unsupported.of((((("import from " + scheme) + " URIs") + LancedbStatements.PENDING) + " (the runtimes read local files)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + LancedbStatements.PENDING);
        String _plus_2 = (_plus_1 + " (the runtimes read JSON and JSON Lines)");
        throw Unsupported.of(_plus_2);
      }
      this.schema.note(it, "parallelism");
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        this.notes.add("async on BulkImport, which the runtime finishes before returning");
      }
      this.writeOptions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = this.writeArgs(it, table, it.getCollection());
      args.put("location", Code.text(it.getLocation()));
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        args.put("on_conflict", Code.text(this.conflict(it.getOnConflict())));
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

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of(LancedbStatements.NO_TRANSACTIONS);
  }

  /**
   * A LanceDB write returns after its commit, so 'wait' and write consistency need no call; extensions are rejected.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, LancedbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * A statement's filter with the conditions of its tenant, namespace and partitions.
   */
  private LancedbFilter filterOf(final EObject statement, final LancedbTable table, final FilterBlock block, final String prefix) {
    LancedbFilter _xblockexpression = null;
    {
      final Function1<Field, String> _function = (Field it) -> {
        return this.schema.column(it);
      };
      final LancedbFilter result = new LancedbFilter(prefix, _function).where(block);
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.equal(table.tenant(), Code.text(tenant.getName()));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        result.equal(table.namespace(), Code.text(namespace));
      }
      final Object partition = this.feature(statement, "partition");
      if ((partition instanceof Partition)) {
        result.equal(LancedbSchema.PARTITION_COLUMN, Code.text(((Partition)partition).getName()));
      }
      final Object partitions = this.feature(statement, "partitions");
      if (((partitions instanceof List<?>) && (!((List<?>) partitions).isEmpty()))) {
        String _id = LancedbSql.id(LancedbSchema.PARTITION_COLUMN);
        String _plus = (_id + " IN ");
        final Function1<Partition, String> _function_1 = (Partition it) -> {
          return LancedbSql.text(it.getName());
        };
        final Function1<String, CharSequence> _function_2 = (String it) -> {
          return it;
        };
        String _join = IterableExtensions.<String>join(IterableExtensions.<Partition, String>map(Iterables.<Partition>filter(((List<?>) partitions), Partition.class), _function_1), "(", ", ", ")", _function_2);
        String _plus_1 = (_plus + _join);
        result.where(_plus_1);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Opens the table and loads its indexes into the cache (prewarm_index; prewarm_data is remote-only in LanceDB OSS).
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("loading partitions; a partition is a value of a column of the table");
      }
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _run = LancedbStatements.run(Code.runtime("load_table", LancedbSchema.kw("table", this.schema.table(it.getCollection()).place())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Each call opens its table, so nothing stays loaded between statements.
   */
  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("releasing partitions; a partition is a value of a column of the table");
      }
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = this.requireTable(it.getCollection());
    }
    return _xblockexpression;
  }

  /**
   * A LanceDB write is committed (data files and manifest written) before the call returns.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      _xblockexpression = this.requireTable(it.getCollection());
    }
    return _xblockexpression;
  }

  /**
   * optimize: compacts small data files and adds new rows to every index.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "targetSegments", "async");
      Code.Stmt _run = LancedbStatements.run(Code.runtime("optimize", LancedbSchema.kw("table", this.schema.table(it.getCollection()).place())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = LancedbSchema.kw("table", this.schema.table(it.getCollection()).layout());
      boolean _isIncludeStats = it.isIncludeStats();
      if (_isIncludeStats) {
        args.put("stats", Code.flag(true));
      }
      boolean _isIncludeIndexProgress = it.isIncludeIndexProgress();
      if (_isIncludeIndexProgress) {
        args.put("index_progress", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("describe", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final EmbeddingMigration it) {
    throw Unsupported.of(("embedding migration" + LancedbStatements.NO_MODELS));
  }

  /**
   * A snapshot is a tag on the table's current version.
   */
  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.snapshotScope(it.getScope(), ModelUtil.written(it, "scope"));
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a snapshot \'location\'; LanceDB tags live in the table directory");
      }
      Tenant _tenant = it.getTenant();
      boolean _tripleNotEquals_1 = (_tenant != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("tenant snapshots; a tag covers the whole table");
      }
      VectorCollection _collection = it.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        throw Unsupported.of("a snapshot without a collection; LanceDB tags versions of one table");
      }
      this.schema.note(it, "incremental", "description");
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = LancedbSchema.kw("table", table.place());
      String _snapshotName = it.getSnapshotName();
      boolean _tripleNotEquals_2 = (_snapshotName != null);
      if (_tripleNotEquals_2) {
        args.put("tag", Code.text(it.getSnapshotName()));
        this.tagged.put(it.getSnapshotName(), table);
      }
      Code.Call _runtime = Code.runtime("create_tag", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private void snapshotScope(final ESnapshotScope scope, final boolean declared) {
    if ((declared && (!Objects.equals(scope, ESnapshotScope.COLLECTION)))) {
      String _literal = scope.getLiteral();
      String _plus = (_literal + " snapshots; LanceDB tags versions of one table");
      throw Unsupported.of(_plus);
    }
  }

  /**
   * Restores a tagged version: checkout of the tag, then restore commits it as the newest version.
   */
  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.snapshotScope(it.getScope(), ModelUtil.written(it, "scope"));
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a restore \'location\'; LanceDB tags live in the table directory");
      }
      VectorCollection _collection = it.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        throw Unsupported.of("a restore without a collection; LanceDB restores versions of one table");
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        this.notes.add("async on Restore, which commits before returning");
      }
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Code.Call restore = Code.runtime("restore_tag", LancedbSchema.kw("table", this.schema.table(it.getCollection()).place(), "tag", Code.text(it.getSnapshotName())));
      final LancedbTable restored = this.tagged.get(it.getSnapshotName());
      if ((restored != null)) {
        this.schema.evolve(it.getCollection(), restored);
      }
      Code.Return _return = new Code.Return(restore);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Deletes the tag; the table is that of the script's snapshot statement of the same name.
   */
  protected List<Code.Stmt> _body(final DropSnapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a snapshot \'location\'; LanceDB tags live in the table directory");
      }
      final String tagName = it.getSnapshotName();
      final Function1<Snapshot, Boolean> _function = (Snapshot s) -> {
        return Boolean.valueOf((Objects.equals(s.getSnapshotName(), tagName) && (s.getCollection() != null)));
      };
      final Snapshot created = IterableExtensions.<Snapshot>findFirst(EcoreUtil2.<Snapshot>getAllContentsOfType(this.script, Snapshot.class), _function);
      if ((created == null)) {
        throw Unsupported.of("dropping a snapshot this script does not create; LanceDB tags belong to a table, which the drop statement does not name");
      }
      Code.Stmt _run = LancedbStatements.run(Code.runtime("drop_tag", LancedbSchema.kw("table", this.schema.table(created.getCollection()).place(), "tag", Code.text(tagName))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A shallow clone references the source's files (clone_table); a snapshot clone copies the rows into a new table.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; LanceDB clones tables");
      }
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      LancedbSchema.checkName(it.getTargetName());
      final LancedbTable table = this.schema.table(it.getSourceCollection());
      final boolean shallow = (!(ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SNAPSHOT)));
      final Code.Entries args = LancedbSchema.kw("table", table.place(), "target", Code.text(it.getTargetName()), "shallow", Code.flag(shallow), "schema", table.schema(), "indexes", table.indexPlans());
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        args.put("version", Literals.of(it.getAsOf()));
      }
      Code.Call _runtime = Code.runtime("clone_table", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Retrains an index over every row: create_index with replace.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      boolean _isConcurrently = it.isConcurrently();
      if (_isConcurrently) {
        this.notes.add("concurrently on RebuildIndex; LanceDB builds the new index beside the old one and commits it as a new version");
      }
      final Index definition = it.getIndex();
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          VectorField _elvis = null;
          VectorField _field = ((VectorIndex)definition).getField();
          if (_field != null) {
            _elvis = _field;
          } else {
            VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(definition, VectorField.class);
            _elvis = _containerOfType;
          }
          final VectorField field = _elvis;
          if ((field == null)) {
            throw Unsupported.of("a vector index without a vector field");
          }
          final LancedbTable table = this.schema.table(this.member(field, "a vector index"));
          String _elvis_1 = null;
          String _get = table.vectors().get(this.schema.column(field));
          if (_get != null) {
            _elvis_1 = _get;
          } else {
            String _metricName = this.schema.metricName(field, ((VectorIndex)definition));
            _elvis_1 = _metricName;
          }
          Code.Stmt _run = LancedbStatements.run(Code.runtime("rebuild_index", LancedbSchema.kw("table", table.place(), "index", this.schema.vectorIndex(((VectorIndex)definition), field, _elvis_1))));
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final Field field = ((PayloadIndex)definition).getTarget().getField();
            VectorCollection _elvis = null;
            VectorCollection _targetCollection = ModelUtil.targetCollection(definition);
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
              throw Unsupported.of("a payload index on a raw path outside a collection");
            }
            Code.Stmt _run = LancedbStatements.run(Code.runtime("rebuild_index", LancedbSchema.kw("table", this.schema.table(collection).place(), "index", this.schema.payloadIndex(collection, ((PayloadIndex)definition)))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = definition.eClass().getName();
        String _plus = (LancedbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(("collections derived from external sources; " + LancedbStatements.EXTERNAL));
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final LancedbTable table = this.schema.table(it.getCollection());
      if (((it.getNamespace() != null) && (table.namespace() == null))) {
        throw Unsupported.of("a namespace the collection does not declare");
      }
      Code.Text _xifexpression = null;
      String _namespace = it.getNamespace();
      boolean _tripleEquals = (_namespace == null);
      if (_tripleEquals) {
        _xifexpression = Code.text("true");
      } else {
        String _id = LancedbSql.id(table.namespace());
        String _plus = (_id + " = ");
        String _text = LancedbSql.text(it.getNamespace());
        String _plus_1 = (_plus + _text);
        _xifexpression = Code.text(_plus_1);
      }
      final Code.Text where = _xifexpression;
      Code.Call _runtime = Code.runtime("truncate", LancedbSchema.kw("table", table.layout(), "where", where));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept owner = it.getScope();
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Entries args = Code.entries();
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner instanceof Database)) {
              args.put("database", Code.text(((Database)owner).getName()));
            } else {
              if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
                String _name = owner.eClass().getName();
                String _plus = ("listing collections in a " + _name);
                String _plus_1 = (_plus + "; LanceDB lists the tables of a database");
                throw Unsupported.of(_plus_1);
              }
            }
            this.listBounds(it, args);
            Code.Call _runtime = Code.runtime("list_tables", args);
            Code.Return _return = new Code.Return(_runtime);
            result.add(_return);
            break;
          case DATABASES:
            if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
              String _name_1 = owner.eClass().getName();
              String _plus_2 = ("listing databases in a " + _name_1);
              throw Unsupported.of(_plus_2);
            }
            if (((it.getPrefix() != null) || ModelUtil.written(it, "listLimit"))) {
              throw Unsupported.of("prefix and limit on databases");
            }
            Code.Call _runtime_1 = Code.runtime("list_databases", args);
            Code.Return _return_1 = new Code.Return(_runtime_1);
            result.add(_return_1);
            break;
          case INDEXES:
          case SNAPSHOTS:
            if ((!(owner instanceof VectorCollection))) {
              String _literal = it.getKind().getLiteral();
              String _plus_3 = ("listing " + _literal);
              String _plus_4 = (_plus_3 + " without a collection; LanceDB lists them per table");
              throw Unsupported.of(_plus_4);
            }
            args.put("table", this.schema.table(((VectorCollection) owner)).place());
            this.listBounds(it, args);
            String _xifexpression = null;
            EListKind _kind_1 = it.getKind();
            boolean _equals = Objects.equals(_kind_1, EListKind.INDEXES);
            if (_equals) {
              _xifexpression = "list_indexes";
            } else {
              _xifexpression = "list_tags";
            }
            Code.Call _runtime_2 = Code.runtime(_xifexpression, args);
            Code.Return _return_2 = new Code.Return(_runtime_2);
            result.add(_return_2);
            break;
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            if ((!(owner instanceof VectorCollection))) {
              String _literal_1 = it.getKind().getLiteral();
              String _plus_5 = ("listing " + _literal_1);
              String _plus_6 = (_plus_5 + " without a collection; they are values of a column per table");
              throw Unsupported.of(_plus_6);
            }
            final LancedbTable table = this.schema.table(((VectorCollection) owner));
            String _switchResult_1 = null;
            EListKind _kind_2 = it.getKind();
            if (_kind_2 != null) {
              switch (_kind_2) {
                case TENANTS:
                  _switchResult_1 = table.tenant();
                  break;
                case NAMESPACES:
                  _switchResult_1 = table.namespace();
                  break;
                default:
                  _switchResult_1 = table.partition();
                  break;
              }
            } else {
              _switchResult_1 = table.partition();
            }
            final String column = _switchResult_1;
            if ((column == null)) {
              String _literal_2 = it.getKind().getLiteral();
              String _plus_7 = ("listing " + _literal_2);
              String _plus_8 = (_plus_7 + " of a collection without them");
              throw Unsupported.of(_plus_8);
            }
            args.put("table", table.layout());
            args.put("column", Code.text(column));
            this.listBounds(it, args);
            Code.Call _runtime_3 = Code.runtime("list_values", args);
            Code.Return _return_3 = new Code.Return(_runtime_3);
            result.add(_return_3);
            break;
          case ALIASES:
            throw Unsupported.of(LancedbStatements.NO_ALIASES);
          default:
            break;
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private void listBounds(final ListObjects list, final Code.Entries args) {
    String _prefix = list.getPrefix();
    boolean _tripleNotEquals = (_prefix != null);
    if (_tripleNotEquals) {
      args.put("prefix", Code.text(list.getPrefix()));
    }
    boolean _written = ModelUtil.written(list, "listLimit");
    if (_written) {
      args.put("limit", Code.num(list.getListLimit()));
    }
  }

  /**
   * Index builds finish inside create_index in the open-source library; wait_for_index confirms the index and reports its coverage.
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Index definition = it.getIndex();
      VectorCollection _switchResult = null;
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        VectorCollection _xblockexpression_1 = null;
        {
          VectorField _elvis = null;
          VectorField _field = ((VectorIndex)definition).getField();
          if (_field != null) {
            _elvis = _field;
          } else {
            VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(definition, VectorField.class);
            _elvis = _containerOfType;
          }
          final VectorField field = _elvis;
          if ((field == null)) {
            throw Unsupported.of("a vector index without a vector field");
          }
          _xblockexpression_1 = this.member(field, "a vector index");
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          VectorCollection _xblockexpression_1 = null;
          {
            final Field field = ((PayloadIndex)definition).getTarget().getField();
            VectorCollection _elvis = null;
            VectorCollection _targetCollection = ModelUtil.targetCollection(definition);
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
            _xblockexpression_1 = _elvis;
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = definition.eClass().getName();
        String _plus = (LancedbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final VectorCollection collection = _switchResult;
      if ((collection == null)) {
        throw Unsupported.of("a payload index on a raw path outside a collection");
      }
      this.schema.note(it, "state");
      final Code.Entries args = LancedbSchema.kw("table", this.schema.table(collection).place(), "name", Code.text(definition.getName()));
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
      this.reject(it, "elementScope", "explain", "reranker", "highlighted", "queryModel");
      this.later(it, "cursor", "withTotal", "mmr", "boost", "aggregations", "prefetches");
      final LancedbTable table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorField(it.getCollection(), it.getVectorField());
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression = it.getMetric();
      } else {
        _xifexpression = null;
      }
      final Code.Entries plan = this.vectorPlan(table, field, _xifexpression, it.getParams(), it.isExact(), this.filterOf(it, table, it.getFilter(), "f"));
      this.queryExtensions(it);
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "plan", plan, "vector", this.queryVector(it.getInput(), it.getCollection(), field, prelude), "limit", Code.num(it.getTopK()));
      this.common(it, table, it.getCollection(), args);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _runtime = Code.runtime("vector_search", args);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The vector field a query names, else the collection's only vector field.
   */
  private VectorField vectorField(final VectorCollection collection, final VectorField named) {
    VectorField _xblockexpression = null;
    {
      final List<VectorField> fields = ModelUtil.vectorFields(collection);
      if ((named != null)) {
        boolean _contains = fields.contains(named);
        boolean _not = (!_contains);
        if (_not) {
          throw Unsupported.of("a vector field outside the collection");
        }
        return named;
      }
      int _size = fields.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        int _size_1 = fields.size();
        String _plus = ("a query without using on a collection with " + Integer.valueOf(_size_1));
        String _plus_1 = (_plus + " vector fields");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = IterableExtensions.<VectorField>head(fields);
    }
    return _xblockexpression;
  }

  /**
   * Column, distance type, where string and search settings of a nearest-neighbour query.
   */
  private Code.Entries vectorPlan(final LancedbTable table, final VectorField field, final EMetric metric, final SearchParams params, final boolean exact, final LancedbFilter where) {
    Code.Entries _xblockexpression = null;
    {
      final String column = this.schema.column(field);
      boolean _has = table.has(column);
      boolean _not = (!_has);
      if (_not) {
        throw Unsupported.of((("vector field " + column) + ", which the table no longer has"));
      }
      final String declared = table.vectors().get(column);
      if (((metric != null) && (!Objects.equals(LancedbSchema.lance(metric), declared)))) {
        throw Unsupported.of("a query metric other than the metric of the vector field; the generator ranks by the field metric");
      }
      final Code.Entries plan = LancedbSchema.kw("column", Code.text(column), "metric", Code.text(declared));
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        plan.put("multivector", Code.flag(true));
      }
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.BINARY_VECTOR);
      if (_equals) {
        plan.put("bits", Code.flag(true));
      }
      boolean _isEmpty = where.isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        plan.put("where", where.code());
      }
      if (exact) {
        plan.put("exact", Code.flag(true));
      }
      this.searchParams(params, plan);
      _xblockexpression = plan;
    }
    return _xblockexpression;
  }

  /**
   * Search effort: probes is nprobes, candidates ef (HNSW), rescore and oversampling refine_factor, filter strategy prefilter.
   */
  private void searchParams(final SearchParams params, final Code.Entries plan) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall; LanceDB exposes no query-time recall target (nprobes, ef and refine_factor are effort settings)");
    }
    boolean _written_1 = ModelUtil.written(params, "scanBudget");
    if (_written_1) {
      throw Unsupported.of("scan-budget; LanceDB bounds a search by nprobes and ef");
    }
    boolean _isExhaustive = params.isExhaustive();
    if (_isExhaustive) {
      plan.put("exact", Code.flag(true));
    }
    boolean _written_2 = ModelUtil.written(params, "probes");
    if (_written_2) {
      plan.put("nprobes", Code.num(params.getProbes()));
    }
    boolean _written_3 = ModelUtil.written(params, "candidatePool");
    if (_written_3) {
      plan.put("ef", Code.num(params.getCandidatePool()));
    }
    boolean _written_4 = ModelUtil.written(params, "oversampling");
    if (_written_4) {
      plan.put("refine_factor", Code.num(Math.max(1, Math.ceil(params.getOversampling()))));
    } else {
      boolean _isRescore = params.isRescore();
      if (_isRescore) {
        plan.put("refine_factor", Code.num(1));
      }
    }
    boolean _written_5 = ModelUtil.written(params, "filterStrategy");
    if (_written_5) {
      EFilterStrategy _filterStrategy = params.getFilterStrategy();
      if (_filterStrategy != null) {
        switch (_filterStrategy) {
          case POST_FILTER:
            plan.put("prefilter", Code.flag(false));
            break;
          case PRE_FILTER:
          case AUTO:
            plan.put("prefilter", Code.flag(true));
            break;
          case ITERATIVE:
            throw Unsupported.of("the iterative filter strategy; LanceDB filters before or after the vector search");
          default:
            break;
        }
      }
    }
    this.schema.note(params, "timeoutMillis");
    Set<String> _keySet = ModelUtil.extParams(params, LancedbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  private Code.Expr queryVector(final QueryInput input, final VectorCollection collection, final VectorField field, final List<Code.Stmt> prelude) {
    Code.Expr _xblockexpression = null;
    {
      if ((input == null)) {
        throw Unsupported.of("a vector query without input");
      }
      Code.Expr _switchResult = null;
      EQueryInputMode _kind = input.getKind();
      if (_kind != null) {
        switch (_kind) {
          case VECTOR:
            Code.Expr _xblockexpression_1 = null;
            {
              boolean _isMultiVector = field.isMultiVector();
              if (_isMultiVector) {
                String _column = this.schema.column(field);
                String _plus = ("a single query vector on multivector field " + _column);
                String _plus_1 = (_plus + "; query it with multi_vector");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = Literals.of(input.getValue());
            }
            _switchResult = _xblockexpression_1;
            break;
          case MULTI_VECTOR:
            Code.Expr _xblockexpression_2 = null;
            {
              boolean _isMultiVector = field.isMultiVector();
              boolean _not = (!_isMultiVector);
              if (_not) {
                String _column = this.schema.column(field);
                String _plus = ("a multi_vector query on " + _column);
                String _plus_1 = (_plus + ", which is not a multivector field");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_2 = Literals.of(input.getValue());
            }
            _switchResult = _xblockexpression_2;
            break;
          case ID:
            Code.Ref _xblockexpression_3 = null;
            {
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final LancedbTable source = this.schema.table(_elvis);
              Code.Call _runtime = Code.runtime("lookup_vector", LancedbSchema.kw("table", source.layout(), "column", Code.text(this.schema.column(field)), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let("query_vector", _runtime);
              prelude.add(_let);
              _xblockexpression_3 = new Code.Ref("query_vector");
            }
            _switchResult = _xblockexpression_3;
            break;
          case TEXT:
            throw Unsupported.of(("text input" + LancedbStatements.NO_MODELS));
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " query input; ");
            String _plus_1 = (_plus + LancedbStatements.NO_SPARSE);
            String _plus_2 = (_plus_1 + " and the programs embed no images");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " query input; ");
        String _plus_1 = (_plus + LancedbStatements.NO_SPARSE);
        String _plus_2 = (_plus_1 + " and the programs embed no images");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Full-text search over the FTS indexes of the listed fields, scored by BM25 (_score).
   */
  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "explain", "reranker", "highlighted");
      this.later(it, "cursor", "withTotal", "mmr", "boost", "aggregations");
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on KeywordSearch, since BM25 is the keyword score");
      }
      final LancedbTable table = this.schema.table(it.getCollection());
      VectorCollection _collection = it.getCollection();
      final Function1<SearchFieldRef, Field> _function = (SearchFieldRef it_1) -> {
        return it_1.getField();
      };
      List<Field> _map = ListExtensions.<SearchFieldRef, Field>map(it.getSearchFields(), _function);
      final Function1<SearchFieldRef, Double> _function_1 = (SearchFieldRef s) -> {
        Double _xifexpression = null;
        boolean _written = ModelUtil.written(s, "boost");
        if (_written) {
          _xifexpression = Double.valueOf(s.getBoost());
        } else {
          _xifexpression = null;
        }
        return _xifexpression;
      };
      List<Double> _map_1 = ListExtensions.<SearchFieldRef, Double>map(it.getSearchFields(), _function_1);
      ETextQueryType _xifexpression = null;
      boolean _written = ModelUtil.written(it, "textQueryType");
      if (_written) {
        _xifexpression = it.getTextQueryType();
      } else {
        _xifexpression = null;
      }
      EMatchOperator _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(it, "matchOperator");
      if (_written_1) {
        _xifexpression_1 = it.getMatchOperator();
      } else {
        _xifexpression_1 = null;
      }
      Integer _xifexpression_2 = null;
      boolean _written_2 = ModelUtil.written(it, "minShouldMatch");
      if (_written_2) {
        _xifexpression_2 = Integer.valueOf(it.getMinShouldMatch());
      } else {
        _xifexpression_2 = null;
      }
      String _xifexpression_3 = null;
      boolean _written_3 = ModelUtil.written(it, "fuzziness");
      if (_written_3) {
        _xifexpression_3 = it.getFuzziness();
      } else {
        _xifexpression_3 = null;
      }
      Integer _xifexpression_4 = null;
      boolean _written_4 = ModelUtil.written(it, "slop");
      if (_written_4) {
        _xifexpression_4 = Integer.valueOf(it.getSlop());
      } else {
        _xifexpression_4 = null;
      }
      final Code.Entries plan = this.textPlan(table, _collection, _map, _map_1, _xifexpression, _xifexpression_1, _xifexpression_2, _xifexpression_3, _xifexpression_4);
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        plan.put("where", where.code());
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.keywordParams(it.getParams(), plan);
      }
      this.queryExtensions(it);
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "plan", plan, "text", this.queryText(it.getInput()), "limit", Code.num(it.getTopK()));
      this.common(it, table, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("keyword_search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private void keywordParams(final SearchParams params, final Code.Entries plan) {
    boolean _written = ModelUtil.written(params, "filterStrategy");
    if (_written) {
      EFilterStrategy _filterStrategy = params.getFilterStrategy();
      boolean _equals = Objects.equals(_filterStrategy, EFilterStrategy.POST_FILTER);
      if (_equals) {
        plan.put("prefilter", Code.flag(false));
      } else {
        EFilterStrategy _filterStrategy_1 = params.getFilterStrategy();
        boolean _equals_1 = Objects.equals(_filterStrategy_1, EFilterStrategy.ITERATIVE);
        if (_equals_1) {
          throw Unsupported.of("the iterative filter strategy; LanceDB filters before or after the search");
        }
      }
    }
    if (((((((ModelUtil.written(params, "targetRecall") || ModelUtil.written(params, "probes")) || ModelUtil.written(params, "candidatePool")) || ModelUtil.written(params, "oversampling")) || params.isRescore()) || params.isExhaustive()) || ModelUtil.written(params, "scanBudget"))) {
      throw Unsupported.of("vector search settings on a keyword search");
    }
    this.schema.note(params, "timeoutMillis");
    Set<String> _keySet = ModelUtil.extParams(params, LancedbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  private Code.Expr queryText(final QueryInput input) {
    Code.Expr _xblockexpression = null;
    {
      if (((input == null) || (!Objects.equals(input.getKind(), EQueryInputMode.TEXT)))) {
        throw Unsupported.of("a keyword search without a text query");
      }
      _xblockexpression = Literals.of(input.getValue());
    }
    return _xblockexpression;
  }

  /**
   * The full-text query: match (MatchQuery, or MultiMatchQuery over several fields with per-field boosts), phrase
   * (PhraseQuery with slop) or fuzzy (MatchQuery with fuzziness); the operator joins the terms.
   */
  private Code.Entries textPlan(final LancedbTable table, final VectorCollection collection, final List<Field> listed, final List<Double> boosts, final ETextQueryType type, final EMatchOperator operator, final Integer minimum, final String fuzziness, final Integer slop) {
    Code.Entries _xblockexpression = null;
    {
      final List<String> columns = CollectionLiterals.<String>newArrayList();
      List<? extends Field> _xifexpression = null;
      boolean _isEmpty = listed.isEmpty();
      if (_isEmpty) {
        final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
          return Boolean.valueOf(this.schema.searchable(it));
        };
        List<PayloadField> _list = IterableExtensions.<PayloadField>toList(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(collection), _function));
        _xifexpression = ((List<? extends Field>) _list);
      } else {
        _xifexpression = listed;
      }
      final List<? extends Field> searched = _xifexpression;
      boolean _isEmpty_1 = searched.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("a keyword search on a collection without full-text fields");
      }
      for (final Field field : searched) {
        {
          final String name = this.schema.column(field);
          final Function1<Code.Entries, Boolean> _function_1 = (Code.Entries i) -> {
            return Boolean.valueOf((Objects.equals(((Code.Text) i.get("kind")).value(), "fts") && Objects.equals(((Code.Text) i.get("column")).value(), name)));
          };
          boolean _exists = IterableExtensions.<Code.Entries>exists(table.indexes(), _function_1);
          boolean _not = (!_exists);
          if (_not) {
            throw Unsupported.of((("a keyword search on " + name) + ", which has no full-text index; LanceDB searches FTS-indexed columns"));
          }
          columns.add(name);
        }
      }
      final Code.Entries plan = LancedbSchema.kw("columns", Code.texts(columns));
      final Code.Entries weights = Code.entries();
      for (int i = 0; (i < columns.size()); i++) {
        if (((boosts.size() > i) && (boosts.get(i) != null))) {
          weights.put(columns.get(i), Code.num((boosts.get(i)).doubleValue()));
        }
      }
      boolean _isEmpty_2 = weights.entries().isEmpty();
      boolean _not = (!_isEmpty_2);
      if (_not) {
        plan.put("boosts", weights);
      }
      boolean _equals = Objects.equals(operator, EMatchOperator.AND);
      if (_equals) {
        plan.put("operator", Code.text("and"));
      }
      if ((minimum != null)) {
        throw Unsupported.of("min-should-match; LanceDB full-text queries join terms with and or or");
      }
      boolean _equals_1 = Objects.equals(type, ETextQueryType.PHRASE);
      if (_equals_1) {
        plan.put("kind", Code.text("phrase"));
        if ((slop != null)) {
          plan.put("slop", Code.num((slop).intValue()));
        }
      } else {
        boolean _equals_2 = Objects.equals(type, ETextQueryType.FUZZY);
        if (_equals_2) {
          plan.put("fuzziness", Code.num(1));
        } else {
          if (((type != null) && (!Objects.equals(type, ETextQueryType.MATCH)))) {
            String _literal = type.getLiteral();
            String _plus = ("text-query " + _literal);
            String _plus_1 = (_plus + "; LanceDB full-text queries are match, phrase, boolean, boost and multi_match (no prefix, wildcard, regexp or query string)");
            throw Unsupported.of(_plus_1);
          }
        }
      }
      if (((slop != null) && (!Objects.equals(type, ETextQueryType.PHRASE)))) {
        throw Unsupported.of("slop without a phrase query");
      }
      if ((fuzziness != null)) {
        boolean _equals_3 = Objects.equals(type, ETextQueryType.PHRASE);
        if (_equals_3) {
          throw Unsupported.of("fuzziness on a phrase query");
        }
        plan.put("fuzziness", Code.num(this.fuzzyEdits(fuzziness)));
      }
      _xblockexpression = plan;
    }
    return _xblockexpression;
  }

  /**
   * Edit distance of a fuzziness setting: AUTO is one edit for LanceDB's single-term expansion; numbers are taken as given.
   */
  private int fuzzyEdits(final String fuzziness) {
    int _xblockexpression = (int) 0;
    {
      boolean _equalsIgnoreCase = fuzziness.equalsIgnoreCase("AUTO");
      if (_equalsIgnoreCase) {
        return 1;
      }
      int _xtrycatchfinallyexpression = (int) 0;
      try {
        int _xblockexpression_1 = (int) 0;
        {
          final int edits = Integer.parseInt(fuzziness);
          if (((edits < 0) || (edits > 2))) {
            throw Unsupported.of((("fuzziness " + fuzziness) + "; LanceDB expands terms by up to 2 edits"));
          }
          _xblockexpression_1 = edits;
        }
        _xtrycatchfinallyexpression = _xblockexpression_1;
      } catch (final Throwable _t) {
        if (_t instanceof NumberFormatException) {
          throw Unsupported.of(("fuzziness " + fuzziness));
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xblockexpression = _xtrycatchfinallyexpression;
    }
    return _xblockexpression;
  }

  /**
   * Runs each leg on its own and fuses in the runtime exactly as vecDSL defines rrf, weighted and relative_score.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "explain", "reranker", "highlighted");
      this.later(it, "cursor", "withTotal", "mmr", "boost", "aggregations");
      final LancedbTable table = this.schema.table(it.getCollection());
      EFusionType _xifexpression = null;
      boolean _written = ModelUtil.written(it, "fusion");
      if (_written) {
        _xifexpression = it.getFusion();
      } else {
        _xifexpression = EFusionType.RRF;
      }
      final EFusionType fusionKind = _xifexpression;
      final List<Double> weights = this.legWeights(it, fusionKind);
      final List<Code.Expr> mapped = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      int index = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Code.Entries _leg = this.leg(leg, table, it.getCollection(), weights.get(index), prelude, (("l" + Integer.valueOf(index)) + "_"));
          mapped.add(_leg);
          index = (index + 1);
        }
      }
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "timeoutMillis");
      }
      this.queryExtensions(it);
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "legs", Code.items(mapped), "limit", Code.num(it.getTopK()), "fusion", Code.text(fusionKind.getLiteral()));
      boolean _equals = Objects.equals(fusionKind, EFusionType.RRF);
      if (_equals) {
        int _xifexpression_1 = (int) 0;
        boolean _written_1 = ModelUtil.written(it, "fusionConstant");
        if (_written_1) {
          _xifexpression_1 = it.getFusionConstant();
        } else {
          _xifexpression_1 = 60;
        }
        args.put("k", Code.num(_xifexpression_1));
      } else {
        boolean _written_2 = ModelUtil.written(it, "fusionConstant");
        if (_written_2) {
          String _literal = fusionKind.getLiteral();
          String _plus = ("fusion-k with " + _literal);
          String _plus_1 = (_plus + " fusion; vecDSL defines fusion-k for rrf only");
          throw Unsupported.of(_plus_1);
        }
      }
      this.common(it, table, it.getCollection(), args);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _runtime = Code.runtime("hybrid_search", args);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * One leg: its own input or the shared one, its filter joined with the search's, its window and weight.
   */
  private Code.Entries leg(final HybridLeg leg, final LancedbTable table, final VectorCollection collection, final Double weight, final List<Code.Stmt> prelude, final String prefix) {
    Code.Entries _xblockexpression = null;
    {
      EObject _eContainer = leg.eContainer();
      final HybridSearch search = ((HybridSearch) _eContainer);
      QueryInput _elvis = null;
      Object _feature = this.feature(leg, "input");
      if (((QueryInput) _feature) != null) {
        _elvis = ((QueryInput) _feature);
      } else {
        QueryInput _input = search.getInput();
        _elvis = _input;
      }
      final QueryInput input = _elvis;
      Object _feature_1 = this.feature(leg, "filter");
      final LancedbFilter conditions = this.filterOf(search, table, search.getFilter(), prefix).where(((FilterBlock) _feature_1));
      Code.Entries _switchResult = null;
      boolean _matched = false;
      if (leg instanceof VectorLeg) {
        _matched=true;
        Code.Entries _xblockexpression_1 = null;
        {
          final VectorField field = this.vectorField(collection, ((VectorLeg)leg).getVectorField());
          EMetric _xifexpression = null;
          boolean _written = ModelUtil.written(leg, "metric");
          if (_written) {
            _xifexpression = ((VectorLeg)leg).getMetric();
          } else {
            _xifexpression = null;
          }
          final Code.Entries result = this.vectorPlan(table, field, _xifexpression, ((VectorLeg)leg).getParams(), ((VectorLeg)leg).isExact(), conditions);
          result.put("kind", Code.text("vector"));
          result.put("vector", this.queryVector(input, collection, field, prelude));
          ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
          boolean _tripleNotEquals = (_bounds != null);
          if (_tripleNotEquals) {
            boolean _written_1 = ModelUtil.written(((VectorLeg)leg).getBounds(), "min");
            if (_written_1) {
              result.put("score_min", Code.num(((VectorLeg)leg).getBounds().getMin()));
            }
            boolean _written_2 = ModelUtil.written(((VectorLeg)leg).getBounds(), "max");
            if (_written_2) {
              result.put("score_max", Code.num(((VectorLeg)leg).getBounds().getMax()));
            }
          }
          Set<String> _keySet = ModelUtil.extParams(leg, LancedbGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of(("extension parameter " + name));
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
            final Function1<SearchFieldRef, Field> _function = (SearchFieldRef it) -> {
              return it.getField();
            };
            List<Field> _map = ListExtensions.<SearchFieldRef, Field>map(((KeywordLeg)leg).getSearchFields(), _function);
            final Function1<SearchFieldRef, Double> _function_1 = (SearchFieldRef s) -> {
              Double _xifexpression = null;
              boolean _written = ModelUtil.written(s, "boost");
              if (_written) {
                _xifexpression = Double.valueOf(s.getBoost());
              } else {
                _xifexpression = null;
              }
              return _xifexpression;
            };
            List<Double> _map_1 = ListExtensions.<SearchFieldRef, Double>map(((KeywordLeg)leg).getSearchFields(), _function_1);
            ETextQueryType _xifexpression = null;
            boolean _written = ModelUtil.written(leg, "textQueryType");
            if (_written) {
              _xifexpression = ((KeywordLeg)leg).getTextQueryType();
            } else {
              _xifexpression = null;
            }
            EMatchOperator _xifexpression_1 = null;
            boolean _written_1 = ModelUtil.written(leg, "matchOperator");
            if (_written_1) {
              _xifexpression_1 = ((KeywordLeg)leg).getMatchOperator();
            } else {
              _xifexpression_1 = null;
            }
            Integer _xifexpression_2 = null;
            boolean _written_2 = ModelUtil.written(leg, "minShouldMatch");
            if (_written_2) {
              _xifexpression_2 = Integer.valueOf(((KeywordLeg)leg).getMinShouldMatch());
            } else {
              _xifexpression_2 = null;
            }
            final Code.Entries result = this.textPlan(table, collection, _map, _map_1, _xifexpression, _xifexpression_1, _xifexpression_2, null, null);
            boolean _isEmpty = conditions.isEmpty();
            boolean _not = (!_isEmpty);
            if (_not) {
              result.put("where", conditions.code());
            }
            result.put("kind", Code.text("keyword"));
            result.put("text", this.queryText(input));
            Set<String> _keySet = ModelUtil.extParams(leg, LancedbGenerator.VENDOR).keySet();
            for (final String name : _keySet) {
              throw Unsupported.of(("extension parameter " + name));
            }
            _xblockexpression_1 = result;
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = leg.eClass().getName();
        String _plus = (LancedbStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final Code.Entries plan = _switchResult;
      boolean _written = ModelUtil.written(leg, "legLimit");
      if (_written) {
        Object _feature_2 = this.feature(leg, "legLimit");
        plan.put("limit", Code.num(((Integer) _feature_2).doubleValue()));
      }
      if ((weight != null)) {
        plan.put("weight", Code.num((weight).doubleValue()));
      }
      _xblockexpression = plan;
    }
    return _xblockexpression;
  }

  /**
   * Leg weights for weighted and relative-score fusion; rrf is unweighted by definition.
   */
  private List<Double> legWeights(final HybridSearch search, final EFusionType fusionKind) {
    List<Double> _xblockexpression = null;
    {
      final Function1<HybridLeg, Boolean> _function = (HybridLeg it) -> {
        return Boolean.valueOf(ModelUtil.written(it, "weight"));
      };
      final int weighted = IterableExtensions.size(IterableExtensions.<HybridLeg>filter(search.getLegs(), _function));
      boolean _equals = Objects.equals(fusionKind, EFusionType.RRF);
      if (_equals) {
        if (((weighted > 0) || ModelUtil.written(search, "alpha"))) {
          throw Unsupported.of("leg weights or alpha with rrf fusion, which is unweighted");
        }
        final Function1<HybridLeg, Double> _function_1 = (HybridLeg it) -> {
          return ((Double) null);
        };
        return ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_1);
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
      final Function1<HybridLeg, Double> _function_2 = (HybridLeg l) -> {
        Double _xifexpression = null;
        boolean _written_1 = ModelUtil.written(l, "weight");
        if (_written_1) {
          Object _feature = this.feature(l, "weight");
          _xifexpression = ((Double) _feature);
        } else {
          _xifexpression = Double.valueOf(1.0);
        }
        return _xifexpression;
      };
      _xblockexpression = ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_2);
    }
    return _xblockexpression;
  }

  /**
   * The average-vector strategy over example records or vectors, computed in the runtime: 2 mean(positive) - mean(negative).
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "explain", "reranker", "highlighted");
      this.later(it, "cursor", "withTotal", "mmr", "boost", "aggregations");
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final LancedbTable table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorField(it.getCollection(), it.getVectorField());
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of("recommend on a multivector field; the average-vector strategy needs one vector per example");
      }
      final Code.Entries plan = this.vectorPlan(table, field, null, it.getParams(), false, this.filterOf(it, table, it.getFilter(), "f"));
      this.queryExtensions(it);
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "plan", plan, "positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)), "limit", Code.num(it.getTopK()));
      boolean _isEmpty_1 = it.getNegatives().isEmpty();
      boolean _not = (!_isEmpty_1);
      if (_not) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      }
      if (((it.getLookupCollection() != null) && (it.getLookupCollection() != it.getCollection()))) {
        args.put("lookup", this.schema.table(it.getLookupCollection()).layout());
      }
      this.common(it, table, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("recommend", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      this.projection(table, it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final LancedbFilter where = this.filterOf(it, table, null, "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("where", where.code());
      }
      this.readOptions(it, args);
      Code.Call _runtime = Code.runtime("get_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      boolean _written = ModelUtil.written(it, "exact");
      if (_written) {
        this.notes.add("exact on Count, since count_rows is exact");
      }
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = LancedbSchema.kw("table", table.layout());
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("where", where.code());
      }
      this.readOptions(it, args);
      Code.Call _runtime = Code.runtime("count", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.later(it, "cursor");
      this.queryExtensions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = LancedbSchema.kw("table", table.layout());
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      boolean _written_1 = ModelUtil.written(it, "sliceIndex");
      if (_written_1) {
        if (((!ModelUtil.written(it, "sliceTotal")) || (it.getSliceIndex() >= it.getSliceTotal()))) {
          throw Unsupported.of("a scroll slice without a total above its index");
        }
        args.put("slice", LancedbSchema.kw("index", Code.num(it.getSliceIndex()), "total", Code.num(it.getSliceTotal())));
      }
      this.common(it, table, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Sort clauses on stored fields, applied in the runtime over the hits (scroll: over the matching rows, then the key).
   */
  private void order(final LancedbTable table, final List<SortClause> clauses, final Code.Entries args, final boolean scored) {
    boolean _isEmpty = clauses.isEmpty();
    if (_isEmpty) {
      return;
    }
    final List<Code.Expr> keys = CollectionLiterals.<Code.Expr>newArrayList();
    for (final SortClause clause : clauses) {
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        if ((!scored)) {
          throw Unsupported.of("sort by relevance in a scroll");
        }
        String _xifexpression = null;
        ESortDirection _direction = clause.getDirection();
        boolean _equals = Objects.equals(_direction, ESortDirection.ASC);
        if (_equals) {
          _xifexpression = "asc";
        } else {
          _xifexpression = "desc";
        }
        Code.Entries _kw = LancedbSchema.kw("key", Code.text("score"), "direction", Code.text(_xifexpression));
        keys.add(_kw);
      } else {
        final FieldPath path = clause.getField();
        String _rawPath = path.getRawPath();
        boolean _tripleNotEquals = (_rawPath != null);
        if (_tripleNotEquals) {
          throw Unsupported.of("sorting by a raw path; Lance columns are declared");
        }
        final ArrayList<String> names = CollectionLiterals.<String>newArrayList(this.schema.column(path.getField()));
        EList<Field> _subFields = path.getSubFields();
        for (final Field sub : _subFields) {
          String _fieldName = ModelUtil.fieldName(sub);
          names.add(_fieldName);
        }
        boolean _has = table.has(IterableExtensions.<String>head(names));
        boolean _not = (!_has);
        if (_not) {
          String _head = IterableExtensions.<String>head(names);
          String _plus = ("sorting by " + _head);
          String _plus_1 = (_plus + ", which the table no longer has");
          throw Unsupported.of(_plus_1);
        }
        final Code.Entries key = LancedbSchema.kw("key", Code.text(IterableExtensions.join(names, ".")));
        ESortDirection _direction_1 = clause.getDirection();
        boolean _equals_1 = Objects.equals(_direction_1, ESortDirection.DESC);
        if (_equals_1) {
          key.put("direction", Code.text("desc"));
        }
        keys.add(key);
      }
    }
    args.put("order", Code.items(keys));
  }

  /**
   * Group-by: the runtime groups the hits of every matching row by the value of the group key (a column or a sub-field path),
   * keeps the groups whose best hit ranks highest and the best size hits of each (1 by default).
   */
  private void groupOf(final LancedbTable table, final EObject query, final Code.Entries args) {
    Object _feature = this.feature(query, "groupBy");
    final FieldPath grouping = ((FieldPath) _feature);
    if ((grouping == null)) {
      return;
    }
    Object _elvis = null;
    Object _feature_1 = this.feature(query, "sortClauses");
    if (_feature_1 != null) {
      _elvis = _feature_1;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    boolean _isEmpty = ((List<SortClause>) _elvis).isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("sort with group-by; groups follow the relevance of their best hit");
    }
    String _rawPath = grouping.getRawPath();
    boolean _tripleNotEquals = (_rawPath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("grouping by a raw path; Lance columns are declared");
    }
    Field _field = grouping.getField();
    boolean _not_1 = (!(_field instanceof PayloadField));
    if (_not_1) {
      String _fieldName = ModelUtil.fieldName(grouping.getField());
      String _plus = ("group-by on vector field " + _fieldName);
      throw Unsupported.of(_plus);
    }
    final ArrayList<String> names = CollectionLiterals.<String>newArrayList(this.schema.column(grouping.getField()));
    EList<Field> _subFields = grouping.getSubFields();
    for (final Field sub : _subFields) {
      String _fieldName_1 = ModelUtil.fieldName(sub);
      names.add(_fieldName_1);
    }
    boolean _has = table.has(IterableExtensions.<String>head(names));
    boolean _not_2 = (!_has);
    if (_not_2) {
      String _head = IterableExtensions.<String>head(names);
      String _plus_1 = ("grouping by " + _head);
      String _plus_2 = (_plus_1 + ", which the table no longer has");
      throw Unsupported.of(_plus_2);
    }
    double _xifexpression = (double) 0;
    boolean _written = ModelUtil.written(query, "groupSize");
    if (_written) {
      Object _feature_2 = this.feature(query, "groupSize");
      _xifexpression = ((Integer) _feature_2).doubleValue();
    } else {
      _xifexpression = 1.0;
    }
    final double size = _xifexpression;
    args.put("group", LancedbSchema.kw("key", Code.text(IterableExtensions.join(names, ".")), "size", Code.num(size)));
  }

  /**
   * A random sample of the matching rows; 'first' takes the first rows in key order.
   */
  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "size", Code.num(it.getSampleSize()));
      this.projection(table, it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("where", where.code());
      }
      this.readOptions(it, args);
      Code.Call _runtime = Code.runtime("sample", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A random sample with the nearest neighbours of each sampled row among the sample, as Qdrant's distance matrix.
   */
  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.queryExtensions(it);
      final LancedbTable table = this.schema.table(it.getCollection());
      final VectorField field = this.vectorField(it.getCollection(), it.getVectorField());
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of("a distance matrix of a multivector field");
      }
      final Code.Entries args = LancedbSchema.kw("table", table.layout(), "plan", LancedbSchema.kw("column", Code.text(this.schema.column(field)), "metric", Code.text(table.vectors().get(this.schema.column(field)))));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      final LancedbFilter where = this.filterOf(it, table, it.getFilter(), "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("where", where.code());
      }
      this.readOptions(it, args);
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + LancedbStatements.NO_GENERATION));
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
          final List<String> parameters = ModelUtil.parameterNames(query);
          List<Code.Stmt> _body = this.body(query);
          ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
          ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList();
          Program.Method _method = new Program.Method(helper, parameters, _body, _newArrayList, _newArrayList_1, true);
          this.helpers.add(_method);
          final Code.Entries passed = Code.entries();
          for (final String parameter : parameters) {
            Code.Param _param = new Code.Param(parameter);
            passed.put(parameter, _param);
          }
          Code.Call _call = new Code.Call(Code.Target.SELF, helper, passed);
          calls.add(_call);
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
    String _plus = (LancedbStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private static Code.Stmt run(final Code.Expr expr) {
    return new Code.Run(expr);
  }

  /**
   * Paging, bounds, score form, projection, sort, consistency and time travel of a ranked query or a scroll.
   */
  private void common(final EObject query, final LancedbTable table, final VectorCollection collection, final Code.Entries args) {
    boolean _written = ModelUtil.written(query, "offset");
    if (_written) {
      Object _feature = this.feature(query, "offset");
      args.put("offset", Code.num(((Integer) _feature).doubleValue()));
    }
    Object _feature_1 = this.feature(query, "bounds");
    final ScoreBounds bounds = ((ScoreBounds) _feature_1);
    if ((bounds != null)) {
      boolean _written_1 = ModelUtil.written(bounds, "min");
      if (_written_1) {
        args.put("score_min", Code.num(bounds.getMin()));
      }
      boolean _written_2 = ModelUtil.written(bounds, "max");
      if (_written_2) {
        args.put("score_max", Code.num(bounds.getMax()));
      }
    }
    if ((((ModelUtil.written(query, "scoreForm") && Objects.equals(this.feature(query, "scoreForm"), EScoreForm.NATIVE)) && (!(query instanceof KeywordSearch))) && (!(query instanceof HybridSearch)))) {
      args.put("native", Code.flag(true));
    }
    if (((ModelUtil.written(query, "scoreForm") && Objects.equals(this.feature(query, "scoreForm"), EScoreForm.NATIVE)) && (query instanceof HybridSearch))) {
      this.notes.add("score native on HybridSearch, since the fused score is the fusion function");
    }
    Object _elvis = null;
    Object _feature_2 = this.feature(query, "excluded");
    if (_feature_2 != null) {
      _elvis = _feature_2;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<Field> excluded = ((List<Field>) _elvis);
    Object _feature_3 = this.feature(query, "output");
    Object _feature_4 = this.feature(query, "withVectors");
    boolean _equals = Objects.equals(Boolean.TRUE, _feature_4);
    this.projection(table, collection, args, ((OutputSpec) _feature_3), excluded, _equals);
    Object _elvis_1 = null;
    Object _feature_5 = this.feature(query, "sortClauses");
    if (_feature_5 != null) {
      _elvis_1 = _feature_5;
    } else {
      ArrayList<Object> _newArrayList_1 = CollectionLiterals.<Object>newArrayList();
      _elvis_1 = _newArrayList_1;
    }
    this.order(table, ((List<SortClause>) _elvis_1), args, (!(query instanceof Scroll)));
    this.groupOf(table, query, args);
    if ((query instanceof Scroll)) {
      final LancedbFilter where = this.filterOf(query, table, ((Scroll) query).getFilter(), "f");
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("where", where.code());
      }
    }
    this.readOptions(query, args);
  }

  /**
   * Consistency (every read checks for newer versions) and time travel (as-of a version number or a tag).
   */
  private void readOptions(final EObject query, final Code.Entries args) {
    boolean _written = ModelUtil.written(query, "consistency");
    if (_written) {
      String _name = query.eClass().getName();
      String _plus = ("consistency on " + _name);
      String _plus_1 = (_plus + ", since every read checks for newer versions (read_consistency_interval 0)");
      this.notes.add(_plus_1);
    }
    Object _feature = this.feature(query, "asOf");
    final Literal asOf = ((Literal) _feature);
    if ((asOf != null)) {
      args.put("version", Literals.of(asOf));
    }
  }

  /**
   * Output columns (the key is always returned); vector columns come with 'with-vectors' or when the output names them.
   */
  private void projection(final LancedbTable table, final VectorCollection collection, final Code.Entries args, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
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
        final String name = this.schema.column(field);
        if ((((!Objects.equals(name, table.key())) && (!excluded.contains(field))) && table.has(name))) {
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
    args.put("output", Code.texts(fields));
    if (withVectors) {
      args.put("vectors", Code.flag(true));
    } else {
      boolean _isEmpty = vectors.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(vectors));
      }
    }
  }

  /**
   * Rejects clauses LanceDB lacks with their reason.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = LancedbStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String reason = LancedbStatements.REASONS.get(name);
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
   * Raises the pending message for clauses LanceDB supports that the generator does not produce yet.
   */
  private void later(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = LancedbStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        String _plus_1 = (_plus + "\' clause");
        String _plus_2 = (_plus_1 + LancedbStatements.PENDING);
        String _plus_3 = (_plus_2 + " (");
        String _get_1 = LancedbStatements.LATER.get(name);
        String _plus_4 = (_plus_3 + _get_1);
        String _plus_5 = (_plus_4 + ")");
        throw Unsupported.of(_plus_5);
      }
    }
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

  /**
   * LanceDB defines no vendor extension items; parameters in a lancedb block are rejected by name.
   */
  private void queryExtensions(final EObject host) {
    Set<String> _keySet = ModelUtil.extParams(host, LancedbGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
    final EStructuralFeature slot = host.eClass().getEStructuralFeature("extensions");
    if ((slot == null)) {
      return;
    }
    Object _eGet = host.eGet(slot);
    Iterable<VendorExtension> _filter = Iterables.<VendorExtension>filter(((List<?>) _eGet), VendorExtension.class);
    for (final VendorExtension block : _filter) {
      String _vendor = block.getVendor();
      boolean _equals = Objects.equals(_vendor, LancedbGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          String _name = item.eClass().getName();
          String _plus = (_name + " on LanceDB");
          throw Unsupported.of(_plus);
        }
      }
    }
  }

  private Code.Expr rows(final RecordSource source) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", LancedbSchema.kw("location", Code.text(((FileSource)source).getLocation())));
    }
    if (!_matched) {
      if (source instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Entries> _function = (RecordLiteral item) -> {
          return this.record(item);
        };
        _switchResult = Code.items(ListExtensions.<RecordLiteral, Code.Entries>map(((InlineRecords)source).getRecords(), _function));
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

  private Code.Entries record(final RecordLiteral record) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        result.put(this.schema.column(entry.getField()), Literals.of(entry.getValue()));
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
