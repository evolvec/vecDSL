package org.ftn.vecDSL.generator;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.AbstractGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.ftn.vecDSL.generator.chroma.ChromaGenerator;
import org.ftn.vecDSL.generator.duckdb.DuckdbGenerator;
import org.ftn.vecDSL.generator.elasticsearch.ElasticsearchGenerator;
import org.ftn.vecDSL.generator.lancedb.LancedbGenerator;
import org.ftn.vecDSL.generator.milvus.MilvusGenerator;
import org.ftn.vecDSL.generator.mongodb.MongodbGenerator;
import org.ftn.vecDSL.generator.opensearch.OpenSearchGenerator;
import org.ftn.vecDSL.generator.oracledb.OracledbGenerator;
import org.ftn.vecDSL.generator.pgvector.PgvectorGenerator;
import org.ftn.vecDSL.generator.pinecone.PineconeGenerator;
import org.ftn.vecDSL.generator.qdrant.QdrantGenerator;
import org.ftn.vecDSL.generator.redis.RedisGenerator;
import org.ftn.vecDSL.generator.vespa.VespaGenerator;
import org.ftn.vecDSL.generator.weaviate.WeaviateGenerator;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * Runs every target generator selected for a script. The system property vecdsl.targets
 * (comma-separated vendor ids) narrows the selection; a script's 'targets [...]' narrows it further.
 */
@SuppressWarnings("all")
public class VecDSLGenerator extends AbstractGenerator {
  public static final String TARGETS_PROPERTY = "vecdsl.targets";

  @Inject
  private MilvusGenerator milvus;

  @Inject
  private WeaviateGenerator weaviate;

  @Inject
  private PineconeGenerator pinecone;

  @Inject
  private QdrantGenerator qdrant;

  @Inject
  private ChromaGenerator chroma;

  @Inject
  private PgvectorGenerator pgvector;

  @Inject
  private ElasticsearchGenerator elasticsearch;

  @Inject
  private OpenSearchGenerator opensearch;

  @Inject
  private MongodbGenerator mongodb;

  @Inject
  private RedisGenerator redis;

  @Inject
  private LancedbGenerator lancedb;

  @Inject
  private VespaGenerator vespa;

  @Inject
  private OracledbGenerator oracledb;

  @Inject
  private DuckdbGenerator duckdb;

  public List<TargetGenerator> generators() {
    return Collections.<TargetGenerator>unmodifiableList(CollectionLiterals.<TargetGenerator>newArrayList(this.milvus, this.weaviate, this.pinecone, this.qdrant, this.chroma, this.pgvector, this.elasticsearch, this.opensearch, this.mongodb, this.redis, this.lancedb, this.vespa, this.oracledb, this.duckdb));
  }

  @Override
  public void doGenerate(final Resource resource, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    Iterable<Script> _filter = Iterables.<Script>filter(resource.getContents(), Script.class);
    for (final Script script : _filter) {
      final Function1<TargetGenerator, Boolean> _function = (TargetGenerator it) -> {
        return Boolean.valueOf(this.selected(script, it.vendor()));
      };
      Iterable<TargetGenerator> _filter_1 = IterableExtensions.<TargetGenerator>filter(this.generators(), _function);
      for (final TargetGenerator generator : _filter_1) {
        {
          boolean _isCanceled = context.getCancelIndicator().isCanceled();
          if (_isCanceled) {
            return;
          }
          generator.generate(script, fsa);
        }
      }
    }
  }

  public boolean selected(final Script script, final String vendor) {
    boolean _xblockexpression = false;
    {
      final String requested = System.getProperty(VecDSLGenerator.TARGETS_PROPERTY);
      final boolean allowed = (((requested == null) || requested.isBlank()) || IterableExtensions.<String>exists(((Iterable<String>)Conversions.doWrapArray(requested.split(","))), ((Function1<String, Boolean>) (String it) -> {
        String _trim = it.trim();
        return Boolean.valueOf(Objects.equals(_trim, vendor));
      })));
      _xblockexpression = (allowed && (script.getTargets().isEmpty() || script.getTargets().contains(vendor)));
    }
    return _xblockexpression;
  }
}
