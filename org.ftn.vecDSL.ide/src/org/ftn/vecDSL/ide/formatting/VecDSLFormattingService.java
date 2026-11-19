package org.ftn.vecDSL.ide.formatting;

import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.server.formatting.FormattingService;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;

/**
 * textDocument/formatting with the document's own line separator. Xtext's service leaves the separator to the
 * server's platform default, so on Windows it wrote CRLF into a file with LF endings, and an already formatted file
 * came back changed. The width and indentation rules are those of Xtext's service.
 */
public class VecDSLFormattingService extends FormattingService {

	@Override
	protected List<ITextReplacement> format2(XtextResource resource, ITextRegion selection,
			ITypedPreferenceValues preferences) {
		MapBasedPreferenceValues values = new MapBasedPreferenceValues(preferences, new LinkedHashMap<>());
		values.put(FormatterPreferenceKeys.lineSeparator, lineSeparator(resource.getParseResult().getRootNode().getText()));
		return super.format2(resource, selection, values);
	}

	/** The first line separator of the text, or the platform's when the text has none. */
	static String lineSeparator(String text) {
		int newline = text.indexOf('\n');
		if (newline > 0 && text.charAt(newline - 1) == '\r') {
			return "\r\n";
		}
		if (newline >= 0) {
			return "\n";
		}
		return text.indexOf('\r') >= 0 ? "\r" : System.lineSeparator();
	}
}
