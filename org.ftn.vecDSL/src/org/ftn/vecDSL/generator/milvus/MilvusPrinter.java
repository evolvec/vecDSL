package org.ftn.vecDSL.generator.milvus;

import java.util.Map;

import org.ftn.vecDSL.generator.code.Program;

/** Renders a Milvus program for one host language and its Milvus SDK. */
public interface MilvusPrinter {

	String VENDOR = "milvus";
	String URI_VARIABLE = "VECDSL_MILVUS_URI";

	/** Folder name under the vendor output folder, for example python. */
	String language();

	/** Generated files by path relative to the language folder. */
	Map<String, String> files(Program program, MilvusManifest manifest);
}
