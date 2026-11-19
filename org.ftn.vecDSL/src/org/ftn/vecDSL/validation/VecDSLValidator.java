package org.ftn.vecDSL.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.Create;
import org.ftn.vecDSL.vecDSL.Delete;
import org.ftn.vecDSL.vecDSL.EComparisonOp;
import org.ftn.vecDSL.vecDSL.EFusionType;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EQueryInputMode;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EScoreForm;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.HybridLeg;
import org.ftn.vecDSL.vecDSL.HybridSearch;
import org.ftn.vecDSL.vecDSL.KeywordSearch;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.Predicate;
import org.ftn.vecDSL.vecDSL.QueryInput;
import org.ftn.vecDSL.vecDSL.QueryMethod;
import org.ftn.vecDSL.vecDSL.RecordEntry;
import org.ftn.vecDSL.vecDSL.ScoreBounds;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.SearchParams;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.Update;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VectorLeg;
import org.ftn.vecDSL.vecDSL.VectorSearch;

/**
 * Well-formedness rules of vecDSL 7.1 that the grammar does not express.
 * <ul>
 * <li>WF-Recall: range of 'target-recall', its exclusions within a search, and admissibility of every
 * SearchParams key per query form ({@link SearchParamsAdmissibility}).</li>
 * <li>WF-CreateTop: which concepts a top-level or nested create may define or reference.</li>
 * <li>WF-Upd and WF-Del: effect and selector of update and delete statements.</li>
 * <li>Premises of the typing judgments (static semantics, Section 5.2): T-Collection, T-Record (the record
 * premise of T-Update, applied to every record literal), T-Ids, T-Cmp, T-Input-Vec, vf, T-Metric, T-Bounds,
 * T-Hybrid and the n &ge; 1 premise of the query rules.</li>
 * </ul>
 */
public class VecDSLValidator extends AbstractVecDSLValidator {

	public static final String RECALL_ZERO = "wf.recall.zero";
	public static final String RECALL_NEGATIVE = "wf.recall.negative";
	public static final String RECALL_ONE = "wf.recall.one";
	public static final String RECALL_ABOVE_ONE = "wf.recall.aboveOne";
	public static final String RECALL_WITH_EXACT = "wf.recall.withExact";
	public static final String RECALL_WITH_EXHAUSTIVE = "wf.recall.withExhaustive";
	public static final String RECALL_WITH_CANDIDATES = "wf.recall.withCandidates";
	public static final String RECALL_WITH_PROBES = "wf.recall.withProbes";
	public static final String RECALL_WITH_SCAN_BUDGET = "wf.recall.withScanBudget";
	public static final String PARAM_NOT_ADMITTED = "wf.params.notAdmitted";
	public static final String CREATE_NOT_TOP_LEVEL = "wf.create.notTopLevel";
	public static final String CREATE_NOT_MEMBER = "wf.create.notMember";
	public static final String UPDATE_SOURCE_AND_SET = "wf.update.sourceAndSet";
	public static final String UPDATE_NO_EFFECT = "wf.update.noEffect";
	public static final String UPDATE_NO_SELECTOR = "wf.update.noSelector";
	public static final String DELETE_NO_SELECTOR = "wf.delete.noSelector";
	public static final String DELETE_SEVERAL_SELECTORS = "wf.delete.severalSelectors";
	public static final String COLLECTION_PRIMARY_KEY = "t.collection.primaryKey";
	public static final String COLLECTION_DUPLICATE_FIELD = "t.collection.duplicateField";
	public static final String RECORD_TYPE = "t.record.type";
	public static final String RECORD_DIMENSION = "t.record.dimension";
	public static final String IDS_TYPE = "t.ids.type";
	public static final String CMP_INCOMPARABLE = "t.cmp.incomparable";
	public static final String INPUT_DIMENSION = "t.input.dimension";
	public static final String VF_AMBIGUOUS = "t.vf.ambiguous";
	public static final String METRIC_MISMATCH = "t.metric.mismatch";
	public static final String BOUNDS_ORDER = "t.bounds.order";
	public static final String BOUNDS_RANGE = "t.bounds.range";
	public static final String HYBRID_INPUT = "t.hybrid.input";
	public static final String HYBRID_ALPHA = "t.hybrid.alpha";
	public static final String HYBRID_FUSION_K = "t.hybrid.fusionK";
	public static final String QUERY_TOP = "t.query.top";

