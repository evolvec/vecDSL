package org.ftn.vecDSL.tests;

import static org.ftn.vecDSL.validation.VecDSLValidator.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/** Validator tests for WF-Recall, WF-CreateTop, WF-Upd, WF-Del and the premises of the typing judgments. */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSLValidatorTest {

	private static final String DOCS = "script t\ncreate collection docs { primary id field id type int field status type varchar"
			+ " vector-field type float_vector dim 3 }\n";
	private static final String VECTOR = "vector-search in docs top 3 query vector [0.1, 0.2, 0.3] ";
	private static final String TWO = "create collection two { primary id field id type int vector-field a type float_vector dim 3"
			+ " vector-field b type float_vector dim 3 }\n";
	private static final String COSINE = "create collection m { primary id field id type int vector-field type float_vector dim 3"
			+ " metric cosine }\n";
	private static final String M = "vector-search in m top 3 query vector [0.1, 0.2, 0.3] ";
	private static final String LEGS = "{ vector { query vector [0.1, 0.2, 0.3] } vector { query vector [0.3, 0.2, 0.1] } } output *";
	private static final VecDSLPackage P = VecDSLPackage.eINSTANCE;

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private ValidationTestHelper validation;

	private Script parse(String statements) throws Exception {
		return parseHelper.parse(DOCS + statements);
	}

	private void error(String statements, EClass type, String code) throws Exception {
		validation.assertError(parse(statements), type, code);
	}

	private void valid(String statements) throws Exception {
		validation.assertNoErrors(parse(statements));
	}

	// WF-Recall (a)

	@Test
	public void recallInsideTheOpenInterval() throws Exception {
		valid(VECTOR + "params { target-recall 0.9 } output *");
	}

	@Test
	public void recallZero() throws Exception {
		error(VECTOR + "params { target-recall 0 } output *", P.getSearchParams(), RECALL_ZERO);
	}

	@Test
	public void recallOne() throws Exception {
		error(VECTOR + "params { target-recall 1 } output *", P.getSearchParams(), RECALL_ONE);
	}

	@Test
	public void recallOnePointZero() throws Exception {
		error(VECTOR + "params { target-recall 1.0 } output *", P.getSearchParams(), RECALL_ONE);
	}

	@Test
	public void recallAboveOne() throws Exception {
		error(VECTOR + "params { target-recall 1.5 } output *", P.getSearchParams(), RECALL_ABOVE_ONE);
	}

	@Test
	public void recallNegative() throws Exception {
		error(VECTOR + "params { target-recall -0.5 } output *", P.getSearchParams(), RECALL_NEGATIVE);
	}

	// WF-Recall (b)

	@Test
	public void recallWithExact() throws Exception {
		error(VECTOR + "exact params { target-recall 0.9 } output *", P.getSearchParams(), RECALL_WITH_EXACT);
	}

	@Test
	public void recallWithExactOnVectorLeg() throws Exception {
		error("hybrid-search in docs top 3 { vector { query vector [0.1, 0.2, 0.3] params { target-recall 0.9 } exact } } output *",
				P.getSearchParams(), RECALL_WITH_EXACT);
	}

	@Test
	public void recallWithExhaustive() throws Exception {
		error(VECTOR + "params { target-recall 0.9 exhaustive } output *", P.getSearchParams(), RECALL_WITH_EXHAUSTIVE);
	}

	@Test
	public void recallWithCandidates() throws Exception {
		error(VECTOR + "params { target-recall 0.9 candidates 64 } output *", P.getSearchParams(), RECALL_WITH_CANDIDATES);
	}

	@Test
	public void recallWithProbes() throws Exception {
		error(VECTOR + "params { target-recall 0.9 probes 16 } output *", P.getSearchParams(), RECALL_WITH_PROBES);
	}

	@Test
	public void recallWithScanBudget() throws Exception {
		error(VECTOR + "params { target-recall 0.9 scan-budget 10000 } output *", P.getSearchParams(), RECALL_WITH_SCAN_BUDGET);
	}

	@Test
	public void recallWithIndependentSettings() throws Exception {
		valid(VECTOR + "params { target-recall 0.9 timeout 200 oversampling 2.0 rescore filter-strategy iterative } output *");
	}

	// WF-Recall (c), through the admissibility table

	@Test
	public void recallOnKeywordSearch() throws Exception {
		error("search in docs top 3 query text \"rag\" params { target-recall 0.9 } output *", P.getSearchParams(), PARAM_NOT_ADMITTED);
	}

	@Test
	public void recallAdmittedOnRecommendLegAndPrefetch() throws Exception {
		valid("recommend in docs top 3 positive [1] params { target-recall 0.9 } output *\n"
				+ "hybrid-search in docs top 3 { vector { query vector [0.1, 0.2, 0.3] params { target-recall 0.9 } } } output *\n"
				+ VECTOR + "prefetch { stage limit 10 params { target-recall 0.9 } } output *");
	}

	// WF-CreateTop

	@Test
	public void topLevelCreateOfAdmittedConcept() throws Exception {
		valid("create embedding-model e5 provider \"huggingface\" model-id \"intfloat/e5\" dim 3");
	}

	@Test
	public void topLevelCreateOfMemberConcept() throws Exception {
		error("create vector-index vi on ^vector family hnsw", P.getCreate(), CREATE_NOT_TOP_LEVEL);
	}

	@Test
	public void nestedCreateOfMember() throws Exception {
		valid("create docs with { create partition p1 }");
	}

	@Test
	public void nestedCreateOfNonMember() throws Exception {
		error("create database prod { connection c host \"h\" } with { create quantization type scalar }", P.getCreate(), CREATE_NOT_MEMBER);
	}

	// WF-Upd

	@Test
	public void updateWithSetAndSelector() throws Exception {
		valid("update docs set { status: \"ok\" } ids [1]");
	}

	@Test
	public void updateWithSourceAndSet() throws Exception {
		error("update docs from \"rows.json\" set { status: \"ok\" } ids [1]", P.getUpdate(), UPDATE_SOURCE_AND_SET);
	}

	@Test
	public void updateWithoutEffect() throws Exception {
		error("update docs ids [1]", P.getUpdate(), UPDATE_NO_EFFECT);
	}

	@Test
	public void updateWithoutSelector() throws Exception {
		error("update docs set { status: \"ok\" }", P.getUpdate(), UPDATE_NO_SELECTOR);
	}

	// WF-Del

	@Test
	public void deleteWithOneSelector() throws Exception {
		valid("delete from docs ids [1]\ndelete from docs all\ndelete from docs where status eq \"x\"");
	}

	@Test
	public void deleteWithoutSelector() throws Exception {
		error("delete from docs", P.getDelete(), DELETE_NO_SELECTOR);
	}

	@Test
	public void deleteWithIdsAndAll() throws Exception {
		error("delete from docs ids [1] all", P.getDelete(), DELETE_SEVERAL_SELECTORS);
	}

	@Test
	public void deleteWithIdsAndWhere() throws Exception {
		error("delete from docs ids [1] where status eq \"x\"", P.getDelete(), DELETE_SEVERAL_SELECTORS);
	}

	@Test
	public void deleteWithAllAndWhere() throws Exception {
		error("delete from docs all where status eq \"x\"", P.getDelete(), DELETE_SEVERAL_SELECTORS);
	}

	// T-Collection

	@Test
	public void primaryKeyOnVectorField() throws Exception {
		error("create collection c { primary v field id type int vector-field v type float_vector dim 3 }",
				P.getVectorCollection(), COLLECTION_PRIMARY_KEY);
	}

	@Test
	public void primaryKeyOfFloatSort() throws Exception {
		error("create collection c { primary p field p type float vector-field type float_vector dim 3 }",
				P.getVectorCollection(), COLLECTION_PRIMARY_KEY);
	}

	@Test
	public void duplicateFieldLabel() throws Exception {
		error("create collection c { primary id field id type int field tag type varchar field tag type text"
				+ " vector-field type float_vector dim 3 }", P.getPayloadField(), COLLECTION_DUPLICATE_FIELD);
	}

	// T-Record, T-Ids

	@Test
	public void recordWithFittingValues() throws Exception {
		valid("upsert into docs values [ { id: 1, status: \"ok\", ^vector: [0.1, 0.2, 0.3] } ]");
	}

	@Test
	public void recordValueOfWrongSort() throws Exception {
		error("upsert into docs values [ { id: 1, status: 5, ^vector: [0.1, 0.2, 0.3] } ]", P.getRecordEntry(), RECORD_TYPE);
	}

	@Test
	public void recordVectorOfWrongLength() throws Exception {
		error("upsert into docs values [ { id: 1, ^vector: [0.1, 0.2] } ]", P.getRecordEntry(), RECORD_DIMENSION);
	}

	@Test
	public void updateIdOfWrongSort() throws Exception {
		error("update docs set { status: \"ok\" } ids [\"a\"]", P.getUpdate(), IDS_TYPE);
	}

	@Test
	public void deleteIdOfWrongSort() throws Exception {
		error("delete from docs ids [\"a\"]", P.getDelete(), IDS_TYPE);
	}

	// T-Cmp

	@Test
	public void comparableComparisons() throws Exception {
		valid("delete from docs where id gt 1\ndelete from docs where status in [\"a\", \"b\"]\n"
				+ "delete from docs where status prefix \"a\"");
	}

	@Test
	public void orderingOnVarchar() throws Exception {
		error("delete from docs where status gt 5", P.getPredicate(), CMP_INCOMPARABLE);
	}

	@Test
	public void equalityOfIntWithString() throws Exception {
		error("delete from docs where id eq \"a\"", P.getPredicate(), CMP_INCOMPARABLE);
	}

	// T-Input-Vec, vf, T-Metric

	@Test
	public void queryVectorOfWrongLength() throws Exception {
		error("vector-search in docs top 3 query vector [0.1, 0.2] output *", P.getQueryInput(), INPUT_DIMENSION);
	}

	@Test
	public void queryOverSeveralVectorFieldsNamesOne() throws Exception {
		error(TWO + "vector-search in two top 3 query vector [0.1, 0.2, 0.3] output *", P.getVectorSearch(), VF_AMBIGUOUS);
		valid(TWO + "vector-search in two top 3 query vector [0.1, 0.2, 0.3] using a output *");
	}

	@Test
	public void searchMetricDiffersFromField() throws Exception {
		error(COSINE + M + "metric dot output *", P.getVectorSearch(), METRIC_MISMATCH);
		valid(COSINE + M + "metric cosine output *");
	}

	// T-Bounds

	@Test
	public void boundsInOrderAndRange() throws Exception {
		valid(COSINE + M + "bounds min 0.2 max 0.9 output *");
	}

	@Test
	public void boundsOutOfOrder() throws Exception {
		error(COSINE + M + "bounds min 0.9 max 0.5 output *", P.getScoreBounds(), BOUNDS_ORDER);
	}

	@Test
	public void boundsOutsideCosineRange() throws Exception {
		error(COSINE + M + "bounds min 1.5 output *", P.getScoreBounds(), BOUNDS_RANGE);
		valid(COSINE + M + "score native bounds min 1.5 output *");
	}

	// T-Hybrid

	@Test
	public void hybridLegWithoutInput() throws Exception {
		error("hybrid-search in docs top 3 { vector { using ^vector } } output *", P.getHybridSearch(), HYBRID_INPUT);
	}

	@Test
	public void alphaRequiresWeightedFusionOfTwoLegs() throws Exception {
		error("hybrid-search in docs top 3 fusion rrf alpha 0.5 " + LEGS, P.getHybridSearch(), HYBRID_ALPHA);
		valid("hybrid-search in docs top 3 fusion weighted alpha 0.5 " + LEGS);
	}

	@Test
	public void fusionKRequiresRrf() throws Exception {
		error("hybrid-search in docs top 3 fusion weighted fusion-k 60 " + LEGS, P.getHybridSearch(), HYBRID_FUSION_K);
		valid("hybrid-search in docs top 3 fusion rrf fusion-k 60 " + LEGS);
	}

	// n >= 1

	@Test
	public void topZero() throws Exception {
		error("vector-search in docs top 0 query vector [0.1, 0.2, 0.3] output *", P.getVectorSearch(), QUERY_TOP);
	}
}
