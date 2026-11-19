package org.ftn.vecDSL.generator.qdrant

import java.util.List
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
import org.ftn.vecDSL.vecDSL.ContextSearch
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
import org.ftn.vecDSL.vecDSL.EConflictPolicy
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EFusionType
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EListKind
import org.ftn.vecDSL.vecDSL.EMatchOperator
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ESampleMethod
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESnapshotScope
import org.ftn.vecDSL.vecDSL.ESortDirection
import org.ftn.vecDSL.vecDSL.ETextQueryType
import org.ftn.vecDSL.vecDSL.EUpdateMode
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.EmbeddingMigration
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.ExtParam
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
import org.ftn.vecDSL.vecDSL.MmrConfig
import org.ftn.vecDSL.vecDSL.Namespace
import org.ftn.vecDSL.vecDSL.OutputSpec
import org.ftn.vecDSL.vecDSL.ParamSource
import org.ftn.vecDSL.vecDSL.Partition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Prefetch
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

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.qdrant.QdrantSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Qdrant operations in the host-language-neutral code model. Every operation is a call
 * into the per-language runtime module, which holds the SDK calls; arguments are plain data in the JSON vocabulary of
 * the Qdrant REST API (collection layout, records, filters, search legs), so the three host languages send the same
 * requests.
 */
class QdrantStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; QdrantCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Qdrant: '
	static val PENDING = ' has no Qdrant mapping yet'
	static val NO_DATABASES = 'logical databases; Qdrant keeps all collections of a server in one namespace (it has no database API)'
	static val NO_MODELS = '; a self-hosted Qdrant has no model objects (inference runs in Qdrant Cloud)'
	static val NO_GENERATION = 'Qdrant has no generation API (its queries return points)'
	static val NO_ANALYZERS = 'Qdrant has no analyzer objects: each text index carries its tokenizer and token filters'
	static val METRICS = 'metrics configuration; Qdrant always serves Prometheus metrics at /metrics and configures telemetry in config.yaml'
	static val CLUSTER_LIMITS = 'cluster limits; Qdrant sets max_collections for the whole server in config.yaml (storage.max_collections)'
	static val SCHEDULES = 'backup schedules; Qdrant creates snapshots only on request'
	static val EXTERNAL = 'collections derived from external sources; Qdrant stores the points of every collection itself'
	static val STORAGE_LOCATIONS = 'snapshot locations; Qdrant writes snapshots to its snapshots path or to the S3, GCS or Azure storage configured on the server (storage.snapshots_config)'
	static val NO_LIMITS = 'Qdrant has no server-side field limits: the runtime checks the declared limits before each write'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'fuzziness' -> 'fuzziness', 'slop' -> 'slop', 'minShouldMatch' -> 'min-should-match', 'boost' -> 'boost',
		'queryModel' -> 'model', 'params' -> 'params'
	}

	/** Why Qdrant cannot express a query clause. */
	static val REASONS = #{
		'asOf' -> 'Qdrant reads only the current state of a collection',
		'cursor' -> 'Qdrant pages queries by offset and scrolls by point id, not by an opaque cursor',
		'withTotal' -> 'Qdrant returns no total hit count with query results',
		'explain' -> 'Qdrant returns no score explanation',
		'highlighted' -> 'Qdrant returns no highlighted fragments',
		'reranker' -> 'a self-hosted Qdrant has no rerankers (prefetch stages rescore instead)',
		'elementScope' -> 'Qdrant scores whole points, not array elements',
		'aggregations' -> 'Qdrant aggregates only as facet counts over a filter, not over search results',
		'sortClauses' -> 'Qdrant orders query results by score (order_by replaces the query)',
		'fuzziness' -> 'Qdrant text matching has no fuzzy terms',
		'slop' -> 'Qdrant phrase matching has no slop',
		'minShouldMatch' -> 'Qdrant BM25 queries have no minimum-should-match',
		'queryModel' -> 'a self-hosted Qdrant embeds no query text (inference runs in Qdrant Cloud)',
		'mmr' -> 'Qdrant applies MMR to nearest-vector queries only'
	}

	/** Clauses Qdrant 1.20 serves that the generator does not emit yet. */
	static val NATIVE_PENDING = #{'boost', 'groupBy'}

	static val DEFAULT_FUSION_K = 60
	static val ORDERINGS = #['weak', 'medium', 'strong']
	static val ORDER_INDEXES = #[EPayloadIndexKind.INTEGER, EPayloadIndexKind.FLOAT, EPayloadIndexKind.DATETIME]

	val Script script
	val List<String> notes = newArrayList
	val QdrantSchema schema = new QdrantSchema(notes)
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
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

	// ---------------------------------------------------------------- DDL

	def dispatch List<Stmt> body(Create it) {
		val List<Stmt> result = newArrayList
		result += define(definition ?: target, ifNotExists)
		for (statement : nested) result += body(statement)
		result
	}

	def List<Stmt> define(Concept concept, boolean ifNotExists) {
		val List<Stmt> result = newArrayList
		switch concept {
			VectorCollection: result += schema.createCollection(concept, ifNotExists)
			Connection: connection(concept)
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(METRICS)
				if (!concept.databases.empty) throw Unsupported.of(NO_DATABASES)
				if (concept.connection !== null) connection(concept.connection)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of(CLUSTER_LIMITS)
				for (name : ModelUtil.extParams(concept, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				if (concept.sharding !== null) QdrantSchema.shardNumber(concept.sharding)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists)
			}
			Database: throw Unsupported.of(NO_DATABASES)
			Alias: result += run(runtime('create_alias', kw('alias', text(concept.name), 'collection', text(concept.collection.name))))
			VectorIndex: result += indexSettings(concept)
			PayloadIndex: result += existingPayloadIndex(concept)
			VectorField: result += addField(member(concept, 'a vector field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += createShardKey(member(concept, 'a partition'), concept, ifNotExists)
			Tenant: result += tenantIndex(member(concept, 'a tenant'), false)
			Namespace: result += tenantIndex(member(concept, 'a namespace'), true)
			Function: result += addFunction(member(concept, 'a function'), concept)
			FileResource: throw Unsupported.of('file resources; Qdrant has no file resource objects (stop word sets are written inline in a text index)')
			Analyzer: schema.analyze(concept, kw())
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; ' + NO_ANALYZERS)
				schema.analyze(analyzer, kw())
			}
			FieldLimit: throw Unsupported.of('a field limit outside a field declaration; ' + NO_LIMITS)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers' + NO_MODELS)
			Quantization: result += quantizationSettings(concept, false)
			ReplicationConfig: result += replication(concept)
			ShardConfig: throw Unsupported.of('sharding of existing collections; Qdrant fixes shard_number when it creates a collection')
			StorageConfig: result += storageSettings(concept)
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of('role bindings' + PENDING)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits (' + concept.type.literal + '); Qdrant strict mode limits payload indexes, points and storage size, not partitions, fields, shards or vector dimensions')
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; the Qdrant TypeScript and Java clients connect to a server')
		}
		if (concept.apiKey instanceof StringLit || concept.token instanceof StringLit) {
			throw Unsupported.of('an API key written in the script; set the environment variable ' + QdrantPrinter.API_KEY_VARIABLE)
		}
		if (concept.databaseName !== null) throw Unsupported.of(NO_DATABASES)
		for (name : ModelUtil.extParams(concept, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** API key and token authentication are client settings: the connection sends the key from the environment. */
	def private void authentication(AuthConfig auth) {
		if (!#[EAuthType.NONE, EAuthType.API_KEY, EAuthType.TOKEN].contains(auth.type)) {
			throw Unsupported.of(auth.type.literal + ' authentication; Qdrant authenticates requests by API key or JWT')
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('an API key written in the script; set the environment variable ' + QdrantPrinter.API_KEY_VARIABLE)
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; Qdrant serves TLS but does not authenticate clients by certificate')
		if (auth.cmekKeyId !== null || auth.auditLog || auth.fieldEncryption) {
			throw Unsupported.of('customer-managed keys, audit logs and field encryption; a self-hosted Qdrant has none of them')
		}
		if (!auth.roles.empty) throw Unsupported.of('role bindings' + PENDING)
	}

	/**
	 * The collection of a member concept: its container, or the collection of the enclosing create. Qdrant adds named
	 * vectors, payload indexes, shard keys and tenant indexes only to a collection.
	 */
	def private VectorCollection member(EObject concept, String what) {
		val direct = ModelUtil.owningCollection(concept)
		if (direct !== null) return direct
		val statement = EcoreUtil2.getContainerOfType(concept, Create)
		val outer = if (statement === null) null else statement.eContainer
		if (outer instanceof Create) {
			val enclosing = (outer as Create).definition ?: (outer as Create).target
			if (enclosing instanceof VectorCollection) return enclosing as VectorCollection
		}
		throw Unsupported.of(what + ' outside a collection; Qdrant adds it only to a collection')
	}

	/** Qdrant creates payload indexes on existing collections. */
	def private List<Stmt> existingPayloadIndex(PayloadIndex index) {
		val field = index.target.field
		if (index.target.rawPath === null && !(field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val collection = ModelUtil.targetCollection(index) ?: ModelUtil.owningCollection(field)
		if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
		val args = kw('name', text(collection.name), 'field', text(QdrantSchema.indexPath(index.target)), 'schema', schema.payloadIndex(collection, index))
		newArrayList(new Code.Run(runtime('create_payload_index', args)) as Stmt)
	}

	/** A vector index on an existing collection: the named vector's HNSW and quantization settings, updated in place. */
	def private List<Stmt> indexSettings(VectorIndex index) {
		val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) {
			throw Unsupported.of('a vector index on sparse field ' + ModelUtil.fieldName(field) + '; Qdrant indexes sparse vectors with its inverted index')
		}
		val params = kw()
		schema.vectorIndex(index, params)
		if (params.entries.empty) params.put('hnsw_config', kw())
		#[run(runtime('alter_collection', kw('name', text(collection.name), 'vectors', kw(QdrantSchema.fieldKey(field), params))))]
	}

	/** Quantization of the named vector whose index declares it; drop disables it. */
	def private List<Stmt> quantizationSettings(Quantization quantization, boolean drop) {
		val index = EcoreUtil2.getContainerOfType(quantization, VectorIndex)
		if (index === null) throw Unsupported.of('quantization outside a vector index; Qdrant sets quantization per collection or named vector')
		val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
		if (field === null) throw Unsupported.of('quantization of an index without a vector field')
		val collection = member(field, 'quantization')
		val config = if (drop) text('Disabled') else schema.quantizationConfig(quantization)
		#[run(runtime('alter_collection', kw('name', text(collection.name), 'vectors',
			kw(QdrantSchema.fieldKey(field), kw('quantization_config', config ?: text('Disabled'))))))]
	}

	/** A replication factor of a cluster applies to each of its collections (Qdrant sets it per collection). */
	def private List<Stmt> replication(ReplicationConfig config) {
		val cluster = EcoreUtil2.getContainerOfType(config, Cluster)
		if (cluster === null) throw Unsupported.of('replication outside a cluster; Qdrant sets replication_factor per collection')
		for (name : ModelUtil.extParams(config, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val List<Stmt> result = newArrayList
		for (collection : cluster.collections) {
			result += run(runtime('alter_collection', kw('name', text(collection.name), 'params', kw('replication_factor', num(config.factor)))))
		}
		result
	}

	def private List<Stmt> storageSettings(StorageConfig storage) {
		val collection = ModelUtil.owningCollection(storage)
		if (collection === null) throw Unsupported.of('storage settings outside a collection; Qdrant places the storage of each collection')
		#[run(runtime('alter_collection', kw('name', text(collection.name), 'params',
			kw('payload', kw('memory', text(QdrantSchema.payloadMemory(storage)))))))]
	}

	def private List<Stmt> createShardKey(VectorCollection collection, Partition partition, boolean ifNotExists) {
		if (ModelUtil.partitions(collection).empty) {
			throw Unsupported.of('a partition of a collection declared without partitions; Qdrant adds shard keys only to collections created with custom sharding')
		}
		val args = kw('name', text(collection.name), 'shard_key', text(QdrantSchema.shardKey(partition)))
		val create = run(runtime('create_shard_key', args))
		if (!ifNotExists) return newArrayList(create)
		newArrayList(new Code.When(runtime('has_shard_key', args), true, #[create]) as Stmt)
	}

	/** Tenants and namespaces are values of a payload key under a tenant index; creating one ensures the index. */
	def private List<Stmt> tenantIndex(VectorCollection collection, boolean namespace) {
		val key = QdrantSchema.sliceKey(collection, namespace)
		#[run(runtime('create_payload_index', kw('name', text(collection.name), 'field', text(key), 'schema', QdrantSchema.sliceIndex)))]
	}

	/**
	 * A field added to an existing collection. A vector field becomes a named vector (Qdrant 1.20); payloads are
	 * schemaless, so a payload field needs only its implied indexes and the backfill of its default.
	 */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val name = text(collection.name)
		val List<Stmt> result = newArrayList
		switch field {
			VectorField: {
				if (QdrantSchema.isBm25Output(collection, field)) {
					throw Unsupported.of('a bm25 output field on its own; Qdrant scores BM25 over the text index of the input field')
				}
				val sparse = field.type == EVectorDType.SPARSE_FLOAT_VECTOR
				result += run(runtime('create_vector', kw('name', name, 'field', text(QdrantSchema.fieldKey(field)),
					if (sparse) 'sparse' else 'dense', schema.vectorParams(collection, field))))
			}
			PayloadField: {
				schema.checkPayloadField(field, field.fullText || field.analyzer !== null)
				val key = text(QdrantSchema.fieldKey(field))
				if (field.fullText || field.analyzer !== null) {
					result += run(runtime('create_payload_index', kw('name', name, 'field', key, 'schema', schema.textIndex(collection, field, null))))
				}
				if (field.partitionKey) {
					result += run(runtime('create_payload_index', kw('name', name, 'field', key, 'schema', QdrantSchema.sliceIndex)))
				}
				if (field.defaultValue !== null) result += run(runtime('fill_field', kw('name', name, 'field', key, 'value', Literals.of(field.defaultValue))))
				if (result.empty) result += run(runtime('require_collection', kw('name', name)))
			}
		}
		result
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; Qdrant derives point ids from it')
		val name = text(collection.name)
		val key = text(QdrantSchema.fieldKey(field))
		switch field {
			VectorField: {
				val function = ModelUtil.producingFunction(collection, field)
				if (function !== null && function.type == EFunctionType.BM25) return dropFunction(collection, function)
				#[run(runtime('drop_vector', kw('name', name, 'field', key)))]
			}
			default: #[run(runtime('drop_field', kw('name', name, 'field', key)))]
		}
	}

	def private List<Stmt> renameField(VectorCollection collection, RenameFieldOp operation) {
		val field = operation.field
		if (field === collection.primaryKey) throw Unsupported.of('renaming the primary key; Qdrant derives point ids from it')
		val op = if (field instanceof VectorField) 'rename_vector' else 'rename_field'
		#[new Code.Return(runtime(op, kw('collection', schema.layout(collection), 'field', text(QdrantSchema.fieldKey(field)),
			'new_name', text(operation.newName))))]
	}

	/** Collection properties: description and tags live in the collection metadata. */
	def private List<Stmt> setProperty(VectorCollection collection, SetPropertyOp operation) {
		val name = text(collection.name)
		val value = Literals.of(operation.value)
		switch operation.property {
			case DESCRIPTION: #[run(runtime('alter_collection', kw('name', name, 'metadata', kw('description', value))))]
			case TAGS: #[run(runtime('alter_collection', kw('name', name, 'metadata', kw('tags', value))))]
			case DYNAMIC: {
				if (!(value instanceof Code.Flag) || !(value as Code.Flag).value) {
					throw Unsupported.of('dynamic false; Qdrant payloads are schemaless and accept any key')
				}
				#[run(runtime('require_collection', kw('name', name)))]
			}
			case TTL: throw Unsupported.of('collection ttl; Qdrant points do not expire')
			case CONSISTENCY: throw Unsupported.of('consistency as a collection property; Qdrant takes read consistency per request (the generator sends the level declared on the collection)')
			case DELETION_PROTECTION: throw Unsupported.of('deletion-protection; Qdrant collections have no deletion guard')
		}
	}

	/** A bm25 function: Qdrant scores BM25 over the text index of each input field. */
	def private List<Stmt> addFunction(VectorCollection collection, Function function) {
		if (function.type != EFunctionType.BM25) {
			throw Unsupported.of(function.type.literal + ' functions; a self-hosted Qdrant computes BM25 over text indexes only (inference runs in Qdrant Cloud)')
		}
		val List<Stmt> result = newArrayList
		for (input : function.inputs) {
			if (!(input instanceof PayloadField)) throw Unsupported.of('bm25 input ' + ModelUtil.fieldName(input) + ', which is not a text field')
			val field = input as PayloadField
			schema.checkPayloadField(field, true)
			result += run(runtime('create_payload_index', kw('name', text(collection.name), 'field', text(QdrantSchema.fieldKey(field)),
				'schema', schema.textIndex(collection, field, null))))
		}
		result
	}

	/** Without its bm25 function a field keeps a text index without scoring if it is still full-text, else loses it. */
	def private List<Stmt> dropFunction(VectorCollection collection, Function function) {
		if (function.type != EFunctionType.BM25) {
			throw Unsupported.of(function.type.literal + ' functions; a self-hosted Qdrant computes BM25 over text indexes only (inference runs in Qdrant Cloud)')
		}
		val List<Stmt> result = newArrayList
		for (input : function.inputs) {
			val key = text(QdrantSchema.fieldKey(input))
			if (input instanceof PayloadField && ((input as PayloadField).fullText || (input as PayloadField).analyzer !== null)) {
				val params = schema.textIndex(collection, input as PayloadField, null)
				params.entries().remove('scoring')
				result += run(runtime('create_payload_index', kw('name', text(collection.name), 'field', key, 'schema', params)))
			} else {
				result += run(runtime('drop_payload_index', kw('name', text(collection.name), 'field', key)))
			}
		}
		result
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; Qdrant alters collections')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) result += run(runtime('rename_collection', kw('name', text(collection.name), 'new_name', text(newName))))
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: dropField(collection, operation.field)
			RenameFieldOp: renameField(collection, operation)
			SetPropertyOp: setProperty(collection, operation)
			AddAliasOp: #[run(runtime('create_alias', kw('alias', text(operation.newAlias.name), 'collection', text(collection.name))))]
			AddFunctionOp: addFunction(collection, operation.newFunction)
			DropFunctionOp: dropFunction(collection, operation.function)
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val name = text(concept.name)
				val drop = run(runtime('drop_collection', kw('name', name)))
				#[if (ifExists) new Code.When(runtime('has_collection', kw('name', name)), false, #[drop]) else drop]
			}
			Alias: #[run(runtime('drop_alias', kw('alias', text(concept.name))))]
			Partition: #[run(runtime('drop_shard_key', kw('name', text(member(concept, 'a partition').name), 'shard_key', text(concept.name))))]
			Tenant: dropSlice(member(concept, 'a tenant'), false, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), true, concept.key)
			VectorIndex: {
				val field = concept.field ?: EcoreUtil2.getContainerOfType(concept, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				#[run(runtime('alter_collection', kw('name', text(member(field, 'a vector index').name), 'vectors',
					kw(QdrantSchema.fieldKey(field), kw('hnsw_config', kw('m', num(0), 'payload_m', num(0)))))))]
			}
			PayloadIndex: {
				val collection = ModelUtil.targetCollection(concept) ?: ModelUtil.owningCollection(concept.target.field)
				if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
				#[run(runtime('drop_payload_index', kw('name', text(collection.name), 'field', text(QdrantSchema.indexPath(concept.target)))))]
			}
			VectorField: dropField(member(concept, 'a vector field'), concept)
			PayloadField: dropField(member(concept, 'a field'), concept)
			Function: dropFunction(member(concept, 'a function'), concept)
			Quantization: quantizationSettings(concept, true)
			Database: throw Unsupported.of(NO_DATABASES)
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Qdrant object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; Qdrant drops collections, not the server')
			Cluster: throw Unsupported.of('dropping a cluster; Qdrant drops collections, not the cluster')
			Analyzer: throw Unsupported.of('dropping an analyzer; ' + NO_ANALYZERS)
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; ' + NO_ANALYZERS)
			FieldLimit: throw Unsupported.of('dropping a field limit; ' + NO_LIMITS)
			FileResource: throw Unsupported.of('file resources; Qdrant has no file resource objects (stop word sets are written inline in a text index)')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers' + NO_MODELS)
			ReplicationConfig: throw Unsupported.of('dropping replication; Qdrant keeps a replication factor of at least 1 per collection')
			ShardConfig: throw Unsupported.of('dropping sharding; Qdrant fixes shard_number when it creates a collection')
			StorageConfig: throw Unsupported.of('dropping storage settings; Qdrant keeps a memory placement for every collection component')
			AuthConfig: throw Unsupported.of('dropping authentication; the API key of a self-hosted Qdrant is a server setting (service.api_key)')
			RoleBinding: throw Unsupported.of('role bindings' + PENDING)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('dropping a collection limit; Qdrant has no such limit (strict mode limits payload indexes, points and storage size)')
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** Dropping a tenant or namespace deletes its points. */
	def private List<Stmt> dropSlice(VectorCollection collection, boolean namespace, String value) {
		val where = kw('must', items(#[kw('key', text(QdrantSchema.sliceKey(collection, namespace)), 'match', kw('value', text(value)))]))
		#[run(runtime('delete_rows', kw('collection', schema.writeLayout(collection), 'filter', where, 'delete_all', flag(true), 'wait', flag(true))))]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		newArrayList(new Code.Run(runtime('swap_alias', kw('alias', text(alias.name), 'collection', text(newCollection.name)))) as Stmt)
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Qdrant points do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection), 'rows', rows(source))
		val upsert = written(it, 'onConflict') && #[EConflictPolicy.UPDATE, EConflictPolicy.REPLACE].contains(onConflict)
		if (written(it, 'onConflict') && !upsert) args.put('on_conflict', text(onConflict.literal))
		writeOptions(it, args)
		writeScope(it, collection, args)
		#[new Code.Return(runtime(if (upsert) 'upsert_rows' else 'insert_rows', args))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Qdrant points do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection), 'rows', rows(source))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		writeOptions(it, args)
		writeScope(it, collection, args)
		#[new Code.Return(runtime('upsert_rows', args))]
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Qdrant conditional writes test a payload filter (update_filter), not a point version')
		schema.note(it, 'recordCountHint')
		val where = new QdrantFilter().where(filter)
		sliceFilter(it, collection, where)
		val args = kw('collection', schema.writeLayout(collection))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (values !== null) args.put('values', record(values))
		if (source !== null) args.put('rows', rows(source))
		val updated = collection
		if (!removedFields.empty) args.put('remove', texts(removedFields.map[removable(updated, it)]))
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) args.put('mode', text('replace'))
		writeOptions(it, args)
		partitionOf(it, collection, args)
		arrayItems(it, args)
		#[new Code.Return(runtime('update_rows', args))]
	}

	/** array-append and array-remove take their items from the statement's set record. */
	def private void arrayItems(Update update, Entries args) {
		if (update.appendFields.empty && update.arrayRemoveFields.empty) return;
		if (update.values === null) throw Unsupported.of("array-append and array-remove without 'set'; the items come from the set record")
		val listed = update.values.entries.map[field].toList
		for (field : update.appendFields + update.arrayRemoveFields) {
			if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) {
				throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ', which is not an array field')
			}
			if (!listed.contains(field)) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ' without items in set')
		}
		if (!update.appendFields.empty) args.put('append', texts(update.appendFields.map[QdrantSchema.fieldKey(it)]))
		if (!update.arrayRemoveFields.empty) args.put('remove_items', texts(update.arrayRemoveFields.map[QdrantSchema.fieldKey(it)]))
	}

	def private String removable(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('removing the primary key')
		if (field instanceof VectorField) throw Unsupported.of('remove-fields on vector field ' + ModelUtil.fieldName(field) + '; Qdrant removes payload keys')
		QdrantSchema.fieldKey(field)
	}

	def dispatch List<Stmt> body(Delete it) {
		val where = new QdrantFilter().where(filter)
		sliceFilter(it, collection, where)
		val args = kw('collection', schema.writeLayout(collection))
		if (deleteAll) args.put('delete_all', flag(true))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		writeOptions(it, args)
		partitionOf(it, collection, args)
		if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		#[new Code.Return(runtime('delete_rows', args))]
	}

	/**
	 * Qdrant has no server-side import of data files: the runtime reads a local JSON or JSON Lines file and writes its
	 * records as an insert (or an upsert for on-conflict update and replace) in batches.
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Qdrant has no ingestion pipeline (the runtime imports a local file once)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; Qdrant has no server-side import of data files (the runtime reads local JSON files)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			throw Unsupported.of('import format ' + format.literal + '; the runtime reads JSON and JSON Lines files')
		}
		schema.note(it, 'parallelism')
		val args = kw('collection', schema.writeLayout(collection), 'location', text(location))
		if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		if (async) notes += 'async on BulkImport, which the runtime finishes before returning'
		writeOptions(it, args)
		writeScope(it, collection, args)
		#[new Code.Return(runtime('import_file', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of('multi-statement transactions; Qdrant applies each write on its own (a batch update is ordered, not atomic)')
	}

	/** 'wait' is Qdrant's own wait flag; the qdrant extension may set the write ordering (weak, medium, strong). */
	def private void writeOptions(EObject statement, Entries args) {
		schema.note(statement, 'writeConsistency')
		if (Boolean.TRUE == feature(statement, 'waitForPersistence')) args.put('wait', flag(true))
		for (entry : ModelUtil.extParams(statement, QdrantGenerator.VENDOR).entrySet) {
			if (entry.key != 'ordering') throw Unsupported.of('extension parameter ' + entry.key)
			val value = entry.value
			if (!(value instanceof StringLit) || !ORDERINGS.contains((value as StringLit).value)) {
				throw Unsupported.of('write ordering other than weak, medium or strong')
			}
			args.put('ordering', text((value as StringLit).value))
		}
	}

	/** Tenant and namespace values of a write go into the slice payload keys; the partition is the shard key. */
	def private void writeScope(EObject statement, VectorCollection collection, Entries args) {
		val slice = entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) slice.put(QdrantSchema.sliceKey(collection, false), text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) slice.put(QdrantSchema.sliceKey(collection, true), text(namespace))
		if (!slice.entries.empty) args.put('slice', slice)
		partitionOf(statement, collection, args)
	}

	/** The shard key of a write: its partition, or the collection's default partition. */
	def private void partitionOf(EObject statement, VectorCollection collection, Entries args) {
		val partition = (feature(statement, 'partition') as Partition)
			?: collection.defaultPartition ?: ModelUtil.partitions(collection).findFirst[isIsDefault]
		if (partition !== null) args.put('shard_key', text(QdrantSchema.shardKey(partition)))
		else if (!collection.partitions.empty) throw Unsupported.of('a write without a partition to a collection with partitions; Qdrant requires the shard key of every write (declare a default partition)')
	}

	/** Tenant and namespace of a statement as conditions on their payload keys. */
	def private void sliceFilter(EObject statement, VectorCollection collection, QdrantFilter where) {
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) where.where(kw('key', text(QdrantSchema.sliceKey(collection, false)), 'match', kw('value', text(tenant.name))))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) where.where(kw('key', text(QdrantSchema.sliceKey(collection, true)), 'match', kw('value', text(namespace))))
	}

	/** The partitions a query reads: their shard keys. */
	def private void readPartitions(EObject query, Entries args) {
		val partitions = (feature(query, 'partitions') ?: newArrayList) as List<Partition>
		if (!partitions.empty) args.put('shard_key', texts(partitions.map[QdrantSchema.shardKey(it)]))
	}

	// ---------------------------------------------------------------- operational

	/** Qdrant serves every collection; loading checks that it exists. */
	def dispatch List<Stmt> body(Load it) {
		for (partition : partitions) QdrantSchema.shardKey(partition)
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[run(runtime('require_collection', kw('name', text(collection.name))))]
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of('releasing a collection; Qdrant serves every collection and has no release (memory placement is set per component: cold, cached or pinned)')
	}

	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[run(runtime('flush', kw('name', text(collection.name))))]
	}

	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val args = kw('name', text(collection.name))
		if (written(it, 'targetSegments')) args.put('segments', num(targetSegments))
		if (async) args.put('wait', flag(false))
		#[run(runtime('compact', args))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = kw('name', text(collection.name))
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration; a self-hosted Qdrant embeds no data (inference runs in Qdrant Cloud)')
	}

	def dispatch List<Stmt> body(Snapshot it) {
		val extent = if (written(it, 'scope')) it.scope else ESnapshotScope.COLLECTION
		if (extent == ESnapshotScope.TENANT || tenant !== null) throw Unsupported.of('tenant snapshots; a Qdrant snapshot holds a whole collection, shard or storage')
		if (extent == ESnapshotScope.SHARD) throw Unsupported.of('shard snapshots' + PENDING)
		if (incremental) throw Unsupported.of('incremental snapshots; a Qdrant snapshot always holds the whole collection')
		if (location !== null) throw Unsupported.of(STORAGE_LOCATIONS)
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'description')
		val args = kw('name', text(collection.name), 'snapshot', text(labelOf(it)))
		if (extent == ESnapshotScope.FULL) args.put('scope', text('full'))
		#[new Code.Return(runtime('create_snapshot', args))]
	}

	/** The name Restore and DropSnapshot use: 'as', else the statement label, else the collection name. */
	def private static String labelOf(Snapshot snapshot) {
		snapshot.snapshotName ?: snapshot.name ?: snapshot.collection.name
	}

	/** The collection the script snapshots under a name; the runtime searches the server when the script does not. */
	def private String snapshotOwner(String snapshot) {
		if (script === null) return null
		for (statement : EcoreUtil2.eAllOfType(script, Snapshot)) {
			if (labelOf(statement) == snapshot) return statement.collection.name
		}
		null
	}

	def dispatch List<Stmt> body(Restore it) {
		if (written(it, 'scope') && it.scope == ESnapshotScope.FULL) {
			throw Unsupported.of('restoring a storage snapshot; Qdrant recovers storage snapshots only when it starts (--storage-snapshot)')
		}
		if (written(it, 'scope') && it.scope != ESnapshotScope.COLLECTION) throw Unsupported.of(it.scope.literal + ' restores; a Qdrant collection snapshot is recovered whole')
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val args = kw('name', text(collection.name), 'snapshot', text(snapshotName))
		val owner = snapshotOwner(snapshotName)
		if (location !== null) args.put('location', text(location))
		else if (owner !== null) args.put('source', text(owner))
		if (async) args.put('wait', flag(false))
		#[new Code.Return(runtime('restore_snapshot', args))]
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		if (location !== null) throw Unsupported.of(STORAGE_LOCATIONS)
		val args = kw('snapshot', text(snapshotName))
		val owner = snapshotOwner(snapshotName)
		if (owner !== null) args.put('source', text(owner))
		#[run(runtime('drop_snapshot', args))]
	}

	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of(NO_DATABASES)
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; a Qdrant recovery copies the segments of the snapshot')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; Qdrant snapshots record the current state only")
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[new Code.Return(runtime('clone_collection', kw('name', text(sourceCollection.name), 'target', text(targetName))))]
	}

	def dispatch List<Stmt> body(RebuildIndex it) {
		for (name : ModelUtil.extParams(it, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val definition = index
		switch definition {
			PayloadIndex: {
				val collection = ModelUtil.targetCollection(definition) ?: ModelUtil.owningCollection(definition.target.field)
				if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
				schema.note(it, 'concurrently')
				#[run(runtime('rebuild_payload_index', kw('name', text(collection.name), 'field', text(QdrantSchema.indexPath(definition.target)))))]
			}
			VectorIndex: throw Unsupported.of('rebuilding a vector index with unchanged parameters; Qdrant rebuilds HNSW when its parameters change (alter the index instead)')
			default: throw Unsupported.of(NOT_MAPPED + definition.eClass.name)
		}
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val args = kw('collection', schema.writeLayout(collection), 'delete_all', flag(true))
		if (namespace !== null) {
			args.put('filter', kw('must', items(#[kw('key', text(QdrantSchema.sliceKey(collection, true)), 'match', kw('value', text(namespace)))])))
		}
		args.put('wait', flag(true))
		#[new Code.Return(runtime('delete_rows', args))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = if (scope instanceof VectorCollection) scope as VectorCollection
		if (scope !== null && owner === null) throw Unsupported.of('listing in a ' + scope.eClass.name + '; Qdrant lists objects of the server or of a collection')
		val args = kw('kind', text(kind.literal))
		switch kind {
			case COLLECTIONS: if (owner !== null) throw Unsupported.of('listing collections in a collection')
			case DATABASES: throw Unsupported.of(NO_DATABASES)
			case TENANTS,
			case NAMESPACES: {
				if (owner === null) throw Unsupported.of('listing ' + kind.literal + ' without a collection; Qdrant keeps them as values of a payload key per collection')
				args.put('kind', text('slices'))
				args.put('key', text(QdrantSchema.sliceKey(owner, kind == EListKind.NAMESPACES)))
			}
			case INDEXES,
			case PARTITIONS,
			case SNAPSHOTS: if (owner === null) throw Unsupported.of('listing ' + kind.literal + ' without a collection; Qdrant keeps them per collection')
			case ALIASES: {}
		}
		if (owner !== null) args.put('scope', text(owner.name))
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('list_objects', args))]
	}

	def dispatch List<Stmt> body(AwaitIndex it) {
		val definition = index
		val args = switch definition {
			VectorIndex: {
				val field = definition.field ?: EcoreUtil2.getContainerOfType(definition, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				kw('name', text(member(field, 'a vector index').name), 'field', text(QdrantSchema.fieldKey(field)), 'kind', text('vector'))
			}
			PayloadIndex: {
				val collection = ModelUtil.targetCollection(definition) ?: ModelUtil.owningCollection(definition.target.field)
				if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
				kw('name', text(collection.name), 'field', text(QdrantSchema.indexPath(definition.target)), 'kind', text('payload'))
			}
			default: throw Unsupported.of(NOT_MAPPED + definition.eClass.name)
		}
		if (written(it, 'state')) args.put('state', text(state.literal))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'cursor', 'withTotal', 'explain', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'boost', 'queryModel')
		val field = searchField(collection, vectorField)
		val metric = indexMetric(collection, field)
		if (written(it, 'metric') && it.metric != metric) throw Unsupported.of('a query metric other than the metric of the vector field')
		val nativeScore = written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE
		val context = QdrantSchema.extensionItems(it).filter(ContextSearch).head
		val search = searchSettings(params, exact)
		if (context !== null) return discover(it, field, context, nativeScore, search)
		val List<Stmt> prelude = newArrayList
		val vector = queryVector(input, collection, field, 'query_vector', prelude)
		val args = kw('collection', layout(collection, it), 'field', text(QdrantSchema.fieldKey(field)), 'vector', vector,
			'limit', num(topK), 'metric', text(metric.literal))
		scoreBounds(bounds, nativeScore, args)
		common(it, collection, args, output, scoped(it, collection, new QdrantFilter().where(filter)))
		if (search !== null) args.put('params', search)
		if (mmr !== null) args.put('mmr', mmrConfig(mmr, field))
		if (!prefetches.empty) {
			val source = collection
			args.put('prefetch', items(prefetches.map[step | stage(step, source)]))
		}
		if (groupBy !== null) {
			if (written(it, 'offset')) throw Unsupported.of("group-by with offset; Qdrant pages groups by limit only")
			args.put('group_by', text(QdrantSchema.indexPath(groupBy)))
			if (written(it, 'groupSize')) args.put('group_size', num(groupSize))
		}
		readPartitions(it, args)
		queryExtensions(it)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('vector_search', args))
		result
	}

	/** Discovery or context search from the qdrant extension; its scores rank by the context pairs, so they stay native. */
	def private List<Stmt> discover(VectorSearch search, VectorField field, ContextSearch context, boolean nativeScore, Entries settings) {
		if (!nativeScore) throw Unsupported.of("discover without 'score native'; discovery scores count satisfied context pairs and have no similarity form")
		if (!search.prefetches.empty || search.mmr !== null) throw Unsupported.of('discover with prefetch or mmr')
		if (search.groupBy !== null) throw Unsupported.of('group-by' + PENDING + ' for discovery')
		val pairs = context.pairs.map[kw('positive', Literals.of(positive), 'negative', Literals.of(negative)) as Expr]
		val args = kw('collection', layout(search.collection, search), 'field', text(QdrantSchema.fieldKey(field)), 'context', items(pairs),
			'limit', num(search.topK))
		if (context.target !== null) args.put('target', Literals.of(context.target))
		scoreBounds(search.bounds, false, args)
		common(search, search.collection, args, search.output, scoped(search, search.collection, new QdrantFilter().where(search.filter)))
		if (settings !== null) args.put('params', settings)
		readPartitions(search, args)
		#[new Code.Return(runtime('discover', args))]
	}

	def private Entries mmrConfig(MmrConfig mmr, VectorField field) {
		if (mmr.vectorField !== null && mmr.vectorField !== field) throw Unsupported.of('mmr on another vector field than the search')
		val config = kw('diversity', num(1 - mmr.diversityLambda))
		if (written(mmr, 'candidates')) config.put('candidates_limit', num(mmr.candidates))
		config
	}

	/** A prefetch stage as a Qdrant prefetch: its own query, vector, limit, filter, parameters and nested stages. */
	def private Expr stage(Prefetch step, VectorCollection collection) {
		if (written(step, 'weight')) throw Unsupported.of('stage weights outside rrf fusion')
		if (step.input === null) throw Unsupported.of('a prefetch stage without input')
		val field = searchField(collection, step.vectorField)
		val metric = indexMetric(collection, field)
		if (written(step, 'metric') && step.metric != metric) throw Unsupported.of('a stage metric other than the metric of the vector field')
		val vector = switch step.input.kind {
			case VECTOR,
			case SPARSE_VECTOR,
			case MULTI_VECTOR: vectorValue(Literals.of(step.input.value))
			default: throw Unsupported.of(step.input.kind.literal + ' input in a prefetch stage')
		}
		val entry = kw('query', vector, 'using', text(QdrantSchema.fieldKey(field)), 'limit', num(step.stageLimit))
		val where = new QdrantFilter().where(step.filter)
		if (!where.empty) entry.put('filter', where.code)
		val search = searchSettings(step.params, false)
		if (search !== null) entry.put('params', search)
		if (!step.nested.empty) entry.put('prefetch', items(step.nested.map[stage(it, collection)]))
		entry
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'fuzziness', 'slop', 'groupBy', 'boost', 'minShouldMatch')
		if (input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search with ' + input.kind.literal + ' input')
		val query = Literals.of(input.value)
		val field = keywordField(collection, searchFields)
		val args = kw('collection', layout(collection, it), 'query', query, 'field', text(QdrantSchema.fieldKey(field)), 'limit', num(topK))
		val where = new QdrantFilter().where(filter)
		termFilters(it, field, query, where)
		val bm25 = schema.bm25Params(collection, field)
		if (bm25 !== null) args.put('bm25', bm25)
		if (bounds !== null) scoreBounds(bounds, false, args)
		common(it, collection, args, output, scoped(it, collection, where))
		searchParams(params)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('keyword_search', args))]
	}

	/** Operator 'and' and phrase queries become text conditions in the filter, beside the BM25 ranking. */
	def private void termFilters(EObject search, PayloadField field, Expr query, QdrantFilter where) {
		if (written(search, 'textQueryType')) {
			val queryType = feature(search, 'textQueryType') as ETextQueryType
			switch queryType {
				case MATCH: {}
				case PHRASE: where.where(kw('key', text(QdrantSchema.fieldKey(field)), 'match', kw('phrase', query)))
				default: throw Unsupported.of('text-query ' + queryType.literal + '; Qdrant ranks terms and filters phrases')
			}
		}
		if (written(search, 'matchOperator') && feature(search, 'matchOperator') == EMatchOperator.AND) {
			where.where(kw('key', text(QdrantSchema.fieldKey(field)), 'match', kw('text', query)))
		}
	}

	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'groupBy', 'boost')
		val fusionKind = if (written(it, 'fusion')) fusion else EFusionType.RRF
		val weights = legWeights(it, fusionKind)
		val shared = searchSettings(params, false)
		val List<Stmt> prelude = newArrayList
		val List<Expr> mapped = newArrayList
		var index = 0
		for (leg : legs) {
			val entry = switch leg {
				VectorLeg: vectorLeg(it, leg, 'query_vector_' + index, prelude, shared)
				KeywordLeg: keywordLeg(it, leg)
			}
			if (written(leg, 'legLimit')) entry.put('limit', num((feature(leg, 'legLimit') as Integer).doubleValue))
			if (weights !== null) entry.put('weight', num(weights.get(index)))
			mapped += entry
			index = index + 1
		}
		val args = kw('collection', layout(collection, it), 'legs', items(mapped), 'limit', num(topK), 'fusion', text(fusionKind.literal))
		if (fusionKind == EFusionType.RRF) args.put('k', num(if (written(it, 'fusionConstant')) fusionConstant else DEFAULT_FUSION_K))
		else if (written(it, 'fusionConstant')) throw Unsupported.of('fusion-k with ' + fusionKind.literal + ' fusion')
		if (bounds !== null) scoreBounds(bounds, false, args)
		if (written(it, 'offset')) args.put('offset', num(offset))
		projection(collection, args, output, excluded, withVectors)
		readPartitions(it, args)
		queryExtensions(it)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('hybrid_search', args))
		result
	}

	/** Leg weights for weighted and relative-score fusion; rrf is unweighted by definition. */
	def private List<Double> legWeights(HybridSearch search, EFusionType fusionKind) {
		val weighted = search.legs.filter[written(it, 'weight')].size
		if (fusionKind == EFusionType.RRF) {
			if (weighted > 0 || written(search, 'alpha')) throw Unsupported.of('leg weights or alpha with rrf fusion, which is unweighted')
			return null
		}
		if (written(search, 'alpha')) {
			if (search.legs.size != 2 || !(search.legs.head instanceof VectorLeg) || weighted > 0) {
				throw Unsupported.of('alpha on other than one vector leg followed by one other leg without weights')
			}
			return #[search.alpha, 1 - search.alpha]
		}
		if (weighted == 0) return search.legs.map[1.0]
		if (weighted != search.legs.size) throw Unsupported.of('weights on some hybrid legs only')
		search.legs.map[feature(it, 'weight') as Double]
	}

	/** Filter of a leg: the statement's filter, its tenant and namespace, and the leg's own filter. */
	def private QdrantFilter legFilter(HybridSearch search, EObject leg) {
		scoped(search, search.collection, new QdrantFilter().where(search.filter)).where(feature(leg, 'filter') as FilterBlock)
	}

	def private Entries vectorLeg(HybridSearch search, VectorLeg leg, String variable, List<Stmt> prelude, Entries shared) {
		val field = searchField(search.collection, leg.vectorField)
		val metric = indexMetric(search.collection, field)
		if (written(leg, 'metric') && leg.metric != metric) throw Unsupported.of('a query metric other than the metric of the vector field')
		val vector = queryVector(leg.input ?: search.input, search.collection, field, variable, prelude)
		val entry = kw('kind', text('vector'), 'field', text(QdrantSchema.fieldKey(field)), 'vector', vector, 'metric', text(metric.literal))
		val where = legFilter(search, leg)
		if (!where.empty) entry.put('filter', where.code)
		if (leg.bounds !== null) scoreBounds(leg.bounds, false, entry)
		val own = searchSettings(leg.params, leg.exact)
		if (own !== null) entry.put('params', own) else if (shared !== null) entry.put('params', shared)
		queryExtensions(leg)
		entry
	}

	def private Entries keywordLeg(HybridSearch search, KeywordLeg leg) {
		reject(leg, 'minShouldMatch')
		val keywordInput = leg.input ?: search.input
		if (keywordInput === null || keywordInput.kind != EQueryInputMode.TEXT) throw Unsupported.of('a keyword leg without text input')
		val query = Literals.of(keywordInput.value)
		val field = keywordField(search.collection, leg.searchFields)
		val entry = kw('kind', text('keyword'), 'field', text(QdrantSchema.fieldKey(field)), 'query', query)
		val where = legFilter(search, leg)
		termFilters(leg, field, query, where)
		if (!where.empty) entry.put('filter', where.code)
		val bm25 = schema.bm25Params(search.collection, field)
		if (bm25 !== null) entry.put('bm25', bm25)
		queryExtensions(leg)
		entry
	}

	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'boost', 'groupBy')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val field = searchField(collection, vectorField)
		val metric = indexMetric(collection, field)
		val args = kw('collection', layout(collection, it), 'field', text(QdrantSchema.fieldKey(field)),
			'positives', items(positives.map[example(it)]), 'limit', num(topK), 'metric', text(metric.literal))
		if (!negatives.empty) args.put('negatives', items(negatives.map[example(it)]))
		if (lookupCollection !== null && lookupCollection !== collection) args.put('lookup', text(lookupCollection.name))
		scoreBounds(bounds, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, scoped(it, collection, new QdrantFilter().where(filter)))
		val search = searchSettings(params, false)
		if (search !== null) args.put('params', search)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('recommend', args))]
	}

	def private Expr example(Literal literal) {
		val value = Literals.of(literal)
		if (literal instanceof ListLit) vectorValue(value) else value
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'ids', items(ids.map[Literals.of(it)]))
		projection(collection, args, output, newArrayList, withVectors)
		val where = scoped(it, collection, new QdrantFilter())
		if (!where.empty) args.put('filter', where.code)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it))
		val where = scoped(it, collection, new QdrantFilter().where(filter))
		if (!where.empty) args.put('filter', where.code)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'cursor', 'asOf')
		val args = kw('collection', layout(collection, it))
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		val where = new QdrantFilter().where(filter)
		if (written(it, 'sliceIndex')) where.where(kw('slice', kw('index', num(sliceIndex), 'total', num(sliceTotal))))
		common(it, collection, args, output, scoped(it, collection, where))
		val sorting = scrollOrder(collection, sortClauses)
		if (sorting !== null) args.put('order', sorting)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('scroll', args))]
	}

	/** Scroll order: one sort clause on a field with an integer, float or datetime index, else the integer key. */
	def private Entries scrollOrder(VectorCollection collection, List<SortClause> clauses) {
		if (clauses.size > 1) throw Unsupported.of('several sort clauses; Qdrant orders a scroll by one payload field')
		if (clauses.empty) {
			if (QdrantSchema.keyType(collection) == 'int') return kw('key', text(ModelUtil.fieldName(collection.primaryKey)))
			return null
		}
		val clause = clauses.head
		if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll')
		val path = QdrantSchema.indexPath(clause.field)
		val indexed = collection.indexes.filter(PayloadIndex).exists[QdrantSchema.indexPath(target) == path && ORDER_INDEXES.contains(kind)]
			|| (clause.field.field === collection.primaryKey && QdrantSchema.keyType(collection) == 'int')
		if (!indexed) throw Unsupported.of('sort on ' + path + ' without an integer, float or datetime payload index; Qdrant orders by indexed fields')
		val result = kw('key', text(path))
		if (clause.direction == ESortDirection.DESC) result.put('direction', text('desc'))
		result
	}

	/** Qdrant's random sample query; 'first' takes the first records in primary key order. */
	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'size', num(sampleSize))
		projection(collection, args, output, newArrayList, false)
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		val where = scoped(it, collection, new QdrantFilter().where(filter))
		if (!where.empty) args.put('filter', where.code)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('sample_rows', args))]
	}

	/** Qdrant's distance matrix (search matrix pairs): a random sample and the nearest neighbours of each sampled point. */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		val field = searchField(collection, vectorField)
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR || field.multiVector) {
			throw Unsupported.of('a distance matrix over ' + ModelUtil.fieldName(field) + '; Qdrant computes distance matrices over dense vectors')
		}
		val args = kw('collection', layout(collection, it), 'field', text(QdrantSchema.fieldKey(field)), 'metric', text(indexMetric(collection, field).literal))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		val where = scoped(it, collection, new QdrantFilter().where(filter))
		if (!where.empty) args.put('filter', where.code)
		readPartitions(it, args)
		queryExtensions(it)
		#[new Code.Return(runtime('distance_matrix', args))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		throw Unsupported.of('generative search; ' + NO_GENERATION)
	}

	def dispatch List<Stmt> body(BatchQuery it) {
		val List<Expr> calls = newArrayList
		val owner = current
		var index = 0
		for (query : queries) {
			val name = owner + '_query_' + index
			current = name
			helpers += new Program.Method(name, ModelUtil.parameterNames(query), body(query), newArrayList, newArrayList, true)
			calls += self(name)
			index = index + 1
		}
		current = owner
		#[new Code.Return(items(calls))]
	}

	def dispatch List<Stmt> body(Method it) {
		throw Unsupported.of(NOT_MAPPED + eClass.name)
	}

	// ---------------------------------------------------------------- helpers

	def private static Stmt run(Expr expr) {
		new Code.Run(expr)
	}

	def private Entries layout(VectorCollection collection, EObject query) {
		val result = schema.layout(collection)
		if (written(query, 'consistency')) {
			val level = schema.readConsistency(feature(query, 'consistency') as EConsistencyLevel)
			if (level === null) result.entries().remove('read_consistency') else result.put('read_consistency', text(level))
		}
		result
	}

	/** A query's filter with the conditions of its tenant and namespace. */
	def private QdrantFilter scoped(EObject query, VectorCollection collection, QdrantFilter where) {
		sliceFilter(query, collection, where)
		where
	}

	def private void common(EObject query, VectorCollection collection, Entries args, OutputSpec output, QdrantFilter where) {
		if (!where.empty) args.put('filter', where.code)
		if (written(query, 'offset')) args.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		projection(collection, args, output, excluded, Boolean.TRUE == feature(query, 'withVectors'))
	}

	/** Output fields (the key is always returned) and the named vectors to include. */
	def private void projection(VectorCollection collection, Entries args, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val key = collection.primaryKey
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val stored = ModelUtil.vectorFields(collection).filter[!QdrantSchema.isBm25Output(collection, it)].toList
		val listed = if (output === null || output.allFields) ModelUtil.payloadFields(collection) as List<? extends Field> else output.fields
		for (field : listed.filter[it !== key && !excluded.contains(it)]) {
			switch field {
				PayloadField: fields += QdrantSchema.fieldKey(field)
				VectorField: if (stored.contains(field)) vectors += QdrantSchema.fieldKey(field)
				else notes += 'output of ' + ModelUtil.fieldName(field) + ', which Qdrant derives from the text index'
			}
		}
		if (withVectors) vectors += stored.map[QdrantSchema.fieldKey(it)]
		args.put('output', texts(fields))
		if (!vectors.empty) args.put('vectors', texts(vectors.stream.distinct.toList))
	}

	/** Rejects clauses Qdrant lacks with their reason, and names the clauses it serves but the generator does not emit yet. */
	def private void reject(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) {
				val clause = "the '" + (CLAUSES.get(name) ?: name) + "' clause"
				if (NATIVE_PENDING.contains(name)) throw Unsupported.of(clause + PENDING)
				val reason = REASONS.get(name)
				throw Unsupported.of(if (reason === null) clause else clause + '; ' + reason)
			}
		}
	}

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	def private VectorField searchField(VectorCollection collection, VectorField explicit) {
		if (explicit !== null) {
			if (QdrantSchema.isBm25Output(collection, explicit)) {
				throw Unsupported.of('vector search on the bm25 output ' + ModelUtil.fieldName(explicit) + '; use keyword search')
			}
			return explicit
		}
		val candidates = ModelUtil.vectorFields(collection).filter[!QdrantSchema.isBm25Output(collection, it)].toList
		if (candidates.size != 1) throw Unsupported.of("vector search without 'using' on a collection with " + candidates.size + ' vector fields')
		candidates.head
	}

	def private EMetric indexMetric(VectorCollection collection, VectorField field) {
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) return EMetric.DOT
		schema.metric(field, ModelUtil.vectorIndex(collection, field))
	}

	def private Expr queryVector(QueryInput input, VectorCollection collection, VectorField field, String variable, List<Stmt> prelude) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR,
			case SPARSE_VECTOR,
			case MULTI_VECTOR: vectorValue(Literals.of(input.value))
			case ID: {
				val source = input.lookupCollection ?: collection
				prelude += new Code.Let(variable, runtime('lookup_vector', kw('collection', schema.layout(source),
					'field', text(QdrantSchema.fieldKey(field)), 'id', Literals.of(input.value))))
				new Code.Ref(variable)
			}
			case TEXT: throw Unsupported.of('text input for vector field ' + ModelUtil.fieldName(field) + ' (Qdrant Cloud Inference is not generated)')
			default: throw Unsupported.of(input.kind.literal + ' query input')
		}
	}

	/** Bounds on the canonical similarity; the runtime passes a lower bound to Qdrant as score_threshold. */
	def private void scoreBounds(ScoreBounds bounds, boolean nativeScore, Entries args) {
		if (nativeScore) args.put('native', flag(true))
		if (bounds === null) return;
		if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
		if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
	}

	/** The one text field a keyword query ranks by BM25; a bm25 function's output stands for its input. */
	def private PayloadField keywordField(VectorCollection collection, List<SearchFieldRef> refs) {
		val List<PayloadField> fields = newArrayList
		for (ref : refs) {
			if (written(ref, 'boost')) throw Unsupported.of('field boosts; a Qdrant text query ranks one field')
			val function = ModelUtil.producingFunction(collection, ref.field)
			val named = if (function !== null && function.type == EFunctionType.BM25) function.inputs else #[ref.field]
			for (field : named) {
				if (!schema.isFullText(collection, field)) throw Unsupported.of('keyword search on ' + ModelUtil.fieldName(field) + ', which is not a full-text field')
				if (!fields.contains(field)) fields += field as PayloadField
			}
		}
		if (refs.empty) fields += schema.fullTextFields(collection)
		if (fields.empty) throw Unsupported.of('keyword search on a collection without full-text fields')
		if (fields.size > 1) throw Unsupported.of('keyword search over several fields; a Qdrant text query ranks one text index')
		fields.head
	}

	/** Qdrant search parameters: candidates as hnsw_ef, exact and exhaustive as exact, quantization rescoring. */
	def private Entries searchSettings(SearchParams params, boolean exact) {
		val result = entries
		if (exact) result.put('exact', flag(true))
		if (params !== null) {
			if (written(params, 'targetRecall')) throw Unsupported.of('target-recall: Qdrant exposes no query-time recall target')
			if (written(params, 'candidatePool')) result.put('hnsw_ef', num(params.candidatePool))
			if (params.exhaustive) result.put('exact', flag(true))
			val quantization = entries
			if (params.rescore) quantization.put('rescore', flag(true))
			if (written(params, 'oversampling')) quantization.put('oversampling', num(params.oversampling))
			if (!quantization.entries.empty) result.put('quantization', quantization)
			schema.note(params, 'probes', 'scanBudget', 'timeoutMillis', 'filterStrategy')
			for (name : ModelUtil.extParams(params, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		}
		if (result.entries.empty) null else result
	}

	def private void searchParams(SearchParams params) {
		if (params === null) return;
		if (written(params, 'targetRecall')) throw Unsupported.of('target-recall: Qdrant exposes no query-time recall target')
		schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'timeoutMillis', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		for (name : ModelUtil.extParams(params, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** Query-level qdrant extensions: discover is mapped by the vector search; parameters and feedback are not generated. */
	def private void queryExtensions(EObject host) {
		for (item : QdrantSchema.extensionItems(host)) {
			switch item {
				ExtParam: throw Unsupported.of('extension parameter ' + (item.key ?: item.quotedKey))
				ContextSearch: if (!(host instanceof VectorSearch)) throw Unsupported.of('discover outside a vector search')
				default: throw Unsupported.of(item.eClass.name + '; Qdrant needs strategy parameters for it that vecDSL 1.0 cannot write')
			}
		}
	}

	def private Expr rows(RecordSource source) {
		switch source {
			FileSource: runtime('read_records', kw('location', text(source.location)))
			InlineRecords: items(source.records.map[record(it)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record) {
		val result = entries
		for (entry : record.entries) result.put(QdrantSchema.fieldKey(entry.field), vectorValue(Literals.of(entry.value)))
		result
	}

	/** Sparse vectors in Qdrant's form: parallel lists of indices and values. */
	def private Expr vectorValue(Expr value) {
		if (!(value instanceof Code.Sparse)) return value
		val weights = (value as Code.Sparse).weights()
		kw('indices', items(weights.keySet.map[num(it.doubleValue)].toList), 'values', items(weights.values.map[num(it)].toList))
	}
}
