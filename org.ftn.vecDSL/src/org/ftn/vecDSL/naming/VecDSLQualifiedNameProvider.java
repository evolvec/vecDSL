package org.ftn.vecDSL.naming;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.util.SimpleAttributeResolver;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.VectorField;

/**
 * Simple names for all named concepts. Statement labels are not exported, and an unnamed
 * vector field is named {@value #DEFAULT_VECTOR_FIELD}.
 */
public class VecDSLQualifiedNameProvider extends IQualifiedNameProvider.AbstractImpl {

	public static final String DEFAULT_VECTOR_FIELD = "vector";

	@Override
	public QualifiedName getFullyQualifiedName(EObject object) {
		if (object instanceof Method) {
			return null;
		}
		String name = SimpleAttributeResolver.NAME_RESOLVER.apply(object);
		if (name == null && object instanceof VectorField) {
			name = DEFAULT_VECTOR_FIELD;
		}
		return name == null || name.isEmpty() ? null : QualifiedName.create(name);
	}
}
