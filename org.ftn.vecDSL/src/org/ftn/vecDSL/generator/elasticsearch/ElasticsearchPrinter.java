package org.ftn.vecDSL.generator.elasticsearch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders an Elasticsearch program for one host language and the official Elasticsearch client of that language. */
public interface ElasticsearchPrinter {

	String VENDOR = "elasticsearch";
	String URL_VARIABLE = "VECDSL_ELASTICSEARCH_URL";
	String PASSWORD_VARIABLE = "VECDSL_ELASTICSEARCH_PASSWORD";
	String USER = "elastic";
	double TIMEOUT_SECONDS = 30;

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, ElasticsearchManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_ELASTICSEARCH_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/**
	 * Connection options after the URL, in the order every runtime's connect takes them: the user, the variables holding the
	 * password or the API key (never their values), and the request timeout in seconds.
	 */
	static Map<String, Code.Expr> options(Program.Connection connection) {
		Map<String, Code.Expr> options = new LinkedHashMap<>();
		options.put("user", Code.text(connection.user() == null ? USER : connection.user()));
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
		return List.of("Connection: " + URL_VARIABLE + " (the server URL without credentials) overrides the default URL; the password of user "
				+ (connection.user() == null ? USER : connection.user()) + " is read", "from " + passwordVariable(connection) + key
				+ ". No credential is written into a generated file.");
	}
}
