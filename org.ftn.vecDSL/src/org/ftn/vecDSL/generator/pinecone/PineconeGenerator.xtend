package org.ftn.vecDSL.generator.pinecone

import java.util.List
import java.util.Set
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.ftn.vecDSL.generator.TargetGenerator
import org.ftn.vecDSL.generator.code.Code
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.code.Syntax
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Connection
import org.ftn.vecDSL.vecDSL.EnvRef
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.Script

/**
 * Pinecone target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/pinecone/python, typescript, java) and writes a Docker Compose file for the Pinecone API mock
 * (generated/pinecone/docker-compose.yml), which checks requests against the API specification. Pinecone itself
 * runs only as a managed service; Pinecone Local reached end of life on 7 October 2026.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class PineconeGenerator implements TargetGenerator {

	public static val VENDOR = PineconePrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = PineconeManifest.load

	override vendor() {
		VENDOR
	}

	override generate(Script script, IFileSystemAccess2 fsa) {
		val program = program(script)
		for (printer : printers(script)) {
			for (file : printer.files(program, manifest).entrySet) {
				fsa.generateFile(printer.language + '/' + file.key, VENDOR, file.value)
			}
		}
		fsa.generateFile('docker-compose.yml', VENDOR, manifest.render('environment/docker-compose.yml'))
	}

	def List<PineconePrinter> printers(Script script) {
		val List<PineconePrinter> all = #[new PineconePython, new PineconeTypeScript, new PineconeJava]
		val requested = System.getProperty(LANGUAGES_PROPERTY)
		val Set<String> selected = if (requested !== null && !requested.blank) {
			requested.split(',').map[trim.toLowerCase].toSet
		} else if (ModelUtil.written(script, 'hostLanguage')) {
			#{script.hostLanguage.literal}
		} else {
			all.map[language].toSet
		}
		all.filter[selected.contains(language)].toList
	}

	def Program program(Script script) {
		val statements = new PineconeStatements(script, manifest.get('namespace'))
		val List<Program.Method> methods = newArrayList
		val Set<String> used = newHashSet
		var index = 0
		for (statement : script.methods) {
			index = index + 1
			methods += statements.methods(statement, uniqueName(statement, index, used))
		}
		val parameters = script.parameters.map[
			new Program.Parameter(name, type.literal, if (defaultValue === null) null else Literals.of(defaultValue))
		]
		new Program(script.name, script.version, script.description, parameters, connection(script), methods)
	}

	/** Control-plane URI and the environment variable that holds the API key (VECDSL_PINECONE_API_KEY by default). */
	def private Program.Connection connection(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUri = manifest.get('control.uri')
		if (connection === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val secret = connection.apiKey ?: connection.token
		new Program.Connection(
			connection.uri ?: defaultUri,
			if (secret instanceof EnvRef) new Code.Env(secret.variable) else null,
			null,
			null,
			null,
			if (connection.timeoutMillis > 0) connection.timeoutMillis / 1000.0 else null
		)
	}

	def private String uniqueName(Method statement, int index, Set<String> used) {
		val feature = statement.eClass.getEStructuralFeature('name')
		val label = if (feature === null) null else statement.eGet(feature) as String
		var name = Syntax.snake(label ?: statement.eClass.name + '_' + index)
		if (!used.add(name)) {
			name = name + '_' + index
			used += name
		}
		name
	}
}
