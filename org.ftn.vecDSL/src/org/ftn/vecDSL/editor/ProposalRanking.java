package org.ftn.vecDSL.editor;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EnumRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * Which proposals content assist makes and in what order, for the Eclipse proposal provider and the language
 * server's: a keyword carries the coverage status of its construct for every target, a construct some target does not
 * support ranks last, a search proposes only the metric its vector field declares, and the fields of the collection in
 * scope rank first.
 */
public final class ProposalRanking {

	/** Priority of a proposal for a construct a targeted vendor does not support: below every other proposal. */
	public static final int UNSUPPORTED_PRIORITY = 1;

	/** Added to the priority of a field of the collection in scope. */
	public static final int MEMBER_BONUS = 1000;

	private ProposalRanking() {
	}

	/** Whether the keyword is a metric other than the one the search's vector field declares. */
	public static boolean hidesMetric(Keyword keyword, EObject model) {
		if (!(GrammarUtil.containingRule(keyword) instanceof EnumRule rule && rule.getName().equals("EMetric"))
				|| !SearchMetrics.isSearch(model)) {
			return false;
		}
		EMetric declared = SearchMetrics.declared(model);
		return declared != null && !declared.getLiteral().equals(keyword.getValue());
	}

	/**
	 * The coverage of the keyword's construct for the targets of the script; null when the script declares no targets
	 * or the keyword stands for no construct, and the host proposes the keyword as it would anyway.
	 */
	public static TargetStatus status(Keyword keyword, EObject model, EObject root, CoverageIndex index) {
		List<String> targets = TargetStatus.targets(root instanceof Script script ? script : null);
		Construct construct = targets.isEmpty() ? null : ConstructLocator.locate(keyword, model);
		return construct == null ? null : TargetStatus.of(index, targets, construct);
	}

	/** The priority of a keyword proposal: the host's priority, or last when a target does not support it. */
	public static int keywordPriority(TargetStatus status, int priority) {
		return status.isUnsupported() ? UNSUPPORTED_PRIORITY : priority;
	}

	/** The collection whose fields rank first for a reference of the type; null when the type is not a field. */
	public static VectorCollection fieldScope(EClass referenceType, EObject model) {
		return VecDSLPackage.eINSTANCE.getField().isSuperTypeOf(referenceType) ? FieldContext.collection(model) : null;
	}

	/**
	 * The priority of a reference proposal: raised for a field of the collection in scope. A candidate of the edited
	 * resource is compared by its object's URI, which has the form of the scope's URI whatever form the index gives.
	 */
	public static int referencePriority(int priority, IEObjectDescription candidate, VectorCollection scope) {
		return scope != null && FieldContext.isMember(EcoreUtil.getURI(candidate.getEObjectOrProxy()), scope)
				? priority + MEMBER_BONUS : priority;
	}
}