	private static final Set<EScalarDType> PRIMARY_KEY_SORTS = Set.of(EScalarDType.INT, EScalarDType.BIGINT,
			EScalarDType.UUID, EScalarDType.VARCHAR);
	private static final Set<EScalarDType> INTEGRAL = Set.of(EScalarDType.INT8, EScalarDType.INT, EScalarDType.BIGINT);
	private static final Set<EScalarDType> NUMERIC = Set.of(EScalarDType.INT8, EScalarDType.INT, EScalarDType.BIGINT,
			EScalarDType.FLOAT, EScalarDType.DOUBLE);
	private static final Set<EScalarDType> TEMPORAL = Set.of(EScalarDType.DATE, EScalarDType.DATETIME,
			EScalarDType.TIMESTAMPTZ);
	private static final Set<EScalarDType> STRINGLIKE = Set.of(EScalarDType.VARCHAR, EScalarDType.TEXT,
			EScalarDType.UUID, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ, EScalarDType.BYTES,
			EScalarDType.GEOMETRY, EScalarDType.IMAGE, EScalarDType.AUDIO, EScalarDType.VIDEO);

	/** Concepts a top-level create may define or reference (WF-CreateTop). */
	public static final Set<String> TOP_LEVEL_CONCEPTS = Set.of("VectorDatabase", "Database", "Cluster",
			"VectorCollection", "Connection", "EmbeddingModel", "GenerativeModel", "Reranker", "Analyzer",
			"FileResource", "AuthConfig", "BackupSchedule");

	// ------------------------------------------------------------------ WF-Recall

	@Check
	public void checkTargetRecall(SearchParams params) {
		if (!ModelUtil.written(params, "targetRecall")) {
			return;
		}
		EStructuralFeature recall = feature(params, "targetRecall");
		double q = params.getTargetRecall();
		if (q == 0) {
			error("target-recall must be greater than 0", recall, RECALL_ZERO);
		} else if (q < 0) {
			error("target-recall must be greater than 0, got " + q, recall, RECALL_NEGATIVE);
		} else if (q == 1) {
			error("target-recall must be less than 1; recall 1 is written 'exact'", recall, RECALL_ONE);
		} else if (q > 1) {
			error("target-recall must be less than 1, got " + q + "; recall 1 is written 'exact'", recall,
					RECALL_ABOVE_ONE);
		}
		EObject owner = params.eContainer();
		if (owner != null && owner.eClass().getEStructuralFeature("exact") != null && ModelUtil.written(owner, "exact")) {
			error("target-recall and 'exact' exclude each other: 'exact' already fixes recall at 1", recall,
					RECALL_WITH_EXACT);
		}
		exclude(params, "exhaustive", "'exhaustive' already fixes recall at 1", RECALL_WITH_EXHAUSTIVE);
		exclude(params, "candidatePool", "'candidates' fixes search effort that target-recall leaves to the engine",
				RECALL_WITH_CANDIDATES);
		exclude(params, "probes", "'probes' fixes search effort that target-recall leaves to the engine",
				RECALL_WITH_PROBES);
		exclude(params, "scanBudget", "'scan-budget' fixes search effort that target-recall leaves to the engine",
				RECALL_WITH_SCAN_BUDGET);
	}

	private void exclude(SearchParams params, String other, String reason, String code) {
		if (ModelUtil.written(params, other)) {
			error("target-recall and " + reason, feature(params, "targetRecall"), code);
		}
	}

	/** Every key written in a params block must be admitted by the owning query form. */
	@Check
	public void checkSearchParamsAdmitted(SearchParams params) {
		EObject owner = params.eContainer();
		if (owner == null) {
			return;
		}
		String form = owner.eClass().getName();
		Set<String> admitted = SearchParamsAdmissibility.admitted(form);
		for (String key : SearchParamsAdmissibility.written(params)) {
			if (!admitted.contains(key)) {
				error("'" + key + "' is not admitted in the params of " + form,
						feature(params, SearchParamsAdmissibility.KEYS.get(key)), PARAM_NOT_ADMITTED, key);
			}
		}
	}

