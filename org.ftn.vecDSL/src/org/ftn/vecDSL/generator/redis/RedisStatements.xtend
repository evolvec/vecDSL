package org.ftn.vecDSL.generator.redis

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
import org.ftn.vecDSL.generator.redis.RedisIndex.Attr
import org.ftn.vecDSL.generator.redis.RedisIndex.Index
import org.ftn.vecDSL.generator.redis.RedisQuery.Not
import org.ftn.vecDSL.generator.redis.RedisQuery.Params
import org.ftn.vecDSL.generator.redis.RedisQuery.Test
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AddAliasOp
import org.ftn.vecDSL.vecDSL.AddFieldOp
import org.ftn.vecDSL.vecDSL.AddFunctionOp
import org.ftn.vecDSL.vecDSL.Alias
import org.ftn.vecDSL.vecDSL.Alter
import org.ftn.vecDSL.vecDSL.AlterOperation
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.AnalyzerFilter
import org.ftn.vecDSL.vecDSL.AuthConfig
import org.ftn.vecDSL.vecDSL.AwaitIndex
import org.ftn.vecDSL.vecDSL.BackupSchedule
import org.ftn.vecDSL.vecDSL.BatchQuery
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
import org.ftn.vecDSL.vecDSL.ECloneMode
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
import org.ftn.vecDSL.vecDSL.FileResource
import org.ftn.vecDSL.vecDSL.FileSource
import org.ftn.vecDSL.vecDSL.FilterBlock
import org.ftn.vecDSL.vecDSL.Flush
import org.ftn.vecDSL.vecDSL.Function
import org.ftn.vecDSL.vecDSL.GenerativeModel
import org.ftn.vecDSL.vecDSL.GenerativeSearch
import org.ftn.vecDSL.vecDSL.GetById
import org.ftn.vecDSL.vecDSL.HybridSearch
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

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.redis.RedisSchema.command
import static org.ftn.vecDSL.generator.redis.RedisSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Redis commands in the host-language-neutral code model. Command argument arrays (FT.CREATE,
 * FT.SEARCH, FT.HYBRID, FT.AGGREGATE, ACL SETUSER, ...) are built here, with values as PARAMS or separate arguments, so the three
 * host languages send identical commands; the runtimes send them, read the documents and shape the results.
 */
class RedisStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; RedisCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Redis: '
	/** Suffix of constructs Redis supports that the generator does not produce yet. */
	public static val PENDING = 'has no Redis mapping yet'
	public static val EXTERNAL = 'collections derived from external sources; Redis holds the documents it is given and reads no external files or tables'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; Redis has no per-index or per-key drop guard (ACL rules restrict commands per user, not per index)'
	public static val NO_STORAGE = 'storage settings; Redis Open Source keeps data in memory and persists it with RDB and AOF files configured on the server'
	public static val NO_TAGS = 'collection tags; a Query Engine index carries no metadata besides its definition'
	public static val NO_SNAPSHOTS = 'snapshots; SAVE and BGSAVE write one RDB file of the whole server under a fixed name (dir and dbfilename are protected configuration), and a restore needs a server restart'
	public static val SINGLE_SERVER = 'the program targets one Redis Open Source server; replication (REPLICAOF) and Redis Cluster are configured on the servers, not by a client'
	static val NO_GENERATION = 'Redis Open Source runs no generative models'
	static val NO_RERANKERS = 'Redis Open Source runs no reranking models'
	static val NO_METRICS = 'metrics configuration; Redis reports statistics through INFO and the server configuration, not through objects a client creates'
	static val NO_SCHEDULES = 'backup schedules; RDB save points (the save configuration) snapshot the whole server under one file name, not named backups'
	static val NO_CLUSTER_LIMITS = 'cluster limits; Redis has no setting that limits the number of indexes'
	static val NO_FILES = 'file resources; the Query Engine reads no dictionary or model files'
	static val NO_RELEASE = 'release; Redis keeps every document and index in memory and has no command that evicts one index'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain',
		'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'sortClauses' -> 'sort', 'aggregations' -> 'aggregate',
		'prefetches' -> 'prefetch', 'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model',
		'minShouldMatch' -> 'min-should-match', 'fuzziness' -> 'fuzziness', 'slop' -> 'slop', 'sliceIndex' -> 'slice'
	}

	/** Why Redis cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'Redis keeps only the current value of each key',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated Redis programs embed no query text' + RedisSchema.NO_MODELS,
		'minShouldMatch' -> 'the query syntax combines terms with intersection, union and optional (~) terms and has no minimum number of matching terms',
		'elementScope' -> 'a multi-value vector attribute scores a document by its nearest vector and does not report which element matched'
	}

	static val NO_TEXT = "the 'highlight' clause on a search without text; HIGHLIGHT marks the terms of a text query"

	/** Clauses Redis supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'cursor' -> 'FT.AGGREGATE WITHCURSOR or a keyset condition after the last hit',
		'explain' -> 'EXPLAINSCORE of FT.SEARCH and FT.HYBRID',
		'mmr' -> 'MMR over the candidates in the runtime',
		'highlighted' -> 'HIGHLIGHT FIELDS of FT.SEARCH',
		'sortClauses' -> 'SORTBY over the hits',
		'aggregations' -> 'FT.AGGREGATE GROUPBY with REDUCE',
		'prefetches' -> 'staged KNN queries',
		'groupBy' -> 'FT.AGGREGATE GROUPBY with REDUCE TOLIST',
		'boost' -> 'FT.AGGREGATE APPLY expressions over the scores',
		'withTotal' -> 'total_results of FT.HYBRID or a count of the union of the legs',
		'sliceIndex' -> 'a hash of the key in an FT.AGGREGATE FILTER'
	}

	static val SEARCH_SETTINGS = #{'ef_runtime' -> 'EF_RUNTIME', 'epsilon' -> 'EPSILON', 'hybrid_policy' -> 'HYBRID_POLICY',
		'batch_size' -> 'BATCH_SIZE', 'search_window_size' -> 'SEARCH_WINDOW_SIZE', 'search_buffer_capacity' -> 'SEARCH_BUFFER_CAPACITY',
		'use_search_history' -> 'USE_SEARCH_HISTORY'}

	static val ACL_CATEGORIES = #{'read' -> #['+@read'], 'write' -> #['+@write'], 'admin' -> #['+@all'], 'all' -> #['+@all'],
		'select' -> #['+@read'], 'insert' -> #['+@write'], 'update' -> #['+@write'], 'delete' -> #['+@write'], 'search' -> #['+@search']}

	val List<String> notes = newArrayList
	val RedisSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new RedisSchema(notes, script)
	}

	/** The method for one statement, followed by helper methods it calls (batch queries). */
	def List<Program.Method> methods(Method statement, String name) {
		notes.clear
		helpers.clear
		current = name
		val List<Stmt> body = try {
			body(statement)
		} catch (Unsupported e) {
			helpers.clear
			newArrayList(new Code.Fail(e.message) as Stmt)
		}
		val List<Program.Method> result = newArrayList
		result += new Program.Method(name, ModelUtil.parameterNames(statement), body, notes.stream.distinct.toList, newArrayList, false)
		result += helpers
		result
	}

	def static String functionReason(String type) {
		switch type {
			case 'bm25': 'the Query Engine indexes TEXT attributes for BM25 itself and has no function that produces sparse vectors'
			case 'custom': 'custom functions; Redis computes no derived fields when documents are written'
			default: 'Redis Open Source runs no model functions' + RedisSchema.NO_MODELS
		}
	}

	def static String collectionLimit(String type) {
		'collection limits (' + type + '); the Query Engine has fixed limits (1024 attributes per index, 128 of them TEXT) and no per-index limit settings'
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
			VectorCollection: result += schema.createCollection(concept, ifNotExists, fresh)
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
				if (concept.replication !== null || concept.sharding !== null) throw Unsupported.of('replication and sharding; ' + SINGLE_SERVER)
				for (key : ModelUtil.extParams(concept, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Database: {
				RedisIndex.id(concept.name)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Alias: {
				val index = schema.index(concept.collection)
				result += new Code.Run(command(#['FT.ALIASADD', RedisIndex.id(concept.name), index.name], if (ifNotExists) #['alias already exists'] else #[]))
			}
			VectorIndex: result += createVectorIndex(concept)
			PayloadIndex: {
				val collection = ModelUtil.targetCollection(concept) ?: (if (concept.target.field === null) null else ModelUtil.owningCollection(concept.target.field))
				if (collection === null) throw Unsupported.of('a payload index outside a collection')
				schema.checkPayloadIndex(collection, concept)
				result += require(collection)
			}
			VectorField: result += addField(member(concept, 'a field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += require(member(concept, 'a partition'))
			Tenant: result += require(member(concept, 'a tenant'))
			Namespace: result += require(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			FileResource: throw Unsupported.of(NO_FILES)
			Analyzer: {
				RedisSchema.checkAnalyzer(concept)
				result += require(member(concept, 'an analyzer'))
			}
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer')
				RedisSchema.checkFilter(concept.type.literal, analyzer)
				result += require(member(analyzer, 'an analyzer'))
			}
			FieldLimit: throw Unsupported.of('field limits; Redis does not validate document values (JSON documents have no constraints)')
			EmbeddingModel: throw Unsupported.of('embedding models' + RedisSchema.NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: throw Unsupported.of('quantization outside a vector index; the Query Engine compresses vectors through the COMPRESSION option of an SVS-VAMANA index')
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_SERVER)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_SERVER)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: result += authentication(concept, ifNotExists)
			RoleBinding: result += role(concept, ifNotExists)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). User and password come from the environment only. */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; the Redis clients connect to a server')
		}
		if (concept.apiKey !== null || concept.token !== null) throw Unsupported.of('api keys and tokens; Redis authenticates ACL users with passwords (AUTH)')
		if (concept.password instanceof StringLit) throw Unsupported.of('a password written in the script; read it from an environment variable with env(...)')
		if (concept.databaseName !== null) throw Unsupported.of('a database name on the connection; the Query Engine indexes database 0 only, and a vecDSL logical-db is a name prefix')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; Redis speaks RESP on one port')
		for (key : ModelUtil.extParams(concept, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(concept, 'timeoutMillis', 'cloud', 'region', 'project')
	}

	/** Password authentication is the connection's; roles are ACL users; other methods are not in Redis Open Source. */
	def private List<Stmt> authentication(AuthConfig auth, boolean ifNotExists) {
		if (auth.type != EAuthType.NONE && auth.type != EAuthType.USERNAME_PASSWORD) {
			if (auth.type == EAuthType.MTLS) throw Unsupported.of('mtls authentication; has no Redis mapping yet (rediss:// with client certificates, tls-auth-clients on the server)')
			throw Unsupported.of(auth.type.literal + ' authentication; Redis Open Source authenticates ACL users with passwords (token and single sign-on methods are Redis Cloud features)')
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; has no Redis mapping yet (rediss:// with client certificates)')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; Redis Open Source has no encryption at rest')
		if (auth.auditLog) throw Unsupported.of('audit logs; Redis Open Source logs no access audit trail (ACL LOG records denied commands only)')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding, ifNotExists)
		if (result.empty) result += new Code.Run(command(#['ACL', 'WHOAMI'], #[]))
		result
	}

	/** An ACL user that cannot log in (off), with command categories and the key pattern of a collection, or all keys. */
	def private List<Stmt> role(RoleBinding binding, boolean ifNotExists) {
		val name = RedisIndex.id(binding.roleName)
		val List<String> rules = newArrayList('off', 'resetkeys', 'resetchannels', '-@all')
		for (privilege : binding.privileges) {
			val categories = ACL_CATEGORIES.get(privilege.toLowerCase)
			if (categories === null) throw Unsupported.of('the privilege ' + privilege + '; the generator grants the ACL categories read, write, search and all')
			rules += categories
		}
		if (binding.collection === null) rules += '~*' else rules += '~' + schema.index(binding.collection).prefix + '*'
		val List<String> args = newArrayList('ACL', 'SETUSER', name)
		args += rules
		val setUser = new Code.Run(command(args, #[]))
		if (!ifNotExists) return #[setUser]
		#[new Code.When(runtime('exists', kw('args', texts(#['ACL', 'GETUSER', name]))), true, #[setUser])]
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
		throw Unsupported.of(what + ' outside a collection; the Query Engine adds it to an index')
	}

	/** Existence check of a collection: FT.INFO fails for a missing index. */
	def private List<Stmt> require(VectorCollection collection) {
		#[new Code.Run(command(#['FT.INFO', schema.index(collection).name], #[]))]
	}

	/** A vector index created on an existing collection: the index is created again with the new algorithm. */
	def private List<Stmt> createVectorIndex(VectorIndex vectorIndex) {
		val field = vectorIndex.field ?: EcoreUtil2.getContainerOfType(vectorIndex, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		val before = schema.index(collection)
		val name = schema.name(collection, field)
		val type = before.vector(name).vec.type
		schema.note(vectorIndex, 'online')
		val attr = new Attr(name, '$.' + name, 'VECTOR', schema.vec(field, vectorIndex, type), 1.0, false)
		val after = new Index(before.database, before.collection, before.key, before.numericKey,
			before.attrs.map[a | if (a.name == name) attr else a].toList, before.fields, before.vectors, before.tenant, before.namespace,
			before.partition, before.language, before.stopwords, before.ttl)
		schema.evolve(collection, after)
		#[new Code.Run(runtime('recreate', kw('index', text(after.name), 'create', texts(RedisIndex.definition(after)))))]
	}

	/** FT.ALTER SCHEMA ADD; existing documents with the field are indexed in the background. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val before = schema.index(collection)
		val added = schema.attributes(collection, field)
		val dim = if (field instanceof VectorField) (field as VectorField).dimension else null
		schema.evolve(collection, before.with(added, schema.name(collection, field), dim))
		if (added.empty) return require(collection)
		#[new Code.Run(command(RedisIndex.alter(before, added), #[]))]
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias can be renamed')
			val alias = concept as Alias
			val index = schema.index(alias.collection)
			return #[new Code.Run(command(#['FT.ALIASADD', RedisIndex.id(newName), index.name], #[])),
				new Code.Run(command(#['FT.ALIASDEL', RedisIndex.id(alias.name)], #[]))]
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; the generator alters collections and aliases')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) {
			val before = schema.index(collection)
			val after = before.named(RedisIndex.id(newName))
			result += new Code.Run(runtime('copy_collection', kw('prefix', text(before.prefix), 'target', text(after.prefix), 'create',
				texts(RedisIndex.definition(after)), 'move', flag(true), 'drop', text(before.name))))
			schema.evolve(collection, after)
		}
		if (result.empty) result += require(collection)
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val before = schema.index(collection)
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: {
				val name = schema.name(collection, operation.field)
				if (name == before.key) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
				val after = before.without(name)
				schema.evolve(collection, after)
				#[new Code.Run(runtime('rewrite', kw('prefix', text(before.prefix), 'delete', texts(#[name])))),
					new Code.Run(runtime('recreate', kw('index', text(after.name), 'create', texts(RedisIndex.definition(after)))))]
			}
			RenameFieldOp: {
				val name = schema.name(collection, operation.field)
				val newName = RedisIndex.id(operation.newName)
				if (name == before.key) throw Unsupported.of('renaming the primary key; the generator addresses documents by the key field')
				val after = before.renamed(name, newName)
				schema.evolve(collection, after)
				schema.rename(collection, name, newName)
				val renamed = entries
				renamed.put(name, text(newName))
				#[new Code.Run(runtime('rewrite', kw('prefix', text(before.prefix), 'rename', renamed))),
					new Code.Run(runtime('recreate', kw('index', text(after.name), 'create', texts(RedisIndex.definition(after)))))]
			}
			SetPropertyOp: setProperty(collection, before, operation)
			AddAliasOp: #[new Code.Run(command(#['FT.ALIASADD', RedisIndex.id(operation.newAlias.name), before.name], #[]))]
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	/** ttl: EXPIRE on the existing keys, and later writes of the program set it; consistency and dynamic hold already. */
	def private List<Stmt> setProperty(VectorCollection collection, Index before, SetPropertyOp operation) {
		switch operation.property {
			case TTL: {
				if (!(operation.value instanceof NumberLit)) throw Unsupported.of('a ttl that is not a number literal')
				val seconds = (operation.value as NumberLit).value as int
				schema.evolve(collection, before.expiring(seconds))
				#[new Code.Run(runtime('expire', kw('prefix', text(before.prefix), 'seconds', num(seconds))))]
			}
			case DESCRIPTION: {
				notes += 'description on SetPropertyOp, which a Query Engine index does not store'
				require(collection)
			}
			case CONSISTENCY,
			case DYNAMIC: require(collection)
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val index = schema.index(concept)
				schema.evolve(concept, null)
				#[new Code.Run(runtime('drop_collection', kw('index', text(index.name), 'prefix', text(index.prefix), 'if_exists', flag(ifExists))))]
			}
			Database: {
				RedisIndex.id(concept.name)
				val List<Stmt> result = newArrayList
				for (collection : concept.collections) {
					val index = schema.index(collection)
					result += new Code.Run(runtime('drop_collection', kw('index', text(index.name), 'prefix', text(index.prefix), 'if_exists', flag(ifExists))))
				}
				if (result.empty) throw Unsupported.of('dropping a logical-db without collections; a logical-db is a name prefix of its collections')
				result
			}
			Tenant: dropSlice(member(concept, 'a tenant'), schema.index(member(concept, 'a tenant')).tenant, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), schema.index(member(concept, 'a namespace')).namespace, concept.key)
			Partition: dropSlice(member(concept, 'a partition'), schema.index(member(concept, 'a partition')).partition, concept.name)
			PayloadField: alter(member(concept, 'a field'), dropOp(concept))
			VectorField: alter(member(concept, 'a field'), dropOp(concept))
			Alias: #[new Code.Run(command(#['FT.ALIASDEL', RedisIndex.id(concept.name)], if (ifExists) #['alias does not exist', 'unknown index name'] else #[]))]
			VectorIndex: dropVectorIndex(concept)
			PayloadIndex: throw Unsupported.of('dropping a payload index; every declared field is an attribute of the collection\'s index, which FT.ALTER cannot remove (drop the field)')
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; the compression belongs to its SVS-VAMANA index (drop the index)')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Redis object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; the program drops indexes and keys, not the server')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE_SERVER)
			Analyzer: throw Unsupported.of('dropping an analyzer; the language and stop words belong to the index (drop the collection)')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; the index applies the filters of its language')
			FieldLimit: throw Unsupported.of('field limits; Redis does not validate document values (JSON documents have no constraints)')
			FileResource: throw Unsupported.of(NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + RedisSchema.NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_SERVER)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_SERVER)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('dropping authentication; password authentication is server configuration (requirepass and ACL files)')
			RoleBinding: {
				val name = RedisIndex.id(concept.roleName)
				#[new Code.Run(command(#['ACL', 'DELUSER', name], #[]))]
			}
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	def private DropFieldOp dropOp(Field field) {
		val operation = org.ftn.vecDSL.vecDSL.VecDSLFactory.eINSTANCE.createDropFieldOp
		operation.field = field
		operation
	}

	/** Dropping a tenant, namespace or partition deletes its documents. */
	def private List<Stmt> dropSlice(VectorCollection collection, String attribute, String value) {
		val index = schema.index(collection)
		val bound = new Params
		val where = RedisQuery.where(new RedisFilter(schema, collection).slice(attribute, #[text(value)]).parts, bound)
		#[new Code.Return(runtime('delete', kw('prefix', text(index.prefix), 'search', RedisQuery.search(RedisQuery.keys(index, where, null), bound, index))))]
	}

	/** Dropping a vector index leaves the field searchable exactly (FLAT): the index is created again. */
	def private List<Stmt> dropVectorIndex(VectorIndex vectorIndex) {
		val field = vectorIndex.field ?: EcoreUtil2.getContainerOfType(vectorIndex, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		val before = schema.index(collection)
		val name = schema.name(collection, field)
		val old = before.vector(name).vec
		val flat = new RedisIndex.Vec('FLAT', old.type, old.dim, old.metric, newLinkedHashMap)
		val after = new Index(before.database, before.collection, before.key, before.numericKey,
			before.attrs.map[a | if (a.name == name) new Attr(name, a.path, 'VECTOR', flat, 1.0, false) else a].toList, before.fields,
			before.vectors, before.tenant, before.namespace, before.partition, before.language, before.stopwords, before.ttl)
		schema.evolve(collection, after)
		#[new Code.Run(runtime('recreate', kw('index', text(after.name), 'create', texts(RedisIndex.definition(after)))))]
	}

	/** An alias swap points the alias at the new collection's index (FT.ALIASUPDATE moves it atomically). */
	def dispatch List<Stmt> body(SwapAlias it) {
		val index = schema.index(newCollection)
		#[new Code.Run(command(#['FT.ALIASUPDATE', RedisIndex.id(alias.name), index.name], #[]))]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		#[new Code.Return(runtime('write', writeArgs(it, collection, source, if (policy == 'update') 'replace' else policy,
			if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count', if (written(it, 'ttlSeconds')) ttlSeconds else null)))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val args = writeArgs(it, collection, source, 'replace', 'upsert_count', if (written(it, 'ttlSeconds')) ttlSeconds else null)
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		#[new Code.Return(runtime('write', args))]
	}

	/** The arguments of a write: prefix, key field, records, conflict policy, counter, vector dimensions, slices, ttl and wait. */
	def private Entries writeArgs(EObject statement, VectorCollection collection, RecordSource source, String policy, String counter, Integer ttl) {
		val index = schema.index(collection)
		val args = kw('prefix', text(index.prefix), 'key', text(index.key), 'records', rows(source, collection), 'policy', text(policy),
			'counter', text(counter))
		val dims = entries
		index.vectors.forEach[name, dim | dims.put(name, num(dim))]
		if (!index.vectors.empty) args.put('vectors', dims)
		val slices = slices(statement, collection)
		if (!slices.entries.empty) args.put('slices', slices)
		val seconds = ttl ?: index.ttl
		if (seconds !== null) args.put('ttl', num(seconds))
		if (wait(statement)) args.put('wait', flag(true))
		args
	}

	def private boolean wait(EObject statement) {
		written(statement, 'waitForPersistence') && feature(statement, 'waitForPersistence') == Boolean.TRUE
	}

	/** Hidden tag values for tenant, namespace and partition of a write; partitioned collections default to the default partition. */
	def private Entries slices(EObject statement, VectorCollection collection) {
		val index = schema.index(collection)
		val result = entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.put(index.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.put(index.namespace, text(namespace))
		val partition = feature(statement, 'partition') as Partition
		if (partition !== null && index.partition === null) throw Unsupported.of('a partition of a collection declared without partitions')
		if (index.partition !== null) {
			result.put(index.partition, text(partition?.name ?: collection.defaultPartition?.name
				?: ModelUtil.partitions(collection).findFirst[isDefault]?.name ?: RedisIndex.DEFAULT_PARTITION))
		}
		result
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Redis keys carry no version number, and the generator adds no version field (WATCH protects a write only within one client\'s read-modify-write)')
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val index = schema.index(collection)
		val args = kw('prefix', text(index.prefix), 'key', text(index.key))
		if (source !== null) {
			if (values !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty) {
				throw Unsupported.of("update from records with 'set', remove-fields, array-append or array-remove; each record carries its own values")
			}
			args.put('records', rows(source, collection))
		} else {
			val set = entries
			val append = entries
			val remove = entries
			if (values !== null) {
				for (entry : values.entries) {
					val name = schema.name(collection, entry.field)
					if (name == index.key) throw Unsupported.of('updating the primary key; the key names the document')
					if (appendFields.contains(entry.field)) {
						arrayField(collection, entry.field)
						append.put(name, listOf(entry.value))
					} else if (arrayRemoveFields.contains(entry.field)) {
						arrayField(collection, entry.field)
						remove.put(name, listOf(entry.value))
					} else {
						set.put(name, Literals.of(entry.value))
					}
				}
			}
			for (field : appendFields + arrayRemoveFields) {
				if (!append.has(schema.name(collection, field)) && !remove.has(schema.name(collection, field))) {
					throw Unsupported.of('array-append or array-remove on ' + schema.name(collection, field) + " without items in 'set'")
				}
			}
			val List<String> removed = newArrayList
			for (field : removedFields) {
				val name = schema.name(collection, field)
				if (name == index.key) throw Unsupported.of('removing the primary key')
				removed += name
			}
			if (set.entries.empty && append.entries.empty && remove.entries.empty && removed.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
			if (!set.entries.empty) args.put('set', set)
			if (!append.entries.empty) args.put('append', append)
			if (!remove.entries.empty) args.put('remove', remove)
			if (!removed.empty) args.put('delete', texts(removed))
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) args.put('replace', replaceSpec(collection))
		val restriction = filterOf(it, collection, filter)
		if (!ids.empty) {
			if (!restriction.parts.empty) throw Unsupported.of('an update by ids with a filter; has no Redis mapping yet (the filter checked on the documents of the ids)')
			args.put('ids', items(ids.map[Literals.of(it)]))
		} else if (!restriction.parts.empty) {
			val bound = new Params
			args.put('search', RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts, bound), null), bound, index))
		} else if (source === null) {
			throw Unsupported.of('an update without ids or a filter')
		}
		if (wait(it)) args.put('wait', flag(true))
		#[new Code.Return(runtime('update', args))]
	}

	/** Mode replace keeps the key, the vectors and the slices and removes every other field the update does not set. */
	def private Entries replaceSpec(VectorCollection collection) {
		val index = schema.index(collection)
		val List<String> keep = newArrayList(index.key)
		keep += index.vectors.keySet
		keep += #[index.tenant, index.namespace, index.partition].filterNull
		kw('keep', texts(keep))
	}

	def private Expr listOf(Literal value) {
		val converted = Literals.of(value)
		if (converted instanceof Code.Items || value instanceof ParamRef) converted else items(#[converted])
	}

	def private void arrayField(VectorCollection collection, Field field) {
		if (!(field instanceof PayloadField) || (field as PayloadField).type.literal != 'array') {
			throw Unsupported.of('array-append or array-remove on ' + schema.name(collection, field) + ', which is not an array field')
		}
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val index = schema.index(collection)
		val args = kw('prefix', text(index.prefix))
		val restriction = filterOf(it, collection, filter)
		if (!ids.empty) {
			if (!restriction.parts.empty || written(it, 'deleteLimit')) throw Unsupported.of('a delete by ids with a filter or limit; has no Redis mapping yet (the filter checked on the documents of the ids)')
			args.put('ids', items(ids.map[Literals.of(it)]))
		} else if (!restriction.parts.empty || written(it, 'deleteLimit')) {
			val bound = new Params
			args.put('search', RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts, bound), null), bound, index))
			if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		} else if (!deleteAll) {
			throw Unsupported.of("a delete without ids, filter or 'all'")
		}
		if (wait(it)) args.put('wait', flag(true))
		#[new Code.Return(runtime('delete', args))]
	}

	/** A local JSON or JSON Lines file written in atomic batches. */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Redis has no ingestion pipeline (clients write the documents)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; Redis reads no object stores (clients write the documents)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV rows typed by the collection fields)')
			throw Unsupported.of('import format ' + format.literal + '; the runtime reads JSON and JSON Lines files')
		}
		schema.note(it, 'parallelism', 'async')
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val index = schema.index(collection)
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val args = kw('location', text(location), 'prefix', text(index.prefix), 'key', text(index.key),
			'policy', text(if (policy == 'update') 'replace' else policy), 'counter', text(if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count'))
		val dims = entries
		index.vectors.forEach[name, dim | dims.put(name, num(dim))]
		if (!index.vectors.empty) args.put('vectors', dims)
		val slices = slices(it, collection)
		if (!slices.entries.empty) args.put('slices', slices)
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		#[new Code.Return(runtime('import_file', args))]
	}

	/**
	 * The writes of a vecDSL transaction in one MULTI/EXEC under WATCH: every step by key, each seeing the documents the steps before
	 * it wrote. Redis runs the queued commands without interleaving and has no rollback; the runtime commits only after every step
	 * was planned, so a failing step writes nothing.
	 */
	def dispatch List<Stmt> body(Transaction it) {
		val List<Expr> steps = newArrayList
		var wait = false
		for (statement : it.methods) {
			if (statement instanceof BulkImport) throw Unsupported.of('bulk-import inside a transaction; ' + PENDING + ' (the file\'s records as one write step)')
			val inner = body(statement)
			if (inner.size != 1 || !(inner.head instanceof Code.Return)) throw Unsupported.of('this statement inside a transaction')
			val invocation = (inner.head as Code.Return).value as Code.Call
			if (!#['write', 'update', 'delete'].contains(invocation.op)) throw Unsupported.of('this statement inside a transaction; a transaction holds writes')
			if (invocation.args.has('search')) throw Unsupported.of('a filtered update or delete inside a transaction; the commands queued in MULTI answer only at EXEC, so a filter cannot choose the keys of a later step (steps select documents by id)')
			if (invocation.op == 'delete' && !invocation.args.has('ids')) throw Unsupported.of('a delete of all documents inside a transaction; steps select documents by id')
			val step = kw('op', text(invocation.op))
			for (entry : invocation.args.entries.entrySet) {
				if (entry.key == 'wait') wait = true else step.put(entry.key, entry.value)
			}
			steps += step
		}
		val args = kw('steps', items(steps))
		if (wait) args.put('wait', flag(true))
		#[new Code.Return(runtime('transaction', args))]
	}

	/** Writes are visible to the next command (the tiered HNSW index searches its flat buffer), so 'wait' asks for durability. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (key : ModelUtil.extParams(statement, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	/** A statement's filter with its tenant, namespace and partition conditions. */
	def private RedisFilter filterOf(EObject statement, VectorCollection collection, FilterBlock block) {
		val index = schema.index(collection)
		val result = new RedisFilter(schema, collection).where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.slice(index.tenant, #[text(tenant.name)])
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result.slice(index.namespace, #[text(namespace as String)])
		val single = feature(statement, 'partition') as Partition
		val partitions = feature(statement, 'partitions') as List<Partition>
		val List<Partition> named = newArrayList
		if (single !== null) named += single
		if (partitions !== null) named += partitions
		if (!named.empty) {
			if (index.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
			result.slice(index.partition, named.map[p | text(p.name) as Expr].toList)
		}
		result
	}

	// ---------------------------------------------------------------- operational

	/** Every document and index lives in memory, so a load checks that the index exists. */
	def dispatch List<Stmt> body(Load it) {
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (!partitions.empty && schema.index(collection).partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
		require(collection)
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of(NO_RELEASE)
	}

	/** WAITAOF 1 0: returns when the server's append-only file has fsynced every write so far (appendonly yes in the compose file). */
	def dispatch List<Stmt> body(Flush it) {
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val List<Stmt> result = newArrayList
		result += require(collection)
		result += new Code.Run(runtime('durable', entries))
		result
	}

	/** The Query Engine's fork garbage collector compacts indexes on its own, so a compaction checks that the index exists. */
	def dispatch List<Stmt> body(Compact it) {
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(it, 'targetSegments', 'async')
		require(collection)
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = kw('index', text(schema.index(collection).name))
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + RedisSchema.NO_MODELS)
	}

	def dispatch List<Stmt> body(Snapshot it) {
		throw Unsupported.of(NO_SNAPSHOTS)
	}

	def dispatch List<Stmt> body(Restore it) {
		throw Unsupported.of(NO_SNAPSHOTS)
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		throw Unsupported.of(NO_SNAPSHOTS)
	}

	/** COPY of every key to the target prefix, then FT.CREATE of the target's index (which indexes the copies). */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; ' + SINGLE_SERVER)
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; COPY duplicates each value (Redis has no copy-on-write keys)')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val source = schema.index(sourceCollection)
		val target = source.named(RedisIndex.id(targetName))
		#[new Code.Return(runtime('copy_collection', kw('prefix', text(source.prefix), 'target', text(target.prefix), 'create', texts(RedisIndex.definition(target)))))]
	}

	/** The index is dropped (documents kept) and created again; the new index scans the keys in the background. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (key : ModelUtil.extParams(it, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val collection = indexCollection(index)
		if (concurrently) notes += 'concurrently on RebuildIndex; the new index serves queries while it scans the keys'
		val current = schema.index(collection)
		#[new Code.Run(runtime('recreate', kw('index', text(current.name), 'create', texts(RedisIndex.definition(current)))))]
	}

	def private VectorCollection indexCollection(org.ftn.vecDSL.vecDSL.Index index) {
		val collection = switch index {
			VectorIndex: {
				val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				if (field === null) null else ModelUtil.owningCollection(field) ?: ModelUtil.targetCollection(index)
			}
			PayloadIndex: ModelUtil.targetCollection(index) ?: (if (index.target.field === null) null else ModelUtil.owningCollection(index.target.field))
		}
		if (collection === null) throw Unsupported.of('an index outside a collection')
		collection
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val index = schema.index(collection)
		if (namespace !== null) return dropSlice(collection, index.namespace ?: RedisIndex.NAMESPACE, namespace)
		#[new Code.Return(runtime('truncate', kw('prefix', text(index.prefix))))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = scope
		val args = kw('kind', text(kind.literal))
		switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) args.put('database', text(RedisIndex.id((owner as Database).name)))
				else if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name)
			}
			case DATABASES: if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
			case INDEXES,
			case ALIASES: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection; FT.ALIASLIST lists the aliases of one index')
				args.put('index', text(schema.index(owner as VectorCollection).name))
			}
			case TENANTS,
			case NAMESPACES,
			case PARTITIONS: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection')
				val index = schema.index(owner as VectorCollection)
				val attribute = switch kind { case TENANTS: index.tenant case NAMESPACES: index.namespace default: index.partition }
				if (attribute === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				args.put('index', text(index.name))
				args.put('attribute', text(attribute))
			}
			case SNAPSHOTS: throw Unsupported.of(NO_SNAPSHOTS)
		}
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('names', args))]
	}

	/** Polls FT.INFO until the background scan has indexed every existing key. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state')
		val args = kw('index', text(schema.index(indexCollection(index)).name))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		if (!highlighted.empty) throw Unsupported.of(NO_TEXT)
		reject(it, 'elementScope', 'asOf', 'explain', 'reranker', 'queryModel', 'cursor', 'mmr', 'highlighted', 'sortClauses',
			'aggregations', 'prefetches', 'groupBy', 'boost')
		queryExtensions(it)
		val index = schema.index(collection)
		val field = vectorFieldOf(collection, vectorField)
		val attr = index.vector(schema.name(collection, field))
		checkMetric(it, attr)
		val bound = new Params
		val blobs = entries
		val lookup = queryVector(bound, blobs, input, collection, attr)
		val conditions = filterOf(it, collection, filter).parts
		val where = RedisQuery.where(conditions, bound)
		val k = topK + offsetOf(it)
		val command = RedisQuery.knn(index, where, attr, k, knnSettings(attr, searchParams(it), exact, where != '*', it), 'vec', offsetOf(it), topK)
		timeout(command, searchParams(it))
		val args = searchArgs(command, bound, index, 'knn', collection, output, it)
		args.put('blobs', blobs)
		if (lookup !== null) args.put('lookup', lookup)
		scoring(args, it, attr)
		total(args, it, index, conditions)
		#[new Code.Return(runtime('search', args))]
	}

	def private SearchParams searchParams(EObject search) {
		feature(search, 'params') as SearchParams
	}

	def private void checkMetric(EObject search, Attr attr) {
		if (written(search, 'metric') && (feature(search, 'metric') as org.ftn.vecDSL.vecDSL.EMetric).literal != attr.vec.metric) {
			throw Unsupported.of('a metric other than the field\'s; the DISTANCE_METRIC of a vector attribute is fixed when the index is created')
		}
	}

	/** Similarity or native distance, and score bounds on the converted score. */
	def private void scoring(Entries args, EObject search, Attr attr) {
		args.put('metric', text(attr.vec.metric))
		if (written(search, 'scoreForm') && feature(search, 'scoreForm') == EScoreForm.NATIVE) args.put('native', flag(true))
		bounds(args, feature(search, 'bounds') as ScoreBounds)
	}

	def private void bounds(Entries args, ScoreBounds bounds) {
		if (bounds !== null && written(bounds, 'min')) args.put('low', num(bounds.min))
		if (bounds !== null && written(bounds, 'max')) args.put('high', num(bounds.max))
	}

	/** with-total: the number of documents the filter matches, counted separately with its own parameters. */
	def private void total(Entries args, EObject search, Index index, List<RedisQuery.Cond> conditions) {
		if (!written(search, 'withTotal')) return;
		val counted = new Params
		args.put('total', RedisQuery.search(RedisQuery.count(index, RedisQuery.where(conditions, counted)), counted, index))
	}

	def private Entries searchArgs(List<String> command, Params bound, Index index, String reply, VectorCollection collection,
		OutputSpec output, EObject search) {
		val args = RedisQuery.search(command, bound, index)
		args.put('reply', text(reply))
		args.put('key', text(index.key))
		val shape = projection(collection, output, excludedOf(search), (feature(search, 'withVectors') ?: false) as Boolean)
		args.put('output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		args
	}

	/** KNN query attributes: EF_RUNTIME (candidates), HYBRID_POLICY (filter strategy, exact) and extension settings. */
	def private Map<String, String> knnSettings(Attr attr, SearchParams bound, boolean exact, boolean filtered, EObject search) {
		val Map<String, String> result = newLinkedHashMap
		val algorithm = attr.vec.algorithm
		if (bound !== null) {
			if (written(bound, 'targetRecall')) throw Unsupported.of('target-recall: EF_RUNTIME and EPSILON are effort settings, with no declarable recall target')
			if (written(bound, 'probes')) throw Unsupported.of('probes; the Query Engine has no IVF index')
			if (written(bound, 'scanBudget')) throw Unsupported.of('scan-budget; the Query Engine has no setting that bounds the vectors a KNN query visits')
			for (key : ModelUtil.extParams(bound, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' in search parameters')
			if (written(bound, 'candidatePool')) {
				if (algorithm == 'HNSW') result.put('EF_RUNTIME', String.valueOf(bound.candidatePool))
				else if (algorithm == 'SVS-VAMANA') result.put('SEARCH_WINDOW_SIZE', String.valueOf(bound.candidatePool))
				else notes += 'candidatePool on SearchParams, which a FLAT index does not use'
			}
			schema.note(bound, 'oversampling', 'rescore')
		}
		val strategy = if (bound !== null && written(bound, 'filterStrategy')) bound.filterStrategy.literal else 'auto'
		val exhaustive = exact || (bound !== null && bound.exhaustive)
		if (exhaustive && algorithm != 'FLAT') {
			if (!filtered) throw Unsupported.of('an exact search without a filter on a ' + algorithm + ' index; HYBRID_POLICY ADHOC_BF scores filtered documents exactly, and a FLAT index searches exactly')
			result.put('HYBRID_POLICY', 'ADHOC_BF')
		} else if (filtered && strategy == 'pre_filter') {
			result.put('HYBRID_POLICY', 'ADHOC_BF')
		} else if (filtered && (strategy == 'post_filter' || strategy == 'iterative')) {
			result.put('HYBRID_POLICY', 'BATCHES')
		}
		// ADHOC_BF scores the filtered documents by brute force: the server rejects an index effort setting next to it
		// (SEARCH_ADHOC_EF_RUNTIME_IRRELEVANT on Redis 8.10.3), so the candidate pool is noted, not sent
		if (result.get('HYBRID_POLICY') == 'ADHOC_BF') {
			val efRuntime = result.remove('EF_RUNTIME')
			val windowSize = result.remove('SEARCH_WINDOW_SIZE')
			if (efRuntime !== null || windowSize !== null) notes += 'candidatePool on SearchParams, which HYBRID_POLICY ADHOC_BF (a brute-force scan of the filtered documents) does not use'
		}
		for (entry : ModelUtil.extParams(search, RedisGenerator.VENDOR).entrySet) {
			val setting = SEARCH_SETTINGS.get(entry.key.toLowerCase)
			if (setting === null) throw Unsupported.of('extension parameter ' + entry.key + '; Redis query attributes are ' + SEARCH_SETTINGS.keySet.join(', '))
			val value = entry.value
			result.put(setting, switch value {
				StringLit: RedisIndex.id(value.value)
				NumberLit: RedisIndex.number(value.value)
				default: throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a string or number literal')
			})
		}
		result
	}

	def private void timeout(List<String> command, SearchParams bound) {
		if (bound === null || !written(bound, 'timeoutMillis')) return;
		val at = command.indexOf('DIALECT')
		command.addAll(if (at < 0) command.size else at, #['TIMEOUT', String.valueOf(bound.timeoutMillis)])
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'minShouldMatch', 'cursor', 'mmr', 'highlighted', 'sortClauses', 'aggregations',
			'groupBy', 'boost')
		queryExtensions(it)
		if (it.params !== null) schema.note(it.params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		val index = schema.index(collection)
		val bound = new Params
		val where = RedisQuery.where(filterOf(it, collection, filter).parts, bound)
		val text = keywordQuery(collection, index, searchFields, input, if (written(it, 'textQueryType')) textQueryType.literal else 'match',
			if (written(it, 'matchOperator')) matchOperator.literal else 'and', if (written(it, 'fuzziness')) fuzziness else null,
			if (written(it, 'slop')) slop else null, bound)
		val query = if (where == '*') text else where + ' ' + text
		val command = RedisQuery.keyword(index, query, offsetOf(it), topK)
		if (written(it, 'textQueryType') && textQueryType.literal == 'term') command.add(3, 'VERBATIM')
		timeout(command, it.params)
		val args = searchArgs(command, bound, index, 'scores', collection, output, it)
		bounds(args, bounds)
		if (written(it, 'withTotal')) args.put('total', RedisQuery.search(RedisQuery.count(index, query), bound, index))
		#[new Code.Return(runtime('search', args))]
	}

	/**
	 * The text part of a keyword query over the listed full-text fields (every one without a list): one group per field, with the
	 * field's boost as $weight, the groups joined with | (a document matches through any field).
	 */
	def private String keywordQuery(VectorCollection collection, Index index, List<SearchFieldRef> refs, QueryInput input, String type,
		String operator, Object fuzziness, Object slop, Params bound) {
		val List<Pair<String, Double>> fields = newArrayList
		if (refs.empty) {
			for (attr : index.attrs.filter[kind == 'TEXT']) fields += attr.name -> 1.0
		} else {
			for (ref : refs) {
				val attr = index.attr(schema.name(collection, ref.field))
				if (attr.kind != 'TEXT') throw Unsupported.of('keyword search on ' + attr.name + ', which is not a full-text field')
				fields += attr.name -> (if (written(ref, 'boost')) ref.boost else 1.0)
			}
		}
		if (fields.empty) throw Unsupported.of('keyword search in a collection without full-text fields')
		val words = keywordText(input)
		val terms = switch type {
			case 'match': {
				if (fuzziness !== null) {
					val level = fuzzLevel(fuzziness)
					bound.terms(words, 'fuzzy' + level)
				} else bound.terms(words, if (operator == 'or') 'or' else 'and')
			}
			case 'phrase': bound.terms(words, 'and')
			case 'term': bound.terms(words, 'and')
			case 'prefix': bound.terms(words, 'prefix')
			case 'phrase_prefix': bound.terms(words, 'prefixlast')
			case 'fuzzy': bound.terms(words, 'fuzzy' + (if (fuzziness === null) 1 else fuzzLevel(fuzziness)))
			case 'wildcard': "w'" + bound.bind(words) + "'"
			case 'query_string': throw Unsupported.of('text-query query_string; the generator never passes a value as query syntax (values are PARAMS), so user query strings are not parsed')
			default: throw Unsupported.of('text-query ' + type + '; the query syntax has prefix, suffix, infix, wildcard and fuzzy terms but no regular expressions')
		}
		if (fuzziness !== null && type != 'match' && type != 'fuzzy') throw Unsupported.of("the 'fuzziness' clause with text-query " + type)
		val attributes = if (type == 'phrase' || type == 'phrase_prefix') '=>{$slop: ' + (slop ?: 0) + '; $inorder: true;}'
			else if (slop !== null) throw Unsupported.of("the 'slop' clause with text-query " + type + '; slop applies to phrases')
			else ''
		val groups = fields.map[f | '@' + f.key + ':(' + terms + ')' + attributes + (if (f.value != 1.0) '=>{$weight: ' + RedisIndex.number(f.value) + ';}' else '')]
		if (groups.size == 1) groups.head else '(' + groups.join(' | ') + ')'
	}

	def private int fuzzLevel(Object fuzziness) {
		val text = String.valueOf(fuzziness)
		if (text == 'AUTO' || text == 'auto') throw Unsupported.of("fuzziness AUTO; the query syntax fixes the Levenshtein distance per term (%, %% or %%% for 1 to 3)")
		val level = Double.parseDouble(text) as int
		if (level < 1 || level > 3) throw Unsupported.of('fuzziness ' + text + '; the query syntax matches Levenshtein distances 1 to 3')
		level
	}

	def private Expr keywordText(QueryInput input) {
		if (input === null || input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(input.value instanceof StringLit || input.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(input.value)
	}

	/**
	 * Hybrid search: FT.HYBRID with COMBINE RRF when there is one keyword and one vector leg with the same window (vecDSL's rrf, ranks
	 * from 1); every other combination runs each leg as its own search and fuses in the runtime (FT.HYBRID's LINEAR fusion normalizes
	 * scores differently from vecDSL's weighted and relative_score).
	 */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'cursor', 'mmr', 'highlighted', 'sortClauses', 'aggregations', 'groupBy', 'boost', 'withTotal')
		queryExtensions(it)
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val index = schema.index(collection)
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val window = topK + offsetOf(it)
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val vectorLegs = legs.filter(VectorLeg).toList
		val keywordLegs = legs.filter(KeywordLeg).toList
		val sameWindow = legs.forall[leg | !written(leg, 'legLimit') || feature(leg, 'legLimit') as Integer == window]
		for (leg : legs) {
			for (key : ModelUtil.extParams(leg, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a hybrid leg')
			if (leg instanceof VectorLeg && (leg as VectorLeg).bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (the leg score filtered before fusion)')
		}
		val args = if (fusionKind == 'rrf' && vectorLegs.size == 1 && keywordLegs.size == 1 && sameWindow && legs.size == 2) {
			nativeHybrid(it, index, vectorLegs.head, keywordLegs.head, window, k)
		} else {
			fusedHybrid(it, index, fusionKind, weights, k, window)
		}
		bounds(args, bounds)
		#[new Code.Return(runtime(if (args.has('legs')) 'hybrid' else 'search', args))]
	}

	def private Entries nativeHybrid(HybridSearch search, Index index, VectorLeg vectorLeg, KeywordLeg keywordLeg, int window, int k) {
		val collection = search.collection
		val bound = new Params
		val blobs = entries
		val field = vectorFieldOf(collection, vectorLeg.vectorField)
		val attr = index.vector(schema.name(collection, field))
		checkMetric(vectorLeg, attr)
		val lookup = queryVector(bound, blobs, vectorLeg.input ?: search.input, collection, attr)
		val textWhere = RedisQuery.where(filterOf(search, collection, legFilter(search.filter, keywordLeg.filter)).parts, bound)
		if (written(keywordLeg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
		val text = keywordQuery(collection, index, keywordLeg.searchFields, keywordLeg.input ?: search.input,
			if (written(keywordLeg, 'textQueryType')) keywordLeg.textQueryType.literal else 'match',
			if (written(keywordLeg, 'matchOperator')) keywordLeg.matchOperator.literal else 'and', null, null, bound)
		val vectorWhere = RedisQuery.where(filterOf(search, collection, legFilter(search.filter, vectorLeg.filter)).parts, bound)
		val knn = knnSettings(attr, vectorLeg.params, vectorLeg.exact, vectorWhere != '*', search).filter[name, value | name == 'EF_RUNTIME']
		val command = RedisQuery.hybrid(index, if (textWhere == '*') text else textWhere + ' ' + text, attr, 'vec', knn,
			if (vectorWhere == '*') null else vectorWhere, window, k, offsetOf(search), search.topK)
		timeout(command, search.params)
		val args = searchArgs(command, bound, index, 'hybrid', collection, search.output, search)
		args.put('blobs', blobs)
		if (lookup !== null) args.put('lookup', lookup)
		args
	}

	def private Entries fusedHybrid(HybridSearch search, Index index, String fusionKind, List<Double> weights, int k, int window) {
		val collection = search.collection
		val List<Expr> legArgs = newArrayList
		var number = 0
		for (leg : search.legs) {
			val bound = new Params('l' + number + '_')
			val limit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else window
			val where = RedisQuery.where(filterOf(search, collection, legFilter(search.filter, feature(leg, 'filter') as FilterBlock)).parts, bound)
			val legEntries = switch leg {
				VectorLeg: {
					val field = vectorFieldOf(collection, leg.vectorField)
					val attr = index.vector(schema.name(collection, field))
					checkMetric(leg, attr)
					val blobs = entries
					val lookup = queryVector(bound, blobs, leg.input ?: search.input, collection, attr)
					val command = RedisQuery.knn(index, where, attr, limit, knnSettings(attr, leg.params, leg.exact, where != '*', search), 'l' + number + '_vec', 0, limit)
					val result = RedisQuery.search(command, bound, index)
					result.put('reply', text('knn'))
					result.put('blobs', blobs)
					result.put('metric', text(attr.vec.metric))
					if (lookup !== null) result.put('lookup', lookup)
					result
				}
				KeywordLeg: {
					if (written(leg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
					val text = keywordQuery(collection, index, leg.searchFields, leg.input ?: search.input,
						if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match',
						if (written(leg, 'matchOperator')) leg.matchOperator.literal else 'and', null, null, bound)
					val result = RedisQuery.search(RedisQuery.keyword(index, if (where == '*') text else where + ' ' + text, 0, limit), bound, index)
					result.put('reply', text('scores'))
					result
				}
			}
			legArgs += legEntries
			number = number + 1
		}
		val args = kw('legs', items(legArgs), 'fusion', text(fusionKind), 'key', text(index.key))
		val shape = projection(collection, search.output, excludedOf(search), search.withVectors)
		args.put('output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		args.put('limit', num(search.topK))
		if (offsetOf(search) > 0) args.put('offset', num(offsetOf(search)))
		if (fusionKind == 'rrf') args.put('k', num(k)) else args.put('weights', items(weights.map[w | num(w) as Expr]))
		args
	}

	/** A leg filter replaces the search's filter; both at once are not generated. */
	def private FilterBlock legFilter(FilterBlock outer, FilterBlock inner) {
		if (outer === null) return inner
		if (inner === null) return outer
		throw Unsupported.of('a filter on both the hybrid search and a leg; ' + PENDING + ' (the conjunction of both)')
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

	/** The average-vector strategy (2 avg(positive) - avg(negative)) computed in the runtime; the examples are not returned. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		if (!highlighted.empty) throw Unsupported.of(NO_TEXT)
		reject(it, 'asOf', 'explain', 'reranker', 'cursor', 'mmr', 'highlighted', 'sortClauses', 'aggregations', 'groupBy', 'boost')
		queryExtensions(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val index = schema.index(collection)
		val lookupIndex = schema.index(lookupCollection ?: collection)
		val field = vectorFieldOf(collection, vectorField)
		val name = schema.name(collection, field)
		val attr = index.vector(name)
		val bound = new Params
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) if (isVector(example)) positiveVectors += Literals.of(example) else positiveIds += Literals.of(example)
		for (example : negatives) if (isVector(example)) negativeVectors += Literals.of(example) else negativeIds += Literals.of(example)
		val restriction = filterOf(it, collection, filter)
		if (lookupCollection === null || lookupCollection === collection) {
			val keyAttr = index.attr(index.key)
			for (example : positiveIds + negativeIds) restriction.add(new Not(new Test(keyAttr, 'eq', example, null)))
		}
		val where = RedisQuery.where(restriction.parts, bound)
		bound.named('vec', items(#[]))
		val blobs = kw('vec', text(attr.vec.type))
		val lookup = kw('param', text('vec'), 'prefix', text(lookupIndex.prefix), 'path', text(name))
		if (!positiveIds.empty) lookup.put('positive_ids', items(positiveIds))
		if (!negativeIds.empty) lookup.put('negative_ids', items(negativeIds))
		if (!positiveVectors.empty) lookup.put('positive_vectors', items(positiveVectors))
		if (!negativeVectors.empty) lookup.put('negative_vectors', items(negativeVectors))
		val command = RedisQuery.knn(index, where, attr, topK + offsetOf(it), knnSettings(attr, searchParams(it), false, where != '*', it), 'vec', offsetOf(it), topK)
		timeout(command, searchParams(it))
		val args = searchArgs(command, bound, index, 'knn', collection, output, it)
		args.put('blobs', blobs)
		args.put('lookup', lookup)
		scoring(args, it, attr)
		total(args, it, index, restriction.parts)
		#[new Code.Return(runtime('search', args))]
	}

	def private boolean isVector(Literal example) {
		example instanceof ListLit || example instanceof SparseLit
			|| (example instanceof ParamRef && #['vector', 'sparse_vector'].contains((example as ParamRef).param.type.literal))
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val index = schema.index(collection)
		val shape = projection(collection, output, newArrayList, withVectors)
		val args = kw('prefix', text(index.prefix), 'key', text(index.key), 'ids', items(ids.map[Literals.of(it)]), 'output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		val slices = slicesOf(it, collection)
		if (!slices.entries.empty) args.put('slices', slices)
		#[new Code.Return(runtime('get', args))]
	}

	/** The tenant, namespace and partitions a get checks on the documents of its ids: attribute -> the values a document may hold. */
	def private Entries slicesOf(GetById statement, VectorCollection collection) {
		val index = schema.index(collection)
		val result = entries
		if (statement.tenant !== null) result.put(index.tenant, items(#[text(statement.tenant.name)]))
		if (statement.namespace !== null) result.put(index.namespace, items(#[text(statement.namespace)]))
		if (!statement.partitions.empty) {
			if (index.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
			result.put(index.partition, items(statement.partitions.map[p | text(p.name) as Expr]))
		}
		result
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val index = schema.index(collection)
		val bound = new Params
		val where = RedisQuery.where(filterOf(it, collection, filter).parts, bound)
		#[new Code.Return(runtime('count', RedisQuery.search(RedisQuery.count(index, where), bound, index)))]
	}

	/** FT.AGGREGATE sorted by the sort clauses and the key, then the documents of the page. */
	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'asOf', 'cursor', 'sliceIndex')
		queryExtensions(it)
		val index = schema.index(collection)
		val bound = new Params
		val where = RedisQuery.where(filterOf(it, collection, filter).parts, bound)
		val List<String> order = newArrayList
		for (clause : sortClauses) {
			if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			val attr = index.attr(new RedisFilter(schema, collection).attribute(clause.field))
			if (attr.kind == 'TEXT' || attr.kind == 'VECTOR' || attr.array) throw Unsupported.of('sorting by ' + attr.name + '; the generator sorts by single NUMERIC and TAG values')
			order += #['@' + attr.name, if (clause.direction == ESortDirection.DESC) 'DESC' else 'ASC']
		}
		order += #['@' + index.key, 'ASC']
		val limit = if (written(it, 'scrollLimit')) scrollLimit else null
		val command = RedisQuery.ordered(index, where, order, offsetOf(it), limit)
		val args = searchArgs(command, bound, index, 'keys', collection, output, it)
		args.entries.remove('reply')
		if (limit === null) args.put('every', flag(true))
		#[new Code.Return(runtime('scroll', args))]
	}

	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val index = schema.index(collection)
		val shape = projection(collection, output, newArrayList, false)
		val args = kw('prefix', text(index.prefix), 'key', text(index.key), 'size', num(sampleSize), 'output', texts(shape.key))
		val restriction = filterOf(it, collection, filter)
		if (!restriction.parts.empty) {
			val bound = new Params
			args.put('search', RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts, bound), null), bound, index))
		}
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		#[new Code.Return(runtime('sample', args))]
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val index = schema.index(collection)
		val field = vectorFieldOf(collection, vectorField)
		val attr = index.vector(schema.name(collection, field))
		val args = kw('prefix', text(index.prefix), 'key', text(index.key), 'field', text(attr.name), 'metric', text(attr.vec.metric),
			'size', num(if (written(it, 'sampleSize')) sampleSize else 10), 'limit', num(if (written(it, 'matrixLimit')) matrixLimit else 3))
		val restriction = filterOf(it, collection, filter)
		if (!restriction.parts.empty) {
			val bound = new Params
			args.put('search', RedisQuery.search(RedisQuery.keys(index, RedisQuery.where(restriction.parts, bound), null), bound, index))
		}
		#[new Code.Return(runtime('distance_matrix', args))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		throw Unsupported.of('generative search; ' + NO_GENERATION)
	}

	def dispatch List<Stmt> body(BatchQuery it) {
		val List<Expr> calls = newArrayList
		val owner = current
		var number = 0
		for (query : queries) {
			val name = owner + '_query_' + number
			current = name
			helpers += new Program.Method(name, ModelUtil.parameterNames(query), body(query), newArrayList, newArrayList, true)
			calls += self(name)
			number = number + 1
		}
		current = owner
		#[new Code.Return(items(calls))]
	}

	def dispatch List<Stmt> body(Method it) {
		throw Unsupported.of(NOT_MAPPED + eClass.name)
	}

	// ---------------------------------------------------------------- helpers

	def private Object feature(EObject object, String name) {
		val slot = object.eClass.getEStructuralFeature(name)
		if (slot === null) null else object.eGet(slot)
	}

	def private int offsetOf(EObject query) {
		if (written(query, 'offset')) feature(query, 'offset') as Integer else 0
	}

	def private List<Field> excludedOf(EObject query) {
		(feature(query, 'excluded') ?: newArrayList) as List<Field>
	}

	/** The searched vector field: the named one, else the collection's only vector field. */
	def private VectorField vectorFieldOf(VectorCollection collection, VectorField named) {
		if (named !== null) {
			if (!collection.fields.contains(named)) throw Unsupported.of('vector field ' + ModelUtil.fieldName(named) + ', which is not a field of collection ' + collection.name)
			return named
		}
		val fields = ModelUtil.vectorFields(collection)
		if (fields.size != 1) throw Unsupported.of("a search without 'using' in a collection with " + fields.size + ' vector fields')
		fields.head
	}

	/** Output fields (the key is always returned as id) and the vector fields returned with 'with-vectors' or named in the output. */
	def private Pair<List<String>, List<String>> projection(VectorCollection collection, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val index = schema.index(collection)
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val listed = if (output === null || output.allFields) collection.fields as List<? extends Field> else output.fields
		for (field : listed) {
			val name = schema.name(collection, field)
			if (name != index.key && !excluded.contains(field) && index.fields.contains(name)) {
				if (field instanceof VectorField) {
					if (output !== null && !output.allFields) vectors += name
				} else {
					fields += name
				}
			}
		}
		if (withVectors) {
			for (field : ModelUtil.vectorFields(collection)) {
				val name = schema.name(collection, field)
				if (!vectors.contains(name) && !excluded.contains(field) && index.fields.contains(name)) vectors += name
			}
		}
		fields -> vectors
	}

	/** Rejects clauses Redis lacks, with their reason, and clauses not generated yet. */
	def private void reject(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) {
				val clause = "the '" + (CLAUSES.get(name) ?: name) + "' clause"
				val pending = PENDING_CLAUSES.get(name)
				if (pending !== null) throw Unsupported.of(clause + '; ' + PENDING + ' (' + pending + ')')
				val reason = REASONS.get(name)
				throw Unsupported.of(if (reason === null) clause else clause + '; ' + reason)
			}
		}
	}

	/** Redis extension blocks of queries carry query attributes only; extension items of other kinds are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == RedisGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on Redis')
				}
			}
		}
	}

	/** The query vector: a PARAMS blob of the given vector, or the stored vector of a record (looked up by the runtime). */
	def private Entries queryVector(Params bound, Entries blobs, QueryInput input, VectorCollection collection, Attr attr) {
		if (input === null) throw Unsupported.of('a vector query without input')
		val name = bound.named('vec', if (input.kind == EQueryInputMode.VECTOR) Literals.of(input.value) else items(#[])).substring(1)
		blobs.put(name, text(attr.vec.type))
		switch input.kind {
			case VECTOR: null
			case ID: kw('param', text(name), 'prefix', text(schema.index(input.lookupCollection ?: collection).prefix), 'path', text(attr.name),
				'positive_ids', items(#[Literals.of(input.value)]))
			case SPARSE_VECTOR: throw Unsupported.of('sparse_vector input; the Query Engine has no sparse vector attributes')
			case TEXT: throw Unsupported.of('text input' + RedisSchema.NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; a KNN query takes one vector')
			default: throw Unsupported.of(input.kind.literal + ' input' + RedisSchema.NO_MODELS)
		}
	}

	def private Expr rows(RecordSource source, VectorCollection collection) {
		switch source {
			FileSource: runtime('read_records', kw('location', text(source.location)))
			InlineRecords: items(source.records.map[item | record(item, collection)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record, VectorCollection collection) {
		val result = entries
		for (entry : record.entries) {
			if (entry.value instanceof SparseLit) throw Unsupported.of('sparse vector values; the Query Engine has no sparse vector attributes')
			result.put(schema.name(collection, entry.field), Literals.of(entry.value))
		}
		result
	}
}
