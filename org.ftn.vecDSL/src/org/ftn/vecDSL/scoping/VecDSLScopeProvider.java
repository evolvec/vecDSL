package org.ftn.vecDSL.scoping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;

import com.google.inject.Inject;

/**
 * Field, partition and tenant references resolve by simple name within the collection the
 * statement addresses. Nested sub-fields resolve along dotted field paths and inside
 * element scopes.
 */
public class VecDSLScopeProvider extends AbstractVecDSLScopeProvider {

	private static final VecDSLPackage PACKAGE = VecDSLPackage.eINSTANCE;

	@Inject
	private IQualifiedNameProvider names;

	@Override
	public IScope getScope(EObject context, EReference reference) {
		EClass type = reference.getEReferenceType();
		if (PACKAGE.getField().isSuperTypeOf(type)) {
			IScope scope = fieldScope(context, reference, type);
			if (scope != null) {
				return scope;
			}
		} else if (type == PACKAGE.getPartition() || type == PACKAGE.getTenant()) {
			VectorCollection collection = ModelUtil.targetCollection(context);
			if (collection != null) {
				return scope(type == PACKAGE.getPartition() ? ModelUtil.partitions(collection) : collection.getTenants(),
						IScope.NULLSCOPE);
			}
		}
		return super.getScope(context, reference);
	}

	private IScope fieldScope(EObject context, EReference reference, EClass type) {
		if (reference == PACKAGE.getFieldPath_SubFields() && context instanceof FieldPath path) {
			return path.getField() == null ? IScope.NULLSCOPE
					: scope(ofType(ModelUtil.descendants(path.getField()), type), IScope.NULLSCOPE);
		}
		VectorCollection collection = ModelUtil.targetCollection(context);
		Field element = ModelUtil.enclosingElementField(context);
		if (collection == null && element == null) {
			return null;
		}
		IScope outer = collection == null ? IScope.NULLSCOPE : scope(ofType(collection.getFields(), type), IScope.NULLSCOPE);
		return element == null ? outer : scope(ofType(ModelUtil.descendants(element), type), outer);
	}

	private IScope scope(Iterable<? extends EObject> elements, IScope outer) {
		return Scopes.<EObject>scopeFor(elements, names, outer);
	}

	private static List<EObject> ofType(Collection<? extends EObject> elements, EClass type) {
		List<EObject> result = new ArrayList<>();
		for (EObject element : elements) {
			if (type.isInstance(element)) {
				result.add(element);
			}
		}
		return result;
	}
}
