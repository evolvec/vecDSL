package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.HoverDocument.Block;
import org.ftn.vecDSL.editor.HoverDocument.Bold;
import org.ftn.vecDSL.editor.HoverDocument.Break;
import org.ftn.vecDSL.editor.HoverDocument.Code;
import org.ftn.vecDSL.editor.HoverDocument.Italic;
import org.ftn.vecDSL.editor.HoverDocument.Items;
import org.ftn.vecDSL.editor.HoverDocument.Link;
import org.ftn.vecDSL.editor.HoverDocument.Paragraph;
import org.ftn.vecDSL.editor.HoverDocument.Span;
import org.ftn.vecDSL.editor.HoverDocument.Table;
import org.ftn.vecDSL.editor.HoverDocument.Text;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * What the hovers of vecDSL say, as a {@link HoverDocument}: the one source of the hover texts of the Eclipse editor
 * (HTML) and of the language server (Markdown).
 */
public final class HoverContent {

	private HoverContent() {
	}

	/** Whether {@link #of(EObject, Keyword, CoverageIndex)} describes the object. */
	public static boolean describes(EObject object, Keyword keyword) {
		return keyword != null || object instanceof Field || object instanceof VectorCollection
				|| object instanceof Parameter || object instanceof EnvRef;
	}

	/**
	 * The hover of an object: of the keyword's construct when a keyword is hovered, otherwise of a field, collection,
	 * parameter or environment reference; null for any other object.
	 */
	public static HoverDocument of(EObject object, Keyword keyword, CoverageIndex index) {
		if (keyword != null) {
			Script script = EcoreUtil2.getContainerOfType(object, Script.class);
			TargetStatus status = TargetStatus.of(index, TargetStatus.targets(script),
					ConstructLocator.locate(keyword, object));
			return keyword(keyword.getValue(), GrammarUtil.containingRule(keyword).getName(), GrammarDocs.ofLanguage(),
					status);
		}
		return switch (object) {
			case Field field -> field(field);
			case VectorCollection collection -> collection(collection);
			case Parameter parameter -> parameter(parameter);
			case EnvRef reference -> environment(reference);
			default -> null;
		};
	}

	/**
	 * A keyword: the rule's concept, the clause comment, the design notes they cite and, per targeted vendor, the
	 * coverage status, the generated calls and the reference.
	 */
	public static HoverDocument keyword(String keyword, String rule, GrammarDocs docs, TargetStatus status) {
		List<Block> blocks = new ArrayList<>();
		List<Span> title = new ArrayList<>(List.of(new Bold(keyword)));
		if (status != null && status.construct() != null) {
			title.add(new Text(", construct "));
			title.add(new Code(status.construct().id()));
		}
		blocks.add(new Paragraph(title));
		String concept = docs.rule(rule);
		String clause = docs.clause(rule, keyword);
		if (!concept.isEmpty()) {
			blocks.add(new Paragraph(List.of(new Text(concept))));
		}
		if (!clause.isEmpty()) {
			blocks.add(new Paragraph(List.of(new Text(clause))));
		}
		Set<Integer> notes = new LinkedHashSet<>(GrammarDocs.notes(concept));
		notes.addAll(GrammarDocs.notes(clause));
		for (int note : notes) {
			String text = DesignNotes.note(note);
			if (text != null) {
				blocks.add(new Paragraph(List.of(new Italic("D" + note + ":"), new Text(" " + text))));
			}
		}
		if (status != null && status.construct() != null && !status.vendors().isEmpty()) {
			blocks.add(coverage(status.vendors()));
		}
		return new HoverDocument(blocks);
	}

	private static Table coverage(List<VendorStatus> vendors) {
		List<List<List<Span>>> rows = new ArrayList<>();
		for (VendorStatus vendor : vendors) {
			CoverageEntry entry = vendor.entry();
			List<Span> status = new ArrayList<>(List.of(new Text(vendor.label())));
			if (entry != null && !entry.reason().isEmpty() && (vendor.isUnsupported() || entry.isGenerationPending())) {
				status.add(new Break());
				status.add(new Text(entry.reason()));
			}
			List<List<Span>> row = new ArrayList<>(List.of(List.of(new Text(vendor.vendor())), status));
			if (entry == null) {
				row.addAll(List.of(List.of(), List.of(), List.of(), List.of()));
			} else {
				row.addAll(List.of(call(entry.python()), call(entry.typescript()), call(entry.java()),
						links(entry.reference())));
			}
			rows.add(row);
		}
		return new Table(List.of("vendor", "status", "Python", "TypeScript", "Java", "reference"), rows);
	}

