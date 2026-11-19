package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Runs the negative corpus (scenarios/negative/*.vecDSL): programs that are syntactically
 * valid but violate one intended well-formedness judgment, named in the first line ("// Judgment: WF-x.").
 * Each program must parse; whether the validator or the linker rejects it is recorded, not asserted, in
 * negative-results.csv next to the programs. An accepted program marks a judgment the implementation does
 * not enforce yet. Override the directory with -Dvecdsl.negative=PATH.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class NegativeProgramsTest {

	private static final Path DIRECTORY = Path.of(System.getProperty("vecdsl.negative", "scenarios/negative"));
	private static final Pattern JUDGMENT = Pattern.compile("^// Judgment: ([\\w-]+)\\.");
	private static final String LINKING = "org.eclipse.xtext.diagnostics.Diagnostic.Linking";
	private static final List<String> RESULTS = Collections.synchronizedList(new ArrayList<>());

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@TestFactory
	public Stream<DynamicTest> negativePrograms() throws IOException {
		List<Path> files;
		try (Stream<Path> listing = Files.list(DIRECTORY)) {
			files = listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
		}
		assertFalse(files.isEmpty(), () -> "no negative programs in " + DIRECTORY.toAbsolutePath());
		return files.stream().map(file -> dynamicTest(file.getFileName().toString(), () -> check(file)));
	}

	private void check(Path file) throws Exception {
		String text = Files.readString(file, StandardCharsets.UTF_8);
		Matcher header = JUDGMENT.matcher(text);
		assertTrue(header.find(), () -> file.getFileName() + ": first line must be '// Judgment: WF-x. ...'");
		Script script = parseHelper.parse(text);
		List<String> syntax = script == null ? List.of("no model")
				: script.eResource().getErrors().stream().filter(XtextSyntaxDiagnostic.class::isInstance)
						.map(d -> "line " + d.getLine() + ": " + d.getMessage()).toList();
		List<Issue> errors = syntax.isEmpty() ? validation.validate(script).stream()
				.filter(i -> i.getSeverity() == Severity.ERROR && !i.isSyntaxError()).toList() : List.of();
		String rejectedBy = errors.isEmpty() ? "" : errors.stream().anyMatch(i -> !LINKING.equals(i.getCode()))
				? (errors.stream().anyMatch(i -> LINKING.equals(i.getCode())) ? "validator+linker" : "validator") : "linker";
		String codes = errors.stream().map(i -> LINKING.equals(i.getCode()) ? "linking" : String.valueOf(i.getCode()))
				.distinct().sorted().collect(Collectors.joining(" "));
		String messages = errors.stream().map(i -> "line " + i.getLineNumber() + ": " + i.getMessage())
				.collect(Collectors.joining(" | "));
		RESULTS.add(String.join(",", csv(file.getFileName().toString()), csv(header.group(1)),
				syntax.isEmpty() ? "yes" : "no", syntax.isEmpty() ? (errors.isEmpty() ? "no" : "yes") : "not run",
				csv(rejectedBy), csv(codes), csv(syntax.isEmpty() ? messages : String.join(" | ", syntax))));
		assertTrue(syntax.isEmpty(), () -> "a negative program must parse: " + syntax);
	}

	@AfterAll
	public static void writeResults() throws IOException {
		List<String> lines = new ArrayList<>();
		lines.add("program,judgment,parses,rejected,rejected_by,codes,messages");
		RESULTS.stream().sorted().forEach(lines::add);
		Files.write(DIRECTORY.resolve("negative-results.csv"), lines, StandardCharsets.UTF_8);
	}

	private static String csv(String value) {
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}
}
