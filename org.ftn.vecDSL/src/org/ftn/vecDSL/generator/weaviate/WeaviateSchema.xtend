package org.ftn.vecDSL.generator.weaviate

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Syntax
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Cluster
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType
import org.ftn.vecDSL.vecDSL.ECollectionLimit
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EFieldLimit
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EIndexFamily
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPartitionStrategy
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.EStorageType
import org.ftn.vecDSL.vecDSL.ETokenizer
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.GenerativeModel
import org.ftn.vecDSL.vecDSL.InvertedIndexSettings
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.ReplicationConfig
import org.ftn.vecDSL.vecDSL.Reranker
import org.ftn.vecDSL.vecDSL.ShardConfig
import org.ftn.vecDSL.vecDSL.StorageConfig
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Weaviate collection definitions: properties, self-provided named vectors and the
 * inverted index. Weaviate object ids are UUIDs and 'id' is a reserved property name, so the primary key is
 * stored as a property (renamed when reserved) and the object UUID is derived from it by the runtime.
 */
class WeaviateSchema {

	static val RESERVED = #{'id', '_id', '_additional'}
	static val RESERVED_SUFFIXES = #['_searchable', '_rangeable', '_temp', '__meta_count', '_propertyLength', '_nullState']

	public static val PARTITIONS = 'partitions; Weaviate places shards by object id, so they cannot be named, and a request on a multi-tenant collection targets one tenant'
	public static val EXTERNAL = 'collections derived from external sources; Weaviate stores the objects of every collection itself'

