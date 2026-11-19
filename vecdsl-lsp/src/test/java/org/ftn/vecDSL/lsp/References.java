package org.ftn.vecDSL.lsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.common.TerminalsStandaloneSetup;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.generator.trace.TraceFileNameProvider;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.preferences.IntegerKey;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.ExceptionAcceptor;
import org.ftn.vecDSL.VecDSLStandaloneSetup;
import org.ftn.vecDSL.editor.StatementCode;
import org.ftn.vecDSL.formatting2.VecDSLFormatter;
import org.ftn.vecDSL.generator.VecDSLGenerator;
import org.ftn.vecDSL.vecDSL.VecDSLPackage;

import com.google.inject.Injector;

/**
 * What vecdsl-cli writes, for the client's expected values: {@code vecdsl fmt} (the request of FormatCommand)
 * and {@code vecdsl gen} (GeneratorDelegate into a JavaIoFileSystemAccess), both through an
 * injector of the runtime bundle's standalone setup. The Python client compares with the CLI itself.
 */
final class References {

	private static final Injector INJECTOR = injector();

	private References() {
	}

	/**
	 * What VecDSLStandaloneSetupGenerated.createInjectorAndDoEMFRegistration does, except registering vecDSL's resource
	 * factory and service provider with EMF: in the surefire run the server registers its own in this JVM, and that one
	 * must stay.
	 */
	private static Injector injector() {
		TerminalsStandaloneSetup.doSetup();
		EPackage.Registry.INSTANCE.putIfAbsent(VecDSLPackage.eNS_URI, VecDSLPackage.eINSTANCE);
		return new VecDSLStandaloneSetup().createInjector();
	}

	private static XtextResource load(String text, URI uri) {
		XtextResource resource = (XtextResource) INJECTOR.getInstance(IResourceFactory.class).createResource(uri);
		INJECTOR.getInstance(XtextResourceSet.class).getResources().add(resource);
		try {
			resource.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), null);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return resource;
	}

	/** {@code vecdsl fmt} with the indentation ("    " is fmt's default, "\t" is --indent tab). */
	static String fmt(String text, String indentation) {
		XtextResource resource = load(text, URI.createURI("fmt.vecDSL"));
		ITextRegionAccess access = INJECTOR.getInstance(TextRegionAccessBuilder.class).forNodeModel(resource).create();
		FormatterRequest request = INJECTOR.getInstance(FormatterRequest.class);
		request.setTextRegionAccess(access);
		request.setExceptionHandler(ExceptionAcceptor.THROWING);
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
		preferences.put(FormatterPreferenceKeys.indentation, indentation);
		preferences.put(FormatterPreferenceKeys.lineSeparator, text.contains("\r\n") ? "\r\n" : "\n");
		IntegerKey width = VecDSLFormatter.SHORT_STATEMENT_WIDTH;
		preferences.put(width, width.toValue(width.getDefaultValue()));
		request.setPreferences(preferences);
		return access.getRewriter().renderToString(INJECTOR.getInstance(IFormatter2.class).format(request));
	}

	/** {@code vecdsl gen <file> --target <target> --lang <language> -o <folder>}: path under the folder to text. */
	static Map<String, String> gen(Path program, String target, String language, Path folder) throws IOException {
		XtextResource resource = load(Files.readString(program), URI.createFileURI(program.toString()));
		IEncodingProvider.Runtime utf8 = new IEncodingProvider.Runtime();
		utf8.setDefaultEncoding(StandardCharsets.UTF_8.name());
		JavaIoFileSystemAccess fsa = new JavaIoFileSystemAccess(IResourceServiceProvider.Registry.INSTANCE, utf8,
				INJECTOR.getInstance(TraceFileNameProvider.class), INJECTOR.getInstance(TraceRegionSerializer.class));
		Map<String, OutputConfiguration> configurations = INJECTOR.getInstance(IOutputConfigurationProvider.class)
				.getOutputConfigurations().stream()
				.collect(Collectors.toMap(OutputConfiguration::getName, configuration -> configuration));
		fsa.setOutputConfigurations(configurations);
		configurations.keySet().forEach(name -> fsa.setOutputPath(name,
				folder.resolve(name.equals(IFileSystemAccess.DEFAULT_OUTPUT) ? "src-gen" : name).toString()));
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		System.setProperty(VecDSLGenerator.TARGETS_PROPERTY, target);
		System.setProperty(StatementCode.LANGUAGES_PROPERTY, language);
		try {
			INJECTOR.getInstance(GeneratorDelegate.class).generate(resource, fsa, context);
		} finally {
			System.clearProperty(VecDSLGenerator.TARGETS_PROPERTY);
			System.clearProperty(StatementCode.LANGUAGES_PROPERTY);
		}
		Map<String, String> files = new TreeMap<>();
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.filter(Files::isRegularFile).toList()) {
				files.put(folder.relativize(path).toString().replace('\\', '/'), Files.readString(path));
			}
		}
		return files;
	}
}
