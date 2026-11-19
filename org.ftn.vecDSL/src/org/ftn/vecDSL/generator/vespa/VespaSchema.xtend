package org.ftn.vecDSL.generator.vespa

import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.generator.vespa.VespaPackage.Field
import org.ftn.vecDSL.generator.vespa.VespaPackage.Hnsw
import org.ftn.vecDSL.generator.vespa.VespaPackage.Rank
import org.ftn.vecDSL.generator.vespa.VespaPackage.Schema
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ExtParam
import org.ftn.vecDSL.vecDSL.Literal
import org.ftn.vecDSL.vecDSL.NumberLit
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.PhasedRankProfile
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.SetPropertyOp
import org.ftn.vecDSL.vecDSL.StringLit
import org.ftn.vecDSL.vecDSL.Update
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * The application package of the program as its statements leave it: one schema per collection (document type), evolved by the
 * alter statements before each statement, and the ordered set of document types the program has deployed. Every DDL statement
 * deploys the whole package as it stands after the statement.
 */
class VespaSchema {

	public static val NO_EMBEDDER = 'generated programs deploy no models: an embedder is a component in services.xml whose model files must be in the application package'
	public static val NO_MODELS = '; ' + NO_EMBEDDER
	public static val NO_DISK_SETTING = 'on-disk; Vespa keeps attributes in memory unless they are paged, and the vecDSL on-disk flag has no Vespa mapping yet (attribute: paged)'

