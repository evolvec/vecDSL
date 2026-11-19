package org.ftn.vecDSL.lsp;

import org.eclipse.xtext.util.Modules2;
import org.ftn.vecDSL.VecDSLRuntimeModule;
import org.ftn.vecDSL.ide.VecDSLIdeModule;
import org.ftn.vecDSL.ide.VecDSLIdeSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The language as the server registers it (META-INF/services/org.eclipse.xtext.ISetup): VecDSLIdeSetup's injector,
 * runtime module mixed with the ide module, plus the server-only bindings of VecDSLServerModule.
 */
public class VecDSLServerSetup extends VecDSLIdeSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(
				Modules2.mixin(new VecDSLRuntimeModule(), new VecDSLIdeModule(), new VecDSLServerModule()));
	}
}
