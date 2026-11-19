package org.ftn.vecDSL.coverage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;

/**
 * Coverage of every construct for every vendor that has a COVERAGE.md, read from the files coverage_index.py writes
 * (index/&lt;vendor&gt;.json beside this class). A vendor without a file has no coverage data.
 */
public final class CoverageIndex {

	/** Folder of the index files, relative to this class. */
	static final String FOLDER = "index/";

	private final Map<String, Map<String, CoverageEntry>> vendors;

	private CoverageIndex(Map<String, Map<String, CoverageEntry>> vendors) {
		this.vendors = vendors;
	}

	/**
	 * Reads the index of each vendor from {@code source}, which returns the JSON of a vendor or null when the vendor
	 * has no coverage file.
	 */
	public static CoverageIndex load(Collection<String> vendorIds, Function<String, InputStream> source) {
		Map<String, Map<String, CoverageEntry>> vendors = new LinkedHashMap<>();
		for (String vendor : vendorIds) {
			try (InputStream in = source.apply(vendor)) {
				if (in != null) {
					vendors.put(vendor, read(vendor, new String(in.readAllBytes(), StandardCharsets.UTF_8)));
				}
			} catch (IOException e) {
				throw new UncheckedIOException("cannot read the coverage index of " + vendor, e);
			}
		}
		return new CoverageIndex(vendors);
	}

	/**
	 * The index of every vendor of {@link VecDSLOutputConfigurationProvider#VENDORS}, read as class resources of this
	 * bundle, so that every host (Eclipse, vecdsl-cli, the language server) reads the same files the same way.
	 */
	public static CoverageIndex fromClasspath() {
		return load(VecDSLOutputConfigurationProvider.VENDORS,
				vendor -> CoverageIndex.class.getResourceAsStream(FOLDER + vendor + ".json"));
	}

	public static CoverageIndex empty() {
		return new CoverageIndex(Map.of());
	}

	@SuppressWarnings("unchecked")
	static Map<String, CoverageEntry> read(String vendor, String json) {
		Map<String, Object> root = (Map<String, Object>) Json.parse(json);
		if (!vendor.equals(root.get("vendor"))) {
			throw new IllegalArgumentException("coverage index for " + root.get("vendor") + " read as " + vendor);
		}
		Map<String, CoverageEntry> entries = new LinkedHashMap<>();
		for (Object item : (List<Object>) root.get("constructs")) {
			Map<String, Object> row = (Map<String, Object>) item;
			Map<String, Object> calls = (Map<String, Object>) row.get("calls");
			CoverageEntry entry = new CoverageEntry(vendor, text(row, "construct"), text(row, "eClass"),
					(String) row.get("feature"), CoverageStatus.of(text(row, "status")), Pending.of(text(row, "pending")),
					text(row, "reason"), text(calls, "python"), text(calls, "typescript"), text(calls, "java"),
					text(row, "reference"), text(row, "notes"));
			entries.put(entry.construct(), entry);
		}
		return entries;
	}

	private static String text(Map<String, Object> row, String key) {
		Object value = row.get(key);
		if (!(value instanceof String string)) {
			throw new IllegalArgumentException("coverage row without '" + key + "': " + row);
		}
		return string;
	}

	public boolean hasData(String vendor) {
		return vendors.containsKey(vendor);
	}

	public Collection<String> vendors() {
		return vendors.keySet();
	}

	public Collection<CoverageEntry> entries(String vendor) {
		return vendors.getOrDefault(vendor, Map.of()).values();
	}

	public Optional<CoverageEntry> entry(String vendor, String construct) {
		return Optional.ofNullable(vendors.getOrDefault(vendor, Map.of()).get(construct));
	}

	/** The row of the feature when the matrix lists it, otherwise the row of the class. */
	public Optional<CoverageEntry> entry(String vendor, EClass type, EStructuralFeature feature) {
		if (feature != null) {
			Optional<CoverageEntry> row = entry(vendor, ConstructIds.of(type, feature));
			if (row.isPresent()) {
				return row;
			}
		}
		String classId = ConstructIds.of(type);
		return classId == null ? Optional.empty() : entry(vendor, classId);
	}
}
