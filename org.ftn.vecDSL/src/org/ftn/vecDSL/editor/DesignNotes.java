package org.ftn.vecDSL.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** One-sentence statements of the design decisions D1 to D15, read from design-notes.properties. */
public final class DesignNotes {

	private static final Properties NOTES = load();

	private DesignNotes() {
	}

	/** The statement of decision Dn, or null for an unknown number. */
	public static String note(int number) {
		return NOTES.getProperty("D" + number);
	}

	private static Properties load() {
		Properties properties = new Properties();
		try (InputStream in = DesignNotes.class.getResourceAsStream("design-notes.properties")) {
			if (in == null) {
				throw new IOException("missing design-notes.properties");
			}
			properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return properties;
	}
}