	// ------------------------------------------------------------------ WF-CreateTop

	@Check
	public void checkCreateReachability(Create create) {
		boolean defines = create.getDefinition() != null;
		EObject concept = defines ? create.getDefinition() : create.getTarget();
		if (concept == null || concept.eIsProxy()) {
			return;
		}
		EStructuralFeature where = feature(create, defines ? "definition" : "target");
		String kind = concept.eClass().getName();
		if (TOP_LEVEL_CONCEPTS.contains(kind)) {
			return;
		}
		EObject container = create.eContainer();
		if (container instanceof Create outer) {
			EObject owner = outer.getDefinition() != null ? outer.getDefinition() : outer.getTarget();
			if (owner != null && !owner.eIsProxy() && isMember(concept, owner)) {
				return;
			}
			error("a nested create may define a top-level concept or a member of the enclosing "
					+ (owner == null ? "concept" : owner.eClass().getName()) + "; " + kind + " is neither", where,
					CREATE_NOT_MEMBER);
		} else if (container instanceof Script) {
			error("a top-level create may define or reference only " + String.join(", ", sorted(TOP_LEVEL_CONCEPTS))
					+ "; define the " + kind + " inside its owner or in a nested 'with { }' create", where,
					CREATE_NOT_TOP_LEVEL);
		}
	}

	private static boolean isMember(EObject concept, EObject owner) {
		return owner.eClass().getEAllContainments().stream()
				.anyMatch(reference -> reference.getEReferenceType().isSuperTypeOf(concept.eClass()));
	}

	// ------------------------------------------------------------------ WF-Upd, WF-Del

	@Check
	public void checkUpdate(Update update) {
		boolean source = update.getSource() != null;
		boolean set = update.getValues() != null;
		if (source && set) {
			error("an update takes its values either from a record source or from 'set', not both",
					feature(update, "values"), UPDATE_SOURCE_AND_SET);
		}
		boolean effect = source || set || !update.getRemovedFields().isEmpty() || !update.getAppendFields().isEmpty()
				|| !update.getArrayRemoveFields().isEmpty();
		if (!effect) {
			error("an update needs an effect: a record source, 'set', 'remove-fields', 'array-append' or 'array-remove'",
					feature(update, "collection"), UPDATE_NO_EFFECT);
		}
		if (update.getIds().isEmpty() && update.getFilter() == null) {
			error("an update needs a selector: 'ids' or 'where'", feature(update, "collection"), UPDATE_NO_SELECTOR);
		}
	}

	@Check
	public void checkDelete(Delete delete) {
		int selectors = (delete.getIds().isEmpty() ? 0 : 1) + (delete.isDeleteAll() ? 1 : 0)
				+ (delete.getFilter() == null ? 0 : 1);
		if (selectors == 0) {
			error("a delete needs exactly one selector: 'ids', 'all' or 'where'", feature(delete, "collection"),
					DELETE_NO_SELECTOR);
		} else if (selectors > 1) {
			error("a delete takes exactly one selector: 'ids', 'all' or 'where'", feature(delete, "collection"),
					DELETE_SEVERAL_SELECTORS);
		}
	}

	// ------------------------------------------------------------------ T-Collection

	@Check
	public void checkCollection(VectorCollection collection) {
		Field key = collection.getPrimaryKey();
		if (key != null && !key.eIsProxy()
				&& !(key instanceof PayloadField payload && PRIMARY_KEY_SORTS.contains(payload.getType()))) {
			error("the primary key must be a payload field of sort int, bigint, uuid or varchar",
					feature(collection, "primaryKey"), COLLECTION_PRIMARY_KEY);
		}
		Set<String> seen = new HashSet<>();
		for (Field field : collection.getFields()) {
			if (!seen.add(ModelUtil.fieldName(field))) {
				error("the field '" + ModelUtil.fieldName(field) + "' is declared twice in collection "
						+ collection.getName(), field, feature(field, "name"), COLLECTION_DUPLICATE_FIELD);
			}
		}
	}

	// ------------------------------------------------------------------ T-Record, T-Ids

