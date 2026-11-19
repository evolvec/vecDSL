package org.ftn.vecDSL.editor;

import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Field;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.VectorCollection;

/**
 * The HTML of the Eclipse editor hovers: {@link HoverContent} rendered by {@link HoverDocument#html()}. Every text
 * taken from the program, the grammar or a matrix is escaped.
 */
public final class HoverText {

	private HoverText() {
	}

	/** @see HoverContent#keyword(String, String, GrammarDocs, TargetStatus) */
	public static String keyword(String keyword, String rule, GrammarDocs docs, TargetStatus status) {
		return HoverContent.keyword(keyword, rule, docs, status).html();
	}

	public static String field(Field field) {
		return HoverContent.field(field).html();
	}

	public static String collection(VectorCollection collection) {
		return HoverContent.collection(collection).html();
	}

	public static String parameter(Parameter parameter) {
		return HoverContent.parameter(parameter).html();
	}

	public static String environment(EnvRef reference) {
		return HoverContent.environment(reference).html();
	}
}
