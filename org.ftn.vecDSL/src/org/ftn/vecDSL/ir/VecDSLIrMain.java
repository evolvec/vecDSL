package org.ftn.vecDSL.ir;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.ftn.vecDSL.VecDSLStandaloneSetup;

import com.google.inject.Injector;

/**
 * Standalone IR commands (the {@code ir} command of the later vecdsl-cli):
 *
 * <pre>
 * ir &lt;program.vecDSL&gt; [out.json]   write the IR, or the diagnostics document of a rejected program
 * ir --check &lt;dir&gt;                  run the round trip on every .vecDSL file of the directory
 * ir --export &lt;dir&gt; &lt;out-dir&gt;      write &lt;name&gt;.json (IR or diagnostics) for every .vecDSL file of the directory
 * </pre>
 *
 * Exit status: 0 on success, 1 when a program is rejected (except with --export) or a round trip fails, 2 on a usage
 * error.
 */
public final class VecDSLIrMain {

	private VecDSLIrMain() {
	}

	public static void main(String[] args) throws IOException {
		System.exit(run(args, System.out, System.err));
	}

	public static int run(String[] args, PrintStream out, PrintStream err) throws IOException {
		if (args.length == 2 && args[0].equals("--check")) {
			return check(Path.of(args[1]), out);
		}
		if (args.length == 3 && args[0].equals("--export")) {
			return exportAll(Path.of(args[1]), Path.of(args[2]), out);
		}
		if (args.length == 1 || args.length == 2 && !args[0].startsWith("--")) {
			return export(Path.of(args[0]), args.length == 2 ? Path.of(args[1]) : null, out);
		}
		err.println("usage: ir <program.vecDSL> [out.json] | ir --check <dir> | ir --export <dir> <out-dir>");
		return 2;
	}

	private static int export(Path program, Path target, PrintStream out) throws IOException {
		Injector injector = new VecDSLStandaloneSetup().createInjectorAndDoEMFRegistration();
		VecDSLIr ir = injector.getInstance(VecDSLIr.class);
		Resource resource = load(injector, program);
		boolean valid = ir.errors(resource).isEmpty();
		String document = ir.export(resource);
		if (target == null) {
			out.print(document);
		} else {
			Files.writeString(target, document, StandardCharsets.UTF_8);
		}
		return valid ? 0 : 1;
	}

	private static int exportAll(Path directory, Path target, PrintStream out) throws IOException {
		Injector injector = new VecDSLStandaloneSetup().createInjectorAndDoEMFRegistration();
		VecDSLIr ir = injector.getInstance(VecDSLIr.class);
		Files.createDirectories(target);
		List<Path> programs = programs(directory);
		for (Path program : programs) {
			String name = program.getFileName().toString().replaceFirst("\\.vecDSL$", ".json");
			Files.writeString(target.resolve(name), ir.export(load(injector, program)), StandardCharsets.UTF_8);
			out.println("wrote " + target.resolve(name));
		}
		return programs.isEmpty() ? 1 : 0;
	}

	private static int check(Path directory, PrintStream out) throws IOException {
		Injector injector = new VecDSLStandaloneSetup().createInjectorAndDoEMFRegistration();
		VecDSLIr ir = injector.getInstance(VecDSLIr.class);
		List<Path> programs = programs(directory);
		int failed = 0;
		for (Path program : programs) {
			VecDSLIr.RoundTrip result = ir.roundTrip(load(injector, program));
			if (result.passed()) {
				out.println("ok   " + program.getFileName() + " (" + result.ir().getBytes(StandardCharsets.UTF_8).length
						+ " bytes of IR)");
			} else {
				failed++;
				out.println("FAIL " + program.getFileName());
				result.failures().forEach(failure -> out.println("     " + failure));
			}
		}
		out.println(programs.size() + " programs, " + (programs.size() - failed) + " round trips passed, " + failed
				+ " failed");
		return failed == 0 && !programs.isEmpty() ? 0 : 1;
	}

	private static List<Path> programs(Path directory) throws IOException {
		try (Stream<Path> listing = Files.list(directory)) {
			return listing.filter(path -> path.getFileName().toString().endsWith(".vecDSL")).sorted().toList();
		}
	}

	private static Resource load(Injector injector, Path program) {
		XtextResourceSet set = injector.getInstance(XtextResourceSet.class);
		return set.getResource(URI.createFileURI(program.toAbsolutePath().toString()), true);
	}
}
