package org.ftn.vecDSL.generator.opensearch

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
import org.ftn.vecDSL.generator.opensearch.OpenSearchIndex.Vector
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

import static org.ftn.vecDSL.generator.opensearch.OpenSearchBodies.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to OpenSearch indices (server 3.9 with the k-NN plugin): one index per collection with an explicit
 * mapping (dynamic strict unless the collection is dynamic), one shard and no replica unless the collection declares sharding or
 * replication, index.knn on and derived source off (so _source keeps the vectors as written), analyzers and BM25 parameters in
 * the index settings, and declared tenants, namespaces, partitions and aliases as aliases (filtered for the slices). Payload
 * fields are keyword, numeric, date, boolean, object, nested and geo fields; full-text fields are text fields with a keyword
 * sub-field for exact filters; dense vector fields are knn_vector fields (hnsw of the Faiss engine), sparse vector fields are
 * rank_features fields.
 */
class OpenSearchSchema {

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
	val Map<VectorCollection, OpenSearchIndex> evolved = newHashMap

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
	def OpenSearchIndex index(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, OpenSearchIndex next) {
		if (next === null) evolved.remove(c) else evolved.put(c, next)
	}

	def private OpenSearchIndex declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys documents (_id) by an integer, text or uuid field')
		}
		val List<OpenSearchIndex.Field> fields = newArrayList
		for (declaredField : c.fields) fields += field(c, declaredField)
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_FIELD else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_FIELD else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else PARTITION_FIELD
		new OpenSearchIndex(indexName(databaseOf(c), c.name), ModelUtil.fieldName(key), fields, tenant, namespace, partition, newHashMap, databaseOf(c))
	}

	/** The logical database of a collection declared in a logical-db, else null. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) null else database.name
	}

	def OpenSearchIndex.Field field(VectorCollection c, Field declaredField) {
		switch declaredField {
			VectorField: vectorField(c, declaredField)
			PayloadField: payloadField(c, declaredField)
		}
	}

	def private OpenSearchIndex.Field payloadField(VectorCollection c, PayloadField f) {
		val name = OpenSearchBodies.field(ModelUtil.fieldName(f))
		note(f, 'nullable', 'documentBody')
		if (!f.limits.empty) throw Unsupported.of('field limits (' + f.limits.head.type.literal + '); ' + OpenSearchStatements.PENDING + ' (an ingest pipeline with fail processors)')
		val defaultValue = if (f.defaultValue === null) null else Literals.of(f.defaultValue)
		if (!f.subFields.empty || f.nested || f.type == EScalarDType.STRUCT) {
			val Entries properties = Code.entries
			for (sub : f.subFields) {
				val inner = field(c, sub)
				properties.put(inner.name, inner.mapping)
			}
			val mapping = obj('type', text(if (f.nested) 'nested' else 'object'), 'properties', if (properties.entries.empty) null else properties,
				'dynamic', if (f.subFields.empty) text('true') else null)
			return new OpenSearchIndex.Field(name, mapping, false, null, f.nested, defaultValue)
		}
		if (f.type == EScalarDType.JSON || f.type == EScalarDType.MAP) {
			if (f.fullText) throw Unsupported.of('full-text search on json field ' + name + '; a json field is an object whose sub-fields OpenSearch maps when they arrive')
			return new OpenSearchIndex.Field(name, obj('type', text('object'), 'dynamic', text('true')), false, null, false, defaultValue)
		}
		val scalarType = if (f.type == EScalarDType.ARRAY) {
			if (!written(f, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			if (f.elementType == EScalarDType.ARRAY || f.elementType == EScalarDType.JSON || f.elementType == EScalarDType.MAP || f.elementType == EScalarDType.STRUCT) {
				throw Unsupported.of('arrays of ' + f.elementType.literal + ' in field ' + name + '; ' + OpenSearchStatements.PENDING + ' (nested fields)')
			}
			f.elementType
		} else f.type
		val analyzer = f.analyzer
		if (f.fullText || analyzer !== null) {
			if (scalarType != EScalarDType.VARCHAR && scalarType != EScalarDType.TEXT) throw Unsupported.of('full-text search on ' + scalarType.literal + ' field ' + name)
			val mapping = obj('type', text('text'), 'analyzer', if (analyzer === null) null else text(aliasName(analyzer.name)),
				'similarity', if (analyzer !== null && bm25(analyzer)) text(aliasName(analyzer.name) + '_bm25') else null,
				'fields', obj(RAW, obj('type', text('keyword'), 'ignore_above', num(if (written(f, 'maxLength')) f.maxLength else 8191))))
			return new OpenSearchIndex.Field(name, mapping, true, null, false, defaultValue)
		}
		val mapping = scalar(scalarType, name, f)
		new OpenSearchIndex.Field(name, mapping, false, null, false, defaultValue)
	}

	def private Entries scalar(EScalarDType type, String name, PayloadField f) {
		val date = DATES.get(type)
		if (date !== null) return obj('type', text('date'), 'format', text(date))
		val mapped = NUMBERS.get(type)
		if (mapped === null) throw Unsupported.of(type.literal + ' field ' + name + '; OpenSearch stores media as URLs or binary (base64) values, not ' + type.literal + ' objects')
		if (mapped == 'keyword' && written(f, 'maxLength')) return obj('type', text('keyword'), 'ignore_above', num(f.maxLength))
		obj('type', text(mapped))
	}

	def private OpenSearchIndex.Field vectorField(VectorCollection c, VectorField f) {
		val name = OpenSearchBodies.field(ModelUtil.fieldName(f))
		if (f.multiVector) throw Unsupported.of('multi-vector field ' + name + '; a knn_vector field holds one vector per document (several vectors per document need nested fields, one vector each)')
		if (f.model !== null || ModelUtil.producingFunction(c, f) !== null) throw Unsupported.of('server-side embedding of ' + name + OpenSearchStatements.NO_MODELS)
		note(f, 'nullable')
		if (!f.limits.empty) throw Unsupported.of('field limits (' + f.limits.head.type.literal + '); ' + OpenSearchStatements.PENDING + ' (an ingest pipeline with fail processors)')
		val index = indexOf(c, f)
		if (f.type.literal == 'sparse_float_vector') {
			if (written(f, 'modifier') && f.modifier.literal == 'idf') throw Unsupported.of('the idf modifier on ' + name + '; the neural_sparse query scores a rank_features field by the dot product of the given weights')
			val sparseMetric = metric(f, index)
			if (sparseMetric != 'dot') throw Unsupported.of('the ' + sparseMetric + ' metric on sparse field ' + name + '; the neural_sparse query scores rank_features by the dot product of the weights')
			if (index !== null && !#['auto', 'sparse_inverted'].contains(index.family.literal)) throw Unsupported.of(index.family.literal + ' indexes on sparse field ' + name + '; rank_features fields are inverted indexes')
			return new OpenSearchIndex.Field(name, obj('type', text('rank_features')), false, new Vector('sparse', 0, 'dot', 'dot', true, null, false), false, null)
		}
		val element = switch f.type.literal {
			case 'float_vector': 'float'
			case 'float16_vector': 'half_float'
			case 'bfloat16_vector': 'bfloat16'
			case 'int8_vector': 'byte'
			case 'binary_vector': 'binary'
			case 'uint8_vector': throw Unsupported.of('uint8_vector fields; knn_vector byte vectors are signed 8-bit integers in [-128, 127]')
			default: throw Unsupported.of(f.type.literal + ' fields; knn_vector fields hold float, half_float, byte (signed int8) and binary elements (bfloat16 through the Faiss sq encoder)')
		}
		if (f.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		if (element == 'binary' && f.dimension % 8 != 0) throw Unsupported.of('binary vector field ' + name + ' with ' + f.dimension + ' dimensions; binary knn_vector dimensions are a multiple of 8')
		if (f.dimension > 16000) throw Unsupported.of('vector field ' + name + ' with ' + f.dimension + ' dimensions; knn_vector fields have at most 16,000')
		val chosenMetric = metric(f, index)
		val dataType = if (element == 'bfloat16') 'float' else element
		val spaceType = OpenSearchBodies.space(chosenMetric, dataType)
		val chosenMethod = knnMethod(f, index, element)
		val Entries mapping = obj('type', text('knn_vector'), 'dimension', num(f.dimension), 'data_type', text(dataType), 'space_type', text(spaceType),
			'mode', if (index !== null && index.onDisk && index.quantization === null) text('on_disk') else null, 'method', chosenMethod.get(0) as Entries)
		val flatIndex = index !== null && index.family.literal == 'flat'
		new OpenSearchIndex.Field(name, mapping, false, new Vector(dataType, f.dimension, spaceType, chosenMetric, false, chosenMethod.get(1) as Double, flatIndex), false, null)
	}

	/**
	 * The method of a knn_vector field and the oversample factor its searches rescore with: hnsw of the Faiss engine with m,
	 * ef_construction and ef_search from the declared index, and the encoder of a declared quantization (sq with 1, 2, 4 or 16
	 * bits, binary with 1 or 2 bits); 7-bit scalar quantization is the sq encoder of the Lucene engine. A flat index keeps the
	 * hnsw graph and searches exactly (knn_score). bfloat16 vectors are float values under the sq encoder of type bf16.
	 */
	def private List<Object> knnMethod(VectorField f, VectorIndex index, String element) {
		var engine = 'faiss'
		val Entries parameters = Code.entries
		var Entries encoder = if (element == 'bfloat16') obj('name', text('sq'), 'parameters', obj('type', text('bf16'), 'bits', num(16))) else null
		var Double oversample = null
		if (index !== null) {
			note(index, 'online')
			if (index.onDisk && index.quantization !== null) notes += 'onDisk on VectorIndex, whose declared quantization replaces the on_disk mode'
			if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; GPU index builds run in the remote vector index build service, a separate deployment that the remote_vector_index_build settings point to')
			switch index.family.literal {
				case 'auto',
				case 'hnsw',
				case 'flat': {}
				case 'ivf': throw Unsupported.of('ivf indexes; the Faiss ivf method needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare')
				case 'vamana': throw Unsupported.of('vamana indexes; DiskANN is the jvector engine of the opensearch-jvector plugin, which the image does not include')
				default: throw Unsupported.of(index.family.literal + ' indexes on dense field ' + ModelUtil.fieldName(f) + '; knn_vector fields are indexed with hnsw')
			}
			if (written(index, 'centroids')) throw Unsupported.of('centroids; Faiss partitions vectors into lists only in its ivf method, which needs a trained model')
			if (written(index, 'maxDegree')) parameters.put('m', num(index.maxDegree))
			if (written(index, 'buildCandidates')) parameters.put('ef_construction', num(index.buildCandidates))
			if (written(index, 'searchCandidates')) parameters.put('ef_search', num(index.searchCandidates))
			val q = index.quantization
			if (q !== null && q.type.literal != 'none') {
				note(q, 'keepOriginals', 'truncateDimension')
				if (element != 'float') throw Unsupported.of('quantization of ' + element + ' vectors; the Faiss and Lucene encoders quantize float vectors')
				encoder = switch q.type.literal {
					case 'scalar': {
						val bits = if (written(q, 'bits')) q.bits else 16.0
						if (bits == 7.0) {
							engine = 'lucene'
							obj('name', text('sq'), 'parameters', obj('bits', num(7)))
						} else if (bits == 16.0) {
							obj('name', text('sq'), 'parameters', obj('type', text('fp16'), 'bits', num(16)))
						} else if (bits == 1.0 || bits == 2.0 || bits == 4.0) {
							obj('name', text('sq'), 'parameters', obj('bits', num(bits)))
						} else {
							throw Unsupported.of('scalar quantization to ' + bits + ' bits; the Faiss sq encoder quantizes to 1, 2, 4 or 16 bits and the Lucene sq encoder to 1, 2, 4 or 7 bits')
						}
					}
					case 'binary': {
						val bits = if (written(q, 'bits')) q.bits else 1.0
						if (bits != 1.0 && bits != 2.0 && bits != 4.0) throw Unsupported.of('binary quantization with ' + bits + ' bits; the Faiss binary encoder keeps 1, 2 or 4 bits per dimension')
						obj('name', text('binary'), 'parameters', obj('bits', num(bits)))
					}
					default: throw Unsupported.of('product quantization; the Faiss pq encoder needs a model trained with the Train API on vectors indexed before the field exists, which vecDSL cannot declare')
				}
				if (q.rescore || written(q, 'oversampling')) oversample = if (written(q, 'oversampling')) q.oversampling else 1.0
			} else if (q !== null && (q.rescore || written(q, 'oversampling'))) {
				throw Unsupported.of('rescoring without quantization; the rescore of a knn query applies to quantized vectors')
			}
		}
		if (engine == 'lucene' && parameters.has('ef_search')) {
			parameters.entries.remove('ef_search')
			notes += 'searchCandidates on VectorIndex, which the Lucene engine replaces with k'
		}
		if (encoder !== null) parameters.put('encoder', encoder)
		val Entries hnsw = obj('name', text('hnsw'), 'engine', text(engine), 'parameters', if (parameters.entries.empty) null else parameters)
		val List<Object> result = newArrayList
		result += hnsw
		result += oversample
		result
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
			if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; ' + OpenSearchStatements.NO_FILES)
			val tokenizer = TOKENIZERS.get(analyzer.tokenizer.literal)
			if (tokenizer === null) throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; the icu tokenizer is in the analysis-icu plugin, which the image does not include')
			val language = if (analyzer.language === null) null else LANGUAGES.get(analyzer.language.toLowerCase)
			if (analyzer.language !== null && language === null) throw Unsupported.of('the language ' + analyzer.language + '; ' + OpenSearchStatements.LANGUAGES_REFERENCE)
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
					case 'length': throw Unsupported.of('the length filter; vecDSL gives no bounds, and without them the OpenSearch length filter (min 0, max Integer.MAX_VALUE) keeps every token')
					case 'synonym': throw Unsupported.of('the synonym filter; vecDSL declares synonyms in file resources, ' + OpenSearchStatements.NO_FILES)
					case 'decompounder': throw Unsupported.of('the decompounder filter; dictionary_decompounder needs a word list, ' + OpenSearchStatements.NO_FILES)
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
				if (!analyzer.stopwords.startsWith('_') || !analyzer.stopwords.endsWith('_')) throw Unsupported.of('stopwords ' + analyzer.stopwords + '; OpenSearch names built-in stop-word lists _english_, _german_ and so on')
				filterSettings.put(name + '_stopwords', obj('type', text('stop'), 'stopwords', text(analyzer.stopwords)))
				chain += text(name + '_stopwords')
			}
			analyzerSettings.put(name, obj('type', text('custom'), 'tokenizer', text(tokenizer), 'filter', arr(chain)))
			for (key : ModelUtil.extParams(analyzer, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
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
		if (c.external !== null) throw Unsupported.of(OpenSearchStatements.EXTERNAL)
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of(OpenSearchStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of('auto-id; ' + OpenSearchStatements.PENDING + ' (documents indexed without _id get generated ids)')
		if (c.deletionProtection) throw Unsupported.of(OpenSearchStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; OpenSearch has no references between indices (joins only within one index, through the join field)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + OpenSearchStatements.NO_MODELS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + OpenSearchStatements.functionReason(c.functions.head.type.literal))
		note(c, 'idType', 'generativeModel', 'consistency')
		if (fresh) evolve(c, null)
		val current = index(c)
		val Entries settings = obj('number_of_shards', num(1), 'number_of_replicas', num(0), 'index.knn', new Code.Flag(true),
			'index.knn.derived_source.enabled', new Code.Flag(false))
		OpenSearchStatements.storage(c.storage, settings)
		OpenSearchStatements.clusterSettings(c.cluster ?: EcoreUtil2.getContainerOfType(c, org.ftn.vecDSL.vecDSL.Cluster), settings)
		for (limit : c.limits) OpenSearchStatements.collectionLimit(limit, settings)
		for (key : ModelUtil.extParams(c, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
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
			OpenSearchStatements.checkPartition(partition)
			aliases.put(sliceAlias(current.name, 'partition', partition.name), obj('filter', term(current.partition, text(partition.name))))
		}
		val body = obj('settings', settings, 'mappings', mappings, 'aliases', if (aliases.entries.empty) null else aliases)
		val creation = new Code.Run(request('PUT', '/' + current.name, null, body))
		if (ifNotExists) newArrayList(new Code.When(call('exists', 'path', text('/' + current.name)), true, #[creation]) as Stmt) else newArrayList(creation as Stmt)
	}

	/** The properties of an index: its fields and the hidden keyword fields of tenants, namespaces and partitions. */
	def Entries properties(OpenSearchIndex current) {
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
	def void checkPayloadIndex(OpenSearchIndex current, PayloadIndex payloadIndex) {
		note(payloadIndex, 'onDisk', 'minTokenLength', 'maxTokenLength')
		for (key : ModelUtil.extParams(payloadIndex, OpenSearchGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (written(payloadIndex, 'structure') && payloadIndex.structure.literal == 'bitmap') throw Unsupported.of('bitmap payload indexes; OpenSearch indexes keywords in inverted indexes and numbers in BKD trees, with no bitmap index type')
		if (payloadIndex.lowercase) throw Unsupported.of('lowercase payload indexes; ' + OpenSearchStatements.PENDING + ' (a keyword field with a lowercase normalizer)')
		if (payloadIndex.prefixMatch) throw Unsupported.of('prefix-match payload indexes; ' + OpenSearchStatements.PENDING + ' (index_prefixes on a text field)')
		if (payloadIndex.analyzer !== null) throw Unsupported.of('an analyzer on a payload index; the analyzer of a text field is part of its mapping (declare it on the field)')
		val path = payloadIndex.target
		if (path.rawPath !== null) return;
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field; knn_vector fields are indexed through their method')
		val leaf = ModelUtil.leaf(path)
		val target = current.field(ModelUtil.fieldName(path.field))
		val kind = payloadIndex.kind.literal
		if (kind == 'full_text' && !(target.fullText || (leaf instanceof PayloadField && (leaf as PayloadField).fullText))) {
			throw Unsupported.of('a full_text index on ' + target.name + ', which is not a full-text field (OpenSearch fixes the type of a field when it is mapped)')
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
