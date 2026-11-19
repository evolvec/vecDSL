package org.ftn.vecDSL.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.ir.Json;
import org.junit.jupiter.api.Test;

/**
 * The commands run against the workspace (system property vecdsl.workspace, set by the pom): the 28 samples, the
 * seven rejected programs with their conformance documents, and the coverage index of the runtime bundle.
 */
public class VecdslTest {

	static final Path WORKSPACE = Path.of(System.getProperty("vecdsl.workspace", ".."));
	static final Path SAMPLES = WORKSPACE.resolve("org.ftn.vecDSL.tests/samples");
	static final Path NEGATIVE = WORKSPACE.resolve("spec/negative");
	static final Path CONFORMANCE = WORKSPACE.resolve("spec/conformance");
	static final Path COVERAGE = WORKSPACE.resolve("org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index");

	/** Exit status and output of one run of the tool. */
	record Run(int status, String out, String err) {
	}

	static Run run(String... args) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		int status;
		try (PrintStream outStream = new PrintStream(out, true, StandardCharsets.UTF_8);
				PrintStream errStream = new PrintStream(err, true, StandardCharsets.UTF_8)) {
			status = new Vecdsl(outStream, errStream).run(args);
		}
		return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
	}

	static List<Path> programs(Path folder) throws IOException {
		try (Stream<Path> listing = Files.list(folder)) {
			return listing.filter(path -> path.toString().endsWith(Toolchain.EXTENSION)).sorted().toList();
		}
	}

	@Test
	public void versionNamesLanguageIrAndNamespace() {
		Run run = run("version");
		assertEquals(Vecdsl.OK, run.status());
		assertTrue(run.out().contains("language: vecDSL 1.0 (1.0.1 documentation patch)"), run.out());
		assertTrue(run.out().contains("IR: 1.0"), run.out());
		assertTrue(run.out().contains("grammar namespace: http://www.ftn.org/vecDSL/VecDSL/7"), run.out());
	}

	@Test
	public void everySampleIsValid() throws IOException {
		Run run = run("check", SAMPLES.toString());
		assertEquals(Vecdsl.OK, run.status(), run.out());
		assertTrue(run.out().contains(programs(SAMPLES).size() + " programs, 0 errors"), run.out());
	}

	@Test
	public void rejectedProgramsGiveTheirConformanceDocuments() throws IOException {
		List<Path> rejected = programs(NEGATIVE);
		assertFalse(rejected.isEmpty());
		for (Path program : rejected) {
			Run run = run("check", "--json", program.toString());
			String name = program.getFileName().toString().replace(Toolchain.EXTENSION, "");
			assertEquals(Vecdsl.FINDINGS, run.status(), name);
			assertEquals(Files.readString(CONFORMANCE.resolve(name).resolve("diagnostics.json")), run.out(), name);
		}
	}

	@Test
	public void formattingIsIdempotentAndKeepsTheModel() throws IOException {
		Path copies = Files.createTempDirectory("vecdsl-fmt");
		for (Path sample : programs(SAMPLES)) {
			Files.copy(sample, copies.resolve(sample.getFileName()));
		}
		Run write = run("fmt", "--write", copies.toString());
		assertTrue(write.status() == Vecdsl.OK, write.out() + write.err());
		Run check = run("fmt", "--check", copies.toString());
		assertEquals(Vecdsl.OK, check.status(), check.out() + check.err());
		assertEquals("", check.out());
	}

	@Test
	public void widthChangesTheLayout() throws IOException {
		Path sample = SAMPLES.resolve("milvus_demo.vecDSL");
		Run wide = run("fmt", "--width", "200", sample.toString());
		Run narrow = run("fmt", "--width", "20", sample.toString());
		assertEquals(Vecdsl.OK, wide.status(), wide.err());
		assertEquals(Vecdsl.OK, narrow.status(), narrow.err());
		assertTrue(narrow.out().lines().count() > wide.out().lines().count());
	}

	@Test
	public void irRoundTripPassesOnEverySample() throws IOException {
		Run run = run("ir", "--check", SAMPLES.toString());
		int samples = programs(SAMPLES).size();
		assertEquals(Vecdsl.OK, run.status(), run.out());
		assertTrue(run.out().contains(samples + " programs, " + samples + " round trips passed, 0 failed"), run.out());
	}

	@Test
	public void irOfARejectedProgramIsItsDiagnosticsDocument() {
		Run run = run("ir", NEGATIVE.resolve("wf_recall_one.vecDSL").toString());
		assertEquals(Vecdsl.FINDINGS, run.status());
		assertTrue(run.out().contains("\"code\": \"wf.recall.one\""), run.out());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void coverageSummaryCountsEveryRowOnce() throws IOException {
		Run run = run("coverage", "--json");
		assertEquals(Vecdsl.OK, run.status(), run.err());
		List<Object> vendors = (List<Object>) Json.parse(run.out());
		assertEquals(VecDSLOutputConfigurationProvider.VENDORS.size(), vendors.size());
		for (Object item : vendors) {
			Map<String, Object> row = (Map<String, Object>) item;
			Path file = COVERAGE.resolve(row.get("vendor") + ".json");
			if (!Files.exists(file)) {
				assertEquals(Boolean.FALSE, row.get("hasData"));
				continue;
			}
			Map<String, Integer> expected = new LinkedHashMap<>();
			List<Object> constructs = (List<Object>) ((Map<String, Object>) Json.parse(Files.readString(file)))
					.get("constructs");
			for (Object construct : constructs) {
				Map<String, Object> entry = (Map<String, Object>) construct;
				String cell = "construct".equals(entry.get("pending")) ? "pending" : (String) entry.get("status");
				expected.merge(cell, 1, Integer::sum);
			}
			for (String column : CoverageCommand.COLUMNS) {
				assertEquals(expected.getOrDefault(column, 0), ((Number) row.get(column)).intValue(),
						row.get("vendor") + " " + column);
			}
			assertEquals(constructs.size(), ((Number) row.get("rows")).intValue());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void explainShowsTheIndexRowOfEachConstruct() throws IOException {
		Run run = run("explain", SAMPLES.resolve("milvus_demo.vecDSL").toString(), "--json");
		assertEquals(Vecdsl.OK, run.status(), run.out());
		Map<String, Object> document = (Map<String, Object>) Json.parse(run.out());
		assertEquals(List.of("milvus"), document.get("targets"));
		Map<String, Map<String, Object>> index = new LinkedHashMap<>();
		for (Object construct : (List<Object>) ((Map<String, Object>) Json.parse(
				Files.readString(COVERAGE.resolve("milvus.json")))).get("constructs")) {
			Map<String, Object> entry = (Map<String, Object>) construct;
			index.put((String) entry.get("construct"), entry);
		}
		int rows = 0;
		for (Object statement : (List<Object>) document.get("statements")) {
			for (Object item : (List<Object>) ((Map<String, Object>) statement).get("constructs")) {
				Map<String, Object> construct = (Map<String, Object>) item;
				Map<String, Object> vendor = (Map<String, Object>) ((List<Object>) construct.get("vendors")).get(0);
				Map<String, Object> entry = index.get((String) vendor.get("row"));
				assertEquals(entry.get("status"), vendor.get("status"), construct.get("construct").toString());
				assertEquals(((Map<String, Object>) entry.get("calls")).get("python"), vendor.get("python"));
				assertEquals(entry.get("reference"), vendor.get("reference"));
				rows++;
			}
		}
		assertTrue(rows > 0);
	}

	@Test
	public void dryRunListsWhatGenerationWrites() throws IOException {
		Path sample = SAMPLES.resolve("redis_demo.vecDSL");
		Path folder = Files.createTempDirectory("vecdsl-gen");
		Run dry = run("gen", sample.toString(), "--target", "all", "--lang", "all", "-o", folder.toString(),
				"--dry-run");
		Run real = run("gen", sample.toString(), "--target", "all", "--lang", "all", "-o", folder.toString());
		assertEquals(Vecdsl.OK, dry.status(), dry.out());
		assertEquals(Vecdsl.OK, real.status(), real.out());
		assertEquals(dry.out().replace("would write ", "wrote ").replace(" (dry run)", ""), real.out());
		for (String line : real.out().lines().filter(line -> line.startsWith("wrote ")).toList()) {
			assertTrue(Files.isRegularFile(Path.of(line.substring("wrote ".length()))), line);
		}
	}

	@Test
	public void languageSelectionNarrowsTheOutput() throws IOException {
		Path folder = Files.createTempDirectory("vecdsl-gen");
		Run run = run("gen", SAMPLES.resolve("qdrant_demo.vecDSL").toString(), "--target", "qdrant", "--lang", "ts",
				"-o", folder.toString(), "--dry-run");
		assertEquals(Vecdsl.OK, run.status(), run.out());
		List<String> written = run.out().lines().filter(line -> line.startsWith("would write ")).toList();
		assertFalse(written.isEmpty());
		Path vendor = folder.resolve("qdrant");
		List<String> outside = new ArrayList<>();
		for (String line : written) {
			Path file = Path.of(line.substring("would write ".length())).toAbsolutePath();
			Path relative = vendor.toAbsolutePath().relativize(file);
			if (relative.getNameCount() > 1 && !relative.getName(0).toString().equals("typescript")) {
				outside.add(line);
			}
		}
		assertEquals(List.of(), outside);
	}

	@Test
	public void usageErrorsExitWithTwo() {
		assertEquals(Vecdsl.USAGE, run().status());
		assertEquals(Vecdsl.USAGE, run("compile").status());
		assertEquals(Vecdsl.USAGE, run("check").status());
		assertEquals(Vecdsl.USAGE, run("check", "--strict", SAMPLES.toString()).status());
		assertEquals(Vecdsl.USAGE, run("fmt", "--check", "--write", SAMPLES.toString()).status());
		assertEquals(Vecdsl.USAGE, run("gen", SAMPLES.resolve("milvus_demo.vecDSL").toString(), "-o", "x").status());
		assertEquals(Vecdsl.USAGE, run("gen", SAMPLES.resolve("milvus_demo.vecDSL").toString(), "--target", "mysql",
				"-o", "x").status());
		assertEquals(Vecdsl.USAGE, run("coverage", "--status", "partial").status());
		assertEquals(Vecdsl.USAGE, run("ir", "--check").status());
		assertEquals(Vecdsl.OK, run("--help").status());
	}

	@Test
	public void everyVendorReadsTheLanguagesProperty() throws ReflectiveOperationException {
		for (String vendor : VecDSLOutputConfigurationProvider.VENDORS) {
			Class<?> generator = generatorClass(vendor);
			assertEquals(GenerateCommand.LANGUAGES_PROPERTY, generator.getField("LANGUAGES_PROPERTY").get(null),
					generator.getName());
		}
	}

	private static Class<?> generatorClass(String vendor) throws ClassNotFoundException {
		String prefix = "org.ftn.vecDSL.generator." + vendor + ".";
		String name = vendor.equals("opensearch") ? "OpenSearch"
				: Character.toUpperCase(vendor.charAt(0)) + vendor.substring(1);
		return Class.forName(prefix + name + "Generator");
	}
}
