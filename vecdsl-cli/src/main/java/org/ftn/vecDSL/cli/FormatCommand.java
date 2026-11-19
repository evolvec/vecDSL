package org.ftn.vecDSL.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.preferences.IntegerKey;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ExceptionAcceptor;
import org.ftn.vecDSL.formatting2.VecDSLFormatter;

import com.google.inject.Injector;

/**
 * {@code vecdsl fmt}: the language's Formatter2 (VecDSLFormatter, bound in VecDSLRuntimeModule) through the standalone
 * injector, with the request VecDSLFormatterTest builds. Before any output, the formatted text is parsed again and
 * must give the same tokens and a model equal to the original (EcoreUtil.equals); otherwise nothing is written and the
 * command fails with an internal error. A program with syntax errors is not formatted, as in the editor.
 */
final class FormatCommand implements Command {

	static final String CHECK = "--check";
	static final String WRITE = "--write";
	static final String WIDTH = "--width";
	static final String INDENT = "--indent";

	static final int DEFAULT_INDENT = 4;

	private final Toolchain toolchain;
	private final PrintStream out;

	FormatCommand(Toolchain toolchain, PrintStream out) {
		this.toolchain = toolchain;
		this.out = out;
	}

	@Override
	public Args.Spec spec() {
		return Args.Spec.of(Set.of(CHECK, WRITE), Set.of(WIDTH, INDENT));
	}

	@Override
	public String usage() {
		return "vecdsl fmt <files or folders> [--check | --write] [--width N] [--indent N|tab]";
	}

	@Override
	public int run(Args args) throws IOException, UsageException {
		List<Path> programs = Toolchain.programs(args.operands(1, Integer.MAX_VALUE, "program files or folders"));
		boolean check = args.flag(CHECK);
		boolean write = args.flag(WRITE);
		if (check && write) {
			throw new UsageException(CHECK + " and " + WRITE + " exclude each other");
		}
		if (!check && !write && programs.size() != 1) {
			throw new UsageException("printing the formatted text takes one program; use " + CHECK + " or " + WRITE);
		}
		IntegerKey widthKey = VecDSLFormatter.SHORT_STATEMENT_WIDTH;
		int width = args.positive(WIDTH, widthKey.toValue(widthKey.getDefaultValue()));
		String indentation = indentation(args.option(INDENT));
		int findings = 0;
		for (Path program : programs) {
			String text = Files.readString(program, StandardCharsets.UTF_8);
			XtextResource resource = toolchain.load(program);
			if (resource.getParseResult() == null || resource.getParseResult().hasSyntaxErrors()) {
				out.println(program + ": not formatted, the program has syntax errors (vecdsl check lists them)");
				findings++;
				continue;
			}
			String formatted = format(resource, indentation, lineSeparator(text), width);
			verify(program, resource, formatted);
			boolean changed = !formatted.equals(text);
			if (check) {
				if (changed) {
					out.println("would reformat " + program);
					findings++;
				}
			} else if (write) {
				if (changed) {
					Files.writeString(program, formatted, StandardCharsets.UTF_8);
					out.println("formatted " + program);
				}
			} else {
				out.print(formatted);
			}
		}
		return findings == 0 ? Vecdsl.OK : Vecdsl.FINDINGS;
	}

	static String indentation(String option) throws UsageException {
		if (option == null) {
			return " ".repeat(DEFAULT_INDENT);
		}
		if (option.equals("tab")) {
			return "\t";
		}
		if (option.matches("[1-9]")) {
			return " ".repeat(Integer.parseInt(option));
		}
		throw new UsageException(INDENT + " takes a number of spaces from 1 to 9 or 'tab', not '" + option + "'");
	}

	/** The line separator the program already uses: CRLF if its first line ends so, LF otherwise. */
	static String lineSeparator(String text) {
		int newline = text.indexOf('\n');
		return newline > 0 && text.charAt(newline - 1) == '\r' ? "\r\n" : "\n";
	}

	private String format(XtextResource resource, String indentation, String lineSeparator, int width) {
		Injector injector = toolchain.injector();
		ITextRegionAccess access = injector.getInstance(TextRegionAccessBuilder.class).forNodeModel(resource).create();
		FormatterRequest request = injector.getInstance(FormatterRequest.class);
		request.setTextRegionAccess(access);
		request.setExceptionHandler(ExceptionAcceptor.THROWING);
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
		preferences.put(FormatterPreferenceKeys.indentation, indentation);
		preferences.put(FormatterPreferenceKeys.lineSeparator, lineSeparator);
		preferences.put(VecDSLFormatter.SHORT_STATEMENT_WIDTH, width);
		request.setPreferences(preferences);
		return access.getRewriter().renderToString(injector.getInstance(IFormatter2.class).format(request));
	}

	/** The formatted program has the original's tokens and parses to an equal model. */
	private void verify(Path program, XtextResource original, String formatted) throws IOException {
		URI uri = URI.createFileURI(program.toAbsolutePath() + ".formatted" + Toolchain.EXTENSION);
		XtextResource again = toolchain.parse(formatted, uri);
		if (!again.getErrors().isEmpty()) {
			throw new IllegalStateException("the formatted text of " + program + " does not parse: "
					+ again.getErrors().get(0).getMessage());
		}
		if (!tokens(original).equals(tokens(again))) {
			throw new IllegalStateException("formatting changed the tokens of " + program);
		}
		EObject before = original.getContents().get(0);
		EObject after = again.getContents().get(0);
		if (!EcoreUtil.equals(before, after)) {
			throw new IllegalStateException("formatting changed the model of " + program);
		}
	}

	private static List<String> tokens(XtextResource resource) {
		List<String> tokens = new ArrayList<>();
		for (ILeafNode leaf : resource.getParseResult().getRootNode().getLeafNodes()) {
			if (!leaf.isHidden()) {
				tokens.add(leaf.getText());
			}
		}
		return tokens;
	}
}
