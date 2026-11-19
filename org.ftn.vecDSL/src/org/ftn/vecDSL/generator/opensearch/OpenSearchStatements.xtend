package org.ftn.vecDSL.generator.opensearch

import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.common.ShapeSpec
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AddAliasOp
import org.ftn.vecDSL.vecDSL.AddFieldOp
import org.ftn.vecDSL.vecDSL.AddFunctionOp
import org.ftn.vecDSL.vecDSL.AggregationClause
import org.ftn.vecDSL.vecDSL.Alias
import org.ftn.vecDSL.vecDSL.Alter
import org.ftn.vecDSL.vecDSL.AlterOperation
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.AnalyzerFilter
import org.ftn.vecDSL.vecDSL.AuthConfig
import org.ftn.vecDSL.vecDSL.AwaitIndex
import org.ftn.vecDSL.vecDSL.BackupSchedule
import org.ftn.vecDSL.vecDSL.BatchQuery
import org.ftn.vecDSL.vecDSL.BoostBlock
import org.ftn.vecDSL.vecDSL.BulkImport
import org.ftn.vecDSL.vecDSL.Clone
import org.ftn.vecDSL.vecDSL.Cluster
import org.ftn.vecDSL.vecDSL.ClusterLimit
import org.ftn.vecDSL.vecDSL.CollectionLimit
import org.ftn.vecDSL.vecDSL.Compact
import org.ftn.vecDSL.vecDSL.Concept
import org.ftn.vecDSL.vecDSL.Connection
import org.ftn.vecDSL.vecDSL.Count
import org.ftn.vecDSL.vecDSL.Create
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.Delete
import org.ftn.vecDSL.vecDSL.Describe
import org.ftn.vecDSL.vecDSL.DistanceMatrix
import org.ftn.vecDSL.vecDSL.Drop
import org.ftn.vecDSL.vecDSL.DropFieldOp
import org.ftn.vecDSL.vecDSL.DropFunctionOp
import org.ftn.vecDSL.vecDSL.DropSnapshot
import org.ftn.vecDSL.vecDSL.EAuthType
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.ESampleMethod
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESortDirection
import org.ftn.vecDSL.vecDSL.EUpdateMode
import org.ftn.vecDSL.vecDSL.EmbeddingMigration
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.FieldLimit
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.FileResource
import org.ftn.vecDSL.vecDSL.FileSource
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.Flush
import org.ftn.vecDSL.vecDSL.Function
import org.ftn.vecDSL.vecDSL.GenerativeModel
import org.ftn.vecDSL.vecDSL.GenerativeSearch
import org.ftn.vecDSL.vecDSL.GetById
import org.ftn.vecDSL.vecDSL.HybridSearch
import org.ftn.vecDSL.vecDSL.Index
import org.ftn.vecDSL.vecDSL.InlineRecords
import org.ftn.vecDSL.vecDSL.Insert
import org.ftn.vecDSL.vecDSL.KeywordLeg
import org.ftn.vecDSL.vecDSL.KeywordSearch
import org.ftn.vecDSL.vecDSL.ListLit
import org.ftn.vecDSL.vecDSL.ListObjects
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.Load
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.MetricsConfig
import org.ftn.vecDSL.vecDSL.MmrConfig
import org.ftn.vecDSL.vecDSL.Namespace
import org.ftn.vecDSL.vecDSL.NumberLit
import org.ftn.vecDSL.vecDSL.OutputSpec
import org.ftn.vecDSL.vecDSL.ParamRef
import org.ftn.vecDSL.vecDSL.ParamSource
import org.ftn.vecDSL.vecDSL.Partition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.QueryInput
import org.ftn.vecDSL.vecDSL.RebuildIndex
import org.ftn.vecDSL.vecDSL.RecommendSearch
import org.ftn.vecDSL.vecDSL.RecordLiteral
import org.ftn.vecDSL.vecDSL.RecordSource
import org.ftn.vecDSL.vecDSL.RefreshExternal
import org.ftn.vecDSL.vecDSL.Release
import org.ftn.vecDSL.vecDSL.RenameFieldOp
import org.ftn.vecDSL.vecDSL.ReplicationConfig
import org.ftn.vecDSL.vecDSL.Reranker
import org.ftn.vecDSL.vecDSL.Restore
import org.ftn.vecDSL.vecDSL.RoleBinding
import org.ftn.vecDSL.vecDSL.Sample
import org.ftn.vecDSL.vecDSL.ScoreBounds
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.Scroll
import org.ftn.vecDSL.vecDSL.SearchFieldRef
import org.ftn.vecDSL.vecDSL.SearchParams
import org.ftn.vecDSL.vecDSL.SetPropertyOp
import org.ftn.vecDSL.vecDSL.ShardConfig
import org.ftn.vecDSL.vecDSL.Snapshot
import org.ftn.vecDSL.vecDSL.SortClause
import org.ftn.vecDSL.vecDSL.SparseLit
import org.ftn.vecDSL.vecDSL.StorageConfig
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.SwapAlias
import org.ftn.vecDSL.vecDSL.Tenant
import org.ftn.vecDSL.vecDSL.Transaction
import org.ftn.vecDSL.vecDSL.Truncate
import org.ftn.vecDSL.vecDSL.Update
import org.ftn.vecDSL.vecDSL.Upsert
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorDatabase
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex
import org.ftn.vecDSL.vecDSL.VectorLeg
import org.ftn.vecDSL.vecDSL.VectorSearch
import org.ftn.vecDSL.vecDSL.VendorExtension