	static val NUMBERS = #{EScalarDType.INT -> 'int', EScalarDType.BIGINT -> 'long', EScalarDType.INT8 -> 'byte', EScalarDType.FLOAT -> 'float',
		EScalarDType.DOUBLE -> 'double'}
	static val STRINGS = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ]
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val CELLS = #{'float_vector' -> 'float', 'bfloat16_vector' -> 'bfloat16', 'float64_vector' -> 'double', 'int8_vector' -> 'int8'}
	static val METRICS = #{'cosine' -> 'angular', 'euclidean' -> 'euclidean', 'dot' -> 'dotproduct', 'hamming' -> 'hamming'}

	val List<String> notes
	val Set<VectorCollection> tenanted = newHashSet
	val Set<VectorCollection> namespaced = newHashSet
	val Set<VectorCollection> expiring = newHashSet
	val Set<VectorCollection> versioned = newHashSet
	val Map<VectorCollection, Schema> evolved = newHashMap
	val Map<VectorCollection, Map<String, String>> renames = newHashMap
	val Map<String, Schema> deployed = newLinkedHashMap
	var declaring = false

	new(List<String> notes, Script script) {
		this.notes = notes
		for (object : script.eAllContents.toIterable) slices(object)
	}

	/**
	 * Hidden attributes a collection needs, decided from the whole script: tenant and namespace slices of its statements, an expiry
	 * time when a statement gives it a ttl, a version counter when an update checks if-version.
	 */
	def private void slices(EObject object) {
		val collection = ModelUtil.targetCollection(object)
		if (collection === null) return;
		val tenant = object.eClass.getEStructuralFeature('tenant')
		if (tenant !== null && object.eGet(tenant) !== null) tenanted += collection
		val namespace = object.eClass.getEStructuralFeature('namespace')
		if (namespace !== null && namespace.EType.name == 'EString' && object.eGet(namespace) !== null) namespaced += collection
		if (written(object, 'ttlSeconds')) expiring += collection
		if (object instanceof SetPropertyOp && (object as SetPropertyOp).property.literal == 'ttl') expiring += collection
		if (object instanceof Update && written(object, 'ifVersion')) versioned += collection
	}

	def Schema schema(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, Schema schema) {
		if (schema === null) {
			evolved.remove(c)
			renames.remove(c)
		} else {
			evolved.put(c, schema)
		}
	}

	def void rename(VectorCollection c, String field, String newName) {
		val map = renames.computeIfAbsent(c)[newHashMap]
		for (entry : map.entrySet) if (entry.value == field) entry.value = newName
		if (!map.containsValue(newName)) map.put(field, newName)
	}

	/** The document field name of a declared field, after the renames of earlier alter statements. */
	def String name(VectorCollection c, org.ftn.vecDSL.vecDSL.Field field) {
		val declared = ModelUtil.fieldName(field)
		renames.get(c)?.get(declared) ?: declared
	}

	def boolean isExpiring(VectorCollection c) {
		expiring.contains(c)
	}

	def boolean isVersioned(VectorCollection c) {
		versioned.contains(c)
	}

	/** The document type of a collection: its name, after the logical database's name and an underscore. */
	def static String documentType(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		val type = if (database === null) c.name else database.name + '_' + c.name
		if (!VespaPackage.identifier(type)) throw Unsupported.of('the document type name ' + type + '; schema names are identifiers')
		type
	}

	def private Schema declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys documents by an integer, text or uuid field, which becomes the user part of the document id')
		}
		val List<Field> fields = newArrayList
		for (field : c.fields) fields += fieldsOf(c, field)
		val keyName = ModelUtil.fieldName(key)
		fields.replaceAll[f | if (f.name == keyName) f.searchable else f]
		if (!c.tenants.empty || tenanted.contains(c)) fields += hidden(VespaPackage.TENANT)
		if (!c.namespaces.empty || namespaced.contains(c)) fields += hidden(VespaPackage.NAMESPACE)
		if (!ModelUtil.partitions(c).empty) fields += hidden(VespaPackage.PARTITION)
		if (versioned.contains(c)) fields += new Field(VespaPackage.VERSION, 'long', 'NUMERIC', false, true, null, null, null)
		val Map<String, Double> bm25 = newLinkedHashMap
		val analyzer = analyzerOf(c)
		if (analyzer !== null && written(analyzer, 'bm25K1')) bm25.put('k1', analyzer.bm25K1)
		if (analyzer !== null && written(analyzer, 'bm25B')) bm25.put('b', analyzer.bm25B)
		val result = new Schema(documentType(c), keyName, NUMBERS.containsKey((key as PayloadField).type), fields, bm25, false, ranks(c))
		if (expiring.contains(c)) result.expiringNow else result
	}

	/** Rank profiles declared in the collection's extension vespa blocks; other extension items are rejected by name. */
	def private List<Rank> ranks(VectorCollection c) {
		val List<Rank> result = newArrayList
		for (block : c.extensions.filter[vendor == VespaGenerator.VENDOR]) {
			for (item : block.items) {
				switch item {
					PhasedRankProfile: result += rank(item)
					ExtParam: throw Unsupported.of('extension parameter ' + (item.key ?: item.quotedKey) + ' on a collection')
					default: throw Unsupported.of(item.eClass.name + ' on a collection; a Vespa collection extension declares rank profiles')
				}
			}
		}
		result
	}

	def private Rank rank(PhasedRankProfile profile) {
		if (profile.name.startsWith('vecdsl_')) throw Unsupported.of('the rank profile name ' + profile.name + '; the prefix vecdsl_ is reserved for the generator\'s rank profiles')
		if (profile.model !== null) throw Unsupported.of("the model of rank profile " + profile.name + '; generated programs deploy no models: an onnx-model needs its model file in the application package')
		val Map<String, String> functions = newLinkedHashMap
		for (fn : profile.functions) functions.put(fn.name, fn.expression)
		val List<String> matchFeatures = newArrayList
		val List<String> summaryFeatures = newArrayList
		val Map<String, String> settings = newLinkedHashMap
		for (param : profile.params) {
			val key = param.key ?: param.quotedKey
			switch key {
				case 'match_features': matchFeatures += features(param.value, key)
				case 'summary_features': summaryFeatures += features(param.value, key)
				case 'num_threads_per_search': {
					if (!(param.value instanceof NumberLit)) throw Unsupported.of('num_threads_per_search that is not a number')
					settings.put('num-threads-per-search', VespaPackage.number((param.value as NumberLit).value))
				}
				default: throw Unsupported.of('the rank profile parameter ' + key + '; has no Vespa mapping yet (keep_rank_count and rank_score_drop_limit belong to the phases of the profile)')
			}
		}
		val List<String> inputs = newArrayList
		for (input : profile.inputs) inputs += 'query(' + input.name + ') ' + input.tensorType
		new Rank(profile.name, profile.parent?.name, inputs, functions, profile.firstPhase, profile.secondPhase, profile.globalPhase,
			if (written(profile, 'rerankCount')) profile.rerankCount else null, matchFeatures, summaryFeatures, settings)
	}

	def private List<String> features(Literal value, String key) {
		if (!(value instanceof StringLit)) throw Unsupported.of(key + ' that is not a string of feature names')
		(value as StringLit).value.trim.split('\\s+').toList
	}

	/** Whether a declared rank profile, or one it inherits, has the input query(vecdsl_q) a nearest-neighbour query sends. */
	def static boolean takesQueryVector(PhasedRankProfile profile) {
		var walked = profile
		while (walked !== null) {
			if (walked.inputs.exists[name == VespaPackage.QUERY_TENSOR]) return true
			walked = walked.parent
		}
		false
	}

	def private Field hidden(String name) {
		new Field(name, 'string', 'STRING', false, true, null, null, null)
	}

	/** The analyzer of the collection's full-text fields; Vespa's linguistics applies one processing to every text field. */
	def Analyzer analyzerOf(VectorCollection c) {
		val used = ModelUtil.payloadFields(c).filter[fullText(c, it)].map[analyzer].filterNull.toSet
		used += c.indexes.filter(PayloadIndex).filter[kind == EPayloadIndexKind.FULL_TEXT].map[analyzer].filterNull
		if (used.size > 1 && (used.map[language].toSet.size > 1 || used.map[bm25K1].toSet.size > 1 || used.map[bm25B].toSet.size > 1)) {
			throw Unsupported.of('full-text fields with different analyzers; has no Vespa mapping yet (one rank profile per analyzer setting and set_language per field)')
		}
		val analyzer = used.head
		if (analyzer !== null) checkAnalyzer(analyzer)
		analyzer
	}

	def static void checkAnalyzer(Analyzer analyzer) {
		val tokenizer = analyzer.tokenizer.literal
		if (tokenizer != 'standard' && tokenizer != 'language') {
			throw Unsupported.of('the ' + tokenizer + ' tokenizer; has no Vespa mapping yet (a linguistics component or match settings per field: match: exact for keyword, match: gram for ngram)')
		}
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; has no Vespa mapping yet (files in the application package read by a linguistics component)')
		if (analyzer.language !== null && !#['en', 'english'].contains(analyzer.language.toLowerCase)) {
			throw Unsupported.of('the language ' + analyzer.language + '; has no Vespa mapping yet (set_language in the indexing statements and the query language)')
		}
		if (written(analyzer, 'stopwords') && analyzer.stopwords != 'none') {
			throw Unsupported.of('stopwords ' + analyzer.stopwords + '; has no Vespa mapping yet (Vespa removes no stop words when indexing; a query rewrite of the stop words would carry it)')
		}
		for (filter : analyzer.filters) checkFilter(filter.type.literal)
	}

	/** Filters Vespa's default linguistics applies: lowercasing and stemming (stemming: best); stop with the list none. */
	def static void checkFilter(String filter) {
		switch filter {
			case 'lowercase',
			case 'stemmer',
			case 'stop': {}
			case 'ascii_folding': {}
			default: throw Unsupported.of('the ' + filter + ' filter; has no Vespa mapping yet (a custom linguistics component)')
		}
	}

	def boolean fullText(VectorCollection c, PayloadField field) {
		field.fullText || field.documentBody || field.analyzer !== null
			|| c.indexes.filter(PayloadIndex).exists[target.field === field && kind == EPayloadIndexKind.FULL_TEXT]
	}

	/** The schema fields of one declared field: a document field, and for a full-text field its exact companion attribute. */
	def List<Field> fieldsOf(VectorCollection c, org.ftn.vecDSL.vecDSL.Field field) {
		val named = name(c, field)
		if (VespaPackage.RESERVED.contains(named.toLowerCase)) {
			throw Unsupported.of('the field name ' + named + '; Vespa reserves it (Schema.RESERVED_NAMES of config-model)')
		}
		if (named.startsWith('vecdsl_')) throw Unsupported.of('the field name ' + named + '; the prefix vecdsl_ is reserved for the generator\'s hidden fields')
		switch field {
			VectorField: #[vectorField(c, field, named)]
			PayloadField: payloadFields(c, field, named)
		}
	}

	def private List<Field> payloadFields(VectorCollection c, PayloadField field, String name) {
		declaredNote(field, 'nullable', 'maxLength', 'defaultValue')
		if (!field.limits.empty) throw Unsupported.of('field limits; Vespa validates the value types of a document, not their sizes')
		if (!field.subFields.empty) throw Unsupported.of('sub-fields of field ' + name + '; has no Vespa mapping yet (struct fields with struct-field attributes)')
		val type = field.type
		val indexed = c.indexes.filter(PayloadIndex).exists[target.field === field]
		if (NUMBERS.containsKey(type)) return #[new Field(name, NUMBERS.get(type), 'NUMERIC', false, indexed, null, null, null)]
		if (type == EScalarDType.BOOL) return #[new Field(name, 'bool', 'BOOL', false, indexed, null, null, null)]
		if (STRINGS.contains(type)) {
			if (fullText(c, field)) {
				if (type != EScalarDType.VARCHAR && type != EScalarDType.TEXT) throw Unsupported.of('full-text search on ' + type.literal + ' field ' + name)
				analyzerOf(c)
				return #[new Field(name, 'string', 'TEXT', false, false, null, null, null),
					new Field(VespaPackage.EXACT + name, 'string', 'STRING', false, false, null, null, name)]
			}
			return #[new Field(name, 'string', 'STRING', false, indexed, null, null, null)]
		}
		switch type {
			case ARRAY: {
				if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
				val element = field.elementType
				if (NUMBERS.containsKey(element)) return #[new Field(name, 'array<' + NUMBERS.get(element) + '>', 'NUMERIC', true, indexed, null, null, null)]
				if (element == EScalarDType.BOOL) return #[new Field(name, 'array<bool>', 'BOOL', true, indexed, null, null, null)]
				if (STRINGS.contains(element)) return #[new Field(name, 'array<string>', 'STRING', true, indexed, null, null, null)]
				throw Unsupported.of('arrays of ' + element.literal + ' in field ' + name + '; has no Vespa mapping yet (array<struct> with struct-field attributes)')
			}
			case JSON,
			case MAP,
			case STRUCT: throw Unsupported.of(type.literal + ' field ' + name + '; has no Vespa mapping yet (struct and map<string,string> fields with struct-field attributes)')
			case GEO_POINT: throw Unsupported.of('geo_point field ' + name + '; has no Vespa mapping yet (a position field and the geoLocation operator)')
			case GEOMETRY: throw Unsupported.of('geometry field ' + name + '; Vespa stores points (the position type) but no shapes, so it has no spatial relation operators')
			case BYTES: throw Unsupported.of('bytes field ' + name + '; has no Vespa mapping yet (a raw field, summary only)')
			default: throw Unsupported.of(type.literal + ' field ' + name + '; Vespa documents hold no media values (the client stores media elsewhere)')
		}
	}

	def private Field vectorField(VectorCollection c, VectorField field, String name) {
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; has no Vespa mapping yet (a mixed tensor tensor<float>(p{},x[d]) with max_sim in a rank expression)')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) throw Unsupported.of('server-side embedding of ' + name + NO_MODELS)
		if (field.onDisk) throw Unsupported.of(NO_DISK_SETTING)
		declaredNote(field, 'normalized', 'nullable')
		val type = field.type.literal
		if (type == 'sparse_float_vector') throw Unsupported.of('sparse vector field ' + name + '; has no Vespa mapping yet (a mapped tensor tensor<float>(t{}) ranked with sum(query(q) * attribute(f)), or a weightedset with wand)')
		if (type == 'binary_vector') throw Unsupported.of('binary vector field ' + name + '; has no Vespa mapping yet (tensor<int8>(x[dim/8]) of packed bits with distance-metric hamming)')
		val cell = CELLS.get(type)
		if (cell === null) {
			throw Unsupported.of(type + ' fields; Vespa tensor cells are double, float, bfloat16 or int8 (no float16 and no unsigned int8)')
		}
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		if (!field.limits.empty) throw Unsupported.of('field limits; Vespa validates the value types of a document, not their sizes')
		val index = ModelUtil.vectorIndex(c, field)
		new Field(name, 'tensor<' + cell + '>(x[' + field.dimension + '])', 'TENSOR', false, false, distanceMetric(field, index), hnsw(index), null)
	}

	def String metric(VectorField field, VectorIndex index) {
		val chosen = if (written(field, 'metric')) field.metric else if (index !== null && written(index, 'metric')) index.metric else EMetric.COSINE
		chosen.literal
	}

	/** angular for cosine (prenormalized-angular for normalized vectors), euclidean, dotproduct and hamming. */
	def String distanceMetric(VectorField field, VectorIndex index) {
		val chosen = metric(field, index)
		val distance = METRICS.get(chosen)
		if (distance === null) throw Unsupported.of('the ' + chosen + ' metric; Vespa\'s distance metrics are euclidean, angular, dotproduct, prenormalized-angular, geodegrees and hamming')
		if (chosen == 'cosine' && field.normalized) 'prenormalized-angular' else distance
	}

	/** HNSW for hnsw and auto with max-links-per-node and neighbors-to-explore-at-insert; no index (exact search) for flat or none. */
	def Hnsw hnsw(VectorIndex index) {
		if (index === null) return null
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; Vespa builds and searches HNSW indexes on the CPU')
		if (index.onDisk) throw Unsupported.of(NO_DISK_SETTING)
		if (written(index, 'centroids')) throw Unsupported.of('centroids; Vespa\'s vector index is HNSW (no IVF lists)')
		for (key : ModelUtil.extParams(index, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a vector index')
		declaredNote(index, 'online')
		val q = index.quantization
		if (q !== null && q.type.literal != 'none') {
			throw Unsupported.of(q.type.literal + ' quantization; has no Vespa mapping yet (binarize | pack_bits into a hamming field for binary, int8 or bfloat16 cells written by the client for scalar)')
		}
		switch index.family {
			case FLAT: {
				if (written(index, 'maxDegree') || written(index, 'buildCandidates')) throw Unsupported.of('graph settings on a flat index; an exact search has no graph')
				null
			}
			case AUTO,
			case HNSW: new Hnsw(if (written(index, 'maxDegree')) index.maxDegree else null,
				if (written(index, 'buildCandidates')) index.buildCandidates else null)
			default: throw Unsupported.of(index.family.literal + ' indexes; Vespa indexes tensor attributes with HNSW (no IVF, Vamana or sparse inverted index)')
		}
	}

	/** Payload indexes ask for an attribute with fast-search (exact kinds) or an index field (full_text) on a declared field. */
	def void checkPayloadIndex(VectorCollection c, PayloadIndex payloadIndex) {
		val path = payloadIndex.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields')
		if (!path.subFields.empty) throw Unsupported.of('a payload index on a sub-field; has no Vespa mapping yet (struct-field attributes)')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		for (key : ModelUtil.extParams(payloadIndex, VespaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + key + ' on a payload index')
		if (payloadIndex.onDisk) throw Unsupported.of(NO_DISK_SETTING)
		note(payloadIndex, 'structure', 'lowercase', 'minTokenLength', 'maxTokenLength', 'prefixMatch')
		val field = schema(c).field(name(c, path.field))
		val expected = switch payloadIndex.kind {
			case KEYWORD,
			case UUID,
			case DATETIME: 'STRING'
			case BOOL: 'BOOL'
			case INTEGER,
			case FLOAT: 'NUMERIC'
			case FULL_TEXT: 'TEXT'
			case GEO: throw Unsupported.of('geo payload indexes; has no Vespa mapping yet (position fields)')
			case JSON: throw Unsupported.of('json payload indexes; has no Vespa mapping yet (struct-field attributes)')
		}
		if (field.kind != expected) throw Unsupported.of('a ' + payloadIndex.kind.literal + ' index on ' + field.name + ', which is a ' + field.kind + ' field; has no Vespa mapping yet (a synthetic field with its own indexing)')
	}

	// ---------------------------------------------------------------- the package

	def boolean isDeployed(String documentType) {
		deployed.containsKey(documentType)
	}

	def void put(Schema schema) {
		deployed.put(schema.name, schema)
	}

	def void remove(String documentType) {
		deployed.remove(documentType)
	}

	def List<Schema> current() {
		deployed.values.toList
	}

	/**
	 * Deploys the package as the program's statements leave it. created and dropped name the document types the statement adds or
	 * removes; the runtime checks them against the deployed application (an existing type fails a create unless skip is set, a
	 * missing type fails a drop unless ignore is set) and deletes the documents of the dropped types before the deployment.
	 */
	def Stmt deploy(List<String> overrides, List<String> created, boolean skip, List<String> dropped, boolean ignore) {
		val files = VespaPackage.files(current)
		val contents = entries
		for (file : files.entrySet) contents.put(file.key, text(file.value))
		val args = kw('files', contents)
		if (!files.empty && !overrides.empty) args.put('overrides', text(VespaPackage.validationOverrides(overrides)))
		if (!created.empty) {
			args.put('created', texts(created))
			if (skip) args.put('skip_existing', flag(true))
		}
		if (!dropped.empty) {
			args.put('dropped', texts(dropped))
			if (ignore) args.put('ignore_missing', flag(true))
		}
		new Code.Run(runtime('deploy', args))
	}

	/** Settings of a declaration that Vespa ignores, listed only on the statement that creates the collection. */
	def private void declaredNote(EObject host, String... features) {
		if (declaring) note(host, features)
	}

	def void setDeclaring(boolean value) {
		declaring = value
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
