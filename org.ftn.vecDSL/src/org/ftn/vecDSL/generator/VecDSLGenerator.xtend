package org.ftn.vecDSL.generator

import com.google.inject.Inject
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.ftn.vecDSL.generator.chroma.ChromaGenerator
import org.ftn.vecDSL.generator.duckdb.DuckdbGenerator
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchGenerator
import org.ftn.vecDSL.generator.lancedb.LancedbGenerator
import org.ftn.vecDSL.generator.milvus.MilvusGenerator
import org.ftn.vecDSL.generator.mongodb.MongodbGenerator
import org.ftn.vecDSL.generator.opensearch.OpenSearchGenerator
import org.ftn.vecDSL.generator.oracledb.OracledbGenerator
import org.ftn.vecDSL.generator.pgvector.PgvectorGenerator
import org.ftn.vecDSL.generator.pinecone.PineconeGenerator
import org.ftn.vecDSL.generator.qdrant.QdrantGenerator
import org.ftn.vecDSL.generator.redis.RedisGenerator
import org.ftn.vecDSL.generator.vespa.VespaGenerator
import org.ftn.vecDSL.generator.weaviate.WeaviateGenerator
import org.ftn.vecDSL.vecDSL.Script

/**
 * Runs every target generator selected for a script. The system property vecdsl.targets
 * (comma-separated vendor ids) narrows the selection; a script's 'targets [...]' narrows it further.
 */
class VecDSLGenerator extends AbstractGenerator {

	public static val TARGETS_PROPERTY = 'vecdsl.targets'

	@Inject MilvusGenerator milvus
	@Inject WeaviateGenerator weaviate
	@Inject PineconeGenerator pinecone
	@Inject QdrantGenerator qdrant
	@Inject ChromaGenerator chroma
	@Inject PgvectorGenerator pgvector
	@Inject ElasticsearchGenerator elasticsearch
	@Inject OpenSearchGenerator opensearch
	@Inject MongodbGenerator mongodb
	@Inject RedisGenerator redis
	@Inject LancedbGenerator lancedb
	@Inject VespaGenerator vespa
	@Inject OracledbGenerator oracledb
	@Inject DuckdbGenerator duckdb

	def List<TargetGenerator> generators() {
		#[milvus, weaviate, pinecone, qdrant, chroma, pgvector, elasticsearch, opensearch, mongodb, redis, lancedb, vespa, oracledb, duckdb]
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		for (script : resource.contents.filter(Script)) {
			for (generator : generators.filter[selected(script, vendor)]) {
				if (context.cancelIndicator.canceled) return;
				generator.generate(script, fsa)
			}
		}
	}

	def boolean selected(Script script, String vendor) {
		val requested = System.getProperty(TARGETS_PROPERTY)
		val allowed = requested === null || requested.blank || requested.split(',').exists[trim == vendor]
		allowed && (script.targets.empty || script.targets.contains(vendor))
	}
}
