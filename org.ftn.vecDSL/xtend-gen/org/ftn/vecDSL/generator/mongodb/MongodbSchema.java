package org.ftn.vecDSL.generator.mongodb;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
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
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.common.Unsupported;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Alias;
import org.ftn.vecDSL.vecDSL.Analyzer;
import org.ftn.vecDSL.vecDSL.AnalyzerFilter;
import org.ftn.vecDSL.vecDSL.Cluster;
import org.ftn.vecDSL.vecDSL.CollectionLimit;
import org.ftn.vecDSL.vecDSL.Connection;
import org.ftn.vecDSL.vecDSL.Database;
import org.ftn.vecDSL.vecDSL.EMetric;
import org.ftn.vecDSL.vecDSL.EScalarDType;
import org.ftn.vecDSL.vecDSL.EmbeddingModel;
import org.ftn.vecDSL.vecDSL.ExternalSource;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.FieldLimit;
import org.ftn.vecDSL.vecDSL.FieldPath;
import org.ftn.vecDSL.vecDSL.Function;
import org.ftn.vecDSL.vecDSL.Insert;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Partition;
import org.ftn.vecDSL.vecDSL.PayloadField;
import org.ftn.vecDSL.vecDSL.PayloadIndex;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.Update;
import org.ftn.vecDSL.vecDSL.Upsert;
import org.ftn.vecDSL.vecDSL.VectorCollection;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.ftn.vecDSL.vecDSL.VectorIndex;

/**
 * Maps vecDSL collections to MongoDB collections (server 9.0 with mongot): one collection per vecDSL collection in its
 * database (the logical-db, else the connection's database), documents keyed by _id, a $jsonSchema validator from the declared
 * fields, one vectorSearch index per vector index with every filterable field as a filter field, the search index vecdsl_text
 * for the full-text fields with their analyzers, regular indexes for payload indexes and a TTL index for collections with a
 * ttl. Tenants, namespaces and partitions are hidden fields (or the partition-key field).
 */
