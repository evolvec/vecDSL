package org.ftn.vecDSL.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

/** The command line without a server: version, usage errors. */
public class MainTest {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	private int run(String... args) throws Exception {
		return Main.run(args, InputStream.nullInputStream(), new PrintStream(out, true, StandardCharsets.UTF_8),
				new PrintStream(err, true, StandardCharsets.UTF_8));
	}

	@Test
	public void versionPrintsTheCliLinesAndLsp4j() throws Exception {
		assertEquals(Main.OK, run("version"));
		assertEquals(List.of("language: vecDSL 1.0 (1.0.1 documentation patch)", "IR: 1.0",
				"grammar namespace: http://www.ftn.org/vecDSL/VecDSL/7", "vecdsl-lsp: 0.1.0 (implements vecDSL >=1.0,<2.0)",
				"LSP4J: 1.0.0"), out.toString(StandardCharsets.UTF_8).lines().toList());
		assertEquals("", err.toString(StandardCharsets.UTF_8));
	}

	@Test
	public void usageErrorsExitWithTwo() throws Exception {
		for (List<String> args : List.of(List.of("serve"), List.of("--log"), List.of("--socket", "0"),
				List.of("--socket", "65536"), List.of("--socket", "x"), List.of("version", "--log", "f"))) {
			err.reset();
			assertEquals(Main.USAGE, run(args.toArray(String[]::new)), args.toString());
			assertEquals(Main.USAGE_LINE, err.toString(StandardCharsets.UTF_8).lines().toList().get(1), args.toString());
		}
		assertEquals("", out.toString(StandardCharsets.UTF_8));
	}
}
