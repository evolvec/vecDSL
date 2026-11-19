package org.ftn.vecDSL.generator.lancedb

import java.math.BigDecimal
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
import org.ftn.vecDSL.vecDSL.EFilterStrategy
import org.ftn.vecDSL.vecDSL.EFusionType
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EListKind
import org.ftn.vecDSL.vecDSL.EMatchOperator
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.ESampleMethod
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESnapshotScope
import org.ftn.vecDSL.vecDSL.ESortDirection
import org.ftn.vecDSL.vecDSL.ETextQueryType
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
import org.ftn.vecDSL.vecDSL.HybridLeg
import org.ftn.vecDSL.vecDSL.HybridSearch
import org.ftn.vecDSL.vecDSL.InlineRecords
import org.ftn.vecDSL.vecDSL.Insert
import org.ftn.vecDSL.vecDSL.KeywordLeg
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
import org.ftn.vecDSL.vecDSL.VectorLeg
import org.ftn.vecDSL.vecDSL.VectorSearch
import org.ftn.vecDSL.vecDSL.VendorExtension

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.lancedb.LancedbSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to LanceDB operations in the host-language-neutral code model. Every operation is a call into
 * the per-language runtime module, which holds the SDK calls; the generator builds the table schemas, where strings,
 * update expressions, index plans and search plans, so the three host languages run the same operations on the same files.
 */
class LancedbStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; LancedbCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for LanceDB: '
	/** Suffix of the constructs LanceDB supports that are not generated yet. */
	public static val PENDING = ' has no LanceDB mapping yet'
	public static val NO_MODELS = '; generated LanceDB programs supply their own vectors (embedding functions and rerankers of the SDK registries call model libraries or remote services)'
	public static val NO_ALIASES = 'aliases; LanceDB has no table aliases'
	public static val NO_FUNCTIONS = 'LanceDB computes no columns with server-side functions (full-text search is an index; embedding functions run in the client SDK)'
	public static val NO_SPARSE = 'the Lance type system has no sparse vector type'
	public static val NO_TTL = 'Lance rows do not expire'
	public static val EXTERNAL = 'a LanceDB table is a Lance dataset in its own directory'
	public static val STORAGE = 'an embedded LanceDB keeps each table in a directory of the storage URI'
	public static val EMBEDDED = 'the embedded LanceDB library runs inside the program on one machine'
	public static val NO_AUTH = 'an embedded library has no users or authentication (access is the file system\'s)'
	static val METRICS = 'metrics configuration; the embedded library exports no metrics endpoint'
	static val SCHEDULES = 'backup schedules; an embedded library runs no scheduler (tags are created by the program)'
	static val NO_TRANSACTIONS = 'multi-statement transactions; LanceDB commits each write to one table as its own version (single-table commits are atomic)'
	static val NO_GENERATION = 'LanceDB has no generation API (its queries return rows)'
	static val NO_RERANK = 'rerankers; the reranker classes of the SDKs call model libraries or remote services'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain', 'mmr' -> 'mmr',
		'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch',
		'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model'
	}

	/** Why LanceDB cannot express a query clause. */
	static val REASONS = #{
		'explain' -> 'LanceDB explains query plans (explain_plan), not scores',
		'highlighted' -> 'LanceDB full-text search returns no highlighted fragments',
		'reranker' -> NO_RERANK,
		'elementScope' -> 'a LanceDB query ranks rows, not list elements',
		'queryModel' -> 'generated LanceDB programs embed no query text'
	}

	/** Clauses LanceDB can carry that are not generated yet: the runtime work they need. */
	static val LATER = #{
		'cursor' -> 'keyset pagination after the key of the last hit',
		'withTotal' -> 'count_rows of the where string beside the hits',
		'mmr' -> 'maximal marginal relevance over the candidates in the runtime',
		'boost' -> 'score functions over the hits in the runtime',
		'aggregations' -> 'aggregation over a scan in the runtime',
		'prefetches' -> 'staged nearest-neighbour queries'
	}

	val List<String> notes = newArrayList
	val LancedbSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	val java.util.Map<String, LancedbTable> tagged = newHashMap
	var String current

	new(Script script) {
		this.script = script
		this.schema = new LancedbSchema(notes, script)
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
			VectorCollection: result += schema.createTable(concept, ifNotExists)
			Connection: connection(concept)
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(METRICS)
				if (concept.connection !== null) connection(concept.connection)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists)
				for (database : concept.databases) result += define(database, ifNotExists)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of('cluster limits; ' + EMBEDDED)
				if (concept.replication !== null || concept.sharding !== null) throw Unsupported.of('replication and sharding; ' + EMBEDDED)
				for (name : ModelUtil.extParams(concept, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				for (collection : concept.collections) result += schema.createTable(collection, ifNotExists)
			}
			Database: {
				LancedbSchema.checkName(concept.name)
				result += run(runtime('create_database', kw('database', text(concept.name))))
				for (collection : concept.collections) result += schema.createTable(collection, ifNotExists)
			}
			Alias: throw Unsupported.of(NO_ALIASES)
			VectorIndex: result += createVectorIndex(concept)
			PayloadIndex: result += createPayloadIndex(concept)
			VectorField: result += addField(member(concept, 'a vector field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += requireTable(member(concept, 'a partition'))
			Tenant: result += requireTable(member(concept, 'a tenant'))
			Namespace: result += requireTable(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + NO_FUNCTIONS)
			FileResource: throw Unsupported.of('file resources; LanceDB full-text indexes read no dictionaries or synonym files')
			Analyzer: schema.tokenizer(concept)
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer !== null) schema.tokenizer(analyzer)
			}
			FieldLimit: throw Unsupported.of('a field limit outside a field declaration; Lance tables have no constraints')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of(NO_RERANK)
			Quantization: throw Unsupported.of('quantization outside a vector index; LanceDB quantizes vectors in the index (IVF_PQ, IVF_RQ, IVF_HNSW_SQ, IVF_HNSW_PQ)')
			ReplicationConfig: throw Unsupported.of('replication; ' + EMBEDDED)
			ShardConfig: throw Unsupported.of('sharding; ' + EMBEDDED)
			StorageConfig: throw Unsupported.of('storage settings; ' + STORAGE)
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_AUTH)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of('cluster limits; ' + EMBEDDED)
			CollectionLimit: throw Unsupported.of('collection limits (' + concept.type.literal + '); LanceDB OSS has no table limits')
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The storage URI: checked here, opened by connect(). A remote URI (db://) needs LanceDB Cloud or Enterprise. */
	def private void connection(Connection concept) {
		if (concept.host !== null) throw Unsupported.of("'host' connections; " + EMBEDDED + " (LanceDB Cloud and Enterprise are reached through db:// URIs and an API key)")
		if (concept.uri !== null && concept.uri.startsWith('db://')) throw Unsupported.of('LanceDB Cloud and Enterprise URIs (db://); the generated programs use the embedded library')
		if (concept.deployment !== null && written(concept, 'deployment') && concept.deployment != EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('deployment ' + concept.deployment.literal + '; ' + EMBEDDED)
		}
		if (concept.apiKey !== null || concept.token !== null || concept.password !== null || written(concept, 'username')) {
			throw Unsupported.of('credentials; ' + NO_AUTH)
		}
		if (concept.tls || written(concept, 'grpcPort')) throw Unsupported.of('tls and grpc-port; ' + EMBEDDED)
		if (concept.databaseName !== null) LancedbSchema.checkName(concept.databaseName)
		for (name : ModelUtil.extParams(concept, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

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
		val target = ModelUtil.targetCollection(concept)
		if (target !== null) return target
		throw Unsupported.of(what + ' outside a collection; LanceDB adds it only to a table')
	}

	def private List<Stmt> requireTable(VectorCollection collection) {
		#[run(runtime('require_table', kw('table', schema.table(collection).place)))]
	}

	def private List<Stmt> createVectorIndex(VectorIndex index) {
		val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		val table = schema.table(collection)
		val plan = schema.vectorIndex(index, field, table.vectors.get(schema.column(field)) ?: schema.metricName(field, index))
		schema.evolve(collection, table.withIndex(plan))
		#[run(runtime('create_index', kw('table', table.place, 'index', plan)))]
	}

	def private List<Stmt> createPayloadIndex(PayloadIndex index) {
		val field = index.target.field
		val collection = ModelUtil.targetCollection(index) ?: (if (field === null) null else ModelUtil.owningCollection(field))
		if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
		val table = schema.table(collection)
		val plan = schema.payloadIndex(collection, index)
		schema.evolve(collection, table.withIndex(plan))
		#[run(runtime('create_index', kw('table', table.place, 'index', plan)))]
	}

	/** A field added to an existing table: a nullable column; a default fills existing rows through a SQL expression. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val name = schema.column(field)
		if (table.has(name)) throw Unsupported.of('adding field ' + name + ', which the table already has')
		val column = schema.addedField(collection, field)
		val args = kw('table', table.place, 'fields', items(#[column]))
		if (field instanceof VectorField && (field as VectorField).index !== null) {
			throw Unsupported.of('a vector index declared with an added field; LanceDB indexes existing columns (create the index after the field)')
		}
		var next = table.withField(column)
		if (field instanceof VectorField) next = next.withVector(name, schema.metricName(field, null))
		if (field instanceof PayloadField) {
			if (field.defaultValue !== null) {
				val sql = new LancedbSql('d')
				val value = sql.value(Literals.of(field.defaultValue), false)
				if (!LancedbSql.constant(Literals.of(field.defaultValue))) throw Unsupported.of('a default from a parameter; the generator writes the default into the column expression')
				args.put('values', kw(name, text('CAST(' + value + ' AS ' + castType(field) + ')')))
			}
			if (schema.searchable(field)) {
				val plan = schema.fts(name + '_fts', name, field.analyzer)
				next = next.withIndex(plan)
				schema.evolve(collection, next)
				return #[run(runtime('add_columns', args)), run(runtime('create_index', kw('table', table.place, 'index', plan)))]
			}
		}
		schema.evolve(collection, next)
		#[run(runtime('add_columns', args))]
	}

	/** The SQL type of a default expression (DataFusion names). */
	def private String castType(PayloadField field) {
		switch field.type {
			case INT: 'INT'
			case BIGINT: 'BIGINT'
			case INT8: 'TINYINT'
			case FLOAT: 'FLOAT'
			case DOUBLE: 'DOUBLE'
			case BOOL: 'BOOLEAN'
			case VARCHAR,
			case TEXT,
			case UUID: 'VARCHAR'
			case DATE: 'DATE'
			case DATETIME: 'TIMESTAMP'
			default: throw Unsupported.of('a default on a ' + field.type.literal + ' field; the generator writes defaults of scalar columns')
		}
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val name = schema.column(field)
		if (name == table.key) throw Unsupported.of('dropping the primary key; the generator keys rows by it')
		if (!table.has(name)) throw Unsupported.of('dropping field ' + name + ', which the table no longer has')
		schema.evolve(collection, table.withoutField(name))
		#[run(runtime('drop_columns', kw('table', table.place, 'names', texts(#[name]))))]
	}

	def private List<Stmt> renameField(VectorCollection collection, RenameFieldOp operation) {
		val table = schema.table(collection)
		val name = schema.column(operation.field)
		if (!table.has(name)) throw Unsupported.of('renaming field ' + name + ', which the table no longer has')
		if (table.has(operation.newName)) throw Unsupported.of('renaming field ' + name + ' to ' + operation.newName + ', which the table already has')
		schema.evolve(collection, table.withRenamedField(name, operation.newName))
		schema.rename(operation.field, operation.newName)
		#[run(runtime('rename_column', kw('table', table.place, 'name', text(name), 'new_name', text(operation.newName))))]
	}

	/** Collection properties: description and tags live in the metadata of the key column. */
	def private List<Stmt> setProperty(VectorCollection collection, SetPropertyOp operation) {
		val table = schema.table(collection)
		val value = Literals.of(operation.value)
		switch operation.property {
			case DESCRIPTION: #[run(runtime('set_metadata', kw('table', table.layout, 'metadata', kw(LancedbSchema.DESCRIPTION, value))))]
			case TAGS: #[run(runtime('set_metadata', kw('table', table.layout, 'metadata', kw(LancedbSchema.TAGS, value))))]
			case DYNAMIC: {
				if (!(value instanceof Code.Flag) || (value as Code.Flag).value) {
					throw Unsupported.of('dynamic true; a Lance table has a declared Arrow schema (add-field adds columns)')
				}
				requireTable(collection)
			}
			case CONSISTENCY: {
				notes += 'consistency on SetPropertyOp, since every read checks for newer versions (read_consistency_interval 0)'
				requireTable(collection)
			}
			case TTL: throw Unsupported.of('collection ttl; ' + NO_TTL)
			case DELETION_PROTECTION: throw Unsupported.of('deletion-protection; a LanceDB table is a directory that drop_table removes')
		}
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; LanceDB alters tables')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (newName !== null) {
			LancedbSchema.checkName(newName)
			val table = schema.table(collection)
			result += run(runtime('rename_table', kw('table', table.place, 'new_name', text(newName), 'schema', table.schema, 'indexes', table.indexPlans)))
			schema.evolve(collection, table.renamed(newName))
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
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + NO_FUNCTIONS)
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + NO_FUNCTIONS)
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val args = kw('table', schema.placeOf(concept))
				schema.evolve(concept, null)
				if (ifExists) args.put('missing_ok', flag(true))
				#[run(runtime('drop_table', args))]
			}
			Database: {
				LancedbSchema.checkName(concept.name)
				#[run(runtime('drop_database', kw('database', text(concept.name))))]
			}
			Tenant: dropSlice(member(concept, 'a tenant'), schema.table(member(concept, 'a tenant')).tenant, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), schema.table(member(concept, 'a namespace')).namespace, concept.key)
			Partition: dropSlice(member(concept, 'a partition'), LancedbSchema.PARTITION_COLUMN, concept.name)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a vector field'), concept)
			VectorIndex: {
				val field = concept.field ?: EcoreUtil2.getContainerOfType(concept, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				dropIndex(member(field, 'a vector index'), concept.name)
			}
			PayloadIndex: {
				val field = concept.target.field
				val collection = ModelUtil.targetCollection(concept) ?: (if (field === null) null else ModelUtil.owningCollection(field))
				if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
				dropIndex(collection, concept.name)
			}
			Alias: throw Unsupported.of(NO_ALIASES)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + NO_FUNCTIONS)
			Quantization: throw Unsupported.of('dropping quantization; drop the vector index that quantizes')
			Connection: throw Unsupported.of('dropping a connection; a connection is the storage URI of the program, not a LanceDB object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; LanceDB drops tables and logical databases')
			Cluster: throw Unsupported.of('dropping a cluster; ' + EMBEDDED)
			Analyzer: throw Unsupported.of('dropping an analyzer; an analyzer is the options of FTS indexes (drop the index)')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; an analyzer is the options of FTS indexes (drop the index)')
			FieldLimit: throw Unsupported.of('dropping a field limit; Lance tables have no constraints')
			FileResource: throw Unsupported.of('file resources; LanceDB full-text indexes read no dictionaries or synonym files')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of(NO_RERANK)
			ReplicationConfig: throw Unsupported.of('replication; ' + EMBEDDED)
			ShardConfig: throw Unsupported.of('sharding; ' + EMBEDDED)
			StorageConfig: throw Unsupported.of('storage settings; ' + STORAGE)
			AuthConfig: throw Unsupported.of('authentication; ' + NO_AUTH)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_AUTH)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of(SCHEDULES)
			ClusterLimit: throw Unsupported.of('cluster limits; ' + EMBEDDED)
			CollectionLimit: throw Unsupported.of('collection limits; LanceDB OSS has no table limits')
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	def private List<Stmt> dropIndex(VectorCollection collection, String index) {
		val table = schema.table(collection)
		schema.evolve(collection, table.withoutIndex(index))
		#[run(runtime('drop_index', kw('table', table.place, 'name', text(index))))]
	}

	/** Dropping a tenant, namespace or partition deletes its rows. */
	def private List<Stmt> dropSlice(VectorCollection collection, String column, String value) {
		val table = schema.table(collection)
		if (column === null) throw Unsupported.of('a slice the collection does not declare')
		#[new Code.Return(runtime('delete', kw('table', table.layout, 'where', text(LancedbSql.id(column) + ' = ' + LancedbSql.text(value)))))]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		throw Unsupported.of(NO_ALIASES)
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; ' + NO_TTL)
		schema.note(it, 'recordCountHint')
		val table = schema.table(collection)
		val args = writeArgs(it, table, collection)
		args.put('rows', rows(source))
		if (written(it, 'onConflict')) args.put('on_conflict', text(conflict(onConflict)))
		writeOptions(it)
		#[new Code.Return(runtime('insert', args))]
	}

	def private String conflict(EConflictPolicy policy) {
		switch policy {
			case ERROR: 'error'
			case IGNORE: 'skip'
			case UPDATE,
			case REPLACE: 'update'
			default: throw Unsupported.of('on-conflict ' + policy.literal)
		}
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; ' + NO_TTL)
		schema.note(it, 'recordCountHint')
		val table = schema.table(collection)
		val args = writeArgs(it, table, collection)
		args.put('rows', rows(source))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		writeOptions(it)
		#[new Code.Return(runtime('upsert', args))]
	}

	/** Table, schema, field defaults and the slice values a write puts into the tenant, namespace and partition columns. */
	def private Entries writeArgs(EObject statement, LancedbTable table, VectorCollection collection) {
		val args = kw('table', table.layout, 'schema', table.schema)
		val defaults = entries
		for (field : ModelUtil.payloadFields(collection).filter[defaultValue !== null && table.has(schema.column(it))]) {
			defaults.put(schema.column(field), Literals.of(field.defaultValue))
		}
		if (!defaults.entries.empty) args.put('defaults', defaults)
		val slice = entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) slice.put(table.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) slice.put(table.namespace, text(namespace))
		val partition = feature(statement, 'partition') as Partition
		if (partition !== null) slice.put(LancedbSchema.PARTITION_COLUMN, text(partition.name))
		else if (table.partition !== null) slice.put(LancedbSchema.PARTITION_COLUMN, text(defaultPartition(collection)))
		if (!slice.entries.empty) args.put('slice', slice)
		args
	}

	def private String defaultPartition(VectorCollection collection) {
		val declared = collection.defaultPartition ?: ModelUtil.partitions(collection).findFirst[isDefault]
		if (declared === null) throw Unsupported.of('a write without a partition to a partitioned collection without a default partition')
		declared.name
	}

	/**
	 * Updates by id or filter. Scalar values become SQL expressions of one UPDATE (literals quoted by the generator,
	 * parameters bound by the runtime); vector, list and struct values, replace mode and update from records rewrite the
	 * matching rows through a merge insert on the key. array-append and array-remove are array_append and array_remove_all.
	 */
	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Lance versions whole tables, not records (a write commits a new table version)')
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val table = schema.table(collection)
		val where = filterOf(it, table, filter, 'u')
		if (!ids.empty) where.keys(table.key, ids.map[Literals.of(it)].toList)
		if (source !== null) {
			if (values !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty) {
				throw Unsupported.of("update from records with 'set', remove-fields or array operations")
			}
			val args = kw('table', table.layout, 'schema', table.schema, 'rows', rows(source))
			if (!where.empty) args.put('where', where.code)
			return #[new Code.Return(runtime('update_from', args))]
		}
		if (where.empty) throw Unsupported.of('an update without ids or a filter')
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			if (!removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty) throw Unsupported.of('replace mode with remove-fields or array operations')
			val args = kw('table', table.layout, 'schema', table.schema, 'where', where.code, 'values', if (values === null) entries else record(values))
			return #[new Code.Return(runtime('replace_rows', args))]
		}
		val sql = new LancedbSql('v')
		val expressions = entries
		val rewritten = entries
		val Iterable<Field> arrays = appendFields + arrayRemoveFields
		if (values !== null) {
			for (entry : values.entries) {
				val columnName = schema.column(entry.field)
				if (columnName == table.key) throw Unsupported.of('updating the primary key; the generator keys rows by it')
				if (!arrays.toList.contains(entry.field)) {
					if (scalarField(entry.field)) expressions.put(columnName, sqlValue(sql, Literals.of(entry.value)))
					else rewritten.put(columnName, Literals.of(entry.value))
				}
			}
		}
		for (field : removedFields) {
			val columnName = schema.column(field)
			if (columnName == table.key) throw Unsupported.of('removing the primary key')
			expressions.put(columnName, text('NULL'))
		}
		for (field : appendFields) expressions.put(schema.column(field), arrayExpression(it, 'array_concat', field))
		for (field : arrayRemoveFields) expressions.put(schema.column(field), arrayExpression(it, 'array_remove', field))
		val List<Stmt> result = newArrayList
		if (!rewritten.entries.empty) {
			if (!expressions.entries.empty) throw Unsupported.of('one update of scalar and vector, list or struct values; the generator writes them in separate updates')
			return #[new Code.Return(runtime('patch_rows', kw('table', table.layout, 'schema', table.schema, 'where', where.code, 'values', rewritten)))]
		}
		if (expressions.entries.empty) throw Unsupported.of('an update without values')
		result += new Code.Return(runtime('update', kw('table', table.layout, 'where', where.code, 'values', expressions)))
		result
	}

	def private boolean scalarField(Field field) {
		field instanceof PayloadField && (field as PayloadField).subFields.empty
			&& !#[EScalarDType.ARRAY, EScalarDType.STRUCT].contains((field as PayloadField).type)
	}

	def private Expr sqlValue(LancedbSql sql, Expr value) {
		val token = sql.value(value, false)
		if (LancedbSql.constant(value)) text(token) else new Code.Filter(token, newLinkedHashMap(token.substring(1, token.length - 1) -> value))
	}

	/** array_append over every item of the set record's list (array_concat), or array_remove_all of each item. */
	def private Expr arrayExpression(Update update, String function, Field field) {
		if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) {
			throw Unsupported.of('array-append or array-remove on ' + schema.column(field) + ', which is not an array field')
		}
		val entry = update.values?.entries?.findFirst[e | e.field === field]
		val name = LancedbSql.id(schema.column(field))
		if (entry === null) throw Unsupported.of('array-append or array-remove on ' + schema.column(field) + " without items in 'set'")
		val items = Literals.of(entry.value)
		if (!LancedbSql.constant(items) || !(items instanceof Code.Items)) throw Unsupported.of('array items that are not a list literal')
		var expression = name
		if (function == 'array_concat') {
			expression = 'array_concat(' + name + ', ' + LancedbSql.literal(items) + ')'
		} else {
			for (item : (items as Code.Items).items) expression = 'array_remove_all(' + expression + ', ' + LancedbSql.literal(item) + ')'
		}
		text(expression)
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val table = schema.table(collection)
		val where = filterOf(it, table, filter, 'w')
		if (!ids.empty) where.keys(table.key, ids.map[Literals.of(it)].toList)
		val args = kw('table', table.layout)
		if (where.empty) {
			if (!deleteAll) throw Unsupported.of('a delete without ids, filter or all')
			args.put('where', text('true'))
		} else {
			args.put('where', where.code)
		}
		if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		#[new Code.Return(runtime('delete', args))]
	}

	/** A local JSON or JSON Lines file written as an insert in batches; LanceDB has no server-side import. */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; ' + EMBEDDED + ' and runs no ingestion pipeline')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + ' URIs' + PENDING + ' (the runtimes read local files)')
		if (written(it, 'format') && format != EImportFormat.JSON) throw Unsupported.of('import format ' + format.literal + PENDING + ' (the runtimes read JSON and JSON Lines)')
		schema.note(it, 'parallelism')
		if (async) notes += 'async on BulkImport, which the runtime finishes before returning'
		writeOptions(it)
		val table = schema.table(collection)
		val args = writeArgs(it, table, collection)
		args.put('location', text(location))
		if (written(it, 'onConflict')) args.put('on_conflict', text(conflict(onConflict)))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		#[new Code.Return(runtime('import_file', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of(NO_TRANSACTIONS)
	}

	/** A LanceDB write returns after its commit, so 'wait' and write consistency need no call; extensions are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (name : ModelUtil.extParams(statement, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** A statement's filter with the conditions of its tenant, namespace and partitions. */
	def private LancedbFilter filterOf(EObject statement, LancedbTable table, FilterBlock block, String prefix) {
		val result = new LancedbFilter(prefix, [schema.column(it)]).where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.equal(table.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.equal(table.namespace, text(namespace))
		val partition = feature(statement, 'partition')
		if (partition instanceof Partition) result.equal(LancedbSchema.PARTITION_COLUMN, text(partition.name))
		val partitions = feature(statement, 'partitions')
		if (partitions instanceof List<?> && !(partitions as List<?>).empty) {
			result.where(LancedbSql.id(LancedbSchema.PARTITION_COLUMN) + ' IN '
				+ (partitions as List<?>).filter(Partition).map[LancedbSql.text(name)].join('(', ', ', ')', [it]))
		}
		result
	}

	// ---------------------------------------------------------------- operational

	/** Opens the table and loads its indexes into the cache (prewarm_index; prewarm_data is remote-only in LanceDB OSS). */
	def dispatch List<Stmt> body(Load it) {
		if (!partitions.empty) throw Unsupported.of('loading partitions; a partition is a value of a column of the table')
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[run(runtime('load_table', kw('table', schema.table(collection).place)))]
	}

	/** Each call opens its table, so nothing stays loaded between statements. */
	def dispatch List<Stmt> body(Release it) {
		if (!partitions.empty) throw Unsupported.of('releasing partitions; a partition is a value of a column of the table')
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		requireTable(collection)
	}

	/** A LanceDB write is committed (data files and manifest written) before the call returns. */
	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		requireTable(collection)
	}

	/** optimize: compacts small data files and adds new rows to every index. */
	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'targetSegments', 'async')
		#[run(runtime('optimize', kw('table', schema.table(collection).place)))]
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = kw('table', schema.table(collection).layout)
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + NO_MODELS)
	}

	/** A snapshot is a tag on the table's current version. */
	def dispatch List<Stmt> body(Snapshot it) {
		snapshotScope(scope, written(it, 'scope'))
		if (location !== null) throw Unsupported.of("a snapshot 'location'; LanceDB tags live in the table directory")
		if (tenant !== null) throw Unsupported.of('tenant snapshots; a tag covers the whole table')
		if (collection === null) throw Unsupported.of('a snapshot without a collection; LanceDB tags versions of one table')
		schema.note(it, 'incremental', 'description')
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(collection)
		val args = kw('table', table.place)
		if (snapshotName !== null) {
			args.put('tag', text(snapshotName))
			tagged.put(snapshotName, table)
		}
		#[new Code.Return(runtime('create_tag', args))]
	}

	def private void snapshotScope(ESnapshotScope scope, boolean declared) {
		if (declared && scope != ESnapshotScope.COLLECTION) throw Unsupported.of(scope.literal + ' snapshots; LanceDB tags versions of one table')
	}

	/** Restores a tagged version: checkout of the tag, then restore commits it as the newest version. */
	def dispatch List<Stmt> body(Restore it) {
		snapshotScope(scope, written(it, 'scope'))
		if (location !== null) throw Unsupported.of("a restore 'location'; LanceDB tags live in the table directory")
		if (collection === null) throw Unsupported.of('a restore without a collection; LanceDB restores versions of one table')
		if (async) notes += 'async on Restore, which commits before returning'
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val restore = runtime('restore_tag', kw('table', schema.table(collection).place, 'tag', text(snapshotName)))
		val restored = tagged.get(snapshotName)
		if (restored !== null) schema.evolve(collection, restored)
		#[new Code.Return(restore)]
	}

	/** Deletes the tag; the table is that of the script's snapshot statement of the same name. */
	def dispatch List<Stmt> body(DropSnapshot it) {
		if (location !== null) throw Unsupported.of("a snapshot 'location'; LanceDB tags live in the table directory")
		val tagName = snapshotName
		val created = EcoreUtil2.getAllContentsOfType(script, Snapshot).findFirst[s | s.snapshotName == tagName && s.collection !== null]
		if (created === null) throw Unsupported.of('dropping a snapshot this script does not create; LanceDB tags belong to a table, which the drop statement does not name')
		#[run(runtime('drop_tag', kw('table', schema.table(created.collection).place, 'tag', text(tagName))))]
	}

	/** A shallow clone references the source's files (clone_table); a snapshot clone copies the rows into a new table. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; LanceDB clones tables')
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		LancedbSchema.checkName(targetName)
		val table = schema.table(sourceCollection)
		val shallow = !(written(it, 'mode') && mode == ECloneMode.SNAPSHOT)
		val args = kw('table', table.place, 'target', text(targetName), 'shallow', flag(shallow), 'schema', table.schema, 'indexes', table.indexPlans)
		if (asOf !== null) args.put('version', Literals.of(asOf))
		#[new Code.Return(runtime('clone_table', args))]
	}

	/** Retrains an index over every row: create_index with replace. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (name : ModelUtil.extParams(it, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (concurrently) notes += 'concurrently on RebuildIndex; LanceDB builds the new index beside the old one and commits it as a new version'
		val definition = index
		switch definition {
			VectorIndex: {
				val field = definition.field ?: EcoreUtil2.getContainerOfType(definition, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				val table = schema.table(member(field, 'a vector index'))
				#[run(runtime('rebuild_index', kw('table', table.place, 'index', schema.vectorIndex(definition, field, table.vectors.get(schema.column(field)) ?: schema.metricName(field, definition)))))]
			}
			PayloadIndex: {
				val field = definition.target.field
				val collection = ModelUtil.targetCollection(definition) ?: (if (field === null) null else ModelUtil.owningCollection(field))
				if (collection === null) throw Unsupported.of('a payload index on a raw path outside a collection')
				#[run(runtime('rebuild_index', kw('table', schema.table(collection).place, 'index', schema.payloadIndex(collection, definition))))]
			}
			default: throw Unsupported.of(NOT_MAPPED + definition.eClass.name)
		}
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of('collections derived from external sources; ' + EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val table = schema.table(collection)
		if (namespace !== null && table.namespace === null) throw Unsupported.of('a namespace the collection does not declare')
		val where = if (namespace === null) text('true') else text(LancedbSql.id(table.namespace) + ' = ' + LancedbSql.text(namespace))
		#[new Code.Return(runtime('truncate', kw('table', table.layout, 'where', where)))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = scope
		val List<Stmt> result = newArrayList
		val args = entries
		switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) args.put('database', text(owner.name))
				else if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; LanceDB lists the tables of a database')
				listBounds(it, args)
				result += new Code.Return(runtime('list_tables', args))
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				if (prefix !== null || written(it, 'listLimit')) throw Unsupported.of('prefix and limit on databases')
				result += new Code.Return(runtime('list_databases', args))
			}
			case INDEXES,
			case SNAPSHOTS: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection; LanceDB lists them per table')
				args.put('table', schema.table(owner as VectorCollection).place)
				listBounds(it, args)
				result += new Code.Return(runtime(if (kind == EListKind.INDEXES) 'list_indexes' else 'list_tags', args))
			}
			case TENANTS,
			case NAMESPACES,
			case PARTITIONS: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection; they are values of a column per table')
				val table = schema.table(owner as VectorCollection)
				val column = switch kind { case TENANTS: table.tenant case NAMESPACES: table.namespace default: table.partition }
				if (column === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without them')
				args.put('table', table.layout)
				args.put('column', text(column))
				listBounds(it, args)
				result += new Code.Return(runtime('list_values', args))
			}
			case ALIASES: throw Unsupported.of(NO_ALIASES)
		}
		result
	}

	def private void listBounds(ListObjects list, Entries args) {
		if (list.prefix !== null) args.put('prefix', text(list.prefix))
		if (written(list, 'listLimit')) args.put('limit', num(list.listLimit))
	}

	/** Index builds finish inside create_index in the open-source library; wait_for_index confirms the index and reports its coverage. */
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
		schema.note(it, 'state')
		val args = kw('table', schema.table(collection).place, 'name', text(definition.name))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'explain', 'reranker', 'highlighted', 'queryModel')
		later(it, 'cursor', 'withTotal', 'mmr', 'boost', 'aggregations', 'prefetches')
		val table = schema.table(collection)
		val field = vectorField(collection, vectorField)
		val plan = vectorPlan(table, field, if (written(it, 'metric')) it.metric else null, params, exact, filterOf(it, table, filter, 'f'))
		queryExtensions(it)
		val List<Stmt> prelude = newArrayList
		val args = kw('table', table.layout, 'plan', plan, 'vector', queryVector(input, collection, field, prelude), 'limit', num(topK))
		common(it, table, collection, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('vector_search', args))
		result
	}

	/** The vector field a query names, else the collection's only vector field. */
	def private VectorField vectorField(VectorCollection collection, VectorField named) {
		val fields = ModelUtil.vectorFields(collection)
		if (named !== null) {
			if (!fields.contains(named)) throw Unsupported.of('a vector field outside the collection')
			return named
		}
		if (fields.size != 1) throw Unsupported.of('a query without using on a collection with ' + fields.size + ' vector fields')
		fields.head
	}

	/** Column, distance type, where string and search settings of a nearest-neighbour query. */
	def private Entries vectorPlan(LancedbTable table, VectorField field, EMetric metric, SearchParams params, boolean exact, LancedbFilter where) {
		val column = schema.column(field)
		if (!table.has(column)) throw Unsupported.of('vector field ' + column + ', which the table no longer has')
		val declared = table.vectors.get(column)
		if (metric !== null && LancedbSchema.lance(metric) != declared) {
			throw Unsupported.of('a query metric other than the metric of the vector field; the generator ranks by the field metric')
		}
		val plan = kw('column', text(column), 'metric', text(declared))
		if (field.multiVector) plan.put('multivector', flag(true))
		if (field.type == org.ftn.vecDSL.vecDSL.EVectorDType.BINARY_VECTOR) plan.put('bits', flag(true))
		if (!where.empty) plan.put('where', where.code)
		if (exact) plan.put('exact', flag(true))
		searchParams(params, plan)
		plan
	}

	/** Search effort: probes is nprobes, candidates ef (HNSW), rescore and oversampling refine_factor, filter strategy prefilter. */
	def private void searchParams(SearchParams params, Entries plan) {
		if (params === null) return;
		if (written(params, 'targetRecall')) throw Unsupported.of('target-recall; LanceDB exposes no query-time recall target (nprobes, ef and refine_factor are effort settings)')
		if (written(params, 'scanBudget')) throw Unsupported.of('scan-budget; LanceDB bounds a search by nprobes and ef')
		if (params.exhaustive) plan.put('exact', flag(true))
		if (written(params, 'probes')) plan.put('nprobes', num(params.probes))
		if (written(params, 'candidatePool')) plan.put('ef', num(params.candidatePool))
		if (written(params, 'oversampling')) plan.put('refine_factor', num(Math.max(1, Math.ceil(params.oversampling))))
		else if (params.rescore) plan.put('refine_factor', num(1))
		if (written(params, 'filterStrategy')) {
			switch params.filterStrategy {
				case POST_FILTER: plan.put('prefilter', flag(false))
				case PRE_FILTER,
				case AUTO: plan.put('prefilter', flag(true))
				case ITERATIVE: throw Unsupported.of('the iterative filter strategy; LanceDB filters before or after the vector search')
			}
		}
		schema.note(params, 'timeoutMillis')
		for (name : ModelUtil.extParams(params, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	def private Expr queryVector(QueryInput input, VectorCollection collection, VectorField field, List<Stmt> prelude) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR: {
				if (field.multiVector) throw Unsupported.of('a single query vector on multivector field ' + schema.column(field) + '; query it with multi_vector')
				Literals.of(input.value)
			}
			case MULTI_VECTOR: {
				if (!field.multiVector) throw Unsupported.of('a multi_vector query on ' + schema.column(field) + ', which is not a multivector field')
				Literals.of(input.value)
			}
			case ID: {
				val source = schema.table(input.lookupCollection ?: collection)
				prelude += new Code.Let('query_vector', runtime('lookup_vector', kw('table', source.layout, 'column', text(schema.column(field)), 'id', Literals.of(input.value))))
				new Code.Ref('query_vector')
			}
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			default: throw Unsupported.of(input.kind.literal + ' query input; ' + NO_SPARSE + ' and the programs embed no images')
		}
	}

	/** Full-text search over the FTS indexes of the listed fields, scored by BM25 (_score). */
	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'explain', 'reranker', 'highlighted')
		later(it, 'cursor', 'withTotal', 'mmr', 'boost', 'aggregations')
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on KeywordSearch, since BM25 is the keyword score'
		val table = schema.table(collection)
		val plan = textPlan(table, collection, searchFields.map[field], searchFields.map[s | if (written(s, 'boost')) s.boost else null],
			if (written(it, 'textQueryType')) textQueryType else null, if (written(it, 'matchOperator')) matchOperator else null,
			if (written(it, 'minShouldMatch')) minShouldMatch else null, if (written(it, 'fuzziness')) fuzziness else null,
			if (written(it, 'slop')) slop else null)
		val where = filterOf(it, table, filter, 'f')
		if (!where.empty) plan.put('where', where.code)
		if (params !== null) keywordParams(params, plan)
		queryExtensions(it)
		val args = kw('table', table.layout, 'plan', plan, 'text', queryText(input), 'limit', num(topK))
		common(it, table, collection, args)
		#[new Code.Return(runtime('keyword_search', args))]
	}

	def private void keywordParams(SearchParams params, Entries plan) {
		if (written(params, 'filterStrategy')) {
			if (params.filterStrategy == EFilterStrategy.POST_FILTER) plan.put('prefilter', flag(false))
			else if (params.filterStrategy == EFilterStrategy.ITERATIVE) throw Unsupported.of('the iterative filter strategy; LanceDB filters before or after the search')
		}
		if (written(params, 'targetRecall') || written(params, 'probes') || written(params, 'candidatePool') || written(params, 'oversampling') || params.rescore || params.exhaustive || written(params, 'scanBudget')) {
			throw Unsupported.of('vector search settings on a keyword search')
		}
		schema.note(params, 'timeoutMillis')
		for (name : ModelUtil.extParams(params, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	def private Expr queryText(QueryInput input) {
		if (input === null || input.kind != EQueryInputMode.TEXT) throw Unsupported.of('a keyword search without a text query')
		Literals.of(input.value)
	}

	/**
	 * The full-text query: match (MatchQuery, or MultiMatchQuery over several fields with per-field boosts), phrase
	 * (PhraseQuery with slop) or fuzzy (MatchQuery with fuzziness); the operator joins the terms.
	 */
	def private Entries textPlan(LancedbTable table, VectorCollection collection, List<Field> listed, List<Double> boosts,
		ETextQueryType type, EMatchOperator operator, Integer minimum, String fuzziness, Integer slop) {
		val List<String> columns = newArrayList
		val searched = if (listed.empty) ModelUtil.payloadFields(collection).filter[schema.searchable(it)].toList as List<? extends Field> else listed
		if (searched.empty) throw Unsupported.of('a keyword search on a collection without full-text fields')
		for (field : searched) {
			val name = schema.column(field)
			if (!table.indexes.exists[i | (i.get('kind') as Code.Text).value == 'fts' && (i.get('column') as Code.Text).value == name]) {
				throw Unsupported.of('a keyword search on ' + name + ', which has no full-text index; LanceDB searches FTS-indexed columns')
			}
			columns += name
		}
		val plan = kw('columns', texts(columns))
		val weights = entries
		for (var i = 0; i < columns.size; i++) {
			if (boosts.size > i && boosts.get(i) !== null) weights.put(columns.get(i), num(boosts.get(i)))
		}
		if (!weights.entries.empty) plan.put('boosts', weights)
		if (operator == EMatchOperator.AND) plan.put('operator', text('and'))
		if (minimum !== null) throw Unsupported.of('min-should-match; LanceDB full-text queries join terms with and or or')
		if (type == ETextQueryType.PHRASE) {
			plan.put('kind', text('phrase'))
			if (slop !== null) plan.put('slop', num(slop))
		} else if (type == ETextQueryType.FUZZY) {
			plan.put('fuzziness', num(1))
		} else if (type !== null && type != ETextQueryType.MATCH) {
			throw Unsupported.of('text-query ' + type.literal + '; LanceDB full-text queries are match, phrase, boolean, boost and multi_match (no prefix, wildcard, regexp or query string)')
		}
		if (slop !== null && type != ETextQueryType.PHRASE) throw Unsupported.of('slop without a phrase query')
		if (fuzziness !== null) {
			if (type == ETextQueryType.PHRASE) throw Unsupported.of('fuzziness on a phrase query')
			plan.put('fuzziness', num(fuzzyEdits(fuzziness)))
		}
		plan
	}

	/** Edit distance of a fuzziness setting: AUTO is one edit for LanceDB's single-term expansion; numbers are taken as given. */
	def private int fuzzyEdits(String fuzziness) {
		if (fuzziness.equalsIgnoreCase('AUTO')) return 1
		try {
			val edits = Integer.parseInt(fuzziness)
			if (edits < 0 || edits > 2) throw Unsupported.of('fuzziness ' + fuzziness + '; LanceDB expands terms by up to 2 edits')
			edits
		} catch (NumberFormatException e) {
			throw Unsupported.of('fuzziness ' + fuzziness)
		}
	}

	/** Runs each leg on its own and fuses in the runtime exactly as vecDSL defines rrf, weighted and relative_score. */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'explain', 'reranker', 'highlighted')
		later(it, 'cursor', 'withTotal', 'mmr', 'boost', 'aggregations')
		val table = schema.table(collection)
		val fusionKind = if (written(it, 'fusion')) it.fusion else EFusionType.RRF
		val weights = legWeights(it, fusionKind)
		val List<Expr> mapped = newArrayList
		val List<Stmt> prelude = newArrayList
		var index = 0
		for (leg : legs) {
			mapped += leg(leg, table, collection, weights.get(index), prelude, 'l' + index + '_')
			index = index + 1
		}
		if (params !== null) schema.note(params, 'timeoutMillis')
		queryExtensions(it)
		val args = kw('table', table.layout, 'legs', items(mapped), 'limit', num(topK), 'fusion', text(fusionKind.literal))
		if (fusionKind == EFusionType.RRF) args.put('k', num(if (written(it, 'fusionConstant')) fusionConstant else 60))
		else if (written(it, 'fusionConstant')) throw Unsupported.of('fusion-k with ' + fusionKind.literal + ' fusion; vecDSL defines fusion-k for rrf only')
		common(it, table, collection, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('hybrid_search', args))
		result
	}

	/** One leg: its own input or the shared one, its filter joined with the search's, its window and weight. */
	def private Entries leg(HybridLeg leg, LancedbTable table, VectorCollection collection, Double weight, List<Stmt> prelude, String prefix) {
		val search = leg.eContainer as HybridSearch
		val input = (feature(leg, 'input') as QueryInput) ?: search.input
		val conditions = filterOf(search, table, search.filter, prefix).where(feature(leg, 'filter') as FilterBlock)
		val plan = switch leg {
			VectorLeg: {
				val field = vectorField(collection, leg.vectorField)
				val result = vectorPlan(table, field, if (written(leg, 'metric')) leg.metric else null, leg.params, leg.exact, conditions)
				result.put('kind', text('vector'))
				result.put('vector', queryVector(input, collection, field, prelude))
				if (leg.bounds !== null) {
					if (written(leg.bounds, 'min')) result.put('score_min', num(leg.bounds.min))
					if (written(leg.bounds, 'max')) result.put('score_max', num(leg.bounds.max))
				}
				for (name : ModelUtil.extParams(leg, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				result
			}
			KeywordLeg: {
				val result = textPlan(table, collection, leg.searchFields.map[field], leg.searchFields.map[s | if (written(s, 'boost')) s.boost else null],
					if (written(leg, 'textQueryType')) leg.textQueryType else null, if (written(leg, 'matchOperator')) leg.matchOperator else null,
					if (written(leg, 'minShouldMatch')) leg.minShouldMatch else null, null, null)
				if (!conditions.empty) result.put('where', conditions.code)
				result.put('kind', text('keyword'))
				result.put('text', queryText(input))
				for (name : ModelUtil.extParams(leg, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				result
			}
			default: throw Unsupported.of(NOT_MAPPED + leg.eClass.name)
		}
		if (written(leg, 'legLimit')) plan.put('limit', num((feature(leg, 'legLimit') as Integer).doubleValue))
		if (weight !== null) plan.put('weight', num(weight))
		plan
	}

	/** Leg weights for weighted and relative-score fusion; rrf is unweighted by definition. */
	def private List<Double> legWeights(HybridSearch search, EFusionType fusionKind) {
		val weighted = search.legs.filter[written(it, 'weight')].size
		if (fusionKind == EFusionType.RRF) {
			if (weighted > 0 || written(search, 'alpha')) throw Unsupported.of('leg weights or alpha with rrf fusion, which is unweighted')
			return search.legs.map[null as Double]
		}
		if (written(search, 'alpha')) {
			if (search.legs.size != 2 || !(search.legs.head instanceof VectorLeg) || weighted > 0) {
				throw Unsupported.of('alpha on other than one vector leg followed by one other leg without weights')
			}
			return #[search.alpha, BigDecimal.ONE.subtract(BigDecimal.valueOf(search.alpha)).doubleValue]
		}
		search.legs.map[l | if (written(l, 'weight')) feature(l, 'weight') as Double else 1.0]
	}

	/** The average-vector strategy over example records or vectors, computed in the runtime: 2 mean(positive) - mean(negative). */
	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'explain', 'reranker', 'highlighted')
		later(it, 'cursor', 'withTotal', 'mmr', 'boost', 'aggregations')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val table = schema.table(collection)
		val field = vectorField(collection, vectorField)
		if (field.multiVector) throw Unsupported.of('recommend on a multivector field; the average-vector strategy needs one vector per example')
		val plan = vectorPlan(table, field, null, params, false, filterOf(it, table, filter, 'f'))
		queryExtensions(it)
		val args = kw('table', table.layout, 'plan', plan, 'positives', items(positives.map[Literals.of(it)]), 'limit', num(topK))
		if (!negatives.empty) args.put('negatives', items(negatives.map[Literals.of(it)]))
		if (lookupCollection !== null && lookupCollection !== collection) args.put('lookup', schema.table(lookupCollection).layout)
		common(it, table, collection, args)
		#[new Code.Return(runtime('recommend', args))]
	}

	def dispatch List<Stmt> body(GetById it) {
		queryExtensions(it)
		val table = schema.table(collection)
		val args = kw('table', table.layout, 'ids', items(ids.map[Literals.of(it)]))
		projection(table, collection, args, output, newArrayList, withVectors)
		val where = filterOf(it, table, null, 'f')
		if (!where.empty) args.put('where', where.code)
		readOptions(it, args)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		queryExtensions(it)
		if (written(it, 'exact')) notes += 'exact on Count, since count_rows is exact'
		val table = schema.table(collection)
		val args = kw('table', table.layout)
		val where = filterOf(it, table, filter, 'f')
		if (!where.empty) args.put('where', where.code)
		readOptions(it, args)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		later(it, 'cursor')
		queryExtensions(it)
		val table = schema.table(collection)
		val args = kw('table', table.layout)
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		if (written(it, 'sliceIndex')) {
			if (!written(it, 'sliceTotal') || sliceIndex >= sliceTotal) throw Unsupported.of('a scroll slice without a total above its index')
			args.put('slice', kw('index', num(sliceIndex), 'total', num(sliceTotal)))
		}
		common(it, table, collection, args)
		#[new Code.Return(runtime('scroll', args))]
	}

	/** Sort clauses on stored fields, applied in the runtime over the hits (scroll: over the matching rows, then the key). */
	def private void order(LancedbTable table, List<SortClause> clauses, Entries args, boolean scored) {
		if (clauses.empty) return;
		val List<Expr> keys = newArrayList
		for (clause : clauses) {
			if (clause.byRelevance) {
				if (!scored) throw Unsupported.of('sort by relevance in a scroll')
				keys += kw('key', text('score'), 'direction', text(if (clause.direction == ESortDirection.ASC) 'asc' else 'desc'))
			} else {
				val path = clause.field
				if (path.rawPath !== null) throw Unsupported.of('sorting by a raw path; Lance columns are declared')
				val names = newArrayList(schema.column(path.field))
				for (sub : path.subFields) names += ModelUtil.fieldName(sub)
				if (!table.has(names.head)) throw Unsupported.of('sorting by ' + names.head + ', which the table no longer has')
				val key = kw('key', text(names.join('.')))
				if (clause.direction == ESortDirection.DESC) key.put('direction', text('desc'))
				keys += key
			}
		}
		args.put('order', items(keys))
	}

	/**
	 * Group-by: the runtime groups the hits of every matching row by the value of the group key (a column or a sub-field path),
	 * keeps the groups whose best hit ranks highest and the best size hits of each (1 by default).
	 */
	def private void groupOf(LancedbTable table, EObject query, Entries args) {
		val grouping = feature(query, 'groupBy') as FieldPath
		if (grouping === null) return;
		if (!((feature(query, 'sortClauses') ?: newArrayList) as List<SortClause>).empty) throw Unsupported.of('sort with group-by; groups follow the relevance of their best hit')
		if (grouping.rawPath !== null) throw Unsupported.of('grouping by a raw path; Lance columns are declared')
		if (!(grouping.field instanceof PayloadField)) throw Unsupported.of('group-by on vector field ' + ModelUtil.fieldName(grouping.field))
		val names = newArrayList(schema.column(grouping.field))
		for (sub : grouping.subFields) names += ModelUtil.fieldName(sub)
		if (!table.has(names.head)) throw Unsupported.of('grouping by ' + names.head + ', which the table no longer has')
		val size = if (written(query, 'groupSize')) (feature(query, 'groupSize') as Integer).doubleValue else 1.0
		args.put('group', kw('key', text(names.join('.')), 'size', num(size)))
	}

	/** A random sample of the matching rows; 'first' takes the first rows in key order. */
	def dispatch List<Stmt> body(Sample it) {
		queryExtensions(it)
		val table = schema.table(collection)
		val args = kw('table', table.layout, 'size', num(sampleSize))
		projection(table, collection, args, output, newArrayList, false)
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		val where = filterOf(it, table, filter, 'f')
		if (!where.empty) args.put('where', where.code)
		readOptions(it, args)
		#[new Code.Return(runtime('sample', args))]
	}

	/** A random sample with the nearest neighbours of each sampled row among the sample, as Qdrant's distance matrix. */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		queryExtensions(it)
		val table = schema.table(collection)
		val field = vectorField(collection, vectorField)
		if (field.multiVector) throw Unsupported.of('a distance matrix of a multivector field')
		val args = kw('table', table.layout, 'plan', kw('column', text(schema.column(field)), 'metric', text(table.vectors.get(schema.column(field)))))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		val where = filterOf(it, table, filter, 'f')
		if (!where.empty) args.put('where', where.code)
		readOptions(it, args)
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
			val helper = owner + '_query_' + index
			current = helper
			val parameters = ModelUtil.parameterNames(query)
			helpers += new Program.Method(helper, parameters, body(query), newArrayList, newArrayList, true)
			val passed = entries
			for (parameter : parameters) passed.put(parameter, new Code.Param(parameter))
			calls += new Code.Call(Code.Target.SELF, helper, passed)
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

	/** Paging, bounds, score form, projection, sort, consistency and time travel of a ranked query or a scroll. */
	def private void common(EObject query, LancedbTable table, VectorCollection collection, Entries args) {
		if (written(query, 'offset')) args.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		val bounds = feature(query, 'bounds') as ScoreBounds
		if (bounds !== null) {
			if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
			if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
		}
		if (written(query, 'scoreForm') && feature(query, 'scoreForm') == EScoreForm.NATIVE && !(query instanceof KeywordSearch) && !(query instanceof HybridSearch)) {
			args.put('native', flag(true))
		}
		if (written(query, 'scoreForm') && feature(query, 'scoreForm') == EScoreForm.NATIVE && query instanceof HybridSearch) {
			notes += 'score native on HybridSearch, since the fused score is the fusion function'
		}
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		projection(table, collection, args, feature(query, 'output') as OutputSpec, excluded, Boolean.TRUE == feature(query, 'withVectors'))
		order(table, (feature(query, 'sortClauses') ?: newArrayList) as List<SortClause>, args, !(query instanceof Scroll))
		groupOf(table, query, args)
		if (query instanceof Scroll) {
			val where = filterOf(query, table, (query as Scroll).filter, 'f')
			if (!where.empty) args.put('where', where.code)
		}
		readOptions(query, args)
	}

	/** Consistency (every read checks for newer versions) and time travel (as-of a version number or a tag). */
	def private void readOptions(EObject query, Entries args) {
		if (written(query, 'consistency')) notes += 'consistency on ' + query.eClass.name + ', since every read checks for newer versions (read_consistency_interval 0)'
		val asOf = feature(query, 'asOf') as org.ftn.vecDSL.vecDSL.Literal
		if (asOf !== null) args.put('version', Literals.of(asOf))
	}

	/** Output columns (the key is always returned); vector columns come with 'with-vectors' or when the output names them. */
	def private void projection(LancedbTable table, VectorCollection collection, Entries args, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val listed = if (output === null || output.allFields) collection.fields as List<? extends Field> else output.fields
		for (field : listed) {
			val name = schema.column(field)
			if (name != table.key && !excluded.contains(field) && table.has(name)) {
				if (field instanceof VectorField) {
					if (output !== null && !output.allFields) vectors += name
				} else {
					fields += name
				}
			}
		}
		args.put('output', texts(fields))
		if (withVectors) args.put('vectors', flag(true))
		else if (!vectors.empty) args.put('vectors', texts(vectors))
	}

	/** Rejects clauses LanceDB lacks with their reason. */
	def private void reject(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) {
				val clause = "the '" + (CLAUSES.get(name) ?: name) + "' clause"
				val reason = REASONS.get(name)
				throw Unsupported.of(if (reason === null) clause else clause + '; ' + reason)
			}
		}
	}

	/** Raises the pending message for clauses LanceDB supports that the generator does not produce yet. */
	def private void later(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) throw Unsupported.of("the '" + (CLAUSES.get(name) ?: name) + "' clause" + PENDING + ' (' + LATER.get(name) + ')')
		}
	}

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	/** LanceDB defines no vendor extension items; parameters in a lancedb block are rejected by name. */
	def private void queryExtensions(EObject host) {
		for (name : ModelUtil.extParams(host, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == LancedbGenerator.VENDOR) {
				for (item : block.getItems) throw Unsupported.of(item.eClass.name + ' on LanceDB')
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

	def private Entries record(RecordLiteral record) {
		val result = entries
		for (entry : record.entries) result.put(schema.column(entry.field), Literals.of(entry.value))
		result
	}
}
