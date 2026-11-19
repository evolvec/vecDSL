package org.ftn.vecDSL.generator.oracledb

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
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Aggregate
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Boost
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Fetch
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.BoostFn
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Group
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Hybrid
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.KeywordField
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Keywords
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Leg
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Mmr
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Order
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Projection
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Ref
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Search
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Shape
import org.ftn.vecDSL.generator.oracledb.OracledbQueries.Terms
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Column
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Cond
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Flashback
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Query
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Table
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
import org.ftn.vecDSL.vecDSL.EParamType
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
import static org.ftn.vecDSL.generator.oracledb.OracledbSchema.call
import static org.ftn.vecDSL.generator.oracledb.OracledbSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to SQL for Oracle AI Database 26ai in the host-language-neutral code model. The SQL text is built
 * here (through OracledbSql and OracledbQueries) and passed to the runtime with its bind values, so the three host languages
 * send identical statements: query and value return JSON rows, modify returns the affected row count, execute runs DDL and
 * PL/SQL blocks (each DDL statement commits), transaction runs a vecDSL transaction.
 */
class OracledbStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; OracledbCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Oracle: '
	/** Suffix of constructs Oracle supports that the generator does not produce yet. */
	public static val PENDING = OracledbSql.PENDING
	public static val NO_MODELS = '; generated Oracle programs load no ONNX embedding model into the database (VECTOR_EMBEDDING and hybrid vector indexes need one), so they supply their own vectors'
	public static val EXTERNAL = 'collections derived from external sources; an Oracle external table reads its files on the database server through a directory object, which only a user with the CREATE ANY DIRECTORY privilege creates, so a generated program can neither create the directory nor place the files'
	public static val NO_TTL = 'ttl; Oracle rows do not expire (a DBMS_SCHEDULER job deleting old rows needs CREATE JOB and is not generated)'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; Oracle has no per-table drop guard for the owner of a table'
	public static val NO_STORAGE = 'storage settings; generated programs create tables in the user\'s default tablespace (tablespaces are created by a DBA)'
	public static val NO_TAGS = 'collection tags; an Oracle table carries one comment (the description) and no tags'
	public static val NO_BM25 = 'BM25 parameters; Oracle Text scores with its own algorithm (SCORE from 0 to 100) and takes no k1 or b'
	public static val NO_SNAPSHOTS = 'snapshots; Oracle backs up with RMAN and Data Pump, outside a SQL session (restore points need SYSDBA or the SELECT ANY DICTIONARY and CREATE ANY RESTORE POINT privileges and flashback logging)'
	public static val SINGLE_SERVER = 'one Oracle AI Database Free instance holds the data; the Free edition has no Real Application Clusters and no Data Guard (Licensing Information, Free column)'
	static val NO_GENERATION = 'generated Oracle programs call no generative model (DBMS_VECTOR_CHAIN.UTL_TO_GENERATE_TEXT needs a third-party provider and credentials)'
	static val NO_RERANKERS = 'Oracle has no reranking model in the database for generated programs'
	static val NO_METRICS = 'metrics configuration; Oracle statistics and diagnostics are instance settings and V$ views, not objects a generated program creates'
	static val NO_SCHEDULES = 'backup schedules; Oracle backs up with RMAN, outside a SQL session'
	static val NO_CLUSTER_LIMITS = 'cluster limits; Oracle has no setting that limits the number of tables of a user (quotas limit space)'
	static val NO_FILES = 'file resources; Oracle Text thesauri and dictionaries are loaded with ctxload from the server\'s file system'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'boost' -> 'boost', 'queryModel' -> 'model', 'minShouldMatch' -> 'min-should-match', 'fuzziness' -> 'fuzziness',
		'slop' -> 'slop', 'sliceIndex' -> 'slice'
	}

	/** Why Oracle cannot express a clause. */
	static val REASONS = #{
		'explain' -> 'Oracle explains query plans (EXPLAIN PLAN), not scores',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> 'generated Oracle programs embed no query text' + NO_MODELS,
		'minShouldMatch' -> 'an Oracle Text query combines terms with AND, OR and ACCUM and has no minimum number of matching terms'
	}

	/** Clauses Oracle supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'JSON_EXISTS with a filter on array elements'
	}

	static val PRIVILEGES = #{'read' -> 'SELECT', 'write' -> 'INSERT, UPDATE, DELETE', 'admin' -> 'ALL', 'all' -> 'ALL',
		'select' -> 'SELECT', 'insert' -> 'INSERT', 'update' -> 'UPDATE', 'delete' -> 'DELETE', 'references' -> 'REFERENCES',
		'alter' -> 'ALTER', 'index' -> 'INDEX'}

	val List<String> notes = newArrayList
	val OracledbSchema schema
	val Script script
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script) {
		this.script = script
		this.schema = new OracledbSchema(notes, script)
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
			case 'bm25': 'Oracle Text indexes text columns directly (CONTEXT indexes) and has no BM25 function producing sparse vectors'
			case 'custom': PENDING + ' (virtual columns)'
			default: 'generated Oracle programs load no ONNX model into the database, which model functions need'
		}
	}

	def static String collectionLimit(String type) {
		'collection limits (' + type + '); Oracle has fixed limits (1000 columns per table, 65535 vector dimensions) and no per-table limit settings'
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
				for (name : ModelUtil.extParams(concept, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Database: {
				OracledbSchema.physical(concept.name, 'x')
				if (concept.name.contains(OracledbSchema.DATABASE_SEPARATOR)) throw Unsupported.of('the logical database name ' + concept.name + '; the generator separates database and collection names with ' + OracledbSchema.DATABASE_SEPARATOR)
				notes += 'logical-db ' + concept.name + ', a table-name prefix that exists through its collections'
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, fresh)
			}
			Alias: {
				val table = schema.table(concept.collection)
				result += OracledbSchema.execute(#[OracledbSchema.createView(concept.name, table)])
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
			Analyzer: result += schema.createAnalyzer(concept)
			AnalyzerFilter: result += analyzerFilter(concept)
			FieldLimit: result += addLimit(concept)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: throw Unsupported.of('quantization outside a vector index; Oracle quantizes in the QUANTIZATION clause of CREATE VECTOR INDEX')
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
			throw Unsupported.of('embedded deployments; the Oracle drivers connect to a database server')
		}
		if (concept.apiKey !== null) throw Unsupported.of('api keys; Oracle authenticates database users with passwords, certificates, Kerberos or tokens of an identity provider')
		if (concept.token !== null) throw Unsupported.of('tokens; has no Oracle mapping yet (OAuth 2.0 access tokens of an identity provider, which the Free container is not configured for)')
		if (concept.password instanceof StringLit) throw Unsupported.of('a password written in the script; read it from an environment variable with env(...)')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; Oracle Net listens on one port')
		if (concept.uri !== null && OracledbGenerator.dsnOf(concept.uri) === null) throw Unsupported.of('the connection uri ' + concept.uri + '; Oracle programs take an Easy Connect string host:port/service, with the user in username and the password from env(...)')
		for (name : ModelUtil.extParams(concept, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(concept, 'timeoutMillis', 'cloud', 'region', 'project')
	}

	/** Password authentication is the connection's; roles are Oracle roles; other methods are server configuration. */
	def private List<Stmt> authentication(AuthConfig auth, boolean ifNotExists) {
		if (auth.type != EAuthType.NONE && auth.type != EAuthType.USERNAME_PASSWORD) {
			throw Unsupported.of(auth.type.literal + ' authentication; Oracle authenticates users with passwords, certificates, Kerberos, RADIUS or an identity provider, configured on the server, and has no api keys')
		}
		if (auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; has no Oracle mapping yet (TCPS with a wallet)')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; Transparent Data Encryption needs a keystore that a DBA configures')
		if (auth.auditLog) throw Unsupported.of('audit logs; Oracle audit policies (CREATE AUDIT POLICY) need the AUDIT_ADMIN role, which the generated programs\' user does not have')
		val List<Stmt> result = newArrayList
		for (binding : auth.roles) result += role(binding, ifNotExists)
		result
	}

	/** A role and its object privileges on a collection (or on every table of the program's schema is not expressible: one table is required). */
	def private List<Stmt> role(RoleBinding binding, boolean ifNotExists) {
		val name = binding.roleName
		OracledbSql.id(name)
		if (binding.collection === null) throw Unsupported.of('a role without a collection; Oracle grants object privileges table by table (GRANT ... ON ANY TABLE is a system privilege)')
		val table = schema.table(binding.collection)
		val List<String> grants = newArrayList
		for (privilege : binding.privileges) {
			val sql = PRIVILEGES.get(privilege.toLowerCase)
			if (sql === null) throw Unsupported.of('the privilege ' + privilege + '; Oracle grants SELECT, INSERT, UPDATE, DELETE, REFERENCES, ALTER and INDEX on tables')
			grants += sql
		}
		val List<Stmt> result = newArrayList
		val create = OracledbSchema.execute(#['CREATE ROLE ' + OracledbSql.id(name)])
		if (ifNotExists) result += new Code.When(roleExists(name), true, #[create]) else result += create
		if (!grants.empty) result += OracledbSchema.execute(#['GRANT ' + grants.join(', ') + ' ON ' + OracledbSql.table(table) + ' TO ' + OracledbSql.id(name)])
		result
	}

	def private Code.Call roleExists(String name) {
		val q = new Query
		call('exists', 'SELECT ' + OracledbQueries.scalar('COUNT(*)') + ' FROM user_role_privs WHERE granted_role = ' + q.bind(text(name), 'string'), q, null)
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
		throw Unsupported.of(what + ' outside a collection; Oracle adds it to a table')
	}

	/** A filter of an analyzer: the lexer preference applies it already, so creating it checks the preference exists. */
	def private List<Stmt> analyzerFilter(AnalyzerFilter filter) {
		val analyzer = EcoreUtil2.getContainerOfType(filter, Analyzer)
		if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer; a lexer preference takes its attributes when it is created')
		OracledbSchema.checkFilter(filter.type.literal, if (analyzer.language === null) null else OracledbSchema.stemmer(analyzer.language))
		val q = new Query
		#[new Code.Run(call('value', 'SELECT ' + OracledbQueries.scalar('COUNT(*)') + ' FROM ctx_user_preferences WHERE pre_name = '
			+ q.bind(text(analyzer.name.toUpperCase), 'string'), q, null))]
	}

	/** A field limit added to an existing column: a named CHECK constraint. */
	def private List<Stmt> addLimit(FieldLimit limit) {
		val field = EcoreUtil2.getContainerOfType(limit, Field)
		if (field === null) throw Unsupported.of('a field limit outside a field declaration; a CHECK constraint belongs to a column')
		val collection = member(field, 'a field limit')
		val table = schema.table(collection)
		val column = table.column(ModelUtil.fieldName(field))
		val check = OracledbSchema.check(limit, field)
		#[OracledbSchema.execute(#['ALTER TABLE ' + OracledbSql.table(table) + ' ADD ' + check.replace('{}', OracledbSql.id(column.name))])]
	}

	/** An index created on an existing collection, unless an index of that name exists. */
	def private List<Stmt> createIndex(Index index, boolean ifNotExists) {
		switch index {
			VectorIndex: {
				val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
				if (field === null) throw Unsupported.of('a vector index without a vector field')
				val collection = member(field, 'a vector index')
				val table = schema.table(collection)
				val sql = schema.vectorIndex(collection, table, field, index)
				if (sql === null) return require(collection)
				#[OracledbSchema.guarded(OracledbSchema.indexName(collection, index.name), sql)]
			}
			PayloadIndex: {
				val field = index.target.field
				val collection = ModelUtil.targetCollection(index) ?: (if (field === null) null else ModelUtil.owningCollection(field))
				if (collection === null) throw Unsupported.of('a payload index outside a collection')
				val table = schema.table(collection)
				val name = OracledbSchema.indexName(collection, index.name)
				if (index.kind == EPayloadIndexKind.FULL_TEXT && field instanceof PayloadField) {
					return #[OracledbSchema.guarded(#[name, OracledbSchema.textIndexName(collection, field as PayloadField)], schema.payloadIndex(collection, table, index))]
				}
				#[OracledbSchema.guarded(name, schema.payloadIndex(collection, table, index))]
			}
		}
	}

	/** Existence check of a collection: the statement fails when the table is missing. */
	def private List<Stmt> require(VectorCollection collection) {
		val table = schema.table(collection)
		#[new Code.Run(call('query', 'SELECT NULL FROM ' + OracledbSql.table(table) + ' WHERE 1 = 0', null, null))]
	}

	/** ALTER TABLE ADD; a vector field with an index gets the index, a full-text field its CONTEXT index. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val result = addColumn(collection, table, field)
		schema.evolve(collection, table.with(schema.column(collection, field)))
		result
	}

	def private List<Stmt> addColumn(VectorCollection collection, Table table, Field field) {
		val column = schema.column(collection, field)
		val List<Stmt> result = newArrayList
		val definition = OracledbSql.columnDefinition(if (field instanceof VectorField) nullableVector(column) else column, false)
		result += OracledbSchema.execute(#['ALTER TABLE ' + OracledbSql.table(table) + ' ADD (' + definition + ')'])
		if (field instanceof VectorField) {
			val index = schema.indexOf(collection, field as VectorField)
			if (index !== null) {
				val sql = schema.vectorIndex(collection, table.with(column), field as VectorField, index)
				if (sql !== null) result += OracledbSchema.guarded(OracledbSchema.indexName(collection, index.name), sql)
			}
		}
		if (column.text && field instanceof PayloadField) {
			val name = OracledbSchema.textIndexName(collection, field as PayloadField)
			result += OracledbSchema.guarded(name, OracledbSql.createPayloadIndex(table.with(column), name, column, #[], null, 'text'))
		}
		result
	}

	/** Rows that exist when a vector column is added have no vector, so the added column is nullable. */
	def private Column nullableVector(Column column) {
		new Column(column.name, column.type, null, false, column.vec, null, true, null, #[])
	}

	/** A list partition added to the table; automatic list partitioning would also create it on the first write. */
	def private List<Stmt> addPartition(VectorCollection collection, Partition partition, boolean ifNotExists) {
		val table = schema.table(collection)
		if (table.partition === null) throw Unsupported.of('a partition of a collection declared without partitions; has no Oracle mapping yet (ALTER TABLE MODIFY PARTITION BY converts a table online)')
		if (partition.key !== null || !partition.subPartitions.empty) throw Unsupported.of('partitions by key and sub-partitions; has no Oracle mapping yet (hash and composite partitioning)')
		val add = OracledbSchema.execute(#['ALTER TABLE ' + OracledbSql.table(table) + ' ADD ' + OracledbSql.partitionClause(partition.name)])
		if (!ifNotExists) return #[add]
		val q = new Query
		#[new Code.When(call('exists', 'SELECT ' + OracledbQueries.scalar('COUNT(*)') + ' FROM user_tab_partitions WHERE table_name = '
			+ q.bind(text(table.name), 'string') + ' AND partition_name = ' + q.bind(text(partition.name), 'string'), q, null), true, #[add])]
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) {
			if (!operations.empty || newName === null) throw Unsupported.of('alter operations on an alias; an alias is a view, which can be renamed')
			val alias = concept as Alias
			return #[OracledbSchema.execute(#['RENAME ' + OracledbSql.id(alias.name) + ' TO ' + OracledbSql.id(newName)])]
		}
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; Oracle alters tables and views')
		val collection = concept as VectorCollection
		var table = schema.table(collection)
		val List<Stmt> result = newArrayList
		for (operation : operations) {
			result += alter(collection, table, operation)
			table = evolved(collection, table, operation)
		}
		if (newName !== null) {
			val renamed = OracledbSchema.physical(table.database, newName)
			result += OracledbSchema.execute(#['ALTER TABLE ' + OracledbSql.table(table) + ' RENAME TO ' + OracledbSql.id(renamed)])
			table = table.named(renamed)
		}
		if (result.empty) result += require(collection)
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

	def List<Stmt> alter(VectorCollection collection, Table table, AlterOperation operation) {
		switch operation {
			AddFieldOp: addColumn(collection, table, operation.newField)
			DropFieldOp: #[OracledbSchema.execute(#[dropColumn(collection, table, operation.field)])]
			RenameFieldOp: {
				if (operation.field === collection.primaryKey) throw Unsupported.of('renaming the primary key; the generated statements address the key by its declared name')
				OracledbSql.id(operation.newName)
				#[OracledbSchema.execute(#['ALTER TABLE ' + OracledbSql.table(table) + ' RENAME COLUMN ' + OracledbSql.id(table.column(ModelUtil.fieldName(operation.field)).name) + ' TO ' + OracledbSql.id(operation.newName)])]
			}
			SetPropertyOp: setProperty(table, operation)
			AddAliasOp: #[OracledbSchema.execute(#[OracledbSchema.createView(operation.newAlias.name, table)])]
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def private String dropColumn(VectorCollection collection, Table table, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
		'ALTER TABLE ' + OracledbSql.table(table) + ' DROP COLUMN ' + OracledbSql.id(table.column(ModelUtil.fieldName(field)).name)
	}

	/** Collection properties: the description is the table comment; consistency is met by every committed write. */
	def private List<Stmt> setProperty(Table table, SetPropertyOp operation) {
		switch operation.property {
			case DESCRIPTION: {
				if (!(operation.value instanceof StringLit)) throw Unsupported.of('a description that is not a string literal; COMMENT takes a constant')
				#[OracledbSchema.execute(#['COMMENT ON TABLE ' + OracledbSql.table(table) + ' IS ' + OracledbSql.literal((operation.value as StringLit).value)])]
			}
			case CONSISTENCY: newArrayList
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DYNAMIC: throw Unsupported.of('dynamic; ' + PENDING + ' (a JSON column for undeclared fields)')
			case TTL: throw Unsupported.of(NO_TTL)
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val table = schema.table(concept)
				schema.evolve(concept, null)
				#[OracledbSchema.execute(#['DROP TABLE ' + (if (ifExists) 'IF EXISTS ' else '') + OracledbSql.table(table) + ' CASCADE CONSTRAINTS PURGE'])]
			}
			Database: {
				val prefix = OracledbSchema.physical(concept.name, '')
				val tables = 'SELECT table_name FROM user_tables WHERE SUBSTR(table_name, 1, ' + prefix.length + ') = ' + OracledbSql.literal(prefix)
				#[OracledbSchema.execute(#['BEGIN FOR r IN (' + tables + ') LOOP EXECUTE IMMEDIATE \'DROP TABLE "\' || r.table_name || \'" CASCADE CONSTRAINTS PURGE\'; END LOOP; END;'])]
			}
			Tenant: dropSlice(member(concept, 'a tenant'), schema.table(member(concept, 'a tenant')).tenant, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), schema.table(member(concept, 'a namespace')).namespace, concept.key)
			PayloadField: dropField(member(concept, 'a field'), concept)
			VectorField: dropField(member(concept, 'a field'), concept)
			Alias: {
				val drop = 'DROP VIEW ' + OracledbSql.id(concept.name)
				#[OracledbSchema.execute(#[if (ifExists) OracledbSql.tolerant(drop, -942) else drop])]
			}
			Partition: {
				val collection = member(concept, 'a partition')
				val table = schema.table(collection)
				if (table.partition === null) throw Unsupported.of('a partition of a collection declared without partitions')
				val drop = 'ALTER TABLE ' + OracledbSql.table(table) + ' DROP PARTITION ' + OracledbSql.id(concept.name) + ' UPDATE INDEXES'
				#[OracledbSchema.execute(#[if (ifExists) OracledbSql.tolerant(drop, -2149) else drop])]
			}
			VectorIndex: dropIndex(concept, ifExists)
			PayloadIndex: dropIndex(concept, ifExists)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; the quantization belongs to its vector index (drop the index)')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not an Oracle object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; the program drops tables and their objects, not the database')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE_SERVER)
			Analyzer: {
				val drop = 'BEGIN CTX_DDL.DROP_PREFERENCE(' + OracledbSql.literal(concept.name) + '); END;'
				#[OracledbSchema.execute(#[if (ifExists) OracledbSql.tolerant(drop, -20000) else drop])]
			}
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; a lexer preference keeps the attributes of its language (drop the analyzer)')
			FieldLimit: dropLimit(concept, ifExists)
			FileResource: throw Unsupported.of(NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_SERVER)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_SERVER)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('dropping authentication; Oracle authentication methods are server configuration')
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
		val sql = 'DELETE FROM ' + OracledbSql.table(table) + ' ' + OracledbSql.ALIAS + ' WHERE ' + OracledbSql.col(column) + ' = ' + q.bind(text(value), 'string')
		#[new Code.Return(modify(sql, q, 'delete_count'))]
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		val table = schema.table(collection)
		val result = #[OracledbSchema.execute(#[dropColumn(collection, table, field)])]
		schema.evolve(collection, table.without(ModelUtil.fieldName(field)))
		result
	}

	def private List<Stmt> dropIndex(Index index, boolean ifExists) {
		val domain = index instanceof PayloadIndex && #[EPayloadIndexKind.FULL_TEXT, EPayloadIndexKind.JSON].contains((index as PayloadIndex).kind)
		val drop = 'DROP INDEX ' + OracledbSql.id(indexName(index)) + (if (domain) ' FORCE' else '')
		#[OracledbSchema.execute(#[if (ifExists) OracledbSql.tolerant(drop, -1418) else drop])]
	}

	def private String indexName(Index index) {
		val collection = indexCollection(index)
		if (collection === null) throw Unsupported.of('an index outside a collection')
		OracledbSchema.indexName(collection, index.name)
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

	def private List<Stmt> dropLimit(FieldLimit limit, boolean ifExists) {
		val field = EcoreUtil2.getContainerOfType(limit, Field)
		if (field === null) throw Unsupported.of('a field limit outside a field declaration')
		val table = schema.table(member(field, 'a field limit'))
		val drop = 'ALTER TABLE ' + OracledbSql.table(table) + ' DROP CONSTRAINT ' + OracledbSql.id(limit.name)
		#[OracledbSchema.execute(#[if (ifExists) OracledbSql.tolerant(drop, -2443) else drop])]
	}

	/** DROP ROLE revokes the role's grants. */
	def private List<Stmt> dropRole(RoleBinding binding) {
		val name = binding.roleName
		OracledbSql.id(name)
		#[new Code.When(roleExists(name), false, #[OracledbSchema.execute(#['DROP ROLE ' + OracledbSql.id(name)])])]
	}

	/** An alias is a view: the swap replaces its query (CREATE OR REPLACE VIEW). */
	def dispatch List<Stmt> body(SwapAlias it) {
		val table = schema.table(newCollection)
		#[OracledbSchema.execute(#[OracledbSchema.createView(alias.name, table)])]
	}

	/** A DML statement whose runtime result is {counter: affected rows}; skipped reports the records an insert did not write. */
	def private Code.Call modify(String sql, Query q, String counter) {
		val call = call('modify', sql, q, null)
		call.args.put('counter', text(counter))
		call
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

	/** One write of the rows (as a JSON array parameter) with the slice values of the statement. */
	def private Code.Call write(VectorCollection collection, RecordSource source, String policy, String counter, EObject statement) {
		val table = schema.table(collection)
		val q = new Query
		val records = rows(source, table)
		val result = modify(OracledbSql.insertRows(q, table, records, slices(statement, collection, table), policy), q, counter)
		if (policy == 'ignore') result.args.put('skipped', num((result.args.get('params') as Code.Items).items.indexOf(records)))
		result
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
		if (table.partition !== null) result.put(table.partition, text(partition?.name ?: OracledbSchema.defaultPartition(collection) ?: OracledbSchema.DEFAULT_PARTITION))
		result
	}

	def dispatch List<Stmt> body(Update it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val table = schema.table(collection)
		val q = new Query
		if (source !== null) {
			if (values !== null || !removedFields.empty || !appendFields.empty || !arrayRemoveFields.empty) {
				throw Unsupported.of("update from records with 'set', remove-fields, array-append or array-remove; each record carries its own values")
			}
			if (written(it, 'ifVersion')) throw Unsupported.of('if-version with an update from records; one version cannot guard records that each carry their own values')
			val records = rows(source, table)
			val List<String> restrictions = newArrayList
			if (!ids.empty) restrictions += OracledbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
			restrictions += OracledbSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
			val fromRecords = OracledbSql.updateRows(q, table, records, written(it, 'mode') && mode == EUpdateMode.REPLACE, OracledbSql.and(restrictions))
			return #[new Code.Return(modify(fromRecords, q, 'update_count')) as Stmt]
		}
		val List<String> sets = newArrayList
		val List<String> given = newArrayList
		if (values !== null) {
			for (entry : values.entries) {
				if (entry.field === collection.primaryKey) throw Unsupported.of('updating the primary key; the key identifies the row')
				val column = table.column(ModelUtil.fieldName(entry.field))
				val name = column.name
				given += name
				if (appendFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += OracledbSql.id(name) + ' = ' + arrayMerge(q, column, convert(entry.value, column), false)
				} else if (arrayRemoveFields.contains(entry.field)) {
					arrayField(entry.field, column)
					sets += OracledbSql.id(name) + ' = ' + arrayMerge(q, column, convert(entry.value, column), true)
				} else {
					sets += OracledbSql.id(name) + ' = ' + OracledbSql.value(q, column, convert(entry.value, column))
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
			sets += OracledbSql.id(column.name) + ' = NULL'
		}
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
			for (column : table.columns) {
				if (column.name != table.key && column.vec === null && !given.contains(column.name) && !removedFields.exists[f | table.column(ModelUtil.fieldName(f)).name == column.name]) {
					sets += OracledbSql.id(column.name) + ' = DEFAULT'
				}
			}
		}
		if (sets.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += OracledbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += OracledbSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
		if (written(it, 'ifVersion')) conditions += 'ORA_ROWSCN = ' + q.bind(num(ifVersion), 'number')
		val update = 'UPDATE ' + OracledbSql.table(table) + ' ' + OracledbSql.ALIAS + ' SET ' + sets.join(', ') + OracledbSql.whereClause(OracledbSql.and(conditions))
		#[new Code.Return(modify(update, q, 'update_count'))]
	}

	/**
	 * The array of a JSON array column after appending the items (in order) or removing every element equal to one of them: the
	 * elements are read with JSON_TABLE and aggregated again with JSON_ARRAYAGG in their order.
	 */
	def private String arrayMerge(Query q, Column column, Expr itemsValue, boolean remove) {
		val type = if (column.element == 'number') 'NUMBER' else if (column.element == 'boolean') 'BOOLEAN' else 'VARCHAR2(4000)'
		val stored = 'SELECT p, v FROM JSON_TABLE(' + OracledbSql.col(column.name) + ', \'$[*]\' COLUMNS (p FOR ORDINALITY, v ' + type + ' PATH \'$\'))'
		val given = 'JSON_TABLE(' + q.json(itemsValue) + ', \'$[*]\' COLUMNS (p FOR ORDINALITY, v ' + type + ' PATH \'$\'))'
		val merged = if (remove) {
			'SELECT 1 AS o, s.p, s.v FROM (' + stored + ') s WHERE s.v NOT IN (SELECT g.v FROM ' + given + ' g WHERE g.v IS NOT NULL)'
		} else {
			'SELECT 1 AS o, s.p, s.v FROM (' + stored + ') s UNION ALL SELECT 2 AS o, g.p, g.v FROM ' + given + ' g'
		}
		'NVL((SELECT JSON_ARRAYAGG(m.v ORDER BY m.o, m.p RETURNING CLOB) FROM (' + merged + ') m), \'[]\')'
	}

	def private void arrayField(Field field, Column column) {
		if (!column.array) throw Unsupported.of('array-append or array-remove on ' + ModelUtil.fieldName(field) + ', which is not an array field')
	}

	def dispatch List<Stmt> body(Delete it) {
		writeOptions(it)
		val table = schema.table(collection)
		val q = new Query
		val List<String> conditions = newArrayList
		if (!ids.empty) conditions += OracledbSql.keyIn(q, table, items(ids.map[Literals.of(it)]))
		conditions += OracledbSql.where(q, table, filterOf(it, collection, table, filter, true).parts)
		val condition = OracledbSql.and(conditions)
		if (condition === null && !deleteAll) throw Unsupported.of("a delete without ids, filter or 'all'")
		val target = if (written(it, 'deleteLimit')) {
			OracledbSql.ALIAS + '.ROWID IN (SELECT ' + OracledbSql.ALIAS + '.ROWID FROM ' + OracledbSql.table(table) + ' ' + OracledbSql.ALIAS
				+ OracledbSql.whereClause(condition) + ' ORDER BY ' + OracledbSql.col(table.key) + ' FETCH FIRST ' + deleteLimit + ' ROWS ONLY)'
		} else condition
		val delete = 'DELETE FROM ' + OracledbSql.table(table) + ' ' + OracledbSql.ALIAS + OracledbSql.whereClause(target)
		#[new Code.Return(modify(delete, q, 'delete_count'))]
	}

	/**
	 * The runtime reads a local JSON or JSON Lines file and writes it in batches with one statement (the batch is the JSON array
	 * parameter at position rows), all in one transaction. SQL*Loader and external tables would read files on the server, not the client.
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Oracle has no ingestion pipeline a generated program sets up (GoldenGate is a separate product)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; has no Oracle mapping yet (DBMS_CLOUD reads object stores with a credential)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV records read by the runtimes)')
			throw Unsupported.of('import format ' + format.literal + '; has no Oracle mapping yet (external tables read ORACLE_LOADER and ORACLE_DATAPUMP files, and parquet through ORACLE_BIGDATA)')
		}
		schema.note(it, 'parallelism', 'async')
		val table = schema.table(collection)
		val q = new Query
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val batch = new Code.Nil
		val statement = modify(OracledbSql.insertRows(q, table, batch, slices(it, collection, table), policy),
			q, if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count')
		val args = kw('sql', statement.args.get('sql'), 'location', text(location))
		val params = statement.args.get('params') as Code.Items
		args.put('params', params)
		args.put('rows', num(params.items.indexOf(batch)))
		args.put('counter', statement.args.get('counter'))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[new Code.Return(runtime('import_file', args))]
	}

	/**
	 * The statements of a vecDSL transaction in one Oracle transaction: every step runs, then one COMMIT; any failure rolls back
	 * all of them. Read committed is Oracle's default; serializable (and repeatable read and snapshot, which Oracle's serializable
	 * level, a snapshot of the transaction start, covers) is set with SET TRANSACTION.
	 */
	def dispatch List<Stmt> body(Transaction it) {
		val List<Expr> steps = newArrayList
		for (statement : it.methods) {
			if (statement instanceof BulkImport) throw Unsupported.of('bulk-import inside a transaction; ' + PENDING + ' (the import as one step of the transaction)')
			val inner = body(statement)
			if (inner.size != 1 || !(inner.head instanceof Code.Return)) throw Unsupported.of('this statement inside a transaction')
			val invocation = (inner.head as Code.Return).value as Code.Call
			if (!#['modify', 'query', 'value'].contains(invocation.op)) throw Unsupported.of('this statement inside a transaction')
			val step = kw('op', text(invocation.op), 'sql', invocation.args.get('sql'))
			for (key : #['params', 'counter', 'skipped']) if (invocation.args.has(key)) step.put(key, invocation.args.get(key))
			steps += step
		}
		val args = kw('steps', items(steps))
		if (written(it, 'isolationLevel')) {
			val level = isolationLevel.literal
			if (level == 'read_uncommitted') notes += 'read_uncommitted on Transaction, which Oracle runs as read committed'
			args.put('isolation', text(if (level == 'read_uncommitted' || level == 'read_committed') 'read_committed' else 'serializable'))
		}
		#[new Code.Return(runtime('transaction', args))]
	}

	/** A committed write is durable and visible to the next statement, so 'wait' needs no call; extensions are rejected. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (name : ModelUtil.extParams(statement, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** A statement's filter with its tenant, namespace and partition conditions. */
	def private OracledbFilter filterOf(EObject statement, VectorCollection collection, Table table, FilterBlock block, boolean write) {
		val result = new OracledbFilter().where(block)
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

	/**
	 * Loading into the In-Memory column store at high priority, so that the database populates the table without waiting for a full
	 * scan (In-Memory Guide, Enabling Objects for In-Memory Population); the store needs INMEMORY_SIZE above 0, an instance
	 * parameter the setup script sets.
	 */
	def dispatch List<Stmt> body(Load it) {
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[OracledbSchema.execute(inMemory(schema.table(collection), partitions, 'INMEMORY PRIORITY HIGH'))]
	}

	/** NO INMEMORY evicts a populated table, as every change of an INMEMORY attribute other than PRIORITY does. */
	def dispatch List<Stmt> body(Release it) {
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		#[OracledbSchema.execute(inMemory(schema.table(collection), partitions, 'NO INMEMORY'))]
	}

	/** ALTER TABLE with an INMEMORY clause for the table, or for each named partition (MODIFY PARTITION). */
	def private List<String> inMemory(Table table, List<Partition> named, String clause) {
		if (named.empty) return #['ALTER TABLE ' + OracledbSql.table(table) + ' ' + clause]
		if (table.partition === null) throw Unsupported.of('partitions of a collection declared without partitions')
		named.map[p | 'ALTER TABLE ' + OracledbSql.table(table) + ' MODIFY PARTITION ' + OracledbSql.id(p.name) + ' ' + clause].toList
	}

	/** A commit is durable once the redo is written, so a flush only checks the table. */
	def dispatch List<Stmt> body(Flush it) {
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		require(collection)
	}

	/**
	 * Online segment shrink (Administrator's Guide, Shrinking Database Segments Online): row movement first, then SHRINK SPACE on
	 * the table, then SHRINK SPACE on each non-partitioned B-tree index of it. Not SHRINK SPACE CASCADE: on 23.26 it fails with
	 * ORA-51926 (cannot alter an existing vector index) when the table has a vector index (an IVF index
	 * included); the table alone shrinks. Vector, Oracle Text (domain) and LOB indexes are left as they are. Indexes are read from
	 * USER_INDEXES at run time because a vector index can come from a later create vector-index statement. Tables with
	 * function-based indexes cannot be shrunk; the generator writes one for every payload index on a sub-field of a JSON field.
	 */
	def dispatch List<Stmt> body(Compact it) {
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema.note(it, 'targetSegments', 'async')
		if (collection.indexes.filter(PayloadIndex).exists[index | index.target.rawPath === null && !index.target.subFields.empty]) {
			throw Unsupported.of("compacting a collection with a payload index on a sub-field; that index is function-based, and Oracle cannot shrink tables with function-based indexes (Administrator's Guide, Shrinking Database Segments Online)")
		}
		val target = schema.table(collection)
		val table = OracledbSql.table(target)
		val indexes = 'BEGIN FOR r IN (SELECT index_name FROM user_indexes WHERE table_name = ' + OracledbSql.literal(target.name())
			+ ' AND index_type = \'NORMAL\' AND partitioned = \'NO\') LOOP EXECUTE IMMEDIATE \'ALTER INDEX "\' || r.index_name || \'" SHRINK SPACE\'; END LOOP; END;'
		#[OracledbSchema.execute(#['ALTER TABLE ' + table + ' ENABLE ROW MOVEMENT', 'ALTER TABLE ' + table + ' SHRINK SPACE', indexes])]
	}

	def dispatch List<Stmt> body(Describe it) {
		val table = schema.table(collection)
		val q = new Query
		#[new Code.Return(call('value', OracledbQueries.describe(q, table, includeStats, includeIndexProgress), q, null))]
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

	/**
	 * CREATE TABLE ... AS SELECT copies the columns and rows; the primary key is added after it. Indexes, comments and views are not
	 * copied (DBMS_METADATA would have to rewrite their names).
	 */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; a vecDSL database block is the server connection (PDB cloning needs CREATE PLUGGABLE DATABASE)')
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; Oracle has no copy-on-write tables in the Free edition (snapshot copies of PDBs need a copy-on-write file system)')
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val table = schema.table(sourceCollection)
		val target = OracledbSql.id(OracledbSchema.physical(table.database, targetName))
		notes += 'the indexes, comment and aliases of ' + table.name + ', which the clone does not copy'
		val copied = if (asOf === null) OracledbSql.table(table) else OracledbSql.table(table) + past(table, asOf).flashback.constant
		#[OracledbSchema.execute(#['CREATE TABLE ' + target + ' AS SELECT * FROM ' + copied,
			'ALTER TABLE ' + target + ' ADD PRIMARY KEY (' + OracledbSql.id(table.key) + ')'])]
	}

	/** ALTER INDEX ... REBUILD; a vector index is rebuilt by dropping and creating it, which the generator does not do yet. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (name : ModelUtil.extParams(it, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (index instanceof VectorIndex) throw Unsupported.of('rebuilding a vector index; ' + PENDING + ' (DBMS_VECTOR.REBUILD_INDEX)')
		val payload = index as PayloadIndex
		val name = OracledbSql.id(indexName(index))
		val sql = if (payload.kind == EPayloadIndexKind.FULL_TEXT || payload.kind == EPayloadIndexKind.JSON) {
			'ALTER INDEX ' + name + ' REBUILD'
		} else {
			'ALTER INDEX ' + name + ' REBUILD' + (if (concurrently) ' ONLINE' else '')
		}
		#[OracledbSchema.execute(#[sql])]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val table = schema.table(collection)
		if (namespace !== null) return dropSlice(collection, table.namespace, namespace)
		#[OracledbSchema.execute(#['TRUNCATE TABLE ' + OracledbSql.table(table)])]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val q = new Query
		val owner = scope
		val source = switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) {
					OracledbQueries.collections(q, (owner as Database).name)
				} else if (owner !== null && !(owner instanceof VectorDatabase)) {
					throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; Oracle lists the tables of a schema')
				} else {
					OracledbQueries.collections()
				}
			}
			case DATABASES: {
				if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
				OracledbQueries.databases()
			}
			case INDEXES: {
				val table = scopeTable(owner, 'indexes')
				OracledbQueries.indexes(q, table, table.name)
			}
			case PARTITIONS: OracledbQueries.partitions(q, scopeTable(owner, 'partitions'))
			case ALIASES: OracledbQueries.aliases(q, if (owner === null) null else scopeTable(owner, 'aliases'))
			case TENANTS,
			case NAMESPACES: {
				val table = scopeTable(owner, kind.literal)
				val column = if (kind.literal == 'tenants') table.tenant else table.namespace
				if (column === null) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				OracledbQueries.slices(table, column)
			}
			case SNAPSHOTS: throw Unsupported.of(NO_SNAPSHOTS)
		}
		val sql = OracledbQueries.names(q, source, if (prefix === null) null else text(prefix), if (written(it, 'listLimit')) listLimit else null)
		#[new Code.Return(call('value', sql, q, null))]
	}

	def private Table scopeTable(Concept owner, String what) {
		if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + what + ' without a collection')
		schema.table(owner as VectorCollection)
	}

	/** CREATE INDEX returns when the index is built (CONTEXT indexes then sync on commit), so awaiting one reads its state once. */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state', 'timeoutSeconds')
		val q = new Query
		#[new Code.Return(call('value', OracledbQueries.indexState(q, indexName(index)), q, null))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'explain', 'reranker', 'queryModel')
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val metric = if (written(it, 'metric')) it.metric.literal else column.vec.metric
		val q = new Query
		val searched = input
		queryVector(new Query, searched, collection, table, column)
		val List<String> stages = newArrayList
		val staged = prefetchStages(it, q, collection, table, prefetches, input, field, stages)
		val where = OracledbSql.and(#[OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts), OracledbQueries.within(staged)])
		val shape = shapeOf(it, collection, table, field, #[])
		val fetch = fetchOf(collection, field, column, metric, params, exact)
		val prelude = if (stages.empty) null else stages.join(', ')
		val search = new Search(table, column, metric, [| queryVector(q, searched, collection, table, column)], prelude, topK, offsetOf(it), where,
			projection(collection, table, output, excludedOf(it), withVectors), written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds), fetch)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', OracledbQueries.vectorSearch(q, search, shape), q, merged(timeout(params), extensionSettings(it))))]
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'explain', 'reranker', 'minShouldMatch')
		queryExtensions(it)
		if (params !== null) schema.note(params, 'targetRecall', 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		val table = past(schema.table(collection), asOf)
		val q = new Query
		val fields = keywordFields(collection, table, searchFields)
		val words = keywordText(input)
		val op = keywordOperator(if (written(it, 'textQueryType')) textQueryType.literal else 'match', if (written(it, 'matchOperator')) matchOperator.literal else 'and')
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val fuzzy = fuzzyOf(it, op)
		val span = spanOf(it, op)
		val keywords = new Keywords(table, fields, op, words, topK, offsetOf(it), where, projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds), fuzzy, span)
		val shape = shapeOf(it, collection, table, null, #[new Terms(op, words, fuzzy, span)])
		highlighting(shape, new Code.Return(call(if (shape.wrapped) 'value' else 'query', OracledbQueries.keywordSearch(q, keywords, shape), q, merged(timeout(params), extensionSettings(it)))))
	}

	/** Hybrid search fused in SQL: each leg ranks its candidates in a CTE, and the fusion function of vecDSL combines them. */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'explain', 'reranker')
		queryExtensions(it)
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val List<Terms> terms = newArrayList
		val table = past(schema.table(collection), asOf)
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
			val legWhere = OracledbSql.where(q, table, filterOf(it, collection, table, if (feature(leg, 'filter') === null) filter else combined(filter, feature(leg, 'filter') as FilterBlock), false).parts)
			for (name : ModelUtil.extParams(leg, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name + ' on a hybrid leg')
			switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (a score condition in the leg CTE)')
					val field = vectorFieldOf(collection, leg.vectorField)
					val column = table.column(ModelUtil.fieldName(field))
					val metric = if (written(leg, 'metric')) leg.metric.literal else column.vec.metric
					val legInput = leg.input ?: shared
					queryVector(new Query, legInput, collection, table, column)
					val fetch = fetchOf(collection, field, column, metric, leg.params, leg.exact)
					if (leg.params !== null) options.putAll(timeout(leg.params))
					parts += new Leg(column, metric, [| queryVector(q, legInput, collection, table, column)], fetch, null, null, null, limit, legWhere, weight)
				}
				KeywordLeg: {
					if (written(leg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
					val fields = keywordFields(collection, table, leg.searchFields)
					val op = keywordOperator(if (written(leg, 'textQueryType')) leg.textQueryType.literal else 'match', if (written(leg, 'matchOperator')) leg.matchOperator.literal else 'and')
					val legText = keywordText(leg.input ?: shared)
					terms += new Terms(op, legText, false, null)
					parts += new Leg(null, null, null, null, fields, op, legText, limit, legWhere, weight)
				}
			}
			index = index + 1
		}
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val hybrid = new Hybrid(table, parts, fusionKind, k, null, topK, offsetOf(it), projection(collection, table, output, excludedOf(it), withVectors), minOf(bounds), maxOf(bounds))
		options.putAll(extensionSettings(it))
		val shape = shapeOf(it, collection, table, null, terms)
		highlighting(shape, new Code.Return(call(if (shape.wrapped) 'value' else 'query', OracledbQueries.hybridSearch(q, hybrid, shape), q, options)))
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
		reject(it, 'explain', 'reranker')
		queryExtensions(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val table = past(schema.table(collection), asOf)
		val lookup = past(schema.table(lookupCollection ?: collection), asOf)
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
		val prelude = OracledbQueries.recommendVector(q, lookup, lookupColumn, positiveIds, positiveVectors, negativeIds, negativeVectors)
		val List<Cond> conditions = newArrayList
		conditions += filterOf(it, collection, table, filter, false).parts
		val List<String> parts = newArrayList
		parts += OracledbSql.where(q, table, conditions)
		if (lookupCollection === null || lookupCollection === collection) {
			val List<Expr> examples = newArrayList
			examples += positiveIds
			examples += negativeIds
			if (!examples.empty) parts += OracledbSql.notTrue(OracledbSql.keyIn(q, table, items(examples)))
		}
		val where = OracledbSql.and(parts)
		val shape = shapeOf(it, collection, table, field, #[])
		val fetch = fetchOf(collection, field, column, column.vec.metric, params, false)
		val search = new Search(table, column, column.vec.metric, [| '(SELECT v FROM q)'], prelude, topK, offsetOf(it), where,
			projection(collection, table, output, excludedOf(it), withVectors), written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, minOf(bounds), maxOf(bounds), fetch)
		#[new Code.Return(call(if (shape.wrapped) 'value' else 'query', OracledbQueries.vectorSearch(q, search, shape), q, merged(timeout(params), extensionSettings(it))))]
	}

	def private boolean isVector(Literal example) {
		example instanceof ListLit || example instanceof SparseLit
			|| (example instanceof ParamRef && #['vector', 'sparse_vector'].contains((example as ParamRef).param.type.literal))
	}

	def dispatch List<Stmt> body(GetById it) {
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val q = new Query
		val shape = projection(collection, table, output, newArrayList, withVectors)
		val List<String> names = newArrayList
		names += shape.output
		names += shape.vectors
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, null, false).parts)
		#[new Code.Return(call('query', OracledbQueries.getRows(q, table, items(ids.map[Literals.of(it)]), names, where), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Count it) {
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val q = new Query
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		#[new Code.Return(call('value', OracledbQueries.count(q, table, where), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val q = new Query
		val shape = projection(collection, table, output, excludedOf(it), withVectors)
		val List<String> names = newArrayList
		names += shape.output
		names += shape.vectors
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val List<Order> order = newArrayList
		for (clause : sortClauses) {
			if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			order += new Order(fieldRefOf(collection, table, clause.field, 'sort'), clause.direction == ESortDirection.DESC)
		}
		val limit = if (written(it, 'scrollLimit')) scrollLimit else null
		if (cursor === null && !written(it, 'sliceTotal')) {
			val terms = order.map[o | o.field.sql(OracledbSql.ALIAS) + (if (o.descending) ' DESC NULLS LAST' else ' ASC NULLS LAST')].toList
			return #[new Code.Return(call('query', OracledbQueries.scroll(q, table, names, where, terms, limit, offsetOf(it)), q, extensionSettings(it))) as Stmt]
		}
		val sliced = written(it, 'sliceTotal')
		if (sliced && (sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal)) throw Unsupported.of('a slice index outside 0 to the slice count minus 1')
		val cursorKey = if (cursor === null) null else text(cursor)
		#[new Code.Return(call('query', OracledbQueries.scroll(q, table, names, where, order, limit, offsetOf(it), cursorKey, if (sliced) sliceIndex else null,
			if (sliced) sliceTotal else null), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(Sample it) {
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val q = new Query
		val shape = projection(collection, table, output, newArrayList, false)
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val first = written(it, 'method') && method == ESampleMethod.FIRST
		#[new Code.Return(call('query', OracledbQueries.sample(q, table, shape.output, where, sampleSize, first), q, extensionSettings(it)))]
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		queryExtensions(it)
		val table = past(schema.table(collection), asOf)
		val field = vectorFieldOf(collection, vectorField)
		val column = table.column(ModelUtil.fieldName(field))
		val q = new Query
		val where = OracledbSql.where(q, table, filterOf(it, collection, table, filter, false).parts)
		val sample = if (written(it, 'sampleSize')) sampleSize else 10
		val limit = if (written(it, 'matrixLimit')) matrixLimit else 3
		#[new Code.Return(call('query', OracledbQueries.distanceMatrix(q, table, column, column.vec.metric, where, sample, limit), q, extensionSettings(it)))]
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

	/**
	 * How a vector search fetches: approximately through the field's HNSW or IVF index when there is one, the search is not exact
	 * and its metric is the index's (VECTOR_DISTANCE with another metric runs an exact search, SQL Language Reference); the target
	 * accuracy is ceil(100 q) of target-recall (recall 1 is an exact fetch, as the capability manifest records), else EFSEARCH of
	 * the candidate pool (or the index's search-candidates) for HNSW and NEIGHBOR PARTITION PROBES for IVF.
	 */
	def private Fetch fetchOf(VectorCollection collection, VectorField field, Column column, String metric, SearchParams params, boolean exact) {
		val index = schema.indexOf(collection, field)
		val family = column.vec.family
		if (params !== null) {
			schema.note(params, 'scanBudget', 'oversampling', 'rescore')
			for (name : ModelUtil.extParams(params, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
			if (written(params, 'filterStrategy') && params.filterStrategy.literal != 'auto') notes += 'filter-strategy on SearchParams, which the Oracle optimizer chooses'
		}
		val exhaustive = exact || (params !== null && params.exhaustive) || (params !== null && written(params, 'targetRecall') && params.targetRecall >= 1.0)
		if (family === null || exhaustive || metric != column.vec.metric) {
			if (params !== null) schema.note(params, 'targetRecall', 'candidatePool', 'probes')
			return Fetch.EXACT
		}
		if (params !== null && written(params, 'targetRecall')) {
			if (params.targetRecall <= 0) throw Unsupported.of('a target-recall of 0 or less')
			return new Fetch(true, accuracy(params.targetRecall), null)
		}
		if (family == 'hnsw') {
			if (params !== null && written(params, 'probes')) throw Unsupported.of('probes on an hnsw index; neighbor partition probes belong to ivf indexes')
			val candidates = if (params !== null && written(params, 'candidatePool')) params.candidatePool else if (written(index, 'searchCandidates')) index.searchCandidates else 0
			return new Fetch(true, null, if (candidates > 0) 'EFSEARCH ' + candidates else null)
		}
		if (params !== null && written(params, 'candidatePool')) throw Unsupported.of('candidates on an ivf index; candidate lists belong to hnsw indexes')
		new Fetch(true, null, if (params !== null && written(params, 'probes')) 'NEIGHBOR PARTITION PROBES ' + params.probes else null)
	}

	/** The target accuracy of a recall: ceil(100 q), at most 100. */
	def static int accuracy(double recall) {
		Math.min(100, Math.ceil(java.math.BigDecimal.valueOf(recall).multiply(java.math.BigDecimal.valueOf(100)).doubleValue) as int)
	}

	def private Map<String, String> merged(Map<String, String> first, Map<String, String> second) {
		val Map<String, String> result = newLinkedHashMap
		result.putAll(first)
		result.putAll(second)
		result
	}

	/** The call timeout of a search, which the runtimes set on the connection for that call. */
	def private Map<String, String> timeout(SearchParams params) {
		val Map<String, String> result = newLinkedHashMap
		if (params !== null && written(params, 'timeoutMillis')) result.put('call_timeout', String.valueOf(params.timeoutMillis))
		result
	}

	/** Oracle extension blocks of queries carry no settings: every search parameter is part of the SQL text. */
	def private Map<String, String> extensionSettings(EObject host) {
		for (name : ModelUtil.extParams(host, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name + '; Oracle search parameters are written in the SQL text, and the generator takes them from vecDSL clauses')
		newLinkedHashMap
	}

	/** Rejects clauses Oracle lacks, with their reason, and clauses not generated yet. */
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

	/** Oracle extension blocks of queries: extension items of other kinds than parameters are rejected. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == OracledbGenerator.VENDOR) {
				for (item : block.getItems) {
					if (!(item instanceof org.ftn.vecDSL.vecDSL.ExtParam)) throw Unsupported.of(item.eClass.name + ' on Oracle')
				}
			}
		}
		extensionSettings(host)
	}

	/** The query vector as SQL: a bound TO_VECTOR, or the stored vector of a record (a scalar subquery). */
	def private String queryVector(Query q, QueryInput input, VectorCollection collection, Table table, Column column) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR,
			case SPARSE_VECTOR: {
				if ((input.kind == EQueryInputMode.SPARSE_VECTOR) != column.vec.sparse) {
					throw Unsupported.of(input.kind.literal + ' input on ' + (if (column.vec.sparse) 'sparse' else 'dense') + ' field ' + column.name)
				}
				q.vector(convert(input.value, column), column.vec)
			}
			case ID: {
				val source = schema.table(input.lookupCollection ?: collection)
				val stored = source.column(column.name)
				'(SELECT x.' + OracledbSql.id(stored.name) + ' FROM ' + OracledbSql.table(source) + ' x WHERE x.' + OracledbSql.id(source.key) + ' = '
					+ q.bind(Literals.of(input.value), source.keyColumn.kind) + ')'
			}
			case TEXT: throw Unsupported.of('text input' + NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; ' + PENDING + ' (rows of chunk vectors ranked with FETCH ... PARTITIONS BY)')
			default: throw Unsupported.of(input.kind.literal + ' input' + NO_MODELS)
		}
	}

	/**
	 * The table a read sees: as of a past SCN (a number) or a past time (a string, an ISO 8601 timestamp with offset), or the
	 * current table without an 'as-of' clause.
	 */
	def private Table past(Table table, Literal value) {
		if (value === null) return table
		val type = if (value instanceof ParamRef) (value as ParamRef).param.type else null
		val scn = value instanceof NumberLit || type == EParamType.INT || type == EParamType.FLOAT
		if (!scn && !(value instanceof StringLit) && type != EParamType.TEXT) {
			throw Unsupported.of("an 'as-of' value that is neither a number (an SCN) nor a string (a timestamp)")
		}
		table.at(new Flashback(Literals.of(value), scn))
	}

	/**
	 * Fuzziness AUTO as Oracle Text's FUZZY with its default similarity score and expansion count; FUZZY takes a similarity score from
	 * 1 to 80 and no edit distance (Text Reference, FUZZY), so numeric fuzziness has no Oracle counterpart.
	 */
	def private boolean fuzzyOf(KeywordSearch search, String op) {
		if (search.fuzziness === null) return false
		if (search.fuzziness.toUpperCase != 'AUTO') {
			throw Unsupported.of('fuzziness ' + search.fuzziness + "; Oracle Text's FUZZY operator takes a similarity score from 1 to 80, not an edit distance, and its reference gives no conversion (fuzziness AUTO maps to FUZZY with its defaults)")
		}
		if (op == 'phrase') throw Unsupported.of("fuzziness with text-query phrase; Oracle Text's FUZZY operator expands single words")
		true
	}

	/** Slop of a phrase as the max_span of NEAR with the words in order (Text Reference, NEAR: at most 100); slop 0 is the phrase. */
	def private Integer spanOf(KeywordSearch search, String op) {
		if (!written(search, 'slop')) return null
		if (op != 'phrase') throw Unsupported.of('slop without text-query phrase; slop is the distance allowed between the words of a phrase')
		if (search.slop > 100) throw Unsupported.of('slop above 100; Oracle Text returns an error for a NEAR span greater than 100')
		if (search.slop == 0) null else search.slop
	}

	/** A search that highlights first makes CTX_DOC.SNIPPET read its text keys as ROWIDs in the session. */
	def private List<Stmt> highlighting(Shape shape, Stmt search) {
		val List<Stmt> result = newArrayList
		if (!shape.highlights.empty) result += OracledbSchema.execute(#[OracledbQueries.ROWID_KEYS])
		result += search
		result
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
			if (terms.empty) throw Unsupported.of("the 'highlight' clause on a search without text; CTX_DOC.SNIPPET marks the terms of a text query")
			val markedColumn = table.column(ModelUtil.fieldName(highlighted))
			if (!markedColumn.text) throw Unsupported.of('highlighting ' + markedColumn.name + ', which is not a full-text field')
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
			val stageWhere = OracledbSql.where(q, table, filterOf(search, collection, table, stage.filter, false).parts)
			val name = 'stage_' + stages.size
			stages += OracledbQueries.stage(q, name, table, stageColumn, stageMetric, stageQuery, OracledbSql.and(#[stageWhere, OracledbQueries.within(inner)]), stage.stageLimit)
			names += name
		}
		names
	}

	/** A field read by a sort key, group key, aggregation or boost: a payload column, or a typed sub-field of a JSON column. */
	def private Ref fieldRefOf(VectorCollection collection, Table table, FieldPath path, String purpose) {
		if (path.rawPath !== null) throw Unsupported.of('raw paths in ' + purpose + '; the generator reads declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of(purpose + ' on vector field ' + ModelUtil.fieldName(path.field))
		val column = table.column(ModelUtil.fieldName(path.field))
		if (path.subFields.empty) return new Ref(column, #[], null)
		new Ref(column, path.subFields.map[sub | ModelUtil.fieldName(sub)].toList, OracledbFilter.leafKind(path))
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
			if (fn.condition !== null) when += new OracledbFilter().condition(fn.condition)
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
			for (column : table.columns) if (column.text) result += new KeywordField(column, 1.0)
		} else {
			for (ref : refs) {
				val column = table.column(ModelUtil.fieldName(ref.field))
				if (!column.text) throw Unsupported.of('keyword search on ' + column.name + ', which is not a full-text field')
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

	/** The Oracle Text form of a text query type: match (all terms with AND, any term with ACCUM) and phrase. */
	def private String keywordOperator(String type, String operator) {
		switch type {
			case 'match': if (operator == 'or') 'match_any_term' else 'match'
			case 'phrase': 'phrase'
			case 'prefix',
			case 'phrase_prefix',
			case 'wildcard': throw Unsupported.of('text-query ' + type + '; ' + PENDING + ' (the % wildcard of Oracle Text on escaped terms)')
			case 'fuzzy': throw Unsupported.of('text-query fuzzy; ' + PENDING + ' (the FUZZY operator of Oracle Text)')
			case 'term',
			case 'query_string': throw Unsupported.of('text-query ' + type + '; ' + PENDING + ' (a query string passed to CONTAINS would need a parser that admits only safe Oracle Text operators)')
			default: throw Unsupported.of('text-query ' + type + '; Oracle Text queries have no regular-expression operator')
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
	 * A literal in the form Oracle reads for its column: sparse vectors as the JSON array [dims, [indexes], [values]] with indexes
	 * from 1 (the AI Vector Search User's Guide's SPARSE textual form; vecDSL counts from 0, as Milvus, Qdrant and Pinecone), binary
	 * vectors as the bytes of 8 bits each, first bit highest; everything else as written.
	 */
	def private Expr convert(Literal literal, Column column) {
		if (column.vec === null) return Literals.of(literal)
		if (column.vec.sparse) {
			if (literal instanceof SparseLit) {
				if (column.vec.dim <= 0) throw Unsupported.of('a sparse vector field without dim; the generator writes the dimension count of sparse vectors')
				val entries = (literal as SparseLit).entries.sortBy[e | e.index]
				return items(#[num(column.vec.dim), items(entries.map[e | num(e.index) as Expr]), items(entries.map[e | num(e.weight) as Expr])])
			}
			if (literal instanceof ParamRef) throw Unsupported.of('a sparse vector parameter; ' + PENDING + ' (conversion of the parameter to the SPARSE textual form in the runtimes)')
		}
		if (column.vec.format == 'BINARY') {
			if (literal instanceof ListLit) {
				val bits = (literal as ListLit).items.map[item | item instanceof NumberLit && (item as NumberLit).value != 0].toList
				if (bits.size % 8 != 0) throw Unsupported.of('a binary vector of ' + bits.size + ' bits; BINARY vectors have a multiple of 8 dimensions')
				val List<Expr> bytes = newArrayList
				for (var i = 0; i < bits.size; i += 8) {
					var value = 0
					for (var j = 0; j < 8; j++) if (bits.get(i + j)) value = value + (1 << (7 - j))
					bytes += num(value)
				}
				return items(bytes)
			}
			if (literal instanceof ParamRef) throw Unsupported.of('a binary vector parameter; ' + PENDING + ' (packing of the parameter\'s bits in the runtimes)')
		}
		Literals.of(literal)
	}
}
