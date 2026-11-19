package org.ftn.vecDSL.generator.mongodb

import java.util.List
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
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
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
import org.ftn.vecDSL.vecDSL.Namespace
import org.ftn.vecDSL.vecDSL.NumberLit
import org.ftn.vecDSL.vecDSL.OutputSpec
import org.ftn.vecDSL.vecDSL.ParamRef
import org.ftn.vecDSL.vecDSL.ParamSource
import org.ftn.vecDSL.vecDSL.Partition
import org.ftn.vecDSL.vecDSL.NullLit
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.QueryInput
import org.ftn.vecDSL.vecDSL.QueryMethod
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

import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to MongoDB commands and aggregation pipelines in the host-language-neutral code model. Every
 * command document, index definition, filter and pipeline is built here, so the three host languages send identical commands:
 * command runs one database command, aggregate runs a pipeline (scores in the canonical form are computed inside it),
 * write, update and delete send the write commands and wait for mongot when asked, hybrid fuses legs that $rankFusion cannot,
 * recommend computes the query vector of a recommendation, and transaction runs writes in one multi-document transaction.
 */
class MongodbStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; MongodbCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for MongoDB: '
	/** Suffix of constructs MongoDB supports that the generator does not produce yet. */
	public static val PENDING = 'has no MongoDB mapping yet'
	/** The group value a grouped search carries through the projection to its $group stage. */
	static val GROUP = 'vecdsl_group'
	/** The marker of a geo_point value that the runtimes convert to a GeoJSON point. */
	static val GEO_POINT = '$geoPoint'
	public static val NO_MODELS = '; the Atlas Local image runs mongot without the embedding block (Voyage AI API key files) that automated embedding needs, so generated programs supply their own vectors'
	public static val EXTERNAL = 'collections derived from external sources; MongoDB reads parquet, lance, iceberg or vortex files neither as collections nor through a database command (Atlas Data Federation is an Atlas service)'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; MongoDB has no per-collection guard against drop (only roles without the dropCollection action)'
	public static val NO_TAGS = 'collection tags; MongoDB collection options carry a validator, not tags (the description goes into the $jsonSchema)'
	public static val NO_FILES = 'Atlas Search reads synonyms from a collection and has no file-based word lists or dictionaries'
	public static val NO_SNAPSHOTS = 'snapshots; MongoDB backs up with mongodump, mongorestore and file system snapshots, tools outside the database commands, and cloud backups and their schedules are Atlas features'
	public static val SINGLE = 'the Atlas Local image runs one mongod as a single-node replica set; more members and shards are server deployments, not database commands'
	public static val NO_RERANK = 'MongoDB has no reranking stage in the Atlas Local deployment (Voyage AI reranking is an Atlas model API, outside the database)'
	public static val NO_GENERATION = 'MongoDB has no generative model stage'
	public static val NO_METRICS = 'metrics configuration; MongoDB metrics come from serverStatus and mongot\'s Prometheus endpoint, both configured on the servers, not per program'
	public static val NO_CLUSTER_LIMITS = 'cluster limits; MongoDB has no setting that limits the number of collections or databases'
	public static val NO_RESIDENCY = 'WiredTiger reads documents and indexes into its cache when they are accessed, and MongoDB has no command that loads or evicts a collection'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain',
		'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'sortClauses' -> 'sort', 'aggregations' -> 'aggregate',
		'prefetches' -> 'prefetch', 'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model', 'sliceIndex' -> 'slice'
	}

	/** Why MongoDB cannot express a clause. */
	static val REASONS = #{
		'asOf' -> '$vectorSearch and $search read mongot\'s current index, and snapshot reads at a past cluster time reach back only minSnapshotHistoryWindowInSeconds (300 s by default)',
		'reranker' -> NO_RERANK,
		'queryModel' -> 'generated MongoDB programs embed no query text' + NO_MODELS,
		'cursor' -> 'ranked results have no key order to continue after (searchAfter needs a sort on indexed fields, which vecDSL cursors do not give)',
		'explain' -> '$vectorSearch returns no score details (explain describes the query plan, not the score)',
		'highlighted' -> 'highlights mark the terms of a text query, which a vector search has not'
	}

	/** Clauses MongoDB supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'nestedRoot vector indexes over arrays of embedded documents', 'prefetches' -> 'prefetch stages as input pipelines'
	}

	/** Options of the aggregate command that an 'extension mongodb' block on a query may set. */
	static val AGGREGATE_KEYS = #['maxTimeMS', 'allowDiskUse', 'comment']

	val List<String> notes = newArrayList
	val MongodbSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script, String fallbackDatabase) {
		this.script = script
		this.schema = new MongodbSchema(notes, script, fallbackDatabase)
	}

	/** The method for one statement, followed by helper methods it calls (batch queries). */
	def List<Program.Method> methods(Method statement, String methodName) {
		notes.clear
		helpers.clear
		current = methodName
		val List<Stmt> statements = try {
			body(statement)
		} catch (Unsupported e) {
			helpers.clear
			newArrayList(new Code.Fail(e.message) as Stmt)
		}
		val List<Program.Method> result = newArrayList
		result += new Program.Method(methodName, ModelUtil.parameterNames(statement), statements, notes.stream.distinct.toList, newArrayList, false)
		result += helpers
		result
	}

	def static String functionReason(String type) {
		switch type {
			case 'bm25': 'Atlas Search scores string fields with BM25 directly and has no sparse vector field for BM25 weights'
			case 'text_embedding',
			case 'sparse_encoding': 'automated embedding' + NO_MODELS.replaceFirst('^; ', ': ')
			default: 'MongoDB runs no ' + type + ' functions on the server'
		}
	}

	/** Storage settings: disk is WiredTiger (the default storage engine); the other kinds are not available on this deployment. */
	def static void storage(StorageConfig storage) {
		if (storage.location !== null) throw Unsupported.of('a storage location; mongod keeps data under its dbPath, a server setting')
		if (storage.tiered) throw Unsupported.of('tiered storage; Atlas Online Archive is an Atlas service')
		switch storage.type.literal {
			case 'disk': if (storage.mmap) throw Unsupported.of('mmap storage; the MMAPv1 storage engine was removed in MongoDB 4.2')
			case 'memory': throw Unsupported.of('memory storage; the in-memory storage engine is MongoDB Enterprise only')
			case 'object_storage': throw Unsupported.of('object storage; MongoDB keeps collections in WiredTiger files on local disks')
			default: throw Unsupported.of('embedded storage; MongoDB runs as a server')
		}
	}

	/** A cluster is the deployment's replica set: replication factor 1 and no sharding. */
	def static void cluster(Cluster declared) {
		if (declared === null) return;
		if (declared.sharding !== null) throw Unsupported.of('sharding; ' + SINGLE)
		if (declared.replication !== null && declared.replication.factor != 1) throw Unsupported.of('a replication factor of ' + declared.replication.factor + '; ' + SINGLE)
		if (!declared.limits.empty) throw Unsupported.of(NO_CLUSTER_LIMITS)
		for (key : ModelUtil.extParams(declared, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	/** Partitions are values of a hidden field; partitioning by key, hash or range is sharding. */
	def static void checkPartition(Partition partition) {
		if (partition.key !== null || (written(partition, 'strategy') && partition.strategy.literal != 'list')) {
			throw Unsupported.of('partitions by key or with the ' + partition.strategy.literal + ' strategy; ' + SINGLE)
		}
		if (!partition.subPartitions.empty) throw Unsupported.of('sub-partitions; partitions are values of one hidden field')
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
				for (declared : concept.clusters) result += define(declared, ifNotExists, fresh)
				for (database : concept.databases) result += define(database, ifNotExists, fresh)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists, fresh)
			}
			Cluster: {
				cluster(concept)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Database: for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			Alias: result += view(schema.collection(concept.collection), concept.name, ifNotExists)
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
			FieldLimit: result += fieldLimit(concept)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANK)
			Quantization: {
				val owner = EcoreUtil2.getContainerOfType(concept, VectorIndex)
				if (owner === null) throw Unsupported.of('quantization outside a vector index; mongot quantizes the vector field of a vectorSearch index')
				result += updateVectorIndex(owner)
			}
			ReplicationConfig: {
				if (concept.factor != 1) throw Unsupported.of('a replication factor of ' + concept.factor + '; ' + SINGLE)
				for (key : ModelUtil.extParams(concept, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
				result += new Code.Return(call('command', 'database', text('admin'), 'command', obj('replSetGetStatus', num(1)), 'select', arr(text('set'))))
			}
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE)
			StorageConfig: {
				storage(concept)
				result += new Code.Return(call('command', 'database', text('admin'), 'command', obj('serverStatus', num(1)), 'select', arr(text('storageEngine'), text('name'))))
			}
			AuthConfig: result += authentication(concept)
			RoleBinding: result += role(concept)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of('backup schedules; ' + NO_SNAPSHOTS)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: result += collectionLimit(concept, false)
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). Passwords come from the environment only. */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; MongoDB runs as a server that the drivers reach over the wire protocol')
		}
		if (concept.token !== null) throw Unsupported.of('bearer tokens; the database authenticates SCRAM users (OIDC tokens need MongoDB Enterprise or Atlas)')
		if (concept.apiKey !== null) throw Unsupported.of('API keys; Atlas API keys authenticate the Atlas Admin API, not the database')
		if (concept.password instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; MongoDB serves the wire protocol only')
		for (key : ModelUtil.extParams(concept, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(concept, 'cloud', 'region', 'project')
	}

	/** Authentication: username_password is the connection's SCRAM user; roles are createRole commands; the rest needs Enterprise or Atlas. */
	def private List<Stmt> authentication(AuthConfig auth) {
		switch auth.type {
			case EAuthType.API_KEY: throw Unsupported.of('api_key authentication; Atlas API keys authenticate the Atlas Admin API, not the database')
			case EAuthType.TOKEN: throw Unsupported.of('token authentication; OIDC needs MongoDB Enterprise or Atlas')
			case EAuthType.MTLS: throw Unsupported.of('mtls authentication; the Atlas Local image serves without TLS, which X.509 authentication needs')
			case EAuthType.SSO: throw Unsupported.of('sso authentication; LDAP, Kerberos and OIDC need MongoDB Enterprise or Atlas')
			default: {}
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; the Atlas Local image serves without TLS')
		if (auth.auditLog) throw Unsupported.of('audit logs; auditing needs MongoDB Enterprise or Atlas')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; automatic encryption needs MongoDB Enterprise or Atlas')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding)
		if (result.empty) result += new Code.Return(call('command', 'database', text('admin'), 'command', obj('connectionStatus', num(1)), 'select', arr(text('authInfo'), text('authenticatedUsers'))))
		result
	}

	/** A role with privilege actions on a collection (or on every collection of the database); an earlier role of that name is replaced. */
	def private List<Stmt> role(RoleBinding binding) {
		val target = if (binding.collection === null) null else schema.collection(binding.collection)
		val database = target?.database ?: schema.defaultDatabase
		val resource = obj('db', text(database), 'collection', text(target?.name ?: ''))
		val roleDocument = obj('createRole', text(roleName(binding.roleName)), 'privileges', arr(obj('resource', resource, 'actions', arr(actions(binding.privileges)))),
			'roles', arr())
		#[new Code.Run(call('command', 'database', text(database), 'command', obj('dropRole', text(roleName(binding.roleName))), 'ignore', arr(text('RoleNotFound')))),
			new Code.Run(command(database, roleDocument))]
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
		throw Unsupported.of(what + ' outside a collection; MongoDB adds it to a collection')
	}

	/** A read-only alias: a view on the collection with an empty pipeline. */
	def private List<Stmt> view(MongodbCollection target, String aliasName, boolean ifNotExists) {
		val name = checkedName(aliasName, 'view')
		val creation = new Code.Run(command(target.database, obj('create', text(name), 'viewOn', text(target.name), 'pipeline', arr())))
		if (ifNotExists) #[new Code.When(call('exists', 'database', text(target.database), 'collection', text(name)), true, #[creation as Stmt]) as Stmt] else #[creation as Stmt]
	}

	/** The view that shows one tenant, namespace or partition; the collection gains the slice field when it lacks it. */
	def private List<Stmt> slice(VectorCollection collection, String kind, String value, EObject concept) {
		if (concept instanceof Partition) checkPartition(concept as Partition)
		var target = schema.collection(collection)
		val List<Stmt> result = newArrayList
		val fieldName = sliceField(target, kind)
		var sliceName = fieldName
		if (fieldName === null) {
			if (kind == 'partition') throw Unsupported.of('a partition of a collection declared without partitions; declare a partition in the collection, which adds the partition field')
			sliceName = if (kind == 'tenant') TENANT_FIELD else NAMESPACE_FIELD
			val next = target.copy
			if (kind == 'tenant') next.tenant = sliceName else next.namespace = sliceName
			schema.evolve(collection, next)
			result += redefine(next, false)
			target = next
		}
		val name = checkedName(target.name + '_' + kind + '_' + value.replaceAll('[^A-Za-z0-9_]', '_'), 'view')
		result += new Code.Run(command(target.database, obj('create', text(name), 'viewOn', text(target.name), 'pipeline',
			arr(obj('$match', obj(sliceName, obj('$eq', text(value))))))))
		result
	}

	def private String sliceField(MongodbCollection target, String kind) {
		switch kind { case 'tenant': target.tenant case 'namespace': target.namespace default: target.partition }
	}

	/** collMod with the collection's validator, then the search indexes updated to its fields (and awaited). */
	def private List<Stmt> redefine(MongodbCollection target, boolean textChanged) {
		val List<Stmt> result = newArrayList
		result += new Code.Run(command(target.database, obj('collMod', text(target.name), 'validator', target.validator, 'validationLevel', text('strict'))))
		for (index : target.vectorIndexes) {
			result += new Code.Run(command(target.database, obj('updateSearchIndex', text(target.name), 'name', text(index.name), 'definition', target.vectorDefinition(index))))
			result += schema.awaitIndex(target, index.name, AWAIT_SECONDS)
		}
		if (textChanged && target.hasText) {
			result += new Code.Run(call('ensure_search_index', 'database', text(target.database), 'collection', text(target.name), 'index', text(TEXT_INDEX),
				'type', text('search'), 'definition', target.textDefinition))
			result += schema.awaitIndex(target, TEXT_INDEX, AWAIT_SECONDS)
		}
		result
	}

	/** The search index vecdsl_text with the collection's analyzers, re-defined after an analyzer changes. */
	def private List<Stmt> reanalyze(Analyzer analyzer) {
		val collection = member(analyzer, 'an analyzer')
		val List<Analyzer> declared = newArrayList(collection.analyzers)
		if (!declared.contains(analyzer)) declared += analyzer
		val next = schema.collection(collection).copy
		next.analyzers = schema.analyzers(declared)
		schema.evolve(collection, next)
		if (!next.hasText) throw Unsupported.of('an analyzer in a collection without full-text fields; the analyzers live in the search index of the full-text fields')
		#[new Code.Run(call('ensure_search_index', 'database', text(next.database), 'collection', text(next.name), 'index', text(TEXT_INDEX), 'type', text('search'),
			'definition', next.textDefinition)) as Stmt, schema.awaitIndex(next, TEXT_INDEX, AWAIT_SECONDS)]
	}

	/** A field limit on an existing field: the field's $jsonSchema gains the keyword, collMod replaces the validator. */
	def private List<Stmt> fieldLimit(FieldLimit limit) {
		val owner = EcoreUtil2.getContainerOfType(limit, Field)
		if (owner === null) throw Unsupported.of('a field limit outside a field')
		val collection = member(owner, 'a field limit')
		val next = schema.collection(collection).copy
		next.replace(ModelUtil.fieldName(owner), schema.field(collection, owner))
		schema.evolve(collection, next)
		#[new Code.Run(command(next.database, obj('collMod', text(next.name), 'validator', next.validator, 'validationLevel', text('strict'))))]
	}

	def private List<Stmt> collectionLimit(CollectionLimit limit, boolean dropped) {
		val collection = EcoreUtil2.getContainerOfType(limit, VectorCollection)
		if (collection === null) throw Unsupported.of('a collection limit outside a collection')
		val next = schema.collection(collection).copy
		switch limit.type.literal {
			case 'max_fields': next.maxProperties = if (dropped) null else limit.value
			case 'max_vector_dim': {
				if (dropped) throw Unsupported.of('dropping max_vector_dim; the vector fields keep their declared dimensions')
				for (vectorField : next.vectorFields) if (vectorField.vector.dims > limit.value) throw Unsupported.of('vector field ' + vectorField.name + ' with more dimensions than max_vector_dim')
			}
			default: throw Unsupported.of('collection limits (' + limit.type.literal + '); ' + SINGLE)
		}
		schema.evolve(collection, next)
		#[new Code.Run(command(next.database, obj('collMod', text(next.name), 'validator', next.validator, 'validationLevel', text('strict'))))]
	}

	/** A vector index created for an existing field: its vectorSearch index replaces the field's default one. */
	def private List<Stmt> updateVectorIndex(VectorIndex vectorIndex) {
		val owner = vectorIndex.field ?: EcoreUtil2.getContainerOfType(vectorIndex, VectorField)
		if (owner === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(owner, 'a vector index')
		val next = schema.collection(collection).copy
		val fieldName = next.field(ModelUtil.fieldName(owner)).name
		val List<Stmt> result = newArrayList
		val previous = next.vectorIndexes.findFirst[i | i.field == fieldName]
		val updated = schema.vectorIndex(collection, owner)
		next.vectorIndexes.removeIf[i | i.field == fieldName]
		next.vectorIndexes += new MongodbCollection.VectorIndex(updated.name, fieldName, updated.options, updated.searchCandidates)
		schema.evolve(collection, next)
		val definition = next.vectorDefinition(next.vectorIndexOf(fieldName))
		if (previous !== null && previous.name != updated.name) {
			result += new Code.Run(call('command', 'database', text(next.database), 'command', obj('dropSearchIndex', text(next.name), 'name', text(previous.name)), 'ignore', arr(text('IndexNotFound'))))
		}
		result += new Code.Run(call('ensure_search_index', 'database', text(next.database), 'collection', text(next.name), 'index', text(updated.name), 'type', text('vectorSearch'),
			'definition', definition))
		result += schema.awaitIndex(next, updated.name, AWAIT_SECONDS)
		result
	}

	/** A payload index on an existing field: createIndexes (geo: 2dsphere); a full_text index is the field's place in vecdsl_text. */
	def private List<Stmt> payloadIndex(PayloadIndex payloadIndex) {
		val owner = payloadIndex.target.field
		val collection = ModelUtil.targetCollection(payloadIndex) ?: (if (owner === null) null else ModelUtil.owningCollection(owner))
		if (collection === null) throw Unsupported.of('a payload index outside a collection')
		val target = schema.collection(collection)
		val keys = schema.payloadIndexKeys(target, payloadIndex)
		if (keys === null) return #[schema.awaitIndex(target, TEXT_INDEX, AWAIT_SECONDS)]
		val next = target.copy
		next.payloadIndexes.removeIf[i | i.name == payloadIndex.name]
		next.payloadIndexes += new MongodbCollection.PayloadIndex(MongodbCommands.field(payloadIndex.name), keys)
		schema.evolve(collection, next)
		#[new Code.Run(command(target.database, obj('createIndexes', text(target.name), 'indexes', arr(obj('key', keys, 'name', text(payloadIndex.name))))))]
	}

	/** A field added to an existing collection: validator, search indexes, and the default written where the field is missing. */
	def private List<Stmt> addField(VectorCollection collection, Field added) {
		val next = schema.collection(collection).copy
		val mapped = schema.field(collection, added)
		next.fields.removeIf[f | f.name == mapped.name]
		next.fields += mapped
		val List<Stmt> result = newArrayList
		if (added instanceof VectorField) {
			val index = schema.vectorIndex(collection, added as VectorField)
			next.vectorIndexes += index
			schema.evolve(collection, next)
			result += new Code.Run(command(next.database, obj('collMod', text(next.name), 'validator', next.validator, 'validationLevel', text('strict'))))
			result += new Code.Run(call('ensure_search_index', 'database', text(next.database), 'collection', text(next.name), 'index', text(index.name), 'type', text('vectorSearch'),
				'definition', next.vectorDefinition(index)))
			result += schema.awaitIndex(next, index.name, AWAIT_SECONDS)
			return result
		}
		schema.evolve(collection, next)
		result += redefine(next, mapped.fullText)
		if (mapped.defaultValue !== null) {
			result += new Code.Run(maintenance(next, obj(mapped.name, obj('$exists', flag(false))), obj('$set', obj(mapped.name, mapped.defaultValue))))
		}
		result
	}

	/** An update of every matching document that maintenance needs (defaults, renames, removed fields), past the validator. */
	def private Code.Call maintenance(MongodbCollection target, Entries matching, Entries change) {
		command(target.database, obj('update', text(target.name), 'updates', arr(obj('q', matching, 'u', change, 'multi', flag(true))),
			'bypassDocumentValidation', flag(true), 'writeConcern', obj('w', text('majority'))))
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias (a view) can be renamed')
			val alias = concept as Alias
			val aliasTarget = schema.collection(alias.collection)
			val List<Stmt> renamed = newArrayList
			renamed += new Code.Run(call('command', 'database', text(aliasTarget.database), 'command', obj('drop', text(checkedName(alias.name, 'view'))), 'ignore', arr(text('NamespaceNotFound'))))
			renamed += view(aliasTarget, newName, false)
			return renamed
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; MongoDB alters collections and views')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) result += rename(collection, newName)
		if (result.empty) {
			val target = schema.collection(collection)
			result += new Code.Return(call('command', 'database', text(target.database), 'command', obj('listCollections', num(1), 'filter', obj('name', obj('$eq', text(target.name)))),
				'select', arr(text('cursor'), text('firstBatch'), text('0'), text('options'))))
		}
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val target = schema.collection(collection)
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: dropField(collection, operation.field)
			RenameFieldOp: {
				if (ModelUtil.fieldName(operation.field) == target.key) throw Unsupported.of('renaming the primary key; the key is _id, which cannot be renamed')
				val old = target.field(ModelUtil.fieldName(operation.field))
				val newField = MongodbCommands.field(operation.newName)
				val next = target.copy
				next.rename(ModelUtil.fieldName(operation.field), newField)
				schema.evolve(collection, next)
				val List<Stmt> result = newArrayList
				result += new Code.Run(maintenance(target, obj(old.name, obj('$exists', flag(true))), obj('$rename', obj(old.name, text(newField)))))
				result += redefine(next, old.fullText)
				result
			}
			SetPropertyOp: setProperty(collection, target, operation)
			AddAliasOp: view(target, operation.newAlias.name, false)
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	/** A field leaves every document ($unset), the validator and the search indexes; a vector field's index is dropped. */
	def private List<Stmt> dropField(VectorCollection collection, Field dropped) {
		val target = schema.collection(collection)
		if (ModelUtil.fieldName(dropped) == target.key) throw Unsupported.of('dropping the primary key; _id cannot be removed')
		val old = target.field(ModelUtil.fieldName(dropped))
		val next = target.copy
		val List<Stmt> result = newArrayList
		result += new Code.Run(maintenance(target, obj(old.name, obj('$exists', flag(true))), obj('$unset', obj(old.name, text('')))))
		if (old.vector !== null) {
			val index = target.vectorIndexOf(old.name)
			result += new Code.Run(call('command', 'database', text(target.database), 'command', obj('dropSearchIndex', text(target.name), 'name', text(index.name)), 'ignore', arr(text('IndexNotFound'))))
		}
		next.remove(ModelUtil.fieldName(dropped))
		schema.evolve(collection, next)
		result += redefine(next, old.fullText)
		result
	}

	/** Collection properties: description in the $jsonSchema, dynamic as additionalProperties, ttl as the TTL index. */
	def private List<Stmt> setProperty(VectorCollection collection, MongodbCollection target, SetPropertyOp operation) {
		val next = target.copy
		val List<Stmt> result = newArrayList
		if (operation.property == org.ftn.vecDSL.vecDSL.ECollectionProperty.CONSISTENCY) {
			notes += 'consistency on SetPropertyOp, which the collection declaration decides for the writes of the script'
			return result
		}
		if (operation.property == org.ftn.vecDSL.vecDSL.ECollectionProperty.TTL) {
			if (!(operation.value instanceof NumberLit)) throw Unsupported.of('a ttl that is not a number of seconds')
			next.ttlSeconds = Long.valueOf((operation.value as NumberLit).value as long)
			schema.evolve(collection, next)
			result += new Code.Run(call('command', 'database', text(target.database), 'command', obj('dropIndexes', text(target.name), 'index', text('vecdsl_ttl')), 'ignore', arr(text('IndexNotFound'))))
			result += new Code.Run(command(target.database, obj('createIndexes', text(target.name), 'indexes', arr(obj('key', obj(EXPIRES_FIELD, num(1)), 'name', text('vecdsl_ttl'), 'expireAfterSeconds', num(0))))))
			return result
		}
		switch operation.property {
			case DESCRIPTION: {
				if (!(operation.value instanceof StringLit)) throw Unsupported.of('a description that is not a string literal')
				next.description = (operation.value as StringLit).value
			}
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DYNAMIC: next.dynamic = operation.value instanceof org.ftn.vecDSL.vecDSL.BoolLit && (operation.value as org.ftn.vecDSL.vecDSL.BoolLit).value
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
			default: {}
		}
		schema.evolve(collection, next)
		result += new Code.Run(command(next.database, obj('collMod', text(next.name), 'validator', next.validator, 'validationLevel', text('strict'))))
		result
	}

	/** renameCollection within the database; the search indexes are ensured on the new name. */
	def private List<Stmt> rename(VectorCollection collection, String newName) {
		val target = schema.collection(collection)
		val next = target.copy
		next.name = checkedName(newName, 'collection')
		schema.evolve(collection, next)
		val List<Stmt> result = newArrayList
		result += new Code.Run(command('admin', obj('renameCollection', text(target.database + '.' + target.name), 'to', text(next.database + '.' + next.name))))
		for (index : next.searchIndexes) {
			val entries = index as Entries
			val indexName = (entries.get('name') as Code.Text).value
			result += new Code.Run(call('ensure_search_index', 'database', text(next.database), 'collection', text(next.name), 'index', text(indexName), 'type', entries.get('type'),
				'definition', entries.get('definition')))
			result += schema.awaitIndex(next, indexName, AWAIT_SECONDS)
		}
		result
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		val ignore = if (ifExists) arr(text('NamespaceNotFound'), text('IndexNotFound')) else null
		switch concept {
			VectorCollection: {
				val dropped = schema.collection(concept)
				schema.evolve(concept, null)
				#[new Code.Run(call('command', 'database', text(dropped.database), 'command', obj('drop', text(dropped.name)), 'ignore', ignore)) as Stmt]
			}
			Database: #[new Code.Run(command(checkedName(concept.name, 'database'), obj('dropDatabase', num(1)))) as Stmt]
			Tenant: dropSlice(member(concept, 'a tenant'), 'tenant', concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), 'namespace', concept.key)
			Partition: dropSlice(member(concept, 'a partition'), 'partition', concept.name)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a field'), concept)
			Alias: {
				val aliasTarget = schema.collection(concept.collection)
				#[new Code.Run(call('command', 'database', text(aliasTarget.database), 'command', obj('drop', text(checkedName(concept.name, 'view'))), 'ignore', ignore)) as Stmt]
			}
			VectorIndex: {
				val owner = concept.field ?: EcoreUtil2.getContainerOfType(concept, VectorField)
				if (owner === null) throw Unsupported.of('a vector index without a vector field')
				val collection = member(owner, 'a vector index')
				val target = schema.collection(collection)
				val next = target.copy
				next.vectorIndexes.removeIf[i | i.name == concept.name]
				schema.evolve(collection, next)
				#[new Code.Run(call('command', 'database', text(target.database), 'command', obj('dropSearchIndex', text(target.name), 'name', text(concept.name)), 'ignore', ignore)) as Stmt]
			}
			PayloadIndex: {
				val collection = ModelUtil.targetCollection(concept) ?: (if (concept.target.field === null) null else ModelUtil.owningCollection(concept.target.field))
				if (collection === null) throw Unsupported.of('a payload index outside a collection')
				val target = schema.collection(collection)
				if (concept.kind.literal == 'full_text') throw Unsupported.of('dropping a full_text payload index; a full-text field stays in the search index vecdsl_text while it is declared full-text')
				#[new Code.Run(call('command', 'database', text(target.database), 'command', obj('dropIndexes', text(target.name), 'index', text(concept.name)), 'ignore', ignore)) as Stmt]
			}
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; redefine the vector index without it (rebuild-index sends the declared definition)')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a MongoDB object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; ' + SINGLE)
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE)
			Analyzer: throw Unsupported.of('dropping an analyzer; analyzers are removed with the search index, not while fields use them')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; analyzers are removed with the search index, not while fields use them')
			FieldLimit: throw Unsupported.of('dropping a field limit; redeclare the field without it (alter replaces the validator)')
			FileResource: throw Unsupported.of('file resources; ' + NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANK)
			ReplicationConfig: throw Unsupported.of('dropping replication; multi-document transactions and change streams need the replica set')
			ShardConfig: throw Unsupported.of('dropping sharding; ' + SINGLE)
			StorageConfig: throw Unsupported.of('dropping storage settings; WiredTiger is the storage engine of the deployment')
			AuthConfig: throw Unsupported.of('dropping authentication; access control is a server setting (security.authorization)')
			RoleBinding: {
				val database = if (concept.collection === null) schema.defaultDatabase else schema.collection(concept.collection).database
				#[new Code.Run(call('command', 'database', text(database), 'command', obj('dropRole', text(roleName(concept.roleName))), 'ignore', if (ifExists) arr(text('RoleNotFound')) else null)) as Stmt]
			}
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of('backup schedules; ' + NO_SNAPSHOTS)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: collectionLimit(concept, true)
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** Dropping a tenant, namespace or partition deletes its documents and its view. */
	def private List<Stmt> dropSlice(VectorCollection collection, String kind, String value) {
		val target = schema.collection(collection)
		val fieldName = sliceField(target, kind)
		if (fieldName === null) throw Unsupported.of('a ' + kind + ' of a collection without ' + kind + 's')
		val name = checkedName(target.name + '_' + kind + '_' + value.replaceAll('[^A-Za-z0-9_]', '_'), 'view')
		#[new Code.Run(call('command', 'database', text(target.database), 'command', obj('drop', text(name)), 'ignore', arr(text('NamespaceNotFound')))),
			new Code.Return(deleteCall(target, obj(fieldName, obj('$eq', text(value))), null, null))]
	}

	/** An alias (a view) points at another collection after one collMod. */
	def dispatch List<Stmt> body(SwapAlias it) {
		val next = schema.collection(newCollection)
		val old = schema.collection(oldCollection)
		if (next.database != old.database) throw Unsupported.of('swapping an alias between databases; a view reads a collection of its own database')
		#[new Code.Run(command(next.database, obj('collMod', text(checkedName(alias.name, 'view')), 'viewOn', text(next.name), 'pipeline', arr())))]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		#[new Code.Return(writeCall(collection, source, if (written(it, 'onConflict')) onConflict.literal else 'error', null, it, ttlOf(it)))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		#[new Code.Return(writeCall(collection, source, 'replace', if (written(it, 'batchSize')) batchSize else null, it, ttlOf(it)))]
	}

	def private Integer ttlOf(EObject statement) {
		if (written(statement, 'ttlSeconds')) feature(statement, 'ttlSeconds') as Integer else null
	}

	/** One write of the records with the slice fields of the statement, the defaults and the coercions of the collection. */
	def private Code.Call writeCall(VectorCollection collection, RecordSource recordSource, String policy, Integer batch, EObject statement, Integer ttl) {
		val target = schema.collection(collection)
		val mode = writeMode(policy)
		if (ttl !== null && target.ttlSeconds === null && !target.expiring) throw Unsupported.of('ttl on a write into a collection declared without ttl; declare ttl on the collection, which creates the TTL index on vecdsl_expires')
		val ttlSeconds = if (ttl !== null) num(ttl) else if (target.ttlSeconds !== null && collection.ttlField === null) num(target.ttlSeconds.doubleValue) else null
		call('write', 'database', text(target.database), 'collection', text(target.name), 'records', recordsOf(recordSource, target), 'key', text(target.key),
			'op', text(mode.get('op')), 'counter', text(mode.get('counter')), 'extra', sliceFields(statement, collection, target), 'defaults', target.defaults,
			'vectors', listOrNull(target.names(true, false)), 'dates', listOrNull(target.names(false, true)), 'versioned', if (target.versioned) flag(true) else null,
			'ttl_seconds', ttlSeconds, 'batch_size', if (batch === null) null else num(batch), 'ignore_conflicts', if (mode.containsKey('ignore')) flag(true) else null,
			'visible', visible(statement, collection, target))
	}

	def private Expr listOrNull(List<Expr> values) {
		if (values.empty) null else arr(values)
	}

	/** What the runtime waits for after a write: mongot shows it in every search index (with 'wait' or consistency strong). */
	def private Entries visible(EObject statement, VectorCollection collection, MongodbCollection target) {
		val waited = written(statement, 'waitForPersistence') || (written(collection, 'consistency') && collection.consistency == EConsistencyLevel.STRONG)
		if (!waited || target.probes.items.empty) return null
		obj('probes', target.probes, 'timeout_seconds', num(AWAIT_SECONDS))
	}

	/** The slice fields of a write: tenant, namespace and partition; partitioned collections default to the default partition. */
	def private Entries sliceFields(EObject statement, VectorCollection collection, MongodbCollection target) {
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
		val target = schema.collection(collection)
		if (source !== null) {
			if (filter !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty || written(it, 'ifVersion')
				|| (written(it, 'mode') && mode == EUpdateMode.REPLACE) || tenant !== null || namespace !== null || partition !== null) {
				throw Unsupported.of('update from records with a filter, a slice, field clauses, if-version or mode replace; update commands without upsert address documents by _id, and each record carries the values it sets')
			}
			return #[new Code.Return(call('write', 'database', text(target.database), 'collection', text(target.name), 'records', recordsOf(source, target),
				'key', text(target.key), 'op', text('patch'), 'counter', text('update_count'), 'vectors', listOrNull(target.names(true, false)),
				'dates', listOrNull(target.names(false, true)), 'versioned', if (target.versioned) flag(true) else null, 'visible', visible(it, collection, target),
				'only', Code.items(ids.map[i | Literals.of(i)].toList)))]
		}
		val Entries assignments = Code.entries
		val Entries appended = Code.entries
		val Entries pulled = Code.entries
		val Entries removed = Code.entries
		val List<String> given = newArrayList
		if (values !== null) {
			for (entry : values.entries) {
				if (ModelUtil.fieldName(entry.field) == target.key) throw Unsupported.of('updating the primary key; the key is _id, which cannot change')
				val mapped = target.field(ModelUtil.fieldName(entry.field))
				given += mapped.name
				val converted = fieldValue(entry.field, entry.value)
				if (appendFields.contains(entry.field)) appended.put(mapped.name, obj('$each', listOf(converted)))
				else if (arrayRemoveFields.contains(entry.field)) pulled.put(mapped.name, obj('$in', listOf(converted)))
				else assignments.put(mapped.name, converted)
			}
		}
		for (arrayField : appendFields + arrayRemoveFields) {
			if (!given.contains(target.field(ModelUtil.fieldName(arrayField)).name)) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(arrayField) + " without items in 'set'")
		}
		for (removedField : removedFields) {
			if (ModelUtil.fieldName(removedField) == target.key) throw Unsupported.of('removing the primary key')
			removed.put(target.field(ModelUtil.fieldName(removedField)).name, text(''))
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			for (mapped : target.fields) {
				if (mapped.name != target.key && mapped.vector === null && !given.contains(mapped.name)) removed.put(mapped.name, text(''))
			}
		}
		if (assignments.entries.empty && appended.entries.empty && pulled.entries.empty && removed.entries.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
		val Entries change = obj('$set', if (assignments.entries.empty) null else assignments, '$unset', if (removed.entries.empty) null else removed,
			'$push', if (appended.entries.empty) null else appended, '$pull', if (pulled.entries.empty) null else pulled,
			'$inc', if (target.versioned) obj(VERSION_FIELD, num(1)) else null)
		val conditions = clauses(it, collection, target, filter, idsOf(ids), false)
		if (written(it, 'ifVersion')) conditions += obj(VERSION_FIELD, obj('$eq', num(ifVersion)))
		#[new Code.Return(call('update', 'database', text(target.database), 'collection', text(target.name), 'filter', matchOf(conditions), 'update', change,
			'vectors', listOrNull(target.names(true, false)), 'dates', listOrNull(target.names(false, true)), 'visible', visible(it, collection, target)))]
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val target = schema.collection(collection)
		if (ids.empty && filter === null && !deleteAll) throw Unsupported.of("a delete without ids, filter or 'all'")
		val matching = matchOf(clauses(it, collection, target, filter, idsOf(ids), false))
		#[new Code.Return(deleteCall(target, matching, if (written(it, 'deleteLimit')) deleteLimit else null, visible(it, collection, target)))]
	}

	def private Code.Call deleteCall(MongodbCollection target, Entries matching, Integer limit, Entries visibleSpec) {
		call('delete', 'database', text(target.database), 'collection', text(target.name), 'filter', matching, 'limit', if (limit === null) null else num(limit), 'visible', visibleSpec)
	}

	/** The runtime reads a local JSON or JSON Lines file and writes it like an insert, batch-size documents per command. */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; MongoDB pulls no streams itself (Atlas Stream Processing is an Atlas service)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; MongoDB stores documents that a client sends (mongoimport and Atlas Data Federation are outside the database commands)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV records in the runtime)')
			throw Unsupported.of('import format ' + format.literal + '; the generated programs read JSON documents')
		}
		schema.note(it, 'parallelism', 'async')
		for (key : ModelUtil.extParams(it, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val target = schema.collection(collection)
		val mode = writeMode(if (written(it, 'onConflict')) onConflict.literal else 'error')
		#[new Code.Return(call('import_file', 'location', text(location), 'database', text(target.database), 'collection', text(target.name), 'key', text(target.key),
			'op', text(mode.get('op')), 'counter', text(mode.get('counter')), 'extra', sliceFields(it, collection, target), 'defaults', target.defaults,
			'vectors', listOrNull(target.names(true, false)), 'dates', listOrNull(target.names(false, true)), 'versioned', if (target.versioned) flag(true) else null,
			'batch_size', if (written(it, 'batchSize')) num(batchSize) else null, 'ignore_conflicts', if (mode.containsKey('ignore')) flag(true) else null))]
	}

	/** The writes of the transaction as steps of one session: startTransaction, the commands, commitTransaction (abort on error). */
	def dispatch List<Stmt> body(Transaction it) {
		val List<Expr> steps = newArrayList
		for (statement : it.methods) {
			if (statement instanceof BulkImport) throw Unsupported.of('bulk-import inside a transaction; an import is a sequence of batches, not one step')
			val inner = body(statement)
			if (inner.size != 1 || !(inner.head instanceof Code.Return)) throw Unsupported.of('this statement inside a transaction')
			val invocation = (inner.head as Code.Return).value as Code.Call
			steps += obj('op', text(invocation.op), 'args', invocation.args)
		}
		val concern = if (written(it, 'isolationLevel')) {
			switch isolationLevel.literal {
				case 'serializable': throw Unsupported.of('serializable isolation; MongoDB transactions read a snapshot and detect write-write conflicts only (snapshot isolation)')
				case 'read_committed': 'majority'
				case 'read_uncommitted': 'local'
				default: 'snapshot'
			}
		} else 'snapshot'
		#[new Code.Return(call('transaction', 'steps', arr(steps), 'read_concern', text(concern)))]
	}

	/** Write consistency is the server's (write concern majority); extensions on writes are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (key : ModelUtil.extParams(statement, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- operational

	def dispatch List<Stmt> body(Load it) {
		throw Unsupported.of('loading a collection; ' + NO_RESIDENCY)
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of('releasing a collection; ' + NO_RESIDENCY)
	}

	def dispatch List<Stmt> body(Flush it) {
		operationExtensions(it)
		#[new Code.Run(command('admin', obj('fsync', num(1))))]
	}

	def dispatch List<Stmt> body(Compact it) {
		operationExtensions(it)
		if (written(it, 'targetSegments')) throw Unsupported.of("the 'target-segments' clause; WiredTiger compacts collection files and has no segments")
		schema.note(it, 'async')
		val target = schema.collection(collection)
		#[new Code.Return(call('command', 'database', text(target.database), 'command', obj('compact', text(target.name), 'force', flag(true)), 'select', arr(text('bytesFreed'))))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val target = schema.collection(collection)
		val info = call('command', 'database', text(target.database), 'command', obj('listCollections', num(1), 'filter', obj('name', obj('$eq', text(target.name)))),
			'select', arr(text('cursor'), text('firstBatch'), text('0'), text('options')))
		if (!includeStats && !includeIndexProgress) return #[new Code.Return(info)]
		val List<Expr> parts = newArrayList
		parts += info
		if (includeStats) parts += aggregateCall(target, arr(obj('$collStats', obj('storageStats', Code.entries)), obj('$project', obj('_id', num(0), 'count', text('$storageStats.count'),
			'size', text('$storageStats.size'), 'nindexes', text('$storageStats.nindexes')))), null, true)
		if (includeIndexProgress) parts += aggregateCall(target, arr(obj('$listSearchIndexes', Code.entries), obj('$project', obj('_id', num(0), 'name', num(1), 'type', num(1),
			'status', num(1), 'queryable', num(1))), obj('$sort', obj('name', num(1)))), null, false)
		#[new Code.Return(Code.items(parts))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + NO_MODELS)
	}

	def dispatch List<Stmt> body(Snapshot it) {
		throw Unsupported.of(NO_SNAPSHOTS)
	}

	def dispatch List<Stmt> body(Restore it) {
		throw Unsupported.of('restoring ' + NO_SNAPSHOTS)
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		throw Unsupported.of('dropping ' + NO_SNAPSHOTS)
	}

	/** A copy with the source's validator, documents ($out), payload indexes and search indexes. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; the program talks to one deployment, and copying a database between deployments is a mongodump and mongorestore job')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		operationExtensions(it)
		schema.note(it, 'mode')
		val source = schema.collection(sourceCollection)
		val copy = source.copy
		copy.name = checkedName(targetName, 'collection')
		val List<Stmt> result = newArrayList
		result += new Code.Run(command(copy.database, obj('create', text(copy.name), 'validator', copy.validator, 'validationLevel', text('strict'), 'validationAction', text('error'))))
		result += new Code.Run(aggregateCall(source, arr(obj('$out', obj('db', text(copy.database), 'coll', text(copy.name)))), null, false))
		result += schema.indexStatements(copy, null)
		result += new Code.Return(aggregateCall(copy, arr(obj('$count', text('copied'))), null, true))
		result
	}

	/** mongot rebuilds a search index for an updated definition; a payload index is dropped and created again. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		operationExtensions(it)
		schema.note(it, 'concurrently')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		val target = schema.collection(collection)
		switch index {
			VectorIndex: {
				val owner = (index as VectorIndex).field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				val definition = target.vectorDefinition(target.vectorIndexOf(target.field(ModelUtil.fieldName(owner)).name))
				#[new Code.Run(command(target.database, obj('updateSearchIndex', text(target.name), 'name', text(index.name), 'definition', definition))) as Stmt,
					schema.awaitIndex(target, index.name, AWAIT_SECONDS)]
			}
			PayloadIndex: {
				val keys = schema.payloadIndexKeys(target, index as PayloadIndex)
				if (keys === null) {
					#[new Code.Run(command(target.database, obj('updateSearchIndex', text(target.name), 'name', text(TEXT_INDEX), 'definition', target.textDefinition))) as Stmt,
						schema.awaitIndex(target, TEXT_INDEX, AWAIT_SECONDS)]
				} else {
					#[new Code.Run(call('command', 'database', text(target.database), 'command', obj('dropIndexes', text(target.name), 'index', text(index.name)), 'ignore', arr(text('IndexNotFound')))) as Stmt,
						new Code.Run(command(target.database, obj('createIndexes', text(target.name), 'indexes', arr(obj('key', keys, 'name', text(index.name))))))]
				}
			}
			default: throw Unsupported.of(NOT_MAPPED + index.eClass.name)
		}
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val target = schema.collection(collection)
		if (namespace !== null) {
			if (target.namespace === null) throw Unsupported.of('a namespace of a collection without namespaces')
			return #[new Code.Return(deleteCall(target, obj(target.namespace, obj('$eq', text(namespace))), null, null))]
		}
		#[new Code.Return(deleteCall(target, Code.entries, null, null))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = scope
		val limit = if (written(it, 'listLimit')) num(listLimit) else null
		val prefixValue = if (prefix === null) null else text(prefix)
		val listing = switch kind {
			case COLLECTIONS: {
				val database = if (owner instanceof Database) checkedName((owner as Database).name, 'database') else {
					if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; MongoDB lists the collections of a database')
					schema.defaultDatabase
				}
				call('names', 'kind', text('collections'), 'database', text(database), 'prefix', prefixValue, 'limit', limit)
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				call('names', 'kind', text('databases'), 'database', text('admin'), 'prefix', prefixValue, 'limit', limit)
			}
			case INDEXES: {
				val target = scopeCollection(owner, 'indexes')
				call('names', 'kind', text('search_indexes'), 'database', text(target.database), 'collection', text(target.name), 'prefix', prefixValue, 'limit', limit)
			}
			case ALIASES: {
				val target = if (owner === null) null else scopeCollection(owner, 'aliases')
				call('names', 'kind', text('views'), 'database', text(target?.database ?: schema.defaultDatabase), 'collection', if (target === null) null else text(target.name),
					'prefix', prefixValue, 'limit', limit)
			}
			case TENANTS,
			case NAMESPACES,
			case PARTITIONS: {
				val target = scopeCollection(owner, kind.literal)
				val fieldName = switch kind { case TENANTS: target.tenant case NAMESPACES: target.namespace default: target.partition }
				if (fieldName === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				call('names', 'kind', text('distinct'), 'database', text(target.database), 'collection', text(target.name), 'field', text(fieldName), 'prefix', prefixValue, 'limit', limit)
			}
			case SNAPSHOTS: throw Unsupported.of('listing ' + NO_SNAPSHOTS)
		}
		#[new Code.Return(listing)]
	}

	def private MongodbCollection scopeCollection(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.collection(owner as VectorCollection)
	}

	/** mongot builds search indexes asynchronously: the runtime polls $listSearchIndexes until the index is queryable. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		val target = schema.collection(collection)
		val seconds = if (written(it, 'timeoutSeconds')) timeoutSeconds else AWAIT_SECONDS
		if (index instanceof PayloadIndex && schema.payloadIndexKeys(target, index as PayloadIndex) !== null) {
			return #[new Code.Return(call('command', 'database', text(target.database), 'command', obj('listIndexes', text(target.name)),
				'select', arr(text('cursor'), text('firstBatch'))))]
		}
		val indexName = switch index {
			VectorIndex: target.vectorIndexOf(target.field(ModelUtil.fieldName((index as VectorIndex).field ?: EcoreUtil2.getContainerOfType(index, VectorField))).name).name
			PayloadIndex: TEXT_INDEX
			default: throw Unsupported.of(NOT_MAPPED + index.eClass.name)
		}
		#[new Code.Return(call('await_index', 'database', text(target.database), 'collection', text(target.name), 'index', text(indexName), 'timeout_seconds', num(seconds)))]
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
		for (key : ModelUtil.extParams(statement, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		rejectClauses(it, 'elementScope', 'asOf', 'cursor', 'reranker', 'queryModel', 'prefetches', 'explain', 'highlighted')
		queryExtensions(it)
		schema.note(it, 'consistency')
		val target = schema.collection(collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		if (input === null) throw Unsupported.of('a vector query without input')
		val queryVector = queryVectorOf(input, mapped)
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val shapes = moduled(it)
		val k = if (shapes === null) topK + offsetOf(it) else windowOf(shapes)
		val searchMetric = if (written(it, 'metric')) metric.literal else mapped.vector.metric
		val List<Expr> stages = newArrayList
		if (searchMetric != mapped.vector.metric) {
			if (input.kind == EQueryInputMode.ID) throw Unsupported.of('a query by stored vector with another metric than the field\'s; ' + PENDING + ' (the stored vector in the exact scan)')
			if (nativeScore) notes += 'score native on VectorSearch, whose exact scan computes the canonical similarity directly'
			stages += obj('$match', matchOf(clauses(it, collection, target, filter, null, false)))
			stages += obj('$addFields', obj(SCORE, exactScore(searchMetric, mapped.name, queryVector)))
			if (shapes !== null) return #[new Code.Return(shapedCall(candidateWindow(target, stages, shapes, false, false, options(it, params)), shapes, target))]
			return #[new Code.Return(ranked(it, collection, target, stages, true, false, options(it, params)))]
		}
		val searchParams = params
		val exhaustive = exact || (searchParams !== null && searchParams.exhaustive)
		val postFilter = searchParams !== null && written(searchParams, 'filterStrategy') && searchParams.filterStrategy.literal == 'post_filter'
		if (searchParams !== null) {
			if (written(searchParams, 'targetRecall')) throw Unsupported.of('target-recall: numCandidates is an effort setting, with no declarable recall target')
			if (written(searchParams, 'probes')) throw Unsupported.of('probes; mongot builds hnsw and flat indexes, with no clusters to probe')
			if (written(searchParams, 'scanBudget')) throw Unsupported.of('scan-budget; $vectorSearch takes numCandidates and no bound on visited vectors')
			if (written(searchParams, 'oversampling') || searchParams.rescore) throw Unsupported.of('oversampling and rescore settings; mongot rescores quantized vectors itself, and numCandidates is the effort setting')
			for (key : ModelUtil.extParams(searchParams, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		}
		val index = target.vectorIndexOf(mapped.name)
		val Integer pool = if (searchParams !== null && written(searchParams, 'candidatePool')) searchParams.candidatePool else index.searchCandidates
		val List<Expr> conditions = clauses(it, collection, target, if (postFilter) null else filter, null, true)
		if (input.kind == EQueryInputMode.ID && input.lookupCollection === null) conditions += obj('_id', obj('$ne', Literals.of(input.value)))
		stages += obj('$vectorSearch', obj('index', text(index.name), 'path', text(mapped.name), 'queryVector', queryVector,
			'numCandidates', if (exhaustive) null else num(candidates(pool, k)), 'limit', num(k), 'filter', if (conditions.empty) null else matchOf(conditions),
			'exact', if (exhaustive) flag(true) else null))
		if (postFilter && filter !== null) stages += obj('$match', new MongodbFilter(target, false).query(filter.condition))
		stages += obj('$addFields', obj(SCORE, if (nativeScore) obj('$meta', text('vectorSearchScore')) else canonicalScore(mapped.vector.similarity, 'vectorSearchScore')))
		val search = if (shapes === null) ranked(it, collection, target, stages, true, false, options(it, searchParams))
			else candidateWindow(target, stages, shapes, false, false, options(it, searchParams))
		if (input.kind == EQueryInputMode.ID) {
			val lookup = schema.collection(input.lookupCollection ?: collection)
			val recommended = recommendCall(target, lookup, mapped, search, arr(Literals.of(input.value)), null, null, null)
			return #[new Code.Return(if (shapes === null) recommended else shapedCall(recommended, shapes, target))]
		}
		#[new Code.Return(if (shapes === null) search else shapedCall(search, shapes, target))]
	}

	def private Entries options(EObject statement, SearchParams searchParams) {
		val Entries result = Code.entries
		if (searchParams !== null && written(searchParams, 'timeoutMillis')) result.put('maxTimeMS', num(searchParams.timeoutMillis))
		for (entry : ModelUtil.extParams(statement, MongodbGenerator.VENDOR).entrySet) {
			if (!AGGREGATE_KEYS.contains(entry.key)) throw Unsupported.of('extension parameter ' + entry.key + '; the aggregate options the generator passes are ' + AGGREGATE_KEYS.join(', '))
			if (entry.value instanceof ParamRef || entry.value instanceof org.ftn.vecDSL.vecDSL.EnvRef) throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a literal')
			result.put(entry.key, Literals.of(entry.value))
		}
		if (result.entries.empty) null else result
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		rejectClauses(it, 'asOf', 'cursor', 'reranker')
		queryExtensions(it)
		schema.note(it, 'consistency')
		if (written(it, 'mmr')) throw Unsupported.of("the 'mmr' clause on a keyword search; maximal marginal relevance compares the hits' vectors with a query vector, which a text query has not")
		if (params !== null) schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		val target = schema.collection(collection)
		if (!target.hasText) throw Unsupported.of('keyword search in a collection without full-text fields; $search needs the search index of the full-text fields')
		val operator = keywordOperator(target, searchFields, input, if (written(it, 'textQueryType')) textQueryType.literal else 'match',
			if (written(it, 'matchOperator')) matchOperator.literal else null, written(it, 'minShouldMatch'), fuzziness, if (written(it, 'slop')) slop else null)
		val Entries searchStage = obj('index', text(TEXT_INDEX))
		searchStage.entries.putAll(operator.entries)
		val highlighted = (feature(it, 'highlighted') ?: newArrayList) as List<Field>
		if (!highlighted.empty) {
			val List<Expr> paths = newArrayList
			for (f : highlighted) {
				val mapped = target.field(ModelUtil.fieldName(f))
				if (!mapped.fullText) throw Unsupported.of('highlighting ' + mapped.name + ', which is not a full-text field')
				paths += text(mapped.name)
			}
			searchStage.put('highlight', obj('path', arr(paths)))
		}
		if (explain) searchStage.put('scoreDetails', flag(true))
		val List<Expr> stages = newArrayList
		stages += obj('$search', searchStage)
		val conditions = clauses(it, collection, target, filter, null, false)
		if (!conditions.empty) stages += obj('$match', matchOf(conditions))
		stages += obj('$addFields', obj(SCORE, obj('$meta', text('searchScore'))))
		val shapes = moduled(it)
		if (shapes !== null) {
			return #[new Code.Return(shapedCall(candidateWindow(target, stages, shapes, !highlighted.empty, explain, options(it, params)), shapes, target))]
		}
		#[new Code.Return(ranked(it, collection, target, stages, true, true, options(it, params)))]
	}

	/**
	 * The $search operator of a text query: text (match and fuzzy), phrase with slop, autocomplete (prefix and phrase_prefix),
	 * wildcard (term and wildcard), regex and queryString; several fields with boosts become compound should clauses, one per
	 * field with a score boost, so the field scores add up.
	 */
	def private Entries keywordOperator(MongodbCollection target, List<SearchFieldRef> refs, QueryInput queryInput, String type, String operator,
		boolean minimum, String fuzzinessValue, Integer slopValue) {
		val words = keywordText(queryInput)
		val List<String> fields = newArrayList
		val List<Double> boosts = newArrayList
		if (refs.empty) {
			for (mapped : target.fields) if (mapped.fullText) {
				fields += mapped.name
				boosts += null as Double
			}
		} else {
			for (ref : refs) {
				val mapped = target.field(ModelUtil.fieldName(ref.field))
				if (!mapped.fullText) throw Unsupported.of('keyword search on ' + mapped.name + ', which is not a full-text field of the search index')
				fields += mapped.name
				boosts += if (written(ref, 'boost')) Double.valueOf(ref.boost) else null
			}
		}
		if (minimum) throw Unsupported.of("the 'min-should-match' clause; the text operator matches any or all terms (matchCriteria), and compound minimumShouldMatch counts clauses, not the terms of one query")
		if (slopValue !== null && type != 'phrase') throw Unsupported.of("the 'slop' clause with text-query " + type + '; slop belongs to phrase queries')
		if (fuzzinessValue !== null && type != 'match') throw Unsupported.of("the 'fuzziness' clause with text-query " + type + '; fuzziness belongs to match queries (text-query fuzzy has its own)')
		if (operator !== null && type != 'match') throw Unsupported.of("the 'operator' clause with text-query " + type + '; matchCriteria belongs to the text operator')
		val maxEdits = if (fuzzinessValue === null || fuzzinessValue == '0') null else switch fuzzinessValue {
			case '1': num(1)
			case '2': num(2)
			default: throw Unsupported.of('fuzziness ' + fuzzinessValue + '; the text operator takes maxEdits 1 or 2 and has no length-dependent edit distance')
		}
		val List<Entries> parts = newArrayList
		val single = fields.size == 1 || type == 'match' || type == 'phrase' || type == 'fuzzy' || type == 'wildcard' || type == 'regexp' || type == 'term'
		val perField = boosts.exists[b | b !== null] || !single
		val List<List<String>> groups = if (perField) fields.map[f | #[f]].toList else #[fields]
		for (group : groups) {
			val Expr path = if (group.size == 1) text(group.head) else arr(group.map[f | text(f) as Expr].toList)
			val Entries operatorBody = switch type {
				case 'match': obj('text', obj('query', words, 'path', path, 'matchCriteria', if (operator === null) null else text(if (operator == 'and') 'all' else 'any'),
					'fuzzy', if (maxEdits === null) null else obj('maxEdits', maxEdits)))
				case 'fuzzy': obj('text', obj('query', words, 'path', path, 'fuzzy', obj('maxEdits', num(2))))
				case 'phrase': obj('phrase', obj('query', words, 'path', path, 'slop', if (slopValue === null) null else num(slopValue)))
				case 'phrase_prefix': obj('autocomplete', obj('query', words, 'path', path, 'tokenOrder', text('sequential')))
				case 'prefix': obj('autocomplete', obj('query', words, 'path', path, 'tokenOrder', text('any')))
				case 'term': obj('wildcard', obj('query', text(MongodbFilter.escaped(constantText(queryInput, 'term')).replace('\\.', '.').replace('\\-', '-')),
					'path', path, 'allowAnalyzedField', flag(true)))
				case 'wildcard': obj('wildcard', obj('query', words, 'path', path, 'allowAnalyzedField', flag(true)))
				case 'regexp': obj('regex', obj('query', words, 'path', path, 'allowAnalyzedField', flag(true)))
				case 'query_string': obj('queryString', obj('defaultPath', path, 'query', words))
				default: throw Unsupported.of('text-query ' + type)
			}
			val boost = boosts.get(fields.indexOf(group.head))
			if (boost !== null && perField) {
				val inner = operatorBody.entries.values.head as Entries
				inner.put('score', obj('boost', obj('value', num(boost))))
			}
			parts += operatorBody
		}
		if (parts.size == 1) parts.head else obj('compound', obj('should', arr(parts), 'minimumShouldMatch', num(1)))
	}

	def private String constantText(QueryInput queryInput, String what) {
		if (!(queryInput.value instanceof StringLit)) throw Unsupported.of('text-query ' + what + ' with a parameter; the generator escapes the wildcard characters of a string literal')
		(queryInput.value as StringLit).value
	}

	/**
	 * Hybrid search: rrf with k 60 and unweighted legs is $rankFusion over one pipeline per leg; other fusions run in the runtime
	 * over one aggregate per leg, each with the canonical similarities. The result clauses (with-total, sort, aggregate,
	 * boost, mmr, group-by) go to the shared result-shape module: the fused records of the candidate window are its candidates.
	 */
	def dispatch List<Stmt> body(HybridSearch it) {
		rejectClauses(it, 'asOf', 'reranker', 'cursor')
		for (clause : #['explain', 'highlighted']) {
			if (written(it, clause)) throw Unsupported.of("the '" + (CLAUSES.get(clause) ?: clause) + "' clause on a hybrid search; " + PENDING + ' (score details and highlights through $rankFusion and the runtime fusion)')
		}
		queryExtensions(it)
		schema.note(it, 'consistency')
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val target = schema.collection(collection)
		val shapes = ShapeSpec.of(it)
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val nativeFusion = fusionKind == 'rrf' && k == 60
		val defaultLimit = topK + offsetOf(it)
		val Entries pipelines = Code.entries
		val List<Expr> legParts = newArrayList
		var position = 0
		for (leg : legs) {
			val legLimit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else defaultLimit
			val legFilter = feature(leg, 'filter') as FilterBlock
			for (key : ModelUtil.extParams(leg, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a hybrid leg')
			val List<Expr> stages = newArrayList
			switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (a score condition on the leg)')
					val legField = vectorFieldOf(collection, leg.vectorField)
					val mapped = target.field(ModelUtil.fieldName(legField))
					if (written(leg, 'metric') && leg.metric.literal != mapped.vector.metric || leg.exact) throw Unsupported.of('an exact or re-metered vector leg; ' + PENDING + ' (an exact scan as a leg)')
					if (leg.params !== null) schema.note(leg.params, 'filterStrategy', 'exhaustive')
					val legInput = leg.input ?: input
					if (legInput === null || legInput.kind == EQueryInputMode.ID) throw Unsupported.of('a vector leg without a vector input')
					val conditions = clauses(it, collection, target, filter, null, true)
					if (legFilter !== null) conditions += new MongodbFilter(target, true).query(legFilter.condition)
					val index = target.vectorIndexOf(mapped.name)
					val Integer pool = if (leg.params !== null && written(leg.params, 'candidatePool')) leg.params.candidatePool else index.searchCandidates
					stages += obj('$vectorSearch', obj('index', text(index.name), 'path', text(mapped.name), 'queryVector', queryVectorOf(legInput, mapped),
						'numCandidates', num(candidates(pool, legLimit)), 'limit', num(legLimit), 'filter', if (conditions.empty) null else matchOf(conditions)))
					if (!nativeFusion) stages += obj('$addFields', obj(SCORE, canonicalScore(mapped.vector.similarity, 'vectorSearchScore')))
				}
				KeywordLeg: {
					if (!target.hasText) throw Unsupported.of('a keyword leg in a collection without full-text fields')
					val operator = keywordOperator(target, leg.searchFields, leg.input ?: input, if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match',
						if (written(leg, 'matchOperator')) leg.matchOperator.literal else null, written(leg, 'minShouldMatch'), null, null)
					val Entries searchStage = obj('index', text(TEXT_INDEX))
					searchStage.entries.putAll(operator.entries)
					stages += obj('$search', searchStage)
					val conditions = clauses(it, collection, target, filter, null, false)
					if (legFilter !== null) conditions += new MongodbFilter(target, false).query(legFilter.condition)
					if (!conditions.empty) stages += obj('$match', matchOf(conditions))
					stages += obj('$limit', num(legLimit))
					if (!nativeFusion) stages += obj('$addFields', obj(SCORE, obj('$meta', text('searchScore'))))
				}
			}
			if (nativeFusion) {
				pipelines.put('leg' + position, arr(stages))
			} else {
				stages += if (shapes.plain()) shape(it, collection, target, true, false) else MongodbShapes.projection(shapes, target, false, false)
				legParts += obj('pipeline', arr(stages), 'weight', num(weights.get(position)))
			}
			position = position + 1
		}
		if (nativeFusion) {
			val List<Expr> stages = newArrayList
			stages += obj('$rankFusion', obj('input', obj('pipelines', pipelines)))
			stages += obj('$addFields', obj(SCORE, obj('$meta', text('score'))))
			if (!shapes.plain()) return #[new Code.Return(shapedCall(candidateWindow(target, stages, shapes, false, false, options(it, params)), shapes, target))]
			return #[new Code.Return(ranked(it, collection, target, stages, true, false, options(it, params)))]
		}
		val fused = call('hybrid', 'database', text(target.database), 'collection', text(target.name), 'legs', arr(legParts), 'fusion', text(fusionKind),
			'k', num(k), 'size', if (shapes.plain()) num(topK) else shapes.window(), 'offset', num(if (shapes.plain()) offsetOf(it) else 0), 'key', text(target.key),
			'bounds', if (shapes.plain()) boundsOf(bounds) else null, 'candidates', if (shapes.plain()) null else flag(true))
		#[new Code.Return(if (shapes.plain()) fused else shapedCall(fused, shapes, target))]
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
		rejectClauses(it, 'asOf', 'cursor', 'reranker', 'explain', 'highlighted')
		queryExtensions(it)
		schema.note(it, 'consistency')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val target = schema.collection(collection)
		val lookup = schema.collection(lookupCollection ?: collection)
		val searched = vectorFieldOf(collection, it.vectorField)
		val mapped = target.field(ModelUtil.fieldName(searched))
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) if (isVector(example)) positiveVectors += Literals.of(example) else positiveIds += Literals.of(example)
		for (example : negatives) if (isVector(example)) negativeVectors += Literals.of(example) else negativeIds += Literals.of(example)
		val conditions = clauses(it, collection, target, filter, null, true)
		if (lookupCollection === null || lookupCollection === collection) {
			val List<Expr> examples = newArrayList
			examples += positiveIds
			examples += negativeIds
			if (!examples.empty) conditions += obj('_id', obj('$nin', arr(examples)))
		}
		val shapes = moduled(it)
		val k = if (shapes === null) topK + offsetOf(it) else windowOf(shapes)
		val index = target.vectorIndexOf(mapped.name)
		val Integer pool = if (params !== null && written(params, 'candidatePool')) params.candidatePool else index.searchCandidates
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val List<Expr> stages = newArrayList
		stages += obj('$vectorSearch', obj('index', text(index.name), 'path', text(mapped.name), 'queryVector', new Code.Nil,
			'numCandidates', num(candidates(pool, k)), 'limit', num(k), 'filter', if (conditions.empty) null else matchOf(conditions)))
		stages += obj('$addFields', obj(SCORE, if (nativeScore) obj('$meta', text('vectorSearchScore')) else canonicalScore(mapped.vector.similarity, 'vectorSearchScore')))
		val search = if (shapes === null) ranked(it, collection, target, stages, true, false, options(it, params))
			else candidateWindow(target, stages, shapes, false, false, options(it, params))
		val recommended = recommendCall(target, lookup, mapped, search, if (positiveIds.empty) null else arr(positiveIds),
			if (positiveVectors.empty) null else arr(positiveVectors), if (negativeIds.empty) null else arr(negativeIds),
			if (negativeVectors.empty) null else arr(negativeVectors))
		#[new Code.Return(if (shapes === null) recommended else shapedCall(recommended, shapes, target))]
	}

	/** The runtime computes the query vector from the examples and runs the search's pipeline with it as queryVector. */
	def private Code.Call recommendCall(MongodbCollection target, MongodbCollection lookup, MongodbCollection.Field mapped, Code.Call search, Expr positiveIds,
		Expr positiveVectors, Expr negativeIds, Expr negativeVectors) {
		call('recommend', 'database', text(target.database), 'collection', text(target.name), 'lookup', text(lookup.name), 'field', text(mapped.name),
			'pipeline', search.args.get('pipeline'), 'key', text(target.key), 'options', search.args.get('options'), 'first', search.args.get('first'),
			'positive_ids', positiveIds, 'positive_vectors', positiveVectors, 'negative_ids', negativeIds, 'negative_vectors', negativeVectors,
			'candidates', search.args.get('candidates'))
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
		schema.note(it, 'consistency')
		val target = schema.collection(collection)
		val keys = idsOf(ids)
		val conditions = clauses(it, collection, target, null, keys, false)
		val List<Expr> stages = newArrayList
		stages += obj('$match', matchOf(conditions))
		stages += obj('$addFields', obj('vecdsl_order', obj('$indexOfArray', arr(literal(keys), text('$_id')))))
		stages += obj('$sort', obj('vecdsl_order', num(1)))
		stages += shape(it, collection, target, false, false)
		#[new Code.Return(aggregateCall(target, arr(stages), null, false))]
	}

	def dispatch List<Stmt> body(Count it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		schema.note(it, 'exact', 'consistency')
		val target = schema.collection(collection)
		val counting = aggregateCall(target, arr(obj('$match', matchOf(clauses(it, collection, target, filter, null, false))), obj('$count', text('count'))), options(it, null), true)
		counting.args.put('select', arr(text('count')))
		counting.args.put('default', num(0))
		#[new Code.Return(counting)]
	}

	def dispatch List<Stmt> body(Scroll it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		schema.note(it, 'consistency')
		val target = schema.collection(collection)
		val conditions = clauses(it, collection, target, filter, null, false)
		if (cursor !== null) {
			if (!sortClauses.empty || offsetOf(it) > 0) throw Unsupported.of("the 'cursor' clause with sort or offset; the cursor continues after the key of the last page")
			conditions += obj('_id', obj('$gt', cursorValue(target, cursor)))
		}
		if (written(it, 'sliceIndex') || written(it, 'sliceTotal')) {
			if (!written(it, 'sliceTotal') || sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal) throw Unsupported.of('a slice index outside 0 to slice total - 1')
			conditions += obj('$expr', obj('$eq', arr(obj('$abs', obj('$mod', arr(obj('$toHashedIndexKey', text('$_id')), num(sliceTotal)))), num(sliceIndex))))
		}
		val Entries ordering = Code.entries
		for (clause : sortClauses) ordering.entries.putAll(sortEntry(target, clause, false).entries)
		ordering.put('_id', num(1))
		val List<Expr> stages = newArrayList
		stages += obj('$match', matchOf(conditions))
		stages += obj('$sort', ordering)
		if (offsetOf(it) > 0) stages += obj('$skip', num(offsetOf(it)))
		stages += obj('$limit', num(if (written(it, 'scrollLimit')) scrollLimit else 10))
		stages += shape(it, collection, target, false, false)
		#[new Code.Return(aggregateCall(target, arr(stages), options(it, null), false))]
	}

	def private Expr cursorValue(MongodbCollection target, String cursorText) {
		if (target.keyKind == 'number') {
			try {
				return num(Long.parseLong(cursorText))
			} catch (NumberFormatException e) {
				throw Unsupported.of('the cursor ' + cursorText + ' on an integer key')
			}
		}
		text(cursorText)
	}

	def private Entries sortEntry(MongodbCollection target, SortClause clause, boolean scored) {
		val order = num(if (clause.direction == ESortDirection.DESC) -1 else 1)
		if (clause.byRelevance) {
			if (!scored) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			return obj(SCORE, order)
		}
		val path = clause.field
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('sorting by a vector field')
		obj(fieldPath(target, path), order)
	}

	def dispatch List<Stmt> body(Sample it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		schema.note(it, 'consistency')
		val target = schema.collection(collection)
		val List<Expr> stages = newArrayList
		stages += obj('$match', matchOf(clauses(it, collection, target, filter, null, false)))
		if (written(it, 'method') && method == ESampleMethod.FIRST) {
			stages += obj('$sort', obj('_id', num(1)))
			stages += obj('$limit', num(sampleSize))
		} else {
			stages += obj('$sample', obj('size', num(sampleSize)))
		}
		stages += shape(it, collection, target, false, false)
		#[new Code.Return(aggregateCall(target, arr(stages), options(it, null), false))]
	}

	/** Qdrant's definition in one aggregation: a sample, each member with its nearest neighbours among the sample (exact similarity). */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		rejectClauses(it, 'asOf')
		queryExtensions(it)
		schema.note(it, 'consistency')
		val target = schema.collection(collection)
		val mapped = target.field(ModelUtil.fieldName(vectorFieldOf(collection, it.vectorField)))
		val limit = if (written(it, 'matrixLimit')) matrixLimit else 3
		val neighbours = obj('$map', obj('input', obj('$filter', obj('input', text('$docs'), 'as', text('b'), 'cond', obj('$ne', arr(text('$$b._id'), text('$$a._id'))))),
			'as', text('b'), 'in', obj('id', text('$$b._id'), SCORE, similarityOf(mapped.vector.metric, text('$$a.v'), text('$$b.v')))))
		val List<Expr> stages = newArrayList
		stages += obj('$match', matchOf(clauses(it, collection, target, filter, null, false)))
		stages += obj('$sample', obj('size', num(if (written(it, 'sampleSize')) sampleSize else 10)))
		stages += obj('$project', obj('v', text('$' + mapped.name)))
		stages += obj('$group', obj('_id', new Code.Nil, 'docs', obj('$push', text('$$ROOT'))))
		stages += obj('$project', obj('_id', num(0), 'rows', obj('$map', obj('input', text('$docs'), 'as', text('a'), 'in', obj('id', text('$$a._id'),
			'neighbours', obj('$slice', arr(obj('$sortArray', obj('input', neighbours, 'sortBy', obj(SCORE, num(-1), 'id', num(1)))), num(limit))))))))
		stages += obj('$unwind', text('$rows'))
		stages += obj('$replaceWith', text('$rows'))
		stages += obj('$sort', obj('id', num(1)))
		#[new Code.Return(aggregateCall(target, arr(stages), options(it, null), false))]
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

	// ---------------------------------------------------------------- pipelines

	/**
	 * The stages after a ranking stage that has set the score: bounds, the sort (score, else the sort clauses, key last), group-by,
	 * then either the page (skip, limit, projection) or, with with-total or aggregations, a $facet with the page, the total and the
	 * aggregations.
	 */
	def private Code.Call ranked(EObject statement, VectorCollection collection, MongodbCollection target, List<Expr> rankingStages, boolean scored,
		boolean textual, Entries aggregateOptions) {
		val List<Expr> stages = newArrayList(rankingStages)
		val scoreBounds = boundsOf(feature(statement, 'bounds') as ScoreBounds)
		if (scoreBounds !== null) {
			val Entries range = Code.entries
			if (scoreBounds.has('min')) range.put('$gte', num((scoreBounds.get('min') as Code.Num).value - 1e-9))
			if (scoreBounds.has('max')) range.put('$lte', num((scoreBounds.get('max') as Code.Num).value + 1e-9))
			stages += obj('$match', obj(SCORE, range))
		}
		val sortClauses = (feature(statement, 'sortClauses') ?: newArrayList) as List<SortClause>
		val Entries ordering = Code.entries
		for (clause : sortClauses) ordering.entries.putAll(sortEntry(target, clause, scored).entries)
		if (!ordering.has(SCORE)) ordering.put(SCORE, num(-1))
		ordering.put('_id', num(1))
		val offset = offsetOf(statement)
		val topK = feature(statement, 'topK') as Integer
		val List<Expr> page = newArrayList
		val group = groupOf(statement, target)
		if (group !== null) {
			val size = if (written(statement, 'groupSize')) feature(statement, 'groupSize') as Integer else 1
			page += obj('$sort', ordering)
			page += shape(statement, collection, target, scored, textual)
			page += obj('$group', obj('_id', text('$' + GROUP), 'hits', obj('$push', text('$$ROOT')), 'best', obj('$max', text('$' + SCORE))))
			page += obj('$sort', obj('best', num(-1), '_id', num(1)))
			if (offset > 0) page += obj('$skip', num(offset))
			page += obj('$limit', num(topK))
			page += obj('$project', obj('_id', num(0), 'group', text('$_id'), 'hits', obj('$slice', arr(obj('$sortArray', obj('input', text('$hits'),
				'sortBy', obj(SCORE, num(-1), target.key, num(1)))), num(size)))))
			page += obj('$unset', text('hits.' + GROUP))
		} else {
			page += obj('$sort', ordering)
			if (offset > 0) page += obj('$skip', num(offset))
			page += obj('$limit', num(topK))
			page += shape(statement, collection, target, scored, textual)
		}
		val withTotal = written(statement, 'withTotal')
		val aggregations = (feature(statement, 'aggregations') ?: newArrayList) as List<AggregationClause>
		if (!withTotal && aggregations.empty) {
			stages += page
			return aggregateCall(target, arr(stages), aggregateOptions, false)
		}
		val Entries facets = obj('hits', arr(page))
		val Entries result = obj('_id', num(0), 'hits', num(1))
		if (withTotal) {
			facets.put('total', arr(obj('$count', text('n'))))
			result.put('total', obj('$ifNull', arr(obj('$first', text('$total.n')), num(0))))
		}
		if (!aggregations.empty) {
			val Entries named = Code.entries
			for (clause : aggregations) {
				val aggregation = aggregationOf(target, clause)
				facets.put(aggregation.key, aggregation.value)
				named.put(aggregation.key, text('$' + aggregation.key))
			}
			result.put('aggregations', named)
		}
		stages += obj('$facet', facets)
		stages += obj('$project', result)
		aggregateCall(target, arr(stages), aggregateOptions, true)
	}

	/** One aggregation as a $facet pipeline over the matching documents, under its alias (else kind_field). */
	def private Pair<String, Expr> aggregationOf(MongodbCollection target, AggregationClause clause) {
		val path = if (clause.field.rawPath !== null) clause.field.rawPath else fieldPath(target, clause.field)
		val operand = text('$' + path)
		val size = if (written(clause, 'bucketCount')) clause.bucketCount else 10
		val Expr definition = switch clause.kind.literal {
			case 'count': arr(obj('$match', obj(path, obj('$exists', flag(true)))), obj('$count', text('value')))
			case 'sum',
			case 'avg',
			case 'min',
			case 'max': arr(obj('$group', obj('_id', new Code.Nil, 'value', obj('$' + clause.kind.literal, operand))), obj('$project', obj('_id', num(0))))
			case 'stats': arr(obj('$group', obj('_id', new Code.Nil, 'count', obj('$sum', obj('$cond', arr(obj('$isNumber', operand), num(1), num(0)))), 'min', obj('$min', operand),
				'max', obj('$max', operand), 'avg', obj('$avg', operand), 'sum', obj('$sum', operand))), obj('$project', obj('_id', num(0))))
			case 'percentiles': arr(obj('$group', obj('_id', new Code.Nil, 'value', obj('$percentile', obj('input', operand,
				'p', arr(num(0.01), num(0.05), num(0.25), num(0.5), num(0.75), num(0.95), num(0.99)), 'method', text('approximate'))))), obj('$project', obj('_id', num(0))))
			case 'cardinality',
			case 'distinct_count': arr(obj('$unwind', operand), obj('$group', obj('_id', operand)), obj('$count', text('value')))
			case 'terms',
			case 'facet': arr(obj('$unwind', operand), obj('$sortByCount', operand), obj('$limit', num(size)), obj('$project', obj('_id', num(0), 'key', text('$_id'), 'count', num(1))))
			case 'histogram': {
				if (clause.interval === null || !clause.interval.matches('[0-9]+(\\.[0-9]+)?')) throw Unsupported.of('a histogram without a numeric interval')
				val width = num(Double.parseDouble(clause.interval))
				arr(obj('$match', obj(path, obj('$exists', flag(true)))), obj('$group', obj('_id', obj('$multiply', arr(obj('$floor', obj('$divide', arr(operand, width))), width)),
					'count', obj('$sum', num(1)))), obj('$sort', obj('_id', num(1))), obj('$project', obj('_id', num(0), 'key', text('$_id'), 'count', num(1))))
			}
			case 'date_histogram': {
				val unit = dateUnit(clause.interval)
				arr(obj('$match', obj(path, obj('$exists', flag(true)))), obj('$group', obj('_id', obj('$dateTrunc', obj('date', operand, 'unit', text(unit.key), 'binSize', num(unit.value.doubleValue))),
					'count', obj('$sum', num(1)))), obj('$sort', obj('_id', num(1))), obj('$project', obj('_id', num(0), 'key', text('$_id'), 'count', num(1))))
			}
			default: throw Unsupported.of('range aggregations; vecDSL gives no ranges, which a $bucket stage needs as boundaries')
		}
		(clause.alias ?: clause.kind.literal + '_' + path.replace('.', '_')) -> definition
	}

	def private Pair<String, Integer> dateUnit(String interval) {
		if (interval === null) throw Unsupported.of('a date histogram without an interval')
		val matcher = java.util.regex.Pattern.compile('([0-9]+)(s|m|h|d|w|M|y)').matcher(interval.trim)
		if (!matcher.matches) throw Unsupported.of('the date interval ' + interval + '; $dateTrunc takes a count of seconds, minutes, hours, days, weeks, months or years (30m, 1d, 1M)')
		val unit = switch matcher.group(2) { case 's': 'second' case 'm': 'minute' case 'h': 'hour' case 'd': 'day' case 'w': 'week' case 'M': 'month' default: 'year' }
		unit -> Integer.valueOf(matcher.group(1))
	}

	/** The group-by path of a grouped search. */
	def private String groupOf(EObject statement, MongodbCollection target) {
		val path = feature(statement, 'groupBy') as FieldPath
		if (path === null) return null
		if (path.rawPath === null && !(path.field instanceof PayloadField)) throw Unsupported.of('grouping by a vector field')
		if (path.rawPath !== null) return path.rawPath
		val leafName = ModelUtil.fieldName(path.field)
		if (path.subFields.empty) target.field(leafName).name else fieldPath(target, path)
	}

	def private String fieldPath(MongodbCollection target, FieldPath path) {
		if (path.rawPath !== null) {
			for (segment : path.rawPath.split('\\.')) MongodbCommands.field(segment)
			return path.rawPath
		}
		val List<String> segments = newArrayList
		segments += target.path(ModelUtil.fieldName(path.field))
		segments += path.subFields.map[s | ModelUtil.fieldName(s)]
		segments.join('.')
	}

	/**
	 * The projection of the records: with an output list, an inclusion $project of the key (from _id), the score and the listed
	 * fields; without one, the key added and _id, the vectors (unless with-vectors), the hidden fields and the excluded fields
	 * removed. Highlights and score details come from the search metadata.
	 */
	def private List<Expr> shape(EObject statement, VectorCollection collection, MongodbCollection target, boolean scored, boolean textual) {
		val outputSpec = feature(statement, 'output') as OutputSpec
		val excluded = (feature(statement, 'excluded') ?: newArrayList) as List<Field>
		val withVectors = Boolean.TRUE == feature(statement, 'withVectors')
		val highlighted = textual && !((feature(statement, 'highlighted') ?: newArrayList) as List<Field>).empty
		val explained = textual && Boolean.TRUE == feature(statement, 'explain')
		val List<String> excludedNames = excluded.map[f | target.path(ModelUtil.fieldName(f))].toList
		val group = groupOf(statement, target)
		if (outputSpec !== null && !outputSpec.allFields) {
			val Entries projection = obj('_id', num(0), target.key, text('$_id'))
			if (scored) projection.put(SCORE, num(1))
			if (group !== null) projection.put(GROUP, text('$' + group))
			for (f : outputSpec.fields) {
				val fieldName = ModelUtil.fieldName(f)
				if (fieldName != target.key && !excludedNames.contains(target.path(fieldName))) projection.put(target.path(fieldName), num(1))
			}
			if (withVectors) for (v : target.vectorFields) if (!excludedNames.contains(v.name)) projection.put(v.name, num(1))
			if (highlighted) projection.put('highlights', obj('$meta', text('searchHighlights')))
			if (explained) projection.put('explanation', obj('$meta', text('searchScoreDetails')))
			return #[obj('$project', projection)]
		}
		val Entries removed = obj('_id', num(0))
		if (!withVectors) for (v : target.vectorFields) removed.put(v.name, num(0))
		for (hidden : target.hidden) removed.put(hidden, num(0))
		removed.put('vecdsl_order', num(0))
		for (name : excludedNames) if (name != '_id') removed.put(name, num(0))
		val Entries added = obj(target.key, text('$_id'))
		if (group !== null) added.put(GROUP, text('$' + group))
		if (highlighted) added.put('highlights', obj('$meta', text('searchHighlights')))
		if (explained) added.put('explanation', obj('$meta', text('searchScoreDetails')))
		#[obj('$addFields', added), obj('$project', removed)]
	}

	/** The spec of the shared result-shape module when a search writes a clause MongoDB has no stage for (boost, mmr), else null. */
	def private ShapeSpec moduled(QueryMethod statement) {
		if (written(statement, 'boost') || written(statement, 'mmr')) ShapeSpec.of(statement) else null
	}

	/** The number of records a search ranks for the shared result-shape module (its window rule). */
	def private int windowOf(ShapeSpec shapes) {
		Double.valueOf((shapes.window() as Code.Num).value()).intValue
	}

	/**
	 * The candidate window of the shared result-shape module after a ranking stage that has set the score: the window's best records
	 * by score, then key, with the fields the module reads (MongodbShapes); bounds, offset, sort, groups, totals and aggregations are
	 * the module's.
	 */
	def private Code.Call candidateWindow(MongodbCollection target, List<Expr> rankingStages, ShapeSpec shapes, boolean highlighted, boolean explained,
		Entries aggregateOptions) {
		val List<Expr> stages = newArrayList(rankingStages)
		stages += obj('$sort', obj(SCORE, num(-1), '_id', num(1)))
		stages += obj('$limit', shapes.window())
		stages += MongodbShapes.projection(shapes, target, highlighted, explained)
		call('aggregate', 'database', text(target.database), 'collection', text(target.name), 'pipeline', arr(stages), 'key', text(target.key),
			'options', aggregateOptions, 'candidates', flag(true))
	}

	/** rt.shape over a candidate window: the module's spec in the collection's names and the vector fields the candidates carry. */
	def private Code.Call shapedCall(Code.Call candidateSearch, ShapeSpec shapes, MongodbCollection target) {
		call('shape', 'candidates', candidateSearch, 'spec', MongodbShapes.spec(shapes, target), 'vectors', Code.texts(MongodbShapes.carried(shapes, target)))
	}

	def private Code.Call aggregateCall(MongodbCollection target, Code.Items pipeline, Entries aggregateOptions, boolean first) {
		call('aggregate', 'database', text(target.database), 'collection', text(target.name), 'pipeline', pipeline, 'key', text(target.key),
			'options', aggregateOptions, 'first', if (first) flag(true) else null)
	}

	// ---------------------------------------------------------------- helpers

	def private Object feature(EObject object, String featureName) {
		val slot = object.eClass.getEStructuralFeature(featureName)
		if (slot === null) null else object.eGet(slot)
	}

	def private int offsetOf(EObject statement) {
		if (written(statement, 'offset')) feature(statement, 'offset') as Integer else 0
	}

	def private Entries boundsOf(ScoreBounds scoreBounds) {
		if (scoreBounds === null) return null
		val result = obj('min', if (written(scoreBounds, 'min')) num(scoreBounds.min) else null, 'max', if (written(scoreBounds, 'max')) num(scoreBounds.max) else null)
		if (result.entries.empty) null else result
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

	/** A statement's filter, its keys and its tenant, namespace and partition conditions, as MQL conditions. */
	def private List<Expr> clauses(EObject statement, VectorCollection collection, MongodbCollection target, FilterBlock block, Expr keys, boolean vector) {
		val List<Expr> result = newArrayList
		if (keys !== null) result += obj('_id', obj('$in', keys))
		if (block !== null) result += new MongodbFilter(target, vector).query(block.condition)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result += obj(target.tenant, obj('$eq', text(tenant.name)))
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result += obj(target.namespace, obj('$eq', text(namespace as String)))
		val single = feature(statement, 'partition') as Partition
		val several = feature(statement, 'partitions') as List<Partition>
		val List<Expr> named = newArrayList
		if (single !== null) named += text(single.name)
		if (several !== null) for (p : several) named += text(p.name)
		if (!named.empty) {
			if (target.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
			result += obj(target.partition, obj('$in', arr(named)))
		}
		result
	}

	def private Entries matchOf(List<Expr> conditions) {
		if (conditions.empty) Code.entries else if (conditions.size == 1) conditions.head as Entries else obj('$and', arr(conditions))
	}

	/** Keys as written (integers stay numbers, strings stay strings): _id holds the typed key. */
	def private Expr idsOf(List<Literal> values) {
		if (values.empty) return null
		Code.items(values.map[v | Literals.of(v)].toList)
	}

	/** Rejects clauses MongoDB lacks, with their reason, and clauses not generated yet. */
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

	/** 'extension mongodb' blocks of queries carry aggregate options; extension items of other kinds are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == MongodbGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on MongoDB')
				}
			}
		}
	}

	/** The query vector: a vector literal or parameter (the runtimes send it as doubles), or for id input the stored vector (runtime). */
	def private Expr queryVectorOf(QueryInput queryInput, MongodbCollection.Field mapped) {
		switch queryInput.kind {
			case VECTOR: Literals.of(queryInput.value)
			case ID: new Code.Nil
			case SPARSE_VECTOR: throw Unsupported.of('sparse_vector input; vectorSearch indexes take dense vectors only')
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; a vectorSearch index compares one vector per document path')
			default: throw Unsupported.of(queryInput.kind.literal + ' input' + NO_MODELS)
		}
	}

	def private Expr keywordText(QueryInput queryInput) {
		if (queryInput === null || queryInput.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(queryInput.value instanceof StringLit || queryInput.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(queryInput.value)
	}

	def private Expr recordsOf(RecordSource recordSource, MongodbCollection target) {
		switch recordSource {
			FileSource: call('read_records', 'location', text(recordSource.location))
			InlineRecords: Code.items(recordSource.records.map[r | recordOf(r, target) as Expr].toList)
			ParamSource: Literals.of(recordSource.param)
		}
	}

	/** A record with the current field names; the key keeps its declared name (the runtime moves it to _id). */
	def private Entries recordOf(RecordLiteral recordLiteral, MongodbCollection target) {
		val Entries result = Code.entries
		for (entry : recordLiteral.entries) {
			val fieldName = ModelUtil.fieldName(entry.field)
			result.put(if (fieldName == target.key) target.key else target.field(fieldName).name, fieldValue(entry.field, entry.value))
		}
		result
	}

	/**
	 * A record or set value: a geo_point value (a "lat,lon" string, which only a parameter can carry) is marked $geoPoint, which
	 * the runtimes turn into a GeoJSON point, longitude first, for the 2dsphere index and $geoWithin.
	 */
	def private Expr fieldValue(Field declared, org.ftn.vecDSL.vecDSL.Literal value) {
		val converted = Literals.of(value)
		if (!(declared instanceof PayloadField) || (declared as PayloadField).type != org.ftn.vecDSL.vecDSL.EScalarDType.GEO_POINT || value instanceof NullLit) return converted
		obj(GEO_POINT, converted)
	}

}
