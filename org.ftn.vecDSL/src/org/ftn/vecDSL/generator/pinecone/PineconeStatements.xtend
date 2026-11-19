package org.ftn.vecDSL.generator.pinecone

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
import org.ftn.vecDSL.vecDSL.BoolLit
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
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EFusionType
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EIndexBuildState
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

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.pinecone.PineconeSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Pinecone operations in the host-language-neutral code model. Every operation is
 * a call into the per-language runtime module, which holds the SDK and REST calls; arguments are plain data (the index
 * layout, records, Pinecone filter JSON, search legs), so the three host languages send the same requests.
 */
class PineconeStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; PineconeCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Pinecone: '
	public static val FIXED_SCHEMA = 'the Pinecone index schema is fixed at creation'
	public static val NO_ALIASES = 'collection aliases; Pinecone aliases name namespaces inside one index, not indexes'
	public static val NO_GENERATION = 'a Pinecone index has no generation API (Pinecone Assistant is a separate product over uploaded files)'
	public static val SERVERLESS = 'Pinecone serverless indexes scale replicas and shards themselves, and API 2026-07 creates no pod-based indexes'
	public static val NO_ACCESS_CONTROL = 'Pinecone authenticates every request with a project API key; keys, roles and SSO are managed in the console or the Admin API'
	public static val NO_ANALYZERS = 'Pinecone configures text analysis on each full-text field when it creates the index'
	public static val MANAGED_MODELS = 'Pinecone hosts its embedding and reranking models; there is nothing to create or drop'
	public static val METRICS = 'metrics configuration; Pinecone exports index metrics through Prometheus and Datadog integrations of the project, not through the database API'
	public static val NO_LIMITS = 'Pinecone limits are set per plan and project; the runtime checks declared field limits before each write'
	public static val NO_STORAGE = 'storage settings; serverless indexes keep their data in Pinecone-managed object storage'
	public static val NO_DATABASES = 'logical databases; an API key reaches one Pinecone project, and projects are created through the Admin API with service-account credentials'
	public static val SINGLE_TERM_PREFIX = 'text-query prefix; Pinecone query_string expands * only after a quoted phrase of at least two terms, and a single-term prefix returns no matches on a word-tokenized field'
	public static val NO_TERM_QUERY = 'text-query term; Pinecone query_string matches analyzed tokens and describes no exact-token query'
	public static val NO_WILDCARD = 'text-query wildcard; Pinecone query_string has no wildcard other than * after a quoted phrase of at least two terms'
	public static val NO_LOCAL_EMBEDDING = 'generated programs do not embed text locally; give the field a model (Pinecone integrated embedding) or the query a model clause (Pinecone Inference embed)'
	public static val STORED_TEXT = 'Pinecone stores the text of an embedded field, not its vector'
	public static val RERANK_TEXT = 'Pinecone rerank scores documents against a query text'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'groupBy' -> 'group-by',
		'sliceIndex' -> 'slice', 'minShouldMatch' -> 'min-should-match', 'exact' -> 'exact', 'boost' -> 'boost', 'params' -> 'params'
	}

	/** Why Pinecone cannot express a query clause. */
	static val REASONS = #{
		'asOf' -> 'Pinecone reads only the current state of a namespace',
		'cursor' -> 'Pinecone search returns no pagination token, and list pagination tokens follow the order of document ids, not the primary-key order of a vecDSL scroll',
		'withTotal' -> 'Pinecone search returns no total hit count',
		'explain' -> 'Pinecone search returns no score explanation',
		'highlighted' -> 'Pinecone search returns no highlighted fragments',
		'elementScope' -> 'Pinecone metadata has no nested elements',
		'aggregations' -> 'Pinecone search has no aggregations',
		'sortClauses' -> 'Pinecone orders search matches by score',
		'mmr' -> 'Pinecone search has no maximal marginal relevance',
		'boost' -> 'Pinecone search has no score functions (field values, decay); query_string boosts weight query terms only',
		'groupBy' -> 'Pinecone search has no grouping',
		'prefetches' -> 'Pinecone scores each search in one stage (score_by), without prefetch stages',
		'exact' -> 'Pinecone serverless search has no exhaustive mode',
		'minShouldMatch' -> 'Pinecone text scoring has no minimum-should-match',
		'reranker' -> 'a recommend search has examples but no query text, and ' + RERANK_TEXT,
		'sliceIndex' -> 'Pinecone lists the documents of a namespace in one sequence of pages'
	}

	/** Parameters of Pinecone Inference for a query text: embedded as a query, truncated rather than rejected when too long. */
	static val QUERY_EMBEDDING = #['input_type' -> 'query', 'truncate' -> 'END']
	/** The Starter reranker reads at most 1024 tokens of query and document and fails on longer input without truncate END. */
	static val TRUNCATING_RERANKER = 'bge-reranker-v2-m3'

	static val DEFAULT_FUSION_K = 60
	static val SCHEDULE_DAYS = #{'daily' -> 1, 'weekly' -> 7, 'monthly' -> 31}

	val Script script
	val String defaultNamespace
	val List<String> notes = newArrayList
	val PineconeSchema schema = new PineconeSchema(notes)
	val List<Program.Method> helpers = newArrayList
	var String current

	new(Script script, String defaultNamespace) {
		this.script = script
		this.defaultNamespace = defaultNamespace
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
			VectorCollection: result += schema.createCollection(concept, ifNotExists, indexDeployment)
			Connection: connection(concept)
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(METRICS)
				if (concept.connection !== null) connection(concept.connection)
				for (cluster : concept.clusters) result += define(cluster, ifNotExists)
				for (database : concept.databases) result += define(database, ifNotExists)
				if (concept.auth !== null) result += define(concept.auth, ifNotExists)
			}
			Cluster: {
				if (!concept.limits.empty) throw Unsupported.of('cluster limits; ' + NO_LIMITS)
				if (concept.replication !== null || concept.sharding !== null) throw Unsupported.of('replication and sharding; ' + SERVERLESS)
				extensionArgs(concept)
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists, indexDeployment)
			}
			Database: throw Unsupported.of(NO_DATABASES)
			Alias: throw Unsupported.of(NO_ALIASES)
			VectorIndex: result += indexSettings(concept)
			PayloadIndex: result += existingPayloadIndex(concept)
			VectorField: throw Unsupported.of('adding vector field ' + ModelUtil.fieldName(concept) + '; ' + FIXED_SCHEMA)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
			Tenant: result += createNamespace(member(concept, 'a tenant'), concept.name, ifNotExists)
			Namespace: result += createNamespace(member(concept, 'a namespace'), concept.key, ifNotExists)
			Function: throw Unsupported.of('adding a ' + concept.type.literal + ' function to an existing collection; ' + FIXED_SCHEMA)
			FileResource: throw Unsupported.of('file resources; ' + NO_ANALYZERS)
			Analyzer: throw Unsupported.of('analyzers outside a field; ' + NO_ANALYZERS)
			AnalyzerFilter: throw Unsupported.of('analyzer filters outside a field; ' + NO_ANALYZERS)
			FieldLimit: throw Unsupported.of('a field limit outside a field declaration; ' + NO_LIMITS)
			EmbeddingModel: {
				if (concept.localPath !== null) throw Unsupported.of('local model files; Pinecone runs the models it hosts')
				if (written(concept, 'chunkStrategy') || written(concept, 'chunkSize') || written(concept, 'chunkOverlap')) {
					throw Unsupported.of('chunking; Pinecone embeds each field value whole')
				}
				result += hostedModel(concept.provider, concept.modelId ?: concept.name, concept)
			}
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: result += hostedModel(concept.provider, concept.modelId ?: concept.name, concept)
			Quantization: result += quantizationSettings(concept)
			ReplicationConfig: throw Unsupported.of('replication; ' + SERVERLESS)
			ShardConfig: throw Unsupported.of('sharding; ' + SERVERLESS)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_ACCESS_CONTROL)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: result += backupSchedule(concept, ifNotExists)
			ClusterLimit: throw Unsupported.of('cluster limits; ' + NO_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits (' + concept.type.literal + '); ' + NO_LIMITS)
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	/** The program endpoint: checked here, opened by connect(). */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; Pinecone is a managed service')
		}
		if (concept.apiKey instanceof StringLit || concept.token instanceof StringLit) {
			throw Unsupported.of('an API key written in the script; set the environment variable ' + PineconePrinter.API_KEY_VARIABLE)
		}
		if (concept.host !== null) throw Unsupported.of("connection host; give the control-plane endpoint as 'uri'")
		extensionArgs(concept)
	}

	/** API-key authentication is what every generated program does; other kinds and access control are outside the API. */
	def private void authentication(AuthConfig auth) {
		if (auth.type != EAuthType.API_KEY) throw Unsupported.of(auth.type.literal + ' authentication; ' + NO_ACCESS_CONTROL)
		if (auth.token instanceof StringLit) {
			throw Unsupported.of('an API key written in the script; set the environment variable ' + PineconePrinter.API_KEY_VARIABLE)
		}
		if (auth.certificatePath !== null || auth.auditLog || auth.fieldEncryption || !auth.roles.empty) {
			throw Unsupported.of('certificates, audit logs, field encryption and roles; ' + NO_ACCESS_CONTROL)
		}
		if (auth.cmekKeyId !== null) {
			throw Unsupported.of('customer-managed keys; CMEK is enabled per organization (Enterprise) and excludes full-text fields')
		}
	}

	/** Cloud and region of new indexes, from the script's connection; the runtime supplies the defaults. */
	def private Entries indexDeployment() {
		val connection = if (script === null) null else EcoreUtil2.getAllContentsOfType(script, Connection).head
		val result = kw()
		if (connection !== null && connection.cloud !== null) result.put('cloud', text(connection.cloud))
		if (connection !== null && connection.region !== null) result.put('region', text(connection.region))
		result
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
		throw Unsupported.of(what + ' outside a collection; Pinecone adds it to an index')
	}

	def private Entries place(VectorCollection collection) {
		kw('name', text(PineconeSchema.indexName(collection)))
	}

	def private Stmt require(VectorCollection collection) {
		run(runtime('require_collection', place(collection)))
	}

	/** Pinecone indexes metadata on first write, so an index for filtering needs no change; full-text needs the schema. */
	def private List<Stmt> existingPayloadIndex(PayloadIndex index) {
		if (index.kind == EPayloadIndexKind.FULL_TEXT) {
			throw Unsupported.of('creating a full-text index on an existing collection; ' + FIXED_SCHEMA)
		}
		if (index.target.rawPath !== null || !index.target.subFields.empty) throw Unsupported.of('payload indexes on nested paths; Pinecone metadata is flat')
		val field = index.target.field
		if (!(field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val collection = ModelUtil.targetCollection(index) ?: ModelUtil.owningCollection(field)
		schema.payloadIndex(index, field as PayloadField, schema.isFullText(collection, field))
		#[require(collection)]
	}

	/** Vector index settings on an existing collection: Pinecone tunes the index, so only a metric change is refused. */
	def private List<Stmt> indexSettings(VectorIndex index) {
		val field = index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
		if (field === null) throw Unsupported.of('a vector index without a vector field')
		val collection = member(field, 'a vector index')
		if (field !== schema.denseVector(collection)) {
			throw Unsupported.of('a vector index on sparse field ' + ModelUtil.fieldName(field) + '; ' + FIXED_SCHEMA)
		}
		if (written(index, 'metric') && index.metric != schema.metric(field, ModelUtil.vectorIndex(collection, field))) {
			throw Unsupported.of('a metric change; ' + FIXED_SCHEMA)
		}
		schema.note(index, 'family', 'maxDegree', 'buildCandidates', 'searchCandidates', 'centroids', 'accelerator', 'onDisk', 'online')
		if (index.quantization !== null) notes += 'quantization on VectorIndex'
		#[require(collection)]
	}

	def private List<Stmt> quantizationSettings(Quantization concept) {
		val index = EcoreUtil2.getContainerOfType(concept, VectorIndex)
		if (index === null) throw Unsupported.of('quantization outside a vector index; Pinecone manages vector compression of its indexes')
		notes += 'quantization on VectorIndex'
		indexSettings(index)
	}

	/** Tenants and vecDSL namespaces are Pinecone namespaces of the collection's index. */
	def private List<Stmt> createNamespace(VectorCollection collection, String namespace, boolean ifNotExists) {
		val args = place(collection)
		args.put('namespace', text(PineconeSchema.namespaceName(namespace)))
		val create = run(runtime('create_namespace', args))
		if (!ifNotExists) return #[create]
		#[new Code.When(runtime('has_namespace', args), true, #[create])]
	}

	/** A hosted model is checked to exist (GET /models); integrated inference uses it by name. */
	def private List<Stmt> hostedModel(String provider, String model, EObject concept) {
		val name = PineconeSchema.hostedName(provider, model)
		extensionArgs(concept)
		#[run(runtime('require_model', kw('model', text(name))))]
	}

	/** A time-based backup schedule; a retention of n backups keeps each backup for n intervals (paid plans). */
	def private List<Stmt> backupSchedule(BackupSchedule concept, boolean ifNotExists) {
		if (concept.location !== null) throw Unsupported.of('backup locations; Pinecone stores backups in its own storage')
		val frequency = concept.interval.toLowerCase.replaceFirst('^@', '')
		val days = SCHEDULE_DAYS.get(frequency)
		if (days === null) throw Unsupported.of("backup interval '" + concept.interval + "'; Pinecone backup schedules run daily, weekly or monthly")
		if (!written(concept, 'retentionCount')) throw Unsupported.of('a backup schedule without retention; Pinecone requires a retention in days')
		val probe = place(concept.collection)
		probe.put('schedule', text(concept.name))
		val args = place(concept.collection)
		args.put('schedule', text(concept.name))
		args.put('frequency', text(frequency))
		args.put('expire_after_days', num(days * concept.retentionCount))
		val create = run(runtime('create_backup_schedule', args))
		if (!ifNotExists) return #[create]
		#[new Code.When(runtime('has_backup_schedule', probe), true, #[create])]
	}

	/** A payload field added to an existing index: metadata needs no schema, so only a default needs writing (visible on return). */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		switch field {
			VectorField: throw Unsupported.of('adding vector field ' + ModelUtil.fieldName(field) + '; ' + FIXED_SCHEMA)
			PayloadField: {
				if (field.fullText || field.analyzer !== null) throw Unsupported.of('adding full-text field ' + ModelUtil.fieldName(field) + '; ' + FIXED_SCHEMA)
				schema.checkMetadataField(field)
				if (field.defaultValue === null) return #[require(collection)]
				val name = PineconeSchema.fieldKey(field)
				#[run(runtime('update_rows', kw('collection', schema.writeLayout(collection, defaultNamespace),
					'filter', kw(name, kw('$exists', flag(false))), 'values', kw(name, Literals.of(field.defaultValue)), 'wait', flag(true))))]
			}
		}
	}

	def private List<Stmt> dropField(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('dropping the primary key; Pinecone document ids are the key text')
		if (field instanceof VectorField || schema.isFullText(collection, field)) {
			throw Unsupported.of('dropping ' + ModelUtil.fieldName(field) + ', a field of the index schema; ' + FIXED_SCHEMA)
		}
		val name = PineconeSchema.fieldKey(field)
		#[run(runtime('update_rows', kw('collection', schema.writeLayout(collection, defaultNamespace),
			'filter', kw(name, kw('$exists', flag(true))), 'remove', texts(#[name]), 'wait', flag(true))))]
	}

	def private List<Stmt> renameField(VectorCollection collection, RenameFieldOp operation) {
		val field = operation.field
		if (field === collection.primaryKey) throw Unsupported.of('renaming the primary key; Pinecone document ids are the key text')
		if (field instanceof VectorField || schema.isFullText(collection, field)) {
			throw Unsupported.of('renaming ' + ModelUtil.fieldName(field) + ', a field of the index schema; ' + FIXED_SCHEMA)
		}
		#[run(runtime('rename_field', kw('collection', schema.writeLayout(collection, defaultNamespace), 'field',
			text(PineconeSchema.fieldKey(field)), 'new_name', text(PineconeSchema.checkKey(operation.newName)), 'wait', flag(true))))]
	}

	/** Collection properties: tags and deletion protection are index settings (configure_index). */
	def private List<Stmt> setProperty(VectorCollection collection, SetPropertyOp operation) {
		val args = place(collection)
		switch operation.property {
			case TAGS: {
				args.put('tags', schema.tags(tagList(operation.value)))
				args.put('replace_tags', flag(true))
				#[run(runtime('configure_collection', args))]
			}
			case DELETION_PROTECTION: {
				if (!(operation.value instanceof BoolLit)) throw Unsupported.of('deletion-protection with a value other than true or false')
				args.put('deletion_protection', text(if ((operation.value as BoolLit).value) 'enabled' else 'disabled'))
				#[run(runtime('configure_collection', args))]
			}
			case DYNAMIC: {
				if (!(operation.value instanceof BoolLit) || !(operation.value as BoolLit).value) {
					throw Unsupported.of('dynamic false; Pinecone stores every metadata key a document carries')
				}
				#[require(collection)]
			}
			case CONSISTENCY: {
				val level = operation.value
				if (!(level instanceof StringLit) || (level as StringLit).value.toLowerCase != EConsistencyLevel.EVENTUAL.literal) {
					throw Unsupported.of('consistency other than eventual; Pinecone reads are eventually consistent (use wait on writes)')
				}
				#[require(collection)]
			}
			case DESCRIPTION: throw Unsupported.of('collection descriptions; a Pinecone index has a name and tags but no description')
			case TTL: throw Unsupported.of('collection ttl; Pinecone documents do not expire')
		}
	}

	def private List<String> tagList(Literal value) {
		if (!(value instanceof ListLit) || !(value as ListLit).items.forall[item | item instanceof StringLit]) {
			throw Unsupported.of('tags other than a list of strings')
		}
		(value as ListLit).items.map[item | (item as StringLit).value].toList
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; Pinecone alters indexes')
		if (newName !== null) throw Unsupported.of('renaming a collection; Pinecone indexes cannot be renamed')
		val collection = concept as VectorCollection
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: dropField(collection, operation.field)
			RenameFieldOp: renameField(collection, operation)
			SetPropertyOp: setProperty(collection, operation)
			AddAliasOp: throw Unsupported.of(NO_ALIASES)
			AddFunctionOp: throw Unsupported.of('adding a ' + operation.newFunction.type.literal + ' function; ' + FIXED_SCHEMA)
			DropFunctionOp: throw Unsupported.of('dropping a ' + operation.function.type.literal + ' function; ' + FIXED_SCHEMA)
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val drop = run(runtime('drop_collection', place(concept)))
				#[if (ifExists) new Code.When(runtime('has_collection', place(concept)), false, #[drop]) else drop]
			}
			Tenant: dropNamespace(member(concept, 'a tenant'), concept.name, ifExists)
			Namespace: dropNamespace(member(concept, 'a namespace'), concept.key, ifExists)
			PayloadField: dropField(member(concept, 'a field'), concept)
			BackupSchedule: {
				val args = place(concept.collection)
				args.put('schedule', text(concept.name))
				val drop = run(runtime('drop_backup_schedule', args))
				#[if (ifExists) new Code.When(runtime('has_backup_schedule', args), false, #[drop]) else drop]
			}
			VectorField: throw Unsupported.of('dropping vector field ' + ModelUtil.fieldName(concept) + '; ' + FIXED_SCHEMA)
			Alias: throw Unsupported.of(NO_ALIASES)
			Partition: throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
			VectorIndex: throw Unsupported.of('dropping a vector index; Pinecone indexes the dense vector field of every index and ' + FIXED_SCHEMA)
			PayloadIndex: throw Unsupported.of('dropping a payload index; Pinecone indexes every metadata key unless a namespace declares its filterable fields at creation')
			Function: throw Unsupported.of('dropping a ' + concept.type.literal + ' function; ' + FIXED_SCHEMA)
			Quantization: throw Unsupported.of('quantization; Pinecone manages vector compression of its indexes')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Pinecone object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; Pinecone projects are deleted in the console or the Admin API')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SERVERLESS)
			Database: throw Unsupported.of(NO_DATABASES)
			Analyzer: throw Unsupported.of('dropping an analyzer; ' + NO_ANALYZERS)
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; ' + NO_ANALYZERS)
			FieldLimit: throw Unsupported.of('dropping a field limit; ' + NO_LIMITS)
			FileResource: throw Unsupported.of('file resources; ' + NO_ANALYZERS)
			EmbeddingModel: throw Unsupported.of('dropping an embedding model; ' + MANAGED_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('dropping a reranker; ' + MANAGED_MODELS)
			ReplicationConfig: throw Unsupported.of('replication; ' + SERVERLESS)
			ShardConfig: throw Unsupported.of('sharding; ' + SERVERLESS)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('authentication; ' + NO_ACCESS_CONTROL)
			RoleBinding: throw Unsupported.of('role bindings; ' + NO_ACCESS_CONTROL)
			MetricsConfig: throw Unsupported.of(METRICS)
			ClusterLimit: throw Unsupported.of('cluster limits; ' + NO_LIMITS)
			CollectionLimit: throw Unsupported.of('collection limits; ' + NO_LIMITS)
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	def private List<Stmt> dropNamespace(VectorCollection collection, String namespace, boolean ifExists) {
		val args = place(collection)
		args.put('namespace', text(PineconeSchema.namespaceName(namespace)))
		val drop = run(runtime('drop_namespace', args))
		#[if (ifExists) new Code.When(runtime('has_namespace', args), false, #[drop]) else drop]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		throw Unsupported.of(NO_ALIASES)
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Pinecone documents do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection, namespaceOf(it)), 'rows', rows(source))
		val upsert = written(it, 'onConflict') && #[EConflictPolicy.UPDATE, EConflictPolicy.REPLACE].contains(onConflict)
		if (written(it, 'onConflict') && !upsert) args.put('on_conflict', text(onConflict.literal))
		writeOptions(it, args)
		#[new Code.Return(runtime(if (upsert) 'upsert_rows' else 'insert_rows', args))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Pinecone documents do not expire')
		schema.note(it, 'recordCountHint')
		val args = kw('collection', schema.writeLayout(collection, namespaceOf(it)), 'rows', rows(source))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		writeOptions(it, args)
		#[new Code.Return(runtime('upsert_rows', args))]
	}

	def dispatch List<Stmt> body(Update it) {
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Pinecone documents carry no version')
		schema.note(it, 'recordCountHint')
		val updated = collection
		val where = new PineconeFilter(updated, schema).where(filter)
		val args = kw('collection', schema.writeLayout(updated, namespaceOf(it)))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (values !== null) args.put('values', record(values))
		if (source !== null) args.put('rows', rows(source))
		if (!removedFields.empty) args.put('remove', texts(removedFields.map[field | removable(updated, field)]))
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) args.put('mode', text('replace'))
		arrayItems(it, args)
		writeOptions(it, args)
		#[new Code.Return(runtime('update_rows', args))]
	}

	/** array-append and array-remove take their items from the statement's set record; the runtime reads and writes back. */
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
		if (!update.appendFields.empty) args.put('append', texts(update.appendFields.map[field | PineconeSchema.fieldKey(field)]))
		if (!update.arrayRemoveFields.empty) args.put('remove_items', texts(update.arrayRemoveFields.map[field | PineconeSchema.fieldKey(field)]))
	}

	def private String removable(VectorCollection collection, Field field) {
		if (field === collection.primaryKey) throw Unsupported.of('removing the primary key')
		if (field instanceof VectorField && field === schema.denseVector(collection)) {
			throw Unsupported.of('removing the dense vector; every Pinecone document carries it')
		}
		PineconeSchema.fieldKey(field)
	}

	def dispatch List<Stmt> body(Delete it) {
		val where = new PineconeFilter(collection, schema).where(filter)
		val args = kw('collection', schema.writeLayout(collection, namespaceOf(it)))
		if (deleteAll) args.put('delete_all', flag(true))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		writeOptions(it, args)
		#[new Code.Return(runtime('delete_rows', args))]
	}

	/**
	 * Remote files (s3, gs, Azure https) load through Pinecone's bulk import of JSON Lines documents, available on paid
	 * plans; local files (file:// or a path) are read by the runtime and written as an insert or upsert.
	 */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Pinecone imports a prefix of object storage once per import')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			throw Unsupported.of('import format ' + format.literal + '; document indexes import JSON Lines files')
		}
		schema.note(it, 'parallelism')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		val slice = namespaceOf(it)
		extensionArgs(it)
		if (scheme == 'file') {
			val args = kw('collection', schema.writeLayout(collection, slice), 'location', text(location))
			if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
			if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
			if (!async) args.put('wait', flag(true))
			return #[new Code.Return(runtime('import_file', args))]
		}
		if (!#['s3', 'gs', 'https'].contains(scheme)) {
			throw Unsupported.of('import from ' + scheme + '; Pinecone imports from s3, gs and Azure Blob Storage (https)')
		}
		if (written(it, 'onConflict') && onConflict != EConflictPolicy.ERROR) {
			throw Unsupported.of('on-conflict ' + onConflict.literal + ' for imports from object storage; Pinecone imports only into new namespaces')
		}
		if (written(it, 'batchSize')) notes += 'batchSize on BulkImport, which Pinecone sizes itself'
		val args = kw('collection', schema.layout(collection, slice), 'uri', text(location))
		if (async) args.put('background', flag(true))
		#[new Code.Return(runtime('start_import', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of('multi-statement transactions; Pinecone applies each write request on its own')
	}

	/** Pinecone acknowledges every write once; 'wait' makes the runtime wait until searches see it. */
	def private void writeOptions(EObject statement, Entries args) {
		schema.note(statement, 'writeConsistency')
		if (Boolean.TRUE == feature(statement, 'waitForPersistence')) args.put('wait', flag(true))
		extensionArgs(statement)
	}

	// ---------------------------------------------------------------- operational

	/** Serverless indexes serve every namespace on demand; loading checks that the index exists. */
	def dispatch List<Stmt> body(Load it) {
		if (!partitions.empty) throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
		extensionArgs(it)
		#[require(collection)]
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of('releasing a collection; Pinecone serverless indexes have no memory residency to release')
	}

	/** An acknowledged write is committed to the index's log (its request log position); flush checks the index. */
	def dispatch List<Stmt> body(Flush it) {
		extensionArgs(it)
		#[require(collection)]
	}

	/** Pinecone compacts serverless index storage itself; compact checks the index. */
	def dispatch List<Stmt> body(Compact it) {
		extensionArgs(it)
		schema.note(it, 'targetSegments', 'async')
		#[require(collection)]
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = place(collection)
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration; the model of a Pinecone embed field cannot be changed after creation')
	}

	/** A backup of the whole index (paid plans); the snapshot name is the backup name. */
	def dispatch List<Stmt> body(Snapshot it) {
		if (written(it, 'scope') && scope != ESnapshotScope.COLLECTION) {
			throw Unsupported.of('snapshot scope ' + scope.literal + '; a Pinecone backup copies one index')
		}
		if (incremental) throw Unsupported.of('incremental snapshots; every Pinecone backup is a full copy of the index')
		if (tenant !== null) throw Unsupported.of('snapshots of a tenant; a Pinecone backup copies every namespace of the index')
		if (location !== null) throw Unsupported.of('snapshot locations; Pinecone stores backups in its own storage')
		extensionArgs(it)
		val args = place(collection)
		args.put('backup', text(PineconeSchema.resourceName(snapshotName ?: name, 'snapshot')))
		if (description !== null) args.put('description', text(description))
		#[new Code.Return(runtime('create_backup', args))]
	}

	/** Restore replaces the collection: the index is deleted and created again from the backup (paid plans). */
	def dispatch List<Stmt> body(Restore it) {
		if (written(it, 'scope') && scope != ESnapshotScope.COLLECTION) {
			throw Unsupported.of('restore scope ' + scope.literal + '; a Pinecone backup restores one index')
		}
		if (location !== null) throw Unsupported.of('snapshot locations; Pinecone stores backups in its own storage')
		extensionArgs(it)
		val args = place(collection)
		args.put('backup', text(PineconeSchema.resourceName(snapshotName, 'snapshot')))
		if (async) args.put('background', flag(true))
		#[new Code.Return(runtime('restore_backup', args))]
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		if (location !== null) throw Unsupported.of('snapshot locations; Pinecone stores backups in its own storage')
		#[new Code.Return(runtime('drop_backup', kw('backup', text(PineconeSchema.resourceName(snapshotName, 'snapshot')))))]
	}

	/** A new index with the source's schema, filled with a copy of every namespace (Starter projects have no backups). */
	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; Pinecone copies indexes')
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; Pinecone has no copy-on-write indexes')
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		extensionArgs(it)
		val args = kw('collection', schema.layout(sourceCollection, defaultNamespace), 'target',
			text(PineconeSchema.resourceName(targetName, 'clone target')))
		val deployment = indexDeployment
		if (!deployment.entries.empty) args.put('deployment', deployment)
		#[new Code.Return(runtime('clone_collection', args))]
	}

	def dispatch List<Stmt> body(RebuildIndex it) {
		throw Unsupported.of('rebuilding an index; Pinecone maintains its indexes with every write and has no rebuild call')
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of('collections derived from external sources; Pinecone stores the documents of every index itself')
	}

	def dispatch List<Stmt> body(Truncate it) {
		val target = if (namespace === null) defaultNamespace else PineconeSchema.namespaceName(namespace)
		#[new Code.Return(runtime('delete_rows', kw('collection', schema.writeLayout(collection, target), 'delete_all', flag(true))))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val args = kw('kind', text(kind.literal))
		val owner = scope
		switch kind {
			case COLLECTIONS: if (owner !== null) {
				throw Unsupported.of('listing collections in a ' + owner.eClass.name + '; Pinecone lists the indexes of a project')
			}
			case TENANTS,
			case NAMESPACES: {
				if (!(owner instanceof VectorCollection)) {
					throw Unsupported.of('listing ' + kind.literal + ' without a collection; Pinecone lists the namespaces of an index')
				}
				args.put('kind', text('namespaces'))
				args.put('scope', place(owner as VectorCollection))
			}
			case SNAPSHOTS: {
				if (owner instanceof VectorCollection) args.put('scope', place(owner as VectorCollection))
				else if (owner !== null) throw Unsupported.of('listing snapshots in a ' + owner.eClass.name + '; Pinecone lists the backups of an index or a project')
			}
			case DATABASES: throw Unsupported.of(NO_DATABASES)
			case INDEXES: throw Unsupported.of('listing indexes; a Pinecone index is the collection itself (list collections)')
			case PARTITIONS: throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
			case ALIASES: throw Unsupported.of(NO_ALIASES)
		}
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('list_objects', args))]
	}

	/** Serverless indexes index every write; an index build is complete when the index reports ready. */
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
		if (written(it, 'state') && state != EIndexBuildState.READY) {
			throw Unsupported.of('await-index until ' + state.literal + '; Pinecone reports whether an index is ready')
		}
		val args = place(collection)
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'highlighted',
			'sortClauses', 'aggregations', 'prefetches', 'exact', 'groupBy', 'boost')
		val field = searchField(collection, vectorField)
		val List<Stmt> prelude = newArrayList
		val vector = queryVector(input, collection, field, 'query_vector', prelude, namespaceOf(it), queryModel)
		val metric = indexMetric(collection, field)
		if (written(it, 'metric') && it.metric != metric) {
			throw Unsupported.of('a query metric other than the metric of the vector index; ' + FIXED_SCHEMA)
		}
		val args = kw('collection', layout(collection, it), 'field', text(PineconeSchema.searchKey(collection, field)), 'vector', vector,
			'limit', num(topK), 'metric', text(schema.metricName(metric)))
		embedClause(collection, field, args)
		scoreBounds(bounds, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, new PineconeFilter(collection, schema).where(filter))
		searchParams(params)
		extensionArgs(it)
		if (reranker !== null) {
			if (input.kind != EQueryInputMode.TEXT) {
				throw Unsupported.of("the 'rerank' clause on a vector search with " + input.kind.literal + ' input; ' + RERANK_TEXT)
			}
			val embedded = PineconeSchema.embeddingModel(collection, field) !== null
			rerank(reranker, collection, Literals.of(input.value), if (embedded) PineconeSchema.searchKey(collection, field) else null, args)
		}
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('vector_search', args))
		result
	}

	/** The scoring of a field Pinecone embeds: the runtime sends the query text with embed or sparse_embed. */
	def private void embedClause(VectorCollection collection, VectorField field, Entries args) {
		val scoring = PineconeSchema.embedScoring(collection, field)
		if (scoring !== null) args.put('embed', text(scoring))
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'highlighted', 'sortClauses',
			'aggregations', 'groupBy', 'boost', 'minShouldMatch')
		if (input.kind != EQueryInputMode.TEXT) {
			throw Unsupported.of('keyword search with ' + input.kind.literal + ' input; Pinecone text scoring takes a query text')
		}
		val query = Literals.of(input.value)
		val fields = keywordFields(collection, searchFields)
		val type = if (written(it, 'textQueryType')) textQueryType else ETextQueryType.MATCH
		val required = written(it, 'matchOperator') && matchOperator == EMatchOperator.AND
		val rewritten = rewrite(collection, type, searchFields, required, if (written(it, 'fuzziness')) fuzziness else null,
			if (written(it, 'slop')) slop else null)
		val args = kw('collection', layout(collection, it), 'query', query, 'fields', texts(fields), 'limit', num(topK))
		if (rewritten !== null) args.put('rewrite', rewritten)
		else if (type == ETextQueryType.QUERY_STRING) args.put('query_string', flag(true))
		if (bounds !== null) scoreBounds(bounds, false, args)
		val where = new PineconeFilter(collection, schema).where(filter)
		common(it, collection, args, output, where)
		if (rewritten === null || type == ETextQueryType.QUERY_STRING) allTerms(required, fields, query, args)
		searchParams(params)
		extensionArgs(it)
		rerank(reranker, collection, query, fields.head, args)
		#[new Code.Return(runtime('keyword_search', args))]
	}

	/**
	 * A text query in Pinecone's query_string syntax, which the runtime builds from the query's terms:
	 * phrase, phrase prefix, fuzzy terms and regular expressions, fuzziness, slop and field boosts. Null for a match or
	 * query_string query that needs none of them, which keeps its text or query_string clause.
	 */
	def private Entries rewrite(VectorCollection collection, ETextQueryType type, List<SearchFieldRef> refs, boolean required,
		String distance, Integer proximity) {
		switch type {
			case PREFIX: throw Unsupported.of(SINGLE_TERM_PREFIX)
			case TERM: throw Unsupported.of(NO_TERM_QUERY)
			case WILDCARD: throw Unsupported.of(NO_WILDCARD)
			default: {}
		}
		if (distance !== null && type != ETextQueryType.MATCH && type != ETextQueryType.FUZZY) {
			throw Unsupported.of("the 'fuzziness' clause with text-query " + type.literal + '; Pinecone applies fuzzy distances to bare terms only')
		}
		if (proximity !== null && type != ETextQueryType.PHRASE) {
			throw Unsupported.of("the 'slop' clause with text-query " + type.literal + '; Pinecone applies slop to quoted phrases only')
		}
		if (required && type == ETextQueryType.REGEXP) {
			throw Unsupported.of("the 'operator' clause with text-query regexp; a regular expression has no terms to combine")
		}
		val boosts = fieldBoosts(collection, refs)
		val plain = type == ETextQueryType.MATCH || (type == ETextQueryType.QUERY_STRING && refs.empty)
		if (plain && distance === null && boosts.entries.empty) return null
		val result = kw('type', text(type.literal))
		if (!boosts.entries.empty) result.put('boosts', boosts)
		if (distance !== null || type == ETextQueryType.FUZZY) result.put('fuzzy', text(fuzzySuffix(distance)))
		if (proximity !== null) result.put('slop', num(proximity.doubleValue))
		if (required && (type == ETextQueryType.MATCH || type == ETextQueryType.FUZZY)) result.put('required', flag(true))
		result
	}

	/** Pinecone's fuzzy suffix: '~' for the automatic distance by term length, '~N' for 0, 1 or 2 edits. */
	def private static String fuzzySuffix(String distance) {
		if (distance === null || distance.equalsIgnoreCase('AUTO')) return '~'
		if (distance.matches('[012]')) return '~' + distance
		throw Unsupported.of("fuzziness '" + distance + "'; Pinecone takes the automatic distance (AUTO) or 0, 1 or 2 edits")
	}

	/** Field boosts as Pinecone boost literals by field key: 0 or greater, digits on both sides of a decimal point, no exponent. */
	def private Entries fieldBoosts(VectorCollection collection, List<SearchFieldRef> refs) {
		val result = entries
		for (ref : refs.filter[entry | written(entry, 'boost')]) {
			if (ref.boost < 0) throw Unsupported.of('a negative field boost; Pinecone boosts are 0 or greater')
			val literal = BigDecimal.valueOf(ref.boost).stripTrailingZeros.toPlainString
			for (field : searchedFields(collection, ref)) result.put(PineconeSchema.fieldKey(field), text(literal))
		}
		result
	}

	/** Operator 'and': every term must occur in one of the searched fields, a $match_all filter beside the scoring. */
	def private void allTerms(boolean required, List<String> fields, Expr query, Entries args) {
		if (!required) return;
		val List<Expr> matches = fields.map[name | kw(name, kw('$match_all', query)) as Expr]
		val match = if (matches.size == 1) matches.head else kw('$or', items(matches))
		args.put('filter', if (args.has('filter')) kw('$and', items(#[args.get('filter'), match])) else match)
	}

	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'highlighted', 'sortClauses',
			'aggregations', 'groupBy', 'boost')
		val fusionKind = if (written(it, 'fusion')) it.fusion else EFusionType.RRF
		val weights = legWeights(it, fusionKind)
		val List<Stmt> prelude = newArrayList
		val List<Expr> mapped = newArrayList
		var index = 0
		for (leg : legs) {
			val entry = switch leg {
				VectorLeg: vectorLeg(it, leg, 'query_vector_' + index, prelude)
				KeywordLeg: keywordLeg(it, leg)
			}
			val where = new PineconeFilter(collection, schema).where(filter).where(feature(leg, 'filter') as FilterBlock)
			if (!where.empty) entry.put('filter', where.code)
			if (written(leg, 'legLimit')) entry.put('limit', num((feature(leg, 'legLimit') as Integer).doubleValue))
			if (weights !== null) entry.put('weight', num(weights.get(index)))
			mapped += entry
			index = index + 1
		}
		val args = kw('collection', layout(collection, it), 'legs', items(mapped), 'limit', num(topK), 'fusion', text(fusionKind.literal))
		if (fusionKind == EFusionType.RRF) args.put('k', num(if (written(it, 'fusionConstant')) fusionConstant else DEFAULT_FUSION_K))
		else if (written(it, 'fusionConstant')) {
			throw Unsupported.of('fusion-k with ' + fusionKind.literal + ' fusion; vecDSL defines fusion-k for rrf only')
		}
		if (bounds !== null) scoreBounds(bounds, false, args)
		if (written(it, 'offset')) args.put('offset', num(offset))
		projection(collection, args, output, excluded, withVectors)
		searchParams(params)
		extensionArgs(it)
		if (reranker !== null) rerankFused(it, mapped, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('hybrid_search', args))
		result
	}

	/** A rerank of fused hits: the query text and rank field of the shared text input or of the first leg with a query text. */
	def private void rerankFused(HybridSearch search, List<Expr> mapped, Entries args) {
		val shared = if (search.input !== null && search.input.kind == EQueryInputMode.TEXT) Literals.of(search.input.value)
		var Expr query = shared
		var String field = null
		for (entry : mapped.filter(Entries)) {
			val leg = if (entry.has('query')) entry.get('query') else if (entry.has('embed')) entry.get('vector')
			if (leg !== null && field === null) {
				query = query ?: leg
				field = if (entry.has('query')) ((entry.get('fields') as Code.Items).items.head as Code.Text).value
					else (entry.get('field') as Code.Text).value
			}
		}
		rerank(search.reranker, search.collection, query, field, args)
	}

	/** Leg weights for weighted and relative-score fusion; rrf is unweighted by definition. */
	def private List<Double> legWeights(HybridSearch search, EFusionType fusionKind) {
		val weighted = search.legs.filter[leg | written(leg, 'weight')].size
		if (fusionKind == EFusionType.RRF) {
			if (weighted > 0 || written(search, 'alpha')) throw Unsupported.of('leg weights or alpha with rrf fusion, which is unweighted')
			return null
		}
		if (written(search, 'alpha')) {
			if (search.legs.size != 2 || !(search.legs.head instanceof VectorLeg) || weighted > 0) {
				throw Unsupported.of('alpha on other than one vector leg followed by one other leg without weights')
			}
			return #[search.alpha, BigDecimal.ONE.subtract(BigDecimal.valueOf(search.alpha)).doubleValue]
		}
		if (weighted == 0) return search.legs.map[1.0]
		if (weighted != search.legs.size) throw Unsupported.of('weights on some hybrid legs only')
		search.legs.map[leg | feature(leg, 'weight') as Double]
	}

	def private Entries vectorLeg(HybridSearch search, VectorLeg leg, String variable, List<Stmt> prelude) {
		reject(leg, 'exact', 'params')
		val field = searchField(search.collection, leg.vectorField)
		val metric = indexMetric(search.collection, field)
		if (written(leg, 'metric') && leg.metric != metric) {
			throw Unsupported.of('a query metric other than the metric of the vector index; ' + FIXED_SCHEMA)
		}
		val vector = queryVector(leg.input ?: search.input, search.collection, field, variable, prelude, namespaceOf(search), null)
		val entry = kw('kind', text('vector'), 'field', text(PineconeSchema.searchKey(search.collection, field)), 'vector', vector,
			'metric', text(schema.metricName(metric)))
		embedClause(search.collection, field, entry)
		if (leg.bounds !== null) scoreBounds(leg.bounds, false, entry)
		extensionArgs(leg)
		entry
	}

	def private Entries keywordLeg(HybridSearch search, KeywordLeg leg) {
		reject(leg, 'minShouldMatch')
		val type = if (written(leg, 'textQueryType')) leg.textQueryType else ETextQueryType.MATCH
		val required = written(leg, 'matchOperator') && leg.matchOperator == EMatchOperator.AND
		val rewritten = rewrite(search.collection, type, leg.searchFields, required, null, null)
		val keywordInput = leg.input ?: search.input
		if (keywordInput === null || keywordInput.kind != EQueryInputMode.TEXT) {
			throw Unsupported.of('a keyword leg without text input; Pinecone text scoring takes a query text')
		}
		val query = Literals.of(keywordInput.value)
		val fields = keywordFields(search.collection, leg.searchFields)
		val entry = kw('kind', text('keyword'), 'query', query, 'fields', texts(fields))
		if (rewritten !== null) entry.put('rewrite', rewritten)
		else if (type == ETextQueryType.QUERY_STRING) entry.put('query_string', flag(true))
		if (rewritten === null || type == ETextQueryType.QUERY_STRING) allTerms(required, fields, query, entry)
		extensionArgs(leg)
		entry
	}

	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'cursor', 'withTotal', 'explain', 'mmr', 'reranker', 'highlighted', 'sortClauses',
			'aggregations', 'prefetches', 'boost', 'groupBy')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val source = lookupCollection ?: collection
		val field = storedField(collection, searchField(collection, vectorField), 'recommend')
		val name = text(PineconeSchema.fieldKey(field))
		val examples = kw('collection', layout(source, it), 'field', name, 'positives', items(positives.map[fieldValue(Literals.of(it))]),
			'negatives', items(negatives.map[fieldValue(Literals.of(it))]))
		val metric = indexMetric(collection, field)
		val args = kw('collection', layout(collection, it), 'field', name, 'vector', new Code.Ref('query_vector'),
			'limit', num(topK), 'metric', text(schema.metricName(metric)))
		scoreBounds(bounds, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		val where = new PineconeFilter(collection, schema).where(filter)
		common(it, collection, args, output, where)
		if (source === collection) exclude((positives + negatives).filter[example | !(example instanceof ListLit || example instanceof SparseLit)].toList, collection, args)
		searchParams(params)
		extensionArgs(it)
		newArrayList(new Code.Let('query_vector', runtime('recommend_vector', examples)) as Stmt, new Code.Return(runtime('vector_search', args)))
	}

	/** Leaves the examples out of the hits with a $nin clause on the stored primary key. */
	def private void exclude(List<Literal> keys, VectorCollection collection, Entries args) {
		if (keys.empty) return;
		val clause = kw(PineconeSchema.fieldKey(collection.primaryKey), kw('$nin', items(keys.map[Literals.of(it)])))
		args.put('filter', if (args.has('filter')) kw('$and', items(#[args.get('filter'), clause])) else clause)
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'ids', items(ids.map[Literals.of(it)]))
		projection(collection, args, output, newArrayList, withVectors)
		extensionArgs(it)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it))
		val where = new PineconeFilter(collection, schema).where(filter)
		if (!where.empty) args.put('filter', where.code)
		extensionArgs(it)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'cursor', 'asOf', 'sliceIndex')
		val args = kw('collection', layout(collection, it))
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		common(it, collection, args, output, new PineconeFilter(collection, schema).where(filter))
		if (!sortClauses.empty) {
			val source = collection
			args.put('order', items(sortClauses.map[clause | sortKey(source, clause)]))
		}
		extensionArgs(it)
		#[new Code.Return(runtime('scroll', args))]
	}

	/** A sort clause of a scroll, applied in the runtime to metadata values. */
	def private Expr sortKey(VectorCollection collection, SortClause clause) {
		if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no scores')
		val path = clause.field
		if (path.rawPath !== null || !path.subFields.empty || !(path.field instanceof PayloadField) || schema.isFullText(collection, path.field)) {
			throw Unsupported.of('sorting by ' + (if (path.field === null) 'a raw path' else ModelUtil.fieldName(path.field)) + '; the runtime sorts by metadata fields')
		}
		val result = kw('key', text(PineconeSchema.fieldKey(path.field)))
		if (clause.direction == ESortDirection.DESC) result.put('direction', text('desc'))
		result
	}

	/** A random sample of the matching records; 'first' takes the first records in primary key order. */
	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'size', num(sampleSize))
		projection(collection, args, output, newArrayList, false)
		if (written(it, 'method') && method == ESampleMethod.FIRST) args.put('first', flag(true))
		val where = new PineconeFilter(collection, schema).where(filter)
		if (!where.empty) args.put('filter', where.code)
		extensionArgs(it)
		#[new Code.Return(runtime('sample_rows', args))]
	}

	/** A random sample with the nearest neighbours of each sampled record among the sample, as Qdrant's distance matrix. */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		val field = storedField(collection, searchField(collection, vectorField), 'distance-matrix')
		val args = kw('collection', layout(collection, it), 'field', text(PineconeSchema.fieldKey(field)),
			'metric', text(schema.metricName(indexMetric(collection, field))))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		val where = new PineconeFilter(collection, schema).where(filter)
		if (!where.empty) args.put('filter', where.code)
		extensionArgs(it)
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

	/** The Pinecone namespace a statement addresses: its tenant, its namespace, or the default namespace of the index. */
	def private String namespaceOf(EObject statement) {
		if (feature(statement, 'partition') !== null) throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
		val partitions = feature(statement, 'partitions') as List<?>
		if (partitions !== null && !partitions.empty) throw Unsupported.of(PineconeSchema.NO_PARTITIONS)
		val tenant = feature(statement, 'tenant') as Tenant
		val namespace = feature(statement, 'namespace') as String
		if (tenant !== null && namespace !== null) {
			throw Unsupported.of('a tenant and a namespace on one statement; both select a Pinecone namespace')
		}
		if (tenant !== null) return PineconeSchema.namespaceName(tenant.name)
		if (namespace !== null) return PineconeSchema.namespaceName(namespace)
		defaultNamespace
	}

	def private Entries layout(VectorCollection collection, EObject query) {
		if (written(query, 'consistency')) schema.consistency(feature(query, 'consistency') as EConsistencyLevel)
		schema.layout(collection, namespaceOf(query))
	}

	def private void common(EObject query, VectorCollection collection, Entries args, OutputSpec output, PineconeFilter where) {
		if (!where.empty) args.put('filter', where.code)
		if (written(query, 'offset')) args.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		projection(collection, args, output, excluded, Boolean.TRUE == feature(query, 'withVectors'))
	}

	/** Output fields (the key is always returned) and the vector fields to include. */
	def private void projection(VectorCollection collection, Entries args, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val key = collection.primaryKey
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val stored = storedVectors(collection)
		val listed = if (output === null || output.allFields) ModelUtil.payloadFields(collection) as List<? extends Field> else output.fields
		for (field : listed.filter[candidate | candidate !== key && !excluded.contains(candidate)]) {
			switch field {
				PayloadField: fields += PineconeSchema.fieldKey(field)
				VectorField: if (stored.contains(field)) vectors += PineconeSchema.fieldKey(field)
				else if (PineconeSchema.embeddingModel(collection, field) !== null) notes += 'output of ' + ModelUtil.fieldName(field) + ', ' + STORED_TEXT
				else notes += 'output of ' + ModelUtil.fieldName(field) + ', which Pinecone derives from full-text fields'
			}
		}
		if (withVectors) vectors += stored.map[vector | PineconeSchema.fieldKey(vector)]
		args.put('output', texts(fields))
		if (!vectors.empty) args.put('vectors', texts(vectors.stream.distinct.toList))
	}

	def private List<VectorField> storedVectors(VectorCollection collection) {
		#[schema.denseVector(collection), schema.sparseVector(collection)].filterNull.filter[vector | PineconeSchema.embeddingModel(collection, vector) === null].toList
	}

	/** Rejects clauses Pinecone lacks with their reason. */
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

	/** The dense or sparse vector field a search scores: the named one, or the dense field of the collection. */
	def private VectorField searchField(VectorCollection collection, VectorField explicit) {
		val dense = schema.denseVector(collection)
		if (explicit !== null) {
			if (PineconeSchema.isBm25Output(collection, explicit)) {
				throw Unsupported.of('vector search on the bm25 output ' + ModelUtil.fieldName(explicit) + '; use keyword search')
			}
			if (explicit !== dense && explicit !== schema.sparseVector(collection)) {
				throw Unsupported.of('vector search on ' + ModelUtil.fieldName(explicit) + ', which is not a vector field of the collection')
			}
			return explicit
		}
		if (dense === null) throw Unsupported.of('vector search on a collection without a dense vector field')
		dense
	}

	/** A vector field whose stored vectors the runtime reads: not one that Pinecone embeds from text. */
	def private VectorField storedField(VectorCollection collection, VectorField field, String statement) {
		if (PineconeSchema.embeddingModel(collection, field) !== null) {
			throw Unsupported.of(statement + ' on ' + ModelUtil.fieldName(field) + '; ' + STORED_TEXT)
		}
		field
	}

	def private static boolean sparse(VectorField field) {
		field.type == EVectorDType.SPARSE_FLOAT_VECTOR
	}

	/** The metric a field is scored by; Pinecone scores sparse vectors by dot product. */
	def private EMetric indexMetric(VectorCollection collection, VectorField field) {
		val index = ModelUtil.vectorIndex(collection, field)
		val declared = schema.metric(field, index)
		if (!sparse(field)) return declared
		if (declared != EMetric.DOT && (written(field, 'metric') || (index !== null && written(index, 'metric')))) {
			throw Unsupported.of('metric ' + declared.literal + ' on sparse field ' + ModelUtil.fieldName(field) + '; Pinecone scores sparse vectors by dot product')
		}
		EMetric.DOT
	}

	/**
	 * The query of a vector search: a dense or sparse vector, the stored vector of a record, the query text of a field Pinecone
	 * embeds, or a query text embedded with the model clause through Pinecone Inference embed.
	 */
	def private Expr queryVector(QueryInput input, VectorCollection collection, VectorField field, String variable, List<Stmt> prelude,
		String namespace, EmbeddingModel model) {
		if (input === null) throw Unsupported.of('a vector query without input')
		val name = ModelUtil.fieldName(field)
		val embedded = PineconeSchema.embeddingModel(collection, field)
		if (model !== null && input.kind != EQueryInputMode.TEXT) {
			throw Unsupported.of("the 'model' clause with " + input.kind.literal + ' input; it names the model that embeds a query text')
		}
		switch input.kind {
			case VECTOR: {
				if (embedded !== null) throw Unsupported.of('a query vector for ' + name + '; ' + STORED_TEXT + ', so it is searched by query text')
				if (sparse(field)) throw Unsupported.of('a dense query vector for sparse field ' + name)
				Literals.of(input.value)
			}
			case SPARSE_VECTOR: {
				if (!sparse(field)) throw Unsupported.of('sparse_vector query input; a Pinecone dense search scores one query vector')
				if (embedded !== null) throw Unsupported.of('a sparse query vector for ' + name + '; ' + STORED_TEXT + ', so it is searched by query text')
				fieldValue(Literals.of(input.value))
			}
			case ID: {
				storedField(collection, field, 'a query by id')
				val source = input.lookupCollection ?: collection
				prelude += new Code.Let(variable, runtime('lookup_vector', kw('collection', schema.layout(source, namespace),
					'field', text(PineconeSchema.fieldKey(field)), 'id', Literals.of(input.value))))
				new Code.Ref(variable)
			}
			case TEXT: {
				if (embedded !== null) {
					if (model !== null && model !== embedded) {
						throw Unsupported.of("the 'model' clause naming " + model.name + ' for ' + name + ', which Pinecone embeds with ' + embedded.name
							+ ', fixed at creation')
					}
					return Literals.of(input.value)
				}
				if (model === null) throw Unsupported.of('text input for vector field ' + name + ' without a model; ' + NO_LOCAL_EMBEDDING)
				prelude += new Code.Let(variable, runtime('embed_query', queryEmbedding(model, field, Literals.of(input.value))))
				new Code.Ref(variable)
			}
			default: throw Unsupported.of(input.kind.literal + ' query input; a Pinecone dense search scores one query vector')
		}
	}

	/** A query text embedded by Pinecone Inference embed with the model clause's hosted model (SPEC-I embed). */
	def private Entries queryEmbedding(EmbeddingModel model, VectorField field, Expr query) {
		if (model.localPath !== null) throw Unsupported.of('local model files; Pinecone runs the models it hosts')
		val parameters = entries
		for (pair : QUERY_EMBEDDING) parameters.put(pair.key, text(pair.value))
		if (written(model, 'dimension')) {
			if (field.dimension > 0 && model.dimension != field.dimension) {
				throw Unsupported.of('embedding model ' + model.name + ' with dim ' + model.dimension + ' for vector field '
					+ ModelUtil.fieldName(field) + ' with dim ' + field.dimension)
			}
			parameters.put('dimension', num(model.dimension))
		}
		extensionArgs(model)
		kw('model', text(PineconeSchema.hostedName(model.provider, model.modelId ?: model.name)), 'text', query, 'parameters', parameters)
	}

	/** A rerank clause: the runtime sends the hits' rank field to Pinecone Inference rerank with the query text. */
	def private void rerank(Reranker reranker, VectorCollection collection, Expr query, String defaultField, Entries args) {
		if (reranker === null) return;
		if (query === null) throw Unsupported.of("the 'rerank' clause on a search without a query text; " + RERANK_TEXT)
		val model = PineconeSchema.hostedName(reranker.provider, reranker.modelId ?: reranker.name)
		val field = if (reranker.field === null) defaultField else rankField(collection, reranker.field)
		if (field === null) throw Unsupported.of("the 'rerank' clause without a rank field; Pinecone rerank reads one text field of each hit")
		val entry = kw('model', text(model), 'query', query, 'field', text(field))
		if (written(reranker, 'topN')) entry.put('top_n', num(reranker.topN))
		if (model == TRUNCATING_RERANKER) entry.put('parameters', kw('truncate', text('END')))
		extensionArgs(reranker)
		args.put('rerank', entry)
	}

	/** The field a reranker reads: a string field of the searched collection, or the text field of an embedded vector field. */
	def private String rankField(VectorCollection collection, Field field) {
		val textual = field instanceof PayloadField && #[EScalarDType.VARCHAR, EScalarDType.TEXT].contains((field as PayloadField).type)
		val embedded = field instanceof VectorField && PineconeSchema.embeddingModel(collection, field as VectorField) !== null
		if (!collection.fields.contains(field) || !(textual || embedded)) {
			throw Unsupported.of('reranking by ' + ModelUtil.fieldName(field) + '; Pinecone rerank reads a text field of the searched collection')
		}
		if (embedded) PineconeSchema.searchKey(collection, field as VectorField) else PineconeSchema.fieldKey(field)
	}

	/** Pinecone search takes no score threshold; the runtime drops hits outside the bounds. */
	def private void scoreBounds(ScoreBounds bounds, boolean nativeScore, Entries args) {
		if (nativeScore) args.put('native', flag(true))
		if (bounds === null) return;
		if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
		if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
	}

	/** Full-text fields a keyword query scores against; without 'fields', every full-text field. */
	def private List<String> keywordFields(VectorCollection collection, List<SearchFieldRef> refs) {
		val List<String> names = newArrayList
		for (ref : refs) names += searchedFields(collection, ref).map[field | PineconeSchema.fieldKey(field)]
		if (refs.empty) names += schema.fullTextFields(collection).map[field | PineconeSchema.fieldKey(field)]
		if (names.empty) throw Unsupported.of('keyword search on a collection without full-text fields')
		names.stream.distinct.toList
	}

	/** The full-text fields a search field names: the field itself, or the inputs of the bm25 function that produces it. */
	def private List<Field> searchedFields(VectorCollection collection, SearchFieldRef ref) {
		val function = ModelUtil.producingFunction(collection, ref.field)
		val List<Field> fields = if (function !== null && function.type == EFunctionType.BM25) function.inputs else #[ref.field]
		for (field : fields) {
			if (!schema.isFullText(collection, field)) {
				throw Unsupported.of('keyword search on ' + ModelUtil.fieldName(field) + ', which is not a full-text field')
			}
		}
		fields
	}

	def private void searchParams(SearchParams params) {
		if (params === null) return;
		if (written(params, 'targetRecall')) {
			throw Unsupported.of('target-recall: Pinecone exposes no query-time recall target')
		}
		if (written(params, 'candidatePool')) {
			throw Unsupported.of('candidates; Pinecone exposes no query-time search effort')
		}
		if (params.exhaustive) throw Unsupported.of("the 'exhaustive' parameter; " + REASONS.get('exact'))
		schema.note(params, 'probes', 'scanBudget', 'timeoutMillis', 'oversampling', 'rescore', 'filterStrategy')
		extensionArgs(params)
	}

	/** Pinecone defines no vendor extension items; parameters and items in a pinecone block are rejected by name. */
	def private void extensionArgs(EObject host) {
		for (name : schema.extValues(host).keySet) throw Unsupported.of('extension parameter ' + name)
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == PineconeGenerator.VENDOR) {
				for (item : block.getItems.filter[entry | !(entry instanceof ExtParam)]) throw Unsupported.of(item.eClass.name + ' on Pinecone')
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
		for (entry : record.entries) {
			writable(entry.field)
			result.put(PineconeSchema.fieldKey(entry.field), fieldValue(Literals.of(entry.value)))
		}
		result
	}

	/** A record value for a vector field that Pinecone embeds from another field cannot be written (SPEC-C StringField). */
	def private void writable(Field field) {
		if (!(field instanceof VectorField)) return;
		val vector = field as VectorField
		val collection = ModelUtil.owningCollection(vector)
		if (collection === null || PineconeSchema.embeddingModel(collection, vector) === null) return;
		val source = PineconeSchema.embeddedText(collection, vector)
		if (source !== vector) {
			throw Unsupported.of('a value for vector field ' + ModelUtil.fieldName(vector) + ', which Pinecone embeds from field ' + ModelUtil.fieldName(source))
		}
	}

	/** Sparse vectors in Pinecone's form: parallel lists of indices and values. */
	def private Expr fieldValue(Expr value) {
		if (!(value instanceof Code.Sparse)) return value
		val weights = (value as Code.Sparse).weights()
		kw('indices', items(weights.keySet.map[index | num(index.doubleValue)].toList), 'values', items(weights.values.map[weight | num(weight)].toList))
	}
}
