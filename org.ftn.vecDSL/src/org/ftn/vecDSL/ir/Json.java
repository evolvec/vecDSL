package org.ftn.vecDSL.ir;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON subset of the IR: objects (insertion-ordered maps), arrays, strings, numbers, booleans and null. Parsed
 * numbers are {@link BigDecimal}s, so no digit of the text is lost; written numbers are canonical (see {@link #number}).
 * The writer is deterministic: two equal values give the same bytes.
 */
public final class Json {

	private final String text;
	private int position;

	private Json(String text) {
		this.text = text;
	}

	public static Object parse(String text) {
		Json json = new Json(text);
		Object value = json.value();
		json.skipWhitespace();
		if (json.position != text.length()) {
			throw json.error("trailing characters");
		}
		return value;
	}

	public static String write(Object value) {
		StringBuilder out = new StringBuilder();
		write(value, 0, out);
		return out.append('\n').toString();
	}

	/**
	 * Canonical text of a number: integral values below 10^15 without a fraction, other doubles in the shortest form
	 * that reads back to the same double.
	 */
	public static String number(Number value) {
		if (value instanceof BigDecimal decimal) {
			return decimal.toString();
		}
		if (value instanceof Integer || value instanceof Long) {
			return value.toString();
		}
		double d = value.doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			throw new IllegalArgumentException("not a JSON number: " + d);
		}
		if (d == Math.rint(d) && Math.abs(d) < 1e15) {
			return Long.toString((long) d);
		}
		return Double.toString(d);
	}

	private static void write(Object value, int indent, StringBuilder out) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof String string) {
			string(string, out);
		} else if (value instanceof Boolean flag) {
			out.append(flag.booleanValue());
		} else if (value instanceof Number number) {
			out.append(number(number));
		} else if (value instanceof Map<?, ?> map) {
			object(map, indent, out);
		} else if (value instanceof List<?> list) {
			array(list, indent, out);
		} else {
			throw new IllegalArgumentException("not a JSON value: " + value.getClass().getName());
		}
	}

	private static void object(Map<?, ?> map, int indent, StringBuilder out) {
		if (map.isEmpty()) {
			out.append("{}");
			return;
		}
		out.append("{\n");
		int i = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			pad(indent + 1, out);
			string((String) entry.getKey(), out);
			out.append(": ");
			write(entry.getValue(), indent + 1, out);
			out.append(++i < map.size() ? ",\n" : "\n");
		}
		pad(indent, out);
		out.append('}');
	}

	private static void array(List<?> list, int indent, StringBuilder out) {
		if (list.isEmpty()) {
			out.append("[]");
			return;
		}
		if (list.stream().noneMatch(item -> item instanceof Map || item instanceof List)) {
			out.append('[');
			for (int i = 0; i < list.size(); i++) {
				write(list.get(i), indent, out);
				out.append(i + 1 < list.size() ? ", " : "");
			}
			out.append(']');
			return;
		}
		out.append("[\n");
		for (int i = 0; i < list.size(); i++) {
			pad(indent + 1, out);
			write(list.get(i), indent + 1, out);
			out.append(i + 1 < list.size() ? ",\n" : "\n");
		}
		pad(indent, out);
		out.append(']');
	}

	private static void pad(int indent, StringBuilder out) {
		out.append("  ".repeat(indent));
	}

	private static void string(String value, StringBuilder out) {
		out.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"' -> out.append("\\\"");
			case '\\' -> out.append("\\\\");
			case '\n' -> out.append("\\n");
			case '\r' -> out.append("\\r");
			case '\t' -> out.append("\\t");
			case '\b' -> out.append("\\b");
			case '\f' -> out.append("\\f");
			default -> {
				if (c < 0x20) {
					out.append(String.format("\\u%04x", (int) c));
				} else {
					out.append(c);
				}
			}
			}
		}
		out.append('"');
	}

	private Object value() {
		skipWhitespace();
		if (position >= text.length()) {
			throw error("unexpected end");
		}
		char c = text.charAt(position);
		return switch (c) {
		case '{' -> object();
		case '[' -> array();
		case '"' -> string();
		case 't' -> literal("true", Boolean.TRUE);
		case 'f' -> literal("false", Boolean.FALSE);
		case 'n' -> literal("null", null);
		default -> number();
		};
	}

	private Map<String, Object> object() {
		Map<String, Object> result = new LinkedHashMap<>();
		position++;
		skipWhitespace();
		if (peek('}')) {
			return result;
		}
		do {
			skipWhitespace();
			String key = string();
			skipWhitespace();
			expect(':');
			if (result.containsKey(key)) {
				throw error("duplicate key \"" + key + "\"");
			}
			result.put(key, value());
			skipWhitespace();
		} while (peek(','));
		expect('}');
		return result;
	}

	private List<Object> array() {
		List<Object> result = new ArrayList<>();
		position++;
		skipWhitespace();
		if (peek(']')) {
			return result;
		}
		do {
			result.add(value());
			skipWhitespace();
		} while (peek(','));
		expect(']');
		return result;
	}

	private String string() {
		expect('"');
		StringBuilder result = new StringBuilder();
		while (position < text.length()) {
			char c = text.charAt(position++);
			if (c == '"') {
				return result.toString();
			}
			if (c != '\\') {
				result.append(c);
				continue;
			}
			if (position >= text.length()) {
				break;
			}
			char escape = text.charAt(position++);
			switch (escape) {
			case 'n' -> result.append('\n');
			case 't' -> result.append('\t');
			case 'r' -> result.append('\r');
			case 'b' -> result.append('\b');
			case 'f' -> result.append('\f');
			case '"', '\\', '/' -> result.append(escape);
			case 'u' -> {
				if (position + 4 > text.length()) {
					throw error("truncated unicode escape");
				}
				result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
				position += 4;
			}
			default -> throw error("invalid escape \\" + escape);
			}
		}
		throw error("unterminated string");
	}

	private BigDecimal number() {
		int start = position;
		while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
			position++;
		}
		String literal = text.substring(start, position);
		if (literal.isEmpty()) {
			throw error("unexpected character '" + text.charAt(start) + "'");
		}
		try {
			return new BigDecimal(literal);
		} catch (NumberFormatException e) {
			throw error("invalid number " + literal);
		}
	}

	private Object literal(String word, Object value) {
		if (!text.startsWith(word, position)) {
			throw error("unexpected token");
		}
		position += word.length();
		return value;
	}

	private boolean peek(char c) {
		if (position < text.length() && text.charAt(position) == c) {
			position++;
			return true;
		}
		return false;
	}

	private void expect(char c) {
		if (!peek(c)) {
			throw error("expected '" + c + "'");
		}
	}

	private void skipWhitespace() {
		while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
			position++;
		}
	}

	private IllegalArgumentException error(String message) {
		return new IllegalArgumentException("JSON: " + message + " at offset " + position);
	}
}
