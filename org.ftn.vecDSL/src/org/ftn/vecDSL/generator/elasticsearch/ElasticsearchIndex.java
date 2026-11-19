package org.ftn.vecDSL.generator.elasticsearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The Elasticsearch index of a vecDSL collection as the statements so far leave it: its name, its key field, its fields with
 * their mappings, and the hidden keyword fields that carry tenants, namespaces and partitions. The same descriptor serves the
 * index creation, the writes and the queries; alter statements derive a new one (an added, dropped or renamed field, a new
 * index name).
 */
public record ElasticsearchIndex(String name, String key, List<Field> fields, String tenant, String namespace, String partition,
		Map<String, String> renames, String database) {

	/** A vector field: element type, dimensions, Elasticsearch similarity and the vecDSL metric it implements. */
	public record Vector(String element, int dims, String similarity, String metric, boolean sparse) {
	}

	/**
	 * A mapped field: its mapping, whether it is a full-text (text) field with an exact keyword sub-field, its vector, whether
	 * it is a nested field, and its default value for writes.
	 */
	public record Field(String name, Entries mapping, boolean fullText, Vector vector, boolean nested, Expr defaultValue) {

		/** The name a filter compares: the keyword sub-field of a full-text field, else the field. */
		public String exact() {
			return fullText ? name + "." + ElasticsearchBodies.RAW : name;
		}
	}

	public Field field(String declared) {
		String current = renames.getOrDefault(declared, declared);
		for (Field field : fields) {
			if (field.name().equals(current)) {
				return field;
			}
		}
		throw Unsupported.of("field " + declared + ", which index " + name + " does not have (dropped by an earlier alter statement)");
	}

	public boolean has(String declared) {
		String current = renames.getOrDefault(declared, declared);
		return fields.stream().anyMatch(field -> field.name().equals(current));
	}

	public Field keyField() {
		return field(key);
	}

	public ElasticsearchIndex with(Field field) {
		List<Field> result = new ArrayList<>(fields);
		result.removeIf(existing -> existing.name().equals(field.name()));
		result.add(field);
		return new ElasticsearchIndex(name, key, result, tenant, namespace, partition, renames, database);
	}

	public ElasticsearchIndex without(String declared) {
		String current = renames.getOrDefault(declared, declared);
		List<Field> result = new ArrayList<>(fields);
		result.removeIf(field -> field.name().equals(current));
		return new ElasticsearchIndex(name, key, result, tenant, namespace, partition, renames, database);
	}

	public ElasticsearchIndex renamed(String declared, String newName) {
		Field old = field(declared);
		List<Field> result = new ArrayList<>();
		for (Field field : fields) {
			result.add(field == old ? new Field(newName, field.mapping(), field.fullText(), field.vector(), field.nested(), field.defaultValue()) : field);
		}
		Map<String, String> moved = new LinkedHashMap<>(renames);
		moved.put(declared, newName);
		return new ElasticsearchIndex(name, key, result, tenant, namespace, partition, moved, database);
	}

	public ElasticsearchIndex named(String newName) {
		return new ElasticsearchIndex(newName, key, fields, tenant, namespace, partition, renames, database);
	}

	/** Default values of the fields that declare one, for the writes that leave them out. */
	public Map<String, Expr> defaults() {
		Map<String, Expr> result = new LinkedHashMap<>();
		for (Field field : fields) {
			if (field.defaultValue() != null) {
				result.put(field.name(), field.defaultValue());
			}
		}
		return result;
	}
}
