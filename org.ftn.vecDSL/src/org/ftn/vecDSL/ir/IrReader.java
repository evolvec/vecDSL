package org.ftn.vecDSL.ir;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLFactory;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * Rebuilds the EMF model of a script from its IR document. Every feature of an element must be present (rule N1);
 * "$norm" is derived and ignored. References are resolved after the containment tree is complete (rule N12). Each
 * rebuilt element carries an {@link IrSource} with its declared attributes, span and literal texts, so that
 * {@code IrWriter.write(IrReader.read(ir))} gives the input IR byte for byte.
 */
public final class IrReader {

	private static final VecDSLPackage PACKAGE = VecDSLPackage.eINSTANCE;

	private record Link(EObject owner, EReference reference, Object value) {
	}

	private final List<Link> links = new ArrayList<>();
	private Map<?, ?> spans;
	private Map<?, ?> literals;

	public Script read(String json) {
		return read(Json.parse(json));
	}

	public Script read(Object document) {
		Map<?, ?> root = object(document, "document");
		if (!IrWriter.VERSION.equals(root.get("vecdsl-ir"))) {
			throw new IllegalArgumentException("not a vecDSL IR " + IrWriter.VERSION + " document: vecdsl-ir is "
					+ root.get("vecdsl-ir"));
		}
		if (root.containsKey("diagnostics")) {
			throw new IllegalArgumentException("a diagnostics document has no program");
		}
		spans = object(root.get("spans"), "spans");
		literals = object(root.get("literals"), "literals");
		links.clear();
		EObject program = element(object(root.get("program"), "program"), "");
		if (!(program instanceof Script script)) {
			throw new IllegalArgumentException("the program is a " + program.eClass().getName() + ", not a Script");
		}
		IrNames names = new IrNames(script);
		for (Link link : links) {
			EClass type = link.reference().getEReferenceType();
			if (link.reference().isMany()) {
				@SuppressWarnings("unchecked")
				EList<EObject> targets = (EList<EObject>) link.owner().eGet(link.reference());
				for (Object item : (List<?>) link.value()) {
					targets.add(names.resolve(string(item, link.reference().getName()), type));
				}
			} else {
				link.owner().eSet(link.reference(), names.resolve(string(link.value(), link.reference().getName()), type));
			}
		}
		return script;
	}

	private EObject element(Map<?, ?> node, String path) {
		String typeName = string(node.get(IrWriter.TYPE), path + "/" + IrWriter.TYPE);
		EClassifier classifier = PACKAGE.getEClassifier(typeName);
		if (!(classifier instanceof EClass type)) {
			throw new IllegalArgumentException(path + ": unknown $type " + typeName);
		}
		EObject element = VecDSLFactory.eINSTANCE.create(type);
		Set<String> expected = new HashSet<>(List.of(IrWriter.TYPE, IrWriter.NORM));
		Set<EStructuralFeature> declared = new HashSet<>();
		Map<EStructuralFeature, String> texts = new LinkedHashMap<>();
		for (EStructuralFeature feature : type.getEAllStructuralFeatures()) {
			String name = feature.getName();
			expected.add(name);
			if (!node.containsKey(name)) {
				throw new IllegalArgumentException(path + ": " + typeName + " lacks feature " + name);
			}
			String featurePath = path + "/" + name;
			Object value = node.get(name);
			if (IrWriter.isExtensions(feature)) {
				extensions(element, feature, object(value, featurePath), featurePath);
			} else if (feature instanceof EAttribute attribute) {
				if (attribute(element, attribute, value, featurePath)) {
					declared.add(feature);
				}
				if (literals.get(featurePath) instanceof String text) {
					texts.put(feature, text);
				}
			} else {
				reference(element, (EReference) feature, value, featurePath);
			}
		}
		for (Object key : node.keySet()) {
			if (!expected.contains(key)) {
				throw new IllegalArgumentException(path + ": " + typeName + " has no feature " + key);
			}
		}
		element.eAdapters().add(new IrSource(declared, span(path), texts));
		return element;
	}

	private Map<String, Object> span(String path) {
		Object span = spans.get(path.isEmpty() ? "/" : path);
		if (span == null) {
			return null;
		}
		Map<String, Object> copy = new LinkedHashMap<>();
		object(span, "span " + path).forEach((key, value) -> copy.put((String) key,
				value instanceof BigDecimal number ? Integer.valueOf(number.intValueExact()) : value));
		return copy;
	}

