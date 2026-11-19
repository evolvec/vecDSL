package org.ftn.vecDSL.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

/**
 * A client that sends neither rootUri nor workspace folders, nor the workspace folders capability,
 * with the expected values of LspClientTest. A class of its own, so that its server is the only one of the JVM
 * while it runs: two in-process servers share EMF's global registries, and the second one's setup takes the file
 * extension over from the first.
 */
public class RootlessClientTest {

	@Test
	public void initializeWithoutRootServesAnOpenedFile() throws Exception {
		System.out.println("RootlessClientTest: server " + Connection.mode());
		Connection connection = Connection.startWithoutRoot();
		try {
			String uri = connection.open("pinecone_demo.vecDSL", LspClientTest.sample("pinecone_demo.vecDSL"));
			LspClientTest.assertPublished(LspClientTest.PINECONE_DIAGNOSTICS, connection.diagnostics(uri));
			LspClientTest.assertHover(LspClientTest.range(35, 9, 22), LspClientTest.PINECONE_HOVER,
					connection.get(connection.server().getTextDocumentService()
							.hover(new HoverParams(LspClientTest.id(uri), new Position(35, 9)))));
			GenerateEndpoint.GenerateResult result = connection.get(connection.server().generate(
					new GenerateEndpoint.GenerateParams(uri, LspClientTest.range(35, 0, 0), "pinecone", "py")));
			connection.close(uri);
			assertEquals("similar", result.statement());
			assertEquals(List.of(), result.errors());
			assertEquals(2, result.sections().size(), result.sections().toString());
			assertTrue(result.files().stream().anyMatch(file -> file.path().equals(LspClientTest.PINECONE_PROGRAM)));
		} finally {
			assertEquals(0, connection.shutdownAndExit());
		}
	}
}