	@Check
	public void checkRecordEntry(RecordEntry entry) {
		Field field = entry.getField();
		Literal value = entry.getValue();
		if (field == null || field.eIsProxy() || value == null) {
			return;
		}
		if (field instanceof VectorField vector) {
			String problem = vectorProblem(vector, value);
			if (problem != null) {
				error(problem, feature(entry, "value"), RECORD_DIMENSION);
			}
		} else if (field instanceof PayloadField payload && !accepts(payload.getType(), payload.getElementType(), value)) {
			error("a value of this form does not fit field '" + payload.getName() + "' of sort "
					+ payload.getType().getLiteral(), feature(entry, "value"), RECORD_TYPE);
		}
	}

	@Check
	public void checkUpdateIds(Update update) {
		checkIds(update, update.getIds());
	}

	@Check
	public void checkDeleteIds(Delete delete) {
		checkIds(delete, delete.getIds());
	}

	private void checkIds(EObject statement, List<Literal> ids) {
		VectorCollection collection = ModelUtil.targetCollection(statement);
		if (collection == null || !(collection.getPrimaryKey() instanceof PayloadField key)) {
			return;
		}
		for (int i = 0; i < ids.size(); i++) {
			if (!accepts(key.getType(), null, ids.get(i))) {
				error("the id does not have the sort of the primary key (" + key.getType().getLiteral() + ")",
						statement, feature(statement, "ids"), i, IDS_TYPE);
			}
		}
	}

	// ------------------------------------------------------------------ T-Cmp

	@Check
	public void checkComparison(Predicate predicate) {
		if (!ModelUtil.written(predicate, "op") || predicate.getValue() == null) {
			return;
		}
		Field field = ModelUtil.leaf(predicate.getTarget());
		if (field == null || field.eIsProxy()) {
			return;
		}
		if (!comparable(predicate.getOp(), field, predicate.getValue())) {
			error("'" + predicate.getOp().getLiteral() + "' cannot compare field '" + ModelUtil.fieldName(field)
					+ "' with this value", feature(predicate, "value"), CMP_INCOMPARABLE);
		}
	}

	private static boolean comparable(EComparisonOp op, Field field, Literal value) {
		if (!(field instanceof PayloadField payload)) {
			return false;
		}
		EScalarDType sort = payload.getType();
		EScalarDType element = payload.getElementType();
		return switch (op) {
		case EQ, NEQ -> accepts(sort, element, value);
		case GT, GTE, LT, LTE -> (NUMERIC.contains(sort) || TEMPORAL.contains(sort)) && accepts(sort, element, value);
		case IN, NOT_IN -> value instanceof ListLit list ? list.getItems().stream().allMatch(item -> accepts(sort, element, item))
				: opaque(value);
		case LIKE, PREFIX -> (sort == EScalarDType.VARCHAR || sort == EScalarDType.TEXT)
				&& (value instanceof StringLit || opaque(value));
		case CONTAINS -> sort == EScalarDType.ARRAY && (element == null || accepts(element, null, value));
		case CONTAINS_ANY, CONTAINS_ALL -> sort == EScalarDType.ARRAY && (value instanceof ListLit list
				? element == null || list.getItems().stream().allMatch(item -> accepts(element, null, item))
				: opaque(value));
		};
	}

	/** Whether a literal can denote a value of the sort (T-Num, T-List and the subsort order of Section 5.1). */
	private static boolean accepts(EScalarDType sort, EScalarDType element, Literal value) {
		if (opaque(value) || sort == EScalarDType.JSON) {
			return true;
		}
		if (value instanceof StringLit) {
			return STRINGLIKE.contains(sort);
		}
		if (value instanceof NumberLit number) {
			return INTEGRAL.contains(sort) ? number.getValue() == Math.rint(number.getValue()) : NUMERIC.contains(sort);
		}
		if (value instanceof BoolLit) {
			return sort == EScalarDType.BOOL;
		}
		if (value instanceof ListLit list) {
			return sort == EScalarDType.ARRAY
					&& (element == null || list.getItems().stream().allMatch(item -> accepts(element, null, item)));
		}
		return false;
	}

	/** Parameters, environment references and null are typed elsewhere or admitted by nullability. */
	private static boolean opaque(Literal value) {
		return !(value instanceof StringLit || value instanceof NumberLit || value instanceof BoolLit
				|| value instanceof ListLit || value instanceof SparseLit);
	}

	// ------------------------------------------------------------------ T-Input-Vec, vf, T-Metric

