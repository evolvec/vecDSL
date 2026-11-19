package org.ftn.vecDSL.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The words of one command after its name: flags, options with a value, and operands. An option is written
 * {@code --name value} or {@code --name=value}; a short alias such as {@code -o} stands for its long form; {@code --}
 * ends the options. Unknown options, a missing value and a repeated option are usage errors.
 */
public record Args(Set<String> flags, Map<String, String> options, List<String> operands) {

	/** The flags and valued options a command accepts, and the short aliases of some of them. */
	public record Spec(Set<String> flags, Set<String> valued, Map<String, String> aliases) {

		public static Spec of(Set<String> flags, Set<String> valued) {
			return new Spec(flags, valued, Map.of());
		}

		public Spec alias(String shortName, String longName) {
			Map<String, String> all = new LinkedHashMap<>(aliases);
			all.put(shortName, longName);
			return new Spec(flags, valued, Map.copyOf(all));
		}
	}

	public static Args parse(List<String> words, Spec spec) throws UsageException {
		Set<String> flags = new LinkedHashSet<>();
		Map<String, String> options = new LinkedHashMap<>();
		List<String> operands = new ArrayList<>();
		boolean onlyOperands = false;
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			if (onlyOperands || word.equals("-") || !word.startsWith("-")) {
				operands.add(word);
				continue;
			}
			if (word.equals("--")) {
				onlyOperands = true;
				continue;
			}
			int equals = word.indexOf('=');
			String name = spec.aliases().getOrDefault(equals < 0 ? word : word.substring(0, equals),
					equals < 0 ? word : word.substring(0, equals));
			if (spec.flags().contains(name)) {
				if (equals >= 0) {
					throw new UsageException(name + " takes no value");
				}
				flags.add(name);
			} else if (spec.valued().contains(name)) {
				String value;
				if (equals >= 0) {
					value = word.substring(equals + 1);
				} else if (i + 1 < words.size()) {
					value = words.get(++i);
				} else {
					throw new UsageException(name + " needs a value");
				}
				if (value.isEmpty()) {
					throw new UsageException(name + " needs a value");
				}
				if (options.putIfAbsent(name, value) != null) {
					throw new UsageException(name + " is given twice");
				}
			} else {
				throw new UsageException("unknown option " + word);
			}
		}
		return new Args(Set.copyOf(flags), Map.copyOf(options), List.copyOf(operands));
	}

	public boolean flag(String name) {
		return flags.contains(name);
	}

	public String option(String name) {
		return options.get(name);
	}

	/** The operands, checked against the number the command takes. */
	public List<String> operands(int min, int max, String what) throws UsageException {
		if (operands.size() < min) {
			throw new UsageException("missing " + what);
		}
		if (operands.size() > max) {
			throw new UsageException("too many operands: " + String.join(" ", operands.subList(max, operands.size())));
		}
		return operands;
	}

	/** A positive integer option, or the default when the option is absent. */
	public int positive(String name, int defaultValue) throws UsageException {
		String value = option(name);
		if (value == null) {
			return defaultValue;
		}
		int number;
		try {
			number = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			number = 0;
		}
		if (number <= 0) {
			throw new UsageException(name + " needs a positive integer, not '" + value + "'");
		}
		return number;
	}

	/** A comma-separated option split into its trimmed, non-empty items; empty when the option is absent. */
	public List<String> list(String name) {
		String value = option(name);
		if (value == null) {
			return List.of();
		}
		List<String> items = new ArrayList<>();
		for (String item : value.split(",")) {
			if (!item.isBlank()) {
				items.add(item.trim());
			}
		}
		return items;
	}
}
