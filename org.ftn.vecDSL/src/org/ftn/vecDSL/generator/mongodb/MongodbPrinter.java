package org.ftn.vecDSL.generator.mongodb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders a MongoDB program for one host language and the official MongoDB driver of that language. */
public interface MongodbPrinter {

	String VENDOR = "mongodb";
	String URL_VARIABLE = "VECDSL_MONGODB_URI";
	String USER_VARIABLE = "VECDSL_MONGODB_USER";
	String PASSWORD_VARIABLE = "VECDSL_MONGODB_PASSWORD";
	double TIMEOUT_SECONDS = 30;

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, MongodbManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_MONGODB_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/**
	 * Connection options after the URI, in the order every runtime's connect takes them: the user written in the script (else
	 * none), the variables holding the user and the password (never their values), and the timeout in seconds.
	 */
	static Map<String, Code.Expr> options(Program.Connection connection) {
		Map<String, Code.Expr> options = new LinkedHashMap<>();
		options.put("user", connection.user() == null ? new Code.Nil() : Code.text(connection.user()));
		options.put("user_variable", Code.text(USER_VARIABLE));
		options.put("password_variable", Code.text(passwordVariable(connection)));
		options.put("timeout_seconds", Code.num(connection.timeoutSeconds() == null ? TIMEOUT_SECONDS : connection.timeoutSeconds()));
		return options;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("Connection: " + URL_VARIABLE + " (a mongodb:// URI without credentials) overrides the default URI; the user is read from "
				+ (connection.user() == null ? USER_VARIABLE : USER_VARIABLE + " (default " + connection.user() + ")"),
				"and the password from " + passwordVariable(connection) + "; without a user the program connects without authentication."
						+ " No credential is written into a generated file.");
	}
}
