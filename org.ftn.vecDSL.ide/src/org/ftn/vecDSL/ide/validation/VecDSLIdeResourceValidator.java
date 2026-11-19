package org.ftn.vecDSL.ide.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ResourceValidatorImpl;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.CoverageIssues;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The issues of the language validator plus, in a FAST check, a warning for each construct a targeted vendor does not
 * support according to its COVERAGE.md ({@link CoverageIssues}). Bound by the language server (VecDSLIdeModule) and
 * by the Eclipse editor and builder (VecDSLUiModule); the language validator and its results are unchanged.
 */
public class VecDSLIdeResourceValidator extends ResourceValidatorImpl {

	@Inject
	private Provider<CoverageIndex> coverage;

	@Override
	public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator monitor)
			throws OperationCanceledError {
		List<Issue> issues = super.validate(resource, mode, monitor);
		if (!mode.shouldCheck(CheckType.FAST) || !(resource instanceof XtextResource xtext)) {
			return issues;
		}
		List<Issue> result = new ArrayList<>(issues);
		result.addAll(CoverageIssues.issues(xtext, coverage.get()));
		return result;
	}
}
