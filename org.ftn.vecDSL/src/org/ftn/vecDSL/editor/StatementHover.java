package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.HoverDocument.Block;
import org.ftn.vecDSL.editor.HoverDocument.Bold;
import org.ftn.vecDSL.editor.HoverDocument.Code;
import org.ftn.vecDSL.editor.HoverDocument.Items;
import org.ftn.vecDSL.editor.HoverDocument.Lines;
import org.ftn.vecDSL.editor.HoverDocument.Paragraph;
import org.ftn.vecDSL.editor.HoverDocument.Span;
import org.ftn.vecDSL.editor.HoverDocument.Text;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/**
 * The hover over a statement's label in the Eclipse editor: the code vecdsl gen writes
 * for that statement ({@link StatementCode}) for the first vendor of the script's targets list, in the script's
 * language hint or else Python. First the reasons that vendor does not support constructs of the statement (the text of
 * the editor warnings), then each section of the statement's code with its file and 1-based lines, at most
 * {@link #MAX_LINES} lines in all. Instead of code, a note when the script declares no targets or has errors.
 */
public final class StatementHover {

	/** The most code lines a hover shows; a cut section ends with a line "...". */
	public static final int MAX_LINES = 30;

	private static final Map<String, String> LANGUAGE_NAMES = Map.of("python", "Python", "typescript", "TypeScript",
			"java", "Java");

	private StatementHover() {
	}

	/** The labeled statement whose label is exactly the region, or null. */
	public static Method labeled(XtextResource resource, int offset, int length) {
		if (resource == null || resource.getParseResult() == null) {
			return null;
		}
		INode leaf = NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset);
		if (leaf == null || !(NodeModelUtils.findActualSemanticObjectFor(leaf) instanceof Method method)
				|| method.getName() == null) {
			return null;
		}
		List<INode> labels = NodeModelUtils.findNodesForFeature(method, VecDSLPackage.eINSTANCE.getMethod_Name());
		return !labels.isEmpty() && labels.get(0).getOffset() == offset && labels.get(0).getLength() == length ? method
				: null;
	}

	/** The host language the hover shows: the script's hint when a generator writes it, otherwise python. */
	public static String language(Script script) {
		String hint = script != null && ModelUtil.written(script, "hostLanguage") ? script.getHostLanguage().getLiteral()
				: null;
		return hint != null && LANGUAGE_NAMES.containsKey(hint) ? hint : "python";
	}

	public static HoverDocument of(Method statement, CoverageIndex index, CancelIndicator cancel) {
		XtextResource resource = (XtextResource) statement.eResource();
		Script script = EcoreUtil2.getContainerOfType(statement, Script.class);
		List<String> targets = TargetStatus.targets(script);
		List<Block> blocks = new ArrayList<>();
		if (targets.isEmpty()) {
			blocks.add(paragraph(new Bold(statement.getName()), new Text(": the script declares no targets [...]; "
					+ "vecdsl gen --target <vendor> writes code for the vendor it is given.")));
			return new HoverDocument(blocks);
		}
		String target = targets.get(0);
		String language = language(script);
		blocks.add(paragraph(new Bold(statement.getName()), new Text(": the code vecdsl gen writes for "),
				new Code(target), new Text(", " + LANGUAGE_NAMES.get(language)
						+ (targets.size() > 1 ? " (also targeted: " + String.join(", ", targets.subList(1, targets.size()))
								+ ")" : ""))));
		List<CoverageCheck.Finding> findings = CoverageCheck.findings(statement, List.of(target), index);
		if (!findings.isEmpty()) {
			blocks.add(new Items(findings.stream().map(finding -> List.<Span>of(new Text(finding.message()))).toList()));
		}
		List<Issue> errors = resource.getResourceServiceProvider().getResourceValidator()
				.validate(resource, CheckMode.ALL, cancel).stream().filter(issue -> issue.getSeverity() == Severity.ERROR)
				.toList();
		if (!errors.isEmpty()) {
			Issue first = errors.get(0);
			blocks.add(paragraph(new Text("The program has " + errors.size() + (errors.size() == 1 ? " error" : " errors")
					+ ", so vecdsl gen writes no code; line " + first.getLineNumber() + ": " + first.getMessage())));
			return new HoverDocument(blocks);
		}
		StatementCode.Code code = StatementCode.generate(resource, statement, StatementCode.targets(target),
				StatementCode.languages(language), cancel);
		if (code.sections().isEmpty()) {
			blocks.add(paragraph(new Text("No lines of the generated files belong to this statement.")));
		}
		int budget = MAX_LINES;
		for (StatementCode.Section section : code.sections()) {
			if (budget == 0) {
				blocks.add(paragraph(new Text("...")));
				break;
			}
			List<String> lines = code.files().get(section.path()).lines().toList()
					.subList(section.start(), section.end());
			blocks.add(paragraph(new Code(section.path()), new Text(section.end() - section.start() == 1
					? ", line " + (section.start() + 1) : ", lines " + (section.start() + 1) + " to " + section.end())));
			List<String> shown = new ArrayList<>(lines.subList(0, Math.min(budget, lines.size())));
			budget -= shown.size();
			if (shown.size() < lines.size()) {
				shown.add("...");
			}
			blocks.add(new Lines(shown));
		}
		return new HoverDocument(blocks);
	}

	private static Paragraph paragraph(Span... spans) {
		return new Paragraph(List.of(spans));
	}
}
