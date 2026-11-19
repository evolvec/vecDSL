package org.ftn.vecDSL.ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.validation.SearchParamsAdmissibility;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.HybridSearch;
import org.ftn.vecDSL.vecDSL.KeywordLeg;
import org.ftn.vecDSL.vecDSL.KeywordSearch;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.SearchParams;
import org.ftn.vecDSL.vecDSL.Snapshot;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;

/**
 * The derived facts of the IR, written under "$norm" of the element they belong to. Each entry implements one rule of
 * spec/SEMANTICS.md: N2 score form and canonical range, N3 vector field name, N4 index attachment and decomposition,
 * N5 addressed vector field, N6 metric in force, N7 hybrid leg input, N8 snapshot name, N9 parameter and environment
 * slots, N10 admitted search parameters, N11 target restriction. Every entry is a function of the model and of
 * {@link IrSource#declared}, so a model rebuilt from an IR gives the same entries.
 */
final class IrNormalizer {

	private static final VecDSLPackage PACKAGE = VecDSLPackage.eINSTANCE;

	private final IrNames names;

	IrNormalizer(IrNames names) {
		this.names = names;
	}

	Map<String, Object> normalize(EObject element) {
		Map<String, Object> norm = new LinkedHashMap<>();
		if (element instanceof Script script) {
			norm.put("restricted", !script.getTargets().isEmpty());
		}
		if (element.eClass().getEStructuralFeature("scoreForm") != null) {
			norm.put("score", score(element));
		}
		if (element instanceof VectorSearch || element instanceof VectorLeg) {
			VectorField field = addressedField(element);
			norm.put("field", field == null ? null : names.reference(field, PACKAGE.getVectorField()));
			EMetric metric = metricInForce(element, field);
			norm.put("metric", metric == null ? null : metric.getLiteral());
		}
		if (element instanceof VectorLeg || element instanceof KeywordLeg) {
			norm.put("input", legInput(element));
		}
		if (element instanceof VectorField field) {
			norm.put("name", ModelUtil.fieldName(field));
			VectorCollection collection = ModelUtil.owningCollection(field);
			VectorIndex index = collection == null ? field.getIndex() : ModelUtil.vectorIndex(collection, field);
			norm.put("index", index == null ? null : names.reference(index, PACKAGE.getVectorIndex()));
		}
		if (element instanceof VectorIndex index) {
			VectorField field = index.getField() != null ? index.getField()
					: index.eContainer() instanceof VectorField owner ? owner : null;
			norm.put("field", field == null ? null : names.reference(field, PACKAGE.getVectorField()));
			norm.put("family", index.getFamily().getLiteral());
			norm.put("accelerator", declared(index, "accelerator") ? index.getAccelerator().getLiteral() : null);
			norm.put("quantization", index.getQuantization() == null ? null : index.getQuantization().getType().getLiteral());
		}
		if (element instanceof Snapshot snapshot) {
			norm.put("snapshotName", snapshot.getSnapshotName() != null ? snapshot.getSnapshotName() : snapshot.getName());
		}
		if (element instanceof ParamRef ref) {
			Parameter parameter = ref.getParam();
			Map<String, Object> slot = new LinkedHashMap<>();
			slot.put("kind", "param");
			slot.put("name", parameter.getName());
			slot.put("type", parameter.getType().getLiteral());
			slot.put("hasDefault", parameter.getDefaultValue() != null);
			norm.put("slot", slot);
		}
		if (element instanceof EnvRef env) {
			Map<String, Object> slot = new LinkedHashMap<>();
			slot.put("kind", "env");
			slot.put("name", env.getVariable());
			slot.put("type", "text");
			slot.put("hasDefault", false);
			norm.put("slot", slot);
		}
		if (element instanceof SearchParams params && params.eContainer() != null) {
			String form = params.eContainer().eClass().getName();
			norm.put("form", form);
			norm.put("admitted", SearchParamsAdmissibility.KEYS.keySet().stream()
					.filter(SearchParamsAdmissibility.admitted(form)::contains).toList());
			norm.put("written", SearchParamsAdmissibility.KEYS.entrySet().stream()
					.filter(key -> declared(params, key.getValue())).map(Map.Entry::getKey).toList());
		}
		return norm;
	}

	/** N2: the score form, and the canonical range of the validator's T-Bounds for the metric in force. */
	private Map<String, Object> score(EObject query) {
		boolean nativeForm = declared(query, "scoreForm")
				&& query.eGet(query.eClass().getEStructuralFeature("scoreForm")) == EScoreForm.NATIVE;
		Map<String, Object> score = new LinkedHashMap<>();
		score.put("form", nativeForm ? "native" : "similarity");
		score.put("higherIsBetter", nativeForm ? null : Boolean.TRUE);
		score.put("range", nativeForm ? null : range(query));
		return score;
	}

	private List<Object> range(EObject query) {
		if (query instanceof KeywordSearch) {
			return bounds(0.0, null);
		}
		if (!(query instanceof VectorSearch)) {
			return null;
		}
		VectorField field = addressedField(query);
		EMetric metric = metricInForce(query, field);
		if (metric == null) {
			return null;
		}
		return switch (metric) {
		case COSINE -> bounds(-1.0, 1.0);
		case EUCLIDEAN, MANHATTAN -> bounds(null, 0.0);
		case HAMMING -> bounds(field != null && field.getDimension() > 0 ? Double.valueOf(-field.getDimension()) : null, 0.0);
		case JACCARD -> bounds(0.0, 1.0);
		case DOT -> bounds(null, null);
		};
	}

	private static List<Object> bounds(Double low, Double high) {
		return new ArrayList<>(Arrays.asList(low, high));
	}

	/** N5: the vector field a query addresses, vf(c, using) of the validator. */
	private static VectorField addressedField(EObject query) {
		EStructuralFeature using = query.eClass().getEStructuralFeature("vectorField");
		if (query.eGet(using) instanceof VectorField named) {
			return named;
		}
		VectorCollection collection = ModelUtil.targetCollection(query);
		if (collection == null) {
			return null;
		}
		List<VectorField> fields = ModelUtil.vectorFields(collection);
		return fields.size() == 1 ? fields.get(0) : null;
	}

	/** N6: the written query metric, else the field's declared metric, else the declared metric of its index. */
	private static EMetric metricInForce(EObject query, VectorField field) {
		if (declared(query, "metric")) {
			return (EMetric) query.eGet(query.eClass().getEStructuralFeature("metric"));
		}
		if (field == null) {
			return null;
		}
		if (declared(field, "metric")) {
			return field.getMetric();
		}
		VectorCollection collection = ModelUtil.owningCollection(field);
		VectorIndex index = collection == null ? field.getIndex() : ModelUtil.vectorIndex(collection, field);
		return index != null && declared(index, "metric") ? index.getMetric() : null;
	}

	/** N7: a leg's own input, the hybrid search's shared input, or none. */
	private static String legInput(EObject leg) {
		if (leg.eGet(leg.eClass().getEStructuralFeature("input")) != null) {
			return "own";
		}
		return leg.eContainer() instanceof HybridSearch hybrid && hybrid.getInput() != null ? "shared" : null;
	}

	private static boolean declared(EObject element, String feature) {
		return IrSource.declared(element, element.eClass().getEStructuralFeature(feature));
	}
}
