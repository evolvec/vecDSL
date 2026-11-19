package org.ftn.vecDSL.generator.duckdb;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a DuckDB program for one host language and its DuckDB client. */
public interface DuckdbPrinter {

	String VENDOR = "duckdb";
	String PATH_VARIABLE = "VECDSL_DUCKDB_PATH";
	String READ_ONLY_VARIABLE = "VECDSL_DUCKDB_READ_ONLY";
	String EXTENSIONS_VARIABLE = "VECDSL_DUCKDB_EXTENSIONS";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, DuckdbManifest manifest);

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("Database: " + PATH_VARIABLE + " (a DuckDB database file, created on first use; :memory: for an in-memory database)",
				"overrides " + connection.uri() + ", which is resolved against the program's folder. " + READ_ONLY_VARIABLE
						+ "=true opens it read-only; " + EXTENSIONS_VARIABLE,
				"names a directory with the vss and fts extensions for offline use. Embedded: no server process and no key.");
	}
}
