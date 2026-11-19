package org.ftn.vecDSL.generator.pinecone;

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
import org.ftn.vecDSL.vecDSL.EIndexBuildState;
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
 * Maps each vecDSL statement to Pinecone operations in the host-language-neutral code model. Every operation is
 * a call into the per-language runtime module, which holds the SDK and REST calls; arguments are plain data (the index
 * layout, records, Pinecone filter JSON, search legs), so the three host languages send the same requests.
 */
@SuppressWarnings("all")
public class PineconeStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; PineconeCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Pinecone: ";

  public static final String FIXED_SCHEMA = "the Pinecone index schema is fixed at creation";

  public static final String NO_ALIASES = "collection aliases; Pinecone aliases name namespaces inside one index, not indexes";

  public static final String NO_GENERATION = "a Pinecone index has no generation API (Pinecone Assistant is a separate product over uploaded files)";

  public static final String SERVERLESS = "Pinecone serverless indexes scale replicas and shards themselves, and API 2026-07 creates no pod-based indexes";

  public static final String NO_ACCESS_CONTROL = "Pinecone authenticates every request with a project API key; keys, roles and SSO are managed in the console or the Admin API";

  public static final String NO_ANALYZERS = "Pinecone configures text analysis on each full-text field when it creates the index";

  public static final String MANAGED_MODELS = "Pinecone hosts its embedding and reranking models; there is nothing to create or drop";

  public static final String METRICS = "metrics configuration; Pinecone exports index metrics through Prometheus and Datadog integrations of the project, not through the database API";

  public static final String NO_LIMITS = "Pinecone limits are set per plan and project; the runtime checks declared field limits before each write";

  public static final String NO_STORAGE = "storage settings; serverless indexes keep their data in Pinecone-managed object storage";

  public static final String NO_DATABASES = "logical databases; an API key reaches one Pinecone project, and projects are created through the Admin API with service-account credentials";

  public static final String SINGLE_TERM_PREFIX = "text-query prefix; Pinecone query_string expands * only after a quoted phrase of at least two terms, and a single-term prefix returns no matches on a word-tokenized field";

  public static final String NO_TERM_QUERY = "text-query term; Pinecone query_string matches analyzed tokens and describes no exact-token query";

  public static final String NO_WILDCARD = "text-query wildcard; Pinecone query_string has no wildcard other than * after a quoted phrase of at least two terms";

  public static final String NO_LOCAL_EMBEDDING = "generated programs do not embed text locally; give the field a model (Pinecone integrated embedding) or the query a model clause (Pinecone Inference embed)";

  public static final String STORED_TEXT = "Pinecone stores the text of an embedded field, not its vector";

  public static final String RERANK_TEXT = "Pinecone rerank scores documents against a query text";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("sliceIndex", "slice"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("exact", "exact"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("params", "params")));

  /**
   * Why Pinecone cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Pinecone reads only the current state of a namespace"), Pair.<String, String>of("cursor", "Pinecone search returns no pagination token, and list pagination tokens follow the order of document ids, not the primary-key order of a vecDSL scroll"), Pair.<String, String>of("withTotal", "Pinecone search returns no total hit count"), Pair.<String, String>of("explain", "Pinecone search returns no score explanation"), Pair.<String, String>of("highlighted", "Pinecone search returns no highlighted fragments"), Pair.<String, String>of("elementScope", "Pinecone metadata has no nested elements"), Pair.<String, String>of("aggregations", "Pinecone search has no aggregations"), Pair.<String, String>of("sortClauses", "Pinecone orders search matches by score"), Pair.<String, String>of("mmr", "Pinecone search has no maximal marginal relevance"), Pair.<String, String>of("boost", "Pinecone search has no score functions (field values, decay); query_string boosts weight query terms only"), Pair.<String, String>of("groupBy", "Pinecone search has no grouping"), Pair.<String, String>of("prefetches", "Pinecone scores each search in one stage (score_by), without prefetch stages"), Pair.<String, String>of("exact", "Pinecone serverless search has no exhaustive mode"), Pair.<String, String>of("minShouldMatch", "Pinecone text scoring has no minimum-should-match"), Pair.<String, String>of("reranker", ("a recommend search has examples but no query text, and " + PineconeStatements.RERANK_TEXT)), Pair.<String, String>of("sliceIndex", "Pinecone lists the documents of a namespace in one sequence of pages")));

  /**
   * Parameters of Pinecone Inference for a query text: embedded as a query, truncated rather than rejected when too long.
   */
  private static final List<Pair<String, String>> QUERY_EMBEDDING = Collections.<Pair<String, String>>unmodifiableList(CollectionLiterals.<Pair<String, String>>newArrayList(Pair.<String, String>of("input_type", "query"), Pair.<String, String>of("truncate", "END")));

  /**
   * The Starter reranker reads at most 1024 tokens of query and document and fails on longer input without truncate END.
   */
  private static final String TRUNCATING_RERANKER = "bge-reranker-v2-m3";

  private static final int DEFAULT_FUSION_K = 60;

  private static final Map<String, Integer> SCHEDULE_DAYS = Collections.<String, Integer>unmodifiableMap(CollectionLiterals.<String, Integer>newHashMap(Pair.<String, Integer>of("daily", Integer.valueOf(1)), Pair.<String, Integer>of("weekly", Integer.valueOf(7)), Pair.<String, Integer>of("monthly", Integer.valueOf(31))));

  private final Script script;

  private final String defaultNamespace;

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final PineconeSchema schema = new PineconeSchema(this.notes);

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public PineconeStatements(final Script script, final String defaultNamespace) {
    this.script = script;
    this.defaultNamespace = defaultNamespace;
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
        List<Code.Stmt> _createCollection = this.schema.createCollection(((VectorCollection)concept), ifNotExists, this.indexDeployment());
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
            throw Unsupported.of(PineconeStatements.METRICS);
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
            throw Unsupported.of(("cluster limits; " + PineconeStatements.NO_LIMITS));
          }
          if (((((Cluster)concept).getReplication() != null) || (((Cluster)concept).getSharding() != null))) {
            throw Unsupported.of(("replication and sharding; " + PineconeStatements.SERVERLESS));
          }
          this.extensionArgs(concept);
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists, this.indexDeployment());
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_DATABASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_ALIASES);
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
          String _plus_1 = (_plus + "; ");
          String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
          throw Unsupported.of(_plus_2);
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
          throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _createNamespace = this.createNamespace(this.member(concept, "a tenant"), ((Tenant)concept).getName(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createNamespace);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _createNamespace = this.createNamespace(this.member(concept, "a namespace"), ((Namespace)concept).getKey(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createNamespace);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = ("adding a " + _literal);
          String _plus_1 = (_plus + " function to an existing collection; ");
          String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
          throw Unsupported.of(_plus_2);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of(("analyzers outside a field; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of(("analyzer filters outside a field; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("a field limit outside a field declaration; " + PineconeStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          String _localPath = ((EmbeddingModel)concept).getLocalPath();
          boolean _tripleNotEquals = (_localPath != null);
          if (_tripleNotEquals) {
            throw Unsupported.of("local model files; Pinecone runs the models it hosts");
          }
          if (((ModelUtil.written(concept, "chunkStrategy") || ModelUtil.written(concept, "chunkSize")) || ModelUtil.written(concept, "chunkOverlap"))) {
            throw Unsupported.of("chunking; Pinecone embeds each field value whole");
          }
          String _provider = ((EmbeddingModel)concept).getProvider();
          String _elvis = null;
          String _modelId = ((EmbeddingModel)concept).getModelId();
          if (_modelId != null) {
            _elvis = _modelId;
          } else {
            String _name = ((EmbeddingModel)concept).getName();
            _elvis = _name;
          }
          List<Code.Stmt> _hostedModel = this.hostedModel(_provider, _elvis, concept);
          Iterables.<Code.Stmt>addAll(result, _hostedModel);
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + PineconeStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          String _provider = ((Reranker)concept).getProvider();
          String _elvis = null;
          String _modelId = ((Reranker)concept).getModelId();
          if (_modelId != null) {
            _elvis = _modelId;
          } else {
            String _name = ((Reranker)concept).getName();
            _elvis = _name;
          }
          List<Code.Stmt> _hostedModel = this.hostedModel(_provider, _elvis, concept);
          Iterables.<Code.Stmt>addAll(result, _hostedModel);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          List<Code.Stmt> _quantizationSettings = this.quantizationSettings(((Quantization)concept));
          Iterables.<Code.Stmt>addAll(result, _quantizationSettings);
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + PineconeStatements.SERVERLESS));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + PineconeStatements.SERVERLESS));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_STORAGE);
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
          throw Unsupported.of(("role bindings; " + PineconeStatements.NO_ACCESS_CONTROL));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          List<Code.Stmt> _backupSchedule = this.backupSchedule(((BackupSchedule)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _backupSchedule);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(("cluster limits; " + PineconeStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          String _literal = ((CollectionLimit)concept).getType().getLiteral();
          String _plus = ("collection limits (" + _literal);
          String _plus_1 = (_plus + "); ");
          String _plus_2 = (_plus_1 + PineconeStatements.NO_LIMITS);
          throw Unsupported.of(_plus_2);
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (PineconeStatements.NOT_MAPPED + _name);
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
      throw Unsupported.of("embedded deployments; Pinecone is a managed service");
    }
    if (((concept.getApiKey() instanceof StringLit) || (concept.getToken() instanceof StringLit))) {
      throw Unsupported.of(("an API key written in the script; set the environment variable " + PineconePrinter.API_KEY_VARIABLE));
    }
    String _host = concept.getHost();
    boolean _tripleNotEquals = (_host != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("connection host; give the control-plane endpoint as \'uri\'");
    }
    this.extensionArgs(concept);
  }

  /**
   * API-key authentication is what every generated program does; other kinds and access control are outside the API.
   */
  private void authentication(final AuthConfig auth) {
    EAuthType _type = auth.getType();
    boolean _notEquals = (!Objects.equals(_type, EAuthType.API_KEY));
    if (_notEquals) {
      String _literal = auth.getType().getLiteral();
      String _plus = (_literal + " authentication; ");
      String _plus_1 = (_plus + PineconeStatements.NO_ACCESS_CONTROL);
      throw Unsupported.of(_plus_1);
    }
    Literal _token = auth.getToken();
    if ((_token instanceof StringLit)) {
      throw Unsupported.of(("an API key written in the script; set the environment variable " + PineconePrinter.API_KEY_VARIABLE));
    }
    if (((((auth.getCertificatePath() != null) || auth.isAuditLog()) || auth.isFieldEncryption()) || (!auth.getRoles().isEmpty()))) {
      throw Unsupported.of(("certificates, audit logs, field encryption and roles; " + PineconeStatements.NO_ACCESS_CONTROL));
    }
    String _cmekKeyId = auth.getCmekKeyId();
    boolean _tripleNotEquals = (_cmekKeyId != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("customer-managed keys; CMEK is enabled per organization (Enterprise) and excludes full-text fields");
    }
  }

  /**
   * Cloud and region of new indexes, from the script's connection; the runtime supplies the defaults.
   */
  private Code.Entries indexDeployment() {
    Code.Entries _xblockexpression = null;
    {
      Connection _xifexpression = null;
      if ((this.script == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(this.script, Connection.class));
      }
      final Connection connection = _xifexpression;
      final Code.Entries result = PineconeSchema.kw();
      if (((connection != null) && (connection.getCloud() != null))) {
        result.put("cloud", Code.text(connection.getCloud()));
      }
      if (((connection != null) && (connection.getRegion() != null))) {
        result.put("region", Code.text(connection.getRegion()));
      }
      _xblockexpression = result;
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
    throw Unsupported.of((what + " outside a collection; Pinecone adds it to an index"));
  }

  private Code.Entries place(final VectorCollection collection) {
    return PineconeSchema.kw("name", Code.text(PineconeSchema.indexName(collection)));
  }

  private Code.Stmt require(final VectorCollection collection) {
    return PineconeStatements.run(Code.runtime("require_collection", this.place(collection)));
  }

  /**
   * Pinecone indexes metadata on first write, so an index for filtering needs no change; full-text needs the schema.
   */
  private List<Code.Stmt> existingPayloadIndex(final PayloadIndex index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EPayloadIndexKind _kind = index.getKind();
      boolean _equals = Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT);
      if (_equals) {
        throw Unsupported.of(("creating a full-text index on an existing collection; " + PineconeStatements.FIXED_SCHEMA));
      }
      if (((index.getTarget().getRawPath() != null) || (!index.getTarget().getSubFields().isEmpty()))) {
        throw Unsupported.of("payload indexes on nested paths; Pinecone metadata is flat");
      }
      final Field field = index.getTarget().getField();
      if ((!(field instanceof PayloadField))) {
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
      this.schema.payloadIndex(index, ((PayloadField) field), this.schema.isFullText(collection, field));
      Code.Stmt _require = this.require(collection);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
    }
    return _xblockexpression;
  }

  /**
   * Vector index settings on an existing collection: Pinecone tunes the index, so only a metric change is refused.
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
      VectorField _denseVector = this.schema.denseVector(collection);
      boolean _tripleNotEquals = (field != _denseVector);
      if (_tripleNotEquals) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("a vector index on sparse field " + _fieldName);
        String _plus_1 = (_plus + "; ");
        String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
        throw Unsupported.of(_plus_2);
      }
      if ((ModelUtil.written(index, "metric") && (!Objects.equals(index.getMetric(), this.schema.metric(field, ModelUtil.vectorIndex(collection, field)))))) {
        throw Unsupported.of(("a metric change; " + PineconeStatements.FIXED_SCHEMA));
      }
      this.schema.note(index, "family", "maxDegree", "buildCandidates", "searchCandidates", "centroids", "accelerator", "onDisk", "online");
      Quantization _quantization = index.getQuantization();
      boolean _tripleNotEquals_1 = (_quantization != null);
      if (_tripleNotEquals_1) {
        this.notes.add("quantization on VectorIndex");
      }
      Code.Stmt _require = this.require(collection);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> quantizationSettings(final Quantization concept) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorIndex index = EcoreUtil2.<VectorIndex>getContainerOfType(concept, VectorIndex.class);
      if ((index == null)) {
        throw Unsupported.of("quantization outside a vector index; Pinecone manages vector compression of its indexes");
      }
      this.notes.add("quantization on VectorIndex");
      _xblockexpression = this.indexSettings(index);
    }
    return _xblockexpression;
  }

  /**
   * Tenants and vecDSL namespaces are Pinecone namespaces of the collection's index.
   */
  private List<Code.Stmt> createNamespace(final VectorCollection collection, final String namespace, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.place(collection);
      args.put("namespace", Code.text(PineconeSchema.namespaceName(namespace)));
      final Code.Stmt create = PineconeStatements.run(Code.runtime("create_namespace", args));
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create));
      }
      Code.Call _runtime = Code.runtime("has_namespace", args);
      Code.When _when = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
    }
    return _xblockexpression;
  }

  /**
   * A hosted model is checked to exist (GET /models); integrated inference uses it by name.
   */
  private List<Code.Stmt> hostedModel(final String provider, final String model, final EObject concept) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String name = PineconeSchema.hostedName(provider, model);
      this.extensionArgs(concept);
      Code.Stmt _run = PineconeStatements.run(Code.runtime("require_model", PineconeSchema.kw("model", Code.text(name))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * A time-based backup schedule; a retention of n backups keeps each backup for n intervals (paid plans).
   */
  private List<Code.Stmt> backupSchedule(final BackupSchedule concept, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _location = concept.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("backup locations; Pinecone stores backups in its own storage");
      }
      final String frequency = concept.getInterval().toLowerCase().replaceFirst("^@", "");
      final Integer days = PineconeStatements.SCHEDULE_DAYS.get(frequency);
      if ((days == null)) {
        String _interval = concept.getInterval();
        String _plus = ("backup interval \'" + _interval);
        String _plus_1 = (_plus + "\'; Pinecone backup schedules run daily, weekly or monthly");
        throw Unsupported.of(_plus_1);
      }
      boolean _written = ModelUtil.written(concept, "retentionCount");
      boolean _not = (!_written);
      if (_not) {
        throw Unsupported.of("a backup schedule without retention; Pinecone requires a retention in days");
      }
      final Code.Entries probe = this.place(concept.getCollection());
      probe.put("schedule", Code.text(concept.getName()));
      final Code.Entries args = this.place(concept.getCollection());
      args.put("schedule", Code.text(concept.getName()));
      args.put("frequency", Code.text(frequency));
      int _retentionCount = concept.getRetentionCount();
      int _multiply = ((days).intValue() * _retentionCount);
      args.put("expire_after_days", Code.num(_multiply));
      final Code.Stmt create = PineconeStatements.run(Code.runtime("create_backup_schedule", args));
      if ((!ifNotExists)) {
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create));
      }
      Code.Call _runtime = Code.runtime("has_backup_schedule", probe);
      Code.When _when = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_when));
    }
    return _xblockexpression;
  }

  /**
   * A payload field added to an existing index: metadata needs no schema, so only a default needs writing (visible on return).
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _switchResult = null;
    boolean _matched = false;
    if (field instanceof VectorField) {
      _matched=true;
      String _fieldName = ModelUtil.fieldName(field);
      String _plus = ("adding vector field " + _fieldName);
      String _plus_1 = (_plus + "; ");
      String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
      throw Unsupported.of(_plus_2);
    }
    if (!_matched) {
      if (field instanceof PayloadField) {
        _matched=true;
        List<Code.Stmt> _xblockexpression = null;
        {
          if ((((PayloadField)field).isFullText() || (((PayloadField)field).getAnalyzer() != null))) {
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("adding full-text field " + _fieldName);
            String _plus_1 = (_plus + "; ");
            String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
            throw Unsupported.of(_plus_2);
          }
          this.schema.checkMetadataField(((PayloadField)field));
          Literal _defaultValue = ((PayloadField)field).getDefaultValue();
          boolean _tripleEquals = (_defaultValue == null);
          if (_tripleEquals) {
            Code.Stmt _require = this.require(collection);
            return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
          }
          final String name = PineconeSchema.fieldKey(field);
          Code.Stmt _run = PineconeStatements.run(Code.runtime("update_rows", PineconeSchema.kw("collection", this.schema.writeLayout(collection, this.defaultNamespace), 
            "filter", PineconeSchema.kw(name, PineconeSchema.kw("$exists", Code.flag(false))), "values", PineconeSchema.kw(name, Literals.of(((PayloadField)field).getDefaultValue())), "wait", Code.flag(true))));
          _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
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
        throw Unsupported.of("dropping the primary key; Pinecone document ids are the key text");
      }
      if (((field instanceof VectorField) || this.schema.isFullText(collection, field))) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("dropping " + _fieldName);
        String _plus_1 = (_plus + ", a field of the index schema; ");
        String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
        throw Unsupported.of(_plus_2);
      }
      final String name = PineconeSchema.fieldKey(field);
      Code.Stmt _run = PineconeStatements.run(Code.runtime("update_rows", PineconeSchema.kw("collection", this.schema.writeLayout(collection, this.defaultNamespace), 
        "filter", PineconeSchema.kw(name, PineconeSchema.kw("$exists", Code.flag(true))), "remove", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name))), "wait", Code.flag(true))));
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
        throw Unsupported.of("renaming the primary key; Pinecone document ids are the key text");
      }
      if (((field instanceof VectorField) || this.schema.isFullText(collection, field))) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("renaming " + _fieldName);
        String _plus_1 = (_plus + ", a field of the index schema; ");
        String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
        throw Unsupported.of(_plus_2);
      }
      Code.Stmt _run = PineconeStatements.run(Code.runtime("rename_field", PineconeSchema.kw("collection", this.schema.writeLayout(collection, this.defaultNamespace), "field", 
        Code.text(PineconeSchema.fieldKey(field)), "new_name", Code.text(PineconeSchema.checkKey(operation.getNewName())), "wait", Code.flag(true))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Collection properties: tags and deletion protection are index settings (configure_index).
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final SetPropertyOp operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.place(collection);
      List<Code.Stmt> _switchResult = null;
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case TAGS:
            List<Code.Stmt> _xblockexpression_1 = null;
            {
              args.put("tags", this.schema.tags(this.tagList(operation.getValue())));
              args.put("replace_tags", Code.flag(true));
              Code.Stmt _run = PineconeStatements.run(Code.runtime("configure_collection", args));
              _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            }
            _switchResult = _xblockexpression_1;
            break;
          case DELETION_PROTECTION:
            List<Code.Stmt> _xblockexpression_2 = null;
            {
              Literal _value = operation.getValue();
              boolean _not = (!(_value instanceof BoolLit));
              if (_not) {
                throw Unsupported.of("deletion-protection with a value other than true or false");
              }
              String _xifexpression = null;
              Literal _value_1 = operation.getValue();
              boolean _isValue = ((BoolLit) _value_1).isValue();
              if (_isValue) {
                _xifexpression = "enabled";
              } else {
                _xifexpression = "disabled";
              }
              args.put("deletion_protection", Code.text(_xifexpression));
              Code.Stmt _run = PineconeStatements.run(Code.runtime("configure_collection", args));
              _xblockexpression_2 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
            }
            _switchResult = _xblockexpression_2;
            break;
          case DYNAMIC:
            List<Code.Stmt> _xblockexpression_3 = null;
            {
              if (((!(operation.getValue() instanceof BoolLit)) || (!((BoolLit) operation.getValue()).isValue()))) {
                throw Unsupported.of("dynamic false; Pinecone stores every metadata key a document carries");
              }
              Code.Stmt _require = this.require(collection);
              _xblockexpression_3 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
            }
            _switchResult = _xblockexpression_3;
            break;
          case CONSISTENCY:
            List<Code.Stmt> _xblockexpression_4 = null;
            {
              final Literal level = operation.getValue();
              if (((!(level instanceof StringLit)) || (!Objects.equals(((StringLit) level).getValue().toLowerCase(), EConsistencyLevel.EVENTUAL.getLiteral())))) {
                throw Unsupported.of("consistency other than eventual; Pinecone reads are eventually consistent (use wait on writes)");
              }
              Code.Stmt _require = this.require(collection);
              _xblockexpression_4 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
            }
            _switchResult = _xblockexpression_4;
            break;
          case DESCRIPTION:
            throw Unsupported.of("collection descriptions; a Pinecone index has a name and tags but no description");
          case TTL:
            throw Unsupported.of("collection ttl; Pinecone documents do not expire");
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<String> tagList(final Literal value) {
    List<String> _xblockexpression = null;
    {
      if (((!(value instanceof ListLit)) || (!IterableExtensions.<Literal>forall(((ListLit) value).getItems(), ((Function1<Literal, Boolean>) (Literal item) -> {
        return Boolean.valueOf((item instanceof StringLit));
      }))))) {
        throw Unsupported.of("tags other than a list of strings");
      }
      final Function1<Literal, String> _function = (Literal item) -> {
        return ((StringLit) item).getValue();
      };
      _xblockexpression = IterableExtensions.<String>toList(ListExtensions.<Literal, String>map(((ListLit) value).getItems(), _function));
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
        String _plus_1 = (_plus + "; Pinecone alters indexes");
        throw Unsupported.of(_plus_1);
      }
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("renaming a collection; Pinecone indexes cannot be renamed");
      }
      final VectorCollection collection = ((VectorCollection) concept);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        List<Code.Stmt> _alter = this.alter(collection, operation);
        Iterables.<Code.Stmt>addAll(result, _alter);
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
        throw Unsupported.of(PineconeStatements.NO_ALIASES);
      }
    }
    if (!_matched) {
      if (operation instanceof AddFunctionOp) {
        _matched=true;
        String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
        String _plus = ("adding a " + _literal);
        String _plus_1 = (_plus + " function; ");
        String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
        throw Unsupported.of(_plus_2);
      }
    }
    if (!_matched) {
      if (operation instanceof DropFunctionOp) {
        _matched=true;
        String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
        String _plus = ("dropping a " + _literal);
        String _plus_1 = (_plus + " function; ");
        String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
        throw Unsupported.of(_plus_2);
      }
    }
    if (!_matched) {
      String _name = operation.eClass().getName();
      String _plus = (PineconeStatements.NOT_MAPPED + _name);
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
          final Code.Stmt drop = PineconeStatements.run(Code.runtime("drop_collection", this.place(((VectorCollection)concept))));
          Code.Stmt _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            Code.Call _runtime = Code.runtime("has_collection", this.place(((VectorCollection)concept)));
            _xifexpression = new Code.When(_runtime, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
          } else {
            _xifexpression = drop;
          }
          _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropNamespace(this.member(concept, "a tenant"), ((Tenant)concept).getName(), it.isIfExists());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropNamespace(this.member(concept, "a namespace"), ((Namespace)concept).getKey(), it.isIfExists());
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          _switchResult = this.dropField(this.member(concept, "a field"), ((Field)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final Code.Entries args = this.place(((BackupSchedule)concept).getCollection());
            args.put("schedule", Code.text(((BackupSchedule)concept).getName()));
            final Code.Stmt drop = PineconeStatements.run(Code.runtime("drop_backup_schedule", args));
            Code.Stmt _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              Code.Call _runtime = Code.runtime("has_backup_schedule", args);
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
        if (concept instanceof VectorField) {
          _matched=true;
          String _fieldName = ModelUtil.fieldName(((Field)concept));
          String _plus = ("dropping vector field " + _fieldName);
          String _plus_1 = (_plus + "; ");
          String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
          throw Unsupported.of(_plus_2);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a vector index; Pinecone indexes the dense vector field of every index and " + PineconeStatements.FIXED_SCHEMA));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of("dropping a payload index; Pinecone indexes every metadata key unless a namespace declares its filterable fields at creation");
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = ("dropping a " + _literal);
          String _plus_1 = (_plus + " function; ");
          String _plus_2 = (_plus_1 + PineconeStatements.FIXED_SCHEMA);
          throw Unsupported.of(_plus_2);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization; Pinecone manages vector compression of its indexes");
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Pinecone object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; Pinecone projects are deleted in the console or the Admin API");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + PineconeStatements.SERVERLESS));
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_DATABASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of(("dropping an analyzer filter; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of(("dropping a field limit; " + PineconeStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(("file resources; " + PineconeStatements.NO_ANALYZERS));
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("dropping an embedding model; " + PineconeStatements.MANAGED_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + PineconeStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("dropping a reranker; " + PineconeStatements.MANAGED_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + PineconeStatements.SERVERLESS));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + PineconeStatements.SERVERLESS));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication; " + PineconeStatements.NO_ACCESS_CONTROL));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings; " + PineconeStatements.NO_ACCESS_CONTROL));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(PineconeStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(("cluster limits; " + PineconeStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(("collection limits; " + PineconeStatements.NO_LIMITS));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((PineconeStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropNamespace(final VectorCollection collection, final String namespace, final boolean ifExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.place(collection);
      args.put("namespace", Code.text(PineconeSchema.namespaceName(namespace)));
      final Code.Stmt drop = PineconeStatements.run(Code.runtime("drop_namespace", args));
      Code.Stmt _xifexpression = null;
      if (ifExists) {
        Code.Call _runtime = Code.runtime("has_namespace", args);
        _xifexpression = new Code.When(_runtime, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
      } else {
        _xifexpression = drop;
      }
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    throw Unsupported.of(PineconeStatements.NO_ALIASES);
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Pinecone documents do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = PineconeSchema.kw("collection", this.schema.writeLayout(it.getCollection(), this.namespaceOf(it)), "rows", this.rows(it.getSource()));
      final boolean upsert = (ModelUtil.written(it, "onConflict") && Collections.<EConflictPolicy>unmodifiableList(CollectionLiterals.<EConflictPolicy>newArrayList(EConflictPolicy.UPDATE, EConflictPolicy.REPLACE)).contains(it.getOnConflict()));
      if ((ModelUtil.written(it, "onConflict") && (!upsert))) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.writeOptions(it, args);
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
        throw Unsupported.of("per-statement ttl; Pinecone documents do not expire");
      }
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = PineconeSchema.kw("collection", this.schema.writeLayout(it.getCollection(), this.namespaceOf(it)), "rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      this.writeOptions(it, args);
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
        throw Unsupported.of("if-version; Pinecone documents carry no version");
      }
      this.schema.note(it, "recordCountHint");
      final VectorCollection updated = it.getCollection();
      final PineconeFilter where = new PineconeFilter(updated, this.schema).where(it.getFilter());
      final Code.Entries args = PineconeSchema.kw("collection", this.schema.writeLayout(updated, this.namespaceOf(it)));
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
      this.arrayItems(it, args);
      this.writeOptions(it, args);
      Code.Call _runtime = Code.runtime("update_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * array-append and array-remove take their items from the statement's set record; the runtime reads and writes back.
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
      final Function1<Field, String> _function_1 = (Field field_1) -> {
        return PineconeSchema.fieldKey(field_1);
      };
      args.put("append", Code.texts(ListExtensions.<Field, String>map(update.getAppendFields(), _function_1)));
    }
    boolean _isEmpty_1 = update.getArrayRemoveFields().isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      final Function1<Field, String> _function_2 = (Field field_1) -> {
        return PineconeSchema.fieldKey(field_1);
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
      if (((field instanceof VectorField) && (field == this.schema.denseVector(collection)))) {
        throw Unsupported.of("removing the dense vector; every Pinecone document carries it");
      }
      _xblockexpression = PineconeSchema.fieldKey(field);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorCollection _collection = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection, this.schema).where(it.getFilter());
      final Code.Entries args = PineconeSchema.kw("collection", this.schema.writeLayout(it.getCollection(), this.namespaceOf(it)));
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
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        args.put("limit", Code.num(it.getDeleteLimit()));
      }
      this.writeOptions(it, args);
      Code.Call _runtime = Code.runtime("delete_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Remote files (s3, gs, Azure https) load through Pinecone's bulk import of JSON Lines documents, available on paid
   * plans; local files (file:// or a path) are read by the runtime and written as an insert or upsert.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Pinecone imports a prefix of object storage once per import");
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; document indexes import JSON Lines files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism");
      String _xifexpression = null;
      boolean _contains = it.getLocation().contains("://");
      if (_contains) {
        _xifexpression = it.getLocation().substring(0, it.getLocation().indexOf("://"));
      } else {
        _xifexpression = "file";
      }
      final String scheme = _xifexpression;
      final String slice = this.namespaceOf(it);
      this.extensionArgs(it);
      boolean _equals = Objects.equals(scheme, "file");
      if (_equals) {
        final Code.Entries args = PineconeSchema.kw("collection", this.schema.writeLayout(it.getCollection(), slice), "location", Code.text(it.getLocation()));
        boolean _written = ModelUtil.written(it, "onConflict");
        if (_written) {
          args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
        }
        boolean _written_1 = ModelUtil.written(it, "batchSize");
        if (_written_1) {
          args.put("batch_size", Code.num(it.getBatchSize()));
        }
        boolean _isAsync = it.isAsync();
        boolean _not = (!_isAsync);
        if (_not) {
          args.put("wait", Code.flag(true));
        }
        Code.Call _runtime = Code.runtime("import_file", args);
        Code.Return _return = new Code.Return(_runtime);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      boolean _contains_1 = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("s3", "gs", "https")).contains(scheme);
      boolean _not_1 = (!_contains_1);
      if (_not_1) {
        throw Unsupported.of((("import from " + scheme) + "; Pinecone imports from s3, gs and Azure Blob Storage (https)"));
      }
      if ((ModelUtil.written(it, "onConflict") && (!Objects.equals(it.getOnConflict(), EConflictPolicy.ERROR)))) {
        String _literal_1 = it.getOnConflict().getLiteral();
        String _plus_2 = ("on-conflict " + _literal_1);
        String _plus_3 = (_plus_2 + " for imports from object storage; Pinecone imports only into new namespaces");
        throw Unsupported.of(_plus_3);
      }
      boolean _written_2 = ModelUtil.written(it, "batchSize");
      if (_written_2) {
        this.notes.add("batchSize on BulkImport, which Pinecone sizes itself");
      }
      final Code.Entries args_1 = PineconeSchema.kw("collection", this.schema.layout(it.getCollection(), slice), "uri", Code.text(it.getLocation()));
      boolean _isAsync_1 = it.isAsync();
      if (_isAsync_1) {
        args_1.put("background", Code.flag(true));
      }
      Code.Call _runtime_1 = Code.runtime("start_import", args_1);
      Code.Return _return_1 = new Code.Return(_runtime_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of("multi-statement transactions; Pinecone applies each write request on its own");
  }

  /**
   * Pinecone acknowledges every write once; 'wait' makes the runtime wait until searches see it.
   */
  private void writeOptions(final EObject statement, final Code.Entries args) {
    this.schema.note(statement, "writeConsistency");
    Object _feature = this.feature(statement, "waitForPersistence");
    boolean _equals = Objects.equals(Boolean.TRUE, _feature);
    if (_equals) {
      args.put("wait", Code.flag(true));
    }
    this.extensionArgs(statement);
  }

  /**
   * Serverless indexes serve every namespace on demand; loading checks that the index exists.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
      }
      this.extensionArgs(it);
      Code.Stmt _require = this.require(it.getCollection());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of("releasing a collection; Pinecone serverless indexes have no memory residency to release");
  }

  /**
   * An acknowledged write is committed to the index's log (its request log position); flush checks the index.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.extensionArgs(it);
      Code.Stmt _require = this.require(it.getCollection());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
    }
    return _xblockexpression;
  }

  /**
   * Pinecone compacts serverless index storage itself; compact checks the index.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.extensionArgs(it);
      this.schema.note(it, "targetSegments", "async");
      Code.Stmt _require = this.require(it.getCollection());
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_require));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.place(it.getCollection());
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
    throw Unsupported.of("embedding migration; the model of a Pinecone embed field cannot be changed after creation");
  }

  /**
   * A backup of the whole index (paid plans); the snapshot name is the backup name.
   */
  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope(), ESnapshotScope.COLLECTION)))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("snapshot scope " + _literal);
        String _plus_1 = (_plus + "; a Pinecone backup copies one index");
        throw Unsupported.of(_plus_1);
      }
      boolean _isIncremental = it.isIncremental();
      if (_isIncremental) {
        throw Unsupported.of("incremental snapshots; every Pinecone backup is a full copy of the index");
      }
      Tenant _tenant = it.getTenant();
      boolean _tripleNotEquals = (_tenant != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("snapshots of a tenant; a Pinecone backup copies every namespace of the index");
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals_1 = (_location != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("snapshot locations; Pinecone stores backups in its own storage");
      }
      this.extensionArgs(it);
      final Code.Entries args = this.place(it.getCollection());
      String _elvis = null;
      String _snapshotName = it.getSnapshotName();
      if (_snapshotName != null) {
        _elvis = _snapshotName;
      } else {
        String _name = it.getName();
        _elvis = _name;
      }
      args.put("backup", Code.text(PineconeSchema.resourceName(_elvis, "snapshot")));
      String _description = it.getDescription();
      boolean _tripleNotEquals_2 = (_description != null);
      if (_tripleNotEquals_2) {
        args.put("description", Code.text(it.getDescription()));
      }
      Code.Call _runtime = Code.runtime("create_backup", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Restore replaces the collection: the index is deleted and created again from the backup (paid plans).
   */
  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if ((ModelUtil.written(it, "scope") && (!Objects.equals(it.getScope(), ESnapshotScope.COLLECTION)))) {
        String _literal = it.getScope().getLiteral();
        String _plus = ("restore scope " + _literal);
        String _plus_1 = (_plus + "; a Pinecone backup restores one index");
        throw Unsupported.of(_plus_1);
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("snapshot locations; Pinecone stores backups in its own storage");
      }
      this.extensionArgs(it);
      final Code.Entries args = this.place(it.getCollection());
      args.put("backup", Code.text(PineconeSchema.resourceName(it.getSnapshotName(), "snapshot")));
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        args.put("background", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("restore_backup", args);
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
        throw Unsupported.of("snapshot locations; Pinecone stores backups in its own storage");
      }
      Code.Call _runtime = Code.runtime("drop_backup", PineconeSchema.kw("backup", Code.text(PineconeSchema.resourceName(it.getSnapshotName(), "snapshot"))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A new index with the source's schema, filled with a copy of every namespace (Starter projects have no backups).
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a deployment; Pinecone copies indexes");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; Pinecone has no copy-on-write indexes");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        String _get = PineconeStatements.REASONS.get("asOf");
        String _plus = ("the \'as-of\' clause; " + _get);
        throw Unsupported.of(_plus);
      }
      this.extensionArgs(it);
      final Code.Entries args = PineconeSchema.kw("collection", this.schema.layout(it.getSourceCollection(), this.defaultNamespace), "target", 
        Code.text(PineconeSchema.resourceName(it.getTargetName(), "clone target")));
      final Code.Entries deployment = this.indexDeployment();
      boolean _isEmpty = deployment.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("deployment", deployment);
      }
      Code.Call _runtime = Code.runtime("clone_collection", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RebuildIndex it) {
    throw Unsupported.of("rebuilding an index; Pinecone maintains its indexes with every write and has no rebuild call");
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of("collections derived from external sources; Pinecone stores the documents of every index itself");
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _xifexpression = null;
      String _namespace = it.getNamespace();
      boolean _tripleEquals = (_namespace == null);
      if (_tripleEquals) {
        _xifexpression = this.defaultNamespace;
      } else {
        _xifexpression = PineconeSchema.namespaceName(it.getNamespace());
      }
      final String target = _xifexpression;
      Code.Call _runtime = Code.runtime("delete_rows", PineconeSchema.kw("collection", this.schema.writeLayout(it.getCollection(), target), "delete_all", Code.flag(true)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = PineconeSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      final Concept owner = it.getScope();
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner != null)) {
              String _name = owner.eClass().getName();
              String _plus = ("listing collections in a " + _name);
              String _plus_1 = (_plus + "; Pinecone lists the indexes of a project");
              throw Unsupported.of(_plus_1);
            }
            break;
          case TENANTS:
          case NAMESPACES:
            if ((!(owner instanceof VectorCollection))) {
              String _literal = it.getKind().getLiteral();
              String _plus_2 = ("listing " + _literal);
              String _plus_3 = (_plus_2 + " without a collection; Pinecone lists the namespaces of an index");
              throw Unsupported.of(_plus_3);
            }
            args.put("kind", Code.text("namespaces"));
            args.put("scope", this.place(((VectorCollection) owner)));
            break;
          case SNAPSHOTS:
            if ((owner instanceof VectorCollection)) {
              args.put("scope", this.place(((VectorCollection) owner)));
            } else {
              if ((owner != null)) {
                String _name_1 = owner.eClass().getName();
                String _plus_4 = ("listing snapshots in a " + _name_1);
                String _plus_5 = (_plus_4 + "; Pinecone lists the backups of an index or a project");
                throw Unsupported.of(_plus_5);
              }
            }
            break;
          case DATABASES:
            throw Unsupported.of(PineconeStatements.NO_DATABASES);
          case INDEXES:
            throw Unsupported.of("listing indexes; a Pinecone index is the collection itself (list collections)");
          case PARTITIONS:
            throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
          case ALIASES:
            throw Unsupported.of(PineconeStatements.NO_ALIASES);
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
   * Serverless indexes index every write; an index build is complete when the index reports ready.
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
        String _plus = (PineconeStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      final VectorCollection collection = _switchResult;
      if ((collection == null)) {
        throw Unsupported.of("a payload index on a raw path outside a collection");
      }
      if ((ModelUtil.written(it, "state") && (!Objects.equals(it.getState(), EIndexBuildState.READY)))) {
        String _literal = it.getState().getLiteral();
        String _plus_1 = ("await-index until " + _literal);
        String _plus_2 = (_plus_1 + "; Pinecone reports whether an index is ready");
        throw Unsupported.of(_plus_2);
      }
      final Code.Entries args = this.place(collection);
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
      this.reject(it, "elementScope", "asOf", "cursor", "withTotal", "explain", "mmr", "highlighted", 
        "sortClauses", "aggregations", "prefetches", "exact", "groupBy", "boost");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Expr vector = this.queryVector(it.getInput(), it.getCollection(), field, "query_vector", prelude, this.namespaceOf(it), it.getQueryModel());
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      if ((ModelUtil.written(it, "metric") && (!Objects.equals(it.getMetric(), metric)))) {
        throw Unsupported.of(("a query metric other than the metric of the vector index; " + PineconeStatements.FIXED_SCHEMA));
      }
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(PineconeSchema.searchKey(it.getCollection(), field)), "vector", vector, 
        "limit", Code.num(it.getTopK()), "metric", Code.text(this.schema.metricName(metric)));
      this.embedClause(it.getCollection(), field, args);
      ScoreBounds _bounds = it.getBounds();
      this.scoreBounds(_bounds, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new PineconeFilter(_collection, this.schema).where(it.getFilter()));
      this.searchParams(it.getParams());
      this.extensionArgs(it);
      Reranker _reranker = it.getReranker();
      boolean _tripleNotEquals = (_reranker != null);
      if (_tripleNotEquals) {
        EQueryInputMode _kind = it.getInput().getKind();
        boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.TEXT));
        if (_notEquals) {
          String _literal = it.getInput().getKind().getLiteral();
          String _plus = ("the \'rerank\' clause on a vector search with " + _literal);
          String _plus_1 = (_plus + " input; ");
          String _plus_2 = (_plus_1 + PineconeStatements.RERANK_TEXT);
          throw Unsupported.of(_plus_2);
        }
        EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(it.getCollection(), field);
        final boolean embedded = (_embeddingModel != null);
        Reranker _reranker_1 = it.getReranker();
        VectorCollection _collection_1 = it.getCollection();
        Code.Expr _of = Literals.of(it.getInput().getValue());
        String _xifexpression = null;
        if (embedded) {
          _xifexpression = PineconeSchema.searchKey(it.getCollection(), field);
        } else {
          _xifexpression = null;
        }
        this.rerank(_reranker_1, _collection_1, _of, _xifexpression, args);
      }
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
   * The scoring of a field Pinecone embeds: the runtime sends the query text with embed or sparse_embed.
   */
  private void embedClause(final VectorCollection collection, final VectorField field, final Code.Entries args) {
    final String scoring = PineconeSchema.embedScoring(collection, field);
    if ((scoring != null)) {
      args.put("embed", Code.text(scoring));
    }
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "highlighted", "sortClauses", 
        "aggregations", "groupBy", "boost", "minShouldMatch");
      EQueryInputMode _kind = it.getInput().getKind();
      boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.TEXT));
      if (_notEquals) {
        String _literal = it.getInput().getKind().getLiteral();
        String _plus = ("keyword search with " + _literal);
        String _plus_1 = (_plus + " input; Pinecone text scoring takes a query text");
        throw Unsupported.of(_plus_1);
      }
      final Code.Expr query = Literals.of(it.getInput().getValue());
      final List<String> fields = this.keywordFields(it.getCollection(), it.getSearchFields());
      ETextQueryType _xifexpression = null;
      boolean _written = ModelUtil.written(it, "textQueryType");
      if (_written) {
        _xifexpression = it.getTextQueryType();
      } else {
        _xifexpression = ETextQueryType.MATCH;
      }
      final ETextQueryType type = _xifexpression;
      final boolean required = (ModelUtil.written(it, "matchOperator") && Objects.equals(it.getMatchOperator(), EMatchOperator.AND));
      VectorCollection _collection = it.getCollection();
      EList<SearchFieldRef> _searchFields = it.getSearchFields();
      String _xifexpression_1 = null;
      boolean _written_1 = ModelUtil.written(it, "fuzziness");
      if (_written_1) {
        _xifexpression_1 = it.getFuzziness();
      } else {
        _xifexpression_1 = null;
      }
      Integer _xifexpression_2 = null;
      boolean _written_2 = ModelUtil.written(it, "slop");
      if (_written_2) {
        _xifexpression_2 = Integer.valueOf(it.getSlop());
      } else {
        _xifexpression_2 = null;
      }
      final Code.Entries rewritten = this.rewrite(_collection, type, _searchFields, required, _xifexpression_1, _xifexpression_2);
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "query", query, "fields", Code.texts(fields), "limit", Code.num(it.getTopK()));
      if ((rewritten != null)) {
        args.put("rewrite", rewritten);
      } else {
        boolean _equals = Objects.equals(type, ETextQueryType.QUERY_STRING);
        if (_equals) {
          args.put("query_string", Code.flag(true));
        }
      }
      ScoreBounds _bounds = it.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        this.scoreBounds(it.getBounds(), false, args);
      }
      VectorCollection _collection_1 = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection_1, this.schema).where(it.getFilter());
      this.common(it, it.getCollection(), args, it.getOutput(), where);
      if (((rewritten == null) || Objects.equals(type, ETextQueryType.QUERY_STRING))) {
        this.allTerms(required, fields, query, args);
      }
      this.searchParams(it.getParams());
      this.extensionArgs(it);
      this.rerank(it.getReranker(), it.getCollection(), query, IterableExtensions.<String>head(fields), args);
      Code.Call _runtime = Code.runtime("keyword_search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A text query in Pinecone's query_string syntax, which the runtime builds from the query's terms:
   * phrase, phrase prefix, fuzzy terms and regular expressions, fuzziness, slop and field boosts. Null for a match or
   * query_string query that needs none of them, which keeps its text or query_string clause.
   */
  private Code.Entries rewrite(final VectorCollection collection, final ETextQueryType type, final List<SearchFieldRef> refs, final boolean required, final String distance, final Integer proximity) {
    Code.Entries _xblockexpression = null;
    {
      if (type != null) {
        switch (type) {
          case PREFIX:
            throw Unsupported.of(PineconeStatements.SINGLE_TERM_PREFIX);
          case TERM:
            throw Unsupported.of(PineconeStatements.NO_TERM_QUERY);
          case WILDCARD:
            throw Unsupported.of(PineconeStatements.NO_WILDCARD);
          default:
            break;
        }
      } else {
      }
      if ((((distance != null) && (!Objects.equals(type, ETextQueryType.MATCH))) && (!Objects.equals(type, ETextQueryType.FUZZY)))) {
        String _literal = type.getLiteral();
        String _plus = ("the \'fuzziness\' clause with text-query " + _literal);
        String _plus_1 = (_plus + "; Pinecone applies fuzzy distances to bare terms only");
        throw Unsupported.of(_plus_1);
      }
      if (((proximity != null) && (!Objects.equals(type, ETextQueryType.PHRASE)))) {
        String _literal_1 = type.getLiteral();
        String _plus_2 = ("the \'slop\' clause with text-query " + _literal_1);
        String _plus_3 = (_plus_2 + "; Pinecone applies slop to quoted phrases only");
        throw Unsupported.of(_plus_3);
      }
      if ((required && Objects.equals(type, ETextQueryType.REGEXP))) {
        throw Unsupported.of("the \'operator\' clause with text-query regexp; a regular expression has no terms to combine");
      }
      final Code.Entries boosts = this.fieldBoosts(collection, refs);
      final boolean plain = (Objects.equals(type, ETextQueryType.MATCH) || (Objects.equals(type, ETextQueryType.QUERY_STRING) && refs.isEmpty()));
      if (((plain && (distance == null)) && boosts.entries().isEmpty())) {
        return null;
      }
      final Code.Entries result = PineconeSchema.kw("type", Code.text(type.getLiteral()));
      boolean _isEmpty = boosts.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        result.put("boosts", boosts);
      }
      if (((distance != null) || Objects.equals(type, ETextQueryType.FUZZY))) {
        result.put("fuzzy", Code.text(PineconeStatements.fuzzySuffix(distance)));
      }
      if ((proximity != null)) {
        result.put("slop", Code.num(proximity.doubleValue()));
      }
      if ((required && (Objects.equals(type, ETextQueryType.MATCH) || Objects.equals(type, ETextQueryType.FUZZY)))) {
        result.put("required", Code.flag(true));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Pinecone's fuzzy suffix: '~' for the automatic distance by term length, '~N' for 0, 1 or 2 edits.
   */
  private static String fuzzySuffix(final String distance) {
    if (((distance == null) || distance.equalsIgnoreCase("AUTO"))) {
      return "~";
    }
    boolean _matches = distance.matches("[012]");
    if (_matches) {
      return ("~" + distance);
    }
    throw Unsupported.of((("fuzziness \'" + distance) + "\'; Pinecone takes the automatic distance (AUTO) or 0, 1 or 2 edits"));
  }

  /**
   * Field boosts as Pinecone boost literals by field key: 0 or greater, digits on both sides of a decimal point, no exponent.
   */
  private Code.Entries fieldBoosts(final VectorCollection collection, final List<SearchFieldRef> refs) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      final Function1<SearchFieldRef, Boolean> _function = (SearchFieldRef entry) -> {
        return Boolean.valueOf(ModelUtil.written(entry, "boost"));
      };
      Iterable<SearchFieldRef> _filter = IterableExtensions.<SearchFieldRef>filter(refs, _function);
      for (final SearchFieldRef ref : _filter) {
        {
          double _boost = ref.getBoost();
          boolean _lessThan = (_boost < 0);
          if (_lessThan) {
            throw Unsupported.of("a negative field boost; Pinecone boosts are 0 or greater");
          }
          final String literal = BigDecimal.valueOf(ref.getBoost()).stripTrailingZeros().toPlainString();
          List<Field> _searchedFields = this.searchedFields(collection, ref);
          for (final Field field : _searchedFields) {
            result.put(PineconeSchema.fieldKey(field), Code.text(literal));
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Operator 'and': every term must occur in one of the searched fields, a $match_all filter beside the scoring.
   */
  private void allTerms(final boolean required, final List<String> fields, final Code.Expr query, final Code.Entries args) {
    if ((!required)) {
      return;
    }
    final Function1<String, Code.Expr> _function = (String name) -> {
      Code.Entries _kw = PineconeSchema.kw(name, PineconeSchema.kw("$match_all", query));
      return ((Code.Expr) _kw);
    };
    final List<Code.Expr> matches = ListExtensions.<String, Code.Expr>map(fields, _function);
    Code.Expr _xifexpression = null;
    int _size = matches.size();
    boolean _equals = (_size == 1);
    if (_equals) {
      _xifexpression = IterableExtensions.<Code.Expr>head(matches);
    } else {
      _xifexpression = PineconeSchema.kw("$or", Code.items(matches));
    }
    final Code.Expr match = _xifexpression;
    Code.Expr _xifexpression_1 = null;
    boolean _has = args.has("filter");
    if (_has) {
      Code.Expr _get = args.get("filter");
      _xifexpression_1 = PineconeSchema.kw("$and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_get, match))));
    } else {
      _xifexpression_1 = match;
    }
    args.put("filter", _xifexpression_1);
  }

  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "highlighted", "sortClauses", 
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
            _switchResult = this.vectorLeg(it, ((VectorLeg)leg), ("query_vector_" + Integer.valueOf(index)), prelude);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              _switchResult = this.keywordLeg(it, ((KeywordLeg)leg));
            }
          }
          final Code.Entries entry = _switchResult;
          VectorCollection _collection = it.getCollection();
          Object _feature = this.feature(leg, "filter");
          final PineconeFilter where = new PineconeFilter(_collection, this.schema).where(it.getFilter()).where(((FilterBlock) _feature));
          boolean _isEmpty = where.isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            entry.put("filter", where.code());
          }
          boolean _written_1 = ModelUtil.written(leg, "legLimit");
          if (_written_1) {
            Object _feature_1 = this.feature(leg, "legLimit");
            entry.put("limit", Code.num(((Integer) _feature_1).doubleValue()));
          }
          if ((weights != null)) {
            entry.put("weight", Code.num((weights.get(index)).doubleValue()));
          }
          mapped.add(entry);
          index = (index + 1);
        }
      }
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "legs", Code.items(mapped), "limit", Code.num(it.getTopK()), "fusion", Code.text(fusionKind.getLiteral()));
      boolean _equals = Objects.equals(fusionKind, EFusionType.RRF);
      if (_equals) {
        int _xifexpression_1 = (int) 0;
        boolean _written_1 = ModelUtil.written(it, "fusionConstant");
        if (_written_1) {
          _xifexpression_1 = it.getFusionConstant();
        } else {
          _xifexpression_1 = PineconeStatements.DEFAULT_FUSION_K;
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
      this.searchParams(it.getParams());
      this.extensionArgs(it);
      Reranker _reranker = it.getReranker();
      boolean _tripleNotEquals_1 = (_reranker != null);
      if (_tripleNotEquals_1) {
        this.rerankFused(it, mapped, args);
      }
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
   * A rerank of fused hits: the query text and rank field of the shared text input or of the first leg with a query text.
   */
  private void rerankFused(final HybridSearch search, final List<Code.Expr> mapped, final Code.Entries args) {
    Code.Expr _xifexpression = null;
    if (((search.getInput() != null) && Objects.equals(search.getInput().getKind(), EQueryInputMode.TEXT))) {
      _xifexpression = Literals.of(search.getInput().getValue());
    }
    final Code.Expr shared = _xifexpression;
    Code.Expr query = shared;
    String field = null;
    Iterable<Code.Entries> _filter = Iterables.<Code.Entries>filter(mapped, Code.Entries.class);
    for (final Code.Entries entry : _filter) {
      {
        Code.Expr _xifexpression_1 = null;
        boolean _has = entry.has("query");
        if (_has) {
          _xifexpression_1 = entry.get("query");
        } else {
          Code.Expr _xifexpression_2 = null;
          boolean _has_1 = entry.has("embed");
          if (_has_1) {
            _xifexpression_2 = entry.get("vector");
          }
          _xifexpression_1 = _xifexpression_2;
        }
        final Code.Expr leg = _xifexpression_1;
        if (((leg != null) && (field == null))) {
          Code.Expr _elvis = null;
          if (query != null) {
            _elvis = query;
          } else {
            _elvis = leg;
          }
          query = _elvis;
          String _xifexpression_3 = null;
          boolean _has_2 = entry.has("query");
          if (_has_2) {
            Code.Expr _get = entry.get("fields");
            Code.Expr _head = IterableExtensions.<Code.Expr>head(((Code.Items) _get).items());
            _xifexpression_3 = ((Code.Text) _head).value();
          } else {
            Code.Expr _get_1 = entry.get("field");
            _xifexpression_3 = ((Code.Text) _get_1).value();
          }
          field = _xifexpression_3;
        }
      }
    }
    this.rerank(search.getReranker(), search.getCollection(), query, field, args);
  }

  /**
   * Leg weights for weighted and relative-score fusion; rrf is unweighted by definition.
   */
  private List<Double> legWeights(final HybridSearch search, final EFusionType fusionKind) {
    List<Double> _xblockexpression = null;
    {
      final Function1<HybridLeg, Boolean> _function = (HybridLeg leg) -> {
        return Boolean.valueOf(ModelUtil.written(leg, "weight"));
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
        double _doubleValue = BigDecimal.ONE.subtract(BigDecimal.valueOf(search.getAlpha())).doubleValue();
        return Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(_alpha), Double.valueOf(_doubleValue)));
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
      final Function1<HybridLeg, Double> _function_2 = (HybridLeg leg) -> {
        Object _feature = this.feature(leg, "weight");
        return ((Double) _feature);
      };
      _xblockexpression = ListExtensions.<HybridLeg, Double>map(search.getLegs(), _function_2);
    }
    return _xblockexpression;
  }

  private Code.Entries vectorLeg(final HybridSearch search, final VectorLeg leg, final String variable, final List<Code.Stmt> prelude) {
    Code.Entries _xblockexpression = null;
    {
      this.reject(leg, "exact", "params");
      final VectorField field = this.searchField(search.getCollection(), leg.getVectorField());
      final EMetric metric = this.indexMetric(search.getCollection(), field);
      if ((ModelUtil.written(leg, "metric") && (!Objects.equals(leg.getMetric(), metric)))) {
        throw Unsupported.of(("a query metric other than the metric of the vector index; " + PineconeStatements.FIXED_SCHEMA));
      }
      QueryInput _elvis = null;
      QueryInput _input = leg.getInput();
      if (_input != null) {
        _elvis = _input;
      } else {
        QueryInput _input_1 = search.getInput();
        _elvis = _input_1;
      }
      final Code.Expr vector = this.queryVector(_elvis, search.getCollection(), field, variable, prelude, this.namespaceOf(search), null);
      final Code.Entries entry = PineconeSchema.kw("kind", Code.text("vector"), "field", Code.text(PineconeSchema.searchKey(search.getCollection(), field)), "vector", vector, 
        "metric", Code.text(this.schema.metricName(metric)));
      this.embedClause(search.getCollection(), field, entry);
      ScoreBounds _bounds = leg.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        this.scoreBounds(leg.getBounds(), false, entry);
      }
      this.extensionArgs(leg);
      _xblockexpression = entry;
    }
    return _xblockexpression;
  }

  private Code.Entries keywordLeg(final HybridSearch search, final KeywordLeg leg) {
    Code.Entries _xblockexpression = null;
    {
      this.reject(leg, "minShouldMatch");
      ETextQueryType _xifexpression = null;
      boolean _written = ModelUtil.written(leg, "textQueryType");
      if (_written) {
        _xifexpression = leg.getTextQueryType();
      } else {
        _xifexpression = ETextQueryType.MATCH;
      }
      final ETextQueryType type = _xifexpression;
      final boolean required = (ModelUtil.written(leg, "matchOperator") && Objects.equals(leg.getMatchOperator(), EMatchOperator.AND));
      final Code.Entries rewritten = this.rewrite(search.getCollection(), type, leg.getSearchFields(), required, null, null);
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
        throw Unsupported.of("a keyword leg without text input; Pinecone text scoring takes a query text");
      }
      final Code.Expr query = Literals.of(keywordInput.getValue());
      final List<String> fields = this.keywordFields(search.getCollection(), leg.getSearchFields());
      final Code.Entries entry = PineconeSchema.kw("kind", Code.text("keyword"), "query", query, "fields", Code.texts(fields));
      if ((rewritten != null)) {
        entry.put("rewrite", rewritten);
      } else {
        boolean _equals = Objects.equals(type, ETextQueryType.QUERY_STRING);
        if (_equals) {
          entry.put("query_string", Code.flag(true));
        }
      }
      if (((rewritten == null) || Objects.equals(type, ETextQueryType.QUERY_STRING))) {
        this.allTerms(required, fields, query, entry);
      }
      this.extensionArgs(leg);
      _xblockexpression = entry;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RecommendSearch it) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "prefetches", "boost", "groupBy");
      boolean _isEmpty = it.getPositives().isEmpty();
      if (_isEmpty) {
        throw Unsupported.of("recommend without positive examples");
      }
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final VectorCollection source = _elvis;
      final VectorField field = this.storedField(it.getCollection(), this.searchField(it.getCollection(), it.getVectorField()), "recommend");
      final Code.Text name = Code.text(PineconeSchema.fieldKey(field));
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return this.fieldValue(Literals.of(it_1));
      };
      final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
        return this.fieldValue(Literals.of(it_1));
      };
      final Code.Entries examples = PineconeSchema.kw("collection", this.layout(source, it), "field", name, "positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)), 
        "negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      Code.Entries _layout = this.layout(it.getCollection(), it);
      Code.Ref _ref = new Code.Ref("query_vector");
      final Code.Entries args = PineconeSchema.kw("collection", _layout, "field", name, "vector", _ref, 
        "limit", Code.num(it.getTopK()), "metric", Code.text(this.schema.metricName(metric)));
      ScoreBounds _bounds = it.getBounds();
      this.scoreBounds(_bounds, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      VectorCollection _collection_1 = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection_1, this.schema).where(it.getFilter());
      this.common(it, it.getCollection(), args, it.getOutput(), where);
      VectorCollection _collection_2 = it.getCollection();
      boolean _tripleEquals = (source == _collection_2);
      if (_tripleEquals) {
        EList<Literal> _positives = it.getPositives();
        EList<Literal> _negatives = it.getNegatives();
        final Function1<Literal, Boolean> _function_2 = (Literal example) -> {
          return Boolean.valueOf((!((example instanceof ListLit) || (example instanceof SparseLit))));
        };
        this.exclude(IterableExtensions.<Literal>toList(IterableExtensions.<Literal>filter(Iterables.<Literal>concat(_positives, _negatives), _function_2)), it.getCollection(), args);
      }
      this.searchParams(it.getParams());
      this.extensionArgs(it);
      Code.Call _runtime = Code.runtime("recommend_vector", examples);
      Code.Let _let = new Code.Let("query_vector", _runtime);
      Code.Call _runtime_1 = Code.runtime("vector_search", args);
      Code.Return _return = new Code.Return(_runtime_1);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _let), _return);
    }
    return _xblockexpression;
  }

  /**
   * Leaves the examples out of the hits with a $nin clause on the stored primary key.
   */
  private void exclude(final List<Literal> keys, final VectorCollection collection, final Code.Entries args) {
    boolean _isEmpty = keys.isEmpty();
    if (_isEmpty) {
      return;
    }
    final Function1<Literal, Code.Expr> _function = (Literal it) -> {
      return Literals.of(it);
    };
    final Code.Entries clause = PineconeSchema.kw(PineconeSchema.fieldKey(collection.getPrimaryKey()), PineconeSchema.kw("$nin", Code.items(ListExtensions.<Literal, Code.Expr>map(keys, _function))));
    Code.Entries _xifexpression = null;
    boolean _has = args.has("filter");
    if (_has) {
      Code.Expr _get = args.get("filter");
      _xifexpression = PineconeSchema.kw("$and", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_get, clause))));
    } else {
      _xifexpression = clause;
    }
    args.put("filter", _xifexpression);
  }

  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      this.extensionArgs(it);
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
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it));
      VectorCollection _collection = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection, this.schema).where(it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.extensionArgs(it);
      Code.Call _runtime = Code.runtime("count", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "cursor", "asOf", "sliceIndex");
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it));
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new PineconeFilter(_collection, this.schema).where(it.getFilter()));
      boolean _isEmpty = it.getSortClauses().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final VectorCollection source = it.getCollection();
        final Function1<SortClause, Code.Expr> _function = (SortClause clause) -> {
          return this.sortKey(source, clause);
        };
        args.put("order", Code.items(ListExtensions.<SortClause, Code.Expr>map(it.getSortClauses(), _function)));
      }
      this.extensionArgs(it);
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A sort clause of a scroll, applied in the runtime to metadata values.
   */
  private Code.Expr sortKey(final VectorCollection collection, final SortClause clause) {
    Code.Entries _xblockexpression = null;
    {
      boolean _isByRelevance = clause.isByRelevance();
      if (_isByRelevance) {
        throw Unsupported.of("sort by relevance in a scroll; a scroll has no scores");
      }
      final FieldPath path = clause.getField();
      if (((((path.getRawPath() != null) || (!path.getSubFields().isEmpty())) || (!(path.getField() instanceof PayloadField))) || this.schema.isFullText(collection, path.getField()))) {
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
      final Code.Entries result = PineconeSchema.kw("key", Code.text(PineconeSchema.fieldKey(path.getField())));
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
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "size", Code.num(it.getSampleSize()));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), false);
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      VectorCollection _collection = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection, this.schema).where(it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.extensionArgs(it);
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
      final VectorField field = this.storedField(it.getCollection(), this.searchField(it.getCollection(), it.getVectorField()), "distance-matrix");
      final Code.Entries args = PineconeSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(PineconeSchema.fieldKey(field)), 
        "metric", Code.text(this.schema.metricName(this.indexMetric(it.getCollection(), field))));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      VectorCollection _collection = it.getCollection();
      final PineconeFilter where = new PineconeFilter(_collection, this.schema).where(it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.extensionArgs(it);
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + PineconeStatements.NO_GENERATION));
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
    String _plus = (PineconeStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private static Code.Stmt run(final Code.Expr expr) {
    return new Code.Run(expr);
  }

  /**
   * The Pinecone namespace a statement addresses: its tenant, its namespace, or the default namespace of the index.
   */
  private String namespaceOf(final EObject statement) {
    String _xblockexpression = null;
    {
      Object _feature = this.feature(statement, "partition");
      boolean _tripleNotEquals = (_feature != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
      }
      Object _feature_1 = this.feature(statement, "partitions");
      final List<?> partitions = ((List<?>) _feature_1);
      if (((partitions != null) && (!partitions.isEmpty()))) {
        throw Unsupported.of(PineconeSchema.NO_PARTITIONS);
      }
      Object _feature_2 = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature_2);
      Object _feature_3 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_3);
      if (((tenant != null) && (namespace != null))) {
        throw Unsupported.of("a tenant and a namespace on one statement; both select a Pinecone namespace");
      }
      if ((tenant != null)) {
        return PineconeSchema.namespaceName(tenant.getName());
      }
      if ((namespace != null)) {
        return PineconeSchema.namespaceName(namespace);
      }
      _xblockexpression = this.defaultNamespace;
    }
    return _xblockexpression;
  }

  private Code.Entries layout(final VectorCollection collection, final EObject query) {
    Code.Entries _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(query, "consistency");
      if (_written) {
        Object _feature = this.feature(query, "consistency");
        this.schema.consistency(((EConsistencyLevel) _feature));
      }
      _xblockexpression = this.schema.layout(collection, this.namespaceOf(query));
    }
    return _xblockexpression;
  }

  private void common(final EObject query, final VectorCollection collection, final Code.Entries args, final OutputSpec output, final PineconeFilter where) {
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
   * Output fields (the key is always returned) and the vector fields to include.
   */
  private void projection(final VectorCollection collection, final Code.Entries args, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    final Field key = collection.getPrimaryKey();
    final List<String> fields = CollectionLiterals.<String>newArrayList();
    final List<String> vectors = CollectionLiterals.<String>newArrayList();
    final List<VectorField> stored = this.storedVectors(collection);
    List<? extends Field> _xifexpression = null;
    if (((output == null) || output.isAllFields())) {
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(collection);
      _xifexpression = ((List<? extends Field>) _payloadFields);
    } else {
      _xifexpression = output.getFields();
    }
    final List<? extends Field> listed = _xifexpression;
    final Function1<Field, Boolean> _function = (Field candidate) -> {
      return Boolean.valueOf(((candidate != key) && (!excluded.contains(candidate))));
    };
    Iterable<? extends Field> _filter = IterableExtensions.filter(listed, _function);
    for (final Field field : _filter) {
      boolean _matched = false;
      if (field instanceof PayloadField) {
        _matched=true;
        String _fieldKey = PineconeSchema.fieldKey(field);
        fields.add(_fieldKey);
      }
      if (!_matched) {
        if (field instanceof VectorField) {
          _matched=true;
          boolean _contains = stored.contains(field);
          if (_contains) {
            String _fieldKey = PineconeSchema.fieldKey(field);
            vectors.add(_fieldKey);
          } else {
            EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(collection, ((VectorField)field));
            boolean _tripleNotEquals = (_embeddingModel != null);
            if (_tripleNotEquals) {
              String _fieldName = ModelUtil.fieldName(field);
              String _plus = ("output of " + _fieldName);
              String _plus_1 = (_plus + ", ");
              String _plus_2 = (_plus_1 + PineconeStatements.STORED_TEXT);
              this.notes.add(_plus_2);
            } else {
              String _fieldName_1 = ModelUtil.fieldName(field);
              String _plus_3 = ("output of " + _fieldName_1);
              String _plus_4 = (_plus_3 + ", which Pinecone derives from full-text fields");
              this.notes.add(_plus_4);
            }
          }
        }
      }
    }
    if (withVectors) {
      final Function1<VectorField, String> _function_1 = (VectorField vector) -> {
        return PineconeSchema.fieldKey(vector);
      };
      List<String> _map = ListExtensions.<VectorField, String>map(stored, _function_1);
      Iterables.<String>addAll(vectors, _map);
    }
    args.put("output", Code.texts(fields));
    boolean _isEmpty = vectors.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("vectors", Code.texts(vectors.stream().distinct().toList()));
    }
  }

  private List<VectorField> storedVectors(final VectorCollection collection) {
    VectorField _denseVector = this.schema.denseVector(collection);
    VectorField _sparseVector = this.schema.sparseVector(collection);
    final Function1<VectorField, Boolean> _function = (VectorField vector) -> {
      EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(collection, vector);
      return Boolean.valueOf((_embeddingModel == null));
    };
    return IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(IterableExtensions.<VectorField>filterNull(Collections.<VectorField>unmodifiableList(CollectionLiterals.<VectorField>newArrayList(_denseVector, _sparseVector))), _function));
  }

  /**
   * Rejects clauses Pinecone lacks with their reason.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String name : features) {
      boolean _written = ModelUtil.written(statement, name);
      if (_written) {
        String _elvis = null;
        String _get = PineconeStatements.CLAUSES.get(name);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = name;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String reason = PineconeStatements.REASONS.get(name);
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

  /**
   * The dense or sparse vector field a search scores: the named one, or the dense field of the collection.
   */
  private VectorField searchField(final VectorCollection collection, final VectorField explicit) {
    VectorField _xblockexpression = null;
    {
      final VectorField dense = this.schema.denseVector(collection);
      if ((explicit != null)) {
        boolean _isBm25Output = PineconeSchema.isBm25Output(collection, explicit);
        if (_isBm25Output) {
          String _fieldName = ModelUtil.fieldName(explicit);
          String _plus = ("vector search on the bm25 output " + _fieldName);
          String _plus_1 = (_plus + "; use keyword search");
          throw Unsupported.of(_plus_1);
        }
        if (((explicit != dense) && (explicit != this.schema.sparseVector(collection)))) {
          String _fieldName_1 = ModelUtil.fieldName(explicit);
          String _plus_2 = ("vector search on " + _fieldName_1);
          String _plus_3 = (_plus_2 + ", which is not a vector field of the collection");
          throw Unsupported.of(_plus_3);
        }
        return explicit;
      }
      if ((dense == null)) {
        throw Unsupported.of("vector search on a collection without a dense vector field");
      }
      _xblockexpression = dense;
    }
    return _xblockexpression;
  }

  /**
   * A vector field whose stored vectors the runtime reads: not one that Pinecone embeds from text.
   */
  private VectorField storedField(final VectorCollection collection, final VectorField field, final String statement) {
    VectorField _xblockexpression = null;
    {
      EmbeddingModel _embeddingModel = PineconeSchema.embeddingModel(collection, field);
      boolean _tripleNotEquals = (_embeddingModel != null);
      if (_tripleNotEquals) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ((statement + " on ") + _fieldName);
        String _plus_1 = (_plus + "; ");
        String _plus_2 = (_plus_1 + PineconeStatements.STORED_TEXT);
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = field;
    }
    return _xblockexpression;
  }

  private static boolean sparse(final VectorField field) {
    EVectorDType _type = field.getType();
    return Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
  }

  /**
   * The metric a field is scored by; Pinecone scores sparse vectors by dot product.
   */
  private EMetric indexMetric(final VectorCollection collection, final VectorField field) {
    EMetric _xblockexpression = null;
    {
      final VectorIndex index = ModelUtil.vectorIndex(collection, field);
      final EMetric declared = this.schema.metric(field, index);
      boolean _sparse = PineconeStatements.sparse(field);
      boolean _not = (!_sparse);
      if (_not) {
        return declared;
      }
      if (((!Objects.equals(declared, EMetric.DOT)) && (ModelUtil.written(field, "metric") || ((index != null) && ModelUtil.written(index, "metric"))))) {
        String _literal = declared.getLiteral();
        String _plus = ("metric " + _literal);
        String _plus_1 = (_plus + " on sparse field ");
        String _fieldName = ModelUtil.fieldName(field);
        String _plus_2 = (_plus_1 + _fieldName);
        String _plus_3 = (_plus_2 + "; Pinecone scores sparse vectors by dot product");
        throw Unsupported.of(_plus_3);
      }
      _xblockexpression = EMetric.DOT;
    }
    return _xblockexpression;
  }

  /**
   * The query of a vector search: a dense or sparse vector, the stored vector of a record, the query text of a field Pinecone
   * embeds, or a query text embedded with the model clause through Pinecone Inference embed.
   */
  private Code.Expr queryVector(final QueryInput input, final VectorCollection collection, final VectorField field, final String variable, final List<Code.Stmt> prelude, final String namespace, final EmbeddingModel model) {
    Code.Expr _xblockexpression = null;
    {
      if ((input == null)) {
        throw Unsupported.of("a vector query without input");
      }
      final String name = ModelUtil.fieldName(field);
      final EmbeddingModel embedded = PineconeSchema.embeddingModel(collection, field);
      if (((model != null) && (!Objects.equals(input.getKind(), EQueryInputMode.TEXT)))) {
        String _literal = input.getKind().getLiteral();
        String _plus = ("the \'model\' clause with " + _literal);
        String _plus_1 = (_plus + " input; it names the model that embeds a query text");
        throw Unsupported.of(_plus_1);
      }
      Code.Expr _switchResult = null;
      EQueryInputMode _kind = input.getKind();
      if (_kind != null) {
        switch (_kind) {
          case VECTOR:
            Code.Expr _xblockexpression_1 = null;
            {
              if ((embedded != null)) {
                throw Unsupported.of((((("a query vector for " + name) + "; ") + PineconeStatements.STORED_TEXT) + ", so it is searched by query text"));
              }
              boolean _sparse = PineconeStatements.sparse(field);
              if (_sparse) {
                throw Unsupported.of(("a dense query vector for sparse field " + name));
              }
              _xblockexpression_1 = Literals.of(input.getValue());
            }
            _switchResult = _xblockexpression_1;
            break;
          case SPARSE_VECTOR:
            Code.Expr _xblockexpression_2 = null;
            {
              boolean _sparse = PineconeStatements.sparse(field);
              boolean _not = (!_sparse);
              if (_not) {
                throw Unsupported.of("sparse_vector query input; a Pinecone dense search scores one query vector");
              }
              if ((embedded != null)) {
                throw Unsupported.of((((("a sparse query vector for " + name) + "; ") + PineconeStatements.STORED_TEXT) + ", so it is searched by query text"));
              }
              _xblockexpression_2 = this.fieldValue(Literals.of(input.getValue()));
            }
            _switchResult = _xblockexpression_2;
            break;
          case ID:
            Code.Ref _xblockexpression_3 = null;
            {
              this.storedField(collection, field, "a query by id");
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final VectorCollection source = _elvis;
              Code.Call _runtime = Code.runtime("lookup_vector", PineconeSchema.kw("collection", this.schema.layout(source, namespace), 
                "field", Code.text(PineconeSchema.fieldKey(field)), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let(variable, _runtime);
              prelude.add(_let);
              _xblockexpression_3 = new Code.Ref(variable);
            }
            _switchResult = _xblockexpression_3;
            break;
          case TEXT:
            Code.Ref _xblockexpression_4 = null;
            {
              if ((embedded != null)) {
                if (((model != null) && (model != embedded))) {
                  String _name = model.getName();
                  String _plus_2 = ("the \'model\' clause naming " + _name);
                  String _plus_3 = (_plus_2 + " for ");
                  String _plus_4 = (_plus_3 + name);
                  String _plus_5 = (_plus_4 + ", which Pinecone embeds with ");
                  String _name_1 = embedded.getName();
                  String _plus_6 = (_plus_5 + _name_1);
                  String _plus_7 = (_plus_6 + ", fixed at creation");
                  throw Unsupported.of(_plus_7);
                }
                return Literals.of(input.getValue());
              }
              if ((model == null)) {
                throw Unsupported.of(((("text input for vector field " + name) + " without a model; ") + PineconeStatements.NO_LOCAL_EMBEDDING));
              }
              Code.Call _runtime = Code.runtime("embed_query", this.queryEmbedding(model, field, Literals.of(input.getValue())));
              Code.Let _let = new Code.Let(variable, _runtime);
              prelude.add(_let);
              _xblockexpression_4 = new Code.Ref(variable);
            }
            _switchResult = _xblockexpression_4;
            break;
          default:
            String _literal_1 = input.getKind().getLiteral();
            String _plus_2 = (_literal_1 + " query input; a Pinecone dense search scores one query vector");
            throw Unsupported.of(_plus_2);
        }
      } else {
        String _literal_1 = input.getKind().getLiteral();
        String _plus_2 = (_literal_1 + " query input; a Pinecone dense search scores one query vector");
        throw Unsupported.of(_plus_2);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A query text embedded by Pinecone Inference embed with the model clause's hosted model (SPEC-I embed).
   */
  private Code.Entries queryEmbedding(final EmbeddingModel model, final VectorField field, final Code.Expr query) {
    Code.Entries _xblockexpression = null;
    {
      String _localPath = model.getLocalPath();
      boolean _tripleNotEquals = (_localPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("local model files; Pinecone runs the models it hosts");
      }
      final Code.Entries parameters = Code.entries();
      for (final Pair<String, String> pair : PineconeStatements.QUERY_EMBEDDING) {
        parameters.put(pair.getKey(), Code.text(pair.getValue()));
      }
      boolean _written = ModelUtil.written(model, "dimension");
      if (_written) {
        if (((field.getDimension() > 0) && (model.getDimension() != field.getDimension()))) {
          String _name = model.getName();
          String _plus = ("embedding model " + _name);
          String _plus_1 = (_plus + " with dim ");
          int _dimension = model.getDimension();
          String _plus_2 = (_plus_1 + Integer.valueOf(_dimension));
          String _plus_3 = (_plus_2 + " for vector field ");
          String _fieldName = ModelUtil.fieldName(field);
          String _plus_4 = (_plus_3 + _fieldName);
          String _plus_5 = (_plus_4 + " with dim ");
          int _dimension_1 = field.getDimension();
          String _plus_6 = (_plus_5 + Integer.valueOf(_dimension_1));
          throw Unsupported.of(_plus_6);
        }
        parameters.put("dimension", Code.num(model.getDimension()));
      }
      this.extensionArgs(model);
      String _provider = model.getProvider();
      String _elvis = null;
      String _modelId = model.getModelId();
      if (_modelId != null) {
        _elvis = _modelId;
      } else {
        String _name_1 = model.getName();
        _elvis = _name_1;
      }
      _xblockexpression = PineconeSchema.kw("model", Code.text(PineconeSchema.hostedName(_provider, _elvis)), "text", query, "parameters", parameters);
    }
    return _xblockexpression;
  }

  /**
   * A rerank clause: the runtime sends the hits' rank field to Pinecone Inference rerank with the query text.
   */
  private void rerank(final Reranker reranker, final VectorCollection collection, final Code.Expr query, final String defaultField, final Code.Entries args) {
    if ((reranker == null)) {
      return;
    }
    if ((query == null)) {
      throw Unsupported.of(("the \'rerank\' clause on a search without a query text; " + PineconeStatements.RERANK_TEXT));
    }
    String _provider = reranker.getProvider();
    String _elvis = null;
    String _modelId = reranker.getModelId();
    if (_modelId != null) {
      _elvis = _modelId;
    } else {
      String _name = reranker.getName();
      _elvis = _name;
    }
    final String model = PineconeSchema.hostedName(_provider, _elvis);
    String _xifexpression = null;
    Field _field = reranker.getField();
    boolean _tripleEquals = (_field == null);
    if (_tripleEquals) {
      _xifexpression = defaultField;
    } else {
      _xifexpression = this.rankField(collection, reranker.getField());
    }
    final String field = _xifexpression;
    if ((field == null)) {
      throw Unsupported.of("the \'rerank\' clause without a rank field; Pinecone rerank reads one text field of each hit");
    }
    final Code.Entries entry = PineconeSchema.kw("model", Code.text(model), "query", query, "field", Code.text(field));
    boolean _written = ModelUtil.written(reranker, "topN");
    if (_written) {
      entry.put("top_n", Code.num(reranker.getTopN()));
    }
    boolean _equals = Objects.equals(model, PineconeStatements.TRUNCATING_RERANKER);
    if (_equals) {
      entry.put("parameters", PineconeSchema.kw("truncate", Code.text("END")));
    }
    this.extensionArgs(reranker);
    args.put("rerank", entry);
  }

  /**
   * The field a reranker reads: a string field of the searched collection, or the text field of an embedded vector field.
   */
  private String rankField(final VectorCollection collection, final Field field) {
    String _xblockexpression = null;
    {
      final boolean textual = ((field instanceof PayloadField) && Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(((PayloadField) field).getType()));
      final boolean embedded = ((field instanceof VectorField) && (PineconeSchema.embeddingModel(collection, ((VectorField) field)) != null));
      if (((!collection.getFields().contains(field)) || (!(textual || embedded)))) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("reranking by " + _fieldName);
        String _plus_1 = (_plus + "; Pinecone rerank reads a text field of the searched collection");
        throw Unsupported.of(_plus_1);
      }
      String _xifexpression = null;
      if (embedded) {
        _xifexpression = PineconeSchema.searchKey(collection, ((VectorField) field));
      } else {
        _xifexpression = PineconeSchema.fieldKey(field);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * Pinecone search takes no score threshold; the runtime drops hits outside the bounds.
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
   * Full-text fields a keyword query scores against; without 'fields', every full-text field.
   */
  private List<String> keywordFields(final VectorCollection collection, final List<SearchFieldRef> refs) {
    List<String> _xblockexpression = null;
    {
      final List<String> names = CollectionLiterals.<String>newArrayList();
      for (final SearchFieldRef ref : refs) {
        final Function1<Field, String> _function = (Field field) -> {
          return PineconeSchema.fieldKey(field);
        };
        List<String> _map = ListExtensions.<Field, String>map(this.searchedFields(collection, ref), _function);
        Iterables.<String>addAll(names, _map);
      }
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        final Function1<PayloadField, String> _function_1 = (PayloadField field) -> {
          return PineconeSchema.fieldKey(field);
        };
        List<String> _map_1 = ListExtensions.<PayloadField, String>map(this.schema.fullTextFields(collection), _function_1);
        Iterables.<String>addAll(names, _map_1);
      }
      boolean _isEmpty_1 = names.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search on a collection without full-text fields");
      }
      _xblockexpression = names.stream().distinct().toList();
    }
    return _xblockexpression;
  }

  /**
   * The full-text fields a search field names: the field itself, or the inputs of the bm25 function that produces it.
   */
  private List<Field> searchedFields(final VectorCollection collection, final SearchFieldRef ref) {
    List<Field> _xblockexpression = null;
    {
      final Function function = ModelUtil.producingFunction(collection, ref.getField());
      List<Field> _xifexpression = null;
      if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
        _xifexpression = function.getInputs();
      } else {
        Field _field = ref.getField();
        _xifexpression = Collections.<Field>unmodifiableList(CollectionLiterals.<Field>newArrayList(_field));
      }
      final List<Field> fields = _xifexpression;
      for (final Field field : fields) {
        boolean _isFullText = this.schema.isFullText(collection, field);
        boolean _not = (!_isFullText);
        if (_not) {
          String _fieldName = ModelUtil.fieldName(field);
          String _plus = ("keyword search on " + _fieldName);
          String _plus_1 = (_plus + ", which is not a full-text field");
          throw Unsupported.of(_plus_1);
        }
      }
      _xblockexpression = fields;
    }
    return _xblockexpression;
  }

  private void searchParams(final SearchParams params) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall: Pinecone exposes no query-time recall target");
    }
    boolean _written_1 = ModelUtil.written(params, "candidatePool");
    if (_written_1) {
      throw Unsupported.of("candidates; Pinecone exposes no query-time search effort");
    }
    boolean _isExhaustive = params.isExhaustive();
    if (_isExhaustive) {
      String _get = PineconeStatements.REASONS.get("exact");
      String _plus = ("the \'exhaustive\' parameter; " + _get);
      throw Unsupported.of(_plus);
    }
    this.schema.note(params, "probes", "scanBudget", "timeoutMillis", "oversampling", "rescore", "filterStrategy");
    this.extensionArgs(params);
  }

  /**
   * Pinecone defines no vendor extension items; parameters and items in a pinecone block are rejected by name.
   */
  private void extensionArgs(final EObject host) {
    Set<String> _keySet = this.schema.extValues(host).keySet();
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
      boolean _equals = Objects.equals(_vendor, PineconeGenerator.VENDOR);
      if (_equals) {
        final Function1<ExtensionItem, Boolean> _function = (ExtensionItem entry) -> {
          return Boolean.valueOf((!(entry instanceof ExtParam)));
        };
        Iterable<ExtensionItem> _filter_1 = IterableExtensions.<ExtensionItem>filter(block.getItems(), _function);
        for (final ExtensionItem item : _filter_1) {
          String _name = item.eClass().getName();
          String _plus = (_name + " on Pinecone");
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
      _switchResult = Code.runtime("read_records", PineconeSchema.kw("location", Code.text(((FileSource)source).getLocation())));
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
        {
          this.writable(entry.getField());
          result.put(PineconeSchema.fieldKey(entry.getField()), this.fieldValue(Literals.of(entry.getValue())));
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * A record value for a vector field that Pinecone embeds from another field cannot be written (SPEC-C StringField).
   */
  private void writable(final Field field) {
    if ((!(field instanceof VectorField))) {
      return;
    }
    final VectorField vector = ((VectorField) field);
    final VectorCollection collection = ModelUtil.owningCollection(vector);
    if (((collection == null) || (PineconeSchema.embeddingModel(collection, vector) == null))) {
      return;
    }
    final Field source = PineconeSchema.embeddedText(collection, vector);
    if ((source != vector)) {
      String _fieldName = ModelUtil.fieldName(vector);
      String _plus = ("a value for vector field " + _fieldName);
      String _plus_1 = (_plus + ", which Pinecone embeds from field ");
      String _fieldName_1 = ModelUtil.fieldName(source);
      String _plus_2 = (_plus_1 + _fieldName_1);
      throw Unsupported.of(_plus_2);
    }
  }

  /**
   * Sparse vectors in Pinecone's form: parallel lists of indices and values.
   */
  private Code.Expr fieldValue(final Code.Expr value) {
    Code.Entries _xblockexpression = null;
    {
      if ((!(value instanceof Code.Sparse))) {
        return value;
      }
      final Map<Long, Double> weights = ((Code.Sparse) value).weights();
      final Function1<Long, Code.Num> _function = (Long index) -> {
        return Code.num(index.doubleValue());
      };
      final Function1<Double, Code.Num> _function_1 = (Double weight) -> {
        return Code.num((weight).doubleValue());
      };
      _xblockexpression = PineconeSchema.kw("indices", Code.items(IterableExtensions.<Code.Num>toList(IterableExtensions.<Long, Code.Num>map(weights.keySet(), _function))), "values", Code.items(IterableExtensions.<Code.Num>toList(IterableExtensions.<Double, Code.Num>map(weights.values(), _function_1))));
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
