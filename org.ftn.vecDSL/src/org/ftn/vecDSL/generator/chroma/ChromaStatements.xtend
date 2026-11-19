package org.ftn.vecDSL.generator.chroma

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
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EListKind
import org.ftn.vecDSL.vecDSL.EScalarDType
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
import org.ftn.vecDSL.vecDSL.KeywordSearch
import org.ftn.vecDSL.vecDSL.ListObjects
import org.ftn.vecDSL.vecDSL.Load
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.MetricsConfig
import org.ftn.vecDSL.vecDSL.Namespace
import org.ftn.vecDSL.vecDSL.OutputSpec
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
import org.ftn.vecDSL.vecDSL.SearchParams
import org.ftn.vecDSL.vecDSL.SetPropertyOp
import org.ftn.vecDSL.vecDSL.ShardConfig
import org.ftn.vecDSL.vecDSL.Snapshot
import org.ftn.vecDSL.vecDSL.SortClause
import org.ftn.vecDSL.vecDSL.StorageConfig
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
import org.ftn.vecDSL.vecDSL.VectorSearch
import org.ftn.vecDSL.vecDSL.VendorExtension

import static org.ftn.vecDSL.generator.chroma.ChromaSchema.kw
import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Chroma operations in the host-language-neutral code model. Every operation is a call into
 * the per-language runtime module, which holds the SDK or HTTP calls; arguments are plain data in the vocabulary of Chroma's
 * HTTP API (collection configuration, records, where and where_document filters), so the three host languages send the same
 * requests.
 */
class ChromaStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; ChromaCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Chroma: '
	public static val NO_MODELS = '; generated Chroma programs supply their own vectors (Chroma embedding functions run in the client)'
	public static val NO_RANKED_TEXT = 'the local Chroma server ranks by vector distance only (its executor does not implement the Search API, which ranks BM25 and sparse vectors in Chroma Cloud)'
	public static val NO_PARTITIONS = 'partitions; a Chroma collection has no partitions or shard keys'
	public static val NO_ALIASES = 'aliases; Chroma has no collection aliases'
	public static val NO_ANALYZERS = 'Chroma has no analyzers: the document is indexed by a fixed full-text index'
	static val NO_GENERATION = 'Chroma has no generation API (its queries return records)'
	static val NO_SNAPSHOTS = 'snapshots; Chroma has no snapshot or backup API (a server keeps its data in its persist path)'
	static val NO_AUTH = 'the open-source Chroma server has no authentication (chroma run allows every request)'
	static val SINGLE_NODE = 'the open-source Chroma server is a single node without replicas or shards'
	static val METRICS = 'metrics configuration; Chroma exports OpenTelemetry traces configured in its server config file (open_telemetry)'
	static val CLUSTER_LIMITS = 'cluster limits; Chroma has no server limits on collections'
	static val SCHEDULES = 'backup schedules; Chroma has no backups'
	static val EXTERNAL = 'collections derived from external sources; Chroma stores the records of every collection itself'
	static val NO_LIMITS = 'Chroma has no server-side field limits: the runtime checks the declared limits before each write'
	static val NO_INDEX_CHANGES = 'Chroma keeps an HNSW index and an inverted index on every metadata key; the HTTP API changes no index after creation'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'boost' -> 'boost', 'queryModel' -> 'model', 'exact' -> 'exact'
	}

	/** Why Chroma cannot express a query clause. */
	static val REASONS = #{
		'asOf' -> 'Chroma reads only the current state of a collection',
		'cursor' -> 'Chroma pages by limit and offset, not by a cursor',
		'withTotal' -> 'Chroma returns no total hit count with query results',
		'explain' -> 'Chroma returns no score explanation',
		'highlighted' -> 'Chroma returns no highlighted fragments',
		'reranker' -> 'the local Chroma server has no rerankers',
		'elementScope' -> 'Chroma metadata has no nested elements',
		'aggregations' -> 'the local Chroma server has no aggregations (the Search API is not implemented by its executor)',
		'sortClauses' -> 'Chroma orders query results by distance',
		'queryModel' -> 'generated Chroma programs embed no query text',
		'mmr' -> 'Chroma has no maximal marginal relevance',
		'boost' -> 'Chroma has no score boosting',
		'groupBy' -> 'the local Chroma server has no grouping (the Search API is not implemented by its executor)',
		'prefetches' -> 'Chroma runs one nearest-neighbour query per request, without stages',
		'exact' -> 'Chroma searches its HNSW graph and has no exhaustive search option'
	}

	val List<String> notes = newArrayList
	val ChromaSchema schema = new ChromaSchema(notes)
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
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
				if (concept.connection !== null) connection(concept.connection)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists)
				for (database : concept.databases) result += define(database, ifNotExists)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of(CLUSTER_LIMITS)
				if (concept.replication !== null || concept.sharding !== null) throw Unsupported.of('replication and sharding; ' + SINGLE_NODE)
				for (name : ModelUtil.extParams(concept, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists)
			}
			Database: {
				ChromaSchema.checkName(concept.name)
				val create = new Code.Run(runtime('create_database', kw('name', text(concept.name)))) as Stmt
				if (ifNotExists) result += new Code.When(runtime('has_database', kw('name', text(concept.name))), true, #[create])
				else result += create
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists)
			}
			Alias: throw Unsupported.of(NO_ALIASES)
			VectorIndex: result += indexSettings(concept)
			PayloadIndex: result += existingPayloadIndex(concept)
			VectorField: throw Unsupported.of('adding vector field ' + ModelUtil.fieldName(concept) + '; a Chroma collection holds one embedding per record')
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: throw Unsupported.of(NO_PARTITIONS)
			Tenant: result += ensureCollection(member(concept, 'a tenant'))
			Namespace: result += ensureCollection(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + NO_RANKED_TEXT)
			FileResource: throw Unsupported.of('file resources; Chroma has no file resource objects')
			Analyzer: throw Unsupported.of('analyzers; ' + NO_ANALYZERS)
			AnalyzerFilter: throw Unsupported.of('analyzer filters; ' + NO_ANALYZERS)
			FieldLimit: throw Unsupported.of('a field limit outside a field declaration; ' + NO_LIMITS)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; the local Chroma server has no rerankers')
			Quantization: throw Unsupported.of('quantization; the local Chroma server stores full-precision HNSW graphs')
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_NODE)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_NODE)
			StorageConfig: throw Unsupported.of('storage settings; Chroma keeps every collection in its persist path')
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_AUTH)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits (' + concept.type.literal + '); Chroma has no collection limits')
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; the Chroma TypeScript and Java clients connect to a server')
		}
		if (concept.apiKey !== null || concept.token !== null || concept.password !== null) {
			throw Unsupported.of('credentials; ' + NO_AUTH)
		}
		if (concept.databaseName !== null) ChromaSchema.checkName(concept.databaseName)
		for (name : ModelUtil.extParams(concept, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** Without credentials there is nothing to configure; every other authentication needs a server Chroma does not offer. */
	def private void authentication(AuthConfig auth) {
		if (auth.type != EAuthType.NONE) throw Unsupported.of(auth.type.literal + ' authentication; ' + NO_AUTH)
		if (auth.certificatePath !== null || auth.cmekKeyId !== null || auth.auditLog || auth.fieldEncryption || !auth.roles.empty) {
			throw Unsupported.of('certificates, customer-managed keys, audit logs, field encryption and roles; ' + NO_AUTH)
		}
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
		throw Unsupported.of(what + ' outside a collection; Chroma adds it only to a collection')
	}

	/** A payload index on an existing collection: Chroma already keeps an inverted index on every metadata key. */
	def private List<Stmt> existingPayloadIndex(PayloadIndex index) {
		val field = index.target.field
		val collection = ModelUtil.targetCollection(index) ?: (if (field === null) null else ModelUtil.owningCollection(field))
		if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
		schema.payloadIndex(collection, index)
		#[run(runtime('require_collection', schema.place(collection)))]
	}

	/** A vector index on an existing collection: the HNSW parameters Chroma lets a collection change. */
	def private List<Stmt> indexSettings(VectorIndex index) {
		val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		if (written(index, 'buildCandidates')) throw Unsupported.of('build-candidates on an existing collection; Chroma fixes ef_construction when it creates a collection')
		if (written(index, 'metric') && index.metric != schema.metric(field, ModelUtil.vectorIndex(collection, field))) {
			throw Unsupported.of('a metric change; Chroma fixes the HNSW space when it creates a collection')
		}
		val graph = schema.hnsw(index)
		if (graph.entries.empty) return #[run(runtime('require_collection', schema.place(collection)))]
		val args = schema.place(collection)
		args.put('hnsw', graph)
		#[run(runtime('alter_collection', args))]
	}

	/** Tenants and namespaces are values of a metadata key; creating one needs only the collection. */
	def private List<Stmt> ensureCollection(VectorCollection collection) {
		#[run(runtime('require_collection', schema.place(collection)))]
	}

	/** A payload field added to an existing collection: metadata is schemaless, so only a default needs writing. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		switch field {
			VectorField: throw Unsupported.of('adding vector field ' + ModelUtil.fieldName(field) + '; a Chroma collection holds one embedding per record')
			PayloadField: {
				schema.checkPayloadField(collection, field)
				if (field.documentBody || field.fullText || field.analyzer !== null) {
					throw Unsupported.of('adding document field ' + ModelUtil.fieldName(field) + '; the generator takes the document from the collection declaration')
				}
				if (field.defaultValue !== null) {
					return #[run(runtime('fill_field', kw('collection', schema.writeLayout(collection), 'field',
						text(ModelUtil.fieldName(field)), 'value', Literals.of(field.defaultValue))))]
				}
				#[run(runtime('require_collection', schema.place(collection)))]
			}
		}
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; Chroma record ids are the key text')
		if (field instanceof VectorField) throw Unsupported.of('dropping vector field ' + ModelUtil.fieldName(field) + '; every Chroma record has an embedding')
		if (field === schema.documentField(collection)) throw Unsupported.of('dropping the document field; the generator takes the document from the collection declaration')
		#[run(runtime('drop_field', kw('collection', schema.writeLayout(collection), 'field', text(ModelUtil.fieldName(field)))))]
	}

	def private List<Stmt> renameField(VectorCollection collection, RenameFieldOp operation) {
		val field = operation.field
		if (field === collection.primaryKey) throw Unsupported.of('renaming the primary key; Chroma record ids are the key text')
		if (field instanceof VectorField || field === schema.documentField(collection)) {
			throw Unsupported.of('renaming the embedding or the document; Chroma stores them without a field name')
		}
		#[run(runtime('rename_field', kw('collection', schema.writeLayout(collection), 'field', text(ModelUtil.fieldName(field)),
			'new_name', text(operation.newName))))]
	}

	/** Collection properties: description and tags live in the collection metadata. */
	def private List<Stmt> setProperty(VectorCollection collection, SetPropertyOp operation) {
		val value = Literals.of(operation.value)
		val args = schema.place(collection)
		switch operation.property {
			case DESCRIPTION: {
				args.put('metadata', kw('description', value))
				#[run(runtime('alter_collection', args))]
			}
			case TAGS: {
				args.put('metadata', kw('tags', value))
				#[run(runtime('alter_collection', args))]
			}
			case DYNAMIC: {
				if (!(value instanceof Code.Flag) || !(value as Code.Flag).value) {
					throw Unsupported.of('dynamic false; Chroma metadata is schemaless and accepts any key')
				}
				#[run(runtime('require_collection', args))]
			}
			case CONSISTENCY: #[run(runtime('require_collection', args))]
			case TTL: throw Unsupported.of('collection ttl; Chroma records do not expire')
			case DELETION_PROTECTION: throw Unsupported.of('deletion-protection; Chroma collections have no deletion guard')
		}
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; Chroma alters collections')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) {
			ChromaSchema.checkName(newName)
			val args = schema.place(collection)
			args.put('new_name', text(newName))
			result += run(runtime('rename_collection', args))
		}
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: dropField(collection, operation.field)
			RenameFieldOp: renameField(collection, operation)
			SetPropertyOp: setProperty(collection, operation)
			AddAliasOp: throw Unsupported.of(NO_ALIASES)
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + NO_RANKED_TEXT)
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + NO_RANKED_TEXT)
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val drop = run(runtime('drop_collection', schema.place(concept)))
				#[if (ifExists) new Code.When(runtime('has_collection', schema.place(concept)), false, #[drop]) else drop]
			}
			Database: {
				val drop = run(runtime('drop_database', kw('name', text(concept.name))))
				#[if (ifExists) new Code.When(runtime('has_database', kw('name', text(concept.name))), false, #[drop]) else drop]
			}
			Tenant: dropSlice(member(concept, 'a tenant'), false, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), true, concept.key)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: throw Unsupported.of('dropping vector field ' + ModelUtil.fieldName(concept) + '; every Chroma record has an embedding')
			Alias: throw Unsupported.of(NO_ALIASES)
			Partition: throw Unsupported.of(NO_PARTITIONS)
			VectorIndex: throw Unsupported.of('dropping a vector index; ' + NO_INDEX_CHANGES)
			PayloadIndex: throw Unsupported.of('dropping a payload index; ' + NO_INDEX_CHANGES)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + NO_RANKED_TEXT)
			Quantization: throw Unsupported.of('quantization; the local Chroma server stores full-precision HNSW graphs')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Chroma object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; Chroma drops databases and collections, not the server')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE_NODE)
			Analyzer: throw Unsupported.of('dropping an analyzer; ' + NO_ANALYZERS)
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; ' + NO_ANALYZERS)
			FieldLimit: throw Unsupported.of('dropping a field limit; ' + NO_LIMITS)
			FileResource: throw Unsupported.of('file resources; Chroma has no file resource objects')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; the local Chroma server has no rerankers')
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_NODE)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_NODE)
			StorageConfig: throw Unsupported.of('storage settings; Chroma keeps every collection in its persist path')
			AuthConfig: throw Unsupported.of('authentication; ' + NO_AUTH)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_AUTH)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits; Chroma has no collection limits')
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** Dropping a tenant or namespace deletes its records. */
	def private List<Stmt> dropSlice(VectorCollection collection, boolean namespace, String value) {
		val where = kw('where', kw(ChromaSchema.sliceKey(collection, namespace), kw('$eq', text(value))))
		#[new Code.Return(runtime('delete_rows', kw('collection', schema.writeLayout(collection), 'filter', where, 'delete_all', flag(true))))]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		throw Unsupported.of(NO_ALIASES)
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Chroma records do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection), 'rows', rows(source))
		val upsert = written(it, 'onConflict') && #[EConflictPolicy.UPDATE, EConflictPolicy.REPLACE].contains(onConflict)
		if (written(it, 'onConflict') && !upsert) args.put('on_conflict', text(onConflict.literal))
		writeOptions(it)
		writeScope(it, collection, args)
		#[new Code.Return(runtime(if (upsert) 'upsert_rows' else 'insert_rows', args))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Chroma records do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection), 'rows', rows(source))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		writeOptions(it)
		writeScope(it, collection, args)
		#[new Code.Return(runtime('upsert_rows', args))]
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Chroma records carry no version')
		schema.note(it, 'recordCountHint')
		noPartition(it)
		val updated = collection
		val where = filterOf(it, updated, filter)
		val args = kw('collection', schema.writeLayout(updated))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (values !== null) args.put('values', record(values))
		if (source !== null) args.put('rows', rows(source))
		if (!removedFields.empty) args.put('remove', texts(removedFields.map[field | removable(updated, field)]))
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) args.put('mode', text('replace'))
		writeOptions(it)
		arrayItems(it, args)
		#[new Code.Return(runtime('update_rows', args))]
	}

	/** array-append and array-remove take their items from the statement's set record. */
	def private void arrayItems(Update update, Entries args) {
		if (update.appendFields.empty && update.arrayRemoveFields.empty) return;
		if (update.values === null) throw Unsupported.of("array-append and array-remove without 'set'; the items come from the set record")
		val listed = update.values.entries.map[entry | entry.field].toList
		for (field : update.appendFields + update.arrayRemoveFields) {
			if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) {
				throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ', which is not an array field')
			}
			if (!listed.contains(field)) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ' without items in set')
		}
		if (!update.appendFields.empty) args.put('append', texts(update.appendFields.map[ModelUtil.fieldName(it)]))
		if (!update.arrayRemoveFields.empty) args.put('remove_items', texts(update.arrayRemoveFields.map[ModelUtil.fieldName(it)]))
	}

	def private String removable(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('removing the primary key')
		if (field instanceof VectorField) throw Unsupported.of('remove-fields on vector field ' + ModelUtil.fieldName(field) + '; every Chroma record has an embedding')
		if (field === schema.documentField(collection)) throw Unsupported.of('remove-fields on the document; Chroma replaces a document but does not remove it')
		ModelUtil.fieldName(field)
	}

	def dispatch List<Stmt> body(Delete it) {
		noPartition(it)
		val where = filterOf(it, collection, filter)
		val args = kw('collection', schema.writeLayout(collection))
		if (deleteAll) args.put('delete_all', flag(true))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		writeOptions(it)
		if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		#[new Code.Return(runtime('delete_rows', args))]
	}

	/**
	 * Chroma has no server-side import of data files: the runtime reads a local JSON or JSON Lines file and writes its
	 * records as an insert (or an upsert for on-conflict update and replace) in batches.
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Chroma has no ingestion pipeline (the runtime imports a local file once)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; Chroma has no server-side import of data files (the runtime reads local JSON files)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			throw Unsupported.of('import format ' + format.literal + '; the runtime reads JSON and JSON Lines files')
		}
		schema.note(it, 'parallelism')
		val args = kw('collection', schema.writeLayout(collection), 'location', text(location))
		if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		if (async) notes += 'async on BulkImport, which the runtime finishes before returning'
		writeOptions(it)
		writeScope(it, collection, args)
		#[new Code.Return(runtime('import_file', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of('multi-statement transactions; Chroma applies each write request on its own')
	}

	/** Chroma applies a write to its log and indexes before it acknowledges it, so 'wait' needs no call; extensions are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (name : ModelUtil.extParams(statement, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	def private void noPartition(EObject statement) {
		if (feature(statement, 'partition') !== null) throw Unsupported.of(NO_PARTITIONS)
	}

	/** Tenant and namespace values of a write go into the slice metadata keys. */
	def private void writeScope(EObject statement, VectorCollection collection, Entries args) {
		noPartition(statement)
		val slice = entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) slice.put(ChromaSchema.sliceKey(collection, false), text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) slice.put(ChromaSchema.sliceKey(collection, true), text(namespace))
		if (!slice.entries.empty) args.put('slice', slice)
	}

	/** A statement's filter with the conditions of its tenant and namespace. */
	def private ChromaFilter filterOf(EObject statement, VectorCollection collection, FilterBlock block) {
		val result = new ChromaFilter(collection, schema.documentField(collection)).where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.where(kw(ChromaSchema.sliceKey(collection, false), kw('$eq', text(tenant.name))))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.where(kw(ChromaSchema.sliceKey(collection, true), kw('$eq', text(namespace))))
		result
	}

	// ---------------------------------------------------------------- operational

	/** Chroma serves every collection; loading checks that it exists. */
	def dispatch List<Stmt> body(Load it) {
		if (!partitions.empty) throw Unsupported.of(NO_PARTITIONS)
		for (name : ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[run(runtime('require_collection', schema.place(collection)))]
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of('releasing a collection; Chroma loads HNSW segments on demand and has no release call')
	}

	/** The local server commits every write to its log and applies it to the segments before it answers. */
	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[run(runtime('require_collection', schema.place(collection)))]
	}

	/** The local server compacts the log into the segments with every write, so a compaction has nothing left to do. */
	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'targetSegments', 'async')
		#[run(runtime('require_collection', schema.place(collection)))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = schema.place(collection)
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration; generated Chroma programs embed no data')
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

	/** Forking is a Chroma Cloud feature; the runtime copies the records into a new collection with the same configuration. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; Chroma copies collections')
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; the local Chroma server cannot fork a collection (Chroma Cloud only)')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; Chroma reads only the current state of a collection")
		for (name : ModelUtil.extParams(it, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		ChromaSchema.checkName(targetName)
		#[new Code.Return(runtime('clone_collection', kw('collection', schema.layout(sourceCollection), 'target', text(targetName))))]
	}

	def dispatch List<Stmt> body(RebuildIndex it) {
		throw Unsupported.of('rebuilding an index; Chroma maintains its indexes with every write and has no rebuild call')
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val args = kw('collection', schema.writeLayout(collection), 'delete_all', flag(true))
		if (namespace !== null) {
			args.put('filter', kw('where', kw(ChromaSchema.sliceKey(collection, true), kw('$eq', text(namespace)))))
		}
		#[new Code.Return(runtime('delete_rows', args))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val args = kw('kind', text(kind.literal))
		val owner = scope
		switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) args.put('scope', kw('database', text(owner.name)))
				else if (owner !== null) throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; Chroma lists the collections of a database')
			}
			case DATABASES: if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
			case TENANTS,
			case NAMESPACES: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection; they are values of a metadata key per collection')
				val collection = owner as VectorCollection
				args.put('kind', text('slices'))
				args.put('key', text(ChromaSchema.sliceKey(collection, kind == EListKind.NAMESPACES)))
				args.put('scope', schema.layout(collection))
			}
			case INDEXES: throw Unsupported.of('listing indexes; Chroma indexes are not named objects')
			case PARTITIONS: throw Unsupported.of(NO_PARTITIONS)
			case ALIASES: throw Unsupported.of(NO_ALIASES)
			case SNAPSHOTS: throw Unsupported.of(NO_SNAPSHOTS)
		}
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('list_objects', args))]
	}

	/** The local server indexes every write before it answers, so an index is ready as soon as its collection exists. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		val definition = index
		val collection = switch definition {
			VectorIndex: {
				val field = definition.field ?: EcoreUtil2.getContainerOfType(definition, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				member(field, 'a vector index')
			}
			PayloadIndex: {
				val field = definition.target.field
				ModelUtil.targetCollection(definition) ?: (if (field === null) null else ModelUtil.owningCollection(field))
			}
			default: throw Unsupported.of(NOT_MAPPED + definition.eClass.name)
		}
		if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
		schema.note(it, 'state', 'timeoutSeconds')
		#[new Code.Return(runtime('await_index', schema.place(collection)))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'cursor', 'withTotal', 'explain', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'boost', 'queryModel', 'mmr', 'prefetches', 'groupBy', 'exact')
		val field = schema.vectorField(collection)
		if (vectorField !== null && vectorField !== field) throw Unsupported.of('a vector field outside the collection')
		val metric = schema.metric(field, ModelUtil.vectorIndex(collection, field))
		if (written(it, 'metric') && it.metric != metric) throw Unsupported.of('a query metric other than the metric of the vector field; Chroma fixes the HNSW space per collection')
		searchParams(params)
		queryExtensions(it)
		val List<Stmt> prelude = newArrayList
		val args = kw('collection', schema.layout(collection), 'vector', queryVector(input, collection, prelude), 'limit', num(topK))
		scoreBounds(bounds, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, filterOf(it, collection, filter))
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('vector_search', args))
		result
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		throw Unsupported.of('keyword search; ' + NO_RANKED_TEXT)
	}

	def dispatch List<Stmt> body(HybridSearch it) {
		throw Unsupported.of('hybrid search; ' + NO_RANKED_TEXT)
	}

	/** The average-vector strategy over example records or vectors, computed in the runtime. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'boost', 'groupBy')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val field = schema.vectorField(collection)
		if (vectorField !== null && vectorField !== field) throw Unsupported.of('a vector field outside the collection')
		searchParams(params)
		queryExtensions(it)
		val args = kw('collection', schema.layout(collection), 'positives', items(positives.map[Literals.of(it)]), 'limit', num(topK))
		if (!negatives.empty) args.put('negatives', items(negatives.map[Literals.of(it)]))
		if (lookupCollection !== null && lookupCollection !== collection) args.put('lookup', schema.layout(lookupCollection))
		scoreBounds(bounds, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, filterOf(it, collection, filter))
		#[new Code.Return(runtime('recommend', args))]
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		noPartitions(it)
		queryExtensions(it)
		val args = kw('collection', schema.layout(collection), 'ids', items(ids.map[Literals.of(it)]))
		projection(collection, args, output, newArrayList, withVectors)
		val where = filterOf(it, collection, null)
		if (!where.empty) args.put('filter', where.code)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		noPartitions(it)
		queryExtensions(it)
		val args = kw('collection', schema.layout(collection))
		val where = filterOf(it, collection, filter)
		if (!where.empty) args.put('filter', where.code)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'cursor', 'asOf')
		if (written(it, 'sliceIndex')) throw Unsupported.of('scroll slices; Chroma pages by limit and offset only')
		noPartitions(it)
		queryExtensions(it)
		val args = kw('collection', schema.layout(collection))
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		common(it, collection, args, output, filterOf(it, collection, filter))
		if (!sortClauses.empty) {
			val source = collection
			args.put('order', items(sortClauses.map[clause | sortKey(source, clause)]))
		}
		#[new Code.Return(runtime('scroll', args))]
	}

	/** A sort clause of a scroll, applied in the runtime: Chroma returns records in insertion order. */
	def private Expr sortKey(VectorCollection collection, SortClause clause) {
		if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll')
		val path = clause.field
		if (path.rawPath !== null || !path.subFields.empty || !(path.field instanceof PayloadField) || path.field === schema.documentField(collection)) {
			throw Unsupported.of('sorting by ' + (if (path.field === null) 'a raw path' else ModelUtil.fieldName(path.field)) + '; the runtime sorts by metadata fields')
		}
		val result = kw('key', text(ModelUtil.fieldName(path.field)))
		if (clause.direction == ESortDirection.DESC) result.put('direction', text('desc'))
		result
	}

	/** A random sample of the matching records; 'first' takes the first records in primary key order. */
	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		noPartitions(it)
		queryExtensions(it)
		val args = kw('collection', schema.layout(collection), 'size', num(sampleSize))
		projection(collection, args, output, newArrayList, false)
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		val where = filterOf(it, collection, filter)
		if (!where.empty) args.put('filter', where.code)
		#[new Code.Return(runtime('sample_rows', args))]
	}

	/** A random sample with the nearest neighbours of each sampled record among the sample, as Qdrant's distance matrix. */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		noPartitions(it)
		queryExtensions(it)
		val field = schema.vectorField(collection)
		if (vectorField !== null && vectorField !== field) throw Unsupported.of('a vector field outside the collection')
		val args = kw('collection', schema.layout(collection))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		val where = filterOf(it, collection, filter)
		if (!where.empty) args.put('filter', where.code)
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

	def private void noPartitions(EObject query) {
		val partitions = feature(query, 'partitions') as List<?>
		if (partitions !== null && !partitions.empty) throw Unsupported.of(NO_PARTITIONS)
	}

	def private void common(EObject query, VectorCollection collection, Entries args, OutputSpec output, ChromaFilter where) {
		noPartitions(query)
		if (!where.empty) args.put('filter', where.code)
		if (written(query, 'offset')) args.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		projection(collection, args, output, excluded, Boolean.TRUE == feature(query, 'withVectors'))
	}

	/** Output fields (the key is always returned); the embedding comes with 'with-vectors' or when the output names it. */
	def private void projection(VectorCollection collection, Entries args, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val key = collection.primaryKey
		val vector = schema.vectorField(collection)
		val List<String> fields = newArrayList
		var vectors = withVectors
		val listed = if (output === null || output.allFields) ModelUtil.payloadFields(collection) as List<? extends Field> else output.fields
		for (field : listed) {
			if (field !== key && !excluded.contains(field)) {
				if (field === vector) {
					vectors = true
				} else {
					fields += ModelUtil.fieldName(field)
				}
			}
		}
		args.put('output', texts(fields))
		if (vectors) args.put('vectors', flag(true))
	}

	/** Rejects clauses Chroma lacks with their reason. */
	def private void reject(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) {
				val clause = "the '" + (CLAUSES.get(name) ?: name) + "' clause"
				val reason = REASONS.get(name)
				throw Unsupported.of(if (reason === null) clause else clause + '; ' + reason)
			}
		}
	}

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	def private Expr queryVector(QueryInput input, VectorCollection collection, List<Stmt> prelude) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR: Literals.of(input.value)
			case ID: {
				val source = input.lookupCollection ?: collection
				prelude += new Code.Let('query_vector', runtime('lookup_vector', kw('collection', schema.layout(source), 'id', Literals.of(input.value))))
				new Code.Ref('query_vector')
			}
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			default: throw Unsupported.of(input.kind.literal + ' query input; Chroma queries one dense embedding')
		}
	}

	/** Bounds on the canonical similarity, or on Chroma's distance with 'score native'; the runtime applies them. */
	def private void scoreBounds(ScoreBounds bounds, boolean nativeScore, Entries args) {
		if (nativeScore) args.put('native', flag(true))
		if (bounds === null) return;
		if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
		if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
	}

	/** Search effort settings: Chroma's ef_search is a collection setting, so per-query effort is listed as ignored. */
	def private void searchParams(SearchParams params) {
		if (params === null) return;
		if (written(params, 'targetRecall')) throw Unsupported.of('target-recall: Chroma exposes no query-time recall target')
		if (params.exhaustive) throw Unsupported.of("the 'exhaustive' parameter; " + REASONS.get('exact'))
		schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'timeoutMillis', 'oversampling', 'rescore', 'filterStrategy')
		for (name : ModelUtil.extParams(params, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** Chroma defines no vendor extension items; parameters in a chroma block are rejected by name. */
	def private void queryExtensions(EObject host) {
		for (name : ModelUtil.extParams(host, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == ChromaGenerator.VENDOR) {
				for (item : block.getItems) throw Unsupported.of(item.eClass.name + ' on Chroma')
			}
		}
	}

	def private Expr rows(RecordSource source) {
		switch source {
			FileSource: runtime('read_records', kw('location', text(source.location)))
			InlineRecords: items(source.records.map[item | record(item)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record) {
		val result = entries
		for (entry : record.entries) result.put(ModelUtil.fieldName(entry.field), Literals.of(entry.value))
		result
	}

}