@SuppressWarnings("all")
public class MongodbSchema {
  private static final List<EScalarDType> INTEGERS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.INT, EScalarDType.BIGINT, EScalarDType.INT8));

  private static final List<EScalarDType> NUMBERS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.FLOAT, EScalarDType.DOUBLE));

  private static final List<EScalarDType> STRINGS = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.VARCHAR, EScalarDType.TEXT, EScalarDType.UUID));

  private static final List<EScalarDType> DATES = Collections.<EScalarDType>unmodifiableList(CollectionLiterals.<EScalarDType>newArrayList(EScalarDType.DATE, EScalarDType.DATETIME, EScalarDType.TIMESTAMPTZ));

  private static final Map<String, String> TOKENIZERS = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("standard", "standard"), Pair.<String, String>of("whitespace", "whitespace"), Pair.<String, String>of("keyword", "keyword"), Pair.<String, String>of("ngram", "nGram"), Pair.<String, String>of("edge_ngram", "edgeGram"), Pair.<String, String>of("language", "standard")));

  private static final Map<String, String> LANGUAGES = Collections.<String, String>unmodifiableMap(CollectionLiterals.<String, String>newHashMap(Pair.<String, String>of("en", "english"), Pair.<String, String>of("english", "english"), Pair.<String, String>of("de", "german"), Pair.<String, String>of("german", "german"), Pair.<String, String>of("fr", "french"), Pair.<String, String>of("french", "french"), Pair.<String, String>of("es", "spanish"), Pair.<String, String>of("spanish", "spanish"), Pair.<String, String>of("it", "italian"), Pair.<String, String>of("italian", "italian"), Pair.<String, String>of("pt", "portuguese"), Pair.<String, String>of("portuguese", "portuguese"), Pair.<String, String>of("nl", "dutch"), Pair.<String, String>of("dutch", "dutch"), Pair.<String, String>of("ru", "russian"), Pair.<String, String>of("russian", "russian"), Pair.<String, String>of("sv", "swedish"), Pair.<String, String>of("swedish", "swedish"), Pair.<String, String>of("da", "danish"), Pair.<String, String>of("danish", "danish"), Pair.<String, String>of("fi", "finnish"), Pair.<String, String>of("finnish", "finnish"), Pair.<String, String>of("no", "norwegian"), Pair.<String, String>of("norwegian", "norwegian"), Pair.<String, String>of("hu", "hungarian"), Pair.<String, String>of("hungarian", "hungarian"), Pair.<String, String>of("ro", "romanian"), Pair.<String, String>of("romanian", "romanian"), Pair.<String, String>of("tr", "turkish"), Pair.<String, String>of("turkish", "turkish")));

  private final List<String> notes;

  private final String defaultDatabase;

  private final Set<VectorCollection> tenanted = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> namespaced = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> versioned = CollectionLiterals.<VectorCollection>newHashSet();

  private final Set<VectorCollection> expiring = CollectionLiterals.<VectorCollection>newHashSet();

  private final Map<VectorCollection, MongodbCollection> evolved = CollectionLiterals.<VectorCollection, MongodbCollection>newHashMap();

  public MongodbSchema(final List<String> notes, final Script script, final String fallbackDatabase) {
    this.notes = notes;
    final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
    String _xifexpression = null;
    if (((declared != null) && (declared.getDatabaseName() != null))) {
      _xifexpression = MongodbCommands.checkedName(declared.getDatabaseName(), "database");
    } else {
      _xifexpression = fallbackDatabase;
    }
    this.defaultDatabase = _xifexpression;
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(script.eAllContents());
    for (final EObject object : _iterable) {
      this.slices(object);
    }
  }

  /**
   * Collections whose statements name a tenant or namespace get the hidden fields; if-version adds the version field.
   */
  private void slices(final EObject object) {
    final VectorCollection collection = ModelUtil.targetCollection(object);
    if ((collection == null)) {
      return;
    }
    final EStructuralFeature tenantFeature = object.eClass().getEStructuralFeature("tenant");
    if (((tenantFeature != null) && (object.eGet(tenantFeature) != null))) {
      this.tenanted.add(collection);
    }
    final EStructuralFeature namespaceFeature = object.eClass().getEStructuralFeature("namespace");
    if ((((namespaceFeature != null) && Objects.equals(namespaceFeature.getEType().getName(), "EString")) && (object.eGet(namespaceFeature) != null))) {
      this.namespaced.add(collection);
    }
    if (((object instanceof Update) && ModelUtil.written(object, "ifVersion"))) {
      this.versioned.add(collection);
    }
    if ((((object instanceof Insert) || (object instanceof Upsert)) && ModelUtil.written(object, "ttlSeconds"))) {
      this.expiring.add(collection);
    }
  }

  public String defaultDatabase() {
    return this.defaultDatabase;
  }

  /**
   * The collection as the statements so far leave it (alter statements change fields and the name).
   */
  public MongodbCollection collection(final VectorCollection c) {
    MongodbCollection _elvis = null;
    MongodbCollection _get = this.evolved.get(c);
    if (_get != null) {
      _elvis = _get;
    } else {
      MongodbCollection _declared = this.declared(c);
      _elvis = _declared;
    }
    return _elvis;
  }

  public void evolve(final VectorCollection c, final MongodbCollection next) {
    if ((next == null)) {
      this.evolved.remove(c);
    } else {
      this.evolved.put(c, next);
    }
  }

  private MongodbCollection declared(final VectorCollection c) {
    MongodbCollection _xblockexpression = null;
    {
      final Field key = c.getPrimaryKey();
      final MongodbCollection result = new MongodbCollection();
      result.database = this.databaseOf(c);
      result.name = MongodbCommands.checkedName(c.getName(), "collection");
      if ((!(key instanceof PayloadField))) {
        throw Unsupported.of("a vector field as the primary key; _id holds an integer or string key");
      }
      final EScalarDType keyType = ((PayloadField) key).getType();
      boolean _contains = MongodbSchema.INTEGERS.contains(keyType);
      if (_contains) {
        result.keySchema = MongodbCommands.obj("bsonType", MongodbCommands.arr(MongodbCommands.text("int"), MongodbCommands.text("long")));
        result.keyKind = "number";
      } else {
        boolean _contains_1 = MongodbSchema.STRINGS.contains(keyType);
        if (_contains_1) {
          result.keySchema = MongodbCommands.obj("bsonType", MongodbCommands.text("string"));
          result.keyKind = "token";
        } else {
          String _literal = keyType.getLiteral();
          String _plus = ("a primary key of type " + _literal);
          String _plus_1 = (_plus + "; the generator keys documents (_id) by an integer, text or uuid field");
          throw Unsupported.of(_plus_1);
        }
      }
      result.key = ModelUtil.fieldName(key);
      EList<Field> _fields = c.getFields();
      for (final Field declaredField : _fields) {
        MongodbCollection.Field _field = this.field(c, declaredField);
        result.fields.add(_field);
      }
      final Function1<PayloadField, Boolean> _function = (PayloadField it) -> {
        return Boolean.valueOf(it.isPartitionKey());
      };
      final PayloadField partitionKey = IterableExtensions.<PayloadField>findFirst(ModelUtil.payloadFields(c), _function);
      String _xifexpression = null;
      if (((!c.getTenants().isEmpty()) || this.tenanted.contains(c))) {
        String _xifexpression_1 = null;
        if ((partitionKey == null)) {
          _xifexpression_1 = MongodbCommands.TENANT_FIELD;
        } else {
          _xifexpression_1 = partitionKey.getName();
        }
        _xifexpression = _xifexpression_1;
      }
      result.tenant = _xifexpression;
      String _xifexpression_2 = null;
      if (((!c.getNamespaces().isEmpty()) || this.namespaced.contains(c))) {
        String _xifexpression_3 = null;
        if ((partitionKey == null)) {
          _xifexpression_3 = MongodbCommands.NAMESPACE_FIELD;
        } else {
          _xifexpression_3 = partitionKey.getName();
        }
        _xifexpression_2 = _xifexpression_3;
      }
      result.namespace = _xifexpression_2;
      String _xifexpression_4 = null;
      boolean _isEmpty = ModelUtil.partitions(c).isEmpty();
      if (_isEmpty) {
        _xifexpression_4 = null;
      } else {
        _xifexpression_4 = MongodbCommands.PARTITION_FIELD;
      }
      result.partition = _xifexpression_4;
      result.versioned = this.versioned.contains(c);
      result.expiring = this.expiring.contains(c);
      result.description = c.getDescription();
      result.dynamic = c.isDynamic();
      boolean _written = ModelUtil.written(c, "ttlSeconds");
      if (_written) {
        result.ttlSeconds = Long.valueOf(c.getTtlSeconds());
      }
      List<VectorField> _vectorFields = ModelUtil.vectorFields(c);
      for (final VectorField vectorField : _vectorFields) {
        MongodbCollection.VectorIndex _vectorIndex = this.vectorIndex(c, vectorField);
        result.vectorIndexes.add(_vectorIndex);
      }
      Iterable<PayloadIndex> _filter = Iterables.<PayloadIndex>filter(c.getIndexes(), PayloadIndex.class);
      for (final PayloadIndex payloadIndex : _filter) {
        {
          final Code.Entries keys = this.payloadIndexKeys(result, payloadIndex);
          if ((keys != null)) {
            String _field_1 = MongodbCommands.field(payloadIndex.getName());
            MongodbCollection.PayloadIndex _payloadIndex = new MongodbCollection.PayloadIndex(_field_1, keys);
            result.payloadIndexes.add(_payloadIndex);
          }
        }
      }
      List<Code.Expr> _analyzers = this.analyzers(c.getAnalyzers());
      Iterables.<Code.Expr>addAll(result.analyzers, _analyzers);
      EList<CollectionLimit> _limits = c.getLimits();
      for (final CollectionLimit limit : _limits) {
        String _literal_1 = limit.getType().getLiteral();
        if (_literal_1 != null) {
          switch (_literal_1) {
            case "max_fields":
              result.maxProperties = Integer.valueOf(limit.getValue());
              break;
            case "max_vector_dim":
              List<MongodbCollection.Field> _vectorFields_1 = result.vectorFields();
              for (final MongodbCollection.Field vectorField_1 : _vectorFields_1) {
                int _dims = vectorField_1.vector().dims();
                int _value = limit.getValue();
                boolean _greaterThan = (_dims > _value);
                if (_greaterThan) {
                  String _name = vectorField_1.name();
                  String _plus_2 = ("vector field " + _name);
                  String _plus_3 = (_plus_2 + " with more dimensions than max_vector_dim");
                  throw Unsupported.of(_plus_3);
                }
              }
              break;
            default:
              String _literal_2 = limit.getType().getLiteral();
              String _plus_4 = ("collection limits (" + _literal_2);
              String _plus_5 = (_plus_4 + "); ");
              String _plus_6 = (_plus_5 + MongodbStatements.SINGLE);
              throw Unsupported.of(_plus_6);
          }
        } else {
          String _literal_2 = limit.getType().getLiteral();
          String _plus_4 = ("collection limits (" + _literal_2);
          String _plus_5 = (_plus_4 + "); ");
          String _plus_6 = (_plus_5 + MongodbStatements.SINGLE);
          throw Unsupported.of(_plus_6);
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The database of a collection: its logical-db, else the connection's database, else vecdsl.
   */
  public String databaseOf(final VectorCollection c) {
    String _xblockexpression = null;
    {
      final Database database = EcoreUtil2.<Database>getContainerOfType(c, Database.class);
      String _xifexpression = null;
      if ((database == null)) {
        _xifexpression = this.defaultDatabase;
      } else {
        _xifexpression = MongodbCommands.checkedName(database.getName(), "database");
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public MongodbCollection.Field field(final VectorCollection c, final Field declaredField) {
    MongodbCollection.Field _switchResult = null;
    boolean _matched = false;
    if (declaredField instanceof VectorField) {
      _matched=true;
      _switchResult = this.vectorField(c, ((VectorField)declaredField));
    }
    if (!_matched) {
      if (declaredField instanceof PayloadField) {
        _matched=true;
        _switchResult = this.payloadField(((PayloadField)declaredField));
      }
    }
    return _switchResult;
  }

  private MongodbCollection.Field payloadField(final PayloadField f) {
    MongodbCollection.Field _xblockexpression = null;
    {
      final String fieldName = MongodbCommands.field(ModelUtil.fieldName(f));
      this.note(f, "documentBody", "partitionKey");
      Code.Expr _xifexpression = null;
      Literal _defaultValue = f.getDefaultValue();
      boolean _tripleEquals = (_defaultValue == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = Literals.of(f.getDefaultValue());
      }
      final Code.Expr defaultValue = _xifexpression;
      final Code.Entries schema = this.scalarSchema(f);
      boolean _isNullable = f.isNullable();
      if (_isNullable) {
        this.nullable(schema);
      }
      EList<FieldLimit> _limits = f.getLimits();
      for (final FieldLimit limit : _limits) {
        this.limitSchema(schema, limit);
      }
      final List<String> paths = CollectionLiterals.<String>newArrayList();
      this.filterPaths(f, fieldName, paths);
      final boolean textual = (f.isFullText() || (f.getAnalyzer() != null));
      if ((textual && (!MongodbSchema.STRINGS.contains(f.getType())))) {
        String _literal = f.getType().getLiteral();
        String _plus = ("full-text search on " + _literal);
        String _plus_1 = (_plus + " field ");
        String _plus_2 = (_plus_1 + fieldName);
        String _plus_3 = (_plus_2 + "; Atlas Search analyzes string fields");
        throw Unsupported.of(_plus_3);
      }
      String _xifexpression_1 = null;
      Analyzer _analyzer = f.getAnalyzer();
      boolean _tripleEquals_1 = (_analyzer == null);
      if (_tripleEquals_1) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = this.analyzerName(f.getAnalyzer());
      }
      final String analyzerName = _xifexpression_1;
      boolean _contains = MongodbSchema.DATES.contains(f.getType());
      _xblockexpression = new MongodbCollection.Field(fieldName, schema, textual, analyzerName, null, _contains, paths, defaultValue);
    }
    return _xblockexpression;
  }

  /**
   * The $jsonSchema property of a payload field: its BSON type, string length, object properties and array items.
   */
  private Code.Entries scalarSchema(final PayloadField f) {
    Code.Entries _xblockexpression = null;
    {
      final EScalarDType type = f.getType();
      if ((((!f.getSubFields().isEmpty()) || f.isNested()) || Objects.equals(type, EScalarDType.STRUCT))) {
        final Code.Entries properties = Code.entries();
        EList<Field> _subFields = f.getSubFields();
        for (final Field sub : _subFields) {
          {
            if ((!(sub instanceof PayloadField))) {
              String _name = f.getName();
              String _plus = ("a vector field inside " + _name);
              String _plus_1 = (_plus + "; vectorSearch indexes take vectors at document paths (nested vectors need nestedRoot, ");
              String _plus_2 = (_plus_1 + MongodbStatements.PENDING);
              String _plus_3 = (_plus_2 + ")");
              throw Unsupported.of(_plus_3);
            }
            final Code.Entries inner = this.scalarSchema(((PayloadField) sub));
            boolean _isNullable = ((PayloadField) sub).isNullable();
            if (_isNullable) {
              this.nullable(inner);
            }
            properties.put(MongodbCommands.field(sub.getName()), inner);
          }
        }
        Code.Text _text = MongodbCommands.text("object");
        Code.Entries _xifexpression = null;
        boolean _isEmpty = properties.entries().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = properties;
        }
        final Code.Entries objectSchema = MongodbCommands.obj("bsonType", _text, "properties", _xifexpression);
        Code.Entries _xifexpression_1 = null;
        boolean _isNested = f.isNested();
        if (_isNested) {
          _xifexpression_1 = MongodbCommands.obj("bsonType", MongodbCommands.text("array"), "items", objectSchema);
        } else {
          _xifexpression_1 = objectSchema;
        }
        return _xifexpression_1;
      }
      boolean _equals = Objects.equals(type, EScalarDType.ARRAY);
      if (_equals) {
        boolean _written = ModelUtil.written(f, "elementType");
        boolean _not = (!_written);
        if (_not) {
          String _name = f.getName();
          String _plus = ("array field " + _name);
          String _plus_1 = (_plus + " without element-type");
          throw Unsupported.of(_plus_1);
        }
        return MongodbCommands.obj("bsonType", MongodbCommands.text("array"), "items", this.elementSchema(f.getElementType(), f));
      }
      _xblockexpression = this.elementSchema(type, f);
    }
    return _xblockexpression;
  }

  private Code.Entries elementSchema(final EScalarDType type, final PayloadField f) {
    Code.Entries _xblockexpression = null;
    {
      boolean _contains = MongodbSchema.INTEGERS.contains(type);
      if (_contains) {
        return MongodbCommands.obj("bsonType", MongodbCommands.arr(MongodbCommands.text("int"), MongodbCommands.text("long")));
      }
      boolean _contains_1 = MongodbSchema.NUMBERS.contains(type);
      if (_contains_1) {
        return MongodbCommands.obj("bsonType", MongodbCommands.text("number"));
      }
      boolean _contains_2 = MongodbSchema.STRINGS.contains(type);
      if (_contains_2) {
        Code.Text _text = MongodbCommands.text("string");
        Code.Num _xifexpression = null;
        boolean _written = ModelUtil.written(f, "maxLength");
        if (_written) {
          _xifexpression = MongodbCommands.num(f.getMaxLength());
        } else {
          _xifexpression = null;
        }
        return MongodbCommands.obj("bsonType", _text, "maxLength", _xifexpression);
      }
      boolean _contains_3 = MongodbSchema.DATES.contains(type);
      if (_contains_3) {
        return MongodbCommands.obj("bsonType", MongodbCommands.text("date"));
      }
      Code.Entries _switchResult = null;
      if (type != null) {
        switch (type) {
          case BOOL:
            _switchResult = MongodbCommands.obj("bsonType", MongodbCommands.text("bool"));
            break;
          case JSON:
          case MAP:
            _switchResult = MongodbCommands.obj("bsonType", MongodbCommands.text("object"));
            break;
          case GEO_POINT:
          case GEOMETRY:
            _switchResult = MongodbCommands.obj("bsonType", MongodbCommands.text("object"), "required", MongodbCommands.arr(MongodbCommands.text("type"), MongodbCommands.text("coordinates")));
            break;
          default:
            String _literal = type.getLiteral();
            String _plus = (_literal + " field ");
            String _name = f.getName();
            String _plus_1 = (_plus + _name);
            String _plus_2 = (_plus_1 + "; generated programs write JSON values, and MongoDB keeps media as binary data or URLs, not ");
            String _literal_1 = type.getLiteral();
            String _plus_3 = (_plus_2 + _literal_1);
            String _plus_4 = (_plus_3 + " objects");
            throw Unsupported.of(_plus_4);
        }
      } else {
        String _literal = type.getLiteral();
        String _plus = (_literal + " field ");
        String _name = f.getName();
        String _plus_1 = (_plus + _name);
        String _plus_2 = (_plus_1 + "; generated programs write JSON values, and MongoDB keeps media as binary data or URLs, not ");
        String _literal_1 = type.getLiteral();
        String _plus_3 = (_plus_2 + _literal_1);
        String _plus_4 = (_plus_3 + " objects");
        throw Unsupported.of(_plus_4);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private void nullable(final Code.Entries schema) {
    final Code.Expr type = schema.get("bsonType");
    final List<Code.Expr> types = CollectionLiterals.<Code.Expr>newArrayList();
    if ((type instanceof Code.Items)) {
      List<Code.Expr> _items = ((Code.Items) type).items();
      Iterables.<Code.Expr>addAll(types, _items);
    } else {
      types.add(type);
    }
    Code.Text _text = MongodbCommands.text("null");
    types.add(_text);
    schema.put("bsonType", MongodbCommands.arr(types));
  }

  private void limitSchema(final Code.Entries schema, final FieldLimit limit) {
    String _literal = limit.getType().getLiteral();
    if (_literal != null) {
      switch (_literal) {
        case "vector_dim":
          schema.put("minItems", MongodbCommands.num(limit.getValue()));
          schema.put("maxItems", MongodbCommands.num(limit.getValue()));
          break;
        case "array_len":
          schema.put("maxItems", MongodbCommands.num(limit.getValue()));
          break;
        default:
          schema.put("maxLength", MongodbCommands.num(limit.getValue()));
          break;
      }
    } else {
      schema.put("maxLength", MongodbCommands.num(limit.getValue()));
    }
  }

  /**
   * The paths a vectorSearch filter can compare: boolean, date, numeric and string values and arrays of them.
   */
  private void filterPaths(final PayloadField f, final String path, final List<String> paths) {
    boolean _isEmpty = f.getSubFields().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      boolean _isNested = f.isNested();
      if (_isNested) {
        return;
      }
      Iterable<PayloadField> _filter = Iterables.<PayloadField>filter(f.getSubFields(), PayloadField.class);
      for (final PayloadField sub : _filter) {
        String _name = sub.getName();
        String _plus = ((path + ".") + _name);
        this.filterPaths(sub, _plus, paths);
      }
      return;
    }
    EScalarDType _xifexpression = null;
    if ((Objects.equals(f.getType(), EScalarDType.ARRAY) && ModelUtil.written(f, "elementType"))) {
      _xifexpression = f.getElementType();
    } else {
      _xifexpression = f.getType();
    }
    final EScalarDType type = _xifexpression;
    if (((((MongodbSchema.INTEGERS.contains(type) || MongodbSchema.NUMBERS.contains(type)) || MongodbSchema.STRINGS.contains(type)) || MongodbSchema.DATES.contains(type)) || Objects.equals(type, EScalarDType.BOOL))) {
      paths.add(path);
    }
  }

  private MongodbCollection.Field vectorField(final VectorCollection c, final VectorField f) {
    MongodbCollection.Field _xblockexpression = null;
    {
      final String fieldName = MongodbCommands.field(ModelUtil.fieldName(f));
      boolean _isMultiVector = f.isMultiVector();
      if (_isMultiVector) {
        throw Unsupported.of((("multi-vector field " + fieldName) + "; a vectorSearch index compares one vector per document path (nested vectors need nestedRoot)"));
      }
      if (((f.getModel() != null) || (ModelUtil.producingFunction(c, f) != null))) {
        throw Unsupported.of((("server-side embedding of " + fieldName) + MongodbStatements.NO_MODELS));
      }
      this.note(f, "onDisk");
      String _literal = f.getType().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "float_vector":
            break;
          case "int8_vector":
          case "binary_vector":
            String _literal_1 = f.getType().getLiteral();
            String _plus = (_literal_1 + " fields; ");
            String _plus_1 = (_plus + MongodbStatements.PENDING);
            String _plus_2 = (_plus_1 + " (BinData vectors of subtype int8 and int1 in the three runtimes)");
            throw Unsupported.of(_plus_2);
          case "sparse_float_vector":
            throw Unsupported.of("sparse vectors; vectorSearch indexes take dense vectors only (vector and filter fields)");
          default:
            String _literal_2 = f.getType().getLiteral();
            String _plus_3 = (_literal_2 + " fields; mongot indexes vectors of doubles and BinData vectors of float32, int8 and int1");
            throw Unsupported.of(_plus_3);
        }
      } else {
        String _literal_2 = f.getType().getLiteral();
        String _plus_3 = (_literal_2 + " fields; mongot indexes vectors of doubles and BinData vectors of float32, int8 and int1");
        throw Unsupported.of(_plus_3);
      }
      int _dimension = f.getDimension();
      boolean _lessEqualsThan = (_dimension <= 0);
      if (_lessEqualsThan) {
        throw Unsupported.of((("vector field " + fieldName) + " without dim"));
      }
      int _dimension_1 = f.getDimension();
      boolean _greaterThan = (_dimension_1 > MongodbCommands.MAX_DIMENSIONS);
      if (_greaterThan) {
        int _dimension_2 = f.getDimension();
        String _plus_4 = ((("vector field " + fieldName) + " with ") + Integer.valueOf(_dimension_2));
        String _plus_5 = (_plus_4 + " dimensions; vectorSearch indexes take at most 8192");
        throw Unsupported.of(_plus_5);
      }
      final VectorIndex index = ModelUtil.vectorIndex(c, f);
      final String chosenMetric = this.metric(f, index);
      final String sim = MongodbCommands.similarity(chosenMetric, f.isNormalized());
      final Code.Entries schema = MongodbCommands.obj("bsonType", MongodbCommands.text("array"), "minItems", MongodbCommands.num(f.getDimension()), "maxItems", MongodbCommands.num(f.getDimension()), "items", MongodbCommands.obj("bsonType", MongodbCommands.text("double")));
      boolean _isNullable = f.isNullable();
      if (_isNullable) {
        this.nullable(schema);
      }
      int _dimension_3 = f.getDimension();
      MongodbCollection.Vector _vector = new MongodbCollection.Vector(_dimension_3, sim, chosenMetric);
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
      _xblockexpression = new MongodbCollection.Field(fieldName, schema, false, null, _vector, false, _newArrayList, null);
    }
    return _xblockexpression;
  }

  /**
   * The vectorSearch index of a vector field: the declared index, else one named after the field with the defaults.
   */
  public MongodbCollection.VectorIndex vectorIndex(final VectorCollection c, final VectorField f) {
    MongodbCollection.VectorIndex _xblockexpression = null;
    {
      final VectorIndex index = ModelUtil.vectorIndex(c, f);
      final String fieldName = ModelUtil.fieldName(f);
      if ((index == null)) {
        Code.Entries _entries = Code.entries();
        return new MongodbCollection.VectorIndex((fieldName + "_index"), fieldName, _entries, null);
      }
      String _field = MongodbCommands.field(index.getName());
      Code.Entries _indexOptions = this.indexOptions(f, index);
      Integer _xifexpression = null;
      boolean _written = ModelUtil.written(index, "searchCandidates");
      if (_written) {
        _xifexpression = Integer.valueOf(index.getSearchCandidates());
      } else {
        _xifexpression = null;
      }
      _xblockexpression = new MongodbCollection.VectorIndex(_field, fieldName, _indexOptions, _xifexpression);
    }
    return _xblockexpression;
  }

  /**
   * The index options of a vector field: indexingMethod flat or hnsw with hnswOptions (maxEdges 16 to 64, numEdgeCandidates
   * 100 to 3200), quantization scalar or binary.
   */
  private Code.Entries indexOptions(final VectorField f, final VectorIndex index) {
    Code.Entries _xblockexpression = null;
    {
      this.note(index, "onDisk", "online");
      if ((ModelUtil.written(index, "accelerator") && Objects.equals(index.getAccelerator().getLiteral(), "gpu"))) {
        throw Unsupported.of("gpu acceleration; mongot builds its indexes on the CPU");
      }
      boolean _written = ModelUtil.written(index, "centroids");
      if (_written) {
        throw Unsupported.of("centroids; mongot builds hnsw and flat indexes, with no clustered (IVF) index");
      }
      Set<String> _keySet = ModelUtil.extParams(index, MongodbGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      final Code.Entries options = Code.entries();
      String _literal = index.getFamily().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "auto":
          case "hnsw":
            final Code.Entries hnsw = Code.entries();
            boolean _written_1 = ModelUtil.written(index, "maxDegree");
            if (_written_1) {
              if (((index.getMaxDegree() < 16) || (index.getMaxDegree() > 64))) {
                int _maxDegree = index.getMaxDegree();
                String _plus = ("max-degree " + Integer.valueOf(_maxDegree));
                String _plus_1 = (_plus + "; maxEdges of hnswOptions lies between 16 and 64");
                throw Unsupported.of(_plus_1);
              }
              hnsw.put("maxEdges", MongodbCommands.num(index.getMaxDegree()));
            }
            boolean _written_2 = ModelUtil.written(index, "buildCandidates");
            if (_written_2) {
              if (((index.getBuildCandidates() < 100) || (index.getBuildCandidates() > 3200))) {
                int _buildCandidates = index.getBuildCandidates();
                String _plus_2 = ("build-candidates " + Integer.valueOf(_buildCandidates));
                String _plus_3 = (_plus_2 + "; numEdgeCandidates of hnswOptions lies between 100 and 3200");
                throw Unsupported.of(_plus_3);
              }
              hnsw.put("numEdgeCandidates", MongodbCommands.num(index.getBuildCandidates()));
            }
            boolean _isEmpty = hnsw.entries().isEmpty();
            boolean _not = (!_isEmpty);
            if (_not) {
              options.put("hnswOptions", hnsw);
            }
            break;
          case "flat":
            if ((ModelUtil.written(index, "maxDegree") || ModelUtil.written(index, "buildCandidates"))) {
              throw Unsupported.of("max-degree and build-candidates on a flat index");
            }
            options.put("indexingMethod", MongodbCommands.text("flat"));
            break;
          default:
            String _literal_1 = index.getFamily().getLiteral();
            String _plus_4 = (_literal_1 + " indexes; mongot builds hnsw and flat vector indexes");
            throw Unsupported.of(_plus_4);
        }
      } else {
        String _literal_1 = index.getFamily().getLiteral();
        String _plus_4 = (_literal_1 + " indexes; mongot builds hnsw and flat vector indexes");
        throw Unsupported.of(_plus_4);
      }
      final Quantization q = index.getQuantization();
      if ((q != null)) {
        this.note(q, "keepOriginals");
        boolean _written_3 = ModelUtil.written(q, "truncateDimension");
        if (_written_3) {
          throw Unsupported.of("truncate-dim; mongot indexes the full vector");
        }
        if ((q.isRescore() || ModelUtil.written(q, "oversampling"))) {
          throw Unsupported.of("rescore and oversampling settings; mongot rescores quantized vectors itself and takes no oversampling factor (numCandidates is the effort setting)");
        }
        String _literal_2 = q.getType().getLiteral();
        if (_literal_2 != null) {
          switch (_literal_2) {
            case "none":
              break;
            case "scalar":
              if ((ModelUtil.written(q, "bits") && (q.getBits() != 8.0))) {
                double _bits = q.getBits();
                String _plus_5 = ("scalar quantization to " + Double.valueOf(_bits));
                String _plus_6 = (_plus_5 + " bits; scalar quantization in mongot keeps one byte per dimension");
                throw Unsupported.of(_plus_6);
              }
              options.put("quantization", MongodbCommands.text("scalar"));
              break;
            case "binary":
              if ((ModelUtil.written(q, "bits") && (q.getBits() != 1.0))) {
                double _bits_1 = q.getBits();
                String _plus_7 = ("binary quantization with " + Double.valueOf(_bits_1));
                String _plus_8 = (_plus_7 + " bits; binary quantization keeps one bit per dimension");
                throw Unsupported.of(_plus_8);
              }
              int _dimension = f.getDimension();
              int _modulo = (_dimension % 8);
              boolean _notEquals = (_modulo != 0);
              if (_notEquals) {
                int _dimension_1 = f.getDimension();
                String _plus_9 = ("binary quantization of " + Integer.valueOf(_dimension_1));
                String _plus_10 = (_plus_9 + " dimensions; numDimensions must be a multiple of 8");
                throw Unsupported.of(_plus_10);
              }
              options.put("quantization", MongodbCommands.text("binary"));
              break;
            default:
              throw Unsupported.of("product quantization; mongot quantizes with scalar and binary quantization");
          }
        } else {
          throw Unsupported.of("product quantization; mongot quantizes with scalar and binary quantization");
        }
      }
      _xblockexpression = options;
    }
    return _xblockexpression;
  }

  /**
   * The metric of a vector field: the field's, else its index's, else cosine.
   */
  public String metric(final VectorField f, final VectorIndex index) {
    String _xblockexpression = null;
    {
      EMetric _xifexpression = null;
      boolean _written = ModelUtil.written(f, "metric");
      if (_written) {
        _xifexpression = f.getMetric();
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
   * The keys of a regular index for a declared payload index; null when the index is part of the search index (full_text).
   */
  public Code.Entries payloadIndexKeys(final MongodbCollection current, final PayloadIndex payloadIndex) {
    Code.Entries _xblockexpression = null;
    {
      this.note(payloadIndex, "onDisk", "minTokenLength", "maxTokenLength", "castType");
      Set<String> _keySet = ModelUtil.extParams(payloadIndex, MongodbGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      if ((ModelUtil.written(payloadIndex, "structure") && Objects.equals(payloadIndex.getStructure().getLiteral(), "bitmap"))) {
        throw Unsupported.of("bitmap payload indexes; MongoDB builds B-tree, 2dsphere, text, hashed and wildcard indexes");
      }
      boolean _isLowercase = payloadIndex.isLowercase();
      if (_isLowercase) {
        throw Unsupported.of("lowercase payload indexes; a case-insensitive index needs a collation, which also changes how every query on the collection compares strings");
      }
      boolean _isPrefixMatch = payloadIndex.isPrefixMatch();
      if (_isPrefixMatch) {
        throw Unsupported.of("prefix-match payload indexes; a B-tree index serves anchored $regex prefixes already, and Atlas Search prefixes are the autocomplete type of vecdsl_text");
      }
      Analyzer _analyzer = payloadIndex.getAnalyzer();
      boolean _tripleNotEquals = (_analyzer != null);
      if (_tripleNotEquals) {
        throw Unsupported.of("an analyzer on a payload index; declare the analyzer on the full-text field");
      }
      final FieldPath path = payloadIndex.getTarget();
      String _xifexpression = null;
      String _rawPath = path.getRawPath();
      boolean _tripleNotEquals_1 = (_rawPath != null);
      if (_tripleNotEquals_1) {
        _xifexpression = MongodbCommands.field(path.getRawPath());
      } else {
        String _xblockexpression_1 = null;
        {
          Field _field = path.getField();
          boolean _not = (!(_field instanceof PayloadField));
          if (_not) {
            throw Unsupported.of("a payload index on a vector field; vector fields are indexed by vectorSearch indexes");
          }
          String _path = current.path(ModelUtil.fieldName(path.getField()));
          final Function1<Field, String> _function = (Field s) -> {
            return ModelUtil.fieldName(s);
          };
          List<String> _map = ListExtensions.<Field, String>map(path.getSubFields(), _function);
          _xblockexpression_1 = IterableExtensions.join(Iterables.<String>concat(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_path)), _map), ".");
        }
        _xifexpression = _xblockexpression_1;
      }
      final String dotted = _xifexpression;
      Code.Entries _switchResult = null;
      String _literal = payloadIndex.getKind().getLiteral();
      if (_literal != null) {
        switch (_literal) {
          case "full_text":
            Object _xblockexpression_2 = null;
            {
              final Field leaf = ModelUtil.leaf(path);
              boolean _not = (!((leaf instanceof PayloadField) && ((PayloadField) leaf).isFullText()));
              if (_not) {
                throw Unsupported.of((("a full_text index on " + dotted) + ", which is not a full-text field (declare full-text on the field; it joins the search index vecdsl_text)"));
              }
              _xblockexpression_2 = null;
            }
            _switchResult = ((Code.Entries)_xblockexpression_2);
            break;
          case "geo":
            _switchResult = MongodbCommands.obj(dotted, MongodbCommands.text("2dsphere"));
            break;
          case "json":
            throw Unsupported.of("json payload indexes; MongoDB wildcard indexes cover paths, not a cast value type");
          default:
            _switchResult = MongodbCommands.obj(dotted, MongodbCommands.num(1));
            break;
        }
      } else {
        _switchResult = MongodbCommands.obj(dotted, MongodbCommands.num(1));
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  /**
   * The name a field's analyzer has in the search index: lucene.<language> for a plain language analyzer, else the custom name.
   */
  public String analyzerName(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      final String language = this.languageOf(analyzer);
      String _xifexpression = null;
      boolean _builtIn = this.builtIn(analyzer, language);
      if (_builtIn) {
        _xifexpression = ("lucene." + language);
      } else {
        _xifexpression = MongodbCommands.field(analyzer.getName());
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private String languageOf(final Analyzer analyzer) {
    String _xblockexpression = null;
    {
      String _language = analyzer.getLanguage();
      boolean _tripleEquals = (_language == null);
      if (_tripleEquals) {
        return null;
      }
      final String language = MongodbSchema.LANGUAGES.get(analyzer.getLanguage().toLowerCase());
      if ((language == null)) {
        String _language_1 = analyzer.getLanguage();
        String _plus = ("the language " + _language_1);
        String _plus_1 = (_plus + "; the generator uses the Lucene language analyzers of Atlas Search for english, german, french, spanish, italian, portuguese, dutch, russian, swedish, danish, finnish, norwegian, hungarian, romanian and turkish");
        throw Unsupported.of(_plus_1);
      }
      _xblockexpression = language;
    }
    return _xblockexpression;
  }

  private boolean builtIn(final Analyzer analyzer, final String language) {
    return (((language != null) && analyzer.getFilters().isEmpty()) && (Objects.equals(analyzer.getTokenizer().getLiteral(), "standard") || Objects.equals(analyzer.getTokenizer().getLiteral(), "language")));
  }

  /**
   * Custom analyzers of the search index; plain language analyzers are the built-in lucene.<language> ones.
   */
  public List<Code.Expr> analyzers(final List<Analyzer> declared) {
    List<Code.Expr> _xblockexpression = null;
    {
      final List<Code.Expr> result = CollectionLiterals.<Code.Expr>newArrayList();
      for (final Analyzer analyzer : declared) {
        {
          boolean _isEmpty = analyzer.getResources().isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            throw Unsupported.of(("analyzer resources; " + MongodbStatements.NO_FILES));
          }
          if ((ModelUtil.written(analyzer, "bm25K1") || ModelUtil.written(analyzer, "bm25B"))) {
            throw Unsupported.of("BM25 parameters; string fields of a search index take the similarity bm25, boolean or stableTfl without parameters");
          }
          if ((ModelUtil.written(analyzer, "stopwords") && (!Objects.equals(analyzer.getStopwords(), "none")))) {
            String _stopwords = analyzer.getStopwords();
            String _plus = ("stopwords " + _stopwords);
            String _plus_1 = (_plus + "; the stopword token filter of Atlas Search takes an explicit list of tokens, not a named list");
            throw Unsupported.of(_plus_1);
          }
          Set<String> _keySet = ModelUtil.extParams(analyzer, MongodbGenerator.VENDOR).keySet();
          for (final String key : _keySet) {
            throw Unsupported.of(("extension parameter " + key));
          }
          final String language = this.languageOf(analyzer);
          final String tokenizerType = MongodbSchema.TOKENIZERS.get(analyzer.getTokenizer().getLiteral());
          if ((tokenizerType == null)) {
            String _literal = analyzer.getTokenizer().getLiteral();
            String _plus_2 = ("the " + _literal);
            String _plus_3 = (_plus_2 + " tokenizer; Atlas Search has icuFolding and icuNormalizer token filters but no icu tokenizer");
            throw Unsupported.of(_plus_3);
          }
          boolean _builtIn = this.builtIn(analyzer, language);
          boolean _not_1 = (!_builtIn);
          if (_not_1) {
            Code.Entries _xifexpression = null;
            if ((Objects.equals(tokenizerType, "nGram") || Objects.equals(tokenizerType, "edgeGram"))) {
              _xifexpression = MongodbCommands.obj("type", MongodbCommands.text(tokenizerType), "minGram", MongodbCommands.num(1), "maxGram", MongodbCommands.num(2));
            } else {
              _xifexpression = MongodbCommands.obj("type", MongodbCommands.text(tokenizerType));
            }
            final Code.Entries tokenizer = _xifexpression;
            final List<Code.Expr> chain = CollectionLiterals.<Code.Expr>newArrayList();
            Code.Entries _obj = MongodbCommands.obj("type", MongodbCommands.text("lowercase"));
            chain.add(_obj);
            EList<AnalyzerFilter> _filters = analyzer.getFilters();
            for (final AnalyzerFilter analyzerFilter : _filters) {
              String _literal_1 = analyzerFilter.getType().getLiteral();
              if (_literal_1 != null) {
                switch (_literal_1) {
                  case "lowercase":
                    break;
                  case "stemmer":
                    if ((language == null)) {
                      throw Unsupported.of("the stemmer filter without a language");
                    }
                    Code.Entries _obj_1 = MongodbCommands.obj("type", MongodbCommands.text("snowballStemming"), "stemmerName", MongodbCommands.text(language));
                    chain.add(_obj_1);
                    break;
                  case "ascii_folding":
                    Code.Entries _obj_2 = MongodbCommands.obj("type", MongodbCommands.text("asciiFolding"));
                    chain.add(_obj_2);
                    break;
                  case "stop":
                    throw Unsupported.of("the stop filter; the stopword token filter of Atlas Search takes an explicit list of tokens (a language without filters uses the lucene.<language> analyzer, which removes its stop words)");
                  case "length":
                    throw Unsupported.of("the length filter; vecDSL gives no bounds");
                  case "synonym":
                    throw Unsupported.of("the synonym filter; Atlas Search reads synonyms from a collection named in a synonym mapping, not from a filter");
                  case "decompounder":
                    throw Unsupported.of("the decompounder filter; Atlas Search has no dictionary decompounder token filter");
                  default:
                    throw Unsupported.of("the phonetic filter; Atlas Search has daitchMokotoffSoundex only, which is not a general phonetic filter");
                }
              } else {
                throw Unsupported.of("the phonetic filter; Atlas Search has daitchMokotoffSoundex only, which is not a general phonetic filter");
              }
            }
            if (((language != null) && analyzer.getFilters().isEmpty())) {
              Code.Entries _obj_3 = MongodbCommands.obj("type", MongodbCommands.text("snowballStemming"), "stemmerName", MongodbCommands.text(language));
              chain.add(_obj_3);
            }
            Code.Entries _obj_4 = MongodbCommands.obj("name", MongodbCommands.text(MongodbCommands.field(analyzer.getName())), "tokenizer", tokenizer, "tokenFilters", MongodbCommands.arr(chain));
            result.add(_obj_4);
          }
        }
      }
      _xblockexpression = result;
    }
    return _xblockexpression;
  }

  /**
   * The statements that create a collection: create with the validator, createIndexes for payload indexes and the TTL index,
   * createSearchIndexes, views for aliases, then await every search index. With if-not-exists they run only when the collection
   * is missing.
   */
  public List<Code.Stmt> createCollection(final VectorCollection c, final boolean ifNotExists, final boolean fresh) {
    List<Code.Stmt> _xblockexpression = null;
    {
      ExternalSource _external = c.getExternal();
      boolean _tripleNotEquals = (_external != null);
      if (_tripleNotEquals) {
        throw Unsupported.of(MongodbStatements.EXTERNAL);
      }
      boolean _isAutoId = c.isAutoId();
      if (_isAutoId) {
        throw Unsupported.of((("auto-id; " + MongodbStatements.PENDING) + " (ObjectId keys returned as strings)"));
      }
      boolean _isDeletionProtection = c.isDeletionProtection();
      if (_isDeletionProtection) {
        throw Unsupported.of(MongodbStatements.NO_DELETION_PROTECTION);
      }
      boolean _isEmpty = c.getTags().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        throw Unsupported.of(MongodbStatements.NO_TAGS);
      }
      boolean _isEmpty_1 = c.getCrossReferences().isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        throw Unsupported.of("cross-ref; MongoDB has no references between collections ($lookup joins at query time)");
      }
      EmbeddingModel _embeddingModel = c.getEmbeddingModel();
      boolean _tripleNotEquals_1 = (_embeddingModel != null);
      if (_tripleNotEquals_1) {
        throw Unsupported.of(("embedding-model" + MongodbStatements.NO_MODELS));
      }
      boolean _isEmpty_2 = c.getFunctions().isEmpty();
      boolean _not_2 = (!_isEmpty_2);
      if (_not_2) {
        String _literal = IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral();
        String _plus = (_literal + " functions; ");
        String _functionReason = MongodbStatements.functionReason(IterableExtensions.<Function>head(c.getFunctions()).getType().getLiteral());
        String _plus_1 = (_plus + _functionReason);
        throw Unsupported.of(_plus_1);
      }
      StorageConfig _storage = c.getStorage();
      boolean _tripleNotEquals_2 = (_storage != null);
      if (_tripleNotEquals_2) {
        MongodbStatements.storage(c.getStorage());
      }
      Cluster _elvis = null;
      Cluster _cluster = c.getCluster();
      if (_cluster != null) {
        _elvis = _cluster;
      } else {
        Cluster _containerOfType = EcoreUtil2.<Cluster>getContainerOfType(c, Cluster.class);
        _elvis = _containerOfType;
      }
      MongodbStatements.cluster(_elvis);
      Set<String> _keySet = ModelUtil.extParams(c, MongodbGenerator.VENDOR).keySet();
      for (final String key : _keySet) {
        throw Unsupported.of(("extension parameter " + key));
      }
      this.note(c, "idType", "generativeModel");
      boolean _isEmpty_3 = c.getStoredSourceFields().isEmpty();
      boolean _not_3 = (!_isEmpty_3);
      if (_not_3) {
        this.notes.add("stored-source on VectorCollection, which mongot decides (documents are read from mongod)");
      }
      if (fresh) {
        this.evolve(c, null);
      }
      final MongodbCollection current = this.collection(c);
      List<Partition> _partitions = ModelUtil.partitions(c);
      for (final Partition partition : _partitions) {
        MongodbStatements.checkPartition(partition);
      }
      final List<Code.Stmt> steps = CollectionLiterals.<Code.Stmt>newArrayList();
      Code.Call _command = MongodbCommands.command(current.database, MongodbCommands.obj("create", MongodbCommands.text(current.name), "validator", current.validator(), "validationLevel", MongodbCommands.text("strict"), 
        "validationAction", MongodbCommands.text("error")));
      Code.Run _run = new Code.Run(_command);
      steps.add(_run);
      List<Code.Stmt> _indexStatements = this.indexStatements(current, c);
      Iterables.<Code.Stmt>addAll(steps, _indexStatements);
      EList<Alias> _aliases = c.getAliases();
      for (final Alias alias : _aliases) {
        Code.Call _command_1 = MongodbCommands.command(current.database, MongodbCommands.obj("create", MongodbCommands.text(MongodbCommands.checkedName(alias.getName(), "view")), "viewOn", MongodbCommands.text(current.name), "pipeline", MongodbCommands.arr()));
        Code.Run _run_1 = new Code.Run(_command_1);
        steps.add(_run_1);
      }
      List<Code.Stmt> _xifexpression = null;
      if (ifNotExists) {
        Code.Call _call = MongodbCommands.call("exists", "database", MongodbCommands.text(current.database), "collection", MongodbCommands.text(current.name));
        Code.When _when = new Code.When(_call, true, steps);
        _xifexpression = CollectionLiterals.<Code.Stmt>newArrayList(((Code.Stmt) _when));
      } else {
        _xifexpression = steps;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  /**
   * createIndexes for the payload and TTL indexes, createSearchIndexes and an await of every search index.
   */
  public List<Code.Stmt> indexStatements(final MongodbCollection current, final VectorCollection c) {
    List<Code.Stmt> _xblockexpression = null;
    {
      final List<Code.Stmt> steps = CollectionLiterals.<Code.Stmt>newArrayList();
      final List<Code.Expr> regular = CollectionLiterals.<Code.Expr>newArrayList();
      for (final MongodbCollection.PayloadIndex index : current.payloadIndexes) {
        Code.Entries _obj = MongodbCommands.obj("key", index.keys(), "name", MongodbCommands.text(index.name()));
        regular.add(_obj);
      }
      if ((((current.ttlSeconds != null) || current.expiring) || ((c != null) && (c.getTtlField() != null)))) {
        if (((c != null) && (c.getTtlField() != null))) {
          final MongodbCollection.Field ttlField = current.field(ModelUtil.fieldName(c.getTtlField()));
          boolean _date = ttlField.date();
          boolean _not = (!_date);
          if (_not) {
            throw Unsupported.of("a ttl-field that is not a date field; TTL indexes expire documents by a date value");
          }
          Long _elvis = null;
          if (current.ttlSeconds != null) {
            _elvis = current.ttlSeconds;
          } else {
            _elvis = Long.valueOf(0L);
          }
          Code.Entries _obj_1 = MongodbCommands.obj("key", MongodbCommands.obj(ttlField.name(), MongodbCommands.num(1)), "name", MongodbCommands.text("vecdsl_ttl"), "expireAfterSeconds", MongodbCommands.num(_elvis.doubleValue()));
          regular.add(_obj_1);
        } else {
          Code.Entries _obj_2 = MongodbCommands.obj("key", MongodbCommands.obj(MongodbCommands.EXPIRES_FIELD, MongodbCommands.num(1)), "name", MongodbCommands.text("vecdsl_ttl"), "expireAfterSeconds", MongodbCommands.num(0));
          regular.add(_obj_2);
        }
      }
      boolean _isEmpty = regular.isEmpty();
      boolean _not_1 = (!_isEmpty);
      if (_not_1) {
        Code.Call _command = MongodbCommands.command(current.database, MongodbCommands.obj("createIndexes", MongodbCommands.text(current.name), "indexes", MongodbCommands.arr(regular)));
        Code.Run _run = new Code.Run(_command);
        steps.add(_run);
      }
      final List<Code.Expr> searchIndexes = current.searchIndexes();
      boolean _isEmpty_1 = searchIndexes.isEmpty();
      boolean _not_2 = (!_isEmpty_1);
      if (_not_2) {
        Code.Call _command_1 = MongodbCommands.command(current.database, MongodbCommands.obj("createSearchIndexes", MongodbCommands.text(current.name), "indexes", MongodbCommands.arr(searchIndexes)));
        Code.Run _run_1 = new Code.Run(_command_1);
        steps.add(_run_1);
        for (final Code.Expr index_1 : searchIndexes) {
          Code.Expr _get = ((Code.Entries) index_1).get("name");
          Code.Stmt _awaitIndex = this.awaitIndex(current, ((Code.Text) _get).value(), MongodbCommands.AWAIT_SECONDS);
          steps.add(_awaitIndex);
        }
      }
      _xblockexpression = steps;
    }
    return _xblockexpression;
  }

  public Code.Stmt awaitIndex(final MongodbCollection current, final String index, final int seconds) {
    Code.Call _call = MongodbCommands.call("await_index", "database", MongodbCommands.text(current.database), "collection", MongodbCommands.text(current.name), "index", MongodbCommands.text(index), "timeout_seconds", MongodbCommands.num(seconds));
    return new Code.Run(_call);
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
}
