package org.ftn.vecDSL.editor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The content of a hover as blocks of styled text, built once by {@link HoverContent} and rendered as HTML for the
 * Eclipse editor or as Markdown for the language server. Renderers escape every text; the model holds plain text.
 */
public record HoverDocument(List<Block> blocks) {

	public sealed interface Block permits Paragraph, Items, Table, Lines {
	}

	public record Paragraph(List<Span> spans) implements Block {
	}

	/** A bulleted list, one item per entry. */
	public record Items(List<List<Span>> items) implements Block {
	}

	/** A table with a header row; a cell is a list of spans. */
	public record Table(List<String> header, List<List<List<Span>>> rows) implements Block {
	}

	/** Lines of code shown as written, in a fixed-width font. */
	public record Lines(List<String> lines) implements Block {
	}

	public sealed interface Span permits Text, Bold, Italic, Code, Link, Break {
	}

	public record Text(String text) implements Span {
	}

	public record Bold(String text) implements Span {
	}

	public record Italic(String text) implements Span {
	}

	public record Code(String text) implements Span {
	}

	public record Link(String url, String text) implements Span {
	}

	public record Break() implements Span {
	}

	public String html() {
		return blocks.stream().map(HoverDocument::html).collect(Collectors.joining());
	}

	public String markdown() {
		return blocks.stream().map(HoverDocument::markdown).collect(Collectors.joining("\n\n"));
	}

	private static String html(Block block) {
		return switch (block) {
			case Paragraph paragraph -> "<p>" + html(paragraph.spans()) + "</p>";
			case Items items -> items.items().stream().map(item -> "<li>" + html(item) + "</li>")
					.collect(Collectors.joining("", "<ul>", "</ul>"));
			case Table table -> "<table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">"
					+ table.header().stream().map(head -> "<th>" + escapeHtml(head) + "</th>")
							.collect(Collectors.joining("", "<tr>", "</tr>"))
					+ table.rows().stream().map(row -> row.stream().map(cell -> "<td>" + html(cell) + "</td>")
							.collect(Collectors.joining("", "<tr>", "</tr>"))).collect(Collectors.joining())
					+ "</table>";
			case Lines lines -> lines.lines().stream().map(HoverDocument::escapeHtml)
					.collect(Collectors.joining("\n", "<pre>", "</pre>"));
		};
	}

	private static String html(List<Span> spans) {
		return spans.stream().map(span -> switch (span) {
			case Text text -> escapeHtml(text.text());
			case Bold bold -> "<b>" + escapeHtml(bold.text()) + "</b>";
			case Italic italic -> "<i>" + escapeHtml(italic.text()) + "</i>";
			case Code code -> "<code>" + escapeHtml(code.text()) + "</code>";
			case Link link -> "<a href=\"" + escapeHtml(link.url()) + "\">" + escapeHtml(link.text()) + "</a>";
			case Break ignored -> "<br/>";
		}).collect(Collectors.joining());
	}

	private static String markdown(Block block) {
		return switch (block) {
			case Paragraph paragraph -> markdown(paragraph.spans(), "\\\n");
			case Items items -> items.items().stream().map(item -> "- " + markdown(item, " "))
					.collect(Collectors.joining("\n"));
			case Table table -> row(table.header().stream().map(HoverDocument::escapeMarkdown).toList()) + "\n"
					+ row(table.header().stream().map(head -> "---").toList())
					+ table.rows().stream().map(cells -> "\n" + row(cells.stream().map(cell -> markdown(cell, "<br>"))
							.toList())).collect(Collectors.joining());
			case Lines lines -> fenced(lines.lines());
		};
	}

	/** A fenced code block whose fence is longer than any backtick run in the lines. */
	private static String fenced(List<String> lines) {
		String text = String.join("\n", lines);
		String fence = "```";
		while (text.contains(fence)) {
			fence += "`";
		}
		return fence + "\n" + text + "\n" + fence;
	}

	private static String row(List<String> cells) {
		return cells.stream().collect(Collectors.joining(" | ", "| ", " |"));
	}

	/** The spans as Markdown; a line break becomes the given text (a hard break, or {@code <br>} inside a table). */
	private static String markdown(List<Span> spans, String lineBreak) {
		return spans.stream().map(span -> switch (span) {
			case Text text -> escapeMarkdown(text.text());
			case Bold bold -> "**" + escapeMarkdown(bold.text()) + "**";
			case Italic italic -> "*" + escapeMarkdown(italic.text()) + "*";
			case Code code -> code(code.text());
			case Link link -> "[" + escapeMarkdown(link.text()) + "](<" + link.url().replace(">", "%3E") + ">)";
			case Break ignored -> lineBreak;
		}).collect(Collectors.joining());
	}

	/** A code span whose delimiter is longer than any backtick run in the text; nothing for an empty text. */
	private static String code(String text) {
		if (text.isEmpty()) {
			return "";
		}
		String fence = "`";
		while (text.contains(fence)) {
			fence += "`";
		}
		String padding = fence.length() > 1 ? " " : "";
		return fence + padding + text + padding + fence;
	}

	static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	/**
	 * Backslash before every character Markdown could read as markup. An underscore between two letters or digits
	 * stays as it is (it cannot start emphasis), so identifiers such as float_vector read as written.
	 */
	static String escapeMarkdown(String text) {
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			boolean intraword = c == '_' && i > 0 && i < text.length() - 1
					&& Character.isLetterOrDigit(text.charAt(i - 1)) && Character.isLetterOrDigit(text.charAt(i + 1));
			if ("\\`*_[]<>|&#".indexOf(c) >= 0 && !intraword) {
				out.append('\\');
			}
			out.append(c == '\n' ? ' ' : c);
		}
		return out.toString();
	}
}
