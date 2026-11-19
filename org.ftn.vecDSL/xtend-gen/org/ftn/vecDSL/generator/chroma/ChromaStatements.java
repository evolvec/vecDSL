package org.ftn.vecDSL.generator.chroma;

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
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
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
import org.ftn.vecDSL.vecDSL.HybridSearch;
import org.ftn.vecDSL.vecDSL.Index;
import org.ftn.vecDSL.vecDSL.InlineRecords;
import org.ftn.vecDSL.vecDSL.Insert;
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
import org.ftn.vecDSL.vecDSL.VectorSearch;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Maps each vecDSL statement to Chroma operations in the host-language-neutral code model. Every operation is a call into
 * the per-language runtime module, which holds the SDK or HTTP calls; arguments are plain data in the vocabulary of Chroma's
 * HTTP API (collection configuration, records, where and where_document filters), so the three host languages send the same
 * requests.
 */
@SuppressWarnings("all")
public class ChromaStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; ChromaCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Chroma: ";

  public static final String NO_MODELS = "; generated Chroma programs supply their own vectors (Chroma embedding functions run in the client)";

  public static final String NO_RANKED_TEXT = "the local Chroma server ranks by vector distance only (its executor does not implement the Search API, which ranks BM25 and sparse vectors in Chroma Cloud)";

  public static final String NO_PARTITIONS = "partitions; a Chroma collection has no partitions or shard keys";

  public static final String NO_ALIASES = "aliases; Chroma has no collection aliases";

  public static final String NO_ANALYZERS = "Chroma has no analyzers: the document is indexed by a fixed full-text index";

  private static final String NO_GENERATION = "Chroma has no generation API (its queries return records)";

  private static final String NO_SNAPSHOTS = "snapshots; Chroma has no snapshot or backup API (a server keeps its data in its persist path)";

  private static final String NO_AUTH = "the open-source Chroma server has no authentication (chroma run allows every request)";

  private static final String SINGLE_NODE = "the open-source Chroma server is a single node without replicas or shards";

  private static final String METRICS = "metrics configuration; Chroma exports OpenTelemetry traces configured in its server config file (open_telemetry)";

  private static final String CLUSTER_LIMITS = "cluster limits; Chroma has no server limits on collections";

  private static final String SCHEDULES = "backup schedules; Chroma has no backups";

  private static final String EXTERNAL = "collections derived from external sources; Chroma stores the records of every collection itself";

  private static final String NO_LIMITS = "Chroma has no server-side field limits: the runtime checks the declared limits before each write";

  private static final String NO_INDEX_CHANGES = "Chroma keeps an HNSW index and an inverted index on every metadata key; the HTTP API changes no index after creation";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("exact", "exact")));

  /**
   * Why Chroma cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Chroma reads only the current state of a collection"), Pair.<String, String>of("cursor", "Chroma pages by limit and offset, not by a cursor"), Pair.<String, String>of("withTotal", "Chroma returns no total hit count with query results"), Pair.<String, String>of("explain", "Chroma returns no score explanation"), Pair.<String, String>of("highlighted", "Chroma returns no highlighted fragments"), Pair.<String, String>of("reranker", "the local Chroma server has no rerankers"), Pair.<String, String>of("elementScope", "Chroma metadata has no nested elements"), Pair.<String, String>of("aggregations", "the local Chroma server has no aggregations (the Search API is not implemented by its executor)"), Pair.<String, String>of("sortClauses", "Chroma orders query results by distance"), Pair.<String, String>of("queryModel", "generated Chroma programs embed no query text"), Pair.<String, String>of("mmr", "Chroma has no maximal marginal relevance"), Pair.<String, String>of("boost", "Chroma has no score boosting"), Pair.<String, String>of("groupBy", "the local Chroma server has no grouping (the Search API is not implemented by its executor)"), Pair.<String, String>of("prefetches", "Chroma runs one nearest-neighbour query per request, without stages"), Pair.<String, String>of("exact", "Chroma searches its HNSW graph and has no exhaustive search option")));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final ChromaSchema schema = new ChromaSchema(this.notes);

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public ChromaStatements(final Script script) {
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
            throw Unsupported.of(ChromaStatements.METRICS);
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
            throw Unsupported.of(ChromaStatements.CLUSTER_LIMITS);
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + ChromaStatements.SINGLE_NODE));
          }
          Set<String> _keySet = ModelUtil.extParams(concept, ChromaGenerator.VENDOR).keySet();
          for (final String name : _keySet) {
            throw Unsupported.of(("extension parameter " + name));
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
          ChromaSchema.checkName(((Database)concept).getName());
          Code.Call _runtime = Code.runtime("create_database", ChromaSchema.kw("name", Code.text(((Database)concept).getName())));
          Code.Run _run = new Code.Run(_runtime);
          final Code.Stmt create = ((Code.Stmt) _run);
          if (ifNotExists) {
            Code.Call _runtime_1 = Code.runtime("has_database", ChromaSchema.kw("name", Code.text(((Database)concept).getName())));
            Code.When _when = new Code.When(_runtime_1, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
            result.add(_when);
          } else {
            result.add(create);
          }
          EList<VectorCollection> _collections = ((Database)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.NO_ALIASES);
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
          String _fieldName = ModelUtil.fieldName(((Field)concept));
          String _plus = ("adding vector field " + _fieldName);
          String _plus_1 = (_plus + "; a Chroma collection holds one embedding per record");
          throw Unsupported.of(_plus_1);
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
          throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _ensureCollection = this.ensureCollection(this.member(concept, "a tenant"));
          Iterables.<Code.Stmt>addAll(result, _ensureCollection);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _ensureCollection = this.ensureCollection(this.member(concept, "a namespace"));
          Iterables.<Code.Stmt>addAll(result, _ensureCollection);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _plus_1 = (_plus + ChromaStatements.NO_RANKED_TEXT);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; Chroma has no file resource objects");
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of(("analyzers; " + ChromaStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of(("analyzer filters; " + ChromaStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("a field limit outside a field declaration; " + ChromaStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + ChromaStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + ChromaStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of("rerankers; the local Chroma server has no rerankers");
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization; the local Chroma server stores full-precision HNSW graphs");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + ChromaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + ChromaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("storage settings; Chroma keeps every collection in its persist path");
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
          throw Unsupported.of(("role bindings; " + ChromaStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          String _literal = ((CollectionLimit)concept).getType().getLiteral();
          String _plus = ("collection limits (" + _literal);
          String _plus_1 = (_plus + "); Chroma has no collection limits");
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (ChromaStatements.NOT_MAPPED + _name);
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
      throw Unsupported.of("embedded deployments; the Chroma TypeScript and Java clients connect to a server");
    }
    if ((((concept.getApiKey() != null) || (concept.getToken() != null)) || (concept.getPassword() != null))) {
      throw Unsupported.of(("credentials; " + ChromaStatements.NO_AUTH));
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      ChromaSchema.checkName(concept.getDatabaseName());
    }
    Set<String> _keySet = ModelUtil.extParams(concept, ChromaGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * Without credentials there is nothing to configure; every other authentication needs a server Chroma does not offer.
   */
  private void authentication(final AuthConfig auth) {
    EAuthType _type = auth.getType();
    boolean _notEquals = (!Objects.equals(_type, EAuthType.NONE));
    if (_notEquals) {
      String _literal = auth.getType().getLiteral();
      String _plus = (_literal + " authentication; ");
      String _plus_1 = (_plus + ChromaStatements.NO_AUTH);
      throw Unsupported.of(_plus_1);
    }
    if ((((((auth.getCertificatePath() != null) || (auth.getCmekKeyId() != null)) || auth.isAuditLog()) || auth.isFieldEncryption()) || (!auth.getRoles().isEmpty()))) {
      throw Unsupported.of(("certificates, customer-managed keys, audit logs, field encryption and roles; " + ChromaStatements.NO_AUTH));
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
    throw Unsupported.of((what + " outside a collection; Chroma adds it only to a collection"));
  }

  /**
   * A payload index on an existing collection: Chroma already keeps an inverted index on every metadata key.
   */
  private List<Code.Stmt> existingPayloadIndex(final PayloadIndex index) {
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
      this.schema.payloadIndex(collection, index);
      Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(collection)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A vector index on an existing collection: the HNSW parameters Chroma lets a collection change.
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
      boolean _written = ModelUtil.written(index, "buildCandidates");
      if (_written) {
        throw Unsupported.of("build-candidates on an existing collection; Chroma fixes ef_construction when it creates a collection");
      }
      if ((ModelUtil.written(index, "metric") && (!Objects.equals(index.getMetric(), this.schema.metric(field, ModelUtil.vectorIndex(collection, field)))))) {
        throw Unsupported.of("a metric change; Chroma fixes the HNSW space when it creates a collection");
      }
      final Code.Entries graph = this.schema.hnsw(index);
      boolean _isEmpty = graph.entries().isEmpty();
      if (_isEmpty) {
        Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(collection)));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
      }
      final Code.Entries args = this.schema.place(collection);
      args.put("hnsw", graph);
      Code.Stmt _run_1 = ChromaStatements.run(Code.runtime("alter_collection", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_1));
    }
    return _xblockexpression;
  }

  /**
   * Tenants and namespaces are values of a metadata key; creating one needs only the collection.
   */
  private List<Code.Stmt> ensureCollection(final VectorCollection collection) {
    Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(collection)));
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  /**
   * A payload field added to an existing collection: metadata is schemaless, so only a default needs writing.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _switchResult = null;
    boolean _matched = false;
    if (field instanceof VectorField) {
      _matched=true;
      String _fieldName = ModelUtil.fieldName(field);
      String _plus = ("adding vector field " + _fieldName);
      String _plus_1 = (_plus + "; a Chroma collection holds one embedding per record");
      throw Unsupported.of(_plus_1);
    }
    if (!_matched) {
      if (field instanceof PayloadField) {
        _matched=true;
        List<Code.Stmt> _xblockexpression = null;
        {
          this.schema.checkPayloadField(collection, ((PayloadField)field));
          if (((((PayloadField)field).isDocumentBody() || ((PayloadField)field).isFullText()) || (((PayloadField)field).getAnalyzer() != null))) {
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("adding document field " + _fieldName);
            String _plus_1 = (_plus + "; the generator takes the document from the collection declaration");
            throw Unsupported.of(_plus_1);
          }
          Literal _defaultValue = ((PayloadField)field).getDefaultValue();
          boolean _tripleNotEquals = (_defaultValue != null);
          if (_tripleNotEquals) {
            Code.Stmt _run = ChromaStatements.run(Code.runtime("fill_field", ChromaSchema.kw("collection", this.schema.writeLayout(collection), "field", 
              Code.text(ModelUtil.fieldName(field)), "value", Literals.of(((PayloadField)field).getDefaultValue()))));
            return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          Code.Stmt _run_1 = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(collection)));
          _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run_1));
        }
        _switchResult = _xblockexpression;
      }
    }
    return _switchResult;
  }

  private List<Code.Stmt> dropField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Field _primaryKey = collection.getPrimaryKey();
      boolean _tripleEquals = (field == _primaryKey);
      if (_tripleEquals) {
        throw Unsupported.of("dropping the primary key; Chroma record ids are the key text");
      }
      if ((field instanceof VectorField)) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("dropping vector field " + _fieldName);
        String _plus_1 = (_plus + "; every Chroma record has an embedding");
        throw Unsupported.of(_plus_1);
      }
      PayloadField _documentField = this.schema.documentField(collection);
      boolean _tripleEquals_1 = (field == _documentField);
      if (_tripleEquals_1) {
        throw Unsupported.of("dropping the document field; the generator takes the document from the collection declaration");
      }
      Code.Stmt _run = ChromaStatements.run(Code.runtime("drop_field", ChromaSchema.kw("collection", this.schema.writeLayout(collection), "field", Code.text(ModelUtil.fieldName(field)))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
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
        throw Unsupported.of("renaming the primary key; Chroma record ids are the key text");
      }
      if (((field instanceof VectorField) || (field == this.schema.documentField(collection)))) {
        throw Unsupported.of("renaming the embedding or the document; Chroma stores them without a field name");
      }
      Code.Stmt _run = ChromaStatements.run(Code.runtime("rename_field", ChromaSchema.kw("collection", this.schema.writeLayout(collection), "field", Code.text(ModelUtil.fieldName(field)), 
        "new_name", Code.text(operation.getNewName()))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: description and tags live in the collection metadata.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Expr value = Literals.of(operation.getValue());
      final Code.Entries args = this.schema.place(collection);
      List<Code.Stmt> _switchResult = null;
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case DESCRIPTION:
            List<Code.Stmt> _xblockexpression_1 = null;
            {
              args.put("metadata", ChromaSchema.kw("description", value));
              Code.Stmt _run = ChromaStatements.run(Code.runtime("alter_collection", args));
              _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            }
            _switchResult = _xblockexpression_1;
            break;
          case TAGS:
            List<Code.Stmt> _xblockexpression_2 = null;
            {
              args.put("metadata", ChromaSchema.kw("tags", value));
              Code.Stmt _run = ChromaStatements.run(Code.runtime("alter_collection", args));
              _xblockexpression_2 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            }
            _switchResult = _xblockexpression_2;
            break;
          case DYNAMIC:
            List<Code.Stmt> _xblockexpression_3 = null;
            {
              if (((!(value instanceof Code.Flag)) || (!((Code.Flag) value).value()))) {
                throw Unsupported.of("dynamic false; Chroma metadata is schemaless and accepts any key");
              }
              Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", args));
              _xblockexpression_3 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            }
            _switchResult = _xblockexpression_3;
            break;
          case CONSISTENCY:
            Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", args));
            _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            break;
          case TTL:
            throw Unsupported.of("collection ttl; Chroma records do not expire");
          case DELETION_PROTECTION:
            throw Unsupported.of("deletion-protection; Chroma collections have no deletion guard");
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
        String _plus_1 = (_plus + "; Chroma alters collections");
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
        ChromaSchema.checkName(it.getNewName());
        final Code.Entries args = this.schema.place(collection);
        args.put("new_name", Code.text(it.getNewName()));
        Code.Stmt _run = ChromaStatements.run(Code.runtime("rename_collection", args));
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
        throw Unsupported.of(ChromaStatements.NO_ALIASES);
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + ChromaStatements.NO_RANKED_TEXT);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + ChromaStatements.NO_RANKED_TEXT);
        throw Unsupported.of(_plus_1);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (ChromaStatements.NOT_MAPPED + _name);
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
          final Code.Stmt drop = ChromaStatements.run(Code.runtime("drop_collection", this.schema.place(((VectorCollection)concept))));
          Code.Stmt _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            Code.Call _runtime = Code.runtime("has_collection", this.schema.place(((VectorCollection)concept)));
            _xifexpression = new Code.When(_runtime, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
          } else {
            _xifexpression = drop;
          }
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final Code.Stmt drop = ChromaStatements.run(Code.runtime("drop_database", ChromaSchema.kw("name", Code.text(((Database)concept).getName()))));
            Code.Stmt _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              Code.Call _runtime = Code.runtime("has_database", ChromaSchema.kw("name", Code.text(((Database)concept).getName())));
              _xifexpression = new Code.When(_runtime, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
            } else {
              _xifexpression = drop;
            }
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
          }
          _switchResult = _xblockexpression_1;
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
        if (concept instanceof PayloadField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          String _fieldName = ModelUtil.fieldName(((Field)concept));
          String _plus = ("dropping vector field " + _fieldName);
          String _plus_1 = (_plus + "; every Chroma record has an embedding");
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a vector index; " + ChromaStatements.NO_INDEX_CHANGES));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a payload index; " + ChromaStatements.NO_INDEX_CHANGES));
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _plus_1 = (_plus + ChromaStatements.NO_RANKED_TEXT);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization; the local Chroma server stores full-precision HNSW graphs");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Chroma object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; Chroma drops databases and collections, not the server");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + ChromaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer; " + ChromaStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer filter; " + ChromaStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("dropping a field limit; " + ChromaStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of("file resources; Chroma has no file resource objects");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + ChromaStatements.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + ChromaStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of("rerankers; the local Chroma server has no rerankers");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + ChromaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + ChromaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("storage settings; Chroma keeps every collection in its persist path");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication; " + ChromaStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings; " + ChromaStatements.NO_AUTH));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(ChromaStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of("collection limits; Chroma has no collection limits");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((ChromaStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant or namespace deletes its records.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final boolean namespace, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries where = ChromaSchema.kw("where", ChromaSchema.kw(ChromaSchema.sliceKey(collection, namespace), ChromaSchema.kw("$eq", Code.text(value))));
      Code.Call _runtime = Code.runtime("delete_rows", ChromaSchema.kw("collection", this.schema.writeLayout(collection), "filter", where, "delete_all", Code.flag(true)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    throw Unsupported.of(ChromaStatements.NO_ALIASES);
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Chroma records do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "rows", this.rows(it.getSource()));
      final boolean upsert = (ModelUtil.written(it, "onConflict") && Collections.<EConflictPolicy>unmodifiableList(CollectionLiterals.<EConflictPolicy>newArrayList(EConflictPolicy.UPDATE, EConflictPolicy.REPLACE)).contains(it.getOnConflict()));
      if ((ModelUtil.written(it, "onConflict") && (!upsert))) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.writeOptions(it);
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
        throw Unsupported.of("per-statement ttl; Chroma records do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      this.writeOptions(it);
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
        throw Unsupported.of("if-version; Chroma records carry no version");
      }
      this.schema.note(it, "recordCountHint");
      this.noPartition(it);
      final VectorCollection updated = it.getCollection();
      final ChromaFilter where = this.filterOf(it, updated, it.getFilter());
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(updated));
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
      boolean _isEmpty_2 = it.getRemovedFields().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        final Function1<Field, String> _function_1 = (Field field) -> {
          return this.removable(updated, field);
        };
        args.put("remove", Code.texts(ListExtensions.<Field, String>map(it.getRemovedFields(), _function_1)));
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        args.put("mode", Code.text("replace"));
      }
      this.writeOptions(it);
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
    final Function1<RecordEntry, Field> _function = (RecordEntry entry) -> {
      return entry.getField();
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
        return ModelUtil.fieldName(it);
      };
      args.put("append", Code.texts(ListExtensions.<Field, String>map(update.getAppendFields(), _function_1)));
    }
    boolean _isEmpty_1 = update.getArrayRemoveFields().isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      final Function1<Field, String> _function_2 = (Field it) -> {
        return ModelUtil.fieldName(it);
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
        String _plus_1 = (_plus + "; every Chroma record has an embedding");
        throw Unsupported.of(_plus_1);
      }
      PayloadField _documentField = this.schema.documentField(collection);
      boolean _tripleEquals_1 = (field == _documentField);
      if (_tripleEquals_1) {
        throw Unsupported.of("remove-fields on the document; Chroma replaces a document but does not remove it");
      }
      _xblockexpression = ModelUtil.fieldName(field);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.noPartition(it);
      final ChromaFilter where = this.filterOf(it, it.getCollection(), it.getFilter());
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(it.getCollection()));
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
      this.writeOptions(it);
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
   * Chroma has no server-side import of data files: the runtime reads a local JSON or JSON Lines file and writes its
   * records as an insert (or an upsert for on-conflict update and replace) in batches.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Chroma has no ingestion pipeline (the runtime imports a local file once)");
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
        throw Unsupported.of((("import from " + scheme) + "; Chroma has no server-side import of data files (the runtime reads local JSON files)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the runtime reads JSON and JSON Lines files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism");
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "location", Code.text(it.getLocation()));
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
      this.writeOptions(it);
      this.writeScope(it, it.getCollection(), args);
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of("multi-statement transactions; Chroma applies each write request on its own");
  }

  /**
   * Chroma applies a write to its log and indexes before it acknowledges it, so 'wait' needs no call; extensions are rejected.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, ChromaGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  private void noPartition(final EObject statement) {
    Object _feature = this.feature(statement, "partition");
    boolean _tripleNotEquals = (_feature != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
    }
  }

  /**
   * Tenant and namespace values of a write go into the slice metadata keys.
   */
  private void writeScope(final EObject statement, final VectorCollection collection, final Code.Entries args) {
    this.noPartition(statement);
    final Code.Entries slice = Code.entries();
    Object _feature = this.feature(statement, "tenant");
    final Tenant tenant = ((Tenant) _feature);
    if ((tenant != null)) {
      slice.put(ChromaSchema.sliceKey(collection, false), Code.text(tenant.getName()));
    }
    Object _feature_1 = this.feature(statement, "namespace");
    final String namespace = ((String) _feature_1);
    if ((namespace != null)) {
      slice.put(ChromaSchema.sliceKey(collection, true), Code.text(namespace));
    }
    boolean _isEmpty = slice.entries().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("slice", slice);
    }
  }

  /**
   * A statement's filter with the conditions of its tenant and namespace.
   */
  private ChromaFilter filterOf(final EObject statement, final VectorCollection collection, final FilterBlock block) {
    ChromaFilter _xblockexpression = null;
    {
      PayloadField _documentField = this.schema.documentField(collection);
      final ChromaFilter result = new ChromaFilter(collection, _documentField).where(block);
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        result.where(ChromaSchema.kw(ChromaSchema.sliceKey(collection, false), ChromaSchema.kw("$eq", Code.text(tenant.getName()))));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        result.where(ChromaSchema.kw(ChromaSchema.sliceKey(collection, true), ChromaSchema.kw("$eq", Code.text(namespace))));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Chroma serves every collection; loading checks that it exists.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
      }
      Set<String> _keySet = ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(it.getCollection())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of("releasing a collection; Chroma loads HNSW segments on demand and has no release call");
  }

  /**
   * The local server commits every write to its log and applies it to the segments before it answers.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(it.getCollection())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * The local server compacts the log into the segments with every write, so a compaction has nothing left to do.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.schema.note(it, "targetSegments", "async");
      Code.Stmt _run = ChromaStatements.run(Code.runtime("require_collection", this.schema.place(it.getCollection())));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.schema.place(it.getCollection());
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
    throw Unsupported.of("embedding migration; generated Chroma programs embed no data");
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(ChromaStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(ChromaStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(ChromaStatements.NO_SNAPSHOTS);
  }

  /**
   * Forking is a Chroma Cloud feature; the runtime copies the records into a new collection with the same configuration.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; Chroma copies collections");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; the local Chroma server cannot fork a collection (Chroma Cloud only)");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("the \'as-of\' clause; Chroma reads only the current state of a collection");
      }
      Set<String> _keySet = ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      ChromaSchema.checkName(it.getTargetName());
      Code.Call _runtime = Code.runtime("clone_collection", ChromaSchema.kw("collection", this.schema.layout(it.getSourceCollection()), "target", Code.text(it.getTargetName())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RebuildIndex it) {
    throw Unsupported.of("rebuilding an index; Chroma maintains its indexes with every write and has no rebuild call");
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(ChromaStatements.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.writeLayout(it.getCollection()), "delete_all", Code.flag(true));
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        args.put("filter", ChromaSchema.kw("where", ChromaSchema.kw(ChromaSchema.sliceKey(it.getCollection(), true), ChromaSchema.kw("$eq", Code.text(it.getNamespace())))));
      }
      Code.Call _runtime = Code.runtime("delete_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = ChromaSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      final Concept owner = it.getScope();
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner instanceof Database)) {
              args.put("scope", ChromaSchema.kw("database", Code.text(((Database)owner).getName())));
            } else {
              if ((owner != null)) {
                String _name = owner.eClass().getName();
                String _plus = ("listing collections in a " + _name);
                String _plus_1 = (_plus + "; Chroma lists the collections of a database");
                throw Unsupported.of(_plus_1);
              }
            }
            break;
          case DATABASES:
            if (((owner != null) && (!(owner instanceof VectorDatabase)))) {
              String _name_1 = owner.eClass().getName();
              String _plus_2 = ("listing databases in a " + _name_1);
              throw Unsupported.of(_plus_2);
            }
            break;
          case TENANTS:
          case NAMESPACES:
            if ((!(owner instanceof VectorCollection))) {
              String _literal = it.getKind().getLiteral();
              String _plus_3 = ("listing " + _literal);
              String _plus_4 = (_plus_3 + " without a collection; they are values of a metadata key per collection");
              throw Unsupported.of(_plus_4);
            }
            final VectorCollection collection = ((VectorCollection) owner);
            args.put("kind", Code.text("slices"));
            EListKind _kind_1 = it.getKind();
            boolean _equals = Objects.equals(_kind_1, EListKind.NAMESPACES);
            args.put("key", Code.text(ChromaSchema.sliceKey(collection, _equals)));
            args.put("scope", this.schema.layout(collection));
            break;
          case INDEXES:
            throw Unsupported.of("listing indexes; Chroma indexes are not named objects");
          case PARTITIONS:
            throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
          case ALIASES:
            throw Unsupported.of(ChromaStatements.NO_ALIASES);
          case SNAPSHOTS:
            throw Unsupported.of(ChromaStatements.NO_SNAPSHOTS);
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
      Code.Call _runtime = Code.runtime("list_objects", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The local server indexes every write before it answers, so an index is ready as soon as its collection exists.
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
        String _plus = (ChromaStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final VectorCollection collection = _switchResult;
      if ((collection == null)) {
        throw Unsupported.of("a payload index on a raw path outside a collection");
      }
      this.schema.note(it, "state", "timeoutSeconds");
      Code.Call _runtime = Code.runtime("await_index", this.schema.place(collection));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "elementScope", "asOf", "cursor", "withTotal", "explain", "reranker", "highlighted", "sortClauses", 
        "aggregations", "boost", "queryModel", "mmr", "prefetches", "groupBy", "exact");
      final VectorField field = this.schema.vectorField(it.getCollection());
      if (((it.getVectorField() != null) && (it.getVectorField() != field))) {
        throw Unsupported.of("a vector field outside the collection");
      }
      final EMetric metric = this.schema.metric(field, ModelUtil.vectorIndex(it.getCollection(), field));
      if ((ModelUtil.written(it, "metric") && (!Objects.equals(it.getMetric(), metric)))) {
        throw Unsupported.of("a query metric other than the metric of the vector field; Chroma fixes the HNSW space per collection");
      }
      this.searchParams(it.getParams());
      this.queryExtensions(it);
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()), "vector", this.queryVector(it.getInput(), it.getCollection(), prelude), "limit", Code.num(it.getTopK()));
      ScoreBounds _bounds = it.getBounds();
      this.scoreBounds(_bounds, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      this.common(it, it.getCollection(), args, it.getOutput(), this.filterOf(it, it.getCollection(), it.getFilter()));
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _runtime = Code.runtime("vector_search", args);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    throw Unsupported.of(("keyword search; " + ChromaStatements.NO_RANKED_TEXT));
  }

  protected List<Code.Stmt> _body(final HybridSearch it) {
    throw Unsupported.of(("hybrid search; " + ChromaStatements.NO_RANKED_TEXT));
  }

  /**
   * The average-vector strategy over example records or vectors, computed in the runtime.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "boost", "groupBy");
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      final VectorField field = this.schema.vectorField(it.getCollection());
      if (((it.getVectorField() != null) && (it.getVectorField() != field))) {
        throw Unsupported.of("a vector field outside the collection");
      }
      this.searchParams(it.getParams());
      this.queryExtensions(it);
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()), "positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)), "limit", Code.num(it.getTopK()));
      boolean _isEmpty_1 = it.getNegatives().isEmpty();
      boolean _not = (!_isEmpty_1);
      if (_not) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      }
      if (((it.getLookupCollection() != null) && (it.getLookupCollection() != it.getCollection()))) {
        args.put("lookup", this.schema.layout(it.getLookupCollection()));
      }
      ScoreBounds _bounds = it.getBounds();
      this.scoreBounds(_bounds, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      this.common(it, it.getCollection(), args, it.getOutput(), this.filterOf(it, it.getCollection(), it.getFilter()));
      Code.Call _runtime = Code.runtime("recommend", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.noPartitions(it);
      this.queryExtensions(it);
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final ChromaFilter where = this.filterOf(it, it.getCollection(), null);
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
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
      this.noPartitions(it);
      this.queryExtensions(it);
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()));
      final ChromaFilter where = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
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
      boolean _written = ModelUtil.written(it, "sliceIndex");
      if (_written) {
        throw Unsupported.of("scroll slices; Chroma pages by limit and offset only");
      }
      this.noPartitions(it);
      this.queryExtensions(it);
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()));
      boolean _written_1 = ModelUtil.written(it, "scrollLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      this.common(it, it.getCollection(), args, it.getOutput(), this.filterOf(it, it.getCollection(), it.getFilter()));
      boolean _isEmpty = it.getSortClauses().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final VectorCollection source = it.getCollection();
        final Function1<SortClause, Code.Expr> _function = (SortClause clause) -> {
          return this.sortKey(source, clause);
        };
        args.put("order", Code.items(ListExtensions.<SortClause, Code.Expr>map(it.getSortClauses(), _function)));
      }
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A sort clause of a scroll, applied in the runtime: Chroma returns records in insertion order.
   */
  private Code.Expr sortKey(final VectorCollection collection, final SortClause clause) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        throw Unsupported.of("sort by relevance in a scroll");
      }
      final FieldPath path = clause.getField();
      if (((((path.getRawPath() != null) || (!path.getSubFields().isEmpty())) || (!(path.getField() instanceof PayloadField))) || (path.getField() == this.schema.documentField(collection)))) {
        String _xifexpression = null;
        Field _field = path.getField();
        boolean _tripleEquals = (_field == null);
        if (_tripleEquals) {
          _xifexpression = "a raw path";
        } else {
          _xifexpression = ModelUtil.fieldName(path.getField());
        }
        String _plus = ("sorting by " + _xifexpression);
        String _plus_1 = (_plus + "; the runtime sorts by metadata fields");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries result = ChromaSchema.kw("key", Code.text(ModelUtil.fieldName(path.getField())));
      ESortDirection _direction = clause.getDirection();
      boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
      if (_equals) {
        result.put("direction", Code.text("desc"));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A random sample of the matching records; 'first' takes the first records in primary key order.
   */
  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.noPartitions(it);
      this.queryExtensions(it);
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()), "size", Code.num(it.getSampleSize()));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      final ChromaFilter where = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      Code.Call _runtime = Code.runtime("sample_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A random sample with the nearest neighbours of each sampled record among the sample, as Qdrant's distance matrix.
   */
  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.noPartitions(it);
      this.queryExtensions(it);
      final VectorField field = this.schema.vectorField(it.getCollection());
      if (((it.getVectorField() != null) && (it.getVectorField() != field))) {
        throw Unsupported.of("a vector field outside the collection");
      }
      final Code.Entries args = ChromaSchema.kw("collection", this.schema.layout(it.getCollection()));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      final ChromaFilter where = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + ChromaStatements.NO_GENERATION));
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
    String _plus = (ChromaStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private static Code.Stmt run(final Code.Expr expr) {
    return new Code.Run(expr);
  }

  private void noPartitions(final EObject query) {
    Object _feature = this.feature(query, "partitions");
    final List<?> partitions = ((List<?>) _feature);
    if (((partitions != null) && (!partitions.isEmpty()))) {
      throw Unsupported.of(ChromaStatements.NO_PARTITIONS);
    }
  }

  private void common(final EObject query, final VectorCollection collection, final Code.Entries args, final OutputSpec output, final ChromaFilter where) {
    this.noPartitions(query);
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
   * Output fields (the key is always returned); the embedding comes with 'with-vectors' or when the output names it.
   */
  private void projection(final VectorCollection collection, final Code.Entries args, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    final Field key = collection.getPrimaryKey();
    final VectorField vector = this.schema.vectorField(collection);
    final List<String> fields = CollectionLiterals.<String>newArrayList();
    boolean vectors = withVectors;
    List<? extends Field> _xifexpression = null;
    if (((output == null) || output.isAllFields())) {
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(collection);
      _xifexpression = ((List<? extends Field>) _payloadFields);
    } else {
      _xifexpression = output.getFields();
    }
    final List<? extends Field> listed = _xifexpression;
    for (final Field field : listed) {
      if (((field != key) && (!excluded.contains(field)))) {
        if ((field == vector)) {
          vectors = true;
        } else {
          String _fieldName = ModelUtil.fieldName(field);
          fields.add(_fieldName);
        }
      }
    }
    args.put("output", Code.texts(fields));
    if (vectors) {
      args.put("vectors", Code.flag(true));
    }
  }

  /**
   * Rejects clauses Chroma lacks with their reason.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = ChromaStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String reason = ChromaStatements.REASONS.get(name);
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

  private Code.Expr queryVector(final QueryInput input, final VectorCollection collection, final List<Code.Stmt> prelude) {
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
            _switchResult = Literals.of(input.getValue());
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
              Code.Call _runtime = Code.runtime("lookup_vector", ChromaSchema.kw("collection", this.schema.layout(source), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let("query_vector", _runtime);
              prelude.add(_let);
              _xblockexpression_1 = new Code.Ref("query_vector");
            }
            _switchResult = _xblockexpression_1;
            break;
          case TEXT:
            throw Unsupported.of(("text input" + ChromaStatements.NO_MODELS));
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " query input; Chroma queries one dense embedding");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " query input; Chroma queries one dense embedding");
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Bounds on the canonical similarity, or on Chroma's distance with 'score native'; the runtime applies them.
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
   * Search effort settings: Chroma's ef_search is a collection setting, so per-query effort is listed as ignored.
   */
  private void searchParams(final SearchParams params) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall: Chroma exposes no query-time recall target");
    }
    boolean _isExhaustive = params.isExhaustive();
    if (_isExhaustive) {
      String _get = ChromaStatements.REASONS.get("exact");
      String _plus = ("the \'exhaustive\' parameter; " + _get);
      throw Unsupported.of(_plus);
    }
    this.schema.note(params, "candidatePool", "probes", "scanBudget", "timeoutMillis", "oversampling", "rescore", "filterStrategy");
    Set<String> _keySet = ModelUtil.extParams(params, ChromaGenerator.VENDOR).keySet();
    for (final String name : _keySet) {
      throw Unsupported.of(("extension parameter " + name));
    }
  }

  /**
   * Chroma defines no vendor extension items; parameters in a chroma block are rejected by name.
   */
  private void queryExtensions(final EObject host) {
    Set<String> _keySet = ModelUtil.extParams(host, ChromaGenerator.VENDOR).keySet();
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
      boolean _equals = Objects.equals(_vendor, ChromaGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          String _name = item.eClass().getName();
          String _plus = (_name + " on Chroma");
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
      _switchResult = Code.runtime("read_records", ChromaSchema.kw("location", Code.text(((FileSource)source).getLocation())));
    }
    if (!_matched) {
      if (source instanceof InlineRecords) {
        _matched=true;
        final Function1<RecordLiteral, Code.Expr> _function = (RecordLiteral item) -> {
          return this.record(item);
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
        result.put(ModelUtil.fieldName(entry.getField()), Literals.of(entry.getValue()));
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
