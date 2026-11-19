package org.ftn.vecDSL.generator.milvus;

import com.google.common.collect.Iterables;
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
import org.ftn.vecDSL.generator.common.Py;
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
import org.ftn.vecDSL.vecDSL.BoostBlock;
import org.ftn.vecDSL.vecDSL.BoostFunction;
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
import org.ftn.vecDSL.vecDSL.EBoostKind;
import org.ftn.vecDSL.vecDSL.ECloneMode;
import org.ftn.vecDSL.vecDSL.ECollectionProperty;
import org.ftn.vecDSL.vecDSL.EConflictPolicy;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EFusionType;
import org.ftn.vecDSL.vecDSL.EImportFormat;
import org.ftn.vecDSL.vecDSL.EListKind;
import org.ftn.vecDSL.vecDSL.EMatchOperator;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.ESampleMethod;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESnapshotScope;
import org.ftn.vecDSL.vecDSL.ETextQueryType;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
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
 * Maps each vecDSL statement to Milvus operations in the host-language-neutral code model. Operation and
 * argument names follow pymilvus MilvusClient; composite behaviour (insert conflict handling, partial
 * updates, score normalization, recommendation vectors) is delegated to the per-language runtime module.
 */
@SuppressWarnings("all")
public class MilvusStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; MilvusCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Milvus: ";

  private static final String PENDING = " has no Milvus mapping yet";

  private static final String METRICS = "metrics configuration; Milvus exports Prometheus metrics on port 9091 and configures them in milvus.yaml";

  private static final String CLUSTER_LIMITS = "cluster limits; Milvus sets them for the whole server in milvus.yaml (rootCoord.maxDatabaseNum, quotaAndLimits.limits.maxCollectionNum)";

  private static final String REPLICATION = "a stored replication factor; Milvus sets the replica count per load request (replica_number)";

  private static final String NO_GENERATION = "Milvus has no generation API (its functions are BM25, text embedding, rerank, MinHash and molecular fingerprints)";

  private static final String GENERATION = ("generative models; " + MilvusStatements.NO_GENERATION);

  private static final Set<String> IMPORT_SCHEMES = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("s3", "minio", "gs", "gcs", "az", "azure"));

  private static final Set<EImportFormat> IMPORT_FORMATS = Collections.<EImportFormat>unmodifiableSet(CollectionLiterals.<EImportFormat>newHashSet(EImportFormat.JSON, EImportFormat.PARQUET, EImportFormat.NUMPY, EImportFormat.CSV));

  private static final String NO_MODEL_OBJECTS = "; Milvus has no model objects: providers are configured in milvus.yaml and used through collection functions";

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("offset", "offset"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("sliceIndex", "slice"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("exact", "exact"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("bounds", "bounds"), Pair.<String, String>of("params", "params"), Pair.<String, String>of("textQueryType", "text-query"), Pair.<String, String>of("matchOperator", "operator"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("consistency", "consistency")));

  /**
   * Why Milvus cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("asOf", "Milvus reads only the current state of a collection"), Pair.<String, String>of("cursor", "Milvus pages searches by offset and scrolls by primary key, not by an opaque cursor"), Pair.<String, String>of("explain", "Milvus returns no score explanation"), Pair.<String, String>of("withTotal", "Milvus returns no total hit count with search results"), Pair.<String, String>of("fuzziness", "Milvus BM25 has no fuzzy matching"), Pair.<String, String>of("minShouldMatch", "Milvus BM25 has no minimum-should-match"), Pair.<String, String>of("exact", "Milvus has no per-query exact search (a FLAT index is always exhaustive)"), Pair.<String, String>of("sliceIndex", "Milvus has no parallel scroll slices"), Pair.<String, String>of("prefetches", "Milvus runs multi-stage retrieval only as hybrid search legs"), Pair.<String, String>of("bounds", "Milvus bounds hybrid results only per leg"), Pair.<String, String>of("params", "Milvus takes search parameters per hybrid leg"), Pair.<String, String>of("queryModel", "Milvus embeds query text with the collection\'s text embedding function")));

  /**
   * Clauses Milvus 3.0 serves that the generator does not emit yet.
   */
  private static final Set<String> NATIVE_PENDING = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("elementScope", "mmr", "reranker", "highlighted", "sortClauses", "aggregations", "textQueryType", "matchOperator", "boost"));

  private final Script script;

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final MilvusSchema schema = new MilvusSchema(this.notes);

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public MilvusStatements(final Script script) {
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
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          MetricsConfig _metrics = ((VectorDatabase)concept).getMetrics();
          boolean _tripleNotEquals = (_metrics != null);
          if (_tripleNotEquals) {
            throw Unsupported.of(MilvusStatements.METRICS);
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
          boolean _tripleNotEquals_1 = (_auth != null);
          if (_tripleNotEquals_1) {
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
            throw Unsupported.of(MilvusStatements.CLUSTER_LIMITS);
          }
          ReplicationConfig _replication = ((Cluster)concept).getReplication();
          boolean _tripleNotEquals = (_replication != null);
          if (_tripleNotEquals) {
            throw Unsupported.of(MilvusStatements.REPLICATION);
          }
          ShardConfig _sharding = ((Cluster)concept).getSharding();
          boolean _tripleNotEquals_1 = (_sharding != null);
          if (_tripleNotEquals_1) {
            throw Unsupported.of(("cluster sharding (shards_num of its collections)" + MilvusStatements.PENDING));
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
          List<Code.Stmt> _database = this.database(((Database)concept), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _database);
        }
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("create_alias", MilvusSchema.kw("collection_name", Code.text(((Alias)concept).getCollection().getName()), "alias", Code.text(((Alias)concept).getName()))));
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          Code.Stmt _createIndex = this.createIndex(this.indexCollection(((Index)concept)), this.schema.vectorIndex(this.indexCollection(((Index)concept)), this.vectorFieldOf(((VectorIndex)concept)), ((VectorIndex)concept)));
          result.add(_createIndex);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          EPayloadIndexKind _kind = ((PayloadIndex)concept).getKind();
          boolean _equals = Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT);
          if (_equals) {
            throw Unsupported.of("a full_text payload index on an existing field; Milvus enables text match only when it creates a field");
          }
          Code.Stmt _createIndex = this.createIndex(this.indexCollection(((Index)concept)), this.schema.payloadIndex(((PayloadIndex)concept)));
          result.add(_createIndex);
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
          List<Code.Stmt> _createPartition = this.createPartition(this.member(concept, "a partition"), ((Partition)concept).getName(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createPartition);
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          List<Code.Stmt> _createSlice = this.createSlice(this.member(concept, "a tenant"), ((Tenant)concept).getName(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createSlice);
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          List<Code.Stmt> _createSlice = this.createSlice(this.member(concept, "a namespace"), ((Namespace)concept).getKey(), ifNotExists);
          Iterables.<Code.Stmt>addAll(result, _createSlice);
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("add_collection_function", MilvusSchema.kw("collection_name", Code.text(this.member(concept, "a function").getName()), 
            "function", this.schema.function(((Function)concept)))));
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          String _path = ((FileResource)concept).getPath();
          boolean _tripleEquals = (_path == null);
          if (_tripleEquals) {
            throw Unsupported.of("a file resource without a path; Milvus registers a file resource by its object storage path");
          }
          Code.Stmt _run = MilvusStatements.run(Code.runtime("add_file_resource", MilvusSchema.kw("name", Code.text(((FileResource)concept).getName()), "path", Code.text(((FileResource)concept).getPath()))));
          result.add(_run);
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          Code.Call _runtime = Code.runtime("check_analyzer", MilvusSchema.kw("analyzer_params", this.schema.analyzerParams(((Analyzer)concept))));
          Code.Return _return = new Code.Return(_runtime);
          result.add(_return);
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          final Analyzer analyzer = EcoreUtil2.<Analyzer>getContainerOfType(concept, Analyzer.class);
          if ((analyzer == null)) {
            throw Unsupported.of("an analyzer filter outside an analyzer; Milvus applies token filters inside an analyzer definition");
          }
          Code.Call _runtime = Code.runtime("check_analyzer", MilvusSchema.kw("analyzer_params", this.schema.analyzerParams(analyzer)));
          Code.Return _return = new Code.Return(_runtime);
          result.add(_return);
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
          throw Unsupported.of(("embedding models" + MilvusStatements.NO_MODEL_OBJECTS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.GENERATION);
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of((("rerankers" + MilvusStatements.NO_MODEL_OBJECTS) + " and per-search rankers"));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("quantization outside a vector index; Milvus sets quantization through the index type of a vector index");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.REPLICATION);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of("sharding for an existing collection; Milvus fixes shards_num when it creates a collection");
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("storage settings as an object; Milvus sets mmap per collection, field or index, and storage for the whole server");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication settings" + MilvusStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings" + MilvusStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of("backup schedules; Milvus creates snapshots only on request");
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of("collection limits; Milvus sets them for the whole server in milvus.yaml (proxy.maxFieldNum, proxy.maxShardNum, proxy.maxDimension, rootCoord.maxPartitionNum)");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = (MilvusStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The collection of a member concept: its container, or the collection of the enclosing create. Milvus creates fields,
   * partitions, tenants and functions only inside a collection.
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
    throw Unsupported.of((what + " outside a collection; Milvus creates it only inside a collection"));
  }

  /**
   * A tenant or namespace: a value of the partition key when the collection declares one, otherwise a partition.
   */
  private List<Code.Stmt> createSlice(final VectorCollection collection, final String slice, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Field _partitionKey = ModelUtil.partitionKey(collection);
      boolean _tripleNotEquals = (_partitionKey != null);
      if (_tripleNotEquals) {
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
      _xblockexpression = this.createPartition(collection, slice, ifNotExists);
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createPartition(final VectorCollection collection, final String partition, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(collection.getName()), "partition_name", Code.text(partition));
      final Code.Stmt create = MilvusStatements.run(Code.client("create_partition", args));
      if ((!ifNotExists)) {
        return CollectionLiterals.<Code.Stmt>newArrayList(create);
      }
      Code.Call _runtime = Code.runtime("has_partition", args);
      Code.When _when = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  /**
   * A field added to an existing collection: Milvus requires added fields to be nullable and vector fields to be indexed.
   */
  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.schema.fieldArgs(collection, field);
      args.put("nullable", Code.flag(true));
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Stmt _run = MilvusStatements.run(Code.client("add_collection_field", MilvusSchema.kw("collection_name", Code.text(collection.getName()), "field", Code.make("Field", args))));
      result.add(_run);
      if ((field instanceof VectorField)) {
        final VectorField vector = ((VectorField) field);
        Code.Stmt _createIndex = this.createIndex(collection, this.schema.vectorIndex(collection, vector, ModelUtil.vectorIndex(collection, vector)));
        result.add(_createIndex);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> fieldLimit(final FieldLimit limit) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field field = EcoreUtil2.<Field>getContainerOfType(limit, Field.class);
      if ((field == null)) {
        throw Unsupported.of("a field limit outside a field; Milvus stores limits as field parameters");
      }
      String _switchResult = null;
      EFieldLimit _type = limit.getType();
      if (_type != null) {
        switch (_type) {
          case ARRAY_LEN:
            _switchResult = "max_capacity";
            break;
          case MAX_SIZE:
            _switchResult = "max_length";
            break;
          case VECTOR_DIM:
            throw Unsupported.of("changing a vector dimension; Milvus fixes dim when it creates a field");
          default:
            break;
        }
      }
      final String key = _switchResult;
      final VectorCollection collection = this.member(field, "a field");
      Code.Num _num = Code.num(limit.getValue());
      Pair<String, Code.Num> _mappedTo = Pair.<String, Code.Num>of(key, _num);
      Code.Stmt _run = MilvusStatements.run(Code.runtime("alter_field", MilvusSchema.kw("collection_name", Code.text(collection.getName()), "field_name", Code.text(ModelUtil.fieldName(field)), 
        "field_params", Code.entries(Collections.<String, Code.Num>unmodifiableMap(CollectionLiterals.<String, Code.Num>newHashMap(_mappedTo))))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> database(final Database database, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(database.getName());
      final Code.Stmt create = MilvusStatements.run(Code.client("create_database", MilvusSchema.kw("db_name", name)));
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Stmt _xifexpression = null;
      if (ifNotExists) {
        Code.Call _runtime = Code.runtime("has_database", MilvusSchema.kw("db_name", name));
        _xifexpression = new Code.When(_runtime, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      } else {
        _xifexpression = create;
      }
      result.add(_xifexpression);
      Code.Stmt _run = MilvusStatements.run(Code.client("use_database", MilvusSchema.kw("db_name", name)));
      result.add(_run);
      EList<VectorCollection> _collections = database.getCollections();
      for (final VectorCollection collection : _collections) {
        List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists);
        Iterables.<Code.Stmt>addAll(result, _createCollection);
      }
      Code.Stmt _run_1 = MilvusStatements.run(Code.client("use_database", MilvusSchema.kw("db_name", Code.text(this.defaultDatabase()))));
      result.add(_run_1);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Stmt createIndex(final VectorCollection collection, final Code.Make index) {
    return MilvusStatements.run(Code.client("create_index", MilvusSchema.kw("collection_name", Code.text(collection.getName()), "index_params", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(index))))));
  }

  protected List<Code.Stmt> _body(final Alter it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Concept concept = it.getTarget();
      if ((!(concept instanceof VectorCollection))) {
        String _name = concept.eClass().getName();
        String _plus = ("altering a " + _name);
        String _plus_1 = (_plus + "; Milvus renames and evolves collections only");
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
        Code.Stmt _run = MilvusStatements.run(Code.client("rename_collection", MilvusSchema.kw("old_name", Code.text(collection.getName()), "new_name", Code.text(it.getNewName()))));
        result.add(_run);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(collection.getName());
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (operation instanceof AddFieldOp) {
        _matched=true;
        _switchResult = this.addField(collection, ((AddFieldOp)operation).getNewField());
      }
      if (!_matched) {
        if (operation instanceof DropFieldOp) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.runtime("drop_field", MilvusSchema.kw("collection_name", name, "field_name", Code.text(ModelUtil.fieldName(((DropFieldOp)operation).getField())))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (operation instanceof RenameFieldOp) {
          _matched=true;
          throw Unsupported.of("renaming a field; Milvus adds and drops fields but cannot rename them");
        }
      }
      if (!_matched) {
        if (operation instanceof SetPropertyOp) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("alter_collection_properties", MilvusSchema.kw("collection_name", name, "properties", this.propertyEntry(((SetPropertyOp)operation)))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (operation instanceof AddAliasOp) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("create_alias", MilvusSchema.kw("collection_name", name, "alias", Code.text(((AddAliasOp)operation).getNewAlias().getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (operation instanceof AddFunctionOp) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("add_collection_function", MilvusSchema.kw("collection_name", name, "function", this.schema.function(((AddFunctionOp)operation).getNewFunction()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (operation instanceof DropFunctionOp) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("drop_collection_function", MilvusSchema.kw("collection_name", name, "function_name", Code.text(((DropFunctionOp)operation).getFunction().getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        String _name = operation.eClass().getName();
        String _plus = (MilvusStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The Milvus collection property for a portable one; the values are strings, as the server stores them.
   */
  private Code.Entries propertyEntry(final SetPropertyOp operation) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Expr value = Literals.of(operation.getValue());
      Code.Entries _switchResult = null;
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case TTL:
            Code.Entries _xblockexpression_1 = null;
            {
              Code.Expr _xifexpression = null;
              if ((value instanceof Code.Num)) {
                _xifexpression = Code.text(Py.num(((Code.Num)value).value()));
              } else {
                _xifexpression = value;
              }
              final Code.Expr seconds = _xifexpression;
              Pair<String, Code.Expr> _mappedTo = Pair.<String, Code.Expr>of("collection.ttl.seconds", seconds);
              _xblockexpression_1 = Code.entries(Collections.<String, Code.Expr>unmodifiableMap(CollectionLiterals.<String, Code.Expr>newHashMap(_mappedTo)));
            }
            _switchResult = _xblockexpression_1;
            break;
          case DESCRIPTION:
            Pair<String, Code.Expr> _mappedTo = Pair.<String, Code.Expr>of("collection.description", value);
            _switchResult = Code.entries(Collections.<String, Code.Expr>unmodifiableMap(CollectionLiterals.<String, Code.Expr>newHashMap(_mappedTo)));
            break;
          case CONSISTENCY:
            Code.Entries _xblockexpression_2 = null;
            {
              if ((!(value instanceof Code.Text))) {
                throw Unsupported.of("a consistency that is not a level name");
              }
              final EConsistencyLevel level = EConsistencyLevel.get(((Code.Text) value).value());
              if ((level == null)) {
                String _value = ((Code.Text) value).value();
                String _plus = ("consistency " + _value);
                throw Unsupported.of(_plus);
              }
              Code.Expr _consistency = this.schema.consistency(level);
              Code.Text _text = Code.text(((Code.Sym) _consistency).name());
              Pair<String, Code.Text> _mappedTo_1 = Pair.<String, Code.Text>of("consistency_level", _text);
              _xblockexpression_2 = Code.entries(Collections.<String, Code.Text>unmodifiableMap(CollectionLiterals.<String, Code.Text>newHashMap(_mappedTo_1)));
            }
            _switchResult = _xblockexpression_2;
            break;
          case DYNAMIC:
            Code.Entries _xblockexpression_3 = null;
            {
              if ((!(value instanceof Code.Flag))) {
                throw Unsupported.of("a dynamic setting that is not true or false");
              }
              Code.Text _text = Code.text(String.valueOf(((Code.Flag) value).value()));
              Pair<String, Code.Text> _mappedTo_1 = Pair.<String, Code.Text>of("dynamicfield.enabled", _text);
              _xblockexpression_3 = Code.entries(Collections.<String, Code.Text>unmodifiableMap(CollectionLiterals.<String, Code.Text>newHashMap(_mappedTo_1)));
            }
            _switchResult = _xblockexpression_3;
            break;
          case DELETION_PROTECTION:
            throw Unsupported.of("deletion-protection; Milvus has no collection property that blocks dropping a collection");
          case TAGS:
            throw Unsupported.of("collection tags; Milvus collections have no tags");
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
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
          final List<Code.Stmt> steps = CollectionLiterals.<Code.Stmt>newArrayList();
          boolean _aliased = this.aliased(((VectorCollection)concept));
          if (_aliased) {
            Code.Stmt _run = MilvusStatements.run(Code.runtime("drop_aliases", MilvusSchema.kw("collection_name", name)));
            steps.add(_run);
          }
          Code.Stmt _run_1 = MilvusStatements.run(Code.client("drop_collection", MilvusSchema.kw("collection_name", name)));
          steps.add(_run_1);
          List<Code.Stmt> _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            Code.Call _runtime = Code.runtime("has_collection", MilvusSchema.kw("collection_name", name));
            Code.When _when = new Code.When(_runtime, false, steps);
            _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when)));
          } else {
            _xifexpression = steps;
          }
          _xblockexpression_1 = _xifexpression;
        }
        _switchResult = _xblockexpression_1;
      }
      if (!_matched) {
        if (concept instanceof Alias) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("drop_alias", MilvusSchema.kw("alias", Code.text(((Alias)concept).getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          _switchResult = this.dropPartition(this.member(concept, "a partition"), ((Partition)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Tenant) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a tenant"), ((Tenant)concept).getName());
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropSlice(this.member(concept, "a namespace"), ((Namespace)concept).getKey());
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          _switchResult = this.dropIndex(this.indexCollection(((Index)concept)), ((Index)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          _switchResult = this.dropIndex(this.indexCollection(((Index)concept)), ((Index)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.client("drop_database", MilvusSchema.kw("db_name", Code.text(((Database)concept).getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          Code.Stmt _dropField = this.dropField(((Field)concept));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_dropField));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          Code.Stmt _dropField = this.dropField(((Field)concept));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_dropField));
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final VectorCollection collection = this.member(concept, "a function");
            Code.Stmt _run = MilvusStatements.run(Code.client("drop_collection_function", MilvusSchema.kw("collection_name", Code.text(collection.getName()), "function_name", Code.text(((Function)concept).getName()))));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof FileResource) {
          _matched=true;
          Code.Stmt _run = MilvusStatements.run(Code.runtime("remove_file_resource", MilvusSchema.kw("name", Code.text(((FileResource)concept).getName()))));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          throw Unsupported.of("dropping a connection; a connection is the program endpoint, not a Milvus object");
        }
      }
      if (!_matched) {
        if (concept instanceof VectorDatabase) {
          _matched=true;
          throw Unsupported.of("dropping a deployment; Milvus drops collections and logical databases, not the server");
        }
      }
      if (!_matched) {
        if (concept instanceof Cluster) {
          _matched=true;
          throw Unsupported.of("dropping a cluster; Milvus drops collections and logical databases, not the cluster");
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer; Milvus stores analyzers inside field definitions, not as objects");
        }
      }
      if (!_matched) {
        if (concept instanceof AnalyzerFilter) {
          _matched=true;
          throw Unsupported.of("dropping an analyzer filter; Milvus stores analyzers inside field definitions, not as objects");
        }
      }
      if (!_matched) {
        if (concept instanceof FieldLimit) {
          _matched=true;
          throw Unsupported.of("dropping a field limit; Milvus requires max_length and max_capacity on their fields");
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          throw Unsupported.of(("embedding models" + MilvusStatements.NO_MODEL_OBJECTS));
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.GENERATION);
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          throw Unsupported.of((("rerankers" + MilvusStatements.NO_MODEL_OBJECTS) + " and per-search rankers"));
        }
      }
      if (!_matched) {
        if (concept instanceof Quantization) {
          _matched=true;
          throw Unsupported.of("dropping quantization; Milvus sets quantization through the index type of a vector index");
        }
      }
      if (!_matched) {
        if (concept instanceof ReplicationConfig) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.REPLICATION);
        }
      }
      if (!_matched) {
        if (concept instanceof ShardConfig) {
          _matched=true;
          throw Unsupported.of("dropping sharding; Milvus fixes shards_num when it creates a collection");
        }
      }
      if (!_matched) {
        if (concept instanceof StorageConfig) {
          _matched=true;
          throw Unsupported.of("storage settings as an object; Milvus sets mmap per collection, field or index, and storage for the whole server");
        }
      }
      if (!_matched) {
        if (concept instanceof AuthConfig) {
          _matched=true;
          throw Unsupported.of(("authentication settings" + MilvusStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          throw Unsupported.of(("role bindings" + MilvusStatements.PENDING));
        }
      }
      if (!_matched) {
        if (concept instanceof MetricsConfig) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.METRICS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of("backup schedules; Milvus creates snapshots only on request");
        }
      }
      if (!_matched) {
        if (concept instanceof ClusterLimit) {
          _matched=true;
          throw Unsupported.of(MilvusStatements.CLUSTER_LIMITS);
        }
      }
      if (!_matched) {
        if (concept instanceof CollectionLimit) {
          _matched=true;
          throw Unsupported.of("collection limits; Milvus sets them for the whole server in milvus.yaml (proxy.maxFieldNum, proxy.maxShardNum, proxy.maxDimension, rootCoord.maxPartitionNum)");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ((MilvusStatements.NOT_MAPPED + "dropping a ") + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Whether the script gives the collection an alias (declared, added or swapped to it). Milvus refuses to drop a collection
   * that still has aliases, so such a drop removes them first.
   */
  private boolean aliased(final VectorCollection collection) {
    return (IterableExtensions.<Alias>exists(EcoreUtil2.<Alias>getAllContentsOfType(this.script, Alias.class), ((Function1<Alias, Boolean>) (Alias alias) -> {
      VectorCollection _collection = alias.getCollection();
      return Boolean.valueOf((_collection == collection));
    })) || IterableExtensions.<SwapAlias>exists(EcoreUtil2.<SwapAlias>getAllContentsOfType(this.script, SwapAlias.class), ((Function1<SwapAlias, Boolean>) (SwapAlias swap) -> {
      VectorCollection _newCollection = swap.getNewCollection();
      return Boolean.valueOf((_newCollection == collection));
    })));
  }

  private Code.Stmt dropField(final Field field) {
    return MilvusStatements.run(Code.runtime("drop_field", MilvusSchema.kw("collection_name", Code.text(this.member(field, "a field").getName()), "field_name", Code.text(ModelUtil.fieldName(field)))));
  }

  private List<Code.Stmt> dropPartition(final VectorCollection collection, final String partition) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(collection.getName());
      Code.Stmt _run = MilvusStatements.run(Code.client("release_partitions", MilvusSchema.kw("collection_name", name, "partition_names", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(partition))))));
      Code.Stmt _run_1 = MilvusStatements.run(Code.client("drop_partition", MilvusSchema.kw("collection_name", name, "partition_name", Code.text(partition))));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1));
    }
    return _xblockexpression;
  }

  /**
   * Dropping a tenant or namespace removes its records: its partition, or its rows when it is a partition key value.
   */
  private List<Code.Stmt> dropSlice(final VectorCollection collection, final String slice) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Field key = ModelUtil.partitionKey(collection);
      if ((key == null)) {
        return this.dropPartition(collection, slice);
      }
      String _fieldName = ModelUtil.fieldName(key);
      String _plus = (_fieldName + " == ");
      String _str = Py.str(slice);
      String _plus_1 = (_plus + _str);
      Code.Call _runtime = Code.runtime("delete_rows", MilvusSchema.kw("collection_name", Code.text(collection.getName()), 
        "filter", Code.text(_plus_1)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
    }
    return _xblockexpression;
  }

  /**
   * Milvus drops a vector index only while the collection is released.
   */
  private List<Code.Stmt> dropIndex(final VectorCollection collection, final Index index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(collection.getName());
      final Code.Stmt drop = MilvusStatements.run(Code.client("drop_index", MilvusSchema.kw("collection_name", name, "index_name", Code.text(index.getName()))));
      List<Code.Stmt> _xifexpression = null;
      if ((index instanceof VectorIndex)) {
        Code.Stmt _run = MilvusStatements.run(Code.client("release_collection", MilvusSchema.kw("collection_name", name)));
        _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, drop));
      } else {
        _xifexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.statementTtl(it, it.getCollection());
      this.schema.note(it, "writeConsistency", "recordCountHint");
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "rows", this.rows(it.getSource()), "primary_key", Code.text(this.primaryKey(it.getCollection())));
      boolean _written = ModelUtil.written(it, "onConflict");
      if (_written) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.writeScope(args, it.getCollection(), it.getNamespace(), it.getTenant(), it.getPartition());
      _xblockexpression = this.write("insert_rows", args);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.statementTtl(it, it.getCollection());
      this.schema.note(it, "writeConsistency", "recordCountHint");
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "rows", this.rows(it.getSource()));
      this.writeScope(args, it.getCollection(), it.getNamespace(), it.getTenant(), it.getPartition());
      boolean _written = ModelUtil.written(it, "batchSize");
      if (_written) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      _xblockexpression = this.write("upsert_rows", args);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((!it.getAppendFields().isEmpty()) || (!it.getArrayRemoveFields().isEmpty()))) {
        throw Unsupported.of(("array-append and array-remove" + MilvusStatements.PENDING));
      }
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        throw Unsupported.of("if-version; Milvus writes have no version condition");
      }
      this.schema.note(it, "writeConsistency", "recordCountHint");
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "primary_key", Code.text(this.primaryKey(it.getCollection())));
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
      boolean _written_1 = ModelUtil.written(it, "mode");
      if (_written_1) {
        args.put("mode", Code.text(it.getMode().getLiteral()));
      }
      boolean _isEmpty_2 = it.getRemovedFields().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        final Function1<Field, String> _function_1 = (Field it_1) -> {
          return ModelUtil.fieldName(it_1);
        };
        args.put("remove", Code.texts(ListExtensions.<Field, String>map(it.getRemovedFields(), _function_1)));
      }
      this.partitionName(args, it.getCollection(), it.getNamespace(), it.getTenant(), it.getPartition());
      _xblockexpression = this.write("update_rows", args);
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "writeConsistency");
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      boolean _written = ModelUtil.written(it, "deleteLimit");
      if (_written) {
        return this.deleteFirst(it, where);
      }
      if (((it.isDeleteAll() && it.getIds().isEmpty()) && where.isEmpty())) {
        if ((((it.getNamespace() == null) && (it.getTenant() == null)) && (it.getPartition() == null))) {
          Code.Stmt _run = MilvusStatements.run(Code.client("truncate_collection", MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()))));
          return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
        }
        return this.deleteFirst(it, where);
      }
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      if (((!it.getIds().isEmpty()) && where.isEmpty())) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      if (((!it.getIds().isEmpty()) && (!where.isEmpty()))) {
        String _primaryKey = this.primaryKey(it.getCollection());
        String _plus = (_primaryKey + " in [");
        final Function1<Literal, String> _function_1 = (Literal it_1) -> {
          return where.value(it_1);
        };
        String _join = IterableExtensions.join(ListExtensions.<Literal, String>map(it.getIds(), _function_1), ", ");
        String _plus_1 = (_plus + _join);
        String _plus_2 = (_plus_1 + "]");
        where.add(_plus_2);
      }
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.partitionName(args, it.getCollection(), it.getNamespace(), it.getTenant(), it.getPartition());
      _xblockexpression = this.write("delete_rows", args);
    }
    return _xblockexpression;
  }

  /**
   * Deletes by keys the runtime selects: the first n matching records in primary key order (Milvus deletes take no
   * limit), or every record of a partition (a Milvus delete needs ids or a filter).
   */
  private List<Code.Stmt> deleteFirst(final Delete delete, final MilvusFilter where) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = delete.getCollection();
      boolean _isEmpty = delete.getIds().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        String _primaryKey = this.primaryKey(collection);
        String _plus = (_primaryKey + " in [");
        final Function1<Literal, String> _function = (Literal it) -> {
          return where.value(it);
        };
        String _join = IterableExtensions.join(ListExtensions.<Literal, String>map(delete.getIds(), _function), ", ");
        String _plus_1 = (_plus + _join);
        String _plus_2 = (_plus_1 + "]");
        where.add(_plus_2);
      }
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(collection.getName()), "primary_key", Code.text(this.primaryKey(collection)));
      boolean _written = ModelUtil.written(delete, "deleteLimit");
      if (_written) {
        args.put("limit", Code.num(delete.getDeleteLimit()));
      }
      boolean _isEmpty_1 = where.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("filter", where.code());
      }
      this.partitionName(args, collection, delete.getNamespace(), delete.getTenant(), delete.getPartition());
      _xblockexpression = this.write("delete_matching", args);
    }
    return _xblockexpression;
  }

  private void statementTtl(final EObject statement, final VectorCollection collection) {
    boolean _written = ModelUtil.written(statement, "ttlSeconds");
    boolean _not = (!_written);
    if (_not) {
      return;
    }
    Field _ttlField = collection.getTtlField();
    boolean _tripleNotEquals = (_ttlField != null);
    if (_tripleNotEquals) {
      throw Unsupported.of(("per-statement ttl on a collection with a ttl-field" + MilvusStatements.PENDING));
    }
    throw Unsupported.of("per-statement ttl; Milvus expires records by the collection ttl or by a timestamptz ttl field");
  }

  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous import; a Milvus import job reads a fixed set of files");
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
        return this.serverImport(it, scheme);
      }
      this.schema.note(it, "parallelism", "async");
      final Code.Entries read = MilvusSchema.kw("location", Code.text(it.getLocation()));
      boolean _written = ModelUtil.written(it, "format");
      if (_written) {
        read.put("format", Code.text(it.getFormat().getLiteral()));
      }
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "rows", Code.runtime("read_records", read));
      final boolean upsert = (ModelUtil.written(it, "onConflict") && (Objects.equals(it.getOnConflict(), EConflictPolicy.UPDATE) || Objects.equals(it.getOnConflict(), EConflictPolicy.REPLACE)));
      if ((!upsert)) {
        args.put("primary_key", Code.text(this.primaryKey(it.getCollection())));
      }
      if (((!upsert) && ModelUtil.written(it, "onConflict"))) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.writeScope(args, it.getCollection(), it.getNamespace(), it.getTenant(), null);
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      String _xifexpression_1 = null;
      if (upsert) {
        _xifexpression_1 = "upsert_rows";
      } else {
        _xifexpression_1 = "insert_rows";
      }
      Code.Call _runtime = Code.runtime(_xifexpression_1, args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * A Milvus import job (REST /v2/vectordb/jobs/import): the server reads the file from its own bucket, so the path is
   * the location without scheme and bucket.
   */
  private List<Code.Stmt> serverImport(final BulkImport job, final String scheme) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _contains = MilvusStatements.IMPORT_SCHEMES.contains(scheme);
      boolean _not = (!_contains);
      if (_not) {
        throw Unsupported.of((("import from " + scheme) + "; Milvus imports files from its object storage bucket"));
      }
      boolean _written = ModelUtil.written(job, "onConflict");
      if (_written) {
        throw Unsupported.of("the \'on-conflict\' clause with a server-side import; Milvus import jobs take no conflict policy");
      }
      if ((ModelUtil.written(job, "format") && (!MilvusStatements.IMPORT_FORMATS.contains(job.getFormat())))) {
        String _literal = job.getFormat().getLiteral();
        String _plus = ("import format " + _literal);
        String _plus_1 = (_plus + "; Milvus imports JSON, JSON Lines, Parquet, NumPy and CSV files");
        throw Unsupported.of(_plus_1);
      }
      this.schema.note(job, "batchSize", "parallelism");
      String _location = job.getLocation();
      int _length = scheme.length();
      int _plus_2 = (_length + 3);
      final String path = _location.substring(_plus_2);
      boolean _contains_1 = path.contains("/");
      boolean _not_1 = (!_contains_1);
      if (_not_1) {
        throw Unsupported.of("an import location without an object path");
      }
      int _indexOf = path.indexOf("/");
      int _plus_3 = (_indexOf + 1);
      String _substring = path.substring(_plus_3);
      Code.Items _texts = Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_substring)));
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(job.getCollection().getName()), "files", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(_texts))));
      final VectorCollection collection = job.getCollection();
      String _elvis = null;
      Tenant _tenant = job.getTenant();
      String _name = null;
      if (_tenant!=null) {
        _name=_tenant.getName();
      }
      if (_name != null) {
        _elvis = _name;
      } else {
        String _namespace = job.getNamespace();
        _elvis = _namespace;
      }
      final String slice = _elvis;
      if (((job.getPartition() != null) || ((slice != null) && (ModelUtil.partitionKey(collection) == null)))) {
        String _elvis_1 = null;
        Partition _partition = job.getPartition();
        String _name_1 = null;
        if (_partition!=null) {
          _name_1=_partition.getName();
        }
        if (_name_1 != null) {
          _elvis_1 = _name_1;
        } else {
          _elvis_1 = slice;
        }
        args.put("partition_name", Code.text(_elvis_1));
      } else {
        if ((slice != null)) {
          throw Unsupported.of("a tenant or namespace with a server-side import into a partition-key collection; the import file must carry the key");
        }
      }
      boolean _isAsync = job.isAsync();
      boolean _not_2 = (!_isAsync);
      args.put("wait", Code.flag(_not_2));
      Code.Call _runtime = Code.runtime("bulk_import", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of("multi-statement transactions; Milvus has no transaction API (every write commits on its own)");
  }

  /**
   * Milvus 3 acknowledges a write only after logging it, and strong reads see it at once, so 'wait' needs no
   * flush; flushing per write would also hit the server's flush rate limit (one per collection every 10 s).
   */
  private List<Code.Stmt> write(final String op, final Code.Entries args) {
    Code.Call _runtime = Code.runtime(op, args);
    Code.Return _return = new Code.Return(_runtime);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
  }

  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Partition, String> _function = (Partition it_1) -> {
          return it_1.getName();
        };
        args.put("partition_names", Code.texts(ListExtensions.<Partition, String>map(it.getPartitions(), _function)));
      }
      this.extensions(args, it);
      String _xifexpression = null;
      boolean _isEmpty_1 = it.getPartitions().isEmpty();
      if (_isEmpty_1) {
        _xifexpression = "load_collection";
      } else {
        _xifexpression = "load_partitions";
      }
      Code.Stmt _run = MilvusStatements.run(Code.client(_xifexpression, args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Partition, String> _function = (Partition it_1) -> {
          return it_1.getName();
        };
        args.put("partition_names", Code.texts(ListExtensions.<Partition, String>map(it.getPartitions(), _function)));
      }
      String _xifexpression = null;
      boolean _isEmpty_1 = it.getPartitions().isEmpty();
      if (_isEmpty_1) {
        _xifexpression = "release_collection";
      } else {
        _xifexpression = "release_partitions";
      }
      Code.Stmt _run = MilvusStatements.run(Code.client(_xifexpression, args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Flush it) {
    Code.Stmt _run = MilvusStatements.run(Code.client("flush", MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()))));
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  protected List<Code.Stmt> _body(final Compact it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.schema.note(it, "targetSegments");
      boolean _isEmpty = this.schema.extValues(it).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(("compaction options (clustering, L0, target size)" + MilvusStatements.PENDING));
      }
      boolean _isAsync = it.isAsync();
      boolean _not_1 = (!_isAsync);
      Code.Call _runtime = Code.runtime("compact", MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "wait", Code.flag(_not_1)));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
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
    throw Unsupported.of(("embedding migration" + MilvusStatements.PENDING));
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.snapshotScope(it, it.getScope());
      boolean _isIncremental = it.isIncremental();
      if (_isIncremental) {
        throw Unsupported.of("incremental snapshots; a Milvus snapshot always records the whole collection");
      }
      Tenant _tenant = it.getTenant();
      boolean _tripleNotEquals = (_tenant != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("tenant snapshots; Milvus snapshots whole collections");
      }
      String _location = it.getLocation();
      boolean _tripleNotEquals_1 = (_location != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("snapshot locations (export to external storage)" + MilvusStatements.PENDING));
      }
      String _elvis = null;
      String _snapshotName = it.getSnapshotName();
      if (_snapshotName != null) {
        _elvis = _snapshotName;
      } else {
        String _name = it.getName();
        _elvis = _name;
      }
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "snapshot_name", Code.text(_elvis));
      String _description = it.getDescription();
      boolean _tripleNotEquals_2 = (_description != null);
      if (_tripleNotEquals_2) {
        args.put("description", Code.text(it.getDescription()));
      }
      Code.Stmt _run = MilvusStatements.run(Code.runtime("create_snapshot", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * Restores into a new collection: Milvus requires an absent target and keeps a snapshot with its source collection.
   */
  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.snapshotScope(it, it.getScope());
      String _location = it.getLocation();
      boolean _tripleNotEquals = (_location != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("restoring from an external location" + MilvusStatements.PENDING));
      }
      final VectorCollection source = this.snapshotSource(it.getSnapshotName());
      VectorCollection _collection = it.getCollection();
      boolean _tripleEquals = (source == _collection);
      if (_tripleEquals) {
        throw Unsupported.of("restoring a snapshot into the collection it was taken from; Milvus restores only into a collection that does not exist");
      }
      final Code.Entries args = MilvusSchema.kw("snapshot_name", Code.text(it.getSnapshotName()), "collection_name", Code.text(it.getCollection().getName()));
      if ((source != null)) {
        args.put("source_collection_name", Code.text(source.getName()));
      }
      boolean _isAsync = it.isAsync();
      boolean _not = (!_isAsync);
      args.put("wait", Code.flag(_not));
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
        throw Unsupported.of(("snapshot locations (export to external storage)" + MilvusStatements.PENDING));
      }
      final Code.Entries args = MilvusSchema.kw("snapshot_name", Code.text(it.getSnapshotName()));
      final VectorCollection source = this.snapshotSource(it.getSnapshotName());
      if ((source != null)) {
        args.put("collection_name", Code.text(source.getName()));
      }
      Code.Stmt _run = MilvusStatements.run(Code.runtime("drop_snapshot", args));
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private void snapshotScope(final EObject statement, final ESnapshotScope requested) {
    if ((ModelUtil.written(statement, "scope") && (!Objects.equals(requested, ESnapshotScope.COLLECTION)))) {
      String _literal = requested.getLiteral();
      String _plus = (_literal + " snapshots; Milvus snapshots one collection at a time");
      throw Unsupported.of(_plus);
    }
  }

  /**
   * The collection a snapshot of this script was taken from; the runtime searches every collection otherwise.
   */
  private VectorCollection snapshotSource(final String snapshot) {
    Object _xblockexpression = null;
    {
      List<Snapshot> _allContentsOfType = EcoreUtil2.<Snapshot>getAllContentsOfType(this.script, Snapshot.class);
      for (final Snapshot taken : _allContentsOfType) {
        String _elvis = null;
        String _snapshotName = taken.getSnapshotName();
        if (_snapshotName != null) {
          _elvis = _snapshotName;
        } else {
          String _name = taken.getName();
          _elvis = _name;
        }
        boolean _equals = Objects.equals(_elvis, snapshot);
        if (_equals) {
          return taken.getCollection();
        }
      }
      _xblockexpression = null;
    }
    return ((VectorCollection)_xblockexpression);
  }

  /**
   * A materialized point-in-time copy: snapshot, restore under the new name, drop the snapshot.
   */
  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("cloning a logical database" + MilvusStatements.PENDING));
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; a Milvus restore copies the segment files");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("the \'as-of\' clause; Milvus snapshots record the current state only");
      }
      Code.Call _runtime = Code.runtime("clone_collection", MilvusSchema.kw("collection_name", Code.text(it.getSourceCollection().getName()), "target_name", Code.text(it.getTargetName())));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(("refreshing external collections" + MilvusStatements.PENDING));
  }

  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(it.getIndex());
      final Index definition = it.getIndex();
      final Code.Text name = Code.text(collection.getName());
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        Code.Stmt _run = MilvusStatements.run(Code.client("release_collection", MilvusSchema.kw("collection_name", name)));
        Code.Stmt _run_1 = MilvusStatements.run(Code.client("drop_index", MilvusSchema.kw("collection_name", name, "index_name", Code.text(((VectorIndex)definition).getName()))));
        Code.Stmt _createIndex = this.createIndex(collection, this.schema.vectorIndex(collection, this.vectorFieldOf(((VectorIndex)definition)), ((VectorIndex)definition)));
        Code.Stmt _run_2 = MilvusStatements.run(Code.client("load_collection", MilvusSchema.kw("collection_name", name)));
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _run_1, _createIndex, _run_2));
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            EPayloadIndexKind _kind = ((PayloadIndex)definition).getKind();
            boolean _equals = Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT);
            if (_equals) {
              throw Unsupported.of("rebuilding a full_text payload index; Milvus builds text match with the field");
            }
            Code.Stmt _run = MilvusStatements.run(Code.client("drop_index", MilvusSchema.kw("collection_name", name, "index_name", Code.text(((PayloadIndex)definition).getName()))));
            Code.Stmt _createIndex = this.createIndex(collection, this.schema.payloadIndex(((PayloadIndex)definition)));
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run, _createIndex));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        String _name = definition.eClass().getName();
        String _plus = (MilvusStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    Code.Stmt _run = MilvusStatements.run(Code.client("alter_alias", MilvusSchema.kw("collection_name", Code.text(it.getNewCollection().getName()), "alias", Code.text(it.getAlias().getName()))));
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _namespace = it.getNamespace();
      boolean _tripleEquals = (_namespace == null);
      if (_tripleEquals) {
        Code.Stmt _run = MilvusStatements.run(Code.client("truncate_collection", MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()))));
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
      }
      final MilvusFilter where = this.scopedFilter(it.getCollection(), null, it.getNamespace(), null);
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
        Code.Call _runtime = Code.runtime("delete_rows", args);
        Code.Return _return = new Code.Return(_runtime);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _return)));
      }
      args.put("primary_key", Code.text(this.primaryKey(it.getCollection())));
      this.partitionName(args, it.getCollection(), it.getNamespace(), null, null);
      Code.Call _runtime_1 = Code.runtime("delete_matching", args);
      Code.Return _return_1 = new Code.Return(_runtime_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final boolean slices = (Objects.equals(it.getKind(), EListKind.TENANTS) || Objects.equals(it.getKind(), EListKind.NAMESPACES));
      if (((slices && (it.getScope() instanceof VectorCollection)) && (ModelUtil.partitionKey(((VectorCollection) it.getScope())) != null))) {
        throw Unsupported.of("listing the tenants or namespaces of a partition-key collection; Milvus does not enumerate partition key values");
      }
      if ((slices && (!(it.getScope() instanceof VectorCollection)))) {
        throw Unsupported.of("listing tenants or namespaces without a collection; Milvus partitions belong to a collection");
      }
      final Code.Entries args = MilvusSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      if (slices) {
        Concept _scope = it.getScope();
        EListKind _kind = it.getKind();
        boolean _equals = Objects.equals(_kind, EListKind.TENANTS);
        args.put("only", Code.texts(this.sliceNames(((VectorCollection) _scope), _equals)));
      }
      Concept _scope_1 = it.getScope();
      boolean _tripleNotEquals = (_scope_1 != null);
      if (_tripleNotEquals) {
        Object _eGet = it.getScope().eGet(it.getScope().eClass().getEStructuralFeature("name"));
        args.put("scope", Code.text(((String) _eGet)));
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
      Code.Call _runtime = Code.runtime("list_objects", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * The tenants (or namespace keys) this script declares for a collection, inside it or in nested creates.
   */
  private List<String> sliceNames(final VectorCollection collection, final boolean tenants) {
    List<String> _xblockexpression = null;
    {
      final List<String> names = CollectionLiterals.<String>newArrayList();
      List<Concept> _allContentsOfType = EcoreUtil2.<Concept>getAllContentsOfType(this.script, Concept.class);
      for (final Concept concept : _allContentsOfType) {
        if ((((concept instanceof Tenant) && tenants) || ((concept instanceof Namespace) && (!tenants)))) {
          VectorCollection _xtrycatchfinallyexpression = null;
          try {
            VectorCollection _member = this.member(concept, "a slice");
            _xtrycatchfinallyexpression = _member;
          } catch (final Throwable _t) {
            if (_t instanceof Unsupported) {
              _xtrycatchfinallyexpression = null;
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          final VectorCollection owner = _xtrycatchfinallyexpression;
          if ((owner == collection)) {
            String _xifexpression = null;
            if ((concept instanceof Tenant)) {
              _xifexpression = ((Tenant) concept).getName();
            } else {
              _xifexpression = ((Namespace) concept).getKey();
            }
            names.add(_xifexpression);
          }
        }
      }
      _xblockexpression = names;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final AwaitIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final VectorCollection collection = this.indexCollection(it.getIndex());
      String _switchResult = null;
      Index _index = it.getIndex();
      final Index definition = _index;
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        _switchResult = ModelUtil.fieldName(this.vectorFieldOf(((VectorIndex)definition)));
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          _switchResult = MilvusFilter.path(((PayloadIndex)definition).getTarget());
        }
      }
      final String field = _switchResult;
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(collection.getName()), "index_name", Code.text(it.getIndex().getName()), "field_name", Code.text(field));
      boolean _written = ModelUtil.written(it, "state");
      if (_written) {
        args.put("until", Code.text(it.getState().getLiteral()));
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
      this.reject(it, "elementScope", "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", 
        "sortClauses", "aggregations", "prefetches", "exact");
      if (((it.getGroupBy() != null) && (it.getBoost() != null))) {
        throw Unsupported.of(("group-by together with boost" + MilvusStatements.PENDING));
      }
      this.reject(it, "queryModel");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Expr data = this.queryData(it.getInput(), it.getCollection(), field, "query_vector", prelude);
      String _xifexpression = null;
      boolean _written = ModelUtil.written(it, "metric");
      if (_written) {
        _xifexpression = this.schema.metricName(it.getMetric());
      } else {
        _xifexpression = this.schema.metric(it.getCollection(), field, ModelUtil.vectorIndex(it.getCollection(), field));
      }
      final String metric = _xifexpression;
      _xblockexpression = this.search(it, it.getCollection(), field, prelude, data, metric, this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant()), it.getBoost());
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final KeywordSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "fuzziness", "minShouldMatch");
      final Function1<SearchFieldRef, Boolean> _function = (SearchFieldRef it_1) -> {
        return Boolean.valueOf(ModelUtil.written(it_1, "boost"));
      };
      boolean _exists = IterableExtensions.<SearchFieldRef>exists(it.getSearchFields(), _function);
      if (_exists) {
        throw Unsupported.of("per-field boosts in keyword search; Milvus BM25 scores one sparse field per search, without field weights");
      }
      EQueryInputMode _kind = it.getInput().getKind();
      boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.TEXT));
      if (_notEquals) {
        String _literal = it.getInput().getKind().getLiteral();
        String _plus = ("keyword search with " + _literal);
        String _plus_1 = (_plus + " input; Milvus BM25 takes query text");
        throw Unsupported.of(_plus_1);
      }
      final Function1<SearchFieldRef, Field> _function_1 = (SearchFieldRef it_1) -> {
        return it_1.getField();
      };
      final Function function = this.bm25Function(it.getCollection(), ListExtensions.<SearchFieldRef, Field>map(it.getSearchFields(), _function_1));
      Field _head = IterableExtensions.<Field>head(function.getOutputs());
      final VectorField sparse = ((VectorField) _head);
      Field _xifexpression = null;
      boolean _isEmpty = it.getSearchFields().isEmpty();
      if (_isEmpty) {
        _xifexpression = IterableExtensions.<Field>head(function.getInputs());
      } else {
        _xifexpression = IterableExtensions.<SearchFieldRef>head(it.getSearchFields()).getField();
      }
      final String textField = ModelUtil.fieldName(_xifexpression);
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      boolean _written = ModelUtil.written(it, "textQueryType");
      if (_written) {
        ETextQueryType _textQueryType = it.getTextQueryType();
        if (_textQueryType != null) {
          switch (_textQueryType) {
            case MATCH:
              break;
            case PHRASE:
              where.add(this.phrase(textField, it.getInput().getValue(), where, it));
              break;
            case PHRASE_PREFIX:
              where.add(this.phrase(textField, it.getInput().getValue(), where, it));
              break;
            default:
              String _literal_1 = it.getTextQueryType().getLiteral();
              String _plus_2 = ("text-query " + _literal_1);
              String _plus_3 = (_plus_2 + "; Milvus keyword search matches terms, phrases and phrase prefixes");
              throw Unsupported.of(_plus_3);
          }
        } else {
          String _literal_1 = it.getTextQueryType().getLiteral();
          String _plus_2 = ("text-query " + _literal_1);
          String _plus_3 = (_plus_2 + "; Milvus keyword search matches terms, phrases and phrase prefixes");
          throw Unsupported.of(_plus_3);
        }
      }
      if ((ModelUtil.written(it, "matchOperator") && Objects.equals(it.getMatchOperator(), EMatchOperator.AND))) {
        Literal _value = it.getInput().getValue();
        boolean _not = (!(_value instanceof StringLit));
        if (_not) {
          throw Unsupported.of("operator \'and\' with a non-constant query");
        }
        Literal _value_1 = it.getInput().getValue();
        final Function1<String, String> _function_2 = (String it_1) -> {
          String _str = Py.str(it_1);
          String _plus_4 = ((("TEXT_MATCH(" + textField) + ", ") + _str);
          return (_plus_4 + ")");
        };
        where.add(IterableExtensions.join(ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(((StringLit) _value_1).getValue().trim().split("\\s+"))), _function_2), " and "));
      }
      _xblockexpression = this.search(it, it.getCollection(), sparse, CollectionLiterals.<Code.Stmt>newArrayList(), MilvusStatements.vector("text", Literals.of(it.getInput().getValue())), "BM25", where, it.getBoost());
    }
    return _xblockexpression;
  }

  private String phrase(final String field, final Literal text, final MilvusFilter where, final KeywordSearch search) {
    String _value = where.value(text);
    String _plus = ((("PHRASE_MATCH(" + field) + ", ") + _value);
    String _xifexpression = null;
    boolean _written = ModelUtil.written(search, "slop");
    if (_written) {
      int _slop = search.getSlop();
      _xifexpression = (", " + Integer.valueOf(_slop));
    } else {
      _xifexpression = "";
    }
    String _plus_1 = (_plus + _xifexpression);
    return (_plus_1 + ")");
  }

  private List<Code.Stmt> search(final EObject query, final VectorCollection collection, final VectorField field, final List<Code.Stmt> prelude, final Code.Expr data, final String metric, final MilvusFilter where, final BoostBlock boost) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final boolean nativeScore = (ModelUtil.written(query, "scoreForm") && Objects.equals(this.feature(query, "scoreForm"), EScoreForm.NATIVE));
      final LinkedHashMap<String, Code.Expr> knobs = new LinkedHashMap<String, Code.Expr>();
      Code.Ref _ref = new Code.Ref("results");
      final Code.Entries hits = MilvusSchema.kw("results", _ref, "metric", Code.text(metric), "primary_key", Code.text(this.primaryKey(collection)));
      if (nativeScore) {
        hits.put("native", Code.flag(true));
      }
      Object _feature = this.feature(query, "bounds");
      this.bounds(((ScoreBounds) _feature), metric, nativeScore, knobs, hits);
      Object _feature_1 = this.feature(query, "topK");
      final Code.Entries args = MilvusSchema.kw(
        "collection_name", Code.text(collection.getName()), 
        "data", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(data))), 
        "anns_field", Code.text(ModelUtil.fieldName(field)), 
        "limit", Code.num(((Integer) _feature_1).doubleValue()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      args.put("output_fields", this.outputFields(collection, query, true));
      Object _feature_2 = this.feature(query, "params");
      this.tuning(((SearchParams) _feature_2), this.schema.indexType(field, ModelUtil.vectorIndex(collection, field)), knobs, args);
      final Code.Entries searchParams = MilvusSchema.kw("metric_type", Code.text(metric));
      boolean _written = ModelUtil.written(query, "offset");
      if (_written) {
        Object _feature_3 = this.feature(query, "offset");
        searchParams.put("offset", Code.num(((Integer) _feature_3).doubleValue()));
      }
      boolean _isEmpty_1 = knobs.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        searchParams.put("params", Code.entries(knobs));
      }
      args.put("search_params", searchParams);
      this.partitionNames(args, collection, query);
      Object _feature_4 = this.feature(query, "groupBy");
      final FieldPath groupBy = ((FieldPath) _feature_4);
      if ((groupBy != null)) {
        args.put("group_by_field", Code.text(MilvusFilter.path(groupBy)));
        boolean _written_1 = ModelUtil.written(query, "groupSize");
        if (_written_1) {
          Object _feature_5 = this.feature(query, "groupSize");
          args.put("group_size", Code.num(((Integer) _feature_5).doubleValue()));
        }
      }
      this.consistency(args, query);
      if ((boost != null)) {
        args.put("ranker", this.ranker(boost));
      }
      this.extensions(args, query);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _client = Code.client("search", args);
      Code.Let _let = new Code.Let("results", _client);
      result.add(_let);
      Code.Call _runtime = Code.runtime("hits", hits);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "boost", "bounds", "params");
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final List<Code.Expr> requests = CollectionLiterals.<Code.Expr>newArrayList();
      final List<Double> weights = CollectionLiterals.<Double>newArrayList();
      int index = 0;
      EList<HybridLeg> _legs = it.getLegs();
      for (final HybridLeg leg : _legs) {
        {
          index = (index + 1);
          final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
          boolean _matched = false;
          if (leg instanceof VectorLeg) {
            _matched=true;
            this.reject(leg, "exact");
            final VectorField field = this.searchField(it.getCollection(), ((VectorLeg)leg).getVectorField());
            QueryInput _elvis = null;
            QueryInput _input = ((VectorLeg)leg).getInput();
            if (_input != null) {
              _elvis = _input;
            } else {
              QueryInput _input_1 = it.getInput();
              _elvis = _input_1;
            }
            final Code.Expr data = this.queryData(_elvis, it.getCollection(), field, ("query_vector_" + Integer.valueOf(index)), prelude);
            where.where(((VectorLeg)leg).getFilter());
            String _xifexpression = null;
            boolean _written = ModelUtil.written(leg, "metric");
            if (_written) {
              _xifexpression = this.schema.metricName(((VectorLeg)leg).getMetric());
            } else {
              _xifexpression = this.schema.metric(it.getCollection(), field, ModelUtil.vectorIndex(it.getCollection(), field));
            }
            final String metric = _xifexpression;
            final LinkedHashMap<String, Code.Expr> knobs = new LinkedHashMap<String, Code.Expr>();
            this.bounds(((VectorLeg)leg).getBounds(), metric, false, knobs, Code.entries());
            final Code.Entries unused = Code.entries();
            this.tuning(((VectorLeg)leg).getParams(), this.schema.indexType(field, ModelUtil.vectorIndex(it.getCollection(), field)), knobs, unused);
            boolean _has = unused.has("timeout");
            if (_has) {
              throw Unsupported.of("per-leg timeouts");
            }
            knobs.putAll(this.schema.extValues(leg));
            Code.Make _request = this.request(data, field, metric, knobs, ((VectorLeg)leg).getLegLimit(), ModelUtil.written(leg, "legLimit"), it.getTopK(), where);
            requests.add(_request);
            Double _xifexpression_1 = null;
            boolean _written_1 = ModelUtil.written(leg, "weight");
            if (_written_1) {
              _xifexpression_1 = Double.valueOf(((VectorLeg)leg).getWeight());
            } else {
              _xifexpression_1 = null;
            }
            weights.add(_xifexpression_1);
          }
          if (!_matched) {
            if (leg instanceof KeywordLeg) {
              _matched=true;
              this.reject(leg, "textQueryType", "matchOperator", "minShouldMatch");
              QueryInput _elvis = null;
              QueryInput _input = ((KeywordLeg)leg).getInput();
              if (_input != null) {
                _elvis = _input;
              } else {
                QueryInput _input_1 = it.getInput();
                _elvis = _input_1;
              }
              final QueryInput text = _elvis;
              if (((text == null) || (!Objects.equals(text.getKind(), EQueryInputMode.TEXT)))) {
                throw Unsupported.of("keyword leg without text input");
              }
              final Function1<SearchFieldRef, Field> _function = (SearchFieldRef it_1) -> {
                return it_1.getField();
              };
              final Function function = this.bm25Function(it.getCollection(), ListExtensions.<SearchFieldRef, Field>map(((KeywordLeg)leg).getSearchFields(), _function));
              where.where(((KeywordLeg)leg).getFilter());
              Code.Make _vector = MilvusStatements.vector("text", Literals.of(text.getValue()));
              Field _head = IterableExtensions.<Field>head(function.getOutputs());
              LinkedHashMap<String, Code.Expr> _linkedHashMap = new LinkedHashMap<String, Code.Expr>();
              Code.Make _request = this.request(_vector, ((VectorField) _head), "BM25", _linkedHashMap, ((KeywordLeg)leg).getLegLimit(), ModelUtil.written(leg, "legLimit"), it.getTopK(), where);
              requests.add(_request);
              Double _xifexpression = null;
              boolean _written = ModelUtil.written(leg, "weight");
              if (_written) {
                _xifexpression = Double.valueOf(((KeywordLeg)leg).getWeight());
              } else {
                _xifexpression = null;
              }
              weights.add(_xifexpression);
            }
          }
        }
      }
      final Code.Entries args = MilvusSchema.kw(
        "collection_name", Code.text(it.getCollection().getName()), 
        "reqs", Code.items(requests), 
        "ranker", this.fusion(it, weights), 
        "limit", Code.num(it.getTopK()), 
        "output_fields", this.outputFields(it.getCollection(), it, true));
      boolean _written = ModelUtil.written(it, "offset");
      if (_written) {
        args.put("offset", Code.num(it.getOffset()));
      }
      FieldPath _groupBy = it.getGroupBy();
      boolean _tripleNotEquals = (_groupBy != null);
      if (_tripleNotEquals) {
        args.put("group_by_field", Code.text(MilvusFilter.path(it.getGroupBy())));
        boolean _written_1 = ModelUtil.written(it, "groupSize");
        if (_written_1) {
          args.put("group_size", Code.num(it.getGroupSize()));
        }
      }
      this.partitionNames(args, it.getCollection(), it);
      this.consistency(args, it);
      this.extensions(args, it);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _client = Code.client("hybrid_search", args);
      Code.Let _let = new Code.Let("results", _client);
      result.add(_let);
      Code.Ref _ref = new Code.Ref("results");
      Code.Call _runtime = Code.runtime("hits", MilvusSchema.kw("results", _ref, "metric", Code.text("FUSED"), "native", Code.flag(true), 
        "primary_key", Code.text(this.primaryKey(it.getCollection()))));
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private Code.Make request(final Code.Expr data, final VectorField field, final String metric, final Map<String, Code.Expr> knobs, final int legLimit, final boolean limited, final int topK, final MilvusFilter where) {
    Code.Make _xblockexpression = null;
    {
      final Code.Entries param = MilvusSchema.kw("metric_type", Code.text(metric));
      boolean _isEmpty = knobs.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        param.put("params", Code.entries(knobs));
      }
      int _xifexpression = (int) 0;
      if (limited) {
        _xifexpression = legLimit;
      } else {
        _xifexpression = topK;
      }
      final Code.Entries args = MilvusSchema.kw(
        "data", Code.items(Collections.<Code.Expr>unmodifiableList(CollectionLiterals.<Code.Expr>newArrayList(data))), 
        "anns_field", Code.text(ModelUtil.fieldName(field)), 
        "param", param, 
        "limit", Code.num(_xifexpression));
      boolean _isEmpty_1 = where.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("expr", where.code());
      }
      _xblockexpression = Code.make("AnnRequest", args);
    }
    return _xblockexpression;
  }

  private Code.Make fusion(final HybridSearch search, final List<Double> weights) {
    Code.Make _xblockexpression = null;
    {
      EFusionType _xifexpression = null;
      boolean _written = ModelUtil.written(search, "fusion");
      if (_written) {
        _xifexpression = search.getFusion();
      } else {
        _xifexpression = EFusionType.RRF;
      }
      final EFusionType kind = _xifexpression;
      Code.Make _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case RRF:
            Code.Make _xblockexpression_1 = null;
            {
              final Code.Entries args = MilvusSchema.kw("strategy", Code.text("rrf"));
              boolean _written_1 = ModelUtil.written(search, "fusionConstant");
              if (_written_1) {
                args.put("k", Code.num(search.getFusionConstant()));
              }
              _xblockexpression_1 = Code.make("Ranker", args);
            }
            _switchResult = _xblockexpression_1;
            break;
          default:
            Code.Make _xblockexpression_2 = null;
            {
              final Function1<Double, Code.Num> _function = (Double it) -> {
                return Code.num((it).doubleValue());
              };
              final Code.Entries args = MilvusSchema.kw("strategy", Code.text("weighted"), "weights", Code.items(ListExtensions.<Double, Code.Num>map(this.fusionWeights(search, weights), _function)));
              boolean _equals = Objects.equals(kind, EFusionType.RELATIVE_SCORE);
              if (_equals) {
                args.put("norm_score", Code.flag(true));
              }
              _xblockexpression_2 = Code.make("Ranker", args);
            }
            _switchResult = _xblockexpression_2;
            break;
        }
      } else {
        Code.Make _xblockexpression_2 = null;
        {
          final Function1<Double, Code.Num> _function = (Double it) -> {
            return Code.num((it).doubleValue());
          };
          final Code.Entries args = MilvusSchema.kw("strategy", Code.text("weighted"), "weights", Code.items(ListExtensions.<Double, Code.Num>map(this.fusionWeights(search, weights), _function)));
          boolean _equals = Objects.equals(kind, EFusionType.RELATIVE_SCORE);
          if (_equals) {
            args.put("norm_score", Code.flag(true));
          }
          _xblockexpression_2 = Code.make("Ranker", args);
        }
        _switchResult = _xblockexpression_2;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Double> fusionWeights(final HybridSearch search, final List<Double> weights) {
    List<Double> _xblockexpression = null;
    {
      boolean _contains = weights.contains(null);
      boolean _not = (!_contains);
      if (_not) {
        return weights;
      }
      if ((ModelUtil.written(search, "alpha") && (weights.size() == 2))) {
        double _alpha = search.getAlpha();
        double _alpha_1 = search.getAlpha();
        double _minus = (1 - _alpha_1);
        return Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(_alpha), Double.valueOf(_minus)));
      }
      final Function1<Double, Double> _function = (Double it) -> {
        int _size = weights.size();
        return Double.valueOf((1.0 / _size));
      };
      _xblockexpression = IterableExtensions.<Double>toList(ListExtensions.<Double, Double>map(weights, _function));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RecommendSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "withTotal", "explain", "mmr", "reranker", "highlighted", "sortClauses", 
        "aggregations", "prefetches", "boost");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries examples = MilvusSchema.kw("positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)));
      boolean _isEmpty = it.getNegatives().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        examples.put("negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      }
      VectorCollection _elvis = null;
      VectorCollection _lookupCollection = it.getLookupCollection();
      if (_lookupCollection != null) {
        _elvis = _lookupCollection;
      } else {
        VectorCollection _collection = it.getCollection();
        _elvis = _collection;
      }
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(_elvis.getName()), "field", Code.text(ModelUtil.fieldName(field)));
      args.entries().putAll(examples.entries());
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      EList<Literal> _positives = it.getPositives();
      EList<Literal> _negatives = it.getNegatives();
      final Function1<Literal, Boolean> _function_2 = (Literal it_1) -> {
        return Boolean.valueOf((!(it_1 instanceof ListLit)));
      };
      boolean _exists = IterableExtensions.<Literal>exists(Iterables.<Literal>concat(_positives, _negatives), _function_2);
      if (_exists) {
        String _primaryKey = this.primaryKey(it.getCollection());
        String _plus = (_primaryKey + " not in ");
        String _placeholder = where.placeholder("examples", Code.runtime("example_ids", examples));
        String _plus_1 = (_plus + _placeholder);
        where.add(_plus_1);
      }
      Code.Call _runtime = Code.runtime("recommend_vector", args);
      Code.Let _let = new Code.Let("query_vector", _runtime);
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _let));
      String _denseKind = MilvusStatements.denseKind(field);
      Code.Ref _ref = new Code.Ref("query_vector");
      _xblockexpression = this.search(it, it.getCollection(), field, prelude, MilvusStatements.vector(_denseKind, _ref), 
        this.schema.metric(it.getCollection(), field, ModelUtil.vectorIndex(it.getCollection(), field)), where, null);
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
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)), 
        "output_fields", this.outputFields(it.getCollection(), it));
      this.partitionNames(args, it.getCollection(), it);
      this.consistency(args, it);
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
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.partitionNames(args, it.getCollection(), it);
      this.consistency(args, it);
      Code.Call _runtime = Code.runtime("count", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "cursor", "sliceIndex", "sortClauses", "asOf");
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()));
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      args.put("output_fields", this.outputFields(it.getCollection(), it));
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      boolean _written_1 = ModelUtil.written(it, "offset");
      if (_written_1) {
        args.put("offset", Code.num(it.getOffset()));
      }
      this.partitionNames(args, it.getCollection(), it);
      this.consistency(args, it);
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * An exact sample of n records: the runtime reads the matching primary keys and draws n uniformly (or takes the first
   * n in key order). Milvus RANDOM_SAMPLE(ratio) keeps an approximate fraction, not a count.
   */
  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "primary_key", Code.text(this.primaryKey(it.getCollection())), "size", Code.num(it.getSampleSize()));
      if ((ModelUtil.written(it, "method") && Objects.equals(it.getMethod(), ESampleMethod.FIRST))) {
        args.put("first", Code.flag(true));
      }
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      args.put("output_fields", this.outputFields(it.getCollection(), it));
      this.partitionNames(args, it.getCollection(), it);
      this.consistency(args, it);
      Code.Call _runtime = Code.runtime("sample_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    throw Unsupported.of(("generative search; " + MilvusStatements.NO_GENERATION));
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final MilvusFilter where = this.scopedFilter(it.getCollection(), it.getFilter(), it.getNamespace(), it.getTenant());
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(it.getCollection().getName()), "field", Code.text(ModelUtil.fieldName(field)), 
        "metric", Code.text(this.schema.metric(it.getCollection(), field, ModelUtil.vectorIndex(it.getCollection(), field))), 
        "primary_key", Code.text(this.primaryKey(it.getCollection())));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.consistency(args, it);
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
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
    String _plus = (MilvusStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private static Code.Stmt run(final Code.Expr expr) {
    return new Code.Run(expr);
  }

  private static Code.Make vector(final String kind, final Code.Expr value) {
    return Code.make("Vector", MilvusSchema.kw("kind", Code.text(kind), "value", value));
  }

  private static String denseKind(final VectorField field) {
    String _switchResult = null;
    EVectorDType _type = field.getType();
    if (_type != null) {
      switch (_type) {
        case SPARSE_FLOAT_VECTOR:
          _switchResult = "sparse";
          break;
        case BINARY_VECTOR:
          _switchResult = "binary";
          break;
        default:
          _switchResult = "dense";
          break;
      }
    } else {
      _switchResult = "dense";
    }
    return _switchResult;
  }

  /**
   * Raises for the first written clause: clauses Milvus serves but the generator does not emit yet name themselves as
   * pending, the others give the reason Milvus cannot express them.
   */
  private void reject(final EObject statement, final String... features) {
    for (final String feature : features) {
      boolean _written = ModelUtil.written(statement, feature);
      if (_written) {
        String _elvis = null;
        String _get = MilvusStatements.CLAUSES.get(feature);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = feature;
        }
        String _plus = ("the \'" + _elvis);
        final String clause = (_plus + "\' clause");
        boolean _contains = MilvusStatements.NATIVE_PENDING.contains(feature);
        if (_contains) {
          throw Unsupported.of((clause + MilvusStatements.PENDING));
        }
        final String reason = MilvusStatements.REASONS.get(feature);
        String _xifexpression = null;
        if ((reason == null)) {
          _xifexpression = "";
        } else {
          _xifexpression = ("; " + reason);
        }
        String _plus_1 = (clause + _xifexpression);
        throw Unsupported.of(_plus_1);
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
        return explicit;
      }
      final Function1<VectorField, Boolean> _function = (VectorField it) -> {
        boolean _xblockexpression_1 = false;
        {
          final Function function = ModelUtil.producingFunction(collection, it);
          _xblockexpression_1 = ((function == null) || (!Objects.equals(function.getType(), EFunctionType.BM25)));
        }
        return Boolean.valueOf(_xblockexpression_1);
      };
      final List<VectorField> candidates = IterableExtensions.<VectorField>toList(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(collection), _function));
      int _size = candidates.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        throw Unsupported.of("choosing among several vector fields; add \'using <field>\'");
      }
      _xblockexpression = IterableExtensions.<VectorField>head(candidates);
    }
    return _xblockexpression;
  }

  private Function bm25Function(final VectorCollection collection, final List<Field> fields) {
    Function _xblockexpression = null;
    {
      final Function1<Function, Boolean> _function = (Function it) -> {
        return Boolean.valueOf((Objects.equals(it.getType(), EFunctionType.BM25) && (fields.isEmpty() || IterableExtensions.<Field>exists(it.getInputs(), ((Function1<Field, Boolean>) (Field it_1) -> {
          return Boolean.valueOf(fields.contains(it_1));
        })))));
      };
      final Function function = IterableExtensions.<Function>findFirst(collection.getFunctions(), _function);
      if ((function == null)) {
        throw Unsupported.of("keyword search without a bm25 function on the searched fields");
      }
      _xblockexpression = function;
    }
    return _xblockexpression;
  }

  private Code.Expr queryData(final QueryInput input, final VectorCollection collection, final VectorField field, final String variable, final List<Code.Stmt> prelude) {
    Code.Make _xblockexpression = null;
    {
      if ((input == null)) {
        throw Unsupported.of("a vector query without input");
      }
      Code.Make _switchResult = null;
      EQueryInputMode _kind = input.getKind();
      if (_kind != null) {
        switch (_kind) {
          case VECTOR:
            _switchResult = MilvusStatements.vector(MilvusStatements.denseKind(field), Literals.of(input.getValue()));
            break;
          case SPARSE_VECTOR:
            _switchResult = MilvusStatements.vector("sparse", Literals.of(input.getValue()));
            break;
          case TEXT:
            Code.Make _xblockexpression_1 = null;
            {
              Function _producingFunction = ModelUtil.producingFunction(collection, field);
              boolean _tripleEquals = (_producingFunction == null);
              if (_tripleEquals) {
                String _fieldName = ModelUtil.fieldName(field);
                String _plus = ("text input for " + _fieldName);
                String _plus_1 = (_plus + ", which has no server-side embedding or bm25 function");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = MilvusStatements.vector("text", Literals.of(input.getValue()));
            }
            _switchResult = _xblockexpression_1;
            break;
          case ID:
            Code.Make _xblockexpression_2 = null;
            {
              VectorCollection _elvis = null;
              VectorCollection _lookupCollection = input.getLookupCollection();
              if (_lookupCollection != null) {
                _elvis = _lookupCollection;
              } else {
                _elvis = collection;
              }
              final VectorCollection source = _elvis;
              Code.Call _runtime = Code.runtime("lookup_vector", MilvusSchema.kw("collection_name", Code.text(source.getName()), 
                "field", Code.text(ModelUtil.fieldName(field)), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let(variable, _runtime);
              prelude.add(_let);
              String _denseKind = MilvusStatements.denseKind(field);
              Code.Ref _ref = new Code.Ref(variable);
              _xblockexpression_2 = MilvusStatements.vector(_denseKind, _ref);
            }
            _switchResult = _xblockexpression_2;
            break;
          default:
            String _literal = input.getKind().getLiteral();
            String _plus = (_literal + " query input");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = input.getKind().getLiteral();
        String _plus = (_literal + " query input");
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private void bounds(final ScoreBounds bounds, final String metric, final boolean nativeScore, final Map<String, Code.Expr> knobs, final Code.Entries hits) {
    if ((bounds == null)) {
      return;
    }
    final boolean hasMin = ModelUtil.written(bounds, "min");
    final boolean hasMax = ModelUtil.written(bounds, "max");
    final boolean larger = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("COSINE", "IP", "BM25")).contains(metric);
    if (larger) {
      if ((!hasMin)) {
        if (hasMax) {
          hits.put("max_score", Code.num(bounds.getMax()));
        }
        return;
      }
      knobs.put("radius", Code.num(bounds.getMin()));
      if (hasMax) {
        knobs.put("range_filter", Code.num(bounds.getMax()));
      }
      return;
    }
    if (nativeScore) {
      if ((!hasMax)) {
        throw Unsupported.of("a lower distance bound without an upper bound");
      }
      knobs.put("radius", Code.num(bounds.getMax()));
      if (hasMin) {
        knobs.put("range_filter", Code.num(bounds.getMin()));
      }
      return;
    }
    if ((!hasMin)) {
      if (hasMax) {
        hits.put("max_score", Code.num(bounds.getMax()));
      }
      return;
    }
    knobs.put("radius", Code.num(this.distance(metric, bounds.getMin())));
    if (hasMax) {
      knobs.put("range_filter", Code.num(this.distance(metric, bounds.getMax())));
    }
  }

  private double distance(final String metric, final double similarity) {
    double _switchResult = (double) 0;
    if (metric != null) {
      switch (metric) {
        case "L2":
          double _xblockexpression = (double) 0;
          {
            if ((similarity > 0)) {
              throw Unsupported.of("a positive similarity bound on euclidean distance");
            }
            _xblockexpression = (similarity * similarity);
          }
          _switchResult = _xblockexpression;
          break;
        case "HAMMING":
          _switchResult = (-similarity);
          break;
        case "JACCARD":
          _switchResult = (1 - similarity);
          break;
        default:
          _switchResult = similarity;
          break;
      }
    } else {
      _switchResult = similarity;
    }
    return _switchResult;
  }

  private void tuning(final SearchParams params, final String indexType, final Map<String, Code.Expr> knobs, final Code.Entries args) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall: Milvus exposes no query-time recall target");
    }
    boolean _written_1 = ModelUtil.written(params, "candidatePool");
    if (_written_1) {
      String _xifexpression = null;
      boolean _startsWith = indexType.startsWith("HNSW");
      if (_startsWith) {
        _xifexpression = "ef";
      } else {
        String _xifexpression_1 = null;
        boolean _equals = Objects.equals(indexType, "DISKANN");
        if (_equals) {
          _xifexpression_1 = "search_list";
        } else {
          String _xifexpression_2 = null;
          boolean _equals_1 = Objects.equals(indexType, "GPU_CAGRA");
          if (_equals_1) {
            _xifexpression_2 = "itopk_size";
          } else {
            _xifexpression_2 = null;
          }
          _xifexpression_1 = _xifexpression_2;
        }
        _xifexpression = _xifexpression_1;
      }
      final String key = _xifexpression;
      if ((key == null)) {
        this.notes.add(("candidates for index type " + indexType));
      } else {
        knobs.put(key, Code.num(params.getCandidatePool()));
      }
    }
    boolean _written_2 = ModelUtil.written(params, "probes");
    if (_written_2) {
      boolean _contains = indexType.contains("IVF");
      if (_contains) {
        knobs.put("nprobe", Code.num(params.getProbes()));
      } else {
        this.notes.add(("probes for index type " + indexType));
      }
    }
    boolean _written_3 = ModelUtil.written(params, "timeoutMillis");
    if (_written_3) {
      int _timeoutMillis = params.getTimeoutMillis();
      double _divide = (_timeoutMillis / 1000.0);
      args.put("timeout", Code.num(_divide));
    }
    this.schema.note(params, "scanBudget", "oversampling", "rescore", "filterStrategy", "exhaustive");
    knobs.putAll(this.schema.extValues(params));
  }

  private Code.Make ranker(final BoostBlock boost) {
    Code.Make _xblockexpression = null;
    {
      int _size = boost.getFunctions().size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        throw Unsupported.of("boosting with several functions; a Milvus function score takes a single function");
      }
      if ((ModelUtil.written(boost, "combine") || ModelUtil.written(boost, "apply"))) {
        throw Unsupported.of(("boost combine and apply modes" + MilvusStatements.PENDING));
      }
      final BoostFunction function = IterableExtensions.<BoostFunction>head(boost.getFunctions());
      if (((!Objects.equals(function.getKind(), EBoostKind.DECAY)) || (function.getTarget() == null))) {
        String _literal = function.getKind().getLiteral();
        String _plus = (_literal + " boost functions");
        String _plus_1 = (_plus + MilvusStatements.PENDING);
        throw Unsupported.of(_plus_1);
      }
      if ((ModelUtil.written(function, "weight") || (function.getCondition() != null))) {
        throw Unsupported.of(("weighted or conditional decay boosts" + MilvusStatements.PENDING));
      }
      String _elvis = null;
      String _name = function.getName();
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = "decay";
      }
      final Code.Entries args = MilvusSchema.kw(
        "strategy", Code.text("decay"), 
        "name", Code.text(_elvis), 
        "field", Code.text(MilvusFilter.path(function.getTarget())), 
        "function", Code.text(function.getShape().getLiteral()), 
        "origin", this.timeValue(function.getOrigin()), 
        "scale", this.timeValue(function.getScale()));
      Literal _decayOffset = function.getDecayOffset();
      boolean _tripleNotEquals = (_decayOffset != null);
      if (_tripleNotEquals) {
        args.put("offset", this.timeValue(function.getDecayOffset()));
      }
      boolean _written = ModelUtil.written(function, "decayRate");
      if (_written) {
        args.put("decay", Code.num(function.getDecayRate()));
      }
      _xblockexpression = Code.make("Ranker", args);
    }
    return _xblockexpression;
  }

  private Code.Expr timeValue(final Literal literal) {
    Code.Expr _xifexpression = null;
    if ((literal instanceof NumberLit)) {
      _xifexpression = Literals.of(literal);
    } else {
      _xifexpression = Code.runtime("time_value", MilvusSchema.kw("value", Literals.of(literal)));
    }
    return _xifexpression;
  }

  private Code.Expr outputFields(final VectorCollection collection, final EObject query) {
    return this.outputFields(collection, query, false);
  }

  /**
   * Searches also request the primary key, which some SDKs omit from hits unless it is an output field.
   */
  private Code.Expr outputFields(final VectorCollection collection, final EObject query, final boolean withKey) {
    Code.Items _xblockexpression = null;
    {
      Object _feature = this.feature(query, "output");
      final OutputSpec output = ((OutputSpec) _feature);
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
      final boolean withVectors = Objects.equals(Boolean.TRUE, _feature_2);
      final List<String> names = CollectionLiterals.<String>newArrayList();
      if (withKey) {
        String _primaryKey = this.primaryKey(collection);
        names.add(_primaryKey);
      }
      if (((output == null) || output.isAllFields())) {
        final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
          boolean _contains = excluded.contains(it);
          return Boolean.valueOf((!_contains));
        };
        final Function1<PayloadField, String> _function_1 = (PayloadField it) -> {
          return it.getName();
        };
        Iterable<String> _map = IterableExtensions.<PayloadField, String>map(IterableExtensions.<PayloadField>filter(Iterables.<PayloadField>filter(collection.getFields(), PayloadField.class), _function), _function_1);
        Iterables.<String>addAll(names, _map);
      } else {
        final Function1<Field, Boolean> _function_2 = (Field it) -> {
          boolean _contains = excluded.contains(it);
          return Boolean.valueOf((!_contains));
        };
        final Function1<Field, String> _function_3 = (Field it) -> {
          return ModelUtil.fieldName(it);
        };
        Iterable<String> _map_1 = IterableExtensions.<Field, String>map(IterableExtensions.<Field>filter(output.getFields(), _function_2), _function_3);
        Iterables.<String>addAll(names, _map_1);
      }
      if (withVectors) {
        final Function1<VectorField, Boolean> _function_4 = (VectorField it) -> {
          boolean _xblockexpression_1 = false;
          {
            final Function function = ModelUtil.producingFunction(collection, it);
            _xblockexpression_1 = ((function == null) || (!Objects.equals(function.getType(), EFunctionType.BM25)));
          }
          return Boolean.valueOf(_xblockexpression_1);
        };
        final Function1<VectorField, String> _function_5 = (VectorField it) -> {
          return ModelUtil.fieldName(it);
        };
        Iterable<String> _map_2 = IterableExtensions.<VectorField, String>map(IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(collection), _function_4), _function_5);
        Iterables.<String>addAll(names, _map_2);
      }
      _xblockexpression = Code.texts(names.stream().distinct().toList());
    }
    return _xblockexpression;
  }

  private MilvusFilter scopedFilter(final VectorCollection collection, final FilterBlock block, final String namespace, final Tenant tenant) {
    MilvusFilter _xblockexpression = null;
    {
      final MilvusFilter where = new MilvusFilter().where(block);
      final Field key = ModelUtil.partitionKey(collection);
      String _elvis = null;
      String _name = null;
      if (tenant!=null) {
        _name=tenant.getName();
      }
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = namespace;
      }
      final String slice = _elvis;
      if (((slice != null) && (key != null))) {
        String _fieldName = ModelUtil.fieldName(key);
        String _plus = (_fieldName + " == ");
        String _str = Py.str(slice);
        String _plus_1 = (_plus + _str);
        where.add(_plus_1);
      }
      _xblockexpression = where;
    }
    return _xblockexpression;
  }

  private void partitionNames(final Code.Entries args, final VectorCollection collection, final EObject query) {
    Object _elvis = null;
    Object _feature = this.feature(query, "partitions");
    if (_feature != null) {
      _elvis = _feature;
    } else {
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      _elvis = _newArrayList;
    }
    final List<Partition> partitions = ((List<Partition>) _elvis);
    final List<String> names = CollectionLiterals.<String>newArrayList();
    final Function1<Partition, String> _function = (Partition it) -> {
      return it.getName();
    };
    List<String> _map = ListExtensions.<Partition, String>map(partitions, _function);
    Iterables.<String>addAll(names, _map);
    Object _feature_1 = this.feature(query, "tenant");
    final Tenant tenant = ((Tenant) _feature_1);
    String _elvis_1 = null;
    String _name = null;
    if (tenant!=null) {
      _name=tenant.getName();
    }
    if (_name != null) {
      _elvis_1 = _name;
    } else {
      Object _feature_2 = this.feature(query, "namespace");
      _elvis_1 = ((String) _feature_2);
    }
    final String slice = _elvis_1;
    if (((slice != null) && (ModelUtil.partitionKey(collection) == null))) {
      names.add(slice);
    }
    boolean _isEmpty = names.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("partition_names", Code.texts(names));
    }
  }

  private void partitionName(final Code.Entries args, final VectorCollection collection, final String namespace, final Tenant tenant, final Partition partition) {
    String _elvis = null;
    String _name = null;
    if (tenant!=null) {
      _name=tenant.getName();
    }
    if (_name != null) {
      _elvis = _name;
    } else {
      _elvis = namespace;
    }
    final String slice = _elvis;
    final Field key = ModelUtil.partitionKey(collection);
    if (((partition != null) && (key != null))) {
      throw Unsupported.of("explicit partitions together with a partition key");
    }
    if ((((partition != null) && (slice != null)) && (key == null))) {
      throw Unsupported.of("a partition together with a tenant or namespace");
    }
    if ((partition != null)) {
      args.put("partition_name", Code.text(partition.getName()));
    } else {
      if (((slice != null) && (key == null))) {
        args.put("partition_name", Code.text(slice));
      }
    }
  }

  private void writeScope(final Code.Entries args, final VectorCollection collection, final String namespace, final Tenant tenant, final Partition partition) {
    final Field key = ModelUtil.partitionKey(collection);
    String _elvis = null;
    String _name = null;
    if (tenant!=null) {
      _name=tenant.getName();
    }
    if (_name != null) {
      _elvis = _name;
    } else {
      _elvis = namespace;
    }
    final String slice = _elvis;
    if (((slice != null) && (key != null))) {
      String _fieldName = ModelUtil.fieldName(key);
      Code.Text _text = Code.text(slice);
      Pair<String, Code.Text> _mappedTo = Pair.<String, Code.Text>of(_fieldName, _text);
      args.put("defaults", Code.entries(Collections.<String, Code.Text>unmodifiableMap(CollectionLiterals.<String, Code.Text>newHashMap(_mappedTo))));
      this.partitionName(args, collection, null, null, partition);
    } else {
      this.partitionName(args, collection, namespace, tenant, partition);
    }
  }

  private void consistency(final Code.Entries args, final EObject query) {
    boolean _written = ModelUtil.written(query, "consistency");
    if (_written) {
      Object _feature = this.feature(query, "consistency");
      args.put("consistency_level", this.schema.consistency(((EConsistencyLevel) _feature)));
    }
  }

  private void extensions(final Code.Entries args, final EObject host) {
    final Map<String, Code.Expr> values = this.schema.extValues(host);
    boolean _isEmpty = values.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("extensions", Code.entries(values));
    }
  }

  private Code.Expr rows(final RecordSource source) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      _switchResult = Code.runtime("read_records", MilvusSchema.kw("location", Code.text(((FileSource)source).getLocation())));
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
        result.put(ModelUtil.fieldName(entry.getField()), Literals.of(entry.getValue()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String primaryKey(final VectorCollection collection) {
    return ModelUtil.fieldName(collection.getPrimaryKey());
  }

  private VectorField vectorFieldOf(final VectorIndex index) {
    VectorField _elvis = null;
    VectorField _field = index.getField();
    if (_field != null) {
      _elvis = _field;
    } else {
      VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(index, VectorField.class);
      _elvis = _containerOfType;
    }
    return _elvis;
  }

  private VectorCollection indexCollection(final Index index) {
    VectorCollection _xblockexpression = null;
    {
      final VectorCollection owner = ModelUtil.owningCollection(index);
      if ((owner != null)) {
        return owner;
      }
      Field _switchResult = null;
      boolean _matched = false;
      if (index instanceof VectorIndex) {
        _matched=true;
        _switchResult = ((VectorIndex)index).getField();
      }
      if (!_matched) {
        if (index instanceof PayloadIndex) {
          _matched=true;
          _switchResult = ((PayloadIndex)index).getTarget().getField();
        }
      }
      final Field field = _switchResult;
      VectorCollection _xifexpression = null;
      if ((field == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = ModelUtil.owningCollection(field);
      }
      final VectorCollection collection = _xifexpression;
      if ((collection == null)) {
        throw Unsupported.of("an index whose field belongs to no collection");
      }
      _xblockexpression = collection;
    }
    return _xblockexpression;
  }

  private String defaultDatabase() {
    String _elvis = null;
    final Function1<Connection, String> _function = (Connection it) -> {
      return it.getDatabaseName();
    };
    String _head = IterableExtensions.<String>head(IterableExtensions.<String>filterNull(ListExtensions.<Connection, String>map(EcoreUtil2.<Connection>getAllContentsOfType(this.script, Connection.class), _function)));
    if (_head != null) {
      _elvis = _head;
    } else {
      _elvis = "default";
    }
    return _elvis;
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
