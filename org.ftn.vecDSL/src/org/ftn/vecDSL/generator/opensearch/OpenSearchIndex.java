package org.ftn.vecDSL.generator.opensearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The OpenSearch index of a vecDSL collection as the statements so far leave it: its name, its key field, its fields with
 * their mappings, and the hidden keyword fields that carry tenants, namespaces and partitions. The same descriptor serves the
 * index creation, the writes and the queries; alter statements derive a new one (an added, dropped or renamed field, a new
 * index name).
 */
public record OpenSearchIndex(String name, String key, List<Field> fields, String tenant, String namespace, String partition,
		Map<String, String> renames, String database) {

	/**
	 * A vector field: the knn_vector data type (float, half_float, byte or binary; bfloat16 values are float values stored by
	 * the sq encoder of type bf16), dimensions, k-NN space type, the vecDSL metric it implements, whether it is a rank_features
	 * field, the oversample factor of a quantized index's rescoring, and whether every search on it is exact (a flat index).
	 */
	public record Vector(String element, int dims, String space, String metric, boolean sparse, Double oversample, boolean exact) {
	}

	/**
	 * A mapped field: its mapping, whether it is a full-text (text) field with an exact keyword sub-field, its vector, whether
	 * it is a nested field, and its default value for writes.
	 */
	public record Field(String name, Entries mapping, boolean fullText, Vector vector, boolean nested, Expr defaultValue) {

		/** The name a filter compares: the keyword sub-field of a full-text field, else the field. */
		public String exact() {
			return fullText ? name + "." + OpenSearchBodies.RAW : name;
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

	/** The names of the vector fields, which _source filtering leaves out unless a statement returns vectors. */
	public List<String> vectorFields() {
		return fields.stream().filter(field -> field.vector() != null).map(Field::name).toList();
	}

	/**
	 * The dense float vector fields (data type float or half_float; bfloat16 fields are float fields), whose values every
	 * runtime writes with a decimal point: the k-NN MMR reranker reads stored vectors from _source and casts each element
	 * to Double, so a vector written as [1, 0] fails there. Byte and binary vectors hold integers and are not listed.
	 */
	public List<String> floatVectorFields() {
		return fields.stream()
				.filter(field -> field.vector() != null && !field.vector().sparse()
						&& ("float".equals(field.vector().element()) || "half_float".equals(field.vector().element())))
				.map(Field::name).toList();
	}

	public Field keyField() {
		return field(key);
	}

	public OpenSearchIndex with(Field field) {
		List<Field> result = new ArrayList<>(fields);
		result.removeIf(existing -> existing.name().equals(field.name()));
		result.add(field);
		return new OpenSearchIndex(name, key, result, tenant, namespace, partition, renames, database);
	}

	public OpenSearchIndex without(String declared) {
		String current = renames.getOrDefault(declared, declared);
		List<Field> result = new ArrayList<>(fields);
		result.removeIf(field -> field.name().equals(current));
		return new OpenSearchIndex(name, key, result, tenant, namespace, partition, renames, database);
	}

	public OpenSearchIndex renamed(String declared, String newName) {
		Field old = field(declared);
		List<Field> result = new ArrayList<>();
		for (Field field : fields) {
			result.add(field == old ? new Field(newName, field.mapping(), field.fullText(), field.vector(), field.nested(), field.defaultValue()) : field);
		}
		Map<String, String> moved = new LinkedHashMap<>(renames);
		moved.put(declared, newName);
		return new OpenSearchIndex(name, key, result, tenant, namespace, partition, moved, database);
	}

	public OpenSearchIndex named(String newName) {
		return new OpenSearchIndex(newName, key, fields, tenant, namespace, partition, renames, database);
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
