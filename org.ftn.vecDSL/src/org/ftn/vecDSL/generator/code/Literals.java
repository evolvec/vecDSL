package org.ftn.vecDSL.generator.code;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ftn.vecDSL.generator.code.Code.Expr;
import org.ftn.vecDSL.vecDSL.BoolLit;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.ListLit;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.NullLit;
import org.ftn.vecDSL.vecDSL.NumberLit;
import org.ftn.vecDSL.vecDSL.ParamRef;
import org.ftn.vecDSL.vecDSL.SparseLit;
import org.ftn.vecDSL.vecDSL.StringLit;

/** vecDSL literals as code-model values. */
public final class Literals {

	private Literals() {
	}

	public static Expr of(Literal literal) {
		if (literal instanceof StringLit s) {
			return new Code.Text(s.getValue());
		}
		if (literal instanceof NumberLit n) {
			return new Code.Num(n.getValue());
		}
		if (literal instanceof BoolLit b) {
			return new Code.Flag(b.isValue());
		}
		if (literal instanceof NullLit) {
			return new Code.Nil();
		}
		if (literal instanceof ListLit l) {
			return new Code.Items(l.getItems().stream().map(Literals::of).toList());
		}
		if (literal instanceof SparseLit s) {
			Map<Long, Double> weights = new LinkedHashMap<>();
			s.getEntries().forEach(e -> weights.put((long) e.getIndex(), e.getWeight()));
			return new Code.Sparse(weights);
		}
		if (literal instanceof ParamRef p) {
			return new Code.Param(p.getParam().getName());
		}
		if (literal instanceof EnvRef e) {
			return new Code.Env(e.getVariable());
		}
		throw new IllegalArgumentException("unknown literal " + literal);
	}
}
