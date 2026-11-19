package org.ftn.vecDSL.generator.mongodb;

import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.SCORE;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.num;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.obj;
import static org.ftn.vecDSL.generator.mongodb.MongodbCommands.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Code.Text;
import org.ftn.vecDSL.generator.common.ShapeSpec;

/**
 * The shared result-shape module (generator/common/shapes/README.md) on a MongoDB collection: the projection of a candidate
 * window, the vector fields the candidates carry, and the runtime spec in the collection's current field names.
 */
public final class MongodbShapes {

	private MongodbShapes() {
	}

	/** The vector fields a candidate carries under vectors: every vector field with with-vectors, else the diversity field of mmr. */
	public static List<String> carried(ShapeSpec shapes, MongodbCollection target) {
		if (shapes.spec().get("withVectors") instanceof Code.Flag flag && flag.value()) {
			return target.vectorFields().stream().map(MongodbCollection.Field::name).toList();
		}
		return shapes.diversityField() == null ? List.of() : List.of(target.field(shapes.diversityField()).name());
	}

	/**
	 * The projection of a candidate window: with an output list, the key (from _id), the score, the output fields, the fields the
	 * spec reads and the carried vectors; with output *, every field but _id, the hidden fields and the vectors the candidates do
	 * not carry. Highlights and score details of a text search come from the search metadata.
	 */
	public static List<Expr> projection(ShapeSpec shapes, MongodbCollection target, boolean highlighted, boolean explained) {
		List<String> carried = carried(shapes, target);
		if (shapes.spec().get("output") instanceof Items output) {
			Entries projection = obj("_id", num(0), target.key, text("$_id"), SCORE, num(1));
			List<String> names = new ArrayList<>();
			for (Expr item : output.items()) {
				names.add(((Text) item).value());
			}
			names.addAll(shapes.reads());
			for (String name : names) {
				if (!name.equals(target.key)) {
					projection.put(name(target, name), num(1));
				}
			}
			for (String vector : carried) {
				projection.put(vector, num(1));
			}
			marks(projection, highlighted, explained);
			return List.of(obj("$project", projection));
		}
		Entries added = obj(target.key, text("$_id"));
		marks(added, highlighted, explained);
		Entries removed = obj("_id", num(0));
		for (MongodbCollection.Field vector : target.vectorFields()) {
			if (!carried.contains(vector.name())) {
				removed.put(vector.name(), num(0));
			}
		}
		for (String hidden : target.hidden()) {
			removed.put(hidden, num(0));
		}
		return List.of(obj("$addFields", added), obj("$project", removed));
	}

	private static void marks(Entries projection, boolean highlighted, boolean explained) {
		if (highlighted) {
			projection.put("highlights", obj("$meta", text("searchHighlights")));
		}
		if (explained) {
			projection.put("explanation", obj("$meta", text("searchScoreDetails")));
		}
	}

	/**
	 * The spec for the runtime: field names as the collection has them after renames, the key field's name, and the diversity
	 * field's metric where ShapeSpec leaves it to the target.
	 */
	public static Entries spec(ShapeSpec shapes, MongodbCollection target) {
		Entries result = (Entries) renamed(shapes.spec(), target);
		result.put("keyField", text(target.key));
		if (result.get("mmr") instanceof Entries mmr && mmr.get("metric") instanceof Code.Nil) {
			mmr.put("metric", text(target.field(shapes.diversityField()).vector().metric()));
		}
		return result;
	}

	/** A copy of a spec value with every field name (paths' first segments, output, exclude, vectorField) renamed. */
	private static Expr renamed(Expr value, MongodbCollection target) {
		if (value instanceof Entries entries) {
			Entries result = Code.entries();
			for (Map.Entry<String, Expr> entry : entries.entries().entrySet()) {
				result.put(entry.getKey(), switch (entry.getKey()) {
				case "field" -> path(entry.getValue(), target);
				case "output", "exclude" -> names(entry.getValue(), target);
				case "vectorField" -> text(name(target, ((Text) entry.getValue()).value()));
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

	private static Expr path(Expr value, MongodbCollection target) {
		if (!(value instanceof Items segments)) {
			return value;
		}
		List<Expr> result = new ArrayList<>(segments.items());
		result.set(0, text(name(target, ((Text) result.get(0)).value())));
		return Code.items(result);
	}

	private static Expr names(Expr value, MongodbCollection target) {
		if (!(value instanceof Items items)) {
			return value;
		}
		return Code.texts(items.items().stream().map(item -> name(target, ((Text) item).value())).toList());
	}

	/** A declared field's name in the records: the key's name, the field's current name, or the name of a dynamic field. */
	private static String name(MongodbCollection target, String declared) {
		if (declared.equals(target.key)) {
			return target.key;
		}
		return target.has(declared) ? target.field(declared).name() : declared;
	}
}
