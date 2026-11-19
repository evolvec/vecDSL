package org.ftn.vecDSL.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.ftn.vecDSL.generator.VecDSLGenerator;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/**
 * The code {@code vecdsl gen} writes for a program, and the lines of one statement's code in it, found without knowing
 * how a generator names things: the program is generated twice, the second time with only the statement's label
 * changed, and every line that differs belongs to the statement. Each such line is widened to the member it starts by
 * the layout the generators share in all three languages: the lines indented deeper than it, a closing line at its own
 * indentation (and on from there when that line ends by opening a block again, with a colon or a bracket), and the
 * comment, annotation or decorator lines directly above it. Lines are those of {@link String#lines()}.
 * <p>
 * The generators read their selection from the system properties {@code vecdsl.targets} and {@code vecdsl.languages},
 * so every generation of this class in one JVM runs under one lock and restores the properties afterwards. Used by
 * the language server's {@code vecdsl/generate} and by the Eclipse hover over a statement's label.
 */
public final class StatementCode {

	/** The property every vendor generator reads for the host languages (vecdsl-cli GenerateCommand). */
	public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

	/** The --lang values of vecdsl gen and the generator language each stands for. */
	public static final Map<String, String> LANGUAGES = Map.of("py", "python", "python", "python", "ts", "typescript",
			"typescript", "typescript", "java", "java");

	/** Above this many cells of the line comparison table a file gets no section rather than a large table. */
	static final long MAX_CELLS = 16_000_000L;

	private static final String CLOSERS = "})]";
	private static final String OPENERS = ":{([";
	private static final List<String> LEADERS = List.of("@", "/*", "*", "//", "#");

	private static final Object LOCK = new Object();

	/** Lines start (inclusive) to end (exclusive), 0-based, of a generated file that hold the statement's code. */
	public record Section(String path, int start, int end) {
	}

	/** The files as gen writes them (path under its output folder to text, in write order), the statement's lines. */
	public record Code(Map<String, String> files, List<Section> sections) {
	}

	private StatementCode() {
	}

	/** The value of vecdsl.targets for a target of gen: the vendor, or null for "all". */
	public static String targets(String target) {
		if ("all".equals(target)) {
			return null;
		}
		if (target == null || !VecDSLOutputConfigurationProvider.VENDORS.contains(target)) {
			throw new IllegalArgumentException("target must be one of all, "
					+ String.join(", ", VecDSLOutputConfigurationProvider.VENDORS) + ", not '" + target + "'");
		}
		return target;
	}

	/** The value of vecdsl.languages for a --lang of gen: null for the script's hint, every language for "all". */
	public static String languages(String language) {
		if (language == null) {
			return null;
		}
		if (language.equals("all")) {
			return "python,typescript,java";
		}
		String name = LANGUAGES.get(language);
		if (name == null) {
			throw new IllegalArgumentException("language must be one of all, py, ts, java, not '" + language + "'");
		}
		return name;
	}

	/** The statement (Method) whose text contains the offset, the innermost when statements nest; null for none. */
	public static Method statement(XtextResource resource, int offset) {
		EObjectAtOffsetHelper helper = resource.getResourceServiceProvider().get(EObjectAtOffsetHelper.class);
		for (EObject object = helper.resolveContainedElementAt(resource, offset); object != null; object = object
				.eContainer()) {
			if (object instanceof Method method) {
				return method;
			}
		}
		return null;
	}

