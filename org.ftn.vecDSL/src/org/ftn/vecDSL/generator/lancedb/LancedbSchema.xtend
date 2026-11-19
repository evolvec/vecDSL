package org.ftn.vecDSL.generator.lancedb

import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EAccelerator
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType
import org.ftn.vecDSL.vecDSL.EIndexFamily
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EPayloadIndexStructure
import org.ftn.vecDSL.vecDSL.EQuantizationKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ETokenizer
import org.ftn.vecDSL.vecDSL.EVectorDType
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
 * Maps vecDSL collections to Lance tables (LanceDB 0.41.0, Lance 14.0.2). A collection is one table with a declared Arrow
 * schema: payload fields are typed columns, vector fields fixed-size lists (a multivector field a list of them), nested fields
 * structs. Lance has no primary key; the key column is kept unique by the runtime (merge insert on the key). Tenant, namespace
 * and partition names live in the partition-key field or in hidden text columns. Indexes are created with the table.
 */
class LancedbSchema {

	public static val TENANT_COLUMN = 'vecdsl_tenant'
	public static val NAMESPACE_COLUMN = 'vecdsl_namespace'
	public static val PARTITION_COLUMN = 'vecdsl_partition'
	public static val DESCRIPTION = 'lancedb:description'
	public static val TAGS = 'vecdsl:tags'
	public static val LANCE_SCHEMA = 'https://github.com/lance-format/lance/blob/v14.0.2/docs/src/format/table/schema.md'

