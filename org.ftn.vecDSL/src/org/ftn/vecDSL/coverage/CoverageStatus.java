package org.ftn.vecDSL.coverage;

/** Status of a construct for one vendor, as COVERAGE.md states it. */
public enum CoverageStatus {

	NATIVE("native"), CONCEALED("concealed"), UNSUPPORTED("unsupported"), NOT_APPLICABLE("not-applicable");

	private final String literal;

	CoverageStatus(String literal) {
		this.literal = literal;
	}

	public String literal() {
		return literal;
	}

	public static CoverageStatus of(String literal) {
		for (CoverageStatus status : values()) {
			if (status.literal.equals(literal)) {
				return status;
			}
		}
		throw new IllegalArgumentException("unknown coverage status '" + literal + "'");
	}
}
