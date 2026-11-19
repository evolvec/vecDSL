package org.ftn.vecDSL.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.RecordEntry;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StringLit;

/**
 * Moves the string literal of a geo_point record value into a text parameter declared at the top of the script, as
 * the samples do ("param northPlace: text = \"45.25,19.85\""): the validator rejects a string for geo_point
 * (t.record.type) but admits a parameter, whose value the generated program receives.
 */
public final class ParameterExtraction {

	private ParameterExtraction() {
	}

	/** Whether the entry is a string literal written for a geo_point field. */
	public static boolean applies(RecordEntry entry) {
		return entry.getField() instanceof PayloadField field && !field.eIsProxy()
				&& field.getType() == EScalarDType.GEO_POINT && entry.getValue() instanceof StringLit;
	}

	/** The declaration and the reference that replace the literal; empty when the entry does not apply. */
	public static List<TextEdit> edits(RecordEntry entry) {
		Script script = EcoreUtil2.getContainerOfType(entry, Script.class);
		if (!applies(entry) || script == null) {
			return List.of();
		}
		String name = name(entry, script);
		ICompositeNode value = NodeModelUtils.getNode(entry.getValue());
		String literal = NodeModelUtils.getTokenText(value);
		int insertion = declarationOffset(script);
		String declaration = "param " + name + ": text = " + literal + "\n";
		String text = NodeModelUtils.getNode(script).getRootNode().getText();
		if (insertion > 0 && text.charAt(insertion - 1) != '\n') {
			declaration = "\n" + declaration.substring(0, declaration.length() - 1);
		}
		return List.of(new TextEdit(insertion, 0, declaration), new TextEdit(value.getOffset(), value.getLength(), "$" + name));
	}

	/** After the last parameter, or else after the last header line before the first statement. */
	private static int declarationOffset(Script script) {
		List<Parameter> parameters = script.getParameters();
		if (!parameters.isEmpty()) {
			return lineEnd(NodeModelUtils.getNode(parameters.get(parameters.size() - 1)).getEndOffset(), script);
		}
		ICompositeNode root = NodeModelUtils.getNode(script);
		int end = root.getOffset();
		Method first = script.getMethods().isEmpty() ? null : script.getMethods().get(0);
		int limit = first == null ? root.getEndOffset() : NodeModelUtils.getNode(first).getOffset();
		for (ILeafNode leaf : root.getLeafNodes()) {
			if (!leaf.isHidden() && leaf.getEndOffset() <= limit) {
				end = leaf.getEndOffset();
			}
		}
		return lineEnd(end, script);
	}

	private static int lineEnd(int offset, Script script) {
		String text = NodeModelUtils.getNode(script).getRootNode().getText();
		int newline = text.indexOf('\n', offset);
		return newline < 0 ? text.length() : newline + 1;
	}

	/** The statement label, or "value", followed by the field name with a capital, made unique among the names. */
	private static String name(RecordEntry entry, Script script) {
		Method statement = EcoreUtil2.getContainerOfType(entry, Method.class);
		String prefix = statement != null && statement.getName() != null ? statement.getName() : "value";
		String field = ModelUtil.fieldName(entry.getField());
		String base = prefix + Character.toUpperCase(field.charAt(0)) + field.substring(1);
		Set<String> taken = new HashSet<>();
		for (EObject object : EcoreUtil2.eAllContents(script)) {
			Object name = object.eClass().getEStructuralFeature("name") == null ? null
					: object.eGet(object.eClass().getEStructuralFeature("name"));
			if (name instanceof String string) {
				taken.add(string);
			}
		}
		String name = base;
		for (int n = 2; taken.contains(name); n++) {
			name = base + n;
		}
		return name;
	}
}
