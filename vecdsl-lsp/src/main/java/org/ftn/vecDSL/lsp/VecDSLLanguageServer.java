package org.ftn.vecDSL.lsp;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.xtext.ide.server.LanguageServerImpl;

/**
 * Xtext's LanguageServerImpl, started also when the client names no root. Xtext 2.42.0 reads
 * the workspace folders of {@code initialize} only from a client that announces the workspace folders capability;
 * otherwise it takes {@code rootUri}, then {@code rootPath}, and with neither its WorkspaceManager fails on a null
 * base folder. Without either root the server takes the workspace folders path instead, with the folders the client
 * sent, usually none: a workspace of Xtext's unknown project alone, which has no source folder, so nothing is scanned
 * and the documents the client opens are served from there.
 */
public class VecDSLLanguageServer extends LanguageServerImpl {

	@Override
	protected boolean clientSupportsWorkspaceFolders() {
		InitializeParams params = getLanguageServerAccess().getInitializeParams();
		return super.clientSupportsWorkspaceFolders() || params.getRootUri() == null && params.getRootPath() == null;
	}
}
