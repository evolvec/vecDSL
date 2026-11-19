package org.ftn.vecDSL.editor;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.coverage.ConstructIds;

/** A construct of the coverage matrices: a class, or a feature of a class when the keyword governs one. */
public record Construct(EClass type, EStructuralFeature feature) {

	public String id() {
		return feature != null ? ConstructIds.of(type, feature) : ConstructIds.of(type);
	}
}
