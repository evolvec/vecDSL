package org.ftn.vecDSL.generator.weaviate;

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
import org.ftn.vecDSL.generator.code.Syntax;
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
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.ESnapshotScope;
import org.ftn.vecDSL.vecDSL.ETextQueryType;
import org.ftn.vecDSL.vecDSL.EUpdateMode;
import org.ftn.vecDSL.vecDSL.EWriteConsistency;
import org.ftn.vecDSL.vecDSL.EmbeddingMigration;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.FileResource;
import org.ftn.vecDSL.vecDSL.FileSource;
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

/**
 * Maps each vecDSL statement to Weaviate operations in the host-language-neutral code model. Every operation is
 * a call into the per-language runtime module, which holds the SDK calls; arguments are plain data (the
 * collection layout, records, filter trees), so the three host languages run the same Weaviate requests.
 */
@SuppressWarnings("all")
public class WeaviateStatements {
  /**
   * Message of the guard that only an unmapped vecDSL class reaches; WeaviateCoverageTest asserts it never fires.
   */
  public static final String NOT_MAPPED = "not mapped for Weaviate: ";

  private static final String LOGICAL_DATABASES = "logical databases; Weaviate namespaces need a license key and NAMESPACES_ENABLED, and no client SDK manages them";

  private static final String METRICS = "metrics configuration; Weaviate exports Prometheus metrics through server environment variables";

