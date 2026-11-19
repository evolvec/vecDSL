package org.ftn.vecDSL.formatting2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.ITextReplacer;
import org.eclipse.xtext.formatting2.ITextReplacerContext;
import org.eclipse.xtext.formatting2.internal.MultilineCommentReplacer;
import org.eclipse.xtext.formatting2.regionaccess.IComment;
import org.eclipse.xtext.formatting2.regionaccess.IEObjectRegion;
import org.eclipse.xtext.formatting2.regionaccess.IHiddenRegion;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.IntegerKey;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.ftn.vecDSL.vecDSL.InlineRecords;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.RecordLiteral;

import com.google.inject.Inject;

/**
 * Lays out vecDSL like SQL. A statement keeps its head (label, statement keywords, first operand) on the first line
 * and puts each further clause on a line of its own, one level deeper; a statement without a block that fits in
 * {@link #SHORT_STATEMENT_WIDTH} columns stays on one line. Block elements go one per line, one level deeper than the
 * line that opens the block, and so do the records of a value list with two or more records. Statements are separated
 * by one blank line. Only whitespace changes; the layout depends on the tokens alone, so formatting is idempotent.
 * A document with syntax errors is left as it is, since its statements cannot be told apart reliably.
 */
public class VecDSLFormatter extends AbstractFormatter2 {

	/** Widest statement, in columns including its indentation, that stays on one line when it has no block. */
	public static final IntegerKey SHORT_STATEMENT_WIDTH = new IntegerKey("vecdsl.format.shortStatementWidth", 100);

	private static final Set<String> NO_SPACE_BEFORE = Set.of(",", ":", ")", "]", ".");
	private static final Set<String> NO_SPACE_AFTER = Set.of("(", "[", "$", ".");

	private enum Role {
		NONE, CLAUSE, HEADER, BLOCK, STATEMENT
	}

	@Inject
	private VecDSLLayoutRules rules;

	@Override
	public void format(Object object, IFormattableDocument document) {
		if (object instanceof XtextResource resource && resource.getParseResult() != null
				&& !resource.getParseResult().hasSyntaxErrors()) {
			new Layout(document, resource.getParseResult().getRootNode()).apply();
		}
	}

	/**
	 * Keeps the lines of a multi-line comment as written. The default replacer puts " * " in front of every inner line,
	 * which changes more than whitespace.
	 */
	@Override
	public ITextReplacer createCommentReplacer(IComment comment) {
		ITextReplacer replacer = super.createCommentReplacer(comment);
		if (replacer instanceof MultilineCommentReplacer) {
			return new MultilineCommentReplacer(comment, '*') {
				@Override
				public ITextReplacerContext createReplacements(ITextReplacerContext context) {
					return context;
				}
			};
		}
		return replacer;
	}

	private final class Layout {

		private final IFormattableDocument document;
		private final ICompositeNode root;
		private final Map<Method, Boolean> expanded = new HashMap<>();
		private final Map<Method, IHiddenRegion> clauseIndent = new LinkedHashMap<>();
		private final Deque<ISemanticRegion> openBlocks = new ArrayDeque<>();
		private Method owner;

		Layout(IFormattableDocument document, ICompositeNode root) {
			this.document = document;
			this.root = root;
		}

		void apply() {
			List<ISemanticRegion> tokens = new ArrayList<>();
			getTextRegionAccess().regionForRootEObject().getAllSemanticRegions().forEach(tokens::add);
			for (int i = 1; i < tokens.size(); i++) {
				ISemanticRegion previous = tokens.get(i - 1);
				ISemanticRegion token = tokens.get(i);
				Role role = role(token);
				document.set(token.getPreviousHiddenRegion(), gap(previous, token, role));
				track(previous, token, role);
			}
			if (!tokens.isEmpty()) {
				document.set(tokens.get(tokens.size() - 1).getNextHiddenRegion(), it -> it.setNewLines(1));
			}
			clauseIndent.forEach((statement, start) -> indent(start, getTextRegionAccess().regionForEObject(statement)
					.getNextHiddenRegion()));
		}

		private Procedure1<? super IHiddenRegionFormatter> gap(ISemanticRegion previous, ISemanticRegion token,
				Role role) {
			if (role == Role.STATEMENT) {
				return it -> it.setNewLines(2);
			}
			if (rules.isBlockOpener(previous.getGrammarElement())) {
				return rules.isBlockCloser(token.getGrammarElement()) ? IHiddenRegionFormatter::noSpace
						: it -> it.setNewLines(1);
			}
			if (rules.isBlockCloser(token.getGrammarElement()) || isRecordListBracket(previous, "[")
					|| isRecordListBracket(token, "]") || isListedRecord(token)) {
				return it -> it.setNewLines(1);
			}
			if (role == Role.BLOCK) {
				return it -> it.setNewLines(1, 1, 2);
			}
			if (role == Role.HEADER || role == Role.CLAUSE && isExpanded(owner)) {
				return it -> it.setNewLines(1);
			}
			return space(previous, token) ? IHiddenRegionFormatter::oneSpace : IHiddenRegionFormatter::noSpace;
		}

