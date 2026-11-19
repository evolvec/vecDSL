package org.ftn.vecDSL.generator.mongodb

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
import org.ftn.vecDSL.generator.mongodb.MongodbCollection.Vector
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Connection
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.FieldLimit
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.Insert
import org.ftn.vecDSL.vecDSL.Update
import org.ftn.vecDSL.vecDSL.Upsert
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to MongoDB collections (server 9.0 with mongot): one collection per vecDSL collection in its
 * database (the logical-db, else the connection's database), documents keyed by _id, a $jsonSchema validator from the declared
 * fields, one vectorSearch index per vector index with every filterable field as a filter field, the search index vecdsl_text
 * for the full-text fields with their analyzers, regular indexes for payload indexes and a TTL index for collections with a
 * ttl. Tenants, namespaces and partitions are hidden fields (or the partition-key field).
 */
class MongodbSchema {

	static val INTEGERS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8]
	static val NUMBERS = #[EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val STRINGS = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val DATES = #[EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]
	static val TOKENIZERS = #{'standard' -> 'standard', 'whitespace' -> 'whitespace', 'keyword' -> 'keyword', 'ngram' -> 'nGram',
		'edge_ngram' -> 'edgeGram', 'language' -> 'standard'}
	static val LANGUAGES = #{'en' -> 'english', 'english' -> 'english', 'de' -> 'german', 'german' -> 'german', 'fr' -> 'french',
		'french' -> 'french', 'es' -> 'spanish', 'spanish' -> 'spanish', 'it' -> 'italian', 'italian' -> 'italian',
		'pt' -> 'portuguese', 'portuguese' -> 'portuguese', 'nl' -> 'dutch', 'dutch' -> 'dutch', 'ru' -> 'russian',
		'russian' -> 'russian', 'sv' -> 'swedish', 'swedish' -> 'swedish', 'da' -> 'danish', 'danish' -> 'danish',
		'fi' -> 'finnish', 'finnish' -> 'finnish', 'no' -> 'norwegian', 'norwegian' -> 'norwegian', 'hu' -> 'hungarian',
		'hungarian' -> 'hungarian', 'ro' -> 'romanian', 'romanian' -> 'romanian', 'tr' -> 'turkish', 'turkish' -> 'turkish'}

	val List<String> notes
	val String defaultDatabase
	val Set<VectorCollection> tenanted = newHashSet
	val Set<VectorCollection> namespaced = newHashSet
	val Set<VectorCollection> versioned = newHashSet
	val Set<VectorCollection> expiring = newHashSet
	val Map<VectorCollection, MongodbCollection> evolved = newHashMap

	new(List<String> notes, Script script, String fallbackDatabase) {
		this.notes = notes
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		this.defaultDatabase = if (declared !== null && declared.databaseName !== null) checkedName(declared.databaseName, 'database') else fallbackDatabase
		for (object : script.eAllContents.toIterable) slices(object)
	}

	/** Collections whose statements name a tenant or namespace get the hidden fields; if-version adds the version field. */
	def private void slices(EObject object) {
		val collection = ModelUtil.targetCollection(object)
		if (collection === null) return;
		val tenantFeature = object.eClass.getEStructuralFeature('tenant')
		if (tenantFeature !== null && object.eGet(tenantFeature) !== null) tenanted += collection
		val namespaceFeature = object.eClass.getEStructuralFeature('namespace')
		if (namespaceFeature !== null && namespaceFeature.EType.name == 'EString' && object.eGet(namespaceFeature) !== null) namespaced += collection
		if (object instanceof Update && written(object, 'ifVersion')) versioned += collection
		if ((object instanceof Insert || object instanceof Upsert) && written(object, 'ttlSeconds')) expiring += collection
	}

	def String defaultDatabase() {
		defaultDatabase
	}

	/** The collection as the statements so far leave it (alter statements change fields and the name). */
	def MongodbCollection collection(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, MongodbCollection next) {
		if (next === null) evolved.remove(c) else evolved.put(c, next)
	}

	def private MongodbCollection declared(VectorCollection c) {
		val key = c.primaryKey
		val result = new MongodbCollection
		result.database = databaseOf(c)
		result.name = checkedName(c.name, 'collection')
		if (!(key instanceof PayloadField)) throw Unsupported.of('a vector field as the primary key; _id holds an integer or string key')
		val keyType = (key as PayloadField).type
		if (INTEGERS.contains(keyType)) {
			result.keySchema = obj('bsonType', arr(text('int'), text('long')))
			result.keyKind = 'number'
		} else if (STRINGS.contains(keyType)) {
			result.keySchema = obj('bsonType', text('string'))
			result.keyKind = 'token'
		} else {
			throw Unsupported.of('a primary key of type ' + keyType.literal + '; the generator keys documents (_id) by an integer, text or uuid field')
		}
		result.key = ModelUtil.fieldName(key)
		for (declaredField : c.fields) result.fields += field(c, declaredField)
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		result.tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_FIELD else partitionKey.name)
		result.namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_FIELD else partitionKey.name)
		result.partition = if (ModelUtil.partitions(c).empty) null else PARTITION_FIELD
		result.versioned = versioned.contains(c)
		result.expiring = expiring.contains(c)
		result.description = c.description
		result.dynamic = c.dynamic
		if (written(c, 'ttlSeconds')) result.ttlSeconds = Long.valueOf(c.ttlSeconds)
		for (vectorField : ModelUtil.vectorFields(c)) result.vectorIndexes += vectorIndex(c, vectorField)
		for (payloadIndex : c.indexes.filter(PayloadIndex)) {
			val keys = payloadIndexKeys(result, payloadIndex)
			if (keys !== null) result.payloadIndexes += new MongodbCollection.PayloadIndex(MongodbCommands.field(payloadIndex.name), keys)
		}
		result.analyzers += analyzers(c.analyzers)
		for (limit : c.limits) {
			switch limit.type.literal {
				case 'max_fields': result.maxProperties = limit.value
				case 'max_vector_dim': for (vectorField : result.vectorFields) if (vectorField.vector.dims > limit.value) throw Unsupported.of('vector field ' + vectorField.name + ' with more dimensions than max_vector_dim')
				default: throw Unsupported.of('collection limits (' + limit.type.literal + '); ' + MongodbStatements.SINGLE)
			}
		}
		result
	}

	/** The database of a collection: its logical-db, else the connection's database, else vecdsl. */
	def String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) defaultDatabase else checkedName(database.name, 'database')
	}

	def MongodbCollection.Field field(VectorCollection c, Field declaredField) {
		switch declaredField {
			VectorField: vectorField(c, declaredField)
			PayloadField: payloadField(declaredField)
		}
	}

	def private MongodbCollection.Field payloadField(PayloadField f) {
		val fieldName = MongodbCommands.field(ModelUtil.fieldName(f))
		note(f, 'documentBody', 'partitionKey')
		val defaultValue = if (f.defaultValue === null) null else Literals.of(f.defaultValue)
		val schema = scalarSchema(f)
		if (f.nullable) nullable(schema)
		for (limit : f.limits) limitSchema(schema, limit)
		val List<String> paths = newArrayList
		filterPaths(f, fieldName, paths)
		val textual = f.fullText || f.analyzer !== null
		if (textual && !STRINGS.contains(f.type)) throw Unsupported.of('full-text search on ' + f.type.literal + ' field ' + fieldName + '; Atlas Search analyzes string fields')
		val analyzerName = if (f.analyzer === null) null else analyzerName(f.analyzer)
		new MongodbCollection.Field(fieldName, schema, textual, analyzerName, null, DATES.contains(f.type), paths, defaultValue)
	}

	/** The $jsonSchema property of a payload field: its BSON type, string length, object properties and array items. */
	def private Entries scalarSchema(PayloadField f) {
		val type = f.type
		if (!f.subFields.empty || f.nested || type == EScalarDType.STRUCT) {
			val Entries properties = Code.entries
			for (sub : f.subFields) {
				if (!(sub instanceof PayloadField)) throw Unsupported.of('a vector field inside ' + f.name + '; vectorSearch indexes take vectors at document paths (nested vectors need nestedRoot, ' + MongodbStatements.PENDING + ')')
				val inner = scalarSchema(sub as PayloadField)
				if ((sub as PayloadField).nullable) nullable(inner)
				properties.put(MongodbCommands.field(sub.name), inner)
			}
			val objectSchema = obj('bsonType', text('object'), 'properties', if (properties.entries.empty) null else properties)
			return if (f.nested) obj('bsonType', text('array'), 'items', objectSchema) else objectSchema
		}
		if (type == EScalarDType.ARRAY) {
			if (!written(f, 'elementType')) throw Unsupported.of('array field ' + f.name + ' without element-type')
			return obj('bsonType', text('array'), 'items', elementSchema(f.elementType, f))
		}
		elementSchema(type, f)
	}

	def private Entries elementSchema(EScalarDType type, PayloadField f) {
		if (INTEGERS.contains(type)) return obj('bsonType', arr(text('int'), text('long')))
		if (NUMBERS.contains(type)) return obj('bsonType', text('number'))
		if (STRINGS.contains(type)) return obj('bsonType', text('string'), 'maxLength', if (written(f, 'maxLength')) num(f.maxLength) else null)
		if (DATES.contains(type)) return obj('bsonType', text('date'))
		switch type {
			case BOOL: obj('bsonType', text('bool'))
			case JSON,
			case MAP: obj('bsonType', text('object'))
			case GEO_POINT,
			case GEOMETRY: obj('bsonType', text('object'), 'required', arr(text('type'), text('coordinates')))
			default: throw Unsupported.of(type.literal + ' field ' + f.name + '; generated programs write JSON values, and MongoDB keeps media as binary data or URLs, not ' + type.literal + ' objects')
		}
	}

	def private void nullable(Entries schema) {
		val type = schema.get('bsonType')
		val List<Expr> types = newArrayList
		if (type instanceof Code.Items) types += (type as Code.Items).items else types += type
		types += text('null')
		schema.put('bsonType', arr(types))
	}

	def private void limitSchema(Entries schema, FieldLimit limit) {
		switch limit.type.literal {
			case 'vector_dim': {
				schema.put('minItems', num(limit.value))
				schema.put('maxItems', num(limit.value))
			}
			case 'array_len': schema.put('maxItems', num(limit.value))
			default: schema.put('maxLength', num(limit.value))
		}
	}

	/** The paths a vectorSearch filter can compare: boolean, date, numeric and string values and arrays of them. */
	def private void filterPaths(PayloadField f, String path, List<String> paths) {
		if (!f.subFields.empty) {
			if (f.nested) return;
			for (sub : f.subFields.filter(PayloadField)) filterPaths(sub, path + '.' + sub.name, paths)
			return
		}
		val type = if (f.type == EScalarDType.ARRAY && written(f, 'elementType')) f.elementType else f.type
		if (INTEGERS.contains(type) || NUMBERS.contains(type) || STRINGS.contains(type) || DATES.contains(type) || type == EScalarDType.BOOL) paths += path
	}

	def private MongodbCollection.Field vectorField(VectorCollection c, VectorField f) {
		val fieldName = MongodbCommands.field(ModelUtil.fieldName(f))
		if (f.multiVector) throw Unsupported.of('multi-vector field ' + fieldName + '; a vectorSearch index compares one vector per document path (nested vectors need nestedRoot)')
		if (f.model !== null || ModelUtil.producingFunction(c, f) !== null) throw Unsupported.of('server-side embedding of ' + fieldName + MongodbStatements.NO_MODELS)
		note(f, 'onDisk')
		switch f.type.literal {
			case 'float_vector': {}
			case 'int8_vector',
			case 'binary_vector': throw Unsupported.of(f.type.literal + ' fields; ' + MongodbStatements.PENDING + ' (BinData vectors of subtype int8 and int1 in the three runtimes)')
			case 'sparse_float_vector': throw Unsupported.of('sparse vectors; vectorSearch indexes take dense vectors only (vector and filter fields)')
			default: throw Unsupported.of(f.type.literal + ' fields; mongot indexes vectors of doubles and BinData vectors of float32, int8 and int1')
		}
		if (f.dimension <= 0) throw Unsupported.of('vector field ' + fieldName + ' without dim')
		if (f.dimension > MAX_DIMENSIONS) throw Unsupported.of('vector field ' + fieldName + ' with ' + f.dimension + ' dimensions; vectorSearch indexes take at most 8192')
		val index = ModelUtil.vectorIndex(c, f)
		val chosenMetric = metric(f, index)
		val sim = similarity(chosenMetric, f.normalized)
		val schema = obj('bsonType', text('array'), 'minItems', num(f.dimension), 'maxItems', num(f.dimension), 'items', obj('bsonType', text('double')))
		if (f.nullable) nullable(schema)
		new MongodbCollection.Field(fieldName, schema, false, null, new Vector(f.dimension, sim, chosenMetric), false, newArrayList, null)
	}

	/** The vectorSearch index of a vector field: the declared index, else one named after the field with the defaults. */
	def MongodbCollection.VectorIndex vectorIndex(VectorCollection c, VectorField f) {
		val index = ModelUtil.vectorIndex(c, f)
		val fieldName = ModelUtil.fieldName(f)
		if (index === null) return new MongodbCollection.VectorIndex(fieldName + '_index', fieldName, Code.entries, null)
		new MongodbCollection.VectorIndex(MongodbCommands.field(index.name), fieldName, indexOptions(f, index), if (written(index, 'searchCandidates')) index.searchCandidates else null)
	}

	/**
	 * The index options of a vector field: indexingMethod flat or hnsw with hnswOptions (maxEdges 16 to 64, numEdgeCandidates
	 * 100 to 3200), quantization scalar or binary.
	 */
	def private Entries indexOptions(VectorField f, VectorIndex index) {
		note(index, 'onDisk', 'online')
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; mongot builds its indexes on the CPU')
		if (written(index, 'centroids')) throw Unsupported.of('centroids; mongot builds hnsw and flat indexes, with no clustered (IVF) index')
		for (key : ModelUtil.extParams(index, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		val Entries options = Code.entries
		switch index.family.literal {
			case 'auto',
			case 'hnsw': {
				val Entries hnsw = Code.entries
				if (written(index, 'maxDegree')) {
					if (index.maxDegree < 16 || index.maxDegree > 64) throw Unsupported.of('max-degree ' + index.maxDegree + '; maxEdges of hnswOptions lies between 16 and 64')
					hnsw.put('maxEdges', num(index.maxDegree))
				}
				if (written(index, 'buildCandidates')) {
					if (index.buildCandidates < 100 || index.buildCandidates > 3200) throw Unsupported.of('build-candidates ' + index.buildCandidates + '; numEdgeCandidates of hnswOptions lies between 100 and 3200')
					hnsw.put('numEdgeCandidates', num(index.buildCandidates))
				}
				if (!hnsw.entries.empty) options.put('hnswOptions', hnsw)
			}
			case 'flat': {
				if (written(index, 'maxDegree') || written(index, 'buildCandidates')) throw Unsupported.of('max-degree and build-candidates on a flat index')
				options.put('indexingMethod', text('flat'))
			}
			default: throw Unsupported.of(index.family.literal + ' indexes; mongot builds hnsw and flat vector indexes')
		}
		val q = index.quantization
		if (q !== null) {
			note(q, 'keepOriginals')
			if (written(q, 'truncateDimension')) throw Unsupported.of('truncate-dim; mongot indexes the full vector')
			if (q.rescore || written(q, 'oversampling')) throw Unsupported.of('rescore and oversampling settings; mongot rescores quantized vectors itself and takes no oversampling factor (numCandidates is the effort setting)')
			switch q.type.literal {
				case 'none': {}
				case 'scalar': {
					if (written(q, 'bits') && q.bits != 8.0) throw Unsupported.of('scalar quantization to ' + q.bits + ' bits; scalar quantization in mongot keeps one byte per dimension')
					options.put('quantization', text('scalar'))
				}
				case 'binary': {
					if (written(q, 'bits') && q.bits != 1.0) throw Unsupported.of('binary quantization with ' + q.bits + ' bits; binary quantization keeps one bit per dimension')
					if (f.dimension % 8 != 0) throw Unsupported.of('binary quantization of ' + f.dimension + ' dimensions; numDimensions must be a multiple of 8')
					options.put('quantization', text('binary'))
				}
				default: throw Unsupported.of('product quantization; mongot quantizes with scalar and binary quantization')
			}
		}
		options
	}

	/** The metric of a vector field: the field's, else its index's, else cosine. */
	def String metric(VectorField f, VectorIndex index) {
		val chosen = if (written(f, 'metric')) f.metric else if (index !== null && written(index, 'metric')) index.metric else EMetric.COSINE
		chosen.literal
	}

	/** The keys of a regular index for a declared payload index; null when the index is part of the search index (full_text). */
	def Entries payloadIndexKeys(MongodbCollection current, PayloadIndex payloadIndex) {
		note(payloadIndex, 'onDisk', 'minTokenLength', 'maxTokenLength', 'castType')
		for (key : ModelUtil.extParams(payloadIndex, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		if (written(payloadIndex, 'structure') && payloadIndex.structure.literal == 'bitmap') throw Unsupported.of('bitmap payload indexes; MongoDB builds B-tree, 2dsphere, text, hashed and wildcard indexes')
		if (payloadIndex.lowercase) throw Unsupported.of('lowercase payload indexes; a case-insensitive index needs a collation, which also changes how every query on the collection compares strings')
		if (payloadIndex.prefixMatch) throw Unsupported.of('prefix-match payload indexes; a B-tree index serves anchored $regex prefixes already, and Atlas Search prefixes are the autocomplete type of vecdsl_text')
		if (payloadIndex.analyzer !== null) throw Unsupported.of('an analyzer on a payload index; declare the analyzer on the full-text field')
		val path = payloadIndex.target
		val dotted = if (path.rawPath !== null) MongodbCommands.field(path.rawPath) else {
			if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field; vector fields are indexed by vectorSearch indexes')
			(#[current.path(ModelUtil.fieldName(path.field))] + path.subFields.map[s | ModelUtil.fieldName(s)]).join('.')
		}
		switch payloadIndex.kind.literal {
			case 'full_text': {
				val leaf = ModelUtil.leaf(path)
				if (!(leaf instanceof PayloadField && (leaf as PayloadField).fullText)) throw Unsupported.of('a full_text index on ' + dotted + ', which is not a full-text field (declare full-text on the field; it joins the search index vecdsl_text)')
				null
			}
			case 'geo': obj(dotted, text('2dsphere'))
			case 'json': throw Unsupported.of('json payload indexes; MongoDB wildcard indexes cover paths, not a cast value type')
			default: obj(dotted, num(1))
		}
	}

	// ---------------------------------------------------------------- analysis

	/** The name a field's analyzer has in the search index: lucene.<language> for a plain language analyzer, else the custom name. */
	def String analyzerName(Analyzer analyzer) {
		val language = languageOf(analyzer)
		if (builtIn(analyzer, language)) 'lucene.' + language else MongodbCommands.field(analyzer.name)
	}

	def private String languageOf(Analyzer analyzer) {
		if (analyzer.language === null) return null
		val language = LANGUAGES.get(analyzer.language.toLowerCase)
		if (language === null) throw Unsupported.of('the language ' + analyzer.language + '; the generator uses the Lucene language analyzers of Atlas Search for english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian and turkish')
		language
	}

	def private boolean builtIn(Analyzer analyzer, String language) {
		language !== null && analyzer.filters.empty && (analyzer.tokenizer.literal == 'standard' || analyzer.tokenizer.literal == 'language')
	}

	/** Custom analyzers of the search index; plain language analyzers are the built-in lucene.<language> ones. */
	def List<Expr> analyzers(List<Analyzer> declared) {
		val List<Expr> result = newArrayList
		for (analyzer : declared) {
			if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; ' + MongodbStatements.NO_FILES)
			if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) throw Unsupported.of('BM25 parameters; string fields of a search index take the similarity bm25, boolean or stableTfl without parameters')
			if (written(analyzer, 'stopwords') && analyzer.stopwords != 'none') throw Unsupported.of('stopwords ' + analyzer.stopwords + '; the stopword token filter of Atlas Search takes an explicit list of tokens, not a named list')
			for (key : ModelUtil.extParams(analyzer, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
			val language = languageOf(analyzer)
			val tokenizerType = TOKENIZERS.get(analyzer.tokenizer.literal)
			if (tokenizerType === null) throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; Atlas Search has icuFolding and icuNormalizer token filters but no icu tokenizer')
			if (!builtIn(analyzer, language)) {
				val tokenizer = if (tokenizerType == 'nGram' || tokenizerType == 'edgeGram') obj('type', text(tokenizerType), 'minGram', num(1), 'maxGram', num(2)) else obj('type', text(tokenizerType))
				val List<Expr> chain = newArrayList
				chain += obj('type', text('lowercase'))
				for (analyzerFilter : analyzer.filters) {
					switch analyzerFilter.type.literal {
						case 'lowercase': {}
						case 'stemmer': {
							if (language === null) throw Unsupported.of('the stemmer filter without a language')
							chain += obj('type', text('snowballStemming'), 'stemmerName', text(language))
						}
						case 'ascii_folding': chain += obj('type', text('asciiFolding'))
						case 'stop': throw Unsupported.of('the stop filter; the stopword token filter of Atlas Search takes an explicit list of tokens (a language without filters uses the lucene.<language> analyzer, which removes its stop words)')
						case 'length': throw Unsupported.of('the length filter; vecDSL gives no bounds')
						case 'synonym': throw Unsupported.of('the synonym filter; Atlas Search reads synonyms from a collection named in a synonym mapping, not from a filter')
						case 'decompounder': throw Unsupported.of('the decompounder filter; Atlas Search has no dictionary decompounder token filter')
						default: throw Unsupported.of('the phonetic filter; Atlas Search has daitchMokotoffSoundex only, which is not a general phonetic filter')
					}
				}
				if (language !== null && analyzer.filters.empty) chain += obj('type', text('snowballStemming'), 'stemmerName', text(language))
				result += obj('name', text(MongodbCommands.field(analyzer.name)), 'tokenizer', tokenizer, 'tokenFilters', arr(chain))
			}
		}
		result
	}

	// ---------------------------------------------------------------- collection creation

	/**
	 * The statements that create a collection: create with the validator, createIndexes for payload indexes and the TTL index,
	 * createSearchIndexes, views for aliases, then await every search index. With if-not-exists they run only when the collection
	 * is missing.
	 */
	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(MongodbStatements.EXTERNAL)
		if (c.autoId) throw Unsupported.of('auto-id; ' + MongodbStatements.PENDING + ' (ObjectId keys returned as strings)')
		if (c.deletionProtection) throw Unsupported.of(MongodbStatements.NO_DELETION_PROTECTION)
		if (!c.tags.empty) throw Unsupported.of(MongodbStatements.NO_TAGS)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; MongoDB has no references between collections ($lookup joins at query time)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + MongodbStatements.NO_MODELS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + MongodbStatements.functionReason(c.functions.head.type.literal))
		if (c.storage !== null) MongodbStatements.storage(c.storage)
		MongodbStatements.cluster(c.cluster ?: EcoreUtil2.getContainerOfType(c, org.ftn.vecDSL.vecDSL.Cluster))
		for (key : ModelUtil.extParams(c, MongodbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key)
		note(c, 'idType', 'generativeModel')
		if (!c.storedSourceFields.empty) notes += 'stored-source on VectorCollection, which mongot decides (documents are read from mongod)'
		if (fresh) evolve(c, null)
		val current = collection(c)
		for (partition : ModelUtil.partitions(c)) MongodbStatements.checkPartition(partition)
		val List<Stmt> steps = newArrayList
		steps += new Code.Run(command(current.database, obj('create', text(current.name), 'validator', current.validator, 'validationLevel', text('strict'),
			'validationAction', text('error'))))
		steps += indexStatements(current, c)
		for (alias : c.aliases) steps += new Code.Run(command(current.database, obj('create', text(checkedName(alias.name, 'view')), 'viewOn', text(current.name), 'pipeline', arr())))
		if (ifNotExists) newArrayList(new Code.When(call('exists', 'database', text(current.database), 'collection', text(current.name)), true, steps) as Stmt) else steps
	}

	/** createIndexes for the payload and TTL indexes, createSearchIndexes and an await of every search index. */
	def List<Stmt> indexStatements(MongodbCollection current, VectorCollection c) {
		val List<Stmt> steps = newArrayList
		val List<Expr> regular = newArrayList
		for (index : current.payloadIndexes) regular += obj('key', index.keys, 'name', text(index.name))
		if (current.ttlSeconds !== null || current.expiring || (c !== null && c.ttlField !== null)) {
			if (c !== null && c.ttlField !== null) {
				val ttlField = current.field(ModelUtil.fieldName(c.ttlField))
				if (!ttlField.date) throw Unsupported.of('a ttl-field that is not a date field; TTL indexes expire documents by a date value')
				regular += obj('key', obj(ttlField.name, num(1)), 'name', text('vecdsl_ttl'), 'expireAfterSeconds', num((current.ttlSeconds ?: 0L).doubleValue))
			} else {
				regular += obj('key', obj(EXPIRES_FIELD, num(1)), 'name', text('vecdsl_ttl'), 'expireAfterSeconds', num(0))
			}
		}
		if (!regular.empty) steps += new Code.Run(command(current.database, obj('createIndexes', text(current.name), 'indexes', arr(regular))))
		val searchIndexes = current.searchIndexes
		if (!searchIndexes.empty) {
			steps += new Code.Run(command(current.database, obj('createSearchIndexes', text(current.name), 'indexes', arr(searchIndexes))))
			for (index : searchIndexes) steps += awaitIndex(current, ((index as Entries).get('name') as Code.Text).value, AWAIT_SECONDS)
		}
		steps
	}

	def Stmt awaitIndex(MongodbCollection current, String index, int seconds) {
		new Code.Run(call('await_index', 'database', text(current.database), 'collection', text(current.name), 'index', text(index), 'timeout_seconds', num(seconds)))
	}

	def void note(EObject host, String... features) {
		for (feature : features) {
			if (written(host, feature)) notes += feature + ' on ' + host.eClass.name
		}
	}
}
