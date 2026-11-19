package org.ftn.vecDSL.generator.milvus

import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Expr
import org.ftn.vecDSL.generator.code.Code.Make
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.AnalyzerFilter
import org.ftn.vecDSL.vecDSL.EAccelerator
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType
import org.ftn.vecDSL.vecDSL.EConsistencyLevel
import org.ftn.vecDSL.vecDSL.EFieldLimit
import org.ftn.vecDSL.vecDSL.EFunctionType
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EQuantizationKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ETokenizer
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.EmbeddingModel
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.Function
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/** Maps vecDSL collections, fields, functions and indexes to Milvus schema, function and index definitions. */
class MilvusSchema {

	static val COLLECTION_ARGUMENTS = #{'num_shards', 'num_partitions'}

	val List<String> notes

	new(List<String> notes) {
		this.notes = notes
	}

	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists) {
		if (c.external !== null) throw Unsupported.of('the derived-from clause has no Milvus mapping yet')
		if (c.ttlField !== null) throw Unsupported.of('ttl-field has no Milvus mapping yet')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; Milvus collections hold no references to other collections')
		note(c, 'tags', 'deletionProtection', 'storage', 'storedSourceFields', 'embeddingModel', 'generativeModel', 'limits')
		val List<Stmt> statements = newArrayList
		statements += new Code.Run(client('create_collection', collectionArgs(c)))
		for (partition : manualPartitions(c)) {
			statements += new Code.Run(client('create_partition', kw('collection_name', text(c.name), 'partition_name', text(partition))))
		}
		for (alias : c.aliases) {
			statements += new Code.Run(client('create_alias', kw('collection_name', text(c.name), 'alias', text(alias.name))))
		}
		if (!ifNotExists) return statements
		newArrayList(new Code.When(runtime('has_collection', kw('collection_name', text(c.name))), true, statements) as Stmt)
	}

	def Make schema(VectorCollection c) {
		val args = entries
		if (c.autoId) args.put('auto_id', flag(true))
		if (c.dynamic) args.put('enable_dynamic_field', flag(true))
		if (c.description !== null) args.put('description', text(c.description))
		args.put('fields', items(c.fields.map[make('Field', fieldArgs(c, it))]))
		val functions = functions(c)
		if (!functions.empty) args.put('functions', items(functions))
		make('Schema', args)
	}

	def Entries fieldArgs(VectorCollection c, Field field) {
		val args = kw('field_name', text(ModelUtil.fieldName(field)))
		switch field {
			VectorField: vectorFieldArgs(field, args)
			PayloadField: payloadFieldArgs(c, field, args)
		}
		args.entries.putAll(extValues(field))
		args
	}

	def private void vectorFieldArgs(VectorField field, Entries args) {
		if (field.multiVector) throw Unsupported.of('multi-vector fields')
		args.put('datatype', dataType(vectorType(field.type)))
		if (field.type != EVectorDType.SPARSE_FLOAT_VECTOR) {
			if (field.dimension <= 0) throw Unsupported.of('vector field ' + ModelUtil.fieldName(field) + ' without dim')
			args.put('dim', num(field.dimension))
		}
		if (field.nullable) args.put('nullable', flag(true))
		if (field.description !== null) args.put('description', text(field.description))
		note(field, 'onDisk', 'limits')
	}

	def private void payloadFieldArgs(VectorCollection c, PayloadField field, Entries args) {
		val type = scalarType(field.type)
		args.put('datatype', dataType(type))
		if (field === c.primaryKey) {
			if (type != 'INT64' && type != 'VARCHAR') throw Unsupported.of('primary key of type ' + field.type.literal)
			args.put('is_primary', flag(true))
		}
		if (type == 'VARCHAR') args.put('max_length', num(maxLength(field)))
		if (type == 'ARRAY') {
			if (!written(field, 'elementType')) throw Unsupported.of('array field ' + field.name + ' without element-type')
			val element = scalarType(field.elementType)
			if (#['ARRAY', 'JSON', 'GEOMETRY'].contains(element)) throw Unsupported.of('arrays of ' + field.elementType.literal)
			args.put('element_type', dataType(element))
			args.put('max_capacity', num(limit(field, EFieldLimit.ARRAY_LEN, 4096)))
			if (element == 'VARCHAR') args.put('max_length', num(maxLength(field)))
		}
		if (!field.subFields.empty && type != 'JSON') throw Unsupported.of('sub-fields on ' + field.type.literal + ' fields')
		if (field.nullable) args.put('nullable', flag(true))
		if (field.defaultValue !== null) args.put('default_value', Literals.of(field.defaultValue))
		if (field.partitionKey || ModelUtil.partitionKey(c) === field) args.put('is_partition_key', flag(true))
		val bm25Input = c.functions.exists[it.type == EFunctionType.BM25 && inputs.contains(field)]
		if (field.fullText || field.analyzer !== null || bm25Input) {
			args.put('enable_analyzer', flag(true))
			if (field.analyzer !== null) args.put('analyzer_params', analyzerParams(field.analyzer))
		}
		if (field.fullText) args.put('enable_match', flag(true))
		if (field.description !== null) args.put('description', text(field.description))
		note(field, 'nested', 'documentBody')
	}

	def private int maxLength(PayloadField field) {
		if (written(field, 'maxLength')) return field.maxLength
		if (field.type == EScalarDType.UUID) return 36
		limit(field, EFieldLimit.MAX_SIZE, 65535)
	}

	def private int limit(PayloadField field, EFieldLimit kind, int fallback) {
		val declared = field.limits.findFirst[type == kind]
		if (declared === null) fallback else declared.value
	}

	def String scalarType(EScalarDType type) {
		switch type {
			case INT: 'INT64'
			case BIGINT: 'INT64'
			case INT8: 'INT8'
			case FLOAT: 'FLOAT'
			case DOUBLE: 'DOUBLE'
			case BOOL: 'BOOL'
			case VARCHAR: 'VARCHAR'
			case TEXT: 'VARCHAR'
			case UUID: 'VARCHAR'
			case DATE: 'TIMESTAMPTZ'
			case DATETIME: 'TIMESTAMPTZ'
			case TIMESTAMPTZ: 'TIMESTAMPTZ'
			case JSON: 'JSON'
			case MAP: 'JSON'
			case STRUCT: 'JSON'
			case ARRAY: 'ARRAY'
			case GEO_POINT: 'GEOMETRY'
			case GEOMETRY: 'GEOMETRY'
			default: throw Unsupported.of(type.literal + ' fields')
		}
	}

	def String vectorType(EVectorDType type) {
		switch type {
			case FLOAT_VECTOR: 'FLOAT_VECTOR'
			case FLOAT16_VECTOR: 'FLOAT16_VECTOR'
			case BFLOAT16_VECTOR: 'BFLOAT16_VECTOR'
			case INT8_VECTOR: 'INT8_VECTOR'
			case BINARY_VECTOR: 'BINARY_VECTOR'
			case SPARSE_FLOAT_VECTOR: 'SPARSE_FLOAT_VECTOR'
			default: throw Unsupported.of(type.literal + ' fields')
		}
	}

	def private Expr dataType(String name) {
		new Code.Sym('DataType', name)
	}

	def List<Make> functions(VectorCollection c) {
		val List<Make> result = newArrayList
		result += c.functions.map[function(it)]
		for (field : ModelUtil.vectorFields(c).filter[model !== null && !sourceFields.empty]) {
			if (ModelUtil.producingFunction(c, field) === null) {
				if (field.sourceFields.size > 1) throw Unsupported.of('embedding a vector field from several fields')
				result += embedding(ModelUtil.fieldName(field) + '_embedding', field.sourceFields, #[field], field.model, field)
			}
		}
		result
	}

	def Make function(Function function) {
		switch function.type {
			case BM25: make('Function', kw(
				'name', text(function.name),
				'function_type', new Code.Sym('FunctionType', 'BM25'),
				'input_field_names', texts(function.inputs.map[ModelUtil.fieldName(it)]),
				'output_field_names', texts(function.outputs.map[ModelUtil.fieldName(it)])
			))
			case TEXT_EMBEDDING: embedding(function.name, function.inputs, function.outputs, function.model, function)
			default: throw Unsupported.of(function.type.literal + ' functions')
		}
	}

	def private Make embedding(String name, List<Field> inputs, List<? extends Field> outputs, EmbeddingModel model, EObject host) {
		if (model === null || model.provider === null || model.modelId === null) {
			throw Unsupported.of('text embedding without an embedding model with provider and model-id')
		}
		val params = kw('provider', text(model.provider), 'model_name', text(model.modelId))
		params.entries.putAll(extValues(model))
		params.entries.putAll(extValues(host))
		make('Function', kw(
			'name', text(name),
			'function_type', new Code.Sym('FunctionType', 'TEXTEMBEDDING'),
			'input_field_names', texts(inputs.map[ModelUtil.fieldName(it)]),
			'output_field_names', texts(outputs.map[ModelUtil.fieldName(it)]),
			'params', params
		))
	}

	def Entries analyzerParams(Analyzer analyzer) {
		val result = entries
		if (analyzer.tokenizer == ETokenizer.LANGUAGE) {
			result.put('type', text(language(analyzer.language)))
		} else {
			result.put('tokenizer', text(tokenizer(analyzer.tokenizer)))
		}
		val List<Expr> filters = newArrayList
		filters += analyzer.filters.map[filter(it, analyzer)]
		if (analyzer.stopwords !== null && !analyzer.filters.exists[type == EAnalyzerFilterType.STOP]) {
			filters += stopFilter(analyzer)
		}
		if (!filters.empty) result.put('filter', items(filters))
		result.entries.putAll(extValues(analyzer))
		result
	}

	def private String tokenizer(ETokenizer tokenizer) {
		switch tokenizer {
			case STANDARD: 'standard'
			case WHITESPACE: 'whitespace'
			case ICU: 'icu'
			default: throw Unsupported.of(tokenizer.literal + ' tokenizer')
		}
	}

	def private String language(String language) {
		switch (language ?: 'english').toLowerCase {
			case 'en': 'english'
			case 'english': 'english'
			case 'zh': 'chinese'
			case 'chinese': 'chinese'
			default: throw Unsupported.of('language analyzer ' + language)
		}
	}

	def private Expr filter(AnalyzerFilter filter, Analyzer analyzer) {
		switch filter.type {
			case LOWERCASE: text('lowercase')
			case ASCII_FOLDING: text('asciifolding')
			case STOP: stopFilter(analyzer)
			case STEMMER: kw('type', text('stemmer'), 'language', text(language(analyzer.language)))
			default: throw Unsupported.of(filter.type.literal + ' token filter')
		}
	}

	def private Expr stopFilter(Analyzer analyzer) {
		val words = analyzer.stopwords ?: 'english'
		kw('type', text('stop'), 'stop_words', texts(#[if (words.startsWith('_')) words else '_' + words + '_']))
	}

	def Make vectorIndex(VectorCollection c, VectorField field, VectorIndex index) {
		val type = indexType(field, index)
		val args = kw('field_name', text(ModelUtil.fieldName(field)))
		if (index !== null) args.put('index_name', text(index.name))
		args.put('index_type', text(type))
		args.put('metric_type', text(metric(c, field, index)))
		val params = indexParams(c, field, index, type)
		if (!params.empty) args.put('params', entries(params))
		make('Index', args)
	}

	def String indexType(VectorField field, VectorIndex index) {
		val override = extString(index, 'index_type')
		if (override !== null) return override
		if (index === null) {
			return if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) 'SPARSE_INVERTED_INDEX' else 'AUTOINDEX'
		}
		val quantization = if (index.quantization === null) EQuantizationKind.NONE else index.quantization.type
		val gpu = index.accelerator == EAccelerator.GPU
		val binary = field.type == EVectorDType.BINARY_VECTOR
		switch index.family {
			case AUTO: 'AUTOINDEX'
			case FLAT: if (gpu) 'GPU_BRUTE_FORCE' else if (binary) 'BIN_FLAT' else 'FLAT'
			case HNSW: if (gpu) 'GPU_CAGRA' else hnsw(quantization)
			case IVF: if (binary) 'BIN_IVF_FLAT' else ivf(quantization, gpu, index)
			case VAMANA: if (gpu) throw Unsupported.of('vamana on GPU') else 'DISKANN'
			case SPARSE_INVERTED: 'SPARSE_INVERTED_INDEX'
		}
	}

	def private String hnsw(EQuantizationKind quantization) {
		switch quantization {
			case NONE: 'HNSW'
			case SCALAR: 'HNSW_SQ'
			case PRODUCT: 'HNSW_PQ'
			default: throw Unsupported.of(quantization.literal + ' quantization on HNSW')
		}
	}

	def private String ivf(EQuantizationKind quantization, boolean gpu, VectorIndex index) {
		switch quantization {
			case NONE: if (gpu) 'GPU_IVF_FLAT' else 'IVF_FLAT'
			case PRODUCT: if (gpu) 'GPU_IVF_PQ' else 'IVF_PQ'
			case SCALAR: {
				if (gpu || (written(index.quantization, 'bits') && index.quantization.bits != 8)) {
					throw Unsupported.of('scalar quantization other than 8-bit on IVF')
				}
				'IVF_SQ8'
			}
			default: throw Unsupported.of(quantization.literal + ' quantization on IVF')
		}
	}

	def String metric(VectorCollection c, VectorField field, VectorIndex index) {
		val function = ModelUtil.producingFunction(c, field)
		if (function !== null && function.type == EFunctionType.BM25) return 'BM25'
		val metric = if (written(field, 'metric')) field.metric
			else if (index !== null && written(index, 'metric')) index.metric
			else if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) EMetric.DOT
			else if (field.type == EVectorDType.BINARY_VECTOR) EMetric.HAMMING
			else EMetric.COSINE
		metricName(metric)
	}

	def String metricName(EMetric metric) {
		switch metric {
			case COSINE: 'COSINE'
			case EUCLIDEAN: 'L2'
			case DOT: 'IP'
			case HAMMING: 'HAMMING'
			case JACCARD: 'JACCARD'
			default: throw Unsupported.of(metric.literal + ' metric')
		}
	}

	def private Map<String, Expr> indexParams(VectorCollection c, VectorField field, VectorIndex index, String type) {
		val params = new LinkedHashMap<String, Expr>
		if (index !== null) {
			if (type.startsWith('HNSW')) {
				if (written(index, 'maxDegree')) params.put('M', num(index.maxDegree))
				if (written(index, 'buildCandidates')) params.put('efConstruction', num(index.buildCandidates))
			}
			if (type.contains('IVF') && written(index, 'centroids')) params.put('nlist', num(index.centroids))
			val quantization = index.quantization
			if (quantization !== null) {
				if (type == 'HNSW_SQ') params.put('sq_type', text(sqType(quantization.bits, written(quantization, 'bits'))))
				if (type.endsWith('_PQ') && written(quantization, 'subvectors')) params.put('m', num(quantization.subvectors))
				note(quantization, 'rescore', 'oversampling', 'keepOriginals', 'truncateDimension')
				params.putAll(extValues(quantization))
			}
			note(index, 'onDisk', 'online', 'searchCandidates')
			params.putAll(extValues(index).filter[key, value|key != 'index_type'])
		}
		val function = ModelUtil.producingFunction(c, field)
		if (function !== null && function.type == EFunctionType.BM25) {
			val analyzer = function.analyzer ?: function.inputs.filter(PayloadField).map[it.analyzer].filterNull.head
			if (analyzer !== null && written(analyzer, 'bm25K1')) params.put('bm25_k1', num(analyzer.bm25K1))
			if (analyzer !== null && written(analyzer, 'bm25B')) params.put('bm25_b', num(analyzer.bm25B))
		}
		params
	}

	def private String sqType(double bits, boolean given) {
		if (!given) return 'SQ8'
		switch bits as int {
			case 8: 'SQ8'
			case 6: 'SQ6'
			case 4: 'SQ4'
			case 16: 'FP16'
			default: throw Unsupported.of(bits + '-bit scalar quantization')
		}
	}

	def Make payloadIndex(PayloadIndex index) {
		if (index.kind == EPayloadIndexKind.FULL_TEXT) {
			notes += 'full_text payload index ' + index.name + ' (declare the field full-text)'
			return null
		}
		val target = index.target
		val root = if (target.rawPath !== null) target.rawPath.split('\\.').head else ModelUtil.fieldName(target.field)
		val args = kw('field_name', text(root), 'index_name', text(index.name))
		args.put('index_type', text(extString(index, 'index_type') ?: payloadIndexType(index)))
		val params = new LinkedHashMap<String, Expr>
		if (target.rawPath !== null || !target.subFields.empty) {
			params.put('json_path', text(MilvusFilter.path(target)))
			params.put('json_cast_type', text(castType(index)))
		}
		params.putAll(extValues(index).filter[key, value|key != 'index_type'])
		if (!params.empty) args.put('params', entries(params))
		note(index, 'analyzer', 'onDisk', 'lowercase', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		make('Index', args)
	}

	def private String payloadIndexType(PayloadIndex index) {
		if (written(index, 'structure')) {
			return switch index.structure {
				case INVERTED: 'INVERTED'
				case BITMAP: 'BITMAP'
				case BTREE: 'STL_SORT'
				default: 'AUTOINDEX'
			}
		}
		switch index.kind {
			case GEO: 'RTREE'
			case JSON: 'INVERTED'
			case KEYWORD: 'INVERTED'
			default: 'AUTOINDEX'
		}
	}

	def private String castType(PayloadIndex index) {
		if (!written(index, 'castType')) return 'varchar'
		switch index.castType {
			case BOOL: 'bool'
			case VARCHAR: 'varchar'
			case TEXT: 'varchar'
			case INT: 'double'
			case BIGINT: 'double'
			case INT8: 'double'
			case FLOAT: 'double'
			case DOUBLE: 'double'
			default: throw Unsupported.of('JSON index cast to ' + index.castType.literal)
		}
	}

	def List<Make> indexes(VectorCollection c) {
		val List<Make> result = newArrayList
		result += ModelUtil.vectorFields(c).map[vectorIndex(c, it, ModelUtil.vectorIndex(c, it))]
		result += c.indexes.filter(PayloadIndex).map[payloadIndex(it)].filterNull
		result
	}

	def Entries collectionArgs(VectorCollection c) {
		val args = kw('collection_name', text(c.name), 'schema', schema(c), 'index_params', items(indexes(c)))
		if (written(c, 'consistency')) args.put('consistency_level', consistency(c.consistency))
		val properties = new LinkedHashMap<String, Expr>
		if (written(c, 'ttlSeconds')) properties.put('collection.ttl.seconds', text(String.valueOf(c.ttlSeconds)))
		for (entry : extValues(c).entrySet) {
			if (COLLECTION_ARGUMENTS.contains(entry.key)) args.put(entry.key, entry.value) else properties.put(entry.key, entry.value)
		}
		if (!properties.empty) args.put('properties', entries(properties))
		args
	}

	def Expr consistency(EConsistencyLevel level) {
		new Code.Sym('ConsistencyLevel', switch level {
			case STRONG: 'Strong'
			case BOUNDED: 'Bounded'
			case SESSION: 'Session'
			case EVENTUAL: 'Eventually'
		})
	}

	def List<String> manualPartitions(VectorCollection c) {
		val key = ModelUtil.partitionKey(c)
		val List<String> names = newArrayList
		for (partition : ModelUtil.partitions(c).filter[!isIsDefault && it !== c.defaultPartition && it.key === null]) {
			if (key !== null) throw Unsupported.of('manual partitions together with a partition key')
			names += partition.name
		}
		if (key === null) {
			names += c.tenants.map[name]
			names += c.namespaces.map[it.key]
		}
		names
	}

	def Map<String, Expr> extValues(EObject host) {
		val values = new LinkedHashMap<String, Expr>
		if (host !== null) {
			for (entry : ModelUtil.extParams(host, MilvusGenerator.VENDOR).entrySet) {
				values.put(entry.key, Literals.of(entry.value))
			}
		}
		values
	}

	def String extString(EObject host, String key) {
		if (host === null) return null
		val value = ModelUtil.extParams(host, MilvusGenerator.VENDOR).get(key)
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
