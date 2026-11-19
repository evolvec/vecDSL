package org.ftn.vecDSL.generator.pinecone;

import static org.ftn.vecDSL.generator.code.Syntax.camel;
import static org.ftn.vecDSL.generator.code.Syntax.number;
import static org.ftn.vecDSL.generator.code.Syntax.quote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ftn.vecDSL.generator.code.Code.Call;
import org.ftn.vecDSL.generator.code.Code.Entries;
import org.ftn.vecDSL.generator.code.Code.Env;
import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Fail;
import org.ftn.vecDSL.generator.code.Code.Flag;
import org.ftn.vecDSL.generator.code.Code.Items;
import org.ftn.vecDSL.generator.code.Code.Let;
import org.ftn.vecDSL.generator.code.Code.Nil;
import org.ftn.vecDSL.generator.code.Code.Num;
import org.ftn.vecDSL.generator.code.Code.Param;
import org.ftn.vecDSL.generator.code.Code.Ref;
import org.ftn.vecDSL.generator.code.Code.Return;
import org.ftn.vecDSL.generator.code.Code.Run;
import org.ftn.vecDSL.generator.code.Code.Stmt;
import org.ftn.vecDSL.generator.code.Code.Text;
import org.ftn.vecDSL.generator.code.Code.When;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.code.Syntax;

/** Java 17 with the Pinecone REST API 2026-07 through java.net.http and Gson, built with Maven. */
public final class PineconeJava implements PineconePrinter {

	static final String RUNTIME = "PineconeRuntime";

