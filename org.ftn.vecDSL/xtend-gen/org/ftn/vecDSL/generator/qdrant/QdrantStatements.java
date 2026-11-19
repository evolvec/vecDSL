package org.ftn.vecDSL.generator.qdrant;

import com.google.common.collect.Iterables;
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
import org.ftn.vecDSL.vecDSL.ContextPair;
import org.ftn.vecDSL.vecDSL.ContextSearch;
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
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EFusionType;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMatchOperator;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
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
import org.ftn.vecDSL.vecDSL.OutputSpec;
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
 * Maps each vecDSL statement to Qdrant operations in the host-language-neutral code model. Every operation is a call
 * into the per-language runtime module, which holds the SDK calls; arguments are plain data in the JSON vocabulary of
 * the Qdrant REST API (collection layout, records, filters, search legs), so the three host languages send the same
 * requests.
 */
@SuppressWarnings("all")
public class QdrantStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; QdrantCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Qdrant: ";

  private static final String PENDING = " has no Qdrant mapping yet";

  private static final String NO_DATABASES = "logical databases; Qdrant keeps all collections of a server in one namespace (it has no database API)";

  private static final String NO_MODELS = "; a self-hosted Qdrant has no model objects (inference runs in Qdrant Cloud)";

  private static final String NO_GENERATION = "Qdrant has no generation API (its queries return points)";

  private static final String NO_ANALYZERS = "Qdrant has no analyzer objects: each text index carries its tokenizer and token filters";

  private static final String METRICS = "metrics configuration; Qdrant always serves Prometheus metrics at /metrics and configures telemetry in config.yaml";

  private static final String CLUSTER_LIMITS = "cluster limits; Qdrant sets max_collections for the whole server in config.yaml (storage.max_collections)";

  private static final String SCHEDULES = "backup schedules; Qdrant creates snapshots only on request";

  private static final String EXTERNAL = "collections derived from external sources; Qdrant stores the points of every collection itself";

  private static final String STORAGE_LOCATIONS = "snapshot locations; Qdrant writes snapshots to its snapshots path or to the S3, GCS or Azure storage configured on the server (storage.snapshots_config)";

  private static final String NO_LIMITS = "Qdrant has no server-side field limits: the runtime checks the declared limits before each write";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("params", "params")));

  /**
   * Why Qdrant cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Qdrant reads only the current state of a collection"), Pair.<String, String>of("cursor", "Qdrant pages queries by offset and scrolls by point id, not by an opaque cursor"), Pair.<String, String>of("withTotal", "Qdrant returns no total hit count with query results"), Pair.<String, String>of("explain", "Qdrant returns no score explanation"), Pair.<String, String>of("highlighted", "Qdrant returns no highlighted fragments"), Pair.<String, String>of("reranker", "a self-hosted Qdrant has no rerankers (prefetch stages rescore instead)"), Pair.<String, String>of("elementScope", "Qdrant scores whole points, not array elements"), Pair.<String, String>of("aggregations", "Qdrant aggregates only as facet counts over a filter, not over search results"), Pair.<String, String>of("sortClauses", "Qdrant orders query results by score (order_by replaces the query)"), Pair.<String, String>of("fuzziness", "Qdrant text matching has no fuzzy terms"), Pair.<String, String>of("slop", "Qdrant phrase matching has no slop"), Pair.<String, String>of("minShouldMatch", "Qdrant BM25 queries have no minimum-should-match"), Pair.<String, String>of("queryModel", "a self-hosted Qdrant embeds no query text (inference runs in Qdrant Cloud)"), Pair.<String, String>of("mmr", "Qdrant applies MMR to nearest-vector queries only")));

  /**
   * Clauses Qdrant 1.20 serves that the generator does not emit yet.
   */
  private static final Set<String> NATIVE_PENDING = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("boost", "groupBy"));

  private static final int DEFAULT_FUSION_K = 60;

  private static final List<String> ORDERINGS = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("weak", "medium", "strong"));

  private static final List<EPayloadIndexKind> ORDER_INDEXES = Collections.<EPayloadIndexKind>unmodifiableList(CollectionLiterals.<EPayloadIndexKind>newArrayList(EPayloadIndexKind.INTEGER, EPayloadIndexKind.FLOAT, EPayloadIndexKind.DATETIME));

  private final Script script;

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final QdrantSchema schema = new QdrantSchema(this.notes);

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public QdrantStatements(final Script script) {
    this.script = script;
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
        List<Code.Stmt> _createCollection = this.schema.createCollection(((VectorCollection)concept), ifNotExists);
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
            throw Unsupported.of(QdrantStatements.METRICS);
          }
          boolean _isEmpty = ((VectorDatabase)concept).getDatabases().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of(QdrantStatements.NO_DATABASES);
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
          AuthConfig _auth = ((VectorDatabase)concept).getAuth();
          boolean _tripleNotEquals_2 = (_auth != null);
          if (_tripleNotEquals_2) {
            List<Code.Stmt> _define_1 = this.define(((VectorDatabase)concept).getAuth(), ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _define_1);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          boolean _isEmpty = ((Cluster)concept).getLimits().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of(QdrantStatements.CLUSTER_LIMITS);
          }
          Set<String> _keySet = ModelUtil.extParams(concept, QdrantGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of(("extension parameter " + name));
          }
          ShardConfig _sharding = ((Cluster)concept).getSharding();
          boolean _tripleNotEquals = (_sharding != null);
          if (_tripleNotEquals) {
            QdrantSchema.shardNumber(((Cluster)concept).getSharding());
          }
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.NO_DATABASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          Code.Stmt _run = QdrantStatements.run(Code.runtime("create_alias", QdrantSchema.kw("alias", Code.text(((Alias)concept).getName()), "collection", Code.text(((Alias)concept).getCollection().getName()))));
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          List<Code.Stmt> _indexSettings = this.indexSettings(((VectorIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _indexSettings);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _existingPayloadIndex = this.existingPayloadIndex(((PayloadIndex)concept));
          Iterables.<Code.Stmt>addAll(result, _existingPayloadIndex);
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
          List<Code.Stmt> _createShardKey = this.createShardKey(this.member(concept, "a partition"), ((Partition)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createShardKey);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _tenantIndex = this.tenantIndex(this.member(concept, "a tenant"), false);
          Iterables.<Code.Stmt>addAll(result, _tenantIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _tenantIndex = this.tenantIndex(this.member(concept, "a namespace"), true);
          Iterables.<Code.Stmt>addAll(result, _tenantIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          List<Code.Stmt> _addFunction = this.addFunction(this.member(concept, "a function"), ((Function)concept));
          Iterables.<Code.Stmt>addAll(result, _addFunction);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; Qdrant has no file resource objects (stop word sets are written inline in a text index)");
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          this.schema.analyze(((Analyzer)concept), QdrantSchema.kw());
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer == null)) {
            throw Unsupported.of(("an analyzer filter outside an analyzer; " + QdrantStatements.NO_ANALYZERS));
          }
          this.schema.analyze(analyzer, QdrantSchema.kw());
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("a field limit outside a field declaration; " + QdrantStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + QdrantStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + QdrantStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers" + QdrantStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          List<Code.Stmt> _quantizationSettings = this.quantizationSettings(((Quantization)concept), false);
          Iterables.<Code.Stmt>addAll(result, _quantizationSettings);
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          List<Code.Stmt> _replication = this.replication(((ReplicationConfig)concept));
          Iterables.<Code.Stmt>addAll(result, _replication);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of("sharding of existing collections; Qdrant fixes shard_number when it creates a collection");
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          List<Code.Stmt> _storageSettings = this.storageSettings(((StorageConfig)concept));
          Iterables.<Code.Stmt>addAll(result, _storageSettings);
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
          throw Unsupported.of(("role bindings" + QdrantStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          String _literal = ((CollectionLimit)concept).getType().getLiteral();
          String _plus = ("collection limits (" + _literal);
          String _plus_1 = (_plus + "); Qdrant strict mode limits payload indexes, points and storage size, not partitions, fields, shards or vector dimensions");
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (QdrantStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The program endpoint: checked here, opened by connect().
   */
  private void connection(final Connection concept) {
    if ((((concept.getLocalPath() != null) || concept.isInMemory()) || Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED))) {
      throw Unsupported.of("embedded deployments; the Qdrant TypeScript and Java clients connect to a server");
    }
    if (((concept.getApiKey() instanceof StringLit) || (concept.getToken() instanceof StringLit))) {
      throw Unsupported.of(("an API key written in the script; set the environment variable " + QdrantPrinter.API_KEY_VARIABLE));
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(QdrantStatements.NO_DATABASES);
    }
    Set<String> _keySet = ModelUtil.extParams(concept, QdrantGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * API key and token authentication are client settings: the connection sends the key from the environment.
   */
  private void authentication(final AuthConfig auth) {
    boolean _contains = Collections.<EAuthType>unmodifiableList(CollectionLiterals.<EAuthType>newArrayList(EAuthType.NONE, EAuthType.API_KEY, EAuthType.TOKEN)).contains(auth.getType());
    boolean _not = (!_contains);
    if (_not) {
      String _literal = auth.getType().getLiteral();
      String _plus = (_literal + " authentication; Qdrant authenticates requests by API key or JWT");
      throw Unsupported.of(_plus);
    }
    Literal _token = auth.getToken();
    if ((_token instanceof StringLit)) {
      throw Unsupported.of(("an API key written in the script; set the environment variable " + QdrantPrinter.API_KEY_VARIABLE));
    }
    String _certificatePath = auth.getCertificatePath();
    boolean _tripleNotEquals = (_certificatePath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("client certificates; Qdrant serves TLS but does not authenticate clients by certificate");
    }
    if ((((auth.getCmekKeyId() != null) || auth.isAuditLog()) || auth.isFieldEncryption())) {
      throw Unsupported.of("customer-managed keys, audit logs and field encryption; a self-hosted Qdrant has none of them");
    }
    boolean _isEmpty = auth.getRoles().isEmpty();
    boolean _not_1 = (!_isEmpty);
    if (_not_1) {
      throw Unsupported.of(("role bindings" + QdrantStatements.PENDING));
    }
  }

  /**
   * The collection of a member concept: its container, or the collection of the enclosing create. Qdrant adds named
   * vectors, payload indexes, shard keys and tenant indexes only to a collection.
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
    throw Unsupported.of((what + " outside a collection; Qdrant adds it only to a collection"));
  }

  /**
   * Qdrant creates payload indexes on existing collections.
   */
  private List<Code.Stmt> existingPayloadIndex(final PayloadIndex index) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      final Field field = index.getTarget().getField();
      if (((index.getTarget().getRawPath() == null) && (!(field instanceof PayloadField)))) {
        throw Unsupported.of("a payload index on a vector field");
      }
      VectorCollection _elvis = null;
      VectorCollection _targetCollection = ModelUtil.targetCollection(index);
      if (_targetCollection != null) {
        _elvis = _targetCollection;
      } else {
        VectorCollection _owningCollection = ModelUtil.owningCollection(field);
        _elvis = _owningCollection;
      }
      final VectorCollection collection = _elvis;
      if ((collection == null)) {
        throw Unsupported.of("a payload index on a raw path outside a collection");
      }
      final Code.Entries args = QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(QdrantSchema.indexPath(index.getTarget())), "schema", this.schema.payloadIndex(collection, index));
      Code.Call _runtime = Code.runtime("create_payload_index", args);
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run));
    }
    return _xblockexpression;
  }

  /**
   * A vector index on an existing collection: the named vector's HNSW and quantization settings, updated in place.
   */
  private List<Code.Stmt> indexSettings(final VectorIndex index) {
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
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
      if (_equals) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("a vector index on sparse field " + _fieldName);
        String _plus_1 = (_plus + "; Qdrant indexes sparse vectors with its inverted index");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries params = QdrantSchema.kw();
      this.schema.vectorIndex(index, params);
      boolean _isEmpty = params.entries().isEmpty();
      if (_isEmpty) {
        params.put("hnsw_config", QdrantSchema.kw());
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", Code.text(collection.getName()), "vectors", QdrantSchema.kw(QdrantSchema.fieldKey(field), params))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Quantization of the named vector whose index declares it; drop disables it.
   */
  private List<Code.Stmt> quantizationSettings(final Quantization quantization, final boolean drop) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorIndex index = EcoreUtil2.<VectorIndex>getContainerOfType(quantization, VectorIndex.class);
      if ((index == null)) {
        throw Unsupported.of("quantization outside a vector index; Qdrant sets quantization per collection or named vector");
      }
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
        throw Unsupported.of("quantization of an index without a vector field");
      }
      final VectorCollection collection = this.member(field, "quantization");
      Record _xifexpression = null;
      if (drop) {
        _xifexpression = Code.text("Disabled");
      } else {
        _xifexpression = this.schema.quantizationConfig(quantization);
      }
      final Record config = ((Record)_xifexpression);
      Record _elvis_1 = null;
      if (((Record)config) != null) {
        _elvis_1 = ((Record)config);
      } else {
        Code.Text _text = Code.text("Disabled");
        _elvis_1 = _text;
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", Code.text(collection.getName()), "vectors", 
        QdrantSchema.kw(QdrantSchema.fieldKey(field), QdrantSchema.kw("quantization_config", _elvis_1)))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A replication factor of a cluster applies to each of its collections (Qdrant sets it per collection).
   */
  private List<Code.Stmt> replication(final ReplicationConfig config) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Cluster cluster = EcoreUtil2.<Cluster>getContainerOfType(config, Cluster.class);
      if ((cluster == null)) {
        throw Unsupported.of("replication outside a cluster; Qdrant sets replication_factor per collection");
      }
      Set<String> _keySet = ModelUtil.extParams(config, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<VectorCollection> _collections = cluster.getCollections();
      for (final VectorCollection collection : _collections) {
        Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", Code.text(collection.getName()), "params", QdrantSchema.kw("replication_factor", Code.num(config.getFactor())))));
        result.add(_run);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> storageSettings(final StorageConfig storage) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = ModelUtil.owningCollection(storage);
      if ((collection == null)) {
        throw Unsupported.of("storage settings outside a collection; Qdrant places the storage of each collection");
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", Code.text(collection.getName()), "params", 
        QdrantSchema.kw("payload", QdrantSchema.kw("memory", Code.text(QdrantSchema.payloadMemory(storage)))))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createShardKey(final VectorCollection collection, final Partition partition, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = ModelUtil.partitions(collection).isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("a partition of a collection declared without partitions; Qdrant adds shard keys only to collections created with custom sharding");
      }
      final Code.Entries args = QdrantSchema.kw("name", Code.text(collection.getName()), "shard_key", Code.text(QdrantSchema.shardKey(partition)));
      final Code.Stmt create = QdrantStatements.run(Code.runtime("create_shard_key", args));
      if ((!ifNotExists)) {
        return CollectionLiterals.<Code.Stmt>newArrayList(create);
      }
      Code.Call _runtime = Code.runtime("has_shard_key", args);
      Code.When _when = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * Tenants and namespaces are values of a payload key under a tenant index; creating one ensures the index.
   */
  private List<Code.Stmt> tenantIndex(final VectorCollection collection, final boolean namespace) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String key = QdrantSchema.sliceKey(collection, namespace);
      Code.Stmt _run = QdrantStatements.run(Code.runtime("create_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(key), "schema", QdrantSchema.sliceIndex())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A field added to an existing collection. A vector field becomes a named vector (Qdrant 1.20); payloads are
   * schemaless, so a payload field needs only its implied indexes and the backfill of its default.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(collection.getName());
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      boolean _matched = false;
      if (field instanceof VectorField) {
        _matched=true;
        boolean _isBm25Output = QdrantSchema.isBm25Output(collection, field);
        if (_isBm25Output) {
          throw Unsupported.of("a bm25 output field on its own; Qdrant scores BM25 over the text index of the input field");
        }
        EVectorDType _type = ((VectorField)field).getType();
        final boolean sparse = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
        Code.Text _text = Code.text(QdrantSchema.fieldKey(field));
        String _xifexpression = null;
        if (sparse) {
          _xifexpression = "sparse";
        } else {
          _xifexpression = "dense";
        }
        Code.Stmt _run = QdrantStatements.run(Code.runtime("create_vector", QdrantSchema.kw("name", name, "field", _text, _xifexpression, this.schema.vectorParams(collection, ((VectorField)field)))));
        result.add(_run);
      }
      if (!_matched) {
        if (field instanceof PayloadField) {
          _matched=true;
          this.schema.checkPayloadField(((PayloadField)field), (((PayloadField)field).isFullText() || (((PayloadField)field).getAnalyzer() != null)));
          final Code.Text key = Code.text(QdrantSchema.fieldKey(field));
          if ((((PayloadField)field).isFullText() || (((PayloadField)field).getAnalyzer() != null))) {
            Code.Stmt _run = QdrantStatements.run(Code.runtime("create_payload_index", QdrantSchema.kw("name", name, "field", key, "schema", this.schema.textIndex(collection, ((PayloadField)field), null))));
            result.add(_run);
          }
          boolean _isPartitionKey = ((PayloadField)field).isPartitionKey();
          if (_isPartitionKey) {
            Code.Stmt _run_1 = QdrantStatements.run(Code.runtime("create_payload_index", QdrantSchema.kw("name", name, "field", key, "schema", QdrantSchema.sliceIndex())));
            result.add(_run_1);
          }
          Literal _defaultValue = ((PayloadField)field).getDefaultValue();
          boolean _tripleNotEquals = (_defaultValue != null);
          if (_tripleNotEquals) {
            Code.Stmt _run_2 = QdrantStatements.run(Code.runtime("fill_field", QdrantSchema.kw("name", name, "field", key, "value", Literals.of(((PayloadField)field).getDefaultValue()))));
            result.add(_run_2);
          }
          boolean _isEmpty = result.isEmpty();
          if (_isEmpty) {
            Code.Stmt _run_3 = QdrantStatements.run(Code.runtime("require_collection", QdrantSchema.kw("name", name)));
            result.add(_run_3);
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; Qdrant derives point ids from it");
      }
      final Code.Text name = Code.text(collection.getName());
      final Code.Text key = Code.text(QdrantSchema.fieldKey(field));
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (field instanceof VectorField) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          final Function function = ModelUtil.producingFunction(collection, field);
          if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
            return this.dropFunction(collection, function);
          }
          Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_vector", QdrantSchema.kw("name", name, "field", key)));
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_field", QdrantSchema.kw("name", name, "field", key)));
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> renameField(final VectorCollection collection, final RenameFieldOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = operation.getField();
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("renaming the primary key; Qdrant derives point ids from it");
      }
      String _xifexpression = null;
      if ((field instanceof VectorField)) {
        _xifexpression = "rename_vector";
      } else {
        _xifexpression = "rename_field";
      }
      final String op = _xifexpression;
      Code.Call _runtime = Code.runtime(op, QdrantSchema.kw("collection", this.schema.layout(collection), "field", Code.text(QdrantSchema.fieldKey(field)), 
        "new_name", Code.text(operation.getNewName())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description and tags live in the collection metadata.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(collection.getName());
      final Code.Expr value = Literals.of(operation.getValue());
      List<Code.Stmt> _switchResult = null;
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case DESCRIPTION:
            Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", name, "metadata", QdrantSchema.kw("description", value))));
            _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            break;
          case TAGS:
            Code.Stmt _run_1 = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", name, "metadata", QdrantSchema.kw("tags", value))));
            _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_1));
            break;
          case DYNAMIC:
            List<Code.Stmt> _xblockexpression_1 = null;
            {
              if (((!(value instanceof Code.Flag)) || (!((Code.Flag) value).value()))) {
                throw Unsupported.of("dynamic false; Qdrant payloads are schemaless and accept any key");
              }
              Code.Stmt _run_2 = QdrantStatements.run(Code.runtime("require_collection", QdrantSchema.kw("name", name)));
              _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_2));
            }
            _switchResult = _xblockexpression_1;
            break;
          case TTL:
            throw Unsupported.of("collection ttl; Qdrant points do not expire");
          case CONSISTENCY:
            throw Unsupported.of("consistency as a collection property; Qdrant takes read consistency per request (the generator sends the level declared on the collection)");
          case DELETION_PROTECTION:
            throw Unsupported.of("deletion-protection; Qdrant collections have no deletion guard");
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A bm25 function: Qdrant scores BM25 over the text index of each input field.
   */
  private List<Code.Stmt> addFunction(final VectorCollection collection, final Function function) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EFunctionType _type = function.getType();
      boolean _notEquals = (!Objects.equals(_type, EFunctionType.BM25));
      if (_notEquals) {
        String _literal = function.getType().getLiteral();
        String _plus = (_literal + " functions; a self-hosted Qdrant computes BM25 over text indexes only (inference runs in Qdrant Cloud)");
        throw Unsupported.of(_plus);
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<Field> _inputs = function.getInputs();
      for (final Field input : _inputs) {
        {
          if ((!(input instanceof PayloadField))) {
            String _fieldName = ModelUtil.fieldName(input);
            String _plus_1 = ("bm25 input " + _fieldName);
            String _plus_2 = (_plus_1 + ", which is not a text field");
            throw Unsupported.of(_plus_2);
          }
          final PayloadField field = ((PayloadField) input);
          this.schema.checkPayloadField(field, true);
          Code.Stmt _run = QdrantStatements.run(Code.runtime("create_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(QdrantSchema.fieldKey(field)), 
            "schema", this.schema.textIndex(collection, field, null))));
          result.add(_run);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Without its bm25 function a field keeps a text index without scoring if it is still full-text, else loses it.
   */
  private List<Code.Stmt> dropFunction(final VectorCollection collection, final Function function) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EFunctionType _type = function.getType();
      boolean _notEquals = (!Objects.equals(_type, EFunctionType.BM25));
      if (_notEquals) {
        String _literal = function.getType().getLiteral();
        String _plus = (_literal + " functions; a self-hosted Qdrant computes BM25 over text indexes only (inference runs in Qdrant Cloud)");
        throw Unsupported.of(_plus);
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<Field> _inputs = function.getInputs();
      for (final Field input : _inputs) {
        {
          final Code.Text key = Code.text(QdrantSchema.fieldKey(input));
          if (((input instanceof PayloadField) && (((PayloadField) input).isFullText() || (((PayloadField) input).getAnalyzer() != null)))) {
            final Code.Entries params = this.schema.textIndex(collection, ((PayloadField) input), null);
            params.entries().remove("scoring");
            Code.Stmt _run = QdrantStatements.run(Code.runtime("create_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", key, "schema", params)));
            result.add(_run);
          } else {
            Code.Stmt _run_1 = QdrantStatements.run(Code.runtime("drop_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", key)));
            result.add(_run_1);
          }
        }
      }
      _xblockexpression = result;
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
        String _plus_1 = (_plus + "; Qdrant alters collections");
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
        Code.Stmt _run = QdrantStatements.run(Code.runtime("rename_collection", QdrantSchema.kw("name", Code.text(collection.getName()), "new_name", Code.text(it.getNewName()))));
        result.add(_run);
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
        Code.Stmt _run = QdrantStatements.run(Code.runtime("create_alias", QdrantSchema.kw("alias", Code.text(((AddAliasOp)operation).getNewAlias().getName()), "collection", Code.text(collection.getName()))));
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        _switchResult = this.addFunction(collection, ((AddFunctionOp)operation).getNewFunction());
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        _switchResult = this.dropFunction(collection, ((DropFunctionOp)operation).getFunction());
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (QdrantStatements.NOT_MAPPED + _name);
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
          final Code.Text name = Code.text(((VectorCollection)concept).getName());
          final Code.Stmt drop = QdrantStatements.run(Code.runtime("drop_collection", QdrantSchema.kw("name", name)));
          Code.Stmt _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            Code.Call _runtime = Code.runtime("has_collection", QdrantSchema.kw("name", name));
            _xifexpression = new Code.When(_runtime, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
          } else {
            _xifexpression = drop;
          }
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_alias", QdrantSchema.kw("alias", Code.text(((Alias)concept).getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_shard_key", QdrantSchema.kw("name", Code.text(this.member(concept, "a partition").getName()), "shard_key", Code.text(((Partition)concept).getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), false, ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), true, ((Namespace)concept).getKey());
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
            Code.Stmt _run = QdrantStatements.run(Code.runtime("alter_collection", QdrantSchema.kw("name", Code.text(this.member(field, "a vector index").getName()), "vectors", 
              QdrantSchema.kw(QdrantSchema.fieldKey(field), QdrantSchema.kw("hnsw_config", QdrantSchema.kw("m", Code.num(0), "payload_m", Code.num(0)))))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
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
              VectorCollection _owningCollection = ModelUtil.owningCollection(((PayloadIndex)concept).getTarget().getField());
              _elvis = _owningCollection;
            }
            final VectorCollection collection = _elvis;
            if ((collection == null)) {
              throw Unsupported.of("a payload index on a raw path outside a collection");
            }
            Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(QdrantSchema.indexPath(((PayloadIndex)concept).getTarget())))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a vector field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          _switchResult = this.dropFunction(this.member(concept, "a function"), ((Function)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          _switchResult = this.quantizationSettings(((Quantization)concept), true);
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.NO_DATABASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Qdrant object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; Qdrant drops collections, not the server");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of("dropping a cluster; Qdrant drops collections, not the cluster");
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer; " + QdrantStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer filter; " + QdrantStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("dropping a field limit; " + QdrantStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; Qdrant has no file resource objects (stop word sets are written inline in a text index)");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + QdrantStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + QdrantStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers" + QdrantStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of("dropping replication; Qdrant keeps a replication factor of at least 1 per collection");
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of("dropping sharding; Qdrant fixes shard_number when it creates a collection");
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("dropping storage settings; Qdrant keeps a memory placement for every collection component");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; the API key of a self-hosted Qdrant is a server setting (service.api_key)");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings" + QdrantStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(QdrantStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of("dropping a collection limit; Qdrant has no such limit (strict mode limits payload indexes, points and storage size)");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((QdrantStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant or namespace deletes its points.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final boolean namespace, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Code.Entries _kw = QdrantSchema.kw("key", Code.text(QdrantSchema.sliceKey(collection, namespace)), "match", QdrantSchema.kw("value", Code.text(value)));
      final Code.Entries where = QdrantSchema.kw("must", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_kw))));
      Code.Stmt _run = QdrantStatements.run(Code.runtime("delete_rows", QdrantSchema.kw("collection", this.schema.writeLayout(collection), "filter", where, "delete_all", Code.flag(true), "wait", Code.flag(true))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    Code.Call _runtime = Code.runtime("swap_alias", QdrantSchema.kw("alias", Code.text(it.getAlias().getName()), "collection", Code.text(it.getNewCollection().getName())));
    Code.Run _run = new Code.Run(_runtime);
    return CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run));
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Qdrant points do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "rows", this.rows(it.getSource()));
      final boolean upsert = (ModelUtil.written(it, "onConflict") && Collections.<EConflictPolicy>unmodifiableList(CollectionLiterals.<EConflictPolicy>newArrayList(EConflictPolicy.UPDATE, EConflictPolicy.REPLACE)).contains(it.getOnConflict()));
      if ((ModelUtil.written(it, "onConflict") && (!upsert))) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.writeOptions(it, args);
      this.writeScope(it, it.getCollection(), args);
      String _xifexpression = null;
      if (upsert) {
        _xifexpression = "upsert_rows";
      } else {
        _xifexpression = "insert_rows";
      }
      Code.Call _runtime = Code.runtime(_xifexpression, args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Qdrant points do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      this.writeOptions(it, args);
      this.writeScope(it, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("upsert_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        throw Unsupported.of("if-version; Qdrant conditional writes test a payload filter (update_filter), not a point version");
      }
      this.schema.note(it, "recordCountHint");
      final QdrantFilter where = new QdrantFilter().where(it.getFilter());
      this.sliceFilter(it, it.getCollection(), where);
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()));
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      boolean _isEmpty_1 = where.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("filter", where.code());
      }
      RecordLiteral _values = it.getValues();
      boolean _tripleNotEquals = (_values != null);
      if (_tripleNotEquals) {
        args.put("values", this.record(it.getValues()));
      }
      RecordSource _source = it.getSource();
      boolean _tripleNotEquals_1 = (_source != null);
      if (_tripleNotEquals_1) {
        args.put("rows", this.rows(it.getSource()));
      }
      final VectorCollection updated = it.getCollection();
      boolean _isEmpty_2 = it.getRemovedFields().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        final Function1<Field, String> _function_1 = (Field it_1) -> {
          return this.removable(updated, it_1);
        };
        args.put("remove", Code.texts(ListExtensions.<Field, String>map(it.getRemovedFields(), _function_1)));
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        args.put("mode", Code.text("replace"));
      }
      this.writeOptions(it, args);
      this.partitionOf(it, it.getCollection(), args);
      this.arrayItems(it, args);
      Code.Call _runtime = Code.runtime("update_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * array-append and array-remove take their items from the statement's set record.
   */
  private void arrayItems(final Update update, final Code.Entries args) {
    if ((update.getAppendFields().isEmpty() && update.getArrayRemoveFields().isEmpty())) {
      return;
    }
    RecordLiteral _values = update.getValues();
    boolean _tripleEquals = (_values == null);
    if (_tripleEquals) {
      throw Unsupported.of("array-append and array-remove without \'set\'; the items come from the set record");
    }
    final Function1<RecordEntry, Field> _function = (RecordEntry it) -> {
      return it.getField();
    };
    final List<Field> listed = IterableExtensions.<Field>toList(ListExtensions.<RecordEntry, Field>map(update.getValues().getEntries(), _function));
    EList<Field> _appendFields = update.getAppendFields();
    EList<Field> _arrayRemoveFields = update.getArrayRemoveFields();
    Iterable<Field> _plus = Iterables.<Field>concat(_appendFields, _arrayRemoveFields);
    for (final Field field : _plus) {
      {
        if (((!(field instanceof PayloadField)) || (!Objects.equals(((PayloadField) field).getType(), EScalarDType.ARRAY)))) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus_1 = ("array-append or array-remove on " + _fieldName);
          String _plus_2 = (_plus_1 + ", which is not an array field");
          throw Unsupported.of(_plus_2);
        }
        boolean _contains = listed.contains(field);
        boolean _not = (!_contains);
        if (_not) {
          String _fieldName_1 = ModelUtil.fieldName(field);
          String _plus_3 = ("array-append or array-remove on " + _fieldName_1);
          String _plus_4 = (_plus_3 + " without items in set");
          throw Unsupported.of(_plus_4);
        }
      }
    }
    boolean _isEmpty = update.getAppendFields().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      final Function1<Field, String> _function_1 = (Field it) -> {
        return QdrantSchema.fieldKey(it);
      };
      args.put("append", Code.texts(ListExtensions.<Field, String>map(update.getAppendFields(), _function_1)));
    }
    boolean _isEmpty_1 = update.getArrayRemoveFields().isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      final Function1<Field, String> _function_2 = (Field it) -> {
        return QdrantSchema.fieldKey(it);
      };
      args.put("remove_items", Code.texts(ListExtensions.<Field, String>map(update.getArrayRemoveFields(), _function_2)));
    }
  }

  private String removable(final VectorCollection collection, final Field field) {
    String _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("removing the primary key");
      }
      if ((field instanceof VectorField)) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("remove-fields on vector field " + _fieldName);
        String _plus_1 = (_plus + "; Qdrant removes payload keys");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = QdrantSchema.fieldKey(field);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final QdrantFilter where = new QdrantFilter().where(it.getFilter());
      this.sliceFilter(it, it.getCollection(), where);
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()));
      boolean _isDeleteAll = it.isDeleteAll();
      if (_isDeleteAll) {
        args.put("delete_all", Code.flag(true));
      }
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      boolean _isEmpty_1 = where.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("filter", where.code());
      }
      this.writeOptions(it, args);
      this.partitionOf(it, it.getCollection(), args);
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        args.put("limit", Code.num(it.getDeleteLimit()));
      }
      Code.Call _runtime = Code.runtime("delete_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Qdrant has no server-side import of data files: the runtime reads a local JSON or JSON Lines file and writes its
   * records as an insert (or an upsert for on-conflict update and replace) in batches.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Qdrant has no ingestion pipeline (the runtime imports a local file once)");
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
        throw Unsupported.of((("import from " + scheme) + "; Qdrant has no server-side import of data files (the runtime reads local JSON files)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the runtime reads JSON and JSON Lines files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism");
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "location", Code.text(it.getLocation()));
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        this.notes.add("async on BulkImport, which the runtime finishes before returning");
      }
      this.writeOptions(it, args);
      this.writeScope(it, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of("multi-statement transactions; Qdrant applies each write on its own (a batch update is ordered, not atomic)");
  }

  /**
   * 'wait' is Qdrant's own wait flag; the qdrant extension may set the write ordering (weak, medium, strong).
   */
  private void writeOptions(final EObject statement, final Code.Entries args) {
    this.schema.note(statement, "writeConsistency");
    Object _feature = this.feature(statement, "waitForPersistence");
    boolean _equals = Objects.equals(Boolean.TRUE, _feature);
    if (_equals) {
      args.put("wait", Code.flag(true));
    }
    Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(statement, QdrantGenerator.VENDOR).entrySet();
    for (final Map.Entry<String, Literal> entry : _entrySet) {
      {
        String _key = entry.getKey();
        boolean _notEquals = (!Objects.equals(_key, "ordering"));
        if (_notEquals) {
          String _key_1 = entry.getKey();
          String _plus = ("extension parameter " + _key_1);
          throw Unsupported.of(_plus);
        }
        final Literal value = entry.getValue();
        if (((!(value instanceof StringLit)) || (!QdrantStatements.ORDERINGS.contains(((StringLit) value).getValue())))) {
          throw Unsupported.of("write ordering other than weak, medium or strong");
        }
        args.put("ordering", Code.text(((StringLit) value).getValue()));
      }
    }
  }

  /**
   * Tenant and namespace values of a write go into the slice payload keys; the partition is the shard key.
   */
  private void writeScope(final EObject statement, final VectorCollection collection, final Code.Entries args) {
    final Code.Entries slice = Code.entries();
    Object _feature = this.feature(statement, "tenant");
    final Tenant tenant = ((Tenant) _feature);
    if ((tenant != null)) {
      slice.put(QdrantSchema.sliceKey(collection, false), Code.text(tenant.getName()));
    }
    Object _feature_1 = this.feature(statement, "namespace");
    final String namespace = ((String) _feature_1);
    if ((namespace != null)) {
      slice.put(QdrantSchema.sliceKey(collection, true), Code.text(namespace));
    }
    boolean _isEmpty = slice.entries().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("slice", slice);
    }
    this.partitionOf(statement, collection, args);
  }

  /**
   * The shard key of a write: its partition, or the collection's default partition.
   */
  private void partitionOf(final EObject statement, final VectorCollection collection, final Code.Entries args) {
    Partition _elvis = null;
    Partition _elvis_1 = null;
    Object _feature = this.feature(statement, "partition");
    if (((Partition) _feature) != null) {
      _elvis_1 = ((Partition) _feature);
    } else {
      Partition _defaultPartition = collection.getDefaultPartition();
      _elvis_1 = _defaultPartition;
    }
    if (_elvis_1 != null) {
      _elvis = _elvis_1;
    } else {
      final Function1<Partition, Boolean> _function = (Partition it) -> {
        return Boolean.valueOf(it.isIsDefault());
      };
      Partition _findFirst = IterableExtensions.<Partition>findFirst(ModelUtil.partitions(collection), _function);
      _elvis = _findFirst;
    }
    final Partition partition = _elvis;
    if ((partition != null)) {
      args.put("shard_key", Code.text(QdrantSchema.shardKey(partition)));
    } else {
      boolean _isEmpty = collection.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("a write without a partition to a collection with partitions; Qdrant requires the shard key of every write (declare a default partition)");
      }
    }
  }

  /**
   * Tenant and namespace of a statement as conditions on their payload keys.
   */
  private void sliceFilter(final EObject statement, final VectorCollection collection, final QdrantFilter where) {
    Object _feature = this.feature(statement, "tenant");
    final Tenant tenant = ((Tenant) _feature);
    if ((tenant != null)) {
      where.where(QdrantSchema.kw("key", Code.text(QdrantSchema.sliceKey(collection, false)), "match", QdrantSchema.kw("value", Code.text(tenant.getName()))));
    }
    Object _feature_1 = this.feature(statement, "namespace");
    final String namespace = ((String) _feature_1);
    if ((namespace != null)) {
      where.where(QdrantSchema.kw("key", Code.text(QdrantSchema.sliceKey(collection, true)), "match", QdrantSchema.kw("value", Code.text(namespace))));
    }
  }

  /**
   * The partitions a query reads: their shard keys.
   */
  private void readPartitions(final EObject query, final Code.Entries args) {
    Object _elvis = null;
    Object _feature = this.feature(query, "partitions");
    if (_feature != null) {
      _elvis = _feature;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<Partition> partitions = ((List<Partition>) _elvis);
    boolean _isEmpty = partitions.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      final Function1<Partition, String> _function = (Partition it) -> {
        return QdrantSchema.shardKey(it);
      };
      args.put("shard_key", Code.texts(ListExtensions.<Partition, String>map(partitions, _function)));
    }
  }

  /**
   * Qdrant serves every collection; loading checks that it exists.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EList<Partition> _partitions = it.getPartitions();
      for (final Partition partition : _partitions) {
        QdrantSchema.shardKey(partition);
      }
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("require_collection", QdrantSchema.kw("name", Code.text(it.getCollection().getName()))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of("releasing a collection; Qdrant serves every collection and has no release (memory placement is set per component: cold, cached or pinned)");
  }

  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("flush", QdrantSchema.kw("name", Code.text(it.getCollection().getName()))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Code.Entries args = QdrantSchema.kw("name", Code.text(it.getCollection().getName()));
      boolean _written = ModelUtil.written(it, "targetSegments");
      if (_written) {
        args.put("segments", Code.num(it.getTargetSegments()));
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        args.put("wait", Code.flag(false));
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("compact", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = QdrantSchema.kw("name", Code.text(it.getCollection().getName()));
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
    throw Unsupported.of("embedding migration; a self-hosted Qdrant embeds no data (inference runs in Qdrant Cloud)");
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ESnapshotScope _xifexpression = null;
      boolean _written = ModelUtil.written(it, "scope");
      if (_written) {
        _xifexpression = it.getScope();
      } else {
        _xifexpression = ESnapshotScope.COLLECTION;
      }
      final ESnapshotScope extent = _xifexpression;
      if ((Objects.equals(extent, ESnapshotScope.TENANT) || (it.getTenant() != null))) {
        throw Unsupported.of("tenant snapshots; a Qdrant snapshot holds a whole collection, shard or storage");
      }
      boolean _equals = Objects.equals(extent, ESnapshotScope.SHARD);
      if (_equals) {
        throw Unsupported.of(("shard snapshots" + QdrantStatements.PENDING));
      }
      boolean _isIncremental = it.isIncremental();
      if (_isIncremental) {
        throw Unsupported.of("incremental snapshots; a Qdrant snapshot always holds the whole collection");
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(QdrantStatements.STORAGE_LOCATIONS);
      }
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "description");
      final Code.Entries args = QdrantSchema.kw("name", Code.text(it.getCollection().getName()), "snapshot", Code.text(QdrantStatements.labelOf(it)));
      boolean _equals_1 = Objects.equals(extent, ESnapshotScope.FULL);
      if (_equals_1) {
        args.put("scope", Code.text("full"));
      }
      Code.Call _runtime = Code.runtime("create_snapshot", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The name Restore and DropSnapshot use: 'as', else the statement label, else the collection name.
   */
  private static String labelOf(final Snapshot snapshot) {
    String _elvis = null;
    String _elvis_1 = null;
    String _snapshotName = snapshot.getSnapshotName();
    if (_snapshotName != null) {
      _elvis_1 = _snapshotName;
    } else {
      String _name = snapshot.getName();
      _elvis_1 = _name;
    }
    if (_elvis_1 != null) {
      _elvis = _elvis_1;
    } else {
      String _name_1 = snapshot.getCollection().getName();
      _elvis = _name_1;
    }
    return _elvis;
  }

  /**
   * The collection the script snapshots under a name; the runtime searches the server when the script does not.
   */
  private String snapshotOwner(final String snapshot) {
    Object _xblockexpression = null;
    {
      if ((this.script == null)) {
        return null;
      }
      List<Snapshot> _eAllOfType = EcoreUtil2.<Snapshot>eAllOfType(this.script, Snapshot.class);
      for (final Snapshot statement : _eAllOfType) {
        String _labelOf = QdrantStatements.labelOf(statement);
        boolean _equals = Objects.equals(_labelOf, snapshot);
        if (_equals) {
          return statement.getCollection().getName();
        }
      }
      _xblockexpression = null;
    }
    return ((String)_xblockexpression);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "scope") && Objects.equals(it.getScope(), ESnapshotScope.FULL))) {
        throw Unsupported.of("restoring a storage snapshot; Qdrant recovers storage snapshots only when it starts (--storage-snapshot)");
      }
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope(), ESnapshotScope.COLLECTION)))) {
        String _literal = it.getScope().getLiteral();
        String _plus = (_literal + " restores; a Qdrant collection snapshot is recovered whole");
        throw Unsupported.of(_plus);
      }
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Code.Entries args = QdrantSchema.kw("name", Code.text(it.getCollection().getName()), "snapshot", Code.text(it.getSnapshotName()));
      final String owner = this.snapshotOwner(it.getSnapshotName());
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        args.put("location", Code.text(it.getLocation()));
      } else {
        if ((owner != null)) {
          args.put("source", Code.text(owner));
        }
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        args.put("wait", Code.flag(false));
      }
      Code.Call _runtime = Code.runtime("restore_snapshot", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(QdrantStatements.STORAGE_LOCATIONS);
      }
      final Code.Entries args = QdrantSchema.kw("snapshot", Code.text(it.getSnapshotName()));
      final String owner = this.snapshotOwner(it.getSnapshotName());
      if ((owner != null)) {
        args.put("source", Code.text(owner));
      }
      Code.Stmt _run = QdrantStatements.run(Code.runtime("drop_snapshot", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(QdrantStatements.NO_DATABASES);
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; a Qdrant recovery copies the segments of the snapshot");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("the \'as-of\' clause; Qdrant snapshots record the current state only");
      }
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Call _runtime = Code.runtime("clone_collection", QdrantSchema.kw("name", Code.text(it.getSourceCollection().getName()), "target", Code.text(it.getTargetName())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Index definition = it.getIndex();
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (definition instanceof PayloadIndex) {
        _matched=true;
        List<Code.Stmt> _xblockexpression_1 = null;
        {
          VectorCollection _elvis = null;
          VectorCollection _targetCollection = ModelUtil.targetCollection(definition);
          if (_targetCollection != null) {
            _elvis = _targetCollection;
          } else {
            VectorCollection _owningCollection = ModelUtil.owningCollection(((PayloadIndex)definition).getTarget().getField());
            _elvis = _owningCollection;
          }
          final VectorCollection collection = _elvis;
          if ((collection == null)) {
            throw Unsupported.of("a payload index on a raw path outside a collection");
          }
          this.schema.note(it, "concurrently");
          Code.Stmt _run = QdrantStatements.run(Code.runtime("rebuild_payload_index", QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(QdrantSchema.indexPath(((PayloadIndex)definition).getTarget())))));
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (definition instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of("rebuilding a vector index with unchanged parameters; Qdrant rebuilds HNSW when its parameters change (alter the index instead)");
        }
      }
      if (!_matched) {
        String _name = definition.eClass().getName();
        String _plus = (QdrantStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(QdrantStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = QdrantSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "delete_all", Code.flag(true));
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        Code.Entries _kw = QdrantSchema.kw("key", Code.text(QdrantSchema.sliceKey(it.getCollection(), true)), "match", QdrantSchema.kw("value", Code.text(it.getNamespace())));
        args.put("filter", QdrantSchema.kw("must", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_kw)))));
      }
      args.put("wait", Code.flag(true));
      Code.Call _runtime = Code.runtime("delete_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorCollection _xifexpression = null;
      Concept _scope = it.getScope();
      if ((_scope instanceof VectorCollection)) {
        Concept _scope_1 = it.getScope();
        _xifexpression = ((VectorCollection) _scope_1);
      }
      final VectorCollection owner = _xifexpression;
      if (((it.getScope() != null) && (owner == null))) {
        String _name = it.getScope().eClass().getName();
        String _plus = ("listing in a " + _name);
        String _plus_1 = (_plus + "; Qdrant lists objects of the server or of a collection");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries args = QdrantSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner != null)) {
              throw Unsupported.of("listing collections in a collection");
            }
            break;
          case DATABASES:
            throw Unsupported.of(QdrantStatements.NO_DATABASES);
          case TENANTS:
          case NAMESPACES:
            if ((owner == null)) {
              String _literal = it.getKind().getLiteral();
              String _plus_2 = ("listing " + _literal);
              String _plus_3 = (_plus_2 + " without a collection; Qdrant keeps them as values of a payload key per collection");
              throw Unsupported.of(_plus_3);
            }
            args.put("kind", Code.text("slices"));
            EListKind _kind_1 = it.getKind();
            boolean _equals = Objects.equals(_kind_1, EListKind.NAMESPACES);
            args.put("key", Code.text(QdrantSchema.sliceKey(owner, _equals)));
            break;
          case INDEXES:
          case PARTITIONS:
          case SNAPSHOTS:
            if ((owner == null)) {
              String _literal_1 = it.getKind().getLiteral();
              String _plus_4 = ("listing " + _literal_1);
              String _plus_5 = (_plus_4 + " without a collection; Qdrant keeps them per collection");
              throw Unsupported.of(_plus_5);
            }
            break;
          case ALIASES:
            break;
          default:
            break;
        }
      }
      if ((owner != null)) {
        args.put("scope", Code.text(owner.getName()));
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
      Code.Call _runtime = Code.runtime("list_objects", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Index definition = it.getIndex();
      Code.Entries _switchResult = null;
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        Code.Entries _xblockexpression_1 = null;
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
          _xblockexpression_1 = QdrantSchema.kw("name", Code.text(this.member(field, "a vector index").getName()), "field", Code.text(QdrantSchema.fieldKey(field)), "kind", Code.text("vector"));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          Code.Entries _xblockexpression_1 = null;
          {
            VectorCollection _elvis = null;
            VectorCollection _targetCollection = ModelUtil.targetCollection(definition);
            if (_targetCollection != null) {
              _elvis = _targetCollection;
            } else {
              VectorCollection _owningCollection = ModelUtil.owningCollection(((PayloadIndex)definition).getTarget().getField());
              _elvis = _owningCollection;
            }
            final VectorCollection collection = _elvis;
            if ((collection == null)) {
              throw Unsupported.of("a payload index on a raw path outside a collection");
            }
            _xblockexpression_1 = QdrantSchema.kw("name", Code.text(collection.getName()), "field", Code.text(QdrantSchema.indexPath(((PayloadIndex)definition).getTarget())), "kind", Code.text("payload"));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = definition.eClass().getName();
        String _plus = (QdrantStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final Code.Entries args = _switchResult;
      boolean _written = ModelUtil.written(it, "state");
      if (_written) {
        args.put("state", Code.text(it.getState().getLiteral()));
      }
      boolean _written_1 = ModelUtil.written(it, "timeoutSeconds");
      if (_written_1) {
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
      this.reject(it, "elementScope", "asOf", "cursor", "withTotal", "explain", "reranker", "highlighted", "sortClauses", 
        "aggregations", "boost", "queryModel");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      if ((ModelUtil.written(it, "metric") && (!Objects.equals(it.getMetric(), metric)))) {
        throw Unsupported.of("a query metric other than the metric of the vector field");
      }
      final boolean nativeScore = (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE));
      final ContextSearch context = IterableExtensions.<ContextSearch>head(Iterables.<ContextSearch>filter(QdrantSchema.extensionItems(it), ContextSearch.class));
      final Code.Entries search = this.searchSettings(it.getParams(), it.isExact());
      if ((context != null)) {
        return this.discover(it, field, context, nativeScore, search);
      }
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Expr vector = this.queryVector(it.getInput(), it.getCollection(), field, "query_vector", prelude);
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(QdrantSchema.fieldKey(field)), "vector", vector, 
        "limit", Code.num(it.getTopK()), "metric", Code.text(metric.getLiteral()));
      this.scoreBounds(it.getBounds(), nativeScore, args);
      this.common(it, it.getCollection(), args, it.getOutput(), this.scoped(it, it.getCollection(), new QdrantFilter().where(it.getFilter())));
      if ((search != null)) {
        args.put("params", search);
      }
      MmrConfig _mmr = it.getMmr();
      boolean _tripleNotEquals = (_mmr != null);
      if (_tripleNotEquals) {
        args.put("mmr", this.mmrConfig(it.getMmr(), field));
      }
      boolean _isEmpty = it.getPrefetches().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final VectorCollection source = it.getCollection();
        final Function1<Prefetch, Code.Expr> _function = (Prefetch step) -> {
          return this.stage(step, source);
        };
        args.put("prefetch", Code.items(ListExtensions.<Prefetch, Code.Expr>map(it.getPrefetches(), _function)));
      }
      FieldPath _groupBy = it.getGroupBy();
      boolean _tripleNotEquals_1 = (_groupBy != null);
      if (_tripleNotEquals_1) {
        boolean _written = ModelUtil.written(it, "offset");
        if (_written) {
          throw Unsupported.of("group-by with offset; Qdrant pages groups by limit only");
        }
        args.put("group_by", Code.text(QdrantSchema.indexPath(it.getGroupBy())));
        boolean _written_1 = ModelUtil.written(it, "groupSize");
        if (_written_1) {
          args.put("group_size", Code.num(it.getGroupSize()));
        }
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
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
   * Discovery or context search from the qdrant extension; its scores rank by the context pairs, so they stay native.
   */
  private List<Code.Stmt> discover(final VectorSearch search, final VectorField field, final ContextSearch context, final boolean nativeScore, final Code.Entries settings) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((!nativeScore)) {
        throw Unsupported.of("discover without \'score native\'; discovery scores count satisfied context pairs and have no similarity form");
      }
      if (((!search.getPrefetches().isEmpty()) || (search.getMmr() != null))) {
        throw Unsupported.of("discover with prefetch or mmr");
      }
      FieldPath _groupBy = search.getGroupBy();
      boolean _tripleNotEquals = (_groupBy != null);
      if (_tripleNotEquals) {
        throw Unsupported.of((("group-by" + QdrantStatements.PENDING) + " for discovery"));
      }
      final Function1<ContextPair, Code.Expr> _function = (ContextPair it) -> {
        Code.Entries _kw = QdrantSchema.kw("positive", Literals.of(it.getPositive()), "negative", Literals.of(it.getNegative()));
        return ((Code.Expr) _kw);
      };
      final List<Code.Expr> pairs = ListExtensions.<ContextPair, Code.Expr>map(context.getPairs(), _function);
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(search.getCollection(), search), "field", Code.text(QdrantSchema.fieldKey(field)), "context", Code.items(pairs), 
        "limit", Code.num(search.getTopK()));
      Literal _target = context.getTarget();
      boolean _tripleNotEquals_1 = (_target != null);
      if (_tripleNotEquals_1) {
        args.put("target", Literals.of(context.getTarget()));
      }
      this.scoreBounds(search.getBounds(), false, args);
      this.common(search, search.getCollection(), args, search.getOutput(), this.scoped(search, search.getCollection(), new QdrantFilter().where(search.getFilter())));
      if ((settings != null)) {
        args.put("params", settings);
      }
      this.readPartitions(search, args);
      Code.Call _runtime = Code.runtime("discover", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Entries mmrConfig(final MmrConfig mmr, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      if (((mmr.getVectorField() != null) && (mmr.getVectorField() != field))) {
        throw Unsupported.of("mmr on another vector field than the search");
      }
      double _diversityLambda = mmr.getDiversityLambda();
      double _minus = (1 - _diversityLambda);
      final Code.Entries config = QdrantSchema.kw("diversity", Code.num(_minus));
      boolean _written = ModelUtil.written(mmr, "candidates");
      if (_written) {
        config.put("candidates_limit", Code.num(mmr.getCandidates()));
      }
      _xblockexpression = config;
    }
    return _xblockexpression;
  }

  /**
   * A prefetch stage as a Qdrant prefetch: its own query, vector, limit, filter, parameters and nested stages.
   */
  private Code.Expr stage(final Prefetch step, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(step, "weight");
      if (_written) {
        throw Unsupported.of("stage weights outside rrf fusion");
      }
      QueryInput _input = step.getInput();
      boolean _tripleEquals = (_input == null);
      if (_tripleEquals) {
        throw Unsupported.of("a prefetch stage without input");
      }
      final VectorField field = this.searchField(collection, step.getVectorField());
      final EMetric metric = this.indexMetric(collection, field);
      if ((ModelUtil.written(step, "metric") && (!Objects.equals(step.getMetric(), metric)))) {
        throw Unsupported.of("a stage metric other than the metric of the vector field");
      }
      Code.Expr _switchResult = null;
      EQueryInputMode _kind = step.getInput().getKind();
      if (_kind != null) {
        switch (_kind) {
          case VECTOR:
          case SPARSE_VECTOR:
          case MULTI_VECTOR:
            _switchResult = this.vectorValue(Literals.of(step.getInput().getValue()));
            break;
          default:
            String _literal = step.getInput().getKind().getLiteral();
            String _plus = (_literal + " input in a prefetch stage");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = step.getInput().getKind().getLiteral();
        String _plus = (_literal + " input in a prefetch stage");
        throw Unsupported.of(_plus);
      }
      final Code.Expr vector = _switchResult;
      final Code.Entries entry = QdrantSchema.kw("query", vector, "using", Code.text(QdrantSchema.fieldKey(field)), "limit", Code.num(step.getStageLimit()));
      final QdrantFilter where = new QdrantFilter().where(step.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        entry.put("filter", where.code());
      }
      final Code.Entries search = this.searchSettings(step.getParams(), false);
      if ((search != null)) {
        entry.put("params", search);
      }
      boolean _isEmpty_1 = step.getNested().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        final Function1<Prefetch, Code.Expr> _function = (Prefetch it) -> {
          return this.stage(it, collection);
        };
        entry.put("prefetch", Code.items(ListExtensions.<Prefetch, Code.Expr>map(step.getNested(), _function)));
      }
      _xblockexpression = entry;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "fuzziness", "slop", "groupBy", "boost", "minShouldMatch");
      EQueryInputMode _kind = it.getInput().getKind();
      boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.TEXT));
      if (_notEquals) {
        String _literal = it.getInput().getKind().getLiteral();
        String _plus = ("keyword search with " + _literal);
        String _plus_1 = (_plus + " input");
        throw Unsupported.of(_plus_1);
      }
      final Code.Expr query = Literals.of(it.getInput().getValue());
      final PayloadField field = this.keywordField(it.getCollection(), it.getSearchFields());
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "query", query, "field", Code.text(QdrantSchema.fieldKey(field)), "limit", Code.num(it.getTopK()));
      final QdrantFilter where = new QdrantFilter().where(it.getFilter());
      this.termFilters(it, field, query, where);
      final Code.Entries bm25 = this.schema.bm25Params(it.getCollection(), field);
      if ((bm25 != null)) {
        args.put("bm25", bm25);
      }
      ScoreBounds _bounds = it.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        this.scoreBounds(it.getBounds(), false, args);
      }
      this.common(it, it.getCollection(), args, it.getOutput(), this.scoped(it, it.getCollection(), where));
      this.searchParams(it.getParams());
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("keyword_search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Operator 'and' and phrase queries become text conditions in the filter, beside the BM25 ranking.
   */
  private void termFilters(final EObject search, final PayloadField field, final Code.Expr query, final QdrantFilter where) {
    boolean _written = ModelUtil.written(search, "textQueryType");
    if (_written) {
      Object _feature = this.feature(search, "textQueryType");
      final ETextQueryType queryType = ((ETextQueryType) _feature);
      if (queryType != null) {
        switch (queryType) {
          case MATCH:
            break;
          case PHRASE:
            where.where(QdrantSchema.kw("key", Code.text(QdrantSchema.fieldKey(field)), "match", QdrantSchema.kw("phrase", query)));
            break;
          default:
            String _literal = queryType.getLiteral();
            String _plus = ("text-query " + _literal);
            String _plus_1 = (_plus + "; Qdrant ranks terms and filters phrases");
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = queryType.getLiteral();
        String _plus = ("text-query " + _literal);
        String _plus_1 = (_plus + "; Qdrant ranks terms and filters phrases");
        throw Unsupported.of(_plus_1);
      }
    }
    if ((ModelUtil.written(search, "matchOperator") && Objects.equals(this.feature(search, "matchOperator"), EMatchOperator.AND))) {
      where.where(QdrantSchema.kw("key", Code.text(QdrantSchema.fieldKey(field)), "match", QdrantSchema.kw("text", query)));
    }
  }

  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "groupBy", "boost");
      EFusionType _xifexpression = null;
      boolean _written = ModelUtil.written(it, "fusion");
      if (_written) {
        _xifexpression = it.getFusion();
      } else {
        _xifexpression = EFusionType.RRF;
      }
      final EFusionType fusionKind = _xifexpression;
      final List<Double> weights = this.legWeights(it, fusionKind);
      final Code.Entries shared = this.searchSettings(it.getParams(), false);
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final List<Code.Expr> mapped = CollectionLiterals.<Code.Expr>newArrayList();
      int index = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Code.Entries _switchResult = null;
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            _switchResult = this.vectorLeg(it, ((VectorLeg)leg), ("query_vector_" + Integer.valueOf(index)), prelude, shared);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              _switchResult = this.keywordLeg(it, ((KeywordLeg)leg));
            }
          }
          final Code.Entries entry = _switchResult;
          boolean _written_1 = ModelUtil.written(leg, "legLimit");
          if (_written_1) {
            Object _feature = this.feature(leg, "legLimit");
            entry.put("limit", Code.num(((Integer) _feature).doubleValue()));
          }
          if ((weights != null)) {
            entry.put("weight", Code.num((weights.get(index)).doubleValue()));
          }
          mapped.add(entry);
          index = (index + 1);
        }
      }
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "legs", Code.items(mapped), "limit", Code.num(it.getTopK()), "fusion", Code.text(fusionKind.getLiteral()));
      boolean _equals = Objects.equals(fusionKind, EFusionType.RRF);
      if (_equals) {
        int _xifexpression_1 = (int) 0;
        boolean _written_1 = ModelUtil.written(it, "fusionConstant");
        if (_written_1) {
          _xifexpression_1 = it.getFusionConstant();
        } else {
          _xifexpression_1 = QdrantStatements.DEFAULT_FUSION_K;
        }
        args.put("k", Code.num(_xifexpression_1));
      } else {
        boolean _written_2 = ModelUtil.written(it, "fusionConstant");
        if (_written_2) {
          String _literal = fusionKind.getLiteral();
          String _plus = ("fusion-k with " + _literal);
          String _plus_1 = (_plus + " fusion");
          throw Unsupported.of(_plus_1);
        }
      }
      ScoreBounds _bounds = it.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        this.scoreBounds(it.getBounds(), false, args);
      }
      boolean _written_3 = ModelUtil.written(it, "offset");
      if (_written_3) {
        args.put("offset", Code.num(it.getOffset()));
      }
      this.projection(it.getCollection(), args, it.getOutput(), it.getExcluded(), it.isWithVectors());
      this.readPartitions(it, args);
      this.queryExtensions(it);
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
        return null;
      }
      boolean _written = ModelUtil.written(search, "alpha");
      if (_written) {
        if ((((search.getLegs().size() != 2) || (!(IterableExtensions.<HybridLeg>head(search.getLegs()) instanceof VectorLeg))) || (weighted > 0))) {
          throw Unsupported.of("alpha on other than one vector leg followed by one other leg without weights");
        }
        double _alpha = search.getAlpha();
        double _alpha_1 = search.getAlpha();
        double _minus = (1 - _alpha_1);
        return Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(_alpha), Double.valueOf(_minus)));
      }
      if ((weighted == 0)) {
        final Function1<HybridLeg, Double> _function_1 = (HybridLeg it) -> {
          return Double.valueOf(1.0);
        };
        return ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_1);
      }
      int _size = search.getLegs().size();
      boolean _notEquals = (weighted != _size);
      if (_notEquals) {
        throw Unsupported.of("weights on some hybrid legs only");
      }
      final Function1<HybridLeg, Double> _function_2 = (HybridLeg it) -> {
        Object _feature = this.feature(it, "weight");
        return ((Double) _feature);
      };
      _xblockexpression = ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_2);
    }
    return _xblockexpression;
  }

  /**
   * Filter of a leg: the statement's filter, its tenant and namespace, and the leg's own filter.
   */
  private QdrantFilter legFilter(final HybridSearch search, final EObject leg) {
    Object _feature = this.feature(leg, "filter");
    return this.scoped(search, search.getCollection(), new QdrantFilter().where(search.getFilter())).where(((FilterBlock) _feature));
  }

  private Code.Entries vectorLeg(final HybridSearch search, final VectorLeg leg, final String variable, final List<Code.Stmt> prelude, final Code.Entries shared) {
    Code.Entries _xblockexpression = null;
    {
      final VectorField field = this.searchField(search.getCollection(), leg.getVectorField());
      final EMetric metric = this.indexMetric(search.getCollection(), field);
      if ((ModelUtil.written(leg, "metric") && (!Objects.equals(leg.getMetric(), metric)))) {
        throw Unsupported.of("a query metric other than the metric of the vector field");
      }
      QueryInput _elvis = null;
      QueryInput _input = leg.getInput();
      if (_input != null) {
        _elvis = _input;
      } else {
        QueryInput _input_1 = search.getInput();
        _elvis = _input_1;
      }
      final Code.Expr vector = this.queryVector(_elvis, search.getCollection(), field, variable, prelude);
      final Code.Entries entry = QdrantSchema.kw("kind", Code.text("vector"), "field", Code.text(QdrantSchema.fieldKey(field)), "vector", vector, "metric", Code.text(metric.getLiteral()));
      final QdrantFilter where = this.legFilter(search, leg);
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        entry.put("filter", where.code());
      }
      ScoreBounds _bounds = leg.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        this.scoreBounds(leg.getBounds(), false, entry);
      }
      final Code.Entries own = this.searchSettings(leg.getParams(), leg.isExact());
      if ((own != null)) {
        entry.put("params", own);
      } else {
        if ((shared != null)) {
          entry.put("params", shared);
        }
      }
      this.queryExtensions(leg);
      _xblockexpression = entry;
    }
    return _xblockexpression;
  }

  private Code.Entries keywordLeg(final HybridSearch search, final KeywordLeg leg) {
    Code.Entries _xblockexpression = null;
    {
      this.reject(leg, "minShouldMatch");
      QueryInput _elvis = null;
      QueryInput _input = leg.getInput();
      if (_input != null) {
        _elvis = _input;
      } else {
        QueryInput _input_1 = search.getInput();
        _elvis = _input_1;
      }
      final QueryInput keywordInput = _elvis;
      if (((keywordInput == null) || (!Objects.equals(keywordInput.getKind(), EQueryInputMode.TEXT)))) {
        throw Unsupported.of("a keyword leg without text input");
      }
      final Code.Expr query = Literals.of(keywordInput.getValue());
      final PayloadField field = this.keywordField(search.getCollection(), leg.getSearchFields());
      final Code.Entries entry = QdrantSchema.kw("kind", Code.text("keyword"), "field", Code.text(QdrantSchema.fieldKey(field)), "query", query);
      final QdrantFilter where = this.legFilter(search, leg);
      this.termFilters(leg, field, query, where);
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        entry.put("filter", where.code());
      }
      final Code.Entries bm25 = this.schema.bm25Params(search.getCollection(), field);
      if ((bm25 != null)) {
        entry.put("bm25", bm25);
      }
      this.queryExtensions(leg);
      _xblockexpression = entry;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "boost", "groupBy");
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return this.example(it_1);
      };
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(QdrantSchema.fieldKey(field)), 
        "positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)), "limit", Code.num(it.getTopK()), "metric", Code.text(metric.getLiteral()));
      boolean _isEmpty_1 = it.getNegatives().isEmpty();
      boolean _not = (!_isEmpty_1);
      if (_not) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
          return this.example(it_1);
        };
        args.put("negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      }
      if (((it.getLookupCollection() != null) && (it.getLookupCollection() != it.getCollection()))) {
        args.put("lookup", Code.text(it.getLookupCollection().getName()));
      }
      ScoreBounds _bounds = it.getBounds();
      this.scoreBounds(_bounds, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      this.common(it, it.getCollection(), args, it.getOutput(), this.scoped(it, it.getCollection(), new QdrantFilter().where(it.getFilter())));
      final Code.Entries search = this.searchSettings(it.getParams(), false);
      if ((search != null)) {
        args.put("params", search);
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("recommend", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Code.Expr example(final Literal literal) {
    Code.Expr _xblockexpression = null;
    {
      final Code.Expr value = Literals.of(literal);
      Code.Expr _xifexpression = null;
      if ((literal instanceof ListLit)) {
        _xifexpression = this.vectorValue(value);
      } else {
        _xifexpression = value;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      VectorCollection _collection = it.getCollection();
      QdrantFilter _qdrantFilter = new QdrantFilter();
      final QdrantFilter where = this.scoped(it, _collection, _qdrantFilter);
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("get_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it));
      final QdrantFilter where = this.scoped(it, it.getCollection(), new QdrantFilter().where(it.getFilter()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("count", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "cursor", "asOf");
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it));
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      final QdrantFilter where = new QdrantFilter().where(it.getFilter());
      boolean _written_1 = ModelUtil.written(it, "sliceIndex");
      if (_written_1) {
        where.where(QdrantSchema.kw("slice", QdrantSchema.kw("index", Code.num(it.getSliceIndex()), "total", Code.num(it.getSliceTotal()))));
      }
      this.common(it, it.getCollection(), args, it.getOutput(), this.scoped(it, it.getCollection(), where));
      final Code.Entries sorting = this.scrollOrder(it.getCollection(), it.getSortClauses());
      if ((sorting != null)) {
        args.put("order", sorting);
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Scroll order: one sort clause on a field with an integer, float or datetime index, else the integer key.
   */
  private Code.Entries scrollOrder(final VectorCollection collection, final List<SortClause> clauses) {
    Code.Entries _xblockexpression = null;
    {
      int _size = clauses.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        throw Unsupported.of("several sort clauses; Qdrant orders a scroll by one payload field");
      }
      boolean _isEmpty = clauses.isEmpty();
      if (_isEmpty) {
        String _keyType = QdrantSchema.keyType(collection);
        boolean _equals = Objects.equals(_keyType, "int");
        if (_equals) {
          return QdrantSchema.kw("key", Code.text(ModelUtil.fieldName(collection.getPrimaryKey())));
        }
        return null;
      }
      final SortClause clause = IterableExtensions.<SortClause>head(clauses);
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        throw Unsupported.of("sort by relevance in a scroll");
      }
      final String path = QdrantSchema.indexPath(clause.getField());
      final boolean indexed = (IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(collection.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
        return Boolean.valueOf((Objects.equals(QdrantSchema.indexPath(it.getTarget()), path) && QdrantStatements.ORDER_INDEXES.contains(it.getKind())));
      })) || ((clause.getField().getField() == collection.getPrimaryKey()) && Objects.equals(QdrantSchema.keyType(collection), "int")));
      if ((!indexed)) {
        throw Unsupported.of((("sort on " + path) + " without an integer, float or datetime payload index; Qdrant orders by indexed fields"));
      }
      final Code.Entries result = QdrantSchema.kw("key", Code.text(path));
      ESortDirection _direction = clause.getDirection();
      boolean _equals_1 = Objects.equals(_direction, ESortDirection.DESC);
      if (_equals_1) {
        result.put("direction", Code.text("desc"));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Qdrant's random sample query; 'first' takes the first records in primary key order.
   */
  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "size", Code.num(it.getSampleSize()));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      final QdrantFilter where = this.scoped(it, it.getCollection(), new QdrantFilter().where(it.getFilter()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("sample_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Qdrant's distance matrix (search matrix pairs): a random sample and the nearest neighbours of each sampled point.
   */
  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      if ((Objects.equals(field.getType(), EVectorDType.SPARSE_FLOAT_VECTOR) || field.isMultiVector())) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("a distance matrix over " + _fieldName);
        String _plus_1 = (_plus + "; Qdrant computes distance matrices over dense vectors");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries args = QdrantSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(QdrantSchema.fieldKey(field)), "metric", Code.text(this.indexMetric(it.getCollection(), field).getLiteral()));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      final QdrantFilter where = this.scoped(it, it.getCollection(), new QdrantFilter().where(it.getFilter()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.readPartitions(it, args);
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + QdrantStatements.NO_GENERATION));
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
    String _plus = (QdrantStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private static Code.Stmt run(final Code.Expr expr) {
    return new Code.Run(expr);
  }

  private Code.Entries layout(final VectorCollection collection, final EObject query) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = this.schema.layout(collection);
      boolean _written = ModelUtil.written(query, "consistency");
      if (_written) {
        Object _feature = this.feature(query, "consistency");
        final String level = this.schema.readConsistency(((EConsistencyLevel) _feature));
        if ((level == null)) {
          result.entries().remove("read_consistency");
        } else {
          result.put("read_consistency", Code.text(level));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A query's filter with the conditions of its tenant and namespace.
   */
  private QdrantFilter scoped(final EObject query, final VectorCollection collection, final QdrantFilter where) {
    QdrantFilter _xblockexpression = null;
    {
      this.sliceFilter(query, collection, where);
      _xblockexpression = where;
    }
    return _xblockexpression;
  }

  private void common(final EObject query, final VectorCollection collection, final Code.Entries args, final OutputSpec output, final QdrantFilter where) {
    boolean _isEmpty = where.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("filter", where.code());
    }
    boolean _written = ModelUtil.written(query, "offset");
    if (_written) {
      Object _feature = this.feature(query, "offset");
      args.put("offset", Code.num(((Integer) _feature).doubleValue()));
    }
    Object _elvis = null;
    Object _feature_1 = this.feature(query, "excluded");
    if (_feature_1 != null) {
      _elvis = _feature_1;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<Field> excluded = ((List<Field>) _elvis);
    Object _feature_2 = this.feature(query, "withVectors");
    boolean _equals = Objects.equals(Boolean.TRUE, _feature_2);
    this.projection(collection, args, output, excluded, _equals);
  }

  /**
   * Output fields (the key is always returned) and the named vectors to include.
   */
  private void projection(final VectorCollection collection, final Code.Entries args, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    final Field key = collection.getPrimaryKey();
    final List<String> fields = CollectionLiterals.<String>newArrayList();
    final List<String> vectors = CollectionLiterals.<String>newArrayList();
    final Function1<VectorField, Boolean> _function = (VectorField it) -> {
      boolean _isBm25Output = QdrantSchema.isBm25Output(collection, it);
      return Boolean.valueOf((!_isBm25Output));
    };
    final List<VectorField> stored = IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(collection), _function));
    List<? extends Field> _xifexpression = null;
    if (((output == null) || output.isAllFields())) {
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(collection);
      _xifexpression = ((List<? extends Field>) _payloadFields);
    } else {
      _xifexpression = output.getFields();
    }
    final List<? extends Field> listed = _xifexpression;
    final Function1<Field, Boolean> _function_1 = (Field it) -> {
      return Boolean.valueOf(((it != key) && (!excluded.contains(it))));
    };
    Iterable<? extends Field> _filter = IterableExtensions.filter(listed, _function_1);
    for (final Field field : _filter) {
      boolean _matched = false;
      if (field instanceof PayloadField) {
        _matched=true;
        String _fieldKey = QdrantSchema.fieldKey(field);
        fields.add(_fieldKey);
      }
      if (!_matched) {
        if (field instanceof VectorField) {
          _matched=true;
          boolean _contains = stored.contains(field);
          if (_contains) {
            String _fieldKey = QdrantSchema.fieldKey(field);
            vectors.add(_fieldKey);
          } else {
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("output of " + _fieldName);
            String _plus_1 = (_plus + ", which Qdrant derives from the text index");
            this.notes.add(_plus_1);
          }
        }
      }
    }
    if (withVectors) {
      final Function1<VectorField, String> _function_2 = (VectorField it) -> {
        return QdrantSchema.fieldKey(it);
      };
      List<String> _map = ListExtensions.<VectorField, String>map(stored, _function_2);
      Iterables.<String>addAll(vectors, _map);
    }
    args.put("output", Code.texts(fields));
    boolean _isEmpty = vectors.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("vectors", Code.texts(vectors.stream().distinct().toList()));
    }
  }

  /**
   * Rejects clauses Qdrant lacks with their reason, and names the clauses it serves but the generator does not emit yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = QdrantStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        boolean _contains = QdrantStatements.NATIVE_PENDING.contains(name);
        if (_contains) {
          throw Unsupported.of((clause + QdrantStatements.PENDING));
        }
        final String reason = QdrantStatements.REASONS.get(name);
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

  private VectorField searchField(final VectorCollection collection, final VectorField explicit) {
    VectorField _xblockexpression = null;
    {
      if ((explicit != null)) {
        boolean _isBm25Output = QdrantSchema.isBm25Output(collection, explicit);
        if (_isBm25Output) {
          String _fieldName = ModelUtil.fieldName(explicit);
          String _plus = ("vector search on the bm25 output " + _fieldName);
          String _plus_1 = (_plus + "; use keyword search");
          throw Unsupported.of(_plus_1);
        }
        return explicit;
      }
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        boolean _isBm25Output_1 = QdrantSchema.isBm25Output(collection, it);
        return Boolean.valueOf((!_isBm25Output_1));
      };
      final List<VectorField> candidates = IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(collection), _function));
      int _size = candidates.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        int _size_1 = candidates.size();
        String _plus_2 = ("vector search without \'using\' on a collection with " + Integer.valueOf(_size_1));
        String _plus_3 = (_plus_2 + " vector fields");
        throw Unsupported.of(_plus_3);
      }
      _xblockexpression = IterableExtensions.<VectorField>head(candidates);
    }
    return _xblockexpression;
  }

  private EMetric indexMetric(final VectorCollection collection, final VectorField field) {
    EMetric _xblockexpression = null;
    {
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
      if (_equals) {
        return EMetric.DOT;
      }
      _xblockexpression = this.schema.metric(field, ModelUtil.vectorIndex(collection, field));
    }
    return _xblockexpression;
  }

  private Code.Expr queryVector(final QueryInput input, final VectorCollection collection, final VectorField field, final String variable, final List<Code.Stmt> prelude) {
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
          case SPARSE_VECTOR:
          case MULTI_VECTOR:
            _switchResult = this.vectorValue(Literals.of(input.getValue()));
            break;
          case ID:
            Code.Ref _xblockexpression_1 = null;
            {
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final VectorCollection source = _elvis;
              Code.Call _runtime = Code.runtime("lookup_vector", QdrantSchema.kw("collection", this.schema.layout(source), 
                "field", Code.text(QdrantSchema.fieldKey(field)), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let(variable, _runtime);
              prelude.add(_let);
              _xblockexpression_1 = new Code.Ref(variable);
            }
            _switchResult = _xblockexpression_1;
            break;
          case TEXT:
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("text input for vector field " + _fieldName);
            String _plus_1 = (_plus + " (Qdrant Cloud Inference is not generated)");
            throw Unsupported.of(_plus_1);
          default:
            String _literal = input.getKind().getLiteral();
            String _plus_2 = (_literal + " query input");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus_2 = (_literal + " query input");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Bounds on the canonical similarity; the runtime passes a lower bound to Qdrant as score_threshold.
   */
  private void scoreBounds(final ScoreBounds bounds, final boolean nativeScore, final Code.Entries args) {
    if (nativeScore) {
      args.put("native", Code.flag(true));
    }
    if ((bounds == null)) {
      return;
    }
    boolean _written = ModelUtil.written(bounds, "min");
    if (_written) {
      args.put("score_min", Code.num(bounds.getMin()));
    }
    boolean _written_1 = ModelUtil.written(bounds, "max");
    if (_written_1) {
      args.put("score_max", Code.num(bounds.getMax()));
    }
  }

  /**
   * The one text field a keyword query ranks by BM25; a bm25 function's output stands for its input.
   */
  private PayloadField keywordField(final VectorCollection collection, final List<SearchFieldRef> refs) {
    PayloadField _xblockexpression = null;
    {
      final List<PayloadField> fields = CollectionLiterals.<PayloadField>newArrayList();
      for (final SearchFieldRef ref : refs) {
        {
          boolean _written = ModelUtil.written(ref, "boost");
          if (_written) {
            throw Unsupported.of("field boosts; a Qdrant text query ranks one field");
          }
          final Function function = ModelUtil.producingFunction(collection, ref.getField());
          List<Field> _xifexpression = null;
          if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
            _xifexpression = function.getInputs();
          } else {
            Field _field = ref.getField();
            _xifexpression = Collections.<Field>unmodifiableList(CollectionLiterals.<Field>newArrayList(_field));
          }
          final List<Field> named = _xifexpression;
          for (final Field field : named) {
            {
              boolean _isFullText = this.schema.isFullText(collection, field);
              boolean _not = (!_isFullText);
              if (_not) {
                String _fieldName = ModelUtil.fieldName(field);
                String _plus = ("keyword search on " + _fieldName);
                String _plus_1 = (_plus + ", which is not a full-text field");
                throw Unsupported.of(_plus_1);
              }
              boolean _contains = fields.contains(field);
              boolean _not_1 = (!_contains);
              if (_not_1) {
                fields.add(((PayloadField) field));
              }
            }
          }
        }
      }
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<PayloadField> _fullTextFields = this.schema.fullTextFields(collection);
        Iterables.<PayloadField>addAll(fields, _fullTextFields);
      }
      boolean _isEmpty_1 = fields.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search on a collection without full-text fields");
      }
      int _size = fields.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        throw Unsupported.of("keyword search over several fields; a Qdrant text query ranks one text index");
      }
      _xblockexpression = IterableExtensions.<PayloadField>head(fields);
    }
    return _xblockexpression;
  }

  /**
   * Qdrant search parameters: candidates as hnsw_ef, exact and exhaustive as exact, quantization rescoring.
   */
  private Code.Entries searchSettings(final SearchParams params, final boolean exact) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      if (exact) {
        result.put("exact", Code.flag(true));
      }
      if ((params != null)) {
        boolean _written = ModelUtil.written(params, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: Qdrant exposes no query-time recall target");
        }
        boolean _written_1 = ModelUtil.written(params, "candidatePool");
        if (_written_1) {
          result.put("hnsw_ef", Code.num(params.getCandidatePool()));
        }
        boolean _isExhaustive = params.isExhaustive();
        if (_isExhaustive) {
          result.put("exact", Code.flag(true));
        }
        final Code.Entries quantization = Code.entries();
        boolean _isRescore = params.isRescore();
        if (_isRescore) {
          quantization.put("rescore", Code.flag(true));
        }
        boolean _written_2 = ModelUtil.written(params, "oversampling");
        if (_written_2) {
          quantization.put("oversampling", Code.num(params.getOversampling()));
        }
        boolean _isEmpty = quantization.entries().isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          result.put("quantization", quantization);
        }
        this.schema.note(params, "probes", "scanBudget", "timeoutMillis", "filterStrategy");
        Set<String> _keySet = ModelUtil.extParams(params, QdrantGenerator.VENDOR).keySet();
        for (final String name : _keySet) {
          throw Unsupported.of(("extension parameter " + name));
        }
      }
      Code.Entries _xifexpression = null;
      boolean _isEmpty_1 = result.entries().isEmpty();
      if (_isEmpty_1) {
        _xifexpression = null;
      } else {
        _xifexpression = result;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void searchParams(final SearchParams params) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall: Qdrant exposes no query-time recall target");
    }
    this.schema.note(params, "candidatePool", "probes", "scanBudget", "timeoutMillis", "oversampling", "rescore", "filterStrategy", "exhaustive");
    Set<String> _keySet = ModelUtil.extParams(params, QdrantGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * Query-level qdrant extensions: discover is mapped by the vector search; parameters and feedback are not generated.
   */
  private void queryExtensions(final EObject host) {
    List<ExtensionItem> _extensionItems = QdrantSchema.extensionItems(host);
    for (final ExtensionItem item : _extensionItems) {
      boolean _matched = false;
      if (item instanceof ExtParam) {
        _matched=true;
        String _elvis = null;
        String _key = ((ExtParam)item).getKey();
        if (_key != null) {
          _elvis = _key;
        } else {
          String _quotedKey = ((ExtParam)item).getQuotedKey();
          _elvis = _quotedKey;
        }
        String _plus = ("extension parameter " + _elvis);
        throw Unsupported.of(_plus);
      }
      if (!_matched) {
        if (item instanceof ContextSearch) {
          _matched=true;
          if ((!(host instanceof VectorSearch))) {
            throw Unsupported.of("discover outside a vector search");
          }
        }
      }
      if (!_matched) {
        String _name = item.eClass().getName();
        String _plus = (_name + "; Qdrant needs strategy parameters for it that vecDSL 1.0 cannot write");
        throw Unsupported.of(_plus);
      }
    }
  }

  private Code.Expr rows(final RecordSource source) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", QdrantSchema.kw("location", Code.text(((FileSource)source).getLocation())));
    }
    if (!_matched) {
      if (source instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Expr> _function = (RecordLiteral it) -> {
          return this.record(it);
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

  private Code.Expr record(final RecordLiteral record) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      EList<RecordEntry> _entries = record.getEntries();
      for (final RecordEntry entry : _entries) {
        result.put(QdrantSchema.fieldKey(entry.getField()), this.vectorValue(Literals.of(entry.getValue())));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Sparse vectors in Qdrant's form: parallel lists of indices and values.
   */
  private Code.Expr vectorValue(final Code.Expr value) {
    Code.Entries _xblockexpression = null;
    {
      if ((!(value instanceof Code.Sparse))) {
        return value;
      }
      final Map<Long, Double> weights = ((Code.Sparse) value).weights();
      final Function1<Long, Code.Num> _function = (Long it) -> {
        return Code.num(it.doubleValue());
      };
      final Function1<Double, Code.Num> _function_1 = (Double it) -> {
        return Code.num((it).doubleValue());
      };
      _xblockexpression = QdrantSchema.kw("indices", Code.items(IterableExtensions.<Code.Num>toList(IterableExtensions.<Long, Code.Num>map(weights.keySet(), _function))), "values", Code.items(IterableExtensions.<Code.Num>toList(IterableExtensions.<Double, Code.Num>map(weights.values(), _function_1))));
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
