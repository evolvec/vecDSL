package org.ftn.vecDSL.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.ftn.vecDSL.naming.VecDSLQualifiedNameProvider;
import org.ftn.vecDSL.vecDSL.Alter;
import org.ftn.vecDSL.vecDSL.ElementPredicate;
import org.ftn.vecDSL.vecDSL.ElementScope;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Index;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VendorExtension;

public final class ModelUtil {

	private ModelUtil() {
	}

	public static boolean written(EObject object, String featureName) {
		EStructuralFeature feature = object.eClass().getEStructuralFeature(featureName);
		return feature != null && !NodeModelUtils.findNodesForFeature(object, feature).isEmpty();
	}

	public static VectorCollection targetCollection(EObject context) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof VectorCollection collection) {
				return collection;
			}
			if (current instanceof Alter alter) {
				return alter.getTarget() instanceof VectorCollection collection ? collection : null;
			}
			EStructuralFeature feature = current.eClass().getEStructuralFeature("collection");
			if (feature instanceof EReference reference && !reference.isMany()
					&& reference.getEReferenceType() == VecDSLPackage.eINSTANCE.getVectorCollection()
					&& current.eGet(reference) instanceof VectorCollection collection) {
				return collection;
			}
		}
		return null;
	}

	public static VectorCollection owningCollection(EObject element) {
		return EcoreUtil2.getContainerOfType(element, VectorCollection.class);
	}

	public static Field enclosingElementField(EObject context) {
		EObject child = context;
		for (EObject current = context.eContainer(); current != null; current = current.eContainer()) {
			if (current instanceof ElementScope scope && child == scope.getCondition()) {
				return scope.getField();
			}
			if (current instanceof ElementPredicate predicate && child == predicate.getCondition()) {
				return leaf(predicate.getTarget());
			}
			child = current;
		}
		return null;
	}

	public static Field leaf(FieldPath path) {
		if (path == null || path.getField() == null) {
			return null;
		}
		List<Field> segments = path.getSubFields();
		return segments.isEmpty() ? path.getField() : segments.get(segments.size() - 1);
	}

	public static List<Field> descendants(Field field) {
		List<Field> result = new ArrayList<>();
		if (field instanceof PayloadField payload) {
			for (Field child : payload.getSubFields()) {
				result.add(child);
				result.addAll(descendants(child));
			}
		}
		return result;
	}

	public static List<Partition> partitions(VectorCollection collection) {
		List<Partition> result = new ArrayList<>();
		collectPartitions(collection.getPartitions(), result);
		return result;
	}

	private static void collectPartitions(Collection<Partition> partitions, List<Partition> result) {
		for (Partition partition : partitions) {
			result.add(partition);
			collectPartitions(partition.getSubPartitions(), result);
		}
	}

	public static String fieldName(Field field) {
		String name = field.getName();
		return name != null ? name : VecDSLQualifiedNameProvider.DEFAULT_VECTOR_FIELD;
	}

	public static List<VectorField> vectorFields(VectorCollection collection) {
		return collection.getFields().stream().filter(VectorField.class::isInstance).map(VectorField.class::cast).toList();
	}

	public static List<PayloadField> payloadFields(VectorCollection collection) {
		return collection.getFields().stream().filter(PayloadField.class::isInstance).map(PayloadField.class::cast).toList();
	}

	public static VectorIndex vectorIndex(VectorCollection collection, VectorField field) {
		if (field.getIndex() != null) {
			return field.getIndex();
		}
		for (Index index : collection.getIndexes()) {
			if (index instanceof VectorIndex vectorIndex && vectorIndex.getField() == field) {
				return vectorIndex;
			}
		}
		return null;
	}

	public static Field partitionKey(VectorCollection collection) {
		for (PayloadField field : payloadFields(collection)) {
			if (field.isPartitionKey()) {
				return field;
			}
		}
		for (Partition partition : partitions(collection)) {
			if (partition.getKey() != null) {
				return partition.getKey();
			}
		}
		return null;
	}

	public static Function producingFunction(VectorCollection collection, Field output) {
		for (Function function : collection.getFunctions()) {
			if (function.getOutputs().contains(output)) {
				return function;
			}
		}
		return null;
	}

	public static Map<String, Literal> extParams(EObject host, String vendor) {
		Map<String, Literal> result = new LinkedHashMap<>();
		EStructuralFeature feature = host.eClass().getEStructuralFeature("extensions");
		if (feature == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		List<VendorExtension> extensions = (List<VendorExtension>) host.eGet(feature);
		for (VendorExtension extension : extensions) {
			if (vendor.equals(extension.getVendor())) {
				for (EObject item : extension.getItems()) {
					if (item instanceof ExtParam param) {
						result.put(param.getKey() != null ? param.getKey() : param.getQuotedKey(), param.getValue());
					}
				}
			}
		}
		return result;
	}

	public static List<String> parameterNames(EObject root) {
		Set<String> names = new LinkedHashSet<>();
		for (ParamRef ref : EcoreUtil2.eAllOfType(root, ParamRef.class)) {
			Parameter parameter = ref.getParam();
			if (parameter != null && parameter.getName() != null) {
				names.add(parameter.getName());
			}
		}
		return new ArrayList<>(names);
	}
}