	static val TENANT_NAME = '[A-Za-z0-9_-]{1,64}'
	static val VECTORIZERS = #{
		'aws' -> 'text2vec-aws', 'cohere' -> 'text2vec-cohere', 'databricks' -> 'text2vec-databricks',
		'google' -> 'text2vec-google', 'huggingface' -> 'text2vec-huggingface', 'jinaai' -> 'text2vec-jinaai',
		'mistral' -> 'text2vec-mistral', 'model2vec' -> 'text2vec-model2vec', 'nvidia' -> 'text2vec-nvidia',
		'ollama' -> 'text2vec-ollama', 'openai' -> 'text2vec-openai', 'transformers' -> 'text2vec-transformers',
		'voyageai' -> 'text2vec-voyageai', 'weaviate' -> 'text2vec-weaviate'
	}
	static val GENERATORS = #{
		'anthropic' -> 'generative-anthropic', 'aws' -> 'generative-aws', 'cohere' -> 'generative-cohere',
		'databricks' -> 'generative-databricks', 'deepseek' -> 'generative-deepseek', 'dummy' -> 'generative-dummy',
		'google' -> 'generative-google', 'mistral' -> 'generative-mistral', 'nvidia' -> 'generative-nvidia',
		'ollama' -> 'generative-ollama', 'openai' -> 'generative-openai', 'xai' -> 'generative-xai'
	}
	static val RERANKERS = #{
		'cohere' -> 'reranker-cohere', 'dummy' -> 'reranker-dummy', 'jinaai' -> 'reranker-jinaai',
		'nvidia' -> 'reranker-nvidia', 'transformers' -> 'reranker-transformers', 'voyageai' -> 'reranker-voyageai'
	}

	val List<String> notes

	new(List<String> notes) {
		this.notes = notes
	}

	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists) {
		if (c.external !== null) throw Unsupported.of(EXTERNAL)
		if (c.autoId) throw Unsupported.of('auto-id has no Weaviate mapping yet')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref has no Weaviate mapping yet')
		if (!c.partitions.empty || c.defaultPartition !== null) throw Unsupported.of(PARTITIONS)
		if (c.deletionProtection) throw Unsupported.of('deletion-protection; Weaviate has no setting that blocks dropping a collection')
		note(c, 'idType', 'tags', 'storedSourceFields')
		val args = kw(
			'name', text(className(c)),
			'properties', items(ModelUtil.payloadFields(c).map[property(c, it, false)]),
			'vectors', items(storedVectors(c).map[vector(c, it)])
		)
		val inverted = invertedIndex(c)
		if (!inverted.entries.empty) args.put('inverted_index', inverted)
		if (c.description !== null) args.put('description', text(c.description))
		collectionSettings(c, args)
		val List<Stmt> body = newArrayList
		body += new Code.Run(runtime('create_collection', args))
		val tenants = tenantNames(c)
		if (!tenants.empty) body += new Code.Run(runtime('create_tenants', kw('name', text(className(c)), 'tenants', texts(tenants))))
		for (alias : c.aliases) body += new Code.Run(runtime('create_alias', kw('alias', text(className(alias.name)), 'collection', text(className(c)))))
		if (!ifNotExists) return body
		newArrayList(new Code.When(runtime('has_collection', kw('name', text(className(c)))), true, body) as Stmt)
	}

	/** Settings beyond the schema, added only when the collection declares them. */
	def private void collectionSettings(VectorCollection c, Entries args) {
		if (multiTenant(c)) {
			val tenancy = kw('enabled', flag(true))
			if (!c.namespaces.empty) tenancy.put('auto_tenant_creation', flag(true))
			args.put('multi_tenancy', tenancy)
		}
		if (c.ttlField !== null || written(c, 'ttlSeconds')) args.put('ttl', ttl(c, c.ttlField, if (written(c, 'ttlSeconds')) c.ttlSeconds else null))
		val cluster = c.cluster ?: EcoreUtil2.getContainerOfType(c, Cluster)
		if (cluster !== null) {
			if (!cluster.limits.empty) throw Unsupported.of('cluster limits; Weaviate sets them only through server environment variables')
			if (cluster.replication !== null) args.put('replication', replication(cluster.replication))
			if (cluster.sharding !== null) args.put('sharding', sharding(cluster.sharding))
			if (!extValues(cluster).empty) throw Unsupported.of('extension parameters on a cluster')
		}
		if (c.storage !== null) storage(c.storage)
		for (limit : c.limits) {
			if (limit.type != ECollectionLimit.MAX_VECTOR_DIM) {
				throw Unsupported.of('collection-limit ' + limit.type.literal + '; Weaviate has no per-collection capacity limits')
			}
			for (field : storedVectors(c)) {
				if (field.dimension <= 0 || field.dimension > limit.value) {
					throw Unsupported.of('vector field ' + ModelUtil.fieldName(field) + ' without a dimension within max_vector_dim ' + limit.value + '; Weaviate checks no dimension limit, the runtime checks the declared dimension')
				}
			}
		}
		val generative = c.generativeModel ?: c.generativeModels.head
		if (c.generativeModels.size > 1 || (c.generativeModel !== null && !c.generativeModels.empty && !c.generativeModels.contains(c.generativeModel))) {
			throw Unsupported.of('several generative models on one collection; Weaviate configures one generative module per collection')
		}
		if (generative !== null) args.put('generative', generativeModule(generative))
	}

	def boolean multiTenant(VectorCollection c) {
		!c.tenants.empty || !c.namespaces.empty
	}

	/** Weaviate tenants declared by a collection: its tenants and its namespaces (by key). */
	def List<String> tenantNames(VectorCollection c) {
		val List<String> names = newArrayList
		for (tenant : c.tenants) {
			note(tenant, 'extensions')
			names += tenantName(tenant.name)
		}
		for (namespace : c.namespaces) names += tenantName(namespace.key)
		names
	}

	def static String tenantName(String name) {
		if (!name.matches(TENANT_NAME)) {
			throw Unsupported.of("tenant name '" + name + "'; Weaviate tenant names have 1 to 64 letters, digits, '-' or '_'")
		}
		name
	}

	def Entries ttl(VectorCollection c, Field field, Integer seconds) {
		val result = kw('filter_expired', flag(true))
		if (field !== null) {
			if (!(field instanceof PayloadField) || dataType(c, field as PayloadField) != 'date') {
				throw Unsupported.of('ttl-field ' + ModelUtil.fieldName(field) + '; Weaviate measures object ttl from a date property')
			}
			result.put('date_property', text(propertyName(c, field)))
		}
		if (seconds !== null) result.put('seconds', num(seconds))
		result
	}

	def private Entries replication(ReplicationConfig config) {
		if (!extValues(config).empty) throw Unsupported.of('extension parameters on replication')
		kw('factor', num(config.factor))
	}

	def private Entries sharding(ShardConfig config) {
		if (config.key !== null) throw Unsupported.of('shard keys; Weaviate shards by object id')
		if (written(config, 'strategy') && config.strategy != EPartitionStrategy.HASH) {
			throw Unsupported.of(config.strategy.literal + ' sharding; Weaviate shards by a hash of the object id')
		}
		if (!extValues(config).empty) throw Unsupported.of('extension parameters on sharding')
		kw('desired_count', num(config.shards))
	}

	/** Weaviate persists every collection on disk; no other storage medium is selectable per collection. */
	def private void storage(StorageConfig config) {
		if (config.type != EStorageType.DISK) throw Unsupported.of('storage ' + config.type.literal + '; Weaviate persists every collection on disk')
		if (config.location !== null) throw Unsupported.of('storage location; Weaviate stores collections under its PERSISTENCE_DATA_PATH')
		if (config.mmap || config.tiered) throw Unsupported.of('mmap and tiered storage per collection; Weaviate sets its storage strategy for the whole server')
		if (!extValues(config).empty) throw Unsupported.of('extension parameters on storage')
	}

	def Entries generativeModule(GenerativeModel model) {
		val module = GENERATORS.get((model.provider ?: '').toLowerCase)
		if (module === null) throw Unsupported.of("generative provider '" + model.provider + "'; Weaviate has no generative module for it")
		val config = kw()
		if (model.modelId !== null) config.put('model', text(model.modelId))
		if (written(model, 'temperature')) config.put('temperature', num(model.temperature))
		if (written(model, 'maxTokens')) config.put('maxTokens', num(model.maxTokens))
		if (written(model, 'topP')) config.put('topP', num(model.topP))
		putExtensions(model, config)
		kw('module', text(module), 'config', config)
	}

	def Entries rerankerModule(Reranker reranker) {
		val module = RERANKERS.get((reranker.provider ?: '').toLowerCase)
		if (module === null) throw Unsupported.of("reranker provider '" + reranker.provider + "'; Weaviate has no reranker module for it")
		val config = kw()
		if (reranker.modelId !== null) config.put('model', text(reranker.modelId))
		putExtensions(reranker, config)
		kw('module', text(module), 'config', config)
	}

	def Entries vectorizerModule(EmbeddingModel model) {
		if (model.localPath !== null || model.builtIn) throw Unsupported.of('built-in and local embedding models; Weaviate embeds through vectorizer modules')
		if (written(model, 'chunkStrategy') || written(model, 'chunkSize') || written(model, 'chunkOverlap')) {
			throw Unsupported.of('chunking; Weaviate vectorizes each object as a whole')
		}
		val module = VECTORIZERS.get((model.provider ?: '').toLowerCase)
		if (module === null) throw Unsupported.of("embedding provider '" + model.provider + "'; Weaviate has no vectorizer module for it")
		note(model, 'version', 'deprecated', 'replacedBy')
		val config = kw()
		if (model.modelId !== null) config.put('model', text(model.modelId))
		if (written(model, 'dimension')) config.put('dimensions', num(model.dimension))
		putExtensions(model, config)
		kw('module', text(module), 'config', config)
	}

	/** Module options from the Weaviate extension of a model concept, passed to the module unchanged. */
	def private void putExtensions(EObject model, Entries config) {
		for (entry : extValues(model).entrySet) config.put(entry.key, entry.value)
	}

	/** Collection identity every runtime call needs: Weaviate name, key field and the property that stores it. */
	def Entries layout(VectorCollection c, String level) {
		val args = kw('name', text(className(c)), 'key', text(ModelUtil.fieldName(c.primaryKey)), 'key_property', text(keyProperty(c)))
		val effective = level ?: (if (written(c, 'consistency')) consistency(c.consistency) else null)
		if (effective !== null) args.put('consistency', text(effective))
		args
	}

	/** The layout plus what writes need: vector fields, declared limits and default values. */
	def Entries writeLayout(VectorCollection c, String level) {
		val args = layout(c, level)
		args.put('vectors', texts(storedVectors(c).map[ModelUtil.fieldName(it)]))
		val declared = limits(c)
		if (!declared.entries.empty) args.put('limits', declared)
		val defaults = entries
		for (field : ModelUtil.payloadFields(c).filter[defaultValue !== null]) {
			defaults.put(ModelUtil.fieldName(field), Literals.of(field.defaultValue))
		}
		if (!defaults.entries.empty) args.put('defaults', defaults)
		args
	}

	def static String className(VectorCollection c) {
		className(c.name)
	}

	def static String className(String name) {
		name.substring(0, 1).toUpperCase + name.substring(1)
	}

	def static String keyProperty(VectorCollection c) {
		val name = ModelUtil.fieldName(c.primaryKey)
		if (RESERVED.contains(name)) name + '_key' else name
	}

	def static String propertyName(VectorCollection c, Field field) {
		if (field === c.primaryKey) return keyProperty(c)
		checkedName(ModelUtil.fieldName(field))
	}

	def private static String checkedName(String name) {
		if (RESERVED.contains(name)) throw Unsupported.of("field name '" + name + "', which Weaviate reserves")
		if (Character.isUpperCase(name.charAt(0))) {
			throw Unsupported.of("field name '" + name + "'; Weaviate property names start with a lower case letter")
		}
		val suffix = RESERVED_SUFFIXES.findFirst[name.endsWith(it)]
		if (suffix !== null) throw Unsupported.of("field name '" + name + "'; Weaviate reserves the suffix " + suffix)
		name
	}

	/** Vector fields stored as named vectors: all except the sparse outputs of bm25 functions. */
	def List<VectorField> storedVectors(VectorCollection c) {
		ModelUtil.vectorFields(c).filter[!isBm25Output(c, it)].toList
	}

	def static boolean isBm25Output(VectorCollection c, Field field) {
		val function = ModelUtil.producingFunction(c, field)
		function !== null && function.type == EFunctionType.BM25
	}

	/** Property definition of a payload field added to an existing collection. */
	def Entries propertySpec(VectorCollection c, PayloadField field) {
		property(c, field, false)
	}

	/** Named-vector definition of a vector field added to an existing collection. */
	def Entries vectorSpec(VectorCollection c, VectorField field) {
		if (isBm25Output(c, field)) throw Unsupported.of('adding the bm25 output ' + ModelUtil.fieldName(field) + '; Weaviate has no sparse vectors')
		vector(c, field)
	}

	def String tokenizationOf(Analyzer analyzer) {
		tokenization(analyzer)
	}

	def private Entries property(VectorCollection c, PayloadField field, boolean nested) {
		val name = if (nested) checkedName(ModelUtil.fieldName(field)) else propertyName(c, field)
		if (field.partitionKey) throw Unsupported.of(PARTITIONS)
		val type = dataType(c, field)
		val args = kw('name', text(name), 'data_type', text(type))
		val textual = type == 'text' || type == 'text[]'
		val indexes = c.indexes.filter(PayloadIndex).filter[target.field === field].toList
		for (index : indexes) {
			if (!index.target.subFields.empty || index.target.rawPath !== null) throw Unsupported.of('payload indexes on nested paths')
			note(index, 'structure', 'castType', 'onDisk', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		}
		val analyzer = field.analyzer ?: indexes.map[it.analyzer].filterNull.head
		val searchable = field.fullText || analyzer !== null || bm25Inputs(c).contains(field)
			|| indexes.exists[kind == EPayloadIndexKind.FULL_TEXT]
		if (searchable) {
			if (!textual) throw Unsupported.of('full-text search on ' + type + ' field ' + name)
			args.put('index_searchable', flag(true))
		}
		if (analyzer !== null) {
			args.put('tokenization', text(tokenization(analyzer)))
			if (analyzer.filters.exists[it.type == EAnalyzerFilterType.ASCII_FOLDING]) args.put('ascii_fold', flag(true))
		}
		for (index : indexes) {
			switch index.kind {
				case KEYWORD: {
					if (!textual) throw Unsupported.of('a keyword index on ' + type + ' field ' + name)
					if (searchable) throw Unsupported.of('a keyword index on ' + name + ', which is also full-text searchable')
					args.put('tokenization', text(if (index.lowercase) 'lowercase' else 'field'))
					args.put('index_filterable', flag(true))
				}
				case INTEGER,
				case FLOAT,
				case DATETIME: {
					if (!#['int', 'number', 'date'].contains(type)) throw Unsupported.of('a range index on ' + type + ' field ' + name)
					args.put('index_range_filters', flag(true))
				}
				case BOOL,
				case UUID: args.put('index_filterable', flag(true))
				case FULL_TEXT: {}
				case GEO: if (type != 'geoCoordinates') throw Unsupported.of('a geo index on ' + type + ' field ' + name)
				default: throw Unsupported.of(index.kind.literal + ' payload indexes')
			}
		}
		val subFields = field.subFields.map[
			if (!(it instanceof PayloadField)) throw Unsupported.of('vector fields inside ' + name)
			property(c, it as PayloadField, true)
		]
		if (!subFields.empty) args.put('nested_properties', items(subFields))
		if (field.description !== null) args.put('description', text(field.description))
		note(field, 'nested', 'documentBody')
		args
	}

	def private String dataType(VectorCollection c, PayloadField field) {
		val name = ModelUtil.fieldName(field)
		if (field === c.primaryKey && !#[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.VARCHAR, EScalarDType.TEXT,
			EScalarDType.UUID].contains(field.type)) {
			throw Unsupported.of('primary key of type ' + field.type.literal)
		}
		switch field.type {
			case JSON,
			case MAP,
			case STRUCT: objectType(field, name, 'object')
			case ARRAY: {
				if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
				switch field.elementType {
					case JSON,
					case MAP,
					case STRUCT: objectType(field, name, 'object[]')
					case BYTES,
					case GEO_POINT,
					case ARRAY: throw Unsupported.of('arrays of ' + field.elementType.literal)
					default: scalarType(field.elementType, name) + '[]'
				}
			}
			default: {
				if (!field.subFields.empty) throw Unsupported.of('sub-fields on ' + field.type.literal + ' fields')
				scalarType(field.type, name)
			}
		}
	}

	def private String objectType(PayloadField field, String name, String type) {
		if (field.subFields.empty) {
			throw Unsupported.of('json field ' + name + ' without sub-fields; Weaviate object properties declare their nested properties')
		}
		type
	}

	def private String scalarType(EScalarDType type, String name) {
		switch type {
			case INT,
			case BIGINT,
			case INT8: 'int'
			case FLOAT,
			case DOUBLE: 'number'
			case BOOL: 'boolean'
			case VARCHAR,
			case TEXT: 'text'
			case UUID: 'uuid'
			case DATE,
			case DATETIME,
			case TIMESTAMPTZ: 'date'
			case BYTES: 'blob'
			case GEO_POINT: 'geoCoordinates'
			default: throw Unsupported.of(type.literal + ' fields')
		}
	}

	def private Entries vector(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		if (field.multiVector) throw Unsupported.of('multi-vector fields')
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) {
			throw Unsupported.of('sparse vector field ' + name + ' without a bm25 function')
		}
		if (field.type != EVectorDType.FLOAT_VECTOR) throw Unsupported.of(field.type.literal + ' fields')
		val embedding = embedding(c, field)
		note(field, 'normalized', 'onDisk', 'nullable')
		val index = ModelUtil.vectorIndex(c, field)
		val chosen = metric(field, index)
		val args = kw('name', text(name), 'distance', text(distance(chosen)))
		if (embedding !== null) args.put('vectorizer', embedding)
		if (index !== null) vectorIndex(index, args)
		if (index !== null && index.family == EIndexFamily.FLAT && chosen != EMetric.COSINE) {
			throw Unsupported.of('flat indexes with a metric other than cosine; io.weaviate:client6 6.3.1 sets no flat-index distance')
		}
		args
	}

	/** Vectorizer module of a vector field: its own model, a text-embedding function, or the collection's model. */
	def Entries embedding(VectorCollection c, VectorField field) {
		val function = ModelUtil.producingFunction(c, field)
		val model = function?.model ?: field.model ?: c.embeddingModel
		if (model === null) {
			if (function !== null) throw Unsupported.of('text-embedding function ' + function.name + ' without a model')
			return null
		}
		val result = vectorizerModule(model)
		val sources = if (function !== null) function.inputs else field.sourceFields
		if (!sources.empty) {
			for (source : sources) {
				if (!(source instanceof PayloadField)) throw Unsupported.of('vectorizing vector field ' + ModelUtil.fieldName(source))
			}
			result.put('source_properties', texts(sources.map[input | propertyName(c, input)]))
		}
		result
	}

	def Entries quantizer(Quantization quantization) {
		if (written(quantization, 'oversampling')) throw Unsupported.of('oversampling; Weaviate rescores with a fixed candidate limit, not a factor')
		if (written(quantization, 'truncateDimension')) throw Unsupported.of('truncate-dim; Weaviate quantizes the full vector')
		if (!extValues(quantization).empty) throw Unsupported.of('extension parameters on quantization')
		note(quantization, 'rescore', 'keepOriginals')
		val bits = if (written(quantization, 'bits')) quantization.bits else 0.0
		switch quantization.type {
			case NONE: kw('type', text('none'))
			case SCALAR: {
				if (bits != 0 && bits != 8) throw Unsupported.of('scalar quantization with ' + Syntax.number(bits) + ' bits; Weaviate SQ uses 8 bits')
				kw('type', text('sq'))
			}
			case BINARY: {
				if (bits != 0 && bits != 1) throw Unsupported.of('binary quantization with ' + Syntax.number(bits) + ' bits; Weaviate BQ uses 1 bit')
				kw('type', text('bq'))
			}
			case PRODUCT: {
				if (written(quantization, 'bits')) throw Unsupported.of('product quantization bits; Weaviate PQ uses 256 centroids per segment')
				val result = kw('type', text('pq'))
				if (written(quantization, 'subvectors')) result.put('segments', num(quantization.subvectors))
				result
			}
		}
	}

	def private void vectorIndex(VectorIndex index, Entries args) {
		if (index.quantization !== null) args.put('quantizer', quantizer(index.quantization))
		if (written(index, 'accelerator')) throw Unsupported.of(index.accelerator.literal + ' accelerated indexes')
		if (written(index, 'centroids')) throw Unsupported.of('centroids; Weaviate has no IVF index')
		note(index, 'onDisk', 'online')
		switch index.family {
			case AUTO: {}
			case HNSW: args.put('index', text('hnsw'))
			case FLAT: args.put('index', text('flat'))
			default: throw Unsupported.of(index.family.literal + ' indexes')
		}
		val graph = index.family != EIndexFamily.FLAT
		val tuned = written(index, 'maxDegree') || written(index, 'buildCandidates') || written(index, 'searchCandidates')
		if (!graph && tuned) throw Unsupported.of('graph parameters on a flat index')
		if (written(index, 'maxDegree')) args.put('max_connections', num(index.maxDegree))
		if (written(index, 'buildCandidates')) args.put('ef_construction', num(index.buildCandidates))
		if (written(index, 'searchCandidates')) args.put('ef', num(index.searchCandidates))
	}

	def EMetric metric(VectorField field, VectorIndex index) {
		if (written(field, 'metric')) field.metric
		else if (index !== null && written(index, 'metric')) index.metric
		else EMetric.COSINE
	}

	def String distance(EMetric metric) {
		switch metric {
			case COSINE: 'cosine'
			case DOT: 'dot'
			case EUCLIDEAN: 'l2-squared'
			case MANHATTAN: 'manhattan'
			case HAMMING: 'hamming'
			default: throw Unsupported.of(metric.literal + ' metric')
		}
	}

	/** Text fields that feed a bm25 function; BM25 is part of their inverted index, so the function emits nothing. */
	def List<Field> bm25Inputs(VectorCollection c) {
		val List<Field> inputs = newArrayList
		for (function : c.functions) {
			switch function.type {
				case BM25: {
					for (input : function.inputs) {
						if (!(input instanceof PayloadField) || !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains((input as PayloadField).type)) {
							throw Unsupported.of('bm25 input ' + ModelUtil.fieldName(input) + ', which is not a text field')
						}
						inputs += input
					}
				}
				case TEXT_EMBEDDING: {}
				case SPARSE_ENCODING: throw Unsupported.of('sparse-encoding functions; Weaviate has no sparse vectors')
				case RERANK: throw Unsupported.of("rerank functions; Weaviate reranks at query time with the collection's reranker module")
				default: throw Unsupported.of(function.type.literal + ' functions; Weaviate runs no custom server-side functions')
			}
		}
		inputs
	}

	def private String tokenization(Analyzer analyzer) {
		val filters = analyzer.filters.map[type].toSet
		for (filter : filters) {
			if (!#[EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STOP, EAnalyzerFilterType.ASCII_FOLDING].contains(filter)) {
				throw Unsupported.of(filter.literal + ' token filters')
			}
		}
		if (filters.contains(EAnalyzerFilterType.STOP) && preset(analyzer) === null) {
			throw Unsupported.of('a stop filter without stopwords or language "en"')
		}
		switch analyzer.tokenizer {
			case STANDARD: 'word'
			case WHITESPACE: if (filters.contains(EAnalyzerFilterType.LOWERCASE)) 'lowercase' else 'whitespace'
			case KEYWORD: {
				if (filters.contains(EAnalyzerFilterType.LOWERCASE)) throw Unsupported.of('a lowercase filter on the keyword tokenizer')
				'field'
			}
			case LANGUAGE: switch language(analyzer) {
				case 'en': 'word'
				case 'ja': 'kagome_ja'
				case 'ko': 'kagome_kr'
				default: throw Unsupported.of('language analyzer ' + analyzer.language)
			}
			default: throw Unsupported.of(analyzer.tokenizer.literal + ' tokenizer')
		}
	}

	def private String language(Analyzer analyzer) {
		switch (analyzer.language ?: 'en').toLowerCase {
			case 'en',
			case 'english': 'en'
			case 'ja',
			case 'japanese': 'ja'
			case 'ko',
			case 'korean': 'ko'
			default: analyzer.language
		}
	}

	def private String preset(Analyzer analyzer) {
		if (analyzer.stopwords !== null) {
			val preset = analyzer.stopwords.toLowerCase
			if (preset != 'en' && preset != 'none') {
				throw Unsupported.of('stopword list "' + analyzer.stopwords + '"; Weaviate stopword presets are en and none')
			}
			return preset
		}
		if (analyzer.language === null) return null
		if (language(analyzer) == 'en') return 'en'
		if (analyzer.tokenizer != ETokenizer.LANGUAGE) {
			throw Unsupported.of('language "' + analyzer.language + '" on the ' + analyzer.tokenizer.literal + ' tokenizer; Weaviate has stopword presets only for en')
		}
		null
	}

	def private Entries invertedIndex(VectorCollection c) {
		val result = entries
		for (analyzer : c.analyzers) {
			if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources')
			val stopwords = preset(analyzer)
			if (stopwords !== null) set(result, 'stopwords_preset', text(stopwords))
			if (written(analyzer, 'bm25K1')) set(result, 'bm25_k1', num(analyzer.bm25K1))
			if (written(analyzer, 'bm25B')) set(result, 'bm25_b', num(analyzer.bm25B))
		}
		for (settings : c.extensions.filter[vendor == WeaviateGenerator.VENDOR].flatMap[items].filter(InvertedIndexSettings)) {
			if (settings.indexTimestamps) set(result, 'index_timestamps', flag(true))
			if (settings.indexNullState) set(result, 'index_null_state', flag(true))
			if (settings.indexPropertyLength) set(result, 'index_property_length', flag(true))
			if (written(settings, 'bm25K1')) set(result, 'bm25_k1', num(settings.bm25K1))
			if (written(settings, 'bm25B')) set(result, 'bm25_b', num(settings.bm25B))
			if (settings.stopwords !== null) set(result, 'stopwords_preset', text(settings.stopwords))
		}
		result
	}

	def private void set(Entries settings, String key, Expr value) {
		if (settings.has(key) && settings.get(key) != value) throw Unsupported.of('conflicting ' + key + ' settings')
		settings.put(key, value)
	}

	def private Entries limits(VectorCollection c) {
		val result = entries
		for (field : ModelUtil.payloadFields(c)) {
			val limit = kw()
			if (written(field, 'maxLength')) limit.put('max_length', num(field.maxLength))
			val capacity = field.limits.findFirst[type == EFieldLimit.ARRAY_LEN]
			if (capacity !== null) limit.put('max_items', num(capacity.value))
			if (!limit.entries.empty) result.put(ModelUtil.fieldName(field), limit)
		}
		for (field : storedVectors(c).filter[dimension > 0]) {
			result.put(ModelUtil.fieldName(field), kw('dim', num(field.dimension)))
		}
		result
	}

	def String consistency(EConsistencyLevel level) {
		switch level {
			case STRONG: 'ALL'
			case BOUNDED: 'QUORUM'
			case SESSION: 'QUORUM'
			case EVENTUAL: 'ONE'
		}
	}

	def Map<String, Expr> extValues(EObject host) {
		val values = new LinkedHashMap<String, Expr>
		if (host !== null) {
			for (entry : ModelUtil.extParams(host, WeaviateGenerator.VENDOR).entrySet) {
				values.put(entry.key, Literals.of(entry.value))
			}
		}
		values
	}

	def String extString(EObject host, String key) {
		if (host === null) return null
		val value = ModelUtil.extParams(host, WeaviateGenerator.VENDOR).get(key)
		if (value instanceof StringLit) value.value else null
	}

	def void note(EObject host, String... features) {
		for (feature : features) {
			if (written(host, feature)) notes += feature + ' on ' + host.eClass.name
		}
	}

	def static Entries kw(Object... pairs) {
		val result = entries
		for (var i = 0; i < pairs.length; i += 2) {
			result.put(pairs.get(i) as String, pairs.get(i + 1) as Expr)
		}
		result
	}
}
