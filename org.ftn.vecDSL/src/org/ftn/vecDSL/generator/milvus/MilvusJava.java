package org.ftn.vecDSL.generator.milvus;

import static org.ftn.vecDSL.generator.code.Syntax.camel;
import static org.ftn.vecDSL.generator.code.Syntax.indent;
import static org.ftn.vecDSL.generator.code.Syntax.number;
import static org.ftn.vecDSL.generator.code.Syntax.pascal;
import static org.ftn.vecDSL.generator.code.Syntax.quote;
import static org.ftn.vecDSL.generator.code.Syntax.words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Code.Call;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Env;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Fail;
import org.ftn.vecDSL.generator.code.Code.Filter;
import org.ftn.vecDSL.generator.code.Code.Flag;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Code.Let;
import org.ftn.vecDSL.generator.code.Code.Make;
import org.ftn.vecDSL.generator.code.Code.Nil;
import org.ftn.vecDSL.generator.code.Code.Num;
import org.ftn.vecDSL.generator.code.Code.Param;
import org.ftn.vecDSL.generator.code.Code.Ref;
import org.ftn.vecDSL.generator.code.Code.Return;
import org.ftn.vecDSL.generator.code.Code.Run;
import org.ftn.vecDSL.generator.code.Code.Sparse;
import org.ftn.vecDSL.generator.code.Code.Stmt;
import org.ftn.vecDSL.generator.code.Code.Sym;
import org.ftn.vecDSL.generator.code.Code.Target;
import org.ftn.vecDSL.generator.code.Code.Text;
import org.ftn.vecDSL.generator.code.Code.When;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.common.Unsupported;

/** Java 17 with the Milvus Java SDK v2 (MilvusClientV2 and request builders). */
public final class MilvusJava implements MilvusPrinter {

	static final String RUNTIME = "MilvusRuntime";

