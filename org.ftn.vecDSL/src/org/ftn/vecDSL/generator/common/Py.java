package org.ftn.vecDSL.generator.common;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;

/** Python source rendering: literals, identifiers, calls and blocks. */
public final class Py {

	private static final int LINE_WIDTH = 100;

	private static final Set<String> KEYWORDS = Set.of("False", "None", "True", "and", "as", "assert", "async",
			"await", "break", "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from",
			"global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try",
			"while", "with", "yield", "self", "rt");

	private Py() {
	}

	public static String str(String value) {
		StringBuilder out = new StringBuilder("\"");
		for (char c : value.toCharArray()) {
			switch (c) {
			case '\\' -> out.append("\\\\");
			case '"' -> out.append("\\\"");
			case '\n' -> out.append("\\n");
			case '\r' -> out.append("\\r");
			case '\t' -> out.append("\\t");
			default -> out.append(c);
			}
		}
		return out.append('"').toString();
	}

	public static String num(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	public static String bool(boolean value) {
		return value ? "True" : "False";
	}

	public static String ident(String name) {
		String snake = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("[^A-Za-z0-9_]", "_")
				.toLowerCase(Locale.ROOT);
		if (snake.isEmpty() || Character.isDigit(snake.charAt(0))) {
			snake = "_" + snake;
		}
		return KEYWORDS.contains(snake) ? snake + "_" : snake;
	}

	public static String className(String name) {
		return Arrays.stream(ident(name).split("_")).filter(part -> !part.isEmpty())
				.map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1)).collect(Collectors.joining());
	}

	public static String literal(Literal literal) {
		if (literal instanceof StringLit s) {
			return str(s.getValue());
		}
		if (literal instanceof NumberLit n) {
			return num(n.getValue());
		}
		if (literal instanceof BoolLit b) {
			return bool(b.isValue());
		}
		if (literal instanceof NullLit) {
			return "None";
		}
		if (literal instanceof ListLit l) {
			return list(l.getItems().stream().map(Py::literal).toList());
		}
		if (literal instanceof SparseLit s) {
			return s.getEntries().stream().map(e -> e.getIndex() + ": " + num(e.getWeight()))
					.collect(Collectors.joining(", ", "{", "}"));
		}
		if (literal instanceof ParamRef p) {
			return ident(p.getParam().getName());
		}
		if (literal instanceof EnvRef e) {
			return "rt.env(" + str(e.getVariable()) + ")";
		}
		throw new IllegalArgumentException("unknown literal " + literal);
	}

	public static String list(List<String> items) {
		return items.stream().collect(Collectors.joining(", ", "[", "]"));
	}

	public static String strings(List<String> items) {
		return list(items.stream().map(Py::str).toList());
	}

	public static String dict(Map<String, String> entries) {
		return entries.entrySet().stream().map(e -> str(e.getKey()) + ": " + e.getValue())
				.collect(Collectors.joining(", ", "{", "}"));
	}

	public static String call(String function, List<String> arguments) {
		String flat = function + "(" + String.join(", ", arguments) + ")";
		if (flat.length() <= LINE_WIDTH && !flat.contains("\n")) {
			return flat;
		}
		StringBuilder out = new StringBuilder(function).append("(\n");
		for (String argument : arguments) {
			out.append(indent(argument, 1)).append(",\n");
		}
		return out.append(")").toString();
	}

	public static String kw(String name, String value) {
		return name + "=" + value;
	}

	public static String indent(String code, int levels) {
		String prefix = "    ".repeat(levels);
		return code.lines().map(line -> line.isBlank() ? "" : prefix + line).collect(Collectors.joining("\n"));
	}

	public static String block(List<String> statements) {
		return statements.stream().filter(s -> s != null && !s.isBlank()).map(s -> s.replaceAll("^\\s*\\n", "").stripTrailing())
				.collect(Collectors.joining("\n"));
	}
}
