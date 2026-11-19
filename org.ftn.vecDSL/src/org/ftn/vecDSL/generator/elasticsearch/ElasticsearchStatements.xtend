package org.ftn.vecDSL.generator.elasticsearch

import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
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
import org.ftn.vecDSL.vecDSL.MmrConfig
import org.ftn.vecDSL.vecDSL.MetricsConfig
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

import static org.ftn.vecDSL.generator.elasticsearch.ElasticsearchBodies.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Elasticsearch REST requests in the host-language-neutral code model. Every request body is
 * built here as a JSON value and passed to the runtime, so the three host languages send identical requests: request sends
 * one request, search and fetch shape hits into records with the canonical score, write sends _bulk requests, hybrid
 * fuses the legs of a hybrid search (the rrf and linear retrievers need an enterprise license), and recommend computes the
 * query vector of a recommendation from stored examples.
 */
class ElasticsearchStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; ElasticsearchCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Elasticsearch: '
	/** Suffix of constructs Elasticsearch supports that the generator does not produce yet. */
	public static val PENDING = 'has no Elasticsearch mapping yet'
	public static val NO_MODELS = '; models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE), so generated programs supply their own vectors'
	public static val EXTERNAL = 'collections derived from external sources; Elasticsearch reads parquet, lance, iceberg or vortex files neither as indices nor through ingest pipelines'
	public static val NO_TTL = 'ttl; Elasticsearch documents do not expire (index lifecycle management deletes whole indices after rollover, not single documents)'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; Elasticsearch has no per-index guard against deletion (action.destructive_requires_name only refuses wildcard deletes)'
	public static val NO_TAGS_REASON = 'Elasticsearch keeps them in the _meta of the mapping'
	public static val NO_FILES = 'Elasticsearch reads analysis files from the config directory of the node, which a client cannot write'
	public static val LANGUAGES_REFERENCE = 'Elasticsearch has stop-word lists and stemmers for its language analyzers (english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian, turkish, serbian and others)'
	public static val NO_TRANSACTIONS = 'transactions; Elasticsearch has no multi-document transactions (each document write is atomic, and optimistic concurrency control guards one document)'
	public static val NO_GENERATION = 'generative models run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)'
	public static val NO_RERANKERS = 'the text_similarity_reranker retriever needs an enterprise license (TextSimilarityRankBuilder, TEXT_SIMILARITY_RERANKER_FEATURE) and an inference endpoint'
	public static val NO_METRICS = 'metrics configuration; Elasticsearch metrics are collected by Elastic Agent or Metricbeat from the stats APIs, not configured per program'
	public static val NO_CLUSTER_LIMITS = 'cluster limits; Elasticsearch limits shards per node (cluster.max_shards_per_node), not the number of indices'
	public static val ONE_CLUSTER = 'the program talks to one Elasticsearch cluster; clusters are deployments, created outside the REST API'
	static val FIXED_SHARDS = 'the shard count of an existing index is fixed (the split and shrink APIs copy it into a new index)'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain',
		'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'sortClauses' -> 'sort', 'aggregations' -> 'aggregate',
		'prefetches' -> 'prefetch', 'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model', 'sliceIndex' -> 'slice'
	}

	/** Why Elasticsearch (in the basic license) cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'Elasticsearch searches the current state of an index (a point in time pins a state for paging, not a past one)',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated Elasticsearch programs embed no query text' + NO_MODELS
	}

	/** Clauses Elasticsearch supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'a kNN search inside a nested field', 'prefetches' -> 'nested retrievers'
	}

	/** Hits a search reads for a cursor page: the k of its kNN query, as Vespa's cursor reads the first 1000 hits of its order. */
	static val CURSOR_WINDOW = 1000

	/** Search body parameters that an 'extension elasticsearch' block on a query may set. */
	static val SEARCH_KEYS = #['terminate_after', 'min_score', 'track_scores', 'timeout']

	val List<String> notes = newArrayList
	val ElasticsearchSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new ElasticsearchSchema(notes, script)
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
			case 'bm25': 'Elasticsearch scores text fields with BM25 directly; a function that writes BM25 weights into a sparse vector field has no equivalent (sparse_vector fields take their weights from the client or an inference model)'
			case 'custom': 'custom functions; ' + PENDING + ' (an ingest pipeline with a script processor)'
			default: 'model functions run through the inference API, which needs an enterprise license (InferencePlugin, INFERENCE_API_FEATURE)'
		}
	}

	/** Index store settings of a storage block: mmap is the mmapfs store; the other kinds are node or license features. */
	def static void storage(StorageConfig storage, Entries settings) {
		if (storage === null) return;
		if (storage.location !== null) throw Unsupported.of('a storage location; Elasticsearch keeps index data under path.data, a node setting')
		if (storage.tiered) throw Unsupported.of('tiered storage; data tiers move indices between nodes of different roles, and one node has one tier')
		switch storage.type.literal {
			case 'disk': settings.put('index.store.type', text(if (storage.mmap) 'mmapfs' else 'niofs'))
			case 'memory': throw Unsupported.of('memory storage; Elasticsearch stores indices on disk (the memory store type was removed in 7.0)')
			case 'object_storage': throw Unsupported.of('object storage; searchable snapshots, which mount indices from a repository, need an enterprise license')
			default: throw Unsupported.of('embedded storage; Elasticsearch runs as a server')
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
			default: throw Unsupported.of('collection limits (' + limit.type.literal + '); Elasticsearch limits the fields of a mapping (index.mapping.total_fields.limit), and the shard count of an index is a setting, not a limit')
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
				for (key : ModelUtil.extParams(concept, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
				if (concept.collections.empty && (concept.replication !== null || concept.sharding !== null)) {
					throw Unsupported.of('replication and sharding of a cluster without collections; Elasticsearch sets shards and replicas per index')
				}
				for (collection : concept.collections) result += schema.createIndex(collection, ifNotExists, fresh)
			}
			Database: for (collection : concept.collections) result += schema.createIndex(collection, ifNotExists, fresh)
			Alias: {
				val target = schema.index(concept.collection)
				result += new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('add', obj('index', text(target.name), 'alias', text(aliasName(concept.name))))))))
			}
			VectorIndex: result += updateVectorIndex(concept)
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
				val owner = EcoreUtil2.getContainerOfType(concept, VectorIndex)
				if (owner === null) throw Unsupported.of('quantization outside a vector index; Elasticsearch quantizes through the index_options of a dense_vector field')
				result += updateVectorIndex(owner)
			}
			ReplicationConfig: {
				val cluster = EcoreUtil2.getContainerOfType(concept, Cluster)
				if (cluster === null) throw Unsupported.of('replication outside a cluster; ' + ONE_CLUSTER)
				result += replicas(cluster, concept)
			}
			ShardConfig: throw Unsupported.of('sharding of existing indices; ' + FIXED_SHARDS)
			StorageConfig: {
				val collection = EcoreUtil2.getContainerOfType(concept, VectorCollection)
				if (collection === null) throw Unsupported.of('storage settings outside a collection; Elasticsearch sets the store type per index')
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
			throw Unsupported.of('embedded deployments; Elasticsearch runs as a server that the clients reach over HTTP')
		}
		if (concept.token !== null) throw Unsupported.of('bearer tokens; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)')
		if (concept.password instanceof StringLit || concept.apiKey instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; Elasticsearch serves its REST API over HTTP only')
		if (concept.databaseName !== null) throw Unsupported.of('a database in the connection; Elasticsearch has no databases (logical databases are index name prefixes)')
		for (key : ModelUtil.extParams(concept, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(concept, 'cloud', 'region', 'project')
	}

	/** Replicas of the indices of a cluster's collections: number_of_replicas is the replication factor minus one. */
	def private List<Stmt> replicas(Cluster cluster, ReplicationConfig replication) {
		if (replication.factor < 1) throw Unsupported.of('a replication factor below 1')
		if (cluster.collections.empty) throw Unsupported.of('replication of a cluster without collections; Elasticsearch sets replicas per index')
		val names = cluster.collections.map[c | schema.index(c).name].join(',')
		#[new Code.Run(request('PUT', '/' + names + '/_settings', null, obj('index.number_of_replicas', num(replication.factor - 1))))]
	}

	/** Authentication: username_password and api_key are the connection's; roles are role definitions; the rest needs licenses. */
	def private List<Stmt> authentication(AuthConfig auth) {
		switch auth.type {
			case EAuthType.TOKEN: throw Unsupported.of('token authentication; the token service of Elasticsearch needs a standard license (Security, TOKEN_SERVICE_FEATURE)')
			case EAuthType.MTLS: throw Unsupported.of('mtls authentication; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)')
			case EAuthType.SSO: throw Unsupported.of('sso authentication; the SAML and OpenID Connect realms need a platinum license (Security, SAML_REALM_FEATURE, OIDC_REALM_FEATURE)')
			default: {}
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; the PKI realm needs a gold license (Security, PKI_REALM_FEATURE)')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; Elasticsearch encrypts at rest only through the file system or the cloud provider')
		if (auth.auditLog) throw Unsupported.of('audit logs; auditing needs a gold license (Security, AUDITING_FEATURE)')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding)
		result
	}

	/** A role with index privileges on a collection's index (or on every index); PUT creates or updates it. */
	def private List<Stmt> role(RoleBinding binding) {
		val names = if (binding.collection === null) text('*') else text(schema.index(binding.collection).name)
		val grants = privileges(binding.privileges)
		val Entries roleBody = obj('indices', arr(obj('names', arr(names), 'privileges', arr(if (grants.empty) #[text('read') as Expr] else grants))),
			'description', if (binding.description === null) null else text(binding.description))
		#[new Code.Run(request('PUT', '/_security/role/' + roleName(binding.roleName), null, roleBody))]
	}

	def private String roleName(String declared) {
		if (!declared.matches('[A-Za-z0-9_][A-Za-z0-9_.@-]*')) throw Unsupported.of('the role name ' + declared + '; the generator writes role names of letters, digits and _ . @ -')
		declared
	}

	/** A snapshot lifecycle policy (SLM, basic license) on the fs repository of the compose file. */
	def private List<Stmt> backupSchedule(BackupSchedule schedule) {
		val repository = repository(schedule.location)
		val target = schema.index(schedule.collection)
		val policy = obj('schedule', text(ElasticsearchBodies.schedule(schedule.interval)), 'name', text('<' + aliasName(schedule.name) + '-{now/d}>'),
			'repository', text(repository), 'config', obj('indices', arr(text(target.name)), 'include_global_state', new Code.Flag(false)),
			'retention', if (written(schedule, 'retentionCount')) obj('max_count', num(schedule.retentionCount), 'min_count', num(1)) else null)
		#[registerRepository(repository), new Code.Run(request('PUT', '/_slm/policy/' + aliasName(schedule.name), null, policy))]
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
		throw Unsupported.of(what + ' outside a collection; Elasticsearch adds it to an index')
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
			target = if (kind == 'tenant') new ElasticsearchIndex(target.name, target.key, target.fields, sliceField, target.namespace, target.partition, target.renames, target.database)
				else new ElasticsearchIndex(target.name, target.key, target.fields, target.tenant, sliceField, target.partition, target.renames, target.database)
			schema.evolve(collection, target)
		}
		val alias = obj('index', text(target.name), 'alias', text(sliceAlias(target.name, kind, value)), 'filter', ElasticsearchSchema.term(sliceField, text(value)))
		result += new Code.Run(request('POST', '/_aliases', null, obj('actions', arr(obj('add', alias)))))
		result
	}

	/** Analysis settings change on a closed index: close, update the analyzer, open again and wait until it is usable. */
	def private List<Stmt> reanalyze(Analyzer analyzer) {
		val collection = ModelUtil.owningCollection(analyzer)
		if (collection === null) throw Unsupported.of('an analyzer outside a collection; Elasticsearch defines analyzers in the settings of an index')
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

	/** A vector index created for an existing field: its index_options in a mapping update (the server allows quantization upgrades). */
	def private List<Stmt> updateVectorIndex(VectorIndex vectorIndex) {
		val owner = vectorIndex.field ?: EcoreUtil2.getContainerOfType(vectorIndex, VectorField)
		if (owner === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(owner, 'a vector index')
		val updated = schema.field(collection, owner)
		val target = schema.index(collection)
		#[new Code.Run(request('PUT', '/' + target.name + '/_mapping', null, obj('properties', obj(target.field(updated.name).name, updated.mapping))))]
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
	def private Code.Call byQuery(ElasticsearchIndex target, String endpoint, Entries matching, Entries painless, String counter, String wrap, Integer maxDocs) {
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
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; Elasticsearch alters indices and aliases')
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
				val newField = ElasticsearchBodies.field(operation.newName)
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
	 * A field leaves the documents (update_by_query removes it from _source); the mapping keeps the field, since Elasticsearch
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
	def private List<Stmt> setProperty(ElasticsearchIndex target, SetPropertyOp operation) {
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
			// DELETE /*,-.*/_alias/<name> removes the alias from every index that has it and answers 404 only when no index has it;
			// a remove action on * with must_exist true fails on the first index without the alias. The -.* keeps out system indices.
			Alias: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/*,-.*/_alias/' + aliasName(concept.name)), 'ignore', ignore)) as Stmt]
			VectorIndex: throw Unsupported.of('dropping a vector index; the index_options of a dense_vector field are part of its mapping, which Elasticsearch cannot remove')
			PayloadIndex: throw Unsupported.of('dropping a payload index; whether a field is indexed is part of its mapping, which Elasticsearch cannot change')
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; Elasticsearch upgrades index_options (hnsw to int8_hnsw to int4_hnsw to bbq_hnsw) but cannot return to raw vectors')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not an Elasticsearch object')
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
			AuthConfig: throw Unsupported.of('dropping authentication; security is configured on the server (xpack.security.enabled)')
			RoleBinding: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/_security/role/' + roleName(concept.roleName)), 'ignore', ignore)) as Stmt]
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: #[new Code.Run(call('request', 'method', text('DELETE'), 'path', text('/_slm/policy/' + aliasName(concept.name)), 'ignore', ignore)) as Stmt]
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: {
				val collection = EcoreUtil2.getContainerOfType(concept, VectorCollection)
				if (collection === null || concept.type.literal != 'max_fields') throw Unsupported.of('collection limits (' + concept.type.literal + '); Elasticsearch limits the fields of a mapping only')
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
			new Code.Return(byQuery(target, '_delete_by_query', ElasticsearchSchema.term(fieldName, text(value)), null, 'deleted', 'delete_count', null))]
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
			'batch_size', if (batch === null) null else num(batch), 'ignore_conflicts', if (mode.containsKey('ignore')) new Code.Flag(true) else null)
	}

	def private Entries defaultsOf(ElasticsearchIndex target) {
		val Map<String, Expr> values = target.defaults
		if (values.empty) null else Code.entries(values)
	}

	/** The slice fields of a write: tenant, namespace and partition; partitioned collections default to the default partition. */
	def private Entries sliceFields(EObject statement, VectorCollection collection, ElasticsearchIndex target) {
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
				'counter', text('update_count'), 'only', Code.items(ids.map[i | Literals.of(i)].toList)))]
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
		if (continuous) throw Unsupported.of('continuous import; Elasticsearch pulls no streams itself (Logstash and Elastic Agent are separate programs)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; Elasticsearch indexes documents that a client sends, and reads object stores only as snapshot repositories')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV records in the runtime)')
			throw Unsupported.of('import format ' + format.literal + '; the _bulk API takes JSON documents')
		}
		schema.note(it, 'parallelism', 'async')
		for (key : ModelUtil.extParams(it, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val target = schema.index(collection)
		val writeModeOf = writeMode(if (written(it, 'onConflict')) onConflict.literal else 'error')
		#[new Code.Return(call('import_file', 'location', text(location), 'index', text(target.name), 'key', text(target.key), 'op', text(writeModeOf.get('op')),
			'counter', text(writeModeOf.get('counter')), 'extra', sliceFields(it, collection, target), 'defaults', defaultsOf(target),
			'batch_size', if (written(it, 'batchSize')) num(batchSize) else null, 'ignore_conflicts', if (writeModeOf.containsKey('ignore')) new Code.Flag(true) else null))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of(NO_TRANSACTIONS)
	}

	/** Every write carries refresh=true, so 'wait' needs no call; write consistency is the server's (wait_for_active_shards 1). */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (key : ModelUtil.extParams(statement, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- operational

	def dispatch List<Stmt> body(Load it) {
		operationExtensions(it)
		if (!partitions.empty) throw Unsupported.of('loading partitions; Elasticsearch opens and closes whole indices')
		val target = schema.index(collection)
		#[new Code.Run(request('POST', '/' + target.name + '/_open', null, null)),
			new Code.Return(call('wait_health', 'index', text(target.name), 'status', text('yellow'), 'timeout_seconds', num(60)))]
	}

	def dispatch List<Stmt> body(Release it) {
		operationExtensions(it)
		if (!partitions.empty) throw Unsupported.of('releasing partitions; Elasticsearch opens and closes whole indices')
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
		#[new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', forceMergeParams, null))]
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

	/** A snapshot of the index into the fs repository (path.repo); Elasticsearch snapshots are incremental by design. */
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

	/** Elasticsearch rebuilds HNSW graphs when it merges segments: a force merge into one segment rebuilds the index's graph. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		operationExtensions(it)
		schema.note(it, 'concurrently')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		#[new Code.Run(request('POST', '/' + schema.index(collection).name + '/_forcemerge', obj('max_num_segments', num(1)), null))]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val target = schema.index(collection)
		if (namespace !== null) {
			if (target.namespace === null) throw Unsupported.of('a namespace of a collection without namespaces')
			return #[new Code.Return(byQuery(target, '_delete_by_query', ElasticsearchSchema.term(target.namespace, text(namespace)), null, 'deleted', 'delete_count', null))]
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
					if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; Elasticsearch lists the indices of a cluster')
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

	def private ElasticsearchIndex scopeIndex(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.index(owner as VectorCollection)
	}

	/** Elasticsearch builds vector indexes with each segment, so awaiting one waits for the index's health. */
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
		for (key : ModelUtil.extParams(statement, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		rejectClauses(it, 'elementScope', 'asOf', 'reranker', 'queryModel', 'prefetches')
		if (!highlighted.empty) throw Unsupported.of("the 'highlight' clause on a vector search; highlighting marks the terms of a text query, which a vector search has not")
		queryExtensions(it)
		combinations(it)
		val target = schema.index(collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val List<Expr> filters = clauses(it, collection, target, filter, null)
		if (input === null) throw Unsupported.of('a vector query without input')
		if (mapped.vector.sparse) {
			if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause on a sparse field; the diversify retriever compares dense vectors")
			if (input.kind != EQueryInputMode.SPARSE_VECTOR) throw Unsupported.of(input.kind.literal + ' input on sparse field ' + mapped.name)
			schema.note(it, 'exact', 'metric')
			val Entries sparseBody = shape(it, collection, target, obj('query', boostQuery(target, obj('bool', obj('must', arr(obj('sparse_vector', obj('field', text(mapped.name), 'query_vector', convert(input.value, mapped)))),
				'filter', if (filters.empty) null else arr(filters))), boost)), output, excludedOf(it), withVectors)
			sparseBody.put('size', num(topK))
			if (offsetOf(it) > 0) sparseBody.put('from', num(offsetOf(it)))
			return #[new Code.Return(paged(searchCall(target, cursorOrder(it, target, sparseBody), relevance(), bounds, groupOf(it, target)), it, target))]
		}
		val searchMetric = if (written(it, 'metric')) metric.literal else mapped.vector.metric
		val exhaustive = exact || searchMetric != mapped.vector.metric || (params !== null && (params.exhaustive || (written(params, 'filterStrategy') && params.filterStrategy.literal == 'pre_filter')))
		val queryVector = queryVectorOf(input, mapped)
		val Entries requestBody = if (exhaustive) {
			if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause with an exact search; the diversify retriever wraps a kNN search")
			val kind = ElasticsearchBodies.exactKind(searchMetric, mapped.vector.element)
			if (params !== null) schema.note(params, 'candidatePool', 'filterStrategy', 'oversampling', 'rescore')
			val Entries scriptParams = obj('query_vector', queryVector, 'field', text(mapped.name), 'dims', if (kind == 'hamming') num(mapped.vector.dims) else null)
			obj('query', boostQuery(target, obj('script_score', obj('query', boolOf(filters), 'script', script(exactScript(kind), scriptParams))), boost), 'size', num(topK),
				'from', if (offsetOf(it) > 0) num(offsetOf(it)) else null)
		} else {
			knnBody(it, target, mapped, queryVector, filters)
		}
		if (requestBody.has('knn') && params !== null && written(params, 'filterStrategy') && params.filterStrategy.literal == 'post_filter' && !filters.empty) {
			(requestBody.get('knn') as Entries).entries.remove('filter')
			requestBody.put('post_filter', boolOf(filters))
		}
		if (mmr !== null && mmr.vectorField !== null && mmr.vectorField !== searched) throw Unsupported.of('mmr on another vector field than the searched one')
		if (params !== null && written(params, 'timeoutMillis')) requestBody.put('timeout', text(params.timeoutMillis + 'ms'))
		val formKind = if (exhaustive) obj('kind', text(ElasticsearchBodies.exactKind(searchMetric, mapped.vector.element)), 'dims', num(mapped.vector.dims)) else scoreForm(mapped.vector, false)
		val form = if (boost !== null) relevance() else if (nativeScore) scoreForm(mapped.vector, true) else formKind
		val shaped = cursorOrder(it, target, shape(it, collection, target, requestBody, output, excludedOf(it), withVectors))
		if (input.kind == EQueryInputMode.ID) return #[new Code.Return(paged(byStoredVector(it, input, collection, mapped, shaped, form), it, target))]
		#[new Code.Return(paged(searchCall(target, shaped, form, bounds, groupOf(it, target)), it, target))]
	}

	/**
	 * A search by the stored vector of a document: the runtime reads the vector and puts it into every query_vector of the body,
	 * which the generator leaves empty (kNN section or query, diversify retriever, script_score params).
	 */
	def private Code.Call byStoredVector(EObject statement, QueryInput queryInput, VectorCollection collection, ElasticsearchIndex.Field mapped, Entries searchBody, Entries form) {
		val target = schema.index(collection)
		val lookup = schema.index(queryInput.lookupCollection ?: collection)
		val group = groupOf(statement, target)
		call('recommend', 'index', text(target.name), 'lookup', text(lookup.name), 'field', text(mapped.name), 'key', text(target.key),
			'body', searchBody, 'score', form, 'positive_ids', arr(Literals.of(queryInput.value)), 'bounds', boundsOf(feature(statement, 'bounds') as ScoreBounds),
			'group', if (group === null) null else text(group))
	}

	/**
	 * The request of a dense kNN search: the top-level knn section; the diversify retriever around the knn retriever (mmr); or a
	 * knn query (group-by, boost or cursor), in a function_score query with a boost. Collapse inner_hits rerun the request's
	 * query, which a top-level knn section is not part of, and search_after and function_score need a query.
	 * A cursor page reads the CURSOR_WINDOW nearest records.
	 */
	def private Entries knnBody(EObject statement, ElasticsearchIndex target, ElasticsearchIndex.Field mapped, Expr queryVector, List<Expr> filters) {
		val size = feature(statement, 'topK') as Integer
		val mmrConfig = feature(statement, 'mmr') as MmrConfig
		val block = feature(statement, 'boost') as BoostBlock
		val paging = written(statement, 'cursor')
		val k = if (paging) CURSOR_WINDOW else size + offsetOf(statement)
		val knn = knnSection(statement, target, mapped, queryVector, k, filters)
		if (paging && !knn.has('num_candidates')) knn.put('num_candidates', num(CURSOR_WINDOW))
		val from = if (offsetOf(statement) > 0) num(offsetOf(statement)) else null
		if (mmrConfig !== null) {
			if (block !== null) throw Unsupported.of("the 'boost' clause with mmr; the generated diversify retriever ranks the similarity of its kNN retriever")
			val window = if (written(mmrConfig, 'candidates')) Math.max(mmrConfig.candidates, k) else Math.max(10 * k, 10)
			if (!knn.has('num_candidates')) knn.put('num_candidates', num(candidates(window, k)))
			return obj('retriever', obj('diversify', obj('type', text('mmr'), 'field', text(mapped.name), 'query_vector', queryVector, 'lambda', num(mmrConfig.diversityLambda),
				'size', num(k), 'rank_window_size', num(window), 'retriever', obj('knn', knn))), 'size', num(size), 'from', from)
		}
		if (block !== null || paging || groupOf(statement, target) !== null) return obj('query', boostQuery(target, obj('knn', knn), block), 'size', num(size), 'from', from)
		obj('knn', knn, 'size', num(size), 'from', from)
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
	def private Entries cursorOrder(EObject statement, ElasticsearchIndex target, Entries requestBody) {
		if (written(statement, 'cursor')) requestBody.put('sort', arr(obj('_score', obj('order', text('desc'))), obj(target.keyField.exact, obj('order', text('asc')))))
		requestBody
	}

	/** The cursor of a search, for the runtime's search_after (the key of the last hit of the previous page). */
	def private Code.Call paged(Code.Call search, EObject statement, ElasticsearchIndex target) {
		if (written(statement, 'cursor')) search.args.put('after', cursorValue(target.keyField, feature(statement, 'cursor') as String))
		search
	}

	/**
	 * The kNN section: k, candidates (num_candidates from the search parameters, else the index's search-candidates, else the
	 * server default of 1.5 k), the pre-filter and the oversampling of a quantized index.
	 */
	def private Entries knnSection(EObject statement, ElasticsearchIndex target, ElasticsearchIndex.Field mapped, Expr queryVector, int k, List<Expr> filters) {
		val searchParams = feature(statement, 'params') as SearchParams
		val collection = ModelUtil.targetCollection(statement)
		val declared = collection.fields.filter(VectorField).findFirst[v | target.field(ModelUtil.fieldName(v)).name == mapped.name]
		val vectorIndex = if (declared === null) null else schema.indexOf(collection, declared)
		var Integer pool = if (vectorIndex !== null && written(vectorIndex, 'searchCandidates')) vectorIndex.searchCandidates else null
		var Entries rescore = null
		if (searchParams !== null) {
			if (written(searchParams, 'targetRecall')) throw Unsupported.of('target-recall: num_candidates and the oversampling of quantized indexes are effort settings, with no declarable recall target')
			if (written(searchParams, 'probes')) throw Unsupported.of('probes; Elasticsearch probes clusters only in its IVF index (bbq_disk, visit_percentage), which needs an enterprise license')
			if (written(searchParams, 'scanBudget')) throw Unsupported.of('scan-budget; an HNSW search in Elasticsearch takes num_candidates and no bound on visited vectors')
			for (key : ModelUtil.extParams(searchParams, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
			if (written(searchParams, 'candidatePool')) pool = searchParams.candidatePool
			if (written(searchParams, 'oversampling')) rescore = obj('oversample', num(searchParams.oversampling))
			if (searchParams.rescore && rescore === null) notes += 'rescore on SearchParams, which the index_options of the field decide'
		}
		obj('field', text(mapped.name), 'query_vector', queryVector, 'k', num(k), 'num_candidates', if (pool === null) null else num(candidates(pool, k)),
			'filter', if (filters.empty) null else arr(filters), 'rescore_vector', rescore)
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		rejectClauses(it, 'asOf', 'reranker')
		queryExtensions(it)
		combinations(it)
		if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause on a keyword search; the diversify retriever compares the hits' dense vectors with a query vector, which a text query has not")
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
	def private Entries keywordQuery(ElasticsearchIndex target, List<SearchFieldRef> refs, QueryInput queryInput, String type, String operator,
		Integer minimum, String fuzzinessValue, Integer slopValue) {
		val words = keywordText(queryInput)
		val List<String> fields = newArrayList
		if (refs.empty) {
			for (mapped : target.fields) if (mapped.fullText) fields += mapped.name
		} else {
			for (ref : refs) {
				val mapped = target.field(ModelUtil.fieldName(ref.field))
				if (mapped.vector !== null) throw Unsupported.of('keyword search on vector field ' + mapped.name)
				fields += mapped.name + (if (written(ref, 'boost')) '^' + ElasticsearchFilter.number(ref.boost) else '')
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
	def private Entries boostQuery(ElasticsearchIndex target, Entries inner, BoostBlock block) {
		if (block === null) return inner
		val List<Expr> functions = newArrayList
		for (boostFunction : block.functions) {
			val weight = if (written(boostFunction, 'weight')) num(boostFunction.weight) else null
			val applies = if (boostFunction.condition === null) null else new ElasticsearchFilter(target).query(boostFunction.condition)
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

	def private String fieldPath(ElasticsearchIndex target, FieldPath path) {
		if (path.rawPath !== null) return ElasticsearchBodies.field(path.rawPath)
		val List<String> segments = newArrayList
		segments += target.field(ModelUtil.fieldName(path.field)).name
		segments += path.subFields.map[s | ModelUtil.fieldName(s)]
		segments.join('.')
	}

	/**
	 * Hybrid search fused in the runtime: each leg is one search request (a kNN search or a text query with the shared and the
	 * leg's filters), and the fusion function of vecDSL combines the legs' ranked hits. The rrf and linear retrievers
	 * would fuse on the server but need an enterprise license.
	 */
	def dispatch List<Stmt> body(HybridSearch it) {
		rejectClauses(it, 'asOf', 'reranker')
		if (written(it, 'boost')) throw Unsupported.of("the 'boost' clause on a hybrid search; " + PENDING + ' (boost functions over the fused records)')
		queryExtensions(it)
		combinations(it)
		if (written(it, 'groupBy') && (written(it, 'sortClauses') || written(it, 'mmr'))) {
			throw Unsupported.of("the 'group-by' clause with sort or mmr on a hybrid search; groups are ordered by their best fused hit")
		}
		if (sortClauses.exists[c | c.byRelevance]) throw Unsupported.of('sort by relevance on a hybrid search; the server sorts the fused page by field values, and the fused order is the relevance order')
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val target = schema.index(collection)
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val defaultLimit = topK + offsetOf(it)
		val List<Expr> parts = newArrayList
		var position = 0
		for (leg : legs) {
			val legLimit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else defaultLimit
			val List<Expr> legFilters = newArrayList
			legFilters += clauses(it, collection, target, filter, null)
			val legFilter = feature(leg, 'filter') as FilterBlock
			if (legFilter !== null) legFilters += new ElasticsearchFilter(target).query(legFilter.condition)
			for (key : ModelUtil.extParams(leg, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a hybrid leg')
			val Entries legBody = switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (a score condition on the leg in the runtime)')
					val legField = vectorFieldOf(collection, leg.vectorField)
					val mapped = target.field(ModelUtil.fieldName(legField))
					if (mapped.vector.sparse) throw Unsupported.of('a sparse vector leg; ' + PENDING + ' (a sparse_vector query as a leg)')
					if (written(leg, 'metric') && leg.metric.literal != mapped.vector.metric || leg.exact) throw Unsupported.of('an exact or re-metered vector leg; ' + PENDING + ' (a script_score query as a leg)')
					if (leg.params !== null) schema.note(leg.params, 'filterStrategy', 'exhaustive')
					val legInput = leg.input ?: input
					if (legInput === null || legInput.kind == EQueryInputMode.ID) throw Unsupported.of('a vector leg without a vector input')
					obj('knn', obj('field', text(mapped.name), 'query_vector', queryVectorOf(legInput, mapped), 'k', num(legLimit),
						'num_candidates', if (leg.params !== null && written(leg.params, 'candidatePool')) num(candidates(leg.params.candidatePool, legLimit)) else null,
						'filter', if (legFilters.empty) null else arr(legFilters)), 'size', num(legLimit))
				}
				KeywordLeg: {
					val textQuery = keywordQuery(target, leg.searchFields, leg.input ?: input, if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match',
						if (written(leg, 'matchOperator')) leg.matchOperator.literal else null, if (written(leg, 'minShouldMatch')) leg.minShouldMatch else null, null, null)
					obj('query', obj('bool', obj('must', arr(textQuery), 'filter', if (legFilters.empty) null else arr(legFilters))), 'size', num(legLimit))
				}
			}
			val legForm = switch leg {
				VectorLeg: scoreForm(target.field(ModelUtil.fieldName(vectorFieldOf(collection, leg.vectorField))).vector, false)
				default: relevance()
			}
			parts += obj('body', legShape(it, collection, target, legBody), 'score', legForm, 'weight', num(weights.get(position)))
			position = position + 1
		}
		val group = groupOf(it, target)
		val fused = call('hybrid', 'index', text(target.name), 'legs', arr(parts), 'fusion', text(fusionKind),
			'k', num(if (written(it, 'fusionConstant')) fusionConstant else 60), 'size', num(topK), 'offset', num(offsetOf(it)), 'key', text(target.key),
			'bounds', boundsOf(bounds), 'group', if (group === null) null else text(group), 'group_size', if (written(it, 'groupSize')) num(groupSize) else null,
			'mmr', if (mmr === null) null else fusedMmr(it, target), 'sort', if (sortClauses.empty) null else arr(sortClauses.map[c | sortEntry(target, c, true) as Expr].toList),
			'total', if (withTotal) new Code.Flag(true) else null, 'aggs', if (aggregations.empty) null else aggs(target, aggregations))
		#[new Code.Return(paged(fused, it, target))]
	}

	/**
	 * The _source, explain and highlight of a hybrid leg, and the group field as a doc value; the total, sort, aggregations, groups,
	 * cursor and mmr apply to the fused records in the runtime.
	 */
	def private Entries legShape(HybridSearch search, VectorCollection collection, ElasticsearchIndex target, Entries legBody) {
		val shaped = shape(search, collection, target, legBody, search.output, search.excluded, search.withVectors, false)
		val group = groupOf(search, target)
		if (group !== null) shaped.put('docvalue_fields', arr(text(group)))
		shaped
	}

	/** MMR over the fused records: the diversity field (named, else the collection's only vector field), its similarity and the window. */
	def private Entries fusedMmr(HybridSearch search, ElasticsearchIndex target) {
		val diversity = target.field(ModelUtil.fieldName(vectorFieldOf(search.collection, search.mmr.vectorField)))
		if (diversity.vector.sparse) throw Unsupported.of("the 'mmr' clause on a sparse field; maximal marginal relevance compares dense vectors")
		val count = search.topK + offsetOf(search)
		obj('field', text(diversity.name), 'kind', text(ElasticsearchBodies.exactKind(diversity.vector.metric, diversity.vector.element)),
			'lambda', num(search.mmr.diversityLambda), 'window', num(if (written(search.mmr, 'candidates')) Math.max(search.mmr.candidates, count) else Math.max(10 * count, 10)))
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
		if (!highlighted.empty) throw Unsupported.of("the 'highlight' clause on a recommendation; highlighting marks the terms of a text query, which a recommendation has not")
		queryExtensions(it)
		combinations(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val target = schema.index(collection)
		val lookup = schema.index(lookupCollection ?: collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		if (mapped.vector.sparse) throw Unsupported.of('recommendations on a sparse field; the average-vector strategy averages dense vectors')
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
		#[new Code.Return(paged(call('recommend', 'index', text(target.name), 'lookup', text(lookup.name), 'field', text(mapped.name), 'key', text(target.key),
			'body', requestBody, 'score', if (boost !== null) relevance() else scoreForm(mapped.vector, nativeScore), 'positive_ids', if (positiveIds.empty) null else arr(positiveIds),
			'positive_vectors', if (positiveVectors.empty) null else arr(positiveVectors), 'negative_ids', if (negativeIds.empty) null else arr(negativeIds),
			'negative_vectors', if (negativeVectors.empty) null else arr(negativeVectors), 'bounds', boundsOf(bounds), 'group', if (group === null) null else text(group)), it, target))]
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
		#[new Code.Return(call('fetch', 'index', text(target.name), 'values', Code.items(ids.map[i | Literals.of(i)].toList), 'key', text(target.key),
			'fields', if (output === null || output.allFields) null else arr(fields), 'vectors', if (withVectors) new Code.Flag(true) else null,
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

	def private Expr cursorValue(ElasticsearchIndex.Field keyField, String cursorText) {
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

	def private Entries sortEntry(ElasticsearchIndex target, SortClause clause, boolean scored) {
		val order = text(if (clause.direction == ESortDirection.DESC) 'desc' else 'asc')
		if (clause.byRelevance) {
			if (!scored) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			return obj('_score', obj('order', order))
		}
		val path = clause.field
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('sorting by a vector field')
		val name = if (path.rawPath !== null) ElasticsearchBodies.field(path.rawPath) else {
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
		if (mapped.vector.sparse) throw Unsupported.of('a distance matrix of sparse vectors; the kNN search of the runtime compares dense vectors')
		#[new Code.Return(call('distance_matrix', 'index', text(target.name), 'field', text(mapped.name), 'key', text(target.key),
			'sample', num(if (written(it, 'sampleSize')) sampleSize else 10), 'limit', num(if (written(it, 'matrixLimit')) matrixLimit else 3),
			'score', scoreForm(mapped.vector, false), 'query', boolOf(clauses(it, collection, target, filter, null))))]
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

	def private Code.Call searchCall(ElasticsearchIndex target, Entries requestBody, Entries form, ScoreBounds scoreBounds, String group) {
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
	def private List<Expr> clauses(EObject statement, VectorCollection collection, ElasticsearchIndex target, FilterBlock block, Expr idValues) {
		val List<Expr> result = newArrayList
		if (idValues !== null) result += obj('ids', obj('values', idValues))
		if (block !== null) result += new ElasticsearchFilter(target).query(block.condition)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result += ElasticsearchSchema.term(target.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result += ElasticsearchSchema.term(target.namespace, text(namespace as String))
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
			return arr(values.map[v | text(if (v instanceof StringLit) (v as StringLit).value else ElasticsearchFilter.number((v as NumberLit).value)) as Expr].toList)
		}
		call('ids', 'values', Code.items(values.map[v | Literals.of(v)].toList))
	}

	/** The _source of the hits: the key and the output fields, vectors only with with-vectors (or named in the output). */
	def private Entries shape(EObject statement, VectorCollection collection, ElasticsearchIndex target, Entries requestBody, OutputSpec outputSpec,
		List<Field> excluded, boolean vectors) {
		shape(statement, collection, target, requestBody, outputSpec, excluded, vectors, true)
	}

	/** The shape of one request; without the result clauses (total, sort, aggregations, groups) for the legs of a hybrid search. */
	def private Entries shape(EObject statement, VectorCollection collection, ElasticsearchIndex target, Entries requestBody, OutputSpec outputSpec,
		List<Field> excluded, boolean vectors, boolean results) {
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
		val excludes = excluded.map[f | text(target.field(ModelUtil.fieldName(f)).name) as Expr].toList
		requestBody.put('_source', obj('includes', if (includes.empty) null else arr(includes), 'excludes', if (excludes.empty) null else arr(excludes),
			'exclude_vectors', new Code.Flag(!returnVectors)))
		if (results && written(statement, 'withTotal')) requestBody.put('track_total_hits', new Code.Flag(true))
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
		if (!results) {
			extensionBody(statement, requestBody)
			return requestBody
		}
		val sortClauses = (feature(statement, 'sortClauses') ?: newArrayList) as List<SortClause>
		if (!sortClauses.empty && !requestBody.has('sort')) requestBody.put('sort', arr(sortClauses.map[c | sortEntry(target, c, true) as Expr].toList))
		// A field sort makes Elasticsearch return _score null unless track_scores is set (search API, track_scores); the score is still part of the result.
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

	/** The collapse field of a grouped search: the exact form of the group-by field. */
	def private String groupOf(EObject statement, ElasticsearchIndex target) {
		val path = feature(statement, 'groupBy') as FieldPath
		if (path === null) return null
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('grouping by a vector field')
		if (path.rawPath !== null || !path.subFields.empty) return fieldPath(target, path)
		target.field(ModelUtil.fieldName(path.field)).exact
	}

	/** Aggregations by name (the alias, else kind_field): metrics, terms, histograms, ranges and percentiles. */
	def private Entries aggs(ElasticsearchIndex target, List<AggregationClause> aggregationClauses) {
		val Entries result = Code.entries
		for (clause : aggregationClauses) {
			val path = if (clause.field.rawPath !== null) ElasticsearchBodies.field(clause.field.rawPath) else {
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
				default: throw Unsupported.of('range aggregations; vecDSL gives no ranges, which the Elasticsearch range aggregation needs')
			}
			result.put(clause.alias ?: clause.kind.literal + '_' + path.replace('.', '_'), definition)
		}
		result
	}

	/** Search body parameters from an 'extension elasticsearch' block: terminate_after, min_score, track_scores, timeout. */
	def private void extensionBody(EObject host, Entries requestBody) {
		for (entry : ModelUtil.extParams(host, ElasticsearchGenerator.VENDOR).entrySet) {
			if (!SEARCH_KEYS.contains(entry.key)) throw Unsupported.of('extension parameter ' + entry.key + '; Elasticsearch search body parameters the generator passes are ' + SEARCH_KEYS.join(', '))
			if (entry.value instanceof ParamRef || entry.value instanceof org.ftn.vecDSL.vecDSL.EnvRef) throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a literal')
			requestBody.put(entry.key, Literals.of(entry.value))
		}
	}

	/** Rejects clauses Elasticsearch lacks, with their reason, and clauses not generated yet. */
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

	/** 'extension elasticsearch' blocks of queries carry search body parameters; extension items of other kinds are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == ElasticsearchGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on Elasticsearch')
				}
			}
		}
	}

	/** The query vector: a vector literal or parameter (bit vectors as signed bytes), or for id input the stored vector (runtime). */
	def private Expr queryVectorOf(QueryInput queryInput, ElasticsearchIndex.Field mapped) {
		switch queryInput.kind {
			case VECTOR: {
				if (mapped.vector.sparse) throw Unsupported.of('vector input on sparse field ' + mapped.name)
				convert(queryInput.value, mapped)
			}
			case ID: new Code.Nil
			case SPARSE_VECTOR: throw Unsupported.of('sparse_vector input on dense field ' + mapped.name)
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; Elasticsearch compares one vector per dense_vector field (rank_vectors needs an enterprise license)')
			default: throw Unsupported.of(queryInput.kind.literal + ' input' + NO_MODELS)
		}
	}

	def private Expr keywordText(QueryInput queryInput) {
		if (queryInput === null || queryInput.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(queryInput.value instanceof StringLit || queryInput.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(queryInput.value)
	}

	def private Expr recordsOf(RecordSource recordSource, ElasticsearchIndex target) {
		switch recordSource {
			FileSource: call('read_records', 'location', text(recordSource.location))
			InlineRecords: Code.items(recordSource.records.map[r | recordOf(r, target) as Expr].toList)
			ParamSource: Literals.of(recordSource.param)
		}
	}

	def private Entries recordOf(RecordLiteral recordLiteral, ElasticsearchIndex target) {
		val Entries result = Code.entries
		for (entry : recordLiteral.entries) {
			val mapped = target.field(ModelUtil.fieldName(entry.field))
			result.put(mapped.name, convert(entry.value, mapped))
		}
		result
	}

	/**
	 * A literal in the form Elasticsearch reads for its field: sparse vectors as objects of token (the dimension index as a
	 * string) to strictly positive weight, bit vectors as arrays of signed bytes, everything else as written.
	 */
	def private Expr convert(Literal literal, ElasticsearchIndex.Field mapped) {
		if (mapped.vector === null) return Literals.of(literal)
		if (mapped.vector.sparse && literal instanceof SparseLit) {
			val Entries weights = Code.entries
			for (entry : (literal as SparseLit).entries) {
				if (entry.weight <= 0) throw Unsupported.of('a sparse weight of ' + entry.weight + '; sparse_vector fields take strictly positive weights (SparseVectorFieldMapper)')
				weights.put(String.valueOf(entry.index), num(entry.weight))
			}
			return weights
		}
		if (mapped.vector.element == 'bit') {
			if (literal instanceof ListLit) {
				val bits = (literal as ListLit).items.map[b | if (b instanceof NumberLit && (b as NumberLit).value != 0) 1 else 0].toList
				// Signed bytes, most significant bit first. Not a string: Elasticsearch 9 reads a string vector in a document as
				// base64 but a string query_vector as hex, so hex documents were stored as other bits (verified on 9.5.6).
				val List<Expr> octets = newArrayList
				var position = 0
				while (position < bits.size) {
					var octet = 0
					for (bit : bits.subList(position, Math.min(bits.size, position + 8))) octet = octet * 2 + bit
					octets += num(if (octet > 127) octet - 256 else octet)
					position = position + 8
				}
				return arr(octets)
			}
			if (literal instanceof ParamRef) throw Unsupported.of('a parameter for bit vector field ' + mapped.name + '; the generator writes bit vectors from literals as signed bytes')
		}
		Literals.of(literal)
	}
}
