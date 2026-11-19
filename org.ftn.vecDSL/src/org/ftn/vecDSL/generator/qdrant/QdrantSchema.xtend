package org.ftn.vecDSL.generator.qdrant

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Cluster
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EFieldLimit
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EIndexFamily
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPartitionStrategy
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EPayloadIndexStructure
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.EStorageType
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.ExtParam
import org.ftn.vecDSL.vecDSL.ExtensionItem
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.FieldPath
import org.ftn.vecDSL.vecDSL.Partition
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.SegmentOptimizers
import org.ftn.vecDSL.vecDSL.ShardConfig
import org.ftn.vecDSL.vecDSL.StorageConfig
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex
import org.ftn.vecDSL.vecDSL.VendorExtension

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Qdrant collections (server 1.20). Every vector field becomes a named vector (dense or
 * sparse); payload fields are schemaless JSON, so the schema consists of the payload indexes: the declared ones, a
 * BM25-scored text index per full-text field and an index on the primary key. Point ids are UUIDv5 of the key's text
 * (the Weaviate convention), so the runtime also stores the key in the payload under its own name, typed and filterable.
 */
class QdrantSchema {

	/** Payload keys that hold tenant and namespace values when the collection declares no partition-key field. */
	public static val TENANT_KEY = 'vecdsl_tenant'
	public static val NAMESPACE_KEY = 'vecdsl_namespace'

