package org.ftn.vecDSL.editor;

import java.util.List;

import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/** Removal of one vendor from 'targets [ .. ]', offered while at least one other vendor stays targeted. */
public final class TargetsEdit {

	private TargetsEdit() {
	}

	/** The edit, or null when the vendor is not targeted or is the only target. */
	public static TextEdit removeVendor(Script script, String vendor) {
		List<INode> values = NodeModelUtils.findNodesForFeature(script, VecDSLPackage.eINSTANCE.getScript_Targets());
		int index = script.getTargets().indexOf(vendor);
		if (index < 0 || values.size() != script.getTargets().size() || values.size() < 2) {
			return null;
		}
		INode node = values.get(index);
		if (index > 0) {
			int start = values.get(index - 1).getEndOffset();
			return new TextEdit(start, node.getEndOffset() - start, "");
		}
		return new TextEdit(node.getOffset(), values.get(1).getOffset() - node.getOffset(), "");
	}
}
