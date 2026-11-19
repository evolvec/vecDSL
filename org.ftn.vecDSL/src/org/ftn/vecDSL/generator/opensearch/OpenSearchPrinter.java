package org.ftn.vecDSL.generator.opensearch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders an OpenSearch program for one host language and the official OpenSearch client of that language. */
public interface OpenSearchPrinter {

	String VENDOR = "opensearch";
	String URL_VARIABLE = "VECDSL_OPENSEARCH_URL";
	String PASSWORD_VARIABLE = "VECDSL_OPENSEARCH_PASSWORD";
	String USER_VARIABLE = "VECDSL_OPENSEARCH_USER";
	String USER = "admin";
	double TIMEOUT_SECONDS = 30;

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, OpenSearchManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_OPENSEARCH_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/**
	 * Connection options after the URL, in the order every runtime's connect takes them: the user and the variable that
	 * overrides it, the variables holding the password or the API key (never their values), and the request timeout in seconds.
	 */
	static Map<String, Code.Expr> options(Program.Connection connection) {
		Map<String, Code.Expr> options = new LinkedHashMap<>();
		options.put("user", Code.text(connection.user() == null ? USER : connection.user()));
		options.put("user_variable", Code.text(USER_VARIABLE));
		options.put("password_variable", Code.text(passwordVariable(connection)));
		if (connection.token() instanceof Code.Env env) {
			options.put("api_key_variable", Code.text(env.variable()));
		}
		options.put("timeout_seconds", Code.num(connection.timeoutSeconds() == null ? TIMEOUT_SECONDS : connection.timeoutSeconds()));
		return options;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		String key = connection.token() instanceof Code.Env env ? "; the API key is read from " + env.variable() + " and sent instead of the password"
				: "";
		return List.of("Connection: " + URL_VARIABLE + " (the server URL without credentials) overrides the default URL, " + USER_VARIABLE
				+ " the user (default " + (connection.user() == null ? USER : connection.user()) + "); the password is read", "from "
				+ passwordVariable(connection) + key + ". No credential is written into a generated file.");
	}
}
