package org.ftn.vecDSL.generator.redis;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * The index of each collection: every declared field is an attribute of one FT.CREATE ... ON JSON index over the key prefix
 * <collection>: (or <database>:<collection>:), so filters, keyword and vector search all read the same documents. Statements see
 * the index as the alter statements before them leave it (added, dropped and renamed fields, a new name).
 */
@SuppressWarnings("all")
public class RedisSchema {
  public static final String NO_MODELS = "; Redis Open Source runs no embedding models (the Query Engine indexes the vectors a client writes), so generated programs supply their own vectors";

  public static final String ON_DISK = "on-disk; Redis Open Source keeps documents and indexes in memory (Redis Flex, formerly Auto Tiering, is a Redis Software and Redis Cloud feature)";

  private static final List<EScalarDType> NUMBERS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> STRINGS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID, EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<String, String> VECTOR_TYPES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("float_vector", "FLOAT32"), Pair.<String, String>of("float16_vector", "FLOAT16"), Pair.<String, String>of("bfloat16_vector", "BFLOAT16"), Pair.<String, String>of("float64_vector", "FLOAT64"), Pair.<String, String>of("int8_vector", "INT8"), Pair.<String, String>of("uint8_vector", "UINT8")));

  private final List<String> notes;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, RedisIndex.Index> evolved = CollectionLiterals.<VectorCollection, RedisIndex.Index>newHashMap();

  private final Map<VectorCollection, Map<String, String>> renames = CollectionLiterals.<VectorCollection, Map<String, String>>newHashMap();

  private boolean declaring = false;

  public RedisSchema(final List<String> notes, final Script script) {
    this.notes = notes;
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
    for (final EObject object : _iterable) {
      this.slices(object);
    }
  }

  /**
   * Collections whose statements name a tenant or a namespace get the hidden slice attributes.
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
  }

  public RedisIndex.Index index(final VectorCollection c) {
    RedisIndex.Index _elvis = null;
    RedisIndex.Index _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      RedisIndex.Index _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final RedisIndex.Index index) {
    if ((index == null)) {
      this.evolved.remove(c);
      this.renames.remove(c);
    } else {
      this.evolved.put(c, index);
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

  private RedisIndex.Index declared(final VectorCollection c) {
    RedisIndex.Index _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!RedisSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys documents by an integer, text or uuid field");
        throw Unsupported.of(_plus_1);
      }
      final String keyName = RedisIndex.id(ModelUtil.fieldName(key));
      final List<RedisIndex.Attr> attrs = CollectionLiterals.<RedisIndex.Attr>newArrayList();
      final List<String> fields = CollectionLiterals.<String>newArrayList();
      final Map<String, Integer> vectors = CollectionLiterals.<String, Integer>newLinkedHashMap();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        {
          String _id = RedisIndex.id(ModelUtil.fieldName(field));
          fields.add(_id);
          List<RedisIndex.Attr> _attributes = this.attributes(c, field);
          Iterables.<RedisIndex.Attr>addAll(attrs, _attributes);
          if ((field instanceof VectorField)) {
            vectors.put(ModelUtil.fieldName(field), Integer.valueOf(((VectorField)field).getDimension()));
          }
        }
      }
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(it.isPartitionKey());
      };
      final PayloadField partitionKey = IterableExtensions.<PayloadField>findFirst(ModelUtil.payloadFields(c), _function);
      String _xifexpression_1 = null;
      if (((!c.getTenants().isEmpty()) || this.tenanted.contains(c))) {
        String _xifexpression_2 = null;
        if ((partitionKey == null)) {
          _xifexpression_2 = RedisIndex.TENANT;
        } else {
          _xifexpression_2 = partitionKey.getName();
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String tenant = _xifexpression_1;
      String _xifexpression_3 = null;
      if (((!c.getNamespaces().isEmpty()) || this.namespaced.contains(c))) {
        String _xifexpression_4 = null;
        if ((partitionKey == null)) {
          _xifexpression_4 = RedisIndex.NAMESPACE;
        } else {
          _xifexpression_4 = partitionKey.getName();
        }
        _xifexpression_3 = _xifexpression_4;
      }
      final String namespace = _xifexpression_3;
      String _xifexpression_5 = null;
      boolean _isEmpty = ModelUtil.partitions(c).isEmpty();
      if (_isEmpty) {
        _xifexpression_5 = null;
      } else {
        _xifexpression_5 = RedisIndex.PARTITION;
      }
      final String partition = _xifexpression_5;
      final Function1<String, Boolean> _function_1 = (String it) -> {
        return Boolean.valueOf(it.startsWith("vecdsl_"));
      };
      Iterable<String> _filter = IterableExtensions.<String>filter(IterableExtensions.<String>filterNull(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(tenant, namespace, partition))), _function_1);
      for (final String hidden : _filter) {
        RedisIndex.Attr _attr = new RedisIndex.Attr(hidden, ("$." + hidden), "TAG", null, 1.0, false);
        attrs.add(_attr);
      }
      final Analyzer analyzer = this.analyzerOf(c);
      String _xifexpression_6 = null;
      String _language = null;
      if (analyzer!=null) {
        _language=analyzer.getLanguage();
      }
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        _xifexpression_6 = null;
      } else {
        _xifexpression_6 = RedisSchema.languageOf(analyzer);
      }
      final String language = _xifexpression_6;
      List<String> _xifexpression_7 = null;
      if ((Objects.equals(language, "english") && (!Objects.equals(analyzer.getStopwords(), "none")))) {
        _xifexpression_7 = null;
      } else {
        _xifexpression_7 = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
      }
      final List<String> stopwords = _xifexpression_7;
      Integer _xifexpression_8 = null;
      boolean _written = ModelUtil.written(c, "ttlSeconds");
      if (_written) {
        _xifexpression_8 = Integer.valueOf(c.getTtlSeconds());
      } else {
        _xifexpression_8 = null;
      }
      final Integer ttl = _xifexpression_8;
      String _databaseOf = RedisSchema.databaseOf(c);
      String _id = RedisIndex.id(c.getName());
      boolean _contains = RedisSchema.NUMBERS.contains(((PayloadField) key).getType());
      _xblockexpression = new RedisIndex.Index(_databaseOf, _id, keyName, _contains, attrs, fields, vectors, tenant, namespace, partition, language, stopwords, ttl);
    }
    return _xblockexpression;
  }

  /**
   * The logical database of a collection: a name prefix of its index and keys (the Query Engine indexes database 0 only).
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      String _xifexpression = null;
      if ((database == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = RedisIndex.id(database.getName());
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * The analyzer of the collection's full-text fields; an index has one language and one stop-word list.
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
      if (((used.size() > 1) && (IterableExtensions.<String>toSet(IterableExtensions.<Analyzer, String>map(used, ((Function1<Analyzer, String>) (Analyzer it) -> {
        return it.getLanguage();
      }))).size() > 1))) {
        throw Unsupported.of("full-text fields with analyzers of different languages; a Query Engine index stems with one LANGUAGE");
      }
      final Analyzer analyzer = IterableExtensions.<Analyzer>head(used);
      if ((analyzer != null)) {
        RedisSchema.checkAnalyzer(analyzer);
      }
      _xblockexpression = analyzer;
    }
    return _xblockexpression;
  }

  public static void checkAnalyzer(final Analyzer analyzer) {
    if (((!Objects.equals(analyzer.getTokenizer().getLiteral(), "standard")) && (!Objects.equals(analyzer.getTokenizer().getLiteral(), "language")))) {
      String _literal = analyzer.getTokenizer().getLiteral();
      String _plus = ("the " + _literal);
      String _plus_1 = (_plus + " tokenizer; the Query Engine splits text at its fixed separator characters (src/toksep.h)");
      throw Unsupported.of(_plus_1);
    }
    if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
      throw Unsupported.of("BM25 parameters; the BM25STD scorer uses the constants k1 = 1.2 and b = 0.75 (src/ext/default.c), which no option changes");
    }
    boolean _isEmpty = analyzer.getResources().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("analyzer resources; has no Redis mapping yet (FT.SYNUPDATE groups and STOPWORDS read from the files)");
    }
    String _language = analyzer.getLanguage();
    boolean _tripleNotEquals = (_language != null);
    if (_tripleNotEquals) {
      RedisSchema.languageOf(analyzer);
    }
    if ((((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "none"))) && (!Objects.equals(analyzer.getStopwords(), "english"))) && (!Objects.equals(analyzer.getStopwords(), "en")))) {
      String _stopwords = analyzer.getStopwords();
      String _plus_2 = ("stopwords " + _stopwords);
      String _plus_3 = (_plus_2 + "; the Query Engine has one built-in stop-word list (English, src/stopwords.h); other lists are given word by word");
      throw Unsupported.of(_plus_3);
    }
    EList<AnalyzerFilter> _filters = analyzer.getFilters();
    for (final AnalyzerFilter filter : _filters) {
      RedisSchema.checkFilter(filter.getType().getLiteral(), analyzer);
    }
  }

  public static String languageOf(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      final String language = RedisIndex.language(analyzer.getLanguage());
      if ((language == null)) {
        String _language = analyzer.getLanguage();
        String _plus = ("the language " + _language);
        String _plus_1 = (_plus + "; the Query Engine has no stemmer for it");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = language;
    }
    return _xblockexpression;
  }

  /**
   * Filters the Query Engine applies itself: lowercase always, stop words of the index, stemming with a language.
   */
  public static void checkFilter(final String filter, final Analyzer analyzer) {
    if (filter != null) {
      switch (filter) {
        case "lowercase":
        case "stop":
          break;
        case "stemmer":
          String _language = analyzer.getLanguage();
          boolean _tripleEquals = (_language == null);
          if (_tripleEquals) {
            throw Unsupported.of("the stemmer filter without a language; the Query Engine stems in the index LANGUAGE");
          }
          break;
        case "phonetic":
          throw Unsupported.of("the phonetic filter; has no Redis mapping yet (PHONETIC dm:<language> on the TEXT attributes)");
        case "synonym":
          throw Unsupported.of("the synonym filter; has no Redis mapping yet (FT.SYNUPDATE groups)");
        default:
          throw Unsupported.of((("the " + filter) + " filter; the Query Engine tokenizes, lowercases, removes stop words and stems, with no other token filters"));
      }
    } else {
      throw Unsupported.of((("the " + filter) + " filter; the Query Engine tokenizes, lowercases, removes stop words and stems, with no other token filters"));
    }
  }

  public boolean fullText(final VectorCollection c, final PayloadField field) {
    return (((field.isFullText() || field.isDocumentBody()) || (field.getAnalyzer() != null)) || IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
      return Boolean.valueOf(((it.getTarget().getField() == field) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
    })));
  }

  /**
   * The attributes of one field; json fields index their declared sub-fields, other fields one attribute (two for full text).
   */
  public List<RedisIndex.Attr> attributes(final VectorCollection c, final Field field) {
    List<RedisIndex.Attr> _switchResult = null;
    boolean _matched = false;
    if (field instanceof VectorField) {
      _matched=true;
      RedisIndex.Attr _vectorAttr = this.vectorAttr(c, ((VectorField)field));
      _switchResult = Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_vectorAttr));
    }
    if (!_matched) {
      if (field instanceof PayloadField) {
        _matched=true;
        String _id = RedisIndex.id(this.name(c, field));
        String _plus = ("$." + _id);
        _switchResult = this.payloadAttrs(c, ((PayloadField)field), _plus, this.name(c, field));
      }
    }
    return _switchResult;
  }

  private List<RedisIndex.Attr> payloadAttrs(final VectorCollection c, final PayloadField field, final String path, final String attrName) {
    this.declaredNote(field, "nullable", "maxLength", "defaultValue");
    boolean _isEmpty = field.getLimits().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      throw Unsupported.of("field limits; Redis does not validate document values (JSON documents have no constraints)");
    }
    boolean _isEmpty_1 = field.getSubFields().isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      final List<RedisIndex.Attr> result = CollectionLiterals.<RedisIndex.Attr>newArrayList();
      EList<Field> _subFields = field.getSubFields();
      for (final Field sub : _subFields) {
        {
          final String subName = RedisIndex.id(ModelUtil.fieldName(sub));
          if ((sub instanceof PayloadField)) {
            List<RedisIndex.Attr> _payloadAttrs = this.payloadAttrs(c, ((PayloadField)sub), ((path + ".") + subName), ((attrName + "_") + subName));
            Iterables.<RedisIndex.Attr>addAll(result, _payloadAttrs);
          } else {
            throw Unsupported.of(("a vector field below field " + attrName));
          }
        }
      }
      return result;
    }
    final EScalarDType type = field.getType();
    boolean _contains = RedisSchema.NUMBERS.contains(type);
    if (_contains) {
      RedisIndex.Attr _attr = new RedisIndex.Attr(attrName, path, "NUMERIC", null, 1.0, false);
      return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr));
    }
    boolean _equals = Objects.equals(type, EScalarDType.BOOL);
    if (_equals) {
      RedisIndex.Attr _attr_1 = new RedisIndex.Attr(attrName, path, "TAG", null, 1.0, false);
      return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr_1));
    }
    boolean _contains_1 = RedisSchema.STRINGS.contains(type);
    if (_contains_1) {
      boolean _fullText = this.fullText(c, field);
      if (_fullText) {
        if (((!Objects.equals(type, EScalarDType.VARCHAR)) && (!Objects.equals(type, EScalarDType.TEXT)))) {
          String _literal = type.getLiteral();
          String _plus = ("full-text search on " + _literal);
          String _plus_1 = (_plus + " field ");
          String _plus_2 = (_plus_1 + attrName);
          throw Unsupported.of(_plus_2);
        }
        Analyzer _analyzerOf = this.analyzerOf(c);
        String _language = null;
        if (_analyzerOf!=null) {
          _language=_analyzerOf.getLanguage();
        }
        boolean _tripleNotEquals = (_language != null);
        RedisIndex.Attr _attr_2 = new RedisIndex.Attr(attrName, path, "TEXT", null, 1.0, _tripleNotEquals);
        RedisIndex.Attr _attr_3 = new RedisIndex.Attr((RedisIndex.EXACT + attrName), path, "TAG", null, 1.0, false);
        return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr_2, _attr_3));
      }
      RedisIndex.Attr _attr_4 = new RedisIndex.Attr(attrName, path, "TAG", null, 1.0, false);
      return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr_4));
    }
    if (type != null) {
      switch (type) {
        case ARRAY:
          boolean _written = ModelUtil.written(field, "elementType");
          boolean _not_2 = (!_written);
          if (_not_2) {
            throw Unsupported.of((("array field " + attrName) + " without element-type"));
          }
          final EScalarDType element = field.getElementType();
          boolean _contains_2 = RedisSchema.NUMBERS.contains(element);
          if (_contains_2) {
            RedisIndex.Attr _attr_5 = new RedisIndex.Attr(attrName, (path + "[*]"), "NUMERIC", null, 1.0, false);
            return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr_5));
          }
          if ((Objects.equals(element, EScalarDType.BOOL) || RedisSchema.STRINGS.contains(element))) {
            RedisIndex.Attr _attr_6 = new RedisIndex.Attr(attrName, (path + "[*]"), "TAG", null, 1.0, false);
            return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList(_attr_6));
          }
          String _literal_1 = element.getLiteral();
          String _plus_3 = ("arrays of " + _literal_1);
          String _plus_4 = (_plus_3 + " in field ");
          String _plus_5 = (_plus_4 + attrName);
          String _plus_6 = (_plus_5 + "; the Query Engine indexes arrays of strings, booleans and numbers");
          throw Unsupported.of(_plus_6);
        case JSON:
        case MAP:
        case STRUCT:
          return Collections.<RedisIndex.Attr>unmodifiableList(CollectionLiterals.<RedisIndex.Attr>newArrayList());
        case GEO_POINT:
          throw Unsupported.of((("geo_point field " + attrName) + "; has no Redis mapping yet (GEO attributes read \"longitude,latitude\" strings)"));
        case GEOMETRY:
          throw Unsupported.of((("geometry field " + attrName) + "; has no Redis mapping yet (GEOSHAPE attributes read WKT strings)"));
        default:
          String _literal_2 = type.getLiteral();
          String _plus_7 = (_literal_2 + " field ");
          String _plus_8 = (_plus_7 + attrName);
          String _plus_9 = (_plus_8 + "; JSON documents hold no binary or media values");
          throw Unsupported.of(_plus_9);
      }
    } else {
      String _literal_2 = type.getLiteral();
      String _plus_7 = (_literal_2 + " field ");
      String _plus_8 = (_plus_7 + attrName);
      String _plus_9 = (_plus_8 + "; JSON documents hold no binary or media values");
      throw Unsupported.of(_plus_9);
    }
  }

  private RedisIndex.Attr vectorAttr(final VectorCollection c, final VectorField field) {
    RedisIndex.Attr _xblockexpression = null;
    {
      final String name = RedisIndex.id(this.name(c, field));
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + name) + "; the Query Engine scores a multi-value vector attribute by its nearest vector, not by late interaction (max_sim)"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + RedisSchema.NO_MODELS));
      }
      boolean _isOnDisk = field.isOnDisk();
      if (_isOnDisk) {
        throw Unsupported.of(RedisSchema.ON_DISK);
      }
      this.declaredNote(field, "normalized", "nullable");
      final String type = RedisSchema.VECTOR_TYPES.get(field.getType().getLiteral());
      if ((type == null)) {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " fields; Query Engine vector attributes hold FLOAT32, FLOAT64, FLOAT16, BFLOAT16, INT8 and UINT8 values (no sparse or binary vectors)");
        throw Unsupported.of(_plus);
      }
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim"));
      }
      boolean _isEmpty = field.getLimits().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("field limits; Redis does not validate document values (JSON documents have no constraints)");
      }
      final VectorIndex index = ModelUtil.vectorIndex(c, field);
      RedisIndex.Vec _vec = this.vec(field, index, type);
      _xblockexpression = new RedisIndex.Attr(name, ("$." + name), "VECTOR", _vec, 1.0, false);
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
      final EMetric metric = _xifexpression;
      _xblockexpression = metric.getLiteral();
    }
    return _xblockexpression;
  }

  /**
   * FLAT without a declared index (exact search), HNSW for hnsw and auto, SVS-VAMANA for vamana.
   */
  public RedisIndex.Vec vec(final VectorField field, final VectorIndex index, final String type) {
    RedisIndex.Vec _xblockexpression = null;
    {
      final String metric = this.metric(field, index);
      final Map<String, String> options = CollectionLiterals.<String, String>newLinkedHashMap();
      if ((index == null)) {
        int _dimension = field.getDimension();
        RedisIndex.Vec _vec = new RedisIndex.Vec("FLAT", type, _dimension, metric, options);
        return this.check(_vec);
      }
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; the Query Engine builds and searches vector indexes on the CPU");
      }
      boolean _isOnDisk = index.isOnDisk();
      if (_isOnDisk) {
        throw Unsupported.of(RedisSchema.ON_DISK);
      }
      boolean _written = ModelUtil.written(index, "centroids");
      if (_written) {
        throw Unsupported.of("centroids; the Query Engine has no IVF index (FLAT, HNSW and SVS-VAMANA)");
      }
      Set<String> _keySet = ModelUtil.extParams(index, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of((("extension parameter " + key) + " on a vector index"));
      }
      this.declaredNote(index, "online");
      String _switchResult = null;
      EIndexFamily _family = index.getFamily();
      if (_family != null) {
        switch (_family) {
          case FLAT:
            String _xblockexpression_1 = null;
            {
              if (((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates")) || ModelUtil.written(index, "searchCandidates"))) {
                throw Unsupported.of("graph settings on a flat index; FLAT takes no options");
              }
              _xblockexpression_1 = "FLAT";
            }
            _switchResult = _xblockexpression_1;
            break;
          case AUTO:
          case HNSW:
            String _xblockexpression_2 = null;
            {
              boolean _written_1 = ModelUtil.written(index, "maxDegree");
              if (_written_1) {
                options.put("M", String.valueOf(index.getMaxDegree()));
              }
              boolean _written_2 = ModelUtil.written(index, "buildCandidates");
              if (_written_2) {
                options.put("EF_CONSTRUCTION", String.valueOf(index.getBuildCandidates()));
              }
              boolean _written_3 = ModelUtil.written(index, "searchCandidates");
              if (_written_3) {
                options.put("EF_RUNTIME", String.valueOf(index.getSearchCandidates()));
              }
              _xblockexpression_2 = "HNSW";
            }
            _switchResult = _xblockexpression_2;
            break;
          case VAMANA:
            String _xblockexpression_3 = null;
            {
              if (((!Objects.equals(type, "FLOAT32")) && (!Objects.equals(type, "FLOAT16")))) {
                throw Unsupported.of((("SVS-VAMANA on " + type) + " vectors; SVS-VAMANA indexes FLOAT16 and FLOAT32"));
              }
              boolean _written_1 = ModelUtil.written(index, "maxDegree");
              if (_written_1) {
                options.put("GRAPH_MAX_DEGREE", String.valueOf(index.getMaxDegree()));
              }
              boolean _written_2 = ModelUtil.written(index, "buildCandidates");
              if (_written_2) {
                options.put("CONSTRUCTION_WINDOW_SIZE", String.valueOf(index.getBuildCandidates()));
              }
              boolean _written_3 = ModelUtil.written(index, "searchCandidates");
              if (_written_3) {
                options.put("SEARCH_WINDOW_SIZE", String.valueOf(index.getSearchCandidates()));
              }
              _xblockexpression_3 = "SVS-VAMANA";
            }
            _switchResult = _xblockexpression_3;
            break;
          case IVF:
            throw Unsupported.of("ivf indexes; the Query Engine builds FLAT, HNSW and SVS-VAMANA vector indexes");
          default:
            String _literal = index.getFamily().getLiteral();
            String _plus = (_literal + " indexes; the Query Engine indexes dense vectors only");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = index.getFamily().getLiteral();
        String _plus = (_literal + " indexes; the Query Engine indexes dense vectors only");
        throw Unsupported.of(_plus);
      }
      final String algorithm = _switchResult;
      final Quantization q = index.getQuantization();
      if (((q != null) && (!Objects.equals(q.getType().getLiteral(), "none")))) {
        boolean _notEquals = (!Objects.equals(algorithm, "SVS-VAMANA"));
        if (_notEquals) {
          throw Unsupported.of((("quantization of a " + algorithm) + " index; the Query Engine compresses SVS-VAMANA indexes only (vector sets quantize to Q8 and BIN, but collections are Query Engine indexes)"));
        }
        if (((!Objects.equals(q.getType().getLiteral(), "scalar")) || (ModelUtil.written(q, "bits") && (q.getBits() != 8)))) {
          String _literal_1 = q.getType().getLiteral();
          String _plus_1 = (_literal_1 + " quantization");
          String _xifexpression = null;
          boolean _written_1 = ModelUtil.written(q, "bits");
          if (_written_1) {
            double _bits = q.getBits();
            String _plus_2 = (" to " + Double.valueOf(_bits));
            _xifexpression = (_plus_2 + " bits");
          } else {
            _xifexpression = "";
          }
          String _plus_3 = (_plus_1 + _xifexpression);
          String _plus_4 = (_plus_3 + "; Redis Open Source compresses SVS-VAMANA vectors with 8-bit scalar quantization only (Intel\'s LVQ and LeanVec optimizations are not in Redis Open Source)");
          throw Unsupported.of(_plus_4);
        }
        this.declaredNote(q, "rescore", "oversampling", "keepOriginals", "truncateDimension");
        options.put("COMPRESSION", "LVQ8");
      }
      int _dimension_1 = field.getDimension();
      RedisIndex.Vec _vec_1 = new RedisIndex.Vec(algorithm, type, _dimension_1, metric, options);
      _xblockexpression = this.check(_vec_1);
    }
    return _xblockexpression;
  }

  private RedisIndex.Vec check(final RedisIndex.Vec vec) {
    RedisIndex.Vec _xblockexpression = null;
    {
      vec.distance();
      _xblockexpression = vec;
    }
    return _xblockexpression;
  }

  /**
   * Index attributes a payload index asks for are the field's own; a different kind is not generated.
   */
  public void checkPayloadIndex(final VectorCollection c, final PayloadIndex payloadIndex) {
    final FieldPath path = payloadIndex.getTarget();
    String _rawPath = path.getRawPath();
    boolean _tripleNotEquals = (_rawPath != null);
    if (_tripleNotEquals) {
      throw Unsupported.of("a payload index on a raw path; the generator indexes declared fields and their sub-fields");
    }
    Field _field = path.getField();
    boolean _not = (!(_field instanceof PayloadField));
    if (_not) {
      throw Unsupported.of("a payload index on a vector field");
    }
    Set<String> _keySet = ModelUtil.extParams(payloadIndex, RedisGenerator.VENDOR).keySet();
    for (final String key : _keySet) {
      throw Unsupported.of((("extension parameter " + key) + " on a payload index"));
    }
    boolean _isOnDisk = payloadIndex.isOnDisk();
    if (_isOnDisk) {
      throw Unsupported.of(RedisSchema.ON_DISK);
    }
    this.note(payloadIndex, "structure", "lowercase", "minTokenLength", "maxTokenLength", "prefixMatch");
    String _name = this.name(c, path.getField());
    final Function1<Field, String> _function = (Field it) -> {
      return ModelUtil.fieldName(it);
    };
    List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
    final String attrName = IterableExtensions.join(Iterables.<String>concat(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_name)), _map), "_");
    final String kind = this.index(c).attr(attrName).kind();
    String _switchResult = null;
    EPayloadIndexKind _kind = payloadIndex.getKind();
    if (_kind != null) {
      switch (_kind) {
        case KEYWORD:
        case BOOL:
        case UUID:
          _switchResult = "TAG";
          break;
        case INTEGER:
        case FLOAT:
          _switchResult = "NUMERIC";
          break;
        case FULL_TEXT:
          _switchResult = "TEXT";
          break;
        case DATETIME:
          _switchResult = "TAG";
          break;
        case GEO:
          throw Unsupported.of("geo payload indexes; has no Redis mapping yet (GEO attributes)");
        case JSON:
          throw Unsupported.of("json payload indexes; the Query Engine indexes JSON values through attributes on their paths (index the sub-fields)");
        default:
          break;
      }
    }
    final String expected = _switchResult;
    boolean _notEquals = (!Objects.equals(kind, expected));
    if (_notEquals) {
      String _literal = payloadIndex.getKind().getLiteral();
      String _plus = ("a " + _literal);
      String _plus_1 = (_plus + " index on ");
      String _plus_2 = (_plus_1 + attrName);
      String _plus_3 = (_plus_2 + ", which is a ");
      String _plus_4 = (_plus_3 + kind);
      String _plus_5 = (_plus_4 + " attribute; has no Redis mapping yet (a second attribute on the same path)");
      throw Unsupported.of(_plus_5);
    }
  }

  /**
   * FT.CREATE of the collection's index; if-not-exists ignores an existing index.
   */
  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(RedisStatements.EXTERNAL);
      }
      Field _ttlField = c.getTtlField();
      boolean _tripleNotEquals_1 = (_ttlField != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("ttl-field; has no Redis mapping yet (EXPIREAT from each record\'s field value)");
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; has no Redis mapping yet (keys from an INCR counter)");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(RedisStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; Redis keys hold no references to other keys, and the Query Engine searches one index per query");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_2 = (_embeddingModel != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of(("embedding-model" + RedisSchema.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_3 = (_storage != null);
      if (_tripleNotEquals_3) {
        throw Unsupported.of(RedisStatements.NO_STORAGE);
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(RedisStatements.NO_TAGS);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<org.ftn.vecDSL.vecDSL.Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = RedisStatements.functionReason(IterableExtensions.<org.ftn.vecDSL.vecDSL.Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        throw Unsupported.of(RedisStatements.collectionLimit(IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral()));
      }
      Set<String> _keySet = ModelUtil.extParams(c, RedisGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of((("extension parameter " + key) + " on a collection"));
      }
      final Function1<Partition, Boolean> _function = (Partition it) -> {
        return Boolean.valueOf((((it.getKey() != null) || (!it.getSubPartitions().isEmpty())) || (ModelUtil.written(it, "strategy") && (!Objects.equals(it.getStrategy().getLiteral(), "list")))));
      };
      boolean _isEmpty_4 = IterableExtensions.isEmpty(IterableExtensions.<Partition>filter(ModelUtil.partitions(c), _function));
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        throw Unsupported.of("partitions by key, with a strategy other than list, or with sub-partitions; a partition is a value of the hidden vecdsl_partition tag");
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "consistency", "dynamic", "description");
      if (fresh) {
        this.evolve(c, null);
      }
      this.declaring = true;
      RedisIndex.Index _xtrycatchfinallyexpression = null;
      try {
        RedisIndex.Index _index = this.index(c);
        _xtrycatchfinallyexpression = _index;
      } finally {
        this.declaring = false;
      }
      final RedisIndex.Index current = _xtrycatchfinallyexpression;
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex payloadIndex : _filter) {
        this.checkPayloadIndex(c, payloadIndex);
      }
      final List<Code.Stmt> result = CollectionLiterals.<Code.Stmt>newArrayList();
      List<String> _definition = RedisIndex.definition(current);
      List<String> _xifexpression = null;
      if (ifNotExists) {
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("index already exists"));
      } else {
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
      }
      Code.Call _command = RedisSchema.command(_definition, _xifexpression);
      Code.Run _run = new Code.Run(_command);
      result.add(_run);
      if ((fresh || (!ifNotExists))) {
        EList<Alias> _aliases = c.getAliases();
        for (final Alias alias : _aliases) {
          String _id = RedisIndex.id(alias.getName());
          String _name = current.name();
          List<String> _xifexpression_1 = null;
          if (ifNotExists) {
            _xifexpression_1 = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("alias already exists"));
          } else {
            _xifexpression_1 = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
          }
          Code.Call _command_1 = RedisSchema.command(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("FT.ALIASADD", _id, _name)), _xifexpression_1);
          Code.Run _run_1 = new Code.Run(_command_1);
          result.add(_run_1);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Settings of a declaration that the index ignores, listed only on the statement that creates the collection.
   */
  private void declaredNote(final EObject host, final String... features) {
    if (this.declaring) {
      this.note(host, features);
    }
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

  public static Code.Call command(final List<String> args, final List<String> ignore) {
    Code.Call _xblockexpression = null;
    {
      final Code.Entries call = RedisSchema.kw("args", Code.texts(args));
      boolean _isEmpty = ignore.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        call.put("ignore", Code.texts(ignore));
      }
      _xblockexpression = Code.runtime("command", call);
    }
    return _xblockexpression;
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
