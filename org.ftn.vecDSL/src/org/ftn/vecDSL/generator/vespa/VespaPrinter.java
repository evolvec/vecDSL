package org.ftn.vecDSL.generator.vespa;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a Vespa program for one host language over the HTTP APIs of Vespa (deploy, document/v1 and query). */
public interface VespaPrinter {

	String VENDOR = "vespa";
	String URL_VARIABLE = "VECDSL_VESPA_URL";
	String CONFIG_VARIABLE = "VECDSL_VESPA_CONFIG_URL";
	String TOKEN_VARIABLE = "VECDSL_VESPA_TOKEN";
	/** Seconds a single request may take; deployments wait longer (see the runtimes). */
	double DEFAULT_TIMEOUT = 30.0;

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, VespaManifest manifest, String configUrl);

	/** Seconds per request: the connection's timeout, else the default. */
	static double timeout(Program.Connection connection) {
		return connection.timeoutSeconds() == null ? DEFAULT_TIMEOUT : connection.timeoutSeconds();
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints() {
		return List.of("Connection: " + URL_VARIABLE + " (query and document API) and " + CONFIG_VARIABLE
				+ " (config server, deploy API) override the default URLs;",
				"an optional bearer token is read from " + TOKEN_VARIABLE + " and never written into a generated file.");
	}
}
