package org.ftn.vecDSL.generator.code;

import java.util.List;

import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.generator.code.Code.Stmt;

/** One vecDSL script mapped to a target: what every host-language printer renders. */
public record Program(String name, String version, String description, List<Parameter> parameters,
		Connection connection, List<Method> methods) {

	/** Program parameter; type is the vecDSL parameter type literal (vector, text, int, float, ...). */
	public record Parameter(String name, String type, Expr defaultValue) {
	}

	/** Default endpoint; every generated program lets an environment variable override the URI. */
	public record Connection(String uri, Expr token, String user, Expr password, String database, Double timeoutSeconds) {
	}

	/**
	 * One statement (or, when helper is set, a part of one that the statement's method calls): its program
	 * parameters, body, settings the target ignores, and features that need a full server rather than an
	 * embedded deployment.
	 */
	public record Method(String name, List<String> parameters, List<Stmt> body, List<String> notes,
			List<String> serverFeatures, boolean helper) {
	}
}
