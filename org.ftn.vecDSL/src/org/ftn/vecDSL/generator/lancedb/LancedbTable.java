package org.ftn.vecDSL.generator.lancedb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Text;

/**
 * A Lance table as the statements so far leave it: its logical database (a sub-directory), name, key column, Arrow fields
 * (as the runtimes' field descriptions), the metric of every vector column, the hidden slice columns and the declared index
 * plans. Alter statements derive a new descriptor, so later statements read and write the evolved table.
 */
public record LancedbTable(String database, String name, String key, List<Entries> fields, Map<String, String> vectors,
		String tenant, String namespace, String partition, List<Entries> indexes) {

	public LancedbTable {
		fields = List.copyOf(fields);
		vectors = Map.copyOf(new LinkedHashMap<>(vectors));
		indexes = List.copyOf(indexes);
	}

	/** Name and database: the arguments that address a table. */
	public Entries place() {
		Entries result = Code.entries();
		result.put("name", new Text(name));
		if (database != null) {
			result.put("database", new Text(database));
		}
		return result;
	}

	/** What every read and write needs: place, key and the metric of each vector column. */
	public Entries layout() {
		Entries result = place();
		result.put("key", new Text(key));
		Entries metrics = Code.entries();
		vectors.keySet().stream().sorted().forEach(column -> metrics.put(column, new Text(vectors.get(column))));
		if (!metrics.entries().isEmpty()) {
			result.put("vectors", metrics);
		}
		return result;
	}

	/** The Arrow schema as field descriptions, hidden slice columns included. */
	public Code.Items schema() {
		return Code.items(fields);
	}

	public List<String> columns() {
		return fields.stream().map(field -> ((Text) field.get("name")).value()).toList();
	}

	public boolean has(String column) {
		return columns().contains(column);
	}

	public Code.Items indexPlans() {
		return Code.items(indexes);
	}

	public LancedbTable withField(Entries field) {
		List<Entries> next = new ArrayList<>(fields);
		next.add(field);
		return new LancedbTable(database, name, key, next, vectors, tenant, namespace, partition, indexes);
	}

	public LancedbTable withVector(String column, String metric) {
		Map<String, String> next = new LinkedHashMap<>(vectors);
		next.put(column, metric);
		return new LancedbTable(database, name, key, fields, next, tenant, namespace, partition, indexes);
	}

	public LancedbTable withIndex(Entries index) {
		List<Entries> next = new ArrayList<>(indexes);
		next.removeIf(existing -> existing.get("name").equals(index.get("name")));
		next.add(index);
		return new LancedbTable(database, name, key, fields, vectors, tenant, namespace, partition, next);
	}

	public LancedbTable withoutIndex(String index) {
		List<Entries> next = new ArrayList<>(indexes);
		next.removeIf(existing -> ((Text) existing.get("name")).value().equals(index));
		return new LancedbTable(database, name, key, fields, vectors, tenant, namespace, partition, next);
	}

	/** The table without a column; indexes on the column go with it (Lance drops them with the column). */
	public LancedbTable withoutField(String column) {
		List<Entries> next = new ArrayList<>(fields);
		next.removeIf(field -> ((Text) field.get("name")).value().equals(column));
		Map<String, String> metrics = new LinkedHashMap<>(vectors);
		metrics.remove(column);
		List<Entries> kept = new ArrayList<>(indexes);
		kept.removeIf(index -> ((Text) index.get("column")).value().equals(column));
		return new LancedbTable(database, name, key, next, metrics, tenant, namespace, partition, kept);
	}

	/** The table with a column renamed; its index plans follow the column. */
	public LancedbTable withRenamedField(String column, String newName) {
		List<Entries> next = new ArrayList<>();
		for (Entries field : fields) {
			next.add(((Text) field.get("name")).value().equals(column) ? rename(field, "name", newName) : field);
		}
		Map<String, String> metrics = new LinkedHashMap<>();
		vectors.forEach((field, metric) -> metrics.put(field.equals(column) ? newName : field, metric));
		List<Entries> plans = new ArrayList<>();
		for (Entries index : indexes) {
			plans.add(((Text) index.get("column")).value().equals(column) ? rename(index, "column", newName) : index);
		}
		return new LancedbTable(database, name, key.equals(column) ? newName : key, next, metrics, tenant, namespace, partition, plans);
	}

	public LancedbTable renamed(String newName) {
		return new LancedbTable(database, newName, key, fields, vectors, tenant, namespace, partition, indexes);
	}

	private static Entries rename(Entries entries, String slot, String value) {
		Map<String, Expr> copy = new LinkedHashMap<>(entries.entries());
		copy.put(slot, new Text(value));
		return Code.entries(copy);
	}
}