import static org.ftn.vecDSL.generator.opensearch.OpenSearchBodies.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to OpenSearch REST requests in the host-language-neutral code model. Every request body is built
 * here as a JSON value and passed to the runtime, so the three host languages send identical requests: request sends one
 * request, search and fetch shape hits into records with the canonical score, write sends _bulk requests, hybrid fuses the
 * legs of a weighted or relative-score hybrid search (rrf is the server's score-ranker-processor), and recommend computes the
 * query vector of a recommendation from stored examples.
 */
class OpenSearchStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; OpenSearchCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for OpenSearch: '
	/** Suffix of constructs OpenSearch supports that the generator does not produce yet. */
	public static val PENDING = 'has no OpenSearch mapping yet'
	public static val NO_MODELS = '; models run through ML Commons, which registers and deploys a model (downloaded from a model repository or connected to a remote service) before a pipeline can call it, so generated programs supply their own vectors'
	public static val EXTERNAL = 'collections derived from external sources; OpenSearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines'
	public static val NO_TTL = 'ttl; OpenSearch documents do not expire (Index State Management deletes whole indices, not single documents)'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; OpenSearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)'
	public static val NO_TAGS_REASON = 'OpenSearch keeps them in the _meta of the mapping'
	public static val NO_FILES = 'OpenSearch reads analysis files from the config directory of the node, which a client cannot write'
	public static val LANGUAGES_REFERENCE = 'OpenSearch has stop-word lists and stemmers for its language analyzers (english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian, turkish, serbian and others)'
	public static val NO_TRANSACTIONS = 'transactions; OpenSearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)'
	public static val NO_GENERATION = 'generative models run through ML Commons connectors to remote services (the retrieval_augmented_generation response processor), which need a registered model and the credentials of that service'
	public static val NO_RERANKERS = 'the rerank response processor needs a cross-encoder model deployed through ML Commons (type ml_opensearch); its by_field type sorts by a document field and is no reranking model'
	public static val NO_METRICS = 'metrics configuration; OpenSearch metrics come from the stats APIs and the Performance Analyzer plugin, not from a per-program configuration'
	public static val NO_CLUSTER_LIMITS = 'cluster limits; OpenSearch limits shards per node (cluster.max_shards_per_node), not the number of indices'
	public static val ONE_CLUSTER = 'the program talks to one OpenSearch cluster; clusters are deployments, created outside the REST API'
	public static val NO_SERVER_AUTH = 'authentication domains of the security plugin (JWT, OpenID Connect, SAML, client certificates) are configured in config.yml on the server and applied with securityadmin.sh, not through the REST API'
	static val FIXED_SHARDS = 'the shard count of an existing index is fixed (the split and shrink APIs copy it into a new index)'
	static val FIXED_METHOD = 'the method of a knn_vector field (engine, parameters and encoder) cannot be updated after the field is created'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain',
		'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'sortClauses' -> 'sort', 'aggregations' -> 'aggregate',
		'prefetches' -> 'prefetch', 'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model', 'sliceIndex' -> 'slice'
	}

	/** Why OpenSearch cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'OpenSearch searches the current state of an index (a point in time pins a state for paging, not a past one)',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated OpenSearch programs embed no query text' + NO_MODELS
	}

	/** Clauses OpenSearch supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'a knn query on a knn_vector field inside a nested field', 'prefetches' -> 'a rescore query over the stage candidates'
	}

	/** Hits a search reads for a cursor page: the k of its knn query, as the Elasticsearch generator's cursor. */
	static val CURSOR_WINDOW = 1000

	/** Highlighters mark the terms of a text query. */
	static val NO_QUERY_TERMS = 'highlighting marks the terms of a text query, which '

	/** Search body parameters that an 'extension opensearch' block on a query may set. */
	static val SEARCH_KEYS = #['terminate_after', 'min_score', 'track_scores', 'timeout']

	val List<String> notes = newArrayList
	val OpenSearchSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new OpenSearchSchema(notes, script)
	}

	/** The method for one statement, followed by helper methods it calls (batch queries). */
	def List<Program.Method> methods(Method statement, String name) {
		notes.clear
		helpers.clear
		current = name
		val List<Stmt> statements = try {
			body(statement)
		} catch (Unsupported e) {
			helpers.clear
			newArrayList(new Code.Fail(e.message) as Stmt)
		}
		val List<Program.Method> result = newArrayList
		result += new Program.Method(name, ModelUtil.parameterNames(statement), statements, notes.stream.distinct.toList, newArrayList, false)
		result += helpers
		result
	}

	def static String functionReason(String type) {
		switch type {
			case 'bm25': 'OpenSearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (rank_features fields take their weights from the client or a sparse encoding model)'
			case 'custom': 'custom functions; ' + PENDING + ' (an ingest pipeline with a script processor)'
			default: 'model functions run in ingest pipelines of ML Commons (text_embedding, sparse_encoding), which need a deployed model' + NO_MODELS
		}
	}

	/** Index store settings of a storage block: mmap is the mmapfs store; the other kinds are node features. */
	def static void storage(StorageConfig storage, Entries settings) {
		if (storage === null) return;
		if (storage.location !== null) throw Unsupported.of('a storage location; OpenSearch keeps index data under path.data, a node setting')
		if (storage.tiered) throw Unsupported.of('tiered storage; data tiers move indices between nodes of different roles, and one node has one tier')
		switch storage.type.literal {
			case 'disk': settings.put('index.store.type', text(if (storage.mmap) 'mmapfs' else 'niofs'))
			case 'memory': throw Unsupported.of('memory storage; OpenSearch stores indices on disk (index.store.type takes fs, niofs, mmapfs and hybridfs)')
			case 'object_storage': throw Unsupported.of('object storage; searchable snapshots mount indices from a repository only on nodes with the search role, and remote-backed storage is a node setting, while the compose node has the default roles')
			default: throw Unsupported.of('embedded storage; OpenSearch runs as a server')
		}
	}

	/** Shards and replicas of the indices of a cluster's collections: the shard count and the replication factor minus one. */
	def static void clusterSettings(Cluster cluster, Entries settings) {
		if (cluster === null) return;
		if (cluster.sharding !== null) {
			val sharding = cluster.sharding
			if (sharding.key !== null || (written(sharding, 'strategy') && sharding.strategy.literal != 'hash')) {
				throw Unsupported.of('sharding by key or with the ' + sharding.strategy.literal + ' strategy; ' + PENDING + ' (custom routing)')
			}
			if (sharding.shards < 1) throw Unsupported.of('a shard count below 1')
			settings.put('number_of_shards', num(sharding.shards))
		}
		if (cluster.replication !== null) {
			if (cluster.replication.factor < 1) throw Unsupported.of('a replication factor below 1')
			settings.put('number_of_replicas', num(cluster.replication.factor - 1))
		}
	}

	/** Collection limits as index settings: max_fields is index.mapping.total_fields.limit. */
	def static void collectionLimit(CollectionLimit limit, Entries settings) {
		switch limit.type.literal {
			case 'max_fields': settings.put('index.mapping.total_fields.limit', num(limit.value))
			default: throw Unsupported.of('collection limits (' + limit.type.literal + '); OpenSearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit')
		}
	}

	/** Partitions are values of a keyword field with a filtered alias each; strategies other than list need routing. */
	def static void checkPartition(Partition partition) {
		if (partition.key !== null || (written(partition, 'strategy') && partition.strategy.literal != 'list')) {
			throw Unsupported.of('partitions by key or with the ' + partition.strategy.literal + ' strategy; ' + PENDING + ' (custom routing)')
		}
		if (!partition.subPartitions.empty) throw Unsupported.of('sub-partitions; ' + PENDING + ' (a partition path in the keyword field)')
	}

	// ---------------------------------------------------------------- DDL

	def dispatch List<Stmt> body(Create it) {
		val List<Stmt> result = newArrayList
		result += define(definition ?: target, ifNotExists, definition !== null)
		for (statement : nested) result += body(statement)
		result
	}

	def List<Stmt> define(Concept concept, boolean ifNotExists, boolean fresh) {
		val List<Stmt> result = newArrayList
		switch concept {
			VectorCollection: result += schema.createIndex(concept, ifNotExists, fresh)
			Connection: connection(concept)
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(NO_METRICS)
				if (concept.connection !== null) connection(concept.connection)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists, fresh)
				for (database : concept.databases) result += define(database, ifNotExists, fresh)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists, fresh)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of(NO_CLUSTER_LIMITS)
				for (key : ModelUtil.extParams(concept, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
				if (concept.collections.empty && (concept.replication !== null || concept.sharding !== null)) {
					throw Unsupported.of('replication and sharding of a cluster without collections; OpenSearch sets shards and replicas per index')
				}
				for (collection : concept.collections) result += schema.createIndex(collection, ifNotExists, fresh)
			}
			Database: for (collection : concept.collections) result += schema.createIndex(collection, ifNotExists, fresh)
			Alias: {
				val target = schema.index(concept.collection)
				result += new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('add', obj('index', text(target.name), 'alias', text(aliasName(concept.name))))))))
			}
			VectorIndex: throw Unsupported.of('a vector index on an existing field; ' + FIXED_METHOD + ' (declare the index with the field)')
			PayloadIndex: result += payloadIndex(concept)
			VectorField: result += addField(member(concept, 'a field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += slice(member(concept, 'a partition'), 'partition', concept.name, concept)
			Tenant: result += slice(member(concept, 'a tenant'), 'tenant', concept.name, concept)
			Namespace: result += slice(member(concept, 'a namespace'), 'namespace', concept.key, concept)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			FileResource: throw Unsupported.of('file resources; ' + NO_FILES)
			Analyzer: result += reanalyze(concept)
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; a token filter belongs to the analyzer that names it')
				result += reanalyze(analyzer)
			}
			FieldLimit: throw Unsupported.of('field limits (' + concept.type.literal + '); ' + PENDING + ' (an ingest pipeline with fail processors)')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: {
				throw Unsupported.of('quantization of an existing field; ' + FIXED_METHOD + ' (declare the quantization with the field)')
			}
			ReplicationConfig: {
				val cluster = EcoreUtil2.getContainerOfType(concept, Cluster)
				if (cluster === null) throw Unsupported.of('replication outside a cluster; ' + ONE_CLUSTER)
				result += replicas(cluster, concept)
			}
			ShardConfig: throw Unsupported.of('sharding of existing indices; ' + FIXED_SHARDS)
			StorageConfig: {
				val collection = EcoreUtil2.getContainerOfType(concept, VectorCollection)
				if (collection === null) throw Unsupported.of('storage settings outside a collection; OpenSearch sets the store type per index')
				val Entries settings = Code.entries
				storage(concept, settings)
				result += reopened(schema.index(collection).name, settings)
			}
			AuthConfig: result += authentication(concept)
			RoleBinding: result += role(concept)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: result += backupSchedule(concept)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: {
				val collection = EcoreUtil2.getContainerOfType(concept, VectorCollection)
				if (collection === null) throw Unsupported.of('a collection limit outside a collection')
				val Entries settings = Code.entries
				collectionLimit(concept, settings)
				result += new Code.Run(request('PUT', '/' + schema.index(collection).name + '/_settings', null, settings))
			}
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). Passwords and API keys come from the environment only. */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; OpenSearch runs as a server that the clients reach over HTTP')
		}
		if (concept.token !== null) throw Unsupported.of('bearer tokens; ' + NO_SERVER_AUTH)
		if (concept.password instanceof StringLit || concept.apiKey instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; the gRPC transport of OpenSearch serves only search and bulk requests, so the generated programs use the REST API')
		if (concept.databaseName !== null) throw Unsupported.of('a database in the connection; OpenSearch has no databases (logical databases are index name prefixes)')
		for (key : ModelUtil.extParams(concept, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(concept, 'cloud', 'region', 'project')
	}

	/** Replicas of the indices of a cluster's collections: number_of_replicas is the replication factor minus one. */
	def private List<Stmt> replicas(Cluster cluster, ReplicationConfig replication) {
		if (replication.factor < 1) throw Unsupported.of('a replication factor below 1')
		if (cluster.collections.empty) throw Unsupported.of('replication of a cluster without collections; OpenSearch sets replicas per index')
		val names = cluster.collections.map[c | schema.index(c).name].join(',')
		#[new Code.Run(request('PUT', '/' + names + '/_settings', null, obj('index.number_of_replicas', num(replication.factor - 1))))]
	}

	/**
	 * Authentication: username_password and api_key are the connection's (internal users of the security plugin, API keys since
	 * 3.7); roles are role definitions; audit logging is switched on through the audit API; the other authentication domains
	 * are server configuration.
	 */
	def private List<Stmt> authentication(AuthConfig auth) {
		switch auth.type {
			case EAuthType.TOKEN: throw Unsupported.of('token authentication; ' + NO_SERVER_AUTH)
			case EAuthType.MTLS: throw Unsupported.of('mtls authentication; ' + NO_SERVER_AUTH)
			case EAuthType.SSO: throw Unsupported.of('sso authentication; ' + NO_SERVER_AUTH)
			default: {}
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; ' + NO_SERVER_AUTH)
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; OpenSearch encrypts at rest only through the file system or the cloud provider')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding)
		if (auth.auditLog) {
			result += new Code.Run(request('PATCH', '/_plugins/_security/api/audit', null, arr(obj('op', text('replace'), 'path', text('/config/enabled'), 'value', new Code.Flag(true)))))
		}
		result
	}

	/** A role with index permissions (action groups) on a collection's index (or on every index); PUT creates or replaces it. */
	def private List<Stmt> role(RoleBinding binding) {
		val names = if (binding.collection === null) text('*') else text(schema.index(binding.collection).name)
		val grants = privileges(binding.privileges)
		val Entries roleBody = obj('description', if (binding.description === null) null else text(binding.description),
			'index_permissions', arr(obj('index_patterns', arr(names), 'allowed_actions', arr(if (grants.empty) #[text('read') as Expr] else grants))))
		#[new Code.Run(request('PUT', '/_plugins/_security/api/roles/' + roleName(binding.roleName), null, roleBody))]
	}

	def private String roleName(String declared) {
		if (!declared.matches('[A-Za-z0-9_][A-Za-z0-9_.@-]*')) throw Unsupported.of('the role name ' + declared + '; the generator writes role names of letters, digits and _ . @ -')
		declared
	}

	/** A snapshot management policy (index management plugin) on the fs repository of the compose file, scheduled by cron. */
	def private List<Stmt> backupSchedule(BackupSchedule schedule) {
		val repository = repository(schedule.location)
		val target = schema.index(schedule.collection)
		val cron = obj('cron', obj('expression', text(OpenSearchBodies.schedule(schedule.interval)), 'timezone', text('UTC')))
		val policy = obj('creation', obj('schedule', cron),
			'deletion', if (written(schedule, 'retentionCount')) obj('schedule', cron, 'condition', obj('max_count', num(schedule.retentionCount), 'min_count', num(1))) else null,
			'snapshot_config', obj('repository', text(repository), 'indices', text(target.name), 'include_global_state', new Code.Flag(false)))
		#[registerRepository(repository), new Code.Run(request('POST', '/_plugins/_sm/policies/' + aliasName(schedule.name), null, policy))]
	}

	/** The snapshot repository of a location: vecdsl (directory vecdsl under path.repo) by default, else the location's name. */
	def private String repository(String location) {
		if (location === null) 'vecdsl' else aliasName(location.replaceAll('[^A-Za-z0-9]+', '_'))
	}

	def private Stmt registerRepository(String repository) {
		new Code.Run(request('PUT', '/_snapshot/' + repository, null, obj('type', text('fs'), 'settings', obj('location', text(repository)))))
	}

	/** The collection of a member concept: its container, or the collection of the enclosing create. */
	def private VectorCollection member(EObject concept, String what) {
		val direct = ModelUtil.owningCollection(concept)
		if (direct !== null) return direct
		val statement = EcoreUtil2.getContainerOfType(concept, Create)
		val outer = if (statement === null) null else statement.eContainer
		if (outer instanceof Create) {
			val enclosing = (outer as Create).definition ?: (outer as Create).target
			if (enclosing instanceof VectorCollection) return enclosing as VectorCollection
		}
		throw Unsupported.of(what + ' outside a collection; OpenSearch adds it to an index')
	}

	/** A tenant, namespace or partition: a filtered alias on the slice's keyword field, which the mapping gains when missing. */
	def private List<Stmt> slice(VectorCollection collection, String kind, String value, EObject concept) {
		if (concept instanceof Partition) checkPartition(concept as Partition)
		var target = schema.index(collection)
		val List<Stmt> result = newArrayList
		val fieldName = switch kind { case 'tenant': target.tenant case 'namespace': target.namespace default: target.partition }
		var sliceField = fieldName
		if (fieldName === null) {
			if (kind == 'partition') throw Unsupported.of('a partition of a collection declared without partitions; declare a partition in the collection, which adds the partition field')
			sliceField = if (kind == 'tenant') TENANT_FIELD else NAMESPACE_FIELD
			result += new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('properties', obj(sliceField, obj('type', text('keyword'))))))
			target = if (kind == 'tenant') new OpenSearchIndex(target.name, target.key, target.fields, sliceField, target.namespace, target.partition, target.renames, target.database)
				else new OpenSearchIndex(target.name, target.key, target.fields, target.tenant, sliceField, target.partition, target.renames, target.database)
			schema.evolve(collection, target)
		}
		val alias = obj('index', text(target.name), 'alias', text(sliceAlias(target.name, kind, value)), 'filter', OpenSearchSchema.term(sliceField, text(value)))
		result += new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('add', alias)))))
		result
	}

	/** Analysis settings change on a closed index: close, update the analyzer, open again and wait until it is usable. */
	def private List<Stmt> reanalyze(Analyzer analyzer) {
		val collection = ModelUtil.owningCollection(analyzer)
		if (collection === null) throw Unsupported.of('an analyzer outside a collection; OpenSearch defines analyzers in the settings of an index')
		val Entries settings = obj('analysis', schema.analysis(#[analyzer]))
		val similarityMap = schema.similarities(#[analyzer])
		if (!similarityMap.entries.empty) settings.put('similarity', similarityMap)
		reopened(schema.index(collection).name, settings)
	}

	def private List<Stmt> reopened(String name, Entries settings) {
		#[new Code.Run(request('POST', '/' + name + '/_close', null, null)),
			new Code.Run(request('PUT', '/' + name + '/_settings', null, settings)),
			new Code.Run(request('POST', '/' + name + '/_open', null, null)),
			new Code.Run(call('wait_health', 'index', text(name), 'status', text('yellow'), 'timeout_seconds', num(60)))]
	}

	/** A payload index on an existing field: every mapped field is indexed, so the request re-sends the field's mapping. */
	def private List<Stmt> payloadIndex(PayloadIndex payloadIndex) {
		val owner = payloadIndex.target.field
		val collection = ModelUtil.targetCollection(payloadIndex) ?: (if (owner === null) null else ModelUtil.owningCollection(owner))
		if (collection === null) throw Unsupported.of('a payload index outside a collection')
		val target = schema.index(collection)
		schema.checkPayloadIndex(target, payloadIndex)
		if (owner === null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields')
		val mapped = target.field(ModelUtil.fieldName(owner))
		#[new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('properties', obj(mapped.name, mapped.mapping))))]
	}

	/** A field added to an existing index; a default value is written into the documents that lack it. */
	def private List<Stmt> addField(VectorCollection collection, Field added) {
		val target = schema.index(collection)
		val mapped = schema.field(collection, added)
		val List<Stmt> result = newArrayList
		result += new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('properties', obj(mapped.name, mapped.mapping))))
		if (mapped.defaultValue !== null) {
			result += new Code.Run(byQuery(target, '_update_by_query', obj('bool', obj('must_not', arr(obj('exists', obj('field', text(mapped.name)))))),
				script(DEFAULT_SCRIPT, obj('field', text(mapped.name), 'value', mapped.defaultValue)), 'updated', 'update_count', null))
		}
		schema.evolve(collection, target.with(mapped))
		result
	}

	/** An update_by_query or delete_by_query request with refresh, counting the documents it changed. */
	def private Code.Call byQuery(OpenSearchIndex target, String endpoint, Entries matching, Entries painless, String counter, String wrap, Integer maxDocs) {
		val requestBody = obj('query', matching, 'script', painless, 'max_docs', if (maxDocs === null) null else num(maxDocs))
		call('request', 'method', text('POST'), 'path', text('/' + target.name + '/' + endpoint), 'params', obj('refresh', text('true'), 'conflicts', text('proceed')),
			'body', requestBody, 'select', arr(text(counter)), 'wrap', if (wrap === null) null else text(wrap))
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias can be renamed (removed and added in one _aliases request)')
			val alias = concept as Alias
			val aliasTarget = schema.index(alias.collection)
			return #[new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(
				obj('remove', obj('index', text(aliasTarget.name), 'alias', text(aliasName(alias.name)))),
				obj('add', obj('index', text(aliasTarget.name), 'alias', text(aliasName(newName)))))))) as Stmt]
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; OpenSearch alters indices and aliases')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) result += rename(collection, newName)
		if (result.empty) result += new Code.Run(request('GET', '/' + schema.index(collection).name + '/_mapping', null, null))
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val target = schema.index(collection)
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: dropField(collection, operation.field)
			RenameFieldOp: {
				if (operation.field === collection.primaryKey) throw Unsupported.of('renaming the primary key; the generated requests address the key by its declared name')
				val old = target.field(ModelUtil.fieldName(operation.field))
				val newField = OpenSearchBodies.field(operation.newName)
				val renamedIndex = target.renamed(ModelUtil.fieldName(operation.field), newField)
				schema.evolve(collection, renamedIndex)
				#[new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('properties', obj(newField, old.mapping)))),
					new Code.Run(byQuery(target, '_update_by_query', obj('exists', obj('field', text(old.name))),
						script(RENAME_SCRIPT, obj('from', text(old.name), 'to', text(newField))), 'updated', 'update_count', null))]
			}
			SetPropertyOp: setProperty(target, operation)
			AddAliasOp: #[new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('add', obj('index', text(target.name), 'alias', text(aliasName(operation.newAlias.name)))))))) as Stmt]
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	/**
	 * A field leaves the documents (update_by_query removes it from _source); the mapping keeps the field, since OpenSearch
	 * cannot remove a field from a mapping.
	 */
	def private List<Stmt> dropField(VectorCollection collection, Field dropped) {
		if (dropped === collection.primaryKey) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
		val target = schema.index(collection)
		val old = target.field(ModelUtil.fieldName(dropped))
		schema.evolve(collection, target.without(ModelUtil.fieldName(dropped)))
		#[new Code.Run(byQuery(target, '_update_by_query', obj('exists', obj('field', text(old.exact))),
			script(DROP_SCRIPT, obj('field', text(old.name))), 'updated', 'update_count', null))]
	}

	/** Collection properties: description and tags live in the _meta of the mapping, dynamic is the mapping's dynamic setting. */
	def private List<Stmt> setProperty(OpenSearchIndex target, SetPropertyOp operation) {
		val List<Stmt> result = newArrayList
		switch operation.property {
			case DESCRIPTION: {
				if (!(operation.value instanceof StringLit)) throw Unsupported.of('a description that is not a string literal')
				result += new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('_meta', obj('description', Literals.of(operation.value)))))
			}
			case TAGS: {
				if (!(operation.value instanceof ListLit)) throw Unsupported.of('tags that are not a list literal')
				result += new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('_meta', obj('tags', Literals.of(operation.value)))))
			}
			case DYNAMIC: result += new Code.Run(request('PUT', '/' + target.name + '/_mapping', null,
				obj('dynamic', text(if (operation.value instanceof org.ftn.vecDSL.vecDSL.BoolLit && (operation.value as org.ftn.vecDSL.vecDSL.BoolLit).value) 'true' else 'strict'))))
			case CONSISTENCY: notes += 'consistency on SetPropertyOp, which every refreshed write meets'
			case TTL: throw Unsupported.of(NO_TTL)
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
		}
		result
	}

	/**
	 * A renamed index: the write-blocked index is cloned under the new name (hard-linked segments, same mapping and settings),
	 * the old index deleted and the write block lifted. Aliases declared in the collection move with it.
	 */
	def private List<Stmt> rename(VectorCollection collection, String newName) {
		val target = schema.index(collection)
		val renamedIndex = target.named(indexName(target.database, newName))
		val Entries aliases = Code.entries
		for (alias : collection.aliases) aliases.put(aliasName(alias.name), Code.entries)
		schema.evolve(collection, renamedIndex)
		#[new Code.Run(request('PUT', '/' + target.name + '/_settings', null, obj('index.blocks.write', new Code.Flag(true)))),
			new Code.Run(request('POST', '/' + target.name + '/_clone/' + renamedIndex.name, null, obj('aliases', aliases))),
			new Code.Run(request('DELETE', '/' + target.name, null, null)),
			new Code.Run(request('PUT', '/' + renamedIndex.name + '/_settings', null, obj('index.blocks.write', new Code.Nil)))]
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		val ignore = if (ifExists) arr(num(404)) else null
		switch concept {
			VectorCollection: {
				val dropped = schema.index(concept)
				schema.evolve(concept, null)
				#[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/' + dropped.name), 'ignore', ignore)) as Stmt]
			}
			Database: #[new Code.Run(call('drop_matching', 'pattern', text(indexName(concept.name, 'x').replaceFirst('x$', '*')))) as Stmt]
			Tenant: dropSlice(member(concept, 'a tenant'), 'tenant', concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), 'namespace', concept.key)
			Partition: dropSlice(member(concept, 'a partition'), 'partition', concept.name)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a field'), concept)
			// Every index but the dot-prefixed system indices: with the security plugin, a bare * also names .opendistro_security,
			// which even the admin user may not change (403 "no permissions for []"); vecDSL index names never start with a dot.
			// DELETE /*,-.*/_alias/<name> removes the alias from every index that has it and answers 404 only when no index has it;
			// a remove action on * with must_exist true fails on the first index without the alias. The -.* keeps out system indices.
			Alias: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/*,-.*/_alias/' + aliasName(concept.name)), 'ignore', ignore)) as Stmt]
			VectorIndex: throw Unsupported.of('dropping a vector index; ' + FIXED_METHOD)
			PayloadIndex: throw Unsupported.of('dropping a payload index; whether a field is indexed is part of its mapping, which OpenSearch cannot change')
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; ' + FIXED_METHOD)
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not an OpenSearch object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; ' + ONE_CLUSTER)
			Cluster: throw Unsupported.of('dropping a cluster; ' + ONE_CLUSTER)
			Analyzer: throw Unsupported.of('dropping an analyzer; analysis settings are removed with their index, not while fields use them')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; analysis settings are removed with their index, not while fields use them')
			FieldLimit: throw Unsupported.of('field limits (' + concept.type.literal + '); ' + PENDING + ' (an ingest pipeline with fail processors)')
			FileResource: throw Unsupported.of('file resources; ' + NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('dropping replication; set the replication factor 1 instead (number_of_replicas 0)')
			ShardConfig: throw Unsupported.of('dropping sharding; ' + FIXED_SHARDS)
			StorageConfig: throw Unsupported.of('dropping storage settings; the store type of an index is fixed while it is open')
			AuthConfig: throw Unsupported.of('dropping authentication; the security plugin is configured on the server (plugins.security.disabled)')
			RoleBinding: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/_plugins/_security/api/roles/' + roleName(concept.roleName)), 'ignore', ignore)) as Stmt]
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/_plugins/_sm/policies/' + aliasName(concept.name)), 'ignore', ignore)) as Stmt]
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: {
				val collection = EcoreUtil2.getContainerOfType(concept, VectorCollection)
				if (collection === null || concept.type.literal != 'max_fields') throw Unsupported.of('collection limits (' + concept.type.literal + '); OpenSearch limits the fields of a mapping only')
				#[new Code.Run(request('PUT', '/' + schema.index(collection).name + '/_settings', null, obj('index.mapping.total_fields.limit', new Code.Nil))) as Stmt]
			}
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** Dropping a tenant, namespace or partition deletes its documents and its filtered alias. */
	def private List<Stmt> dropSlice(VectorCollection collection, String kind, String value) {
		val target = schema.index(collection)
		val fieldName = switch kind { case 'tenant': target.tenant case 'namespace': target.namespace default: target.partition }
		if (fieldName === null) throw Unsupported.of('a ' + kind + ' of a collection without ' + kind + 's')
		#[new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('remove', obj('index', text(target.name), 'alias', text(sliceAlias(target.name, kind, value)),
				'must_exist', new Code.Flag(false))))))),
			new Code.Return(byQuery(target, '_delete_by_query', OpenSearchSchema.term(fieldName, text(value)), null, 'deleted', 'delete_count', null))]
	}

	/** An alias moves between indices in one atomic _aliases request. */
	def dispatch List<Stmt> body(SwapAlias it) {
		val aliasValue = aliasName(alias.name)
		#[new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(
			obj('remove', obj('index', text(schema.index(oldCollection).name), 'alias', text(aliasValue), 'must_exist', new Code.Flag(false))),
			obj('add', obj('index', text(schema.index(newCollection).name), 'alias', text(aliasValue)))))))]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of(NO_TTL)
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		#[new Code.Return(writeCall(collection, source, if (written(it, 'onConflict')) onConflict.literal else 'error', null, it))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of(NO_TTL)
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		#[new Code.Return(writeCall(collection, source, 'replace', if (written(it, 'batchSize')) batchSize else null, it))]
	}

	/** One _bulk write of the records with the slice fields of the statement and the defaults of the index. */
	def private Code.Call writeCall(VectorCollection collection, RecordSource recordSource, String policy, Integer batch, EObject statement) {
		val target = schema.index(collection)
		val mode = writeMode(policy)
		call('write', 'index', text(target.name), 'records', recordsOf(recordSource, target), 'key', text(target.key), 'op', text(mode.get('op')),
			'counter', text(mode.get('counter')), 'extra', sliceFields(statement, collection, target), 'defaults', defaultsOf(target),
			'batch_size', if (batch === null) null else num(batch), 'ignore_conflicts', if (mode.containsKey('ignore')) new Code.Flag(true) else null,
			'vectors', vectorsOf(target))
	}

	/** The float vector fields a write sends with a decimal point (OpenSearchIndex.floatVectorFields); null without any. */
	def private Code.Items vectorsOf(OpenSearchIndex target) {
		val names = target.floatVectorFields
		if (names.empty) null else arr(names.map[name | text(name)])
	}

	def private Entries defaultsOf(OpenSearchIndex target) {
		val Map<String, Expr> values = target.defaults
		if (values.empty) null else Code.entries(values)
	}

	/** The slice fields of a write: tenant, namespace and partition; partitioned collections default to the default partition. */
	def private Entries sliceFields(EObject statement, VectorCollection collection, OpenSearchIndex target) {
		val Entries result = Code.entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.put(target.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result.put(target.namespace, text(namespace as String))
		val partition = feature(statement, 'partition') as Partition
		if (partition !== null && target.partition === null) throw Unsupported.of('a partition of a collection declared without partitions')
		if (target.partition !== null) result.put(target.partition, text(partition?.name ?: collection.defaultPartition?.name ?: DEFAULT_PARTITION))
		if (result.entries.empty) null else result
	}

	def dispatch List<Stmt> body(Update it) {
		writeOptions(it)
		val target = schema.index(collection)
		if (source !== null) {
			if (filter !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty || written(it, 'ifVersion')
				|| (written(it, 'mode') && mode == EUpdateMode.REPLACE) || tenant !== null || namespace !== null || partition !== null) {
				throw Unsupported.of('update from records with a filter, a slice, field clauses, if-version or mode replace; bulk update operations address documents by id, and each record carries the values it merges')
			}
			return #[new Code.Return(call('write', 'index', text(target.name), 'records', recordsOf(source, target), 'key', text(target.key), 'op', text('patch'),
				'counter', text('update_count'), 'only', Code.items(ids.map[i | Literals.of(i)].toList), 'vectors', vectorsOf(target)))]
		}
		val Entries assignments = Code.entries
		val Entries appended = Code.entries
		val Entries pulled = Code.entries
		val List<Expr> removed = newArrayList
		val List<String> given = newArrayList
		if (values !== null) {
			for (entry : values.entries) {
				if (entry.field === collection.primaryKey) throw Unsupported.of('updating the primary key; the key is the document id')
				val mapped = target.field(ModelUtil.fieldName(entry.field))
				given += mapped.name
				val converted = convert(entry.value, mapped)
				if (appendFields.contains(entry.field)) appended.put(mapped.name, listOf(converted)) else if (arrayRemoveFields.contains(entry.field)) pulled.put(mapped.name, listOf(converted)) else assignments.put(mapped.name, converted)
			}
		}
		for (arrayField : appendFields + arrayRemoveFields) {
			if (!given.contains(target.field(ModelUtil.fieldName(arrayField)).name)) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(arrayField) + " without items in 'set'")
		}
		for (removedField : removedFields) {
			if (removedField === collection.primaryKey) throw Unsupported.of('removing the primary key')
			removed += text(target.field(ModelUtil.fieldName(removedField)).name)
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			for (mapped : target.fields) {
				if (mapped.name != target.key && mapped.vector === null && !given.contains(mapped.name) && !removed.contains(text(mapped.name))) removed += text(mapped.name)
			}
		}
		if (assignments.entries.empty && appended.entries.empty && pulled.entries.empty && removed.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
		val painless = script(UPDATE_SCRIPT, obj('set', assignments, 'remove', arr(removed), 'append', appended, 'pull', pulled))
		if (written(it, 'ifVersion')) {
			if (ids.size != 1 || filter !== null || tenant !== null || namespace !== null || partition !== null) {
				throw Unsupported.of('if-version on other than one id; optimistic concurrency control (if_seq_no and if_primary_term) guards one document')
			}
			return #[new Code.Return(call('update_if_version', 'index', text(target.name), 'value', Literals.of(ids.head), 'seq_no', num(ifVersion), 'body', obj('script', painless)))]
		}
		#[new Code.Return(byQuery(target, '_update_by_query', boolOf(clauses(it, collection, target, filter, idsOf(ids))), painless, 'updated', 'update_count', null))]
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val target = schema.index(collection)
		val matching = clauses(it, collection, target, filter, idsOf(ids))
		if (ids.empty && filter === null && !deleteAll) throw Unsupported.of("a delete without ids, filter or 'all'")
		#[new Code.Return(byQuery(target, '_delete_by_query', boolOf(matching), null, 'deleted', 'delete_count', if (written(it, 'deleteLimit')) deleteLimit else null))]
	}

	/** The runtime reads a local JSON or JSON Lines file and writes it with _bulk requests of batch-size records. */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; pull-based ingestion (experimental since 3.0) reads Kafka or Kinesis through the ingestion-kafka and ingestion-kinesis plugins, which the image does not include, into an index created with an ingestion_source')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; OpenSearch indexes documents that a client sends, and reads object stores only as snapshot repositories or through the experimental pull-based ingestion of an index created for it')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV records in the runtime)')
			throw Unsupported.of('import format ' + format.literal + '; the _bulk API takes JSON documents')
		}
		schema.note(it, 'parallelism', 'async')
		for (key : ModelUtil.extParams(it, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val target = schema.index(collection)
		val writeModeOf = writeMode(if (written(it, 'onConflict')) onConflict.literal else 'error')
		#[new Code.Return(call('import_file', 'location', text(location), 'index', text(target.name), 'key', text(target.key), 'op', text(writeModeOf.get('op')),
			'counter', text(writeModeOf.get('counter')), 'extra', sliceFields(it, collection, target), 'defaults', defaultsOf(target),
			'batch_size', if (written(it, 'batchSize')) num(batchSize) else null, 'ignore_conflicts', if (writeModeOf.containsKey('ignore')) new Code.Flag(true) else null,
			'vectors', vectorsOf(target)))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of(NO_TRANSACTIONS)
	}

	/** Every write carries refresh=true, so 'wait' needs no call; write consistency is the server's (wait_for_active_shards 1). */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (key : ModelUtil.extParams(statement, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- operational

	/** Load opens the index, waits until it is usable and loads its vector graphs into native memory (k-NN warmup API). */
	def dispatch List<Stmt> body(Load it) {
		operationExtensions(it)
		if (!partitions.empty) throw Unsupported.of('loading partitions; OpenSearch opens and closes whole indices')
		val target = schema.index(collection)
		#[new Code.Run(request('POST', '/' + target.name + '/_open', null, null)),
			new Code.Let('health', call('wait_health', 'index', text(target.name), 'status', text('yellow'), 'timeout_seconds', num(60))),
			new Code.Run(request('GET', '/_plugins/_knn/warmup/' + target.name, null, null)),
			new Code.Return(new Code.Ref('health'))]
	}

	def dispatch List<Stmt> body(Release it) {
		operationExtensions(it)
		if (!partitions.empty) throw Unsupported.of('releasing partitions; OpenSearch opens and closes whole indices')
		#[new Code.Return(call('request', 'method', text('POST'), 'path', text('/' + schema.index(collection).name + '/_close'), 'select', arr(text('acknowledged'))))]
	}

	def dispatch List<Stmt> body(Flush it) {
		operationExtensions(it)
		#[new Code.Run(request('POST', '/' + schema.index(collection).name + '/_flush', null, null))]
	}

	def dispatch List<Stmt> body(Compact it) {
		operationExtensions(it)
		val Entries forceMergeParams = obj('max_num_segments', if (written(it, 'targetSegments')) num(targetSegments) else null,
			'wait_for_completion', if (async) text('false') else null)
		// Deleted documents first: the faiss graph keeps them until their segment is merged away, and they take k slots in a
		// kNN query before being filtered out. A force merge to one segment skips an index that is one segment already.
		#[new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', obj('only_expunge_deletes', text('true'), 'wait_for_completion', if (async) text('false') else null), null)),
			new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', forceMergeParams, null))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val target = schema.index(collection)
		val info = call('request', 'method', text('GET'), 'path', text('/' + target.name), 'select', arr(text(target.name)))
		if (!includeStats && !includeIndexProgress) return #[new Code.Return(info)]
		val List<Expr> parts = newArrayList
		parts += info
		if (includeStats) parts += call('request', 'method', text('GET'), 'path', text('/' + target.name + '/_stats'), 'select', arr(text('_all'), text('primaries')))
		if (includeIndexProgress) parts += call('request', 'method', text('GET'), 'path', text('/_cluster/health/' + target.name), 'select', arr(text('status')))
		#[new Code.Return(Code.items(parts))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + NO_MODELS)
	}

	/** A snapshot of the index into the fs repository (path.repo); OpenSearch snapshots are incremental by design. */
	def dispatch List<Stmt> body(Snapshot it) {
		operationExtensions(it)
		if (tenant !== null) throw Unsupported.of('tenant snapshots; a snapshot holds whole indices')
		if (written(it, 'scope') && scope.literal != 'collection' && scope.literal != 'full') throw Unsupported.of('snapshots of a ' + scope.literal + '; a snapshot holds whole indices or the cluster')
		schema.note(it, 'incremental')
		val repo = repository(location)
		val indices = if (written(it, 'scope') && scope.literal == 'full') text('*') else text(schema.index(collection).name)
		val snapshotBody = obj('indices', indices, 'include_global_state', new Code.Flag(false),
			'metadata', if (description === null) null else obj('description', text(description)))
		#[registerRepository(repo), new Code.Return(call('request', 'method', text('PUT'), 'path', text('/_snapshot/' + repo + '/' + snapshotOf(it)),
			'params', obj('wait_for_completion', text('true')), 'body', snapshotBody, 'select', arr(text('snapshot'), text('state'))))]
	}

	def private String snapshotOf(Snapshot snapshot) {
		aliasName(snapshot.snapshotName ?: schema.index(snapshot.collection).name + '-snapshot')
	}

	/** Restore closes the index (a restore replaces a closed index) and reopens it from the snapshot. */
	def dispatch List<Stmt> body(Restore it) {
		operationExtensions(it)
		if (written(it, 'scope') && scope.literal != 'collection') throw Unsupported.of('restoring a ' + scope.literal + '; the generator restores the index of the collection')
		val repo = repository(location)
		val target = schema.index(collection)
		#[new Code.Run(call('request', 'method', text('POST'), 'path', text('/' + target.name + '/_close'), 'ignore', arr(num(404)))),
			new Code.Return(call('request', 'method', text('POST'), 'path', text('/_snapshot/' + repo + '/' + aliasName(snapshotName) + '/_restore'),
				'params', obj('wait_for_completion', text(if (async) 'false' else 'true')), 'body', obj('indices', text(target.name), 'include_global_state', new Code.Flag(false)),
				'select', if (async) arr(text('accepted')) else arr(text('snapshot'), text('indices'))))]
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		#[new Code.Run(request('DELETE', '/_snapshot/' + repository(location) + '/' + aliasName(snapshotName), null, null))]
	}

	/** _clone copies a write-blocked index (hard-linked segments, same mapping and settings); both blocks are lifted after. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; ' + ONE_CLUSTER)
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		operationExtensions(it)
		schema.note(it, 'mode')
		val original = schema.index(sourceCollection).name
		val copy = indexName(schema.index(sourceCollection).database, targetName)
		#[new Code.Run(request('PUT', '/' + original + '/_settings', null, obj('index.blocks.write', new Code.Flag(true)))),
			new Code.Run(request('POST', '/' + original + '/_clone/' + copy, null, null)),
			new Code.Run(request('PUT', '/' + original + '/_settings', null, obj('index.blocks.write', new Code.Nil))),
			new Code.Run(request('PUT', '/' + copy + '/_settings', null, obj('index.blocks.write', new Code.Nil))),
			new Code.Return(call('request', 'method', text('POST'), 'path', text('/' + copy + '/_count'), 'select', arr(text('count')), 'wrap', text('copied')))]
	}

	/** OpenSearch rebuilds HNSW graphs when it merges segments: a force merge into one segment rebuilds the index's graph. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		operationExtensions(it)
		schema.note(it, 'concurrently')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		// Deleted documents first: the faiss graph keeps them until their segment is merged away, and they take k slots in a
		// kNN query before being filtered out. A force merge to one segment skips an index that is one segment already.
		#[new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', obj('only_expunge_deletes', text('true')), null)),
			new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', obj('max_num_segments', num(1)), null))]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val target = schema.index(collection)
		if (namespace !== null) {
			if (target.namespace === null) throw Unsupported.of('a namespace of a collection without namespaces')
			return #[new Code.Return(byQuery(target, '_delete_by_query', OpenSearchSchema.term(target.namespace, text(namespace)), null, 'deleted', 'delete_count', null))]
		}
		#[new Code.Return(byQuery(target, '_delete_by_query', obj('match_all', Code.entries), null, 'deleted', 'delete_count', null))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = scope
		val limit = if (written(it, 'listLimit')) num(listLimit) else null
		val prefixValue = if (prefix === null) null else text(prefix)
		val listing = switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) {
					val database = (owner as Database).name
					val pattern = indexName(database, 'x').replaceFirst('x$', '')
					call('names', 'kind', text('indices'), 'path', text('/_cat/indices/' + pattern + '*'), 'strip', text(pattern), 'prefix', prefixValue, 'limit', limit)
				} else {
					if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; OpenSearch lists the indices of a cluster')
					call('names', 'kind', text('indices'), 'path', text('/_cat/indices'), 'prefix', prefixValue, 'limit', limit)
				}
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				call('names', 'kind', text('databases'), 'path', text('/_cat/indices'), 'prefix', prefixValue, 'limit', limit)
			}
			case INDEXES: call('names', 'kind', text('vector_fields'), 'path', text('/' + scopeIndex(owner, 'indexes').name + '/_mapping'), 'prefix', prefixValue, 'limit', limit)
			case ALIASES: {
				val path = if (owner === null) '/_alias' else '/' + scopeIndex(owner, 'aliases').name + '/_alias'
				call('names', 'kind', text('aliases'), 'path', text(path), 'prefix', prefixValue, 'limit', limit)
			}
			case TENANTS,
			case NAMESPACES,
			case PARTITIONS: {
				val target = scopeIndex(owner, kind.literal)
				val fieldName = switch kind { case TENANTS: target.tenant case NAMESPACES: target.namespace default: target.partition }
				if (fieldName === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				call('names', 'kind', text('terms'), 'path', text('/' + target.name + '/_search'),
					'body', obj('size', num(0), 'aggs', obj('names', obj('terms', obj('field', text(fieldName), 'size', num(MAX_CANDIDATES))))), 'prefix', prefixValue, 'limit', limit)
			}
			case SNAPSHOTS: call('names', 'kind', text('snapshots'), 'path', text('/_snapshot/vecdsl/_all'), 'prefix', prefixValue, 'limit', limit, 'ignore', arr(num(404)))
		}
		#[new Code.Return(listing)]
	}

	def private OpenSearchIndex scopeIndex(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.index(owner as VectorCollection)
	}

	/** OpenSearch builds vector indexes with each segment, so awaiting one waits for the index's health. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		#[new Code.Return(call('wait_health', 'index', text(schema.index(collection).name), 'status', text('yellow'),
			'timeout_seconds', num(if (written(it, 'timeoutSeconds')) timeoutSeconds else 60)))]
	}

	def private VectorCollection indexCollection(Index declaredIndex) {
		switch declaredIndex {
			VectorIndex: {
				val owner = declaredIndex.field ?: EcoreUtil2.getContainerOfType(declaredIndex, VectorField)
				if (owner === null) null else ModelUtil.owningCollection(owner) ?: ModelUtil.targetCollection(declaredIndex)
			}
			PayloadIndex: ModelUtil.targetCollection(declaredIndex) ?: (if (declaredIndex.target.field === null) null else ModelUtil.owningCollection(declaredIndex.target.field))
		}
	}

	def private void operationExtensions(EObject statement) {
		for (key : ModelUtil.extParams(statement, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		rejectClauses(it, 'elementScope', 'asOf', 'reranker', 'queryModel', 'prefetches')
		queryExtensions(it)
		combinations(it)
		if (!highlighted.empty) throw Unsupported.of("the 'highlight' clause on a vector search; " + NO_QUERY_TERMS + 'a vector search has not')
		val target = schema.index(collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val List<Expr> filters = clauses(it, collection, target, filter, null)
		if (input === null) throw Unsupported.of('a vector query without input')
		if (mapped.vector.sparse) {
			if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause on a sparse field; MMR reranking compares the knn_vector values of the hits")
			if (input.kind != EQueryInputMode.SPARSE_VECTOR) throw Unsupported.of(input.kind.literal + ' input on sparse field ' + mapped.name)
			schema.note(it, 'exact', 'metric')
			val Entries sparseBody = shape(it, collection, target, obj('query', boostQuery(target, obj('bool', obj('must', arr(obj('neural_sparse', obj(mapped.name, obj('query_tokens', convert(input.value, mapped))))),
				'filter', if (filters.empty) null else arr(filters))), boost)), output, excludedOf(it), withVectors)
			sparseBody.put('size', num(topK))
			if (offsetOf(it) > 0) sparseBody.put('from', num(offsetOf(it)))
			return #[new Code.Return(paged(searchCall(target, cursorOrder(it, target, sparseBody), relevance(), bounds, groupOf(it, target)), it, target))]
		}
		val searchMetric = if (written(it, 'metric')) metric.literal else mapped.vector.metric
		val exhaustive = exact || mapped.vector.exact || searchMetric != mapped.vector.metric
			|| (params !== null && (params.exhaustive || (written(params, 'filterStrategy') && params.filterStrategy.literal == 'pre_filter')))
		val queryVector = queryVectorOf(input, mapped)
		val searchSpace = OpenSearchBodies.exactSpace(searchMetric, mapped.vector.element)
		val Entries requestBody = if (exhaustive) {
			if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause with an exact search; MMR reranking oversamples the k of a knn query")
			if (params !== null) schema.note(params, 'candidatePool', 'filterStrategy', 'oversampling', 'rescore')
			obj('query', boostQuery(target, obj('script_score', obj('query', boolOf(filters), 'script', exactScript(mapped.name, queryVector, searchSpace))), boost), 'size', num(topK),
				'from', if (offsetOf(it) > 0) num(offsetOf(it)) else null)
		} else {
			if (mmr !== null && mmr.vectorField !== null && mmr.vectorField !== searched) throw Unsupported.of('mmr on another vector field than the searched one')
			knnBody(it, target, mapped, queryVector, filters)
		}
		val postFiltered = !exhaustive && params !== null && written(params, 'filterStrategy') && params.filterStrategy.literal == 'post_filter' && !filters.empty
		if (postFiltered) {
			knnLeaf(requestBody.get('query') as Entries).put('filter', obj('match_all', Code.entries))
			requestBody.put('post_filter', boolOf(filters))
		}
		if (params !== null && written(params, 'timeoutMillis')) requestBody.put('timeout', text(params.timeoutMillis + 'ms'))
		val formKind = if (exhaustive) scoreForm(OpenSearchBodies.exactForm(searchSpace, mapped.vector.element), false) else scoreForm(mapped.vector.space, false)
		val form = if (boost !== null) relevance() else if (nativeScore) scoreForm(mapped.vector.space, true) else formKind
		val shaped = cursorOrder(it, target, shape(it, collection, target, requestBody, output, excludedOf(it), withVectors))
		val search = if (input.kind == EQueryInputMode.ID) byStoredVector(it, input, collection, mapped, shaped, form) else searchCall(target, shaped, form, bounds, groupOf(it, target))
		val k = globalK(it, exhaustive, postFiltered)
		if (k !== null) search.args().put('top_k', num(k))
		#[new Code.Return(paged(search, it, target))]
	}

	/**
	 * The k of the runtime's global top k (top_k), or null. A knn query takes k per segment, and a total, aggregations, collapse
	 * groups, a post filter, a sort and boost functions see every segment's hits, so their answer depended on how documents fell
	 * into segments. The runtime first finds the global top k, then runs the request restricted to those documents (the meaning
	 * these clauses have in Elasticsearch, where k is global). A cursor page's k is CURSOR_WINDOW.
	 */
	def private Integer globalK(EObject statement, boolean exhaustive, boolean postFiltered) {
		if (exhaustive || written(statement, 'mmr')) return null
		val sorted = !((feature(statement, 'sortClauses') ?: newArrayList) as List<SortClause>).empty
		if (!written(statement, 'withTotal') && !written(statement, 'aggregations') && !written(statement, 'groupBy') && !postFiltered && !sorted && !written(statement, 'boost')) return null
		if (written(statement, 'cursor')) CURSOR_WINDOW else (feature(statement, 'topK') as Integer) + offsetOf(statement)
	}

	/**
	 * The request of a dense knn search: a knn query, in a function_score query with a boost, and the MMR reranker of the k-NN
	 * plugin (ext.mmr) for mmr. A cursor page reads the CURSOR_WINDOW nearest records.
	 */
	def private Entries knnBody(EObject statement, OpenSearchIndex target, OpenSearchIndex.Field mapped, Expr queryVector, List<Expr> filters) {
		val size = feature(statement, 'topK') as Integer
		val mmrConfig = feature(statement, 'mmr') as MmrConfig
		val block = feature(statement, 'boost') as BoostBlock
		if (mmrConfig !== null && block !== null) throw Unsupported.of("the 'boost' clause with mmr; MMR reranking supports only a knn or neural query as the top-level query of a search")
		val k = if (written(statement, 'cursor')) CURSOR_WINDOW else size + offsetOf(statement)
		val window = if (mmrConfig === null) k else if (written(mmrConfig, 'candidates')) Math.max(mmrConfig.candidates, k) else 3 * k
		val Entries searchBody = obj('query', boostQuery(target, knnQuery(statement, target, mapped, queryVector, window, filters), block), 'size', num(size),
			'from', if (offsetOf(statement) > 0) num(offsetOf(statement)) else null)
		if (mmrConfig !== null) {
			val diversity = java.math.BigDecimal.ONE.subtract(java.math.BigDecimal.valueOf(mmrConfig.diversityLambda)).doubleValue
			searchBody.put('ext', obj('mmr', obj('diversity', num(diversity), 'candidates', num(window), 'vector_field_path', text(mapped.name))))
		}
		searchBody
	}

	/** Clause combinations the generated requests do not express. */
	def private void combinations(EObject statement) {
		if (written(statement, 'cursor')) {
			for (clause : #['offset', 'groupBy', 'mmr', 'sortClauses']) {
				if (written(statement, clause)) throw Unsupported.of("the 'cursor' clause with " + (CLAUSES.get(clause) ?: clause) + '; a cursor page continues after its record in the order of the search')
			}
		}
	}

	/** A cursor page sorts by score, then by key, so that search_after continues after the cursor record. */
	def private Entries cursorOrder(EObject statement, OpenSearchIndex target, Entries requestBody) {
		if (written(statement, 'cursor')) requestBody.put('sort', arr(obj('_score', obj('order', text('desc'))), obj(target.keyField.exact, obj('order', text('asc')))))
		requestBody
	}

	/** The cursor of a search, for the runtime's search_after (the key of the last hit of the previous page). */
	def private Code.Call paged(Code.Call search, EObject statement, OpenSearchIndex target) {
		if (written(statement, 'cursor')) search.args().put('after', cursorValue(target.keyField, feature(statement, 'cursor') as String))
		search
	}

	/** The field object of a knn query ({"knn": {field: {...}}}), also inside the function_score query of a boost. */
	def private Entries knnLeaf(Entries query) {
		val inner = if (query.has('function_score')) (query.get('function_score') as Entries).get('query') as Entries else query
		val fields = inner.get('knn') as Entries
		fields.entries.values.head as Entries
	}

	/**
	 * A search by the stored vector of a document: the runtime reads the vector and puts it into the knn query (also inside a
	 * function_score query) or the params of the exact search's script before it sends the search.
	 */
	def private Code.Call byStoredVector(EObject statement, QueryInput queryInput, VectorCollection collection, OpenSearchIndex.Field mapped, Entries searchBody, Entries form) {
		val lookup = schema.index(queryInput.lookupCollection ?: collection)
		call('recommend', 'index', text(schema.index(collection).name), 'lookup', text(lookup.name), 'field', text(mapped.name), 'key', text(schema.index(collection).key),
			'body', searchBody, 'score', form, 'positive_ids', arr(Literals.of(queryInput.value)), 'bounds', boundsOf(feature(statement, 'bounds') as ScoreBounds),
			'group', if (groupOf(statement, schema.index(collection)) === null) null else text(groupOf(statement, schema.index(collection))))
	}

	/**
	 * The knn query of a field: the vector, k, the filter (efficient filtering of the Faiss and Lucene engines), ef_search from
	 * the search parameters (else the index's own), and the rescoring of a quantized field.
	 */
	def private Entries knnQuery(EObject statement, OpenSearchIndex target, OpenSearchIndex.Field mapped, Expr queryVector, int k, List<Expr> filters) {
		val searchParams = feature(statement, 'params') as SearchParams
		var Integer pool = null
		var Expr rescore = if (mapped.vector.oversample === null) null else obj('oversample_factor', num(mapped.vector.oversample))
		if (searchParams !== null) {
			if (written(searchParams, 'targetRecall')) throw Unsupported.of('target-recall: the knn query takes k, ef_search and an oversample factor, effort settings with no declarable recall target')
			if (written(searchParams, 'probes')) throw Unsupported.of('probes; nprobes belongs to the Faiss ivf method, which needs a model trained with the Train API')
			if (written(searchParams, 'scanBudget')) throw Unsupported.of('scan-budget; an hnsw search takes ef_search and no bound on visited vectors')
			for (key : ModelUtil.extParams(searchParams, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
			if (written(searchParams, 'candidatePool')) pool = searchParams.candidatePool
			if (written(searchParams, 'oversampling')) rescore = obj('oversample_factor', num(searchParams.oversampling)) else if (searchParams.rescore && rescore === null) rescore = new Code.Flag(true)
		}
		// Always a filter, match_all when the statement has none: a Faiss graph keeps deleted and updated documents until a merge
		// purges them, which soft-delete retention leases delay past a force merge. Without a filter they take k slots and are
		// dropped afterwards (on 3.9.0, with 6 live and 17 deleted documents, k 2 returns one hit); with a filter the graph
		// search runs on the live documents' bitset and returns the k nearest live documents.
		obj('knn', obj(mapped.name, obj('vector', queryVector, 'k', num(k), 'filter', if (filters.empty) obj('match_all', Code.entries) else boolOf(filters),
			'method_parameters', if (pool === null) null else obj('ef_search', num(candidates(pool, k))), 'rescore', rescore)))
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		rejectClauses(it, 'asOf', 'reranker')
		queryExtensions(it)
		combinations(it)
		if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause on a keyword search; MMR reranking supports only a knn or neural query as the top-level query of a search")
		if (params !== null) schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		val target = schema.index(collection)
		val textQuery = keywordQuery(target, searchFields, input, if (written(it, 'textQueryType')) textQueryType.literal else 'match',
			if (written(it, 'matchOperator')) matchOperator.literal else null, if (written(it, 'minShouldMatch')) minShouldMatch else null,
			fuzziness, if (written(it, 'slop')) slop else null)
		val filters = clauses(it, collection, target, filter, null)
		val scored = boostQuery(target, obj('bool', obj('must', arr(textQuery), 'filter', if (filters.empty) null else arr(filters))), boost)
		val requestBody = obj('query', scored, 'size', num(topK), 'from', if (offsetOf(it) > 0) num(offsetOf(it)) else null)
		if (params !== null && written(params, 'timeoutMillis')) requestBody.put('timeout', text(params.timeoutMillis + 'ms'))
		val shaped = cursorOrder(it, target, shape(it, collection, target, requestBody, output, excludedOf(it), withVectors))
		#[new Code.Return(paged(searchCall(target, shaped, relevance(), bounds, groupOf(it, target)), it, target))]
	}

	/**
	 * A text query: match (or multi_match over several fields, most_fields so that the field scores add up), match_phrase with
	 * slop, match_phrase_prefix, query_string, and the term-level prefix, term, fuzzy, wildcard and regexp queries on the
	 * analyzed terms of each field, with boosts per field.
	 */
	def private Entries keywordQuery(OpenSearchIndex target, List<SearchFieldRef> refs, QueryInput queryInput, String type, String operator,
		Integer minimum, String fuzzinessValue, Integer slopValue) {
		val words = keywordText(queryInput)
		val List<String> fields = newArrayList
		if (refs.empty) {
			for (mapped : target.fields) if (mapped.fullText) fields += mapped.name
		} else {
			for (ref : refs) {
				val mapped = target.field(ModelUtil.fieldName(ref.field))
				if (mapped.vector !== null) throw Unsupported.of('keyword search on vector field ' + mapped.name)
				fields += mapped.name + (if (written(ref, 'boost')) '^' + OpenSearchFilter.number(ref.boost) else '')
			}
		}
		if (fields.empty) throw Unsupported.of('keyword search in a collection without full-text fields')
		if (slopValue !== null && type != 'phrase') throw Unsupported.of("the 'slop' clause with text-query " + type + '; slop belongs to phrase queries')
		if (fuzzinessValue !== null && type != 'match') throw Unsupported.of("the 'fuzziness' clause with text-query " + type + '; fuzziness belongs to match queries (text-query fuzzy has its own)')
		if (minimum !== null && type != 'match') throw Unsupported.of("the 'min-should-match' clause with text-query " + type)
		val Entries options = obj('operator', if (operator === null) null else text(operator), 'minimum_should_match', if (minimum === null) null else num(minimum),
			'fuzziness', if (fuzzinessValue === null) null else text(fuzzinessValue), 'slop', if (slopValue === null) null else num(slopValue))
		switch type {
			case 'match',
			case 'phrase',
			case 'phrase_prefix': {
				val multiType = switch type { case 'match': 'most_fields' case 'phrase': 'phrase' default: 'phrase_prefix' }
				if (fields.size == 1 && !fields.head.contains('^')) {
					val single = switch type { case 'match': 'match' case 'phrase': 'match_phrase' default: 'match_phrase_prefix' }
					val Entries perField = obj('query', words)
					perField.entries.putAll(options.entries)
					return obj(single, obj(fields.head, perField))
				}
				val Entries multi = obj('query', words, 'fields', arr(fields.map[f | text(f) as Expr].toList), 'type', text(multiType))
				multi.entries.putAll(options.entries)
				obj('multi_match', multi)
			}
			case 'query_string': {
				if (operator !== null) options.entries.put('default_operator', options.entries.remove('operator'))
				val Entries queryString = obj('query', words, 'fields', arr(fields.map[f | text(f) as Expr].toList))
				queryString.entries.putAll(options.entries)
				obj('query_string', queryString)
			}
			default: {
				if (operator !== null) throw Unsupported.of("the 'operator' clause with text-query " + type + '; it applies to analyzed queries')
				val List<Expr> perField = newArrayList
				for (f : fields) {
					val parts = f.split('\\^')
					val mapped = target.field(parts.get(0))
					val leaf = obj('value', words, 'boost', if (parts.length > 1) num(Double.parseDouble(parts.get(1))) else null, 'fuzziness', if (type == 'fuzzy') text('AUTO') else null)
					perField += obj(type, obj(mapped.name, leaf))
				}
				if (perField.size == 1) perField.head as Entries else obj('bool', obj('should', arr(perField), 'minimum_should_match', num(1)))
			}
		}
	}

	/**
	 * Boost functions as a function_score query: field_value_factor, the gauss, exp and linear decay functions and weights of
	 * filters; combine is score_mode and apply is boost_mode.
	 */
	def private Entries boostQuery(OpenSearchIndex target, Entries inner, BoostBlock block) {
		if (block === null) return inner
		val List<Expr> functions = newArrayList
		for (boostFunction : block.functions) {
			val weight = if (written(boostFunction, 'weight')) num(boostFunction.weight) else null
			val applies = if (boostFunction.condition === null) null else new OpenSearchFilter(target).query(boostFunction.condition)
			val path = if (boostFunction.target === null) null else fieldPath(target, boostFunction.target)
			switch boostFunction.kind.literal {
				case 'field_value': {
					if (path === null) throw Unsupported.of('a field_value boost without a field')
					functions += obj('field_value_factor', obj('field', text(path), 'missing', num(0)), 'weight', weight, 'filter', applies)
				}
				case 'decay': {
					if (path === null || boostFunction.origin === null) throw Unsupported.of('a decay boost without field, origin and scale')
					val decay = obj('origin', Literals.of(boostFunction.origin), 'scale', Literals.of(boostFunction.scale),
						'offset', if (boostFunction.decayOffset === null) null else Literals.of(boostFunction.decayOffset),
						'decay', if (written(boostFunction, 'decayRate')) num(boostFunction.decayRate) else null)
					functions += obj(boostFunction.shape.literal, obj(path, decay), 'weight', weight, 'filter', applies)
				}
				default: {
					if (applies === null) throw Unsupported.of('a condition boost without a condition')
					functions += obj('filter', applies, 'weight', weight ?: num(1))
				}
			}
		}
		val scoreMode = if (written(block, 'combine')) block.combine.literal else 'multiply'
		val boostMode = if (written(block, 'apply')) block.apply.literal else 'multiply'
		obj('function_score', obj('query', inner, 'functions', arr(functions), 'score_mode', text(if (scoreMode == 'replace') 'first' else scoreMode),
			'boost_mode', text(boostMode)))
	}

	def private String fieldPath(OpenSearchIndex target, FieldPath path) {
		if (path.rawPath !== null) return OpenSearchBodies.field(path.rawPath)
		val List<String> segments = newArrayList
		segments += target.field(ModelUtil.fieldName(path.field)).name
		segments += path.subFields.map[s | ModelUtil.fieldName(s)]
		segments.join('.')
	}

	/**
	 * Hybrid search. rrf is the server's: one hybrid query whose subqueries are the legs (a knn query, or a text query with the
	 * shared and the leg's filters), fused by the score-ranker-processor of a temporary search pipeline, which sums 1/(k + rank)
	 * as vecDSL defines it; the runtime orders equal fused scores by key. weighted and relative_score are fused in the runtime from
	 * one search per leg, because the normalization-processor's min_max floors normalized scores at 0.001, divides by the sum of
	 * the weights of the legs that found a hit and normalizes the server's transformed scores, not the canonical similarities; rrf with
	 * legs of different limits is fused in the runtime too (pagination_depth bounds every subquery alike). The result clauses
	 * (cursor, with-total, sort, aggregate, boost, mmr, group-by) go to the shared result-shape module: the fused records of the
	 * candidate window are its candidates.
	 */
	def dispatch List<Stmt> body(HybridSearch it) {
		rejectClauses(it, 'asOf', 'reranker')
		if (!highlighted.empty && !legs.exists[leg | leg instanceof KeywordLeg]) {
			throw Unsupported.of("the 'highlight' clause on a hybrid search without a keyword leg; " + NO_QUERY_TERMS + 'its vector legs have not')
		}
		queryExtensions(it)
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val target = schema.index(collection)
		val shapes = ShapeSpec.of(it)
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val defaultLimit = topK + offsetOf(it)
		val List<Expr> parts = newArrayList
		val List<Expr> subqueries = newArrayList
		val Set<Integer> limits = newHashSet
		var position = 0
		for (leg : legs) {
			val legLimit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else defaultLimit
			limits += legLimit
			val List<Expr> legFilters = newArrayList
			legFilters += clauses(it, collection, target, filter, null)
			val legFilter = feature(leg, 'filter') as FilterBlock
			if (legFilter !== null) legFilters += new OpenSearchFilter(target).query(legFilter.condition)
			for (key : ModelUtil.extParams(leg, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a hybrid leg')
			val Entries legQuery = switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (a score condition on the leg in the runtime)')
					val legField = vectorFieldOf(collection, leg.vectorField)
					val mapped = target.field(ModelUtil.fieldName(legField))
					if (mapped.vector.sparse) throw Unsupported.of('a sparse vector leg; ' + PENDING + ' (a neural_sparse query as a leg)')
					if (written(leg, 'metric') && leg.metric.literal != mapped.vector.metric || leg.exact || mapped.vector.exact) throw Unsupported.of('an exact or re-metered vector leg; ' + PENDING + ' (a knn_score script query as a leg)')
					if (leg.params !== null) schema.note(leg.params, 'filterStrategy', 'exhaustive')
					val legInput = leg.input ?: input
					if (legInput === null || legInput.kind == EQueryInputMode.ID) throw Unsupported.of('a vector leg without a vector input')
					knnQuery(leg, target, mapped, queryVectorOf(legInput, mapped), legLimit, legFilters)
				}
				KeywordLeg: {
					val textQuery = keywordQuery(target, leg.searchFields, leg.input ?: input, if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match',
						if (written(leg, 'matchOperator')) leg.matchOperator.literal else null, if (written(leg, 'minShouldMatch')) leg.minShouldMatch else null, null, null)
					obj('bool', obj('must', arr(textQuery), 'filter', if (legFilters.empty) null else arr(legFilters)))
				}
			}
			subqueries += legQuery
			val legForm = switch leg {
				VectorLeg: scoreForm(target.field(ModelUtil.fieldName(vectorFieldOf(collection, leg.vectorField))).vector.space, false)
				default: relevance()
			}
			val legBody = obj('query', legQuery, 'size', num(legLimit))
			parts += obj('body', if (shapes.plain()) shape(it, collection, target, legBody, output, excludedOf(it), withVectors) else candidateBody(it, target, legBody, shapes),
				'score', legForm, 'weight', num(weights.get(position)))
			position = position + 1
		}
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		if (fusionKind == 'rrf' && limits.size == 1) {
			if (k < 1 || k > 10000) throw Unsupported.of('fusion-k ' + k + '; the rank_constant of the score-ranker-processor lies in [1, 10000]')
			val pipeline = obj('phase_results_processors', arr(obj('score-ranker-processor', obj('combination', obj('technique', text('rrf'), 'rank_constant', num(k))))),
				'response_processors', if (explain) arr(obj('hybrid_score_explanation', Code.entries)) else null)
			val hybridQuery = obj('hybrid', obj('queries', arr(subqueries), 'pagination_depth', num(limits.head.doubleValue)))
			if (shapes.plain()) {
				val hybridBody = shape(it, collection, target, obj('query', hybridQuery, 'size', num(topK), 'from', num(offsetOf(it)), 'search_pipeline', pipeline),
					output, excludedOf(it), withVectors)
				return #[new Code.Return(searchCall(target, hybridBody, obj('kind', text('fused')), bounds, null))]
			}
			val candidateSearch = call('search', 'index', text(target.name), 'body', candidateBody(it, target, obj('query', hybridQuery, 'size', shapes.window(),
				'search_pipeline', pipeline), shapes), 'key', text(target.key), 'score', obj('kind', text('fused')), 'candidates', new Code.Flag(true))
			return #[new Code.Return(shapedCall(candidateSearch, shapes, target))]
		}
		val fused = call('hybrid', 'index', text(target.name), 'legs', arr(parts), 'fusion', text(fusionKind), 'k', num(k),
			'size', if (shapes.plain()) num(topK) else shapes.window(), 'offset', num(if (shapes.plain()) offsetOf(it) else 0), 'key', text(target.key),
			'bounds', if (shapes.plain()) boundsOf(bounds) else null, 'candidates', if (shapes.plain()) null else new Code.Flag(true))
		#[new Code.Return(if (shapes.plain()) fused else shapedCall(fused, shapes, target))]
	}

	/** rt.shape over a candidate window: the module's spec in the index's names and the vector fields the candidates carry. */
	def private Code.Call shapedCall(Code.Call candidateSearch, ShapeSpec shapes, OpenSearchIndex target) {
		call('shape', 'candidates', candidateSearch, 'spec', OpenSearchShapes.spec(shapes, target), 'vectors', Code.texts(OpenSearchShapes.carried(shapes, target)))
	}

	/** Leg weights for weighted and relative-score fusion; rrf is unweighted by definition. */
	def private List<Double> legWeights(HybridSearch search, String fusionKind) {
		val weighted = search.legs.filter[leg | written(leg, 'weight')].size
		if (fusionKind == 'rrf') {
			if (weighted > 0 || written(search, 'alpha')) throw Unsupported.of('leg weights or alpha with rrf fusion, which is unweighted')
			return search.legs.map[1.0].toList
		}
		if (written(search, 'alpha')) {
			if (search.legs.size != 2 || !(search.legs.head instanceof VectorLeg) || weighted > 0) {
				throw Unsupported.of('alpha on other than one vector leg followed by one other leg without weights')
			}
			return #[search.alpha, java.math.BigDecimal.ONE.subtract(java.math.BigDecimal.valueOf(search.alpha)).doubleValue]
		}
		if (weighted == 0) return search.legs.map[1.0].toList
		if (weighted != search.legs.size) throw Unsupported.of('weights on some hybrid legs only')
		search.legs.map[leg | feature(leg, 'weight') as Double].toList
	}

	/** The average-vector strategy over example documents or vectors, computed by the runtime; the examples are not returned. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		rejectClauses(it, 'asOf', 'reranker')
		if (!highlighted.empty) throw Unsupported.of("the 'highlight' clause on a recommendation; " + NO_QUERY_TERMS + 'a recommendation has not')
		queryExtensions(it)
		combinations(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val target = schema.index(collection)
		val lookup = schema.index(lookupCollection ?: collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		if (mapped.vector.sparse) throw Unsupported.of('recommendations on a sparse field; the average-vector strategy averages dense vectors')
		if (mapped.vector.element == 'binary' || mapped.vector.element == 'byte') throw Unsupported.of('recommendations on ' + mapped.vector.element + ' vectors; the average of byte or binary vectors is no vector of the field')
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) if (isVector(example)) positiveVectors += convert(example, mapped) else positiveIds += Literals.of(example)
		for (example : negatives) if (isVector(example)) negativeVectors += convert(example, mapped) else negativeIds += Literals.of(example)
		val List<Expr> filters = clauses(it, collection, target, filter, null)
		if (lookupCollection === null || lookupCollection === collection) {
			val List<Literal> examples = newArrayList
			examples += positives.filter[e | !isVector(e)]
			examples += negatives.filter[e | !isVector(e)]
			if (!examples.empty) filters += obj('bool', obj('must_not', arr(obj('ids', obj('values', idsOf(examples))))))
		}
		if (mmr !== null && mmr.vectorField !== null && mmr.vectorField !== searched) throw Unsupported.of('mmr on another vector field than the searched one')
		val requestBody = cursorOrder(it, target, shape(it, collection, target, knnBody(it, target, mapped, new Code.Nil, filters), output, excludedOf(it), withVectors))
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val group = groupOf(it, target)
		val recommendation = call('recommend', 'index', text(target.name), 'lookup', text(lookup.name), 'field', text(mapped.name), 'key', text(target.key),
			'body', requestBody, 'score', if (boost !== null) relevance() else scoreForm(mapped.vector.space, nativeScore), 'positive_ids', if (positiveIds.empty) null else arr(positiveIds),
			'positive_vectors', if (positiveVectors.empty) null else arr(positiveVectors), 'negative_ids', if (negativeIds.empty) null else arr(negativeIds),
			'negative_vectors', if (negativeVectors.empty) null else arr(negativeVectors), 'bounds', boundsOf(bounds), 'group', if (group === null) null else text(group))
		val k = globalK(it, false, false)
		if (k !== null) recommendation.args().put('top_k', num(k))
		#[new Code.Return(paged(recommendation, it, target))]
	}

	def private Expr listOf(Expr value) {
		if (value instanceof Code.Items) value else arr(value)
	}

	def private boolean isVector(Literal example) {
		example instanceof ListLit || example instanceof SparseLit
			|| (example instanceof ParamRef && #['vector', 'sparse_vector'].contains((example as ParamRef).param.type.literal))
	}

	def dispatch List<Stmt> body(GetById it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		val target = schema.index(collection)
		val List<Expr> slices = clauses(it, collection, target, null, null)
		val List<Expr> fields = newArrayList
		if (output !== null && !output.allFields) {
			for (f : output.fields) if (f !== collection.primaryKey) fields += text(target.field(ModelUtil.fieldName(f)).name)
		}
		val List<Expr> hidden = newArrayList
		if (!withVectors) for (v : target.vectorFields) if (!fields.contains(text(v))) hidden += text(v)
		#[new Code.Return(call('fetch', 'index', text(target.name), 'values', Code.items(ids.map[i | Literals.of(i)].toList), 'key', text(target.key),
			'fields', if (output === null || output.allFields) null else arr(fields), 'excludes', if (hidden.empty) null else arr(hidden),
			'filters', if (slices.empty) null else arr(slices)))]
	}

	def dispatch List<Stmt> body(Count it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		schema.note(it, 'exact')
		val target = schema.index(collection)
		#[new Code.Return(call('request', 'method', text('POST'), 'path', text('/' + target.name + '/_count'),
			'body', obj('query', boolOf(clauses(it, collection, target, filter, null))), 'select', arr(text('count'))))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		val target = schema.index(collection)
		if (written(it, 'sliceTotal') && (sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal)) throw Unsupported.of('slice ' + sliceIndex + ' of ' + sliceTotal + '; a slice number is at least 0 and below the slice count')
		val List<Expr> ordering = newArrayList
		for (clause : sortClauses) ordering += sortEntry(target, clause, false)
		val keyField = target.keyField
		ordering += obj(keyField.exact, obj('order', text('asc')))
		val requestBody = obj('query', boolOf(clauses(it, collection, target, filter, null)), 'sort', arr(ordering),
			'size', num(if (written(it, 'scrollLimit')) scrollLimit else 10), 'from', if (offsetOf(it) > 0) num(offsetOf(it)) else null)
		if (cursor !== null) {
			if (!sortClauses.empty || offsetOf(it) > 0) throw Unsupported.of("the 'cursor' clause with sort or offset; search_after continues after the key of the last page")
			requestBody.put('search_after', arr(cursorValue(keyField, cursor)))
		}
		if (written(it, 'sliceTotal') && sliceTotal > 1) requestBody.put('slice', obj('id', num(sliceIndex), 'max', num(sliceTotal)))
		#[new Code.Return(searchCall(target, shape(it, collection, target, requestBody, output, newArrayList, withVectors), null, null, null))]
	}

	def private Expr cursorValue(OpenSearchIndex.Field keyField, String cursorText) {
		val mappedType = (keyField.mapping.get('type') as Code.Text).value
		if (#['integer', 'long', 'byte'].contains(mappedType)) {
			try {
				return num(Long.parseLong(cursorText))
			} catch (NumberFormatException e) {
				throw Unsupported.of('the cursor ' + cursorText + ' on an integer key')
			}
		}
		text(cursorText)
	}

	def private Entries sortEntry(OpenSearchIndex target, SortClause clause, boolean scored) {
		val order = text(if (clause.direction == ESortDirection.DESC) 'desc' else 'asc')
		if (clause.byRelevance) {
			if (!scored) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			return obj('_score', obj('order', order))
		}
		val path = clause.field
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('sorting by a vector field')
		val name = if (path.rawPath !== null) OpenSearchBodies.field(path.rawPath) else {
			val top = target.field(ModelUtil.fieldName(path.field))
			val leaf = ModelUtil.leaf(path)
			val dotted = fieldPath(target, path)
			if (path.subFields.empty) top.exact else if (leaf instanceof PayloadField && (leaf as PayloadField).fullText) dotted + '.' + RAW else dotted
		}
		obj(name, obj('order', order, 'missing', text('_last')))
	}

	def dispatch List<Stmt> body(Sample it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		val target = schema.index(collection)
		val matching = boolOf(clauses(it, collection, target, filter, null))
		val first = written(it, 'method') && method == ESampleMethod.FIRST
		val requestBody = if (first) {
			obj('query', matching, 'sort', arr(obj(target.keyField.exact, obj('order', text('asc')))), 'size', num(sampleSize))
		} else {
			obj('query', obj('function_score', obj('query', matching, 'random_score', Code.entries, 'boost_mode', text('replace'))), 'size', num(sampleSize))
		}
		#[new Code.Return(searchCall(target, shape(it, collection, target, requestBody, output, newArrayList, false), null, null, null))]
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		val target = schema.index(collection)
		val mapped = target.field(ModelUtil.fieldName(vectorFieldOf(collection, it.vectorField)))
		if (mapped.vector.sparse) throw Unsupported.of('a distance matrix of sparse vectors; the knn query of the runtime compares knn_vector values')
		#[new Code.Return(call('distance_matrix', 'index', text(target.name), 'field', text(mapped.name), 'key', text(target.key),
			'sample', num(if (written(it, 'sampleSize')) sampleSize else 10), 'limit', num(if (written(it, 'matrixLimit')) matrixLimit else 3),
			'score', scoreForm(mapped.vector.space, false), 'query', boolOf(clauses(it, collection, target, filter, null))))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		throw Unsupported.of('generative search; ' + NO_GENERATION)
	}

	def dispatch List<Stmt> body(BatchQuery it) {
		val List<Expr> calls = newArrayList
		val owner = current
		var position = 0
		for (batched : queries) {
			val helperName = owner + '_query_' + position
			current = helperName
			helpers += new Program.Method(helperName, ModelUtil.parameterNames(batched), body(batched), newArrayList, newArrayList, true)
			calls += Code.self(helperName)
			position = position + 1
		}
		current = owner
		#[new Code.Return(Code.items(calls))]
	}

	def dispatch List<Stmt> body(Method it) {
		throw Unsupported.of(NOT_MAPPED + eClass.name)
	}

	// ---------------------------------------------------------------- helpers

	def private Object feature(EObject object, String featureName) {
		val slot = object.eClass.getEStructuralFeature(featureName)
		if (slot === null) null else object.eGet(slot)
	}

	def private int offsetOf(EObject statement) {
		if (written(statement, 'offset')) feature(statement, 'offset') as Integer else 0
	}

	def private List<Field> excludedOf(EObject statement) {
		(feature(statement, 'excluded') ?: newArrayList) as List<Field>
	}

	def private Entries boundsOf(ScoreBounds scoreBounds) {
		if (scoreBounds === null) return null
		val result = obj('min', if (written(scoreBounds, 'min')) num(scoreBounds.min) else null, 'max', if (written(scoreBounds, 'max')) num(scoreBounds.max) else null)
		if (result.entries.empty) null else result
	}

	def private Code.Call searchCall(OpenSearchIndex target, Entries requestBody, Entries form, ScoreBounds scoreBounds, String group) {
		call('search', 'index', text(target.name), 'body', requestBody, 'key', text(target.key), 'score', form, 'bounds', boundsOf(scoreBounds),
			'group', if (group === null) null else text(group))
	}

	/** The searched vector field: the named one, else the collection's only vector field. */
	def private VectorField vectorFieldOf(VectorCollection collection, VectorField named) {
		if (named !== null) {
			if (!collection.fields.contains(named)) throw Unsupported.of('vector field ' + ModelUtil.fieldName(named) + ', which is not a field of collection ' + collection.name)
			return named
		}
		val vectorFields = ModelUtil.vectorFields(collection)
		if (vectorFields.size != 1) throw Unsupported.of("a search without 'using' in a collection with " + vectorFields.size + ' vector fields')
		vectorFields.head
	}

	/** A statement's filter, its ids and its tenant, namespace and partition conditions, as filter clauses. */
	def private List<Expr> clauses(EObject statement, VectorCollection collection, OpenSearchIndex target, FilterBlock block, Expr idValues) {
		val List<Expr> result = newArrayList
		if (idValues !== null) result += obj('ids', obj('values', idValues))
		if (block !== null) result += new OpenSearchFilter(target).query(block.condition)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result += OpenSearchSchema.term(target.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result += OpenSearchSchema.term(target.namespace, text(namespace as String))
		val single = feature(statement, 'partition') as Partition
		val several = feature(statement, 'partitions') as List<Partition>
		val List<Expr> named = newArrayList
		if (single !== null) named += text(single.name)
		if (several !== null) for (p : several) named += text(p.name)
		if (!named.empty) {
			if (target.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
			result += obj('terms', obj(target.partition, arr(named)))
		}
		result
	}

	def private Entries boolOf(List<Expr> filters) {
		if (filters.empty) obj('match_all', Code.entries) else obj('bool', obj('filter', arr(filters)))
	}

	/** Document ids as JSON strings (_id is a string); parameters become strings in the runtime. */
	def private Expr idsOf(List<Literal> values) {
		if (values.empty) return null
		if (values.forall[v | v instanceof StringLit || v instanceof NumberLit]) {
			return arr(values.map[v | text(if (v instanceof StringLit) (v as StringLit).value else OpenSearchFilter.number((v as NumberLit).value)) as Expr].toList)
		}
		call('ids', 'values', Code.items(values.map[v | Literals.of(v)].toList))
	}

	/** The _source of the hits: the key and the output fields, vectors only with with-vectors (or named in the output). */
	def private Entries shape(EObject statement, VectorCollection collection, OpenSearchIndex target, Entries requestBody, OutputSpec outputSpec,
		List<Field> excluded, boolean vectors) {
		val List<Expr> includes = newArrayList
		var returnVectors = vectors
		if (outputSpec !== null && !outputSpec.allFields) {
			includes += text(target.key)
			for (f : outputSpec.fields) {
				if (f instanceof VectorField) returnVectors = true
				val named = text(target.field(ModelUtil.fieldName(f)).name)
				if (f !== collection.primaryKey && !excluded.contains(f) && !includes.contains(named)) includes += named
			}
			if (vectors) {
				for (v : ModelUtil.vectorFields(collection)) {
					val named = text(target.field(ModelUtil.fieldName(v)).name)
					if (!excluded.contains(v) && !includes.contains(named)) includes += named
				}
			}
		}
		val List<Expr> excludes = newArrayList
		excludes += excluded.map[f | text(target.field(ModelUtil.fieldName(f)).name) as Expr]
		if (!returnVectors) {
			for (v : target.vectorFields) if (!excludes.contains(text(v))) excludes += text(v)
		}
		requestBody.put('_source', obj('includes', if (includes.empty) null else arr(includes), 'excludes', if (excludes.empty) null else arr(excludes)))
		if (written(statement, 'withTotal')) requestBody.put('track_total_hits', new Code.Flag(true))
		marks(statement, target, requestBody)
		val sortClauses = (feature(statement, 'sortClauses') ?: newArrayList) as List<SortClause>
		if (!sortClauses.empty && !requestBody.has('sort')) requestBody.put('sort', arr(sortClauses.map[c | sortEntry(target, c, true) as Expr].toList))
		// A field sort makes OpenSearch return _score null unless track_scores is set (search API, track_scores); the score is still part of the result.
		if (sortClauses.exists[!byRelevance] && !requestBody.has('track_scores')) requestBody.put('track_scores', new Code.Flag(true))
		val aggregations = (feature(statement, 'aggregations') ?: newArrayList) as List<AggregationClause>
		if (!aggregations.empty) requestBody.put('aggs', aggs(target, aggregations))
		val group = groupOf(statement, target)
		if (group !== null) {
			val size = if (written(statement, 'groupSize')) feature(statement, 'groupSize') as Integer else 1
			requestBody.put('collapse', obj('field', text(group), 'inner_hits', obj('name', text(GROUP), 'size', num(size),
				'_source', requestBody.get('_source'))))
		}
		extensionBody(statement, requestBody)
		requestBody
	}

	/** explain and highlight, which every search body and every candidate request of the shared result-shape module carries. */
	def private void marks(EObject statement, OpenSearchIndex target, Entries requestBody) {
		if (written(statement, 'explain')) requestBody.put('explain', new Code.Flag(true))
		val highlighted = (feature(statement, 'highlighted') ?: newArrayList) as List<Field>
		if (!highlighted.empty) {
			val Entries highlightFields = Code.entries
			for (f : highlighted) {
				val mapped = target.field(ModelUtil.fieldName(f))
				if (!mapped.fullText) throw Unsupported.of('highlighting ' + mapped.name + ', which is not a full-text field')
				highlightFields.put(mapped.name, Code.entries)
			}
			requestBody.put('highlight', obj('fields', highlightFields))
		}
	}

	/**
	 * A candidate request of the shared result-shape module: the fields the module reads and the vectors it compares or returns
	 * (OpenSearchShapes), explain, highlight and the extension parameters; offset, cursor, sort, groups, bounds and totals are the
	 * module's.
	 */
	def private Entries candidateBody(EObject statement, OpenSearchIndex target, Entries requestBody, ShapeSpec shapes) {
		requestBody.put('_source', OpenSearchShapes.source(shapes, target))
		marks(statement, target, requestBody)
		extensionBody(statement, requestBody)
		requestBody
	}

	/** The collapse field of a grouped search: the exact form of the group-by field. */
	def private String groupOf(EObject statement, OpenSearchIndex target) {
		val path = feature(statement, 'groupBy') as FieldPath
		if (path === null) return null
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('grouping by a vector field')
		if (path.rawPath !== null || !path.subFields.empty) return fieldPath(target, path)
		target.field(ModelUtil.fieldName(path.field)).exact
	}

	/** Aggregations by name (the alias, else kind_field): metrics, terms, histograms, ranges and percentiles. */
	def private Entries aggs(OpenSearchIndex target, List<AggregationClause> aggregationClauses) {
		val Entries result = Code.entries
		for (clause : aggregationClauses) {
			val path = if (clause.field.rawPath !== null) OpenSearchBodies.field(clause.field.rawPath) else {
				val top = target.field(ModelUtil.fieldName(clause.field.field))
				if (clause.field.subFields.empty) top.exact else fieldPath(target, clause.field)
			}
			val fieldName = text(path)
			val size = if (written(clause, 'bucketCount')) num(clause.bucketCount) else null
			val definition = switch clause.kind.literal {
				case 'count': obj('value_count', obj('field', fieldName))
				case 'sum',
				case 'avg',
				case 'min',
				case 'max',
				case 'stats',
				case 'percentiles',
				case 'cardinality': obj(clause.kind.literal, obj('field', fieldName))
				case 'distinct_count': obj('cardinality', obj('field', fieldName))
				case 'terms',
				case 'facet': obj('terms', obj('field', fieldName, 'size', size))
				case 'histogram': {
					if (clause.interval === null || !clause.interval.matches('[0-9]+(\\.[0-9]+)?')) throw Unsupported.of('a histogram without a numeric interval')
					obj('histogram', obj('field', fieldName, 'interval', num(Double.parseDouble(clause.interval))))
				}
				case 'date_histogram': {
					if (clause.interval === null) throw Unsupported.of('a date histogram without an interval')
					obj('date_histogram', obj('field', fieldName, 'fixed_interval', text(clause.interval)))
				}
				default: throw Unsupported.of('range aggregations; vecDSL gives no ranges, which the OpenSearch range aggregation needs')
			}
			result.put(clause.alias ?: clause.kind.literal + '_' + path.replace('.', '_'), definition)
		}
		result
	}

	/** Search body parameters from an 'extension opensearch' block: terminate_after, min_score, track_scores, timeout. */
	def private void extensionBody(EObject host, Entries requestBody) {
		for (entry : ModelUtil.extParams(host, OpenSearchGenerator.VENDOR).entrySet) {
			if (!SEARCH_KEYS.contains(entry.key)) throw Unsupported.of('extension parameter ' + entry.key + '; OpenSearch search body parameters the generator passes are ' + SEARCH_KEYS.join(', '))
			if (entry.value instanceof ParamRef || entry.value instanceof org.ftn.vecDSL.vecDSL.EnvRef) throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a literal')
			requestBody.put(entry.key, Literals.of(entry.value))
		}
	}

	/** Rejects clauses OpenSearch lacks, with their reason, and clauses not generated yet. */
	def private void rejectClauses(EObject statement, String... features) {
		for (featureName : features) {
			if (written(statement, featureName)) {
				val clause = "the '" + (CLAUSES.get(featureName) ?: featureName) + "' clause"
				val pending = PENDING_CLAUSES.get(featureName)
				if (pending !== null) throw Unsupported.of(clause + '; ' + PENDING + ' (' + pending + ')')
				val reason = REASONS.get(featureName)
				throw Unsupported.of(if (reason === null) clause else clause + '; ' + reason)
			}
		}
	}

	/** 'extension opensearch' blocks of queries carry search body parameters; extension items of other kinds are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == OpenSearchGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on OpenSearch')
				}
			}
		}
	}

	/** The query vector: a vector literal or parameter (bit vectors as hex strings), or for id input the stored vector (runtime). */
	def private Expr queryVectorOf(QueryInput queryInput, OpenSearchIndex.Field mapped) {
		switch queryInput.kind {
			case VECTOR: {
				if (mapped.vector.sparse) throw Unsupported.of('vector input on sparse field ' + mapped.name)
				convert(queryInput.value, mapped)
			}
			case ID: new Code.Nil
			case SPARSE_VECTOR: throw Unsupported.of('sparse_vector input on dense field ' + mapped.name)
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; a knn query compares one query vector with one knn_vector per document (or per nested document)')
			default: throw Unsupported.of(queryInput.kind.literal + ' input' + NO_MODELS)
		}
	}

	def private Expr keywordText(QueryInput queryInput) {
		if (queryInput === null || queryInput.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(queryInput.value instanceof StringLit || queryInput.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(queryInput.value)
	}

	def private Expr recordsOf(RecordSource recordSource, OpenSearchIndex target) {
		switch recordSource {
			FileSource: call('read_records', 'location', text(recordSource.location))
			InlineRecords: Code.items(recordSource.records.map[r | recordOf(r, target) as Expr].toList)
			ParamSource: Literals.of(recordSource.param)
		}
	}

	def private Entries recordOf(RecordLiteral recordLiteral, OpenSearchIndex target) {
		val Entries result = Code.entries
		for (entry : recordLiteral.entries) {
			val mapped = target.field(ModelUtil.fieldName(entry.field))
			result.put(mapped.name, convert(entry.value, mapped))
		}
		result
	}

	/**
	 * A literal in the form OpenSearch reads for its field: sparse vectors as rank_features objects of token (the dimension
	 * index as a string) to strictly positive weight, binary vectors as signed bytes (eight dimensions each, most significant bit
	 * first), everything else as written.
	 */
	def private Expr convert(Literal literal, OpenSearchIndex.Field mapped) {
		if (mapped.vector === null) return Literals.of(literal)
		if (mapped.vector.sparse && literal instanceof SparseLit) {
			val Entries weights = Code.entries
			for (entry : (literal as SparseLit).entries) {
				if (entry.weight <= 0) throw Unsupported.of('a sparse weight of ' + entry.weight + '; rank_features fields take strictly positive weights')
				weights.put(String.valueOf(entry.index), num(entry.weight))
			}
			return weights
		}
		if (mapped.vector.element == 'binary') {
			if (literal instanceof ListLit) {
				val bits = (literal as ListLit).items.map[b | if (b instanceof NumberLit && (b as NumberLit).value != 0) 1 else 0].toList
				val List<Expr> packed = newArrayList
				var position = 0
				while (position < bits.size) {
					var octet = 0
					for (bit : bits.subList(position, Math.min(bits.size, position + 8))) octet = octet * 2 + bit
					packed += num(if (octet > 127) octet - 256 else octet)
					position = position + 8
				}
				return Code.items(packed)
			}
			if (literal instanceof ParamRef) throw Unsupported.of('a parameter for binary vector field ' + mapped.name + '; the generator packs binary vectors from literals into signed bytes')
		}
		Literals.of(literal)
	}
}
