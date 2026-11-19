package org.ftn.vecDSL.editor;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.ftn.vecDSL.coverage.ConstructIds;
import org.ftn.vecDSL.vecDSL.Method;

/**
 * The construct a keyword stands for. The first keyword of a statement, concept, alter operation or extension item
 * rule stands for the class; a clause keyword stands for the feature its clause assigns. Keywords of helper rules
 * (query input, search parameters, filters) stand for the feature through which the enclosing construct holds them.
 */
public final class ConstructLocator {

	private ConstructLocator() {
	}

	/**
	 * @param keyword a keyword of the grammar
	 * @param context the object the keyword belongs to, or the object being written around it
	 * @return the construct, or null when the keyword stands for none
	 */
	public static Construct locate(Keyword keyword, EObject context) {
		ParserRule rule = GrammarUtil.containingParserRule(keyword);
		if (rule == null || GrammarUtil.isDatatypeRule(rule)) {
			return null;
		}
		EClass ruleType = type(rule);
		if (!rule.isFragment() && ruleType != null && ConstructIds.of(ruleType) != null) {
			return isFirstKeyword(keyword, rule) ? new Construct(ruleType, null)
					: new Construct(ruleType, feature(ruleType, governingAssignment(keyword)));
		}
		EObject owner = construct(context);
		if (owner == null) {
			return null;
		}
		if (rule.isFragment()) {
			return new Construct(owner.eClass(), feature(owner.eClass(), governingAssignment(keyword)));
		}
		if (isFirstKeyword(keyword, rule)) {
			Assignment call = callOf(rule, owner.eClass());
			if (call != null) {
				return new Construct(owner.eClass(), feature(owner.eClass(), call));
			}
		}
		EObject held = heldBy(context, owner);
		return held == null ? new Construct(owner.eClass(), null)
				: new Construct(owner.eClass(), held.eContainingFeature());
	}

	/** The nearest object, the context included, that the coverage matrices list. */
	public static EObject construct(EObject context) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (ConstructIds.of(current.eClass()) != null) {
				return current;
			}
		}
		return null;
	}

	private static EObject heldBy(EObject context, EObject owner) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current.eContainer() == owner) {
				return current;
			}
		}
		return null;
	}

	private static EClass type(AbstractRule rule) {
		EClassifier classifier = rule.getType() == null ? null : rule.getType().getClassifier();
		return classifier instanceof EClass eClass ? eClass : null;
	}

	private static boolean isFirstKeyword(Keyword keyword, ParserRule rule) {
		for (Keyword candidate : GrammarUtil.containedKeywords(rule)) {
			return candidate == keyword;
		}
		return false;
	}

	/** The assignment a clause keyword introduces: its own boolean assignment or the next one in its group. */
	private static Assignment governingAssignment(Keyword keyword) {
		Assignment own = GrammarUtil.containingAssignment(keyword);
		if (own != null) {
			return own;
		}
		if (keyword.eContainer() instanceof Group group) {
			List<AbstractElement> elements = group.getElements();
			for (int i = elements.indexOf(keyword) + 1; i < elements.size(); i++) {
				List<Assignment> assignments = elements.get(i) instanceof Assignment assignment ? List.of(assignment)
						: GrammarUtil.containedAssignments(elements.get(i));
				if (!assignments.isEmpty()) {
					return assignments.get(0);
				}
			}
		}
		return null;
	}

	/** The assignment of the owner's rule, or of a fragment it calls, that calls the helper rule. */
	private static Assignment callOf(ParserRule helper, EClass owner) {
		AbstractRule ownerRule = GrammarUtil.findRuleForName(GrammarUtil.getGrammar(helper), owner.getName());
		if (ownerRule == null) {
			return null;
		}
		for (Assignment assignment : GrammarUtil.containedAssignments(ownerRule)) {
			if (assignment.getTerminal() instanceof RuleCall call && calls(call.getRule(), helper)) {
				return assignment;
			}
		}
		for (RuleCall call : GrammarUtil.containedRuleCalls(ownerRule)) {
			if (call.getRule() instanceof ParserRule fragment && fragment.isFragment()) {
				for (Assignment assignment : GrammarUtil.containedAssignments(fragment)) {
					if (assignment.getTerminal() instanceof RuleCall inner && calls(inner.getRule(), helper)) {
						return assignment;
					}
				}
			}
		}
		return null;
	}

	/** Whether the rule is the helper or an alternatives rule that offers it, such as RecordSource for InlineRecords. */
	private static boolean calls(AbstractRule rule, ParserRule helper) {
		if (rule == helper) {
			return true;
		}
		return rule instanceof ParserRule parserRule && !parserRule.isFragment()
				&& EcoreUtil2.eAllOfType(parserRule.getAlternatives(), RuleCall.class).stream()
						.anyMatch(call -> call.getRule() == helper && GrammarUtil.containingAssignment(call) == null);
	}

	private static EStructuralFeature feature(EClass owner, Assignment assignment) {
		return assignment == null ? null : owner.getEStructuralFeature(assignment.getFeature());
	}

	/** The statement the context belongs to, or null. */
	public static Method statement(EObject context) {
		return EcoreUtil2.getContainerOfType(context, Method.class);
	}
}
