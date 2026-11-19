package org.ftn.vecDSL.generator.duckdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Supported DuckDB, extension and client versions, read from duckdb.properties. */
public final class DuckdbManifest {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-z0-9.]+)\\}\\}");

	private final Properties properties = new Properties();

	private DuckdbManifest(Properties values) {
		properties.putAll(values);
	}

	public static DuckdbManifest load() {
		return new DuckdbManifest(load("duckdb.properties"));
	}

	public String get(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalStateException("duckdb.properties has no entry " + key);
		}
		return value;
	}

	public String product() {
		return get("product") + " " + get("duckdb.version");
	}

	public String pythonRequirement() {
		return get("sdk.python.package") + ">=" + get("sdk.python.min") + ",<" + get("sdk.python.max");
	}

	/** The pip requirements: the client and the packaged vss and fts extensions of the same DuckDB version. */
	public String pythonRequirements() {
		return pythonRequirement() + "\n" + String.join("\n", get("sdk.python.extensions").split(" ")) + "\n";
	}

	public String render(String resourceName) {
		return render(resourceName, Map.of());
	}

	/** The resource with each {{key}} replaced by the given value or, failing that, the manifest entry. */
	public String render(String resourceName, Map<String, String> values) {
		Matcher matcher = PLACEHOLDER.matcher(resource(resourceName));
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String key = matcher.group(1);
			matcher.appendReplacement(out, Matcher.quoteReplacement(values.containsKey(key) ? values.get(key) : get(key)));
		}
		return matcher.appendTail(out).toString();
	}

	private static Properties load(String name) {
		Properties result = new Properties();
		try (InputStream in = open(name)) {
			result.load(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return result;
	}

	private static String resource(String name) {
		try (InputStream in = open(name)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static InputStream open(String name) throws IOException {
		InputStream in = DuckdbManifest.class.getResourceAsStream(name);
		if (in == null) {
			throw new IOException("missing generator resource " + name);
		}
		return in;
	}
}
