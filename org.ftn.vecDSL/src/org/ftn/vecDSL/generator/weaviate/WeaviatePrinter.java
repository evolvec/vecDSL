package org.ftn.vecDSL.generator.weaviate;

import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a Weaviate program for one host language and its Weaviate SDK. */
public interface WeaviatePrinter {

	String VENDOR = "weaviate";
	String URI_VARIABLE = "VECDSL_WEAVIATE_URI";
	String GRPC_VARIABLE = "VECDSL_WEAVIATE_GRPC_PORT";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, WeaviateManifest manifest);
}
