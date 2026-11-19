package org.ftn.vecDSL.generator.pinecone;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders a Pinecone program for one host language and its Pinecone client. */
public interface PineconePrinter {

	String VENDOR = "pinecone";
	String URI_VARIABLE = "VECDSL_PINECONE_URI";
	String INDEX_HOST_VARIABLE = "VECDSL_PINECONE_INDEX_HOST";
	String API_KEY_VARIABLE = "VECDSL_PINECONE_API_KEY";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, PineconeManifest manifest);

	/** The environment variable holding the API key: the script's env(...) reference, else VECDSL_PINECONE_API_KEY. */
	static Code.Env apiKey(Program.Connection connection) {
		return connection.token() instanceof Code.Env env ? env : new Code.Env(API_KEY_VARIABLE);
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		return List.of("API key: " + apiKey(connection).variable() + ". Other endpoints: " + URI_VARIABLE + " (control plane),",
				INDEX_HOST_VARIABLE + " (data plane, for example the API mock of docker-compose.yml).");
	}
}
