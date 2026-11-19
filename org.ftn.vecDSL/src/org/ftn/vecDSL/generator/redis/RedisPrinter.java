package org.ftn.vecDSL.generator.redis;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders a Redis program for one host language and its Redis client. */
public interface RedisPrinter {

	String VENDOR = "redis";
	String URL_VARIABLE = "VECDSL_REDIS_URL";
	String USER_VARIABLE = "VECDSL_REDIS_USER";
	String PASSWORD_VARIABLE = "VECDSL_REDIS_PASSWORD";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, RedisManifest manifest);

	/** The environment variable holding the password: the connection's env(...) reference, else VECDSL_REDIS_PASSWORD. */
	static String passwordVariable(Program.Connection connection) {
		return connection.password() instanceof Code.Env env ? env.variable() : PASSWORD_VARIABLE;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("Connection: " + URL_VARIABLE + " (a redis:// URL without credentials) overrides the default URL; the user is read from",
				USER_VARIABLE + " and the password from " + passwordVariable(connection) + ", never written into a generated file.");
	}
}
