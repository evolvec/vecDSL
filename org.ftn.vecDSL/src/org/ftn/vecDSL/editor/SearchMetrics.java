package org.ftn.vecDSL.editor;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.Prefetch;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;

/**
 * The metric a search may name: the one its vector field declares, on the field or on the field's index (T-Metric).
 */
public final class SearchMetrics {

	private SearchMetrics() {
	}

	/** The declared metric of the field a search, leg or stage addresses; null when none is declared or known. */
	public static EMetric declared(EObject search) {
		VectorCollection collection = ModelUtil.targetCollection(search);
		VectorField field = addressedField(search, collection);
		if (field == null || field.eIsProxy()) {
			return null;
		}
		if (ModelUtil.written(field, "metric")) {
			return field.getMetric();
		}
		VectorIndex index = collection == null ? field.getIndex() : ModelUtil.vectorIndex(collection, field);
		return index != null && ModelUtil.written(index, "metric") ? index.getMetric() : null;
	}

	/** Whether the object names a metric that SearchMetrics governs. */
	public static boolean isSearch(EObject object) {
		return object instanceof VectorSearch || object instanceof VectorLeg || object instanceof Prefetch;
	}

	/** The named vector field, or the only vector field of the collection. */
	public static VectorField addressedField(EObject search, VectorCollection collection) {
		VectorField named = search instanceof VectorSearch vector ? vector.getVectorField()
				: search instanceof VectorLeg leg ? leg.getVectorField()
						: search instanceof Prefetch stage ? stage.getVectorField() : null;
		if (named != null) {
			return named;
		}
		if (search instanceof Prefetch && search.eContainer() != null && isSearch(search.eContainer())) {
			return addressedField(search.eContainer(), collection);
		}
		if (collection == null) {
			return null;
		}
		List<VectorField> fields = ModelUtil.vectorFields(collection);
		return fields.size() == 1 ? fields.get(0) : null;
	}
}
