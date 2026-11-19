package org.ftn.vecDSL.generator.weaviate

import java.util.List
import java.util.Set
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.ftn.vecDSL.generator.TargetGenerator
import org.ftn.vecDSL.generator.code.Literals
import org.ftn.vecDSL.generator.code.Program
import org.ftn.vecDSL.generator.code.Syntax
import org.ftn.vecDSL.util.ModelUtil
import org.ftn.vecDSL.vecDSL.Connection
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.Script

/**
 * Weaviate target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/weaviate/python, typescript, java) and writes a Docker Compose file for a single-node server of
 * the supported version (generated/weaviate/docker-compose.yml).
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class WeaviateGenerator implements TargetGenerator {

	public static val VENDOR = WeaviatePrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = WeaviateManifest.load

	override vendor() {
		VENDOR
	}

	override generate(Script script, IFileSystemAccess2 fsa) {
		val program = program(script)
		val target = manifest.with('grpc.port', String.valueOf(grpcPort(script)))
		for (printer : printers(script)) {
			for (file : printer.files(program, target).entrySet) {
				fsa.generateFile(printer.language + '/' + file.key, VENDOR, file.value)
			}
		}
		fsa.generateFile('docker-compose.yml', VENDOR, manifest.render('environment/docker-compose.yml'))
	}

	def List<WeaviatePrinter> printers(Script script) {
		val List<WeaviatePrinter> all = #[new WeaviatePython, new WeaviateTypeScript, new WeaviateJava]
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
		val statements = new WeaviateStatements(script)
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

	def private Program.Connection connection(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		if (connection === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val token = connection.apiKey ?: connection.token
		new Program.Connection(
			uri(connection),
			if (token === null) null else Literals.of(token),
			null,
			null,
			null,
			if (connection.timeoutMillis > 0) connection.timeoutMillis / 1000.0 else null
		)
	}

	def private String uri(Connection connection) {
		if (connection.uri !== null) return connection.uri
		if (connection.host !== null) {
			val scheme = if (connection.tls) 'https' else 'http'
			return scheme + '://' + connection.host + ':' + (if (connection.port > 0) String.valueOf(connection.port) else manifest.get('server.port'))
		}
		defaultUri
	}

	def private int grpcPort(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		if (connection !== null && connection.grpcPort > 0) connection.grpcPort else Integer.parseInt(manifest.get('grpc.port'))
	}

	def private String defaultUri() {
		'http://localhost:' + manifest.get('server.port')
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
