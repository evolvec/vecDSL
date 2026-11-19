package org.ftn.vecDSL.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * The client side of one LSP session with LSP4J's launcher. With the system property vecdsl.lsp.jar naming an existing
 * file the server is that jar, started as a subprocess with only its own class path (failsafe, after shade); otherwise
 * it runs in this JVM over piped streams (surefire). Every request and wait has a timeout of {@link #TIMEOUT} seconds.
 */
final class Connection {

	static final long TIMEOUT = 60;

	/** The server interface with vecDSL's request. */
	interface VecdslServer extends LanguageServer {
		@JsonRequest(GenerateEndpoint.METHOD)
		CompletableFuture<GenerateEndpoint.GenerateResult> generate(GenerateEndpoint.GenerateParams params);
	}

	private final Path root;
	private final Process process;
	private final CompletableFuture<Integer> inProcessExit;
	private final Client client = new Client();
	private final VecdslServer server;
	private final Map<String, Integer> versions = new HashMap<>();
	private final Map<String, String> texts = new HashMap<>();
	private boolean exited;
	final ServerCapabilities capabilities;

	private Connection(Path root, boolean rooted, Process process, CompletableFuture<Integer> inProcessExit,
			InputStream in, OutputStream out) throws Exception {
		this.root = root;
		this.process = process;
		this.inProcessExit = inProcessExit;
		Launcher<VecdslServer> launcher = new Launcher.Builder<VecdslServer>().setLocalService(client)
				.setRemoteInterface(VecdslServer.class).setInput(in).setOutput(out).create();
		server = launcher.getRemoteProxy();
		launcher.startListening();
		InitializeParams params = new InitializeParams();
		params.setProcessId((int) ProcessHandle.current().pid());
		ClientCapabilities capabilities = new ClientCapabilities();
		if (rooted) {
			params.setWorkspaceFolders(List.of(new WorkspaceFolder(root.toUri().toString(), "root")));
			WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
			workspace.setWorkspaceFolders(true);
			capabilities.setWorkspace(workspace);
		}
		params.setCapabilities(capabilities);
		this.capabilities = server.initialize(params).get(TIMEOUT, TimeUnit.SECONDS).getCapabilities();
		server.initialized(new InitializedParams());
	}

	/** "subprocess of the jar" or "in-process over piped streams". */
	static String mode() {
		return jar() == null ? "in-process over piped streams" : "subprocess of " + jar();
	}

	private static Path jar() {
		String jar = System.getProperty("vecdsl.lsp.jar");
		return jar != null && Files.isRegularFile(Path.of(jar)) ? Path.of(jar) : null;
	}

	/** A server for documents under a new temporary folder (the session's root). */
	static Connection start() throws Exception {
		return start(true);
	}

	/**
	 * A server initialized with neither rootUri nor workspace folders, and no workspace folders capability (a client
	 * with single files and no folder); the documents still go to a new temporary folder.
	 */
	static Connection startWithoutRoot() throws Exception {
		return start(false);
	}

	private static Connection start(boolean rooted) throws Exception {
		Path root = Files.createTempDirectory("vecdsl-lsp-test").toRealPath();
		Path jar = jar();
		if (jar != null) {
			List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString()));
			if (Runtime.version().feature() >= 24) {
				command.add("--sun-misc-unsafe-memory-access=allow");
			}
			command.addAll(List.of("-jar", jar.toString()));
			Process process = new ProcessBuilder(command).redirectError(root.resolve("server.stderr").toFile()).start();
			return new Connection(root, rooted, process, null, process.getInputStream(), process.getOutputStream());
		}
		PipedInputStream serverIn = new PipedInputStream(1 << 16);
		PipedOutputStream clientOut = new PipedOutputStream(serverIn);
		PipedInputStream clientIn = new PipedInputStream(1 << 16);
		PipedOutputStream serverOut = new PipedOutputStream(clientIn);
		CompletableFuture<Integer> exit = new CompletableFuture<>();
		Server.listen(serverIn, serverOut, new ShutdownAndExit(exit::complete));
		return new Connection(root, rooted, null, exit, clientIn, clientOut);
	}

	VecdslServer server() {
		return server;
	}

	/**
	 * Writes the text as a new file of the root and opens it; returns its URI. Each document gets a file name of its
	 * own, so diagnostics a closed document's last build publishes cannot be taken for a later document's.
	 */
	String open(String name, String text) throws IOException {
		Path file = root.resolve(versions.size() + "-" + name);
		Files.writeString(file, text, StandardCharsets.UTF_8);
		String uri = file.toUri().toString();
		versions.put(uri, 1);
		texts.put(uri, text);
		client.expect(uri);
		server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "vecdsl", 1, text)));
		return uri;
	}

	/** Replaces the whole text of the open document. */
	void change(String uri, String text) {
		int version = versions.merge(uri, 1, Integer::sum);
		texts.put(uri, text);
		client.expect(uri);
		server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, version), List.of(new TextDocumentContentChangeEvent(text))));
	}

	void close(String uri) {
		server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(uri)));
	}

	String text(String uri) {
		return texts.get(uri);
	}

	/** The first diagnostics published for the document after its last didOpen or didChange. */
	List<Diagnostic> diagnostics(String uri) throws InterruptedException {
		return client.published(uri);
	}

	<T> T get(CompletableFuture<T> future) throws Exception {
		return future.get(TIMEOUT, TimeUnit.SECONDS);
	}

	/** shutdown, then exit; the server's exit status (the process's, or what the in-process handler received). */
	int shutdownAndExit() throws Exception {
		exited = true;
		get(server.shutdown());
		server.exit();
		if (process != null) {
			if (!process.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				throw new IllegalStateException("the server did not exit within " + TIMEOUT + " s");
			}
			return process.exitValue();
		}
		return inProcessExit.get(TIMEOUT, TimeUnit.SECONDS);
	}

	/** Ends the session unless shutdownAndExit already did. */
	void end() throws Exception {
		if (!exited) {
			shutdownAndExit();
		}
	}

	/**
	 * Collects published diagnostics by file (the server may spell a file URI differently, a Windows drive letter for
	 * one); requests from the server get empty answers.
	 */
	static final class Client implements LanguageClient {

		private final Map<Path, List<Diagnostic>> published = new HashMap<>();

		private static Path file(String uri) {
			return Path.of(URI.create(uri));
		}

		synchronized void expect(String uri) {
			published.remove(file(uri));
		}

		synchronized List<Diagnostic> published(String uri) throws InterruptedException {
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT);
			while (!published.containsKey(file(uri))) {
				long left = deadline - System.nanoTime();
				if (left <= 0) {
					throw new IllegalStateException("no diagnostics for " + uri + " within " + TIMEOUT + " s");
				}
				TimeUnit.NANOSECONDS.timedWait(this, left);
			}
			return published.get(file(uri));
		}

		@Override
		public synchronized void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
			published.putIfAbsent(file(diagnostics.getUri()), diagnostics.getDiagnostics());
			notifyAll();
		}

		@Override
		public void telemetryEvent(Object object) {
		}

		@Override
		public void showMessage(MessageParams message) {
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams request) {
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void logMessage(MessageParams message) {
		}
	}
}
