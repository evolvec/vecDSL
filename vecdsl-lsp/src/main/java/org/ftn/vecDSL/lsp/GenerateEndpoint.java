package org.ftn.vecDSL.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ICapabilitiesContributor;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.ILanguageServerExtension;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.editor.CoverageCheck;
import org.ftn.vecDSL.editor.StatementCode;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Script;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * {@code vecdsl/generate}: the generated files of the open document for one target, as
 * {@code vecdsl gen <file> --target <vendor> [--lang <language>] -o <folder>} writes them, paths relative to the
 * output folder. It reads the document as the editor holds it, saved or not. A program with errors gives no files and
 * its errors, as {@code gen} does. The result also carries the script's top-level statements and
 * declared targets, the lines of the statement at {@code range} in the files and the constructs of that statement the
 * target does not support; without {@code target} nothing is generated. The generation and the statement's lines are
 * {@link StatementCode}'s, the constructs {@link CoverageCheck}'s, both in the runtime bundle.
 */
@JsonSegment("vecdsl")
public class GenerateEndpoint implements ILanguageServerExtension {

	public static final String METHOD = "vecdsl/generate";

	/** 1: files and errors; 2: also statements, targets, sections and unsupported. */
	public static final int VERSION = 2;

	/**
	 * uri, target (a vendor id or "all") and language (py, ts, java, all; null for the script's hint) are as in gen;
	 * without target the request only reads the script (statements, targets, errors).
	 */
	public record GenerateParams(String uri, Range range, String target, String language) {
	}

	/**
	 * The statement at the range's start (null without range or label), the files in the order written, the errors;
	 * the script's top-level statements and declared targets; the statement's lines in the files and its constructs the
	 * target does not support (both empty without range).
	 */
	public record GenerateResult(String statement, List<GeneratedFile> files, List<String> errors,
			List<Statement> statements, List<String> targets, List<Section> sections, List<Unsupported> unsupported) {
	}

	/** A top-level statement of the script: its label (null without one) and the range of its text. */
	public record Statement(String name, Range range) {
	}

	/** Lines start (inclusive) to end (exclusive), 0-based, of a generated file that hold the statement's code. */
	public record Section(String path, int start, int end) {
	}

	/** A construct of the statement a vendor does not support: as a vecdsl.coverage.unsupported warning would say. */
	public record Unsupported(String vendor, String feature, String message, Range range) {
	}

	/** A file as gen writes it: path under the output folder (vendor/language/...), the text. */
	public record GeneratedFile(String path, String text) {
	}

	private ILanguageServerAccess access;

	@Override
	public void initialize(ILanguageServerAccess access) {
		this.access = access;
	}

	@JsonRequest
	public CompletableFuture<GenerateResult> generate(GenerateParams params) {
		String targets = null;
		String languages = null;
		try {
			if (params == null || params.uri() == null) {
				throw new IllegalArgumentException("uri is required");
			}
			if (params.target() != null) {
				targets = StatementCode.targets(params.target());
				languages = StatementCode.languages(params.language());
			}
		} catch (IllegalArgumentException e) {
			return CompletableFuture.failedFuture(new ResponseErrorException(
					new ResponseError(ResponseErrorCode.InvalidParams, e.getMessage(), null)));
		}
		Selection selection = new Selection(params.target(), targets, languages);
		return access.doRead(params.uri(), context -> generate((XtextResource) context.getResource(),
				context.getDocument(), params.range(), selection, context.getCancelChecker()));
	}

	/** The request's target (null: nothing is generated) and the values of vecdsl.targets and vecdsl.languages. */
	private record Selection(String target, String targets, String languages) {

		List<String> vendors() {
			return target.equals("all") ? VecDSLOutputConfigurationProvider.VENDORS : List.of(target);
		}
	}

	private static GenerateResult generate(XtextResource resource, Document document, Range range, Selection selection,
			CancelIndicator cancel) {
		IResourceServiceProvider services = resource.getResourceServiceProvider();
		Script script = resource.getContents().isEmpty() ? null
				: resource.getContents().get(0) instanceof Script candidate ? candidate : null;
		Method method = range == null ? null : StatementCode.statement(resource, document.getOffSet(range.getStart()));
		String statement = method == null ? null : method.getName();
		List<String> errors = services.getResourceValidator().validate(resource, CheckMode.ALL, cancel).stream()
				.filter(issue -> issue.getSeverity() == Severity.ERROR)
				.map(issue -> issue.getLineNumber() + ":" + issue.getColumn() + ": " + issue.getMessage()).toList();
		List<Statement> statements = script == null ? List.of()
				: script.getMethods().stream().map(each -> new Statement(each.getName(), range(document,
						NodeModelUtils.getNode(each)))).toList();
		List<String> targets = script == null ? List.of() : List.copyOf(script.getTargets());
		if (selection.target() == null) {
			return new GenerateResult(statement, List.of(), errors, statements, targets, List.of(), List.of());
		}
		List<Unsupported> unsupported = method == null ? List.of()
				: unsupported(method, selection.vendors(), services.get(CoverageIndex.class), document);
		if (!errors.isEmpty()) {
			return new GenerateResult(statement, List.of(), errors, statements, targets, List.of(), unsupported);
		}
		StatementCode.Code code = StatementCode.generate(resource, method, selection.targets(), selection.languages(),
				cancel);
		List<GeneratedFile> generated = code.files().entrySet().stream()
				.map(file -> new GeneratedFile(file.getKey(), file.getValue())).toList();
		List<Section> sections = code.sections().stream()
				.map(section -> new Section(section.path(), section.start(), section.end())).toList();
		return new GenerateResult(statement, generated, List.of(), statements, targets, sections, unsupported);
	}

	/** The constructs of the statement the vendors do not support, as the vecdsl.coverage.unsupported warnings say. */
	private static List<Unsupported> unsupported(Method statement, List<String> vendors, CoverageIndex index,
			Document document) {
		return CoverageCheck.findings(statement, vendors, index).stream().filter(finding -> finding.node() != null)
				.map(finding -> new Unsupported(finding.entry().vendor(),
						finding.feature() == null ? "" : finding.feature().getName(), finding.message(),
						range(document, finding.node())))
				.toList();
	}

	private static Range range(Document document, INode node) {
		return new Range(document.getPosition(node.getOffset()), document.getPosition(node.getEndOffset()));
	}

	/**
	 * {@code "experimental": {"vecdsl": {"generate": "vecdsl/generate", "generateVersion": 2, "targets": [...]}}} in
	 * the initialize result: version 2 has the outline, sections and unsupported fields, targets are the vendors the request takes.
	 */
	public static class Capability implements ICapabilitiesContributor {

		@Override
		public ServerCapabilities contribute(ServerCapabilities capabilities, InitializeParams params) {
			JsonObject vecdsl = new JsonObject();
			vecdsl.addProperty("generate", METHOD);
			vecdsl.addProperty("generateVersion", VERSION);
			JsonArray targets = new JsonArray();
			VecDSLOutputConfigurationProvider.VENDORS.forEach(targets::add);
			vecdsl.add("targets", targets);
			JsonObject experimental = new JsonObject();
			experimental.add("vecdsl", vecdsl);
			capabilities.setExperimental(experimental);
			return capabilities;
		}
	}
}
