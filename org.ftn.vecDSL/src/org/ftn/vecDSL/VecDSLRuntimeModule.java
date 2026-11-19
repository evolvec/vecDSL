package org.ftn.vecDSL;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.formatting2.FormatterPreferenceValuesProvider;
import org.eclipse.xtext.formatting2.FormatterPreferences;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.ftn.vecDSL.conversion.VecDSLValueConverterService;
import org.ftn.vecDSL.formatting2.VecDSLFormatter;
import org.ftn.vecDSL.generator.VecDSLOutputConfigurationProvider;
import org.ftn.vecDSL.naming.VecDSLQualifiedNameProvider;

import com.google.inject.Binder;
import com.google.inject.Singleton;

public class VecDSLRuntimeModule extends AbstractVecDSLRuntimeModule {

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return VecDSLQualifiedNameProvider.class;
	}

	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return VecDSLValueConverterService.class;
	}

	public void configureIOutputConfigurationProvider(Binder binder) {
		binder.bind(IOutputConfigurationProvider.class).to(VecDSLOutputConfigurationProvider.class).in(Singleton.class);
	}

	public Class<? extends IFormatter2> bindIFormatter2() {
		return VecDSLFormatter.class;
	}

	public void configureFormatterPreferences(Binder binder) {
		binder.bind(IPreferenceValuesProvider.class).annotatedWith(FormatterPreferences.class)
				.to(FormatterPreferenceValuesProvider.class);
	}
}
