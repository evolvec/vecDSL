package org.ftn.vecDSL.generator.lancedb;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a LanceDB program for one host language and its LanceDB or Lance SDK. */
public interface LancedbPrinter {

	String VENDOR = "lancedb";
	String URI_VARIABLE = "VECDSL_LANCEDB_URI";

	/** Seconds between checks for commits of other processes; 0 checks before every read (read_consistency_interval). */
	double INTERVAL = 0;

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, LancedbManifest manifest);

	/** Header lines naming the environment variable a generated program reads. */
	static List<String> endpoints() {
		return List.of("Storage: " + URI_VARIABLE + " (a directory, created on first use, relative to the working directory;",
				"object-store URIs such as s3:// are accepted). Embedded: no server process and no key.");
	}
}
