package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import java.util.Set;

import org.ftn.vecDSL.ir.IrWriter;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

/**
 * {@code vecdsl version}: the language version (vecDSL 1.0, documentation patch 1.0.1, spec/CHANGELOG.md), the IR
 * version the exporter writes, the EMF namespace of the grammar, and the tool's own version from the pom, which
 * Maven filters into vecdsl-cli.properties.
 */
final class VersionCommand implements Command {

	static final String LANGUAGE = "vecDSL 1.0 (1.0.1 documentation patch)";
	static final String RANGE = "vecDSL >=1.0,<2.0";

	private final PrintStream out;

	VersionCommand(PrintStream out) {
		this.out = out;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(), Set.of());
	}

	@Override
	public String usage() {
		return "vecdsl version";
	}

	@Override
	public int run(Args args) throws UsageException {
		args.operands(0, 0, "nothing");
		out.println("language: " + LANGUAGE);
		out.println("IR: " + IrWriter.VERSION);
		out.println("grammar namespace: " + VecDSLPackage.eNS_URI);
		out.println("vecdsl-cli: " + tool() + " (implements " + RANGE + ")");
		return Vecdsl.OK;
	}

	static String summary() {
		return LANGUAGE + ", vecdsl-cli " + tool();
	}

	static String tool() {
		Properties properties = new Properties();
		try (InputStream in = VersionCommand.class.getResourceAsStream("vecdsl-cli.properties")) {
			if (in == null) {
				throw new IllegalStateException("vecdsl-cli.properties is not on the classpath");
			}
			properties.load(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return properties.getProperty("version");
	}
}
