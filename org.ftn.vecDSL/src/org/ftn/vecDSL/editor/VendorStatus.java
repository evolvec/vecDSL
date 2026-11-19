package org.ftn.vecDSL.editor;

import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageStatus;

/** The coverage of one construct for one targeted vendor; the entry is null when the vendor has no coverage data. */
public record VendorStatus(String vendor, CoverageEntry entry) {

	public boolean hasData() {
		return entry != null;
	}

	public boolean isUnsupported() {
		return entry != null && entry.status() == CoverageStatus.UNSUPPORTED;
	}

	/** "native", "concealed", "unsupported", "not-applicable", the generation pending text or "no coverage data". */
	public String label() {
		if (entry == null) {
			return "no coverage data";
		}
		if (entry.isGenerationPending()) {
			return "supported by " + vendor + ", not generated yet";
		}
		return entry.status().literal();
	}

	/** One line for a proposal or a hover: "milvus: native" or "unsupported on milvus: reason". */
	public String describe() {
		if (isUnsupported()) {
			return "unsupported on " + vendor + ": " + entry.reason();
		}
		return vendor + ": " + label();
	}
}
