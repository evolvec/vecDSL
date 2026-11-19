package org.ftn.vecDSL.ui.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;
import org.eclipse.xtext.util.CancelIndicator;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.HoverContent;
import org.ftn.vecDSL.editor.HoverDocument;
import org.ftn.vecDSL.editor.HoverTarget;
import org.ftn.vecDSL.editor.StatementHover;
import org.ftn.vecDSL.vecDSL.Method;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The hovers of {@link HoverContent} as HTML in the Eclipse editor, and over a statement's label the statement's
 * generated code ({@link StatementHover}). JFace computes hovers off the UI thread (TextViewerHoverManager).
 */
public class VecDSLHoverProvider extends DefaultEObjectHoverProvider {

	@Inject
	private Provider<CoverageIndex> coverage;

	/** The keyword of the hover being built; set while {@link #getHoverInfoAsHtml(EObject)} runs for it. */
	private Keyword hovered;

	/** The statement whose label is hovered; set while {@link #getHoverInfoAsHtml(EObject)} runs for it. */
	private Method statement;

	@Override
	protected XtextBrowserInformationControlInput getHoverInfo(EObject element, IRegion region,
			XtextBrowserInformationControlInput previous) {
		XtextResource resource = region != null && element.eResource() instanceof XtextResource candidate ? candidate
				: null;
		hovered = resource == null ? null : HoverTarget.keyword(resource, region.getOffset(), region.getLength());
		statement = resource == null ? null : StatementHover.labeled(resource, region.getOffset(), region.getLength());
		try {
			return super.getHoverInfo(element, region, previous);
		} finally {
			hovered = null;
			statement = null;
		}
	}

	@Override
	protected boolean hasHover(EObject object) {
		return object == statement || HoverContent.describes(object, hovered) || super.hasHover(object);
	}

	@Override
	protected String getHoverInfoAsHtml(EObject object) {
		if (object == statement) {
			return StatementHover.of(statement, coverage.get(), CancelIndicator.NullImpl).html();
		}
		HoverDocument document = HoverContent.of(object, hovered, coverage.get());
		return document != null ? document.html() : super.getHoverInfoAsHtml(object);
	}
}
