package org.ftn.vecDSL.cli;

import java.io.IOException;

/** One command of the tool: its accepted options, its usage line, and its run returning the exit status. */
interface Command {

	Args.Spec spec();

	String usage();

	int run(Args args) throws IOException, UsageException;
}
