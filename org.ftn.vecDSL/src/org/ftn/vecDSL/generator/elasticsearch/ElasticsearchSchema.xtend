package org.ftn.vecDSL.generator.elasticsearch

import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchIndex.Vector
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.elasticsearch.ElasticsearchBodies.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Elasticsearch indices (server 9.5): one index per collection with an explicit mapping (dynamic
 * strict unless the collection is dynamic), one shard and no replica unless the collection declares sharding or replication,
 * analyzers and BM25 parameters in the index settings, and declared tenants, namespaces, partitions and aliases as aliases
 * (filtered for the slices). Payload fields are keyword, numeric, date, boolean, object, nested and geo fields; full-text
 * fields are text fields with a keyword sub-field for exact filters; vector fields are dense_vector (or sparse_vector) fields.
 */
class ElasticsearchSchema {

	static val NUMBERS = #{EScalarDType.INT -> 'integer', EScalarDType.BIGINT -> 'long', EScalarDType.INT8 -> 'byte',
		EScalarDType.FLOAT -> 'float', EScalarDType.DOUBLE -> 'double', EScalarDType.BOOL -> 'boolean', EScalarDType.UUID -> 'keyword',
		EScalarDType.VARCHAR -> 'keyword', EScalarDType.TEXT -> 'keyword', EScalarDType.BYTES -> 'binary',
		EScalarDType.GEO_POINT -> 'geo_point', EScalarDType.GEOMETRY -> 'geo_shape'}
	static val DATES = #{EScalarDType.DATE -> 'strict_date', EScalarDType.DATETIME -> 'strict_date_optional_time',
		EScalarDType.TIMESTAMPTZ -> 'strict_date_optional_time'}
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val TOKENIZERS = #{'standard' -> 'standard', 'whitespace' -> 'whitespace', 'keyword' -> 'keyword', 'ngram' -> 'ngram',
		'edge_ngram' -> 'edge_ngram', 'language' -> 'standard'}
	static val LANGUAGES = #{'en' -> 'english', 'english' -> 'english', 'de' -> 'german', 'german' -> 'german', 'fr' -> 'french',
		'french' -> 'french', 'es' -> 'spanish', 'spanish' -> 'spanish', 'it' -> 'italian', 'italian' -> 'italian',
		'pt' -> 'portuguese', 'portuguese' -> 'portuguese', 'nl' -> 'dutch', 'dutch' -> 'dutch', 'ru' -> 'russian',
		'russian' -> 'russian', 'sv' -> 'swedish', 'swedish' -> 'swedish', 'da' -> 'danish', 'danish' -> 'danish',
		'fi' -> 'finnish', 'finnish' -> 'finnish', 'no' -> 'norwegian', 'norwegian' -> 'norwegian', 'hu' -> 'hungarian',
		'hungarian' -> 'hungarian', 'ro' -> 'romanian', 'romanian' -> 'romanian', 'tr' -> 'turkish', 'turkish' -> 'turkish',
		'sr' -> 'serbian', 'serbian' -> 'serbian'}

	val List<String> notes
	val Set<VectorCollection> tenanted = newHashSet
	val Set<VectorCollection> namespaced = newHashSet
	val Map<VectorCollection, ElasticsearchIndex> evolved = newHashMap

	new(List<String> notes, Script script) {
		this.notes = notes
		for (object : script.eAllContents.toIterable) slices(object)
	}

	/** Collections whose statements name a tenant or a namespace get the hidden keyword fields. */
	def private void slices(EObject object) {
		val collection = ModelUtil.targetCollection(object)
		if (collection === null) return;
		val tenantFeature = object.eClass.getEStructuralFeature('tenant')
		if (tenantFeature !== null && object.eGet(tenantFeature) !== null) tenanted += collection
		val namespaceFeature = object.eClass.getEStructuralFeature('namespace')
		if (namespaceFeature !== null && namespaceFeature.EType.name == 'EString' && object.eGet(namespaceFeature) !== null) namespaced += collection
	}

	// ---------------------------------------------------------------- indices

	/** The index of a collection as the statements so far leave it (alter statements change fields and the name). */
	def ElasticsearchIndex index(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, ElasticsearchIndex next) {
		if (next === null) evolved.remove(c) else evolved.put(c, next)
	}

	def private ElasticsearchIndex declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys documents (_id) by an integer, text or uuid field')
		}
		val List<ElasticsearchIndex.Field> fields = newArrayList
		for (declaredField : c.fields) fields += field(c, declaredField)
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_FIELD else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_FIELD else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else PARTITION_FIELD
		new ElasticsearchIndex(indexName(databaseOf(c), c.name), ModelUtil.fieldName(key), fields, tenant, namespace, partition, newHashMap, databaseOf(c))
	}

	/** The logical database of a collection declared in a logical-db, else null. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) null else database.name
	}

	def ElasticsearchIndex.Field field(VectorCollection c, Field declaredField) {
		switch declaredField {
			VectorField: vectorField(c, declaredField)
			PayloadField: payloadField(c, declaredField)
		}
	}

	def private ElasticsearchIndex.Field payloadField(VectorCollection c, PayloadField f) {
		val name = ElasticsearchBodies.field(ModelUtil.fieldName(f))
		note(f, 'nullable', 'documentBody')
		if (!f.limits.empty) throw Unsupported.of('field limits (' + f.limits.head.type.literal + '); ' + ElasticsearchStatements.PENDING + ' (an ingest pipeline with fail processors)')
		val defaultValue = if (f.defaultValue === null) null else Literals.of(f.defaultValue)
		if (!f.subFields.empty || f.nested || f.type == EScalarDType.STRUCT) {
			val Entries properties = Code.entries
			for (sub : f.subFields) {
				val inner = field(c, sub)
				properties.put(inner.name, inner.mapping)
			}
			val mapping = obj('type', text(if (f.nested) 'nested' else 'object'), 'properties', if (properties.entries.empty) null else properties,
				'dynamic', if (f.subFields.empty) text('true') else null)
			return new ElasticsearchIndex.Field(name, mapping, false, null, f.nested, defaultValue)
		}
		if (f.type == EScalarDType.JSON || f.type == EScalarDType.MAP) {
			if (f.fullText) throw Unsupported.of('full-text search on json field ' + name + '; a json field is an object whose sub-fields Elasticsearch maps when they arrive')
			return new ElasticsearchIndex.Field(name, obj('type', text('object'), 'dynamic', text('true')), false, null, false, defaultValue)
		}
		val scalarType = if (f.type == EScalarDType.ARRAY) {
			if (!written(f, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			if (f.elementType == EScalarDType.ARRAY || f.elementType == EScalarDType.JSON || f.elementType == EScalarDType.MAP || f.elementType == EScalarDType.STRUCT) {
				throw Unsupported.of('arrays of ' + f.elementType.literal + ' in field ' + name + '; ' + ElasticsearchStatements.PENDING + ' (nested fields)')
			}
			f.elementType
		} else f.type
		val analyzer = f.analyzer
		if (f.fullText || analyzer !== null) {
			if (scalarType != EScalarDType.VARCHAR && scalarType != EScalarDType.TEXT) throw Unsupported.of('full-text search on ' + scalarType.literal + ' field ' + name)
			val mapping = obj('type', text('text'), 'analyzer', if (analyzer === null) null else text(aliasName(analyzer.name)),
				'similarity', if (analyzer !== null && bm25(analyzer)) text(aliasName(analyzer.name) + '_bm25') else null,
				'fields', obj(RAW, obj('type', text('keyword'), 'ignore_above', num(if (written(f, 'maxLength')) f.maxLength else 8191))))
			return new ElasticsearchIndex.Field(name, mapping, true, null, false, defaultValue)
		}
		val mapping = scalar(scalarType, name, f)
		new ElasticsearchIndex.Field(name, mapping, false, null, false, defaultValue)
	}

	def private Entries scalar(EScalarDType type, String name, PayloadField f) {
		val date = DATES.get(type)
		if (date !== null) return obj('type', text('date'), 'format', text(date))
		val mapped = NUMBERS.get(type)
		if (mapped === null) throw Unsupported.of(type.literal + ' field ' + name + '; Elasticsearch stores media as URLs or binary (base64) values, not ' + type.literal + ' objects')
		if (mapped == 'keyword' && written(f, 'maxLength')) return obj('type', text('keyword'), 'ignore_above', num(f.maxLength))
		obj('type', text(mapped))
	}

	def private ElasticsearchIndex.Field vectorField(VectorCollection c, VectorField f) {
		val name = ElasticsearchBodies.field(ModelUtil.fieldName(f))
		if (f.multiVector) throw Unsupported.of('multi-vector field ' + name + '; Elasticsearch stores several vectors per document in rank_vectors fields, which need an enterprise license (RankVectorsPlugin)')
		if (f.model !== null || ModelUtil.producingFunction(c, f) !== null) throw Unsupported.of('server-side embedding of ' + name + ElasticsearchStatements.NO_MODELS)
		note(f, 'onDisk', 'nullable')
		if (!f.limits.empty) throw Unsupported.of('field limits (' + f.limits.head.type.literal + '); ' + ElasticsearchStatements.PENDING + ' (an ingest pipeline with fail processors)')
		val index = indexOf(c, f)
		if (f.type.literal == 'sparse_float_vector') {
			if (written(f, 'modifier') && f.modifier.literal == 'idf') throw Unsupported.of('the idf modifier on ' + name + '; sparse_vector fields score with the weights as given')
			val sparseMetric = metric(f, index)
			if (sparseMetric != 'dot') throw Unsupported.of('the ' + sparseMetric + ' metric on sparse field ' + name + '; sparse_vector fields score by the dot product of the weights')
			if (index !== null && !#['auto', 'sparse_inverted'].contains(index.family.literal)) throw Unsupported.of(index.family.literal + ' indexes on sparse field ' + name + '; sparse_vector fields are inverted indexes')
			return new ElasticsearchIndex.Field(name, obj('type', text('sparse_vector'), 'index_options', obj('prune', new Code.Flag(false))), false,
				new Vector('sparse', 0, 'dot_product', 'dot', true), false, null)
		}
		val element = switch f.type.literal {
			case 'float_vector': 'float'
			case 'bfloat16_vector': 'bfloat16'
			case 'int8_vector': 'byte'
			case 'binary_vector': 'bit'
			default: throw Unsupported.of(f.type.literal + ' fields; Elasticsearch dense_vector fields hold float, bfloat16, byte (signed int8) and bit elements')
		}
		if (f.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		if (element == 'bit' && f.dimension % 8 != 0) throw Unsupported.of('bit vector field ' + name + ' with ' + f.dimension + ' dimensions; bit vectors have a multiple of 8')
		val limit = if (element == 'bit') 32768 else 4096
		if (f.dimension > limit) throw Unsupported.of('vector field ' + name + ' with ' + f.dimension + ' dimensions; dense_vector fields have at most ' + limit)
		val chosenMetric = metric(f, index)
		val sim = ElasticsearchBodies.similarity(chosenMetric, element, f.normalized)
		val mapping = obj('type', text('dense_vector'), 'element_type', text(element), 'dims', num(f.dimension), 'index', new Code.Flag(true),
			'similarity', text(sim), 'index_options', indexOptions(f, index, element))
		new ElasticsearchIndex.Field(name, mapping, false, new Vector(element, f.dimension, sim, chosenMetric, false), false, null)
	}

	/**
	 * The index_options of a dense_vector field: hnsw (raw vectors) without a declared index, so that scores are exact; hnsw or
	 * flat with int8, int4 or bbq quantization from the declared index. The server default for new float fields (int8_hnsw,
	 * bbq_hnsw from 384 dimensions) is always overridden, since its scores are quantized approximations.
	 */
	def private Entries indexOptions(VectorField f, VectorIndex index, String element) {
		if (index === null) return obj('type', text('hnsw'))
		note(index, 'onDisk', 'online')
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; GPU indexing needs an enterprise license (GPUPlugin, GPU_INDEXING_FEATURE)')
		val family = switch index.family.literal {
			case 'auto',
			case 'hnsw': 'hnsw'
			case 'flat': 'flat'
			case 'ivf': throw Unsupported.of('ivf indexes; the IVF index of Elasticsearch (bbq_disk) needs an enterprise license (DiskBBQPlugin, DISK_BBQ_FEATURE)')
			default: throw Unsupported.of(index.family.literal + ' indexes; Elasticsearch builds hnsw and flat indexes for dense_vector fields')
		}
		if (family == 'flat' && (written(index, 'maxDegree') || written(index, 'buildCandidates'))) throw Unsupported.of('max-degree and build-candidates on a flat index')
		if (written(index, 'centroids')) throw Unsupported.of('centroids; Elasticsearch partitions vectors into clusters only in its IVF index (bbq_disk), which needs an enterprise license')
		var prefix = ''
		var Code.Expr rescore = null
		val q = index.quantization
		if (q !== null) {
			note(q, 'keepOriginals', 'truncateDimension')
			if (element != 'float' && q.type.literal != 'none') throw Unsupported.of('quantization of ' + element + ' vectors; Elasticsearch quantizes float vectors')
			prefix = switch q.type.literal {
				case 'none': ''
				case 'scalar': {
					val bits = if (written(q, 'bits')) q.bits else 8.0
					if (bits == 8.0) 'int8_' else if (bits == 4.0) 'int4_' else throw Unsupported.of('scalar quantization to ' + bits + ' bits; Elasticsearch quantizes to int8 and int4')
				}
				case 'binary': {
					if (written(q, 'bits') && q.bits != 1.0) throw Unsupported.of('binary quantization with ' + q.bits + ' bits; bbq keeps one bit per dimension')
					if (f.dimension < 64) throw Unsupported.of('binary quantization of ' + f.dimension + ' dimensions; bbq indexes need at least 64 (DenseVectorFieldMapper.BBQ_MIN_DIMS)')
					'bbq_'
				}
				default: throw Unsupported.of('product quantization; Elasticsearch quantizes with int8, int4 and bbq')
			}
			if (q.rescore || written(q, 'oversampling')) {
				if (prefix == '') throw Unsupported.of('rescoring without quantization; rescore_vector applies to quantized index types')
				rescore = obj('oversample', num(if (written(q, 'oversampling')) q.oversampling else 3.0))
			}
		}
		obj('type', text(prefix + family), 'm', if (written(index, 'maxDegree')) num(index.maxDegree) else null,
			'ef_construction', if (written(index, 'buildCandidates')) num(index.buildCandidates) else null, 'rescore_vector', rescore)
	}

	def VectorIndex indexOf(VectorCollection c, VectorField f) {
		ModelUtil.vectorIndex(c, f)
	}

	/** The metric of a vector field: the field's, else its index's, else cosine. */
	def String metric(VectorField f, VectorIndex index) {
		val chosen = if (written(f, 'metric')) f.metric else if (index !== null && written(index, 'metric')) index.metric else if (f.type.literal == 'sparse_float_vector') EMetric.DOT else EMetric.COSINE
		chosen.literal
	}

	// ---------------------------------------------------------------- analysis

	def static boolean bm25(Analyzer analyzer) {
		written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')
	}

	/** The custom analyzer of an analyzer declaration with its filters, as index settings. */
	def Entries analysis(List<Analyzer> analyzers) {
		val Entries analyzerSettings = Code.entries
		val Entries filterSettings = Code.entries
		for (analyzer : analyzers) {
			if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; ' + ElasticsearchStatements.NO_FILES)
			val tokenizer = TOKENIZERS.get(analyzer.tokenizer.literal)
			if (tokenizer === null) throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; the icu tokenizer is in the analysis-icu plugin, which the image does not include')
			val language = if (analyzer.language === null) null else LANGUAGES.get(analyzer.language.toLowerCase)
			if (analyzer.language !== null && language === null) throw Unsupported.of('the language ' + analyzer.language + '; ' + ElasticsearchStatements.LANGUAGES_REFERENCE)
			val name = aliasName(analyzer.name)
			val List<Expr> chain = newArrayList
			chain += text('lowercase')
			for (analyzerFilter : analyzer.filters) {
				val step = switch analyzerFilter.type.literal {
					case 'lowercase': null
					case 'stop': {
						if (language === null) throw Unsupported.of('the stop filter without a language; the stop filter takes the stop words of a language')
						val filterName = name + '_stop'
						filterSettings.put(filterName, obj('type', text('stop'), 'stopwords', text('_' + language + '_')))
						filterName
					}
					case 'stemmer': {
						if (language === null) throw Unsupported.of('the stemmer filter without a language')
						val filterName = name + '_stemmer'
						filterSettings.put(filterName, obj('type', text('stemmer'), 'language', text(language)))
						filterName
					}
					case 'ascii_folding': 'asciifolding'
					case 'length': throw Unsupported.of('the length filter; vecDSL gives no bounds, and without them the Elasticsearch length filter (min 0, max Integer.MAX_VALUE) keeps every token')
					case 'synonym': throw Unsupported.of('the synonym filter; vecDSL declares synonyms in file resources, ' + ElasticsearchStatements.NO_FILES)
					case 'decompounder': throw Unsupported.of('the decompounder filter; dictionary_decompounder needs a word list, ' + ElasticsearchStatements.NO_FILES)
					default: throw Unsupported.of('the phonetic filter; it is in the analysis-phonetic plugin, which the image does not include')
				}
				if (step !== null) chain += text(step)
			}
			if (language !== null && analyzer.filters.empty) {
				filterSettings.put(name + '_stop', obj('type', text('stop'), 'stopwords', text('_' + language + '_')))
				filterSettings.put(name + '_stemmer', obj('type', text('stemmer'), 'language', text(language)))
				chain += text(name + '_stop')
				chain += text(name + '_stemmer')
			}
			if (written(analyzer, 'stopwords') && analyzer.stopwords != 'none') {
				if (!analyzer.stopwords.startsWith('_') || !analyzer.stopwords.endsWith('_')) throw Unsupported.of('stopwords ' + analyzer.stopwords + '; Elasticsearch names built-in stop-word lists _english_, _german_ and so on')
				filterSettings.put(name + '_stopwords', obj('type', text('stop'), 'stopwords', text(analyzer.stopwords)))
				chain += text(name + '_stopwords')
			}
			analyzerSettings.put(name, obj('type', text('custom'), 'tokenizer', text(tokenizer), 'filter', arr(chain)))
			for (key : ModelUtil.extParams(analyzer, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		}
		obj('analyzer', if (analyzerSettings.entries.empty) null else analyzerSettings, 'filter', if (filterSettings.entries.empty) null else filterSettings)
	}

	/** BM25 similarities of analyzers with k1 or b, named after the analyzer. */
	def Entries similarities(List<Analyzer> analyzers) {
		val Entries result = Code.entries
		for (analyzer : analyzers.filter[a | bm25(a)]) {
			result.put(aliasName(analyzer.name) + '_bm25', obj('type', text('BM25'), 'k1', if (written(analyzer, 'bm25K1')) num(analyzer.bm25K1) else null,
				'b', if (written(analyzer, 'bm25B')) num(analyzer.bm25B) else null))
		}
		result
	}

	// ---------------------------------------------------------------- index creation

	/**
	 * The create-index request of a collection: settings (shards, replicas, analysis, similarities, field limits), the mapping
	 * and the aliases. With if-not-exists the request runs only when the index is missing (HEAD /index).
	 */
	def List<Stmt> createIndex(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(ElasticsearchStatements.EXTERNAL)
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of(ElasticsearchStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of('auto-id; ' + ElasticsearchStatements.PENDING + ' (documents indexed without _id get generated ids)')
		if (c.deletionProtection) throw Unsupported.of(ElasticsearchStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; Elasticsearch has no references between indices (joins only within one index, through the join field)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + ElasticsearchStatements.NO_MODELS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + ElasticsearchStatements.functionReason(c.functions.head.type.literal))
		note(c, 'idType', 'generativeModel', 'consistency')
		if (fresh) evolve(c, null)
		val current = index(c)
		val Entries settings = obj('number_of_shards', num(1), 'number_of_replicas', num(0))
		ElasticsearchStatements.storage(c.storage, settings)
		ElasticsearchStatements.clusterSettings(c.cluster ?: EcoreUtil2.getContainerOfType(c, org.ftn.vecDSL.vecDSL.Cluster), settings)
		for (limit : c.limits) ElasticsearchStatements.collectionLimit(limit, settings)
		for (key : ModelUtil.extParams(c, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val analysisSettings = analysis(c.analyzers)
		if (!analysisSettings.entries.empty) settings.put('analysis', analysisSettings)
		val similaritySettings = similarities(c.analyzers)
		if (!similaritySettings.entries.empty) settings.put('similarity', similaritySettings)
		val Entries meta = obj('description', if (c.description === null) null else text(c.description),
			'tags', if (c.tags.empty) null else arr(c.tags.map[t | text(t) as Expr].toList))
		val mappings = obj('dynamic', text(if (c.dynamic) 'true' else 'strict'), '_meta', if (meta.entries.empty) null else meta, 'properties', properties(current))
		if (!c.storedSourceFields.empty) mappings.put('_source', obj('includes', arr(c.storedSourceFields.map[s | text(ModelUtil.fieldName(s)) as Expr].toList)))
		for (payloadIndex : c.indexes.filter(PayloadIndex)) checkPayloadIndex(current, payloadIndex)
		val Entries aliases = Code.entries
		for (alias : c.aliases) aliases.put(aliasName(alias.name), Code.entries)
		for (tenant : c.tenants) aliases.put(sliceAlias(current.name, 'tenant', tenant.name), obj('filter', term(current.tenant, text(tenant.name))))
		for (namespace : c.namespaces) aliases.put(sliceAlias(current.name, 'namespace', namespace.key), obj('filter', term(current.namespace, text(namespace.key))))
		for (partition : ModelUtil.partitions(c)) {
			ElasticsearchStatements.checkPartition(partition)
			aliases.put(sliceAlias(current.name, 'partition', partition.name), obj('filter', term(current.partition, text(partition.name))))
		}
		val body = obj('settings', settings, 'mappings', mappings, 'aliases', if (aliases.entries.empty) null else aliases)
		val creation = new Code.Run(request('PUT', '/' + current.name, null, body))
		if (ifNotExists) newArrayList(new Code.When(call('exists', 'path', text('/' + current.name)), true, #[creation]) as Stmt) else newArrayList(creation as Stmt)
	}

	/** The properties of an index: its fields and the hidden keyword fields of tenants, namespaces and partitions. */
	def Entries properties(ElasticsearchIndex current) {
		val Entries result = Code.entries
		for (f : current.fields) result.put(f.name, f.mapping)
		for (hidden : #[current.tenant, current.namespace, current.partition].filterNull) {
			if (!result.has(hidden)) result.put(hidden, obj('type', text('keyword')))
		}
		result
	}

	/**
	 * A declared payload index: every mapped field is indexed already (index: true), so the declaration is checked against the
	 * field's mapping; lowercase and prefix-match need mapping parameters that only a new field can take.
	 */
	def void checkPayloadIndex(ElasticsearchIndex current, PayloadIndex payloadIndex) {
		note(payloadIndex, 'onDisk', 'minTokenLength', 'maxTokenLength')
		for (key : ModelUtil.extParams(payloadIndex, ElasticsearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (written(payloadIndex, 'structure') && payloadIndex.structure.literal == 'bitmap') throw Unsupported.of('bitmap payload indexes; Elasticsearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type')
		if (payloadIndex.lowercase) throw Unsupported.of('lowercase payload indexes; ' + ElasticsearchStatements.PENDING + ' (a keyword field with a lowercase normalizer)')
		if (payloadIndex.prefixMatch) throw Unsupported.of('prefix-match payload indexes; ' + ElasticsearchStatements.PENDING + ' (index_prefixes on a text field)')
		if (payloadIndex.analyzer !== null) throw Unsupported.of('an analyzer on a payload index; the analyzer of a text field is part of its mapping (declare it on the field)')
		val path = payloadIndex.target
		if (path.rawPath !== null) return;
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field; dense_vector fields are indexed through their index_options')
		val leaf = ModelUtil.leaf(path)
		val target = current.field(ModelUtil.fieldName(path.field))
		val kind = payloadIndex.kind.literal
		if (kind == 'full_text' && !(target.fullText || (leaf instanceof PayloadField && (leaf as PayloadField).fullText))) {
			throw Unsupported.of('a full_text index on ' + target.name + ', which is not a full-text field (Elasticsearch fixes the type of a field when it is mapped)')
		}
		if (kind == 'geo' && leaf instanceof PayloadField && (leaf as PayloadField).type != EScalarDType.GEO_POINT && (leaf as PayloadField).type != EScalarDType.GEOMETRY) {
			throw Unsupported.of('a geo index on a field that is not geo_point or geometry')
		}
	}

	def static Entries term(String fieldName, Expr value) {
		obj('term', obj(fieldName, value))
	}

	def void note(EObject host, String... features) {
		for (feature : features) {
			if (written(host, feature)) notes += feature + ' on ' + host.eClass.name
		}
	}
}
