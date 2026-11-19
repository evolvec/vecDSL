package org.ftn.vecDSL.ui.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hover.DispatchingEObjectTextHover;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import org.ftn.vecDSL.editor.HoverTarget;

/**
 * Adds hovers over keywords and over the whole text of env("NAME") ({@link HoverTarget});
 * {@link VecDSLHoverProvider} recognizes a keyword region and describes the keyword's construct.
 */
public class VecDSLEObjectTextHover extends DispatchingEObjectTextHover {

	@Override
	protected Pair<EObject, IRegion> getXtextElementAt(XtextResource resource, int offset) {
		HoverTarget target = HoverTarget.at(resource, offset);
		return target != null ? Tuples.create(target.element(), new Region(target.offset(), target.length()))
				: super.getXtextElementAt(resource, offset);
	}
}