	@Check
	public void checkVectorSearch(VectorSearch search) {
		VectorField field = addressedField(search, search.getCollection(), search.getVectorField(), "vectorField");
		checkInput(search.getInput(), field);
		checkMetric(search, search.getMetric(), field, search.getCollection());
	}

	@Check
	public void checkVectorLeg(VectorLeg leg) {
		VectorCollection collection = ModelUtil.targetCollection(leg);
		VectorField field = addressedField(leg, collection, leg.getVectorField(), "vectorField");
		QueryInput input = leg.getInput() != null ? leg.getInput()
				: leg.eContainer() instanceof HybridSearch hybrid ? hybrid.getInput() : null;
		checkInput(input, field);
		checkMetric(leg, leg.getMetric(), field, collection);
	}

	/** vf(c, using): the named vector field, or the unique vector field of c when none is named. */
	private VectorField addressedField(EObject query, VectorCollection collection, VectorField named, String feature) {
		if (named != null) {
			return named.eIsProxy() ? null : named;
		}
		if (collection == null || collection.eIsProxy()) {
			return null;
		}
		List<VectorField> fields = ModelUtil.vectorFields(collection);
		if (fields.size() != 1) {
			error("collection " + collection.getName() + " has " + fields.size()
					+ " vector fields, so the query must name one with 'using'", feature(query, "collection") != null
							? feature(query, "collection") : feature(query, feature), VF_AMBIGUOUS);
			return null;
		}
		return fields.get(0);
	}

	private void checkInput(QueryInput input, VectorField field) {
		if (input == null || field == null || input.getKind() != EQueryInputMode.VECTOR) {
			return;
		}
		String problem = vectorProblem(field, input.getValue());
		if (problem != null) {
			error(problem, input, feature(input, "value"), INPUT_DIMENSION);
		}
	}

	/** T-List-Vec against the type of a dense vector field; null when the value fits or cannot be judged. */
	private static String vectorProblem(VectorField field, Literal value) {
		if (field.getType() == EVectorDType.SPARSE_FLOAT_VECTOR || field.isMultiVector() || opaque(value)) {
			return null;
		}
		if (!(value instanceof ListLit list) || !list.getItems().stream().allMatch(NumberLit.class::isInstance)) {
			return "a dense vector is written as a list of numbers";
		}
		int dimension = field.getDimension();
		if (dimension > 0 && list.getItems().size() != dimension) {
			return "the vector has " + list.getItems().size() + " elements, but field '" + ModelUtil.fieldName(field)
					+ "' declares dim " + dimension;
		}
		return null;
	}

	private void checkMetric(EObject query, EMetric metric, VectorField field, VectorCollection collection) {
		if (field == null || !ModelUtil.written(query, "metric")) {
			return;
		}
		EMetric declared = declaredMetric(field, collection);
		if (declared != null && declared != metric) {
			error("the search metric " + metric.getLiteral() + " differs from " + declared.getLiteral()
					+ ", declared for field '" + ModelUtil.fieldName(field) + "'", feature(query, "metric"), METRIC_MISMATCH);
		}
	}

	private static EMetric declaredMetric(VectorField field, VectorCollection collection) {
		if (ModelUtil.written(field, "metric")) {
			return field.getMetric();
		}
		VectorIndex index = collection == null ? field.getIndex() : ModelUtil.vectorIndex(collection, field);
		return index != null && ModelUtil.written(index, "metric") ? index.getMetric() : null;
	}

	// ------------------------------------------------------------------ T-Bounds

	@Check
	public void checkBounds(ScoreBounds bounds) {
		boolean min = ModelUtil.written(bounds, "min");
		boolean max = ModelUtil.written(bounds, "max");
		if (min && max && bounds.getMin() > bounds.getMax()) {
			error("min " + bounds.getMin() + " is greater than max " + bounds.getMax(), feature(bounds, "min"),
					BOUNDS_ORDER);
		}
		EObject owner = bounds.eContainer();
		if (owner == null || native_(owner)) {
			return;
		}
		double[] range = range(owner);
		if (range == null) {
			return;
		}
		if (min && (bounds.getMin() < range[0] || bounds.getMin() > range[1])) {
			error("min " + bounds.getMin() + " lies outside the canonical range [" + range[0] + ", " + range[1]
					+ "] of the metric in force", feature(bounds, "min"), BOUNDS_RANGE);
		}
		if (max && (bounds.getMax() < range[0] || bounds.getMax() > range[1])) {
			error("max " + bounds.getMax() + " lies outside the canonical range [" + range[0] + ", " + range[1]
					+ "] of the metric in force", feature(bounds, "max"), BOUNDS_RANGE);
		}
	}

