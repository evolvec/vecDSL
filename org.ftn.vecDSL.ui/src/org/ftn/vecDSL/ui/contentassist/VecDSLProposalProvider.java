package org.ftn.vecDSL.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ConfigurableCompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.GrammarDocs;
import org.ftn.vecDSL.editor.HoverText;
import org.ftn.vecDSL.editor.ProposalRanking;
import org.ftn.vecDSL.editor.TargetStatus;
import org.ftn.vecDSL.vecDSL.VectorCollection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Vendor-aware proposals in the Eclipse editor: the proposals, statuses and ranking of {@link ProposalRanking} as
 * Eclipse completion proposals (status after the keyword, warning icon, keyword hover as additional information).
 */
public class VecDSLProposalProvider extends AbstractVecDSLProposalProvider {

	@Inject
	private Provider<CoverageIndex> coverage;

	@Override
	public void completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		EObject model = context.getCurrentModel();
		if (ProposalRanking.hidesMetric(keyword, model)) {
			return;
		}
		TargetStatus status = ProposalRanking.status(keyword, model, context.getRootModel(), coverage.get());
		if (status == null) {
			super.completeKeyword(keyword, context, acceptor);
			return;
		}
		StyledString display = new StyledString(keyword.getValue()).append("  " + status.summary(),
				StyledString.QUALIFIER_STYLER);
		Image image = status.isUnsupported()
				? PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK)
				: getImage(keyword);
		ICompletionProposal proposal = createCompletionProposal(keyword.getValue(), display, image, context);
		if (proposal instanceof ConfigurableCompletionProposal configurable) {
			getPriorityHelper().adjustKeywordPriority(proposal, context.getPrefix());
			configurable.setPriority(ProposalRanking.keywordPriority(status, configurable.getPriority()));
			configurable.setAdditionalProposalInfo(HoverText.keyword(keyword.getValue(),
					GrammarUtil.containingRule(keyword).getName(), GrammarDocs.ofLanguage(), status));
		}
		acceptor.accept(proposal);
	}

	@Override
	protected void lookupCrossReference(CrossReference crossReference, EReference reference,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor, Predicate<IEObjectDescription> filter) {
		VectorCollection scope = ProposalRanking.fieldScope(reference.getEReferenceType(), context.getCurrentModel());
		if (scope == null) {
			super.lookupCrossReference(crossReference, reference, context, acceptor, filter);
			return;
		}
		String ruleName = crossReference.getTerminal() instanceof RuleCall call ? call.getRule().getName() : null;
		Function<IEObjectDescription, ICompletionProposal> factory = getProposalFactory(ruleName, context);
		lookupCrossReference(context.getCurrentModel(), reference, acceptor, filter, candidate -> {
			ICompletionProposal proposal = factory.apply(candidate);
			if (proposal instanceof ConfigurableCompletionProposal configurable) {
				configurable.setPriority(ProposalRanking.referencePriority(configurable.getPriority(), candidate, scope));
			}
			return proposal;
		});
	}
}
