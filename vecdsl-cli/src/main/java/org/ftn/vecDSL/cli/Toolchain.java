package org.ftn.vecDSL.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.VecDSLStandaloneSetup;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.CoverageIssues;
import org.ftn.vecDSL.vecDSL.Script;

import com.google.inject.Injector;

/**
 * The standalone vecDSL runtime shared by the commands: one injector (the language's runtime module, as
 * {@link VecDSLStandaloneSetup} creates it), program loading, validation, and the coverage index of the runtime bundle.
 */
public final class Toolchain {

	public static final String EXTENSION = ".vecDSL";

	private Injector injector;
	private CoverageIndex coverage;

	public Injector injector() {
		if (injector == null) {
			injector = new VecDSLStandaloneSetup().createInjectorAndDoEMFRegistration();
		}
		return injector;
	}

	/** The coverage index the runtime bundle carries as class resources, the one the Eclipse editor reads. */
	public CoverageIndex coverage() {
		if (coverage == null) {
			coverage = CoverageIndex.fromClasspath();
		}
		return coverage;
	}

	/** A program read as UTF-8 into a resource set of its own, so that no other program is in its scope. */
	public XtextResource load(Path program) throws IOException {
		return parse(Files.readAllBytes(program), URI.createFileURI(program.toAbsolutePath().toString()));
	}

	public XtextResource parse(String text, URI uri) throws IOException {
		return parse(text.getBytes(StandardCharsets.UTF_8), uri);
	}

	private XtextResource parse(byte[] bytes, URI uri) throws IOException {
		XtextResourceSet set = injector().getInstance(XtextResourceSet.class);
		XtextResource resource = (XtextResource) set.createResource(uri);
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			resource.load(in, Map.of(XtextResource.OPTION_ENCODING, StandardCharsets.UTF_8.name()));
		}
		EcoreUtil.resolveAll(resource);
		return resource;
	}

	/**
	 * Diagnostics of a loaded program: syntax and linking errors, the validator's issues (CheckMode.ALL, as
	 * {@code VecDSLIr.errors} runs it), then, when asked, the coverage warnings the Eclipse editor adds.
	 */
	public List<Issue> validate(XtextResource resource, boolean coverageWarnings) {
		IResourceValidator validator = injector().getInstance(IResourceValidator.class);
		List<Issue> issues = new ArrayList<>(validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl));
		if (coverageWarnings) {
			issues.addAll(CoverageIssues.issues(resource, coverage()));
		}
		return issues;
	}

	public static List<Issue> errors(List<Issue> issues) {
		return issues.stream().filter(issue -> issue.getSeverity() == Severity.ERROR).toList();
	}

	public static Script script(XtextResource resource) {
		return resource.getContents().isEmpty() || !(resource.getContents().get(0) instanceof Script script) ? null
				: script;
	}

	/** The programs named by the operands: a file as given, a folder by its *.vecDSL files (not recursive), sorted. */
	public static List<Path> programs(List<String> operands) throws IOException, UsageException {
		List<Path> programs = new ArrayList<>();
		for (String operand : operands) {
			Path path = Path.of(operand);
			if (Files.isDirectory(path)) {
				try (Stream<Path> listing = Files.list(path)) {
					programs.addAll(listing.filter(file -> file.getFileName().toString().endsWith(EXTENSION))
							.filter(Files::isRegularFile).sorted().toList());
				}
			} else if (Files.isRegularFile(path)) {
				programs.add(path);
			} else {
				throw new UsageException("no such file or folder: " + operand);
			}
		}
		return programs;
	}
}
