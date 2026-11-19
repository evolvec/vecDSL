package org.ftn.vecDSL.coverage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reader for the JSON the coverage index script writes: objects, arrays, strings, numbers, booleans and null. */
final class Json {

	private final String text;
	private int position;

	private Json(String text) {
		this.text = text;
	}

	static Object parse(String text) {
		Json json = new Json(text);
		Object value = json.value();
		json.skipWhitespace();
		if (json.position != text.length()) {
			throw json.error("trailing characters");
		}
		return value;
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
			char escape = text.charAt(position++);
			switch (escape) {
			case 'n' -> result.append('\n');
			case 't' -> result.append('\t');
			case 'r' -> result.append('\r');
			case 'b' -> result.append('\b');
			case 'f' -> result.append('\f');
			case 'u' -> {
				result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
				position += 4;
			}
			default -> result.append(escape);
			}
		}
		throw error("unterminated string");
	}

	private Object number() {
		int start = position;
		while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
			position++;
		}
		String literal = text.substring(start, position);
		if (literal.isEmpty()) {
			throw error("unexpected character '" + text.charAt(start) + "'");
		}
		return literal.matches("-?\\d+") ? (Object) Long.valueOf(literal) : (Object) Double.valueOf(literal);
	}

	private Object literal(String word, Object value) {
		if (!text.startsWith(word, position)) {
			throw error("expected " + word);
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
