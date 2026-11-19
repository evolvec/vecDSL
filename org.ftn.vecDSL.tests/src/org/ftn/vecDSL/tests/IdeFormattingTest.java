package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.formatting.FormattingService;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.TextRegion;
import org.ftn.vecDSL.vecDSL.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * textDocument/formatting runs VecDSLFormatter through VecDSLFormattingService: the runtime module binds IFormatter2,
 * the service passes no width, so the formatter reads the default of vecdsl.format.shortStatementWidth (100), as
 * Ctrl+Shift+F does without a project setting. Indentation follows the client's options (one tab when it does not ask
 * for spaces, the Eclipse default); line separators follow the document, whatever the platform of the server.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLIdeInjectorProvider.class)
public class IdeFormattingTest {

	static final String BROKEN = "script s\ncreate collection c {primary id field id type int vector-field v type float_vector dim 2}\n"
			+ "byId:   get from c ids [1, 2]   output [id]\n";

	static final String FORMATTED = "script s\n\ncreate collection c {\n\tprimary id\n\tfield id type int\n"
			+ "\tvector-field v type float_vector dim 2\n}\n\nbyId: get from c ids [1, 2] output [id]\n";

	@Inject
	private ParseHelper<Script> parseHelper;

	@Inject
	private FormattingService formatting;

	@Inject
	private Provider<IFormatter2> formatter;

	@Inject
	private Provider<FormatterRequest> requests;

	@Inject
	private TextRegionAccessBuilder regions;

	private String format(String text, FormattingOptions options) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		List<? extends TextEdit> edits = formatting.format(new Document(1, text), resource,
				new DocumentFormattingParams(new TextDocumentIdentifier(resource.getURI().toString()), options), () -> false);
		return IdeProtocol.apply(text, edits);
	}

	/**
	 * What Ctrl+Shift+F produces with the Eclipse defaults: ContentFormatter's request, width 100, one tab, in a
	 * workspace whose text files use LF.
	 */
	private String formatLikeEclipse(String text) throws Exception {
		XtextResource resource = (XtextResource) parseHelper.parse(text).eResource();
		FormatterRequest request = requests.get();
		request.setAllowIdentityEdits(false);
		request.setFormatUndefinedHiddenRegionsOnly(false);
		request.setRegions(List.of(new TextRegion(0, text.length())));
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
		preferences.put(FormatterPreferenceKeys.lineSeparator, "\n");
		request.setPreferences(preferences);
		request.setTextRegionAccess(regions.forNodeModel(resource).create());
		StringBuilder result = new StringBuilder(text);
		formatter.get().format(request).stream()
				.sorted((a, b) -> Integer.compare(b.getOffset(), a.getOffset()))
				.forEach(r -> result.replace(r.getOffset(), r.getEndOffset(), r.getReplacementText()));
		return result.toString();
	}

	@Test
	public void brokenLayoutIsFormattedAsCtrlShiftF() throws Exception {
		assertEquals(FORMATTED, format(BROKEN, new FormattingOptions(4, false)));
		assertEquals(formatLikeEclipse(BROKEN), format(BROKEN, new FormattingOptions(4, false)));
	}

	@Test
	public void formattedTextIsUnchanged() throws Exception {
		assertEquals(FORMATTED, format(FORMATTED, new FormattingOptions(4, false)));
	}

	@Test
	public void spacesWhenTheClientAsksForThem() throws Exception {
		assertEquals(FORMATTED.replace("\t", "    "), format(BROKEN, new FormattingOptions(4, true)));
	}

	@Test
	public void lineSeparatorsFollowTheDocument() throws Exception {
		assertEquals(FORMATTED.replace("\n", "\r\n"),
				format(BROKEN.replace("\n", "\r\n"), new FormattingOptions(4, false)));
	}
}
