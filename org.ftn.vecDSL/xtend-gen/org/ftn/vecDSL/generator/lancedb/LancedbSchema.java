package org.ftn.vecDSL.generator.lancedb;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EAccelerator;
import org.ftn.vecDSL.vecDSL.EAnalyzerFilterType;
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
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to Lance tables (LanceDB 0.41.0, Lance 14.0.2). A collection is one table with a declared Arrow
 * schema: payload fields are typed columns, vector fields fixed-size lists (a multivector field a list of them), nested fields
 * structs. Lance has no primary key; the key column is kept unique by the runtime (merge insert on the key). Tenant, namespace
 * and partition names live in the partition-key field or in hidden text columns. Indexes are created with the table.
 */
@SuppressWarnings("all")
public class LancedbSchema {
  public static final String TENANT_COLUMN = "vecdsl_tenant";

  public static final String NAMESPACE_COLUMN = "vecdsl_namespace";

  public static final String PARTITION_COLUMN = "vecdsl_partition";

  public static final String DESCRIPTION = "lancedb:description";

  public static final String TAGS = "vecdsl:tags";

  public static final String LANCE_SCHEMA = "https://github.com/lance-format/lance/blob/v14.0.2/docs/src/format/table/schema.md";

  private static final Map<EScalarDType, String> SCALARS = Collections.<EScalarDType, String>unmodifiableMap(CollectionLiterals.<EScalarDType, String>newHashMap(Pair.<EScalarDType, String>of(EScalarDType.INT, "int32"), Pair.<EScalarDType, String>of(EScalarDType.BIGINT, "int64"), Pair.<EScalarDType, String>of(EScalarDType.INT8, "int8"), Pair.<EScalarDType, String>of(EScalarDType.FLOAT, "float32"), Pair.<EScalarDType, String>of(EScalarDType.DOUBLE, "float64"), Pair.<EScalarDType, String>of(EScalarDType.BOOL, "bool"), Pair.<EScalarDType, String>of(EScalarDType.VARCHAR, "utf8"), Pair.<EScalarDType, String>of(EScalarDType.TEXT, "utf8"), Pair.<EScalarDType, String>of(EScalarDType.UUID, "utf8"), Pair.<EScalarDType, String>of(EScalarDType.DATE, "date"), Pair.<EScalarDType, String>of(EScalarDType.DATETIME, "timestamp"), Pair.<EScalarDType, String>of(EScalarDType.TIMESTAMPTZ, "timestamptz"), Pair.<EScalarDType, String>of(EScalarDType.BYTES, "binary"), Pair.<EScalarDType, String>of(EScalarDType.IMAGE, "binary"), Pair.<EScalarDType, String>of(EScalarDType.AUDIO, "binary"), Pair.<EScalarDType, String>of(EScalarDType.VIDEO, "binary")));

  private static final List<EScalarDType> KEYS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8, EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final Map<EVectorDType, String> ELEMENTS = Collections.<EVectorDType, String>unmodifiableMap(CollectionLiterals.<EVectorDType, String>newHashMap(Pair.<EVectorDType, String>of(EVectorDType.FLOAT_VECTOR, "float32"), Pair.<EVectorDType, String>of(EVectorDType.FLOAT16_VECTOR, "float16"), Pair.<EVectorDType, String>of(EVectorDType.FLOAT64_VECTOR, "float64"), Pair.<EVectorDType, String>of(EVectorDType.BINARY_VECTOR, "uint8")));

  private static final String NAME = "[A-Za-z0-9_][A-Za-z0-9_.-]*";

  private final List<String> notes;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, LancedbTable> evolved = CollectionLiterals.<VectorCollection, LancedbTable>newHashMap();

  private final Map<Field, String> renamed = CollectionLiterals.<Field, String>newHashMap();

  public LancedbSchema(final List<String> notes, final Script script) {
    this.notes = notes;
    if ((script != null)) {
      Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
      for (final EObject object : _iterable) {
        this.slices(object);
      }
    }
  }

