package org.ftn.vecDSL.editor;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

/**
 * The text of the clause that writes a feature, from its keyword to its last token ("metric dot", "partitions [a, b]",
 * "exact"), with the whitespace before it, so that removing it leaves the rest of the statement as it was laid out.
 */
public final class ClauseRegion {

	private ClauseRegion() {
	}

	/** The edit that removes the clause, or null when the feature is not written. */
	public static TextEdit removal(EObject owner, EStructuralFeature feature) {
		List<INode> nodes = NodeModelUtils.findNodesForFeature(owner, feature);
		if (nodes.isEmpty()) {
			return null;
		}
		INode first = nodes.get(0);
		ILeafNode start = firstLeaf(first);
		Group clause = clause(first);
		for (ILeafNode previous = previousToken(start); clause != null && previous != null
				&& inClause(previous, clause); previous = previousToken(previous)) {
			start = previous;
		}
		ILeafNode end = lastLeaf(nodes.get(nodes.size() - 1));
		for (ILeafNode next = nextToken(end); clause != null && next != null && inClause(next, clause); next = nextToken(next)) {
			end = next;
		}
		ILeafNode before = previousToken(start);
		int lineStart = before == null ? start.getOffset() : before.getEndOffset();
		return new TextEdit(lineStart, end.getEndOffset() - lineStart, "");
	}

	/** The optional group that starts with a keyword and assigns the feature, such as ('metric' metric=EMetric). */
	private static Group clause(INode node) {
		EObject element = node.getGrammarElement();
		Assignment assignment = element instanceof Assignment own ? own : GrammarUtil.containingAssignment(element);
		if (assignment == null || !(assignment.eContainer() instanceof Group group)
				|| group == GrammarUtil.containingRule(group).getAlternatives()) {
			return null;
		}
		return group.getElements().get(0) instanceof Keyword ? group : null;
	}

	/** Whether the token, or a node it belongs to, was parsed by an element of the clause group. */
	private static boolean inClause(ILeafNode token, Group clause) {
		for (INode node = token; node != null; node = node.getParent()) {
			if (node.getGrammarElement() instanceof AbstractElement element && EcoreUtil.isAncestor(clause, element)) {
				return true;
			}
		}
		return false;
	}

	private static ILeafNode firstLeaf(INode node) {
		for (ILeafNode leaf : node.getLeafNodes()) {
			if (!leaf.isHidden()) {
				return leaf;
			}
		}
		return null;
	}

	private static ILeafNode lastLeaf(INode node) {
		ILeafNode result = null;
		for (ILeafNode leaf : node.getLeafNodes()) {
			if (!leaf.isHidden()) {
				result = leaf;
			}
		}
		return result;
	}

	private static ILeafNode previousToken(ILeafNode leaf) {
		ILeafNode result = null;
		for (ILeafNode candidate : leaf.getRootNode().getLeafNodes()) {
			if (candidate == leaf) {
				return result;
			}
			if (!candidate.isHidden()) {
				result = candidate;
			}
		}
		return null;
	}

	private static ILeafNode nextToken(ILeafNode leaf) {
		boolean found = false;
		for (ILeafNode candidate : leaf.getRootNode().getLeafNodes()) {
			if (found && !candidate.isHidden()) {
				return candidate;
			}
			found |= candidate == leaf;
		}
		return null;
	}
}
