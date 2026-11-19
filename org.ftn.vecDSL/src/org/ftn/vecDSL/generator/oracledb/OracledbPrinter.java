package org.ftn.vecDSL.generator.oracledb;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders an Oracle AI Database program for one host language and its Oracle driver. */
public interface OracledbPrinter {

	String VENDOR = "oracledb";
	String DSN_VARIABLE = "VECDSL_ORACLEDB_DSN";
	String USER_VARIABLE = "VECDSL_ORACLEDB_USER";
	String PASSWORD_VARIABLE = "VECDSL_ORACLEDB_PASSWORD";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, OracledbManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_ORACLEDB_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("Connection: " + DSN_VARIABLE + " (an Easy Connect string host:port/service) and " + USER_VARIABLE + " override the defaults;",
				"the password is read from " + passwordVariable(connection) + " and never written into a generated file.");
	}
}