	private static final String MAP = "Map<String, Object>";
	private static final String ROWS = "List<Map<String, Object>>";
	private static final Map<String, String> RETURN_TYPES = Map.ofEntries(Map.entry("has_collection", "boolean"),
			Map.entry("insert_rows", MAP), Map.entry("upsert_rows", MAP), Map.entry("update_rows", MAP),
			Map.entry("delete_rows", MAP), Map.entry("get_rows", ROWS), Map.entry("scroll", ROWS),
			Map.entry("vector_search", ROWS), Map.entry("keyword_search", ROWS), Map.entry("hybrid_search", ROWS),
			Map.entry("count", "long"), Map.entry("recommend_vector", "List<Double>"), Map.entry("lookup_vector", "List<Double>"),
			Map.entry("has_namespace", "boolean"), Map.entry("has_backup_schedule", "boolean"), Map.entry("describe", MAP),
			Map.entry("await_index", "String"), Map.entry("list_objects", "List<String>"), Map.entry("sample_rows", ROWS),
			Map.entry("distance_matrix", ROWS), Map.entry("import_file", MAP), Map.entry("start_import", MAP),
			Map.entry("rename_field", MAP), Map.entry("create_backup", MAP), Map.entry("restore_backup", MAP),
			Map.entry("drop_backup", MAP), Map.entry("clone_collection", MAP));
	private static final Map<String, String> PARAMETER_TYPES = Map.of("vector", "List<Float>", "text", "String", "int",
			"long", "float", "double", "bool", "boolean", "string_list", "List<String>", "records", ROWS);
	private static final Map<String, String> PARAMETER_READERS = Map.of("vector", "floats", "text", "text", "int",
			"integer", "float", "decimal", "bool", "flag", "string_list", "texts", "records", "records");
	private static final Set<String> KEYWORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case",
			"catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "false",
			"final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
			"long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
			"strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "var",
			"void", "volatile", "while", "client", "values", "program", "results", "parameters");

	/** Method names of the program class; a runtime member of the same name is called qualified, as the method hides it. */
	private Set<String> members = Set.of();

	@Override
	public String language() {
		return "java";
	}

	@Override
	public Map<String, String> files(Program program, PineconeManifest manifest) {
		String javaPackage = "vecdsl." + program.name().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
		String className = Syntax.pascal(program.name());
		String folder = "src/main/java/" + javaPackage.replace('.', '/') + "/";
		Map<String, String> values = Map.of("java.package", javaPackage, "java.artifact",
				String.join("-", Syntax.words(program.name())), "java.main", javaPackage + "." + className);
		Map<String, String> files = new LinkedHashMap<>();
		files.put(folder + className + ".java", module(program, manifest, javaPackage, className));
		files.put(folder + RUNTIME + ".java", manifest.render("java/" + RUNTIME + ".java.template", values));
		files.put("pom.xml", manifest.render("java/pom.xml", values));
		return files;
	}

	String module(Program program, PineconeManifest manifest, String javaPackage, String className) {
		members = program.methods().stream().map(m -> camel(m.name())).collect(Collectors.toSet());
		List<String> members = new ArrayList<>();
		members.add("private final Client client;");
		members.add(String.join("\n", "public " + className + "(Client client) {", "    this.client = client;", "}"));
		members.add(connect(program));
		members.add("@Override\npublic void close() {\n    client.close();\n}");
		members.addAll(program.methods().stream().map(m -> method(m, program)).toList());
		members.add(run(program, className));
		members.add(String.join("\n", "public static void main(String[] args) {", "    try (Client client = connect()) {",
				"        System.out.println(toJson(run(client, parseArguments(args))));", "    }", "}"));
		return String.join("\n", "package " + javaPackage + ";", "", "import static " + javaPackage + "." + RUNTIME + ".*;", "",
				"import java.util.LinkedHashMap;",
				"import java.util.List;", "import java.util.Map;", "", header(program, manifest),
				"public final class " + className + " implements AutoCloseable {", "",
				Syntax.indent(String.join("\n\n", members), 1), "}") + "\n";
	}

	private String header(Program program, PineconeManifest manifest) {
		List<String> lines = new ArrayList<>();
		lines.add("Generated by vecDSL from script " + program.name()
				+ (program.version() == null ? "" : " version " + program.version()) + ".");
		if (program.description() != null) {
			lines.add(program.description());
		}
		lines.add("");
		lines.add("Target: " + manifest.product() + " through its REST API with java.net.http and Gson "
				+ manifest.get("gson.version") + ", Java >= " + manifest.get("java.release") + ". The Java SDK "
				+ manifest.get("sdk.java.group") + ":" + manifest.get("sdk.java.artifact") + " " + manifest.get("sdk.java.version")
				+ " targets API " + manifest.get("sdk.java.api.version") + " and has no Documents API.");
		lines.addAll(PineconePrinter.endpoints(program.connection()));
		return "/**\n" + lines.stream().map(line -> (" * " + line.replace("*/", "* /")).stripTrailing())
				.collect(Collectors.joining("\n")) + "\n */";
	}

	private String connect(Program program) {
		Program.Connection connection = program.connection();
		List<String> options = new ArrayList<>();
		options.add(quote("uri"));
		options.add("envOr(" + quote(URI_VARIABLE) + ", " + quote(connection.uri()) + ")");
		options.add(quote("api_key"));
		options.add(expr(PineconePrinter.apiKey(connection)));
		options.add(quote("index_host"));
		options.add("envOr(" + quote(INDEX_HOST_VARIABLE) + ", null)");
		if (connection.timeoutSeconds() != null) {
			options.add(quote("timeout"));
			options.add(number(connection.timeoutSeconds()));
		}
		return "public static Client connect() {\n    return " + RUNTIME + ".connect(" + call("args", options) + ");\n}";
	}

	private String method(Program.Method method, Program program) {
		List<String> parameters = method.parameters().stream()
				.map(name -> PARAMETER_TYPES.getOrDefault(type(program, name), "Object") + " " + ident(name)).toList();
		List<String> body = new ArrayList<>();
		method.body().forEach(statement -> body.add(statement(statement)));
		String signature = (method.helper() ? "private " : "public ") + returnType(method.body()) + " " + camel(method.name())
				+ "(" + String.join(", ", parameters) + ") {";
		String javadoc = method.notes().isEmpty() ? "" : "/** Ignored on Pinecone: " + String.join(", ", method.notes()) + ". */\n";
		return javadoc + signature + (body.isEmpty() ? "" : "\n" + Syntax.indent(String.join("\n", body), 1)) + "\n}";
	}

	private static String returnType(List<Stmt> body) {
		return returned(body).map(value -> {
			if (value instanceof Call call && RETURN_TYPES.containsKey(call.op())) {
				return RETURN_TYPES.get(call.op());
			}
			return value instanceof Items ? "List<Object>" : "Object";
		}).orElse("void");
	}

	private static Optional<Expr> returned(List<Stmt> body) {
		for (Stmt statement : body) {
			if (statement instanceof Return ret) {
				return Optional.of(ret.value());
			}
			if (statement instanceof When when) {
				Optional<Expr> nested = returned(when.body());
				if (nested.isPresent()) {
					return nested;
				}
			}
		}
		return Optional.empty();
	}

	private static String type(Program program, String parameter) {
		return program.parameters().stream().filter(p -> p.name().equals(parameter)).map(Program.Parameter::type)
				.findFirst().orElse("json");
	}

	private String statement(Stmt statement) {
		return switch (statement) {
		case Let let -> "var " + ident(let.name()) + " = " + expr(let.value()) + ";";
		case Run run -> expr(run.value()) + ";";
		case Return ret -> "return " + expr(ret.value()) + ";";
		case When when -> "if (" + (when.negated() ? "!" : "") + expr(when.condition()) + ") {\n"
				+ Syntax.indent(when.body().stream().map(this::statement).collect(Collectors.joining("\n")), 1) + "\n}";
		case Fail fail -> "throw " + runtime("unsupported") + "(" + quote(VENDOR) + ", " + quote(fail.feature()) + ");";
		};
	}

	String expr(Expr expr) {
		return switch (expr) {
		case Text text -> quote(text.value());
		case Num num -> numeral(num);
		case Flag flag -> flag.value() ? "true" : "false";
		case Nil nil -> "null";
		case Items items -> call(runtime("list"), items.items().stream().map(this::expr).toList());
		case Entries entries -> call(runtime("args"), entries.entries().entrySet().stream()
				.flatMap(e -> List.of(quote(e.getKey()), expr(e.getValue())).stream()).toList());
		case Param param -> ident(param.name());
		case Env env -> runtime("env") + "(" + quote(env.variable()) + ")";
		case Ref ref -> ident(ref.name());
		case Call call -> call(call);
		default -> throw new IllegalStateException("no Java form for " + expr);
		};
	}

	private static String numeral(Num num) {
		if (!num.integral()) {
			return Double.toString(num.value());
		}
		long value = (long) num.value();
		return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE ? Long.toString(value) : value + "L";
	}

	private String call(Call call) {
		return switch (call.target()) {
		case RUNTIME -> call(runtime(camel(call.op())), List.of("client", expr(call.args())));
		case SELF -> camel(call.op()) + "()";
		case CLIENT -> throw new IllegalStateException("Pinecone programs call the SDK through the runtime: " + call.op());
		};
	}

	/** A call whose arguments go one per line, in pairs for args(...), when the flat form is too long. */
	private static String call(String function, List<String> arguments) {
		String flat = function + "(" + String.join(", ", arguments) + ")";
		if (flat.length() <= Syntax.LINE_WIDTH && !flat.contains("\n")) {
			return flat;
		}
		List<String> lines = new ArrayList<>();
		int step = function.endsWith("args") ? 2 : 1;
		for (int i = 0; i < arguments.size(); i += step) {
			lines.add(String.join(", ", arguments.subList(i, Math.min(i + step, arguments.size()))));
		}
		return function + "(\n" + Syntax.indent(String.join(",\n", lines), 2) + ")";
	}

	private String run(Program program, String className) {
		List<String> defaults = new ArrayList<>();
		for (Program.Parameter parameter : program.parameters()) {
			if (parameter.defaultValue() != null) {
				defaults.add(quote(parameter.name()));
				defaults.add(expr(parameter.defaultValue()));
			}
		}
		String names = program.parameters().stream().map(p -> quote(p.name())).collect(Collectors.joining(", "));
		List<String> body = new ArrayList<>();
		body.add("Map<String, Object> values = parameters(parameters, " + call("args", defaults) + ", List.of(" + names + "));");
		body.add("var program = new " + className + "(client);");
		body.add("Map<String, Object> results = new LinkedHashMap<>();");
		for (Program.Method method : program.methods()) {
			if (method.helper()) {
				continue;
			}
			String invocation = "program." + camel(method.name()) + "(" + method.parameters().stream()
					.map(p -> read(program, p)).collect(Collectors.joining(", ")) + ")";
			if (returnType(method.body()).equals("void")) {
				body.add(invocation + ";");
				body.add("results.put(" + quote(camel(method.name())) + ", null);");
			} else {
				body.add("results.put(" + quote(camel(method.name())) + ", " + invocation + ");");
			}
		}
		body.add("return results;");
		return "public static Map<String, Object> run(Client client, Map<String, Object> parameters) {\n"
				+ Syntax.indent(String.join("\n", body), 1) + "\n}";
	}

	private static String read(Program program, String parameter) {
		String reader = PARAMETER_READERS.get(type(program, parameter));
		String value = "values.get(" + quote(parameter) + ")";
		return reader == null ? value : reader + "(" + value + ")";
	}

	private String runtime(String name) {
		return members.contains(name) ? RUNTIME + "." + name : name;
	}

	static String ident(String name) {
		String camel = camel(name);
		return KEYWORDS.contains(camel) ? camel + "_" : camel;
	}
}