	private static final String ROWS = "List<Map<String, Object>>";
	private static final String MAP = "Map<String, Object>";
	private static final Map<String, String> RUNTIME_TYPES = Map.ofEntries(Map.entry("hits", ROWS),
			Map.entry("get_rows", ROWS), Map.entry("scroll", ROWS), Map.entry("distance_matrix", ROWS),
			Map.entry("read_records", ROWS), Map.entry("insert_rows", MAP), Map.entry("upsert_rows", MAP),
			Map.entry("update_rows", MAP), Map.entry("delete_rows", MAP), Map.entry("compact", MAP),
			Map.entry("describe", MAP), Map.entry("await_index", MAP), Map.entry("count", "long"),
			Map.entry("list_objects", "List<String>"), Map.entry("has_collection", "boolean"),
			Map.entry("has_database", "boolean"), Map.entry("time_value", "double"),
			Map.entry("example_ids", "List<Object>"), Map.entry("lookup_vector", "List<Float>"),
			Map.entry("recommend_vector", "List<Float>"), Map.entry("has_partition", "boolean"),
			Map.entry("check_analyzer", "List<String>"), Map.entry("delete_matching", MAP), Map.entry("sample_rows", ROWS),
			Map.entry("bulk_import", MAP), Map.entry("restore_snapshot", MAP), Map.entry("clone_collection", MAP));
	private static final Set<String> CLIENTLESS = Set.of("hits", "read_records", "time_value", "example_ids");
	private static final Map<String, String> PARAMETER_TYPES = Map.of("vector", "List<Float>", "sparse_vector",
			"Map<Long, Float>", "text", "String", "int", "long", "float", "double", "bool", "boolean", "string_list",
			"List<String>", "records", ROWS, "json", "Object");
	private static final Map<String, String> PARAMETER_READERS = Map.of("vector", "floats", "sparse_vector",
			"sparseMap", "text", "text", "int", "integer", "float", "decimal", "bool", "flag", "string_list", "texts",
			"records", "records", "json", "");
	private static final Map<String, String> DATA_TYPES = Map.ofEntries(Map.entry("INT64", "Int64"),
			Map.entry("INT8", "Int8"), Map.entry("FLOAT", "Float"), Map.entry("DOUBLE", "Double"),
			Map.entry("BOOL", "Bool"), Map.entry("VARCHAR", "VarChar"), Map.entry("TIMESTAMPTZ", "Timestamptz"),
			Map.entry("JSON", "JSON"), Map.entry("ARRAY", "Array"), Map.entry("GEOMETRY", "Geometry"),
			Map.entry("FLOAT_VECTOR", "FloatVector"), Map.entry("FLOAT16_VECTOR", "Float16Vector"),
			Map.entry("BFLOAT16_VECTOR", "BFloat16Vector"), Map.entry("INT8_VECTOR", "Int8Vector"),
			Map.entry("BINARY_VECTOR", "BinaryVector"), Map.entry("SPARSE_FLOAT_VECTOR", "SparseFloatVector"));
	private static final Map<String, String> REQUESTS = Map.ofEntries(
			Map.entry("drop_collection", "collection.request.DropCollectionReq"),
			Map.entry("create_index", "index.request.CreateIndexReq"),
			Map.entry("create_database", "database.request.CreateDatabaseReq"),
			Map.entry("drop_database", "database.request.DropDatabaseReq"),
			Map.entry("create_alias", "utility.request.CreateAliasReq"),
			Map.entry("drop_alias", "utility.request.DropAliasReq"),
			Map.entry("alter_alias", "utility.request.AlterAliasReq"),
			Map.entry("rename_collection", "collection.request.RenameCollectionReq"),
			Map.entry("alter_collection_properties", "collection.request.AlterCollectionPropertiesReq"),
			Map.entry("add_collection_function", "collection.request.AddCollectionFunctionReq"),
			Map.entry("drop_collection_function", "collection.request.DropCollectionFunctionReq"),
			Map.entry("add_collection_field", "collection.request.AddCollectionFieldReq"),
			Map.entry("create_partition", "partition.request.CreatePartitionReq"),
			Map.entry("drop_partition", "partition.request.DropPartitionReq"),
			Map.entry("load_partitions", "partition.request.LoadPartitionsReq"),
			Map.entry("release_partitions", "partition.request.ReleasePartitionsReq"),
			Map.entry("load_collection", "collection.request.LoadCollectionReq"),
			Map.entry("release_collection", "collection.request.ReleaseCollectionReq"),
			Map.entry("drop_index", "index.request.DropIndexReq"),
			Map.entry("truncate_collection", "collection.request.TruncateCollectionReq"),
			Map.entry("flush", "utility.request.FlushReq"),
			Map.entry("create_collection", "collection.request.CreateCollectionReq"),
			Map.entry("search", "vector.request.SearchReq"),
			Map.entry("hybrid_search", "vector.request.HybridSearchReq"));
	/** milvus-sdk-java 3.0.12 QueryIterator.executeQuery sets use_default_consistency, so the server ignores the requested level. */
	public static final String SCROLL_CONSISTENCY = "the 'consistency' clause on scroll in Java; milvus-sdk-java 3.0.12 query iterators send"
			+ " use_default_consistency, so Milvus applies the collection's level";
	private static final Map<String, String> SETTERS = Map.ofEntries(Map.entry("collection_name", "collectionName"),
			Map.entry("db_name", "databaseName"), Map.entry("old_name", "collectionName"),
			Map.entry("new_name", "newCollectionName"), Map.entry("alias", "alias"),
			Map.entry("partition_name", "partitionName"), Map.entry("partition_names", "partitionNames"),
			Map.entry("index_name", "indexName"), Map.entry("function_name", "functionName"),
			Map.entry("properties", "properties"), Map.entry("field_name", "fieldName"),
			Map.entry("anns_field", "annsField"), Map.entry("limit", "limit"), Map.entry("filter", "filter"),
			Map.entry("output_fields", "outputFields"), Map.entry("group_by_field", "groupByFieldName"),
			Map.entry("group_size", "groupSize"), Map.entry("consistency_level", "consistencyLevel"),
			Map.entry("offset", "offset"),
			Map.entry("ranker", "ranker"), Map.entry("data", "data"), Map.entry("index_params", "indexParams"),
			Map.entry("function", "function"), Map.entry("num_shards", "numShards"),
			Map.entry("num_partitions", "numPartitions"), Map.entry("description", "description"),
			Map.entry("enable_dynamic_field", "enableDynamicField"));
	private static final Map<String, String> FIELD_SETTERS = Map.ofEntries(Map.entry("field_name", "fieldName"),
			Map.entry("datatype", "dataType"), Map.entry("is_primary", "isPrimaryKey"),
			Map.entry("max_length", "maxLength"), Map.entry("dim", "dimension"),
			Map.entry("element_type", "elementType"), Map.entry("max_capacity", "maxCapacity"),
			Map.entry("nullable", "isNullable"), Map.entry("default_value", "defaultValue"),
			Map.entry("is_partition_key", "isPartitionKey"), Map.entry("enable_analyzer", "enableAnalyzer"),
			Map.entry("analyzer_params", "analyzerParams"), Map.entry("enable_match", "enableMatch"),
			Map.entry("description", "description"));
	private static final Set<String> KEYWORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case",
			"catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
			"final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
			"long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
			"strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
			"volatile", "while", "true", "false", "null", "var", "record", "yield", "client");

	private final Set<String> imports = new TreeSet<>();
	private final Map<String, String> locals = new HashMap<>();
	private Map<String, String> parameterTypes = Map.of();

	@Override
	public String language() {
		return "java";
	}

	public static String packageName(Program program) {
		return "vecdsl." + String.join("", words(program.name()));
	}

	@Override
	public Map<String, String> files(Program program, MilvusManifest manifest) {
		String pkg = packageName(program);
		String folder = "src/main/java/" + pkg.replace('.', '/') + "/";
		Map<String, String> placeholders = Map.of("java.package", pkg, "java.main", pkg + "." + pascal(program.name()),
				"java.artifact", String.join("-", words(program.name())));
		Map<String, String> files = new LinkedHashMap<>();
		files.put(folder + pascal(program.name()) + ".java", module(program, manifest));
		files.put(folder + RUNTIME + ".java", manifest.render("java/" + RUNTIME + ".java.template", placeholders));
		files.put("pom.xml", manifest.render("java/pom.xml", placeholders));
		return files;
	}

	String module(Program program, MilvusManifest manifest) {
		imports.clear();
		imports.add("io.milvus.v2.client.ConnectConfig");
		imports.add("io.milvus.v2.client.MilvusClientV2");
		imports.add("java.util.LinkedHashMap");
		imports.add("java.util.List");
		imports.add("java.util.Map");
		parameterTypes = program.parameters().stream()
				.collect(Collectors.toMap(Program.Parameter::name, Program.Parameter::type));
		String className = pascal(program.name());
		List<String> members = new ArrayList<>();
		members.add("private final MilvusClientV2 client;");
		members.add("public " + className + "(MilvusClientV2 client) {\n    this.client = client;\n    checkServer(client);\n}");
		members.add(connect(program));
		members.add("@Override\npublic void close() {\n    client.close();\n}");
		for (Program.Method method : program.methods()) {
			members.add(method(method));
		}
		members.add(run(program, className));
		members.add(main(className));
		String pkg = packageName(program);
		List<String> javaImports = imports.stream().filter(i -> i.startsWith("java.")).toList();
		List<String> otherImports = imports.stream().filter(i -> !i.startsWith("java.")).toList();
		return String.join("\n", "package " + pkg + ";", "", "import static " + pkg + "." + RUNTIME + ".*;", "",
				otherImports.stream().map(i -> "import " + i + ";").collect(Collectors.joining("\n")), "",
				javaImports.stream().map(i -> "import " + i + ";").collect(Collectors.joining("\n")), "",
				header(program, manifest), "public final class " + className + " implements AutoCloseable {", "",
				indent(String.join("\n\n", members), 1), "}", "");
	}

	private String header(Program program, MilvusManifest manifest) {
		List<String> lines = new ArrayList<>();
		lines.add("Generated by vecDSL from script " + program.name()
				+ (program.version() == null ? "" : " version " + program.version()) + ".");
		if (program.description() != null) {
			lines.add(program.description());
		}
		lines.add("");
		lines.add("Target: " + manifest.product() + " standalone (docker compose up -d) through "
				+ manifest.get("sdk.java.group") + ":" + manifest.get("sdk.java.artifact") + " "
				+ manifest.get("sdk.java.version") + ", Java >= " + manifest.get("java.release") + ".");
		lines.add("Set " + URI_VARIABLE + " to use another deployment.");
		return "/**\n" + lines.stream().map(line -> (" * " + line.replace("*/", "* /")).stripTrailing())
				.collect(Collectors.joining("\n")) + "\n */";
	}

	private String connect(Program program) {
		Program.Connection connection = program.connection();
		List<String> calls = new ArrayList<>();
		calls.add(".uri(envOr(" + quote(URI_VARIABLE) + ", " + quote(connection.uri()) + "))");
		if (connection.token() != null) {
			calls.add(".token(" + text(connection.token()) + ")");
		}
		if (connection.user() != null) {
			calls.add(".username(" + quote(connection.user()) + ")");
		}
		if (connection.password() != null) {
			calls.add(".password(" + text(connection.password()) + ")");
		}
		if (connection.database() != null) {
			calls.add(".dbName(" + quote(connection.database()) + ")");
		}
		if (connection.timeoutSeconds() != null) {
			calls.add(".rpcDeadlineMs(" + (long) (connection.timeoutSeconds() * 1000) + "L)");
		}
		return "public static MilvusClientV2 connect() {\n    return " + RUNTIME + ".connect(" + builder("ConnectConfig", calls)
				.replace("\n", "\n    ") + ");\n}";
	}

	private String method(Program.Method method) {
		locals.clear();
		List<String> body = new ArrayList<>();
		for (String feature : method.serverFeatures()) {
			body.add("requireServer(client, " + quote(feature) + ");");
		}
		String type;
		try {
			if (droppedByTheSdk(method.body())) {
				throw Unsupported.of(SCROLL_CONSISTENCY);
			}
			for (Stmt statement : method.body()) {
				body.add(statement(statement));
			}
			type = returnType(method.body());
		} catch (Unsupported e) {
			body.clear();
			body.add(fail(e.getMessage()));
			type = "void";
		}
		String parameters = method.parameters().stream()
				.map(p -> PARAMETER_TYPES.getOrDefault(parameterTypes.getOrDefault(p, "json"), "Object") + " " + ident(p))
				.collect(Collectors.joining(", "));
		String doc = method.notes().isEmpty() ? ""
				: "/** Ignored on Milvus: " + String.join(", ", method.notes()).replace("*/", "* /") + ". */\n";
		String visibility = method.helper() ? "private " : "public ";
		return doc + visibility + type + " " + ident(method.name()) + "(" + parameters + ") {"
				+ (body.isEmpty() ? "" : "\n" + indent(String.join("\n", body), 1)) + "\n}";
	}

	/** Whether the body scrolls with a consistency level, which the Java SDK's query iterator does not send. */
	private static boolean droppedByTheSdk(List<Stmt> body) {
		List<Call> scrolls = new ArrayList<>();
		Code.visit(body, expr -> {
			if (expr instanceof Call call && call.target() == Target.RUNTIME && call.op().equals("scroll")
					&& call.args().has("consistency_level")) {
				scrolls.add(call);
			}
		});
		return !scrolls.isEmpty();
	}

	private String returnType(List<Stmt> body) {
		for (Stmt statement : body) {
			if (statement instanceof Return ret) {
				return typeOf(ret.value());
			}
			if (statement instanceof When when) {
				String nested = returnType(when.body());
				if (!nested.equals("void")) {
					return nested;
				}
			}
		}
		return "void";
	}

	private String typeOf(Expr expr) {
		if (expr instanceof Call call && call.target() == Target.RUNTIME) {
			return RUNTIME_TYPES.getOrDefault(call.op(), "Object");
		}
		if (expr instanceof Ref ref) {
			return locals.getOrDefault(ref.name(), "Object");
		}
		if (expr instanceof Items) {
			return "List<Object>";
		}
		return "Object";
	}

	private String statement(Stmt statement) {
		return switch (statement) {
		case Let let -> {
			locals.put(let.name(), typeOf(let.value()));
			yield "var " + ident(let.name()) + " = " + expr(let.value()) + ";";
		}
		case Run run -> expr(run.value()) + ";";
		case Return ret -> "return " + expr(ret.value()) + ";";
		case When when -> "if (" + (when.negated() ? "!" : "") + expr(when.condition()) + ") {\n"
				+ indent(when.body().stream().map(this::statement).collect(Collectors.joining("\n")), 1) + "\n}";
		case Fail fail -> fail(fail.feature());
		};
	}

	private String fail(String feature) {
		return "throw unsupported(" + quote(VENDOR) + ", " + quote(feature) + ");";
	}

	/** A value in an untyped position: a runtime argument, a row, a list element or a template value. */
	String expr(Expr expr) {
		return switch (expr) {
		case Text text -> quote(text.value());
		case Num num -> number(num.value());
		case Flag flag -> String.valueOf(flag.value());
		case Nil nil -> "null";
		case Items items -> list(items.items().stream().map(this::expr).toList());
		case Entries entries -> args(entries);
		case Sparse sparse -> sparse(sparse);
		case Param param -> ident(param.name());
		case Env env -> "env(" + quote(env.variable()) + ")";
		case Ref ref -> ident(ref.name());
		case Sym sym -> symbol(sym);
		case Filter filter -> filter(filter);
		case Call call -> call(call);
		case Make make -> make(make);
		};
	}

	private String make(Make make) {
		return switch (make.type()) {
		case "Vector" -> vector(make.args());
		case "Function" -> function(make.args());
		case "Ranker" -> ranker(make.args());
		case "AnnRequest" -> annRequest(make.args());
		case "Index" -> index(make.args());
		default -> throw Unsupported.of(make.type() + " values in Java");
		};
	}

	private String args(Entries entries) {
		List<String> items = new ArrayList<>();
		entries.entries().forEach((key, value) -> {
			items.add(quote(key));
			items.add(expr(value));
		});
		return wrapCall("args", items);
	}

	private String sparse(Sparse sparse) {
		return "sparse(new long[] {" + sparse.weights().keySet().stream().map(k -> k + "L").collect(Collectors.joining(", "))
				+ "}, new float[] {" + sparse.weights().values().stream().map(MilvusJava::floatLiteral)
						.collect(Collectors.joining(", ")) + "})";
	}

	private String filter(Filter filter) {
		if (filter.values().isEmpty()) {
			return quote(filter.template());
		}
		List<String> items = new ArrayList<>();
		filter.values().forEach((key, value) -> {
			items.add(quote(key));
			items.add(expr(value));
		});
		return "expr(" + quote(filter.template()) + ", " + wrapCall("args", items) + ")";
	}

	private String symbol(Sym sym) {
		return switch (sym.type()) {
		case "DataType" -> {
			imports.add("io.milvus.v2.common.DataType");
			yield "DataType." + DATA_TYPES.getOrDefault(sym.name(), sym.name());
		}
		case "FunctionType" -> {
			imports.add("io.milvus.common.clientenum.FunctionType");
			yield "FunctionType." + sym.name();
		}
		case "ConsistencyLevel" -> {
			imports.add("io.milvus.v2.common.ConsistencyLevel");
			yield "ConsistencyLevel." + sym.name().toUpperCase(Locale.ROOT);
		}
		default -> throw Unsupported.of(sym.type() + " constants in Java");
		};
	}

	private String call(Call call) {
		return switch (call.target()) {
		case RUNTIME -> wrapCall(camel(call.op()), CLIENTLESS.contains(call.op()) ? List.of(args(call.args()))
				: List.of("client", args(call.args())));
		case SELF -> ident(call.op()) + "()";
		case CLIENT -> clientCall(call.op(), call.args());
		};
	}

	private String clientCall(String op, Entries args) {
		if (op.equals("use_database")) {
			return "useDatabase(client, " + text(args.get("db_name")) + ")";
		}
		String request = request(op);
		List<String> calls = new ArrayList<>();
		switch (op) {
		case "create_collection" -> createCollection(args, calls);
		case "flush" -> calls.add(".collectionNames(List.of(" + text(args.get("collection_name")) + "))");
		case "create_index" -> {
			args.entries().forEach((key, value) -> calls.add(setter(key, value)));
			calls.add(".sync(false)");
		}
		case "add_collection_field" -> {
			calls.add(".collectionName(" + text(args.get("collection_name")) + ")");
			fieldSetters(((Make) args.get("field")).args(), false, calls);
		}
		case "search" -> search(args, calls);
		case "hybrid_search" -> hybridSearch(args, calls);
		default -> args.entries().forEach((key, value) -> calls.add(setter(key, value)));
		}
		return "client." + camel(op) + "(" + builder(request, calls) + ")";
	}

	private String request(String op) {
		String type = REQUESTS.get(op);
		if (type == null) {
			throw Unsupported.of(op + " in Java");
		}
		imports.add("io.milvus.v2.service." + type);
		return type.substring(type.lastIndexOf('.') + 1);
	}

	private String setter(String key, Expr value) {
		String name = SETTERS.get(key);
		if (name == null) {
			throw Unsupported.of("the Milvus argument " + key + " in Java");
		}
		return "." + name + "(" + typed(key, value) + ")";
	}

	/** A value in a builder argument, converted to the SDK's parameter type. */
	private String typed(String key, Expr value) {
		return switch (key) {
		case "properties" -> "strings(" + expr(value) + ")";
		case "consistency_level" -> symbol((Sym) value);
		case "data" -> vectors((Items) value);
		case "index_params" -> list(((Items) value).items().stream().map(i -> index(((Make) i).args())).toList());
		case "function" -> function(((Make) value).args());
		case "ranker" -> ranker(((Make) value).args());
		case "limit", "group_size", "num_shards", "num_partitions", "offset" -> integer(value);
		default -> expr(value);
		};
	}

	private void createCollection(Entries args, List<String> calls) {
		imports.add("io.milvus.v2.service.collection.request.AddFieldReq");
		Entries schema = ((Make) args.get("schema")).args();
		boolean autoId = schema.has("auto_id");
		calls.add(".collectionName(" + text(args.get("collection_name")) + ")");
		for (String key : List.of("description", "enable_dynamic_field")) {
			if (schema.has(key)) {
				calls.add(setter(key, schema.get(key)));
			}
		}
		List<String> schemaCalls = new ArrayList<>();
		for (Expr field : ((Items) schema.get("fields")).items()) {
			List<String> fieldCalls = new ArrayList<>();
			fieldSetters(((Make) field).args(), autoId, fieldCalls);
			schemaCalls.add(".addField(" + builder("AddFieldReq", fieldCalls) + ")");
		}
		if (schema.has("functions")) {
			for (Expr function : ((Items) schema.get("functions")).items()) {
				schemaCalls.add(".addFunction(" + function(((Make) function).args()) + ")");
			}
		}
		calls.add(".collectionSchema(client.createSchema()\n" + indent(String.join("\n", schemaCalls), 2) + ")");
		args.entries().forEach((key, value) -> {
			if (!List.of("collection_name", "schema").contains(key)) {
				calls.add(setter(key, value));
			}
		});
	}

	private void fieldSetters(Entries field, boolean autoId, List<String> calls) {
		field.entries().forEach((key, value) -> {
			String setter = FIELD_SETTERS.get(key);
			if (setter == null) {
				throw Unsupported.of("the field setting " + key + " in Java");
			}
			String argument = switch (key) {
			case "max_length", "dim", "max_capacity" -> integer(value);
			default -> expr(value);
			};
			calls.add("." + setter + "(" + argument + ")");
			if (key.equals("is_primary") && autoId) {
				calls.add(".autoID(true)");
			}
		});
	}

	private String function(Entries args) {
		imports.add("io.milvus.v2.service.collection.request.CreateCollectionReq");
		List<String> calls = new ArrayList<>();
		args.entries().forEach((key, value) -> calls.add(switch (key) {
		case "name" -> ".name(" + expr(value) + ")";
		case "function_type" -> ".functionType(" + expr(value) + ")";
		case "input_field_names" -> ".inputFieldNames(" + expr(value) + ")";
		case "output_field_names" -> ".outputFieldNames(" + expr(value) + ")";
		case "params" -> ".params(strings(" + expr(value) + "))";
		default -> throw Unsupported.of("the function setting " + key + " in Java");
		}));
		return builder("CreateCollectionReq.Function", calls);
	}

	private String index(Entries args) {
		imports.add("io.milvus.v2.common.IndexParam");
		List<String> calls = new ArrayList<>();
		args.entries().forEach((key, value) -> calls.add(switch (key) {
		case "field_name" -> ".fieldName(" + expr(value) + ")";
		case "index_name" -> ".indexName(" + expr(value) + ")";
		case "index_type" -> ".indexType(IndexParam.IndexType." + ((Text) value).value() + ")";
		case "metric_type" -> ".metricType(" + metric(value) + ")";
		case "params" -> ".extraParams(" + expr(value) + ")";
		default -> throw Unsupported.of("the index setting " + key + " in Java");
		}));
		return builder("IndexParam", calls);
	}

	private String metric(Expr value) {
		imports.add("io.milvus.v2.common.IndexParam");
		return "IndexParam.MetricType." + ((Text) value).value();
	}

	private void search(Entries args, List<String> calls) {
		args.entries().forEach((key, value) -> {
			switch (key) {
			case "search_params" -> ((Entries) value).entries().forEach((name, param) -> calls.add(switch (name) {
			case "metric_type" -> ".metricType(" + metric(param) + ")";
			case "offset" -> ".offset(" + integer(param) + ")";
			case "params" -> ".searchParams(" + expr(param) + ")";
			default -> throw Unsupported.of("the search parameter " + name + " in Java");
			}));
			case "timeout" -> throw Unsupported.of("per-request timeouts in Java; set timeout on the connection");
			case "extensions" -> throw Unsupported.of("vendor extension arguments on searches in Java");
			default -> calls.add(setter(key, value));
			}
		});
	}

	private void hybridSearch(Entries args, List<String> calls) {
		imports.add("io.milvus.v2.service.vector.request.AnnSearchReq");
		args.entries().forEach((key, value) -> {
			switch (key) {
			case "reqs" -> calls.add(".searchRequests(" + list(((Items) value).items().stream()
					.map(leg -> annRequest(((Make) leg).args())).toList()) + ")");
			case "output_fields" -> calls.add(".outFields(" + expr(value) + ")");
			case "extensions" -> throw Unsupported.of("vendor extension arguments on searches in Java");
			default -> calls.add(setter(key, value));
			}
		});
	}

	private String annRequest(Entries args) {
		List<String> calls = new ArrayList<>();
		args.entries().forEach((key, value) -> {
			switch (key) {
			case "data" -> calls.add(".vectors(" + vectors((Items) value) + ")");
			case "anns_field" -> calls.add(".vectorFieldName(" + expr(value) + ")");
			case "limit" -> calls.add(".limit(" + integer(value) + ")");
			case "expr" -> calls.add(".filter(" + expr(value) + ")");
			case "param" -> ((Entries) value).entries().forEach((name, param) -> calls.add(switch (name) {
			case "metric_type" -> ".metricType(" + metric(param) + ")";
			case "params" -> ".params(json(" + expr(param) + "))";
			default -> throw Unsupported.of("the search parameter " + name + " in Java");
			}));
			default -> throw Unsupported.of("the hybrid leg setting " + key + " in Java");
			}
		});
		return builder("AnnSearchReq", calls);
	}

	private String vectors(Items items) {
		return list(items.items().stream().map(item -> vector(((Make) item).args())).toList());
	}

	private String vector(Entries args) {
		String kind = ((Text) args.get("kind")).value();
		Expr value = args.get("value");
		return switch (kind) {
		case "dense" -> {
			imports.add("io.milvus.v2.service.vector.request.data.FloatVec");
			yield value instanceof Items items ? "new FloatVec(new float[] {" + items.items().stream()
					.map(v -> floatLiteral(((Num) v).value())).collect(Collectors.joining(", ")) + "})"
					: "new FloatVec(floats(" + expr(value) + "))";
		}
		case "sparse" -> {
			imports.add("io.milvus.v2.service.vector.request.data.SparseFloatVec");
			yield value instanceof Sparse sparse ? "new SparseFloatVec(" + sparse(sparse) + ")"
					: "new SparseFloatVec(sparseMap(" + expr(value) + "))";
		}
		case "text" -> {
			imports.add("io.milvus.v2.service.vector.request.data.EmbeddedText");
			yield "new EmbeddedText(" + text(value) + ")";
		}
		default -> throw Unsupported.of(kind + " query vectors in Java");
		};
	}

	private String ranker(Entries args) {
		String strategy = ((Text) args.get("strategy")).value();
		List<String> calls = new ArrayList<>();
		String type;
		switch (strategy) {
		case "rrf" -> {
			type = "RRFRanker";
			if (args.has("k")) {
				calls.add(".k(" + integer(args.get("k")) + ")");
			}
		}
		case "weighted" -> {
			if (args.has("norm_score")) {
				throw Unsupported.of("relative-score fusion in Java");
			}
			type = "WeightedRanker";
			calls.add(".weights(List.of(" + ((Items) args.get("weights")).items().stream()
					.map(w -> floatLiteral(((Num) w).value())).collect(Collectors.joining(", ")) + "))");
		}
		case "decay" -> {
			type = "DecayRanker";
			calls.add(".name(" + expr(args.get("name")) + ")");
			calls.add(".inputFieldNames(List.of(" + expr(args.get("field")) + "))");
			calls.add(".function(" + expr(args.get("function")) + ")");
			for (String setting : List.of("origin", "scale", "offset", "decay")) {
				if (args.has(setting)) {
					calls.add("." + setting + "(" + expr(args.get(setting)) + ")");
				}
			}
		}
		default -> throw Unsupported.of(strategy + " ranking in Java");
		}
		imports.add("io.milvus.v2.service.vector.request.ranker." + type);
		return builder(type, calls);
	}

	private String integer(Expr value) {
		return value instanceof Num num ? number(num.value()) : "(int) " + expr(value);
	}

	private String text(Expr value) {
		return value instanceof Text t ? quote(t.value()) : "text(" + expr(value) + ")";
	}

	private static String floatLiteral(double value) {
		return number(value) + "f";
	}

	private String run(Program program, String className) {
		List<String> body = new ArrayList<>();
		List<String> defaults = new ArrayList<>();
		for (Program.Parameter parameter : program.parameters()) {
			if (parameter.defaultValue() != null) {
				defaults.add(quote(parameter.name()));
				defaults.add(expr(parameter.defaultValue()));
			}
		}
		body.add("Map<String, Object> values = parameters(parameters, " + wrapCall("args", defaults) + ", "
				+ list(program.parameters().stream().map(p -> quote(p.name())).toList()) + ");");
		body.add("var program = new " + className + "(client);");
		body.add("Map<String, Object> results = new LinkedHashMap<>();");
		for (Program.Method method : program.methods()) {
			if (method.helper()) {
				continue;
			}
			String arguments = method.parameters().stream().map(p -> {
				String reader = PARAMETER_READERS.getOrDefault(parameterTypes.getOrDefault(p, "json"), "");
				String read = "values.get(" + quote(p) + ")";
				return reader.isEmpty() ? read : reader + "(" + read + ")";
			}).collect(Collectors.joining(", "));
			String invocation = "program." + ident(method.name()) + "(" + arguments + ")";
			boolean returns;
			try {
				returns = !droppedByTheSdk(method.body()) && !returnType(method.body()).equals("void");
			} catch (Unsupported e) {
				returns = false;
			}
			body.add(returns ? "results.put(" + quote(camel(method.name())) + ", " + invocation + ");"
					: invocation + ";\nresults.put(" + quote(camel(method.name())) + ", null);");
		}
		body.add("return results;");
		return "public static Map<String, Object> run(MilvusClientV2 client, Map<String, Object> parameters) {\n"
				+ indent(String.join("\n", body), 1) + "\n}";
	}

	private static String main(String className) {
		return String.join("\n", "public static void main(String[] args) {", "    MilvusClientV2 client = connect();",
				"    try {", "        System.out.println(toJson(run(client, parseArguments(args))));", "    } finally {",
				"        client.close();", "    }", "}");
	}

	private static String builder(String type, List<String> calls) {
		String flat = type + ".builder()" + String.join("", calls) + ".build()";
		if (flat.length() <= 100 && !flat.contains("\n")) {
			return flat;
		}
		return type + ".builder()\n" + indent(String.join("\n", calls) + "\n.build()", 2);
	}

	private static String list(List<String> items) {
		return wrapCall("List.of", items);
	}

	private static String wrapCall(String function, List<String> items) {
		String flat = function + "(" + String.join(", ", items) + ")";
		if (flat.length() <= 100 && !flat.contains("\n")) {
			return flat;
		}
		if (function.equals("args")) {
			List<String> pairs = new ArrayList<>();
			for (int i = 0; i < items.size(); i += 2) {
				pairs.add(items.get(i) + ", " + items.get(i + 1));
			}
			items = pairs;
		}
		return function + "(\n" + indent(String.join(",\n", items), 2) + ")";
	}

	static String ident(String name) {
		String camel = camel(name);
		return KEYWORDS.contains(camel) ? camel + "_" : camel;
	}
}
