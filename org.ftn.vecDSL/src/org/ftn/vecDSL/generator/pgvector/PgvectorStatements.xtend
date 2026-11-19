package org.ftn.vecDSL.generator.pgvector

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
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Aggregate
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Boost
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.BoostFn
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Group
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Hybrid
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.KeywordField
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Keywords
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Leg
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Mmr
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Order
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Projection
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Ref
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Search
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Shape
import org.ftn.vecDSL.generator.pgvector.PgvectorQueries.Terms
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Column
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Cond
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Query
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Table
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.AddAliasOp
import org.ftn.vecDSL.vecDSL.AddFieldOp
import org.ftn.vecDSL.vecDSL.AddFunctionOp
import org.ftn.vecDSL.vecDSL.Alias
import org.ftn.vecDSL.vecDSL.AggregationClause
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
import org.ftn.vecDSL.vecDSL.SortClause
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
import static org.ftn.vecDSL.generator.pgvector.PgvectorSchema.call
import static org.ftn.vecDSL.generator.pgvector.PgvectorSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to SQL for PostgreSQL with pgvector in the host-language-neutral code model. The SQL text is
 * built here (through PgvectorSql and PgvectorQueries) and passed to the runtime with its parameters, so the three host
 * languages send identical statements: query and value return json rows, execute runs DDL in one transaction, transaction
 * runs a vecDSL transaction, autocommit runs what PostgreSQL refuses inside a transaction block.
 */
class PgvectorStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; PgvectorCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for pgvector: '
	/** Suffix of constructs PostgreSQL supports that the generator does not produce yet. */
	public static val PENDING = 'has no pgvector mapping yet'
	public static val NO_MODELS = '; PostgreSQL has no embedding models (the pgvector image includes no model extension such as pgai), so generated programs supply their own vectors'
	public static val EXTERNAL = 'collections derived from external sources; PostgreSQL reads foreign files through file_fdw, which reads text and CSV, not parquet, lance, iceberg or vortex'
	public static val NO_TTL = 'ttl; PostgreSQL rows do not expire, and the pgvector image has no scheduler (pg_cron) to delete them'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; PostgreSQL has no per-table drop guard (event triggers need a superuser and act on the whole database)'
	public static val NO_STORAGE = 'storage settings; PostgreSQL keeps tables in its data directory or in tablespaces that a superuser creates for server directories'
	public static val NO_TAGS = 'collection tags; a PostgreSQL table carries one comment (the description) and no tags'
	public static val NO_BM25 = 'BM25 parameters; PostgreSQL full-text search ranks with ts_rank, which has no k1 or b (BM25 needs an extension such as ParadeDB pg_search, not in the pgvector image)'
	public static val NO_SNAPSHOTS = 'snapshots; PostgreSQL backs up with pg_dump and pg_basebackup, client programs outside SQL (pg_export_snapshot names a snapshot only for the transaction that exports it)'
	public static val SINGLE_SERVER = 'one PostgreSQL server holds the data; streaming replication is configured on the servers, and core PostgreSQL has no sharding'
	static val NO_GENERATION = 'PostgreSQL has no generative models (the pgvector image includes no model extension)'
	static val NO_RERANKERS = 'PostgreSQL has no reranking models (the pgvector image includes no model extension)'
	static val NO_METRICS = 'metrics configuration; PostgreSQL statistics are server settings (postgresql.conf, shared_preload_libraries for pg_stat_statements), not SQL objects'
	static val NO_SCHEDULES = 'backup schedules; PostgreSQL has no scheduler in the pgvector image (pg_cron) and no SQL backup'
	static val NO_CLUSTER_LIMITS = 'cluster limits; PostgreSQL has no setting that limits the number of tables'
	static val NO_FILES = 'file resources; text search dictionaries read files from the server\'s tsearch_data directory, which the driver cannot write'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'boost' -> 'boost', 'queryModel' -> 'model', 'minShouldMatch' -> 'min-should-match', 'fuzziness' -> 'fuzziness',
		'slop' -> 'slop', 'sliceIndex' -> 'slice'
	}

	/** Why PostgreSQL cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'PostgreSQL reads the current committed state and keeps no queryable history of a table',
		'explain' -> 'PostgreSQL explains query plans (EXPLAIN), not scores',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated pgvector programs embed no query text' + NO_MODELS,
		'minShouldMatch' -> 'a tsquery combines terms with and, or and phrase operators and has no minimum number of matching terms',
		'fuzziness' -> 'full-text search matches normalized lexemes exactly (pg_trgm similarity is a separate operator without ts_rank scores)',
		'slop' -> 'a tsquery phrase fixes the distance between terms (<N>) and has no slop'
	}

	/** Clauses PostgreSQL supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'jsonb_path_exists'
	}

	static val PRIVILEGES = #{'read' -> 'SELECT', 'write' -> 'INSERT, UPDATE, DELETE', 'admin' -> 'ALL PRIVILEGES', 'all' -> 'ALL PRIVILEGES',
		'select' -> 'SELECT', 'insert' -> 'INSERT', 'update' -> 'UPDATE', 'delete' -> 'DELETE', 'truncate' -> 'TRUNCATE',
		'references' -> 'REFERENCES', 'trigger' -> 'TRIGGER', 'maintain' -> 'MAINTAIN'}

	static val SETTINGS = #['hnsw.ef_search', 'hnsw.iterative_scan', 'hnsw.max_scan_tuples', 'hnsw.scan_mem_multiplier', 'ivfflat.probes',
		'ivfflat.iterative_scan', 'ivfflat.max_probes']

	val List<String> notes = newArrayList
	val PgvectorSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new PgvectorSchema(notes, script)
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
			case 'bm25': 'PostgreSQL full-text search indexes text columns directly (tsvector) and has no BM25 function producing sparse vectors'
			case 'custom': 'custom functions; has no pgvector mapping yet (generated columns)'
			default: 'PostgreSQL has no model functions (the pgvector image includes no model extension such as pgai)'
		}
	}

	def static String collectionLimit(String type) {
		'collection limits (' + type + '); PostgreSQL has fixed limits (1600 columns per table, 2000 indexed and 16000 stored dimensions per vector) and no per-table limit settings'
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
				for (name : ModelUtil.extParams(concept, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Database: {
				PgvectorSql.id(concept.name)
				val create = PgvectorSchema.autocommit('CREATE DATABASE ' + PgvectorSql.id(concept.name), null, null)
				if (ifNotExists) result += new Code.When(databaseExists(concept.name), true, #[create]) else result += create
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Alias: {
				val table = schema.table(concept.collection)
				result += PgvectorSchema.execute(#[PgvectorSchema.createView(concept.name, table, ifNotExists)], table.database)
			}
			VectorIndex: result += createIndex(concept, ifNotExists)
			PayloadIndex: result += createIndex(concept, ifNotExists)
			VectorField: result += addField(member(concept, 'a field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += addPartition(member(concept, 'a partition'), concept, ifNotExists)
			Tenant: result += require(member(concept, 'a tenant'))
			Namespace: result += require(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			FileResource: throw Unsupported.of(NO_FILES)
			Analyzer: result += schema.createAnalyzer(concept, analyzerDatabase(concept))
			AnalyzerFilter: result += analyzerFilter(concept)
			FieldLimit: result += addLimit(concept)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: throw Unsupported.of('quantization outside a vector index; pgvector quantizes through the expression of an hnsw or ivfflat index')
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

	/** The program endpoint: checked here, opened by connect(). Passwords come from the environment only. */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; the PostgreSQL drivers connect to a server (an embedded or WASM PostgreSQL is not the target)')
		}
		if (concept.apiKey !== null || concept.token !== null) throw Unsupported.of('api keys and tokens; PostgreSQL authenticates users with passwords, certificates or the server\'s other methods')
		if (concept.password instanceof StringLit) throw Unsupported.of('a password written in the script; read it from an environment variable with env(...)')
		if (concept.databaseName !== null) PgvectorSql.id(concept.databaseName)
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; PostgreSQL speaks its own protocol on one port')
		for (name : ModelUtil.extParams(concept, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(concept, 'timeoutMillis', 'cloud', 'region', 'project')
	}

	def private Code.Call databaseExists(String name) {
		val q = new Query
		call('exists', 'SELECT to_json(EXISTS (SELECT 1 FROM pg_database WHERE datname = ' + q.bind(text(name), 'text') + '))', q, null, null)
	}

	/** Password authentication is the connection's; roles are PostgreSQL roles; other methods are server configuration. */
	def private List<Stmt> authentication(AuthConfig auth, boolean ifNotExists) {
		if (auth.type != EAuthType.NONE && auth.type != EAuthType.USERNAME_PASSWORD) {
			throw Unsupported.of(auth.type.literal + ' authentication; PostgreSQL authenticates with passwords, certificates, Kerberos, LDAP and RADIUS, configured on the server (pg_hba.conf), and has no api keys or tokens')
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; has no pgvector mapping yet (sslcert and sslkey connection parameters)')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; core PostgreSQL has no transparent data encryption')
		if (auth.auditLog) throw Unsupported.of('audit logs; PostgreSQL audits through the pgaudit extension, which the pgvector image does not include')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding, ifNotExists)
		result
	}

	/** A role without login and its grants on a collection (or on every table of the public schema). */
	def private List<Stmt> role(RoleBinding binding, boolean ifNotExists) {
		val name = binding.roleName
		PgvectorSql.id(name)
		val table = if (binding.collection === null) null else schema.table(binding.collection)
		val database = table?.database
		val List<String> grants = newArrayList
		for (privilege : binding.privileges) {
			val sql = PRIVILEGES.get(privilege.toLowerCase)
			if (sql === null) throw Unsupported.of('the privilege ' + privilege + '; PostgreSQL grants SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER and MAINTAIN on tables')
			grants += sql
		}
		val List<Stmt> result = newArrayList
		val create = PgvectorSchema.execute(#['CREATE ROLE ' + PgvectorSql.id(name) + ' NOLOGIN'], database)
		if (ifNotExists) result += new Code.When(roleExists(name, database), true, #[create]) else result += create
		if (!grants.empty) {
			val on = if (table === null) 'ALL TABLES IN SCHEMA public' else 'TABLE ' + PgvectorSql.table(table)
			result += PgvectorSchema.execute(#['GRANT ' + grants.join(', ') + ' ON ' + on + ' TO ' + PgvectorSql.id(name)], database)
		}
		result
	}

	def private Code.Call roleExists(String name, String database) {
		val q = new Query
		call('exists', 'SELECT to_json(EXISTS (SELECT 1 FROM pg_roles WHERE rolname = ' + q.bind(text(name), 'text') + '))', q, null, database)
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
		throw Unsupported.of(what + ' outside a collection; PostgreSQL adds it to a table')
	}

	def private String analyzerDatabase(Analyzer analyzer) {
		val collection = ModelUtil.owningCollection(analyzer)
		if (collection === null) null else PgvectorSchema.databaseOf(collection)
	}

	/** A filter of an analyzer: the copied configuration applies it already, so creating it checks the configuration exists. */
	def private List<Stmt> analyzerFilter(AnalyzerFilter filter) {
		val analyzer = EcoreUtil2.getContainerOfType(filter, Analyzer)
		if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; a text search configuration takes its dictionaries when it is created')
		val source = if (analyzer.language === null) 'simple' else PgvectorSql.configuration(analyzer.language)
		PgvectorSchema.checkFilter(filter.type.literal, source ?: 'simple')
		val q = new Query
		#[new Code.Run(call('value', 'SELECT to_json(' + q.bind(text(PgvectorSchema.configName(analyzer)), 'text') + '::regconfig::text)', q, null, analyzerDatabase(analyzer)))]
	}

	/** A field limit added to an existing column: a named CHECK constraint. */
	def private List<Stmt> addLimit(FieldLimit limit) {
		val field = EcoreUtil2.getContainerOfType(limit, Field)
		if (field === null) throw Unsupported.of('a field limit outside a field declaration; a CHECK constraint belongs to a column')
		val collection = member(field, 'a field limit')
		val table = schema.table(collection)
		val column = table.column(ModelUtil.fieldName(field))
		val check = PgvectorSchema.check(limit, field)
		#[PgvectorSchema.execute(#['ALTER TABLE ' + PgvectorSql.table(table) + ' ADD ' + check.replace('{}', PgvectorSql.id(column.name))], table.database)]
	}

	/** An index created on an existing collection; online builds it CONCURRENTLY, outside a transaction block. */
	def private List<Stmt> createIndex(Index index, boolean ifNotExists) {
		switch index {
			VectorIndex: {
				val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				val collection = member(field, 'a vector index')
				val table = schema.table(collection)
				val sql = schema.vectorIndex(collection, table, field, index, ifNotExists)
				if (sql === null) return require(collection)
				if (index.online) return #[PgvectorSchema.autocommit(sql.replaceFirst('CREATE INDEX ', 'CREATE INDEX CONCURRENTLY '), table.database, null)]
				#[PgvectorSchema.execute(#[sql], table.database)]
			}
			PayloadIndex: {
				val field = index.target.field
				val collection = ModelUtil.targetCollection(index) ?: (if (field === null) null else ModelUtil.owningCollection(field))
				if (collection === null) throw Unsupported.of('a payload index outside a collection')
				val table = schema.table(collection)
				#[PgvectorSchema.execute(#[schema.payloadIndex(collection, table, index, ifNotExists)], table.database)]
			}
		}
	}

	/** Existence check of a collection: the statement fails when the table is missing. */
	def private List<Stmt> require(VectorCollection collection) {
		val table = schema.table(collection)
		#[new Code.Run(call('query', 'SELECT NULL::json FROM ' + PgvectorSql.table(table) + ' WHERE false', null, null, table.database))]
	}

	/** ALTER TABLE ADD COLUMN; a vector field with an index gets the index in the same transaction. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val result = #[PgvectorSchema.execute(addColumn(collection, table, field), table.database)]
		schema.evolve(collection, table.with(schema.column(collection, field)))
		result
	}

	def private List<String> addColumn(VectorCollection collection, Table table, Field field) {
		val column = schema.column(collection, field)
		val List<String> result = newArrayList
		val definition = PgvectorSql.columnDefinition(if (field instanceof VectorField) nullableVector(column) else column, false)
		result += 'ALTER TABLE ' + PgvectorSql.table(table) + ' ADD COLUMN ' + definition
		if (field instanceof VectorField) {
			val index = schema.indexOf(collection, field as VectorField)
			if (index !== null) result += schema.vectorIndex(collection, table, field as VectorField, index, false)
		}
		if (column.config !== null && field instanceof PayloadField) {
			result += PgvectorSql.createPayloadIndex(table, PgvectorSchema.ftsIndexName(collection, field as PayloadField), column, #[], 'fts', true)
		}
		result.filterNull.toList
	}

	/** Rows that exist when a vector column is added have no vector, so the added column is nullable. */
	def private Column nullableVector(Column column) {
		new Column(column.name, column.type, null, false, column.vec, null, true, null, #[])
	}

	def private List<Stmt> addPartition(VectorCollection collection, Partition partition, boolean ifNotExists) {
		val table = schema.table(collection)
		if (table.partition === null) throw Unsupported.of('a partition of a collection declared without partitions; PostgreSQL cannot partition an existing table in place')
		if (partition.key !== null || !partition.subPartitions.empty) throw Unsupported.of('partitions by key and sub-partitions; has no pgvector mapping yet (hash and sub-partitioned tables)')
		#[PgvectorSchema.execute(#[PgvectorSql.createPartition(table, partition.name, false, ifNotExists)], table.database)]
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias is a view, which can be renamed')
			val alias = concept as Alias
			return #[PgvectorSchema.execute(#['ALTER VIEW ' + PgvectorSql.id(alias.name) + ' RENAME TO ' + PgvectorSql.id(newName)], schema.table(alias.collection).database)]
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; PostgreSQL alters tables and views')
		val collection = concept as VectorCollection
		var table = schema.table(collection)
		val database = table.database
		val List<String> ddl = newArrayList
		for (operation : operations) {
			ddl += alter(collection, table, operation)
			table = evolved(collection, table, operation)
		}
		if (newName !== null) {
			ddl += 'ALTER TABLE ' + PgvectorSql.table(table) + ' RENAME TO ' + PgvectorSql.id(newName)
			table = table.named(newName)
		}
		val List<Stmt> result = if (ddl.empty) require(collection) else #[PgvectorSchema.execute(ddl, database)]
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
				PgvectorSql.id(operation.newName)
				#['ALTER TABLE ' + PgvectorSql.table(table) + ' RENAME COLUMN ' + PgvectorSql.id(table.column(ModelUtil.fieldName(operation.field)).name) + ' TO ' + PgvectorSql.id(operation.newName)]
			}
			SetPropertyOp: setProperty(table, operation)
			AddAliasOp: #[PgvectorSchema.createView(operation.newAlias.name, table, false)]
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def private String dropColumn(VectorCollection collection, Table table, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
		'ALTER TABLE ' + PgvectorSql.table(table) + ' DROP COLUMN ' + PgvectorSql.id(table.column(ModelUtil.fieldName(field)).name)
	}

	/** Collection properties: the description is the table comment; consistency is met by every committed write. */
	def private List<String> setProperty(Table table, SetPropertyOp operation) {
		switch operation.property {
			case DESCRIPTION: {
				if (!(operation.value instanceof StringLit)) throw Unsupported.of('a description that is not a string literal; COMMENT takes a constant')
				#['COMMENT ON TABLE ' + PgvectorSql.table(table) + ' IS ' + PgvectorSql.literal((operation.value as StringLit).value)]
			}
			case CONSISTENCY: newArrayList
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DYNAMIC: throw Unsupported.of('dynamic; ' + PENDING + ' (a jsonb column for undeclared fields)')
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
				#[PgvectorSchema.execute(#['DROP TABLE ' + exists + PgvectorSql.table(table)], table.database)]
			}
			Database: {
				val drop = PgvectorSchema.autocommit('DROP DATABASE ' + exists + PgvectorSql.id(concept.name) + ' WITH (FORCE)', null, concept.name)
				#[drop]
			}
			Tenant: dropSlice(member(concept, 'a tenant'), schema.table(member(concept, 'a tenant')).tenant, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), schema.table(member(concept, 'a namespace')).namespace, concept.key)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a field'), concept)
			Alias: #[PgvectorSchema.execute(#['DROP VIEW ' + exists + PgvectorSql.id(concept.name)], schema.table(concept.collection).database)]
			Partition: {
				val collection = member(concept, 'a partition')
				val table = schema.table(collection)
				if (table.partition === null) throw Unsupported.of('a partition of a collection declared without partitions')
				#[PgvectorSchema.execute(#['DROP TABLE ' + exists + PgvectorSql.partitionTable(table, concept.name)], table.database)]
			}
			VectorIndex: dropIndex(concept, exists)
			PayloadIndex: dropIndex(concept, exists)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; the quantized expression belongs to its vector index (drop the index)')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a PostgreSQL object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; the program drops databases and tables, not the server')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE_SERVER)
			Analyzer: #[PgvectorSchema.execute(#['DROP TEXT SEARCH CONFIGURATION ' + exists + 'public.' + PgvectorSql.id(concept.name)], analyzerDatabase(concept))]
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; a text search configuration keeps the dictionaries of its language (drop the analyzer)')
			FieldLimit: dropLimit(concept, exists)
			FileResource: throw Unsupported.of(NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_SERVER)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_SERVER)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('dropping authentication; PostgreSQL authentication methods are server configuration (pg_hba.conf)')
			RoleBinding: dropRole(concept)
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
		val sql = PgvectorSql.counted('DELETE FROM ' + PgvectorSql.table(table) + ' ' + PgvectorSql.ALIAS + ' WHERE '
			+ PgvectorSql.col(column) + ' = ' + q.bind(text(value), 'text'), 'delete_count')
		#[new Code.Return(call('value', sql, q, null, table.database))]
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val result = #[PgvectorSchema.execute(#[dropColumn(collection, table, field)], table.database)]
		schema.evolve(collection, table.without(ModelUtil.fieldName(field)))
		result
	}

	def private List<Stmt> dropIndex(Index index, String exists) {
		#[PgvectorSchema.execute(#['DROP INDEX ' + exists + PgvectorSql.id(indexName(index))], indexDatabase(index))]
	}

	def private String indexName(Index index) {
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		PgvectorSchema.indexName(collection, index.name)
	}

	def private String indexDatabase(Index index) {
		val collection = indexCollection(index)
		if (collection === null) null else PgvectorSchema.databaseOf(collection)
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

	def private List<Stmt> dropLimit(FieldLimit limit, String exists) {
		val field = EcoreUtil2.getContainerOfType(limit, Field)
		if (field === null) throw Unsupported.of('a field limit outside a field declaration')
		val table = schema.table(member(field, 'a field limit'))
		#[PgvectorSchema.execute(#['ALTER TABLE ' + PgvectorSql.table(table) + ' DROP CONSTRAINT ' + exists + PgvectorSql.id(limit.name)], table.database)]
	}

	/** DROP OWNED revokes the role's privileges in the database; then the role is dropped. */
	def private List<Stmt> dropRole(RoleBinding binding) {
		val name = binding.roleName
		PgvectorSql.id(name)
		val database = if (binding.collection === null) null else schema.table(binding.collection).database
		val drop = PgvectorSchema.execute(#['DROP OWNED BY ' + PgvectorSql.id(name), 'DROP ROLE ' + PgvectorSql.id(name)], database)
		#[new Code.When(roleExists(name, database), false, #[drop])]
	}

	/** An alias is a view: the swap replaces its query in one transaction. */
	def dispatch List<Stmt> body(SwapAlias it) {
		val table = schema.table(newCollection)
		val old = schema.table(oldCollection)
		if (table.database != old.database) throw Unsupported.of('swapping an alias across databases; a view reads tables of its own database')
		#[PgvectorSchema.execute(#['DROP VIEW IF EXISTS ' + PgvectorSql.id(alias.name), PgvectorSchema.createView(alias.name, table, false)], table.database)]
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

	/** One INSERT of the rows (as a json array parameter) with the slice values of the statement. */
	def private Code.Call write(VectorCollection collection, RecordSource source, String policy, String counter, EObject statement) {
		val table = schema.table(collection)
		val q = new Query
		val sql = PgvectorSql.insertRows(q, table, rows(source, table), slices(statement, collection, table), policy, counter)
		call('value', sql, q, null, table.database)
	}

	/** Column values for tenant, namespace and partition of a write; partitioned tables default to the default partition. */
	def private Map<String, Expr> slices(EObject statement, VectorCollection collection, Table table) {
		val Map<String, Expr> result = newLinkedHashMap
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.put(table.tenant, text(tenant.name))
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.put(table.namespace, text(namespace))
		val partition = feature(statement, 'partition') as Partition
		if (partition !== null && table.partition === null) throw Unsupported.of('a partition of a collection declared without partitions')
		if (table.partition !== null) result.put(table.partition, text(partition?.name ?: PgvectorSchema.defaultPartition(collection) ?: PgvectorSchema.DEFAULT_PARTITION))
		result
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; PostgreSQL rows carry no version number (xmin is the id of the inserting transaction), and the generator adds no version column')
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
			if (!ids.empty) restrictions += PgvectorSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
			restrictions += PgvectorSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
			val fromRecords = PgvectorSql.updateRows(q, table, records, written(it, 'mode') && mode == EUpdateMode.REPLACE, PgvectorSql.and(restrictions))
			return #[new Code.Return(call('value', PgvectorSql.counted(fromRecords, 'update_count'), q, null, table.database)) as Stmt]
		}
		val List<String> sets = newArrayList
		val List<String> given = newArrayList
		if (values !== null) {
			for (entry : values.entries) {
				if (entry.field === collection.primaryKey) throw Unsupported.of('updating the primary key; the key identifies the row')
				val column = table.column(ModelUtil.fieldName(entry.field))
				val name = column.name
				given += name
				val value = PgvectorSql.value(q, column, convert(entry.value, column))
				if (appendFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += PgvectorSql.id(name) + ' = coalesce(' + PgvectorSql.id(name) + ', \'{}\') || ' + value
				} else if (arrayRemoveFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += PgvectorSql.id(name) + ' = ARRAY(SELECT x FROM unnest(' + PgvectorSql.id(name) + ') WITH ORDINALITY AS u(x, i) WHERE x <> ALL(' + value + ') ORDER BY i)'
				} else {
					sets += PgvectorSql.id(name) + ' = ' + value
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
			sets += PgvectorSql.id(column.name) + ' = NULL'
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			for (column : table.columns) {
				if (column.name != table.key && column.vec === null && !given.contains(column.name) && !removedFields.exists[f | table.column(ModelUtil.fieldName(f)).name == column.name]) {
					sets += PgvectorSql.id(column.name) + ' = DEFAULT'
				}
			}
		}
		if (sets.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += PgvectorSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += PgvectorSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
		val update = 'UPDATE ' + PgvectorSql.table(table) + ' ' + PgvectorSql.ALIAS + ' SET ' + sets.join(', ') + PgvectorSql.whereClause(PgvectorSql.and(conditions))
		#[new Code.Return(call('value', PgvectorSql.counted(update, 'update_count'), q, null, table.database))]
	}

	def private void arrayField(Field field, Column column) {
		if (!column.array) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ', which is not an array field')
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val table = schema.table(collection)
		val q = new Query
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += PgvectorSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += PgvectorSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
		val condition = PgvectorSql.and(conditions)
		if (condition === null && !deleteAll) throw Unsupported.of("a delete without ids, filter or 'all'")
		val target = if (written(it, 'deleteLimit')) {
			'(' + PgvectorSql.ALIAS + '.tableoid, ' + PgvectorSql.ALIAS + '.ctid) IN (SELECT ' + PgvectorSql.ALIAS + '.tableoid, ' + PgvectorSql.ALIAS
				+ '.ctid FROM ' + PgvectorSql.table(table) + ' ' + PgvectorSql.ALIAS + PgvectorSql.whereClause(condition) + ' ORDER BY '
				+ PgvectorSql.col(table.key) + ' LIMIT ' + deleteLimit + ')'
		} else condition
		val delete = 'DELETE FROM ' + PgvectorSql.table(table) + ' ' + PgvectorSql.ALIAS + PgvectorSql.whereClause(target)
		#[new Code.Return(call('value', PgvectorSql.counted(delete, 'delete_count'), q, null, table.database))]
	}

	/**
	 * The runtime reads a local JSON or JSON Lines file and writes it in batches with one INSERT statement, all in one
	 * transaction. COPY FROM STDIN is not used: each driver streams it differently (psycopg copy, pg-copy-streams, CopyManager).
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; PostgreSQL has no ingestion pipeline (logical replication subscribes to another PostgreSQL server)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; PostgreSQL reads object stores only through extensions the pgvector image does not include')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (COPY FROM STDIN with FORMAT csv)')
			throw Unsupported.of('import format ' + format.literal + '; PostgreSQL reads text, CSV and binary COPY data, not ' + format.literal)
		}
		schema.note(it, 'parallelism', 'async')
		val table = schema.table(collection)
		val q = new Query
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val sql = PgvectorSql.insertRows(q, table, new Code.Nil, slices(it, collection, table), policy,
			if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count')
		val extra = q.params.items.subList(1, q.params.items.size)
		val args = kw('sql', text(sql), 'location', text(location))
		if (!extra.empty) args.put('params', items(extra))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		if (table.database !== null) args.put('database', text(table.database))
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[new Code.Return(runtime('import_file', args))]
	}

	/** The statements of a vecDSL transaction in one PostgreSQL transaction at the given isolation level. */
	def dispatch List<Stmt> body(Transaction it) {
		val List<Expr> steps = newArrayList
		var String database = null
		var first = true
		for (statement : it.methods) {
			if (statement instanceof BulkImport) throw Unsupported.of('bulk-import inside a transaction; ' + PENDING + ' (the import as one step of the transaction)')
			val inner = body(statement)
			if (inner.size != 1 || !(inner.head instanceof Code.Return)) throw Unsupported.of('this statement inside a transaction')
			val invocation = (inner.head as Code.Return).value as Code.Call
			val stepDatabase = (invocation.args.get('database') as Code.Text)?.value
			if (!first && stepDatabase != database) throw Unsupported.of('a transaction across databases; a PostgreSQL transaction runs in one database')
			database = stepDatabase
			first = false
			val step = kw('sql', invocation.args.get('sql'))
			if (invocation.args.has('params')) step.put('params', invocation.args.get('params'))
			steps += step
		}
		val args = kw('steps', items(steps))
		if (written(it, 'isolationLevel')) {
			args.put('isolation', text(if (isolationLevel.literal == 'snapshot') 'repeatable_read' else isolationLevel.literal))
			if (isolationLevel.literal == 'read_uncommitted') notes += 'read_uncommitted on Transaction, which PostgreSQL runs as read committed'
		}
		if (database !== null) args.put('database', text(database))
		#[new Code.Return(runtime('transaction', args))]
	}

	/** A committed write is durable and visible to the next statement, so 'wait' needs no call; extensions are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (name : ModelUtil.extParams(statement, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** A statement's filter with its tenant, namespace and partition conditions. */
	def private PgvectorFilter filterOf(EObject statement, VectorCollection collection, Table table, FilterBlock block, boolean write) {
		val result = new PgvectorFilter().where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.slice(table.tenant, #[text(tenant.name)])
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result.slice(table.namespace, #[text(namespace as String)])
		val single = feature(statement, 'partition') as Partition
		val partitions = feature(statement, 'partitions') as List<Partition>
		val List<Partition> named = newArrayList
		if (single !== null) named += single
		if (partitions !== null) named += partitions
		if (!named.empty) {
			if (table.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
			result.slice(table.partition, named.map[p | text(p.name) as Expr].toList)
		}
		result
	}

	// ---------------------------------------------------------------- operational

	/** pg_prewarm (contrib, in the image) reads the table, its partitions and their indexes into shared buffers. */
	def dispatch List<Stmt> body(Load it) {
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(collection)
		val List<String> relations = if (partitions.empty) #[PgvectorSql.id(table.name)] else partitions.map[p | PgvectorSql.partitionTable(table, p.name)].toList
		if (!partitions.empty && table.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
		val q = new Query
		val roots = 'SELECT relid FROM jsonb_array_elements_text(' + q.bind(texts(relations), 'jsonb') + ') AS r(name), pg_partition_tree(to_regclass(r.name))'
		val sql = 'SELECT to_json(coalesce(sum(pg_prewarm(c.oid)), 0)) FROM pg_class c WHERE c.relkind IN (\'r\', \'i\') AND (c.oid IN (' + roots
			+ ') OR c.oid IN (SELECT indexrelid FROM pg_index WHERE indrelid IN (' + roots + ')))'
		#[PgvectorSchema.execute(#['CREATE EXTENSION IF NOT EXISTS pg_prewarm'], table.database), new Code.Return(call('value', sql, q, null, table.database))]
	}

	/** pg_buffercache_evict_relation (PostgreSQL 18 contrib, superuser) evicts the table's and its indexes' buffers. */
	def dispatch List<Stmt> body(Release it) {
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(collection)
		if (!partitions.empty && table.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
		val List<String> relations = if (partitions.empty) #[PgvectorSql.id(table.name)] else partitions.map[p | PgvectorSql.partitionTable(table, p.name)].toList
		val q = new Query
		val roots = 'SELECT relid FROM jsonb_array_elements_text(' + q.bind(texts(relations), 'jsonb') + ') AS r(name), pg_partition_tree(to_regclass(r.name))'
		val sql = 'SELECT to_json(count(*)) FROM pg_class c, LATERAL pg_buffercache_evict_relation(c.oid) WHERE c.relkind IN (\'r\', \'i\') AND (c.oid IN ('
			+ roots + ') OR c.oid IN (SELECT indexrelid FROM pg_index WHERE indrelid IN (' + roots + ')))'
		#[PgvectorSchema.execute(#['CREATE EXTENSION IF NOT EXISTS pg_buffercache'], table.database), new Code.Return(call('value', sql, q, null, table.database))]
	}

	/** A commit is durable once its WAL record is flushed (synchronous_commit on), so a flush only checks the table. */
	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		require(collection)
	}

	/** VACUUM (ANALYZE) reclaims dead rows and refreshes statistics; it cannot run inside a transaction block. VACUUM FULL is not used: it locks the table exclusively. */
	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'targetSegments', 'async')
		val table = schema.table(collection)
		#[PgvectorSchema.autocommit('VACUUM (ANALYZE) ' + PgvectorSql.table(table), table.database, null)]
	}

	def dispatch List<Stmt> body(Describe it) {
		val table = schema.table(collection)
		val q = new Query
		#[new Code.Return(call('value', PgvectorQueries.describe(q, table, includeStats, includeIndexProgress), q, null, table.database))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + NO_MODELS)
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

	/** A new table LIKE the source (columns, defaults, constraints, indexes, comments) filled from it, in one transaction. */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; a vecDSL database block is the server connection (CREATE DATABASE ... TEMPLATE copies one database)')
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; PostgreSQL has no copy-on-write tables')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(sourceCollection)
		val target = PgvectorSql.id(targetName)
		if (table.partition !== null) notes += 'the partitions of ' + table.name + ', which the clone holds in one table'
		val steps = #[kw('sql', text('CREATE TABLE ' + target + ' (LIKE ' + PgvectorSql.table(table) + ' INCLUDING ALL)')) as Expr,
			kw('sql', text(PgvectorSql.counted('INSERT INTO ' + target + ' SELECT * FROM ' + PgvectorSql.table(table), 'copied'))) as Expr]
		val args = kw('steps', items(steps))
		if (table.database !== null) args.put('database', text(table.database))
		#[new Code.Return(runtime('transaction', args))]
	}

	/** REINDEX rebuilds the index; concurrently it runs outside a transaction block. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (name : ModelUtil.extParams(it, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val database = indexDatabase(index)
		val sql = 'REINDEX INDEX ' + (if (concurrently) 'CONCURRENTLY ' else '') + PgvectorSql.id(indexName(index))
		#[if (concurrently) PgvectorSchema.autocommit(sql, database, null) else PgvectorSchema.execute(#[sql], database)]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val table = schema.table(collection)
		if (namespace !== null) return dropSlice(collection, table.namespace, namespace)
		#[PgvectorSchema.execute(#['TRUNCATE TABLE ' + PgvectorSql.table(table)], table.database)]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val q = new Query
		val owner = scope
		var String database = null
		val source = switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) {
					database = (owner as Database).name
				} else if (owner !== null && !(owner instanceof VectorDatabase)) {
					throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; PostgreSQL lists the tables of a database')
				}
				PgvectorQueries.collections()
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				PgvectorQueries.databases()
			}
			case INDEXES: {
				val table = scopeTable(owner, 'indexes')
				database = table.database
				PgvectorQueries.indexes(q, table, (owner as VectorCollection).name)
			}
			case PARTITIONS: {
				val table = scopeTable(owner, 'partitions')
				database = table.database
				PgvectorQueries.partitions(q, table)
			}
			case ALIASES: {
				val table = if (owner === null) null else scopeTable(owner, 'aliases')
				database = table?.database
				PgvectorQueries.aliases(q, table)
			}
			case TENANTS,
			case NAMESPACES: {
				val table = scopeTable(owner, kind.literal)
				database = table.database
				val column = if (kind.literal == 'tenants') table.tenant else table.namespace
				if (column === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				PgvectorQueries.slices(table, column)
			}
			case SNAPSHOTS: throw Unsupported.of(NO_SNAPSHOTS)
		}
		val sql = PgvectorQueries.names(q, source, if (prefix === null) null else text(prefix), if (written(it, 'listLimit')) listLimit else null)
		#[new Code.Return(call('value', sql, q, null, database))]
	}

	def private Table scopeTable(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.table(owner as VectorCollection)
	}

	/** Indexes are built synchronously (CREATE INDEX returns when the index is valid), so awaiting one reads its state once. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state', 'timeoutSeconds')
		val q = new Query
		#[new Code.Return(call('value', PgvectorQueries.indexState(q, indexName(index)), q, null, indexDatabase(index)))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'explain', 'reranker', 'queryModel')
		queryExtensions(it)
		val table = schema.table(collection)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val metric = if (written(it, 'metric')) it.metric.literal else column.vec.metric
		val q = new Query
		val query = queryVector(q, input, collection, table, column)
		val List<String> stages = newArrayList
		val staged = prefetchStages(it, q, collection, table, prefetches, input, field, stages)
		val where = PgvectorSql.and(#[PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts), PgvectorQueries.within(staged)])
		val shape = shapeOf(it, collection, table, field, #[])
		val options = settings(collection, field, params, extensionSettings(it), fetchOf(shape, topK + offsetOf(it)), where !== null || cursor !== null, exact)
		val prelude = if (stages.empty) null else stages.join(', ')
		val search = new Search(table, quantized(column, params), metric, query, prelude, topK, offsetOf(it), where, projection(collection, table, output, excludedOf(it), withVectors),
			written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds))
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', PgvectorQueries.vectorSearch(q, search, shape), q, options, table.database))]
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'explain', 'reranker', 'minShouldMatch', 'fuzziness', 'slop')
		queryExtensions(it)
		if (params !== null) schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy')
		val table = schema.table(collection)
		val q = new Query
		val fields = keywordFields(collection, table, searchFields)
		val words = keywordText(input)
		val op = keywordOperator(if (written(it, 'textQueryType')) textQueryType.literal else 'match', if (written(it, 'matchOperator')) matchOperator.literal else 'and')
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val keywords = new Keywords(table, fields, op, words, topK, offsetOf(it), where, projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds))
		val shape = shapeOf(it, collection, table, null, #[new Terms(op, words)])
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', PgvectorQueries.keywordSearch(q, keywords, shape), q, merged(timeout(params), extensionSettings(it)), table.database))]
	}

	/** Hybrid search fused in SQL: each leg ranks its candidates in a CTE, and the fusion function of vecDSL combines them. */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'explain', 'reranker')
		queryExtensions(it)
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val List<Terms> terms = newArrayList
		val table = schema.table(collection)
		val q = new Query
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val shared = input
		val defaultLimit = topK + offsetOf(it)
		val Map<String, String> options = newLinkedHashMap
		options.putAll(timeout(params))
		val List<Leg> parts = newArrayList
		var index = 0
		for (leg : legList(it)) {
			val weight = weights.get(index)
			val limit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else defaultLimit
			val legWhere = PgvectorSql.where(q, table, filterOf(it, collection, table, if (feature(leg, 'filter') === null) filter else combined(filter, feature(leg, 'filter') as FilterBlock), false).parts)
			for (name : ModelUtil.extParams(leg, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name + ' on a hybrid leg')
			switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (a score condition in the leg CTE)')
					val field = vectorFieldOf(collection, leg.vectorField)
					val column = table.column(ModelUtil.fieldName(field))
					val metric = if (written(leg, 'metric')) leg.metric.literal else column.vec.metric
					val query = queryVector(q, leg.input ?: shared, collection, table, column)
					options.putAll(settings(collection, field, leg.params, newLinkedHashMap, limit, legWhere !== null, leg.exact))
					parts += new Leg(column, metric, query, null, null, null, limit, legWhere, weight)
				}
				KeywordLeg: {
					if (written(leg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
					val fields = keywordFields(collection, table, leg.searchFields)
					val op = keywordOperator(if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match', if (written(leg, 'matchOperator')) leg.matchOperator.literal else 'and')
					val legText = keywordText(leg.input ?: shared)
					terms += new Terms(op, legText)
					parts += new Leg(null, null, null, fields, op, legText, limit, legWhere, weight)
				}
			}
			index = index + 1
		}
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val hybrid = new Hybrid(table, parts, fusionKind, k, null, topK, offsetOf(it), projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds))
		options.putAll(extensionSettings(it))
		val shape = shapeOf(it, collection, table, null, terms)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', PgvectorQueries.hybridSearch(q, hybrid, shape), q, options, table.database))]
	}

	def private List<EObject> legList(HybridSearch search) {
		val List<EObject> result = newArrayList
		result += search.legs
		result
	}

	/** A leg filter joins the statement's filter: both must hold. */
	def private FilterBlock combined(FilterBlock outer, FilterBlock inner) {
		if (outer === null) return inner
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

	/** The average-vector strategy over example records or vectors, computed in SQL; the examples are not returned. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'explain', 'reranker')
		queryExtensions(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val table = schema.table(collection)
		val lookup = schema.table(lookupCollection ?: collection)
		if (lookup.database != table.database) throw Unsupported.of('examples looked up in another database')
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val lookupColumn = lookup.column(column.name)
		val q = new Query
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) {
			if (isVector(example)) positiveVectors += convert(example, column) else positiveIds += Literals.of(example)
		}
		for (example : negatives) {
			if (isVector(example)) negativeVectors += convert(example, column) else negativeIds += Literals.of(example)
		}
		val prelude = PgvectorQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors)
		val List<Cond> conditions = newArrayList
		conditions += filterOf(it, collection, table, filter, false).parts
		val List<String> parts = newArrayList
		parts += PgvectorSql.where(q, table, conditions)
		if (lookupCollection === null || lookupCollection === collection) {
			val List<Expr> examples = newArrayList
			examples += positiveIds
			examples += negativeIds
			if (!examples.empty) parts += '(' + PgvectorSql.keyIn(q, table, items(examples)) + ') IS NOT TRUE'
		}
		val where = PgvectorSql.and(parts)
		val shape = shapeOf(it, collection, table, field, #[])
		val options = settings(collection, field, params, extensionSettings(it), fetchOf(shape, topK + offsetOf(it)), where !== null || cursor !== null, false)
		val search = new Search(table, column, column.vec.metric, '(SELECT v FROM q)', prelude, topK, offsetOf(it), where,
			projection(collection, table, output, excludedOf(it), withVectors), written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds))
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', PgvectorQueries.vectorSearch(q, search, shape), q, options, table.database))]
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
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, null, false).parts)
		#[new Code.Return(call('query', PgvectorQueries.getRows(q, table, items(ids.map[Literals.of(it)]), names, where), q, extensionSettings(it), table.database))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		#[new Code.Return(call('value', PgvectorQueries.count(table, where), q, extensionSettings(it), table.database))]
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
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val List<Order> order = newArrayList
		for (clause : sortClauses) {
			if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			order += new Order(fieldRefOf(collection, table, clause.field, 'sort'), clause.direction == ESortDirection.DESC)
		}
		val limit = if (written(it, 'scrollLimit')) scrollLimit else null
		if (cursor === null && !written(it, 'sliceTotal')) {
			val terms = order.map[o | o.field.sql(PgvectorSql.ALIAS) + (if (o.descending) ' DESC NULLS LAST' else ' ASC NULLS LAST')].toList
			return #[new Code.Return(call('query', PgvectorQueries.scroll(table, names, where, terms, limit, offsetOf(it)), q, extensionSettings(it), table.database)) as Stmt]
		}
		if (written(it, 'sliceTotal') && (sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal)) throw Unsupported.of('a slice index outside 0 to the slice count minus 1')
		val cursorKey = if (cursor === null) null else text(cursor)
		val slices = if (written(it, 'sliceTotal')) sliceTotal else null
		#[new Code.Return(call('query', PgvectorQueries.scroll(q, table, names, where, order, limit, offsetOf(it), cursorKey, sliceIndex, slices), q,
			extensionSettings(it), table.database))]
	}

	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val q = new Query
		val shape = projection(collection, table, output, newArrayList, false)
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val first = written(it, 'method') && method == ESampleMethod.FIRST
		#[new Code.Return(call('query', PgvectorQueries.sample(table, shape.output, where, sampleSize, first), q, extensionSettings(it), table.database))]
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val table = schema.table(collection)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val q = new Query
		val where = PgvectorSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val sample = if (written(it, 'sampleSize')) sampleSize else 10
		val limit = if (written(it, 'matrixLimit')) matrixLimit else 3
		#[new Code.Return(call('query', PgvectorQueries.distanceMatrix(table, column, column.vec.metric, where, sample, limit), q, extensionSettings(it), table.database))]
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

	/** The column a search orders by, with the oversampling of the search parameters for a quantized index. */
	def private Column quantized(Column column, SearchParams params) {
		if (params === null || !written(params, 'oversampling') || column.vec.quantization === null) return column
		new Column(column.name, column.type, null, false, new PgvectorSql.Vec(column.vec.type, column.vec.dim, column.vec.metric,
			column.vec.quantization, params.oversampling), null, column.nullable, null, #[])
	}

	/** Output fields (the key is always returned) and the vector fields returned with 'with-vectors' or named in the output. */
	def private Projection projection(VectorCollection collection, Table table, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val listed = if (output === null || output.allFields) collection.fields as List<? extends Field> else output.fields
		for (field : listed) {
			if (field !== collection.primaryKey && !excluded.contains(field)) {
				if (field instanceof VectorField) {
					if (output !== null && !output.allFields) vectors += table.column(ModelUtil.fieldName(field)).name
				} else {
					fields += table.column(ModelUtil.fieldName(field)).name
				}
			}
		}
		if (withVectors) {
			for (field : ModelUtil.vectorFields(collection)) {
				val name = table.column(ModelUtil.fieldName(field)).name
				if (!vectors.contains(name) && !excluded.contains(field)) vectors += name
			}
		}
		new Projection(fields, vectors)
	}

	/** Search settings applied with SET LOCAL: candidate lists of at least limit + offset, iterative scans when filtered. */
	def private Map<String, String> settings(VectorCollection collection, VectorField field, SearchParams params, Map<String, String> fromExtension,
		int fetch, boolean filtered, boolean exact) {
		val Map<String, String> result = newLinkedHashMap
		val index = schema.indexOf(collection, field)
		val method = if (index === null || index.family.literal == 'flat') null else if (index.family.literal == 'ivf') 'ivfflat' else 'hnsw'
		if (params !== null) {
			if (written(params, 'targetRecall')) throw Unsupported.of('target-recall: hnsw.ef_search, ivfflat.probes and iterative scans are effort settings, with no declarable recall target')
			for (name : ModelUtil.extParams(params, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		}
		val exhaustive = exact || (params !== null && (params.exhaustive || (written(params, 'filterStrategy') && params.filterStrategy.literal == 'pre_filter')))
		if (exhaustive) {
			result.put('enable_indexscan', 'off')
		} else if (method == 'hnsw') {
			val candidates = if (params !== null && written(params, 'candidatePool')) params.candidatePool else if (written(index, 'searchCandidates')) index.searchCandidates else 40
			result.put('hnsw.ef_search', String.valueOf(Math.min(1000, Math.max(candidates, fetch))))
			val strategy = if (params !== null && written(params, 'filterStrategy')) params.filterStrategy.literal else 'auto'
			if (strategy == 'iterative' || (filtered && strategy == 'auto')) result.put('hnsw.iterative_scan', 'strict_order')
			if (params !== null && written(params, 'scanBudget')) result.put('hnsw.max_scan_tuples', String.valueOf(params.scanBudget))
			if (params !== null && written(params, 'probes')) throw Unsupported.of('probes on an hnsw index; probes belong to ivf indexes')
		} else if (method == 'ivfflat') {
			if (params !== null && written(params, 'probes')) result.put('ivfflat.probes', String.valueOf(params.probes))
			val strategy = if (params !== null && written(params, 'filterStrategy')) params.filterStrategy.literal else 'auto'
			if (strategy == 'iterative' || (filtered && strategy == 'auto')) result.put('ivfflat.iterative_scan', 'relaxed_order')
			if (params !== null && written(params, 'scanBudget')) result.put('ivfflat.max_probes', String.valueOf(params.scanBudget))
			if (params !== null && written(params, 'candidatePool')) throw Unsupported.of('candidates on an ivf index; candidate lists belong to hnsw indexes')
		} else if (params !== null) {
			schema.note(params, 'candidatePool', 'probes', 'scanBudget', 'filterStrategy')
		}
		if (params !== null) {
			result.putAll(timeout(params))
			if (field !== null && params.rescore && schema.indexOf(collection, field)?.quantization === null) notes += 'rescore on SearchParams, without a quantized index'
		}
		result.putAll(fromExtension)
		result
	}

	def private Map<String, String> merged(Map<String, String> first, Map<String, String> second) {
		val Map<String, String> result = newLinkedHashMap
		result.putAll(first)
		result.putAll(second)
		result
	}

	def private Map<String, String> timeout(SearchParams params) {
		val Map<String, String> result = newLinkedHashMap
		if (params !== null && written(params, 'timeoutMillis')) result.put('statement_timeout', String.valueOf(params.timeoutMillis))
		result
	}

	/** pgvector query settings written in an 'extension pgvector' block, by their setting names. */
	def private Map<String, String> extensionSettings(EObject host) {
		val Map<String, String> result = newLinkedHashMap
		for (entry : ModelUtil.extParams(host, PgvectorGenerator.VENDOR).entrySet) {
			if (!SETTINGS.contains(entry.key)) throw Unsupported.of('extension parameter ' + entry.key + '; pgvector query settings are ' + SETTINGS.join(', '))
			val value = entry.value
			val setting = switch value {
				StringLit: value.value
				NumberLit: PgvectorSql.number(value.value)
				default: throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a string or number literal')
			}
			result.put(entry.key, setting)
		}
		result
	}

	/** Rejects clauses PostgreSQL lacks, with their reason, and clauses not generated yet. */
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

	/** pgvector extension blocks of queries carry settings only; extension items of other kinds are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == PgvectorGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on pgvector')
				}
			}
		}
		extensionSettings(host)
	}

	/** The query vector as SQL: a bound vector, or the stored vector of a record (a scalar subquery). */
	def private String queryVector(Query q, QueryInput input, VectorCollection collection, Table table, Column column) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR,
			case SPARSE_VECTOR: {
				if ((input.kind == EQueryInputMode.SPARSE_VECTOR) != (column.vec.type == 'sparsevec')) {
					throw Unsupported.of(input.kind.literal + ' input on ' + column.vec.type + ' field ' + column.name)
				}
				q.bind(convert(input.value, column), column.vec.sql)
			}
			case ID: {
				val source = schema.table(input.lookupCollection ?: collection)
				if (source.database != table.database) throw Unsupported.of('a query vector looked up in another database')
				val stored = source.column(column.name)
				'(SELECT x.' + PgvectorSql.id(stored.name) + ' FROM ' + PgvectorSql.table(source) + ' x WHERE x.' + PgvectorSql.id(source.key) + ' = '
					+ q.bind(Literals.of(input.value), source.keyColumn.paramType) + ' LIMIT 1)'
			}
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; pgvector compares one vector per column')
			default: throw Unsupported.of(input.kind.literal + ' input' + NO_MODELS)
		}
	}

	/**
	 * The result shape of a search: the keyset cursor, boosts, MMR, groups, total, aggregations, highlights of the text inputs and
	 * sort keys. The searched vector field is null for keyword and hybrid searches; terms are empty for searches without text.
	 */
	def private Shape shapeOf(EObject search, VectorCollection collection, Table table, VectorField searched, List<Terms> terms) {
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
		val List<Column> marked = newArrayList
		for (highlighted : (feature(search, 'highlighted') ?: newArrayList) as List<Field>) {
			if (terms.empty) throw Unsupported.of("the 'highlight' clause on a search without text; ts_headline marks the terms of a text query")
			val markedColumn = table.column(ModelUtil.fieldName(highlighted))
			if (markedColumn.config === null) throw Unsupported.of('highlighting ' + markedColumn.name + ', which is not a full-text field')
			marked += markedColumn
		}
		val List<Order> order = newArrayList
		for (clause : sortClauses) {
			order += new Order(if (clause.byRelevance) null else fieldRefOf(collection, table, clause.field, 'sort'), clause.direction == ESortDirection.DESC)
		}
		val cursorKey = if (cursorText === null) null else text(cursorText)
		new Shape(cursorKey, if (boosting === null) null else boostOf(collection, table, boosting),
			if (diversity === null) null else mmrOf(collection, table, diversity, searched, (feature(search, 'topK') as Integer) + offsetOf(search)),
			group, written(search, 'withTotal'), aggregates, marked, terms, order)
	}

	/**
	 * Prefetch stages as CTEs of the rows each stage retrieves (its own input, vector field, metric, filter and limit, ranking only
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
			val stageWhere = PgvectorSql.where(q, table, filterOf(search, collection, table, stage.filter, false).parts)
			val name = 'stage_' + stages.size
			stages += PgvectorQueries.stage(name, table, stageColumn, stageMetric, stageQuery, PgvectorSql.and(#[stageWhere, PgvectorQueries.within(inner)]), stage.stageLimit)
			names += name
		}
		names
	}

	/** Candidates the index must deliver: the MMR pool when there is one, else limit plus offset. */
	def private int fetchOf(Shape shape, int fetch) {
		if (shape.mmr === null) fetch else Math.max(shape.mmr.candidates, fetch)
	}

	/** A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a jsonb column. */
	def private Ref fieldRefOf(VectorCollection collection, Table table, FieldPath path, String purpose) {
		if (path.rawPath !== null) throw Unsupported.of('raw paths in ' + purpose + '; the generator reads declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of(purpose + ' on vector field ' + ModelUtil.fieldName(path.field))
		val column = table.column(ModelUtil.fieldName(path.field))
		if (path.subFields.empty) return new Ref(column, #[], null)
		new Ref(column, path.subFields.map[sub | ModelUtil.fieldName(sub)].toList, PgvectorFilter.leafType(path))
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
			if (fn.condition !== null) when += new PgvectorFilter().condition(fn.condition)
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

	/** Keyword fields: the listed full-text columns with boosts, else every full-text column of the collection. */
	def private List<KeywordField> keywordFields(VectorCollection collection, Table table, List<SearchFieldRef> refs) {
		val List<KeywordField> result = newArrayList
		if (refs.empty) {
			for (column : table.columns) if (column.config !== null) result += new KeywordField(column, 1.0)
		} else {
			for (ref : refs) {
				val column = table.column(ModelUtil.fieldName(ref.field))
				if (column.config === null) throw Unsupported.of('keyword search on ' + column.name + ', which is not a full-text field')
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

	/** The tsquery form of a text query type: match (all terms with and, any term with or) and phrase. */
	def private String keywordOperator(String type, String operator) {
		switch type {
			case 'match': if (operator == 'or') 'match_any_term' else 'match'
			case 'phrase': 'phrase'
			case 'prefix',
			case 'phrase_prefix',
			case 'term',
			case 'query_string': throw Unsupported.of('text-query ' + type + '; ' + PENDING + ' (to_tsquery prefixes, simple lexemes and websearch_to_tsquery)')
			default: throw Unsupported.of('text-query ' + type + '; PostgreSQL full-text search matches normalized lexemes, without fuzzy, wildcard or regular-expression terms')
		}
	}

	def private Expr rows(RecordSource source, Table table) {
		switch source {
			FileSource: runtime('read_records', kw('location', text(source.location)))
			InlineRecords: items(source.records.map[item | record(item, table)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record, Table table) {
		val result = entries
		for (entry : record.entries) {
			val column = table.column(ModelUtil.fieldName(entry.field))
			result.put(column.name, convert(entry.value, column))
		}
		result
	}

	/**
	 * A literal in the form PostgreSQL reads for its column: sparse vectors as {index:value}/dim with indexes from 1 (vecDSL
	 * counts from 0, as Milvus, Qdrant and Pinecone), bit vectors as a bit string; everything else as written.
	 */
	def private Expr convert(Literal literal, Column column) {
		if (column.vec === null) return Literals.of(literal)
		switch column.vec.type {
			case 'sparsevec': {
				if (literal instanceof SparseLit) {
					val weights = (literal as SparseLit).entries.map[e | String.valueOf(e.index + 1) + ':' + PgvectorSql.number(e.weight)].join(',')
					if (column.vec.dim <= 0) throw Unsupported.of('a sparse vector field without dim; sparsevec text names its dimension')
					return text('{' + weights + '}/' + column.vec.dim)
				}
			}
			case 'bit': {
				if (literal instanceof ListLit) {
					val bits = (literal as ListLit).items.map[item | if (item instanceof NumberLit && (item as NumberLit).value != 0) '1' else '0'].join
					return text(bits)
				}
			}
		}
		Literals.of(literal)
	}
}
