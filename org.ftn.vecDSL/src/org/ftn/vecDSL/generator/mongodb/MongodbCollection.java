package org.ftn.vecDSL.generator.mongodb;

import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.arr;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.flag;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.num;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.obj;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The MongoDB collection of a vecDSL collection as the statements so far leave it: its database and name, the key (stored as
 * _id), the declared fields with their $jsonSchema properties, the hidden fields that carry tenants, namespaces, partitions,
 * versions, visibility stamps and expiry, the vectorSearch indexes, the search index of the full-text fields, the payload
 * indexes and the analyzers. The same descriptor gives the validator, the index definitions and the write options; alter
 * statements derive a copy with an added, dropped or renamed field or a new name.
 */
public final class MongodbCollection {

	/** A vector field: dimensions, mongot similarity and the vecDSL metric it implements. */
	public record Vector(int dims, String similarity, String metric) {
	}

	/**
	 * A declared field: its $jsonSchema property, whether it is a full-text field (and its analyzer), its vector, whether its
	 * values are dates, the paths that a vectorSearch index can filter on, and its default value for writes.
	 */
	public record Field(String name, Entries schema, boolean fullText, String analyzer, Vector vector, boolean date, List<String> filterPaths,
			Expr defaultValue) {

		public Field named(String newName) {
			List<String> paths = new ArrayList<>();
			for (String path : filterPaths) {
				paths.add(path.equals(name) ? newName : newName + path.substring(name.length()));
			}
			return new Field(newName, schema, fullText, analyzer, vector, date, paths, defaultValue);
		}
	}

	/** A vectorSearch index on one vector field with its index options (quantization, indexingMethod, hnswOptions). */
	public record VectorIndex(String name, String field, Entries options, Integer searchCandidates) {
	}

	/** A regular index: its name, keys and options. */
	public record PayloadIndex(String name, Entries keys) {
	}

	public String database;
	public String name;
	public String key;
	public Entries keySchema;
	public String keyKind;
	public List<Field> fields = new ArrayList<>();
	public String tenant;
	public String namespace;
	public String partition;
	public Map<String, String> renames = new LinkedHashMap<>();
	public boolean versioned;
	public boolean expiring;
	public Long ttlSeconds;
	public String description;
	public boolean dynamic;
	public Integer maxProperties;
	public List<VectorIndex> vectorIndexes = new ArrayList<>();
	public List<PayloadIndex> payloadIndexes = new ArrayList<>();
	public List<Expr> analyzers = new ArrayList<>();

	public MongodbCollection copy() {
		MongodbCollection result = new MongodbCollection();
		result.database = database;
		result.name = name;
		result.key = key;
		result.keySchema = keySchema;
		result.keyKind = keyKind;
		result.fields = new ArrayList<>(fields);
		result.tenant = tenant;
		result.namespace = namespace;
		result.partition = partition;
		result.renames = new LinkedHashMap<>(renames);
		result.versioned = versioned;
		result.expiring = expiring;
		result.ttlSeconds = ttlSeconds;
		result.description = description;
		result.dynamic = dynamic;
		result.maxProperties = maxProperties;
		result.vectorIndexes = new ArrayList<>(vectorIndexes);
		result.payloadIndexes = new ArrayList<>(payloadIndexes);
		result.analyzers = new ArrayList<>(analyzers);
		return result;
	}

	public String current(String declared) {
		return renames.getOrDefault(declared, declared);
	}

	public Field field(String declared) {
		String current = current(declared);
		for (Field field : fields) {
			if (field.name().equals(current)) {
				return field;
			}
		}
		throw Unsupported.of("field " + declared + ", which collection " + name + " does not have (dropped by an earlier alter statement)");
	}

	public boolean has(String declared) {
		String current = current(declared);
		return fields.stream().anyMatch(field -> field.name().equals(current));
	}

	/** The document path of a declared field: _id for the key, else the field's current name. */
	public String path(String declared) {
		return declared.equals(key) ? "_id" : field(declared).name();
	}

	public List<Field> vectorFields() {
		return fields.stream().filter(field -> field.vector() != null).toList();
	}

	public boolean hasText() {
		return fields.stream().anyMatch(Field::fullText);
	}

	public void replace(String declared, Field updated) {
		String current = current(declared);
		fields.replaceAll(field -> field.name().equals(current) ? updated : field);
	}

	public void remove(String declared) {
		String current = current(declared);
		fields.removeIf(field -> field.name().equals(current));
		vectorIndexes.removeIf(index -> index.field().equals(current));
	}

	public void rename(String declared, String newName) {
		Field old = field(declared);
		fields.replaceAll(field -> field == old ? field.named(newName) : field);
		vectorIndexes.replaceAll(index -> index.field().equals(old.name()) ? new VectorIndex(index.name(), newName, index.options(), index.searchCandidates()) : index);
		renames.put(declared, newName);
	}

	/** Default values of the fields that declare one, for the writes that leave them out. */
	public Entries defaults() {
		Entries result = Code.entries();
		for (Field field : fields) {
			if (field.defaultValue() != null) {
				result.put(field.name(), field.defaultValue());
			}
		}
		return result.entries().isEmpty() ? null : result;
	}

