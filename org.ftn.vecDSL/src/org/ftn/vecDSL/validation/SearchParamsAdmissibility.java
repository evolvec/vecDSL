package org.ftn.vecDSL.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.SearchParams;

/** Which SearchParams keys each query form admits, read from search-params-admissibility.properties. */
public final class SearchParamsAdmissibility {

	/** Surface keyword of each SearchParams key and the feature that records it. */
	public static final Map<String, String> KEYS = keys("target-recall", "targetRecall", "candidates", "candidatePool",
			"probes", "probes", "scan-budget", "scanBudget", "timeout", "timeoutMillis", "oversampling", "oversampling",
			"rescore", "rescore", "filter-strategy", "filterStrategy", "exhaustive", "exhaustive", "extension",
			"extensions");

	private static final Map<String, Set<String>> ADMITTED = load();

	private SearchParamsAdmissibility() {
	}

	public static Set<String> admitted(String form) {
		return ADMITTED.getOrDefault(form, Set.of());
	}

	public static Map<String, Set<String>> table() {
		return ADMITTED;
	}

	/** Keys written in the block, in declaration order. */
	public static List<String> written(SearchParams params) {
		return KEYS.entrySet().stream().filter(key -> isWritten(params, key.getValue())).map(Map.Entry::getKey)
				.toList();
	}

	private static boolean isWritten(EObject params, String feature) {
		if (feature.equals("extensions")) {
			return !((List<?>) params.eGet(params.eClass().getEStructuralFeature(feature))).isEmpty();
		}
		return ModelUtil.written(params, feature);
	}

	private static Map<String, String> keys(String... pairs) {
		Map<String, String> result = new LinkedHashMap<>();
		for (int i = 0; i < pairs.length; i += 2) {
			result.put(pairs[i], pairs[i + 1]);
		}
		return result;
	}

	private static Map<String, Set<String>> load() {
		Properties properties = new Properties();
		try (InputStream in = SearchParamsAdmissibility.class.getResourceAsStream("search-params-admissibility.properties")) {
			if (in == null) {
				throw new IOException("missing search-params-admissibility.properties");
			}
			properties.load(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		Map<String, Set<String>> table = new LinkedHashMap<>();
		for (String form : properties.stringPropertyNames()) {
			Set<String> keys = Arrays.stream(properties.getProperty(form).split(",")).map(String::trim)
					.collect(Collectors.toCollection(LinkedHashSet::new));
			Set<String> unknown = keys.stream().filter(key -> !KEYS.containsKey(key)).collect(Collectors.toSet());
			if (!unknown.isEmpty()) {
				throw new IllegalStateException("unknown SearchParams keys for " + form + ": " + unknown);
			}
			table.put(form, Set.copyOf(keys));
		}
		return Map.copyOf(table);
	}
}
