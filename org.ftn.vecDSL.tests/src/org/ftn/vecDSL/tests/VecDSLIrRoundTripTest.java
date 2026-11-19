package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.ir.IrReader;
import org.ftn.vecDSL.ir.Json;
import org.ftn.vecDSL.ir.VecDSLIr;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The IR round trip on every program in samples/, read live: the IR of the parsed model,
 * the model rebuilt from it (equal to the parsed model), the IR of the rebuilt model (byte-identical), and the rebuilt
 * model serialized and parsed again (equal model). The rejected programs of spec/negative must give a diagnostics document
 * that contains the code named on their first line ("// Expect: code").
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLIrRoundTripTest {

	private static final Path SAMPLES = Path.of("samples");
	private static final Path REJECTED = Path.of(System.getProperty("vecdsl.ir.rejected", "../spec/negative"));
	private static final Pattern EXPECT = Pattern.compile("^// Expect: (\\S+)");

	@Inject
	private VecDSLIr ir;

	@Inject
	private Provider<XtextResourceSet> resourceSets;

	@Inject
	private ParseHelper<Script> parseHelper;

	@TestFactory
	public Stream<DynamicTest> everySampleRoundTrips() throws IOException {
		List<Path> samples = programs(SAMPLES);
		assertFalse(samples.isEmpty(), () -> "no samples in " + SAMPLES.toAbsolutePath());
		return samples.stream().map(path -> dynamicTest(path.getFileName().toString(), () -> {
			VecDSLIr.RoundTrip result = ir.roundTrip(load(path));
			assertTrue(result.passed(), () -> path.getFileName() + ": " + String.join("; ", result.failures()));
		}));
	}

	@TestFactory
	public Stream<DynamicTest> rejectedProgramsGiveDiagnostics() throws IOException {
		List<Path> rejected = programs(REJECTED);
		assertFalse(rejected.isEmpty(), () -> "no rejected programs in " + REJECTED.toAbsolutePath());
		return rejected.stream().map(path -> dynamicTest(path.getFileName().toString(), () -> {
			Matcher expect = EXPECT.matcher(Files.readString(path, StandardCharsets.UTF_8));
			assertTrue(expect.find(), () -> path.getFileName() + ": first line must be '// Expect: <code>'");
			Map<?, ?> document = (Map<?, ?>) Json.parse(ir.export(load(path)));
			assertFalse(document.containsKey("program"), "a rejected program has no IR");
			List<?> diagnostics = (List<?>) document.get("diagnostics");
			assertTrue(diagnostics.stream().anyMatch(d -> expect.group(1).equals(((Map<?, ?>) d).get("code"))),
					() -> path.getFileName() + ": expected " + expect.group(1) + " in " + diagnostics);
		}));
	}

	@Test
	public void layoutDoesNotChangeTheBody() throws Exception {
		String compact = "script s\ncreate collection docs { primary id field id type int vector-field type float_vector dim 2 }\n"
				+ "q: vector-search in docs top 2 query vector [1, 0] output *\n";
		String spread = "script   s\n\n\ncreate collection docs {\n    primary id\n    field id type int\n"
				+ "    vector-field type float_vector dim 2\n}\n\nq:\n  vector-search in docs top 2\n  query vector [1.0, 0]\n  output *\n";
		Map<?, ?> first = document(compact);
		Map<?, ?> second = document(spread);
		assertEquals(first.get("program"), second.get("program"));
		assertNotEquals(first.get("spans"), second.get("spans"));
	}

	@Test
	public void readerRejectsMissingAndUnknownFeatures() throws Exception {
		String text = ir.export(parseHelper.parse("script s\ncreate collection docs { primary id field id type int }\n").eResource());
		assertThrows(IllegalArgumentException.class, () -> new IrReader().read(text.replace("\"version\": null,", "")));
		assertThrows(IllegalArgumentException.class,
				() -> new IrReader().read(text.replace("\"version\": null,", "\"version\": null, \"colour\": 1,")));
		assertThrows(IllegalArgumentException.class, () -> new IrReader().read(text.replace("\"1.0\"", "\"0.9\"")));
	}

	private Map<?, ?> document(String program) throws Exception {
		return (Map<?, ?>) Json.parse(ir.export(parseHelper.parse(program).eResource()));
	}

	private Resource load(Path program) {
		return resourceSets.get().getResource(URI.createFileURI(program.toAbsolutePath().toString()), true);
	}

	private static List<Path> programs(Path directory) throws IOException {
		try (Stream<Path> listing = Files.list(directory)) {
			return listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
		}
	}
}
