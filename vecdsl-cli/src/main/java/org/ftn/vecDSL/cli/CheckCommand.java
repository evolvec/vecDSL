package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.ir.VecDSLIr;

/**
 * {@code vecdsl check}: parse and validate programs. Prints {@code file:line:column: severity code message} for every
 * issue, the editor's coverage warnings included; {@code --json} prints the diagnostics document of
 * spec/conformance/&lt;name&gt;/diagnostics.json (errors only, as {@link VecDSLIr#diagnostics} writes it).
 */
final class CheckCommand implements Command {

	static final String JSON = "--json";
	static final String OUTPUT = "--output";
	static final String NO_COVERAGE = "--no-coverage";

	private final Toolchain toolchain;
	private final PrintStream out;

	CheckCommand(Toolchain toolchain, PrintStream out) {
		this.toolchain = toolchain;
		this.out = out;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(JSON, NO_COVERAGE), Set.of(OUTPUT)).alias("-o", OUTPUT);
	}

	@Override
	public String usage() {
		return "vecdsl check <files or folders> [--no-coverage] [--json [-o <folder>]]";
	}

	@Override
	public int run(Args args) throws IOException, UsageException {
		List<Path> programs = Toolchain.programs(args.operands(1, Integer.MAX_VALUE, "program files or folders"));
		if (programs.isEmpty()) {
			throw new UsageException("no " + Toolchain.EXTENSION + " files in " + String.join(" ", args.operands()));
		}
		boolean json = args.flag(JSON);
		Path folder = args.option(OUTPUT) == null ? null : Path.of(args.option(OUTPUT));
		if (folder != null && !json) {
			throw new UsageException(OUTPUT + " is used with " + JSON);
		}
		if (json && folder == null && programs.size() > 1) {
			throw new UsageException(JSON + " with several programs needs " + OUTPUT + " <folder>");
		}
		if (folder != null) {
			Files.createDirectories(folder);
		}
		int errors = 0;
		int warnings = 0;
		for (Path program : programs) {
			XtextResource resource = toolchain.load(program);
			List<Issue> issues = toolchain.validate(resource, !args.flag(NO_COVERAGE));
			List<Issue> programErrors = Toolchain.errors(issues);
			errors += programErrors.size();
			warnings += (int) issues.stream().filter(issue -> issue.getSeverity() == Severity.WARNING).count();
			if (!json) {
				issues.forEach(issue -> out.println(line(program, issue)));
			} else if (folder == null) {
				out.print(VecDSLIr.diagnostics(programErrors));
			} else {
				Path document = folder.resolve(
						program.getFileName().toString().replaceFirst("\\.vecDSL$", "") + ".json");
				Files.writeString(document, VecDSLIr.diagnostics(programErrors), StandardCharsets.UTF_8);
			}
		}
		if (!json || folder != null) {
			out.println(programs.size() + (programs.size() == 1 ? " program, " : " programs, ") + errors
					+ (errors == 1 ? " error, " : " errors, ") + warnings + (warnings == 1 ? " warning" : " warnings"));
		}
		return errors == 0 ? Vecdsl.OK : Vecdsl.FINDINGS;
	}

	static String line(Path program, Issue issue) {
		String code = issue.getCode() == null ? "-" : issue.getCode();
		return program + ":" + issue.getLineNumber() + ":" + issue.getColumn() + ": "
				+ issue.getSeverity().name().toLowerCase(Locale.ROOT) + " " + code + " " + issue.getMessage();
	}
}
