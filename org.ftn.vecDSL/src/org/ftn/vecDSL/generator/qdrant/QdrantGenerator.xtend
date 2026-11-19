package org.ftn.vecDSL.generator.qdrant

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
 * Qdrant target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/qdrant/python, typescript, java) and writes a Docker Compose file for the Qdrant server
 * (generated/qdrant/docker-compose.yml). Python and TypeScript use Qdrant's REST API, Java its gRPC API.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class QdrantGenerator implements TargetGenerator {

	public static val VENDOR = QdrantPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = QdrantManifest.load

	override vendor() {
		VENDOR
	}

	override generate(Script script, IFileSystemAccess2 fsa) {
		val program = program(script)
		val grpcPort = grpcPort(script)
		for (printer : printers(script)) {
			for (file : printer.files(program, manifest, grpcPort).entrySet) {
				fsa.generateFile(printer.language + '/' + file.key, VENDOR, file.value)
			}
		}
		fsa.generateFile('docker-compose.yml', VENDOR, manifest.render('environment/docker-compose.yml'))
	}

	def List<QdrantPrinter> printers(Script script) {
		val List<QdrantPrinter> all = #[new QdrantPython, new QdrantTypeScript, new QdrantJava]
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
		val statements = new QdrantStatements(script)
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

	/** REST endpoint and the environment variable that holds the API key, if the script names one. */
	def private Program.Connection connection(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUri = manifest.get('rest.uri')
		if (connection === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val secret = connection.apiKey ?: connection.token
		val uri = if (connection.uri !== null) {
			connection.uri
		} else if (connection.host !== null) {
			(if (connection.tls) 'https://' else 'http://') + connection.host + ':' + (if (connection.port > 0) connection.port else manifest.get('rest.port'))
		} else {
			defaultUri
		}
		new Program.Connection(
			uri,
			if (secret instanceof EnvRef) new Code.Env(secret.variable) else null,
			null,
			null,
			null,
			if (connection.timeoutMillis > 0) connection.timeoutMillis / 1000.0 else null
		)
	}

	/** gRPC port of the Java client: the connection's grpc-port, else Qdrant's default. */
	def private int grpcPort(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		if (connection !== null && connection.grpcPort > 0) connection.grpcPort else Integer.parseInt(manifest.get('grpc.port'))
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
