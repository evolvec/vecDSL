package org.ftn.vecDSL.generator.oracledb

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
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Column
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Query
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Table
import org.ftn.vecDSL.generator.oracledb.OracledbSql.Vec
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.Field
import org.ftn.vecDSL.vecDSL.PayloadField
import org.ftn.vecDSL.vecDSL.PayloadIndex
import org.ftn.vecDSL.vecDSL.Quantization
import org.ftn.vecDSL.vecDSL.Script
import org.ftn.vecDSL.vecDSL.VectorCollection
import org.ftn.vecDSL.vecDSL.VectorField
import org.ftn.vecDSL.vecDSL.VectorIndex

import static org.ftn.vecDSL.generator.code.Code.*
import static org.ftn.vecDSL.util.ModelUtil.written

/**
 * Maps vecDSL collections to Oracle AI Database 26ai tables. Every field is a typed column in declaration order: payload fields
 * as SQL types (nested, json and array fields as JSON columns), vector fields as VECTOR columns with their dimension and format,
 * each with its own vector index; full-text fields carry an Oracle Text CONTEXT index synchronized on commit. The primary key is
 * the key column; tenant, namespace and partition names live in the partition-key field or in hidden VARCHAR2 columns. A logical
 * database is a table-name prefix (database__collection) in the program's schema.
 */
class OracledbSchema {

	public static val TENANT_COLUMN = 'vecdsl_tenant'
	public static val NAMESPACE_COLUMN = 'vecdsl_namespace'
	public static val PARTITION_COLUMN = 'vecdsl_partition'
	public static val DEFAULT_PARTITION = 'default'
	public static val DATABASE_SEPARATOR = '__'

