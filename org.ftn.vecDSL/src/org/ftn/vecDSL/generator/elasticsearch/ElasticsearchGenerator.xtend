package org.ftn.vecDSL.generator.elasticsearch

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
import org.ftn.vecDSL.vecDSL.EnvRef
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.Script

/**
 * Elasticsearch target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/elasticsearch/python, typescript, java) and writes a Docker Compose file for a single-node Elasticsearch server
 * (generated/elasticsearch/docker-compose.yml). Every language uses the official client: elasticsearch (Python),
 * @elastic/elasticsearch (TypeScript) and the Rest5Client of elasticsearch-java (Java).
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class ElasticsearchGenerator implements TargetGenerator {

	public static val VENDOR = ElasticsearchPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = ElasticsearchManifest.load

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

	def List<ElasticsearchPrinter> printers(Script script) {
		val List<ElasticsearchPrinter> all = #[new ElasticsearchPython, new ElasticsearchTypeScript, new ElasticsearchJava]
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
		val statements = new ElasticsearchStatements(script)
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

	/**
	 * The default server URL of the program: the connection's uri, or one built from host and port (https with tls); the user
	 * is the connection's username, else elastic. Passwords and API keys never enter the URL: the generated program reads them
	 * from the environment variables the connection names with env(...).
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUrl = manifest.get('url')
		if (declared === null) return new Program.Connection(defaultUrl, null, null, null, null, null)
		val password = if (declared.password instanceof EnvRef) Literals.of(declared.password) else null
		val apiKey = if (declared.apiKey instanceof EnvRef) Literals.of(declared.apiKey) else null
		val url = if (declared.uri !== null) {
			declared.uri
		} else if (declared.host !== null) {
			(if (declared.tls) 'https://' else 'http://') + declared.host + ':' + (if (declared.port > 0) declared.port else manifest.get('server.port'))
		} else {
			defaultUrl
		}
		val timeout = if (declared.timeoutMillis > 0) Double.valueOf(declared.timeoutMillis / 1000.0) else null
		new Program.Connection(url, apiKey, declared.username, password, null, timeout)
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
