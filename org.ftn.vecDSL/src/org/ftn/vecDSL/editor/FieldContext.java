package org.ftn.vecDSL.editor;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * The collection whose fields a field reference should name first: the collection the statement addresses, or the
 * collection a create defines or names when a definition is nested in its 'with { }' block.
 */
public final class FieldContext {

	private FieldContext() {
	}

	public static VectorCollection collection(EObject context) {
		VectorCollection target = ModelUtil.targetCollection(context);
		if (target != null) {
			return target;
		}
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof Create create) {
				EObject concept = create.getDefinition() != null ? create.getDefinition() : create.getTarget();
				if (concept instanceof VectorCollection collection && !collection.eIsProxy()) {
					return collection;
				}
			}
		}
		return null;
	}

	/** Whether the object at the URI is contained, at any depth, in the collection. */
	public static boolean isMember(URI candidate, VectorCollection collection) {
		URI owner = EcoreUtil.getURI(collection);
		return candidate != null && owner != null && candidate.trimFragment().equals(owner.trimFragment())
				&& candidate.fragment() != null && candidate.fragment().startsWith(owner.fragment() + "/");
	}
}
