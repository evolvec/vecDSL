package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;

/** The grammar order of the clauses of the fixed-order blocks, read from the grammar. */
public final class ClauseOrder {

	/** Word that opens a block and the rule of the block. */
	private static final Map<String, String> BLOCKS = Map.of("params", "SearchParams", "vector", "VectorLeg", "keyword",
			"KeywordLeg");

	private ClauseOrder() {
	}

	/** Clause keywords in grammar order, keyed by the word that opens the block. */
	public static Map<String, List<String>> blocks(Grammar grammar) {
		Map<String, List<String>> result = new LinkedHashMap<>();
		BLOCKS.forEach((opener, rule) -> result.put(opener, inBraces((ParserRule) GrammarUtil.findRuleForName(grammar, rule))));
		return result;
	}

	private static List<String> inBraces(ParserRule rule) {
		List<String> order = new ArrayList<>();
		boolean inside = false;
		for (AbstractElement element : ((Group) rule.getAlternatives()).getElements()) {
			if (element instanceof Keyword keyword && keyword.getValue().equals("{")) {
				inside = true;
			} else if (element instanceof Keyword keyword && keyword.getValue().equals("}")) {
				break;
			} else if (inside) {
				for (String first : firstKeywords(element)) {
					if (!order.contains(first)) {
						order.add(first);
					}
				}
			}
		}
		return order;
	}

	private static Set<String> firstKeywords(AbstractElement element) {
		Set<String> result = new LinkedHashSet<>();
		if (element instanceof Keyword keyword) {
			result.add(keyword.getValue());
		} else if (element instanceof Assignment assignment) {
			result.addAll(firstKeywords(assignment.getTerminal()));
		} else if (element instanceof RuleCall call && call.getRule() instanceof ParserRule rule
				&& !GrammarUtil.isDatatypeRule(rule)) {
			result.addAll(firstKeywords(rule.getAlternatives()));
		} else if (element instanceof Group group) {
			for (AbstractElement child : group.getElements()) {
				if (!(child instanceof Action)) {
					result.addAll(firstKeywords(child));
					if (!GrammarUtil.isOptionalCardinality(child)) {
						break;
					}
				}
			}
		} else if (element instanceof Alternatives alternatives) {
			alternatives.getElements().forEach(child -> result.addAll(firstKeywords(child)));
		}
		return result;
	}

	/** Every keyword of the grammar that is a plain word, the names the parser rejects. */
	public static Set<String> keywordWords(Grammar grammar) {
		Set<String> result = new LinkedHashSet<>();
		for (AbstractRule rule : GrammarUtil.allRules(grammar)) {
			for (Keyword keyword : GrammarUtil.containedKeywords(rule)) {
				if (keyword.getValue().matches("[a-z][a-z0-9_]*")) {
					result.add(keyword.getValue());
				}
			}
		}
		result.removeAll(softKeywords(grammar));
		return result;
	}

	/** Keywords that ValidID admits as names. */
	private static Set<String> softKeywords(Grammar grammar) {
		Set<String> result = new LinkedHashSet<>();
		GrammarUtil.containedKeywords(GrammarUtil.findRuleForName(grammar, "ValidID")).forEach(k -> result.add(k.getValue()));
		return result;
	}
}
