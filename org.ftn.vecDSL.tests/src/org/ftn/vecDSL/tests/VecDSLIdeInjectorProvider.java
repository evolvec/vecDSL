package org.ftn.vecDSL.tests;

import org.eclipse.xtext.util.Modules2;
import org.ftn.vecDSL.ide.VecDSLIdeModule;
import org.ftn.vecDSL.ide.VecDSLIdeSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

/** The injector of the language server: the runtime module mixed with VecDSLIdeModule, as VecDSLIdeSetup builds it. */
public class VecDSLIdeInjectorProvider extends VecDSLInjectorProvider {

	@Override
	protected Injector internalCreateInjector() {
		return new VecDSLIdeSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(Modules2.mixin(createRuntimeModule(), new VecDSLIdeModule()));
			}
		}.createInjectorAndDoEMFRegistration();
	}
}
