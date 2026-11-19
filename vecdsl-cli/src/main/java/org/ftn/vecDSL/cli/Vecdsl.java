package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The command dispatcher. Exit status: 0 ok, 1 diagnostics or differences, 2 usage error, 3 internal error (the stack
 * trace is printed only under {@code --verbose}, which every command accepts).
 */
public final class Vecdsl {

	public static final int OK = 0;
	public static final int FINDINGS = 1;
	public static final int USAGE = 2;
	public static final int INTERNAL = 3;

	static final String VERBOSE = "--verbose";

	private final PrintStream out;
	private final PrintStream err;
	private final Map<String, Command> commands = new LinkedHashMap<>();

	public Vecdsl(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
		Toolchain toolchain = new Toolchain();
		commands.put("check", new CheckCommand(toolchain, out));
		commands.put("fmt", new FormatCommand(toolchain, out));
		commands.put("ir", new IrCommand(out, err));
		commands.put("gen", new GenerateCommand(toolchain, out));
		commands.put("explain", new ExplainCommand(toolchain, out));
		commands.put("coverage", new CoverageCommand(toolchain, out));
		commands.put("version", new VersionCommand(out));
	}

	public int run(String[] arguments) {
		List<String> words = new ArrayList<>(Arrays.asList(arguments));
		boolean verbose = words.removeIf(VERBOSE::equals);
		if (words.isEmpty()) {
			usage(err);
			return USAGE;
		}
		String name = words.get(0);
		if (name.equals("help") || name.equals("--help") || name.equals("-h")) {
			usage(out);
			return OK;
		}
		Command command = commands.get(name);
		if (command == null) {
			err.println("vecdsl: unknown command '" + name + "'");
			usage(err);
			return USAGE;
		}
		try {
			return command.run(Args.parse(words.subList(1, words.size()), command.spec()));
		} catch (UsageException e) {
			err.println("vecdsl " + name + ": " + e.getMessage());
			err.println("usage: " + command.usage());
			return USAGE;
		} catch (IOException | RuntimeException | LinkageError e) {
			err.println("vecdsl " + name + ": internal error: " + e);
			if (verbose) {
				e.printStackTrace(err);
			} else {
				err.println("(run again with --verbose for the stack trace)");
			}
			return INTERNAL;
		}
	}

	private void usage(PrintStream stream) {
		stream.println("vecDSL toolchain, " + VersionCommand.summary());
		stream.println("usage:");
		for (Command command : commands.values()) {
			stream.println("  " + command.usage());
		}
		stream.println("every command accepts " + VERBOSE + "; exit status: 0 ok, 1 diagnostics or differences, "
				+ "2 usage error, 3 internal error");
	}
}
