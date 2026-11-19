package org.ftn.vecDSL.coverage;

/** How much of a construct the vendor supports but the generator does not emit yet ("generation pending"). */
public enum Pending {

	NONE, PART, CONSTRUCT;

	public static Pending of(String literal) {
		return valueOf(literal.toUpperCase(java.util.Locale.ROOT));
	}
}
