package org.ftn.vecDSL.generator.code;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Host-language-neutral code model. A target mapping (vendor knowledge) produces it once per statement;
 * a printer per host language (SDK knowledge) renders it. Operation and argument names follow the
 * vendor's canonical API vocabulary; printers translate them to each SDK.
 */
public final class Code {

	private Code() {
	}

	public sealed interface Expr permits Text, Num, Flag, Nil, Items, Entries, Sparse, Param, Env, Ref, Sym, Filter,
			Call, Make {
	}

	public record Text(String value) implements Expr {
	}

	public record Num(double value) implements Expr {
		public boolean integral() {
			return value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15;
		}
	}

	public record Flag(boolean value) implements Expr {
	}

	public record Nil() implements Expr {
	}

	public record Items(List<Expr> items) implements Expr {
	}

	/** Ordered name-value pairs: keyword arguments, request objects and dictionaries. */
	public record Entries(Map<String, Expr> entries) implements Expr {
		public Expr get(String name) {
			return entries.get(name);
		}

		public boolean has(String name) {
			return entries.containsKey(name);
		}

		public void put(String name, Expr value) {
			entries.put(name, value);
		}
	}

	/** Sparse vector literal: dimension index to weight. */
	public record Sparse(Map<Long, Double> weights) implements Expr {
	}

	/** Program parameter, by its vecDSL name. */
	public record Param(String name) implements Expr {
	}

	public record Env(String variable) implements Expr {
	}

	/** Local variable of the generated method. */
	public record Ref(String name) implements Expr {
	}

	/** Vendor enumeration constant, for example DataType.INT64 or ConsistencyLevel.Strong. */
	public record Sym(String type, String name) implements Expr {
	}

	/** Vendor filter expression; each {name} placeholder is filled with an escaped literal at run time. */
	public record Filter(String template, Map<String, Expr> values) implements Expr {
	}

	/** Receiver of a call: the vendor client, the generated runtime support module, or the program itself. */
	public enum Target {
		CLIENT, RUNTIME, SELF
	}

	public record Call(Target target, String op, Entries args) implements Expr {
	}

	/** Vendor value object: Schema, Field, Function, Index, AnnRequest or Ranker. */
	public record Make(String type, Entries args) implements Expr {
	}

	public sealed interface Stmt permits Let, Run, Return, When, Fail {
	}

	public record Let(String name, Expr value) implements Stmt {
	}

	public record Run(Expr value) implements Stmt {
	}

	public record Return(Expr value) implements Stmt {
	}

	public record When(Expr condition, boolean negated, List<Stmt> body) implements Stmt {
	}

	/** Statement the target cannot execute; the generated method raises the target's unsupported error. */
	public record Fail(String feature) implements Stmt {
	}

	/** Calls the visitor for every expression in the statements, depth first. */
	public static void visit(List<Stmt> statements, Consumer<Expr> visitor) {
		for (Stmt statement : statements) {
			switch (statement) {
			case Let let -> visit(let.value(), visitor);
			case Run run -> visit(run.value(), visitor);
			case Return ret -> visit(ret.value(), visitor);
			case When when -> {
				visit(when.condition(), visitor);
				visit(when.body(), visitor);
			}
			case Fail fail -> {
			}
			}
		}
	}

	public static void visit(Expr expr, Consumer<Expr> visitor) {
		visitor.accept(expr);
		switch (expr) {
		case Items items -> items.items().forEach(item -> visit(item, visitor));
		case Entries entries -> entries.entries().values().forEach(value -> visit(value, visitor));
		case Filter filter -> filter.values().values().forEach(value -> visit(value, visitor));
		case Call call -> visit(call.args(), visitor);
		case Make make -> visit(make.args(), visitor);
		default -> {
		}
		}
	}

	public static Text text(String value) {
		return new Text(value);
	}

	public static Num num(double value) {
		return new Num(value);
	}

	public static Flag flag(boolean value) {
		return new Flag(value);
	}

	public static Items items(List<? extends Expr> items) {
		return new Items(List.copyOf(items));
	}

	public static Items texts(List<String> values) {
		return new Items(values.stream().<Expr>map(Text::new).toList());
	}

	public static Entries entries() {
		return new Entries(new LinkedHashMap<>());
	}

	public static Entries entries(Map<String, ? extends Expr> values) {
		return new Entries(new LinkedHashMap<>(values));
	}

	public static Call client(String op, Entries args) {
		return new Call(Target.CLIENT, op, args);
	}

	public static Call runtime(String op, Entries args) {
		return new Call(Target.RUNTIME, op, args);
	}

	public static Call self(String op) {
		return new Call(Target.SELF, op, entries());
	}

	public static Make make(String type, Entries args) {
		return new Make(type, args);
	}

	public static Filter filter(String template) {
		return new Filter(template, Collections.emptyMap());
	}
}
