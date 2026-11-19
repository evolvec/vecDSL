package org.ftn.vecDSL.generator.duckdb

import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Aggregate
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Boost
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.BoostFn
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Group
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Hybrid
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.KeywordField
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Keywords
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Leg
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Mmr
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Order
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Projection
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Ref
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Search
import org.ftn.vecDSL.generator.duckdb.DuckdbQueries.Shape
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Column
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Cond
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Query
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Table
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
import org.ftn.vecDSL.vecDSL.EBoostKind
import org.ftn.vecDSL.vecDSL.ECloneMode
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
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
import org.ftn.vecDSL.vecDSL.OutputSpec
import org.ftn.vecDSL.vecDSL.ParamRef
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
import org.ftn.vecDSL.vecDSL.SortClause
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
import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.duckdb.DuckdbSchema.call
import static org.ftn.vecDSL.generator.duckdb.DuckdbSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.duckdb.DuckdbSchema.call
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to SQL for DuckDB with the vss and fts extensions in the host-language-neutral code model. The
 * SQL text is built here (through DuckdbSql and DuckdbQueries) and passed to the runtime with its parameters, so the three
 * host languages send identical statements: query and value return JSON rows, change runs a write and the fts rebuild of its
 * table in one transaction, execute runs DDL in one transaction, transaction runs a vecDSL transaction.
 */
class DuckdbStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; DuckdbCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for DuckDB: '
	/** Suffix of constructs DuckDB supports that the generator does not produce yet. */
	public static val PENDING = 'has no DuckDB mapping yet'
	public static val NO_MODELS = '; DuckDB has no embedding models in its core extensions, so generated programs supply their own vectors'
	public static val FLOAT_ONLY = 'the vss extension indexes FLOAT[n] arrays only ("Only vectors consisting of FLOATs (32-bit, single precision) are supported"), and DuckDB has no half-precision, binary or sparse vector type with distance functions'
	public static val NO_SPATIAL = 'geo fields, predicates and indexes need the spatial extension (ST_ functions, R-tree indexes), which DuckDB does not autoload and which the generated programs do not load (they load vss and fts only)'
	public static val NO_AUTO_ID = 'auto-id; the generated runtimes report how many rows a write changed, not the keys DuckDB would assign (a sequence or uuid() default), so records written without a key could not be addressed by id afterwards'
	public static val NO_CROSS_REF = 'cross-ref; a cross-ref names collections but no field that holds the reference, so there is no column to declare a FOREIGN KEY on'
	public static val NO_PATH_INDEX = 'DuckDB 2.0.0 refuses JSON functions in index expressions (CREATE INDEX on json_extract_string fails with Binder Error: Cannot use "json_extract_string" in this context, measured)'
	public static val NO_FUZZY = "match_bm25 matches stemmed terms exactly, and DuckDB's string similarity functions (levenshtein, jaro_winkler_similarity) compare whole strings, not the terms of a text"
	public static val NO_DYNAMIC = 'dynamic; a DuckDB table has a fixed set of typed columns, and the generator keeps no undeclared fields (a JSON column for them would hide them from filters, indexes and full-text search)'
	public static val NO_TTL = 'ttl; DuckDB rows do not expire and an embedded database runs no background jobs'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; DuckDB has no per-table drop guard'
	public static val NO_STORAGE = 'storage settings; a DuckDB database is one file, whose path is the connection'
	public static val NO_TAGS = 'collection tags; a DuckDB table carries one comment (the description) and no tags'
	public static val NO_PARTITIONS = 'partitions; DuckDB tables have no partitions (Hive partitioning applies to COPY ... TO files, not to tables)'
	public static val EMBEDDED = 'an embedded database: DuckDB runs inside the program over one file, with no server, users or replicas'
	static val NO_GENERATION = 'DuckDB has no generative models in its core extensions'
	static val NO_RERANKERS = 'DuckDB has no reranking models in its core extensions'
	static val NO_METRICS = 'metrics configuration; ' + EMBEDDED
	static val NO_SCHEDULES = 'backup schedules; ' + EMBEDDED + ', so nothing runs between programs'
	static val NO_CLUSTER_LIMITS = 'cluster limits; DuckDB has no setting that limits the number of tables'
	static val NO_FILES = 'file resources; the fts extension takes stop words from a table and has no other resource files'
	static val NO_RESIDENCY = 'load and release; DuckDB reads table data on demand through its buffer manager, and an HNSW index is loaded into memory when its table is first accessed after a restart (vss documentation, "Persistence"), so there is nothing to load or release'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'boost' -> 'boost', 'queryModel' -> 'model', 'minShouldMatch' -> 'min-should-match', 'fuzziness' -> 'fuzziness',
		'slop' -> 'slop', 'sliceIndex' -> 'slice'
	}

	/** Why DuckDB cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'DuckDB reads the current committed state and keeps no queryable history of a table',
		'explain' -> 'DuckDB explains query plans (EXPLAIN), not scores',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated DuckDB programs embed no query text' + NO_MODELS,
		'minShouldMatch' -> 'match_bm25 requires all terms (conjunctive) or any term, with no minimum number of matching terms',
		'fuzziness' -> 'match_bm25 matches stemmed terms exactly',
		'slop' -> DuckdbSql.NO_POSITIONS,
		'highlighted' -> 'the fts extension returns scores only and has no highlighting function'
	}

	/** Clauses DuckDB supports that the generator does not produce yet, with the SQL that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'list_filter over the elements'
	}

	val List<String> notes = newArrayList
	/** Snapshots of one tenant taken earlier in the script, by name: their restore replaces that tenant's rows only. */
	val Map<String, String> tenantSnapshots = newHashMap
	val DuckdbSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new DuckdbSchema(notes, script)
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
			case 'bm25': 'the fts extension indexes text columns directly and has no BM25 function producing sparse vectors'
			case 'custom': 'a custom function names its input and output fields but no computation, so there is no expression for a generated column'
			default: 'DuckDB has no model functions in its core extensions'
		}
	}

	def static String collectionLimit(String type) {
		'collection limits (' + type + '); DuckDB has no per-table limit settings'
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
				if (concept.replication !== null || concept.sharding !== null) throw Unsupported.of('replication and sharding; ' + EMBEDDED)
				for (name : ModelUtil.extParams(concept, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Database: {
				result += DuckdbSchema.execute(#['CREATE SCHEMA ' + (if (ifNotExists) 'IF NOT EXISTS ' else '') + DuckdbSql.id(concept.name)])
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Alias: {
				result += DuckdbSchema.execute(#[DuckdbSchema.createView(concept.name, schema.table(concept.collection), ifNotExists)])
				schema.view(concept.name, concept.collection)
			}
			VectorIndex: result += createIndex(concept, ifNotExists)
			PayloadIndex: result += createIndex(concept, ifNotExists)
			VectorField: result += addField(member(concept, 'a field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: throw Unsupported.of(NO_PARTITIONS)
			Tenant: result += require(member(concept, 'a tenant'))
			Namespace: result += require(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			FileResource: throw Unsupported.of(NO_FILES)
			Analyzer: result += checkAnalyzer(concept)
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; the fts options belong to an analyzer')
				result += checkAnalyzer(analyzer)
			}
			FieldLimit: throw Unsupported.of('a field limit on an existing field; DuckDB adds CHECK constraints only in CREATE TABLE (DuckDB 2.0.0: "No support for adding CHECK constraints with ALTER TABLE")')
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: throw Unsupported.of('quantization; ' + FLOAT_ONLY)
			ReplicationConfig: throw Unsupported.of('replication; ' + EMBEDDED)
			ShardConfig: throw Unsupported.of('sharding; ' + EMBEDDED)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of('roles and grants; ' + EMBEDDED + ', and access to it is the file system\'s')
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program's database file: checked here, opened by connect(). */
	def private void connection(Connection concept) {
		if (written(concept, 'deployment') && concept.deployment != EDeploymentMode.EMBEDDED) {
			throw Unsupported.of(concept.deployment.literal + ' deployments; the generated programs open DuckDB in process (MotherDuck, the hosted service, is out of scope)')
		}
		if (concept.apiKey !== null || concept.token !== null) throw Unsupported.of('api keys and tokens; ' + EMBEDDED)
		if (concept.password !== null || concept.username !== null) throw Unsupported.of('users and passwords; ' + EMBEDDED)
		if (concept.host !== null || concept.port > 0 || concept.grpcPort > 0) throw Unsupported.of('host and ports; ' + EMBEDDED)
		if (concept.uri !== null && concept.uri.matches('[A-Za-z][A-Za-z0-9+.-]+:.*')) throw Unsupported.of('the uri ' + concept.uri + '; the generated programs open a local database file')
		if (concept.databaseName !== null) DuckdbSql.id(concept.databaseName)
		for (name : ModelUtil.extParams(concept, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(concept, 'timeoutMillis', 'cloud', 'region', 'project', 'tls')
	}

	/** Authentication: none is the only mode of an embedded database. */
	def private void authentication(AuthConfig auth) {
		if (auth.type != EAuthType.NONE || !auth.roles.empty || auth.token !== null || auth.certificatePath !== null || auth.cmekKeyId !== null
			|| auth.fieldEncryption || auth.auditLog) {
			throw Unsupported.of('authentication; ' + EMBEDDED + ', and access to it is the file system\'s')
		}
	}

	/** An analyzer is the option set of its table's fts index; creating it checks the stemmer in the extension. */
	def private List<Stmt> checkAnalyzer(Analyzer analyzer) {
		val analysis = DuckdbSchema.analysis(analyzer)
		val q = new Query
		#[new Code.Run(call('value', 'SELECT to_json(stem(' + q.bind(text('running'), 'VARCHAR') + ', ' + q.bind(text(analysis.stemmer), 'VARCHAR') + '))', q, null))]
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
		throw Unsupported.of(what + ' outside a collection; DuckDB adds it to a table')
	}

	/** An index created on an existing collection; full_text indexes are the table's fts index, rebuilt with every write. */
	def private List<Stmt> createIndex(Index index, boolean ifNotExists) {
		switch index {
			VectorIndex: {
				val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				val collection = member(field, 'a vector index')
				val table = schema.table(collection)
				val sql = schema.vectorIndex(collection, table, field, index, ifNotExists)
				if (sql === null) return require(collection)
				#[DuckdbSchema.execute(#[sql])]
			}
			PayloadIndex: {
				val field = index.target.field
				val collection = ModelUtil.targetCollection(index) ?: (if (field === null) null else ModelUtil.owningCollection(field))
				if (collection === null) throw Unsupported.of('a payload index outside a collection')
				val table = schema.table(collection)
				val sql = schema.payloadIndex(collection, table, index, ifNotExists)
				#[DuckdbSchema.execute(#[sql ?: DuckdbSql.createFtsIndex(table)])]
			}
		}
	}

	/** Existence check of a collection: the statement fails when the table is missing. */
	def private List<Stmt> require(VectorCollection collection) {
		val table = schema.table(collection)
		#[new Code.Run(call('query', 'SELECT NULL::JSON FROM ' + DuckdbSql.table(table) + ' WHERE false', null, null))]
	}

	/** ALTER TABLE ADD COLUMN; a vector field with an index gets the index, a full-text field the rebuilt fts index. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val ddl = addColumn(collection, table, field)
		val after = table.with(schema.column(collection, field))
		if (field instanceof PayloadField && after.column(ModelUtil.fieldName(field)).fullText) ddl += DuckdbSql.createFtsIndex(after)
		schema.evolve(collection, after)
		#[DuckdbSchema.execute(ddl)]
	}

	def private List<String> addColumn(VectorCollection collection, Table table, Field field) {
		val column = schema.column(collection, field)
		if (!column.checks.empty) throw Unsupported.of('a field limit on an added field; DuckDB adds CHECK constraints only in CREATE TABLE')
		val List<String> result = newArrayList
		val definition = DuckdbSql.columnDefinition(if (field instanceof VectorField) nullableVector(column) else column, false)
		result += 'ALTER TABLE ' + DuckdbSql.table(table) + ' ADD COLUMN ' + definition
		if (field instanceof VectorField) {
			val index = schema.indexOf(collection, field as VectorField)
			if (index !== null) result += schema.vectorIndex(collection, table.with(column), field as VectorField, index, false)
		}
		result.filterNull.toList
	}

	/** Rows that exist when a vector column is added have no vector, so the added column is nullable. */
	def private Column nullableVector(Column column) {
		new Column(column.name, column.type, null, false, column.vec, null, true, null, #[])
	}

	/**
	 * ALTER TABLE in one transaction. DuckDB refuses to drop or rename a column, or to rename a table, while secondary indexes
	 * depend on it ("Cannot alter entry ... because there are entries that depend on it" on DuckDB 2.0.0), and indexes
	 * dropped in the same transaction still count ("an index depends on a column after it"), so such an alter first drops the
	 * table's indexes in a transaction of their own, then alters the table and creates the indexes again on it; a renamed
	 * table gets its views again.
	 */
	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias is a view, which can be renamed')
			val alias = concept as Alias
			val table = schema.table(alias.collection)
			DuckdbSql.id(newName)
			schema.view(alias.name, null)
			schema.view(newName, alias.collection)
			return #[DuckdbSchema.execute(#['ALTER VIEW ' + DuckdbSql.qualified(table, alias.name) + ' RENAME TO ' + DuckdbSql.id(newName)])]
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; DuckDB alters tables and views')
		val collection = concept as VectorCollection
		val before = schema.table(collection)
		var table = before
		val List<String> ddl = newArrayList
		val restructures = newName !== null || operations.exists[op | op instanceof DropFieldOp || op instanceof RenameFieldOp]
		for (operation : operations) {
			ddl += alter(collection, table, operation)
			table = evolved(collection, table, operation)
		}
		if (newName !== null) {
			ddl += 'ALTER TABLE ' + DuckdbSql.table(table) + ' RENAME TO ' + DuckdbSql.id(newName)
			table = table.named(newName)
		}
		if (restructures) {
			ddl += schema.indexes(collection, table, false)
			if (newName !== null) for (alias : schema.viewsOf(collection)) ddl += DuckdbSchema.createView(alias, table, true)
		} else if (operations.exists[op | op instanceof AddFieldOp && (op as AddFieldOp).newField instanceof PayloadField]) {
			if (!table.fullTextColumns.empty) ddl += DuckdbSql.createFtsIndex(table)
		}
		val List<Stmt> result = newArrayList
		if (restructures) result += DuckdbSchema.execute(schema.dropIndexes(collection, before))
		if (ddl.empty) result += require(collection) else result += DuckdbSchema.execute(ddl)
		schema.evolve(collection, table)
		result
	}

	/** The table after an alter operation, for the statements that follow it. */
	def private Table evolved(VectorCollection collection, Table table, AlterOperation operation) {
		switch operation {
			AddFieldOp: table.with(schema.column(collection, operation.newField))
			DropFieldOp: table.without(ModelUtil.fieldName(operation.field))
			RenameFieldOp: table.renamed(ModelUtil.fieldName(operation.field), operation.newName)
			default: table
		}
	}

	def List<String> alter(VectorCollection collection, Table table, AlterOperation operation) {
		switch operation {
			AddFieldOp: addColumn(collection, table, operation.newField)
			DropFieldOp: #[dropColumn(collection, table, operation.field)]
			RenameFieldOp: {
				if (operation.field === collection.primaryKey) throw Unsupported.of('renaming the primary key; the generated statements address the key by its declared name')
				DuckdbSql.id(operation.newName)
				#['ALTER TABLE ' + DuckdbSql.table(table) + ' RENAME COLUMN ' + DuckdbSql.id(table.column(ModelUtil.fieldName(operation.field)).name) + ' TO ' + DuckdbSql.id(operation.newName)]
			}
			SetPropertyOp: setProperty(table, operation)
			AddAliasOp: {
				schema.view(operation.newAlias.name, collection)
				#[DuckdbSchema.createView(operation.newAlias.name, table, false)]
			}
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def private String dropColumn(VectorCollection collection, Table table, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
		'ALTER TABLE ' + DuckdbSql.table(table) + ' DROP COLUMN ' + DuckdbSql.id(table.column(ModelUtil.fieldName(field)).name)
	}

	/** Collection properties: the description is the table comment; consistency is met by every committed write. */
	def private List<String> setProperty(Table table, SetPropertyOp operation) {
		switch operation.property {
			case DESCRIPTION: {
				if (!(operation.value instanceof StringLit)) throw Unsupported.of('a description that is not a string literal; COMMENT takes a constant')
				#['COMMENT ON TABLE ' + DuckdbSql.table(table) + ' IS ' + DuckdbSql.literal((operation.value as StringLit).value)]
			}
			case CONSISTENCY: newArrayList
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DYNAMIC: throw Unsupported.of(NO_DYNAMIC)
			case TTL: throw Unsupported.of(NO_TTL)
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		val exists = if (ifExists) 'IF EXISTS ' else ''
		switch concept {
			VectorCollection: {
				val table = schema.table(concept)
				schema.evolve(concept, null)
				val List<String> ddl = newArrayList
				ddl += 'DROP SCHEMA IF EXISTS ' + DuckdbSql.ftsSchema(table) + ' CASCADE'
				ddl += 'DROP TABLE ' + exists + DuckdbSql.table(table)
				#[DuckdbSchema.execute(ddl)]
			}
			Database: {
				val List<String> ddl = newArrayList
				for (collection : concept.collections) ddl += 'DROP SCHEMA IF EXISTS ' + DuckdbSql.ftsSchema(schema.table(collection)) + ' CASCADE'
				ddl += 'DROP SCHEMA ' + exists + DuckdbSql.id(concept.name) + ' CASCADE'
				#[DuckdbSchema.execute(ddl)]
			}
			Tenant: dropSlice(member(concept, 'a tenant'), schema.table(member(concept, 'a tenant')).tenant, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), schema.table(member(concept, 'a namespace')).namespace, concept.key)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a field'), concept)
			Alias: {
				schema.view(concept.name, null)
				#[DuckdbSchema.execute(#['DROP VIEW ' + exists + DuckdbSql.qualified(schema.table(concept.collection), concept.name)])]
			}
			Partition: throw Unsupported.of(NO_PARTITIONS)
			VectorIndex: dropIndex(concept, exists)
			PayloadIndex: dropIndex(concept, exists)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('quantization; ' + FLOAT_ONLY)
			Connection: throw Unsupported.of('dropping a connection; a connection is the program\'s database file, not a DuckDB object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; the program drops schemas and tables, not the database file')
			Cluster: throw Unsupported.of('dropping a cluster; ' + EMBEDDED)
			Analyzer: throw Unsupported.of('dropping an analyzer; an analyzer is the option set of an fts index, which every write rebuilds')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; an analyzer is the option set of an fts index')
			FieldLimit: throw Unsupported.of('dropping a field limit; DuckDB drops no constraints with ALTER TABLE (DuckDB 2.0.0: "No support for that ALTER TABLE option yet")')
			FileResource: throw Unsupported.of(NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('replication; ' + EMBEDDED)
			ShardConfig: throw Unsupported.of('sharding; ' + EMBEDDED)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('authentication; ' + EMBEDDED)
			RoleBinding: throw Unsupported.of('roles and grants; ' + EMBEDDED)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** Dropping a tenant or namespace deletes its rows. */
	def private List<Stmt> dropSlice(VectorCollection collection, String column, String value) {
		val table = schema.table(collection)
		val q = new Query
		val sql = 'DELETE FROM ' + DuckdbSql.table(table) + ' AS ' + DuckdbSql.ALIAS + ' WHERE ' + DuckdbSql.col(column) + ' = ' + q.bind(text(value), 'VARCHAR')
		#[new Code.Return(DuckdbSchema.change(sql, q, 'delete_count', table, false))]
	}

	/** ALTER TABLE DROP COLUMN, with the table's indexes dropped first (own transaction) and created again on the remaining columns. */
	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val after = table.without(ModelUtil.fieldName(field))
		val List<String> ddl = newArrayList
		ddl += dropColumn(collection, table, field)
		ddl += schema.indexes(collection, after, false)
		schema.evolve(collection, after)
		#[DuckdbSchema.execute(schema.dropIndexes(collection, table)), DuckdbSchema.execute(ddl)]
	}

	def private List<Stmt> dropIndex(Index index, String exists) {
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		val table = schema.table(collection)
		if (index instanceof PayloadIndex && (index as PayloadIndex).kind == EPayloadIndexKind.FULL_TEXT) {
			throw Unsupported.of('dropping a full_text index; the fts index of a table covers all its full-text fields and every write rebuilds it')
		}
		#[DuckdbSchema.execute(#['DROP INDEX ' + exists + DuckdbSql.qualified(table, DuckdbSchema.indexName(collection, index.name))])]
	}

	def private VectorCollection indexCollection(Index index) {
		switch index {
			VectorIndex: {
				val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				if (field === null) null else ModelUtil.owningCollection(field) ?: ModelUtil.targetCollection(index)
			}
			PayloadIndex: ModelUtil.targetCollection(index) ?: (if (index.target.field === null) null else ModelUtil.owningCollection(index.target.field))
		}
	}

	/** An alias is a view: the swap replaces its query in one transaction. */
	def dispatch List<Stmt> body(SwapAlias it) {
		val table = schema.table(newCollection)
		val old = schema.table(oldCollection)
		if (table.schema != old.schema) throw Unsupported.of('swapping an alias across databases; a view lives in the schema of its table')
		schema.view(alias.name, newCollection)
		#[DuckdbSchema.execute(#['DROP VIEW IF EXISTS ' + DuckdbSql.qualified(table, alias.name), DuckdbSchema.createView(alias.name, table, false)])]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of(NO_TTL)
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		#[new Code.Return(write(collection, source, policy, if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count', it))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of(NO_TTL)
		schema.note(it, 'recordCountHint', 'batchSize')
		writeOptions(it)
		#[new Code.Return(write(collection, source, 'replace', 'upsert_count', it))]
	}

	/** One INSERT of the rows (as a JSON array parameter) with the slice values of the statement, then the fts rebuild. */
	def private Code.Call write(VectorCollection collection, RecordSource source, String policy, String counter, EObject statement) {
		val table = schema.table(collection)
		val q = new Query
		val sql = DuckdbSql.insertRows(q, table, rows(source, table), slices(statement, collection, table), policy)
		DuckdbSchema.change(sql, q, counter, table, policy == 'ignore')
	}

	/** Column values for tenant and namespace of a write. */
	def private Map<String, Expr> slices(EObject statement, VectorCollection collection, Table table) {
		val Map<String, Expr> result = newLinkedHashMap
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.put(table.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.put(table.namespace, text(namespace))
		if (feature(statement, 'partition') !== null) throw Unsupported.of(NO_PARTITIONS)
		result
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; DuckDB rows carry no version number (MVCC versions are internal), and the generator adds no version column')
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val table = schema.table(collection)
		val q = new Query
		if (source !== null) {
			if (values !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty) {
				throw Unsupported.of("update from records with 'set', remove-fields, array-append or array-remove; each record carries its own values")
			}
			val records = rows(source, table)
			val List<String> restrictions = newArrayList
			if (!ids.empty) restrictions += DuckdbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
			restrictions += DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
			val fromRecords = DuckdbSql.updateRows(q, table, records, written(it, 'mode') && mode == EUpdateMode.REPLACE, DuckdbSql.and(restrictions))
			return #[new Code.Return(DuckdbSchema.change(fromRecords, q, 'update_count', table, false)) as Stmt]
		}
		val List<String> sets = newArrayList
		val List<String> given = newArrayList
		if (values !== null) {
			for (entry : values.entries) {
				if (entry.field === collection.primaryKey) throw Unsupported.of('updating the primary key; the key identifies the row')
				val column = table.column(ModelUtil.fieldName(entry.field))
				val columnName = column.name
				given += columnName
				val assigned = DuckdbSql.value(q, column, Literals.of(entry.value))
				if (appendFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += DuckdbSql.id(columnName) + ' = list_concat(coalesce(' + DuckdbSql.id(columnName) + ', []), ' + assigned + ')'
				} else if (arrayRemoveFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += DuckdbSql.id(columnName) + ' = list_filter(' + DuckdbSql.id(columnName) + ', lambda element: NOT coalesce(list_contains(' + assigned + ', element), false))'
				} else {
					sets += DuckdbSql.id(columnName) + ' = ' + assigned
				}
			}
		}
		for (field : appendFields + arrayRemoveFields) {
			if (!given.contains(table.column(ModelUtil.fieldName(field)).name)) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + " without items in 'set'")
		}
		for (field : removedFields) {
			if (field === collection.primaryKey) throw Unsupported.of('removing the primary key')
			val column = table.column(ModelUtil.fieldName(field))
			if (column.vec !== null && !column.nullable) throw Unsupported.of('remove-fields on vector field ' + column.name + ', which is not nullable')
			sets += DuckdbSql.id(column.name) + ' = NULL'
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			for (column : table.columns) {
				if (column.name != table.key && column.vec === null && !given.contains(column.name) && !removedFields.exists[f | table.column(ModelUtil.fieldName(f)).name == column.name]) {
					sets += DuckdbSql.id(column.name) + ' = DEFAULT'
				}
			}
		}
		if (sets.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += DuckdbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val update = 'UPDATE ' + DuckdbSql.table(table) + ' AS ' + DuckdbSql.ALIAS + ' SET ' + sets.join(', ') + DuckdbSql.whereClause(DuckdbSql.and(conditions))
		#[new Code.Return(DuckdbSchema.change(update, q, 'update_count', table, false))]
	}

	def private void arrayField(Field field, Column column) {
		if (!column.array) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ', which is not an array field')
	}

	/** DELETE by ids, filter or all; with a limit, the rows are the first ones in key order (a rowid subquery). */
	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val table = schema.table(collection)
		val q = new Query
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += DuckdbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val condition = DuckdbSql.and(conditions)
		if (condition === null && !deleteAll) throw Unsupported.of("a delete without ids, filter or 'all'")
		val target = if (written(it, 'deleteLimit')) {
			DuckdbSql.ALIAS + '.rowid IN (SELECT ' + DuckdbSql.ALIAS + '.rowid FROM ' + DuckdbSql.table(table) + ' ' + DuckdbSql.ALIAS
				+ DuckdbSql.whereClause(condition) + ' ORDER BY ' + DuckdbSql.col(table.key) + ' LIMIT ' + deleteLimit + ')'
		} else condition
		val delete = 'DELETE FROM ' + DuckdbSql.table(table) + ' AS ' + DuckdbSql.ALIAS + DuckdbSql.whereClause(target)
		#[new Code.Return(DuckdbSchema.change(delete, q, 'delete_count', table, false))]
	}

	/**
	 * The runtime reads a local JSON or JSON Lines file and writes it in batches with one INSERT statement, all in one
	 * transaction, as the pgvector generator does; csv and parquet files, and imports inside a transaction, are read by DuckDB.
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; an embedded database runs no ingestion pipeline between programs')
		DuckdbSql.localPath(location)
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.PARQUET || format == EImportFormat.CSV) return #[new Code.Return(fileImport(it))]
			throw Unsupported.of('import format ' + format.literal + '; DuckDB reads JSON, CSV and Parquet with its core readers, not ' + format.literal)
		}
		schema.note(it, 'parallelism', 'async')
		val table = schema.table(collection)
		val q = new Query
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val sql = DuckdbSql.insertRows(q, table, new Code.Nil, slices(it, collection, table), policy)
		val extra = q.params.items.subList(1, q.params.items.size)
		val args = DuckdbSchema.kw('sql', text(sql), 'location', text(location), 'counter', text(if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count'))
		if (!extra.empty) args.put('params', items(extra))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		val refresh = DuckdbSql.createFtsIndex(table)
		if (refresh !== null) args.put('refresh', items(#[text(refresh) as Expr]))
		if (policy == 'ignore') args.put('skipped', flag(true))
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[new Code.Return(runtime('import_file', args))]
	}

	/**
	 * An import read by DuckDB's core reader of the file (read_parquet, read_csv or read_json) in one INSERT, then the fts rebuild;
	 * the path is a bound parameter relative to the working directory.
	 */
	def private Code.Call fileImport(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; an embedded database runs no ingestion pipeline between programs')
		schema.note(it, 'parallelism', 'async', 'batchSize')
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(collection)
		val q = new Query
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val reader = DuckdbSql.reader(location, if (written(it, 'format')) format.literal else null)
		val sql = DuckdbSql.insertFromFile(q, table, reader, DuckdbSql.localPath(location), slices(it, collection, table), policy)
		DuckdbSchema.change(sql, q, if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count', table, false)
	}

	/**
	 * The statements of a vecDSL transaction in one DuckDB transaction (DuckDB has one isolation level, snapshot isolation); an
	 * import is one INSERT through DuckDB's file reader, so that it commits or rolls back with the other steps.
	 */
	def dispatch List<Stmt> body(Transaction it) {
		val List<Expr> steps = newArrayList
		for (statement : it.methods) {
			val inner = if (statement instanceof BulkImport) #[new Code.Return(fileImport(statement as BulkImport)) as Stmt] else body(statement)
			if (inner.size != 1 || !(inner.head instanceof Code.Return)) throw Unsupported.of('this statement inside a transaction')
			val returned = (inner.head as Code.Return).value
			if (!(returned instanceof Code.Call) || !(returned as Code.Call).args.has('sql')) throw Unsupported.of('this statement inside a transaction')
			val invocation = returned as Code.Call
			if (invocation.args.has('settings')) notes += 'search settings inside a transaction, which apply to single statements only'
			val step = DuckdbSchema.kw('sql', invocation.args.get('sql'))
			for (key : #['params', 'counter', 'refresh', 'skipped']) if (invocation.args.has(key)) step.put(key, invocation.args.get(key))
			steps += step
		}
		if (written(it, 'isolationLevel')) notes += 'isolationLevel ' + isolationLevel.literal + ' on Transaction, which DuckDB runs with snapshot isolation'
		#[new Code.Return(runtime('transaction', DuckdbSchema.kw('steps', items(steps))))]
	}

	/** A committed write is durable and visible to the next statement, so 'wait' needs no call; extensions are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (name : ModelUtil.extParams(statement, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** A statement's filter with its tenant and namespace conditions. */
	def private DuckdbFilter filterOf(EObject statement, VectorCollection collection, Table table, FilterBlock block) {
		val result = new DuckdbFilter().where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.slice(table.tenant, #[text(tenant.name)])
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result.slice(table.namespace, #[text(namespace as String)])
		if (feature(statement, 'partition') !== null) throw Unsupported.of(NO_PARTITIONS)
		val partitions = feature(statement, 'partitions') as List<Partition>
		if (partitions !== null && !partitions.empty) throw Unsupported.of(NO_PARTITIONS)
		result
	}

	// ---------------------------------------------------------------- operational

	def dispatch List<Stmt> body(Load it) {
		throw Unsupported.of(NO_RESIDENCY)
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of(NO_RESIDENCY)
	}

	/** CHECKPOINT writes the write-ahead log into the database file (and serializes the HNSW indexes). */
	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val List<Stmt> result = newArrayList
		result += require(collection)
		result += DuckdbSchema.execute(#['CHECKPOINT'])
		result
	}

	/** PRAGMA hnsw_compact_index removes deleted entries from each HNSW index of the table (vss documentation); then CHECKPOINT. */
	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'targetSegments', 'async')
		val table = schema.table(collection)
		val List<String> ddl = newArrayList
		for (field : ModelUtil.vectorFields(collection).filter[DuckdbSchema.present(table, it)]) {
			val index = schema.indexOf(collection, field)
			if (index !== null && index.family.literal != 'flat') {
				ddl += 'PRAGMA hnsw_compact_index(' + DuckdbSql.literal(table.schema + '.' + DuckdbSchema.indexName(collection, index.name)) + ')'
			}
		}
		ddl += 'CHECKPOINT'
		#[DuckdbSchema.execute(ddl)]
	}

	def dispatch List<Stmt> body(Describe it) {
		val table = schema.table(collection)
		val q = new Query
		#[new Code.Return(call('value', DuckdbQueries.describe(q, table, includeStats, includeIndexProgress), q, null))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + NO_MODELS)
	}

	/** A snapshot is a copy of the table in the database file: main.vecdsl_snapshot__<name>. */
	def dispatch List<Stmt> body(Snapshot it) {
		if (written(it, 'scope') && scope.literal != 'collection') throw Unsupported.of('snapshot scope ' + scope.literal + '; the generator snapshots one collection as a table copy')
		if (incremental) throw Unsupported.of('incremental snapshots; a table copy is always complete')
		if (location !== null) throw Unsupported.of('a snapshot location; snapshots are tables in the database file (EXPORT DATABASE writes a whole database, not one table)')
		schema.note(it, 'description')
		val table = schema.table(collection)
		val copy = 'CREATE TABLE ' + DuckdbSql.id(DuckdbSql.MAIN) + '.' + DuckdbSql.id(DuckdbQueries.snapshotTable(snapshotOf(it))) + ' AS SELECT * FROM ' + DuckdbSql.table(table)
		if (tenant === null) return #[DuckdbSchema.execute(#[copy])]
		if (table.tenant === null) throw Unsupported.of('a tenant snapshot of ' + collection.name + ', which has no tenants')
		tenantSnapshots.put(snapshotOf(it), tenant.name)
		#[DuckdbSchema.execute(#[copy + ' t WHERE t.' + DuckdbSql.id(table.tenant) + ' = ' + DuckdbSql.literal(tenant.name)])]
	}

	def private String snapshotOf(Snapshot snapshot) {
		snapshot.snapshotName ?: snapshot.name
	}

	/** Restore replaces the rows of the collection with the snapshot's, by column name, in one transaction. */
	def dispatch List<Stmt> body(Restore it) {
		if (written(it, 'scope') && scope.literal != 'collection') throw Unsupported.of('restore scope ' + scope.literal + '; the generator restores one collection from a table copy')
		if (location !== null) throw Unsupported.of('a snapshot location; snapshots are tables in the database file')
		schema.note(it, 'async')
		val table = schema.table(collection)
		val snapshot = DuckdbSql.id(DuckdbSql.MAIN) + '.' + DuckdbSql.id(DuckdbQueries.snapshotTable(snapshotName))
		val tenantName = if (table.tenant === null) null else tenantSnapshots.get(snapshotName)
		val delete = DuckdbSchema.kw('sql', text('DELETE FROM ' + DuckdbSql.table(table)), 'counter', text('delete_count'))
		if (tenantName !== null) {
			val q = new Query
			delete.put('sql', text('DELETE FROM ' + DuckdbSql.table(table) + ' AS t WHERE t.' + DuckdbSql.id(table.tenant) + ' = ' + q.bind(text(tenantName), 'VARCHAR')))
			delete.put('params', q.params)
		}
		val steps = #[delete as Expr, restoreStep('INSERT INTO ' + DuckdbSql.table(table) + ' BY NAME SELECT * FROM ' + snapshot, table)]
		#[new Code.Return(runtime('transaction', DuckdbSchema.kw('steps', items(steps))))]
	}

	def private Expr restoreStep(String sql, Table table) {
		val step = DuckdbSchema.kw('sql', text(sql), 'counter', text('restored'))
		val refresh = DuckdbSql.createFtsIndex(table)
		if (refresh !== null) step.put('refresh', items(#[text(refresh) as Expr]))
		step
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		if (location !== null) throw Unsupported.of('a snapshot location; snapshots are tables in the database file')
		#[DuckdbSchema.execute(#['DROP TABLE IF EXISTS ' + DuckdbSql.id(DuckdbSql.MAIN) + '.' + DuckdbSql.id(DuckdbQueries.snapshotTable(snapshotName))])]
	}

	/** A new table with the source's columns, key and indexes, filled from it, in one transaction. */
	def dispatch List<Stmt> body(Clone it) {
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; DuckDB has no copy-on-write tables')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (sourceDatabase !== null) return cloneDatabase(targetName)
		val table = schema.table(sourceCollection)
		val copy = table.named(targetName)
		val List<Expr> steps = newArrayList
		steps += DuckdbSchema.kw('sql', text(DuckdbSql.createTable(copy, false)))
		val insert = DuckdbSchema.kw('sql', text('INSERT INTO ' + DuckdbSql.table(copy) + ' BY NAME SELECT * FROM ' + DuckdbSql.table(table)), 'counter', text('copied'))
		steps += insert
		for (field : ModelUtil.vectorFields(sourceCollection).filter[DuckdbSchema.present(table, it)]) {
			val index = schema.indexOf(sourceCollection, field)
			if (index !== null && index.family.literal != 'flat') {
				val column = table.column(ModelUtil.fieldName(field))
				val sql = schema.vectorIndex(sourceCollection, table, field, index, false)
				steps += DuckdbSchema.kw('sql', text(sql.replace('CREATE INDEX ' + DuckdbSql.id(DuckdbSchema.indexName(sourceCollection, index.name)),
					'CREATE INDEX ' + DuckdbSql.id(targetName + '_' + index.name)).replace(' ON ' + DuckdbSql.table(table) + ' USING HNSW (' + DuckdbSql.id(column.name),
					' ON ' + DuckdbSql.table(copy) + ' USING HNSW (' + DuckdbSql.id(column.name))))
			}
		}
		val refresh = DuckdbSql.createFtsIndex(copy)
		if (refresh !== null) steps += DuckdbSchema.kw('sql', text(refresh))
		#[new Code.Return(runtime('transaction', DuckdbSchema.kw('steps', items(steps))))]
	}

	/**
	 * A deployment is the database file the program attached as vecdsl: the target file is attached, COPY FROM DATABASE copies
	 * schemas, tables, indexes, sequences, macros and data into it in one transaction, and it is detached after the commit
	 * (DuckDB refuses DETACH while the transaction has work on it, measured). The result counts the tables copied.
	 */
	def private List<Stmt> cloneDatabase(String target) {
		val clone = 'vecdsl_clone'
		val steps = #[
			DuckdbSchema.kw('sql', text('ATTACH ' + DuckdbSql.literal(DuckdbSql.localPath(target)) + ' AS ' + clone)) as Expr,
			DuckdbSchema.kw('sql', text('COPY FROM DATABASE vecdsl TO ' + clone)),
			DuckdbSchema.kw('sql', text("SELECT count(*) FROM duckdb_tables() WHERE database_name = '" + clone + "' AND NOT starts_with(schema_name, 'fts_')"))
		]
		#[new Code.Let('copied', runtime('transaction', DuckdbSchema.kw('steps', items(steps)))) as Stmt, DuckdbSchema.execute(#['DETACH ' + clone]),
			new Code.Return(new Code.Ref('copied'))]
	}

	/** An HNSW or ART index is dropped and created again; the fts index of a table is rebuilt in place. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (name : ModelUtil.extParams(it, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (concurrently) notes += 'concurrently on RebuildIndex, which DuckDB runs inside one transaction'
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		val table = schema.table(collection)
		val target = index
		val sql = switch target {
			VectorIndex: {
				val field = target.field ?: EcoreUtil2.getContainerOfType(target, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				schema.vectorIndex(collection, table, field, target, false)
			}
			PayloadIndex: schema.payloadIndex(collection, table, target, false)
		}
		if (sql === null) return #[DuckdbSchema.execute(#[DuckdbSql.createFtsIndex(table)])]
		#[DuckdbSchema.execute(#['DROP INDEX IF EXISTS ' + DuckdbSql.qualified(table, DuckdbSchema.indexName(collection, index.name)), sql])]
	}

	/** The rows of a derived collection are replaced by the rows its file holds now, in one transaction, then the fts rebuild. */
	def dispatch List<Stmt> body(RefreshExternal it) {
		if (collection.external === null) throw Unsupported.of('refreshing ' + collection.name + ', which is not derived from an external source')
		schema.note(it, 'async')
		val table = schema.table(collection)
		val q = new Query
		val source = collection.external
		val sql = DuckdbSql.insertFromFile(q, table, DuckdbSql.reader(source.location, if (written(source, 'format')) source.format.literal else null),
			DuckdbSql.localPath(source.location), newLinkedHashMap, 'error')
		val insert = DuckdbSchema.kw('sql', text(sql), 'params', q.params, 'counter', text('refreshed'))
		val refresh = DuckdbSql.createFtsIndex(table)
		if (refresh !== null) insert.put('refresh', items(#[text(refresh) as Expr]))
		val steps = #[DuckdbSchema.kw('sql', text('DELETE FROM ' + DuckdbSql.table(table)), 'counter', text('delete_count')) as Expr, insert]
		#[new Code.Return(runtime('transaction', DuckdbSchema.kw('steps', items(steps))))]
	}

	def dispatch List<Stmt> body(Truncate it) {
		val table = schema.table(collection)
		if (namespace !== null) return dropSlice(collection, table.namespace, namespace)
		#[new Code.Return(DuckdbSchema.change('TRUNCATE ' + DuckdbSql.table(table), null, 'delete_count', table, false))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val q = new Query
		val owner = scope
		val source = switch kind {
			case COLLECTIONS: {
				val schemaName = if (owner instanceof Database) (owner as Database).name else DuckdbSql.MAIN
				if (owner !== null && !(owner instanceof VectorDatabase) && !(owner instanceof Database)) {
					throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; DuckDB lists the tables of a schema')
				}
				DuckdbQueries.collections(q, schemaName)
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				DuckdbQueries.databases()
			}
			case INDEXES: DuckdbQueries.indexes(q, scopeTable(owner, 'indexes'), (owner as VectorCollection).name)
			case PARTITIONS: throw Unsupported.of(NO_PARTITIONS)
			case ALIASES: {
				val table = if (owner === null) null else scopeTable(owner, 'aliases')
				DuckdbQueries.aliases(q, table, table?.schema ?: DuckdbSql.MAIN)
			}
			case TENANTS,
			case NAMESPACES: {
				val table = scopeTable(owner, kind.literal)
				val column = if (kind.literal == 'tenants') table.tenant else table.namespace
				if (column === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				DuckdbQueries.slices(table, column)
			}
			case SNAPSHOTS: DuckdbQueries.snapshots(q)
		}
		val sql = DuckdbQueries.names(q, source, if (prefix === null) null else text(prefix), if (written(it, 'listLimit')) listLimit else null)
		#[new Code.Return(call('value', sql, q, null))]
	}

	def private Table scopeTable(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.table(owner as VectorCollection)
	}

	/** CREATE INDEX returns once the index is built, so awaiting one reads its state once. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state', 'timeoutSeconds')
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		val q = new Query
		#[new Code.Return(call('value', DuckdbQueries.indexState(q, schema.table(collection), DuckdbSchema.indexName(collection, index.name)), q, null))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'explain', 'reranker', 'queryModel', 'highlighted')
		queryExtensions(it)
		val table = schema.table(collection)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val metric = if (written(it, 'metric')) it.metric.literal else column.vec.metric
		val q = new Query
		val query = queryVector(q, input, collection, table, column)
		val List<String> stages = newArrayList
		val staged = prefetchStages(it, q, collection, table, prefetches, input, field, stages)
		val where = DuckdbSql.and(#[DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts), DuckdbQueries.within(table, staged)])
		val shape = shapeOf(it, collection, table, field)
		val exhaustive = exact || (params !== null && params.exhaustive)
		val options = settings(collection, field, params, fetchOf(shape, topK + offsetOf(it)), exhaustive)
		val prelude = if (stages.empty) null else stages.join(', ')
		val search = new Search(table, column, metric, query, prelude, topK, offsetOf(it), where, projection(collection, table, output, excludedOf(it), withVectors),
			written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds), exhaustive)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', DuckdbQueries.vectorSearch(q, search, shape), q, options))]
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'minShouldMatch', 'fuzziness', 'slop', 'highlighted')
		queryExtensions(it)
		if (params !== null) schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'timeoutMillis')
		val table = schema.table(collection)
		val q = new Query
		val fields = keywordFields(collection, table, searchFields)
		val words = keywordText(input)
		val operator = keywordOperator(if (written(it, 'textQueryType')) textQueryType.literal else 'match', if (written(it, 'matchOperator')) matchOperator.literal else 'and')
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val keywords = new Keywords(table, fields, operator, words, topK, offsetOf(it), where, projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds))
		val shape = shapeOf(it, collection, table, null)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', DuckdbQueries.keywordSearch(q, keywords, shape), q, extensionSettings(it)))]
	}

	/** Hybrid search fused in SQL: each leg ranks its candidates in a CTE, and the fusion function of vecDSL combines them. */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'highlighted')
		queryExtensions(it)
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		if (params !== null) schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'timeoutMillis')
		val table = schema.table(collection)
		val q = new Query
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val shared = input
		val defaultLimit = topK + offsetOf(it)
		val Map<String, String> options = newLinkedHashMap
		val List<Leg> parts = newArrayList
		var index = 0
		for (leg : legs) {
			val weight = weights.get(index)
			val limit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else defaultLimit
			val List<Cond> legConditions = newArrayList
			legConditions += filterOf(it, collection, table, filter).parts
			if (feature(leg, 'filter') !== null) legConditions += new DuckdbFilter().where(feature(leg, 'filter') as FilterBlock).parts
			val legWhere = DuckdbSql.where(q, table, legConditions)
			for (name : ModelUtil.extParams(leg, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name + ' on a hybrid leg')
			switch leg {
				VectorLeg: {
					val field = vectorFieldOf(collection, leg.vectorField)
					val column = table.column(ModelUtil.fieldName(field))
					val metric = if (written(leg, 'metric')) leg.metric.literal else column.vec.metric
					val query = queryVector(q, leg.input ?: shared, collection, table, column)
					options.putAll(settings(collection, field, leg.params, limit, leg.exact))
					parts += new Leg(column, metric, query, null, null, null, limit, legWhere, weight, minOf(leg.bounds), maxOf(leg.bounds), leg.exact)
				}
				KeywordLeg: {
					if (written(leg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
					val fields = keywordFields(collection, table, leg.searchFields)
					val operator = keywordOperator(if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match', if (written(leg, 'matchOperator')) leg.matchOperator.literal else 'and')
					parts += new Leg(null, null, null, fields, operator, keywordText(leg.input ?: shared), limit, legWhere, weight, null, null, false)
				}
			}
			index = index + 1
		}
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val hybrid = new Hybrid(table, parts, fusionKind, k, topK, offsetOf(it), projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds))
		options.putAll(extensionSettings(it))
		val shape = shapeOf(it, collection, table, null)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', DuckdbQueries.hybridSearch(q, hybrid, shape), q, options))]
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

	/** The average-vector strategy over example records or vectors, computed in SQL; the examples are not returned. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'highlighted')
		queryExtensions(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val table = schema.table(collection)
		val lookup = schema.table(lookupCollection ?: collection)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val lookupColumn = lookup.column(column.name)
		val q = new Query
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) {
			if (isVector(example)) positiveVectors += Literals.of(example) else positiveIds += Literals.of(example)
		}
		for (example : negatives) {
			if (isVector(example)) negativeVectors += Literals.of(example) else negativeIds += Literals.of(example)
		}
		val prelude = DuckdbQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors)
		val List<String> parts = newArrayList
		parts += DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		if (lookupCollection === null || lookupCollection === collection) {
			val List<Expr> examples = newArrayList
			examples += positiveIds
			examples += negativeIds
			if (!examples.empty) parts += '(' + DuckdbSql.keyIn(q, table, items(examples)) + ') IS NOT TRUE'
		}
		val where = DuckdbSql.and(parts)
		val shape = shapeOf(it, collection, table, field)
		val options = settings(collection, field, params, fetchOf(shape, topK + offsetOf(it)), false)
		val search = new Search(table, column, column.vec.metric, '(SELECT v FROM q)', prelude, topK, offsetOf(it), where,
			projection(collection, table, output, excludedOf(it), withVectors), written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds), false)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', DuckdbQueries.vectorSearch(q, search, shape), q, options))]
	}

	def private boolean isVector(Literal example) {
		example instanceof ListLit || example instanceof SparseLit
			|| (example instanceof ParamRef && #['vector', 'sparse_vector'].contains((example as ParamRef).param.type.literal))
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val shape = projection(collection, table, output, newArrayList, withVectors)
		val List<String> names = newArrayList
		names += shape.output
		names += shape.vectors
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, null).parts)
		#[new Code.Return(call('query', DuckdbQueries.getRows(q, table, items(ids.map[Literals.of(it)]), names, where), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		#[new Code.Return(call('value', DuckdbQueries.count(table, where), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val shape = projection(collection, table, output, excludedOf(it), withVectors)
		val List<String> names = newArrayList
		names += shape.output
		names += shape.vectors
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val List<Order> order = newArrayList
		for (clause : sortClauses) {
			if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			order += new Order(fieldRefOf(collection, table, clause.field, 'sort'), clause.direction == ESortDirection.DESC)
		}
		val limit = if (written(it, 'scrollLimit')) scrollLimit else null
		if (written(it, 'sliceTotal') && (sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal)) throw Unsupported.of('a slice index outside 0 to the slice count minus 1')
		val cursorKey = if (cursor === null) null else text(cursor)
		val slices = if (written(it, 'sliceTotal')) sliceTotal else null
		#[new Code.Return(call('query', DuckdbQueries.scroll(q, table, names, where, order, limit, offsetOf(it), cursorKey, sliceIndex, slices), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val shape = projection(collection, table, output, newArrayList, false)
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val first = written(it, 'method') && method == ESampleMethod.FIRST
		#[new Code.Return(call('query', DuckdbQueries.sample(table, shape.output, where, sampleSize, first), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val q = new Query
		val where = DuckdbSql.where(q, table, filterOf(it, collection, table, filter).parts)
		val sample = if (written(it, 'sampleSize')) sampleSize else 10
		val limit = if (written(it, 'matrixLimit')) matrixLimit else 3
		#[new Code.Return(call('query', DuckdbQueries.distanceMatrix(table, column, column.vec.metric, where, sample, limit), q, extensionSettings(it)))]
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
			helpers += new Program.Method(helper, ModelUtil.parameterNames(query), body(query), newArrayList, newArrayList, true)
			calls += self(helper)
			index = index + 1
		}
		current = owner
		#[new Code.Return(items(calls))]
	}

	def dispatch List<Stmt> body(Method it) {
		throw Unsupported.of(NOT_MAPPED + eClass.name)
	}

	// ---------------------------------------------------------------- helpers

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	def private int offsetOf(EObject query) {
		if (written(query, 'offset')) feature(query, 'offset') as Integer else 0
	}

	def private List<Field> excludedOf(EObject query) {
		(feature(query, 'excluded') ?: newArrayList) as List<Field>
	}

	def private Expr minOf(ScoreBounds bounds) {
		if (bounds !== null && written(bounds, 'min')) num(bounds.min) else null
	}

	def private Expr maxOf(ScoreBounds bounds) {
		if (bounds !== null && written(bounds, 'max')) num(bounds.max) else null
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

	/** Output fields (the key is always returned) and the vector fields returned with 'with-vectors' or named in the output. */
	def private Projection projection(VectorCollection collection, Table table, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val listed = if (output === null || output.allFields) collection.fields as List<? extends Field> else output.fields
		for (field : listed) {
			if (field !== collection.primaryKey && !excluded.contains(field) && DuckdbSchema.present(table, field)) {
				if (field instanceof VectorField) {
					if (output !== null && !output.allFields) vectors += table.column(ModelUtil.fieldName(field)).name
				} else {
					fields += table.column(ModelUtil.fieldName(field)).name
				}
			}
		}
		if (withVectors) {
			for (field : ModelUtil.vectorFields(collection)) {
				if (DuckdbSchema.present(table, field)) {
					val vectorName = table.column(ModelUtil.fieldName(field)).name
					if (!vectors.contains(vectorName) && !excluded.contains(field)) vectors += vectorName
				}
			}
		}
		new Projection(fields, vectors)
	}

	/**
	 * Search settings applied with SET for one statement: hnsw_ef_search of at least limit plus offset when the field has an
	 * HNSW index, and the pre- or post-filtering of the vss index scan (hnsw_prefilter, on by default since vss 2026-09-03).
	 */
	def private Map<String, String> settings(VectorCollection collection, VectorField field, SearchParams params, int fetch, boolean exact) {
		val Map<String, String> result = newLinkedHashMap
		val index = schema.indexOf(collection, field)
		val hnsw = index !== null && index.family.literal != 'flat'
		if (params !== null) {
			if (written(params, 'targetRecall')) throw Unsupported.of('target-recall: hnsw_ef_search is an effort setting, with no declarable recall target')
			if (written(params, 'probes')) throw Unsupported.of('probes; vss builds HNSW indexes, which have no lists to probe')
			if (written(params, 'scanBudget')) throw Unsupported.of('scan-budget; the vss index scan has no limit on visited entries')
			for (name : ModelUtil.extParams(params, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
			schema.note(params, 'timeoutMillis', 'oversampling', 'rescore')
		}
		if (exact || !hnsw) {
			if (params !== null) schema.note(params, 'candidatePool', 'filterStrategy')
			return result
		}
		val candidates = if (params !== null && written(params, 'candidatePool')) params.candidatePool else if (written(index, 'searchCandidates')) index.searchCandidates else 64
		if (candidates < fetch || (params !== null && written(params, 'candidatePool'))) result.put('hnsw_ef_search', String.valueOf(Math.max(candidates, fetch)))
		val strategy = if (params !== null && written(params, 'filterStrategy')) params.filterStrategy.literal else 'auto'
		switch strategy {
			case 'post_filter': result.put('hnsw_prefilter', 'false')
			case 'iterative': throw Unsupported.of('the iterative filter strategy; the vss index scan filters before the graph search (hnsw_prefilter) or after it, without iterative scans')
			default: {}
		}
		result
	}

	/** DuckDB settings written in an 'extension duckdb' block cannot be named: the vendor id is not a VendorId of the grammar. */
	def private Map<String, String> extensionSettings(EObject host) {
		val Map<String, String> result = newLinkedHashMap
		for (name : ModelUtil.extParams(host, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		result
	}

	/** Rejects clauses DuckDB lacks, with their reason, and clauses not generated yet. */
	def private void reject(EObject statement, String... features) {
		for (name : features) {
			if (written(statement, name)) {
				val clause = "the '" + (CLAUSES.get(name) ?: name) + "' clause"
				val reason = REASONS.get(name)
				if (reason !== null) throw Unsupported.of(clause + '; ' + reason)
				val pending = PENDING_CLAUSES.get(name)
				if (pending !== null) throw Unsupported.of(clause + '; ' + PENDING + ' (' + pending + ')')
				throw Unsupported.of(clause)
			}
		}
	}

	/** Extension blocks name a vendor id; duckdb is not one of the grammar, so none applies here. */
	def private void queryExtensions(EObject host) {
		extensionSettings(host)
	}

	/** The query vector as SQL: a bound vector, or the stored vector of a record (a scalar subquery). */
	def private String queryVector(Query q, QueryInput input, VectorCollection collection, Table table, Column column) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR: q.json(Literals.of(input.value), column.vec.sql)
			case ID: {
				val source = schema.table(input.lookupCollection ?: collection)
				val stored = source.column(column.name)
				'(SELECT x.' + DuckdbSql.id(stored.name) + ' FROM ' + DuckdbSql.table(source) + ' x WHERE x.' + DuckdbSql.id(source.key) + ' = '
					+ q.bind(Literals.of(input.value), source.keyColumn.paramType) + ' LIMIT 1)'
			}
			case SPARSE_VECTOR: throw Unsupported.of('sparse vector input; ' + FLOAT_ONLY)
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; a DuckDB array column holds one vector')
			default: throw Unsupported.of(input.kind.literal + ' input' + NO_MODELS)
		}
	}

	/**
	 * The result shape of a search: the keyset cursor, boosts, MMR, groups, total, aggregations and sort keys. The searched
	 * vector field is null for keyword and hybrid searches.
	 */
	def private Shape shapeOf(EObject search, VectorCollection collection, Table table, VectorField searched) {
		val cursorText = feature(search, 'cursor') as String
		val grouping = feature(search, 'groupBy') as FieldPath
		val diversity = feature(search, 'mmr') as MmrConfig
		val boosting = feature(search, 'boost') as BoostBlock
		val sortClauses = (feature(search, 'sortClauses') ?: newArrayList) as List<SortClause>
		if (cursorText !== null && (grouping !== null || diversity !== null)) {
			throw Unsupported.of("the 'cursor' clause with group-by or mmr; keyset pagination continues after one record in an order that each record's own values decide")
		}
		if (grouping !== null && !sortClauses.empty) throw Unsupported.of('sort with group-by; groups follow the relevance of their best hit')
		if (grouping !== null && diversity !== null) throw Unsupported.of('mmr with group-by; MMR selects single hits, groups select records per group value')
		if (boosting !== null && written(search, 'scoreForm') && feature(search, 'scoreForm') == EScoreForm.NATIVE) {
			throw Unsupported.of('boosts with score native; boosts apply to the canonical similarity, where higher is better')
		}
		val group = if (grouping === null) null else new Group(fieldRefOf(collection, table, grouping, 'group-by'),
			if (written(search, 'groupSize')) feature(search, 'groupSize') as Integer else 1)
		val List<Aggregate> aggregates = newArrayList
		for (clause : (feature(search, 'aggregations') ?: newArrayList) as List<AggregationClause>) aggregates += aggregateOf(collection, table, clause)
		val List<Order> order = newArrayList
		for (clause : sortClauses) {
			order += new Order(if (clause.byRelevance) null else fieldRefOf(collection, table, clause.field, 'sort'), clause.direction == ESortDirection.DESC)
		}
		val cursorKey = if (cursorText === null) null else text(cursorText)
		new Shape(cursorKey, if (boosting === null) null else boostOf(collection, table, boosting),
			if (diversity === null) null else mmrOf(collection, table, diversity, searched, (feature(search, 'topK') as Integer) + offsetOf(search)),
			group, written(search, 'withTotal'), aggregates, order)
	}

	/**
	 * Prefetch stages as CTEs of the keys each stage retrieves (its own input, vector field, metric, filter and limit, ranking only
	 * the rows its nested stages return); the names of the given stages, in order. A stage without input or field takes the search's.
	 */
	def private List<String> prefetchStages(EObject search, Query q, VectorCollection collection, Table table, List<Prefetch> given, QueryInput input,
		VectorField field, List<String> stages) {
		val List<String> names = newArrayList
		for (stage : given) {
			schema.note(stage, 'weight', 'params')
			val stageInput = stage.input ?: input
			val stageField = if (stage.vectorField !== null) vectorFieldOf(collection, stage.vectorField) else field
			val inner = prefetchStages(search, q, collection, table, stage.nested, stageInput, stageField, stages)
			val stageColumn = table.column(ModelUtil.fieldName(stageField))
			val stageMetric = if (written(stage, 'metric')) stage.metric.literal else stageColumn.vec.metric
			val stageQuery = queryVector(q, stageInput, collection, table, stageColumn)
			val stageWhere = DuckdbSql.where(q, table, filterOf(search, collection, table, stage.filter).parts)
			val name = 'stage_' + stages.size
			stages += DuckdbQueries.stage(name, table, stageColumn, stageMetric, stageQuery, DuckdbSql.and(#[stageWhere, DuckdbQueries.within(table, inner)]), stage.stageLimit)
			names += name
		}
		names
	}

	/** Candidates the index must deliver: the MMR pool when there is one, else limit plus offset. */
	def private int fetchOf(Shape shape, int fetch) {
		if (shape.mmr === null) fetch else Math.max(shape.mmr.candidates, fetch)
	}

	/** An aggregation over the matching records, named by its alias or kind_field as on the other targets. */
	def private Aggregate aggregateOf(VectorCollection collection, Table table, AggregationClause clause) {
		val kind = clause.kind.literal
		val source = fieldRefOf(collection, table, clause.field, 'aggregations')
		val name = clause.alias ?: kind + '_' + (#[source.column.name] + source.path).join('_')
		val size = if (written(clause, 'bucketCount')) clause.bucketCount else 10
		switch kind {
			case 'range': throw Unsupported.of('range aggregations; vecDSL gives no range bounds to count records in')
			case 'histogram': {
				if (clause.interval === null || !clause.interval.matches('[0-9]+(\\.[0-9]+)?')) throw Unsupported.of('a histogram without a numeric interval')
				new Aggregate(name, kind, source, num(Double.parseDouble(clause.interval)), size)
			}
			case 'date_histogram': {
				if (clause.interval === null) throw Unsupported.of('a date histogram without an interval')
				new Aggregate(name, kind, source, text(clause.interval), size)
			}
			default: new Aggregate(name, kind, source, null, size)
		}
	}

	/** Boost functions: defaults weight 1, decay rate 0.5 and offset 0, combine and apply multiply. */
	def private Boost boostOf(VectorCollection collection, Table table, BoostBlock block) {
		val List<BoostFn> functions = newArrayList
		for (fn : block.functions) {
			val weight = if (written(fn, 'weight')) fn.weight else 1.0
			val List<Cond> when = newArrayList
			if (fn.condition !== null) when += new DuckdbFilter().condition(fn.condition)
			switch fn.kind {
				case EBoostKind.FIELD_VALUE: {
					if (fn.target === null) throw Unsupported.of('a field_value boost without a field')
					functions += new BoostFn('field_value', fieldRefOf(collection, table, fn.target, 'boost'), weight, null, null, null, null, 0.5, when)
				}
				case EBoostKind.DECAY: {
					if (fn.target === null || !written(fn, 'shape')) throw Unsupported.of('a decay boost without a field, decay shape, origin and scale')
					val rate = if (written(fn, 'decayRate')) fn.decayRate else 0.5
					if (rate <= 0 || rate >= 1) throw Unsupported.of('a decay rate outside the open interval from 0 to 1')
					functions += new BoostFn('decay', fieldRefOf(collection, table, fn.target, 'boost'), weight, fn.shape.literal, Literals.of(fn.origin),
						Literals.of(fn.scale), if (fn.decayOffset === null) null else Literals.of(fn.decayOffset), rate, when)
				}
				case EBoostKind.CONDITION: {
					if (when.empty) throw Unsupported.of('a condition boost without a condition')
					functions += new BoostFn('condition', null, weight, null, null, null, null, 0.5, when)
				}
			}
		}
		new Boost(functions, if (written(block, 'combine')) block.combine.literal else 'multiply', if (written(block, 'apply')) block.apply.literal else 'multiply')
	}

	/** MMR over a candidate pool (the given size, else ten times limit plus offset), diversified on the named, searched or only vector field. */
	def private Mmr mmrOf(VectorCollection collection, Table table, MmrConfig config, VectorField searched, int fetch) {
		if (config.diversityLambda < 0 || config.diversityLambda > 1) throw Unsupported.of('an mmr lambda outside 0 to 1')
		val field = if (config.vectorField !== null) vectorFieldOf(collection, config.vectorField) else searched ?: vectorFieldOf(collection, null)
		val column = table.column(ModelUtil.fieldName(field))
		val pool = if (written(config, 'candidates')) Math.max(config.candidates, fetch) else Math.max(10 * fetch, 10)
		new Mmr(config.diversityLambda, pool, column, column.vec.metric)
	}

	/** A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a JSON column. */
	def private Ref fieldRefOf(VectorCollection collection, Table table, FieldPath path, String purpose) {
		if (path.rawPath !== null) throw Unsupported.of('raw paths in ' + purpose + '; the generator reads declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of(purpose + ' on vector field ' + ModelUtil.fieldName(path.field))
		val column = table.column(ModelUtil.fieldName(path.field))
		if (path.subFields.empty) return new Ref(column, #[], null)
		new Ref(column, path.subFields.map[sub | ModelUtil.fieldName(sub)].toList, DuckdbFilter.leafType(path))
	}

	/** Keyword fields: the listed full-text columns with boosts, else every full-text column of the collection. */
	def private List<KeywordField> keywordFields(VectorCollection collection, Table table, List<SearchFieldRef> refs) {
		val List<KeywordField> result = newArrayList
		if (refs.empty) {
			for (column : table.columns) if (column.fullText) result += new KeywordField(column, 1.0)
		} else {
			for (ref : refs) {
				val column = table.column(ModelUtil.fieldName(ref.field))
				if (!column.fullText) throw Unsupported.of('keyword search on ' + column.name + ', which is not a full-text field')
				result += new KeywordField(column, if (written(ref, 'boost')) ref.boost else 1.0)
			}
		}
		if (result.empty) throw Unsupported.of('keyword search in a collection without full-text fields')
		result
	}

	def private Expr keywordText(QueryInput input) {
		if (input === null || input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(input.value instanceof StringLit || input.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(input.value)
	}

	/** The match_bm25 form of a text query type: match with all terms (and) or any term (or). */
	def private String keywordOperator(String type, String operator) {
		switch type {
			case 'match': if (operator == 'or') 'or' else 'and'
			case 'phrase',
			case 'phrase_prefix': throw Unsupported.of('text-query ' + type + '; ' + DuckdbSql.NO_POSITIONS)
			case 'term',
			case 'query_string': throw Unsupported.of('text-query ' + type + '; match_bm25 tokenizes, stems and filters the query text and has no query syntax or unanalyzed terms')
			default: throw Unsupported.of('text-query ' + type + '; match_bm25 matches whole stemmed terms, without fuzzy, prefix, wildcard or regular-expression terms')
		}
	}

	def private Expr rows(RecordSource source, Table table) {
		switch source {
			FileSource: runtime('read_records', DuckdbSchema.kw('location', text(source.location)))
			InlineRecords: items(source.records.map[item | record(item, table)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record, Table table) {
		val result = entries
		for (entry : record.entries) {
			val column = table.column(ModelUtil.fieldName(entry.field))
			result.put(column.name, Literals.of(entry.value))
		}
		result
	}
}
