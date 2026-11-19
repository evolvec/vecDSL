package org.ftn.vecDSL.generator.pinecone

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Syntax
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EFieldLimit
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Pinecone document indexes (API 2026-07). The index schema declares only searched
 * fields: one dense vector, one sparse vector and full-text strings. Every other field is metadata, which
 * Pinecone indexes for filtering on first write. Document ids are strings, so the runtime derives the id from the
 * primary key and also stores the key as metadata under its own name, which keeps it typed and filterable.
 */
class PineconeSchema {

	public static val NO_PARTITIONS = 'partitions; Pinecone isolates data by namespace only (vecDSL tenants and namespaces are Pinecone namespaces)'
	static val TAG = '[A-Za-z0-9_-]{1,80}'
	/** Documents per upsert or update on an index that embeds a field (SPEC-D UpsertDocumentsRequest.documents). */
	static val EMBEDDED_BATCH = 96
	static val MAX_TAGS = 20

	static val INTEGRAL = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8]
	static val NUMERIC = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val TEXTUAL = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val LANGUAGES = #{'english' -> 'en', 'german' -> 'de', 'french' -> 'fr', 'spanish' -> 'es', 'italian' -> 'it',
		'portuguese' -> 'pt', 'dutch' -> 'nl', 'russian' -> 'ru', 'japanese' -> 'ja', 'korean' -> 'ko', 'chinese' -> 'zh'}

	val List<String> notes

	new(List<String> notes) {
		this.notes = notes
	}

	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, Entries deployment) {
		if (c.external !== null) throw Unsupported.of('collections derived from external sources')
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of('collection ttl')
		if (c.autoId) throw Unsupported.of('auto-id; Pinecone document ids are derived from the primary key')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref')
		if (!c.aliases.empty) throw Unsupported.of('aliases (Pinecone aliases name namespaces, not indexes)')
		if (!c.partitions.empty) throw Unsupported.of(NO_PARTITIONS)
		if (written(c, 'consistency')) consistency(c.consistency)
		note(c, 'dynamic', 'idType', 'description', 'storage', 'storedSourceFields', 'generativeModel', 'limits')
		val fields = indexFields(c)
		if (fields.entries.empty) {
			throw Unsupported.of('a collection without vector or full-text fields; a Pinecone index declares at least one searched field')
		}
		val args = kw('name', text(indexName(c)), 'fields', fields)
		if (!deployment.entries().empty) args.put('deployment', deployment)
		if (c.deletionProtection) args.put('deletion_protection', text('enabled'))
		if (!c.tags.empty) args.put('tags', tags(c.tags))
		val List<Stmt> created = newArrayList(new Code.Run(runtime('create_collection', args)) as Stmt)
		val List<String> slices = newArrayList
		slices += c.namespaces.map[namespace | namespaceName(namespace.key)]
		slices += c.tenants.map[tenant | namespaceName(tenant.name)]
		for (slice : slices.stream.distinct.toList) {
			created += new Code.Run(runtime('create_namespace', kw('name', text(indexName(c)), 'namespace', text(slice))))
		}
		if (!ifNotExists) return created
		newArrayList(new Code.When(runtime('has_collection', kw('name', text(indexName(c)))), true, created) as Stmt)
	}

	/** Index tags: Pinecone tags are key-value pairs, so each vecDSL tag becomes a key with the value "true". */
	def Entries tags(List<String> labels) {
		if (labels.size > MAX_TAGS) throw Unsupported.of('more than ' + MAX_TAGS + ' tags; a Pinecone index has at most ' + MAX_TAGS)
		val result = entries
		for (label : labels) {
			if (!label.matches(TAG)) {
				throw Unsupported.of("tag '" + label + "'; Pinecone tag keys are 1 to 80 letters, digits, '_' or '-'")
			}
			result.put(label, text('true'))
		}
		result
	}

	/** A Pinecone namespace name: 1 to 512 ASCII characters other than NUL. */
	def static String namespaceName(String name) {
		if (name === null || name.empty || name.length > 512 || name.chars.anyMatch[c | c == 0 || c > 127]) {
			throw Unsupported.of("namespace '" + name + "'; Pinecone namespace names are 1 to 512 ASCII characters")
		}
		name
	}

	/** Pinecone index and backup names: the lower-case words of a vecDSL name joined by '-', at most 45 characters. */
	def static String resourceName(String name, String what) {
		val result = String.join('-', Syntax.words(name))
		if (result.empty || result.length > 45 || !result.matches('[a-z0-9]([a-z0-9-]*[a-z0-9])?')) {
			throw Unsupported.of(what + " name '" + name + "'; Pinecone names are 1 to 45 lower-case letters, digits and '-'")
		}
		result
	}

	/** A metadata key: names starting with '_' or '$' are reserved. */
	def static String checkKey(String name) {
		if (name.startsWith('_') || name.startsWith('$')) {
			throw Unsupported.of("field name '" + name + "'; Pinecone reserves names that start with '_' or '$'")
		}
		name
	}

	/** A payload field added to an existing index: a metadata value of one of the Pinecone value types. */
	def void checkMetadataField(PayloadField field) {
		payloadType(field, false)
	}

	/** Collection identity every runtime call needs: index name, namespace and the primary key with its id form. */
	def Entries layout(VectorCollection c, String namespace) {
		kw('name', text(indexName(c)), 'namespace', text(namespace), 'key', text(ModelUtil.fieldName(c.primaryKey)),
			'key_type', text(keyType(c)))
	}

	/** The layout plus what writes need: the search clause that observes freshness, limits and default values. */
	def Entries writeLayout(VectorCollection c, String namespace) {
		val args = layout(c, namespace)
		args.put('probe', probe(c))
		val declared = limits(c)
		if (!declared.entries.empty) args.put('limits', declared)
		val defaults = entries
		for (field : ModelUtil.payloadFields(c).filter[defaultValue !== null]) {
			defaults.put(ModelUtil.fieldName(field), Literals.of(field.defaultValue))
		}
		if (!defaults.entries.empty) args.put('defaults', defaults)
		if (embeds(c)) args.put('max_batch', num(EMBEDDED_BATCH))
		args
	}

	/** Pinecone index name: lower-case words of the collection name joined by '-'. */
	def static String indexName(VectorCollection c) {
		resourceName(c.name, 'collection')
	}

	def static String keyType(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField)) throw Unsupported.of('a vector field as primary key')
		val type = (key as PayloadField).type
		if (INTEGRAL.contains(type)) return 'int'
		if (TEXTUAL.contains(type)) return 'text'
		throw Unsupported.of('primary key of type ' + type.literal)
	}

	/** Field name as a Pinecone document key; names starting with '_' or '$' are reserved. */
	def static String fieldKey(Field field) {
		checkKey(ModelUtil.fieldName(field))
	}

	/** The dense vector field, or null for a collection searched only by text. */
	def VectorField denseVector(VectorCollection c) {
		val dense = ModelUtil.vectorFields(c).filter[type != EVectorDType.SPARSE_FLOAT_VECTOR].toList
		if (dense.size > 1) {
			throw Unsupported.of('several dense vector fields; a Pinecone index has one dense vector field')
		}
		dense.head
	}

	/** The sparse vector field stored as such: one that no bm25 function produces. */
	def VectorField sparseVector(VectorCollection c) {
		val sparse = ModelUtil.vectorFields(c).filter[type == EVectorDType.SPARSE_FLOAT_VECTOR && !isBm25Output(c, it)].toList
		if (sparse.size > 1) {
			throw Unsupported.of('several sparse vector fields; a Pinecone index has one sparse vector field')
		}
		sparse.head
	}

	def static boolean isBm25Output(VectorCollection c, Field field) {
		val function = ModelUtil.producingFunction(c, field)
		function !== null && function.type == EFunctionType.BM25
	}

	/**
	 * Payload fields declared for full-text search: the schema's string fields with full_text_search, and the text fields of
	 * embedded vector fields, which Pinecone stores and indexes for full-text search (SPEC-C StringField.store_text).
	 */
	def List<PayloadField> fullTextFields(VectorCollection c) {
		val inputs = bm25Inputs(c)
		val embedded = ModelUtil.vectorFields(c).filter[vector | !isBm25Output(c, vector) && embeddingModel(c, vector) !== null]
			.map[vector | embeddedText(c, vector)].toList
		ModelUtil.payloadFields(c).filter[
			fullText || analyzer !== null || inputs.contains(it) || embedded.contains(it)
				|| c.indexes.filter(PayloadIndex).exists[index | index.target.field === it && index.kind == EPayloadIndexKind.FULL_TEXT]
		].toList
	}

	def boolean isFullText(VectorCollection c, Field field) {
		fullTextFields(c).contains(field)
	}

	def private Entries indexFields(VectorCollection c) {
		val result = entries
		val dense = denseVector(c)
		if (dense !== null) {
			if (embeddingModel(c, dense) === null) result.put(fieldKey(dense), denseField(c, dense))
			else embed(c, dense, result)
		}
		val sparse = sparseVector(c)
		if (sparse !== null) {
			note(sparse, 'normalized', 'onDisk', 'nullable')
			if (embeddingModel(c, sparse) === null) result.put(fieldKey(sparse), describe(kw('type', text('sparse_vector')), sparse))
			else embed(c, sparse, result)
		}
		val searched = fullTextFields(c)
		for (field : ModelUtil.payloadFields(c)) {
			payloadField(c, field, searched.contains(field))
			if (searched.contains(field)) merge(result, fieldKey(field), stringField(c, field))
		}
		result
	}

	def private Entries denseField(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		denseType(field)
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim; a Pinecone dense vector field declares its dimension')
		note(field, 'normalized', 'onDisk', 'nullable')
		val index = ModelUtil.vectorIndex(c, field)
		if (index !== null) vectorIndex(index)
		describe(kw('type', text('dense_vector'), 'dimension', num(field.dimension), 'metric', text(metricName(metric(field, index)))), field)
	}

	def private void denseType(VectorField field) {
		if (field.multiVector) throw Unsupported.of('multi-vector fields')
		if (field.type != EVectorDType.FLOAT_VECTOR) throw Unsupported.of(field.type.literal + ' fields; Pinecone stores float vectors')
	}

	/**
	 * The hosted model that embeds a vector field: the field's own model, or the model of the text_embedding
	 * (dense) or sparse_encoding (sparse) function that produces it; null for a field the program writes as vectors.
	 */
	def static EmbeddingModel embeddingModel(VectorCollection c, VectorField field) {
		if (field.model !== null) return field.model
		val function = ModelUtil.producingFunction(c, field)
		if (function === null || !#[EFunctionType.TEXT_EMBEDDING, EFunctionType.SPARSE_ENCODING].contains(function.type)) return null
		if ((function.type == EFunctionType.SPARSE_ENCODING) != (field.type == EVectorDType.SPARSE_FLOAT_VECTOR)) {
			throw Unsupported.of('a ' + function.type.literal + ' function producing ' + field.type.literal + ' field ' + ModelUtil.fieldName(field)
				+ '; Pinecone embeds dense vectors with embed and sparse vectors with sparse_embed')
		}
		if (function.model === null) {
			throw Unsupported.of('a ' + function.type.literal + ' function without a model; Pinecone integrated embedding runs a hosted model')
		}
		function.model
	}

	/** The field whose text Pinecone embeds into a vector field: its one source field, or the vector field itself. */
	def static Field embeddedText(VectorCollection c, VectorField field) {
		val List<Field> sources = if (field.model !== null) field.sourceFields else ModelUtil.producingFunction(c, field).inputs
		if (sources.size > 1) {
			throw Unsupported.of('embedding ' + sources.size + ' source fields into ' + ModelUtil.fieldName(field)
				+ '; Pinecone embeds the text of one string field')
		}
		sources.head ?: field
	}

	/** The name of a hosted model, checked against the provider: Pinecone integrated inference runs the models it hosts. */
	def static String hostedName(String provider, String model) {
		if (provider !== null && provider.toLowerCase != PineconeGenerator.VENDOR) {
			throw Unsupported.of('models of provider ' + provider + '; Pinecone integrated inference runs the models it hosts')
		}
		model
	}

	/** A string field with integrated embedding (SPEC-C CreateEmbedConfig, CreateSparseEmbedConfig): embed or sparse_embed. */
	def private void embed(VectorCollection c, VectorField field, Entries fields) {
		val model = embeddingModel(c, field)
		val source = embeddedText(c, field)
		if (source instanceof PayloadField && !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains((source as PayloadField).type)) {
			throw Unsupported.of('embedding ' + (source as PayloadField).type.literal + ' field ' + ModelUtil.fieldName(source)
				+ '; Pinecone embeds string fields')
		}
		if (source instanceof VectorField && source !== field) {
			throw Unsupported.of('embedding vector field ' + ModelUtil.fieldName(source) + '; Pinecone embeds string fields')
		}
		if (model.localPath !== null) throw Unsupported.of('local model files; Pinecone runs the models it hosts')
		val config = kw('model', text(hostedName(model.provider, model.modelId ?: model.name)))
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) {
			merge(fields, fieldKey(source), describe(kw('type', text('string'), 'sparse_embed', config), field))
			return
		}
		denseType(field)
		if (field.dimension > 0 && written(model, 'dimension') && model.dimension != field.dimension) {
			throw Unsupported.of('vector field ' + ModelUtil.fieldName(field) + ' with dim ' + field.dimension + ' and model '
				+ model.name + ' with dim ' + model.dimension)
		}
		if (field.dimension > 0) config.put('dimension', num(field.dimension))
		else if (written(model, 'dimension')) config.put('dimension', num(model.dimension))
		note(field, 'normalized', 'onDisk', 'nullable')
		val index = ModelUtil.vectorIndex(c, field)
		if (index !== null) vectorIndex(index)
		config.put('metric', text(metricName(metric(field, index))))
		merge(fields, fieldKey(source), describe(kw('type', text('string'), 'embed', config), field))
	}

	/** Adds a field configuration, combining it with one already declared under that name (a text field that is also embedded). */
	def private static void merge(Entries fields, String name, Entries config) {
		val present = fields.get(name)
		if (present instanceof Entries) {
			for (entry : config.entries.entrySet) (present as Entries).put(entry.key, entry.value)
		} else {
			fields.put(name, config)
		}
	}

	/** The key of the field a search scores: the embedded text field of a vector field with a model, else the vector field. */
	def static String searchKey(VectorCollection c, VectorField field) {
		if (embeddingModel(c, field) === null) fieldKey(field) else fieldKey(embeddedText(c, field))
	}

	/** The scoring of a vector field with a model: embed for dense fields, sparse_embed for sparse ones; null otherwise. */
	def static String embedScoring(VectorCollection c, VectorField field) {
		if (embeddingModel(c, field) === null) null
		else if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) 'sparse_embed'
		else 'embed'
	}

	/** True when any vector field of the collection is embedded by Pinecone, which limits write batches to 96 documents. */
	def boolean embeds(VectorCollection c) {
		#[denseVector(c), sparseVector(c)].filterNull.exists[field | embeddingModel(c, field) !== null]
	}

	def private void vectorIndex(VectorIndex index) {
		note(index, 'family', 'maxDegree', 'buildCandidates', 'searchCandidates', 'centroids', 'quantization',
			'accelerator', 'onDisk', 'online')
	}

	/** Checks a payload field against the value types of Pinecone metadata: numbers, strings, booleans, string lists. */
	def private void payloadField(VectorCollection c, PayloadField field, boolean searched) {
		payloadType(field, searched)
		for (index : c.indexes.filter(PayloadIndex).filter[target.field === field]) payloadIndex(index, field, searched)
		note(field, 'nested', 'documentBody')
	}

	def private void payloadType(PayloadField field, boolean searched) {
		val name = fieldKey(field)
		if (field.partitionKey) throw Unsupported.of(NO_PARTITIONS)
		if (!field.subFields.empty) throw Unsupported.of('sub-fields on ' + name + '; Pinecone metadata is flat')
		if (searched && !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains(field.type)) {
			throw Unsupported.of('full-text search on ' + field.type.literal + ' field ' + name)
		}
		switch field.type {
			case ARRAY: {
				if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
				if (!TEXTUAL.contains(field.elementType)) {
					throw Unsupported.of('arrays of ' + field.elementType.literal + '; Pinecone metadata lists hold strings')
				}
			}
			case BOOL: {}
			default: if (!NUMERIC.contains(field.type) && !TEXTUAL.contains(field.type)) {
				throw Unsupported.of(field.type.literal + ' field ' + name + '; Pinecone metadata holds numbers, strings, booleans and string lists')
			}
		}
	}

	/** Metadata is indexed for filtering automatically, so only full-text indexes change the index schema. */
	def void payloadIndex(PayloadIndex index, PayloadField field, boolean searched) {
		if (!index.target.subFields.empty || index.target.rawPath !== null) throw Unsupported.of('payload indexes on nested paths')
		note(index, 'structure', 'castType', 'onDisk', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		switch index.kind {
			case FULL_TEXT: {}
			case KEYWORD,
			case INTEGER,
			case FLOAT,
			case BOOL,
			case UUID:
				if (searched) {
					throw Unsupported.of('a ' + index.kind.literal + ' index on full-text field ' + ModelUtil.fieldName(field)
						+ '; Pinecone filters full-text fields by text match only')
				}
			default: throw Unsupported.of(index.kind.literal + ' payload indexes')
		}
	}

	def private Entries stringField(VectorCollection c, PayloadField field) {
		val search = entries
		val analyzer = field.analyzer ?: c.indexes.filter(PayloadIndex).filter[target.field === field].map[it.analyzer].filterNull.head
		if (analyzer !== null) analyze(analyzer, search)
		describe(kw('type', text('string'), 'full_text_search', search), field)
	}

	def private void analyze(Analyzer analyzer, Entries search) {
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources')
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) {
			throw Unsupported.of('bm25-k1 and bm25-b; Pinecone sets the BM25 parameters')
		}
		switch analyzer.tokenizer {
			case STANDARD,
			case LANGUAGE: {}
			default: throw Unsupported.of(analyzer.tokenizer.literal + ' tokenizer; Pinecone full-text search tokenizes words')
		}
		val filters = analyzer.filters.map[type].toSet
		for (filter : filters) {
			if (!#[EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STEMMER, EAnalyzerFilterType.STOP].contains(filter)) {
				throw Unsupported.of(filter.literal + ' token filters')
			}
		}
		if (filters.contains(EAnalyzerFilterType.LOWERCASE)) notes += 'lowercase filter on analyzer ' + analyzer.name
		val stop = filters.contains(EAnalyzerFilterType.STOP) || (analyzer.stopwords !== null && analyzer.stopwords.toLowerCase != 'none')
		if (stop && !filters.contains(EAnalyzerFilterType.STEMMER)) {
			throw Unsupported.of('stop words without a stemmer filter; Pinecone removes stop words only when it stems')
		}
		if (analyzer.language !== null) search.put('language', text(language(analyzer.language)))
		if (filters.contains(EAnalyzerFilterType.STEMMER)) search.put('stemming', flag(true))
		if (stop) {
			if (analyzer.stopwords !== null && analyzer.language !== null && language(analyzer.stopwords) != language(analyzer.language)) {
				throw Unsupported.of('stopwords "' + analyzer.stopwords + '" for language "' + analyzer.language + '"')
			}
			search.put('stop_words', flag(true))
		}
	}

	def private String language(String name) {
		val lower = name.toLowerCase
		LANGUAGES.getOrDefault(lower, lower)
	}

	def private Entries describe(Entries config, Field field) {
		if (field.description !== null) config.put('description', text(field.description))
		config
	}

	/** Text fields that feed a bm25 function; Pinecone scores them with BM25 through full-text search. */
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
				case TEXT_EMBEDDING,
				case SPARSE_ENCODING: {}
				default: throw Unsupported.of(function.type.literal + ' functions')
			}
		}
		inputs
	}

	/** A search clause the runtime sends to observe whether the index has caught up with a write. */
	def private Entries probe(VectorCollection c) {
		val dense = denseVector(c)
		if (dense !== null && embeddingModel(c, dense) === null) {
			return kw('type', text('dense_vector'), 'field', text(fieldKey(dense)), 'dim', num(dense.dimension))
		}
		if (dense !== null) return kw('type', text('text'), 'field', text(searchKey(c, dense)))
		val searched = fullTextFields(c)
		if (!searched.empty) return kw('type', text('text'), 'field', text(fieldKey(searched.head)))
		val sparse = sparseVector(c)
		if (sparse !== null && embeddingModel(c, sparse) !== null) return kw('type', text('text'), 'field', text(searchKey(c, sparse)))
		throw Unsupported.of('writes to a collection without a dense vector or full-text field')
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
		val dense = denseVector(c)
		if (dense !== null && embeddingModel(c, dense) === null) result.put(ModelUtil.fieldName(dense), kw('dim', num(dense.dimension)))
		result
	}

	def EMetric metric(VectorField field, VectorIndex index) {
		if (written(field, 'metric')) field.metric
		else if (index !== null && written(index, 'metric')) index.metric
		else EMetric.COSINE
	}

	def String metricName(EMetric metric) {
		switch metric {
			case COSINE: 'cosine'
			case DOT: 'dotproduct'
			case EUCLIDEAN: 'euclidean'
			default: throw Unsupported.of(metric.literal + ' metric; Pinecone scores by cosine, dotproduct or euclidean')
		}
	}

	/** Pinecone reads are eventually consistent and take no consistency level. */
	def void consistency(EConsistencyLevel level) {
		if (level != EConsistencyLevel.EVENTUAL) {
			throw Unsupported.of('consistency ' + level.literal + '; Pinecone reads are eventually consistent (use wait on writes)')
		}
	}

	def Map<String, Expr> extValues(EObject host) {
		val values = new LinkedHashMap<String, Expr>
		if (host !== null) {
			for (entry : ModelUtil.extParams(host, PineconeGenerator.VENDOR).entrySet) {
				values.put(entry.key, Literals.of(entry.value))
			}
		}
		values
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
