package org.ftn.vecDSL.ir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.VendorExtension;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Entry point of the IR toolchain: the IR or the diagnostics document of a program, and the round trip that every
 * sample must pass. A program with an error (syntax, linking or validation) has no IR; its document is
 * {"vecdsl-ir", "diagnostics"} with one entry per error, in the validator's order.
 */
public class VecDSLIr {

	/** Outcome of the round trip of one program; {@link #failures()} is empty when every step holds. */
	public record RoundTrip(String ir, List<String> failures) {
		public boolean passed() {
			return failures.isEmpty();
		}
	}

	@Inject
	private IResourceValidator validator;

	@Inject
	private ISerializer serializer;

	@Inject
	private Provider<XtextResourceSet> resourceSets;

	/** Errors of a loaded program: syntax and linking errors of the resource, then validation errors. */
	public List<Issue> errors(Resource resource) {
		EcoreUtil.resolveAll(resource);
		return validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl).stream()
				.filter(issue -> issue.getSeverity() == Severity.ERROR).toList();
	}

	/** The IR of a valid program, or its diagnostics document. */
	public String export(Resource resource) {
		List<Issue> errors = errors(resource);
		if (!errors.isEmpty()) {
			return diagnostics(errors);
		}
		return new IrWriter().write((Script) resource.getContents().get(0));
	}

	public static String diagnostics(List<Issue> errors) {
		List<Object> entries = new ArrayList<>();
		for (Issue issue : errors) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("severity", "error");
			entry.put("code", issue.getCode());
			entry.put("message", issue.getMessage());
			entry.put("line", issue.getLineNumber());
			entry.put("column", issue.getColumn());
			entries.add(entry);
		}
		Map<String, Object> document = new LinkedHashMap<>();
		document.put("vecdsl-ir", IrWriter.VERSION);
		document.put("diagnostics", entries);
		return Json.write(document);
	}

	/**
	 * The round trip of a valid program: (1) the IR of the parsed model; (2) the model rebuilt from it equals the parsed
	 * model (EcoreUtil.equals) once the parsed model's extension blocks are grouped by vendor (rule N13, the only order
	 * the IR does not keep); (3) the IR of the rebuilt model equals the first IR byte for byte; (4) the rebuilt model,
	 * serialized by the Xtext serializer and parsed again, equals the rebuilt model.
	 */
	public RoundTrip roundTrip(Resource resource) {
		List<String> failures = new ArrayList<>();
		List<Issue> errors = errors(resource);
		if (!errors.isEmpty()) {
			errors.forEach(issue -> failures.add("line " + issue.getLineNumber() + ": " + issue.getMessage()));
			return new RoundTrip(diagnostics(errors), failures);
		}
		Script parsed = (Script) resource.getContents().get(0);
		String ir = new IrWriter().write(parsed);
		Script rebuilt = new IrReader().read(ir);
		if (!EcoreUtil.equals(groupedByVendor(parsed), rebuilt)) {
			failures.add("the model rebuilt from the IR differs from the parsed model");
		}
		String again = new IrWriter().write(rebuilt);
		if (!again.equals(ir)) {
			failures.add("the IR of the rebuilt model differs from the IR at offset " + mismatch(ir, again));
		}
		ResourceSet set = resourceSets.get();
		Resource holder = set.createResource(URI.createURI("synthetic:/rebuilt.vecDSL"));
		holder.getContents().add(rebuilt);
		String text = serializer.serialize(rebuilt);
		Resource reparsed = set.createResource(URI.createURI("synthetic:/reparsed.vecDSL"));
		try {
			reparsed.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
					Map.of(XtextResource.OPTION_ENCODING, StandardCharsets.UTF_8.name()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		EcoreUtil.resolveAll(reparsed);
		if (!reparsed.getErrors().isEmpty()) {
			failures.add("the serialized rebuilt model does not parse: " + reparsed.getErrors().get(0).getMessage());
		} else if (!EcoreUtil.equals(rebuilt, reparsed.getContents().get(0))) {
			failures.add("the serialized rebuilt model parses to a different model");
		}
		return new RoundTrip(ir, failures);
	}

	/** A copy of the script whose extension blocks are grouped by vendor in first-occurrence order, as N13 writes them. */
	static Script groupedByVendor(Script script) {
		Script copy = EcoreUtil.copy(script);
		for (TreeIterator<EObject> it = EcoreUtil.getAllContents(List.of(copy)); it.hasNext();) {
			EObject element = it.next();
			for (EStructuralFeature feature : element.eClass().getEAllStructuralFeatures()) {
				if (IrWriter.isExtensions(feature)) {
					@SuppressWarnings("unchecked")
					EList<VendorExtension> blocks = (EList<VendorExtension>) element.eGet(feature);
					List<VendorExtension> grouped = new ArrayList<>(blocks);
					List<String> vendors = grouped.stream().map(VendorExtension::getVendor).distinct().toList();
					grouped.sort(Comparator.comparingInt(block -> vendors.indexOf(block.getVendor())));
					ECollections.setEList(blocks, grouped);
				}
			}
		}
		return copy;
	}

	private static int mismatch(String left, String right) {
		int i = 0;
		while (i < left.length() && i < right.length() && left.charAt(i) == right.charAt(i)) {
			i++;
		}
		return i;
	}
}
