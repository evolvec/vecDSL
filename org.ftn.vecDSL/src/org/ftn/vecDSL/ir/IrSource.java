package org.ftn.vecDSL.ir;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.LineAndColumn;

/**
 * Source facts of a model element that the EMF model does not hold: which single-valued attributes the program wrote
 * (rule N1), the element's source span and the literal text of numbers whose value differs from their canonical text
 * (rule N14). For a parsed element they come from the node model; an element rebuilt from an IR carries them in this
 * adapter, so that writing the rebuilt model gives the same IR.
 */
public final class IrSource extends AdapterImpl {

	private final Set<EStructuralFeature> declared;
	private final Map<String, Object> span;
	private final Map<EStructuralFeature, String> literals;

	IrSource(Set<EStructuralFeature> declared, Map<String, Object> span, Map<EStructuralFeature, String> literals) {
		this.declared = Set.copyOf(declared);
		this.span = span;
		this.literals = Map.copyOf(literals);
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return type == IrSource.class;
	}

	static IrSource of(EObject object) {
		return (IrSource) EcoreUtil.getExistingAdapter(object, IrSource.class);
	}

	/** Whether the program writes the feature: the node model, else the adapter, else EMF's isSet. */
	public static boolean declared(EObject object, EStructuralFeature feature) {
		if (feature.isMany()) {
			return !((List<?>) object.eGet(feature)).isEmpty();
		}
		if (isBoolean(feature)) {
			return Boolean.TRUE.equals(object.eGet(feature));
		}
		if (!(feature instanceof EAttribute)) {
			return object.eGet(feature) != null;
		}
		IrSource source = of(object);
		if (source != null) {
			return source.declared.contains(feature);
		}
		if (NodeModelUtils.getNode(object) != null) {
			return !NodeModelUtils.findNodesForFeature(object, feature).isEmpty();
		}
		return object.eIsSet(feature);
	}

	static boolean isBoolean(EStructuralFeature feature) {
		EDataType type = feature instanceof EAttribute attribute ? attribute.getEAttributeType() : null;
		return type != null && (type == EcorePackage.Literals.EBOOLEAN || type.getInstanceClass() == boolean.class);
	}

	/** Source span of the element, or null for an element that was neither parsed nor rebuilt from an IR. */
	static Map<String, Object> span(EObject object) {
		IrSource source = of(object);
		if (source != null) {
			return source.span;
		}
		ICompositeNode node = NodeModelUtils.getNode(object);
		if (node == null) {
			return null;
		}
		LineAndColumn start = NodeModelUtils.getLineAndColumn(node.getRootNode(), node.getOffset());
		Map<String, Object> span = new LinkedHashMap<>();
		span.put("offset", node.getOffset());
		span.put("length", node.getLength());
		span.put("line", start.getLine());
		span.put("column", start.getColumn());
		span.put("endLine", node.getEndLine());
		return span;
	}

	/**
	 * Literal text of a numeric attribute when the parsed text and the canonical JSON number differ in value (more digits
	 * than a double holds); null otherwise.
	 */
	static String literal(EObject object, EStructuralFeature feature, Number value) {
		IrSource source = of(object);
		if (source != null) {
			return source.literals.get(feature);
		}
		if (NodeModelUtils.getNode(object) == null) {
			return null;
		}
		List<INode> nodes = NodeModelUtils.findNodesForFeature(object, feature);
		if (nodes.isEmpty()) {
			return null;
		}
		String text = NodeModelUtils.getTokenText(nodes.get(0)).replace(" ", "");
		try {
			return new BigDecimal(text).compareTo(new BigDecimal(Json.number(value))) == 0 ? null : text;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