		private void track(ISemanticRegion previous, ISemanticRegion token, Role role) {
			if (rules.isBlockOpener(token.getGrammarElement()) || isRecordListBracket(token, "[")) {
				openBlocks.push(token);
			} else if ((rules.isBlockCloser(token.getGrammarElement()) || isRecordListBracket(token, "]"))
					&& !openBlocks.isEmpty()) {
				ISemanticRegion opener = openBlocks.pop();
				if (opener != previous) {
					indent(opener.getNextHiddenRegion(), token.getPreviousHiddenRegion());
				}
			}
			if (role == Role.CLAUSE && isExpanded(owner) && !clauseIndent.containsKey(owner)) {
				clauseIndent.put(owner, token.getPreviousHiddenRegion());
			}
		}

		private void indent(IHiddenRegion first, IHiddenRegion last) {
			if (first != null && last != null && first != last) {
				document.set(first, last, IHiddenRegionFormatter::indent);
			}
		}

		/**
		 * The line role of a token, read from the grammar elements of its leaf node and of every enclosing node it
		 * begins. Sets {@link #owner} to the statement a clause belongs to.
		 */
		private Role role(ISemanticRegion token) {
			ILeafNode leaf = NodeModelUtils.findLeafNodeAtOffset(root, token.getOffset());
			if (leaf == null) {
				return Role.NONE;
			}
			Role role = Role.NONE;
			for (INode node = leaf; node != null && firstToken(node) == leaf; node = node.getParent()) {
				EObject element = node.getGrammarElement();
				Role candidate = rules.isStatementStart(element) ? Role.STATEMENT
						: rules.isBlockStart(element) ? Role.BLOCK
								: rules.isHeaderStart(element) ? Role.HEADER
										: rules.isClauseStart(element) ? Role.CLAUSE : Role.NONE;
				if (candidate.compareTo(role) > 0) {
					role = candidate;
				}
			}
			if (role == Role.CLAUSE) {
				owner = EcoreUtil2.getContainerOfType(token.getSemanticElement(), Method.class);
				if (owner == null) {
					return Role.NONE;
				}
			}
			return role;
		}

		private ILeafNode firstToken(INode node) {
			if (node instanceof ILeafNode leaf) {
				return leaf;
			}
			for (ILeafNode leaf : node.getLeafNodes()) {
				if (!leaf.isHidden()) {
					return leaf;
				}
			}
			return null;
		}

		private boolean isExpanded(Method statement) {
			return statement != null && expanded.computeIfAbsent(statement, this::needsSeveralLines);
		}

		private boolean needsSeveralLines(Method statement) {
			IEObjectRegion region = getTextRegionAccess().regionForEObject(statement);
			if (region == null) {
				return false;
			}
			int width = indentation(statement);
			ISemanticRegion previous = null;
			for (ISemanticRegion token : region.getAllSemanticRegions()) {
				if (rules.isBlockOpener(token.getGrammarElement()) || isRecordListBracket(token, "[")) {
					return true;
				}
				if (previous != null) {
					if (token.getPreviousHiddenRegion().containsComment()) {
						return true;
					}
					width += space(previous, token) ? 1 : 0;
				}
				width += token.getLength();
				previous = token;
			}
			return width > getPreference(SHORT_STATEMENT_WIDTH);
		}

		private int indentation(Method statement) {
			int depth = 0;
			for (EObject container = statement.eContainer(); container instanceof Method; container = container
					.eContainer()) {
				depth++;
			}
			int columns = 0;
			for (char c : getPreference(FormatterPreferenceKeys.indentation).toCharArray()) {
				columns += c == '\t' ? getPreference(FormatterPreferenceKeys.tabWidth) : 1;
			}
			return depth * columns;
		}

		/** The brackets of a value list of two or more records, which lay the records out one per line. */
		private boolean isRecordListBracket(ISemanticRegion token, String value) {
			return rules.isRecordListBracket(token.getGrammarElement()) && keyword(token, value)
					&& token.getSemanticElement() instanceof InlineRecords records && records.getRecords().size() > 1;
		}

		private boolean isListedRecord(ISemanticRegion token) {
			return keyword(token, "{") && token.getSemanticElement() instanceof RecordLiteral record
					&& record.eContainer() instanceof InlineRecords records && records.getRecords().size() > 1;
		}

		private boolean space(ISemanticRegion previous, ISemanticRegion token) {
			if (keyword(token, "}") && keyword(previous, "{")) {
				return false;
			}
			if (token.getGrammarElement() instanceof Keyword next && NO_SPACE_BEFORE.contains(next.getValue())) {
				return false;
			}
			if (previous.getGrammarElement() instanceof Keyword last && NO_SPACE_AFTER.contains(last.getValue())) {
				return false;
			}
			return !(keyword(previous, "env") && keyword(token, "("));
		}

		private boolean keyword(ISemanticRegion token, String value) {
			return token.getGrammarElement() instanceof Keyword keyword && keyword.getValue().equals(value);
		}
	}
}
