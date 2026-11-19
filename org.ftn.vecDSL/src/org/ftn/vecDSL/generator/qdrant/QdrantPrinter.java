package org.ftn.vecDSL.generator.qdrant;

import java.util.List;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Program;

/** Renders a Qdrant program for one host language and its Qdrant client. */
public interface QdrantPrinter {

	String VENDOR = "qdrant";
	String URI_VARIABLE = "VECDSL_QDRANT_URI";
	String GRPC_PORT_VARIABLE = "VECDSL_QDRANT_GRPC_PORT";
	String API_KEY_VARIABLE = "VECDSL_QDRANT_API_KEY";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder; grpcPort is the server's gRPC port. */
	Map<String, String> files(Program program, QdrantManifest manifest, int grpcPort);

	/** The script's env(...) reference for the API key, or null when the key is the optional VECDSL_QDRANT_API_KEY. */
	static Code.Env requiredKey(Program.Connection connection) {
		return connection.token() instanceof Code.Env env ? env : null;
	}

	/** Header lines naming the environment variables a generated program reads. */
	static List<String> endpoints(Program.Connection connection) {
		Code.Env key = requiredKey(connection);
		String apiKey = key == null ? API_KEY_VARIABLE + " (optional; the local server of docker-compose.yml sets one)"
				: key.variable() + " (required)";
		return List.of("API key: " + apiKey + ". Endpoint: " + URI_VARIABLE + " (REST), " + GRPC_PORT_VARIABLE
				+ " (gRPC, Java).");
	}
}