	/**
	 * The files the generators write for a resource without errors under the selection (values of vecdsl.targets and
	 * vecdsl.languages as {@link #targets} and {@link #languages} give them) and, for a statement of the resource, its
	 * lines in them (none for a null statement or when the relabeled text does not parse).
	 */
	public static Code generate(XtextResource resource, Method statement, String targets, String languages,
			CancelIndicator cancel) {
		IResourceServiceProvider services = resource.getResourceServiceProvider();
		synchronized (LOCK) {
			String previousTargets = System.getProperty(VecDSLGenerator.TARGETS_PROPERTY);
			String previousLanguages = System.getProperty(LANGUAGES_PROPERTY);
			try {
				set(VecDSLGenerator.TARGETS_PROPERTY, targets);
				set(LANGUAGES_PROPERTY, languages);
				Map<String, String> files = files(services, resource, cancel);
				XtextResource copy = statement == null ? null : relabel(services, resource, statement);
				Map<String, String> relabeled = copy == null ? Map.of() : files(services, copy, cancel);
				return new Code(files, sections(files, relabeled));
			} finally {
				set(VecDSLGenerator.TARGETS_PROPERTY, previousTargets);
				set(LANGUAGES_PROPERTY, previousLanguages);
			}
		}
	}

	private static Map<String, String> files(IResourceServiceProvider services, XtextResource resource,
			CancelIndicator cancel) {
		Files files = new Files();
		files.setOutputConfigurations(services.get(IOutputConfigurationProvider.class).getOutputConfigurations()
				.stream().collect(Collectors.toMap(OutputConfiguration::getName, configuration -> configuration)));
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(cancel);
		services.get(GeneratorDelegate.class).generate(resource, files, context);
		Map<String, String> result = new LinkedHashMap<>();
		files.getAllFiles().forEach((path, contents) -> result.put(path,
				contents instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8) : contents.toString()));
		return result;
	}

	/**
	 * The resource's text parsed again, in a resource set of its own, with the statement's label replaced by a name that
	 * occurs nowhere in the text (an unlabeled statement gets that label); null when that text does not parse. Only the
	 * code the generators derive from the label differs between the two outputs.
	 */
	static XtextResource relabel(IResourceServiceProvider services, XtextResource resource, Method statement) {
		ICompositeNode node = NodeModelUtils.getNode(statement);
		if (node == null) {
			return null;
		}
		String text = node.getRootNode().getText();
		List<INode> labels = NodeModelUtils.findNodesForFeature(statement, VecDSLPackage.eINSTANCE.getMethod_Name());
		String sentinel = sentinel(text, statement.getName() == null ? 0 : statement.getName().length());
		String relabeled = labels.isEmpty()
				? text.substring(0, node.getOffset()) + sentinel + ": " + text.substring(node.getOffset())
				: text.substring(0, labels.get(0).getOffset()) + sentinel
						+ text.substring(labels.get(0).getEndOffset());
		XtextResource copy = (XtextResource) services.get(IResourceFactory.class).createResource(resource.getURI());
		services.get(XtextResourceSet.class).getResources().add(copy);
		try {
			copy.load(new ByteArrayInputStream(relabeled.getBytes(StandardCharsets.UTF_8)),
					Map.of(XtextResource.OPTION_ENCODING, StandardCharsets.UTF_8.name()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return copy.getErrors().isEmpty() ? copy : null;
	}

	/** One letter repeated as often as the label is long (at least three times), the first that is not in the text. */
	static String sentinel(String text, int length) {
		for (int count = Math.max(3, length);; count++) {
			for (char letter = 'z'; letter >= 'a'; letter--) {
				String candidate = String.valueOf(letter).repeat(count);
				if (!text.contains(candidate)) {
					return candidate;
				}
			}
		}
	}

	static List<Section> sections(Map<String, String> files, Map<String, String> relabeled) {
		List<Section> result = new ArrayList<>();
		files.forEach((path, text) -> {
			String other = relabeled.get(path);
			if (other != null) {
				List<String> lines = text.lines().toList();
				for (int[] range : ranges(lines, changed(lines, other.lines().toList()))) {
					result.add(new Section(path, range[0], range[1]));
				}
			}
		});
		return result;
	}

	/** The indexes of the lines of a that are not in a longest common subsequence of a and b, ascending. */
	static List<Integer> changed(List<String> a, List<String> b) {
		List<Integer> result = new ArrayList<>();
		if (a.size() == b.size()) {
			for (int i = 0; i < a.size(); i++) {
				if (!a.get(i).equals(b.get(i))) {
					result.add(i);
				}
			}
			return result;
		}
		int prefix = 0;
		while (prefix < a.size() && prefix < b.size() && a.get(prefix).equals(b.get(prefix))) {
			prefix++;
		}
		int suffix = 0;
		while (suffix < a.size() - prefix && suffix < b.size() - prefix
				&& a.get(a.size() - 1 - suffix).equals(b.get(b.size() - 1 - suffix))) {
			suffix++;
		}
		int n = a.size() - prefix - suffix;
		int m = b.size() - prefix - suffix;
		if ((long) (n + 1) * (m + 1) > MAX_CELLS) {
			return result;
		}
		int[][] common = new int[n + 1][m + 1];
		for (int i = n - 1; i >= 0; i--) {
			for (int j = m - 1; j >= 0; j--) {
				common[i][j] = a.get(prefix + i).equals(b.get(prefix + j)) ? common[i + 1][j + 1] + 1
						: Math.max(common[i + 1][j], common[i][j + 1]);
			}
		}
		int i = 0;
		int j = 0;
		while (i < n && j < m) {
			if (a.get(prefix + i).equals(b.get(prefix + j))) {
				i++;
				j++;
			} else if (common[i + 1][j] >= common[i][j + 1]) {
				result.add(prefix + i++);
			} else {
				j++;
			}
		}
		while (i < n) {
			result.add(prefix + i++);
		}
		return result;
	}

	/** The members of the changed lines as [start, end) line ranges, overlapping or touching ones merged. */
	static List<int[]> ranges(List<String> lines, List<Integer> changed) {
		List<int[]> result = new ArrayList<>();
		for (int line : changed) {
			int[] last = result.isEmpty() ? null : result.get(result.size() - 1);
			if (last != null && line < last[1]) {
				continue;
			}
			int[] member = member(lines, line);
			if (last != null && member[0] <= last[1]) {
				last[1] = Math.max(last[1], member[1]);
			} else {
				result.add(member);
			}
		}
		return result;
	}

	static int[] member(List<String> lines, int line) {
		int indent = indent(lines.get(line));
		int start = line;
		while (start > 0 && !blank(lines.get(start - 1)) && indent(lines.get(start - 1)) >= indent
				&& leads(lines.get(start - 1))) {
			start--;
		}
		int end = line + 1;
		while (true) {
			int next = end;
			while (next < lines.size() && (blank(lines.get(next)) || indent(lines.get(next)) > indent)) {
				next++;
			}
			int last = next;
			while (last > end && blank(lines.get(last - 1))) {
				last--;
			}
			if (last < next || next == lines.size() || indent(lines.get(next)) != indent
					|| CLOSERS.indexOf(lines.get(next).strip().charAt(0)) < 0) {
				return new int[] { start, last };
			}
			String closing = lines.get(next).strip();
			end = next + 1;
			if (OPENERS.indexOf(closing.charAt(closing.length() - 1)) < 0) {
				return new int[] { start, end };
			}
		}
	}

	private static int indent(String line) {
		int count = 0;
		while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) {
			count++;
		}
		return count;
	}

	private static boolean blank(String line) {
		return line.isBlank();
	}

	private static boolean leads(String line) {
		String stripped = line.strip();
		return LEADERS.stream().anyMatch(stripped::startsWith);
	}

	private static void set(String property, String value) {
		if (value == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, value);
		}
	}

	/** Records the files by their path under gen's output folder: the output configuration's folder, then the name. */
	static final class Files extends InMemoryFileSystemAccess {

		@Override
		protected String getFileName(String fileName, String outputConfigName) {
			String folder = outputConfigName.equals(IFileSystemAccess.DEFAULT_OUTPUT) ? "src-gen" : outputConfigName;
			return folder + "/" + (fileName.startsWith("/") ? fileName.substring(1) : fileName);
		}
	}
}
