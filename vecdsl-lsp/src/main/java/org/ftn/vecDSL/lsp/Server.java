package org.ftn.vecDSL.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.xtext.ide.server.ILanguageServerShutdownAndExitHandler;
import org.eclipse.xtext.ide.server.ServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

/**
 * Xtext's language server: LanguageServerImpl (as VecDSLLanguageServer) with Xtext's ServerModule, which finds vecDSL
 * through the META-INF/services/org.eclipse.xtext.ISetup entry (VecDSLServerSetup), and the exit handler given.
 */
final class Server {

	private Server() {
	}

	static Injector injector(ILanguageServerShutdownAndExitHandler exit) {
		return Guice.createInjector(Modules.override(new ServerModule())
				.with(binder -> binder.bind(ILanguageServerShutdownAndExitHandler.class).toInstance(exit)));
	}

	/** Serves one client over the streams; the future completes when the input ends. */
	static Future<Void> listen(InputStream in, OutputStream out, ILanguageServerShutdownAndExitHandler exit) {
		return listen(injector(exit), in, out);
	}

	/** Serves clients one connection each on 127.0.0.1, as Xtext's SocketServerLauncher does on 0.0.0.0. */
	static void listen(int port, ILanguageServerShutdownAndExitHandler exit) throws IOException {
		Injector injector = injector(exit);
		try (ServerSocket socket = new ServerSocket(port, 50, InetAddress.getLoopbackAddress())) {
			while (true) {
				Socket client = socket.accept();
				listen(injector, client.getInputStream(), client.getOutputStream());
			}
		}
	}

	private static Future<Void> listen(Injector injector, InputStream in, OutputStream out) {
		VecDSLLanguageServer server = injector.getInstance(VecDSLLanguageServer.class);
		Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in, out, true, null);
		server.connect(launcher.getRemoteProxy());
		return launcher.startListening();
	}
}