	public List<Expr> names(boolean vectors, boolean dates) {
		List<Expr> result = new ArrayList<>();
		for (Field field : fields) {
			if ((vectors && field.vector() != null) || (dates && field.date())) {
				result.add(text(field.name()));
			}
		}
		return result;
	}

	/** The hidden fields that every projection without an output list leaves out. */
	public List<String> hidden() {
		List<String> result = new ArrayList<>();
		for (String hiddenField : List.of(MongodbCommands.TENANT_FIELD, MongodbCommands.NAMESPACE_FIELD, MongodbCommands.PARTITION_FIELD,
				MongodbCommands.VERSION_FIELD, MongodbCommands.STAMP_FIELD, MongodbCommands.EXPIRES_FIELD)) {
			result.add(hiddenField);
		}
		return result;
	}

	/** The $jsonSchema validator: every declared field, _id, the hidden fields, additionalProperties unless dynamic. */
	public Entries validator() {
		Entries properties = obj("_id", keySchema);
		for (Field field : fields) {
			if (!field.name().equals(key)) {
				properties.put(field.name(), field.schema());
			}
		}
		for (String slice : new String[] { tenant, namespace, partition }) {
			if (slice != null && !properties.has(slice)) {
				properties.put(slice, obj("bsonType", text("string")));
			}
		}
		properties.put(MongodbCommands.VERSION_FIELD, obj("bsonType", arr(text("int"), text("long"))));
		properties.put(MongodbCommands.STAMP_FIELD, obj("bsonType", text("string")));
		properties.put(MongodbCommands.EXPIRES_FIELD, obj("bsonType", text("date")));
		Entries schema = obj("bsonType", text("object"), "description", description == null ? null : text(description), "properties", properties,
				"additionalProperties", flag(dynamic), "maxProperties", maxProperties == null ? null : num(maxProperties));
		return obj("$jsonSchema", schema);
	}

	/** The definition of a vectorSearch index: its vector field and every filterable field, hidden fields and _id included. */
	public Entries vectorDefinition(VectorIndex index) {
		Field vectorField = field(index.field());
		List<Expr> definitions = new ArrayList<>();
		Entries vector = obj("type", text("vector"), "path", text(vectorField.name()), "numDimensions", num(vectorField.vector().dims()), "similarity",
				text(vectorField.vector().similarity()));
		vector.entries().putAll(index.options().entries());
		definitions.add(vector);
		for (String path : filterPaths()) {
			definitions.add(obj("type", text("filter"), "path", text(path)));
		}
		return obj("fields", arr(definitions));
	}

	public List<String> filterPaths() {
		List<String> result = new ArrayList<>();
		result.add("_id");
		for (Field field : fields) {
			if (!field.name().equals(key)) {
				result.addAll(field.filterPaths());
			}
		}
		for (String slice : new String[] { tenant, namespace, partition }) {
			if (slice != null && !result.contains(slice)) {
				result.add(slice);
			}
		}
		if (versioned) {
			result.add(MongodbCommands.VERSION_FIELD);
		}
		result.add(MongodbCommands.STAMP_FIELD);
		return result;
	}

	/** The definition of the search index vecdsl_text: the full-text fields as string and autocomplete fields, _id and the stamp. */
	public Entries textDefinition() {
		Entries mapped = Code.entries();
		mapped.put("_id", obj("type", text(keyKind)));
		for (Field field : fields) {
			if (field.fullText()) {
				Entries string = obj("type", text("string"), "analyzer", field.analyzer() == null ? null : text(field.analyzer()));
				Entries prefix = obj("type", text("autocomplete"), "analyzer", field.analyzer() == null ? null : text(field.analyzer()));
				mapped.put(field.name(), arr(string, prefix));
			}
		}
		mapped.put(MongodbCommands.STAMP_FIELD, obj("type", text("token")));
		return obj("mappings", obj("dynamic", flag(false), "fields", mapped), "analyzers", analyzers.isEmpty() ? null : arr(analyzers));
	}

	/** The search indexes as createSearchIndexes entries: name, type and definition. */
	public List<Expr> searchIndexes() {
		List<Expr> result = new ArrayList<>();
		for (VectorIndex index : vectorIndexes) {
			result.add(obj("name", text(index.name()), "type", text("vectorSearch"), "definition", vectorDefinition(index)));
		}
		if (hasText()) {
			result.add(obj("name", text(MongodbCommands.TEXT_INDEX), "type", text("search"), "definition", textDefinition()));
		}
		return result;
	}

	/** What the runtime queries to see whether mongot shows a write: one probe per search index. */
	public Code.Items probes() {
		List<Expr> result = new ArrayList<>();
		for (VectorIndex index : vectorIndexes) {
			Field vectorField = field(index.field());
			result.add(obj("index", text(index.name()), "kind", text("vector"), "path", text(vectorField.name()), "dims", num(vectorField.vector().dims())));
		}
		if (hasText()) {
			result.add(obj("index", text(MongodbCommands.TEXT_INDEX), "kind", text("text")));
		}
		return arr(result);
	}

	public VectorIndex vectorIndexOf(String fieldName) {
		for (VectorIndex index : vectorIndexes) {
			if (index.field().equals(fieldName)) {
				return index;
			}
		}
		throw Unsupported.of("vector field " + fieldName + " without a vector index");
	}
}
