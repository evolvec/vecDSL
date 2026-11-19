package org.ftn.vecDSL.editor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.ftn.vecDSL.vecDSL.EnvRef;

/**
 * What a hover at an offset describes when it is not a plain cross reference or declaration: a keyword word, hovered
 * as the object it belongs to over the keyword's own region, or an environment reference env("NAME"), hovered over its
 * whole text (Xtext's significant region of an object without a name is its first keyword, so the variable name in
 * quotes would have no hover). Shared by the Eclipse hover and the language server's.
 */
public record HoverTarget(EObject element, int offset, int length) {

	/** The keyword or environment reference at the offset; null when the host's default lookup applies. */
	public static HoverTarget at(XtextResource resource, int offset) {
		ILeafNode keyword = keywordAt(resource, offset);
		if (keyword != null && keyword.getSemanticElement() != null && !(keyword.getSemanticElement() instanceof EnvRef)) {
			return new HoverTarget(keyword.getSemanticElement(), keyword.getOffset(), keyword.getLength());
		}
		ILeafNode leaf = resource == null || resource.getParseResult() == null ? null
				: NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset);
		if (leaf != null && !leaf.isHidden() && NodeModelUtils.findActualSemanticObjectFor(leaf) instanceof EnvRef reference) {
			ICompositeNode node = NodeModelUtils.getNode(reference);
			if (node != null) {
				return new HoverTarget(reference, node.getOffset(), node.getLength());
			}
		}
		return null;
	}

	/** The keyword whose word covers exactly the region, or null: a hover over that region describes the keyword. */
	public static Keyword keyword(XtextResource resource, int offset, int length) {
		ILeafNode leaf = keywordAt(resource, offset);
		return leaf != null && leaf.getLength() == length ? (Keyword) leaf.getGrammarElement() : null;
	}

	/** The keyword word at the offset, or null. */
	private static ILeafNode keywordAt(XtextResource resource, int offset) {
		if (resource == null || resource.getParseResult() == null) {
			return null;
		}
		ILeafNode leaf = NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset);
		return leaf != null && !leaf.isHidden() && leaf.getGrammarElement() instanceof Keyword keyword
				&& keyword.getValue().matches("[a-z][a-z0-9_-]*") ? leaf : null;
	}
}
