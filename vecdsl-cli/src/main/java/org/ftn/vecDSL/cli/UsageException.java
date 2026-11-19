package org.ftn.vecDSL.cli;

/** A command line the tool cannot run: an unknown command or option, a missing value or operand (exit status 2). */
public final class UsageException extends Exception {

	private static final long serialVersionUID = 1L;

	public UsageException(String message) {
		super(message);
	}
}
