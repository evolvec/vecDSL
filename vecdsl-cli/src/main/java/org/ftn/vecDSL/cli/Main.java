package org.ftn.vecDSL.cli;

/** Entry point of the runnable jar: {@code java -jar vecdsl-cli.jar <command> ...}. */
public final class Main {

	private Main() {
	}

	public static void main(String[] args) {
		System.exit(new Vecdsl(System.out, System.err).run(args));
	}
}