	/** Sets an attribute; true when the IR declares it (a non-null single value). */
	private boolean attribute(EObject element, EAttribute attribute, Object value, String path) {
		EDataType type = attribute.getEAttributeType();
		if (attribute.isMany()) {
			@SuppressWarnings("unchecked")
			EList<Object> values = (EList<Object>) element.eGet(attribute);
			for (Object item : array(value, path)) {
				values.add(scalar(type, item, path));
			}
			return false;
		}
		if (value == null) {
			if (IrSource.isBoolean(attribute)) {
				throw new IllegalArgumentException(path + ": a flag is true or false, not null");
			}
			return false;
		}
		element.eSet(attribute, scalar(type, value, path));
		return true;
	}

	private static Object scalar(EDataType type, Object value, String path) {
		if (type instanceof EEnum enumeration) {
			EEnumLiteral literal = enumeration.getEEnumLiteralByLiteral(string(value, path));
			if (literal == null) {
				throw new IllegalArgumentException(path + ": '" + value + "' is no literal of " + enumeration.getName());
			}
			return literal.getInstance();
		}
		Class<?> java = type.getInstanceClass();
		if (java == String.class) {
			return string(value, path);
		}
		if (java == boolean.class || java == Boolean.class) {
			if (value instanceof Boolean flag) {
				return flag;
			}
			throw new IllegalArgumentException(path + ": expected true or false");
		}
		if (!(value instanceof BigDecimal number)) {
			throw new IllegalArgumentException(path + ": expected a number");
		}
		if (java == int.class || java == Integer.class) {
			return number.intValueExact();
		}
		if (java == double.class || java == Double.class) {
			return number.doubleValue();
		}
		throw new IllegalArgumentException(path + ": unsupported attribute type " + type.getName());
	}

	private void reference(EObject element, EReference reference, Object value, String path) {
		if (reference.isContainment()) {
			if (reference.isMany()) {
				@SuppressWarnings("unchecked")
				EList<EObject> children = (EList<EObject>) element.eGet(reference);
				List<?> items = array(value, path);
				for (int i = 0; i < items.size(); i++) {
					children.add(child(reference, object(items.get(i), path + "/" + i), path + "/" + i));
				}
			} else if (value != null) {
				element.eSet(reference, child(reference, object(value, path), path));
			}
		} else if (reference.isMany()) {
			links.add(new Link(element, reference, array(value, path)));
		} else if (value != null) {
			links.add(new Link(element, reference, value));
		}
	}

	private EObject child(EReference reference, Map<?, ?> node, String path) {
		EObject child = element(node, path);
		if (!reference.getEReferenceType().isInstance(child)) {
			throw new IllegalArgumentException(path + ": a " + child.eClass().getName() + " cannot be the "
					+ reference.getName() + " of a " + reference.getEContainingClass().getName());
		}
		return child;
	}

	private void extensions(EObject element, EStructuralFeature feature, Map<?, ?> byVendor, String path) {
		@SuppressWarnings("unchecked")
		EList<EObject> blocks = (EList<EObject>) element.eGet(feature);
		for (Map.Entry<?, ?> entry : byVendor.entrySet()) {
			String vendor = (String) entry.getKey();
			List<?> vendorBlocks = array(entry.getValue(), path + "/" + vendor);
			for (int b = 0; b < vendorBlocks.size(); b++) {
				String blockPath = path + "/" + vendor + "/" + b;
				VendorExtension block = VecDSLFactory.eINSTANCE.createVendorExtension();
				block.setVendor(vendor);
				List<?> items = array(vendorBlocks.get(b), blockPath);
				for (int i = 0; i < items.size(); i++) {
					if (!(element(object(items.get(i), blockPath + "/" + i), blockPath + "/" + i) instanceof ExtensionItem item)) {
						throw new IllegalArgumentException(blockPath + "/" + i + ": not an extension item");
					}
					block.getItems().add(item);
				}
				block.eAdapters().add(new IrSource(Set.of(PACKAGE.getVendorExtension_Vendor()), span(blockPath), Map.of()));
				blocks.add(block);
			}
		}
	}

	private static Map<?, ?> object(Object value, String path) {
		if (value instanceof Map<?, ?> map) {
			return map;
		}
		throw new IllegalArgumentException(path + ": expected an object");
	}

	private static List<?> array(Object value, String path) {
		if (value instanceof List<?> list) {
			return list;
		}
		throw new IllegalArgumentException(path + ": expected an array");
	}

	private static String string(Object value, String path) {
		if (value instanceof String string) {
			return string;
		}
		throw new IllegalArgumentException(path + ": expected a string");
	}
}