  /**
   * Collections whose statements name a tenant or a namespace get the slice columns.
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

  /**
   * The table as the statements so far leave it; the declaration's notes belong to its create statement only.
   */
  public LancedbTable table(final VectorCollection c) {
    LancedbTable _xblockexpression = null;
    {
      final LancedbTable known = this.evolved.get(c);
      if ((known != null)) {
        return known;
      }
      final int size = this.notes.size();
      final LancedbTable result = this.declared(c);
      while ((this.notes.size() > size)) {
        int _size = this.notes.size();
        int _minus = (_size - 1);
        this.notes.remove(_minus);
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Name and logical database of a collection's table, without the checks of its declaration (drop statements).
   */
  public Code.Entries placeOf(final VectorCollection c) {
    Code.Entries _xblockexpression = null;
    {
      final LancedbTable known = this.evolved.get(c);
      if ((known != null)) {
        return known.place();
      }
      final Code.Entries result = LancedbSchema.kw("name", Code.text(c.getName()));
      final String database = LancedbSchema.databaseOf(c);
      if ((database != null)) {
        result.put("database", Code.text(database));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * Records the table an alter statement leaves; create and drop return the collection to its declaration.
   */
  public void evolve(final VectorCollection c, final LancedbTable table) {
    if ((table != null)) {
      this.evolved.put(c, table);
      return;
    }
    this.evolved.remove(c);
    EList<Field> _fields = c.getFields();
    for (final Field field : _fields) {
      this.renamed.remove(field);
    }
  }

  /**
   * The column of a field as the statements so far leave it (a rename-field changes it).
   */
  public String column(final Field field) {
    String _elvis = null;
    String _get = this.renamed.get(field);
    if (_get != null) {
      _elvis = _get;
    } else {
      String _fieldName = ModelUtil.fieldName(field);
      _elvis = _fieldName;
    }
    return _elvis;
  }

  public void rename(final Field field, final String name) {
    this.renamed.put(field, name);
  }

  private LancedbTable declared(final VectorCollection c) {
    LancedbTable _xblockexpression = null;
    {
      LancedbSchema.checkName(c.getName());
      final Field key = c.getPrimaryKey();
      if (((!(key instanceof PayloadField)) || (!LancedbSchema.KEYS.contains(((PayloadField) key).getType())))) {
        String _xifexpression = null;
        if ((key instanceof PayloadField)) {
          _xifexpression = ((PayloadField) key).getType().getLiteral();
        } else {
          _xifexpression = "vector";
        }
        String _plus = ("a primary key of type " + _xifexpression);
        String _plus_1 = (_plus + "; the generator keys tables by an integer or text column");
        throw Unsupported.of(_plus_1);
      }
      final List<Code.Entries> fields = CollectionLiterals.<Code.Entries>newArrayList();
      final Map<String, String> vectors = CollectionLiterals.<String, String>newLinkedHashMap();
      EList<Field> _fields = c.getFields();
      for (final Field field : _fields) {
        {
          Code.Entries _fieldOf = this.fieldOf(c, field);
          fields.add(_fieldOf);
          if ((field instanceof VectorField)) {
            vectors.put(ModelUtil.fieldName(field), this.metricName(((VectorField)field), ModelUtil.vectorIndex(c, ((VectorField)field))));
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
          _xifexpression_2 = LancedbSchema.TENANT_COLUMN;
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
          _xifexpression_4 = LancedbSchema.NAMESPACE_COLUMN;
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
        _xifexpression_5 = LancedbSchema.PARTITION_COLUMN;
      }
      final String partition = _xifexpression_5;
      Iterable<String> _filterNull = IterableExtensions.<String>filterNull(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(tenant, namespace, partition)));
      for (final String column : _filterNull) {
        final Function1<Code.Entries, Boolean> _function_1 = (Code.Entries f) -> {
          Code.Expr _get = f.get("name");
          String _value = ((Code.Text) _get).value();
          return Boolean.valueOf((!Objects.equals(_value, column)));
        };
        boolean _forall = IterableExtensions.<Code.Entries>forall(fields, _function_1);
        if (_forall) {
          Code.Entries _kw = LancedbSchema.kw("name", Code.text(column), "type", Code.text("utf8"));
          fields.add(_kw);
        }
      }
      final List<Code.Entries> indexes = CollectionLiterals.<Code.Entries>newArrayList();
      List<VectorField> _vectorFields = ModelUtil.vectorFields(c);
      for (final VectorField field_1 : _vectorFields) {
        {
          final VectorIndex index = ModelUtil.vectorIndex(c, field_1);
          if ((index != null)) {
            Code.Entries _vectorIndex = this.vectorIndex(index, field_1, this.metricName(field_1, index));
            indexes.add(_vectorIndex);
          }
        }
      }
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex index : _filter) {
        Code.Entries _payloadIndex = this.payloadIndex(c, index);
        indexes.add(_payloadIndex);
      }
      List<PayloadField> _payloadFields = ModelUtil.payloadFields(c);
      for (final PayloadField field_2 : _payloadFields) {
        if ((this.searchable(field_2) && (!IterableExtensions.<PayloadIndex>exists(Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class), ((Function1<PayloadIndex, Boolean>) (PayloadIndex it) -> {
          return Boolean.valueOf(((it.getTarget().getField() == field_2) && Objects.equals(it.getKind(), EPayloadIndexKind.FULL_TEXT)));
        }))))) {
          String _fieldName = ModelUtil.fieldName(field_2);
          String _plus_2 = (_fieldName + "_fts");
          Code.Entries _fts = this.fts(_plus_2, ModelUtil.fieldName(field_2), field_2.getAnalyzer());
          indexes.add(_fts);
        }
      }
      final Function1<Code.Entries, Boolean> _function_2 = (Code.Entries f) -> {
        Code.Expr _get = f.get("name");
        String _value = ((Code.Text) _get).value();
        String _fieldName_1 = ModelUtil.fieldName(key);
        return Boolean.valueOf(Objects.equals(_value, _fieldName_1));
      };
      final Code.Entries keyField = IterableExtensions.<Code.Entries>findFirst(fields, _function_2);
      keyField.put("nullable", Code.flag(false));
      final Code.Entries metadata = Code.entries();
      String _description = c.getDescription();
      boolean _tripleNotEquals = (_description != null);
      if (_tripleNotEquals) {
        metadata.put(LancedbSchema.DESCRIPTION, Code.text(c.getDescription()));
      }
      boolean _isEmpty_1 = c.getTags().isEmpty();
      boolean _not = (!_isEmpty_1);
      if (_not) {
        metadata.put(LancedbSchema.TAGS, Code.texts(c.getTags()));
      }
      boolean _isEmpty_2 = metadata.entries().isEmpty();
      boolean _not_1 = (!_isEmpty_2);
      if (_not_1) {
        keyField.put("metadata", metadata);
      }
      String _databaseOf = LancedbSchema.databaseOf(c);
      String _name = c.getName();
      String _fieldName_1 = ModelUtil.fieldName(key);
      _xblockexpression = new LancedbTable(_databaseOf, _name, _fieldName_1, fields, vectors, tenant, namespace, partition, indexes);
    }
    return _xblockexpression;
  }

  /**
   * Whether a payload field is searched by keyword search: full-text, document-body or with an analyzer.
   */
  public boolean searchable(final PayloadField field) {
    return ((field.isFullText() || field.isDocumentBody()) || (field.getAnalyzer() != null));
  }

  public List<Code.Stmt> createTable(final VectorCollection c, final boolean ifNotExists) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(("collections derived from external sources; " + LancedbStatements.EXTERNAL));
      }
      if (((c.getTtlField() != null) || ModelUtil.written(c, "ttlSeconds"))) {
        throw Unsupported.of(("collection ttl; " + LancedbStatements.NO_TTL));
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of("auto-id; the generator keys rows by the primary key value the program writes (Lance has no generated keys)");
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of("deletion-protection; a LanceDB table is a directory that drop_table removes");
      }
      boolean _isEmpty = c.getCrossReferences().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("cross-ref; Lance tables hold no references to other tables");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + LancedbStatements.NO_MODELS));
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        throw Unsupported.of(("storage settings; " + LancedbStatements.STORAGE));
      }
      boolean _isEmpty_1 = c.getAliases().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of(LancedbStatements.NO_ALIASES);
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _plus_1 = (_plus + LancedbStatements.NO_FUNCTIONS);
        throw Unsupported.of(_plus_1);
      }
      boolean _isEmpty_3 = c.getLimits().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        String _literal_1 = IterableExtensions.<CollectionLimit>head(c.getLimits()).getType().getLiteral();
        String _plus_2 = ("collection limits (" + _literal_1);
        String _plus_3 = (_plus_2 + "); LanceDB OSS has no table limits");
        throw Unsupported.of(_plus_3);
      }
      Set<String> _keySet = ModelUtil.extParams(c, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      this.note(c, "idType", "storedSourceFields", "generativeModel", "consistency", "dynamic");
      EList<Analyzer> _analyzers = c.getAnalyzers();
      for (final Analyzer analyzer : _analyzers) {
        this.tokenizer(analyzer);
      }
      LancedbTable _xifexpression = null;
      if (ifNotExists) {
        _xifexpression = this.evolved.get(c);
      } else {
        _xifexpression = null;
      }
      final LancedbTable known = _xifexpression;
      if ((known == null)) {
        this.evolve(c, null);
      }
      LancedbTable _elvis = null;
      if (known != null) {
        _elvis = known;
      } else {
        LancedbTable _declared = this.declared(c);
        _elvis = _declared;
      }
      final LancedbTable table = _elvis;
      final Code.Entries args = LancedbSchema.kw("table", table.place(), "schema", table.schema());
      boolean _isEmpty_4 = table.indexes().isEmpty();
      boolean _not_4 = (!_isEmpty_4);
      if (_not_4) {
        args.put("indexes", table.indexPlans());
      }
      if (ifNotExists) {
        args.put("exist_ok", Code.flag(true));
      }
      Code.Call _runtime = Code.runtime("create_table", args);
      Code.Run _run = new Code.Run(_runtime);
      _xblockexpression = Collections.<Code.Stmt>unmodifiableList(CollectionLiterals.<Code.Stmt>newArrayList(_run));
    }
    return _xblockexpression;
  }

  /**
   * The logical database of a collection declared in a logical-db (a sub-directory of the storage URI), else null.
   */
  public static String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      if ((database == null)) {
        return null;
      }
      LancedbSchema.checkName(database.getName());
      _xblockexpression = database.getName();
    }
    return _xblockexpression;
  }

  public static void checkName(final String name) {
    boolean _matches = name.matches(LancedbSchema.NAME);
    boolean _not = (!_matches);
    if (_not) {
      throw Unsupported.of((("the name \'" + name) + "\'; LanceDB table and database names are letters, digits, underscores, hyphens and periods"));
    }
  }

  public Code.Entries fieldOf(final VectorCollection c, final Field field) {
    Code.Entries _switchResult = null;
    boolean _matched = false;
    if (field instanceof VectorField) {
      _matched=true;
      _switchResult = this.vectorFieldOf(c, ((VectorField)field));
    }
    if (!_matched) {
      if (field instanceof PayloadField) {
        _matched=true;
        _switchResult = this.payloadFieldOf(((PayloadField)field));
      }
    }
    return _switchResult;
  }

  public Code.Entries vectorFieldOf(final VectorCollection c, final VectorField field) {
    Code.Entries _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      EVectorDType _type = field.getType();
      boolean _equals = Objects.equals(_type, EVectorDType.SPARSE_FLOAT_VECTOR);
      if (_equals) {
        throw Unsupported.of(((("sparse vector field " + name) + "; ") + LancedbStatements.NO_SPARSE));
      }
      EVectorDType _type_1 = field.getType();
      boolean _equals_1 = Objects.equals(_type_1, EVectorDType.BFLOAT16_VECTOR);
      if (_equals_1) {
        throw Unsupported.of((((("bfloat16_vector field " + name) + "; Arrow and Lance have no bfloat16 type (") + LancedbSchema.LANCE_SCHEMA) + ")"));
      }
      if ((Objects.equals(field.getType(), EVectorDType.INT8_VECTOR) || Objects.equals(field.getType(), EVectorDType.UINT8_VECTOR))) {
        String _literal = field.getType().getLiteral();
        String _plus = (_literal + " field ");
        String _plus_1 = (_plus + name);
        String _plus_2 = (_plus_1 + "; LanceDB compares a query vector of the column element type, and the SDKs send float32 queries (scanner.rs: \"Column ... has element type\")");
        throw Unsupported.of(_plus_2);
      }
      int _dimension = field.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + name) + " without dim; a Lance vector column is a fixed-size list"));
      }
      if (((field.getModel() != null) || (ModelUtil.producingFunction(c, field) != null))) {
        throw Unsupported.of((("server-side embedding of " + name) + LancedbStatements.NO_MODELS));
      }
      this.note(field, "normalized", "onDisk", "modifier", "limits");
      Set<String> _keySet = ModelUtil.extParams(field, LancedbGenerator.VENDOR).keySet();
      for (final String name2 : _keySet) {
        throw Unsupported.of(("extension parameter " + name2));
      }
      final String element = LancedbSchema.ELEMENTS.get(field.getType());
      EVectorDType _type_2 = field.getType();
      final boolean binary = Objects.equals(_type_2, EVectorDType.BINARY_VECTOR);
      if ((binary && ((field.getDimension() % 8) != 0))) {
        throw Unsupported.of((("binary_vector field " + name) + " with a dim that is not a multiple of 8; Lance stores packed bits as uint8"));
      }
      String _xifexpression = null;
      boolean _isMultiVector = field.isMultiVector();
      if (_isMultiVector) {
        _xifexpression = "multivector";
      } else {
        _xifexpression = "vector";
      }
      int _xifexpression_1 = (int) 0;
      if (binary) {
        int _dimension_1 = field.getDimension();
        _xifexpression_1 = (_dimension_1 / 8);
      } else {
        _xifexpression_1 = field.getDimension();
      }
      final Code.Entries result = LancedbSchema.kw("name", Code.text(name), "type", Code.text(_xifexpression), "element", Code.text(element), 
        "dim", Code.num(_xifexpression_1));
      if (binary) {
        result.put("bits", Code.num(field.getDimension()));
      }
      if ((field.isMultiVector() && (!Objects.equals(this.metricName(field, ModelUtil.vectorIndex(c, field)), "cosine")))) {
        throw Unsupported.of("a multivector field with a metric other than cosine; LanceDB multivector search supports cosine only");
      }
      boolean _isNullable = field.isNullable();
      boolean _not = (!_isNullable);
      if (_not) {
        result.put("nullable", Code.flag(false));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public Code.Entries payloadFieldOf(final PayloadField field) {
    Code.Entries _xblockexpression = null;
    {
      final String name = ModelUtil.fieldName(field);
      boolean _startsWith = name.startsWith("_");
      if (_startsWith) {
        throw Unsupported.of((("field " + name) + "; LanceDB reserves names starting with an underscore for _distance, _score, _rowid and _relevance_score"));
      }
      this.note(field, "maxLength", "limits");
      Set<String> _keySet = ModelUtil.extParams(field, LancedbGenerator.VENDOR).keySet();
      for (final String name2 : _keySet) {
        throw Unsupported.of(("extension parameter " + name2));
      }
      final Code.Entries result = LancedbSchema.kw("name", Code.text(name));
      if (((!field.getSubFields().isEmpty()) || Objects.equals(field.getType(), EScalarDType.STRUCT))) {
        if ((((!Objects.equals(field.getType(), EScalarDType.STRUCT)) && (!Objects.equals(field.getType(), EScalarDType.JSON))) && (!Objects.equals(field.getType(), EScalarDType.ARRAY)))) {
          String _literal = field.getType().getLiteral();
          String _plus = ("sub-fields on a " + _literal);
          String _plus_1 = (_plus + " field ");
          String _plus_2 = (_plus_1 + name);
          throw Unsupported.of(_plus_2);
        }
        boolean _isEmpty = field.getSubFields().isEmpty();
        if (_isEmpty) {
          throw Unsupported.of((("struct field " + name) + " without sub-fields; an Arrow struct has declared children"));
        }
        final List<Code.Entries> members = CollectionLiterals.<Code.Entries>newArrayList();
        EList<Field> _subFields = field.getSubFields();
        for (final Field sub : _subFields) {
          {
            if ((!(sub instanceof PayloadField))) {
              throw Unsupported.of(("a vector field inside the struct " + name));
            }
            Code.Entries _payloadFieldOf = this.payloadFieldOf(((PayloadField) sub));
            members.add(_payloadFieldOf);
          }
        }
        final Code.Items children = Code.items(members);
        EScalarDType _type = field.getType();
        boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
        if (_equals) {
          result.put("type", Code.text("list"));
          result.put("element", LancedbSchema.kw("type", Code.text("struct"), "fields", children));
        } else {
          result.put("type", Code.text("struct"));
          result.put("fields", children);
        }
      } else {
        EScalarDType _type_1 = field.getType();
        boolean _equals_1 = Objects.equals(_type_1, EScalarDType.ARRAY);
        if (_equals_1) {
          boolean _written = ModelUtil.written(field, "elementType");
          boolean _not = (!_written);
          if (_not) {
            throw Unsupported.of((("array field " + name) + " without element-type"));
          }
          result.put("type", Code.text("list"));
          result.put("element", LancedbSchema.kw("type", Code.text(this.scalar(field.getElementType(), name))));
        } else {
          result.put("type", Code.text(this.scalar(field.getType(), name)));
        }
      }
      String _description = field.getDescription();
      boolean _tripleNotEquals = (_description != null);
      if (_tripleNotEquals) {
        result.put("metadata", LancedbSchema.kw(LancedbSchema.DESCRIPTION, Code.text(field.getDescription())));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String scalar(final EScalarDType type, final String name) {
    String _switchResult = null;
    if (type != null) {
      switch (type) {
        case JSON:
          throw Unsupported.of(((("json field " + name) + LancedbStatements.PENDING) + " (the lance.json Arrow extension type)"));
        case MAP:
          throw Unsupported.of((((("map field " + name) + "; the Lance type system has no map type (") + LancedbSchema.LANCE_SCHEMA) + ")"));
        case GEO_POINT:
        case GEOMETRY:
          String _literal = type.getLiteral();
          String _plus = (_literal + " field ");
          String _plus_1 = (_plus + name);
          String _plus_2 = (_plus_1 + LancedbStatements.PENDING);
          String _plus_3 = (_plus_2 + " (GeoArrow types and the R-tree index)");
          throw Unsupported.of(_plus_3);
        case ARRAY:
        case STRUCT:
          String _literal_1 = type.getLiteral();
          String _plus_4 = ("nested " + _literal_1);
          String _plus_5 = (_plus_4 + " elements in field ");
          String _plus_6 = (_plus_5 + name);
          throw Unsupported.of(_plus_6);
        default:
          _switchResult = LancedbSchema.SCALARS.get(type);
          break;
      }
    } else {
      _switchResult = LancedbSchema.SCALARS.get(type);
    }
    return _switchResult;
  }

  /**
   * A field added by an alter: nullable, since existing rows have no value (a default is written by the runtime).
   */
  public Code.Entries addedField(final VectorCollection c, final Field field) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = this.fieldOf(c, field);
      result.put("nullable", Code.flag(true));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public EMetric metric(final VectorField field, final VectorIndex index) {
    EMetric _xifexpression = null;
    EVectorDType _type = field.getType();
    boolean _equals = Objects.equals(_type, EVectorDType.BINARY_VECTOR);
    if (_equals) {
      _xifexpression = EMetric.HAMMING;
    } else {
      EMetric _xifexpression_1 = null;
      boolean _written = ModelUtil.written(field, "metric");
      if (_written) {
        _xifexpression_1 = field.getMetric();
      } else {
        EMetric _xifexpression_2 = null;
        if (((index != null) && ModelUtil.written(index, "metric"))) {
          _xifexpression_2 = index.getMetric();
        } else {
          _xifexpression_2 = EMetric.COSINE;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  public String metricName(final VectorField field, final VectorIndex index) {
    return LancedbSchema.lance(this.metric(field, index));
  }

  /**
   * LanceDB's distance types: l2 (squared Euclidean), cosine (1 - cos), dot (1 - x.y) and hamming.
   */
  public static String lance(final EMetric metric) {
    String _switchResult = null;
    if (metric != null) {
      switch (metric) {
        case COSINE:
          _switchResult = "cosine";
          break;
        case DOT:
          _switchResult = "dot";
          break;
        case EUCLIDEAN:
          _switchResult = "l2";
          break;
        case HAMMING:
          _switchResult = "hamming";
          break;
        default:
          String _literal = metric.getLiteral();
          String _plus = (_literal + " metric; LanceDB measures l2, cosine, dot and hamming distances");
          throw Unsupported.of(_plus);
      }
    } else {
      String _literal = metric.getLiteral();
      String _plus = (_literal + " metric; LanceDB measures l2, cosine, dot and hamming distances");
      throw Unsupported.of(_plus);
    }
    return _switchResult;
  }

  /**
   * A vector index plan: family and quantization select the Lance index type (IVF_FLAT, IVF_PQ, IVF_RQ, IVF_HNSW_SQ,
   * IVF_HNSW_PQ); centroids is num_partitions, max-degree m, build-candidates ef_construction.
   */
  public Code.Entries vectorIndex(final VectorIndex index, final VectorField field, final String metric) {
    Code.Entries _xblockexpression = null;
    {
      if (((Objects.equals(field.getType(), EVectorDType.BINARY_VECTOR) && (!Objects.equals(index.getFamily(), EIndexFamily.IVF))) && (!Objects.equals(index.getFamily(), EIndexFamily.FLAT)))) {
        String _literal = index.getFamily().getLiteral();
        String _plus = ("a " + _literal);
        String _plus_1 = (_plus + " index on binary vectors; LanceDB indexes hamming distance with IVF_FLAT only");
        throw Unsupported.of(_plus_1);
      }
      if ((ModelUtil.written(index, "metric") && (!Objects.equals(LancedbSchema.lance(index.getMetric()), metric)))) {
        String _fieldName = ModelUtil.fieldName(field);
        String _plus_2 = ("an index metric other than the metric of field " + _fieldName);
        String _plus_3 = (_plus_2 + "; a LanceDB query names one distance type");
        throw Unsupported.of(_plus_3);
      }
      EAccelerator _accelerator = index.getAccelerator();
      boolean _equals = Objects.equals(_accelerator, EAccelerator.GPU);
      if (_equals) {
        throw Unsupported.of("the \'gpu\' accelerator; GPU index training needs pylance with CUDA, which the TypeScript and Java SDKs do not offer");
      }
      this.note(index, "onDisk", "online", "searchCandidates");
      Set<String> _keySet = ModelUtil.extParams(index, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      final Quantization quantization = index.getQuantization();
      String _xifexpression = null;
      if (((quantization == null) || Objects.equals(quantization.getType(), EQuantizationKind.NONE))) {
        String _switchResult = null;
        EIndexFamily _family = index.getFamily();
        if (_family != null) {
          switch (_family) {
            case AUTO:
            case HNSW:
              _switchResult = "ivf_hnsw_sq";
              break;
            case FLAT:
            case IVF:
              _switchResult = "ivf_flat";
              break;
            default:
              String _literal_1 = index.getFamily().getLiteral();
              String _plus_4 = (_literal_1 + " indexes; LanceDB builds IVF partitions with flat, PQ, RQ or HNSW sub-indexes");
              throw Unsupported.of(_plus_4);
          }
        } else {
          String _literal_1 = index.getFamily().getLiteral();
          String _plus_4 = (_literal_1 + " indexes; LanceDB builds IVF partitions with flat, PQ, RQ or HNSW sub-indexes");
          throw Unsupported.of(_plus_4);
        }
        _xifexpression = _switchResult;
      } else {
        _xifexpression = this.quantizationKind(index, quantization.getType());
      }
      final String kind = _xifexpression;
      if ((quantization != null)) {
        this.note(quantization, "rescore", "oversampling", "keepOriginals");
        boolean _written = ModelUtil.written(quantization, "truncateDimension");
        if (_written) {
          throw Unsupported.of("truncate-dim; LanceDB indexes the full vector");
        }
        if (((ModelUtil.written(quantization, "bits") && (!(Objects.equals(kind, "ivf_pq") || Objects.equals(kind, "ivf_hnsw_pq")))) && (!(Objects.equals(kind, "ivf_hnsw_sq") && (quantization.getBits() == 8))))) {
          throw Unsupported.of((("bits on " + kind) + "; LanceDB sets bits for product quantization only (scalar quantization is 8 bits)"));
        }
        Set<String> _keySet_1 = ModelUtil.extParams(quantization, LancedbGenerator.VENDOR).keySet();
        for (final String name_1 : _keySet_1) {
          throw Unsupported.of(("extension parameter " + name_1));
        }
      }
      final Code.Entries result = LancedbSchema.kw("name", Code.text(index.getName()), "column", Code.text(ModelUtil.fieldName(field)), "kind", Code.text(kind), "metric", Code.text(metric));
      boolean _written_1 = ModelUtil.written(index, "centroids");
      if (_written_1) {
        result.put("num_partitions", Code.num(index.getCentroids()));
      }
      boolean _startsWith = kind.startsWith("ivf_hnsw");
      if (_startsWith) {
        boolean _written_2 = ModelUtil.written(index, "maxDegree");
        if (_written_2) {
          result.put("m", Code.num(index.getMaxDegree()));
        }
        boolean _written_3 = ModelUtil.written(index, "buildCandidates");
        if (_written_3) {
          result.put("ef_construction", Code.num(index.getBuildCandidates()));
        }
      } else {
        if ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates"))) {
          throw Unsupported.of((("max-degree and build-candidates on a " + kind) + " index; they configure HNSW graphs"));
        }
      }
      if (((quantization != null) && ModelUtil.written(quantization, "subvectors"))) {
        boolean _endsWith = kind.endsWith("pq");
        boolean _not = (!_endsWith);
        if (_not) {
          throw Unsupported.of("subvectors without product quantization");
        }
        result.put("num_sub_vectors", Code.num(quantization.getSubvectors()));
      }
      if ((((quantization != null) && ModelUtil.written(quantization, "bits")) && kind.endsWith("pq"))) {
        result.put("num_bits", Code.num(quantization.getBits()));
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  private String quantizationKind(final VectorIndex index, final EQuantizationKind type) {
    String _xblockexpression = null;
    {
      final boolean graph = (Objects.equals(index.getFamily(), EIndexFamily.HNSW) || Objects.equals(index.getFamily(), EIndexFamily.AUTO));
      if ((Objects.equals(type, EQuantizationKind.SCALAR) && (!graph))) {
        throw Unsupported.of("scalar quantization on an ivf index; the TypeScript SDK 0.41.0 builds no IVF_SQ index (Index.ivfFlat, ivfPq, ivfRq, hnswPq, hnswSq)");
      }
      if ((Objects.equals(type, EQuantizationKind.BINARY) && graph)) {
        throw Unsupported.of("binary quantization on an hnsw index; LanceDB quantizes IVF partitions with RaBitQ (IVF_RQ) only");
      }
      String _switchResult = null;
      if (type != null) {
        switch (type) {
          case PRODUCT:
            String _xifexpression = null;
            if (graph) {
              _xifexpression = "ivf_hnsw_pq";
            } else {
              _xifexpression = "ivf_pq";
            }
            _switchResult = _xifexpression;
            break;
          case SCALAR:
            _switchResult = "ivf_hnsw_sq";
            break;
          case BINARY:
            _switchResult = "ivf_rq";
            break;
          default:
            String _literal = type.getLiteral();
            String _plus = (_literal + " quantization");
            throw Unsupported.of(_plus);
        }
      } else {
        String _literal = type.getLiteral();
        String _plus = (_literal + " quantization");
        throw Unsupported.of(_plus);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * A payload index plan: BTREE for ordered and keyword values, BITMAP for low-cardinality values (structure bitmap or
   * bool), LABEL_LIST for arrays, FTS (inverted, BM25) for full_text.
   */
  public Code.Entries payloadIndex(final VectorCollection c, final PayloadIndex index) {
    Code.Entries _xblockexpression = null;
    {
      final FieldPath path = index.getTarget();
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals = (_rawPath != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("a payload index on a raw path; Lance columns are declared");
      }
      String _xifexpression = null;
      boolean _isEmpty = path.getSubFields().isEmpty();
      if (_isEmpty) {
        _xifexpression = ModelUtil.fieldName(path.getField());
      } else {
        String _fieldName = ModelUtil.fieldName(path.getField());
        String _plus = (_fieldName + ".");
        final Function1<Field, String> _function = (Field it) -> {
          return ModelUtil.fieldName(it);
        };
        String _join = IterableExtensions.join(ListExtensions.<Field, String>map(path.getSubFields(), _function), ".");
        _xifexpression = (_plus + _join);
      }
      final String column = _xifexpression;
      final Field field = ModelUtil.leaf(path);
      if ((!(field instanceof PayloadField))) {
        throw Unsupported.of("a payload index on a vector field");
      }
      final PayloadField payload = ((PayloadField) field);
      this.note(index, "onDisk", "castType", "prefixMatch");
      Set<String> _keySet = ModelUtil.extParams(index, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      Code.Entries _switchResult = null;
      EPayloadIndexKind _kind = index.getKind();
      if (_kind != null) {
        switch (_kind) {
          case FULL_TEXT:
            Code.Entries _xblockexpression_1 = null;
            {
              boolean _contains = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT)).contains(payload.getType());
              boolean _not = (!_contains);
              if (_not) {
                String _literal = payload.getType().getLiteral();
                String _plus_1 = ("a full_text index on " + _literal);
                String _plus_2 = (_plus_1 + " field ");
                String _plus_3 = (_plus_2 + column);
                throw Unsupported.of(_plus_3);
              }
              String _name = index.getName();
              Analyzer _elvis = null;
              Analyzer _analyzer = index.getAnalyzer();
              if (_analyzer != null) {
                _elvis = _analyzer;
              } else {
                Analyzer _analyzer_1 = payload.getAnalyzer();
                _elvis = _analyzer_1;
              }
              final Code.Entries plan = this.fts(_name, column, _elvis);
              boolean _isLowercase = index.isLowercase();
              if (_isLowercase) {
                plan.put("lower_case", Code.flag(true));
              }
              boolean _written = ModelUtil.written(index, "maxTokenLength");
              if (_written) {
                plan.put("max_token_length", Code.num(index.getMaxTokenLength()));
              }
              boolean _written_1 = ModelUtil.written(index, "minTokenLength");
              if (_written_1) {
                throw Unsupported.of("min-token-len; the LanceDB FTS index sets a maximum token length only");
              }
              _xblockexpression_1 = plan;
            }
            _switchResult = _xblockexpression_1;
            break;
          case GEO:
            throw Unsupported.of((("geo payload indexes" + LancedbStatements.PENDING) + " (GeoArrow types and the R-tree index)"));
          case JSON:
            throw Unsupported.of((("json payload indexes" + LancedbStatements.PENDING) + " (the lance.json Arrow extension type)"));
          default:
            Code.Entries _xblockexpression_2 = null;
            {
              String _xifexpression_1 = null;
              EScalarDType _type = payload.getType();
              boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
              if (_equals) {
                _xifexpression_1 = "label_list";
              } else {
                String _xifexpression_2 = null;
                if (((Objects.equals(index.getStructure(), EPayloadIndexStructure.BITMAP) || Objects.equals(index.getStructure(), EPayloadIndexStructure.INVERTED)) || Objects.equals(index.getKind(), EPayloadIndexKind.BOOL))) {
                  _xifexpression_2 = "bitmap";
                } else {
                  _xifexpression_2 = "btree";
                }
                _xifexpression_1 = _xifexpression_2;
              }
              final String kind = _xifexpression_1;
              if (((((index.getAnalyzer() != null) || index.isLowercase()) || ModelUtil.written(index, "minTokenLength")) || ModelUtil.written(index, "maxTokenLength"))) {
                String _literal = index.getKind().getLiteral();
                String _plus_1 = ("text analysis on a " + _literal);
                String _plus_2 = (_plus_1 + " index; LanceDB analyzes text in FTS indexes only");
                throw Unsupported.of(_plus_2);
              }
              _xblockexpression_2 = LancedbSchema.kw("name", Code.text(index.getName()), "column", Code.text(column), "kind", Code.text(kind));
            }
            _switchResult = _xblockexpression_2;
            break;
        }
      } else {
        Code.Entries _xblockexpression_2 = null;
        {
          String _xifexpression_1 = null;
          EScalarDType _type = payload.getType();
          boolean _equals = Objects.equals(_type, EScalarDType.ARRAY);
          if (_equals) {
            _xifexpression_1 = "label_list";
          } else {
            String _xifexpression_2 = null;
            if (((Objects.equals(index.getStructure(), EPayloadIndexStructure.BITMAP) || Objects.equals(index.getStructure(), EPayloadIndexStructure.INVERTED)) || Objects.equals(index.getKind(), EPayloadIndexKind.BOOL))) {
              _xifexpression_2 = "bitmap";
            } else {
              _xifexpression_2 = "btree";
            }
            _xifexpression_1 = _xifexpression_2;
          }
          final String kind = _xifexpression_1;
          if (((((index.getAnalyzer() != null) || index.isLowercase()) || ModelUtil.written(index, "minTokenLength")) || ModelUtil.written(index, "maxTokenLength"))) {
            String _literal = index.getKind().getLiteral();
            String _plus_1 = ("text analysis on a " + _literal);
            String _plus_2 = (_plus_1 + " index; LanceDB analyzes text in FTS indexes only");
            throw Unsupported.of(_plus_2);
          }
          _xblockexpression_2 = LancedbSchema.kw("name", Code.text(index.getName()), "column", Code.text(column), "kind", Code.text(kind));
        }
        _switchResult = _xblockexpression_2;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * An FTS index plan with positions (phrase queries need them) and the analyzer's tokenizer, language and filters. An
   * analyzer without filters keeps LanceDB's defaults (lower case, ASCII folding) and, with a language, stems and removes
   * stop words; listed filters are taken exactly.
   */
  public Code.Entries fts(final String name, final String column, final Analyzer analyzer) {
    Code.Entries _xblockexpression = null;
    {
      final Code.Entries result = LancedbSchema.kw("name", Code.text(name), "column", Code.text(column), "kind", Code.text("fts"), "with_position", Code.flag(true));
      if ((analyzer == null)) {
        return result;
      }
      result.put("base_tokenizer", Code.text(this.tokenizer(analyzer)));
      String _language = analyzer.getLanguage();
      boolean _tripleNotEquals = (_language != null);
      if (_tripleNotEquals) {
        result.put("language", Code.text(LancedbSchema.language(analyzer.getLanguage())));
      }
      final Function1<AnalyzerFilter, EAnalyzerFilterType> _function = (AnalyzerFilter it) -> {
        return it.getType();
      };
      final Set<EAnalyzerFilterType> filters = IterableExtensions.<EAnalyzerFilterType>toSet(ListExtensions.<AnalyzerFilter, EAnalyzerFilterType>map(analyzer.getFilters(), _function));
      final boolean defaults = filters.isEmpty();
      final boolean linguistic = ((analyzer.getLanguage() != null) || Objects.equals(analyzer.getTokenizer(), ETokenizer.LANGUAGE));
      boolean _xifexpression = false;
      if (defaults) {
        _xifexpression = linguistic;
      } else {
        _xifexpression = filters.contains(EAnalyzerFilterType.STEMMER);
      }
      result.put("stem", Code.flag(_xifexpression));
      boolean _xifexpression_1 = false;
      if (defaults) {
        _xifexpression_1 = (linguistic || (analyzer.getStopwords() != null));
      } else {
        _xifexpression_1 = filters.contains(EAnalyzerFilterType.STOP);
      }
      result.put("remove_stop_words", Code.flag(_xifexpression_1));
      result.put("lower_case", Code.flag((defaults || filters.contains(EAnalyzerFilterType.LOWERCASE))));
      result.put("ascii_folding", Code.flag((defaults || filters.contains(EAnalyzerFilterType.ASCII_FOLDING))));
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  public String tokenizer(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
        throw Unsupported.of("bm25-k1 and bm25-b; LanceDB scores BM25 with fixed parameters");
      }
      boolean _isEmpty = analyzer.getResources().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of("analyzer resources; LanceDB FTS has no file resources");
      }
      if ((((analyzer.getStopwords() != null) && (!Objects.equals(analyzer.getStopwords(), "_english_"))) && (!Objects.equals(analyzer.getStopwords(), "english")))) {
        String _stopwords = analyzer.getStopwords();
        String _plus = ("the stop word list \'" + _stopwords);
        String _plus_1 = (_plus + "\'; LanceDB removes the built-in list of the index language");
        throw Unsupported.of(_plus_1);
      }
      EList<AnalyzerFilter> _filters = analyzer.getFilters();
      for (final AnalyzerFilter filter : _filters) {
        EAnalyzerFilterType _type = filter.getType();
        if (_type != null) {
          switch (_type) {
            case LOWERCASE:
            case STOP:
            case STEMMER:
            case ASCII_FOLDING:
              break;
            default:
              String _literal = filter.getType().getLiteral();
              String _plus_2 = (_literal + " filters; LanceDB FTS filters are lower_case, stop words, stemming and ascii_folding");
              throw Unsupported.of(_plus_2);
          }
        } else {
          String _literal = filter.getType().getLiteral();
          String _plus_2 = (_literal + " filters; LanceDB FTS filters are lower_case, stop words, stemming and ascii_folding");
          throw Unsupported.of(_plus_2);
        }
      }
      Set<String> _keySet = ModelUtil.extParams(analyzer, LancedbGenerator.VENDOR).keySet();
      for (final String name : _keySet) {
        throw Unsupported.of(("extension parameter " + name));
      }
      String _switchResult_1 = null;
      ETokenizer _tokenizer = analyzer.getTokenizer();
      if (_tokenizer != null) {
        switch (_tokenizer) {
          case STANDARD:
          case LANGUAGE:
            _switchResult_1 = "simple";
            break;
          case WHITESPACE:
            _switchResult_1 = "whitespace";
            break;
          case KEYWORD:
            _switchResult_1 = "raw";
            break;
          case NGRAM:
            _switchResult_1 = "ngram";
            break;
          default:
            String _literal_1 = analyzer.getTokenizer().getLiteral();
            String _plus_3 = (_literal_1 + " tokenizers; LanceDB FTS tokenizers are simple, whitespace, raw and ngram");
            throw Unsupported.of(_plus_3);
        }
      } else {
        String _literal_1 = analyzer.getTokenizer().getLiteral();
        String _plus_3 = (_literal_1 + " tokenizers; LanceDB FTS tokenizers are simple, whitespace, raw and ngram");
        throw Unsupported.of(_plus_3);
      }
      _xblockexpression = _switchResult_1;
    }
    return _xblockexpression;
  }

  /**
   * The language names of LanceDB's stemmers and stop word lists (Tantivy's Language enum).
   */
  private static String language(final String code) {
    String _xblockexpression = null;
    {
      Pair<String, String> _mappedTo = Pair.<String, String>of("en", "English");
      Pair<String, String> _mappedTo_1 = Pair.<String, String>of("de", "German");
      Pair<String, String> _mappedTo_2 = Pair.<String, String>of("fr", "French");
      Pair<String, String> _mappedTo_3 = Pair.<String, String>of("es", "Spanish");
      Pair<String, String> _mappedTo_4 = Pair.<String, String>of("it", "Italian");
      Pair<String, String> _mappedTo_5 = Pair.<String, String>of("pt", "Portuguese");
      Pair<String, String> _mappedTo_6 = Pair.<String, String>of("nl", "Dutch");
      Pair<String, String> _mappedTo_7 = Pair.<String, String>of("ru", "Russian");
      Pair<String, String> _mappedTo_8 = Pair.<String, String>of("sv", "Swedish");
      Pair<String, String> _mappedTo_9 = Pair.<String, String>of("da", "Danish");
      Pair<String, String> _mappedTo_10 = Pair.<String, String>of("fi", "Finnish");
      Pair<String, String> _mappedTo_11 = Pair.<String, String>of("no", "Norwegian");
      Pair<String, String> _mappedTo_12 = Pair.<String, String>of("hu", "Hungarian");
      Pair<String, String> _mappedTo_13 = Pair.<String, String>of("ro", "Romanian");
      Pair<String, String> _mappedTo_14 = Pair.<String, String>of("tr", "Turkish");
      Pair<String, String> _mappedTo_15 = Pair.<String, String>of("ar", "Arabic");
      Pair<String, String> _mappedTo_16 = Pair.<String, String>of("el", "Greek");
      Pair<String, String> _mappedTo_17 = Pair.<String, String>of("ta", "Tamil");
      final Map<String, String> names = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(_mappedTo, _mappedTo_1, _mappedTo_2, _mappedTo_3, _mappedTo_4, _mappedTo_5, _mappedTo_6, _mappedTo_7, _mappedTo_8, _mappedTo_9, _mappedTo_10, _mappedTo_11, _mappedTo_12, _mappedTo_13, _mappedTo_14, _mappedTo_15, _mappedTo_16, _mappedTo_17));
      String _elvis = null;
      String _get = names.get(code.toLowerCase());
      if (_get != null) {
        _elvis = _get;
      } else {
        final Function1<String, Boolean> _function = (String it) -> {
          return Boolean.valueOf(it.equalsIgnoreCase(code));
        };
        String _findFirst = IterableExtensions.<String>findFirst(names.values(), _function);
        _elvis = _findFirst;
      }
      final String name = _elvis;
      if ((name == null)) {
        String _join = IterableExtensions.join(IterableExtensions.<String>sort(names.values()), ", ");
        String _plus = ((("the language \'" + code) + "\'; LanceDB stems ") + _join);
        throw Unsupported.of(_plus);
      }
      _xblockexpression = name;
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
