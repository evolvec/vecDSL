package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.google.inject.Inject;

/** Parser tests for vecDSL v7. Generated from tests/cases.json. Only syntax errors are
 *  asserted; linking errors are expected because the snippets reference undeclared names. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLParsingTest {

    @Inject private ParseHelper<Script> parseHelper;

    private List<Resource.Diagnostic> syntaxErrors(String src) throws Exception {
        Script s = parseHelper.parse(src);
        assertNotNull(s);
        return s.eResource().getErrors().stream()
            .filter(d -> d instanceof XtextSyntaxDiagnostic).collect(Collectors.toList());
    }
    private void parses(String src) throws Exception {
        List<Resource.Diagnostic> e = syntaxErrors(src);
        assertTrue(e.isEmpty(), () -> "unexpected syntax errors: " + e);
    }
    private void rejects(String src) throws Exception {
        assertFalse(syntaxErrors(src).isEmpty(), "expected a syntax error");
    }

    @Test public void kitchen_sink() throws Exception { // coverage
        parses("// Full-coverage program: every grammar rule is exercised at least once.\nscript ragPipeline\ntargets [ qdrant, milvus, pgvector, elasticsearch, lancedb, oracledb ]\nversion \"7.0\"\nlanguage python\ndescription \"coverage program\"\ncreated \"2026-11-05\"\nmodified \"2026-11-05\"\nhint retry_policy = \"exponential\"\nprovenance generator \"vecdsl-gen\" at \"2026-11-05T10:00:00Z\" template \"t-7.0\" model-hash \"abc\"\nparam qvec : vector\nparam qtext : text = \"vector databases\"\nparam rows : records\n\ncreateDb: create database prod {\n    connection main uri \"https://example.invalid:6333\" grpc-port 6334\n        api-key env(\"VDB_API_KEY\") password env(\"VDB_PW\") tls cloud \"aws\" region \"eu-central-1\"\n        database \"default\" deployment serverless timeout 3000\n        extension pinecone { environment = \"legacy\" }\n    cluster c1 {\n        extension milvus { resource_group = \"rg1\" }\n        collection inner1 { primary id field id type varchar max-length 64 }\n        cluster-limit cl1 max_collections = 100\n        replication factor 2 extension weaviate { async_replication = true }\n        sharding shards 4 strategy hash\n    }\n    logical-db analytics { collection inner2 { primary id field id type int } }\n    auth a1 type api_key token env(\"TOKEN\") certificate \"/etc/ca.pem\" cmek \"key-1\" audit-log field-encryption {\n        role reader \"read-only\" privileges [\"read\"] collection docs\n    }\n    metrics m1 enabled endpoint \"http://prom:9090\" latency recall\n}\n\ncreateDocs: create collection docs in c1 dynamic auto-id id-type string ttl 86400 ttl-field expiresAt\n    consistency bounded embedding-model e5 generative-model llm tags [\"rag\", \"prod\"] deletion-protection\n    derived-from \"s3://lake/docs\" format iceberg {\n    primary id\n    default-partition p0\n    extension qdrant { strict_mode = true optimizers indexing-threshold 20000 }\n    analyzer std tokenizer standard language \"en\" stopwords \"english\" bm25-k1 1.2 bm25-b 0.75 resources [syn] {\n        filter lc type lowercase\n    }\n    field id type varchar max-length 64\n    field title type varchar max-length 512 full-text analyzer std description \"headline\"\n    field text type text document-body full-text\n    field status type varchar nullable default \"draft\"\n    field score type float\n    field tenantKey type varchar partition-key\n    field expiresAt type timestamptz\n    field place type geo_point\n    field meta type json { field author type varchar field year type int }\n    field chunks type array element-type struct nested { field body type text vector-field type float_vector dim 384 }\n    field tags type array element-type varchar field-limit tl array_len = 32\n    vector-field dense type float_vector dim 768 metric cosine normalized model e5 from [title, text] on-disk\n        description \"dense\" field-limit vl vector_dim = 768 {\n        vector-index hnsw1 family hnsw max-degree 16 build-candidates 200 search-candidates 64 on-disk online {\n            extension opensearch { engine = \"faiss\" }\n            quantization q1 type scalar bits 8 rescore oversampling 2.0 keep-originals truncate-dim 512\n        }\n        extension qdrant { \"memory\" = \"cached\" }\n    }\n    vector-field splade type sparse_float_vector modifier idf\n    vector-field colbert type float_vector dim 128 multivector comparator max_sim\n    vector-index ivf1 on colbert family ivf accelerator gpu centroids 1024 description \"ivf\"\n    payload-index pi1 on status kind keyword structure inverted on-disk lowercase prefix-match\n    payload-index pi2 on path \"meta.author\" kind json cast varchar { extension milvus { json_path_index = true } }\n    payload-index pi3 on title kind full_text analyzer std min-token-len 2 max-token-len 20\n    partition p0 strategy key key tenantKey default { partition p0a }\n    function bm25fn type bm25 input [text] output [splade] analyzer std description \"bm25\"\n    namespace ns1 key \"news\"\n    tenant acme description \"tenant acme\" extension weaviate { activity_status = \"active\" }\n    alias docsLive for docs\n    collection-limit lim1 max_fields = 64\n    generative-model llm provider \"openai\" model-id \"gpt-x\" temperature 0.2 max-tokens 512 top-p 0.9\n    storage type object_storage location \"s3://bucket/docs\" mmap tiered\n    stored-source [title, status]\n    cross-ref [inner1]\n} description \"documents\"\n\ncreateModels: create embedding-model e5 provider \"huggingface\" model-id \"intfloat/e5\" dim 768 built-in version \"2\"\n    deprecated replaced-by e5 path \"/models/e5.onnx\" chunk-strategy sentence chunk-size 512 chunk-overlap 64\n    description \"embedder\" extension vespa { embedder_id = \"e5\" }\n    with { create reranker rr provider \"cohere\" model \"rerank-v3\" top-n 20 field text }\ncreate file-resource syn type synonyms path \"/res/syn.txt\"\ncreate filter f2 type stemmer\ncreate quantization type binary bits 1.5\ncreate storage type disk\ncreate backup-schedule nightly for docs interval \"0 2 * * *\" retention 7 location \"s3://bk\"\ncreate cluster-limit x max_collections = 10\ncreate docs if-not-exists\n\nalt1: alter docs rename to docs2 {\n    add-field field summary type text\n    drop-field summary\n    rename-field status to state\n    set ttl = 3600\n    add-alias alias a2 for docs\n    add-function function emb type text_embedding input [title] output [dense] model e5\n    drop-function emb\n}\ndrop docsLive if-exists\n\nins: insert into docs from \"s3://in/batch.parquet\" records 1000 ttl 60 on-conflict ignore\n    namespace \"news\" tenant acme partition p0 consistency quorum wait extension qdrant { ordering = \"strong\" }\nups: upsert into docs values [ { id: \"a1\", title: \"t\", dense: [0.1, -0.2, 0.3] }, { id: \"a2\", splade: sparse { 12: 0.4, 907: 1.2 } } ]\n    batch 100 wait\nupd: update docs set { status: \"published\" } mode merge ids [\"a1\", 2] remove-fields [tags]\n    array-append [tags] array-remove [tags] if-version 3 consistency all where status eq \"draft\"\ndel: delete from docs limit 100 where (status in [\"x\", \"y\"] or not title is_null) and tags length gt 2\ndelAll: delete from docs all\nbulk: bulk-import into docs from \"kafka://broker/topic\" format json batch 500 parallel 4 continuous\n    on-conflict replace async namespace \"news\"\nfromParam: upsert into docs from-param $rows\ntx: transaction isolation serializable { insert into docs from \"f.json\" delete from docs ids [\"a\"] }\n\nload docs partitions [p0]\nrelease docs\nflush docs\ncompact docs target-segments 1 async extension milvus { clustering_key = \"tenantKey\" }\ndescribe docs stats index-progress\nmigrate-embeddings docs from-model e5 to-model e5 batch 256 strategy dual_write\nsnapshot docs scope tenant incremental tenant acme location \"s3://bk/s1\" description \"nightly\"\nrestore docs from \"s1\" scope collection location \"s3://bk\" async extension pinecone { region = \"us-east-1\" }\ndrop-snapshot \"s1\" location \"s3://bk\"\nclone docs to \"docs_branch\" mode shallow as-of \"2026-11-01T00:00:00Z\"\nclone database prod to \"prod_copy\" mode snapshot\nrebuild-index hnsw1 concurrently\nswap-alias docsLive from docs to docs2\nrefresh docs async\ntruncate docs namespace \"news\"\nlist collections in prod prefix \"doc\" limit 50\nawait-index hnsw1 until ready timeout 600\n\nvq: vector-search in docs top 10 query vector $qvec using dense metric cosine model e5 exact explain\n    within chunks collapse max collapse-k 3 (body contains_text \"rag\")\n    offset 0 cursor \"c1\" namespace \"news\" tenant acme partitions [p0] consistency strong as-of 1730000000\n    score similarity bounds min 0.5 max 1.0\n    mmr lambda 0.5 candidates 50 using dense\n    boost { boost-fn recency kind decay field expiresAt weight 2.0 decay gauss origin \"now\" scale \"7d\" offset \"1d\" rate 0.5\n            boost-fn kind condition weight 1.5 when status eq \"featured\"\n            combine sum apply multiply }\n    rerank rr\n    group-by meta.author size 3 with-vectors with-total exclude [tags] highlight [text]\n    sort { sort-by relevance desc sort-by meta.year asc }\n    aggregate { agg terms on status as byStatus size 10 agg date_histogram on expiresAt interval \"1d\" }\n    params { candidates 128 probes 16 scan-budget 20000 timeout 500 oversampling 1.5 rescore filter-strategy iterative exhaustive\n             extension pgvector { iterative_scan = \"relaxed_order\" } }\n    prefetch { stage s1 query sparse_vector sparse { 1: 0.5 } using splade weight 0.3 limit 100 metric dot\n               params { candidates 64 } prefetch { stage limit 50 } where status neq \"deleted\" }\n    output [id, title, score]\n    where geo place radius (45.25, 19.85) 10.5 km and any chunks (body match \"vector\")\n          and meta.year between 2020 to 2026 and status not_in [\"x\"] and title prefix \"A\"\n    extension vespa { rank ranker1 } extension qdrant { discover target \"a1\" { context \"a2\" vs \"a3\" } }\nks: search in docs top 20 query text $qtext text-query phrase fields [title boost 2.0, text] operator and\n    min-should-match 2 fuzziness \"AUTO\" slop 1 explain score native bounds min -1.5 output *\n    where geo place box (46.0, 19.0) to (45.0, 20.0) or geo place polygon [(1.0, 2.0), (3.0, 4.0), (5.0, -6.0)]\n          or geo place relation within wkt \"POLYGON((0 0,1 0,1 1,0 0))\" or path \"meta.x\" exists\nhs: hybrid-search in docs top 10 query text \"rag\" fusion rrf fusion-k 60 alpha 0.7 explain {\n        vector { query vector [0.1, 0.2] using dense metric cosine weight 0.7 limit 100 bounds min 0.2 params { candidates 64 } exact where status eq \"ok\" extension milvus { nprobe = 16 } }\n        vector { using splade }\n        keyword { query text \"rag\" text-query match fields [text] operator or min-should-match 1 weight 0.3 limit 100 where status eq \"ok\" extension elasticsearch { tie_breaker = 0.3 } }\n    } output [id]\nrec: recommend in docs top 5 positive [\"a1\", [0.1, 0.2]] negative [\"a9\"] lookup-in docs2 using dense output [id]\n    extension qdrant { \"strategy\" = \"best_score\" feedback strategy \"naive\" { example \"a1\" score 0.9 example \"a2\" score -0.5 } }\ngid: get from docs ids [\"a1\", \"a2\"] with-vectors namespace \"news\" output *\ngen: generate in docs top 5 query text \"summarize\" using dense model llm single-prompt \"Summarize {text}\"\n    grouped-task \"Compare\" grouped-fields [title, text] rerank rr output [title] where status eq \"published\"\ndm: distance-matrix in docs using dense sample 100 limit 10 where status eq \"ok\"\ncnt: count in docs exact tenant acme where status is_not_null\nscr: scroll in docs limit 100 cursor \"x\" slice 0 of 4 sort { sort-by id asc } with-vectors output [id]\nsmp: sample in docs size 50 method random output *\nbq: batch-query { vector-search in docs top 3 query id \"a1\" lookup-in docs output * count in docs }\nranking: alter docs { set description = \"with ranking\" }\ncreate collection vespaDocs {\n    primary id\n    extension vespa {\n        rank-profile ranker1 inherits ranker1 inputs { input q \"tensor<float>(x[768])\" }\n            functions { rank-function f1 expression \"closeness(field, dense)\" }\n            first-phase \"f1\" second-phase \"xgboost\" global-phase \"reciprocal_rank\" rerank-count 100 model m1\n            params { keep_rank_count = 1000 match_features = [\"f1\"] } description \"phased\"\n        ml-model m1 format onnx path \"models/m.onnx\" inputs { binding \"input_ids\" source \"query(q)\" } outputs { binding \"out\" source \"score\" }\n    }\n    field id type varchar\n}\ncreate collection wv { primary id extension weaviate { inverted-index index-timestamps index-null-state index-property-length bm25-k1 1.2 bm25-b 0.75 stopwords \"en\" } field id type uuid }\ncreate connection local path \"./data\" extension lancedb { read_consistency_interval = 5 }\ncreate connection mem in-memory\ncreate connection srv host \"localhost\" port 19530 username \"root\" password \"pw\" token \"t\" project \"p\"\ncreate tenant t2\ncreate namespace n2 key \"k\"\ncreate alias a3 for docs\ncreate partition p9 strategy range\ncreate replication r2 factor 3\ncreate sharding s2 shards 2 key tenantKey\ncreate role rr2 \"admin\"\ncreate auth a2 type none\ncreate metrics m2\ncreate generative-model g2\ncreate analyzer a9 tokenizer ngram\ncreate function f9 type rerank input [title] output [score]\ncreate vector-index vi9 on dense family vamana\ncreate payload-index pi9 on title kind keyword\ncreate collection-limit cl9 max_vector_dim = 4096\ncreate field-limit fl9 max_size = 10\ncreate logical-db l9 { }\ncreate cluster c9\ncreate vector-field v9 type binary_vector dim 1024 metric hamming\ncreate database d9 { connection c9c host \"h\" }\n");
    }
    @Test public void validid_title_pos() throws Exception { // T3
        parses("script t1 create collection docs { primary title field title type varchar max-length 200 }");
    }
    @Test public void validid_title_neg() throws Exception { // T3
        rejects("script t1 create field f type title");
    }
    @Test public void validid_score_pos() throws Exception { // T3
        parses("script t2 q: vector-search in docs top 5 query vector [0.1, 0.2] score native output [score] where score gt 0.5");
    }
    @Test public void validid_score_neg() throws Exception { // T3
        rejects("script t2 q: vector-search in docs top 5 query vector [0.1, 0.2] score score output *");
    }
    @Test public void validid_version_pos() throws Exception { // T3
        parses("script t3 version \"7.0\" version: count in docs where version eq 2");
    }
    @Test public void validid_version_neg() throws Exception { // T3
        rejects("script t3 version: \"7.0\"");
    }
    @Test public void validid_name_pos() throws Exception { // T3
        parses("script name create field name type varchar");
    }
    @Test public void validid_name_neg() throws Exception { // T3
        rejects("script t4 create field name type varchar name");
    }
    @Test public void validid_type_pos() throws Exception { // T3
        parses("script t5 create field type type varchar");
    }
    @Test public void validid_type_neg() throws Exception { // T3
        rejects("script t5 create field f type type");
    }
    @Test public void validid_text_pos() throws Exception { // T3
        parses("script t6 s: search in docs top 10 query text \"vector databases\" output [text]");
    }
    @Test public void validid_text_neg() throws Exception { // T3
        rejects("script t6 s: search in docs top 10 query text text output *");
    }
    @Test public void validid_id_pos() throws Exception { // T3
        parses("script t7 g: get from docs ids [\"a1\"] output [id, title]");
    }
    @Test public void validid_id_neg() throws Exception { // T3
        rejects("script t7 g: get from docs id [\"a1\"] output *");
    }
    @Test public void validid_status_pos() throws Exception { // T3
        parses("script t8 d: delete from docs where status eq \"archived\"");
    }
    @Test public void validid_status_neg() throws Exception { // T3
        rejects("script t8 d: delete from docs where status \"archived\"");
    }
    @Test public void validid_date_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary date field date type varchar }");
    }
    @Test public void validid_datetime_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary datetime field datetime type varchar }");
    }
    @Test public void validid_json_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary json field json type varchar }");
    }
    @Test public void validid_image_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary image field image type varchar }");
    }
    @Test public void validid_audio_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary audio field audio type varchar }");
    }
    @Test public void validid_video_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary video field video type varchar }");
    }
    @Test public void validid_key_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary key field key type varchar }");
    }
    @Test public void validid_source_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary source field source type varchar }");
    }
    @Test public void validid_region_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary region field region type varchar }");
    }
    @Test public void validid_tags_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary tags field tags type varchar }");
    }
    @Test public void validid_model_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary model field model type varchar }");
    }
    @Test public void validid_format_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary format field format type varchar }");
    }
    @Test public void validid_size_pos() throws Exception { // T3-retained
        parses("script r create collection c { primary size field size type varchar }");
    }
    @Test public void hard_keyword_unescaped() throws Exception { // T3
        rejects("script e create field top type int");
    }
    @Test public void hard_keyword_escaped() throws Exception { // T3
        parses("script e create field ^top type int");
    }
    @Test public void vendor_id_as_name_unescaped() throws Exception { // T3
        rejects("script e create collection redis { primary id field id type int }");
    }
    @Test public void vendor_id_as_name_escaped() throws Exception { // T3
        parses("script e create collection ^redis { primary id field id type int }");
    }
    @Test public void vf_unnamed() throws Exception {
        parses("script v create vector-field type float_vector dim 768");
    }
    @Test public void vf_named_type() throws Exception {
        parses("script v create vector-field type type float_vector dim 768");
    }
    @Test public void quant_unnamed() throws Exception {
        parses("script v create quantization type scalar bits 8");
    }
    @Test public void quant_named_type() throws Exception {
        parses("script v create quantization type type scalar");
    }
    @Test public void sort_relevance() throws Exception {
        parses("script s q: scroll in docs sort { sort-by relevance desc } output *");
    }
    @Test public void sort_field_score() throws Exception {
        parses("script s q: scroll in docs sort { sort-by score desc } output *");
    }
    @Test public void ext_keyword_key_unquoted() throws Exception { // T2
        rejects("script x c: count in docs extension qdrant { memory = \"cached\" }");
    }
    @Test public void ext_keyword_key_quoted() throws Exception { // T2
        parses("script x c: count in docs extension qdrant { \"memory\" = \"cached\" }");
    }
    @Test public void ext_dotted_key() throws Exception { // T2
        parses("script x c: count in docs extension elasticsearch { index.knn.algo_param = 1 }");
    }
    @Test public void ext_unknown_vendor() throws Exception { // T2
        rejects("script x c: count in docs extension faiss { a = 1 }");
    }
    @Test public void ext_container_after_member() throws Exception { // T2
        rejects("script x create collection c { primary id field id type int extension qdrant { a = 1 } }");
    }
    @Test public void ext_container_before_member() throws Exception { // T2
        parses("script x create collection c { primary id extension qdrant { a = 1 } field id type int }");
    }
    @Test public void targets_ok() throws Exception {
        parses("script s targets [milvus, oracledb, lancedb] count in docs");
    }
    @Test public void targets_unknown() throws Exception {
        rejects("script s targets [milvus, faiss] count in docs");
    }
    @Test public void v6_script_vendor() throws Exception {
        rejects("script milvus 1");
    }
    @Test public void v6_access() throws Exception {
        rejects("script s access native_sdk");
    }
    @Test public void v6_index_type() throws Exception {
        rejects("script s create vector-index i type hnsw metric cosine");
    }
    @Test public void v6_named_vector() throws Exception {
        rejects("script s create named-vector v dim 3 metric cosine");
    }
    @Test public void v6_vector_name() throws Exception {
        rejects("script s q: vector-search in c top 1 query vector [1] vector-name \"a\" output *");
    }
    @Test public void v6_core_rank_profile() throws Exception {
        rejects("script s create rank-profile r first-phase \"x\"");
    }
    @Test public void v6_query_rank() throws Exception {
        rejects("script s q: vector-search in c top 1 query vector [1] rank r output *");
    }
    @Test public void v6_fork() throws Exception {
        rejects("script s fork f1 docs to \"x\"");
    }
    @Test public void v6_branch() throws Exception {
        rejects("script s branch docs to \"x\"");
    }
    @Test public void v6_optimizers_core() throws Exception {
        rejects("script s create optimizers o indexing-threshold 1");
    }
    @Test public void v6_inverted_index_core() throws Exception {
        rejects("script s create inverted-index ii");
    }
    @Test public void v6_scalar_index() throws Exception {
        rejects("script s create scalar-index s type keyword");
    }
    @Test public void v6_storage_layout() throws Exception {
        rejects("script s create vector-index i family hnsw storage-layout \"x\"");
    }
    @Test public void v6_vectordb_keyword() throws Exception {
        rejects("script s create database d { connection c host \"h\" vectordb c1 }");
    }
    @Test public void v6_provider_enum() throws Exception {
        rejects("script s create embedding-model e provider openai");
    }
    @Test public void v6_pod_type() throws Exception {
        rejects("script s create collection c pod-type \"p1\" { primary id }");
    }
    @Test public void v6_score_threshold() throws Exception {
        rejects("script s q: vector-search in c top 1 query vector [1] score-threshold 0.5 output *");
    }
    @Test public void v6_metric_ip() throws Exception {
        rejects("script s create vector-field v type float_vector metric ip");
    }
    @Test public void v6_flag_true() throws Exception { // Rv
        rejects("script s i: insert into c from \"f\" wait true");
    }
    @Test public void v6_reindex_preview() throws Exception {
        rejects("script s alter c { reindex-field f }");
    }
    @Test public void m01() throws Exception { // migration
        parses("script ingest targets [milvus]");
    }
    @Test public void m02() throws Exception { // migration
        parses("script s q1: vector-search in docs top 10 query vector $qvec using dense output [title] where category eq \"news\"");
    }
    @Test public void m03() throws Exception { // migration
        parses("script s create vector-field dense type float_vector dim 768 metric cosine { vector-index h family hnsw max-degree 16 build-candidates 200 { quantization type scalar bits 8 } }");
    }
    @Test public void m04() throws Exception { // migration
        parses("script s create vector-index v1 family ivf metric euclidean centroids 1024 { quantization type product subvectors 16 }");
    }
    @Test public void m05() throws Exception { // migration
        parses("script s create vector-index g1 family ivf metric dot accelerator gpu");
    }
    @Test public void m06() throws Exception { // migration
        parses("script s create vector-index d1 family ivf metric cosine on-disk { extension elasticsearch { index_type = \"bbq_disk\" } quantization type binary }");
    }
    @Test public void m07() throws Exception { // migration
        parses("script s q: vector-search in docs top 10 query vector [0.1] bounds min 0.8 output *");
    }
    @Test public void m08() throws Exception { // migration
        parses("script s q: vector-search in docs top 10 query vector [0.1] bounds min 0.6 max 1.0 output *");
    }
    @Test public void m09() throws Exception { // migration
        parses("script s create collection docs { primary id extension vespa { rank-profile rp first-phase \"bm25(title)\" } field id type varchar }");
    }
    @Test public void m10() throws Exception { // migration
        parses("script s q: vector-search in docs top 10 query vector [0.1] output * extension vespa { rank rp }");
    }
    @Test public void m11() throws Exception { // migration
        parses("script s q: vector-search in docs top 10 query vector [0.1] boost { boost-fn b1 kind decay field published decay gauss origin \"now\" scale \"7d\" } output *");
    }
    @Test public void m12() throws Exception { // migration
        parses("script s f1: clone docs to \"docs_copy\" mode snapshot");
    }
    @Test public void m13() throws Exception { // migration
        parses("script s b1: clone docs to \"exp\" mode shallow");
    }
    @Test public void m14() throws Exception { // migration
        parses("script s create collection docs { primary id extension qdrant { optimizers indexing-threshold 20000 } field id type int }");
    }
    @Test public void m15() throws Exception { // migration
        parses("script s create collection docs { primary id extension weaviate { inverted-index index-timestamps bm25-k1 1.2 } field id type int }");
    }
    @Test public void m16() throws Exception { // migration
        parses("script s create collection docs { primary id field id type int field category type varchar payload-index s1 on category kind keyword }");
    }
    @Test public void m17() throws Exception { // migration
        parses("script s create vector-index v1 family hnsw");
    }
    @Test public void m18() throws Exception { // migration
        parses("script s create database prod { connection c host \"h\" port 1 cluster main cluster c2 }");
    }
    @Test public void m19() throws Exception { // migration
        parses("script s targets [oracledb] create embedding-model e provider \"openai\"");
    }
    @Test public void m20() throws Exception { // migration
        parses("script s create collection c { primary id extension qdrant { strict_mode = true } field id type int }");
    }
    @Test public void m21() throws Exception { // migration
        parses("script s create collection c { primary id extension pinecone { pod_type = \"p1.x1\" } field id type int }");
    }
    @Test public void m22() throws Exception { // migration
        parses("script s q: vector-search in c top 5 query vector [0.1] params { candidates 128 probes 16 scan-budget 20000 } output *");
    }
    @Test public void m23() throws Exception { // migration
        parses("script s d1: vector-search in docs top 5 query id \"t\" output [id] extension qdrant { discover { context \"a\" vs \"b\" } }");
    }
    @Test public void m24() throws Exception { // migration
        parses("script s m1: hybrid-search in docs top 5 fusion rrf { vector { query vector [0.1] using a } vector { query vector [0.2] using b } } output [id]");
    }
    @Test public void m25() throws Exception { // migration
        parses("script s i: insert into docs from \"f.json\" wait");
    }
    @Test public void m26() throws Exception { // migration
        parses("script s q: count in docs where status eq \"a\" and year gt 2020");
    }
    @Test public void m27() throws Exception { // migration
        parses("script s g1: get from docs ids [\"a\"] with-vectors output [id]");
    }
    @Test public void m28() throws Exception { // migration
        parses("script s create collection c { primary id field id type int tenant t1 extension weaviate { activity_status = \"active\" } }");
    }
    @Test public void m29() throws Exception { // migration
        parses("script s create vector-field sp type sparse_float_vector metric dot");
    }
    @Test public void m30() throws Exception { // migration
        parses("script s create vector-field v type float_vector dim 768 metric cosine normalized");
    }
    @Test public void m31() throws Exception { // migration
        parses("script s q: search in docs top 10 query text \"rag\" bounds min 1.0 output *");
    }
    @Test public void m32() throws Exception { // migration
        parses("script s q: vector-search in docs top 10 query vector [0.1] params { candidates 64 extension pinecone { scan_factor = 2.0 } } output *");
    }
    @Test public void m33() throws Exception { // migration
        parses("script s create collection c consistency eventual { primary id field id type int }");
    }
    @Test public void null_literal() throws Exception { // literals
        parses("script n u: update docs set { status: null } where id eq \"a\"");
    }
}