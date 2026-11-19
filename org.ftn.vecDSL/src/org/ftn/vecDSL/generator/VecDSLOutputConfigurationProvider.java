package org.ftn.vecDSL.generator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;

/** One output folder per target vendor, configurable per project under Properties > VecDSL > Compiler. */
public class VecDSLOutputConfigurationProvider implements IOutputConfigurationProvider {

	public static final List<String> VENDORS = List.of("milvus", "pinecone", "weaviate", "qdrant", "chroma",
			"pgvector", "redis", "mongodb", "oracledb", "elasticsearch", "opensearch", "vespa", "lancedb", "duckdb");

	@Override
	public Set<OutputConfiguration> getOutputConfigurations() {
		Set<OutputConfiguration> configurations = new LinkedHashSet<>();
		configurations.add(configuration(IFileSystemAccess.DEFAULT_OUTPUT, "Output folder", "./src-gen"));
		for (String vendor : VENDORS) {
			configurations.add(configuration(vendor, "Generated client code for " + vendor, "./generated/" + vendor));
		}
		return configurations;
	}

	private static OutputConfiguration configuration(String name, String description, String directory) {
		OutputConfiguration configuration = new OutputConfiguration(name);
		configuration.setDescription(description);
		configuration.setOutputDirectory(directory);
		configuration.setOverrideExistingResources(true);
		configuration.setCreateOutputDirectory(true);
		configuration.setCleanUpDerivedResources(true);
		configuration.setSetDerivedProperty(true);
		configuration.setKeepLocalHistory(true);
		return configuration;
	}
}
