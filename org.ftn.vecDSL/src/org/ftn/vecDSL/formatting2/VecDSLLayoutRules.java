package org.ftn.vecDSL.formatting2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.UnorderedGroup;
import org.eclipse.xtext.IGrammarAccess;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Layout facts derived from the grammar: which grammar elements begin a clause line of a statement, which begin an
 * element line inside a block, and which braces delimit blocks. A clause or block element starts with a keyword
 * word, an optional or repeated element, or a call of a parser rule; mandatory names, numbers, references and
 * punctuation continue the line they follow.
 */
@Singleton
public class VecDSLLayoutRules {

	/** Statements whose clauses stay on one line: definitions read like declarations, groups like their members. */
	private static final Set<String> UNSPLIT_STATEMENTS = Set.of("Create", "Alter", "Drop", "Transaction", "BatchQuery");

	/** Rules whose braces enclose inline values rather than blocks. */
	private static final Set<String> INLINE_BRACE_RULES = Set.of("RecordLiteral", "SparseLit");

	private static final String SCRIPT = "Script";
	private static final String LABELED = "Labeled";

	private final Set<EObject> headerStarts = new HashSet<>();
	private final Set<EObject> statementStarts = new HashSet<>();
	private final Set<EObject> clauseStarts = new HashSet<>();
	private final Set<EObject> blockStarts = new HashSet<>();
	private final Set<Keyword> blockOpeners = new HashSet<>();
	private final Set<Keyword> blockClosers = new HashSet<>();
	private final Set<Keyword> recordListBrackets = new HashSet<>();

	@Inject
	public VecDSLLayoutRules(IGrammarAccess grammarAccess) {
		Grammar grammar = grammarAccess.getGrammar();
		for (ParserRule rule : GrammarUtil.allParserRules(grammar)) {
			if (GrammarUtil.isDatatypeRule(rule)) {
				continue;
			}
			if (isSplit(rule)) {
				collectClauses(rule);
			}
			collectBlocks(rule);
		}
		ParserRule records = (ParserRule) GrammarUtil.findRuleForName(grammar, "InlineRecords");
		for (Keyword keyword : GrammarUtil.containedKeywords(records)) {
			if (keyword.getValue().equals("[") || keyword.getValue().equals("]")) {
				recordListBrackets.add(keyword);
			}
		}
	}

	/** First token of a statement written directly in the script. */
	public boolean isStatementStart(EObject grammarElement) {
		return statementStarts.contains(grammarElement);
	}

	/** First token of a script header line: targets, version, a hint, a parameter. */
	public boolean isHeaderStart(EObject grammarElement) {
		return headerStarts.contains(grammarElement);
	}

	/** First token of a statement clause; it starts a line when the statement is laid out over several lines. */
	public boolean isClauseStart(EObject grammarElement) {
		return clauseStarts.contains(grammarElement);
	}

	/** First token of an element inside a block; it always starts a line. */
	public boolean isBlockStart(EObject grammarElement) {
		return blockStarts.contains(grammarElement);
	}

	public boolean isBlockOpener(EObject grammarElement) {
		return grammarElement instanceof Keyword keyword && blockOpeners.contains(keyword);
	}

	public boolean isBlockCloser(EObject grammarElement) {
		return grammarElement instanceof Keyword keyword && blockClosers.contains(keyword);
	}

	public boolean isRecordListBracket(EObject grammarElement) {
		return grammarElement instanceof Keyword keyword && recordListBrackets.contains(keyword);
	}

	/** Whether the rule's clauses are laid out one per line: the script header, statements and their fragments. */
	public boolean isSplit(AbstractRule rule) {
		if (!(rule instanceof ParserRule parserRule) || GrammarUtil.isDatatypeRule(parserRule)) {
			return false;
		}
		if (parserRule.isFragment()) {
			return !LABELED.equals(rule.getName());
		}
		if (SCRIPT.equals(rule.getName())) {
			return true;
		}
		EClassifier type = rule.getType() == null ? null : rule.getType().getClassifier();
		return type instanceof EClass eClass && VecDSLPackage.eINSTANCE.getMethod().isSuperTypeOf(eClass)
				&& eClass != VecDSLPackage.eINSTANCE.getMethod() && !eClass.isAbstract() && isConcreteStatement(rule)
				&& !UNSPLIT_STATEMENTS.contains(rule.getName());
	}

	private static boolean isConcreteStatement(AbstractRule rule) {
		return !(rule.getAlternatives() instanceof Alternatives alternatives
				&& alternatives.getElements().stream().allMatch(RuleCall.class::isInstance));
	}

