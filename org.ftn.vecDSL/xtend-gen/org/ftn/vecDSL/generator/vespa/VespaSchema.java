package org.ftn.vecDSL.generator.vespa;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ExtParam;
import org.ftn.vecDSL.vecDSL.ExtensionItem;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.ModelDeployment;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.PhasedRankProfile;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.RankFunction;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.SetPropertyOp;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.TensorInput;
import org.ftn.vecDSL.vecDSL.Update;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;
import org.ftn.vecDSL.vecDSL.VendorExtension;

/**
 * The application package of the program as its statements leave it: one schema per collection (document type), evolved by the
 * alter statements before each statement, and the ordered set of document types the program has deployed. Every DDL statement
 * deploys the whole package as it stands after the statement.
 */
@SuppressWarnings("all")
public class VespaSchema {
  public static final String NO_EMBEDDER = "generated programs deploy no models: an embedder is a component in services.xml whose model files must be in the application package";

  public static final String NO_MODELS = ("; " + VespaSchema.NO_EMBEDDER);

  public static final String NO_DISK_SETTING = "on-disk; Vespa keeps attributes in memory unless they are paged, and the vecDSL on-disk flag has no Vespa mapping yet (attribute: paged)";

  private static final Map<EScalarDType, String> NUMBERS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "int"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "long"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "byte"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "float"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "double")));

  private static final List<EScalarDType> STRINGS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<String, String> CELLS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("float_vector", "float"), Pair.<String, String>of("bfloat16_vector", "bfloat16"), Pair.<String, String>of("float64_vector", "double"), Pair.<String, String>of("int8_vector", "int8")));

  private static final Map<String, String> METRICS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("cosine", "angular"), Pair.<String, String>of("euclidean", "euclidean"), Pair.<String, String>of("dot", "dotproduct"), Pair.<String, String>of("hamming", "hamming")));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> expiring = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> versioned = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, VespaPackage.Schema> evolved = CollectionLiterals.<VectorCollection, VespaPackage.Schema>newHashMap();

  private final Map<VectorCollection, Map<String, String>> renames = CollectionLiterals.<VectorCollection, Map<String, String>>newHashMap();

  private final Map<String, VespaPackage.Schema> deployed = CollectionLiterals.<String, VespaPackage.Schema>newLinkedHashMap();

  private boolean declaring = false;

  public VespaSchema(final List<String> notes, final Script script) {
    this.notes = notes;
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
    for (final EObject object : _iterable) {
      this.slices(object);
    }
  }

  /**
   * Hidden attributes a collection needs, decided from the whole script: tenant and namespace slices of its statements, an expiry
   * time when a statement gives it a ttl, a version counter when an update checks if-version.
   */
  private void slices(final EObject object) {
    final VectorCollection collection = ModelUtil.targetCollection(object);
    if ((collection == null)) {
      return;
    }
    final EStructuralFeature tenant = object.eClass().getEStructuralFeature("tenant");
    if (((tenant != null) && (object.eGet(tenant) != null))) {
      this.tenanted.add(collection);
    }
    final EStructuralFeature namespace = object.eClass().getEStructuralFeature("namespace");
    if ((((namespace != null) && Objects.equals(namespace.getEType().getName(), "EString")) && (object.eGet(namespace) != null))) {
      this.namespaced.add(collection);
    }
    boolean _written = ModelUtil.written(object, "ttlSeconds");
    if (_written) {
      this.expiring.add(collection);
    }
    if (((object instanceof SetPropertyOp) && Objects.equals(((SetPropertyOp) object).getProperty().getLiteral(), "ttl"))) {
      this.expiring.add(collection);
    }
    if (((object instanceof Update) && ModelUtil.written(object, "ifVersion"))) {
      this.versioned.add(collection);
    }
  }

  public VespaPackage.Schema schema(final VectorCollection c) {
    VespaPackage.Schema _elvis = null;
    VespaPackage.Schema _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      VespaPackage.Schema _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final VespaPackage.Schema schema) {
    if ((schema == null)) {
      this.evolved.remove(c);
      this.renames.remove(c);
    } else {
      this.evolved.put(c, schema);
    }
  }

  public void rename(final VectorCollection c, final String field, final String newName) {
    final Function<VectorCollection, Map<String, String>> _function = (VectorCollection it) -> {
      return CollectionLiterals.<String, String>newHashMap();
    };
    final Map<String, String> map = this.renames.computeIfAbsent(c, _function);
    Set<Map.Entry<String, String>> _entrySet = map.entrySet();
    for (final Map.Entry<String, String> entry : _entrySet) {
      String _value = entry.getValue();
      boolean _equals = Objects.equals(_value, field);
      if (_equals) {
        entry.setValue(newName);
      }
    }
    boolean _containsValue = map.containsValue(newName);
    boolean _not = (!_containsValue);
    if (_not) {
      map.put(field, newName);
    }
  }

  /**
   * The document field name of a declared field, after the renames of earlier alter statements.
   */
  public String name(final VectorCollection c, final Field field) {
    String _xblockexpression = null;
    {
      final String declared = ModelUtil.fieldName(field);
      String _elvis = null;
      Map<String, String> _get = this.renames.get(c);
      String _get_1 = null;
      if (_get!=null) {
        _get_1=_get.get(declared);
      }
      if (_get_1 != null) {
        _elvis = _get_1;
      } else {
        _elvis = declared;
      }
      _xblockexpression = _elvis;
    }
    return _xblockexpression;
  }

  public boolean isExpiring(final VectorCollection c) {
    return this.expiring.contains(c);
  }

  public boolean isVersioned(final VectorCollection c) {
    return this.versioned.contains(c);
  }

  /**
   * The document type of a collection: its name, after the logical database's name and an underscore.
   */
  public static String documentType(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      String _xifexpression = null;
      if ((database == null)) {
        _xifexpression = c.getName();
      } else {
        String _name = database.getName();
        String _plus = (_name + "_");
        String _name_1 = c.getName();
        _xifexpression = (_plus + _name_1);
      }
      final String type = _xifexpression;
      boolean _identifier = VespaPackage.identifier(type);
      boolean _not = (!_identifier);
      if (_not) {
        throw Unsupported.of((("the document type name " + type) + "; schema names are identifiers"));
      }
      _xblockexpression = type;
    }
    return _xblockexpression;
  }

  private VespaPackage.Schema declared(final VectorCollection c) {
    VespaPackage.Schema _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!VespaSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys documents by an integer, text or uuid field, which becomes the user part of the document id");
        throw Unsupported.of(_plus_1);
      }
      final List<VespaPackage.Field> fields = CollectionLiterals.<VespaPackage.Field>newArrayList();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        List<VespaPackage.Field> _fieldsOf = this.fieldsOf(c, field);
        Iterables.<VespaPackage.Field>addAll(fields, _fieldsOf);
      }
      final String keyName = ModelUtil.fieldName(key);
      final UnaryOperator<VespaPackage.Field> _function = (VespaPackage.Field f) -> {
        VespaPackage.Field _xifexpression_1 = null;
        String _name = f.name();
        boolean _equals = Objects.equals(_name, keyName);
        if (_equals) {
          _xifexpression_1 = f.searchable();
        } else {
          _xifexpression_1 = f;
        }
        return _xifexpression_1;
      };
      fields.replaceAll(_function);
      if (((!c.getTenants().isEmpty()) || this.tenanted.contains(c))) {
        VespaPackage.Field _hidden = this.hidden(VespaPackage.TENANT);
        fields.add(_hidden);
      }
      if (((!c.getNamespaces().isEmpty()) || this.namespaced.contains(c))) {
        VespaPackage.Field _hidden_1 = this.hidden(VespaPackage.NAMESPACE);
        fields.add(_hidden_1);
      }
      boolean _isEmpty = ModelUtil.partitions(c).isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        VespaPackage.Field _hidden_2 = this.hidden(VespaPackage.PARTITION);
        fields.add(_hidden_2);
      }
      boolean _contains = this.versioned.contains(c);
      if (_contains) {
        VespaPackage.Field _field = new VespaPackage.Field(VespaPackage.VERSION, "long", "NUMERIC", false, true, null, null, null);
        fields.add(_field);
      }
      final Map<String, Double> bm25 = CollectionLiterals.<String, Double>newLinkedHashMap();
      final Analyzer analyzer = this.analyzerOf(c);
      if (((analyzer != null) && ModelUtil.written(analyzer, "bm25K1"))) {
        bm25.put("k1", Double.valueOf(analyzer.getBm25K1()));
      }
      if (((analyzer != null) && ModelUtil.written(analyzer, "bm25B"))) {
        bm25.put("b", Double.valueOf(analyzer.getBm25B()));
      }
      String _documentType = VespaSchema.documentType(c);
      boolean _containsKey = VespaSchema.NUMBERS.containsKey(((PayloadField) key).getType());
      List<VespaPackage.Rank> _ranks = this.ranks(c);
      final VespaPackage.Schema result = new VespaPackage.Schema(_documentType, keyName, _containsKey, fields, bm25, false, _ranks);
      VespaPackage.Schema _xifexpression_1 = null;
      boolean _contains_1 = this.expiring.contains(c);
      if (_contains_1) {
        _xifexpression_1 = result.expiringNow();
      } else {
        _xifexpression_1 = result;
      }
      _xblockexpression = _xifexpression_1;
    }
    return _xblockexpression;
  }

  /**
   * Rank profiles declared in the collection's extension vespa blocks; other extension items are rejected by name.
   */
  private List<VespaPackage.Rank> ranks(final VectorCollection c) {
    List<VespaPackage.Rank> _xblockexpression = null;
    {
      final List<VespaPackage.Rank> result = CollectionLiterals.<VespaPackage.Rank>newArrayList();
      final Function1<VendorExtension, Boolean> _function = (VendorExtension it) -> {
        String _vendor = it.getVendor();
        return Boolean.valueOf(Objects.equals(_vendor, VespaGenerator.VENDOR));
      };
      Iterable<VendorExtension> _filter = IterableExtensions.<VendorExtension>filter(c.getExtensions(), _function);
      for (final VendorExtension block : _filter) {
        EList<ExtensionItem> _items = block.getItems();
        for (final ExtensionItem item : _items) {
          boolean _matched = false;
          if (item instanceof PhasedRankProfile) {
            _matched=true;
            VespaPackage.Rank _rank = this.rank(((PhasedRankProfile)item));
            result.add(_rank);
          }
          if (!_matched) {
            if (item instanceof ExtParam) {
              _matched=true;
              String _elvis = null;
              String _key = ((ExtParam)item).getKey();
              if (_key != null) {
                _elvis = _key;
              } else {
                String _quotedKey = ((ExtParam)item).getQuotedKey();
                _elvis = _quotedKey;
              }
              String _plus = ("extension parameter " + _elvis);
              String _plus_1 = (_plus + " on a collection");
              throw Unsupported.of(_plus_1);
            }
          }
          if (!_matched) {
            String _name = item.eClass().getName();
            String _plus = (_name + " on a collection; a Vespa collection extension declares rank profiles");
            throw Unsupported.of(_plus);
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private VespaPackage.Rank rank(final PhasedRankProfile profile) {
    VespaPackage.Rank _xblockexpression = null;
    {
      boolean _startsWith = profile.getName().startsWith("vecdsl_");
      if (_startsWith) {
        String _name = profile.getName();
        String _plus = ("the rank profile name " + _name);
        String _plus_1 = (_plus + "; the prefix vecdsl_ is reserved for the generator\'s rank profiles");
        throw Unsupported.of(_plus_1);
      }
      ModelDeployment _model = profile.getModel();
      boolean _tripleNotEquals = (_model != null);
      if (_tripleNotEquals) {
        String _name_1 = profile.getName();
        String _plus_2 = ("the model of rank profile " + _name_1);
        String _plus_3 = (_plus_2 + "; generated programs deploy no models: an onnx-model needs its model file in the application package");
        throw Unsupported.of(_plus_3);
      }
      final Map<String, String> functions = CollectionLiterals.<String, String>newLinkedHashMap();
      EList<RankFunction> _functions = profile.getFunctions();
      for (final RankFunction fn : _functions) {
        functions.put(fn.getName(), fn.getExpression());
      }
      final List<String> matchFeatures = CollectionLiterals.<String>newArrayList();
      final List<String> summaryFeatures = CollectionLiterals.<String>newArrayList();
      final Map<String, String> settings = CollectionLiterals.<String, String>newLinkedHashMap();
      EList<ExtParam> _params = profile.getParams();
      for (final ExtParam param : _params) {
        {
          String _elvis = null;
          String _key = param.getKey();
          if (_key != null) {
            _elvis = _key;
          } else {
            String _quotedKey = param.getQuotedKey();
            _elvis = _quotedKey;
          }
          final String key = _elvis;
          if (key != null) {
            switch (key) {
              case "match_features":
                List<String> _features = this.features(param.getValue(), key);
                Iterables.<String>addAll(matchFeatures, _features);
                break;
              case "summary_features":
                List<String> _features_1 = this.features(param.getValue(), key);
                Iterables.<String>addAll(summaryFeatures, _features_1);
                break;
              case "num_threads_per_search":
                Literal _value = param.getValue();
                boolean _not = (!(_value instanceof NumberLit));
                if (_not) {
                  throw Unsupported.of("num_threads_per_search that is not a number");
                }
                Literal _value_1 = param.getValue();
                settings.put("num-threads-per-search", VespaPackage.number(((NumberLit) _value_1).getValue()));
                break;
              default:
                throw Unsupported.of((("the rank profile parameter " + key) + "; has no Vespa mapping yet (keep_rank_count and rank_score_drop_limit belong to the phases of the profile)"));
            }
          } else {
            throw Unsupported.of((("the rank profile parameter " + key) + "; has no Vespa mapping yet (keep_rank_count and rank_score_drop_limit belong to the phases of the profile)"));
          }
        }
      }
      final List<String> inputs = CollectionLiterals.<String>newArrayList();
      EList<TensorInput> _inputs = profile.getInputs();
      for (final TensorInput input : _inputs) {
        String _name_2 = input.getName();
        String _plus_4 = ("query(" + _name_2);
        String _plus_5 = (_plus_4 + ") ");
        String _tensorType = input.getTensorType();
        String _plus_6 = (_plus_5 + _tensorType);
        inputs.add(_plus_6);
      }
      String _name_3 = profile.getName();
      PhasedRankProfile _parent = profile.getParent();
      String _name_4 = null;
      if (_parent!=null) {
        _name_4=_parent.getName();
      }
      String _firstPhase = profile.getFirstPhase();
      String _secondPhase = profile.getSecondPhase();
      String _globalPhase = profile.getGlobalPhase();
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(profile, "rerankCount");
      if (_written) {
        _xifexpression = Integer.valueOf(profile.getRerankCount());
      } else {
        _xifexpression = null;
      }
      _xblockexpression = new VespaPackage.Rank(_name_3, _name_4, inputs, functions, _firstPhase, _secondPhase, _globalPhase, _xifexpression, matchFeatures, summaryFeatures, settings);
    }
    return _xblockexpression;
  }

  private List<String> features(final Literal value, final String key) {
    List<String> _xblockexpression = null;
    {
      if ((!(value instanceof StringLit))) {
        throw Unsupported.of((key + " that is not a string of feature names"));
      }
      _xblockexpression = IterableExtensions.<String>toList(((Iterable<String>)Conversions.doWrapArray(((StringLit) value).getValue().trim().split("\\s+"))));
    }
    return _xblockexpression;
  }

  /**
   * Whether a declared rank profile, or one it inherits, has the input query(vecdsl_q) a nearest-neighbour query sends.
   */
  public static boolean takesQueryVector(final PhasedRankProfile profile) {
    boolean _xblockexpression = false;
    {
      PhasedRankProfile walked = profile;
      while ((walked != null)) {
        {
          final Function1<TensorInput, Boolean> _function = (TensorInput it) -> {
            String _name = it.getName();
            return Boolean.valueOf(Objects.equals(_name, VespaPackage.QUERY_TENSOR));
          };
          boolean _exists = IterableExtensions.<TensorInput>exists(walked.getInputs(), _function);
          if (_exists) {
            return true;
          }
          walked = walked.getParent();
        }
      }
      _xblockexpression = false;
    }
    return _xblockexpression;
  }

  private VespaPackage.Field hidden(final String name) {
    return new VespaPackage.Field(name, "string", "STRING", false, true, null, null, null);
  }

  /**
   * The analyzer of the collection's full-text fields; Vespa's linguistics applies one processing to every text field.
   */
  public Analyzer analyzerOf(final VectorCollection c) {
    Analyzer _xblockexpression = null;
    {
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(this.fullText(c, it));
      };
      final Function1<PayloadField, Analyzer> _function_1 = (PayloadField it) -> {
        return it.getAnalyzer();
      };
      final Set<Analyzer> used = IterableExtensions.<Analyzer>toSet(IterableExtensions.<Analyzer>filterNull(IterableExtensions.<PayloadField, Analyzer>map(IterableExtensions.<PayloadField>filter(ModelUtil.payloadFields(c), _function), _function_1)));
      final Function1<PayloadIndex, Boolean> _function_2 = (PayloadIndex it) -> {
        EPayloadIndexKind _kind = it.getKind();
        return Boolean.valueOf(Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT));
      };
      final Function1<PayloadIndex, Analyzer> _function_3 = (PayloadIndex it) -> {
        return it.getAnalyzer();
      };
      Iterable<Analyzer> _filterNull = IterableExtensions.<Analyzer>filterNull(IterableExtensions.<PayloadIndex, Analyzer>map(IterableExtensions.<PayloadIndex>filter(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_2), _function_3));
      Iterables.<Analyzer>addAll(used, _filterNull);
      if (((used.size() > 1) && (((IterableExtensions.<String>toSet(IterableExtensions.<Analyzer, String>map(used, ((Function1<Analyzer, String>) (Analyzer it) -> {
        return it.getLanguage();
      }))).size() > 1) || (IterableExtensions.<Double>toSet(IterableExtensions.<Analyzer, Double>map(used, ((Function1<Analyzer, Double>) (Analyzer it) -> {
        return Double.valueOf(it.getBm25K1());
      }))).size() > 1)) || (IterableExtensions.<Double>toSet(IterableExtensions.<Analyzer, Double>map(used, ((Function1<Analyzer, Double>) (Analyzer it) -> {
        return Double.valueOf(it.getBm25B());
      }))).size() > 1)))) {
        throw Unsupported.of("full-text fields with different analyzers; has no Vespa mapping yet (one rank profile per analyzer setting and set_language per field)");
      }
      final Analyzer analyzer = IterableExtensions.<Analyzer>head(used);
      if ((analyzer != null)) {
        VespaSchema.checkAnalyzer(analyzer);
      }
      _xblockexpression = analyzer;
    }
    return _xblockexpression;
  }

  public static void checkAnalyzer(final Analyzer analyzer) {
    final String tokenizer = analyzer.getTokenizer().getLiteral();
    if (((!Objects.equals(tokenizer, "standard")) && (!Objects.equals(tokenizer, "language")))) {
      throw Unsupported.of((("the " + tokenizer) + " tokenizer; has no Vespa mapping yet (a linguistics component or match settings per field: match: exact for keyword, match: gram for ngram)"));
    }
    boolean _isEmpty = analyzer.getResources().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("analyzer resources; has no Vespa mapping yet (files in the application package read by a linguistics component)");
    }
    if (((analyzer.getLanguage() != null) && (!Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("en", "english")).contains(analyzer.getLanguage().toLowerCase())))) {
      String _language = analyzer.getLanguage();
      String _plus = ("the language " + _language);
      String _plus_1 = (_plus + "; has no Vespa mapping yet (set_language in the indexing statements and the query language)");
      throw Unsupported.of(_plus_1);
    }
    if ((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "none")))) {
      String _stopwords = analyzer.getStopwords();
      String _plus_2 = ("stopwords " + _stopwords);
      String _plus_3 = (_plus_2 + "; has no Vespa mapping yet (Vespa removes no stop words when indexing; a query rewrite of the stop words would carry it)");
      throw Unsupported.of(_plus_3);
    }
    EList<AnalyzerFilter> _filters = analyzer.getFilters();
    for (final AnalyzerFilter filter : _filters) {
      VespaSchema.checkFilter(filter.getType().getLiteral());
    }
  }

  /**
   * Filters Vespa's default linguistics applies: lowercasing and stemming (stemming: best); stop with the list none.
   */
  public static void checkFilter(final String filter) {
    if (filter != null) {
      switch (filter) {
        case "lowercase":
        case "stemmer":
        case "stop":
          break;
        case "ascii_folding":
          break;
        default:
          throw Unsupported.of((("the " + filter) + " filter; has no Vespa mapping yet (a custom linguistics component)"));
      }
    } else {
      throw Unsupported.of((("the " + filter) + " filter; has no Vespa mapping yet (a custom linguistics component)"));
    }
  }

  public boolean fullText(final VectorCollection c, final PayloadField field) {
    return (((field.isFullText() || field.isDocumentBody()) || (field.getAnalyzer() != null)) || IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
      return Boolean.valueOf(((it.getTarget().getField() == field) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
    })));
  }

  /**
   * The schema fields of one declared field: a document field, and for a full-text field its exact companion attribute.
   */
  public List<VespaPackage.Field> fieldsOf(final VectorCollection c, final Field field) {
    List<VespaPackage.Field> _xblockexpression = null;
    {
      final String named = this.name(c, field);
      boolean _contains = VespaPackage.RESERVED.contains(named.toLowerCase());
      if (_contains) {
        throw Unsupported.of((("the field name " + named) + "; Vespa reserves it (Schema.RESERVED_NAMES of config-model)"));
      }
      boolean _startsWith = named.startsWith("vecdsl_");
      if (_startsWith) {
        throw Unsupported.of((("the field name " + named) + "; the prefix vecdsl_ is reserved for the generator\'s hidden fields"));
      }
      List<VespaPackage.Field> _switchResult = null;
      boolean _matched = false;
      if (field instanceof VectorField) {
        _matched=true;
        VespaPackage.Field _vectorField = this.vectorField(c, ((VectorField)field), named);
        _switchResult = Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_vectorField));
      }
      if (!_matched) {
        if (field instanceof PayloadField) {
          _matched=true;
          _switchResult = this.payloadFields(c, ((PayloadField)field), named);
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private List<VespaPackage.Field> payloadFields(final VectorCollection c, final PayloadField field, final String name) {
    this.declaredNote(field, "nullable", "maxLength", "defaultValue");
    boolean _isEmpty = field.getLimits().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("field limits; Vespa validates the value types of a document, not their sizes");
    }
    boolean _isEmpty_1 = field.getSubFields().isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      throw Unsupported.of((("sub-fields of field " + name) + "; has no Vespa mapping yet (struct fields with struct-field attributes)"));
    }
    final EScalarDType type = field.getType();
    final Function1<PayloadIndex, Boolean> _function = (PayloadIndex it) -> {
      Field _field = it.getTarget().getField();
      return Boolean.valueOf((_field == field));
    };
    final boolean indexed = IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function);
    boolean _containsKey = VespaSchema.NUMBERS.containsKey(type);
    if (_containsKey) {
      String _get = VespaSchema.NUMBERS.get(type);
      VespaPackage.Field _field = new VespaPackage.Field(name, _get, "NUMERIC", false, indexed, null, null, null);
      return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field));
    }
    boolean _equals = Objects.equals(type, EScalarDType.BOOL);
    if (_equals) {
      VespaPackage.Field _field_1 = new VespaPackage.Field(name, "bool", "BOOL", false, indexed, null, null, null);
      return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_1));
    }
    boolean _contains = VespaSchema.STRINGS.contains(type);
    if (_contains) {
      boolean _fullText = this.fullText(c, field);
      if (_fullText) {
        if (((!Objects.equals(type, EScalarDType.VARCHAR)) && (!Objects.equals(type, EScalarDType.TEXT)))) {
          String _literal = type.getLiteral();
          String _plus = ("full-text search on " + _literal);
          String _plus_1 = (_plus + " field ");
          String _plus_2 = (_plus_1 + name);
          throw Unsupported.of(_plus_2);
        }
        this.analyzerOf(c);
        VespaPackage.Field _field_2 = new VespaPackage.Field(name, "string", "TEXT", false, false, null, null, null);
        VespaPackage.Field _field_3 = new VespaPackage.Field((VespaPackage.EXACT + name), "string", "STRING", false, false, null, null, name);
        return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_2, _field_3));
      }
      VespaPackage.Field _field_4 = new VespaPackage.Field(name, "string", "STRING", false, indexed, null, null, null);
      return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_4));
    }
    if (type != null) {
      switch (type) {
        case ARRAY:
          boolean _written = ModelUtil.written(field, "elementType");
          boolean _not_2 = (!_written);
          if (_not_2) {
            throw Unsupported.of((("array field " + name) + " without element-type"));
          }
          final EScalarDType element = field.getElementType();
          boolean _containsKey_1 = VespaSchema.NUMBERS.containsKey(element);
          if (_containsKey_1) {
            String _get_1 = VespaSchema.NUMBERS.get(element);
            String _plus_3 = ("array<" + _get_1);
            String _plus_4 = (_plus_3 + ">");
            VespaPackage.Field _field_5 = new VespaPackage.Field(name, _plus_4, "NUMERIC", true, indexed, null, null, null);
            return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_5));
          }
          boolean _equals_1 = Objects.equals(element, EScalarDType.BOOL);
          if (_equals_1) {
            VespaPackage.Field _field_6 = new VespaPackage.Field(name, "array<bool>", "BOOL", true, indexed, null, null, null);
            return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_6));
          }
          boolean _contains_1 = VespaSchema.STRINGS.contains(element);
          if (_contains_1) {
            VespaPackage.Field _field_7 = new VespaPackage.Field(name, "array<string>", "STRING", true, indexed, null, null, null);
            return Collections.<VespaPackage.Field>unmodifiableList(CollectionLiterals.<VespaPackage.Field>newArrayList(_field_7));
          }
          String _literal_1 = element.getLiteral();
          String _plus_5 = ("arrays of " + _literal_1);
          String _plus_6 = (_plus_5 + " in field ");
          String _plus_7 = (_plus_6 + name);
          String _plus_8 = (_plus_7 + "; has no Vespa mapping yet (array<struct> with struct-field attributes)");
          throw Unsupported.of(_plus_8);
        case JSON:
        case MAP:
        case STRUCT:
          String _literal_2 = type.getLiteral();
          String _plus_9 = (_literal_2 + " field ");
          String _plus_10 = (_plus_9 + name);
          String _plus_11 = (_plus_10 + "; has no Vespa mapping yet (struct and map<string,string> fields with struct-field attributes)");
          throw Unsupported.of(_plus_11);
        case GEO_POINT:
          throw Unsupported.of((("geo_point field " + name) + "; has no Vespa mapping yet (a position field and the geoLocation operator)"));
        case GEOMETRY:
          throw Unsupported.of((("geometry field " + name) + "; Vespa stores points (the position type) but no shapes, so it has no spatial relation operators"));
        case BYTES:
          throw Unsupported.of((("bytes field " + name) + "; has no Vespa mapping yet (a raw field, summary only)"));
        default:
          String _literal_3 = type.getLiteral();
          String _plus_12 = (_literal_3 + " field ");
          String _plus_13 = (_plus_12 + name);
          String _plus_14 = (_plus_13 + "; Vespa documents hold no media values (the client stores media elsewhere)");
          throw Unsupported.of(_plus_14);
      }
    } else {
      String _literal_3 = type.getLiteral();
      String _plus_12 = (_literal_3 + " field ");
      String _plus_13 = (_plus_12 + name);
      String _plus_14 = (_plus_13 + "; Vespa documents hold no media values (the client stores media elsewhere)");
      throw Unsupported.of(_plus_14);
    }
  }

  private VespaPackage.Field vectorField(final VectorCollection c, final VectorField field, final String name) {
    VespaPackage.Field _xblockexpression = null;
    {
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; has no Vespa mapping yet (a mixed tensor tensor<float>(p{},x[d]) with max_sim in a rank expression)"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + VespaSchema.NO_MODELS));
      }
      boolean _isOnDisk = field.isOnDisk();
      if (_isOnDisk) {
        throw Unsupported.of(VespaSchema.NO_DISK_SETTING);
      }
      this.declaredNote(field, "normalized", "nullable");
      final String type = field.getType().getLiteral();
      boolean _equals = Objects.equals(type, "sparse_float_vector");
      if (_equals) {
        throw Unsupported.of((("sparse vector field " + name) + "; has no Vespa mapping yet (a mapped tensor tensor<float>(t{}) ranked with sum(query(q) * attribute(f)), or a weightedset with wand)"));
      }
      boolean _equals_1 = Objects.equals(type, "binary_vector");
      if (_equals_1) {
        throw Unsupported.of((("binary vector field " + name) + "; has no Vespa mapping yet (tensor<int8>(x[dim/8]) of packed bits with distance-metric hamming)"));
      }
      final String cell = VespaSchema.CELLS.get(type);
      if ((cell == null)) {
        throw Unsupported.of((type + " fields; Vespa tensor cells are double, float, bfloat16 or int8 (no float16 and no unsigned int8)"));
      }
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      boolean _isEmpty = field.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("field limits; Vespa validates the value types of a document, not their sizes");
      }
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      int _dimension_1 = field.getDimension();
      String _plus = ((("tensor<" + cell) + ">(x[") + Integer.valueOf(_dimension_1));
      String _plus_1 = (_plus + "])");
      String _distanceMetric = this.distanceMetric(field, index);
      VespaPackage.Hnsw _hnsw = this.hnsw(index);
      _xblockexpression = new VespaPackage.Field(name, _plus_1, "TENSOR", false, false, _distanceMetric, _hnsw, null);
    }
    return _xblockexpression;
  }

  public String metric(final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(field, "metric");
      if (_written) {
        _xifexpression = field.getMetric();
      } else {
        EMetric _xifexpression_1 = null;
        if (((index != null) && ModelUtil.written(index, "metric"))) {
          _xifexpression_1 = index.getMetric();
        } else {
          _xifexpression_1 = EMetric.COSINE;
        }
        _xifexpression = _xifexpression_1;
      }
      final EMetric chosen = _xifexpression;
      _xblockexpression = chosen.getLiteral();
    }
    return _xblockexpression;
  }

  /**
   * angular for cosine (prenormalized-angular for normalized vectors), euclidean, dotproduct and hamming.
   */
  public String distanceMetric(final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      final String chosen = this.metric(field, index);
      final String distance = VespaSchema.METRICS.get(chosen);
      if ((distance == null)) {
        throw Unsupported.of((("the " + chosen) + " metric; Vespa\'s distance metrics are euclidean, angular, dotproduct, prenormalized-angular, geodegrees and hamming"));
      }
      String _xifexpression = null;
      if ((Objects.equals(chosen, "cosine") && field.isNormalized())) {
        _xifexpression = "prenormalized-angular";
      } else {
        _xifexpression = distance;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * HNSW for hnsw and auto with max-links-per-node and neighbors-to-explore-at-insert; no index (exact search) for flat or none.
   */
  public VespaPackage.Hnsw hnsw(final VectorIndex index) {
    VespaPackage.Hnsw _xblockexpression = null;
    {
      if ((index == null)) {
        return null;
      }
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; Vespa builds and searches HNSW indexes on the CPU");
      }
      boolean _isOnDisk = index.isOnDisk();
      if (_isOnDisk) {
        throw Unsupported.of(VespaSchema.NO_DISK_SETTING);
      }
      boolean _written = ModelUtil.written(index, "centroids");
      if (_written) {
        throw Unsupported.of("centroids; Vespa\'s vector index is HNSW (no IVF lists)");
      }
      Set<String> _keySet = ModelUtil.extParams(index, VespaGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of((("extension parameter " + key) + " on a vector index"));
      }
      this.declaredNote(index, "online");
      final Quantization q = index.getQuantization();
      if (((q != null) && (!Objects.equals(q.getType().getLiteral(), "none")))) {
        String _literal = q.getType().getLiteral();
        String _plus = (_literal + " quantization; has no Vespa mapping yet (binarize | pack_bits into a hamming field for binary, int8 or bfloat16 cells written by the client for scalar)");
        throw Unsupported.of(_plus);
      }
      VespaPackage.Hnsw _switchResult = null;
      EIndexFamily _family = index.getFamily();
      if (_family != null) {
        switch (_family) {
          case FLAT:
            Object _xblockexpression_1 = null;
            {
              if ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates"))) {
                throw Unsupported.of("graph settings on a flat index; an exact search has no graph");
              }
              _xblockexpression_1 = null;
            }
            _switchResult = ((VespaPackage.Hnsw)_xblockexpression_1);
            break;
          case AUTO:
          case HNSW:
            Integer _xifexpression = null;
            boolean _written_1 = ModelUtil.written(index, "maxDegree");
            if (_written_1) {
              _xifexpression = Integer.valueOf(index.getMaxDegree());
            } else {
              _xifexpression = null;
            }
            Integer _xifexpression_1 = null;
            boolean _written_2 = ModelUtil.written(index, "buildCandidates");
            if (_written_2) {
              _xifexpression_1 = Integer.valueOf(index.getBuildCandidates());
            } else {
              _xifexpression_1 = null;
            }
            _switchResult = new VespaPackage.Hnsw(_xifexpression, _xifexpression_1);
            break;
          default:
            String _literal_1 = index.getFamily().getLiteral();
            String _plus_1 = (_literal_1 + " indexes; Vespa indexes tensor attributes with HNSW (no IVF, Vamana or sparse inverted index)");
            throw Unsupported.of(_plus_1);
        }
      } else {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus_1 = (_literal_1 + " indexes; Vespa indexes tensor attributes with HNSW (no IVF, Vamana or sparse inverted index)");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * Payload indexes ask for an attribute with fast-search (exact kinds) or an index field (full_text) on a declared field.
   */
  public void checkPayloadIndex(final VectorCollection c, final PayloadIndex payloadIndex) {
    final FieldPath path = payloadIndex.getTarget();
    String _rawPath = path.getRawPath();
    boolean _tripleNotEquals = (_rawPath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields");
    }
    boolean _isEmpty = path.getSubFields().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("a payload index on a sub-field; has no Vespa mapping yet (struct-field attributes)");
    }
    Field _field = path.getField();
    boolean _not_1 = (!(_field instanceof PayloadField));
    if (_not_1) {
      throw Unsupported.of("a payload index on a vector field");
    }
    Set<String> _keySet = ModelUtil.extParams(payloadIndex, VespaGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of((("extension parameter " + key) + " on a payload index"));
    }
    boolean _isOnDisk = payloadIndex.isOnDisk();
    if (_isOnDisk) {
      throw Unsupported.of(VespaSchema.NO_DISK_SETTING);
    }
    this.note(payloadIndex, "structure", "lowercase", "minTokenLength", "maxTokenLength", "prefixMatch");
    final VespaPackage.Field field = this.schema(c).field(this.name(c, path.getField()));
    String _switchResult = null;
    EPayloadIndexKind _kind = payloadIndex.getKind();
    if (_kind != null) {
      switch (_kind) {
        case KEYWORD:
        case UUID:
        case DATETIME:
          _switchResult = "STRING";
          break;
        case BOOL:
          _switchResult = "BOOL";
          break;
        case INTEGER:
        case FLOAT:
          _switchResult = "NUMERIC";
          break;
        case FULL_TEXT:
          _switchResult = "TEXT";
          break;
        case GEO:
          throw Unsupported.of("geo payload indexes; has no Vespa mapping yet (position fields)");
        case JSON:
          throw Unsupported.of("json payload indexes; has no Vespa mapping yet (struct-field attributes)");
        default:
          break;
      }
    }
    final String expected = _switchResult;
    String _kind_1 = field.kind();
    boolean _notEquals = (!Objects.equals(_kind_1, expected));
    if (_notEquals) {
      String _literal = payloadIndex.getKind().getLiteral();
      String _plus = ("a " + _literal);
      String _plus_1 = (_plus + " index on ");
      String _name = field.name();
      String _plus_2 = (_plus_1 + _name);
      String _plus_3 = (_plus_2 + ", which is a ");
      String _kind_2 = field.kind();
      String _plus_4 = (_plus_3 + _kind_2);
      String _plus_5 = (_plus_4 + " field; has no Vespa mapping yet (a synthetic field with its own indexing)");
      throw Unsupported.of(_plus_5);
    }
  }

  public boolean isDeployed(final String documentType) {
    return this.deployed.containsKey(documentType);
  }

  public void put(final VespaPackage.Schema schema) {
    this.deployed.put(schema.name(), schema);
  }

  public void remove(final String documentType) {
    this.deployed.remove(documentType);
  }

  public List<VespaPackage.Schema> current() {
    return IterableExtensions.<VespaPackage.Schema>toList(this.deployed.values());
  }

  /**
   * Deploys the package as the program's statements leave it. created and dropped name the document types the statement adds or
   * removes; the runtime checks them against the deployed application (an existing type fails a create unless skip is set, a
   * missing type fails a drop unless ignore is set) and deletes the documents of the dropped types before the deployment.
   */
  public Code.Stmt deploy(final List<String> overrides, final List<String> created, final boolean skip, final List<String> dropped, final boolean ignore) {
    Code.Run _xblockexpression = null;
    {
      final Map<String, String> files = VespaPackage.files(this.current());
      final Code.Entries contents = Code.entries();
      Set<Map.Entry<String, String>> _entrySet = files.entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        contents.put(file.getKey(), Code.text(file.getValue()));
      }
      final Code.Entries args = VespaSchema.kw("files", contents);
      if (((!files.isEmpty()) && (!overrides.isEmpty()))) {
        args.put("overrides", Code.text(VespaPackage.validationOverrides(overrides)));
      }
      boolean _isEmpty = created.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("created", Code.texts(created));
        if (skip) {
          args.put("skip_existing", Code.flag(true));
        }
      }
      boolean _isEmpty_1 = dropped.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        args.put("dropped", Code.texts(dropped));
        if (ignore) {
          args.put("ignore_missing", Code.flag(true));
        }
      }
      Code.Call _runtime = Code.runtime("deploy", args);
      _xblockexpression = new Code.Run(_runtime);
    }
    return _xblockexpression;
  }

  /**
   * Settings of a declaration that Vespa ignores, listed only on the statement that creates the collection.
   */
  private void declaredNote(final EObject host, final String... features) {
    if (this.declaring) {
      this.note(host, features);
    }
  }

  public void setDeclaring(final boolean value) {
    this.declaring = value;
  }

  public void note(final EObject host, final String... features) {
    for (final String feature : features) {
      boolean _written = ModelUtil.written(host, feature);
      if (_written) {
        String _name = host.eClass().getName();
        String _plus = ((feature + " on ") + _name);
        this.notes.add(_plus);
      }
    }
  }

  public static Code.Entries kw(final Object... pairs) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      {
        int i = 0;
        int _length = pairs.length;
        boolean _lessThan = (i < _length);
        boolean _while = _lessThan;
        while (_while) {
          Object _get = pairs[i];
          Object _get_1 = pairs[(i + 1)];
          result.put(((String) _get), ((Code.Expr) _get_1));
          int _i = i;
          i = (_i + 2);
          int _length_1 = pairs.length;
          boolean _lessThan_1 = (i < _length_1);
          _while = _lessThan_1;
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }
}
