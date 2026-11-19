package org.ftn.vecDSL.lsp;

import java.util.function.IntConsumer;

import org.eclipse.xtext.ide.server.ILanguageServerShutdownAndExitHandler;

/**
 * The exit semantics of the protocol: status 0 when shutdown came before exit, 1 otherwise. Xtext's default handler
 * does the same with System.exit; here the action is given (System::exit in the jar, a recorder in the tests), and the
 * status is also what the jar returns when the client closes the input without sending exit.
 */
final class ShutdownAndExit implements ILanguageServerShutdownAndExitHandler {

	private final IntConsumer action;
	private volatile boolean shutdown;

	ShutdownAndExit(IntConsumer action) {
		this.action = action;
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void exit() {
		action.accept(status());
	}

	int status() {
		return shutdown ? 0 : 1;
	}
}