	private void collectClauses(ParserRule rule) {
		List<AbstractElement> elements = topLevel(rule.getAlternatives());
		boolean script = SCRIPT.equals(rule.getName());
		int first = rule.isFragment() ? 0 : headLength(elements);
		for (int i = first; i < elements.size(); i++) {
			AbstractElement element = elements.get(i);
			if (isKeyword(element, "}")) {
				continue;
			}
			if (isKeyword(element, "{") || startsClause(element)) {
				Set<EObject> starts = first(element);
				if (script && element instanceof Assignment assignment && assignment.getFeature().equals("methods")) {
					statementStarts.addAll(starts);
				} else {
					(script ? headerStarts : clauseStarts).addAll(starts);
				}
			}
		}
	}

	/** The head stays on the statement's first line: its label, leading keywords and the first mandatory operand. */
	private static int headLength(List<AbstractElement> elements) {
		int i = 0;
		while (i < elements.size() && (elements.get(i) instanceof Action
				|| elements.get(i) instanceof RuleCall call && call.getRule().getName().equals(LABELED))) {
			i++;
		}
		while (i < elements.size() && elements.get(i) instanceof Keyword && elements.get(i).getCardinality() == null) {
			i++;
		}
		if (i < elements.size() && !GrammarUtil.isOptionalCardinality(elements.get(i))) {
			i++;
		}
		return i;
	}

	private void collectBlocks(ParserRule rule) {
		if (INLINE_BRACE_RULES.contains(rule.getName())) {
			return;
		}
		for (Keyword opener : GrammarUtil.containedKeywords(rule)) {
			if (!opener.getValue().equals("{") || !(opener.eContainer() instanceof Group group)) {
				continue;
			}
			List<AbstractElement> siblings = group.getElements();
			int start = siblings.indexOf(opener);
			for (int i = start + 1; i < siblings.size(); i++) {
				AbstractElement sibling = siblings.get(i);
				if (isKeyword(sibling, "}")) {
					blockOpeners.add(opener);
					blockClosers.add((Keyword) sibling);
					break;
				}
				for (AbstractElement element : topLevel(sibling)) {
					if (startsClause(element)) {
						blockStarts.addAll(first(element));
					}
				}
			}
		}
	}

	/** The elements of a mandatory group, flattened; any other element stands for itself. */
	private static List<AbstractElement> topLevel(AbstractElement element) {
		List<AbstractElement> result = new ArrayList<>();
		if (element instanceof Group group && group.getCardinality() == null) {
			for (AbstractElement child : group.getElements()) {
				result.addAll(topLevel(child));
			}
		} else if (element != null) {
			result.add(element);
		}
		return result;
	}

	private static boolean startsClause(AbstractElement element) {
		if (element.getCardinality() != null) {
			return true;
		}
		if (element instanceof Keyword keyword) {
			return keyword.getValue().matches("[a-z][a-z0-9-]*");
		}
		if (element instanceof Assignment assignment) {
			return isObjectRuleCall(assignment.getTerminal());
		}
		if (element instanceof Alternatives alternatives) {
			return alternatives.getElements().stream().allMatch(VecDSLLayoutRules::startsClause);
		}
		return isObjectRuleCall(element);
	}

	private static boolean isObjectRuleCall(EObject element) {
		return element instanceof RuleCall call && call.getRule() instanceof ParserRule rule
				&& !GrammarUtil.isDatatypeRule(rule) && !rule.isFragment();
	}

	/** Grammar elements the node of an element's first token, or one of its ancestors, can carry. */
	private static Set<EObject> first(AbstractElement element) {
		Set<EObject> result = new LinkedHashSet<>();
		if (element instanceof Keyword || element instanceof RuleCall) {
			result.add(element);
		} else if (element instanceof CrossReference reference) {
			result.add(reference);
			result.addAll(first(reference.getTerminal()));
		} else if (element instanceof Assignment assignment) {
			result.add(assignment);
			result.addAll(first(assignment.getTerminal()));
		} else if (element instanceof Group group) {
			for (AbstractElement child : group.getElements()) {
				if (child instanceof Action) {
					continue;
				}
				result.addAll(first(child));
				if (!GrammarUtil.isOptionalCardinality(child)) {
					break;
				}
			}
		} else if (element instanceof Alternatives || element instanceof UnorderedGroup) {
			for (AbstractElement child : EcoreUtil2.typeSelect(element.eContents(), AbstractElement.class)) {
				result.addAll(first(child));
			}
		}
		return result;
	}

	private static boolean isKeyword(EObject element, String value) {
		return element instanceof Keyword keyword && keyword.getValue().equals(value) && keyword.getCardinality() == null;
	}
}