	static val INTEGRAL = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8]
	static val NUMERIC = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val TEXTUAL = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val TEMPORAL = #[EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]
	static val STRUCTURED = #[EScalarDType.JSON, EScalarDType.STRUCT, EScalarDType.MAP]
	static val LANGUAGES = #{'en' -> 'english', 'de' -> 'german', 'fr' -> 'french', 'es' -> 'spanish', 'it' -> 'italian',
		'pt' -> 'portuguese', 'nl' -> 'dutch', 'ru' -> 'russian', 'da' -> 'danish', 'fi' -> 'finnish', 'sv' -> 'swedish',
		'no' -> 'norwegian', 'hu' -> 'hungarian', 'ro' -> 'romanian', 'tr' -> 'turkish', 'el' -> 'greek', 'ar' -> 'arabic'}
	static val STOPWORD_LANGUAGES = #{'arabic', 'azerbaijani', 'basque', 'bengali', 'catalan', 'chinese', 'danish', 'dutch',
		'english', 'finnish', 'french', 'german', 'greek', 'hebrew', 'hinglish', 'hungarian', 'indonesian', 'italian',
		'japanese', 'kazakh', 'nepali', 'norwegian', 'portuguese', 'romanian', 'russian', 'slovene', 'spanish', 'swedish',
		'tajik', 'turkish'}
	static val STEMMER_LANGUAGES = #{'arabic', 'armenian', 'danish', 'dutch', 'english', 'finnish', 'french', 'german',
		'greek', 'hungarian', 'italian', 'norwegian', 'portuguese', 'romanian', 'russian', 'spanish', 'swedish', 'tamil',
		'turkish'}
	static val OPTIMIZERS = #{'indexingThreshold' -> 'indexing_threshold', 'memmapThreshold' -> 'memmap_threshold',
		'defaultSegmentNumber' -> 'default_segment_number', 'maxSegmentSize' -> 'max_segment_size',
		'flushIntervalSec' -> 'flush_interval_sec'}

	val List<String> notes

	new(List<String> notes) {
		this.notes = notes
	}

	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists) {
		if (c.external !== null) throw Unsupported.of('collections derived from external sources')
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of('collection ttl; Qdrant points do not expire')
		if (c.autoId) throw Unsupported.of('auto-id; Qdrant point ids are derived from the primary key')
		if (c.deletionProtection) throw Unsupported.of('deletion-protection; Qdrant collections have no deletion guard')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model (Qdrant Cloud Inference is not generated)')
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'limits')
		if (written(c, 'consistency')) readConsistency(c.consistency)
		keyType(c)
		val name = text(c.name)
		val args = kw('name', name)
		val dense = entries
		val sparse = entries
		for (field : ModelUtil.vectorFields(c)) {
			if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) {
				if (isBm25Output(c, field)) notes += 'storage of ' + ModelUtil.fieldName(field) + ', which Qdrant computes as BM25 over the text index'
				else sparse.put(fieldKey(field), sparseVector(c, field))
			} else {
				dense.put(fieldKey(field), denseVector(c, field))
			}
		}
		if (!dense.entries.empty) args.put('vectors', dense)
		if (!sparse.entries.empty) args.put('sparse_vectors', sparse)
		args.put('indexes', items(payloadIndexes(c)))
		clusterSettings(c, args)
		if (c.storage !== null) args.put('payload', kw('memory', text(payloadMemory(c.storage))))
		val partitions = ModelUtil.partitions(c)
		if (!partitions.empty) {
			for (partition : partitions) shardKey(partition)
			args.put('sharding_method', text('custom'))
			args.put('shard_keys', texts(partitions.map[it.name]))
		}
		collectionExtensions(c, args)
		val metadata = entries
		if (!c.tags.empty) metadata.put('tags', texts(c.tags))
		if (c.description !== null) metadata.put('description', text(c.description))
		if (!metadata.entries.empty) args.put('metadata', metadata)
		if (!c.aliases.empty) args.put('aliases', texts(c.aliases.map[it.name]))
		val create = new Code.Run(runtime('create_collection', args)) as Stmt
		if (!ifNotExists) return #[create]
		newArrayList(new Code.When(runtime('has_collection', kw('name', name)), true, #[create]) as Stmt)
	}

	/** Collection identity every runtime call needs: name, primary key and key form, read consistency. */
	def Entries layout(VectorCollection c) {
		val args = kw('name', text(c.name), 'key', text(ModelUtil.fieldName(c.primaryKey)), 'key_type', text(keyType(c)))
		if (written(c, 'consistency') && readConsistency(c.consistency) !== null) args.put('read_consistency', text(readConsistency(c.consistency)))
		args
	}

	/** Replication and shard count of the cluster that holds the collection (by reference or as its member). */
	def private void clusterSettings(VectorCollection c, Entries args) {
		val cluster = c.cluster ?: EcoreUtil2.getContainerOfType(c, Cluster)
		if (cluster === null) return;
		if (cluster.replication !== null) args.put('replication_factor', num(cluster.replication.factor))
		if (cluster.sharding !== null) args.put('shard_number', num(shardNumber(cluster.sharding)))
	}

	def static int shardNumber(ShardConfig sharding) {
		if (sharding.key !== null || (written(sharding, 'strategy') && sharding.strategy != EPartitionStrategy.HASH)) {
			throw Unsupported.of('sharding by ' + (if (sharding.key !== null) 'a key field' else sharding.strategy.literal) + '; Qdrant distributes points over shards by hashing the point id, or by shard keys named in each write (custom sharding, see partitions)')
		}
		sharding.shards
	}

	/** Qdrant memory placement of the payload storage: memory pre-loads it into RAM, disk and mmap keep it cold. */
	def static String payloadMemory(StorageConfig storage) {
		if (storage.tiered || storage.location !== null || storage.type == EStorageType.OBJECT_STORAGE
			|| storage.type == EStorageType.EMBEDDED) {
			throw Unsupported.of('storage ' + (if (storage.tiered) 'tiering' else if (storage.location !== null) 'locations' else storage.type.literal) + '; Qdrant keeps collection data in its storage path and places it in RAM (cached, pinned) or leaves it on disk (cold)')
		}
		if (storage.type == EStorageType.MEMORY && !storage.mmap) 'cached' else 'cold'
	}

	/** A partition is a Qdrant shard key of a custom-sharded collection; writes name it, reads select it. */
	def static String shardKey(Partition partition) {
		if (written(partition, 'strategy') && #[EPartitionStrategy.HASH, EPartitionStrategy.RANGE].contains(partition.strategy)) {
			throw Unsupported.of('partition strategy ' + partition.strategy.literal + '; Qdrant routes a point to the shard key named in its write (custom sharding)')
		}
		partition.name
	}

	/** The payload key of tenant or namespace values: the partition-key field, else a reserved key. */
	def static String sliceKey(VectorCollection c, boolean namespace) {
		val key = ModelUtil.partitionKey(c)
		if (key !== null) return fieldKey(key)
		if (namespace) NAMESPACE_KEY else TENANT_KEY
	}

	/** The tenant index of a slice key: a keyword index with is_tenant, so Qdrant co-locates the points of each value. */
	def static Entries sliceIndex() {
		kw('type', text('keyword'), 'is_tenant', flag(true))
	}

	/** The layout plus what writes need: vector names, integer fields, declared limits, defaults and write ordering. */
	def Entries writeLayout(VectorCollection c) {
		val args = layout(c)
		val vectors = ModelUtil.vectorFields(c).filter[!isBm25Output(c, it)].map[fieldKey(it)].toList
		if (!vectors.empty) args.put('vectors', texts(vectors))
		val integers = ModelUtil.payloadFields(c).filter[INTEGRAL.contains(type)].map[ModelUtil.fieldName(it)].toList
		if (!integers.empty) args.put('integers', texts(integers))
		val declared = limits(c)
		if (!declared.entries.empty) args.put('limits', declared)
		val defaults = entries
		for (field : ModelUtil.payloadFields(c).filter[defaultValue !== null]) {
			defaults.put(ModelUtil.fieldName(field), Literals.of(field.defaultValue))
		}
		if (!defaults.entries.empty) args.put('defaults', defaults)
		if (written(c, 'consistency') && c.consistency == EConsistencyLevel.STRONG) args.put('ordering', text('strong'))
		args
	}

	def static String keyType(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField)) throw Unsupported.of('a vector field as primary key')
		val type = (key as PayloadField).type
		if (INTEGRAL.contains(type)) return 'int'
		if (TEXTUAL.contains(type)) return 'text'
		throw Unsupported.of('primary key of type ' + type.literal)
	}

	/** Field name as a Qdrant payload key or vector name; '.' and '[' would be read as a nested path. */
	def static String fieldKey(Field field) {
		val name = ModelUtil.fieldName(field)
		if (name.contains('.') || name.contains('[')) {
			throw Unsupported.of("field name '" + name + "'; Qdrant reads '.' and '[' in payload keys as nested paths")
		}
		name
	}

	def static boolean isBm25Output(VectorCollection c, Field field) {
		val function = ModelUtil.producingFunction(c, field)
		function !== null && function.type == EFunctionType.BM25
	}

	/** Payload fields searched by keyword: declared full-text, analyzed, BM25 inputs or under a full-text index. */
	def List<PayloadField> fullTextFields(VectorCollection c) {
		val inputs = bm25Inputs(c)
		ModelUtil.payloadFields(c).filter[
			fullText || analyzer !== null || inputs.contains(it)
				|| c.indexes.filter(PayloadIndex).exists[index | index.target.field === it && index.target.subFields.empty
					&& index.kind == EPayloadIndexKind.FULL_TEXT]
		].toList
	}

	def boolean isFullText(VectorCollection c, Field field) {
		fullTextFields(c).contains(field)
	}

	/** The analyzer of a full-text field: its own, or the analyzer of its full-text index. */
	def Analyzer analyzerOf(VectorCollection c, PayloadField field) {
		field.analyzer ?: c.indexes.filter(PayloadIndex).filter[target.field === field].map[it.analyzer].filterNull.head
	}

	/** Per-query BM25 parameters of a field, from its analyzer's bm25-k1 and bm25-b. */
	def Entries bm25Params(VectorCollection c, PayloadField field) {
		val analyzer = analyzerOf(c, field)
		if (analyzer === null) return null
		val result = entries
		if (written(analyzer, 'bm25K1')) result.put('k', num(analyzer.bm25K1))
		if (written(analyzer, 'bm25B')) result.put('b', num(analyzer.bm25B))
		if (result.entries.empty) null else result
	}

	/** Qdrant parameters of a named vector: VectorParams for dense fields, SparseVectorParams for sparse ones. */
	def Entries vectorParams(VectorCollection c, VectorField field) {
		if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) sparseVector(c, field) else denseVector(c, field)
	}

	def private Entries denseVector(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) {
			throw Unsupported.of('server-side embedding of ' + name + ' (Qdrant Cloud Inference is not generated)')
		}
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim; a Qdrant dense vector declares its size')
		val index = ModelUtil.vectorIndex(c, field)
		val params = kw('size', num(field.dimension), 'distance', text(distance(metric(field, index))))
		switch field.type {
			case FLOAT_VECTOR: {}
			case FLOAT16_VECTOR: params.put('datatype', text('float16'))
			case UINT8_VECTOR: params.put('datatype', text('uint8'))
			default: throw Unsupported.of(field.type.literal + ' fields; Qdrant stores float32, float16 and uint8 vectors')
		}
		if (field.multiVector) params.put('multivector_config', kw('comparator', text('max_sim')))
		if (field.onDisk) params.put('memory', text('cold'))
		note(field, 'normalized')
		if (index !== null) vectorIndex(index, params)
		params
	}

	def private Entries sparseVector(VectorCollection c, VectorField field) {
		val params = kw()
		if (written(field, 'modifier')) params.put('modifier', text(field.modifier.literal))
		note(field, 'normalized', 'onDisk')
		val index = ModelUtil.vectorIndex(c, field)
		if (index !== null) {
			if (index.family != EIndexFamily.SPARSE_INVERTED && index.family != EIndexFamily.AUTO) {
				throw Unsupported.of(index.family.literal + ' index on sparse field ' + ModelUtil.fieldName(field) + '; Qdrant indexes sparse vectors with an inverted index')
			}
			note(index, 'maxDegree', 'buildCandidates', 'searchCandidates', 'centroids', 'accelerator', 'onDisk', 'online', 'quantization')
		}
		params
	}

	def void vectorIndex(VectorIndex index, Entries params) {
		val hnsw = entries
		switch index.family {
			case AUTO,
			case HNSW: {
				if (written(index, 'maxDegree')) hnsw.put('m', num(index.maxDegree))
				if (written(index, 'buildCandidates')) hnsw.put('ef_construct', num(index.buildCandidates))
			}
			case FLAT: hnsw.put('m', num(0))
			default: throw Unsupported.of(index.family.literal + ' indexes; Qdrant builds HNSW graphs (family flat disables the graph)')
		}
		if (index.onDisk) hnsw.put('memory', text('cold'))
		if (!hnsw.entries.empty) params.put('hnsw_config', hnsw)
		note(index, 'searchCandidates', 'centroids', 'accelerator', 'online')
		if (index.quantization !== null) {
			val config = quantizationConfig(index.quantization)
			if (config !== null) params.put('quantization_config', config)
		}
		for (name : ModelUtil.extParams(index, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	def Entries quantizationConfig(Quantization q) {
		if (written(q, 'truncateDimension')) throw Unsupported.of('truncate-dim; Qdrant quantizes the full vector')
		note(q, 'rescore', 'oversampling', 'keepOriginals')
		switch q.type {
			case NONE: null
			case SCALAR: {
				if (written(q, 'bits') && q.bits != 8) throw Unsupported.of('scalar quantization with ' + q.bits + ' bits; Qdrant scalar quantization is int8')
				kw('scalar', kw('type', text('int8')))
			}
			case BINARY: {
				if (!written(q, 'bits') || q.bits == 1) return kw('binary', kw())
				if (q.bits == 1.5) return kw('binary', kw('encoding', text('one_and_half_bits')))
				if (q.bits == 2) return kw('binary', kw('encoding', text('two_bits')))
				throw Unsupported.of('binary quantization with ' + q.bits + ' bits; Qdrant encodes 1, 1.5 or 2 bits')
			}
			case PRODUCT: throw Unsupported.of('product quantization with sub-vectors; Qdrant sets product quantization by compression ratio')
		}
	}

	/** Payload indexes in creation order: the primary key, then each field's declared or implied index. */
	def private List<Expr> payloadIndexes(VectorCollection c) {
		val Map<String, Expr> result = new LinkedHashMap
		val declared = c.indexes.filter(PayloadIndex).toList
		val key = c.primaryKey as PayloadField
		if (!declared.exists[target.field === key && target.subFields.empty]) {
			result.put(ModelUtil.fieldName(key), indexEntry(ModelUtil.fieldName(key), kw('type', text(if (keyType(c) == 'int') 'integer' else 'keyword'))))
		}
		val List<String> slices = newArrayList
		if (ModelUtil.partitionKey(c) !== null || !c.tenants.empty) slices += sliceKey(c, false)
		if (!c.namespaces.empty && !slices.contains(sliceKey(c, true))) slices += sliceKey(c, true)
		for (slice : slices) {
			if (declared.exists[target.field !== null && target.subFields.empty && fieldKey(target.field) == slice]) {
				throw Unsupported.of('a payload index on partition key ' + slice + '; Qdrant gives a tenant key its own tenant index')
			}
			result.put(slice, indexEntry(slice, sliceIndex))
		}
		val searched = fullTextFields(c)
		for (field : ModelUtil.payloadFields(c)) {
			checkPayloadField(field, searched.contains(field))
			val own = declared.filter[target.field === field && target.subFields.empty && target.rawPath === null].toList
			if (searched.contains(field) && !own.exists[kind == EPayloadIndexKind.FULL_TEXT]) {
				if (!own.empty) throw Unsupported.of('a ' + own.head.kind.literal + ' index on full-text field ' + ModelUtil.fieldName(field) + '; a Qdrant payload field has one index')
				result.put(fieldKey(field), indexEntry(fieldKey(field), textIndex(c, field, null)))
			}
		}
		for (index : declared) {
			val path = indexPath(index.target)
			if (result.containsKey(path)) throw Unsupported.of('two payload indexes on ' + path + '; a Qdrant payload field has one index')
			result.put(path, indexEntry(path, payloadIndex(c, index)))
		}
		newArrayList(result.values)
	}

	def private Entries indexEntry(String path, Entries schema) {
		kw('field', text(path), 'schema', schema)
	}

	/** Index path of a field path: sub-fields joined by '.', array-of-object segments marked with '[]'. */
	def static String indexPath(FieldPath path) {
		if (path.rawPath !== null) return path.rawPath
		val segments = newArrayList(path.field)
		segments += path.subFields
		val List<String> names = newArrayList
		for (var i = 0; i < segments.size; i++) {
			val segment = segments.get(i)
			val array = segment instanceof PayloadField && (segment as PayloadField).type == EScalarDType.ARRAY
			names += fieldKey(segment) + (if (array && i < segments.size - 1) '[]' else '')
		}
		String.join('.', names)
	}

	/** The index schema of a declared payload index (create collection or create payload-index). */
	def Entries payloadIndex(VectorCollection c, PayloadIndex index) {
		note(index, 'castType')
		if (written(index, 'structure') && index.structure != EPayloadIndexStructure.AUTO) notes += 'structure on PayloadIndex'
		val field = ModelUtil.leaf(index.target)
		val schema = switch index.kind {
			case KEYWORD: {
				val keyword = kw('type', text('keyword'))
				if (index.prefixMatch) keyword.put('prefix', flag(true))
				keyword
			}
			case FULL_TEXT: {
				if (field !== null && !(field instanceof PayloadField && TEXTUAL.contains((field as PayloadField).type))) {
					throw Unsupported.of('a full_text index on ' + ModelUtil.fieldName(field) + ', which is not a text field')
				}
				textIndex(c, if (field instanceof PayloadField) field as PayloadField else null, index)
			}
			case INTEGER: kw('type', text('integer'))
			case FLOAT: kw('type', text('float'))
			case DATETIME: kw('type', text('datetime'))
			case BOOL: kw('type', text('bool'))
			case UUID: kw('type', text('uuid'))
			case GEO: kw('type', text('geo'))
			case JSON: throw Unsupported.of('json payload indexes; Qdrant indexes typed paths (keyword, integer, float, ...)')
		}
		if (index.kind != EPayloadIndexKind.FULL_TEXT) note(index, 'lowercase', 'minTokenLength', 'maxTokenLength', 'analyzer')
		if (index.kind != EPayloadIndexKind.KEYWORD) note(index, 'prefixMatch')
		if (index.onDisk) schema.put('memory', text('cold'))
		for (name : ModelUtil.extParams(index, QdrantGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		schema
	}

	/** A text index scored by BM25, with the tokenizer, token filters and stop words of the field's analyzer. */
	def Entries textIndex(VectorCollection c, PayloadField field, PayloadIndex index) {
		val params = kw('type', text('text'), 'tokenizer', text('word'))
		val analyzer = if (index !== null && index.analyzer !== null) index.analyzer else if (field !== null) analyzerOf(c, field)
		if (analyzer !== null) analyze(analyzer, params)
		if (index !== null) {
			if (index.lowercase) params.put('lowercase', flag(true))
			if (written(index, 'minTokenLength')) params.put('min_token_len', num(index.minTokenLength))
			if (written(index, 'maxTokenLength')) params.put('max_token_len', num(index.maxTokenLength))
		}
		params.put('phrase_matching', flag(true))
		params.put('scoring', kw('type', text('bm25')))
		params
	}

	def void analyze(Analyzer analyzer, Entries params) {
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources')
		switch analyzer.tokenizer {
			case STANDARD: {}
			case WHITESPACE: params.put('tokenizer', text('whitespace'))
			case EDGE_NGRAM: params.put('tokenizer', text('prefix'))
			case ICU,
			case LANGUAGE: params.put('tokenizer', text('multilingual'))
			default: throw Unsupported.of(analyzer.tokenizer.literal + ' tokenizer; Qdrant tokenizes by word, whitespace, prefix, multilingual or code')
		}
		val filters = analyzer.filters.map[type].toSet
		for (filter : filters) {
			if (!#[EAnalyzerFilterType.LOWERCASE, EAnalyzerFilterType.STOP, EAnalyzerFilterType.STEMMER, EAnalyzerFilterType.ASCII_FOLDING].contains(filter)) {
				throw Unsupported.of(filter.literal + ' token filters')
			}
		}
		if (filters.contains(EAnalyzerFilterType.LOWERCASE)) params.put('lowercase', flag(true))
		if (filters.contains(EAnalyzerFilterType.ASCII_FOLDING)) params.put('ascii_folding', flag(true))
		val stop = filters.contains(EAnalyzerFilterType.STOP) || (analyzer.stopwords !== null && analyzer.stopwords.toLowerCase != 'none')
		if (stop) {
			val language = languageName(analyzer.stopwords ?: analyzer.language, 'stop words')
			if (!STOPWORD_LANGUAGES.contains(language)) throw Unsupported.of('stop words for "' + language + '"; Qdrant has no such stop word list')
			params.put('stopwords', text(language))
		}
		if (filters.contains(EAnalyzerFilterType.STEMMER)) {
			val language = languageName(analyzer.language, 'a stemmer')
			if (!STEMMER_LANGUAGES.contains(language)) throw Unsupported.of('stemming for "' + language + '"; Qdrant has no such Snowball stemmer')
			params.put('stemmer', kw('type', text('snowball'), 'language', text(language)))
		}
		if (analyzer.language !== null && !stop && !filters.contains(EAnalyzerFilterType.STEMMER)) notes += 'language on Analyzer ' + analyzer.name
	}

	def private String languageName(String name, String purpose) {
		if (name === null) throw Unsupported.of(purpose + ' without a language')
		val lower = name.toLowerCase
		LANGUAGES.getOrDefault(lower, lower)
	}

	/** Checks a payload field against the JSON values Qdrant payloads hold. */
	def void checkPayloadField(PayloadField field, boolean searched) {
		val name = fieldKey(field)
		if (field.partitionKey && !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains(field.type)) {
			throw Unsupported.of('partition key ' + name + ' of type ' + field.type.literal + '; Qdrant tenant indexes are keyword indexes, and tenant names are text')
		}
		if (searched && !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains(field.type)) {
			throw Unsupported.of('full-text search on ' + field.type.literal + ' field ' + name)
		}
		val type = if (field.type == EScalarDType.ARRAY && written(field, 'elementType')) field.elementType else field.type
		if (field.type == EScalarDType.ARRAY && !written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
		if (!NUMERIC.contains(type) && !TEXTUAL.contains(type) && !TEMPORAL.contains(type) && !STRUCTURED.contains(type)
			&& type != EScalarDType.BOOL && type != EScalarDType.GEO_POINT) {
			throw Unsupported.of(type.literal + ' field ' + name + '; Qdrant payloads hold JSON numbers, strings, booleans, objects and arrays')
		}
		for (sub : field.subFields) {
			if (sub instanceof PayloadField) checkPayloadField(sub as PayloadField, false) else throw Unsupported.of('vector sub-field ' + ModelUtil.fieldName(sub))
		}
		note(field, 'nested', 'documentBody')
	}

	/** Text fields that feed a bm25 function; Qdrant scores them with BM25 through their text index. */
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
				case TEXT_EMBEDDING: throw Unsupported.of('text-embedding functions (Qdrant Cloud Inference is not generated)')
				default: throw Unsupported.of(function.type.literal + ' functions')
			}
		}
		inputs
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
		result
	}

	/** Collection extensions: strict_mode (ExtParam) and the segment optimizer settings. */
	def private void collectionExtensions(VectorCollection c, Entries args) {
		for (entry : ModelUtil.extParams(c, QdrantGenerator.VENDOR).entrySet) {
			switch entry.key {
				case 'strict_mode': args.put('strict_mode', Literals.of(entry.value))
				default: throw Unsupported.of('extension parameter ' + entry.key)
			}
		}
		for (optimizers : extensionItems(c).filter(SegmentOptimizers)) {
			val config = entries
			for (feature : OPTIMIZERS.entrySet) {
				if (written(optimizers, feature.key)) config.put(feature.value, num((optimizers.eGet(optimizers.eClass.getEStructuralFeature(feature.key)) as Integer).doubleValue))
			}
			args.put('optimizers', config)
		}
		for (item : extensionItems(c).filter[!(it instanceof SegmentOptimizers) && !(it instanceof ExtParam)]) {
			throw Unsupported.of(item.eClass.name + ' on a collection')
		}
	}

	/** Items of the host's qdrant extension blocks. */
	def static List<ExtensionItem> extensionItems(EObject host) {
		val feature = host.eClass.getEStructuralFeature('extensions')
		val List<ExtensionItem> result = newArrayList
		if (feature === null) return result
		for (block : (host.eGet(feature) as List<?>).filter(VendorExtension)) {
			if (block.getVendor == QdrantGenerator.VENDOR) result.addAll(block.getItems)
		}
		result
	}

	def EMetric metric(VectorField field, VectorIndex index) {
		if (written(field, 'metric')) field.metric
		else if (index !== null && written(index, 'metric')) index.metric
		else EMetric.COSINE
	}

	def String distance(EMetric metric) {
		switch metric {
			case COSINE: 'Cosine'
			case DOT: 'Dot'
			case EUCLIDEAN: 'Euclid'
			case MANHATTAN: 'Manhattan'
			default: throw Unsupported.of(metric.literal + ' metric; Qdrant scores by Cosine, Dot, Euclid or Manhattan')
		}
	}

	/** Qdrant read consistency of a vecDSL level: strong reads every replica; eventual is the default. */
	def String readConsistency(EConsistencyLevel level) {
		switch level {
			case STRONG: 'all'
			case EVENTUAL: null
			default: throw Unsupported.of('consistency ' + level.literal + '; Qdrant reads at a replica count (one, majority, quorum or all)')
		}
	}

	def Map<String, Expr> extValues(EObject host) {
		val values = new LinkedHashMap<String, Expr>
		if (host !== null) {
			for (entry : ModelUtil.extParams(host, QdrantGenerator.VENDOR).entrySet) {
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
