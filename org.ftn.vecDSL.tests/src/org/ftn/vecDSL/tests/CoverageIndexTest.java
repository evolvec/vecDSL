package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.ftn.vecDSL.coverage.ConstructIds;
import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.coverage.CoverageStatus;
import org.ftn.vecDSL.coverage.Pending;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The coverage index (org.ftn.vecDSL/src/org/ftn/vecDSL/coverage/index, written by coverage_index.py): every construct
 * id names a class or feature of the current metamodel, every index agrees with its COVERAGE.md, and the class
 * resources every host reads hold the files on disk. A failure of the second kind means a matrix changed after the
 * index was written: rerun coverage_index.py. The injector provider registers the metamodel, which the construct ids
 * resolve against.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class CoverageIndexTest {

	private static final Path INDEX = Path.of("..", "org.ftn.vecDSL", "src", "org", "ftn", "vecDSL", "coverage", "index");
	private static final Path GENERATOR = Path.of("..", "org.ftn.vecDSL", "src", "org", "ftn", "vecDSL", "generator");
	private static final VecDSLPackage P = VecDSLPackage.eINSTANCE;

	@TestFactory
	public Stream<DynamicTest> everyConstructIdNamesAMetamodelElement() throws IOException {
		return vendors().stream().map(vendor -> dynamicTest(vendor, () -> {
			CoverageIndex index = load(vendor);
			for (CoverageEntry entry : index.entries(vendor)) {
				EClass type = ConstructIds.eClass(entry.construct());
				assertNotNull(type, () -> vendor + ": no class for " + entry.construct());
				assertEquals(entry.eClass(), type.getName());
				if (entry.feature() != null) {
					assertNotNull(ConstructIds.feature(entry.construct()), () -> vendor + ": no feature for " + entry.construct());
				}
			}
		}));
	}

	@TestFactory
	public Stream<DynamicTest> indexAgreesWithTheCoverageMatrix() throws IOException {
		return vendors().stream().map(vendor -> dynamicTest(vendor, () -> {
			List<String[]> rows = matrix(GENERATOR.resolve(vendor).resolve("COVERAGE.md"));
			List<CoverageEntry> entries = new ArrayList<>(load(vendor).entries(vendor));
			assertEquals(rows.size(), entries.size(), () -> vendor + ": rerun coverage_index.py");
			for (int i = 0; i < rows.size(); i++) {
				String[] row = rows.get(i);
				CoverageEntry entry = entries.get(i);
				assertEquals(row[0], entry.construct(), () -> vendor + ": rerun coverage_index.py");
				assertEquals(CoverageStatus.of(row[2]), entry.status(), () -> vendor + " " + row[0] + ": rerun coverage_index.py");
				assertEquals(row[8], entry.notes(), () -> vendor + " " + row[0] + ": rerun coverage_index.py");
			}
		}));
	}

	@Test
	public void everyMatrixHasAnIndex() throws IOException {
		List<String> matrices;
		try (Stream<Path> listing = Files.list(GENERATOR)) {
			matrices = listing.filter(directory -> Files.exists(directory.resolve("COVERAGE.md")))
					.map(directory -> directory.getFileName().toString()).sorted().toList();
		}
		assertFalse(matrices.isEmpty());
		assertEquals(matrices, vendors());
	}

	@Test
	public void classpathIndexHoldsTheFilesOnDisk() throws IOException {
		CoverageIndex disk = CoverageIndex.load(vendors(), CoverageIndexTest::open);
		CoverageIndex classpath = CoverageIndex.fromClasspath();
		assertEquals(vendors(), VecDSLOutputConfigurationProvider.VENDORS.stream().sorted().toList(),
				"every vendor with an index file is in VecDSLOutputConfigurationProvider.VENDORS and back");
		assertEquals(vendors(), classpath.vendors().stream().sorted().toList(), "F5 on org.ftn.vecDSL");
		for (String vendor : vendors()) {
			assertEquals(disk.entries(vendor).size(), classpath.entries(vendor).size(), vendor + ": F5 on org.ftn.vecDSL");
		}
	}

	@Test
	public void featureRowsWinOverClassRows() {
		CoverageIndex index = CoverageIndex.load(Set.of("pgvector"), vendor -> json(vendor,
				row("Method KeywordSearch", "KeywordSearch", null, "native", "none"),
				row("KeywordSearch.fuzziness", "KeywordSearch", "fuzziness", "unsupported", "none")));
		assertEquals("KeywordSearch.fuzziness", index.entry("pgvector", P.getKeywordSearch(),
				P.getKeywordSearch_Fuzziness()).orElseThrow().construct());
		assertEquals("Method KeywordSearch", index.entry("pgvector", P.getKeywordSearch(),
				P.getKeywordSearch_Slop()).orElseThrow().construct());
		assertTrue(index.entry("pgvector", P.getKeywordSearch(), P.getKeywordSearch_Fuzziness()).orElseThrow().isUnsupported());
	}

	@Test
	public void generationPendingIsItsOwnMarker() {
		CoverageIndex index = CoverageIndex.load(Set.of("milvus"),
				vendor -> json(vendor, row("Concept Reranker", "Reranker", null, "native", "construct")));
		CoverageEntry entry = index.entry("milvus", P.getReranker(), null).orElseThrow();
		assertEquals(Pending.CONSTRUCT, entry.pending());
		assertTrue(entry.isGenerationPending());
	}

	@Test
	public void vendorWithoutAFileHasNoData() {
		CoverageIndex index = CoverageIndex.load(Set.of("redis"), vendor -> null);
		assertFalse(index.hasData("redis"));
		assertTrue(index.entry("redis", P.getVectorSearch(), null).isEmpty());
	}

	@Test
	public void unknownStatusIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> CoverageIndex.load(Set.of("qdrant"),
				vendor -> json(vendor, row("Method Count", "Count", null, "partial", "none"))));
	}

	@Test
	public void constructIdsFollowTheMatrixConvention() {
		assertEquals("Method VectorSearch", ConstructIds.of(P.getVectorSearch()));
		assertEquals("Concept VectorCollection", ConstructIds.of(P.getVectorCollection()));
		assertEquals("AlterOperation AddFieldOp", ConstructIds.of(P.getAddFieldOp()));
		assertEquals("ExtensionItem ContextSearch", ConstructIds.of(P.getContextSearch()));
		assertEquals("KeywordSearch.fuzziness", ConstructIds.of(P.getKeywordSearch(), P.getKeywordSearch_Fuzziness()));
		assertEquals(P.getKeywordSearch_Fuzziness(), ConstructIds.feature("KeywordSearch.fuzziness"));
		assertEquals(null, ConstructIds.eClass("Concept VectorSearch"));
	}

	private static List<String> vendors() throws IOException {
		try (Stream<Path> listing = Files.list(INDEX)) {
			return listing.map(file -> file.getFileName().toString()).filter(name -> name.endsWith(".json"))
					.map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
		}
	}

	private static CoverageIndex load(String vendor) {
		return CoverageIndex.load(Set.of(vendor), CoverageIndexTest::open);
	}

	private static InputStream open(String vendor) {
		try {
			return Files.newInputStream(INDEX.resolve(vendor + ".json"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Rows of the coverage table: construct, rule, status, three calls, reference, test, notes. */
	private static List<String[]> matrix(Path file) throws IOException {
		List<String[]> rows = new ArrayList<>();
		boolean table = false;
		for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
			if (line.startsWith("| construct |")) {
				table = true;
			} else if (table && line.startsWith("|---")) {
				continue;
			} else if (table && line.startsWith("|")) {
				String body = line.strip();
				String[] cells = body.substring(1, body.length() - 1).split("(?<!\\\\)\\|", -1);
				for (int i = 0; i < cells.length; i++) {
					cells[i] = cells[i].strip().replace("\\|", "|");
				}
				assertEquals(9, cells.length, () -> file + ": " + line);
				rows.add(cells);
			} else if (table) {
				break;
			}
		}
		return rows;
	}

	private static String row(String construct, String eClass, String feature, String status, String pending) {
		return "{\"construct\": \"" + construct + "\", \"eClass\": \"" + eClass + "\", \"feature\": "
				+ (feature == null ? "null" : "\"" + feature + "\"") + ", \"rule\": \"" + eClass + "\", \"status\": \""
				+ status + "\", \"pending\": \"" + pending + "\", \"reason\": \"why \\\"quoted\\\" \\u00e9\", "
				+ "\"calls\": {\"python\": \"`rt.x`\", \"typescript\": \"`rt.x`\", \"java\": \"`x`\"}, "
				+ "\"reference\": \"https://example.org\", \"test\": \"T#t\", \"notes\": \"n\"}";
	}

	private static InputStream json(String vendor, String... rows) {
		String text = "{\"vendor\": \"" + vendor + "\", \"rows\": " + rows.length + ", \"constructs\": ["
				+ String.join(", ", rows) + "]}";
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}
