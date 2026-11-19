package org.ftn.vecDSL.generator.vespa

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
import org.ftn.vecDSL.generator.vespa.VespaPackage.Schema
import org.ftn.vecDSL.generator.vespa.VespaQueries.Cond
import org.ftn.vecDSL.generator.vespa.VespaQueries.Not
import org.ftn.vecDSL.generator.vespa.VespaQueries.Params
import org.ftn.vecDSL.generator.vespa.VespaQueries.Raw
import org.ftn.vecDSL.generator.vespa.VespaQueries.Test
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
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.ESampleMethod
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESortDirection
import org.ftn.vecDSL.vecDSL.EUpdateMode
import org.ftn.vecDSL.vecDSL.EmbeddingMigration
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.ExtParam
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
import org.ftn.vecDSL.vecDSL.PhasedRankProfile
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.QueryInput
import org.ftn.vecDSL.vecDSL.RankProfileUse
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
import static org.ftn.vecDSL.generator.vespa.VespaSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to calls of the Vespa runtime in the host-language-neutral code model. Application packages
 * (services.xml, schemas, query profile), YQL texts and query request bodies are built here, with every value as a request parameter
 * that the YQL references with @name, so the three host languages send identical requests; the runtimes deploy, feed and query.
 */
class VespaStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; VespaCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Vespa: '
	/** Suffix of constructs Vespa supports that the generator does not produce yet. */
	public static val PENDING = 'has no Vespa mapping yet'
	public static val EXTERNAL = 'collections derived from external sources; Vespa stores the documents it is fed and reads no external tables'
	public static val NO_ALIASES = 'aliases; Vespa addresses a document type by its schema name and has no alias for a schema or document type'
	public static val NO_DELETION_PROTECTION = 'deletion-protection; open-source Vespa has no drop guard on a schema (a deployment that removes a schema needs the schema-removal validation override, which every generated drop writes)'
	public static val NO_STORAGE = 'storage settings; a self-hosted Vespa node keeps its data under its var directory, set on the node, not per schema'
	public static val NO_TAGS = 'collection tags; a Vespa schema carries no metadata besides its definition'
	public static val NO_TRANSACTIONS = 'transactions; Vespa applies each document operation atomically on its own and has no transaction over several documents'
	public static val SINGLE_NODE = 'the program deploys a single-node application (one node in services.xml); replication and distribution need a multi-node application with hosts.xml'
	public static val NO_USERS = 'users and roles; Vespa authorizes clients by mTLS certificates (and Vespa Cloud data-plane tokens) per endpoint and has no users or roles'
	public static val NO_RESTART = 'a content node restart (adding or removing an HNSW index, or fast-search, takes effect only after the searchnode restarts), which the HTTP APIs of a running application do not offer'
	public static val NO_SNAPSHOTS = 'snapshots; ' + PENDING + ' (visiting every document of the schema into a local JSON Lines file, and feeding it back to restore; backups are a Vespa Cloud feature)'
	static val NO_GENERATION = 'generated programs deploy no models: generation needs an LLM client component in services.xml, with a local model file in the application package or an external service and its API key'
	static val NO_RERANKERS = 'generated programs deploy no models: a cross-encoder reranks as an ONNX model in a global-phase rank expression, which needs the model file in the application package'
	static val NO_METRICS = 'metrics configuration; Vespa reports metrics through /state/v1/metrics and the metrics proxy configured in services.xml, not through objects a client creates'
	static val NO_SCHEDULES = 'backup schedules; open-source Vespa has no backup or snapshot scheduler'
	static val NO_CLUSTER_LIMITS = 'cluster limits; Vespa has no setting that limits the number of schemas'
	static val NO_FILES = 'file resources; ' + PENDING + ' (files in the application package read by a linguistics or model component)'
	static val NO_RELEASE = 'release; Vespa keeps a deployed schema\'s attributes in memory and has no request that evicts one document type'
	static val NO_DYNAMIC = 'dynamic collections; a Vespa document type takes its declared fields only (others are dropped, X-Vespa-Ignored-Fields)'
	static val NO_RENAME = PENDING + ' (Vespa has no rename: a new schema or field filled by visiting every document, then the old one removed)'

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total', 'explain' -> 'explain',
		'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight', 'sortClauses' -> 'sort', 'aggregations' -> 'aggregate',
		'prefetches' -> 'prefetch', 'groupBy' -> 'group-by', 'boost' -> 'boost', 'queryModel' -> 'model',
		'minShouldMatch' -> 'min-should-match', 'fuzziness' -> 'fuzziness', 'slop' -> 'slop', 'sliceIndex' -> 'slice'
	}

	/** Why Vespa cannot express a clause. */
	static val REASONS = #{
		'asOf' -> 'Vespa keeps only the current version of each document',
		'reranker' -> NO_RERANKERS,
		'queryModel' -> VespaSchema.NO_EMBEDDER,
		'minShouldMatch' -> 'userInput combines the terms with and, or, weakAnd or phrase and has no minimum number of matching terms',
		'fuzziness' -> VespaFilter.NO_FUZZY
	}

	static val NO_TEXT = "the 'highlight' clause on a search without text; dynamic summaries mark the terms of a text query"

	/** Clauses Vespa supports that the generator does not produce yet, with the feature that would carry them. */
	static val PENDING_CLAUSES = #{
		'elementScope' -> 'sameElement over array<struct> fields, or the closest cell of a mixed tensor',
		'mmr' -> 'MMR over the candidates in the runtime',
		'aggregations' -> 'grouping with output(count(), sum(), avg(), min(), max())',
		'prefetches' -> 'staged queries or a second-phase expression',
		'boost' -> 'a first-phase expression over the attributes'
	}

	/** The document summary whose vecdsl_hl_ fields bold the matched terms of the full-text fields (VespaPackage.HIGHLIGHT_SUMMARY). */
	static val NO_HIGHLIGHT_FIELD = ', which is not a full-text field; bolding marks the matched terms of index fields of type string'

	/** Query settings an extension vespa block may set on a search, by key: the nearestNeighbor annotation or ranking.matching key. */
	static val SEARCH_SETTINGS = #{'explore_additional_hits' -> 'hnsw.exploreAdditionalHits', 'post_filter_threshold' -> 'postFilterThreshold',
		'approximate_threshold' -> 'approximateThreshold', 'filter_first_threshold' -> 'filterFirstThreshold',
		'filter_first_exploration' -> 'filterFirstExploration', 'target_hits_max_adjustment_factor' -> 'targetHitsMaxAdjustmentFactor'}

	val List<String> notes = newArrayList
	val VespaSchema schema
	val Map<VectorCollection, Integer> ttls = newHashMap
	val List<Program.Method> helpers = newArrayList
	var String currentMethod

	new(Script script) {
		this.schema = new VespaSchema(notes, script)
	}

	/** The method for one statement, followed by helper methods it calls (batch queries). */
	def List<Program.Method> methods(Method statement, String name) {
		notes.clear
		helpers.clear
		currentMethod = name
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
			case 'bm25': 'Vespa computes BM25 over index fields itself (bm25(field) with enable-bm25) and has no function that writes sparse vectors'
			case 'rerank': NO_RERANKERS
			case 'custom': PENDING + ' (an indexing statement over the input fields)'
			default: 'embedding functions' + VespaSchema.NO_MODELS
		}
	}

	def static String collectionLimit(String type) {
		'collection limits (' + type + '); a Vespa schema has no limit settings (tensor sizes are part of the field type)'
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
			VectorCollection: result += createCollections(#[concept], ifNotExists, fresh)
			Connection: connection(concept)
			VectorDatabase: {
				if (concept.metrics !== null) throw Unsupported.of(NO_METRICS)
				if (concept.connection !== null) connection(concept.connection)
				if (concept.auth !== null) authentication(concept.auth)
				val List<VectorCollection> collections = newArrayList
				for (cluster : concept.clusters) collections += clusterCollections(cluster)
				for (database : concept.databases) collections += database.collections
				if (!collections.empty) result += createCollections(collections, ifNotExists, fresh)
			}
			Cluster: {
				val collections = clusterCollections(concept)
				if (!collections.empty) result += createCollections(collections, ifNotExists, fresh)
			}
			Database: {
				if (concept.collections.empty) throw Unsupported.of('a logical-db without collections; a logical-db is a prefix of the document type names of its collections')
				result += createCollections(concept.collections, ifNotExists, fresh)
			}
			Alias: throw Unsupported.of(NO_ALIASES)
			VectorIndex: throw Unsupported.of('creating a vector index on an existing collection; it needs ' + NO_RESTART)
			PayloadIndex: result += payloadIndex(concept)
			VectorField: result += addField(member(concept, 'a field'), concept)
			PayloadField: result += addField(member(concept, 'a field'), concept)
			Partition: result += require(member(concept, 'a partition'))
			Tenant: result += require(member(concept, 'a tenant'))
			Namespace: result += require(member(concept, 'a namespace'))
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			FileResource: throw Unsupported.of(NO_FILES)
			Analyzer: {
				VespaSchema.checkAnalyzer(concept)
				result += require(member(concept, 'an analyzer'))
			}
			AnalyzerFilter: {
				val analyzer = EcoreUtil2.getContainerOfType(concept, Analyzer)
				if (analyzer === null) throw Unsupported.of('an analyzer filter outside an analyzer')
				VespaSchema.checkFilter(concept.type.literal)
				result += require(member(analyzer, 'an analyzer'))
			}
			FieldLimit: throw Unsupported.of('field limits; Vespa validates the value types of a document, not their sizes')
			EmbeddingModel: throw Unsupported.of('embedding models' + VespaSchema.NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			Quantization: throw Unsupported.of('quantization outside a vector index; ' + PENDING + ' (binarize | pack_bits into a hamming field)')
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_NODE)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_NODE)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: authentication(concept)
			RoleBinding: throw Unsupported.of(NO_USERS)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
		result
	}

	def private List<VectorCollection> clusterCollections(Cluster cluster) {
		if (!cluster.limits.empty) throw Unsupported.of(NO_CLUSTER_LIMITS)
		if (cluster.replication !== null || cluster.sharding !== null) throw Unsupported.of('replication and sharding; ' + SINGLE_NODE)
		for (key : ModelUtil.extParams(cluster, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		cluster.collections
	}

	/** The collections' schemas join the package, which is deployed once for all of them. */
	def private List<Stmt> createCollections(List<VectorCollection> collections, boolean ifNotExists, boolean fresh) {
		val List<String> created = newArrayList
		for (c : collections) created += declare(c, fresh).name
		#[schema.deploy(#[], created, ifNotExists, #[], false)]
	}

	def private Schema declare(VectorCollection c, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(EXTERNAL)
		if (c.ttlField !== null) throw Unsupported.of('ttl-field; ' + PENDING + ' (a garbage-collection selection on the field, <type>.<field> > now())')
		if (c.autoId) throw Unsupported.of('auto-id; ' + PENDING + ' (document ids generated by the runtime)')
		if (c.deletionProtection) throw Unsupported.of(NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; ' + PENDING + ' (a reference field to a global document type and imported fields)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + VespaSchema.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of(NO_STORAGE)
		if (!c.tags.empty) throw Unsupported.of(NO_TAGS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + functionReason(c.functions.head.type.literal))
		if (!c.limits.empty) throw Unsupported.of(collectionLimit(c.limits.head.type.literal))
		if (!c.aliases.empty) throw Unsupported.of(NO_ALIASES)
		if (c.dynamic) throw Unsupported.of(NO_DYNAMIC)
		if (!ModelUtil.partitions(c).filter[it.key !== null || !subPartitions.empty || (written(it, 'strategy') && strategy.literal != 'list')].empty) {
			throw Unsupported.of('partitions by key, with a strategy other than list, or with sub-partitions; a partition is a value of the hidden vecdsl_partition attribute')
		}
		schema.note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency', 'description')
		if (fresh) schema.evolve(c, null)
		schema.declaring = true
		val declared = try schema.schema(c) finally schema.declaring = false
		for (payloadIndex : c.indexes.filter(PayloadIndex)) schema.checkPayloadIndex(c, payloadIndex)
		if (written(c, 'ttlSeconds')) ttls.put(c, c.ttlSeconds)
		schema.put(declared)
		declared
	}

	/** The program endpoint: checked here, opened by connect(). A bearer token comes from the environment only. */
	def private void connection(Connection concept) {
		if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
			throw Unsupported.of('embedded deployments; Vespa runs as a service, and the programs use its HTTP APIs')
		}
		if (concept.apiKey !== null || concept.token !== null) {
			throw Unsupported.of('api keys and tokens on the connection; a self-hosted Vespa node authenticates clients with mTLS certificates (data-plane tokens are a Vespa Cloud feature; the runtime sends VECDSL_VESPA_TOKEN when it is set)')
		}
		if (concept.username !== null || concept.password !== null) throw Unsupported.of('users and passwords; Vespa has no password authentication')
		if (concept.databaseName !== null) throw Unsupported.of('a database name on the connection; a vecDSL logical-db is a prefix of the document type names')
		if (concept.grpcPort > 0) throw Unsupported.of('grpc-port; the generated programs use the HTTP APIs (document/v1, query, deploy)')
		for (key : ModelUtil.extParams(concept, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (concept.uri !== null && VespaGenerator.endpoint(concept) === null) {
			throw Unsupported.of('the connection uri ' + concept.uri + '; Vespa programs take an http or https URL with a host, or host:port (read as http://host:port)')
		}
		schema.note(concept, 'cloud', 'region', 'project')
	}

	/** none needs nothing; the other methods are not part of a single-node open-source deployment the program can configure. */
	def private void authentication(AuthConfig auth) {
		switch auth.type {
			case NONE: {}
			case MTLS: throw Unsupported.of('mtls authentication; ' + PENDING + ' (client certificates in the runtimes and a TLS-enabled node configured outside the application package)')
			case TOKEN: throw Unsupported.of('token authentication; data-plane tokens are a Vespa Cloud feature (a self-hosted node authenticates clients with mTLS)')
			default: throw Unsupported.of(auth.type.literal + ' authentication; Vespa authenticates clients with mTLS certificates (and Vespa Cloud data-plane tokens) and has no ' + auth.type.literal + ' method')
		}
		if (auth.type != EAuthType.NONE && auth.token instanceof StringLit) throw Unsupported.of('a secret written in the script; read it from an environment variable with env(...)')
		if (auth.certificatePath !== null) throw Unsupported.of('client certificates; ' + PENDING + ' (client certificates in the runtimes)')
		if (auth.cmekKeyId !== null || auth.fieldEncryption) throw Unsupported.of('customer-managed keys and field encryption; open-source Vespa has no encryption at rest of its own (encryption is a property of the disk)')
		if (!auth.roles.empty) throw Unsupported.of(NO_USERS)
		schema.note(auth, 'auditLog')
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
		throw Unsupported.of(what + ' outside a collection; it belongs to a collection\'s schema')
	}

	/** Existence check of a collection: its schema file in the active application package. */
	def private List<Stmt> require(VectorCollection collection) {
		#[new Code.Run(runtime('require_collection', kw('document_type', text(schema.schema(collection).name))))]
	}

	/** A payload index on an existing collection: the field's attribute already serves its filters; fast-search would need a restart. */
	def private List<Stmt> payloadIndex(PayloadIndex index) {
		val collection = ModelUtil.targetCollection(index) ?: (if (index.target.field === null) null else ModelUtil.owningCollection(index.target.field))
		if (collection === null) throw Unsupported.of('a payload index outside a collection')
		schema.checkPayloadIndex(collection, index)
		notes += 'fast-search on PayloadIndex, which a deployed attribute gets only after a content node restart (filters work without it)'
		require(collection)
	}

	/** A new field joins the schema; Vespa adds a field to a deployed schema without a validation override. */
	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val before = schema.schema(collection)
		var after = before
		for (added : schema.fieldsOf(collection, field)) after = after.with(added)
		schema.evolve(collection, after)
		schema.put(after)
		#[schema.deploy(#[], #[], false, #[], false)]
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (concept instanceof Alias) throw Unsupported.of(NO_ALIASES)
		if (!(concept instanceof VectorCollection)) throw Unsupported.of('altering a ' + concept.eClass.name + '; the generator alters collections')
		val collection = concept as VectorCollection
		if (newName !== null) throw Unsupported.of('renaming a collection; ' + NO_RENAME)
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		if (result.empty) result += require(collection)
		result
	}

	def List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val before = schema.schema(collection)
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: {
				val named = schema.name(collection, operation.field)
				if (named == before.key) throw Unsupported.of('dropping the primary key; a vecDSL collection keeps its key')
				val after = before.without(named)
				schema.evolve(collection, after)
				schema.put(after)
				notes += 'drop-field on DropFieldOp keeps the removed field\'s data on the content node; a field added later under the same name and type may show it again'
				#[schema.deploy(#[], #[], false, #[], false)]
			}
			RenameFieldOp: throw Unsupported.of('rename-field; ' + NO_RENAME)
			SetPropertyOp: setProperty(collection, operation)
			AddAliasOp: throw Unsupported.of(NO_ALIASES)
			AddFunctionOp: throw Unsupported.of(operation.newFunction.type.literal + ' functions; ' + functionReason(operation.newFunction.type.literal))
			DropFunctionOp: throw Unsupported.of(operation.function.type.literal + ' functions; ' + functionReason(operation.function.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	/**
	 * ttl: the expiry time of every existing document is set (an update where the selection is true), and later writes of the program
	 * use the ttl; the deployed schema already has the expiry attribute and the garbage-collection selection.
	 */
	def private List<Stmt> setProperty(VectorCollection collection, SetPropertyOp operation) {
		switch operation.property {
			case TTL: {
				if (!(operation.value instanceof NumberLit)) throw Unsupported.of('a ttl that is not a number literal')
				val seconds = (operation.value as NumberLit).value as int
				ttls.put(collection, seconds)
				#[new Code.Return(runtime('expire', kw('document_type', text(schema.schema(collection).name), 'seconds', num(seconds)))) as Stmt]
			}
			case DESCRIPTION: {
				notes += 'description on SetPropertyOp, which a Vespa schema does not store'
				require(collection)
			}
			case CONSISTENCY: require(collection)
			case DYNAMIC: throw Unsupported.of(NO_DYNAMIC)
			case TAGS: throw Unsupported.of(NO_TAGS)
			case DELETION_PROTECTION: throw Unsupported.of(NO_DELETION_PROTECTION)
		}
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: dropCollections(#[concept], ifExists)
			Database: {
				if (concept.collections.empty) throw Unsupported.of('dropping a logical-db without collections; a logical-db is a prefix of the document type names of its collections')
				dropCollections(concept.collections, ifExists)
			}
			Tenant: dropSlice(member(concept, 'a tenant'), VespaPackage.TENANT, concept.name)
			Namespace: dropSlice(member(concept, 'a namespace'), VespaPackage.NAMESPACE, concept.key)
			Partition: dropSlice(member(concept, 'a partition'), VespaPackage.PARTITION, concept.name)
			PayloadField: alter(member(concept, 'a field'), dropOp(concept))
			VectorField: alter(member(concept, 'a field'), dropOp(concept))
			Alias: throw Unsupported.of(NO_ALIASES)
			VectorIndex: throw Unsupported.of('dropping a vector index; it needs ' + NO_RESTART)
			PayloadIndex: throw Unsupported.of('dropping a payload index; removing fast-search from an attribute needs ' + NO_RESTART)
			Function: throw Unsupported.of(concept.type.literal + ' functions; ' + functionReason(concept.type.literal))
			Quantization: throw Unsupported.of('dropping quantization; ' + PENDING + ' (binarize | pack_bits into a hamming field)')
			Connection: throw Unsupported.of('dropping a connection; a connection is the program endpoint, not a Vespa object')
			VectorDatabase: throw Unsupported.of('dropping a deployment; the program drops schemas and documents, not the Vespa node')
			Cluster: throw Unsupported.of('dropping a cluster; ' + SINGLE_NODE)
			Analyzer: throw Unsupported.of('dropping an analyzer; Vespa\'s linguistics belongs to the node (drop the full-text fields)')
			AnalyzerFilter: throw Unsupported.of('dropping an analyzer filter; Vespa\'s linguistics belongs to the node')
			FieldLimit: throw Unsupported.of('field limits; Vespa validates the value types of a document, not their sizes')
			FileResource: throw Unsupported.of(NO_FILES)
			EmbeddingModel: throw Unsupported.of('embedding models' + VespaSchema.NO_MODELS)
			GenerativeModel: throw Unsupported.of('generative models; ' + NO_GENERATION)
			Reranker: throw Unsupported.of('rerankers; ' + NO_RERANKERS)
			ReplicationConfig: throw Unsupported.of('replication; ' + SINGLE_NODE)
			ShardConfig: throw Unsupported.of('sharding; ' + SINGLE_NODE)
			StorageConfig: throw Unsupported.of(NO_STORAGE)
			AuthConfig: throw Unsupported.of('dropping authentication; TLS and certificates are configured on the Vespa node')
			RoleBinding: throw Unsupported.of(NO_USERS)
			MetricsConfig: throw Unsupported.of(NO_METRICS)
			BackupSchedule: throw Unsupported.of(NO_SCHEDULES)
			ClusterLimit: throw Unsupported.of(NO_CLUSTER_LIMITS)
			CollectionLimit: throw Unsupported.of(collectionLimit(concept.type.literal))
			default: throw Unsupported.of(NOT_MAPPED + 'dropping a ' + concept.eClass.name)
		}
	}

	/** The schemas leave the package; the runtime deletes their documents, then deploys with the schema-removal override. */
	def private List<Stmt> dropCollections(List<VectorCollection> collections, boolean ifExists) {
		val List<String> dropped = newArrayList
		for (c : collections) {
			val type = schema.schema(c).name
			schema.remove(type)
			schema.evolve(c, null)
			ttls.remove(c)
			dropped += type
		}
		#[schema.deploy(#['schema-removal'], #[], false, dropped, ifExists)]
	}

	def private DropFieldOp dropOp(Field field) {
		val operation = org.ftn.vecDSL.vecDSL.VecDSLFactory.eINSTANCE.createDropFieldOp
		operation.field = field
		operation
	}

	/** Dropping a tenant, namespace or partition deletes its documents (the ids found by a query on the hidden attribute). */
	def private List<Stmt> dropSlice(VectorCollection collection, String attribute, String value) {
		val doc = schema.schema(collection)
		val restriction = new VespaFilter(schema, collection).slice(attribute, #[text(value)])
		val args = kw('document_type', text(doc.name), 'key', text(doc.key))
		args.put('select', select(doc, restriction.parts, null))
		#[new Code.Return(runtime('delete', args))]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		throw Unsupported.of(NO_ALIASES)
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
		schema.note(it, 'recordCountHint', 'batchSize')
		writeOptions(it)
		val args = writeArgs(it, collection, source, 'replace', 'upsert_count', if (written(it, 'ttlSeconds')) ttlSeconds else null)
		#[new Code.Return(runtime('write', args))]
	}

	/** The arguments of a write: document type, key field, records, conflict policy, counter, vector sizes, slices, expiry and version. */
	def private Entries writeArgs(EObject statement, VectorCollection collection, RecordSource source, String policy, String counter, Integer ttl) {
		val doc = schema.schema(collection)
		val args = kw('document_type', text(doc.name), 'key', text(doc.key), 'records', rows(source, collection), 'policy', text(policy),
			'counter', text(counter))
		vectorSizes(args, collection)
		val slices = slices(statement, collection)
		if (!slices.entries.empty) args.put('slices', slices)
		expiry(args, collection, ttl)
		if (schema.isVersioned(collection)) args.put('versioned', flag(true))
		args
	}

	def private void vectorSizes(Entries args, VectorCollection collection) {
		val sizes = entries
		for (field : ModelUtil.vectorFields(collection)) {
			val named = schema.name(collection, field)
			if (schema.schema(collection).has(named)) sizes.put(named, num(field.dimension))
		}
		if (!sizes.entries.empty) args.put('vectors', sizes)
	}

	/** Documents of an expiring collection carry their expiry time: now plus the ttl, or none (the far future) without one. */
	def private void expiry(Entries args, VectorCollection collection, Integer ttl) {
		if (!schema.schema(collection).expiring) return;
		args.put('expiring', flag(true))
		val seconds = ttl ?: ttls.get(collection)
		if (seconds !== null) args.put('ttl', num(seconds))
	}

	/** Hidden attribute values for tenant, namespace and partition of a write; partitioned collections default to the default partition. */
	def private Entries slices(EObject statement, VectorCollection collection) {
		val doc = schema.schema(collection)
		val result = entries
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) {
			if (!doc.has(VespaPackage.TENANT)) throw Unsupported.of('a tenant of a collection without tenants')
			result.put(VespaPackage.TENANT, text(tenant.name))
		}
		val namespace = feature(statement, 'namespace') as String
		if (namespace !== null) result.put(VespaPackage.NAMESPACE, text(namespace))
		val partition = feature(statement, 'partition') as Partition
		if (partition !== null && !doc.has(VespaPackage.PARTITION)) throw Unsupported.of('a partition of a collection declared without partitions')
		if (doc.has(VespaPackage.PARTITION)) {
			result.put(VespaPackage.PARTITION, text(partition?.name ?: collection.defaultPartition?.name
				?: ModelUtil.partitions(collection).findFirst[isDefault]?.name ?: '_default'))
		}
		result
	}

	/**
	 * Partial updates with assign, add and remove (arrays) and assign of null (remove-fields), each with a test-and-set condition: the
	 * document type alone (an update of a missing document then answers 412 and is not counted) or the version (if-version).
	 */
	def dispatch List<Stmt> body(Update it) {
		schema.note(it, 'recordCountHint')
		writeOptions(it)
		val doc = schema.schema(collection)
		val args = kw('document_type', text(doc.name), 'key', text(doc.key))
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
					val named = schema.name(collection, entry.field)
					if (named == doc.key) throw Unsupported.of('updating the primary key; the key is the user part of the document id')
					if (entry.value instanceof SparseLit) throw Unsupported.of('sparse vector values; ' + PENDING + ' (mapped tensors)')
					if (appendFields.contains(entry.field)) {
						arrayField(collection, entry.field)
						append.put(named, listOf(entry.value))
					} else if (arrayRemoveFields.contains(entry.field)) {
						arrayField(collection, entry.field)
						remove.put(named, listOf(entry.value))
					} else {
						set.put(named, Literals.of(entry.value))
					}
				}
			}
			for (field : appendFields + arrayRemoveFields) {
				if (!append.has(schema.name(collection, field)) && !remove.has(schema.name(collection, field))) {
					throw Unsupported.of('array-append or array-remove on ' + schema.name(collection, field) + " without items in 'set'")
				}
			}
			val List<String> cleared = newArrayList
			for (field : removedFields) {
				val named = schema.name(collection, field)
				if (named == doc.key) throw Unsupported.of('removing the primary key')
				cleared += named
			}
			if (written(it, 'mode') && mode == EUpdateMode.REPLACE) {
				for (field : doc.documentFields) {
					if (field.name != doc.key && !field.vector && !field.name.startsWith('vecdsl_') && !set.has(field.name)
						&& !append.has(field.name) && !remove.has(field.name) && !cleared.contains(field.name)) cleared += field.name
				}
			}
			if (set.entries.empty && append.entries.empty && remove.entries.empty && cleared.empty) throw Unsupported.of("an update without 'set' or 'remove-fields'")
			if (!set.entries.empty) args.put('set', set)
			if (!append.entries.empty) args.put('append', append)
			if (!remove.entries.empty) args.put('remove', remove)
			if (!cleared.empty) args.put('clear', texts(cleared))
		}
		vectorSizes(args, collection)
		if (written(it, 'ifVersion')) args.put('version', num(ifVersion))
		if (schema.isVersioned(collection)) args.put('versioned', flag(true))
		val restriction = filterOf(it, collection, filter)
		if (!ids.empty) {
			if (!restriction.parts.empty) {
				val List<Cond> parts = newArrayList(restriction.parts)
				args.put('select', select(doc, parts, ids.map[Literals.of(it)]))
			} else {
				args.put('ids', items(ids.map[Literals.of(it)]))
			}
		} else if (!restriction.parts.empty) {
			args.put('select', select(doc, restriction.parts, null))
		} else if (source === null) {
			throw Unsupported.of('an update without ids or a filter')
		}
		#[new Code.Return(runtime('update', args))]
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
		val doc = schema.schema(collection)
		val args = kw('document_type', text(doc.name), 'key', text(doc.key))
		val restriction = filterOf(it, collection, filter)
		if (!ids.empty) {
			if (!restriction.parts.empty || written(it, 'deleteLimit')) {
				args.put('select', select(doc, newArrayList(restriction.parts), ids.map[Literals.of(it)]))
				if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
			} else {
				args.put('ids', items(ids.map[Literals.of(it)]))
			}
		} else if (!restriction.parts.empty || written(it, 'deleteLimit')) {
			args.put('select', select(doc, restriction.parts, null))
			if (written(it, 'deleteLimit')) args.put('limit', num(deleteLimit))
		} else if (deleteAll) {
			args.put('all', flag(true))
		} else {
			throw Unsupported.of("a delete without ids, filter or 'all'")
		}
		#[new Code.Return(runtime('delete', args))]
	}

	/**
	 * The query that selects the documents of a filtered update or delete: the keys of the matching documents in key order, page by
	 * page (a list of ids restricts it further); the runtime then writes each document by id.
	 */
	def private Entries select(Schema doc, List<Cond> parts, List<Expr> ids) {
		val List<Cond> all = newArrayList(parts)
		if (ids !== null) {
			val keyField = doc.field(doc.key)
			all += new Test(keyField.name, keyField.kind, 'in', items(ids), null)
		}
		if (doc.expiring) all += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val bound = new Params
		val where = VespaQueries.where(all, bound)
		val args = kw('body', VespaQueries.body(VespaQueries.select(doc.name, where, #[doc.key]), VespaQueries.PAGE, 0, 'unranked', null))
		bound.into(args)
		if (doc.expiring) args.put('clock', flag(true))
		args
	}

	/** A local JSON or JSON Lines file fed record by record through document/v1, which takes one document per request. */
	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous import; Vespa has no ingestion pipeline that pulls from a source (clients feed the documents)')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('import from ' + scheme + '; the runtime reads local files (Vespa reads no object stores; clients feed the documents)')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			if (format == EImportFormat.CSV) throw Unsupported.of('import format csv; ' + PENDING + ' (CSV rows typed by the collection fields)')
			throw Unsupported.of('import format ' + format.literal + '; the runtime reads JSON and JSON Lines files')
		}
		schema.note(it, 'parallelism', 'async', 'batchSize')
		for (key : ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val doc = schema.schema(collection)
		val policy = if (written(it, 'onConflict')) onConflict.literal else 'error'
		val args = kw('location', text(location), 'document_type', text(doc.name), 'key', text(doc.key),
			'policy', text(if (policy == 'update') 'replace' else policy), 'counter', text(if (policy == 'error' || policy == 'ignore') 'insert_count' else 'upsert_count'))
		vectorSizes(args, collection)
		val slices = slices(it, collection)
		if (!slices.entries.empty) args.put('slices', slices)
		expiry(args, collection, null)
		if (schema.isVersioned(collection)) args.put('versioned', flag(true))
		#[new Code.Return(runtime('import_file', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of(NO_TRANSACTIONS)
	}

	/** A write is acknowledged once it is in the transaction log and visible to queries, so 'wait' holds already. */
	def private void writeOptions(EObject statement) {
		schema.note(statement, 'writeConsistency')
		for (key : ModelUtil.extParams(statement, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
	}

	/** A statement's filter with its tenant, namespace and partition conditions. */
	def private VespaFilter filterOf(EObject statement, VectorCollection collection, FilterBlock block) {
		val result = new VespaFilter(schema, collection).where(block)
		val tenant = feature(statement, 'tenant') as Tenant
		if (tenant !== null) result.slice(VespaPackage.TENANT, #[text(tenant.name)])
		val namespace = feature(statement, 'namespace')
		if (namespace instanceof String) result.slice(VespaPackage.NAMESPACE, #[text(namespace as String)])
		val single = feature(statement, 'partition') as Partition
		val partitions = feature(statement, 'partitions') as List<Partition>
		val List<Partition> named = newArrayList
		if (single !== null) named += single
		if (partitions !== null) named += partitions
		if (!named.empty) result.slice(VespaPackage.PARTITION, named.map[p | text(p.name) as Expr].toList)
		result
	}

	// ---------------------------------------------------------------- operational

	/** Attributes of a deployed schema are in memory, so a load checks that the schema is deployed. */
	def dispatch List<Stmt> body(Load it) {
		for (key : ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (!partitions.empty && !schema.schema(collection).has(VespaPackage.PARTITION)) throw Unsupported.of('partitions of a collection declared without partitions')
		require(collection)
	}

	def dispatch List<Stmt> body(Release it) {
		throw Unsupported.of(NO_RELEASE)
	}

	/** Writes are durable when acknowledged (transaction log), so a flush checks that the schema is deployed. */
	def dispatch List<Stmt> body(Flush it) {
		for (key : ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		require(collection)
	}

	/** Proton flushes and compacts its attributes and document store on its own, so a compaction checks that the schema is deployed. */
	def dispatch List<Stmt> body(Compact it) {
		for (key : ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		schema.note(it, 'targetSegments', 'async')
		require(collection)
	}

	def dispatch List<Stmt> body(Describe it) {
		val doc = schema.schema(collection)
		val args = kw('document_type', text(doc.name))
		if (includeStats) args.put('stats', countArgs(collection, newArrayList))
		if (includeIndexProgress) args.put('progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration' + VespaSchema.NO_MODELS)
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

	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a deployment; ' + SINGLE_NODE)
		if (asOf !== null) throw Unsupported.of("the 'as-of' clause; " + REASONS.get('asOf'))
		throw Unsupported.of('cloning a collection; ' + PENDING + ' (a new schema deployed, then every document visited and fed under the new document type)')
	}

	/** The reindexing of the document type (the deploy API's reindex request), awaited until it succeeds. */
	def dispatch List<Stmt> body(RebuildIndex it) {
		for (key : ModelUtil.extParams(it, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val collection = indexCollection(index)
		schema.note(it, 'concurrently')
		#[new Code.Return(runtime('reindex', kw('document_type', text(schema.schema(collection).name))))]
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

	/** Every document of the type (a delete where the selection is true); a namespace: its documents, selected by a query. */
	def dispatch List<Stmt> body(Truncate it) {
		if (namespace !== null) return dropSlice(collection, VespaPackage.NAMESPACE, namespace)
		val doc = schema.schema(collection)
		#[new Code.Return(runtime('delete', kw('document_type', text(doc.name), 'key', text(doc.key), 'all', flag(true))))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val owner = scope
		val args = kw('kind', text(kind.literal))
		switch kind {
			case COLLECTIONS: {
				if (owner instanceof Database) args.put('database', text((owner as Database).name))
				else if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing collections in a ' + owner.eClass.name)
			}
			case DATABASES: if (owner !== null && !(owner instanceof VectorDatabase)) throw Unsupported.of('listing databases in a ' + owner.eClass.name)
			case INDEXES: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing indexes without a collection')
				val collection = owner as VectorCollection
				val List<String> found = newArrayList
				for (index : collection.indexes) found += index.name
				for (field : ModelUtil.vectorFields(collection)) if (field.index !== null) found += field.index.name
				args.put('names', texts(found))
			}
			case ALIASES: throw Unsupported.of(NO_ALIASES)
			case TENANTS,
			case NAMESPACES,
			case PARTITIONS: {
				if (!(owner instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' without a collection')
				val collection = owner as VectorCollection
				val attribute = switch kind { case TENANTS: VespaPackage.TENANT case NAMESPACES: VespaPackage.NAMESPACE default: VespaPackage.PARTITION }
				val doc = schema.schema(collection)
				if (!doc.has(attribute)) throw Unsupported.of('listing ' + kind.literal + ' of a collection without ' + kind.literal)
				val request = VespaQueries.body(VespaQueries.select(doc.name, 'true', #[]) + ' | all(group(' + attribute + ') max(' + VespaQueries.PAGE
					+ ') each(output(count())))', 0, 0, 'unranked', null)
				args.put('body', request)
			}
			case SNAPSHOTS: throw Unsupported.of(NO_SNAPSHOTS)
		}
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('names', args))]
	}

	/** Writes are searchable when acknowledged; the wait covers a deployment still converging (serviceconverge and the query API). */
	def dispatch List<Stmt> body(AwaitIndex it) {
		schema.note(it, 'state')
		val args = kw('document_type', text(schema.schema(indexCollection(index)).name))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_ready', args))]
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		if (!highlighted.empty) throw Unsupported.of(NO_TEXT)
		reject(it, 'elementScope', 'asOf', 'reranker', 'queryModel', 'mmr', 'aggregations', 'prefetches', 'boost')
		combinations(it)
		val profile = rankProfile(it)
		val doc = schema.schema(collection)
		val field = vectorFieldOf(collection, vectorField)
		val vector = doc.field(schema.name(collection, field))
		checkMetric(it, field)
		val bound = new Params
		val conditions = filterOf(it, collection, filter).parts
		val List<Cond> parts = newArrayList(nearest(it, field, vector.name, windowOf(it, topK + offsetOf(it)), exact, params, bounds))
		parts += conditions
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val where = VespaQueries.where(parts, bound)
		val request = shapedBody(it, collection, VespaQueries.select(doc.name, where, #[]), topK, profile ?: VespaPackage.nearestProfile(vector.name),
			timeoutOf(params))
		matching(request, params, it)
		val args = searchArgs(request, bound, doc, collection, output, it)
		queryVector(args, input, collection, vector.name)
		if (profile === null) scoring(args, it, field, vector.name) else nativeScore(args, it)
		total(args, it, collection, conditions)
		shape(args, it, collection, topK)
		#[new Code.Return(runtime('search', args))]
	}

	/** Clause pairs whose result shapes exclude each other. */
	def private void combinations(EObject search) {
		if (written(search, 'groupBy') && written(search, 'cursor')) {
			throw Unsupported.of("the 'cursor' clause with 'group-by'; a cursor names a hit, and grouped results are pages of groups")
		}
		if (written(search, 'groupBy') && written(search, 'sortClauses')) {
			throw Unsupported.of("the 'sort' clause with 'group-by'; groups are ordered by their best hit")
		}
	}

	/**
	 * The hits a search reads: limit plus offset; with a cursor the first PAGE hits of the result order, in which the runtime finds the
	 * cursor record; with group-by the groups times the hits per group.
	 */
	def private int windowOf(EObject search, int window) {
		if (written(search, 'cursor')) VespaQueries.PAGE
		else if (written(search, 'groupBy')) window * groupSizeOf(search)
		else window
	}

	def private int groupSizeOf(EObject search) {
		if (written(search, 'groupSize')) feature(search, 'groupSize') as Integer else 1
	}

	/**
	 * The request body of a search: hits and offset of the statement; with a cursor PAGE hits from the start (the runtime pages after
	 * the cursor record); with group-by no hits and the grouping expression; explain: ranking.listFeatures.
	 */
	def private Entries shapedBody(EObject search, VectorCollection collection, String yql, int limit, String profile, Integer timeout) {
		val Entries request = if (written(search, 'cursor')) {
			VespaQueries.body(yql, VespaQueries.PAGE, 0, profile, timeout)
		} else if (written(search, 'groupBy')) {
			val grouped = groupField(search, collection, true)
			VespaQueries.body(yql + VespaQueries.grouping(grouped, limit + offsetOf(search), groupSizeOf(search)), 0, 0, profile, timeout)
		} else {
			VespaQueries.body(yql, limit, offsetOf(search), profile, timeout)
		}
		if (feature(search, 'explain') == Boolean.TRUE) (request.get('ranking') as Entries).put('listFeatures', flag(true))
		request
	}

	/**
	 * The field a search groups by, a single-value field: in Vespa's grouping the attribute (the exact companion of a full-text field),
	 * in the runtime's grouping of fused hybrid hits the summary field.
	 */
	def private String groupField(EObject search, VectorCollection collection, boolean attribute) {
		val grouped = new VespaFilter(schema, collection).field(feature(search, 'groupBy') as FieldPath)
		if (grouped.vector || grouped.array) {
			throw Unsupported.of('group-by on ' + grouped.name + '; the generator groups by single-value fields (grouping treats array elements as separate values)')
		}
		if (attribute && grouped.text) VespaPackage.EXACT + grouped.name else grouped.name
	}

	/** Result shapes the runtime applies: the cursor key with limit and offset, sort keys, groups and highlighted fields. */
	def private void shape(Entries args, EObject search, VectorCollection collection, int limit) {
		if (written(search, 'cursor')) {
			args.put('after', text(feature(search, 'cursor') as String))
			args.put('limit', num(limit))
			if (offsetOf(search) > 0) args.put('offset', num(offsetOf(search)))
		}
		val sorted = (feature(search, 'sortClauses') ?: newArrayList) as List<SortClause>
		if (!sorted.empty) args.put('order', orderOf(collection, sorted))
		if (written(search, 'groupBy')) {
			val groups = kw('field', text(groupField(search, collection, true)))
			if (offsetOf(search) > 0) groups.put('offset', num(offsetOf(search)))
			args.put('groups', groups)
		}
	}

	/** Sort keys as [field, direction]; a key without a field (sort-by relevance) is the score. */
	def private Expr orderOf(VectorCollection collection, List<SortClause> clauses) {
		val List<Expr> keys = newArrayList
		for (clause : clauses) {
			val direction = text(if (clause.direction == ESortDirection.DESC) 'desc' else 'asc')
			if (clause.byRelevance) {
				keys += items(#[new Code.Nil, direction])
			} else {
				val sorted = new VespaFilter(schema, collection).field(clause.field)
				if (sorted.text || sorted.vector || sorted.array) throw Unsupported.of('sorting by ' + sorted.name + '; the generator sorts by single numeric, boolean and exact string attributes')
				keys += items(#[text(sorted.name), direction])
			}
		}
		items(keys)
	}

	/** highlight: the document summary with the bolded copies of full-text fields, and the fields the runtime reports. */
	def private void highlight(Entries request, Entries args, EObject search, VectorCollection collection) {
		val fields = (feature(search, 'highlighted') ?: newArrayList) as List<Field>
		if (fields.empty) return;
		val doc = schema.schema(collection)
		val List<String> names = newArrayList
		for (field : fields) {
			val named = schema.name(collection, field)
			if (!doc.has(named) || !doc.field(named).text()) throw Unsupported.of('highlight on ' + named + NO_HIGHLIGHT_FIELD)
			names += named
		}
		(request.get('presentation') as Entries).put('summary', text(VespaPackage.HIGHLIGHT_SUMMARY))
		args.put('highlight', texts(names))
	}

	/** The nearestNeighbor operator: targetHits, exact (approximate: false), the candidates beyond targetHits, and a min bound as distance. */
	def private Cond nearest(EObject search, VectorField field, String vectorName, int targetHits, boolean exact, SearchParams bound, ScoreBounds bounds) {
		var Integer explore = null
		var exhaustive = exact
		if (bound !== null) {
			if (written(bound, 'targetRecall')) throw Unsupported.of('target-recall: targetHits and hnsw.exploreAdditionalHits are effort settings, with no declarable recall target')
			if (written(bound, 'probes')) throw Unsupported.of('probes; Vespa\'s vector index is HNSW (no IVF lists to probe)')
			if (written(bound, 'scanBudget')) throw Unsupported.of('scan-budget; Vespa has no setting that bounds the documents a nearestNeighbor query visits')
			if (written(bound, 'candidatePool')) explore = Math.max(0, bound.candidatePool - targetHits)
			if (bound.exhaustive) exhaustive = true
			schema.note(bound, 'oversampling', 'rescore')
		}
		val owner = ModelUtil.owningCollection(field)
		val index = if (owner === null) field.index else ModelUtil.vectorIndex(owner, field)
		if (explore === null && index !== null && written(index, 'searchCandidates')) explore = Math.max(0, index.searchCandidates - targetHits)
		for (entry : ModelUtil.extParams(search, VespaGenerator.VENDOR).entrySet) {
			if (entry.key == 'explore_additional_hits') {
				if (!(entry.value instanceof NumberLit)) throw Unsupported.of('explore_additional_hits that is not a number')
				explore = (entry.value as NumberLit).value as int
			}
		}
		val threshold = if (bounds !== null && written(bounds, 'min') && !(written(search, 'scoreForm') && feature(search, 'scoreForm') == EScoreForm.NATIVE)) {
			distanceAtMost(metricOf(field), bounds.min)
		}
		new Raw(VespaQueries.nearest(vectorName, targetHits, exhaustive, threshold, if (explore !== null && explore > 0) explore else null))
	}

	/** The largest distance whose canonical similarity is at least the bound (cosine: the angle, others: the negated bound), or none. */
	def private Double distanceAtMost(String metric, double similarity) {
		switch metric {
			case 'cosine': if (similarity <= -1) null else if (similarity >= 1) 0.0 else Math.acos(similarity)
			case 'hamming',
			case 'euclidean': if (similarity > 0) 0.0 else -similarity
			case 'dot': -similarity
			default: null
		}
	}

	def private String metricOf(VectorField field) {
		val collection = ModelUtil.owningCollection(field)
		schema.metric(field, if (collection === null) field.index else ModelUtil.vectorIndex(collection, field))
	}

	/** filter-strategy: pre_filter never post-filters (postFilterThreshold 1), post_filter always does (0); extension keys by name. */
	def private void matching(Entries request, SearchParams bound, EObject search) {
		val settings = entries
		if (bound !== null && written(bound, 'filterStrategy')) {
			switch bound.filterStrategy.literal {
				case 'pre_filter': settings.put('postFilterThreshold', num(1))
				case 'post_filter': settings.put('postFilterThreshold', num(0))
				case 'iterative': throw Unsupported.of('filter-strategy iterative; Vespa pre-filters or post-filters a nearestNeighbor query (with targetHits adjusted for the filter), with no iterative scan')
				default: {}
			}
		}
		if (bound !== null) for (key : ModelUtil.extParams(bound, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' in search parameters')
		for (entry : ModelUtil.extParams(search, VespaGenerator.VENDOR).entrySet) {
			val setting = SEARCH_SETTINGS.get(entry.key.toLowerCase)
			if (setting === null) throw Unsupported.of('extension parameter ' + entry.key + '; Vespa query settings here are ' + SEARCH_SETTINGS.keySet.join(', '))
			if (!(entry.value instanceof NumberLit)) throw Unsupported.of('extension parameter ' + entry.key + ' with a value that is not a number')
			if (setting != 'hnsw.exploreAdditionalHits') settings.put(setting, num((entry.value as NumberLit).value))
		}
		if (!settings.entries.empty) (request.get('ranking') as Entries).put('matching', settings)
	}

	def private Integer timeoutOf(SearchParams bound) {
		if (bound !== null && written(bound, 'timeoutMillis')) bound.timeoutMillis else null
	}

	def private void checkMetric(EObject search, VectorField field) {
		if (written(search, 'metric') && (feature(search, 'metric') as org.ftn.vecDSL.vecDSL.EMetric).literal != metricOf(field)) {
			throw Unsupported.of('a metric other than the field\'s; the distance-metric of a tensor field is part of its schema (changing it needs a restart)')
		}
	}

	/** Canonical similarity from the distance match feature, or the native relevance (closeness), and score bounds. */
	def private void scoring(Entries args, EObject search, VectorField field, String vectorName) {
		args.put('metric', text(metricOf(field)))
		args.put('feature', text(VespaPackage.distanceFeature(vectorName)))
		if (written(search, 'scoreForm') && feature(search, 'scoreForm') == EScoreForm.NATIVE) args.put('native', flag(true))
		scoreBounds(args, feature(search, 'bounds') as ScoreBounds)
	}

	def private void nativeScore(Entries args, EObject search) {
		if (written(search, 'scoreForm') && feature(search, 'scoreForm') != EScoreForm.NATIVE) {
			notes += 'score similarity on ' + search.eClass.name + ', whose declared rank profile scores natively'
		}
		scoreBounds(args, feature(search, 'bounds') as ScoreBounds)
	}

	def private void scoreBounds(Entries args, ScoreBounds limits) {
		if (limits !== null && written(limits, 'min')) args.put('low', num(limits.min))
		if (limits !== null && written(limits, 'max')) args.put('high', num(limits.max))
	}

	/** with-total: the number of documents the filter matches, counted by its own query. */
	def private void total(Entries args, EObject search, VectorCollection collection, List<Cond> conditions) {
		if (!written(search, 'withTotal')) return;
		args.put('total', countArgs(collection, conditions))
	}

	def private Entries countArgs(VectorCollection collection, List<Cond> conditions) {
		countArgs(collection, conditions, new Params)
	}

	/** The count query of the conditions; counted already holds the values of conditions bound before (the text of a keyword search). */
	def private Entries countArgs(VectorCollection collection, List<Cond> conditions, Params counted) {
		val doc = schema.schema(collection)
		val List<Cond> parts = newArrayList(conditions)
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val where = VespaQueries.where(parts, counted)
		val args = kw('body', VespaQueries.body(VespaQueries.select(doc.name, where, #[]), 0, 0, 'unranked', null))
		counted.into(args)
		if (doc.expiring) args.put('clock', flag(true))
		args
	}

	def private Entries searchArgs(Entries request, Params bound, Schema doc, VectorCollection collection, OutputSpec output, EObject search) {
		val args = kw('body', request)
		bound.into(args)
		if (doc.expiring) args.put('clock', flag(true))
		args.put('key', text(doc.key))
		val shape = projection(collection, output, excludedOf(search), (feature(search, 'withVectors') ?: false) as Boolean)
		args.put('output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		args
	}

	/** The rank profile an extension vespa block names (RankProfileUse), or none. */
	def private String rankProfile(EObject search) {
		var String chosen = null
		val slot = search.eClass.getEStructuralFeature('extensions')
		if (slot === null) return null
		for (block : (search.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == VespaGenerator.VENDOR) {
				for (item : block.getItems) {
					switch item {
						RankProfileUse: chosen = useProfile(search, item.profile)
						ExtParam: {}
						default: throw Unsupported.of(item.eClass.name + ' on a query; a Vespa query extension names a rank profile (rank) or query settings')
					}
				}
			}
		}
		chosen
	}

	def private String useProfile(EObject search, PhasedRankProfile profile) {
		val owner = EcoreUtil2.getContainerOfType(profile, VectorCollection)
		val collection = feature(search, 'collection') as VectorCollection
		if (owner !== collection) throw Unsupported.of('rank profile ' + profile.name + ', which collection ' + collection.name + ' does not declare')
		if ((search instanceof VectorSearch || search instanceof RecommendSearch) && !VespaSchema.takesQueryVector(profile)) {
			throw Unsupported.of('rank profile ' + profile.name + ' on a vector search; it declares no input ' + VespaPackage.QUERY_TENSOR
				+ ', which carries the query vector of nearestNeighbor')
		}
		profile.name
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'reranker', 'minShouldMatch', 'mmr', 'aggregations', 'boost', 'fuzziness')
		combinations(it)
		val profile = rankProfile(it)
		if (it.params !== null) schema.note(it.params, 'candidatePool', 'probes', 'scanBudget', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		val doc = schema.schema(collection)
		val bound = new Params
		val weights = entries
		val List<Cond> parts = newArrayList(keywordQuery(collection, searchFields, input, textType(it), operatorOf(it), slopOf(it), bound, weights))
		val conditions = filterOf(it, collection, filter).parts
		parts += conditions
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val request = shapedBody(it, collection, VespaQueries.select(doc.name, VespaQueries.where(parts, bound), #[]), topK,
			profile ?: VespaPackage.TEXT_PROFILE, timeoutOf(it.params))
		if (profile === null && !weights.entries.empty) request.put('input', weights)
		val args = searchArgs(request, bound, doc, collection, output, it)
		scoreBounds(args, bounds)
		if (written(it, 'withTotal')) {
			val totalBound = new Params
			val List<Cond> counted = newArrayList(keywordQuery(collection, searchFields, input, textType(it), operatorOf(it), slopOf(it), totalBound, entries))
			counted += conditions
			args.put('total', countArgs(collection, counted, totalBound))
		}
		highlight(request, args, it, collection)
		shape(args, it, collection, topK)
		#[new Code.Return(runtime('search', args))]
	}

	def private Integer slopOf(EObject search) {
		if (written(search, 'slop')) feature(search, 'slop') as Integer else null
	}

	def private String textType(EObject search) {
		if (written(search, 'textQueryType')) (feature(search, 'textQueryType') as org.ftn.vecDSL.vecDSL.ETextQueryType).literal else 'match'
	}

	def private String operatorOf(EObject search) {
		if (written(search, 'matchOperator')) (feature(search, 'matchOperator') as org.ftn.vecDSL.vecDSL.EMatchOperator).literal else 'and'
	}

	/**
	 * The text part of a keyword query: userInput over each listed full-text field (every one without a list), joined with or, and the
	 * field boosts as the rank profile's weight inputs query(vecdsl_w_<field>) (a field not listed weighs 0).
	 */
	def private Cond keywordQuery(VectorCollection collection, List<SearchFieldRef> refs, QueryInput input, String type, String operator,
		Integer slop, Params bound, Entries weights) {
		val doc = schema.schema(collection)
		val List<Pair<String, Double>> fields = newArrayList
		if (refs.empty) {
			for (field : doc.texts) fields += field.name -> 1.0
		} else {
			for (ref : refs) {
				val named = schema.name(collection, ref.field)
				if (!doc.has(named) || !doc.field(named).text()) throw Unsupported.of('keyword search on ' + named + ', which is not a full-text field')
				fields += named -> (if (written(ref, 'boost')) ref.boost else 1.0)
			}
		}
		if (fields.empty) throw Unsupported.of('keyword search in a collection without full-text fields')
		val plainComposite = switch type {
			case 'match': operator
			case 'phrase': 'phrase'
			case 'query_string': throw Unsupported.of('text-query query_string; the generator never parses a value as query syntax (userInput with grammar.syntax none), so user query strings are not parsed')
			case 'regexp': throw Unsupported.of('text-query regexp; YQL matches regular expressions on attributes only, and case-insensitively (the matches operator)')
			case 'fuzzy': throw Unsupported.of('text-query fuzzy; ' + VespaFilter.NO_FUZZY)
			default: throw Unsupported.of('text-query ' + type + '; ' + PENDING + ' (the prefix and substring term annotations)')
		}
		val composite = if (slop === null) plainComposite else switch plainComposite {
			case 'and': 'near'
			case 'phrase': 'oNear'  // the query API's composite value (QueryType.Composite.oNear); the YQL operator is onear()
			default: throw Unsupported.of('slop with operator ' + plainComposite + '; near and onear bound the distance of terms that must all match')
		}
		val userText = keywordText(input)
		val distance = if (slop === null) null else slop + 1
		val parts = fields.map[f | new Raw(VespaQueries.userInput(f.key, composite, userText, bound, distance)) as Cond].toList
		for (field : doc.texts) {
			val weight = fields.findFirst[key == field.name]?.value ?: 0.0
			if (weight != 1.0) weights.put('query(' + VespaPackage.WEIGHT + field.name + ')', num(weight))
		}
		if (parts.size == 1) parts.head else new VespaQueries.Or(parts)
	}

	def private Expr keywordText(QueryInput input) {
		if (input === null || input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search without text input')
		if (!(input.value instanceof StringLit || input.value instanceof ParamRef)) throw Unsupported.of('keyword input that is not a string')
		Literals.of(input.value)
	}

	/**
	 * Hybrid search: each leg is its own query and the runtime fuses the legs as vecDSL defines rrf, weighted and relative_score. Vespa's
	 * global-phase reciprocal_rank_fusion ranks the union of the first-phase hits by every score, including documents a leg did not
	 * retrieve, which is not the per-leg rank of vecDSL's fusion.
	 */
	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'reranker', 'mmr', 'aggregations', 'boost')
		combinations(it)
		if (rankProfile(it) !== null) throw Unsupported.of('a declared rank profile on a hybrid search; the runtime fuses the legs\' own scores')
		if (written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE) notes += 'score native on HybridSearch, whose fused score has one form'
		val doc = schema.schema(collection)
		val fusionKind = if (written(it, 'fusion')) fusion.literal else 'rrf'
		val weights = legWeights(it, fusionKind)
		val window = windowOf(it, topK + offsetOf(it))
		val k = if (written(it, 'fusionConstant')) fusionConstant else 60
		val List<Expr> legArgs = newArrayList
		val List<Cond> legMatches = newArrayList
		val counted = new Params
		val marked = (feature(it, 'highlighted') ?: newArrayList) as List<Field>
		for (leg : legs) {
			for (key : ModelUtil.extParams(leg, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a hybrid leg')
			val limit = if (written(leg, 'legLimit')) feature(leg, 'legLimit') as Integer else window
			val bound = new Params
			val conditions = filterOf(it, collection, legFilter(filter, feature(leg, 'filter') as FilterBlock)).parts
			legArgs += switch leg {
				VectorLeg: {
					if (leg.bounds !== null) throw Unsupported.of('bounds on a hybrid leg; ' + PENDING + ' (the leg score filtered before fusion)')
					val field = vectorFieldOf(collection, leg.vectorField)
					val vector = doc.field(schema.name(collection, field))
					checkMetric(leg, field)
					val List<Cond> parts = newArrayList(nearest(it, field, vector.name, limit, leg.exact, leg.params, null))
					parts += conditions
					if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
					val request = VespaQueries.body(VespaQueries.select(doc.name, VespaQueries.where(parts, bound), #[]), limit, 0,
						VespaPackage.nearestProfile(vector.name), timeoutOf(params))
					if (explain) (request.get('ranking') as Entries).put('listFeatures', flag(true))
					matching(request, leg.params, leg)
					legMatches.add(if (conditions.empty) new Raw('true') as Cond else if (conditions.size == 1) conditions.head else new VespaQueries.And(conditions))
					val result = kw('body', request)
					bound.into(result)
					if (doc.expiring) result.put('clock', flag(true))
					queryVector(result, leg.input ?: input, collection, vector.name)
					result.put('metric', text(metricOf(field)))
					result.put('feature', text(VespaPackage.distanceFeature(vector.name)))
					result
				}
				KeywordLeg: {
					if (written(leg, 'minShouldMatch')) throw Unsupported.of("the 'min-should-match' clause; " + REASONS.get('minShouldMatch'))
					val weightInputs = entries
					val List<Cond> parts = newArrayList(keywordQuery(collection, leg.searchFields, leg.input ?: input, textType(leg), operatorOf(leg), null, bound, weightInputs))
					parts += conditions
					if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
					val request = VespaQueries.body(VespaQueries.select(doc.name, VespaQueries.where(parts, bound), #[]), limit, 0,
						VespaPackage.TEXT_PROFILE, timeoutOf(params))
					if (!weightInputs.entries.empty) request.put('input', weightInputs)
					if (explain) (request.get('ranking') as Entries).put('listFeatures', flag(true))
					if (!marked.empty) (request.get('presentation') as Entries).put('summary', text(VespaPackage.HIGHLIGHT_SUMMARY))
					if (written(it, 'withTotal')) {
						val List<Cond> matched = newArrayList(keywordQuery(collection, leg.searchFields, leg.input ?: input, textType(leg), operatorOf(leg), null, counted, entries))
						matched += conditions
						legMatches.add(if (matched.size == 1) matched.head else new VespaQueries.And(matched) as Cond)
					}
					val result = kw('body', request)
					bound.into(result)
					if (doc.expiring) result.put('clock', flag(true))
					result
				}
			}
		}
		val args = kw('legs', items(legArgs), 'fusion', text(fusionKind), 'key', text(doc.key))
		val shape = projection(collection, output, excludedOf(it), withVectors)
		args.put('output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		args.put('limit', num(topK))
		if (offsetOf(it) > 0) args.put('offset', num(offsetOf(it)))
		if (fusionKind == 'rrf') args.put('k', num(k)) else args.put('weights', items(weights.map[w | num(w) as Expr]))
		scoreBounds(args, bounds)
		if (written(it, 'cursor')) args.put('after', text(cursor))
		if (!sortClauses.empty) args.put('order', orderOf(collection, sortClauses))
		if (written(it, 'groupBy')) args.put('groups', kw('field', text(groupField(it, collection, false)), 'size', num(groupSizeOf(it))))
		if (!marked.empty) {
			val List<String> names = newArrayList
			for (field : marked) {
				val named = schema.name(collection, field)
				if (!doc.has(named) || !doc.field(named).text()) throw Unsupported.of('highlight on ' + named + NO_HIGHLIGHT_FIELD)
				names += named
			}
			args.put('highlight', texts(names))
		}
		if (explain) args.put('explain', flag(true))
		if (written(it, 'withTotal')) args.put('total', countArgs(collection, #[unionOf(legMatches)], counted))
		#[new Code.Return(runtime('hybrid', args))]
	}

	/** The documents some leg matches: a vector leg matches its filter (nearestNeighbor ranks every document), a keyword leg its text. */
	def private Cond unionOf(List<Cond> legMatches) {
		if (legMatches.exists[match | match instanceof Raw && (match as Raw).yql == 'true']) new Raw('true')
		else if (legMatches.size == 1) legMatches.head
		else new VespaQueries.Or(legMatches)
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

	/** The average-vector strategy (2 avg(positive) - avg(negative)) computed in the runtime; the example documents are excluded. */
	def dispatch List<Stmt> body(RecommendSearch it) {
		if (!highlighted.empty) throw Unsupported.of(NO_TEXT)
		reject(it, 'asOf', 'reranker', 'mmr', 'aggregations', 'boost')
		combinations(it)
		val profile = rankProfile(it)
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val doc = schema.schema(collection)
		val examples = it.lookupCollection ?: collection
		val field = vectorFieldOf(collection, vectorField)
		val vector = doc.field(schema.name(collection, field))
		val List<Expr> positiveIds = newArrayList
		val List<Expr> positiveVectors = newArrayList
		val List<Expr> negativeIds = newArrayList
		val List<Expr> negativeVectors = newArrayList
		for (example : positives) if (isVector(example)) positiveVectors += Literals.of(example) else positiveIds += Literals.of(example)
		for (example : negatives) if (isVector(example)) negativeVectors += Literals.of(example) else negativeIds += Literals.of(example)
		val restriction = filterOf(it, collection, filter)
		val List<Expr> exampleIds = newArrayList
		exampleIds += positiveIds
		exampleIds += negativeIds
		if (examples === collection && !exampleIds.empty) {
			val keyField = doc.field(doc.key)
			restriction.add(new Not(new Test(keyField.name, keyField.kind, 'in', items(exampleIds), null)))
		}
		val bound = new Params
		val List<Cond> parts = newArrayList(nearest(it, field, vector.name, windowOf(it, topK + offsetOf(it)), false, params, bounds))
		parts += restriction.parts
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val request = shapedBody(it, collection, VespaQueries.select(doc.name, VespaQueries.where(parts, bound), #[]), topK,
			profile ?: VespaPackage.nearestProfile(vector.name), timeoutOf(params))
		matching(request, params, it)
		val args = searchArgs(request, bound, doc, collection, output, it)
		val lookup = kw('document_type', text(schema.schema(examples).name), 'field', text(schema.name(examples, field)))
		if (!positiveIds.empty) lookup.put('positive_ids', items(positiveIds))
		if (!negativeIds.empty) lookup.put('negative_ids', items(negativeIds))
		if (!positiveVectors.empty) lookup.put('positive_vectors', items(positiveVectors))
		if (!negativeVectors.empty) lookup.put('negative_vectors', items(negativeVectors))
		args.put('lookup', lookup)
		if (profile === null) scoring(args, it, field, vector.name) else nativeScore(args, it)
		total(args, it, collection, restriction.parts)
		shape(args, it, collection, topK)
		#[new Code.Return(runtime('search', args))]
	}

	def private boolean isVector(Literal example) {
		example instanceof ListLit || example instanceof SparseLit
			|| (example instanceof ParamRef && #['vector', 'sparse_vector'].contains((example as ParamRef).param.type.literal))
	}

	/** document/v1 GET of each id; a slice or an expiry is checked on the returned document. */
	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val doc = schema.schema(collection)
		val shape = projection(collection, output, newArrayList, withVectors)
		val args = kw('document_type', text(doc.name), 'key', text(doc.key), 'ids', items(ids.map[Literals.of(it)]), 'output', texts(shape.key))
		if (!shape.value.empty) args.put('vectors', texts(shape.value))
		val restriction = entries
		val tenant = it.tenant
		if (tenant !== null) restriction.put(VespaPackage.TENANT, items(#[text(tenant.name)]))
		if (it.namespace !== null) restriction.put(VespaPackage.NAMESPACE, items(#[text(it.namespace)]))
		if (!partitions.empty) restriction.put(VespaPackage.PARTITION, items(partitions.map[p | text(p.name) as Expr]))
		for (attribute : restriction.entries.keySet) {
			if (!doc.has(attribute)) throw Unsupported.of('a slice of collection ' + collection.name + ', which declares no ' + attribute.substring(7))
		}
		if (!restriction.entries.empty) args.put('slices', restriction)
		if (doc.expiring) args.put('expiring', flag(true))
		#[new Code.Return(runtime('get', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		queryExtensions(it)
		#[new Code.Return(runtime('count', countArgs(collection, filterOf(it, collection, filter).parts)))]
	}

	/** select ... order by the sort clauses and the key (a total order), one page or with every all pages. */
	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val doc = schema.schema(collection)
		val List<String> order = newArrayList
		for (clause : sortClauses) {
			if (clause.byRelevance) throw Unsupported.of('sort by relevance in a scroll; a scroll has no score')
			val sorted = new VespaFilter(schema, collection).field(clause.field)
			if (sorted.text || sorted.vector || sorted.array) throw Unsupported.of('sorting by ' + sorted.name + '; the generator sorts by single numeric, boolean and exact string attributes')
			order += sorted.name + (if (clause.direction == ESortDirection.DESC) ' desc' else ' asc')
		}
		order += doc.key + ' asc'
		val bound = new Params
		val List<Cond> parts = newArrayList(filterOf(it, collection, filter).parts)
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val limit = if (written(it, 'scrollLimit')) scrollLimit else VespaQueries.PAGE
		val streamed = written(it, 'cursor') || written(it, 'sliceIndex')
		val request = VespaQueries.body(VespaQueries.select(doc.name, VespaQueries.where(parts, bound), order), limit, if (streamed) 0 else offsetOf(it),
			'unranked', null)
		val args = searchArgs(request, bound, doc, collection, output, it)
		args.put('scores', flag(false))
		if (!written(it, 'scrollLimit')) args.put('every', flag(true))
		if (written(it, 'cursor')) args.put('after', text(cursor))
		if (written(it, 'sliceIndex')) {
			if (sliceTotal < 1 || sliceIndex < 0 || sliceIndex >= sliceTotal) throw Unsupported.of('slice ' + sliceIndex + ' of ' + sliceTotal + '; a slice index is at least 0 and below the slice count')
			args.put('slice', items(#[num(sliceIndex), num(sliceTotal)]))
		}
		if (streamed) {
			if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
			if (offsetOf(it) > 0) args.put('offset', num(offsetOf(it)))
		}
		#[new Code.Return(runtime('search', args))]
	}

	/** random: the rank profile vecdsl_random (the random rank feature) over the matching documents; first: the first in key order. */
	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val doc = schema.schema(collection)
		val first = written(it, 'method') && method == ESampleMethod.FIRST
		val bound = new Params
		val List<Cond> parts = newArrayList(filterOf(it, collection, filter).parts)
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val request = VespaQueries.body(VespaQueries.select(doc.name, VespaQueries.where(parts, bound), if (first) #[doc.key + ' asc'] else #[]),
			sampleSize, 0, if (first) 'unranked' else VespaPackage.RANDOM_PROFILE, null)
		val args = searchArgs(request, bound, doc, collection, output, it)
		args.put('scores', flag(false))
		#[new Code.Return(runtime('search', args))]
	}

	/** A random sample (the random rank profile) with its vectors; the pairs and their canonical similarities are computed in the runtime. */
	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		queryExtensions(it)
		val doc = schema.schema(collection)
		val field = vectorFieldOf(collection, vectorField)
		val named = schema.name(collection, field)
		val bound = new Params
		val List<Cond> parts = newArrayList(filterOf(it, collection, filter).parts)
		if (doc.expiring) parts += new Raw(VespaPackage.EXPIRES + ' > @vecdsl_now')
		val request = VespaQueries.body(VespaQueries.select(doc.name, VespaQueries.where(parts, bound), #[]),
			if (written(it, 'sampleSize')) sampleSize else 10, 0, VespaPackage.RANDOM_PROFILE, null)
		val args = kw('body', request)
		bound.into(args)
		if (doc.expiring) args.put('clock', flag(true))
		args.put('key', text(doc.key))
		args.put('field', text(named))
		args.put('metric', text(metricOf(field)))
		args.put('limit', num(if (written(it, 'matrixLimit')) matrixLimit else 3))
		#[new Code.Return(runtime('distance_matrix', args))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		throw Unsupported.of('generative search; ' + NO_GENERATION)
	}

	def dispatch List<Stmt> body(BatchQuery it) {
		val List<Expr> calls = newArrayList
		val owner = currentMethod
		var number = 0
		for (query : queries) {
			val name = owner + '_query_' + number
			currentMethod = name
			helpers += new Program.Method(name, ModelUtil.parameterNames(query), body(query), newArrayList, newArrayList, true)
			calls += self(name)
			number = number + 1
		}
		currentMethod = owner
		#[new Code.Return(items(calls)) as Stmt]
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
		val doc = schema.schema(collection)
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val listed = if (output === null || output.allFields) collection.fields as List<? extends Field> else output.fields
		for (field : listed) {
			val named = schema.name(collection, field)
			if (named != doc.key && !excluded.contains(field) && doc.has(named)) {
				if (field instanceof VectorField) {
					if (output !== null && !output.allFields) vectors += named
				} else {
					fields += named
				}
			}
		}
		if (withVectors) {
			for (field : ModelUtil.vectorFields(collection)) {
				val named = schema.name(collection, field)
				if (!vectors.contains(named) && !excluded.contains(field) && doc.has(named)) vectors += named
			}
		}
		fields -> vectors
	}

	/** Rejects clauses Vespa lacks, with their reason, and clauses not generated yet. */
	def private void reject(EObject statement, String... features) {
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

	/** Extension vespa blocks of reads other than searches carry nothing the generator maps; their items are rejected by name. */
	def private void queryExtensions(EObject host) {
		val slot = host.eClass.getEStructuralFeature('extensions')
		if (slot === null) return;
		for (block : (host.eGet(slot) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == VespaGenerator.VENDOR) {
				for (item : block.getItems) {
					if (item instanceof ExtParam) throw Unsupported.of('extension parameter ' + (item.key ?: item.quotedKey) + ' on ' + host.eClass.name)
					throw Unsupported.of(item.eClass.name + ' on ' + host.eClass.name + '; Vespa extensions on this statement carry nothing the generator maps')
				}
			}
		}
	}

	/** The query vector: the given vector as input.query(vecdsl_q), or a stored document's vector the runtime reads first. */
	def private void queryVector(Entries args, QueryInput input, VectorCollection collection, String vectorName) {
		if (input === null) throw Unsupported.of('a vector query without input')
		val request = args.get('body') as Entries
		switch input.kind {
			case VECTOR: {
				val inputs = (if (request.has('input')) request.get('input') else entries) as Entries
				inputs.put('query(' + VespaPackage.QUERY_TENSOR + ')', Literals.of(input.value))
				request.put('input', inputs)
			}
			case ID: {
				val stored = input.lookupCollection ?: collection
				args.put('lookup', kw('document_type', text(schema.schema(stored).name), 'field', text(vectorName),
					'positive_ids', items(#[Literals.of(input.value)])))
			}
			case SPARSE_VECTOR: throw Unsupported.of('sparse_vector input; ' + PENDING + ' (mapped tensors ranked with a dot product)')
			case TEXT: throw Unsupported.of('text input' + VespaSchema.NO_MODELS)
			case MULTI_VECTOR: throw Unsupported.of('multi-vector input; ' + PENDING + ' (a mixed query tensor with max_sim in a rank expression)')
			default: throw Unsupported.of(input.kind.literal + ' input' + VespaSchema.NO_MODELS)
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
			if (entry.value instanceof SparseLit) throw Unsupported.of('sparse vector values; ' + PENDING + ' (mapped tensors)')
			result.put(schema.name(collection, entry.field), Literals.of(entry.value))
		}
		result
	}
}
