package org.ftn.vecDSL.generator.chroma;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a Chroma program for one host language and its Chroma client. */
public interface ChromaPrinter {

	String VENDOR = "chroma";
	String URI_VARIABLE = "VECDSL_CHROMA_URI";
	String DATABASE_VARIABLE = "VECDSL_CHROMA_DATABASE";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, ChromaManifest manifest);

	/** The database the program uses unless VECDSL_CHROMA_DATABASE names another: the connection's, else Chroma's default. */
	static String database(Program.Connection connection, ChromaManifest manifest) {
		return connection.database() == null ? manifest.get("database") : connection.database();
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints() {
		return List.of("Endpoint: " + URI_VARIABLE + "; database: " + DATABASE_VARIABLE + " (tenant default_tenant). The open-source",
				"Chroma server has no authentication; the program sends no key.");
	}
}
