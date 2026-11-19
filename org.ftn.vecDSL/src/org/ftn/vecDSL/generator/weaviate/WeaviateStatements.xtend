package org.ftn.vecDSL.generator.weaviate

import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.code.Syntax
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
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EFusionType
import org.ftn.vecDSL.vecDSL.EImportFormat
import org.ftn.vecDSL.vecDSL.EMatchOperator
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EQueryInputMode
import org.ftn.vecDSL.vecDSL.EScoreForm
import org.ftn.vecDSL.vecDSL.ESnapshotScope
import org.ftn.vecDSL.vecDSL.ETextQueryType
import org.ftn.vecDSL.vecDSL.EUpdateMode
import org.ftn.vecDSL.vecDSL.EWriteConsistency
import org.ftn.vecDSL.vecDSL.EmbeddingMigration
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.FieldLimit
import org.ftn.vecDSL.vecDSL.FileResource
import org.ftn.vecDSL.vecDSL.FileSource
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
import org.ftn.vecDSL.vecDSL.SearchFieldRef
import org.ftn.vecDSL.vecDSL.SearchParams
import org.ftn.vecDSL.vecDSL.SetPropertyOp
import org.ftn.vecDSL.vecDSL.ShardConfig
import org.ftn.vecDSL.vecDSL.Snapshot
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

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.generator.weaviate.WeaviateSchema.kw
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps each vecDSL statement to Weaviate operations in the host-language-neutral code model. Every operation is
 * a call into the per-language runtime module, which holds the SDK calls; arguments are plain data (the
 * collection layout, records, filter trees), so the three host languages run the same Weaviate requests.
 */
class WeaviateStatements {