	static val SCALARS = #{EScalarDType.INT -> 'NUMBER(10)', EScalarDType.BIGINT -> 'NUMBER(19)', EScalarDType.INT8 -> 'NUMBER(3)',
		EScalarDType.FLOAT -> 'BINARY_FLOAT', EScalarDType.DOUBLE -> 'BINARY_DOUBLE', EScalarDType.BOOL -> 'BOOLEAN',
		EScalarDType.VARCHAR -> 'VARCHAR2(4000)', EScalarDType.TEXT -> 'VARCHAR2(4000)', EScalarDType.DATE -> 'DATE',
		EScalarDType.DATETIME -> 'TIMESTAMP', EScalarDType.TIMESTAMPTZ -> 'TIMESTAMP WITH TIME ZONE', EScalarDType.UUID -> 'VARCHAR2(36)',
		EScalarDType.JSON -> 'JSON', EScalarDType.MAP -> 'JSON', EScalarDType.STRUCT -> 'JSON'}
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

	/** The table of a collection as the statements so far leave it (alters change columns and the name). */
	def Table table(VectorCollection c) {
		evolved.get(c) ?: declared(c)
	}

	def void evolve(VectorCollection c, Table table) {
		if (table === null) evolved.remove(c) else evolved.put(c, table)
	}

	def private Table declared(VectorCollection c) {
		val key = c.primaryKey
		if (!(key instanceof PayloadField) || !KEYS.contains((key as PayloadField).type)) {
			throw Unsupported.of('a primary key of type ' + (if (key instanceof PayloadField) (key as PayloadField).type.literal else 'vector')
				+ '; the generator keys tables by an integer, text or uuid column (Oracle cannot key a table by a VECTOR column)')
		}
		val List<Column> columns = newArrayList
		for (field : c.fields) columns += column(c, field)
		val partitionKey = ModelUtil.payloadFields(c).findFirst[partitionKey]
		val tenant = if (!c.tenants.empty || tenanted.contains(c)) (if (partitionKey === null) TENANT_COLUMN else partitionKey.name)
		val namespace = if (!c.namespaces.empty || namespaced.contains(c)) (if (partitionKey === null) NAMESPACE_COLUMN else partitionKey.name)
		val partition = if (ModelUtil.partitions(c).empty) null else PARTITION_COLUMN
		val database = databaseOf(c)
		new Table(database, physical(database, c.name), ModelUtil.fieldName(key), columns, tenant, namespace, partition, emptyMap)
	}

	/** The table name of a collection: prefixed with its logical database. */
	def static String physical(String database, String collection) {
		val name = if (database === null) collection else database + DATABASE_SEPARATOR + collection
		OracledbSql.id(name)
		name
	}

	/** The logical database of a collection declared in a logical-db, else null for the program's schema. */
	def static String databaseOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) return null
		if (database.name.contains(DATABASE_SEPARATOR)) throw Unsupported.of('the logical database name ' + database.name + '; the generator separates database and collection names with ' + DATABASE_SEPARATOR)
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
		OracledbSql.id(name)
		note(field, 'nullable')
		val nested = !field.subFields.empty || field.nested
		val lexer = lexerOf(c, field)
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		if (nested || field.type == EScalarDType.JSON || field.type == EScalarDType.MAP || field.type == EScalarDType.STRUCT) {
			if (lexer !== null) throw Unsupported.of('full-text search on json field ' + name)
			return new Column(name, 'JSON', null, true, null, null, true, defaultOf(field), checks)
		}
		if (field.type == EScalarDType.ARRAY) {
			if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			val element = scalar(field.elementType, name)
			if (element == 'JSON') throw Unsupported.of('arrays of ' + field.elementType.literal + ' in field ' + name)
			if (lexer !== null) throw Unsupported.of('full-text search on array field ' + name)
			return new Column(name, 'JSON', OracledbSql.kindOf(element), true, null, null, true, defaultOf(field), checks)
		}
		val base = scalar(field.type, name)
		val sqlType = if (written(field, 'maxLength') && base.startsWith('VARCHAR2')) {
			if (field.maxLength > 4000) throw Unsupported.of('max-length ' + field.maxLength + ' on ' + name + '; the generator writes VARCHAR2 columns of at most 4000 bytes, the limit of MAX_STRING_SIZE STANDARD')
			'VARCHAR2(' + field.maxLength + ')'
		} else base
		if (lexer !== null && !base.startsWith('VARCHAR2')) throw Unsupported.of('full-text search on ' + field.type.literal + ' field ' + name)
		new Column(name, sqlType, null, false, null, lexer, true, defaultOf(field), checks)
	}

	def private static Expr defaultOf(PayloadField field) {
		if (field.defaultValue === null) null else Literals.of(field.defaultValue)
	}

	def private String scalar(EScalarDType type, String field) {
		val sql = SCALARS.get(type)
		if (sql !== null) return sql
		switch type {
			case BYTES: throw Unsupported.of('bytes field ' + field + '; has no Oracle mapping yet (RAW and BLOB columns with base64 values in the three runtimes)')
			case GEO_POINT,
			case GEOMETRY: throw Unsupported.of(type.literal + ' field ' + field + '; has no Oracle mapping yet (SDO_GEOMETRY of Oracle Spatial)')
			default: throw Unsupported.of(type.literal + ' field ' + field + '; Oracle has no media column types (BLOB holds bytes without media semantics)')
		}
	}

	/** A field limit as a named CHECK constraint; {} stands for the column. */
	def static String check(org.ftn.vecDSL.vecDSL.FieldLimit limit, Field field) {
		val name = ModelUtil.fieldName(field)
		val condition = switch limit.type {
			case ARRAY_LEN: {
				if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) throw Unsupported.of('array_len on ' + name + ', which is not an array')
				'JSON_VALUE({}, \'$.size()\' RETURNING NUMBER) <= ' + limit.value
			}
			case MAX_SIZE: {
				if (field instanceof VectorField) throw Unsupported.of('max_size on vector field ' + name)
				'LENGTHB(JSON_SERIALIZE(JSON_SCALAR({}) RETURNING CLOB)) <= ' + limit.value
			}
			case VECTOR_DIM: {
				if (!(field instanceof VectorField)) throw Unsupported.of('vector_dim on payload field ' + name)
				'VECTOR_DIMENSION_COUNT({}) <= ' + limit.value
			}
		}
		'CONSTRAINT ' + OracledbSql.id(limit.name) + ' CHECK (' + condition + ')'
	}

	/**
	 * The Oracle Text lexer of a full-text field: "" for the default lexer, the analyzer's lexer preference otherwise; null for a
	 * field without full-text search.
	 */
	def String lexerOf(VectorCollection c, PayloadField field) {
		val indexed = c.indexes.filter(PayloadIndex).filter[target.field === field && kind == EPayloadIndexKind.FULL_TEXT].toList
		if (!field.fullText && !field.documentBody && field.analyzer === null && indexed.empty) return null
		val analyzer = field.analyzer ?: indexed.map[it.analyzer].filterNull.head
		if (analyzer === null) '' else lexerName(analyzer)
	}

	/**
	 * An analyzer is a BASIC_LEXER preference of the program's user, named like the analyzer. Oracle Text stores preference names in
	 * upper case and reads them unquoted in index parameters, so the name must be a plain identifier.
	 */
	def static String lexerName(Analyzer analyzer) {
		if (!analyzer.name.matches('[A-Za-z][A-Za-z0-9_]*')) throw Unsupported.of('the analyzer name ' + analyzer.name + '; the generator names lexer preferences with plain identifiers')
		analyzer.name
	}

	def private Column vectorColumn(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		OracledbSql.id(name)
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; an Oracle VECTOR column holds one vector (multi-vector search ranks rows of chunks with PARTITIONS BY)')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) {
			throw Unsupported.of('server-side embedding of ' + name + OracledbStatements.NO_MODELS)
		}
		if (written(field, 'modifier') && field.modifier.literal == 'idf') throw Unsupported.of('the idf modifier on ' + name + '; Oracle stores sparse vectors as given')
		note(field, 'normalized', 'onDisk')
		var sparse = false
		val format = switch field.type {
			case FLOAT_VECTOR: 'FLOAT32'
			case FLOAT64_VECTOR: 'FLOAT64'
			case INT8_VECTOR: 'INT8'
			case BINARY_VECTOR: 'BINARY'
			case SPARSE_FLOAT_VECTOR: {
				sparse = true
				'FLOAT32'
			}
			default: throw Unsupported.of(field.type.literal + ' fields; Oracle VECTOR dimensions are INT8, FLOAT32, FLOAT64 or BINARY')
		}
		if (!sparse && field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		if (format == 'BINARY' && field.dimension % 8 != 0) throw Unsupported.of('a binary vector field with ' + field.dimension + ' dimensions; BINARY vectors have a multiple of 8 dimensions')
		val index = indexOf(c, field)
		val metric = metric(field, index)
		val family = if (index === null || index.family.literal == 'flat') null else if (index.family.literal == 'ivf') 'ivf' else 'hnsw'
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		new Column(name, 'VECTOR', null, false, new Vec(format, field.dimension, sparse, metric, family), null, field.nullable, null, checks)
	}

	def VectorIndex indexOf(VectorCollection c, VectorField field) {
		ModelUtil.vectorIndex(c, field)
	}

	/** The metric of a vector field: the field's, else its index's, else cosine (hamming for binary fields). */
	def String metric(VectorField field, VectorIndex index) {
		if (written(field, 'metric')) return field.metric.literal
		if (index !== null && written(index, 'metric')) return index.metric.literal
		if (field.type.literal == 'binary_vector') 'hamming' else EMetric.COSINE.literal
	}

	// ---------------------------------------------------------------- DDL

	/**
	 * A collection's table with its objects: lexer preferences of its analyzers, the table, its vector and payload indexes, the
	 * CONTEXT indexes of full-text fields, the comment and the views of its aliases. Oracle commits each DDL statement on its own,
	 * so a failure leaves the objects created before it.
	 */
	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.external !== null) throw Unsupported.of(OracledbStatements.EXTERNAL)
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of(OracledbStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of('auto-id; has no Oracle mapping yet (identity columns returned to the program)')
		if (c.dynamic) throw Unsupported.of('dynamic; has no Oracle mapping yet (a JSON column for undeclared fields)')
		if (c.deletionProtection) throw Unsupported.of(OracledbStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of('cross-ref; has no Oracle mapping yet (foreign keys)')
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + OracledbStatements.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of(OracledbStatements.NO_STORAGE)
		if (!c.tags.empty) throw Unsupported.of(OracledbStatements.NO_TAGS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + OracledbStatements.functionReason(c.functions.head.type.literal))
		if (!c.limits.empty) throw Unsupported.of(OracledbStatements.collectionLimit(c.limits.head.type.literal))
		for (name : ModelUtil.extParams(c, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency')
		if (fresh) evolve(c, null)
		val table = table(c)
		val List<Stmt> result = newArrayList
		for (analyzer : c.analyzers) result += createAnalyzer(analyzer)
		result += execute(#[OracledbSql.createTable(table, partitionNames(c, table), ifNotExists)])
		for (field : ModelUtil.vectorFields(c).filter[present(table, it)]) {
			val index = indexOf(c, field)
			if (index !== null) {
				val sql = vectorIndex(c, table, field, index)
				if (sql !== null) result += guarded(indexName(c, index.name), sql)
			}
		}
		for (declared : c.indexes.filter(PayloadIndex).filter[target.field === null || present(table, target.field)]) {
			result += guarded(indexName(c, declared.name), payloadIndex(c, table, declared))
		}
		for (field : ModelUtil.payloadFields(c).filter[present(table, it)]) {
			val column = table.column(ModelUtil.fieldName(field))
			if (column.text && !c.indexes.filter(PayloadIndex).exists[target.field === field && kind == EPayloadIndexKind.FULL_TEXT]) {
				result += guarded(textIndexName(c, field), OracledbSql.createPayloadIndex(table, textIndexName(c, field), column, #[], null, 'text'))
			}
		}
		if (fresh || !ifNotExists) {
			if (c.description !== null) result += execute(#['COMMENT ON TABLE ' + OracledbSql.table(table) + ' IS ' + OracledbSql.literal(c.description)])
			for (alias : c.aliases) result += execute(#[createView(alias.name, table)])
		}
		result
	}

	/** An index created unless an index of that name exists (USER_INDEXES), so that a rerun of an if-not-exists create succeeds. */
	def static Stmt guarded(String index, String sql) {
		new Code.When(indexExists(#[index]), true, #[execute(#[sql])])
	}

	/** An index created unless one of the names exists: a CONTEXT index of a column may exist under its implicit name. */
	def static Stmt guarded(List<String> names, String sql) {
		new Code.When(indexExists(names), true, #[execute(#[sql])])
	}

	def static Code.Call indexExists(List<String> names) {
		val q = new Query
		call('exists', 'SELECT ' + OracledbQueries.scalar('COUNT(*)') + ' FROM user_indexes WHERE index_name '
			+ (if (names.size == 1) '= ' + q.bind(text(names.head), 'string') else 'IN (' + names.map[name | q.bind(text(name), 'string')].join(', ') + ')'), q, null)
	}

	def private static boolean present(Table table, Field field) {
		val name = table.renames.getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field))
		table.columns.exists[column | column.name == name]
	}

	/** The CONTEXT index of a full-text field without a declared full_text index. */
	def static String textIndexName(VectorCollection c, PayloadField field) {
		indexName(c, ModelUtil.fieldName(field) + '_text')
	}

	/** The Oracle name of a vecDSL index: index names belong to the schema, so the name carries the collection's table name. */
	def static String indexName(VectorCollection c, String index) {
		val database = databaseOf(c)
		val name = physical(database, c.name) + '_' + index
		OracledbSql.id(name)
		name
	}

	def private List<String> partitionNames(VectorCollection c, Table table) {
		val List<String> result = newArrayList
		if (table.partition === null) return result
		for (partition : ModelUtil.partitions(c)) {
			if (partition.key !== null || (written(partition, 'strategy') && partition.strategy.literal != 'list')) {
				throw Unsupported.of('partitions by key or with the ' + partition.strategy.literal + ' strategy; has no Oracle mapping yet (hash and range partitioning)')
			}
			if (!partition.subPartitions.empty) throw Unsupported.of('sub-partitions; has no Oracle mapping yet (composite partitioning)')
			OracledbSql.id(partition.name)
			result += partition.name
		}
		if (!result.contains(DEFAULT_PARTITION)) result += DEFAULT_PARTITION
		result
	}

	def static String defaultPartition(VectorCollection c) {
		if (c.defaultPartition !== null) c.defaultPartition.name else null
	}

	/** The vector index of a field: null for a flat index (exact search needs none). */
	def String vectorIndex(VectorCollection c, Table table, VectorField field, VectorIndex index) {
		val column = table.column(ModelUtil.fieldName(field))
		val Map<String, Integer> options = newLinkedHashMap
		note(index, 'onDisk', 'online')
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; Oracle builds and searches vector indexes on the database server\'s CPUs')
		for (name : ModelUtil.extParams(index, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (index.family.literal == 'flat') {
			if (index.quantization !== null && index.quantization.type.literal != 'none') throw Unsupported.of('quantization of a flat index; Oracle quantizes HNSW vector indexes')
			return null
		}
		if (column.vec.sparse) throw Unsupported.of('a vector index on sparse field ' + column.name + '; has no Oracle mapping yet (sparse vector indexes)')
		val family = switch index.family {
			case AUTO,
			case HNSW: {
				if (written(index, 'centroids')) throw Unsupported.of('centroids on an hnsw index; neighbor partitions belong to ivf indexes')
				if (written(index, 'maxDegree')) options.put('NEIGHBORS', index.maxDegree)
				if (written(index, 'buildCandidates')) options.put('EFCONSTRUCTION', index.buildCandidates)
				'hnsw'
			}
			case IVF: {
				if (written(index, 'maxDegree') || written(index, 'buildCandidates')) throw Unsupported.of('max-degree and build-candidates on an ivf index; IVF takes neighbor partitions')
				if (written(index, 'centroids')) options.put('NEIGHBOR PARTITIONS', index.centroids)
				'ivf'
			}
			default: throw Unsupported.of(index.family.literal + ' indexes; Oracle builds HNSW (in-memory neighbor graph) and IVF (neighbor partition) vector indexes')
		}
		val metric = if (written(index, 'metric')) index.metric.literal else column.vec.metric
		val indexed = new Column(column.name, column.type, null, false, column.vec.withMetric(metric), null, column.nullable, null, #[])
		OracledbSql.createVectorIndex(table, indexName(c, index.name), indexed, family, options, null, compressionOf(index.quantization, family, column.vec.format, options))
	}

	/**
	 * Scalar quantization of an HNSW index as the compression ratio of the column's format to the quantized width (AI Vector Search
	 * User's Guide, HNSW Index Syntax and Parameters: ratio 2 or 4 for FLOAT32, 2, 4 or 8 for FLOAT64; 8 bits by default); oversampling
	 * as RESCORE FACTOR, an integer from 1 to 100. Null without quantization.
	 */
	def private Integer compressionOf(Quantization quantization, String family, String format, Map<String, Integer> options) {
		if (quantization === null || quantization.type.literal == 'none') return null
		note(quantization, 'subvectors', 'keepOriginals', 'description')
		for (name : ModelUtil.extParams(quantization, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (quantization.type.literal != 'scalar') throw Unsupported.of(quantization.type.literal + ' quantization; Oracle quantizes vector indexes with scalar quantization only')
		if (family != 'hnsw') throw Unsupported.of('quantization of an ivf index; Oracle quantizes HNSW vector indexes')
		if (written(quantization, 'truncateDimension')) throw Unsupported.of('truncate-dim; Oracle quantizes every dimension of the vector')
		val source = switch format { case 'FLOAT32': 32 case 'FLOAT64': 64 default: 0 }
		if (source == 0) throw Unsupported.of('quantization of ' + format + ' vectors; Oracle quantizes FLOAT32 and FLOAT64 vectors')
		val bits = if (written(quantization, 'bits')) quantization.bits else 8.0
		val ratio = source / bits
		if (ratio != Math.rint(ratio) || !#[2.0, 4.0, 8.0].contains(ratio) || (source == 32 && ratio == 8.0)) {
			throw Unsupported.of('scalar quantization to ' + bits + ' bits of ' + format + ' vectors; Oracle compresses FLOAT32 to 16 or 8 bits and FLOAT64 to 32, 16 or 8 bits')
		}
		if (written(quantization, 'oversampling')) {
			val factor = quantization.oversampling
			if (factor != Math.rint(factor) || factor < 1 || factor > 100) throw Unsupported.of('oversampling ' + factor + '; the Oracle rescore factor is an integer from 1 to 100')
			options.put('RESCORE FACTOR', factor as int)
		}
		ratio as int
	}

	def String payloadIndex(VectorCollection c, Table table, PayloadIndex index) {
		val path = index.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields and their sub-fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val column = table.column(ModelUtil.fieldName(path.field))
		note(index, 'onDisk', 'lowercase', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		for (name : ModelUtil.extParams(index, OracledbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		val segments = path.subFields.map[ModelUtil.fieldName(it)].toList
		if (written(index, 'structure') && index.structure.literal == 'bitmap') {
			if (!segments.empty || column.json) throw Unsupported.of('bitmap indexes on JSON values')
			return 'CREATE BITMAP INDEX ' + OracledbSql.id(indexName(c, index.name)) + ' ON ' + OracledbSql.table(table) + ' (' + OracledbSql.id(column.name) + ')'
		}
		switch index.kind {
			case FULL_TEXT: {
				if (!column.text) throw Unsupported.of('a full_text index on ' + column.name + ', which is not a text field')
				OracledbSql.createPayloadIndex(table, indexName(c, index.name), column, #[], null, 'text')
			}
			case GEO: throw Unsupported.of('geo payload indexes; has no Oracle mapping yet (Oracle Spatial)')
			case JSON: {
				if (!column.json) throw Unsupported.of('a json index on ' + column.name + ', which is not a json field')
				OracledbSql.createPayloadIndex(table, indexName(c, index.name), column, #[], null, 'json')
			}
			default: {
				if (!segments.empty && !column.json) throw Unsupported.of('a path index below ' + column.name + ', which is not a json field')
				val kind = if (segments.empty) null else OracledbFilter.leafKind(path)
				OracledbSql.createPayloadIndex(table, indexName(c, index.name), column, segments, kind, 'auto')
			}
		}
	}

	/**
	 * An analyzer as a BASIC_LEXER preference (CTX_DDL.CREATE_PREFERENCE): with a language, stemming of that language at index
	 * time (INDEX_STEMS); the default English stop list applies to every CONTEXT index.
	 */
	def List<Stmt> createAnalyzer(Analyzer analyzer) {
		if (analyzer.tokenizer.literal != 'standard' && analyzer.tokenizer.literal != 'language') {
			throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; has no Oracle mapping yet (lexer preferences other than BASIC_LEXER)')
		}
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) throw Unsupported.of(OracledbStatements.NO_BM25)
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; Oracle Text dictionaries and thesauri are loaded with ctxload, outside SQL')
		val stems = if (analyzer.language === null) null else stemmer(analyzer.language)
		if (analyzer.language !== null && stems === null) throw Unsupported.of('the language ' + analyzer.language + '; has no Oracle mapping yet (stemmers other than English)')
		if (written(analyzer, 'stopwords') && analyzer.stopwords != 'english') throw Unsupported.of('stopwords ' + analyzer.stopwords + '; has no Oracle mapping yet (stop list preferences)')
		for (filter : analyzer.filters) checkFilter(filter.type.literal, stems)
		val name = lexerName(analyzer)
		val List<String> calls = newArrayList
		calls += 'CTX_DDL.CREATE_PREFERENCE(' + OracledbSql.literal(name) + ', \'BASIC_LEXER\');'
		if (stems !== null) calls += 'CTX_DDL.SET_ATTRIBUTE(' + OracledbSql.literal(name) + ', \'INDEX_STEMS\', ' + OracledbSql.literal(stems) + ');'
		val q = new Query
		val check = 'SELECT ' + OracledbQueries.scalar('COUNT(*)') + ' FROM ctx_user_preferences WHERE pre_name = ' + q.bind(text(name.toUpperCase), 'string')
		newArrayList(new Code.When(call('exists', check, q, null), true, #[execute(#['BEGIN ' + calls.join(' ') + ' END;'])]) as Stmt)
	}

	/** The stemmer of a language name or code; Oracle Text's BASIC_LEXER stems English here. */
	def static String stemmer(String language) {
		val lower = language.toLowerCase
		if (lower == 'en' || lower == 'english' || lower.startsWith('en-')) 'ENGLISH' else null
	}

	/** Filters a lexer preference applies: lowercase always (BASIC_LEXER is case-insensitive by default), stop and stemmer with a language. */
	def static void checkFilter(String filter, String stems) {
		switch filter {
			case 'lowercase': {}
			case 'stop': {}
			case 'stemmer': if (stems === null) throw Unsupported.of('the stemmer filter without a language; BASIC_LEXER stems with INDEX_STEMS of a language')
			case 'ascii_folding': throw Unsupported.of('the ascii_folding filter; has no Oracle mapping yet (BASE_LETTER of BASIC_LEXER)')
			default: throw Unsupported.of('the ' + filter + ' filter; has no Oracle mapping yet (Oracle Text lexer attributes)')
		}
	}

	def static String createView(String alias, Table table) {
		'CREATE OR REPLACE VIEW ' + OracledbSql.id(alias) + ' AS SELECT * FROM ' + OracledbSql.table(table)
	}

	// ---------------------------------------------------------------- code

	/** One runtime call with SQL text and parameters (renumbered in textual order); settings only when present. */
	def static Code.Call call(String op, String sql, Query q, Map<String, String> settings) {
		val finished = OracledbSql.finish(sql, if (q === null) items(#[]) else q.params)
		val args = kw('sql', text(finished.text))
		if (!finished.params.items.empty) args.put('params', finished.params)
		if (settings !== null && !settings.empty) {
			val values = entries
			settings.forEach[name, value | values.put(name, text(value))]
			args.put('settings', values)
		}
		runtime(op, args)
	}

	/** DDL or PL/SQL statements without parameters, run in order (Oracle commits each DDL statement). */
	def static Stmt execute(List<String> statements) {
		new Code.Run(runtime('execute', kw('steps', items(statements.filterNull.map[sql | kw('sql', text(sql)) as Expr].toList))))
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
