package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.ftn.vecDSL.coverage.ConstructIds;
import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * Constructs of a script that a targeted vendor does not support, by its COVERAGE.md: a class whose row is
 * unsupported, or a written clause whose feature row is unsupported while its class is supported. The editor shows
 * them as warnings; the language validator is not involved.
 */
public final class CoverageCheck {

	/** Issue code of the editor warnings. */
	public static final String UNSUPPORTED = "vecdsl.coverage.unsupported";

	/** A construct a vendor does not support; the feature is null when the whole construct is unsupported. */
	public record Finding(EObject object, EStructuralFeature feature, CoverageEntry entry) {

		public String message() {
			return "unsupported on " + entry.vendor() + ": " + entry.reason();
		}

		/** The node to mark: the feature's first node, or the first keyword of the construct. */
		public INode node() {
			if (feature != null) {
				List<INode> nodes = NodeModelUtils.findNodesForFeature(object, feature);
				if (!nodes.isEmpty()) {
					return nodes.get(0);
				}
			}
			ICompositeNode node = NodeModelUtils.getNode(object);
			if (node == null) {
				return null;
			}
			for (ILeafNode leaf : node.getLeafNodes()) {
				if (!leaf.isHidden() && leaf.getGrammarElement() instanceof Keyword keyword
						&& keyword.getValue().matches("[a-z].*")) {
					return leaf;
				}
			}
			return node;
		}
	}

	private CoverageCheck() {
	}

	/** The findings for the vendors of the script's targets list. */
	public static List<Finding> findings(Script script, CoverageIndex index) {
		return findings(script, TargetStatus.targets(script), index);
	}

	/**
	 * The findings of the object and everything it contains, for each of the vendors that has coverage data, vendor by
	 * vendor in the given order and in document order within a vendor.
	 */
	public static List<Finding> findings(EObject root, List<String> vendors, CoverageIndex index) {
		List<Finding> result = new ArrayList<>();
		for (String vendor : vendors) {
			if (!index.hasData(vendor)) {
				continue;
			}
			check(root, vendor, index, result);
			root.eAllContents().forEachRemaining(object -> check(object, vendor, index, result));
		}
		return result;
	}

	private static void check(EObject object, String vendor, CoverageIndex index, List<Finding> result) {
		String classId = ConstructIds.of(object.eClass());
		if (classId == null) {
			return;
		}
		CoverageEntry classEntry = index.entry(vendor, classId).orElse(null);
		if (classEntry != null && classEntry.isUnsupported()) {
			result.add(new Finding(object, null, classEntry));
			return;
		}
		for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
			index.entry(vendor, ConstructIds.of(object.eClass(), feature))
					.filter(entry -> entry.isUnsupported() && ModelUtil.written(object, feature.getName()))
					.ifPresent(entry -> result.add(new Finding(object, feature, entry)));
		}
	}
}
