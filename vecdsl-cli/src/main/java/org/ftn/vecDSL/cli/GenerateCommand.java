package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.generator.trace.TraceFileNameProvider;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.generator.VecDSLGenerator;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;

import com.google.inject.Injector;

/**
 * {@code vecdsl gen}: runs the language's generator (VecDSLGenerator through GeneratorDelegate, as the Eclipse builder
 * does) into a JavaIoFileSystemAccess whose output configurations are VecDSLOutputConfigurationProvider's, each vendor
 * folder placed under the output folder: {@code <out>/<vendor>/<language>/...}. Files are written as UTF-8. Target
 * and language selection go through the system properties the generators read, {@code vecdsl.targets} and
 * {@code vecdsl.languages}; the script's {@code targets [...]} and {@code language} hint apply as in Eclipse.
 */
final class GenerateCommand implements Command {

	static final String TARGET = "--target";
	static final String LANG = "--lang";
	static final String OUTPUT = "--output";
	static final String DRY_RUN = "--dry-run";

	/** The property every vendor generator reads (for example RedisGenerator.LANGUAGES_PROPERTY). */
	static final String LANGUAGES_PROPERTY = "vecdsl.languages";

	static final Map<String, String> LANGUAGES = languages();

	private final Toolchain toolchain;
	private final PrintStream out;

	GenerateCommand(Toolchain toolchain, PrintStream out) {
		this.toolchain = toolchain;
		this.out = out;
	}

