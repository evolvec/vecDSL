package org.ftn.vecDSL.lsp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.ftn.vecDSL.ir.IrWriter;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/**
 * Entry point of the runnable jar: {@code java -jar vecdsl-lsp.jar [--log <file>] [--socket <port>]} serves the
 * Language Server Protocol over standard input and output (or a socket on 127.0.0.1), {@code version} prints the
 * versions. Standard output carries only the protocol: anything else written to it goes to standard error, which
 * {@code --log} appends to a file.
 */
public final class Main {

	static final int OK = 0;
	static final int USAGE = 2;

	static final String USAGE_LINE = "usage: vecdsl-lsp [--log <file>] [--socket <port>] | vecdsl-lsp version";

	/** The language lines of {@code vecdsl version} (vecdsl-cli VersionCommand). */
	static final String LANGUAGE = "vecDSL 1.0 (1.0.1 documentation patch)";
	static final String RANGE = "vecDSL >=1.0,<2.0";

	private Main() {
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		System.exit(run(args, System.in, System.out, System.err));
	}

	static int run(String[] args, InputStream in, PrintStream out, PrintStream err)
			throws IOException, InterruptedException {
		Options options;
		try {
			options = Options.parse(List.of(args));
		} catch (IllegalArgumentException e) {
			err.println("vecdsl-lsp: " + e.getMessage());
			err.println(USAGE_LINE);
			return USAGE;
		}
		if (options.version()) {
			version(out);
			return OK;
		}
		if (options.log() != null) {
			System.setErr(new PrintStream(new FileOutputStream(options.log(), true), true, StandardCharsets.UTF_8));
		}
		System.setOut(System.err);
		System.setIn(InputStream.nullInputStream());
		ShutdownAndExit exit = new ShutdownAndExit(System::exit);
		if (options.port() != null) {
			System.err.println("vecdsl-lsp: listening on 127.0.0.1:" + options.port());
			Server.listen(options.port(), exit);
			return OK;
		}
		try {
			Server.listen(in, out, exit).get();
		} catch (ExecutionException e) {
			e.getCause().printStackTrace();
		}
		return exit.status();
	}

	static void version(PrintStream out) {
		Properties properties = properties();
		out.println("language: " + LANGUAGE);
		out.println("IR: " + IrWriter.VERSION);
		out.println("grammar namespace: " + VecDSLPackage.eNS_URI);
		out.println("vecdsl-lsp: " + properties.getProperty("version") + " (implements " + RANGE + ")");
		out.println("LSP4J: " + properties.getProperty("lsp4j"));
	}

	private static Properties properties() {
		Properties properties = new Properties();
		try (InputStream in = Main.class.getResourceAsStream("vecdsl-lsp.properties")) {
			if (in == null) {
				throw new IllegalStateException("vecdsl-lsp.properties is not on the classpath");
			}
			properties.load(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return properties;
	}

	/** The command line: {@code version}, or the server with an optional log file and socket port. */
	record Options(boolean version, String log, Integer port) {

		static Options parse(List<String> args) {
			if (args.equals(List.of("version"))) {
				return new Options(true, null, null);
			}
			String log = null;
			Integer port = null;
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				switch (arg) {
					case "--log" -> log = value(args, ++i, arg);
					case "--socket" -> port = port(value(args, ++i, arg));
					default -> throw new IllegalArgumentException("unknown argument '" + arg + "'");
				}
			}
			return new Options(false, log, port);
		}

		private static String value(List<String> args, int index, String option) {
			if (index >= args.size()) {
				throw new IllegalArgumentException(option + " needs a value");
			}
			return args.get(index);
		}

		private static int port(String value) {
			if (value.matches("[0-9]{1,5}") && Integer.parseInt(value) >= 1 && Integer.parseInt(value) <= 65535) {
				return Integer.parseInt(value);
			}
			throw new IllegalArgumentException("--socket takes a port from 1 to 65535, not '" + value + "'");
		}
	}
}
