package org.ftn.vecDSL.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.ftn.vecDSL.naming.VecDSLQualifiedNameProvider;
import org.ftn.vecDSL.vecDSL.Method;

/**
 * Reference names of the IR (rule N12). The name of an element is its qualified path: the names of its named
 * ancestors and its own, joined by dots (`books.title`, `books.meta.author`); statements and the script contribute no
 * segment. Element names are those of {@link VecDSLQualifiedNameProvider} (an unnamed vector field is `vector`). When
 * several elements that a reference could target share a path, the reference adds the 1-based position of its target
 * among them in document order (`books#2`).
 */
final class IrNames {

	private final EObject root;
	private final VecDSLQualifiedNameProvider names = new VecDSLQualifiedNameProvider();
	private final Map<EObject, String> paths = new HashMap<>();
	private final Map<String, List<EObject>> byPath = new LinkedHashMap<>();

	IrNames(EObject root) {
		this.root = root;
		for (TreeIterator<EObject> it = root.eAllContents(); it.hasNext();) {
			EObject element = it.next();
			String path = path(element);
			if (path != null) {
				byPath.computeIfAbsent(path, key -> new ArrayList<>()).add(element);
			}
		}
	}

	String reference(EObject target, EClass type) {
		String path = path(target);
		if (path == null) {
			throw new IllegalStateException("reference to an element without a name: " + target.eClass().getName());
		}
		List<EObject> candidates = candidates(path, type);
		return candidates.size() == 1 ? path : path + "#" + (candidates.indexOf(target) + 1);
	}

	EObject resolve(String reference, EClass type) {
		int hash = reference.lastIndexOf('#');
		String path = hash < 0 ? reference : reference.substring(0, hash);
		List<EObject> candidates = candidates(path, type);
		int index = hash < 0 ? 0 : Integer.parseInt(reference.substring(hash + 1)) - 1;
		if (candidates.isEmpty() || index < 0 || index >= candidates.size() || (hash < 0 && candidates.size() > 1)) {
			throw new IllegalArgumentException("IR reference '" + reference + "' does not name exactly one "
					+ type.getName() + " (" + candidates.size() + " candidates)");
		}
		return candidates.get(index);
	}

	private List<EObject> candidates(String path, EClass type) {
		return byPath.getOrDefault(path, List.of()).stream().filter(type::isInstance).toList();
	}

	String path(EObject element) {
		if (element == root || element instanceof Method) {
			return null;
		}
		if (paths.containsKey(element)) {
			return paths.get(element);
		}
		QualifiedName own = names.getFullyQualifiedName(element);
		String result = null;
		if (own != null) {
			String prefix = null;
			for (EObject container = element.eContainer(); container != null && prefix == null; container = container
					.eContainer()) {
				prefix = path(container);
			}
			result = prefix == null ? own.toString() : prefix + "." + own;
		}
		paths.put(element, result);
		return result;
	}
}
