package org.ftn.vecDSL.generator.vespa;

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
import org.eclipse.xtext.xbase.lib.Conversions;
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
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMatchOperator;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESortDirection;
import org.ftn.vecDSL.vecDSL.ETextQueryType;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
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
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.OutputSpec;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.ParamSource;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.PhasedRankProfile;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.QueryInput;
import org.ftn.vecDSL.vecDSL.QueryMethod;
import org.ftn.vecDSL.vecDSL.RankProfileUse;
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
 * Maps each vecDSL statement to calls of the Vespa runtime in the host-language-neutral code model. Application packages
 * (services.xml, schemas, query profile), YQL texts and query request bodies are built here, with every value as a request parameter
 * that the YQL references with @name, so the three host languages send identical requests; the runtimes deploy, feed and query.
 */
@SuppressWarnings("all")
public class VespaStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; VespaCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Vespa: ";

  /**
   * Suffix of constructs Vespa supports that the generator does not produce yet.
   */
  public static final String PENDING = "has no Vespa mapping yet";

  public static final String EXTERNAL = "collections derived from external sources; Vespa stores the documents it is fed and reads no external tables";

  public static final String NO_ALIASES = "aliases; Vespa addresses a document type by its schema name and has no alias for a schema or document type";

  public static final String NO_DELETION_PROTECTION = "deletion-protection; open-source Vespa has no drop guard on a schema (a deployment that removes a schema needs the schema-removal validation override, which every generated drop writes)";

  public static final String NO_STORAGE = "storage settings; a self-hosted Vespa node keeps its data under its var directory, set on the node, not per schema";

  public static final String NO_TAGS = "collection tags; a Vespa schema carries no metadata besides its definition";

  public static final String NO_TRANSACTIONS = "transactions; Vespa applies each document operation atomically on its own and has no transaction over several documents";

  public static final String SINGLE_NODE = "the program deploys a single-node application (one node in services.xml); replication and distribution need a multi-node application with hosts.xml";

  public static final String NO_USERS = "users and roles; Vespa authorizes clients by mTLS certificates (and Vespa Cloud data-plane tokens) per endpoint and has no users or roles";

  public static final String NO_RESTART = "a content node restart (adding or removing an HNSW index, or fast-search, takes effect only after the searchnode restarts), which the HTTP APIs of a running application do not offer";

  public static final String NO_SNAPSHOTS = (("snapshots; " + VespaStatements.PENDING) + " (visiting every document of the schema into a local JSON Lines file, and feeding it back to restore; backups are a Vespa Cloud feature)");

  private static final String NO_GENERATION = "generated programs deploy no models: generation needs an LLM client component in services.xml, with a local model file in the application package or an external service and its API key";

  private static final String NO_RERANKERS = "generated programs deploy no models: a cross-encoder reranks as an ONNX model in a global-phase rank expression, which needs the model file in the application package";

  private static final String NO_METRICS = "metrics configuration; Vespa reports metrics through /state/v1/metrics and the metrics proxy configured in services.xml, not through objects a client creates";

  private static final String NO_SCHEDULES = "backup schedules; open-source Vespa has no backup or snapshot scheduler";

  private static final String NO_CLUSTER_LIMITS = "cluster limits; Vespa has no setting that limits the number of schemas";

  private static final String NO_FILES = (("file resources; " + VespaStatements.PENDING) + " (files in the application package read by a linguistics or model component)");

  private static final String NO_RELEASE = "release; Vespa keeps a deployed schema\'s attributes in memory and has no request that evicts one document type";

  private static final String NO_DYNAMIC = "dynamic collections; a Vespa document type takes its declared fields only (others are dropped, X-Vespa-Ignored-Fields)";

  private static final String NO_RENAME = (VespaStatements.PENDING + " (Vespa has no rename: a new schema or field filled by visiting every document, then the old one removed)");

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("sliceIndex", "slice")));

  /**
   * Why Vespa cannot express a clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Vespa keeps only the current version of each document"), Pair.<String, String>of("reranker", VespaStatements.NO_RERANKERS), Pair.<String, String>of("queryModel", VespaSchema.NO_EMBEDDER), Pair.<String, String>of("minShouldMatch", "userInput combines the terms with and, or, weakAnd or phrase and has no minimum number of matching terms"), Pair.<String, String>of("fuzziness", VespaFilter.NO_FUZZY)));

  private static final String NO_TEXT = "the \'highlight\' clause on a search without text; dynamic summaries mark the terms of a text query";

  /**
   * Clauses Vespa supports that the generator does not produce yet, with the feature that would carry them.
   */
  private static final Map<String, String> PENDING_CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "sameElement over array<struct> fields, or the closest cell of a mixed tensor"), Pair.<String, String>of("mmr", "MMR over the candidates in the runtime"), Pair.<String, String>of("aggregations", "grouping with output(count(), sum(), avg(), min(), max())"), Pair.<String, String>of("prefetches", "staged queries or a second-phase expression"), Pair.<String, String>of("boost", "a first-phase expression over the attributes")));

  /**
   * The document summary whose vecdsl_hl_ fields bold the matched terms of the full-text fields (VespaPackage.HIGHLIGHT_SUMMARY).
   */
  private static final String NO_HIGHLIGHT_FIELD = ", which is not a full-text field; bolding marks the matched terms of index fields of type string";

  /**
   * Query settings an extension vespa block may set on a search, by key: the nearestNeighbor annotation or ranking.matching key.
   */
  private static final Map<String, String> SEARCH_SETTINGS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("explore_additional_hits", "hnsw.exploreAdditionalHits"), Pair.<String, String>of("post_filter_threshold", "postFilterThreshold"), Pair.<String, String>of("approximate_threshold", "approximateThreshold"), Pair.<String, String>of("filter_first_threshold", "filterFirstThreshold"), Pair.<String, String>of("filter_first_exploration", "filterFirstExploration"), Pair.<String, String>of("target_hits_max_adjustment_factor", "targetHitsMaxAdjustmentFactor")));

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final VespaSchema schema;

  private final Map<VectorCollection, Integer> ttls = CollectionLiterals.<VectorCollection, Integer>newHashMap();

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String currentMethod;

  public VespaStatements(final Script script) {
    VespaSchema _vespaSchema = new VespaSchema(this.notes, script);
    this.schema = _vespaSchema;
  }

  /**
   * The method for one statement, followed by helper methods it calls (batch queries).
   */
  public List<Program.Method> methods(final Method statement, final String name) {
    List<Program.Method> _xblockexpression = null;
    {
      this.notes.clear();
      this.helpers.clear();
      this.currentMethod = name;
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
          _switchResult = "Vespa computes BM25 over index fields itself (bm25(field) with enable-bm25) and has no function that writes sparse vectors";
          break;
        case "rerank":
          _switchResult = VespaStatements.NO_RERANKERS;
          break;
        case "custom":
          _switchResult = (VespaStatements.PENDING + " (an indexing statement over the input fields)");
          break;
        default:
          _switchResult = ("embedding functions" + VespaSchema.NO_MODELS);
          break;
      }
    } else {
      _switchResult = ("embedding functions" + VespaSchema.NO_MODELS);
    }
    return _switchResult;
  }

  public static String collectionLimit(final String type) {
    return (("collection limits (" + type) + "); a Vespa schema has no limit settings (tensor sizes are part of the field type)");
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
        List<Code.Stmt> _createCollections = this.createCollections(Collections.<VectorCollection>unmodifiableList(CollectionLiterals.<VectorCollection>newArrayList(((VectorCollection)concept))), ifNotExists, fresh);
        Iterables.<Code.Stmt>addAll(result, _createCollections);
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
            throw Unsupported.of(VespaStatements.NO_METRICS);
          }
          Connection _connection = ((VectorDatabase)concept).getConnection();
          boolean _tripleNotEquals_1 = (_connection != null);
          if (_tripleNotEquals_1) {
            this.connection(((VectorDatabase)concept).getConnection());
          }
          AuthConfig _auth = ((VectorDatabase)concept).getAuth();
          boolean _tripleNotEquals_2 = (_auth != null);
          if (_tripleNotEquals_2) {
            this.authentication(((VectorDatabase)concept).getAuth());
          }
          final List<VectorCollection> collections = CollectionLiterals.<VectorCollection>newArrayList();
          EList<Cluster> _clusters = ((VectorDatabase)concept).getClusters();
          for (final Cluster cluster : _clusters) {
            List<VectorCollection> _clusterCollections = this.clusterCollections(cluster);
            Iterables.<VectorCollection>addAll(collections, _clusterCollections);
          }
          EList<Database> _databases = ((VectorDatabase)concept).getDatabases();
          for (final Database database : _databases) {
            EList<VectorCollection> _collections = database.getCollections();
            Iterables.<VectorCollection>addAll(collections, _collections);
          }
          boolean _isEmpty = collections.isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            List<Code.Stmt> _createCollections = this.createCollections(collections, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createCollections);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          final List<VectorCollection> collections = this.clusterCollections(((Cluster)concept));
          boolean _isEmpty = collections.isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            List<Code.Stmt> _createCollections = this.createCollections(collections, ifNotExists, fresh);
            Iterables.<Code.Stmt>addAll(result, _createCollections);
          }
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          boolean _isEmpty = ((Database)concept).getCollections().isEmpty();
          if (_isEmpty) {
            throw Unsupported.of("a logical-db without collections; a logical-db is a prefix of the document type names of its collections");
          }
          List<Code.Stmt> _createCollections = this.createCollections(((Database)concept).getCollections(), ifNotExists, fresh);
          Iterables.<Code.Stmt>addAll(result, _createCollections);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of(("creating a vector index on an existing collection; it needs " + VespaStatements.NO_RESTART));
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
          String _functionReason = VespaStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          VespaSchema.checkAnalyzer(((Analyzer)concept));
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
          VespaSchema.checkFilter(((AnalyzerFilter)concept).getType().getLiteral());
          List<Code.Stmt> _require = this.require(this.member(analyzer, "an analyzer"));
          Iterables.<Code.Stmt>addAll(result, _require);
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("field limits; Vespa validates the value types of a document, not their sizes");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + VespaSchema.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + VespaStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + VespaStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of((("quantization outside a vector index; " + VespaStatements.PENDING) + " (binarize | pack_bits into a hamming field)"));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + VespaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + VespaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_STORAGE);
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
          throw Unsupported.of(VespaStatements.NO_USERS);
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(VespaStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (VespaStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<VectorCollection> clusterCollections(final Cluster cluster) {
    EList<VectorCollection> _xblockexpression = null;
    {
      boolean _isEmpty = cluster.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(VespaStatements.NO_CLUSTER_LIMITS);
      }
      if (((cluster.getReplication() != null) || (cluster.getSharding() != null))) {
        throw Unsupported.of(("replication and sharding; " + VespaStatements.SINGLE_NODE));
      }
      Set<String> _keySet = ModelUtil.extParams(cluster, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      _xblockexpression = cluster.getCollections();
    }
    return _xblockexpression;
  }

  /**
   * The collections' schemas join the package, which is deployed once for all of them.
   */
  private List<Code.Stmt> createCollections(final List<VectorCollection> collections, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<String> created = CollectionLiterals.<String>newArrayList();
      for (final VectorCollection c : collections) {
        String _name = this.declare(c, fresh).name();
        created.add(_name);
      }
      Code.Stmt _deploy = this.schema.deploy(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), created, ifNotExists, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_deploy));
    }
    return _xblockexpression;
  }

  private VespaPackage.Schema declare(final VectorCollection c, final boolean fresh) {
    VespaPackage.Schema _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(VespaStatements.EXTERNAL);
      }
      Field _ttlField = c.getTtlField();
      boolean _tripleNotEquals_1 = (_ttlField != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of((("ttl-field; " + VespaStatements.PENDING) + " (a garbage-collection selection on the field, <type>.<field> > now())"));
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of((("auto-id; " + VespaStatements.PENDING) + " (document ids generated by the runtime)"));
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(VespaStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of((("cross-ref; " + VespaStatements.PENDING) + " (a reference field to a global document type and imported fields)"));
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_2 = (_embeddingModel != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of(("embedding-model" + VespaSchema.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_3 = (_storage != null);
      if (_tripleNotEquals_3) {
        throw Unsupported.of(VespaStatements.NO_STORAGE);
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(VespaStatements.NO_TAGS);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = VespaStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        throw Unsupported.of(VespaStatements.collectionLimit(IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral()));
      }
      boolean _isEmpty_4 = c.getAliases().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        throw Unsupported.of(VespaStatements.NO_ALIASES);
      }
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        throw Unsupported.of(VespaStatements.NO_DYNAMIC);
      }
      final Function1<Partition, Boolean> _function = (Partition it) -> {
        return Boolean.valueOf((((it.getKey() != null) || (!it.getSubPartitions().isEmpty())) || (ModelUtil.written(it, "strategy") && (!Objects.equals(it.getStrategy().getLiteral(), "list")))));
      };
      boolean _isEmpty_5 = IterableExtensions.isEmpty(IterableExtensions.<Partition>filter(ModelUtil.partitions(c), _function));
      boolean _not_5 = (!_isEmpty_5);
      if (_not_5) {
        throw Unsupported.of("partitions by key, with a strategy other than list, or with sub-partitions; a partition is a value of the hidden vecdsl_partition attribute");
      }
      this.schema.note(c, "idType", "storedSourceFields", "generativeModel", "consistency", "description");
      if (fresh) {
        this.schema.evolve(c, null);
      }
      this.schema.setDeclaring(true);
      VespaPackage.Schema _xtrycatchfinallyexpression = null;
      try {
        VespaPackage.Schema _schema = this.schema.schema(c);
        _xtrycatchfinallyexpression = _schema;
      } finally {
        this.schema.setDeclaring(false);
      }
      final VespaPackage.Schema declared = _xtrycatchfinallyexpression;
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex payloadIndex : _filter) {
        this.schema.checkPayloadIndex(c, payloadIndex);
      }
      boolean _written = ModelUtil.written(c, "ttlSeconds");
      if (_written) {
        this.ttls.put(c, Integer.valueOf(c.getTtlSeconds()));
      }
      this.schema.put(declared);
      _xblockexpression = declared;
    }
    return _xblockexpression;
  }

  /**
   * The program endpoint: checked here, opened by connect(). A bearer token comes from the environment only.
   */
  private void connection(final Connection concept) {
    if ((((concept.getLocalPath() != null) || concept.isInMemory()) || Objects.equals(concept.getDeployment(), EDeploymentMode.EMBEDDED))) {
      throw Unsupported.of("embedded deployments; Vespa runs as a service, and the programs use its HTTP APIs");
    }
    if (((concept.getApiKey() != null) || (concept.getToken() != null))) {
      throw Unsupported.of("api keys and tokens on the connection; a self-hosted Vespa node authenticates clients with mTLS certificates (data-plane tokens are a Vespa Cloud feature; the runtime sends VECDSL_VESPA_TOKEN when it is set)");
    }
    if (((concept.getUsername() != null) || (concept.getPassword() != null))) {
      throw Unsupported.of("users and passwords; Vespa has no password authentication");
    }
    String _databaseName = concept.getDatabaseName();
    boolean _tripleNotEquals = (_databaseName != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a database name on the connection; a vecDSL logical-db is a prefix of the document type names");
    }
    int _grpcPort = concept.getGrpcPort();
    boolean _greaterThan = (_grpcPort > 0);
    if (_greaterThan) {
      throw Unsupported.of("grpc-port; the generated programs use the HTTP APIs (document/v1, query, deploy)");
    }
    Set<String> _keySet = ModelUtil.extParams(concept, VespaGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
    if (((concept.getUri() != null) && (VespaGenerator.endpoint(concept) == null))) {
      String _uri = concept.getUri();
      String _plus = ("the connection uri " + _uri);
      String _plus_1 = (_plus + "; Vespa programs take an http or https URL with a host, or host:port (read as http://host:port)");
      throw Unsupported.of(_plus_1);
    }
    this.schema.note(concept, "cloud", "region", "project");
  }

  /**
   * none needs nothing; the other methods are not part of a single-node open-source deployment the program can configure.
   */
  private void authentication(final AuthConfig auth) {
    EAuthType _type = auth.getType();
    if (_type != null) {
      switch (_type) {
        case NONE:
          break;
        case MTLS:
          throw Unsupported.of((("mtls authentication; " + VespaStatements.PENDING) + " (client certificates in the runtimes and a TLS-enabled node configured outside the application package)"));
        case TOKEN:
          throw Unsupported.of("token authentication; data-plane tokens are a Vespa Cloud feature (a self-hosted node authenticates clients with mTLS)");
        default:
          String _literal = auth.getType().getLiteral();
          String _plus = (_literal + " authentication; Vespa authenticates clients with mTLS certificates (and Vespa Cloud data-plane tokens) and has no ");
          String _literal_1 = auth.getType().getLiteral();
          String _plus_1 = (_plus + _literal_1);
          String _plus_2 = (_plus_1 + " method");
          throw Unsupported.of(_plus_2);
      }
    } else {
      String _literal = auth.getType().getLiteral();
      String _plus = (_literal + " authentication; Vespa authenticates clients with mTLS certificates (and Vespa Cloud data-plane tokens) and has no ");
      String _literal_1 = auth.getType().getLiteral();
      String _plus_1 = (_plus + _literal_1);
      String _plus_2 = (_plus_1 + " method");
      throw Unsupported.of(_plus_2);
    }
    if (((!Objects.equals(auth.getType(), EAuthType.NONE)) && (auth.getToken() instanceof StringLit))) {
      throw Unsupported.of("a secret written in the script; read it from an environment variable with env(...)");
    }
    String _certificatePath = auth.getCertificatePath();
    boolean _tripleNotEquals = (_certificatePath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of((("client certificates; " + VespaStatements.PENDING) + " (client certificates in the runtimes)"));
    }
    if (((auth.getCmekKeyId() != null) || auth.isFieldEncryption())) {
      throw Unsupported.of("customer-managed keys and field encryption; open-source Vespa has no encryption at rest of its own (encryption is a property of the disk)");
    }
    boolean _isEmpty = auth.getRoles().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of(VespaStatements.NO_USERS);
    }
    this.schema.note(auth, "auditLog");
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
    throw Unsupported.of((what + " outside a collection; it belongs to a collection\'s schema"));
  }

  /**
   * Existence check of a collection: its schema file in the active application package.
   */
  private List<Code.Stmt> require(final VectorCollection collection) {
    Code.Call _runtime = Code.runtime("require_collection", VespaSchema.kw("document_type", Code.text(this.schema.schema(collection).name())));
    Code.Run _run = new Code.Run(_runtime);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  /**
   * A payload index on an existing collection: the field's attribute already serves its filters; fast-search would need a restart.
   */
  private List<Code.Stmt> payloadIndex(final PayloadIndex index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorCollection _elvis = null;
      VectorCollection _targetCollection = ModelUtil.targetCollection(index);
      if (_targetCollection != null) {
        _elvis = _targetCollection;
      } else {
        VectorCollection _xifexpression = null;
        Field _field = index.getTarget().getField();
        boolean _tripleEquals = (_field == null);
        if (_tripleEquals) {
          _xifexpression = null;
        } else {
          _xifexpression = ModelUtil.owningCollection(index.getTarget().getField());
        }
        _elvis = _xifexpression;
      }
      final VectorCollection collection = _elvis;
      if ((collection == null)) {
        throw Unsupported.of("a payload index outside a collection");
      }
      this.schema.checkPayloadIndex(collection, index);
      this.notes.add("fast-search on PayloadIndex, which a deployed attribute gets only after a content node restart (filters work without it)");
      _xblockexpression = this.require(collection);
    }
    return _xblockexpression;
  }

  /**
   * A new field joins the schema; Vespa adds a field to a deployed schema without a validation override.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VespaPackage.Schema before = this.schema.schema(collection);
      VespaPackage.Schema after = before;
      List<VespaPackage.Field> _fieldsOf = this.schema.fieldsOf(collection, field);
      for (final VespaPackage.Field added : _fieldsOf) {
        after = after.with(added);
      }
      this.schema.evolve(collection, after);
      this.schema.put(after);
      Code.Stmt _deploy = this.schema.deploy(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_deploy));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((concept instanceof Alias)) {
        throw Unsupported.of(VespaStatements.NO_ALIASES);
      }
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; the generator alters collections");
        throw Unsupported.of(_plus_1);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("renaming a collection; " + VespaStatements.NO_RENAME));
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<AlterOperation> _operations = it.getOperations();
      for (final AlterOperation operation : _operations) {
        List<Code.Stmt> _alter = this.alter(collection, operation);
        Iterables.<Code.Stmt>addAll(result, _alter);
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
      final VespaPackage.Schema before = this.schema.schema(collection);
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
            final String named = this.schema.name(collection, ((DropFieldOp)operation).getField());
            String _key = before.key();
            boolean _equals = Objects.equals(named, _key);
            if (_equals) {
              throw Unsupported.of("dropping the primary key; a vecDSL collection keeps its key");
            }
            final VespaPackage.Schema after = before.without(named);
            this.schema.evolve(collection, after);
            this.schema.put(after);
            this.notes.add("drop-field on DropFieldOp keeps the removed field\'s data on the content node; a field added later under the same name and type may show it again");
            Code.Stmt _deploy = this.schema.deploy(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_deploy));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof RenameFieldOp) {
          _matched=true;
          throw Unsupported.of(("rename-field; " + VespaStatements.NO_RENAME));
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
          throw Unsupported.of(VespaStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          String _literal = ((AddFunctionOp)operation).getNewFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = VespaStatements.functionReason(((AddFunctionOp)operation).getNewFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          String _literal = ((DropFunctionOp)operation).getFunction().getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = VespaStatements.functionReason(((DropFunctionOp)operation).getFunction().getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (VespaStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * ttl: the expiry time of every existing document is set (an update where the selection is true), and later writes of the program
   * use the ttl; the deployed schema already has the expiry attribute and the garbage-collection selection.
   */
  private List<Code.Stmt> setProperty(final VectorCollection collection, final SetPropertyOp operation) {
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
            this.ttls.put(collection, Integer.valueOf(seconds));
            Code.Call _runtime = Code.runtime("expire", VespaSchema.kw("document_type", Code.text(this.schema.schema(collection).name()), "seconds", Code.num(seconds)));
            Code.Return _return = new Code.Return(_runtime);
            _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
          }
          _switchResult = _xblockexpression;
          break;
        case DESCRIPTION:
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            this.notes.add("description on SetPropertyOp, which a Vespa schema does not store");
            _xblockexpression_1 = this.require(collection);
          }
          _switchResult = _xblockexpression_1;
          break;
        case CONSISTENCY:
          _switchResult = this.require(collection);
          break;
        case DYNAMIC:
          throw Unsupported.of(VespaStatements.NO_DYNAMIC);
        case TAGS:
          throw Unsupported.of(VespaStatements.NO_TAGS);
        case DELETION_PROTECTION:
          throw Unsupported.of(VespaStatements.NO_DELETION_PROTECTION);
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
        _switchResult = this.dropCollections(Collections.<VectorCollection>unmodifiableList(CollectionLiterals.<VectorCollection>newArrayList(((VectorCollection)concept))), it.isIfExists());
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            boolean _isEmpty = ((Database)concept).getCollections().isEmpty();
            if (_isEmpty) {
              throw Unsupported.of("dropping a logical-db without collections; a logical-db is a prefix of the document type names of its collections");
            }
            _xblockexpression_1 = this.dropCollections(((Database)concept).getCollections(), it.isIfExists());
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), VespaPackage.TENANT, ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), VespaPackage.NAMESPACE, ((Namespace)concept).getKey());
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a partition"), VespaPackage.PARTITION, ((Partition)concept).getName());
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
          throw Unsupported.of(VespaStatements.NO_ALIASES);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a vector index; it needs " + VespaStatements.NO_RESTART));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          throw Unsupported.of(("dropping a payload index; removing fast-search from an attribute needs " + VespaStatements.NO_RESTART));
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          String _literal = ((Function)concept).getType().getLiteral();
          String _plus = (_literal + " functions; ");
          String _functionReason = VespaStatements.functionReason(((Function)concept).getType().getLiteral());
          String _plus_1 = (_plus + _functionReason);
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of((("dropping quantization; " + VespaStatements.PENDING) + " (binarize | pack_bits into a hamming field)"));
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Vespa object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; the program drops schemas and documents, not the Vespa node");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of(("dropping a cluster; " + VespaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; Vespa\'s linguistics belongs to the node (drop the full-text fields)");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; Vespa\'s linguistics belongs to the node");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("field limits; Vespa validates the value types of a document, not their sizes");
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_FILES);
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + VespaSchema.NO_MODELS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(("generative models; " + VespaStatements.NO_GENERATION));
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of(("rerankers; " + VespaStatements.NO_RERANKERS));
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(("replication; " + VespaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of(("sharding; " + VespaStatements.SINGLE_NODE));
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_STORAGE);
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of("dropping authentication; TLS and certificates are configured on the Vespa node");
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_USERS);
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_SCHEDULES);
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(VespaStatements.NO_CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of(VespaStatements.collectionLimit(((CollectionLimit)concept).getType().getLiteral()));
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((VespaStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The schemas leave the package; the runtime deletes their documents, then deploys with the schema-removal override.
   */
  private List<Code.Stmt> dropCollections(final List<VectorCollection> collections, final boolean ifExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<String> dropped = CollectionLiterals.<String>newArrayList();
      for (final VectorCollection c : collections) {
        {
          final String type = this.schema.schema(c).name();
          this.schema.remove(type);
          this.schema.evolve(c, null);
          this.ttls.remove(c);
          dropped.add(type);
        }
      }
      Code.Stmt _deploy = this.schema.deploy(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("schema-removal")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), false, dropped, ifExists);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_deploy));
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
   * Dropping a tenant, namespace or partition deletes its documents (the ids found by a query on the hidden attribute).
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String attribute, final String value) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VespaPackage.Schema doc = this.schema.schema(collection);
      Code.Text _text = Code.text(value);
      final VespaFilter restriction = new VespaFilter(this.schema, collection).slice(attribute, Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text)));
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()));
      args.put("select", this.select(doc, restriction.parts(), null));
      Code.Call _runtime = Code.runtime("delete", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    throw Unsupported.of(VespaStatements.NO_ALIASES);
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
      this.schema.note(it, "recordCountHint", "batchSize");
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
      Code.Call _runtime = Code.runtime("write", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The arguments of a write: document type, key field, records, conflict policy, counter, vector sizes, slices, expiry and version.
   */
  private Code.Entries writeArgs(final EObject statement, final VectorCollection collection, final RecordSource source, final String policy, final String counter, final Integer ttl) {
    Code.Entries _xblockexpression = null;
    {
      final VespaPackage.Schema doc = this.schema.schema(collection);
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()), "records", this.rows(source, collection), "policy", Code.text(policy), 
        "counter", Code.text(counter));
      this.vectorSizes(args, collection);
      final Code.Entries slices = this.slices(statement, collection);
      boolean _isEmpty = slices.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("slices", slices);
      }
      this.expiry(args, collection, ttl);
      boolean _isVersioned = this.schema.isVersioned(collection);
      if (_isVersioned) {
        args.put("versioned", Code.flag(true));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private void vectorSizes(final Code.Entries args, final VectorCollection collection) {
    final Code.Entries sizes = Code.entries();
    List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
    for (final VectorField field : _vectorFields) {
      {
        final String named = this.schema.name(collection, field);
        boolean _has = this.schema.schema(collection).has(named);
        if (_has) {
          sizes.put(named, Code.num(field.getDimension()));
        }
      }
    }
    boolean _isEmpty = sizes.entries().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("vectors", sizes);
    }
  }

  /**
   * Documents of an expiring collection carry their expiry time: now plus the ttl, or none (the far future) without one.
   */
  private void expiry(final Code.Entries args, final VectorCollection collection, final Integer ttl) {
    boolean _expiring = this.schema.schema(collection).expiring();
    boolean _not = (!_expiring);
    if (_not) {
      return;
    }
    args.put("expiring", Code.flag(true));
    Integer _elvis = null;
    if (ttl != null) {
      _elvis = ttl;
    } else {
      Integer _get = this.ttls.get(collection);
      _elvis = _get;
    }
    final Integer seconds = _elvis;
    if ((seconds != null)) {
      args.put("ttl", Code.num((seconds).intValue()));
    }
  }

  /**
   * Hidden attribute values for tenant, namespace and partition of a write; partitioned collections default to the default partition.
   */
  private Code.Entries slices(final EObject statement, final VectorCollection collection) {
    Code.Entries _xblockexpression = null;
    {
      final VespaPackage.Schema doc = this.schema.schema(collection);
      final Code.Entries result = Code.entries();
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        boolean _has = doc.has(VespaPackage.TENANT);
        boolean _not = (!_has);
        if (_not) {
          throw Unsupported.of("a tenant of a collection without tenants");
        }
        result.put(VespaPackage.TENANT, Code.text(tenant.getName()));
      }
      Object _feature_1 = this.feature(statement, "namespace");
      final String namespace = ((String) _feature_1);
      if ((namespace != null)) {
        result.put(VespaPackage.NAMESPACE, Code.text(namespace));
      }
      Object _feature_2 = this.feature(statement, "partition");
      final Partition partition = ((Partition) _feature_2);
      if (((partition != null) && (!doc.has(VespaPackage.PARTITION)))) {
        throw Unsupported.of("a partition of a collection declared without partitions");
      }
      boolean _has_1 = doc.has(VespaPackage.PARTITION);
      if (_has_1) {
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
          _elvis = "_default";
        }
        result.put(VespaPackage.PARTITION, Code.text(_elvis));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Partial updates with assign, add and remove (arrays) and assign of null (remove-fields), each with a test-and-set condition: the
   * document type alone (an update of a missing document then answers 412 and is not counted) or the version (if-version).
   */
  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "recordCountHint");
      this.writeOptions(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()));
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
              final String named = this.schema.name(it.getCollection(), entry.getField());
              String _key = doc.key();
              boolean _equals = Objects.equals(named, _key);
              if (_equals) {
                throw Unsupported.of("updating the primary key; the key is the user part of the document id");
              }
              Literal _value = entry.getValue();
              if ((_value instanceof SparseLit)) {
                throw Unsupported.of((("sparse vector values; " + VespaStatements.PENDING) + " (mapped tensors)"));
              }
              boolean _contains = it.getAppendFields().contains(entry.getField());
              if (_contains) {
                this.arrayField(it.getCollection(), entry.getField());
                append.put(named, this.listOf(entry.getValue()));
              } else {
                boolean _contains_1 = it.getArrayRemoveFields().contains(entry.getField());
                if (_contains_1) {
                  this.arrayField(it.getCollection(), entry.getField());
                  remove.put(named, this.listOf(entry.getValue()));
                } else {
                  set.put(named, Literals.of(entry.getValue()));
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
        final List<String> cleared = CollectionLiterals.<String>newArrayList();
        EList<Field> _removedFields = it.getRemovedFields();
        for (final Field field_1 : _removedFields) {
          {
            final String named = this.schema.name(it.getCollection(), field_1);
            String _key = doc.key();
            boolean _equals = Objects.equals(named, _key);
            if (_equals) {
              throw Unsupported.of("removing the primary key");
            }
            cleared.add(named);
          }
        }
        if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
          List<VespaPackage.Field> _documentFields = doc.documentFields();
          for (final VespaPackage.Field field_2 : _documentFields) {
            if ((((((((!Objects.equals(field_2.name(), doc.key())) && (!field_2.vector())) && (!field_2.name().startsWith("vecdsl_"))) && (!set.has(field_2.name()))) && (!append.has(field_2.name()))) && (!remove.has(field_2.name()))) && (!cleared.contains(field_2.name())))) {
              String _name_1 = field_2.name();
              cleared.add(_name_1);
            }
          }
        }
        if ((((set.entries().isEmpty() && append.entries().isEmpty()) && remove.entries().isEmpty()) && cleared.isEmpty())) {
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
        boolean _isEmpty_3 = cleared.isEmpty();
        boolean _not_3 = (!_isEmpty_3);
        if (_not_3) {
          args.put("clear", Code.texts(cleared));
        }
      }
      this.vectorSizes(args, it.getCollection());
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        args.put("version", Code.num(it.getIfVersion()));
      }
      boolean _isVersioned = this.schema.isVersioned(it.getCollection());
      if (_isVersioned) {
        args.put("versioned", Code.flag(true));
      }
      final VespaFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty_4 = it.getIds().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        boolean _isEmpty_5 = restriction.parts().isEmpty();
        boolean _not_5 = (!_isEmpty_5);
        if (_not_5) {
          final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(restriction.parts(), VespaQueries.Cond.class)));
          final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          args.put("select", this.select(doc, parts, ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
        } else {
          final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function_1)));
        }
      } else {
        boolean _isEmpty_6 = restriction.parts().isEmpty();
        boolean _not_6 = (!_isEmpty_6);
        if (_not_6) {
          args.put("select", this.select(doc, restriction.parts(), null));
        } else {
          RecordSource _source_1 = it.getSource();
          boolean _tripleEquals = (_source_1 == null);
          if (_tripleEquals) {
            throw Unsupported.of("an update without ids or a filter");
          }
        }
      }
      Code.Call _runtime = Code.runtime("update", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
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
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()));
      final VespaFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      boolean _isEmpty = it.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        if (((!restriction.parts().isEmpty()) || ModelUtil.written(it, "deleteLimit"))) {
          final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          args.put("select", this.select(doc, CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(restriction.parts(), VespaQueries.Cond.class))), ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
          boolean _written = ModelUtil.written(it, "deleteLimit");
          if (_written) {
            args.put("limit", Code.num(it.getDeleteLimit()));
          }
        } else {
          final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
            return Literals.of(it_1);
          };
          args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function_1)));
        }
      } else {
        if (((!restriction.parts().isEmpty()) || ModelUtil.written(it, "deleteLimit"))) {
          args.put("select", this.select(doc, restriction.parts(), null));
          boolean _written_1 = ModelUtil.written(it, "deleteLimit");
          if (_written_1) {
            args.put("limit", Code.num(it.getDeleteLimit()));
          }
        } else {
          boolean _isDeleteAll = it.isDeleteAll();
          if (_isDeleteAll) {
            args.put("all", Code.flag(true));
          } else {
            throw Unsupported.of("a delete without ids, filter or \'all\'");
          }
        }
      }
      Code.Call _runtime = Code.runtime("delete", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The query that selects the documents of a filtered update or delete: the keys of the matching documents in key order, page by
   * page (a list of ids restricts it further); the runtime then writes each document by id.
   */
  private Code.Entries select(final VespaPackage.Schema doc, final List<VespaQueries.Cond> parts, final List<Code.Expr> ids) {
    Code.Entries _xblockexpression = null;
    {
      final List<VespaQueries.Cond> all = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(parts, VespaQueries.Cond.class)));
      if ((ids != null)) {
        final VespaPackage.Field keyField = doc.field(doc.key());
        String _name = keyField.name();
        String _kind = keyField.kind();
        Code.Items _items = Code.items(ids);
        VespaQueries.Test _test = new VespaQueries.Test(_name, _kind, "in", _items, null);
        all.add(_test);
      }
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        all.add(_raw);
      }
      final VespaQueries.Params bound = new VespaQueries.Params();
      final String where = VespaQueries.where(all, bound);
      String _key = doc.key();
      final Code.Entries args = VespaSchema.kw("body", VespaQueries.body(VespaQueries.select(doc.name(), where, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_key))), VespaQueries.PAGE, 0, "unranked", null));
      bound.into(args);
      boolean _expiring_1 = doc.expiring();
      if (_expiring_1) {
        args.put("clock", Code.flag(true));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  /**
   * A local JSON or JSON Lines file fed record by record through document/v1, which takes one document per request.
   */
  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; Vespa has no ingestion pipeline that pulls from a source (clients feed the documents)");
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
        throw Unsupported.of((("import from " + scheme) + "; the runtime reads local files (Vespa reads no object stores; clients feed the documents)"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        EImportFormat _format = it.getFormat();
        boolean _equals = Objects.equals(_format, EImportFormat.CSV);
        if (_equals) {
          throw Unsupported.of((("import format csv; " + VespaStatements.PENDING) + " (CSV rows typed by the collection fields)"));
        }
        String _literal = it.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; the runtime reads JSON and JSON Lines files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(it, "parallelism", "async", "batchSize");
      Set<String> _keySet = ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
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
      final Code.Entries args = VespaSchema.kw("location", Code.text(it.getLocation()), "document_type", Code.text(doc.name()), "key", Code.text(doc.key()), 
        "policy", Code.text(_xifexpression_2), "counter", Code.text(_xifexpression_3));
      this.vectorSizes(args, it.getCollection());
      final Code.Entries slices = this.slices(it, it.getCollection());
      boolean _isEmpty = slices.entries().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("slices", slices);
      }
      this.expiry(args, it.getCollection(), null);
      boolean _isVersioned = this.schema.isVersioned(it.getCollection());
      if (_isVersioned) {
        args.put("versioned", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of(VespaStatements.NO_TRANSACTIONS);
  }

  /**
   * A write is acknowledged once it is in the transaction log and visible to queries, so 'wait' holds already.
   */
  private void writeOptions(final EObject statement) {
    this.schema.note(statement, "writeConsistency");
    Set<String> _keySet = ModelUtil.extParams(statement, VespaGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of(("extension parameter " + key));
    }
  }

  /**
   * A statement's filter with its tenant, namespace and partition conditions.
   */
  private VespaFilter filterOf(final EObject statement, final VectorCollection collection, final FilterBlock block) {
    VespaFilter _xblockexpression = null;
    {
      final VespaFilter result = new VespaFilter(this.schema, collection).where(block);
      Object _feature = this.feature(statement, "tenant");
      final Tenant tenant = ((Tenant) _feature);
      if ((tenant != null)) {
        Code.Text _text = Code.text(tenant.getName());
        result.slice(VespaPackage.TENANT, Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text)));
      }
      final Object namespace = this.feature(statement, "namespace");
      if ((namespace instanceof String)) {
        Code.Text _text_1 = Code.text(((String) namespace));
        result.slice(VespaPackage.NAMESPACE, Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text_1)));
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
        final Function1<Partition, Code.Expr> _function = (Partition p) -> {
          Code.Text _text_2 = Code.text(p.getName());
          return ((Code.Expr) _text_2);
        };
        result.slice(VespaPackage.PARTITION, IterableExtensions.<Code.Expr>toList(ListExtensions.<Partition, Code.Expr>map(named, _function)));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Attributes of a deployed schema are in memory, so a load checks that the schema is deployed.
   */
  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      if (((!it.getPartitions().isEmpty()) && (!this.schema.schema(it.getCollection()).has(VespaPackage.PARTITION)))) {
        throw Unsupported.of("partitions of a collection declared without partitions");
      }
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    throw Unsupported.of(VespaStatements.NO_RELEASE);
  }

  /**
   * Writes are durable when acknowledged (transaction log), so a flush checks that the schema is deployed.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      _xblockexpression = this.require(it.getCollection());
    }
    return _xblockexpression;
  }

  /**
   * Proton flushes and compacts its attributes and document store on its own, so a compaction checks that the schema is deployed.
   */
  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet();
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
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()));
      boolean _isIncludeStats = it.isIncludeStats();
      if (_isIncludeStats) {
        args.put("stats", this.countArgs(it.getCollection(), CollectionLiterals.<VespaQueries.Cond>newArrayList()));
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
    throw Unsupported.of(("embedding migration" + VespaSchema.NO_MODELS));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    throw Unsupported.of(VespaStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Restore it) {
    throw Unsupported.of(VespaStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of(VespaStatements.NO_SNAPSHOTS);
  }

  protected List<Code.Stmt> _body(final Clone it) {
    VectorDatabase _sourceDatabase = it.getSourceDatabase();
    boolean _tripleNotEquals = (_sourceDatabase != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(("cloning a deployment; " + VespaStatements.SINGLE_NODE));
    }
    Literal _asOf = it.getAsOf();
    boolean _tripleNotEquals_1 = (_asOf != null);
    if (_tripleNotEquals_1) {
      String _get = VespaStatements.REASONS.get("asOf");
      String _plus = ("the \'as-of\' clause; " + _get);
      throw Unsupported.of(_plus);
    }
    throw Unsupported.of((("cloning a collection; " + VespaStatements.PENDING) + " (a new schema deployed, then every document visited and fed under the new document type)"));
  }

  /**
   * The reindexing of the document type (the deploy API's reindex request), awaited until it succeeds.
   */
  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Set<String> _keySet = ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final VectorCollection collection = this.indexCollection(it.getIndex());
      this.schema.note(it, "concurrently");
      Code.Call _runtime = Code.runtime("reindex", VespaSchema.kw("document_type", Code.text(this.schema.schema(collection).name())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
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
    throw Unsupported.of(VespaStatements.EXTERNAL);
  }

  /**
   * Every document of the type (a delete where the selection is true); a namespace: its documents, selected by a query.
   */
  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        return this.dropSlice(it.getCollection(), VespaPackage.NAMESPACE, it.getNamespace());
      }
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      Code.Call _runtime = Code.runtime("delete", VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()), "all", Code.flag(true)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept owner = it.getScope();
      final Code.Entries args = VespaSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
            if ((owner instanceof Database)) {
              args.put("database", Code.text(((Database) owner).getName()));
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
            if ((!(owner instanceof VectorCollection))) {
              throw Unsupported.of("listing indexes without a collection");
            }
            final VectorCollection collection = ((VectorCollection) owner);
            final List<String> found = CollectionLiterals.<String>newArrayList();
            EList<Index> _indexes = collection.getIndexes();
            for (final Index index : _indexes) {
              String _name_2 = index.getName();
              found.add(_name_2);
            }
            List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
            for (final VectorField field : _vectorFields) {
              VectorIndex _index = field.getIndex();
              boolean _tripleNotEquals = (_index != null);
              if (_tripleNotEquals) {
                String _name_3 = field.getIndex().getName();
                found.add(_name_3);
              }
            }
            args.put("names", Code.texts(found));
            break;
          case ALIASES:
            throw Unsupported.of(VespaStatements.NO_ALIASES);
          case TENANTS:
          case NAMESPACES:
          case PARTITIONS:
            if ((!(owner instanceof VectorCollection))) {
              String _literal = it.getKind().getLiteral();
              String _plus_2 = ("listing " + _literal);
              String _plus_3 = (_plus_2 + " without a collection");
              throw Unsupported.of(_plus_3);
            }
            final VectorCollection collection_1 = ((VectorCollection) owner);
            String _switchResult_1 = null;
            EListKind _kind_1 = it.getKind();
            if (_kind_1 != null) {
              switch (_kind_1) {
                case TENANTS:
                  _switchResult_1 = VespaPackage.TENANT;
                  break;
                case NAMESPACES:
                  _switchResult_1 = VespaPackage.NAMESPACE;
                  break;
                default:
                  _switchResult_1 = VespaPackage.PARTITION;
                  break;
              }
            } else {
              _switchResult_1 = VespaPackage.PARTITION;
            }
            final String attribute = _switchResult_1;
            final VespaPackage.Schema doc = this.schema.schema(collection_1);
            boolean _has = doc.has(attribute);
            boolean _not = (!_has);
            if (_not) {
              String _literal_1 = it.getKind().getLiteral();
              String _plus_4 = ("listing " + _literal_1);
              String _plus_5 = (_plus_4 + " of a collection without ");
              String _literal_2 = it.getKind().getLiteral();
              String _plus_6 = (_plus_5 + _literal_2);
              throw Unsupported.of(_plus_6);
            }
            String _select = VespaQueries.select(doc.name(), "true", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
            String _plus_7 = (_select + " | all(group(");
            String _plus_8 = (_plus_7 + attribute);
            String _plus_9 = (_plus_8 + ") max(");
            String _plus_10 = (_plus_9 + Integer.valueOf(VespaQueries.PAGE));
            String _plus_11 = (_plus_10 + ") each(output(count())))");
            final Code.Entries request = VespaQueries.body(_plus_11, 0, 0, "unranked", null);
            args.put("body", request);
            break;
          case SNAPSHOTS:
            throw Unsupported.of(VespaStatements.NO_SNAPSHOTS);
          default:
            break;
        }
      }
      String _prefix = it.getPrefix();
      boolean _tripleNotEquals_1 = (_prefix != null);
      if (_tripleNotEquals_1) {
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
   * Writes are searchable when acknowledged; the wait covers a deployment still converging (serviceconverge and the query API).
   */
  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "state");
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(this.schema.schema(this.indexCollection(it.getIndex())).name()));
      boolean _written = ModelUtil.written(it, "timeoutSeconds");
      if (_written) {
        args.put("timeout", Code.num(it.getTimeoutSeconds()));
      }
      Code.Call _runtime = Code.runtime("await_ready", args);
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
        throw Unsupported.of(VespaStatements.NO_TEXT);
      }
      this.reject(it, "elementScope", "asOf", "reranker", "queryModel", "mmr", "aggregations", "prefetches", "boost");
      this.combinations(it);
      final String profile = this.rankProfile(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final VespaPackage.Field vector = doc.field(this.schema.name(it.getCollection(), field));
      this.checkMetric(it, field);
      final VespaQueries.Params bound = new VespaQueries.Params();
      final List<VespaQueries.Cond> conditions = this.filterOf(it, it.getCollection(), it.getFilter()).parts();
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus = (_topK + _offsetOf);
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.nearest(it, field, vector.name(), this.windowOf(it, _plus), it.isExact(), it.getParams(), it.getBounds()));
      Iterables.<VespaQueries.Cond>addAll(parts, conditions);
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      final String where = VespaQueries.where(parts, bound);
      VectorCollection _collection = it.getCollection();
      String _select = VespaQueries.select(doc.name(), where, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      int _topK_1 = it.getTopK();
      String _elvis = null;
      if (profile != null) {
        _elvis = profile;
      } else {
        String _nearestProfile = VespaPackage.nearestProfile(vector.name());
        _elvis = _nearestProfile;
      }
      final Code.Entries request = this.shapedBody(it, _collection, _select, _topK_1, _elvis, 
        this.timeoutOf(it.getParams()));
      this.matching(request, it.getParams(), it);
      final Code.Entries args = this.searchArgs(request, bound, doc, it.getCollection(), it.getOutput(), it);
      this.queryVector(args, it.getInput(), it.getCollection(), vector.name());
      if ((profile == null)) {
        this.scoring(args, it, field, vector.name());
      } else {
        this.nativeScore(args, it);
      }
      this.total(args, it, it.getCollection(), conditions);
      this.shape(args, it, it.getCollection(), it.getTopK());
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Clause pairs whose result shapes exclude each other.
   */
  private void combinations(final EObject search) {
    if ((ModelUtil.written(search, "groupBy") && ModelUtil.written(search, "cursor"))) {
      throw Unsupported.of("the \'cursor\' clause with \'group-by\'; a cursor names a hit, and grouped results are pages of groups");
    }
    if ((ModelUtil.written(search, "groupBy") && ModelUtil.written(search, "sortClauses"))) {
      throw Unsupported.of("the \'sort\' clause with \'group-by\'; groups are ordered by their best hit");
    }
  }

  /**
   * The hits a search reads: limit plus offset; with a cursor the first PAGE hits of the result order, in which the runtime finds the
   * cursor record; with group-by the groups times the hits per group.
   */
  private int windowOf(final EObject search, final int window) {
    int _xifexpression = (int) 0;
    boolean _written = ModelUtil.written(search, "cursor");
    if (_written) {
      _xifexpression = VespaQueries.PAGE;
    } else {
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(search, "groupBy");
      if (_written_1) {
        int _groupSizeOf = this.groupSizeOf(search);
        _xifexpression_1 = (window * _groupSizeOf);
      } else {
        _xifexpression_1 = window;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  private int groupSizeOf(final EObject search) {
    Integer _xifexpression = null;
    boolean _written = ModelUtil.written(search, "groupSize");
    if (_written) {
      Object _feature = this.feature(search, "groupSize");
      _xifexpression = ((Integer) _feature);
    } else {
      _xifexpression = Integer.valueOf(1);
    }
    return (_xifexpression).intValue();
  }

  /**
   * The request body of a search: hits and offset of the statement; with a cursor PAGE hits from the start (the runtime pages after
   * the cursor record); with group-by no hits and the grouping expression; explain: ranking.listFeatures.
   */
  private Code.Entries shapedBody(final EObject search, final VectorCollection collection, final String yql, final int limit, final String profile, final Integer timeout) {
    Code.Entries _xblockexpression = null;
    {
      Code.Entries _xifexpression = null;
      boolean _written = ModelUtil.written(search, "cursor");
      if (_written) {
        _xifexpression = VespaQueries.body(yql, VespaQueries.PAGE, 0, profile, timeout);
      } else {
        Code.Entries _xifexpression_1 = null;
        boolean _written_1 = ModelUtil.written(search, "groupBy");
        if (_written_1) {
          Code.Entries _xblockexpression_1 = null;
          {
            final String grouped = this.groupField(search, collection, true);
            int _offsetOf = this.offsetOf(search);
            int _plus = (limit + _offsetOf);
            String _grouping = VespaQueries.grouping(grouped, _plus, this.groupSizeOf(search));
            String _plus_1 = (yql + _grouping);
            _xblockexpression_1 = VespaQueries.body(_plus_1, 0, 0, profile, timeout);
          }
          _xifexpression_1 = _xblockexpression_1;
        } else {
          _xifexpression_1 = VespaQueries.body(yql, limit, this.offsetOf(search), profile, timeout);
        }
        _xifexpression = _xifexpression_1;
      }
      final Code.Entries request = _xifexpression;
      Object _feature = this.feature(search, "explain");
      boolean _equals = Objects.equals(_feature, Boolean.TRUE);
      if (_equals) {
        Code.Expr _get = request.get("ranking");
        ((Code.Entries) _get).put("listFeatures", Code.flag(true));
      }
      _xblockexpression = request;
    }
    return _xblockexpression;
  }

  /**
   * The field a search groups by, a single-value field: in Vespa's grouping the attribute (the exact companion of a full-text field),
   * in the runtime's grouping of fused hybrid hits the summary field.
   */
  private String groupField(final EObject search, final VectorCollection collection, final boolean attribute) {
    String _xblockexpression = null;
    {
      Object _feature = this.feature(search, "groupBy");
      final VespaPackage.Field grouped = new VespaFilter(this.schema, collection).field(((FieldPath) _feature));
      if ((grouped.vector() || grouped.array())) {
        String _name = grouped.name();
        String _plus = ("group-by on " + _name);
        String _plus_1 = (_plus + "; the generator groups by single-value fields (grouping treats array elements as separate values)");
        throw Unsupported.of(_plus_1);
      }
      String _xifexpression = null;
      if ((attribute && grouped.text())) {
        String _name_1 = grouped.name();
        _xifexpression = (VespaPackage.EXACT + _name_1);
      } else {
        _xifexpression = grouped.name();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * Result shapes the runtime applies: the cursor key with limit and offset, sort keys, groups and highlighted fields.
   */
  private void shape(final Code.Entries args, final EObject search, final VectorCollection collection, final int limit) {
    boolean _written = ModelUtil.written(search, "cursor");
    if (_written) {
      Object _feature = this.feature(search, "cursor");
      args.put("after", Code.text(((String) _feature)));
      args.put("limit", Code.num(limit));
      int _offsetOf = this.offsetOf(search);
      boolean _greaterThan = (_offsetOf > 0);
      if (_greaterThan) {
        args.put("offset", Code.num(this.offsetOf(search)));
      }
    }
    Object _elvis = null;
    Object _feature_1 = this.feature(search, "sortClauses");
    if (_feature_1 != null) {
      _elvis = _feature_1;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<SortClause> sorted = ((List<SortClause>) _elvis);
    boolean _isEmpty = sorted.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("order", this.orderOf(collection, sorted));
    }
    boolean _written_1 = ModelUtil.written(search, "groupBy");
    if (_written_1) {
      final Code.Entries groups = VespaSchema.kw("field", Code.text(this.groupField(search, collection, true)));
      int _offsetOf_1 = this.offsetOf(search);
      boolean _greaterThan_1 = (_offsetOf_1 > 0);
      if (_greaterThan_1) {
        groups.put("offset", Code.num(this.offsetOf(search)));
      }
      args.put("groups", groups);
    }
  }

  /**
   * Sort keys as [field, direction]; a key without a field (sort-by relevance) is the score.
   */
  private Code.Expr orderOf(final VectorCollection collection, final List<SortClause> clauses) {
    Code.Items _xblockexpression = null;
    {
      final List<Code.Expr> keys = CollectionLiterals.<Code.Expr>newArrayList();
      for (final SortClause clause : clauses) {
        {
          String _xifexpression = null;
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          if (_equals) {
            _xifexpression = "desc";
          } else {
            _xifexpression = "asc";
          }
          final Code.Text direction = Code.text(_xifexpression);
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            Code.Nil _nil = new Code.Nil();
            Code.Items _items = Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_nil, direction)));
            keys.add(_items);
          } else {
            final VespaPackage.Field sorted = new VespaFilter(this.schema, collection).field(clause.getField());
            if (((sorted.text() || sorted.vector()) || sorted.array())) {
              String _name = sorted.name();
              String _plus = ("sorting by " + _name);
              String _plus_1 = (_plus + "; the generator sorts by single numeric, boolean and exact string attributes");
              throw Unsupported.of(_plus_1);
            }
            Code.Text _text = Code.text(sorted.name());
            Code.Items _items_1 = Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text, direction)));
            keys.add(_items_1);
          }
        }
      }
      _xblockexpression = Code.items(keys);
    }
    return _xblockexpression;
  }

  /**
   * highlight: the document summary with the bolded copies of full-text fields, and the fields the runtime reports.
   */
  private void highlight(final Code.Entries request, final Code.Entries args, final EObject search, final VectorCollection collection) {
    Object _elvis = null;
    Object _feature = this.feature(search, "highlighted");
    if (_feature != null) {
      _elvis = _feature;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<Field> fields = ((List<Field>) _elvis);
    boolean _isEmpty = fields.isEmpty();
    if (_isEmpty) {
      return;
    }
    final VespaPackage.Schema doc = this.schema.schema(collection);
    final List<String> names = CollectionLiterals.<String>newArrayList();
    for (final Field field : fields) {
      {
        final String named = this.schema.name(collection, field);
        if (((!doc.has(named)) || (!doc.field(named).text()))) {
          throw Unsupported.of((("highlight on " + named) + VespaStatements.NO_HIGHLIGHT_FIELD));
        }
        names.add(named);
      }
    }
    Code.Expr _get = request.get("presentation");
    ((Code.Entries) _get).put("summary", Code.text(VespaPackage.HIGHLIGHT_SUMMARY));
    args.put("highlight", Code.texts(names));
  }

  /**
   * The nearestNeighbor operator: targetHits, exact (approximate: false), the candidates beyond targetHits, and a min bound as distance.
   */
  private VespaQueries.Cond nearest(final EObject search, final VectorField field, final String vectorName, final int targetHits, final boolean exact, final SearchParams bound, final ScoreBounds bounds) {
    VespaQueries.Raw _xblockexpression = null;
    {
      Integer explore = null;
      boolean exhaustive = exact;
      if ((bound != null)) {
        boolean _written = ModelUtil.written(bound, "targetRecall");
        if (_written) {
          throw Unsupported.of("target-recall: targetHits and hnsw.exploreAdditionalHits are effort settings, with no declarable recall target");
        }
        boolean _written_1 = ModelUtil.written(bound, "probes");
        if (_written_1) {
          throw Unsupported.of("probes; Vespa\'s vector index is HNSW (no IVF lists to probe)");
        }
        boolean _written_2 = ModelUtil.written(bound, "scanBudget");
        if (_written_2) {
          throw Unsupported.of("scan-budget; Vespa has no setting that bounds the documents a nearestNeighbor query visits");
        }
        boolean _written_3 = ModelUtil.written(bound, "candidatePool");
        if (_written_3) {
          int _candidatePool = bound.getCandidatePool();
          int _minus = (_candidatePool - targetHits);
          explore = Integer.valueOf(Math.max(0, _minus));
        }
        boolean _isExhaustive = bound.isExhaustive();
        if (_isExhaustive) {
          exhaustive = true;
        }
        this.schema.note(bound, "oversampling", "rescore");
      }
      final VectorCollection owner = ModelUtil.owningCollection(field);
      VectorIndex _xifexpression = null;
      if ((owner == null)) {
        _xifexpression = field.getIndex();
      } else {
        _xifexpression = ModelUtil.vectorIndex(owner, field);
      }
      final VectorIndex index = _xifexpression;
      if ((((explore == null) && (index != null)) && ModelUtil.written(index, "searchCandidates"))) {
        int _searchCandidates = index.getSearchCandidates();
        int _minus_1 = (_searchCandidates - targetHits);
        explore = Integer.valueOf(Math.max(0, _minus_1));
      }
      Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(search, VespaGenerator.VENDOR).entrySet();
      for (final Map.Entry<String, Literal> entry : _entrySet) {
        String _key = entry.getKey();
        boolean _equals = Objects.equals(_key, "explore_additional_hits");
        if (_equals) {
          Literal _value = entry.getValue();
          boolean _not = (!(_value instanceof NumberLit));
          if (_not) {
            throw Unsupported.of("explore_additional_hits that is not a number");
          }
          Literal _value_1 = entry.getValue();
          double _value_2 = ((NumberLit) _value_1).getValue();
          explore = Integer.valueOf(((int) _value_2));
        }
      }
      Double _xifexpression_1 = null;
      if ((((bounds != null) && ModelUtil.written(bounds, "min")) && (!(ModelUtil.written(search, "scoreForm") && Objects.equals(this.feature(search, "scoreForm"), EScoreForm.NATIVE))))) {
        _xifexpression_1 = this.distanceAtMost(this.metricOf(field), bounds.getMin());
      }
      final Double threshold = _xifexpression_1;
      Integer _xifexpression_2 = null;
      if (((explore != null) && ((explore).intValue() > 0))) {
        _xifexpression_2 = explore;
      } else {
        _xifexpression_2 = null;
      }
      String _nearest = VespaQueries.nearest(vectorName, targetHits, exhaustive, threshold, _xifexpression_2);
      _xblockexpression = new VespaQueries.Raw(_nearest);
    }
    return _xblockexpression;
  }

  /**
   * The largest distance whose canonical similarity is at least the bound (cosine: the angle, others: the negated bound), or none.
   */
  private Double distanceAtMost(final String metric, final double similarity) {
    Double _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case "cosine":
          Double _xifexpression = null;
          if ((similarity <= (-1))) {
            _xifexpression = null;
          } else {
            double _xifexpression_1 = (double) 0;
            if ((similarity >= 1)) {
              _xifexpression_1 = 0.0;
            } else {
              _xifexpression_1 = Math.acos(similarity);
            }
            _xifexpression = Double.valueOf(_xifexpression_1);
          }
          _switchResult = _xifexpression;
          break;
        case "hamming":
        case "euclidean":
          double _xifexpression_2 = (double) 0;
          if ((similarity > 0)) {
            _xifexpression_2 = 0.0;
          } else {
            _xifexpression_2 = (-similarity);
          }
          _switchResult = Double.valueOf(_xifexpression_2);
          break;
        case "dot":
          _switchResult = Double.valueOf((-similarity));
          break;
        default:
          _switchResult = null;
          break;
      }
    } else {
      _switchResult = null;
    }
    return _switchResult;
  }

  private String metricOf(final VectorField field) {
    String _xblockexpression = null;
    {
      final VectorCollection collection = ModelUtil.owningCollection(field);
      VectorIndex _xifexpression = null;
      if ((collection == null)) {
        _xifexpression = field.getIndex();
      } else {
        _xifexpression = ModelUtil.vectorIndex(collection, field);
      }
      _xblockexpression = this.schema.metric(field, _xifexpression);
    }
    return _xblockexpression;
  }

  /**
   * filter-strategy: pre_filter never post-filters (postFilterThreshold 1), post_filter always does (0); extension keys by name.
   */
  private void matching(final Code.Entries request, final SearchParams bound, final EObject search) {
    final Code.Entries settings = Code.entries();
    if (((bound != null) && ModelUtil.written(bound, "filterStrategy"))) {
      String _literal = bound.getFilterStrategy().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "pre_filter":
            settings.put("postFilterThreshold", Code.num(1));
            break;
          case "post_filter":
            settings.put("postFilterThreshold", Code.num(0));
            break;
          case "iterative":
            throw Unsupported.of("filter-strategy iterative; Vespa pre-filters or post-filters a nearestNeighbor query (with targetHits adjusted for the filter), with no iterative scan");
          default:
            break;
        }
      } else {
      }
    }
    if ((bound != null)) {
      Set<String> _keySet = ModelUtil.extParams(bound, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of((("extension parameter " + key) + " in search parameters"));
      }
    }
    Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(search, VespaGenerator.VENDOR).entrySet();
    for (final Map.Entry<String, Literal> entry : _entrySet) {
      {
        final String setting = VespaStatements.SEARCH_SETTINGS.get(entry.getKey().toLowerCase());
        if ((setting == null)) {
          String _key = entry.getKey();
          String _plus = ("extension parameter " + _key);
          String _plus_1 = (_plus + "; Vespa query settings here are ");
          String _join = IterableExtensions.join(VespaStatements.SEARCH_SETTINGS.keySet(), ", ");
          String _plus_2 = (_plus_1 + _join);
          throw Unsupported.of(_plus_2);
        }
        Literal _value = entry.getValue();
        boolean _not = (!(_value instanceof NumberLit));
        if (_not) {
          String _key_1 = entry.getKey();
          String _plus_3 = ("extension parameter " + _key_1);
          String _plus_4 = (_plus_3 + " with a value that is not a number");
          throw Unsupported.of(_plus_4);
        }
        boolean _notEquals = (!Objects.equals(setting, "hnsw.exploreAdditionalHits"));
        if (_notEquals) {
          Literal _value_1 = entry.getValue();
          settings.put(setting, Code.num(((NumberLit) _value_1).getValue()));
        }
      }
    }
    boolean _isEmpty = settings.entries().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      Code.Expr _get = request.get("ranking");
      ((Code.Entries) _get).put("matching", settings);
    }
  }

  private Integer timeoutOf(final SearchParams bound) {
    Integer _xifexpression = null;
    if (((bound != null) && ModelUtil.written(bound, "timeoutMillis"))) {
      _xifexpression = Integer.valueOf(bound.getTimeoutMillis());
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  private void checkMetric(final EObject search, final VectorField field) {
    if ((ModelUtil.written(search, "metric") && (!Objects.equals(((EMetric) this.feature(search, "metric")).getLiteral(), this.metricOf(field))))) {
      throw Unsupported.of("a metric other than the field\'s; the distance-metric of a tensor field is part of its schema (changing it needs a restart)");
    }
  }

  /**
   * Canonical similarity from the distance match feature, or the native relevance (closeness), and score bounds.
   */
  private void scoring(final Code.Entries args, final EObject search, final VectorField field, final String vectorName) {
    args.put("metric", Code.text(this.metricOf(field)));
    args.put("feature", Code.text(VespaPackage.distanceFeature(vectorName)));
    if ((ModelUtil.written(search, "scoreForm") && Objects.equals(this.feature(search, "scoreForm"), EScoreForm.NATIVE))) {
      args.put("native", Code.flag(true));
    }
    Object _feature = this.feature(search, "bounds");
    this.scoreBounds(args, ((ScoreBounds) _feature));
  }

  private void nativeScore(final Code.Entries args, final EObject search) {
    if ((ModelUtil.written(search, "scoreForm") && (!Objects.equals(this.feature(search, "scoreForm"), EScoreForm.NATIVE)))) {
      String _name = search.eClass().getName();
      String _plus = ("score similarity on " + _name);
      String _plus_1 = (_plus + ", whose declared rank profile scores natively");
      this.notes.add(_plus_1);
    }
    Object _feature = this.feature(search, "bounds");
    this.scoreBounds(args, ((ScoreBounds) _feature));
  }

  private void scoreBounds(final Code.Entries args, final ScoreBounds limits) {
    if (((limits != null) && ModelUtil.written(limits, "min"))) {
      args.put("low", Code.num(limits.getMin()));
    }
    if (((limits != null) && ModelUtil.written(limits, "max"))) {
      args.put("high", Code.num(limits.getMax()));
    }
  }

  /**
   * with-total: the number of documents the filter matches, counted by its own query.
   */
  private void total(final Code.Entries args, final EObject search, final VectorCollection collection, final List<VespaQueries.Cond> conditions) {
    boolean _written = ModelUtil.written(search, "withTotal");
    boolean _not = (!_written);
    if (_not) {
      return;
    }
    args.put("total", this.countArgs(collection, conditions));
  }

  private Code.Entries countArgs(final VectorCollection collection, final List<VespaQueries.Cond> conditions) {
    VespaQueries.Params _params = new VespaQueries.Params();
    return this.countArgs(collection, conditions, _params);
  }

  /**
   * The count query of the conditions; counted already holds the values of conditions bound before (the text of a keyword search).
   */
  private Code.Entries countArgs(final VectorCollection collection, final List<VespaQueries.Cond> conditions, final VespaQueries.Params counted) {
    Code.Entries _xblockexpression = null;
    {
      final VespaPackage.Schema doc = this.schema.schema(collection);
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(conditions, VespaQueries.Cond.class)));
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      final String where = VespaQueries.where(parts, counted);
      final Code.Entries args = VespaSchema.kw("body", VespaQueries.body(VespaQueries.select(doc.name(), where, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList())), 0, 0, "unranked", null));
      counted.into(args);
      boolean _expiring_1 = doc.expiring();
      if (_expiring_1) {
        args.put("clock", Code.flag(true));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private Code.Entries searchArgs(final Code.Entries request, final VespaQueries.Params bound, final VespaPackage.Schema doc, final VectorCollection collection, final OutputSpec output, final EObject search) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = VespaSchema.kw("body", request);
      bound.into(args);
      boolean _expiring = doc.expiring();
      if (_expiring) {
        args.put("clock", Code.flag(true));
      }
      args.put("key", Code.text(doc.key()));
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
   * The rank profile an extension vespa block names (RankProfileUse), or none.
   */
  private String rankProfile(final EObject search) {
    String _xblockexpression = null;
    {
      String chosen = null;
      final EStructuralFeature slot = search.eClass().getEStructuralFeature("extensions");
      if ((slot == null)) {
        return null;
      }
      Object _eGet = search.eGet(slot);
      Iterable<VendorExtension> _filter = Iterables.<VendorExtension>filter(((List<?>) _eGet), VendorExtension.class);
      for (final VendorExtension block : _filter) {
        String _vendor = block.getVendor();
        boolean _equals = Objects.equals(_vendor, VespaGenerator.VENDOR);
        if (_equals) {
          EList<ExtensionItem> _items = block.getItems();
          for (final ExtensionItem item : _items) {
            boolean _matched = false;
            if (item instanceof RankProfileUse) {
              _matched=true;
              chosen = this.useProfile(search, ((RankProfileUse)item).getProfile());
            }
            if (!_matched) {
              if (item instanceof ExtParam) {
                _matched=true;
              }
            }
            if (!_matched) {
              String _name = item.eClass().getName();
              String _plus = (_name + " on a query; a Vespa query extension names a rank profile (rank) or query settings");
              throw Unsupported.of(_plus);
            }
          }
        }
      }
      _xblockexpression = chosen;
    }
    return _xblockexpression;
  }

  private String useProfile(final EObject search, final PhasedRankProfile profile) {
    String _xblockexpression = null;
    {
      final VectorCollection owner = EcoreUtil2.<VectorCollection>getContainerOfType(profile, VectorCollection.class);
      Object _feature = this.feature(search, "collection");
      final VectorCollection collection = ((VectorCollection) _feature);
      if ((owner != collection)) {
        String _name = profile.getName();
        String _plus = ("rank profile " + _name);
        String _plus_1 = (_plus + ", which collection ");
        String _name_1 = collection.getName();
        String _plus_2 = (_plus_1 + _name_1);
        String _plus_3 = (_plus_2 + " does not declare");
        throw Unsupported.of(_plus_3);
      }
      if ((((search instanceof VectorSearch) || (search instanceof RecommendSearch)) && (!VespaSchema.takesQueryVector(profile)))) {
        String _name_2 = profile.getName();
        String _plus_4 = ("rank profile " + _name_2);
        String _plus_5 = (_plus_4 + " on a vector search; it declares no input ");
        String _plus_6 = (_plus_5 + VespaPackage.QUERY_TENSOR);
        String _plus_7 = (_plus_6 + ", which carries the query vector of nearestNeighbor");
        throw Unsupported.of(_plus_7);
      }
      _xblockexpression = profile.getName();
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "reranker", "minShouldMatch", "mmr", "aggregations", "boost", "fuzziness");
      this.combinations(it);
      final String profile = this.rankProfile(it);
      SearchParams _params = it.getParams();
      boolean _tripleNotEquals = (_params != null);
      if (_tripleNotEquals) {
        this.schema.note(it.getParams(), "candidatePool", "probes", "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
      }
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final VespaQueries.Params bound = new VespaQueries.Params();
      final Code.Entries weights = Code.entries();
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.keywordQuery(it.getCollection(), it.getSearchFields(), it.getInput(), this.textType(it), this.operatorOf(it), this.slopOf(it), bound, weights));
      final List<VespaQueries.Cond> conditions = this.filterOf(it, it.getCollection(), it.getFilter()).parts();
      Iterables.<VespaQueries.Cond>addAll(parts, conditions);
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      VectorCollection _collection = it.getCollection();
      String _select = VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      int _topK = it.getTopK();
      String _elvis = null;
      if (profile != null) {
        _elvis = profile;
      } else {
        _elvis = VespaPackage.TEXT_PROFILE;
      }
      final Code.Entries request = this.shapedBody(it, _collection, _select, _topK, _elvis, this.timeoutOf(it.getParams()));
      if (((profile == null) && (!weights.entries().isEmpty()))) {
        request.put("input", weights);
      }
      final Code.Entries args = this.searchArgs(request, bound, doc, it.getCollection(), it.getOutput(), it);
      this.scoreBounds(args, it.getBounds());
      boolean _written = ModelUtil.written(it, "withTotal");
      if (_written) {
        final VespaQueries.Params totalBound = new VespaQueries.Params();
        final List<VespaQueries.Cond> counted = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.keywordQuery(it.getCollection(), it.getSearchFields(), it.getInput(), this.textType(it), this.operatorOf(it), this.slopOf(it), totalBound, Code.entries()));
        Iterables.<VespaQueries.Cond>addAll(counted, conditions);
        args.put("total", this.countArgs(it.getCollection(), counted, totalBound));
      }
      this.highlight(request, args, it, it.getCollection());
      this.shape(args, it, it.getCollection(), it.getTopK());
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private Integer slopOf(final EObject search) {
    Integer _xifexpression = null;
    boolean _written = ModelUtil.written(search, "slop");
    if (_written) {
      Object _feature = this.feature(search, "slop");
      _xifexpression = ((Integer) _feature);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  private String textType(final EObject search) {
    String _xifexpression = null;
    boolean _written = ModelUtil.written(search, "textQueryType");
    if (_written) {
      Object _feature = this.feature(search, "textQueryType");
      _xifexpression = ((ETextQueryType) _feature).getLiteral();
    } else {
      _xifexpression = "match";
    }
    return _xifexpression;
  }

  private String operatorOf(final EObject search) {
    String _xifexpression = null;
    boolean _written = ModelUtil.written(search, "matchOperator");
    if (_written) {
      Object _feature = this.feature(search, "matchOperator");
      _xifexpression = ((EMatchOperator) _feature).getLiteral();
    } else {
      _xifexpression = "and";
    }
    return _xifexpression;
  }

  /**
   * The text part of a keyword query: userInput over each listed full-text field (every one without a list), joined with or, and the
   * field boosts as the rank profile's weight inputs query(vecdsl_w_<field>) (a field not listed weighs 0).
   */
  private VespaQueries.Cond keywordQuery(final VectorCollection collection, final List<SearchFieldRef> refs, final QueryInput input, final String type, final String operator, final Integer slop, final VespaQueries.Params bound, final Code.Entries weights) {
    VespaQueries.Cond _xblockexpression = null;
    {
      final VespaPackage.Schema doc = this.schema.schema(collection);
      final List<Pair<String, Double>> fields = CollectionLiterals.<Pair<String, Double>>newArrayList();
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        List<VespaPackage.Field> _texts = doc.texts();
        for (final VespaPackage.Field field : _texts) {
          String _name = field.name();
          Pair<String, Double> _mappedTo = Pair.<String, Double>of(_name, Double.valueOf(1.0));
          fields.add(_mappedTo);
        }
      } else {
        for (final SearchFieldRef ref : refs) {
          {
            final String named = this.schema.name(collection, ref.getField());
            if (((!doc.has(named)) || (!doc.field(named).text()))) {
              throw Unsupported.of((("keyword search on " + named) + ", which is not a full-text field"));
            }
            double _xifexpression = (double) 0;
            boolean _written = ModelUtil.written(ref, "boost");
            if (_written) {
              _xifexpression = ref.getBoost();
            } else {
              _xifexpression = 1.0;
            }
            Pair<String, Double> _mappedTo_1 = Pair.<String, Double>of(named, Double.valueOf(_xifexpression));
            fields.add(_mappedTo_1);
          }
        }
      }
      boolean _isEmpty_1 = fields.isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("keyword search in a collection without full-text fields");
      }
      String _switchResult = null;
      if (type != null) {
        switch (type) {
          case "match":
            _switchResult = operator;
            break;
          case "phrase":
            _switchResult = "phrase";
            break;
          case "query_string":
            throw Unsupported.of("text-query query_string; the generator never parses a value as query syntax (userInput with grammar.syntax none), so user query strings are not parsed");
          case "regexp":
            throw Unsupported.of("text-query regexp; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)");
          case "fuzzy":
            throw Unsupported.of(("text-query fuzzy; " + VespaFilter.NO_FUZZY));
          default:
            throw Unsupported.of((((("text-query " + type) + "; ") + VespaStatements.PENDING) + " (the prefix and substring term annotations)"));
        }
      } else {
        throw Unsupported.of((((("text-query " + type) + "; ") + VespaStatements.PENDING) + " (the prefix and substring term annotations)"));
      }
      final String plainComposite = _switchResult;
      String _xifexpression = null;
      if ((slop == null)) {
        _xifexpression = plainComposite;
      } else {
        String _switchResult_1 = null;
        if (plainComposite != null) {
          switch (plainComposite) {
            case "and":
              _switchResult_1 = "near";
              break;
            case "phrase":
              _switchResult_1 = "oNear";
              break;
            default:
              throw Unsupported.of((("slop with operator " + plainComposite) + "; near and onear bound the distance of terms that must all match"));
          }
        } else {
          throw Unsupported.of((("slop with operator " + plainComposite) + "; near and onear bound the distance of terms that must all match"));
        }
        _xifexpression = _switchResult_1;
      }
      final String composite = _xifexpression;
      final Code.Expr userText = this.keywordText(input);
      Integer _xifexpression_1 = null;
      if ((slop == null)) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = Integer.valueOf(((slop).intValue() + 1));
      }
      final Integer distance = _xifexpression_1;
      final Function1<Pair<String, Double>, VespaQueries.Cond> _function = (Pair<String, Double> f) -> {
        String _userInput = VespaQueries.userInput(f.getKey(), composite, userText, bound, distance);
        VespaQueries.Raw _raw = new VespaQueries.Raw(_userInput);
        return ((VespaQueries.Cond) _raw);
      };
      final List<VespaQueries.Cond> parts = IterableExtensions.<VespaQueries.Cond>toList(ListExtensions.<Pair<String, Double>, VespaQueries.Cond>map(fields, _function));
      List<VespaPackage.Field> _texts_1 = doc.texts();
      for (final VespaPackage.Field field_1 : _texts_1) {
        {
          Double _elvis = null;
          final Function1<Pair<String, Double>, Boolean> _function_1 = (Pair<String, Double> it) -> {
            String _key = it.getKey();
            String _name_1 = field_1.name();
            return Boolean.valueOf(Objects.equals(_key, _name_1));
          };
          Pair<String, Double> _findFirst = IterableExtensions.<Pair<String, Double>>findFirst(fields, _function_1);
          Double _value = null;
          if (_findFirst!=null) {
            _value=_findFirst.getValue();
          }
          if (_value != null) {
            _elvis = _value;
          } else {
            _elvis = Double.valueOf(0.0);
          }
          final Double weight = _elvis;
          if (((weight).doubleValue() != 1.0)) {
            String _name_1 = field_1.name();
            String _plus = (("query(" + VespaPackage.WEIGHT) + _name_1);
            String _plus_1 = (_plus + ")");
            weights.put(_plus_1, Code.num((weight).doubleValue()));
          }
        }
      }
      VespaQueries.Cond _xifexpression_2 = null;
      int _size = parts.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        _xifexpression_2 = IterableExtensions.<VespaQueries.Cond>head(parts);
      } else {
        _xifexpression_2 = new VespaQueries.Or(parts);
      }
      _xblockexpression = _xifexpression_2;
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
   * Hybrid search: each leg is its own query and the runtime fuses the legs as vecDSL defines rrf, weighted and relative_score. Vespa's
   * global-phase reciprocal_rank_fusion ranks the union of the first-phase hits by every score, including documents a leg did not
   * retrieve, which is not the per-leg rank of vecDSL's fusion.
   */
  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "reranker", "mmr", "aggregations", "boost");
      this.combinations(it);
      String _rankProfile = this.rankProfile(it);
      boolean _tripleNotEquals = (_rankProfile != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a declared rank profile on a hybrid search; the runtime fuses the legs\' own scores");
      }
      if ((ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE))) {
        this.notes.add("score native on HybridSearch, whose fused score has one form");
      }
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
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
      int _plus = (_topK + _offsetOf);
      final int window = this.windowOf(it, _plus);
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "fusionConstant");
      if (_written_1) {
        _xifexpression_1 = it.getFusionConstant();
      } else {
        _xifexpression_1 = 60;
      }
      final int k = _xifexpression_1;
      final List<Code.Expr> legArgs = CollectionLiterals.<Code.Expr>newArrayList();
      final List<VespaQueries.Cond> legMatches = CollectionLiterals.<VespaQueries.Cond>newArrayList();
      final VespaQueries.Params counted = new VespaQueries.Params();
      Object _elvis = null;
      Object _feature = this.feature(it, "highlighted");
      if (_feature != null) {
        _elvis = _feature;
      } else {
        ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
        _elvis = _newArrayList;
      }
      final List<Field> marked = ((List<Field>) _elvis);
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          Set<String> _keySet = ModelUtil.extParams(leg, VespaGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of((("extension parameter " + key) + " on a hybrid leg"));
          }
          Integer _xifexpression_2 = null;
          boolean _written_2 = ModelUtil.written(leg, "legLimit");
          if (_written_2) {
            Object _feature_1 = this.feature(leg, "legLimit");
            _xifexpression_2 = ((Integer) _feature_1);
          } else {
            _xifexpression_2 = Integer.valueOf(window);
          }
          final Integer limit = _xifexpression_2;
          final VespaQueries.Params bound = new VespaQueries.Params();
          Object _feature_2 = this.feature(leg, "filter");
          final List<VespaQueries.Cond> conditions = this.filterOf(it, it.getCollection(), this.legFilter(it.getFilter(), ((FilterBlock) _feature_2))).parts();
          Code.Entries _switchResult = null;
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            Code.Entries _xblockexpression_1 = null;
            {
              ScoreBounds _bounds = ((VectorLeg)leg).getBounds();
              boolean _tripleNotEquals_1 = (_bounds != null);
              if (_tripleNotEquals_1) {
                throw Unsupported.of((("bounds on a hybrid leg; " + VespaStatements.PENDING) + " (the leg score filtered before fusion)"));
              }
              final VectorField field = this.vectorFieldOf(it.getCollection(), ((VectorLeg)leg).getVectorField());
              final VespaPackage.Field vector = doc.field(this.schema.name(it.getCollection(), field));
              this.checkMetric(leg, field);
              final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.nearest(it, field, vector.name(), (limit).intValue(), ((VectorLeg)leg).isExact(), ((VectorLeg)leg).getParams(), null));
              Iterables.<VespaQueries.Cond>addAll(parts, conditions);
              boolean _expiring = doc.expiring();
              if (_expiring) {
                VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
                parts.add(_raw);
              }
              final Code.Entries request = VespaQueries.body(VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList())), (limit).intValue(), 0, 
                VespaPackage.nearestProfile(vector.name()), this.timeoutOf(it.getParams()));
              boolean _isExplain = it.isExplain();
              if (_isExplain) {
                Code.Expr _get = request.get("ranking");
                ((Code.Entries) _get).put("listFeatures", Code.flag(true));
              }
              this.matching(request, ((VectorLeg)leg).getParams(), leg);
              VespaQueries.Cond _xifexpression_3 = null;
              boolean _isEmpty = conditions.isEmpty();
              if (_isEmpty) {
                VespaQueries.Raw _raw_1 = new VespaQueries.Raw("true");
                _xifexpression_3 = ((VespaQueries.Cond) _raw_1);
              } else {
                VespaQueries.Cond _xifexpression_4 = null;
                int _size = conditions.size();
                boolean _equals = (_size == 1);
                if (_equals) {
                  _xifexpression_4 = IterableExtensions.<VespaQueries.Cond>head(conditions);
                } else {
                  _xifexpression_4 = new VespaQueries.And(conditions);
                }
                _xifexpression_3 = _xifexpression_4;
              }
              legMatches.add(_xifexpression_3);
              final Code.Entries result = VespaSchema.kw("body", request);
              bound.into(result);
              boolean _expiring_1 = doc.expiring();
              if (_expiring_1) {
                result.put("clock", Code.flag(true));
              }
              QueryInput _elvis_1 = null;
              QueryInput _input = ((VectorLeg)leg).getInput();
              if (_input != null) {
                _elvis_1 = _input;
              } else {
                QueryInput _input_1 = it.getInput();
                _elvis_1 = _input_1;
              }
              this.queryVector(result, _elvis_1, it.getCollection(), vector.name());
              result.put("metric", Code.text(this.metricOf(field)));
              result.put("feature", Code.text(VespaPackage.distanceFeature(vector.name())));
              _xblockexpression_1 = result;
            }
            _switchResult = _xblockexpression_1;
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              Code.Entries _xblockexpression_1 = null;
              {
                boolean _written_3 = ModelUtil.written(leg, "minShouldMatch");
                if (_written_3) {
                  String _get = VespaStatements.REASONS.get("minShouldMatch");
                  String _plus_1 = ("the \'min-should-match\' clause; " + _get);
                  throw Unsupported.of(_plus_1);
                }
                final Code.Entries weightInputs = Code.entries();
                VectorCollection _collection = it.getCollection();
                EList<SearchFieldRef> _searchFields = ((KeywordLeg)leg).getSearchFields();
                QueryInput _elvis_1 = null;
                QueryInput _input = ((KeywordLeg)leg).getInput();
                if (_input != null) {
                  _elvis_1 = _input;
                } else {
                  QueryInput _input_1 = it.getInput();
                  _elvis_1 = _input_1;
                }
                final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.keywordQuery(_collection, _searchFields, _elvis_1, this.textType(leg), this.operatorOf(leg), null, bound, weightInputs));
                Iterables.<VespaQueries.Cond>addAll(parts, conditions);
                boolean _expiring = doc.expiring();
                if (_expiring) {
                  VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
                  parts.add(_raw);
                }
                final Code.Entries request = VespaQueries.body(VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList())), (limit).intValue(), 0, 
                  VespaPackage.TEXT_PROFILE, this.timeoutOf(it.getParams()));
                boolean _isEmpty = weightInputs.entries().isEmpty();
                boolean _not = (!_isEmpty);
                if (_not) {
                  request.put("input", weightInputs);
                }
                boolean _isExplain = it.isExplain();
                if (_isExplain) {
                  Code.Expr _get_1 = request.get("ranking");
                  ((Code.Entries) _get_1).put("listFeatures", Code.flag(true));
                }
                boolean _isEmpty_1 = marked.isEmpty();
                boolean _not_1 = (!_isEmpty_1);
                if (_not_1) {
                  Code.Expr _get_2 = request.get("presentation");
                  ((Code.Entries) _get_2).put("summary", Code.text(VespaPackage.HIGHLIGHT_SUMMARY));
                }
                boolean _written_4 = ModelUtil.written(it, "withTotal");
                if (_written_4) {
                  VectorCollection _collection_1 = it.getCollection();
                  EList<SearchFieldRef> _searchFields_1 = ((KeywordLeg)leg).getSearchFields();
                  QueryInput _elvis_2 = null;
                  QueryInput _input_2 = ((KeywordLeg)leg).getInput();
                  if (_input_2 != null) {
                    _elvis_2 = _input_2;
                  } else {
                    QueryInput _input_3 = it.getInput();
                    _elvis_2 = _input_3;
                  }
                  final List<VespaQueries.Cond> matched = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.keywordQuery(_collection_1, _searchFields_1, _elvis_2, this.textType(leg), this.operatorOf(leg), null, counted, Code.entries()));
                  Iterables.<VespaQueries.Cond>addAll(matched, conditions);
                  VespaQueries.Cond _xifexpression_3 = null;
                  int _size = matched.size();
                  boolean _equals = (_size == 1);
                  if (_equals) {
                    _xifexpression_3 = IterableExtensions.<VespaQueries.Cond>head(matched);
                  } else {
                    VespaQueries.And _and = new VespaQueries.And(matched);
                    _xifexpression_3 = ((VespaQueries.Cond) _and);
                  }
                  legMatches.add(_xifexpression_3);
                }
                final Code.Entries result = VespaSchema.kw("body", request);
                bound.into(result);
                boolean _expiring_1 = doc.expiring();
                if (_expiring_1) {
                  result.put("clock", Code.flag(true));
                }
                _xblockexpression_1 = result;
              }
              _switchResult = _xblockexpression_1;
            }
          }
          legArgs.add(_switchResult);
        }
      }
      final Code.Entries args = VespaSchema.kw("legs", Code.items(legArgs), "fusion", Code.text(fusionKind), "key", Code.text(doc.key()));
      final Pair<List<String>, List<String>> shape = this.projection(it.getCollection(), it.getOutput(), this.excludedOf(it), it.isWithVectors());
      args.put("output", Code.texts(shape.getKey()));
      boolean _isEmpty = shape.getValue().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(shape.getValue()));
      }
      args.put("limit", Code.num(it.getTopK()));
      int _offsetOf_1 = this.offsetOf(it);
      boolean _greaterThan = (_offsetOf_1 > 0);
      if (_greaterThan) {
        args.put("offset", Code.num(this.offsetOf(it)));
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
      this.scoreBounds(args, it.getBounds());
      boolean _written_2 = ModelUtil.written(it, "cursor");
      if (_written_2) {
        args.put("after", Code.text(it.getCursor()));
      }
      boolean _isEmpty_1 = it.getSortClauses().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("order", this.orderOf(it.getCollection(), it.getSortClauses()));
      }
      boolean _written_3 = ModelUtil.written(it, "groupBy");
      if (_written_3) {
        args.put("groups", VespaSchema.kw("field", Code.text(this.groupField(it, it.getCollection(), false)), "size", Code.num(this.groupSizeOf(it))));
      }
      boolean _isEmpty_2 = marked.isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        final List<String> names = CollectionLiterals.<String>newArrayList();
        for (final Field field : marked) {
          {
            final String named = this.schema.name(it.getCollection(), field);
            if (((!doc.has(named)) || (!doc.field(named).text()))) {
              throw Unsupported.of((("highlight on " + named) + VespaStatements.NO_HIGHLIGHT_FIELD));
            }
            names.add(named);
          }
        }
        args.put("highlight", Code.texts(names));
      }
      boolean _isExplain = it.isExplain();
      if (_isExplain) {
        args.put("explain", Code.flag(true));
      }
      boolean _written_4 = ModelUtil.written(it, "withTotal");
      if (_written_4) {
        VespaQueries.Cond _unionOf = this.unionOf(legMatches);
        args.put("total", this.countArgs(it.getCollection(), Collections.<VespaQueries.Cond>unmodifiableList(CollectionLiterals.<VespaQueries.Cond>newArrayList(_unionOf)), counted));
      }
      Code.Call _runtime = Code.runtime("hybrid", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The documents some leg matches: a vector leg matches its filter (nearestNeighbor ranks every document), a keyword leg its text.
   */
  private VespaQueries.Cond unionOf(final List<VespaQueries.Cond> legMatches) {
    VespaQueries.Cond _xifexpression = null;
    final Function1<VespaQueries.Cond, Boolean> _function = (VespaQueries.Cond match) -> {
      return Boolean.valueOf(((match instanceof VespaQueries.Raw) && Objects.equals(((VespaQueries.Raw) match).yql(), "true")));
    };
    boolean _exists = IterableExtensions.<VespaQueries.Cond>exists(legMatches, _function);
    if (_exists) {
      _xifexpression = new VespaQueries.Raw("true");
    } else {
      VespaQueries.Cond _xifexpression_1 = null;
      int _size = legMatches.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        _xifexpression_1 = IterableExtensions.<VespaQueries.Cond>head(legMatches);
      } else {
        _xifexpression_1 = new VespaQueries.Or(legMatches);
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
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
    throw Unsupported.of((("a filter on both the hybrid search and a leg; " + VespaStatements.PENDING) + " (the conjunction of both)"));
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
   * The average-vector strategy (2 avg(positive) - avg(negative)) computed in the runtime; the example documents are excluded.
   */
  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getHighlighted().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(VespaStatements.NO_TEXT);
      }
      this.reject(it, "asOf", "reranker", "mmr", "aggregations", "boost");
      this.combinations(it);
      final String profile = this.rankProfile(it);
      boolean _isEmpty_1 = it.getPositives().isEmpty();
      if (_isEmpty_1) {
        throw Unsupported.of("recommend without positive examples");
      }
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final VectorCollection examples = _elvis;
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final VespaPackage.Field vector = doc.field(this.schema.name(it.getCollection(), field));
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
      final VespaFilter restriction = this.filterOf(it, it.getCollection(), it.getFilter());
      final List<Code.Expr> exampleIds = CollectionLiterals.<Code.Expr>newArrayList();
      Iterables.<Code.Expr>addAll(exampleIds, positiveIds);
      Iterables.<Code.Expr>addAll(exampleIds, negativeIds);
      if (((examples == it.getCollection()) && (!exampleIds.isEmpty()))) {
        final VespaPackage.Field keyField = doc.field(doc.key());
        String _name = keyField.name();
        String _kind = keyField.kind();
        Code.Items _items = Code.items(exampleIds);
        VespaQueries.Test _test = new VespaQueries.Test(_name, _kind, "in", _items, null);
        VespaQueries.Not _not_1 = new VespaQueries.Not(_test);
        restriction.add(_not_1);
      }
      final VespaQueries.Params bound = new VespaQueries.Params();
      int _topK = it.getTopK();
      int _offsetOf = this.offsetOf(it);
      int _plus = (_topK + _offsetOf);
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(this.nearest(it, field, vector.name(), this.windowOf(it, _plus), false, it.getParams(), it.getBounds()));
      List<VespaQueries.Cond> _parts = restriction.parts();
      Iterables.<VespaQueries.Cond>addAll(parts, _parts);
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      VectorCollection _collection_1 = it.getCollection();
      String _select = VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      int _topK_1 = it.getTopK();
      String _elvis_1 = null;
      if (profile != null) {
        _elvis_1 = profile;
      } else {
        String _nearestProfile = VespaPackage.nearestProfile(vector.name());
        _elvis_1 = _nearestProfile;
      }
      final Code.Entries request = this.shapedBody(it, _collection_1, _select, _topK_1, _elvis_1, this.timeoutOf(it.getParams()));
      this.matching(request, it.getParams(), it);
      final Code.Entries args = this.searchArgs(request, bound, doc, it.getCollection(), it.getOutput(), it);
      final Code.Entries lookup = VespaSchema.kw("document_type", Code.text(this.schema.schema(examples).name()), "field", Code.text(this.schema.name(examples, field)));
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
      args.put("lookup", lookup);
      if ((profile == null)) {
        this.scoring(args, it, field, vector.name());
      } else {
        this.nativeScore(args, it);
      }
      this.total(args, it, it.getCollection(), restriction.parts());
      this.shape(args, it, it.getCollection(), it.getTopK());
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  private boolean isVector(final Literal example) {
    return (((example instanceof ListLit) || (example instanceof SparseLit)) || ((example instanceof ParamRef) && Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("vector", "sparse_vector")).contains(((ParamRef) example).getParam().getType().getLiteral())));
  }

  /**
   * document/v1 GET of each id; a slice or an expiry is checked on the returned document.
   */
  protected List<Code.Stmt> _body(final GetById it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final Pair<List<String>, List<String>> shape = this.projection(it.getCollection(), it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries args = VespaSchema.kw("document_type", Code.text(doc.name()), "key", Code.text(doc.key()), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), "output", Code.texts(shape.getKey()));
      boolean _isEmpty = shape.getValue().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("vectors", Code.texts(shape.getValue()));
      }
      final Code.Entries restriction = Code.entries();
      final Tenant tenant = it.getTenant();
      if ((tenant != null)) {
        Code.Text _text = Code.text(tenant.getName());
        restriction.put(VespaPackage.TENANT, Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text))));
      }
      String _namespace = it.getNamespace();
      boolean _tripleNotEquals = (_namespace != null);
      if (_tripleNotEquals) {
        Code.Text _text_1 = Code.text(it.getNamespace());
        restriction.put(VespaPackage.NAMESPACE, Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_text_1))));
      }
      boolean _isEmpty_1 = it.getPartitions().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        final Function1<Partition, Code.Expr> _function_1 = (Partition p) -> {
          Code.Text _text_2 = Code.text(p.getName());
          return ((Code.Expr) _text_2);
        };
        restriction.put(VespaPackage.PARTITION, Code.items(ListExtensions.<Partition, Code.Expr>map(it.getPartitions(), _function_1)));
      }
      Set<String> _keySet = restriction.entries().keySet();
      for (final String attribute : _keySet) {
        boolean _has = doc.has(attribute);
        boolean _not_2 = (!_has);
        if (_not_2) {
          String _name = it.getCollection().getName();
          String _plus = ("a slice of collection " + _name);
          String _plus_1 = (_plus + ", which declares no ");
          String _substring = attribute.substring(7);
          String _plus_2 = (_plus_1 + _substring);
          throw Unsupported.of(_plus_2);
        }
      }
      boolean _isEmpty_2 = restriction.entries().isEmpty();
      boolean _not_3 = (!_isEmpty_2);
      if (_not_3) {
        args.put("slices", restriction);
      }
      boolean _expiring = doc.expiring();
      if (_expiring) {
        args.put("expiring", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("get", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Count it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      Code.Call _runtime = Code.runtime("count", this.countArgs(it.getCollection(), this.filterOf(it, it.getCollection(), it.getFilter()).parts()));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * select ... order by the sort clauses and the key (a total order), one page or with every all pages.
   */
  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final List<String> order = CollectionLiterals.<String>newArrayList();
      EList<SortClause> _sortClauses = it.getSortClauses();
      for (final SortClause clause : _sortClauses) {
        {
          boolean _isByRelevance = clause.isByRelevance();
          if (_isByRelevance) {
            throw Unsupported.of("sort by relevance in a scroll; a scroll has no score");
          }
          VectorCollection _collection = it.getCollection();
          final VespaPackage.Field sorted = new VespaFilter(this.schema, _collection).field(clause.getField());
          if (((sorted.text() || sorted.vector()) || sorted.array())) {
            String _name = sorted.name();
            String _plus = ("sorting by " + _name);
            String _plus_1 = (_plus + "; the generator sorts by single numeric, boolean and exact string attributes");
            throw Unsupported.of(_plus_1);
          }
          String _name_1 = sorted.name();
          String _xifexpression = null;
          ESortDirection _direction = clause.getDirection();
          boolean _equals = Objects.equals(_direction, ESortDirection.DESC);
          if (_equals) {
            _xifexpression = " desc";
          } else {
            _xifexpression = " asc";
          }
          String _plus_2 = (_name_1 + _xifexpression);
          order.add(_plus_2);
        }
      }
      String _key = doc.key();
      String _plus = (_key + " asc");
      order.add(_plus);
      final VespaQueries.Params bound = new VespaQueries.Params();
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), VespaQueries.Cond.class)));
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        _xifexpression = it.getScrollLimit();
      } else {
        _xifexpression = VespaQueries.PAGE;
      }
      final int limit = _xifexpression;
      final boolean streamed = (ModelUtil.written(it, "cursor") || ModelUtil.written(it, "sliceIndex"));
      String _select = VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), order);
      int _xifexpression_1 = (int) 0;
      if (streamed) {
        _xifexpression_1 = 0;
      } else {
        _xifexpression_1 = this.offsetOf(it);
      }
      final Code.Entries request = VespaQueries.body(_select, limit, _xifexpression_1, 
        "unranked", null);
      final Code.Entries args = this.searchArgs(request, bound, doc, it.getCollection(), it.getOutput(), it);
      args.put("scores", Code.flag(false));
      boolean _written_1 = ModelUtil.written(it, "scrollLimit");
      boolean _not = (!_written_1);
      if (_not) {
        args.put("every", Code.flag(true));
      }
      boolean _written_2 = ModelUtil.written(it, "cursor");
      if (_written_2) {
        args.put("after", Code.text(it.getCursor()));
      }
      boolean _written_3 = ModelUtil.written(it, "sliceIndex");
      if (_written_3) {
        if ((((it.getSliceTotal() < 1) || (it.getSliceIndex() < 0)) || (it.getSliceIndex() >= it.getSliceTotal()))) {
          int _sliceIndex = it.getSliceIndex();
          String _plus_1 = ("slice " + Integer.valueOf(_sliceIndex));
          String _plus_2 = (_plus_1 + " of ");
          int _sliceTotal = it.getSliceTotal();
          String _plus_3 = (_plus_2 + Integer.valueOf(_sliceTotal));
          String _plus_4 = (_plus_3 + "; a slice index is at least 0 and below the slice count");
          throw Unsupported.of(_plus_4);
        }
        Code.Num _num = Code.num(it.getSliceIndex());
        Code.Num _num_1 = Code.num(it.getSliceTotal());
        args.put("slice", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_num, _num_1))));
      }
      if (streamed) {
        boolean _written_4 = ModelUtil.written(it, "scrollLimit");
        if (_written_4) {
          args.put("limit", Code.num(it.getScrollLimit()));
        }
        int _offsetOf = this.offsetOf(it);
        boolean _greaterThan = (_offsetOf > 0);
        if (_greaterThan) {
          args.put("offset", Code.num(this.offsetOf(it)));
        }
      }
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * random: the rank profile vecdsl_random (the random rank feature) over the matching documents; first: the first in key order.
   */
  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final boolean first = (ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST));
      final VespaQueries.Params bound = new VespaQueries.Params();
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), VespaQueries.Cond.class)));
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      String _name = doc.name();
      String _where = VespaQueries.where(parts, bound);
      List<String> _xifexpression = null;
      if (first) {
        String _key = doc.key();
        String _plus = (_key + " asc");
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_plus));
      } else {
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
      }
      String _select = VespaQueries.select(_name, _where, _xifexpression);
      int _sampleSize = it.getSampleSize();
      String _xifexpression_1 = null;
      if (first) {
        _xifexpression_1 = "unranked";
      } else {
        _xifexpression_1 = VespaPackage.RANDOM_PROFILE;
      }
      final Code.Entries request = VespaQueries.body(_select, _sampleSize, 0, _xifexpression_1, null);
      final Code.Entries args = this.searchArgs(request, bound, doc, it.getCollection(), it.getOutput(), it);
      args.put("scores", Code.flag(false));
      Code.Call _runtime = Code.runtime("search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A random sample (the random rank profile) with its vectors; the pairs and their canonical similarities are computed in the runtime.
   */
  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.queryExtensions(it);
      final VespaPackage.Schema doc = this.schema.schema(it.getCollection());
      final VectorField field = this.vectorFieldOf(it.getCollection(), it.getVectorField());
      final String named = this.schema.name(it.getCollection(), field);
      final VespaQueries.Params bound = new VespaQueries.Params();
      final List<VespaQueries.Cond> parts = CollectionLiterals.<VespaQueries.Cond>newArrayList(((VespaQueries.Cond[])Conversions.unwrapArray(this.filterOf(it, it.getCollection(), it.getFilter()).parts(), VespaQueries.Cond.class)));
      boolean _expiring = doc.expiring();
      if (_expiring) {
        VespaQueries.Raw _raw = new VespaQueries.Raw((VespaPackage.EXPIRES + " > @vecdsl_now"));
        parts.add(_raw);
      }
      String _select = VespaQueries.select(doc.name(), VespaQueries.where(parts, bound), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      int _xifexpression = (int) 0;
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        _xifexpression = it.getSampleSize();
      } else {
        _xifexpression = 10;
      }
      final Code.Entries request = VespaQueries.body(_select, _xifexpression, 0, VespaPackage.RANDOM_PROFILE, null);
      final Code.Entries args = VespaSchema.kw("body", request);
      bound.into(args);
      boolean _expiring_1 = doc.expiring();
      if (_expiring_1) {
        args.put("clock", Code.flag(true));
      }
      args.put("key", Code.text(doc.key()));
      args.put("field", Code.text(named));
      args.put("metric", Code.text(this.metricOf(field)));
      int _xifexpression_1 = (int) 0;
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        _xifexpression_1 = it.getMatrixLimit();
      } else {
        _xifexpression_1 = 3;
      }
      args.put("limit", Code.num(_xifexpression_1));
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + VespaStatements.NO_GENERATION));
  }

  protected List<Code.Stmt> _body(final BatchQuery it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> calls = CollectionLiterals.<Code.Expr>newArrayList();
      final String owner = this.currentMethod;
      int number = 0;
      EList<QueryMethod> _queries = it.getQueries();
      for (final QueryMethod query : _queries) {
        {
          final String name = ((owner + "_query_") + Integer.valueOf(number));
          this.currentMethod = name;
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
      this.currentMethod = owner;
      Code.Items _items = Code.items(calls);
      Code.Return _return = new Code.Return(_items);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Method it) {
    String _name = it.eClass().getName();
    String _plus = (VespaStatements.NOT_MAPPED + _name);
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
      final VespaPackage.Schema doc = this.schema.schema(collection);
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
          final String named = this.schema.name(collection, field);
          if ((((!Objects.equals(named, doc.key())) && (!excluded.contains(field))) && doc.has(named))) {
            if ((field instanceof VectorField)) {
              if (((output != null) && (!output.isAllFields()))) {
                vectors.add(named);
              }
            } else {
              fields.add(named);
            }
          }
        }
      }
      if (withVectors) {
        List<VectorField> _vectorFields = ModelUtil.vectorFields(collection);
        for (final VectorField field_1 : _vectorFields) {
          {
            final String named = this.schema.name(collection, field_1);
            if ((((!vectors.contains(named)) && (!excluded.contains(field_1))) && doc.has(named))) {
              vectors.add(named);
            }
          }
        }
      }
      _xblockexpression = Pair.<List<String>, List<String>>of(fields, vectors);
    }
    return _xblockexpression;
  }

  /**
   * Rejects clauses Vespa lacks, with their reason, and clauses not generated yet.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String featureName : features) {
      boolean _written = ModelUtil.written(statement, featureName);
      if (_written) {
        String _elvis = null;
        String _get = VespaStatements.CLAUSES.get(featureName);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = featureName;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        final String pending = VespaStatements.PENDING_CLAUSES.get(featureName);
        if ((pending != null)) {
          throw Unsupported.of((((((clause + "; ") + VespaStatements.PENDING) + " (") + pending) + ")"));
        }
        final String reason = VespaStatements.REASONS.get(featureName);
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
   * Extension vespa blocks of reads other than searches carry nothing the generator maps; their items are rejected by name.
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
      boolean _equals = Objects.equals(_vendor, VespaGenerator.VENDOR);
      if (_equals) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          {
            if ((item instanceof ExtParam)) {
              String _elvis = null;
              String _key = ((ExtParam)item).getKey();
              if (_key != null) {
                _elvis = _key;
              } else {
                String _quotedKey = ((ExtParam)item).getQuotedKey();
                _elvis = _quotedKey;
              }
              String _plus = ("extension parameter " + _elvis);
              String _plus_1 = (_plus + " on ");
              String _name = host.eClass().getName();
              String _plus_2 = (_plus_1 + _name);
              throw Unsupported.of(_plus_2);
            }
            String _name_1 = item.eClass().getName();
            String _plus_3 = (_name_1 + " on ");
            String _name_2 = host.eClass().getName();
            String _plus_4 = (_plus_3 + _name_2);
            String _plus_5 = (_plus_4 + "; Vespa extensions on this statement carry nothing the generator maps");
            throw Unsupported.of(_plus_5);
          }
        }
      }
    }
  }

  /**
   * The query vector: the given vector as input.query(vecdsl_q), or a stored document's vector the runtime reads first.
   */
  private void queryVector(final Code.Entries args, final QueryInput input, final VectorCollection collection, final String vectorName) {
    if ((input == null)) {
      throw Unsupported.of("a vector query without input");
    }
    Code.Expr _get = args.get("body");
    final Code.Entries request = ((Code.Entries) _get);
    EQueryInputMode _kind = input.getKind();
    if (_kind != null) {
      switch (_kind) {
        case VECTOR:
          Code.Expr _xifexpression = null;
          boolean _has = request.has("input");
          if (_has) {
            _xifexpression = request.get("input");
          } else {
            _xifexpression = Code.entries();
          }
          final Code.Entries inputs = ((Code.Entries) _xifexpression);
          inputs.put((("query(" + VespaPackage.QUERY_TENSOR) + ")"), Literals.of(input.getValue()));
          request.put("input", inputs);
          break;
        case ID:
          VectorCollection _elvis = null;
          VectorCollection _lookupCollection = input.getLookupCollection();
          if (_lookupCollection != null) {
            _elvis = _lookupCollection;
          } else {
            _elvis = collection;
          }
          final VectorCollection stored = _elvis;
          Code.Expr _of = Literals.of(input.getValue());
          args.put("lookup", VespaSchema.kw("document_type", Code.text(this.schema.schema(stored).name()), "field", Code.text(vectorName), 
            "positive_ids", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_of)))));
          break;
        case SPARSE_VECTOR:
          throw Unsupported.of((("sparse_vector input; " + VespaStatements.PENDING) + " (mapped tensors ranked with a dot product)"));
        case TEXT:
          throw Unsupported.of(("text input" + VespaSchema.NO_MODELS));
        case MULTI_VECTOR:
          throw Unsupported.of((("multi-vector input; " + VespaStatements.PENDING) + " (a mixed query tensor with max_sim in a rank expression)"));
        default:
          String _literal = input.getKind().getLiteral();
          String _plus = (_literal + " input");
          String _plus_1 = (_plus + VespaSchema.NO_MODELS);
          throw Unsupported.of(_plus_1);
      }
    } else {
      String _literal = input.getKind().getLiteral();
      String _plus = (_literal + " input");
      String _plus_1 = (_plus + VespaSchema.NO_MODELS);
      throw Unsupported.of(_plus_1);
    }
  }

  private Code.Expr rows(final RecordSource source, final VectorCollection collection) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", VespaSchema.kw("location", Code.text(((FileSource)source).getLocation())));
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
            throw Unsupported.of((("sparse vector values; " + VespaStatements.PENDING) + " (mapped tensors)"));
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
