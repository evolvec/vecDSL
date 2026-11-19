package org.ftn.vecDSL.coverage;

/** One row of a vendor's COVERAGE.md: the construct id, its status, the generated calls and the reference. */
public record CoverageEntry(String vendor, String construct, String eClass, String feature, CoverageStatus status,
		Pending pending, String reason, String python, String typescript, String java, String reference, String notes) {

	/** Whether the generator raises "no mapping yet" for the construct although the vendor supports it. */
	public boolean isGenerationPending() {
		return pending == Pending.CONSTRUCT;
	}

	/** Whether content assist should rank the construct last and mark it. */
	public boolean isUnsupported() {
		return status == CoverageStatus.UNSUPPORTED;
	}
}
