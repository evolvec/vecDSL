package org.ftn.vecDSL.generator.pgvector;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders a pgvector program for one host language and its PostgreSQL driver. */
public interface PgvectorPrinter {

	String VENDOR = "pgvector";
	String URL_VARIABLE = "VECDSL_PGVECTOR_URL";
	String PASSWORD_VARIABLE = "VECDSL_PGVECTOR_PASSWORD";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, PgvectorManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_PGVECTOR_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("Connection: " + URL_VARIABLE + " (a libpq URI without the password) overrides the default URI; the password is read",
				"from " + passwordVariable(connection) + " and never written into a generated file.");
	}
}