	private static Map<String, String> languages() {
		Map<String, String> languages = new LinkedHashMap<>();
		languages.put("py", "python");
		languages.put("python", "python");
		languages.put("ts", "typescript");
		languages.put("typescript", "typescript");
		languages.put("java", "java");
		return languages;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(DRY_RUN), Set.of(TARGET, LANG, OUTPUT)).alias("-o", OUTPUT);
	}

	@Override
	public String usage() {
		return "vecdsl gen <file> --target <vendor,...|all> [--lang <py,ts,java|all>] -o <folder> [--dry-run]";
	}

	@Override
	public int run(Args args) throws IOException, UsageException {
		Path program = Path.of(args.operands(1, 1, "program file").get(0));
		if (args.option(TARGET) == null) {
			throw new UsageException("missing " + TARGET + " <vendor,...|all>");
		}
		if (args.option(OUTPUT) == null) {
			throw new UsageException("missing -o <folder>");
		}
		String targets = targets(args.list(TARGET));
		String languages = languages(args.list(LANG), args.option(LANG) != null);
		Path folder = Path.of(args.option(OUTPUT));
		boolean dryRun = args.flag(DRY_RUN);

		if (!Files.isRegularFile(program)) {
			throw new UsageException("no such file: " + program);
		}
		XtextResource resource = toolchain.load(program);
		List<Issue> errors = Toolchain.errors(toolchain.validate(resource, false));
		if (!errors.isEmpty()) {
			errors.forEach(issue -> out.println(CheckCommand.line(program, issue)));
			out.println("not generated: the program has errors");
			return Vecdsl.FINDINGS;
		}
		List<Path> files = generate(resource, folder, targets, languages, dryRun);
		Path here = Path.of("").toAbsolutePath();
		files.forEach(file -> out.println((dryRun ? "would write " : "wrote ")
				+ (file.startsWith(here) ? here.relativize(file) : file)));
		if (files.isEmpty()) {
			out.println("nothing generated: no selected target is in the script's targets");
			return Vecdsl.FINDINGS;
		}
		out.println(files.size() + (files.size() == 1 ? " file" : " files") + (dryRun ? " (dry run)" : ""));
		return Vecdsl.OK;
	}

	/** The value of vecdsl.targets: the vendors asked for, or null for every vendor ("all"). */
	static String targets(List<String> requested) throws UsageException {
		if (requested.equals(List.of("all"))) {
			return null;
		}
		if (requested.isEmpty()) {
			throw new UsageException(TARGET + " needs vendor ids or 'all'");
		}
		Set<String> vendors = new LinkedHashSet<>();
		for (String vendor : requested) {
			if (!VecDSLOutputConfigurationProvider.VENDORS.contains(vendor)) {
				throw new UsageException("unknown target '" + vendor + "'; targets: all, "
						+ String.join(", ", VecDSLOutputConfigurationProvider.VENDORS));
			}
			vendors.add(vendor);
		}
		return String.join(",", vendors);
	}

	/**
	 * The value of vecdsl.languages: null without {@code --lang} (the script's language hint, otherwise every
	 * language, as in Eclipse), every language for "all", otherwise the languages asked for.
	 */
	static String languages(List<String> requested, boolean given) throws UsageException {
		if (!given) {
			return null;
		}
		if (requested.equals(List.of("all"))) {
			return String.join(",", new LinkedHashSet<>(LANGUAGES.values()));
		}
		if (requested.isEmpty()) {
			throw new UsageException(LANG + " needs languages or 'all'");
		}
		Set<String> languages = new LinkedHashSet<>();
		for (String language : requested) {
			String name = LANGUAGES.get(language);
			if (name == null) {
				throw new UsageException("unknown language '" + language + "'; languages: all, py, ts, java");
			}
			languages.add(name);
		}
		return String.join(",", languages);
	}

	private List<Path> generate(XtextResource resource, Path folder, String targets, String languages,
			boolean dryRun) {
		Injector injector = toolchain.injector();
		IEncodingProvider.Runtime utf8 = new IEncodingProvider.Runtime();
		utf8.setDefaultEncoding(StandardCharsets.UTF_8.name());
		RecordingFileSystemAccess fsa = new RecordingFileSystemAccess(IResourceServiceProvider.Registry.INSTANCE, utf8,
				injector.getInstance(TraceFileNameProvider.class), injector.getInstance(TraceRegionSerializer.class),
				dryRun);
		Map<String, OutputConfiguration> configurations = injector.getInstance(IOutputConfigurationProvider.class)
				.getOutputConfigurations().stream()
				.collect(Collectors.toMap(OutputConfiguration::getName, configuration -> configuration));
		fsa.setOutputConfigurations(configurations);
		for (String name : configurations.keySet()) {
			fsa.setOutputPath(name, folder.resolve(name.equals(IFileSystemAccess.DEFAULT_OUTPUT) ? "src-gen" : name)
					.toString());
		}
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		String previousTargets = System.getProperty(VecDSLGenerator.TARGETS_PROPERTY);
		String previousLanguages = System.getProperty(LANGUAGES_PROPERTY);
		try {
			set(VecDSLGenerator.TARGETS_PROPERTY, targets);
			set(LANGUAGES_PROPERTY, languages);
			injector.getInstance(GeneratorDelegate.class).generate(resource, fsa, context);
		} finally {
			set(VecDSLGenerator.TARGETS_PROPERTY, previousTargets);
			set(LANGUAGES_PROPERTY, previousLanguages);
		}
		return fsa.files;
	}

	private static void set(String property, String value) {
		if (value == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, value);
		}
	}

	/** A JavaIoFileSystemAccess that records the files the generators write and, in a dry run, writes none. */
	static final class RecordingFileSystemAccess extends JavaIoFileSystemAccess {

		private final boolean dryRun;
		private final List<Path> files = new ArrayList<>();

		RecordingFileSystemAccess(IResourceServiceProvider.Registry registry, IEncodingProvider encoding,
				TraceFileNameProvider traceFileNames, TraceRegionSerializer traces, boolean dryRun) {
			super(registry, encoding, traceFileNames, traces);
			this.dryRun = dryRun;
		}

		@Override
		public void generateFile(String fileName, String outputConfigName, CharSequence contents) {
			files.add(getFile(fileName, outputConfigName).toPath());
			if (!dryRun) {
				super.generateFile(fileName, outputConfigName, contents);
			}
		}

		@Override
		public void generateFile(String fileName, String outputConfigName, InputStream content) {
			files.add(getFile(fileName, outputConfigName).toPath());
			if (!dryRun) {
				super.generateFile(fileName, outputConfigName, content);
			}
		}
	}
}