	static val SCALARS = #{EScalarDType.INT -> 'int32', EScalarDType.BIGINT -> 'int64', EScalarDType.INT8 -> 'int8',
		EScalarDType.FLOAT -> 'float32', EScalarDType.DOUBLE -> 'float64', EScalarDType.BOOL -> 'bool',
		EScalarDType.VARCHAR -> 'utf8', EScalarDType.TEXT -> 'utf8', EScalarDType.UUID -> 'utf8', EScalarDType.DATE -> 'date',
		EScalarDType.DATETIME -> 'timestamp', EScalarDType.TIMESTAMPTZ -> 'timestamptz', EScalarDType.BYTES -> 'binary',
		EScalarDType.IMAGE -> 'binary', EScalarDType.AUDIO -> 'binary', EScalarDType.VIDEO -> 'binary'}
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val ELEMENTS = #{EVectorDType.FLOAT_VECTOR -> 'float32', EVectorDType.FLOAT16_VECTOR -> 'float16',
		EVectorDType.FLOAT64_VECTOR -> 'float64', EVectorDType.BINARY_VECTOR -> 'uint8'}
	static val NAME = '[A-Za-z0-9_][A-Za-z0-9_.-]*'

	val List<String> notes
	val Set<VectorCollection> tenanted = newHashSet
	val Set<VectorCollection> namespaced = newHashSet
	val Map<VectorCollection, LancedbTable> evolved = newHashMap
	val Map<Field, String> renamed = newHashMap

	new(List<String> notes, Script script) {
		this.notes = notes
		if (script !== null) {
			for (object : script.eAllContents.toIterable) slices(object)
		}
	}

	/** Collections whose statements name a tenant or a namespace get the slice columns. */
	def private void slices(EObject object) {
		val collection = ModelUtil.targetCollection(object)
		if (collection === null) return;
		val tenant = object.eClass.getEStructuralFeature('tenant')
		if (tenant !== null && object.eGet(tenant) !== null) tenanted += collection
		val namespace = object.eClass.getEStructuralFeature('namespace')
		if (namespace !== null && namespace.EType.name == 'EString' && object.eGet(namespace) !== null) namespaced += collection
	}

	// ---------------------------------------------------------------- tables

	/** The table as the statements so far leave it; the declaration's notes belong to its create statement only. */
	def LancedbTable table(VectorCollection c) {
		val known = evolved.get(c)
		if (known !== null) return known
		val size = notes.size
		val result = declared(c)
		while (notes.size > size) notes.remove(notes.size - 1)
		result
	}

	/** Name and logical database of a collection's table, without the checks of its declaration (drop statements). */
	def Entries placeOf(VectorCollection c) {
		val known = evolved.get(c)
		if (known !== null) return known.place
		val result = kw('name', text(c.name))
		val database = databaseOf(c)
		if (database !== null) result.put('database', text(database))
		result
	}

	/** Records the table an alter statement leaves; create and drop return the collection to its declaration. */
	def void evolve(VectorCollection c, LancedbTable table) {
		if (table !== null) {
			evolved.put(c, table)
			return
		}
		evolved.remove(c)
		for (field : c.fields) renamed.remove(field)
	}

	/** The column of a field as the statements so far leave it (a rename-field changes it). */
	def String column(Field field) {
		renamed.get(field) ?: ModelUtil.fieldName(field)
	}

	def void rename(Field field, String name) {
		renamed.put(field, name)
	}

	def private LancedbTable declared(VectorCollection c) {
		checkName(c.name)
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys tables by an integer or text column')
		}
		val List<Entries> fields = newArrayList
		val Map<String, String> vectors = newLinkedHashMap
		for (field : c.fields) {
			fields += fieldOf(c, field)
			if (field instanceof VectorField) vectors.put(ModelUtil.fieldName(field), metricName(field, ModelUtil.vectorIndex(c, field)))
		}
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_COLUMN else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_COLUMN else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else PARTITION_COLUMN
		for (column : #[tenant, namespace, partition].filterNull) {
			if (fields.forall[f | (f.get('name') as Code.Text).value != column]) fields += kw('name', text(column), 'type', text('utf8'))
		}
		val List<Entries> indexes = newArrayList
		for (field : ModelUtil.vectorFields(c)) {
			val index = ModelUtil.vectorIndex(c, field)
			if (index !== null) indexes += vectorIndex(index, field, metricName(field, index))
		}
		for (index : c.indexes.filter(PayloadIndex)) indexes += payloadIndex(c, index)
		for (field : ModelUtil.payloadFields(c)) {
			if (searchable(field) && !c.indexes.filter(PayloadIndex).exists[target.field === field && kind == EPayloadIndexKind.FULL_TEXT]) {
				indexes += fts(ModelUtil.fieldName(field) + '_fts', ModelUtil.fieldName(field), field.analyzer)
			}
		}
		val keyField = fields.findFirst[f | (f.get('name') as Code.Text).value == ModelUtil.fieldName(key)]
		keyField.put('nullable', flag(false))
		val metadata = entries
		if (c.description !== null) metadata.put(DESCRIPTION, text(c.description))
		if (!c.tags.empty) metadata.put(TAGS, texts(c.tags))
		if (!metadata.entries.empty) keyField.put('metadata', metadata)
		new LancedbTable(databaseOf(c), c.name, ModelUtil.fieldName(key), fields, vectors, tenant, namespace, partition, indexes)
	}

	/** Whether a payload field is searched by keyword search: full-text, document-body or with an analyzer. */
	def boolean searchable(PayloadField field) {
		field.fullText || field.documentBody || field.analyzer !== null
	}

	def List<Stmt> createTable(VectorCollection c, boolean ifNotExists) {
		if (c.external !== null) throw Unsupported.of('collections derived from external sources; ' + LancedbStatements.EXTERNAL)
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of('collection ttl; ' + LancedbStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of('auto-id; the generator keys rows by the primary key value the program writes (Lance has no generated keys)')
		if (c.deletionProtection) throw Unsupported.of('deletion-protection; a LanceDB table is a directory that drop_table removes')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; Lance tables hold no references to other tables')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + LancedbStatements.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of('storage settings; ' + LancedbStatements.STORAGE)
		if (!c.aliases.empty) throw Unsupported.of(LancedbStatements.NO_ALIASES)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + LancedbStatements.NO_FUNCTIONS)
		if (!c.limits.empty) throw Unsupported.of('collection limits (' + c.limits.head.type.literal + '); LanceDB OSS has no table limits')
		for (name : ModelUtil.extParams(c, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency', 'dynamic')
		for (analyzer : c.analyzers) tokenizer(analyzer)
		val known = if (ifNotExists) evolved.get(c) else null
		if (known === null) evolve(c, null)
		val table = known ?: declared(c)
		val args = kw('table', table.place, 'schema', table.schema)
		if (!table.indexes.empty) args.put('indexes', table.indexPlans)
		if (ifNotExists) args.put('exist_ok', flag(true))
		#[new Code.Run(runtime('create_table', args))]
	}

	/** The logical database of a collection declared in a logical-db (a sub-directory of the storage URI), else null. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) return null
		checkName(database.name)
		database.name
	}

	def static void checkName(String name) {
		if (!name.matches(NAME)) {
			throw Unsupported.of("the name '" + name + "'; LanceDB table and database names are letters, digits, underscores, hyphens and periods")
		}
	}

	// ---------------------------------------------------------------- fields

	def Entries fieldOf(VectorCollection c, Field field) {
		switch field {
			VectorField: vectorFieldOf(c, field)
			PayloadField: payloadFieldOf(field)
		}
	}

	def Entries vectorFieldOf(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) throw Unsupported.of('sparse vector field ' + name + '; ' + LancedbStatements.NO_SPARSE)
		if (field.type == EVectorDType.BFLOAT16_VECTOR) throw Unsupported.of('bfloat16_vector field ' + name + '; Arrow and Lance have no bfloat16 type (' + LANCE_SCHEMA + ')')
		if (field.type == EVectorDType.INT8_VECTOR || field.type == EVectorDType.UINT8_VECTOR) {
			throw Unsupported.of(field.type.literal + ' field ' + name + '; LanceDB compares a query vector of the column element type, and the SDKs send float32 queries (scanner.rs: "Column ... has element type")')
		}
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim; a Lance vector column is a fixed-size list')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) throw Unsupported.of('server-side embedding of ' + name + LancedbStatements.NO_MODELS)
		note(field, 'normalized', 'onDisk', 'modifier', 'limits')
		for (name2 : ModelUtil.extParams(field, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name2)
		val element = ELEMENTS.get(field.type)
		val binary = field.type == EVectorDType.BINARY_VECTOR
		if (binary && field.dimension % 8 != 0) throw Unsupported.of('binary_vector field ' + name + ' with a dim that is not a multiple of 8; Lance stores packed bits as uint8')
		val result = kw('name', text(name), 'type', text(if (field.multiVector) 'multivector' else 'vector'), 'element', text(element),
			'dim', num(if (binary) field.dimension / 8 else field.dimension))
		if (binary) result.put('bits', num(field.dimension))
		if (field.multiVector && metricName(field, ModelUtil.vectorIndex(c, field)) != 'cosine') {
			throw Unsupported.of('a multivector field with a metric other than cosine; LanceDB multivector search supports cosine only')
		}
		if (!field.nullable) result.put('nullable', flag(false))
		result
	}

	def Entries payloadFieldOf(PayloadField field) {
		val name = ModelUtil.fieldName(field)
		if (name.startsWith('_')) throw Unsupported.of('field ' + name + '; LanceDB reserves names starting with an underscore for _distance, _score, _rowid and _relevance_score')
		note(field, 'maxLength', 'limits')
		for (name2 : ModelUtil.extParams(field, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name2)
		val result = kw('name', text(name))
		if (!field.subFields.empty || field.type == EScalarDType.STRUCT) {
			if (field.type != EScalarDType.STRUCT && field.type != EScalarDType.JSON && field.type != EScalarDType.ARRAY) {
				throw Unsupported.of('sub-fields on a ' + field.type.literal + ' field ' + name)
			}
			if (field.subFields.empty) throw Unsupported.of('struct field ' + name + ' without sub-fields; an Arrow struct has declared children')
			val List<Entries> members = newArrayList
			for (sub : field.subFields) {
				if (!(sub instanceof PayloadField)) throw Unsupported.of('a vector field inside the struct ' + name)
				members += payloadFieldOf(sub as PayloadField)
			}
			val children = items(members)
			if (field.type == EScalarDType.ARRAY) {
				result.put('type', text('list'))
				result.put('element', kw('type', text('struct'), 'fields', children))
			} else {
				result.put('type', text('struct'))
				result.put('fields', children)
			}
		} else if (field.type == EScalarDType.ARRAY) {
			if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			result.put('type', text('list'))
			result.put('element', kw('type', text(scalar(field.elementType, name))))
		} else {
			result.put('type', text(scalar(field.type, name)))
		}
		if (field.description !== null) result.put('metadata', kw(DESCRIPTION, text(field.description)))
		result
	}

	def private String scalar(EScalarDType type, String name) {
		switch type {
			case JSON: throw Unsupported.of('json field ' + name + LancedbStatements.PENDING + ' (the lance.json Arrow extension type)')
			case MAP: throw Unsupported.of('map field ' + name + '; the Lance type system has no map type (' + LANCE_SCHEMA + ')')
			case GEO_POINT,
			case GEOMETRY: throw Unsupported.of(type.literal + ' field ' + name + LancedbStatements.PENDING + ' (GeoArrow types and the R-tree index)')
			case ARRAY,
			case STRUCT: throw Unsupported.of('nested ' + type.literal + ' elements in field ' + name)
			default: SCALARS.get(type)
		}
	}

	/** A field added by an alter: nullable, since existing rows have no value (a default is written by the runtime). */
	def Entries addedField(VectorCollection c, Field field) {
		val result = fieldOf(c, field)
		result.put('nullable', flag(true))
		result
	}

	// ---------------------------------------------------------------- metrics and indexes

	def EMetric metric(VectorField field, VectorIndex index) {
		if (field.type == EVectorDType.BINARY_VECTOR) EMetric.HAMMING
		else if (written(field, 'metric')) field.metric
		else if (index !== null && written(index, 'metric')) index.metric
		else EMetric.COSINE
	}

	def String metricName(VectorField field, VectorIndex index) {
		lance(metric(field, index))
	}

	/** LanceDB's distance types: l2 (squared Euclidean), cosine (1 - cos), dot (1 - x.y) and hamming. */
	def static String lance(EMetric metric) {
		switch metric {
			case COSINE: 'cosine'
			case DOT: 'dot'
			case EUCLIDEAN: 'l2'
			case HAMMING: 'hamming'
			default: throw Unsupported.of(metric.literal + ' metric; LanceDB measures l2, cosine, dot and hamming distances')
		}
	}

	/**
	 * A vector index plan: family and quantization select the Lance index type (IVF_FLAT, IVF_PQ, IVF_RQ, IVF_HNSW_SQ,
	 * IVF_HNSW_PQ); centroids is num_partitions, max-degree m, build-candidates ef_construction.
	 */
	def Entries vectorIndex(VectorIndex index, VectorField field, String metric) {
		if (field.type == EVectorDType.BINARY_VECTOR && index.family != EIndexFamily.IVF && index.family != EIndexFamily.FLAT) {
			throw Unsupported.of('a ' + index.family.literal + ' index on binary vectors; LanceDB indexes hamming distance with IVF_FLAT only')
		}
		if (written(index, 'metric') && LancedbSchema.lance(index.metric) != metric) {
			throw Unsupported.of('an index metric other than the metric of field ' + ModelUtil.fieldName(field) + '; a LanceDB query names one distance type')
		}
		if (index.accelerator == EAccelerator.GPU) throw Unsupported.of("the 'gpu' accelerator; GPU index training needs pylance with CUDA, which the TypeScript and Java SDKs do not offer")
		note(index, 'onDisk', 'online', 'searchCandidates')
		for (name : ModelUtil.extParams(index, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val quantization = index.quantization
		val kind = if (quantization === null || quantization.type == EQuantizationKind.NONE) {
			switch index.family {
				case AUTO,
				case HNSW: 'ivf_hnsw_sq'
				case FLAT,
				case IVF: 'ivf_flat'
				default: throw Unsupported.of(index.family.literal + ' indexes; LanceDB builds IVF partitions with flat, PQ, RQ or HNSW sub-indexes')
			}
		} else {
			quantizationKind(index, quantization.type)
		}
		if (quantization !== null) {
			note(quantization, 'rescore', 'oversampling', 'keepOriginals')
			if (written(quantization, 'truncateDimension')) throw Unsupported.of('truncate-dim; LanceDB indexes the full vector')
			if (written(quantization, 'bits') && !(kind == 'ivf_pq' || kind == 'ivf_hnsw_pq') && !(kind == 'ivf_hnsw_sq' && quantization.bits == 8)) {
				throw Unsupported.of('bits on ' + kind + '; LanceDB sets bits for product quantization only (scalar quantization is 8 bits)')
			}
			for (name : ModelUtil.extParams(quantization, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		}
		val result = kw('name', text(index.name), 'column', text(ModelUtil.fieldName(field)), 'kind', text(kind), 'metric', text(metric))
		if (written(index, 'centroids')) result.put('num_partitions', num(index.centroids))
		if (kind.startsWith('ivf_hnsw')) {
			if (written(index, 'maxDegree')) result.put('m', num(index.maxDegree))
			if (written(index, 'buildCandidates')) result.put('ef_construction', num(index.buildCandidates))
		} else if (written(index, 'maxDegree') || written(index, 'buildCandidates')) {
			throw Unsupported.of('max-degree and build-candidates on a ' + kind + ' index; they configure HNSW graphs')
		}
		if (quantization !== null && written(quantization, 'subvectors')) {
			if (!kind.endsWith('pq')) throw Unsupported.of('subvectors without product quantization')
			result.put('num_sub_vectors', num(quantization.subvectors))
		}
		if (quantization !== null && written(quantization, 'bits') && kind.endsWith('pq')) result.put('num_bits', num(quantization.bits))
		result
	}

	def private String quantizationKind(VectorIndex index, EQuantizationKind type) {
		val graph = index.family == EIndexFamily.HNSW || index.family == EIndexFamily.AUTO
		if (type == EQuantizationKind.SCALAR && !graph) {
			throw Unsupported.of('scalar quantization on an ivf index; the TypeScript SDK 0.41.0 builds no IVF_SQ index (Index.ivfFlat, ivfPq, ivfRq, hnswPq, hnswSq)')
		}
		if (type == EQuantizationKind.BINARY && graph) {
			throw Unsupported.of('binary quantization on an hnsw index; LanceDB quantizes IVF partitions with RaBitQ (IVF_RQ) only')
		}
		switch type {
			case PRODUCT: if (graph) 'ivf_hnsw_pq' else 'ivf_pq'
			case SCALAR: 'ivf_hnsw_sq'
			case BINARY: 'ivf_rq'
			default: throw Unsupported.of(type.literal + ' quantization')
		}
	}

	/**
	 * A payload index plan: BTREE for ordered and keyword values, BITMAP for low-cardinality values (structure bitmap or
	 * bool), LABEL_LIST for arrays, FTS (inverted, BM25) for full_text.
	 */
	def Entries payloadIndex(VectorCollection c, PayloadIndex index) {
		val path = index.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; Lance columns are declared')
		val column = if (path.subFields.empty) ModelUtil.fieldName(path.field) else ModelUtil.fieldName(path.field) + '.' + path.subFields.map[ModelUtil.fieldName(it)].join('.')
		val field = ModelUtil.leaf(path)
		if (!(field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val payload = field as PayloadField
		note(index, 'onDisk', 'castType', 'prefixMatch')
		for (name : ModelUtil.extParams(index, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		switch index.kind {
			case FULL_TEXT: {
				if (!#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains(payload.type)) throw Unsupported.of('a full_text index on ' + payload.type.literal + ' field ' + column)
				val plan = fts(index.name, column, index.analyzer ?: payload.analyzer)
				if (index.lowercase) plan.put('lower_case', flag(true))
				if (written(index, 'maxTokenLength')) plan.put('max_token_length', num(index.maxTokenLength))
				if (written(index, 'minTokenLength')) throw Unsupported.of('min-token-len; the LanceDB FTS index sets a maximum token length only')
				plan
			}
			case GEO: throw Unsupported.of('geo payload indexes' + LancedbStatements.PENDING + ' (GeoArrow types and the R-tree index)')
			case JSON: throw Unsupported.of('json payload indexes' + LancedbStatements.PENDING + ' (the lance.json Arrow extension type)')
			default: {
				val kind = if (payload.type == EScalarDType.ARRAY) 'label_list'
					else if (index.structure == EPayloadIndexStructure.BITMAP || index.structure == EPayloadIndexStructure.INVERTED || index.kind == EPayloadIndexKind.BOOL) 'bitmap'
					else 'btree'
				if (index.analyzer !== null || index.lowercase || written(index, 'minTokenLength') || written(index, 'maxTokenLength')) {
					throw Unsupported.of('text analysis on a ' + index.kind.literal + ' index; LanceDB analyzes text in FTS indexes only')
				}
				kw('name', text(index.name), 'column', text(column), 'kind', text(kind))
			}
		}
	}

	/**
	 * An FTS index plan with positions (phrase queries need them) and the analyzer's tokenizer, language and filters. An
	 * analyzer without filters keeps LanceDB's defaults (lower case, ASCII folding) and, with a language, stems and removes
	 * stop words; listed filters are taken exactly.
	 */
	def Entries fts(String name, String column, Analyzer analyzer) {
		val result = kw('name', text(name), 'column', text(column), 'kind', text('fts'), 'with_position', flag(true))
		if (analyzer === null) return result
		result.put('base_tokenizer', text(tokenizer(analyzer)))
		if (analyzer.language !== null) result.put('language', text(language(analyzer.language)))
		val filters = analyzer.filters.map[type].toSet
		val defaults = filters.empty
		val linguistic = analyzer.language !== null || analyzer.tokenizer == ETokenizer.LANGUAGE
		result.put('stem', flag(if (defaults) linguistic else filters.contains(EAnalyzerFilterType.STEMMER)))
		result.put('remove_stop_words', flag(if (defaults) linguistic || analyzer.stopwords !== null else filters.contains(EAnalyzerFilterType.STOP)))
		result.put('lower_case', flag(defaults || filters.contains(EAnalyzerFilterType.LOWERCASE)))
		result.put('ascii_folding', flag(defaults || filters.contains(EAnalyzerFilterType.ASCII_FOLDING)))
		result
	}

	def String tokenizer(Analyzer analyzer) {
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) throw Unsupported.of('bm25-k1 and bm25-b; LanceDB scores BM25 with fixed parameters')
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; LanceDB FTS has no file resources')
		if (analyzer.stopwords !== null && analyzer.stopwords != '_english_' && analyzer.stopwords != 'english') {
			throw Unsupported.of("the stop word list '" + analyzer.stopwords + "'; LanceDB removes the built-in list of the index language")
		}
		for (filter : analyzer.filters) {
			switch filter.type {
				case LOWERCASE,
				case STOP,
				case STEMMER,
				case ASCII_FOLDING: {}
				default: throw Unsupported.of(filter.type.literal + ' filters; LanceDB FTS filters are lower_case, stop words, stemming and ascii_folding')
			}
		}
		for (name : ModelUtil.extParams(analyzer, LancedbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		switch analyzer.tokenizer {
			case STANDARD,
			case LANGUAGE: 'simple'
			case WHITESPACE: 'whitespace'
			case KEYWORD: 'raw'
			case NGRAM: 'ngram'
			default: throw Unsupported.of(analyzer.tokenizer.literal + ' tokenizers; LanceDB FTS tokenizers are simple, whitespace, raw and ngram')
		}
	}

	/** The language names of LanceDB's stemmers and stop word lists (Tantivy's Language enum). */
	def private static String language(String code) {
		val names = #{'en' -> 'English', 'de' -> 'German', 'fr' -> 'French', 'es' -> 'Spanish', 'it' -> 'Italian', 'pt' -> 'Portuguese',
			'nl' -> 'Dutch', 'ru' -> 'Russian', 'sv' -> 'Swedish', 'da' -> 'Danish', 'fi' -> 'Finnish', 'no' -> 'Norwegian',
			'hu' -> 'Hungarian', 'ro' -> 'Romanian', 'tr' -> 'Turkish', 'ar' -> 'Arabic', 'el' -> 'Greek', 'ta' -> 'Tamil'}
		val name = names.get(code.toLowerCase) ?: names.values.findFirst[equalsIgnoreCase(code)]
		if (name === null) throw Unsupported.of("the language '" + code + "'; LanceDB stems " + names.values.sort.join(', '))
		name
	}

	def void note(EObject host, String... features) {
		for (feature : features) {
			if (written(host, feature)) notes += feature + ' on ' + host.eClass.name
		}
	}

	def static Entries kw(Object... pairs) {
		val result = entries
		for (var i = 0; i < pairs.length; i += 2) {
			result.put(pairs.get(i) as String, pairs.get(i + 1) as Code.Expr)
		}
		result
	}
}
