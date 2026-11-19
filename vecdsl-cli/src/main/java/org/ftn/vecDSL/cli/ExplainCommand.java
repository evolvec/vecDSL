package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.coverage.ConstructIds;
import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.Construct;
import org.ftn.vecDSL.editor.TargetStatus;
import org.ftn.vecDSL.editor.VendorStatus;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.ir.Json;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * {@code vecdsl explain}: for each statement, the constructs it uses and, per target, the coverage status, the
 * generated calls and the reference: the data of the editor's keyword hover ({@link TargetStatus} and
 * {@link VendorStatus} over the coverage index), as text or JSON. A statement uses the class row of every object of
 * a listed kind it contains, and the feature row of every clause written on such an object that a matrix lists.
 * Targets: {@code --target}, otherwise the script's {@code targets}, otherwise every vendor with coverage data.
 */
final class ExplainCommand implements Command {

	static final String STATEMENT = "--statement";
	static final String TARGET = "--target";
	static final String JSON = "--json";

	private final Toolchain toolchain;
	private final PrintStream out;

	ExplainCommand(Toolchain toolchain, PrintStream out) {
		this.toolchain = toolchain;
		this.out = out;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(JSON), Set.of(STATEMENT, TARGET));
	}

	@Override
	public String usage() {
		return "vecdsl explain <file> [--statement <label>] [--target <vendor,...>] [--json]";
	}

	@Override
	public int run(Args args) throws IOException, UsageException {
		Path program = Path.of(args.operands(1, 1, "program file").get(0));
		if (!Files.isRegularFile(program)) {
			throw new UsageException("no such file: " + program);
		}
		CoverageIndex index = toolchain.coverage();
		for (String vendor : args.list(TARGET)) {
			if (!VecDSLOutputConfigurationProvider.VENDORS.contains(vendor)) {
				throw new UsageException("unknown target '" + vendor + "'");
			}
		}
		XtextResource resource = toolchain.load(program);
		List<Issue> errors = Toolchain.errors(toolchain.validate(resource, false));
		if (!errors.isEmpty()) {
			errors.forEach(issue -> out.println(CheckCommand.line(program, issue)));
			out.println("not explained: the program has errors");
			return Vecdsl.FINDINGS;
		}
		Script script = Toolchain.script(resource);
		List<String> targets = !args.list(TARGET).isEmpty() ? args.list(TARGET)
				: !TargetStatus.targets(script).isEmpty() ? TargetStatus.targets(script) : List.copyOf(index.vendors());
		String label = args.option(STATEMENT);
		List<Method> statements = script.getMethods().stream()
				.filter(statement -> label == null || label.equals(statement.getName())).toList();
		if (statements.isEmpty()) {
			out.println("no statement labeled '" + label + "' in " + program);
			return Vecdsl.FINDINGS;
		}
		List<Statement> explained = new ArrayList<>();
		for (Method statement : statements) {
			explained.add(explain(statement, script.getMethods().indexOf(statement) + 1, index, targets));
		}
		if (args.flag(JSON)) {
			Map<String, Object> document = new LinkedHashMap<>();
			document.put("file", program.toString());
			document.put("targets", targets);
			document.put("statements", explained.stream().map(Statement::json).toList());
			out.print(Json.write(document));
		} else {
			out.println(program + " (targets: " + String.join(", ", targets) + ")");
			explained.forEach(statement -> statement.print(out));
		}
		return Vecdsl.OK;
	}

	/** One explained statement: its label (or #position), its class row, its first line and its constructs. */
	record Statement(String label, String type, Integer line, List<TargetStatus> constructs) {

		Map<String, Object> json() {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("label", label);
			result.put("statement", type);
			result.put("line", line);
			List<Object> rows = new ArrayList<>();
			for (TargetStatus construct : constructs) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("construct", construct.construct().id());
				row.put("vendors", construct.vendors().stream().map(ExplainCommand::json).toList());
				rows.add(row);
			}
			result.put("constructs", rows);
			return result;
		}

		void print(PrintStream out) {
			out.println();
			out.println("statement " + label + " (" + type + "), line " + line);
			for (TargetStatus construct : constructs) {
				out.println("  " + construct.construct().id());
				for (VendorStatus vendor : construct.vendors()) {
					out.println("    " + vendor.vendor() + ": " + vendor.label());
					CoverageEntry entry = vendor.entry();
					if (entry == null) {
						continue;
					}
					if (!entry.construct().equals(construct.construct().id())) {
						out.println("      row: " + entry.construct());
					}
					if (entry.isUnsupported() || entry.isGenerationPending()) {
						out.println("      reason: " + entry.reason());
					}
					field(out, "python", entry.python());
					field(out, "typescript", entry.typescript());
					field(out, "java", entry.java());
					field(out, "reference", entry.reference());
				}
			}
		}
	}

	private static void field(PrintStream out, String name, String value) {
		if (!value.isEmpty()) {
			out.println("      " + name + ": " + value);
		}
	}

	private static Statement explain(Method statement, int position, CoverageIndex index, List<String> targets) {
		ICompositeNode node = NodeModelUtils.getNode(statement);
		List<TargetStatus> constructs = constructs(statement, index).stream()
				.map(construct -> TargetStatus.of(index, targets, construct)).toList();
		return new Statement(statement.getName() == null ? "#" + position : statement.getName(),
				ConstructIds.of(statement.eClass()), node == null ? null : node.getStartLine(), constructs);
	}

	/** The class construct of every listed object in the statement, then the feature constructs written on it. */
	static List<Construct> constructs(Method statement, CoverageIndex index) {
		Set<Construct> constructs = new LinkedHashSet<>();
		List<EObject> objects = new ArrayList<>();
		objects.add(statement);
		statement.eAllContents().forEachRemaining(objects::add);
		for (EObject object : objects) {
			if (ConstructIds.of(object.eClass()) == null) {
				continue;
			}
			constructs.add(new Construct(object.eClass(), null));
			for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
				String id = ConstructIds.of(object.eClass(), feature);
				if (listed(index, id) && ModelUtil.written(object, feature.getName())) {
					constructs.add(new Construct(object.eClass(), feature));
				}
			}
		}
		return List.copyOf(constructs);
	}

	private static boolean listed(CoverageIndex index, String construct) {
		return index.vendors().stream().anyMatch(vendor -> index.entry(vendor, construct).isPresent());
	}

	private static Map<String, Object> json(VendorStatus status) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("vendor", status.vendor());
		row.put("label", status.label());
		CoverageEntry entry = status.entry();
		if (entry != null) {
			row.put("row", entry.construct());
			row.put("status", entry.status().literal());
			row.put("generationPending", entry.isGenerationPending());
			row.put("reason", entry.reason());
			row.put("python", entry.python());
			row.put("typescript", entry.typescript());
			row.put("java", entry.java());
			row.put("reference", entry.reference());
		}
		return row;
	}
}
