package org.ftn.vecDSL.generator.opensearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Supported OpenSearch server, client and Docker image versions, read from opensearch.properties. */
public final class OpenSearchManifest {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-z0-9.]+)\\}\\}");

	private final Properties properties = new Properties();

	private OpenSearchManifest(Properties values) {
		properties.putAll(values);
	}

	public static OpenSearchManifest load() {
		return new OpenSearchManifest(load("opensearch.properties"));
	}

	public String get(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalStateException("opensearch.properties has no entry " + key);
		}
		return value;
	}

	public String product() {
		return get("product") + " " + get("server.version");
	}

	public String pythonRequirement() {
		return get("sdk.python.package") + ">=" + get("sdk.python.min") + ",<" + get("sdk.python.max");
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
		InputStream in = OpenSearchManifest.class.getResourceAsStream(name);
		if (in == null) {
			throw new IOException("missing generator resource " + name);
		}
		return in;
	}
}
