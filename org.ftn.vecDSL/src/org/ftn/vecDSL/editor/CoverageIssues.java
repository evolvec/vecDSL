package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.LineAndColumn;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * The {@code vecdsl.coverage.unsupported} warnings of a program: one per construct a targeted vendor does not support
 * according to its COVERAGE.md ({@link CoverageCheck}). Shared by the Eclipse editor, vecdsl-cli and the language server;
 * the language validator is not involved.
 */
public final class CoverageIssues {

	private CoverageIssues() {
	}

	public static List<Issue> issues(XtextResource resource, CoverageIndex index) {
		if (resource.getContents().isEmpty() || !(resource.getContents().get(0) instanceof Script script)
				|| script.getTargets().isEmpty()) {
			return List.of();
		}
		List<Issue> issues = new ArrayList<>();
		for (CoverageCheck.Finding finding : CoverageCheck.findings(script, index)) {
			INode node = finding.node();
			if (node != null) {
				issues.add(issue(finding, node));
			}
		}
		return issues;
	}

	private static Issue issue(CoverageCheck.Finding finding, INode node) {
		Issue.IssueImpl issue = new Issue.IssueImpl();
		issue.setCode(CoverageCheck.UNSUPPORTED);
		issue.setSeverity(Severity.WARNING);
		issue.setType(CheckType.FAST);
		issue.setMessage(finding.message());
		issue.setUriToProblem(EcoreUtil.getURI(finding.object()));
		issue.setOffset(node.getOffset());
		issue.setLength(node.getLength());
		LineAndColumn start = NodeModelUtils.getLineAndColumn(node, node.getOffset());
		LineAndColumn end = NodeModelUtils.getLineAndColumn(node, node.getEndOffset());
		issue.setLineNumber(start.getLine());
		issue.setColumn(start.getColumn());
		issue.setLineNumberEnd(end.getLine());
		issue.setColumnEnd(end.getColumn());
		issue.setData(new String[] { finding.entry().vendor(),
				finding.feature() == null ? "" : finding.feature().getName() });
		return issue;
	}
}
