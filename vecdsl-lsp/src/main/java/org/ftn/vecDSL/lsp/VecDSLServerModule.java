package org.ftn.vecDSL.lsp;

import org.eclipse.xtext.generator.IShouldGenerate;
import org.eclipse.xtext.ide.server.ICapabilitiesContributor;
import org.eclipse.xtext.ide.server.ILanguageServerExtension;
import org.eclipse.xtext.service.AbstractGenericModule;

import com.google.inject.Binder;

/** Bindings only the language server needs; the editor services are VecDSLIdeModule's. */
public class VecDSLServerModule extends AbstractGenericModule {

	/**
	 * The server's incremental build never runs the generators: generated code comes from vecdsl
	 * gen and the vecdsl/generate preview. Xtext's default generates on every build of a document without errors.
	 */
	public void configureIShouldGenerate(Binder binder) {
		binder.bind(IShouldGenerate.class).toInstance((resource, cancelIndicator) -> false);
	}

	/** The vecdsl/generate request. */
	public Class<? extends ILanguageServerExtension> bindILanguageServerExtension() {
		return GenerateEndpoint.class;
	}

	/** Announces vecdsl/generate in the experimental capabilities. */
	public Class<? extends ICapabilitiesContributor> bindICapabilitiesContributor() {
		return GenerateEndpoint.Capability.class;
	}
}
