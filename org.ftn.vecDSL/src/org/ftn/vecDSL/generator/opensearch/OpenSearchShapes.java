package org.ftn.vecDSL.generator.opensearch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Code.Text;
import org.ftn.vecDSL.generator.common.ShapeSpec;
import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The shared result-shape module (generator/common/shapes/README.md) on an OpenSearch index: the _source of a candidate request,
 * the vector fields the candidates carry, and the runtime spec in the index's current field names.
 */
public final class OpenSearchShapes {

	private OpenSearchShapes() {
	}

	/**
	 * The vector fields a candidate carries under vectors: every vector field with with-vectors, else the diversity field of mmr.
	 * MMR compares dense vectors; a sparse diversity field is unsupported.
	 */
	public static List<String> carried(ShapeSpec shapes, OpenSearchIndex target) {
		String diversity = shapes.diversityField();
		if (diversity != null && target.field(diversity).vector().sparse()) {
			throw Unsupported.of("the 'mmr' clause on a sparse field; maximal marginal relevance compares dense vectors");
		}
		if (shapes.spec().get("withVectors") instanceof Code.Flag flag && flag.value()) {
			return target.vectorFields();
		}
		return diversity == null ? List.of() : List.of(target.field(diversity).name());
	}

	/**
	 * The _source of a candidate request: with an output list, the key, the output fields, the fields the spec reads and the
	 * carried vectors; with output *, every field but the vector fields the candidates do not carry.
	 */
	public static Entries source(ShapeSpec shapes, OpenSearchIndex target) {
		List<String> carried = carried(shapes, target);
		Entries result = Code.entries();
		if (shapes.spec().get("output") instanceof Items output) {
			Set<String> includes = new LinkedHashSet<>();
			includes.add(target.key());
			for (Expr item : output.items()) {
				includes.add(name(target, ((Text) item).value()));
			}
			for (String read : shapes.reads()) {
				includes.add(name(target, read));
			}
			includes.addAll(carried);
			result.put("includes", Code.texts(List.copyOf(includes)));
		} else {
			List<String> excludes = new ArrayList<>(target.vectorFields());
			excludes.removeAll(carried);
			if (!excludes.isEmpty()) {
				result.put("excludes", Code.texts(excludes));
			}
		}
		return result;
	}

	/**
	 * The spec for the runtime: field names as the index has them after renames, the index's key field, and the diversity field's
	 * metric where ShapeSpec leaves it to the target.
	 */
	public static Entries spec(ShapeSpec shapes, OpenSearchIndex target) {
		Entries result = (Entries) renamed(shapes.spec(), target);
		result.put("keyField", Code.text(target.key()));
		if (result.get("mmr") instanceof Entries mmr && mmr.get("metric") instanceof Code.Nil) {
			mmr.put("metric", Code.text(target.field(shapes.diversityField()).vector().metric()));
		}
		return result;
	}

	/** A copy of a spec value with every field name (paths' first segments, output, exclude, vectorField) renamed. */
	private static Expr renamed(Expr value, OpenSearchIndex target) {
		if (value instanceof Entries entries) {
			Entries result = Code.entries();
			for (Map.Entry<String, Expr> entry : entries.entries().entrySet()) {
				result.put(entry.getKey(), switch (entry.getKey()) {
				case "field" -> path(entry.getValue(), target);
				case "output", "exclude" -> names(entry.getValue(), target);
				case "vectorField" -> Code.text(name(target, ((Text) entry.getValue()).value()));
				default -> renamed(entry.getValue(), target);
				});
			}
			return result;
		}
		if (value instanceof Items items) {
			return Code.items(items.items().stream().map(item -> renamed(item, target)).toList());
		}
		return value;
	}

	private static Expr path(Expr value, OpenSearchIndex target) {
		if (!(value instanceof Items segments)) {
			return value;
		}
		List<Expr> result = new ArrayList<>(segments.items());
		result.set(0, Code.text(name(target, ((Text) result.get(0)).value())));
		return Code.items(result);
	}

	private static Expr names(Expr value, OpenSearchIndex target) {
		if (!(value instanceof Items items)) {
			return value;
		}
		return Code.texts(items.items().stream().map(item -> name(target, ((Text) item).value())).toList());
	}

	/** A declared field's name in the index (after rename-field), or the name itself for a raw path into a dynamic field. */
	private static String name(OpenSearchIndex target, String declared) {
		return target.has(declared) ? target.field(declared).name() : declared;
	}
}
