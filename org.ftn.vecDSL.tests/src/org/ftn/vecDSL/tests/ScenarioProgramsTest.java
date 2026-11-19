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
 * Parses and validates the scenario programs (scenarios/*.vecDSL) and writes
 * scenario-results.csv next to them. Override the directory with -Dvecdsl.scenarios=PATH.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class ScenarioProgramsTest {

	private static final Path DIRECTORY = Path.of(System.getProperty("vecdsl.scenarios", "scenarios"));
	private static final List<String> RESULTS = Collections.synchronizedList(new ArrayList<>());

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	@TestFactory
	public Stream<DynamicTest> scenarios() throws IOException {
		List<Path> files;
		try (Stream<Path> listing = Files.list(DIRECTORY)) {
			files = listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
		}
		assertFalse(files.isEmpty(), () -> "no scenario programs in " + DIRECTORY.toAbsolutePath());
		return files.stream().map(file -> dynamicTest(file.getFileName().toString(), () -> check(file)));
	}

	private void check(Path file) throws Exception {
		Script script = parseHelper.parse(Files.readString(file, StandardCharsets.UTF_8));
		List<String> syntax = script == null ? List.of("no model")
				: script.eResource().getErrors().stream().filter(XtextSyntaxDiagnostic.class::isInstance)
						.map(d -> "line " + d.getLine() + ": " + d.getMessage()).toList();
		List<Issue> issues = syntax.isEmpty() ? validation.validate(script) : List.of();
		List<Issue> errors = issues.stream().filter(i -> i.getSeverity() == Severity.ERROR && !i.isSyntaxError()).toList();
		String diagnostics = issues.stream().filter(i -> !i.isSyntaxError())
				.map(i -> i.getSeverity() + " " + i.getCode() + " line " + i.getLineNumber() + ": " + i.getMessage())
				.collect(Collectors.joining(" | "));
		RESULTS.add(String.join(",", csv(file.getFileName().toString()), syntax.isEmpty() ? "yes" : "no",
				csv(String.join(" | ", syntax)), syntax.isEmpty() ? (errors.isEmpty() ? "yes" : "no") : "not run",
				csv(diagnostics)));
		assertTrue(syntax.isEmpty(), () -> "syntax errors: " + syntax);
		assertTrue(errors.isEmpty(), () -> "validation errors: " + diagnostics);
	}

	@AfterAll
	public static void writeResults() throws IOException {
		List<String> lines = new ArrayList<>();
		lines.add("scenario,parses,syntax_errors,validates,diagnostics");
		RESULTS.stream().sorted().forEach(lines::add);
		Files.write(DIRECTORY.resolve("scenario-results.csv"), lines, StandardCharsets.UTF_8);
	}

	private static String csv(String value) {
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}
}
