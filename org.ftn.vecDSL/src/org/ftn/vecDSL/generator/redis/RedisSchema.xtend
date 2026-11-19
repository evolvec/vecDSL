package org.ftn.vecDSL.generator.redis

import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.redis.RedisIndex.Attr
import org.ftn.vecDSL.generator.redis.RedisIndex.Index
import org.ftn.vecDSL.generator.redis.RedisIndex.Vec
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EIndexFamily
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * The index of each collection: every declared field is an attribute of one FT.CREATE ... ON JSON index over the key prefix
 * <collection>: (or <database>:<collection>:), so filters, keyword and vector search all read the same documents. Statements see
 * the index as the alter statements before them leave it (added, dropped and renamed fields, a new name).
 */
class RedisSchema {

	public static val NO_MODELS = '; Redis Open Source runs no embedding models (the Query Engine indexes the vectors a client writes), so generated programs supply their own vectors'
	public static val ON_DISK = 'on-disk; Redis Open Source keeps documents and indexes in memory (Redis Flex, formerly Auto Tiering, is a Redis Software and Redis Cloud feature)'

	static val NUMBERS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val STRINGS = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val VECTOR_TYPES = #{'float_vector' -> 'FLOAT32', 'float16_vector' -> 'FLOAT16', 'bfloat16_vector' -> 'BFLOAT16',
		'float64_vector' -> 'FLOAT64', 'int8_vector' -> 'INT8', 'uint8_vector' -> 'UINT8'}

	val List<String> notes
	val Set<VectorCollection> tenanted = newHashSet
	val Set<VectorCollection> namespaced = newHashSet
	val Map<VectorCollection, Index> evolved = newHashMap
	val Map<VectorCollection, Map<String, String>> renames = newHashMap
	var declaring = false

	new(List<String> notes, Script script) {
		this.notes = notes
		for (object : script.eAllContents.toIterable) slices(object)
	}

	/** Collections whose statements name a tenant or a namespace get the hidden slice attributes. */
	def private void slices(EObject object) {
		val collection = ModelUtil.targetCollection(object)
		if (collection === null) return;
		val tenant = object.eClass.getEStructuralFeature('tenant')
		if (tenant !== null && object.eGet(tenant) !== null) tenanted += collection
		val namespace = object.eClass.getEStructuralFeature('namespace')
		if (namespace !== null && namespace.EType.name == 'EString' && object.eGet(namespace) !== null) namespaced += collection
	}

	def Index index(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, Index index) {
		if (index === null) {
			evolved.remove(c)
			renames.remove(c)
		} else {
			evolved.put(c, index)
		}
	}

	def void rename(VectorCollection c, String field, String newName) {
		val map = renames.computeIfAbsent(c)[newHashMap]
		for (entry : map.entrySet) if (entry.value == field) entry.value = newName
		if (!map.containsValue(newName)) map.put(field, newName)
	}

	/** The document field name of a declared field, after the renames of earlier alter statements. */
	def String name(VectorCollection c, Field field) {
		val declared = ModelUtil.fieldName(field)
		renames.get(c)?.get(declared) ?: declared
	}

	def private Index declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys documents by an integer, text or uuid field')
		}
		val keyName = RedisIndex.id(ModelUtil.fieldName(key))
		val List<Attr> attrs = newArrayList
		val List<String> fields = newArrayList
		val Map<String, Integer> vectors = newLinkedHashMap
		for (field : c.fields) {
			fields += RedisIndex.id(ModelUtil.fieldName(field))
			attrs += attributes(c, field)
			if (field instanceof VectorField) vectors.put(ModelUtil.fieldName(field), field.dimension)
		}
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) RedisIndex.TENANT else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) RedisIndex.NAMESPACE else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else RedisIndex.PARTITION
		for (hidden : #[tenant, namespace, partition].filterNull.filter[startsWith('vecdsl_')]) {
			attrs += new Attr(hidden, '$.' + hidden, 'TAG', null, 1.0, false)
		}
		val analyzer = analyzerOf(c)
		val language = if (analyzer?.language === null) null else languageOf(analyzer)
		val List<String> stopwords = if (language == 'english' && analyzer.stopwords != 'none') null else #[]
		val ttl = if (written(c, 'ttlSeconds')) c.ttlSeconds else null
		new Index(databaseOf(c), RedisIndex.id(c.name), keyName, NUMBERS.contains((key as PayloadField).type), attrs, fields, vectors,
			tenant, namespace, partition, language, stopwords, ttl)
	}

	/** The logical database of a collection: a name prefix of its index and keys (the Query Engine indexes database 0 only). */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) null else RedisIndex.id(database.name)
	}

	/** The analyzer of the collection's full-text fields; an index has one language and one stop-word list. */
	def Analyzer analyzerOf(VectorCollection c) {
		val used = ModelUtil.payloadFields(c).filter[fullText(c, it)].map[analyzer].filterNull.toSet
		used += c.indexes.filter(PayloadIndex).filter[kind == EPayloadIndexKind.FULL_TEXT].map[analyzer].filterNull
		if (used.size > 1 && used.map[language].toSet.size > 1) {
			throw Unsupported.of('full-text fields with analyzers of different languages; a Query Engine index stems with one LANGUAGE')
		}
		val analyzer = used.head
		if (analyzer !== null) checkAnalyzer(analyzer)
		analyzer
	}

	def static void checkAnalyzer(Analyzer analyzer) {
		if (analyzer.tokenizer.literal != 'standard' && analyzer.tokenizer.literal != 'language') {
			throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; the Query Engine splits text at its fixed separator characters (src/toksep.h)')
		}
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) {
			throw Unsupported.of('BM25 parameters; the BM25STD scorer uses the constants k1 = 1.2 and b = 0.75 (src/ext/default.c), which no option changes')
		}
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; has no Redis mapping yet (FT.SYNUPDATE groups and STOPWORDS read from the files)')
		if (analyzer.language !== null) languageOf(analyzer)
		if (written(analyzer, 'stopwords') && analyzer.stopwords != 'none' && analyzer.stopwords != 'english' && analyzer.stopwords != 'en') {
			throw Unsupported.of('stopwords ' + analyzer.stopwords + '; the Query Engine has one built-in stop-word list (English, src/stopwords.h); other lists are given word by word')
		}
		for (filter : analyzer.filters) checkFilter(filter.type.literal, analyzer)
	}

	def static String languageOf(Analyzer analyzer) {
		val language = RedisIndex.language(analyzer.language)
		if (language === null) throw Unsupported.of('the language ' + analyzer.language + '; the Query Engine has no stemmer for it')
		language
	}

	/** Filters the Query Engine applies itself: lowercase always, stop words of the index, stemming with a language. */
	def static void checkFilter(String filter, Analyzer analyzer) {
		switch filter {
			case 'lowercase',
			case 'stop': {}
			case 'stemmer': if (analyzer.language === null) throw Unsupported.of('the stemmer filter without a language; the Query Engine stems in the index LANGUAGE')
			case 'phonetic': throw Unsupported.of('the phonetic filter; has no Redis mapping yet (PHONETIC dm:<language> on the TEXT attributes)')
			case 'synonym': throw Unsupported.of('the synonym filter; has no Redis mapping yet (FT.SYNUPDATE groups)')
			default: throw Unsupported.of('the ' + filter + ' filter; the Query Engine tokenizes, lowercases, removes stop words and stems, with no other token filters')
		}
	}

	def boolean fullText(VectorCollection c, PayloadField field) {
		field.fullText || field.documentBody || field.analyzer !== null
			|| c.indexes.filter(PayloadIndex).exists[target.field === field && kind == EPayloadIndexKind.FULL_TEXT]
	}

	/** The attributes of one field; json fields index their declared sub-fields, other fields one attribute (two for full text). */
	def List<Attr> attributes(VectorCollection c, Field field) {
		switch field {
			VectorField: #[vectorAttr(c, field)]
			PayloadField: payloadAttrs(c, field, '$.' + RedisIndex.id(name(c, field)), name(c, field))
		}
	}

	def private List<Attr> payloadAttrs(VectorCollection c, PayloadField field, String path, String attrName) {
		declaredNote(field, 'nullable', 'maxLength', 'defaultValue')
		if (!field.limits.empty) throw Unsupported.of('field limits; Redis does not validate document values (JSON documents have no constraints)')
		if (!field.subFields.empty) {
			val List<Attr> result = newArrayList
			for (sub : field.subFields) {
				val subName = RedisIndex.id(ModelUtil.fieldName(sub))
				if (sub instanceof PayloadField) result += payloadAttrs(c, sub, path + '.' + subName, attrName + '_' + subName)
				else throw Unsupported.of('a vector field below field ' + attrName)
			}
			return result
		}
		val type = field.type
		if (NUMBERS.contains(type)) return #[new Attr(attrName, path, 'NUMERIC', null, 1.0, false)]
		if (type == EScalarDType.BOOL) return #[new Attr(attrName, path, 'TAG', null, 1.0, false)]
		if (STRINGS.contains(type)) {
			if (fullText(c, field)) {
				if (type != EScalarDType.VARCHAR && type != EScalarDType.TEXT) throw Unsupported.of('full-text search on ' + type.literal + ' field ' + attrName)
				return #[new Attr(attrName, path, 'TEXT', null, 1.0, analyzerOf(c)?.language !== null),
					new Attr(RedisIndex.EXACT + attrName, path, 'TAG', null, 1.0, false)]
			}
			return #[new Attr(attrName, path, 'TAG', null, 1.0, false)]
		}
		switch type {
			case ARRAY: {
				if (!written(field, 'elementType')) throw Unsupported.of('array field ' + attrName + ' without element-type')
				val element = field.elementType
				if (NUMBERS.contains(element)) return #[new Attr(attrName, path + '[*]', 'NUMERIC', null, 1.0, false)]
				if (element == EScalarDType.BOOL || STRINGS.contains(element)) return #[new Attr(attrName, path + '[*]', 'TAG', null, 1.0, false)]
				throw Unsupported.of('arrays of ' + element.literal + ' in field ' + attrName + '; the Query Engine indexes arrays of strings, booleans and numbers')
			}
			case JSON,
			case MAP,
			case STRUCT: return #[]
			case GEO_POINT: throw Unsupported.of('geo_point field ' + attrName + '; has no Redis mapping yet (GEO attributes read "longitude,latitude" strings)')
			case GEOMETRY: throw Unsupported.of('geometry field ' + attrName + '; has no Redis mapping yet (GEOSHAPE attributes read WKT strings)')
			default: throw Unsupported.of(type.literal + ' field ' + attrName + '; JSON documents hold no binary or media values')
		}
	}

	def private Attr vectorAttr(VectorCollection c, VectorField field) {
		val name = RedisIndex.id(name(c, field))
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; the Query Engine scores a multi-value vector attribute by its nearest vector, not by late interaction (max_sim)')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) throw Unsupported.of('server-side embedding of ' + name + NO_MODELS)
		if (field.onDisk) throw Unsupported.of(ON_DISK)
		declaredNote(field, 'normalized', 'nullable')
		val type = VECTOR_TYPES.get(field.type.literal)
		if (type === null) {
			throw Unsupported.of(field.type.literal + ' fields; Query Engine vector attributes hold FLOAT32, FLOAT64, FLOAT16, BFLOAT16, INT8 and UINT8 values (no sparse or binary vectors)')
		}
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		if (!field.limits.empty) throw Unsupported.of('field limits; Redis does not validate document values (JSON documents have no constraints)')
		val index = ModelUtil.vectorIndex(c, field)
		new Attr(name, '$.' + name, 'VECTOR', vec(field, index, type), 1.0, false)
	}

	def String metric(VectorField field, VectorIndex index) {
		val metric = if (written(field, 'metric')) field.metric else if (index !== null && written(index, 'metric')) index.metric else EMetric.COSINE
		metric.literal
	}

	/** FLAT without a declared index (exact search), HNSW for hnsw and auto, SVS-VAMANA for vamana. */
	def Vec vec(VectorField field, VectorIndex index, String type) {
		val metric = metric(field, index)
		val Map<String, String> options = newLinkedHashMap
		if (index === null) return check(new Vec('FLAT', type, field.dimension, metric, options))
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; the Query Engine builds and searches vector indexes on the CPU')
		if (index.onDisk) throw Unsupported.of(ON_DISK)
		if (written(index, 'centroids')) throw Unsupported.of('centroids; the Query Engine has no IVF index (FLAT, HNSW and SVS-VAMANA)')
		for (key : ModelUtil.extParams(index, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a vector index')
		declaredNote(index, 'online')
		val algorithm = switch index.family {
			case FLAT: {
				if (written(index, 'maxDegree') || written(index, 'buildCandidates') || written(index, 'searchCandidates')) {
					throw Unsupported.of('graph settings on a flat index; FLAT takes no options')
				}
				'FLAT'
			}
			case AUTO,
			case HNSW: {
				if (written(index, 'maxDegree')) options.put('M', String.valueOf(index.maxDegree))
				if (written(index, 'buildCandidates')) options.put('EF_CONSTRUCTION', String.valueOf(index.buildCandidates))
				if (written(index, 'searchCandidates')) options.put('EF_RUNTIME', String.valueOf(index.searchCandidates))
				'HNSW'
			}
			case VAMANA: {
				if (type != 'FLOAT32' && type != 'FLOAT16') throw Unsupported.of('SVS-VAMANA on ' + type + ' vectors; SVS-VAMANA indexes FLOAT16 and FLOAT32')
				if (written(index, 'maxDegree')) options.put('GRAPH_MAX_DEGREE', String.valueOf(index.maxDegree))
				if (written(index, 'buildCandidates')) options.put('CONSTRUCTION_WINDOW_SIZE', String.valueOf(index.buildCandidates))
				if (written(index, 'searchCandidates')) options.put('SEARCH_WINDOW_SIZE', String.valueOf(index.searchCandidates))
				'SVS-VAMANA'
			}
			case IVF: throw Unsupported.of('ivf indexes; the Query Engine builds FLAT, HNSW and SVS-VAMANA vector indexes')
			default: throw Unsupported.of(index.family.literal + ' indexes; the Query Engine indexes dense vectors only')
		}
		val q = index.quantization
		if (q !== null && q.type.literal != 'none') {
			if (algorithm != 'SVS-VAMANA') {
				throw Unsupported.of('quantization of a ' + algorithm + ' index; the Query Engine compresses SVS-VAMANA indexes only (vector sets quantize to Q8 and BIN, but collections are Query Engine indexes)')
			}
			if (q.type.literal != 'scalar' || (written(q, 'bits') && q.bits != 8)) {
				throw Unsupported.of(q.type.literal + ' quantization' + (if (written(q, 'bits')) ' to ' + q.bits + ' bits' else '') + '; Redis Open Source compresses SVS-VAMANA vectors with 8-bit scalar quantization only (Intel\'s LVQ and LeanVec optimizations are not in Redis Open Source)')
			}
			declaredNote(q, 'rescore', 'oversampling', 'keepOriginals', 'truncateDimension')
			options.put('COMPRESSION', 'LVQ8')
		}
		check(new Vec(algorithm, type, field.dimension, metric, options))
	}

	def private Vec check(Vec vec) {
		vec.distance
		vec
	}

	/** Index attributes a payload index asks for are the field's own; a different kind is not generated. */
	def void checkPayloadIndex(VectorCollection c, PayloadIndex payloadIndex) {
		val path = payloadIndex.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		for (key : ModelUtil.extParams(payloadIndex, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a payload index')
		if (payloadIndex.onDisk) throw Unsupported.of(ON_DISK)
		note(payloadIndex, 'structure', 'lowercase', 'minTokenLength', 'maxTokenLength', 'prefixMatch')
		val attrName = (#[name(c, path.field)] + path.subFields.map[ModelUtil.fieldName(it)]).join('_')
		val kind = index(c).attr(attrName).kind
		val expected = switch payloadIndex.kind {
			case KEYWORD,
			case BOOL,
			case UUID: 'TAG'
			case INTEGER,
			case FLOAT: 'NUMERIC'
			case FULL_TEXT: 'TEXT'
			case DATETIME: 'TAG'
			case GEO: throw Unsupported.of('geo payload indexes; has no Redis mapping yet (GEO attributes)')
			case JSON: throw Unsupported.of('json payload indexes; the Query Engine indexes JSON values through attributes on their paths (index the sub-fields)')
		}
		if (kind != expected) throw Unsupported.of('a ' + payloadIndex.kind.literal + ' index on ' + attrName + ', which is a ' + kind + ' attribute; has no Redis mapping yet (a second attribute on the same path)')
	}

	// ---------------------------------------------------------------- statements

	/** FT.CREATE of the collection's index; if-not-exists ignores an existing index. */
	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(RedisStatements.EXTERNAL)
		if (c.ttlField !== null) throw Unsupported.of('ttl-field; has no Redis mapping yet (EXPIREAT from each record\'s field value)')
		if (c.autoId) throw Unsupported.of('auto-id; has no Redis mapping yet (keys from an INCR counter)')
		if (c.deletionProtection) throw Unsupported.of(RedisStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; Redis keys hold no references to other keys, and the Query Engine searches one index per query')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + NO_MODELS)
		if (c.storage !== null) throw Unsupported.of(RedisStatements.NO_STORAGE)
		if (!c.tags.empty) throw Unsupported.of(RedisStatements.NO_TAGS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + RedisStatements.functionReason(c.functions.head.type.literal))
		if (!c.limits.empty) throw Unsupported.of(RedisStatements.collectionLimit(c.limits.head.type.literal))
		for (key : ModelUtil.extParams(c, RedisGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a collection')
		if (!ModelUtil.partitions(c).filter[it.key !== null || !subPartitions.empty || (written(it, 'strategy') && strategy.literal != 'list')].empty) {
			throw Unsupported.of('partitions by key, with a strategy other than list, or with sub-partitions; a partition is a value of the hidden vecdsl_partition tag')
		}
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency', 'dynamic', 'description')
		if (fresh) evolve(c, null)
		declaring = true
		val current = try index(c) finally declaring = false
		for (payloadIndex : c.indexes.filter(PayloadIndex)) checkPayloadIndex(c, payloadIndex)
		val List<Stmt> result = newArrayList
		result += new Code.Run(command(RedisIndex.definition(current), if (ifNotExists) #['index already exists'] else #[]))
		if (fresh || !ifNotExists) {
			for (alias : c.aliases) {
				result += new Code.Run(command(#['FT.ALIASADD', RedisIndex.id(alias.name), current.name], if (ifNotExists) #['alias already exists'] else #[]))
			}
		}
		result
	}

	/** Settings of a declaration that the index ignores, listed only on the statement that creates the collection. */
	def private void declaredNote(EObject host, String... features) {
		if (declaring) note(host, features)
	}

	def void note(EObject host, String... features) {
		for (feature : features) {
			if (written(host, feature)) notes += feature + ' on ' + host.eClass.name
		}
	}

	def static Code.Call command(List<String> args, List<String> ignore) {
		val call = kw('args', texts(args))
		if (!ignore.empty) call.put('ignore', texts(ignore))
		runtime('command', call)
	}

	def static Entries kw(Object... pairs) {
		val result = entries
		for (var i = 0; i < pairs.length; i += 2) {
			result.put(pairs.get(i) as String, pairs.get(i + 1) as Code.Expr)
		}
		result
	}
}