	private static boolean native_(EObject owner) {
		EStructuralFeature form = owner.eClass().getEStructuralFeature("scoreForm");
		return form != null && owner.eGet(form) == EScoreForm.NATIVE && ModelUtil.written(owner, "scoreForm");
	}

	/** Canonical similarity range for the owner of the bounds, or null when it is unbounded or unknown. */
	private static double[] range(EObject owner) {
		if (owner instanceof KeywordSearch) {
			return new double[] { 0, Double.POSITIVE_INFINITY };
		}
		VectorCollection collection = ModelUtil.targetCollection(owner);
		VectorField field = null;
		EMetric metric = null;
		if (owner instanceof VectorSearch search) {
			field = search.getVectorField() != null ? search.getVectorField() : unique(collection);
			metric = ModelUtil.written(search, "metric") ? search.getMetric() : null;
		} else if (owner instanceof VectorLeg leg) {
			field = leg.getVectorField() != null ? leg.getVectorField() : unique(collection);
			metric = ModelUtil.written(leg, "metric") ? leg.getMetric() : null;
		} else {
			return null;
		}
		if (metric == null && field != null && !field.eIsProxy()) {
			metric = declaredMetric(field, collection);
		}
		if (metric == null) {
			return null;
		}
		return switch (metric) {
		case COSINE -> new double[] { -1, 1 };
		case EUCLIDEAN, MANHATTAN -> new double[] { Double.NEGATIVE_INFINITY, 0 };
		case HAMMING -> field != null && field.getDimension() > 0 ? new double[] { -field.getDimension(), 0 }
				: new double[] { Double.NEGATIVE_INFINITY, 0 };
		case JACCARD -> new double[] { 0, 1 };
		case DOT -> null;
		};
	}

	private static VectorField unique(VectorCollection collection) {
		if (collection == null) {
			return null;
		}
		List<VectorField> fields = ModelUtil.vectorFields(collection);
		return fields.size() == 1 ? fields.get(0) : null;
	}

	// ------------------------------------------------------------------ T-Hybrid

	@Check
	public void checkHybrid(HybridSearch hybrid) {
		if (hybrid.getInput() == null) {
			for (int i = 0; i < hybrid.getLegs().size(); i++) {
				HybridLeg leg = hybrid.getLegs().get(i);
				if (leg.getInput() == null) {
					error("the leg has no query input and the hybrid search writes no shared input", hybrid,
							feature(hybrid, "legs"), i, HYBRID_INPUT);
				}
			}
		}
		boolean fusion = ModelUtil.written(hybrid, "fusion");
		if (ModelUtil.written(hybrid, "alpha")
				&& (hybrid.getLegs().size() != 2 || !fusion || hybrid.getFusion() != EFusionType.WEIGHTED)) {
			error("alpha requires exactly two legs and 'fusion weighted'", feature(hybrid, "alpha"), HYBRID_ALPHA);
		}
		if (ModelUtil.written(hybrid, "fusionConstant") && (!fusion || hybrid.getFusion() != EFusionType.RRF)) {
			error("fusion-k requires 'fusion rrf'", feature(hybrid, "fusionConstant"), HYBRID_FUSION_K);
		}
	}

	// ------------------------------------------------------------------ n >= 1 of the query rules

	@Check
	public void checkTop(QueryMethod query) {
		EStructuralFeature top = query.eClass().getEStructuralFeature("topK");
		if (top != null && ModelUtil.written(query, "topK") && ((Integer) query.eGet(top)) < 1) {
			error("top must be at least 1", top, QUERY_TOP);
		}
	}

	private static EStructuralFeature feature(EObject object, String name) {
		return object.eClass().getEStructuralFeature(name);
	}

	private static java.util.List<String> sorted(Set<String> values) {
		return values.stream().sorted().toList();
	}
}