	private static List<Span> call(String call) {
		return List.of(new Code(call.replace("`", "")));
	}

	/** References separated by "; ", each linked when it starts with a URL. */
	private static List<Span> links(String references) {
		List<Span> spans = new ArrayList<>();
		for (String reference : references.split("; ")) {
			if (!spans.isEmpty()) {
				spans.add(new Break());
			}
			int end = reference.indexOf(' ');
			String url = end < 0 ? reference : reference.substring(0, end);
			spans.add(url.startsWith("http") ? new Link(url, reference) : new Text(reference));
		}
		return spans;
	}

	/** A field: kind, type, dimension and metric of a vector field, owning collection. */
	public static HoverDocument field(Field field) {
		List<Span> spans = new ArrayList<>(List.of(new Bold(ModelUtil.fieldName(field))));
		StringBuilder kind = new StringBuilder(": ");
		if (field instanceof VectorField vector) {
			kind.append("vector field of type ").append(vector.getType().getLiteral());
			if (vector.getDimension() > 0) {
				kind.append(", dim ").append(vector.getDimension());
			}
			EMetric metric = metric(vector);
			kind.append(metric == null ? ", no declared metric" : ", metric " + metric.getLiteral());
		} else if (field instanceof PayloadField payload) {
			kind.append("payload field of type ").append(payload.getType().getLiteral());
			if (payload.getElementType() != null && ModelUtil.written(payload, "elementType")) {
				kind.append(" of ").append(payload.getElementType().getLiteral());
			}
			if (payload.getMaxLength() > 0) {
				kind.append(", max-length ").append(payload.getMaxLength());
			}
			if (payload.isFullText()) {
				kind.append(", full-text");
			}
		}
		spans.add(new Text(kind.toString()));
		VectorCollection owner = ModelUtil.owningCollection(field);
		if (owner != null) {
			spans.addAll(List.of(new Break(), new Text("in collection "), new Bold(owner.getName())));
			if (owner.getPrimaryKey() == field) {
				spans.add(new Text(" (primary key)"));
			}
		}
		return new HoverDocument(List.of(new Paragraph(spans)));
	}

	private static EMetric metric(VectorField field) {
		if (ModelUtil.written(field, "metric")) {
			return field.getMetric();
		}
		VectorCollection owner = ModelUtil.owningCollection(field);
		VectorIndex index = owner == null ? field.getIndex() : ModelUtil.vectorIndex(owner, field);
		return index != null && ModelUtil.written(index, "metric") ? index.getMetric() : null;
	}

	/** A collection: its fields as declared. */
	public static HoverDocument collection(VectorCollection collection) {
		List<List<Span>> fields = collection.getFields().stream()
				.<List<Span>>map(field -> List.of(new Code(declaration(field)))).toList();
		return new HoverDocument(
				List.of(new Paragraph(List.of(new Bold("collection " + collection.getName()))), new Items(fields)));
	}

	/** A parameter: its declaration as written. */
	public static HoverDocument parameter(Parameter parameter) {
		return new HoverDocument(
				List.of(new Paragraph(List.of(new Bold("parameter"), new Text(" "), new Code(declaration(parameter))))));
	}

	/** An environment reference: the variable it reads at run time. */
	public static HoverDocument environment(EnvRef reference) {
		return new HoverDocument(List.of(new Paragraph(List.of(new Bold("environment variable"), new Text(" "),
				new Code(reference.getVariable()), new Text(
						", read by the generated program when it runs; its value never appears in the program text.")))));
	}

	private static String declaration(EObject object) {
		String text = NodeModelUtils.getTokenText(NodeModelUtils.getNode(object));
		int block = text.indexOf(" {");
		return block > 0 ? text.substring(0, block) : text;
	}
}
