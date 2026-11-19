package org.ftn.vecDSL.coverage;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/**
 * Construct ids of COVERAGE.md and the metamodel elements they name: "&lt;Kind&gt; &lt;Class&gt;" for a concrete
 * Method, Concept, AlterOperation or ExtensionItem class, "&lt;Class&gt;.&lt;feature&gt;" for a feature of a Method
 * class.
 */
public final class ConstructIds {

	private static final VecDSLPackage PACKAGE = VecDSLPackage.eINSTANCE;
	private static final List<EClass> KINDS = List.of(PACKAGE.getMethod(), PACKAGE.getConcept(),
			PACKAGE.getAlterOperation(), PACKAGE.getExtensionItem());

	private ConstructIds() {
	}

	/** The class row id, or null for a class no matrix lists. */
	public static String of(EClass type) {
		for (EClass kind : KINDS) {
			if (kind != type && kind.isSuperTypeOf(type)) {
				return kind.getName() + " " + type.getName();
			}
		}
		return null;
	}

	public static String of(EClass type, EStructuralFeature feature) {
		return type.getName() + "." + feature.getName();
	}

	/** The class a construct id names, or null. */
	public static EClass eClass(String construct) {
		int dot = construct.indexOf('.');
		String name = dot >= 0 ? construct.substring(0, dot) : construct.substring(construct.indexOf(' ') + 1);
		EClassifier classifier = PACKAGE.getEClassifier(name);
		if (!(classifier instanceof EClass type)) {
			return null;
		}
		return dot >= 0 || construct.equals(of(type)) ? type : null;
	}

	/** The feature a "&lt;Class&gt;.&lt;feature&gt;" id names, or null. */
	public static EStructuralFeature feature(String construct) {
		int dot = construct.indexOf('.');
		EClass type = dot < 0 ? null : eClass(construct);
		return type == null ? null : type.getEStructuralFeature(construct.substring(dot + 1));
	}
}
