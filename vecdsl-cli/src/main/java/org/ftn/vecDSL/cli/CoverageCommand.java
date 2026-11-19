package org.ftn.vecDSL.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.ftn.vecDSL.coverage.CoverageEntry;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.coverage.CoverageStatus;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.ir.Json;

/**
 * {@code vecdsl coverage}: queries the coverage index. Without a filter it prints the summary per vendor; a row is
 * counted as {@code pending} when the generator does not emit the construct yet (CoverageEntry.isGenerationPending,
 * the "+pending" rule of coverage_index.py), otherwise under its status. With {@code --target},
 * {@code --construct} or {@code --status} it lists the matching rows.
 */
final class CoverageCommand implements Command {

	static final String TARGET = "--target";
	static final String CONSTRUCT = "--construct";
	static final String STATUS = "--status";
	static final String JSON = "--json";

	static final String PENDING = "pending";
	static final List<String> COLUMNS = columns();

	private final Toolchain toolchain;
	private final PrintStream out;

	CoverageCommand(Toolchain toolchain, PrintStream out) {
		this.toolchain = toolchain;
		this.out = out;
	}

	private static List<String> columns() {
		List<String> columns = new ArrayList<>();
		for (CoverageStatus status : CoverageStatus.values()) {
			columns.add(status.literal());
		}
		columns.add(PENDING);
		return List.copyOf(columns);
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(JSON), Set.of(TARGET, CONSTRUCT, STATUS));
	}

	@Override
	public String usage() {
		return "vecdsl coverage [--target <vendor>] [--construct <id>] [--status " + String.join("|", COLUMNS)
				+ "] [--json]";
	}

	/** The cell of a row in the summary and in the matrices: pending, or the row's status. */
	static String cell(CoverageEntry entry) {
		return entry.isGenerationPending() ? PENDING : entry.status().literal();
	}

	@Override
	public int run(Args args) throws UsageException {
		args.operands(0, 0, "nothing");
		CoverageIndex index = toolchain.coverage();
		String target = args.option(TARGET);
		String construct = args.option(CONSTRUCT);
		String status = args.option(STATUS);
		if (target != null && !VecDSLOutputConfigurationProvider.VENDORS.contains(target)) {
			throw new UsageException("unknown target '" + target + "'; targets: "
					+ String.join(", ", VecDSLOutputConfigurationProvider.VENDORS));
		}
		if (status != null && !COLUMNS.contains(status)) {
			throw new UsageException("unknown status '" + status + "'; statuses: " + String.join(", ", COLUMNS));
		}
		if (target == null && construct == null && status == null) {
			return summary(index, args.flag(JSON));
		}
		List<CoverageEntry> rows = new ArrayList<>();
		for (String vendor : target == null ? index.vendors() : List.of(target)) {
			for (CoverageEntry entry : index.entries(vendor)) {
				if ((construct == null || entry.construct().equals(construct))
						&& (status == null || cell(entry).equals(status))) {
					rows.add(entry);
				}
			}
		}
		if (args.flag(JSON)) {
			out.print(Json.write(rows.stream().map(CoverageCommand::json).toList()));
		} else {
			for (CoverageEntry entry : rows) {
				out.println(entry.vendor() + "\t" + entry.construct() + "\t" + cell(entry)
						+ (entry.isUnsupported() || entry.isGenerationPending() ? "\t" + entry.reason() : ""));
			}
			out.println(rows.size() + (rows.size() == 1 ? " row" : " rows"));
		}
		return rows.isEmpty() ? Vecdsl.FINDINGS : Vecdsl.OK;
	}

	private int summary(CoverageIndex index, boolean json) {
		List<Object> vendors = new ArrayList<>();
		List<String> header = new ArrayList<>(List.of("vendor", "rows"));
		header.addAll(COLUMNS);
		List<String> lines = new ArrayList<>();
		lines.add(line(header));
		for (String vendor : VecDSLOutputConfigurationProvider.VENDORS) {
			Map<String, Integer> counts = new LinkedHashMap<>();
			COLUMNS.forEach(column -> counts.put(column, 0));
			index.entries(vendor).forEach(entry -> counts.merge(cell(entry), 1, Integer::sum));
			int rows = index.entries(vendor).size();
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("vendor", vendor);
			row.put("hasData", index.hasData(vendor));
			row.put("rows", rows);
			row.putAll(counts);
			vendors.add(row);
			List<String> cells = new ArrayList<>(List.of(vendor, String.valueOf(rows)));
			counts.values().forEach(count -> cells.add(String.valueOf(count)));
			lines.add(index.hasData(vendor) ? line(cells) : line(List.of(vendor, "no coverage data")));
		}
		if (json) {
			out.print(Json.write(vendors));
		} else {
			lines.forEach(out::println);
		}
		return Vecdsl.OK;
	}

	/** A table line: the vendor left-aligned in 14 columns, every other cell right-aligned to its header's width. */
	private static String line(List<String> cells) {
		StringBuilder line = new StringBuilder(String.format("%-14s", cells.get(0)));
		for (int i = 1; i < cells.size(); i++) {
			int width = i == 1 ? 5 : Math.max(5, COLUMNS.get(i - 2).length());
			line.append(' ').append(cells.size() == 2 ? cells.get(i) : String.format("%" + width + "s", cells.get(i)));
		}
		return line.toString();
	}

	private static Map<String, Object> json(CoverageEntry entry) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("vendor", entry.vendor());
		row.put("construct", entry.construct());
		row.put("cell", cell(entry));
		row.put("status", entry.status().literal());
		row.put("pending", entry.pending().name().toLowerCase(Locale.ROOT));
		row.put("reason", entry.reason());
		row.put("python", entry.python());
		row.put("typescript", entry.typescript());
		row.put("java", entry.java());
		row.put("reference", entry.reference());
		row.put("notes", entry.notes());
		return row;
	}
}