  private static final Map<String, String> BACKENDS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("filesystem", "filesystem"), Pair.<String, String>of("file", "filesystem"), Pair.<String, String>of("s3", "s3"), Pair.<String, String>of("gs", "gcs"), Pair.<String, String>of("gcs", "gcs"), Pair.<String, String>of("azure", "azure")));

  private static final Map<String, List<String>> PRIVILEGES = Collections.<String, List<String>>unmodifiableMap(CollectionLiterals.<String, List<String>>newHashMap(Pair.<String, List<String>>of("read", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("read_collections", "read_data"))), Pair.<String, List<String>>of("write", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("create_data", "update_data", "delete_data"))), Pair.<String, List<String>>of("insert", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("create_data"))), Pair.<String, List<String>>of("update", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("update_data"))), Pair.<String, List<String>>of("delete", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("delete_data"))), Pair.<String, List<String>>of("manage", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("create_collections", "read_collections", "update_collections", "delete_collections"))), Pair.<String, List<String>>of("all", Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("create_collections", "read_collections", "update_collections", "delete_collections", "create_data", "read_data", "update_data", "delete_data")))));

  private static final Map<String, String> CLAUSES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "within"), Pair.<String, String>of("asOf", "as-of"), Pair.<String, String>of("cursor", "cursor"), Pair.<String, String>of("withTotal", "with-total"), Pair.<String, String>of("explain", "explain"), Pair.<String, String>of("mmr", "mmr"), Pair.<String, String>of("reranker", "rerank"), Pair.<String, String>of("highlighted", "highlight"), Pair.<String, String>of("sortClauses", "sort"), Pair.<String, String>of("aggregations", "aggregate"), Pair.<String, String>of("prefetches", "prefetch"), Pair.<String, String>of("offset", "offset"), Pair.<String, String>of("groupBy", "group-by"), Pair.<String, String>of("sliceIndex", "slice"), Pair.<String, String>of("fuzziness", "fuzziness"), Pair.<String, String>of("slop", "slop"), Pair.<String, String>of("minShouldMatch", "min-should-match"), Pair.<String, String>of("exact", "exact"), Pair.<String, String>of("boost", "boost"), Pair.<String, String>of("queryModel", "model"), Pair.<String, String>of("bounds", "bounds"), Pair.<String, String>of("params", "params"), Pair.<String, String>of("scoreForm", "score")));

  /**
   * Why Weaviate cannot express a query clause.
   */
  private static final Map<String, String> REASONS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("elementScope", "Weaviate searches whole objects, not array elements"), Pair.<String, String>of("asOf", "Weaviate keeps no object history"), Pair.<String, String>of("cursor", "Weaviate pages search results by offset only"), Pair.<String, String>of("highlighted", "Weaviate returns no highlights"), Pair.<String, String>of("sortClauses", "Weaviate sorts object listings, not search results"), Pair.<String, String>of("prefetches", "Weaviate runs single-stage searches"), Pair.<String, String>of("exact", "Weaviate has no per-query exact search (a flat index is always exhaustive)"), Pair.<String, String>of("explain", "Weaviate explains keyword and hybrid scores only"), Pair.<String, String>of("fuzziness", "Weaviate BM25 has no fuzzy matching"), Pair.<String, String>of("slop", "Weaviate BM25 has no phrase slop"), Pair.<String, String>of("sliceIndex", "Weaviate has no parallel scroll slices"), Pair.<String, String>of("queryModel", "Weaviate embeds query text with the vectorizer of the named vector"), Pair.<String, String>of("bounds", "Weaviate bounds hybrid results only through the vector leg"), Pair.<String, String>of("params", "Weaviate takes no search parameters on a hybrid query")));

  private final Script script;

  private final List<String> notes = CollectionLiterals.<String>newArrayList();

  private final WeaviateSchema schema = new WeaviateSchema(this.notes);

  private final List<Program.Method> helpers = CollectionLiterals.<Program.Method>newArrayList();

  private String current;

  public WeaviateStatements(final Script script) {
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

  private List<Code.Stmt> define(final Concept concept, final boolean ifNotExists) {
    List<Code.Stmt> _switchResult = null;
    boolean _matched = false;
    if (concept instanceof VectorCollection) {
      _matched=true;
      _switchResult = this.schema.createCollection(((VectorCollection)concept), ifNotExists);
    }
    if (!_matched) {
      if (concept instanceof Connection) {
        _matched=true;
        if ((((((Connection)concept).getLocalPath() != null) || ((Connection)concept).isInMemory()) || Objects.equals(((Connection)concept).getDeployment(), EDeploymentMode.EMBEDDED))) {
          throw Unsupported.of("embedded deployments; the generated programs connect to a Weaviate server");
        }
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof VectorDatabase) {
        _matched=true;
        _switchResult = this.database(((VectorDatabase)concept), ifNotExists);
      }
    }
    if (!_matched) {
      if (concept instanceof Cluster) {
        _matched=true;
        List<Code.Stmt> _xblockexpression = null;
        {
          final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
          EList<VectorCollection> _collections = ((Cluster)concept).getCollections();
          for (final VectorCollection collection : _collections) {
            List<Code.Stmt> _createCollection = this.schema.createCollection(collection, ifNotExists);
            Iterables.<Code.Stmt>addAll(result, _createCollection);
          }
          _xblockexpression = result;
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (concept instanceof Database) {
        _matched=true;
        throw Unsupported.of(WeaviateStatements.LOGICAL_DATABASES);
      }
    }
    if (!_matched) {
      if (concept instanceof Partition) {
        _matched=true;
        throw Unsupported.of(WeaviateSchema.PARTITIONS);
      }
    }
    if (!_matched) {
      if (concept instanceof Tenant) {
        _matched=true;
        _switchResult = this.createTenants(this.owner(concept), WeaviateSchema.tenantName(((Tenant)concept).getName()), ifNotExists);
      }
    }
    if (!_matched) {
      if (concept instanceof Namespace) {
        _matched=true;
        _switchResult = this.createTenants(this.owner(concept), WeaviateSchema.tenantName(((Namespace)concept).getKey()), ifNotExists);
      }
    }
    if (!_matched) {
      if (concept instanceof Alias) {
        _matched=true;
        _switchResult = this.createAlias(((Alias)concept), ((Alias)concept).getCollection(), ifNotExists);
      }
    }
    if (!_matched) {
      if (concept instanceof Field) {
        _matched=true;
        _switchResult = this.addField(this.owner(concept), ((Field)concept));
      }
    }
    if (!_matched) {
      if (concept instanceof PayloadIndex) {
        _matched=true;
        _switchResult = this.createPayloadIndex(((PayloadIndex)concept));
      }
    }
    if (!_matched) {
      if (concept instanceof VectorIndex) {
        _matched=true;
        throw Unsupported.of("creating a vector index on an existing collection; Weaviate fixes the index type of a named vector when it creates the vector");
      }
    }
    if (!_matched) {
      if (concept instanceof Function) {
        _matched=true;
        _switchResult = this.addFunction(this.owner(concept), ((Function)concept));
      }
    }
    if (!_matched) {
      if (concept instanceof EmbeddingModel) {
        _matched=true;
        this.schema.vectorizerModule(((EmbeddingModel)concept));
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof GenerativeModel) {
        _matched=true;
        this.schema.generativeModule(((GenerativeModel)concept));
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof Reranker) {
        _matched=true;
        this.schema.rerankerModule(((Reranker)concept));
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof Analyzer) {
        _matched=true;
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof AnalyzerFilter) {
        _matched=true;
        return CollectionLiterals.<Code.Stmt>newArrayList();
      }
    }
    if (!_matched) {
      if (concept instanceof FileResource) {
        _matched=true;
        throw Unsupported.of("file resources; Weaviate takes stopword lists and tokenizer dictionaries inline in the collection definition");
      }
    }
    if (!_matched) {
      if (concept instanceof Quantization) {
        _matched=true;
        throw Unsupported.of("quantization outside a collection definition; vecDSL reaches it only inside a vector-index create, which Weaviate cannot apply to an existing collection");
      }
    }
    if (!_matched) {
      if (concept instanceof ReplicationConfig) {
        _matched=true;
        throw Unsupported.of("replication for an existing collection; Weaviate fixes the replication factor at creation");
      }
    }
    if (!_matched) {
      if (concept instanceof ShardConfig) {
        _matched=true;
        throw Unsupported.of("sharding for an existing collection; Weaviate fixes sharding at creation");
      }
    }
    if (!_matched) {
      if (concept instanceof StorageConfig) {
        _matched=true;
        throw Unsupported.of("storage for an existing collection; Weaviate persists every collection on disk");
      }
    }
    if (!_matched) {
      if (concept instanceof AuthConfig) {
        _matched=true;
        _switchResult = this.auth(((AuthConfig)concept));
      }
    }
    if (!_matched) {
      if (concept instanceof RoleBinding) {
        _matched=true;
        _switchResult = this.createRole(((RoleBinding)concept), ifNotExists);
      }
    }
    if (!_matched) {
      if (concept instanceof MetricsConfig) {
        _matched=true;
        throw Unsupported.of(WeaviateStatements.METRICS);
      }
    }
    if (!_matched) {
      if (concept instanceof BackupSchedule) {
        _matched=true;
        throw Unsupported.of("backup schedules; Weaviate creates backups only on request");
      }
    }
    if (!_matched) {
      if (concept instanceof ClusterLimit) {
        _matched=true;
        throw Unsupported.of("cluster limits; Weaviate sets them only through server environment variables");
      }
    }
    if (!_matched) {
      if (concept instanceof CollectionLimit) {
        _matched=true;
        throw Unsupported.of("collection limits; Weaviate has no per-collection capacity limits");
      }
    }
    if (!_matched) {
      if (concept instanceof FieldLimit) {
        _matched=true;
        throw Unsupported.of("field limits on an existing field; Weaviate stores no field limits, the runtime checks the declared ones");
      }
    }
    if (!_matched) {
      String _name = concept.eClass().getName();
      String _plus = (WeaviateStatements.NOT_MAPPED + _name);
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * Collections of a deployment; its connection is the program's endpoint.
   */
  private List<Code.Stmt> database(final VectorDatabase database, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = database.getDatabases().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(WeaviateStatements.LOGICAL_DATABASES);
      }
      MetricsConfig _metrics = database.getMetrics();
      boolean _tripleNotEquals = (_metrics != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(WeaviateStatements.METRICS);
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      List<Code.Stmt> _define = this.define(database.getConnection(), ifNotExists);
      Iterables.<Code.Stmt>addAll(result, _define);
      AuthConfig _auth = database.getAuth();
      boolean _tripleNotEquals_1 = (_auth != null);
      if (_tripleNotEquals_1) {
        List<Code.Stmt> _auth_1 = this.auth(database.getAuth());
        Iterables.<Code.Stmt>addAll(result, _auth_1);
      }
      EList<Cluster> _clusters = database.getClusters();
      for (final Cluster cluster : _clusters) {
        List<Code.Stmt> _define_1 = this.define(cluster, ifNotExists);
        Iterables.<Code.Stmt>addAll(result, _define_1);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Weaviate configures authentication at server start; the client side and roles are generated.
   */
  private List<Code.Stmt> auth(final AuthConfig config) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _contains = Collections.<EAuthType>unmodifiableList(CollectionLiterals.<EAuthType>newArrayList(EAuthType.NONE, EAuthType.API_KEY, EAuthType.TOKEN)).contains(config.getType());
      boolean _not = (!_contains);
      if (_not) {
        String _literal = config.getType().getLiteral();
        String _plus = (_literal + " authentication; Weaviate accepts API keys and OIDC tokens");
        throw Unsupported.of(_plus);
      }
      if (((((config.getCertificatePath() != null) || (config.getCmekKeyId() != null)) || config.isAuditLog()) || config.isFieldEncryption())) {
        throw Unsupported.of("certificates, customer-managed keys, audit logs and field encryption; Weaviate configures them outside the API");
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<RoleBinding> _roles = config.getRoles();
      for (final RoleBinding role : _roles) {
        List<Code.Stmt> _createRole = this.createRole(role, false);
        Iterables.<Code.Stmt>addAll(result, _createRole);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createRole(final RoleBinding binding, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Expr> permissions = CollectionLiterals.<Code.Expr>newArrayList();
      String _xifexpression = null;
      VectorCollection _collection = binding.getCollection();
      boolean _tripleEquals = (_collection == null);
      if (_tripleEquals) {
        _xifexpression = "*";
      } else {
        _xifexpression = WeaviateSchema.className(binding.getCollection());
      }
      final String scope = _xifexpression;
      EList<String> _privileges = binding.getPrivileges();
      for (final String privilege : _privileges) {
        {
          final List<String> actions = WeaviateStatements.PRIVILEGES.get(privilege.toLowerCase());
          if ((actions == null)) {
            String _join = IterableExtensions.join(IterableExtensions.<String>sort(WeaviateStatements.PRIVILEGES.keySet()), ", ");
            String _plus = ((("privilege \'" + privilege) + "\'; Weaviate collection and data permissions are ") + _join);
            throw Unsupported.of(_plus);
          }
          Code.Entries _kw = WeaviateSchema.kw("collection", Code.text(scope), "actions", Code.texts(actions));
          permissions.add(_kw);
        }
      }
      final Code.Text name = Code.text(binding.getRoleName());
      Code.Call _runtime = Code.runtime("create_role", WeaviateSchema.kw("role", name, "permissions", Code.items(permissions)));
      Code.Run _run = new Code.Run(_runtime);
      final Code.Stmt create = ((Code.Stmt) _run);
      Code.Stmt _xifexpression_1 = null;
      if (ifNotExists) {
        Code.Call _runtime_1 = Code.runtime("has_role", WeaviateSchema.kw("role", name));
        _xifexpression_1 = new Code.When(_runtime_1, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      } else {
        _xifexpression_1 = create;
      }
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression_1));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createTenants(final VectorCollection collection, final String name, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _multiTenant = this.schema.multiTenant(collection);
      boolean _not = (!_multiTenant);
      if (_not) {
        String _name = collection.getName();
        String _plus = ("a tenant of collection " + _name);
        String _plus_1 = (_plus + ", which declares none; Weaviate enables multi-tenancy only when it creates a collection");
        throw Unsupported.of(_plus_1);
      }
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), "tenants", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name))));
      if (ifNotExists) {
        args.put("if_not_exists", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("create_tenants", args);
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createAlias(final Alias alias, final VectorCollection collection, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(WeaviateSchema.className(alias.getName()));
      Code.Call _runtime = Code.runtime("create_alias", WeaviateSchema.kw("alias", name, "collection", Code.text(WeaviateSchema.className(collection))));
      Code.Run _run = new Code.Run(_runtime);
      final Code.Stmt create = ((Code.Stmt) _run);
      Code.Stmt _xifexpression = null;
      if (ifNotExists) {
        Code.Call _runtime_1 = Code.runtime("has_alias", WeaviateSchema.kw("alias", name));
        _xifexpression = new Code.When(_runtime_1, true, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(create)));
      } else {
        _xifexpression = create;
      }
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_xifexpression));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> addField(final VectorCollection collection, final Field field) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(WeaviateSchema.className(collection));
      List<Code.Stmt> _switchResult = null;
      boolean _matched = false;
      if (field instanceof PayloadField) {
        _matched=true;
        Code.Call _runtime = Code.runtime("add_property", WeaviateSchema.kw("name", name, "property", this.schema.propertySpec(collection, ((PayloadField)field))));
        Code.Run _run = new Code.Run(_runtime);
        _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
      }
      if (!_matched) {
        if (field instanceof VectorField) {
          _matched=true;
          Code.Call _runtime = Code.runtime("add_vector", WeaviateSchema.kw("name", name, "vector", this.schema.vectorSpec(collection, ((VectorField)field))));
          Code.Run _run = new Code.Run(_runtime);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> createPayloadIndex(final PayloadIndex index) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final FieldPath target = index.getTarget();
      if (((!target.getSubFields().isEmpty()) || (target.getRawPath() != null))) {
        throw Unsupported.of("payload indexes on nested paths");
      }
      final VectorCollection collection = this.indexCollection(index);
      this.schema.note(index, "structure", "castType", "onDisk", "prefixMatch", "minTokenLength", "maxTokenLength");
      final Field field = target.getField();
      if ((!(field instanceof PayloadField))) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("a payload index on vector field " + _fieldName);
        throw Unsupported.of(_plus);
      }
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), "property", Code.text(WeaviateSchema.propertyName(collection, field)));
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case KEYWORD:
            args.put("index", Code.text("filterable"));
            break;
          case BOOL:
          case UUID:
            args.put("index", Code.text("filterable"));
            break;
          case INTEGER:
          case FLOAT:
          case DATETIME:
            args.put("index", Code.text("rangeFilters"));
            break;
          case FULL_TEXT:
            args.put("index", Code.text("searchable"));
            String _xifexpression = null;
            Analyzer _analyzer = index.getAnalyzer();
            boolean _tripleEquals = (_analyzer == null);
            if (_tripleEquals) {
              _xifexpression = "word";
            } else {
              _xifexpression = this.schema.tokenizationOf(index.getAnalyzer());
            }
            args.put("tokenization", Code.text(_xifexpression));
            break;
          default:
            String _literal = index.getKind().getLiteral();
            String _plus_1 = (_literal + " payload indexes on an existing collection; Weaviate adds filterable, searchable and range indexes");
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal = index.getKind().getLiteral();
        String _plus_1 = (_literal + " payload indexes on an existing collection; Weaviate adds filterable, searchable and range indexes");
        throw Unsupported.of(_plus_1);
      }
      Code.Call _runtime = Code.runtime("create_property_index", args);
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> addFunction(final VectorCollection collection, final Function function) {
    List<Code.Stmt> _switchResult = null;
    EFunctionType _type = function.getType();
    if (_type != null) {
      switch (_type) {
        case BM25:
          List<Code.Stmt> _xblockexpression = null;
          {
            final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
            EList<Field> _inputs = function.getInputs();
            for (final Field input : _inputs) {
              {
                if ((!(input instanceof PayloadField))) {
                  String _fieldName = ModelUtil.fieldName(input);
                  String _plus = ("bm25 input " + _fieldName);
                  String _plus_1 = (_plus + ", which is not a text field");
                  throw Unsupported.of(_plus_1);
                }
                String _xifexpression = null;
                Analyzer _analyzer = function.getAnalyzer();
                boolean _tripleEquals = (_analyzer == null);
                if (_tripleEquals) {
                  _xifexpression = "word";
                } else {
                  _xifexpression = this.schema.tokenizationOf(function.getAnalyzer());
                }
                Code.Call _runtime = Code.runtime("create_property_index", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), 
                  "property", Code.text(WeaviateSchema.propertyName(collection, input)), "index", Code.text("searchable"), 
                  "tokenization", Code.text(_xifexpression)));
                Code.Run _run = new Code.Run(_runtime);
                result.add(_run);
              }
            }
            _xblockexpression = result;
          }
          _switchResult = _xblockexpression;
          break;
        case TEXT_EMBEDDING:
          throw Unsupported.of("adding a text-embedding function; Weaviate fixes the vectorizer of a named vector when it creates the vector");
        default:
          String _literal = function.getType().getLiteral();
          String _plus = (_literal + " functions; Weaviate runs no such server-side functions");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = function.getType().getLiteral();
      String _plus = (_literal + " functions; Weaviate runs no such server-side functions");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private List<Code.Stmt> dropFunction(final VectorCollection collection, final Function function) {
    List<Code.Stmt> _xblockexpression = null;
    {
      EFunctionType _type = function.getType();
      boolean _notEquals = (!Objects.equals(_type, EFunctionType.BM25));
      if (_notEquals) {
        String _literal = function.getType().getLiteral();
        String _plus = ("dropping a " + _literal);
        String _plus_1 = (_plus + " function; Weaviate cannot remove a vectorizer from a named vector");
        throw Unsupported.of(_plus_1);
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      EList<Field> _inputs = function.getInputs();
      for (final Field input : _inputs) {
        Code.Call _runtime = Code.runtime("drop_property_index", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), 
          "property", Code.text(WeaviateSchema.propertyName(collection, input)), "index", Code.text("searchable")));
        Code.Run _run = new Code.Run(_runtime);
        result.add(_run);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The collection a member concept belongs to: its container, or the concept of the enclosing create.
   */
  private VectorCollection owner(final EObject member) {
    final VectorCollection direct = ModelUtil.owningCollection(member);
    if ((direct != null)) {
      return direct;
    }
    final Create statement = EcoreUtil2.<Create>getContainerOfType(member, Create.class);
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
    String _name = member.eClass().getName();
    String _plus = ("a " + _name);
    String _plus_1 = (_plus + " outside a collection");
    throw Unsupported.of(_plus_1);
  }

  private VectorCollection indexCollection(final Index index) {
    VectorCollection _xblockexpression = null;
    {
      final VectorCollection owned = ModelUtil.owningCollection(index);
      if ((owned != null)) {
        return owned;
      }
      Field _switchResult = null;
      boolean _matched = false;
      if (index instanceof VectorIndex) {
        _matched=true;
        VectorField _elvis = null;
        VectorField _field = ((VectorIndex)index).getField();
        if (_field != null) {
          _elvis = _field;
        } else {
          VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(index, VectorField.class);
          _elvis = _containerOfType;
        }
        _switchResult = _elvis;
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
        return this.owner(index);
      }
      _xblockexpression = collection;
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
        String _plus_1 = (_plus + "; alter operations apply to collections, and Weaviate names are immutable");
        throw Unsupported.of(_plus_1);
      }
      final VectorCollection collection = ((VectorCollection) concept);
      String _newName = it.getNewName();
      boolean _tripleNotEquals = (_newName != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("renaming a collection; Weaviate collection names are immutable (an alias serves another name)");
      }
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

  private List<Code.Stmt> alter(final VectorCollection collection, final AlterOperation operation) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Text name = Code.text(WeaviateSchema.className(collection));
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
            final Field field = ((DropFieldOp)operation).getField();
            if ((!(field instanceof VectorField))) {
              String _fieldName = ModelUtil.fieldName(field);
              String _plus = ("dropping property " + _fieldName);
              String _plus_1 = (_plus + "; Weaviate cannot remove a property from a collection");
              throw Unsupported.of(_plus_1);
            }
            Code.Call _runtime = Code.runtime("drop_vector", WeaviateSchema.kw("name", name, "vector", Code.text(ModelUtil.fieldName(field))));
            Code.Run _run = new Code.Run(_runtime);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (operation instanceof RenameFieldOp) {
          _matched=true;
          throw Unsupported.of("renaming a field; Weaviate cannot rename properties or named vectors");
        }
      }
      if (!_matched) {
        if (operation instanceof SetPropertyOp) {
          _matched=true;
          Code.Stmt _setProperty = this.setProperty(collection, ((SetPropertyOp)operation));
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_setProperty));
        }
      }
      if (!_matched) {
        if (operation instanceof AddAliasOp) {
          _matched=true;
          _switchResult = this.createAlias(((AddAliasOp)operation).getNewAlias(), collection, false);
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
        String _plus = (WeaviateStatements.NOT_MAPPED + _name);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private Code.Stmt setProperty(final VectorCollection collection, final SetPropertyOp operation) {
    Code.Run _xblockexpression = null;
    {
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)));
      final Code.Expr value = Literals.of(operation.getValue());
      ECollectionProperty _property = operation.getProperty();
      if (_property != null) {
        switch (_property) {
          case DESCRIPTION:
            args.put("description", value);
            break;
          case TTL:
            if ((!(value instanceof Code.Num))) {
              throw Unsupported.of("a ttl that is not a number of seconds");
            }
            args.put("ttl", this.schema.ttl(collection, collection.getTtlField(), Integer.valueOf(Double.valueOf(((Code.Num) value).value()).intValue())));
            break;
          case CONSISTENCY:
            throw Unsupported.of("setting consistency; Weaviate sets consistency per request, not on the collection");
          case DELETION_PROTECTION:
            throw Unsupported.of("deletion-protection; Weaviate has no setting that blocks dropping a collection");
          case DYNAMIC:
            throw Unsupported.of("setting dynamic; Weaviate enables auto-schema for the whole server");
          case TAGS:
            throw Unsupported.of("collection tags; Weaviate collections have no tags");
          default:
            break;
        }
      }
      Code.Call _runtime = Code.runtime("update_collection", args);
      _xblockexpression = new Code.Run(_runtime);
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
          final Code.Text name = Code.text(WeaviateSchema.className(((VectorCollection)concept)));
          Code.Call _runtime = Code.runtime("drop_collection", WeaviateSchema.kw("name", name));
          Code.Run _run = new Code.Run(_runtime);
          final Code.Stmt drop = ((Code.Stmt) _run);
          Code.Stmt _xifexpression = null;
          boolean _isIfExists = it.isIfExists();
          if (_isIfExists) {
            Code.Call _runtime_1 = Code.runtime("has_collection", WeaviateSchema.kw("name", name));
            _xifexpression = new Code.When(_runtime_1, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
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
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final Code.Text name = Code.text(WeaviateSchema.className(((Alias)concept).getName()));
            Code.Call _runtime = Code.runtime("drop_alias", WeaviateSchema.kw("alias", name));
            Code.Run _run = new Code.Run(_runtime);
            final Code.Stmt drop = ((Code.Stmt) _run);
            Code.Stmt _xifexpression = null;
            boolean _isIfExists = it.isIfExists();
            if (_isIfExists) {
              Code.Call _runtime_1 = Code.runtime("has_alias", WeaviateSchema.kw("alias", name));
              _xifexpression = new Code.When(_runtime_1, false, Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(drop)));
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
          _switchResult = this.dropTenant(this.owner(concept), WeaviateSchema.tenantName(((Tenant)concept).getName()));
        }
      }
      if (!_matched) {
        if (concept instanceof Namespace) {
          _matched=true;
          _switchResult = this.dropTenant(this.owner(concept), WeaviateSchema.tenantName(((Namespace)concept).getKey()));
        }
      }
      if (!_matched) {
        if (concept instanceof VectorField) {
          _matched=true;
          Code.Call _runtime = Code.runtime("drop_vector", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(this.owner(concept))), "vector", Code.text(ModelUtil.fieldName(((Field)concept)))));
          Code.Run _run = new Code.Run(_runtime);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadField) {
          _matched=true;
          String _name = ((PayloadField)concept).getName();
          String _plus = ("dropping property " + _name);
          String _plus_1 = (_plus + "; Weaviate cannot remove a property from a collection");
          throw Unsupported.of(_plus_1);
        }
      }
      if (!_matched) {
        if (concept instanceof PayloadIndex) {
          _matched=true;
          List<Code.Stmt> _xblockexpression_1 = null;
          {
            final VectorCollection collection = this.indexCollection(((Index)concept));
            final Field field = ((PayloadIndex)concept).getTarget().getField();
            String _switchResult_1 = null;
            EPayloadIndexKind _kind = ((PayloadIndex)concept).getKind();
            if (_kind != null) {
              switch (_kind) {
                case INTEGER:
                case FLOAT:
                case DATETIME:
                  _switchResult_1 = "rangeFilters";
                  break;
                case FULL_TEXT:
                  _switchResult_1 = "searchable";
                  break;
                default:
                  _switchResult_1 = "filterable";
                  break;
              }
            } else {
              _switchResult_1 = "filterable";
            }
            final String index = _switchResult_1;
            Code.Call _runtime = Code.runtime("drop_property_index", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), 
              "property", Code.text(WeaviateSchema.propertyName(collection, field)), "index", Code.text(index)));
            Code.Run _run = new Code.Run(_runtime);
            _xblockexpression_1 = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
          }
          _switchResult = _xblockexpression_1;
        }
      }
      if (!_matched) {
        if (concept instanceof VectorIndex) {
          _matched=true;
          throw Unsupported.of("dropping a vector index; Weaviate removes a vector index only together with the embeddings of its named vector");
        }
      }
      if (!_matched) {
        if (concept instanceof Function) {
          _matched=true;
          _switchResult = this.dropFunction(this.owner(concept), ((Function)concept));
        }
      }
      if (!_matched) {
        if (concept instanceof RoleBinding) {
          _matched=true;
          Code.Call _runtime = Code.runtime("drop_role", WeaviateSchema.kw("role", Code.text(((RoleBinding)concept).getRoleName())));
          Code.Run _run = new Code.Run(_runtime);
          _switchResult = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
        }
      }
      if (!_matched) {
        if (concept instanceof Connection) {
          _matched=true;
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
        }
      }
      if (!_matched) {
        if (concept instanceof EmbeddingModel) {
          _matched=true;
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
        }
      }
      if (!_matched) {
        if (concept instanceof GenerativeModel) {
          _matched=true;
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
        }
      }
      if (!_matched) {
        if (concept instanceof Reranker) {
          _matched=true;
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
        }
      }
      if (!_matched) {
        if (concept instanceof Analyzer) {
          _matched=true;
          _switchResult = CollectionLiterals.<Code.Stmt>newArrayList();
        }
      }
      if (!_matched) {
        if (concept instanceof Database) {
          _matched=true;
          throw Unsupported.of(WeaviateStatements.LOGICAL_DATABASES);
        }
      }
      if (!_matched) {
        if (concept instanceof Partition) {
          _matched=true;
          throw Unsupported.of(WeaviateSchema.PARTITIONS);
        }
      }
      if (!_matched) {
        if (concept instanceof BackupSchedule) {
          _matched=true;
          throw Unsupported.of("backup schedules; Weaviate creates backups only on request");
        }
      }
      if (!_matched) {
        String _name = concept.eClass().getName();
        String _plus = ("dropping a " + _name);
        String _plus_1 = (_plus + "; Weaviate has no such object to remove");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<Code.Stmt> dropTenant(final VectorCollection collection, final String name) {
    Code.Call _runtime = Code.runtime("drop_tenants", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), "tenants", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(name)))));
    Code.Run _run = new Code.Run(_runtime);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _run)));
  }

  protected List<Code.Stmt> _body(final Insert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Weaviate sets object ttl on the collection");
      }
      final String slice = this.tenantOf(it.getCollection(), it);
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = WeaviateSchema.kw("collection", this.scoped(this.schema.writeLayout(it.getCollection(), this.writeConsistency(it)), slice), "rows", this.rows(it.getSource()));
      if ((ModelUtil.written(it, "onConflict") && Collections.<EConflictPolicy>unmodifiableList(CollectionLiterals.<EConflictPolicy>newArrayList(EConflictPolicy.UPDATE, EConflictPolicy.REPLACE)).contains(it.getOnConflict()))) {
        Code.Call _runtime = Code.runtime("upsert_rows", args);
        Code.Return _return = new Code.Return(_runtime);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      boolean _written_1 = ModelUtil.written(it, "onConflict");
      if (_written_1) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      Code.Call _runtime_1 = Code.runtime("insert_rows", args);
      Code.Return _return_1 = new Code.Return(_runtime_1);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Upsert it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(it, "ttlSeconds");
      if (_written) {
        throw Unsupported.of("per-statement ttl; Weaviate sets object ttl on the collection");
      }
      final String slice = this.tenantOf(it.getCollection(), it);
      this.schema.note(it, "recordCountHint");
      final Code.Entries args = WeaviateSchema.kw("collection", this.scoped(this.schema.writeLayout(it.getCollection(), this.writeConsistency(it)), slice), "rows", this.rows(it.getSource()));
      boolean _written_1 = ModelUtil.written(it, "batchSize");
      if (_written_1) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      Code.Call _runtime = Code.runtime("upsert_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Update it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      if (((!it.getAppendFields().isEmpty()) || (!it.getArrayRemoveFields().isEmpty()))) {
        throw Unsupported.of("array-append and array-remove; Weaviate replaces array properties as a whole");
      }
      boolean _isEmpty = it.getRemovedFields().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("remove-fields has no Weaviate mapping yet");
      }
      boolean _written = ModelUtil.written(it, "ifVersion");
      if (_written) {
        throw Unsupported.of("if-version; Weaviate objects carry no version for conditional writes");
      }
      final String slice = this.tenantOf(it.getCollection(), it);
      this.schema.note(it, "recordCountHint");
      VectorCollection _collection = it.getCollection();
      final WeaviateFilter where = new WeaviateFilter(_collection).where(it.getFilter());
      final Code.Entries args = WeaviateSchema.kw("collection", this.scoped(this.schema.writeLayout(it.getCollection(), this.writeConsistency(it)), slice));
      boolean _isEmpty_1 = it.getIds().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      }
      boolean _isEmpty_2 = where.isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
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
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), EUpdateMode.REPLACE))) {
        args.put("mode", Code.text("replace"));
      }
      Code.Call _runtime = Code.runtime("update_rows", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Delete it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final String slice = this.tenantOf(it.getCollection(), it);
      VectorCollection _collection = it.getCollection();
      final WeaviateFilter where = new WeaviateFilter(_collection).where(it.getFilter());
      final Code.Entries scopedLayout = this.scoped(this.schema.layout(it.getCollection(), this.writeConsistency(it)), slice);
      if (((it.isDeleteAll() && it.getIds().isEmpty()) && where.isEmpty())) {
        boolean _written = ModelUtil.written(it, "deleteLimit");
        if (_written) {
          throw Unsupported.of("\'all\' together with a limit");
        }
        Code.Call _runtime = Code.runtime("truncate", WeaviateSchema.kw("collection", scopedLayout));
        Code.Return _return = new Code.Return(_runtime);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
      }
      if ((it.getIds().isEmpty() && where.isEmpty())) {
        throw Unsupported.of("a delete without ids or where");
      }
      final Code.Entries args = WeaviateSchema.kw("collection", scopedLayout);
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
      boolean _written_1 = ModelUtil.written(it, "deleteLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getDeleteLimit()));
        Code.Call _runtime_1 = Code.runtime("delete_limited", args);
        Code.Return _return_1 = new Code.Return(_runtime_1);
        return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_1));
      }
      Code.Call _runtime_2 = Code.runtime("delete_rows", args);
      Code.Return _return_2 = new Code.Return(_runtime_2);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return_2));
    }
    return _xblockexpression;
  }

  /**
   * Statement write consistency in Weaviate's level names. 'wait' adds no call.
   */
  private String writeConsistency(final EObject statement) {
    String _xifexpression = null;
    boolean _written = ModelUtil.written(statement, "writeConsistency");
    if (_written) {
      Object _feature = this.feature(statement, "writeConsistency");
      _xifexpression = ((EWriteConsistency) _feature).getLiteral().toUpperCase();
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  protected List<Code.Stmt> _body(final BulkImport it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isContinuous = it.isContinuous();
      if (_isContinuous) {
        throw Unsupported.of("continuous ingestion; Weaviate has no pull-based ingestion");
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        throw Unsupported.of("asynchronous bulk import; Weaviate has no server-side import jobs");
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
        throw Unsupported.of((("bulk import from " + scheme) + "; Weaviate has no server-side ingestion, and the runtime reads local files"));
      }
      if ((ModelUtil.written(it, "format") && (!Objects.equals(it.getFormat(), EImportFormat.JSON)))) {
        String _literal = it.getFormat().getLiteral();
        String _plus = (_literal + " bulk import; Weaviate has no server-side ingestion, and the runtime reads JSON and JSON Lines files");
        throw Unsupported.of(_plus);
      }
      this.schema.note(it, "parallelism");
      final String slice = this.tenantOf(it.getCollection(), it);
      final Code.Entries args = WeaviateSchema.kw("collection", this.scoped(this.schema.writeLayout(it.getCollection(), null), slice), "location", Code.text(it.getLocation()));
      boolean _written = ModelUtil.written(it, "batchSize");
      if (_written) {
        args.put("batch_size", Code.num(it.getBatchSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "onConflict");
      if (_written_1) {
        args.put("on_conflict", Code.text(it.getOnConflict().getLiteral()));
      }
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("import_file", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Transaction it) {
    throw Unsupported.of("transactions; Weaviate writes each object on its own and has no multi-object transactions");
  }

  protected List<Code.Stmt> _body(final Load it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(WeaviateSchema.PARTITIONS);
      }
      boolean _isEmpty_1 = this.schema.extValues(it).isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of("extension parameters on load");
      }
      Code.Call _runtime = Code.runtime("load_collection", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(it.getCollection())), "multi_tenant", Code.flag(this.schema.multiTenant(it.getCollection()))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Release it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = it.getPartitions().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(WeaviateSchema.PARTITIONS);
      }
      boolean _multiTenant = this.schema.multiTenant(it.getCollection());
      boolean _not_1 = (!_multiTenant);
      if (_not_1) {
        String _name = it.getCollection().getName();
        String _plus = ("releasing collection " + _name);
        String _plus_1 = (_plus + "; Weaviate can deactivate tenants but keeps a collection without tenants loaded");
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_1 = this.schema.extValues(it).isEmpty();
      boolean _not_2 = (!_isEmpty_1);
      if (_not_2) {
        throw Unsupported.of("extension parameters on release");
      }
      Code.Call _runtime = Code.runtime("release_collection", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(it.getCollection()))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  /**
   * Weaviate acknowledges a write after logging it, and the next read sees it; flush only checks the collection.
   */
  protected List<Code.Stmt> _body(final Flush it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      boolean _isEmpty = this.schema.extValues(it).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on flush");
      }
      Code.Call _runtime = Code.runtime("flush", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(it.getCollection()))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Compact it) {
    throw Unsupported.of("compaction; Weaviate compacts its storage in the background and has no API to start it");
  }

  protected List<Code.Stmt> _body(final Describe it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = WeaviateSchema.kw("collection", this.schema.layout(it.getCollection(), null));
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
    throw Unsupported.of("embedding migration; Weaviate cannot change the vectorizer of a named vector, and a named vector added later is empty for existing objects");
  }

  protected List<Code.Stmt> _body(final Snapshot it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      Tenant _tenant = it.getTenant();
      boolean _tripleNotEquals = (_tenant != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("backups of one tenant; Weaviate backs up whole collections");
      }
      this.schema.note(it, "description");
      String _elvis = null;
      String _snapshotName = it.getSnapshotName();
      if (_snapshotName != null) {
        _elvis = _snapshotName;
      } else {
        String _name = it.getName();
        _elvis = _name;
      }
      final Code.Entries args = this.backup(_elvis, it.getScope(), it.getLocation());
      if (((!ModelUtil.written(it, "scope")) || Objects.equals(it.getScope(), ESnapshotScope.COLLECTION))) {
        String _className = WeaviateSchema.className(it.getCollection());
        args.put("collections", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_className))));
      }
      boolean _isIncremental = it.isIncremental();
      if (_isIncremental) {
        args.put("incremental", Code.flag(true));
      }
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("create_backup", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Restore it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = this.backup(it.getSnapshotName(), it.getScope(), it.getLocation());
      if (((!ModelUtil.written(it, "scope")) || Objects.equals(it.getScope(), ESnapshotScope.COLLECTION))) {
        String _className = WeaviateSchema.className(it.getCollection());
        args.put("collections", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_className))));
      }
      boolean _isAsync = it.isAsync();
      if (_isAsync) {
        args.put("wait", Code.flag(false));
      }
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("restore_backup", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DropSnapshot it) {
    throw Unsupported.of("drop-snapshot; Weaviate has no API that deletes a backup (DELETE /backups cancels a running one)");
  }

  /**
   * Backup id and storage: the id is the snapshot name in Weaviate's lower case form, the location selects the backend.
   */
  private Code.Entries backup(final String snapshot, final ESnapshotScope backupScope, final String location) {
    Code.Entries _xblockexpression = null;
    {
      if ((snapshot == null)) {
        throw Unsupported.of("a backup without a name; add a statement label or \'as\'");
      }
      if ((Objects.equals(backupScope, ESnapshotScope.TENANT) || Objects.equals(backupScope, ESnapshotScope.SHARD))) {
        String _literal = backupScope.getLiteral();
        String _plus = (_literal + " backups; Weaviate backs up whole collections");
        throw Unsupported.of(_plus);
      }
      final String id = Syntax.snake(snapshot).toLowerCase().replace(".", "-");
      boolean _matches = id.matches("[a-z0-9_-]+");
      boolean _not = (!_matches);
      if (_not) {
        throw Unsupported.of((("backup id \'" + id) + "\'; Weaviate backup ids use lower case letters, digits, \'_\' and \'-\'"));
      }
      final Code.Entries args = WeaviateSchema.kw("backup_id", Code.text(id));
      String _elvis = null;
      if (location != null) {
        _elvis = location;
      } else {
        _elvis = "filesystem";
      }
      final String target = _elvis;
      final int separator = target.indexOf("://");
      String _xifexpression = null;
      if ((separator < 0)) {
        _xifexpression = target;
      } else {
        _xifexpression = target.substring(0, separator);
      }
      final String scheme = _xifexpression;
      final String backend = WeaviateStatements.BACKENDS.get(scheme);
      if ((backend == null)) {
        throw Unsupported.of((("backup location " + target) + "; Weaviate backs up to filesystem, s3, gcs and azure"));
      }
      args.put("backend", Code.text(backend));
      if ((separator >= 0)) {
        final String rest = target.substring((separator + 3));
        boolean _equals = Objects.equals(backend, "filesystem");
        if (_equals) {
          args.put("path", Code.text(rest));
        } else {
          final int slash = rest.indexOf("/");
          String _xifexpression_1 = null;
          if ((slash < 0)) {
            _xifexpression_1 = rest;
          } else {
            _xifexpression_1 = rest.substring(0, slash);
          }
          args.put("bucket", Code.text(_xifexpression_1));
          if (((slash >= 0) && (slash < (rest.length() - 1)))) {
            args.put("path", Code.text(rest.substring((slash + 1))));
          }
        }
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Clone it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      VectorDatabase _sourceDatabase = it.getSourceDatabase();
      boolean _tripleNotEquals = (_sourceDatabase != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("cloning a database; Weaviate has no logical databases");
      }
      Literal _asOf = it.getAsOf();
      boolean _tripleNotEquals_1 = (_asOf != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("clone as-of; Weaviate keeps no object history");
      }
      if ((ModelUtil.written(it, "mode") && Objects.equals(it.getMode(), ECloneMode.SHALLOW))) {
        throw Unsupported.of("shallow clones; Weaviate has no copy-on-write collections");
      }
      boolean _isEmpty = this.schema.extValues(it).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on clone");
      }
      boolean _multiTenant = this.schema.multiTenant(it.getSourceCollection());
      if (_multiTenant) {
        throw Unsupported.of("cloning a multi-tenant collection has no Weaviate mapping yet");
      }
      final Function1<VectorField, String> _function = (VectorField vector) -> {
        return ModelUtil.fieldName(vector);
      };
      Code.Call _runtime = Code.runtime("clone_collection", WeaviateSchema.kw("source", this.schema.layout(it.getSourceCollection(), null), 
        "target", Code.text(WeaviateSchema.className(it.getTargetName())), "vectors", Code.texts(ListExtensions.<VectorField, String>map(this.schema.storedVectors(it.getSourceCollection()), _function))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RebuildIndex it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Index definition = it.getIndex();
      if ((!(definition instanceof PayloadIndex))) {
        throw Unsupported.of("rebuilding a vector index; Weaviate has no API that rebuilds a vector index");
      }
      final PayloadIndex payloadIndex = ((PayloadIndex) definition);
      boolean _isEmpty = this.schema.extValues(it).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("extension parameters on rebuild-index");
      }
      this.schema.note(it, "concurrently");
      final VectorCollection collection = this.indexCollection(payloadIndex);
      Code.Call _runtime = Code.runtime("rebuild_property_index", WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)), 
        "property", Code.text(WeaviateSchema.propertyName(collection, payloadIndex.getTarget().getField())), "index", Code.text(this.propertyIndex(payloadIndex))));
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final SwapAlias it) {
    Code.Call _runtime = Code.runtime("swap_alias", WeaviateSchema.kw("alias", Code.text(WeaviateSchema.className(it.getAlias().getName())), 
      "source", Code.text(WeaviateSchema.className(it.getOldCollection())), "target", Code.text(WeaviateSchema.className(it.getNewCollection()))));
    Code.Run _run = new Code.Run(_runtime);
    return Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
  }

  protected List<Code.Stmt> _body(final RefreshExternal it) {
    throw Unsupported.of(WeaviateSchema.EXTERNAL);
  }

  protected List<Code.Stmt> _body(final Truncate it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      String _xifexpression = null;
      String _namespace = it.getNamespace();
      boolean _tripleEquals = (_namespace == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.tenantOf(it.getCollection(), it);
      }
      final String slice = _xifexpression;
      final Code.Entries args = WeaviateSchema.kw("collection", this.scoped(this.schema.layout(it.getCollection(), null), slice));
      if (((slice == null) && this.schema.multiTenant(it.getCollection()))) {
        args.put("all_tenants", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("truncate", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final ListObjects it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final Code.Entries args = WeaviateSchema.kw("kind", Code.text(it.getKind().getLiteral()));
      EListKind _kind = it.getKind();
      if (_kind != null) {
        switch (_kind) {
          case COLLECTIONS:
          case ALIASES:
          case SNAPSHOTS:
            if (((it.getScope() != null) && (!(it.getScope() instanceof VectorCollection)))) {
              String _literal = it.getKind().getLiteral();
              String _plus = ("listing " + _literal);
              String _plus_1 = (_plus + " in a ");
              String _name = it.getScope().eClass().getName();
              String _plus_2 = (_plus_1 + _name);
              throw Unsupported.of(_plus_2);
            }
            break;
          case TENANTS:
          case NAMESPACES:
          case INDEXES:
            Concept _scope = it.getScope();
            boolean _not = (!(_scope instanceof VectorCollection));
            if (_not) {
              String _literal_1 = it.getKind().getLiteral();
              String _plus_3 = ("listing " + _literal_1);
              String _plus_4 = (_plus_3 + " without \'in <collection>\'");
              throw Unsupported.of(_plus_4);
            }
            break;
          case PARTITIONS:
            throw Unsupported.of(WeaviateSchema.PARTITIONS);
          case DATABASES:
            throw Unsupported.of(WeaviateStatements.LOGICAL_DATABASES);
          default:
            break;
        }
      }
      Concept _scope_1 = it.getScope();
      if ((_scope_1 instanceof VectorCollection)) {
        Concept _scope_2 = it.getScope();
        args.put("collection", Code.text(WeaviateSchema.className(((VectorCollection) _scope_2))));
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
      final VectorCollection collection = this.indexCollection(definition);
      final Code.Entries args = WeaviateSchema.kw("name", Code.text(WeaviateSchema.className(collection)));
      boolean _matched = false;
      if (definition instanceof VectorIndex) {
        _matched=true;
        VectorField _elvis = null;
        VectorField _field = ((VectorIndex)definition).getField();
        if (_field != null) {
          _elvis = _field;
        } else {
          VectorField _containerOfType = EcoreUtil2.<VectorField>getContainerOfType(definition, VectorField.class);
          _elvis = _containerOfType;
        }
        final VectorField field = _elvis;
        args.put("vector", Code.text(ModelUtil.fieldName(field)));
      }
      if (!_matched) {
        if (definition instanceof PayloadIndex) {
          _matched=true;
          args.put("property", Code.text(WeaviateSchema.propertyName(collection, ((PayloadIndex)definition).getTarget().getField())));
          args.put("index", Code.text(this.propertyIndex(((PayloadIndex)definition))));
        }
      }
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

  /**
   * The Weaviate inverted index that serves a payload index of the given kind.
   */
  private String propertyIndex(final PayloadIndex definition) {
    String _switchResult = null;
    EPayloadIndexKind _kind = definition.getKind();
    if (_kind != null) {
      switch (_kind) {
        case INTEGER:
        case FLOAT:
        case DATETIME:
          _switchResult = "rangeFilters";
          break;
        case FULL_TEXT:
          _switchResult = "searchable";
          break;
        default:
          _switchResult = "filterable";
          break;
      }
    } else {
      _switchResult = "filterable";
    }
    return _switchResult;
  }

  protected List<Code.Stmt> _body(final VectorSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "elementScope", "asOf", "cursor", "explain", "highlighted", "sortClauses", "prefetches", "exact", "queryModel");
      this.pending(it, "withTotal", "mmr", "reranker", "aggregations", "groupBy", "boost");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      final Code.Expr vector = this.queryVector(it.getInput(), it.getCollection(), field, "query_vector", prelude);
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      if ((ModelUtil.written(it, "metric") && (!Objects.equals(it.getMetric(), metric)))) {
        throw Unsupported.of("a query metric other than the metric of the vector index");
      }
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(ModelUtil.fieldName(field)), "vector", vector, 
        "limit", Code.num(it.getTopK()), "metric", Code.text(this.schema.distance(metric)));
      ScoreBounds _bounds = it.getBounds();
      this.vectorBounds(_bounds, metric, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.searchParams(it.getParams(), args);
      this.extensionArgs(it, args);
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
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "highlighted", "sortClauses", "fuzziness", "slop");
      this.pending(it, "withTotal", "explain", "mmr", "reranker", "aggregations", "groupBy", "boost");
      EQueryInputMode _kind = it.getInput().getKind();
      boolean _notEquals = (!Objects.equals(_kind, EQueryInputMode.TEXT));
      if (_notEquals) {
        String _literal = it.getInput().getKind().getLiteral();
        String _plus = ("keyword search with " + _literal);
        String _plus_1 = (_plus + " input");
        throw Unsupported.of(_plus_1);
      }
      if ((ModelUtil.written(it, "textQueryType") && (!Objects.equals(it.getTextQueryType(), ETextQueryType.MATCH)))) {
        String _literal_1 = it.getTextQueryType().getLiteral();
        String _plus_2 = ("text-query " + _literal_1);
        throw Unsupported.of(_plus_2);
      }
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "query", Literals.of(it.getInput().getValue()), "limit", Code.num(it.getTopK()));
      this.keywordFields(it.getCollection(), it.getSearchFields(), args);
      this.bm25Operator(it, args);
      ScoreBounds _bounds = it.getBounds();
      boolean _tripleNotEquals = (_bounds != null);
      if (_tripleNotEquals) {
        boolean _written = ModelUtil.written(it.getBounds(), "min");
        if (_written) {
          args.put("score_min", Code.num(it.getBounds().getMin()));
        }
        boolean _written_1 = ModelUtil.written(it.getBounds(), "max");
        if (_written_1) {
          args.put("score_max", Code.num(it.getBounds().getMax()));
        }
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.searchParams(it.getParams(), args);
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("keyword_search", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final HybridSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "highlighted", "sortClauses", "bounds", "params");
      this.pending(it, "withTotal", "explain", "mmr", "reranker", "aggregations", "groupBy", "boost");
      final List<VectorLeg> vectorLegs = IterableExtensions.<VectorLeg>toList(Iterables.<VectorLeg>filter(it.getLegs(), VectorLeg.class));
      final List<KeywordLeg> keywordLegs = IterableExtensions.<KeywordLeg>toList(Iterables.<KeywordLeg>filter(it.getLegs(), KeywordLeg.class));
      if (((vectorLegs.size() != 1) || (keywordLegs.size() != 1))) {
        throw Unsupported.of("hybrid search with other than one vector leg and one keyword leg");
      }
      final VectorLeg vectorLeg = IterableExtensions.<VectorLeg>head(vectorLegs);
      final KeywordLeg keywordLeg = IterableExtensions.<KeywordLeg>head(keywordLegs);
      for (final HybridLeg leg : Collections.<HybridLeg>unmodifiableList(CollectionLiterals.<HybridLeg>newArrayList(vectorLeg, keywordLeg))) {
        {
          boolean _written = ModelUtil.written(leg, "legLimit");
          if (_written) {
            throw Unsupported.of("per-leg limits; Weaviate hybrid search takes one limit");
          }
          Object _feature = this.feature(leg, "filter");
          boolean _tripleNotEquals = (_feature != null);
          if (_tripleNotEquals) {
            throw Unsupported.of("per-leg filters; Weaviate hybrid search takes one filter");
          }
        }
      }
      this.reject(vectorLeg, "exact", "params");
      this.reject(keywordLeg, "textQueryType", "minShouldMatch");
      QueryInput _elvis = null;
      QueryInput _input = keywordLeg.getInput();
      if (_input != null) {
        _elvis = _input;
      } else {
        QueryInput _input_1 = it.getInput();
        _elvis = _input_1;
      }
      final QueryInput keywordInput = _elvis;
      if (((keywordInput == null) || (!Objects.equals(keywordInput.getKind(), EQueryInputMode.TEXT)))) {
        throw Unsupported.of("a keyword leg without text input");
      }
      final VectorField field = this.searchField(it.getCollection(), vectorLeg.getVectorField());
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      QueryInput _elvis_1 = null;
      QueryInput _input_2 = vectorLeg.getInput();
      if (_input_2 != null) {
        _elvis_1 = _input_2;
      } else {
        QueryInput _input_3 = it.getInput();
        _elvis_1 = _input_3;
      }
      final Code.Expr vector = this.queryVector(_elvis_1, it.getCollection(), field, "query_vector", prelude);
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      if ((ModelUtil.written(vectorLeg, "metric") && (!Objects.equals(vectorLeg.getMetric(), metric)))) {
        throw Unsupported.of("a query metric other than the metric of the vector index");
      }
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "query", Literals.of(keywordInput.getValue()), "vector", vector, 
        "field", Code.text(ModelUtil.fieldName(field)), "limit", Code.num(it.getTopK()), "fusion", Code.text(this.fusionName(it)), 
        "alpha", Code.num(this.vectorWeight(it, vectorLeg, keywordLeg)));
      this.keywordFields(it.getCollection(), keywordLeg.getSearchFields(), args);
      if ((ModelUtil.written(keywordLeg, "matchOperator") && Objects.equals(keywordLeg.getMatchOperator(), EMatchOperator.AND))) {
        args.put("operator", Code.text("and"));
      }
      final ScoreBounds legBounds = vectorLeg.getBounds();
      if ((legBounds != null)) {
        boolean _written = ModelUtil.written(legBounds, "max");
        if (_written) {
          throw Unsupported.of("an upper similarity bound on the vector leg");
        }
        boolean _written_1 = ModelUtil.written(legBounds, "min");
        if (_written_1) {
          args.put("max_vector_distance", Code.num(this.distance(metric, legBounds.getMin())));
        }
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.extensionArgs(it, args);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _runtime = Code.runtime("hybrid_search", args);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String fusionName(final HybridSearch search) {
    String _xblockexpression = null;
    {
      EFusionType _xifexpression = null;
      boolean _written = ModelUtil.written(search, "fusion");
      if (_written) {
        _xifexpression = search.getFusion();
      } else {
        _xifexpression = EFusionType.RRF;
      }
      final EFusionType kind = _xifexpression;
      String _switchResult = null;
      if (kind != null) {
        switch (kind) {
          case RRF:
            String _xblockexpression_1 = null;
            {
              if ((ModelUtil.written(search, "fusionConstant") && (search.getFusionConstant() != 60))) {
                int _fusionConstant = search.getFusionConstant();
                String _plus = ("fusion-k " + Integer.valueOf(_fusionConstant));
                String _plus_1 = (_plus + "; Weaviate ranked fusion uses k = 60");
                throw Unsupported.of(_plus_1);
              }
              _xblockexpression_1 = "ranked";
            }
            _switchResult = _xblockexpression_1;
            break;
          case RELATIVE_SCORE:
            _switchResult = "relative_score";
            break;
          default:
            String _literal = kind.getLiteral();
            String _plus = (_literal + " fusion; Weaviate fuses by rank or by relative score");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = kind.getLiteral();
        String _plus = (_literal + " fusion; Weaviate fuses by rank or by relative score");
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Weaviate alpha is the weight of the vector leg; vecDSL legs without weights weigh equally.
   */
  private double vectorWeight(final HybridSearch search, final VectorLeg vectorLeg, final KeywordLeg keywordLeg) {
    double _xblockexpression = (double) 0;
    {
      boolean _written = ModelUtil.written(search, "alpha");
      if (_written) {
        return search.getAlpha();
      }
      if ((ModelUtil.written(vectorLeg, "weight") && ModelUtil.written(keywordLeg, "weight"))) {
        double _weight = vectorLeg.getWeight();
        double _weight_1 = keywordLeg.getWeight();
        final double total = (_weight + _weight_1);
        if ((total <= 0)) {
          throw Unsupported.of("leg weights that do not sum to a positive value");
        }
        double _weight_2 = vectorLeg.getWeight();
        return (_weight_2 / total);
      }
      if ((ModelUtil.written(vectorLeg, "weight") || ModelUtil.written(keywordLeg, "weight"))) {
        throw Unsupported.of("a weight on only one hybrid leg");
      }
      _xblockexpression = 0.5;
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final RecommendSearch it) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf", "cursor", "explain", "highlighted", "sortClauses", "prefetches");
      this.pending(it, "withTotal", "mmr", "reranker", "aggregations", "groupBy", "boost");
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
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final Function1<Literal, Code.Expr> _function = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Function1<Literal, Code.Expr> _function_1 = (Literal it_1) -> {
        return Literals.of(it_1);
      };
      final Code.Entries examples = WeaviateSchema.kw("collection", this.layout(source, it), "field", Code.text(ModelUtil.fieldName(field)), 
        "positives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getPositives(), _function)), "negatives", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getNegatives(), _function_1)));
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      Code.Entries _layout = this.layout(it.getCollection(), it);
      Code.Text _text = Code.text(ModelUtil.fieldName(field));
      Code.Ref _ref = new Code.Ref("query_vector");
      final Code.Entries args = WeaviateSchema.kw("collection", _layout, "field", _text, "vector", _ref, 
        "limit", Code.num(it.getTopK()), "metric", Code.text(this.schema.distance(metric)));
      VectorCollection _collection_1 = it.getCollection();
      boolean _tripleEquals = (source == _collection_1);
      if (_tripleEquals) {
        EList<Literal> _positives = it.getPositives();
        EList<Literal> _negatives = it.getNegatives();
        final Function1<Literal, Code.Expr> _function_2 = (Literal it_1) -> {
          return Literals.of(it_1);
        };
        args.put("exclude", Code.items(IterableExtensions.<Code.Expr>toList(IterableExtensions.<Literal, Code.Expr>map(Iterables.<Literal>concat(_positives, _negatives), _function_2))));
      }
      ScoreBounds _bounds = it.getBounds();
      this.vectorBounds(_bounds, metric, (ModelUtil.written(it, "scoreForm") && Objects.equals(it.getScoreForm(), EScoreForm.NATIVE)), args);
      VectorCollection _collection_2 = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection_2).where(it.getFilter()));
      this.searchParams(it.getParams(), args);
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("recommend_vector", examples);
      Code.Let _let = new Code.Let("query_vector", _runtime);
      Code.Call _runtime_1 = Code.runtime("vector_search", args);
      Code.Return _return = new Code.Return(_runtime_1);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _let), _return);
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
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "ids", Code.items(ListExtensions.<Literal, Code.Expr>map(it.getIds(), _function)));
      this.projection(it.getCollection(), args, it.getOutput(), CollectionLiterals.<Field>newArrayList(), it.isWithVectors());
      this.extensionArgs(it, args);
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
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it));
      VectorCollection _collection = it.getCollection();
      final WeaviateFilter where = new WeaviateFilter(_collection).where(it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("count", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Scroll it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "sliceIndex", "asOf");
      this.pending(it, "cursor", "sortClauses");
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it));
      boolean _written = ModelUtil.written(it, "scrollLimit");
      if (_written) {
        args.put("limit", Code.num(it.getScrollLimit()));
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("scroll", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final GenerativeSearch it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      this.pending(it, "mmr", "boost", "reranker");
      GenerativeModel _xifexpression = null;
      GenerativeModel _generativeModel = it.getCollection().getGenerativeModel();
      boolean _tripleNotEquals = (_generativeModel != null);
      if (_tripleNotEquals) {
        _xifexpression = it.getCollection().getGenerativeModel();
      } else {
        _xifexpression = IterableExtensions.<GenerativeModel>head(it.getCollection().getGenerativeModels());
      }
      final GenerativeModel module = _xifexpression;
      if ((module == null)) {
        String _name = it.getCollection().getName();
        String _plus = ("generative search on collection " + _name);
        String _plus_1 = (_plus + ", which has no generative-model; Weaviate generates with the module of the collection");
        throw Unsupported.of(_plus_1);
      }
      if (((it.getGenerativeModel() != null) && (it.getGenerativeModel() != module))) {
        throw Unsupported.of("a generative model other than the collection\'s has no Weaviate mapping yet");
      }
      if (((it.getSinglePrompt() == null) && (it.getGroupedTask() == null))) {
        throw Unsupported.of("generate without single-prompt or grouped-task");
      }
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "limit", Code.num(it.getTopK()));
      final List<Code.Stmt> prelude = CollectionLiterals.<Code.Stmt>newArrayList();
      QueryInput _input = it.getInput();
      boolean _tripleNotEquals_1 = (_input != null);
      if (_tripleNotEquals_1) {
        EQueryInputMode _kind = it.getInput().getKind();
        if (_kind != null) {
          switch (_kind) {
            case TEXT:
              final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
              Code.Entries _embedding = this.schema.embedding(it.getCollection(), field);
              boolean _tripleEquals = (_embedding == null);
              if (_tripleEquals) {
                String _fieldName = ModelUtil.fieldName(field);
                String _plus_2 = ("text input for vector field " + _fieldName);
                String _plus_3 = (_plus_2 + ", which has no embedding model; Weaviate embeds query text with the vectorizer of the named vector");
                throw Unsupported.of(_plus_3);
              }
              args.put("query", Literals.of(it.getInput().getValue()));
              args.put("field", Code.text(ModelUtil.fieldName(field)));
              break;
            default:
              {
                final VectorField field_1 = this.searchField(it.getCollection(), it.getVectorField());
                args.put("vector", this.queryVector(it.getInput(), it.getCollection(), field_1, "query_vector", prelude));
                args.put("field", Code.text(ModelUtil.fieldName(field_1)));
              }
              break;
          }
        } else {
          {
            final VectorField field_1 = this.searchField(it.getCollection(), it.getVectorField());
            args.put("vector", this.queryVector(it.getInput(), it.getCollection(), field_1, "query_vector", prelude));
            args.put("field", Code.text(ModelUtil.fieldName(field_1)));
          }
        }
      }
      Literal _singlePrompt = it.getSinglePrompt();
      boolean _tripleNotEquals_2 = (_singlePrompt != null);
      if (_tripleNotEquals_2) {
        args.put("single_prompt", this.prompt(it.getCollection(), Literals.of(it.getSinglePrompt())));
      }
      Literal _groupedTask = it.getGroupedTask();
      boolean _tripleNotEquals_3 = (_groupedTask != null);
      if (_tripleNotEquals_3) {
        args.put("grouped_task", Literals.of(it.getGroupedTask()));
      }
      final VectorCollection searched = it.getCollection();
      boolean _isEmpty = it.getGroupedFields().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function1<Field, String> _function = (Field field_1) -> {
          return WeaviateSchema.propertyName(searched, field_1);
        };
        args.put("grouped_properties", Code.texts(ListExtensions.<Field, String>map(it.getGroupedFields(), _function)));
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.extensionArgs(it, args);
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      Iterables.<Code.Stmt>addAll(result, prelude);
      Code.Call _runtime = Code.runtime("generate", args);
      Code.Return _return = new Code.Return(_runtime);
      result.add(_return);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Weaviate prompts name properties as {property}; a renamed key property is referred to by its stored name.
   */
  private Code.Expr prompt(final VectorCollection collection, final Code.Expr template) {
    Code.Text _xblockexpression = null;
    {
      if ((!(template instanceof Code.Text))) {
        return template;
      }
      final String key = ModelUtil.fieldName(collection.getPrimaryKey());
      final String stored = WeaviateSchema.keyProperty(collection);
      _xblockexpression = Code.text(((Code.Text) template).value().replace((("{" + key) + "}"), (("{" + stored) + "}")));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final DistanceMatrix it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final VectorField field = this.searchField(it.getCollection(), it.getVectorField());
      final EMetric metric = this.indexMetric(it.getCollection(), field);
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "field", Code.text(ModelUtil.fieldName(field)), "metric", Code.text(this.schema.distance(metric)));
      boolean _written = ModelUtil.written(it, "sampleSize");
      if (_written) {
        args.put("sample", Code.num(it.getSampleSize()));
      }
      boolean _written_1 = ModelUtil.written(it, "matrixLimit");
      if (_written_1) {
        args.put("limit", Code.num(it.getMatrixLimit()));
      }
      VectorCollection _collection = it.getCollection();
      final WeaviateFilter where = new WeaviateFilter(_collection).where(it.getFilter());
      boolean _isEmpty = where.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("filter", where.code());
      }
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("distance_matrix", args);
      Code.Return _return = new Code.Return(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_return));
    }
    return _xblockexpression;
  }

  protected List<Code.Stmt> _body(final Sample it) {
    List<Code.Stmt> _xblockexpression = null;
    {
      this.reject(it, "asOf");
      final Code.Entries args = WeaviateSchema.kw("collection", this.layout(it.getCollection(), it), "size", Code.num(it.getSampleSize()));
      boolean _written = ModelUtil.written(it, "method");
      if (_written) {
        args.put("method", Code.text(it.getMethod().getLiteral()));
      }
      VectorCollection _collection = it.getCollection();
      this.common(it, it.getCollection(), args, it.getOutput(), new WeaviateFilter(_collection).where(it.getFilter()));
      this.extensionArgs(it, args);
      Code.Call _runtime = Code.runtime("sample", args);
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
    String _plus = (WeaviateStatements.NOT_MAPPED + _name);
    throw Unsupported.of(_plus);
  }

  private Code.Entries layout(final VectorCollection collection, final EObject query) {
    Code.Entries _xblockexpression = null;
    {
      String _xifexpression = null;
      boolean _written = ModelUtil.written(query, "consistency");
      if (_written) {
        Object _feature = this.feature(query, "consistency");
        _xifexpression = this.schema.consistency(((EConsistencyLevel) _feature));
      } else {
        _xifexpression = null;
      }
      final String level = _xifexpression;
      _xblockexpression = this.scoped(this.schema.layout(collection, level), this.tenantOf(collection, query));
    }
    return _xblockexpression;
  }

  private void common(final EObject query, final VectorCollection collection, final Code.Entries args, final OutputSpec output, final WeaviateFilter where) {
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
   * Output payload fields (the key is always returned) and the named vectors to include.
   */
  private void projection(final VectorCollection collection, final Code.Entries args, final OutputSpec output, final List<Field> excluded, final boolean withVectors) {
    final Field key = collection.getPrimaryKey();
    final List<String> fields = CollectionLiterals.<String>newArrayList();
    final List<String> vectors = CollectionLiterals.<String>newArrayList();
    final List<VectorField> stored = this.schema.storedVectors(collection);
    List<? extends Field> _xifexpression = null;
    if (((output == null) || output.isAllFields())) {
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(collection);
      _xifexpression = ((List<? extends Field>) _payloadFields);
    } else {
      _xifexpression = output.getFields();
    }
    final List<? extends Field> listed = _xifexpression;
    final Function1<Field, Boolean> _function = (Field it) -> {
      return Boolean.valueOf(((it != key) && (!excluded.contains(it))));
    };
    Iterable<? extends Field> _filter = IterableExtensions.filter(listed, _function);
    for (final Field field : _filter) {
      boolean _matched = false;
      if (field instanceof PayloadField) {
        _matched=true;
        String _propertyName = WeaviateSchema.propertyName(collection, field);
        fields.add(_propertyName);
      }
      if (!_matched) {
        if (field instanceof VectorField) {
          _matched=true;
          boolean _contains = stored.contains(field);
          if (_contains) {
            String _fieldName = ModelUtil.fieldName(field);
            vectors.add(_fieldName);
          } else {
            String _fieldName_1 = ModelUtil.fieldName(field);
            String _plus = ("output of " + _fieldName_1);
            String _plus_1 = (_plus + ", which Weaviate keeps in the inverted index");
            this.notes.add(_plus_1);
          }
        }
      }
    }
    if (withVectors) {
      final Function1<VectorField, String> _function_1 = (VectorField it) -> {
        return ModelUtil.fieldName(it);
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

  /**
   * The Weaviate tenant a statement addresses: its tenant or namespace, which a multi-tenant collection requires.
   */
  private String tenantOf(final VectorCollection collection, final EObject statement) {
    String _xblockexpression = null;
    {
      for (final String feature : Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("partitions", "partition"))) {
        boolean _written = ModelUtil.written(statement, feature);
        if (_written) {
          throw Unsupported.of(WeaviateSchema.PARTITIONS);
        }
      }
      Object _feature = this.feature(statement, "tenant");
      final Tenant named = ((Tenant) _feature);
      Object _feature_1 = this.feature(statement, "namespace");
      final String key = ((String) _feature_1);
      if (((named != null) && (key != null))) {
        throw Unsupported.of("a tenant together with a namespace; Weaviate has one tenant level");
      }
      String _xifexpression = null;
      if ((named != null)) {
        _xifexpression = WeaviateSchema.tenantName(named.getName());
      } else {
        String _xifexpression_1 = null;
        if ((key != null)) {
          _xifexpression_1 = WeaviateSchema.tenantName(key);
        } else {
          _xifexpression_1 = null;
        }
        _xifexpression = _xifexpression_1;
      }
      final String slice = _xifexpression;
      if (((slice == null) && this.schema.multiTenant(collection))) {
        String _name = collection.getName();
        String _plus = ("a request on multi-tenant collection " + _name);
        String _plus_1 = (_plus + " without a tenant or namespace; Weaviate needs one");
        throw Unsupported.of(_plus_1);
      }
      if (((slice != null) && (!this.schema.multiTenant(collection)))) {
        String _name_1 = collection.getName();
        String _plus_2 = ("a tenant or namespace on collection " + _name_1);
        String _plus_3 = (_plus_2 + ", which declares none; Weaviate enables multi-tenancy only when it creates a collection");
        throw Unsupported.of(_plus_3);
      }
      _xblockexpression = slice;
    }
    return _xblockexpression;
  }

  private Code.Entries scoped(final Code.Entries collectionLayout, final String slice) {
    Code.Entries _xblockexpression = null;
    {
      if ((slice != null)) {
        collectionLayout.put("tenant", Code.text(slice));
      }
      _xblockexpression = collectionLayout;
    }
    return _xblockexpression;
  }

  private void reject(final EObject statement, final String... features) {
    for (final String feature : features) {
      boolean _written = ModelUtil.written(statement, feature);
      if (_written) {
        final String reason = WeaviateStatements.REASONS.get(feature);
        String _elvis = null;
        String _get = WeaviateStatements.CLAUSES.get(feature);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = feature;
        }
        String _plus = ("the \'" + _elvis);
        String _plus_1 = (_plus + "\' clause");
        String _xifexpression = null;
        if ((reason == null)) {
          _xifexpression = "";
        } else {
          _xifexpression = ("; " + reason);
        }
        String _plus_2 = (_plus_1 + _xifexpression);
        throw Unsupported.of(_plus_2);
      }
    }
  }

  /**
   * Clauses Weaviate supports that the generator does not map yet.
   */
  private void pending(final EObject statement, final String... features) {
    for (final String feature : features) {
      boolean _written = ModelUtil.written(statement, feature);
      if (_written) {
        String _elvis = null;
        String _get = WeaviateStatements.CLAUSES.get(feature);
        if (_get != null) {
          _elvis = _get;
        } else {
          _elvis = feature;
        }
        String _plus = ("the \'" + _elvis);
        String _plus_1 = (_plus + "\' clause has no Weaviate mapping yet");
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
        boolean _isBm25Output = WeaviateSchema.isBm25Output(collection, explicit);
        if (_isBm25Output) {
          String _fieldName = ModelUtil.fieldName(explicit);
          String _plus = ("vector search on the bm25 output " + _fieldName);
          String _plus_1 = (_plus + "; use keyword search");
          throw Unsupported.of(_plus_1);
        }
        return explicit;
      }
      final List<VectorField> candidates = this.schema.storedVectors(collection);
      int _size = candidates.size();
      boolean _notEquals = (_size != 1);
      if (_notEquals) {
        throw Unsupported.of("choosing among several vector fields; add \'using <field>\'");
      }
      _xblockexpression = IterableExtensions.<VectorField>head(candidates);
    }
    return _xblockexpression;
  }

  private EMetric indexMetric(final VectorCollection collection, final VectorField field) {
    return this.schema.metric(field, ModelUtil.vectorIndex(collection, field));
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
              Code.Call _runtime = Code.runtime("lookup_vector", WeaviateSchema.kw("collection", this.schema.layout(source, null), 
                "field", Code.text(ModelUtil.fieldName(field)), "id", Literals.of(input.getValue())));
              Code.Let _let = new Code.Let(variable, _runtime);
              prelude.add(_let);
              _xblockexpression_1 = new Code.Ref(variable);
            }
            _switchResult = _xblockexpression_1;
            break;
          case TEXT:
            String _fieldName = ModelUtil.fieldName(field);
            String _plus = ("text input for vector field " + _fieldName);
            String _plus_1 = (_plus + " (Weaviate vectorizer modules are not generated)");
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
   * Weaviate filters vector hits by a maximum distance; the runtime applies the other bound to the reported score.
   */
  private void vectorBounds(final ScoreBounds bounds, final EMetric metric, final boolean nativeScore, final Code.Entries args) {
    if (nativeScore) {
      args.put("native", Code.flag(true));
    }
    if ((bounds == null)) {
      return;
    }
    if (nativeScore) {
      boolean _written = ModelUtil.written(bounds, "max");
      if (_written) {
        args.put("max_distance", Code.num(bounds.getMax()));
      }
      boolean _written_1 = ModelUtil.written(bounds, "min");
      if (_written_1) {
        args.put("score_min", Code.num(bounds.getMin()));
      }
      return;
    }
    boolean _written_2 = ModelUtil.written(bounds, "min");
    if (_written_2) {
      args.put("max_distance", Code.num(this.distance(metric, bounds.getMin())));
    }
    boolean _written_3 = ModelUtil.written(bounds, "max");
    if (_written_3) {
      args.put("score_max", Code.num(bounds.getMax()));
    }
  }

  /**
   * Weaviate distance for a canonical similarity.
   */
  private double distance(final EMetric metric, final double similarity) {
    double _switchResult = (double) 0;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = (1 - similarity);
          break;
        case EUCLIDEAN:
          double _xblockexpression = (double) 0;
          {
            if ((similarity > 0)) {
              throw Unsupported.of("a positive similarity bound on euclidean distance");
            }
            _xblockexpression = (similarity * similarity);
          }
          _switchResult = _xblockexpression;
          break;
        default:
          _switchResult = (-similarity);
          break;
      }
    } else {
      _switchResult = (-similarity);
    }
    return _switchResult;
  }

  private void keywordFields(final VectorCollection collection, final List<SearchFieldRef> refs, final Code.Entries args) {
    final List<String> properties = CollectionLiterals.<String>newArrayList();
    for (final SearchFieldRef ref : refs) {
      {
        String _xifexpression = null;
        boolean _written = ModelUtil.written(ref, "boost");
        if (_written) {
          String _number = Syntax.number(ref.getBoost());
          _xifexpression = ("^" + _number);
        } else {
          _xifexpression = "";
        }
        final String boost = _xifexpression;
        final Function function = ModelUtil.producingFunction(collection, ref.getField());
        List<Field> _xifexpression_1 = null;
        if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
          _xifexpression_1 = function.getInputs();
        } else {
          Field _field = ref.getField();
          _xifexpression_1 = Collections.<Field>unmodifiableList(CollectionLiterals.<Field>newArrayList(_field));
        }
        final List<Field> fields = _xifexpression_1;
        for (final Field field : fields) {
          {
            if ((!(field instanceof PayloadField))) {
              String _fieldName = ModelUtil.fieldName(field);
              String _plus = ("keyword search on vector field " + _fieldName);
              throw Unsupported.of(_plus);
            }
            String _propertyName = WeaviateSchema.propertyName(collection, field);
            String _plus_1 = (_propertyName + boost);
            properties.add(_plus_1);
          }
        }
      }
    }
    boolean _isEmpty = properties.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      args.put("properties", Code.texts(properties.stream().distinct().toList()));
    }
  }

  private void bm25Operator(final KeywordSearch search, final Code.Entries args) {
    if ((ModelUtil.written(search, "matchOperator") && Objects.equals(search.getMatchOperator(), EMatchOperator.AND))) {
      boolean _written = ModelUtil.written(search, "minShouldMatch");
      if (_written) {
        throw Unsupported.of("min-should-match with operator \'and\'");
      }
      args.put("operator", Code.text("and"));
    } else {
      boolean _written_1 = ModelUtil.written(search, "minShouldMatch");
      if (_written_1) {
        args.put("minimum_match", Code.num(search.getMinShouldMatch()));
      }
    }
  }

  private void searchParams(final SearchParams params, final Code.Entries args) {
    if ((params == null)) {
      return;
    }
    boolean _written = ModelUtil.written(params, "targetRecall");
    if (_written) {
      throw Unsupported.of("target-recall: Weaviate exposes no query-time recall target");
    }
    boolean _written_1 = ModelUtil.written(params, "candidatePool");
    if (_written_1) {
      throw Unsupported.of("candidates; Weaviate sets ef on the vector index, not per query");
    }
    this.schema.note(params, "probes", "scanBudget", "timeoutMillis", "oversampling", "rescore", "filterStrategy", "exhaustive");
    this.extensionArgs(params, args);
  }

  private void extensionArgs(final EObject host, final Code.Entries args) {
    Set<Map.Entry<String, Code.Expr>> _entrySet = this.schema.extValues(host).entrySet();
    for (final Map.Entry<String, Code.Expr> entry : _entrySet) {
      String _key = entry.getKey();
      if (_key != null) {
        switch (_key) {
          case "autocut":
            args.put("auto_limit", entry.getValue());
            break;
          default:
            String _key_1 = entry.getKey();
            String _plus = ("extension parameter " + _key_1);
            throw Unsupported.of(_plus);
        }
      } else {
        String _key_1 = entry.getKey();
        String _plus = ("extension parameter " + _key_1);
        throw Unsupported.of(_plus);
      }
    }
  }

  private Code.Expr rows(final RecordSource source) {
    Code.Expr _switchResult = null;
    boolean _matched = false;
    if (source instanceof FileSource) {
      _matched=true;
      throw Unsupported.of("records read from files");
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
