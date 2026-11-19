package org.ftn.vecDSL.ide.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.GrammarDocs;
import org.ftn.vecDSL.editor.HoverContent;
import org.ftn.vecDSL.editor.ProposalRanking;
import org.ftn.vecDSL.editor.TargetStatus;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Vendor-aware proposals for textDocument/completion: the proposals, statuses and ranking of {@link ProposalRanking}.
 * A keyword's description (the completion item's detail) is the status of its construct for every target, its
 * documentation the keyword hover in Markdown.
 */
public class VecDSLIdeContentProposalProvider extends IdeContentProposalProvider {

	@Inject
	private Provider<CoverageIndex> coverage;

	@Override
	protected void _createProposals(Keyword keyword, ContentAssistContext context,
			IIdeContentProposalAcceptor acceptor) {
		EObject model = context.getCurrentModel();
		if (ProposalRanking.hidesMetric(keyword, model)) {
			return;
		}
		TargetStatus status = ProposalRanking.status(keyword, model, context.getRootModel(), coverage.get());
		if (status == null) {
			super._createProposals(keyword, context, acceptor);
			return;
		}
		ContentAssistEntry entry = getProposalCreator().createProposal(keyword.getValue(), context,
				ContentAssistEntry.KIND_KEYWORD, proposal -> {
					proposal.setDescription(status.summary());
					proposal.setDocumentation(HoverContent.keyword(keyword.getValue(),
							GrammarUtil.containingRule(keyword).getName(), GrammarDocs.ofLanguage(), status).markdown());
				});
		if (entry != null) {
			acceptor.accept(entry, ProposalRanking.keywordPriority(status,
					getProposalPriorities().getKeywordPriority(keyword.getValue(), entry)));
		}
	}

	@Override
	protected void _createProposals(CrossReference reference, ContentAssistContext context,
			IIdeContentProposalAcceptor acceptor) {
		super._createProposals(reference, context, new IIdeContentProposalAcceptor() {

			@Override
			public void accept(ContentAssistEntry entry, int priority) {
				acceptor.accept(entry, entry != null && entry.getSource() instanceof IEObjectDescription candidate
						? ProposalRanking.referencePriority(priority, candidate,
								ProposalRanking.fieldScope(candidate.getEClass(), context.getCurrentModel()))
						: priority);
			}

			@Override
			public boolean canAcceptMoreProposals() {
				return acceptor.canAcceptMoreProposals();
			}
		});
	}
}
