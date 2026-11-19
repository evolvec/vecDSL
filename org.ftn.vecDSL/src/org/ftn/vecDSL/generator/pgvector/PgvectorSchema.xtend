package org.ftn.vecDSL.generator.pgvector

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
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Column
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Query
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Table
import org.ftn.vecDSL.generator.pgvector.PgvectorSql.Vec
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
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
 * Maps vecDSL collections to PostgreSQL tables with pgvector columns (server 18, pgvector 0.8.7). Every field is a typed
 * column in declaration order: payload fields as SQL types (nested and json fields as jsonb, arrays as SQL arrays), vector
 * fields as vector, halfvec, bit or sparsevec columns with their dimension, each with its own index. The primary key is the
 * key column; tenant, namespace and partition names live in the partition-key field or in hidden text columns.
 */
class PgvectorSchema {

	public static val TENANT_COLUMN = 'vecdsl_tenant'
	public static val NAMESPACE_COLUMN = 'vecdsl_namespace'
	public static val PARTITION_COLUMN = 'vecdsl_partition'
	public static val DEFAULT_PARTITION = 'default'

	static val SCALARS = #{EScalarDType.INT -> 'integer', EScalarDType.BIGINT -> 'bigint', EScalarDType.INT8 -> 'smallint',
		EScalarDType.FLOAT -> 'real', EScalarDType.DOUBLE -> 'double precision', EScalarDType.BOOL -> 'boolean',
		EScalarDType.VARCHAR -> 'text', EScalarDType.TEXT -> 'text', EScalarDType.DATE -> 'date', EScalarDType.DATETIME -> 'timestamp',
		EScalarDType.TIMESTAMPTZ -> 'timestamptz', EScalarDType.UUID -> 'uuid', EScalarDType.BYTES -> 'bytea', EScalarDType.JSON -> 'jsonb',
		EScalarDType.MAP -> 'jsonb', EScalarDType.STRUCT -> 'jsonb'}
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]

	val List<String> notes
	val Set<VectorCollection> tenanted
	val Set<VectorCollection> namespaced
	val Map<VectorCollection, Table> evolved = newHashMap

	new(List<String> notes, Script script) {
		this.notes = notes
		this.tenanted = newHashSet
		this.namespaced = newHashSet
		for (object : script.eAllContents.toIterable) slices(object)
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

	/**
	 * The table of a collection as the statements so far leave it: the declaration, changed by the alter statements mapped
	 * before (added, dropped and renamed columns, a new table name). The same descriptor serves DDL, writes and queries.
	 */
	def Table table(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	/** Records the table an alter statement leaves; create and drop return the collection to its declaration. */
	def void evolve(VectorCollection c, Table table) {
		if (table === null) evolved.remove(c) else evolved.put(c, table)
	}

	def private Table declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys tables by an integer, text or uuid column')
		}
		val List<Column> columns = newArrayList
		for (field : c.fields) columns += column(c, field)
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_COLUMN else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_COLUMN else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else PARTITION_COLUMN
		new Table(databaseOf(c), c.name, ModelUtil.fieldName(key), columns, tenant, namespace, partition, emptyMap)
	}

	/** The PostgreSQL database of a collection declared in a logical-db, else null for the program's database. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) return null
		PgvectorSql.id(database.name)
		database.name
	}

	def Column column(VectorCollection c, Field field) {
		switch field {
			VectorField: vectorColumn(c, field)
			PayloadField: payloadColumn(c, field)
		}
	}

	def private Column payloadColumn(VectorCollection c, PayloadField field) {
		val name = ModelUtil.fieldName(field)
		PgvectorSql.id(name)
		note(field, 'nullable')
		val nested = !field.subFields.empty || field.nested
		val config = textConfig(c, field)
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		if (nested || field.type == EScalarDType.JSON || field.type == EScalarDType.MAP || field.type == EScalarDType.STRUCT) {
			if (config !== null) throw Unsupported.of('full-text search on json field ' + name)
			return new Column(name, 'jsonb', null, true, null, null, true, defaultOf(field), checks)
		}
		if (field.type == EScalarDType.ARRAY) {
			if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			val element = scalar(field.elementType, name)
			if (element == 'jsonb' || element == 'bytea') throw Unsupported.of('arrays of ' + field.elementType.literal + ' in field ' + name)
			return new Column(name, element + '[]', element, false, null, null, true, defaultOf(field), checks)
		}
		val base = scalar(field.type, name)
		val sqlType = if (written(field, 'maxLength') && base == 'text') 'varchar(' + field.maxLength + ')' else base
		if (config !== null && base != 'text') throw Unsupported.of('full-text search on ' + field.type.literal + ' field ' + name)
		new Column(name, sqlType, null, false, null, config, true, defaultOf(field), checks)
	}

	def private static Expr defaultOf(PayloadField field) {
		if (field.defaultValue === null) null else Literals.of(field.defaultValue)
	}

	def private String scalar(EScalarDType type, String field) {
		val sql = SCALARS.get(type)
		if (sql !== null) return sql
		switch type {
			case GEO_POINT: throw Unsupported.of('geo_point field ' + field + '; has no pgvector mapping yet (earthdistance in contrib)')
			case GEOMETRY: throw Unsupported.of('geometry field ' + field + '; PostgreSQL has no geometry type without PostGIS, which the pgvector image does not include')
			default: throw Unsupported.of(type.literal + ' field ' + field + '; PostgreSQL has no media types (bytea holds bytes without media semantics)')
		}
	}

	/** A field limit as a named CHECK constraint; {} stands for the column (pgvector checks vector dimensions in the type). */
	def static String check(org.ftn.vecDSL.vecDSL.FieldLimit limit, Field field) {
		val name = ModelUtil.fieldName(field)
		val condition = switch limit.type {
			case ARRAY_LEN: {
				if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) throw Unsupported.of('array_len on ' + name + ', which is not an array')
				'cardinality({}) <= ' + limit.value
			}
			case MAX_SIZE: {
				if (field instanceof VectorField) throw Unsupported.of('max_size on vector field ' + name)
				'octet_length({}::text) <= ' + limit.value
			}
			case VECTOR_DIM: {
				if (!(field instanceof VectorField)) throw Unsupported.of('vector_dim on payload field ' + name)
				'vector_dims({}) <= ' + limit.value
			}
		}
		'CONSTRAINT ' + PgvectorSql.id(limit.name) + ' CHECK (' + condition + ')'
	}

	/** The text search configuration of a full-text field: its analyzer's, else simple (lowercased words, no stemming). */
	def String textConfig(VectorCollection c, PayloadField field) {
		val indexed = c.indexes.filter(PayloadIndex).filter[target.field === field && kind == EPayloadIndexKind.FULL_TEXT].toList
		if (!field.fullText && !field.documentBody && field.analyzer === null && indexed.empty) return null
		val analyzer = field.analyzer ?: indexed.map[it.analyzer].filterNull.head
		if (analyzer === null) 'simple' else configName(analyzer)
	}

	/** An analyzer is a text search configuration in the public schema, named like the analyzer. */
	def static String configName(Analyzer analyzer) {
		'public.' + PgvectorSql.id(analyzer.name)
	}

	def private Column vectorColumn(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		PgvectorSql.id(name)
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; pgvector stores one vector per column')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) {
			throw Unsupported.of('server-side embedding of ' + name + PgvectorStatements.NO_MODELS)
		}
		if (written(field, 'modifier') && field.modifier.literal == 'idf') throw Unsupported.of('the idf modifier on ' + name + '; pgvector stores sparse vectors as given')
		note(field, 'normalized', 'onDisk')
		val vectorType = switch field.type {
			case FLOAT_VECTOR: 'vector'
			case FLOAT16_VECTOR: 'halfvec'
			case BINARY_VECTOR: 'bit'
			case SPARSE_FLOAT_VECTOR: 'sparsevec'
			default: throw Unsupported.of(field.type.literal + ' fields; pgvector stores vector (float32), halfvec (float16), bit and sparsevec values')
		}
		if (vectorType != 'sparsevec' && field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		val index = indexOf(c, field)
		val metric = metric(field, index)
		var String quantization = null
		var oversampling = 1.0
		if (index !== null && index.quantization !== null) {
			val q = index.quantization
			note(q, 'keepOriginals', 'truncateDimension')
			quantization = switch q.type {
				case NONE: null
				case BINARY: {
					if (written(q, 'bits') && q.bits != 1) throw Unsupported.of('binary quantization with ' + q.bits + ' bits; binary_quantize keeps one bit per dimension')
					'binary'
				}
				case SCALAR: {
					if (!written(q, 'bits') || q.bits != 16) throw Unsupported.of('scalar quantization to ' + (if (written(q, 'bits')) q.bits + ' bits' else 'the default width') + '; pgvector quantizes to halfvec (16 bits) and to bits only')
					'halfvec'
				}
				case PRODUCT: throw Unsupported.of('product quantization; pgvector has no product quantization')
			}
			if (quantization !== null && vectorType != 'vector') throw Unsupported.of('quantization of ' + vectorType + ' field ' + name + '; pgvector quantizes vector columns')
			if (quantization == 'binary' && metric != 'euclidean' && metric != 'cosine' && metric != 'dot') throw Unsupported.of('binary quantization with the ' + metric + ' metric')
			if (written(q, 'oversampling')) oversampling = q.oversampling
			else if (quantization !== null) oversampling = 4.0
		}
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		new Column(name, vectorType, null, false, new Vec(vectorType, field.dimension, metric, quantization, oversampling), null, field.nullable, null, checks)
	}

	/** The vector index of a field: declared on the field or in the collection. */
	def VectorIndex indexOf(VectorCollection c, VectorField field) {
		ModelUtil.vectorIndex(c, field)
	}

	/** The metric of a vector field: the field's, else its index's, else cosine. */
	def String metric(VectorField field, VectorIndex index) {
		val metric = if (written(field, 'metric')) field.metric else if (index !== null && written(index, 'metric')) index.metric else EMetric.COSINE
		metric.literal
	}

	// ---------------------------------------------------------------- DDL

	/** Analyzers first (their configurations exist before the indexes that use them), then the table and its objects in one transaction. */
	/**
	 * A collection's table with its objects. A declaration (fresh) starts from the declared fields; a create that names an
	 * existing collection keeps the columns earlier alter statements changed, and skips fields and indexes they dropped.
	 */
	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(PgvectorStatements.EXTERNAL)
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of(PgvectorStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of('auto-id; has no pgvector mapping yet (identity columns and gen_random_uuid)')
		if (c.dynamic) throw Unsupported.of('dynamic; has no pgvector mapping yet (a jsonb column for undeclared fields)')
		if (c.deletionProtection) throw Unsupported.of(PgvectorStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; has no pgvector mapping yet (foreign keys)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + PgvectorStatements.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of(PgvectorStatements.NO_STORAGE)
		if (!c.tags.empty) throw Unsupported.of(PgvectorStatements.NO_TAGS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + PgvectorStatements.functionReason(c.functions.head.type.literal))
		if (!c.limits.empty) throw Unsupported.of(PgvectorStatements.collectionLimit(c.limits.head.type.literal))
		for (name : ModelUtil.extParams(c, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency')
		if (fresh) evolve(c, null)
		val table = table(c)
		val database = table.database
		val List<Stmt> result = newArrayList
		for (analyzer : c.analyzers) result += createAnalyzer(analyzer, database)
		val List<String> ddl = newArrayList
		ddl += PgvectorSql.createTable(table, ifNotExists)
		ddl += partitions(c, table, ifNotExists)
		for (field : ModelUtil.vectorFields(c).filter[present(table, it)]) {
			val index = indexOf(c, field)
			if (index !== null) ddl += vectorIndex(c, table, field, index, ifNotExists)
		}
		for (declared : c.indexes.filter(PayloadIndex).filter[target.field === null || present(table, target.field)]) ddl += payloadIndex(c, table, declared, ifNotExists)
		for (field : ModelUtil.payloadFields(c).filter[present(table, it)]) {
			val config = table.column(ModelUtil.fieldName(field)).config
			if (config !== null && !c.indexes.filter(PayloadIndex).exists[target.field === field && kind == EPayloadIndexKind.FULL_TEXT]) {
				ddl += PgvectorSql.createPayloadIndex(table, ftsIndexName(c, field), table.column(ModelUtil.fieldName(field)), #[], 'fts', true)
			}
		}
		if (fresh || !ifNotExists) {
			if (c.description !== null) ddl += 'COMMENT ON TABLE ' + PgvectorSql.table(table) + ' IS ' + PgvectorSql.literal(c.description)
			for (alias : c.aliases) ddl += createView(alias.name, table, ifNotExists)
		}
		result += execute(ddl, database)
		result
	}

	/** Whether a field still has its column (an alter statement may have dropped it). */
	def private static boolean present(Table table, Field field) {
		val name = table.renames.getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field))
		table.columns.exists[column | column.name == name]
	}

	/** The GIN index of a full-text field without a declared full_text index. */
	def static String ftsIndexName(VectorCollection c, PayloadField field) {
		indexName(c, ModelUtil.fieldName(field) + '_fts')
	}

	/**
	 * The PostgreSQL name of a vecDSL index: PostgreSQL scopes index names to the schema, vecDSL to the collection, so the name
	 * carries the collection's declared name (a renamed table keeps the names of its indexes).
	 */
	def static String indexName(VectorCollection c, String index) {
		val name = c.name + '_' + index
		if (name.length > 63) throw Unsupported.of('the index name ' + name + '; PostgreSQL names have at most 63 bytes')
		name
	}

	def private List<String> partitions(VectorCollection c, Table table, boolean ifNotExists) {
		val List<String> result = newArrayList
		if (table.partition === null) return result
		for (partition : ModelUtil.partitions(c)) {
			if (partition.key !== null || (written(partition, 'strategy') && partition.strategy.literal != 'list')) {
				throw Unsupported.of('partitions by key or with the ' + partition.strategy.literal + ' strategy; has no pgvector mapping yet (declarative hash and range partitioning)')
			}
			if (!partition.subPartitions.empty) throw Unsupported.of('sub-partitions; has no pgvector mapping yet (sub-partitioned tables)')
			result += PgvectorSql.createPartition(table, partition.name, false, ifNotExists)
		}
		result += PgvectorSql.createPartition(table, DEFAULT_PARTITION, true, ifNotExists)
		result
	}

	/** The partition a write goes to without a partition clause: the collection's default-partition, else the default partition. */
	def static String defaultPartition(VectorCollection c) {
		if (c.defaultPartition !== null) c.defaultPartition.name else null
	}

	def String vectorIndex(VectorCollection c, Table table, VectorField field, VectorIndex index, boolean ifNotExists) {
		val column = table.column(ModelUtil.fieldName(field))
		val Map<String, Integer> options = newLinkedHashMap
		note(index, 'onDisk', 'online')
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; pgvector builds and searches indexes on the CPU')
		for (name : ModelUtil.extParams(index, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (index.family.literal == 'flat') {
			if (index.quantization !== null) throw Unsupported.of('quantization without an index; pgvector quantizes through hnsw and ivfflat expression indexes')
			return null
		}
		val method = switch index.family {
			case AUTO,
			case HNSW: {
				if (written(index, 'centroids')) throw Unsupported.of('centroids on an hnsw index; lists belong to ivf indexes')
				if (written(index, 'maxDegree')) options.put('m', index.maxDegree)
				if (written(index, 'buildCandidates')) options.put('ef_construction', index.buildCandidates)
				'hnsw'
			}
			case IVF: {
				if (written(index, 'maxDegree') || written(index, 'buildCandidates')) throw Unsupported.of('max-degree and build-candidates on an ivf index; IVFFlat takes lists only')
				if (written(index, 'centroids')) options.put('lists', index.centroids)
				'ivfflat'
			}
			default: throw Unsupported.of(index.family.literal + ' indexes; pgvector builds hnsw and ivfflat indexes')
		}
		val metric = if (written(index, 'metric')) index.metric.literal else column.vec.metric
		val indexed = new Column(column.name, column.type, null, false, new Vec(column.vec.type, column.vec.dim, metric, column.vec.quantization,
			column.vec.oversampling), null, column.nullable, null, #[])
		PgvectorSql.createVectorIndex(table, indexName(c, index.name), indexed, method, options, ifNotExists)
	}

	def String payloadIndex(VectorCollection c, Table table, PayloadIndex index, boolean ifNotExists) {
		val path = index.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val column = table.column(ModelUtil.fieldName(path.field))
		note(index, 'onDisk', 'lowercase', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		for (name : ModelUtil.extParams(index, PgvectorGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (written(index, 'structure') && index.structure.literal == 'bitmap') {
			throw Unsupported.of('bitmap payload indexes; PostgreSQL builds bitmaps at query time and has no bitmap index type')
		}
		val segments = path.subFields.map[ModelUtil.fieldName(it)].toList
		switch index.kind {
			case FULL_TEXT: {
				if (column.config === null) throw Unsupported.of('a full_text index on ' + column.name + ', which is not a text field')
				PgvectorSql.createPayloadIndex(table, indexName(c, index.name), column, #[], 'fts', ifNotExists)
			}
			case GEO: throw Unsupported.of('geo payload indexes; has no pgvector mapping yet (earthdistance in contrib)')
			case JSON: {
				if (!column.json) throw Unsupported.of('a json index on ' + column.name + ', which is not a json field')
				PgvectorSql.createPayloadIndex(table, indexName(c, index.name), column, segments, 'gin', ifNotExists)
			}
			default: {
				if (!segments.empty && !column.json) throw Unsupported.of('a path index below ' + column.name + ', which is not a json field')
				PgvectorSql.createPayloadIndex(table, indexName(c, index.name), column, segments, if (written(index, 'structure')) index.structure.literal else 'auto', ifNotExists)
			}
		}
	}

	/** A text search configuration copied from the built-in configuration of the analyzer's language (simple without one). */
	def List<Stmt> createAnalyzer(Analyzer analyzer, String database) {
		if (analyzer.tokenizer.literal != 'standard' && analyzer.tokenizer.literal != 'language') {
			throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; PostgreSQL ships one text search parser (default), which splits words like the standard tokenizer')
		}
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) throw Unsupported.of(PgvectorStatements.NO_BM25)
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; text search dictionaries read files from the server\'s tsearch_data directory, which the driver cannot write')
		val source = if (analyzer.language === null) 'simple' else PgvectorSql.configuration(analyzer.language)
		if (source === null) throw Unsupported.of('the language ' + analyzer.language + '; PostgreSQL has no built-in text search configuration for it')
		if (written(analyzer, 'stopwords') && analyzer.stopwords != source && analyzer.stopwords != 'none') {
			throw Unsupported.of('stopwords ' + analyzer.stopwords + '; a text search configuration uses the stop-word list of its language')
		}
		if (analyzer.stopwords == 'none' && source != 'simple') throw Unsupported.of('stopwords none with a language; the snowball dictionaries of PostgreSQL remove the stop words of their language')
		for (filter : analyzer.filters) checkFilter(filter.type.literal, source)
		val create = 'CREATE TEXT SEARCH CONFIGURATION public.' + PgvectorSql.id(analyzer.name) + ' (COPY = pg_catalog.' + source + ')'
		val q = new Query
		val check = 'SELECT to_json(EXISTS (SELECT 1 FROM pg_ts_config c JOIN pg_namespace n ON n.oid = c.cfgnamespace WHERE n.nspname = \'public\' AND c.cfgname = '
			+ q.bind(text(analyzer.name), 'text') + '))'
		newArrayList(new Code.When(call('exists', check, q, null, database), true, #[execute(#[create], database)]) as Stmt)
	}

	/** Filters a copied configuration already applies: lowercase always, stop and stemmer with a language. */
	def static void checkFilter(String filter, String source) {
		switch filter {
			case 'lowercase': {}
			case 'stop',
			case 'stemmer': if (source == 'simple') throw Unsupported.of('the ' + filter + ' filter without a language; the simple configuration only lowercases')
			case 'ascii_folding': throw Unsupported.of('the ascii_folding filter; has no pgvector mapping yet (the unaccent dictionary in contrib)')
			default: throw Unsupported.of('the ' + filter + ' filter; PostgreSQL text search dictionaries for it read files from the server\'s tsearch_data directory')
		}
	}

	def static String createView(String alias, Table table, boolean ifNotExists) {
		'CREATE ' + (if (ifNotExists) 'OR REPLACE ' else '') + 'VIEW ' + PgvectorSql.id(alias) + ' AS SELECT * FROM ' + PgvectorSql.table(table)
	}

	// ---------------------------------------------------------------- code

	/** One runtime call with SQL text and parameters; settings and database only when present. */
	def static Code.Call call(String op, String sql, Query q, Map<String, String> settings, String database) {
		val args = kw('sql', text(sql))
		if (q !== null && q.size > 0) args.put('params', q.params)
		if (settings !== null && !settings.empty) args.put('settings', PgvectorSql.settings(settings))
		if (database !== null) args.put('database', text(database))
		runtime(op, args)
	}

	/** DDL statements in one transaction. */
	def static Stmt execute(List<String> statements, String database) {
		val args = kw('steps', items(statements.filterNull.map[sql | kw('sql', text(sql)) as Expr].toList))
		if (database !== null) args.put('database', text(database))
		new Code.Run(runtime('execute', args))
	}

	/** A statement outside a transaction block; closes names a database whose connection must close first. */
	def static Stmt autocommit(String sql, String database, String closes) {
		val args = kw('sql', text(sql))
		if (database !== null) args.put('database', text(database))
		if (closes !== null) args.put('closes', text(closes))
		new Code.Run(runtime('autocommit', args))
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
