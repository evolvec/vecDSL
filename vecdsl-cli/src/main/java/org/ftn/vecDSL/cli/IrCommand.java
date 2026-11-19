package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import org.ftn.vecDSL.ir.VecDSLIrMain;

/**
 * {@code vecdsl ir}: a thin wrapper over org.ftn.vecDSL.ir.VecDSLIrMain with its exit statuses (0 ok, 1 rejected
 * program or failed round trip, 2 usage). {@code ir <file> -o out.json} is {@code VecDSLIrMain ir <file> out.json}.
 */
final class IrCommand implements Command {

	static final String CHECK = "--check";
	static final String EXPORT = "--export";
	static final String OUTPUT = "--output";

	private final PrintStream out;
	private final PrintStream err;

	IrCommand(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(CHECK, EXPORT), Set.of(OUTPUT)).alias("-o", OUTPUT);
	}

	@Override
	public String usage() {
		return "vecdsl ir <file> [-o out.json] | vecdsl ir --check <folder> | vecdsl ir --export <folder> <out-folder>";
	}

	@Override
	public int run(Args args) throws IOException, UsageException {
		String[] forwarded;
		if (args.flag(CHECK) || args.flag(EXPORT)) {
			if (args.flag(CHECK) && args.flag(EXPORT) || args.option(OUTPUT) != null) {
				throw new UsageException("use one of " + CHECK + " <folder>, " + EXPORT + " <folder> <out-folder>");
			}
			List<String> operands = args.flag(CHECK) ? args.operands(1, 1, "folder")
					: args.operands(2, 2, "folder and out-folder");
			forwarded = args.flag(CHECK) ? new String[] { CHECK, operands.get(0) }
					: new String[] { EXPORT, operands.get(0), operands.get(1) };
		} else {
			String program = args.operands(1, 1, "program file").get(0);
			forwarded = args.option(OUTPUT) == null ? new String[] { program }
					: new String[] { program, args.option(OUTPUT) };
		}
		return VecDSLIrMain.run(forwarded, out, err);
	}
}
