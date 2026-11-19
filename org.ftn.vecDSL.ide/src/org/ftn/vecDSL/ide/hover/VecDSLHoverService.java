package org.ftn.vecDSL.ide.hover;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.hover.HoverContext;
import org.eclipse.xtext.ide.server.hover.HoverService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.TextRegion;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.HoverContent;
import org.ftn.vecDSL.editor.HoverDocument;
import org.ftn.vecDSL.editor.HoverTarget;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The hovers of {@link HoverContent} as Markdown for textDocument/hover: the same elements and regions as the Eclipse
 * hover ({@link HoverTarget} before Xtext's cross reference and declaration lookup) and the same content.
 */
public class VecDSLHoverService extends HoverService {

	@Inject
	private Provider<CoverageIndex> coverage;

	@Override
	protected HoverContext createContext(Document document, XtextResource resource, int offset) {
		HoverTarget target = HoverTarget.at(resource, offset);
		return target != null
				? new HoverContext(document, resource, offset, new TextRegion(target.offset(), target.length()),
						target.element())
				: super.createContext(document, resource, offset);
	}

	@Override
	protected MarkupContent getMarkupContent(HoverContext context) {
		ITextRegion region = context.getRegion();
		Keyword keyword = HoverTarget.keyword(context.getResource(), region.getOffset(), region.getLength());
		HoverDocument content = HoverContent.of(context.getElement(), keyword, coverage.get());
		return content != null ? toMarkupContent(getKind(context), content.markdown())
				: super.getMarkupContent(context);
	}
}
