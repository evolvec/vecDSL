package org.ftn.vecDSL.conversion;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.KeywordAlternativeConverter;
import org.eclipse.xtext.conversion.impl.QualifiedNameValueConverter;
import org.eclipse.xtext.common.services.DefaultTerminalConverters;

import com.google.inject.Inject;

/**
 * Strips the escape caret from identifiers: {@code ^type} denotes the name {@code type}.
 */
public class VecDSLValueConverterService extends DefaultTerminalConverters {

	@Inject
	private KeywordAlternativeConverter validId;

	@Inject
	private QualifiedNameValueConverter extKey;

	@ValueConverter(rule = "ValidID")
	public IValueConverter<String> ValidID() {
		return validId;
	}

	@ValueConverter(rule = "ExtKey")
	public IValueConverter<String> ExtKey() {
		return extKey;
	}
}
