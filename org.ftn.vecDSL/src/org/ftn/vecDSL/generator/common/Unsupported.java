package org.ftn.vecDSL.generator.common;

/** Signals a vecDSL construct that has no mapping for the current target. */
public class Unsupported extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public Unsupported(String feature) {
		super(feature);
	}

	public static Unsupported of(String feature) {
		return new Unsupported(feature);
	}
}