	/** Message of the guard that only an unmapped vecDSL class reaches; WeaviateCoverageTest asserts it never fires. */
	public static val NOT_MAPPED = 'not mapped for Weaviate: '
	static val LOGICAL_DATABASES = 'logical databases; Weaviate namespaces need a license key and NAMESPACES_ENABLED, and no client SDK manages them'
	static val METRICS = 'metrics configuration; Weaviate exports Prometheus metrics through server environment variables'
	static val BACKENDS = #{'filesystem' -> 'filesystem', 'file' -> 'filesystem', 's3' -> 's3', 'gs' -> 'gcs', 'gcs' -> 'gcs', 'azure' -> 'azure'}
	static val PRIVILEGES = #{
		'read' -> #['read_collections', 'read_data'],
		'write' -> #['create_data', 'update_data', 'delete_data'],
		'insert' -> #['create_data'],
		'update' -> #['update_data'],
		'delete' -> #['delete_data'],
		'manage' -> #['create_collections', 'read_collections', 'update_collections', 'delete_collections'],
		'all' -> #['create_collections', 'read_collections', 'update_collections', 'delete_collections', 'create_data', 'read_data', 'update_data', 'delete_data']
	}

	static val CLAUSES = #{
		'elementScope' -> 'within', 'asOf' -> 'as-of', 'cursor' -> 'cursor', 'withTotal' -> 'with-total',
		'explain' -> 'explain', 'mmr' -> 'mmr', 'reranker' -> 'rerank', 'highlighted' -> 'highlight',
		'sortClauses' -> 'sort', 'aggregations' -> 'aggregate', 'prefetches' -> 'prefetch', 'offset' -> 'offset',
		'groupBy' -> 'group-by', 'sliceIndex' -> 'slice', 'fuzziness' -> 'fuzziness', 'slop' -> 'slop',
		'minShouldMatch' -> 'min-should-match', 'exact' -> 'exact', 'boost' -> 'boost', 'queryModel' -> 'model',
		'bounds' -> 'bounds', 'params' -> 'params', 'scoreForm' -> 'score'
	}

	/** Why Weaviate cannot express a query clause. */
	static val REASONS = #{
		'elementScope' -> 'Weaviate searches whole objects, not array elements',
		'asOf' -> 'Weaviate keeps no object history',
		'cursor' -> 'Weaviate pages search results by offset only',
		'highlighted' -> 'Weaviate returns no highlights',
		'sortClauses' -> 'Weaviate sorts object listings, not search results',
		'prefetches' -> 'Weaviate runs single-stage searches',
		'exact' -> 'Weaviate has no per-query exact search (a flat index is always exhaustive)',
		'explain' -> 'Weaviate explains keyword and hybrid scores only',
		'fuzziness' -> 'Weaviate BM25 has no fuzzy matching',
		'slop' -> 'Weaviate BM25 has no phrase slop',
		'sliceIndex' -> 'Weaviate has no parallel scroll slices',
		'queryModel' -> 'Weaviate embeds query text with the vectorizer of the named vector',
		'bounds' -> 'Weaviate bounds hybrid results only through the vector leg',
		'params' -> 'Weaviate takes no search parameters on a hybrid query'
	}

	val Script script
	val List<String> notes = newArrayList
	val WeaviateSchema schema = new WeaviateSchema(notes)
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

	def private List<Stmt> define(Concept concept, boolean ifNotExists) {
		switch concept {
			VectorCollection: schema.createCollection(concept, ifNotExists)
			Connection: {
				if (concept.localPath !== null || concept.inMemory || concept.deployment == EDeploymentMode.EMBEDDED) {
					throw Unsupported.of('embedded deployments; the generated programs connect to a Weaviate server')
				}
				return newArrayList
			}
			VectorDatabase: database(concept, ifNotExists)
			Cluster: {
				val List<Stmt> result = newArrayList
				for (collection : concept.collections) result += schema.createCollection(collection, ifNotExists)
				result
			}
			Database: throw Unsupported.of(LOGICAL_DATABASES)
			Partition: throw Unsupported.of(WeaviateSchema.PARTITIONS)
			Tenant: createTenants(owner(concept), WeaviateSchema.tenantName(concept.name), ifNotExists)
			Namespace: createTenants(owner(concept), WeaviateSchema.tenantName(concept.key), ifNotExists)
			Alias: createAlias(concept, concept.collection, ifNotExists)
			Field: addField(owner(concept), concept)
			PayloadIndex: createPayloadIndex(concept)
			VectorIndex: throw Unsupported.of('creating a vector index on an existing collection; Weaviate fixes the index type of a named vector when it creates the vector')
			Function: addFunction(owner(concept), concept)
			EmbeddingModel: {
				schema.vectorizerModule(concept)
				return newArrayList
			}
			GenerativeModel: {
				schema.generativeModule(concept)
				return newArrayList
			}
			Reranker: {
				schema.rerankerModule(concept)
				return newArrayList
			}
			Analyzer: return newArrayList
			AnalyzerFilter: return newArrayList
			FileResource: throw Unsupported.of('file resources; Weaviate takes stopword lists and tokenizer dictionaries inline in the collection definition')
			Quantization: throw Unsupported.of('quantization outside a collection definition; vecDSL reaches it only inside a vector-index create, which Weaviate cannot apply to an existing collection')
			ReplicationConfig: throw Unsupported.of('replication for an existing collection; Weaviate fixes the replication factor at creation')
			ShardConfig: throw Unsupported.of('sharding for an existing collection; Weaviate fixes sharding at creation')
			StorageConfig: throw Unsupported.of('storage for an existing collection; Weaviate persists every collection on disk')
			AuthConfig: auth(concept)
			RoleBinding: createRole(concept, ifNotExists)
			MetricsConfig: throw Unsupported.of(METRICS)
			BackupSchedule: throw Unsupported.of('backup schedules; Weaviate creates backups only on request')
			ClusterLimit: throw Unsupported.of('cluster limits; Weaviate sets them only through server environment variables')
			CollectionLimit: throw Unsupported.of('collection limits; Weaviate has no per-collection capacity limits')
			FieldLimit: throw Unsupported.of('field limits on an existing field; Weaviate stores no field limits, the runtime checks the declared ones')
			default: throw Unsupported.of(NOT_MAPPED + concept.eClass.name)
		}
	}

	/** Collections of a deployment; its connection is the program's endpoint. */
	def private List<Stmt> database(VectorDatabase database, boolean ifNotExists) {
		if (!database.databases.empty) throw Unsupported.of(LOGICAL_DATABASES)
		if (database.metrics !== null) throw Unsupported.of(METRICS)
		val List<Stmt> result = newArrayList
		result += define(database.connection, ifNotExists)
		if (database.auth !== null) result += auth(database.auth)
		for (cluster : database.clusters) result += define(cluster, ifNotExists)
		result
	}

	/** Weaviate configures authentication at server start; the client side and roles are generated. */
	def private List<Stmt> auth(AuthConfig config) {
		if (!#[EAuthType.NONE, EAuthType.API_KEY, EAuthType.TOKEN].contains(config.type)) {
			throw Unsupported.of(config.type.literal + ' authentication; Weaviate accepts API keys and OIDC tokens')
		}
		if (config.certificatePath !== null || config.cmekKeyId !== null || config.auditLog || config.fieldEncryption) {
			throw Unsupported.of('certificates, customer-managed keys, audit logs and field encryption; Weaviate configures them outside the API')
		}
		val List<Stmt> result = newArrayList
		for (role : config.roles) result += createRole(role, false)
		result
	}

	def private List<Stmt> createRole(RoleBinding binding, boolean ifNotExists) {
		val List<Expr> permissions = newArrayList
		val scope = if (binding.collection === null) '*' else WeaviateSchema.className(binding.collection)
		for (privilege : binding.privileges) {
			val actions = PRIVILEGES.get(privilege.toLowerCase)
			if (actions === null) throw Unsupported.of("privilege '" + privilege + "'; Weaviate collection and data permissions are " + PRIVILEGES.keySet.sort.join(', '))
			permissions += kw('collection', text(scope), 'actions', texts(actions))
		}
		val name = text(binding.roleName)
		val create = new Code.Run(runtime('create_role', kw('role', name, 'permissions', items(permissions)))) as Stmt
		#[if (ifNotExists) new Code.When(runtime('has_role', kw('role', name)), true, #[create]) else create]
	}

	def private List<Stmt> createTenants(VectorCollection collection, String name, boolean ifNotExists) {
		if (!schema.multiTenant(collection)) {
			throw Unsupported.of('a tenant of collection ' + collection.name + ', which declares none; Weaviate enables multi-tenancy only when it creates a collection')
		}
		val args = kw('name', text(WeaviateSchema.className(collection)), 'tenants', texts(#[name]))
		if (ifNotExists) args.put('if_not_exists', flag(true))
		#[new Code.Run(runtime('create_tenants', args))]
	}

	def private List<Stmt> createAlias(Alias alias, VectorCollection collection, boolean ifNotExists) {
		val name = text(WeaviateSchema.className(alias.name))
		val create = new Code.Run(runtime('create_alias', kw('alias', name, 'collection', text(WeaviateSchema.className(collection))))) as Stmt
		#[if (ifNotExists) new Code.When(runtime('has_alias', kw('alias', name)), true, #[create]) else create]
	}

	def private List<Stmt> addField(VectorCollection collection, Field field) {
		val name = text(WeaviateSchema.className(collection))
		switch field {
			PayloadField: #[new Code.Run(runtime('add_property', kw('name', name, 'property', schema.propertySpec(collection, field)))) as Stmt]
			VectorField: #[new Code.Run(runtime('add_vector', kw('name', name, 'vector', schema.vectorSpec(collection, field)))) as Stmt]
		}
	}

	def private List<Stmt> createPayloadIndex(PayloadIndex index) {
		val target = index.target
		if (!target.subFields.empty || target.rawPath !== null) throw Unsupported.of('payload indexes on nested paths')
		val collection = indexCollection(index)
		schema.note(index, 'structure', 'castType', 'onDisk', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		val field = target.field
		if (!(field instanceof PayloadField)) throw Unsupported.of('a payload index on vector field ' + ModelUtil.fieldName(field))
		val args = kw('name', text(WeaviateSchema.className(collection)), 'property', text(WeaviateSchema.propertyName(collection, field)))
		switch index.kind {
			case KEYWORD: args.put('index', text('filterable'))
			case BOOL,
			case UUID: args.put('index', text('filterable'))
			case INTEGER,
			case FLOAT,
			case DATETIME: args.put('index', text('rangeFilters'))
			case FULL_TEXT: {
				args.put('index', text('searchable'))
				args.put('tokenization', text(if (index.analyzer === null) 'word' else schema.tokenizationOf(index.analyzer)))
			}
			default: throw Unsupported.of(index.kind.literal + ' payload indexes on an existing collection; Weaviate adds filterable, searchable and range indexes')
		}
		#[new Code.Run(runtime('create_property_index', args))]
	}

	def private List<Stmt> addFunction(VectorCollection collection, Function function) {
		switch function.type {
			case BM25: {
				val List<Stmt> result = newArrayList
				for (input : function.inputs) {
					if (!(input instanceof PayloadField)) throw Unsupported.of('bm25 input ' + ModelUtil.fieldName(input) + ', which is not a text field')
					result += new Code.Run(runtime('create_property_index', kw('name', text(WeaviateSchema.className(collection)),
						'property', text(WeaviateSchema.propertyName(collection, input)), 'index', text('searchable'),
						'tokenization', text(if (function.analyzer === null) 'word' else schema.tokenizationOf(function.analyzer)))))
				}
				result
			}
			case TEXT_EMBEDDING: throw Unsupported.of('adding a text-embedding function; Weaviate fixes the vectorizer of a named vector when it creates the vector')
			default: throw Unsupported.of(function.type.literal + ' functions; Weaviate runs no such server-side functions')
		}
	}

	def private List<Stmt> dropFunction(VectorCollection collection, Function function) {
		if (function.type != EFunctionType.BM25) {
			throw Unsupported.of('dropping a ' + function.type.literal + ' function; Weaviate cannot remove a vectorizer from a named vector')
		}
		val List<Stmt> result = newArrayList
		for (input : function.inputs) {
			result += new Code.Run(runtime('drop_property_index', kw('name', text(WeaviateSchema.className(collection)),
				'property', text(WeaviateSchema.propertyName(collection, input)), 'index', text('searchable'))))
		}
		result
	}

	/** The collection a member concept belongs to: its container, or the concept of the enclosing create. */
	def private VectorCollection owner(EObject member) {
		val direct = ModelUtil.owningCollection(member)
		if (direct !== null) return direct
		val statement = EcoreUtil2.getContainerOfType(member, Create)
		val outer = if (statement === null) null else statement.eContainer
		if (outer instanceof Create) {
			val enclosing = (outer as Create).definition ?: (outer as Create).target
			if (enclosing instanceof VectorCollection) return enclosing as VectorCollection
		}
		throw Unsupported.of('a ' + member.eClass.name + ' outside a collection')
	}

	def private VectorCollection indexCollection(Index index) {
		val owned = ModelUtil.owningCollection(index)
		if (owned !== null) return owned
		val field = switch index {
			VectorIndex: index.field ?: EcoreUtil2.getContainerOfType(index, VectorField)
			PayloadIndex: index.target.field
		}
		val collection = if (field === null) null else ModelUtil.owningCollection(field)
		if (collection === null) return owner(index)
		collection
	}

	def dispatch List<Stmt> body(Alter it) {
		val concept = target
		if (!(concept instanceof VectorCollection)) {
			throw Unsupported.of('altering a ' + concept.eClass.name + '; alter operations apply to collections, and Weaviate names are immutable')
		}
		val collection = concept as VectorCollection
		if (newName !== null) throw Unsupported.of('renaming a collection; Weaviate collection names are immutable (an alias serves another name)')
		val List<Stmt> result = newArrayList
		for (operation : operations) result += alter(collection, operation)
		result
	}

	def private List<Stmt> alter(VectorCollection collection, AlterOperation operation) {
		val name = text(WeaviateSchema.className(collection))
		switch operation {
			AddFieldOp: addField(collection, operation.newField)
			DropFieldOp: {
				val field = operation.field
				if (!(field instanceof VectorField)) {
					throw Unsupported.of('dropping property ' + ModelUtil.fieldName(field) + '; Weaviate cannot remove a property from a collection')
				}
				#[new Code.Run(runtime('drop_vector', kw('name', name, 'vector', text(ModelUtil.fieldName(field))))) as Stmt]
			}
			RenameFieldOp: throw Unsupported.of('renaming a field; Weaviate cannot rename properties or named vectors')
			SetPropertyOp: #[setProperty(collection, operation)]
			AddAliasOp: createAlias(operation.newAlias, collection, false)
			AddFunctionOp: addFunction(collection, operation.newFunction)
			DropFunctionOp: dropFunction(collection, operation.function)
			default: throw Unsupported.of(NOT_MAPPED + operation.eClass.name)
		}
	}

	def private Stmt setProperty(VectorCollection collection, SetPropertyOp operation) {
		val args = kw('name', text(WeaviateSchema.className(collection)))
		val value = Literals.of(operation.value)
		switch operation.property {
			case DESCRIPTION: args.put('description', value)
			case TTL: {
				if (!(value instanceof Code.Num)) throw Unsupported.of('a ttl that is not a number of seconds')
				args.put('ttl', schema.ttl(collection, collection.ttlField, (value as Code.Num).value.intValue))
			}
			case CONSISTENCY: throw Unsupported.of('setting consistency; Weaviate sets consistency per request, not on the collection')
			case DELETION_PROTECTION: throw Unsupported.of('deletion-protection; Weaviate has no setting that blocks dropping a collection')
			case DYNAMIC: throw Unsupported.of('setting dynamic; Weaviate enables auto-schema for the whole server')
			case TAGS: throw Unsupported.of('collection tags; Weaviate collections have no tags')
		}
		new Code.Run(runtime('update_collection', args))
	}

	def dispatch List<Stmt> body(Drop it) {
		val concept = target
		switch concept {
			VectorCollection: {
				val name = text(WeaviateSchema.className(concept))
				val drop = new Code.Run(runtime('drop_collection', kw('name', name))) as Stmt
				#[if (ifExists) new Code.When(runtime('has_collection', kw('name', name)), false, #[drop]) else drop]
			}
			Alias: {
				val name = text(WeaviateSchema.className(concept.name))
				val drop = new Code.Run(runtime('drop_alias', kw('alias', name))) as Stmt
				#[if (ifExists) new Code.When(runtime('has_alias', kw('alias', name)), false, #[drop]) else drop]
			}
			Tenant: dropTenant(owner(concept), WeaviateSchema.tenantName(concept.name))
			Namespace: dropTenant(owner(concept), WeaviateSchema.tenantName(concept.key))
			VectorField: #[new Code.Run(runtime('drop_vector', kw('name', text(WeaviateSchema.className(owner(concept))), 'vector', text(ModelUtil.fieldName(concept))))) as Stmt]
			PayloadField: throw Unsupported.of('dropping property ' + concept.name + '; Weaviate cannot remove a property from a collection')
			PayloadIndex: {
				val collection = indexCollection(concept)
				val field = concept.target.field
				val index = switch concept.kind {
					case INTEGER,
					case FLOAT,
					case DATETIME: 'rangeFilters'
					case FULL_TEXT: 'searchable'
					default: 'filterable'
				}
				#[new Code.Run(runtime('drop_property_index', kw('name', text(WeaviateSchema.className(collection)),
					'property', text(WeaviateSchema.propertyName(collection, field)), 'index', text(index)))) as Stmt]
			}
			VectorIndex: throw Unsupported.of('dropping a vector index; Weaviate removes a vector index only together with the embeddings of its named vector')
			Function: dropFunction(owner(concept), concept)
			RoleBinding: #[new Code.Run(runtime('drop_role', kw('role', text(concept.roleName)))) as Stmt]
			Connection: newArrayList
			EmbeddingModel: newArrayList
			GenerativeModel: newArrayList
			Reranker: newArrayList
			Analyzer: newArrayList
			Database: throw Unsupported.of(LOGICAL_DATABASES)
			Partition: throw Unsupported.of(WeaviateSchema.PARTITIONS)
			BackupSchedule: throw Unsupported.of('backup schedules; Weaviate creates backups only on request')
			default: throw Unsupported.of('dropping a ' + concept.eClass.name + '; Weaviate has no such object to remove')
		}
	}

	def private List<Stmt> dropTenant(VectorCollection collection, String name) {
		#[new Code.Run(runtime('drop_tenants', kw('name', text(WeaviateSchema.className(collection)), 'tenants', texts(#[name])))) as Stmt]
	}

	// ---------------------------------------------------------------- DML

	def dispatch List<Stmt> body(Insert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Weaviate sets object ttl on the collection')
		val slice = tenantOf(collection, it)
		schema.note(it, 'recordCountHint')
		val args = kw('collection', scoped(schema.writeLayout(collection, writeConsistency(it)), slice), 'rows', rows(source))
		if (written(it, 'onConflict') && #[EConflictPolicy.UPDATE, EConflictPolicy.REPLACE].contains(onConflict)) {
			return #[new Code.Return(runtime('upsert_rows', args))]
		}
		if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		#[new Code.Return(runtime('insert_rows', args))]
	}

	def dispatch List<Stmt> body(Upsert it) {
		if (written(it, 'ttlSeconds')) throw Unsupported.of('per-statement ttl; Weaviate sets object ttl on the collection')
		val slice = tenantOf(collection, it)
		schema.note(it, 'recordCountHint')
		val args = kw('collection', scoped(schema.writeLayout(collection, writeConsistency(it)), slice), 'rows', rows(source))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		#[new Code.Return(runtime('upsert_rows', args))]
	}

	def dispatch List<Stmt> body(Update it) {
		if (!appendFields.empty || !arrayRemoveFields.empty) throw Unsupported.of('array-append and array-remove; Weaviate replaces array properties as a whole')
		if (!removedFields.empty) throw Unsupported.of('remove-fields has no Weaviate mapping yet')
		if (written(it, 'ifVersion')) throw Unsupported.of('if-version; Weaviate objects carry no version for conditional writes')
		val slice = tenantOf(collection, it)
		schema.note(it, 'recordCountHint')
		val where = new WeaviateFilter(collection).where(filter)
		val args = kw('collection', scoped(schema.writeLayout(collection, writeConsistency(it)), slice))
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (values !== null) args.put('values', record(values))
		if (source !== null) args.put('rows', rows(source))
		if (written(it, 'mode') && mode == EUpdateMode.REPLACE) args.put('mode', text('replace'))
		#[new Code.Return(runtime('update_rows', args))]
	}

	def dispatch List<Stmt> body(Delete it) {
		val slice = tenantOf(collection, it)
		val where = new WeaviateFilter(collection).where(filter)
		val scopedLayout = scoped(schema.layout(collection, writeConsistency(it)), slice)
		if (deleteAll && ids.empty && where.empty) {
			if (written(it, 'deleteLimit')) throw Unsupported.of("'all' together with a limit")
			return #[new Code.Return(runtime('truncate', kw('collection', scopedLayout)))]
		}
		if (ids.empty && where.empty) throw Unsupported.of('a delete without ids or where')
		val args = kw('collection', scopedLayout)
		if (!ids.empty) args.put('ids', items(ids.map[Literals.of(it)]))
		if (!where.empty) args.put('filter', where.code)
		if (written(it, 'deleteLimit')) {
			args.put('limit', num(deleteLimit))
			return #[new Code.Return(runtime('delete_limited', args))]
		}
		#[new Code.Return(runtime('delete_rows', args))]
	}

	/** Statement write consistency in Weaviate's level names. 'wait' adds no call. */
	def private String writeConsistency(EObject statement) {
		if (written(statement, 'writeConsistency')) (feature(statement, 'writeConsistency') as EWriteConsistency).literal.toUpperCase else null
	}

	def dispatch List<Stmt> body(BulkImport it) {
		if (continuous) throw Unsupported.of('continuous ingestion; Weaviate has no pull-based ingestion')
		if (async) throw Unsupported.of('asynchronous bulk import; Weaviate has no server-side import jobs')
		val scheme = if (location.contains('://')) location.substring(0, location.indexOf('://')) else 'file'
		if (scheme != 'file') throw Unsupported.of('bulk import from ' + scheme + '; Weaviate has no server-side ingestion, and the runtime reads local files')
		if (written(it, 'format') && format != EImportFormat.JSON) {
			throw Unsupported.of(format.literal + ' bulk import; Weaviate has no server-side ingestion, and the runtime reads JSON and JSON Lines files')
		}
		schema.note(it, 'parallelism')
		val slice = tenantOf(collection, it)
		val args = kw('collection', scoped(schema.writeLayout(collection, null), slice), 'location', text(location))
		if (written(it, 'batchSize')) args.put('batch_size', num(batchSize))
		if (written(it, 'onConflict')) args.put('on_conflict', text(onConflict.literal))
		extensionArgs(it, args)
		#[new Code.Return(runtime('import_file', args))]
	}

	def dispatch List<Stmt> body(Transaction it) {
		throw Unsupported.of('transactions; Weaviate writes each object on its own and has no multi-object transactions')
	}

	// ---------------------------------------------------------------- operations

	def dispatch List<Stmt> body(Load it) {
		if (!partitions.empty) throw Unsupported.of(WeaviateSchema.PARTITIONS)
		if (!schema.extValues(it).empty) throw Unsupported.of('extension parameters on load')
		#[new Code.Return(runtime('load_collection', kw('name', text(WeaviateSchema.className(collection)), 'multi_tenant', flag(schema.multiTenant(collection)))))]
	}

	def dispatch List<Stmt> body(Release it) {
		if (!partitions.empty) throw Unsupported.of(WeaviateSchema.PARTITIONS)
		if (!schema.multiTenant(collection)) {
			throw Unsupported.of('releasing collection ' + collection.name + '; Weaviate can deactivate tenants but keeps a collection without tenants loaded')
		}
		if (!schema.extValues(it).empty) throw Unsupported.of('extension parameters on release')
		#[new Code.Return(runtime('release_collection', kw('name', text(WeaviateSchema.className(collection)))))]
	}

	/** Weaviate acknowledges a write after logging it, and the next read sees it; flush only checks the collection. */
	def dispatch List<Stmt> body(Flush it) {
		if (!schema.extValues(it).empty) throw Unsupported.of('extension parameters on flush')
		#[new Code.Return(runtime('flush', kw('name', text(WeaviateSchema.className(collection)))))]
	}

	def dispatch List<Stmt> body(Compact it) {
		throw Unsupported.of('compaction; Weaviate compacts its storage in the background and has no API to start it')
	}

	def dispatch List<Stmt> body(Describe it) {
		val args = kw('collection', schema.layout(collection, null))
		if (includeStats) args.put('stats', flag(true))
		if (includeIndexProgress) args.put('index_progress', flag(true))
		#[new Code.Return(runtime('describe', args))]
	}

	def dispatch List<Stmt> body(EmbeddingMigration it) {
		throw Unsupported.of('embedding migration; Weaviate cannot change the vectorizer of a named vector, and a named vector added later is empty for existing objects')
	}

	def dispatch List<Stmt> body(Snapshot it) {
		if (tenant !== null) throw Unsupported.of('backups of one tenant; Weaviate backs up whole collections')
		schema.note(it, 'description')
		val args = backup(snapshotName ?: name, scope, location)
		if (!written(it, 'scope') || scope == ESnapshotScope.COLLECTION) args.put('collections', texts(#[WeaviateSchema.className(collection)]))
		if (incremental) args.put('incremental', flag(true))
		extensionArgs(it, args)
		#[new Code.Return(runtime('create_backup', args))]
	}

	def dispatch List<Stmt> body(Restore it) {
		val args = backup(snapshotName, scope, location)
		if (!written(it, 'scope') || scope == ESnapshotScope.COLLECTION) args.put('collections', texts(#[WeaviateSchema.className(collection)]))
		if (async) args.put('wait', flag(false))
		extensionArgs(it, args)
		#[new Code.Return(runtime('restore_backup', args))]
	}

	def dispatch List<Stmt> body(DropSnapshot it) {
		throw Unsupported.of('drop-snapshot; Weaviate has no API that deletes a backup (DELETE /backups cancels a running one)')
	}

	/** Backup id and storage: the id is the snapshot name in Weaviate's lower case form, the location selects the backend. */
	def private Entries backup(String snapshot, ESnapshotScope backupScope, String location) {
		if (snapshot === null) throw Unsupported.of('a backup without a name; add a statement label or \'as\'')
		if (backupScope == ESnapshotScope.TENANT || backupScope == ESnapshotScope.SHARD) {
			throw Unsupported.of(backupScope.literal + ' backups; Weaviate backs up whole collections')
		}
		val id = Syntax.snake(snapshot).toLowerCase.replace('.', '-')
		if (!id.matches('[a-z0-9_-]+')) throw Unsupported.of("backup id '" + id + "'; Weaviate backup ids use lower case letters, digits, '_' and '-'")
		val args = kw('backup_id', text(id))
		val target = location ?: 'filesystem'
		val separator = target.indexOf('://')
		val scheme = if (separator < 0) target else target.substring(0, separator)
		val backend = BACKENDS.get(scheme)
		if (backend === null) throw Unsupported.of('backup location ' + target + '; Weaviate backs up to filesystem, s3, gcs and azure')
		args.put('backend', text(backend))
		if (separator >= 0) {
			val rest = target.substring(separator + 3)
			if (backend == 'filesystem') {
				args.put('path', text(rest))
			} else {
				val slash = rest.indexOf('/')
				args.put('bucket', text(if (slash < 0) rest else rest.substring(0, slash)))
				if (slash >= 0 && slash < rest.length - 1) args.put('path', text(rest.substring(slash + 1)))
			}
		}
		args
	}

	def dispatch List<Stmt> body(Clone it) {
		if (sourceDatabase !== null) throw Unsupported.of('cloning a database; Weaviate has no logical databases')
		if (asOf !== null) throw Unsupported.of('clone as-of; Weaviate keeps no object history')
		if (written(it, 'mode') && mode == ECloneMode.SHALLOW) throw Unsupported.of('shallow clones; Weaviate has no copy-on-write collections')
		if (!schema.extValues(it).empty) throw Unsupported.of('extension parameters on clone')
		if (schema.multiTenant(sourceCollection)) throw Unsupported.of('cloning a multi-tenant collection has no Weaviate mapping yet')
		#[new Code.Return(runtime('clone_collection', kw('source', schema.layout(sourceCollection, null),
			'target', text(WeaviateSchema.className(targetName)), 'vectors', texts(schema.storedVectors(sourceCollection).map[vector | ModelUtil.fieldName(vector)]))))]
	}

	def dispatch List<Stmt> body(RebuildIndex it) {
		val definition = index
		if (!(definition instanceof PayloadIndex)) {
			throw Unsupported.of('rebuilding a vector index; Weaviate has no API that rebuilds a vector index')
		}
		val payloadIndex = definition as PayloadIndex
		if (!schema.extValues(it).empty) throw Unsupported.of('extension parameters on rebuild-index')
		schema.note(it, 'concurrently')
		val collection = indexCollection(payloadIndex)
		#[new Code.Return(runtime('rebuild_property_index', kw('name', text(WeaviateSchema.className(collection)),
			'property', text(WeaviateSchema.propertyName(collection, payloadIndex.target.field)), 'index', text(propertyIndex(payloadIndex)))))]
	}

	def dispatch List<Stmt> body(SwapAlias it) {
		#[new Code.Run(runtime('swap_alias', kw('alias', text(WeaviateSchema.className(alias.name)),
			'source', text(WeaviateSchema.className(oldCollection)), 'target', text(WeaviateSchema.className(newCollection)))))]
	}

	def dispatch List<Stmt> body(RefreshExternal it) {
		throw Unsupported.of(WeaviateSchema.EXTERNAL)
	}

	def dispatch List<Stmt> body(Truncate it) {
		val slice = if (namespace === null) null else tenantOf(collection, it)
		val args = kw('collection', scoped(schema.layout(collection, null), slice))
		if (slice === null && schema.multiTenant(collection)) args.put('all_tenants', flag(true))
		#[new Code.Return(runtime('truncate', args))]
	}

	def dispatch List<Stmt> body(ListObjects it) {
		val args = kw('kind', text(kind.literal))
		switch kind {
			case COLLECTIONS,
			case ALIASES,
			case SNAPSHOTS: if (scope !== null && !(scope instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + ' in a ' + scope.eClass.name)
			case TENANTS,
			case NAMESPACES,
			case INDEXES: if (!(scope instanceof VectorCollection)) throw Unsupported.of('listing ' + kind.literal + " without 'in <collection>'")
			case PARTITIONS: throw Unsupported.of(WeaviateSchema.PARTITIONS)
			case DATABASES: throw Unsupported.of(LOGICAL_DATABASES)
		}
		if (scope instanceof VectorCollection) args.put('collection', text(WeaviateSchema.className(scope as VectorCollection)))
		if (prefix !== null) args.put('prefix', text(prefix))
		if (written(it, 'listLimit')) args.put('limit', num(listLimit))
		#[new Code.Return(runtime('list_objects', args))]
	}

	def dispatch List<Stmt> body(AwaitIndex it) {
		val definition = index
		val collection = indexCollection(definition)
		val args = kw('name', text(WeaviateSchema.className(collection)))
		switch definition {
			VectorIndex: {
				val field = definition.field ?: EcoreUtil2.getContainerOfType(definition, VectorField)
				args.put('vector', text(ModelUtil.fieldName(field)))
			}
			PayloadIndex: {
				args.put('property', text(WeaviateSchema.propertyName(collection, definition.target.field)))
				args.put('index', text(propertyIndex(definition)))
			}
		}
		if (written(it, 'state')) args.put('until', text(state.literal))
		if (written(it, 'timeoutSeconds')) args.put('timeout', num(timeoutSeconds))
		#[new Code.Return(runtime('await_index', args))]
	}

	/** The Weaviate inverted index that serves a payload index of the given kind. */
	def private String propertyIndex(PayloadIndex definition) {
		switch definition.kind {
			case INTEGER,
			case FLOAT,
			case DATETIME: 'rangeFilters'
			case FULL_TEXT: 'searchable'
			default: 'filterable'
		}
	}

	// ---------------------------------------------------------------- queries

	def dispatch List<Stmt> body(VectorSearch it) {
		reject(it, 'elementScope', 'asOf', 'cursor', 'explain', 'highlighted', 'sortClauses', 'prefetches', 'exact', 'queryModel')
		pending(it, 'withTotal', 'mmr', 'reranker', 'aggregations', 'groupBy', 'boost')
		val field = searchField(collection, vectorField)
		val List<Stmt> prelude = newArrayList
		val vector = queryVector(input, collection, field, 'query_vector', prelude)
		val metric = indexMetric(collection, field)
		if (written(it, 'metric') && it.metric != metric) throw Unsupported.of('a query metric other than the metric of the vector index')
		val args = kw('collection', layout(collection, it), 'field', text(ModelUtil.fieldName(field)), 'vector', vector,
			'limit', num(topK), 'metric', text(schema.distance(metric)))
		vectorBounds(bounds, metric, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		searchParams(params, args)
		extensionArgs(it, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('vector_search', args))
		result
	}

	def dispatch List<Stmt> body(KeywordSearch it) {
		reject(it, 'asOf', 'cursor', 'highlighted', 'sortClauses', 'fuzziness', 'slop')
		pending(it, 'withTotal', 'explain', 'mmr', 'reranker', 'aggregations', 'groupBy', 'boost')
		if (input.kind != EQueryInputMode.TEXT) throw Unsupported.of('keyword search with ' + input.kind.literal + ' input')
		if (written(it, 'textQueryType') && textQueryType != ETextQueryType.MATCH) throw Unsupported.of('text-query ' + textQueryType.literal)
		val args = kw('collection', layout(collection, it), 'query', Literals.of(input.value), 'limit', num(topK))
		keywordFields(collection, searchFields, args)
		bm25Operator(it, args)
		if (bounds !== null) {
			if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
			if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
		}
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		searchParams(params, args)
		extensionArgs(it, args)
		#[new Code.Return(runtime('keyword_search', args))]
	}

	def dispatch List<Stmt> body(HybridSearch it) {
		reject(it, 'asOf', 'cursor', 'highlighted', 'sortClauses', 'bounds', 'params')
		pending(it, 'withTotal', 'explain', 'mmr', 'reranker', 'aggregations', 'groupBy', 'boost')
		val vectorLegs = legs.filter(VectorLeg).toList
		val keywordLegs = legs.filter(KeywordLeg).toList
		if (vectorLegs.size != 1 || keywordLegs.size != 1) throw Unsupported.of('hybrid search with other than one vector leg and one keyword leg')
		val vectorLeg = vectorLegs.head
		val keywordLeg = keywordLegs.head
		for (leg : #[vectorLeg, keywordLeg]) {
			if (written(leg, 'legLimit')) throw Unsupported.of('per-leg limits; Weaviate hybrid search takes one limit')
			if (feature(leg, 'filter') !== null) throw Unsupported.of('per-leg filters; Weaviate hybrid search takes one filter')
		}
		reject(vectorLeg, 'exact', 'params')
		reject(keywordLeg, 'textQueryType', 'minShouldMatch')
		val keywordInput = keywordLeg.input ?: input
		if (keywordInput === null || keywordInput.kind != EQueryInputMode.TEXT) throw Unsupported.of('a keyword leg without text input')
		val field = searchField(collection, vectorLeg.vectorField)
		val List<Stmt> prelude = newArrayList
		val vector = queryVector(vectorLeg.input ?: input, collection, field, 'query_vector', prelude)
		val metric = indexMetric(collection, field)
		if (written(vectorLeg, 'metric') && vectorLeg.metric != metric) throw Unsupported.of('a query metric other than the metric of the vector index')
		val args = kw('collection', layout(collection, it), 'query', Literals.of(keywordInput.value), 'vector', vector,
			'field', text(ModelUtil.fieldName(field)), 'limit', num(topK), 'fusion', text(fusionName(it)),
			'alpha', num(vectorWeight(it, vectorLeg, keywordLeg)))
		keywordFields(collection, keywordLeg.searchFields, args)
		if (written(keywordLeg, 'matchOperator') && keywordLeg.matchOperator == EMatchOperator.AND) args.put('operator', text('and'))
		val legBounds = vectorLeg.bounds
		if (legBounds !== null) {
			if (written(legBounds, 'max')) throw Unsupported.of('an upper similarity bound on the vector leg')
			if (written(legBounds, 'min')) args.put('max_vector_distance', num(distance(metric, legBounds.min)))
		}
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		extensionArgs(it, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('hybrid_search', args))
		result
	}

	def private String fusionName(HybridSearch search) {
		val kind = if (written(search, 'fusion')) search.fusion else EFusionType.RRF
		switch kind {
			case RRF: {
				if (written(search, 'fusionConstant') && search.fusionConstant != 60) {
					throw Unsupported.of('fusion-k ' + search.fusionConstant + '; Weaviate ranked fusion uses k = 60')
				}
				'ranked'
			}
			case RELATIVE_SCORE: 'relative_score'
			default: throw Unsupported.of(kind.literal + ' fusion; Weaviate fuses by rank or by relative score')
		}
	}

	/** Weaviate alpha is the weight of the vector leg; vecDSL legs without weights weigh equally. */
	def private double vectorWeight(HybridSearch search, VectorLeg vectorLeg, KeywordLeg keywordLeg) {
		if (written(search, 'alpha')) return search.alpha
		if (written(vectorLeg, 'weight') && written(keywordLeg, 'weight')) {
			val total = vectorLeg.weight + keywordLeg.weight
			if (total <= 0) throw Unsupported.of('leg weights that do not sum to a positive value')
			return vectorLeg.weight / total
		}
		if (written(vectorLeg, 'weight') || written(keywordLeg, 'weight')) throw Unsupported.of('a weight on only one hybrid leg')
		0.5
	}

	def dispatch List<Stmt> body(RecommendSearch it) {
		reject(it, 'asOf', 'cursor', 'explain', 'highlighted', 'sortClauses', 'prefetches')
		pending(it, 'withTotal', 'mmr', 'reranker', 'aggregations', 'groupBy', 'boost')
		if (positives.empty) throw Unsupported.of('recommend without positive examples')
		val source = lookupCollection ?: collection
		val field = searchField(collection, vectorField)
		val examples = kw('collection', layout(source, it), 'field', text(ModelUtil.fieldName(field)),
			'positives', items(positives.map[Literals.of(it)]), 'negatives', items(negatives.map[Literals.of(it)]))
		val metric = indexMetric(collection, field)
		val args = kw('collection', layout(collection, it), 'field', text(ModelUtil.fieldName(field)), 'vector', new Code.Ref('query_vector'),
			'limit', num(topK), 'metric', text(schema.distance(metric)))
		if (source === collection) args.put('exclude', items((positives + negatives).map[Literals.of(it)].toList))
		vectorBounds(bounds, metric, written(it, 'scoreForm') && scoreForm == EScoreForm.NATIVE, args)
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		searchParams(params, args)
		extensionArgs(it, args)
		newArrayList(new Code.Let('query_vector', runtime('recommend_vector', examples)) as Stmt, new Code.Return(runtime('vector_search', args)))
	}

	def dispatch List<Stmt> body(GetById it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'ids', items(ids.map[Literals.of(it)]))
		projection(collection, args, output, newArrayList, withVectors)
		extensionArgs(it, args)
		#[new Code.Return(runtime('get_rows', args))]
	}

	def dispatch List<Stmt> body(Count it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it))
		val where = new WeaviateFilter(collection).where(filter)
		if (!where.empty) args.put('filter', where.code)
		extensionArgs(it, args)
		#[new Code.Return(runtime('count', args))]
	}

	def dispatch List<Stmt> body(Scroll it) {
		reject(it, 'sliceIndex', 'asOf')
		pending(it, 'cursor', 'sortClauses')
		val args = kw('collection', layout(collection, it))
		if (written(it, 'scrollLimit')) args.put('limit', num(scrollLimit))
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		extensionArgs(it, args)
		#[new Code.Return(runtime('scroll', args))]
	}

	def dispatch List<Stmt> body(GenerativeSearch it) {
		reject(it, 'asOf')
		pending(it, 'mmr', 'boost', 'reranker')
		val module = if (collection.generativeModel !== null) collection.generativeModel else collection.generativeModels.head
		if (module === null) throw Unsupported.of('generative search on collection ' + collection.name + ', which has no generative-model; Weaviate generates with the module of the collection')
		if (generativeModel !== null && generativeModel !== module) {
			throw Unsupported.of('a generative model other than the collection\'s has no Weaviate mapping yet')
		}
		if (singlePrompt === null && groupedTask === null) throw Unsupported.of('generate without single-prompt or grouped-task')
		val args = kw('collection', layout(collection, it), 'limit', num(topK))
		val List<Stmt> prelude = newArrayList
		if (input !== null) {
			switch input.kind {
				case TEXT: {
					val field = searchField(collection, vectorField)
					if (schema.embedding(collection, field) === null) {
						throw Unsupported.of('text input for vector field ' + ModelUtil.fieldName(field) + ', which has no embedding model; Weaviate embeds query text with the vectorizer of the named vector')
					}
					args.put('query', Literals.of(input.value))
					args.put('field', text(ModelUtil.fieldName(field)))
				}
				default: {
					val field = searchField(collection, vectorField)
					args.put('vector', queryVector(input, collection, field, 'query_vector', prelude))
					args.put('field', text(ModelUtil.fieldName(field)))
				}
			}
		}
		if (singlePrompt !== null) args.put('single_prompt', prompt(collection, Literals.of(singlePrompt)))
		if (groupedTask !== null) args.put('grouped_task', Literals.of(groupedTask))
		val searched = collection
		if (!groupedFields.empty) args.put('grouped_properties', texts(groupedFields.map[field | WeaviateSchema.propertyName(searched, field)]))
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		extensionArgs(it, args)
		val List<Stmt> result = newArrayList
		result += prelude
		result += new Code.Return(runtime('generate', args))
		result
	}

	/** Weaviate prompts name properties as {property}; a renamed key property is referred to by its stored name. */
	def private Expr prompt(VectorCollection collection, Expr template) {
		if (!(template instanceof Code.Text)) return template
		val key = ModelUtil.fieldName(collection.primaryKey)
		val stored = WeaviateSchema.keyProperty(collection)
		text((template as Code.Text).value.replace('{' + key + '}', '{' + stored + '}'))
	}

	def dispatch List<Stmt> body(DistanceMatrix it) {
		reject(it, 'asOf')
		val field = searchField(collection, vectorField)
		val metric = indexMetric(collection, field)
		val args = kw('collection', layout(collection, it), 'field', text(ModelUtil.fieldName(field)), 'metric', text(schema.distance(metric)))
		if (written(it, 'sampleSize')) args.put('sample', num(sampleSize))
		if (written(it, 'matrixLimit')) args.put('limit', num(matrixLimit))
		val where = new WeaviateFilter(collection).where(filter)
		if (!where.empty) args.put('filter', where.code)
		extensionArgs(it, args)
		#[new Code.Return(runtime('distance_matrix', args))]
	}

	def dispatch List<Stmt> body(Sample it) {
		reject(it, 'asOf')
		val args = kw('collection', layout(collection, it), 'size', num(sampleSize))
		if (written(it, 'method')) args.put('method', text(method.literal))
		common(it, collection, args, output, new WeaviateFilter(collection).where(filter))
		extensionArgs(it, args)
		#[new Code.Return(runtime('sample', args))]
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

	def private Entries layout(VectorCollection collection, EObject query) {
		val level = if (written(query, 'consistency')) schema.consistency(feature(query, 'consistency') as EConsistencyLevel) else null
		scoped(schema.layout(collection, level), tenantOf(collection, query))
	}

	def private void common(EObject query, VectorCollection collection, Entries args, OutputSpec output, WeaviateFilter where) {
		if (!where.empty) args.put('filter', where.code)
		if (written(query, 'offset')) args.put('offset', num((feature(query, 'offset') as Integer).doubleValue))
		val excluded = (feature(query, 'excluded') ?: newArrayList) as List<Field>
		projection(collection, args, output, excluded, Boolean.TRUE == feature(query, 'withVectors'))
	}

	/** Output payload fields (the key is always returned) and the named vectors to include. */
	def private void projection(VectorCollection collection, Entries args, OutputSpec output, List<Field> excluded, boolean withVectors) {
		val key = collection.primaryKey
		val List<String> fields = newArrayList
		val List<String> vectors = newArrayList
		val stored = schema.storedVectors(collection)
		val listed = if (output === null || output.allFields) ModelUtil.payloadFields(collection) as List<? extends Field> else output.fields
		for (field : listed.filter[it !== key && !excluded.contains(it)]) {
			switch field {
				PayloadField: fields += WeaviateSchema.propertyName(collection, field)
				VectorField: if (stored.contains(field)) vectors += ModelUtil.fieldName(field)
				else notes += 'output of ' + ModelUtil.fieldName(field) + ', which Weaviate keeps in the inverted index'
			}
		}
		if (withVectors) vectors += stored.map[ModelUtil.fieldName(it)]
		args.put('output', texts(fields))
		if (!vectors.empty) args.put('vectors', texts(vectors.stream.distinct.toList))
	}

	/** The Weaviate tenant a statement addresses: its tenant or namespace, which a multi-tenant collection requires. */
	def private String tenantOf(VectorCollection collection, EObject statement) {
		for (feature : #['partitions', 'partition']) {
			if (written(statement, feature)) throw Unsupported.of(WeaviateSchema.PARTITIONS)
		}
		val named = feature(statement, 'tenant') as Tenant
		val key = feature(statement, 'namespace') as String
		if (named !== null && key !== null) throw Unsupported.of('a tenant together with a namespace; Weaviate has one tenant level')
		val slice = if (named !== null) WeaviateSchema.tenantName(named.name) else if (key !== null) WeaviateSchema.tenantName(key) else null
		if (slice === null && schema.multiTenant(collection)) {
			throw Unsupported.of('a request on multi-tenant collection ' + collection.name + ' without a tenant or namespace; Weaviate needs one')
		}
		if (slice !== null && !schema.multiTenant(collection)) {
			throw Unsupported.of('a tenant or namespace on collection ' + collection.name + ', which declares none; Weaviate enables multi-tenancy only when it creates a collection')
		}
		slice
	}

	def private Entries scoped(Entries collectionLayout, String slice) {
		if (slice !== null) collectionLayout.put('tenant', text(slice))
		collectionLayout
	}

	def private void reject(EObject statement, String... features) {
		for (feature : features) {
			if (written(statement, feature)) {
				val reason = REASONS.get(feature)
				throw Unsupported.of("the '" + (CLAUSES.get(feature) ?: feature) + "' clause" + (if (reason === null) '' else '; ' + reason))
			}
		}
	}

	/** Clauses Weaviate supports that the generator does not map yet. */
	def private void pending(EObject statement, String... features) {
		for (feature : features) {
			if (written(statement, feature)) throw Unsupported.of("the '" + (CLAUSES.get(feature) ?: feature) + "' clause has no Weaviate mapping yet")
		}
	}

	def private Object feature(EObject object, String name) {
		val feature = object.eClass.getEStructuralFeature(name)
		if (feature === null) null else object.eGet(feature)
	}

	def private VectorField searchField(VectorCollection collection, VectorField explicit) {
		if (explicit !== null) {
			if (WeaviateSchema.isBm25Output(collection, explicit)) throw Unsupported.of('vector search on the bm25 output ' + ModelUtil.fieldName(explicit) + '; use keyword search')
			return explicit
		}
		val candidates = schema.storedVectors(collection)
		if (candidates.size != 1) throw Unsupported.of("choosing among several vector fields; add 'using <field>'")
		candidates.head
	}

	def private EMetric indexMetric(VectorCollection collection, VectorField field) {
		schema.metric(field, ModelUtil.vectorIndex(collection, field))
	}

	def private Expr queryVector(QueryInput input, VectorCollection collection, VectorField field, String variable, List<Stmt> prelude) {
		if (input === null) throw Unsupported.of('a vector query without input')
		switch input.kind {
			case VECTOR: Literals.of(input.value)
			case ID: {
				val source = input.lookupCollection ?: collection
				prelude += new Code.Let(variable, runtime('lookup_vector', kw('collection', schema.layout(source, null),
					'field', text(ModelUtil.fieldName(field)), 'id', Literals.of(input.value))))
				new Code.Ref(variable)
			}
			case TEXT: throw Unsupported.of('text input for vector field ' + ModelUtil.fieldName(field) + ' (Weaviate vectorizer modules are not generated)')
			default: throw Unsupported.of(input.kind.literal + ' query input')
		}
	}

	/** Weaviate filters vector hits by a maximum distance; the runtime applies the other bound to the reported score. */
	def private void vectorBounds(ScoreBounds bounds, EMetric metric, boolean nativeScore, Entries args) {
		if (nativeScore) args.put('native', flag(true))
		if (bounds === null) return;
		if (nativeScore) {
			if (written(bounds, 'max')) args.put('max_distance', num(bounds.max))
			if (written(bounds, 'min')) args.put('score_min', num(bounds.min))
			return
		}
		if (written(bounds, 'min')) args.put('max_distance', num(distance(metric, bounds.min)))
		if (written(bounds, 'max')) args.put('score_max', num(bounds.max))
	}

	/** Weaviate distance for a canonical similarity. */
	def private double distance(EMetric metric, double similarity) {
		switch metric {
			case COSINE: 1 - similarity
			case EUCLIDEAN: {
				if (similarity > 0) throw Unsupported.of('a positive similarity bound on euclidean distance')
				similarity * similarity
			}
			default: -similarity
		}
	}

	def private void keywordFields(VectorCollection collection, List<SearchFieldRef> refs, Entries args) {
		val List<String> properties = newArrayList
		for (ref : refs) {
			val boost = if (written(ref, 'boost')) '^' + Syntax.number(ref.boost) else ''
			val function = ModelUtil.producingFunction(collection, ref.field)
			val fields = if (function !== null && function.type == EFunctionType.BM25) function.inputs else #[ref.field]
			for (field : fields) {
				if (!(field instanceof PayloadField)) throw Unsupported.of('keyword search on vector field ' + ModelUtil.fieldName(field))
				properties += WeaviateSchema.propertyName(collection, field) + boost
			}
		}
		if (!properties.empty) args.put('properties', texts(properties.stream.distinct.toList))
	}

	def private void bm25Operator(KeywordSearch search, Entries args) {
		if (written(search, 'matchOperator') && search.matchOperator == EMatchOperator.AND) {
			if (written(search, 'minShouldMatch')) throw Unsupported.of("min-should-match with operator 'and'")
			args.put('operator', text('and'))
		} else if (written(search, 'minShouldMatch')) {
			args.put('minimum_match', num(search.minShouldMatch))
		}
	}

	def private void searchParams(SearchParams params, Entries args) {
		if (params === null) return;
		if (written(params, 'targetRecall')) {
			throw Unsupported.of('target-recall: Weaviate exposes no query-time recall target')
		}
		if (written(params, 'candidatePool')) {
			throw Unsupported.of('candidates; Weaviate sets ef on the vector index, not per query')
		}
		schema.note(params, 'probes', 'scanBudget', 'timeoutMillis', 'oversampling', 'rescore', 'filterStrategy', 'exhaustive')
		extensionArgs(params, args)
	}

	def private void extensionArgs(EObject host, Entries args) {
		for (entry : schema.extValues(host).entrySet) {
			switch entry.key {
				case 'autocut': args.put('auto_limit', entry.value)
				default: throw Unsupported.of('extension parameter ' + entry.key)
			}
		}
	}

	def private Expr rows(RecordSource source) {
		switch source {
			FileSource: throw Unsupported.of('records read from files')
			InlineRecords: items(source.records.map[record(it)])
			ParamSource: Literals.of(source.param)
		}
	}

	def private Expr record(RecordLiteral record) {
		val result = entries
		for (entry : record.entries) result.put(ModelUtil.fieldName(entry.field), Literals.of(entry.value))
		result
	}
}
