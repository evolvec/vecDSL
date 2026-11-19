package org.ftn.vecDSL.generator.milvus

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Make
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.common.Py
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
import org.ftn.vecDSL.vecDSL.EBoostKind
import org.ftn.vecDSL.vecDSL.ECloneMode
import org.ftn.vecDSL.vecDSL.ECollectionProperty
import org.ftn.vecDSL.vecDSL.EConflictPolicy
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EFusionType
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EListKind
import org.ftn.vecDSL.vecDSL.EMatchOperator
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.ESampleMethod
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESnapshotScope
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
import static org.ftn.vecDSL.generator.milvus.MilvusSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Milvus operations in the host-language-neutral code model. Operation and
 * argument names follow pymilvus MilvusClient; composite behaviour (insert conflict handling, partial
 * updates, score normalization, recommendation vectors) is delegated to the per-language runtime module.
 */
class MilvusStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; MilvusCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Milvus: '
	static val PENDING = ' has no Milvus mapping yet'
	static val METRICS = 'metrics configuration; Milvus exports Prometheus metrics on port 9091 and configures them in milvus.yaml'
	static val CLUSTER_LIMITS = 'cluster limits; Milvus sets them for the whole server in milvus.yaml (rootCoord.maxDatabaseNum, quotaAndLimits.limits.maxCollectionNum)'
	static val REPLICATION = 'a stored replication factor; Milvus sets the replica count per load request (replica_number)'
	static val NO_GENERATION = 'Milvus has no generation API (its functions are BM25, text embedding, rerank, MinHash and molecular fingerprints)'
	static val GENERATION = 'generative models; ' + NO_GENERATION
	static val IMPORT_SCHEMES = #{'s3', 'minio', 'gs', 'gcs', 'az', 'azure'}
	static val IMPORT_FORMATS = #{EImportFormat.JSON, EImportFormat.PARQUET, EImportFormat.NUMPY, EImportFormat.CSV}
	static val NO_MODEL_OBJECTS = '; Milvus has no model objects: providers are configured in milvus.yaml and used through collection functions'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'offset' -> 'offset',
		'groupBy' -> 'group-by', 'sliceIndex' -> 'slice', 'fuzziness' -> 'fuzziness',
		'minShouldMatch' -> 'min-should-match', 'exact' -> 'exact', 'boost' -> 'boost', 'bounds' -> 'bounds',
		'params' -> 'params', 'textQueryType' -> 'text-query', 'matchOperator' -> 'operator', 'queryModel' -> 'model',
		'consistency' -> 'consistency'
	}

	/** Why Milvus cannot express a query clause. */
	static val REASONS = #{
		'asOf' -> 'Milvus reads only the current state of a collection',
		'cursor' -> 'Milvus pages searches by offset and scrolls by primary key, not by an opaque cursor',
		'explain' -> 'Milvus returns no score explanation',
		'withTotal' -> 'Milvus returns no total hit count with search results',
		'fuzziness' -> 'Milvus BM25 has no fuzzy matching',
		'minShouldMatch' -> 'Milvus BM25 has no minimum-should-match',
		'exact' -> 'Milvus has no per-query exact search (a FLAT index is always exhaustive)',
		'sliceIndex' -> 'Milvus has no parallel scroll slices',
		'prefetches' -> 'Milvus runs multi-stage retrieval only as hybrid search legs',
		'bounds' -> 'Milvus bounds hybrid results only per leg',
		'params' -> 'Milvus takes search parameters per hybrid leg',
		'queryModel' -> "Milvus embeds query text with the collection's text embedding function"
	}

	/** Clauses Milvus 3.0 serves that the generator does not emit yet. */
	static val NATIVE_PENDING = #{'elementScope', 'mmr', 'reranker', 'highlighted', 'sortClauses', 'aggregations',
		'textQueryType', 'matchOperator', 'boost'}

	val Script script
	val List<String> notes = newArrayList
	val MilvusSchema schema = new MilvusSchema(notes)
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
			Connection: {}
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(METRICS)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists)
				for (database : concept.databases) result += define(database, ifNotExists)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of(CLUSTER_LIMITS)
				if (concept.replication !== null) throw Unsupported.of(REPLICATION)
				if (concept.sharding !== null) throw Unsupported.of('cluster sharding (shards_num of its collections)' + PENDING)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists)
			}
			Database: result += database(concept, ifNotExists)
			Alias: result += run(client('create_alias', kw('collection_name', text(concept.collection.name), 'alias', text(concept.name))))
			VectorIndex: result += createIndex(indexCollection(concept), schema.vectorIndex(indexCollection(concept), vectorFieldOf(concept), concept))
			PayloadIndex: {
				if (concept.kind == EPayloadIndexKind.FULL_TEXT) {
					throw Unsupported.of('a full_text payload index on an existing field; Milvus enables text match only when it creates a field')
				}
				result += createIndex(indexCollection(concept), schema.payloadIndex(concept))
			}
			VectorField: result += addField(member(concept, 'a vector field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += createPartition(member(concept, 'a partition'), concept.name, ifNotExists)
			Tenant: result += createSlice(member(concept, 'a tenant'), concept.name, ifNotExists)
			Namespace: result += createSlice(member(concept, 'a namespace'), concept.key, ifNotExists)
			Function: result += run(client('add_collection_function', kw('collection_name', text(member(concept, 'a function').name),
				'function', schema.function(concept))))
			FileResource: {
				if (concept.path === null) throw Unsupported.of('a file resource without a path; Milvus registers a file resource by its object storage path')
				result += run(runtime('add_file_resource', kw('name', text(concept.name), 'path', text(concept.path))))
			}
			Analyzer: result += new Code.Return(runtime('check_analyzer', kw('analyzer_params', schema.analyzerParams(concept))))
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; Milvus applies token filters inside an analyzer definition')
				result += new Code.Return(runtime('check_analyzer', kw('analyzer_params', schema.analyzerParams(analyzer))))
			}
			FieldLimit: result += fieldLimit(concept)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODEL_OBJECTS)
			GenerativeModel: throw Unsupported.of(GENERATION)
			Reranker: throw Unsupported.of('rerankers' + NO_MODEL_OBJECTS + ' and per-search rankers')
			Quantization: throw Unsupported.of('quantization outside a vector index; Milvus sets quantization through the index type of a vector index')
			ReplicationConfig: throw Unsupported.of(REPLICATION)
			ShardConfig: throw Unsupported.of('sharding for an existing collection; Milvus fixes shards_num when it creates a collection')
			StorageConfig: throw Unsupported.of('storage settings as an object; Milvus sets mmap per collection, field or index, and storage for the whole server')
			AuthConfig: throw Unsupported.of('authentication settings' + PENDING)
			RoleBinding: throw Unsupported.of('role bindings' + PENDING)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of('backup schedules; Milvus creates snapshots only on request')
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits; Milvus sets them for the whole server in milvus.yaml (proxy.maxFieldNum, proxy.maxShardNum, proxy.maxDimension, rootCoord.maxPartitionNum)')
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/**
	 * The collection of a member concept: its container, or the collection of the enclosing create. Milvus creates fields,
	 * partitions, tenants and functions only inside a collection.
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
		throw Unsupported.of(what + ' outside a collection; Milvus creates it only inside a collection')
	}

	/** A tenant or namespace: a value of the partition key when the collection declares one, otherwise a partition. */
	def private List<Stmt> createSlice(VectorCollection collection, String slice, boolean ifNotExists) {
		if (ModelUtil.partitionKey(collection) !== null) return newArrayList
		createPartition(collection, slice, ifNotExists)
	}

	def private List<Stmt> createPartition(VectorCollection collection, String partition, boolean ifNotExists) {
		val args = kw('collection_name', text(collection.name), 'partition_name', text(partition))
		val create = run(client('create_partition', args))
		if (!ifNotExists) return newArrayList(create)
		newArrayList(new Code.When(runtime('has_partition', args), true, #[create]) as Stmt)
	}

	/** A field added to an existing collection: Milvus requires added fields to be nullable and vector fields to be indexed. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val args = schema.fieldArgs(collection, field)
		args.put('nullable', flag(true))
		val List<Stmt> result = newArrayList
		result += run(client('add_collection_field', kw('collection_name', text(collection.name), 'field', make('Field', args))))
		if (field instanceof VectorField) {
			val vector = field as VectorField
			result += createIndex(collection, schema.vectorIndex(collection, vector, ModelUtil.vectorIndex(collection, vector)))
		}
		result
	}

	def private List<Stmt> fieldLimit(FieldLimit limit) {
		val field = EcoreUtil2.getContainerOfType(limit, Field)
		if (field === null) throw Unsupported.of('a field limit outside a field; Milvus stores limits as field parameters')
		val key = switch limit.type {
			case ARRAY_LEN: 'max_capacity'
			case MAX_SIZE: 'max_length'
			case VECTOR_DIM: throw Unsupported.of('changing a vector dimension; Milvus fixes dim when it creates a field')
		}
		val collection = member(field, 'a field')
		#[run(runtime('alter_field', kw('collection_name', text(collection.name), 'field_name', text(ModelUtil.fieldName(field)),
			'field_params', entries(#{key -> num(limit.value)}))))]
	}

	def private List<Stmt> database(Database database, boolean ifNotExists) {
		val name = text(database.name)
		val create = run(client('create_database', kw('db_name', name)))
		val List<Stmt> result = newArrayList
		result += if (ifNotExists) new Code.When(runtime('has_database', kw('db_name', name)), true, #[create]) else create
		result += run(client('use_database', kw('db_name', name)))
		for (collection : database.collections) result += schema.createCollection(collection, ifNotExists)
		result += run(client('use_database', kw('db_name', text(defaultDatabase))))
		result
	}

	def private Stmt createIndex(VectorCollection collection, Make index) {
		run(client('create_index', kw('collection_name', text(collection.name), 'index_params', items(#[index]))))
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) {
			throw Unsupported.of('altering a ' + concept.eClass.name + '; Milvus renames and evolves collections only')
		}
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) result += run(client('rename_collection', kw('old_name', text(collection.name), 'new_name', text(newName))))
		result
	}

	def private List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val name = text(collection.name)
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: #[run(runtime('drop_field', kw('collection_name', name, 'field_name', text(ModelUtil.fieldName(operation.field)))))]
			RenameFieldOp: throw Unsupported.of('renaming a field; Milvus adds and drops fields but cannot rename them')
			SetPropertyOp: #[run(client('alter_collection_properties', kw('collection_name', name, 'properties', propertyEntry(operation))))]
			AddAliasOp: #[run(client('create_alias', kw('collection_name', name, 'alias', text(operation.newAlias.name))))]
			AddFunctionOp: #[run(client('add_collection_function', kw('collection_name', name, 'function', schema.function(operation.newFunction))))]
			DropFunctionOp: #[run(client('drop_collection_function', kw('collection_name', name, 'function_name', text(operation.function.name))))]
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	/** The Milvus collection property for a portable one; the values are strings, as the server stores them. */
	def private Entries propertyEntry(SetPropertyOp operation) {
		val value = Literals.of(operation.value)
		switch operation.property {
			case TTL: {
				val seconds = if (value instanceof Code.Num) text(Py.num(value.value)) else value
				entries(#{'collection.ttl.seconds' -> seconds})
			}
			case DESCRIPTION: entries(#{'collection.description' -> value})
			case CONSISTENCY: {
				if (!(value instanceof Code.Text)) throw Unsupported.of('a consistency that is not a level name')
				val level = EConsistencyLevel.get((value as Code.Text).value)
				if (level === null) throw Unsupported.of('consistency ' + (value as Code.Text).value)
				entries(#{'consistency_level' -> text((schema.consistency(level) as Code.Sym).name)})
			}
			case DYNAMIC: {
				if (!(value instanceof Code.Flag)) throw Unsupported.of('a dynamic setting that is not true or false')
				entries(#{'dynamicfield.enabled' -> text(String.valueOf((value as Code.Flag).value))})
			}
			case DELETION_PROTECTION: throw Unsupported.of('deletion-protection; Milvus has no collection property that blocks dropping a collection')
			case TAGS: throw Unsupported.of('collection tags; Milvus collections have no tags')
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val name = text(concept.name)
				val List<Stmt> steps = newArrayList
				if (aliased(concept)) steps += run(runtime('drop_aliases', kw('collection_name', name)))
				steps += run(client('drop_collection', kw('collection_name', name)))
				if (ifExists) #[new Code.When(runtime('has_collection', kw('collection_name', name)), false, steps) as Stmt] else steps
			}
			Alias: #[run(client('drop_alias', kw('alias', text(concept.name))))]
			Partition: dropPartition(member(concept, 'a partition'), concept.name)
			Tenant: dropSlice(member(concept, 'a tenant'), concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), concept.key)
			VectorIndex: dropIndex(indexCollection(concept), concept)
			PayloadIndex: dropIndex(indexCollection(concept), concept)
			Database: #[run(client('drop_database', kw('db_name', text(concept.name))))]
			VectorField: #[dropField(concept)]
			PayloadField: #[dropField(concept)]
			Function: {
				val collection = member(concept, 'a function')
				#[run(client('drop_collection_function', kw('collection_name', text(collection.name), 'function_name', text(concept.name))))]
			}
			FileResource: #[run(runtime('remove_file_resource', kw('name', text(concept.name))))]
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Milvus object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; Milvus drops collections and logical databases, not the server')
			Cluster: throw Unsupported.of('dropping a cluster; Milvus drops collections and logical databases, not the cluster')
			Analyzer: throw Unsupported.of('dropping an analyzer; Milvus stores analyzers inside field definitions, not as objects')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; Milvus stores analyzers inside field definitions, not as objects')
			FieldLimit: throw Unsupported.of('dropping a field limit; Milvus requires max_length and max_capacity on their fields')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODEL_OBJECTS)
			GenerativeModel: throw Unsupported.of(GENERATION)
			Reranker: throw Unsupported.of('rerankers' + NO_MODEL_OBJECTS + ' and per-search rankers')
			Quantization: throw Unsupported.of('dropping quantization; Milvus sets quantization through the index type of a vector index')
			ReplicationConfig: throw Unsupported.of(REPLICATION)
			ShardConfig: throw Unsupported.of('dropping sharding; Milvus fixes shards_num when it creates a collection')
			StorageConfig: throw Unsupported.of('storage settings as an object; Milvus sets mmap per collection, field or index, and storage for the whole server')
			AuthConfig: throw Unsupported.of('authentication settings' + PENDING)
			RoleBinding: throw Unsupported.of('role bindings' + PENDING)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of('backup schedules; Milvus creates snapshots only on request')
			ClusterLimit: throw Unsupported.of(CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits; Milvus sets them for the whole server in milvus.yaml (proxy.maxFieldNum, proxy.maxShardNum, proxy.maxDimension, rootCoord.maxPartitionNum)')
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/**
	 * Whether the script gives the collection an alias (declared, added or swapped to it). Milvus refuses to drop a collection
	 * that still has aliases, so such a drop removes them first.
	 */
	def private boolean aliased(VectorCollection collection) {
		EcoreUtil2.getAllContentsOfType(script, Alias).exists[alias | alias.collection === collection]
			|| EcoreUtil2.getAllContentsOfType(script, SwapAlias).exists[swap | swap.newCollection === collection]
	}

	def private Stmt dropField(Field field) {
		run(runtime('drop_field', kw('collection_name', text(member(field, 'a field').name), 'field_name', text(ModelUtil.fieldName(field)))))
	}

	def private List<Stmt> dropPartition(VectorCollection collection, String partition) {
		val name = text(collection.name)
		#[
			run(client('release_partitions', kw('collection_name', name, 'partition_names', texts(#[partition])))),
			run(client('drop_partition', kw('collection_name', name, 'partition_name', text(partition))))
		]
	}

	/** Dropping a tenant or namespace removes its records: its partition, or its rows when it is a partition key value. */
	def private List<Stmt> dropSlice(VectorCollection collection, String slice) {
		val key = ModelUtil.partitionKey(collection)
		if (key === null) return dropPartition(collection, slice)
		#[new Code.Return(runtime('delete_rows', kw('collection_name', text(collection.name),
			'filter', text(ModelUtil.fieldName(key) + ' == ' + Py.str(slice))))) as Stmt]
	}

	/** Milvus drops a vector index only while the collection is released. */
	def private List<Stmt> dropIndex(VectorCollection collection, Index index) {
		val name = text(collection.name)
		val drop = run(client('drop_index', kw('collection_name', name, 'index_name', text(index.name))))
		if (index instanceof VectorIndex) #[run(client('release_collection', kw('collection_name', name))), drop] else #[drop]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		statementTtl(it, collection)
		schema.note(it, 'writeConsistency', 'recordCountHint')
		val args = kw('collection_name', text(collection.name), 'rows', rows(source), 'primary_key', text(primaryKey(collection)))
		if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		writeScope(args, collection, namespace, tenant, partition)
		write('insert_rows', args)
	}

	def dispatch List<Stmt> body(Upsert it) {
		statementTtl(it, collection)
		schema.note(it, 'writeConsistency', 'recordCountHint')
		val args = kw('collection_name', text(collection.name), 'rows', rows(source))
		writeScope(args, collection, namespace, tenant, partition)
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		write('upsert_rows', args)
	}

	def dispatch List<Stmt> body(Update it) {
		if (!appendFields.empty || !arrayRemoveFields.empty) throw Unsupported.of('array-append and array-remove' + PENDING)
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Milvus writes have no version condition')
		schema.note(it, 'writeConsistency', 'recordCountHint')
		val where = scopedFilter(collection, filter, namespace, tenant)
		val args = kw('collection_name', text(collection.name), 'primary_key', text(primaryKey(collection)))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (values !== null) args.put('values', record(values))
		if (source !== null) args.put('rows', rows(source))
		if (written(it, 'mode')) args.put('mode', text(mode.literal))
		if (!removedFields.empty) args.put('remove', texts(removedFields.map[ModelUtil.fieldName(it)]))
		partitionName(args, collection, namespace, tenant, partition)
		write('update_rows', args)
	}

	def dispatch List<Stmt> body(Delete it) {
		schema.note(it, 'writeConsistency')
		val where = scopedFilter(collection, filter, namespace, tenant)
		if (written(it, 'deleteLimit')) return deleteFirst(it, where)
		if (deleteAll && ids.empty && where.empty) {
			if (namespace === null && tenant === null && partition === null) {
				return #[run(client('truncate_collection', kw('collection_name', text(collection.name))))]
			}
			return deleteFirst(it, where)
		}
		val args = kw('collection_name', text(collection.name))
		if (!ids.empty && where.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!ids.empty && !where.empty) where.add(primaryKey(collection) + ' in [' + ids.map[where.value(it)].join(', ') + ']')
		if (!where.empty) args.put('filter', where.code)
		partitionName(args, collection, namespace, tenant, partition)
		write('delete_rows', args)
	}

	/**
	 * Deletes by keys the runtime selects: the first n matching records in primary key order (Milvus deletes take no
	 * limit), or every record of a partition (a Milvus delete needs ids or a filter).
	 */
	def private List<Stmt> deleteFirst(Delete delete, MilvusFilter where) {
		val collection = delete.collection
		if (!delete.ids.empty) where.add(primaryKey(collection) + ' in [' + delete.ids.map[where.value(it)].join(', ') + ']')
		val args = kw('collection_name', text(collection.name), 'primary_key', text(primaryKey(collection)))
		if (written(delete, 'deleteLimit')) args.put('limit', num(delete.deleteLimit))
		if (!where.empty) args.put('filter', where.code)
		partitionName(args, collection, delete.namespace, delete.tenant, delete.partition)
		write('delete_matching', args)
	}

	def private void statementTtl(EObject statement, VectorCollection collection) {
		if (!written(statement, 'ttlSeconds')) return;
		if (collection.ttlField !== null) throw Unsupported.of('per-statement ttl on a collection with a ttl-field' + PENDING)
		throw Unsupported.of('per-statement ttl; Milvus expires records by the collection ttl or by a timestamptz ttl field')
	}

	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; a Milvus import job reads a fixed set of files')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') return serverImport(it, scheme)
		schema.note(it, 'parallelism', 'async')
		val read = kw('location', text(location))
		if (written(it, 'format')) read.put('format', text(format.literal))
		val args = kw('collection_name', text(collection.name), 'rows', runtime('read_records', read))
		val upsert = written(it, 'onConflict') && (onConflict == EConflictPolicy.UPDATE || onConflict == EConflictPolicy.REPLACE)
		if (!upsert) args.put('primary_key', text(primaryKey(collection)))
		if (!upsert && written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		writeScope(args, collection, namespace, tenant, null)
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		#[new Code.Return(runtime(if (upsert) 'upsert_rows' else 'insert_rows', args))]
	}

	/**
	 * A Milvus import job (REST /v2/vectordb/jobs/import): the server reads the file from its own bucket, so the path is
	 * the location without scheme and bucket.
	 */
	def private List<Stmt> serverImport(BulkImport job, String scheme) {
		if (!IMPORT_SCHEMES.contains(scheme)) throw Unsupported.of('import from ' + scheme + '; Milvus imports files from its object storage bucket')
		if (written(job, 'onConflict')) throw Unsupported.of("the 'on-conflict' clause with a server-side import; Milvus import jobs take no conflict policy")
		if (written(job, 'format') && !IMPORT_FORMATS.contains(job.format)) {
			throw Unsupported.of('import format ' + job.format.literal + '; Milvus imports JSON, JSON Lines, Parquet, NumPy and CSV files')
		}
		schema.note(job, 'batchSize', 'parallelism')
		val path = job.location.substring(scheme.length + 3)
		if (!path.contains('/')) throw Unsupported.of('an import location without an object path')
		val args = kw('collection_name', text(job.collection.name), 'files', items(#[texts(#[path.substring(path.indexOf('/') + 1)])]))
		val collection = job.collection
		val slice = job.tenant?.name ?: job.namespace
		if (job.partition !== null || (slice !== null && ModelUtil.partitionKey(collection) === null)) {
			args.put('partition_name', text(job.partition?.name ?: slice))
		} else if (slice !== null) {
			throw Unsupported.of('a tenant or namespace with a server-side import into a partition-key collection; the import file must carry the key')
		}
		args.put('wait', flag(!job.async))
		#[new Code.Return(runtime('bulk_import', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of('multi-statement transactions; Milvus has no transaction API (every write commits on its own)')
	}

	/**
	 * Milvus 3 acknowledges a write only after logging it, and strong reads see it at once, so 'wait' needs no
	 * flush; flushing per write would also hit the server's flush rate limit (one per collection every 10 s).
	 */
	def private List<Stmt> write(String op, Entries args) {
		#[new Code.Return(runtime(op, args))]
	}

	// ---------------------------------------------------------------- operations

	def dispatch List<Stmt> body(Load it) {
		val args = kw('collection_name', text(collection.name))
		if (!partitions.empty) args.put('partition_names', texts(partitions.map[it.name]))
		extensions(args, it)
		#[run(client(if (partitions.empty) 'load_collection' else 'load_partitions', args))]
	}

	def dispatch List<Stmt> body(Release it) {
		val args = kw('collection_name', text(collection.name))
		if (!partitions.empty) args.put('partition_names', texts(partitions.map[it.name]))
		#[run(client(if (partitions.empty) 'release_collection' else 'release_partitions', args))]
	}

	def dispatch List<Stmt> body(Flush it) {
		#[run(client('flush', kw('collection_name', text(collection.name))))]
	}

	def dispatch List<Stmt> body(Compact it) {
		schema.note(it, 'targetSegments')
		if (!schema.extValues(it).empty) throw Unsupported.of('compaction options (clustering, L0, target size)' + PENDING)
		#[new Code.Return(runtime('compact', kw('collection_name', text(collection.name), 'wait', flag(!async))))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = kw('collection_name', text(collection.name))
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + PENDING)
	}

	def dispatch List<Stmt> body(Snapshot it) {
		snapshotScope(it, scope)
		if (incremental) throw Unsupported.of('incremental snapshots; a Milvus snapshot always records the whole collection')
		if (tenant !== null) throw Unsupported.of('tenant snapshots; Milvus snapshots whole collections')
		if (location !== null) throw Unsupported.of('snapshot locations (export to external storage)' + PENDING)
		val args = kw('collection_name', text(collection.name), 'snapshot_name', text(snapshotName ?: name))
		if (description !== null) args.put('description', text(description))
		#[run(runtime('create_snapshot', args))]
	}

	/** Restores into a new collection: Milvus requires an absent target and keeps a snapshot with its source collection. */
	def dispatch List<Stmt> body(Restore it) {
		snapshotScope(it, scope)
		if (location !== null) throw Unsupported.of('restoring from an external location' + PENDING)
		val source = snapshotSource(snapshotName)
		if (source === collection) {
			throw Unsupported.of('restoring a snapshot into the collection it was taken from; Milvus restores only into a collection that does not exist')
		}
		val args = kw('snapshot_name', text(snapshotName), 'collection_name', text(collection.name))
		if (source !== null) args.put('source_collection_name', text(source.name))
		args.put('wait', flag(!async))
		#[new Code.Return(runtime('restore_snapshot', args))]
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		if (location !== null) throw Unsupported.of('snapshot locations (export to external storage)' + PENDING)
		val args = kw('snapshot_name', text(snapshotName))
		val source = snapshotSource(snapshotName)
		if (source !== null) args.put('collection_name', text(source.name))
		#[run(runtime('drop_snapshot', args))]
	}

	def private void snapshotScope(EObject statement, ESnapshotScope requested) {
		if (written(statement, 'scope') && requested != ESnapshotScope.COLLECTION) {
			throw Unsupported.of(requested.literal + ' snapshots; Milvus snapshots one collection at a time')
		}
	}

	/** The collection a snapshot of this script was taken from; the runtime searches every collection otherwise. */
	def private VectorCollection snapshotSource(String snapshot) {
		for (taken : EcoreUtil2.getAllContentsOfType(script, Snapshot)) {
			if ((taken.snapshotName ?: taken.name) == snapshot) return taken.collection
		}
		null
	}

	/** A materialized point-in-time copy: snapshot, restore under the new name, drop the snapshot. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a logical database' + PENDING)
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; a Milvus restore copies the segment files')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; Milvus snapshots record the current state only")
		#[new Code.Return(runtime('clone_collection', kw('collection_name', text(sourceCollection.name), 'target_name', text(targetName))))]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of('refreshing external collections' + PENDING)
	}

	def dispatch List<Stmt> body(RebuildIndex it) {
		val collection = indexCollection(index)
		val definition = index
		val name = text(collection.name)
		switch definition {
			VectorIndex: #[
				run(client('release_collection', kw('collection_name', name))),
				run(client('drop_index', kw('collection_name', name, 'index_name', text(definition.name)))),
				createIndex(collection, schema.vectorIndex(collection, vectorFieldOf(definition), definition)),
				run(client('load_collection', kw('collection_name', name)))
			]
			PayloadIndex: {
				if (definition.kind == EPayloadIndexKind.FULL_TEXT) {
					throw Unsupported.of('rebuilding a full_text payload index; Milvus builds text match with the field')
				}
				#[
				run(client('drop_index', kw('collection_name', name, 'index_name', text(definition.name)))),
				createIndex(collection, schema.payloadIndex(definition))
				]
			}
			default: throw Unsupported.of(NOT_MAPPED + definition.eClass.name)
		}
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		#[run(client('alter_alias', kw('collection_name', text(newCollection.name), 'alias', text(alias.name))))]
	}

	def dispatch List<Stmt> body(Truncate it) {
		if (namespace === null) {
			return #[run(client('truncate_collection', kw('collection_name', text(collection.name))))]
		}
		val where = scopedFilter(collection, null, namespace, null)
		val args = kw('collection_name', text(collection.name))
		if (!where.empty) {
			args.put('filter', where.code)
			return #[new Code.Return(runtime('delete_rows', args)) as Stmt]
		}
		args.put('primary_key', text(primaryKey(collection)))
		partitionName(args, collection, namespace, null, null)
		#[new Code.Return(runtime('delete_matching', args))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val slices = kind == EListKind.TENANTS || kind == EListKind.NAMESPACES
		if (slices && scope instanceof VectorCollection && ModelUtil.partitionKey(scope as VectorCollection) !== null) {
			throw Unsupported.of('listing the tenants or namespaces of a partition-key collection; Milvus does not enumerate partition key values')
		}
		if (slices && !(scope instanceof VectorCollection)) throw Unsupported.of('listing tenants or namespaces without a collection; Milvus partitions belong to a collection')
		val args = kw('kind', text(kind.literal))
		if (slices) args.put('only', texts(sliceNames(scope as VectorCollection, kind == EListKind.TENANTS)))
		if (scope !== null) args.put('scope', text(scope.eGet(scope.eClass.getEStructuralFeature('name')) as String))
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('list_objects', args))]
	}

	/** The tenants (or namespace keys) this script declares for a collection, inside it or in nested creates. */
	def private List<String> sliceNames(VectorCollection collection, boolean tenants) {
		val List<String> names = newArrayList
		for (concept : EcoreUtil2.getAllContentsOfType(script, Concept)) {
			if (concept instanceof Tenant && tenants || concept instanceof Namespace && !tenants) {
				val owner = try member(concept, 'a slice') catch (Unsupported e) null
				if (owner === collection) names += if (concept instanceof Tenant) (concept as Tenant).name else (concept as Namespace).key
			}
		}
		names
	}

	def dispatch List<Stmt> body(AwaitIndex it) {
		val collection = indexCollection(index)
		val field = switch definition : index {
			VectorIndex: ModelUtil.fieldName(vectorFieldOf(definition))
			PayloadIndex: MilvusFilter.path(definition.target)
		}
		val args = kw('collection_name', text(collection.name), 'index_name', text(index.name), 'field_name', text(field))
		if (written(it, 'state')) args.put('until', text(state.literal))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted',
			'sortClauses', 'aggregations', 'prefetches', 'exact')
		if (groupBy !== null && boost !== null) throw Unsupported.of('group-by together with boost' + PENDING)
		reject(it, 'queryModel')
		val field = searchField(collection, vectorField)
		val List<Stmt> prelude = newArrayList
		val data = queryData(input, collection, field, 'query_vector', prelude)
		val metric = if (written(it, 'metric')) schema.metricName(it.metric) else schema.metric(collection, field, ModelUtil.vectorIndex(collection, field))
		search(it, collection, field, prelude, data, metric, scopedFilter(collection, filter, namespace, tenant), boost)
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'fuzziness', 'minShouldMatch')
		if (searchFields.exists[written(it, 'boost')]) throw Unsupported.of('per-field boosts in keyword search; Milvus BM25 scores one sparse field per search, without field weights')
		if (input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search with ' + input.kind.literal + ' input; Milvus BM25 takes query text')
		val function = bm25Function(collection, searchFields.map[it.field])
		val sparse = function.outputs.head as VectorField
		val textField = ModelUtil.fieldName(if (searchFields.empty) function.inputs.head else searchFields.head.field)
		val where = scopedFilter(collection, filter, namespace, tenant)
		if (written(it, 'textQueryType')) {
			switch textQueryType {
				case MATCH: {}
				case PHRASE: where.add(phrase(textField, input.value, where, it))
				case PHRASE_PREFIX: where.add(phrase(textField, input.value, where, it))
				default: throw Unsupported.of('text-query ' + textQueryType.literal + '; Milvus keyword search matches terms, phrases and phrase prefixes')
			}
		}
		if (written(it, 'matchOperator') && matchOperator == EMatchOperator.AND) {
			if (!(input.value instanceof StringLit)) throw Unsupported.of("operator 'and' with a non-constant query")
			where.add((input.value as StringLit).value.trim.split('\\s+').map['TEXT_MATCH(' + textField + ', ' + Py.str(it) + ')'].join(' and '))
		}
		search(it, collection, sparse, newArrayList, vector('text', Literals.of(input.value)), 'BM25', where, boost)
	}

	def private String phrase(String field, Literal text, MilvusFilter where, KeywordSearch search) {
		'PHRASE_MATCH(' + field + ', ' + where.value(text) + (if (written(search, 'slop')) ', ' + search.slop else '') + ')'
	}

	def private List<Stmt> search(EObject query, VectorCollection collection, VectorField field, List<Stmt> prelude, Expr data,
		String metric, MilvusFilter where, BoostBlock boost) {
		val nativeScore = written(query, 'scoreForm') && feature(query, 'scoreForm') == EScoreForm.NATIVE
		val knobs = new LinkedHashMap<String, Expr>
		val hits = kw('results', new Code.Ref('results'), 'metric', text(metric), 'primary_key', text(primaryKey(collection)))
		if (nativeScore) hits.put('native', flag(true))
		bounds(feature(query, 'bounds') as ScoreBounds, metric, nativeScore, knobs, hits)
		val args = kw(
			'collection_name', text(collection.name),
			'data', items(#[data]),
			'anns_field', text(ModelUtil.fieldName(field)),
			'limit', num((feature(query, 'topK') as Integer).doubleValue)
		)
		if (!where.empty) args.put('filter', where.code)
		args.put('output_fields', outputFields(collection, query, true))
		tuning(feature(query, 'params') as SearchParams, schema.indexType(field, ModelUtil.vectorIndex(collection, field)), knobs, args)
		val searchParams = kw('metric_type', text(metric))
		if (written(query, 'offset')) searchParams.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		if (!knobs.empty) searchParams.put('params', entries(knobs))
		args.put('search_params', searchParams)
		partitionNames(args, collection, query)
		val groupBy = feature(query, 'groupBy') as FieldPath
		if (groupBy !== null) {
			args.put('group_by_field', text(MilvusFilter.path(groupBy)))
			if (written(query, 'groupSize')) args.put('group_size', num((feature(query, 'groupSize') as Integer).doubleValue))
		}
		consistency(args, query)
		if (boost !== null) args.put('ranker', ranker(boost))
		extensions(args, query)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Let('results', client('search', args))
		result += new Code.Return(runtime('hits', hits))
		result
	}

	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'boost', 'bounds', 'params')
		val List<Stmt> prelude = newArrayList
		val List<Expr> requests = newArrayList
		val List<Double> weights = newArrayList
		var index = 0
		for (leg : legs) {
			index = index + 1
			val where = scopedFilter(collection, filter, namespace, tenant)
			switch leg {
				VectorLeg: {
					reject(leg, 'exact')
					val field = searchField(collection, leg.vectorField)
					val data = queryData(leg.input ?: input, collection, field, 'query_vector_' + index, prelude)
					where.where(leg.filter)
					val metric = if (written(leg, 'metric')) schema.metricName(leg.metric) else schema.metric(collection, field, ModelUtil.vectorIndex(collection, field))
					val knobs = new LinkedHashMap<String, Expr>
					bounds(leg.bounds, metric, false, knobs, entries)
					val unused = entries
					tuning(leg.params, schema.indexType(field, ModelUtil.vectorIndex(collection, field)), knobs, unused)
					if (unused.has('timeout')) throw Unsupported.of('per-leg timeouts')
					knobs.putAll(schema.extValues(leg))
					requests += request(data, field, metric, knobs, leg.legLimit, written(leg, 'legLimit'), topK, where)
					weights += if (written(leg, 'weight')) leg.weight else null
				}
				KeywordLeg: {
					reject(leg, 'textQueryType', 'matchOperator', 'minShouldMatch')
					val text = leg.input ?: input
					if (text === null || text.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword leg without text input')
					val function = bm25Function(collection, leg.searchFields.map[it.field])
					where.where(leg.filter)
					requests += request(vector('text', Literals.of(text.value)), function.outputs.head as VectorField, 'BM25',
						new LinkedHashMap, leg.legLimit, written(leg, 'legLimit'), topK, where)
					weights += if (written(leg, 'weight')) leg.weight else null
				}
			}
		}
		val args = kw(
			'collection_name', text(collection.name),
			'reqs', items(requests),
			'ranker', fusion(it, weights),
			'limit', num(topK),
			'output_fields', outputFields(collection, it, true)
		)
		if (written(it, 'offset')) args.put('offset', num(offset))
		if (groupBy !== null) {
			args.put('group_by_field', text(MilvusFilter.path(groupBy)))
			if (written(it, 'groupSize')) args.put('group_size', num(groupSize))
		}
		partitionNames(args, collection, it)
		consistency(args, it)
		extensions(args, it)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Let('results', client('hybrid_search', args))
		result += new Code.Return(runtime('hits', kw('results', new Code.Ref('results'), 'metric', text('FUSED'), 'native', flag(true),
			'primary_key', text(primaryKey(collection)))))
		result
	}

	def private Make request(Expr data, VectorField field, String metric, Map<String, Expr> knobs, int legLimit, boolean limited,
		int topK, MilvusFilter where) {
		val param = kw('metric_type', text(metric))
		if (!knobs.empty) param.put('params', entries(knobs))
		val args = kw(
			'data', items(#[data]),
			'anns_field', text(ModelUtil.fieldName(field)),
			'param', param,
			'limit', num(if (limited) legLimit else topK)
		)
		if (!where.empty) args.put('expr', where.code)
		make('AnnRequest', args)
	}

	def private Make fusion(HybridSearch search, List<Double> weights) {
		val kind = if (written(search, 'fusion')) search.fusion else EFusionType.RRF
		switch kind {
			case RRF: {
				val args = kw('strategy', text('rrf'))
				if (written(search, 'fusionConstant')) args.put('k', num(search.fusionConstant))
				make('Ranker', args)
			}
			default: {
				val args = kw('strategy', text('weighted'), 'weights', items(fusionWeights(search, weights).map[num(it)]))
				if (kind == EFusionType.RELATIVE_SCORE) args.put('norm_score', flag(true))
				make('Ranker', args)
			}
		}
	}

	def private List<Double> fusionWeights(HybridSearch search, List<Double> weights) {
		if (!weights.contains(null)) return weights
		if (written(search, 'alpha') && weights.size == 2) return #[search.alpha, 1 - search.alpha]
		weights.map[1.0 / weights.size].toList
	}

	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'prefetches', 'boost')
		val field = searchField(collection, vectorField)
		val examples = kw('positives', items(positives.map[Literals.of(it)]))
		if (!negatives.empty) examples.put('negatives', items(negatives.map[Literals.of(it)]))
		val args = kw('collection_name', text((lookupCollection ?: collection).name), 'field', text(ModelUtil.fieldName(field)))
		args.entries.putAll(examples.entries)
		val where = scopedFilter(collection, filter, namespace, tenant)
		if ((positives + negatives).exists[!(it instanceof ListLit)]) {
			where.add(primaryKey(collection) + ' not in ' + where.placeholder('examples', runtime('example_ids', examples)))
		}
		val List<Stmt> prelude = newArrayList(new Code.Let('query_vector', runtime('recommend_vector', args)) as Stmt)
		search(it, collection, field, prelude, vector(denseKind(field), new Code.Ref('query_vector')),
			schema.metric(collection, field, ModelUtil.vectorIndex(collection, field)), where, null)
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		val args = kw('collection_name', text(collection.name), 'ids', items(ids.map[Literals.of(it)]),
			'output_fields', outputFields(collection, it))
		partitionNames(args, collection, it)
		consistency(args, it)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		val where = scopedFilter(collection, filter, namespace, tenant)
		val args = kw('collection_name', text(collection.name))
		if (!where.empty) args.put('filter', where.code)
		partitionNames(args, collection, it)
		consistency(args, it)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'cursor', 'sliceIndex', 'sortClauses', 'asOf')
		val where = scopedFilter(collection, filter, namespace, tenant)
		val args = kw('collection_name', text(collection.name))
		if (!where.empty) args.put('filter', where.code)
		args.put('output_fields', outputFields(collection, it))
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		if (written(it, 'offset')) args.put('offset', num(offset))
		partitionNames(args, collection, it)
		consistency(args, it)
		#[new Code.Return(runtime('scroll', args))]
	}

	/**
	 * An exact sample of n records: the runtime reads the matching primary keys and draws n uniformly (or takes the first
	 * n in key order). Milvus RANDOM_SAMPLE(ratio) keeps an approximate fraction, not a count.
	 */
	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		val where = scopedFilter(collection, filter, namespace, tenant)
		val args = kw('collection_name', text(collection.name), 'primary_key', text(primaryKey(collection)), 'size', num(sampleSize))
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		if (!where.empty) args.put('filter', where.code)
		args.put('output_fields', outputFields(collection, it))
		partitionNames(args, collection, it)
		consistency(args, it)
		#[new Code.Return(runtime('sample_rows', args))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		throw Unsupported.of('generative search; ' + NO_GENERATION)
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		val field = searchField(collection, vectorField)
		val where = scopedFilter(collection, filter, namespace, tenant)
		val args = kw('collection_name', text(collection.name), 'field', text(ModelUtil.fieldName(field)),
			'metric', text(schema.metric(collection, field, ModelUtil.vectorIndex(collection, field))),
			'primary_key', text(primaryKey(collection)))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		if (!where.empty) args.put('filter', where.code)
		consistency(args, it)
		#[new Code.Return(runtime('distance_matrix', args))]
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

	def private static Make vector(String kind, Expr value) {
		make('Vector', kw('kind', text(kind), 'value', value))
	}

	def private static String denseKind(VectorField field) {
		switch field.type {
			case SPARSE_FLOAT_VECTOR: 'sparse'
			case BINARY_VECTOR: 'binary'
			default: 'dense'
		}
	}

	/**
	 * Raises for the first written clause: clauses Milvus serves but the generator does not emit yet name themselves as
	 * pending, the others give the reason Milvus cannot express them.
	 */
	def private void reject(EObject statement, String... features) {
		for (feature : features) {
			if (written(statement, feature)) {
				val clause = "the '" + (CLAUSES.get(feature) ?: feature) + "' clause"
				if (NATIVE_PENDING.contains(feature)) throw Unsupported.of(clause + PENDING)
				val reason = REASONS.get(feature)
				throw Unsupported.of(clause + (if (reason === null) '' else '; ' + reason))
			}
		}
	}

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	def private VectorField searchField(VectorCollection collection, VectorField explicit) {
		if (explicit !== null) return explicit
		val candidates = ModelUtil.vectorFields(collection).filter[
			val function = ModelUtil.producingFunction(collection, it)
			function === null || function.type != EFunctionType.BM25
		].toList
		if (candidates.size != 1) throw Unsupported.of("choosing among several vector fields; add 'using <field>'")
		candidates.head
	}

	def private Function bm25Function(VectorCollection collection, List<Field> fields) {
		val function = collection.functions.findFirst[type == EFunctionType.BM25 && (fields.empty || inputs.exists[fields.contains(it)])]
		if (function === null) throw Unsupported.of('keyword search without a bm25 function on the searched fields')
		function
	}

	def private Expr queryData(QueryInput input, VectorCollection collection, VectorField field, String variable, List<Stmt> prelude) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR: vector(denseKind(field), Literals.of(input.value))
			case SPARSE_VECTOR: vector('sparse', Literals.of(input.value))
			case TEXT: {
				if (ModelUtil.producingFunction(collection, field) === null) {
					throw Unsupported.of('text input for ' + ModelUtil.fieldName(field) + ', which has no server-side embedding or bm25 function')
				}
				vector('text', Literals.of(input.value))
			}
			case ID: {
				val source = input.lookupCollection ?: collection
				prelude += new Code.Let(variable, runtime('lookup_vector', kw('collection_name', text(source.name),
					'field', text(ModelUtil.fieldName(field)), 'id', Literals.of(input.value))))
				vector(denseKind(field), new Code.Ref(variable))
			}
			default: throw Unsupported.of(input.kind.literal + ' query input')
		}
	}

	def private void bounds(ScoreBounds bounds, String metric, boolean nativeScore, Map<String, Expr> knobs, Entries hits) {
		if (bounds === null) return;
		val hasMin = written(bounds, 'min')
		val hasMax = written(bounds, 'max')
		val larger = #['COSINE', 'IP', 'BM25'].contains(metric)
		if (larger) {
			if (!hasMin) {
				if (hasMax) hits.put('max_score', num(bounds.max))
				return
			}
			knobs.put('radius', num(bounds.min))
			if (hasMax) knobs.put('range_filter', num(bounds.max))
			return
		}
		if (nativeScore) {
			if (!hasMax) throw Unsupported.of('a lower distance bound without an upper bound')
			knobs.put('radius', num(bounds.max))
			if (hasMin) knobs.put('range_filter', num(bounds.min))
			return
		}
		if (!hasMin) {
			if (hasMax) hits.put('max_score', num(bounds.max))
			return
		}
		knobs.put('radius', num(distance(metric, bounds.min)))
		if (hasMax) knobs.put('range_filter', num(distance(metric, bounds.max)))
	}

	def private double distance(String metric, double similarity) {
		switch metric {
			case 'L2': {
				if (similarity > 0) throw Unsupported.of('a positive similarity bound on euclidean distance')
				similarity * similarity
			}
			case 'HAMMING': -similarity
			case 'JACCARD': 1 - similarity
			default: similarity
		}
	}

	def private void tuning(SearchParams params, String indexType, Map<String, Expr> knobs, Entries args) {
		if (params === null) return;
		if (written(params, 'targetRecall')) {
			throw Unsupported.of('target-recall: Milvus exposes no query-time recall target')
		}
		if (written(params, 'candidatePool')) {
			val key = if (indexType.startsWith('HNSW')) 'ef' else if (indexType == 'DISKANN') 'search_list' else if (indexType == 'GPU_CAGRA') 'itopk_size' else null
			if (key === null) notes += 'candidates for index type ' + indexType else knobs.put(key, num(params.candidatePool))
		}
		if (written(params, 'probes')) {
			if (indexType.contains('IVF')) knobs.put('nprobe', num(params.probes)) else notes += 'probes for index type ' + indexType
		}
		if (written(params, 'timeoutMillis')) args.put('timeout', num(params.timeoutMillis / 1000.0))
		schema.note(params, 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		knobs.putAll(schema.extValues(params))
	}

	def private Make ranker(BoostBlock boost) {
		if (boost.functions.size != 1) throw Unsupported.of('boosting with several functions; a Milvus function score takes a single function')
		if (written(boost, 'combine') || written(boost, 'apply')) throw Unsupported.of('boost combine and apply modes' + PENDING)
		val function = boost.functions.head
		if (function.kind != EBoostKind.DECAY || function.target === null) throw Unsupported.of(function.kind.literal + ' boost functions' + PENDING)
		if (written(function, 'weight') || function.condition !== null) throw Unsupported.of('weighted or conditional decay boosts' + PENDING)
		val args = kw(
			'strategy', text('decay'),
			'name', text(function.name ?: 'decay'),
			'field', text(MilvusFilter.path(function.target)),
			'function', text(function.shape.literal),
			'origin', timeValue(function.origin),
			'scale', timeValue(function.scale)
		)
		if (function.decayOffset !== null) args.put('offset', timeValue(function.decayOffset))
		if (written(function, 'decayRate')) args.put('decay', num(function.decayRate))
		make('Ranker', args)
	}

	def private Expr timeValue(Literal literal) {
		if (literal instanceof NumberLit) Literals.of(literal) else runtime('time_value', kw('value', Literals.of(literal)))
	}

	def private Expr outputFields(VectorCollection collection, EObject query) {
		outputFields(collection, query, false)
	}

	/** Searches also request the primary key, which some SDKs omit from hits unless it is an output field. */
	def private Expr outputFields(VectorCollection collection, EObject query, boolean withKey) {
		val output = feature(query, 'output') as OutputSpec
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		val withVectors = Boolean.TRUE == feature(query, 'withVectors')
		val List<String> names = newArrayList
		if (withKey) names += primaryKey(collection)
		if (output === null || output.allFields) {
			names += collection.fields.filter(PayloadField).filter[!excluded.contains(it)].map[name]
		} else {
			names += output.fields.filter[!excluded.contains(it)].map[ModelUtil.fieldName(it)]
		}
		if (withVectors) {
			names += ModelUtil.vectorFields(collection).filter[
				val function = ModelUtil.producingFunction(collection, it)
				function === null || function.type != EFunctionType.BM25
			].map[ModelUtil.fieldName(it)]
		}
		texts(names.stream.distinct.toList)
	}

	def private MilvusFilter scopedFilter(VectorCollection collection, FilterBlock block, String namespace, Tenant tenant) {
		val where = new MilvusFilter().where(block)
		val key = ModelUtil.partitionKey(collection)
		val slice = tenant?.name ?: namespace
		if (slice !== null && key !== null) where.add(ModelUtil.fieldName(key) + ' == ' + Py.str(slice))
		where
	}

	def private void partitionNames(Entries args, VectorCollection collection, EObject query) {
		val partitions = (feature(query, 'partitions') ?: newArrayList) as List<Partition>
		val List<String> names = newArrayList
		names += partitions.map[name]
		val tenant = feature(query, 'tenant') as Tenant
		val slice = tenant?.name ?: feature(query, 'namespace') as String
		if (slice !== null && ModelUtil.partitionKey(collection) === null) names += slice
		if (!names.empty) args.put('partition_names', texts(names))
	}

	def private void partitionName(Entries args, VectorCollection collection, String namespace, Tenant tenant, Partition partition) {
		val slice = tenant?.name ?: namespace
		val key = ModelUtil.partitionKey(collection)
		if (partition !== null && key !== null) throw Unsupported.of('explicit partitions together with a partition key')
		if (partition !== null && slice !== null && key === null) throw Unsupported.of('a partition together with a tenant or namespace')
		if (partition !== null) args.put('partition_name', text(partition.name))
		else if (slice !== null && key === null) args.put('partition_name', text(slice))
	}

	def private void writeScope(Entries args, VectorCollection collection, String namespace, Tenant tenant, Partition partition) {
		val key = ModelUtil.partitionKey(collection)
		val slice = tenant?.name ?: namespace
		if (slice !== null && key !== null) {
			args.put('defaults', entries(#{ModelUtil.fieldName(key) -> text(slice)}))
			partitionName(args, collection, null, null, partition)
		} else {
			partitionName(args, collection, namespace, tenant, partition)
		}
	}

	def private void consistency(Entries args, EObject query) {
		if (written(query, 'consistency')) args.put('consistency_level', schema.consistency(feature(query, 'consistency') as EConsistencyLevel))
	}

	def private void extensions(Entries args, EObject host) {
		val values = schema.extValues(host)
		if (!values.empty) args.put('extensions', entries(values))
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
		for (entry : record.entries) result.put(ModelUtil.fieldName(entry.field), Literals.of(entry.value))
		result
	}

	def private String primaryKey(VectorCollection collection) {
		ModelUtil.fieldName(collection.primaryKey)
	}

	def private VectorField vectorFieldOf(VectorIndex index) {
		index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
	}

	def private VectorCollection indexCollection(Index index) {
		val owner = ModelUtil.owningCollection(index)
		if (owner !== null) return owner
		val field = switch index {
			VectorIndex: index.field
			PayloadIndex: index.target.field
		}
		val collection = if (field === null) null else ModelUtil.owningCollection(field)
		if (collection === null) throw Unsupported.of('an index whose field belongs to no collection')
		collection
	}

	def private String defaultDatabase() {
		EcoreUtil2.getAllContentsOfType(script, Connection).map[databaseName].filterNull.head ?: 'default'
	}
}
