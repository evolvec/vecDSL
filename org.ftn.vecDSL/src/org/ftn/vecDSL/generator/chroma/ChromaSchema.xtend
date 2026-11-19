package org.ftn.vecDSL.generator.chroma

import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Code.Entries
import org.ftn.vecDSL.generator.code.Code.Stmt
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.common.Unsupported
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EFieldLimit
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.EVectorDType
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Chroma collections (server 1.5.9). A Chroma collection holds one dense embedding per record,
 * an optional document and flat metadata; its HNSW space and graph parameters are set when it is created. The single vector
 * field is the embedding, the document-body field (or the only full-text field) is the document, and every other payload
 * field is a metadata key. Record ids are the key's text, and the key is also stored in the metadata under its own name,
 * typed and filterable.
 */
class ChromaSchema {

	static val INTEGRAL = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8]
	static val NUMERIC = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE]
	static val TEXTUAL = #[EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]
	static val NAME = '[a-zA-Z0-9][a-zA-Z0-9._-]{1,510}[a-zA-Z0-9]'
	static val KEYS = #{EPayloadIndexKind.KEYWORD, EPayloadIndexKind.INTEGER, EPayloadIndexKind.FLOAT, EPayloadIndexKind.BOOL,
		EPayloadIndexKind.UUID}

	/** Metadata keys that hold tenant and namespace values when the collection declares no partition-key field. */
	public static val TENANT_KEY = 'vecdsl_tenant'
	public static val NAMESPACE_KEY = 'vecdsl_namespace'

	val List<String> notes

	new(List<String> notes) {
		this.notes = notes
	}

	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists) {
		if (c.external !== null) throw Unsupported.of('collections derived from external sources; Chroma stores the records of every collection itself')
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of('collection ttl; Chroma records do not expire')
		if (c.autoId) throw Unsupported.of('auto-id; Chroma record ids are the primary key text')
		if (c.deletionProtection) throw Unsupported.of('deletion-protection; Chroma collections have no deletion guard')
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; Chroma records hold no references to other collections')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + ChromaStatements.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of('storage settings; Chroma keeps every collection in its persist path')
		if (!ModelUtil.partitions(c).empty) throw Unsupported.of(ChromaStatements.NO_PARTITIONS)
		if (!c.aliases.empty) throw Unsupported.of(ChromaStatements.NO_ALIASES)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + ChromaStatements.NO_RANKED_TEXT)
		if (!c.analyzers.empty) throw Unsupported.of('analyzers; ' + ChromaStatements.NO_ANALYZERS)
		for (name : ModelUtil.extParams(c, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'limits')
		checkName(c.name)
		keyType(c)
		val field = vectorField(c)
		val index = ModelUtil.vectorIndex(c, field)
		val args = kw('name', text(c.name))
		val database = databaseOf(c)
		if (database !== null) args.put('database', text(database))
		args.put('metric', text(space(metric(field, index))))
		if (index !== null) {
			val graph = hnsw(index)
			if (!graph.entries.empty) args.put('hnsw', graph)
		}
		for (payload : ModelUtil.payloadFields(c)) checkPayloadField(c, payload)
		documentField(c)
		for (declared : c.indexes.filter(PayloadIndex)) payloadIndex(c, declared)
		val metadata = entries
		if (c.description !== null) metadata.put('description', text(c.description))
		if (!c.tags.empty) metadata.put('tags', texts(c.tags))
		if (!metadata.entries.empty) args.put('metadata', metadata)
		val create = new Code.Run(runtime('create_collection', args)) as Stmt
		if (!ifNotExists) return #[create]
		newArrayList(new Code.When(runtime('has_collection', place(c)), true, #[create]) as Stmt)
	}

	/** Name and database of a collection, the arguments of the calls that address a collection by name. */
	def Entries place(VectorCollection c) {
		val args = kw('name', text(c.name))
		val database = databaseOf(c)
		if (database !== null) args.put('database', text(database))
		args
	}

	/** Collection identity every record call needs: name, database, key, metric, vector field and document field. */
	def Entries layout(VectorCollection c) {
		val args = place(c)
		args.put('key', text(ModelUtil.fieldName(c.primaryKey)))
		args.put('key_type', text(keyType(c)))
		val field = vectorField(c)
		args.put('metric', text(space(metric(field, ModelUtil.vectorIndex(c, field)))))
		args.put('vector', text(ModelUtil.fieldName(field)))
		if (field.dimension > 0) args.put('dim', num(field.dimension))
		val document = documentField(c)
		if (document !== null) args.put('document', text(ModelUtil.fieldName(document)))
		args
	}

	/** The layout plus what writes need: integer fields, declared limits and defaults. */
	def Entries writeLayout(VectorCollection c) {
		val args = layout(c)
		val integers = ModelUtil.payloadFields(c).filter[INTEGRAL.contains(type) || (type == EScalarDType.ARRAY && INTEGRAL.contains(elementType))]
			.map[ModelUtil.fieldName(it)].toList
		if (!integers.empty) args.put('integers', texts(integers))
		val declared = limits(c)
		if (!declared.entries.empty) args.put('limits', declared)
		val defaults = entries
		for (field : ModelUtil.payloadFields(c).filter[defaultValue !== null]) {
			defaults.put(ModelUtil.fieldName(field), Literals.of(field.defaultValue))
		}
		if (!defaults.entries.empty) args.put('defaults', defaults)
		args
	}

	/** The Chroma database of a collection declared in a logical-db, else null for the program's database. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) return null
		checkName(database.name)
		database.name
	}

	def static void checkName(String name) {
		if (!name.matches(NAME)) {
			throw Unsupported.of("the name '" + name + "'; Chroma names have 3 to 512 characters from [a-zA-Z0-9._-] and start and end with a letter or digit")
		}
	}

	/** The one dense vector field: Chroma stores one embedding per record. */
	def VectorField vectorField(VectorCollection c) {
		val fields = ModelUtil.vectorFields(c)
		for (field : fields) {
			if (field.type == EVectorDType.SPARSE_FLOAT_VECTOR) {
				throw Unsupported.of('sparse vector field ' + ModelUtil.fieldName(field) + '; ' + ChromaStatements.NO_RANKED_TEXT)
			}
		}
		if (fields.size != 1) throw Unsupported.of('a collection with ' + fields.size + ' vector fields; a Chroma collection holds one embedding per record')
		val field = fields.head
		val name = ModelUtil.fieldName(field)
		if (field.type != EVectorDType.FLOAT_VECTOR) throw Unsupported.of(field.type.literal + ' fields; Chroma stores float32 embeddings')
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; Chroma stores one embedding per record')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) throw Unsupported.of('server-side embedding of ' + name + ChromaStatements.NO_MODELS)
		note(field, 'normalized', 'onDisk', 'nullable', 'limits')
		field
	}

	def String keyType(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField)) throw Unsupported.of('a vector field as primary key')
		val type = (key as PayloadField).type
		if (INTEGRAL.contains(type)) return 'int'
		if (TEXTUAL.contains(type)) return 'text'
		throw Unsupported.of('primary key of type ' + type.literal + '; Chroma ids are strings')
	}

	/** The field stored as the Chroma document: the document-body field, else the only full-text field. */
	def PayloadField documentField(VectorCollection c) {
		val bodies = ModelUtil.payloadFields(c).filter[documentBody].toList
		val searched = ModelUtil.payloadFields(c).filter[fullText || analyzer !== null || fullTextIndexed(c, it)].toList
		if (bodies.size > 1) throw Unsupported.of('several document-body fields; a Chroma record has one document')
		val chosen = bodies.head ?: searched.head
		for (field : searched) {
			if (field !== chosen) throw Unsupported.of('full-text field ' + ModelUtil.fieldName(field) + ' beside the document ' + ModelUtil.fieldName(chosen) + '; a Chroma record has one document')
		}
		if (chosen !== null && !#[EScalarDType.VARCHAR, EScalarDType.TEXT].contains(chosen.type)) {
			throw Unsupported.of('document field ' + ModelUtil.fieldName(chosen) + ' of type ' + chosen.type.literal + '; Chroma documents are text')
		}
		if (chosen === c.primaryKey) throw Unsupported.of('the primary key as document; Chroma keeps the key in the metadata')
		chosen
	}

	def private boolean fullTextIndexed(VectorCollection c, PayloadField field) {
		c.indexes.filter(PayloadIndex).exists[index | index.target.field === field && index.kind == EPayloadIndexKind.FULL_TEXT]
	}

	/** Checks a payload field against what Chroma metadata holds: strings, integers, floats, booleans and non-empty arrays of one of them. */
	def void checkPayloadField(VectorCollection c, PayloadField field) {
		val name = ModelUtil.fieldName(field)
		if (field.partitionKey && !TEXTUAL.contains(field.type)) {
			throw Unsupported.of('partition key ' + name + ' of type ' + field.type.literal + '; tenant and namespace names are text')
		}
		if (!field.subFields.empty || field.nested) throw Unsupported.of('nested field ' + name + '; Chroma metadata is flat')
		val type = if (field.type == EScalarDType.ARRAY) field.elementType else field.type
		if (field.type == EScalarDType.ARRAY && !written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
		if (!NUMERIC.contains(type) && !TEXTUAL.contains(type) && type != EScalarDType.BOOL) {
			throw Unsupported.of(type.literal + ' field ' + name + '; Chroma metadata holds strings, integers, floats, booleans and arrays of them')
		}
		note(field, 'nullable')
	}

	/** Declared payload indexes: Chroma's default schema already keeps an inverted index on every metadata key. */
	def void payloadIndex(VectorCollection c, PayloadIndex index) {
		val path = index.target
		if (path.rawPath !== null || !path.subFields.empty) throw Unsupported.of('a payload index on a nested path; Chroma metadata is flat')
		if (index.kind == EPayloadIndexKind.FULL_TEXT) {
			if (path.field !== documentField(c)) throw Unsupported.of('a full_text index on ' + ModelUtil.fieldName(path.field) + ', which is not the document; Chroma indexes the document only')
		} else if (!KEYS.contains(index.kind)) {
			throw Unsupported.of(index.kind.literal + ' payload indexes; Chroma indexes metadata strings, integers, floats and booleans')
		} else if (!(path.field instanceof PayloadField)) {
			throw Unsupported.of('a payload index on a vector field')
		}
		note(index, 'structure', 'castType', 'analyzer', 'onDisk', 'lowercase', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		for (name : ModelUtil.extParams(index, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
	}

	/** Chroma's HNSW parameters of a vector index: max-degree, build-candidates and search-candidates. */
	def Entries hnsw(VectorIndex index) {
		switch index.family {
			case AUTO,
			case HNSW: {}
			default: throw Unsupported.of(index.family.literal + ' indexes; the local Chroma server builds HNSW graphs (SPANN is distributed only)')
		}
		if (index.quantization !== null) throw Unsupported.of('quantization; the local Chroma server stores full-precision HNSW graphs')
		note(index, 'accelerator', 'centroids', 'onDisk', 'online')
		for (name : ModelUtil.extParams(index, ChromaGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val result = entries
		if (written(index, 'maxDegree')) result.put('max_neighbors', num(index.maxDegree))
		if (written(index, 'buildCandidates')) result.put('ef_construction', num(index.buildCandidates))
		if (written(index, 'searchCandidates')) result.put('ef_search', num(index.searchCandidates))
		result
	}

	def EMetric metric(VectorField field, VectorIndex index) {
		if (written(field, 'metric')) field.metric
		else if (index !== null && written(index, 'metric')) index.metric
		else EMetric.COSINE
	}

	/** Chroma's HNSW space of a metric: l2 is squared Euclidean distance, ip and cosine are 1 minus the similarity. */
	def static String space(EMetric metric) {
		switch metric {
			case COSINE: 'cosine'
			case DOT: 'ip'
			case EUCLIDEAN: 'l2'
			default: throw Unsupported.of(metric.literal + ' metric; Chroma measures l2, ip and cosine distances')
		}
	}

	/** The metadata key of tenant or namespace values: the partition-key field, else a reserved key. */
	def static String sliceKey(VectorCollection c, boolean namespace) {
		val key = ModelUtil.partitionKey(c)
		if (key !== null) return ModelUtil.fieldName(key)
		if (namespace) NAMESPACE_KEY else TENANT_KEY
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
