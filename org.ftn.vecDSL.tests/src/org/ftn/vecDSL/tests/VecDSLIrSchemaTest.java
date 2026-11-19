package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.ftn.vecDSL.ir.Json;
import org.ftn.vecDSL.ir.VecDSLIr;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Every exported document against spec/ir/vecdsl-ir-1.0.schema.json. No JSON Schema validator is on the test classpath
 * (org.ftn.vecDSL adds no bundle dependency), so the test checks the structural part in Java: each element's "$type" has a
 * definition, and its keys are exactly the definition's required keys, "$norm" included where the definition has one.
 * It writes every document to generated/ir/, where the full draft 2020-12 validation runs in Python:
 * {@code python spec/tools/ir_schema.py --check org.ftn.vecDSL.tests/generated/ir/*.json} from the workspace root.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLIrSchemaTest {

	private static final Path SCHEMA = Path.of("../spec/ir/vecdsl-ir-1.0.schema.json");
	private static final Path OUT = Path.of("generated", "ir");
	private static final List<Path> SOURCES = List.of(Path.of("samples"),
			Path.of(System.getProperty("vecdsl.ir.rejected", "../spec/negative")));

	@Inject
	private VecDSLIr ir;

	@Inject
	private Provider<XtextResourceSet> resourceSets;

	@TestFactory
	public Stream<DynamicTest> everyDocumentMatchesTheSchema() throws IOException {
		Map<?, ?> definitions = (Map<?, ?>) ((Map<?, ?>) Json.parse(Files.readString(SCHEMA, StandardCharsets.UTF_8)))
				.get("$defs");
		Files.createDirectories(OUT);
		Stream<DynamicTest> tests = Stream.empty();
		for (Path source : SOURCES) {
			List<Path> programs;
			try (Stream<Path> listing = Files.list(source)) {
				programs = listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
			}
			assertFalse(programs.isEmpty(), () -> "no programs in " + source.toAbsolutePath());
			tests = Stream.concat(tests, programs.stream().map(path -> dynamicTest(path.getFileName().toString(), () -> {
				String text = ir.export(resourceSets.get()
						.getResource(URI.createFileURI(path.toAbsolutePath().toString()), true));
				String name = path.getFileName().toString().replace(".vecDSL", ".json");
				Files.writeString(OUT.resolve(name), text, StandardCharsets.UTF_8);
				Map<?, ?> document = (Map<?, ?>) Json.parse(text);
				Map<?, ?> shape = (Map<?, ?>) definitions.get(document.containsKey("program") ? "document" : "diagnostics");
				assertEquals(Set.copyOf((List<?>) shape.get("required")), document.keySet(), name);
				if (document.containsKey("program")) {
					check(document.get("program"), definitions, "", name);
				}
			})));
		}
		return tests;
	}

	private static void check(Object value, Map<?, ?> definitions, String path, String file) {
		if (value instanceof List<?> list) {
			for (int i = 0; i < list.size(); i++) {
				check(list.get(i), definitions, path + "/" + i, file);
			}
		} else if (value instanceof Map<?, ?> map) {
			Object type = map.get("$type");
			if (type == null) {
				map.forEach((key, child) -> check(child, definitions, path + "/" + key, file));
				return;
			}
			Map<?, ?> definition = (Map<?, ?>) definitions.get(type);
			assertTrue(definition != null, () -> file + path + ": no definition for " + type);
			Set<Object> expected = new HashSet<>((List<?>) definition.get("required"));
			if (((Map<?, ?>) definition.get("properties")).containsKey("$norm")) {
				expected.add("$norm");
			}
			assertEquals(expected, map.keySet(), () -> file + path + ": keys of " + type);
			map.forEach((key, child) -> {
				if (!"$norm".equals(key)) {
					check(child, definitions, path + "/" + key, file);
				}
			});
		}
	}
}
