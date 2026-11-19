package org.ftn.vecDSL.generator.duckdb

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
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Analysis
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Column
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Query
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Table
import org.ftn.vecDSL.generator.duckdb.DuckdbSql.Vec
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Analyzer
import org.ftn.vecDSL.vecDSL.Database
import org.ftn.vecDSL.vecDSL.EMetric
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind
import org.ftn.vecDSL.vecDSL.EScalarDType
import org.ftn.vecDSL.vecDSL.ExternalSource
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
 * Maps vecDSL collections to DuckDB tables (DuckDB 2.0.0 with vss and fts). Every field is a typed column in declaration
 * order: payload fields as SQL types (nested and json fields as JSON, arrays as lists), vector fields as FLOAT[dim] columns,
 * each with its own HNSW index. The primary key is the key column; tenant and namespace names live in the partition-key field
 * or in hidden VARCHAR columns. A logical database is a schema of the database file. The full-text fields of a table share one
 * fts index, rebuilt after every write.
 */
class DuckdbSchema {

	public static val TENANT_COLUMN = 'vecdsl_tenant'
	public static val NAMESPACE_COLUMN = 'vecdsl_namespace'

	static val SCALARS = #{EScalarDType.INT -> 'INTEGER', EScalarDType.BIGINT -> 'BIGINT', EScalarDType.INT8 -> 'TINYINT',
		EScalarDType.FLOAT -> 'FLOAT', EScalarDType.DOUBLE -> 'DOUBLE', EScalarDType.BOOL -> 'BOOLEAN',
		EScalarDType.VARCHAR -> 'VARCHAR', EScalarDType.TEXT -> 'VARCHAR', EScalarDType.DATE -> 'DATE', EScalarDType.DATETIME -> 'TIMESTAMP',
		EScalarDType.TIMESTAMPTZ -> 'TIMESTAMPTZ', EScalarDType.UUID -> 'UUID', EScalarDType.BYTES -> 'BLOB', EScalarDType.JSON -> 'JSON',
		EScalarDType.MAP -> 'JSON', EScalarDType.STRUCT -> 'JSON'}
	static val KEYS = #[EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID]

	val List<String> notes
	val Set<VectorCollection> tenanted
	val Set<VectorCollection> namespaced
	val Map<VectorCollection, Table> evolved = newHashMap
	val Map<String, VectorCollection> views = newLinkedHashMap

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
		new Table(schemaOf(c), c.name, ModelUtil.fieldName(key), columns, tenant, namespace, emptyMap)
	}

	/** The schema of a collection: its logical database, else main. */
	def static String schemaOf(VectorCollection c) {
		val database = EcoreUtil2.getContainerOfType(c, Database)
		if (database === null) return DuckdbSql.MAIN
		DuckdbSql.id(database.name)
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
		DuckdbSql.id(name)
		note(field, 'nullable')
		val nested = !field.subFields.empty || field.nested
		val analysis = analysis(c, field)
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		if (nested || field.type == EScalarDType.JSON || field.type == EScalarDType.MAP || field.type == EScalarDType.STRUCT) {
			if (analysis !== null) throw Unsupported.of('full-text search on json field ' + name)
			return new Column(name, 'JSON', null, true, null, null, true, defaultOf(field), checks)
		}
		if (field.type == EScalarDType.ARRAY) {
			if (!written(field, 'elementType')) throw Unsupported.of('array field ' + name + ' without element-type')
			val element = scalar(field.elementType, name)
			if (element == 'JSON' || element == 'BLOB') throw Unsupported.of('arrays of ' + field.elementType.literal + ' in field ' + name)
			return new Column(name, element + '[]', element, false, null, null, true, defaultOf(field), checks)
		}
		val sqlType = scalar(field.type, name)
		if (written(field, 'maxLength') && sqlType == 'VARCHAR') checks += 'CHECK (length({}) <= ' + field.maxLength + ')'
		if (analysis !== null && sqlType != 'VARCHAR') throw Unsupported.of('full-text search on ' + field.type.literal + ' field ' + name)
		new Column(name, sqlType, null, false, null, analysis, true, defaultOf(field), checks)
	}

	def private static Expr defaultOf(PayloadField field) {
		if (field.defaultValue === null) null else Literals.of(field.defaultValue)
	}

	def private String scalar(EScalarDType type, String field) {
		val sql = SCALARS.get(type)
		if (sql !== null) return sql
		switch type {
			case GEO_POINT: throw Unsupported.of('geo_point field ' + field + '; ' + DuckdbStatements.NO_SPATIAL)
			case GEOMETRY: throw Unsupported.of('geometry field ' + field + '; ' + DuckdbStatements.NO_SPATIAL)
			default: throw Unsupported.of(type.literal + ' field ' + field + '; DuckDB has no media types (BLOB holds bytes without media semantics)')
		}
	}

	/** A field limit as a CHECK constraint of the column; {} stands for the column. */
	def static String check(org.ftn.vecDSL.vecDSL.FieldLimit limit, Field field) {
		val name = ModelUtil.fieldName(field)
		val condition = switch limit.type {
			case ARRAY_LEN: {
				if (!(field instanceof PayloadField) || (field as PayloadField).type != EScalarDType.ARRAY) throw Unsupported.of('array_len on ' + name + ', which is not an array')
				'len({}) <= ' + limit.value
			}
			case MAX_SIZE: {
				if (field instanceof VectorField) throw Unsupported.of('max_size on vector field ' + name)
				'octet_length(CAST({} AS VARCHAR)::BLOB) <= ' + limit.value
			}
			case VECTOR_DIM: {
				if (!(field instanceof VectorField)) throw Unsupported.of('vector_dim on payload field ' + name)
				'array_length({}) <= ' + limit.value
			}
		}
		'CONSTRAINT ' + DuckdbSql.id(limit.name) + ' CHECK (' + condition + ')'
	}

	/**
	 * The text analysis of a full-text field, null for other fields: its analyzer's stemmer and stop words, else no stemming,
	 * no stop words and lower case, which is what the other generators do without an analyzer.
	 */
	def Analysis analysis(VectorCollection c, PayloadField field) {
		val indexed = c.indexes.filter(PayloadIndex).filter[target.field === field && kind == EPayloadIndexKind.FULL_TEXT].toList
		if (!field.fullText && !field.documentBody && field.analyzer === null && indexed.empty) return null
		val analyzer = field.analyzer ?: indexed.map[it.analyzer].filterNull.head
		if (analyzer === null) Analysis.DEFAULT else analysis(analyzer)
	}

	/** The fts options of an analyzer: the language's stemmer, its stop words (English only), accents stripped by ascii_folding. */
	def static Analysis analysis(Analyzer analyzer) {
		if (analyzer.tokenizer.literal != 'standard' && analyzer.tokenizer.literal != 'language') {
			throw Unsupported.of('the ' + analyzer.tokenizer.literal + ' tokenizer; the fts extension splits text with one regular expression (ignore option)')
		}
		if (!analyzer.resources.empty) throw Unsupported.of('analyzer resources; the fts extension reads stop words from a table, not from files')
		val stemmer = if (analyzer.language === null) 'none' else DuckdbSql.stemmer(analyzer.language)
		if (stemmer === null) throw Unsupported.of('the language ' + analyzer.language + '; the fts extension has no stemmer for it')
		val filters = analyzer.filters.map[type.literal].toList
		for (filter : filters) {
			switch filter {
				case 'lowercase', case 'ascii_folding': {}
				case 'stop', case 'stemmer': if (stemmer == 'none') throw Unsupported.of('the ' + filter + ' filter without a language')
				default: throw Unsupported.of('the ' + filter + ' filter; the fts extension lowercases, strips accents, stems and removes stop words only')
			}
		}
		val stopwords = if (written(analyzer, 'stopwords')) analyzer.stopwords else if (stemmer == 'english') 'english' else 'none'
		if (stopwords != 'none' && stopwords != 'english' && stopwords != 'en') {
			throw Unsupported.of('stopwords ' + stopwords + '; the fts extension has an English stop-word list built in, other lists are tables')
		}
		if (written(analyzer, 'bm25K1') || written(analyzer, 'bm25B')) {
			throw Unsupported.of('BM25 parameters on an analyzer; ' + DuckdbStatements.PENDING + ' (the k and b arguments of match_bm25)')
		}
		new Analysis(stemmer, if (stopwords == 'none') 'none' else 'english', filters.contains('ascii_folding'), true)
	}

	def private Column vectorColumn(VectorCollection c, VectorField field) {
		val name = ModelUtil.fieldName(field)
		DuckdbSql.id(name)
		if (field.multiVector) throw Unsupported.of('multi-vector field ' + name + '; a DuckDB array column holds one vector')
		if (field.model !== null || ModelUtil.producingFunction(c, field) !== null) {
			throw Unsupported.of('server-side embedding of ' + name + DuckdbStatements.NO_MODELS)
		}
		note(field, 'normalized', 'onDisk', 'modifier')
		if (field.type.literal != 'float_vector') {
			throw Unsupported.of(field.type.literal + ' fields; ' + DuckdbStatements.FLOAT_ONLY)
		}
		if (field.dimension <= 0) throw Unsupported.of('vector field ' + name + ' without dim')
		val index = indexOf(c, field)
		val List<String> checks = newArrayList
		for (limit : field.limits) checks += check(limit, field)
		new Column(name, 'FLOAT[' + field.dimension + ']', null, false, new Vec(field.dimension, metric(field, index)), null, field.nullable, null, checks)
	}

	/** The vector index of a field: declared on the field or in the collection. */
	def VectorIndex indexOf(VectorCollection c, VectorField field) {
		ModelUtil.vectorIndex(c, field)
	}

	/** The metric of a vector field: the field's, else its index's, else cosine. */
	def String metric(VectorField field, VectorIndex index) {
		val metric = if (written(field, 'metric')) field.metric else if (index !== null && written(index, 'metric')) index.metric else EMetric.COSINE
		DuckdbSql.distance(metric.literal)
		metric.literal
	}

	// ---------------------------------------------------------------- DDL

	/**
	 * A collection's table with its objects in one transaction. A declaration (fresh) starts from the declared fields; a create
	 * that names an existing collection keeps the columns earlier alter statements changed, and skips fields and indexes they
	 * dropped.
	 */
	def List<Stmt> createCollection(VectorCollection c, boolean ifNotExists, boolean fresh) {
		if (c.ttlField !== null || written(c, 'ttlSeconds')) throw Unsupported.of(DuckdbStatements.NO_TTL)
		if (c.autoId) throw Unsupported.of(DuckdbStatements.NO_AUTO_ID)
		if (c.dynamic) throw Unsupported.of(DuckdbStatements.NO_DYNAMIC)
		if (c.deletionProtection) throw Unsupported.of(DuckdbStatements.NO_DELETION_PROTECTION)
		if (!c.crossReferences.empty) throw Unsupported.of(DuckdbStatements.NO_CROSS_REF)
		if (c.embeddingModel !== null) throw Unsupported.of('embedding-model' + DuckdbStatements.NO_MODELS)
		if (c.storage !== null) throw Unsupported.of(DuckdbStatements.NO_STORAGE)
		if (!c.tags.empty) throw Unsupported.of(DuckdbStatements.NO_TAGS)
		if (!c.functions.empty) throw Unsupported.of(c.functions.head.type.literal + ' functions; ' + DuckdbStatements.functionReason(c.functions.head.type.literal))
		if (!c.limits.empty) throw Unsupported.of(DuckdbStatements.collectionLimit(c.limits.head.type.literal))
		if (!ModelUtil.partitions(c).empty) throw Unsupported.of(DuckdbStatements.NO_PARTITIONS)
		for (name : ModelUtil.extParams(c, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		note(c, 'idType', 'storedSourceFields', 'generativeModel', 'consistency')
		if (fresh) evolve(c, null)
		val table = table(c)
		for (analyzer : c.analyzers) analysis(analyzer)
		val List<String> ddl = newArrayList
		if (table.schema != DuckdbSql.MAIN) ddl += 'CREATE SCHEMA IF NOT EXISTS ' + DuckdbSql.id(table.schema)
		ddl += DuckdbSql.createTable(table, ifNotExists)
		val List<String> rest = newArrayList
		rest += indexes(c, table, ifNotExists)
		if (fresh || !ifNotExists) {
			if (c.description !== null) rest += 'COMMENT ON TABLE ' + DuckdbSql.table(table) + ' IS ' + DuckdbSql.literal(c.description)
			for (alias : c.aliases) {
				rest += createView(alias.name, table, ifNotExists)
				view(alias.name, c)
			}
		}
		if (c.external !== null) return derived(c.external, table, ddl, rest, ifNotExists)
		ddl += rest
		#[execute(ddl)]
	}

	/**
	 * A collection derived from an external file: its table is filled by DuckDB's reader of the file (read_parquet, read_csv or
	 * read_json, the path bound) before the indexes are built, in one transaction; RefreshExternal reads the file again.
	 */
	def private List<Stmt> derived(ExternalSource source, Table table, List<String> ddl, List<String> rest, boolean ifNotExists) {
		val q = new Query
		val insert = DuckdbSql.insertFromFile(q, table, DuckdbSql.reader(source.location, if (written(source, 'format')) source.format.literal else null),
			DuckdbSql.localPath(source.location), newLinkedHashMap, if (ifNotExists) 'ignore' else 'error')
		val List<Expr> steps = newArrayList
		for (sql : ddl) steps.add(kw('sql', text(sql)))
		steps.add(kw('sql', text(insert), 'params', q.params))
		for (sql : rest) steps.add(kw('sql', text(sql)))
		#[new Code.Run(runtime('execute', kw('steps', items(steps))))]
	}

	/** The secondary indexes of a table: HNSW per indexed vector field, ART per declared payload index, the fts index. */
	def List<String> indexes(VectorCollection c, Table table, boolean ifNotExists) {
		val List<String> ddl = newArrayList
		for (field : ModelUtil.vectorFields(c).filter[present(table, it)]) {
			val index = indexOf(c, field)
			if (index !== null) ddl += vectorIndex(c, table, field, index, ifNotExists)
		}
		for (declared : c.indexes.filter(PayloadIndex).filter[target.field === null || present(table, target.field)]) {
			ddl += payloadIndex(c, table, declared, ifNotExists)
		}
		ddl += DuckdbSql.createFtsIndex(table)
		ddl.filterNull.toList
	}

	/** DROP INDEX for every secondary index of a table and its fts index, before an ALTER that DuckDB refuses while they exist. */
	def List<String> dropIndexes(VectorCollection c, Table table) {
		val List<String> ddl = newArrayList
		for (field : ModelUtil.vectorFields(c).filter[present(table, it)]) {
			val index = indexOf(c, field)
			if (index !== null && index.family.literal != 'flat') ddl += 'DROP INDEX IF EXISTS ' + DuckdbSql.qualified(table, indexName(c, index.name))
		}
		for (declared : c.indexes.filter(PayloadIndex).filter[kind != EPayloadIndexKind.FULL_TEXT]) {
			ddl += 'DROP INDEX IF EXISTS ' + DuckdbSql.qualified(table, indexName(c, declared.name))
		}
		if (!table.fullTextColumns.empty) ddl += 'DROP SCHEMA IF EXISTS ' + DuckdbSql.ftsSchema(table) + ' CASCADE'
		ddl
	}

	/** Records which collection a view reads after the statements mapped so far (create, add-alias, swap-alias, drop). */
	def void view(String alias, VectorCollection c) {
		if (c === null) views.remove(alias) else views.put(alias, c)
	}

	/** The views that read a collection now, which a renamed table must get again. */
	def List<String> viewsOf(VectorCollection c) {
		views.entrySet.filter[value === c].map[key].toList
	}

	/** Whether a field still has its column (an alter statement may have dropped it). */
	def static boolean present(Table table, Field field) {
		val name = table.renames.getOrDefault(ModelUtil.fieldName(field), ModelUtil.fieldName(field))
		table.columns.exists[column | column.name == name]
	}

	/** The DuckDB name of a vecDSL index: the collection's declared name and the index name (vecDSL scopes index names to the collection). */
	def static String indexName(VectorCollection c, String index) {
		c.name + '_' + index
	}

	/** An HNSW index (vss): max-degree is M, build-candidates ef_construction, search-candidates ef_search; flat means no index. */
	def String vectorIndex(VectorCollection c, Table table, VectorField field, VectorIndex index, boolean ifNotExists) {
		val column = table.column(ModelUtil.fieldName(field))
		val Map<String, Integer> options = newLinkedHashMap
		note(index, 'onDisk', 'online')
		if (written(index, 'accelerator') && index.accelerator.literal == 'gpu') throw Unsupported.of('gpu acceleration; vss builds and searches HNSW indexes on the CPU (usearch)')
		for (name : ModelUtil.extParams(index, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (index.quantization !== null && index.quantization.type.literal != 'none') throw Unsupported.of('quantization; ' + DuckdbStatements.FLOAT_ONLY)
		if (index.family.literal == 'flat') return null
		if (index.family.literal != 'hnsw' && index.family.literal != 'auto') {
			throw Unsupported.of(index.family.literal + ' indexes; vss builds HNSW indexes only (CREATE INDEX ... USING HNSW)')
		}
		if (written(index, 'centroids')) throw Unsupported.of('centroids on an hnsw index; lists belong to ivf indexes')
		if (written(index, 'maxDegree')) options.put('M', index.maxDegree)
		if (written(index, 'buildCandidates')) options.put('ef_construction', index.buildCandidates)
		if (written(index, 'searchCandidates')) options.put('ef_search', index.searchCandidates)
		val metric = if (written(index, 'metric')) index.metric.literal else column.vec.metric
		val indexed = new Column(column.name, column.type, null, false, new Vec(column.vec.dim, metric), null, column.nullable, null, #[])
		DuckdbSql.createVectorIndex(table, indexName(c, index.name), indexed, options, ifNotExists)
	}

	/** An ART index on a payload column; a full_text index is the table's fts index, built with the table. */
	def String payloadIndex(VectorCollection c, Table table, PayloadIndex index, boolean ifNotExists) {
		val path = index.target
		if (path.rawPath !== null) throw Unsupported.of('a payload index on a raw path; the generator indexes declared fields')
		if (!(path.field instanceof PayloadField)) throw Unsupported.of('a payload index on a vector field')
		val column = table.column(ModelUtil.fieldName(path.field))
		note(index, 'onDisk', 'lowercase', 'prefixMatch', 'minTokenLength', 'maxTokenLength')
		for (name : ModelUtil.extParams(index, DuckdbGenerator.VENDOR).keySet) throw Unsupported.of('extension parameter ' + name)
		if (written(index, 'structure') && index.structure.literal == 'bitmap') {
			throw Unsupported.of('bitmap payload indexes; DuckDB indexes columns with ART and keeps min-max zone maps per row group')
		}
		if (!path.subFields.empty) throw Unsupported.of('a path index below ' + column.name + '; ' + DuckdbStatements.NO_PATH_INDEX)
		switch index.kind {
			case FULL_TEXT: {
				if (!column.fullText) throw Unsupported.of('a full_text index on ' + column.name + ', which is not a text field')
				null
			}
			case GEO: throw Unsupported.of('geo payload indexes; ' + DuckdbStatements.NO_SPATIAL)
			case JSON: throw Unsupported.of('json payload indexes; DuckDB indexes scalar columns and expressions with ART, not JSON documents')
			default: {
				if (column.array || column.json) throw Unsupported.of('an index on ' + column.name + '; ART indexes keys of scalar types, not lists or JSON')
				DuckdbSql.createPayloadIndex(table, indexName(c, index.name), column, ifNotExists)
			}
		}
	}

	def static String createView(String alias, Table table, boolean ifNotExists) {
		'CREATE ' + (if (ifNotExists) 'OR REPLACE ' else '') + 'VIEW ' + DuckdbSql.qualified(table, alias) + ' AS SELECT * FROM ' + DuckdbSql.table(table)
	}

	// ---------------------------------------------------------------- code

	/** One runtime call with SQL text and parameters; settings only when present. */
	def static Code.Call call(String op, String sql, Query q, Map<String, String> settings) {
		val args = kw('sql', text(sql))
		if (q !== null && q.size > 0) args.put('params', q.params)
		if (settings !== null && !settings.empty) args.put('settings', DuckdbSql.settings(settings))
		runtime(op, args)
	}

	/** A write: the statement, its counter and the fts rebuild of the table when it has full-text columns. */
	def static Code.Call change(String sql, Query q, String counter, Table table, boolean skipped) {
		val args = kw('sql', text(sql), 'counter', text(counter))
		if (q !== null && q.size > 0) args.put('params', q.params)
		val refresh = DuckdbSql.createFtsIndex(table)
		if (refresh !== null) args.put('refresh', items(#[text(refresh) as Expr]))
		if (skipped) args.put('skipped', flag(true))
		runtime('change', args)
	}

	/** DDL statements in one transaction. */
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
