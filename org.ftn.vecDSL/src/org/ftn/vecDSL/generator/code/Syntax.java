package org.ftn.vecDSL.generator.code;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Lexical helpers shared by the host-language printers. */
public final class Syntax {

	public static final int LINE_WIDTH = 100;

	private Syntax() {
	}

	/** Double-quoted string literal valid in Python, TypeScript and Java. */
	public static String quote(String value) {
		StringBuilder out = new StringBuilder("\"");
		for (char c : value.toCharArray()) {
			switch (c) {
			case '\\' -> out.append("\\\\");
			case '"' -> out.append("\\\"");
			case '\n' -> out.append("\\n");
			case '\r' -> out.append("\\r");
			case '\t' -> out.append("\\t");
			default -> {
				if (c < 0x20) {
					out.append(String.format("\\u%04x", (int) c));
				} else {
					out.append(c);
				}
			}
			}
		}
		return out.append('"').toString();
	}

	public static String number(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	public static List<String> words(String name) {
		String spaced = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("[^A-Za-z0-9]+", "_");
		return Arrays.stream(spaced.split("_")).filter(part -> !part.isEmpty())
				.map(part -> part.toLowerCase(Locale.ROOT)).toList();
	}

	public static String snake(String name) {
		String result = String.join("_", words(name));
		return result.isEmpty() || Character.isDigit(result.charAt(0)) ? "_" + result : result;
	}

	public static String pascal(String name) {
		String result = words(name).stream().map(Syntax::capitalize).collect(Collectors.joining());
		return result.isEmpty() || Character.isDigit(result.charAt(0)) ? "_" + result : result;
	}

	public static String camel(String name) {
		String pascal = pascal(name);
		return pascal.startsWith("_") ? pascal : Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
	}

	private static String capitalize(String word) {
		return Character.toUpperCase(word.charAt(0)) + word.substring(1);
	}

	public static String indent(String code, int levels) {
		String prefix = "    ".repeat(levels);
		return code.lines().map(line -> line.isBlank() ? "" : prefix + line).collect(Collectors.joining("\n"));
	}

	/** Comma-separated items between delimiters, wrapped one item per line when the flat form is too long. */
	public static String wrap(String open, List<String> items, String close) {
		String flat = open + String.join(", ", items) + close;
		if (flat.length() <= LINE_WIDTH && !flat.contains("\n")) {
			return flat;
		}
		if (items.isEmpty()) {
			return open + close;
		}
		return open + "\n" + items.stream().map(item -> indent(item, 1) + ",").collect(Collectors.joining("\n")) + "\n"
				+ close;
	}
}
