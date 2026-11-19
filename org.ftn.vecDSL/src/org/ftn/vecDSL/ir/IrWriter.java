package org.ftn.vecDSL.ir;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Writes the IR document of a script: {"vecdsl-ir", "program", "spans", "literals"}. The program body has one JSON
 * object per model element, "$type" first, then every structural feature in the order of
 * {@code eClass().getEAllStructuralFeatures()} (schema order), then "$norm". Rules: N1 an undeclared single-valued
 * attribute is null, a flag is false, an absent element null and an empty list []; N12 references are qualified paths;
 * N13 "extensions" is an object keyed by vendor id whose values are the item lists of that vendor's blocks; N14 numbers
 * are canonical JSON numbers. Positions live only in "spans", keyed by the JSON pointer of the element in "program", so
 * two programs that differ only in layout have the same body.
 */
public final class IrWriter {

	public static final String VERSION = "1.0";
	static final String TYPE = "$type";
	static final String NORM = "$norm";

	private IrNames names;
	private IrNormalizer normalizer;
	private Map<String, Object> spans;
	private Map<String, Object> literals;

	public String write(Script script) {
		return Json.write(document(script));
	}

	public Map<String, Object> document(Script script) {
		names = new IrNames(script);
		normalizer = new IrNormalizer(names);
		spans = new LinkedHashMap<>();
		literals = new LinkedHashMap<>();
		Map<String, Object> program = element(script, "");
		Map<String, Object> document = new LinkedHashMap<>();
		document.put("vecdsl-ir", VERSION);
		document.put("program", program);
		document.put("spans", spans);
		document.put("literals", literals);
		return document;
	}

	private Map<String, Object> element(EObject element, String path) {
		Map<String, Object> span = IrSource.span(element);
		if (span != null) {
			spans.put(path.isEmpty() ? "/" : path, span);
		}
		Map<String, Object> node = new LinkedHashMap<>();
		node.put(TYPE, element.eClass().getName());
		for (EStructuralFeature feature : element.eClass().getEAllStructuralFeatures()) {
			String featurePath = path + "/" + feature.getName();
			node.put(feature.getName(), isExtensions(feature) ? extensions(element, feature, featurePath)
					: value(element, feature, featurePath));
		}
		Map<String, Object> norm = normalizer.normalize(element);
		if (!norm.isEmpty()) {
			node.put(NORM, norm);
		}
		return node;
	}

	static boolean isExtensions(EStructuralFeature feature) {
		return feature instanceof EReference reference && reference.isContainment() && reference.isMany()
				&& reference.getEReferenceType() == VecDSLPackage.eINSTANCE.getVendorExtension();
	}

	private Object value(EObject element, EStructuralFeature feature, String path) {
		Object value = element.eGet(feature);
		if (feature instanceof EAttribute) {
			if (feature.isMany()) {
				return ((List<?>) value).stream().map(IrWriter::scalar).toList();
			}
			if (IrSource.isBoolean(feature)) {
				return value;
			}
			if (!IrSource.declared(element, feature)) {
				return null;
			}
			if (value instanceof Number number) {
				String text = IrSource.literal(element, feature, number);
				if (text != null) {
					literals.put(path, text);
				}
			}
			return scalar(value);
		}
		EReference reference = (EReference) feature;
		if (reference.isContainment()) {
			if (!reference.isMany()) {
				return value == null ? null : element((EObject) value, path);
			}
			List<Object> children = new ArrayList<>();
			List<?> list = (List<?>) value;
			for (int i = 0; i < list.size(); i++) {
				children.add(element((EObject) list.get(i), path + "/" + i));
			}
			return children;
		}
		if (!reference.isMany()) {
			return value == null ? null : reference((EObject) value, reference);
		}
		return ((List<?>) value).stream().map(target -> (Object) reference((EObject) target, reference)).toList();
	}

	private String reference(EObject target, EReference reference) {
		if (target.eIsProxy()) {
			throw new IllegalStateException("unresolved reference " + reference.getName());
		}
		return names.reference(target, reference.getEReferenceType());
	}

	private Map<String, Object> extensions(EObject element, EStructuralFeature feature, String path) {
		Map<String, List<VendorExtension>> grouped = new LinkedHashMap<>();
		for (Object block : (List<?>) element.eGet(feature)) {
			grouped.computeIfAbsent(((VendorExtension) block).getVendor(), vendor -> new ArrayList<>())
					.add((VendorExtension) block);
		}
		Map<String, Object> byVendor = new LinkedHashMap<>();
		grouped.forEach((vendor, blocks) -> {
			List<Object> lists = new ArrayList<>();
			for (int b = 0; b < blocks.size(); b++) {
				String blockPath = path + "/" + vendor + "/" + b;
				Map<String, Object> span = IrSource.span(blocks.get(b));
				if (span != null) {
					spans.put(blockPath, span);
				}
				List<Object> items = new ArrayList<>();
				for (int i = 0; i < blocks.get(b).getItems().size(); i++) {
					items.add(element(blocks.get(b).getItems().get(i), blockPath + "/" + i));
				}
				lists.add(items);
			}
			byVendor.put(vendor, lists);
		});
		return byVendor;
	}

	private static Object scalar(Object value) {
		return value instanceof Enumerator literal ? literal.getLiteral() : value;
	}
}
