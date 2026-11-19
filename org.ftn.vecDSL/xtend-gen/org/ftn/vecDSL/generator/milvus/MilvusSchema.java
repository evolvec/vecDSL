package org.ftn.vecDSL.generator.milvus;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.EAccelerator;
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType;
import org.ftn.vecDSL.vecDSL.EConsistencyLevel;
import org.ftn.vecDSL.vecDSL.EFieldLimit;
import org.ftn.vecDSL.vecDSL.EFunctionType;
import org.ftn.vecDSL.vecDSL.EIndexFamily;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EPayloadIndexKind;
import org.ftn.vecDSL.vecDSL.EPayloadIndexStructure;
import org.ftn.vecDSL.vecDSL.EQuantizationKind;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.ETokenizer;
import org.ftn.vecDSL.vecDSL.EVectorDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Namespace;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.StringLit;
import org.ftn.vecDSL.vecDSL.Tenant;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections, fields, functions and indexes to Milvus schema, function and index definitions.
 */
@SuppressWarnings("all")
public class MilvusSchema {
  private static final Set<String> COLLECTION_ARGUMENTS = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("num_shards", "num_partitions"));

  private final List<String> notes;

  public MilvusSchema(final List<String> notes) {
    this.notes = notes;
  }

  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists) {
    ArrayList<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("the derived-from clause has no Milvus mapping yet");
      }
      Field _ttlField = c.getTtlField();
      boolean _tripleNotEquals_1 = (_ttlField != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of("ttl-field has no Milvus mapping yet");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; Milvus collections hold no references to other collections");
      }
      this.note(c, "tags", "deletionProtection", "storage", "storedSourceFields", "embeddingModel", "generativeModel", "limits");
      final List<Code.Stmt> statements = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _client = Code.client("create_collection", this.collectionArgs(c));
      Code.Run _run = new Code.Run(_client);
      statements.add(_run);
      List<String> _manualPartitions = this.manualPartitions(c);
      for (final String partition : _manualPartitions) {
        Code.Call _client_1 = Code.client("create_partition", MilvusSchema.kw("collection_name", Code.text(c.getName()), "partition_name", Code.text(partition)));
        Code.Run _run_1 = new Code.Run(_client_1);
        statements.add(_run_1);
      }
      EList<Alias> _aliases = c.getAliases();
      for (final Alias alias : _aliases) {
        Code.Call _client_2 = Code.client("create_alias", MilvusSchema.kw("collection_name", Code.text(c.getName()), "alias", Code.text(alias.getName())));
        Code.Run _run_2 = new Code.Run(_client_2);
        statements.add(_run_2);
      }
      if ((!ifNotExists)) {
        return statements;
      }
      Code.Call _runtime = Code.runtime("has_collection", MilvusSchema.kw("collection_name", Code.text(c.getName())));
      Code.When _when = new Code.When(_runtime, true, statements);
      _xblockexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
    }
    return _xblockexpression;
  }

  public Code.Make schema(final VectorCollection c) {
    Code.Make _xblockexpression = null;
    {
      final Code.Entries args = Code.entries();
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        args.put("auto_id", Code.flag(true));
      }
      boolean _isDynamic = c.isDynamic();
      if (_isDynamic) {
        args.put("enable_dynamic_field", Code.flag(true));
      }
      String _description = c.getDescription();
      boolean _tripleNotEquals = (_description != null);
      if (_tripleNotEquals) {
        args.put("description", Code.text(c.getDescription()));
      }
      final Function1<Field, Code.Make> _function = (Field it) -> {
        return Code.make("Field", this.fieldArgs(c, it));
      };
      args.put("fields", Code.items(ListExtensions.<Field, Code.Make>map(c.getFields(), _function)));
      final List<Code.Make> functions = this.functions(c);
      boolean _isEmpty = functions.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("functions", Code.items(functions));
      }
      _xblockexpression = Code.make("Schema", args);
    }
    return _xblockexpression;
  }

  public Code.Entries fieldArgs(final VectorCollection c, final Field field) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("field_name", Code.text(ModelUtil.fieldName(field)));
      boolean _matched = false;
      if (field instanceof VectorField) {
        _matched=true;
        this.vectorFieldArgs(((VectorField)field), args);
      }
      if (!_matched) {
        if (field instanceof PayloadField) {
          _matched=true;
          this.payloadFieldArgs(c, ((PayloadField)field), args);
        }
      }
      args.entries().putAll(this.extValues(field));
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  private void vectorFieldArgs(final VectorField field, final Code.Entries args) {
    boolean _isMultiVector = field.isMultiVector();
    if (_isMultiVector) {
      throw Unsupported.of("multi-vector fields");
    }
    args.put("datatype", this.dataType(this.vectorType(field.getType())));
    EVectorDType _type = field.getType();
    boolean _notEquals = (!Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR));
    if (_notEquals) {
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus = ("vector field " + _fieldName);
        String _plus_1 = (_plus + " without dim");
        throw Unsupported.of(_plus_1);
      }
      args.put("dim", Code.num(field.getDimension()));
    }
    boolean _isNullable = field.isNullable();
    if (_isNullable) {
      args.put("nullable", Code.flag(true));
    }
    String _description = field.getDescription();
    boolean _tripleNotEquals = (_description != null);
    if (_tripleNotEquals) {
      args.put("description", Code.text(field.getDescription()));
    }
    this.note(field, "onDisk", "limits");
  }

  private void payloadFieldArgs(final VectorCollection c, final PayloadField field, final Code.Entries args) {
    final String type = this.scalarType(field.getType());
    args.put("datatype", this.dataType(type));
    Field _primaryKey = c.getPrimaryKey();
    boolean _tripleEquals = (field == _primaryKey);
    if (_tripleEquals) {
      if (((!Objects.equals(type, "INT64")) && (!Objects.equals(type, "VARCHAR")))) {
        String _literal = field.getType().getLiteral();
        String _plus = ("primary key of type " + _literal);
        throw Unsupported.of(_plus);
      }
      args.put("is_primary", Code.flag(true));
    }
    boolean _equals = Objects.equals(type, "VARCHAR");
    if (_equals) {
      args.put("max_length", Code.num(this.maxLength(field)));
    }
    boolean _equals_1 = Objects.equals(type, "ARRAY");
    if (_equals_1) {
      boolean _written = ModelUtil.written(field, "elementType");
      boolean _not = (!_written);
      if (_not) {
        String _name = field.getName();
        String _plus_1 = ("array field " + _name);
        String _plus_2 = (_plus_1 + " without element-type");
        throw Unsupported.of(_plus_2);
      }
      final String element = this.scalarType(field.getElementType());
      boolean _contains = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("ARRAY", "JSON", "GEOMETRY")).contains(element);
      if (_contains) {
        String _literal_1 = field.getElementType().getLiteral();
        String _plus_3 = ("arrays of " + _literal_1);
        throw Unsupported.of(_plus_3);
      }
      args.put("element_type", this.dataType(element));
      args.put("max_capacity", Code.num(this.limit(field, EFieldLimit.ARRAY_LEN, 4096)));
      boolean _equals_2 = Objects.equals(element, "VARCHAR");
      if (_equals_2) {
        args.put("max_length", Code.num(this.maxLength(field)));
      }
    }
    if (((!field.getSubFields().isEmpty()) && (!Objects.equals(type, "JSON")))) {
      String _literal_2 = field.getType().getLiteral();
      String _plus_4 = ("sub-fields on " + _literal_2);
      String _plus_5 = (_plus_4 + " fields");
      throw Unsupported.of(_plus_5);
    }
    boolean _isNullable = field.isNullable();
    if (_isNullable) {
      args.put("nullable", Code.flag(true));
    }
    Literal _defaultValue = field.getDefaultValue();
    boolean _tripleNotEquals = (_defaultValue != null);
    if (_tripleNotEquals) {
      args.put("default_value", Literals.of(field.getDefaultValue()));
    }
    if ((field.isPartitionKey() || (ModelUtil.partitionKey(c) == field))) {
      args.put("is_partition_key", Code.flag(true));
    }
    final Function1<Function, Boolean> _function = (Function it) -> {
      return Boolean.valueOf((Objects.equals(it.getType(), EFunctionType.BM25) && it.getInputs().contains(field)));
    };
    final boolean bm25Input = IterableExtensions.<Function>exists(c.getFunctions(), _function);
    if (((field.isFullText() || (field.getAnalyzer() != null)) || bm25Input)) {
      args.put("enable_analyzer", Code.flag(true));
      Analyzer _analyzer = field.getAnalyzer();
      boolean _tripleNotEquals_1 = (_analyzer != null);
      if (_tripleNotEquals_1) {
        args.put("analyzer_params", this.analyzerParams(field.getAnalyzer()));
      }
    }
    boolean _isFullText = field.isFullText();
    if (_isFullText) {
      args.put("enable_match", Code.flag(true));
    }
    String _description = field.getDescription();
    boolean _tripleNotEquals_2 = (_description != null);
    if (_tripleNotEquals_2) {
      args.put("description", Code.text(field.getDescription()));
    }
    this.note(field, "nested", "documentBody");
  }

  private int maxLength(final PayloadField field) {
    int _xblockexpression = (int) 0;
    {
      boolean _written = ModelUtil.written(field, "maxLength");
      if (_written) {
        return field.getMaxLength();
      }
      EScalarDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EScalarDType.UUID);
      if (_equals) {
        return 36;
      }
      _xblockexpression = this.limit(field, EFieldLimit.MAX_SIZE, 65535);
    }
    return _xblockexpression;
  }

  private int limit(final PayloadField field, final EFieldLimit kind, final int fallback) {
    int _xblockexpression = (int) 0;
    {
      final Function1<FieldLimit, Boolean> _function = (FieldLimit it) -> {
        EFieldLimit _type = it.getType();
        return Boolean.valueOf(Objects.equals(_type, kind));
      };
      final FieldLimit declared = IterableExtensions.<FieldLimit>findFirst(field.getLimits(), _function);
      int _xifexpression = (int) 0;
      if ((declared == null)) {
        _xifexpression = fallback;
      } else {
        _xifexpression = declared.getValue();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public String scalarType(final EScalarDType type) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case INT:
          _switchResult = "INT64";
          break;
        case BIGINT:
          _switchResult = "INT64";
          break;
        case INT8:
          _switchResult = "INT8";
          break;
        case FLOAT:
          _switchResult = "FLOAT";
          break;
        case DOUBLE:
          _switchResult = "DOUBLE";
          break;
        case BOOL:
          _switchResult = "BOOL";
          break;
        case VARCHAR:
          _switchResult = "VARCHAR";
          break;
        case TEXT:
          _switchResult = "VARCHAR";
          break;
        case UUID:
          _switchResult = "VARCHAR";
          break;
        case DATE:
          _switchResult = "TIMESTAMPTZ";
          break;
        case DATETIME:
          _switchResult = "TIMESTAMPTZ";
          break;
        case TIMESTAMPTZ:
          _switchResult = "TIMESTAMPTZ";
          break;
        case JSON:
          _switchResult = "JSON";
          break;
        case MAP:
          _switchResult = "JSON";
          break;
        case STRUCT:
          _switchResult = "JSON";
          break;
        case ARRAY:
          _switchResult = "ARRAY";
          break;
        case GEO_POINT:
          _switchResult = "GEOMETRY";
          break;
        case GEOMETRY:
          _switchResult = "GEOMETRY";
          break;
        default:
          String _literal = type.getLiteral();
          String _plus = (_literal + " fields");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = type.getLiteral();
      String _plus = (_literal + " fields");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  public String vectorType(final EVectorDType type) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case FLOAT_VECTOR:
          _switchResult = "FLOAT_VECTOR";
          break;
        case FLOAT16_VECTOR:
          _switchResult = "FLOAT16_VECTOR";
          break;
        case BFLOAT16_VECTOR:
          _switchResult = "BFLOAT16_VECTOR";
          break;
        case INT8_VECTOR:
          _switchResult = "INT8_VECTOR";
          break;
        case BINARY_VECTOR:
          _switchResult = "BINARY_VECTOR";
          break;
        case SPARSE_FLOAT_VECTOR:
          _switchResult = "SPARSE_FLOAT_VECTOR";
          break;
        default:
          String _literal = type.getLiteral();
          String _plus = (_literal + " fields");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = type.getLiteral();
      String _plus = (_literal + " fields");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Code.Expr dataType(final String name) {
    return new Code.Sym("DataType", name);
  }

  public List<Code.Make> functions(final VectorCollection c) {
    List<Code.Make> _xblockexpression = null;
    {
      final List<Code.Make> result = CollectionLiterals.<Code.Make>newArrayList();
      final Function1<Function, Code.Make> _function = (Function it) -> {
        return this.function(it);
      };
      List<Code.Make> _map = ListExtensions.<Function, Code.Make>map(c.getFunctions(), _function);
      Iterables.<Code.Make>addAll(result, _map);
      final Function1<VectorField, Boolean> _function_1 = (VectorField it) -> {
        return Boolean.valueOf(((it.getModel() != null) && (!it.getSourceFields().isEmpty())));
      };
      Iterable<VectorField> _filter = IterableExtensions.<VectorField>filter(ModelUtil.vectorFields(c), _function_1);
      for (final VectorField field : _filter) {
        Function _producingFunction = ModelUtil.producingFunction(c, field);
        boolean _tripleEquals = (_producingFunction == null);
        if (_tripleEquals) {
          int _size = field.getSourceFields().size();
          boolean _greaterThan = (_size > 1);
          if (_greaterThan) {
            throw Unsupported.of("embedding a vector field from several fields");
          }
          String _fieldName = ModelUtil.fieldName(field);
          String _plus = (_fieldName + "_embedding");
          Code.Make _embedding = this.embedding(_plus, field.getSourceFields(), Collections.<Field>unmodifiableList(CollectionLiterals.<Field>newArrayList(field)), field.getModel(), field);
          result.add(_embedding);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public Code.Make function(final Function function) {
    Code.Make _switchResult = null;
    EFunctionType _type = function.getType();
    if (_type != null) {
      switch (_type) {
        case BM25:
          Code.Text _text = Code.text(function.getName());
          Code.Sym _sym = new Code.Sym("FunctionType", "BM25");
          final Function1<Field, String> _function = (Field it) -> {
            return ModelUtil.fieldName(it);
          };
          final Function1<Field, String> _function_1 = (Field it) -> {
            return ModelUtil.fieldName(it);
          };
          _switchResult = Code.make("Function", MilvusSchema.kw(
            "name", _text, 
            "function_type", _sym, 
            "input_field_names", Code.texts(ListExtensions.<Field, String>map(function.getInputs(), _function)), 
            "output_field_names", Code.texts(ListExtensions.<Field, String>map(function.getOutputs(), _function_1))));
          break;
        case TEXT_EMBEDDING:
          _switchResult = this.embedding(function.getName(), function.getInputs(), function.getOutputs(), function.getModel(), function);
          break;
        default:
          String _literal = function.getType().getLiteral();
          String _plus = (_literal + " functions");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = function.getType().getLiteral();
      String _plus = (_literal + " functions");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Code.Make embedding(final String name, final List<Field> inputs, final List<? extends Field> outputs, final EmbeddingModel model, final EObject host) {
    Code.Make _xblockexpression = null;
    {
      if ((((model == null) || (model.getProvider() == null)) || (model.getModelId() == null))) {
        throw Unsupported.of("text embedding without an embedding model with provider and model-id");
      }
      final Code.Entries params = MilvusSchema.kw("provider", Code.text(model.getProvider()), "model_name", Code.text(model.getModelId()));
      params.entries().putAll(this.extValues(model));
      params.entries().putAll(this.extValues(host));
      Code.Text _text = Code.text(name);
      Code.Sym _sym = new Code.Sym("FunctionType", "TEXTEMBEDDING");
      final Function1<Field, String> _function = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      final Function1<Field, String> _function_1 = (Field it) -> {
        return ModelUtil.fieldName(it);
      };
      _xblockexpression = Code.make("Function", MilvusSchema.kw(
        "name", _text, 
        "function_type", _sym, 
        "input_field_names", Code.texts(ListExtensions.<Field, String>map(inputs, _function)), 
        "output_field_names", Code.texts(ListExtensions.map(outputs, _function_1)), 
        "params", params));
    }
    return _xblockexpression;
  }

  public Code.Entries analyzerParams(final Analyzer analyzer) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = Code.entries();
      ETokenizer _tokenizer = analyzer.getTokenizer();
      boolean _equals = Objects.equals(_tokenizer, ETokenizer.LANGUAGE);
      if (_equals) {
        result.put("type", Code.text(this.language(analyzer.getLanguage())));
      } else {
        result.put("tokenizer", Code.text(this.tokenizer(analyzer.getTokenizer())));
      }
      final List<Code.Expr> filters = CollectionLiterals.<Code.Expr>newArrayList();
      final Function1<AnalyzerFilter, Code.Expr> _function = (AnalyzerFilter it) -> {
        return this.filter(it, analyzer);
      };
      List<Code.Expr> _map = ListExtensions.<AnalyzerFilter, Code.Expr>map(analyzer.getFilters(), _function);
      Iterables.<Code.Expr>addAll(filters, _map);
      if (((analyzer.getStopwords() != null) && (!IterableExtensions.<AnalyzerFilter>exists(analyzer.getFilters(), ((Function1<AnalyzerFilter, Boolean>) (AnalyzerFilter it) -> {
        EAnalyzerFilterType _type = it.getType();
        return Boolean.valueOf(Objects.equals(_type, EAnalyzerFilterType.STOP));
      }))))) {
        Code.Expr _stopFilter = this.stopFilter(analyzer);
        filters.add(_stopFilter);
      }
      boolean _isEmpty = filters.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        result.put("filter", Code.items(filters));
      }
      result.entries().putAll(this.extValues(analyzer));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String tokenizer(final ETokenizer tokenizer) {
    String _switchResult = null;
    if (tokenizer != null) {
      switch (tokenizer) {
        case STANDARD:
          _switchResult = "standard";
          break;
        case WHITESPACE:
          _switchResult = "whitespace";
          break;
        case ICU:
          _switchResult = "icu";
          break;
        default:
          String _literal = tokenizer.getLiteral();
          String _plus = (_literal + " tokenizer");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = tokenizer.getLiteral();
      String _plus = (_literal + " tokenizer");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private String language(final String language) {
    String _switchResult = null;
    String _elvis = null;
    if (language != null) {
      _elvis = language;
    } else {
      _elvis = "english";
    }
    String _lowerCase = _elvis.toLowerCase();
    if (_lowerCase != null) {
      switch (_lowerCase) {
        case "en":
          _switchResult = "english";
          break;
        case "english":
          _switchResult = "english";
          break;
        case "zh":
          _switchResult = "chinese";
          break;
        case "chinese":
          _switchResult = "chinese";
          break;
        default:
          throw Unsupported.of(("language analyzer " + language));
      }
    } else {
      throw Unsupported.of(("language analyzer " + language));
    }
    return _switchResult;
  }

  private Code.Expr filter(final AnalyzerFilter filter, final Analyzer analyzer) {
    Code.Expr _switchResult = null;
    EAnalyzerFilterType _type = filter.getType();
    if (_type != null) {
      switch (_type) {
        case LOWERCASE:
          _switchResult = Code.text("lowercase");
          break;
        case ASCII_FOLDING:
          _switchResult = Code.text("asciifolding");
          break;
        case STOP:
          _switchResult = this.stopFilter(analyzer);
          break;
        case STEMMER:
          _switchResult = MilvusSchema.kw("type", Code.text("stemmer"), "language", Code.text(this.language(analyzer.getLanguage())));
          break;
        default:
          String _literal = filter.getType().getLiteral();
          String _plus = (_literal + " token filter");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = filter.getType().getLiteral();
      String _plus = (_literal + " token filter");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Code.Expr stopFilter(final Analyzer analyzer) {
    Code.Entries _xblockexpression = null;
    {
      String _elvis = null;
      String _stopwords = analyzer.getStopwords();
      if (_stopwords != null) {
        _elvis = _stopwords;
      } else {
        _elvis = "english";
      }
      final String words = _elvis;
      String _xifexpression = null;
      boolean _startsWith = words.startsWith("_");
      if (_startsWith) {
        _xifexpression = words;
      } else {
        _xifexpression = (("_" + words) + "_");
      }
      _xblockexpression = MilvusSchema.kw("type", Code.text("stop"), "stop_words", Code.texts(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_xifexpression))));
    }
    return _xblockexpression;
  }

  public Code.Make vectorIndex(final VectorCollection c, final VectorField field, final VectorIndex index) {
    Code.Make _xblockexpression = null;
    {
      final String type = this.indexType(field, index);
      final Code.Entries args = MilvusSchema.kw("field_name", Code.text(ModelUtil.fieldName(field)));
      if ((index != null)) {
        args.put("index_name", Code.text(index.getName()));
      }
      args.put("index_type", Code.text(type));
      args.put("metric_type", Code.text(this.metric(c, field, index)));
      final Map<String, Code.Expr> params = this.indexParams(c, field, index, type);
      boolean _isEmpty = params.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("params", Code.entries(params));
      }
      _xblockexpression = Code.make("Index", args);
    }
    return _xblockexpression;
  }

  public String indexType(final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      final String override = this.extString(index, "index_type");
      if ((override != null)) {
        return override;
      }
      if ((index == null)) {
        String _xifexpression = null;
        EVectorDType _type = field.getType();
        boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
        if (_equals) {
          _xifexpression = "SPARSE_INVERTED_INDEX";
        } else {
          _xifexpression = "AUTOINDEX";
        }
        return _xifexpression;
      }
      EQuantizationKind _xifexpression_1 = null;
      Quantization _quantization = index.getQuantization();
      boolean _tripleEquals = (_quantization == null);
      if (_tripleEquals) {
        _xifexpression_1 = EQuantizationKind.NONE;
      } else {
        _xifexpression_1 = index.getQuantization().getType();
      }
      final EQuantizationKind quantization = _xifexpression_1;
      EAccelerator _accelerator = index.getAccelerator();
      final boolean gpu = Objects.equals(_accelerator, EAccelerator.GPU);
      EVectorDType _type_1 = field.getType();
      final boolean binary = Objects.equals(_type_1, EVectorDType.BINARY_VECTOR);
      String _switchResult = null;
      EIndexFamily _family = index.getFamily();
      if (_family != null) {
        switch (_family) {
          case AUTO:
            _switchResult = "AUTOINDEX";
            break;
          case FLAT:
            String _xifexpression_2 = null;
            if (gpu) {
              _xifexpression_2 = "GPU_BRUTE_FORCE";
            } else {
              String _xifexpression_3 = null;
              if (binary) {
                _xifexpression_3 = "BIN_FLAT";
              } else {
                _xifexpression_3 = "FLAT";
              }
              _xifexpression_2 = _xifexpression_3;
            }
            _switchResult = _xifexpression_2;
            break;
          case HNSW:
            String _xifexpression_4 = null;
            if (gpu) {
              _xifexpression_4 = "GPU_CAGRA";
            } else {
              _xifexpression_4 = this.hnsw(quantization);
            }
            _switchResult = _xifexpression_4;
            break;
          case IVF:
            String _xifexpression_5 = null;
            if (binary) {
              _xifexpression_5 = "BIN_IVF_FLAT";
            } else {
              _xifexpression_5 = this.ivf(quantization, gpu, index);
            }
            _switchResult = _xifexpression_5;
            break;
          case VAMANA:
            String _xifexpression_6 = null;
            if (gpu) {
              throw Unsupported.of("vamana on GPU");
            } else {
              _xifexpression_6 = "DISKANN";
            }
            _switchResult = _xifexpression_6;
            break;
          case SPARSE_INVERTED:
            _switchResult = "SPARSE_INVERTED_INDEX";
            break;
          default:
            break;
        }
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private String hnsw(final EQuantizationKind quantization) {
    String _switchResult = null;
    if (quantization != null) {
      switch (quantization) {
        case NONE:
          _switchResult = "HNSW";
          break;
        case SCALAR:
          _switchResult = "HNSW_SQ";
          break;
        case PRODUCT:
          _switchResult = "HNSW_PQ";
          break;
        default:
          String _literal = quantization.getLiteral();
          String _plus = (_literal + " quantization on HNSW");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = quantization.getLiteral();
      String _plus = (_literal + " quantization on HNSW");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private String ivf(final EQuantizationKind quantization, final boolean gpu, final VectorIndex index) {
    String _switchResult = null;
    if (quantization != null) {
      switch (quantization) {
        case NONE:
          String _xifexpression = null;
          if (gpu) {
            _xifexpression = "GPU_IVF_FLAT";
          } else {
            _xifexpression = "IVF_FLAT";
          }
          _switchResult = _xifexpression;
          break;
        case PRODUCT:
          String _xifexpression_1 = null;
          if (gpu) {
            _xifexpression_1 = "GPU_IVF_PQ";
          } else {
            _xifexpression_1 = "IVF_PQ";
          }
          _switchResult = _xifexpression_1;
          break;
        case SCALAR:
          String _xblockexpression = null;
          {
            if ((gpu || (ModelUtil.written(index.getQuantization(), "bits") && (index.getQuantization().getBits() != 8)))) {
              throw Unsupported.of("scalar quantization other than 8-bit on IVF");
            }
            _xblockexpression = "IVF_SQ8";
          }
          _switchResult = _xblockexpression;
          break;
        default:
          String _literal = quantization.getLiteral();
          String _plus = (_literal + " quantization on IVF");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = quantization.getLiteral();
      String _plus = (_literal + " quantization on IVF");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  public String metric(final VectorCollection c, final VectorField field, final VectorIndex index) {
    String _xblockexpression = null;
    {
      final Function function = ModelUtil.producingFunction(c, field);
      if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
        return "BM25";
      }
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(field, "metric");
      if (_written) {
        _xifexpression = field.getMetric();
      } else {
        EMetric _xifexpression_1 = null;
        if (((index != null) && ModelUtil.written(index, "metric"))) {
          _xifexpression_1 = index.getMetric();
        } else {
          EMetric _xifexpression_2 = null;
          EVectorDType _type = field.getType();
          boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
          if (_equals) {
            _xifexpression_2 = EMetric.DOT;
          } else {
            EMetric _xifexpression_3 = null;
            EVectorDType _type_1 = field.getType();
            boolean _equals_1 = Objects.equals(_type_1, EVectorDType.BINARY_VECTOR);
            if (_equals_1) {
              _xifexpression_3 = EMetric.HAMMING;
            } else {
              _xifexpression_3 = EMetric.COSINE;
            }
            _xifexpression_2 = _xifexpression_3;
          }
          _xifexpression_1 = _xifexpression_2;
        }
        _xifexpression = _xifexpression_1;
      }
      final EMetric metric = _xifexpression;
      _xblockexpression = this.metricName(metric);
    }
    return _xblockexpression;
  }

  public String metricName(final EMetric metric) {
    String _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = "COSINE";
          break;
        case EUCLIDEAN:
          _switchResult = "L2";
          break;
        case DOT:
          _switchResult = "IP";
          break;
        case HAMMING:
          _switchResult = "HAMMING";
          break;
        case JACCARD:
          _switchResult = "JACCARD";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  private Map<String, Code.Expr> indexParams(final VectorCollection c, final VectorField field, final VectorIndex index, final String type) {
    LinkedHashMap<String, Code.Expr> _xblockexpression = null;
    {
      final LinkedHashMap<String, Code.Expr> params = new LinkedHashMap<String, Code.Expr>();
      if ((index != null)) {
        boolean _startsWith = type.startsWith("HNSW");
        if (_startsWith) {
          boolean _written = ModelUtil.written(index, "maxDegree");
          if (_written) {
            params.put("M", Code.num(index.getMaxDegree()));
          }
          boolean _written_1 = ModelUtil.written(index, "buildCandidates");
          if (_written_1) {
            params.put("efConstruction", Code.num(index.getBuildCandidates()));
          }
        }
        if ((type.contains("IVF") && ModelUtil.written(index, "centroids"))) {
          params.put("nlist", Code.num(index.getCentroids()));
        }
        final Quantization quantization = index.getQuantization();
        if ((quantization != null)) {
          boolean _equals = Objects.equals(type, "HNSW_SQ");
          if (_equals) {
            params.put("sq_type", Code.text(this.sqType(quantization.getBits(), ModelUtil.written(quantization, "bits"))));
          }
          if ((type.endsWith("_PQ") && ModelUtil.written(quantization, "subvectors"))) {
            params.put("m", Code.num(quantization.getSubvectors()));
          }
          this.note(quantization, "rescore", "oversampling", "keepOriginals", "truncateDimension");
          params.putAll(this.extValues(quantization));
        }
        this.note(index, "onDisk", "online", "searchCandidates");
        final Function2<String, Code.Expr, Boolean> _function = (String key, Code.Expr value) -> {
          return Boolean.valueOf((!Objects.equals(key, "index_type")));
        };
        params.putAll(MapExtensions.<String, Code.Expr>filter(this.extValues(index), _function));
      }
      final Function function = ModelUtil.producingFunction(c, field);
      if (((function != null) && Objects.equals(function.getType(), EFunctionType.BM25))) {
        Analyzer _elvis = null;
        Analyzer _analyzer = function.getAnalyzer();
        if (_analyzer != null) {
          _elvis = _analyzer;
        } else {
          final Function1<PayloadField, Analyzer> _function_1 = (PayloadField it) -> {
            return it.getAnalyzer();
          };
          Analyzer _head = IterableExtensions.<Analyzer>head(IterableExtensions.<Analyzer>filterNull(IterableExtensions.<PayloadField, Analyzer>map(Iterables.<PayloadField>filter(function.getInputs(), PayloadField.class), _function_1)));
          _elvis = _head;
        }
        final Analyzer analyzer = _elvis;
        if (((analyzer != null) && ModelUtil.written(analyzer, "bm25K1"))) {
          params.put("bm25_k1", Code.num(analyzer.getBm25K1()));
        }
        if (((analyzer != null) && ModelUtil.written(analyzer, "bm25B"))) {
          params.put("bm25_b", Code.num(analyzer.getBm25B()));
        }
      }
      _xblockexpression = params;
    }
    return _xblockexpression;
  }

  private String sqType(final double bits, final boolean given) {
    String _xblockexpression = null;
    {
      if ((!given)) {
        return "SQ8";
      }
      String _switchResult = null;
      final int _switchValue = ((int) bits);
      switch (_switchValue) {
        case 8:
          _switchResult = "SQ8";
          break;
        case 6:
          _switchResult = "SQ6";
          break;
        case 4:
          _switchResult = "SQ4";
          break;
        case 16:
          _switchResult = "FP16";
          break;
        default:
          String _plus = (Double.valueOf(bits) + "-bit scalar quantization");
          throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  public Code.Make payloadIndex(final PayloadIndex index) {
    Code.Make _xblockexpression = null;
    {
      EPayloadIndexKind _kind = index.getKind();
      boolean _equals = Objects.equals(_kind, EPayloadIndexKind.FULL_TEXT);
      if (_equals) {
        String _name = index.getName();
        String _plus = ("full_text payload index " + _name);
        String _plus_1 = (_plus + " (declare the field full-text)");
        this.notes.add(_plus_1);
        return null;
      }
      final FieldPath target = index.getTarget();
      String _xifexpression = null;
      String _rawPath = target.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        _xifexpression = IterableExtensions.<String>head(((Iterable<String>)Conversions.doWrapArray(target.getRawPath().split("\\."))));
      } else {
        _xifexpression = ModelUtil.fieldName(target.getField());
      }
      final String root = _xifexpression;
      final Code.Entries args = MilvusSchema.kw("field_name", Code.text(root), "index_name", Code.text(index.getName()));
      String _elvis = null;
      String _extString = this.extString(index, "index_type");
      if (_extString != null) {
        _elvis = _extString;
      } else {
        String _payloadIndexType = this.payloadIndexType(index);
        _elvis = _payloadIndexType;
      }
      args.put("index_type", Code.text(_elvis));
      final LinkedHashMap<String, Code.Expr> params = new LinkedHashMap<String, Code.Expr>();
      if (((target.getRawPath() != null) || (!target.getSubFields().isEmpty()))) {
        params.put("json_path", Code.text(MilvusFilter.path(target)));
        params.put("json_cast_type", Code.text(this.castType(index)));
      }
      final Function2<String, Code.Expr, Boolean> _function = (String key, Code.Expr value) -> {
        return Boolean.valueOf((!Objects.equals(key, "index_type")));
      };
      params.putAll(MapExtensions.<String, Code.Expr>filter(this.extValues(index), _function));
      boolean _isEmpty = params.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("params", Code.entries(params));
      }
      this.note(index, "analyzer", "onDisk", "lowercase", "prefixMatch", "minTokenLength", "maxTokenLength");
      _xblockexpression = Code.make("Index", args);
    }
    return _xblockexpression;
  }

  private String payloadIndexType(final PayloadIndex index) {
    String _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(index, "structure");
      if (_written) {
        String _switchResult = null;
        EPayloadIndexStructure _structure = index.getStructure();
        if (_structure != null) {
          switch (_structure) {
            case INVERTED:
              _switchResult = "INVERTED";
              break;
            case BITMAP:
              _switchResult = "BITMAP";
              break;
            case BTREE:
              _switchResult = "STL_SORT";
              break;
            default:
              _switchResult = "AUTOINDEX";
              break;
          }
        } else {
          _switchResult = "AUTOINDEX";
        }
        return _switchResult;
      }
      String _switchResult_1 = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case GEO:
            _switchResult_1 = "RTREE";
            break;
          case JSON:
            _switchResult_1 = "INVERTED";
            break;
          case KEYWORD:
            _switchResult_1 = "INVERTED";
            break;
          default:
            _switchResult_1 = "AUTOINDEX";
            break;
        }
      } else {
        _switchResult_1 = "AUTOINDEX";
      }
      _xblockexpression = _switchResult_1;
    }
    return _xblockexpression;
  }

  private String castType(final PayloadIndex index) {
    String _xblockexpression = null;
    {
      boolean _written = ModelUtil.written(index, "castType");
      boolean _not = (!_written);
      if (_not) {
        return "varchar";
      }
      String _switchResult = null;
      EScalarDType _castType = index.getCastType();
      if (_castType != null) {
        switch (_castType) {
          case BOOL:
            _switchResult = "bool";
            break;
          case VARCHAR:
            _switchResult = "varchar";
            break;
          case TEXT:
            _switchResult = "varchar";
            break;
          case INT:
            _switchResult = "double";
            break;
          case BIGINT:
            _switchResult = "double";
            break;
          case INT8:
            _switchResult = "double";
            break;
          case FLOAT:
            _switchResult = "double";
            break;
          case DOUBLE:
            _switchResult = "double";
            break;
          default:
            String _literal = index.getCastType().getLiteral();
            String _plus = ("JSON index cast to " + _literal);
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = index.getCastType().getLiteral();
        String _plus = ("JSON index cast to " + _literal);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  public List<Code.Make> indexes(final VectorCollection c) {
    List<Code.Make> _xblockexpression = null;
    {
      final List<Code.Make> result = CollectionLiterals.<Code.Make>newArrayList();
      final Function1<VectorField, Code.Make> _function = (VectorField it) -> {
        return this.vectorIndex(c, it, ModelUtil.vectorIndex(c, it));
      };
      List<Code.Make> _map = ListExtensions.<VectorField, Code.Make>map(ModelUtil.vectorFields(c), _function);
      Iterables.<Code.Make>addAll(result, _map);
      final Function1<PayloadIndex, Code.Make> _function_1 = (PayloadIndex it) -> {
        return this.payloadIndex(it);
      };
      Iterable<Code.Make> _filterNull = IterableExtensions.<Code.Make>filterNull(IterableExtensions.<PayloadIndex, Code.Make>map(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), _function_1));
      Iterables.<Code.Make>addAll(result, _filterNull);
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public Code.Entries collectionArgs(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries args = MilvusSchema.kw("collection_name", Code.text(c.getName()), "schema", this.schema(c), "index_params", Code.items(this.indexes(c)));
      boolean _written = ModelUtil.written(c, "consistency");
      if (_written) {
        args.put("consistency_level", this.consistency(c.getConsistency()));
      }
      final LinkedHashMap<String, Code.Expr> properties = new LinkedHashMap<String, Code.Expr>();
      boolean _written_1 = ModelUtil.written(c, "ttlSeconds");
      if (_written_1) {
        properties.put("collection.ttl.seconds", Code.text(String.valueOf(c.getTtlSeconds())));
      }
      Set<Map.Entry<String, Code.Expr>> _entrySet = this.extValues(c).entrySet();
      for (final Map.Entry<String, Code.Expr> entry : _entrySet) {
        boolean _contains = MilvusSchema.COLLECTION_ARGUMENTS.contains(entry.getKey());
        if (_contains) {
          args.put(entry.getKey(), entry.getValue());
        } else {
          properties.put(entry.getKey(), entry.getValue());
        }
      }
      boolean _isEmpty = properties.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        args.put("properties", Code.entries(properties));
      }
      _xblockexpression = args;
    }
    return _xblockexpression;
  }

  public Code.Expr consistency(final EConsistencyLevel level) {
    String _switchResult = null;
    if (level != null) {
      switch (level) {
        case STRONG:
          _switchResult = "Strong";
          break;
        case BOUNDED:
          _switchResult = "Bounded";
          break;
        case SESSION:
          _switchResult = "Session";
          break;
        case EVENTUAL:
          _switchResult = "Eventually";
          break;
        default:
          break;
      }
    }
    return new Code.Sym("ConsistencyLevel", _switchResult);
  }

  public List<String> manualPartitions(final VectorCollection c) {
    List<String> _xblockexpression = null;
    {
      final Field key = ModelUtil.partitionKey(c);
      final List<String> names = CollectionLiterals.<String>newArrayList();
      final Function1<Partition, Boolean> _function = (Partition it) -> {
        return Boolean.valueOf((((!it.isIsDefault()) && (it != c.getDefaultPartition())) && (it.getKey() == null)));
      };
      Iterable<Partition> _filter = IterableExtensions.<Partition>filter(ModelUtil.partitions(c), _function);
      for (final Partition partition : _filter) {
        {
          if ((key != null)) {
            throw Unsupported.of("manual partitions together with a partition key");
          }
          String _name = partition.getName();
          names.add(_name);
        }
      }
      if ((key == null)) {
        final Function1<Tenant, String> _function_1 = (Tenant it) -> {
          return it.getName();
        };
        List<String> _map = ListExtensions.<Tenant, String>map(c.getTenants(), _function_1);
        Iterables.<String>addAll(names, _map);
        final Function1<Namespace, String> _function_2 = (Namespace it) -> {
          return it.getKey();
        };
        List<String> _map_1 = ListExtensions.<Namespace, String>map(c.getNamespaces(), _function_2);
        Iterables.<String>addAll(names, _map_1);
      }
      _xblockexpression = names;
    }
    return _xblockexpression;
  }

  public Map<String, Code.Expr> extValues(final EObject host) {
    LinkedHashMap<String, Code.Expr> _xblockexpression = null;
    {
      final LinkedHashMap<String, Code.Expr> values = new LinkedHashMap<String, Code.Expr>();
      if ((host != null)) {
        Set<Map.Entry<String, Literal>> _entrySet = ModelUtil.extParams(host, MilvusGenerator.VENDOR).entrySet();
        for (final Map.Entry<String, Literal> entry : _entrySet) {
          values.put(entry.getKey(), Literals.of(entry.getValue()));
        }
      }
      _xblockexpression = values;
    }
    return _xblockexpression;
  }

  public String extString(final EObject host, final String key) {
    String _xblockexpression = null;
    {
      if ((host == null)) {
        return null;
      }
      final Literal value = ModelUtil.extParams(host, MilvusGenerator.VENDOR).get(key);
      String _xifexpression = null;
      if ((value instanceof StringLit)) {
        _xifexpression = ((StringLit)value).getValue();
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
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
